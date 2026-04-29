package com.pm.resourceplanningledger.domain.state;

import org.springframework.stereotype.Component;

@Component
public class InProgressState implements ActionState {

    @Override
    public void implement(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "implement");
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
        ctx.transitionTo(new CompletedState());
        ctx.getActionManager().onComplete(ctx.getProposedAction());
    }

    @Override
    public void abandon(ActionContext ctx) {
        ctx.transitionTo(new AbandonedState());
    }

    @Override
    public String name() {
        return "IN_PROGRESS";
    }
}