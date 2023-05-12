package org.example.server.cache;


import java.util.HashMap;
import java.util.Map;

public class LocalCache {
    private static final Map<String, Object>localMap=new HashMap<>();
    public static void store(String serviceName,Object bean){
        localMap.put(serviceName,bean);
    }
    public static Object get(String serviceName){
        return localMap.get(serviceName);
    }
    public static Object delete(String serviceName){
        return localMap.remove(serviceName);
    }
}
