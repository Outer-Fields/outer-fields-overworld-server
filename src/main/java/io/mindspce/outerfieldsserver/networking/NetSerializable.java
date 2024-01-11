package io.mindspce.outerfieldsserver.networking;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;


public interface NetSerializable {

    static ByteBuffer getEmptyBuffer(int bufferSize) {
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        var ds = new ByteArrayOutputStream();
        return buffer;
    }

    static byte[] trimBufferToBytes(ByteBuffer buffer) {
        buffer.flip();
        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        return byteArray;
    }

    static List<Byte> encodeVarInt(int value) {
        List<Byte> byteList = new ArrayList<>();
        while (true) {
            if ((value & ~0x7F) == 0) {
                byteList.add((byte) value);
                return byteList;
            } else {
                byteList.add((byte) ((value & 0x7F) | 0x80));
                value >>>= 7;
            }
        }
    }

    static int decodeVarInt(List<Byte> byteList) {
        int value = 0;
        int shift = 0;
        for (byte b : byteList) {
            value |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return value;
            }
            shift += 7;
        }
        throw new IllegalArgumentException("Byte array did not contain valid variants.");
    }

    static byte[] combineByteArrays(byte[] arr1, byte[] arr2) {
        byte[] combined = new byte[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, combined, 0, arr1.length);
        System.arraycopy(arr2, 0, combined, arr1.length, arr2.length);
        return combined;
    }

    int byteSize();


    void addBytesToBuffer(ByteBuffer buffer);

}
