/* The Great Computer Language Shootout
   http://shootout.alioth.debian.org/
 
   contributed by Isaac Gouy 
*/


public class process {

   public static void main(String args[]) {
      int n = Integer.parseInt(args[0]);

      EndLink chainEnd = new EndLink(null, n);
      chainEnd.start();
      Link chain = chainEnd;
         
      for (int i=2; i<=n; i++){
         Link link = new Link(chain);             
         link.start();
         chain = link;
      }

      chain.put(0);     
      try { chainEnd.join(); } catch (InterruptedException e){} 

      System.out.println(chainEnd.count);
      System.exit(0);
   }
}


class Link extends Thread {
   Link next;
   int message = -1;
   boolean busy = false;
   
   Link(Link t){
      next = t;
   }

   public void run() { 
      for (;;) next.put(this.take());          
   }

   synchronized void put(int m) {
      while (busy)
         try { wait(); } catch (InterruptedException e){}   
      busy = true;
      message = m;
      notifyAll();

      while (message != -1)
         try { wait(); } catch (InterruptedException e){}  
      busy = false;
      notifyAll();            
   }

   synchronized int take() {
      while (message == -1)
         try { wait(); } catch (InterruptedException e){}  

      int m = message;
      message = -1;
      notifyAll();
      return m+1;             
   }
}


class EndLink extends Link {
   public int count = 0;
   private int finalcount;
   
   EndLink(Link t, int i){
      super(t);
      finalcount = i;
   }

   public void run() { 
      do 
         count += this.take(); 
      while (count < finalcount);       
   }
}

// vim: set ts=4 ft=java
