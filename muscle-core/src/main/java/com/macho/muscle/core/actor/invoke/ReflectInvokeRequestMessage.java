package com.macho.muscle.core.actor.invoke;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReflectInvokeRequestMessage implements IReflectInvokeMessage {
    private Long id;
    private String methodName;
    private Object[] args;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public InvokeMessageType getType() {
        return InvokeMessageType.REQUEST;
    }
}
