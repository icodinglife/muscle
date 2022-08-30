package com.macho.muscle.core.cluster.node;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class NodeInfo {
    private final String host;
    private final int port;

    /**
     * host:port
     */
    public String getNodeHostPort() {
        return String.format("%s:%d", host, port);
    }
}