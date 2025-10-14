// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2012, 2014  The original author or authors
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

import groovyx.gpars.actor.Actors;
import groovyx.gpars.group.PGroup;
import groovyx.gpars.scheduler.Pool;
import groovyx.gpars.serial.WithSerialId;
import groovyx.gpars.util.AsyncMessagingCore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Vaclav Pech
 *         Date: 13.4.2010
 */
@SuppressWarnings({"UnqualifiedStaticUsage"})
public abstract class AgentCore extends WithSerialId {

    private final AsyncMessagingCore core;

    protected volatile Thread currentThread;

    protected AgentCore() {
        this.core = new AsyncMessagingCore(Actors.defaultActorPGroup.getThreadPool()) {
            @Override
            protected void registerError(final Throwable e) {
                AgentCore.this.registerError(e);
            }

            @Override
            protected void handleMessage(final Object message) {
                AgentCore.this.handleMessage(message);
            }

            /**
             * Informs about the thread being removed from the task
             */
            @Override
            protected void threadAssigned() {
                currentThread = Thread.currentThread();
            }

            /**
             * Informs about a new thread being assigned to process the next message
             */
            @Override
            protected void threadUnassigned() {
                currentThread = null;
            }
        };
    }

    /**
     * Sets a new thread pool to be used by the agent
     *
     * @param threadPool The thread pool to use
     */
    public final void attachToThreadPool(final Pool threadPool) {
        core.attachToThreadPool(threadPool);
    }

    /**
     * Sets an actor group to use for task scheduling
     *
     * @param pGroup The pGroup to use
     */
    public void setPGroup(final PGroup pGroup) {
        attachToThreadPool(pGroup.getThreadPool());
    }

    /**
     * Retrieves the agent's fairness flag
     * Fair agents give up the thread after processing each message, non-fair agents keep a thread until their message queue is empty.
     * Non-fair agents tend to perform better than fair ones.
     *
     * @return True for fair agents, false for non-fair ones. Agents are non-fair by default.
     */
    public boolean isFair() {
        return core.isFair();
    }

    /**
     * Makes the agent fair. Agents are non-fair by default.
     * Fair agents give up the thread after processing each message, non-fair agents keep a thread until their message queue is empty.
     * Non-fair agents tend to perform better than fair ones.
     */
    public void makeFair() {
        core.makeFair();
    }

    /**
     * Holds agent errors
     */
    private List<Throwable> errors;

    /**
     * Adds the message to the agent\s message queue
     *
     * @param message A value or a closure
     */
    public final void send(final Object message) {
        core.store(message);
    }

    /**
     * Adds the message to the agent\s message queue
     *
     * @param message A value or a closure
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public final void leftShift(final Object message) {
        send(message);
    }

    /**
     * Adds the message to the agent\s message queue
     *
     * @param message A value or a closure
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public final void call(final Object message) {
        send(message);
    }

    /**
     * Dynamically dispatches the method call
     *
     * @param message A value or a closure
     */
    public abstract void handleMessage(final Object message);

    /**
     * Adds the exception to the list of thrown exceptions
     *
     * @param e The exception to store
     */
    @SuppressWarnings({"MethodOnlyUsedFromInnerClass", "SynchronizedMethod"})
    synchronized void registerError(final Throwable e) {
        if (errors == null) errors = new ArrayList<Throwable>();
        errors.add(e);
    }

    /**
     * Retrieves a list of exception thrown within the agent's body.
     * Clears the exception history
     *
     * @return A detached collection of exception that have occurred in the agent's body
     */
    @SuppressWarnings({"SynchronizedMethod", "ReturnOfCollectionOrArrayField"})
    public synchronized List<Throwable> getErrors() {
        if (errors == null) return Collections.emptyList();
        try {
            return errors;
        } finally {
            errors = null;
        }
    }

    /**
     * Indicates whether there have been exception thrown within the agent's body.
     *
     * @return True, if any exceptions have occurred in the agent's body
     */
    @SuppressWarnings({"SynchronizedMethod"})
    public synchronized boolean hasErrors() {
        if (errors == null) return false;
        return !errors.isEmpty();
    }
}
