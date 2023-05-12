package org.example.server.handler;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.common.protocol.*;
import org.example.server.cache.LocalCache;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Method;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ServerRequestHandler extends SimpleChannelInboundHandler<Message<MessageRequest>> {
    private ThreadPoolExecutor poolExecutor=new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),Runtime.getRuntime().availableProcessors()*2,60, TimeUnit.SECONDS,new ArrayBlockingQueue<>(10000));
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message<MessageRequest> messageRequestMessage) throws Exception {
        System.out.println("=============ServerRequestHandler============");
        MessageRequest request=messageRequestMessage.getMessageBody();
        //交给线程池执行
        poolExecutor.execute(()->{
            Message<MessageResponse>responseMessage=new Message<>();
            MessageHeader requestHeader=messageRequestMessage.getMessageHeader();
            //构造回应的消息头
            MessageHeader responseHeader=new MessageHeader(
                    requestHeader.getSerialization(),
                    MessageType.RESPONSE.getType(),
                    requestHeader.getId());
            //回应消息体
            MessageResponse responseBody=new MessageResponse();
            try {
                //执行成功
                Object result = handle(request);
                responseHeader.setStatus(MessageStatus.SUCCESS.getStatus());
                responseBody.setData(result);
            } catch (Exception e) {
                //执行出错
                e.printStackTrace();
                responseHeader.setStatus(MessageStatus.ERROR.getStatus());
                responseBody.setMessage(e.getMessage());
            }finally {
                //返回客户端
                responseMessage.setMessageHeader(responseHeader);
                responseMessage.setMessageBody(responseBody);
                channelHandlerContext.writeAndFlush(responseMessage);
            }

        });

    }
    private Object handle(MessageRequest request) throws Exception {
        Object bean= LocalCache.get(request.getServiceName());
        if(bean==null){
            throw new Exception("不存在名称为"+request.getServiceName()+"的服务");
        }
        //反射调用方法并返回结果
        Method method=bean.getClass().getMethod(request.getMethod(),request.getParameterTypes());
        return method.invoke(bean,request.getParameters());
    }
}
