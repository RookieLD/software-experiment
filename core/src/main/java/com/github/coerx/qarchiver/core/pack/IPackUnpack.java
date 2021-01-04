package com.github.coerx.qarchiver.core.pack;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * 定义打包解包的接口
 */
public interface IPackUnpack {
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
    public void pack(List<File> fileList, OutputStream outputStream);

    public void unpack(InputStream inputStream, File root);
}
