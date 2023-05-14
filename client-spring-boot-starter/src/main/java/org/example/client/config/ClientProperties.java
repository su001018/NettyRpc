package org.example.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
public class ClientProperties {
    //序列化
    Integer serialization;
    //注册中心地址
    String registryAddress;
    //处理超时时间
    Integer timeout;

}
