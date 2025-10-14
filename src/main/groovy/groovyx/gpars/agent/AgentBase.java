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

package groovyx.gpars.agent;

import groovy.lang.Closure;
import groovyx.gpars.dataflow.DataflowVariable;
import org.codehaus.groovy.runtime.NullObject;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Implements most of Agent's public method in Java
 *
 * @author Vaclav Pech
 */
abstract class AgentBase<T> extends AgentCore {
    /**
     * Allows reads not to wait in the message queue.
     * Writes and reads are mutually separated by using write or read locks respectively.
     */
    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Holds the internal mutable state
     */
    protected T data;

    /**
     * Function converting the internal state during read to prevent internal state escape from
     * the protected boundary of the agent
     */
    private final Closure copy;

    /**
     * Holds all listeners interested in state updates
     * A listener should be a closure accepting the old and the new value in this order.
     */
    private final Collection<Closure> listeners = new CopyOnWriteArrayList<Closure>();

    /**
     * Holds all validators checking the agent's state
     * A validator should be a closure accepting the old and the new value in this order.
     */
    private final Collection<Closure> validators = new CopyOnWriteArrayList<Closure>();

    AgentBase(final T data) {
        this(data, null);
    }

    /**
     * Creates a new Agent around the supplied modifiable object
     *
     * @param data The object to use for storing the internal state of the variable
     * @param copy A closure to use to create a copy of the internal state when sending the internal state out
     */
    AgentBase(final T data, final Closure copy) {
        this.data = data;
        this.copy = copy;
    }

    /**
     * Accepts a NullObject instance and sets the internal state to null
     *
     * @param obj The received message
     */
    final void onMessage(final NullObject obj) {
        lock.readLock().lock();
        try {
            updateValue(null);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Accepts and invokes the closure
     *
     * @param code The received message
     */
    final void onMessage(final Closure code) {
        lock.writeLock().lock();
        try {
            code.setDelegate(this);
            code.call(copy != null ? copy.call(data) : data);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Other messages than closures are accepted as new values for the internal state
     *
     * @param message The received message
     */
    final void onMessage(final T message) {
        lock.writeLock().lock();
        try {
            updateValue(message);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Allows closures to set the new internal state as a whole
     * Do not call this method directly from the outside. It is designed to be used from within the submitted closures.
     *
     * @param newValue The value to set the internal state to
     */
    @SuppressWarnings({"unchecked", "CatchGenericClass"})
    public final void updateValue(final T newValue) {
        final T oldValue = copy != null ? (T) copy.call(data) : data;
        boolean validated = false;
        try {
            for (final Closure validator : validators) {
                validator.call(new Object[]{oldValue, newValue});
            }
            validated = true;
        } catch (Throwable e) {
            registerError(e);
        }
        if (validated) {
            data = newValue;
            for (final Closure listener : listeners) {
                listener.call(new Object[]{oldValue, newValue});
            }
        }
    }

    /**
     * A shorthand method for safe message-based retrieval of the internal state.
     * Retrieves the internal state immediately by-passing the queue of tasks waiting to be processed.
     *
     * @return The current value of the actor's state
     */
    @SuppressWarnings({"unchecked"})
    public final T getInstantVal() {
        lock.readLock().lock();
        try {
            return copy != null ? (T) copy.call(data) : data;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * A shorthand method for safe message-based retrieval of the internal state.
     * The request to retrieve a value is put into the message queue, so will wait for all messages delivered earlier to complete.
     *
     * @return The value of the actor's state once all previously sent messages in the queue get processed
     * @throws InterruptedException If the thread gets interrupted while waiting for a value to be bound
     */
    public final T getVal() throws InterruptedException {
        return sendAndWait(awaitClosure);
    }

    /**
     * A shorthand method for safe asynchronous message-based retrieval of the internal state.
     * The request to retrieve a value is put into the message queue, so will wait for all messages delivered earlier to complete.
     *
     * @param callback A closure to invoke with the internal state as a parameter
     */
    public final void valAsync(final Closure callback) {
        send(new Closure(this) {
            private static final long serialVersionUID = 27598476470091452L;

            @Override
            public Object call(final Object arguments) {
                callback.call(arguments);
                return null;
            }
        });
    }


    /**
     * Submits the closure waiting for the result
     *
     * @param message The message/closure to send
     * @return The return value of the closure
     * @throws InterruptedException If the thread gets interrupted while waiting for a value to be bound
     */
    @SuppressWarnings({"unchecked"})
    public final T sendAndWait(final Closure message) throws InterruptedException {
        if (Thread.currentThread() == currentThread)
            throw new IllegalStateException("Cannot submit messages to agents inside submitted commands");

        final DataflowVariable<Object> result = new DataflowVariable<Object>();
        this.send(new Closure(message.getOwner()) {
            private static final long serialVersionUID = -4637623342002266534L;

            @Override
            public Object call(final Object arguments) {
                final Object value = message.call(arguments);
                result.bind(value);
                return null;
            }
        });
        return (T) result.getVal();
    }

    /**
     * Blocks until all messages in the queue prior to call to await() complete.
     * Provides a means to synchronize with the Agent
     *
     * @throws InterruptedException If the thread gets interrupted while waiting for a value to be bound
     */
    public final void await() throws InterruptedException {
        sendAndWait(awaitClosure);
    }


    /**
     * Adds a listener interested in state updates
     * A listener should be a closure accepting the old and the new value in this order plus optionally the agent reference as the first argument.
     *
     * @param listener The closure with two or three arguments
     */
    public void addListener(final Closure listener) {
        listeners.add(checkClosure(listener));
    }

    /**
     * Adds a validator checking the agent's state
     * A listener should be a closure accepting the old and the new value in this order plus optionally the agent reference as the first argument.
     *
     * @param validator The closure with two or three arguments
     */
    public void addValidator(final Closure validator) {
        validators.add(checkClosure(validator));
    }


    /**
     * Only two-argument closures are allowed
     *
     * @param code The passed-in closure
     * @return Either the original closure or a two-argument closure after currying the first argument of the passed-in closure to self
     */
    private Closure checkClosure(final Closure code) {
        final int maximumNumberOfParameters = code.getMaximumNumberOfParameters();
        if (maximumNumberOfParameters < 2 || maximumNumberOfParameters > 3)
            throw new IllegalArgumentException("Agent listeners and validators can only take two arguments plus optionally the current agent instance as the first argument.");
        if (maximumNumberOfParameters == 3) return code.curry(new Object[]{this});
        else return code;
    }

    private static final Closure awaitClosure = new AwaitClosure();

    /**
     * Returns the current value of the Agent's state
     */
    private static class AwaitClosure extends Closure {
        private static final long serialVersionUID = 8104821777516625579L;

        private AwaitClosure() {
            super(null);
        }

        @Override
        public Object call(final Object arguments) {
            return arguments;
        }
    }
}
