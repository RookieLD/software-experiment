import java.io.*;
import java.util.HashMap;

public class LZWCompression {

    private HashMap<String,Integer> dict = new HashMap<String, Integer>();
    private String[] arr_char;
    private int count;

    public LZWCompression(){
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

    public void LZW_compress(String input,String output) throws IOException {
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
    public void LZW_Decompress(String input, String output) throws IOException {
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
    /*public static void main(String[] args)throws IOException{
        LZWCompression t = new LZWCompression();
        t.LZW_compress("C:\\Users\\74710\\Desktop\\Compress-software-master.zip","C:\\Users\\74710\\Desktop\\Compress-software-master.zip.lzw");
        //t.LZW_Decompress("C:\\Users\\74710\\Desktop\\test.lzw","C:\\Users\\74710\\Desktop\\test_lzw");
    }*/
}

