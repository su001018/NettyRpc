package org.example.client.transport;


import lombok.Data;
import org.example.common.protocol.Message;
import org.example.common.protocol.MessageRequest;

@Data
public class RequestData {
    //消息
    Message<MessageRequest> requestMessage;
    //请求地址
    String address;
    //端口
    Integer port;
    //服务嗲用超时
    Integer timeout;

}
