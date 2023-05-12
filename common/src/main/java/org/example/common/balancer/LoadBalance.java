package org.example.common.balancer;


import org.example.common.protocol.ServiceDetails;

import java.util.List;

public interface LoadBalance {
    ServiceDetails getInstance(List<ServiceDetails>serviceDetails);
}
