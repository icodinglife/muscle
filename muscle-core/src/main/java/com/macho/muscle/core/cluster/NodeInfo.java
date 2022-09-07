package com.macho.muscle.core.cluster;

import java.util.Objects;

public class NodeInfo {
    private String host;
    private int port;

    public NodeInfo() {
    }

    public NodeInfo(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeInfo nodeInfo)) return false;
        return getPort() == nodeInfo.getPort() && getHost().equals(nodeInfo.getHost());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHost(), getPort());
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
