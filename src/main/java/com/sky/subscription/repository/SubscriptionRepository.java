package com.sky.subscription.repository;

import com.sky.subscription.entity.Subscription;
import com.sky.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    
    List<Subscription> findByCustomerCustomerId(Integer customerId);
    
    List<Subscription> findByStatus(SubscriptionStatus status);
    
    List<Subscription> findByCustomerCustomerIdAndStatus(Integer customerId, SubscriptionStatus status);
    
    List<Subscription> findByPackageTierPackageTierId(Integer packageTierId);
}
