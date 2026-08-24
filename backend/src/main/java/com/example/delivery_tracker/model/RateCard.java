package com.example.delivery_tracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "rate_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originZone;

    @Column(nullable = false)
    private String destinationZone;

    @Column(nullable = false)
    private String orderType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseRate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal perKgRate;
}
