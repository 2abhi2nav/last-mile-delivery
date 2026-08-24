package com.example.delivery_tracker.controller;

import com.example.delivery_tracker.dto.OrderPreviewResponseDto;
import com.example.delivery_tracker.dto.OrderRequestDto;
import com.example.delivery_tracker.model.Order;
import com.example.delivery_tracker.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/preview")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<OrderPreviewResponseDto> previewOrder(@RequestBody OrderRequestDto request) {
        OrderPreviewResponseDto preview = orderService.previewOrderCharges(request);
        return ResponseEntity.ok(preview);
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<Order> confirmOrder(@RequestBody OrderRequestDto request, Authentication authentication) {
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Order order = orderService.confirmAndCreateOrder(request, currentUsername, isAdmin);
        return ResponseEntity.ok(order);
    }
}
