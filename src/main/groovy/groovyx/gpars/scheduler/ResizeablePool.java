// GPars - Groovy Parallel Systems
//
// Copyright © 2008-10  The original author or authors
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

import groovyx.gpars.util.PoolUtils;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Represents the actors' thread pool, which performs tasks on behalf of the actors.
 * The actors' thread pool size defaults to the n + 1, where n is the number of processors/cores available on the machine.
 * The VM parameter -Dgpars.poolsize can be used the configure the default size of the actors' thread pool.
 * The resize() and resetDefaultSize() methods can be used to configure size of the thread pool at runtime.
 *
 * @author Vaclav Pech
 *         Date: Feb 27, 2009
 */
public final class ResizeablePool extends DefaultPool {
    private static final long KEEP_ALIVE_TIME = 10L;

    /**
     * Creates the pool with default number of threads.
     *
     * @param daemon Sets the daemon flag of threads in the pool.
     */
    public ResizeablePool(final boolean daemon) {
        this(daemon, PoolUtils.retrieveDefaultPoolSize());
    }

    /**
     * Creates the pool with specified number of threads.
     *
     * @param daemon   Sets the daemon flag of threads in the pool.
     * @param poolSize The required size of the pool
     */
    public ResizeablePool(final boolean daemon, final int poolSize) {
        super(ResizeablePool.createResizeablePool(daemon, poolSize));
    }

    /**
     * Creates a fixed-thread pool of given size. Each thread will have the uncaught exception handler set
     * to print the unhandled exception to standard error output.
     *
     * @param daemon   Sets the daemon flag of threads in the pool.
     * @param poolSize The required pool size  @return The created thread pool
     * @return The newly created thread pool
     */
    private static ThreadPoolExecutor createResizeablePool(final boolean daemon, final int poolSize) {
        assert poolSize > 0;
        return new ThreadPoolExecutor(poolSize, 1000, ResizeablePool.KEEP_ALIVE_TIME, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                final Thread thread = new Thread(r, DefaultPool.createThreadName());
                thread.setDaemon(daemon);
                thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(final Thread t, final Throwable e) {
                        System.err.println();
                        e.printStackTrace(System.err);
                    }
                });
                return thread;
            }
        }, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
                final int currentPoolSize = executor.getPoolSize();
                final int maximumPoolSize = executor.getMaximumPoolSize();
                throw new IllegalStateException("The thread pool executor cannot run the task. " +
                        "The upper limit of the thread pool size has probably been reached. " +
                        "Current pool size: " + currentPoolSize + " Maximum pool size: " + maximumPoolSize);
            }
        });
    }
}
