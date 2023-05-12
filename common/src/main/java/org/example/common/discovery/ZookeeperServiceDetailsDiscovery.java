package org.example.common.discovery;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ProviderStrategy;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RandomStrategy;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;
import org.example.common.balancer.LoadBalanceType;
import org.example.common.protocol.ServiceDetails;
import org.springframework.beans.factory.annotation.Value;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZookeeperServiceDetailsDiscovery implements ServiceDetailsDiscovery{

    //Curator的服务发现接口，用来查询服务
    private ServiceDiscovery<ServiceDetails>serviceDiscovery;
    //Curator的客户端
    private CuratorFramework client;
    //负载均衡策略
    @Value("${common.load_balance}")
    private int loadBalance=0;
    //根目录
    @Value("${common.zookeeper.root_path}")
    private final String ROOT_PATH="services";
    //需要关闭的列表
    private List<Closeable>closeableList=new ArrayList<>();

    public ZookeeperServiceDetailsDiscovery(){}
    //构造函数
    public ZookeeperServiceDetailsDiscovery(String address) throws Exception {

        //客户端
        client= CuratorFrameworkFactory.newClient(address,new ExponentialBackoffRetry(1000,3));
        client.start();
        closeableList.add(client);

        this.serviceDiscovery= ServiceDiscoveryBuilder.builder(ServiceDetails.class)
                .client(client)
                .basePath(ROOT_PATH)
                .serializer(new JsonInstanceSerializer<ServiceDetails>(ServiceDetails.class))
                .build();
        serviceDiscovery.start();
        closeableList.add(serviceDiscovery);
    }


    @Override
    public ServiceDetails discovery(String serviceName) throws Exception {
        ProviderStrategy<ServiceDetails> strategy=null;
        switch (LoadBalanceType.findType(loadBalance)){
            case RANDOM -> {
                strategy=new RandomStrategy<ServiceDetails>();
                break;
            }
            case ROUND_ROBIN -> {
                strategy=new RoundRobinStrategy<ServiceDetails>();
                break;
            }
        }
        ServiceProvider<ServiceDetails>provider=serviceDiscovery.serviceProviderBuilder().serviceName(serviceName)
                .providerStrategy(strategy).build();
        provider.start();
        closeableList.add(provider);
        ServiceDetails serviceDetails= provider.getInstance().getPayload();
        return serviceDetails;
    }

    //关闭client、provider和discovery
    @Override
    public void close() throws IOException {
        synchronized (this) {
            for (Closeable closeable : closeableList) {
                CloseableUtils.closeQuietly(closeable);
            }
            closeableList.clear();
        }
    }
}
