package org.example.client.handler;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.client.cache.FutureCache;
import org.example.common.protocol.Message;
import org.example.common.protocol.MessageResponse;

public class ClientResponseHandler extends SimpleChannelInboundHandler<Message<MessageResponse>> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message<MessageResponse> messageResponseMessage) throws Exception {
        Long id=messageResponseMessage.getMessageHeader().getId();
        FutureCache.responseCallBack(id,messageResponseMessage);
    }
}
