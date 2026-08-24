package com.example.delivery_tracker.repository;

import com.example.delivery_tracker.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByUserId(Long userId);
    List<Agent> findByAvailable(boolean available);
}
