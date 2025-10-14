// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2012  The original author or authors
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

package groovyx.gpars;

import groovyx.gpars.scheduler.Pool;
import groovyx.gpars.scheduler.ResizeablePool;
import groovyx.gpars.scheduler.Timer;
import groovyx.gpars.util.GeneralTimer;
import groovyx.gpars.util.PoolFactory;
import groovyx.gpars.util.TimerFactory;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Enables to specify custom thread pools and timers to run GPars in hosted environments, such as GAE
 *
 * @author Vaclav Pech
 */
public final class GParsConfig {
    private static volatile PoolFactory poolFactory;
    private static volatile TimerFactory timerFactory;
    private static volatile boolean poolFactoryFlag = false;
    private static volatile boolean timerFactoryFlag = false;

    public static synchronized void setPoolFactory(final PoolFactory pool) {
        if (poolFactoryFlag)
            throw new IllegalArgumentException("The pool factory cannot be altered at this stage. It has already been set before.");
        poolFactoryFlag = true;
        poolFactory = pool;
    }

    public static PoolFactory getPoolFactory() {
        return poolFactory;
    }

    /**
     * If a pool factory has been set, it will be used to create a new thread pool.
     * Otherwise a new instance of ResizeablePool will be returned.
     *
     * @return A thread pool instance to use for default parallel groups (actors, dataflow)
     */
    public static Pool retrieveDefaultPool() {
        if (poolFactory != null) return poolFactory.createPool();
        return new ResizeablePool(true, 1);
    }

    public static synchronized void setTimerFactory(final TimerFactory timerFactory) {
        if (timerFactoryFlag)
            throw new IllegalArgumentException("The timer factory cannot be altered at this stage. It has already been set before.");
        timerFactoryFlag = true;
        GParsConfig.timerFactory = timerFactory;
    }

    public static TimerFactory getTimerFactory() {
        return timerFactory;
    }

    private static final Collection<GeneralTimer> timers = new CopyOnWriteArrayList<GeneralTimer>();

    /**
     * If a timer factory has been set, it will be used to create a timer.
     * Otherwise a new instance of java.util.Timer will be created, wrapped inside a GeneralTimer instance and returned.
     *
     * @return A timer instance to use to handle timeouts (actors, GParsPool, GParsExecutorsPool)
     */
    public static GeneralTimer retrieveDefaultTimer(final String name, final boolean daemon) {
        final GeneralTimer timer;
        if (timerFactory != null) {
            timer = timerFactory.createTimer(name, daemon);
        } else {
            timer = new GeneralTimer() {

                @Override
                public void schedule(final Runnable task, final long timeout) {
                    Timer.timer.schedule(task, timeout, TimeUnit.MILLISECONDS);
                }

                @Override
                public void shutdown() {
                    Timer.timer.shutdown();
                }
            };
        }
        if (!timers.contains(timer)) timers.add(timer);
        return timer;
    }

    public static void shutdown() {
        for (final GeneralTimer timer : timers) {
            timer.shutdown();
        }
        GParsPool.shutdown();
        GParsExecutorsPool.shutdown();
    }
}
