package com.github.coerx.qarchiver.common.util;

import com.google.common.hash.Hashing;
import org.junit.jupiter.api.RepeatedTest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteUtilTest {

    @RepeatedTest(10)
    public void bytesToHexStringTest() throws NoSuchAlgorithmException {
        Random random = new Random();
        byte[] bytes = new byte[random.nextInt(100) + 1];
        random.nextBytes(bytes);
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(bytes);
        assertEquals(Hashing.md5().hashBytes(bytes).toString(), ByteUtil.bytesToHexString(md.digest()));
    }
}
