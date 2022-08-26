package com.macho.muscle.core.actor;

public interface ActorLifecycle {
    void onException(Exception e);

    void onStart();

    void onStop();
}
