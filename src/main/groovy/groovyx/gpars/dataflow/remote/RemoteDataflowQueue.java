// GPars - Groovy Parallel Systems
//
// Copyright © 2014  The original author or authors
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

package groovyx.gpars.dataflow.remote;

import groovy.lang.Closure;
import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.dataflow.*;
import groovyx.gpars.dataflow.expression.DataflowExpression;
import groovyx.gpars.dataflow.impl.DataflowChannelEventListenerManager;
import groovyx.gpars.dataflow.impl.ThenMessagingRunnable;
import groovyx.gpars.dataflow.operator.*;
import groovyx.gpars.group.DefaultPGroup;
import groovyx.gpars.group.PGroup;
import groovyx.gpars.remote.RemoteHost;
import groovyx.gpars.remote.message.RemoteDataflowQueueEnqueueValueMsg;
import groovyx.gpars.remote.message.RemoteDataflowQueueValueRequestMsg;
import groovyx.gpars.scheduler.Pool;
import groovyx.gpars.serial.RemoteSerialized;
import groovyx.gpars.serial.WithSerialId;

import static java.util.Arrays.asList;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Proxy object for remote instance of Queue.
 * @param <T> the type of the queue
 *
 * @author Rafal Slawik
 */
public class RemoteDataflowQueue<T> extends WithSerialId implements DataflowChannel<T>, RemoteSerialized {
    private RemoteHost remoteHost;

    public RemoteDataflowQueue(RemoteHost host) {
        this.remoteHost = host;
    }

    @Override
    public void getValAsync(MessageStream callback) {
        getValAsync(null, callback);
    }

    @Override
    public void getValAsync(Object attachment, MessageStream callback) {
        DataflowVariable<T> value = createRequestVariable();
        value.getValAsync(attachment, callback);
    }

    @Override
    public T getVal() throws InterruptedException {
        DataflowVariable<T> value = createRequestVariable();
        return value.getVal();
    }

    @Override
    public T getVal(long timeout, TimeUnit units) throws InterruptedException {
        final DataflowVariable<T> value = createRequestVariable();
        return value.getVal(timeout, units);
    }

    @Override
    public <V> Promise<V> rightShift(Closure<V> closure) {
        return then(closure);
    }

    @Override
    public <V> void whenBound(Closure<V> closure) {
        getValAsync(new DataCallback(closure, Dataflow.retrieveCurrentDFPGroup()));
    }

    @Override
    public <V> void whenBound(Pool pool, Closure<V> closure) {
        getValAsync(new DataCallbackWithPool(pool, closure));
    }

    @Override
    public <V> void whenBound(PGroup group, Closure<V> closure) {
        getValAsync(new DataCallback(closure, group));
    }

    @Override
    public void whenBound(MessageStream stream) {
        getValAsync(stream);
    }

    @Override
    public <V> Promise<V> then(Closure<V> closure) {
        final DataflowVariable<V> result = new DataflowVariable<>();
        whenBound(new ThenMessagingRunnable<>(result, closure));
        return result;
    }

    @Override
    public <V> Promise<V> then(Pool pool, Closure<V> closure) {
        final DataflowVariable<V> result = new DataflowVariable<>();
        whenBound(pool, new ThenMessagingRunnable<>(result, closure));
        return result;
    }

    @Override
    public <V> Promise<V> then(PGroup group, Closure<V> closure) {
        final DataflowVariable<V> result = new DataflowVariable<>();
        whenBound(group, new ThenMessagingRunnable<>(result, closure));
        return result;
    }

    @Override
    public <V> DataflowReadChannel<V> chainWith(Closure<V> closure) {
        return chainWith(Dataflow.retrieveCurrentDFPGroup(), closure);
    }

    @Override
    public <V> DataflowReadChannel<V> chainWith(Pool pool, Closure<V> closure) {
        return chainWith(new DefaultPGroup(pool), closure);
    }

    @Override
    public <V> DataflowReadChannel<V> chainWith(PGroup group, Closure<V> closure) {
        final DataflowQueue<V> result = new DataflowQueue<>();
        group.operator(this, result, new ChainWithClosure<V>(closure));
        return result;
    }

