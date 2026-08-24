package com.example.delivery_tracker.repository;

import com.example.delivery_tracker.model.CodSurcharge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CodSurchargeRepository extends JpaRepository<CodSurcharge, Long> {
    Optional<CodSurcharge> findByOrderType(String orderType);
}
