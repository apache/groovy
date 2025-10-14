// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2011, 2013, 2014  The original author or authors
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
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import groovy.time.BaseDuration;
import groovyx.gpars.GParsConfig;
import groovyx.gpars.MessagingRunnable;
import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.actor.remote.RemoteActor;
import groovyx.gpars.dataflow.DataCallback;
import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.dataflow.Promise;
import groovyx.gpars.dataflow.expression.DataflowExpression;
import groovyx.gpars.group.PGroup;
import groovyx.gpars.remote.RemoteConnection;
import groovyx.gpars.remote.RemoteHost;
import groovyx.gpars.serial.DefaultRemoteHandle;
import groovyx.gpars.serial.RemoteHandle;
import groovyx.gpars.serial.RemoteSerialized;
import groovyx.gpars.serial.SerialContext;
import groovyx.gpars.serial.SerialHandle;
import groovyx.gpars.serial.SerialMsg;
import groovyx.gpars.serial.WithSerialId;
import groovyx.gpars.util.GeneralTimer;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Actors are active objects, which borrow a thread from a thread pool.
 * The Actor interface provides means to send messages to the actor, start and stop the background thread as well as
 * check its status.
 *
 * @author Vaclav Pech, Alex Tkachman
 */
public abstract class Actor extends MessageStream {

    /**
     * Maps each thread to the actor it currently processes.
     * Used in the send() method to remember the sender of each message for potential replies
     */
    private static final ThreadLocal<Actor> currentActorPerThread = new ThreadLocal<Actor>();
    private static final long serialVersionUID = -3491276479442857422L;
    public static final String CANNOT_SEND_REPLIES_NO_SENDER_HAS_BEEN_REGISTERED = "Cannot send replies. No sender has been registered.";

    private final DataflowExpression<Object> joinLatch;

    /**
     * The parallel group to which the message stream belongs
     */
    protected volatile PGroup parallelGroup;
    protected static final ActorMessage START_MESSAGE = new ActorMessage("Start", null);
    protected static final ActorMessage STOP_MESSAGE = new ActorMessage("STOP_MESSAGE", null);
    protected static final ActorMessage TERMINATE_MESSAGE = new ActorMessage("TERMINATE_MESSAGE", null);
    private static final String AFTER_START = "afterStart";
    private static final String RESPONDS_TO = "respondsTo";
    private static final String ON_DELIVERY_ERROR = "onDeliveryError";
    private static final Object[] EMPTY_ARGUMENTS = new Object[0];
    @SuppressWarnings({"ConstantDeclaredInAbstractClass"})
    public static final String TIMEOUT = "TIMEOUT";
    protected static final ActorMessage TIMEOUT_MESSAGE = new ActorMessage(TIMEOUT, null);

    private volatile Closure onStop = null;

    protected volatile Thread currentThread;
    protected static final String ACTOR_HAS_ALREADY_BEEN_STARTED = "Actor has already been started.";
    /**
     * Timer holding timeouts for react methods
     */
    protected static final GeneralTimer timer = GParsConfig.retrieveDefaultTimer("GPars Actor Timer", true);

    protected Actor() {
        this(new DataflowVariable<Object>());
    }

    /**
     * Constructor to be used by deserialization
     *
     * @param joinLatch The instance of DataflowExpression to use for join operation
     */
    protected Actor(final DataflowExpression<Object> joinLatch) {
        this(joinLatch, Actors.defaultActorPGroup);
    }

    protected Actor(final DataflowExpression<Object> joinLatch, final PGroup parallelGroup) {
        this.joinLatch = joinLatch;
        this.parallelGroup = parallelGroup;
    }

    /**
     * Retrieves the group to which the actor belongs
     *
     * @return The group
     */
    public final PGroup getParallelGroup() {
        return parallelGroup;
    }

    /**
     * Sets the parallel group.
     * It can only be invoked before the actor is started.
     *
     * @param group new group
     */
    public void setParallelGroup(final PGroup group) {
        if (group == null) {
            throw new IllegalArgumentException("Cannot set actor's group to null.");
        }

        parallelGroup = group;
    }

