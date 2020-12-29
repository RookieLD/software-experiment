package huffmanzip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Scanner;
import java.io.*;


public class TestHuffmanCode {

	public static void main(String[] args) {
		 
		/** 下面是在huffman编码基础 实现通过huffman编码解码的文件压缩功能 **/
		//1.压缩文件
		System.out.println("请输入路径:（例如D:\\zip\\word1.docx");
		Scanner scanner = new Scanner(System.in);
		String path  = scanner.next();
		
		File file = new File(path);
        String strParentDirectory = file.getParent();
        System.out.println("文件目录 : " + strParentDirectory);	//path

        String src = path;
		String fn = path.substring(src.lastIndexOf("\\")+1);
		String filename = fn.replaceAll("\\\\","/");;
		System.out.println("文件名为 : "+filename);
		
    	
		String dst = strParentDirectory +"\\"+filename +".rar ";
		System.out.println("压缩目录 : "+dst);
		String newdst = strParentDirectory + "\\new"+filename;
		System.out.println("解压目录 : "+newdst);
		
//		String src = "D:\\zip\\word3.docx";
//		String dst = "D:\\zip\\word3.rar";
//		String newdst = "D:\\zip\\word3.docx";
		try {
			zipFile(src, dst);
		} catch (IOException e) {
			// TODO 自动生成 catch 
			e.printStackTrace();
		}
		
		//2.解压文件
		try {
			unZip(dst,newdst);
		} catch (FileNotFoundException e) {
			// TODO 自动生成 catch 
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成 catch 
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO 自动生成 catch 
			e.printStackTrace();
		}
	}
	
	/**
	 * 文件的解
	 * @param src
	 * @param dst
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void unZip(String src,String dst) throws IOException, ClassNotFoundException {
		//先从src中读取内
		FileInputStream fis = new FileInputStream(src);
		ObjectInputStream ois = new ObjectInputStream(fis);
			//读取要分两步，并且按照写入的顺序进行读取
		//1）读取先写入的byte数组
		byte[] b = (byte[])ois.readObject();
		//2）读取后写入的huffCodes
		HashMap<Byte,String> huffcodes = (HashMap)ois.readObject();
		ois.close();
		fis.close();
		
		//始解
		byte[] by = decode(huffcodes,b);
		
		//写入文件
		FileOutputStream fos = new FileOutputStream(dst);
		fos.write(by);
		fos.close();
	}

	/**
	 * 压缩文件
	 * 
	 * @param src
	 * @param dst
	 */
	private static void zipFile(String src, String dst) throws IOException {
		// 首先创建个输入流,从src文件中读
		FileInputStream fis = new FileInputStream(src);
		// 然后声明个和目标文件样大的byte数组，用于一次读
		byte[] b = new byte[fis.available()];
		// 读取
		fis.read(b);
		fis.close();
		// 对读取的文件byte数组进行huffman编码
		byte[] byteZip = huffmanZip(b);
		System.out.println("b.length:"+b.length);
		System.out.println("byteZip.length:"+byteZip.length);

		// 输出流，将编码后的codes写入到文件dst
		FileOutputStream fos = new FileOutputStream(dst);
		// FileOutputStream是字节流的写，但我们还要把自定义类型的huffcodes写入，这样才能解码，以需要用到ObjectOutputStream!
		// ObjectOutputStream的构造方法中，传入的参数不是文件，是output流！以还是得先用FileOutputStream创建个文件输出流
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		//写入的顺序就是之后的读取顺序
		oos.writeObject(byteZip);
		oos.writeObject(huffCodes);
		oos.close();
		fos.close();
	}

	/**
	 * 使用指定的哈夫曼编码表，进行哈夫曼解
	 * 
	 * @param huffCodes 哈夫曼编码表
	 * @param bytes     待解
	 * @return
	 */
	static ArrayList<Byte> decodes = new ArrayList<Byte>();

