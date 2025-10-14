// GPars - Groovy Parallel Systems
//
// Copyright © 2008-11  The original author or authors
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

package groovyx.gpars.actor;

import groovy.lang.Closure;
import groovy.time.Duration;
import groovyx.gpars.actor.impl.SequentialProcessingActor;

import java.util.concurrent.TimeUnit;

/**
 * @author Vaclav Pech, Alex Tkachman, Dierk Koenig
 */
@SuppressWarnings({"ThrowCaughtLocally", "UnqualifiedStaticUsage"})
public abstract class BlockingActor extends SequentialProcessingActor {

    private static final String THE_ACTOR_HAS_NOT_BEEN_STARTED = "The actor hasn't been started.";
    private static final String THE_ACTOR_HAS_BEEN_STOPPED = "The actor has been stopped.";
    private static final long serialVersionUID = -6232655362494852540L;
    public static final String AN_ACTOR_CAN_ONLY_RECEIVE_ONE_MESSAGE_AT_A_TIME = "An actor can only receive one message at a time";

    /**
     * Adds {@code reply} and {@code replyIfExists} methods to the current {@code Actor} and the message.
     * These methods will call {@code send} on the target actor (the sender of the original message).  The
     * {@code reply}/{@code replyIfExists} methods invoked on the actor will be sent to all currently
     * processed messages, {@code reply}/{@code replyIfExists} invoked on a message will send a reply to the
     * sender of that particular message only.
     *
     * @param message The original message
     */
    private void enhanceReplies(final ActorMessage message) {
        setSender(message == null ? null : message.getSender());
    }

    /**
     * Retrieves a message from the message queue, waiting, if necessary, for a message to arrive.
     *
     * @return The message retrieved from the queue.
     * @throws InterruptedException If the thread is interrupted during the wait. Should propagate up to stop the thread.
     */
    @Override
    protected final Object receiveImpl() throws InterruptedException {
        checkStoppedFlags();
        final ActorMessage message = takeMessage();
        return enhanceAndUnwrap(message);
    }

    /**
     * Retrieves a message from the message queue, waiting, if necessary, for a message to arrive.
     *
     * @param timeout how long to wait before giving up, in units of unit
     * @param units   a {@code TimeUnit} determining how to interpret the timeout parameter
     * @return The message retrieved from the queue, or null, if the timeout expires.
     * @throws InterruptedException If the thread is interrupted during the wait. Should propagate up to stop the thread.
     */
    @Override
    protected final Object receiveImpl(final long timeout, final TimeUnit units) throws InterruptedException {
        checkStoppedFlags();
        final ActorMessage message = takeMessage(timeout, units);
        return enhanceAndUnwrap(message);
    }

    private Object enhanceAndUnwrap(final ActorMessage message) {
        enhanceReplies(message);
        if (message == null) {
            return null;
        }
        return message.getPayLoad();
    }

    private void checkStoppedFlags() {
        if (stopFlag == S_NOT_STARTED) throw new IllegalStateException(THE_ACTOR_HAS_NOT_BEEN_STARTED);
        if (stopFlag == S_STOPPED) throw new IllegalStateException(THE_ACTOR_HAS_BEEN_STOPPED);
    }

    /**
     * Retrieves a message from the message queue, waiting, if necessary, for a message to arrive.
     * The message retrieved from the queue is passed into the handler as the only parameter.
     *
     * @param handler A closure accepting the retrieved message as a parameter, which will be invoked after a message is received.
     * @throws InterruptedException If the thread is interrupted during the wait. Should propagate up to stop the thread.
     */
    @SuppressWarnings({"MethodOverloadsMethodOfSuperclass"})
    protected final void receive(final Closure handler) throws InterruptedException {
        handler.setResolveStrategy(Closure.DELEGATE_FIRST);
        handler.setDelegate(this);

        final int maxNumberOfParameters = handler.getMaximumNumberOfParameters();
        if (maxNumberOfParameters > 1)
            throw new IllegalArgumentException(AN_ACTOR_CAN_ONLY_RECEIVE_ONE_MESSAGE_AT_A_TIME);

        checkStopTerminate();
        final ActorMessage message = takeMessage();
        enhanceReplies(message);

        try {
            if (maxNumberOfParameters == 0) {
                handler.call();
            } else {
                handler.call(message == null ? message : message.getPayLoad());
            }

        } finally {
            setSender(null);
        }
    }

    /**
     * Retrieves a message from the message queue, waiting, if necessary, for a message to arrive.
     * The message retrieved from the queue is passed into the handler as the only parameter.
     * A null value is passed into the handler, if the timeout expires
     *
     * @param timeout  how long to wait before giving up, in units of unit
     * @param timeUnit a TimeUnit determining how to interpret the timeout parameter
     * @param handler  A closure accepting the retrieved message as a parameter, which will be invoked after a message is received.
     * @throws InterruptedException If the thread is interrupted during the wait. Should propagate up to stop the thread.
     */
    protected final void receive(final long timeout, final TimeUnit timeUnit, final Closure handler) throws InterruptedException {
        handler.setResolveStrategy(Closure.DELEGATE_FIRST);
        handler.setDelegate(this);

        final int maxNumberOfParameters = handler.getMaximumNumberOfParameters();
        if (maxNumberOfParameters > 1)
            throw new IllegalArgumentException(AN_ACTOR_CAN_ONLY_RECEIVE_ONE_MESSAGE_AT_A_TIME);

        final long stopTime = timeUnit.toMillis(timeout) + System.currentTimeMillis();

        if (stopFlag != S_RUNNING) {
            throw new IllegalStateException(THE_ACTOR_HAS_NOT_BEEN_STARTED);
        }
        final ActorMessage message =
                takeMessage(Math.max(stopTime - System.currentTimeMillis(), 0L), TimeUnit.MILLISECONDS);

        try {
            enhanceReplies(message);

            if (maxNumberOfParameters == 0) {
                handler.call();
            } else {
                handler.call(message == null ? message : message.getPayLoad());
            }
        } finally {
            setSender(null);
        }
    }

    /**
     * Retrieves a message from the message queue, waiting, if necessary, for a message to arrive.
     * The message retrieved from the queue is passed into the handler as the only parameter.
     * A null value is passed into the handler, if the timeout expires
     *
     * @param duration how long to wait before giving up, in units of unit
     * @param handler  A closure accepting the retrieved message as a parameter, which will be invoked after a message is received.
     * @throws InterruptedException If the thread is interrupted during the wait. Should propagate up to stop the thread.
     */
    @SuppressWarnings({"MethodOverloadsMethodOfSuperclass", "TypeMayBeWeakened"})
    protected final void receive(final Duration duration, final Closure handler) throws InterruptedException {
        receive(duration.toMilliseconds(), TimeUnit.MILLISECONDS, handler);
    }

    @Override
    protected void handleStart() {
        super.handleStart();
        act();
    }
}
