package com.github.coerx.qarchiver.core.compress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 定义压缩解压缩的接口
 */
//todo 因为项目中的lzw的实现的实际压缩效果不好，甚至越压越大，建议实现deflate算法
public interface ICompressDecompress {

    /**
     * @param inputStream  正常的字节流
     * @param outputStream 输出被压缩的字节流
     */
    //public void compress(InputStream inputStream, OutputStream outputStream);
    public void compress(String source,String dest) throws IOException;

    /**
     * @param inputStream  压缩过的字节流
     * @param outputStream 输出解压后的字节流
     */
    //public void decompress(InputStream inputStream, OutputStream outputStream);
    public void decompress(String source,String dest) throws IOException, ClassNotFoundException;

}
