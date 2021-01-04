package com.github.coerx.qarchiver.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class ByteUtil {
    /**
     * 返回用户友好的表示字节大小的字符串
     *
     * @param bytes 字节大小
     * @return
     */
    private static MessageDigest md5;

    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f%cB", value / 1024.0, ci.current());
    }

    public static String bytesToHexString(byte[] bytes) {
        char[] chars = {'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuilder builder = new StringBuilder();
        for (byte byt : bytes) {
            int height = (byt & 0x000000ff) >>> 4;
            int low = byt & 0x0000000f;
            if (height < 10) {
                builder.append(height);
            } else {
                builder.append(chars[height - 10]);
            }
            if (low < 10) {
                builder.append(low);
            } else {
                builder.append(chars[low - 10]);
            }
        }
        return builder.toString();
    }

    public static String md5StringOfBytes(byte[] bytes) {
        return bytesToHexString(md5OfBytes(bytes));
    }

    public static byte[] md5OfBytes(byte[] bytes) {
        if (md5 == null) {
            synchronized (ByteUtil.class) {
                if (md5 == null) {
                    try {
                        md5 = MessageDigest.getInstance("MD5");
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        md5.reset();
        md5.update(bytes);
        return md5.digest();
    }
}
