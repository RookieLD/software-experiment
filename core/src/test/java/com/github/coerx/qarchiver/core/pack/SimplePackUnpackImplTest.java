package com.github.coerx.qarchiver.core.pack;

import com.github.coerx.qarchiver.core.common.utils.CommonUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static com.github.coerx.qarchiver.common.Constants.TEST_DIR;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SimplePackUnpackImplTest {

    private static List<File> needPackFiles;

    private static File packFile;


    @BeforeAll
    public static void createDirAndFile() throws IOException {
        File testRootDir = new File(TEST_DIR);
        if (testRootDir.exists() && testRootDir.isDirectory()) {
            FileUtils.deleteDirectory(testRootDir);
        } else if (testRootDir.isFile()) testRootDir.delete();
        testRootDir.mkdirs();

        File dir1 = new File(TEST_DIR + "/testDir1/testDir1_testDir1");
        File dir2 = new File(TEST_DIR + "/testDir2");
        dir1.mkdirs();
        dir2.mkdirs();

        File[] files = new File[5];
        files[0] = new File(TEST_DIR + "/file1.txt");
        files[1] = new File(TEST_DIR + "/file2.txt");
        files[2] = new File(TEST_DIR + "/testDir1/testDir1_file1.txt");
        files[3] = new File(TEST_DIR + "/testDir2/testDir2_file1.txt");
        files[4] = new File(TEST_DIR + "/file3.txt");
        for (File tmpFile : files) {
            tmpFile.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(tmpFile);
            outputStream.write("I am trying to get this code work work properly, but when I try to encode things it doesn't seem to work as it should. I have a text file thats 60bytes. I encode it and the outputted file is 100 bytes. When I decode that file it goes to like 65bytes. It decodes properly but the file size is larger than the original. I tried encode a jpg and the file size did go down, however I couldn't open the file afters. I tried to decode the jpg file and it didn't work, seemed like cmd had frozen. This is the code I was trying to use.".getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        }

        needPackFiles = List.of(new File(TEST_DIR + "/testDir1"),
                new File(TEST_DIR + "/testDir2"), files[0], files[1]);
        //needPackFiles = List.of(new File(testRootPath + "/testDir1"));
        //needPackFiles = List.of(files[0],files[1]);
        /*needPackFiles = List.of(new File(testRootPath + "/testDir1"),
                new File(testRootPath + "/testDir2"), files[0], files[1],files[3]);*/
        /*needPackFiles = List.of(new File("/home/coer/tmp/hello/test/dir1"),
                new File("/home/coer/tmp/hello/test/dir2"),
                new File("/home/coer/tmp/hello/test/ls"));*/
    }

    @AfterAll
    public static void clean() throws IOException {
        File testRootDir = new File(TEST_DIR);
        if (testRootDir.exists() && testRootDir.isDirectory()) {
            FileUtils.deleteDirectory(testRootDir);
        } else if (testRootDir.isFile()) testRootDir.delete();
    }

    @Test
    @Order(1)
    public void packTest() throws IOException {
        SimplePackUnpackImpl util = new SimplePackUnpackImpl();
        packFile = new File(TEST_DIR + "/pack.mytar");
        FileOutputStream outputStream = new FileOutputStream(packFile);
        util.pack(needPackFiles, outputStream);
        outputStream.close();
    }

    //@Disabled
    @Test
    @Order(2)
    public void unPackTest() throws IOException {
        SimplePackUnpackImpl util = new SimplePackUnpackImpl();
        File dir = new File(TEST_DIR + "/unpack");
        if (dir.exists()) FileUtils.deleteDirectory(dir);
        dir.mkdir();
        util.unpack(new FileInputStream(TEST_DIR + "/pack.mytar"), dir);
    }

    //@Disabled
    @Test
    @Order(3)
    public void check() throws IOException {
        File unpackDir = new File(TEST_DIR + "/unpack");
        Assertions.assertEquals(unpackDir.listFiles().length, needPackFiles.size());
        for (File file : unpackDir.listFiles()) {
            File otherFile = null;
            Path relativePath = unpackDir.toPath().relativize(file.toPath());
            for (File tmpFile : needPackFiles) {
                if (Path.of(TEST_DIR).relativize(tmpFile.toPath()).equals(relativePath)) {
                    otherFile = tmpFile;
                    break;
                }
            }
            Assertions.assertNotNull(otherFile);
            if (file.isFile()) {
                Assertions.assertTrue(Arrays.equals(Files.readAllBytes(file.toPath()),
                        Files.readAllBytes(otherFile.toPath())));
                continue;
            }
            Assertions.assertTrue(CommonUtil.compareTwoDirTree(file, otherFile));
        }
    }
}
