package com.pm.resourceplanningledger.domain.ledger;

import com.pm.resourceplanningledger.domain.knowledge.ResourceType;
import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountKind kind;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_type_id")
    private ResourceType resourceType;

    public Account() {}

    public Account(String name, AccountKind kind) {
        this.name = name;
        this.kind = kind;
    }

    public Account(String name, AccountKind kind, ResourceType resourceType) {
        this.name = name;
        this.kind = kind;
        this.resourceType = resourceType;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public AccountKind getKind() { return kind; }
    public void setKind(AccountKind kind) { this.kind = kind; }

    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }

    public enum AccountKind {
        POOL, USAGE, ALERT_MEMO
    }
}