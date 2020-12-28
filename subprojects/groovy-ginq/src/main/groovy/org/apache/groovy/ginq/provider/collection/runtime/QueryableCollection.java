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

import groovy.lang.Tuple;
import groovy.lang.Tuple2;
import groovy.transform.Internal;
import org.apache.groovy.internal.util.Supplier;
import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.typehandling.NumberMath;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static groovy.lang.Tuple.tuple;
import static org.apache.groovy.ginq.provider.collection.runtime.Queryable.from;

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

    protected List<Tuple2<T, Long>> listWithIndex;
    QueryableCollection(Queryable<Tuple2<T, Long>> queryableWithIndex) {
        this(queryableWithIndex.toList().stream().map(Tuple2::getV1).collect(Collectors.toList()));
        this.listWithIndex = queryableWithIndex.toList();
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
        final ConcurrentObjectHolder<Map<Integer, List<U>>> hashTableHolder = new ConcurrentObjectHolder<>();
        final Supplier<Map<Integer, List<U>>> hashTableSupplier = createHashTableSupplier(queryable, fieldsExtractor2);
        Stream<Tuple2<T, U>> stream = this.stream().flatMap(p -> {
            // build hash table
            Map<Integer, List<U>> hashTable = buildHashTable(hashTableHolder, hashTableSupplier);

            // probe the hash table
            return probeHashTable(hashTable, p, fieldsExtractor1, fieldsExtractor2);
        });

        return from(stream);
    }

    private static <U> Supplier<Map<Integer, List<U>>> createHashTableSupplier(Queryable<? extends U> queryable, Function<? super U, ?> fieldsExtractor2) {
        return () -> queryable.stream()
                .collect(
                        Collectors.toMap(
                                c -> hash(fieldsExtractor2.apply(c)),
                                Collections::singletonList,
                                (oldList, newList) -> {
                                    if (!(oldList instanceof ArrayList)) {
                                        List<U> tmpList = new ArrayList<>(HASHTABLE_BUCKET_INITIAL_SIZE);
                                        tmpList.addAll(oldList);
                                        oldList = tmpList;
                                    }

                                    oldList.addAll(newList);
                                    return oldList;
                                }
                        ));
    }

    private static final int HASHTABLE_MAX_SIZE = SystemUtil.getIntegerSafe("groovy.ginq.hashtable.max.size", 128);
    private static final int HASHTABLE_BUCKET_INITIAL_SIZE = SystemUtil.getIntegerSafe("groovy.ginq.hashtable.bucket.initial.size", 16);
    private static Integer hash(Object obj) {
        return Objects.hash(obj) % HASHTABLE_MAX_SIZE; // mod `HASHTABLE_MAX_SIZE` to limit the size of hash table
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
        Stream<Tuple2<?, Queryable<T>>> stream =
                this.stream()
                        .collect(Collectors.groupingBy(classifier, Collectors.toList()))
                        .entrySet().stream()
                        .filter(m -> null == having || having.test(tuple(m.getKey(), from(m.getValue()))))
                        .map(m -> tuple(m.getKey(), from(m.getValue())));

        return from(stream);
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
            Comparator<U> ascOrDesc = order.isAsc() ? Comparator.naturalOrder() : Comparator.reverseOrder();
            comparator =
                    0 == i
                            ? Comparator.comparing(order.getKeyExtractor(), ascOrDesc)
                            : comparator.thenComparing(order.getKeyExtractor(), ascOrDesc);
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
        if (TRUE_STR.equals(QueryableHelper.getVar(USE_WINDOW_FUNCTION))) {
            this.makeReusable();
        }

        Stream<U> stream = this.stream().map((T t) -> mapper.apply(t, this));

        return from(stream);
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

                    return NumberMath.toBigDecimal(n);
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

        return ((BigDecimal) result[1]).divide(BigDecimal.valueOf((Long) result[0]), 16, RoundingMode.HALF_UP);
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
        final ConcurrentObjectHolder<Map<Integer, List<U>>> hashTableHolder = new ConcurrentObjectHolder<>();
        final Supplier<Map<Integer, List<U>>> hashTableSupplier = createHashTableSupplier(queryable2, fieldsExtractor2);
        Stream<Tuple2<T, U>> stream = queryable1.stream().flatMap(p -> {
            // build hash table
            Map<Integer, List<U>> hashTable = buildHashTable(hashTableHolder, hashTableSupplier);

            // probe the hash table
            List<Tuple2<T, U>> joinResultList =
                    probeHashTable(hashTable, (T) p, fieldsExtractor1, fieldsExtractor2).collect(Collectors.toList());

            return joinResultList.isEmpty() ? Stream.of(tuple(p, null)) : joinResultList.stream();
        });

        return from(stream);
    }

    private static <U> Map<Integer, List<U>> buildHashTable(final ConcurrentObjectHolder<Map<Integer, List<U>>> hashTableHolder, final Supplier<Map<Integer, List<U>>> hashTableSupplier) {
        return hashTableHolder.getObject(hashTableSupplier);
    }

    private static <T, U> Stream<Tuple2<T, U>> probeHashTable(Map<Integer, List<U>> hashTable, T p, Function<? super T, ?> fieldsExtractor1, Function<? super U, ?> fieldsExtractor2) {
        final Object otherFields = fieldsExtractor1.apply(p);
        return hashTable.entrySet().stream()
                .filter(entry -> hash(otherFields).equals(entry.getKey()))
                .flatMap(entry -> {
                    List<U> candidateList = entry.getValue();
                    return candidateList.stream()
                            .filter(c -> Objects.equals(otherFields, fieldsExtractor2.apply(c)))
                            .map(c -> tuple(p, c));
                });
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

            if (!sourceStream.isParallel() && TRUE_STR.equals(QueryableHelper.getVar(PARALLEL))) {
                sourceStream = sourceStream.parallel();
            }

            return sourceStream;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public <U extends Comparable<? super U>> Window<T> over(Tuple2<T, Long> currentRecord, WindowDefinition<T, U> windowDefinition) {
        this.makeReusable();
        Queryable<Tuple2<T, Long>> partition =
                partitionCache.computeIfAbsent(windowDefinition, wd -> {
                    long[] rn = new long[] { 1L };
                    List<Tuple2<T, Long>> listWithIndex =
                            this.toList().stream()
                                    .map(e -> Tuple.tuple(e, rn[0]++))
                                    .collect(Collectors.toList());

                    final Queryable<Tuple2<?, Queryable<Tuple2<T, Long>>>> q = from(listWithIndex).groupBy(wd.partitionBy().compose(Tuple2::getV1));
                    if (q instanceof QueryableCollection) {
                        ((QueryableCollection) q).makeReusable();
                    }
                    return q;
                })
                        .where(e -> Objects.equals(e.getV1(), windowDefinition.partitionBy().apply(currentRecord.getV1())))
                        .select((e, q) -> e.getV2())
                        .stream()
                        .findFirst()
                        .orElse(Queryable.emptyQueryable());

        return new WindowImpl<>(currentRecord, partition, windowDefinition);
    }

    private static <T> Stream<T> toStream(Iterable<T> sourceIterable) {
        return StreamSupport.stream(sourceIterable.spliterator(), false);
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
        if (List.class == clazz || Collection.class == clazz || Iterable.class == clazz) {
            return toList();
        }

        if (clazz.isArray()) {
            return DefaultGroovyMethods.asType(toList(), clazz);
        }

        if (Set.class == clazz) {
            return new LinkedHashSet<>(toList());
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

    private final Map<WindowDefinition<T, ?>, Queryable<Tuple2<?, Queryable<Tuple2<T, Long>>>>> partitionCache = new ConcurrentHashMap<>(4);
    private Stream<T> sourceStream;
    private volatile Iterable<T> sourceIterable;
    private final ReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();
    private static final BigDecimal BD_TWO = BigDecimal.valueOf(2);
    private static final String USE_WINDOW_FUNCTION = "useWindowFunction";
    private static final String PARALLEL = "parallel";
    private static final String TRUE_STR = "true";
    private static final long serialVersionUID = -5067092453136522893L;
}
