package com.macho.muscle.core.cluster;

import java.util.concurrent.CompletableFuture;

public class ClusterNode {
    private final NodeInfo nodeInfo;
    private final MessageTransport messageTransport;

    public ClusterNode(NodeInfo nodeInfo, MessageTransport messageTransport) {
        this.nodeInfo = nodeInfo;
        this.messageTransport = messageTransport;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void disconnect() {
        messageTransport.disconnect();
    }

    public CompletableFuture<Object> transferRemoteMessage(TransportActorMessage transportActorMessage) {
        return messageTransport.transfer(transportActorMessage);
    }
}
