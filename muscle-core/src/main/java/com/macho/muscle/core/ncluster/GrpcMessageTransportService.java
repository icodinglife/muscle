package com.macho.muscle.core.ncluster;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import com.macho.muscle.core.cluster.proto.MuscleTransferGrpc;
import com.macho.muscle.core.cluster.proto.TransferMessage;
import com.macho.muscle.core.cluster.proto.TransferMessageRequest;
import com.macho.muscle.core.cluster.proto.TransferMessageResponse;
import com.macho.muscle.core.nactor.ActorInfo;
import com.macho.muscle.core.nactor.ActorRef;
import com.macho.muscle.core.nactor.MuscleSystem;
import com.macho.muscle.core.nactor.RemoteInvokeTask;
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
            TransferMessageResponse transferMessageResponse = TransferMessageResponse.newBuilder()
                    .setCode(err != null ? TransferMessageResponse.ResponseCode.FAILURE : TransferMessageResponse.ResponseCode.SUCCESS)
                    .setData(res != null ? ByteString.copyFrom(KryoUtil.serialize(res)) : null)
                    .setException(err != null ? ByteString.copyFrom(KryoUtil.serialize(err)) : null)
                    .build();

            responseObserver.onNext(transferMessageResponse);
            responseObserver.onCompleted();
        });
    }
}
