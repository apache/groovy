/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.ui;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implementation helper for groovyConsole. Flushes {@code System.out} and {@code System.err} at a fixed rate. This
 * bundles updates for the outputArea Swing document as the {@link SystemOutputInterceptor} uses a
 * a {@link java.io.BufferedOutputStream} with a certain size that will get flushed by this class constantly.
 *
 * @author Andre Steingress
 */
public class OutputStreamSynchronisation {

    protected ScheduledExecutorService executorService;

    protected final long delay;
    protected final long period;

    /**
     * Creates a new instance without actually starting the synchronisation. This must be done with a separate call to
     * {@link #start()}.
     *
     * @param delay the initial delay in milli-seconds till synchronisation will start
     * @param period the time period in milli-seconds to be used between subsequent flushes
     */
    public OutputStreamSynchronisation(long delay, long period)  {
        this.delay = delay;
        this.period = period;
    }

    /**
     * Starts the scheduled flushes with the given {@code delay} and {@code period}.
     */
    public synchronized void start() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.flush();
                System.err.flush();
            }

        }, delay, period, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the scheduled flushes and blocks as long as all scheduled updates have been completed.
     *
     * @throws InterruptedException if waiting has been interrupted
     */
    public synchronized void stop() throws InterruptedException {
        if (executorService == null)  return;

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }
}
