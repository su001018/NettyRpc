package org.example.common.balancer;

import lombok.Getter;

public enum LoadBalanceType {
    RANDOM(0),
    ROUND_ROBIN(1);

    @Getter
    int type;
    LoadBalanceType(int i) {
        this.type=i;
    }
    public static LoadBalanceType findType(int type){
        for(LoadBalanceType t:LoadBalanceType.values()){
            if(t.getType()==type)return t;
        }
        return RANDOM;
    }
}
