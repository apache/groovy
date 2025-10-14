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

package groovyx.gpars.actor.impl;

import groovy.lang.GroovyRuntimeException;
import groovy.time.BaseDuration;
import groovyx.gpars.actor.Actor;
import groovyx.gpars.actor.ActorMessage;
import groovyx.gpars.actor.Actors;
import groovyx.gpars.group.PGroup;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.LockSupport;

import static groovyx.gpars.actor.impl.ActorException.STOP;
import static groovyx.gpars.actor.impl.ActorException.TERMINATE;

/**
 * @author Alex Tkachman, Vaclav Pech
 */
@SuppressWarnings({"UnqualifiedStaticUsage"})
public abstract class SequentialProcessingActor extends ReplyingMessageStream implements Runnable {

    private static final long serialVersionUID = 6479220959200502418L;

    /**
     * Stored incoming messages. The most recently received message is in the head of the list.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    //modified through inputQueryUpdater
    private volatile Node inputQueue;

    /**
     * Stores messages ready for processing by the actor. The oldest message is in the head of the list.
     * Messages are transferred from the inputQueue into the output queue in the transferQueues() method.
     */
    private Node outputQueue;

    private final AtomicBoolean ongoingThreadTermination = new AtomicBoolean(false);

    private static final AtomicReferenceFieldUpdater<SequentialProcessingActor, Node> inputQueueUpdater = AtomicReferenceFieldUpdater.newUpdater(SequentialProcessingActor.class, Node.class, "inputQueue");

    private volatile Thread waitingThread;

    protected static final int S_ACTIVE_MASK = 1;
    protected static final int S_FINISHING_MASK = 2;
    protected static final int S_FINISHED_MASK = 4;
    protected static final int S_STOP_TERMINATE_MASK = 8;

    protected static final int S_NOT_STARTED = 0;
    protected static final int S_RUNNING = S_ACTIVE_MASK;

    protected static final int S_STOPPING = S_STOP_TERMINATE_MASK | S_FINISHING_MASK | S_ACTIVE_MASK;
    protected static final int S_TERMINATING = S_FINISHING_MASK | S_ACTIVE_MASK;

    protected static final int S_STOPPED = S_STOP_TERMINATE_MASK | S_FINISHED_MASK;
    protected static final int S_TERMINATED = S_FINISHED_MASK;

    /**
     * Indicates whether the actor should terminate
     */
    protected volatile int stopFlag = S_NOT_STARTED;

    protected static final AtomicIntegerFieldUpdater<SequentialProcessingActor> stopFlagUpdater = AtomicIntegerFieldUpdater.newUpdater(SequentialProcessingActor.class, "stopFlag");

    private static final String SHOULD_NOT_REACH_HERE = "Should not reach here";

    /**
     * Checks the current status of the Actor.
     */
    @Override
    public final boolean isActive() {
        return (stopFlag & S_ACTIVE_MASK) != 0;
    }

    /**
     * Checks the supplied message and throws either STOP or TERMINATE, if the message is a Stop or Terminate message respectively.
     *
     * @param toProcess The next message to process by the actors
     */
    private void throwIfNeeded(final ActorMessage toProcess) {
        if (toProcess == STOP_MESSAGE) {
            stopFlag = S_STOPPING;
            throw STOP;
        }

        if (toProcess == TERMINATE_MESSAGE) {
            stopFlag = S_TERMINATING;
            throw TERMINATE;
        }
    }

    /**
     * Polls a message from the queues
     *
     * @return The message
     */
    protected final ActorMessage pollMessage() {
        assert isActorThread();

        transferQueues();

        ActorMessage toProcess = null;
        if (outputQueue != null) {
            toProcess = outputQueue.msg;
            outputQueue = outputQueue.next;
        }
        return toProcess;
    }

    /**
     * Takes a message from the queues. Blocks until a message is available.
     *
     * @return The message
     * @throws InterruptedException If the thread gets interrupted.
     */
    protected final ActorMessage takeMessage() throws InterruptedException {
        assert isActorThread();

        while (true) {
            final ActorMessage message = awaitNextMessage(0L);
            if (message != null) return message;
        }
    }

    /**
     * Takes a message from the queues. Blocks until a message is available.
     *
     * @param timeout  Max time to wait for a message
     * @param timeUnit The units for the timeout
     * @return The message
     * @throws InterruptedException If the thread gets interrupted.
     */
    protected ActorMessage takeMessage(final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        assert isActorThread();

        final long endTime = System.nanoTime() + timeUnit.toNanos(timeout);
        do {
            final ActorMessage message = awaitNextMessage(endTime);
            if (message != null) return message;
        } while (System.nanoTime() < endTime);

        handleTimeout();
        return TIMEOUT_MESSAGE;
    }

