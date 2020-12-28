请仔细阅读以下内容，方便实用：
LZ77和Tags为工具类，
App目的是测试工具的类的可行性
LZ7中的接口函数为
/OriginalPath 源文件 路径
CompressPath 目的文件路径lz77
return  压缩时间
public long Compress(String OriginalPath, String CompressPath)





/*CompressPath 源文件路径lz77
DecompressPath目的文件路径
*/
public void Decompress(String CompressPath, String DecompressPath)





/*OrifinalPath源文件路径
CompressPath压缩文件路径
return 压缩率
*/

public   float showCompressRate(String OriginalPath,String CompressPath) 
