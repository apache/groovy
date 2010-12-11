/* The Great Computer Language Shootout
   http://shootout.alioth.debian.org/
 
   contributed by Isaac Gouy
*/

import java.math.BigInteger;

public class pidigits {
   static final int L = 10;

   public static void main(String args[]) { 
      int n = Integer.parseInt(args[0]);
      int j = 0;
   
      PiDigitSpigot digits = new PiDigitSpigot();
      
      while (n > 0){
         if (n >= L){
            for (int i=0; i<L; i++) System.out.print( digits.next() );
            j += L;
         } else {
            for (int i=0; i<n; i++) System.out.print( digits.next() );
            for (int i=n; i<L; i++) System.out.print(" ");  
            j += n;   
         }
         System.out.print("\t:"); System.out.println(j);
         n -= L;           
      }               
   }
}


class PiDigitSpigot {
   Transformation z, x, inverse;            
       
   public PiDigitSpigot(){
      z = new Transformation(1,0,0,1);
      x = new Transformation(0,0,0,0);
      inverse = new Transformation(0,0,0,0);
   }   
   
   public int next(){
      int y = digit();
      if (isSafe(y)){ 
         z = produce(y); return y;
      } else {
         z = consume( x.next() ); return next();   
      }
   }    
      
   public int digit(){
      return z.extract(3);
   }        
   
   public boolean isSafe(int digit){
      return digit == z.extract(4);
   }   
   
   public Transformation produce(int i){
      return ( inverse.qrst(10,-10*i,0,1) ).compose(z);
   }     
      
   public Transformation consume(Transformation a){
      return z.compose(a);
   }                   
} 


class Transformation {
   BigInteger q, r, s, t;
   int k;              
       
   public Transformation(int q, int r, int s, int t){
      this.q = BigInteger.valueOf(q);
      this.r = BigInteger.valueOf(r);
      this.s = BigInteger.valueOf(s);
      this.t = BigInteger.valueOf(t);                  
      k = 0;
   }
   
   public Transformation(BigInteger q, BigInteger r, BigInteger s, BigInteger t){
      this.q = q;
      this.r = r;
      this.s = s;
      this.t = t;                  
      k = 0;
   }        
   
   public Transformation next(){
      k++;
      q = BigInteger.valueOf(k);
      r = BigInteger.valueOf(4 * k + 2);
      s = BigInteger.valueOf(0);
      t = BigInteger.valueOf(2 * k + 1); 
      return this;                 
   }      
   
   public int extract(int j){
      BigInteger bigj = BigInteger.valueOf(j);
      BigInteger numerator = (q.multiply(bigj)).add(r);
      BigInteger denominator = (s.multiply(bigj)).add(t);                  
      return ( numerator.divide(denominator) ).intValue();                    
   }     
   
   public Transformation qrst(int q, int r, int s, int t){
      this.q = BigInteger.valueOf(q);
      this.r = BigInteger.valueOf(r);
      this.s = BigInteger.valueOf(s);
      this.t = BigInteger.valueOf(t); 
      k = 0;  
      return this;                             
   }         
  
   public Transformation compose(Transformation a){      
      return new Transformation(
         q.multiply(a.q)
         ,(q.multiply(a.r)).add( (r.multiply(a.t)) ) 
         ,(s.multiply(a.q)).add( (t.multiply(a.s)) ) 
         ,(s.multiply(a.r)).add( (t.multiply(a.t)) )                   
         );                    
   }          
}


  
