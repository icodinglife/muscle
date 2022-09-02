package com.macho.muscle.core.exception;

public class ActorPublishFailedException extends RuntimeException {
    public ActorPublishFailedException(String actorId, String msg) {
        super(String.format("actor[%s] publish failed, %s.", actorId, msg));
    }
}
