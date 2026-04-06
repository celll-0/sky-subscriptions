package com.sky.subscription.repository;

import com.sky.subscription.entity.SubscriptionApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionAppRepository extends JpaRepository<SubscriptionApp, Integer> {
    
    List<SubscriptionApp> findBySubscriptionId(Integer subscriptionId);
    
    List<SubscriptionApp> findByAppTierId(Integer appTierId);
    
    void deleteBySubscriptionIdAndAppTierId(Integer subscriptionId, Integer appTierId);
}
