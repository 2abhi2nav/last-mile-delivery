package com.example.delivery_tracker.service;

import com.example.delivery_tracker.model.CodSurcharge;
import com.example.delivery_tracker.model.RateCard;
import com.example.delivery_tracker.model.Zone;
import com.example.delivery_tracker.model.ZoneArea;
import com.example.delivery_tracker.repository.CodSurchargeRepository;
import com.example.delivery_tracker.repository.RateCardRepository;
import com.example.delivery_tracker.repository.ZoneAreaRepository;
import com.example.delivery_tracker.repository.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminManagementService {

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private ZoneAreaRepository zoneAreaRepository;

    @Autowired
    private RateCardRepository rateCardRepository;

    @Autowired
    private CodSurchargeRepository codSurchargeRepository;

    // Zone Management
    public Zone createZone(String name) {
        if (zoneRepository.existsByName(name)) {
            throw new IllegalArgumentException("Zone already exists: " + name);
        }
        return zoneRepository.save(Zone.builder().name(name).build());
    }

    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }

    // Zone Area Management
    public ZoneArea assignPincodeToZone(String pincode, String zoneName) {
        if (zoneAreaRepository.existsByPincode(pincode)) {
            throw new IllegalArgumentException("Pincode is already assigned to a zone: " + pincode);
        }
        Zone zone = zoneRepository.findByName(zoneName)
                .orElseThrow(() -> new IllegalArgumentException("Zone not found: " + zoneName));

        return zoneAreaRepository.save(ZoneArea.builder().pincode(pincode).zone(zone).build());
    }

    public List<ZoneArea> getAllZoneAreas() {
        return zoneAreaRepository.findAll();
    }

    // Rate Card Management
    public RateCard saveRateCard(RateCard rateCard) {
        return rateCardRepository.save(rateCard);
    }

    public List<RateCard> getAllRateCards() {
        return rateCardRepository.findAll();
    }

    // COD Surcharge Management
    public CodSurcharge saveCodSurcharge(CodSurcharge codSurcharge) {
        return codSurchargeRepository.save(codSurcharge);
    }

    public List<CodSurcharge> getAllCodSurcharges() {
        return codSurchargeRepository.findAll();
    }
}
