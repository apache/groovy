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
package org.codehaus.groovy.runtime;

import groovy.concurrent.Pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

/**
 * Element-wise parallel operations for a {@link Pool} that is not a
 * fork-join pool.
 * <p>
 * Not a supported API. {@code ParallelCollectionExtensions} calls this
 * when {@link Pool#asForkJoinPool()} does not apply, so {@link Pool#virtual()}
 * and {@link Pool#io()} run on their own executor instead of the common pool.
 * One task is submitted per element: a blocking callback does not need a
 * spare fork-join worker, and a fixed fork-join pool is left at its cap.
 * Short-circuit operations complete as soon as the result is known and
 * interrupt tasks that are already running. Interrupting a thread blocked
 * on an InterruptibleChannel closes that channel. A fork-join pool does
 * not interrupt a running callback.
 * <p>
 * A nested call on the same bounded pool runs on the worker that is
 * already executing the outer task. Waiting for a task queued on that
 * pool would deadlock the worker. An unbounded pool, and a pool that
 * {@linkplain Pool#usesVirtualThreads() uses virtual threads}, still
 * submits one task per element.
 */
final class ParallelExecutorTasks {

    private ParallelExecutorTasks() {
    }

    static <T> void forEach(Pool pool, Collection<T> items, Consumer<? super T> action) {
        List<T> list = randomAccess(items);
        runAll(pool, list.size(), index -> action.accept(list.get(index)));
    }

    static <T> void forEachIndexed(Pool pool, List<T> items, BiConsumer<? super T, Integer> action) {
        List<T> list = randomAccess(items);
        runAll(pool, list.size(), index -> action.accept(list.get(index), index));
    }

