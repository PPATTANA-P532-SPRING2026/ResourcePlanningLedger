package com.pm.resourceplanningledger;

import com.pm.resourceplanningledger.domain.knowledge.ResourceType;
import com.pm.resourceplanningledger.domain.ledger.Account;
import com.pm.resourceplanningledger.domain.ledger.Transaction;
import com.pm.resourceplanningledger.domain.operational.*;
import com.pm.resourceplanningledger.domain.state.*;
import com.pm.resourceplanningledger.engine.AssetLedgerEntryGenerator;
import com.pm.resourceplanningledger.engine.ReversalLedgerEntryGenerator;
import com.pm.resourceplanningledger.manager.ActionManager;
import com.pm.resourceplanningledger.resourceaccess.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Week2Tests {

    private Clock fixedClock;

    @Mock
    private ActionManager actionManager;

    @Mock
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneId.of("UTC"));
    }

    // =========================================================================
    // CHANGE 1: PendingApprovalState tests
    // =========================================================================

    @Test
    void submitForApproval_proposedState_transitionsToPendingApproval() {
        // Arrange
        ProposedAction action = new ProposedAction("Test");
        action.setStateName("PROPOSED");
        ActionContext ctx = new ActionContext(action, actionManager);
        ProposedState state = new ProposedState();

        // Act
        state.submitForApproval(ctx);

        // Assert
        assertEquals("PENDING_APPROVAL", action.getStateName());
    }

    @Test
    void implement_proposedState_nowThrowsIllegalStateTransition() {
        // Arrange — Week 2: direct implement removed
        ProposedAction action = new ProposedAction("Test");
        ActionContext ctx = new ActionContext(action, actionManager);
        ProposedState state = new ProposedState();

        // Act & Assert
        assertThrows(IllegalStateTransitionException.class, () -> state.implement(ctx));
    }

    @Test
    void approve_pendingApprovalState_transitionsToInProgress() {
        // Arrange
        ProposedAction action = new ProposedAction("Test");
        action.setStateName("PENDING_APPROVAL");
        ActionContext ctx = new ActionContext(action, actionManager);
        PendingApprovalState state = new PendingApprovalState();

        // Act
        state.approve(ctx);

        // Assert
        assertEquals("IN_PROGRESS", action.getStateName());
    }

    @Test
    void reject_pendingApprovalState_transitionsToProposed() {
        // Arrange
        ProposedAction action = new ProposedAction("Test");
        action.setStateName("PENDING_APPROVAL");
        ActionContext ctx = new ActionContext(action, actionManager);
        PendingApprovalState state = new PendingApprovalState();

        // Act
        state.reject(ctx);

        // Assert
        assertEquals("PROPOSED", action.getStateName());
    }

    @Test
    void suspend_pendingApprovalState_throwsIllegalStateTransition() {
        // Arrange
        ProposedAction action = new ProposedAction("Test");
        ActionContext ctx = new ActionContext(action, actionManager);
        PendingApprovalState state = new PendingApprovalState();

        // Act & Assert
        assertThrows(IllegalStateTransitionException.class, () -> state.suspend(ctx, "reason"));
    }

    // =========================================================================
    // CHANGE 1: ReopenedState tests
    // =========================================================================

    @Test
    void reopen_completedState_transitionsToReopened() {
        // Arrange
        ProposedAction action = new ProposedAction("Test");
        action.setStateName("COMPLETED");
        ActionContext ctx = new ActionContext(action, actionManager);
        CompletedState state = new CompletedState();

        // Act
        state.reopen(ctx);

        // Assert
        assertEquals("REOPENED", action.getStateName());
    }

    @Test
    void complete_reopenedState_transitionsToCompleted() {
        // Arrange
        ProposedAction action = new ProposedAction("Test");
        action.setStateName("REOPENED");
        ActionContext ctx = new ActionContext(action, actionManager);
        ReopenedState state = new ReopenedState();

        // Act
        state.complete(ctx);

        // Assert
        assertEquals("COMPLETED", action.getStateName());
    }

    @Test
    void abandon_reopenedState_transitionsToAbandoned() {
        // Arrange
        ProposedAction action = new ProposedAction("Test");
        action.setStateName("REOPENED");
        ActionContext ctx = new ActionContext(action, actionManager);
        ReopenedState state = new ReopenedState();

        // Act
        state.abandon(ctx);

        // Assert
        assertEquals("ABANDONED", action.getStateName());
    }

    @Test
    void reopen_reopenedState_throwsIllegalStateTransition() {
        // Arrange
        ProposedAction action = new ProposedAction("Test");
        ActionContext ctx = new ActionContext(action, actionManager);
        ReopenedState state = new ReopenedState();

        // Act & Assert
        assertThrows(IllegalStateTransitionException.class, () -> state.reopen(ctx));
    }

    // =========================================================================
    // CHANGE 1: ReversalLedgerEntryGenerator test
    // =========================================================================

    @Test
    void reversalGenerator_producesOppositeEntries() {
        // Arrange
        ReversalLedgerEntryGenerator generator = new ReversalLedgerEntryGenerator(fixedClock);

        ResourceType cement = new ResourceType("Cement", ResourceType.ResourceKind.CONSUMABLE, "kg");
        cement.setId(1L);
        Account poolAccount = new Account("Pool - Cement", Account.AccountKind.POOL);
        poolAccount.setId(1L);
        cement.setPoolAccount(poolAccount);

        ProposedAction action = new ProposedAction("Build Wall");
        action.addAllocation(new ResourceAllocation(
                cement, new BigDecimal("100"), ResourceAllocation.AllocationKind.GENERAL));

        ImplementedAction impl = new ImplementedAction(action, java.time.LocalDateTime.now(fixedClock));

        // Act
        Transaction tx = generator.generateEntries(impl);

        // Assert: withdrawal should be POSITIVE (restoring pool), deposit NEGATIVE
        assertEquals(2, tx.getEntries().size());
        BigDecimal net = tx.getEntries().stream()
                .map(e -> e.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, BigDecimal.ZERO.compareTo(net)); // still balanced
        // Pool entry should be positive (restoring)
        boolean hasPositivePoolEntry = tx.getEntries().stream()
                .anyMatch(e -> e.getAccount() != null && e.getAmount().signum() > 0);
        assertTrue(hasPositivePoolEntry);
    }

    // =========================================================================
    // CHANGE 2: AssetLedgerEntryGenerator tests
    // =========================================================================

    @Test
    void assetGenerator_selectsOnlySpecificAssetAllocations() {
        // Arrange
        AssetLedgerEntryGenerator generator = new AssetLedgerEntryGenerator(fixedClock, auditLogRepository);

        ResourceType crane = new ResourceType("Crane", ResourceType.ResourceKind.ASSET, "hours");
        crane.setId(2L);
        Account assetPool = new Account("Pool - Crane", Account.AccountKind.POOL);
        assetPool.setId(2L);
        crane.setPoolAccount(assetPool);

        ResourceType cement = new ResourceType("Cement", ResourceType.ResourceKind.CONSUMABLE, "kg");
        cement.setId(1L);
        Account poolAccount = new Account("Pool - Cement", Account.AccountKind.POOL);
        poolAccount.setId(1L);
        cement.setPoolAccount(poolAccount);

        ProposedAction action = new ProposedAction("Crane Work");
        ResourceAllocation assetAlloc = new ResourceAllocation(crane, new BigDecimal("8"), ResourceAllocation.AllocationKind.SPECIFIC);
        assetAlloc.setTimePeriod("8");
        action.addAllocation(assetAlloc);
        // Also add consumable — should be ignored
        action.addAllocation(new ResourceAllocation(cement, new BigDecimal("50"), ResourceAllocation.AllocationKind.GENERAL));

        ImplementedAction impl = new ImplementedAction(action, java.time.LocalDateTime.now(fixedClock));

        // Act
        Transaction tx = generator.generateEntries(impl);

        // Assert: 2 entries (withdrawal + deposit) for crane only, not cement
        assertEquals(2, tx.getEntries().size());
    }

    @Test
    void assetGenerator_usesHoursAsAmount() {
        // Arrange
        AssetLedgerEntryGenerator generator = new AssetLedgerEntryGenerator(fixedClock, auditLogRepository);

        ResourceType crane = new ResourceType("Crane", ResourceType.ResourceKind.ASSET, "hours");
        crane.setId(2L);
        Account assetPool = new Account("Pool - Crane", Account.AccountKind.POOL);
        assetPool.setId(2L);
        crane.setPoolAccount(assetPool);

        ProposedAction action = new ProposedAction("Crane Work");
        ResourceAllocation assetAlloc = new ResourceAllocation(crane, new BigDecimal("1"), ResourceAllocation.AllocationKind.SPECIFIC);
        assetAlloc.setTimePeriod("4.5");
        action.addAllocation(assetAlloc);

        ImplementedAction impl = new ImplementedAction(action, java.time.LocalDateTime.now(fixedClock));

        // Act
        Transaction tx = generator.generateEntries(impl);

        // Assert: withdrawal amount should be -4.5 (hours), not -1 (quantity)
        boolean hasCorrectAmount = tx.getEntries().stream()
                .anyMatch(e -> e.getAccount() != null &&
                        e.getAmount().compareTo(new BigDecimal("-4.5")) == 0);
        assertTrue(hasCorrectAmount);
    }

    @Test
    void assetGenerator_nullTimePeriod_throwsValidation() {
        // Arrange
        AssetLedgerEntryGenerator generator = new AssetLedgerEntryGenerator(fixedClock, auditLogRepository);

        ResourceType crane = new ResourceType("Crane", ResourceType.ResourceKind.ASSET, "hours");
        crane.setId(2L);
        Account assetPool = new Account("Pool - Crane", Account.AccountKind.POOL);
        assetPool.setId(2L);
        crane.setPoolAccount(assetPool);

        ProposedAction action = new ProposedAction("Crane Work");
        ResourceAllocation assetAlloc = new ResourceAllocation(crane, new BigDecimal("1"), ResourceAllocation.AllocationKind.SPECIFIC);
        assetAlloc.setTimePeriod(null); // Missing time period
        action.addAllocation(assetAlloc);

        ImplementedAction impl = new ImplementedAction(action, java.time.LocalDateTime.now(fixedClock));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> generator.generateEntries(impl));
    }

    // =========================================================================
    // CHANGE 3: FilteredPlanIterator tests
    // =========================================================================

    @Test
    void filteredIterator_onlyReturnsMatchingNodes() {
        // Arrange
        Plan root = new Plan("Root");
        root.setId(1L);

        ProposedAction a1 = new ProposedAction("A1");
        a1.setId(2L);
        a1.setStateName("COMPLETED");

        ProposedAction a2 = new ProposedAction("A2");
        a2.setId(3L);
        a2.setStateName("PROPOSED");

        ProposedAction a3 = new ProposedAction("A3");
        a3.setId(4L);
        a3.setStateName("COMPLETED");

        root.addAction(a1);
        root.addAction(a2);
        root.addAction(a3);

        // Act
        FilteredPlanIterator iterator = new FilteredPlanIterator(root,
                node -> "COMPLETED".equals(node.getStatus()));

        java.util.List<String> names = new java.util.ArrayList<>();
        while (iterator.hasNext()) {
            names.add(iterator.next().getName());
        }

        // Assert: only COMPLETED nodes — A1 and A3 (root plan excluded since it's not COMPLETED)
        assertTrue(names.contains("A1"));
        assertTrue(names.contains("A3"));
        assertFalse(names.contains("A2"));
        assertFalse(names.contains("Root"));
    }

    @Test
    void filteredIterator_noMatches_returnsEmpty() {
        // Arrange
        Plan root = new Plan("Root");
        root.setId(1L);
        ProposedAction a1 = new ProposedAction("A1");
        a1.setId(2L);
        a1.setStateName("PROPOSED");
        root.addAction(a1);

        // Act
        FilteredPlanIterator iterator = new FilteredPlanIterator(root,
                node -> "COMPLETED".equals(node.getStatus()));

        // Assert
        assertFalse(iterator.hasNext());
    }

    // =========================================================================
    // CHANGE 3: LazySubtreeIterator tests
    // =========================================================================

    @Test
    void lazyIterator_depthZero_onlyReturnsRoot() {
        // Arrange
        Plan root = new Plan("Root");
        root.setId(1L);
        Plan sub = new Plan("Sub");
        sub.setId(2L);
        ProposedAction a1 = new ProposedAction("A1");
        a1.setId(3L);
        sub.addAction(a1);
        root.addSubPlan(sub);

        // Act
        LazySubtreeIterator iterator = new LazySubtreeIterator(root, 0);
        java.util.List<String> names = new java.util.ArrayList<>();
        while (iterator.hasNext()) {
            names.add(iterator.next().getName());
        }

        // Assert: only root, no children expanded
        assertEquals(List.of("Root"), names);
    }

    @Test
    void lazyIterator_depthOne_expandsRootNotSub() {
        // Arrange
        Plan root = new Plan("Root");
        root.setId(1L);
        Plan sub = new Plan("Sub");
        sub.setId(2L);
        ProposedAction a1 = new ProposedAction("A1");
        a1.setId(3L);
        sub.addAction(a1);
        root.addSubPlan(sub);

        // Act
        LazySubtreeIterator iterator = new LazySubtreeIterator(root, 1);
        java.util.List<String> names = new java.util.ArrayList<>();
        while (iterator.hasNext()) {
            names.add(iterator.next().getName());
        }

        // Assert: Root + Sub (but not A1 inside Sub)
        assertTrue(names.contains("Root"));
        assertTrue(names.contains("Sub"));
        assertFalse(names.contains("A1"));
    }

    // =========================================================================
    // CHANGE 4: Visitor tests
    // =========================================================================

    @Test
    void completionRatioVisitor_allCompleted_returnsOne() {
        // Arrange
        Plan plan = new Plan("Plan");
        ProposedAction a1 = new ProposedAction("A1");
        a1.setStateName("COMPLETED");
        ProposedAction a2 = new ProposedAction("A2");
        a2.setStateName("COMPLETED");
        plan.addAction(a1);
        plan.addAction(a2);
        CompletionRatioVisitor visitor = new CompletionRatioVisitor();

        // Act
        plan.accept(visitor);

        // Assert
        assertEquals(1.0, visitor.getRatio(), 0.001);
        assertEquals(2, visitor.getTotalLeaves());
        assertEquals(2, visitor.getCompletedLeaves());
    }

    @Test
    void completionRatioVisitor_noneCompleted_returnsZero() {
        // Arrange
        Plan plan = new Plan("Plan");
        ProposedAction a1 = new ProposedAction("A1");
        a1.setStateName("PROPOSED");
        plan.addAction(a1);
        CompletionRatioVisitor visitor = new CompletionRatioVisitor();

        // Act
        plan.accept(visitor);

        // Assert
        assertEquals(0.0, visitor.getRatio(), 0.001);
    }

    @Test
    void riskScoreVisitor_countsSuspendedAndAbandoned() {
        // Arrange
        Plan plan = new Plan("Plan");
        ProposedAction a1 = new ProposedAction("A1");
        a1.setStateName("SUSPENDED");
        ProposedAction a2 = new ProposedAction("A2");
        a2.setStateName("ABANDONED");
        ProposedAction a3 = new ProposedAction("A3");
        a3.setStateName("COMPLETED");
        plan.addAction(a1);
        plan.addAction(a2);
        plan.addAction(a3);
        RiskScoreVisitor visitor = new RiskScoreVisitor();

        // Act
        plan.accept(visitor);

        // Assert: 2 risky nodes (SUSPENDED + ABANDONED)
        assertEquals(2, visitor.getScore());
    }

    @Test
    void resourceCostVisitor_sumsQuantityTimesUnitCost() {
        // Arrange
        Plan plan = new Plan("Plan");
        ResourceType cement = new ResourceType("Cement", ResourceType.ResourceKind.CONSUMABLE, "kg");
        cement.setId(1L);
        cement.setUnitCost(new BigDecimal("5.00"));
        Account pool = new Account("Pool", Account.AccountKind.POOL);
        pool.setId(1L);
        cement.setPoolAccount(pool);

        ProposedAction a1 = new ProposedAction("A1");
        a1.addAllocation(new ResourceAllocation(cement, new BigDecimal("10"), ResourceAllocation.AllocationKind.GENERAL));
        plan.addAction(a1);

        ResourceCostVisitor visitor = new ResourceCostVisitor();

        // Act
        plan.accept(visitor);

        // Assert: 10 * 5.00 = 50.00
        assertEquals(0, new BigDecimal("50.00").compareTo(visitor.getTotalCost()));
    }
}