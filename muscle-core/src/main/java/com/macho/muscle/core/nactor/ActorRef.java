package com.macho.muscle.core.nactor;

public class ActorRef {
    private final ActorInfo actorInfo;

    public ActorRef(ActorInfo actorInfo) {
        this.actorInfo = actorInfo;
    }

    public ActorInfo getActorInfo() {
        return actorInfo;
    }
}
