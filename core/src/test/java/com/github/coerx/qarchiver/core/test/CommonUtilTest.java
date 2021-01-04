package com.github.coerx.qarchiver.core.test;


import com.github.coerx.qarchiver.core.common.utils.CommonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommonUtilTest {

    @Test
    public void longAndBytesConversionTest() {
        Assertions.assertEquals(323232, CommonUtil.bytesToLong(CommonUtil.longToBytes(323232)));
        Assertions.assertEquals(-323232, CommonUtil.bytesToLong(CommonUtil.longToBytes(-323232)));
        Assertions.assertEquals(32, CommonUtil.bytesToLong(CommonUtil.longToBytes(32)));
        Assertions.assertEquals(-32, CommonUtil.bytesToLong(CommonUtil.longToBytes(-32)));

        Assertions.assertEquals(0xf00000000000003fL, CommonUtil.bytesToLong(CommonUtil.longToBytes(0xf00000000000003fL)));
        Assertions.assertEquals(0x0f0000000000003fL, CommonUtil.bytesToLong(CommonUtil.longToBytes(0x0f0000000000003fL)));
        Assertions.assertEquals(0x1f0000022000003fL, CommonUtil.bytesToLong(CommonUtil.longToBytes(0x1f0000022000003fL)));
    }

    @Test
    public void longToBytesTest() {
        byte[] bytes1 = new byte[8];
        bytes1[0] = (byte) 0x3f;
        bytes1[1] = (byte) 0xa0;
        Assertions.assertArrayEquals(CommonUtil.longToBytes(0xa03f), bytes1);

        bytes1 = new byte[8];
        bytes1[0] = (byte) 0x3f;
        bytes1[7] = (byte) 0xa0;
        Assertions.assertArrayEquals(CommonUtil.longToBytes(0xa00000000000003fL), bytes1);
    }

    @Test
    public void bytesToLongTest() {
        long num = 0xa03fL;
        byte[] bytes1 = new byte[8];
        bytes1[0] = (byte) 0x3f;
        bytes1[1] = (byte) 0xa0;
        Assertions.assertEquals(num, CommonUtil.bytesToLong(bytes1));

        num = 0xf00000000000003fL;
        bytes1 = new byte[8];
        bytes1[0] = (byte) 0x3f;
        bytes1[7] = (byte) 0xf0;
        Assertions.assertEquals(num, CommonUtil.bytesToLong(bytes1));

    }
}
