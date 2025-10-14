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

package groovyx.gpars.dataflow.expression;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.MetaProperty;
import groovyx.gpars.DataflowMessagingRunnable;
import groovyx.gpars.MessagingRunnable;
import groovyx.gpars.actor.Actors;
import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.dataflow.DataCallback;
import groovyx.gpars.dataflow.DataCallbackWithPool;
import groovyx.gpars.dataflow.Dataflow;
import groovyx.gpars.dataflow.DataflowReadChannel;
import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.dataflow.DataflowWriteChannel;
import groovyx.gpars.dataflow.Promise;
import groovyx.gpars.dataflow.impl.BindErrorListenerManager;
import groovyx.gpars.dataflow.impl.DataflowChannelEventListenerManager;
import groovyx.gpars.dataflow.impl.DataflowChannelEventOrchestrator;
import groovyx.gpars.dataflow.impl.ThenMessagingRunnable;
import groovyx.gpars.dataflow.operator.BinaryChoiceClosure;
import groovyx.gpars.dataflow.operator.ChainWithClosure;
import groovyx.gpars.dataflow.operator.ChoiceClosure;
import groovyx.gpars.dataflow.operator.FilterClosure;
import groovyx.gpars.dataflow.operator.SeparationClosure;
import groovyx.gpars.group.DefaultPGroup;
import groovyx.gpars.group.PGroup;
import groovyx.gpars.remote.RemoteConnection;
import groovyx.gpars.remote.RemoteHost;
import groovyx.gpars.scheduler.Pool;
import groovyx.gpars.serial.SerialContext;
import groovyx.gpars.serial.SerialMsg;
import groovyx.gpars.serial.WithSerialId;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.LockSupport;

import static java.util.Arrays.asList;

/**
 * The base class for all dataflow elements.
 *
 * @author Alex Tkachman, Vaclav Pech
 */
@SuppressWarnings({"UnqualifiedStaticUsage", "CallToSimpleGetterFromWithinClass", "ConstantDeclaredInAbstractClass", "unchecked"})
public abstract class DataflowExpression<T> extends WithSerialId implements GroovyObject, DataflowReadChannel<T> {

    private static final String ATTACHMENT = "attachment";
    private static final String RESULT = "result";

    /**
     * Updater for the state field
     */
    @SuppressWarnings({"rawtypes", "RawUseOfParameterizedType"})
    protected static final AtomicIntegerFieldUpdater<DataflowExpression> stateUpdater
            = AtomicIntegerFieldUpdater.newUpdater(DataflowExpression.class, "state");

    /**
     * Updater for the waiting field
     */
    @SuppressWarnings({"rawtypes", "RawUseOfParameterizedType"})
    protected static final AtomicReferenceFieldUpdater<DataflowExpression, WaitingThread> waitingUpdater
            = AtomicReferenceFieldUpdater.newUpdater(DataflowExpression.class, WaitingThread.class, "waiting");
    private static final long serialVersionUID = 8961916630562820109L;
    private static final String CANNOT_FIRE_BIND_ERRORS_THE_THREAD_HAS_BEEN_INTERRUPTED = "Cannot fire bind errors - the thread has been interrupted.";
    private static final String A_DATAFLOW_VARIABLE_CAN_ONLY_BE_ASSIGNED_ONCE_ONLY_RE_ASSIGNMENTS_TO_AN_EQUAL_VALUE_ARE_ALLOWED = "A DataflowVariable can only be assigned once. Only re-assignments to an equal value are allowed.";

    /**
     * The current metaclass
     */
    private MetaClass metaClass = InvokerHelper.getMetaClass(getClass());

    /**
     * Holds the actual value. Is null before a concrete value is bound to it.
     */
    @SuppressWarnings({"InstanceVariableMayNotBeInitialized"})
    protected volatile T value;
    protected volatile Throwable error;

    /**
     * Holds the current state of the variable
     */
    protected volatile int state;  //modified through stateUpdater

