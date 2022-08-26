package com.macho.muscle.core.actor;

import lombok.Data;

@Data
public class ActorRef {
    private final ActorInfo actorInfo;
    private final ActorSystem actorSystem;

    public <M> void send(ActorRef sourceActorRef, M message) {
        UserActorMessage<M> actorMessage = UserActorMessage.<M>builder()
                .sourceActorRef(sourceActorRef)
                .targetActorRef(this)
                .data(message)
                .build();

        actorSystem.dispatch(actorMessage);
    }
}
