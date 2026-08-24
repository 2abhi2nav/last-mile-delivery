package com.example.delivery_tracker.service;

import com.example.delivery_tracker.model.NotificationLog;
import com.example.delivery_tracker.model.Order;
import com.example.delivery_tracker.repository.NotificationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendStatusChangeNotification(Order order, Order.OrderStatus newStatus) {
        String recipientEmail = order.getMerchant().getEmail();
        String message = "Your order " + order.getTrackingNumber() + " status has updated to: " + newStatus;

        try {
            System.out.println("[EMAIL] Sending to " + recipientEmail + ": " + message);

            notificationLogRepository.save(NotificationLog.builder()
                    .orderId(order.getId())
                    .channel("EMAIL")
                    .recipient(recipientEmail)
                    .message(message)
                    .success(true)
                    .sentAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            notificationLogRepository.save(NotificationLog.builder()
                    .orderId(order.getId())
                    .channel("EMAIL")
                    .recipient(recipientEmail)
                    .message(message)
                    .success(false)
                    .errorMessage(e.getMessage())
                    .sentAt(LocalDateTime.now())
                    .build());
        }

        try {
            String recipientPhone = "+1234567890";
            System.out.println("[SMS] Sending to " + recipientPhone + ": " + message);

            notificationLogRepository.save(NotificationLog.builder()
                    .orderId(order.getId())
                    .channel("SMS")
                    .recipient(recipientPhone)
                    .message(message)
                    .success(true)
                    .sentAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            notificationLogRepository.save(NotificationLog.builder()
                    .orderId(order.getId())
                    .channel("SMS")
                    .recipient("+1234567890")
                    .message(message)
                    .success(false)
                    .errorMessage(e.getMessage())
                    .sentAt(LocalDateTime.now())
                    .build());
        }
    }
}
