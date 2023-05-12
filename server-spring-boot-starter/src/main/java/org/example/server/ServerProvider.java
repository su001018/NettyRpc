package org.example.server;


import org.example.common.protocol.ServiceDetails;
import org.example.common.register.ServiceRegister;
import org.example.server.annotation.RpcService;
import org.example.server.cache.LocalCache;
import org.example.server.config.ServerProperties;
import org.example.server.transport.Server;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;


import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerProvider implements BeanPostProcessor, CommandLineRunner {
    private ServerProperties serverProperties;
    private Server server;
    private ServiceRegister serviceRegister;

    public ServerProvider(){}
    public ServerProvider(ServerProperties serverProperties,Server server,ServiceRegister serviceRegister){
        this.serverProperties=serverProperties;
        this.server=server;
        this.serviceRegister=serviceRegister;
    }

    @Override
    //bean初始化完成之后处理带有RpcService注解的bean
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//        System.out.println("==================postProcessAfterInitialization===================");
        RpcService rpcService=bean.getClass().getAnnotation(RpcService.class);
        if(rpcService!=null){
            try {
                String serviceName=rpcService.interfaceType().getName()+":"+rpcService.version();
                LocalCache.store(serviceName, bean);

                ServiceDetails serviceDetails=new ServiceDetails();
                serviceDetails.setServiceName(serviceName);
                serviceDetails.setAddress(InetAddress.getLocalHost().getHostAddress());
                serviceDetails.setPort(serverProperties.getPort());
                serviceDetails.setVersion(rpcService.version());

                serviceRegister.register(serviceDetails);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        return bean;
    }

    @Override
    //项目启动后立即执行
    public void run(String... args) throws Exception {
        new Thread(()->{
            server.start(serverProperties.getPort());
        }).start();
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                serviceRegister.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }
}
