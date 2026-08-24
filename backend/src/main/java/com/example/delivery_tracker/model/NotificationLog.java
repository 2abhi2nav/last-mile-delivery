package com.example.delivery_tracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String channel; // EMAIL, SMS

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 500)
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime sentAt;
}
