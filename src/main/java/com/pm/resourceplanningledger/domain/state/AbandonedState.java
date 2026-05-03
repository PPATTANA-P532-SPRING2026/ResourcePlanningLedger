package com.pm.resourceplanningledger.domain.state;

import org.springframework.stereotype.Component;

@Component
public class AbandonedState implements ActionState {

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
        throw new IllegalStateTransitionException(name(), "approve");
    }

    @Override
    public void reject(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "reject");
    }

    @Override
    public void reopen(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "reopen");
    }

    @Override
    public String name() {
        return "ABANDONED";
    }
}