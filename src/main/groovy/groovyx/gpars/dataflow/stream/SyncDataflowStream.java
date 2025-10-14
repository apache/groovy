// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2013  The original author or authors
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

package groovyx.gpars.dataflow.stream;

import groovy.lang.Closure;
import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.dataflow.DataflowChannelListener;
import groovyx.gpars.dataflow.SyncDataflowVariable;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Represents a synchronous deterministic dataflow channel. Unlike a SyncDataflowQueue, syncDataflowStream allows multiple readers each to read all the messages.
 * Essentially, you may think of SyncDataflowStream as a 1 to many communication channel, since when a reader consumes a messages,
 * other readers will still be able to read the message. Also, all messages arrive to all readers in the same order.
 * SyncDataflowStream is implemented as a functional queue, which impacts the API in that users have to traverse the values in the stream themselves.
 * On the other hand in offers handy methods for value filtering or transformation together with interesting performance characteristics.
 * For convenience and for the ability to use SyncDataflowStream with other dataflow constructs, like e.g. operators,
 * you can wrap SyncDataflowStreams with DataflowReadAdapter for read access or DataflowWriteAdapter for write access.
 * <p>
 * The SyncDataflowStream class is designed for single-threaded producers and consumers. If multiple threads are supposed to read or write values
 * to the stream, their access to the stream must be serialized externally or the adapters should be used.
 * </p>
 * <p>
 * SyncDataflowStream uses SyncDataflowVariables to preform the actual data exchange. Unlike DataflowStream, which exchanges data
 * in asynchronous manner, SyncDataflowStream is synchronous. The writer as well as the readers are blocked until all the required
 * parties become ready for the data exchange. Writers can thus never get too far ahead of readers and also all the readers themselves
 * are always processing the same message in parallel and wait for one-another before getting the next one.
 * </p>
 *
 * @param <T> Type for values to pass through the stream
 * @author Vaclav Pech
 */
@SuppressWarnings({"rawtypes", "TailRecursion", "unchecked", "StaticMethodNamingConvention", "ClassWithTooManyMethods"})
public final class SyncDataflowStream<T> extends StreamCore<T> {

    private int parties;

    /**
     * Creates an empty stream
     *
     * @param parties The number of readers to ask for a value before the message gets exchanged.
     */
    public SyncDataflowStream(final int parties) {
        super(new SyncDataflowVariable<T>(parties));
        this.parties = parties;
    }

    /**
     * Creates an empty stream while applying the supplied initialization closure to it
     *
     * @param parties     The number of readers to ask for a value before the message gets exchanged.
     * @param toBeApplied The closure to use for initialization
     */
    public SyncDataflowStream(final int parties, final Closure toBeApplied) {
        super(new SyncDataflowVariable<T>(parties), toBeApplied);
        this.parties = parties;
    }

    /**
     * Creates an empty stream with the specified listeners set
     *
     * @param parties                The number of readers to ask for a value before the message gets exchanged.
     * @param wheneverBoundListeners The collection of listeners to bind to the stream
     */
    private SyncDataflowStream(final int parties, final Collection<MessageStream> wheneverBoundListeners, final Collection<DataflowChannelListener<T>> updateListeners) {
        super(new SyncDataflowVariable<T>(parties), wheneverBoundListeners, updateListeners);
        this.parties = parties;
    }

    /**
     * Retrieves a DataflowStream representing the rest of this Stream after removing the first element
     *
     * @return The remaining stream elements
     */
    @Override
    public FList<T> getRest() {
        if (rest.get() == null)
            rest.compareAndSet(null, new SyncDataflowStream<T>(parties, wheneverBoundListeners, first.getEventManager().getListeners()));
        return rest.get();
    }

    /**
     * A factory method to create new instances of the correct class when needed
     *
     * @return An instance of the appropriate sub-class
     */
    @Override
    protected StreamCore<T> createNewStream() {
        return new SyncDataflowStream<T>(parties);
    }

    @Override
    public String appendingString() {
        if (!first.isBound())
            return ", ?";
        if (isEmptyWithRespectToSync())
            return "";
        return ", " + getFirst() + getRest().appendingString();
    }

    @Override
    public String toString() {
        if (!first.isBound())
            return "SyncDataflowStream[?]";
        if (isEmptyWithRespectToSync())
            return "SyncDataflowStream[]";
        return "SyncDataflowStream[" + getFirst() + getRest().appendingString() + ']';
    }

    private boolean isEmptyWithRespectToSync() {
        try {
            final T val = getFirstDFV().getVal(0L, TimeUnit.MILLISECONDS);
            return val == eos() || val == null;
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while checking the oes.", e);
        }
    }

    /**
     * Increases the number of parties required to perform the data exchange
     */
    @Override
    public synchronized void incrementParties() {
        parties++;
        ((SyncDataflowVariable) first).incrementParties();
    }

    /**
     * Decreases the number of parties required to perform the data exchange
     */
    @Override
    public synchronized void decrementParties() {
        if (parties == 0) throw new IllegalArgumentException("Cannot decrease the number of parties. Already at zero.");
        parties--;
        ((SyncDataflowVariable) first).decrementParties();
    }
}
