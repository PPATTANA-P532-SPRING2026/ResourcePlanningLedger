package com.pm.resourceplanningledger.domain.operational;

import com.pm.resourceplanningledger.domain.knowledge.ResourceType;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "resource_allocations")
public class ResourceAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proposed_action_id")
    private ProposedAction proposedAction;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_type_id", nullable = false)
    private ResourceType resourceType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllocationKind kind;

    private String assetId;
    private String timePeriod;

    public ResourceAllocation() {}

    public ResourceAllocation(ResourceType resourceType, BigDecimal quantity, AllocationKind kind) {
        this.resourceType = resourceType;
        this.quantity = quantity;
        this.kind = kind;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProposedAction getProposedAction() { return proposedAction; }
    public void setProposedAction(ProposedAction proposedAction) { this.proposedAction = proposedAction; }

    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public AllocationKind getKind() { return kind; }
    public void setKind(AllocationKind kind) { this.kind = kind; }

    public String getAssetId() { return assetId; }
    public void setAssetId(String assetId) { this.assetId = assetId; }

    public String getTimePeriod() { return timePeriod; }
    public void setTimePeriod(String timePeriod) { this.timePeriod = timePeriod; }

    public enum AllocationKind {
        GENERAL, SPECIFIC
    }
}