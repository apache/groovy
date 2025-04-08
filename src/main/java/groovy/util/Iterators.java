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
package groovy.util;

import org.codehaus.groovy.runtime.ArrayGroovyMethods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Provides some iterator based generators.
 */
public class Iterators {

    /**
     * An iterator providing infinite elements using a seed and a UnaryOperator to produce the next element
     * from the previous one. Since the iterator produces infinite elements, you should have some means
     * to stop requesting elements external to this iterator.
     *
     * <pre class="groovyTestCase">
     * assert Iterators.iterate(0, n -> n + 2).take(5).toList() == [0, 2, 4, 6, 8]
     * assert Iterators.iterate('a', String::next).take(6).join() == 'abcdef'
     * </pre>
     *
     * @param seed the first element
     * @param advance an operator returning the next element given the current (previous) one.
     * @see java.util.stream.Stream#iterate(Object, UnaryOperator)
     * @return the iterator of produced elements
     */
    public static <T> Iterator<T> iterate(final T seed, final UnaryOperator<T> advance) {
        Objects.requireNonNull(advance);
        return new IterateIterator<>(seed, advance);
    }

    private static final class IterateIterator<T> implements Iterator<T> {
        private final UnaryOperator<T> advance;
        private T next;
        private boolean first; // lazy calculation of next except when we have the seed

