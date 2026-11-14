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
import groovy.lang.NumberRange;
import groovy.lang.Range;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Logger;

/**
 * Support methods for DefaultGroovyMethods and PluginDefaultMethods.
 */
public class DefaultGroovyMethodsSupport {

    /**
     * Resolves {@code range} to list borders for getAt and putAt.
     * An {@link IntRange} or {@link NumberRange} supplies its own borders.
     * Any other range has its endpoints normalised against {@code size},
     * and the ends are swapped when the normalised start sits past the
     * normalised end, which is how a range written with a negative end is applied.
     * The end border is one past the last included index.
     *
     * @param size length of the list being indexed
     * @param range range to resolve
     * @return the start index, the exclusive end index, and whether the slice runs backwards
     */
    protected static RangeInfo subListBorders(int size, Range range) {
        if (range instanceof IntRange) {
            return ((IntRange) range).subListBorders(size);
        }
        if (range instanceof NumberRange) {
            return ((NumberRange) range).subListBorders(size);
        }
        int from = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getFrom()), size);
        int to = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getTo()), size);
        boolean reverse = range.isReverse();
        if (from > to) {
            // support list[1..-1]
            int tmp = to;
            to = from;
            from = tmp;
            reverse = !reverse;
        }
        return new RangeInfo(from, to + 1, reverse);
    }

    /**
     * Resolves an empty range to list borders for getAt and putAt.
     * Both borders are the normalised start, so the slice contains no elements.
     *
     * @param size length of the list being indexed
     * @param range empty range whose start is normalised
     * @return a forward slice whose start and exclusive end are equal
     */
    protected static RangeInfo subListBorders(int size, EmptyRange range) {
        int from = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getFrom()), size);
        return new RangeInfo(from, from, false);
    }

    /**
     * Rebuilds an {@link IntRange} from borders that were prepared for a
     * sub-list. The exclusive end in {@code info} becomes an inclusive end,
     * an exclusive bound is stepped back to the original endpoint, and the
     * inclusive ends are swapped when {@code info} runs backwards.
     *
     * @param info borders previously computed for {@code range}
     * @param range original range, whose inclusive ends are restored
     * @return an inclusive-aware range covering the same indexes
     */
    protected static IntRange subListRange(RangeInfo info, IntRange range) {
        int from = info.from;
        int to = info.to - 1;

        // undo inclusiveness effects done by subListBorders()
        if (!info.reverse) {
            if (Boolean.FALSE.equals(range.getInclusiveLeft())) {
                from--;
            }
            if (Boolean.FALSE.equals(range.getInclusiveRight())) {
                to++;
            }
        } else {
            if (Boolean.FALSE.equals(range.getInclusiveLeft())) {
                to++;
            }
            if (Boolean.FALSE.equals(range.getInclusiveRight())) {
                from--;
            }
        }

        boolean inclusiveLeft = info.reverse ? range.getInclusiveRight() : range.getInclusiveLeft();
        boolean inclusiveRight = info.reverse ? range.getInclusiveLeft() : range.getInclusiveRight();

        return new IntRange(inclusiveLeft, inclusiveRight, from, to);
    }

    /**
     * Converts a possibly negative index to a real index into the array.
     *
     * @param i    the unnormalized index
     * @param size the array size
     * @return the normalised index
     * @throws ArrayIndexOutOfBoundsException when {@code i} is negative and its magnitude exceeds {@code size}
     */
    protected static int normaliseIndex(int i, int size) {
        int temp = i;
        if (i < 0) {
            i += size;
        }
        if (i < 0) {
            throw new ArrayIndexOutOfBoundsException("Negative array index [" + temp + "] too large for array size " + size);
        }
        return i;
    }

    /**
     * Close the Closeable. Logging a warning if any problems occur.
     *
     * @param closeable the thing to close
     */
    public static void closeWithWarning(Closeable closeable) {
        tryClose(closeable, true); // ignore result
    }

    /**
     * Attempts to close the closeable returning rather than throwing
     * any Exception that may occur.
     *
     * @param closeable the thing to close
     * @param logWarning if true will log a warning if an exception occurs
     * @return throwable Exception from the close method, else null
     */
    static Throwable tryClose(AutoCloseable closeable, boolean logWarning) {
        Throwable thrown = null;
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                thrown = e;
                if (logWarning) {
                    String name = DefaultGroovyMethodsSupport.class.getName();
                    Logger.getLogger(name).warning("Caught exception during close(): " + e);
                }
            }
        }
        return thrown;
    }

    /**
     * Closes the Closeable, ignoring any problems that might occur.
     *
     * @param c the thing to close
     */
    public static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ignore) {
            }
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Copies {@code orig}. When {@code orig} implements {@link Cloneable},
     * the copy is the result of calling {@code clone} and {@code newCapacity}
     * is ignored. Otherwise an empty collection of a similar concrete type
     * is created with room for {@code newCapacity} elements, then filled
     * from {@code orig}.
     *
     * @param <T> element type
     * @param orig collection to copy
     * @param newCapacity capacity of the replacement when {@code orig} cannot be cloned
     * @return a copy of {@code orig}
     */
    protected static <T> Collection<T> cloneSimilarCollection(Collection<T> orig, int newCapacity) {
        var copy = maybeClone(orig);
        if (copy == null) {
            copy = createSimilarCollection(orig, newCapacity);
            copy.addAll(orig);
        }
        return copy;
    }

    /**
     * Copies {@code orig}. When {@code orig} implements {@link Cloneable},
     * the copy is the result of calling {@code clone}. Otherwise an empty
     * map of a similar concrete type is created and filled from {@code orig}.
     *
     * @param <K> key type
     * @param <V> value type
     * @param orig map to copy
     * @return a copy of {@code orig}
     */
    protected static <K, V> Map<K ,V> cloneSimilarMap(Map<K, V> orig) {
        var copy = maybeClone(orig);
        if (copy == null) {
            copy = createSimilarMap(orig);
            copy.putAll(orig);
        }
        return copy;
    }

    private static <T> T maybeClone(T object) {
        if (object instanceof Cloneable) {
            @SuppressWarnings("unchecked")
            T copy = (T) InvokerHelper.invokeMethod(object, "clone", null);
            return copy;
        }
        return null;
    }

    /**
     * Builds an empty collection whose concrete type matches {@code object}
     * when {@code object} is a {@link Collection}. Any other value,
     * including null, yields a new {@link ArrayList}.
     *
     * @param object collection to imitate, or any other value
     * @return an empty collection of a similar type, or an {@link ArrayList}
     */
    protected static Collection createSimilarOrDefaultCollection(Object object) {
        if (object instanceof Collection) {
            return createSimilarCollection((Collection<?>) object);
        }
        return new ArrayList<>();
    }

    /**
     * Builds an empty collection whose concrete type matches {@code iterable}
     * when it is a {@link Collection}. Any other iterable yields a new
     * {@link ArrayList}. Elements of {@code iterable} are not copied.
     *
     * @param <T> element type
     * @param iterable source of the concrete collection type
     * @return an empty collection of a similar type
     */
    protected static <T> Collection<T> createSimilarCollection(Iterable<T> iterable) {
        if (iterable instanceof Collection) {
            return createSimilarCollection((Collection<T>) iterable);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Builds an empty collection of the same kind as {@code collection},
     * using {@code collection.size()} as the capacity where the kind
     * accepts one.
     *
     * @param <T> element type
     * @param collection collection to imitate
     * @return an empty collection of a similar type
     */
    protected static <T> Collection<T> createSimilarCollection(Collection<T> collection) {
        return createSimilarCollection(collection, collection.size());
    }

    /**
     * Builds an empty collection of the same kind as {@code orig}.
     * A set, list, or queue keeps that kind. Anything else becomes an
     * {@link ArrayList} with capacity {@code newCapacity}. The capacity
     * is not applied to a set or a queue.
     *
     * @param <T> element type
     * @param orig collection to imitate
     * @param newCapacity initial capacity of a list that is not one of the preserved list classes
     * @return an empty collection of a similar type
     */
    protected static <T> Collection<T> createSimilarCollection(Collection<T> orig, int newCapacity) {
        if (orig instanceof Set) {
            return createSimilarSet((Set<T>) orig);
        }
        if (orig instanceof List) {
            return createSimilarList((List<T>) orig, newCapacity);
        }
        if (orig instanceof Queue) {
            return createSimilarQueue((Queue<T>) orig);
        }
        return new ArrayList<>(newCapacity);
    }

    /**
     * Builds an empty list of the same kind as {@code orig}.
     * {@link LinkedList}, {@link Stack}, {@link Vector}, and
     * {@link CopyOnWriteArrayList} are preserved. Any other list becomes
     * an {@link ArrayList} with capacity {@code newCapacity}.
     *
     * @param <T> element type
     * @param orig list to imitate
     * @param newCapacity initial capacity when the result is an {@link ArrayList}
     * @return an empty list of a similar type
     */
    protected static <T> List<T> createSimilarList(List<T> orig, int newCapacity) {
        if (orig instanceof LinkedList) {
            return new LinkedList<>();
        }
        if (orig instanceof Stack) {
            return new Stack<>();
        }
        if (orig instanceof Vector) {
            return new Vector<>();
        }
        if (orig instanceof CopyOnWriteArrayList) {
            return new CopyOnWriteArrayList<>();
        }
        return new ArrayList<>(newCapacity);
    }

    /**
     * Allocates an array with the component type of {@code orig} and
     * length {@code newCapacity}. Elements of {@code orig} are not copied.
     * An {@code Object[]} source produces an {@code Object[]} result
     * rather than a reflective array of {@code Object}.
     *
     * @param <T> component type
     * @param orig array whose component type is reused
     * @param newCapacity length of the allocated array
     * @return a new array of length {@code newCapacity}
     */
    @SuppressWarnings("unchecked")
    protected static <T> T[] createSimilarArray(final T[] orig, final int newCapacity) {
        Class<?> type = orig.getClass();
        if (type == Object[].class) return (T[]) new Object[newCapacity];
        return (T[]) Array.newInstance(type.getComponentType(), newCapacity);
    }

    /**
     * Builds an empty set of the same kind as {@code orig}. A sorted set
     * keeps its comparator: a {@link ConcurrentSkipListSet} stays that
     * class, and any other {@link SortedSet} becomes a {@link TreeSet}.
     * A {@link CopyOnWriteArraySet} is preserved. Any other set becomes
     * a {@link LinkedHashSet}, so insertion order is kept.
     *
     * @param <T> element type
     * @param orig set to imitate
     * @return an empty set of a similar type
     */
    protected static <T> Set<T> createSimilarSet(Set<T> orig) {
        if (orig instanceof SortedSet) {
            var comparator = ((SortedSet<T>)orig).comparator();
            if (orig instanceof ConcurrentSkipListSet) {
                return new ConcurrentSkipListSet<>(comparator);
            } else {
                return new TreeSet<>(comparator);
            }
        } else {
            if (orig instanceof CopyOnWriteArraySet) {
                return new CopyOnWriteArraySet<>();
            } else {
                return new LinkedHashSet<>(); // not HashSet
            }
        }
    }

    /**
     * Builds an empty queue of the same kind as {@code orig}.
     * An {@link ArrayBlockingQueue} is recreated with its original
     * capacity, the sum of its size and remaining capacity. A
     * {@link PriorityQueue} keeps its comparator. {@link ArrayDeque},
     * {@link ConcurrentLinkedQueue}, {@link DelayQueue},
     * {@link LinkedBlockingDeque}, {@link LinkedBlockingQueue},
     * {@link PriorityBlockingQueue}, and {@link SynchronousQueue} are
     * each recreated as the same class. Any other queue becomes a
     * {@link LinkedList}.
     *
     * @param <T> element type
     * @param orig queue to imitate
     * @return an empty queue of a similar type
     */
    protected static <T> Queue<T> createSimilarQueue(Queue<T> orig) {
        if (orig instanceof ArrayBlockingQueue<T> queue) {
            return new ArrayBlockingQueue<>(queue.size() + queue.remainingCapacity());
        } else if (orig instanceof ArrayDeque) {
            return new ArrayDeque<>();
        } else if (orig instanceof ConcurrentLinkedQueue) {
            return new ConcurrentLinkedQueue<>();
        } else if (orig instanceof DelayQueue) {
            return new DelayQueue(); // T extends Delayed
        } else if (orig instanceof LinkedBlockingDeque) {
            return new LinkedBlockingDeque<>();
        } else if (orig instanceof LinkedBlockingQueue) {
            return new LinkedBlockingQueue<>();
        } else if (orig instanceof PriorityBlockingQueue) {
            return new PriorityBlockingQueue<>();
        } else if (orig instanceof PriorityQueue) {
            return new PriorityQueue<>(11, ((PriorityQueue<T>) orig).comparator());
        } else if (orig instanceof SynchronousQueue) {
            return new SynchronousQueue<>();
        } else {
            return new LinkedList<>();
        }
    }

    /**
     * Builds an empty map of the same kind as {@code orig}. A sorted map
     * keeps its comparator: a {@link ConcurrentSkipListMap} stays that
     * class, and any other {@link SortedMap} becomes a {@link TreeMap}.
     * {@link ConcurrentHashMap}, {@link Hashtable}, {@link Properties},
     * {@link IdentityHashMap}, and {@link WeakHashMap} are preserved.
     * Any other map becomes a {@link LinkedHashMap}, so insertion order
     * is kept.
     *
     * @param <K> key type
     * @param <V> value type
     * @param orig map to imitate
     * @return an empty map of a similar type
     */
    protected static <K, V> Map<K, V> createSimilarMap(Map<K, V> orig) {
        if (orig instanceof SortedMap) {
            var comparator = ((SortedMap<K,V>)orig).comparator();
            if (orig instanceof ConcurrentSkipListMap) {
                return new ConcurrentSkipListMap<>(comparator);
            } else {
                return new TreeMap<>(comparator);
            }
        } else {
            if (orig instanceof ConcurrentHashMap) {
                return new ConcurrentHashMap<>();
            } else if (orig instanceof Hashtable) {
                if (orig instanceof Properties) {
                    @SuppressWarnings("unchecked") // safe?
                    var props = (Map<K,V>)new Properties();
                    return props;
                } else {
                    return new Hashtable<>();
                }
            } else if (orig instanceof IdentityHashMap) {
                return new IdentityHashMap<>();
            } else if (orig instanceof WeakHashMap) {
                return new WeakHashMap<>();
            } else {
                return new LinkedHashMap<>(); // not HashMap
            }
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Determines if all items of given array are of the same type.
     *
     * @param cols an array of collections
     * @return true if the collections are all of the same type
     */
    protected static boolean sameType(Collection[] cols) {
        List<Object> all = new ArrayList<>(cols.length*8);
        for (Collection<?> col : cols) {
            all.addAll(col);
        }
        if (all.isEmpty()) {
            return true;
        }

        Object first = all.get(0);
        // trying to determine the base class of the collections
        // special case for Numbers
        Class<?> baseClass;
        if (first instanceof Number) {
            baseClass = Number.class;
        } else if (first == null) {
            baseClass = NullObject.class;
        } else {
            baseClass = first.getClass();
        }

        for (Collection<?> col : cols) {
            for (Object o : col) {
                if (!baseClass.isInstance(o)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Reports whether the elements of {@code iters} fit one type.
     * The type is taken from the first element. When that element is a
     * {@link Number}, later numbers of any numeric class are accepted.
     * No elements counts as one type.
     *
     * @param iters iterables whose elements are compared
     * @return {@code true} when every element fits the type of the first element
     */
    protected static boolean sameType(Iterable[] iters) {
        Object first;
        Class<?> baseClass = null;

        for (var iter : iters) {
            for (Object o : iter) {
                if (baseClass == null) {
                    first = o;
                    if (first instanceof Number) {
                        baseClass = Number.class;
                    } else if (first == null) {
                        baseClass = NullObject.class;
                    } else {
                        baseClass = first.getClass();
                    }
                } else {
                    if (!baseClass.isInstance(o)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Writes a UTF-16 byte-order mark to {@code writer} when {@code charset}
     * names {@code UTF-16BE} or {@code UTF-16LE}. Any other charset name
     * writes nothing. The name is resolved with {@link Charset#forName(String)}.
     *
     * @param writer destination for the mark
     * @param charset charset name to test
     * @throws java.nio.charset.IllegalCharsetNameException when {@code charset} is not a legal charset name
     * @throws java.nio.charset.UnsupportedCharsetException when no charset is available for that name
     * @throws IOException when writing the mark fails
     */
    protected static void writeUTF16BomIfRequired(final Writer writer, final String charset) throws IOException {
        writeUTF16BomIfRequired(writer, Charset.forName(charset));
    }

    /**
     * Writes a UTF-16 byte-order mark to {@code writer} when the canonical
     * name of {@code charset} is {@code UTF-16BE} or {@code UTF-16LE}.
     * Any other charset writes nothing.
     *
     * @param writer destination for the mark
     * @param charset charset whose canonical name is tested
     * @throws IOException when writing the mark fails
     */
    protected static void writeUTF16BomIfRequired(final Writer writer, final Charset charset) throws IOException {
        if ("UTF-16BE".equals(charset.name())) {
            writeUtf16Bom(writer, true);
        } else if ("UTF-16LE".equals(charset.name())) {
            writeUtf16Bom(writer, false);
        }
    }

    /**
     * Writes a UTF-16 byte-order mark to {@code stream} when {@code charset}
     * names {@code UTF-16BE} or {@code UTF-16LE}. Any other charset name
     * writes nothing. The name is resolved with {@link Charset#forName(String)}.
     *
     * @param stream destination for the mark
     * @param charset charset name to test
     * @throws java.nio.charset.IllegalCharsetNameException when {@code charset} is not a legal charset name
     * @throws java.nio.charset.UnsupportedCharsetException when no charset is available for that name
     * @throws IOException when writing the mark fails
     */
    protected static void writeUTF16BomIfRequired(final OutputStream stream, final String charset) throws IOException {
        writeUTF16BomIfRequired(stream, Charset.forName(charset));
    }

    /**
     * Writes a UTF-16 byte-order mark to {@code stream} when the canonical
     * name of {@code charset} is {@code UTF-16BE} or {@code UTF-16LE}.
     * Big-endian writes {@code FE FF}; little-endian writes {@code FF FE}.
     * Any other charset writes nothing.
     *
     * @param stream destination for the mark
     * @param charset charset whose canonical name is tested
     * @throws IOException when writing the mark fails
     */
    protected static void writeUTF16BomIfRequired(final OutputStream stream, final Charset charset) throws IOException {
        if ("UTF-16BE".equals(charset.name())) {
            writeUtf16Bom(stream, true);
        } else if ("UTF-16LE".equals(charset.name())) {
            writeUtf16Bom(stream, false);
        }
    }

    private static void writeUtf16Bom(OutputStream stream, boolean bigEndian) throws IOException {
        if (bigEndian) {
            stream.write(-2);  // FE
            stream.write(-1);  // FF
        } else {
            stream.write(-1);  // FF
            stream.write(-2);  // FE
        }
    }

    private static void writeUtf16Bom(Writer writer, boolean bigEndian) throws IOException {
        if (bigEndian) {
            writer.write(-2);  // FE
            writer.write(-1);  // FF
        } else {
            writer.write(-1);  // FF
            writer.write(-2);  // FE
        }
    }
}
