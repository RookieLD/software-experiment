package com.github.coerx.qarchiver.common.util;

import com.google.common.hash.Hashing;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class FileUtilTest {

    Random random = new Random();

    @RepeatedTest(5)
    public void getMd5sumOfEachSliceTest() throws IOException {
        int slice = 1024 * 1024 * 4;
        byte[] bytes1 = new byte[slice];
        byte[] bytes2 = new byte[slice];
        byte[] bytes3 = new byte[slice];
        byte[] bytes4 = new byte[random.nextInt(slice) + 1];
        random.nextBytes(bytes1);
        random.nextBytes(bytes2);
        random.nextBytes(bytes3);
        random.nextBytes(bytes4);
        File file = new File("/tmp/BaiduPanClientTestFile_" + RandomStringUtils.randomAlphanumeric(20));
        file.createNewFile();
        OutputStream outputStream = new FileOutputStream(file);
        outputStream.write(bytes1);
        outputStream.write(bytes2);
        outputStream.write(bytes3);
        outputStream.write(bytes4);
        outputStream.flush();
        outputStream.close();
        List<String> list1 = FileUtil.md5sumStringListOfEachSlice(file, slice);
        List<String> list2 = new LinkedList<>();
        list2.add(Hashing.md5().hashBytes(bytes1).toString());
        list2.add(Hashing.md5().hashBytes(bytes2).toString());
        list2.add(Hashing.md5().hashBytes(bytes3).toString());
        list2.add(Hashing.md5().hashBytes(bytes4).toString());

        Assertions.assertEquals(list2, list1);
    }

    @RepeatedTest(5)
    public void md5sumStringTest() throws IOException {
        byte[] bytes1 = new byte[random.nextInt(1024 * 1024 * 10) + 1];
        File file = new File("/tmp/BaiduPanClientTestFile_" + RandomStringUtils.randomAlphanumeric(20));
        file.createNewFile();
        OutputStream outputStream = new FileOutputStream(file);
        outputStream.write(bytes1);
        outputStream.flush();
        outputStream.close();

        Assertions.assertEquals(Hashing.md5().hashBytes(bytes1).toString(),
                FileUtil.md5sumString(file));
    }
}
