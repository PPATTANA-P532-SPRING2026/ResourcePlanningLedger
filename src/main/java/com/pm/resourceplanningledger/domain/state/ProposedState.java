package com.pm.resourceplanningledger.domain.state;

import org.springframework.stereotype.Component;

@Component
public class ProposedState implements ActionState {

    @Override
    public void implement(ActionContext ctx) {
        ctx.transitionTo(new InProgressState());
        ctx.getActionManager().onImplement(ctx.getProposedAction());
    }

    @Override
    public void suspend(ActionContext ctx, String reason) {
        ctx.transitionTo(new SuspendedState());
        ctx.getActionManager().onSuspend(ctx.getProposedAction(), reason);
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
        ctx.transitionTo(new AbandonedState());
    }

    @Override
    public String name() {
        return "PROPOSED";
    }
}