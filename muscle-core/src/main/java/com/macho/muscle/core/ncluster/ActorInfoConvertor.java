package com.macho.muscle.core.ncluster;

import com.macho.muscle.core.nactor.ActorInfo;

public class ActorInfoConvertor {
    public static ActorInfo convertFrom(com.macho.muscle.core.cluster.proto.ActorInfo actorInfo) {
        return new ActorInfo(actorInfo.getId(), actorInfo.getService(), NodeInfoConvertor.convertFrom(actorInfo.getNodeInfo()));
    }
}
