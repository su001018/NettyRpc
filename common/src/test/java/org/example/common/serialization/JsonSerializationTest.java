package org.example.common.serialization;


import org.example.common.protocol.Message;
import org.example.common.protocol.MessageRequest;

import java.io.IOException;

class JsonSerializationTest {
    public static void main(String[] args) throws IOException {
        MessageRequest request=new MessageRequest();
        request.setParameterTypes(new Class[]{String.class});
        byte[] serialize = new JsonSerialization().serialize(request);
        MessageRequest res= new JsonSerialization().deserialize(serialize,MessageRequest.class);
        System.out.println(res);

    }
}