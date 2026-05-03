package com.pm.resourceplanningledger.domain.operational;

public interface PlanNodeVisitor {
    void visitLeaf(ProposedAction leaf);
    void visitComposite(Plan plan);
}