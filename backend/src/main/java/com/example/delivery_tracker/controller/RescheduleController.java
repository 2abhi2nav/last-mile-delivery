package com.example.delivery_tracker.controller;

import com.example.delivery_tracker.dto.RescheduleDto;
import com.example.delivery_tracker.model.Order;
import com.example.delivery_tracker.service.RescheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class RescheduleController {

    @Autowired
    private RescheduleService rescheduleService;

    @PostMapping("/{orderId}/fail")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<Order> failDelivery(@PathVariable Long orderId, @RequestParam String reason, Authentication authentication) {
        String role = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ? "ADMIN" : "AGENT";
        Order order = rescheduleService.handleFailedDelivery(orderId, reason, authentication.getName(), role);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/reschedule")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<Order> rescheduleOrder(@PathVariable Long orderId, @RequestBody RescheduleDto dto, Authentication authentication) {
        String role = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ? "ADMIN" : "CUSTOMER";
        Order order = rescheduleService.rescheduleOrder(orderId, dto, authentication.getName(), role);
        return ResponseEntity.ok(order);
    }
}
