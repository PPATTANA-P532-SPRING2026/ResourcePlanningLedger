package com.pm.resourceplanningledger.domain.operational;

public class RiskScoreVisitor implements PlanNodeVisitor {

    private int score = 0;

    @Override
    public void visitLeaf(ProposedAction leaf) {
        String status = leaf.getStateName();
        if ("SUSPENDED".equals(status) || "ABANDONED".equals(status)) {
            score++;
        }
    }

    @Override
    public void visitComposite(Plan plan) {
        // Risk computed from leaves only
    }

    public int getScore() { return score; }
}