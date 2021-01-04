package com.github.coerx.qarchiver.common.util;

public class NumberUtil {
    /**
     * byte order is little-endian
     *
     * @param num
     * @return
     */
    public static byte[] longToBytes(long num) {
        byte[] bytes = new byte[8];
        long mask = 0x00ffL;
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) ((num & mask) >>> i * 8);
            mask <<= 8;
        }
        return bytes;
    }

    /**
     * byte order is little-endian
     *
     * @param bytes
     * @return
     */
    public static long bytesToLong(byte[] bytes) {
        if (bytes.length != 8) {
            throw new RuntimeException("数组长度不为8");
        }
        long result = 0;
        for (int i = 0; i < bytes.length; i++) {
            long nowByte = (bytes[i] & 0x00000000000000ffL) << i * 8;
            result |= nowByte;
        }
        return result;
    }

}
