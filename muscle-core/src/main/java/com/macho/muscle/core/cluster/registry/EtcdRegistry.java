package com.macho.muscle.core.cluster.registry;

import com.google.common.collect.Lists;
import com.macho.muscle.core.cluster.ServiceInfo;
import com.macho.muscle.core.utils.JsonUtil;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import org.apache.commons.collections4.CollectionUtils;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class EtcdRegistry implements Registry {
    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
    private Client client;

    public EtcdRegistry(String targetEtcdAddr) {
        client = Client.builder()
                .target(targetEtcdAddr)
                .build();
    }

    @Override
    public CompletableFuture<Boolean> registry(String key, ServiceInfo serviceInfo, long leaseSeconds) {
        CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();

        String value = JsonUtil.toJsonString(serviceInfo);

        ByteSequence keyByteSeq = ByteSequence.from(key, CHARSET_UTF8);
        ByteSequence valueByteSeq = ByteSequence.from(value, CHARSET_UTF8);

        CompletableFuture<LeaseGrantResponse> leaseGrantFuture = client.getLeaseClient().grant(leaseSeconds);
        leaseGrantFuture.whenComplete((leaseResult, leaseErr) -> {
            if (leaseErr != null) {
                resultFuture.completeExceptionally(leaseErr);
                return;
            }
            if (leaseResult == null) {
                resultFuture.completeExceptionally(new IllegalStateException("create etcd lease failed."));
                return;
            }

            PutOption putOption = PutOption.newBuilder()
                    .withLeaseId(leaseResult.getID())
                    .build();

            CompletableFuture<PutResponse> put = client.getKVClient().put(keyByteSeq, valueByteSeq, putOption);
            put.whenComplete((putResult, putErr) -> {
                if (putErr != null) {
                    resultFuture.completeExceptionally(putErr);
                    return;
                }

                resultFuture.complete(Boolean.TRUE);
            });

        });

        return resultFuture;
    }

    @Override
    public CompletableFuture<List<ServiceInfo>> getServicesWithName(String keyPrefix) {
        CompletableFuture<List<ServiceInfo>> resultFuture = new CompletableFuture<>();

        GetOption getOption = GetOption.newBuilder()
                .isPrefix(true)
                .build();

        ByteSequence keyPrefixByteSeq = ByteSequence.from(keyPrefix, CHARSET_UTF8);

        CompletableFuture<GetResponse> getFuture = client.getKVClient().get(keyPrefixByteSeq, getOption);
        getFuture.whenComplete((getResult, getErr) -> {
            if (getErr != null) {
                resultFuture.completeExceptionally(getErr);
                return;
            }

            List<ServiceInfo> resultServiceInfoList = Lists.newArrayList();

            if (getResult != null && CollectionUtils.isNotEmpty(getResult.getKvs())) {
                List<KeyValue> kvs = getResult.getKvs();
                for (KeyValue kv : kvs) {
                    ServiceInfo serviceInfo = JsonUtil.fromJsonString(
                            kv.getValue().toString(CHARSET_UTF8), ServiceInfo.class);

                    resultServiceInfoList.add(serviceInfo);
                }
            }

            resultFuture.complete(resultServiceInfoList);
        });

        return resultFuture;
    }

    @Override
    public void watchPrefix(String keyPrefix, Consumer<List<ServiceInfo>> onAddCallback, Consumer<List<ServiceInfo>> onRemoveCallback) {
        ByteSequence keyPrefixByteSeq = ByteSequence.from(keyPrefix, CHARSET_UTF8);

        WatchOption watchOption = WatchOption.newBuilder()
                .isPrefix(true)
                .build();

        client.getWatchClient().watch(keyPrefixByteSeq, watchOption, (watchResponse) -> {
            if (watchResponse != null) {
                List<WatchEvent> events = watchResponse.getEvents();
                if (CollectionUtils.isNotEmpty(events)) {
                    List<ServiceInfo> addServiceInfoList = Lists.newArrayList();
                    List<ServiceInfo> removeServiceInfoList = Lists.newArrayList();

                    for (WatchEvent event : events) {
                        ServiceInfo serviceInfo = JsonUtil.fromJsonString(
                                event.getKeyValue().getValue().toString(CHARSET_UTF8),
                                ServiceInfo.class
                        );

                        if (event.getEventType().equals(WatchEvent.EventType.PUT)) {
                            addServiceInfoList.add(serviceInfo);
                        }
                        if (event.getEventType().equals(WatchEvent.EventType.DELETE)) {
                            removeServiceInfoList.add(serviceInfo);
                        }
                    }

                    if (CollectionUtils.isNotEmpty(addServiceInfoList)) {
                        onAddCallback.accept(addServiceInfoList);
                    }
                    if (CollectionUtils.isNotEmpty(removeServiceInfoList)) {
                        onRemoveCallback.accept(removeServiceInfoList);
                    }
                }
            }
        });
    }
}
