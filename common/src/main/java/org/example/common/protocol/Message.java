package org.example.common.protocol;

import lombok.Data;

import java.io.Serializable;

@Data
public class Message<T> implements Serializable {
    //消息头
    MessageHeader messageHeader;
    //消息内容
    T messageBody;
}
