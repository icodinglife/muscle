package com.macho.muscle.core.actor;

import com.macho.muscle.core.cluster.ClusterSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class KeepAliveTask<T extends ActorLifecycle> implements ActorTask<T> {
    private static final Logger logger = LoggerFactory.getLogger(KeepAliveTask.class);

    private final long leaseId;
    private final long lastSeconds;

    public KeepAliveTask(long leaseId) {
        this.leaseId = leaseId;
        this.lastSeconds = currentSeconds();
    }

    @Override
    public void accept(ActorContainer<T> actorContainer) {
        if (actorContainer.getActorStatus().equals(ActorStatusEnum.STOPPED)) {
            return;
        }

        MuscleSystem muscleSystem = actorContainer.getMuscleSystem();
        ClusterSystem clusterSystem = muscleSystem.getClusterSystem();
        String actorId = actorContainer.getSelfRef().getActorInfo().getId();

        long currentSeconds = currentSeconds();
        if (currentSeconds - lastSeconds > ClusterSystem.LEASE_SECONDS) {
            PublishTask<T> publishTask = new PublishTask<>();
            publishTask.accept(actorContainer);

            return;
        }

        CompletableFuture<Long> longCompletableFuture = clusterSystem.keepAlive(leaseId);

        longCompletableFuture.whenComplete((res, err) -> {
            if (err != null) {
                rePublish(actorContainer);

                return;
            }

            muscleSystem.schedule((timeout) -> {
                        try {
                            KeepAliveTask<T> keepAliveTask = new KeepAliveTask<>(res);
                            if (!muscleSystem.dispatch(actorId, keepAliveTask)) {
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

    private void rePublish(ActorContainer<T> actorContainer) {
        String actorId = actorContainer.getSelfRef().getActorInfo().getId();
        PublishTask<T> publishTask = new PublishTask<>();
        if (!actorContainer.getMuscleSystem().dispatch(actorId, publishTask)) {
            logger.error("actor[{}] republish error!", actorId);
        }
    }

    private long currentSeconds() {
        return System.currentTimeMillis() / 1000L;
    }
}
