/* The Computer Language Benchmarks Game
 http://shootout.alioth.debian.org/

 contributed by Mattias Bergander
 */

import java.util.LinkedList;
import java.util.List;

public class message {
    public static final int numberOfThreads = 500;

    public static int numberOfMessagesToSend;

    public static void main(String args[]) {
        numberOfMessagesToSend = Integer.parseInt(args[0]);

        MessageThread chain = null;
        for (int i = 0; i < numberOfThreads; i++) {
            chain = new MessageThread(chain);
            new Thread(chain).start();
        }

        for (int i = 0; i < numberOfMessagesToSend; i++) {
            chain.enqueue(new MutableInteger(0));
        }

    }
}

class MutableInteger {
    int value;

    public MutableInteger() {
        this(0);
    }

    public MutableInteger(int value) {
        this.value = value;
    }

    public MutableInteger increment() {
        value++;
        return this;
    }

    public int intValue() {
        return value;
    }
}

class MessageThread implements Runnable {
    MessageThread nextThread;

    List<MutableInteger> list = new LinkedList<MutableInteger>();

    MessageThread(MessageThread nextThread) {
        this.nextThread = nextThread;
    }

    public void run() {
        if (nextThread != null) {
            while (true) {
                nextThread.enqueue(dequeue());
            }
        } else {
            int sum = 0;
            int finalSum = message.numberOfThreads * message.numberOfMessagesToSend;
            while (sum < finalSum) {
                sum += dequeue().intValue();
            }
            System.out.println(sum);
            System.exit(0);
        }
    }

    /**
     * @param message
     */
    public void enqueue(MutableInteger message) {
        synchronized (list) {
            list.add(message);
            if (list.size() == 1) {
                list.notify();
            }
        }
    }

    public MutableInteger dequeue() {
        synchronized (list) {
            while (list.size() == 0) {
                try {
                    list.wait();
                } catch (InterruptedException e) {
                }
            }
            return list.remove(0).increment();
        }
    }
}
