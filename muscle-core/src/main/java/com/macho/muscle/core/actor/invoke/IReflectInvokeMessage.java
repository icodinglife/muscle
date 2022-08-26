package com.macho.muscle.core.actor.invoke;

public interface IReflectInvokeMessage {
    Long getId();
    InvokeMessageType getType();

    enum InvokeMessageType {
        REQUEST,
        RESPONSE,
    }
}
