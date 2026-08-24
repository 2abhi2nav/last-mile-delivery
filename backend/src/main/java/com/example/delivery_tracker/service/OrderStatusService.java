package com.example.delivery_tracker.service;

import com.example.delivery_tracker.model.Agent;
import com.example.delivery_tracker.model.Order;
import com.example.delivery_tracker.model.OrderStatusHistory;
import com.example.delivery_tracker.model.User;
import com.example.delivery_tracker.repository.AgentRepository;
import com.example.delivery_tracker.repository.OrderRepository;
import com.example.delivery_tracker.repository.OrderStatusHistoryRepository;
import com.example.delivery_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderStatusService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public List<OrderStatusHistory> getOrderTimeline(Long orderId, String username, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (!isAdmin) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            if (!order.getMerchant().getId().equals(user.getId())) {
                throw new SecurityException("Unauthorized access to order timeline");
            }
        }

        return orderStatusHistoryRepository.findByOrderId(orderId);
    }

    @Transactional
    public Order updateOrderStatusByAgent(Long orderId, Order.OrderStatus newStatus, String agentUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        User user = userRepository.findByUsername(agentUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Agent agent = agentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Agent profile not found for user"));

        if (order.getAssignedAgent() == null || !order.getAssignedAgent().getId().equals(agent.getId())) {
            throw new SecurityException("Agent is not assigned to this order");
        }

        validateTransition(order.getCurrentStatus(), newStatus);

        order.setCurrentStatus(newStatus);
        orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(newStatus)
                .timestamp(LocalDateTime.now())
                .actorId(user.getId())
                .actorRole("AGENT")
                .build();
        orderStatusHistoryRepository.save(history);

        // Trigger non-blocking notification on status change hook
        try {
            notificationService.sendStatusChangeNotification(order, newStatus);
        } catch (Exception e) {
            // Non-blocking: swallow exception so notification failure never rolls back the status change
        }

        return order;
    }

    @Transactional
    public Order adminOverrideOrderStatus(Long orderId, Order.OrderStatus newStatus, String adminUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        order.setCurrentStatus(newStatus);
        orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(newStatus)
                .timestamp(LocalDateTime.now())
                .actorId(admin.getId())
                .actorRole("ADMIN")
                .build();
        orderStatusHistoryRepository.save(history);

        // Trigger non-blocking notification on status change hook
        try {
            notificationService.sendStatusChangeNotification(order, newStatus);
        } catch (Exception e) {
            // Non-blocking
        }

        return order;
    }

    private void validateTransition(Order.OrderStatus current, Order.OrderStatus next) {
        boolean valid = switch (current) {
            case CREATED -> next == Order.OrderStatus.PICKED_UP || next == Order.OrderStatus.RETURNED;
            case PICKED_UP -> next == Order.OrderStatus.IN_TRANSIT || next == Order.OrderStatus.RETURNED;
            case IN_TRANSIT -> next == Order.OrderStatus.OUT_FOR_DELIVERY || next == Order.OrderStatus.RETURNED;
            case OUT_FOR_DELIVERY -> next == Order.OrderStatus.DELIVERED || next == Order.OrderStatus.RETURNED;
            case DELIVERED, RETURNED -> false;
        };

        if (!valid) {
            throw new IllegalStateException("Invalid status transition from " + current + " to " + next);
        }
    }
}
