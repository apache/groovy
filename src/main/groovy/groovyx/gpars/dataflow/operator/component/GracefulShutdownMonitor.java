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

package groovyx.gpars.dataflow.operator.component;

import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.dataflow.Promise;
import groovyx.gpars.scheduler.Timer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The monitor will orchestrate a graceful shutdown, when its shutdownNetwork() method is called.
 * For this mechanism to work, all selectors/operators in the network have to have their own instance of GracefulShutdownListener added
 * and these listeners have to share the same GracefulShutdownMonitor instance.
 * Each listener observes the activity in the input channels of its corresponding operator/selector. Upon graceful shutdown
 * initialization, the listeners also start watching the state of their operators/selectors.
 * The GracefulShutdownMonitor then repeatedly watches, whether listeners report activity in the operators.
 * When the activity ceases, the monitor will poll all listeners about the state of their operator/selector and its input channels.
 * If all listeners report no activity and no incoming messages, the monitor can safely terminate all operators.
 *
 * @author Vaclav Pech
 */
public final class GracefulShutdownMonitor implements OperatorStateMonitor {
    private static final long DEFAULT_DELAY = 500L;
    private final long delay;

    /**
     * Indicates whether shutdown has been initialized
     */
    private boolean shutdownFlag = false;

    /**
     * Indicates, whether some activity has been reported since last time
     */
    private final AtomicBoolean notificationArrivedFlag = new AtomicBoolean(false);

    /**
     * The final latch that indicates the network has been shutdown
     */
    private final DataflowVariable<Boolean> result = new DataflowVariable<Boolean>();

    /**
     * All listeners that watch the operators/selectors in the network
     */
    private final Collection<GracefulShutdownListener> listeners = new ArrayList<GracefulShutdownListener>();

    /**
     * Uses the default timer delay
     */
    public GracefulShutdownMonitor() {
        this(DEFAULT_DELAY);
    }

    /**
     * Allows to use a customized delay
     *
     * @param delay A timeout in milliseconds to wait between two subsequent polls on processors' state.
     *              Lower values will reduce the wait time for network shutdown,
     *              but maz have impact on the performance when the shutdown process gets initialized.
     */
    public GracefulShutdownMonitor(final long delay) {
        this.delay = delay;
    }

    /**
     * Invoked whenever a processor changes state
     */
    @Override
    public void stateChanged() {
        notificationArrivedFlag.set(true);
    }

    /**
     * Invoked by GracefulShutdownListeners, which listen on operators/selectors for lifecycle events, to get registered with the monitor.
     * The monitor will query these registered listeners about their respective operator/selector state when performing graceful shutdown.
     *
     * @param listener The listener to register
     */
    @Override
    public synchronized void registerProcessorListener(final GracefulShutdownListener listener) {
        if (shutdownFlag)
            throw new IllegalStateException("Cannot register processors while performing graceful shutdown.");
        listeners.add(listener);
    }

    /**
     * Initializes the shutdown process.
     * New listeners cannot be registered after this point.
     * New messages should not enter the dataflow network from the outside, since this may prevent the network from terminating.
     *
     * @return A Promise, which may be used to wait for or get notified about the shutdown success.
     */
    public synchronized Promise<Boolean> shutdownNetwork() {
        if (!shutdownFlag) {
            shutdownFlag = true;
            for (final GracefulShutdownListener listener : listeners) {
                listener.initiateShutdown();
            }
            schedule();
        }
        return result;
    }

    /**
     * Checks, whether the network is no longer active.
     * To reduce effect on system performance, cheap criteria are being checked first.
     */
    void onTimer() {
        //  Has there been any activity in the processors?
        if (checkWhetherAnyEventsHaveArrived()) return;

        //  Are all processors idle?
        for (final GracefulShutdownListener listener : listeners) {
            if (!listener.isIdle()) {
                schedule();
                return;
            }
        }

        //  Are all processors idle and have empty input channels?
        for (final GracefulShutdownListener listener : listeners) {
            if (!listener.isIdleAndNoIncomingMessages()) {
                schedule();
                return;
            }
        }

        //  Has there been any activity in the processors while we were checking input channels?
        if (checkWhetherAnyEventsHaveArrived()) return;

        //  We are safe to shutdown now
        result.bind(true);
        for (final GracefulShutdownListener listener : listeners) {
            listener.terminateProcessor();
        }
    }

    /**
     * Checks the notification flag whether there has been some activity since last timer run.
     *
     * @return True, if events have been registered since last time, false otherwise.
     */
    private boolean checkWhetherAnyEventsHaveArrived() {
        if (notificationArrivedFlag.get()) {
            schedule();
            return true;
        }
        return false;
    }

    /**
     * Clears the notification flag so as we know whether some activity happens between subsequent checks.
     * Schedules the timer to run again after the delay set on the monitor.
     */
    private void schedule() {
        notificationArrivedFlag.set(false);
        Timer.timer.schedule(new Runnable() {
            @Override
            public void run() {
                onTimer();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }
}
