package com.example.delivery_tracker.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "zone_areas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneArea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String pincode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;
}
