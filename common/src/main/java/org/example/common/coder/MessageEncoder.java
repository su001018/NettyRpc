package org.example.common.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.example.common.protocol.Message;
import org.example.common.protocol.MessageHeader;
import org.example.common.serialization.Serialization;
import org.example.common.serialization.SerializationFactory;
import org.example.common.serialization.SerializationType;

public class MessageEncoder<T> extends MessageToByteEncoder<Message<T>> {

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
    protected void encode(ChannelHandlerContext channelHandlerContext, Message<T> tMessage, ByteBuf byteBuf) throws Exception {
        System.out.println("==========encoder===========");
        //获取消息头
        MessageHeader header=tMessage.getMessageHeader();
        //编码
        byteBuf.writeShort(header.getMagic());
        byteBuf.writeByte(header.getVersion());
        byteBuf.writeByte(header.getSerialization());
        byteBuf.writeByte(header.getType());
        byteBuf.writeByte(header.getStatus());
        byteBuf.writeLong(header.getId());


        //序列化对象
        Serialization serialization= SerializationFactory.getSerialization(SerializationType.findType(header.getSerialization()));
        byte[] data= serialization.serialize(tMessage.getMessageBody());

        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);


    }
}
