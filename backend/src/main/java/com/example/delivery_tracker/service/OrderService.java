package com.example.delivery_tracker.service;

import com.example.delivery_tracker.dto.OrderPreviewResponseDto;
import com.example.delivery_tracker.dto.OrderRequestDto;
import com.example.delivery_tracker.model.*;
import com.example.delivery_tracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private ZoneAreaRepository zoneAreaRepository;

    @Autowired
    private RateCardRepository rateCardRepository;

    @Autowired
    private CodSurchargeRepository codSurchargeRepository;

    @Autowired
    private RateCalculationService rateCalculationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    public OrderPreviewResponseDto previewOrderCharges(OrderRequestDto request) {
        // 1. Resolve zones from pincodes
        ZoneArea originArea = zoneAreaRepository.findByPincode(request.getOriginPincode())
                .orElseThrow(() -> new IllegalArgumentException("Origin pincode not found in any zone: " + request.getOriginPincode()));
        ZoneArea destArea = zoneAreaRepository.findByPincode(request.getDestinationPincode())
                .orElseThrow(() -> new IllegalArgumentException("Destination pincode not found in any zone: " + request.getDestinationPincode()));

        String originZone = originArea.getZone().getName();
        String destinationZone = destArea.getZone().getName();

        // 2. Volumetric & Billable Weight
        BigDecimal volumetricWeight = rateCalculationService.calculateVolumetricWeight(
                request.getLengthCm(), request.getBreadthCm(), request.getHeightCm());
        BigDecimal billableWeight = rateCalculationService.calculateBillableWeight(
                request.getWeightKg(), volumetricWeight);

        // 3. Rate Card Lookup
        RateCard rateCard = rateCardRepository.findByOriginZoneAndDestinationZoneAndOrderType(
                originZone, destinationZone, request.getOrderType())
                .orElseThrow(() -> new IllegalArgumentException("No rate card found for zone pair " + originZone + " -> " + destinationZone + " with type " + request.getOrderType()));

        // 4. COD Surcharge Lookup
        CodSurcharge codSurcharge = null;
        if (request.isCod()) {
            codSurcharge = codSurchargeRepository.findByOrderType(request.getOrderType()).orElse(null);
        }

        // 5. Compute Shipping Cost
        BigDecimal shippingCost = rateCalculationService.calculateShippingCost(
                billableWeight, rateCard, request.isCod(), codSurcharge);

        return OrderPreviewResponseDto.builder()
                .originZone(originZone)
                .destinationZone(destinationZone)
                .orderType(request.getOrderType())
                .isCod(request.isCod())
                .actualWeightKg(request.getWeightKg())
                .volumetricWeightKg(volumetricWeight)
                .billableWeightKg(billableWeight)
                .baseRate(rateCard.getBaseRate())
                .perKgRate(rateCard.getPerKgRate())
                .codSurcharge(codSurcharge != null ? codSurcharge.getSurchargeAmount() : BigDecimal.ZERO)
                .shippingCost(shippingCost)
                .build();
    }

    @Transactional
    public Order confirmAndCreateOrder(OrderRequestDto request, String currentUsername, boolean isAdmin) {
        // Preview charges to validate and compute exact costs
        OrderPreviewResponseDto preview = previewOrderCharges(request);

        // Determine merchant/customer user
        User merchant;
        if (isAdmin && request.getCustomerId() != null) {
            merchant = userRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException("Customer/Merchant ID not found"));
        } else {
            merchant = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        }

        String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = Order.builder()
                .trackingNumber(trackingNumber)
                .merchant(merchant)
                .originPincode(request.getOriginPincode())
                .destinationPincode(request.getDestinationPincode())
                .orderType(request.getOrderType())
                .isCod(request.isCod())
                .weightKg(request.getWeightKg())
                .volumetricWeightKg(preview.getVolumetricWeightKg())
                .billableWeightKg(preview.getBillableWeightKg())
                .shippingCost(preview.getShippingCost())
                .currentStatus(Order.OrderStatus.CREATED)
                .build();

        order = orderRepository.save(order);

        // Append initial status history (immutable)
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(Order.OrderStatus.CREATED)
                .timestamp(LocalDateTime.now())
                .actorId(merchant.getId())
                .actorRole(merchant.getRole().name())
                .build();

        orderStatusHistoryRepository.save(history);

        return order;
    }
}
