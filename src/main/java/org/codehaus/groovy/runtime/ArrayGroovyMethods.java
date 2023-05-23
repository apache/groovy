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

import groovy.lang.Closure;
import groovy.lang.EmptyRange;
import groovy.lang.IntRange;
import groovy.lang.MetaClass;
import groovy.lang.ObjectRange;
import groovy.lang.Range;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FirstParam;
import groovy.transform.stc.FromString;
import groovy.util.ClosureComparator;
import groovy.util.OrderBy;
import groovy.util.function.DoubleComparator;
import groovy.util.function.IntComparator;
import groovy.util.function.LongComparator;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.runtime.callsite.BooleanClosureWrapper;
import org.codehaus.groovy.runtime.callsite.BooleanReturningMethodInvoker;
import org.codehaus.groovy.runtime.dgmimpl.NumberNumberDiv;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.util.ArrayIterable;
import org.codehaus.groovy.util.ArrayIterator;
import org.codehaus.groovy.util.BooleanArrayIterator;
import org.codehaus.groovy.util.ByteArrayIterator;
import org.codehaus.groovy.util.CharArrayIterator;
import org.codehaus.groovy.util.DoubleArrayIterable;
import org.codehaus.groovy.util.DoubleArrayIterator;
import org.codehaus.groovy.util.FloatArrayIterator;
import org.codehaus.groovy.util.IntArrayIterable;
import org.codehaus.groovy.util.IntArrayIterator;
import org.codehaus.groovy.util.LongArrayIterable;
import org.codehaus.groovy.util.LongArrayIterator;
import org.codehaus.groovy.util.ShortArrayIterator;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntUnaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongUnaryOperator;

/**
 * This class defines new groovy methods which appear on primitive arrays inside
 * the Groovy environment. Static methods are used with the first parameter being
 * the destination class, i.e. <code>public static int[] each(int[] self, Closure closure)</code>
 * provides a <code>each({i -> })</code> method for <code>int[]</code>.
 * <p>
 * NOTE: While this class contains many 'public' static methods, it is
 * primarily regarded as an internal class (its internal package name
 * suggests this also). We value backwards compatibility of these
 * methods when used within Groovy but value less backwards compatibility
 * at the Java method call level. I.e. future versions of Groovy may
 * remove or move a method call in this file but would normally
 * aim to keep the method available from within Groovy.
 */
public class ArrayGroovyMethods extends DefaultGroovyMethodsSupport {

    private ArrayGroovyMethods() {}

    /* Arrangement of each method (skip any inapplicable types for the methods):
     * 1. boolean[]
     * 2. byte[]
     * 3. char[]
     * 4. short[]
     * 5. int[]
     * 6. long[]
     * 7. float[]
     * 8. double[]
     * 9. Object[]
     */

    //--------------------------------------------------------------------------
    // any

    /**
     * Iterates over the contents of a boolean Array, and checks whether
     * any element is true.
     * <pre class="groovyTestCase">
     * boolean[] array1 = [false, true]
     * assert array1.any()
     * boolean[] array2 = [false]
     * assert !array2.any()
     * </pre>
     *
     * @param self the boolean array over which we iterate
     * @return true if any boolean is true
     * @since 5.0.0
     */
    public static boolean any(boolean[] self) {
        Objects.requireNonNull(self);
        for (boolean item : self) {
            if (item) return true;
        }
        return false;
    }

