package org.example.client.processor;

import org.example.client.annotation.RpcAutowired;
import org.example.client.config.ClientProperties;
import org.example.client.proxy.ClientStubProxyFactory;
import org.example.common.discovery.ServiceDetailsDiscovery;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class ClientProcessor implements ApplicationContextAware,BeanFactoryPostProcessor {
    private ClientStubProxyFactory clientStubProxyFactory;
    private ServiceDetailsDiscovery serviceDetailsDiscovery;
    private ClientProperties clientProperties;
    private ApplicationContext applicationContext;

    public ClientProcessor(ClientStubProxyFactory clientStubProxyFactory,ServiceDetailsDiscovery serviceDetailsDiscovery,
                           ClientProperties clientProperties){
        this.clientStubProxyFactory=clientStubProxyFactory;
        this.serviceDetailsDiscovery=serviceDetailsDiscovery;
        this.clientProperties=clientProperties;
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("======================setApplicationContext============================");
        this.applicationContext=applicationContext;
//        for(String beanName: applicationContext.getBeanDefinitionNames()){
//            Object bean=applicationContext.getBean(beanName);
//            for(Field field:bean.getClass().getDeclaredFields()){
//                RpcAutowired rpcAutowired= field.getAnnotation(RpcAutowired.class);
//                if(rpcAutowired!=null){
//                    field.setAccessible(true);
//                    try {
//                        field.set(bean,clientStubProxyFactory.getProxy(
//                                serviceDetailsDiscovery,field.getType(),rpcAutowired.version(),clientProperties));
//                    } catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("postProcessBeanFactory()->applicationContext值为"+applicationContext);
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
