package com.pm.resourceplanningledger.domain.operational;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class FilteredPlanIterator implements Iterator<PlanNode> {

    private final DepthFirstPlanIterator inner;
    private final Predicate<PlanNode> predicate;
    private PlanNode nextNode;

    public FilteredPlanIterator(PlanNode root, Predicate<PlanNode> predicate) {
        this.inner = new DepthFirstPlanIterator(root);
        this.predicate = predicate;
        this.nextNode = advance();
    }

    private PlanNode advance() {
        while (inner.hasNext()) {
            PlanNode candidate = inner.next();
            if (predicate.test(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        return nextNode != null;
    }

    @Override
    public PlanNode next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more matching nodes");
        }
        PlanNode current = nextNode;
        nextNode = advance();
        return current;
    }
}