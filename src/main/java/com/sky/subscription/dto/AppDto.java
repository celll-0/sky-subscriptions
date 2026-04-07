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
public class AppDto {
    
    private Integer id;
    
    @NotBlank(message = "App name is required")
    @Size(max = 100, message = "App name must be at most 100 characters")
    private String appName;
    
    private List<AppTierDto> tiers;
}
