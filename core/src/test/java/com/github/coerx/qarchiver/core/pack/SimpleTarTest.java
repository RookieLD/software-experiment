package com.github.coerx.qarchiver.core.pack;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Map;
import java.util.Random;

public class SimpleTarTest {
    Random random = new Random();

    @RepeatedTest(20)
    public void packUnpackTest() {
        String file1 = RandomStringUtils.random(20);
        String file2 = RandomStringUtils.random(20);
        String file3 = RandomStringUtils.random(20);
        byte[] bytes1 = new byte[random.nextInt(1000)];
        byte[] bytes2 = new byte[random.nextInt(1000)];
        byte[] bytes3 = new byte[random.nextInt(1000)];
        random.nextBytes(bytes1);
        random.nextBytes(bytes2);
        random.nextBytes(bytes3);
        Map<String, byte[]> fileMap = Map.of(file1, bytes1, file2, bytes2, file3, bytes3);

        SimpleTar simpleTar = new SimpleTar();
        Map<String, byte[]> unpackFileMap = simpleTar.unpack(simpleTar.pack(fileMap));

        Assertions.assertArrayEquals(bytes1, unpackFileMap.get(file1));
        Assertions.assertArrayEquals(bytes2, unpackFileMap.get(file2));
        Assertions.assertArrayEquals(bytes3, unpackFileMap.get(file3));

    }
}
