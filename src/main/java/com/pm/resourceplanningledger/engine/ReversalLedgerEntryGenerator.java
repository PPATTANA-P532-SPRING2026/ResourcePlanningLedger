package com.pm.resourceplanningledger.engine;

import com.pm.resourceplanningledger.domain.knowledge.ResourceType;
import com.pm.resourceplanningledger.domain.ledger.Account;
import com.pm.resourceplanningledger.domain.ledger.Entry;
import com.pm.resourceplanningledger.domain.ledger.Transaction;
import com.pm.resourceplanningledger.domain.operational.ImplementedAction;
import com.pm.resourceplanningledger.domain.operational.ResourceAllocation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReversalLedgerEntryGenerator extends AbstractLedgerEntryGenerator {

    public ReversalLedgerEntryGenerator(Clock clock) {
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
        // Reversals are always valid if there are allocations
    }

    @Override
    protected Entry buildWithdrawal(Transaction tx, ResourceAllocation a) {
        // Reversal: DEPOSIT back into pool (positive amount)
        Account poolAccount = a.getResourceType().getPoolAccount();
        LocalDateTime now = LocalDateTime.now(clock);
        return new Entry(poolAccount, a.getQuantity(), now, now);
    }

    @Override
    protected Entry buildDeposit(Transaction tx, ResourceAllocation a) {
        // Reversal: WITHDRAW from usage account (negative amount)
        LocalDateTime now = LocalDateTime.now(clock);
        return new Entry(null, a.getQuantity().negate(), now, now);
    }
}