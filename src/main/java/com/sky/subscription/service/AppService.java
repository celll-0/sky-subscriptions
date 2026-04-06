package com.sky.subscription.service;

import com.sky.subscription.dto.AppDto;
import com.sky.subscription.dto.AppTierDto;
import com.sky.subscription.entity.App;
import com.sky.subscription.entity.AppTier;
import com.sky.subscription.exception.DuplicateResourceException;
import com.sky.subscription.exception.ResourceNotFoundException;
import com.sky.subscription.repository.AppRepository;
import com.sky.subscription.repository.AppTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AppService {

    private final AppRepository appRepository;
    private final AppTierRepository appTierRepository;

    // App operations
    public List<AppDto> getAllApps() {
        return appRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public AppDto getAppById(Integer id) {
        App app = appRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("App", "id", id));
        return toDto(app);
    }

    public AppDto createApp(AppDto dto) {
        if (appRepository.findByAppName(dto.getAppName()).isPresent()) {
            throw new DuplicateResourceException("App", "name", dto.getAppName());
        }
        
        App app = App.builder()
                .appName(dto.getAppName())
                .included(dto.getIncluded() != null ? dto.getIncluded() : false)
                .build();
        
        App saved = appRepository.save(app);
        return toDto(saved);
    }

    public AppDto updateApp(Integer id, AppDto dto) {
        App existing = appRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("App", "id", id));
        
        if (!existing.getAppName().equals(dto.getAppName())) {
            appRepository.findByAppName(dto.getAppName())
                    .ifPresent(a -> {
                        throw new DuplicateResourceException("App", "name", dto.getAppName());
                    });
        }
        
        existing.setAppName(dto.getAppName());
        existing.setIncluded(dto.getIncluded() != null ? dto.getIncluded() : false);
        
        App saved = appRepository.save(existing);
        return toDto(saved);
    }

    public void deleteApp(Integer id) {
        if (!appRepository.existsById(id)) {
            throw new ResourceNotFoundException("App", "id", id);
        }
        appRepository.deleteById(id);
    }

    // App Tier operations
    public List<AppTierDto> getTiersByAppId(Integer appId) {
        return appTierRepository.findByAppId(appId).stream()
                .map(this::toTierDto)
                .collect(Collectors.toList());
    }

    public AppTierDto getTierById(Integer id) {
        AppTier tier = appTierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppTier", "id", id));
        return toTierDto(tier);
    }

    public AppTierDto createTier(AppTierDto dto) {
        App app = appRepository.findById(dto.getAppId())
                .orElseThrow(() -> new ResourceNotFoundException("App", "id", dto.getAppId()));
        
        AppTier tier = AppTier.builder()
                .app(app)
                .tierName(dto.getTierName())
                .price(dto.getPrice())
                .paymentCycle(dto.getPaymentCycle())
                .build();
        
        AppTier saved = appTierRepository.save(tier);
        return toTierDto(saved);
    }

    public AppTierDto updateTier(Integer id, AppTierDto dto) {
        AppTier existing = appTierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppTier", "id", id));
        
        if (dto.getAppId() != null && !dto.getAppId().equals(existing.getApp().getId())) {
            App app = appRepository.findById(dto.getAppId())
                    .orElseThrow(() -> new ResourceNotFoundException("App", "id", dto.getAppId()));
            existing.setApp(app);
        }
        
        existing.setTierName(dto.getTierName());
        existing.setPrice(dto.getPrice());
        existing.setPaymentCycle(dto.getPaymentCycle());
        
        AppTier saved = appTierRepository.save(existing);
        return toTierDto(saved);
    }

    public void deleteTier(Integer id) {
        if (!appTierRepository.existsById(id)) {
            throw new ResourceNotFoundException("AppTier", "id", id);
        }
        appTierRepository.deleteById(id);
    }

    private AppDto toDto(App entity) {
        List<AppTierDto> tiers = entity.getTiers() != null
                ? entity.getTiers().stream().map(this::toTierDto).collect(Collectors.toList())
                : null;
        
        return AppDto.builder()
                .id(entity.getId())
                .appName(entity.getAppName())
                .included(entity.getIncluded())
                .tiers(tiers)
                .build();
    }

    private AppTierDto toTierDto(AppTier entity) {
        return AppTierDto.builder()
                .id(entity.getId())
                .appId(entity.getApp().getId())
                .appName(entity.getApp().getAppName())
                .tierName(entity.getTierName())
                .price(entity.getPrice())
                .paymentCycle(entity.getPaymentCycle())
                .build();
    }
}
