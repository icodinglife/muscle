package com.macho.muscle.core.cluster.node;

import com.macho.muscle.core.cluster.transport.MessageTransport;

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

    public void disconnect() {
        messageTransport.disconnect();
    }
}
