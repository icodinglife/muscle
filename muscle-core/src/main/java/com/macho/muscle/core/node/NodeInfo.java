package com.macho.muscle.core.node;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeInfo {
    private String host;
    private int port;

    /**
     * host:port
     */
    public String getNodeInfo() {
        return String.format("%s:%d", host, port);
    }
}