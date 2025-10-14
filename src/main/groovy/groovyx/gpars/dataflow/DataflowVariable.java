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

import groovy.lang.Closure;
import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.dataflow.expression.DataflowExpression;
import groovyx.gpars.dataflow.impl.ThenMessagingRunnable;
import groovyx.gpars.dataflow.remote.RemoteDataflowVariable;
import groovyx.gpars.group.PGroup;
import groovyx.gpars.scheduler.Pool;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents a thread-safe single-assignment, multi-read variable.
 * Each instance of DataflowVariable can be read repeatedly any time using the 'val' property and assigned once
 * in its lifetime using the '&lt;&lt;' operator. Reads preceding assignment will be blocked until the value
 * is assigned.
 * For actors and Dataflow Operators the asynchronous non-blocking variants of the getValAsync() methods can be used.
 * They register the request to read a value and will send a message to the actor or operator once the value is available.
 *
 * @param <T> Type of values to bind with the DataflowVariable
 * @author Vaclav Pech, Alex Tkachman
 *         Date: Jun 4, 2009
 */
@SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject", "UnqualifiedStaticUsage"})
public class DataflowVariable<T> extends DataflowExpression<T> implements DataflowChannel<T>, Promise<T> {
    private static final long serialVersionUID = 1340439210749936258L;

    /**
     * Creates a new unbound Dataflow Variable
     */
    public DataflowVariable() {
    }

    /**
     * Assigns a value to the variable. Can only be invoked once on each instance of DataflowVariable
     *
     * @param value The value to assign
     */
    @SuppressWarnings("unchecked")
    @Override
    public DataflowWriteChannel<T> leftShift(final T value) {
        if (value instanceof DataflowReadChannel) bindDFV((DataflowReadChannel<T>) value);
        else bind(value);
        return this;
    }

    /**
     * Assigns a value from one DataflowVariable instance to this variable.
     * Can only be invoked once on each instance of DataflowVariable
     *
     * @param ref The DataflowVariable instance the value of which to bind
     */
    @Override
    public DataflowWriteChannel<T> leftShift(final DataflowReadChannel<T> ref) {
        return bindDFV(ref);
    }

    private DataflowWriteChannel<T> bindDFV(final DataflowReadChannel<T> ref) {
        ref.getValAsync(new MessageStream() {
            private static final long serialVersionUID = -458384302762038543L;

            @SuppressWarnings({"unchecked"})
            @Override
            public MessageStream send(final Object message) {
                bind((T) message);
                return this;
            }
        });
        return this;
    }

    /**
     * Retrieves the value of the variable, blocking until a value is available
     *
     * @return The value stored in the variable
     * @throws Throwable If the stored value is an exception instance it gets re-thrown
     */
    @Override
    @SuppressWarnings({"ProhibitedExceptionDeclared"})
    public final T get() throws Throwable {
        final T result = getVal();
        if (error != null) throw error;
        return result;
    }

    /**
     * Retrieves the value of the variable, blocking up to given timeout, if the value has not been assigned yet.
     *
     * @param timeout The timeout value
     * @param units   Units for the timeout
     * @return The value stored in the variable
     * @throws Throwable If the stored value is an exception instance it gets re-thrown
     */
    @Override
    @SuppressWarnings({"ProhibitedExceptionDeclared"})
    public final T get(final long timeout, final TimeUnit units) throws Throwable {
        final T result = getVal(timeout, units);
        if (result instanceof Throwable) {
            throw (Throwable) result;
        }
        if (result == null) {
            if (shouldThrowTimeout()) throw new TimeoutException("Timeout expired in DataflowVariable.get().");
            return get();
        }
        return result;
    }

    boolean shouldThrowTimeout() {
        return !this.isBound();
    }

    /**
     * Checks if the promise is bound to an error
     *
     * @return True, if an error has been bound
     */
    @Override
    public boolean isError() {
        return isBound() && error != null;
    }

    /**
     * Returns the error bound to the promise
     *
     * @return The error
     * @throws IllegalStateException If not bound or not bound to an error
     */
    @Override
    public Throwable getError() {
        if (isError()) return error;
        else throw new IllegalStateException("No error has been bound to the dataflow variable.");
    }

    @Override
    public void touch() {
        //Intentionally left empty
    }

    /**
     * Schedule closure to be executed after data became available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled
     *
     * @param closure      closure to execute when data becomes available. The closure should take at most one argument.
     * @param errorHandler closure to execute when an error (instance of Throwable) gets bound. The closure should take at most one argument.
     * @return A promise for the results of the supplied closure. This allows for chaining of then() method calls.
     */
    @Override
    public <V> Promise<V> then(final Closure<V> closure, final Closure<V> errorHandler) {
        final DataflowVariable<V> result = new DataflowVariable<V>();
        whenBound(new ThenMessagingRunnable<T, V>(result, closure, errorHandler));
        return result;
    }

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
    @Override
    public <V> Promise<V> then(final Pool pool, final Closure<V> closure, final Closure<V> errorHandler) {
        final DataflowVariable<V> result = new DataflowVariable<V>();
        whenBound(pool, new ThenMessagingRunnable<T, V>(result, closure, errorHandler));
        return result;
    }

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
    @Override
    public <V> Promise<V> then(final PGroup group, final Closure<V> closure, final Closure<V> errorHandler) {
        final DataflowVariable<V> result = new DataflowVariable<V>();
        whenBound(group, new ThenMessagingRunnable<T, V>(result, closure, errorHandler));
        return result;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Class<RemoteDataflowVariable> getRemoteClass() {
        return RemoteDataflowVariable.class;
    }
}
