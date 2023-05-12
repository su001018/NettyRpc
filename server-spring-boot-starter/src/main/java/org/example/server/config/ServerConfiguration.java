package org.example.server.config;


import org.example.common.register.ServiceRegister;
import org.example.common.register.ZookeeperServiceRegister;
import org.example.server.ServerProvider;
import org.example.server.transport.NettyServer;
import org.example.server.transport.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ServerProperties.class)
public class ServerConfiguration {
    @Autowired
    ServerProperties serverProperties;

    @Bean
    @ConditionalOnProperty(prefix = "rpc.server",name = "register_type",havingValue = "zookeeper",matchIfMissing = true)
    @ConditionalOnMissingBean(ServiceRegister.class)
    public ServiceRegister getZookeeperServiceRegister() throws Exception {
        System.out.println("===============getZookeeperServiceRegister==================");
        return new ZookeeperServiceRegister(serverProperties.getRegistryAddress());
    }

    @Bean
    @ConditionalOnProperty(prefix = "rpc.server",name = "type",havingValue = "netty",matchIfMissing = true)
    @ConditionalOnMissingBean(Server.class)
    public Server getNettyServer(){
        return new NettyServer();
    }

    @Bean
    @ConditionalOnMissingBean(ServerProvider.class)
    public ServerProvider getServerProvider(
            @Autowired ServerProperties serverProperties,
            @Autowired Server server,
            @Autowired ServiceRegister serviceRegister
    ){
        System.out.println("==================getServerProvider===================");
        return new ServerProvider(serverProperties,server,serviceRegister);
    }

}
