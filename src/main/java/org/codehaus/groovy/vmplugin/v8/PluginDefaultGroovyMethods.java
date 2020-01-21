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
package org.codehaus.groovy.vmplugin.v8;

import groovy.lang.Closure;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FirstParam;
import org.codehaus.groovy.runtime.NullObject;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Defines new Groovy methods which appear on normal JDK 8
 * classes inside the Groovy environment.
 *
 * @since 2.5.0
 */
public class PluginDefaultGroovyMethods {

    // No instances, static methods only
    private PluginDefaultGroovyMethods() {
    }

    /**
     * Coerce an {@code Optional} instance to a {@code boolean} value.
     * <pre class="groovyTestCase">
     * assert !Optional.empty().asBoolean()
     * assert Optional.of(1234).asBoolean()
     * </pre>
     *
     * @param optional the Optional
     * @return {@code true} if a value is present, otherwise {@code false}
     *
     * @since 2.5.0
     */
    public static boolean asBoolean(final Optional<?> optional) {
        return optional.isPresent();
    }

    /**
     * If a value is present in the {@code OptionalInt}, returns the value,
     * otherwise throws {@code NoSuchElementException}.
     * <pre class="groovyTestCase">
     * assert OptionalInt.of(1234).get() == 1234
     * </pre>
     *
     * @since 3.0.0
     */
    public static int get(final OptionalInt self) {
        return self.getAsInt();
    }

    /**
     * If a value is present in the {@code OptionalLong}, returns the value,
     * otherwise throws {@code NoSuchElementException}.
     * <pre class="groovyTestCase">
     * assert OptionalLong.of(1234L).get() == 1234L
     * </pre>
     *
     * @since 3.0.0
     */
    public static long get(final OptionalLong self) {
        return self.getAsLong();
    }

    /**
     * If a value is present in the {@code OptionalDouble}, returns the value,
     * otherwise throws {@code NoSuchElementException}.
     * <pre class="groovyTestCase">
     * assert OptionalDouble.of(Math.PI).get() == Math.PI
     * </pre>
     *
     * @since 3.0.0
     */
    public static double get(final OptionalDouble self) {
        return self.getAsDouble();
    }

    /**
     * Tests given value against specified type and changes generics of result.
     * This is equivalent to: <code>self.filter(it -&gt; it instanceof Type).map(it -&gt; (Type) it)</code>
     * <pre class="groovyTestCase">
     * assert !Optional.empty().filter(Number).isPresent()
     * assert !Optional.of('x').filter(Number).isPresent()
     * assert Optional.of(1234).filter(Number).isPresent()
     * assert Optional.of(1234).filter(Number).get().equals(1234)
     * </pre>
     *
     * @since 3.0.0
     */
    public static <T> Optional<T> filter(final Optional<?> self, final Class<T> type) {
        return self.filter(type::isInstance).map(type::cast);
    }

    /**
     * If a value is present in the {@code OptionalInt}, tests the value using
     * the given predicate and returns the optional if the test returns true or
     * else empty.
     * <pre class="groovyTestCase">
     * assert !OptionalInt.empty().filter(i -&gt; true).isPresent()
     * assert  OptionalInt.of(1234).filter(i -&gt; true).isPresent()
     * assert !OptionalInt.of(1234).filter(i -&gt; false).isPresent()
     * assert  OptionalInt.of(1234).filter(i -&gt; true).getAsInt() == 1234
     * </pre>
     *
     * @since 3.0.0
     */
    public static OptionalInt filter(final OptionalInt self, final IntPredicate test) {
        if (!self.isPresent() || !test.test(self.getAsInt())) {
            return OptionalInt.empty();
        }
        return self;
    }

    /**
     * If a value is present in the {@code OptionalLong}, tests the value using
     * the given predicate and returns the optional if the test returns true or
     * else empty.
     * <pre class="groovyTestCase">
     * assert !OptionalLong.empty().filter(n -&gt; true).isPresent()
     * assert  OptionalLong.of(123L).filter(n -&gt; true).isPresent()
     * assert !OptionalLong.of(123L).filter(n -&gt; false).isPresent()
     * assert  OptionalLong.of(123L).filter(n -&gt; true).getAsLong() == 123L
     * </pre>
     *
     * @since 3.0.0
     */
    public static OptionalLong filter(final OptionalLong self, final LongPredicate test) {
        if (!self.isPresent() || !test.test(self.getAsLong())) {
            return OptionalLong.empty();
        }
        return self;
    }

    /**
     * If a value is present in the {@code OptionalDouble}, tests the value using
     * the given predicate and returns the optional if the test returns true or
     * empty otherwise.
     * <pre class="groovyTestCase">
     * assert !OptionalDouble.empty().filter(n -&gt; true).isPresent()
     * assert  OptionalDouble.of(Math.PI).filter(n -&gt; true).isPresent()
     * assert !OptionalDouble.of(Math.PI).filter(n -&gt; false).isPresent()
     * assert  OptionalDouble.of(Math.PI).filter(n -&gt; true).getAsDouble() == Math.PI
     * </pre>
     *
     * @since 3.0.0
     */
    public static OptionalDouble filter(final OptionalDouble self, final DoublePredicate test) {
        if (!self.isPresent() || !test.test(self.getAsDouble())) {
            return OptionalDouble.empty();
        }
        return self;
    }

