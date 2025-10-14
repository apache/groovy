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

package groovyx.gpars.dataflow.stream;

import groovy.lang.Closure;
import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.dataflow.DataCallback;
import groovyx.gpars.dataflow.Dataflow;
import groovyx.gpars.dataflow.DataflowChannelListener;
import groovyx.gpars.dataflow.DataflowReadChannel;
import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.dataflow.expression.DataflowExpression;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a common base for publish-subscribe deterministic channels.
 *
 * @param <T> Type for values to pass through the channels
 * @author Johannes Link, Vaclav Pech
 */
public abstract class StreamCore<T> implements FList<T> {

    protected final DataflowVariable<T> first;
    protected final AtomicReference<StreamCore<T>> rest = new AtomicReference<StreamCore<T>>();

    /**
     * A collection of listeners who need to be informed each time the stream is bound to a value
     */
    protected final Collection<MessageStream> wheneverBoundListeners;

    /**
     * Creates an empty stream
     *
     * @param first The variable to store as the head of the stream
     */
    protected StreamCore(final DataflowVariable<T> first) {
        this.first = first;
        wheneverBoundListeners = new CopyOnWriteArrayList<MessageStream>();
    }

    /**
     * Creates a stream while applying the supplied initialization closure to it
     *
     * @param first       The variable to store as the head of the stream
     * @param toBeApplied The closure to use for initialization
     */
    protected StreamCore(final DataflowVariable<T> first, final Closure toBeApplied) {
        this(first);
        apply(toBeApplied);
    }

    @SuppressWarnings({"AssignmentToCollectionOrArrayFieldFromParameter"})
    protected StreamCore(final DataflowVariable<T> first, final Collection<MessageStream> wheneverBoundListeners, final Collection<DataflowChannelListener<T>> updateListeners) {
        this.first = first;
        this.wheneverBoundListeners = wheneverBoundListeners;
        hookWheneverBoundListeners(first);
        addUpdateListeners(updateListeners);
    }

    private void addUpdateListeners(final Collection<DataflowChannelListener<T>> updateListeners) {
        first.getEventManager().addAllDataflowChannelListeners(updateListeners);
    }

    final void addUpdateListener(final DataflowChannelListener<T> updateListener) {
        first.getEventManager().addDataflowChannelListener(updateListener);
    }

    public static <T> T eos() {
        return null;
    }

