package com.macho.muscle.core.actor;

public interface ActorLifecycle {
    void onException(Throwable e);

    void onStart();

    void onStop();
}
