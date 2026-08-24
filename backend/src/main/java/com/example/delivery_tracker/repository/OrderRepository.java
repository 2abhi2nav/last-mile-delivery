package com.example.delivery_tracker.repository;

import com.example.delivery_tracker.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByTrackingNumber(String trackingNumber);
    List<Order> findByMerchantId(Long merchantId);
}
