package org.codehaus.groovy.util;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * A bit simplified lock designed to be inherited by.
 */
public class LockableObject extends AbstractQueuedSynchronizer {
    transient Thread owner;

    protected final boolean isHeldExclusively() {
        return getState() != 0 && owner == Thread.currentThread();
    }

    public final void lock() {
        if (compareAndSetState(0, 1))
            owner = Thread.currentThread();
        else
            acquire(1);
    }

    public final void unlock() {
        release(1);
    }

    protected final boolean tryAcquire(int acquires) {
        final Thread current = Thread.currentThread();
        int c = getState();
        if (c == 0) {
            if (compareAndSetState(0, acquires)) {
                owner = current;
                return true;
            }
        }
        else if (current == owner) {
            setState(c+ acquires);
            return true;
        }
        return false;
    }

    protected final boolean tryRelease(int releases) {
        int c = getState() - releases;
        if (Thread.currentThread() != owner)
            throw new IllegalMonitorStateException();
        boolean free = false;
        if (c == 0) {
            free = true;
            owner = null;
        }
        setState(c);
        return free;
    }
}
