package com.pm.resourceplanningledger.domain.operational;

import com.pm.resourceplanningledger.domain.knowledge.Protocol;
import com.pm.resourceplanningledger.domain.knowledge.ResourceType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proposed_actions")
public class ProposedAction implements PlanNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocol_id")
    private Protocol protocol;

    private String party;
    private LocalDateTime timeRef;
    private String location;

    @Column(nullable = false)
    private String stateName = "PROPOSED";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @OneToMany(mappedBy = "proposedAction", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ResourceAllocation> allocations = new ArrayList<>();

    @OneToOne(mappedBy = "proposedAction", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private ImplementedAction implementedAction;

    @OneToMany(mappedBy = "proposedAction", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Suspension> suspensions = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "action_dependencies", joinColumns = @JoinColumn(name = "action_id"))
    @Column(name = "depends_on_action_name")
    private List<String> dependsOn = new ArrayList<>();

    public ProposedAction() {}

    public ProposedAction(String name) {
        this.name = name;
    }

    @Override
    public Long getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public boolean isLeaf() { return true; }

    @Override
    public String getStatus() {
        return stateName;
    }

    @Override
    public BigDecimal getTotalAllocatedQuantity(ResourceType resourceType) {
        return allocations.stream()
                .filter(a -> a.getResourceType().getId().equals(resourceType.getId()))
                .map(ResourceAllocation::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public void accept(PlanNodeVisitor visitor) {
        visitor.visitLeaf(this);
    }

    public void addAllocation(ResourceAllocation allocation) {
        allocations.add(allocation);
        allocation.setProposedAction(this);
    }

    // --- Getters and Setters ---
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }

    public Protocol getProtocol() { return protocol; }
    public void setProtocol(Protocol protocol) { this.protocol = protocol; }

    public String getParty() { return party; }
    public void setParty(String party) { this.party = party; }

    public LocalDateTime getTimeRef() { return timeRef; }
    public void setTimeRef(LocalDateTime timeRef) { this.timeRef = timeRef; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStateName() { return stateName; }
    public void setStateName(String stateName) { this.stateName = stateName; }

    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }

    public List<ResourceAllocation> getAllocations() { return allocations; }
    public void setAllocations(List<ResourceAllocation> allocations) { this.allocations = allocations; }

    public ImplementedAction getImplementedAction() { return implementedAction; }
    public void setImplementedAction(ImplementedAction implementedAction) { this.implementedAction = implementedAction; }

    public List<Suspension> getSuspensions() { return suspensions; }
    public void setSuspensions(List<Suspension> suspensions) { this.suspensions = suspensions; }

    public List<String> getDependsOn() { return dependsOn; }
    public void setDependsOn(List<String> dependsOn) { this.dependsOn = dependsOn; }
}