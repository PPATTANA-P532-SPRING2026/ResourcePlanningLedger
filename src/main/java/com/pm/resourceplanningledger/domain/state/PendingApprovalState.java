package com.pm.resourceplanningledger.domain.state;

import org.springframework.stereotype.Component;

@Component
public class PendingApprovalState implements ActionState {

    @Override
    public void implement(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "implement");
    }

    @Override
    public void suspend(ActionContext ctx, String reason) {
        throw new IllegalStateTransitionException(name(), "suspend");
    }

    @Override
    public void resume(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "resume");
    }

    @Override
    public void complete(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "complete");
    }

    @Override
    public void abandon(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "abandon");
    }

    @Override
    public void submitForApproval(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "submitForApproval");
    }

    @Override
    public void approve(ActionContext ctx) {
        ctx.transitionTo(new InProgressState());
        ctx.getActionManager().onApprove(ctx.getProposedAction());
    }

    @Override
    public void reject(ActionContext ctx) {
        ctx.transitionTo(new ProposedState());
        ctx.getActionManager().onReject(ctx.getProposedAction());
    }

    @Override
    public void reopen(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "reopen");
    }

    @Override
    public String name() {
        return "PENDING_APPROVAL";
    }
}