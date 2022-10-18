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
import groovy.lang.IntRange;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FirstParam;
import groovy.transform.stc.FromString;
import org.codehaus.groovy.runtime.callsite.BooleanClosureWrapper;
import org.codehaus.groovy.util.BooleanArrayIterator;
import org.codehaus.groovy.util.ByteArrayIterator;
import org.codehaus.groovy.util.CharArrayIterator;
import org.codehaus.groovy.util.DoubleArrayIterator;
import org.codehaus.groovy.util.FloatArrayIterator;
import org.codehaus.groovy.util.IntArrayIterator;
import org.codehaus.groovy.util.LongArrayIterator;
import org.codehaus.groovy.util.ShortArrayIterator;

import java.math.BigDecimal;
import java.util.List;

/**
 * This class defines new groovy methods which appear on primitive arrays inside the Groovy environment.
 * Static methods are used with the
 * first parameter being the destination class,
 * i.e. <code>public static int[] each(int[] self, Closure closure)</code>
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
public class ArrayGroovyMethods {
    /* Arrangement of each method (skip any unapplicable types for the methods):
     * 1. boolean[]
     * 2. byte[]
     * 3. char[]
     * 4. short[]
     * 5. int[]
     * 6. long[]
     * 7. float[]
     * 8. double[]
     */

    private ArrayGroovyMethods() {
    }

    //-------------------------------------------------------------------------
    // any

    /**
     * Iterates over the contents of a boolean Array, and checks whether a
     * predicate is valid for at least one element.
     * <pre class="groovyTestCase">
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      boolean[] array = [false, true, false]
     *      assert array.any{ true == it.booleanValue() }
     * }
     * test()
     * </pre>
     *
     * @param self      the boolean array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the booleans matches the closure predicate
     * @since 5.0.0
     */
    public static boolean any(boolean[] self, @ClosureParams(FirstParam.Component.class) Closure predicate) {
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      byte[] array = [0, 1, 2]
     *      assert array.any{ 0 == it.byteValue() }
     * }
     * test()
     * </pre>
     *
     * @param self      the byte array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the bytes matches the closure predicate
     * @since 5.0.0
     */
    public static boolean any(byte[] self, @ClosureParams(FirstParam.Component.class) Closure predicate) {
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      char[] array = ['a' as char, 'b' as char, 'c' as char]
     *      assert array.any{ 'a' as char == it.charValue() }
     * }
     * test()
     * </pre>
     *
     * @param self      the char array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the chars matches the closure predicate
     * @since 5.0.0
     */
    public static boolean any(char[] self, @ClosureParams(FirstParam.Component.class) Closure predicate) {
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      short[] array = [0, 1, 2]
     *      assert array.any{ 0 == it.shortValue() }
     * }
     * test()
     * </pre>
     *
     * @param self      the char array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the shorts matches the closure predicate
     * @since 5.0.0
     */
    public static boolean any(short[] self, @ClosureParams(FirstParam.Component.class) Closure predicate) {
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      int[] array = [0, 1, 2]
     *      assert array.any{ 0 == it.intValue() }
     * }
     * test()
     * </pre>
     *
     * @param self      the int array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the ints matches the closure predicate
     * @since 5.0.0
     */
    public static boolean any(int[] self, @ClosureParams(FirstParam.Component.class) Closure predicate) {
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      long[] array = [0, 1, 2]
     *      assert array.any{ 0 == it.longValue() }
     * }
     * test()
     * </pre>
     *
     * @param self      the long array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the longs matches the closure predicate
     * @since 5.0.0
     */
    public static boolean any(long[] self, @ClosureParams(FirstParam.Component.class) Closure predicate) {
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      float[] array = [0, 1, 2]
     *      assert array.any{ 0 == it.floatValue() }
     * }
     * test()
     * </pre>
     *
     * @param self      the float array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the floats matches the closure predicate
     * @since 5.0.0
     */
    public static boolean any(float[] self, @ClosureParams(FirstParam.Component.class) Closure predicate) {
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      double[] array = [0, 1, 2]
     *      assert array.any{ 0 == it.floatValue() }
     * }
     * test()
     * </pre>
     *
     * @param self      the double array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the doubles matches the closure predicate
     * @since 5.0.0
     */
    public static boolean any(double[] self, @ClosureParams(FirstParam.Component.class) Closure predicate) {
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        for (double item : self) {
            if (bcw.call(item)) return true;
        }
        return false;
    }

    //-------------------------------------------------------------------------
    // asBoolean (brought over from DefaultGroovyMethods)

    /**
     * Coerces a boolean array to a boolean value.
     * A boolean array is false if the array is of length 0,
     * and true otherwise.
     *
     * @param array an array
     * @return the array's boolean value
     * @since 1.7.4
     */
    public static boolean asBoolean(boolean[] array) {
        return array != null && array.length > 0;
    }

    /**
     * Coerces a byte array to a boolean value.
     * A byte array is false if the array is of length 0,
     * and true otherwise.
     *
     * @param array an array
     * @return the array's boolean value
     * @since 1.7.4
     */
    public static boolean asBoolean(byte[] array) {
        return array != null && array.length > 0;
    }

    /**
     * Coerces a char array to a boolean value.
     * A char array is false if the array is of length 0,
     * and true otherwise.
     *
     * @param array an array
     * @return the array's boolean value
     * @since 1.7.4
     */
    public static boolean asBoolean(char[] array) {
        return array != null && array.length > 0;
    }

    /**
     * Coerces a short array to a boolean value.
     * A short array is false if the array is of length 0,
     * and true otherwise.
     *
     * @param array an array
     * @return the array's boolean value
     * @since 1.7.4
     */
    public static boolean asBoolean(short[] array) {
        return array != null && array.length > 0;
    }

    /**
     * Coerces an int array to a boolean value.
     * An int array is false if the array is of length 0,
     * and true otherwise.
     *
     * @param array an array
     * @return the array's boolean value
     * @since 1.7.4
     */
    public static boolean asBoolean(int[] array) {
        return array != null && array.length > 0;
    }

    /**
     * Coerces a long array to a boolean value.
     * A long array is false if the array is of length 0,
     * and true otherwise.
     *
     * @param array an array
     * @return the array's boolean value
     * @since 1.7.4
     */
    public static boolean asBoolean(long[] array) {
        return array != null && array.length > 0;
    }

    /**
     * Coerces a float array to a boolean value.
     * A float array is false if the array is of length 0,
     * and true otherwise.
     *
     * @param array an array
     * @return the array's boolean value
     * @since 1.7.4
     */
    public static boolean asBoolean(float[] array) {
        return array != null && array.length > 0;
    }

    /**
     * Coerces a double array to a boolean value.
     * A double array is false if the array is of length 0,
     * and true otherwise.
     *
     * @param array an array
     * @return the array's boolean value
     * @since 1.7.4
     */
    public static boolean asBoolean(double[] array) {
        return array != null && array.length > 0;
    }

    //-------------------------------------------------------------------------
    // asType (skipped, as it is not needed)
    //-------------------------------------------------------------------------
    // average (brought over from DefaultGroovyMethods)

    /**
     * Calculates the average of the bytes in the array.
     * <pre class="groovyTestCase">assert 5.0G == ([2,4,6,8] as byte[]).average()</pre>
     *
     * @param self The array of values to calculate the average of
     * @return The average of the items
     * @since 3.0.0
     */
    public static BigDecimal average(byte[] self) {
        long s = 0;
        int count = 0;
        for (byte v : self) {
            s += v;
            count++;
        }
        return BigDecimal.valueOf(s).divide(BigDecimal.valueOf(count));
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
        long s = 0;
        int count = 0;
        for (short v : self) {
            s += v;
            count++;
        }
        return BigDecimal.valueOf(s).divide(BigDecimal.valueOf(count));
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
        long s = 0;
        int count = 0;
        for (int v : self) {
            s += v;
            count++;
        }
        return BigDecimal.valueOf(s).divide(BigDecimal.valueOf(count));
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
        long s = 0;
        int count = 0;
        for (long v : self) {
            s += v;
            count++;
        }
        return BigDecimal.valueOf(s).divide(BigDecimal.valueOf(count));
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
        double s = 0.0d;
        int count = 0;
        for (float v : self) {
            s += v;
            count++;
        }
        return s / count;
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
        double s = 0.0d;
        int count = 0;
        for (double v : self) {
            s += v;
            count++;
        }
        return s / count;
    }

    //-------------------------------------------------------------------------
    // chop

    /**
     * Chops the boolean array into pieces, returning lists with sizes corresponding to the supplied chop sizes.
     * If the array isn't large enough, truncated (possibly empty) pieces are returned.
     * Using a chop size of -1 will cause that piece to contain all remaining items from the array.
     * <pre class="groovyTestCase">
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      boolean[] array = [false, true, false]
     *      assert array.chop(1, 2) == [[false], [true, false]]
     * }
     * test()
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      byte[] array = [0, 1, 2]
     *      assert array.chop(1, 2) == [[0], [1, 2]]
     * }
     * test()
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      char[] array = [0, 1, 2]
     *      assert array.chop(1, 2) == [[0], [1, 2]]
     * }
     * test()
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      short[] array = [0, 1, 2]
     *      assert array.chop(1, 2) == [[0], [1, 2]]
     * }
     * test()
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      int[] array = [0, 1, 2]
     *      assert array.chop(1, 2) == [[0], [1, 2]]
     * }
     * test()
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      long[] array = [0, 1, 2]
     *      assert array.chop(1, 2) == [[0], [1, 2]]
     * }
     * test()
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      float[] array = [0, 1, 2]
     *      assert array.chop(1, 2) == [[0], [1, 2]]
     * }
     * test()
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      double[] array = [0, 1, 2]
     *      assert array.chop(1, 2) == [[0], [1, 2]]
     * }
     * test()
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

    //-------------------------------------------------------------------------
    // collate
    //-------------------------------------------------------------------------
    // collect
    //-------------------------------------------------------------------------
    // collectEntries
    //-------------------------------------------------------------------------
    // collectMany
    //-------------------------------------------------------------------------
    // contains
    //-------------------------------------------------------------------------
    // count

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
    public static Number count(boolean[] self, Object value) {
        return DefaultGroovyMethods.count(InvokerHelper.asIterator(self), value);
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
    public static Number count(byte[] self, Object value) {
        return DefaultGroovyMethods.count(InvokerHelper.asIterator(self), value);
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
    public static Number count(char[] self, Object value) {
        return DefaultGroovyMethods.count(InvokerHelper.asIterator(self), value);
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
    public static Number count(short[] self, Object value) {
        return DefaultGroovyMethods.count(InvokerHelper.asIterator(self), value);
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
    public static Number count(int[] self, Object value) {
        return DefaultGroovyMethods.count(InvokerHelper.asIterator(self), value);
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
    public static Number count(long[] self, Object value) {
        return DefaultGroovyMethods.count(InvokerHelper.asIterator(self), value);
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
    public static Number count(float[] self, Object value) {
        return DefaultGroovyMethods.count(InvokerHelper.asIterator(self), value);
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
    public static Number count(double[] self, Object value) {
        return DefaultGroovyMethods.count(InvokerHelper.asIterator(self), value);
    }

    //-------------------------------------------------------------------------
    // countBy
    //-------------------------------------------------------------------------
    // drop
    //-------------------------------------------------------------------------
    // dropRight
    //-------------------------------------------------------------------------
    // dropWhile
    //-------------------------------------------------------------------------
    // each

    /**
     * Iterates through a boolean[] passing each boolean to the given closure.
     * <pre class="groovyTestCase">
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      boolean[] array = [false, true, false]
     *      String result = ''
     *      array.each{ result += it.booleanValue() }
     *      assert result == 'falsetruefalse'
     * }
     * test()
     * </pre>
     *
     * @param self    the boolean array over which we iterate
     * @param closure the closure applied on each boolean
     * @return the self array
     * @since 5.0.0
     */
    public static boolean[] each(boolean[] self, @ClosureParams(FirstParam.Component.class) Closure closure) {
        for (boolean item : self) {
            closure.call(item);
        }
        return self;
    }

    /**
     * Iterates through a byte[] passing each byte to the given closure.
     * <pre class="groovyTestCase">
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      byte[] array = [0, 1, 2]
     *      String result = ''
     *      array.each{ result += it.intValue() }
     *      assert result == '012'
     * }
     * test()
     * </pre>
     *
     * @param self    the byte array over which we iterate
     * @param closure the closure applied on each byte
     * @return the self array
     * @since 5.0.0
     */
    public static byte[] each(byte[] self, @ClosureParams(FirstParam.Component.class) Closure closure) {
        for (byte item : self) {
            closure.call(item);
        }
        return self;
    }

    /**
     * Iterates through a char[] passing each char to the given closure.
     * <pre class="groovyTestCase">
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      char[] array = ['a' as char, 'b' as char, 'c' as char]
     *      String result = ''
     *      array.each{ result += it.charValue() }
     *      assert result == 'abc'
     * }
     * test()
     * </pre>
     *
     * @param self    the char array over which we iterate
     * @param closure the closure applied on each char
     * @return the self array
     * @since 5.0.0
     */
    public static char[] each(char[] self, @ClosureParams(FirstParam.Component.class) Closure closure) {
        for (char item : self) {
            closure.call(item);
        }
        return self;
    }

    /**
     * Iterates through a short[] passing each short to the given closure.
     * <pre class="groovyTestCase">
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      short[] array = [0, 1, 2]
     *      String result = ''
     *      array.each{ result += it.shortValue() }
     *      assert result == '012'
     * }
     * test()
     * </pre>
     *
     * @param self    the short array over which we iterate
     * @param closure the closure applied on each short
     * @return the self array
     * @since 5.0.0
     */
    public static short[] each(short[] self, @ClosureParams(FirstParam.Component.class) Closure closure) {
        for (short item : self) {
            closure.call(item);
        }
        return self;
    }

    /**
     * Iterates through an int[] passing each int to the given closure.
     * <pre class="groovyTestCase">
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      int[] array = [0, 1, 2]
     *      String result = ''
     *      array.each{ result += it.intValue() }
     *      assert result == '012'
     * }
     * test()
     * </pre>
     *
     * @param self    the int array over which we iterate
     * @param closure the closure applied on each int
     * @return the self array
     * @since 5.0.0
     */
    public static int[] each(int[] self, @ClosureParams(FirstParam.Component.class) Closure closure) {
        for (int item : self) {
            closure.call(item);
        }
        return self;
    }

    /**
     * Iterates through a long[] passing each long to the given closure.
     * <pre class="groovyTestCase">
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      long[] array = [0, 1, 2]
     *      String result = ''
     *      array.each{ result += it.longValue() }
     *      assert result == '012'
     * }
     * test()
     * </pre>
     *
     * @param self    the long array over which we iterate
     * @param closure the closure applied on each long
     * @return the self array
     * @since 5.0.0
     */
    public static long[] each(long[] self, @ClosureParams(FirstParam.Component.class) Closure closure) {
        for (long item : self) {
            closure.call(item);
        }
        return self;
    }

    /**
     * Iterates through a float[] passing each float to the given closure.
     * <pre class="groovyTestCase">
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      float[] array = [0, 1, 2]
     *      String result = ''
     *      array.each{ result += it.floatValue() }
     *      assert result == '0.01.02.0'
     * }
     * test()
     * </pre>
     *
     * @param self    the float array over which we iterate
     * @param closure the closure applied on each float
     * @return the self array
     * @since 5.0.0
     */
    public static float[] each(float[] self, @ClosureParams(FirstParam.Component.class) Closure closure) {
        for (float item : self) {
            closure.call(item);
        }
        return self;
    }

    /**
     * Iterates through a double[] passing each double to the given closure.
     * <pre class="groovyTestCase">
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      double[] array = [0, 1, 2]
     *      String result = ''
     *      array.each{ result += it.doubleValue() }
     *      assert result == '0.01.02.0'
     * }
     * test()
     * </pre>
     *
     * @param self    the double array over which we iterate
     * @param closure the closure applied on each double
     * @return the self array
     * @since 5.0.0
     */
    public static double[] each(double[] self, @ClosureParams(FirstParam.Component.class) Closure closure) {
        for (double item : self) {
            closure.call(item);
        }
        return self;
    }

    //-------------------------------------------------------------------------
    // eachByte

    /**
     * Traverse through each byte of this byte array. Alias for each.
     *
     * @param self    a byte array
     * @param closure a closure
     * @see #each(byte[], groovy.lang.Closure)
     * @since 1.5.5
     */
    public static void eachByte(byte[] self, @ClosureParams(FirstParam.Component.class) Closure closure) {
        each(self, closure);
    }

    //-------------------------------------------------------------------------
    // eachWithIndex

    /**
     * Iterates through an boolean[],
     * passing each boolean and the element's index (a counter starting at
     * zero) to the given closure.
     * <pre class="groovyTestCase">
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      boolean[] array = [false, true, false]
     *      String result = ''
     *      array.eachWithIndex{ item, index {@code ->} result += "$index:${item.booleanValue()}" }
     *      assert result == '0:false1:true2:false'
     * }
     * test()
     * </pre>
     *
     * @param self    an boolean array
     * @param closure a Closure to operate on each boolean
     * @return the self array
     * @since 5.0.0
     */
    public static boolean[] eachWithIndex(boolean[] self, @ClosureParams(value = FromString.class, options = "Boolean,Integer") Closure closure) {
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      byte[] array = [1, 2, 3]
     *      String result = ''
     *      array.eachWithIndex{ item, index {@code ->} result += "$index:${item.byteValue()}" }
     *      assert result == '0:11:22:3'
     * }
     * test()
     * </pre>
     *
     * @param self    a byte array
     * @param closure a Closure to operate on each byte
     * @return the self array
     * @since 5.0.0
     */
    public static byte[] eachWithIndex(byte[] self, @ClosureParams(value = FromString.class, options = "Byte,Integer") Closure closure) {
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      char[] array = ['a' as char, 'b' as char, 'c' as char]
     *      String result = ''
     *      array.eachWithIndex{ item, index {@code ->} result += "$index:${item.charValue()}" }
     *      assert result == '0:a1:b2:c'
     * }
     * test()
     * </pre>
     *
     * @param self    a char array
     * @param closure a Closure to operate on each char
     * @return the self array
     * @since 5.0.0
     */
    public static char[] eachWithIndex(char[] self, @ClosureParams(value = FromString.class, options = "Character,Integer") Closure closure) {
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      short[] array = [1, 2, 3]
     *      String result = ''
     *      array.eachWithIndex{ item, index {@code ->} result += "$index:${item.shortValue()}" }
     *      assert result == '0:11:22:3'
     * }
     * test()
     * </pre>
     *
     * @param self    a short array
     * @param closure a Closure to operate on each short
     * @return the self array
     * @since 5.0.0
     */
    public static short[] eachWithIndex(short[] self, @ClosureParams(value = FromString.class, options = "Short,Integer") Closure closure) {
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      int[] array = [1, 2, 3]
     *      String result = ''
     *      array.eachWithIndex{ item, index {@code ->} result += "$index:${item.intValue()}" }
     *      assert result == '0:11:22:3'
     * }
     * test()
     * </pre>
     *
     * @param self    an int array
     * @param closure a Closure to operate on each int
     * @return the self array
     * @since 5.0.0
     */
    public static int[] eachWithIndex(int[] self, @ClosureParams(value = FromString.class, options = "Integer,Integer") Closure closure) {
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      long[] array = [1, 2, 3]
     *      String result = ''
     *      array.eachWithIndex{ item, index {@code ->} result += "$index:${item.longValue()}" }
     *      assert result == '0:11:22:3'
     * }
     * test()
     * </pre>
     *
     * @param self    a long array
     * @param closure a Closure to operate on each long
     * @return the self array
     * @since 5.0.0
     */
    public static long[] eachWithIndex(long[] self, @ClosureParams(value = FromString.class, options = "Long,Integer") Closure closure) {
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
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      float[] array = [1, 2, 3]
     *      String result = ''
     *      array.eachWithIndex{ item, index {@code ->} result += "$index:${item.floatValue()}" }
     *      assert result == '0:1.01:2.02:3.0'
     * }
     * test()
     * </pre>
     *
     * @param self    an float array
     * @param closure a Closure to operate on each float
     * @return the self array
     * @since 5.0.0
     */
    public static float[] eachWithIndex(float[] self, @ClosureParams(value = FromString.class, options = "Float,Integer") Closure closure) {
        final Object[] args = new Object[2];
        for (int i = 0, n = self.length; i < n; i += 1) {
            args[0] = self[i];
            args[1] = i;
            closure.call(args);
        }
        return self;
    }

    /**
     * Iterates through an double[],
     * passing each double and the element's index (a counter starting at
     * zero) to the given closure.
     * <pre class="groovyTestCase">
     * {@code @groovy.transform.TypeChecked}
     * void test(){
     *      double[] array = [1, 2, 3]
     *      String result = ''
     *      array.eachWithIndex{ item, index {@code ->} result += "$index:${item.doubleValue()}" }
     *      assert result == '0:1.01:2.02:3.0'
     * }
     * test()
     * </pre>
     *
     * @param self    a double array
     * @param closure a Closure to operate on each double
     * @return the self array
     * @since 5.0.0
     */
    public static double[] eachWithIndex(double[] self, @ClosureParams(value = FromString.class, options = "Double,Integer") Closure closure) {
        final Object[] args = new Object[2];
        for (int i = 0, n = self.length; i < n; i += 1) {
            args[0] = self[i];
            args[1] = i;
            closure.call(args);
        }
        return self;
    }

    //-------------------------------------------------------------------------
    // equals

    /**
     * Compare the contents of this array to the contents of the given array.
     *
     * @param left  a boolean array
     * @param right the array being compared
     * @return true if the contents of both arrays are equal.
     * @since 5.0.0
     */
    public static boolean equals(boolean[] left, boolean[] right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }
        if (left == right) {
            return true;
        }
        if (left.length != right.length) {
            return false;
        }
        for (int i = 0; i < left.length; i++) {
            if (left[i] != right[i]) return false;
        }
        return true;
    }

    /**
     * Compare the contents of this array to the contents of the given array.
     *
     * @param left  a byte array
     * @param right the array being compared
     * @return true if the contents of both arrays are equal.
     * @since 5.0.0
     */
    public static boolean equals(byte[] left, byte[] right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }
        if (left == right) {
            return true;
        }
        if (left.length != right.length) {
            return false;
        }
        for (int i = 0; i < left.length; i++) {
            if (left[i] != right[i]) return false;
        }
        return true;
    }

    /**
     * Compare the contents of this array to the contents of the given array.
     *
     * @param left  a char array
     * @param right the array being compared
     * @return true if the contents of both arrays are equal.
     * @since 5.0.0
     */
    public static boolean equals(char[] left, char[] right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }
        if (left == right) {
            return true;
        }
        if (left.length != right.length) {
            return false;
        }
        for (int i = 0; i < left.length; i++) {
            if (left[i] != right[i]) return false;
        }
        return true;
    }

    /**
     * Compare the contents of this array to the contents of the given array.
     *
     * @param left  a short array
     * @param right the array being compared
     * @return true if the contents of both arrays are equal.
     * @since 5.0.0
     */
    public static boolean equals(short[] left, short[] right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }
        if (left == right) {
            return true;
        }
        if (left.length != right.length) {
            return false;
        }
        for (int i = 0; i < left.length; i++) {
            if (left[i] != right[i]) return false;
        }
        return true;
    }

    /**
     * Compare the contents of this array to the contents of the given array.
     *
     * @param left  an int array
     * @param right the array being compared
     * @return true if the contents of both arrays are equal.
     * @since 5.0.0
     */
    public static boolean equals(int[] left, int[] right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }
        if (left == right) {
            return true;
        }
        if (left.length != right.length) {
            return false;
        }
        for (int i = 0; i < left.length; i++) {
            if (left[i] != right[i]) return false;
        }
        return true;
    }

    /**
     * Compare the contents of this array to the contents of the given array.
     *
     * @param left  a long array
     * @param right the array being compared
     * @return true if the contents of both arrays are equal.
     * @since 5.0.0
     */
    public static boolean equals(long[] left, long[] right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }
        if (left == right) {
            return true;
        }
        if (left.length != right.length) {
            return false;
        }
        for (int i = 0; i < left.length; i++) {
            if (left[i] != right[i]) return false;
        }
        return true;
    }

    /**
     * Compare the contents of this array to the contents of the given array.
     *
     * @param left  a float array
     * @param right the array being compared
     * @return true if the contents of both arrays are equal.
     * @since 5.0.0
     */
    public static boolean equals(float[] left, float[] right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }
        if (left == right) {
            return true;
        }
        if (left.length != right.length) {
            return false;
        }
        for (int i = 0; i < left.length; i++) {
            if (left[i] != right[i]) return false;
        }
        return true;
    }

    /**
     * Compare the contents of this array to the contents of the given array.
     *
     * @param left  a double array
     * @param right the array being compared
     * @return true if the contents of both arrays are equal.
     * @since 5.0.0
     */
    public static boolean equals(double[] left, double[] right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }
        if (left == right) {
            return true;
        }
        if (left.length != right.length) {
            return false;
        }
        for (int i = 0; i < left.length; i++) {
            if (left[i] != right[i]) return false;
        }
        return true;
    }

    //-------------------------------------------------------------------------
    // every
    //-------------------------------------------------------------------------
    // find
    //-------------------------------------------------------------------------
    // findAll
    //-------------------------------------------------------------------------
    // findIndexOf
    //-------------------------------------------------------------------------
    // findIndexValues
    //-------------------------------------------------------------------------
    // findLastIndexOf
    //-------------------------------------------------------------------------
    // findResult
    //-------------------------------------------------------------------------
    // findResults
    //-------------------------------------------------------------------------
    // first
    //-------------------------------------------------------------------------
    // flatten
    //-------------------------------------------------------------------------
    // getAt
    //-------------------------------------------------------------------------
    // getIndices

    /**
     * Returns indices of the boolean array.
     *
     * @see DefaultGroovyMethods#getIndices(Object[])
     * @since 3.0.8
     */
    public static IntRange getIndices(boolean[] self) {
        return new IntRange(false, 0, self.length);
    }

    /**
     * Returns indices of the byte array.
     *
     * @see DefaultGroovyMethods#getIndices(Object[])
     * @since 3.0.8
     */
    public static IntRange getIndices(byte[] self) {
        return new IntRange(false, 0, self.length);
    }

    /**
     * Returns indices of the char array.
     *
     * @see DefaultGroovyMethods#getIndices(Object[])
     * @since 3.0.8
     */
    public static IntRange getIndices(char[] self) {
        return new IntRange(false, 0, self.length);
    }

    /**
     * Returns indices of the short array.
     *
     * @see DefaultGroovyMethods#getIndices(Object[])
     * @since 3.0.8
     */
    public static IntRange getIndices(short[] self) {
        return new IntRange(false, 0, self.length);
    }

    /**
     * Returns indices of the int array.
     *
     * @see DefaultGroovyMethods#getIndices(Object[])
     * @since 3.0.8
     */
    public static IntRange getIndices(int[] self) {
        return new IntRange(false, 0, self.length);
    }

    /**
     * Returns indices of the long array.
     *
     * @see DefaultGroovyMethods#getIndices(Object[])
     * @since 3.0.8
     */
    public static IntRange getIndices(long[] self) {
        return new IntRange(false, 0, self.length);
    }

    /**
     * Returns indices of the float array.
     *
     * @see DefaultGroovyMethods#getIndices(Object[])
     * @since 3.0.8
     */
    public static IntRange getIndices(float[] self) {
        return new IntRange(false, 0, self.length);
    }

    /**
     * Returns indices of the double array.
     *
     * @see DefaultGroovyMethods#getIndices(Object[])
     * @since 3.0.8
     */
    public static IntRange getIndices(double[] self) {
        return new IntRange(false, 0, self.length);
    }

    //-------------------------------------------------------------------------
    // grep
    //-------------------------------------------------------------------------
    // groupBy
    //-------------------------------------------------------------------------
    // head
    //-------------------------------------------------------------------------
    // indexed
    //-------------------------------------------------------------------------
    // init
    //-------------------------------------------------------------------------
    // inject
    //-------------------------------------------------------------------------
    // iterator
    //-------------------------------------------------------------------------
    // join

    /**
     * Concatenates the string representation of each
     * items in this array, with the given String as a separator between each
     * item.
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
     * Concatenates the string representation of each
     * items in this array, with the given String as a separator between each
     * item.
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
     * Concatenates the string representation of each
     * items in this array, with the given String as a separator between each
     * item.
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
     * Concatenates the string representation of each
     * items in this array, with the given String as a separator between each
     * item.
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
     * Concatenates the string representation of each
     * items in this array, with the given String as a separator between each
     * item.
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
     * Concatenates the string representation of each
     * items in this array, with the given String as a separator between each
     * item.
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
     * Concatenates the string representation of each
     * items in this array, with the given String as a separator between each
     * item.
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
     * Concatenates the string representation of each
     * items in this array, with the given String as a separator between each
     * item.
     *
     * @param self      an array of double
     * @param separator a String separator
     * @return the joined String
     * @since 2.4.1
     */
    public static String join(double[] self, String separator) {
        return DefaultGroovyMethods.join(new DoubleArrayIterator(self), separator);
    }

    //-------------------------------------------------------------------------
    // last
    //-------------------------------------------------------------------------
    // max
    //-------------------------------------------------------------------------
    // min
    //-------------------------------------------------------------------------
    // minus
    //-------------------------------------------------------------------------
    // plus
    //-------------------------------------------------------------------------
    // reverse
    //-------------------------------------------------------------------------
    // reverseEach
    //-------------------------------------------------------------------------
    // shuffle
    //-------------------------------------------------------------------------
    // shuffled
    //-------------------------------------------------------------------------
    // size
    //-------------------------------------------------------------------------
    // sort
    //-------------------------------------------------------------------------
    // split
    //-------------------------------------------------------------------------
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
        return sum(self, 0);
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
        return sum(self, (float) 0);
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
        return sum(self, 0);
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
        double s = initialValue;
        for (double v : self) {
            s += v;
        }
        return s;
    }

    //-------------------------------------------------------------------------
    // swap
    //-------------------------------------------------------------------------
    // tail
    //-------------------------------------------------------------------------
    // take
    //-------------------------------------------------------------------------
    // takeRight
    //-------------------------------------------------------------------------
    // takeWhile
    //-------------------------------------------------------------------------
    // toArrayString
    //-------------------------------------------------------------------------
    // toList
    //-------------------------------------------------------------------------
    // toSet
    //-------------------------------------------------------------------------
    // toSorted
    //-------------------------------------------------------------------------
    // toSpreadMap
    //-------------------------------------------------------------------------
    // toString

    /**
     * Returns the string representation of the given array.
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
     *
     * @param self an array
     * @return the string representation
     * @since 1.6.0
     */
    public static String toString(double[] self) {
        return FormatHelper.toString(self);
    }

    //-------------------------------------------------------------------------
    // toUnique
    //-------------------------------------------------------------------------
    // transpose
    //-------------------------------------------------------------------------
    // union
}