    @Override
    public <V> DataflowReadChannel<V> chainWith(Map<String, Object> params, Closure<V> closure) {
        return chainWith(Dataflow.retrieveCurrentDFPGroup(), params, closure);
    }

    @Override
    public <V> DataflowReadChannel<V> chainWith(Pool pool, Map<String, Object> params, Closure<V> closure) {
        return chainWith(new DefaultPGroup(pool), params, closure);
    }

    @Override
    public <V> DataflowReadChannel<V> chainWith(PGroup group, Map<String, Object> params, Closure<V> closure) {
        final DataflowQueue<V> result = new DataflowQueue<>();
        final Map<String, Object> parameters = new HashMap<>(params);
        parameters.put("inputs", asList(this));
        parameters.put("outputs", asList(result));

        group.operator(parameters, new ChainWithClosure<>(closure));
        return result;
    }

    @Override
    public <V> DataflowReadChannel<V> or(Closure<V> closure) {
        return chainWith(closure);
    }

    @Override
    public DataflowReadChannel<T> filter(Closure<Boolean> closure) {
        return chainWith(new FilterClosure<>(closure));
    }

    @Override
    public DataflowReadChannel<T> filter(Pool pool, Closure<Boolean> closure) {
        return chainWith(pool, new FilterClosure<>(closure));
    }

    @Override
    public DataflowReadChannel<T> filter(PGroup group, Closure<Boolean> closure) {
        return chainWith(group, new FilterClosure<>(closure));
    }

    @Override
    public DataflowReadChannel<T> filter(Map<String, Object> params, Closure<Boolean> closure) {
        return chainWith(params, new FilterClosure<>(closure));
    }

    @Override
    public DataflowReadChannel<T> filter(Pool pool, Map<String, Object> params, Closure<Boolean> closure) {
        return chainWith(pool, params, new FilterClosure<>(closure));
    }

    @Override
    public DataflowReadChannel<T> filter(PGroup group, Map<String, Object> params, Closure<Boolean> closure) {
        return chainWith(group, params, new FilterClosure<>(closure));
    }

    @Override
    public void into(DataflowWriteChannel<T> target) {
        into(Dataflow.retrieveCurrentDFPGroup(), target);
    }

    @Override
    public void into(Pool pool, DataflowWriteChannel<T> target) {
        into(new DefaultPGroup(pool), target);
    }

    @Override
    public void into(PGroup group, DataflowWriteChannel<T> target) {
        group.operator(this, target, new ChainWithClosure(new CopyChannelsClosure()));
    }

    @Override
    public void into(Map<String, Object> params, DataflowWriteChannel<T> target) {
        into(Dataflow.retrieveCurrentDFPGroup(), params, target);
    }

    @Override
    public void into(Pool pool, Map<String, Object> params, DataflowWriteChannel<T> target) {
        into(new DefaultPGroup(pool), params, target);
    }

    @Override
    public void into(PGroup group, Map<String, Object> params, DataflowWriteChannel<T> target) {
        final Map<String, Object> parameters = new HashMap<>(params);
        parameters.put("inputs", asList(this));
        parameters.put("outputs", asList(target));
        group.operator(parameters, new ChainWithClosure<>(new CopyChannelsClosure()));
    }

    @Override
    public void or(DataflowWriteChannel<T> target) {
        into(target);
    }

    @Override
    public void split(DataflowWriteChannel<T> target1, DataflowWriteChannel<T> target2) {
        split(Dataflow.retrieveCurrentDFPGroup(), target1, target2);
    }

    @Override
    public void split(Pool pool, DataflowWriteChannel<T> target1, DataflowWriteChannel<T> target2) {
        split(new DefaultPGroup(pool), target1, target2);
    }

    @Override
    public void split(PGroup group, DataflowWriteChannel<T> target1, DataflowWriteChannel<T> target2) {
        split(group, asList(target1, target2));
    }

