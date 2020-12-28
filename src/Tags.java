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
