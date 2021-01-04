package com.github.coerx.qarchiver.core.common.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

@Deprecated
public class CommonUtil {
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
}
