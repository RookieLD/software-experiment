package com.github.coerx.qarchiver.core.compress;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;


public class SimpleCompressAndDecompressTest {
    Random random = new Random();

    @RepeatedTest(100)
    public void compressAndDecompressTest() {
        ICompressDecompress simpleCompressAndDecompress = new SimpleCompressAndDecompress();
        byte[] originBytes = new byte[random.nextInt(8000) + 1];
        random.nextBytes(originBytes);
        ByteArrayInputStream originInputStream = new ByteArrayInputStream(originBytes);
        ByteArrayOutputStream compressOutputStream = new ByteArrayOutputStream();
       // simpleCompressAndDecompress.compress(originInputStream, compressOutputStream);

        byte[] compressBytes = compressOutputStream.toByteArray();
        ByteArrayInputStream compressInputStream = new ByteArrayInputStream(compressBytes);
        ByteArrayOutputStream decompressOutputStream = new ByteArrayOutputStream();
      //  simpleCompressAndDecompress.decompress(compressInputStream, decompressOutputStream);

        byte[] decompressBytes = decompressOutputStream.toByteArray();

        Assertions.assertArrayEquals(originBytes, decompressBytes);
        //System.out.println("originBytes length,compressBytes length: "+originBytes.length+", "+compressBytes.length);
        if (originBytes.length > compressBytes.length) {
            System.out.println("success compress");
        }
    }

    @Test
    @Disabled
    public void reallifeScenarioTest() {
        //文件地址
        String location = "/home/coer/tmp/index.html";
        ICompressDecompress simpleCompressAndDecompress = new SimpleCompressAndDecompress();
        byte[] originBytes = null;
        try {
            originBytes = Files.readAllBytes(Path.of(location));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayInputStream originInputStream = new ByteArrayInputStream(originBytes);
        ByteArrayOutputStream compressOutputStream = new ByteArrayOutputStream();
     //   simpleCompressAndDecompress.compress(originInputStream, compressOutputStream);

        byte[] compressBytes = compressOutputStream.toByteArray();
        ByteArrayInputStream compressInputStream = new ByteArrayInputStream(compressBytes);
        ByteArrayOutputStream decompressOutputStream = new ByteArrayOutputStream();
      //  simpleCompressAndDecompress.decompress(compressInputStream, decompressOutputStream);

        byte[] decompressBytes = decompressOutputStream.toByteArray();

        Assertions.assertArrayEquals(originBytes, decompressBytes);
        System.out.println("originBytes length,compressBytes length: " + originBytes.length + ", " + compressBytes.length);
        if (originBytes.length > compressBytes.length) {
            System.out.println("success compress");
        }
    }
}
