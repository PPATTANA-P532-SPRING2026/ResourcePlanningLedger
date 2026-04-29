package com.pm.resourceplanningledger.domain.state;

public class IllegalStateTransitionException extends RuntimeException {
    public IllegalStateTransitionException(String currentState, String event) {
        super("Cannot " + event + " from state " + currentState);
    }
}