    /**
     * Holds common functionality for takeMessage() methods.
     *
     * @param endTime End of the timeout, 0 if no timeout was set
     * @return The next message
     * @throws InterruptedException If the thread has been interrupted
     */
    private ActorMessage awaitNextMessage(final long endTime) throws InterruptedException {
        transferQueues();

        waitingThread = Thread.currentThread();
        if (outputQueue != null) return retrieveNextMessage();

        if (endTime == 0L) LockSupport.park();
        else LockSupport.parkNanos(endTime - System.nanoTime());
        MessageStream.reInterrupt();
        return null;
    }

    /**
     * Takes the next message from the outputQueue, decrements the counter and possibly throws control exceptions
     *
     * @return The next message
     */
    private ActorMessage retrieveNextMessage() {
        final ActorMessage toProcess = outputQueue.msg;
        outputQueue = outputQueue.next;

        throwIfNeeded(toProcess);
        return toProcess;
    }

    /**
     * Transfers messages from the input queue into the output queue, reverting the order of the elements.
     */
    private void transferQueues() {
        if (outputQueue == null) {
            Node node = inputQueueUpdater.getAndSet(this, null);
            while (node != null) {
                final Node next = node.next;
                node.next = outputQueue;
                outputQueue = node;
                node = next;
            }
        }
    }

    /**
     * Creates a new instance, sets the default actor group.
     */
    protected SequentialProcessingActor() {
        setParallelGroup(Actors.defaultActorPGroup);
    }

    /**
     * Sets the actor's group.
     * It can only be invoked before the actor is started.
     *
     * @param group new group
     */
    @Override
    public final void setParallelGroup(final PGroup group) {
        if (stopFlag != S_NOT_STARTED) {
            throw new IllegalStateException("Cannot reset actor's group after it was started.");
        }
        super.setParallelGroup(group);
        parallelGroup = group;
    }

    @Override
    public final MessageStream send(final Object message) {

        final Node toAdd = new Node(createActorMessage(message));

        while (true) {
            final Node prev = inputQueue;
            toAdd.next = prev;
            if (inputQueueUpdater.compareAndSet(this, prev, toAdd)) {

                final Thread w = waitingThread;
                if (w != null) {
                    waitingThread = null;
                    LockSupport.unpark(w);
                }
                break;
            }
        }
        return this;
    }

    @Override
    protected final boolean hasBeenStopped() {
        return stopFlag != S_RUNNING;
    }

    @Override
    protected void handleTermination() {
        if (stopFlag == S_STOPPING)
            stopFlag = S_STOPPED;
        else if (stopFlag == S_TERMINATING)
            stopFlag = S_TERMINATED;
        else
            //noinspection ArithmeticOnVolatileField
            throw new IllegalStateException("Messed up actors state detected when terminating: " + stopFlag);

        try {
            super.handleTermination();
        } finally {
            getJoinLatch().bindUnique(null);
        }
    }

    @Override
    public Actor silentStart() {
        throw new UnsupportedOperationException("Old actors cannot start silently. Use DefaultActor instead.");
    }

    /**
     * Starts the Actor. No messages can be send or received before an Actor is started.
     *
     * @return this (the actor itself) to allow method chaining
     */
    @Override
    public final SequentialProcessingActor start() {
        if (!stopFlagUpdater.compareAndSet(this, S_NOT_STARTED, S_RUNNING)) {
            throw new IllegalStateException(ACTOR_HAS_ALREADY_BEEN_STARTED);
        }
        parallelGroup.getThreadPool().execute(this);
        send(START_MESSAGE);
        return this;
    }

    /**
     * Send message to stop to the actor.
     * All messages in queue will be processed before stopped but no new messages will be accepted
     * after that point
     *
     * @return this (the actor itself) to allow method chaining
     */
    @Override
    public final Actor stop() {
        if (stopFlagUpdater.compareAndSet(this, S_RUNNING, S_STOPPING)) {
            send(STOP_MESSAGE);
        }
        return this;
    }

    /**
     * Terminate the Actor. The background thread will be interrupted, unprocessed messages will be passed to the afterStop
     * method, if exists.
     * Has no effect if the Actor is not started.
     *
     * @return this (the actor itself) to allow method chaining
     */
    @Override
    public final Actor terminate() {
        while (true) {
            final int flag = stopFlag;
            if ((flag & S_FINISHED_MASK) != 0 || flag == S_TERMINATING)
                break;

            if (stopFlagUpdater.compareAndSet(this, flag, S_TERMINATING)) {
                if (isActorThread()) {
                    throw TERMINATE;
                }

                try {
                    while (!ongoingThreadTermination.compareAndSet(false, true)) //noinspection CallToThreadYield
                        Thread.yield();
                    if (currentThread != null) {
                        currentThread.interrupt();
                    } else {
                        // just to make sure that scheduled
                        try {
                            send(TERMINATE_MESSAGE);
                        } catch (IllegalStateException ignore) {
                        }
                    }
                } finally {
                    ongoingThreadTermination.set(false);
                }

                break;
            }
        }

        return this;
    }

    /**
     * Retrieves a message from the message queue, waiting, if necessary, for a message to arrive.
     *
     * @return The message retrieved from the queue, or null, if the timeout expires.
     * @throws InterruptedException If the thread is interrupted during the wait. Should propagate up to stop the thread.
     */
    protected abstract Object receiveImpl() throws InterruptedException;

