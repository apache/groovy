/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.Range;
import groovy.util.ClosureComparator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import java.util.regex.Pattern;

/**
 * This class defines all the new groovy methods which appear on normal JDK
 * classes inside the Groovy environment. Static methods are used with the 
 * first parameter the destination class.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class DefaultGroovyMethods {

    private static final Integer ONE = new Integer(1);

    /**
     * Generates a detailed inspection string of an object showing its
     * class, hashCode and fields 
     */
    public static String inspect(Object self) {
        if (self == null) {
            return "null";
        }
        StringBuffer buffer = new StringBuffer("<");
        Class klass = self.getClass();
        buffer.append(klass.getName());
        buffer.append("@");
        buffer.append(Integer.toHexString(self.hashCode()));
        while (true) {
            Field[] fields = klass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if ((field.getModifiers() & Modifier.STATIC) == 0) {
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
     * @return the String that would be printend on the console if
     * the object were displayed in interactive mode
     */
    public static String toConsoleOutput(Object self) {
        return InvokerHelper.toString(self);
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
    public static void print(Collection self, PrintWriter out) {
        out.print("[");
        boolean first = true;
        for (Iterator iter = self.iterator(); iter.hasNext(); ) {
            if (first) {
                first = false;
            }
            else {
                out.print(", ");
            }
            InvokerHelper.invokeMethod(iter.next(), "print", out);
        }
        out.print("]");
    }
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
     * Provide a dynamic method invocation method which can be overloaded in classes
     * to implement dynamic proxies easily.
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
     * A helper method to allow lists to work with subscript operators
     */
    public static void put(List self, Number n, Object value) {
        int i = n.intValue();
        int size = self.size();
        while (i < 0) {
            i += size;
        }
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
     * Allows objects to be iterated through using a closure
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
     * @param source
     * @param closure
     */
    public static void each(Map self, Closure closure) {
        for (Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
            closure.call(iter.next());
        }
    }

    /**
     * @return true if every item in the collection matches the closure predicate
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
     * Iterates through the given collection, passing in the initial
     * value to the closure along with the current iterated item
     * then passing into the next iteration the value of the previous 
     * closure.
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
     * Concatenates all of the items of the collection together with the
     * given string as a separator
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
     * Selects the maximum value found in the collection using the given comparator
     */
    public static Object max(Collection self, Comparator comparator) {
        Object answer = null;
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (comparator.compare(value, answer) > 0) {
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
     * Selects the minimum value found in the collection using the given comparator
     */
    public static Object min(Collection self, Comparator comparator) {
        Object answer = null;
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (comparator.compare(value, answer) < 0) {
                answer = value;

            }
        }
        return answer;
    }

    /**
     * Selects the minimum value found in the collection using the given closure as a comparator
     */
    public static Object min(Collection self, Closure closure) {
        return min(self, new ClosureComparator(closure));
    }

    /**
     * Selects the maximum value found in the collection using the given closure as a comparator
     */
    public static Object max(Collection self, Closure closure) {
        return max(self, new ClosureComparator(closure));
    }

    /**
     * Makes a String look like a Collection by adding support for the size() method
     * 
     * @param text
     * @return
     */
    public static int size(String text) {
        return text.length();
    }

    /**
     * Support the subscript operator for String
     * 
     * @param text
     * @return the Character object at the given index
     */
    public static Object get(String text, int index) {
        return new Character(text.charAt(index));
    }

    /**
     * Support the range subscript operator for String
     * 
     * @param text
     * @return
     */
    public static Object get(String text, Range range) {
        return text.substring(InvokerHelper.asInt(range.getFrom()), InvokerHelper.asInt(range.getTo()));
    }

    /**
     * Support the range subscript operator for a List
     * 
     * @param text
     * @return
     */
    public static Object get(List list, Range range) {
        return list.subList(InvokerHelper.asInt(range.getFrom()), InvokerHelper.asInt(range.getTo()));
    }

    /**
     * Support the subscript operator for List
     * 
     * @return
     */
    public static List get(Collection coll, String property) {
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

    public static List plus(List left, Collection right) {
        List answer = new ArrayList(left.size() + right.size());
        answer.addAll(left);
        answer.addAll(right);
        return answer;
    }

    public static List plus(List left, Object right) {
        List answer = new ArrayList(left.size() + 1);
        answer.addAll(left);
        answer.add(right);
        return answer;
    }

    public static List multiply(List self, Number factor) {
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
                boolean removeElement = false;
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

    // String methods
    //-------------------------------------------------------------------------
    public static Object tokenize(String self, String token) {
        return InvokerHelper.asList(new StringTokenizer(self, token));
    }

    public static Object tokenize(String self) {
        return InvokerHelper.asList(new StringTokenizer(self));
    }

    public static String plus(String left, Object value) {
        return left + value;
        //return left + toString(value);
    }

    public static String minus(String left, Object value) {
        String text = toString(value);
        return left.replaceFirst(text, "");
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
        for (int i = self.intValue(), size = to.intValue(); i < size; i++) {
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
     * @return
     * @throws FileNotFoundException
     */
    public static BufferedReader newReader(File file) throws FileNotFoundException {
        return new BufferedReader(new FileReader(file));
    }

    /**
      * Helper method to create a buffered writer for a file
      * 
      * @param file
      * @return
      * @throws FileNotFoundException
      */
    public static BufferedWriter newWriter(File file) throws IOException {
        return new BufferedWriter(new FileWriter(file));
    }

    // SQL based methods
    //-------------------------------------------------------------------------

    /**
     * Iterates through the result set of an SQL query passing the result set into 
     * the closure
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
}
