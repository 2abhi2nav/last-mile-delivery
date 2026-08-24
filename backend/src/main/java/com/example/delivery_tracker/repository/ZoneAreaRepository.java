package com.example.delivery_tracker.repository;

import com.example.delivery_tracker.model.ZoneArea;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ZoneAreaRepository extends JpaRepository<ZoneArea, Long> {
    Optional<ZoneArea> findByPincode(String pincode);
    boolean existsByPincode(String pincode);
}
