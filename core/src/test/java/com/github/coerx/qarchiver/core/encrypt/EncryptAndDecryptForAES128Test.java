package com.github.coerx.qarchiver.core.encrypt;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

public class EncryptAndDecryptForAES128Test {
    Random random = new Random();

    @RepeatedTest(10)
    public void encryptAndDecryptTest() {
        char[] chars = RandomStringUtils.randomAlphanumeric(random.nextInt(20) + 1).toCharArray();
        EncryptAndDecryptForAES128 encryptAndDecryptForAES128 = new EncryptAndDecryptForAES128(chars);
        byte[] data = new byte[random.nextInt(1024 * 512) + 1];
        random.nextBytes(data);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        encryptAndDecryptForAES128.encrypt(inputStream, outputStream);
        byte[] encryptData = outputStream.toByteArray();
        //System.out.println("origin data length: "+data.length+",encrypt data length: " + encryptData.length);

        inputStream = new ByteArrayInputStream(encryptData);
        outputStream = new ByteArrayOutputStream();
        encryptAndDecryptForAES128.decrypt(inputStream, outputStream);
        byte[] decryptData = outputStream.toByteArray();

        /*System.out.println(ArrayUtils.toString(data));
        System.out.println(ArrayUtils.toString(encryptData));*/
        Assertions.assertArrayEquals(data, decryptData);
    }
}
