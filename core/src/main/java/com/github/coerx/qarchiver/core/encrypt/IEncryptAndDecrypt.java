package com.github.coerx.qarchiver.core.encrypt;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 加密解密接口
 */
public interface IEncryptAndDecrypt {
    /**
     * 加密
     *
     * @param inputSteam   原始字节流
     * @param outputStream 加密后的字节流
     */
    public void encrypt(InputStream inputSteam, OutputStream outputStream);

    /**
     * 解密
     *
     * @param inputStream  加密字节流
     * @param outputStream 解密后的字节流
     */
    public void decrypt(InputStream inputStream, OutputStream outputStream);
}
