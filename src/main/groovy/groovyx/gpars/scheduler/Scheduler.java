// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2010, 2013  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.scheduler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Prototype of self-regulated thread pooled scheduler
 * <p>
 * Self regulation happened according to following rules:
 * </p>
 * <ul>
 *   <li>worker thread, which had nothing to do 10 seconds dies</li>
 *   <li>if no tasks were taken for processing during last 0.5sec new worker starts</li>
 * </ul>
 */
public final class Scheduler implements Pool {
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();

    private final AtomicInteger threadCount = new AtomicInteger();

    private volatile long lastTaskPoke = -POKE_INTERVAL;

    private volatile long schedulerTime;

    private volatile boolean terminating;

    static final RuntimeException TERMINATE = new RuntimeException("terminate");
    private static final long POKE_INTERVAL = 10L;

    public Scheduler() {
        this(0);
    }

    public Scheduler(final int coreSize) {
        new WatchdogThread().start();

        for (int i = 0; i != coreSize; ++i) {
            startNewThread();
        }
    }

    @Override
    public void execute(final Runnable task) {
        if (terminating) {
            throw new RuntimeException("Scheduler is shutting down");
        }

        try {
            queue.put(task);
            if (threadCount.get() == 0) {
                startNewThread();
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public Runnable loop(final Runnable operation) {
        return new Runnable() {
            @Override
            public void run() {
                operation.run();
                if (!terminating) {
                    execute(this);
                }
            }
        };
    }

    private void startNewThread() {
        threadCount.incrementAndGet();
        new WorkerThread().start();
    }

    @Override
    public void resize(final int poolSize) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetDefaultSize() {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieves the current thread pool size
     *
     * @return The pool size
     */
    @Override
    public int getPoolSize() {
        return threadCount.get();
    }

    @Override
    @SuppressWarnings({"ObjectAllocationInLoop"})
    public void shutdown() {
        terminating = true;
        final int count = threadCount.get();
        for (int i = 0; i != count; ++i) {
            try {
                queue.put(new Runnable() {
                    @Override
                    public void run() {
                        throw Scheduler.TERMINATE;
                    }
                });
            } catch (InterruptedException ignored) { //
                Thread.currentThread().interrupt();
            }
        }
    }

    private class WorkerThread extends Thread {
        {
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                try {
                    while (!terminating) {
                        final Runnable task = queue.poll(POKE_INTERVAL, TimeUnit.SECONDS);
                        if (task == null) {
                            return;
                        }

                        lastTaskPoke = schedulerTime;
                        try {
                            task.run();
                        }
                        catch (Throwable t) {
                            if (Scheduler.TERMINATE != t) {
                                //todo allow for a customizable handler
                                t.printStackTrace();
                            }
                        }
                    }
                } catch (InterruptedException e) {//
                }
            }
            finally {
                threadCount.decrementAndGet();
            }
        }
    }

    private class WatchdogThread extends Thread {
        private static final long SLEEP_INTERVAL = 50L;

        {
            setDaemon(true);
        }

        @Override
        public void run() {
            while (!terminating) {
                try {
                    schedulerTime++;
                    if (schedulerTime > lastTaskPoke + POKE_INTERVAL) {
                        startNewThread();
                    }
                    Thread.sleep(SLEEP_INTERVAL);
                } catch (InterruptedException ignore) {
                    break;
                }
            }
        }
    }
}
