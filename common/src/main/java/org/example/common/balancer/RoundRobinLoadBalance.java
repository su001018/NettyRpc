package org.example.common.balancer;


import org.example.common.protocol.ServiceDetails;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RoundRobinLoadBalance implements LoadBalance{
    private static AtomicInteger no=new AtomicInteger(0);
    @Override
    public ServiceDetails getInstance(List<ServiceDetails> serviceDetails) {
        if(no.get()>=serviceDetails.size())no.set(0);
        return serviceDetails.get(no.incrementAndGet());
    }
}
