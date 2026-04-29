package com.pm.resourceplanningledger.engine;

import com.pm.resourceplanningledger.domain.knowledge.ResourceType;
import com.pm.resourceplanningledger.domain.operational.ImplementedAction;
import com.pm.resourceplanningledger.domain.operational.ResourceAllocation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsumableLedgerEntryGenerator extends AbstractLedgerEntryGenerator {

    public ConsumableLedgerEntryGenerator(Clock clock) {
        super(clock);
    }

    @Override
    protected List<ResourceAllocation> selectAllocations(ImplementedAction action) {
        return action.getProposedAction().getAllocations().stream()
                .filter(a -> a.getResourceType().getKind() == ResourceType.ResourceKind.CONSUMABLE)
                .collect(Collectors.toList());
    }

    @Override
    protected void validate(List<ResourceAllocation> allocs) {
        for (ResourceAllocation alloc : allocs) {
            if (alloc.getQuantity() == null || alloc.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Consumable allocation must have a positive quantity: " + alloc.getResourceType().getName());
            }
        }
    }
}