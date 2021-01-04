package com.github.coerx.qarchiver.core.compress;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 算法参考LZW <br>
 * See <a href="https://segmentfault.com/a/1190000011425787">https://segmentfault.com/a/1190000011425787</a><br>
 * See <a href="https://en.wikipedia.org/wiki/Lempel%E2%80%93Ziv%E2%80%93Welch">Lempel–Ziv–Welch</a><br>
 * 实现上压缩输出的每个symbol占12bit,所以字典的大小是0-4095<br>
 * <p>
 * 测试发现LZW对包含随机字节的数组进行压缩，压缩得到的字节数组反而比原字节数组更大<br>
 * 对文本文件的压缩效果比较好，一个222661字节的html文件压缩后为143023字节
 */
public class SimpleCompressAndDecompress implements ICompressDecompress {

    private HashMap<String,Integer> dict = new HashMap<String, Integer>();
    private String[] arr_char;
    private int count;

    public SimpleCompressAndDecompress(){
    }

    public String to12bit(int i) {
        String temp = Integer.toBinaryString(i);
        while (temp.length() < 12) {
            temp = "0" + temp;
        }
        return temp;
    }


    public int getvalue(byte b1, byte b2, boolean onleft) {
        String temp1 = Integer.toBinaryString(b1);
        String temp2 = Integer.toBinaryString(b2);
        while (temp1.length() < 8) {
            temp1 = "0" + temp1;
        }
        if (temp1.length() == 32) {
            temp1 = temp1.substring(24, 32);
        }
        while (temp2.length() < 8) {
            temp2 = "0" + temp2;
        }
        if (temp2.length() == 32) {
            temp2 = temp2.substring(24, 32);
        }

        /** On left being true */
        if (onleft) {
            return Integer.parseInt(temp1 + temp2.substring(0, 4), 2);
        } else {
            return Integer.parseInt(temp1.substring(4, 8) + temp2, 2);
        }

    }

    @Override
    public void compress(String input,String output) throws IOException {
        byte input_byte;
        String prev = "";
        byte[] buffer = new byte[3];
        boolean onleft = true;
        arr_char = new String[4096];

        long startTime=System.currentTimeMillis();//记录开始时间
        //初始化字典
        for (int i = 0; i < 256; i++){
            dict.put(Character.toString((char) i),i);
            arr_char[i]=Character.toString((char) i);
        }
        count = 256;

        DataInputStream read = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(input)));
        DataOutputStream write = new DataOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(output)));

        try {
            input_byte = read.readByte();
            int i = new Byte(input_byte).intValue();
            if (i < 0){
                i += 256;
            }
            char c = (char) i;
            prev = ""+ c;

            while (true){
                input_byte = read.readByte();
                i = new Byte(input_byte).intValue();

                if (i < 0){
                    i += 256;
                }
                c = (char) i;

                if (dict.containsKey(prev + c)){
                    prev =prev+c;
                }else {
                    String pw = to12bit(dict.get(prev));
                    if (onleft){
                        buffer[0]=(byte)Integer.parseInt(
                                pw.substring(0,8),2);
                        buffer[1]=(byte)Integer.parseInt(
                                pw.substring(8,12)+"0000",2);

                    }else {
                        buffer[1]+=(byte)Integer.parseInt(
                                pw.substring(0,4),2);
                        buffer[2]=(byte)Integer.parseInt(
                                pw.substring(4,12),2);
                        for (int j = 0;j<buffer.length;j++){
                            write.writeByte(buffer[j]);
                        }
                    }
                    onleft =!onleft;
                    if (count<4096){
                        dict.put(prev+c,count++);
                    }
                    prev = ""+c;
                }
            }
        }catch (EOFException e){
            String pw_12 = to12bit(dict.get(prev));
            if (onleft){
                buffer[0]=(byte)Integer.parseInt(
                        pw_12.substring(0,8),2);
                buffer[1]=(byte)Integer.parseInt(
                        pw_12.substring(8,12)+"0000",2);
                write.writeByte(buffer[0]);
                write.writeByte(buffer[1]);
            }else {
                buffer[1]+=(byte)Integer.parseInt(
                        pw_12.substring(0,4),2);
                buffer[2]=(byte)Integer.parseInt(
                        pw_12.substring(4,12),2);
                for (int j = 0;j<buffer.length;j++){
                    write.writeByte(buffer[j]);
                }
            }
            read.close();
            write.close();
        }
        long endTime=System.currentTimeMillis();//记录结束时间
        System.out.println("压缩时间： "+(endTime-startTime)+"ms");
    }

    @Override
    public void decompress(String input, String output) throws IOException {
        arr_char = new String[4096];
        for (int i = 0; i < 256; i++) {
            dict.put(Character.toString((char) i), i);
            arr_char[i] = Character.toString((char) i);
        }
        count = 256;

        DataInputStream in = new DataInputStream(new BufferedInputStream(
                new FileInputStream(input)));

        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
                new FileOutputStream(output)));

        int currword, priorword;
        byte[] buffer = new byte[3];
        boolean onleft = true;
        try {

            buffer[0] = in.readByte();
            buffer[1] = in.readByte();

            priorword = getvalue(buffer[0], buffer[1], onleft);
            onleft = !onleft;
            out.writeBytes(arr_char[priorword]);

            while (true) {

                if (onleft) {
                    buffer[0] = in.readByte();
                    buffer[1] = in.readByte();
                    currword = getvalue(buffer[0], buffer[1], onleft);
                } else {
                    buffer[2] = in.readByte();
                    currword = getvalue(buffer[1], buffer[2], onleft);
                }
                onleft = !onleft;
                if (currword >= count) {

                    if (count < 4096)
                        arr_char[count] = arr_char[priorword]
                                + arr_char[priorword].charAt(0);
                    count++;
                    out.writeBytes(arr_char[priorword]
                            + arr_char[priorword].charAt(0));
                } else {

                    if (count < 4096)
                        arr_char[count] = arr_char[priorword]
                                + arr_char[currword].charAt(0);
                    count++;
                    out.writeBytes(arr_char[currword]);
                }
                priorword = currword;
            }

        } catch (EOFException e) {
            in.close();
            out.close();
        }

    }