	private static byte[] decode(Map<Byte, String> huffCodes, byte[] bytes) {
		StringBuilder sb = new StringBuilder();// 用于装转二进制后的新字符
		// 1.把byte数组转为个二进制的字符串
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			boolean flag = (i == bytes.length - 1);
//			String s = Integer.toBinaryString(b);//不能直接转，因为Integer.toBinaryString(int i)方法默认传入的数据转int再转二进制>而int在计算机中占32
			// 且计算机中整数都是用补码表示的，以负数就会有32;而正数前面的0则全被舍了，不足8位>想要全都8
			// solve way：负数取后八位（前面的全1舍掉），正数不足八位的补0
			String s = byteToBitStr(!flag, b);
			// 但是又出现了个问题，bytes中最后一个元素可能不8位，且不能补0—>在byteToBitStr中增设一个flag告诉我要不要
			sb.append(s);
		}
		System.out.println("Huanyuan:" + sb.toString());//找到问题了，后一个字符，因为8位，如果其首位有0，我们又不补8位，就会删掉其首位的0，所以从还原就开始错了，
		//以要从更早：即一始将0111二进制码存成数字的位置，就要记录后一段（不足8位）长度
		// 2.把字符串按照指定的哈夫曼编码进行解码
		// (1)首先要把哈夫曼编码的HashMap键对调换—>实现通过key的编 get到对应编码的value字符
		Map<String, Byte> map = new HashMap<String, Byte>();
		Set<Entry<Byte, String>> entry = huffCodes.entrySet();
		for (Entry<Byte, String> en : entry) {
			map.put(en.getValue(), en.getKey());
		}
		// (2)处理字符串，解码
		for (int i = 0; i < sb.length();) {
			int count = 1;// 因为不知道多少位数字会解码出个原字符，所以要记录
			boolean flag = true;// 用于判断 每截取一个字 是否匹配成功，成功则跳出，进入下轮；否则count++继续
			// 每个while能成功译码一个kv match
			while (flag) {
				// 通过当前长度的码作key去get，如果为null则匹配不成功
				String s = sb.substring(i, i + count);// subString是取到i+count的前
				Byte b = map.get(s);
				if (b == null) {
					// 匹配失败
					count++;
				} else {
					// 匹配成功
//					System.out.println(b);//可以从这里打印观察到取得基本都是我们原对应字符的unicode码，只需要放入byte数组，再变换回字符串即可
					// 但是有一个问题：byte数组的长度未...—>以不能用byte数组放，不知道长度无法初始化—>用一个集合来
					System.out.println("解释出字符："+b);
					decodes.add(b);
					flag = false;
				}
			}
			i += count;
		}
		System.out.println("decodes list:" + decodes.toString());// 这里打印可以看到已经存入了一个list，再据此翻译成字符即
		byte[] res = new byte[decodes.size()];
		for (int i = 0; i < decodes.size(); i++) {
			res[i] = decodes.get(i);
		}
		return res;
	}

	// 实现补全
	public static String byteToBitStr(boolean flag, byte b) {
		int temp = b;
		if (flag) {// 如果非最后一个byte，则要补
//			System.out.println("temp或前"+Integer.toBinaryString(temp));
			temp |= 256;// 2561 0000 0000;以按位或出来的后八位就是我们要的—>主要针对的是不足8位的正数，补全成9位，且第=1，所以后八位我们要得，就0打头，也不会被丢
//			System.out.println("temp或后"+Integer.toBinaryString(temp));
		}
		String s = Integer.toBinaryString(temp); //如果是最后一个byte则直接转二进制给s(后一个byte8位时要单独虑)，否则先256按位
		if (flag) {// 如果非最后一个byte
			System.out.println("bytes:"+s.substring(s.length() - 8));
			return s.substring(s.length() - 8);
		} else {
			//必须要根据lastbyteLength存的原最后一位的长度进行还原，因为其可能0头，且最后一项不用补8位，有开头的0都会被抛弃，从导致译码错
			//但是还要考虑，如果最后一个byte为负，则前面1，所以要
			System.out.println("lastbyteLength:"+lastbyteLength);
			if(lastbyteLength==8) {//如果后一个byte恰好8位，不补，直接取
				return s.substring(s.length()-8);
			}
			if(s.length()<lastbyteLength) {
				StringBuilder strb = new StringBuilder(s);
				for(int i=0;i<lastbyteLength-s.length();i++) {
					strb.insert(0,"0");
				}
				s = strb.toString();
			}
			System.out.println("last bytes"+s);
			return s;
		}
	}

	/**
	 * 进行huffman编码压缩的方
	 * 
	 * @param bytes
	 * @return
	 */
	private static byte[] huffmanZip(byte[] bytes) {
		// 以下四步都较复杂，所以各写一个方
		// 1.统计原byte数组中各字符出现的频,并放入一个集合中—>节点权weight;并且按该权进行排
		List<Node> nodes = getNodes(bytes);
//		for(int i=0;i<nodes.size();i++) {
//			System.out.println(nodes.get(i));
//		}
		// 2.创建颗huffman
		Node tree = createHuffmanTree(nodes);
		// 3.创建个哈夫曼编码
		Map<Byte, String> huffCodes = getCodes(tree);
		System.out.println("huffCodes:" + huffCodes);
		// 4.编码
		byte[] b = zip(bytes, huffCodes);
		return b;
	}

	/**
	 * 进行哈夫曼编 把每个byte根据huffCodes进行转换，然后拼
	 * 
	 * @param bytes
	 * @param huffCodes
	 * @return
	 */
	static int lastbyteLength = 8;//用于记录后一个不足八位的byte的length，以防将其从二进制码转为数字时，首位0丢失//default=8，才能处理最后一个byte正好8位的情况
	private static byte[] zip(byte[] bytes, Map<Byte, String> huffCodes) {
		// 用sb去装编码后的byte,把需要压缩的byte数组变成了一个二进制字符
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(huffCodes.get(b));
		}
		System.out.println("哈夫曼编码后，对应huffcode(length:" + sb.length() + "):" + sb);
		// 接下来需要把编码后的字符串转为一个byte数组（每8位转成一个byte
		int len = (sb.length() + 7) / 8;
		byte[] by = new byte[len];
		// 使用个index记录当前存入by数组的下
		int index = 0;
		for (int i = 0; i < sb.length(); i += 8) {
			String subStr;
			if ((i + 8) > sb.length()) {//如果恰好后也8位，就不会进入这
				subStr = sb.substring(i);
				lastbyteLength = sb.length()-i;//记录如果首位0，不能因为不补全8位就被抛
				System.out.println("subStr last:"+subStr);
			} else {
				subStr = sb.substring(i, i + 8);
			}
			// 把切割好的，每八位数字的字符串，以二进制转换成一个新的数
			byte byt = (byte) Integer.parseInt(subStr, 2);
//			System.out.println("subStr:"+subStr+",byte:"+byt);
			by[index] = byt;
			index++;
		}
		return by;
	}

	/**
	 * 输入棵huffman树，创建个huffman编码 要想编码，必要遍历，以必定会用到递归
	 * 
	 * @param tree
	 * @return
	 */
	// 要对节点编码，则必须记录每一次走过的每一条路径，以在外面设置个StringBuilder静成员变量，用于记录
	static StringBuilder sb = new StringBuilder();

	private static Map<Byte, String> getCodes(Node tree) {
		if (tree == null) {
			return null;
		}
		getCodes(tree.left, "0", sb); // 每次左儿子添0，并且在递归入口传入之前路径的记录>再另个函数用于归
		getCodes(tree.right, "1", sb);
		return huffCodes;
	}

	// 用于huffman编码过程的归遍历
	// 要一个HashMap成员变量来存储已经编码好的字符及其对应路
	static Map<Byte, String> huffCodes = new HashMap<Byte, String>();

	private static void getCodes(Node node, String code, StringBuilder sb) {
		if (node == null) {
			return;
		}
		StringBuilder sb2 = new StringBuilder(sb);
		sb2.append(code);
		// 每次递归的一个节点，都要查此节点的data是否为null，如果不为null，则要将其插入huffman编码的Map
		if (node.data == null) {
			getCodes(node.left, "0", sb2);
			getCodes(node.right, "1", sb2);
		} else {
			// 将此节点及当前路径String插入个hashmap
			huffCodes.put(node.data, sb2.toString());
		}
	}

	/**
	 * 用于创建huffman
	 * 
	 * @param nodes
	 * @return
	 */
	private static Node createHuffmanTree(List<Node> nodes) {
		// 循环取排序后的末尾两个节
		while (nodes.size() > 1) {
			// 对list进行排序
			Collections.sort(nodes);
			// 取最小的两个分别做左右子进行融合
			Node left = nodes.get(nodes.size() - 1);
			Node right = nodes.get(nodes.size() - 2);
			Node parent = new Node(null, left.weight + right.weight);
			// 移除两个被融合的
			nodes.remove(left);
			nodes.remove(right);
			// 修改parent指向
			parent.left = left;
			parent.right = right;
			// 融合后的节点加入list
			nodes.add(parent);
		}
		return nodes.get(0);
	}

	/**
	 * 把byte数组转为node集合
	 * 
	 * @param bytes
	 * @return
	 */
	private static List<Node> getNodes(byte[] bytes) {
		// 1.把byte装入node—>注意可能有重复的！所以不能只用list—>hashmap
		List<Node> nodes = new ArrayList<Node>();
		// 2.HashMap统计每一个byte出现的次数（key：byte value：出现次数）
		Map<Byte, Integer> counts = new HashMap<Byte, Integer>();
		for (byte b : bytes) {
			// 首先根据b，这个key去取value
			Integer count = counts.get(b);
			// 分情况：1）可能是第一次出现迭代的这个b对象
			if (count == null)// 有java包装类的默认初始值都=null
			{
				count = 1;
				counts.put(b, count);
			}
			// 2）不是第次出现了，直接对count++
			else {
				count++;
				counts.put(b, count);
			}
			// 迭代结束即可统计出各字符出现的频
		}
		// 打印结果，方法一：HashMap重写过toString方法，所以可以直接打印该HashMap对象
		System.out.println("each byte weight:" + counts);

		// 3.统计完出现次数后，需要转为节
		for (Map.Entry<Byte, Integer> entry : counts.entrySet()) {
			// 把每个键值对，存成data和weight，放入创建的ArrayList中；以便后续根据该list生成哈夫曼树
			nodes.add(new Node(entry.getKey(), entry.getValue()));
		}
		return nodes;
	}
}