    /**
     * Points to the head of the chain of requests waiting for a value to be bound
     */
    @SuppressWarnings({"UnusedDeclaration"})
    //modified through stateUpdater
    private volatile WaitingThread waiting;

    /**
     * Possible states
     */
    protected static final int S_NOT_INITIALIZED = 0;
    protected static final int S_INITIALIZING = 1;
    protected static final int S_INITIALIZED = 2;

    /**
     * A logical representation of a synchronous or asynchronous request to read the value once it is bound.
     */
    private static final class WaitingThread extends AtomicBoolean {
        private static final long serialVersionUID = 8909974768784947460L;
        private final Thread thread;
        private volatile WaitingThread previous;
        private final MessageStream callback;
        private final Object attachment;

        /**
         * Creates a representation of the request to read the value once it is bound
         *
         * @param thread     The physical thread of the request, which will be suspended
         * @param previous   The previous request in the chain of requests
         * @param attachment An arbitrary object closely identifying the request for the caller
         * @param callback   An actor or operator to send a message to once a value is bound
         */
        private WaitingThread(final Thread thread, final WaitingThread previous, final Object attachment, final MessageStream callback) {
            this.callback = callback;
            this.attachment = attachment;
            this.thread = thread;
            this.previous = previous;
        }
    }

    /**
     * A request chain terminator
     */
    private static final WaitingThread dummyWaitingThread = new WaitingThread(null, null, null, null);

    /**
     * Creates a new unbound Dataflow Expression
     */
    protected DataflowExpression() {
        state = S_NOT_INITIALIZED;
    }

    /**
     * Check if value has been set already for this expression
     *
     * @return true if bound already
     */
    @Override
    public final boolean isBound() {
        return state == S_INITIALIZED;
    }

    @Override
    public final int length() {
        return isBound() ? 1 : 0;
    }

    /**
     * Asynchronously retrieves the value of the variable. Sends the actual value of the variable as a message
     * back the the supplied actor once the value has been bound.
     * The actor can perform other activities or release a thread back to the pool by calling react() waiting for the message
     * with the value of the Dataflow Variable.
     *
     * @param callback An actor to send the bound value to.
     */
    @Override
    public void getValAsync(final MessageStream callback) {
        getValAsync(null, callback);
    }

    /**
     * Used by Dataflow operators.
     * Asynchronously retrieves the value of the variable. Sends a message back the the supplied MessageStream
     * with a map holding the supplied attachment under the 'attachment' key and the actual value of the variable under
     * the 'result' key once the value has been bound.
     * Attachment is an arbitrary value helping the actor.operator match its request with the reply.
     * The actor/operator can perform other activities or release a thread back to the pool by calling react() waiting for the message
     * with the value of the Dataflow Variable.
     *
     * @param attachment arbitrary non-null attachment if reader needs better identification of result
     * @param callback   An actor to send the bound value plus the supplied index to.
     */
    @Override
    public void getValAsync(final Object attachment, final MessageStream callback) {
        if (callback == null) {
            throw new NullPointerException();
        }
        WaitingThread newWaiting = null;
        while (state != S_INITIALIZED) {
            if (newWaiting == null) {
                newWaiting = new WaitingThread(null, null, attachment, callback);
            }
            final WaitingThread previous = waiting;
            // it means that writer already started processing queue, so value is already in place
            if (previous == dummyWaitingThread) {
                break;
            }
            newWaiting.previous = previous;
            if (waitingUpdater.compareAndSet(this, previous, newWaiting)) {
                // ok, we are in the queue, so writer is responsible to process us
                return;
            }
        }
        scheduleCallback(attachment, callback);
    }

    /**
     * Blocks, if the value has not been assigned yet to the DataflowVariable
     *
     * @throws InterruptedException If the current thread gets interrupted while waiting for the variable to be bound
     */
    public final void join() throws InterruptedException {
        getVal();
    }

    /**
     * Blocks, if the value has not been assigned yet to the DataflowVariable
     *
     * @param timeout The timeout value
     * @param units   Units for the timeout
     * @throws InterruptedException If the current thread gets interrupted while waiting for the variable to be bound
     */
    public final void join(final long timeout, final TimeUnit units) throws InterruptedException {
        getVal(timeout, units);
    }

