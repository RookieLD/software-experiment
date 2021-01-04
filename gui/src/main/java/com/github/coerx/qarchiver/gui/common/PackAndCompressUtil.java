package com.github.coerx.qarchiver.gui.common;

import com.github.coerx.qarchiver.core.compress.ICompressDecompress;
import com.github.coerx.qarchiver.core.compress.SimpleCompressAndDecompress;
import com.github.coerx.qarchiver.core.encrypt.EncryptAndDecryptForAES128;
import com.github.coerx.qarchiver.core.encrypt.IEncryptAndDecrypt;
import com.github.coerx.qarchiver.core.pack.IPackUnpack;
import com.github.coerx.qarchiver.core.pack.SimplePackUnpackImpl;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.*;
import java.util.List;

import static com.github.coerx.qarchiver.common.Constants.TEMP_DIR_PATH;

public class PackAndCompressUtil {


    public static void packAndCompress(List<File> fileList, File targetFile) throws IOException {
        //OutputStream targetFileOutputStream = new BufferedOutputStream(new FileOutputStream(targetFile));

        IPackUnpack packUnpack = new SimplePackUnpackImpl();
        ICompressDecompress compressDecompress = new SimpleCompressAndDecompress();

        File tmpFile = new File(TEMP_DIR_PATH + "\\temp_file_" + RandomStringUtils.randomAlphanumeric(12) + ".mytar");
        tmpFile.createNewFile();
        OutputStream tmpFileOutputStream = new BufferedOutputStream(new FileOutputStream(tmpFile));

        packUnpack.pack(fileList, tmpFileOutputStream);
        tmpFileOutputStream.flush();
        tmpFileOutputStream.close();

        //compressDecompress.compress(new BufferedInputStream(new FileInputStream(tmpFile)), targetFileOutputStream);
        compressDecompress.compress(tmpFile.getAbsolutePath(), targetFile.getAbsolutePath());
//        targetFileOutputStream.flush();
//        targetFileOutputStream.close();
    }

    public static void deCompressAndUnpack(String sourceFile, String targetDir) throws IOException {
        File compressFile = new File(sourceFile);
        if (!compressFile.exists()) {
            System.out.println(compressFile.getPath() + " 压缩文件不存在");
            return;
        }
        File target = new File(targetDir);
        if (!target.exists() || !target.isDirectory()) {
            System.out.println("目标文件夹不存在或者不是文件夹");
            return;
        }
        File tmpFile = new File(TEMP_DIR_PATH + "\\temp_file_" + RandomStringUtils.randomAlphanumeric(12) + ".mytar");
        tmpFile.createNewFile();
        OutputStream tmpFileOutputStream = new BufferedOutputStream(new FileOutputStream(tmpFile));

        ICompressDecompress compressDecompress = new SimpleCompressAndDecompress();
        //todo
        //compressDecompress.decompress(new BufferedInputStream(new FileInputStream(compressdFile)), tmpFileOutputStream);
        compressDecompress.decompress(sourceFile,tmpFile.getAbsolutePath());
        tmpFileOutputStream.flush();
        tmpFileOutputStream.close();

        IPackUnpack packUnpack = new SimplePackUnpackImpl();
        packUnpack.unpack(new BufferedInputStream(new FileInputStream(tmpFile)), target);
    }

    public static void packAndCompressAndEncrypt(List<File> fileList, File targetFile, char[] password) throws IOException {
        File tmpFile = new File(TEMP_DIR_PATH + "/temp_file_no_encryption" + RandomStringUtils.randomAlphanumeric(12) + ".mytar.myz");
        tmpFile.createNewFile();
        packAndCompress(fileList, tmpFile);

        IEncryptAndDecrypt iEncryptAndDecrypt = new EncryptAndDecryptForAES128(password);
        FileOutputStream targetFileoutputStream = new FileOutputStream(targetFile);
        FileInputStream tmpFileInputStream = new FileInputStream(tmpFile);
        iEncryptAndDecrypt.encrypt(tmpFileInputStream, targetFileoutputStream);

        tmpFileInputStream.close();
        targetFileoutputStream.close();
    }

    public static void decryptAndDeCompressAndUnpack(File compressdFile, File rootDir, char[] password) throws IOException {
        File tmpFile = new File(TEMP_DIR_PATH + "\\temp_file_no_encryption" + RandomStringUtils.randomAlphanumeric(12) + ".mytar.myz");
        tmpFile.createNewFile();

        IEncryptAndDecrypt iEncryptAndDecrypt = new EncryptAndDecryptForAES128(password);
        FileInputStream compressdFileInputStream = new FileInputStream(compressdFile);
        FileOutputStream tmpFileOutputStream = new FileOutputStream(tmpFile);
        iEncryptAndDecrypt.decrypt(compressdFileInputStream, tmpFileOutputStream);
        compressdFileInputStream.close();
        tmpFileOutputStream.close();

        deCompressAndUnpack(tmpFile.getAbsolutePath(), rootDir.getAbsolutePath());
    }
}
