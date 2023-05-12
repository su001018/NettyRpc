package org.example.client.proxy;


import org.example.client.config.ClientProperties;
import org.example.common.discovery.ServiceDetailsDiscovery;

import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;

public class ClientStubProxyFactory {
    private ConcurrentHashMap<Class<?>, Object>proxyCache=new ConcurrentHashMap<>();

    public <T>T getProxy(ServiceDetailsDiscovery serviceDetailsDiscovery, Class<T>clazz,
                         String version, ClientProperties clientProperties){
        return (T)proxyCache.computeIfAbsent(clazz, clz->Proxy.newProxyInstance(clz.getClassLoader(),
                new Class[]{clz},new ClientStubInvocationHandler(serviceDetailsDiscovery,clz,version,clientProperties)));
    }

}
