package org.example.provider.service;

import org.example.api.service.HelloService;
import org.example.server.annotation.RpcService;

@RpcService(interfaceType = HelloService.class,version = "1.0")
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "hello "+name;
    }
}
