// GPars - Groovy Parallel Systems
//
// Copyright © 2008-11  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package groovyx.gpars.actor;

import groovy.lang.Closure;
import groovyx.gpars.actor.impl.ActorReplyException;
import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.group.PGroup;
import groovyx.gpars.util.AsyncMessagingCore;

/**
 * Wraps all actors that repeatedly loop through incoming messages and hold no implicit state between subsequent messages.
 *
 * @author Vaclav Pech
 *         Date: Aug 23, 2010
 */
public abstract class AbstractLoopingActor extends Actor {

    private static final long serialVersionUID = -7638132628355085829L;
    private volatile boolean stoppedFlag = true;
    private volatile boolean terminatedFlag = true;
    volatile boolean terminatingFlag = true;
    private ActorTimerTask currentTimerTask = null;
    private int timeoutCounter = 0;
    private MessageStream currentSender;

    /**
     * Holds the particular instance of async messaging core to use
     */
    private AsyncMessagingCore core;

    final AsyncMessagingCore getCore() {
        return core;
    }

    /**
     * Builds the async messaging core using the supplied code handler
     *
     * @param code Code to run on each message
     */
    protected final void initialize(final Closure code) {

        //noinspection OverlyComplexAnonymousInnerClass
        this.core = new AsyncMessagingCore(parallelGroup.getThreadPool()) {
            @Override
            protected void registerError(final Throwable e) {
                if (e instanceof InterruptedException) {
                    handleInterrupt((InterruptedException) e);
                } else {
                    handleException(e);
                }
                terminate();
            }

            @Override
            protected void handleMessage(final Object message) {
                if (message == START_MESSAGE) handleStart();
                else {
                    if (message == TIMEOUT_MESSAGE) {
                        final ActorTimerTask localTimerTask = currentTimerTask;
                        if (localTimerTask != null) {
                            cancelCurrentTimeoutTask();
                            if (timeoutCounter != localTimerTask.getId()) return;  //ignore obsolete timeout messages
                        } else return;
                        handleTimeout();
                    } else {
                        if (currentTimerTask != null) cancelCurrentTimeoutTask();
                    }
                    timeoutCounter = (timeoutCounter + 1) % Integer.MAX_VALUE;

                    if (terminatingFlag || message == STOP_MESSAGE) {
                        if (!terminatedFlag) {
                            handleTermination();
                            terminatedFlag = true;
                            getJoinLatch().bindUnique(null);
                        }
                    } else {
                        final ActorMessage actorMessage = (ActorMessage) message;
                        try {
                            runEnhancedWithoutRepliesOnMessages(actorMessage, code, actorMessage.getPayLoad());
                        } finally {
                            currentSender = null;
                        }
                    }
                }
            }

            @Override
            protected boolean continueProcessingMessages() {
                return isActive();
            }

            @Override
            protected void threadAssigned() {
                registerCurrentActorWithThread(AbstractLoopingActor.this);
                currentThread = Thread.currentThread();
            }

            @Override
            protected void threadUnassigned() {
                deregisterCurrentActorWithThread();
                currentThread = null;
            }
        };
    }

    /**
     * Retrieves the actor's fairness flag
     * Fair actors give up the thread after processing each message, non-fair actors keep a thread until their message queue is empty.
     * Non-fair actors tend to perform better than fair ones.
     *
     * @return True for fair actors, false for non-fair ones. actors are non-fair by default.
     */
    public final boolean isFair() {
        return core.isFair();
    }

    /**
     * Makes the actor fair. Actors are non-fair by default.
     * Fair actors give up the thread after processing each message, non-fair actors keep a thread until their message queue is empty.
     * Non-fair actors tend to perform better than fair ones.
     */
    public final void makeFair() {
        core.makeFair();
    }

    protected final void setTimeout(final long timeout) {
        if (timeout < 0L) throw new IllegalArgumentException("Actor timeout must be a non-negative value");
        currentTimerTask = new ActorTimerTask(this, timeoutCounter);
        timer.schedule(currentTimerTask, timeout);
    }

