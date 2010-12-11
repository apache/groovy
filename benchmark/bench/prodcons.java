// $Id: prodcons.java,v 1.1 2004-05-23 07:14:28 bfulgham Exp $
// http://www.bagley.org/~doug/shootout/
// Producer-Consumer Example by Bill Lear
// Adapted from http://java.sun.com/docs/books/tutorial/essential/threads

public class prodcons {
    private class CubbyHole {
        private int m_contents;
        private boolean m_available = false;

        public synchronized int get() {
            while (m_available == false) {
                try {
                    wait();
                } catch (InterruptedException e) { }
            }
            m_available = false;
            notifyAll();
            return m_contents;
        }

        public synchronized void put(int value) {
            while (m_available == true) {
                try {
                    wait();
                } catch (InterruptedException e) { }
            }
            m_contents = value;
            m_available = true;
            notifyAll();
        }
    }

    private class Producer extends Thread {
        private CubbyHole m_cubbyhole;
        private int m_count;

        public Producer(CubbyHole c, int count) {
            m_cubbyhole = c;
            m_count = count;
        }

        public void run() {
            for (int i = 0; i < m_count; i++) {
                m_cubbyhole.put(i);
                ++m_produced;
            }
        }
    }

    private class Consumer extends Thread {
        private CubbyHole m_cubbyhole;
        private int m_count;

        public Consumer(CubbyHole c, int count) {
            m_cubbyhole = c;
            m_count = count;
        }

        public void run() {
            int value = 0;
            for (int i = 0; i < m_count; i++) {
                value = m_cubbyhole.get();
                ++m_consumed;
            }
        }
    }

    public void run() {
        m_producer.start();
        m_consumer.start();
        try { m_producer.join(); } catch (InterruptedException e) { }
        try { m_consumer.join(); } catch (InterruptedException e) { }
        System.out.println(m_produced + " " + m_consumed);
    }

    public prodcons(int count) {
        CubbyHole m_cubbyhole = new CubbyHole();
        m_producer = new Producer(m_cubbyhole, count);
        m_consumer = new Consumer(m_cubbyhole, count);
    }

    public static void main(String[] args) {
        int count = 1;
        try { count = Integer.parseInt(args[0]); } catch (Exception e) { }
        new prodcons(count).run();
    }

    private Producer m_producer;
    private Consumer m_consumer;
    private int m_produced = 0;
    private int m_consumed = 0;
}
