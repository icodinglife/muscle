package com.macho.muscle.core.actor;

import com.macho.muscle.core.node.NodeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActorInfo {
    private String id;
    private String service;
    private NodeInfo nodeInfo;

    /**
     * /service/id@node:port
     */
    public String getActorPath() {
        return String.format("/%s/%s@%s", service, id, nodeInfo.getNodeInfo());
    }
}
