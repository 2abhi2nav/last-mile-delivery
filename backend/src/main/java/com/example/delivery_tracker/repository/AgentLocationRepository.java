package com.example.delivery_tracker.repository;

import com.example.delivery_tracker.model.AgentLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AgentLocationRepository extends JpaRepository<AgentLocation, Long> {
    Optional<AgentLocation> findByAgentId(Long agentId);
}
