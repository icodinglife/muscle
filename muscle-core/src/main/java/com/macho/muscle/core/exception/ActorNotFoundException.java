package com.macho.muscle.core.exception;

public class ActorNotFoundException extends RuntimeException {
    private final String actorId;

    public ActorNotFoundException(String actorId) {
        super(String.format("Actor[%s] not found, or it's already stopped.", actorId));
        this.actorId = actorId;
    }

    public String getActorId() {
        return actorId;
    }
}
