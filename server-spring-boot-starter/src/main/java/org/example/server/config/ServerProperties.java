package org.example.server.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rpc.server")
public class ServerProperties {
    //服务名称
    String serviceName;
    //服务启动端口
    int port=8081;
    //注册中心地址
    String registryAddress;


}
