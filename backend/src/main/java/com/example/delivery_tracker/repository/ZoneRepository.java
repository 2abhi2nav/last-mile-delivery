package com.example.delivery_tracker.repository;

import com.example.delivery_tracker.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Optional<Zone> findByName(String name);
    boolean existsByName(String name);
}
