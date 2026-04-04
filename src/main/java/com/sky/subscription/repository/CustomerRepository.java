package com.sky.subscription.repository;

import com.sky.subscription.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    
    Optional<Customer> findByEmailAddress(String emailAddress);
    
    Optional<Customer> findByExternalAuthId(String externalAuthId);
    
    List<Customer> findByIsDeletedFalse();
    
    List<Customer> findByCity(String city);
    
    List<Customer> findByMarketingConsentTrue();
}