//    private static String intToBinaryString(int num) {
//        String str = Integer.toBinaryString(num);
//        if (str.length() > 12) throw new RuntimeException("num must less than 4096");
//        while (str.length() < 12) {
//            str = "0" + str;
//        }
//        return str;
//    }
//
//    @Override
//    public void compress(InputStream inputStream, OutputStream outputStream) {
//        //todo dict的key应该是一个字节数组。不应该用字符串替代字节数组
//        Map<String, Integer> dict = new HashMap<>();
//        for (int i = 0; i < 256; i++) {
//            dict.put(String.valueOf((char) i), i);
//        }
//        String pre = "";
//        String cur = "";
//        int count = 256;
//        int next;
//        //每个记号占12bit，两个记号3个字节，每两个一起输出到输出流
//        byte[] buffer = new byte[3];
//        boolean needWrite = false;
//        try {
//            while ((next = inputStream.read()) != -1) {
//                //即便next为0也没有问题
//                cur = String.valueOf((char) next);
//                if (dict.containsKey(pre + cur)) {
//                    pre = pre + cur;
//                    continue;
//                }
//                //p+c不再字典中，将p的记号输出
//                Integer symbol = dict.get(pre);
//                String symbolBinary = intToBinaryString(symbol);
//                if (!needWrite) {
//                    buffer[0] = (byte) Integer.parseInt(symbolBinary.substring(0, 8), 2);
//                    buffer[1] = (byte) Integer.parseInt(symbolBinary.substring(8, 12) + "0000", 2);
//                    needWrite = true;
//                } else {
//                    buffer[1] += (byte) Integer.parseInt(symbolBinary.substring(0, 4), 2);
//                    buffer[2] = (byte) Integer.parseInt(symbolBinary.substring(4, 12), 2);
//                    outputStream.write(buffer);
//                    //outputStream.flush();
//                    needWrite = false;
//                }
//                if (count < 4096) {
//                    dict.put(pre + cur, count++);
//                }
//                pre = cur;
//            }
//            if (pre.equals("")) throw new RuntimeException("输入流中没有数据");
//            //循环结束时，pre不为空，最后剩下的pre还没有输出
//            int symbol = dict.get(pre);
//            String symbolBinary = intToBinaryString(symbol);
//            if (!needWrite) {
//                buffer[0] = (byte) Integer.parseInt(symbolBinary.substring(0, 8), 2);
//                buffer[1] = (byte) Integer.parseInt(symbolBinary.substring(8, 12) + "0000", 2);
//                outputStream.write(buffer[0]);
//                outputStream.write(buffer[1]);
//            } else {
//                buffer[1] += (byte) Integer.parseInt(symbolBinary.substring(0, 4), 2);
//                buffer[2] = (byte) Integer.parseInt(symbolBinary.substring(4, 12), 2);
//                outputStream.write(buffer);
//            }
//            outputStream.flush();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        File file = new File("cc");
//
//    }
//
//    @Override
//    public void decompress(InputStream inputStream, OutputStream outputStream) {
//        try {
//            //字典数组, 数组的每一个元素是一个字节数组，数组的下标代表symbol，数组的值是一个字节数组，代表当前下标对应的symbol的解码后的字节
//            byte[][] dict = new byte[4096][];
//            int count = 256;
//            for (int i = 0; i < count; i++) {
//                dict[i] = new byte[1];
//                dict[i][0] = (byte) i;
//            }
//            int byte1 = inputStream.read();
//            int byte2 = inputStream.read();
//            if (byte1 == -1 || byte2 == -1) throw new RuntimeException("输入流缺失数据");
//            //cw是symbol，每个symbol是12bit
//            int cw = (byte1 << 4) + (byte2 >> 4);
//            outputStream.write(dict[cw]);
//            //outputStream.flush();
//            int pw = cw;
//            boolean needLastByte = true;
//            int byteCount = 0;
//            while ((byte1 = inputStream.read()) != -1) {
//                //当前读的字节需要和上一个字节组成12bit的symbol
//                if (needLastByte) {
//                    cw = ((byte2 & 0x0f) << 8) + byte1;
//                    needLastByte = false;
//                } else {
//                    byte2 = inputStream.read();
//                    if (byte2 == -1) throw new RuntimeException("输入流缺失数据");
//                    cw = (byte1 << 4) + (byte2 >> 4);
//                    needLastByte = true;
//                }
//
//                byte[] p;
//                byte c;
//                if (dict[cw] != null) {
//                    outputStream.write(dict[cw]);
//                    p = dict[pw];
//                    c = dict[cw][0];
//                    //byteCount += dict[cw].getBytes().length;
//                } else {
//                    p = dict[pw];
//                    c = dict[pw][0];
//                    outputStream.write(p);
//                    outputStream.write(c);
//                    //byteCount += (p+c).getBytes().length;
//                }
//                //outputStream.flush();
//                if (count < 4096) {
//                    dict[count] = Arrays.copyOf(p, p.length + 1);
//                    dict[count][dict[count].length - 1] = c;
//                    count++;
//                }
//                pw = cw;
//            }
//            outputStream.flush();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//    }
}
