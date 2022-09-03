//package com.macho.muscle.core.actor;
//
//import com.macho.muscle.core.cluster.node.NodeInfo;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.apache.commons.lang3.StringUtils;
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class ActorInfo {
//    private String id;
//    private String service;
//    private NodeInfo nodeInfo;
//
//    /**
//     * /prefix/service/id@node:port
//     */
//    public String getActorPath(String servicePrefix) {
//        return String.format("%s%s/%s@%s:%d",
//                servicePrefix,
//                StringUtils.firstNonBlank(getService(), "-"),
//                getId(),
//                getNodeInfo().getHost(),
//                getNodeInfo().getPort()
//        );
//    }
//}