    private static <T> T eval(final Object valueOrDataflowVariable) {
        if (valueOrDataflowVariable instanceof DataflowVariable)
            try {
                return ((DataflowReadChannel<T>) valueOrDataflowVariable).getVal();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        return (T) valueOrDataflowVariable;
    }

    /**
     * Populates the stream with generated values
     *
     * @param seed      The initial element to evaluate and add as the first value of the stream
     * @param generator A closure generating stream elements from the previous values
     * @param condition A closure indicating whether the generation should continue based on the last generated value
     * @return This stream
     */
    public final StreamCore<T> generate(final T seed, final Closure generator, final Closure condition) {
        generateNext(seed, this, generator, condition);
        return this;
    }

    private void generateNext(final T value, final StreamCore<T> stream, final Closure generator, final Closure condition) {
        T recurValue = value;
        StreamCore<T> recurStream = stream;
        while (true) {
            final boolean addValue = (Boolean) condition.call(new Object[]{recurValue});
            if (!addValue) {
                recurStream.leftShift(StreamCore.<T>eos());
                return;
            }
            recurStream = recurStream.leftShift(recurValue);
            recurValue = (T) eval(generator.call(new Object[]{recurValue}));
        }
    }

    /**
     * Calls the supplied closure with the stream as a parameter
     *
     * @param closure The closure to call
     * @return This instance of DataflowStream
     */
    public final StreamCore<T> apply(final Closure closure) {
        closure.call(new Object[]{this});
        return this;
    }

    /**
     * Adds a dataflow variable value to the stream, once the value is available
     *
     * @param ref The DataflowVariable to check for value
     * @return The rest of the stream
     */
    public final StreamCore<T> leftShift(final DataflowReadChannel<T> ref) {
        ref.getValAsync(new MessageStream() {
            @Override
            public MessageStream send(final Object message) {
                first.bind((T) message);
                return null;
            }
        });
        return (StreamCore<T>) getRest();
    }

    /**
     * Adds a value to the stream
     *
     * @param value The value to add
     * @return The rest of the stream
     */
    public final StreamCore<T> leftShift(final T value) {
        bind(value);
        return (StreamCore<T>) getRest();
    }

    private void bind(final T value) {
        first.bind(value);
    }

    final DataflowVariable<T> getFirstDFV() {
        return first;
    }

    /**
     * Retrieved the first element in the stream, blocking until a value is available
     *
     * @return The first item in the stream
     */
    @Override
    public final T getFirst() {
        try {
            return first.getVal();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a DataflowStream representing the rest of this Stream after removing the first element
     *
     * @return The remaining stream elements
     */
    @Override
    public abstract FList<T> getRest();

    /**
     * Indicates, whether the first element in the stream is an eos
     */
    @Override
    public final boolean isEmpty() {
        return getFirst() == eos();
    }

    /**
     * Builds a filtered stream using the supplied filter closure
     *
     * @param filterClosure The closure to decide on inclusion of elements
     * @return The first item of the filtered stream
     */
    @Override
    public final FList<T> filter(final Closure filterClosure) {
        final StreamCore<T> newStream = createNewStream();
        filter(this, filterClosure, newStream);
        return newStream;
    }

    private void filter(final StreamCore<T> rest, final Closure filterClosure, final StreamCore<T> result) {
        StreamCore<T> recurRest = rest;
        StreamCore<T> recurResult = result;
        while (true) {
            if (recurRest.isEmpty()) {
                recurResult.leftShift(StreamCore.<T>eos());
                return;
            }
            final boolean include = (Boolean) eval(filterClosure.call(new Object[]{recurRest.getFirst()}));
            if (include) recurResult = recurResult.leftShift(recurRest.getFirst());
            recurRest = (StreamCore<T>) recurRest.getRest();
        }
    }

    /**
     * Builds a modified stream using the supplied map closure
     *
     * @param mapClosure The closure to transform elements
     * @return The first item of the transformed stream
     */
    @Override
    public final FList<Object> map(final Closure mapClosure) {
        final StreamCore<Object> newStream = (StreamCore<Object>) createNewStream();
        map(this, mapClosure, newStream);
        return newStream;
    }

    private void map(final FList<T> rest, final Closure mapClosure, final StreamCore<Object> result) {
        FList<T> recurRest = rest;
        StreamCore<Object> recurResult = result;
        while (true) {
            if (recurRest.isEmpty()) {
                recurResult.leftShift((Object)StreamCore.eos()); // explicit casting to make tests pass (Java8 related)
                return;
            }
            final Object mapped = mapClosure.call(new Object[]{recurRest.getFirst()});
            recurResult = recurResult.leftShift((Object)eval(mapped)); // explicit casting to make tests pass (Java8 related)
            recurRest = recurRest.getRest();
        }
    }

    /**
     * Reduces all elements in the stream using the supplied closure
     *
     * @param reduceClosure The closure to reduce elements of the stream gradually into an accumulator. The accumulator is seeded with the first stream element.
     * @return The result of reduction of the whole stream
     */
    @Override
    public final T reduce(final Closure reduceClosure) {
        if (isEmpty())
            return null;
        return reduce(getFirst(), getRest(), reduceClosure);
    }

    /**
     * Reduces all elements in the stream using the supplied closure
     *
     * @param reduceClosure The closure to reduce elements of the stream gradually into an accumulator.
     * @param seed          The value to initialize the accumulator with.
     * @return The result of reduction of the whole stream
     */
    @Override
    public final T reduce(final T seed, final Closure reduceClosure) {
        return reduce(seed, this, reduceClosure);
    }

    private T reduce(final T current, final FList<T> rest, final Closure reduceClosure) {
        T recurCurrent = current;
        FList<T> recurRest = rest;
        while (true) {
            if (recurRest.isEmpty())
                return recurCurrent;
            final Object aggregate = reduceClosure.call(new Object[]{recurCurrent, recurRest.getFirst()});
            recurCurrent = (T) eval(aggregate);
            recurRest = recurRest.getRest();
        }
    }

    /**
     * Builds an iterator to iterate over the stream
     *
     * @return A new FListIterator instance
     */
    @Override
    public final Iterator<T> iterator() {
        return new FListIterator<T>(this);
    }

    @Override
    public String appendingString() {
        if (!first.isBound())
            return ", ?";
        if (isEmpty())
            return "";
        return ", " + getFirst() + getRest().appendingString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        final FList stream = (FList) obj;
        if (isEmpty())
            return stream.isEmpty();
        if (!getFirst().equals(stream.getFirst()))
            return false;
        return getRest().equals(stream.getRest());
    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = 31 * result + rest.hashCode();
        return result;
    }

    /**
     * A factory method to create new instances of the correct class when needed
     *
     * @return An instance of the appropriate sub-class
     */
    protected abstract StreamCore<T> createNewStream();

    public final void wheneverBound(final Closure closure) {
        wheneverBoundListeners.add(new DataCallback(closure, Dataflow.retrieveCurrentDFPGroup()));
        first.whenBound(closure);
    }

    public final void wheneverBound(final MessageStream stream) {
        wheneverBoundListeners.add(stream);
        first.whenBound(stream);
    }

    /**
     * Hooks the registered when bound handlers to the supplied dataflow expression
     *
     * @param expr The expression to hook all the when bound listeners to
     * @return The supplied expression handler to allow method chaining
     */
    private DataflowExpression<T> hookWheneverBoundListeners(final DataflowExpression<T> expr) {
        for (final MessageStream listener : wheneverBoundListeners) {
            expr.whenBound(listener);
        }
        return expr;
    }

    /**
     * Increases the number of parties required to perform the data exchange
     */
    public void incrementParties() {
    }

    /**
     * Decreases the number of parties required to perform the data exchange
     */
    public void decrementParties() {
    }
}
