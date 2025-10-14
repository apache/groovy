// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2010, 2013  The original author or authors
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
import groovy.lang.DelegatesTo;
import groovy.time.Duration;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * The DefaultActor class is the base for all stateful actors, who need to maintain implicit state between subsequent message arrivals.
 * Allowing the actor creator to structure code in a continuation-like style with message retrieval react commands mixed within normal code
 * makes implementation of some algorithms particularly easy.
 * <p>
 * The DefaultActor upon start-up will grab a thread from the associated actor thread pool and run its body.
 * The body is either the parameter passed to the constructor, or the act() method, if no parameter has been set.
 * The parameter takes precedence over the act() method.
 * Once a react() method call is discovered within the actor's body, its Closure-typed parameter will be scheduled for processing on next message arrival.
 * To preserve the actor principle of at-most-one active thread per actor, the next message, however, will only be handled once the currently run code finishes and frees the current thread.
 * It is thus advisable to avoid code after call to react().
 * The loop() method will ensure its body is executed repeatedly, until the actor either finishes or an optional loop condition is not met.
 * </p>
 *
 * @author Vaclav Pech
 *         Date: Nov 4th 2010
 */
public class DefaultActor extends AbstractLoopingActor {

    private Closure nextContinuation;
    private Closure loopClosure;

    /**
     * Misused also for the code to run at start-up
     */
    private Runnable loopCode;

    private Callable<Boolean> loopCondition;
    private Closure afterLoopCode;
    private boolean started = false;
    private static final long serialVersionUID = -439517926332934061L;

    /**
     * Creates an actor, which will execute its act() methods
     */
    public DefaultActor() {
        this(null);
    }

    /**
     * Creates an actor, which will execute the supplied code
     *
     * @param code A Runnable or Closure to be considered the actor's body
     */
    public DefaultActor(final Runnable code) {
        if (code != null) {
            if (code instanceof Closure) checkForBodyArguments((Closure) code);
            loopCode = code;
        }
        initialize(new DefaultActorClosure(this));
    }

    /**
     * If no parameter is provided at construction time, the act() method becomes the actor's body
     */
    protected void act() {
        throw new UnsupportedOperationException("The act method has not been overridden");
    }

    /**
     * Handles all incoming messages
     *
     * @param message The current message to process
     */
    final void onMessage(final Object message) {
        if (nextContinuation != null) {
            final Closure closure = nextContinuation;
            nextContinuation = null;
            closure.call(message);
        } else
            throw new IllegalStateException("The actor " + this + " cannot handle the message " + message + ", as it has no registered message handler at the moment.");
        if (nextContinuation == null && !terminatingFlag) {
            if (loopCondition == null || evalLoopCondition()) {
                if (loopCode == null)
                    if (loopClosure == null) terminate();
                    else loopClosure.call();
                else loopCode.run();
            } else {
                if (afterLoopCode != null) {
                    runAfterLoopCode(afterLoopCode);
                }
                if (nextContinuation == null) terminate();
            }
        }
    }

    /**
     * Ensures that the supplied closure will be invoked repeatedly in a loop.
     * The method never returns, but instead frees the processing thread back to the thread pool.
     *
     * @param code The closure to invoke repeatedly
     */
    public final void loop(@DelegatesTo(DefaultActor.class) final Runnable code) {
        doLoop(null, null, code);
    }

    /**
     * Ensures that the supplied closure will be invoked repeatedly in a loop.
     * The method never returns, but instead frees the processing thread back to the thread pool.
     *
     * @param numberOfLoops The loop will only be run the given number of times
     * @param code          The closure to invoke repeatedly
     */
    protected final void loop(final int numberOfLoops, @DelegatesTo(DefaultActor.class) final Runnable code) {
        loop(numberOfLoops, null, code);
    }

    /**
     * Ensures that the supplied closure will be invoked repeatedly in a loop.
     * The method never returns, but instead frees the processing thread back to the thread pool.
     *
     * @param numberOfLoops The loop will only be run the given number of times
     * @param afterLoopCode Code to run after the main actor's loop finishes
     * @param code          The closure to invoke repeatedly
     */
    protected final void loop(final int numberOfLoops, @DelegatesTo(DefaultActor.class) final Closure afterLoopCode, @DelegatesTo(DefaultActor.class) final Runnable code) {
        doLoop(new Callable<Boolean>() {
            private int counter = 0;

            @Override
            public Boolean call() {
                counter++;
                //noinspection UnnecessaryBoxing
                return Boolean.valueOf(counter <= numberOfLoops);
            }
        }, afterLoopCode, code);
    }

