package com.macho.muscle.core.exception;

public class ActorIsStoppedException extends RuntimeException {
    private final String actorId;

    public ActorIsStoppedException(String actorId) {
        super(String.format("Actor[%s] is stopped", actorId));

        this.actorId = actorId;
    }

    public String getActorId() {
        return actorId;
    }
}
