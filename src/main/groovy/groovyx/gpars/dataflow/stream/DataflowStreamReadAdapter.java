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
import groovyx.gpars.dataflow.Dataflow;
import groovyx.gpars.dataflow.DataflowChannelListener;
import groovyx.gpars.dataflow.DataflowQueue;
import groovyx.gpars.dataflow.DataflowReadChannel;
import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.dataflow.DataflowWriteChannel;
import groovyx.gpars.dataflow.Promise;
import groovyx.gpars.dataflow.SyncDataflowVariable;
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
import groovyx.gpars.group.DefaultPGroup;
import groovyx.gpars.group.PGroup;
import groovyx.gpars.scheduler.Pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

/**
 * Adapts a DataflowStream to accommodate for the DataflowReadChannel interface.
 * To minimize the overhead and stay in-line with the DataflowStream semantics, the DataflowStreamReadAdapter class is not thread-safe
 * and should only be used from within a single thread.
 * If multiple threads need to read from a DataflowStream, they should each create their own wrapping DataflowStreamReadAdapter.
 *
 * @param <T> The type of messages to pass through the stream
 * @author Vaclav Pech
 */
@SuppressWarnings({"unchecked"})
public class DataflowStreamReadAdapter<T> implements DataflowReadChannel<T> {

    private StreamCore<T> head;
    private StreamCore<T> asyncHead;

    /**
     * Creates a new adapter
     *
     * @param stream The stream to wrap
     */
    public DataflowStreamReadAdapter(final StreamCore<T> stream) {
        this.head = stream;
        this.asyncHead = head;

        this.head.addUpdateListener(new DataflowChannelListener<T>() {
            @Override
            public void onMessage(final T message) {
                fireOnMessage(message);
            }
        });
    }

    public Iterator<T> iterator() {
        return new FListIterator<T>(head);
    }

    @SuppressWarnings("ObjectToString")
    @Override
    public final String toString() {
        return head.toString();
    }

    @Override
    public T getVal() throws InterruptedException {
        final T first = head.getFirst();
        moveHead();
        return first;
    }

    @Override
    public T getVal(final long timeout, final TimeUnit units) throws InterruptedException {
        final T value = head.getFirstDFV().getVal(timeout, units);
        if (value == null) {
            if (shouldReportTimeout()) {
                return null;
            } else {
                final T result = head.getFirstDFV().getVal();
                moveHead();
                return result;
            }
        } else {
            moveHead();
            return value;
        }
    }

    private boolean shouldReportTimeout() {
        final DataflowVariable<T> firstDFV = head.getFirstDFV();
        if (!firstDFV.isBound()) return true;
        if (firstDFV instanceof SyncDataflowVariable) {
            return ((SyncDataflowVariable<T>) firstDFV).awaitingParties();
        }
        return false;
    }

    @Override
    public void getValAsync(final MessageStream callback) {
        asyncHead.getFirstDFV().getValAsync(callback);
        moveAsyncHead();
    }

    @Override
    public void getValAsync(final Object attachment, final MessageStream callback) {
        asyncHead.getFirstDFV().getValAsync(attachment, callback);
        moveAsyncHead();
    }

    @Override
    public <V> Promise<V> rightShift(final Closure<V> closure) {
        return then(closure);
    }

    @Override
    public <V> void whenBound(final Closure<V> closure) {
        asyncHead.getFirstDFV().whenBound(closure);
        moveAsyncHead();
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
    public <V> void whenBound(final Pool pool, final Closure<V> closure) {
        asyncHead.getFirstDFV().whenBound(pool, closure);
        moveAsyncHead();
    }

    @Override
    public <V> void whenBound(final PGroup group, final Closure<V> closure) {
        asyncHead.getFirstDFV().whenBound(group, closure);
        moveAsyncHead();
    }

    @Override
    public void whenBound(final MessageStream stream) {
        asyncHead.getFirstDFV().whenBound(stream);
        moveAsyncHead();
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

    @Override
    public <V> void wheneverBound(final Closure<V> closure) {
        head.wheneverBound(closure);
    }

    @Override
    public void wheneverBound(final MessageStream stream) {
        head.wheneverBound(stream);
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

    @Override
    public boolean isBound() {
        return head.getFirstDFV().isBound();
    }

    /**
     * Returns the current size of the buffer
     *
     * @return Number of DFVs in the queue
     */
    @Override
    public int length() {
        StreamCore<T> current = head;
        int length = 0;
        while (current.getFirstDFV().isBound()) {
            length += 1;
            current = (StreamCore<T>) current.getRest();
        }
        return length;
    }

    @Override
    public DataflowExpression<T> poll() throws InterruptedException {
        final DataflowVariable<T> firstDFV = head.getFirstDFV();
        if (firstDFV.isBound()) {
            moveHead();
            return firstDFV;
        } else return null;
    }

    protected final List<DataflowVariable<T>> allUnprocessedDFVs() throws InterruptedException {
        final List<DataflowVariable<T>> values = new ArrayList<DataflowVariable<T>>();
        StreamCore<T> currentHead = asyncHead;
        while (currentHead != null) {
            values.add(currentHead.getFirstDFV());
            currentHead = (StreamCore<T>) currentHead.rest.get();
        }
        return values;
    }

    private void moveHead() {
        asyncHead = (StreamCore<T>) asyncHead.getRest();
        head = (StreamCore<T>) head.getRest();
    }

    private void moveAsyncHead() {
        moveHead();
    }
}

