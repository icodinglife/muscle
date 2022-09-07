package com.macho.muscle.core.cluster;

import com.macho.muscle.core.actor.MuscleSystem;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ClusterGrpcServer {
    private static final Logger logger = LoggerFactory.getLogger(ClusterGrpcServer.class);

    private final NodeInfo nodeInfo;
    private final GrpcMessageTransportService grpcMessageTransportService;
    private Server server;

    public ClusterGrpcServer(NodeInfo nodeInfo, MuscleSystem muscleSystem) {
        this.nodeInfo = nodeInfo;
        this.grpcMessageTransportService = new GrpcMessageTransportService(muscleSystem);
    }

    public Server start() throws IOException {
        this.server = ServerBuilder.forPort(nodeInfo.getPort())
                .addService(grpcMessageTransportService)
                .build();

        this.server.start();

        return this.server;
    }

    public void awaitTermination() throws InterruptedException {
        if (server == null) {
            throw new IllegalStateException("grpc server not initialized");
        }

        this.server.awaitTermination();
    }
}
