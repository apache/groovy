List list = new ArrayList();
List list2 = new java.util.ArrayList();
List<String> list3 = new ArrayList<String>();
List<String> list4 = new java.util.ArrayList<String>();
List<String> list5 = new ArrayList<>();
//List<String> list6 = new java.util.ArrayList<>(); // the old parser can not parse "new java.util.ArrayList<>()"
def x = new A<EE, TT>();
int[] a = new int[10];
int[][] b = new int[length()][2 * 8];
ArrayList[] c = new ArrayList[10];
ArrayList[][] cc = new ArrayList[10][size()];
java.util.ArrayList[] d = new java.util.ArrayList[10];
ArrayList[] e = new ArrayList<String>[10];
java.util.ArrayList[] f = new java.util.ArrayList<String>[10];
java.util.ArrayList[] g = new java.util.ArrayList<String>[size()];

int[][] h = new int[10][];
int[][][] i = new int[10][][];
ArrayList[][] j = new ArrayList[10][];
ArrayList[][] k = new ArrayList<String>[10][];

def bb = new A.B();
def bb2 = new A.B[0];

new
    A
        ('x', 'y');


new a();
new $a();
new as.def.in.trait.a();
