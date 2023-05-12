package org.example.common.protocol;

import lombok.Data;

@Data
public class ServiceDetails {
    //服务名称
    String serviceName;
    //版本号
    String version;
    //服务地址
    String address;
    //服务端口号
    Integer port;
}
