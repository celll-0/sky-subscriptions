package com.sky.subscription.repository;

import com.sky.subscription.entity.SubscriptionAddOn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionAddOnRepository extends JpaRepository<SubscriptionAddOn, Integer> {
    
    List<SubscriptionAddOn> findBySubscriptionId(Integer subscriptionId);
    
    List<SubscriptionAddOn> findByAddOnId(Integer addonId);
    
    void deleteBySubscriptionIdAndAddOnId(Integer subscriptionId, Integer addonId);
}
