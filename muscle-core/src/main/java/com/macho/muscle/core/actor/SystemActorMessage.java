package com.macho.muscle.core.actor;

public enum SystemActorMessage implements ActorMessage<Void> {
    START_MESSAGE,
    PUBLISH_MESSAGE,
    KEEP_ALIVE_MESSAGE,
    // 停止，不接受新消息，也不执行已接收到的消息
    STOP_MESSAGE,
    ;

    @Override
    public ActorMessageType getMessageType() {
        return ActorMessageType.SYSTEM_MESSAGE;
    }

    @Override
    public Void getData() {
        return null;
    }
}
