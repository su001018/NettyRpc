package org.example.client.processor;

import org.example.client.annotation.RpcAutowired;
import org.example.client.config.ClientProperties;
import org.example.client.proxy.ClientStubProxyFactory;
import org.example.common.discovery.ServiceDetailsDiscovery;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class ClientProcessor implements ApplicationContextAware, BeanFactoryPostProcessor {
    private final ClientStubProxyFactory clientStubProxyFactory;
    private final ServiceDetailsDiscovery serviceDetailsDiscovery;
    private final ClientProperties clientProperties;
    private ApplicationContext applicationContext;

    public ClientProcessor(ClientStubProxyFactory clientStubProxyFactory, ServiceDetailsDiscovery serviceDetailsDiscovery,
                           ClientProperties clientProperties) {
        System.out.println("ClientProcessor Constructor");
        this.clientStubProxyFactory = clientStubProxyFactory;
        this.serviceDetailsDiscovery = serviceDetailsDiscovery;
        this.clientProperties = clientProperties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("ClientProcessor setApplicationContext");
        this.applicationContext = applicationContext;
//        for (String beanName : applicationContext.getBeanDefinitionNames()) {
//            Object bean = applicationContext.getBean(beanName);
//            for (Field field : bean.getClass().getDeclaredFields()) {
//                RpcAutowired rpcAutowired = field.getAnnotation(RpcAutowired.class);
//                if (rpcAutowired != null) {
//                    field.setAccessible(true);
//
//                    ReflectionUtils.setField(field, bean, clientStubProxyFactory.getProxy(
//                            serviceDetailsDiscovery, field.getType(), rpcAutowired.version(), clientProperties));
//
//
//                }
//            }
//        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("ClientProcessor postProcessBeanFactory");
        for(String beanDefinitionName: beanFactory.getBeanDefinitionNames()){
            BeanDefinition beanDefinition=beanFactory.getBeanDefinition(beanDefinitionName);
            String beanClassName=beanDefinition.getBeanClassName();
            if(beanClassName==null)continue;
            Class<?>clazz= ClassUtils.resolveClassName(beanClassName,this.getClass().getClassLoader());
            ReflectionUtils.doWithFields(clazz,field -> {
                RpcAutowired rpcAutowired=field.getAnnotation(RpcAutowired.class);
                if(rpcAutowired!=null){
                    Object bean= applicationContext.getBean(clazz);
                    field.setAccessible(true);
                    ReflectionUtils.setField(field,bean,clientStubProxyFactory.getProxy(
                            serviceDetailsDiscovery,field.getType(),rpcAutowired.version(),clientProperties
                    ));
                }

            });
        }

    }
}
