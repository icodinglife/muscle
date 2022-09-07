package com.macho.muscle.core.actor;

import com.macho.muscle.core.exception.ActorIsStoppedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ActorContainer<T extends ActorLifecycle> implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ActorContainer.class);

    private final MuscleSystem muscleSystem;

    private final ActorRef selfRef;
    private volatile ActorStatusEnum actorStatus = ActorStatusEnum.CREATING;
    private final BlockingQueue<ActorTask<T>> tasks;

    private final T target;

    public ActorContainer(MuscleSystem muscleSystem, ActorRef actorRef, T target, int taskQueueSize) {
        this.muscleSystem = muscleSystem;
        this.selfRef = actorRef;
        this.target = target;
        this.tasks = new LinkedBlockingQueue<>(taskQueueSize);
    }

    T getTarget() {
        return target;
    }

    public ActorRef getSelfRef() {
        return selfRef;
    }

    public ActorStatusEnum getActorStatus() {
        return actorStatus;
    }

    public MuscleSystem getMuscleSystem() {
        return muscleSystem;
    }

    @Override
    public void run() {
        while (true) {
            try {
                ActorTask<T> task = tasks.take();

                task.accept(this);

                if (actorStatus.equals(ActorStatusEnum.STOPPED)) {
                    cancelAllTasks();
                    return;
                }
            } catch (Throwable e) {
                target.onException(e);
            }
        }
    }

    private void cancelAllTasks() {
        ActorTask task = tasks.poll();
        while (task != null) {
            task.cancel();

            task = tasks.poll();
        }
    }

    public boolean schedule(ActorTask task) {
        if (actorStatus.equals(ActorStatusEnum.STOPPED)) {
            throw new ActorIsStoppedException(selfRef.getActorInfo().getId());
        }

        try {
            tasks.put(task);
            return true;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void start() {
        this.actorStatus = ActorStatusEnum.RUNNING;
    }

    void stop() {
        this.actorStatus = ActorStatusEnum.STOPPED;

        muscleSystem.removeActor(selfRef.getActorInfo().getId());
    }
}
