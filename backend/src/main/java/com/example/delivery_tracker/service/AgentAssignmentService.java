package com.example.delivery_tracker.service;

import com.example.delivery_tracker.model.*;
import com.example.delivery_tracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgentAssignmentService {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AgentLocationRepository agentLocationRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ZoneAreaRepository zoneAreaRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Agent updateAgentLocation(Long agentId, Double latitude, Double longitude) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));

        AgentLocation location = agentLocationRepository.findByAgentId(agentId)
                .orElse(AgentLocation.builder().agent(agent).build());

        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setUpdatedAt(LocalDateTime.now());
        agentLocationRepository.save(location);

        return agent;
    }

    @Transactional
    public Agent setAgentAvailability(Long agentId, boolean available) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));
        agent.setAvailable(available);
        return agentRepository.save(agent);
    }

    @Transactional
    public Order manualAssignAgent(Long orderId, Long agentId, String username, String role) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));

        order.setAssignedAgent(agent);
        orderRepository.save(order);

        User actor = userRepository.findByUsername(username).orElse(null);
        Long actorId = actor != null ? actor.getId() : 0L;

        // Record status history
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(order.getCurrentStatus())
                .timestamp(LocalDateTime.now())
                .actorId(actorId)
                .actorRole(role)
                .build();
        orderStatusHistoryRepository.save(history);

        return order;
    }

    @Transactional
    public Order autoAssignAgent(Long orderId, String username, String role) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Find available agents
        List<Agent> availableAgents = agentRepository.findByAvailable(true);
        if (availableAgents.isEmpty()) {
            throw new IllegalStateException("No available delivery agents at the moment. Order " + order.getTrackingNumber() + " has been queued.");
        }

        // Simple assignment policy: pick the first available agent (can be extended to proximity calculation via AgentLocation)
        Agent selectedAgent = availableAgents.get(0);
        order.setAssignedAgent(selectedAgent);
        orderRepository.save(order);

        User actor = userRepository.findByUsername(username).orElse(null);
        Long actorId = actor != null ? actor.getId() : 0L;

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(order.getCurrentStatus())
                .timestamp(LocalDateTime.now())
                .actorId(actorId)
                .actorRole(role)
                .build();
        orderStatusHistoryRepository.save(history);

        return order;
    }
}
