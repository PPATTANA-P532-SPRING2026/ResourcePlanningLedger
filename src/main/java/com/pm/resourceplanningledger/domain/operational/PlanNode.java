package com.pm.resourceplanningledger.domain.operational;

import com.pm.resourceplanningledger.domain.knowledge.ResourceType;
import java.math.BigDecimal;

public interface PlanNode {
    Long getId();
    String getName();
    String getStatus();
    BigDecimal getTotalAllocatedQuantity(ResourceType resourceType);
    void accept(PlanNodeVisitor visitor);
    boolean isLeaf();
}