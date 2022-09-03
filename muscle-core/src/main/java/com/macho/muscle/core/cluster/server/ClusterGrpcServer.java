//package com.macho.muscle.core.cluster.server;
//
//import com.macho.muscle.core.actor.ActorSystem;
//import com.macho.muscle.core.cluster.node.NodeInfo;
//import io.grpc.Server;
//import io.grpc.ServerBuilder;
//import lombok.extern.slf4j.Slf4j;
//
//import java.io.IOException;
//
//@Slf4j
//public class ClusterGrpcServer {
//    private final NodeInfo nodeInfo;
//    private final GrpcMessageTransportService grpcMessageTransportService;
//    private Server server;
//
//    public ClusterGrpcServer(NodeInfo nodeInfo, ActorSystem actorSystem) {
//        this.nodeInfo = nodeInfo;
//        this.grpcMessageTransportService = new GrpcMessageTransportService(actorSystem);
//    }
//
//    public Server start() throws IOException {
//        this.server = ServerBuilder.forPort(nodeInfo.getPort())
//                .addService(grpcMessageTransportService)
//                .build();
//
//        this.server.start();
//
//        return this.server;
//    }
//
//    public void awaitTermination() throws InterruptedException {
//        if (server == null) {
//            throw new IllegalStateException("grpc server not initialized");
//        }
//
//        this.server.awaitTermination();
//    }
//}
