package com.example.delivery_tracker.controller;

import com.example.delivery_tracker.dto.StatusUpdateDto;
import com.example.delivery_tracker.model.Order;
import com.example.delivery_tracker.model.OrderStatusHistory;
import com.example.delivery_tracker.service.OrderStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderTrackingController {

    @Autowired
    private OrderStatusService orderStatusService;

    @GetMapping("/{orderId}/timeline")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MERCHANT') or hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<List<OrderStatusHistory>> getOrderTimeline(@PathVariable Long orderId, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        List<OrderStatusHistory> timeline = orderStatusService.getOrderTimeline(orderId, authentication.getName(), isAdmin);
        return ResponseEntity.ok(timeline);
    }

    @PatchMapping("/{orderId}/status/agent")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<Order> agentUpdateStatus(@PathVariable Long orderId, @RequestBody StatusUpdateDto dto, Authentication authentication) {
        Order order = orderStatusService.updateOrderStatusByAgent(orderId, dto.getStatus(), authentication.getName());
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{orderId}/status/override")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> adminOverrideStatus(@PathVariable Long orderId, @RequestBody StatusUpdateDto dto, Authentication authentication) {
        Order order = orderStatusService.adminOverrideOrderStatus(orderId, dto.getStatus(), authentication.getName());
        return ResponseEntity.ok(order);
    }
}
