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
@Table(name = "add_on")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddOn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "addon_id")
    private Integer id;

    @Column(name = "addon_name", nullable = false, length = 100)
    private String addonName;

    @Column(name = "price", nullable = false, precision = 6, scale = 2)
    private BigDecimal price;

    @OneToMany(mappedBy = "addOn", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SubscriptionAddOn> subscriptionAddOns = new ArrayList<>();

    @OneToMany(mappedBy = "addOn", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PackageIncludedAddOn> packageIncludedAddOns = new ArrayList<>();
}