    /**
     * Sends a message and execute continuation when reply became available.
     *
     * @param message message to send
     * @param closure closure to execute when reply became available
     * @return The message that came in reply to the original send.
     */
    @SuppressWarnings({"AssignmentToMethodParameter"})
    public final <T> MessageStream sendAndContinue(final T message, Closure closure) {
        closure = (Closure) closure.clone();
        closure.setDelegate(this);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        return send(message, new DataCallback(closure, parallelGroup));
    }

    /**
     * Sends a message and returns a promise for the reply.
     *
     * @param message message to send
     * @return The message that came in reply to the original send.
     */
    @SuppressWarnings({"AssignmentToMethodParameter"})
    public final <T> Promise<Object> sendAndPromise(final T message) {
        final DataflowVariable<Object> result = new DataflowVariable<Object>();
        sendAndContinue(message, new MessagingRunnable<Object>() {
            @Override
            protected void doRun(final Object argument) {
                result.leftShift(argument);
            }
        });
        return result;
    }

    /**
     * Starts the Actor without sending the START_MESSAGE message to speed the start-up.
     * The potential custom afterStart handlers won't be run.
     * No messages can be sent or received before an Actor is started.
     *
     * @return same actor
     */
    public abstract Actor silentStart();

    /**
     * Starts the Actor and sends it the START_MESSAGE to run any afterStart handlers.
     * No messages can be sent or received before an Actor is started.
     *
     * @return same actor
     */
    public abstract Actor start();

    /**
     * Send message to stop to the Actor. The actor will finish processing the current message and all unprocessed messages will be passed to the afterStop method, if such exists.
     * No new messages will be accepted since that point.
     * Has no effect if the Actor is not started.
     *
     * @return same actor
     */
    public abstract Actor stop();

    /**
     * Terminates the Actor. Unprocessed messages will be passed to the afterStop method, if exists.
     * No new messages will be accepted since that point.
     * Has no effect if the Actor is not started.
     *
     * @return same actor
     */
    public abstract Actor terminate();

    /**
     * Checks the current status of the Actor.
     *
     * @return current status of the Actor.
     */
    public abstract boolean isActive();

    /**
     * Joins the actor. Waits for its termination.
     *
     * @throws InterruptedException when interrupted while waiting
     */
    public final void join() throws InterruptedException {
        joinLatch.getVal();
    }

    /**
     * Notify listener when finished
     *
     * @param listener listener to notify
     */
    public final void join(final MessageStream listener) {
        joinLatch.getValAsync(listener);
    }

    /**
     * Joins the actor. Waits for its termination.
     *
     * @param timeout timeout
     * @param unit    units of timeout
     * @throws InterruptedException if interrupted while waiting
     */
    public final void join(final long timeout, final TimeUnit unit) throws InterruptedException {
        if (timeout > 0L) {
            joinLatch.getVal(timeout, unit);
        } else {
            joinLatch.getVal();
        }
    }

    /**
     * Joins the actor. Waits for its termination.
     *
     * @param duration timeout to wait
     * @throws InterruptedException if interrupted while waiting
     */
    public final void join(final BaseDuration duration) throws InterruptedException {
        join(duration.toMilliseconds(), TimeUnit.MILLISECONDS);
    }

    /**
     * Join-point for this actor
     *
     * @return The DataflowExpression instance, which is used to join this actor
     */
    public DataflowExpression<Object> getJoinLatch() {
        return joinLatch;
    }

    /**
     * Registers the actor with the current thread
     *
     * @param currentActor The actor to register
     */
    protected static void registerCurrentActorWithThread(final Actor currentActor) {
        Actor.currentActorPerThread.set(currentActor);
    }

    /**
     * Deregisters the actor registered from the thread
     */
    protected static void deregisterCurrentActorWithThread() {
        Actor.currentActorPerThread.set(null);
    }

    /**
     * Retrieves the actor registered with the current thread
     *
     * @return The associated actor
     */
    public static Actor threadBoundActor() {
        return Actor.currentActorPerThread.get();
    }

    protected final ActorMessage createActorMessage(final Object message) {
        if (hasBeenStopped()) {
            //noinspection ObjectEquality
            if (message != TERMINATE_MESSAGE && message != STOP_MESSAGE)
                throw new IllegalStateException("The actor cannot accept messages at this point.");
        }

        final ActorMessage actorMessage;
        if (message instanceof ActorMessage) {
            actorMessage = (ActorMessage) message;
        } else {
            actorMessage = ActorMessage.build(message);
        }
        return actorMessage;
    }

