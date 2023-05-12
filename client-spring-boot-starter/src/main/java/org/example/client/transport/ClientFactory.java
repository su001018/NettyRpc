package org.example.client.transport;

public class ClientFactory {
    public static ClientTransport getClientTransport(){
        return new NettyClientTransport();
    }
}
