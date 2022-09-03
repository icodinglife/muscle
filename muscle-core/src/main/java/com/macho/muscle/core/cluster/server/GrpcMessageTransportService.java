//package com.macho.muscle.core.cluster.server;
//
//import com.macho.muscle.core.actor.ActorInfo;
//import com.macho.muscle.core.actor.ActorRef;
//import com.macho.muscle.core.actor.ActorSystem;
//import com.macho.muscle.core.actor.UserActorMessage;
//import com.macho.muscle.core.cluster.node.NodeInfo;
//import com.macho.muscle.core.cluster.proto.MuscleTransferGrpc;
//import com.macho.muscle.core.cluster.proto.TransferMessage;
//import com.macho.muscle.core.cluster.proto.TransferMessageRequest;
//import com.macho.muscle.core.cluster.proto.TransferMessageResponse;
//import com.macho.muscle.core.utils.KryoUtil;
//import io.grpc.stub.StreamObserver;
//
//public class GrpcMessageTransportService extends MuscleTransferGrpc.MuscleTransferImplBase {
//    private final ActorSystem actorSystem;
//
//    public GrpcMessageTransportService(ActorSystem actorSystem) {
//        this.actorSystem = actorSystem;
//    }
//
//    @Override
//    public void transfer(TransferMessageRequest request, StreamObserver<TransferMessageResponse> responseObserver) {
//        TransferMessage message = request.getMessage();
//
//        UserActorMessage<Object> userActorMessage = UserActorMessage.builder()
//                .sourceActorRef(new ActorRef(ActorInfo.builder()
//                        .id(message.getSourceActorInfo().getId())
//                        .service(message.getSourceActorInfo().getService())
//                        .nodeInfo(NodeInfo.builder()
//                                .host(message.getSourceActorInfo().getNodeInfo().getHost())
//                                .port(message.getSourceActorInfo().getNodeInfo().getPort())
//                                .build()
//                        )
//                        .build(),
//                        actorSystem,
//                        true)
//                )
//                .targetActorRef(new ActorRef(ActorInfo.builder()
//                        .id(message.getTargetActorInfo().getId())
//                        .service(message.getTargetActorInfo().getService())
//                        .nodeInfo(NodeInfo.builder()
//                                .host(message.getTargetActorInfo().getNodeInfo().getHost())
//                                .port(message.getTargetActorInfo().getNodeInfo().getPort())
//                                .build()
//                        )
//                        .build(),
//                        actorSystem,
//                        false)
//                )
//                .data(KryoUtil.deserialize(message.getData().toByteArray()))
//                .build();
//
//        actorSystem.dispatchFromRemote(userActorMessage);
//    }
//}
