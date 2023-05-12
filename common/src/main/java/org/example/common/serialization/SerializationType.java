package org.example.common.serialization;

import lombok.Getter;

public enum SerializationType {
    JSON((byte)0),
    PROTOBUF((byte)1),
    HESSIAN((byte)2);

    @Getter
    private byte type;

    SerializationType(byte b) {
        this.type=b;
    }
    public static SerializationType findType(byte type){
        for(SerializationType t:SerializationType.values()){
            if(t.getType()==type){
                return t;
            }
        }
        return JSON;
    }
}
