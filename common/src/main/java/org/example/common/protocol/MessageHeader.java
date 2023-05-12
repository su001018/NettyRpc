package org.example.common.protocol;

import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class MessageHeader implements Serializable {
/*
+---------------------------------------------------------------+
| 魔数 2byte | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte  |
+---------------------------------------------------------------+
| 状态 1byte |        消息id 8byte     |      数据长度 4byte      |
+---------------------------------------------------------------+
|                   数据内容 （长度不定）                          |
+---------------------------------------------------------------+
*/
    private static AtomicLong num=new AtomicLong(0);
    private short magic;
    private byte version;
    private byte serialization;
    private byte type;
    private byte status;
    private long id;
    private int length;

    public MessageHeader(byte serialization,byte type){
        this.magic=Constants.MAGIC;
        this.version=Constants.VERSION;
        this.serialization=serialization;
        this.type=type;
        this.status=0;
        this.id=num.incrementAndGet();
    }
    public MessageHeader(byte serialization,byte type,long id){
        this.magic=Constants.MAGIC;
        this.version=Constants.VERSION;
        this.serialization=serialization;
        this.type=type;
        this.status=0;
        this.id=id;
    }
    public MessageHeader(byte serialization,byte type,long id,int length){
        this(serialization,type,id);
        this.length=length;
    }


}
