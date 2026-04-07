package com.sky.subscription.service;

import com.sky.subscription.dto.SubscriptionDto;
import com.sky.subscription.entity.*;
import com.sky.subscription.exception.ResourceNotFoundException;
import com.sky.subscription.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final PackageTierRepository packageTierRepository;
    private final AddOnRepository addOnRepository;
    private final AppTierRepository appTierRepository;
    private final SubscriptionAddOnRepository subscriptionAddOnRepository;
    private final SubscriptionAppRepository subscriptionAppRepository;

    public List<SubscriptionDto> getAllSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public SubscriptionDto getSubscriptionById(Integer id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
        return toDto(subscription);
    }

    public List<SubscriptionDto> getSubscriptionsByCustomerId(Integer customerId) {
        return subscriptionRepository.findByCustomerId(customerId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<SubscriptionDto> getSubscriptionsByStatus(SubscriptionStatus status) {
        return subscriptionRepository.findByStatus(status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public SubscriptionDto createSubscription(SubscriptionDto dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", dto.getCustomerId()));
        
        PackageTier packageTier = packageTierRepository.findById(dto.getPackageTierId())
                .orElseThrow(() -> new ResourceNotFoundException("PackageTier", "id", dto.getPackageTierId()));
        
        Subscription subscription = Subscription.builder()
                .customer(customer)
                .packageTier(packageTier)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(dto.getStatus())
                .lastUpdated(LocalDateTime.now())
                .build();
        
        Subscription saved = subscriptionRepository.save(subscription);
        
        // Add add-ons if provided
        if (dto.getAddOnIds() != null && !dto.getAddOnIds().isEmpty()) {
            for (Integer addonId : dto.getAddOnIds()) {
                AddOn addOn = addOnRepository.findById(addonId)
                        .orElseThrow(() -> new ResourceNotFoundException("AddOn", "id", addonId));
                SubscriptionAddOn subAddon = SubscriptionAddOn.builder()
                        .subscription(saved)
                        .addOn(addOn)
                        .build();
                subscriptionAddOnRepository.save(subAddon);
            }
        }
        
        // Add app tiers if provided
        if (dto.getAppTierIds() != null && !dto.getAppTierIds().isEmpty()) {
            for (Integer appTierId : dto.getAppTierIds()) {
                AppTier appTier = appTierRepository.findById(appTierId)
                        .orElseThrow(() -> new ResourceNotFoundException("AppTier", "id", appTierId));
                SubscriptionApp subApp = SubscriptionApp.builder()
                        .subscription(saved)
                        .appTier(appTier)
                        .build();
                subscriptionAppRepository.save(subApp);
            }
        }
        
        return toDto(subscriptionRepository.findById(saved.getId()).orElse(saved));
    }

    public SubscriptionDto updateSubscription(Integer id, SubscriptionDto dto) {
        Subscription existing = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
        
        if (dto.getPackageTierId() != null && !dto.getPackageTierId().equals(existing.getPackageTier().getId())) {
            PackageTier packageTier = packageTierRepository.findById(dto.getPackageTierId())
                    .orElseThrow(() -> new ResourceNotFoundException("PackageTier", "id", dto.getPackageTierId()));
            existing.setPackageTier(packageTier);
        }
        
        if (dto.getStartDate() != null) {
            existing.setStartDate(dto.getStartDate());
        }
        existing.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }
        existing.setLastUpdated(LocalDateTime.now());
        
        Subscription saved = subscriptionRepository.save(existing);
        return toDto(saved);
    }

    public void deleteSubscription(Integer id) {
        if (!subscriptionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subscription", "id", id);
        }
        subscriptionRepository.deleteById(id);
    }

    // Add-on management for subscriptions
    public SubscriptionDto addAddOnToSubscription(Integer subscriptionId, Integer addonId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", subscriptionId));
        AddOn addOn = addOnRepository.findById(addonId)
                .orElseThrow(() -> new ResourceNotFoundException("AddOn", "id", addonId));
        
        SubscriptionAddOn subAddon = SubscriptionAddOn.builder()
                .subscription(subscription)
                .addOn(addOn)
                .build();
        subscriptionAddOnRepository.save(subAddon);
        
        return toDto(subscriptionRepository.findById(subscriptionId).orElse(subscription));
    }

    public void removeAddOnFromSubscription(Integer subscriptionId, Integer addonId) {
        subscriptionAddOnRepository.deleteBySubscriptionIdAndAddOnId(subscriptionId, addonId);
    }

    // App tier management for subscriptions
    public SubscriptionDto addAppTierToSubscription(Integer subscriptionId, Integer appTierId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", subscriptionId));
        AppTier appTier = appTierRepository.findById(appTierId)
                .orElseThrow(() -> new ResourceNotFoundException("AppTier", "id", appTierId));
        
        SubscriptionApp subApp = SubscriptionApp.builder()
                .subscription(subscription)
                .appTier(appTier)
                .build();
        subscriptionAppRepository.save(subApp);
        
        return toDto(subscriptionRepository.findById(subscriptionId).orElse(subscription));
    }

    public void removeAppTierFromSubscription(Integer subscriptionId, Integer appTierId) {
        subscriptionAppRepository.deleteBySubscriptionIdAndAppTierId(subscriptionId, appTierId);
    }

    private SubscriptionDto toDto(Subscription entity) {
        List<Integer> addOnIds = entity.getAddOns() != null
                ? entity.getAddOns().stream().map(sa -> sa.getAddOn().getId()).collect(Collectors.toList())
                : null;
        
        List<Integer> appTierIds = entity.getApps() != null
                ? entity.getApps().stream().map(sa -> sa.getAppTier().getId()).collect(Collectors.toList())
                : null;
        
        return SubscriptionDto.builder()
                .id(entity.getId())
                .customerId(entity.getCustomer().getId())
                .customerName(entity.getCustomer().getFirstName() + " " + entity.getCustomer().getLastName())
                .packageTierId(entity.getPackageTier().getId())
                .packageTierName(entity.getPackageTier().getTierName())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .lastUpdated(entity.getLastUpdated())
                .addOnIds(addOnIds)
                .appTierIds(appTierIds)
                .build();
    }
}
