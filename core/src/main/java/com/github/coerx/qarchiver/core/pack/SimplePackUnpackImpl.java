package com.github.coerx.qarchiver.core.pack;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 参考tar的一个简单实现，单个文件大小和最后打包的文件大小都不要超过2GB
 */
public class SimplePackUnpackImpl implements IPackUnpack {
    private static final int FILE_NAME_FIELD_OFFSET = 0;
    private static final int FILE_NAME_FIELD_SIZE = 200;
    private static final int FILE_LENGTH_FIELD_OFFSET = 200;
    private static final int FILE_LENGTH_FIELD_SIZE = 8;
    private static final int HEADER_SIZE = 208;

    /**
     * 打包工具类，注意fileList中的文件是目录树最上层的文件夹和处于同一层的文件，文件夹中所有的文件都会被打包
     * <pre>
     * 例子，缩进代表当前文件或文件夹属于上一层文件夹:
     * dir1
     *      file1
     *      dir2
     *          file2
     * dir2
     *      file3
     * file4
     *
     * fileList中的file是dir1,dir2,file4
     * </pre>
     *
     * @param fileList
     * @param outputStream
     */
    public void pack(List<File> fileList, OutputStream outputStream) {
        try {
            if (fileList.isEmpty()) throw new RuntimeException("没有指定需要打包的文件");
            Map<String, byte[]> fileMap = new HashMap<>();
            for (File file : fileList) {
                if (!file.exists()) {
                    throw new RuntimeException("文件或文件夹不存在, " + file.getPath());
                }
                if (file.isFile()) {
                    fileMap.put(file.getName(),
                            Files.readAllBytes(Path.of(file.getCanonicalPath())));
                    continue;
                }
                visitedDir(file, fileMap, file.getCanonicalFile().getParent());
            }
            SimplePackUnpack simplePackUnpack = new SimpleTar();
            byte[] bytes = simplePackUnpack.pack(fileMap);
            outputStream.write(bytes);
            outputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void visitedDir(File dir, Map<String, byte[]> fileMap, String rootPath) throws IOException {
        File[] files = dir.getCanonicalFile().listFiles();
        if (files.length == 0) {
            fileMap.put(dir.getCanonicalPath().substring(rootPath.length() + 1) + "\\",
                    new byte[0]);
            return;
        }
        for (File file : files) {
            if (file.isFile()) {
                fileMap.put(file.getCanonicalPath().substring(rootPath.length() + 1),
                        Files.readAllBytes(Path.of(file.getCanonicalPath())));
                continue;
            }
            visitedDir(file, fileMap, rootPath);
        }
    }

    /**
     * @param inputStream 打包的文件
     * @param root        解包的路径
     */
    public void unpack(InputStream inputStream, File root) {
        try {
            SimplePackUnpack simplePackUnpack = new SimpleTar();
            byte[] bytes = inputStream.readAllBytes();
            Map<String, byte[]> fileMap = simplePackUnpack.unpack(bytes);
            String rootPath = root.getCanonicalPath();
            if (!rootPath.endsWith("\\")) rootPath = rootPath + "\\";
            for (Map.Entry<String, byte[]> entry : fileMap.entrySet()) {
                String fileName = entry.getKey();
                if (fileName.endsWith("\\")) {
                    File newDir = new File(rootPath + fileName.substring(0, fileName.length() - 1));
                    if (newDir.exists()) {
                        throw new RuntimeException("文件夹已存在: " + newDir.getCanonicalPath());
                    }
                    newDir.mkdirs();
                    continue;
                }
                File newFile = new File(rootPath + fileName);
                if (newFile.exists()) {
                    throw new RuntimeException("文件已存在: " + newFile.getCanonicalPath());
                }
                File parentDir = newFile.getCanonicalFile().getParentFile();
                if (!parentDir.exists()) parentDir.mkdirs();
                newFile.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(newFile);
                outputStream.write(entry.getValue());
                outputStream.flush();
                outputStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
