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

import groovy.lang.Closure;

import java.util.LinkedList;

public class ThreadPool {

    public static final ThreadPool POOL = new ThreadPool();

    final LinkedList tasks = new LinkedList();
    final LinkedList availableThreads = new LinkedList();
    private long keepAliveTime = 60 * 1000; // one minute

    public void addTask(Runnable run) {
        synchronized (tasks) {
            tasks.add(run);

            Worker worker = null;
            synchronized (availableThreads) {
                if (availableThreads.size() == 0) {
                    worker = new Worker();
                    availableThreads.add(worker);
                }
            }

            if (worker != null) {
                synchronized (worker) {
                    try {
                        worker.start();
                        worker.wait();
                    } catch (InterruptedException e) { //
                    }
                }
            }

            tasks.notify();
        }
    }

    public void addTask(final Closure closure, final Object params) {
        addTask(new Runnable() {
            public void run() {
                closure.call(params instanceof Object[] ? (Object[]) params : params);
            }
        });
    }

    public static Runnable currentTask() {
        Thread t = Thread.currentThread();
        if (!(t instanceof Worker))
            return null;

        synchronized (t) {
            return ((Worker) t).currentTask;
        }
    }

    class Worker extends Thread {
        private Runnable currentTask;

        Worker() {
            setDaemon(true);
        }

        public void run() {
            synchronized (this) {
                notify();
            }

            while (true) {
                currentTask = waitForTask();

                try {
                    if (currentTask == null)
                        return;

                    try {
                        currentTask.run();
                    }
                    catch (Throwable t) {//
                    }
                }
                finally {
                    currentTask = null;
                    putToPool();
                }
            }
        }

        private void putToPool() {
            synchronized (availableThreads) {
                availableThreads.add(this);
            }
        }

        private Runnable waitForTask() {
            synchronized (tasks) {
                if (!tasks.isEmpty()) {
                    synchronized (availableThreads) {
                        availableThreads.remove(this);
                    }
                    return (Runnable) tasks.removeFirst();
                }

                try {
                    tasks.wait(keepAliveTime);
                } catch (InterruptedException e) {//
                }
                return null;
            }
        }
    }
}
