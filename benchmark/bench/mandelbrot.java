/* The Computer Language Benchmarks Game
   http://shootout.alioth.debian.org/
   contributed by Stefan Krause
   slightly modified by Chad Whipkey
*/

import java.io.IOException;
import java.io.PrintStream;

class mandelbrot {

   public static void main(String[] args) throws Exception {
       new Mandelbrot(Integer.parseInt(args[0])).compute();
   }

   public static class Mandelbrot {
       private static final int BUFFER_SIZE = 8192;

       public Mandelbrot(int size) {
         this.size = size;
         fac = 2.0 / size;
         out = System.out;
         shift = size % 8 == 0 ? 0 : (8- size % 8);
      }
      final int size;
      final PrintStream out;
      final byte [] buf = new byte[BUFFER_SIZE];
      int bufLen = 0;
      final double fac;
      final int shift;

      public void compute() throws IOException
      {
         out.format("P4\n%d %d\n",size,size);
         for (int y = 0; y<size; y++)
            computeRow(y);
         out.write( buf, 0, bufLen);
         out.close();
      }

      private void computeRow(int y) throws IOException
      {
         int bits = 0;

         final double Ci = (y*fac - 1.0);
          final byte[] bufLocal = buf;
          for (int x = 0; x<size;x++) {
            double Zr = 0.0;
            double Zi = 0.0;
            double Cr = (x*fac - 1.5);
            int i = 50;
            double ZrN = 0;
            double ZiN = 0;
            do {
               Zi = 2.0 * Zr * Zi + Ci;
               Zr = ZrN - ZiN + Cr;
               ZiN = Zi * Zi;
               ZrN = Zr * Zr;
            } while (!(ZiN + ZrN > 4.0) && --i > 0);

            bits = bits << 1;
            if (i == 0) bits++;

            if (x%8 == 7) {
                bufLocal[bufLen++] = (byte) bits;
                if ( bufLen == BUFFER_SIZE) {
                    out.write(bufLocal, 0, BUFFER_SIZE);
                    bufLen = 0;
                }
               bits = 0;
            }
         }
         if (shift!=0) {
            bits = bits << shift;
            bufLocal[bufLen++] = (byte) bits;
            if ( bufLen == BUFFER_SIZE) {
                out.write(bufLocal, 0, BUFFER_SIZE);
                bufLen = 0;
            }
         }
      }
   }
}
