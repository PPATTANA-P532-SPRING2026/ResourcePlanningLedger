package com.pm.resourceplanningledger.domain.operational;

import java.util.*;

public class DepthFirstPlanIterator implements Iterator<PlanNode> {

    private final Deque<PlanNode> stack = new ArrayDeque<>();

    public DepthFirstPlanIterator(PlanNode root) {
        if (root != null) {
            stack.push(root);
        }
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public PlanNode next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more nodes in plan tree");
        }
        PlanNode current = stack.pop();
        if (current instanceof Plan plan) {
            List<PlanNode> children = plan.getChildren();
            // Push in reverse order so first child is processed first
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(children.get(i));
            }
        }
        return current;
    }
}