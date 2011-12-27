/* The Great Computer Language Shootout
   http://shootout.alioth.debian.org/

   contributed by Isaac Gouy
*/


public class takfp {
    public static void main(String args[]) {
        int n = Integer.parseInt(args[0]);
        System.out.println( Tak(n*3.0f, n*2.0f, n*1.0f) );
    }

    public static float Tak (float x, float y, float z) {
        if (y >= x) return z;
        return Tak(Tak(x-1.0f,y,z), Tak(y-1.0f,z,x), Tak(z-1.0f,x,y));
    }
}