    /**
     * Reads the value of the variable. Blocks, if the value has not been assigned yet.
     *
     * @return The actual value
     * @throws InterruptedException If the current thread gets interrupted while waiting for the variable to be bound
     */
    @Override
    public T getVal() throws InterruptedException {
        WaitingThread newWaiting = null;
        while (state != S_INITIALIZED) {
            if (newWaiting == null) {
                newWaiting = new WaitingThread(Thread.currentThread(), null, null, null);
            }
            final WaitingThread previous = waiting;
            // it means that writer already started processing queue, so value is already in place
            if (previous == dummyWaitingThread) {
                break;
            }
            newWaiting.previous = previous;
            if (waitingUpdater.compareAndSet(this, previous, newWaiting)) {
                // ok, we are in the queue, so writer is responsible to process us
                while (state != S_INITIALIZED) {
                    LockSupport.park();
                    if (Thread.currentThread().isInterrupted()) handleInterruption(newWaiting);
                }
                break;
            }
        }
        return value;
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
        final long endNano = System.nanoTime() + units.toNanos(timeout);
        WaitingThread newWaiting = null;
        while (state != S_INITIALIZED) {
            if (newWaiting == null) {
                newWaiting = new WaitingThread(Thread.currentThread(), null, null, null);
            }
            final WaitingThread previous = waiting;
            // it means that writer already started processing queue, so value is already in place
            if (previous == dummyWaitingThread) {
                break;
            }
            newWaiting.previous = previous;
            if (waitingUpdater.compareAndSet(this, previous, newWaiting)) {
                // ok, we are in the queue, so writer is responsible to process us
                while (state != S_INITIALIZED) {
                    final long toWait = endNano - System.nanoTime();
                    if (toWait <= 0) {
                        newWaiting.set(true); // don't unpark please
                        return null;
                    }
                    LockSupport.parkNanos(toWait);
                    if (Thread.currentThread().isInterrupted()) handleInterruption(newWaiting);
                }
                break;
            }
        }
        return value;
    }

    /**
     * Retrieves the bound value. Returns null, if no value is available.
     *
     * @return The value bound to the DFV or null
     */
    @Override
    public final DataflowExpression<T> poll() {
        if (isBound()) return this;
        else return null;
    }

    private static void handleInterruption(final AtomicBoolean newWaiting) throws InterruptedException {
        newWaiting.set(true); // don't unpark please
        throw new InterruptedException();
    }

    /**
     * Assigns a value to the variable. Returns silently if invoked on an already bound variable.
     *
     * @param value The value to assign
     */
    public final void bindSafely(final T value) {
        if (!stateUpdater.compareAndSet(this, S_NOT_INITIALIZED, S_INITIALIZING)) {
            fireBindError(value, false);
            return;
        }
        doBind(value);
    }

    public final void bindError(final Throwable e) {
        if (!stateUpdater.compareAndSet(this, S_NOT_INITIALIZED, S_INITIALIZING)) {
            fireBindError(e);
            throw new IllegalStateException(A_DATAFLOW_VARIABLE_CAN_ONLY_BE_ASSIGNED_ONCE_ONLY_RE_ASSIGNMENTS_TO_AN_EQUAL_VALUE_ARE_ALLOWED);
        }
        error = e;
        doBind(null);
    }

    /**
     * Assigns a value to the variable. Can only be invoked once on each instance of DataflowVariable.
     * Allows attempts to bind to equal values.
     *
     * @param value The value to assign
     */
    public final void bind(final T value) {
        if (!stateUpdater.compareAndSet(this, S_NOT_INITIALIZED, S_INITIALIZING)) {
            try {
                final Object boundValue = getVal();
                if (value == null && boundValue == null) return;
                if (value != null) {
                    if (value.equals(boundValue)) return;
                }
            } catch (InterruptedException ignore) {
            }  //Can ignore since will throw an IllegalStateException below
            fireBindError(value, false);
            throw new IllegalStateException(A_DATAFLOW_VARIABLE_CAN_ONLY_BE_ASSIGNED_ONCE_ONLY_RE_ASSIGNMENTS_TO_AN_EQUAL_VALUE_ARE_ALLOWED);
        }
        doBind(value);
    }

