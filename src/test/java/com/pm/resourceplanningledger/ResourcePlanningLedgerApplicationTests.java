package com.pm.resourceplanningledger;

import com.pm.resourceplanningledger.domain.knowledge.ResourceType;
import com.pm.resourceplanningledger.domain.ledger.Account;
import com.pm.resourceplanningledger.domain.ledger.Transaction;
import com.pm.resourceplanningledger.domain.operational.*;
import com.pm.resourceplanningledger.domain.state.*;
import com.pm.resourceplanningledger.engine.ConsumableLedgerEntryGenerator;
import com.pm.resourceplanningledger.manager.ActionManager;
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
class ResourcePlanningLedgerApplicationTests {

    private Clock fixedClock;

    @Mock
    private ActionManager actionManager;

    @BeforeEach
    void setUp() {
        // Arrange: fixed clock for determinism
        fixedClock = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneId.of("UTC"));
    }

    // =========================================================================
    // STATE PATTERN TESTS
    // =========================================================================

    @Test
    void implement_proposedState_transitionsToInProgress() {
        // Arrange
        ProposedAction action = new ProposedAction("Test Action");
        action.setStateName("PROPOSED");
        ActionContext ctx = new ActionContext(action, actionManager);
        ProposedState state = new ProposedState();

        // Act
        state.implement(ctx);

        // Assert
        assertEquals("IN_PROGRESS", action.getStateName());
    }

    @Test
    void suspend_proposedState_transitionsToSuspended() {
        // Arrange
        ProposedAction action = new ProposedAction("Test Action");
        action.setStateName("PROPOSED");
        ActionContext ctx = new ActionContext(action, actionManager);
        ProposedState state = new ProposedState();

        // Act
        state.suspend(ctx, "Budget freeze");

        // Assert
        assertEquals("SUSPENDED", action.getStateName());
    }

    @Test
    void abandon_proposedState_transitionsToAbandoned() {
        // Arrange
        ProposedAction action = new ProposedAction("Test Action");
        action.setStateName("PROPOSED");
        ActionContext ctx = new ActionContext(action, actionManager);
        ProposedState state = new ProposedState();

        // Act
        state.abandon(ctx);

        // Assert
        assertEquals("ABANDONED", action.getStateName());
    }

    @Test
    void resume_proposedState_throwsIllegalStateTransition() {
        // Arrange
        ProposedAction action = new ProposedAction("Test Action");
        ActionContext ctx = new ActionContext(action, actionManager);
        ProposedState state = new ProposedState();

        // Act & Assert
        assertThrows(IllegalStateTransitionException.class, () -> state.resume(ctx));
    }

    @Test
    void complete_proposedState_throwsIllegalStateTransition() {
        // Arrange
        ProposedAction action = new ProposedAction("Test Action");
        ActionContext ctx = new ActionContext(action, actionManager);
        ProposedState state = new ProposedState();

        // Act & Assert
        assertThrows(IllegalStateTransitionException.class, () -> state.complete(ctx));
    }

    @Test
    void resume_suspendedState_transitionsToProposed() {
        // Arrange
        ProposedAction action = new ProposedAction("Test Action");
        action.setStateName("SUSPENDED");
        ActionContext ctx = new ActionContext(action, actionManager);
        SuspendedState state = new SuspendedState();

        // Act
        state.resume(ctx);

        // Assert
        assertEquals("PROPOSED", action.getStateName());
    }

    @Test
    void abandon_suspendedState_transitionsToAbandoned() {
        // Arrange
        ProposedAction action = new ProposedAction("Test Action");
        action.setStateName("SUSPENDED");
        ActionContext ctx = new ActionContext(action, actionManager);
        SuspendedState state = new SuspendedState();

        // Act
        state.abandon(ctx);

        // Assert
        assertEquals("ABANDONED", action.getStateName());
    }

    @Test
    void complete_inProgressState_transitionsToCompleted() {
        // Arrange
        ProposedAction action = new ProposedAction("Test Action");
        action.setStateName("IN_PROGRESS");
        ActionContext ctx = new ActionContext(action, actionManager);
        InProgressState state = new InProgressState();

        // Act
        state.complete(ctx);

        // Assert
        assertEquals("COMPLETED", action.getStateName());
    }

    @Test
    void suspend_inProgressState_transitionsToSuspended() {
        // Arrange
        ProposedAction action = new ProposedAction("Test Action");
        action.setStateName("IN_PROGRESS");
        ActionContext ctx = new ActionContext(action, actionManager);
        InProgressState state = new InProgressState();

        // Act
        state.suspend(ctx, "Waiting for parts");

        // Assert
        assertEquals("SUSPENDED", action.getStateName());
    }

    @Test
    void abandon_inProgressState_transitionsToAbandoned() {
        // Arrange
        ProposedAction action = new ProposedAction("Test Action");
        action.setStateName("IN_PROGRESS");
        ActionContext ctx = new ActionContext(action, actionManager);
        InProgressState state = new InProgressState();

        // Act
        state.abandon(ctx);

        // Assert
        assertEquals("ABANDONED", action.getStateName());
    }

    @Test
    void implement_completedState_throwsIllegalStateTransition() {
        // Arrange
        ProposedAction action = new ProposedAction("Test Action");
        ActionContext ctx = new ActionContext(action, actionManager);
        CompletedState state = new CompletedState();

        // Act & Assert
        assertThrows(IllegalStateTransitionException.class, () -> state.implement(ctx));
    }

    @Test
    void abandon_completedState_throwsIllegalStateTransition() {
        // Arrange
        ProposedAction action = new ProposedAction("Test Action");
        ActionContext ctx = new ActionContext(action, actionManager);
        CompletedState state = new CompletedState();

        // Act & Assert
        assertThrows(IllegalStateTransitionException.class, () -> state.abandon(ctx));
    }

    @Test
    void resume_abandonedState_throwsIllegalStateTransition() {
        // Arrange
        ProposedAction action = new ProposedAction("Test Action");
        ActionContext ctx = new ActionContext(action, actionManager);
        AbandonedState state = new AbandonedState();

        // Act & Assert
        assertThrows(IllegalStateTransitionException.class, () -> state.resume(ctx));
    }

    // =========================================================================
    // COMPOSITE PATTERN TESTS
    // =========================================================================

    @Test
    void getStatus_allChildrenCompleted_returnsCompleted() {
        // Arrange
        Plan plan = new Plan("Master Plan");
        ProposedAction a1 = new ProposedAction("A1");
        a1.setStateName("COMPLETED");
        ProposedAction a2 = new ProposedAction("A2");
        a2.setStateName("COMPLETED");
        plan.addAction(a1);
        plan.addAction(a2);

        // Act
        String status = plan.getStatus();

        // Assert
        assertEquals("COMPLETED", status);
    }

    @Test
    void getStatus_allChildrenAbandoned_returnsAbandoned() {
        // Arrange
        Plan plan = new Plan("Master Plan");
        ProposedAction a1 = new ProposedAction("A1");
        a1.setStateName("ABANDONED");
        ProposedAction a2 = new ProposedAction("A2");
        a2.setStateName("ABANDONED");
        plan.addAction(a1);
        plan.addAction(a2);

        // Act
        String status = plan.getStatus();

        // Assert
        assertEquals("ABANDONED", status);
    }

    @Test
    void getStatus_someInProgress_returnsInProgress() {
        // Arrange
        Plan plan = new Plan("Master Plan");
        ProposedAction a1 = new ProposedAction("A1");
        a1.setStateName("IN_PROGRESS");
        ProposedAction a2 = new ProposedAction("A2");
        a2.setStateName("PROPOSED");
        plan.addAction(a1);
        plan.addAction(a2);

        // Act
        String status = plan.getStatus();

        // Assert
        assertEquals("IN_PROGRESS", status);
    }

    @Test
    void getStatus_someCompleted_someProposed_returnsInProgress() {
        // Arrange
        Plan plan = new Plan("Master Plan");
        ProposedAction a1 = new ProposedAction("A1");
        a1.setStateName("COMPLETED");
        ProposedAction a2 = new ProposedAction("A2");
        a2.setStateName("PROPOSED");
        plan.addAction(a1);
        plan.addAction(a2);

        // Act
        String status = plan.getStatus();

        // Assert
        assertEquals("IN_PROGRESS", status);
    }

    @Test
    void getStatus_suspended_nonInProgress_returnsSuspended() {
        // Arrange
        Plan plan = new Plan("Master Plan");
        ProposedAction a1 = new ProposedAction("A1");
        a1.setStateName("SUSPENDED");
        ProposedAction a2 = new ProposedAction("A2");
        a2.setStateName("PROPOSED");
        plan.addAction(a1);
        plan.addAction(a2);

        // Act
        String status = plan.getStatus();

        // Assert
        assertEquals("SUSPENDED", status);
    }

    @Test
    void getStatus_nestedPlans_derivedRecursively() {
        // Arrange
        Plan root = new Plan("Root");
        Plan sub = new Plan("Sub");
        ProposedAction a1 = new ProposedAction("A1");
        a1.setStateName("COMPLETED");
        sub.addAction(a1);
        root.addSubPlan(sub);

        ProposedAction a2 = new ProposedAction("A2");
        a2.setStateName("COMPLETED");
        root.addAction(a2);

        // Act
        String status = root.getStatus();

        // Assert
        assertEquals("COMPLETED", status);
    }

    @Test
    void getTotalAllocatedQuantity_sumsAcrossLeaves() {
        // Arrange
        Plan plan = new Plan("Plan");
        ResourceType cement = new ResourceType("Cement", ResourceType.ResourceKind.CONSUMABLE, "kg");
        cement.setId(1L);

        ProposedAction a1 = new ProposedAction("A1");
        ResourceAllocation alloc1 = new ResourceAllocation(cement, new BigDecimal("10"), ResourceAllocation.AllocationKind.GENERAL);
        a1.addAllocation(alloc1);

        ProposedAction a2 = new ProposedAction("A2");
        ResourceAllocation alloc2 = new ResourceAllocation(cement, new BigDecimal("5"), ResourceAllocation.AllocationKind.GENERAL);
        a2.addAllocation(alloc2);

        plan.addAction(a1);
        plan.addAction(a2);

        // Act
        BigDecimal total = plan.getTotalAllocatedQuantity(cement);

        // Assert
        assertEquals(0, new BigDecimal("15").compareTo(total));
    }

    // =========================================================================
    // ITERATOR PATTERN TESTS
    // =========================================================================

    @Test
    void depthFirstIterator_traversesInCorrectOrder() {
        // Arrange
        Plan root = new Plan("Root");
        root.setId(1L);
        Plan sub = new Plan("Sub");
        sub.setId(2L);
        ProposedAction a1 = new ProposedAction("A1");
        a1.setId(3L);
        ProposedAction a2 = new ProposedAction("A2");
        a2.setId(4L);
        ProposedAction a3 = new ProposedAction("A3");
        a3.setId(5L);

        sub.addAction(a1);
        sub.addAction(a2);
        root.addSubPlan(sub);
        root.addAction(a3);

        // Act
        DepthFirstPlanIterator iterator = new DepthFirstPlanIterator(root);
        java.util.List<String> names = new java.util.ArrayList<>();
        while (iterator.hasNext()) {
            names.add(iterator.next().getName());
        }

        // Assert: Root → Sub → A1 → A2 → A3
        assertEquals(List.of("Root", "Sub", "A1", "A2", "A3"), names);
    }

    @Test
    void depthFirstIterator_emptyPlan_onlyRoot() {
        // Arrange
        Plan root = new Plan("Empty");

        // Act
        DepthFirstPlanIterator iterator = new DepthFirstPlanIterator(root);
        java.util.List<String> names = new java.util.ArrayList<>();
        while (iterator.hasNext()) {
            names.add(iterator.next().getName());
        }

        // Assert
        assertEquals(List.of("Empty"), names);
    }

    @Test
    void depthFirstIterator_singleLeaf() {
        // Arrange
        ProposedAction leaf = new ProposedAction("Leaf");

        // Act
        DepthFirstPlanIterator iterator = new DepthFirstPlanIterator(leaf);

        // Assert
        assertTrue(iterator.hasNext());
        assertEquals("Leaf", iterator.next().getName());
        assertFalse(iterator.hasNext());
    }

    // =========================================================================
    // TEMPLATE METHOD TESTS
    // =========================================================================

    @Test
    void consumableLedgerEntryGenerator_selectsOnlyConsumables() {
        // Arrange
        ConsumableLedgerEntryGenerator generator = new ConsumableLedgerEntryGenerator(fixedClock);

        ResourceType consumable = new ResourceType("Cement", ResourceType.ResourceKind.CONSUMABLE, "kg");
        consumable.setId(1L);
        Account poolAccount = new Account("Pool - Cement", Account.AccountKind.POOL);
        poolAccount.setId(1L);
        consumable.setPoolAccount(poolAccount);

        ResourceType asset = new ResourceType("Crane", ResourceType.ResourceKind.ASSET, "unit");
        asset.setId(2L);
        Account assetPool = new Account("Pool - Crane", Account.AccountKind.POOL);
        assetPool.setId(2L);
        asset.setPoolAccount(assetPool);

        ProposedAction action = new ProposedAction("Build Wall");
        action.addAllocation(new ResourceAllocation(consumable, new BigDecimal("100"), ResourceAllocation.AllocationKind.GENERAL));
        action.addAllocation(new ResourceAllocation(asset, new BigDecimal("1"), ResourceAllocation.AllocationKind.SPECIFIC));

        ImplementedAction impl = new ImplementedAction(action, java.time.LocalDateTime.now(fixedClock));

        // Act
        Transaction tx = generator.generateEntries(impl);

        // Assert: only 2 entries (1 withdrawal + 1 deposit for the consumable; asset ignored)
        assertEquals(2, tx.getEntries().size());
    }

    @Test
    void consumableLedgerEntryGenerator_balancedEntries() {
        // Arrange
        ConsumableLedgerEntryGenerator generator = new ConsumableLedgerEntryGenerator(fixedClock);

        ResourceType consumable = new ResourceType("Fuel", ResourceType.ResourceKind.CONSUMABLE, "litre");
        consumable.setId(1L);
        Account poolAccount = new Account("Pool - Fuel", Account.AccountKind.POOL);
        poolAccount.setId(1L);
        consumable.setPoolAccount(poolAccount);

        ProposedAction action = new ProposedAction("Transport");
        action.addAllocation(new ResourceAllocation(consumable, new BigDecimal("50"), ResourceAllocation.AllocationKind.GENERAL));

        ImplementedAction impl = new ImplementedAction(action, java.time.LocalDateTime.now(fixedClock));

        // Act
        Transaction tx = generator.generateEntries(impl);

        // Assert: entries sum to zero (conservation)
        BigDecimal net = tx.getEntries().stream()
                .map(e -> e.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, BigDecimal.ZERO.compareTo(net));
    }

    @Test
    void consumableLedgerEntryGenerator_zeroQuantity_throwsValidationError() {
        // Arrange
        ConsumableLedgerEntryGenerator generator = new ConsumableLedgerEntryGenerator(fixedClock);

        ResourceType consumable = new ResourceType("Nails", ResourceType.ResourceKind.CONSUMABLE, "box");
        consumable.setId(1L);
        Account poolAccount = new Account("Pool - Nails", Account.AccountKind.POOL);
        poolAccount.setId(1L);
        consumable.setPoolAccount(poolAccount);

        ProposedAction action = new ProposedAction("Nail something");
        action.addAllocation(new ResourceAllocation(consumable, BigDecimal.ZERO, ResourceAllocation.AllocationKind.GENERAL));

        ImplementedAction impl = new ImplementedAction(action, java.time.LocalDateTime.now(fixedClock));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> generator.generateEntries(impl));
    }

    // =========================================================================
    // ACTION STATE MACHINE TESTS
    // =========================================================================

    @Test
    void actionStateMachine_resolvesValidStates() {
        // Arrange
        ActionStateMachine machine = new ActionStateMachine(
                new ProposedState(), new SuspendedState(), new InProgressState(),
                new CompletedState(), new AbandonedState());

        // Act & Assert
        assertEquals("PROPOSED", machine.resolve("PROPOSED").name());
        assertEquals("IN_PROGRESS", machine.resolve("IN_PROGRESS").name());
        assertEquals("COMPLETED", machine.resolve("COMPLETED").name());
        assertEquals("SUSPENDED", machine.resolve("SUSPENDED").name());
        assertEquals("ABANDONED", machine.resolve("ABANDONED").name());
    }

    @Test
    void actionStateMachine_unknownState_throws() {
        // Arrange
        ActionStateMachine machine = new ActionStateMachine(
                new ProposedState(), new SuspendedState(), new InProgressState(),
                new CompletedState(), new AbandonedState());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> machine.resolve("UNKNOWN"));
    }
}