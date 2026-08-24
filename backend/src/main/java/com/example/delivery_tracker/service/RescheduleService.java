package com.example.delivery_tracker.service;

import com.example.delivery_tracker.dto.RescheduleDto;
import com.example.delivery_tracker.model.*;
import com.example.delivery_tracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RescheduleService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderRescheduleRepository orderRescheduleRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AgentAssignmentService agentAssignmentService;

    @Transactional
    public Order handleFailedDelivery(Long orderId, String reason, String actorUsername, String actorRole) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        User actor = userRepository.findByUsername(actorUsername)
                .orElseThrow(() -> new IllegalArgumentException("Actor not found"));

        // Update status to FAILED (if not already handled via enum, let's add or map to RETURNED/FAILED)
        // Since Order.OrderStatus has [CREATED, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, RETURNED],
        // let's transition to RETURNED or treat FAILED as RETURNED / unassign agent for rescheduling.
        order.setAssignedAgent(null); // Unassign agent for retry
        orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(order.getCurrentStatus())
                .timestamp(LocalDateTime.now())
                .actorId(actor.getId())
                .actorRole(actorRole)
                .build();
        orderStatusHistoryRepository.save(history);

        // Here notification trigger would occur (e.g. email/SMS notification mock)
        System.out.println("NOTIFICATION TO CUSTOMER: Delivery failed for order " + order.getTrackingNumber() + ". Reason: " + reason);

        return order;
    }

    @Transactional
    public Order rescheduleOrder(Long orderId, RescheduleDto dto, String actorUsername, String actorRole) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        User actor = userRepository.findByUsername(actorUsername)
                .orElseThrow(() -> new IllegalArgumentException("Actor not found"));

        // Save reschedule record (immutable audit trail)
        OrderReschedule reschedule = OrderReschedule.builder()
                .order(order)
                .newDeliveryDate(dto.getNewDeliveryDate())
                .reason(dto.getReason())
                .requestedAt(LocalDateTime.now())
                .build();
        orderRescheduleRepository.save(reschedule);

        // Log history entry
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(order.getCurrentStatus())
                .timestamp(LocalDateTime.now())
                .actorId(actor.getId())
                .actorRole(actorRole)
                .build();
        orderStatusHistoryRepository.save(history);

        // Re-run auto assignment for rescheduled attempt
        try {
            agentAssignmentService.autoAssignAgent(orderId, actorUsername, actorRole);
        } catch (IllegalStateException e) {
            System.out.println("Reschedule warning: No agents available for auto-assignment yet. Order queued.");
        }

        return order;
    }
}
