package com.example.delivery_tracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String trackingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private User merchant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent_id")
    private Agent assignedAgent;

    @Column(nullable = false)
    private String originPincode;

    @Column(nullable = false)
    private String destinationPincode;

    @Column(name = "origin_zone")
    private String originZone;

    @Column(name = "destination_zone")
    private String destinationZone;

    @Column(nullable = false)
    private String orderType;

    @Column(nullable = false)
    private boolean isCod;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weightKg;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal volumetricWeightKg;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal billableWeightKg;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus currentStatus;

    public enum OrderStatus {
        CREATED, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, RETURNED
    }
}