    /**
     * Assigns a value to the variable. Can only be invoked once on each instance of DataflowVariable
     * Doesn't allow attempts to bind to equal values.
     *
     * @param value The value to assign
     */
    public final void bindUnique(final T value) {
        if (!stateUpdater.compareAndSet(this, S_NOT_INITIALIZED, S_INITIALIZING)) {
            fireBindError(value, true);
            throw new IllegalStateException("A DataflowVariable can only be assigned once. Use bind() to allow for equal values to be passed into already-bound variables.");
        }
        doBind(value);
    }

    /**
     * Performs the actual bind operation, unblocks all blocked threads and informs all asynchronously waiting actors.
     *
     * @param value The value to assign
     */
    private void doBind(final T value) {
        doBindImpl(value);
        notifyRemote(null);
    }

    protected void doBindImpl(final T value) {
        this.value = value;
        if (value instanceof Throwable) error = (Throwable) value;
        state = S_INITIALIZED;
        fireOnMessage(value);
        final WaitingThread waitingQueue = waitingUpdater.getAndSet(this, dummyWaitingThread);
        // no more new waiting threads since that point
        for (WaitingThread currentWaiting = waitingQueue; currentWaiting != null; currentWaiting = currentWaiting.previous) {
            // maybe currentWaiting thread canceled or was interrupted
            if (currentWaiting.compareAndSet(false, true)) {
                if (currentWaiting.thread != null) {
                    // can be potentially called on a non-parked thread,
                    // which is OK as in this case next park () will be ignored
                    LockSupport.unpark(currentWaiting.thread);
                } else {
                    if (currentWaiting.callback != null) {
                        scheduleCallback(currentWaiting.attachment, currentWaiting.callback);
                    }
                }
            }
        }
    }

    /**
     * Binds the value after receiving a bing message over the wire
     *
     * @param hostId  Id of the bind originator host
     * @param message The value to bind
     */
    public final void doBindRemote(final UUID hostId, final T message) {
        doBindImpl(message);
        notifyRemote(hostId);
    }

