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
import groovy.lang.Range;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;


/**
 * This class defines all the new groovy methods which appear on normal JDK
 * classes inside the Groovy environment. Static methods are used with the 
 * first parameter the destination class.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class DefaultGroovyMethods {

    /**
     * Print to a console in interactive format
     */
    public static void print(Object self) {
        /** @todo should re-enable this */
        //InvokerHelper.invokeMethod(self, "print", new PrintWriter(System.out));
        System.out.print(InvokerHelper.toString(self));
    }

    /**
     * Print to a console in interactive format along with a newline
     */
    public static void println(Object self) {
        /** @todo should re-enable this */
        //InvokerHelper.invokeMethod(self, "println", new PrintWriter(System.out));
        System.out.println(InvokerHelper.toString(self));
    }

    /**
     * @return the String that would be printend on the console if the print() 
     * method were called
     */
    public static String toConsoleOutput(Object self) {
        StringWriter buffer = new StringWriter();
        PrintWriter out = new PrintWriter(buffer);
        InvokerHelper.invokeMethod(self, "print", out);
        return buffer.toString();
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

    // Collection based methods
    //-------------------------------------------------------------------------

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
     * Maps the values of a collection to new values using the 
     * 
     * @param source
     * @param closure
     */
    public static List collect(Object self, Closure closure) {
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
    public static List collect(Collection self, Closure closure) {
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
    public static List collect(Map self, Closure closure) {
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
     * Selects all values matching the closure condition
     * 
     * @param source
     * @param closure
     */
    public static Object select(Object self, Closure closure) {
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
     * Selects all values matching the closure condition
     * 
     * @param source
     * @param closure
     */
    public static List select(Collection self, Closure closure) {
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
     * Selects all values matching the closure condition
     * 
     * @param source
     * @param closure
     */
    public static List select(Map self, Closure closure) {
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
     * Selects the minimum value found in the collection
     * 
     * @param source
     * @param closure
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
        return text.substring(range.getFrom(), range.getTo());
    }

    /**
     * Support the range subscript operator for a List
     * 
     * @param text
     * @return
     */
    public static Object get(List list, Range range) {
        return list.subList(range.getFrom(), range.getTo());
    }

    /**
     * Support the subscript operator for List
     * 
     * @return
     */
    public static Object get(Collection coll, String property) {
        List answer = new ArrayList(coll.size());
        for (Iterator iter = coll.iterator(); iter.hasNext(); ) {
            Object item = iter.next();
            Object value = InvokerHelper.getProperty(item, property);
            answer.add(value);
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

    // Number based methods
    //-------------------------------------------------------------------------
    public static String plus(String left, Object value) {
        return left + toString(value);
    }

    public static String minus(String left, Object value) {
        String text = toString(value);
        return left.replaceFirst(text, "");
    }

    protected static String toString(Object value) {
        return (value == null) ? "null" : value.toString();
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

    // File based methods
    //-------------------------------------------------------------------------

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
    public static void query(Connection connection, TextExpression expression, Closure closure) throws SQLException {
        // lets turn the expression into an SQL string
        String sql = null;
        ResultSet results = null;
        PreparedStatement statement = connection.prepareStatement(sql);
        try {
            // lets bind the values to the statement

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
