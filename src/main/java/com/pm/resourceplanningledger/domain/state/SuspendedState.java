package com.pm.resourceplanningledger.domain.state;

import org.springframework.stereotype.Component;

@Component
public class SuspendedState implements ActionState {

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
        ctx.transitionTo(new ProposedState());
        ctx.getActionManager().onResume(ctx.getProposedAction());
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
        return "SUSPENDED";
    }
}