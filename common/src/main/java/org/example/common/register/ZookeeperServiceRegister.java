package org.example.common.register;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.example.common.protocol.ServiceDetails;
import org.springframework.beans.factory.annotation.Value;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class ZookeeperServiceRegister implements ServiceRegister{

    //根目录
    @Value("${common.zookeeper.root_path}")
    private final String ROOT_PATH="services";

    private ServiceDiscovery<ServiceDetails>serviceDiscovery;
    private CuratorFramework client;

    //关闭zookeeper应用
    private List<Closeable>closeableList=new ArrayList<>();

    public ZookeeperServiceRegister(String address) throws Exception {
        client= CuratorFrameworkFactory.newClient(address,
                new ExponentialBackoffRetry(1000,3));
        client.start();
        closeableList.add(client);
        serviceDiscovery= ServiceDiscoveryBuilder.builder(ServiceDetails.class)
                .client(client)
                .basePath(ROOT_PATH)
                .serializer(new JsonInstanceSerializer<>(ServiceDetails.class))
                .build();
        serviceDiscovery.start();
        closeableList.add(serviceDiscovery);
    }
    @Override
    public void register(ServiceDetails serviceDetails) throws Exception {
        ServiceInstance<ServiceDetails>serviceInstance=ServiceInstance.<ServiceDetails>builder()
                .address(serviceDetails.getAddress())
                .port(serviceDetails.getPort())
                .name(serviceDetails.getServiceName())
                .payload(serviceDetails)
                .build();
        serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public void unRegister(ServiceDetails serviceDetails) throws Exception {
        ServiceInstance<ServiceDetails>serviceInstance=ServiceInstance.<ServiceDetails>builder()
                .address(serviceDetails.getAddress())
                .port(serviceDetails.getPort())
                .name(serviceDetails.getServiceName())
                .payload(serviceDetails)
                .build();
        serviceDiscovery.unregisterService(serviceInstance);
    }


    @Override
    public void close() throws Exception {
        synchronized (this){
            for(Closeable closeable:closeableList){
                CloseableUtils.closeQuietly(closeable);
            }
            closeableList.clear();
        }

    }
}
