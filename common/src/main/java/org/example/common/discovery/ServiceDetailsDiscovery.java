package org.example.common.discovery;


import org.example.common.protocol.ServiceDetails;

public interface ServiceDetailsDiscovery {
    ServiceDetails discovery(String ServiceName) throws Exception;
    void close() throws Exception;
}
