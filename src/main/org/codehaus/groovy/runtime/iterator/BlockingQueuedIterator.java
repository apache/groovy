/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.runtime.iterator;

import groovy.lang.GroovyRuntimeException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockingQueuedIterator extends QueuedIterator {
    private final Set producers = new HashSet();
    private final Object lock = new Object();

    protected int poolSize, maxCapacity;

    protected final ArrayList/*<LocalVars>*/ threadVars = new ArrayList();
    private final Object barrier = new Object();

    public BlockingQueuedIterator(Map vars) {
        super(vars);
        if (poolSize == 0)
            poolSize = 1;
        if (maxCapacity == 0)
            maxCapacity = Integer.MAX_VALUE;
    }

    public int getThreadIndexInPool() {
        Thread t = Thread.currentThread();
        if (t instanceof WorkerThread)
            return ((WorkerThread) t).threadIndex;
        return -1;
    }

    protected void attachProducer(WorkerThread thread) {
        synchronized (lock) {
            if (!producers.contains(thread)) {
                thread.threadIndex = producers.size();
                producers.add(thread);
                thread.start();
            } else
                throw new GroovyRuntimeException("Internal Error");
        }
    }

    protected void detachProducer() {
        synchronized (lock) {
            Thread curThread = Thread.currentThread();
            if (curThread instanceof WorkerThread && producers.contains(curThread)) {
                WorkerThread workerThread = (WorkerThread) curThread;
                producers.remove(curThread);
                workerThread.threadIndex = -1;
                lock.notify();
            } else {
                throw new GroovyRuntimeException("Internal Error");
            }
        }
    }

    protected void doCheckNext() {
        while (true) {
            Object res;
            synchronized (lock) {
                if (!isQueueEmpty()) {
                    boolean needNotify = getQueueSize() >= maxCapacity;
                    res = get();
                    if (needNotify)
                        lock.notify();
                } else if (!producers.isEmpty()) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) { //
                    }
                    continue;
                } else {
                    hasNext = false;
                    return;
                }
            }

            if (res instanceof Throwable)
                throw new GroovyRuntimeException((Throwable) res);

            lastAcquired = res;
            hasNext = true;
            return;
        }
    }

    public void put(Object object) {
        synchronized (lock) {
            while (getQueueSize() > maxCapacity) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    lock.notify();
                    throw new GroovyRuntimeException(e);
                }
            }
            super.put(object);
            lock.notify();
        }
    }

    protected static class WorkerThread extends Thread {
        protected int threadIndex;
        public LocalVars localVars = new LocalVars();
    }

    protected Map findVar(String property) {
        Thread t = Thread.currentThread();
        if (t instanceof WorkerThread) {
            WorkerThread workerThread = (WorkerThread) t;
            if (workerThread.localVars.containsKey(property))
                return workerThread.localVars;
        }
        return super.findVar(property);
    }

    protected int getQueueSize() {
        synchronized (barrier) {
            return super.getQueueSize();
        }
    }
}