    @Override
    public void split(List<DataflowWriteChannel<T>> targets) {
        split(Dataflow.retrieveCurrentDFPGroup(), targets);
    }

    @Override
    public void split(Pool pool, List<DataflowWriteChannel<T>> targets) {
        split(new DefaultPGroup(pool), targets);
    }

    @Override
    public void split(PGroup group, List<DataflowWriteChannel<T>> targets) {
        group.operator(asList(this), targets, new ChainWithClosure(new CopyChannelsClosure()));
    }

    @Override
    public void split(Map<String, Object> params, DataflowWriteChannel<T> target1, DataflowWriteChannel<T> target2) {
        split(Dataflow.retrieveCurrentDFPGroup(), params, target1, target2);
    }

    @Override
    public void split(Pool pool, Map<String, Object> params, DataflowWriteChannel<T> target1, DataflowWriteChannel<T> target2) {
        split(new DefaultPGroup(pool), params, target1, target2);
    }

    @Override
    public void split(PGroup group, Map<String, Object> params, DataflowWriteChannel<T> target1, DataflowWriteChannel<T> target2) {
        split(group, params, asList(target1, target2));
    }

    @Override
    public void split(Map<String, Object> params, List<DataflowWriteChannel<T>> targets) {
        split(Dataflow.retrieveCurrentDFPGroup(), targets);
    }

    @Override
    public void split(Pool pool, Map<String, Object> params, List<DataflowWriteChannel<T>> targets) {
        split(new DefaultPGroup(pool), params, targets);
    }

    @Override
    public void split(PGroup group, Map<String, Object> params, List<DataflowWriteChannel<T>> targets) {
        final Map<String, Object> parameters = new HashMap<>(params);
        parameters.put("inputs", asList(this));
        parameters.put("outputs", asList(targets));
        group.operator(parameters, new ChainWithClosure<>(new CopyChannelsClosure()));
    }

    @Override
    public DataflowReadChannel<T> tap(DataflowWriteChannel<T> target) {
        return tap(Dataflow.retrieveCurrentDFPGroup(), target);
    }

    @Override
    public DataflowReadChannel<T> tap(Pool pool, DataflowWriteChannel<T> target) {
        return tap(new DefaultPGroup(pool), target);
    }

    @Override
    public DataflowReadChannel<T> tap(PGroup group, DataflowWriteChannel<T> target) {
        final DataflowQueue<T> result = new DataflowQueue<>();
        group.operator(asList(this), asList(result, target), new ChainWithClosure(new CopyChannelsClosure()));
        return result;
    }

    @Override
    public DataflowReadChannel<T> tap(Map<String, Object> params, DataflowWriteChannel<T> target) {
        return tap(Dataflow.retrieveCurrentDFPGroup(), params, target);
    }

    @Override
    public DataflowReadChannel<T> tap(Pool pool, Map<String, Object> params, DataflowWriteChannel<T> target) {
        return tap(new DefaultPGroup(pool), params, target);
    }

    @Override
    public DataflowReadChannel<T> tap(PGroup group, Map<String, Object> params, DataflowWriteChannel<T> target) {
        final DataflowQueue<T> result = new DataflowQueue<>();
        final Map<String, Object> parameters = new HashMap<>(params);
        parameters.put("inputs", asList(this));
        parameters.put("outputs", asList(result, target));
        group.operator(parameters, new ChainWithClosure<>(new CopyChannelsClosure()));
        return result;
    }

