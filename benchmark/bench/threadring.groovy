/*
 * The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * contributed by Klaus Friedel
 * converted to Groovy by Danno Ferrin
 */

import java.util.concurrent.locks.LockSupport

class MessageThread extends Thread {
    MessageThread nextThread
    volatile Integer message

    MessageThread(MessageThread nextThread, int name) {
        super(name as String)
        this.nextThread = nextThread
    }

    void run() {
        while (true) nextThread.enqueue(dequeue())
    }

    void enqueue(Integer hopsRemaining) {
        if (hopsRemaining == 0) {
            println(getName())
            System.exit(0)
        }
        // as only one message populates the ring, it's impossible
        // that queue is not empty
        message = hopsRemaining - 1
        LockSupport.unpark(this) // work waiting...
    }

    private Integer dequeue() {
        while (message == null) {
            LockSupport.park()
        }
        Integer msg = message
        message = null
        return msg
    }
}

int THREAD_COUNT = 503
int hopCount = args[0] as Integer

MessageThread first
MessageThread last
(THREAD_COUNT..1).each { i ->
    first = new MessageThread(first, i)
    if (i == THREAD_COUNT) last = first
}
// close the ring
last.nextThread = first

// start all threads
MessageThread t = first
THREAD_COUNT.times {
    t.start()
    t = t.nextThread
}

// inject message
first.enqueue(hopCount)
first.join() // wait for System.exit
