package com.example.delivery_tracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cod_surcharges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodSurcharge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal surchargeAmount;
}
