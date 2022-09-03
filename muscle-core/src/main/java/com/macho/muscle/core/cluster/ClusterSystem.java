package com.macho.muscle.core.cluster;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.macho.muscle.core.cluster.registry.EtcdRegistry;
import com.macho.muscle.core.cluster.registry.Registry;
import com.macho.muscle.core.actor.ActorInfo;
import com.macho.muscle.core.actor.MuscleSystem;
import com.macho.muscle.core.utils.NetUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ClusterSystem {
    private static final Logger logger = LoggerFactory.getLogger(ClusterSystem.class);

    private static final String SERVICE_PREFIX = "/muscle/services/";
    public static final long LEASE_SECONDS = 60L;

    private final Registry registry;
    private final NodeInfo nodeInfo;

    private ClusterGrpcServer clusterServer;

    private final Map<String, Map<String, ClusterNode>> remoteServiceNameToClusterNodesMap = Maps.newConcurrentMap();

    public ClusterSystem(String etcdAddr, int localNodePort) {
        this.registry = new EtcdRegistry(etcdAddr);
        try {
            this.nodeInfo = new NodeInfo(NetUtil.selfIpAddr(), localNodePort);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void start(MuscleSystem muscleSystem) throws IOException {
        this.clusterServer = new ClusterGrpcServer(this.nodeInfo, muscleSystem);

        this.clusterServer.start();

        fetchRemoteActors();
    }

    public List<String> getActorIdsWithService(String serviceName) {
        Map<String, ClusterNode> actorIdToClusterNodeMap = remoteServiceNameToClusterNodesMap.get(serviceName);

        return Lists.newArrayList(actorIdToClusterNodeMap.keySet());
    }

    public ClusterNode getClusterNodeWithServiceAndActorId(String service, String actorId) {
        Map<String, ClusterNode> actorIdToClusterNodeMap = remoteServiceNameToClusterNodesMap.get(service);
        if (MapUtils.isNotEmpty(actorIdToClusterNodeMap)) {
            return actorIdToClusterNodeMap.get(actorId);
        }
        return null;
    }

    public CompletableFuture<Long> publishActor(ActorInfo actorInfo) {
        checkClusterServer();

        if (actorInfo == null || StringUtils.isBlank(actorInfo.getId())) {
            throw new IllegalArgumentException("actor id can't be null or blank");
        }

        String actorPath = actorInfo.getActorPath(SERVICE_PREFIX);

        return registry.registry(actorPath, actorInfo, LEASE_SECONDS);
    }

    public CompletableFuture<Long> keepAlive(long leaseId) {
        checkClusterServer();

        CompletableFuture<Long> keepAliveFuture = registry.keepAlive(leaseId);

        return keepAliveFuture;
    }

    private void checkClusterServer() {
        if (clusterServer == null) {
            throw new IllegalStateException("ClusterServer not started");
        }
    }

    private void fetchRemoteActors() {
        registry.watchPrefix(SERVICE_PREFIX,
                (addActorInfoList) -> {
                    setupRemoteActors(addActorInfoList);
                },
                (removeActorInfoList) -> {
                    removeRemoteActors(removeActorInfoList);
                });

        CompletableFuture<List<ActorInfo>> servicesWithName = registry.getActorsWithName(SERVICE_PREFIX);
        servicesWithName.whenComplete((actorInfoList, err) -> {
            if (err != null) {
                logger.error("get service info from registry error", err);
                return;
            }

            setupRemoteActors(actorInfoList);
        });
    }

    private void removeRemoteActors(List<ActorInfo> actorInfoList) {
        if (CollectionUtils.isNotEmpty(actorInfoList)) {
            for (ActorInfo actorInfo : actorInfoList) {
                if (remoteServiceNameToClusterNodesMap.containsKey(actorInfo.getService())) {
                    Map<String, ClusterNode> actorIdToClusterNodeMap = remoteServiceNameToClusterNodesMap.get(actorInfo.getService());
                    if (actorIdToClusterNodeMap != null) {
                        ClusterNode clusterNode = actorIdToClusterNodeMap.remove(actorInfo.getId());
                        if (clusterNode != null) {
                            clusterNode.disconnect();
                        }
                    }
                }
            }
        }
    }

    private void setupRemoteActors(List<ActorInfo> actorInfoList) {
        if (CollectionUtils.isNotEmpty(actorInfoList)) {
            for (ActorInfo actorInfo : actorInfoList) {
                Map<String, ClusterNode> actorIdToClusterNodeMap = remoteServiceNameToClusterNodesMap
                        .computeIfAbsent(actorInfo.getService(), (k) -> Maps.newConcurrentMap());

                actorIdToClusterNodeMap.computeIfAbsent(actorInfo.getId(), (k) -> {
                    NodeInfo remoteNodeInfo = actorInfo.getNodeInfo();

                    ClusterGrpcClient client = new ClusterGrpcClient(remoteNodeInfo);

                    return new ClusterNode(remoteNodeInfo, client);
                });
            }
        }
    }
}