    /**
     * Ensures that the supplied closure will be invoked repeatedly in a loop.
     * The method never returns, but instead frees the processing thread back to the thread pool.
     *
     * @param condition A condition to evaluate before each iteration starts. If the condition returns false, the loop exits.
     * @param code      The closure to invoke repeatedly
     */
    protected final void loop(final Closure condition, @DelegatesTo(DefaultActor.class) final Runnable code) {
        loop(condition, null, code);

    }

    /**
     * Ensures that the supplied closure will be invoked repeatedly in a loop.
     * The method never returns, but instead frees the processing thread back to the thread pool.
     *
     * @param condition     A condition to evaluate before each iteration starts. If the condition returns false, the loop exits.
     * @param afterLoopCode Code to run after the main actor's loop finishes
     * @param code          The closure to invoke repeatedly
     */
    protected final void loop(final Closure condition, @DelegatesTo(DefaultActor.class) final Closure afterLoopCode, @DelegatesTo(DefaultActor.class) final Runnable code) {
        doLoop(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return (Boolean) condition.call();
            }
        }, afterLoopCode, code);

    }

    /**
     * Ensures that the supplied closure will be invoked repeatedly in a loop.
     * The method never returns, but instead frees the processing thread back to the thread pool.
     *
     * @param condition     A condition to evaluate before each iteration starts. If the condition returns false, the loop exits.
     * @param afterLoopCode Code to run after the main actor's loop finishes
     * @param code          The closure to invoke repeatedly
     */
    @SuppressWarnings({"OverlyComplexBooleanExpression"})
    private void doLoop(final Callable<Boolean> condition, final Closure afterLoopCode, final Runnable code) {
        checkForNull(code);

        if (afterLoopCode != null) {
            this.afterLoopCode = enhanceClosure(afterLoopCode);
        }
        loopCondition = condition;

        if (code instanceof Closure) {
            final Closure closure = (Closure) code;
            checkForBodyArguments(closure);
            final Closure enhancedClosure = enhanceClosure(closure);
            this.loopClosure = enhancedClosure;

            assert nextContinuation == null;
            while (!terminatingFlag && nextContinuation == null && (loopCondition == null || evalLoopCondition())) {
                enhancedClosure.call();
            }
            if (!terminatingFlag && nextContinuation == null && afterLoopCode != null) {
                runAfterLoopCode(afterLoopCode);
            }
        } else {
            this.loopCode = code;
            assert nextContinuation == null;
            while (!terminatingFlag && nextContinuation == null && (loopCondition == null || evalLoopCondition())) {
                loopCode.run();
            }
            if (!terminatingFlag && nextContinuation == null && afterLoopCode != null) {
                runAfterLoopCode(afterLoopCode);
            }
        }
    }

    private void runAfterLoopCode(final Closure afterLoopCode) {
        loopCondition = null;
        loopCode = null;
        loopClosure = null;
        this.afterLoopCode = null;
        afterLoopCode.call();
    }

    private boolean evalLoopCondition() {
        try {
            return loopCondition.call();
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Schedules an ActorAction to take the next message off the message queue and to pass it on to the supplied closure.
     * The method never returns, but instead frees the processing thread back to the thread pool.
     *
     * @param code The code to handle the next message. The reply() and replyIfExists() methods are available inside
     *             the closure to send a reply back to the actor, which sent the original message.
     */
    public final void react(@DelegatesTo(DefaultActor.class) final Closure code) {
        react(-1L, code);
    }

    /**
     * Schedules an ActorAction to take the next message off the message queue and to pass it on to the supplied closure.
     * The method never returns, but instead frees the processing thread back to the thread pool.
     *
     * @param duration Time to wait at most for a message to arrive. The actor terminates if a message doesn't arrive within the given timeout.
     *                 The TimeCategory DSL to specify timeouts must be enabled explicitly inside the Actor's act() method.
     * @param code     The code to handle the next message. The reply() and replyIfExists() methods are available inside
     *                 the closure to send a reply back to the actor, which sent the original message.
     */
    protected final void react(final Duration duration, @DelegatesTo(DefaultActor.class) final Closure code) {
        react(duration.toMilliseconds(), code);
    }

    /**
     * Schedules an ActorAction to take the next message off the message queue and to pass it on to the supplied closure.
     * The method never returns, but instead frees the processing thread back to the thread pool.
     *
     * @param timeout  Time in milliseconds to wait at most for a message to arrive. The actor terminates if a message doesn't arrive within the given timeout.
     * @param timeUnit a TimeUnit determining how to interpret the timeout parameter
     * @param code     The code to handle the next message. The reply() and replyIfExists() methods are available inside
     *                 the closure to send a reply back to the actor, which sent the original message.
     */
    protected final void react(final long timeout, final TimeUnit timeUnit, @DelegatesTo(DefaultActor.class) final Closure code) {
        react(timeUnit.toMillis(timeout), code);
    }

    /**
     * Schedules an ActorAction to take the next message off the message queue and to pass it on to the supplied closure.
     * The method never returns, but instead frees the processing thread back to the thread pool.
     * Also adds reply() and replyIfExists() methods to the currentActor and the message.
     * These methods will call send() on the target actor (the sender of the original message).
     * The reply()/replyIfExists() methods invoked on the actor will be sent to all currently processed messages,
     * reply()/replyIfExists() invoked on a message will send a reply to the sender of that particular message only.
     *
     * @param timeout Time in milliseconds to wait at most for a message to arrive. The actor terminates if a message doesn't arrive within the given timeout.
     * @param code    The code to handle the next message. The reply() and replyIfExists() methods are available inside
     *                the closure to send a reply back to the actor, which sent the original message.
     */
    protected final void react(final long timeout, @DelegatesTo(DefaultActor.class) final Closure code) {
        if (!isActorThread()) {
            throw new IllegalStateException("Cannot call react from a thread which is not owned by the actor");
        }
        checkForNull(code);
        checkForMessageHandlerArguments(code);
        nextContinuation = enhanceClosure(code);
        if (timeout >= 0L) setTimeout(timeout);
//        throw ActorException.CONTINUE;
    }

    /**
     * Not supported by DefaultActor
     *
     * @return same actor
     */
    @Override
    public Actor silentStart() {
        throw new UnsupportedOperationException("Old actors cannot start silently. Use DefaultActor instead.");
    }

    /**
     * Starts the Actor and sends it the START_MESSAGE to run any afterStart handlers.
     * No messages can be sent or received before an Actor is started.
     *
     * @return same actor
     */
    @Override
    public Actor start() {
        if (started) throw new IllegalStateException("The DefaultActor cannot be restarted.");
        started = true;
        return super.start();
    }

    /**
     * Called once the START_MESSAGE arrives.
     * We need to run the actor's body here, letting it set nextContinuation to hols the next message handler
     */
    @Override
    protected void handleStart() {
        super.handleStart();
        try {
            final Runnable startCode = loopCode;
            loopCode = null;
            if (startCode != null) {
                if (startCode instanceof Closure) {
                    final Closure closure = enhanceClosure((Closure) startCode);
                    closure.call();
                } else {
                    startCode.run();
                }
            } else act();
            if (nextContinuation == null) terminate();
        } catch (IllegalStateException e) {
            terminate();
            throw e;
        }
    }

    private Closure enhanceClosure(final Closure closure) {
        final Closure cloned = (Closure) closure.clone();
        if (cloned.getOwner() == cloned.getDelegate()) {
            cloned.setResolveStrategy(Closure.DELEGATE_FIRST);
            cloned.setDelegate(this);
        } else {
            cloned.setDelegate(new ForwardingDelegate(cloned.getDelegate(), this));
        }
        return cloned;
    }

    private static void checkForNull(final Runnable code) {
        if (code == null)
            throw new IllegalArgumentException("An actor's message handlers and loops cannot be set to a null value.");
    }

    private static void checkForBodyArguments(final Closure closure) {
        if (closure.getMaximumNumberOfParameters() > 1)
            throw new IllegalArgumentException("An actor's body as well as a body of a loop can only expect 0 arguments. " + closure.getMaximumNumberOfParameters() + EXPECTED);
    }

    private static void checkForMessageHandlerArguments(final Closure code) {
        if (code.getMaximumNumberOfParameters() > 1)
            throw new IllegalArgumentException("An actor's message handler can only expect 0 or 1 argument. " + code.getMaximumNumberOfParameters() + EXPECTED);
    }

    private static final String EXPECTED = " expected.";
}
