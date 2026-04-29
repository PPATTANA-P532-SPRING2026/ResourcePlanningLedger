package com.pm.resourceplanningledger.manager;

import com.pm.resourceplanningledger.domain.knowledge.ResourceType;
import com.pm.resourceplanningledger.domain.ledger.Account;
import com.pm.resourceplanningledger.domain.ledger.AuditLogEntry;
import com.pm.resourceplanningledger.domain.ledger.Entry;
import com.pm.resourceplanningledger.domain.ledger.Transaction;
import com.pm.resourceplanningledger.domain.operational.*;
import com.pm.resourceplanningledger.domain.state.ActionContext;
import com.pm.resourceplanningledger.domain.state.ActionState;
import com.pm.resourceplanningledger.domain.state.ActionStateMachine;
import com.pm.resourceplanningledger.engine.ConsumableLedgerEntryGenerator;
import com.pm.resourceplanningledger.engine.PostingRuleEngine;
import com.pm.resourceplanningledger.resourceaccess.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActionManager {

    private final ProposedActionRepository proposedActionRepository;
    private final ImplementedActionRepository implementedActionRepository;
    private final ResourceAllocationRepository resourceAllocationRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final SuspensionRepository suspensionRepository;
    private final AuditLogRepository auditLogRepository;
    private final ActionStateMachine stateMachine;
    private final ConsumableLedgerEntryGenerator consumableLedgerEntryGenerator;
    private final PostingRuleEngine postingRuleEngine;
    private final Clock clock;

    public ActionManager(ProposedActionRepository proposedActionRepository,
                         ImplementedActionRepository implementedActionRepository,
                         ResourceAllocationRepository resourceAllocationRepository,
                         ResourceTypeRepository resourceTypeRepository,
                         AccountRepository accountRepository,
                         TransactionRepository transactionRepository,
                         SuspensionRepository suspensionRepository,
                         AuditLogRepository auditLogRepository,
                         ActionStateMachine stateMachine,
                         ConsumableLedgerEntryGenerator consumableLedgerEntryGenerator,
                         PostingRuleEngine postingRuleEngine,
                         Clock clock) {
        this.proposedActionRepository = proposedActionRepository;
        this.implementedActionRepository = implementedActionRepository;
        this.resourceAllocationRepository = resourceAllocationRepository;
        this.resourceTypeRepository = resourceTypeRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.suspensionRepository = suspensionRepository;
        this.auditLogRepository = auditLogRepository;
        this.stateMachine = stateMachine;
        this.consumableLedgerEntryGenerator = consumableLedgerEntryGenerator;
        this.postingRuleEngine = postingRuleEngine;
        this.clock = clock;
    }

    public ProposedAction findById(Long id) {
        return proposedActionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Action not found: " + id));
    }

    @Transactional
    public ProposedAction implement(Long actionId) {
        ProposedAction action = findById(actionId);
        ActionState currentState = stateMachine.resolve(action.getStateName());
        ActionContext ctx = new ActionContext(action, this);
        currentState.implement(ctx);
        return proposedActionRepository.save(action);
    }

    @Transactional
    public ProposedAction suspend(Long actionId, String reason) {
        ProposedAction action = findById(actionId);
        ActionState currentState = stateMachine.resolve(action.getStateName());
        ActionContext ctx = new ActionContext(action, this);
        currentState.suspend(ctx, reason);
        return proposedActionRepository.save(action);
    }

    @Transactional
    public ProposedAction resume(Long actionId) {
        ProposedAction action = findById(actionId);
        ActionState currentState = stateMachine.resolve(action.getStateName());
        ActionContext ctx = new ActionContext(action, this);
        currentState.resume(ctx);
        return proposedActionRepository.save(action);
    }

    @Transactional
    public ProposedAction complete(Long actionId) {
        ProposedAction action = findById(actionId);
        ActionState currentState = stateMachine.resolve(action.getStateName());
        ActionContext ctx = new ActionContext(action, this);
        currentState.complete(ctx);
        return proposedActionRepository.save(action);
    }

    @Transactional
    public ProposedAction abandon(Long actionId) {
        ProposedAction action = findById(actionId);
        ActionState currentState = stateMachine.resolve(action.getStateName());
        ActionContext ctx = new ActionContext(action, this);
        currentState.abandon(ctx);
        return proposedActionRepository.save(action);
    }

    // Called by state objects via ActionContext

    public void onImplement(ProposedAction action) {
        LocalDateTime now = LocalDateTime.now(clock);
        ImplementedAction impl = new ImplementedAction(action, now);
        impl.setActualParty(action.getParty());
        impl.setActualLocation(action.getLocation());
        implementedActionRepository.save(impl);
        action.setImplementedAction(impl);

        auditLogRepository.save(new AuditLogEntry(
                "ACTION_IMPLEMENTED", null, null, action.getId(), now));
    }

    public void onSuspend(ProposedAction action, String reason) {
        LocalDateTime now = LocalDateTime.now(clock);
        Suspension suspension = new Suspension(action, reason, LocalDate.now(clock));
        suspensionRepository.save(suspension);

        // Close any open suspensions when resuming later
        auditLogRepository.save(new AuditLogEntry(
                "ACTION_SUSPENDED", null, null, action.getId(), now));
    }

    public void onResume(ProposedAction action) {
        LocalDateTime now = LocalDateTime.now(clock);
        // Close the most recent open suspension
        List<Suspension> suspensions = action.getSuspensions();
        for (Suspension s : suspensions) {
            if (s.getEndDate() == null) {
                s.setEndDate(LocalDate.now(clock));
                suspensionRepository.save(s);
            }
        }

        auditLogRepository.save(new AuditLogEntry(
                "ACTION_RESUMED", null, null, action.getId(), now));
    }

    public void onComplete(ProposedAction action) {
        LocalDateTime now = LocalDateTime.now(clock);
        ImplementedAction impl = implementedActionRepository.findByProposedActionId(action.getId())
                .orElse(null);

        if (impl != null) {
            impl.setStatus("COMPLETED");
            implementedActionRepository.save(impl);

            // Generate ledger entries using Template Method
            if (!action.getAllocations().isEmpty()) {
                Transaction tx = consumableLedgerEntryGenerator.generateEntries(impl);

                // Set usage accounts for deposit entries
                for (Entry entry : tx.getEntries()) {
                    if (entry.getAccount() == null) {
                        Account usageAccount = new Account(
                                "Usage - " + action.getName(),
                                Account.AccountKind.USAGE
                        );
                        usageAccount = accountRepository.save(usageAccount);
                        entry.setAccount(usageAccount);
                    }
                }

                tx = transactionRepository.save(tx);

                // Evaluate posting rules (over-consumption alert)
                postingRuleEngine.evaluate(tx);
                transactionRepository.save(tx);

                // Audit log for each entry
                for (Entry entry : tx.getEntries()) {
                    auditLogRepository.save(new AuditLogEntry(
                            "LEDGER_ENTRY_CREATED",
                            entry.getAccount().getId(),
                            entry.getId(),
                            action.getId(),
                            now
                    ));
                }
            }
        }

        auditLogRepository.save(new AuditLogEntry(
                "ACTION_COMPLETED", null, null, action.getId(), now));
    }

    @Transactional
    public ResourceAllocation addAllocation(Long actionId, Long resourceTypeId,
                                            BigDecimal quantity, ResourceAllocation.AllocationKind kind,
                                            String assetId, String timePeriod) {
        ProposedAction action = findById(actionId);
        ResourceType resourceType = resourceTypeRepository.findById(resourceTypeId)
                .orElseThrow(() -> new IllegalArgumentException("ResourceType not found: " + resourceTypeId));

        ResourceAllocation allocation = new ResourceAllocation(resourceType, quantity, kind);
        allocation.setAssetId(assetId);
        allocation.setTimePeriod(timePeriod);
        action.addAllocation(allocation);

        proposedActionRepository.save(action);
        return allocation;
    }
}