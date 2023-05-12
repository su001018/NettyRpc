package org.example.common.register;


import org.example.common.protocol.ServiceDetails;

public interface ServiceRegister {
    void register(ServiceDetails serviceDetails) throws Exception;
    void unRegister(ServiceDetails serviceDetails) throws Exception;
    void close() throws Exception;
}
