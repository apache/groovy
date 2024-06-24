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
package org.apache.groovy.ginq.provider.collection.runtime;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.Tuple;
import groovy.lang.Tuple2;
import groovy.lang.Tuple3;
import groovy.transform.Internal;
import org.apache.groovy.internal.util.Supplier;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.dgmimpl.NumberNumberMinus;
import org.codehaus.groovy.runtime.typehandling.NumberMath;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static groovy.lang.Tuple.tuple;
import static java.lang.Math.sqrt;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
import static org.apache.groovy.ginq.provider.collection.runtime.Queryable.from;
import static org.apache.groovy.ginq.provider.collection.runtime.WindowImpl.composeOrders;
import static org.codehaus.groovy.runtime.typehandling.NumberMath.toBigDecimal;

/**
 * Represents the queryable collections
 *
 * @param <T> the type of Queryable element
 * @since 4.0.0
 */
@Internal
class QueryableCollection<T> implements Queryable<T>, Serializable {

    QueryableCollection(Iterable<T> sourceIterable) {
        this.sourceIterable = sourceIterable;
    }

    QueryableCollection(Stream<T> sourceStream) {
        this.sourceStream = sourceStream;
    }

    public Iterator<T> iterator() {
        readLock.lock();
        try {
            if (null != sourceIterable) {
                return sourceIterable.iterator();
            }

            return sourceStream.iterator();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> innerJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        Stream<Tuple2<T, U>> stream =
                this.stream()
                        .flatMap(p -> {
                            if (queryable instanceof QueryableCollection) {
                                ((QueryableCollection) queryable).makeReusable();
                            }

                            return queryable.stream()
                                    .filter(c -> joiner.test(p, c))
                                    .map(c -> tuple(p, c));
                        });

        return from(stream);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> innerHashJoin(Queryable<? extends U> queryable, Function<? super T, ?> fieldsExtractor1, Function<? super U, ?> fieldsExtractor2) {
        final ConcurrentObjectHolder<Map<Integer, List<Candidate<U>>>> hashTableHolder = new ConcurrentObjectHolder<>(createHashTableSupplier(queryable, fieldsExtractor2));
        if (isParallel()) hashTableHolder.getObject(); // avoid nested parallel querying, which results in deadlock sometimes
        Stream<Tuple2<T, U>> stream = this.stream().flatMap(p -> {
            // build hash table
            Map<Integer, List<Candidate<U>>> hashTable = hashTableHolder.getObject();

            // probe the hash table
            return probeHashTable(hashTable, p, fieldsExtractor1);
        });

        return from(stream);
    }

    private static final class Bucket<E> extends ArrayList<E> {
        private static final long serialVersionUID = 2813676753531316403L;
        Bucket(int initialCapacity) {
            super(initialCapacity);
        }
        static <E> Bucket<E> singletonBucket(E o) {
            Bucket<E> bucket = new Bucket<>(1);
            bucket.add(o);
            return bucket;
        }
    }

    private static final class Candidate<U> {
        private final U original;
        private final Object extracted;

        private Candidate(U original, Object extracted) {
            this.original = original;
            this.extracted = extracted;
        }
    }

    private static <U> Supplier<Map<Integer, List<Candidate<U>>>> createHashTableSupplier(Queryable<? extends U> queryable, Function<? super U, ?> fieldsExtractor2) {
        return () -> {
            Function<Candidate<U>, Integer> keyMapper = c -> hash(c.extracted);
            Function<Candidate<U>, List<Candidate<U>>> valueMapper = Bucket::singletonBucket;
            BinaryOperator<List<Candidate<U>>> mergeFunction = (oldBucket, newBucket) -> {
                oldBucket.addAll(newBucket);
                return oldBucket;
            };
            Collector<Candidate<U>, ?, ? extends Map<Integer, List<Candidate<U>>>> candidateMapCollector =
                    isParallel() ? Collectors.toConcurrentMap(keyMapper, valueMapper, mergeFunction)
                                 : Collectors.toMap(keyMapper, valueMapper, mergeFunction);

            return queryable.stream()
                    .map(e -> new Candidate<U>(e, fieldsExtractor2.apply(e)))
                    .collect(candidateMapCollector);
        };
    }

    private static Integer hash(Object obj) {
        return Objects.hash(obj);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> leftJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        return outerJoin(this, queryable, joiner);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> leftHashJoin(Queryable<? extends U> queryable, Function<? super T, ?> fieldsExtractor1, Function<? super U, ?> fieldsExtractor2) {
        return outerHashJoin(this, queryable, fieldsExtractor1, fieldsExtractor2);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> rightJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        return outerJoin(queryable, this, (a, b) -> joiner.test(b, a)).select((e, q) -> tuple(e.getV2(), e.getV1()));
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> rightHashJoin(Queryable<? extends U> queryable, Function<? super T, ?> fieldsExtractor1, Function<? super U, ?> fieldsExtractor2) {
        return outerHashJoin(queryable, this, fieldsExtractor2, fieldsExtractor1).select((e, q) -> tuple(e.getV2(), e.getV1()));
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> fullJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        if (queryable instanceof QueryableCollection) {
            ((QueryableCollection) queryable).makeReusable();
        }
        this.makeReusable();

        Queryable<Tuple2<T, U>> lj = this.leftJoin(queryable, joiner);
        Queryable<Tuple2<T, U>> rj = this.rightJoin(queryable, joiner);
        return lj.union(rj);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> fullHashJoin(Queryable<? extends U> queryable, Function<? super T, ?> fieldsExtractor1, Function<? super U, ?> fieldsExtractor2) {
        if (queryable instanceof QueryableCollection) {
            ((QueryableCollection) queryable).makeReusable();
        }
        this.makeReusable();

        Queryable<Tuple2<T, U>> lj = this.leftHashJoin(queryable, fieldsExtractor1, fieldsExtractor2);
        Queryable<Tuple2<T, U>> rj = this.rightHashJoin(queryable, fieldsExtractor1, fieldsExtractor2);
        return lj.union(rj);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> crossJoin(Queryable<? extends U> queryable) {
        Stream<Tuple2<T, U>> stream =
                this.stream()
                        .flatMap(p -> {
                            if (queryable instanceof QueryableCollection) {
                                ((QueryableCollection) queryable).makeReusable();
                            }

                            return queryable.stream()
                                    .map(c -> tuple(p, c));
                        });

        return from(stream);
    }

    @Override
    public Queryable<T> where(Predicate<? super T> filter) {
        Stream<T> stream = this.stream().filter(filter);

        return from(stream);
    }

    @Override
    public Queryable<Tuple2<?, Queryable<T>>> groupBy(Function<? super T, ?> classifier, Predicate<? super Tuple2<?, Queryable<? extends T>>> having) {
        Collector<T, ?, ? extends Map<?, List<T>>> groupingBy =
                isParallel() ? Collectors.groupingByConcurrent(classifier, Collectors.toList())
                             : Collectors.groupingBy(classifier, Collectors.toList());

        Stream<Tuple2<?, Queryable<T>>> stream =
                this.stream()
                        .collect(groupingBy)
                        .entrySet().stream()
                        .filter(m -> null == having || having.test(tuple(m.getKey(), from(m.getValue()))))
                        .map(m -> tuple(m.getKey(), from(m.getValue())));

        return Group.of(stream);
    }

    @Override
    public <U extends Comparable<? super U>> Queryable<T> orderBy(Order<? super T, ? extends U>... orders) {
        Comparator<T> comparator = makeComparator(orders);
        if (null == comparator) {
            return this;
        }

        return from(this.stream().sorted(comparator));
    }

    protected static <T, U extends Comparable<? super U>> Comparator<T> makeComparator(List<? extends Order<? super T, ? extends U>> orders) {
        return makeComparator(orders.toArray(Order.EMPTY_ARRAY));
    }

    protected static <T, U extends Comparable<? super U>> Comparator<T> makeComparator(Order<? super T, ? extends U>... orders) {
        if (null == orders || 0 == orders.length) {
            return null;
        }
        Comparator<T> comparator = null;
        for (int i = 0, n = orders.length; i < n; i++) {
            Order<? super T, ? extends U> order = orders[i];
            Comparator<U> ascOrDesc = order.isAsc() ? naturalOrder() : reverseOrder();
            Comparator<U> nullsLastOrFirst = order.isNullsLast() ? nullsLast(ascOrDesc) : nullsFirst(ascOrDesc);
            comparator =
                    0 == i
                            ? Comparator.comparing(order.getKeyExtractor(), nullsLastOrFirst)
                            : comparator.thenComparing(order.getKeyExtractor(), nullsLastOrFirst);
        }
        return comparator;
    }

    @Override
    public Queryable<T> limit(long offset, long size) {
        Stream<T> stream = this.stream().skip(offset).limit(size);

        return from(stream);
    }

    @Override
    public <U> Queryable<U> select(BiFunction<? super T, ? super Queryable<? extends T>, ? extends U> mapper) {
        final String originalParallel = QueryableHelper.getVar(PARALLEL);
        QueryableHelper.setVar(PARALLEL, FALSE_STR); // ensure the row number is generated sequentially
        try {
            boolean useWindowFunction = TRUE_STR.equals(QueryableHelper.getVar(USE_WINDOW_FUNCTION));
            if (useWindowFunction) {
                this.makeReusable();
            }
            Stream<U> stream = null;
            if (this instanceof Group) {
                this.makeReusable();
                if (0 == this.count()) {
                    stream = Stream.of((T) tuple(NULL, EMPTY_QUERYABLE)).map((T t) -> mapper.apply(t, this));
                }
            }
            if (null == stream) {
                stream = this.stream().map((T t) -> mapper.apply(t, this));
            }

            if (TRUE_STR.equals(originalParallel)) {
                // invoke `collect` to trigger the intermediate operator, which will create `CompletableFuture` instances
                stream = stream.collect(Collectors.toList()).parallelStream().map((U u) -> {
                    boolean interrupted = false;
                    try {
                        return (U) ((CompletableFuture) u).get();
                    } catch (InterruptedException | ExecutionException ex) {
                        if (ex instanceof InterruptedException) interrupted = true;
                        throw new GroovyRuntimeException(ex);
                    } finally {
                        if (interrupted) Thread.currentThread().interrupt();
                    }
                });
            }

            return from(stream);
        } finally {
            QueryableHelper.setVar(PARALLEL, originalParallel);
        }
    }

    @Override
    public Queryable<T> distinct() {
        Stream<T> stream = this.stream().distinct();

        return from(stream);
    }

    @Override
    public Queryable<T> unionAll(Queryable<? extends T> queryable) {
        Stream<T> stream = Stream.concat(this.stream(), queryable.stream());

        return from(stream);
    }

    @Override
    public Queryable<T> intersect(Queryable<? extends T> queryable) {
        Stream<T> stream = this.stream().filter(a -> {
            if (queryable instanceof QueryableCollection) {
                ((QueryableCollection) queryable).makeReusable();
            }

            return queryable.stream().anyMatch(b -> b.equals(a));
        }).distinct();

        return from(stream);
    }

    @Override
    public Queryable<T> minus(Queryable<? extends T> queryable) {
        Stream<T> stream = this.stream().filter(a -> {
            if (queryable instanceof QueryableCollection) {
                ((QueryableCollection) queryable).makeReusable();
            }

            return queryable.stream().noneMatch(b -> b.equals(a));
        }).distinct();

        return from(stream);
    }

    // ------------------------------ BEGIN AGGREGATE FUNCTIONS --------------------------------
    @Override
    public Long count() {
        return agg(q -> q.stream().count());
    }

    @Override
    public <U> Long count(Function<? super T, ? extends U> mapper) {
        return agg(q -> q.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .count());
    }

    @Override
    public BigDecimal sum(Function<? super T, ? extends Number> mapper) {
        return agg(q -> this.stream()
                .map(e -> {
                    Number n = mapper.apply(e);
                    if (null == n) return BigDecimal.ZERO;

                    return toBigDecimal(n);
                }).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @Override
    public BigDecimal avg(Function<? super T, ? extends Number> mapper) {
        Object[] result = agg(q -> q.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .map(NumberMath::toBigDecimal)
                .reduce(new Object[] {0L, BigDecimal.ZERO}, (r, e) -> {
                    r[0] = (Long) r[0] + 1;
                    r[1] = ((BigDecimal) r[1]).add(e);
                    return r;
                }, (o1, o2) -> o1)
        );

        return ((BigDecimal) result[1]).divide(toBigDecimal((Long) result[0]), 16, RoundingMode.HALF_UP);
    }

    @Override
    public <U extends Comparable<? super U>> U min(Function<? super T, ? extends U> mapper) {
        return agg(q -> q.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .min(Comparator.comparing(Function.identity()))
                .orElse(null));
    }

    @Override
    public <U extends Comparable<? super U>> U max(Function<? super T, ? extends U> mapper) {
        return agg(q -> q.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .max(Comparator.comparing(Function.identity()))
                .orElse(null));
    }

    @Override
    public <U> List<U> list(Function<? super T, ? extends U> mapper) {
        return agg(q -> q.stream()
            .map(mapper)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
    }

    @Override
    public BigDecimal median(Function<? super T, ? extends Number> mapper) {
        List<BigDecimal> sortedNumList = agg(q -> q.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .map(NumberMath::toBigDecimal)
                .sorted()
                .collect(Collectors.toList())
        );

        int size = sortedNumList.size();
        if (0 == size) {
            return null;
        }

        int index = size / 2;
        BigDecimal num = sortedNumList.get(index);

        if (0 == size % 2) {
            return num.add(sortedNumList.get(index - 1)).divide(BD_TWO);
        }

        return num;
    }

    @Override
    public BigDecimal stdev(Function<? super T, ? extends Number> mapper) {
        return sd(mapper, 0);
    }

    @Override
    public BigDecimal stdevp(Function<? super T, ? extends Number> mapper) {
        return sd(mapper, 1);
    }

    @Override
    public BigDecimal var(Function<? super T, ? extends Number> mapper) {
        return vr(mapper, 0);
    }

    @Override
    public BigDecimal varp(Function<? super T, ? extends Number> mapper) {
        return vr(mapper, 1);
    }

    private BigDecimal vr(Function<? super T, ? extends Number> mapper, int diff) {
        BigDecimal avg = this.avg(mapper);
        Object[] result = agg(q -> q.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .map(e -> toBigDecimal(NumberNumberMinus.minus(e, avg)).pow(2))
                .reduce(new Object[]{0L, BigDecimal.ZERO}, (r, e) -> {
                    r[0] = (Long) r[0] + 1;
                    r[1] = ((BigDecimal) r[1]).add(e);
                    return r;
                }, (o1, o2) -> o1));

        return ((BigDecimal) result[1]).divide(toBigDecimal((Long) result[0] - diff), 16, RoundingMode.HALF_UP);
    }

    private BigDecimal sd(Function<? super T, ? extends Number> mapper, int diff) {
        // `BigDecimal.sqrt` is introduced since Java9, so we can not use it for now.
        return toBigDecimal(sqrt(vr(mapper, diff).doubleValue()));
    }

    @Override
    public <U> U agg(Function<? super Queryable<? extends T>, ? extends U> mapper) {
        return mapper.apply(this);
    }
    // ------------------------------ END AGGREGATE FUNCTIONS --------------------------------

    private static <T, U> Queryable<Tuple2<T, U>> outerJoin(Queryable<? extends T> queryable1, Queryable<? extends U> queryable2, BiPredicate<? super T, ? super U> joiner) {
        Stream<Tuple2<T, U>> stream =
                queryable1.stream()
                        .flatMap(p -> {
                            if (queryable2 instanceof QueryableCollection) {
                                ((QueryableCollection) queryable2).makeReusable();
                            }

                            List<Tuple2<T, U>> joinResultList =
                                    queryable2.stream()
                                            .filter(c -> joiner.test(p, c))
                                            .map(c -> tuple((T) p, (U) c))
                                            .collect(Collectors.toList());

                            return joinResultList.isEmpty() ? Stream.of(tuple(p, null)) : joinResultList.stream();
                        });

        return from(stream);
    }

    private static <T, U> Queryable<Tuple2<T, U>> outerHashJoin(Queryable<? extends T> queryable1, Queryable<? extends U> queryable2, Function<? super T, ?> fieldsExtractor1, Function<? super U, ?> fieldsExtractor2) {
        final ConcurrentObjectHolder<Map<Integer, List<Candidate<U>>>> hashTableHolder = new ConcurrentObjectHolder<>(createHashTableSupplier(queryable2, fieldsExtractor2));
        if (isParallel()) hashTableHolder.getObject(); // avoid nested parallel querying, which results in deadlock sometimes
        Stream<Tuple2<T, U>> stream = queryable1.stream().flatMap(p -> {
            // build hash table
            Map<Integer, List<Candidate<U>>> hashTable = hashTableHolder.getObject();

            // probe the hash table
            List<Tuple2<T, U>> joinResultList =
                    probeHashTable(hashTable, (T) p, fieldsExtractor1).collect(Collectors.toList());

            return joinResultList.isEmpty() ? Stream.of(tuple(p, null)) : joinResultList.stream();
        });

        return from(stream);
    }

    private static <T, U> Stream<Tuple2<T, U>> probeHashTable(Map<Integer, List<Candidate<U>>> hashTable, T p, Function<? super T, ?> fieldsExtractor1) {
        final Object otherFields = fieldsExtractor1.apply(p);
        final Integer h = hash(otherFields);

        List<Candidate<U>> candidateList = hashTable.get(h);
        if (null == candidateList) return Stream.empty();

        Stream<Candidate<U>> stream = candidateList.stream();
        if (isParallel()) stream = stream.parallel();

        return stream.filter(c -> Objects.equals(otherFields, c.extracted))
                     .map(c -> tuple(p, c.original));
    }

    @Override
    public List<T> toList() {
        writeLock.lock();
        try {
            if (sourceIterable instanceof List) {
                return (List<T>) sourceIterable;
            }

            final List<T> result = stream().collect(Collectors.toList());
            sourceIterable = result;

            return result;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public long size() {
        return stream().count();
    }

    @Override
    public Stream<T> stream() {
        writeLock.lock();
        try {
            if (isReusable()) {
                sourceStream = toStream(sourceIterable);  // we have to create new stream every time because Java stream can not be reused
            }

            if (!sourceStream.isParallel() && isParallel()) {
                sourceStream = sourceStream.parallel();
            }

            return sourceStream;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public <U extends Comparable<? super U>> Window<T> over(Tuple2<T, Long> currentRecord, WindowDefinition<T, U> windowDefinition) {
        final Tuple3<String, String, String> idTuple = (Tuple3<String, String, String>) windowDefinition.getId(); // (partitionId, orderId, windowDefinitionId)
        final String partitionId = idTuple.getV1();

        Partition<Tuple2<T, Long>> partition = partitionCache.computeIfAbsent(
                new PartitionCacheKey(windowDefinition.partitionBy().apply(currentRecord.getV1()), partitionId),
                partitionCacheKey -> from(Collections.singletonList(currentRecord)).innerHashJoin(
                        allPartitionCache.computeIfAbsent(partitionId, pid -> {
                            long[] rn = new long[]{0L};
                            List<Tuple2<T, Long>> listWithIndex =
                                    this.toList().stream()
                                            .map(e -> Tuple.tuple(e, rn[0]++))
                                            .collect(Collectors.toList());

                            final Queryable<Tuple2<?, Partition<Tuple2<T, Long>>>> q =
                                    from(listWithIndex)
                                            .groupBy(windowDefinition.partitionBy().compose(Tuple2::getV1))
                                            .select((e, x) -> Tuple.tuple(e.getV1(), Partition.of(e.getV2().toList())));
                            if (q instanceof QueryableCollection) {
                                ((QueryableCollection) q).makeReusable();
                            }
                            return q;
                        }), a -> partitionCacheKey.partitionKey, Tuple2::getV1
                ).select((e, q) -> e.getV2().getV2())
                        .stream()
                        .findFirst()
                        .orElse(Partition.emptyPartition())
        );

        final String orderId = idTuple.getV2();
        final SortedPartitionCacheKey<T> sortedPartitionCacheKey = new SortedPartitionCacheKey<>(partition, orderId);
        Partition<Tuple2<T, Long>> sortedPartition = sortedPartitionCache.computeIfAbsent(
                sortedPartitionCacheKey,
                sortedPartitionId -> Partition.of(partition.orderBy(composeOrders(windowDefinition)).toList())
        );

        return Window.of(currentRecord, sortedPartition, windowDefinition);
    }

    private static class PartitionCacheKey {
        private final Object partitionKey;
        private final String partitionId;

        PartitionCacheKey(Object partitionKey, String partitionId) {
            this.partitionKey = partitionKey;
            this.partitionId = partitionId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PartitionCacheKey)) return false;
            PartitionCacheKey that = (PartitionCacheKey) o;
            return partitionKey.equals(that.partitionKey) && partitionId.equals(that.partitionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(partitionKey, partitionId);
        }
    }

    private static class SortedPartitionCacheKey<T> {
        private final Partition<Tuple2<T, Long>> partition;
        private final String orderId;

        SortedPartitionCacheKey(Partition<Tuple2<T, Long>> partition, String orderId) {
            this.partition = partition;
            this.orderId = orderId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SortedPartitionCacheKey)) return false;
            SortedPartitionCacheKey that = (SortedPartitionCacheKey) o;
            return partition == that.partition && orderId.equals(that.orderId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(partition.size(), orderId);
        }
    }

    private static <T> Stream<T> toStream(Iterable<T> sourceIterable) {
        return StreamSupport.stream(sourceIterable.spliterator(), isParallel());
    }

    private static boolean isParallel() {
        return QueryableHelper.isParallel();
    }

    private boolean isReusable() {
        readLock.lock();
        try {
            return null != sourceIterable;
        } finally {
            readLock.unlock();
        }
    }

    private void makeReusable() {
        if (null != this.sourceIterable) return;

        writeLock.lock();
        try {
            if (null != this.sourceIterable) return;

            this.sourceIterable = this.sourceStream.collect(Collectors.toList());
        } finally {
            writeLock.unlock();
        }
    }

    public Object asType(Class<?> clazz) {
        if (Queryable.class == clazz || QueryableCollection.class == clazz) {
            return this;
        }

        if (List.class == clazz || Collection.class == clazz || Iterable.class == clazz) {
            return toList();
        }
        if (ArrayList.class == clazz) {
            List<T> list = toList();
            if (list instanceof ArrayList) {
                return list;
            }
            return new ArrayList<>(list);
        }
        if (LinkedList.class == clazz || Deque.class == clazz || Queue.class == clazz) {
            List<T> list = toList();
            if (list instanceof LinkedList) {
                return list;
            }
            return new LinkedList<>(list);
        }

        if (clazz.isArray()) {
            return DefaultGroovyMethods.asType(toList(), clazz);
        }

        if (Set.class == clazz || LinkedHashSet.class == clazz) {
            return new LinkedHashSet<>(toList());
        }
        if (HashSet.class == clazz) {
            return new HashSet<>(toList());
        }
        if (TreeSet.class == clazz) {
            return new TreeSet<>(toList());
        }


        if (Stream.class == clazz) {
            return stream();
        }

        if (Iterator.class == clazz) {
            return iterator();
        }

        return DefaultGroovyMethods.asType(this, clazz);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QueryableCollection)) return false;
        QueryableCollection<?> that = (QueryableCollection<?>) o;
        return toList().equals(that.toList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(toList());
    }

    @Override
    public String toString() {
        return AsciiTableMaker.makeAsciiTable(this);
    }

    private final Map<String, Queryable<Tuple2<?, Partition<Tuple2<T, Long>>>>> allPartitionCache = new ConcurrentHashMap<>(4);
    private final Map<PartitionCacheKey, Partition<Tuple2<T, Long>>> partitionCache = new ConcurrentHashMap<>(4);
    private final Map<SortedPartitionCacheKey<T>, Partition<Tuple2<T, Long>>> sortedPartitionCache = new ConcurrentHashMap<>(4);
    private Stream<T> sourceStream;
    private volatile Iterable<T> sourceIterable;
    private final ReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();
    private static final BigDecimal BD_TWO = BigDecimal.valueOf(2);
    private static final String USE_WINDOW_FUNCTION = "useWindowFunction";
    private static final String PARALLEL = "parallel";
    private static final String TRUE_STR = "true";
    private static final String FALSE_STR = "false";
    private static final long serialVersionUID = -5067092453136522893L;
}
