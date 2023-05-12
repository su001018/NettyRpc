package org.example.client.cache;


import org.example.client.transport.RequestFuture;
import org.example.common.protocol.Message;
import org.example.common.protocol.MessageResponse;

import java.util.concurrent.ConcurrentHashMap;

public class FutureCache {
    private static final ConcurrentHashMap<Long, RequestFuture<Message<MessageResponse>>>futureCache=
            new ConcurrentHashMap<>();
    public static void store(long id,RequestFuture<Message<MessageResponse>> requestFuture){
        futureCache.put(id,requestFuture);
    }
    public static void responseCallBack(long id,Message<MessageResponse>result){
        RequestFuture<Message<MessageResponse>> requestFuture=futureCache.get(id);
        futureCache.remove(id);
        if(requestFuture==null)return;
        requestFuture.setResult(result);
    }
}
