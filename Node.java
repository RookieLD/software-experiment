package huffmanzip;

//Node的定义
public class Node implements Comparable<Node>{
	Byte data;//该节点所代表的字符——>但是该huffmantree并不是所有节点都有data的！有些融合点，是不含字符的，所以要允许=null，所以用byte的封装类Byte来写
	int weight;//权值，出现频率
	Node left;
	Node right; 
	
	public Node(Byte data,int weight) {
		this.data = data;
		this.weight = weight;
	}
	
	public String toString() {
		return "data:"+data+",weight:"+weight;
	}
	
	public int compareTo(Node o) {
		if(this.weight<o.weight) return 1;//这样才是从大到小排序（正常这里一般-1，实现从小到大排序）
		if(this.weight==o.weight) return 0;
		else return -1;
	}
}

