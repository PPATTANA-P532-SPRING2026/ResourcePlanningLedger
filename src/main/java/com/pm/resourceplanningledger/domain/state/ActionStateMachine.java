package com.pm.resourceplanningledger.domain.state;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class ActionStateMachine {

    private final Map<String, ActionState> states;

    public ActionStateMachine(
            ProposedState proposedState,
            SuspendedState suspendedState,
            InProgressState inProgressState,
            CompletedState completedState,
            AbandonedState abandonedState,
            PendingApprovalState pendingApprovalState,
            ReopenedState reopenedState) {
        this.states = Map.of(
                "PROPOSED", proposedState,
                "SUSPENDED", suspendedState,
                "IN_PROGRESS", inProgressState,
                "COMPLETED", completedState,
                "ABANDONED", abandonedState,
                "PENDING_APPROVAL", pendingApprovalState,
                "REOPENED", reopenedState
        );
    }

    public ActionState resolve(String stateName) {
        ActionState state = states.get(stateName);
        if (state == null) {
            throw new IllegalArgumentException("Unknown state: " + stateName);
        }
        return state;
    }
}