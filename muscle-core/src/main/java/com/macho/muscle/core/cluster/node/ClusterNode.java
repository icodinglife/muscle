package com.macho.muscle.core.cluster.node;

import com.macho.muscle.core.cluster.transport.MessageTransport;
import com.macho.muscle.core.cluster.transport.TransportActorMessage;

import java.util.concurrent.CompletableFuture;

/**
 * watch/alive
 */
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

    public CompletableFuture<Void> transferRemoteMessage(TransportActorMessage transportActorMessage) {
        return messageTransport.transfer(transportActorMessage);
    }
}
