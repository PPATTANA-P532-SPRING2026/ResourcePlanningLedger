package com.pm.resourceplanningledger.domain.state;

import com.pm.resourceplanningledger.domain.operational.ProposedAction;
import com.pm.resourceplanningledger.manager.ActionManager;

public class ActionContext {

    private final ProposedAction proposedAction;
    private final ActionManager actionManager;

    public ActionContext(ProposedAction proposedAction, ActionManager actionManager) {
        this.proposedAction = proposedAction;
        this.actionManager = actionManager;
    }

    public ProposedAction getProposedAction() {
        return proposedAction;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public void transitionTo(ActionState newState) {
        proposedAction.setStateName(newState.name());
    }
}