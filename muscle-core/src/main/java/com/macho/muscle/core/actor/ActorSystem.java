package com.macho.muscle.core.actor;

import com.google.common.collect.Maps;
import com.macho.muscle.core.exception.ActorIsStoppedException;
import com.macho.muscle.core.exception.ActorNotFoundException;
import com.macho.muscle.core.exception.TargetActorRejectException;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

public class ActorSystem {

    private final String systemName;
    private final Executor executor;

    private final Map<String, ActorRunner> actorIdToActorRunnerMap = Maps.newConcurrentMap();

    public ActorSystem(String systemName, int parallelism) {
        this.systemName = systemName;
        this.executor = new ForkJoinPool(parallelism);

        init();
    }

    public Executor getExecutor() {
        return executor;
    }

    private void init() {

    }

    public ActorRunner registerActor(String id, String serviceName, Function<ActorRef, ActorRunner> actorBuilder) {
        return actorIdToActorRunnerMap.computeIfAbsent(id, k -> {
            ActorRef actorRef = buildActorRef(id, serviceName);

            ActorRunner actorRunner = actorBuilder.apply(actorRef);

            actorRunner.offer(SystemActorMessage.START_MESSAGE);

            return actorRunner;
        });
    }

    void deregisterActor(String id) {
        actorIdToActorRunnerMap.remove(id);
    }

    protected ActorRef buildActorRef(String id, String serviceName) {
        ActorInfo actorInfo = ActorInfo.builder()
                .id(id)
                .service(serviceName)
                .build();

        return new ActorRef(actorInfo, this);
    }

    public <T> void dispatch(UserActorMessage<T> actorMessage) {
        dispatch(actorMessage.getTargetActorRef(), actorMessage);
    }

    private void dispatch(ActorRef targetActorRef, ActorMessage actorMessage) {
        String targetActorId = targetActorRef.getActorInfo().getId();

        ActorRunner targetActorRunner = actorIdToActorRunnerMap.get(targetActorId);

        if (targetActorRunner == null) {
            throw new ActorNotFoundException(targetActorId);
        }

        boolean offerSuccess = targetActorRunner.offer(actorMessage);
        if (!offerSuccess) {
            throw new TargetActorRejectException(targetActorId);
        }
    }
}
