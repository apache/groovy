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

package groovyx.gpars.dataflow;

import groovy.lang.Closure;
import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.group.PGroup;
import groovyx.gpars.scheduler.Pool;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A unifying future-like interface to dataflow variables, asynchronous functions and active objects.
 * Represents the read end of DataflowVariables.
 *
 * @author Vaclav Pech
 */
public interface Promise<T> extends SelectableChannel<T> {

    /**
     * Retrieves the value of the variable, blocking until a value is available
     *
     * @return The value stored in the variable
     * @throws Throwable If the stored value is an exception instance it gets re-thrown
     */
    @SuppressWarnings({"ProhibitedExceptionDeclared"})
    T get() throws Throwable;

    /**
     * Retrieves the value of the variable, blocking up to given timeout, if the value has not been assigned yet.
     *
     * @param timeout The timeout value
     * @param units   Units for the timeout
     * @return The value stored in the variable
     * @throws Throwable If the stored value is an exception instance it gets re-thrown
     */
    @SuppressWarnings({"ProhibitedExceptionDeclared"})
    T get(final long timeout, final TimeUnit units) throws Throwable;

//    /**
//     * Asynchronously retrieves the value from the channel. Sends the actual value of the channel as a message
//     * back the the supplied actor once the value has been bound.
//     * The actor can perform other activities or release a thread back to the pool by calling react() waiting for the message
//     * with the value of the Dataflow channel.
//     *
//     * @param callback An actor to send the bound value to.
//     */
//    void getValAsync(final MessageStream callback);
//
//    /**
//     * Asynchronously retrieves the value from the channel. Sends a message back the the supplied MessageStream
//     * with a map holding the supplied attachment under the 'attachment' key and the actual value of the channel under
//     * the 'result' key once the value has been bound.
//     * Attachment is an arbitrary value helping the actor.operator match its request with the reply.
//     * The actor/operator can perform other activities or release a thread back to the pool by calling react() waiting for the message
//     * with the value of the Dataflow channel.
//     *
//     * @param attachment arbitrary non-null attachment if reader needs better identification of result
//     * @param callback   An actor to send the bound value plus the supplied index to.
//     */
//    void getValAsync(final Object attachment, final MessageStream callback);
//
//    /**
//     * Reads the current value of the channel. Blocks, if the value has not been assigned yet.
//     *
//     * @return The actual value
//     * @throws InterruptedException If the current thread gets interrupted while waiting for the channel to be bound
//     */
//    T getVal() throws InterruptedException;
//
//    /**
//     * Reads the current value of the channel. Blocks up to given timeout, if the value has not been assigned yet.
//     *
//     * @param timeout The timeout value
//     * @param units   Units for the timeout
//     * @return The actual value
//     * @throws InterruptedException If the current thread gets interrupted while waiting for the channel to be bound
//     */
//    T getVal(final long timeout, final TimeUnit units) throws InterruptedException;
//

    /**
     * Blocks, if the value has not been assigned yet to the DataflowVariable
     *
     * @throws InterruptedException If the current thread gets interrupted while waiting for the variable to be bound
     */
    void join() throws InterruptedException;

    /**
     * Blocks, if the value has not been assigned yet to the DataflowVariable
     *
     * @param timeout The timeout value
     * @param units   Units for the timeout
     * @throws InterruptedException If the current thread gets interrupted while waiting for the variable to be bound
     */
    void join(final long timeout, final TimeUnit units) throws InterruptedException;

    /**
     * Schedule closure to be executed after data became available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled
     *
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     * @return A promise for the results of the supplied closure. This allows for chaining of then() method calls.
     */
    <V> Promise<V> rightShift(final Closure<V> closure);

    /**
     * Schedule closure to be executed after data becomes available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled.
     *
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     */
    <V> void whenBound(final Closure<V> closure);

    /**
     * Schedule closure to be executed after data becomes available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled.
     *
     * @param pool    The thread pool to use for task scheduling for asynchronous message delivery
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     */
    <V> void whenBound(final Pool pool, final Closure<V> closure);

    /**
     * Schedule closure to be executed after data becomes available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled.
     *
     * @param group   The PGroup to use for task scheduling for asynchronous message delivery
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     */
    <V> void whenBound(final PGroup group, final Closure<V> closure);

    /**
     * Send the bound data to provided stream when it becomes available
     *
     * @param stream stream where to send result
     */
    void whenBound(final MessageStream stream);

    /**
     * Schedule closure to be executed after data became available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled
     *
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     * @return A promise for the results of the supplied closure. This allows for chaining of then() method calls.
     */
    <V> Promise<V> then(final Closure<V> closure);

    /**
     * Schedule closure to be executed after data becomes available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled.
     *
     * @param pool    The thread pool to use for task scheduling for asynchronous message delivery
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     * @return A promise for the results of the supplied closure. This allows for chaining of then() method calls.
     */
    <V> Promise<V> then(final Pool pool, final Closure<V> closure);

    /**
     * Schedule closure to be executed after data becomes available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled.
     *
     * @param group   The PGroup to use for task scheduling for asynchronous message delivery
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     * @return A promise for the results of the supplied closure. This allows for chaining of then() method calls.
     */
    <V> Promise<V> then(final PGroup group, final Closure<V> closure);

    /**
     * Schedule closure to be executed after data became available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled
     *
     * @param closure      closure to execute when data becomes available. The closure should take at most one argument.
     * @param errorHandler closure to execute when an error (instance of Throwable) gets bound. The closure should take at most one argument.
     * @return A promise for the results of the supplied closure. This allows for chaining of then() method calls.
     */
    <V> Promise<V> then(final Closure<V> closure, final Closure<V> errorHandler);

    /**
     * Schedule closure to be executed after data becomes available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled.
     *
     * @param pool         The thread pool to use for task scheduling for asynchronous message delivery
     * @param closure      closure to execute when data becomes available. The closure should take at most one argument.
     * @param errorHandler closure to execute when an error (instance of Throwable) gets bound. The closure should take at most one argument.
     * @return A promise for the results of the supplied closure. This allows for chaining of then() method calls.
     */
    <V> Promise<V> then(final Pool pool, final Closure<V> closure, final Closure<V> errorHandler);

    /**
     * Schedule closure to be executed after data becomes available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled.
     *
     * @param group        The PGroup to use for task scheduling for asynchronous message delivery
     * @param closure      closure to execute when data becomes available. The closure should take at most one argument.
     * @param errorHandler closure to execute when an error (instance of Throwable) gets bound. The closure should take at most one argument.
     * @return A promise for the results of the supplied closure. This allows for chaining of then() method calls.
     */
    <V> Promise<V> then(final PGroup group, final Closure<V> closure, final Closure<V> errorHandler);

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
    Promise<List> thenForkAndJoin(final Closure<? extends Object>... closures);

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
    Promise<List> thenForkAndJoin(final Pool pool, final Closure<? extends Object>... closures);

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
    Promise<List> thenForkAndJoin(final PGroup group, final Closure<? extends Object>... closures);

    /**
     * Check if value has been set already for this expression
     *
     * @return true if bound already
     */
    boolean isBound();

    /**
     * Checks if the promise is bound to an error
     *
     * @return True, if an error has been bound
     */
    boolean isError();

    /**
     * Returns the error bound to the promise
     *
     * @return The error
     * @throws IllegalStateException If not bound or not bound to an error
     */
    Throwable getError();

    /**
     * May be used by lazy implementations to warm up
     */
    void touch();
}
