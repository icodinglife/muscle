package com.macho.muscle.core.cluster.server;

import com.macho.muscle.core.cluster.proto.MuscleTransferGrpc;
import com.macho.muscle.core.cluster.proto.TransferMessageRequest;
import com.macho.muscle.core.cluster.proto.TransferMessageResponse;
import io.grpc.stub.StreamObserver;

public class GrpcMessageTransportService extends MuscleTransferGrpc.MuscleTransferImplBase {
    @Override
    public void transfer(TransferMessageRequest request, StreamObserver<TransferMessageResponse> responseObserver) {
        super.transfer(request, responseObserver);
    }
}
