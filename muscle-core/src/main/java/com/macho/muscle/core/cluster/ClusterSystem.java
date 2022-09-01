package com.macho.muscle.core.cluster;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.macho.muscle.core.actor.ActorInfo;
import com.macho.muscle.core.actor.ActorSystem;
import com.macho.muscle.core.cluster.node.ClusterNode;
import com.macho.muscle.core.cluster.node.NodeInfo;
import com.macho.muscle.core.cluster.registry.EtcdRegistry;
import com.macho.muscle.core.cluster.registry.Registry;
import com.macho.muscle.core.cluster.server.ClusterGrpcClient;
import com.macho.muscle.core.cluster.server.ClusterGrpcServer;
import com.macho.muscle.core.utils.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.*;

@Slf4j
public class ClusterSystem {
    private static final long LEASE_SECONDS = 60L;
    private static final String SERVICE_PREFIX = "/muscle/services/";
    private final Registry registry;
    private final NodeInfo nodeInfo;
    private final ClusterGrpcServer clusterServer;

    private final ScheduledExecutorService scheduledExecutorService = newSingleThreadScheduledExecutor();

    private final Map<String, Map<String, ClusterNode>> remoteServiceNameToClusterNodesMap = Maps.newConcurrentMap();
    private final Map<String, ActorInfo> localActorPathToActorInfoMap = Maps.newConcurrentMap();

    public ClusterSystem(String etcdAddr, int nodePort, ActorSystem actorSystem) {
        try {
            this.registry = new EtcdRegistry(etcdAddr);

            this.nodeInfo = NodeInfo.builder()
                    .host(NetUtil.selfIpAddr())
                    .port(nodePort)
                    .build();

            this.clusterServer = new ClusterGrpcServer(this.nodeInfo, actorSystem);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        scheduledExecutorService.scheduleAtFixedRate(
                this::keepAllServiceAlive,
                LEASE_SECONDS / 2,
                LEASE_SECONDS / 2,
                TimeUnit.SECONDS);
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void start() {
        if (clusterServer == null) {
            throw new IllegalStateException("ClusterServer not initialized");
        }

        try {
            clusterServer.start();

            fetchRemoteActors();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void awaitTermination() throws InterruptedException {
        checkClusterServer();

        clusterServer.awaitTermination();
    }

    public void registerActor(ActorInfo actorInfo) {
        if (actorInfo == null || StringUtils.isBlank(actorInfo.getId())) {
            throw new IllegalArgumentException("actor id can't be null or blank");
        }

        String actorPath = wrapActorPath(actorInfo);

        localActorPathToActorInfoMap.putIfAbsent(actorPath, actorInfo);

        registry.registry(actorPath, actorInfo, LEASE_SECONDS);
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
                log.error("get service info from registry error", err);
                return;
            }

            setupRemoteActors(actorInfoList);
        });
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

    private void checkClusterServer() {
        if (clusterServer == null) {
            throw new IllegalStateException("ClusterServer not initialized");
        }
    }

    private String wrapActorPath(ActorInfo actorInfo) {
        return actorInfo.getActorPath(SERVICE_PREFIX);
    }

    // todo modify alive, should be lease from actor
    private void keepAllServiceAlive() {
        if (MapUtils.isNotEmpty(localActorPathToActorInfoMap)) {
            for (Map.Entry<String, ActorInfo> entry : localActorPathToActorInfoMap.entrySet()) {
                registry.registry(entry.getKey(), entry.getValue(), LEASE_SECONDS);
            }
        }
    }
}
