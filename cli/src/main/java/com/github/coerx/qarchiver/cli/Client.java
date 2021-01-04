package com.github.coerx.qarchiver.cli;

import com.github.coerx.qarchiver.core.compress.ICompressDecompress;
import com.github.coerx.qarchiver.core.compress.SimpleCompressAndDecompress;
import com.github.coerx.qarchiver.core.pack.IPackUnpack;
import com.github.coerx.qarchiver.core.pack.SimplePackUnpackImpl;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.github.coerx.qarchiver.common.Constants.TEMP_DIR_PATH;

public class Client {


    public static void main(String[] args) throws IOException {
        File dir = new File(TEMP_DIR_PATH);
        dir.mkdir();
        if (args == null || args.length == 0) {
            System.out.println("请指定文件夹和文件路径");
            return;
        }
        if (args[0].equals("-h")) {
            System.out.println("指令参数: ");
            System.out.println("压缩指令，path指定文件和文件夹的路径(用逗号分隔)，targetFile指定生成的压缩文件路径和名字");
            System.out.println("-c path[, path[, ...path]] targetFile\n");
            System.out.println("解压指令，path指定压缩文件的路径，rootDir指定文件被解压的目录");
            System.out.println("-d path rootDir");
            return;
        } else if (args[0].equals("-c")) {
            if (args.length != 3) {
                System.out.println("压缩指令缺少参数: -c path[, path[, ...path]] targetFile");
                return;
            }
            List<File> fileList = new ArrayList<>();
            for (String str : args[1].split(",")) {
                File file = new File(str);
                if (!file.exists()) {
                    System.out.println(file.getPath() + " 不存在");
                    return;
                }
                File canonicalFile = file.getCanonicalFile();
                fileList.add(canonicalFile);
            }

            File targetFile = new File(args[2]);
            if (targetFile.exists()) {
                System.out.println(targetFile.getPath() + "已存在");
            }
            targetFile.createNewFile();
            OutputStream targetFileOutputStream = new BufferedOutputStream(new FileOutputStream(targetFile));

            IPackUnpack packUnpack = new SimplePackUnpackImpl();
            ICompressDecompress compressDecompress = new SimpleCompressAndDecompress();

            File tmpFile = new File(TEMP_DIR_PATH + "\\temp_file_" + RandomStringUtils.randomAlphanumeric(12) + ".mytar");
            tmpFile.createNewFile();
            OutputStream tmpFileOutputStream = new BufferedOutputStream(new FileOutputStream(tmpFile));

            packUnpack.pack(fileList, tmpFileOutputStream);
            tmpFileOutputStream.flush();
            tmpFileOutputStream.close();

            compressDecompress.compress(new BufferedInputStream(new FileInputStream(tmpFile)), targetFileOutputStream);
            targetFileOutputStream.flush();
            targetFileOutputStream.close();
        } else if (args[0].equals("-d")) {
            if (args.length != 3) {
                System.out.println("解压指令缺少参数: -d file rootDir");
                return;
            }

            File compressdFile = new File(args[1]);
            if (!compressdFile.exists()) {
                System.out.println(compressdFile.getPath() + " 压缩文件不存在");
                return;
            }
            File rootDir = new File(args[2]);
            if (!rootDir.exists() || !rootDir.isDirectory()) {
                System.out.println("目标文件夹不存在或者不是文件夹");
                return;
            }

            File tmpFile = new File(TEMP_DIR_PATH + "\\temp_file_" + RandomStringUtils.randomAlphanumeric(12) + ".mytar");
            tmpFile.createNewFile();
            OutputStream tmpFileOutputStream = new BufferedOutputStream(new FileOutputStream(tmpFile));

            ICompressDecompress compressDecompress = new SimpleCompressAndDecompress();
            compressDecompress.decompress(new BufferedInputStream(new FileInputStream(compressdFile)), tmpFileOutputStream);
            tmpFileOutputStream.flush();
            tmpFileOutputStream.close();

            IPackUnpack packUnpack = new SimplePackUnpackImpl();
            packUnpack.unpack(new BufferedInputStream(new FileInputStream(tmpFile)), rootDir);
        } else {
            System.out.println("指令参数: ");
            System.out.println("压缩指令，path指定文件和文件夹的路径(用逗号分隔)，targetFile指定生成的压缩文件路径和名字");
            System.out.println("-c path[, path[, ...path]] targetFile\n");
            System.out.println("解压指令，path指定压缩文件的路径，rootDir指定文件被解压的目录");
            System.out.println("-d path rootDir");
            return;
        }
    }
}