    /**
     * If a value is present in the {@code OptionalInt}, returns an {@code Optional}
     * consisting of the result of applying the given function to the value or else empty.
     * <pre class="groovyTestCase">
     * assert !OptionalInt.empty().mapToObj(x -&gt; new Object()).isPresent()
     * assert  OptionalInt.of(1234).mapToObj(x -&gt; new Object()).isPresent()
     * assert !OptionalInt.of(1234).mapToObj(x -&gt; null).isPresent()
     * assert  OptionalInt.of(1234).mapToObj(Integer::toString).get() == '1234'
     * </pre>
     *
     * @since 3.0.0
     */
    public static <T> Optional<T> mapToObj(final OptionalInt self, final IntFunction<? extends T> mapper) {
        if (!self.isPresent()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.apply(self.getAsInt()));
    }

    /**
     * If a value is present in the {@code OptionalLong}, returns an {@code Optional}
     * consisting of the result of applying the given function to the value or else empty.
     * <pre class="groovyTestCase">
     * assert !OptionalLong.empty().mapToObj(x -&gt; new Object()).isPresent()
     * assert  OptionalLong.of(123L).mapToObj(x -&gt; new Object()).isPresent()
     * assert !OptionalLong.of(123L).mapToObj(x -&gt; null).isPresent()
     * assert  OptionalLong.of(1234L).mapToObj(Long::toString).get() == '1234'
     * </pre>
     *
     * @since 3.0.0
     */
    public static <T> Optional<T> mapToObj(final OptionalLong self, final LongFunction<? extends T> mapper) {
        if (!self.isPresent()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.apply(self.getAsLong()));
    }

    /**
     * If a value is present in the {@code OptionalDouble}, returns an {@code Optional}
     * consisting of the result of applying the given function to the value or else empty.
     * <pre class="groovyTestCase">
     * assert !OptionalDouble.empty().mapToObj(x -&gt; new Object()).isPresent()
     * assert  OptionalDouble.of(Math.PI).mapToObj(x -&gt; new Object()).isPresent()
     * assert !OptionalDouble.of(Math.PI).mapToObj(x -&gt; null).isPresent()
     * assert  OptionalDouble.of(Math.PI).mapToObj(Double::toString).get().startsWith('3.14')
     * </pre>
     *
     * @since 3.0.0
     */
    public static <T> Optional<T> mapToObj(final OptionalDouble self, final DoubleFunction<? extends T> mapper) {
        if (!self.isPresent()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.apply(self.getAsDouble()));
    }

    /**
     * If a value is present in the {@code OptionalInt}, returns an {@code OptionalInt}
     * consisting of the result of applying the given function to the value or else empty.
     * <pre class="groovyTestCase">
     * assert !Optional.empty().mapToInt(x -&gt; 42).isPresent()
     * assert  Optional.of('x').mapToInt(x -&gt; 42).getAsInt() == 42
     * </pre>
     *
     * @since 3.0.0
     */
    public static <T> OptionalInt mapToInt(final Optional<T> self, final ToIntFunction<? super T> mapper) {
        if (!self.isPresent()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(mapper.applyAsInt(self.get()));
    }

    /**
     * If a value is present in the {@code OptionalLong}, returns an {@code OptionalLong}
     * consisting of the result of applying the given function to the value or else empty.
     * <pre class="groovyTestCase">
     * assert !Optional.empty().mapToLong(x -&gt; 42L).isPresent()
     * assert  Optional.of('x').mapToLong(x -&gt; 42L).getAsLong() == 42L
     * </pre>
     *
     * @since 3.0.0
     */
    public static <T> OptionalLong mapToLong(final Optional<T> self, final ToLongFunction<? super T> mapper) {
        if (!self.isPresent()) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(mapper.applyAsLong(self.get()));
    }

    /**
     * If a value is present in the {@code OptionalDouble}, returns an {@code OptionalDouble}
     * consisting of the result of applying the given function to the value or else empty.
     * <pre class="groovyTestCase">
     * assert !Optional.empty().mapToDouble(x -&gt; Math.PI).isPresent()
     * assert  Optional.of('x').mapToDouble(x -&gt; Math.PI).getAsDouble() == Math.PI
     * </pre>
     *
     * @since 3.0.0
     */
    public static <T> OptionalDouble mapToDouble(final Optional<T> self, final ToDoubleFunction<? super T> mapper) {
        if (!self.isPresent()) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(mapper.applyAsDouble(self.get()));
    }

    /**
     * If the optional contains a value, returns an optional containing the transformed value obtained using the <code>transform</code> closure
     * or otherwise an empty optional.
     * <pre class="groovyTestCase">
     * assert Optional.of("foobar").collect{ it.size() }.get() == 6
     * assert !Optional.empty().collect{ it.size() }.isPresent()
     * </pre>
     *
     * @param self      an Optional
     * @param transform the closure used to transform the optional value if present
     * @return an Optional containing the transformed value or empty if the optional is empty or the transform returns null
     *
     * @since 3.0.0
     */
    public static <S,T> Optional<T> collect(final Optional<S> self, @ClosureParams(FirstParam.FirstGenericType.class) final Closure<T> transform) {
        Objects.requireNonNull(self);
        Objects.requireNonNull(transform);
        if (!self.isPresent()) {
            return Optional.empty();
        }
        return Optional.ofNullable(transform.call(self.get()));
    }

    /**
     * Returns a Future asynchronously returning a transformed result.
     * <pre class="_temp_disabled_groovyTestCase">
     * import java.util.concurrent.*
     * def executor = Executors.newSingleThreadExecutor()
     * Future<String> foobar = executor.submit{ "foobar" }
     * Future<Integer> foobarSize = foobar.collect{ it.size() }
     * assert foobarSize.get() == 6
     * executor.shutdown()
     * </pre>
     *
     * @param self      a Future
     * @param transform the closure used to transform the Future value
     * @return a Future allowing the transformed value to be obtained asynchronously
     *
     * @since 3.0.0
     */
    public static <S,T> Future<T> collect(final Future<S> self, @ClosureParams(FirstParam.FirstGenericType.class) final Closure<T> transform) {
        Objects.requireNonNull(self);
        Objects.requireNonNull(transform);
        return new TransformedFuture<T>(self, transform);
    }

    private static class TransformedFuture<E> implements Future<E> {
        private final Future delegate;
        private final Closure<E> transform;

        private TransformedFuture(final Future delegate, final Closure<E> transform) {
            this.delegate = delegate;
            this.transform = transform;
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            return delegate.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return delegate.isCancelled();
        }

        @Override
        public boolean isDone() {
            return delegate.isDone();
        }

        @Override
        public E get() throws InterruptedException, ExecutionException {
            return transform.call(delegate.get());
        }

        @Override
        public E get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return transform.call(delegate.get(timeout, unit));
        }
    }

