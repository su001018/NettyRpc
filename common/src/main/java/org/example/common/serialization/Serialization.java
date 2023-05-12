package org.example.common.serialization;

import java.io.IOException;

public interface Serialization {
    byte[] serialize(Object obj) throws IOException;
    <T>T deserialize(byte[] data,Class<T>clz) throws IOException;
}
