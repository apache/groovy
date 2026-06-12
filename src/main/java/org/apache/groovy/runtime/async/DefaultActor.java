/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.runtime.async;

import groovy.concurrent.Actor;
import groovy.concurrent.ActorContext;
import groovy.concurrent.ActorOptions;
import groovy.concurrent.Awaitable;
import groovy.concurrent.Cancellable;
import groovy.concurrent.DataflowVariable;
import groovy.concurrent.ReactorHandler;
import groovy.concurrent.StatefulHandler;
import groovy.util.function.TriConsumer;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Default implementation of {@link Actor}.
 * <p>
 * Each actor runs on a dedicated worker thread (virtual on JDK 21+) and
 * dispatches messages sequentially from a FIFO mailbox, guaranteeing
 * thread-safe state access without locks. The mailbox is a
 * {@link LinkedBlockingDeque} so that
 * {@link ActorContext#unstashAll()} can re-inject deferred messages at
 * the head of the queue; a {@link Semaphore} gates send-side capacity
 * for bounded mailboxes (the deque itself is internally unbounded). A
 * message's permit is released as soon as the worker takes it from the
 * deque — so the bound applies to the queue, not to messages currently
 * being processed or held in the stash buffer; the stash buffer itself
 * is unbounded.
 *
 * @param <T> the message type
 * @see Actor
 * @since 6.0.0
 */
public final class DefaultActor<T> implements Actor<T> {

    private static final Object POISON = new Object();

    /**
     * Thread-local back-reference to the actor whose handler is
     * currently executing on this thread. Set around each handler /
     * {@code onError} dispatch and cleared in {@code finally}. Read via
     * {@link #currentlyExecuting()}, exposed publicly through
     * {@link Actor#currentSelf()}.
     */
    private static final ThreadLocal<Actor<?>> CURRENT = new ThreadLocal<>();

    private final LinkedBlockingDeque<Envelope<T>> queue = new LinkedBlockingDeque<>();
    /** null when the mailbox is unbounded. */
    private final Semaphore capacity;
    private final MessageProcessor<T> processor;
    private final ActorOptions options;
    private final ActorContext<T> context;

    private volatile boolean active = true;
    // Set true once processLoop has exited (after drainStashOnExit).
    // Distinguishes "draining" (!active, !terminated) from "fully shut
    // down" (!active, terminated) for callers of isTerminated().
    private volatile boolean terminated;
    private volatile TriConsumer<ActorContext<T>, Throwable, ? super T> errorHandler;
    // Identifies the thread running processLoop; used both to police
    // context-mutating calls (become/stash/unstashAll) and to detect
    // self-sends that would deadlock a bounded BLOCK mailbox.
    private volatile Thread workerThread;

    // ---- Worker-thread-only state (no synchronisation needed) ----------

    /** Messages deferred via {@link ActorContext#stash()}, in FIFO order. */
    private final ArrayDeque<Envelope<T>> stashBuffer = new ArrayDeque<>();
    private Envelope<T> currentEnvelope;
    private boolean currentStashed;

    /**
     * Outstanding scheduled timers (one-shot or recurring) that have
     * not yet fired (or, for recurring timers, that have not been
     * cancelled). Cancelled wholesale on {@link #stop()} so timers do
     * not keep firing into a dead actor. Accessed from both the worker
     * thread (registration) and the scheduler thread (cleanup of
     * fired one-shots), so the collection itself is concurrent.
     */
    private final Set<ScheduledFuture<?>> pendingTimers = ConcurrentHashMap.newKeySet();

    // ---- Construction / factories --------------------------------------

    private DefaultActor(MessageProcessor<T> processor, ActorOptions options) {
        this.processor = processor;
        this.options = options;
        this.capacity = options.isBounded() ? new Semaphore(options.mailboxCapacity()) : null;
        this.context = processor.makeContext(this);
        Executor executor = options.executor() != null
                ? options.executor()
                : AsyncSupport.getExecutor();
        executor.execute(this::processLoop);
    }

    /**
     * Implementation hook for {@link Actor#currentSelf()}.
     */
    public static Actor<?> currentlyExecuting() {
        return CURRENT.get();
    }

    public static <T, R> Actor<T> reactor(Function<T, R> handler, ActorOptions options) {
        Objects.requireNonNull(handler, "handler must not be null");
        Objects.requireNonNull(options, "options must not be null");
        ReactorHandler<T, R> adapted = (ctx, msg) -> handler.apply(msg);
        return new DefaultActor<>(new ReactorProcessor<>(adapted), options);
    }

    public static <T, R> Actor<T> reactor(ReactorHandler<T, R> handler, ActorOptions options) {
        Objects.requireNonNull(handler, "handler must not be null");
        Objects.requireNonNull(options, "options must not be null");
        return new DefaultActor<>(new ReactorProcessor<>(handler), options);
    }

    public static <T, S> Actor<T> stateful(S initialState, BiFunction<S, T, S> handler, ActorOptions options) {
        Objects.requireNonNull(handler, "handler must not be null");
        Objects.requireNonNull(options, "options must not be null");
        StatefulHandler<S, T> adapted = (ctx, s, m) -> handler.apply(s, m);
        return new DefaultActor<>(new StatefulProcessor<>(initialState, adapted), options);
    }

    public static <T, S> Actor<T> stateful(S initialState, StatefulHandler<S, T> handler, ActorOptions options) {
        Objects.requireNonNull(handler, "handler must not be null");
        Objects.requireNonNull(options, "options must not be null");
        return new DefaultActor<>(new StatefulProcessor<>(initialState, handler), options);
    }

    // ---- Actor interface ------------------------------------------------

    @Override
    public void send(T message) {
        Objects.requireNonNull(message, "message must not be null");
        if (!active) throw new IllegalStateException("Actor has been stopped");
        enqueue(new Envelope<>(message, null));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Awaitable<R> sendAndGet(T message) {
        Objects.requireNonNull(message, "message must not be null");
        if (!active) throw new IllegalStateException("Actor has been stopped");
        DataflowVariable<R> reply = new DataflowVariable<>();
        Envelope<T> envelope = new Envelope<>(message, (DataflowVariable<Object>) reply);
        try {
            enqueue(envelope);
        } catch (RuntimeException re) {
            // FAIL strategy or interrupt — also reflect on the reply so callers
            // awaiting it don't block forever.
            reply.bindError(re);
            throw re;
        }
        return reply;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isTerminated() {
        return terminated;
    }

    @Override
    public void stop() {
        if (!active) return;
        active = false;
        // Cancel any outstanding scheduled timers so they do not fire
        // into a stopped actor (one-shots would get IllegalStateException
        // from send and be silently caught, but periodic timers would
        // keep firing forever holding refs to this actor).
        for (ScheduledFuture<?> f : pendingTimers) {
            f.cancel(false);
        }
        pendingTimers.clear();
        // Best-effort wake-up. The deque is internally unbounded
        // (capacity is gated by the semaphore), so offerLast always
        // succeeds — but the POISON itself is just a marker; the
        // post-message drain check below is what actually terminates
        // the loop.
        queue.offerLast(new Envelope<>(POISON, null));
    }

    @Override
    public Actor<T> onError(BiConsumer<Throwable, ? super T> handler) {
        Objects.requireNonNull(handler, "handler must not be null");
        this.errorHandler = (ctx, t, msg) -> handler.accept(t, msg);
        return this;
    }

    @Override
    public Actor<T> onError(TriConsumer<ActorContext<T>, Throwable, ? super T> handler) {
        Objects.requireNonNull(handler, "handler must not be null");
        this.errorHandler = handler;
        return this;
    }

    // ---- Send-side -----------------------------------------------------

    /**
     * Routes an envelope through the configured mailbox policy. A permit
     * is acquired here at {@code send} time and released by the worker
     * the moment it dequeues the envelope, so the bound applies to the
     * size of the queue rather than to in-flight or stashed messages.
     */
    private void enqueue(Envelope<T> envelope) {
        if (capacity == null) {
            queue.addLast(envelope);
            return;
        }
        switch (options.overflow()) {
            case BLOCK -> {
                // A handler that sends to its own actor on a full BLOCK
                // mailbox would deadlock: it is the only consumer, and
                // acquire() would park it indefinitely waiting for
                // capacity it can never free. The worker-thread branch
                // uses tryAcquire so an external sender can't steal the
                // last permit between an availablePermits() check and
                // the acquire() (a multi-sender TOCTOU that would
                // otherwise re-introduce the same deadlock).
                if (Thread.currentThread() == workerThread) {
                    if (!capacity.tryAcquire()) {
                        throw new IllegalStateException(
                                "send from the actor's own handler would deadlock "
                                        + "the bounded BLOCK mailbox (capacity "
                                        + options.mailboxCapacity() + ")");
                    }
                } else {
                    try {
                        capacity.acquire();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted while sending to actor", ie);
                    }
                    // A blocked sender can park here across a stop(): the worker
                    // may drain the queue and terminate while we wait for a
                    // permit. Re-check before enqueuing so we don't add into a
                    // stopped/terminated actor — that would orphan the message
                    // and hang any sendAndGet reply. Release the permit we just
                    // took and fail (sendAndGet reflects the throw on the reply).
                    if (!active) {
                        capacity.release();
                        throw new IllegalStateException("Actor has been stopped");
                    }
                }
                queue.addLast(envelope);
            }
            case DROP_NEWEST -> {
                if (!capacity.tryAcquire()) {
                    if (envelope.reply != null) {
                        envelope.reply.bindError(new IllegalStateException(
                                "mailbox full (capacity " + options.mailboxCapacity()
                                        + "); message dropped"));
                    }
                    return;
                }
                queue.addLast(envelope);
            }
            case FAIL -> {
                if (!capacity.tryAcquire()) {
                    throw new IllegalStateException(
                            "mailbox full (capacity " + options.mailboxCapacity() + ")");
                }
                queue.addLast(envelope);
            }
        }
    }

    // ---- Receive-side --------------------------------------------------

    private void processLoop() {
        workerThread = Thread.currentThread();
        while (true) {
            try {
                Envelope<T> envelope = queue.takeFirst();
                // POISON is a wake-up marker, not a terminator. A message
                // can race past the !active check in send() and land in
                // the queue *after* POISON; treating POISON as an
                // unconditional return would orphan that message (and its
                // sendAndGet reply). The drain-on-stop guarantee is owned
                // entirely by the post-processing !active check below.
                if (envelope.message != POISON) {
                    // Free the send-side slot as soon as the message
                    // leaves the queue, so the bound applies to "queued"
                    // not "in flight". permitReleased guards against a
                    // double release when a stashed envelope is later
                    // unstashed and re-taken (it never re-acquired a
                    // permit on the way back into the queue).
                    if (capacity != null && !envelope.permitReleased) {
                        capacity.release();
                        envelope.permitReleased = true;
                    }
                    dispatch(envelope);
                }
                // Terminate once stop() has been requested and the queue
                // has fully drained — works even when stop() could not
                // enqueue the poison pill (bounded mailbox full at the
                // time of a self-stop) and ensures race-late sends are
                // honoured before exit. Any messages still in the stash
                // are rejected here so sendAndGet callers do not hang.
                if (!active && queue.isEmpty()) {
                    drainStashOnExit();
                    terminated = true;
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                drainStashOnExit();
                terminated = true;
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void dispatch(Envelope<T> envelope) {
        T msg = (T) envelope.message;
        currentEnvelope = envelope;
        currentStashed = false;
        boolean publishSelf = options.currentSelfEnabled();
        if (publishSelf) CURRENT.set(this);

        Object result = null;
        Throwable thrown = null;
        try {
            try {
                result = processor.process(context, msg);
            } catch (Throwable t) {
                thrown = t;
                // If the main handler stashed and then threw, undo the
                // stash so the message is reported as failed rather than
                // silently deferred. An onError handler can still re-stash
                // if it explicitly chooses to retry.
                if (currentStashed) {
                    if (!stashBuffer.isEmpty() && stashBuffer.peekLast() == envelope) {
                        stashBuffer.removeLast();
                    }
                    currentStashed = false;
                }
            }

            // Fire onError if registered; treat it as best-effort —
            // exceptions from the error handler are swallowed so they
            // cannot crash the processing loop. The handler may call
            // ctx.become(...) / ctx.stash() / ctx.unstashAll() and those
            // calls take effect normally.
            if (thrown != null) {
                TriConsumer<ActorContext<T>, Throwable, ? super T> handler = errorHandler;
                if (handler != null) {
                    try {
                        handler.accept(context, thrown, msg);
                    } catch (Throwable ignored) {
                    }
                }
            }

            // Finalise based on the post-onError stash state. Three
            // outcomes:
            //   stashed              → discard pending state; reply
            //                          stays unbound until the message
            //                          is later unstashed and processed
            //   threw & not stashed  → discard pending state; bind
            //                          error on reply
            //   success              → commit pending state; bind reply
            // The send-side permit was already released by the worker
            // loop when it dequeued this envelope, so capacity does not
            // need to be released here in any outcome.
            if (currentStashed) {
                processor.rollback();
            } else if (thrown != null) {
                processor.rollback();
                if (envelope.reply != null) envelope.reply.bindError(thrown);
            } else {
                processor.commit();
                if (envelope.reply != null) envelope.reply.bind(result);
            }
        } finally {
            currentEnvelope = null;
            currentStashed = false;
            if (publishSelf) CURRENT.remove();
        }
    }

    /**
     * Rejects any messages still in the stash on shutdown so that
     * {@code sendAndGet} callers do not wait on replies that will never
     * arrive. Stashed envelopes hold no send-side permit (it was
     * released when the worker first dequeued them), so capacity does
     * not need to be released here.
     */
    private void drainStashOnExit() {
        if (stashBuffer.isEmpty()) return;
        for (Envelope<T> stashed : stashBuffer) {
            if (stashed.reply != null) {
                stashed.reply.bindError(new IllegalStateException(
                        "actor stopped with message in stash"));
            }
        }
        stashBuffer.clear();
    }

    @Override
    public String toString() {
        return "Actor[active=" + active
                + ", queued=" + queue.size()
                + ", stashed=" + stashBuffer.size() + "]";
    }

    // ---- Helpers shared with the per-shape context implementations -----

    private void checkOnWorkerThread(String op) {
        if (Thread.currentThread() != workerThread) {
            throw new IllegalStateException(
                    op + " must be called from a handler invocation on the actor's worker thread");
        }
    }

    private void stashCurrent() {
        checkOnWorkerThread("stash()");
        if (currentEnvelope == null) {
            throw new IllegalStateException("stash() called outside handler dispatch");
        }
        if (currentStashed) return; // idempotent within a single dispatch

        if (options.isStashBounded() && stashBuffer.size() >= options.stashCapacity()) {
            switch (options.stashOverflow()) {
                case FAIL ->
                    // Surface the overflow to the handler. If uncaught it
                    // propagates out and dispatch reports the message as
                    // failed; if caught the handler can choose to drop /
                    // process / reroute the current message.
                    throw new IllegalStateException(
                            "stash full (capacity " + options.stashCapacity() + ")");
                case DROP_OLDEST -> {
                    Envelope<T> evicted = stashBuffer.removeFirst();
                    if (evicted.reply != null) {
                        evicted.reply.bindError(new IllegalStateException(
                                "evicted from stash (capacity "
                                        + options.stashCapacity() + " exceeded)"));
                    }
                    // Fall through to add current envelope below.
                }
                case REJECT -> {
                    // Bind the current message's reply immediately so the
                    // caller learns of the rejection now. Mark stashed so
                    // dispatch finalise discards any pending state change
                    // and does not try to bind the reply again, but DON'T
                    // add to the buffer (the message is gone, not deferred).
                    if (currentEnvelope.reply != null) {
                        currentEnvelope.reply.bindError(new IllegalStateException(
                                "stash full (capacity " + options.stashCapacity()
                                        + "); message rejected"));
                    }
                    currentStashed = true;
                    return;
                }
            }
        }
        currentStashed = true;
        stashBuffer.addLast(currentEnvelope);
    }

    private void unstashAllInternal() {
        checkOnWorkerThread("unstashAll()");
        if (stashBuffer.isEmpty()) return;
        // Re-inject at head in original (FIFO) order: iterate the buffer
        // from tail to head and addFirst each, so the oldest stashed
        // message ends up at the front of the queue.
        Iterator<Envelope<T>> it = stashBuffer.descendingIterator();
        while (it.hasNext()) {
            queue.addFirst(it.next());
        }
        stashBuffer.clear();
    }

    private Cancellable scheduleOnceInternal(T message, Duration delay) {
        checkOnWorkerThread("scheduleOnce()");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(delay, "delay must not be null");
        // A handler can run during the draining phase (active=false); refuse
        // to schedule then so a late timer can't fire into a stopped actor.
        if (!active) throw new IllegalStateException("Actor has been stopped");
        // Chicken-and-egg: the task wants to deregister itself from
        // pendingTimers after firing, but the ScheduledFuture doesn't
        // exist until schedule(...) returns. Stash it via AtomicReference
        // and read it inside the task.
        AtomicReference<ScheduledFuture<?>> ref = new AtomicReference<>();
        Runnable task = () -> {
            // Hand the send off to the async executor so the scheduler
            // thread never blocks on a full BLOCK mailbox. The scheduler
            // is a shared resource (one per JVM); blocking it on one
            // actor's bound would starve every other actor's timers.
            AsyncSupport.getExecutor().execute(() -> {
                try {
                    send(message);
                } catch (IllegalStateException ignored) {
                    // Actor was stopped between schedule and fire — drop.
                }
            });
            ScheduledFuture<?> f = ref.get();
            if (f != null) pendingTimers.remove(f);
        };
        ScheduledFuture<?> future = AsyncSupport.getScheduler()
                .schedule(task, delay.toNanos(), TimeUnit.NANOSECONDS);
        // Race window: with a near-zero delay, the task can fire and
        // reach `ref.get()` before this set() runs, observing null and
        // skipping its self-deregistration. The future is then added to
        // pendingTimers below and stays there until stop() cancels it
        // (a no-op on an already-fired future). Functionally benign —
        // just a dangling entry until shutdown — so we don't pay for a
        // compareAndSet here. Documented for future readers.
        ref.set(future);
        return registerTimer(future);
    }

    private Cancellable scheduleAtFixedRateInternal(T message, Duration initialDelay, Duration interval) {
        checkOnWorkerThread("scheduleAtFixedRate()");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(initialDelay, "initialDelay must not be null");
        Objects.requireNonNull(interval, "interval must not be null");
        // See scheduleOnceInternal: never schedule a periodic timer once the
        // actor is stopping, or it would keep firing send()s forever.
        if (!active) throw new IllegalStateException("Actor has been stopped");
        // Periodic timers never self-deregister — they fire repeatedly
        // until cancelled explicitly or via stop(). Each fire offloads
        // the send to the async executor so a BLOCK-bounded actor can
        // never starve the shared scheduler thread.
        Runnable task = () -> AsyncSupport.getExecutor().execute(() -> {
            try {
                send(message);
            } catch (IllegalStateException ignored) {
                // Actor stopped — drop.
            }
        });
        ScheduledFuture<?> future = AsyncSupport.getScheduler()
                .scheduleAtFixedRate(task,
                        initialDelay.toNanos(),
                        interval.toNanos(),
                        TimeUnit.NANOSECONDS);
        return registerTimer(future);
    }

    /**
     * Registers a freshly scheduled timer for cancellation on {@code stop()}.
     * Closes the race where {@code stop()} runs between the {@code active}
     * check and this registration: its cancel sweep may have missed this
     * future, so if we observe {@code !active} after adding, we cancel and
     * deregister it ourselves and reject the schedule.
     */
    private Cancellable registerTimer(ScheduledFuture<?> future) {
        pendingTimers.add(future);
        if (!active) {
            future.cancel(false);
            pendingTimers.remove(future);
            throw new IllegalStateException("Actor has been stopped");
        }
        return new TimerCancellable(future, pendingTimers);
    }

    // ---- Internal types ------------------------------------------------

    private static final class TimerCancellable implements Cancellable {
        private final ScheduledFuture<?> future;
        private final Set<ScheduledFuture<?>> registry;

        TimerCancellable(ScheduledFuture<?> future, Set<ScheduledFuture<?>> registry) {
            this.future = future;
            this.registry = registry;
        }

        @Override
        public boolean cancel() {
            boolean cancelled = future.cancel(false);
            registry.remove(future);
            return cancelled;
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }
    }

    private static final class Envelope<T> {
        final Object message;
        final DataflowVariable<Object> reply;
        // Set true once the send-side capacity permit has been released
        // for this envelope. Guards against a double release when a
        // stashed envelope is unstashed and re-taken — only the first
        // dequeue corresponds to an original send.
        boolean permitReleased;

        Envelope(Object message, DataflowVariable<Object> reply) {
            this.message = message;
            this.reply = reply;
        }
    }

    /**
     * Processes a single message and reports whether the change should
     * be retained or discarded. {@link #process} computes pending output
     * without committing; the dispatch loop then calls exactly one of
     * {@link #commit} or {@link #rollback} based on whether the message
     * was stashed or threw. This indirection is what lets stateful
     * actors roll back a partial state change when a handler stashes
     * after computing its result.
     */
    private interface MessageProcessor<T> {
        Object process(ActorContext<T> ctx, T message);

        void commit();

        void rollback();

        ActorContext<T> makeContext(DefaultActor<T> actor);
    }

    private static final class ReactorProcessor<T, R> implements MessageProcessor<T> {
        // Worker-thread-confined. Written only by swap(), called from
        // ReactorContext.become(), which is gated by checkOnWorkerThread().
        // Read only by process(), called from DefaultActor.dispatch on
        // the worker thread. No cross-thread access exists, so no
        // volatile / synchronisation is needed.
        private ReactorHandler<T, R> handler;

        ReactorProcessor(ReactorHandler<T, R> handler) {
            this.handler = handler;
        }

        @Override
        public Object process(ActorContext<T> ctx, T message) {
            return handler.apply(ctx, message);
        }

        @Override
        public void commit() {
            // Reactors have no per-dispatch state to commit.
        }

        @Override
        public void rollback() {
            // Reactors have no per-dispatch state to roll back.
        }

        @Override
        public ActorContext<T> makeContext(DefaultActor<T> actor) {
            return new ReactorContext<>(actor, this);
        }

        void swap(ReactorHandler<T, R> newHandler) {
            this.handler = newHandler;
        }
    }

    private static final class StatefulProcessor<T, S> implements MessageProcessor<T> {
        // Worker-thread-confined (same reasoning as ReactorProcessor.handler):
        // become() runs checkOnWorkerThread() before swap(), and process()
        // is only ever invoked from the worker's dispatch loop.
        private StatefulHandler<S, T> handler;
        // Committed state — the value the next handler invocation will see.
        private S state;
        // Pending state — the value computed by the current handler;
        // promoted to {@code state} on commit, discarded on rollback.
        private S pending;
        private boolean hasPending;

        StatefulProcessor(S initialState, StatefulHandler<S, T> handler) {
            this.state = initialState;
            this.handler = handler;
        }

        @Override
        public Object process(ActorContext<T> ctx, T message) {
            S next = handler.apply(ctx, state, message);
            pending = next;
            hasPending = true;
            return next;
        }

        @Override
        public void commit() {
            if (hasPending) {
                state = pending;
                pending = null;
                hasPending = false;
            }
        }

        @Override
        public void rollback() {
            pending = null;
            hasPending = false;
        }

        @Override
        public ActorContext<T> makeContext(DefaultActor<T> actor) {
            return new StatefulContext<>(actor, this);
        }

        void swap(StatefulHandler<S, T> newHandler) {
            this.handler = newHandler;
        }
    }

    // ---- ActorContext implementations ---------------------------------

    private static final class ReactorContext<T, R> implements ActorContext<T> {
        private final DefaultActor<T> actor;
        private final ReactorProcessor<T, R> processor;

        ReactorContext(DefaultActor<T> actor, ReactorProcessor<T, R> processor) {
            this.actor = actor;
            this.processor = processor;
        }

        @Override
        public Actor<T> self() {
            return actor;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <R2> void become(ReactorHandler<T, R2> newHandler) {
            Objects.requireNonNull(newHandler, "newHandler must not be null");
            actor.checkOnWorkerThread("become()");
            // The new handler may declare a different reply type than the
            // original; the actor stores no R, so the cast is safe.
            processor.swap((ReactorHandler) newHandler);
        }

        @Override
        public void stash() {
            actor.stashCurrent();
        }

        @Override
        public void unstashAll() {
            actor.unstashAllInternal();
        }

        @Override
        public Cancellable scheduleOnce(T message, Duration delay) {
            return actor.scheduleOnceInternal(message, delay);
        }

        @Override
        public Cancellable scheduleAtFixedRate(T message, Duration initialDelay, Duration interval) {
            return actor.scheduleAtFixedRateInternal(message, initialDelay, interval);
        }
    }

    private static final class StatefulContext<T, S> implements ActorContext<T> {
        private final DefaultActor<T> actor;
        private final StatefulProcessor<T, S> processor;

        StatefulContext(DefaultActor<T> actor, StatefulProcessor<T, S> processor) {
            this.actor = actor;
            this.processor = processor;
        }

        @Override
        public Actor<T> self() {
            return actor;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <S2> void become(StatefulHandler<S2, T> newHandler) {
            Objects.requireNonNull(newHandler, "newHandler must not be null");
            actor.checkOnWorkerThread("become()");
            // Unchecked across S — a wrong-typed handler surfaces as a
            // ClassCastException on first dispatch (when the handler
            // tries to use the existing state).
            processor.swap((StatefulHandler) newHandler);
        }

        @Override
        public void stash() {
            actor.stashCurrent();
        }

        @Override
        public void unstashAll() {
            actor.unstashAllInternal();
        }

        @Override
        public Cancellable scheduleOnce(T message, Duration delay) {
            return actor.scheduleOnceInternal(message, delay);
        }

        @Override
        public Cancellable scheduleAtFixedRate(T message, Duration initialDelay, Duration interval) {
            return actor.scheduleAtFixedRateInternal(message, initialDelay, interval);
        }
    }
}
