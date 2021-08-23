package org.codehaus.groovy.runtime;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
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
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamGroovyMethods {

    private StreamGroovyMethods() {
    }

    /**
     * Returns a lazily concatenated stream whose elements are all the elements of this stream followed by all the elements of the {@link Collection} object.
     *
     * <pre class="groovyTestCase">
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
     * <pre class="groovyTestCase">
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
     * <pre class="groovyTestCase">
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
     * If a value is present in the {@link Optional}, returns a {@link Stream}
     * with the value as its source or else an empty stream.
     *
     * @since 3.0.0
     */
    public static <T> Stream<T> stream(final Optional<T> self) {
        return self.map(Stream::of).orElseGet(Stream::empty);
    }

    //

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

    //--------------------------------------------------------------------------

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
}
