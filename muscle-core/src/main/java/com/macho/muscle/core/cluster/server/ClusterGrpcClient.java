package com.macho.muscle.core.cluster.server;

import com.google.protobuf.ByteString;
import com.macho.muscle.core.actor.ActorMessage;
import com.macho.muscle.core.cluster.node.NodeInfo;
import com.macho.muscle.core.cluster.proto.ActorInfo;
import com.macho.muscle.core.cluster.proto.MuscleTransferGrpc;
import com.macho.muscle.core.cluster.proto.TransferMessage;
import com.macho.muscle.core.cluster.proto.TransferMessageRequest;
import com.macho.muscle.core.cluster.transport.MessageTransport;
import com.macho.muscle.core.cluster.transport.TransportActorMessage;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.CompletableFuture;

public class ClusterGrpcClient implements MessageTransport {
    private final ManagedChannel channel;
    private final MuscleTransferGrpc.MuscleTransferFutureStub muscleTransferFutureStub;

    public ClusterGrpcClient(NodeInfo nodeInfo) {
        channel = ManagedChannelBuilder.forAddress(nodeInfo.getHost(), nodeInfo.getPort())
                .usePlaintext()
                .build();

        muscleTransferFutureStub = MuscleTransferGrpc.newFutureStub(channel);
    }

    @Override
    public CompletableFuture<Void> transfer(TransportActorMessage message) {
        TransferMessageRequest transferMessageRequest = TransferMessageRequest.newBuilder()
                .setMessage(TransferMessage.newBuilder()
                        .setType(message.getType())
                        .setSourceActorInfo(ActorInfo.newBuilder()
                                .setId(message.getSourceActorInfo().getId())
                                .setService(message.getSourceActorInfo().getService())
                                .setNodeInfo(com.macho.muscle.core.cluster.proto.NodeInfo.newBuilder()
                                        .setHost(message.getSourceActorInfo().getNodeInfo().getHost())
                                        .setPort(message.getSourceActorInfo().getNodeInfo().getPort())
                                        .build())
                                .build())
                        .setTargetActorInfo(ActorInfo.newBuilder()
                                .setId(message.getTargetActorInfo().getId())
                                .setService(message.getTargetActorInfo().getService())
                                .setNodeInfo(com.macho.muscle.core.cluster.proto.NodeInfo.newBuilder()
                                        .setHost(message.getTargetActorInfo().getNodeInfo().getHost())
                                        .setPort(message.getTargetActorInfo().getNodeInfo().getPort())
                                        .build())
                                .build())
                        .setData(ByteString.copyFrom(message.getData()))
                        .build())
                .build();

        muscleTransferFutureStub.transfer(transferMessageRequest);

        // todo process result future
        return null;
    }

    @Override
    public void disconnect() {
        channel.shutdown();
    }
}
