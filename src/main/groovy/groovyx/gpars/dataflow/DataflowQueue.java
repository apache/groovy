// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2014  The original author or authors
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

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import groovy.lang.Closure;
import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.dataflow.expression.DataflowExpression;
import groovyx.gpars.dataflow.impl.DataflowChannelEventListenerManager;
import groovyx.gpars.dataflow.impl.DataflowChannelEventOrchestrator;
import groovyx.gpars.dataflow.impl.ThenMessagingRunnable;
import groovyx.gpars.dataflow.operator.BinaryChoiceClosure;
import groovyx.gpars.dataflow.operator.ChainWithClosure;
import groovyx.gpars.dataflow.operator.ChoiceClosure;
import groovyx.gpars.dataflow.operator.CopyChannelsClosure;
import groovyx.gpars.dataflow.operator.FilterClosure;
import groovyx.gpars.dataflow.operator.SeparationClosure;
import groovyx.gpars.dataflow.remote.RemoteDataflowQueue;
import groovyx.gpars.group.DefaultPGroup;
import groovyx.gpars.group.PGroup;
import groovyx.gpars.scheduler.Pool;
import groovyx.gpars.serial.WithSerialId;

/**
 * Represents a thread-safe data flow stream. Values or DataflowVariables are added using the '&lt;&lt;' operator
 * and safely read once available using the 'val' property.
 * The iterative methods like each(), collect(), iterator(), any(), all() or the for loops work with snapshots
 * of the stream at the time of calling the particular method.
 * For actors and Dataflow Operators the asynchronous non-blocking variants of the getValAsync() methods can be used.
 * They register the request to read a value and will send a message to the actor or operator once the value is available.
 *
 * @author Vaclav Pech
 *         Date: Jun 5, 2009
 */
@SuppressWarnings({"ClassWithTooManyMethods", "unchecked"})
public class DataflowQueue<T> extends WithSerialId implements DataflowChannel<T> {

    /**
     * Internal lock
     */
    private final Object queueLock = new Object();

    /**
     * Stores the received DataflowVariables in the buffer.
     */
    private final LinkedBlockingQueue<DataflowVariable<T>> queue = new LinkedBlockingQueue<DataflowVariable<T>>();

    /**
     * Stores unsatisfied requests for values
     */
    private final Queue<DataflowVariable<T>> requests = new LinkedList<DataflowVariable<T>>();

    /**
     * A collection of listeners who need to be informed each time the stream is bound to a value
     */
    private final Collection<MessageStream> wheneverBoundListeners = new CopyOnWriteArrayList<MessageStream>();

    /**
     * Adds a DataflowVariable to the buffer.
     * Implementation detail - in fact another DFV is added to the buffer and an asynchronous 'whenBound' handler
     * is registered with the supplied DFV to update the one stored in the buffer.
     *
     * @param ref The DFV to add to the stream
     */
    @Override
    @SuppressWarnings("unchecked")
    public final DataflowWriteChannel<T> leftShift(final DataflowReadChannel<T> ref) {
        final DataflowVariable<T> originalRef = retrieveForBind();
        hookWheneverBoundListeners(originalRef);

        ref.getValAsync(new MessageStream() {
            private static final long serialVersionUID = -4966523895011173569L;

            @Override
            public MessageStream send(final Object message) {
                originalRef.bind((T) message);
                fireOnMessage((T) message);
                return this;
            }
        });
        return this;
    }

    /**
     * Adds a DataflowVariable representing the passed in value to the buffer.
     *
     * @param value The value to bind to the head of the stream
     */
    @Override
    public final DataflowWriteChannel<T> leftShift(final T value) {
        hookWheneverBoundListeners(retrieveForBind()).bind(value);
        fireOnMessage(value);
        return this;
    }

