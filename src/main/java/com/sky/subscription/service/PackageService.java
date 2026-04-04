package com.sky.subscription.service;

import com.sky.subscription.dto.PackageDto;
import com.sky.subscription.dto.PackageTierDto;
import com.sky.subscription.entity.Package;
import com.sky.subscription.entity.PackageTier;
import com.sky.subscription.exception.DuplicateResourceException;
import com.sky.subscription.exception.ResourceNotFoundException;
import com.sky.subscription.repository.PackageRepository;
import com.sky.subscription.repository.PackageTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PackageService {

    private final PackageRepository packageRepository;
    private final PackageTierRepository packageTierRepository;

    // Package operations
    public List<PackageDto> getAllPackages() {
        return packageRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PackageDto getPackageById(Integer id) {
        Package pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package", "id", id));
        return toDto(pkg);
    }

    public PackageDto createPackage(PackageDto dto) {
        if (packageRepository.findByPackageName(dto.getPackageName()).isPresent()) {
            throw new DuplicateResourceException("Package", "name", dto.getPackageName());
        }
        
        Package pkg = Package.builder()
                .packageName(dto.getPackageName())
                .build();
        
        Package saved = packageRepository.save(pkg);
        return toDto(saved);
    }

    public PackageDto updatePackage(Integer id, PackageDto dto) {
        Package existing = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package", "id", id));
        
        if (!existing.getPackageName().equals(dto.getPackageName())) {
            packageRepository.findByPackageName(dto.getPackageName())
                    .ifPresent(p -> {
                        throw new DuplicateResourceException("Package", "name", dto.getPackageName());
                    });
        }
        
        existing.setPackageName(dto.getPackageName());
        Package saved = packageRepository.save(existing);
        return toDto(saved);
    }

    public void deletePackage(Integer id) {
        if (!packageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Package", "id", id);
        }
        packageRepository.deleteById(id);
    }

    // Package Tier operations
    public List<PackageTierDto> getTiersByPackageId(Integer packageId) {
        return packageTierRepository.findByPkgPackageId(packageId).stream()
                .map(this::toTierDto)
                .collect(Collectors.toList());
    }

    public PackageTierDto getTierById(Integer id) {
        PackageTier tier = packageTierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PackageTier", "id", id));
        return toTierDto(tier);
    }

    public PackageTierDto createTier(PackageTierDto dto) {
        Package pkg = packageRepository.findById(dto.getPackageId())
                .orElseThrow(() -> new ResourceNotFoundException("Package", "id", dto.getPackageId()));
        
        PackageTier tier = PackageTier.builder()
                .pkg(pkg)
                .tierName(dto.getTierName())
                .price(dto.getPrice())
                .paymentCycle(dto.getPaymentCycle())
                .build();
        
        PackageTier saved = packageTierRepository.save(tier);
        return toTierDto(saved);
    }

    public PackageTierDto updateTier(Integer id, PackageTierDto dto) {
        PackageTier existing = packageTierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PackageTier", "id", id));
        
        if (dto.getPackageId() != null && !dto.getPackageId().equals(existing.getPkg().getPackageId())) {
            Package pkg = packageRepository.findById(dto.getPackageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Package", "id", dto.getPackageId()));
            existing.setPkg(pkg);
        }
        
        existing.setTierName(dto.getTierName());
        existing.setPrice(dto.getPrice());
        existing.setPaymentCycle(dto.getPaymentCycle());
        
        PackageTier saved = packageTierRepository.save(existing);
        return toTierDto(saved);
    }

    public void deleteTier(Integer id) {
        if (!packageTierRepository.existsById(id)) {
            throw new ResourceNotFoundException("PackageTier", "id", id);
        }
        packageTierRepository.deleteById(id);
    }

    private PackageDto toDto(Package entity) {
        List<PackageTierDto> tiers = entity.getTiers() != null 
                ? entity.getTiers().stream().map(this::toTierDto).collect(Collectors.toList())
                : null;
        
        return PackageDto.builder()
                .packageId(entity.getPackageId())
                .packageName(entity.getPackageName())
                .tiers(tiers)
                .build();
    }

    private PackageTierDto toTierDto(PackageTier entity) {
        return PackageTierDto.builder()
                .packageTierId(entity.getPackageTierId())
                .packageId(entity.getPkg().getPackageId())
                .packageName(entity.getPkg().getPackageName())
                .tierName(entity.getTierName())
                .price(entity.getPrice())
                .paymentCycle(entity.getPaymentCycle())
                .build();
    }
}
