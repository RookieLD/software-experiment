public class App {
    public static void main(String[] args) throws Exception {
       String OriginalPath1 = "resource/OriginalText.txt";
       String CompressPath = "resource/CompressedText.lz77";
       String DecompressPath = "resource/DecompressedText.txt";
        LZ77 l=new LZ77();
        long time=l.Compress(OriginalPath1,CompressPath);
        System.out.println("压缩时间：="+time+"ms");
        float compressRate = l.showCompressRate(OriginalPath1, CompressPath);
        System.out.println("压缩率:="+compressRate* 100 + "%");
        l.Decompress(CompressPath,DecompressPath);

    }
}
