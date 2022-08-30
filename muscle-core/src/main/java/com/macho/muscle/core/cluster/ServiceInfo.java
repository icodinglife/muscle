package com.macho.muscle.core.cluster;

import com.macho.muscle.core.cluster.node.NodeInfo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceInfo {
    private final String servicePath;
    private final String serviceName;
    private final NodeInfo nodeInfo;
}
