package com.sky.subscription.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_tier")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_tier_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", nullable = false)
    private App app;

    @Column(name = "tier_name", nullable = false, length = 100)
    private String tierName;

    @Column(name = "price", nullable = false, precision = 6, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_cycle", nullable = false)
    private PaymentCycle paymentCycle;

    @OneToMany(mappedBy = "appTier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PackageIncludedApp> packageIncludedApps = new ArrayList<>();

    @OneToMany(mappedBy = "appTier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SubscriptionApp> subscriptionApps = new ArrayList<>();
}
