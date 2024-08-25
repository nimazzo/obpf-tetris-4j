package com.example.network;

import java.nio.ByteBuffer;

public class TypeSerializer {
    private final ByteBuffer fromCharBuffer = ByteBuffer.allocate(2);
    private final ByteBuffer fromLongBuffer = ByteBuffer.allocate(8);

    public byte[] serializeShort(short c) {
        var bytes = fromCharBuffer.putShort(c).array();
        fromCharBuffer.clear();
        return bytes;
    }

    public byte[] serializeLong(long l) {
        var bytes = fromLongBuffer.putLong(l).array();
        fromLongBuffer.clear();
        return bytes;
    }
}