    /**
     * Accumulates the elements of stream into a new List.
     * @param self the Stream
     * @param <T> the type of element
     * @return a new {@code java.util.List} instance
     *
     * @since 2.5.0
     */
    public static <T> List<T> toList(final Stream<T> self) {
        return self.collect(Collectors.toList());
    }

    /**
     * Accumulates the elements of stream into a new Set.
     * @param self the Stream
     * @param <T> the type of element
     * @return a new {@code java.util.Set} instance
     *
     * @since 2.5.0
     */
    public static <T> Set<T> toSet(final Stream<T> self) {
        return self.collect(Collectors.toSet());
    }

    /**
     * Accumulates the elements of stream into a new List.
     * @param self the {@code java.util.stream.BaseStream}
     * @param <T> the type of element
     * @return a new {@code java.util.List} instance
     *
     * @since 2.5.0
     */
    public static <T> List<T> toList(final BaseStream<T, ? extends BaseStream> self) {
        return stream(self.iterator()).collect(Collectors.toList());
    }

    /**
     * Accumulates the elements of stream into a new Set.
     * @param self the {@code java.util.stream.BaseStream}
     * @param <T> the type of element
     * @return a new {@code java.util.Set} instance
     *
     * @since 2.5.0
     */
    public static <T> Set<T> toSet(final BaseStream<T, ? extends BaseStream> self) {
        return stream(self.iterator()).collect(Collectors.toSet());
    }

    /**
     * Returns an empty sequential {@link Stream}.
     * <pre class="groovyTestCase">
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
     * Returns a sequential {@link Stream} containing a single element.
     * <pre class="groovyTestCase">
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
        return Arrays.stream(self).mapToObj(Integer::valueOf);
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
        return Arrays.stream(self).mapToObj(Long::valueOf);
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
        return Arrays.stream(self).mapToObj(Double::valueOf);
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
     * <pre class="groovyTestCase">
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
     * <pre class="groovyTestCase">
     * class Items implements Iterable<String> {
     *   Iterator<String> iterator() {
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
     * <pre class="groovyTestCase">
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
     * <pre class="groovyTestCase">
     * [].spliterator().stream().toList().isEmpty()
     * ['one', 'two'].spliterator().stream().toList() == ['one', 'two']
     * </pre>
     *
     * @since 3.0.0
     */
    public static <T> Stream<T> stream(final Spliterator<T> self) {
        return StreamSupport.stream(self, false);
    }

    /**
     * If a value is present in the {@link Optional}, returns a {@link Stream}
     * with the value as its source or else an empty stream.
     *
     * @since 3.0.0
     */
    public static <T> Stream<T> stream(final Optional<T> self) {
        return self.map(Stream::of).orElseGet(Stream::empty);
    }

    /**
     * If a value is present in the {@link OptionalInt}, returns an {@link IntStream}
     * with the value as its source or else an empty stream.
     *
     * @since 3.0.0
     */
    public static IntStream stream(final OptionalInt self) {
        if (!self.isPresent()) {
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
        if (!self.isPresent()) {
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
        if (!self.isPresent()) {
            return DoubleStream.empty();
        }
        return DoubleStream.of(self.getAsDouble());
    }
}
