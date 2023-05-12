package org.example.client.transport;


import org.example.common.protocol.Message;
import org.example.common.protocol.MessageResponse;

import java.util.concurrent.ExecutionException;

public interface ClientTransport {
    Message<MessageResponse> sendMessage(RequestData requestData) throws Exception;
}
