package org.example.common.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.example.common.protocol.*;
import org.example.common.serialization.SerializationFactory;
import org.example.common.serialization.SerializationType;

import java.lang.reflect.Type;
import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {


/*
+---------------------------------------------------------------+
| 魔数 2byte | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte  |
+---------------------------------------------------------------+
| 状态 1byte |        消息id 8byte     |      数据长度 4byte      |
+---------------------------------------------------------------+
|                   数据内容 （长度不定）                          |
+---------------------------------------------------------------+
*/
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        System.out.println("==========decoder===========");
        //协议长度小于头部长度，放弃读取
        if(byteBuf.readableBytes()< Constants.HEADER_LEN){
            System.out.println("协议长度小于头部长度，放弃读取");
            return;
        }
        //标记读指针位置，出错回退
        byteBuf.markReaderIndex();

        if(byteBuf.readShort()!=Constants.MAGIC){
            throw new Exception("协议出错，头部魔数不匹配");
        }
        //读取消息头内容
        byte version=byteBuf.readByte();
        byte serialization=byteBuf.readByte();
        byte type=byteBuf.readByte();
        byte status=byteBuf.readByte();
        long id=byteBuf.readLong();
        int length=byteBuf.readInt();

        //半包现象，消息内容不完整，放弃读取
        if(byteBuf.readableBytes()<length){
            byteBuf.resetReaderIndex();
            System.out.println("半包现象，消息内容不完整，放弃读取");
            return;
        }

        //读取消息体
        byte[] data=new byte[length];
        byteBuf.readBytes(data);

        //构造消息头
        MessageHeader header=new MessageHeader(serialization,type,id,length);

        switch (MessageType.findType(type)){
            case REQUEST -> {
                Message<MessageRequest>message=new Message<>();
                message.setMessageHeader(header);
                MessageRequest request= SerializationFactory.getSerialization(SerializationType.findType(serialization))
                        .deserialize(data,MessageRequest.class);
                message.setMessageBody(request);
                list.add(message);
                break;
            }
            case RESPONSE -> {
                Message<MessageResponse>message=new Message<>();
                message.setMessageHeader(header);
                MessageResponse response=SerializationFactory.getSerialization(SerializationType.findType(serialization))
                        .deserialize(data,MessageResponse.class);
                message.setMessageBody(response);
                list.add(message);
                break;
            }
        }
    }
}
