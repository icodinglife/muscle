package com.macho.muscle.core.actor.invoke;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReflectInvokeRequestMessage implements IReflectInvokeMessage {
    private final Long id;
    private final String methodName;
    private final Object[] args;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public InvokeMessageType getType() {
        return InvokeMessageType.REQUEST;
    }
}
