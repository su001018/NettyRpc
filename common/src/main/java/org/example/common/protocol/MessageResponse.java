package org.example.common.protocol;

import lombok.Data;

import java.io.Serializable;

@Data
public class MessageResponse implements Serializable {
    //回应数据
    private Object data;
    //错误信息
    private String message;

}
