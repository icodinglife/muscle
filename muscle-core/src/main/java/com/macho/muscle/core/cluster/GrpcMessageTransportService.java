package com.macho.muscle.core.cluster;

import com.google.protobuf.ByteString;
import com.macho.muscle.core.cluster.proto.MuscleTransferGrpc;
import com.macho.muscle.core.cluster.proto.TransferMessage;
import com.macho.muscle.core.cluster.proto.TransferMessageRequest;
import com.macho.muscle.core.cluster.proto.TransferMessageResponse;
import com.macho.muscle.core.actor.ActorInfo;
import com.macho.muscle.core.actor.ActorRef;
import com.macho.muscle.core.actor.MuscleSystem;
import com.macho.muscle.core.actor.RemoteInvokeTask;
import com.macho.muscle.core.utils.KryoUtil;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CompletableFuture;

public class GrpcMessageTransportService extends MuscleTransferGrpc.MuscleTransferImplBase {
    private final MuscleSystem muscleSystem;

    public GrpcMessageTransportService(MuscleSystem muscleSystem) {
        this.muscleSystem = muscleSystem;
    }

    @Override
    public void transfer(TransferMessageRequest request, StreamObserver<TransferMessageResponse> responseObserver) {
        TransferMessage message = request.getMessage();

        ActorInfo targetActorInfo = ActorInfoConvertor.convertFrom(message.getTargetActorInfo());
        String fullMethodName = message.getFullMethodName();
        Object[] args = KryoUtil.deserialize(message.getData().toByteArray());

        CompletableFuture<Object> future = new CompletableFuture<>();

        ActorRef targetActorRef = muscleSystem.buildActorRef(targetActorInfo);
        RemoteInvokeTask invokeTask = new RemoteInvokeTask(targetActorRef, fullMethodName, args, future);
        muscleSystem.dispatch(targetActorInfo.getId(), invokeTask);

        future.whenComplete((res, err) -> {
            try {
                TransferMessageResponse transferMessageResponse = TransferMessageResponse.newBuilder()
                        .setCode(err != null ? TransferMessageResponse.ResponseCode.FAILURE : TransferMessageResponse.ResponseCode.SUCCESS)
                        .setData(res != null ? ByteString.copyFrom(KryoUtil.serialize(res)) : ByteString.EMPTY)
                        .setException(err != null ? ByteString.copyFrom(KryoUtil.serialize(err)) : ByteString.EMPTY)
                        .build();

                responseObserver.onNext(transferMessageResponse);
            } catch (Throwable e) {
                responseObserver.onError(e);
            }

            responseObserver.onCompleted();
        });
    }
}
