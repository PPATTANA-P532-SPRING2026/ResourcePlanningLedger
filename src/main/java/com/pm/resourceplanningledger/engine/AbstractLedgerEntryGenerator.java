package com.pm.resourceplanningledger.engine;

import com.pm.resourceplanningledger.domain.ledger.Account;
import com.pm.resourceplanningledger.domain.ledger.Entry;
import com.pm.resourceplanningledger.domain.ledger.Transaction;
import com.pm.resourceplanningledger.domain.operational.ImplementedAction;
import com.pm.resourceplanningledger.domain.operational.ResourceAllocation;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

public abstract class AbstractLedgerEntryGenerator {

    protected final Clock clock;

    protected AbstractLedgerEntryGenerator(Clock clock) {
        this.clock = clock;
    }

    // Template method — final, defines the skeleton
    public final Transaction generateEntries(ImplementedAction action) {
        List<ResourceAllocation> allocs = selectAllocations(action);
        validate(allocs);
        Transaction tx = createTransaction(action);
        for (ResourceAllocation a : allocs) {
            Entry withdrawal = buildWithdrawal(tx, a);
            Entry deposit = buildDeposit(tx, a);
            postEntries(tx, withdrawal, deposit);
        }
        afterPost(tx);
        return tx;
    }

    protected abstract List<ResourceAllocation> selectAllocations(ImplementedAction action);

    protected abstract void validate(List<ResourceAllocation> allocs);

    protected Entry buildWithdrawal(Transaction tx, ResourceAllocation a) {
        Account poolAccount = a.getResourceType().getPoolAccount();
        LocalDateTime now = LocalDateTime.now(clock);
        return new Entry(poolAccount, a.getQuantity().negate(), now, now);
    }

    protected Entry buildDeposit(Transaction tx, ResourceAllocation a) {
        // Usage account is created per action; for now use a placeholder
        // The manager layer handles creating/finding the usage account
        LocalDateTime now = LocalDateTime.now(clock);
        return new Entry(null, a.getQuantity(), now, now);  // account set by manager
    }

    protected void afterPost(Transaction tx) {
        // Hook — empty in base class. Override in subclasses.
    }

    private Transaction createTransaction(ImplementedAction action) {
        LocalDateTime now = LocalDateTime.now(clock);
        return new Transaction(
                "Completion of action: " + action.getProposedAction().getName(),
                now
        );
    }

    private void postEntries(Transaction tx, Entry withdrawal, Entry deposit) {
        tx.addEntry(withdrawal);
        tx.addEntry(deposit);
        // Conservation check: withdrawal + deposit should net to zero
        BigDecimal net = withdrawal.getAmount().add(deposit.getAmount());
        if (net.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Entries are not balanced. Net: " + net);
        }
    }
}