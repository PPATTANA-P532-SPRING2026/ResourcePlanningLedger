package com.pm.resourceplanningledger.domain.state;

public interface ActionState {
    void implement(ActionContext ctx);
    void suspend(ActionContext ctx, String reason);
    void resume(ActionContext ctx);
    void complete(ActionContext ctx);
    void abandon(ActionContext ctx);
    void submitForApproval(ActionContext ctx);
    void approve(ActionContext ctx);
    void reject(ActionContext ctx);
    void reopen(ActionContext ctx);
    String name();
}