package org.example.server.annotation;


import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface RpcService {
    //服务接口类型
    Class<?> interfaceType() default Object.class;
    //版本
    String version() default "1.0";

}
