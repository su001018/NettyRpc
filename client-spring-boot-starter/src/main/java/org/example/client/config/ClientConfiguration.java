package org.example.client.config;


import org.example.client.processor.ClientProcessor;
import org.example.client.proxy.ClientStubProxyFactory;
import org.example.common.discovery.ServiceDetailsDiscovery;
import org.example.common.discovery.ZookeeperServiceDetailsDiscovery;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ClientConfiguration {

    @Bean
    public ClientProperties clientProperties(Environment environment) {
        BindResult<ClientProperties> result = Binder.get(environment).bind("rpc.client", ClientProperties.class);
        return result.get();
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientStubProxyFactory getClientStubProxyFactory(){
        return new ClientStubProxyFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rpc.client",name = "discovery_type",havingValue = "zookeeper",matchIfMissing = true)
    public ServiceDetailsDiscovery getServiceDetailsDiscovery(
            @Autowired ClientProperties clientProperties
    ) throws Exception {
        return new ZookeeperServiceDetailsDiscovery(clientProperties.getRegistryAddress());
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientProcessor getClientProcessor(
            @Autowired ClientStubProxyFactory clientStubProxyFactory,
            @Autowired ServiceDetailsDiscovery serviceDetailsDiscovery,
            @Autowired ClientProperties clientProperties
    ){
        System.out.println("===============getClientProcessor================");
        return new ClientProcessor(clientStubProxyFactory, serviceDetailsDiscovery, clientProperties);
    }


}
