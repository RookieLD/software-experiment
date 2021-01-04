package com.github.coerx.qarchiver.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FileUtil {

    private static MessageDigest md5;

    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 比较两个目录下的目录树是否相同，
     *
     * @param dir1
     * @param dir2
     * @return
     */
    public static boolean compareTwoDirTree(File dir1, File dir2) {
        if (!dir1.exists() || !dir2.exists() || dir1.isFile() || dir2.isFile()) {
            throw new RuntimeException("arguments are not all directory or not exist");
        }
        return compare(dir1.toPath(), dir2.toPath()) && compare(dir2.toPath(), dir1.toPath());
    }

    private static boolean compare(Path one, Path other) {
        final boolean[] isEqual = new boolean[1];
        isEqual[0] = true;
        try {
            Files.walkFileTree(one, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    FileVisitResult result = super.preVisitDirectory(dir, attrs);
                    Path relativize = one.relativize(dir);
                    Path dirInOther = other.resolve(relativize);
                    if (!dirInOther.toFile().exists()) {
                        isEqual[0] = false;
                        return FileVisitResult.TERMINATE;
                    }
                    return result;
                }

                @Override
                public FileVisitResult visitFile(Path file,
                                                 BasicFileAttributes attrs)
                        throws IOException {
                    FileVisitResult result = super.visitFile(file, attrs);

                    // get the relative file name from path "one"
                    Path relativize = one.relativize(file);
                    // construct the path for the counterpart file in "other"
                    Path fileInOther = other.resolve(relativize);
                    //log.debug("=== comparing: {} to {}", file, fileInOther);
                    if (!fileInOther.toFile().exists()) {
                        isEqual[0] = false;
                        return FileVisitResult.TERMINATE;
                    }
                    byte[] otherBytes = Files.readAllBytes(fileInOther);
                    byte[] theseBytes = Files.readAllBytes(file);
                    if (!Arrays.equals(otherBytes, theseBytes)) {
                        isEqual[0] = false;
                        return FileVisitResult.TERMINATE;
                    }
                    return result;
                }
            });
        } catch (Exception e) {
            isEqual[0] = false;
        }
        return isEqual[0];
    }

    /**
     * 计算文件每个分片的md5值
     *
     * @param file
     * @param slice
     * @return
     * @throws IOException
     */
    public static List<String> md5sumStringListOfEachSlice(File file, int slice) throws IOException {
        if (file == null || !file.exists() || !file.isFile() || file.length() == 0) {
            throw new IllegalArgumentException("文件不存在或者读取不到内容");
        }
        if (slice <= 0) {
            throw new IllegalArgumentException("切片大小必须大于0");
        }
        List<String> md5sumList = new LinkedList<>();
        InputStream in = new FileInputStream(file);
        byte[] buffer = new byte[slice];
        int len;
        int haveRead = 0;
        md5.reset();
        while ((len = in.read(buffer)) >= 0) {
            md5.update(buffer, 0, len);
            haveRead += len;
            if (haveRead == slice) {
                md5sumList.add(ByteUtil.bytesToHexString(md5.digest()));
                haveRead = 0;
            }
        }
        if (haveRead > 0) {
            md5sumList.add(ByteUtil.bytesToHexString(md5.digest()));
        }
        return md5sumList;
    }

    public static String md5sumString(File file) throws IOException {
        if (file == null || !file.exists() || !file.isFile() || file.length() == 0) {
            throw new IllegalArgumentException("文件不存在或者读取不到内容");
        }
        byte[] buffer = new byte[1024 * 1024];
        FileInputStream inputStream = new FileInputStream(file);
        int len = 0;
        while ((len = inputStream.read(buffer)) >= 0) {
            md5.update(buffer, 0, len);
        }
        return ByteUtil.bytesToHexString(md5.digest());
    }
}
