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

import groovy.lang.EmptyRange;
import groovy.lang.IntRange;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.BaseStream;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamGroovyMethods {

    private StreamGroovyMethods() {
    }

    // ---- Cached Collectors -------------------------------------------------
    // Collector instances are stateless configuration objects; the per-call
    // accumulator is freshly produced by the Supplier on each collect()
    // invocation. So sharing the Collector across calls and threads is safe —
    // and saves a per-call allocation that the JDK never elides. Same trick
    // Eclipse Collections' Collectors2 uses.
    //
    // toCollection(ArrayList::new) / toCollection(HashSet::new) (rather than
    // toList()/toSet()) is deliberate: the JDK contract on Collectors.toList()
    // and toSet() doesn't guarantee mutability or concrete type, and several
    // public GDK methods promise a "new mutable List/Set". toCollection pins
    // the supplier so the guarantee is held by the implementation, not by
    // current JDK behaviour.
    //
    // For the immutable Set case (toImmutableSet on Stream / BaseStream), we
    // collect via the cached TO_SET and wrap with Collections.unmodifiableSet
    // at the call site rather than using Collectors.toUnmodifiableSet() — the
    // latter's finisher calls Set.copyOf which rejects null elements, which
    // would diverge from Groovy's null-tolerant idiom and from the matching
    // toImmutableSet methods on Iterator/Iterable/T[].

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Collector TO_LIST = Collectors.toCollection(ArrayList::new);
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Collector TO_SET = Collectors.toCollection(HashSet::new);

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> Collector<T, ?, List<T>> toListCollector() {
        return (Collector) TO_LIST;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> Collector<T, ?, Set<T>> toSetCollector() {
        return (Collector) TO_SET;
    }

    /**
     * Returns element at {@code index} or {@code null}.
     * <p>
     * This is a <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/stream/package-summary.html#StreamOps">short-circuiting terminal operation</a>.
     *
     * <pre class="language-groovy groovyTestCase">
     * import java.util.stream.Stream
     * import static groovy.test.GroovyAssert.shouldFail
     *
     * Stream<String> stream = ['foo','bar','baz'].stream()
     * shouldFail(IllegalArgumentException) { stream[-1] }
     *
     * stream = ['foo','bar','baz'].stream()
     * assert stream[0] == 'foo'
     *
     * stream = ['foo','bar','baz'].stream()
     * assert stream[1] == 'bar'
     *
     * stream = ['foo','bar','baz'].stream()
     * assert stream[2] == 'baz'
     *
     * stream = ['foo','bar','baz'].stream()
     * assert stream[3] === null
     * </pre>
     *
     * @throws IllegalArgumentException if {@code index} is negative
     *
     * @since 5.0.0
     */
    public static <T> T getAt(final Stream<T> self, final int index) {
        return self.skip(index).findFirst().orElse(null);
    }

    /**
     * Returns element(s) in {@code range} or an empty list.
     * <p>
     * This is a <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/stream/package-summary.html#StreamOps">short-circuiting terminal operation</a>.
     *
     * <pre class="language-groovy groovyTestCase">
     * import java.util.stream.Stream
     * import static groovy.test.GroovyAssert.shouldFail
     *
     * Stream&lt;String&gt; stream = ['foo','bar','baz'].stream()
     * shouldFail(IllegalArgumentException) { stream[-1..0] }
     *
     * stream = ['foo','bar','baz'].stream()
     * shouldFail(IllegalArgumentException) { stream[0..-1] }
     *
     * stream = ['foo','bar','baz'].stream()
     * assert stream[0..&lt;1] == ['foo']
     *
     * stream = ['foo','bar','baz'].stream()
     * assert stream[1..&lt;2] == ['bar']
     *
     * stream = ['foo','bar','baz'].stream()
     * assert stream[2..&lt;3] == ['baz']
     *
     * stream = ['foo','bar','baz'].stream()
     * assert stream[3..&lt;4] == []
     *
     * stream = ['foo','bar','baz'].stream()
     * assert stream[0&lt;..2] == ['bar','baz']
     *
     * stream = ['foo','bar','baz'].stream()
     * assert stream[0..99] == ['foo','bar','baz']
     * </pre>
     *
     * @throws IllegalArgumentException for negative index or reverse range
     *
     * @since 5.0.0
     */
    public static <T> List<T> getAt(final Stream<T> self, final IntRange range) {
        if (range.isReverse()) throw new IllegalArgumentException("reverse range");
        return self.skip(range.getFromInt()).limit(range.size()).collect(toListCollector());
    }

    /**
     * Returns an empty list.
     * <p>
     * <pre class="language-groovy groovyTestCase">
     * import java.util.stream.Stream
     * Stream&lt;String&gt; stream = ['foo','bar','baz'].stream()
     * assert stream[1..&lt;1].isEmpty()
     * </pre>
     *
     * @since 5.0.0
     */
    public static <T> List<T> getAt(final Stream<T> self, final EmptyRange range) {
        return new ArrayList<>();
    }

    /**
     * Returns a (possibly empty) stream.
     * <p>
     * This is a <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/stream/package-summary.html#StreamOps">short-circuiting intermediate operation</a>.
     *
     * <pre class="language-groovy groovyTestCase">
     * import java.util.stream.Stream
     * import static groovy.test.GroovyAssert.shouldFail
     *
     * Stream&lt;String&gt; stream = ['foo','bar','baz'].stream()
     * shouldFail(IllegalArgumentException) { stream.from(-1..0) }
     *
     * stream = ['foo','bar','baz'].stream()
     * shouldFail(IllegalArgumentException) { stream.from(0..-1) }
     *
     * stream = ['foo','bar','baz'].stream()
     * assert stream.from(0..&lt;1).toList() == ['foo']
     *
     * stream = ['foo','bar','baz'].stream()
     * assert stream.from(1..&lt;2).toList() == ['bar']
     *
     * stream = ['foo','bar','baz'].stream()
     * assert stream.from(2..&lt;3).toList() == ['baz']
     *
     * stream = ['foo','bar','baz'].stream()
     * assert stream.from(3..&lt;4).toList() == []
     *
     * stream = ['foo','bar','baz'].stream()
     * assert stream.from(0&lt;..2).toList() == ['bar','baz']
     *
     * stream = ['foo','bar','baz'].stream()
     * assert stream.from(0&lt;..&lt;2).toList() == ['bar']
     *
     * stream = ['foo','bar','baz'].stream()
     * assert stream.from(0..99).toList() == ['foo','bar','baz']
     * </pre>
     *
     * @throws IllegalArgumentException for negative index or reverse range
     *
     * @since 5.0.0
     */
    public static <T> Stream<T> from(final Stream<T> self, final IntRange range) {
        if (range.isReverse()) throw new IllegalArgumentException("reverse range");
        return self.skip(range.getFromInt()).limit(range.size());
    }

    /**
     * Returns an empty stream.
     * <p>
     * <pre class="language-groovy groovyTestCase">
     * import java.util.stream.Stream
     * Stream&lt;String&gt; stream = ['foo','bar','baz'].stream()
     * assert !stream.from(1..&lt;1).findAny().isPresent()
     * </pre>
     *
     * @since 5.0.0
     */
    public static <T> Stream<T> from(final Stream<T> self, final EmptyRange range) {
        return Stream.empty();
    }

    //--------------------------------------------------------------------------

    /**
     * Returns a lazily concatenated stream whose elements are all the elements of this stream followed by all the elements of the {@link Collection} object.
     *
     * <pre class="language-groovy groovyTestCase">
     * import java.util.stream.Stream
     * assert (Stream.of(1) + [2]).toList() == [1,2]
     * assert (Stream.of(1) + []).toList() == [1]
     * </pre>
     *
     * @since 4.0.0
     */
    public static <T> Stream<T> plus(final Stream<? extends T> lhs, final Collection<? extends T> rhs) {
        return Stream.concat(lhs, rhs.stream());
    }

    /**
     * Returns a lazily concatenated stream whose elements are all the elements of this stream followed by all the elements of the {@link Iterable} object.
     *
     * <pre class="language-groovy groovyTestCase">
     * import java.util.stream.Stream
     * assert (Stream.of(1) + [2]).toList() == [1,2]
     * assert (Stream.of(1) + []).toList() == [1]
     * </pre>
     *
     * @since 4.0.0
     */
    public static <T> Stream<T> plus(final Stream<? extends T> lhs, final Iterable<? extends T> rhs) {
        return Stream.concat(lhs, stream(rhs));
    }

    /**
     * Returns a lazily concatenated stream whose elements are all the elements of this stream followed by all the elements of the second stream.
     *
     * <pre class="language-groovy groovyTestCase">
     * import java.util.stream.Stream
     * assert (Stream.of(1) + Stream.&lt;Integer&gt;empty()).toList() == [1]
     * assert (Stream.of(1) + Stream.of(2)).toList() == [1,2]
     * assert (Stream.of(1) + [2].stream()).toList() == [1,2]
     * </pre>
     *
     * @since 4.0.0
     */
    public static <T> Stream<T> plus(final Stream<? extends T> lhs, final Stream<? extends T> rhs) {
        return Stream.concat(lhs, rhs);
    }

    //--------------------------------------------------------------------------

    /**
     * Returns a sequential {@link Stream} containing a single element.
     *
     * <pre class="language-groovy groovyTestCase">
     * def item = 'string'
     * assert item.stream().toList() == ['string']
     * assert item.stream().findFirst().isPresent()
     * </pre>
     *
     * @since 3.0.0
     */
    public static <T> Stream<T> stream(final T self) {
        return Stream.of(self);
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param <T> The type of the array elements
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     *
     * @since 2.5.0
     */
    public static <T> Stream<T> stream(final T[] self) {
        return Arrays.stream(self);
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     *
     * @since 2.5.0
     */
    public static Stream<Integer> stream(final int[] self) {
        return Arrays.stream(self).boxed();
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     *
     * @since 2.5.0
     */
    public static Stream<Long> stream(final long[] self) {
        return Arrays.stream(self).boxed();
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     *
     * @since 2.5.0
     */
    public static Stream<Double> stream(final double[] self) {
        return Arrays.stream(self).boxed();
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     *
     * @since 2.5.0
     */
    public static Stream<Character> stream(final char[] self) {
        return IntStream.range(0, self.length).mapToObj(i -> self[i]);
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     *
     * @since 2.5.0
     */
    public static Stream<Byte> stream(final byte[] self) {
        return IntStream.range(0, self.length).mapToObj(i -> self[i]);
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     *
     * @since 2.5.0
     */
    public static Stream<Short> stream(final short[] self) {
        return IntStream.range(0, self.length).mapToObj(i -> self[i]);
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     *
     * @since 2.5.0
     */
    public static Stream<Boolean> stream(final boolean[] self) {
        return IntStream.range(0, self.length).mapToObj(i -> self[i]);
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     *
     * @since 2.5.0
     */
    public static Stream<Float> stream(final float[] self) {
        return IntStream.range(0, self.length).mapToObj(i -> self[i]);
    }

    /**
     * Returns a sequential {@link Stream} with the specified element(s) as its
     * source.
     * <pre class="language-groovy groovyTestCase">
     * def tokens = new StringTokenizer('one two')
     * assert tokens.stream().toList() == ['one', 'two']
     * </pre>
     *
     * @since 3.0.0
     */
    public static <T> Stream<T> stream(final Enumeration<T> self) {
        return stream(new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED) {
            @Override
            public void forEachRemaining(final Consumer<? super T> action) {
                while (self.hasMoreElements()) {
                    action.accept(self.nextElement());
                }
            }
            @Override
            public boolean tryAdvance(final Consumer<? super T> action) {
                if (self.hasMoreElements()) {
                    action.accept(self.nextElement());
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Returns a sequential {@link Stream} with the specified element(s) as its
     * source.
     *
     * <pre class="language-groovy groovyTestCase">
     * class Items implements Iterable<String> {
     *   Iterator&lt;String&gt; iterator() {
     *     ['one', 'two'].iterator()
     *   }
     * }
     * def items = new Items()
     * assert items.stream().toList() == ['one', 'two']
     * </pre>
     *
     * @since 3.0.0
     */
    public static <T> Stream<T> stream(final Iterable<T> self) {
        return StreamSupport.stream(self.spliterator(), false);
    }

    /**
     * Returns a sequential {@link Stream} with the specified element(s) as its
     * source.
     *
     * <pre class="language-groovy groovyTestCase">
     * [].iterator().stream().toList().isEmpty()
     * ['one', 'two'].iterator().stream().toList() == ['one', 'two']
     * </pre>
     *
     * @since 3.0.0
     */
    public static <T> Stream<T> stream(final Iterator<T> self) {
        return stream(Spliterators.spliteratorUnknownSize(self, Spliterator.ORDERED));
    }

    /**
     * Returns a sequential {@link Stream} with the specified element(s) as its
     * source.
     *
     * <pre class="language-groovy groovyTestCase">
     * assert [].spliterator().stream().toList().isEmpty()
     * assert ['one', 'two'].spliterator().stream().toList() == ['one', 'two']
     * </pre>
     *
     * @since 3.0.0
     */
    public static <T> Stream<T> stream(final Spliterator<T> self) {
        return StreamSupport.stream(self, false);
    }

    /**
     * Returns an empty sequential {@link Stream}.
     *
     * <pre class="language-groovy groovyTestCase">
     * def item = null
     * assert item.stream().toList() == []
     * assert !item.stream().findFirst().isPresent()
     * </pre>
     *
     * @since 3.0.0
     */
    public static <T> Stream<T> stream(final NullObject self) {
        return Stream.empty();
    }

    /**
     * If a value is present in the {@link Optional}, returns a {@link Stream}
     * with the value as its source or else an empty stream.
     *
     * @since 3.0.0
     */
    public static <T> Stream<T> stream(final Optional<T> self) {
        return self.stream();
    }

    //

    /**
     * If a value is present in the {@link OptionalInt}, returns an {@link IntStream}
     * with the value as its source or else an empty stream.
     *
     * @since 3.0.0
     */
    public static IntStream stream(final OptionalInt self) {
        if (self.isEmpty()) {
            return IntStream.empty();
        }
        return IntStream.of(self.getAsInt());
    }

    /**
     * If a value is present in the {@link OptionalLong}, returns a {@link LongStream}
     * with the value as its source or else an empty stream.
     *
     * @since 3.0.0
     */
    public static LongStream stream(final OptionalLong self) {
        if (self.isEmpty()) {
            return LongStream.empty();
        }
        return LongStream.of(self.getAsLong());
    }

    /**
     * If a value is present in the {@link OptionalDouble}, returns a {@link DoubleStream}
     * with the value as its source or else an empty stream.
     *
     * @since 3.0.0
     */
    public static DoubleStream stream(final OptionalDouble self) {
        if (self.isEmpty()) {
            return DoubleStream.empty();
        }
        return DoubleStream.of(self.getAsDouble());
    }

    /**
     * Returns a sequential {@link IntStream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     *
     * @since 3.0.8
     */
    public static IntStream intStream(final int[] self) {
        return Arrays.stream(self);
    }

    /**
     * Returns a sequential {@link LongStream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     *
     * @since 3.0.8
     */
    public static LongStream longStream(final long[] self) {
        return Arrays.stream(self);
    }

    /**
     * Returns a sequential {@link DoubleStream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     *
     * @since 3.0.8
     */
    public static DoubleStream doubleStream(final double[] self) {
        return Arrays.stream(self);
    }

    /**
     * An alias for {@code count}. Returns the count of elements for this stream.
     * This is a terminal operator and, depending on the underlying stream, may invoke
     * the stream pipeline, leaving it empty after this call.
     * Care should be taken with stream pipelines that have side effects.
     * This method should not be called on an infinite stream.
     * <pre class="language-groovy groovyTestCase">
     * assert [1, 2, 3].stream().size() == 3
     * </pre>
     *
     * @param self A Stream
     * @return the count of elements in this stream
     * @see Stream#count()
     *
     * @since 5.0.0
     */
    public static long size(final Stream<?> self) {
        return self.count();
    }

    /**
     * An alias for {@code count}. Returns the count of elements for this stream.
     * This is a terminal operator and, depending on the underlying stream, may invoke
     * the stream pipeline, leaving it empty after this call.
     * Care should be taken with stream pipelines that have side effects.
     * This method should not be called on an infinite stream.
     * <pre class="language-groovy groovyTestCase">
     * int[] nums = [1, 2, 3]
     * assert nums.intStream().size() == 3
     * </pre>
     *
     * @param self An IntStream
     * @return the count of elements in this stream
     * @see IntStream#count()
     *
     * @since 5.0.0
     */
    public static long size(final IntStream self) {
        return self.count();
    }

    /**
     * An alias for {@code count}. Returns the count of elements for this stream.
     * This is a terminal operator and, depending on the underlying stream, may invoke
     * the stream pipeline, leaving it empty after this call.
     * Care should be taken with stream pipelines that have side effects.
     * This method should not be called on an infinite stream.
     * <pre class="language-groovy groovyTestCase">
     * long[] nums = [1, 2, 3]
     * assert nums.longStream().size() == 3
     * </pre>
     *
     * @param self A LongStream
     * @return the count of elements in this stream
     * @see LongStream#count()
     *
     * @since 5.0.0
     */
    public static long size(final LongStream self) {
        return self.count();
    }

    /**
     * An alias for {@code count}. Returns the count of elements for this stream.
     * This is a terminal operator and, depending on the underlying stream, may invoke
     * the stream pipeline, leaving it empty after this call.
     * Care should be taken with stream pipelines that have side effects.
     * This method should not be called on an infinite stream.
     * <pre class="language-groovy groovyTestCase">
     * double[] nums = [1.0d, 2.0d, 3.0d]
     * assert nums.doubleStream().size() == 3
     * </pre>
     *
     * @param self A DoubleStream
     * @return the count of elements in this stream
     * @see DoubleStream#count()
     *
     * @since 5.0.0
     */
    public static long size(final DoubleStream self) {
        return self.count();
    }

    //--------------------------------------------------------------------------

    /**
     * Returns an array containing the elements of the stream.
     * <pre class="language-groovy groovyTestCase">
     * import static groovy.test.GroovyAssert.shouldFail
     *
     * assert Arrays.equals([].stream().toArray(Object), new Object[0])
     * assert Arrays.equals([].stream().toArray(String), new String[0])
     * assert Arrays.equals([].stream().toArray(String[]), new String[0][])
     * assert Arrays.equals(['x'].stream().toArray(Object), ['x'].toArray())
     * assert Arrays.equals(['x'].stream().toArray(String), ['x'] as String[])
     * assert Arrays.deepEquals([['x'] as String[]].stream().toArray(String[]), [['x'] as String[]] as String[][])
     * assert Arrays.equals(['x'].stream().toArray(CharSequence), ['x'] as CharSequence[])
     *
     * shouldFail(ArrayStoreException) {
     *     ['x'].stream().toArray(Thread)
     * }
     *
     * shouldFail(IllegalArgumentException) {
     *     ['x'].stream().toArray((Class) null)
     * }
     *
     * // Stream#toArray(IntFunction) should still be used for closure literal:
     * assert Arrays.equals(['x'].stream().toArray { n -&gt; new String[n] }, ['x'] as String[])
     *
     * // Stream#toArray(IntFunction) should still be used for method reference:
     * assert Arrays.equals(['x'].stream().toArray(String[]::new), ['x'] as String[])
     * </pre>
     *
     * @param self the stream
     * @param type the array element type
     *
     * @since 3.0.4
     */
    public static <T> T[] toArray(final Stream<? extends T> self, final Class<T> type) {
        if (type == null) throw new IllegalArgumentException("type cannot be null");
        return self.toArray(length -> (T[]) Array.newInstance(type, length));
    }

    /**
     * Accumulates the elements of stream into a new mutable List.
     * <p>
     * <strong>Note:</strong> since JDK 16, {@link Stream} has a native
     * {@code toList()} method that returns an <em>unmodifiable</em> list.
     * Java instance methods take precedence over GDK extensions in both
     * {@code @CompileStatic} and dynamic dispatch, so {@code stream.toList()}
     * now resolves to the native call and yields an immutable result —
     * <em>not</em> a fresh {@code ArrayList} as it did pre-JDK&nbsp;16.
     * Direct callers of this extension method are pointed at the explicit
     * replacements; see the deprecation note.
     *
     * @param self the stream
     * @param <T> the type of element
     * @return a new mutable {@code java.util.List} instance
     *
     * @since 2.5.0
     *
     * @deprecated since 6.0.0; the native {@link Stream#toList()} (JDK&nbsp;16+)
     *             shadows this and returns an <em>unmodifiable</em> list. If
     *             you need a mutable list, call {@link #toMutableList(Stream)};
     *             otherwise use {@code stream.toList()} directly (faster than
     *             this extension because it skips the {@code Collectors} path).
     */
    @Deprecated(since = "6.0.0")
    public static <T> List<T> toList(final Stream<T> self) {
        return self.collect(toListCollector());
    }

    /**
     * Accumulates the elements of stream into a new List.
     *
     * @param self the {@code java.util.stream.BaseStream}
     * @param <T> the type of element
     * @return a new {@code java.util.List} instance
     *
     * @since 2.5.0
     */
    public static <T> List<T> toList(final BaseStream<T, ? extends BaseStream> self) {
        return stream(self.iterator()).collect(toListCollector());
    }

    /**
     * Accumulates the elements of stream into a new Set.
     *
     * @param self the stream
     * @param <T> the type of element
     * @return a new {@code java.util.Set} instance
     *
     * @since 2.5.0
     */
    public static <T> Set<T> toSet(final Stream<T> self) {
        return self.collect(toSetCollector());
    }

    /**
     * Accumulates the elements of stream into a new Set.
     *
     * @param self the {@code java.util.stream.BaseStream}
     * @param <T> the type of element
     * @return a new {@code java.util.Set} instance
     *
     * @since 2.5.0
     */
    public static <T> Set<T> toSet(final BaseStream<T, ? extends BaseStream> self) {
        return stream(self.iterator()).collect(toSetCollector());
    }

    /**
     * Accumulates the elements of stream into a new mutable List.
     * <p>
     * Explicit alternative to the native {@link Stream#toList()} (which
     * returns an <em>unmodifiable</em> list since JDK&nbsp;16). Use this when
     * you need to add to, remove from, or sort the returned list. The returned
     * list is a concrete {@link ArrayList} — the cached collector pins the
     * supplier so this is contract, not happenstance.
     * <pre class="language-groovy groovyTestCase">
     * import java.util.stream.Stream
     * import org.codehaus.groovy.runtime.StreamGroovyMethods
     * def list = StreamGroovyMethods.toMutableList(Stream.of('a', 'b'))
     * assert list == ['a', 'b']
     * list &lt;&lt; 'c'   // mutable — no UnsupportedOperationException
     * assert list == ['a', 'b', 'c']
     * </pre>
     *
     * @param self the stream
     * @param <T> the type of element
     * @return a new mutable {@code java.util.List} instance
     *
     * @since 6.0.0
     */
    public static <T> List<T> toMutableList(final Stream<T> self) {
        return self.collect(toListCollector());
    }

    /**
     * Accumulates the elements of stream into a new mutable List.
     * <p>
     * Explicit-mutability alias for {@link #toList(BaseStream)}, symmetric with
     * {@link #toMutableList(Stream)}.
     *
     * @param self the {@code java.util.stream.BaseStream}
     * @param <T> the type of element
     * @return a new mutable {@code java.util.List} instance
     *
     * @since 6.0.0
     */
    public static <T> List<T> toMutableList(final BaseStream<T, ? extends BaseStream> self) {
        return stream(self.iterator()).collect(toListCollector());
    }

    /**
     * Accumulates the elements of stream into a new mutable Set.
     * <p>
     * Explicit-mutability alias for {@link #toSet(Stream)}. Provided defensively
     * so user intent stays unambiguous if a future JDK release adds a native
     * {@code Stream.toSet()} (which would shadow the GDK extension the way
     * native {@link Stream#toList()} did in JDK&nbsp;16).
     *
     * @param self the stream
     * @param <T> the type of element
     * @return a new mutable {@code java.util.Set} instance
     *
     * @since 6.0.0
     */
    public static <T> Set<T> toMutableSet(final Stream<T> self) {
        return self.collect(toSetCollector());
    }

    /**
     * Accumulates the elements of stream into a new mutable Set.
     * <p>
     * Explicit-mutability alias for {@link #toSet(BaseStream)}, symmetric with
     * {@link #toMutableSet(Stream)}.
     *
     * @param self the {@code java.util.stream.BaseStream}
     * @param <T> the type of element
     * @return a new mutable {@code java.util.Set} instance
     *
     * @since 6.0.0
     */
    public static <T> Set<T> toMutableSet(final BaseStream<T, ? extends BaseStream> self) {
        return stream(self.iterator()).collect(toSetCollector());
    }

    /**
     * Accumulates the elements of stream into an immutable List.
     *
     * @param self the {@code java.util.stream.BaseStream}
     * @param <T> the type of element
     * @return an immutable {@code java.util.List} instance
     *
     * @since 6.0.0
     */
    public static <T> List<T> toImmutableList(final BaseStream<T, ? extends BaseStream> self) {
        return stream(self.iterator()).toList();
    }

    /**
     * Accumulates the elements of stream into an immutable Set.
     * <p>
     * No native {@code Stream.toSet()} exists, so this provides the immutable
     * counterpart to {@link #toSet(Stream)}. Null elements are preserved
     * (unlike {@link java.util.stream.Collectors#toUnmodifiableSet()} which
     * rejects nulls); the returned set is unmodifiable and mutation attempts
     * throw {@link UnsupportedOperationException}. Returns the canonical
     * empty set ({@link Collections#emptySet()}) when the stream is empty.
     * <pre class="language-groovy groovyTestCase">
     * import java.util.stream.Stream
     * import static groovy.test.GroovyAssert.shouldFail
     * import static org.codehaus.groovy.runtime.StreamGroovyMethods.toImmutableSet
     * def set = toImmutableSet(Stream.of('a', null, 'b', 'a'))
     * assert set == ['a', null, 'b'] as Set
     * shouldFail(UnsupportedOperationException) { set &lt;&lt; 'c' }
     * assert toImmutableSet(Stream.&lt;String&gt;empty()) === Collections.emptySet()
     * </pre>
     *
     * @param self the stream
     * @param <T> the type of element
     * @return an immutable {@code java.util.Set} instance
     *
     * @since 6.0.0
     */
    public static <T> Set<T> toImmutableSet(final Stream<T> self) {
        Set<T> answer = self.collect(toSetCollector());
        return answer.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(answer);
    }

    /**
     * Accumulates the elements of stream into an immutable Set.
     * <p>
     * See {@link #toImmutableSet(Stream)} for the null-tolerance and empty-set
     * semantics — this overload is the {@link BaseStream} counterpart with the
     * same contract.
     *
     * @param self the {@code java.util.stream.BaseStream}
     * @param <T> the type of element
     * @return an immutable {@code java.util.Set} instance
     *
     * @since 6.0.0
     */
    public static <T> Set<T> toImmutableSet(final BaseStream<T, ? extends BaseStream> self) {
        Set<T> answer = stream(self.iterator()).collect(toSetCollector());
        return answer.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(answer);
    }
}
