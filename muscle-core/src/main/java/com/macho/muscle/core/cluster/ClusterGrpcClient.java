package com.macho.muscle.core.cluster;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.macho.muscle.core.cluster.proto.*;
import com.macho.muscle.core.exception.ActorMessageTransportException;
import com.macho.muscle.core.utils.KryoUtil;
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
    public CompletableFuture<Object> transfer(TransportActorMessage message) {
        TransferMessageRequest transferMessageRequest = TransferMessageRequest.newBuilder()
                .setMessage(TransferMessage.newBuilder()
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
                        .setFullMethodName(message.getFullMethodName())
                        .setData(ByteString.copyFrom(message.getData()))
                        .build())
                .build();

        CompletableFuture<Object> resultFuture = new CompletableFuture<>();

        ListenableFuture<TransferMessageResponse> transferFuture = muscleTransferFutureStub.transfer(transferMessageRequest);
        transferFuture.addListener(() -> {
            String errMsg = null;
            try {
                TransferMessageResponse transferMessageResponse = transferFuture.get();
                if (transferMessageResponse.getCode().equals(TransferMessageResponse.ResponseCode.SUCCESS)) {

                    resultFuture.complete(KryoUtil.deserialize(transferMessageResponse.getData().toByteArray()));

                } else {
                    resultFuture.completeExceptionally(KryoUtil.deserialize(transferMessageResponse.getData().toByteArray()));
                }

                return;
            } catch (Exception e) {
                errMsg = e.getMessage();
            }

            resultFuture.completeExceptionally(
                    new ActorMessageTransportException(message.getTargetActorInfo().toString(), errMsg));
        }, MoreExecutors.directExecutor());

        return resultFuture;
    }

    @Override
    public void disconnect() {
        channel.shutdown();
    }
}