    /**
     * Iterates over the contents of a boolean Array, and checks whether a
     * predicate is valid for at least one element.
     * <pre class="groovyTestCase">
     * boolean[] array = [true]
     * assert array.any{ it }
     * assert !array.any{ !it }
     * </pre>
     *
     * @param self      the boolean array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the booleans matches the closure predicate
     * @since 5.0.0
     */
    public static boolean any(boolean[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (boolean item : self) {
            if (bcw.call(item)) return true;
        }
        return false;
    }

    /**
     * Iterates over the contents of a byte Array, and checks whether a
     * predicate is valid for at least one element.
     * <pre class="groovyTestCase">
     * byte[] array = [0, 1, 2]
     * assert array.any{ it > 1 }
     * assert !array.any{ it > 3 }
     * </pre>
     *
     * @param self      the byte array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the bytes matches the closure predicate
     * @since 5.0.0
     */
    public static boolean any(byte[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (byte item : self) {
            if (bcw.call(item)) return true;
        }
        return false;
    }

    /**
     * Iterates over the contents of a char Array, and checks whether a
     * predicate is valid for at least one element.
     * <pre class="groovyTestCase">
     * char[] array = ['a', 'b', 'c']
     * assert array.any{ it &lt;= 'a' }
     * assert !array.any{ it &lt; 'a' }
     * </pre>
     *
     * @param self      the char array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the chars matches the closure predicate
     * @since 5.0.0
     */
    public static boolean any(char[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (char item : self) {
            if (bcw.call(item)) return true;
        }
        return false;
    }

    /**
     * Iterates over the contents of a short Array, and checks whether a
     * predicate is valid for at least one element.
     * <pre class="groovyTestCase">
     * short[] array = [0, 1, 2]
     * assert array.any{ it > 1 }
     * assert !array.any{ it > 3 }
     * </pre>
     *
     * @param self      the char array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the shorts matches the closure predicate
     * @since 5.0.0
     */
    public static boolean any(short[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (short item : self) {
            if (bcw.call(item)) return true;
        }
        return false;
    }

    /**
     * Iterates over the contents of an int Array, and checks whether a
     * predicate is valid for at least one element.
     * <pre class="groovyTestCase">
     * int[] array = [0, 1, 2]
     * assert array.any{ it > 1 }
     * assert !array.any{ it > 3 }
     * </pre>
     *
     * @param self      the int array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the ints matches the closure predicate
     * @since 5.0.0
     */
    public static boolean any(int[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (int item : self) {
            if (bcw.call(item)) return true;
        }
        return false;
    }

    /**
     * Iterates over the contents of a long Array, and checks whether a
     * predicate is valid for at least one element.
     * <pre class="groovyTestCase">
     * long[] array = [0L, 1L, 2L]
     * assert array.any{ it > 1L }
     * assert !array.any{ it > 3L }
     * </pre>
     *
     * @param self      the long array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the longs matches the closure predicate
     * @since 5.0.0
     */
    public static boolean any(long[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (long item : self) {
            if (bcw.call(item)) return true;
        }
        return false;
    }

    /**
     * Iterates over the contents of a float Array, and checks whether a
     * predicate is valid for at least one element.
     * <pre class="groovyTestCase">
     * float[] array = [0.0f, 1.0f, 2.0f]
     * assert array.any{ it > 1.5f }
     * assert !array.any{ it > 2.5f }
     * </pre>
     *
     * @param self      the float array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the floats matches the closure predicate
     * @since 5.0.0
     */
    public static boolean any(float[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (float item : self) {
            if (bcw.call(item)) return true;
        }
        return false;
    }

    /**
     * Iterates over the contents of a double Array, and checks whether a
     * predicate is valid for at least one element.
     * <pre class="groovyTestCase">
     * double[] array = [0.0d, 1.0d, 2.0d]
     * assert array.any{ it > 1.5d }
     * assert !array.any{ it > 2.5d }
     * </pre>
     *
     * @param self      the double array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the doubles matches the closure predicate
     * @since 5.0.0
     */
    public static boolean any(double[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (double item : self) {
            if (bcw.call(item)) return true;
        }
        return false;
    }

    /**
     * Iterates over the contents of an Array, and checks whether a
     * predicate is valid for at least one element.
     *
     * @param self      the array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the object matches the closure predicate
     * @since 2.5.0
     */
    public static <T> boolean any(T[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (T item : self) {
            if (bcw.call(item)) return true;
        }
        return false;
    }

    //--------------------------------------------------------------------------
    // asBoolean

    /**
     * Coerces a boolean array to a boolean value.
     * A boolean array is false if the array is of length 0,
     * and true otherwise.
     *
     * @param self an array
     * @return the array's boolean value
     * @since 1.7.4
     */
    public static boolean asBoolean(boolean[] self) {
        return self != null && self.length > 0;
    }

    /**
     * Coerces a byte array to a boolean value.
     * A byte array is false if the array is null or of length 0,
     * and true otherwise.
     * <pre class="groovyTestCase">
     * byte[] array1 = []
     * assert !array1
     * byte[] array2 = [0]
     * assert array2
     * </pre>
     *
     * @param self an array
     * @return the array's boolean value
     * @since 1.7.4
     */
    public static boolean asBoolean(byte[] self) {
        return self != null && self.length > 0;
    }

    /**
     * Coerces a char array to a boolean value.
     * A char array is false if the array is of length 0,
     * and true otherwise.
     *
     * @param self an array
     * @return the array's boolean value
     * @since 1.7.4
     */
    public static boolean asBoolean(char[] self) {
        return self != null && self.length > 0;
    }

    /**
     * Coerces a short array to a boolean value.
     * A short array is false if the array is of length 0,
     * and true otherwise.
     *
     * @param self an array
     * @return the array's boolean value
     * @since 1.7.4
     */
    public static boolean asBoolean(short[] self) {
        return self != null && self.length > 0;
    }

    /**
     * Coerces an int array to a boolean value.
     * An int array is false if the array is of length 0,
     * and true otherwise.
     *
     * @param self an array
     * @return the array's boolean value
     * @since 1.7.4
     */
    public static boolean asBoolean(int[] self) {
        return self != null && self.length > 0;
    }

    /**
     * Coerces a long array to a boolean value.
     * A long array is false if the array is of length 0,
     * and true otherwise.
     *
     * @param self an array
     * @return the array's boolean value
     * @since 1.7.4
     */
    public static boolean asBoolean(long[] self) {
        return self != null && self.length > 0;
    }

    /**
     * Coerces a float array to a boolean value.
     * A float array is false if the array is of length 0,
     * and true otherwise.
     *
     * @param self an array
     * @return the array's boolean value
     * @since 1.7.4
     */
    public static boolean asBoolean(float[] self) {
        return self != null && self.length > 0;
    }

    /**
     * Coerces a double array to a boolean value.
     * A double array is false if the array is of length 0,
     * and true otherwise.
     *
     * @param self an array
     * @return the array's boolean value
     * @since 1.7.4
     */
    public static boolean asBoolean(double[] self) {
        return self != null && self.length > 0;
    }

    /**
     * Coerces an object array to a boolean value.
     * An Object array is false if the array is of length 0.
     * and to true otherwise
     *
     * @param self the array
     * @return the boolean value
     * @since 1.7.0
     */
    public static boolean asBoolean(Object[] self) {
        return self != null && self.length > 0;
    }

    //--------------------------------------------------------------------------
    // asType

    /**
     * Converts the given array to either a List, Set, or SortedSet. If the given class is
     * something else, the call is deferred to {@link DefaultGroovyMethods#asType(Object,Class)}.
     *
     * @param self an array
     * @param type the desired class
     * @return the object resulting from this type conversion
     * @since 1.5.1
     */
    @SuppressWarnings("unchecked")
    public static <T> T asType(Object[] self, Class<T> type) {
        if (type == List.class) {
            return (T) new ArrayList<>(Arrays.asList(self));
        }
        if (type == Set.class) {
            return (T) new HashSet<>(Arrays.asList(self));
        }
        if (type == SortedSet.class) {
            return (T) new TreeSet<>(Arrays.asList(self));
        }
        return DefaultGroovyMethods.asType((Object) self, type);
    }

    //--------------------------------------------------------------------------
    // average

    /**
     * Calculates the average of the bytes in the array.
     * <pre class="groovyTestCase">assert 5.0G == ([2,4,6,8] as byte[]).average()</pre>
     *
     * @param self The array of values to calculate the average of
     * @return The average of the items
     * @since 3.0.0
     */
    public static BigDecimal average(byte[] self) {
        Objects.requireNonNull(self);
        long s = 0;
        for (byte v : self) {
            s += v;
        }
        return (BigDecimal) NumberNumberDiv.div(BigDecimal.valueOf(s), self.length);
    }

    /**
     * Calculates the average of the shorts in the array.
     * <pre class="groovyTestCase">assert 5.0G == ([2,4,6,8] as short[]).average()</pre>
     *
     * @param self The array of values to calculate the average of
     * @return The average of the items
     * @since 3.0.0
     */
    public static BigDecimal average(short[] self) {
        Objects.requireNonNull(self);
        long s = 0;
        for (short v : self) {
            s += v;
        }
        return (BigDecimal) NumberNumberDiv.div(BigDecimal.valueOf(s), self.length);
    }

    /**
     * Calculates the average of the ints in the array.
     * <pre class="groovyTestCase">assert 5.0G == ([2,4,6,8] as int[]).average()</pre>
     *
     * @param self The array of values to calculate the average of
     * @return The average of the items
     * @since 3.0.0
     */
    public static BigDecimal average(int[] self) {
        Objects.requireNonNull(self);
        long s = 0;
        for (int v : self) {
            s += v;
        }
        return (BigDecimal) NumberNumberDiv.div(BigDecimal.valueOf(s), self.length);
    }

    /**
     * Calculates the average of the longs in the array.
     * <pre class="groovyTestCase">assert 5.0G == ([2,4,6,8] as long[]).average()</pre>
     *
     * @param self The array of values to calculate the average of
     * @return The average of the items
     * @since 3.0.0
     */
    public static BigDecimal average(long[] self) {
        Objects.requireNonNull(self);
        long s = 0;
        for (long v : self) {
            s += v;
        }
        return (BigDecimal) NumberNumberDiv.div(BigDecimal.valueOf(s), self.length);
    }

    /**
     * Calculates the average of the floats in the array.
     * <pre class="groovyTestCase">assert 5.0d == ([2,4,6,8] as float[]).average()</pre>
     *
     * @param self The array of values to calculate the average of
     * @return The average of the items
     * @since 3.0.0
     */
    public static double average(float[] self) {
        Objects.requireNonNull(self);
        double s = 0.0d;
        for (float v : self) {
            s += v;
        }
        return s / self.length;
    }

    /**
     * Calculates the average of the doubles in the array.
     * <pre class="groovyTestCase">assert 5.0d == ([2,4,6,8] as double[]).average()</pre>
     *
     * @param self The array of values to calculate the average of
     * @return The average of the items
     * @since 3.0.0
     */
    public static double average(double[] self) {
        Objects.requireNonNull(self);
        double s = 0.0d;
        for (double v : self) {
            s += v;
        }
        return s / self.length;
    }

    /**
     * Averages the items in an array. This is equivalent to invoking the
     * "plus" method on all items in the array and then dividing by the
     * total count using the "div" method for the resulting sum.
     * <pre class="groovyTestCase">
     * assert 3 == ([1, 2, 6] as Integer[]).average()
     * </pre>
     *
     * @param self The array of values to average
     * @return The average of all the items
     * @see #sum(java.lang.Object[])
     * @since 3.0.0
     */
    public static Object average(Object[] self) {
        Object result = sum(self);
        MetaClass metaClass = InvokerHelper.getMetaClass(result);
        result = metaClass.invokeMethod(result, "div", self.length);
        return result;
    }

    /**
     * Averages the result of applying a closure to each item of an array.
     * <code>array.average(closure)</code> is equivalent to:
     * <code>array.collect(closure).average()</code>.
     * <pre class="groovyTestCase">
     * def (nums, strings) = [[1, 3] as Integer[], ['to', 'from'] as String[]]
     * assert 20 == nums.average { it * 10 }
     * assert 3 == strings.average { it.size() }
     * assert 3 == strings.average (String::size)
     * </pre>
     *
     * @param self    An array
     * @param closure a single parameter closure that returns a (typically) numeric value.
     * @return The average of the values returned by applying the closure to each
     *         item of the array.
     * @since 3.0.0
     */
    public static <T> Object average(T[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        return DefaultGroovyMethods.average(new ArrayIterator<>(self), closure);
    }

    //--------------------------------------------------------------------------
    // chop

    /**
     * Chops the boolean array into pieces, returning lists with sizes corresponding to the supplied chop sizes.
     * If the array isn't large enough, truncated (possibly empty) pieces are returned.
     * Using a chop size of -1 will cause that piece to contain all remaining items from the array.
     * <pre class="groovyTestCase">
     * boolean[] array = [false, true, false]
     * assert array.chop(1, 2) == [[false], [true, false]]
     * </pre>
     *
     * @param self      a boolean Array to be chopped
     * @param chopSizes the sizes for the returned pieces
     * @return a list of lists chopping the original array elements into pieces determined by chopSizes
     * @see DefaultGroovyMethods#collate(Object[], int) to chop a list into pieces of a fixed size
     * @since 5.0.0
     */
    public static List<List<Boolean>> chop(boolean[] self, int... chopSizes) {
        return DefaultGroovyMethods.chop(new BooleanArrayIterator(self), chopSizes);
    }

    /**
     * Chops the byte array into pieces, returning lists with sizes corresponding to the supplied chop sizes.
     * If the array isn't large enough, truncated (possibly empty) pieces are returned.
     * Using a chop size of -1 will cause that piece to contain all remaining items from the array.
     * <pre class="groovyTestCase">
     * byte[] array = [0, 1, 2]
     * assert array.chop(1, 2) == [[0], [1, 2]]
     * </pre>
     *
     * @param self      a byte Array to be chopped
     * @param chopSizes the sizes for the returned pieces
     * @return a list of lists chopping the original array elements into pieces determined by chopSizes
     * @see DefaultGroovyMethods#collate(Object[], int) to chop a list into pieces of a fixed size
     * @since 5.0.0
     */
    public static List<List<Byte>> chop(byte[] self, int... chopSizes) {
        return DefaultGroovyMethods.chop(new ByteArrayIterator(self), chopSizes);
    }

    /**
     * Chops the char array into pieces, returning lists with sizes corresponding to the supplied chop sizes.
     * If the array isn't large enough, truncated (possibly empty) pieces are returned.
     * Using a chop size of -1 will cause that piece to contain all remaining items from the array.
     * <pre class="groovyTestCase">
     * char[] array = [0, 1, 2]
     * assert array.chop(1, 2) == [[0], [1, 2]]
     * </pre>
     *
     * @param self      a char Array to be chopped
     * @param chopSizes the sizes for the returned pieces
     * @return a list of lists chopping the original array elements into pieces determined by chopSizes
     * @see DefaultGroovyMethods#collate(Object[], int) to chop a list into pieces of a fixed size
     * @since 5.0.0
     */
    public static List<List<Character>> chop(char[] self, int... chopSizes) {
        return DefaultGroovyMethods.chop(new CharArrayIterator(self), chopSizes);
    }

    /**
     * Chops the short array into pieces, returning lists with sizes corresponding to the supplied chop sizes.
     * If the array isn't large enough, truncated (possibly empty) pieces are returned.
     * Using a chop size of -1 will cause that piece to contain all remaining items from the array.
     * <pre class="groovyTestCase">
     * short[] array = [0, 1, 2]
     * assert array.chop(1, 2) == [[0], [1, 2]]
     * </pre>
     *
     * @param self      a short Array to be chopped
     * @param chopSizes the sizes for the returned pieces
     * @return a list of lists chopping the original array elements into pieces determined by chopSizes
     * @see DefaultGroovyMethods#collate(Object[], int) to chop a list into pieces of a fixed size
     * @since 5.0.0
     */
    public static List<List<Short>> chop(short[] self, int... chopSizes) {
        return DefaultGroovyMethods.chop(new ShortArrayIterator(self), chopSizes);
    }

    /**
     * Chops the int array into pieces, returning lists with sizes corresponding to the supplied chop sizes.
     * If the array isn't large enough, truncated (possibly empty) pieces are returned.
     * Using a chop size of -1 will cause that piece to contain all remaining items from the array.
     * <pre class="groovyTestCase">
     * int[] array = [0, 1, 2]
     * assert array.chop(1, 2) == [[0], [1, 2]]
     * </pre>
     *
     * @param self      an int Array to be chopped
     * @param chopSizes the sizes for the returned pieces
     * @return a list of lists chopping the original array elements into pieces determined by chopSizes
     * @see DefaultGroovyMethods#collate(Object[], int) to chop a list into pieces of a fixed size
     * @since 5.0.0
     */
    public static List<List<Integer>> chop(int[] self, int... chopSizes) {
        return DefaultGroovyMethods.chop(new IntArrayIterator(self), chopSizes);
    }

    /**
     * Chops the long array into pieces, returning lists with sizes corresponding to the supplied chop sizes.
     * If the array isn't large enough, truncated (possibly empty) pieces are returned.
     * Using a chop size of -1 will cause that piece to contain all remaining items from the array.
     * <pre class="groovyTestCase">
     * long[] array = [0, 1, 2]
     * assert array.chop(1, 2) == [[0], [1, 2]]
     * </pre>
     *
     * @param self      a long Array to be chopped
     * @param chopSizes the sizes for the returned pieces
     * @return a list of lists chopping the original array elements into pieces determined by chopSizes
     * @see DefaultGroovyMethods#collate(Object[], int) to chop a list into pieces of a fixed size
     * @since 5.0.0
     */
    public static List<List<Long>> chop(long[] self, int... chopSizes) {
        return DefaultGroovyMethods.chop(new LongArrayIterator(self), chopSizes);
    }

    /**
     * Chops the float array into pieces, returning lists with sizes corresponding to the supplied chop sizes.
     * If the array isn't large enough, truncated (possibly empty) pieces are returned.
     * Using a chop size of -1 will cause that piece to contain all remaining items from the array.
     * <pre class="groovyTestCase">
     * float[] array = [0, 1, 2]
     * assert array.chop(1, 2) == [[0], [1, 2]]
     * </pre>
     *
     * @param self      a float Array to be chopped
     * @param chopSizes the sizes for the returned pieces
     * @return a list of lists chopping the original array elements into pieces determined by chopSizes
     * @see DefaultGroovyMethods#collate(Object[], int) to chop a list into pieces of a fixed size
     * @since 5.0.0
     */
    public static List<List<Float>> chop(float[] self, int... chopSizes) {
        return DefaultGroovyMethods.chop(new FloatArrayIterator(self), chopSizes);
    }

    /**
     * Chops the double array into pieces, returning lists with sizes corresponding to the supplied chop sizes.
     * If the array isn't large enough, truncated (possibly empty) pieces are returned.
     * Using a chop size of -1 will cause that piece to contain all remaining items from the array.
     * <pre class="groovyTestCase">
     * double[] array = [0, 1, 2]
     * assert array.chop(1, 2) == [[0], [1, 2]]
     * </pre>
     *
     * @param self      a double Array to be chopped
     * @param chopSizes the sizes for the returned pieces
     * @return a list of lists chopping the original array elements into pieces determined by chopSizes
     * @see DefaultGroovyMethods#collate(Object[], int) to chop a list into pieces of a fixed size
     * @since 5.0.0
     */
    public static List<List<Double>> chop(double[] self, int... chopSizes) {
        return DefaultGroovyMethods.chop(new DoubleArrayIterator(self), chopSizes);
    }

    //--------------------------------------------------------------------------
    // collate

    //--------------------------------------------------------------------------
    // collect

    //--------------------------------------------------------------------------
    // collectEntries

    //--------------------------------------------------------------------------
    // collectMany

    //--------------------------------------------------------------------------
    // contains

    /**
     * Checks whether the array contains the given value.
     *
     * @param self  the array we are searching
     * @param value the value being searched for
     * @return true if the array contains the value
     * @since 1.8.6
     */
    public static boolean contains(boolean[] self, Object value) {
        Objects.requireNonNull(self);
        for (boolean next : self) {
            if (DefaultTypeTransformation.compareEqual(value, next)) return true;
        }
        return false;
    }

    /**
     * Checks whether the array contains the given value.
     *
     * @param self  the array we are searching
     * @param value the value being searched for
     * @return true if the array contains the value
     * @since 1.8.6
     */
    public static boolean contains(byte[] self, Object value) {
        Objects.requireNonNull(self);
        for (byte next : self) {
            if (DefaultTypeTransformation.compareEqual(value, next)) return true;
        }
        return false;
    }

    /**
     * Checks whether the array contains the given value.
     *
     * @param self  the array we are searching
     * @param value the value being searched for
     * @return true if the array contains the value
     * @since 1.8.6
     */
    public static boolean contains(char[] self, Object value) {
        Objects.requireNonNull(self);
        for (char next : self) {
            if (DefaultTypeTransformation.compareEqual(value, next)) return true;
        }
        return false;
    }

    /**
     * Checks whether the array contains the given value.
     *
     * @param self  the array we are searching
     * @param value the value being searched for
     * @return true if the array contains the value
     * @since 1.8.6
     */
    public static boolean contains(short[] self, Object value) {
        Objects.requireNonNull(self);
        for (short next : self) {
            if (DefaultTypeTransformation.compareEqual(value, next)) return true;
        }
        return false;
    }

    /**
     * Checks whether the array contains the given value.
     *
     * @param self  the array we are searching
     * @param value the value being searched for
     * @return true if the array contains the value
     * @since 1.8.6
     */
    public static boolean contains(int[] self, Object value) {
        Objects.requireNonNull(self);
        for (int next : self) {
            if (DefaultTypeTransformation.compareEqual(value, next)) return true;
        }
        return false;
    }

    /**
     * Checks whether the array contains the given value.
     *
     * @param self  the array we are searching
     * @param value the value being searched for
     * @return true if the array contains the value
     * @since 1.8.6
     */
    public static boolean contains(long[] self, Object value) {
        Objects.requireNonNull(self);
        for (long next : self) {
            if (DefaultTypeTransformation.compareEqual(value, next)) return true;
        }
        return false;
    }

    /**
     * Checks whether the array contains the given value.
     *
     * @param self  the array we are searching
     * @param value the value being searched for
     * @return true if the array contains the value
     * @since 1.8.6
     */
    public static boolean contains(float[] self, Object value) {
        Objects.requireNonNull(self);
        for (float next : self) {
            if (DefaultTypeTransformation.compareEqual(value, next)) return true;
        }
        return false;
    }

    /**
     * Checks whether the array contains the given value.
     *
     * @param self  the array we are searching
     * @param value the value being searched for
     * @return true if the array contains the value
     * @since 1.8.6
     */
    public static boolean contains(double[] self, Object value) {
        Objects.requireNonNull(self);
        for (double next : self) {
            if (DefaultTypeTransformation.compareEqual(value, next)) return true;
        }
        return false;
    }

    //--------------------------------------------------------------------------
    // count

    /**
     * Counts the number of occurrences of the given value inside this array.
     * Comparison is done using Groovy's == operator (using
     * <code>compareTo(value) == 0</code>).
     * <pre class="groovyTestCase">
     * boolean[] array = [false, true, true]
     * assert array.count(true) == 2
     * </pre>
     *
     * @param self  the array within which we count the number of occurrences
     * @param value the value being searched for
     * @return the number of occurrences
     * @see DefaultGroovyMethods#count(Iterator, Object)
     * @since 1.6.4
     */
    public static Number count(boolean[] self, Object value) {
        return DefaultGroovyMethods.count(new BooleanArrayIterator(self), value);
    }

    /**
     * Counts the number of occurrences of the given value inside this array.
     * Comparison is done using Groovy's == operator (using
     * <code>compareTo(value) == 0</code>).
     * <pre class="groovyTestCase">
     * byte[] array = [10, 20, 20, 30]
     * assert array.count(20) == 2
     * </pre>
     *
     * @param self  the array within which we count the number of occurrences
     * @param value the value being searched for
     * @return the number of occurrences
     * @see DefaultGroovyMethods#count(Iterator, Object)
     * @since 1.6.4
     */
    public static Number count(byte[] self, Object value) {
        return DefaultGroovyMethods.count(new ByteArrayIterator(self), value);
    }

    /**
     * Counts the number of occurrences of the given value inside this array.
     * Comparison is done using Groovy's == operator (using
     * <code>compareTo(value) == 0</code>).
     * <pre class="groovyTestCase">
     * char[] array = ['x', 'y', 'z', 'z', 'y']
     * assert array.count('y') == 2
     * </pre>
     *
     * @param self  the array within which we count the number of occurrences
     * @param value the value being searched for
     * @return the number of occurrences
     * @see DefaultGroovyMethods#count(Iterator, Object)
     * @since 1.6.4
     */
    public static Number count(char[] self, Object value) {
        return DefaultGroovyMethods.count(new CharArrayIterator(self), value);
    }

    /**
     * Counts the number of occurrences of the given value inside this array.
     * Comparison is done using Groovy's == operator (using
     * <code>compareTo(value) == 0</code>).
     * <pre class="groovyTestCase">
     * short[] array = [10, 20, 20, 30]
     * assert array.count(20) == 2
     * </pre>
     *
     * @param self  the array within which we count the number of occurrences
     * @param value the value being searched for
     * @return the number of occurrences
     * @see DefaultGroovyMethods#count(Iterator, Object)
     * @since 1.6.4
     */
    public static Number count(short[] self, Object value) {
        return DefaultGroovyMethods.count(new ShortArrayIterator(self), value);
    }

    /**
     * Counts the number of occurrences of the given value inside this array.
     * Comparison is done using Groovy's == operator (using
     * <code>compareTo(value) == 0</code>).
     * <pre class="groovyTestCase">
     * int[] array = [10, 20, 20, 30]
     * assert array.count(20) == 2
     * </pre>
     *
     * @param self  the array within which we count the number of occurrences
     * @param value the value being searched for
     * @return the number of occurrences
     * @see DefaultGroovyMethods#count(Iterator, Object)
     * @since 1.6.4
     */
    public static Number count(int[] self, Object value) {
        return DefaultGroovyMethods.count(new IntArrayIterator(self), value);
    }

    /**
     * Counts the number of occurrences of the given value inside this array.
     * Comparison is done using Groovy's == operator (using
     * <code>compareTo(value) == 0</code>).
     * <pre class="groovyTestCase">
     * long[] array = [10L, 20L, 20L, 30L]
     * assert array.count(20L) == 2
     * </pre>
     *
     * @param self  the array within which we count the number of occurrences
     * @param value the value being searched for
     * @return the number of occurrences
     * @see DefaultGroovyMethods#count(Iterator, Object)
     * @since 1.6.4
     */
    public static Number count(long[] self, Object value) {
        return DefaultGroovyMethods.count(new LongArrayIterator(self), value);
    }

    /**
     * Counts the number of occurrences of the given value inside this array.
     * Comparison is done using Groovy's == operator (using
     * <code>compareTo(value) == 0</code>).
     * <pre class="groovyTestCase">
     * float[] array = [10.0f, 20.0f, 20.0f, 30.0f]
     * assert array.count(20.0f) == 2
     * </pre>
     *
     * @param self  the array within which we count the number of occurrences
     * @param value the value being searched for
     * @return the number of occurrences
     * @see DefaultGroovyMethods#count(Iterator, Object)
     * @since 1.6.4
     */
    public static Number count(float[] self, Object value) {
        return DefaultGroovyMethods.count(new FloatArrayIterator(self), value);
    }

    /**
     * Counts the number of occurrences of the given value inside this array.
     * Comparison is done using Groovy's == operator (using
     * <code>compareTo(value) == 0</code>).
     * <pre class="groovyTestCase">
     * double[] array = [10.0d, 20.0d, 20.0d, 30.0d]
     * assert array.count(20.0d) == 2
     * </pre>
     *
     * @param self  the array within which we count the number of occurrences
     * @param value the value being searched for
     * @return the number of occurrences
     * @see DefaultGroovyMethods#count(Iterator, Object)
     * @since 1.6.4
     */
    public static Number count(double[] self, Object value) {
        return DefaultGroovyMethods.count(new DoubleArrayIterator(self), value);
    }

    /**
     * Counts the number of occurrences of the given value inside this array.
     * Comparison is done using Groovy's == operator (using
     * <code>compareTo(value) == 0</code> or <code>equals(value)</code> ).
     *
     * @param self  the array within which we count the number of occurrences
     * @param value the value being searched for
     * @return the number of occurrences
     * @since 1.6.4
     */
    public static Number count(Object[] self, Object value) {
        return DefaultGroovyMethods.count(Arrays.asList(self), value);
    }

    /**
     * Counts the number of occurrences which satisfy the given closure from inside this array.
     *
     * @param self      the array within which we count the number of occurrences
     * @param predicate a closure condition
     * @return the number of occurrences
     * @since 1.8.0
     */
    public static <T> Number count(T[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        return DefaultGroovyMethods.count(Arrays.asList(self), predicate);
    }

    //-------------------------------------------------------------------------
    // countBy

    /**
     * Sorts all array members into groups determined by the supplied mapping
     * closure and counts the group size.  The closure should return the key that each
     * item should be grouped by.  The returned Map will have an entry for each
     * distinct key returned from the closure, with each value being the frequency of
     * items occurring for that group.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">assert ([1,2,2,2,3] as Object[]).countBy{ it % 2 } == [1:2, 0:3]</pre>
     *
     * @param self    an array to group and count
     * @param closure a closure mapping items to the frequency keys
     * @return a new Map grouped by keys with frequency counts
     * @see DefaultGroovyMethods#countBy(Iterator, Closure)
     * @since 1.8.0
     */
    public static <K,E> Map<K, Integer> countBy(E[] self, @ClosureParams(FirstParam.Component.class) Closure<K> closure) {
        return DefaultGroovyMethods.countBy(new ArrayIterator<>(self), closure);
    }

    //-------------------------------------------------------------------------
    // drop

    //--------------------------------------------------------------------------
    // dropRight

    //--------------------------------------------------------------------------
    // dropWhile

    //--------------------------------------------------------------------------
    // each

    /**
     * Iterates through a boolean[] passing each boolean to the given consumer.
     * <pre class="groovyTestCase">
     * boolean[] array = [false, true, false]
     * String result = ''
     * array.each{ result += it.toString()[0] }
     * assert result == 'ftf'
     * </pre>
     *
     * @param self     the boolean array over which we iterate
     * @param consumer the consumer for each boolean
     * @return the self array
     * @since 5.0.0
     */
    @Incubating
    public static boolean[] each(boolean[] self, Consumer<Boolean> consumer) {
        Objects.requireNonNull(self);
        for (boolean item : self) {
            consumer.accept(item);
        }
        return self;
    }

    /**
     * Iterates through a byte[] passing each byte to the given consumer.
     * <pre class="groovyTestCase">
     * byte[] array = [0, 1, 2]
     * String result = ''
     * array.each{ result += it }
     * assert result == '012'
     * </pre>
     *
     * @param self     the byte array over which we iterate
     * @param consumer the consumer for each byte
     * @return the self array
     * @since 5.0.0
     */
    @Incubating
    public static byte[] each(byte[] self, Consumer<Byte> consumer) {
        Objects.requireNonNull(self);
        for (byte item : self) {
            consumer.accept(item);
        }
        return self;
    }

    /**
     * Iterates through a char[] passing each char to the given consumer.
     * <pre class="groovyTestCase">
     * char[] array = ['a' as char, 'b' as char, 'c' as char]
     * String result = ''
     * array.each{ result += it }
     * assert result == 'abc'
     * </pre>
     *
     * @param self     the char array over which we iterate
     * @param consumer the consumer for each char
     * @return the self array
     * @since 5.0.0
     */
    @Incubating
    public static char[] each(char[] self, Consumer<Character> consumer) {
        Objects.requireNonNull(self);
        for (char item : self) {
            consumer.accept(item);
        }
        return self;
    }

    /**
     * Iterates through a short[] passing each short to the given consumer.
     * <pre class="groovyTestCase">
     * short[] array = [0, 1, 2]
     * String result = ''
     * array.each{ result += it }
     * assert result == '012'
     * </pre>
     *
     * @param self     the short array over which we iterate
     * @param consumer the consumer for each short
     * @return the self array
     * @since 5.0.0
     */
    @Incubating
    public static short[] each(short[] self, Consumer<Short> consumer) {
        Objects.requireNonNull(self);
        for (short item : self) {
            consumer.accept(item);
        }
        return self;
    }

    /**
     * Iterates through an int[] passing each int to the given consumer.
     * <pre class="groovyTestCase">
     * int[] array = [0, 1, 2]
     * String result = ''
     * array.each{ result += it }
     * assert result == '012'
     * </pre>
     *
     * @param self     the int array over which we iterate
     * @param consumer the consumer for each int
     * @return the self array
     * @since 5.0.0
     */
    @Incubating
    public static int[] each(int[] self, IntConsumer consumer) {
        Objects.requireNonNull(self);
        for (int item : self) {
            consumer.accept(item);
        }
        return self;
    }

    /**
     * Iterates through a long[] passing each long to the given consumer.
     * <pre class="groovyTestCase">
     * long[] array = [0L, 1L, 2L]
     * String result = ''
     * array.each{ result += it }
     * assert result == '012'
     * </pre>
     *
     * @param self     the long array over which we iterate
     * @param consumer the consumer for each long
     * @return the self array
     * @since 5.0.0
     */
    @Incubating
    public static long[] each(long[] self, LongConsumer consumer) {
        Objects.requireNonNull(self);
        for (long item : self) {
            consumer.accept(item);
        }
        return self;
    }

    /**
     * Iterates through a float[] passing each float to the given consumer.
     * <pre class="groovyTestCase">
     * float[] array = [0f, 1f, 2f]
     * String result = ''
     * array.each{ result += it }
     * assert result == '0.01.02.0'
     * </pre>
     *
     * @param self     the float array over which we iterate
     * @param consumer the consumer for each float
     * @return the self array
     * @since 5.0.0
     */
    @Incubating
    public static float[] each(float[] self, Consumer<Float> consumer) {
        Objects.requireNonNull(self);
        for (float item : self) {
            consumer.accept(item);
        }
        return self;
    }

    /**
     * Iterates through a double[] passing each double to the given consumer.
     * <pre class="groovyTestCase">
     * double[] array = [0d, 1d, 2d]
     * String result = ''
     * array.each{ result += it }
     * assert result == '0.01.02.0'
     * </pre>
     *
     * @param self     the double array over which we iterate
     * @param consumer the consumer for each double
     * @return the self array
     * @since 5.0.0
     */
    @Incubating
    public static double[] each(double[] self, DoubleConsumer consumer) {
        Objects.requireNonNull(self);
        for (double item : self) {
            consumer.accept(item);
        }
        return self;
    }

    /**
     * Iterates through an array passing each array entry to the given closure.
     * <pre class="groovyTestCase">
     * String[] letters = ['a', 'b', 'c']
     * String result = ''
     * letters.each{ result += it }
     * assert result == 'abc'
     * </pre>
     *
     * @param self    the array over which we iterate
     * @param closure the closure applied on each array entry
     * @return the self array
     * @since 2.5.0
     */
    public static <T> T[] each(T[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        for (T item : self) {
            closure.call(item);
        }
        return self;
    }

    //--------------------------------------------------------------------------
    // eachByte

    /**
     * Traverses through each byte of this byte array.
     *
     * @param self    a byte array
     * @param closure a closure
     * @since 1.5.5
     */
    public static void eachByte(byte[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        Objects.requireNonNull(self);
        for (double item : self) {
            closure.call(item);
        }
    }

    /**
     * Traverses through each byte of this Byte array. Alias for each.
     *
     * @param self    a Byte array
     * @param closure a closure
     * @since 1.5.5
     */
    public static void eachByte(Byte[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        each(self, closure);
    }

    //--------------------------------------------------------------------------
    // eachWithIndex

    /**
     * Iterates through a boolean[],
     * passing each boolean and the element's index (a counter starting at
     * zero) to the given closure.
     * <pre class="groovyTestCase">
     * boolean[] array = [false, true, false]
     * String result = ''
     * array.eachWithIndex{ item, index {@code ->} result += "$index($item)" }
     * assert result == '0(false)1(true)2(false)'
     * </pre>
     *
     * @param self    a boolean array
     * @param closure a Closure to operate on each boolean
     * @return the self array
     * @since 5.0.0
     */
    public static boolean[] eachWithIndex(boolean[] self, @ClosureParams(value=FromString.class,options="Boolean,Integer") Closure<?> closure) {
        Objects.requireNonNull(self);
        final Object[] args = new Object[2];
        for (int i = 0, n = self.length; i < n; i += 1) {
            args[0] = self[i];
            args[1] = i;
            closure.call(args);
        }
        return self;
    }

    /**
     * Iterates through a byte[],
     * passing each byte and the element's index (a counter starting at
     * zero) to the given closure.
     * <pre class="groovyTestCase">
     * byte[] array = [10, 20, 30]
     * String result = ''
     * array.eachWithIndex{ item, index {@code ->} result += "$index($item)" }
     * assert result == '0(10)1(20)2(30)'
     * </pre>
     *
     * @param self    a byte array
     * @param closure a Closure to operate on each byte
     * @return the self array
     * @since 5.0.0
     */
    public static byte[] eachWithIndex(byte[] self, @ClosureParams(value=FromString.class,options="Byte,Integer") Closure<?> closure) {
        Objects.requireNonNull(self);
        final Object[] args = new Object[2];
        for (int i = 0, n = self.length; i < n; i += 1) {
            args[0] = self[i];
            args[1] = i;
            closure.call(args);
        }
        return self;
    }

    /**
     * Iterates through a char[],
     * passing each char and the element's index (a counter starting at
     * zero) to the given closure.
     * <pre class="groovyTestCase">
     * char[] array = ['a' as char, 'b' as char, 'c' as char]
     * String result = ''
     * array.eachWithIndex{ item, index {@code ->} result += "$index($item)" }
     * assert result == '0(a)1(b)2(c)'
     * </pre>
     *
     * @param self    a char array
     * @param closure a Closure to operate on each char
     * @return the self array
     * @since 5.0.0
     */
    public static char[] eachWithIndex(char[] self, @ClosureParams(value=FromString.class,options="Character,Integer") Closure<?> closure) {
        Objects.requireNonNull(self);
        final Object[] args = new Object[2];
        for (int i = 0, n = self.length; i < n; i += 1) {
            args[0] = self[i];
            args[1] = i;
            closure.call(args);
        }
        return self;
    }

    /**
     * Iterates through a short[],
     * passing each short and the element's index (a counter starting at
     * zero) to the given closure.
     * <pre class="groovyTestCase">
     * short[] array = [10, 20, 30]
     * String result = ''
     * array.eachWithIndex{ item, index {@code ->} result += "$index($item)" }
     * assert result == '0(10)1(20)2(30)'
     * </pre>
     *
     * @param self    a short array
     * @param closure a Closure to operate on each short
     * @return the self array
     * @since 5.0.0
     */
    public static short[] eachWithIndex(short[] self, @ClosureParams(value=FromString.class,options="Short,Integer") Closure<?> closure) {
        Objects.requireNonNull(self);
        final Object[] args = new Object[2];
        for (int i = 0, n = self.length; i < n; i += 1) {
            args[0] = self[i];
            args[1] = i;
            closure.call(args);
        }
        return self;
    }

    /**
     * Iterates through an int[],
     * passing each int and the element's index (a counter starting at
     * zero) to the given closure.
     * <pre class="groovyTestCase">
     * int[] array = [10, 20, 30]
     * String result = ''
     * array.eachWithIndex{ item, index {@code ->} result += "$index($item)" }
     * assert result == '0(10)1(20)2(30)'
     * </pre>
     *
     * @param self    an int array
     * @param closure a Closure to operate on each int
     * @return the self array
     * @since 5.0.0
     */
    public static int[] eachWithIndex(int[] self, @ClosureParams(value=FromString.class,options="Integer,Integer") Closure<?> closure) {
        Objects.requireNonNull(self);
        final Object[] args = new Object[2];
        for (int i = 0, n = self.length; i < n; i += 1) {
            args[0] = self[i];
            args[1] = i;
            closure.call(args);
        }
        return self;
    }

    /**
     * Iterates through a long[],
     * passing each long and the element's index (a counter starting at
     * zero) to the given closure.
     * <pre class="groovyTestCase">
     * long[] array = [10L, 20L, 30L]
     * String result = ''
     * array.eachWithIndex{ item, index {@code ->} result += "$index($item)" }
     * assert result == '0(10)1(20)2(30)'
     * </pre>
     *
     * @param self    a long array
     * @param closure a Closure to operate on each long
     * @return the self array
     * @since 5.0.0
     */
    public static long[] eachWithIndex(long[] self, @ClosureParams(value=FromString.class,options="Long,Integer") Closure<?> closure) {
        Objects.requireNonNull(self);
        final Object[] args = new Object[2];
        for (int i = 0, n = self.length; i < n; i += 1) {
            args[0] = self[i];
            args[1] = i;
            closure.call(args);
        }
        return self;
    }

    /**
     * Iterates through a float[],
     * passing each float and the element's index (a counter starting at
     * zero) to the given closure.
     * <pre class="groovyTestCase">
     * float[] array = [10f, 20f, 30f]
     * String result = ''
     * array.eachWithIndex{ item, index {@code ->} result += "$index($item)" }
     * assert result == '0(10.0)1(20.0)2(30.0)'
     * </pre>
     *
     * @param self    a float array
     * @param closure a Closure to operate on each float
     * @return the self array
     * @since 5.0.0
     */
    public static float[] eachWithIndex(float[] self, @ClosureParams(value=FromString.class,options="Float,Integer") Closure<?> closure) {
        Objects.requireNonNull(self);
        final Object[] args = new Object[2];
        for (int i = 0, n = self.length; i < n; i += 1) {
            args[0] = self[i];
            args[1] = i;
            closure.call(args);
        }
        return self;
    }

    /**
     * Iterates through a double[],
     * passing each double and the element's index (a counter starting at
     * zero) to the given closure.
     * <pre class="groovyTestCase">
     * double[] array = [10d, 20d, 30d]
     * String result = ''
     * array.eachWithIndex{ item, index {@code ->} result += "$index($item)" }
     * assert result == '0(10.0)1(20.0)2(30.0)'
     * </pre>
     *
     * @param self    a double array
     * @param closure a Closure to operate on each double
     * @return the self array
     * @since 5.0.0
     */
    public static double[] eachWithIndex(double[] self, @ClosureParams(value=FromString.class,options="Double,Integer") Closure<?> closure) {
        Objects.requireNonNull(self);
        final Object[] args = new Object[2];
        for (int i = 0, n = self.length; i < n; i += 1) {
            args[0] = self[i];
            args[1] = i;
            closure.call(args);
        }
        return self;
    }

    /**
     * Iterates through an array, passing each array element and the element's
     * index (a counter starting at zero) to the given closure.
     * <pre class="groovyTestCase">
     * String[] letters = ['a', 'b', 'c']
     * String result = ''
     * letters.eachWithIndex{ letter, index {@code ->} result += "$index:$letter" }
     * assert result == '0:a1:b2:c'
     * </pre>
     *
     * @param self    an array
     * @param closure a Closure to operate on each array entry
     * @return the self array
     * @since 2.5.0
     */
    public static <T> T[] eachWithIndex(T[] self, @ClosureParams(value=FromString.class,options="T,Integer") Closure<?> closure) {
        final Object[] args = new Object[2];
        int counter = 0;
        for(T item : self) {
            args[0] = item;
            args[1] = counter++;
            closure.call(args);
        }
        return self;
    }

    //--------------------------------------------------------------------------
    // equals

    /**
     * Compare the contents of this array to the contents of the given array.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * boolean[] array1 = [true, false]
     * boolean[] array2 = [true, false]
     * assert array1 !== array2
     * assert array1.equals(array2)
     * </pre>
     *
     * @param self  a boolean array
     * @param right the array being compared
     * @return true if the contents of both arrays are equal.
     * @since 5.0.0
     */
    public static boolean equals(boolean[] self, boolean[] right) {
        return Arrays.equals(self, right);
    }

    /**
     * Compare the contents of this array to the contents of the given array.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * byte[] array1 = [4, 8]
     * byte[] array2 = [4, 8]
     * assert array1 !== array2
     * assert array1.equals(array2)
     * </pre>
     *
     * @param self  a byte array
     * @param right the array being compared
     * @return true if the contents of both arrays are equal.
     * @since 5.0.0
     */
    public static boolean equals(byte[] self, byte[] right) {
        return Arrays.equals(self, right);
    }

    /**
     * Compare the contents of this array to the contents of the given array.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * char[] array1 = ['a', 'b']
     * char[] array2 = ['a', 'b']
     * assert array1 !== array2
     * assert array1.equals(array2)
     * </pre>
     *
     * @param self  a char array
     * @param right the array being compared
     * @return true if the contents of both arrays are equal.
     * @since 5.0.0
     */
    public static boolean equals(char[] self, char[] right) {
        return Arrays.equals(self, right);
    }

    /**
     * Compare the contents of this array to the contents of the given array.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * short[] array1 = [4, 8]
     * short[] array2 = [4, 8]
     * assert array1 !== array2
     * assert array1.equals(array2)
     * </pre>
     *
     * @param self  a short array
     * @param right the array being compared
     * @return true if the contents of both arrays are equal.
     * @since 5.0.0
     */
    public static boolean equals(short[] self, short[] right) {
        return Arrays.equals(self, right);
    }

    /**
     * Compare the contents of this array to the contents of the given array.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * int[] array1 = [4, 8]
     * int[] array2 = [4, 8]
     * assert array1 !== array2
     * assert array1.equals(array2)
     * </pre>
     *
     * @param self  an int array
     * @param right the array being compared
     * @return true if the contents of both arrays are equal.
     * @since 5.0.0
     */
    public static boolean equals(int[] self, int[] right) {
        return Arrays.equals(self, right);
    }

    /**
     * Compare the contents of this array to the contents of the given array.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * long[] array1 = [4L, 8L]
     * long[] array2 = [4L, 8L]
     * assert array1 !== array2
     * assert array1.equals(array2)
     * </pre>
     *
     * @param self  a long array
     * @param right the array being compared
     * @return true if the contents of both arrays are equal.
     * @since 5.0.0
     */
    public static boolean equals(long[] self, long[] right) {
        return Arrays.equals(self, right);
    }

    /**
     * Compare the contents of this array to the contents of the given array.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * float[] array1 = [4.0f, 8.0f]
     * float[] array2 = [4.0f, 8.0f]
     * assert array1 !== array2
     * assert array1.equals(array2)
     * </pre>
     *
     * @param self  a float array
     * @param right the array being compared
     * @return true if the contents of both arrays are equal.
     * @since 5.0.0
     */
    public static boolean equals(float[] self, float[] right) {
        return Arrays.equals(self, right);
    }

    /**
     * Compare the contents of this array to the contents of the given array.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * double[] array1 = [4.0d, 8.0d]
     * double[] array2 = [4.0d, 8.0d]
     * assert array1 !== array2
     * assert array1.equals(array2)
     * </pre>
     *
     * @param self  a double array
     * @param right the array being compared
     * @return true if the contents of both arrays are equal.
     * @since 5.0.0
     */
    public static boolean equals(double[] self, double[] right) {
        return Arrays.equals(self, right);
    }

    //--------------------------------------------------------------------------
    // every

    /**
     * Iterates over the contents of a boolean Array, and checks whether
     * every element is true.
     * <pre class="groovyTestCase">
     * boolean[] array1 = [false, true]
     * assert !array1.every()
     * boolean[] array2 = [true, true]
     * assert array2.every()
     * </pre>
     *
     * @param self the boolean array over which we iterate
     * @return true if no boolean is false
     * @since 5.0.0
     */
    public static boolean every(boolean[] self) {
        Objects.requireNonNull(self);
        for (boolean item : self) {
            if (!item) return false;
        }
        return true;
    }

    /**
     * Iterates over the contents of a boolean Array, and checks whether a
     * predicate is valid for every element.
     * <pre class="groovyTestCase">
     * boolean[] array = [true]
     * assert array.every{ it }
     * assert !array.every{ !it }
     * </pre>
     *
     * @param self      the boolean array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if the predicate returns true for all elements in the array
     * @since 5.0.0
     */
    public static boolean every(boolean[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (boolean item : self) {
            if (!bcw.call(item)) return false;
        }
        return true;
    }

    /**
     * Iterates over the contents of a byte Array, and checks whether a
     * predicate is valid for all elements.
     * <pre class="groovyTestCase">
     * byte[] array = [0, 1, 2]
     * assert array.every{ it &lt; 3 }
     * assert !array.every{ it > 1 }
     * </pre>
     *
     * @param self      the byte array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if the predicate returns true for all elements in the array
     * @since 5.0.0
     */
    public static boolean every(byte[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (byte item : self) {
            if (!bcw.call(item)) return false;
        }
        return true;
    }

    /**
     * Iterates over the contents of a char Array, and checks whether a
     * predicate is valid for all elements.
     * <pre class="groovyTestCase">
     * char[] array = ['a', 'b', 'c']
     * assert array.every{ it &lt;= 'd' }
     * assert !array.every{ it > 'b' }
     * </pre>
     *
     * @param self      the char array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if the predicate returns true for all elements in the array
     * @since 5.0.0
     */
    public static boolean every(char[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (char item : self) {
            if (!bcw.call(item)) return false;
        }
        return true;
    }

    /**
     * Iterates over the contents of a short Array, and checks whether a
     * predicate is valid for all elements.
     * <pre class="groovyTestCase">
     * short[] array = [0, 1, 2]
     * assert array.every{ it &lt; 3 }
     * assert !array.every{ it > 1 }
     * </pre>
     *
     * @param self      the char array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if the predicate returns true for all elements in the array
     * @since 5.0.0
     */
    public static boolean every(short[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (short item : self) {
            if (!bcw.call(item)) return false;
        }
        return true;
    }

    /**
     * Iterates over the contents of an int Array, and checks whether a
     * predicate is valid for all elements.
     * <pre class="groovyTestCase">
     * int[] array = [0, 1, 2]
     * assert array.every{ it &lt; 3 }
     * assert !array.every{ it > 1 }
     * </pre>
     *
     * @param self      the int array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if the predicate returns true for all elements in the array
     * @since 5.0.0
     */
    public static boolean every(int[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (int item : self) {
            if (!bcw.call(item)) return false;
        }
        return true;
    }

    /**
     * Iterates over the contents of a long Array, and checks whether a
     * predicate is valid for all elements.
     * <pre class="groovyTestCase">
     * long[] array = [0L, 1L, 2L]
     * assert array.every{ it &lt; 3L }
     * assert !array.every{ it > 1L }
     * </pre>
     *
     * @param self      the long array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if the predicate returns true for all elements in the array
     * @since 5.0.0
     */
    public static boolean every(long[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (long item : self) {
            if (!bcw.call(item)) return false;
        }
        return true;
    }

    /**
     * Iterates over the contents of a float Array, and checks whether a
     * predicate is valid for all elements.
     * <pre class="groovyTestCase">
     * float[] array = [0.0f, 1.0f, 2.0f]
     * assert array.every{ it &lt; 2.5f }
     * assert !array.every{ it > 1.5f }
     * </pre>
     *
     * @param self      the float array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if the predicate returns true for all elements in the array
     * @since 5.0.0
     */
    public static boolean every(float[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (float item : self) {
            if (!bcw.call(item)) return false;
        }
        return true;
    }

    /**
     * Iterates over the contents of a double Array, and checks whether a
     * predicate is valid for all elements.
     * <pre class="groovyTestCase">
     * double[] array = [0.0d, 1.0d, 2.0d]
     * assert array.every{ it &lt; 2.5d }
     * assert !array.every{ it > 1.5d }
     * </pre>
     *
     * @param self      the double array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if the predicate returns true for all elements in the array
     * @since 5.0.0
     */
    public static boolean every(double[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (double item : self) {
            if (!bcw.call(item)) return false;
        }
        return true;
    }

    /**
     * Used to determine if the given predicate closure is valid (i.e. returns
     * <code>true</code> for all items in this Array).
     *
     * @param self      an array
     * @param predicate the closure predicate used for matching
     * @return true if the predicate returns true for all elements in the array
     * @since 2.5.0
     */
    public static <T> boolean every(T[] self, @ClosureParams(FirstParam.Component.class) Closure<?> predicate) {
        Objects.requireNonNull(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (T item : self) {
            if (!bcw.call(item)) return false;
        }
        return true;
    }

    //--------------------------------------------------------------------------
    // find

    //--------------------------------------------------------------------------
    // findAll

    //--------------------------------------------------------------------------
    // findIndexOf

    /**
     * Iterates over the elements of an array and returns the index of the first
     * item that satisfies the condition specified by the closure.
     *
     * @param self      an array
     * @param condition the matching condition
     * @return an integer that is the index of the first matched object or -1 if no match was found
     * @since 2.5.0
     */
    public static <T> int findIndexOf(T[] self, @ClosureParams(FirstParam.Component.class) Closure<?> condition) {
        return findIndexOf(self, 0, condition);
    }

    /**
     * Iterates over the elements of an array, starting from a specified index,
     * and returns the index of the first item that satisfies the condition
     * specified by the closure.
     *
     * @param self       an array
     * @param startIndex start matching from this index
     * @param condition  the matching condition
     * @return an integer that is the index of the first matched object or -1 if no match was found
     * @since 2.5.0
     */
    public static <T> int findIndexOf(T[] self, int startIndex, @ClosureParams(FirstParam.Component.class) Closure<?> condition) {
        return DefaultGroovyMethods.findIndexOf(new ArrayIterator<>(self), startIndex, condition);
    }

    //--------------------------------------------------------------------------
    // findIndexValues

    /**
     * Iterates over the elements of an array and returns the index values of
     * the items that match the condition specified in the closure.
     *
     * @param self      an array
     * @param condition the matching condition
     * @return a list of numbers corresponding to the index values of all matched objects
     * @since 2.5.0
     */
    public static <T> List<Number> findIndexValues(T[] self, @ClosureParams(FirstParam.Component.class) Closure<?> condition) {
        return findIndexValues(self, 0, condition);
    }

    /**
     * Iterates over the elements of an array, starting from a specified index,
     * and returns the index values of the items that match the condition
     * specified in the closure.
     *
     * @param self       an array
     * @param startIndex start matching from this index
     * @param condition  the matching condition
     * @return a list of numbers corresponding to the index values of all matched objects
     * @since 2.5.0
     */
    public static <T> List<Number> findIndexValues(T[] self, Number startIndex, @ClosureParams(FirstParam.Component.class) Closure<?> condition) {
        return DefaultGroovyMethods.findIndexValues(new ArrayIterator<>(self), startIndex, condition);
    }

    //--------------------------------------------------------------------------
    // findLastIndexOf

    /**
     * Iterates over the elements of an array and returns the index of the last
     * item that matches the condition specified in the closure.
     *
     * @param self      an array
     * @param condition the matching condition
     * @return an integer that is the index of the last matched object or -1 if no match was found
     * @since 2.5.0
     */
    public static <T> int findLastIndexOf(T[] self, @ClosureParams(FirstParam.Component.class) Closure<?> condition) {
        return DefaultGroovyMethods.findLastIndexOf(new ArrayIterator<>(self), 0, condition);
    }

    /**
     * Iterates over the elements of an array, starting from a specified index,
     * and returns the index of the last item that matches the condition
     * specified in the closure.
     *
     * @param self       an array
     * @param startIndex start matching from this index
     * @param condition  the matching condition
     * @return an integer that is the index of the last matched object or -1 if no match was found
     * @since 2.5.0
     */
    public static <T> int findLastIndexOf(T[] self, int startIndex, @ClosureParams(FirstParam.Component.class) Closure<?> condition) {
        // TODO: Could this be made more efficient by using a reverse index?
        return DefaultGroovyMethods.findLastIndexOf(new ArrayIterator<>(self), startIndex, condition);
    }

    //--------------------------------------------------------------------------
    // findResult

    //--------------------------------------------------------------------------
    // findResults

    //--------------------------------------------------------------------------
    // first

    /**
     * Returns the first item from the boolean array.
     * <pre class="groovyTestCase">
     * boolean[] array = [true, false]
     * assert array.first() == true
     * </pre>
     * An alias for {@code head()}.
     *
     * @param self an array
     * @return the first element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static boolean first(boolean[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "first");
        return self[0];
    }

    /**
     * Returns the first item from the byte array.
     * <pre class="groovyTestCase">
     * byte[] bytes = [1, 2, 3]
     * assert bytes.first() == 1
     * </pre>
     * An alias for {@code head()}.
     *
     * @param self an array
     * @return the first element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static byte first(byte[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "first");
        return self[0];
    }

    /**
     * Returns the first item from the char array.
     * <pre class="groovyTestCase">
     * char[] chars = ['a', 'b', 'c']
     * assert chars.first() == 'a'
     * </pre>
     * An alias for {@code head()}.
     *
     * @param self an array
     * @return the first element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static char first(char[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "first");
        return self[0];
    }

    /**
     * Returns the first item from the short array.
     * <pre class="groovyTestCase">
     * short[] shorts = [10, 20, 30]
     * assert shorts.first() == 10
     * </pre>
     * An alias for {@code head()}.
     *
     * @param self an array
     * @return the first element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static short first(short[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "first");
        return self[0];
    }

    /**
     * Returns the first item from the int array.
     * <pre class="groovyTestCase">
     * int[] ints = [1, 3, 5]
     * assert ints.first() == 1
     * </pre>
     * An alias for {@code head()}.
     *
     * @param self an array
     * @return the first element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static int first(int[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "first");
        return self[0];
    }

    /**
     * Returns the first item from the long array.
     * <pre class="groovyTestCase">
     * long[] longs = [2L, 4L, 6L]
     * assert longs.first() == 2L
     * </pre>
     * An alias for {@code head()}.
     *
     * @param self an array
     * @return the first element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static long first(long[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "first");
        return self[0];
    }

    /**
     * Returns the first item from the float array.
     * <pre class="groovyTestCase">
     * float[] floats = [2.0f, 4.0f, 6.0f]
     * assert floats.first() == 2.0f
     * </pre>
     * An alias for {@code head()}.
     *
     * @param self an array
     * @return the first element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static float first(float[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "first");
        return self[0];
    }

    /**
     * Returns the first item from the double array.
     * <pre class="groovyTestCase">
     * double[] doubles = [10.0d, 20.0d, 30.0d]
     * assert doubles.first() == 10.0d
     * </pre>
     * An alias for {@code head()}.
     *
     * @param self an array
     * @return the first element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static double first(double[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "first");
        return self[0];
    }

    //--------------------------------------------------------------------------
    // flatten

    /**
     * Flattens an array. This array is added to a new collection.
     * It is an alias for {@code toList()} but allows algorithms to be written which also
     * work on multidimensional arrays or non-arrays where flattening would be applicable.
     * <pre class="groovyTestCase">
     * boolean[] array = [false, true]
     * assert array.flatten() == [false, true]
     * </pre>
     *
     * @param self a boolean Array
     * @return a Collection of the array elements
     * @since 1.6.0
     */
    public static List<Boolean> flatten(boolean[] self) {
        return toList(self);
    }

    /**
     * Flattens an array. This array is added to a new collection.
     * It is an alias for {@code toList()} but allows algorithms to be written which also
     * work on multidimensional arrays or non-arrays where flattening would be applicable.
     * <pre class="groovyTestCase">
     * byte[] array = [0, 1]
     * assert array.flatten() == [0, 1]
     * </pre>
     *
     * @param self a byte Array
     * @return a Collection of the array elements
     * @since 1.6.0
     */
    public static List<Byte> flatten(byte[] self) {
        return toList(self);
    }

    /**
     * Flattens an array. This array is added to a new collection.
     * It is an alias for {@code toList()} but allows algorithms to be written which also
     * work on multidimensional arrays or non-arrays where flattening would be applicable.
     * <pre class="groovyTestCase">
     * char[] array = 'ab'.chars
     * assert array.flatten() == ['a', 'b']
     * </pre>
     *
     * @param self a char Array
     * @return a Collection of the array elements
     * @since 1.6.0
     */
    public static List<Character> flatten(char[] self) {
        return toList(self);
    }

    /**
     * Flattens an array. This array is added to a new collection.
     * It is an alias for {@code toList()} but allows algorithms to be written which also
     * work on multidimensional arrays or non-arrays where flattening would be applicable.
     * <pre class="groovyTestCase">
     * short[] array = [0, 1]
     * assert array.flatten() == [0, 1]
     * </pre>
     *
     * @param self a short Array
     * @return a Collection of the array elements
     * @since 1.6.0
     */
    public static List<Short> flatten(short[] self) {
        return toList(self);
    }

    /**
     * Flattens an array. This array is added to a new collection.
     * It is an alias for {@code toList()} but allows algorithms to be written which also
     * work on multidimensional arrays or non-arrays where flattening would be applicable.
     * <pre class="groovyTestCase">
     * int[] array = [0, 1]
     * assert array.flatten() == [0, 1]
     * </pre>
     *
     * @param self an int Array
     * @return a Collection of the array elements
     * @since 1.6.0
     */
    public static List<Integer> flatten(int[] self) {
        return toList(self);
    }

    /**
     * Flattens an array. This array is added to a new collection.
     * It is an alias for {@code toList()} but allows algorithms to be written which also
     * work on multidimensional arrays or non-arrays where flattening would be applicable.
     * <pre class="groovyTestCase">
     * long[] array = [0L, 1L]
     * assert array.flatten() == [0L, 1L]
     * </pre>
     *
     * @param self a long Array to flatten
     * @return a Collection of the array elements
     * @since 1.6.0
     */
    public static List<Long> flatten(long[] self) {
        return toList(self);
    }

    /**
     * Flattens an array. This array is added to a new collection.
     * It is an alias for {@code toList()} but allows algorithms to be written which also
     * work on multidimensional arrays or non-arrays where flattening would be applicable.
     * <pre class="groovyTestCase">
     * float[] array = [0.0f, 1.0f]
     * assert array.flatten() == [0.0f, 1.0f]
     * </pre>
     *
     * @param self a float Array to flatten
     * @return a Collection of the array elements
     * @since 1.6.0
     */
    public static List<Float> flatten(float[] self) {
        return toList(self);
    }

    /**
     * Flattens an array. This array is added to a new collection.
     * It is an alias for {@code toList()} but allows algorithms to be written which also
     * work on multidimensional arrays or non-arrays where flattening would be applicable.
     * <pre class="groovyTestCase">
     * double[] array = [0.0d, 1.0d]
     * assert array.flatten() == [0.0d, 1.0d]
     * </pre>
     *
     * @param self a double Array to flatten
     * @return a Collection of the array elements
     * @since 1.6.0
     */
    public static List<Double> flatten(double[] self) {
        return toList(self);
    }

    //

    /**
     * Flattens a 2D array into a new collection.
     * The items are copied row by row.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * boolean[][] array = [[true, false], [true, false]]
     * assert array.flatten() == [true, false, true, false]
     * </pre>
     *
     * @param self a 2D boolean Array
     * @return a Collection of the array elements
     * @since 5.0.0
     */
    public static List<Boolean> flatten(boolean[][] self) {
        Objects.requireNonNull(self);
        List<Boolean> result = new ArrayList<>();
        for (boolean[] booleans : self) {
            result.addAll(toList(booleans));
        }
        return result;
    }

    /**
     * Flattens a 2D array into a new collection.
     * The items are copied row by row.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * byte[][] array = [[0, 1], [2, 3]]
     * assert array.flatten() == [0, 1, 2, 3]
     * </pre>
     *
     * @param self a 2D byte Array
     * @return a Collection of the array elements
     * @since 5.0.0
     */
    public static List<Byte> flatten(byte[][] self) {
        Objects.requireNonNull(self);
        List<Byte> result = new ArrayList<>();
        for (byte[] bytes : self) {
            result.addAll(toList(bytes));
        }
        return result;
    }

    /**
     * Flattens a 2D array into a new collection.
     * The items are copied row by row.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * char[][] array = ['ab'.chars, 'cd'.chars]
     * assert array.flatten() == ['a', 'b', 'c', 'd']
     * </pre>
     *
     * @param self a 2D char Array
     * @return a Collection of the array elements
     * @since 5.0.0
     */
    public static List<Character> flatten(char[][] self) {
        Objects.requireNonNull(self);
        List<Character> result = new ArrayList<>();
        for (char[] chars : self) {
            result.addAll(toList(chars));
        }
        return result;
    }

    /**
     * Flattens a 2D array into a new collection.
     * The items are copied row by row.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * short[][] array = [[0, 1], [2, 3]]
     * assert array.flatten() == [0, 1, 2, 3]
     * </pre>
     *
     * @param self a 2D short Array
     * @return a Collection of the array elements
     * @since 5.0.0
     */
    public static List<Short> flatten(short[][] self) {
        Objects.requireNonNull(self);
        List<Short> result = new ArrayList<>();
        for (short[] shorts : self) {
            result.addAll(toList(shorts));
        }
        return result;
    }

    /**
     * Flattens a 2D array into a new collection.
     * The items are copied row by row.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * int[][] array = [[0, 1], [2, 3]]
     * assert array.flatten() == [0, 1, 2, 3]
     * </pre>
     *
     * @param self a 2D int Array
     * @return a Collection of the array elements
     * @since 5.0.0
     */
    public static List<Integer> flatten(int[][] self) {
        Objects.requireNonNull(self);
        List<Integer> result = new ArrayList<>();
        for (int[] ints : self) {
            result.addAll(toList(ints));
        }
        return result;
    }

    /**
     * Flattens a 2D array into a new collection.
     * The items are copied row by row.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * long[][] array = [[0, 1], [2, 3]]
     * assert array.flatten() == [0, 1, 2, 3]
     * </pre>
     *
     * @param self a 2D long Array
     * @return a Collection of the array elements
     * @since 5.0.0
     */
    public static List<Long> flatten(long[][] self) {
        Objects.requireNonNull(self);
        List<Long> result = new ArrayList<>();
        for (long[] longs : self) {
            result.addAll(toList(longs));
        }
        return result;
    }

    /**
     * Flattens a 2D array into a new collection.
     * The items are copied row by row.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * float[][] array = [[0.0f, 1.0f], [2.0f, 3.0f]]
     * assert array.flatten() == [0.0f, 1.0f, 2.0f, 3.0f]
     * </pre>
     *
     * @param self a 2D float Array
     * @return a Collection of the array elements
     * @since 5.0.0
     */
    public static List<Float> flatten(float[][] self) {
        Objects.requireNonNull(self);
        List<Float> result = new ArrayList<>();
        for (float[] floats : self) {
            result.addAll(toList(floats));
        }
        return result;
    }

    /**
     * Flattens a 2D array into a new collection.
     * The items are copied row by row.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * double[][] array = [[0.0f, 1.0f], [2.0f, 3.0f]]
     * assert array.flatten() == [0.0f, 1.0f, 2.0f, 3.0f]
     * </pre>
     *
     * @param self a 2D double Array
     * @return a Collection of the array elements
     * @since 5.0.0
     */
    public static List<Double> flatten(double[][] self) {
        Objects.requireNonNull(self);
        List<Double> result = new ArrayList<>();
        for (double[] doubles : self) {
            result.addAll(toList(doubles));
        }
        return result;
    }

    //--------------------------------------------------------------------------
    // getAt

    /**
     * Support the subscript operator for a boolean array with a range giving the desired indices.
     * <pre class="groovyTestCase">
     * boolean[] array = [false, true, false, true, false, true]
     * assert array[2..&lt;2] == [] // EmptyRange
     * assert array[(0..5.5).step(2)] == [false, false, false] // NumberRange
     * assert array[(1..5.5).step(2)] == [true, true, true]    // NumberRange
     * </pre>
     *
     * @param array a boolean array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved booleans
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Boolean> getAt(boolean[] array, Range<?> range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator for a byte array with a range giving the desired indices.
     * <pre class="groovyTestCase">
     * byte[] array = [1, 3, 5, 7, 9, 11]
     * assert array[2..&lt;2] == [] // EmptyRange
     * assert array[(0..5.5).step(2)] == [1, 5, 9]   // NumberRange
     * assert array[(1..5.5).step(2)] == [3, 7, 11]  // NumberRange
     * </pre>
     *
     * @param array a byte array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved bytes
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Byte> getAt(byte[] array, Range<?> range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator for a char array with a range giving the desired indices.
     * <pre class="groovyTestCase">
     * char[] array = 'abcdef'.chars
     * assert array[2..&lt;2] == [] // EmptyRange
     * assert array[(0..5.5).step(2)] == ['a', 'c', 'e']  // NumberRange
     * assert array[(1..5.5).step(2)] == ['b', 'd', 'f']  // NumberRange
     * </pre>
     *
     * @param array a char array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved chars
     * @since 1.5.0
     */
    @SuppressWarnings("unchecked")
    public static List<Character> getAt(char[] array, Range<?> range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator for a short array with a range giving the desired indices.
     * <pre class="groovyTestCase">
     * short[] array = [1, 3, 5, 7, 9, 11]
     * assert array[2..&lt;2] == [] // EmptyRange
     * assert array[(0..5.5).step(2)] == [1, 5, 9]   // NumberRange
     * assert array[(1..5.5).step(2)] == [3, 7, 11]  // NumberRange
     * </pre>
     *
     * @param array a short array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved shorts
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Short> getAt(short[] array, Range<?> range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator for an int array with a range giving the desired indices.
     * <pre class="groovyTestCase">
     * int[] array = [1, 3, 5, 7, 9, 11]
     * assert array[2..&lt;2] == [] // EmptyRange
     * assert array[(0..5.5).step(2)] == [1, 5, 9]   // NumberRange
     * assert array[(1..5.5).step(2)] == [3, 7, 11]  // NumberRange
     * </pre>
     *
     * @param array an int array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the ints at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Integer> getAt(int[] array, Range<?> range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator for a long array with a range giving the desired indices.
     * <pre class="groovyTestCase">
     * long[] array = [1L, 3L, 5L, 7L, 9L, 11L]
     * assert array[2..&lt;2] == [] // EmptyRange
     * assert array[(0..5.5).step(2)] == [1L, 5L, 9L]   // NumberRange
     * assert array[(1..5.5).step(2)] == [3L, 7L, 11L]  // NumberRange
     * </pre>
     *
     * @param array a long array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved longs
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Long> getAt(long[] array, Range<?> range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator for a float array with a range giving the desired indices.
     * <pre class="groovyTestCase">
     * float[] array = [1.0f, 3.0f, 5.0f, 7.0f, 9.0f, 11.0f]
     * assert array[2..&lt;2] == [] // EmptyRange
     * assert array[(0..5.5).step(2)] == [1.0f, 5.0f, 9.0f]   // NumberRange
     * assert array[(1..5.5).step(2)] == [3.0f, 7.0f, 11.0f]  // NumberRange
     * </pre>
     *
     * @param array a float array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved floats
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Float> getAt(float[] array, Range<?> range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator for a double array with a range giving the desired indices.
     * <pre class="groovyTestCase">
     * double[] array = [1.0d, 3.0d, 5.0d, 7.0d, 9.0d, 11.0d]
     * assert array[2..&lt;2] == [] // EmptyRange
     * assert array[(0..5.5).step(2)] == [1.0d, 5.0d, 9.0d]   // NumberRange
     * assert array[(1..5.5).step(2)] == [3.0d, 7.0d, 11.0d]  // NumberRange
     * </pre>
     *
     * @param array a double array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved doubles
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Double> getAt(double[] array, Range<?> range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator for an object array with a range giving the desired indices.
     *
     * @param array an Array of Objects
     * @param range a Range
     * @return a range of a list from the range's from index up to but not
     *         including the range's to value
     * @since 1.0
     */
    public static <T> List<T> getAt(T[] array, Range<?> range) {
        return DefaultGroovyMethods.getAt(Arrays.asList(array), range);
    }

    //

    /**
     * Support the subscript operator for a boolean array with an IntRange giving the desired indices.
     * <pre class="groovyTestCase">
     * boolean[] array = [false, false, true, true, false]
     * assert array[2..3] == [true, true]
     * assert array[-2..-1] == [true, false]
     * assert array[-1..-2] == [false, true]
     * </pre>
     *
     * @param array a boolean array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved booleans
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Boolean> getAt(boolean[] array, IntRange range) {
        RangeInfo info = subListBorders(array.length, range);
        List<Boolean> answer = primitiveArrayGet(array, subListRange(info, range));
        return info.reverse ? DefaultGroovyMethods.reverse(answer) : answer;
    }

    /**
     * Support the subscript operator for a byte array with an IntRange giving the desired indices.
     * <pre class="groovyTestCase">
     * byte[] array = [0, 10, 20, 30, 40]
     * assert array[2..3] == [20, 30]
     * assert array[-2..-1] == [30, 40]
     * assert array[-1..-2] == [40, 30]
     * </pre>
     *
     * @param array a byte array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved bytes
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Byte> getAt(byte[] array, IntRange range) {
        RangeInfo info = subListBorders(array.length, range);
        List<Byte> answer = primitiveArrayGet(array, subListRange(info, range));
        return info.reverse ? DefaultGroovyMethods.reverse(answer) : answer;
    }

    /**
     * Support the subscript operator for a char array with an IntRange giving the desired indices.
     * <pre class="groovyTestCase">
     * char[] array = 'abcdef'.chars
     * assert array[2..3] == ['c', 'd']
     * assert array[-2..-1] == ['e', 'f']
     * assert array[-1..-2] == ['f', 'e']
     * </pre>
     *
     * @param array a char array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved chars
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Character> getAt(char[] array, IntRange range) {
        RangeInfo info = subListBorders(array.length, range);
        List<Character> answer = primitiveArrayGet(array, subListRange(info, range));
        return info.reverse ? DefaultGroovyMethods.reverse(answer) : answer;
    }

    /**
     * Support the subscript operator for a short array with an IntRange giving the desired indices.
     * <pre class="groovyTestCase">
     * short[] array = [0, 10, 20, 30, 40]
     * assert array[2..3] == [20, 30]
     * assert array[-2..-1] == [30, 40]
     * assert array[-1..-2] == [40, 30]
     * </pre>
     *
     * @param array a short array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved shorts
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Short> getAt(short[] array, IntRange range) {
        RangeInfo info = subListBorders(array.length, range);
        List<Short> answer = primitiveArrayGet(array, subListRange(info, range));
        return info.reverse ? DefaultGroovyMethods.reverse(answer) : answer;
    }

    /**
     * Support the subscript operator for an int array with an IntRange giving the desired indices.
     * <pre class="groovyTestCase">
     * int[] array = [0, 10, 20, 30, 40]
     * assert array[2..3] == [20, 30]
     * assert array[-2..-1] == [30, 40]
     * assert array[-1..-2] == [40, 30]
     * </pre>
     *
     * @param array an int array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved ints
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Integer> getAt(int[] array, IntRange range) {
        RangeInfo info = subListBorders(array.length, range);
        List<Integer> answer = primitiveArrayGet(array, subListRange(info, range));
        return info.reverse ? DefaultGroovyMethods.reverse(answer) : answer;
    }

    /**
     * Support the subscript operator for a long array with an IntRange giving the desired indices.
     * <pre class="groovyTestCase">
     * long[] array = [0L, 10L, 20L, 30L, 40L]
     * assert array[2..3] == [20L, 30L]
     * assert array[-2..-1] == [30L, 40L]
     * assert array[-1..-2] == [40L, 30L]
     * </pre>
     *
     * @param array a long array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved longs
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Long> getAt(long[] array, IntRange range) {
        RangeInfo info = subListBorders(array.length, range);
        List<Long> answer = primitiveArrayGet(array, subListRange(info, range));
        return info.reverse ? DefaultGroovyMethods.reverse(answer) : answer;
    }

    /**
     * Support the subscript operator for a float array with an IntRange giving the desired indices.
     * <pre class="groovyTestCase">
     * float[] array = [0.0f, 10.0f, 20.0f, 30.0f, 40.0f]
     * assert array[2..3] == [20.0f, 30.0f]
     * assert array[-2..-1] == [30.0f, 40.0f]
     * assert array[-1..-2] == [40.0f, 30.0f]
     * </pre>
     *
     * @param array a float array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved floats
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Float> getAt(float[] array, IntRange range) {
        RangeInfo info = subListBorders(array.length, range);
        List<Float> answer = primitiveArrayGet(array, subListRange(info, range));
        return info.reverse ? DefaultGroovyMethods.reverse(answer) : answer;
    }

    /**
     * Support the subscript operator for a double array with an IntRange giving the desired indices.
     * <pre class="groovyTestCase">
     * double[] array = [0.0d, 10.0d, 20.0d, 30.0d, 40.0d]
     * assert array[2..3] == [20.0d, 30.0d]
     * assert array[-2..-1] == [30.0d, 40.0d]
     * assert array[-1..-2] == [40.0d, 30.0d]
     * </pre>
     *
     * @param array a double array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved doubles
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Double> getAt(double[] array, IntRange range) {
        RangeInfo info = subListBorders(array.length, range);
        List<Double> answer = primitiveArrayGet(array, subListRange(info, range));
        return info.reverse ? DefaultGroovyMethods.reverse(answer) : answer;
    }

    /**
     *
     * @param array an object array
     * @param range an IntRange
     * @return a range of a list from the range's from index up to but not
     *         including the range's to value
     * @since 1.0
     */
    public static <T> List<T> getAt(T[] array, IntRange range) {
        return DefaultGroovyMethods.getAt(Arrays.asList(array), range);
    }

    //

    /**
     * Support the subscript operator for a boolean array with an ObjectRange giving the desired indices.
     * <pre class="groovyTestCase">
     * boolean[] array = [false, false, true, true, false]
     * def range = new ObjectRange(2, 3)
     * assert array[range] == [true, true]
     * </pre>
     *
     * @param array a boolean array
     * @param range an ObjectRange indicating the indices for the items to retrieve
     * @return list of the retrieved bytes
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Boolean> getAt(boolean[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator for a byte array with an ObjectRange giving the desired indices.
     * <pre class="groovyTestCase">
     * byte[] array = [0, 10, 20, 30, 40]
     * def range = new ObjectRange(2, 3)
     * assert array[range] == [20, 30]
     * </pre>
     *
     * @param array a byte array
     * @param range an ObjectRange indicating the indices for the items to retrieve
     * @return list of the retrieved bytes
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Byte> getAt(byte[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator for a boolean array with an ObjectRange giving the desired indices.
     * <pre class="groovyTestCase">
     * char[] array = 'abcdef'.chars
     * def range = new ObjectRange(2, 3)
     * assert array[range] == ['c', 'd']
     * </pre>
     *
     * @param array a char array
     * @param range an ObjectRange indicating the indices for the items to retrieve
     * @return list of the retrieved chars
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Character> getAt(char[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator for a short array with an ObjectRange giving the desired indices.
     * <pre class="groovyTestCase">
     * short[] array = [0, 10, 20, 30, 40]
     * def range = new ObjectRange(2, 3)
     * assert array[range] == [20, 30]
     * </pre>
     *
     * @param array a short array
     * @param range an ObjectRange indicating the indices for the items to retrieve
     * @return list of the retrieved shorts
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Short> getAt(short[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator for an int array with an ObjectRange giving the desired indices.
     * <pre class="groovyTestCase">
     * int[] array = [0, 10, 20, 30, 40]
     * def range = new ObjectRange(2, 3)
     * assert array[range] == [20, 30]
     * </pre>
     *
     * @param array an int array
     * @param range an ObjectRange indicating the indices for the items to retrieve
     * @return list of the retrieved ints
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Integer> getAt(int[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator for a long array with an ObjectRange giving the desired indices.
     * <pre class="groovyTestCase">
     * long[] array = [0L, 10L, 20L, 30L, 40L]
     * def range = new ObjectRange(2, 3)
     * assert array[range] == [20L, 30L]
     * </pre>
     *
     * @param array a long array
     * @param range an ObjectRange indicating the indices for the items to retrieve
     * @return list of the retrieved longs
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Long> getAt(long[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator for a float array with an ObjectRange giving the desired indices.
     * <pre class="groovyTestCase">
     * float[] array = [0.0f, 10.0f, 20.0f, 30.0f, 40.0f]
     * def range = new ObjectRange(2, 3)
     * assert array[range] == [20.0f, 30.0f]
     * </pre>
     *
     * @param array a float array
     * @param range an ObjectRange indicating the indices for the items to retrieve
     * @return list of the retrieved floats
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Float> getAt(float[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator for a double array with an ObjectRange giving the desired indices.
     * <pre class="groovyTestCase">
     * double[] array = [0.0d, 10.0d, 20.0d, 30.0d, 40.0d]
     * def range = new ObjectRange(2, 3)
     * assert array[range] == [20.0d, 30.0d]
     * </pre>
     *
     * @param array a double array
     * @param range an ObjectRange indicating the indices for the items to retrieve
     * @return list of the retrieved doubles
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Double> getAt(double[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * @param array an Array of Objects
     * @param range an ObjectRange
     * @return a range of a list from the range's from index up to but not
     *         including the range's to value
     * @since 1.0
     */
    public static <T> List<T> getAt(T[] array, ObjectRange range) {
        return DefaultGroovyMethods.getAt(Arrays.asList(array), range);
    }

    //

    /**
     *
     * @param array an Array of Objects
     * @param range an EmptyRange
     * @return an empty Range
     * @since 1.5.0
     */
    public static <T> List<T> getAt(T[] array, EmptyRange<?> range) {
        return new ArrayList<>();
    }

    //

    /**
     * Support the subscript operator for a boolean array
     * with a (potentially nested) collection giving the desired indices.
     * <pre class="groovyTestCase">
     * boolean[] array = [false, false, true, true, false]
     * assert array[2, 3] == [true, true]
     * assert array[0, 0..1, [1, [-1]]] == [false, false, false, false, false]
     * </pre>
     *
     * @param array   a boolean array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the booleans at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Boolean> getAt(boolean[] array, Collection<?> indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator for a byte array
     * with a (potentially nested) collection giving the desired indices.
     * <pre class="groovyTestCase">
     * byte[] array = [0, 2, 4, 6, 8]
     * assert array[2, 3] == [4, 6]
     * assert array[1, 0..1, [0, [-1]]] == [2, 0, 2, 0, 8]
     * </pre>
     *
     * @param array   a byte array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the bytes at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Byte> getAt(byte[] array, Collection<?> indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator for a char array
     * with a (potentially nested) collection giving the desired indices.
     * <pre class="groovyTestCase">
     * char[] array = 'abcde'.chars
     * assert array[2, 3] == ['c', 'd']
     * assert array[1, 0..1, [0, [-1]]] == ['b', 'a', 'b', 'a', 'e']
     * </pre>
     *
     * @param array   a char array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the chars at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Character> getAt(char[] array, Collection<?> indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator for a short array
     * with a (potentially nested) collection giving the desired indices.
     * <pre class="groovyTestCase">
     * short[] array = [0, 2, 4, 6, 8]
     * assert array[2, 3] == [4, 6]
     * assert array[1, 0..1, [0, [-1]]] == [2, 0, 2, 0, 8]
     * </pre>
     *
     * @param array   a short array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the shorts at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Short> getAt(short[] array, Collection<?> indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator for an int array
     * with a (potentially nested) collection giving the desired indices.
     * <pre class="groovyTestCase">
     * int[] array = [0, 2, 4, 6, 8]
     * assert array[2, 3] == [4, 6]
     * assert array[1, 0..1, [0, [-1]]] == [2, 0, 2, 0, 8]
     * </pre>
     *
     * @param array   an int array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the ints at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Integer> getAt(int[] array, Collection<?> indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator for a long array
     * with a (potentially nested) collection giving the desired indices.
     * <pre class="groovyTestCase">
     * long[] array = [0L, 2L, 4L, 6L, 8L]
     * assert array[2, 3] == [4L, 6L]
     * assert array[1, 0..1, [0, [-1]]] == [2L, 0L, 2L, 0L, 8L]
     * </pre>
     *
     * @param array   a long array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the longs at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Long> getAt(long[] array, Collection<?> indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator for a float array
     * with a (potentially nested) collection giving the desired indices.
     * <pre class="groovyTestCase">
     * float[] array = [0.0f, 2.0f, 4.0f, 6.0f, 8.0f]
     * assert array[2, 3] == [4.0f, 6.0f]
     * assert array[1, 0..1, [0, [-1]]] == [2.0f, 0.0f, 2.0f, 0.0f, 8.0f]
     * </pre>
     *
     * @param array   a float array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the floats at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Float> getAt(float[] array, Collection<?> indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator for a double array
     * with a (potentially nested) collection giving the desired indices.
     * <pre class="groovyTestCase">
     * double[] array = [0.0d, 2.0d, 4.0d, 6.0d, 8.0d]
     * assert array[2, 3] == [4.0d, 6.0d]
     * assert array[1, 0..1, [0, [-1]]] == [2.0d, 0.0d, 2.0d, 0.0d, 8.0d]
     * </pre>
     *
     * @param array   a double array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the doubles at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Double> getAt(double[] array, Collection<?> indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Select a List of items from an array using a Collection to
     * identify the indices to be selected.
     *
     * @param array   an object array
     * @param indices a collection of indices
     * @return a new list of the values at the given indices
     * @since 1.0
     */
    public static <T> List<T> getAt(T[] array, Collection<?> indices) {
        List<T> answer = new ArrayList<>(indices.size());
        for (Object value : indices) {
            if (value instanceof Range) {
                answer.addAll(getAt(array, (Range<?>) value));
            } else if (value instanceof Collection) {
                answer.addAll(getAt(array, (Collection<?>) value));
            } else {
                int idx = DefaultTypeTransformation.intUnbox(value);
                answer.add(array[normaliseIndex(idx, array.length)]);
            }
        }
        return answer;
    }

    //--------------------------------------------------------------------------
    // getIndices

    /**
     * Returns indices of the boolean array.
     * <pre class="groovyTestCase">
     * boolean[] array = [false, true]
     * assert array.indices == 0..1
     * </pre>
     *
     * @see DefaultGroovyMethods#getIndices(Object[])
     * @since 3.0.8
     */
    public static IntRange getIndices(boolean[] self) {
        Objects.requireNonNull(self);
        return new IntRange(false, 0, self.length);
    }

    /**
     * Returns indices of the byte array.
     * <pre class="groovyTestCase">
     * byte[] array = [0, 1]
     * assert array.indices == 0..1
     * </pre>
     *
     * @see DefaultGroovyMethods#getIndices(Object[])
     * @since 3.0.8
     */
    public static IntRange getIndices(byte[] self) {
        Objects.requireNonNull(self);
        return new IntRange(false, 0, self.length);
    }

    /**
     * Returns indices of the char array.
     * <pre class="groovyTestCase">
     * char[] array = 'ab'.chars
     * assert array.indices == 0..1
     * </pre>
     *
     * @see DefaultGroovyMethods#getIndices(Object[])
     * @since 3.0.8
     */
    public static IntRange getIndices(char[] self) {
        Objects.requireNonNull(self);
        return new IntRange(false, 0, self.length);
    }

    /**
     * Returns indices of the short array.
     * <pre class="groovyTestCase">
     * short[] array = [0, 1]
     * assert array.indices == 0..1
     * </pre>
     *
     * @see DefaultGroovyMethods#getIndices(Object[])
     * @since 3.0.8
     */
    public static IntRange getIndices(short[] self) {
        Objects.requireNonNull(self);
        return new IntRange(false, 0, self.length);
    }

    /**
     * Returns indices of the int array.
     * <pre class="groovyTestCase">
     * int[] array = [0, 1]
     * assert array.indices == 0..1
     * </pre>
     *
     * @see DefaultGroovyMethods#getIndices(Object[])
     * @since 3.0.8
     */
    public static IntRange getIndices(int[] self) {
        Objects.requireNonNull(self);
        return new IntRange(false, 0, self.length);
    }

    /**
     * Returns indices of the long array.
     * <pre class="groovyTestCase">
     * long[] array = [0L, 1L]
     * assert array.indices == 0..1
     * </pre>
     *
     * @see DefaultGroovyMethods#getIndices(Object[])
     * @since 3.0.8
     */
    public static IntRange getIndices(long[] self) {
        Objects.requireNonNull(self);
        return new IntRange(false, 0, self.length);
    }

    /**
     * Returns indices of the float array.
     * <pre class="groovyTestCase">
     * float[] array = [0.0f, 1.0f]
     * assert array.indices == 0..1
     * </pre>
     *
     * @see DefaultGroovyMethods#getIndices(Object[])
     * @since 3.0.8
     */
    public static IntRange getIndices(float[] self) {
        Objects.requireNonNull(self);
        return new IntRange(false, 0, self.length);
    }

    /**
     * Returns indices of the double array.
     * <pre class="groovyTestCase">
     * double[] array = [0.0d, 1.0d]
     * assert array.indices == 0..1
     * </pre>
     *
     * @see DefaultGroovyMethods#getIndices(Object[])
     * @since 3.0.8
     */
    public static IntRange getIndices(double[] self) {
        Objects.requireNonNull(self);
        return new IntRange(false, 0, self.length);
    }

    /**
     * Returns indices of the array.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * String[] letters = ['a', 'b', 'c', 'd']
     * {@code assert 0..<4 == letters.indices}
     * </pre>
     *
     * @param self an array
     * @return an index range
     * @since 2.4.0
     */
    public static <T> IntRange getIndices(T[] self) {
        return new IntRange(false, 0, self.length);
    }

    //--------------------------------------------------------------------------
    // grep

    /**
     * Iterates over the array returning each element that matches
     * using the IDENTITY Closure as a filter - effectively returning all elements which satisfy Groovy truth.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * def items = [1, 2, 0, false, true, '', 'foo', [], [4, 5], null] as Object[]
     * assert items.grep() == [1, 2, true, 'foo', [4, 5]]
     * </pre>
     *
     * @param self an array
     * @return a list of elements which satisfy Groovy truth
     * @see Closure#IDENTITY
     * @since 2.0
     */
    public static <T> List<T> grep(T[] self) {
        return grep(self, Closure.IDENTITY);
    }

    /**
     * Iterates over the array of items and returns a collection of items that match
     * the given filter - calling the {@link DefaultGroovyMethods#isCase(Object,Object)}
     * method used by switch statements. This method can be used with different
     * kinds of filters like regular expressions, classes, ranges etc.
     * Example:
     * <pre class="groovyTestCase">
     * def items = ['a', 'b', 'aa', 'bc', 3, 4.5] as Object[]
     * assert items.grep( ~/a+/ )  == ['a', 'aa']
     * assert items.grep( ~/../ )  == ['aa', 'bc']
     * assert items.grep( Number ) == [ 3, 4.5 ]
     * assert items.grep{ it.toString().size() == 1 } == [ 'a', 'b', 3 ]
     * </pre>
     *
     * @param self   an array
     * @param filter the filter to perform on each element of the array (using the {@link #isCase(java.lang.Object, java.lang.Object)} method)
     * @return a list of objects which match the filter
     * @since 2.0
     */
    public static <T> List<T> grep(T[] self, Object filter) {
        List<T> answer = new ArrayList<>();
        BooleanReturningMethodInvoker bmi = new BooleanReturningMethodInvoker("isCase");
        for (T element : self) {
            if (bmi.invoke(filter, element)) {
                answer.add(element);
            }
        }
        return answer;
    }

    //--------------------------------------------------------------------------
    // groupBy

    //--------------------------------------------------------------------------
    // head

    /**
     * Returns the first item from the boolean array.
     * <pre class="groovyTestCase">
     * boolean[] array = [true, false]
     * assert array.head() == true
     * </pre>
     * An alias for {@code first()}.
     *
     * @param self an array
     * @return the first element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static boolean head(boolean[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "head");
        return self[0];
    }

    /**
     * Returns the first item from the byte array.
     * <pre class="groovyTestCase">
     * byte[] bytes = [1, 2, 3]
     * assert bytes.head() == 1
     * </pre>
     * An alias for {@code first()}.
     *
     * @param self an array
     * @return the first element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static byte head(byte[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "head");
        return self[0];
    }

    /**
     * Returns the first item from the char array.
     * <pre class="groovyTestCase">
     * char[] chars = ['a', 'b', 'c']
     * assert chars.head() == 'a'
     * </pre>
     * An alias for {@code first()}.
     *
     * @param self an array
     * @return the first element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static char head(char[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "head");
        return self[0];
    }

    /**
     * Returns the first item from the short array.
     * <pre class="groovyTestCase">
     * short[] shorts = [10, 20, 30]
     * assert shorts.head() == 10
     * </pre>
     * An alias for {@code first()}.
     *
     * @param self an array
     * @return the first element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static short head(short[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "head");
        return self[0];
    }

    /**
     * Returns the first item from the int array.
     * <pre class="groovyTestCase">
     * int[] ints = [1, 3, 5]
     * assert ints.head() == 1
     * </pre>
     * An alias for {@code first()}.
     *
     * @param self an array
     * @return the first element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static int head(int[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "head");
        return self[0];
    }

    /**
     * Returns the first item from the long array.
     * <pre class="groovyTestCase">
     * long[] longs = [2L, 4L, 6L]
     * assert longs.head() == 2L
     * </pre>
     * An alias for {@code first()}.
     *
     * @param self an array
     * @return the first element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static long head(long[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "head");
        return self[0];
    }

    /**
     * Returns the first item from the float array.
     * <pre class="groovyTestCase">
     * float[] floats = [2.0f, 4.0f, 6.0f]
     * assert floats.head() == 2.0f
     * </pre>
     * An alias for {@code first()}.
     *
     * @param self an array
     * @return the first element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static float head(float[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "head");
        return self[0];
    }

    /**
     * Returns the first item from the double array.
     * <pre class="groovyTestCase">
     * double[] doubles = [10.0d, 20.0d, 30.0d]
     * assert doubles.head() == 10.0d
     * </pre>
     * An alias for {@code first()}.
     *
     * @param self an array
     * @return the first element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static double head(double[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "head");
        return self[0];
    }

    //--------------------------------------------------------------------------
    // indexed

    /**
     * Zips an int[] with indices in (index, value) order starting from index 0.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * int[] nums = [10, 20, 30]
     * assert [0: 10, 1: 20, 2: 30] == nums.indexed()
     * </pre>
     *
     * @see #indexed(int[], int)
     * @since 3.0.8
     */
    public static Map<Integer, Integer> indexed(int[] self) {
        return indexed(self, 0);
    }

    /**
     * Zips a long[] with indices in (index, value) order starting from index 0.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * long[] nums = [10L, 20L, 30L]
     * assert [0: 10L, 1: 20L, 2: 30L] == nums.indexed()
     * </pre>
     *
     * @see #indexed(long[], int)
     * @since 3.0.8
     */
    public static Map<Integer, Long> indexed(long[] self) {
        return indexed(self, 0);
    }

    /**
     * Zips a double[] with indices in (index, value) order starting from index 0.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * double[] nums = [10.0d, 20.0d, 30.0d]
     * assert [0: 10.0d, 1: 20.0d, 2: 30.0d] == nums.indexed()
     * </pre>
     *
     * @see #indexed(double[], int)
     * @since 3.0.8
     */
    public static Map<Integer, Double> indexed(double[] self) {
        return indexed(self, 0);
    }

    //

    /**
     * Zips an int[] with indices in (index, value) order.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * int[] nums = [10, 20, 30]
     * assert [5: 10, 6: 20, 7: 30] == nums.indexed(5)
     * assert ["1: 10", "2: 20", "3: 30"] == nums.indexed(1).collect { idx, str {@code ->} "$idx: $str" }
     * </pre>
     *
     * @param self   an Iterable
     * @param offset an index to start from
     * @return a Map (since the keys/indices are unique) containing the elements from the iterable zipped with indices
     * @see DefaultGroovyMethods#indexed(Iterable, int)
     * @since 3.0.8
     */
    public static Map<Integer, Integer> indexed(int[] self, int offset) {
        return DefaultGroovyMethods.indexed(new IntArrayIterable(self), offset);
    }

    /**
     * Zips a long[] with indices in (index, value) order.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * long[] nums = [10L, 20L, 30L]
     * assert [5: 10L, 6: 20L, 7: 30L] == nums.indexed(5)
     * </pre>
     *
     * @param self   a long[]
     * @param offset an index to start from
     * @return a Map (since the keys/indices are unique) containing the elements from the iterable zipped with indices
     * @see DefaultGroovyMethods#indexed(Iterable, int)
     * @since 3.0.8
     */
    public static Map<Integer, Long> indexed(long[] self, int offset) {
        return DefaultGroovyMethods.indexed(new LongArrayIterable(self), offset);
    }

    /**
     * Zips a double[] with indices in (index, value) order.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * double[] nums = [10.0d, 20.0d, 30.0d]
     * assert [5: 10.0d, 6: 20.0d, 7: 30.0d] == nums.indexed(5)
     * </pre>
     *
     * @param self   a double[]
     * @param offset an index to start from
     * @return a Map (since the keys/indices are unique) containing the elements from the iterable zipped with indices
     * @see DefaultGroovyMethods#indexed(Iterable, int)
     * @since 3.0.8
     */
    public static Map<Integer, Double> indexed(double[] self, int offset) {
        return DefaultGroovyMethods.indexed(new DoubleArrayIterable(self), offset);
    }

    //--------------------------------------------------------------------------
    // init

    /**
     * Returns the items from the boolean array excluding the last item.
     * <pre class="groovyTestCase">
     * boolean[] array = [true, false, true]
     * def result = array.init()
     * assert result == [true, false]
     * assert array.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self an array
     * @return an array without its last element
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    public static boolean[] init(boolean[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "init");
        return Arrays.copyOfRange(self, 0, self.length - 1);
    }

    /**
     * Returns the items from the byte array excluding the last item.
     * <pre class="groovyTestCase">
     * byte[] bytes = [1, 2, 3]
     * def result = bytes.init()
     * assert result == [1, 2]
     * assert bytes.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self an array
     * @return an array without its last element
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    public static byte[] init(byte[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "init");
        return Arrays.copyOfRange(self, 0, self.length - 1);
    }

    /**
     * Returns the items from the char array excluding the last item.
     * <pre class="groovyTestCase">
     * char[] chars = ['a', 'b', 'c']
     * def result = chars.init()
     * assert result == ['a', 'b']
     * assert chars.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self an array
     * @return an array without its last element
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    public static char[] init(char[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "init");
        return Arrays.copyOfRange(self, 0, self.length - 1);
    }

    /**
     * Returns the items from the short array excluding the last item.
     * <pre class="groovyTestCase">
     * short[] shorts = [10, 20, 30]
     * def result = shorts.init()
     * assert result == [10, 20]
     * assert shorts.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self an array
     * @return an array without its last element
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    public static short[] init(short[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "init");
        return Arrays.copyOfRange(self, 0, self.length - 1);
    }

    /**
     * Returns the items from the int array excluding the last item.
     * <pre class="groovyTestCase">
     * int[] ints = [1, 3, 5]
     * def result = ints.init()
     * assert result == [1, 3]
     * assert ints.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self an array
     * @return an array without its last element
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    public static int[] init(int[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "init");
        return Arrays.copyOfRange(self, 0, self.length - 1);
    }

    /**
     * Returns the items from the long array excluding the last item.
     * <pre class="groovyTestCase">
     * long[] longs = [2L, 4L, 6L]
     * def result = longs.init()
     * assert result == [2L, 4L]
     * assert longs.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self an array
     * @return an array without its last element
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    public static long[] init(long[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "init");
        return Arrays.copyOfRange(self, 0, self.length - 1);
    }

    /**
     * Returns the items from the float array excluding the last item.
     * <pre class="groovyTestCase">
     * float[] floats = [2.0f, 4.0f, 6.0f]
     * def result = floats.init()
     * assert result == [2.0f, 4.0f]
     * assert floats.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self an array
     * @return an array without its last element
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    public static float[] init(float[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "init");
        return Arrays.copyOfRange(self, 0, self.length - 1);
    }

    /**
     * Returns the items from the double array excluding the last item.
     * <pre class="groovyTestCase">
     * double[] doubles = [10.0d, 20.0d, 30.0d]
     * def result = doubles.init()
     * assert result == [10.0d, 20.0d]
     * assert doubles.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self an array
     * @return an array without its last element
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    public static double[] init(double[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "init");
        return Arrays.copyOfRange(self, 0, self.length - 1);
    }

    //--------------------------------------------------------------------------
    // inject

    /**
     * Iterates through the given array as with inject(Object[],initialValue,closure), but
     * using the first element of the array as the initialValue, and then iterating
     * the remaining elements of the array.
     *
     * @param self    an array
     * @param closure a closure
     * @return the result of the last closure call
     * @throws NoSuchElementException if the array is empty.
     * @see #inject(Object[], Object, Closure)
     * @since 1.8.7
     */
    public static <E,T, V extends T> T inject(E[] self, @ClosureParams(value=FromString.class,options="E,E") Closure<V> closure) {
        return DefaultGroovyMethods.inject((Object) self, closure);
    }

    /**
     * Iterates through the given array, passing in the initial value to
     * the closure along with the first item. The result is passed back (injected) into
     * the closure along with the second item. The new result is injected back into
     * the closure along with the third item and so on until all elements of the array
     * have been used.
     * <p>
     * Also known as foldLeft in functional parlance.
     *
     * @param self         an Object[]
     * @param initialValue some initial value
     * @param closure      a closure
     * @return the result of the last closure call
     * @since 1.5.0
     */
    public static <E, T, U extends T, V extends T> T inject(E[] self, U initialValue, @ClosureParams(value=FromString.class,options="U,E") Closure<V> closure) {
        Object[] params = new Object[2];
        T value = initialValue;
        for (Object next : self) {
            params[0] = value;
            params[1] = next;
            value = closure.call(params);
        }
        return value;
    }

    //--------------------------------------------------------------------------
    // iterator

    /**
     * Attempts to create an Iterator for the given object by first
     * converting it to a Collection.
     *
     * @param self an array
     * @return an Iterator for the given Array.
     * @see org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation#asCollection(java.lang.Object[])
     * @since 1.6.4
     */
    public static <T> Iterator<T> iterator(final T[] self) {
        return DefaultTypeTransformation.asCollection(self).iterator();
    }

    //--------------------------------------------------------------------------
    // join

    /**
     * Concatenates the string representation of each item in this array,
     * with the given String as a separator between each item.
     *
     * @param self      an array of boolean
     * @param separator a String separator
     * @return the joined String
     * @since 2.4.1
     */
    public static String join(boolean[] self, String separator) {
        return DefaultGroovyMethods.join(new BooleanArrayIterator(self), separator);
    }

    /**
     * Concatenates the string representation of each item in this array,
     * with the given String as a separator between each item.
     *
     * @param self      an array of byte
     * @param separator a String separator
     * @return the joined String
     * @since 2.4.1
     */
    public static String join(byte[] self, String separator) {
        return DefaultGroovyMethods.join(new ByteArrayIterator(self), separator);
    }

    /**
     * Concatenates the string representation of each item in this array,
     * with the given String as a separator between each item.
     *
     * @param self      an array of char
     * @param separator a String separator
     * @return the joined String
     * @since 2.4.1
     */
    public static String join(char[] self, String separator) {
        return DefaultGroovyMethods.join(new CharArrayIterator(self), separator);
    }

    /**
     * Concatenates the string representation of each item in this array,
     * with the given String as a separator between each item.
     *
     * @param self      an array of short
     * @param separator a String separator
     * @return the joined String
     * @since 2.4.1
     */
    public static String join(short[] self, String separator) {
        return DefaultGroovyMethods.join(new ShortArrayIterator(self), separator);
    }

    /**
     * Concatenates the string representation of each item in this array,
     * with the given String as a separator between each item.
     *
     * @param self      an array of int
     * @param separator a String separator
     * @return the joined String
     * @since 2.4.1
     */
    public static String join(int[] self, String separator) {
        return DefaultGroovyMethods.join(new IntArrayIterator(self), separator);
    }

    /**
     * Concatenates the string representation of each item in this array,
     * with the given String as a separator between each item.
     *
     * @param self      an array of long
     * @param separator a String separator
     * @return the joined String
     * @since 2.4.1
     */
    public static String join(long[] self, String separator) {
        return DefaultGroovyMethods.join(new LongArrayIterator(self), separator);
    }

    /**
     * Concatenates the string representation of each item in this array,
     * with the given String as a separator between each item.
     *
     * @param self      an array of float
     * @param separator a String separator
     * @return the joined String
     * @since 2.4.1
     */
    public static String join(float[] self, String separator) {
        return DefaultGroovyMethods.join(new FloatArrayIterator(self), separator);
    }

    /**
     * Concatenates the string representation of each item in this array,
     * with the given String as a separator between each item.
     *
     * @param self      an array of double
     * @param separator a String separator
     * @return the joined String
     * @since 2.4.1
     */
    public static String join(double[] self, String separator) {
        return DefaultGroovyMethods.join(new DoubleArrayIterator(self), separator);
    }

    /**
     * Concatenates the <code>toString()</code> representation of each
     * item in this array, with the given String as a separator between each
     * item.
     *
     * @param self      an array of Object
     * @param separator a String separator
     * @return the joined String
     * @since 1.0
     */
    public static <T> String join(T[] self, String separator) {
        return DefaultGroovyMethods.join(new ArrayIterator<>(self), separator);
    }

    //--------------------------------------------------------------------------
    // last

    /**
     * Returns the last item from the boolean array.
     * <pre class="groovyTestCase">
     * boolean[] array = [true, false, true]
     * assert array.last() == true
     * </pre>
     *
     * @param self an array
     * @return the last element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static boolean last(boolean[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "last");
        return self[self.length - 1];
    }

    /**
     * Returns the last item from the byte array.
     * <pre class="groovyTestCase">
     * byte[] bytes = [1, 2, 3]
     * assert bytes.last() == 3
     * </pre>
     *
     * @param self an array
     * @return the last element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static byte last(byte[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "last");
        return self[self.length - 1];
    }

    /**
     * Returns the last item from the char array.
     * <pre class="groovyTestCase">
     * char[] chars = ['a', 'b', 'c']
     * assert chars.last() == 'c'
     * </pre>
     *
     * @param self an array
     * @return the last element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static char last(char[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "last");
        return self[self.length - 1];
    }

    /**
     * Returns the last item from the short array.
     * <pre class="groovyTestCase">
     * short[] shorts = [10, 20, 30]
     * assert shorts.last() == 30
     * </pre>
     *
     * @param self an array
     * @return the last element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static short last(short[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "last");
        return self[self.length - 1];
    }

    /**
     * Returns the last item from the int array.
     * <pre class="groovyTestCase">
     * int[] ints = [1, 3, 5]
     * assert ints.last() == 5
     * </pre>
     *
     * @param self an array
     * @return the last element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static int last(int[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "last");
        return self[self.length - 1];
    }

    /**
     * Returns the last item from the long array.
     * <pre class="groovyTestCase">
     * long[] longs = [2L, 4L, 6L]
     * assert longs.last() == 6L
     * </pre>
     *
     * @param self an array
     * @return the last element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static long last(long[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "last");
        return self[self.length - 1];
    }

    /**
     * Returns the last item from the float array.
     * <pre class="groovyTestCase">
     * float[] floats = [2.0f, 4.0f, 6.0f]
     * assert floats.last() == 6.0f
     * </pre>
     *
     * @param self an array
     * @return the last element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static float last(float[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "last");
        return self[self.length - 1];
    }

    /**
     * Returns the last item from the double array.
     * <pre class="groovyTestCase">
     * double[] doubles = [10.0d, 20.0d, 30.0d]
     * assert doubles.last() == 30.0d
     * </pre>
     *
     * @param self an array
     * @return the last element
     * @throws NoSuchElementException if the array is empty
     * @since 5.0.0
     */
    public static double last(double[] self) {
        Objects.requireNonNull(self);
        throwNoSuchElementIfEmpty(self.length, "last");
        return self[self.length - 1];
    }

    //--------------------------------------------------------------------------
    // max

    /**
     * Adds max() method to int arrays.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * int[] nums = [1, 3, 2]
     * assert 3 == nums.max()
     * </pre>
     *
     * @param self an int array
     * @return the maximum value
     * @since 3.0.8
     */
    public static int max(int[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "max");
        int answer = self[0];
        for (int i = 1; i < self.length; i++) {
            int value = self[i];
            if (value > answer) answer = value;
        }
        return answer;
    }

    /**
     * Adds max() method to long arrays.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * long[] nums = [1L, 3L, 2L]
     * assert 3L == nums.max()
     * </pre>
     *
     * @param self a long array
     * @return the maximum value
     * @since 3.0.8
     */
    public static long max(long[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "max");
        long answer = self[0];
        for (int i = 1; i < self.length; i++) {
            long value = self[i];
            if (value > answer) answer = value;
        }
        return answer;
    }

    /**
     * Adds max() method to double arrays.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * double[] nums = [1.1d, 3.3d, 2.2d]
     * assert 3.3d == nums.max()
     * </pre>
     *
     * @param self a double array
     * @return the maximum value
     * @since 3.0.8
     */
    public static double max(double[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "max");
        double answer = self[0];
        for (int i = 1; i < self.length; i++) {
            double value = self[i];
            if (value > answer) answer = value;
        }
        return answer;
    }

    /**
     * Adds max() method to Object arrays.
     *
     * @param self an array
     * @return the maximum value
     * @see #max(Iterator)
     * @since 1.5.5
     */
    public static <T> T max(T[] self) {
        return DefaultGroovyMethods.max(new ArrayIterator<>(self));
    }

    //

    /**
     * Selects the maximum value found from the int array
     * using the supplier IntBinaryOperator as a comparator to determine the maximum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * int[] nums = [10, 20, -30]
     * assert 20 == nums.max{ n, m {@code ->} n {@code <=>} m }
     * assert -30 == nums.max{ n, m {@code ->} n.abs() {@code <=>} m.abs() }
     * </pre>
     * <p>
     *
     * @param self     an int array
     * @param comparator a comparator, i.e. returns a negative value if the first parameter is less than the second
     * @return the maximum value
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    @Incubating
    public static int max(int[] self, IntComparator comparator) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "max");

        int maxV = self[0];
        for (int i = 1; i < self.length; i++) {
            int v = self[i];
            if (comparator.compare(v, maxV) > 0) {
                maxV = v;
            }
        }
        return maxV;
    }

    /**
     * Selects the maximum value found from the int array
     * using the supplier IntUnaryOperator to determine the maximum of any two values.
     * The operator is applied to each array element and the results are compared.
     * <p>
     * <pre class="groovyTestCase">
     * int[] nums = [10, 20, -30]
     * assert 20 == nums.max{ it }
     * assert -30 == nums.max{ it.abs() }
     * </pre>
     * <p>
     *
     * @param self     an int array
     * @param operator an operator that returns an int used for comparing values
     * @return the maximum value
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    @Incubating
    public static int max(int[] self, IntUnaryOperator operator) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "max");

        int maxV = self[0];
        for (int i = 1; i < self.length; i++) {
            int v = self[i];
            if (operator.applyAsInt(v) > operator.applyAsInt(maxV)) {
                maxV = v;
            }
        }
        return maxV;
    }

    /**
     * Selects the maximum value found from the long array
     * using the supplier LongBinaryOperator as a comparator to determine the maximum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * long[] nums = [10L, 20L, -30L]
     * assert 20L == nums.max{ n, m {@code ->} n {@code <=>} m }
     * assert -30L == nums.max{ n, m {@code ->} n.abs() {@code <=>} m.abs() }
     * </pre>
     * <p>
     *
     * @param self     a long array
     * @param comparator a comparator, i.e. returns a negative value if the first parameter is less than the second
     * @return the maximum value
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    @Incubating
    public static long max(long[] self, LongComparator comparator) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "max");

        long maxV = self[0];
        for (int i = 1; i < self.length; i++) {
            long v = self[i];
            if (comparator.compare(v, maxV) > 0) {
                maxV = v;
            }
        }
        return maxV;
    }

    /**
     * Selects the maximum value found from the long array
     * using the supplier LongUnaryOperator to determine the maximum of any two values.
     * The operator is applied to each array element and the results are compared.
     * <p>
     * <pre class="groovyTestCase">
     * long[] nums = [10L, 20L, -30L]
     * assert 20L == nums.max{ it }
     * assert -30L == nums.max{ it.abs() }
     * </pre>
     * <p>
     *
     * @param self     a long array
     * @param operator an operator that returns a long used for comparing values
     * @return the maximum value
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    @Incubating
    public static long max(long[] self, LongUnaryOperator operator) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "max");

        long maxV = self[0];
        for (int i = 1; i < self.length; i++) {
            long v = self[i];
            if (operator.applyAsLong(v) > operator.applyAsLong(maxV)) {
                maxV = v;
            }
        }
        return maxV;
    }

    /**
     * Selects the maximum value found from the double array
     * using the supplier DoubleComparator to determine the maximum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * double[] nums = [10d, 20d, -30d]
     * assert 20d == nums.max{ n, m {@code ->} n {@code <=>} m }
     * assert -30d == nums.max{ n, m {@code ->} n.abs() {@code <=>} m.abs() }
     * </pre>
     * <p>
     *
     * @param self       a double array
     * @param comparator a comparator, i.e. returns a negative value if the first parameter is less than the second
     * @return the maximum value
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    @Incubating
    public static double max(double[] self, DoubleComparator comparator) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "max");

        double maxV = self[0];
        for (int i = 1; i < self.length; i++) {
            double v = self[i];
            if (comparator.compare(v, maxV) > 0) {
                maxV = v;
            }
        }
        return maxV;
    }

    /**
     * Selects the maximum value found from the double array
     * using the supplier DoubleUnaryOperator to determine the maximum of any two values.
     * The operator is applied to each array element and the results are compared.
     * <p>
     * <pre class="groovyTestCase">
     * double[] nums = [10d, 20d, -30d]
     * assert -30d == nums.max{ it.abs() }
     * assert 20d == nums.max{ it }
     * </pre>
     * <p>
     *
     * @param self     a double array
     * @param operator an operator that returns a double used for comparing values
     * @return the maximum value
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    @Incubating
    public static double max(double[] self, DoubleUnaryOperator operator) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "max");

        double maxV = self[0];
        for (int i = 1; i < self.length; i++) {
            double v = self[i];
            if (operator.applyAsDouble(v) > operator.applyAsDouble(maxV)) {
                maxV = v;
            }
        }
        return maxV;
    }

    /**
     * Selects the maximum value found from the Object array using the given comparator.
     *
     * @param self       an array
     * @param comparator a Comparator
     * @return the maximum value
     * @see #max(Iterator, Comparator)
     * @since 1.5.5
     */
    public static <T> T max(T[] self, Comparator<? super T> comparator) {
        return DefaultGroovyMethods.max(new ArrayIterator<>(self), comparator);
    }

    /**
     * Selects the maximum value found from the Object array
     * using the closure to determine the correct ordering.
     * <p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     *
     * @param self    an array
     * @param closure a Closure used to determine the correct ordering
     * @return the maximum value
     * @since 1.5.5
     */
    public static <T> T max(T[] self, @ClosureParams(value=FromString.class,options={"T","T,T"}) Closure<?> closure) {
        return DefaultGroovyMethods.max(new ArrayIterator<>(self), closure);
    }

    //

    /**
     * Selects the maximum value found from the int array
     * using the closure to determine the maximum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * int[] nums = [30, 45, 60, 90]
     * assert 90 == nums.maxBy{ Math.sin(Math.toRadians(it)) }
     * assert 30 == nums.maxBy{ Math.cos(Math.toRadians(it)) } // cos(90) == 0
     * </pre>
     * <p>
     * If the closure has two parameters it is used like a traditional Comparator,
     * i.e., it should compare its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     *
     * @param self    an int array
     * @param closure a Closure used to determine the correct ordering
     * @return the maximum value
     * @see DefaultGroovyMethods#max(Iterator, groovy.lang.Closure)
     * @since 5.0.0
     */
    @Incubating
    public static int maxBy(int[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        return DefaultGroovyMethods.max(new IntArrayIterator(self), closure);
    }

    /**
     * Selects the maximum value found from the long array
     * using the closure to determine the maximum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * long[] nums = [-30L, 10L, 20L]
     * assert 20L == nums.maxBy{ a, b {@code ->} a {@code <=>} b }
     * assert -30L == nums.maxBy{ it.abs() }
     * </pre>
     * <p>
     * If the closure has two parameters it is used like a traditional Comparator,
     * i.e., it should compare its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     *
     * @param self    a long array
     * @param closure a Closure used to determine the correct ordering
     * @return the maximum value
     * @see DefaultGroovyMethods#max(Iterator, groovy.lang.Closure)
     * @since 5.0.0
     */
    @Incubating
    public static long maxBy(long[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        return DefaultGroovyMethods.max(new LongArrayIterator(self), closure);
    }

    /**
     * Selects the maximum value found from the double array
     * using the closure to determine the maximum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * double[] nums = [-30.0d, 10.0d, 20.0d]
     * assert 20.0d == nums.maxBy{ a, b {@code ->} a {@code <=>} b }
     * assert -30.0d == nums.maxBy{ it.abs() }
     * </pre>
     * <p>
     * If the closure has two parameters it is used like a traditional Comparator,
     * i.e., it should compare its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     *
     * @param self    a double array
     * @param closure a Closure used to determine the correct ordering
     * @return the maximum value
     * @see DefaultGroovyMethods#max(Iterator, groovy.lang.Closure)
     * @since 5.0.0
     */
    @Incubating
    public static double maxBy(double[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        return DefaultGroovyMethods.max(new DoubleArrayIterator(self), closure);
    }

    //

    /**
     * Selects the maximum value found from the int array
     * using the comparator to determine the maximum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * int[] nums = [10, 20, 30]
     * assert 30 == nums.maxComparing(Comparator.naturalOrder())
     * assert 10 == nums.maxComparing(Comparator.reverseOrder())
     * </pre>
     * <p>
     *
     * @param self       an int array
     * @param comparator a Comparator
     * @return the maximum value
     * @see DefaultGroovyMethods#max(Iterator, java.util.Comparator)
     * @since 5.0.0
     */
    @Incubating
    public static int maxComparing(int[] self, Comparator<Integer> comparator) {
        return DefaultGroovyMethods.max(new IntArrayIterator(self), comparator);
    }

    /**
     * Selects the maximum value found from the long array
     * using the comparator to determine the maximum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * long[] nums = [10L, 20L, 30L]
     * assert 30L == nums.maxComparing(Comparator.naturalOrder())
     * assert 10L == nums.maxComparing(Comparator.reverseOrder())
     * </pre>
     * <p>
     *
     * @param self       a long array
     * @param comparator a Comparator
     * @return the maximum value
     * @see DefaultGroovyMethods#max(Iterator, java.util.Comparator)
     * @since 5.0.0
     */
    @Incubating
    public static long maxComparing(long[] self, Comparator<Long> comparator) {
        return DefaultGroovyMethods.max(new LongArrayIterator(self), comparator);
    }

    /**
     * Selects the maximum value found from the double array
     * using the comparator to determine the maximum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * double[] nums = [10.0d, 20.0d, 30.0d]
     * assert 30d == nums.maxComparing(Comparator.naturalOrder())
     * assert 10d == nums.maxComparing(Comparator.reverseOrder())
     * </pre>
     * <p>
     *
     * @param self       a double array
     * @param comparator a Comparator
     * @return the maximum value
     * @see DefaultGroovyMethods#max(Iterator, java.util.Comparator)
     * @since 5.0.0
     */
    @Incubating
    public static double maxComparing(double[] self, Comparator<Double> comparator) {
        return DefaultGroovyMethods.max(new DoubleArrayIterator(self), comparator);
    }

    //--------------------------------------------------------------------------
    // min

    /**
     * Adds min() method to int arrays.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * int[] nums = [20, 10, 30]
     * assert 10 == nums.min()
     * </pre>
     *
     * @param self an int array
     * @return the minimum value
     * @since 3.0.8
     */
    public static int min(int[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "max");
        int answer = self[0];
        for (int i = 1; i < self.length; i++) {
            int value = self[i];
            if (value < answer) answer = value;
        }
        return answer;
    }

    /**
     * Adds min() method to long arrays.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * long[] nums = [20L, 10L, 30L]
     * assert 10L == nums.min()
     * </pre>
     *
     * @param self a long array
     * @return the minimum value
     * @since 3.0.8
     */
    public static long min(long[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "max");
        long answer = self[0];
        for (int i = 1; i < self.length; i++) {
            long value = self[i];
            if (value < answer) answer = value;
        }
        return answer;
    }

    /**
     * Adds min() method to double arrays.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * double[] nums = [20.0d, 10.0d, 30.0d]
     * assert 10.0d == nums.min()
     * </pre>
     *
     * @param self a double array
     * @return the minimum value
     * @since 3.0.8
     */
    public static double min(double[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "max");
        double answer = self[0];
        for (int i = 1; i < self.length; i++) {
            double value = self[i];
            if (value < answer) answer = value;
        }
        return answer;
    }

    /**
     * Adds min() method to Object arrays.
     *
     * @param self an array
     * @return the minimum value
     * @see #min(Iterator)
     * @since 1.5.5
     */
    public static <T> T min(T[] self) {
        return DefaultGroovyMethods.min(new ArrayIterator<>(self));
    }

    //

    /**
     * Selects the minimum value found from the int array
     * using the supplier IntComparator to determine the minimum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * int[] nums = [10, -20, 30]
     * assert -20 == nums.min{ n, m {@code ->} n {@code <=>} m }
     * assert 10 == nums.min{ n, m {@code ->} n.abs() {@code <=>} m.abs() }
     * </pre>
     * <p>
     *
     * @param self       an int array
     * @param comparator a comparator, i.e. returns a negative value if the first parameter is less than the second
     * @return the minimum value
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    @Incubating
    public static int min(int[] self, IntComparator comparator) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "min");

        int minV = self[0];
        for (int i = 1; i < self.length; i++) {
            int v = self[i];
            if (comparator.compare(v, minV) < 0) {
                minV = v;
            }
        }
        return minV;
    }

    /**
     * Selects the minimum value found from the int array
     * using the supplier IntUnaryOperator to determine the minimum of any two values.
     * The operator is applied to each array element and the results are compared.
     * <p>
     * <pre class="groovyTestCase">
     * int[] nums = [10, -20, 30]
     * assert -20L == nums.min{ n {@code ->} n }
     * assert 10L == nums.min{ n {@code ->} n.abs() }
     * </pre>
     * <p>
     *
     * @param self     an int array
     * @param operator an operator that returns an int used for comparing values
     * @return the minimum value
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    @Incubating
    public static int min(int[] self, IntUnaryOperator operator) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "min");

        int minV = self[0];
        for (int i = 1; i < self.length; i++) {
            int v = self[i];
            if (operator.applyAsInt(v) < operator.applyAsInt(minV)) {
                minV = v;
            }
        }
        return minV;
    }

    /**
     * Selects the minimum value found from the long array
     * using the supplier LongBinaryOperator as a comparator to determine the minimum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * long[] nums = [10L, -20L, 30L]
     * assert -20L == nums.min{ n, m {@code ->} n {@code <=>} m }
     * assert 10L == nums.min{ n, m {@code ->} n.abs() {@code <=>} m.abs() }
     * </pre>
     * <p>
     *
     * @param self       a long array
     * @param comparator a comparator, i.e. returns a negative value if the first parameter is less than the second
     * @return the minimum value
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    @Incubating
    public static long min(long[] self, LongComparator comparator) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "min");

        long minV = self[0];
        for (int i = 1; i < self.length; i++) {
            long v = self[i];
            if (comparator.compare(v, minV) < 0) {
                minV = v;
            }
        }
        return minV;
    }

    /**
     * Selects the minimum value found from the long array
     * using the supplier LongUnaryOperator to determine the minimum of any two values.
     * The operator is applied to each array element and the results are compared.
     * <p>
     * <pre class="groovyTestCase">
     * long[] nums = [10L, -20L, 30L]
     * assert -20L == nums.min{ it }
     * assert 10L == nums.min{ it.abs() }
     * </pre>
     * <p>
     *
     * @param self     a long array
     * @param operator an operator that returns a long used for comparing values
     * @return the minimum value
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    @Incubating
    public static long min(long[] self, LongUnaryOperator operator) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "min");

        long minV = self[0];
        for (int i = 1; i < self.length; i++) {
            long v = self[i];
            if (operator.applyAsLong(v) < operator.applyAsLong(minV)) {
                minV = v;
            }
        }
        return minV;
    }

    /**
     * Selects the minimum value found from the double array
     * using the supplier DoubleBinaryOperator as a comparator to determine the minimum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * double[] nums = [10d, -20d, 30d]
     * assert -20d == nums.min{ n, m {@code ->} n {@code <=>} m }
     * assert 10d == nums.min{ n, m {@code ->} n.abs() {@code <=>} m.abs() }
     * </pre>
     * <p>
     *
     * @param self       a double array
     * @param comparator a comparator, i.e. returns a negative value if the first parameter is less than the second
     * @return the minimum value
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    @Incubating
    public static double min(double[] self, DoubleComparator comparator) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "min");

        double minV = self[0];
        for (int i = 1; i < self.length; i++) {
            double v = self[i];
            if (comparator.compare(v, minV) < 0) {
                minV = v;
            }
        }
        return minV;
    }

    /**
     * Selects the minimum value found from the double array
     * using the supplier DoubleUnaryOperator to determine the minimum of any two values.
     * The operator is applied to each array element and the results are compared.
     * <p>
     * <pre class="groovyTestCase">
     * double[] nums = [10d, -20d, 30d]
     * assert -20d == nums.min{ it }
     * assert 10d == nums.min{ it.abs() }
     * </pre>
     * <p>
     *
     * @param self     a double array
     * @param operator an operator that returns a double used for comparing values
     * @return the minimum value
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    @Incubating
    public static double min(double[] self, DoubleUnaryOperator operator) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "min");

        double minV = self[0];
        for (int i = 1; i < self.length; i++) {
            double v = self[i];
            if (operator.applyAsDouble(v) < operator.applyAsDouble(minV)) {
                minV = v;
            }
        }
        return minV;
    }

    /**
     * Selects the minimum value found from the Object array using the given comparator.
     *
     * @param self       an array
     * @param comparator a Comparator
     * @return the minimum value
     * @see #min(Iterator, java.util.Comparator)
     * @since 1.5.5
     */
    public static <T> T min(T[] self, Comparator<? super T> comparator) {
        return DefaultGroovyMethods.min(new ArrayIterator<>(self), comparator);
    }

    /**
     * Selects the minimum value found from the Object array
     * using the closure to determine the correct ordering.
     * <p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     *
     * @param self    an array
     * @param closure a Closure used to determine the correct ordering
     * @return the minimum value
     * @see #min(Iterator, groovy.lang.Closure)
     * @since 1.5.5
     */
    public static <T> T min(T[] self, @ClosureParams(value=FromString.class,options={"T","T,T"}) Closure<?> closure) {
        return DefaultGroovyMethods.min(new ArrayIterator<>(self), closure);
    }

    //

    /**
     * Selects the minimum value found from the int array
     * using the closure to determine the minimum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * int[] nums = [-20, 10, 30]
     * assert -20 == nums.minBy{ a, b {@code ->} a {@code <=>} b }
     * assert 10 == nums.minBy{ it.abs() }
     * </pre>
     * <p>
     * If the closure has two parameters it is used like a traditional Comparator,
     * i.e., it should compare its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     *
     * @param self    an int array
     * @param closure a Closure used to determine the correct ordering
     * @return the minimum value
     * @see DefaultGroovyMethods#min(Iterator, groovy.lang.Closure)
     * @since 5.0.0
     */
    @Incubating
    public static int minBy(int[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        return DefaultGroovyMethods.min(new IntArrayIterator(self), closure);
    }

    /**
     * Selects the minimum value found from the long array
     * using the closure to determine the minimum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * long[] nums = [-20L, 10L, 30L]
     * assert -20L == nums.minBy{ a, b {@code ->} a {@code <=>} b }
     * assert 10L == nums.minBy{ it.abs() }
     * </pre>
     * <p>
     * If the closure has two parameters it is used like a traditional Comparator,
     * i.e., it should compare its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an int or long) which is then used for
     * further comparison.
     *
     * @param self    a long array
     * @param closure a Closure used to determine the correct ordering
     * @return the minimum value
     * @see DefaultGroovyMethods#min(Iterator, groovy.lang.Closure)
     * @since 5.0.0
     */
    @Incubating
    public static long minBy(long[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        return DefaultGroovyMethods.min(new LongArrayIterator(self), closure);
    }

    /**
     * Selects the minimum value found from the double array
     * using the closure to determine the minimum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * double[] nums = [-20.0d, 10.0d, 30.0d]
     * assert -20.0d == nums.minBy{ a, b {@code ->} a {@code <=>} b }
     * assert 10.0d == nums.minBy{ it.abs() }
     * </pre>
     * <p>
     * If the closure has two parameters it is used like a traditional Comparator,
     * i.e., it should compare its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an int or double) which is then used for
     * further comparison.
     *
     * @param self    a double array
     * @param closure a Closure used to determine the correct ordering
     * @return the minimum value
     * @see DefaultGroovyMethods#min(Iterator, groovy.lang.Closure)
     * @since 5.0.0
     */
    @Incubating
    public static double minBy(double[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        return DefaultGroovyMethods.min(new DoubleArrayIterator(self), closure);
    }

    //

    /**
     * Selects the minimum value found from the int array
     * using the comparator to determine the minimum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * int[] nums = [1, 2, 3]
     * assert 1 == nums.minComparing(Comparator.naturalOrder())
     * assert 3 == nums.minComparing(Comparator.reverseOrder())
     * </pre>
     * <p>
     *
     * @param self       an int array
     * @param comparator a Comparator
     * @return the minimum value
     * @see DefaultGroovyMethods#min(Iterator, java.util.Comparator)
     * @since 5.0.0
     */
    @Incubating
    public static int minComparing(int[] self, Comparator<Integer> comparator) {
        return DefaultGroovyMethods.min(new IntArrayIterator(self), comparator);
    }

    /**
     * Selects the minimum value found from the long array
     * using the comparator to determine the minimum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * long[] nums = [10L, 20L, 30L]
     * assert 10L == nums.minComparing(Comparator.naturalOrder())
     * assert 30L == nums.minComparing(Comparator.reverseOrder())
     * </pre>
     * <p>
     *
     * @param self       a long array
     * @param comparator a Comparator
     * @return the minimum value
     * @see DefaultGroovyMethods#min(Iterator, java.util.Comparator)
     * @since 5.0.0
     */
    @Incubating
    public static long minComparing(long[] self, Comparator<Long> comparator) {
        return DefaultGroovyMethods.min(new LongArrayIterator(self), comparator);
    }

    /**
     * Selects the minimum value found from the double array
     * using the comparator to determine the minimum of any two values.
     * <p>
     * <pre class="groovyTestCase">
     * double[] nums = [10.0d, 20.0d, 30.0d]
     * assert 10d == nums.minComparing(Comparator.naturalOrder())
     * assert 30d == nums.minComparing(Comparator.reverseOrder())
     * </pre>
     * <p>
     *
     * @param self       a double array
     * @param comparator a Comparator
     * @return the minimum value
     * @see DefaultGroovyMethods#min(Iterator, java.util.Comparator)
     * @since 5.0.0
     */
    @Incubating
    public static double minComparing(double[] self, Comparator<Double> comparator) {
        return DefaultGroovyMethods.min(new DoubleArrayIterator(self), comparator);
    }

    //--------------------------------------------------------------------------
    // minus

    //--------------------------------------------------------------------------
    // plus

    //--------------------------------------------------------------------------
    // reverse

    /**
     * Creates a new boolean array containing items which are the same as this array but in reverse order.
     * <pre class="groovyTestCase">
     * boolean[] array = [false, true]
     * assert array.reverse() == [true, false]
     * </pre>
     *
     * @param self a boolean array
     * @return an array containing the reversed items
     * @see #reverse(boolean[], boolean)
     * @since 5.0.0
     */
    public static boolean[] reverse(boolean[] self) {
        return reverse(self, false);
    }

    /**
     * Reverse the items in an array. If mutate is true, the original array is modified in place and returned.
     * Otherwise, a new array containing the reversed items is produced.
     * <pre class="groovyTestCase">
     * boolean[] array = [false, true, true]
     * def yarra = array.reverse(true)
     * assert array == [true, true, false]
     * assert yarra == [true, true, false]
     * assert array === yarra
     * yarra = array.reverse(false)
     * assert array !== yarra
     * assert array == [true, true, false]
     * assert yarra == [false, true, true]
     * </pre>
     *
     * @param self   a boolean array
     * @param mutate {@code true} if the array itself should be reversed in place, {@code false} if a new array should be created
     * @return an array containing the reversed items
     * @since 5.0.0
     */
    public static boolean[] reverse(boolean[] self, boolean mutate) {
        int len = self.length;
        if (!mutate) self = self.clone();
        for (int i=0, mid=len>>1, j=len-1; i<mid; i++, j--)
            swap(self, i, j);
        return self;
    }

    /**
     * Creates a new byte array containing items which are the same as this array but in reverse order.
     * <pre class="groovyTestCase">
     * byte[] array = 1..2
     * assert array.reverse() == 2..1
     * </pre>
     *
     * @param self a byte array
     * @return an array containing the reversed items
     * @see #reverse(byte[], boolean)
     * @since 5.0.0
     */
    public static byte[] reverse(byte[] self) {
        return reverse(self, false);
    }

    /**
     * Reverse the items in an array. If mutate is true, the original array is modified in place and returned.
     * Otherwise, a new array containing the reversed items is produced.
     * <pre class="groovyTestCase">
     * byte[] array = 1..3
     * def yarra = array.reverse(true)
     * assert array == 3..1
     * assert yarra == 3..1
     * assert array === yarra
     * yarra = array.reverse(false)
     * assert array !== yarra
     * assert array == 3..1
     * assert yarra == 1..3
     * </pre>
     *
     * @param self   a byte array
     * @param mutate {@code true} if the array itself should be reversed in place, {@code false} if a new array should be created
     * @return an array containing the reversed items
     * @since 5.0.0
     */
    public static byte[] reverse(byte[] self, boolean mutate) {
        int len = self.length;
        if (!mutate) self = self.clone();
        for (int i=0, mid=len>>1, j=len-1; i<mid; i++, j--)
            swap(self, i, j);
        return self;
    }

    /**
     * Creates a new char array containing items which are the same as this array but in reverse order.
     * <pre class="groovyTestCase">
     * char[] array = ['a', 'b']
     * assert array.reverse() == ['b', 'a'] as char[]
     * </pre>
     *
     * @param self a char array
     * @return an array containing the reversed items
     * @see #reverse(char[], boolean)
     * @since 5.0.0
     */
    public static char[] reverse(char[] self) {
        return reverse(self, false);
    }

    /**
     * Reverse the items in an array. If mutate is true, the original array is modified in place and returned.
     * Otherwise, a new array containing the reversed items is produced.
     * <pre class="groovyTestCase">
     * char[] array = ['a', 'b', 'c']
     * def yarra = array.reverse(true)
     * assert array == ['c', 'b', 'a']
     * assert yarra == ['c', 'b', 'a']
     * assert array === yarra
     * yarra = array.reverse(false)
     * assert array !== yarra
     * assert array == ['c', 'b', 'a']
     * assert yarra == ['a', 'b', 'c']
     * </pre>
     *
     * @param self   a char array
     * @param mutate {@code true} if the array itself should be reversed in place, {@code false} if a new array should be created
     * @return an array containing the reversed items
     * @since 5.0.0
     */
    public static char[] reverse(char[] self, boolean mutate) {
        int len = self.length;
        if (!mutate) self = self.clone();
        for (int i=0, mid=len>>1, j=len-1; i<mid; i++, j--)
            swap(self, i, j);
        return self;
    }

    /**
     * Creates a new short array containing items which are the same as this array but in reverse order.
     * <pre class="groovyTestCase">
     * short[] array = 1..2
     * assert array.reverse() == 2..1
     * </pre>
     *
     * @param self a short array
     * @return an array containing the reversed items
     * @see #reverse(short[], boolean)
     * @since 5.0.0
     */
    public static short[] reverse(short[] self) {
        return reverse(self, false);
    }

    /**
     * Reverse the items in an array. If mutate is true, the original array is modified in place and returned.
     * Otherwise, a new array containing the reversed items is produced.
     * <pre class="groovyTestCase">
     * short[] array = 1..3
     * def yarra = array.reverse(true)
     * assert array == 3..1
     * assert yarra == 3..1
     * assert array === yarra
     * yarra = array.reverse(false)
     * assert array !== yarra
     * assert array == 3..1
     * assert yarra == 1..3
     * </pre>
     *
     * @param self   a short array
     * @param mutate {@code true} if the array itself should be reversed in place, {@code false} if a new array should be created
     * @return an array containing the reversed items
     * @since 5.0.0
     */
    public static short[] reverse(short[] self, boolean mutate) {
        int len = self.length;
        if (!mutate) self = self.clone();
        for (int i=0, mid=len>>1, j=len-1; i<mid; i++, j--)
            swap(self, i, j);
        return self;
    }

    /**
     * Creates a new int array containing items which are the same as this array but in reverse order.
     * <pre class="groovyTestCase">
     * int[] array = 1..2
     * assert array.reverse() == 2..1
     * </pre>
     *
     * @param self an int array
     * @return an array containing the reversed items
     * @see #reverse(int[], boolean)
     * @since 5.0.0
     */
    public static int[] reverse(int[] self) {
        return reverse(self, false);
    }

    /**
     * Reverse the items in an array. If mutate is true, the original array is modified in place and returned.
     * Otherwise, a new array containing the reversed items is produced.
     * <pre class="groovyTestCase">
     * int[] array = 1..3
     * def yarra = array.reverse(true)
     * assert array == 3..1
     * assert yarra == 3..1
     * assert array === yarra
     * yarra = array.reverse(false)
     * assert array !== yarra
     * assert array == 3..1
     * assert yarra == 1..3
     * </pre>
     *
     * @param self   an int array
     * @param mutate {@code true} if the array itself should be reversed in place, {@code false} if a new array should be created
     * @return an array containing the reversed items
     * @since 5.0.0
     */
    public static int[] reverse(int[] self, boolean mutate) {
        int len = self.length;
        if (!mutate) self = self.clone();
        for (int i=0, mid=len>>1, j=len-1; i<mid; i++, j--)
            swap(self, i, j);
        return self;
    }

    /**
     * Creates a new long array containing items which are the same as this array but in reverse order.
     * <pre class="groovyTestCase">
     * long[] array = 1L..2L
     * assert array.reverse() == 2L..1L
     * </pre>
     *
     * @param self a long array
     * @return an array containing the reversed items
     * @see #reverse(long[], boolean)
     * @since 5.0.0
     */
    public static long[] reverse(long[] self) {
        return reverse(self, false);
    }

    /**
     * Reverse the items in an array. If mutate is true, the original array is modified in place and returned.
     * Otherwise, a new array containing the reversed items is produced.
     * <pre class="groovyTestCase">
     * long[] array = 1L..3L
     * def yarra = array.reverse(true)
     * assert array == 3L..1L
     * assert yarra == 3L..1L
     * assert array === yarra
     * yarra = array.reverse(false)
     * assert array !== yarra
     * assert array == 3L..1L
     * assert yarra == 1L..3L
     * </pre>
     *
     * @param self   a long array
     * @param mutate {@code true} if the array itself should be reversed in place, {@code false} if a new array should be created
     * @return an array containing the reversed items
     * @since 5.0.0
     */
    public static long[] reverse(long[] self, boolean mutate) {
        int len = self.length;
        if (!mutate) self = self.clone();
        for (int i=0, mid=len>>1, j=len-1; i<mid; i++, j--)
            swap(self, i, j);
        return self;
    }

    /**
     * Creates a new float array containing items which are the same as this array but in reverse order.
     * <pre class="groovyTestCase">
     * float[] array = [1f, 2f]
     * assert array.reverse() == [2f, 1f]
     * </pre>
     *
     * @param self a float array
     * @return an array containing the reversed items
     * @see #reverse(float[], boolean)
     * @since 5.0.0
     */
    public static float[] reverse(float[] self) {
        return reverse(self, false);
    }

    /**
     * Reverse the items in an array. If mutate is true, the original array is modified in place and returned.
     * Otherwise, a new array containing the reversed items is produced.
     * <pre class="groovyTestCase">
     * float[] array = 1f..3f
     * def yarra = array.reverse(true)
     * assert array == 3f..1f
     * assert yarra == 3f..1f
     * assert array === yarra
     * yarra = array.reverse(false)
     * assert array !== yarra
     * assert array == 3f..1f
     * assert yarra == 1f..3f
     * </pre>
     *
     * @param self   a float array
     * @param mutate {@code true} if the array itself should be reversed in place, {@code false} if a new array should be created
     * @return an array containing the reversed items
     * @since 5.0.0
     */
    public static float[] reverse(float[] self, boolean mutate) {
        int len = self.length;
        if (!mutate) self = self.clone();
        for (int i=0, mid=len>>1, j=len-1; i<mid; i++, j--)
            swap(self, i, j);
        return self;
    }

    /**
     * Creates a new double array containing items which are the same as this array but in reverse order.
     * <pre class="groovyTestCase">
     * double[] array = [1d, 2d]
     * assert array.reverse() == [2d, 1d]
     * </pre>
     *
     * @param self a double array
     * @return an array containing the reversed items
     * @see #reverse(double[], boolean)
     * @since 5.0.0
     */
    public static double[] reverse(double[] self) {
        return reverse(self, false);
    }

    /**
     * Reverse the items in an array. If mutate is true, the original array is modified in place and returned.
     * Otherwise, a new array containing the reversed items is produced.
     * <pre class="groovyTestCase">
     * double[] array = 1d..3d
     * def yarra = array.reverse(true)
     * assert array == 3d..1d
     * assert yarra == 3d..1d
     * assert array === yarra
     * yarra = array.reverse(false)
     * assert array !== yarra
     * assert array == 3d..1d
     * assert yarra == 1d..3d
     * </pre>
     *
     * @param self   a double array
     * @param mutate {@code true} if the array itself should be reversed in place, {@code false} if a new array should be created
     * @return an array containing the reversed items
     * @since 5.0.0
     */
    public static double[] reverse(double[] self, boolean mutate) {
        int len = self.length;
        if (!mutate) self = self.clone();
        for (int i=0, mid=len>>1, j=len-1; i<mid; i++, j--)
            swap(self, i, j);
        return self;
    }

    //--------------------------------------------------------------------------
    // reverseEach

    /**
     * Iterates through a boolean[] in reverse order passing each boolean to the given closure.
     * <pre class="groovyTestCase">
     * boolean[] array = [false, true, true]
     * String result = ''
     * array.reverseEach{ result += it }
     * assert result == 'truetruefalse'
     * </pre>
     *
     * @param self    the boolean array over which we iterate
     * @param closure the closure applied on each boolean
     * @return the self array
     * @since 5.0.0
     */
    public static boolean[] reverseEach(boolean[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        Objects.requireNonNull(self);
        for (int i = self.length - 1; i >= 0; i--) {
            closure.call(self[i]);
        }
        return self;
    }

    /**
     * Iterates through a byte[] in reverse order passing each byte to the given closure.
     * <pre class="groovyTestCase">
     * byte[] array = [0, 1, 2]
     * String result = ''
     * array.reverseEach{ result += it }
     * assert result == '210'
     * </pre>
     *
     * @param self    the byte array over which we iterate
     * @param closure the closure applied on each byte
     * @return the self array
     * @since 5.0.0
     */
    public static byte[] reverseEach(byte[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        Objects.requireNonNull(self);
        for (int i = self.length - 1; i >= 0; i--) {
            closure.call(self[i]);
        }
        return self;
    }

    /**
     * Iterates through a char[] in reverse order passing each char to the given closure.
     * <pre class="groovyTestCase">
     * char[] array = 'abc'.chars
     * String result = ''
     * array.reverseEach{ result += it }
     * assert result == 'cba'
     * </pre>
     *
     * @param self    the char array over which we iterate
     * @param closure the closure applied on each char
     * @return the self array
     * @since 5.0.0
     */
    public static char[] reverseEach(char[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        Objects.requireNonNull(self);
        for (int i = self.length - 1; i >= 0; i--) {
            closure.call(self[i]);
        }
        return self;
    }

    /**
     * Iterates through a short[] in reverse order passing each short to the given closure.
     * <pre class="groovyTestCase">
     * short[] array = [0, 1, 2]
     * String result = ''
     * array.reverseEach{ result += it }
     * assert result == '210'
     * </pre>
     *
     * @param self    the short array over which we iterate
     * @param closure the closure applied on each short
     * @return the self array
     * @since 5.0.0
     */
    public static short[] reverseEach(short[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        Objects.requireNonNull(self);
        for (int i = self.length - 1; i >= 0; i--) {
            closure.call(self[i]);
        }
        return self;
    }

    /**
     * Iterates through an int[] in reverse order passing each int to the given closure.
     * <pre class="groovyTestCase">
     * int[] array = [0, 1, 2]
     * String result = ''
     * array.reverseEach{ result += it }
     * assert result == '210'
     * </pre>
     *
     * @param self    the int array over which we iterate
     * @param closure the closure applied on each int
     * @return the self array
     * @since 5.0.0
     */
    public static int[] reverseEach(int[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        Objects.requireNonNull(self);
        for (int i = self.length - 1; i >= 0; i--) {
            closure.call(self[i]);
        }
        return self;
    }

    /**
     * Iterates through a long[] in reverse order passing each long to the given closure.
     * <pre class="groovyTestCase">
     * long[] array = [0, 1, 2]
     * String result = ''
     * array.reverseEach{ result += it }
     * assert result == '210'
     * </pre>
     *
     * @param self    the long array over which we iterate
     * @param closure the closure applied on each long
     * @return the self array
     * @since 5.0.0
     */
    public static long[] reverseEach(long[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        Objects.requireNonNull(self);
        for (int i = self.length - 1; i >= 0; i--) {
            closure.call(self[i]);
        }
        return self;
    }

    /**
     * Iterates through a float[] in reverse order passing each float to the given closure.
     * <pre class="groovyTestCase">
     * float[] array = [0, 1, 2]
     * String result = ''
     * array.reverseEach{ result += it }
     * assert result == '2.01.00.0'
     * </pre>
     *
     * @param self    the float array over which we iterate
     * @param closure the closure applied on each float
     * @return the self array
     * @since 5.0.0
     */
    public static float[] reverseEach(float[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        Objects.requireNonNull(self);
        for (int i = self.length - 1; i >= 0; i--) {
            closure.call(self[i]);
        }
        return self;
    }

    /**
     * Iterates through a double[] in reverse order passing each double to the given closure.
     * <pre class="groovyTestCase">
     * double[] array = [0, 1, 2]
     * String result = ''
     * array.reverseEach{ result += it }
     * assert result == '2.01.00.0'
     * </pre>
     *
     * @param self    the double array over which we iterate
     * @param closure the closure applied on each double
     * @return the self array
     * @since 5.0.0
     */
    public static double[] reverseEach(double[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        Objects.requireNonNull(self);
        for (int i = self.length - 1; i >= 0; i--) {
            closure.call(self[i]);
        }
        return self;
    }

    /**
     * Iterate over each element of the array in the reverse order.
     *
     * @param self    an array
     * @param closure a closure to which each item is passed
     * @return the original array
     * @since 1.5.2
     */
    public static <T> T[] reverseEach(T[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        Objects.requireNonNull(self);
        for (int i = self.length - 1; i >= 0; i--) {
            closure.call(self[i]);
        }
        return self;
    }

    //--------------------------------------------------------------------------
    // shuffle

    //--------------------------------------------------------------------------
    // shuffled

    //--------------------------------------------------------------------------
    // size

    /**
     * Provide arrays with a {@code size} method similar to collections.
     * <pre class="groovyTestCase">
     * boolean[] array = [true, false, true]
     * assert array.size() == 3
     * </pre>
     *
     * @param self a boolean array
     * @return the length of the array
     * @since 1.5.0
     */
    public static int size(boolean[] self) {
        return self.length;
    }

    /**
     * Provide arrays with a {@code size} method similar to collections.
     *
     * @param self a byte array
     * @return the length of the array
     * @since 1.0
     */
    public static int size(byte[] self) {
        return self.length;
    }

    /**
     * Provide arrays with a {@code size} method similar to collections.
     *
     * @param self a char array
     * @return the length of the array
     * @since 1.0
     */
    public static int size(char[] self) {
        return self.length;
    }

    /**
     * Provide arrays with a {@code size} method similar to collections.
     *
     * @param self a short array
     * @return the length of the array
     * @since 1.0
     */
    public static int size(short[] self) {
        return self.length;
    }

    /**
     * Provide arrays with a {@code size} method similar to collections.
     *
     * @param self an int array
     * @return the length of the array
     * @since 1.0
     */
    public static int size(int[] self) {
        return self.length;
    }

    /**
     * Provide arrays with a {@code size} method similar to collections.
     *
     * @param self a long array
     * @return the length of the array
     * @since 1.0
     */
    public static int size(long[] self) {
        return self.length;
    }

    /**
     * Provide arrays with a {@code size} method similar to collections.
     *
     * @param self a float array
     * @return the length of the array
     * @since 1.0
     */
    public static int size(float[] self) {
        return self.length;
    }

    /**
     * Provide arrays with a {@code size} method similar to collections.
     *
     * @param self a double array
     * @return the length of the array
     * @since 1.0
     */
    public static int size(double[] self) {
        return self.length;
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for an array.
     *
     * @param self an object array
     * @return the length of the array
     * @since 1.0
     */
    public static int size(Object[] self) {
        return self.length;
    }

    //--------------------------------------------------------------------------
    // sort

    //--------------------------------------------------------------------------
    // split

    //--------------------------------------------------------------------------
    // sum

    /**
     * Sums the items in an array.
     * <pre class="groovyTestCase">assert (1+2+3+4 as byte) == ([1,2,3,4] as byte[]).sum()</pre>
     *
     * @param self The array of values to add together
     * @return The sum of all the items
     * @since 2.4.2
     */
    public static byte sum(byte[] self) {
        return sum(self, (byte) 0);
    }

    /**
     * Sums the items in an array.
     * <pre class="groovyTestCase">assert (1+2+3+4 as char) == ([1,2,3,4] as char[]).sum()</pre>
     *
     * @param self The array of values to add together
     * @return The sum of all the items
     * @since 2.4.2
     */
    public static char sum(char[] self) {
        return sum(self, (char) 0);
    }

    /**
     * Sums the items in an array.
     * <pre class="groovyTestCase">assert (1+2+3+4 as short) == ([1,2,3,4] as short[]).sum()</pre>
     *
     * @param self The array of values to add together
     * @return The sum of all the items
     * @since 2.4.2
     */
    public static short sum(short[] self) {
        return sum(self, (short) 0);
    }

    /**
     * Sums the items in an array.
     * <pre class="groovyTestCase">assert 1+2+3+4 == ([1,2,3,4] as int[]).sum()</pre>
     *
     * @param self The array of values to add together
     * @return The sum of all the items
     * @since 2.4.2
     */
    public static int sum(int[] self) {
        return sum(self, 0);
    }

    /**
     * Sums the items in an array.
     * <pre class="groovyTestCase">assert (1+2+3+4 as long) == ([1,2,3,4] as long[]).sum()</pre>
     *
     * @param self The array of values to add together
     * @return The sum of all the items
     * @since 2.4.2
     */
    public static long sum(long[] self) {
        return sum(self, 0L);
    }

    /**
     * Sums the items in an array.
     * <pre class="groovyTestCase">assert (1+2+3+4 as float) == ([1,2,3,4] as float[]).sum()</pre>
     *
     * @param self The array of values to add together
     * @return The sum of all the items
     * @since 2.4.2
     */
    public static float sum(float[] self) {
        return sum(self, 0.0f);
    }

    /**
     * Sums the items in an array.
     * <pre class="groovyTestCase">assert (1+2+3+4 as double) == ([1,2,3,4] as double[]).sum()</pre>
     *
     * @param self The array of values to add together
     * @return The sum of all the items
     * @since 2.4.2
     */
    public static double sum(double[] self) {
        return sum(self, 0.0d);
    }

    /**
     * Sums the items in an array. This is equivalent to invoking the
     * "plus" method on all items in the array.
     *
     * @param self The array of values to add together
     * @return The sum of all the items
     * @see #sum(java.util.Iterator)
     * @since 1.7.1
     */
    public static Object sum(Object[] self) {
        return DefaultGroovyMethods.sum(new ArrayIterator<>(self), null, true);
    }

    /**
     * Sums the items in an array, adding the result to some initial value.
     * <pre class="groovyTestCase">assert (5+1+2+3+4 as byte) == ([1,2,3,4] as byte[]).sum(5 as byte)</pre>
     *
     * @param self         an array of values to sum
     * @param initialValue the items in the array will be summed to this initial value
     * @return The sum of all the items.
     * @since 2.4.2
     */
    public static byte sum(byte[] self, byte initialValue) {
        Objects.requireNonNull(self);
        byte s = initialValue;
        for (byte v : self) {
            s += v;
        }
        return s;
    }

    /**
     * Sums the items in an array, adding the result to some initial value.
     * <pre class="groovyTestCase">assert (5+1+2+3+4 as char) == ([1,2,3,4] as char[]).sum(5 as char)</pre>
     *
     * @param self         an array of values to sum
     * @param initialValue the items in the array will be summed to this initial value
     * @return The sum of all the items.
     * @since 2.4.2
     */
    public static char sum(char[] self, char initialValue) {
        Objects.requireNonNull(self);
        char s = initialValue;
        for (char v : self) {
            s += v;
        }
        return s;
    }

    /**
     * Sums the items in an array, adding the result to some initial value.
     * <pre class="groovyTestCase">assert (5+1+2+3+4 as short) == ([1,2,3,4] as short[]).sum(5 as short)</pre>
     *
     * @param self         an array of values to sum
     * @param initialValue the items in the array will be summed to this initial value
     * @return The sum of all the items.
     * @since 2.4.2
     */
    public static short sum(short[] self, short initialValue) {
        Objects.requireNonNull(self);
        short s = initialValue;
        for (short v : self) {
            s += v;
        }
        return s;
    }

    /**
     * Sums the items in an array, adding the result to some initial value.
     * <pre class="groovyTestCase">assert 5+1+2+3+4 == ([1,2,3,4] as int[]).sum(5)</pre>
     *
     * @param self         an array of values to sum
     * @param initialValue the items in the array will be summed to this initial value
     * @return The sum of all the items.
     * @since 2.4.2
     */
    public static int sum(int[] self, int initialValue) {
        Objects.requireNonNull(self);
        int s = initialValue;
        for (int v : self) {
            s += v;
        }
        return s;
    }

    /**
     * Sums the items in an array, adding the result to some initial value.
     * <pre class="groovyTestCase">assert (5+1+2+3+4 as long) == ([1,2,3,4] as long[]).sum(5)</pre>
     *
     * @param self         an array of values to sum
     * @param initialValue the items in the array will be summed to this initial value
     * @return The sum of all the items.
     * @since 2.4.2
     */
    public static long sum(long[] self, long initialValue) {
        Objects.requireNonNull(self);
        long s = initialValue;
        for (long v : self) {
            s += v;
        }
        return s;
    }

    /**
     * Sums the items in an array, adding the result to some initial value.
     * <pre class="groovyTestCase">assert (5+1+2+3+4 as float) == ([1,2,3,4] as float[]).sum(5)</pre>
     *
     * @param self         an array of values to sum
     * @param initialValue the items in the array will be summed to this initial value
     * @return The sum of all the items.
     * @since 2.4.2
     */
    public static float sum(float[] self, float initialValue) {
        Objects.requireNonNull(self);
        float s = initialValue;
        for (float v : self) {
            s += v;
        }
        return s;
    }

    /**
     * Sums the items in an array, adding the result to some initial value.
     * <pre class="groovyTestCase">assert (5+1+2+3+4 as double) == ([1,2,3,4] as double[]).sum(5)</pre>
     *
     * @param self         an array of values to sum
     * @param initialValue the items in the array will be summed to this initial value
     * @return The sum of all the items.
     * @since 2.4.2
     */
    public static double sum(double[] self, double initialValue) {
        Objects.requireNonNull(self);
        double s = initialValue;
        for (double v : self) {
            s += v;
        }
        return s;
    }

    /**
     * Sums the items in an array, adding the result to some initial value.
     *
     * @param self         an array of values to sum
     * @param initialValue the items in the array will be summed to this initial value
     * @return The sum of all the items.
     * @since 1.7.1
     */
    public static Object sum(Object[] self, Object initialValue) {
        return DefaultGroovyMethods.sum(new ArrayIterator<>(self), initialValue, false);
    }

    /**
     * Sums the result of applying a closure to each item of an array.
     * <code>array.sum(closure)</code> is equivalent to:
     * <code>array.collect(closure).sum()</code>.
     *
     * @param self    An array
     * @param closure a single parameter closure that returns a (typically) numeric value.
     * @return The sum of the values returned by applying the closure to each
     *         item of the array.
     * @since 1.7.1
     */
    public static <T> Object sum(T[] self, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        return DefaultGroovyMethods.sum(new ArrayIterator<>(self), null, closure, true);
    }

    /**
     * Sums the result of applying a closure to each item of an array to some initial value.
     * <code>array.sum(initVal, closure)</code> is equivalent to:
     * <code>array.collect(closure).sum(initVal)</code>.
     *
     * @param self         an array
     * @param closure      a single parameter closure that returns a (typically) numeric value.
     * @param initialValue the closure results will be summed to this initial value
     * @return The sum of the values returned by applying the closure to each
     *         item of the array.
     * @since 1.7.1
     */
    public static <T> Object sum(T[] self, Object initialValue, @ClosureParams(FirstParam.Component.class) Closure<?> closure) {
        return DefaultGroovyMethods.sum(new ArrayIterator<>(self), initialValue, closure, false);
    }

    //--------------------------------------------------------------------------
    // swap

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert ([false, true, false, true] as boolean[]) == ([false, false, true, true] as boolean[]).swap(1, 2)
     * </pre>
     *
     * @param self a boolean array
     * @param i    a position
     * @param j    a position
     * @return self
     * @since 2.4.0
     */
    public static boolean[] swap(boolean[] self, int i, int j) {
        Objects.requireNonNull(self);
        boolean tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert ([1, 3, 2, 4] as byte[]) == ([1, 2, 3, 4] as byte[]).swap(1, 2)
     * </pre>
     *
     * @param self a boolean array
     * @param i    a position
     * @param j    a position
     * @return self
     * @since 2.4.0
     */
    public static byte[] swap(byte[] self, int i, int j) {
        Objects.requireNonNull(self);
        byte tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert ([1, 3, 2, 4] as char[]) == ([1, 2, 3, 4] as char[]).swap(1, 2)
     * </pre>
     *
     * @param self a boolean array
     * @param i    a position
     * @param j    a position
     * @return self
     * @since 2.4.0
     */
    public static char[] swap(char[] self, int i, int j) {
        Objects.requireNonNull(self);
        char tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert ([1, 3, 2, 4] as short[]) == ([1, 2, 3, 4] as short[]).swap(1, 2)
     * </pre>
     *
     * @param self a boolean array
     * @param i    a position
     * @param j    a position
     * @return self
     * @since 2.4.0
     */
    public static short[] swap(short[] self, int i, int j) {
        Objects.requireNonNull(self);
        short tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert ([1, 3, 2, 4] as int[]) == ([1, 2, 3, 4] as int[]).swap(1, 2)
     * </pre>
     *
     * @param self a boolean array
     * @param i    a position
     * @param j    a position
     * @return self
     * @since 2.4.0
     */
    public static int[] swap(int[] self, int i, int j) {
        Objects.requireNonNull(self);
        int tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert ([1, 3, 2, 4] as long[]) == ([1, 2, 3, 4] as long[]).swap(1, 2)
     * </pre>
     *
     * @param self a boolean array
     * @param i    a position
     * @param j    a position
     * @return self
     * @since 2.4.0
     */
    public static long[] swap(long[] self, int i, int j) {
        Objects.requireNonNull(self);
        long tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert ([1, 3, 2, 4] as float[]) == ([1, 2, 3, 4] as float[]).swap(1, 2)
     * </pre>
     *
     * @param self a boolean array
     * @param i    a position
     * @param j    a position
     * @return self
     * @since 2.4.0
     */
    public static float[] swap(float[] self, int i, int j) {
        Objects.requireNonNull(self);
        float tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert ([1, 3, 2, 4] as double[]) == ([1, 2, 3, 4] as double[]).swap(1, 2)
     * </pre>
     *
     * @param self a boolean array
     * @param i    a position
     * @param j    a position
     * @return self
     * @since 2.4.0
     */
    public static double[] swap(double[] self, int i, int j) {
        Objects.requireNonNull(self);
        double tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert (["a", "c", "b", "d"] as String[]) == (["a", "b", "c", "d"] as String[]).swap(1, 2)
     * </pre>
     *
     * @param self an array
     * @param i a position
     * @param j a position
     * @return self
     * @since 2.4.0
     */
    public static <T> T[] swap(T[] self, int i, int j) {
        T tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    //--------------------------------------------------------------------------
    // tail

    /**
     * Returns the items from the boolean array excluding the first item.
     * <pre class="groovyTestCase">
     * boolean[] array = [true, false, true]
     * def result = array.tail()
     * assert result == [false, true]
     * assert array.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self an array
     * @return an array without its first element
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    public static boolean[] tail(boolean[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "tail");
        return Arrays.copyOfRange(self, 1, self.length);
    }

    /**
     * Returns the items from the byte array excluding the first item.
     * <pre class="groovyTestCase">
     * byte[] bytes = [1, 2, 3]
     * def result = bytes.tail()
     * assert result == [2, 3]
     * assert bytes.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self an array
     * @return an array without its first element
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    public static byte[] tail(byte[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "tail");
        return Arrays.copyOfRange(self, 1, self.length);
    }

    /**
     * Returns the items from the char array excluding the first item.
     * <pre class="groovyTestCase">
     * char[] chars = ['a', 'b', 'c']
     * def result = chars.tail()
     * assert result == ['b', 'c']
     * assert chars.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self an array
     * @return an array without its first element
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    public static char[] tail(char[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "tail");
        return Arrays.copyOfRange(self, 1, self.length);
    }

    /**
     * Returns the items from the short array excluding the first item.
     * <pre class="groovyTestCase">
     * short[] shorts = [10, 20, 30]
     * def result = shorts.tail()
     * assert result == [20, 30]
     * assert shorts.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self an array
     * @return an array without its first element
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    public static short[] tail(short[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "tail");
        return Arrays.copyOfRange(self, 1, self.length);
    }

    /**
     * Returns the items from the int array excluding the first item.
     * <pre class="groovyTestCase">
     * int[] ints = [1, 3, 5]
     * def result = ints.tail()
     * assert result == [3, 5]
     * assert ints.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self an array
     * @return an array without its first element
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    public static int[] tail(int[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "tail");
        return Arrays.copyOfRange(self, 1, self.length);
    }

    /**
     * Returns the items from the long array excluding the first item.
     * <pre class="groovyTestCase">
     * long[] longs = [2L, 4L, 6L]
     * def result = longs.tail()
     * assert result == [4L, 6L]
     * assert longs.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self an array
     * @return an array without its first element
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    public static long[] tail(long[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "tail");
        return Arrays.copyOfRange(self, 1, self.length);
    }

    /**
     * Returns the items from the float array excluding the first item.
     * <pre class="groovyTestCase">
     * float[] floats = [2.0f, 4.0f, 6.0f]
     * def result = floats.tail()
     * assert result == [4.0f, 6.0f]
     * assert floats.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self an array
     * @return an array without its first element
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    public static float[] tail(float[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "tail");
        return Arrays.copyOfRange(self, 1, self.length);
    }

    /**
     * Returns the items from the double array excluding the first item.
     * <pre class="groovyTestCase">
     * double[] doubles = [10.0d, 20.0d, 30.0d]
     * def result = doubles.tail()
     * assert result == [20.0d, 30.0d]
     * assert doubles.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self an array
     * @return an array without its first element
     * @throws UnsupportedOperationException if the array is empty
     * @since 5.0.0
     */
    public static double[] tail(double[] self) {
        Objects.requireNonNull(self);
        throwUnsupportedOperationIfEmpty(self.length, "tail");
        return Arrays.copyOfRange(self, 1, self.length);
    }

    //--------------------------------------------------------------------------
    // take

    //--------------------------------------------------------------------------
    // takeRight

    //--------------------------------------------------------------------------
    // takeWhile

    //--------------------------------------------------------------------------
    // toArrayString

    //--------------------------------------------------------------------------
    // toList

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param self a boolean array
     * @return A list containing the contents of this array.
     * @since 1.6.0
     */
    @SuppressWarnings("unchecked")
    public static List<Boolean> toList(boolean[] self) {
        return DefaultTypeTransformation.primitiveArrayToList(self);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param self a byte array
     * @return A list containing the contents of this array.
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Byte> toList(byte[] self) {
        return DefaultTypeTransformation.primitiveArrayToList(self);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param self a char array
     * @return A list containing the contents of this array.
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Character> toList(char[] self) {
        return DefaultTypeTransformation.primitiveArrayToList(self);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param self a short array
     * @return A list containing the contents of this array.
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Short> toList(short[] self) {
        return DefaultTypeTransformation.primitiveArrayToList(self);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param self an int array
     * @return A list containing the contents of this array.
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Integer> toList(int[] self) {
        return DefaultTypeTransformation.primitiveArrayToList(self);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param self a long array
     * @return A list containing the contents of this array.
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Long> toList(long[] self) {
        return DefaultTypeTransformation.primitiveArrayToList(self);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param self a float array
     * @return A list containing the contents of this array.
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Float> toList(float[] self) {
        return DefaultTypeTransformation.primitiveArrayToList(self);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param self a double array
     * @return A list containing the contents of this array.
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Double> toList(double[] self) {
        return DefaultTypeTransformation.primitiveArrayToList(self);
    }

    /**
     * Allows conversion of arrays into a mutable List.
     *
     * @param self an object array
     * @return A list containing the contents of this array.
     * @since 1.0
     */
    public static <T> List<T> toList(T[] self) {
        return new ArrayList<>(Arrays.asList(self));
    }

    //--------------------------------------------------------------------------
    // toSet

    /**
     * Converts this array to a Set, with each unique element added to the set.
     * <pre class="groovyTestCase">
     * boolean[] array = [true, false, true]
     * Set expected = [true, false]
     * assert array.toSet() == expected
     * </pre>
     *
     * @param self a boolean array
     * @return A set containing the unique contents of this array.
     * @since 1.8.0
     */
    @SuppressWarnings("unchecked")
    public static Set<Boolean> toSet(boolean[] self) {
        return DefaultGroovyMethods.toSet(DefaultTypeTransformation.primitiveArrayToUnmodifiableList(self));
    }

    /**
     * Converts this array to a Set, with each unique element added to the set.
     * <pre class="groovyTestCase">
     * byte[] array = [1, 2, 3, 2, 1]
     * Set expected = [1, 2, 3]
     * assert array.toSet() == expected
     * </pre>
     *
     * @param self a byte array
     * @return A set containing the unique contents of this array.
     * @since 1.8.0
     */
    @SuppressWarnings("unchecked")
    public static Set<Byte> toSet(byte[] self) {
        return DefaultGroovyMethods.toSet(DefaultTypeTransformation.primitiveArrayToUnmodifiableList(self));
    }

    /**
     * Converts this array to a Set, with each unique element added to the set.
     * <pre class="groovyTestCase">
     * char[] array = 'xyzzy'.chars
     * Set expected = ['x', 'y', 'z']
     * assert array.toSet() == expected
     * </pre>
     *
     * @param self a char array
     * @return A set containing the unique contents of this array.
     * @since 1.8.0
     */
    @SuppressWarnings("unchecked")
    public static Set<Character> toSet(char[] self) {
        return DefaultGroovyMethods.toSet(DefaultTypeTransformation.primitiveArrayToUnmodifiableList(self));
    }

    /**
     * Converts this array to a Set, with each unique element added to the set.
     * <pre class="groovyTestCase">
     * short[] array = [1, 2, 3, 2, 1]
     * Set expected = [1, 2, 3]
     * assert array.toSet() == expected
     * </pre>
     *
     * @param self a short array
     * @return A set containing the unique contents of this array.
     * @since 1.8.0
     */
    @SuppressWarnings("unchecked")
    public static Set<Short> toSet(short[] self) {
        return DefaultGroovyMethods.toSet(DefaultTypeTransformation.primitiveArrayToUnmodifiableList(self));
    }

    /**
     * Converts this array to a Set, with each unique element added to the set.
     * <pre class="groovyTestCase">
     * int[] array = [1, 2, 3, 2, 1]
     * Set expected = [1, 2, 3]
     * assert array.toSet() == expected
     * </pre>
     *
     * @param self an int array
     * @return A set containing the unique contents of this array.
     * @since 1.8.0
     */
    @SuppressWarnings("unchecked")
    public static Set<Integer> toSet(int[] self) {
        return DefaultGroovyMethods.toSet(DefaultTypeTransformation.primitiveArrayToUnmodifiableList(self));
    }

    /**
     * Converts this array to a Set, with each unique element added to the set.
     * <pre class="groovyTestCase">
     * long[] array = [1, 2, 3, 2, 1]
     * Set expected = [1, 2, 3]
     * assert array.toSet() == expected
     * </pre>
     *
     * @param self a long array
     * @return A set containing the unique contents of this array.
     * @since 1.8.0
     */
    @SuppressWarnings("unchecked")
    public static Set<Long> toSet(long[] self) {
        return DefaultGroovyMethods.toSet(DefaultTypeTransformation.primitiveArrayToUnmodifiableList(self));
    }

    /**
     * Converts this array to a Set, with each unique element added to the set.
     * <pre class="groovyTestCase">
     * float[] array = [1.0f, 2.0f, 3.0f, 2.0f, 1.0f]
     * Set expected = [1.0f, 2.0f, 3.0f]
     * assert array.toSet() == expected
     * </pre>
     *
     * @param self a float array
     * @return A set containing the unique contents of this array.
     * @since 1.8.0
     */
    @SuppressWarnings("unchecked")
    public static Set<Float> toSet(float[] self) {
        return DefaultGroovyMethods.toSet(DefaultTypeTransformation.primitiveArrayToUnmodifiableList(self));
    }

    /**
     * Converts this array to a Set, with each unique element added to the set.
     * <pre class="groovyTestCase">
     * double[] array = [1.0d, 2.0d, 3.0d, 2.0d, 1.0d]
     * Set expected = [1.0d, 2.0d, 3.0d]
     * assert array.toSet() == expected
     * </pre>
     *
     * @param self a double array
     * @return A set containing the unique contents of this array.
     * @since 1.8.0
     */
    @SuppressWarnings("unchecked")
    public static Set<Double> toSet(double[] self) {
        return DefaultGroovyMethods.toSet(DefaultTypeTransformation.primitiveArrayToUnmodifiableList(self));
    }

    /**
     * Converts this array to a Set, with each unique element added to the set.
     *
     * @param self an object array
     * @return A set containing the unique contents of this array.
     * @since 5.0.0
     */
    public static <T> Set<T> toSet(T[] self) {
        return DefaultGroovyMethods.toSet(Arrays.asList(self));
    }

    //--------------------------------------------------------------------------
    // toSorted

    //--------------------------------------------------------------------------
    // toString

    /**
     * Returns the string representation of the given array.
     * <pre class="groovyTestCase">
     * boolean[] array = [false, true, false]
     * assert array.toString() == '[false, true, false]'
     * </pre>
     *
     * @param self an array
     * @return the string representation
     * @since 1.6.0
     */
    public static String toString(boolean[] self) {
        return FormatHelper.toString(self);
    }

    /**
     * Returns the string representation of the given array.
     * <pre class="groovyTestCase">
     * byte[] array = [1, 2, 3, 2, 1]
     * assert array.toString() == '[1, 2, 3, 2, 1]'
     * </pre>
     *
     * @param self an array
     * @return the string representation
     * @since 1.6.0
     */
    public static String toString(byte[] self) {
        return FormatHelper.toString(self);
    }

    /**
     * Returns the string representation of the given array.
     * <pre class="groovyTestCase">
     * char[] array = 'abcd'.chars
     * assert array instanceof char[]
     * assert array.toString() == 'abcd'
     * </pre>
     *
     * @param self an array
     * @return the string representation
     * @since 1.6.0
     */
    public static String toString(char[] self) {
        return FormatHelper.toString(self);
    }

    /**
     * Returns the string representation of the given array.
     * <pre class="groovyTestCase">
     * short[] array = [1, 2, 3, 2, 1]
     * assert array.toString() == '[1, 2, 3, 2, 1]'
     * </pre>
     *
     * @param self an array
     * @return the string representation
     * @since 1.6.0
     */
    public static String toString(short[] self) {
        return FormatHelper.toString(self);
    }

    /**
     * Returns the string representation of the given array.
     * <pre class="groovyTestCase">
     * int[] array = [1, 2, 3, 2, 1]
     * assert array.toString() == '[1, 2, 3, 2, 1]'
     * </pre>
     *
     * @param self an array
     * @return the string representation
     * @since 1.6.0
     */
    public static String toString(int[] self) {
        return FormatHelper.toString(self);
    }

    /**
     * Returns the string representation of the given array.
     * <pre class="groovyTestCase">
     * long[] array = [1, 2, 3, 2, 1]
     * assert array.toString() == '[1, 2, 3, 2, 1]'
     * </pre>
     *
     * @param self an array
     * @return the string representation
     * @since 1.6.0
     */
    public static String toString(long[] self) {
        return FormatHelper.toString(self);
    }

    /**
     * Returns the string representation of the given array.
     * <pre class="groovyTestCase">
     * float[] array = [1, 2, 3, 2, 1]
     * assert array.toString() == '[1.0, 2.0, 3.0, 2.0, 1.0]'
     * </pre>
     *
     * @param self an array
     * @return the string representation
     * @since 1.6.0
     */
    public static String toString(float[] self) {
        return FormatHelper.toString(self);
    }

    /**
     * Returns the string representation of the given array.
     * <pre class="groovyTestCase">
     * double[] array = [1, 2, 3, 2, 1]
     * assert array.toString() == '[1.0, 2.0, 3.0, 2.0, 1.0]'
     * </pre>
     *
     * @param self an array
     * @return the string representation
     * @since 1.6.0
     */
    public static String toString(double[] self) {
        return FormatHelper.toString(self);
    }


    //--------------------------------------------------------------------------
    // toUnique

    /**
     * Returns a new Array containing the items from the original Array but with duplicates removed using the
     * natural ordering of the items in the array.
     * <p>
     * <pre class="groovyTestCase">
     * String[] letters = ['c', 'a', 't', 's', 'a', 't', 'h', 'a', 't']
     * String[] expected = ['c', 'a', 't', 's', 'h']
     * def result = letters.toUnique()
     * assert result == expected
     * assert result.class.componentType == String
     * </pre>
     *
     * @param self an array
     * @return the unique items from the array
     */
    public static <T> T[] toUnique(T[] self) {
        return toUnique(self, (Comparator<T>) null);
    }

    /**
     * Returns a new Array containing the items from the original Array but with duplicates removed with the supplied
     * comparator determining which items are unique.
     * <p>
     * <pre class="groovyTestCase">
     * String[] letters = ['c', 'a', 't', 's', 'A', 't', 'h', 'a', 'T']
     * String[] lower = ['c', 'a', 't', 's', 'h']
     * class LowerComparator implements Comparator {
     *     int compare(a, b) { a.toLowerCase() {@code <=>} b.toLowerCase() }
     * }
     * assert letters.toUnique(new LowerComparator()) == lower
     * </pre>
     *
     * @param self an array
     * @param comparator a Comparator used to determine unique (equal) items
     *        If {@code null}, the Comparable natural ordering of the elements will be used.
     * @return the unique items from the array
     */
    public static <T> T[] toUnique(T[] self, Comparator<? super T> comparator) {
        Collection<T> items = DefaultGroovyMethods.toUnique(new ArrayIterable<>(self), comparator);
        return items.toArray(createSimilarArray(self, items.size()));
    }

    /**
     * Returns a new Array containing the items from the original Array but with duplicates removed with the supplied
     * closure determining which items are unique.
     * <p>
     * <pre class="groovyTestCase">
     * String[] letters = ['c', 'a', 't', 's', 'A', 't', 'h', 'a', 'T']
     * String[] expected = ['c', 'a', 't', 's', 'h']
     * assert letters.toUnique{ p1, p2 {@code ->} p1.toLowerCase() {@code <=>} p2.toLowerCase() } == expected
     * assert letters.toUnique{ it.toLowerCase() } == expected
     * </pre>
     *
     * @param self an array
     * @param closure a Closure used to determine unique items
     * @return the unique items from the array
     */
    public static <T> T[] toUnique(T[] self, @ClosureParams(value=FromString.class,options={"T","T,T"}) Closure<?> closure) {
        Comparator<T> comparator = closure.getMaximumNumberOfParameters() == 1
                ? new OrderBy<>(closure, true)
                : new ClosureComparator<>(closure);
        return toUnique(self, comparator);
    }

    //--------------------------------------------------------------------------
    // transpose

    /**
     * A transpose method for 2D boolean arrays.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * boolean[][] array = [[false, false], [true, true]]
     * boolean[][] expected = [[false, true], [false, true]]
     * def result = array.transpose()
     * assert result == expected
     * assert array.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self a 2D boolean array
     * @return the transposed 2D boolean array
     * @since 5.0.0
     */
    public static boolean[][] transpose(boolean[][] self) {
        Objects.requireNonNull(self);
        boolean[][] result = new boolean[self[0].length][self.length];
        for (int i = 0; i < self.length; i++) {
            for (int j = 0; j < self[i].length; j++) {
                result[j][i] = self[i][j];
            }
        }
        return result;
    }

    /**
     * A transpose method for 2D byte arrays.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * byte[][] bytes = [[1, 10], [2, 20]]
     * byte[][] expected = [[1, 2], [10, 20]]
     * def result = bytes.transpose()
     * assert result == expected
     * assert bytes.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self a 2D byte array
     * @return the transposed 2D byte array
     * @since 5.0.0
     */
    public static byte[][] transpose(byte[][] self) {
        Objects.requireNonNull(self);
        byte[][] result = new byte[self[0].length][self.length];
        for (int i = 0; i < self.length; i++) {
            for (int j = 0; j < self[i].length; j++) {
                result[j][i] = self[i][j];
            }
        }
        return result;
    }

    /**
     * A transpose method for 2D char arrays.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * char[][] chars = [['a', 'b'], ['A', 'B']]
     * char[][] expected = [['a', 'A'], ['b', 'B']]
     * def result = chars.transpose()
     * assert result == expected
     * assert chars.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self a 2D char array
     * @return the transposed 2D char array
     * @since 5.0.0
     */
    public static char[][] transpose(char[][] self) {
        Objects.requireNonNull(self);
        char[][] result = new char[self[0].length][self.length];
        for (int i = 0; i < self.length; i++) {
            for (int j = 0; j < self[i].length; j++) {
                result[j][i] = self[i][j];
            }
        }
        return result;
    }

    /**
     * A transpose method for 2D short arrays.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * short[][] shorts = [[1, 10], [100, 1000]]
     * short[][] expected = [[1, 100], [10, 1000]]
     * def result = shorts.transpose()
     * assert result == expected
     * assert shorts.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self a 2D short array
     * @return the transposed 2D short array
     * @since 5.0.0
     */
    public static short[][] transpose(short[][] self) {
        Objects.requireNonNull(self);
        short[][] result = new short[self[0].length][self.length];
        for (int i = 0; i < self.length; i++) {
            for (int j = 0; j < self[i].length; j++) {
                result[j][i] = self[i][j];
            }
        }
        return result;
    }

    /**
     * A transpose method for 2D int arrays.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * int[][] nums = [[10, 15, 20], [30, 35, 40]]
     * int[][] expected = [[10, 30], [15, 35], [20, 40]]
     * assert nums.transpose() == expected
     * </pre>
     *
     * @param self a 2D int array
     * @return the transposed 2D int array
     * @since 3.0.8
     */
    public static int[][] transpose(int[][] self) {
        Objects.requireNonNull(self);
        int[][] result = new int[self[0].length][self.length];
        for (int i = 0; i < self.length; i++) {
            for (int j = 0; j < self[i].length; j++) {
                result[j][i] = self[i][j];
            }
        }
        return result;
    }

    /**
     * A transpose method for 2D long arrays.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * long[][] longs = [[1L, 3L, 5L], [2L, 4L, 6L]]
     * long[][] expected = [[1L, 2L], [3L, 4L], [5L, 6L]]
     * def result = longs.transpose()
     * assert result == expected
     * assert longs.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self a 2D long array
     * @return the transposed 2D long array
     * @since 3.0.8
     */
    public static long[][] transpose(long[][] self) {
        Objects.requireNonNull(self);
        long[][] result = new long[self[0].length][self.length];
        for (int i = 0; i < self.length; i++) {
            for (int j = 0; j < self[i].length; j++) {
                result[j][i] = self[i][j];
            }
        }
        return result;
    }

    /**
     * A transpose method for 2D float arrays.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * float[][] floats = [[1.0f, 10.0f], [2.0f, 20.0f]]
     * float[][] expected = [[1.0f, 2.0f], [10.0f, 20.0f]]
     * def result = floats.transpose()
     * assert result == expected
     * assert floats.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self a 2D float array
     * @return the transposed 2D float array
     * @since 5.0.0
     */
    public static float[][] transpose(float[][] self) {
        Objects.requireNonNull(self);
        float[][] result = new float[self[0].length][self.length];
        for (int i = 0; i < self.length; i++) {
            for (int j = 0; j < self[i].length; j++) {
                result[j][i] = self[i][j];
            }
        }
        return result;
    }

    /**
     * A transpose method for 2D double arrays.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * double[][] doubles = [[1.0d, 10.0d], [2.0d, 20.0d]]
     * double[][] expected = [[1.0d, 2.0d], [10.0d, 20.0d]]
     * def result = doubles.transpose()
     * assert result == expected
     * assert doubles.class.componentType == result.class.componentType
     * </pre>
     *
     * @param self a 2D double array
     * @return the transposed 2D double array
     * @since 3.0.8
     */
    public static double[][] transpose(double[][] self) {
        Objects.requireNonNull(self);
        double[][] result = new double[self[0].length][self.length];
        for (int i = 0; i < self.length; i++) {
            for (int j = 0; j < self[i].length; j++) {
                result[j][i] = self[i][j];
            }
        }
        return result;
    }

    //--------------------------------------------------------------------------
    // union

    //--------------------------------------------------------------------------

    private static void throwNoSuchElementIfEmpty(final int size, final String method) {
        if (size == 0) {
            throw new NoSuchElementException("Cannot access " + method + "() for an empty array");
        }
    }

    private static void throwUnsupportedOperationIfEmpty(final int size, final String method) {
        if (size == 0) {
            throw new UnsupportedOperationException("Accessing " + method + "() is unsupported for an empty array");
        }
    }

    /**
     * Implements the getAt(int) method for primitive type arrays.
     *
     * @param self an array object
     * @param index  the index of interest
     * @return the returned value from the array
     * @since 1.5.0
     */
    private static Object primitiveArrayGet(final Object self, final int index) {
        return Array.get(self, normaliseIndex(index, Array.getLength(self)));
    }

    /**
     * Implements the getAt(Range) method for primitive type arrays.
     *
     * @param self  an array object
     * @param range the range of indices of interest
     * @return the returned values from the array corresponding to the range
     * @since 1.5.0
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List primitiveArrayGet(final Object self, final Range range) {
        List answer = new ArrayList<>();
        for (Object next : range) {
            int idx = DefaultTypeTransformation.intUnbox(next);
            answer.add(primitiveArrayGet(self, idx));
        }
        return answer;
    }

    /**
     * Implements the getAt(Collection) method for primitive type arrays.  Each
     * value in the collection argument is assumed to be a valid array index.
     * The value at each index is then added to a list which is returned.
     *
     * @param self    an array object
     * @param indices the indices of interest
     * @return the returned values from the array
     * @since 1.0
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List primitiveArrayGet(final Object self, final Collection indices) {
        List answer = new ArrayList<>();
        for (Object value : indices) {
            if (value instanceof Range) {
                answer.addAll(primitiveArrayGet(self, (Range) value));
            } else if (value instanceof List) {
                answer.addAll(primitiveArrayGet(self, (List) value));
            } else {
                int idx = DefaultTypeTransformation.intUnbox(value);
                answer.add(primitiveArrayGet(self, idx));
            }
        }
        return answer;
    }
}
