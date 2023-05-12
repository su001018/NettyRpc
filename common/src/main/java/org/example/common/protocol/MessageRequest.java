package org.example.common.protocol;

import lombok.Data;

import java.io.Serializable;

@Data
public class MessageRequest implements Serializable {
    //服务名称
    private String ServiceName;
    //调用方法名
    private String method;
    //方法参数类型
    Class<?>[] parameterTypes;
    //方法参数
    Object[] parameters;

}
