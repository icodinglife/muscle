package com.macho.muscle.core.exception;

public class ActorMessageTransportException extends RuntimeException {
    public ActorMessageTransportException(String actorId, String message) {
        super(String.format("Send to actor[%s] error, %s", actorId, message));
    }
}
