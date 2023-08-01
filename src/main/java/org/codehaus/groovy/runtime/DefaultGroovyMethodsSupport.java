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

/**
 * Support methods for DefaultGroovyMethods and PluginDefaultMethods.
 */
public class DefaultGroovyMethodsSupport {

    /**
     * Helper method for getAt and putAt.
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
     * Helper method for getAt and putAt.
     */
    protected static RangeInfo subListBorders(int size, EmptyRange range) {
        int from = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getFrom()), size);
        return new RangeInfo(from, from, false);
    }

    /**
     * Helper method for primitive array getAt.
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
                    java.util.logging.Logger.getLogger(name).warning("Caught exception during close(): " + e);
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

    protected static <T> Collection<T> cloneSimilarCollection(Collection<T> orig, int newCapacity) {
        var copy = maybeClone(orig);
        if (copy == null) {
            copy = createSimilarCollection(orig, newCapacity);
            copy.addAll(orig);
        }
        return copy;
    }

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

    protected static Collection createSimilarOrDefaultCollection(Object object) {
        if (object instanceof Collection) {
            return createSimilarCollection((Collection<?>) object);
        }
        return new ArrayList<>();
    }

    protected static <T> Collection<T> createSimilarCollection(Iterable<T> iterable) {
        if (iterable instanceof Collection) {
            return createSimilarCollection((Collection<T>) iterable);
        } else {
            return new ArrayList<>();
        }
    }

    protected static <T> Collection<T> createSimilarCollection(Collection<T> collection) {
        return createSimilarCollection(collection, collection.size());
    }

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

    @SuppressWarnings("unchecked")
    protected static <T> T[] createSimilarArray(final T[] orig, final int newCapacity) {
        Class<?> type = orig.getClass();
        if (type == Object[].class) return (T[]) new Object[newCapacity];
        return (T[]) Array.newInstance(type.getComponentType(), newCapacity);
    }

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

    protected static <T> Queue<T> createSimilarQueue(Queue<T> orig) {
        if (orig instanceof ArrayBlockingQueue) {
            ArrayBlockingQueue<T> queue = (ArrayBlockingQueue<T>) orig;
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
     * Determines if all items of given array are of the same type.
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

    protected static void writeUTF16BomIfRequired(final Writer writer, final String charset) throws IOException {
        writeUTF16BomIfRequired(writer, Charset.forName(charset));
    }

    protected static void writeUTF16BomIfRequired(final Writer writer, final Charset charset) throws IOException {
        if ("UTF-16BE".equals(charset.name())) {
            writeUtf16Bom(writer, true);
        } else if ("UTF-16LE".equals(charset.name())) {
            writeUtf16Bom(writer, false);
        }
    }

    protected static void writeUTF16BomIfRequired(final OutputStream stream, final String charset) throws IOException {
        writeUTF16BomIfRequired(stream, Charset.forName(charset));
    }

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
