package com.sky.subscription.service;

import com.sky.subscription.dto.AddOnDto;
import com.sky.subscription.entity.AddOn;
import com.sky.subscription.exception.DuplicateResourceException;
import com.sky.subscription.exception.ResourceNotFoundException;
import com.sky.subscription.repository.AddOnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AddOnService {

    private final AddOnRepository addOnRepository;

    public List<AddOnDto> getAllAddOns() {
        return addOnRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AddOnDto> getIncludedAddOns() {
        return addOnRepository.findByIncludedTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AddOnDto> getPurchasableAddOns() {
        return addOnRepository.findByIncludedFalse().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public AddOnDto getAddOnById(Integer id) {
        AddOn addOn = addOnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AddOn", "id", id));
        return toDto(addOn);
    }

    public AddOnDto createAddOn(AddOnDto dto) {
        if (addOnRepository.findByAddonName(dto.getAddonName()).isPresent()) {
            throw new DuplicateResourceException("AddOn", "name", dto.getAddonName());
        }
        
        AddOn addOn = toEntity(dto);
        AddOn saved = addOnRepository.save(addOn);
        return toDto(saved);
    }

    public AddOnDto updateAddOn(Integer id, AddOnDto dto) {
        AddOn existing = addOnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AddOn", "id", id));
        
        if (!existing.getAddonName().equals(dto.getAddonName())) {
            addOnRepository.findByAddonName(dto.getAddonName())
                    .ifPresent(a -> {
                        throw new DuplicateResourceException("AddOn", "name", dto.getAddonName());
                    });
        }
        
        existing.setAddonName(dto.getAddonName());
        existing.setCostMonthly(dto.getCostMonthly());
        existing.setIncluded(dto.getIncluded() != null ? dto.getIncluded() : false);
        
        AddOn saved = addOnRepository.save(existing);
        return toDto(saved);
    }

    public void deleteAddOn(Integer id) {
        if (!addOnRepository.existsById(id)) {
            throw new ResourceNotFoundException("AddOn", "id", id);
        }
        addOnRepository.deleteById(id);
    }

    private AddOnDto toDto(AddOn entity) {
        return AddOnDto.builder()
                .addonId(entity.getAddonId())
                .addonName(entity.getAddonName())
                .costMonthly(entity.getCostMonthly())
                .included(entity.getIncluded())
                .build();
    }

    private AddOn toEntity(AddOnDto dto) {
        return AddOn.builder()
                .addonId(dto.getAddonId())
                .addonName(dto.getAddonName())
                .costMonthly(dto.getCostMonthly())
                .included(dto.getIncluded() != null ? dto.getIncluded() : false)
                .build();
    }
}
