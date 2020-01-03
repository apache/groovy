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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
     * Coerce an Optional instance to a boolean value.
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
     * @param stream the Stream
     * @param <T> the type of element
     * @return a new {@code java.util.List} instance
     *
     * @since 2.5.0
     */
    public static <T> List<T> toList(final Stream<T> stream) {
        return stream.collect(Collectors.<T>toList());
    }

    /**
     * Accumulates the elements of stream into a new Set.
     * @param stream the Stream
     * @param <T> the type of element
     * @return a new {@code java.util.Set} instance
     *
     * @since 2.5.0
     */
    public static <T> Set<T> toSet(final Stream<T> stream) {
        return stream.collect(Collectors.<T>toSet());
    }

    /**
     * Accumulates the elements of stream into a new List.
     * @param stream the {@code java.util.stream.BaseStream}
     * @param <T> the type of element
     * @return a new {@code java.util.List} instance
     *
     * @since 2.5.0
     */
    public static <T> List<T> toList(final BaseStream<T, ? extends BaseStream> stream) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(stream.iterator(), Spliterator.ORDERED),
                false
        ).collect(Collectors.<T>toList());
    }

    /**
     * Accumulates the elements of stream into a new Set.
     * @param stream the {@code java.util.stream.BaseStream}
     * @param <T> the type of element
     * @return a new {@code java.util.Set} instance
     *
     * @since 2.5.0
     */
    public static <T> Set<T> toSet(final BaseStream<T, ? extends BaseStream> stream) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(stream.iterator(), Spliterator.ORDERED),
                false
        ).collect(Collectors.<T>toSet());
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
    public static Stream<Long> stream(final long[] self) {
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
    public static Stream<Double> stream(final double[] self) {
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
}
