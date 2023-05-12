package org.example.common.protocol;

import lombok.Getter;


public enum MessageStatus {
    SUCCESS((byte)0),
    ERROR((byte)1);

    @Getter
    private byte status;
    MessageStatus(byte b) {
        this.status=b;
    }


}
