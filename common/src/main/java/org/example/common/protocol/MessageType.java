package org.example.common.protocol;

import lombok.Getter;

public enum MessageType {
    REQUEST((byte)0),
    RESPONSE((byte)1);

    @Getter
    private byte type;
    MessageType(byte b) {
        this.type=b;
    }

    public static MessageType findType(short type){
        for(MessageType t:MessageType.values()){
            if(t.getType()==type)return t;
        }
        return REQUEST;
    }
}
