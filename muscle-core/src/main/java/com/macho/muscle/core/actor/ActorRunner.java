package com.macho.muscle.core.actor;

import com.macho.muscle.core.exception.ActorIsStoppedException;
import lombok.extern.slf4j.Slf4j;
import org.jctools.queues.MpscArrayQueue;

import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public abstract class ActorRunner implements Runnable {
    private static final ThreadLocal<ActorRunner> currentActor = new ThreadLocal<>();

    private final ActorSystem actorSystem;

    private final ActorRef actorRef;

    private final Executor executor;

    private final Queue<ActorMessage<?>> queue;

    private AtomicReference<ActorStatusEnum> actorStatus = new AtomicReference<>(ActorStatusEnum.CREATING);

    private AtomicReference<RunningStatus> runningStatus = new AtomicReference<>(RunningStatus.SUSPENDED);

    public ActorRunner(ActorRef actorRef, int queueCapacity, Executor executor, ActorSystem actorSystem) {
        this.actorRef = actorRef;
        this.executor = executor;
        this.actorSystem = actorSystem;

        queue = new MpscArrayQueue<>(queueCapacity);
    }

    public void stop() {
        actorStatus.set(ActorStatusEnum.STOPPED);

        try {
            onStop();
        } catch (Exception e) {
            try {
                onException(e);
            } catch (Exception ee) {
                log.error("Unpressed exception", ee);
            }
        }

        actorSystem.deregisterActor(selfActorRef().getActorInfo().getId());
    }

    public ActorRef selfActorRef() {
        return actorRef;
    }

    public boolean offer(ActorMessage<?> message) {
        if (actorStatus.get() == ActorStatusEnum.STOPPED) {
            throw new ActorIsStoppedException(selfActorRef().getActorInfo().getId());
        }

        boolean success = queue.offer(message);

        if (success) {
            scheduleToRun();
        }

        return success;
    }

    private void scheduleToRun() {
        if (runningStatus.compareAndSet(RunningStatus.SUSPENDED, RunningStatus.RUNNING)) {
            try {
                executor.execute(this);
            } catch (RejectedExecutionException e) {
                log.warn("Actor调度失败，等待延时重新调度", e);
                // todo process rejected exception, add a timer to reschedule
            }
        }
    }

    private void suspendSelf() {
        runningStatus.set(RunningStatus.SUSPENDED);
    }

    void processMessage(ActorMessage<?> actorMessage) {
        try {
            switch (actorMessage.getMessageType()) {
                case SYSTEM_MESSAGE:

                    processSystemMessage((SystemActorMessage) actorMessage);
                    break;
                case USER_MESSAGE:

                    this.onRecvMessage((UserActorMessage<?>) actorMessage);
                    break;
            }
        } catch (Exception e) {
            onException(e);
        }
    }

    private void processSystemMessage(SystemActorMessage systemActorMessage) {
        switch (systemActorMessage) {
            case START_MESSAGE:
                actorStatus.set(ActorStatusEnum.RUNNING);

                try {
                    onStart();
                } catch (Exception e) {
                    try {
                        onException(e);
                    } catch (Exception ee) {
                        log.error("Unprocessed exception", ee);
                    }
                }
                break;
            case STOP_MESSAGE:

                stop();
                break;
            default:
                throw new IllegalStateException("Unexpected SystemActorMessage value: " + systemActorMessage);
        }
    }

    private void setCurrentActor(ActorRunner actorRunner) {
        currentActor.set(actorRunner);
    }

    public static ActorRunner currentActor() {
        return currentActor.get();
    }

    @Override
    public void run() {
        setCurrentActor(this);

        // todo move a mailbox out ？
        while (true) {
            if (actorStatus.get() == ActorStatusEnum.STOPPED) {
                rejectUnpressedMessages(queue);
                break;
            }

            ActorMessage actorMessage = queue.poll();

            if (actorMessage == null) {
                break;
            }

            processMessage(actorMessage);
        }

        setCurrentActor(null);

        suspendSelf();

        ActorMessage hasMoreMessage = queue.peek();
        if (hasMoreMessage != null) {
            scheduleToRun();
        }
    }

    public void rejectUnpressedMessages(Queue<ActorMessage<?>> queue) {
        // todo send to dead letter
        queue.clear();
    }

    protected abstract void onRecvMessage(UserActorMessage<?> message);

    protected abstract void onException(Exception e);

    protected abstract void onStart();

    protected abstract void onStop();

    private enum RunningStatus {
        RUNNING,
        SUSPENDED,
    }
}