    private void cancelCurrentTimeoutTask() {
        assert currentTimerTask != null;
        currentTimerTask.cancel();
        currentTimerTask = null;
    }

    /**
     * Starts the Actor without sending the START_MESSAGE message to speed the start-up.
     * The potential custom afterStart handlers won't be run.
     * No messages can be sent or received before an Actor is started.
     *
     * @return same actor
     */
    @Override
    public Actor silentStart() {
        return doStart();
    }

    /**
     * Starts the Actor and sends it the START_MESSAGE to run any afterStart handlers.
     * No messages can be sent or received before an Actor is started.
     *
     * @return same actor
     */
    @Override
    public Actor start() {
        doStart();
        send(START_MESSAGE);
        return this;
    }

    private Actor doStart() {
        if (!hasBeenStopped()) throw new IllegalStateException(ACTOR_HAS_ALREADY_BEEN_STARTED);
        stoppedFlag = false;
        terminatedFlag = false;
        terminatingFlag = false;
        return this;
    }

    @Override
    public final Actor stop() {
        if (!hasBeenStopped()) {
            stoppedFlag = true;
            send(STOP_MESSAGE);
        }
        return this;
    }

    @Override
    public final Actor terminate() {
        synchronized (this) {
            if (!isActive()) return this;
            stop();
            terminatingFlag = true;
        }

        if (isActorThread()) {
            terminatedFlag = true;
            handleTermination();
            getJoinLatch().bindUnique(null);
        }
        //noinspection CallToThreadYield
        Thread.yield();
        if (!isActorThread() && currentThread != null) {
            try {
                currentThread.interrupt();  //NPE may occur non-deterministically, since we're running concurrently with the actor's thread
            } catch (Exception e) {
                send(TERMINATE_MESSAGE);
            }
        } else send(TERMINATE_MESSAGE);
        return this;
    }

    @Override
    public final boolean isActive() {
        return !terminatedFlag;
    }

    @Override
    protected final boolean hasBeenStopped() {
        return stoppedFlag;
    }

    /**
     * Removes the head of the message queue
     *
     * @return The head message, or null, if the message queue is empty
     */
    @Override
    protected ActorMessage sweepNextMessage() {
        return (ActorMessage) core.sweepNextMessage();
    }

    @Override
    public MessageStream send(final Object message) {
        core.store(createActorMessage(message));
        return this;
    }

    @Override
    public void setParallelGroup(final PGroup group) {
        super.setParallelGroup(group);
        core.attachToThreadPool(group.getThreadPool());
    }

    /**
     * Retrieves the sender actor of the currently processed message.
     *
     * @return The sender of the currently processed message or null, if the message was not sent by an actor
     * @throws groovyx.gpars.actor.impl.ActorReplyException If some of the replies failed to be sent.
     */
    public final MessageStream getSender() {
        return currentSender;
    }

    /**
     * Sends a reply to all currently processed messages. Throws ActorReplyException if some messages
     * have not been sent by an actor. For such cases use replyIfExists().
     *
     * @param message reply message
     * @throws groovyx.gpars.actor.impl.ActorReplyException If some of the replies failed to be sent.
     */
    public final void reply(final Object message) {
        if (currentSender == null) {
            throw new ActorReplyException(CANNOT_SEND_REPLIES_NO_SENDER_HAS_BEEN_REGISTERED);
        } else {
            currentSender.send(message);
        }
    }

    /**
     * Sends a reply to all currently processed messages, which have been sent by an actor.
     * Ignores potential errors when sending the replies, like no sender or sender already stopped.
     *
     * @param message reply message
     */
    public final void replyIfExists(final Object message) {
        if (currentSender == null) return;
        try {
            currentSender.send(message);
        } catch (IllegalStateException ignore) {
        }
    }

    private void runEnhancedWithoutRepliesOnMessages(final ActorMessage message, final Closure code, final Object arguments) {
        assert message != null;
        currentSender = message.getSender();
        code.call(arguments);
    }
}
