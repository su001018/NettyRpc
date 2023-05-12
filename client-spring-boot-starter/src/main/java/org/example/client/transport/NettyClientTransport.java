package org.example.client.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.example.client.cache.FutureCache;
import org.example.client.handler.ClientResponseHandler;
import org.example.common.coder.MessageDecoder;
import org.example.common.coder.MessageEncoder;
import org.example.common.protocol.Message;
import org.example.common.protocol.MessageRequest;
import org.example.common.protocol.MessageResponse;
import org.springframework.beans.factory.DisposableBean;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;


public class NettyClientTransport implements ClientTransport, DisposableBean {
    private EventLoopGroup bossGroup;
    private Bootstrap bootstrap;
    private final ClientResponseHandler handler;
    public NettyClientTransport(){
        bossGroup=new NioEventLoopGroup();
        bootstrap=new Bootstrap();
        handler=new ClientResponseHandler();
        bootstrap.group(bossGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new MessageDecoder())
                                .addLast(handler)
                                .addLast(new MessageEncoder<>());
                    }
                });

    }
    @Override
    public Message<MessageResponse> sendMessage(RequestData requestData) throws Exception {
        Message<MessageRequest> message=requestData.getRequestMessage();
        RequestFuture<Message<MessageResponse>>requestFuture=new RequestFuture<>();
        FutureCache.store(message.getMessageHeader().getId(),requestFuture);

        ChannelFuture channelFuture=bootstrap.connect(requestData.getAddress(),requestData.getPort()).sync();
        channelFuture.addListener(arg0->{
            if(channelFuture.isSuccess()){
                System.out.println("客户端连接成功");
            }else{
                System.out.println("客户端连接失败");
                channelFuture.cause().printStackTrace();
                bossGroup.shutdownGracefully();
            }
        });
        channelFuture.channel().writeAndFlush(message);
//        channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("hello netty!".getBytes()));
        return requestData.getTimeout()==null?requestFuture.get():requestFuture.get(requestData.getTimeout(), TimeUnit.SECONDS);
    }

    @Override
    public void destroy() throws Exception {
        if(bossGroup!=null){
            bossGroup.shutdownGracefully();
        }
    }
}
