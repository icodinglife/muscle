package com.macho.muscle.core.cluster;

public class NodeInfoConvertor {
    public static NodeInfo convertFrom(com.macho.muscle.core.cluster.proto.NodeInfo nodeInfo) {
        return new NodeInfo(nodeInfo.getHost(), nodeInfo.getPort());
    }
}
