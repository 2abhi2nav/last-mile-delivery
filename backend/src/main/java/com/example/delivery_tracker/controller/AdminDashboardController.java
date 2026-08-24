package com.example.delivery_tracker.controller;

import com.example.delivery_tracker.dto.OrderDetailResponseDto;
import com.example.delivery_tracker.model.Order;
import com.example.delivery_tracker.model.OrderStatusHistory;
import com.example.delivery_tracker.repository.OrderRepository;
import com.example.delivery_tracker.repository.OrderStatusHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders(
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) Long agentId) {

        List<Order> orders = orderRepository.findAll();

        if (status != null) {
            orders = orders.stream().filter(o -> o.getCurrentStatus() == status).collect(Collectors.toList());
        }

        if (zone != null) {
            orders = orders.stream().filter(o -> o.getOriginZone() != null && o.getOriginZone().equalsIgnoreCase(zone)).collect(Collectors.toList());
        }

        if (agentId != null) {
            orders = orders.stream().filter(o -> o.getAssignedAgent() != null && o.getAssignedAgent().getId().equals(agentId)).collect(Collectors.toList());
        }

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponseDto> getOrderDetail(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        List<OrderStatusHistory> timeline = orderStatusHistoryRepository.findByOrderId(orderId);

        return ResponseEntity.ok(OrderDetailResponseDto.builder()
                .order(order)
                .timeline(timeline)
                .build());
    }
}
