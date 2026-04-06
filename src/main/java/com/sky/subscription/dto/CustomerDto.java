package com.sky.subscription.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {
    
    private Integer id;
    
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be at most 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    private String lastName;
    
    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String emailAddress;
    
    @Size(max = 100, message = "City must be at most 100 characters")
    private String city;
    
    @Size(max = 255, message = "External auth ID must be at most 255 characters")
    private String externalAuthId;
    
    private Boolean marketingConsent;
    
    private Boolean isDeleted;
    
    private LocalDateTime deletedAt;
    
    private LocalDateTime createdAt;
}
