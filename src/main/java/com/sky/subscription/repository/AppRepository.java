package com.sky.subscription.repository;

import com.sky.subscription.entity.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppRepository extends JpaRepository<App, Integer> {
    
    Optional<App> findByAppName(String appName);
    
    List<App> findByIncludedTrue();
    
    List<App> findByIncludedFalse();
}
