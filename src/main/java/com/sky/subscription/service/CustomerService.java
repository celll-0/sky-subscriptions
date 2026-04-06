package com.sky.subscription.service;

import com.sky.subscription.dto.CustomerDto;
import com.sky.subscription.entity.Customer;
import com.sky.subscription.exception.DuplicateResourceException;
import com.sky.subscription.exception.ResourceNotFoundException;
import com.sky.subscription.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<CustomerDto> getAllCustomers() {
        return customerRepository.findByIsDeletedFalse().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CustomerDto getCustomerById(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return toDto(customer);
    }

    public CustomerDto getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmailAddress(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        return toDto(customer);
    }

    public CustomerDto createCustomer(CustomerDto dto) {
        if (customerRepository.findByEmailAddress(dto.getEmailAddress()).isPresent()) {
            throw new DuplicateResourceException("Customer", "email", dto.getEmailAddress());
        }
        
        Customer customer = toEntity(dto);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setIsDeleted(false);
        
        Customer saved = customerRepository.save(customer);
        return toDto(saved);
    }

    public CustomerDto updateCustomer(Integer id, CustomerDto dto) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        
        // Check for email uniqueness if changed
        if (!existing.getEmailAddress().equals(dto.getEmailAddress())) {
            customerRepository.findByEmailAddress(dto.getEmailAddress())
                    .ifPresent(c -> {
                        throw new DuplicateResourceException("Customer", "email", dto.getEmailAddress());
                    });
        }
        
        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setEmailAddress(dto.getEmailAddress());
        existing.setCity(dto.getCity());
        existing.setExternalAuthId(dto.getExternalAuthId());
        existing.setMarketingConsent(dto.getMarketingConsent() != null ? dto.getMarketingConsent() : false);
        
        Customer saved = customerRepository.save(existing);
        return toDto(saved);
    }

    public void deleteCustomer(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        
        // Soft delete for GDPR compliance
        customer.setIsDeleted(true);
        customer.setDeletedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }

    private CustomerDto toDto(Customer entity) {
        return CustomerDto.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .emailAddress(entity.getEmailAddress())
                .city(entity.getCity())
                .externalAuthId(entity.getExternalAuthId())
                .marketingConsent(entity.getMarketingConsent())
                .isDeleted(entity.getIsDeleted())
                .deletedAt(entity.getDeletedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private Customer toEntity(CustomerDto dto) {
        return Customer.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .emailAddress(dto.getEmailAddress())
                .city(dto.getCity())
                .externalAuthId(dto.getExternalAuthId())
                .marketingConsent(dto.getMarketingConsent() != null ? dto.getMarketingConsent() : false)
                .build();
    }
}
