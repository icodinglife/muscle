package com.macho.muscle.core.cluster;

import com.google.common.collect.Maps;
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

    private final Map<String, ClusterNode> remoteServicePathToClusterNodeMap = Maps.newConcurrentMap();
    private final Map<String, ServiceInfo> localServicePathToServiceInfoMap = Maps.newConcurrentMap();

    public ClusterSystem(String etcdAddr, int nodePort) {
        try {
            this.registry = new EtcdRegistry(etcdAddr);

            this.nodeInfo = NodeInfo.builder()
                    .host(NetUtil.selfIpAddr())
                    .port(nodePort)
                    .build();

            this.clusterServer = new ClusterGrpcServer(this.nodeInfo);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        scheduledExecutorService.scheduleAtFixedRate(
                this::keepAllServiceAlive,
                LEASE_SECONDS / 2,
                LEASE_SECONDS / 2,
                TimeUnit.SECONDS);
    }

    public void start() {
        if (clusterServer == null) {
            throw new IllegalStateException("ClusterServer not initialized");
        }

        try {
            clusterServer.start();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void awaitTermination() throws InterruptedException {
        checkClusterServer();

        clusterServer.awaitTermination();
    }

    public void fetchRemoteServices() {
        registry.watchPrefix(SERVICE_PREFIX,
                (addServiceInfoList) -> {
                    setupRemoteServices(addServiceInfoList);
                },
                (removeServiceInfoList) -> {
                    // todo
                });

        CompletableFuture<List<ServiceInfo>> servicesWithName = registry.getServicesWithName(SERVICE_PREFIX);
        servicesWithName.whenComplete((serviceInfoList, err) -> {
            if (err != null) {
                log.error("get service info from registry error", err);
                return;
            }

            setupRemoteServices(serviceInfoList);
        });
    }

    private void removeRemoteServices(List<ServiceInfo> serviceInfoList) {
        if (CollectionUtils.isNotEmpty(serviceInfoList)) {
            for (ServiceInfo serviceInfo : serviceInfoList) {
                ClusterNode clusterNode = remoteServicePathToClusterNodeMap.remove(serviceInfo.getServicePath());
                if (clusterNode != null) {
                    clusterNode.disconnect();
                }
            }
        }
    }

    private void setupRemoteServices(List<ServiceInfo> serviceInfoList) {
        if (CollectionUtils.isNotEmpty(serviceInfoList)) {
            for (ServiceInfo serviceInfo : serviceInfoList) {
                remoteServicePathToClusterNodeMap.computeIfAbsent(
                        serviceInfo.getServicePath(),
                        (k) -> {
                            NodeInfo remoteNodeInfo = serviceInfo.getNodeInfo();

                            ClusterGrpcClient client = new ClusterGrpcClient(remoteNodeInfo);

                            return new ClusterNode(remoteNodeInfo, client);
                        }
                );
            }
        }
    }

    private void checkClusterServer() {
        if (clusterServer == null) {
            throw new IllegalStateException("ClusterServer not initialized");
        }
    }

    public void registerService(String serviceName) {
        if (StringUtils.isBlank(serviceName)) {
            throw new IllegalArgumentException("service name can't be null or blank");
        }

        String servicePath = wrapServicePath(serviceName);

        ServiceInfo serviceInfo = localServicePathToServiceInfoMap.computeIfAbsent(
                servicePath,
                (k) -> ServiceInfo.builder()
                        .servicePath(servicePath)
                        .serviceName(serviceName)
                        .nodeInfo(this.nodeInfo)
                        .build());

        registry.registry(servicePath, serviceInfo, LEASE_SECONDS);
    }

    private String wrapServicePath(String serviceName) {
        return String.format("%s%s@%s:%d", SERVICE_PREFIX, serviceName, this.nodeInfo.getHost(), this.nodeInfo.getPort());
    }

    private void keepAllServiceAlive() {
        if (MapUtils.isNotEmpty(localServicePathToServiceInfoMap)) {
            for (Map.Entry<String, ServiceInfo> entry : localServicePathToServiceInfoMap.entrySet()) {
                registry.registry(entry.getKey(), entry.getValue(), LEASE_SECONDS);
            }
        }
    }
}