    protected abstract boolean hasBeenStopped();

    @Override
    protected RemoteHandle createRemoteHandle(final SerialHandle handle, final SerialContext host) {
        return new RemoteActor.MyRemoteHandle(handle, host, joinLatch);
    }

    @SuppressWarnings("unchecked")
    protected void handleStart() {
        final Object list = InvokerHelper.invokeMethod(this, RESPONDS_TO, new Object[]{AFTER_START});
        if (list != null && !((Collection<Object>) list).isEmpty()) {
            InvokerHelper.invokeMethod(this, AFTER_START, EMPTY_ARGUMENTS);
        }
    }

    protected void handleTermination() {
        final List<?> queue = sweepQueue();
        if (onStop != null)
            onStop.call(queue);

        callDynamic("afterStop", new Object[]{queue});
    }

    /**
     * Set on stop handler for this actor
     *
     * @param onStop The code to invoke when stopping
     */
    public final void onStop(final Closure onStop) {
        if (onStop != null) {
            this.onStop = (Closure) onStop.clone();
            this.onStop.setDelegate(this);
            this.onStop.setResolveStrategy(Closure.DELEGATE_FIRST);
        }
    }


    @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
    protected void handleException(final Throwable exception) {
        if (!callDynamic("onException", new Object[]{exception})) {
            System.err.println("An exception occurred in the Actor thread " + Thread.currentThread().getName());
            exception.printStackTrace(System.err);
        }
    }

    @SuppressWarnings({"TypeMayBeWeakened", "UseOfSystemOutOrSystemErr"})
    protected void handleInterrupt(final InterruptedException exception) {
        Thread.interrupted();
        if (!callDynamic("onInterrupt", new Object[]{exception})) {
            if (!hasBeenStopped()) {
                System.err.println("The actor processing thread has been interrupted " + Thread.currentThread().getName());
                exception.printStackTrace(System.err);
            }
        }
    }

    protected void handleTimeout() {
        callDynamic("onTimeout", EMPTY_ARGUMENTS);
    }

    private boolean callDynamic(final String method, final Object[] args) {
        final MetaClass metaClass = InvokerHelper.getMetaClass(this);
        final List<MetaMethod> list = metaClass.respondsTo(this, method);
        if (list != null && !list.isEmpty()) {
            boolean hasArgs = false;
            for (final MetaMethod metaMethod : list) {
                if (metaMethod.getParameterTypes().length > 0) hasArgs = true;
            }
            if (hasArgs) InvokerHelper.invokeMethod(this, method, args);
            else InvokerHelper.invokeMethod(this, method, EMPTY_ARGUMENTS);
            return true;
        }
        return false;
    }

    /**
     * Removes the head of the message queue
     *
     * @return The head message, or null, if the message queue is empty
     */
    protected abstract ActorMessage sweepNextMessage();

    /**
     * Clears the message queue returning all the messages it held.
     *
     * @return The messages stored in the queue
     */
    @SuppressWarnings("unchecked")
    final List<ActorMessage> sweepQueue() {
        final List<ActorMessage> messages = new ArrayList<ActorMessage>();

        ActorMessage message = sweepNextMessage();
        while (message != null && message != STOP_MESSAGE) {
            final Object senderMethodList = InvokerHelper.invokeMethod(message.getSender(), RESPONDS_TO, new Object[]{ON_DELIVERY_ERROR});
            if (senderMethodList != null && !((Collection<Object>) senderMethodList).isEmpty()) {
                InvokerHelper.invokeMethod(message.getSender(), ON_DELIVERY_ERROR, message.getPayLoad());
            } else {
                final Object payloadMethodList = InvokerHelper.invokeMethod(message.getPayLoad(), RESPONDS_TO, new Object[]{ON_DELIVERY_ERROR});
                if (payloadMethodList != null && !((Collection<Object>) payloadMethodList).isEmpty()) {
                    InvokerHelper.invokeMethod(message.getPayLoad(), ON_DELIVERY_ERROR, EMPTY_ARGUMENTS);
                }
            }

            messages.add(message);
            message = sweepNextMessage();
        }
        return messages;
    }

    /**
     * Checks whether the current thread is the actor's current thread.
     *
     * @return True if invoked from within an actor thread
     */
    public final boolean isActorThread() {
        return Thread.currentThread() == currentThread;
    }
}