        private IterateIterator(final T seed, final UnaryOperator<T> advance) {
            this.next = seed;
            this.first = true;
            this.advance = advance;
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public T next() {
            if (first) {
                first = false;
            } else {
                next = advance.apply(next);
            }
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * An iterator providing infinite elements using a Supplier.
     * Since the iterator produces infinite elements, you should have some means
     * to stop requesting elements external to this iterator.
     *
     * <pre class="groovyTestCase">
     * import java.util.function.Supplier
     * var r = new Random()
     * assert Iterators.generate({ r.nextInt(10) } as Supplier).take(3).collect() ==~ /\[\d, \d, \d\]/
     * </pre>
     *
     * @param next the supplier of elements
     * @see java.util.stream.Stream#generate(Supplier)
     * @return the iterator of generated values
     */
    public static <T> Iterator<T> generate(Supplier<? extends T> next) {
        Objects.requireNonNull(next);
        return new GenerateIterator<>(next);
    }

    private static final class GenerateIterator<T> implements Iterator<T> {
        private final Supplier<? extends T> next;

        private GenerateIterator(final Supplier<? extends T> next) {
            this.next = next;
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public T next() {
            return next.get();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * An iterator returning a map for each combination of elements the iterables sources.
     *
     * <pre class="groovyTestCase">
     * assert Iterators.combine(x: 1..2, y: 'a'..'c').collect().toString()
     *     == '[[x:1, y:a], [x:1, y:b], [x:1, y:c], [x:2, y:a], [x:2, y:b], [x:2, y:c]]'
     * assert Iterators.combine(x: 1..3, y: 'a'..'b').collect().toString()
     *     == '[[x:1, y:a], [x:1, y:b], [x:2, y:a], [x:2, y:b], [x:3, y:a], [x:3, y:b]]'
     * </pre>
     *
     * @param map the source iterables
     * @return the output iterator
     */
    public static <K, T> Iterator<Map<K, T>> combine(Map<K, ? extends Iterable<T>> map) {
        return new CrossProductIterator<>(map);
    }

    /**
     * An iterator returning a map for each combination of elements the iterator sources.
     * If one of the iterators is infinite, the produced elements will be infinite
     * and will need to be appropriately handled.
     * By necessity, elements from the iterators are cached, so you should
     * use {@link #combine(Map)} if you have existing iterables as this will be more efficient.
     *
     * <pre class="groovyTestCase">
     * assert Iterators.combineLazy(x: (1..2).iterator(), y: ('a'..'c').iterator()).collect().toString()
     *     == '[[x:1, y:a], [x:1, y:b], [x:1, y:c], [x:2, y:a], [x:2, y:b], [x:2, y:c]]'
     * assert Iterators.combineLazy(x: (1..3).iterator(), y: ('a'..'b').iterator()).collect().toString()
     *     == '[[x:1, y:a], [x:1, y:b], [x:2, y:a], [x:2, y:b], [x:3, y:a], [x:3, y:b]]'
     * </pre>
     *
     * @param map the source iterators
     * @return the output iterator
     */
    public static <K, T> Iterator<Map<K, T>> combineLazy(Map<K, ? extends Iterator<T>> map) {
        return combineLazy(map, false);
    }

    /**
     * An iterator returning a map for each combination of elements the iterator sources.
     * If one of the iterators is infinite, the produced elements will be infinite
     * and will need to be appropriately handled.
     * By necessity, elements from the iterators are cached, so you should
     * use {@link #combine(Map)} if you have existing iterables as this will be more efficient.
     *
     * <pre class="groovyTestCase">
     * assert Iterators.combineLazy(
     *     even: Iterators.iterate(0, n {@code ->} n + 2),
     *     odd: Iterators.iterate(1, n {@code ->} n + 2), false)
     *     .take(10).collect().join('\n')
     *     == '''
     * [even:0, odd:1]
     * [even:0, odd:3]
     * [even:0, odd:5]
     * [even:0, odd:7]
     * [even:0, odd:9]
     * [even:0, odd:11]
     * [even:0, odd:13]
     * [even:0, odd:15]
     * [even:0, odd:17]
     * [even:0, odd:19]
     * '''.trim()
     *
     * assert Iterators.combineLazy(
     *     even: Iterators.iterate(0, n {@code ->} n + 2),
     *     odd: Iterators.iterate(1, n {@code ->} n + 2), true)
     *     .take(10).collect().join('\n')
     *     == '''
     * [even:0, odd:1]
     * [even:0, odd:3]
     * [even:2, odd:1]
     * [even:2, odd:3]
     * [even:0, odd:5]
     * [even:2, odd:5]
     * [even:4, odd:1]
     * [even:4, odd:3]
     * [even:4, odd:5]
     * [even:0, odd:7]
     * '''.trim()
     * </pre>
     *
     * @param map the source iterators
     * @param fairOrdering determine the ordering in which combinations are processed, fair implies that all source iterators will be visited roughly equally (until exhausted)
     * @return the output iterator
     */
    public static <K, T> Iterator<Map<K, T>> combineLazy(Map<K, ? extends Iterator<T>> map, boolean fairOrdering) {
        if (fairOrdering) {
            return new CrossByIndexIterator<>(map);
        } else {
            return new CrossProductIterator<>(false, map);
        }
    }

    private static class CrossProductIterator<K, T> implements Iterator<Map<K, T>> {
        private final Map<K, Iterable<T>> iterables;
        private final Map<K, Collection<T>> cache;
        private final Map<K, Iterator<T>> iterators = new LinkedHashMap<>();
        private final List<K> keys = new ArrayList<>();
        private Map<K, T> current;
        private boolean loaded;
        private boolean exhausted;
        private boolean usingIterables;
        private final Map<K, Boolean> usingCache = new LinkedHashMap<>();

        private CrossProductIterator(Map<K, Iterable<T>> iterables, Map<K, Collection<T>> cache, int numItems) {
            this.iterables = iterables;
            this.cache = cache;
        }

        private CrossProductIterator(boolean usingIterables, Map<K, ? extends Iterator<T>> iterators) {
            this(null, new LinkedHashMap<>(), iterators.size());
            this.usingIterables = usingIterables;
            for (Map.Entry<K, ? extends Iterator<T>> entry : iterators.entrySet()) {
                K key = entry.getKey();
                keys.add(key);
                this.iterators.put(key, entry.getValue());
                cache.put(key, new ArrayList<>());
                usingCache.put(key, false);
            }
        }

        private CrossProductIterator(Map<K, ? extends Iterable<T>> iterables) {
            this(new LinkedHashMap<>(), null, iterables.size());
            this.usingIterables = true;
            for (Map.Entry<K, ? extends Iterable<T>> entry : iterables.entrySet()) {
                K key = entry.getKey();
                keys.add(key);
                this.iterables.put(key, entry.getValue());
                iterators.put(key, entry.getValue().iterator());
                usingCache.put(key, false);
            }
        }

        private void loadFirst() {
            current = new LinkedHashMap<>();
            for (K key : keys) {
                T next = iterators.get(key).next();
                current.put(key, next);
                if (!usingIterables) {
                    cache.get(key).add(next);
                }
            }
        }

        @Override
        public boolean hasNext() {
            if (!loaded) {
                loadNext();
                loaded = true;
            }
            return !exhausted;
        }

        @Override
        public Map<K, T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException("Iterator has been exhausted and contains no more elements");
            }
            Map<K, T> ret = current;
            loaded = false;
            return ret;
        }

        private void loadNext() {
            if (current == null) {
                loadFirst();
            } else {
                current = new LinkedHashMap<>(current);
                for (int i = keys.size() - 1; i >= 0; i--) {
                    K key = keys.get(i);
                    if (iterators.get(key).hasNext()) {
                        T next = iterators.get(key).next();
                        current.put(key, next);
                        if (!usingIterables && !usingCache.get(key)) {
                            cache.get(key).add(next);
                        }
                        break;
                    } else if (i > 0) {
                        usingCache.put(key, true);
                        if (usingIterables) {
                            iterators.put(key, iterables.get(key).iterator());
                        } else {
                            iterators.put(key, cache.get(key).iterator());
                        }
                        current.put(key, iterators.get(key).next());
                    } else {
                        exhausted = true;
                    }
                }
            }
        }
    }

    private static class CrossByIndexIterator<K, T> implements Iterator<Map<K, T>> {
        private final Map<K, LazyCachedIterator<T>> sources;
        private final List<K> keys = new ArrayList<>();
        private final Queue<int[]> indexQueue = new LinkedList<>();
        private final Set<int[]> seen = new HashSet<>();
        private Map<K, T> nextItem = null;
        private int currentDepth = 0;

        private CrossByIndexIterator(Map<K, ? extends Iterator<T>> iterators) {
            sources = new LinkedHashMap<>();
            for (Map.Entry<K, ? extends Iterator<T>> entry : iterators.entrySet()) {
                K key = entry.getKey();
                keys.add(key);
                this.sources.put(key, new LazyCachedIterator<>(entry.getValue()));
            }
            enqueueDepth(currentDepth);
            fetchNext();
        }

        private void fetchNext() {
            nextItem = null;

            while (nextItem == null) {
                while (!indexQueue.isEmpty()) {
                    int[] indices = indexQueue.poll();
                    if (!seen.add(indices)) continue;

                    Map<K, T> combination = new LinkedHashMap<>();
                    boolean valid = true;
                    for (int i = 0; i < sources.size(); i++) {
                        K key = keys.get(i);
                        T val = sources.get(key).get(indices[i]);
                        if (val == null) {
                            valid = false;
                            break;
                        }
                        combination.put(key, val);
                    }

                    if (valid) {
                        nextItem = combination;
                        return;
                    }
                }

                currentDepth++;
                if (!enqueueDepth(currentDepth)) return;
            }
        }

        private boolean enqueueDepth(int depth) {
            generateIndices(new int[sources.size()], 0, depth);
            return !indexQueue.isEmpty();
        }

        private void generateIndices(int[] current, int pos, int max) {
            if (pos == current.length) {
                if (max == ArrayGroovyMethods.max(current)) {
                    indexQueue.add(current.clone());
                }
                return;
            }

            int size = sources.get(keys.get(pos)).size();
            for (int i = 0; i <= max && i < size; i++) {
                current[pos] = i;
                generateIndices(current, pos + 1, max);
            }
        }

        @Override
        public boolean hasNext() {
            return nextItem != null;
        }

        @Override
        public Map<K, T> next() {
            if (nextItem == null) throw new NoSuchElementException();
            Map<K, T> result = nextItem;
            fetchNext();
            return result;
        }
    }

    private static class LazyCachedIterator<T> {
        private final Iterator<T> source;
        private final List<T> cache = new ArrayList<>();

        private LazyCachedIterator(Iterator<T> source) {
            this.source = source;
        }

        private T get(int index) {
            while (cache.size() <= index) {
                if (source.hasNext()) {
                    cache.add(source.next());
                } else {
                    return null;
                }
            }
            return cache.get(index);
        }

        private int size() {
            if (source.hasNext()) return Integer.MAX_VALUE;
            return cache.size();
        }
    }
}
