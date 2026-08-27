package com.hiru.globaltrade.ejb.entity;

import com.hiru.globaltrade.common.enums.VendorTier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "vendors")
public class VendorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String vendorCode;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 80)
    private String country;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score = BigDecimal.valueOf(90);

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private VendorTier tier = VendorTier.APPROVED;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant lastEvaluated;

    @Version
    private long version;

    @PrePersist
    @PreUpdate
    void timestamp() {
        if (lastEvaluated == null) {
            lastEvaluated = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public VendorTier getTier() {
        return tier;
    }

    public void setTier(VendorTier tier) {
        this.tier = tier;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getLastEvaluated() {
        return lastEvaluated;
    }

    public void setLastEvaluated(Instant lastEvaluated) {
        this.lastEvaluated = lastEvaluated;
    }
}
