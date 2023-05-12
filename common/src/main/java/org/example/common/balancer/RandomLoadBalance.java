package org.example.common.balancer;



import org.example.common.protocol.ServiceDetails;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance implements LoadBalance{
    @Override
    public ServiceDetails getInstance(List<ServiceDetails> serviceDetails) {
        return serviceDetails.get(new Random().nextInt(serviceDetails.size()));
    }
}