    /**
     * Adds a DataflowVariable representing the passed in value to the buffer.
     *
     * @param value The value to bind to the head of the stream
     */
    @Override
    public final void bind(final T value) {
        hookWheneverBoundListeners(retrieveForBind()).bind(value);
        fireOnMessage(value);
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
     * Takes the first unsatisfied value request and binds a value on it.
     * If there are no unsatisfied value requests, a new DFV is stored in the queue.
     *
     * @return The DFV to bind the value on
     */
    private DataflowVariable<T> retrieveForBind() {
        return copyDFV(requests, queue);
    }

    private DataflowVariable<T> copyDFV(final Queue<DataflowVariable<T>> from, final Queue<DataflowVariable<T>> to) {
        DataflowVariable<T> ref;
        synchronized (queueLock) {
            ref = from.poll();
            if (ref == null) {
                ref = createVariable();
                to.offer(ref);
            }
        }
        return ref;
    }

    /**
     * Creates a new variable to perform the next data exchange
     *
     * @return The newly created DataflowVariable instance
     */
    protected DataflowVariable<T> createVariable() {
        return new DataflowVariable<T>();
    }

    /**
     * Retrieves the value at the head of the buffer. Blocks until a value is available.
     *
     * @return The value bound to the DFV at the head of the stream
     * @throws InterruptedException If the current thread is interrupted
     */
    @Override
    public final T getVal() throws InterruptedException {
        return retrieveOrCreateVariable().getVal();
    }

    /**
     * Retrieves the value at the head of the buffer. Blocks until a value is available.
     *
     * @param timeout The timeout value
     * @param units   Units for the timeout
     * @return The value bound to the DFV at the head of the stream
     * @throws InterruptedException If the current thread is interrupted
     */
    @Override
    public final T getVal(final long timeout, final TimeUnit units) throws InterruptedException {
        final DataflowVariable<T> variable = retrieveOrCreateVariable();
        variable.getVal(timeout, units);
        synchronized (queueLock) {
            if (!variable.isBound()) {
                requests.remove(variable);
                return null;
            }
        }
        return variable.getVal();
    }

    /**
     * Retrieves the value at the head of the buffer. Returns null, if no value is available.
     *
     * @return The value bound to the DFV at the head of the stream or null
     */
    @Override
    public final DataflowExpression<T> poll() {
        synchronized (queueLock) {
            final DataflowVariable<T> df = queue.peek();
            if (df != null && df.isBound()) {
                queue.poll();
                return df;
            }
            return null;
        }
    }

    /**
     * Asynchronously retrieves the value at the head of the buffer. Sends the actual value of the variable as a message
     * back the the supplied actor once the value has been bound.
     * The actor can perform other activities or release a thread back to the pool by calling react() waiting for the message
     * with the value of the Dataflow Variable.
     *
     * @param callback The actor to notify when a value is bound
     */
    @Override
    public final void getValAsync(final MessageStream callback) {
        getValAsync(null, callback);
    }

    /**
     * Asynchronously retrieves the value at the head of the buffer. Sends a message back the the supplied actor / operator
     * with a map holding the supplied index under the 'index' key and the actual value of the variable under
     * the 'result' key once the value has been bound.
     * The actor/operator can perform other activities or release a thread back to the pool by calling react() waiting for the message
     * with the value of the Dataflow Variable.
     *
     * @param attachment An arbitrary value to identify operator channels and so match requests and replies
     * @param callback   The actor / operator to notify when a value is bound
     */
    @Override
    public final void getValAsync(final Object attachment, final MessageStream callback) {
        retrieveOrCreateVariable().getValAsync(attachment, callback);
    }

    /**
     * Schedule closure to be executed by pooled actor after data became available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled
     *
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     */
    @Override
    public final <V> Promise<V> rightShift(final Closure<V> closure) {
        return then(closure);
    }

    /**
     * Schedule closure to be executed by pooled actor after the next data becomes available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled.
     *
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     */
    @Override
    public final <V> void whenBound(final Closure<V> closure) {
        getValAsync(new DataCallback(closure, Dataflow.retrieveCurrentDFPGroup()));
    }

    /**
     * Schedule closure to be executed by pooled actor after data becomes available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled.
     *
     * @param pool    The thread pool to use for task scheduling for asynchronous message delivery
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     */
    @Override
    public final <V> void whenBound(final Pool pool, final Closure<V> closure) {
        getValAsync(new DataCallbackWithPool(pool, closure));
    }

    @Override
    public <V> void whenBound(final PGroup group, final Closure<V> closure) {
        getValAsync(new DataCallback(closure, group));
    }

    /**
     * Send the next bound piece of data to the provided stream when it becomes available.
     *
     * @param stream stream where to send result
     */
    @Override
    public final void whenBound(final MessageStream stream) {
        getValAsync(stream);
    }

    /**
     * Schedule closure to be executed after data became available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled
     *
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     * @return A promise for the results of the supplied closure. This allows for chaining of then() method calls.
     */
    @Override
    public final <V> Promise<V> then(final Closure<V> closure) {
        final DataflowVariable<V> result = new DataflowVariable<V>();
        whenBound(new ThenMessagingRunnable<T, V>(result, closure));
        return result;
    }

    /**
     * Schedule closure to be executed after data becomes available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled.
     *
     * @param pool    The thread pool to use for task scheduling for asynchronous message delivery
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     * @return A promise for the results of the supplied closure. This allows for chaining of then() method calls.
     */
    @Override
    public <V> Promise<V> then(final Pool pool, final Closure<V> closure) {
        final DataflowVariable<V> result = new DataflowVariable<V>();
        whenBound(pool, new ThenMessagingRunnable<T, V>(result, closure));
        return result;
    }

    /**
     * Schedule closure to be executed after data becomes available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled.
     *
     * @param group   The PGroup to use for task scheduling for asynchronous message delivery
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     * @return A promise for the results of the supplied closure. This allows for chaining of then() method calls.
     */
    @Override
    public <V> Promise<V> then(final PGroup group, final Closure<V> closure) {
        final DataflowVariable<V> result = new DataflowVariable<V>();
        whenBound(group, new ThenMessagingRunnable<T, V>(result, closure));
        return result;
    }

    /**
     * Send all pieces of data bound in the future to the provided stream when it becomes available.     *
     *
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     */
    @Override
    public final <V> void wheneverBound(final Closure<V> closure) {
        wheneverBoundListeners.add(new DataCallback(closure, Dataflow.retrieveCurrentDFPGroup()));
    }

    /**
     * Send all pieces of data bound in the future to the provided stream when it becomes available.
     *
     * @param stream stream where to send result
     */
    @Override
    public final void wheneverBound(final MessageStream stream) {
        wheneverBoundListeners.add(stream);
    }

    @Override
    public final <V> DataflowReadChannel<V> chainWith(final Closure<V> closure) {
        return chainWith(Dataflow.retrieveCurrentDFPGroup(), closure);
    }

    @Override
    public final <V> DataflowReadChannel<V> chainWith(final Pool pool, final Closure<V> closure) {
        return chainWith(new DefaultPGroup(pool), closure);
    }

    @Override
    public <V> DataflowReadChannel<V> chainWith(final PGroup group, final Closure<V> closure) {
        final DataflowQueue<V> result = new DataflowQueue<V>();
        group.operator(this, result, new ChainWithClosure<V>(closure));
        return result;
    }

    @Override
    public final <V> DataflowReadChannel<V> chainWith(final Map<String, Object> params, final Closure<V> closure) {
        return chainWith(Dataflow.retrieveCurrentDFPGroup(), params, closure);
    }

    @Override
    public final <V> DataflowReadChannel<V> chainWith(final Pool pool, final Map<String, Object> params, final Closure<V> closure) {
        return chainWith(new DefaultPGroup(pool), params, closure);
    }

    @Override
    public <V> DataflowReadChannel<V> chainWith(final PGroup group, final Map<String, Object> params, final Closure<V> closure) {
        final DataflowQueue<V> result = new DataflowQueue<V>();
        final Map<String, Object> parameters = new HashMap<String, Object>(params);
        parameters.put("inputs", asList(this));
        parameters.put("outputs", asList(result));

        group.operator(parameters, new ChainWithClosure<V>(closure));
        return result;
    }

    @Override
    public <V> DataflowReadChannel<V> or(final Closure<V> closure) {
        return chainWith(closure);
    }

    @Override
    public DataflowReadChannel<T> filter(final Closure<Boolean> closure) {
        return chainWith(new FilterClosure(closure));
    }

    @Override
    public DataflowReadChannel<T> filter(final Pool pool, final Closure<Boolean> closure) {
        return chainWith(pool, new FilterClosure(closure));
    }

    @Override
    public DataflowReadChannel<T> filter(final PGroup group, final Closure<Boolean> closure) {
        return chainWith(group, new FilterClosure(closure));
    }

    @Override
    public DataflowReadChannel<T> filter(final Map<String, Object> params, final Closure<Boolean> closure) {
        return chainWith(params, new FilterClosure(closure));
    }

    @Override
    public DataflowReadChannel<T> filter(final Pool pool, final Map<String, Object> params, final Closure<Boolean> closure) {
        return chainWith(pool, params, new FilterClosure(closure));
    }

    @Override
    public DataflowReadChannel<T> filter(final PGroup group, final Map<String, Object> params, final Closure<Boolean> closure) {
        return chainWith(group, params, new FilterClosure(closure));
    }

    @Override
    public void into(final DataflowWriteChannel<T> target) {
        into(Dataflow.retrieveCurrentDFPGroup(), target);
    }

    @Override
    public void into(final Pool pool, final DataflowWriteChannel<T> target) {
        into(new DefaultPGroup(pool), target);
    }

    @Override
    public void into(final PGroup group, final DataflowWriteChannel<T> target) {
        group.operator(this, target, new ChainWithClosure(new CopyChannelsClosure()));
    }

    @Override
    public void into(final Map<String, Object> params, final DataflowWriteChannel<T> target) {
        into(Dataflow.retrieveCurrentDFPGroup(), params, target);
    }

    @Override
    public void into(final Pool pool, final Map<String, Object> params, final DataflowWriteChannel<T> target) {
        into(new DefaultPGroup(pool), params, target);
    }

    @Override
    public void into(final PGroup group, final Map<String, Object> params, final DataflowWriteChannel<T> target) {
        final Map<String, Object> parameters = new HashMap<String, Object>(params);
        parameters.put("inputs", asList(this));
        parameters.put("outputs", asList(target));
        group.operator(parameters, new ChainWithClosure(new CopyChannelsClosure()));
    }

    @Override
    public void or(final DataflowWriteChannel<T> target) {
        into(target);
    }

    @Override
    public void split(final DataflowWriteChannel<T> target1, final DataflowWriteChannel<T> target2) {
        split(Dataflow.retrieveCurrentDFPGroup(), target1, target2);
    }

    @Override
    public void split(final Pool pool, final DataflowWriteChannel<T> target1, final DataflowWriteChannel<T> target2) {
        split(new DefaultPGroup(pool), target1, target2);
    }

    @Override
    public void split(final PGroup group, final DataflowWriteChannel<T> target1, final DataflowWriteChannel<T> target2) {
        split(group, asList(target1, target2));
    }

    @Override
    public void split(final List<DataflowWriteChannel<T>> targets) {
        split(Dataflow.retrieveCurrentDFPGroup(), targets);
    }

    @Override
    public void split(final Pool pool, final List<DataflowWriteChannel<T>> targets) {
        split(new DefaultPGroup(pool), targets);
    }

    @Override
    public void split(final PGroup group, final List<DataflowWriteChannel<T>> targets) {
        group.operator(asList(this), targets, new ChainWithClosure(new CopyChannelsClosure()));
    }

    @Override
    public void split(final Map<String, Object> params, final DataflowWriteChannel<T> target1, final DataflowWriteChannel<T> target2) {
        split(Dataflow.retrieveCurrentDFPGroup(), params, target1, target2);
    }

    @Override
    public void split(final Pool pool, final Map<String, Object> params, final DataflowWriteChannel<T> target1, final DataflowWriteChannel<T> target2) {
        split(new DefaultPGroup(pool), params, target1, target2);
    }

    @Override
    public void split(final PGroup group, final Map<String, Object> params, final DataflowWriteChannel<T> target1, final DataflowWriteChannel<T> target2) {
        split(group, params, asList(target1, target2));
    }

    @Override
    public void split(final Map<String, Object> params, final List<DataflowWriteChannel<T>> targets) {
        split(Dataflow.retrieveCurrentDFPGroup(), params, targets);
    }

    @Override
    public void split(final Pool pool, final Map<String, Object> params, final List<DataflowWriteChannel<T>> targets) {
        split(new DefaultPGroup(pool), params, targets);
    }

    @Override
    public void split(final PGroup group, final Map<String, Object> params, final List<DataflowWriteChannel<T>> targets) {
        final Map<String, Object> parameters = new HashMap<String, Object>(params);
        parameters.put("inputs", asList(this));
        parameters.put("outputs", asList(targets));

        group.operator(parameters, new ChainWithClosure(new CopyChannelsClosure()));
    }

    @Override
    public DataflowReadChannel<T> tap(final DataflowWriteChannel<T> target) {
        return tap(Dataflow.retrieveCurrentDFPGroup(), target);
    }

    @Override
    public DataflowReadChannel<T> tap(final Pool pool, final DataflowWriteChannel<T> target) {
        return tap(new DefaultPGroup(pool), target);
    }

    @Override
    public DataflowReadChannel<T> tap(final PGroup group, final DataflowWriteChannel<T> target) {
        final DataflowQueue<T> result = new DataflowQueue<T>();
        group.operator(asList(this), asList(result, target), new ChainWithClosure(new CopyChannelsClosure()));
        return result;
    }

    @Override
    public DataflowReadChannel<T> tap(final Map<String, Object> params, final DataflowWriteChannel<T> target) {
        return tap(Dataflow.retrieveCurrentDFPGroup(), params, target);
    }

    @Override
    public DataflowReadChannel<T> tap(final Pool pool, final Map<String, Object> params, final DataflowWriteChannel<T> target) {
        return tap(new DefaultPGroup(pool), params, target);
    }

    @Override
    public DataflowReadChannel<T> tap(final PGroup group, final Map<String, Object> params, final DataflowWriteChannel<T> target) {
        final DataflowQueue<T> result = new DataflowQueue<T>();
        final Map<String, Object> parameters = new HashMap<String, Object>(params);
        parameters.put("inputs", asList(this));
        parameters.put("outputs", asList(result, target));

        group.operator(parameters, new ChainWithClosure(new CopyChannelsClosure()));
        return result;
    }

    @Override
    public <V> DataflowReadChannel<V> merge(final DataflowReadChannel<Object> other, final Closure<V> closure) {
        return merge(asList(other), closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(final Pool pool, final DataflowReadChannel<Object> other, final Closure<V> closure) {
        return merge(pool, asList(other), closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(final PGroup group, final DataflowReadChannel<Object> other, final Closure<V> closure) {
        return merge(group, asList(other), closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(final List<DataflowReadChannel<Object>> others, final Closure<V> closure) {
        return merge(Dataflow.retrieveCurrentDFPGroup(), others, closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(final Pool pool, final List<DataflowReadChannel<Object>> others, final Closure<V> closure) {
        return merge(new DefaultPGroup(pool), others, closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(final PGroup group, final List<DataflowReadChannel<Object>> others, final Closure<V> closure) {
        final DataflowQueue<V> result = new DataflowQueue<V>();
        final List<DataflowReadChannel<?>> inputs = new ArrayList<DataflowReadChannel<?>>();
        inputs.add(this);
        inputs.addAll(others);
        group.operator(inputs, asList(result), new ChainWithClosure(closure));
        return result;
    }

    @Override
    public <V> DataflowReadChannel<V> merge(final Map<String, Object> params, final DataflowReadChannel<Object> other, final Closure<V> closure) {
        return merge(params, asList(other), closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(final Pool pool, final Map<String, Object> params, final DataflowReadChannel<Object> other, final Closure<V> closure) {
        return merge(pool, params, asList(other), closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(final PGroup group, final Map<String, Object> params, final DataflowReadChannel<Object> other, final Closure<V> closure) {
        return merge(group, params, asList(other), closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(final Map<String, Object> params, final List<DataflowReadChannel<Object>> others, final Closure<V> closure) {
        return merge(Dataflow.retrieveCurrentDFPGroup(), params, others, closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(final Pool pool, final Map<String, Object> params, final List<DataflowReadChannel<Object>> others, final Closure<V> closure) {
        return merge(new DefaultPGroup(pool), params, others, closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(final PGroup group, final Map<String, Object> params, final List<DataflowReadChannel<Object>> others, final Closure<V> closure) {
        final DataflowQueue<V> result = new DataflowQueue<V>();
        final Collection<DataflowReadChannel<?>> inputs = new ArrayList<DataflowReadChannel<?>>();
        inputs.add(this);
        inputs.addAll(others);
        final Map<String, Object> parameters = new HashMap<String, Object>(params);
        parameters.put("inputs", inputs);
        parameters.put("outputs", asList(result));
        group.operator(parameters, new ChainWithClosure(closure));
        return result;
    }

    @Override
    public void binaryChoice(final DataflowWriteChannel<T> trueBranch, final DataflowWriteChannel<T> falseBranch, final Closure<Boolean> code) {
        binaryChoice(Dataflow.retrieveCurrentDFPGroup(), trueBranch, falseBranch, code);
    }

    @Override
    public void binaryChoice(final Pool pool, final DataflowWriteChannel<T> trueBranch, final DataflowWriteChannel<T> falseBranch, final Closure<Boolean> code) {
        binaryChoice(new DefaultPGroup(pool), trueBranch, falseBranch, code);
    }

    @Override
    public void binaryChoice(final PGroup group, final DataflowWriteChannel<T> trueBranch, final DataflowWriteChannel<T> falseBranch, final Closure<Boolean> code) {
        group.operator(asList(this), asList(trueBranch, falseBranch), new BinaryChoiceClosure(code));
    }

    @Override
    public void binaryChoice(final Map<String, Object> params, final DataflowWriteChannel<T> trueBranch, final DataflowWriteChannel<T> falseBranch, final Closure<Boolean> code) {
        binaryChoice(Dataflow.retrieveCurrentDFPGroup(), params, trueBranch, falseBranch, code);
    }

    @Override
    public void binaryChoice(final Pool pool, final Map<String, Object> params, final DataflowWriteChannel<T> trueBranch, final DataflowWriteChannel<T> falseBranch, final Closure<Boolean> code) {
        binaryChoice(new DefaultPGroup(pool), params, trueBranch, falseBranch, code);
    }

    @Override
    public void binaryChoice(final PGroup group, final Map<String, Object> params, final DataflowWriteChannel<T> trueBranch, final DataflowWriteChannel<T> falseBranch, final Closure<Boolean> code) {
        final Map<String, Object> parameters = new HashMap<String, Object>(params);
        parameters.put("inputs", asList(this));
        parameters.put("outputs", asList(trueBranch, falseBranch));

        group.operator(parameters, new BinaryChoiceClosure(code));
    }

    @Override
    public void choice(final List<DataflowWriteChannel<T>> outputs, final Closure<Integer> code) {
        choice(Dataflow.retrieveCurrentDFPGroup(), outputs, code);
    }

    @Override
    public void choice(final Pool pool, final List<DataflowWriteChannel<T>> outputs, final Closure<Integer> code) {
        choice(new DefaultPGroup(pool), outputs, code);
    }

    @Override
    public void choice(final PGroup group, final List<DataflowWriteChannel<T>> outputs, final Closure<Integer> code) {
        group.operator(asList(this), outputs, new ChoiceClosure(code));
    }

    @Override
    public void choice(final Map<String, Object> params, final List<DataflowWriteChannel<T>> outputs, final Closure<Integer> code) {
        choice(Dataflow.retrieveCurrentDFPGroup(), params, outputs, code);
    }

    @Override
    public void choice(final Pool pool, final Map<String, Object> params, final List<DataflowWriteChannel<T>> outputs, final Closure<Integer> code) {
        choice(new DefaultPGroup(pool), params, outputs, code);
    }

    @Override
    public void choice(final PGroup group, final Map<String, Object> params, final List<DataflowWriteChannel<T>> outputs, final Closure<Integer> code) {
        final Map<String, Object> parameters = new HashMap<String, Object>(params);
        parameters.put("inputs", asList(this));
        parameters.put("outputs", asList(outputs));

        group.operator(parameters, new ChoiceClosure(code));
    }

    @Override
    public void separate(final List<DataflowWriteChannel<?>> outputs, final Closure<List<Object>> code) {
        separate(Dataflow.retrieveCurrentDFPGroup(), outputs, code);
    }

    @Override
    public void separate(final Pool pool, final List<DataflowWriteChannel<?>> outputs, final Closure<List<Object>> code) {
        separate(new DefaultPGroup(pool), outputs, code);
    }

    @Override
    public void separate(final PGroup group, final List<DataflowWriteChannel<?>> outputs, final Closure<List<Object>> code) {
        group.operator(asList(this), outputs, new SeparationClosure(code));
    }

    @Override
    public void separate(final Map<String, Object> params, final List<DataflowWriteChannel<?>> outputs, final Closure<List<Object>> code) {
        separate(Dataflow.retrieveCurrentDFPGroup(), params, outputs, code);
    }

    @Override
    public void separate(final Pool pool, final Map<String, Object> params, final List<DataflowWriteChannel<?>> outputs, final Closure<List<Object>> code) {
        separate(new DefaultPGroup(pool), params, outputs, code);
    }

    @Override
    public void separate(final PGroup group, final Map<String, Object> params, final List<DataflowWriteChannel<?>> outputs, final Closure<List<Object>> code) {
        final Map<String, Object> parameters = new HashMap<String, Object>(params);
        parameters.put("inputs", asList(this));
        parameters.put("outputs", asList(outputs));

        group.operator(parameters, new SeparationClosure(code));
    }

    /**
     * Check if value has been set already for this expression
     *
     * @return true if bound already
     */
    @Override
    public final boolean isBound() {
        return !queue.isEmpty();
    }

    /**
     * Checks whether there's a DFV waiting in the queue and retrieves it. If not, a new unmatched value request, represented
     * by a new DFV, is added to the requests queue.
     *
     * @return The DFV to wait for value on
     */
    private DataflowVariable<T> retrieveOrCreateVariable() {
        return copyDFV(queue, requests);
    }

    /**
     * Returns the current size of the buffer
     *
     * @return Number of DFVs in the queue
     */
    @Override
    public final int length() {
        return queue.size();
    }

    /**
     * Returns an iterator over a current snapshot of the buffer's content. The next() method returns actual values
     * not the DataflowVariables.
     *
     * @return AN iterator over all DFVs in the queue
     */
    public final Iterator<T> iterator() {
        final Iterator<DataflowVariable<T>> iterator = queue.iterator();
        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                try {
                    return iterator.next().getVal();
                } catch (InterruptedException e) {
                    throw new IllegalStateException("The thread has been interrupted, which prevented the iterator from retrieving the next element.", e);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove not available");
            }
        };

    }

    private volatile DataflowChannelEventOrchestrator<T> eventManager;

    @Override
    public synchronized DataflowChannelEventListenerManager<T> getEventManager() {
        if (eventManager != null) return eventManager;
        eventManager = new DataflowChannelEventOrchestrator<T>();
        return eventManager;
    }

    private void fireOnMessage(final T value) {
        if (eventManager != null) {
            eventManager.fireOnMessage(value);
        }
    }

    final LinkedBlockingQueue<DataflowVariable<T>> getQueue() {
        return queue;
    }

    @Override
    public String toString() {
        return "DataflowQueue(queue=" + new ArrayList<DataflowVariable<T>>(queue).toString() + ')';
    }

    @Override
    public Class<RemoteDataflowQueue> getRemoteClass() {
        return RemoteDataflowQueue.class;
    }
}
