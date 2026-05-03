package com.pm.resourceplanningledger.domain.operational;

import java.math.BigDecimal;

public class ResourceCostVisitor implements PlanNodeVisitor {

    private BigDecimal totalCost = BigDecimal.ZERO;

    @Override
    public void visitLeaf(ProposedAction leaf) {
        for (ResourceAllocation alloc : leaf.getAllocations()) {
            BigDecimal unitCost = alloc.getResourceType().getUnitCost();
            if (unitCost != null) {
                totalCost = totalCost.add(alloc.getQuantity().multiply(unitCost));
            }
        }
    }

    @Override
    public void visitComposite(Plan plan) {
        // Cost computed from leaves only
    }

    public BigDecimal getTotalCost() { return totalCost; }
}