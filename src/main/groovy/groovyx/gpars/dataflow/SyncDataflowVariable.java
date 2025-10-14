// GPars - Groovy Parallel Systems
//
// Copyright © 2008-11  The original author or authors
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

package groovyx.gpars.dataflow;

import groovy.lang.Closure;
import groovyx.gpars.MessagingRunnable;
import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.dataflow.impl.ResizeableCountDownLatch;
import groovyx.gpars.dataflow.operator.ChainWithClosure;
import groovyx.gpars.dataflow.operator.CopyChannelsClosure;
import groovyx.gpars.group.PGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

/**
 * A synchronous variant of DataflowVariable, which blocks the writer as well as the readers.
 * The synchronous variable ensures a specified number of readers must ask for a value before the writer as well as the readers can continue.
 *
 * @author Vaclav Pech
 */
public final class SyncDataflowVariable<T> extends DataflowVariable<T> {
    private static final String ERROR_READING_A_SYNCHRONOUS_CHANNEL = "Error reading a synchronous channel.";
    private final ResizeableCountDownLatch parties;

    /**
     * Creates a new variable, which will never block writers.
     */
    public SyncDataflowVariable() {
        this(0);
    }

    /**
     * Creates a new variable blocking the specified number of readers.
     *
     * @param parties Number of readers that have to match a writer before the message gets transferred
     */
    public SyncDataflowVariable(final int parties) {
        this.parties = new ResizeableCountDownLatch(parties);
    }

    @Override
    protected void doBindImpl(final T value) {
        super.doBindImpl(value);
        awaitParties();
    }

    /**
     * Reads the value of the variable. Blocks, if the value has not been assigned yet.
     *
     * @return The actual value
     * @throws InterruptedException If the current thread gets interrupted while waiting for the variable to be bound
     */
    @Override
    public T getVal() throws InterruptedException {
        final T val = super.getVal();
        readerIsReady();
        return val;
    }

    /**
     * Reads the value of the variable. Blocks up to given timeout, if the value has not been assigned yet.
     *
     * @param timeout The timeout value
     * @param units   Units for the timeout
     * @return The actual value
     * @throws InterruptedException If the current thread gets interrupted while waiting for the variable to be bound
     */
    @Override
    public T getVal(final long timeout, final TimeUnit units) throws InterruptedException {
        final long start = System.nanoTime();
        final long duration = units.toNanos(timeout);

        final T result = super.getVal(timeout, units);
        if (result == null) {
            if (!this.isBound()) return null;
            if (readerIsReady(duration - (System.nanoTime() - start))) return getVal();
            else return null;
        }

        if (readerIsReady(duration - (System.nanoTime() - start))) return result;
        else return null;
    }

    @Override
    boolean shouldThrowTimeout() {
        return super.shouldThrowTimeout() || awaitingParties();
    }

    /**
     * Reports whether the variable is still waiting for parties to arrive for the rendezvous.
     *
     * @return True if not all parties have shown yet
     */
    public boolean awaitingParties() {
        return !parties.isReleasedFlag();
    }

    @Override
    public <V> DataflowReadChannel<V> chainWith(final PGroup group, final Closure<V> closure) {
        final SyncDataflowVariable<V> result = new SyncDataflowVariable<V>();
        group.operator(this, result, new ChainWithClosure<V>(closure));
        return result;
    }

    @Override
    public DataflowReadChannel<T> tap(final PGroup group, final DataflowWriteChannel<T> target) {
        final SyncDataflowVariable<T> result = new SyncDataflowVariable<T>();
        group.operator(asList(this), asList(result, target), new ChainWithClosure(new CopyChannelsClosure()));
        return result;
    }

    @Override
    public <V> DataflowReadChannel<V> merge(final PGroup group, final List<DataflowReadChannel<Object>> others, final Closure<V> closure) {
        final SyncDataflowVariable<V> result = new SyncDataflowVariable<V>();
        final List<DataflowReadChannel> inputs = new ArrayList<DataflowReadChannel>();
        inputs.add(this);
        inputs.addAll(others);
        group.operator(inputs, asList(result), new ChainWithClosure(closure));
        return result;
    }

    @Override
    protected void scheduleCallback(final Object attachment, final MessageStream callback) {
        super.scheduleCallback(attachment, new DataCallback(new MessagingRunnable() {
            @Override
            protected void doRun(final Object argument) {
                readerIsReady();
                callback.send(argument);
            }
        }, Dataflow.retrieveCurrentDFPGroup()));
    }

    private void readerIsReady() {
        parties.countDown();
        awaitParties();
    }

    private void awaitParties() {
        try {
            parties.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(ERROR_READING_A_SYNCHRONOUS_CHANNEL, e);
        }
    }

    private boolean readerIsReady(final long timeout) {
        parties.countDown();
        try {
            return parties.attemptToCountDownAndAwait(timeout);
        } catch (InterruptedException e) {
            throw new IllegalStateException("The thread has been interrupted while waiting.", e);
        }
    }

    /**
     * Increases the number of parties required to perform data exchange by one.
     */
    public void incrementParties() {
        parties.increaseCount();
    }

    /**
     * Decreases the number of parties required to perform data exchange by one
     */
    public void decrementParties() {
        parties.decreaseCount();
    }
}