    @Override
    public <V> DataflowReadChannel<V> merge(DataflowReadChannel<Object> other, Closure<V> closure) {
        return merge(asList(other), closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(Pool pool, DataflowReadChannel<Object> other, Closure<V> closure) {
        return merge(pool, asList(other), closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(PGroup group, DataflowReadChannel<Object> other, Closure<V> closure) {
        return merge(group, asList(other), closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(List<DataflowReadChannel<Object>> others, Closure<V> closure) {
        return merge(Dataflow.retrieveCurrentDFPGroup(), others, closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(Pool pool, List<DataflowReadChannel<Object>> others, Closure<V> closure) {
        return merge(new DefaultPGroup(pool), others, closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(PGroup group, List<DataflowReadChannel<Object>> others, Closure<V> closure) {
        final DataflowQueue<V> result = new DataflowQueue<>();
        final List<DataflowReadChannel<?>> inputs = new ArrayList<>();
        inputs.add(this);
        inputs.addAll(others);
        group.operator(inputs, asList(result), new ChainWithClosure(closure));
        return result;
    }

    @Override
    public <V> DataflowReadChannel<V> merge(Map<String, Object> params, DataflowReadChannel<Object> other, Closure<V> closure) {
        return merge(params, asList(other), closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(Pool pool, Map<String, Object> params, DataflowReadChannel<Object> other, Closure<V> closure) {
        return merge(pool, params, asList(other), closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(PGroup group, Map<String, Object> params, DataflowReadChannel<Object> other, Closure<V> closure) {
        return merge(group, params, asList(other), closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(Map<String, Object> params, List<DataflowReadChannel<Object>> others, Closure<V> closure) {
        return merge(Dataflow.retrieveCurrentDFPGroup(), params, others, closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(Pool pool, Map<String, Object> params, List<DataflowReadChannel<Object>> others, Closure<V> closure) {
        return merge(new DefaultPGroup(pool), params, others, closure);
    }

    @Override
    public <V> DataflowReadChannel<V> merge(PGroup group, Map<String, Object> params, List<DataflowReadChannel<Object>> others, Closure<V> closure) {
        final DataflowQueue<V> result = new DataflowQueue<>();
        final List<DataflowReadChannel<?>> inputs = new ArrayList<>();
        inputs.add(this);
        inputs.addAll(others);
        final Map<String, Object> parameters = new HashMap<>(params);
        parameters.put("inputs", inputs);
        parameters.put("outputs", asList(result));
        group.operator(parameters, new ChainWithClosure<>(closure));
        return result;
    }

    @Override
    public void binaryChoice(DataflowWriteChannel<T> trueBranch, DataflowWriteChannel<T> falseBranch, Closure<Boolean> code) {
        binaryChoice(Dataflow.retrieveCurrentDFPGroup(), trueBranch, falseBranch, code);
    }

    @Override
    public void binaryChoice(Pool pool, DataflowWriteChannel<T> trueBranch, DataflowWriteChannel<T> falseBranch, Closure<Boolean> code) {
        binaryChoice(new DefaultPGroup(pool), trueBranch, falseBranch, code);
    }

    @Override
    public void binaryChoice(PGroup group, DataflowWriteChannel<T> trueBranch, DataflowWriteChannel<T> falseBranch, Closure<Boolean> code) {
        group.operator(asList(this), asList(trueBranch, falseBranch), new BinaryChoiceClosure(code));
    }

    @Override
    public void binaryChoice(Map<String, Object> params, DataflowWriteChannel<T> trueBranch, DataflowWriteChannel<T> falseBranch, Closure<Boolean> code) {
        binaryChoice(Dataflow.retrieveCurrentDFPGroup(), params, trueBranch, falseBranch, code);
    }

    @Override
    public void binaryChoice(Pool pool, Map<String, Object> params, DataflowWriteChannel<T> trueBranch, DataflowWriteChannel<T> falseBranch, Closure<Boolean> code) {
        binaryChoice(new DefaultPGroup(pool), params, trueBranch, falseBranch, code);
    }

    @Override
    public void binaryChoice(PGroup group, Map<String, Object> params, DataflowWriteChannel<T> trueBranch, DataflowWriteChannel<T> falseBranch, Closure<Boolean> code) {
        final Map<String, Object> parameters = new HashMap<>(params);
        parameters.put("inputs", asList(this));
        parameters.put("outputs", asList(trueBranch, falseBranch));
        group.operator(parameters, new BinaryChoiceClosure(code));
    }

    @Override
    public void choice(List<DataflowWriteChannel<T>> outputs, Closure<Integer> code) {
        choice(Dataflow.retrieveCurrentDFPGroup(), outputs, code);
    }

    @Override
    public void choice(Pool pool, List<DataflowWriteChannel<T>> outputs, Closure<Integer> code) {
        choice(new DefaultPGroup(pool), outputs, code);
    }

    @Override
    public void choice(PGroup group, List<DataflowWriteChannel<T>> outputs, Closure<Integer> code) {
        group.operator(asList(this), outputs, new ChoiceClosure(code));
    }

    @Override
    public void choice(Map<String, Object> params, List<DataflowWriteChannel<T>> outputs, Closure<Integer> code) {
        choice(Dataflow.retrieveCurrentDFPGroup(), params, outputs, code);
    }

    @Override
    public void choice(Pool pool, Map<String, Object> params, List<DataflowWriteChannel<T>> outputs, Closure<Integer> code) {
        choice(new DefaultPGroup(pool), params, outputs, code);
    }

    @Override
    public void choice(PGroup group, Map<String, Object> params, List<DataflowWriteChannel<T>> outputs, Closure<Integer> code) {
        final Map<String, Object> parameters = new HashMap<>(params);
        parameters.put("inputs", asList(this));
        parameters.put("outputs", asList(outputs));
        group.operator(parameters, new ChoiceClosure(code));
    }

    @Override
    public void separate(List<DataflowWriteChannel<?>> outputs, Closure<List<Object>> code) {
        separate(Dataflow.retrieveCurrentDFPGroup(), outputs, code);
    }

    @Override
    public void separate(Pool pool, List<DataflowWriteChannel<?>> outputs, Closure<List<Object>> code) {
        separate(new DefaultPGroup(pool), outputs, code);
    }

    @Override
    public void separate(PGroup group, List<DataflowWriteChannel<?>> outputs, Closure<List<Object>> code) {
        group.operator(asList(this), outputs, new SeparationClosure(code));
    }

    @Override
    public void separate(Map<String, Object> params, List<DataflowWriteChannel<?>> outputs, Closure<List<Object>> code) {
        separate(Dataflow.retrieveCurrentDFPGroup(), params, outputs, code);
    }

    @Override
    public void separate(Pool pool, Map<String, Object> params, List<DataflowWriteChannel<?>> outputs, Closure<List<Object>> code) {
        separate(new DefaultPGroup(pool), params, outputs, code);
    }

    @Override
    public void separate(PGroup group, Map<String, Object> params, List<DataflowWriteChannel<?>> outputs, Closure<List<Object>> code) {
        final Map<String, Object> parameters = new HashMap<>(params);
        parameters.put("inputs", asList(this));
        parameters.put("outputs", asList(outputs));
        group.operator(parameters, new SeparationClosure(code));
    }

    @Override
    public DataflowChannelEventListenerManager<T> getEventManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBound() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int length() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sends passed in value to queue on remote host.
     * @param value The value to enqueue
     * @return this
     */
    @Override
    public DataflowWriteChannel<T> leftShift(T value) {
        enqueueValue(value);
        return this;
    }

    @Override
    public void bind(T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataflowWriteChannel<T> leftShift(DataflowReadChannel<T> ref) {
        ref.getValAsync(new MessageStream() {
            @Override
            public MessageStream send(Object message) {
                enqueueValue((T) message);
                return this;
            }
        });
        return this;
    }

    @Override
    public <V> void wheneverBound(Closure<V> closure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void wheneverBound(MessageStream stream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataflowExpression<T> poll() throws InterruptedException {
        // there is no buffer
        return null;
    }

    /**
     * Creates a new variable and sends request to queue on remote host asking for value
     * @return The newly created DataflowVariable instance
     */
    private DataflowVariable<T> createRequestVariable() {
        DataflowVariable<T> value = new DataflowVariable<>();
        remoteHost.write(new RemoteDataflowQueueValueRequestMsg<>(this, value));
        return value;
    }

    /**
     * Sends value to queue on remote host
     * @param value
     */
    private void enqueueValue(T value) {
        remoteHost.write(new RemoteDataflowQueueEnqueueValueMsg<T>(this, value));
    }
}
