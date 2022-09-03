package com.macho.muscle.core.actor;

import com.macho.muscle.core.cluster.NodeInfo;
import org.apache.commons.lang3.StringUtils;

public class ActorInfo {
    private String id;
    private String service;
    private NodeInfo nodeInfo;

    public ActorInfo() {
    }

    public ActorInfo(String id, String service, NodeInfo nodeInfo) {
        this.id = id;
        this.service = service;
        this.nodeInfo = nodeInfo;
    }

    /**
     * /prefix/service/id@node:port
     */
    public String getActorPath(String servicePrefix) {
        return String.format("%s%s/%s@%s:%d",
                servicePrefix,
                StringUtils.firstNonBlank(getService(), "-"),
                getId(),
                getNodeInfo().getHost(),
                getNodeInfo().getPort()
        );
    }

    public String getId() {
        return id;
    }

    public String getService() {
        return service;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setNodeInfo(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    @Override
    public String toString() {
        return "ActorInfo{" +
                "id='" + id + '\'' +
                ", service='" + service + '\'' +
                ", nodeInfo=" + nodeInfo +
                '}';
    }
}
