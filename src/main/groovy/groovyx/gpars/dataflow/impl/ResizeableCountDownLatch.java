// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2011, 2013  The original author or authors
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

package groovyx.gpars.dataflow.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * CountDownLatch with the ability to change the number of waiting parties
 *
 * @author Vaclav Pech
 * @author Doug Lea
 */
public final class ResizeableCountDownLatch {

    /**
     * Synchronization control For CountDownLatch.
     * Uses AQS state to represent count.
     */
    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        Sync(final int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        public int tryAcquireShared(final int acquires) {
            return getState() == 0 ? 1 : -1;
        }

        public boolean tryReleaseShared(final int releases) {
            // Decrement count; signal when transition to zero
            //noinspection ForLoopWithMissingComponent
            for (; ; ) {
                final int c = getState();
                if (c == 0)
                    return false;
                final int nextc = c - 1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }

        public void increaseCount() {
            boolean repeat = true;
            while (repeat) {
                final int state = getState();
                repeat = !compareAndSetState(state, state + 1);
            }
        }

        public void decreaseCount() {
            boolean repeat = true;
            while (repeat) {
                final int state = getState();
                repeat = !compareAndSetState(state, state - 1);
            }
        }
    }

    private final Sync sync;

    /**
     * Constructs a {@code CountDownLatch} initialized with the given count.
     *
     * @param count the number of times {@link #countDown} must be invoked
     *              before threads can pass through {@link #await}
     * @throws IllegalArgumentException if {@code count} is negative
     */
    public ResizeableCountDownLatch(final int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }

    /**
     * Causes the current thread to wait until the latch has counted down to
     * zero, unless the thread is {@linkplain Thread#interrupt interrupted}.
     * <p>
     * If the current count is zero then this method returns immediately.
     * </p>
     * <p>
     * If the current count is greater than zero then the current
     * thread becomes disabled for thread scheduling purposes and lies
     * dormant until one of two things happen:
     * </p>
     * <ul>
     * <li>The count reaches zero due to invocations of the
     * {@link #countDown} method; or</li>
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread.</li>
     * </ul>
     * <p>If the current thread:</p>
     * <ul>
     * <li>has its interrupted status set on entry to this method; or</li>
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting,</li>
     * </ul>
     * <p>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     * </p>
     *
     * @throws InterruptedException if the current thread is interrupted
     *                              while waiting
     */
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * Causes the current thread to wait until the latch has counted down to
     * zero, unless the thread is {@linkplain Thread#interrupt interrupted},
     * or the specified waiting time elapses.
     * <p>
     * If the current count is zero then this method returns immediately
     * with the value {@code true}.
     * </p>
     * <p>
     * If the current count is greater than zero then the current
     * thread becomes disabled for thread scheduling purposes and lies
     * dormant until one of three things happen:
     * </p>
     * <ul>
     * <li>The count reaches zero due to invocations of the
     * {@link #countDown} method; or</li>
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     * <li>The specified waiting time elapses.</li>
     * </ul>
     * <p>
     * If the count reaches zero then the method returns with the
     * value {@code true}.
     * </p>
     * <p>
     * If the current thread:
     * </p>
     * <ul>
     * <li>has its interrupted status set on entry to this method; or</li>
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting,</li>
     * </ul>
     * <p>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     * </p>
     * <p>
     * If the specified waiting time elapses then the value {@code false}
     * is returned.  If the time is less than or equal to zero, the method
     * will not wait at all.
     * </p>
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the {@code timeout} argument
     * @return {@code true} if the count reached zero and {@code false}
     *         if the waiting time elapsed before the count reached zero
     * @throws InterruptedException if the current thread is interrupted
     *                              while waiting
     */
    public boolean await(final long timeout, final TimeUnit unit)
            throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /**
     * Attempts to atomically count down the latch and await release with a timeout.
     * If the timeout expires, the count is increased and the latch is re-tested before reporting failed timeout.
     *
     * @param timeout The time in nanoseconds to await
     * @return True, if successful, false, is the timeout elapses without the latch being released
     * @throws InterruptedException If the thread gets interrupted while waiting for the release
     */
    public boolean attemptToCountDownAndAwait(final long timeout) throws InterruptedException {
        if (await(timeout, TimeUnit.NANOSECONDS)) return true;
        increaseCount();
        if (getCount() <= 1L) {
            countDown();
            return true;
        }
        return false;
    }

    public boolean isReleasedFlag() {
        return sync.getCount() == 0;
    }

    /**
     * Decrements the count of the latch, releasing all waiting threads if
     * the count reaches zero.
     * <p>
     * If the current count is greater than zero then it is decremented.
     * If the new count is zero then all waiting threads are re-enabled for
     * thread scheduling purposes.
     * </p>
     * <p>
     * If the current count equals zero then nothing happens.
     * </p>
     */
    public void countDown() {
        sync.releaseShared(1);
    }

    public void increaseCount() {
        sync.increaseCount();
    }

    public void decreaseCount() {
        sync.decreaseCount();
    }

    /**
     * Returns the current count.
     * <p>
     * This method is typically used for debugging and testing purposes.
     * </p>
     *
     * @return the current count
     */
    public long getCount() {
        return sync.getCount();
    }

    /**
     * Returns a string identifying this latch, as well as its state.
     * The state, in brackets, includes the String {@code "Count ="}
     * followed by the current count.
     *
     * @return a string identifying this latch, as well as its state
     */
    public String toString() {
        return super.toString() + "[Count = " + sync.getCount() + "]";
    }
}
