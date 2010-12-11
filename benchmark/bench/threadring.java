/**
 * The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 * contributed by Klaus Friedel
 */

import java.util.*;

public class threadring {

  public static long startTime;
  public static final int THREAD_COUNT = 503;

  // The scheduler for cooperative Multithreading
  static class Scheduler extends Thread{
    private final List<CooperativeThread> threads = Collections.synchronizedList(new ArrayList<CooperativeThread>());
    private int rrIndex = -1;

    public void start(CooperativeThread t){
      threads.add(t);
    }

    public void run() {
      for(;;){ // Scheduler will run foerever
        CooperativeThread nextThread;
        synchronized (threads){
          rrIndex++;
          if(rrIndex >= threads.size()) rrIndex = 0;
          nextThread = threads.get(rrIndex);
        }
        nextThread.handleMessage();
      }
    }
  }

  static abstract class CooperativeThread{
    public abstract void handleMessage();
  }

  static class MessageThread extends CooperativeThread{
      MessageThread nextThread;
      String name;
      Integer msg;

      public MessageThread(MessageThread nextThread, int name) {
        this.name = "" + name;
        this.nextThread = nextThread;
      }

      public void handleMessage(){
        if(msg == null) return;
        if(msg == 0){
          System.out.println(getName());
          System.exit(0);
        }
        nextThread.put(msg - 1);
        msg = null;
      }

      void put(Integer message){
        msg = message;
      }

      String getName() {
        return name;
      }
    }


  public static void main(String args[]) throws Exception{
    int hopCount = Integer.parseInt(args[0]);

    MessageThread thread = null;
    MessageThread last = null;
    for (int i = THREAD_COUNT; i >= 1 ; i--) {
      thread = new MessageThread(thread, i);
      if(i == THREAD_COUNT) last = thread;
    }
    // close the ring:
    last.nextThread = thread;

    Scheduler scheduler = new Scheduler();
    // start all Threads
    MessageThread t = thread;
    do{
      scheduler.start(t);
      t = t.nextThread;
    }while(t != thread);
    scheduler.start();

    // inject message
    thread.put(hopCount);
  }
}