    static <T, R> List<R> map(Pool pool, Collection<T> items, Function<? super T, ? extends R> fn) {
        List<T> list = randomAccess(items);
        int size = list.size();
        @SuppressWarnings("unchecked")
        R[] values = (R[]) new Object[size];
        runAll(pool, size, index -> values[index] = fn.apply(list.get(index)));
        List<R> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(values[i]);
        }
        return result;
    }

    static <T> List<T> filter(Pool pool, Collection<T> items, Predicate<? super T> predicate) {
        List<T> list = randomAccess(items);
        int size = list.size();
        boolean[] matches = new boolean[size];
        runAll(pool, size, index -> matches[index] = predicate.test(list.get(index)));
        List<T> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (matches[i]) {
                result.add(list.get(i));
            }
        }
        return result;
    }

    static <T> long count(Pool pool, Collection<T> items, Predicate<? super T> predicate) {
        List<T> list = randomAccess(items);
        AtomicLong matches = new AtomicLong();
        runAll(pool, list.size(), index -> {
            if (predicate.test(list.get(index))) {
                matches.incrementAndGet();
            }
        });
        return matches.get();
    }

    static <T, R> List<R> flatMap(Pool pool, Collection<T> items,
                                   Function<? super T, ? extends Collection<? extends R>> fn) {
        List<T> list = randomAccess(items);
        int size = list.size();
        @SuppressWarnings("unchecked")
        Collection<? extends R>[] parts = new Collection[size];
        runAll(pool, size, index -> parts[index] = fn.apply(list.get(index)));
        List<R> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Collection<? extends R> part = parts[i];
            if (part == null) {
                throw new NullPointerException("transform returned null");
            }
            result.addAll(part);
        }
        return result;
    }

    static <T, K> Map<K, List<T>> groupBy(Pool pool, Collection<T> items, Function<? super T, ? extends K> classifier) {
        List<T> list = randomAccess(items);
        int size = list.size();
        Object[] keys = new Object[size];
        runAll(pool, size, index -> keys[index] = classifier.apply(list.get(index)));
        return groupInOrder(list, keys);
    }

    /** Inserts in index order. Key order is first-seen, including a null key. */
    static <T, K> Map<K, List<T>> groupInOrder(List<T> elements, Object[] keys) {
        Map<K, List<T>> grouped = new LinkedHashMap<>();
        for (int i = 0; i < elements.size(); i++) {
            @SuppressWarnings("unchecked")
            K key = (K) keys[i];
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(elements.get(i));
        }
        return grouped;
    }

    static <T> List<List<T>> partition(Pool pool, Collection<T> items, Predicate<? super T> predicate) {
        List<T> list = randomAccess(items);
        int size = list.size();
        boolean[] matches = new boolean[size];
        runAll(pool, size, index -> matches[index] = predicate.test(list.get(index)));
        List<T> matching = new ArrayList<>();
        List<T> others = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            (matches[i] ? matching : others).add(list.get(i));
        }
        return List.of(matching, others);
    }

    static <T> T findFirst(Pool pool, Collection<T> items, Predicate<? super T> predicate) {
        List<T> list = randomAccess(items);
        if (list.isEmpty()) {
            return null;
        }
        return new FindFirst<>(pool, list, predicate).invoke();
    }

    static <T> T findAny(Pool pool, Collection<T> items, Predicate<? super T> predicate) {
        List<T> list = randomAccess(items);
        int size = list.size();
        if (size == 0) {
            return null;
        }
        Batch batch = new Batch(pool, size);
        Settle<Integer> settle = new Settle<>();
        AtomicInteger remaining = new AtomicInteger(size);
        batch.start(index -> {
            if (predicate.test(list.get(index)) && settle.succeed(index)) {
                // Interrupt after the win is published. Doing it under the
                // settle lock can stall a worker that needs that lock.
                batch.abandonOthers(index);
            }
        }, (index, error) -> finishAny(batch, settle, remaining, error));
        int found = batch.await(settle.future);
        return found < 0 ? null : list.get(found);
    }

    static <T> boolean anyMatch(Pool pool, Collection<T> items, Predicate<? super T> predicate) {
        List<T> list = randomAccess(items);
        int size = list.size();
        if (size == 0) {
            return false;
        }
        Batch batch = new Batch(pool, size);
        Settle<Boolean> settle = new Settle<>();
        AtomicInteger remaining = new AtomicInteger(size);
        batch.start(index -> {
            // Publish under the same lock that fail() uses, before this
            // task returns, so a sibling handler cannot fail from a stale read.
            if (predicate.test(list.get(index)) && settle.succeed(Boolean.TRUE)) {
                batch.abandonOthers(index);
            }
        }, (index, error) -> {
            if (error != null && settle.fail(error)) {
                batch.abandonAll();
            }
            if (remaining.decrementAndGet() == 0) {
                settle.succeed(Boolean.FALSE);
            }
        });
        return batch.await(settle.future);
    }

    static <T> boolean allMatch(Pool pool, Collection<T> items, Predicate<? super T> predicate) {
        List<T> list = randomAccess(items);
        int size = list.size();
        if (size == 0) {
            return true;
        }
        Batch batch = new Batch(pool, size);
        Settle<Boolean> settle = new Settle<>();
        AtomicInteger remaining = new AtomicInteger(size);
        batch.start(index -> {
            if (!predicate.test(list.get(index)) && settle.succeed(Boolean.FALSE)) {
                batch.abandonOthers(index);
            }
        }, (index, error) -> {
            if (error != null && settle.fail(error)) {
                batch.abandonAll();
            }
            if (remaining.decrementAndGet() == 0) {
                settle.succeed(Boolean.TRUE);
            }
        });
        return batch.await(settle.future);
    }

    static <T> T min(Pool pool, Collection<T> items, Comparator<? super T> comparator) {
        return extreme(pool, items, comparator, true);
    }

    static <T> T max(Pool pool, Collection<T> items, Comparator<? super T> comparator) {
        return extreme(pool, items, comparator, false);
    }

    static <T> T reduce(Pool pool, Collection<T> items, BinaryOperator<T> operator) {
        List<T> list = randomAccess(items);
        int size = list.size();
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return list.get(0);
        }
        return parallelRange(pool, list, operator, (values, from, to) -> {
            T acc = values.get(from);
            for (int i = from + 1; i < to; i++) {
                acc = operator.apply(acc, values.get(i));
            }
            return acc;
        });
    }

    /**
     * Parallel {@code reduce(identity, accumulator)}. {@code seed} is the
     * identity of {@code operator}; it is applied once per chunk and the
     * chunk results are combined with the same operator.
     */
    static <T> T inject(Pool pool, Collection<T> items, T seed, BinaryOperator<T> operator) {
        List<T> list = randomAccess(items);
        if (list.isEmpty()) {
            return seed;
        }
        return parallelRange(pool, list, operator, (values, from, to) -> {
            T acc = seed;
            for (int i = from; i < to; i++) {
                acc = operator.apply(acc, values.get(i));
            }
            return acc;
        });
    }

    private static <T> T extreme(Pool pool, Collection<T> items, Comparator<? super T> comparator, boolean min) {
        List<T> list = randomAccess(items);
        int size = list.size();
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return list.get(0);
        }
        return parallelRange(pool, list, (left, right) -> pick(comparator, left, right, min), (values, from, to) -> {
            T acc = values.get(from);
            for (int i = from + 1; i < to; i++) {
                acc = pick(comparator, acc, values.get(i), min);
            }
            return acc;
        });
    }

    /**
     * {@code compare == 0} keeps {@code left}, so an encounter-order fold returns the earliest tie.
     * A maximum skips nulls and does not call the comparator for them, matching sequential
     * {@code max(Comparator)}. A minimum passes nulls through, matching sequential {@code min(Comparator)}.
     */
    static <T> T pick(Comparator<? super T> comparator, T left, T right, boolean min) {
        if (!min) {
            if (left == null) {
                return right;
            }
            if (right == null) {
                return left;
            }
        }
        int order = comparator.compare(left, right);
        if (order == 0) {
            return left;
        }
        if (min) {
            return order < 0 ? left : right;
        }
        return order > 0 ? left : right;
    }

    private static <T> T parallelRange(Pool pool, List<T> list, BinaryOperator<T> combine, RangeFn<T> range) {
        int size = list.size();
        int chunks = chunkCount(size);
        int chunkSize = (size + chunks - 1) / chunks;
        int count = (size + chunkSize - 1) / chunkSize;
        @SuppressWarnings("unchecked")
        T[] parts = (T[]) new Object[count];
        runAll(pool, count, index -> {
            int from = index * chunkSize;
            int to = Math.min(size, from + chunkSize);
            parts[index] = range.apply(list, from, to);
        });
        T acc = parts[0];
        for (int i = 1; i < count; i++) {
            acc = combine.apply(acc, parts[i]);
        }
        return acc;
    }

    /** At least two chunks when there is something to split, and never more tasks than elements. */
    private static int chunkCount(int size) {
        if (size < 2) {
            return 1;
        }
        int processors = Runtime.getRuntime().availableProcessors();
        return Math.min(size, Math.max(2, processors));
    }

    private static void finishAny(Batch batch, Settle<Integer> settle,
                                   AtomicInteger remaining, Throwable error) {
        if (error != null && settle.fail(error)) {
            batch.abandonAll();
        }
        if (remaining.decrementAndGet() == 0) {
            // A recorded winner already completed the future with its index.
            settle.succeed(-1);
        }
    }

    private static void runAll(Pool pool, int size, IntConsumer body) {
        if (size == 0) {
            return;
        }
        Batch batch = new Batch(pool, size);
        Settle<Void> settle = new Settle<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicInteger remaining = new AtomicInteger(size);
        batch.start(body, (index, error) -> {
            // Publish the failure before counting this task as finished so the
            // task that reaches zero cannot complete successfully ahead of it.
            if (error != null && failure.compareAndSet(null, error) && settle.fail(error)) {
                batch.abandonAll();
            }
            if (remaining.decrementAndGet() == 0) {
                Throwable thrown = failure.get();
                if (thrown != null) {
                    settle.fail(thrown);
                } else {
                    settle.succeed(null);
                }
            }
        });
        batch.await(settle.future);
    }

    /** A random-access snapshot. A list that already is one is returned as-is. */
    static <T> List<T> randomAccess(Collection<T> items) {
        if (items instanceof List && items instanceof RandomAccess) {
            return (List<T>) items;
        }
        return new ArrayList<>(items);
    }

    private static Throwable unwrap(Throwable thrown) {
        Throwable current = thrown;
        while (current instanceof CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    /**
     * Throws {@code thrown} as an unchecked exception and never returns.
     * The type parameter lets callers write {@code return sneakyThrow(ex)}
     * so the compiler accepts the throw on every branch.
     */
    @SuppressWarnings("unchecked")
    private static <T, E extends Throwable> T sneakyThrow(Throwable thrown) throws E {
        throw (E) thrown;
    }

    @FunctionalInterface
    private interface RangeFn<T> {
        T apply(List<T> list, int from, int to);
    }

    @FunctionalInterface
    private interface TaskEnd {
        void finished(int index, Throwable error);
    }

    /**
     * Completes at most once. {@code succeed} and {@code fail} share one lock
     * so a late success cannot overwrite a failure, or the reverse. A
     * short-circuit win is published with {@code succeed} from the task
     * body, before that task returns.
     */
    private static final class Settle<T> {
        private final CompletableFuture<T> future = new CompletableFuture<>();
        private final Object lock = new Object();

        private boolean succeed(T value) {
            synchronized (lock) {
                if (future.isDone()) {
                    return false;
                }
                return future.complete(value);
            }
        }

        private boolean fail(Throwable error) {
            synchronized (lock) {
                if (future.isDone()) {
                    return false;
                }
                return future.completeExceptionally(error);
            }
        }
    }

    /**
     * Submits one task per index and can abandon tasks that the result no
     * longer depends on. Abandoning interrupts a running worker and makes
     * that worker return before calling the user callback when it has not
     * started the callback yet.
     */
    private static final class Batch {
        /** How many tasks on this thread are already inside {@link #run}. */
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

        private final Pool pool;
        private final Thread caller;
        private final int size;
        private final AtomicReferenceArray<Thread> workers;
        /** Pairs a worker slot with the interrupt that cancels it. */
        private final Object slotLock = new Object();
        private final AtomicBoolean stop = new AtomicBoolean();
        private final AtomicInteger keep = new AtomicInteger(-1);
        private final AtomicInteger cutoff = new AtomicInteger(Integer.MAX_VALUE);

        private Batch(Pool pool, int size) {
            this.pool = pool;
            this.caller = Thread.currentThread();
            this.size = size;
            this.workers = new AtomicReferenceArray<>(size);
        }

        private void start(IntConsumer body, TaskEnd end) {
            if (inlineNested()) {
                runSequentially(body, end);
                return;
            }
            for (int i = 0; i < size; i++) {
                int index = i;
                CompletableFuture<Void> future;
                try {
                    future = CompletableFuture.runAsync(() -> run(index, body), pool);
                } catch (Exception rejected) {
                    // The executor refused the task. Settle this index and
                    // every index not yet submitted so the caller cannot hang.
                    // Errors propagate: they are not turned into a failed result.
                    end.finished(index, unwrap(rejected));
                    for (int rest = index + 1; rest < size; rest++) {
                        end.finished(rest, null);
                    }
                    abandonAll();
                    return;
                }
                future.whenComplete((ignored, thrown) -> end.finished(index, normalize(index, thrown)));
            }
        }

        /**
         * The worker running this pool cannot wait for a nested task queued
         * behind it. Virtual threads and unbounded executors can take that task.
         */
        private boolean inlineNested() {
            if (DEPTH.get() == 0 || Pool.current() != pool) {
                return false;
            }
            if (pool.usesVirtualThreads()) {
                return false;
            }
            return pool.getPoolSize() != Integer.MAX_VALUE;
        }

        private void runSequentially(IntConsumer body, TaskEnd end) {
            for (int index = 0; index < size; index++) {
                if (abandoned(index)) {
                    end.finished(index, null);
                    continue;
                }
                Throwable error = null;
                try {
                    body.accept(index);
                } catch (Throwable thrown) {
                    // runAsync captures Errors as the task result. Recording
                    // the same way lets a published match beat this failure,
                    // and lets this failure win when nothing was published.
                    error = abandoned(index) ? null : thrown;
                }
                end.finished(index, error);
            }
        }

        private void run(int index, IntConsumer body) {
            boolean callerThread = Thread.currentThread() == caller;
            // A reused pool thread must not observe a cancellation interrupt
            // left by the previous task. A synchronous executor runs this
            // method on the caller, and that interrupt belongs to the caller.
            if (!callerThread) {
                Thread.interrupted();
            }
            int depth = DEPTH.get();
            DEPTH.set(depth + 1);
            try {
                if (!begin(index, callerThread)) {
                    return;
                }
                try {
                    // Workers created for this pool already carry it. Binding
                    // again on every element is the cost a light map cannot pay.
                    // A foreign executor has no carrier, so bind for that task.
                    if (Pool.current() == pool) {
                        body.accept(index);
                    } else {
                        Pool.withCurrent(pool, () -> {
                            body.accept(index);
                            return null;
                        });
                    }
                } finally {
                    end(index, callerThread);
                }
            } finally {
                if (depth == 0) {
                    DEPTH.remove();
                } else {
                    DEPTH.set(depth);
                }
            }
        }

        /** @return false when the callback must not run */
        private boolean begin(int index, boolean callerThread) {
            synchronized (slotLock) {
                workers.set(index, Thread.currentThread());
                if (!abandoned(index)) {
                    return true;
                }
                workers.set(index, null);
                if (!callerThread) {
                    Thread.interrupted();
                }
                return false;
            }
        }

        private void end(int index, boolean callerThread) {
            synchronized (slotLock) {
                workers.set(index, null);
                // Clear the cancellation only after the slot is empty, and
                // while the canceller cannot still be interrupting this task.
                if (abandoned(index) && !callerThread) {
                    Thread.interrupted();
                }
            }
        }

        private Throwable normalize(int index, Throwable thrown) {
            if (thrown == null || abandoned(index)) {
                return null;
            }
            return unwrap(thrown);
        }

        private boolean abandoned(int index) {
            if (index >= cutoff.get()) {
                return true;
            }
            return stop.get() && index != keep.get();
        }

        /** Indexes {@code >= index} are no longer required. */
        private void abandonFrom(int index) {
            cutoff.accumulateAndGet(index, Math::min);
            interruptAbandoned();
        }

        /** Every index other than {@code keepIndex} is no longer required. */
        private void abandonOthers(int keepIndex) {
            keep.set(keepIndex);
            stop.set(true);
            interruptAbandoned();
        }

        private void abandonAll() {
            keep.set(-2);
            stop.set(true);
            cutoff.set(0);
            interruptAbandoned();
        }

        private void interruptAbandoned() {
            // The slot and the interrupt move together. Reading the slot
            // and interrupting later lets the interrupt land on the next
            // task that reused the thread.
            synchronized (slotLock) {
                for (int i = 0; i < size; i++) {
                    if (!abandoned(i)) {
                        continue;
                    }
                    Thread worker = workers.get(i);
                    if (worker != null) {
                        worker.interrupt();
                    }
                }
            }
        }

        private <T> T await(CompletableFuture<T> decision) {
            try {
                return decision.get();
            } catch (InterruptedException ex) {
                abandonAll();
                Thread.currentThread().interrupt();
                return sneakyThrow(ex);
            } catch (ExecutionException ex) {
                // get() does not throw InterruptedException once the future
                // has already completed, so a caller interrupted in that
                // window still has the flag set. Prefer that interrupt.
                if (Thread.interrupted()) {
                    abandonAll();
                    Thread.currentThread().interrupt();
                    return sneakyThrow(new InterruptedException());
                }
                Throwable cause = ex.getCause();
                return sneakyThrow(cause == null ? ex : unwrap(cause));
            }
        }
    }

    /** Encounter-order find. Later matches do not beat an earlier element, and a tail past the winner is abandoned. */
    private static final class FindFirst<T> {
        private final Batch batch;
        private final List<T> list;
        private final Predicate<? super T> predicate;
        private final Settle<Integer> settle = new Settle<>();
        private final AtomicInteger winner;
        private final Throwable[] errors;
        private final boolean[] done;
        private final Object lock = new Object();
        private int frontier;

        private FindFirst(Pool pool, List<T> list, Predicate<? super T> predicate) {
            this.list = list;
            this.predicate = predicate;
            this.winner = new AtomicInteger(list.size());
            this.errors = new Throwable[list.size()];
            this.done = new boolean[list.size()];
            this.batch = new Batch(pool, list.size());
        }

        private T invoke() {
            batch.start(this::search, this::finished);
            int found = batch.await(settle.future);
            return found < 0 ? null : list.get(found);
        }

        private void search(int index) {
            if (!predicate.test(list.get(index))) {
                return;
            }
            int previous;
            do {
                previous = winner.get();
                if (index >= previous) {
                    return;
                }
            } while (!winner.compareAndSet(previous, index));
            batch.abandonFrom(index + 1);
        }

        private void finished(int index, Throwable error) {
            int abandonAt;
            synchronized (lock) {
                if (error != null) {
                    errors[index] = error;
                }
                done[index] = true;
                abandonAt = advance();
            }
            // Interrupt outside the lock. A worker blocked in finished()
            // needs this lock, and interrupt does not wait for it.
            if (abandonAt >= 0) {
                batch.abandonFrom(abandonAt);
            }
        }

        /** @return the first index that is no longer required, or {@code -1} */
        private int advance() {
            int size = list.size();
            while (frontier < size && done[frontier]) {
                int current = winner.get();
                if (current < size && frontier > current) {
                    break;
                }
                Throwable error = errors[frontier];
                if (error != null && (current >= size || frontier < current)) {
                    if (settle.fail(error)) {
                        return frontier + 1;
                    }
                    return -1;
                }
                frontier++;
            }
            int current = winner.get();
            int need = current >= size ? size : current + 1;
            if (frontier >= need) {
                settle.succeed(current >= size ? -1 : current);
            }
            return -1;
        }
    }
}
