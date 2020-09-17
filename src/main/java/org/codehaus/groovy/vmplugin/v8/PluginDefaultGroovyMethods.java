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
import groovy.lang.EmptyRange;
import groovy.lang.GString;
import groovy.lang.IntRange;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FirstParam;
import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.NullObject;
import org.codehaus.groovy.runtime.RangeInfo;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
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
import java.util.function.Supplier;
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
 * Defines new Groovy methods which appear on standard Java 8 classes within the
 * Groovy environment.
 *
 * @since 2.5.0
 */
public class PluginDefaultGroovyMethods extends DefaultGroovyMethodsSupport {

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
        return self.map(t -> OptionalInt.of(mapper.applyAsInt(t))).orElseGet(OptionalInt::empty);
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
        return self.map(t -> OptionalLong.of(mapper.applyAsLong(t))).orElseGet(OptionalLong::empty);
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
        return self.map(t -> OptionalDouble.of(mapper.applyAsDouble(t))).orElseGet(OptionalDouble::empty);
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
        return self.map(transform::call);
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

    /**
     * Overloads the {@code ++} operator for enums. It will invoke Groovy's
     * default next behaviour for enums that do not have their own next method.
     *
     * @param self an Enum
     * @return the next defined enum from the enum class
     *
     * @since 1.5.2
     */
    public static Object next(final Enum self) {
        for (Method method : self.getClass().getMethods()) {
            if (method.getName().equals("next") && method.getParameterCount() == 0) {
                return InvokerHelper.invokeMethod(self, "next", InvokerHelper.EMPTY_ARGS);
            }
        }
        Object[] values = (Object[]) InvokerHelper.invokeStaticMethod(self.getClass(), "values", InvokerHelper.EMPTY_ARGS);
        int index = Arrays.asList(values).indexOf(self);
        return values[index < values.length - 1 ? index + 1 : 0];
    }

    /**
     * Overloads the {@code --} operator for enums. It will invoke Groovy's
     * default previous behaviour for enums that do not have their own previous method.
     *
     * @param self an Enum
     * @return the previous defined enum from the enum class
     *
     * @since 1.5.2
     */
    public static Object previous(final Enum self) {
        for (Method method : self.getClass().getMethods()) {
            if (method.getName().equals("previous") && method.getParameterCount() == 0) {
                return InvokerHelper.invokeMethod(self, "previous", InvokerHelper.EMPTY_ARGS);
            }
        }
        Object[] values = (Object[]) InvokerHelper.invokeStaticMethod(self.getClass(), "values", InvokerHelper.EMPTY_ARGS);
        int index = Arrays.asList(values).indexOf(self);
        return values[index > 0 ? index - 1 : values.length - 1];
    }

    /**
     * Provides the standard Groovy <code>size()</code> method for <code>StringBuilder</code>.
     *
     * @param self a StringBuilder
     * @return the length of the StringBuilder
     *
     * @since 1.5.2
     *
     * @see org.codehaus.groovy.runtime.StringGroovyMethods#size(CharSequence)
     */
    @Deprecated
    public static int size(final StringBuilder self) {
        return self.length();
    }

    /**
     * Overloads the left shift operator to provide an easy way to append multiple
     * objects as string representations to a StringBuilder.
     *
     * @param self  a StringBuilder
     * @param value a value to append
     * @return the StringBuilder on which this operation was invoked
     *
     * @since 1.5.2
     */
    public static StringBuilder leftShift(final StringBuilder self, final Object value) {
        if (value instanceof GString) {
            // Force the conversion of the GString to string now, or appending
            // is going to be extremely expensive, due to calls to GString#charAt,
            // which is going to re-evaluate the GString for each character!
            return self.append(value.toString());
        }
        if (value instanceof CharSequence) {
            return self.append((CharSequence)value);
        }
        return self.append(value);
    }

    /**
     * Supports the range subscript operator for StringBuilder.
     * Index values are treated as characters within the builder.
     *
     * @param self  a StringBuilder
     * @param range a Range
     * @param value the object that's toString() will be inserted
     *
     * @since 1.5.2
     */
    public static void putAt(final StringBuilder self, final IntRange range, final Object value) {
        RangeInfo info = DefaultGroovyMethodsSupport.subListBorders(self.length(), range);
        self.replace(info.from, info.to, value.toString());
    }

    /**
     * Supports the range subscript operator for StringBuilder.
     *
     * @param self  a StringBuilder
     * @param range a Range
     * @param value the object that's toString() will be inserted
     *
     * @since 1.5.2
     */
    public static void putAt(final StringBuilder self, final EmptyRange range, final Object value) {
        RangeInfo info = DefaultGroovyMethodsSupport.subListBorders(self.length(), range);
        self.replace(info.from, info.to, value.toString());
    }

    /**
     * Appends a String to this StringBuilder.
     *
     * @param self  a StringBuilder
     * @param value a String
     * @return a String
     *
     * @since 1.5.2
     */
    public static String plus(final StringBuilder self, final String value) {
        return self + value;
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
     * Returns an array containing the elements of the stream.
     * <pre class="groovyTestCase">
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
     * Accumulates the elements of stream into a new List.
     *
     * @param self the stream
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
     *
     * @param self the stream
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
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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

    /**
     * Provide JDK9 {@code or} on JDK8.
     */
    public static <T> Optional<T> orElse(Optional<T> self, Supplier<? extends Optional<? extends T>> supplier) {
        if (self.isPresent()) {
            return self;
        }
        return (Optional<T>) supplier.get();
    }
}
