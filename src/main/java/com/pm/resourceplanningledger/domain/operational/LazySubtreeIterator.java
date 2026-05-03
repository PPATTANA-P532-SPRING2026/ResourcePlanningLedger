package com.pm.resourceplanningledger.domain.operational;

import java.util.*;

public class LazySubtreeIterator implements Iterator<PlanNode> {

    private final Deque<NodeWithDepth> stack = new ArrayDeque<>();
    private final int depthLimit;

    public LazySubtreeIterator(PlanNode root, int depthLimit) {
        this.depthLimit = depthLimit;
        if (root != null) {
            stack.push(new NodeWithDepth(root, 0));
        }
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public PlanNode next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more nodes");
        }
        NodeWithDepth current = stack.pop();
        PlanNode node = current.node;
        int depth = current.depth;

        // Only expand children if under the depth limit and node is composite
        if (node instanceof Plan plan && depth < depthLimit) {
            List<PlanNode> children = plan.getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(new NodeWithDepth(children.get(i), depth + 1));
            }
        }
        // If at or beyond depth limit, yield the node itself without expanding

        return node;
    }

    private static class NodeWithDepth {
        final PlanNode node;
        final int depth;

        NodeWithDepth(PlanNode node, int depth) {
            this.node = node;
            this.depth = depth;
        }
    }
}