package com.macho.muscle.core.actor.invoke;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReflectInvokeResponseMessage implements IReflectInvokeMessage {
    private final Long id;
    private final Object result;
    private final Throwable exception;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public InvokeMessageType getType() {
        return InvokeMessageType.RESPONSE;
    }
}
