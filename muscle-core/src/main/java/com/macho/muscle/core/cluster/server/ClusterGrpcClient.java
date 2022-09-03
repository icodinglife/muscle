//package com.macho.muscle.core.cluster.server;
//
//import com.google.common.util.concurrent.ListenableFuture;
//import com.google.common.util.concurrent.MoreExecutors;
//import com.google.protobuf.ByteString;
//import com.macho.muscle.core.cluster.node.NodeInfo;
//import com.macho.muscle.core.cluster.proto.*;
//import com.macho.muscle.core.cluster.transport.MessageTransport;
//import com.macho.muscle.core.cluster.transport.TransportActorMessage;
//import com.macho.muscle.core.exception.ActorMessageTransportException;
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//
//import java.util.concurrent.CompletableFuture;
//
//public class ClusterGrpcClient implements MessageTransport {
//    private final ManagedChannel channel;
//    private final MuscleTransferGrpc.MuscleTransferFutureStub muscleTransferFutureStub;
//
//    public ClusterGrpcClient(NodeInfo nodeInfo) {
//        channel = ManagedChannelBuilder.forAddress(nodeInfo.getHost(), nodeInfo.getPort())
//                .usePlaintext()
//                .build();
//
//        muscleTransferFutureStub = MuscleTransferGrpc.newFutureStub(channel);
//    }
//
//    @Override
//    public CompletableFuture<Void> transfer(TransportActorMessage message) {
//        TransferMessageRequest transferMessageRequest = TransferMessageRequest.newBuilder()
//                .setMessage(TransferMessage.newBuilder()
//                        .setType(message.getType())
//                        .setSourceActorInfo(ActorInfo.newBuilder()
//                                .setId(message.getSourceActorInfo().getId())
//                                .setService(message.getSourceActorInfo().getService())
//                                .setNodeInfo(com.macho.muscle.core.cluster.proto.NodeInfo.newBuilder()
//                                        .setHost(message.getSourceActorInfo().getNodeInfo().getHost())
//                                        .setPort(message.getSourceActorInfo().getNodeInfo().getPort())
//                                        .build())
//                                .build())
//                        .setTargetActorInfo(ActorInfo.newBuilder()
//                                .setId(message.getTargetActorInfo().getId())
//                                .setService(message.getTargetActorInfo().getService())
//                                .setNodeInfo(com.macho.muscle.core.cluster.proto.NodeInfo.newBuilder()
//                                        .setHost(message.getTargetActorInfo().getNodeInfo().getHost())
//                                        .setPort(message.getTargetActorInfo().getNodeInfo().getPort())
//                                        .build())
//                                .build())
//                        .setData(ByteString.copyFrom(message.getData()))
//                        .build())
//                .build();
//
//        CompletableFuture<Void> resultFuture = new CompletableFuture<>();
//
//        ListenableFuture<TransferMessageResponse> transferFuture = muscleTransferFutureStub.transfer(transferMessageRequest);
//        transferFuture.addListener(() -> {
//            String errMsg = null;
//            try {
//                TransferMessageResponse transferMessageResponse = transferFuture.get();
//                if (transferMessageResponse.getCode().equals(TransferMessageResponse.ResponseCode.SUCCESS)) {
//
//                    resultFuture.complete(null);
//
//                    return;
//                } else {
//                    errMsg = transferMessageResponse.getMsg();
//                }
//            } catch (Exception e) {
//                errMsg = e.getMessage();
//            }
//
//            resultFuture.completeExceptionally(
//                    new ActorMessageTransportException(message.getTargetActorInfo().toString(), errMsg));
//        }, MoreExecutors.directExecutor());
//
//        return resultFuture;
//    }
//
//    @Override
//    public void disconnect() {
//        channel.shutdown();
//    }
//}
