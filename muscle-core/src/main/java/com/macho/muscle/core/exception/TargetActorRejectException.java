package com.macho.muscle.core.exception;

public class TargetActorRejectException extends RuntimeException {
    private final String targetActorId;

    public TargetActorRejectException(String targetActorId) {
        super(String.format("Target actor[%s]'s queue is full, reject the message", targetActorId));

        this.targetActorId = targetActorId;
    }

    public String getTargetActorId() {
        return targetActorId;
    }
}
