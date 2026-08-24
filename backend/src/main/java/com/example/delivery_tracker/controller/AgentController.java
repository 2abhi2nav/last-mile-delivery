package com.example.delivery_tracker.controller;

import com.example.delivery_tracker.dto.AgentLocationDto;
import com.example.delivery_tracker.model.Agent;
import com.example.delivery_tracker.model.Order;
import com.example.delivery_tracker.service.AgentAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    @Autowired
    private AgentAssignmentService agentAssignmentService;

    @PostMapping("/{agentId}/location")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<Agent> updateLocation(@PathVariable Long agentId, @RequestBody AgentLocationDto dto) {
        Agent agent = agentAssignmentService.updateAgentLocation(agentId, dto.getLatitude(), dto.getLongitude());
        return ResponseEntity.ok(agent);
    }

    @PatchMapping("/{agentId}/availability")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<Agent> setAvailability(@PathVariable Long agentId, @RequestParam boolean available) {
        Agent agent = agentAssignmentService.setAgentAvailability(agentId, available);
        return ResponseEntity.ok(agent);
    }

    @PostMapping("/assign/manual")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> manualAssign(@RequestParam Long orderId, @RequestParam Long agentId, Authentication authentication) {
        Order order = agentAssignmentService.manualAssignAgent(orderId, agentId, authentication.getName(), "ADMIN");
        return ResponseEntity.ok(order);
    }

    @PostMapping("/assign/auto/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<Order> autoAssign(@PathVariable Long orderId, Authentication authentication) {
        String role = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ? "ADMIN" : "MERCHANT";
        Order order = agentAssignmentService.autoAssignAgent(orderId, authentication.getName(), role);
        return ResponseEntity.ok(order);
    }
}
