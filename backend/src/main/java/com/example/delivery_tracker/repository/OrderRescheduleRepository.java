package com.example.delivery_tracker.repository;

import com.example.delivery_tracker.model.OrderReschedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRescheduleRepository extends JpaRepository<OrderReschedule, Long> {
    List<OrderReschedule> findByOrderId(Long orderId);
}
