package com.pm.resourceplanningledger.domain.knowledge;

import com.pm.resourceplanningledger.domain.ledger.Account;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "resource_types")
public class ResourceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceKind kind;

    @Column(nullable = false)
    private String unit;

    @Column(precision = 19, scale = 4)
    private BigDecimal unitCost;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "pool_account_id")
    private Account poolAccount;

    public ResourceType() {}

    public ResourceType(String name, ResourceKind kind, String unit) {
        this.name = name;
        this.kind = kind;
        this.unit = unit;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ResourceKind getKind() { return kind; }
    public void setKind(ResourceKind kind) { this.kind = kind; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public Account getPoolAccount() { return poolAccount; }
    public void setPoolAccount(Account poolAccount) { this.poolAccount = poolAccount; }

    public enum ResourceKind {
        ASSET, CONSUMABLE
    }
}