package com.pm.resourceplanningledger.domain.operational;

import com.pm.resourceplanningledger.domain.knowledge.Protocol;
import com.pm.resourceplanningledger.domain.knowledge.ResourceType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plans")
public class Plan implements PlanNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_protocol_id")
    private Protocol sourceProtocol;

    private LocalDate targetStartDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_plan_id")
    private Plan parentPlan;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderColumn(name = "child_order")
    private List<ProposedAction> actions = new ArrayList<>();

    @OneToMany(mappedBy = "parentPlan", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Plan> subPlans = new ArrayList<>();

    public Plan() {}

    public Plan(String name) {
        this.name = name;
    }

    @Override
    public Long getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public boolean isLeaf() { return false; }


    @Override
    public String getStatus() {
        List<PlanNode> children = getChildren();
        if (children.isEmpty()) return "PROPOSED";

        boolean allCompleted = true;
        boolean allAbandoned = true;
        boolean anyInProgress = false;
        boolean anyCompleted = false;
        boolean anySuspended = false;

        for (PlanNode child : children) {
            String status = child.getStatus();
            if (!"COMPLETED".equals(status)) allCompleted = false;
            if (!"ABANDONED".equals(status)) allAbandoned = false;
            if ("IN_PROGRESS".equals(status)) anyInProgress = true;
            if ("COMPLETED".equals(status)) anyCompleted = true;
            if ("SUSPENDED".equals(status)) anySuspended = true;
        }

        if (allAbandoned) return "ABANDONED";
        if (allCompleted) return "COMPLETED";
        if (anyInProgress || anyCompleted) return "IN_PROGRESS";
        if (anySuspended) return "SUSPENDED";
        return "PROPOSED";
    }


    @Override
    public BigDecimal getTotalAllocatedQuantity(ResourceType resourceType) {
        BigDecimal total = BigDecimal.ZERO;
        for (PlanNode child : getChildren()) {
            total = total.add(child.getTotalAllocatedQuantity(resourceType));
        }
        return total;
    }


    @Override
    public void accept(PlanNodeVisitor visitor) {
        visitor.visitComposite(this);
        for (PlanNode child : getChildren()) {
            child.accept(visitor);
        }
    }


    public List<PlanNode> getChildren() {
        List<PlanNode> children = new ArrayList<>();
        children.addAll(subPlans);
        children.addAll(actions);
        return children;
    }

    public void addAction(ProposedAction action) {
        actions.add(action);
        action.setPlan(this);
    }

    public void addSubPlan(Plan subPlan) {
        subPlans.add(subPlan);
        subPlan.setParentPlan(this);
    }

    // --- Getters and Setters ---
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }

    public Protocol getSourceProtocol() { return sourceProtocol; }
    public void setSourceProtocol(Protocol sourceProtocol) { this.sourceProtocol = sourceProtocol; }

    public LocalDate getTargetStartDate() { return targetStartDate; }
    public void setTargetStartDate(LocalDate targetStartDate) { this.targetStartDate = targetStartDate; }

    public Plan getParentPlan() { return parentPlan; }
    public void setParentPlan(Plan parentPlan) { this.parentPlan = parentPlan; }

    public List<ProposedAction> getActions() { return actions; }
    public void setActions(List<ProposedAction> actions) { this.actions = actions; }

    public List<Plan> getSubPlans() { return subPlans; }
    public void setSubPlans(List<Plan> subPlans) { this.subPlans = subPlans; }
}