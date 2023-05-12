package org.example.client.proxy;


import org.example.client.config.ClientProperties;
import org.example.client.transport.ClientFactory;
import org.example.client.transport.ClientTransport;
import org.example.client.transport.RequestData;
import org.example.common.discovery.ServiceDetailsDiscovery;
import org.example.common.protocol.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;

public class ClientStubInvocationHandler implements InvocationHandler {
    private ServiceDetailsDiscovery serviceDetailsDiscovery;
    private Class<?> clazz;
    private String version;
    private ClientProperties clientProperties;

    public ClientStubInvocationHandler(ServiceDetailsDiscovery serviceDetailsDiscovery,Class<?> clazz,
                                       String version,ClientProperties clientProperties){
        this.serviceDetailsDiscovery=serviceDetailsDiscovery;
        this.clazz=clazz;
        this.version=version;
        this.clientProperties=clientProperties;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String serviceName= clazz.getName()+":"+version;
        ServiceDetails serviceDetails=serviceDetailsDiscovery.discovery(serviceName);
        if(serviceDetails==null){
            throw new Exception("服务未找到");
        }
        ClientTransport clientTransport= ClientFactory.getClientTransport();
        Message<MessageRequest> message=new Message<>();
        MessageHeader messageHeader=new MessageHeader(clientProperties.getSerialization().byteValue(), MessageType.REQUEST.getType());
        MessageRequest request=new MessageRequest();
        request.setServiceName(serviceName);
        request.setMethod(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        message.setMessageHeader(messageHeader);
        message.setMessageBody(request);

        RequestData requestData=new RequestData();
        requestData.setRequestMessage(message);
        requestData.setAddress(serviceDetails.getAddress());
        requestData.setPort(serviceDetails.getPort());
        requestData.setTimeout(clientProperties.getTimeout());

        Message<MessageResponse>responseMessage=clientTransport.sendMessage(requestData);
        if(responseMessage==null){
            throw new Exception("请求超时");
        }
        if(responseMessage.getMessageHeader().getStatus()!=MessageStatus.SUCCESS.getStatus()){
            throw new Exception(responseMessage.getMessageBody().getMessage());
        }
        return responseMessage.getMessageBody().getData();
    }
}
