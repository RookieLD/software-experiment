package com.github.coerx.qarchiver.core.compress;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;
import java.util.*;


public class L7z implements ICompressDecompress {


    private ArrayList<String> Search = new ArrayList<>();
    private ArrayList<String> look_Ahead = new ArrayList<>();
    private ArrayList<Tags> tags = new ArrayList<Tags>();

    static long cost = 0l;

    String content = "";
    int SearchSize = 1024, look_AheadSize = 50;

    @Override
    public void compress(String OriginalPath, String CompressPath) throws IOException {
        long startTime = System.currentTimeMillis();
        tags.clear();
        Path path = Paths.get(OriginalPath);
        content = ReadFromFile(path);
        int indexofText = 0;
        for (; indexofText < content.length() && indexofText < look_AheadSize; indexofText++)
            look_Ahead.add(String.valueOf(content.charAt(indexofText)));// Fill look ahead buffer
        while (!look_Ahead.isEmpty()) {
            if (!Search.contains(look_Ahead.get(0))) {// Substring NotFound in searchbuffer
                Tags t = new Tags();
                t.set_Tags(0, 0, look_Ahead.get(0).toString());
                tags.add(t);
                // remove from Look ahead and add to Search
                Search.add(look_Ahead.get(0).toString());
                look_Ahead.remove(look_Ahead.get(0));

                if (indexofText < content.length()) {
                    look_Ahead.add(String.valueOf(content.charAt(indexofText++)));// Adding new character to the buffer
                }
            } else {// matching found
                // pos=end-index of matching;
                int pos = Search.size() - Search.lastIndexOf(look_Ahead.get(0));
                // len= try get matching from search and count length
                int len = 1; // default value
                int iter = 1;
                String LMatching = look_Ahead.get(0);
                String SMatching = Search.get(Search.size() - pos);
                // need to recheck condition
                while (LMatching.equals(SMatching) && iter < pos && iter < look_Ahead.size()) {
                    len++;
                    LMatching += look_Ahead.get(iter);
                    SMatching += Search.get(Search.size() - pos + iter);
                    iter++;
                }
                // nc=nc+content.charAt(indexofText++);
                String nc = Character.toString(LMatching.charAt(LMatching.length() - 1));
                Tags t = new Tags();
                t.set_Tags(pos, len - 1, nc); // define tag and set properties
                tags.add(t); // add it to array to print
                // shift by len+1
                for (; len > 0; len--) {
                    if (Search.size() == SearchSize)
                        Search.remove(0);// if buffer is full
                    Search.add(look_Ahead.get(0));
                    look_Ahead.remove(0);

                    if (indexofText < content.length()) {
                        // Adding new characters to the buffer
                        look_Ahead.add(String.valueOf(content.charAt(indexofText++)));
                    }
                }
            }
        }
        FileWriter out = new FileWriter(CompressPath);
        WriteToFile(out, tags);
        long endTime = System.currentTimeMillis();
        cost = endTime-startTime;
        System.out.println("花费时间：" +  cost + "s");
        return;
    }

    @Override
    public void decompress(String CompressPath, String DecompressPath) throws IOException {
        tags.clear();
        File inputFile = new File(CompressPath);
        FileWriter outputFile = new FileWriter(DecompressPath);
        Scanner scan = new Scanner(inputFile);
        String content = "";
        while (scan.hasNextLine()) { // still there is line to read
            String Tag = scan.nextLine();
            String arrofTags[] = Tag.split(",");
            int pos = Integer.parseInt(arrofTags[0]);
            int len = Integer.parseInt(arrofTags[1]);
            String nextChar = arrofTags[2];
            if (pos == 0 && len == 0)// first time appear in the original
                content += nextChar; // adding char to content
            else { // appeard once before at leasts
                String o = "";
                int beginofSubString = content.length() - pos;
                // there is position value , so go back p values to get char
                //o += content.charAt(beginofSubString);
                // take length substring and add it
                o += content.substring(beginofSubString, beginofSubString + len);
                // add nextchar
                o += nextChar;
                content += o;
            }
        }
        outputFile.write(content);//print into file
        scan.close();
        outputFile.close();
    }

    //显示压缩率
    public   float showCompressRate(String OriginalPath,String CompressPath) {  // 显示压缩率
        File file1 = new File(OriginalPath);
        File file2 = new File(CompressPath);
        float file1Size = file1.length();
        float file2Size = file2.length();

        return (file2Size/file1Size);


    }

    //Write Tags to file
    private void WriteToFile(FileWriter out, ArrayList<Tags> tags) throws IOException {
        for (int i = 0; i < tags.size(); i++)
            out.write(tags.get(i).toString());
        out.close();
    }

    private String ReadFromFile(Path path) throws IOException {
        return Files.readAllLines(path, StandardCharsets.UTF_8).toString().replace("\n", "").replace("\r", "");
        // remove carrige return and line feed
    }

    //Check if Original data = Decompressed data
    public boolean isEqual(String OriginalPath, String DecompressPath) throws IOException {
        String Original = ReadFromFile(Paths.get(OriginalPath));
        String ResultDecompress = ReadFromFile(Paths.get(DecompressPath));
        return Original.equals(ResultDecompress) ? true : false;
    }

    public class Tags {
        private int Position;
        private int Length;
        private String nextchar;
        Tags(){
            Position=0;
            Length=0;
            nextchar="";
        }
        public void set_Tags(int p,int l,String nc){
            Position=p;
            Length=l;
            nextchar=nc;
        }
        public String toString(){
            return +Position+","+Length+","+nextchar+System.lineSeparator();
        }
    }
}
