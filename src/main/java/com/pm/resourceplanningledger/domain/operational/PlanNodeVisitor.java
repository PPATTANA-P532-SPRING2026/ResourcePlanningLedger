package com.pm.resourceplanningledger.domain.operational;

public interface PlanNodeVisitor {
    void visitPlan(Plan plan);
    void visitAction(ProposedAction action);
}