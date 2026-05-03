package com.pm.resourceplanningledger.engine;

import com.pm.resourceplanningledger.domain.knowledge.ResourceType;
import com.pm.resourceplanningledger.domain.ledger.Account;
import com.pm.resourceplanningledger.domain.ledger.Entry;
import com.pm.resourceplanningledger.domain.ledger.Transaction;
import com.pm.resourceplanningledger.domain.operational.ImplementedAction;
import com.pm.resourceplanningledger.domain.operational.ResourceAllocation;
import com.pm.resourceplanningledger.resourceaccess.AuditLogRepository;
import com.pm.resourceplanningledger.domain.ledger.AuditLogEntry;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssetLedgerEntryGenerator extends AbstractLedgerEntryGenerator {

    private final AuditLogRepository auditLogRepository;

    public AssetLedgerEntryGenerator(Clock clock, AuditLogRepository auditLogRepository) {
        super(clock);
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    protected List<ResourceAllocation> selectAllocations(ImplementedAction action) {
        return action.getProposedAction().getAllocations().stream()
                .filter(a -> a.getResourceType().getKind() == ResourceType.ResourceKind.ASSET)
                .filter(a -> a.getKind() == ResourceAllocation.AllocationKind.SPECIFIC)
                .collect(Collectors.toList());
    }

    @Override
    protected void validate(List<ResourceAllocation> allocs) {
        for (ResourceAllocation alloc : allocs) {
            if (alloc.getTimePeriod() == null || alloc.getTimePeriod().isEmpty()) {
                throw new IllegalArgumentException(
                        "Asset allocation for '" + alloc.getResourceType().getName() +
                                "' must have a non-null time period");
            }
            double hours = parseHours(alloc.getTimePeriod());
            if (hours <= 0) {
                throw new IllegalArgumentException(
                        "Asset allocation for '" + alloc.getResourceType().getName() +
                                "' must have a positive duration in hours");
            }
        }
    }

    @Override
    protected Entry buildWithdrawal(Transaction tx, ResourceAllocation a) {
        Account poolAccount = a.getResourceType().getPoolAccount();
        LocalDateTime now = LocalDateTime.now(clock);
        BigDecimal hours = BigDecimal.valueOf(parseHours(a.getTimePeriod()));
        return new Entry(poolAccount, hours.negate(), now, now);
    }

    @Override
    protected Entry buildDeposit(Transaction tx, ResourceAllocation a) {
        LocalDateTime now = LocalDateTime.now(clock);
        BigDecimal hours = BigDecimal.valueOf(parseHours(a.getTimePeriod()));
        return new Entry(null, hours, now, now);
    }

    @Override
    protected void afterPost(Transaction tx) {
        // Hook: append utilisation record to audit log for each asset
        LocalDateTime now = LocalDateTime.now(clock);
        for (Entry entry : tx.getEntries()) {
            if (entry.getAccount() != null && entry.getAmount().signum() < 0) {
                auditLogRepository.save(new AuditLogEntry(
                        "ASSET_UTILISATION",
                        entry.getAccount().getId(),
                        entry.getId(),
                        entry.getActionId(),
                        now
                ));
            }
        }
    }

    private double parseHours(String timePeriod) {
        try {
            return Double.parseDouble(timePeriod.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}