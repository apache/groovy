/*
 * $Id$
 * 
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 *  1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The name "groovy" must not be used to endorse or promote products
 * derived from this Software without prior written permission of The Codehaus.
 * For written permission, please contact info@codehaus.org.
 *  4. Products derived from this Software may not be called "groovy" nor may
 * "groovy" appear in their names without prior written permission of The
 * Codehaus. "groovy" is a registered trademark of The Codehaus.
 *  5. Due credit should be given to The Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *  
 */
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyObject;
import groovy.lang.Range;
import groovy.util.CharsetToolkit;
import groovy.util.ClosureComparator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class defines all the new groovy methods which appear on normal JDK
 * classes inside the Groovy environment. Static methods are used with the
 * first parameter the destination class.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Sam Pullara
 * @author Rod Cope
 * @author Guillaume Laforge
 * @version $Revision$
 */
public class DefaultGroovyMethods {

    private static Logger log = Logger.getLogger(DefaultGroovyMethods.class.getName());

    private static final Integer ONE = new Integer(1);

    /**
     * Generates a detailed dump string of an object showing its class,
     * hashCode and fields
     */
    public static String dump(Object self) {
        if (self == null) {
            return "null";
        }
        StringBuffer buffer = new StringBuffer("<");
        Class klass = self.getClass();
        buffer.append(klass.getName());
        buffer.append("@");
        buffer.append(Integer.toHexString(self.hashCode()));
        boolean groovyObject = self instanceof GroovyObject;
        while (true) {
            Field[] fields = klass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if ((field.getModifiers() & Modifier.STATIC) == 0) {
                    if (groovyObject && field.getName().equals("metaClass")) {
                        continue;
                    }
                    field.setAccessible(true);
                    buffer.append(" ");
                    buffer.append(field.getName());
                    buffer.append("=");
                    try {
                        buffer.append(InvokerHelper.toString(field.get(self)));
                    }
                    catch (Exception e) {
                        buffer.append(e);
                    }
                }
            }
            klass = klass.getSuperclass();
            if (klass == null) {
                break;
            }
        }
        buffer.append(">");
        return buffer.toString();
    }

    /**
     * Print to a console in interactive format
     */
    public static void print(Object self, Object value) {
        System.out.print(InvokerHelper.toString(value));
    }

    /**
     * Print to a console in interactive format along with a newline
     */
    public static void println(Object self, Object value) {
        System.out.println(InvokerHelper.toString(value));
    }

    /**
     * @return a String that matches what would be typed into a terminal to 
     * create this object. e.g. [1, 'hello'].inspect() -> [1, "hello"]
     */
    public static String inspect(Object self) {
        return InvokerHelper.inspect(self);
    }

    /**
     * Print to a console in interactive format
     */
    public static void print(Object self, PrintWriter out) {
        if (out == null) {
            out = new PrintWriter(System.out);
        }
        out.print(InvokerHelper.toString(self));
    }

    /**
     * Print to a console in interactive format
     */
    /*
     * public static void print(Collection self, PrintWriter out) {
     * out.print("["); boolean first = true; for (Iterator iter =
     * self.iterator(); iter.hasNext(); ) { if (first) { first = false; } else {
     * out.print(", "); } InvokerHelper.invokeMethod(iter.next(), "print",
     * out); } out.print("]"); }
     */

    /**
     * Print to a console in interactive format
     */
    public static void println(Object self, PrintWriter out) {
        if (out == null) {
            out = new PrintWriter(System.out);
        }
        InvokerHelper.invokeMethod(self, "print", out);
        out.println();
    }

    /**
     * Provide a dynamic method invocation method which can be overloaded in
     * classes to implement dynamic proxies easily.
     */
    public static Object invokeMethod(Object object, String method, Object arguments) {
        return InvokerHelper.invokeMethod(object, method, arguments);
    }

    // isCase methods
    //-------------------------------------------------------------------------
    public static boolean isCase(Object caseValue, Object switchValue) {
        return caseValue.equals(switchValue);
    }

    public static boolean isCase(String caseValue, Object switchValue) {
        if (switchValue == null) {
            return caseValue == null;
        }
        return caseValue.equals(switchValue.toString());
    }

    public static boolean isCase(Class caseValue, Object switchValue) {
        return caseValue.isInstance(switchValue);
    }

    public static boolean isCase(Collection caseValue, Object switchValue) {
        return caseValue.contains(switchValue);
    }

    public static boolean isCase(Pattern caseValue, Object switchValue) {
        return caseValue.matcher(switchValue.toString()).matches();
    }

    // Collection based methods
    //-------------------------------------------------------------------------

    /**
     * Allows objects to be iterated through using a closure
     * 
     * @param source
     * @param closure
     */
    public static void each(Object self, Closure closure) {
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            closure.call(iter.next());
        }
    }

    /**
     * Allows objects to be iterated through using a closure
     * 
     * @param source
     * @param closure
     */
    public static void each(Collection self, Closure closure) {
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            closure.call(iter.next());
        }
    }

    /**
     * Allows objects to be iterated through using a closure
     * 
     * @param source
     * @param closure
     */
    public static void each(Map self, Closure closure) {
        for (Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
            closure.call(iter.next());
        }
    }

    /**
     * @return true if every item in the collection matches the closure
     *         predicate
     */
    public static boolean every(Object self, Closure closure) {
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            if (!InvokerHelper.asBool(closure.call(iter.next()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return true if any item in the collection matches the closure predicate
     */
    public static boolean any(Object self, Closure closure) {
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            if (InvokerHelper.asBool(closure.call(iter.next()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the number of occurrencies of the given value inside this collection
     */
    public static int count(Collection self, Object value) {
        int answer = 0;
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            if (InvokerHelper.compareEqual(iter.next(), value)) {
                ++answer;
            }
        }
        return answer;
    }

    /**
     * Maps the values of a collection to new values using the
     * 
     * @param source
     * @param closure
     */
    public static List map(Object self, Closure closure) {
        List answer = new ArrayList();
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            answer.add(closure.call(iter.next()));
        }
        return answer;
    }

    /**
     * Collects the values of the closure
     * 
     * @param source
     * @param closure
     */
    public static List map(Collection self, Closure closure) {
        List answer = new ArrayList(self.size());
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            answer.add(closure.call(iter.next()));
        }
        return answer;
    }

    /**
     * Collects the values of the closure
     * 
     * @param source
     * @param closure
     */
    public static List map(Map self, Closure closure) {
        List answer = new ArrayList(self.size());
        for (Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
            answer.add(closure.call(iter.next()));
        }
        return answer;
    }

    /**
     * Finds the first value matching the closure condition
     * 
     * @param source
     * @param closure
     */
    public static Object find(Object self, Closure closure) {
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            Object value = iter.next();
            if (InvokerHelper.asBool(closure.call(value))) {
                return value;
            }
        }
        return null;
    }

    /**
     * Finds the first value matching the closure condition
     * 
     * @param source
     * @param closure
     */
    public static Object find(Collection self, Closure closure) {
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (InvokerHelper.asBool(closure.call(value))) {
                return value;
            }
        }
        return null;
    }

    /**
     * Finds the first value matching the closure condition
     * 
     * @param source
     * @param closure
     */
    public static Object find(Map self, Closure closure) {
        for (Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (InvokerHelper.asBool(closure.call(value))) {
                return value;
            }
        }
        return null;
    }

    /**
     * Finds all values matching the closure condition
     * 
     * @param source
     * @param closure
     */
    public static Object findAll(Object self, Closure closure) {
        List answer = new ArrayList();
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            Object value = iter.next();
            if (InvokerHelper.asBool(closure.call(value))) {
                answer.add(value);
            }
        }
        return answer;
    }

    /**
     * Finds all values matching the closure condition
     * 
     * @param source
     * @param closure
     */
    public static List findAll(Collection self, Closure closure) {
        List answer = new ArrayList(self.size());
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (InvokerHelper.asBool(closure.call(value))) {
                answer.add(value);
            }
        }
        return answer;
    }

    /**
     * Finds all values matching the closure condition
     * 
     * @param source
     * @param closure
     */
    public static List findAll(Map self, Closure closure) {
        List answer = new ArrayList(self.size());
        for (Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (InvokerHelper.asBool(closure.call(value))) {
                answer.add(value);
            }
        }
        return answer;
    }

    /**
     * Iterates through the given collection, passing in the initial value to
     * the closure along with the current iterated item then passing into the
     * next iteration the value of the previous closure.
     * 
     * @param source
     * @param closure
     */
    public static Object inject(Collection self, Object value, Closure closure) {
        Object[] params = new Object[2];
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            Object item = iter.next();
            params[0] = value;
            params[1] = item;
            value = closure.call(params);
        }
        return value;
    }

    /**
     * Concatenates all of the items of the collection together with the given
     * string as a separator
     */
    public static String join(Collection self, String separator) {
        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (first) {
                first = false;
            }
            else {
                buffer.append(separator);
            }
            buffer.append(InvokerHelper.toString(value));
        }
        return buffer.toString();
    }

    /**
     * Selects the maximum value found in the collection
     * 
     * @param source
     * @param closure
     */
    public static Object max(Collection self) {
        Object answer = null;
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (value != null) {
                if (answer == null || InvokerHelper.compareGreaterThan(value, answer)) {
                    answer = value;
                }
            }
        }
        return answer;
    }

    /**
     * Selects the maximum value found in the collection using the given
     * comparator
     */
    public static Object max(Collection self, Comparator comparator) {
        Object answer = null;
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (answer == null || comparator.compare(value, answer) > 0) {
                answer = value;
            }
        }
        return answer;
    }

    /**
     * Selects the minimum value found in the collection
     */
    public static Object min(Collection self) {
        Object answer = null;
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (value != null) {
                if (answer == null || InvokerHelper.compareLessThan(value, answer)) {
                    answer = value;
                }
            }
        }
        return answer;
    }

    /**
     * Selects the minimum value found in the collection using the given
     * comparator
     */
    public static Object min(Collection self, Comparator comparator) {
        Object answer = null;
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (answer == null || comparator.compare(value, answer) < 0) {
                answer = value;

            }
        }
        return answer;
    }

    /**
     * Selects the minimum value found in the collection using the given
     * closure as a comparator
     */
    public static Object min(Collection self, Closure closure) {
        return min(self, new ClosureComparator(closure));
    }

    /**
     * Selects the maximum value found in the collection using the given
     * closure as a comparator
     */
    public static Object max(Collection self, Closure closure) {
        return max(self, new ClosureComparator(closure));
    }

    /**
     * Makes a String look like a Collection by adding support for the size()
     * method
     * 
     * @param text
     * @return
     */
    public static int size(String text) {
        return text.length();
    }

    /**
     * Makes an Array look like a Collection by adding support for the size()
     * method
     * 
     * @param text
     * @return
     */
    public static int size(Object[] self) {
        return self.length;
    }

    /**
     * Support the subscript operator for String
     * 
     * @param text
     * @return the Character object at the given index
     */
    public static CharSequence getAt(CharSequence text, int index) {
        index = normaliseIndex(index, text.length());
        return text.subSequence(index, index + 1);
    }

    /**
     * Support the subscript operator for String
     * 
     * @param text
     * @return the Character object at the given index
     */
    public static String getAt(String text, int index) {
        index = normaliseIndex(index, text.length());
        return text.substring(index, index + 1);
    }

    /**
     * Support the range subscript operator for CharSequence
     */
    public static CharSequence getAt(CharSequence text, Range range) {
        int from = normaliseIndex(InvokerHelper.asInt(range.getFrom()), text.length());
        int to = normaliseIndex(InvokerHelper.asInt(range.getTo()), text.length());
        int length = text.length();

        // if this is a backwards range, reverse the arguments to substring
        if (from > to) {
            int tmp = from;
            from = to;
            to = tmp;
        }

        return text.subSequence(from, to + 1);
    }

    /**
     * Support the range subscript operator for String
     */
    public static String getAt(String text, Range range) {
        int from = normaliseIndex(InvokerHelper.asInt(range.getFrom()), text.length());
        int to = normaliseIndex(InvokerHelper.asInt(range.getTo()), text.length());
        int length = text.length();

        // if this is a backwards range, reverse the arguments to substring
        if (from > to) {
            int tmp = from;
            from = to;
            to = tmp;
        }

        return text.substring(from, to + 1);
    }

    /**
     * Support the subscript operator for a regex Matcher
     * 
     * @returns the group at the given index
     */
    public static String getAt(Matcher matcher, int idx) {
        matcher.reset();
        idx = normaliseIndex(idx, matcher.groupCount());

        // are we using groups?
        if (matcher.groupCount() > 0) {
            // yes, so return the specified group
            matcher.find();
            return matcher.group(idx);
        }
        else {
            // not using groups, so return the nth
            // occurrence of the pattern
            for (int i = 0; i <= idx; i++) {
                matcher.find();
            }
            return matcher.group();
        }
    }

    /**
     * Support the range subscript operator for a List
     * 
     * @returns a range of a list from the range's from index up to but not
     * including the ranges's to value
     */
    public static List getAt(List list, Range range) {
        return list.subList(InvokerHelper.asInt(range.getFrom()), InvokerHelper.asInt(range.getTo()) + 1);
    }

    /**
     * Allows a List to be used as the indices to be used on a List
     * 
     * @returns a new list of the values at the given indices
     */
    public static List getAt(List self, Collection indices) {
        List answer = new ArrayList(indices.size());
        for (Iterator iter = indices.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (value instanceof Range) {
                answer.addAll(getAt(self, (Range) value));
            }
            else if (value instanceof List) {
                answer.addAll(getAt(self, (List) value));
            }
            else {
                int idx = InvokerHelper.asInt(value);
                answer.add(getAt(self, idx));
            }
        }
        return answer;
    }

    /**
     * Allows a List to be used as the indices to be used on a List
     * 
     * @returns a new list of the values at the given indices
     */
    public static List getAt(Object[] self, Collection indices) {
        List answer = new ArrayList(indices.size());
        for (Iterator iter = indices.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (value instanceof Range) {
                answer.addAll(getAt(self, (Range) value));
            }
            else if (value instanceof Collection) {
                answer.addAll(getAt(self, (Collection) value));
            }
            else {
                int idx = InvokerHelper.asInt(value);
                answer.add(getAt(self, idx));
            }
        }
        return answer;
    }

    /**
     * Allows a List to be used as the indices to be used on a CharSequence
     * 
     * @returns a String of the values at the given indices
     */
    public static CharSequence getAt(CharSequence self, Collection indices) {
        StringBuffer answer = new StringBuffer();
        for (Iterator iter = indices.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (value instanceof Range) {
                answer.append(getAt(self, (Range) value));
            }
            else if (value instanceof Collection) {
                answer.append(getAt(self, (Collection) value));
            }
            else {
                int idx = InvokerHelper.asInt(value);
                answer.append(getAt(self, idx));
            }
        }
        return answer.toString();
    }

    /**
     * Allows a List to be used as the indices to be used on a String
     * 
     * @returns a String of the values at the given indices
     */
    public static String getAt(String self, Collection indices) {
        return (String) getAt((CharSequence) self, indices);
    }

    /**
     * Allows a List to be used as the indices to be used on a Matcher
     * 
     * @returns a String of the values at the given indices
     */
    public static String getAt(Matcher self, Collection indices) {
        StringBuffer answer = new StringBuffer();
        for (Iterator iter = indices.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (value instanceof Range) {
                answer.append(getAt(self, (Range) value));
            }
            else if (value instanceof Collection) {
                answer.append(getAt(self, (Collection) value));
            }
            else {
                int idx = InvokerHelper.asInt(value);
                answer.append(getAt(self, idx));
            }
        }
        return answer.toString();
    }

    /**
     * Creates a sub-Map containing the given keys. This method is similar to
     * List.subList() but uses keys rather than index ranges.
     * 
     * @returns a new Map containing the given keys
     */
    public static Map subMap(Map map, Collection keys) {
        Map answer = new HashMap(keys.size());
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            Object key = iter.next();
            answer.put(key, map.get(key));
        }
        return answer;
    }

    /**
     * Support the range subscript operator for an Array
     * 
     * @returns a range of a list from the range's from index up to but not
     * including the ranges's to value
     */
    public static List getAt(Object[] array, Range range) {
        List list = Arrays.asList(array);
        return getAt(list, range);
    }

    /**
     * Support the subscript operator for an Array
     * 
     * @returns the value at the given index
     */
    public static Object getAt(Object[] array, int idx) {
        return array[normaliseIndex(idx, array.length)];
    }

    /**
     * Support the subscript operator for an Array
     *  
     */
    public static void putAt(Object[] array, int idx, Object value) {
        array[normaliseIndex(idx, array.length)] = value;
    }

    /**
     * Allows conversion of arrays into a mutable List
     * 
     * @returns the array as a List
     */
    public static List toList(Object[] array) {
        int size = array.length;
        List list = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            list.add(array[i]);
        }
        return list;
    }

    /**
     * Support the subscript operator for a List
     * 
     * @returns the value at the given index
     */
    public static Object getAt(List self, int idx) {
        int size = self.size();
        int i = normaliseIndex(idx, size);
        if (i < size) {
            return self.get(i);
        }
        else {
            return null;
        }
    }

    /**
     * A helper method to allow lists to work with subscript operators
     */
    public static void putAt(List self, int i, Object value) {
        int size = self.size();
        i = normaliseIndex(i, size);
        if (i < size) {
            self.set(i, value);
        }
        else {
            while (size < i) {
                self.add(size++, null);
            }
            self.add(i, value);
        }
    }

    /**
     * Support the subscript operator for a List
     * 
     * @returns the value at the given index
     */
    public static Object getAt(Map self, Object key) {
        return self.get(key);
    }

    /**
     * A helper method to allow lists to work with subscript operators
     */
    public static Object putAt(Map self, Object key, Object value) {
        return self.put(key, value);
    }

    protected static int normaliseIndex(int i, int size) {
        while (i < 0) {
            i += size;
        }
        return i;
    }

    /**
     * Support the subscript operator for List
     * 
     * @return
     */
    public static List getAt(Collection coll, String property) {
        List answer = new ArrayList(coll.size());
        for (Iterator iter = coll.iterator(); iter.hasNext();) {
            Object item = iter.next();
            Object value = InvokerHelper.getProperty(item, property);
            if (value instanceof Collection) {
                answer.addAll((Collection) value);
            }
            else {
                answer.add(value);
            }
        }
        return answer;
    }

    /**
     * A convenience method for creating an immutable map
     */
    public static Map immutable(Map self) {
        return Collections.unmodifiableMap(self);
    }

    /**
     * A convenience method for creating an immutable sorted map
     */
    public static SortedMap immutable(SortedMap self) {
        return Collections.unmodifiableSortedMap(self);
    }

    /**
     * A convenience method for creating an immutable list
     */
    public static List immutable(List self) {
        return Collections.unmodifiableList(self);
    }

    /**
     * A convenience method for creating an immutable list
     */
    public static Set immutable(Set self) {
        return Collections.unmodifiableSet(self);
    }

    /**
     * A convenience method for creating an immutable sorted set
     */
    public static SortedSet immutable(SortedSet self) {
        return Collections.unmodifiableSortedSet(self);
    }

    /**
     * A convenience method for sorting a List
     */
    public static List sort(List self) {
        Collections.sort(self);
        return self;
    }

    /**
     * A convenience method for sorting a List with a specific comparator
     */
    public static List sort(List self, Comparator comparator) {
        Collections.sort(self, comparator);
        return self;
    }

    /**
     * A convenience method for sorting a List using a closure as a comparator
     */
    public static List sort(List self, Closure closure) {
        Collections.sort(self, new ClosureComparator(closure));
        return self;
    }

    /**
     * Reverses the list
     */
    public static List reverse(List self) {
        int size = self.size();
        List answer = new ArrayList(size);
        ListIterator iter = self.listIterator(size);
        while (iter.hasPrevious()) {
            answer.add(iter.previous());
        }
        return answer;
    }

    public static List plus(Collection left, Collection right) {
        List answer = new ArrayList(left.size() + right.size());
        answer.addAll(left);
        answer.addAll(right);
        return answer;
    }

    public static List plus(Collection left, Object right) {
        List answer = new ArrayList(left.size() + 1);
        answer.addAll(left);
        answer.add(right);
        return answer;
    }

    public static List multiply(Collection self, Number factor) {
        int size = factor.intValue();
        List answer = new ArrayList(self.size() * size);
        for (int i = 0; i < size; i++) {
            answer.addAll(self);
        }
        return answer;
    }

    public static List intersect(List left, Collection right) {

        if (left.size() == 0)
            return new ArrayList();

        boolean nlgnSort = sameType(new Collection[] { left, right });

        ArrayList result = new ArrayList();
        //creates the collection to look for values.
        Collection pickFrom = nlgnSort ? (Collection) new TreeSet(left) : left;

        for (Iterator iter = right.iterator(); iter.hasNext();) {
            final Object o = iter.next();
            if (pickFrom.contains(o))
                result.add(o);
        }
        return result;
    }

    public static List minus(List self, Collection removeMe) {

        if (self.size() == 0)
            return new ArrayList();

        boolean nlgnSort = sameType(new Collection[] { self, removeMe });

        //we can't use the same tactic as for intersection
        //since AbstractCollection only does a remove on the first
        //element it encounter.

        if (nlgnSort) {
            //n*log(n) version
            Set answer = new TreeSet(self);
            answer.removeAll(removeMe);
            return new ArrayList(answer);
        }
        else {
            //n*n version
            List tmpAnswer = new LinkedList(self);
            for (Iterator iter = tmpAnswer.iterator(); iter.hasNext();) {
                Object element = iter.next();
                //boolean removeElement = false;
                for (Iterator iterator = removeMe.iterator(); iterator.hasNext();) {
                    if (element.equals(iterator.next())) {
                        iter.remove();
                    }
                }
            }
            //remove duplicates
            //can't use treeset since the base classes are different
            List answer = new LinkedList();
            Object[] array = (Object[]) tmpAnswer.toArray(new Object[tmpAnswer.size()]);

            for (int i = 0; i < array.length; i++) {
                if (array[i] != null) {
                    for (int j = i + 1; j < array.length; j++) {
                        if (array[i].equals(array[j])) {
                            array[j] = null;
                        }
                    }
                    answer.add(array[i]);
                }
            }
            return new ArrayList(answer);
        }
    }

    public static List flatten(List self) {
        return new ArrayList(flatten(self, new LinkedList()));
    }

    private static List flatten(Collection elements, List addTo) {
        Iterator iter = elements.iterator();
        while (iter.hasNext()) {
            Object element = iter.next();
            if (element instanceof Collection) {
                flatten((Collection) element, addTo);
            }
            else if (element instanceof Map) {
                flatten(((Map) element).values(), addTo);
            }
            else {
                addTo.add(element);
            }
        }
        return addTo;
    }
    
    /**
     * Overloads the left shift operator to provide an append mechanism to add things
     * to a list
     */
    public static Collection leftShift(Collection self, Object value) {
        self.add(value);
        return self;
    }

    /**
     * Overloads the left shift operator to provide an append mechanism to add things
     * to a String buffer
     */
    public static StringBuffer leftShift(StringBuffer self, Object value) {
        self.append(value);
        return self;
    }

    /**
     * Overloads the left shift operator to provide an append mechanism to add things
     * to a writer
     */
    public static PrintWriter leftShift(PrintWriter self, Object value) {
        self.print(value);
        return self;
    }

    private static boolean sameType(Collection[] cols) {
        List all = new LinkedList();
        for (int i = 0; i < cols.length; i++) {
            all.addAll(cols[i]);
        }
        if (all.size() == 0)
            return true;

        Object first = all.get(0);

        //trying to determine the base class of the collections
        //special case for Numbers
        Class baseClass;
        if (first instanceof Number) {
            baseClass = Number.class;
        }
        else {
            baseClass = first.getClass();
        }

        for (int i = 0; i < cols.length; i++) {
            for (Iterator iter = cols[i].iterator(); iter.hasNext();) {
                if (!baseClass.isInstance(iter.next())) {
                    return false;
                }
            }
        }
        return true;
    }

    // Primitive type array methods
    //-------------------------------------------------------------------------

    public static Object getAt(byte[] array, int idx) {
        return primitiveArrayGet(array, idx);
    }
    public static Object getAt(char[] array, int idx) {
        return primitiveArrayGet(array, idx);
    }
    public static Object getAt(short[] array, int idx) {
        return primitiveArrayGet(array, idx);
    }
    public static Object getAt(int[] array, int idx) {
        return primitiveArrayGet(array, idx);
    }
    public static Object getAt(long[] array, int idx) {
        return primitiveArrayGet(array, idx);
    }
    public static Object getAt(float[] array, int idx) {
        return primitiveArrayGet(array, idx);
    }
    public static Object getAt(double[] array, int idx) {
        return primitiveArrayGet(array, idx);
    }

    public static Object getAt(byte[] array, Range range) {
        return primitiveArrayGet(array, range);
    }
    public static Object getAt(char[] array, Range range) {
        return primitiveArrayGet(array, range);
    }
    public static Object getAt(short[] array, Range range) {
        return primitiveArrayGet(array, range);
    }
    public static Object getAt(int[] array, Range range) {
        return primitiveArrayGet(array, range);
    }
    public static Object getAt(long[] array, Range range) {
        return primitiveArrayGet(array, range);
    }
    public static Object getAt(float[] array, Range range) {
        return primitiveArrayGet(array, range);
    }
    public static Object getAt(double[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    public static Object getAt(byte[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }
    public static Object getAt(char[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }
    public static Object getAt(short[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }
    public static Object getAt(int[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }
    public static Object getAt(long[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }
    public static Object getAt(float[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }
    public static Object getAt(double[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    public static void putAt(byte[] array, int idx, Object newValue) {
        primitiveArrayPut(array, idx, newValue);
    }

    public static void putAt(char[] array, int idx, Object newValue) {
        primitiveArrayPut(array, idx, newValue);
    }
    public static void putAt(short[] array, int idx, Object newValue) {
        primitiveArrayPut(array, idx, newValue);
    }
    public static void putAt(int[] array, int idx, Object newValue) {
        primitiveArrayPut(array, idx, newValue);
    }
    public static void putAt(long[] array, int idx, Object newValue) {
        primitiveArrayPut(array, idx, newValue);
    }
    public static void putAt(float[] array, int idx, Object newValue) {
        primitiveArrayPut(array, idx, newValue);
    }
    public static void putAt(double[] array, int idx, Object newValue) {
        primitiveArrayPut(array, idx, newValue);
    }

    public static int size(byte[] array) {
        return Array.getLength(array);
    }
    public static int size(char[] array) {
        return Array.getLength(array);
    }
    public static int size(short[] array) {
        return Array.getLength(array);
    }
    public static int size(int[] array) {
        return Array.getLength(array);
    }
    public static int size(long[] array) {
        return Array.getLength(array);
    }
    public static int size(float[] array) {
        return Array.getLength(array);
    }
    public static int size(double[] array) {
        return Array.getLength(array);
    }

    public static List toList(byte[] array) {
        return InvokerHelper.primitiveArrayToList(array);
    }
    public static List toList(char[] array) {
        return InvokerHelper.primitiveArrayToList(array);
    }
    public static List toList(short[] array) {
        return InvokerHelper.primitiveArrayToList(array);
    }
    public static List toList(int[] array) {
        return InvokerHelper.primitiveArrayToList(array);
    }
    public static List toList(long[] array) {
        return InvokerHelper.primitiveArrayToList(array);
    }
    public static List toList(float[] array) {
        return InvokerHelper.primitiveArrayToList(array);
    }
    public static List toList(double[] array) {
        return InvokerHelper.primitiveArrayToList(array);
    }

    /**
     * Implements the getAt(int) method for primitve type arrays
     */
    protected static Object primitiveArrayGet(Object array, int idx) {
        return Array.get(array, normaliseIndex(idx, Array.getLength(array)));
    }

    /**
     * Implements the getAt(Range) method for primitve type arrays
     */
    protected static List primitiveArrayGet(Object array, Range range) {
        List answer = new ArrayList();
        for (Iterator iter = range.iterator(); iter.hasNext();) {
            int idx = InvokerHelper.asInt(iter.next());
            answer.add(primitiveArrayGet(array, idx));
        }
        return answer;
    }

    /**
     * Implements the getAt(Collection) method for primitve type arrays
     */
    protected static List primitiveArrayGet(Object self, Collection indices) {
        List answer = new ArrayList();
        for (Iterator iter = indices.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (value instanceof Range) {
                answer.addAll(primitiveArrayGet(self, (Range) value));
            }
            else if (value instanceof List) {
                answer.addAll(primitiveArrayGet(self, (List) value));
            }
            else {
                int idx = InvokerHelper.asInt(value);
                answer.add(primitiveArrayGet(self, idx));
            }
        }
        return answer;
    }

    /**
     * Implements the set(int idx) method for primitve type arrays
     */
    protected static void primitiveArrayPut(Object array, int idx, Object newValue) {
        Array.set(array, normaliseIndex(idx, Array.getLength(array)), newValue);
    }

    // String methods
    //-------------------------------------------------------------------------
    public static Object tokenize(String self, String token) {
        return InvokerHelper.asList(new StringTokenizer(self, token));
    }

    public static Object tokenize(String self) {
        return InvokerHelper.asList(new StringTokenizer(self));
    }

    public static String plus(String left, Object value) {
        //return left + value;
        return left + toString(value);
    }

    public static String minus(String left, Object value) {
        String text = toString(value);
        return left.replaceFirst(text, "");
    }

    /**
     * Provide an implementation of contains() like Collection to make Strings more polymorphic
     * This method is not required on JDK 1.5 onwards
     * 
     * @return true if this string contains the given text
     */
    public static boolean contains(String self, String text) {
        int idx = self.indexOf(text);
        return idx >= 0;
    }

    /**
     * @return the number of occurrencies of the given string inside this String
     */
    public static int count(String self, String text) {
        int answer = 0;
        for (int idx = 0; true; idx++) {
            idx = self.indexOf(text, idx);
            if (idx >= 0) {
                ++answer;
            }
            else {
                break;
            }
        }
        return answer;
    }

    /**
     * Increments the last digit in the given string, resetting
     * it and moving onto the next digit if increasing the digit
     * no longer becomes a letter or digit.
     * 
     * @return
     */
    public static String increment(String self) {
        StringBuffer buffer = new StringBuffer(self);
        char firstCh = firstCharacter();
        for (int idx = buffer.length() - 1; idx >= 0; idx-- ) {
            char ch = increment(buffer.charAt(idx));
            if (ch != 0) {
                buffer.setCharAt(idx, ch);
                break;
            }
            else {
                // lets find the first char
                buffer.setCharAt(idx, firstCh);
            }
        }
        return buffer.toString();
    }
    
    /**
     * Decrements the last digit in the given string, resetting
     * it and moving onto the next digit if increasing the digit
     * no longer becomes a letter or digit.
     * 
     * @return
     */
    public static String decrement(String self) {
        StringBuffer buffer = new StringBuffer(self);
        char lastCh = lastCharacter();
        for (int idx = buffer.length() - 1; idx >= 0; idx-- ) {
            char ch = decrement(buffer.charAt(idx));
            if (ch != 0) {
                buffer.setCharAt(idx, ch);
                break;
            }
            else {
                // lets find the first char
                buffer.setCharAt(idx, lastCh);
            }
        }
        return buffer.toString();
    }
    
    private static char increment(char ch) {
        if (Character.isLetterOrDigit(++ch)) {
            return ch;
        }
        else {
            return 0;
        }
    }
    
    private static char decrement(char ch) {
        if (Character.isLetterOrDigit(--ch)) {
            return ch;
        }
        else {
            return 0;
        }
    }
    
    /**
     * @return the first character used when a letter rolls over when incrementing
     */
    private static char firstCharacter() {
        char ch = 0;
        while (! Character.isLetterOrDigit(ch)) {
            ch++;
        }
        return ch;
    }

    /**
     * @return the last character used when a letter rolls over when decrementing
     */
    private static char lastCharacter() {
        char ch = firstCharacter();
        while (Character.isLetterOrDigit(++ch));
        return --ch;
    }

    public static String multiply(String self, Number factor) {
        int size = factor.intValue();
        if (size < 1) {
            throw new IllegalArgumentException(
                "multiply() should be called with a number of 1 or greater not: " + size);
        }
        StringBuffer answer = new StringBuffer(self);
        for (int i = 1; i < size; i++) {
            answer.append(self);
        }
        return answer.toString();
    }

    // Number based methods
    //-------------------------------------------------------------------------
    protected static String toString(Object value) {
        return (value == null) ? "null" : value.toString();
    }

    public static Number increment(Number self) {
        return plus(self, ONE);
    }

    public static Number decrement(Number self) {
        return minus(self, ONE);
    }

    public static Number plus(Number left, Number right) {
        /** @todo maybe a double dispatch thing to handle new large numbers? */
        if (isFloatingPoint(left) || isFloatingPoint(right)) {
            return new Double(left.doubleValue() + right.doubleValue());
        }
        else if (isLong(left) || isLong(right)) {
            return new Long(left.longValue() + right.longValue());
        }
        else {
            return new Integer(left.intValue() + right.intValue());
        }
    }

    public static int compareTo(Number left, Number right) {
        /** @todo maybe a double dispatch thing to handle new large numbers? */
        if (isFloatingPoint(left) || isFloatingPoint(right)) {
            double diff = left.doubleValue() - right.doubleValue();
            if (diff == 0) {
                return 0;
            }
            else {
                return (diff > 0) ? 1 : -1;
            }
        }
        else if (isLong(left) || isLong(right)) {
            long diff = left.longValue() - right.longValue();
            if (diff == 0) {
                return 0;
            }
            else {
                return (diff > 0) ? 1 : -1;
            }
        }
        else {
            int diff = left.intValue() - right.intValue();
            if (diff == 0) {
                return 0;
            }
            else {
                return (diff > 0) ? 1 : -1;
            }
        }
    }

    public static Number minus(Number left, Number right) {
        if (isFloatingPoint(left) || isFloatingPoint(right)) {
            return new Double(left.doubleValue() - right.doubleValue());
        }
        else if (isLong(left) || isLong(right)) {
            return new Long(left.longValue() - right.longValue());
        }
        else {
            return new Integer(left.intValue() - right.intValue());
        }
    }

    public static Number multiply(Number left, Number right) {
        if (isFloatingPoint(left) || isFloatingPoint(right)) {
            return new Double(left.doubleValue() * right.doubleValue());
        }
        else if (isLong(left) || isLong(right)) {
            return new Long(left.longValue() * right.longValue());
        }
        else {
            return new Integer(left.intValue() * right.intValue());
        }
    }

    public static Number power(Number self, Number exponent) {
        double answer = Math.pow(self.doubleValue(), exponent.doubleValue());
        if (isFloatingPoint(self) || isFloatingPoint(exponent) || answer < 1) {
            return new Double(answer);
        }
        else if (isLong(self) || isLong(exponent) || answer > Integer.MAX_VALUE) {
            return new Long((long) answer);
        }
        else {
            return new Integer((int) answer);
        }
    }

    public static Number divide(Number left, Number right) {
        // lets use double for division?
        return new Double(left.doubleValue() / right.doubleValue());
    }

    public static boolean isLong(Number number) {
        return number instanceof Long;
    }

    public static boolean isFloatingPoint(Number number) {
        return number instanceof Float || number instanceof Double;
    }

    /**
     * Iterates a number of times
     */
    public static void times(Number self, Closure closure) {
        for (int i = 0, size = self.intValue(); i < size; i++) {
            closure.call(new Integer(i));
        }
    }

    /**
     * Iterates from this number up to the given number
     */
    public static void upto(Number self, Number to, Closure closure) {
        for (int i = self.intValue(), size = to.intValue(); i <= size; i++) {
            closure.call(new Integer(i));
        }
    }

    /**
     * Iterates from this number up to the given number using a step increment
     */
    public static void step(Number self, Number to, Number stepNumber, Closure closure) {
        for (int i = self.intValue(), size = to.intValue(), step = stepNumber.intValue(); i < size; i += step) {
            closure.call(new Integer(i));
        }
    }

    public static int abs(Number number) {
        return Math.abs(number.intValue());
    }

    public static long abs(Long number) {
        return Math.abs(number.longValue());
    }

    public static float abs(Float number) {
        return Math.abs(number.floatValue());
    }

    public static double abs(Double number) {
        return Math.abs(number.doubleValue());
    }

    public static int round(Float number) {
        return Math.round(number.floatValue());
    }

    public static long round(Double number) {
        return Math.round(number.doubleValue());
    }

    // File based methods
    //-------------------------------------------------------------------------

    /**
     * Iterates through the given file line by line
     */
    public static void eachLine(File self, Closure closure) throws IOException {
        BufferedReader reader = newReader(self);
        try {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                else {
                    closure.call(line);
                }
            }
            reader.close();
        }
        catch (IOException e) {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (Exception e2) {
                    // ignore as we're already throwing
                }
                throw e;
            }
        }
    }

    /**
     * Reads the file into a list of Strings for each line
     */
    public static List readLines(File file) throws IOException {
        IteratorClosureAdapter closure = new IteratorClosureAdapter(file);
        eachLine(file, closure);
        return closure.asList();
    }

    /**
     * Invokes the closure for each file in the given directory
     */
    public static void eachFile(File self, Closure closure) {
        File[] files = self.listFiles();
        for (int i = 0; i < files.length; i++) {
            closure.call(files[i]);
        }
    }

    /**
     * Helper method to create a buffered reader for a file
     * 
     * @param file
     * @return @throws
     *         IOException
     */
    public static BufferedReader newReader(File file) throws IOException {
        CharsetToolkit toolkit = new CharsetToolkit(file);
        return toolkit.getReader();
    }

    /**
     * Helper method to create a new BufferedReader for a file and then 
     * passes it into the closure and ensures its closed again afterwords
     * 
     * @param file
     * @return @throws
     *         FileNotFoundException
     */
    public static void withReader(File file, Closure closure) throws IOException {
        withReader(newReader(file), closure);
    }
    
    /**
     * Helper method to create a buffered output stream for a file
     *
     * @param file
     * @return @throws
     *         FileNotFoundException
     */
    public static BufferedOutputStream newOutputStream(File file) throws IOException {
        return new BufferedOutputStream(new FileOutputStream(file));
    }

    /**
     * Helper method to create a new OutputStream for a file and then 
     * passes it into the closure and ensures its closed again afterwords
     * 
     * @param file
     * @return @throws
     *         FileNotFoundException
     */
    public static void withOutputStream(File file, Closure closure) throws IOException {
        withStream(newOutputStream(file), closure);
    }

    
    /**
     * Helper method to create a buffered writer for a file
     * 
     * @param file
     * @return @throws
     *         FileNotFoundException
     */
    public static BufferedWriter newWriter(File file) throws IOException {
        return new BufferedWriter(new FileWriter(file));
    }

    /**
     * Helper method to create a new BufferedWriter for a file and then 
     * passes it into the closure and ensures its closed again afterwords
     * 
     * @param file
     * @return @throws
     *         FileNotFoundException
     */
    public static void withWriter(File file, Closure closure) throws IOException {
        withWriter(newWriter(file), closure);
    }
    
    /**
     * Helper method to create a new PrintWriter for a file
     * 
     * @param file
     * @return @throws
     *         FileNotFoundException
     */
    public static PrintWriter newPrintWriter(File file) throws IOException {
        return new PrintWriter(newWriter(file));
    }

    /**
     * Helper method to create a new PrintWriter for a file and then 
     * passes it into the closure and ensures its closed again afterwords
     * 
     * @param file
     * @return @throws
     *         FileNotFoundException
     */
    public static void withPrintWriter(File file, Closure closure) throws IOException {
        withWriter(newPrintWriter(file), closure);
    }

    /**
     * Allows a writer to be used, calling the closure with the writer
     * and then ensuring that the writer is closed down again irrespective
     * of whether exceptions occur or the 
     * 
     * @param writer the writer which is used and then closed
     * @param closure the closure that the writer is passed into 
     * @throws IOException
     */
    public static void withWriter(Writer writer, Closure closure) throws IOException {
        try {
            closure.call(writer);
            
            // lets try close the writer & throw the exception if it fails
            // but not try to reclose it in the finally block
            Writer temp = writer;
            writer = null;
            temp.close();
        }
        finally {
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (IOException e) {
                    log.warning("Caught exception closing writer: " + e);
                }
            }
        }
    }
    
    /**
     * Allows a Reader to be used, calling the closure with the writer
     * and then ensuring that the writer is closed down again irrespective
     * of whether exceptions occur or the 
     * 
     * @param writer the writer which is used and then closed
     * @param closure the closure that the writer is passed into 
     * @throws IOException
     */
    public static void withReader(Reader writer, Closure closure) throws IOException {
        try {
            closure.call(writer);
            
            // lets try close the writer & throw the exception if it fails
            // but not try to reclose it in the finally block
            Reader temp = writer;
            writer = null;
            temp.close();
        }
        finally {
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (IOException e) {
                    log.warning("Caught exception closing writer: " + e);
                }
            }
        }
    }
    
    /**
     * Allows a InputStream to be used, calling the closure with the stream
     * and then ensuring that the stream is closed down again irrespective
     * of whether exceptions occur or the 
     * 
     * @param stream the stream which is used and then closed
     * @param closure the closure that the stream is passed into 
     * @throws IOException
     */
    public static void withStream(InputStream stream, Closure closure) throws IOException {
        try {
            closure.call(stream);
            
            // lets try close the stream & throw the exception if it fails
            // but not try to reclose it in the finally block
            InputStream temp = stream;
            stream = null;
            temp.close();
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                }
                catch (IOException e) {
                    log.warning("Caught exception closing stream: " + e);
                }
            }
        }
    }
    
    /**
     * Allows a OutputStream to be used, calling the closure with the stream
     * and then ensuring that the stream is closed down again irrespective
     * of whether exceptions occur or the 
     * 
     * @param stream the stream which is used and then closed
     * @param closure the closure that the stream is passed into 
     * @throws IOException
     */
    public static void withStream(OutputStream stream, Closure closure) throws IOException {
        try {
            closure.call(stream);
            
            // lets try close the stream & throw the exception if it fails
            // but not try to reclose it in the finally block
            OutputStream temp = stream;
            stream = null;
            temp.close();
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                }
                catch (IOException e) {
                    log.warning("Caught exception closing stream: " + e);
                }
            }
        }
    }
    
    /**
     * Helper method to create a buffered input stream for a file
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public static BufferedInputStream newInputStream(File file) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(file));
    }

    /**
     * Iterates through the given file byte by byte.
     */
    public static void eachByte(File self, Closure closure) throws IOException {
        BufferedInputStream is = newInputStream(self);
        try {
            while (true) {
                int b = is.read();
                if (b == -1) {
                    break;
                }
                else {
                    closure.call(new Byte((byte) b));
                }
            }
            is.close();
        }
        catch (IOException e) {
            if (is != null) {
                try {
                    is.close();
                }
                catch (Exception e2) {
                    // ignore as we're already throwing
                }
                throw e;
            }
        }
    }

    /**
     * Reads the file into a list of Bytes for each byte
     */
    public static List readBytes(File file) throws IOException {
        IteratorClosureAdapter closure = new IteratorClosureAdapter(file);
        eachByte(file, closure);
        return closure.asList();
    }

    // SQL based methods
    //-------------------------------------------------------------------------

    /**
     * Iterates through the result set of an SQL query passing the result set
     * into the closure
     * 
     * @param connection
     * @param expression
     * @param closure
     */
    public static void query(Connection connection, GString gstring, Closure closure) throws SQLException {
        ResultSet results = null;

        // lets turn the expression into an SQL string
        String sql = null;
        String[] text = gstring.getStrings();
        if (text.length == 1) {
            sql = text[0];
        }
        else {
            StringBuffer buffer = new StringBuffer(text[0]);
            for (int i = 1; i < text.length; i++) {
                buffer.append("?");
                buffer.append(text[i]);
            }
            sql = buffer.toString();
        }
        PreparedStatement statement = connection.prepareStatement(sql);
        try {
            // lets bind the values to the statement
            Object[] values = gstring.getValues();
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }
            results = statement.executeQuery();

            closure.call(results);
        }
        finally {
            if (results != null) {
                try {
                    results.close();
                }
                catch (SQLException e) {
                    // ignore
                }
            }
            try {
                statement.close();
            }
            catch (SQLException e) {
                // ignore
            }
        }
    }

    /**
     * Converts the given String into a List of strings of one character
     */
    public static List toList(String self) {
        int size = self.length();
        List answer = new ArrayList(size);
        for (int i = 0; i < size; i++ ) {
            answer.add(self.substring(i, i+1));
        }
        return answer;
    }
}
