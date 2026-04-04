package com.sky.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageDto {
    
    private Integer packageId;
    
    @NotBlank(message = "Package name is required")
    @Size(max = 100, message = "Package name must be at most 100 characters")
    private String packageName;
    
    private List<PackageTierDto> tiers;
}
