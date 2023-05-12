package org.example.common.serialization;

public class SerializationFactory {

    public static Serialization getSerialization(SerializationType type) throws Exception {

        switch (type){
            case JSON:
                return new JsonSerialization();
            default:
                throw new Exception("序列化参数不合法");
        }
    }
}