    /**
     * Retrieves a message from the message queue, waiting, if necessary, for a message to arrive.
     *
     * @param timeout how long to wait before giving up, in units of unit
     * @param units   a TimeUnit determining how to interpret the timeout parameter
     * @return The message retrieved from the queue, or null, if the timeout expires.
     * @throws InterruptedException If the thread is interrupted during the wait. Should propagate up to stop the thread.
     */
    protected abstract Object receiveImpl(final long timeout, final TimeUnit units) throws InterruptedException;

    /**
     * Retrieves a message from the message queue, waiting, if necessary, for a message to arrive.
     *
     * @return The message retrieved from the queue, or null, if the timeout expires.
     * @throws InterruptedException If the thread is interrupted during the wait. Should propagate up to stop the thread.
     */
    protected final Object receive() throws InterruptedException {
        final Object msg = receiveImpl();
        return SequentialProcessingActor.unwrapMessage(msg);
    }

    /**
     * Retrieves a message from the message queue, waiting, if necessary, for a message to arrive.
     *
     * @param timeout how long to wait before giving up, in units of unit
     * @param units   a TimeUnit determining how to interpret the timeout parameter
     * @return The message retrieved from the queue, or null, if the timeout expires.
     * @throws InterruptedException If the thread is interrupted during the wait. Should propagate up to stop the thread.
     */
    protected final Object receive(final long timeout, final TimeUnit units) throws InterruptedException {
        final Object msg = receiveImpl(timeout, units);
        return SequentialProcessingActor.unwrapMessage(msg);
    }

    private static Object unwrapMessage(final Object msg) {
        //more a double-check here, since all current implementations of the receiveImpl() method do unwrap already
        if (msg instanceof ActorMessage) {
            return ((ActorMessage) msg).getPayLoad();
        } else {
            return msg;
        }
    }

    /**
     * Retrieves a message from the message queue, waiting, if necessary, for a message to arrive.
     *
     * @param duration how long to wait before giving up, in units of unit
     * @return The message retrieved from the queue, or null, if the timeout expires.
     * @throws InterruptedException If the thread is interrupted during the wait. Should propagate up to stop the thread.
     */
    protected final Object receive(final BaseDuration duration) throws InterruptedException {
        return receive(duration.toMilliseconds(), TimeUnit.MILLISECONDS);
    }

    /**
     * Removes the head of the message queue
     *
     * @return The head message, or null, if the message queue is empty
     */
    @Override
    protected final ActorMessage sweepNextMessage() {
        return pollMessage();
    }

    /**
     * Represents an element in the message queue. Holds an ActorMessage and a reference to the next element in the queue.
     * The reference is null for the last element in the queue.
     */
    private static class Node {
        volatile Node next;
        final ActorMessage msg;

        Node(final ActorMessage actorMessage) {
            this.msg = actorMessage;
        }
    }

    /**
     * This method represents the body of the actor. It is called upon actor's start and can exit either
     * normally by return or due to actor being stopped through the stop() method, which cancels the current
     * actor action.  Provides an extension point for subclasses to provide their custom {@code Actor}'s
     * message handling code.
     */
    protected abstract void act();

    @Override
    @SuppressWarnings({"ThrowCaughtLocally", "OverlyLongMethod"})
    public void run() {
        //noinspection OverlyBroadCatchBlock
        try {
            assert currentThread == null;

            registerCurrentActorWithThread(this);
            currentThread = Thread.currentThread();

            try {
                if (stopFlag == S_TERMINATING) {
                    throw TERMINATE;
                }
                final ActorMessage toProcess = takeMessage();

                if (toProcess == START_MESSAGE) {
                    handleStart();

                    // if we came here it means no loop was started
                    stopFlag = S_STOPPING;
                    throw STOP;
                }

            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } catch (ActorTerminationException ignore) {
        } catch (ActorStopException termination) {
            assert stopFlag != S_STOPPED;
            assert stopFlag != S_TERMINATED;
        } catch (InterruptedException e) {
            assert stopFlag != S_STOPPED;
            assert stopFlag != S_TERMINATED;
            stopFlag = S_TERMINATING;
            handleInterrupt(e);
        } catch (Throwable e) {
            assert stopFlag != S_STOPPED;
            assert stopFlag != S_TERMINATED;
            stopFlag = S_TERMINATING;
            handleException(e);
        } finally {
            try {
                while (!ongoingThreadTermination.compareAndSet(false, true)) //noinspection CallToThreadYield
                    Thread.yield();
                Thread.interrupted();
                handleTermination();
            } finally {
                deregisterCurrentActorWithThread();
                currentThread = null;
                ongoingThreadTermination.set(false);
            }
        }
    }

    protected final void checkStopTerminate() {
        if (hasBeenStopped()) {
            if (stopFlag == S_TERMINATING)
                throw TERMINATE;

            if (stopFlag != S_STOPPING)
                throw new IllegalStateException(SHOULD_NOT_REACH_HERE);
        }
    }
}
