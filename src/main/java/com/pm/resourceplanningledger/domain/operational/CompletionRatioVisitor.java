package com.pm.resourceplanningledger.domain.operational;

public class CompletionRatioVisitor implements PlanNodeVisitor {

    private int totalLeaves = 0;
    private int completedLeaves = 0;

    @Override
    public void visitLeaf(ProposedAction leaf) {
        totalLeaves++;
        if ("COMPLETED".equals(leaf.getStateName())) {
            completedLeaves++;
        }
    }

    @Override
    public void visitComposite(Plan plan) {
        // Do not count composites — only leaves
    }

    public double getRatio() {
        if (totalLeaves == 0) return 0.0;
        return (double) completedLeaves / totalLeaves;
    }

    public int getTotalLeaves() { return totalLeaves; }
    public int getCompletedLeaves() { return completedLeaves; }
}