    /**
     * Sends notifications to all subscribers
     *
     * @param hostId The local host id
     */
    private void notifyRemote(final UUID hostId) {
        if (serialHandle != null) {
            Actors.defaultActorPGroup.getThreadPool().execute(new Runnable() {
                @Override
                @SuppressWarnings("unchecked")
                public void run() {
                    final Object sub = serialHandle.getSubscribers();
                    if (sub instanceof RemoteHost) {
                        final RemoteHost host = (RemoteHost) sub;
                        if (hostId == null || !host.getHostId().equals(hostId)) {
                            host.write(new BindDataflow<T>(DataflowExpression.this, value, host.getLocalHost().getId()));
                        }
                    }
                    if (sub instanceof List) {
                        //noinspection SynchronizeOnNonFinalField
                        synchronized (serialHandle) {
                            for (final SerialContext host : (List<SerialContext>) sub) {
                                if (hostId == null || !host.getHostId().equals(hostId)) {
                                    host.write(new BindDataflow<T>(DataflowExpression.this, value, host.getLocalHostId()));
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * Sends the result back to the actor, which is waiting asynchronously for the value to be bound.
     * The message will either be a map holding the attachment under the 'attachment' key and the actual bound value under the 'result' key,
     * or it will be the result itself if the callback doesn't care about the index.
     *
     * @param attachment An arbitrary object identifying the request
     * @param callback   The actor to send the message to
     */
    @SuppressWarnings({"TypeMayBeWeakened"})
    protected void scheduleCallback(final Object attachment, final MessageStream callback) {
        if (attachment == null) {
            callback.send(error != null ? error : value);
        } else {
            final Map<String, Object> message = new HashMap<String, Object>(2);
            message.put(ATTACHMENT, attachment);
            message.put(RESULT, error != null ? error : value);
            callback.send(message);
        }
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
     * Schedule closure to be executed by pooled actor after data becomes available.
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
    public <V> void whenBound(final Pool pool, final Closure<V> closure) {
        getValAsync(new DataCallbackWithPool(pool, closure));
    }

    /**
     * Schedule closure to be executed by pooled actor after data becomes available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled.
     *
     * @param group   The PGroup to use for task scheduling for asynchronous message delivery
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     */
    @Override
    public final <V> void whenBound(final PGroup group, final Closure<V> closure) {
        getValAsync(new DataCallback(closure, group));
    }

    /**
     * Send the bound data to provided stream when it becomes available
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
    public final <V> Promise<V> then(final Pool pool, final Closure<V> closure) {
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
    public final <V> Promise<V> then(final PGroup group, final Closure<V> closure) {
        final DataflowVariable<V> result = new DataflowVariable<V>();
        whenBound(group, new ThenMessagingRunnable<T, V>(result, closure));
        return result;
    }

    /**
     * Schedule a set of closures to be executed after data became available on the current promise.
     * It is important to notice that even if the expression is already bound the execution of closures
     * will not happen immediately, but will be scheduled.
     * The returned Promise will hold a list of results of the individual closures, ordered in the same order.
     * In case of an exception being thrown from any of the closures, the first exception gets propagated into the promise returned from the method.
     *
     * @param closures closure to execute when data becomes available. The closure should take at most one argument.
     * @return A promise for the results of the supplied closures. This allows for chaining of then() method calls.
     */
    @SuppressWarnings({"rawtypes", "ClassReferencesSubclass"})
    public final Promise<List> thenForkAndJoin(final Closure<? extends Object>... closures) {
        return doThenForkAndJoin(Dataflow.DATA_FLOW_GROUP, Dataflow.DATA_FLOW_GROUP.getThreadPool(), closures);
    }

    /**
     * Schedule a set of closures to be executed after data became available on the current promise.
     * It is important to notice that even if the expression is already bound the execution of closures
     * will not happen immediately, but will be scheduled.
     * The returned Promise will hold a list of results of the individual closures, ordered in the same order.
     * In case of an exception being thrown from any of the closures, the first exception gets propagated into the promise returned from the method.
     *
     * @param pool     The thread pool to use for task scheduling for asynchronous message delivery
     * @param closures closure to execute when data becomes available. The closure should take at most one argument.
     * @return A promise for the results of the supplied closures. This allows for chaining of then() method calls.
     */
    @SuppressWarnings({"rawtypes", "ClassReferencesSubclass"})
    public final Promise<List> thenForkAndJoin(final Pool pool, final Closure<? extends Object>... closures) {
        return doThenForkAndJoin(Dataflow.DATA_FLOW_GROUP, pool, closures);
    }

    /**
     * Schedule a set of closures to be executed after data became available on the current promise.
     * It is important to notice that even if the expression is already bound the execution of closures
     * will not happen immediately, but will be scheduled.
     * The returned Promise will hold a list of results of the individual closures, ordered in the same order.
     * In case of an exception being thrown from any of the closures, the first exception gets propagated into the promise returned from the method.
     *
     * @param group    The PGroup to use for task scheduling for asynchronous message delivery
     * @param closures closure to execute when data becomes available. The closure should take at most one argument.
     * @return A promise for the results of the supplied closures. This allows for chaining of then() method calls.
     */
    @SuppressWarnings({"rawtypes", "ClassReferencesSubclass"})
    public final Promise<List> thenForkAndJoin(final PGroup group, final Closure<? extends Object>... closures) {
        return doThenForkAndJoin(group, group.getThreadPool(), closures);
    }

    private Promise<List> doThenForkAndJoin(final PGroup group, final Pool pool, final Closure<? extends Object>[] closures) {
        final DataflowVariable<List> result = new DataflowVariable<List>();
        final List<Promise> partialResults = new ArrayList<Promise>(closures.length);
        for (final Closure<? extends Object> closure : closures) {
            final DataflowVariable<? extends Object> partialResult = new DataflowVariable<Object>();
            whenBound(pool, new ThenMessagingRunnable(partialResult, closure));
            partialResults.add(partialResult);
        }
        group.whenAllBound(partialResults,
                new DataflowMessagingRunnable(partialResults.size()) {
                    @Override
                    protected void doRun(final Object... arguments) {
                        result.bind(Arrays.asList(arguments));
                    }
                },
                new DataflowMessagingRunnable(1) {
                    @Override
                    protected void doRun(final Object... arguments) {
                        result.bindError((Throwable) arguments[0]);
                    }
                }
        );
        return result;
    }

    /**
     * Send all pieces of data bound in the future to the provided stream when it becomes available.     *
     *
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     */
    @Override
    public final <V> void wheneverBound(final Closure<V> closure) {
        whenBound(closure);
    }

    /**
     * Send all pieces of data bound in the future to the provided stream when it becomes available.
     *
     * @param stream stream where to send result
     */
    @Override
    public final void wheneverBound(final MessageStream stream) {
        whenBound(stream);
    }

    @Override
    public final <V> DataflowReadChannel<V> chainWith(final Closure<V> closure) {
        return chainWith(Dataflow.retrieveCurrentDFPGroup(), closure);
    }

    @Override
    public final <V> DataflowReadChannel<V> chainWith(final Pool pool, final Closure<V> closure) {
        return chainWith(new DefaultPGroup(pool), closure);
    }

    @SuppressWarnings({"ClassReferencesSubclass"})
    @Override
    public <V> DataflowReadChannel<V> chainWith(final PGroup group, final Closure<V> closure) {
        final DataflowVariable<V> result = new DataflowVariable<V>();
        whenBound(group, new ThenMessagingRunnable<T, V>(result, closure));
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

    @SuppressWarnings({"ClassReferencesSubclass"})
    @Override
    public <V> DataflowReadChannel<V> chainWith(final PGroup group, final Map<String, Object> params, final Closure<V> closure) {
        final DataflowVariable<V> result = new DataflowVariable<V>();
        whenBound(group, new ThenMessagingRunnable<T, V>(result, closure));
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
        this.whenBound(new MessagingRunnable<T>() {
            @Override
            protected void doRun(final T argument) {
                target.leftShift(argument);
            }
        });
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
        this.whenBound(new MessagingRunnable<T>() {
            @Override
            protected void doRun(final T argument) {
                target.leftShift(argument);
            }
        });
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
        this.whenBound(new MessagingRunnable<T>() {
            @Override
            protected void doRun(final T argument) {
                for (final DataflowWriteChannel<T> target : targets) {
                    target.leftShift(argument);
                }
            }
        });
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
        this.whenBound(new MessagingRunnable<T>() {
            @Override
            protected void doRun(final T argument) {
                for (final DataflowWriteChannel<T> target : targets) {
                    target.leftShift(argument);
                }
            }
        });
    }

    @Override
    public DataflowReadChannel<T> tap(final DataflowWriteChannel<T> target) {
        return tap(Dataflow.retrieveCurrentDFPGroup(), target);
    }

    @Override
    public DataflowReadChannel<T> tap(final Pool pool, final DataflowWriteChannel<T> target) {
        return tap(new DefaultPGroup(pool), target);
    }

    @SuppressWarnings({"ClassReferencesSubclass"})
    @Override
    public DataflowReadChannel<T> tap(final PGroup group, final DataflowWriteChannel<T> target) {
        final DataflowVariable<T> result = new DataflowVariable<T>();
        this.whenBound(new MessagingRunnable<T>() {
            @Override
            protected void doRun(final T argument) {
                result.leftShift(argument);
                target.leftShift(argument);
            }
        });
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

    @SuppressWarnings({"ClassReferencesSubclass"})
    @Override
    public DataflowReadChannel<T> tap(final PGroup group, final Map<String, Object> params, final DataflowWriteChannel<T> target) {
        return tap(group, target);
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

    @SuppressWarnings({"ClassReferencesSubclass"})
    @Override
    public <V> DataflowReadChannel<V> merge(final PGroup group, final List<DataflowReadChannel<Object>> others, final Closure<V> closure) {
        final DataflowVariable<V> result = new DataflowVariable<V>();
        final List<DataflowReadChannel> inputs = new ArrayList<DataflowReadChannel>();
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

    @SuppressWarnings({"ClassReferencesSubclass"})
    @Override
    public <V> DataflowReadChannel<V> merge(final PGroup group, final Map<String, Object> params, final List<DataflowReadChannel<Object>> others, final Closure<V> closure) {
        final DataflowVariable<V> result = new DataflowVariable<V>();
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
        return createEventManager();
    }

    public synchronized BindErrorListenerManager<T> getBindErrorManager() {
        return createEventManager();
    }

    private DataflowChannelEventOrchestrator<T> createEventManager() {
        if (eventManager != null) return eventManager;
        eventManager = new DataflowChannelEventOrchestrator<T>();
        return eventManager;
    }

    private void fireOnMessage(final T value) {
        if (eventManager != null) {
            eventManager.fireOnMessage(value);
        }
    }

    private void fireBindError(final T value, final boolean unique) {
        if (eventManager != null) {
            try {
                final T oldValue = this.getVal();
                final Throwable error = this.error;
                if (error == null) eventManager.fireBindError(oldValue, value, unique);
                else eventManager.fireBindError(error, value, unique);
            } catch (InterruptedException ex) {
                throw new IllegalStateException(CANNOT_FIRE_BIND_ERRORS_THE_THREAD_HAS_BEEN_INTERRUPTED, ex);
            }
        }
    }

    private void fireBindError(final Throwable e) {
        if (eventManager != null) {
            try {
                final T oldValue = this.getVal();
                final Throwable error = this.error;
                if (error == null) eventManager.fireBindError(oldValue, e);
                else eventManager.fireBindError(error, e);
            } catch (InterruptedException ex) {
                throw new IllegalStateException(CANNOT_FIRE_BIND_ERRORS_THE_THREAD_HAS_BEEN_INTERRUPTED, ex);
            }
        }
    }


    /**
     * Transforms values bound eventually to dataflow variables using the supplied closure.
     *
     * @param another A list of DataflowVariables to transform
     * @param closure The transformation function, which must take the same number of arguments as there are elements in the "another" list
     * @param <V>     Type of the bound values
     * @return The value returned from the transformation closure.
     */
    public static <V> DataflowExpression<V> transform(final Object another, final Closure closure) {
        final int pnum = closure.getMaximumNumberOfParameters();
        if (pnum == 0) {
            throw new IllegalArgumentException("Closure should have parameters");
        }
        if (pnum == 1) {
            return new TransformOne<V>(another, closure);
        } else {
            if (another instanceof Collection) {
                final Collection<?> collection = (Collection<?>) another;
                if (collection.size() != pnum) {
                    throw new IllegalArgumentException("Closure parameters don't match the # of arguments");
                }
                return new TransformMany<V>(collection, closure);
            }
            throw new IllegalArgumentException("Collection expected");
        }
    }

    /**
     * A utility method to call at the very end of constructors of derived expressions.
     * Creates and subscribes a listener to monitor the expression
     */
    protected final void subscribe() {
        final DataflowExpressionsCollector listener = new DataflowExpressionsCollector();
        subscribe(listener);
        listener.start();
    }

    /**
     * Evaluates the expression after the ones we depend on are ready
     *
     * @return The value to bind
     */
    protected T evaluate() {
        return value;
    }

    protected void subscribe(final DataflowExpressionsCollector listener) {
        listener.subscribe(this);
    }

    /**
     * Invokes the method on itself or creates an expression, which will invoke the method n the bound value,
     * once it is available.
     */
    @Override
    public final Object invokeMethod(final String name, final Object args) {
        if (getMetaClass().respondsTo(this, name).isEmpty()) {
            return new DataflowInvocationExpression(this, name, (Object[]) args);
        }
        return InvokerHelper.invokeMethod(this, name, args);
    }

    /**
     * Returns either standard property of the expression or
     * creates an expression, which will request given property when the receiver becomes available
     *
     * @param propertyName The name of the property to retrieve
     * @return The property value, instance of DataflowGetPropertyExpression
     */
    @Override
    public final Object getProperty(final String propertyName) {
        final MetaProperty metaProperty = getMetaClass().hasProperty(this, propertyName);
        if (metaProperty != null) {
            return metaProperty.getProperty(this);
        }
        return new DataflowGetPropertyExpression<T>(this, propertyName);
    }

    @Override
    public final void setMetaClass(final MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    @Override
    public final void setProperty(final String propertyName, final Object newValue) {
        metaClass.setProperty(this, propertyName, newValue);
    }

    @Override
    public final MetaClass getMetaClass() {
        return metaClass;
    }

    /**
     * Listener for availability of data flow expressions we depend on.
     * Keeps a counter of monitored dataflow expressions. The counter gets decreased with each expression becoming available.
     * Once the counter reaches 0, the Collector evaluates itself and becomes bound to the resulting value.
     */
    final class DataflowExpressionsCollector extends MessageStream {
        private static final long serialVersionUID = 3414942165521113575L;
        private final AtomicInteger count = new AtomicInteger(1);

        @Override
        public MessageStream send(final Object message) {
            if (count.decrementAndGet() == 0) {
                bind(evaluate());
            }
            return this;
        }

        Object subscribe(final Object element) {
            if (!(element instanceof DataflowExpression)) {
                return element;
            }
            final DataflowExpression<?> dataflowExpression = (DataflowExpression<?>) element;
            if (dataflowExpression.state == S_INITIALIZED) {
                return dataflowExpression.value;
            }
            count.incrementAndGet();
            dataflowExpression.getValAsync(this);
            return element;
        }

        void start() {
            if (count.decrementAndGet() == 0) {
                doBind(evaluate());
            }
        }
    }

    @SuppressWarnings({"ArithmeticOnVolatileField"})
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(value=" + value + ')';
    }

    private static class TransformOne<V> extends DataflowExpression<V> {
        private static final long serialVersionUID = 6701886501249351047L;
        Object arg;
        private final Closure closure;

        private TransformOne(final Object another, final Closure closure) {
            this.closure = closure;
            arg = another;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected V evaluate() {
            return (V) closure.call(arg instanceof DataflowExpression<?> ? ((DataflowExpression<?>) arg).value : arg);
        }

        @Override
        protected void subscribe(final DataflowExpressionsCollector listener) {
            arg = listener.subscribe(arg);
        }
    }

    private static class TransformMany<V> extends DataflowComplexExpression<V> {
        private static final long serialVersionUID = 4115456542358280855L;
        private final Closure closure;

        private TransformMany(final Collection<?> collection, final Closure closure) {
            super(collection.toArray());
            this.closure = closure;
            subscribe();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected V evaluate() {
            super.evaluate();
            return (V) closure.call(args);
        }
    }

    /**
     * Represents a remote message binding a value to a remoted DataflowExpression
     */
    public static class BindDataflow<T> extends SerialMsg {
        private static final long serialVersionUID = -8674023870562062769L;
        @SuppressWarnings("rawtypes")
        private final DataflowExpression<T> expr;
        private final T message;

        /**
         * @param expr    The local DataflowExpression instance
         * @param message The actual value to bind
         * @param hostId  The identification of the host to send the bind information to
         */
        public BindDataflow(@SuppressWarnings("rawtypes") final DataflowExpression<T> expr, final T message, final UUID hostId) {
            super(hostId);
            this.expr = expr;
            this.message = message;
        }

        /**
         * Performs the actual bind on the remote host
         *
         * @param conn The connection object
         */
        @Override
        @SuppressWarnings("unchecked")
        public void execute(final RemoteConnection conn) {
            expr.doBindRemote(hostId, message);
        }
    }

}
