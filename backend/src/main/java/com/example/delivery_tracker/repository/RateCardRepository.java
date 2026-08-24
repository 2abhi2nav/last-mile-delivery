package com.example.delivery_tracker.repository;

import com.example.delivery_tracker.model.RateCard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RateCardRepository extends JpaRepository<RateCard, Long> {
    Optional<RateCard> findByOriginZoneAndDestinationZoneAndOrderType(
            String originZone, String destinationZone, String orderType);
}
