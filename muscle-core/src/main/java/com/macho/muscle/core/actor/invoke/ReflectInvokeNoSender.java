package com.macho.muscle.core.actor.invoke;

import com.macho.muscle.core.actor.ActorLifecycle;
import com.macho.muscle.core.actor.ActorRef;
import com.macho.muscle.core.actor.ActorSystem;

import java.util.concurrent.Executor;

public class ReflectInvokeNoSender extends ReflectInvokeActorRunner<ActorLifecycle> {
    private static final InnerActor innerActor = new InnerActor();

    public ReflectInvokeNoSender(ActorRef actorRef, int queueCapacity, Executor executor, ActorSystem actorSystem) {
        super(actorRef, innerActor, queueCapacity, executor, actorSystem);
    }

    @Override
    protected void processRequestMessage(ActorRef sourceActorRef, ReflectInvokeRequestMessage reflectInvokeRequestMessage) {
        // todo process dead letter invoke
    }

    private static final class InnerActor implements ActorLifecycle {
        @Override
        public void onException(Exception e) {

        }

        @Override
        public void onStart() {

        }

        @Override
        public void onStop() {

        }
    }
}
