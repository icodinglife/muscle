package com.macho.muscle.core.nactor;

import com.macho.muscle.core.actor.ActorLifecycle;
import com.macho.muscle.core.actor.ActorStatusEnum;
import com.macho.muscle.core.ncluster.ClusterSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PublishTask<T extends ActorLifecycle> implements ActorTask<T> {
    private static final Logger logger = LoggerFactory.getLogger(PublishTask.class);

    @Override
    public void accept(ActorContainer<T> actorContainer) {
        if (actorContainer.getActorStatus().equals(ActorStatusEnum.STOPPED)) {
            return;
        }

        ActorRef selfRef = actorContainer.getSelfRef();
        ActorInfo actorInfo = selfRef.getActorInfo();
        MuscleSystem muscleSystem = actorContainer.getMuscleSystem();
        ClusterSystem clusterSystem = muscleSystem.getClusterSystem();
        String actorId = actorInfo.getId();

        CompletableFuture<Long> publishFuture = clusterSystem.publishActor(actorInfo);
        publishFuture.whenComplete((res, err) -> {
            if (err != null) {
                ExceptionInvokeTask<T> invokeTask = new ExceptionInvokeTask<>(err);
                if (!muscleSystem.dispatch(actorId, invokeTask)) {
                    logger.error(String.format("actor[%s] publish error!", actorId), err);
                }

                return;
            }

            muscleSystem.schedule((timeout) -> {
                        try {
                            KeepAliveTask<T> keepAliveTask = new KeepAliveTask<>(res);
                            if (!muscleSystem.dispatch(actorInfo.getId(), keepAliveTask)) {
                                logger.warn(String.format("actor[%s] keep alive error, maybe stopped!", actorId));
                            }
                        } catch (Exception e) {
                            logger.error(String.format("actor[%s] keep alive error!", actorId), e);
                        }
                    },
                    ClusterSystem.LEASE_SECONDS / 2,
                    TimeUnit.SECONDS
            );
        });
    }
}
