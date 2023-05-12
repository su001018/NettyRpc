package org.example.client.config;

import lombok.Data;


@Data
public class ClientProperties {
    //序列化
    private Integer serialization;
    //注册中心地址
    private String registryAddress="127.0.0.1:2181";
    //处理超时时间
    private Integer timeout;

}
