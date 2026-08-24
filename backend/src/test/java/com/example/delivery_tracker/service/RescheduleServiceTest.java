package com.example.delivery_tracker.service;

import com.example.delivery_tracker.dto.RescheduleDto;
import com.example.delivery_tracker.model.Order;
import com.example.delivery_tracker.model.OrderReschedule;
import com.example.delivery_tracker.model.User;
import com.example.delivery_tracker.repository.OrderRescheduleRepository;
import com.example.delivery_tracker.repository.OrderRepository;
import com.example.delivery_tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.flyway.enabled=false"
})
class RescheduleServiceTest {

    @Autowired
    private RescheduleService rescheduleService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderRescheduleRepository orderRescheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    void testRescheduleFlow() {
        // Setup mock merchant
        User merchant = userRepository.findAll().stream().findFirst().orElseGet(() ->
            userRepository.save(User.builder().username("testmerchant").password("pass").email("m@test.com").role(User.Role.MERCHANT).build())
        );

        Order order = orderRepository.save(Order.builder()
                .trackingNumber("TRK-TEST-999")
                .merchant(merchant)
                .originPincode("110001")
                .destinationPincode("560001")
                .orderType("STANDARD")
                .isCod(false)
                .weightKg(java.math.BigDecimal.TEN)
                .volumetricWeightKg(java.math.BigDecimal.TEN)
                .billableWeightKg(java.math.BigDecimal.TEN)
                .shippingCost(java.math.BigDecimal.valueOf(100))
                .currentStatus(Order.OrderStatus.OUT_FOR_DELIVERY)
                .build());

        // 1. Handle failed delivery
        rescheduleService.handleFailedDelivery(order.getId(), "Recipient not available", merchant.getUsername(), "AGENT");

        // 2. Reschedule order
        RescheduleDto dto = new RescheduleDto();
        dto.setNewDeliveryDate(LocalDateTime.now().plusDays(1));
        dto.setReason("Customer requested next day delivery");

        rescheduleService.rescheduleOrder(order.getId(), dto, merchant.getUsername(), "CUSTOMER");

        // Verify reschedule record created
        List<OrderReschedule> reschedules = orderRescheduleRepository.findByOrderId(order.getId());
        assertEquals(1, reschedules.size());
        assertEquals("Customer requested next day delivery", reschedules.get(0).getReason());
    }
}
