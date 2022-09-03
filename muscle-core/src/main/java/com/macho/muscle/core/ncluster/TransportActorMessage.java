package com.macho.muscle.core.ncluster;

import com.macho.muscle.core.nactor.ActorInfo;

public class TransportActorMessage {
    private ActorInfo sourceActorInfo;
    private ActorInfo targetActorInfo;
    private String fullMethodName;
    private byte[] data;

    public TransportActorMessage() {
    }

    public TransportActorMessage(ActorInfo sourceActorInfo, ActorInfo targetActorInfo, String fullMethodName, byte[] data) {
        this.sourceActorInfo = sourceActorInfo;
        this.targetActorInfo = targetActorInfo;
        this.fullMethodName = fullMethodName;
        this.data = data;
    }

    public ActorInfo getSourceActorInfo() {
        return sourceActorInfo;
    }

    public void setSourceActorInfo(ActorInfo sourceActorInfo) {
        this.sourceActorInfo = sourceActorInfo;
    }

    public ActorInfo getTargetActorInfo() {
        return targetActorInfo;
    }

    public void setTargetActorInfo(ActorInfo targetActorInfo) {
        this.targetActorInfo = targetActorInfo;
    }

    public String getFullMethodName() {
        return fullMethodName;
    }

    public void setFullMethodName(String fullMethodName) {
        this.fullMethodName = fullMethodName;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
