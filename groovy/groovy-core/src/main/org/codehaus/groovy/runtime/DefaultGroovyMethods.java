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
import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.PropertyValue;
import groovy.lang.MetaProperty;
import groovy.lang.Range;
import groovy.lang.StringWriterIOException;
import groovy.lang.Writable;
import groovy.util.CharsetToolkit;
import groovy.util.ClosureComparator;
import groovy.util.OrderBy;

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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
 * @author Jeremy Rayner
 * @author Sam Pullara
 * @author Rod Cope
 * @author Guillaume Laforge
 * @author John Wilson
 * @version $Revision$
 */
public class DefaultGroovyMethods {

    private static Logger log = Logger.getLogger(DefaultGroovyMethods.class.getName());

    private static final Integer ONE = new Integer(1);
    private static final char ZERO_CHAR = '\u0000';

    /**
     * Allows the subscript operator to be used to lookup dynamic property values.
     * <code>bean[somePropertyNameExpression]</code>. The normal property notation 
     * of groovy is neater and more concise but only works with compile time known 
     * property names.
     * 
     * @param self
     * @return
     */
    public static Object getAt(Object self, String property) {
        return InvokerHelper.getProperty(self, property);
    }

    /**
     * Allows the subscript operator to be used to set dynamically named property values.
     * <code>bean[somePropertyNameExpression] = foo</code>. The normal property notation 
     * of groovy is neater and more concise but only works with compile time known 
     * property names.
     * 
     * @param self
     */
    public static void putAt(Object self, String property, Object newValue) {
        InvokerHelper.setProperty(self, property, newValue);
    }

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
		
		/*jes this may be rewritten to use the new allProperties() stuff
		 * but the original pulls out private variables, whereas allProperties()
		 * does not. What's the real use of dump() here?
		 */
	    while (klass != null) {
	        Field[] fields = klass.getDeclaredFields();
	        for (int i = 0; i < fields.length; i++) {
	            final Field field = fields[i];
	            if ((field.getModifiers() & Modifier.STATIC) == 0) {
	                if (groovyObject && field.getName().equals("metaClass")) {
	                    continue;
	                }
	    	    	AccessController.doPrivileged(new PrivilegedAction() {
	    	    		public Object run() {
	    	    			field.setAccessible(true);
	    	                return null;
	    	    		}
	    	    	});
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
	    }
		
		/* here is a different implementation that uses allProperties(). I have left
		 * it commented out because it returns a slightly different list of properties; 
		 * ie it does not return privates. I don't know what dump() really should be doing, 
		 * although IMO showing private fields is a no-no
		 */
		/*
		List props = allProperties(self);
        for(Iterator itr = props.iterator(); itr.hasNext(); ) {
            PropertyValue pv = (PropertyValue) itr.next();
			
			// the original skipped this, so I will too
			if(pv.getName().equals("metaClass")) continue;
			if(pv.getName().equals("class")) continue;
			
			buffer.append(" ");
			buffer.append(pv.getName());
			buffer.append("=");
			try {
				buffer.append(InvokerHelper.toString(pv.getValue()));
			}
			catch (Exception e) {
				buffer.append(e);
			}
        }
		*/

	    buffer.append(">");
	    return buffer.toString();
	}

    public static void eachPropertyName(Object self, Closure closure) {
        List props = allProperties(self);
        for(Iterator itr = props.iterator(); itr.hasNext(); ) {
            PropertyValue pv = (PropertyValue) itr.next();
            closure.call(pv.getName());
        }
    }

    public static void eachProperty(Object self, Closure closure) {
        List props = allProperties(self);
        for(Iterator itr = props.iterator(); itr.hasNext(); ) {
            PropertyValue pv = (PropertyValue) itr.next();
            closure.call(pv);
        }
    }
    
    public static List allProperties(Object self) {
        List props = new ArrayList();
        MetaClass metaClass = InvokerHelper.getMetaClass(self);
        
		List mps;
		
		if(self instanceof groovy.util.Expando) {
			mps = ((groovy.util.Expando) self).getProperties();
		}
		else {
			// get the MetaProperty list from the MetaClass
			mps = metaClass.getProperties();
		}
		
		for(Iterator itr = mps.iterator(); itr.hasNext();) {
			MetaProperty mp = (MetaProperty) itr.next();
			PropertyValue pv = new PropertyValue(self, mp);
			props.add(pv);
		}
        
        return props;
    }

	/**
	 * Scoped use method
	 * 
	 */
	public static void use(Object self, Class categoryClass, Closure closure) {
		GroovyCategorySupport.use(categoryClass, closure);
	}
	
	/**
	 * Scoped use method with list of categories
	 * 
	 */
	public static void use(Object self, List categoryClassList, Closure closure) {
		GroovyCategorySupport.use(categoryClassList, closure);
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
     *
     * @param out the PrintWriter used for printing
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
        Matcher matcher = caseValue.matcher(switchValue.toString());
        if (matcher.matches()) {
            RegexSupport.setLastMatcher(matcher);
            return true;
        } else {
            return false;
        }
    }

    // Collection based methods
    //-------------------------------------------------------------------------

    /**
     * Allows objects to be iterated through using a closure
     *
     * @param self    the object over which we iterate
     * @param closure the closure applied on each element found
     */
    public static void each(Object self, Closure closure) {
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            closure.call(iter.next());
        }
    }

    /**
     * Allows object to be iterated through a closure with a counter
     *
     * @param self an Object
     * @param closure a Closure
     */
    public static void eachWithIndex(Object self, Closure closure) {
        int counter = 0;
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            closure.call(new Object[] { iter.next(), new Integer(counter++) });
        }
    }

    /**
     * Allows objects to be iterated through using a closure
     *
     * @param self    the collection over which we iterate
     * @param closure the closure applied on each element of the collection
     */
    public static void each(Collection self, Closure closure) {
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            closure.call(iter.next());
        }
    }

    /**
     * Allows a Map to be iterated through using a closure. If the
     * closure takes one parameter then it will be passed the Map.Entry
     * otherwise if the closure takes two parameters then it will be
     * passed the key and the value.
     *
     * @param self    the map over which we iterate
     * @param closure the closure applied on each entry of the map
     */
    public static void each(Map self, Closure closure) {
        if (closure.getParameterTypes().length == 2) {
            for (Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                closure.call(new Object[] { entry.getKey(), entry.getValue()});
            }
        }
        else {
            for (Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
                closure.call(iter.next());
            }
        }
    }

    /**
     * Iterates over every element of a collection, and check whether a predicate is valid for all elements.
     *
     * @param self    the object over which we iterate
     * @param closure the closure predicate used for matching
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
     * Iterates over every element of a collection, and check whether a predicate is valid for at least one element
     *
     * @param self    the object over which we iterate
     * @param closure the closure predicate used for matching
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
     * Iterates over every element of the collection and return each object that matches
     * the given filter - calling the isCase() method used by switch statements.
     * This method can be used with different kinds of filters like regular expresions, classes, ranges etc.
     *
     * @param self    the object over which we iterate
     * @param filter  the filter to perform on the collection (using the isCase(object) method)
     * @return a list of objects which match the filter
     */
    public static List grep(Object self, Object filter) {
        List answer = new ArrayList();
        MetaClass metaClass = InvokerHelper.getMetaClass(filter);
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            Object object = iter.next();
            if (InvokerHelper.asBool(metaClass.invokeMethod(filter, "isCase", object))) {
                answer.add(object);
            }
        }
        return answer;
    }

    /**
     * Counts the number of occurencies of the given value inside this collection
     *
     * @param self  the collection within which we count the number of occurencies
     * @param value the value
     * @return the number of occurrencies
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
     * Convert a collection to a List.
     *
     * @param self a collection
     * @return a List
     */
    public static List toList(Collection self) {
        List answer = new ArrayList(self.size());
        answer.addAll(self);
        return answer;
    }

    /**
     * Iterates through this object transforming each object into a new value using the closure 
     * as a transformer, returning a list of transformed values.
     * 
     * @param self the values of the object to map
     * @param closure the closure used to map each element of the collection
     * @return a List of the mapped values
     */
    public static List collect(Object self, Closure closure) {
        List answer = new ArrayList();
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            answer.add(closure.call(iter.next()));
        }
        return answer;
    }

    /**
     * Iterates through this collection transforming each entry into a new value using the closure 
     * as a transformer, returning a list of transformed values.
     *
     * @param self a collection
     * @param closure the closure used for mapping
     * @return a List of the mapped values
     */
    public static List collect(Collection self, Closure closure) {
        List answer = new ArrayList(self.size());
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            answer.add(closure.call(iter.next()));
            if (closure.getDirective() == Closure.DONE) {
            	break;
            }
        }
        return answer;
    }

    /**
     * Iterates through this Map transforming each entry into a new value using the closure 
     * as a transformer, returning a list of transformed values.
     *
     * @param self a Map
     * @param closure the closure used for mapping
     * @return a List of the mapped values
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
     * @param self an Object with an iterator returning its values
     * @param closure a closure condition
     * @return the first Object found
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
     * @param self a Collection
     * @param closure a closure condition
     * @return the first Object found
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
     * @param self a Map
     * @param closure a closure condition
     * @return the first Object found
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
     * @param self an Object with an Iterator returning its values
     * @param closure a closure condition
     * @return a List of the values found
     */
    public static List findAll(Object self, Closure closure) {
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
     * @param self a Collection
     * @param closure a closure condition
     * @return a List of the values found
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
     * @param self a Map
     * @param closure a closure condition applying on the keys
     * @return a List of keys
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
     * @param self a Collection
     * @param value a value
     * @param closure a closure
     * @return the last value of the last iteration
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
     * Concatenates all of the items of the collection together with the given String as a separator
     *
     * @param self a Collection of objects
     * @param separator a String separator
     * @return the joined String
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
     * Concatenates all of the elements of the array together with the given String as a separator
     *
     * @param self an array of Object
     * @param separator a String separator
     * @return the joined String
     */
    public static String join(Object[] self, String separator) {
        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        for (int i = 0; i < self.length; i++) {
            String value = InvokerHelper.toString(self[i]);
            if (first) {
                first = false;
            }
            else {
                buffer.append(separator);
            }
            buffer.append(value);
        }
        return buffer.toString();
    }

    /**
     * Selects the maximum value found in the collection
     *
     * @param self a Collection
     * @return the maximum value
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
     *
     * @param self a Collection
     * @param comparator a Comparator
     * @return the maximum value
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
     *
     * @param self a Collection
     * @return the minimum value
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
     *
     * @param self a Collection
     * @param comparator a Comparator
     * @return the minimum value
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
     * Selects the minimum value found in the collection using the given closure as a comparator
     *
     * @param self a Collection
     * @param closure a closure used as a comparator
     * @return the minimum value
     */
    public static Object min(Collection self, Closure closure) {
        return min(self, new ClosureComparator(closure));
    }

    /**
     * Selects the maximum value found in the collection using the given closure as a comparator
     *
     * @param self a Collection
     * @param closure a closure used as a comparator
     * @return the maximum value
     */
    public static Object max(Collection self, Closure closure) {
        return max(self, new ClosureComparator(closure));
    }

    /**
     * Makes a String look like a Collection by adding support for the size() method
     *
     * @param text a String
     * @return the length of the String
     */
    public static int size(String text) {
        return text.length();
    }

    /**
     * Makes an Array look like a Collection by adding support for the size() method
     *
     * @param self an Array of Object
     * @return the size of the Array
     */
    public static int size(Object[] self) {
        return self.length;
    }

    /**
     * Support the subscript operator for String.
     *
     * @param text a String
     * @param index the index of the Character to get
     * @return the Character at the given index
     */
    public static CharSequence getAt(CharSequence text, int index) {
        index = normaliseIndex(index, text.length());
        return text.subSequence(index, index + 1);
    }

    /**
     * Support the subscript operator for String
     *
     * @param text a String
     * @return the Character object at the given index
     */
    public static String getAt(String text, int index) {
        index = normaliseIndex(index, text.length());
        return text.substring(index, index + 1);
    }

    /**
     * Support the range subscript operator for CharSequence
     *
     * @param text a CharSequence
     * @param range a Range
     * @return the subsequence CharSequence
     */
    public static CharSequence getAt(CharSequence text, Range range) {
        int from = normaliseIndex(InvokerHelper.asInt(range.getFrom()), text.length());
        int to = normaliseIndex(InvokerHelper.asInt(range.getTo()), text.length());

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
     *
     * @param text a String
     * @param range a Range
     * @return a substring corresponding to the Range
     */
    public static String getAt(String text, Range range) {
        int from = normaliseIndex(InvokerHelper.asInt(range.getFrom()), text.length());
        int to = normaliseIndex(InvokerHelper.asInt(range.getTo()), text.length());

        // if this is a backwards range, reverse the arguments to substring
        boolean reverse = range.isReverse();
        if (from > to) {
            int tmp = to;
            to = from;
            from = tmp;
            reverse = !reverse;
        }

        String answer = text.substring(from, to + 1);
        if (reverse) {
            answer = reverse(answer);
        }
        return answer;
    }

    /**
     * Creates a new string which is the reverse (backwards) of this string
     *
     * @param self a String
     * @return a new string with all the characters reversed.
     */
    public static String reverse(String self) {
        int size = self.length();
        StringBuffer buffer = new StringBuffer(size);
        for (int i = size - 1; i >= 0; i--) {
            buffer.append(self.charAt(i));
        }
        return buffer.toString();
    }

    /**
     * Transforms a String representing a URL into a URL object.
     *
     * @param self the String representing a URL
     * @return a URL
     * @throws MalformedURLException is thrown if the URL is not well formed.
     */
    public static URL toURL(String self) throws MalformedURLException {
        return new URL(self);
    }

    private static String getPadding(String padding, int length) {
        if (padding.length() < length) {
            return multiply(padding, new Integer(length / padding.length() + 1)).substring(0, length);
        } else {
            return padding.substring(0, length);
        }
    }

    /**
     * Pad a String with the characters appended to the left
     *
     * @param numberOfChars the total number of characters
     * @param padding the charaters used for padding
     * @return the String padded to the left
     */
    public static String padLeft(String self, Number numberOfChars, String padding) {
        int numChars = numberOfChars.intValue();
        if (numChars <= self.length()) {
            return self;
        } else {
            return getPadding(padding, numChars - self.length()) + self;
        }
    }

    /**
     * Pad a String with the spaces appended to the left
     *
     * @param numberOfChars the total number of characters
     * @return the String padded to the left
     */

    public static String padLeft(String self, Number numberOfChars) {
        return padLeft(self, numberOfChars, " ");
    }

    /**
     * Pad a String with the characters appended to the right
     *
     * @param numberOfChars the total number of characters
     * @param padding the charaters used for padding
     * @return the String padded to the right
     */

    public static String padRight(String self, Number numberOfChars, String padding) {
        int numChars = numberOfChars.intValue();
        if (numChars <= self.length()) {
            return self;
        }
        else {
            return self + getPadding(padding, numChars - self.length());
        }
    }

    /**
     * Pad a String with the spaces appended to the right
     *
     * @param numberOfChars the total number of characters
     * @return the String padded to the right
     */

    public static String padRight(String self, Number numberOfChars) {
        return padRight(self, numberOfChars, " ");
    }

    /**
     * Center a String and padd it with the characters appended around it
     *
     * @param numberOfChars the total number of characters
     * @param padding the charaters used for padding
     * @return the String centered with padded character around
     */
    public static String center(String self, Number numberOfChars, String padding) {
        int numChars = numberOfChars.intValue();
        if (numChars <= self.length()) {
            return self;
        }
        else {
            int charsToAdd = numChars - self.length();
            String semiPad = charsToAdd % 2 == 1 ?
                getPadding(padding, charsToAdd / 2 + 1) :
                getPadding(padding, charsToAdd / 2);
            if (charsToAdd % 2 == 0)
                return semiPad + self + semiPad;
            else
                return semiPad.substring(0, charsToAdd / 2) + self + semiPad;
        }
    }

    /**
     * Center a String and padd it with spaces appended around it
     *
     * @param numberOfChars the total number of characters
     * @return the String centered with padded character around
     */
    public static String center(String self, Number numberOfChars) {
        return center(self, numberOfChars, " ");
    }

    /**
     * Support the subscript operator for a regex Matcher
     *
     * @param matcher a Matcher
     * @param idx an index
     * @return the group at the given index
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
     * @param self a List
     * @param range a Range
     * @return a range of a list from the range's from index up to but not including the ranges's to value
     */
    public static List getAt(List self, Range range) {
        int size = self.size();
        int from = normaliseIndex(InvokerHelper.asInt(range.getFrom()), size);
        int to = normaliseIndex(InvokerHelper.asInt(range.getTo()), size);
        boolean reverse = range.isReverse();
        if (from > to) {
            int tmp = to;
            to = from;
            from = tmp;
            reverse = !reverse;
        }
        if (++to > size) {
            to = size;
        }
        List answer = self.subList(from, to);
        if (reverse) {
            answer = reverse(answer);
        }
        return answer;
    }

    /**
     * Allows a List to be used as the indices to be used on a List
     *
     * @param self a List
     * @param indices a Collection of indices
     * @return a new list of the values at the given indices
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
     * @param self an Array of Objects
     * @param indices a Collection of indices
     * @return a new list of the values at the given indices
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
     * @param self a CharSequence
     * @param indices a Collection of indices
     * @return a String of the values at the given indices
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
     * @param self a String
     * @param indices a Collection of indices
     * @return a String of the values at the given indices
     */
    public static String getAt(String self, Collection indices) {
        return (String) getAt((CharSequence) self, indices);
    }

    /**
     * Allows a List to be used as the indices to be used on a Matcher
     *
     * @param self a Matcher
     * @param indices a Collection of indices
     * @return a String of the values at the given indices
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
     * @param map a Map
     * @param keys a Collection of keys
     * @return a new Map containing the given keys
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
     * Looks up an item in a Map for the given key and returns the value - unless
     * there is no entry for the given key in which case add the default value
     * to the map and return that.
     *
     * @param map a Map
     * @param key the key to lookup the value of
     * @param defaultValue the value to return and add to the map for this key if 
     * there is no entry for the given key
     * @return the value of the given key or the default value, added to the map if the
     * key did not exist
     */
    public static Object get(Map map, Object key, Object defaultValue) {
        Object answer = map.get(key);
        if (answer == null) {
            answer = defaultValue;
            map.put(key, answer);
        }
        return answer;
    }

    /**
     * Support the range subscript operator for an Array
     *
     * @param array an Array of Objects
     * @param range a Range
     * @return a range of a list from the range's from index up to but not
     * including the ranges's to value
     */
    public static List getAt(Object[] array, Range range) {
        List list = Arrays.asList(array);
        return getAt(list, range);
    }

    /**
     * Support the subscript operator for an Array
     *
     * @param array an Array of Objects
     * @param idx an index
     * @return the value at the given index
     */
    public static Object getAt(Object[] array, int idx) {
        return array[normaliseIndex(idx, array.length)];
    }

    /**
     * Support the subscript operator for an Array
     *
     * @param array an Array of Objects
     * @param idx an index
     * @param value an Object to put at the given index
     */
    public static void putAt(Object[] array, int idx, Object value) {
        if(value instanceof Number) {
            Class arrayComponentClass = array.getClass().getComponentType();
            
            if(!arrayComponentClass.equals(value.getClass())) {
                Object newVal = InvokerHelper.asType(value, arrayComponentClass);
                array[normaliseIndex(idx, array.length)] = newVal;
                return; 
            }
        }
        array[normaliseIndex(idx, array.length)] = value;
    }

    /**
     * Allows conversion of arrays into a mutable List
     *
     * @param array an Array of Objects
     * @return the array as a List
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
     * @param self a List
     * @param idx an index
     * @return the value at the given index
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
     *
     * @param self a List
     * @param idx an index
     * @param value the value to put at the given index
     */
    public static void putAt(List self, int idx, Object value) {
        int size = self.size();
        idx = normaliseIndex(idx, size);
        if (idx < size) {
            self.set(idx, value);
        }
        else {
            while (size < idx) {
                self.add(size++, null);
            }
            self.add(idx, value);
        }
    }

    /**
     * Support the subscript operator for a List
     *
     * @param self a Map
     * @param key an Object as a key for the map
     * @return the value corresponding to the given key
     */
    public static Object getAt(Map self, Object key) {
        return self.get(key);
    }

    /**
     * A helper method to allow lists to work with subscript operators
     *
     * @param self a Map
     * @param key an Object as a key for the map
     * @return the value corresponding to the given key
     */
    public static Object putAt(Map self, Object key, Object value) {
        return self.put(key, value);
    }

    /**
     * This converts a possibly negative index to a real index into the array.
     *
     * @param i
     * @param size
     * @return
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
     * Support the subscript operator for List
     *
     * @param coll a Collection
     * @param property a String
     * @return a List
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
     *
     * @param self a Map
     * @return an immutable Map
     */
    public static Map asImmutable(Map self) {
        return Collections.unmodifiableMap(self);
    }

    /**
     * A convenience method for creating an immutable sorted map
     *
     * @param self a SortedMap
     * @return an immutable SortedMap
     */
    public static SortedMap asImmutable(SortedMap self) {
        return Collections.unmodifiableSortedMap(self);
    }

    /**
     * A convenience method for creating an immutable list
     *
     * @param self a List
     * @return an immutable List
     */
    public static List asImmutable(List self) {
        return Collections.unmodifiableList(self);
    }

    /**
     * A convenience method for creating an immutable list
     *
     * @param self a Set
     * @return an immutable Set
     */
    public static Set asImmutable(Set self) {
        return Collections.unmodifiableSet(self);
    }

    /**
     * A convenience method for creating an immutable sorted set
     *
     * @param self a SortedSet
     * @return an immutable SortedSet
     */
    public static SortedSet asImmutable(SortedSet self) {
        return Collections.unmodifiableSortedSet(self);
    }

    /**
     * A convenience method for creating an immutable Collection
     *
     * @param self a Collection
     * @return an immutable Collection
     */
    public static Collection asImmutable(Collection self) {
        return Collections.unmodifiableCollection(self);
    }

    /**
     * A convenience method for creating a synchronized Map
     *
     * @param self a Map
     * @return a synchronized Map
     */
    public static Map asSynchronized(Map self) {
        return Collections.synchronizedMap(self);
    }

    /**
     * A convenience method for creating a synchronized SortedMap
     *
     * @param self a SortedMap
     * @return a synchronized SortedMap
     */
    public static SortedMap asSynchronized(SortedMap self) {
        return Collections.synchronizedSortedMap(self);
    }

    /**
     * A convenience method for creating a synchronized Collection
     *
     * @param self a Collection
     * @return a synchronized Collection
     */
    public static Collection asSynchronized(Collection self) {
        return Collections.synchronizedCollection(self);
    }

    /**
     * A convenience method for creating a synchronized List
     *
     * @param self a List
     * @return a synchronized List
     */
    public static List asSynchronized(List self) {
        return Collections.synchronizedList(self);
    }

    /**
     * A convenience method for creating a synchronized Set
     *
     * @param self a Set
     * @return a synchronized Set
     */
    public static Set asSynchronized(Set self) {
        return Collections.synchronizedSet(self);
    }

    /**
     * A convenience method for creating a synchronized SortedSet
     *
     * @param self a SortedSet
     * @return a synchronized SortedSet
     */
    public static SortedSet asSynchronized(SortedSet self) {
        return Collections.synchronizedSortedSet(self);
    }

    /**
     * Sorts the given collection into a sorted list
     *
     * @param self the collection to be sorted
     * @return the sorted collection as a List
     */
    public static List sort(Collection self) {
        List answer = asList(self);
        Collections.sort(answer);
        return answer;
    }

    /**
     * Avoids doing unnecessary work when sorting an already sorted set
     *
     * @param self 
     * @return the sorted set
     */
    public static SortedSet sort(SortedSet self) {
        return self;
    }

    /**
     * A convenience method for sorting a List
     *
     * @param self a List to be sorted
     * @return the sorted List
     */
    public static List sort(List self) {
        Collections.sort(self);
        return self;
    }

    /**
     * Removes the last item from the List. Using add() and pop()
     * is similar to push and pop on a Stack.
     *
     * @param self a List
     * @return the item removed from the List
     * @throws UnsupportedOperationException if the list is empty and you try to pop() it.
     */
    public static Object pop(List self) {
        if (self.isEmpty()) {
            throw new UnsupportedOperationException("Cannot pop() an empty List");
        }
        return self.remove(self.size() - 1);
    }

    /**
     * A convenience method for sorting a List with a specific comparator
     *
     * @param self a List
     * @param comparator a Comparator used for the comparison
     * @return a sorted List
     */
    public static List sort(List self, Comparator comparator) {
        Collections.sort(self, comparator);
        return self;
    }

    /**
     * A convenience method for sorting a Collection with a specific comparator
     *
     * @param self a collection to be sorted
     * @param comparator a Comparator used for the comparison
     * @return a newly created sorted List
     */
    public static List sort(Collection self, Comparator comparator) {
        return sort(asList(self), comparator);
    }

    /**
     * A convenience method for sorting a List using a closure as a comparator
     *
     * @param self a List
     * @param closure a Closure used as a comparator
     * @return a sorted List
     */
    public static List sort(List self, Closure closure) {
        // use a comparator of one item or two
        Class[] params = closure.getParameterTypes();
        if (params.length == 1) {
            Collections.sort(self, new OrderBy(closure));
        }
        else {
            Collections.sort(self, new ClosureComparator(closure));
        }
        return self;
    }

    /**
     * A convenience method for sorting a Collection using a closure as a comparator
     *
     * @param self a Collection to be sorted
     * @param closure a Closure used as a comparator
     * @return a newly created sorted List
     */
    public static List sort(Collection self, Closure closure) {
        return sort(asList(self), closure);
    }

    /**
     * Converts the given collection into a List
     * 
     * @param self a collection to be converted into a List
     * @return a newly created List if this collection is not already a List
     */
    public static List asList(Collection self) {
        if (self instanceof List) {
            return (List) self;
        }
        else {
            return new ArrayList(self);
        }
    }

    /**
     * Reverses the list
     *
     * @param self a List
     * @return a reversed List
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

    /**
     * Create a List as a union of both Collections
     *
     * @param left the left Collection
     * @param right the right Collection
     * @return a List
     */
    public static List plus(Collection left, Collection right) {
        List answer = new ArrayList(left.size() + right.size());
        answer.addAll(left);
        answer.addAll(right);
        return answer;
    }

    /**
     * Create a List as a union of a Collection and an Object
     *
     * @param left a Collection
     * @param right an object to append
     * @return a List
     */
    public static List plus(Collection left, Object right) {
        List answer = new ArrayList(left.size() + 1);
        answer.addAll(left);
        answer.add(right);
        return answer;
    }

    /**
     * Create a List composed of the same elements repeated a certain number of times.
     *
     * @param self a Collection
     * @param factor the number of times to append
     * @return a List
     */
    public static List multiply(Collection self, Number factor) {
        int size = factor.intValue();
        List answer = new ArrayList(self.size() * size);
        for (int i = 0; i < size; i++) {
            answer.addAll(self);
        }
        return answer;
    }

    /**
     * Create a List composed of the intersection of both collections
     *
     * @param left a List
     * @param right a Collection
     * @return a List as an intersection of both collections
     */
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

    /**
     * Create a List composed of the elements of the first list minus the elements of the collection
     *
     * @param self a List
     * @param removeMe a Collection of elements to remove
     * @return a List with the common elements removed
     */
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
            Object[] array = tmpAnswer.toArray(new Object[tmpAnswer.size()]);

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

    /**
     * Flatten a list
     *
     * @param self a List
     * @return a flattened List
     */
    public static List flatten(List self) {
        return new ArrayList(flatten(self, new LinkedList()));
    }

    /**
     * Iterate over each element of the list in the reverse order.
     *
     * @param self a List
     * @param closure a closure
     */
    public static void reverseEach(List self, Closure closure) {
        List reversed = reverse(self);
        for (Iterator iter = reversed.iterator(); iter.hasNext();) {
            closure.call(iter.next());
        }
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
     * Overloads the left shift operator to provide an easy way to append objects to a list
     *
     * @param self a Collection
     * @param value an Object to be added to the collection.
     * @return a Collection with an Object added to it.
     */
    public static Collection leftShift(Collection self, Object value) {
        self.add(value);
        return self;
    }

    /**
     * Overloads the left shift operator to provide an easy way to append multiple
     * objects as string representations to a String
     *
     * @param self a String
     * @param value an Obect
     * @return a StringWriter
     */
    public static StringWriter leftShift(String self, Object value) {
        StringWriter answer = createStringWriter(self);
        try {
            leftShift(answer, value);
        }
        catch (IOException e) {
            throw new StringWriterIOException(e);
        }
        return answer;
    }

    protected static StringWriter createStringWriter(String self) {
        StringWriter answer = new StringWriter();
        answer.write(self);
        return answer;
    }

    protected static StringBufferWriter createStringBufferWriter(StringBuffer self) {
        return new StringBufferWriter(self);
    }

    /**
     * Overloads the left shift operator to provide an easy way to append multiple
     * objects as string representations to a StringBuffer
     *
     * @param self a StringBuffer
     * @param value a value to append
     * @return a StringWriter
     */
    public static Writer leftShift(StringBuffer self, Object value) {
        StringBufferWriter answer = createStringBufferWriter(self);
        try {
            leftShift(answer, value);
        }
        catch (IOException e) {
            throw new StringWriterIOException(e);
        }
        return answer;
    }

    /**
     * Overloads the left shift operator to provide an append mechanism to add things to a writer
     *
     * @param self a Writer
     * @param value a value to append
     * @return a StringWriter
     */
    public static Writer leftShift(Writer self, Object value) throws IOException {
        InvokerHelper.write(self, value);
        return self;
    }

    /**
     * Implementation of the left shift operator for integral types.  Non integral
     * Number types throw UnsupportedOperationException.
     */
    public static Number leftShift(Number left, Number right) {
    	return NumberMath.leftShift(left, right);
    }
    
    /**
     * Implementation of the right shift operator for integral types.  Non integral
     * Number types throw UnsupportedOperationException.
     */
    public static Number rightShift(Number left, Number right) {
    	return NumberMath.rightShift(left, right);
    }
    
    /**
     * Implementation of the right shift (unsigned) operator for integral types.  Non integral
     * Number types throw UnsupportedOperationException.
     */
    public static Number rightShiftUnsigned(Number left, Number right) {
    	return NumberMath.rightShiftUnsigned(left, right);
    }
    
    /**
     * A helper method so that dynamic dispatch of the writer.write(object) method
     * will always use the more efficient Writable.writeTo(writer) mechanism if the
     * object implements the Writable interface.
     *
     * @param self a Writer
     * @param writable an object implementing the Writable interface
     */
    public static void write(Writer self, Writable writable) throws IOException {
        writable.writeTo(self);
    }

    /**
     * Overloads the left shift operator to provide an append mechanism to add things to a stream
     *
     * @param self an OutputStream
     * @param value a value to append
     * @return a Writer
     */
    public static Writer leftShift(OutputStream self, Object value) throws IOException {
        OutputStreamWriter writer = new FlushingStreamWriter(self);
        leftShift(writer, value);
        return writer;
    }
    
    /**
     * Overloads the left shift operator to provide an append mechanism to add bytes to a stream
     *
     * @param self an OutputStream
     * @param value a value to append
     * @return an OutputStream
     */
    public static OutputStream leftShift(OutputStream self, byte[] value) throws IOException {
        self.write(value);
        self.flush();
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
    
	private static final char[] tTable = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();
    
    public static Writable encodeBase64(final Byte[] data) {
        return encodeBase64(InvokerHelper.convertToByteArray(data));
    }
    /**
     * Produce a Writable object which writes the base64 encoding of the byte array
     * Calling toString() on the result rerurns the encoding as a String
     * 
     * @param data byte array to be encoded
     * @return object which will write the base64 encoding of the byte array
     */
    public static Writable encodeBase64(final byte[] data) {
		return new Writable() {
			public Writer writeTo(final Writer writer) throws IOException {
				int charCount = 0;
				final int dLimit = (data.length / 3) * 3;

				for (int dIndex = 0; dIndex != dLimit; dIndex += 3) {
					int d = ((data[dIndex] & 0XFF) << 16) |  ((data[dIndex + 1] & 0XFF) << 8) | (data[dIndex + 2] & 0XFF);

					writer.write(tTable[d >> 18]);
					writer.write(tTable[(d >> 12) & 0X3F]);
					writer.write(tTable[(d >> 6) & 0X3F]);
					writer.write(tTable[d & 0X3F]);

					if (++charCount == 18) {
						writer.write('\n');
						charCount = 0;
					}
				}

				if (dLimit != data.length) {
					int d = (data[dLimit] & 0XFF) << 16;

					if (dLimit + 1 != data.length) {
						d |= (data[dLimit + 1] & 0XFF) << 8;
					}

					writer.write(tTable[d >> 18]);
					writer.write(tTable[(d >> 12) & 0X3F]);
					writer.write((dLimit + 1 < data.length) ? tTable[(d >> 6) & 0X3F] : '=');
					writer.write('=');
				}
				
				return writer;
			}
			
			public String toString() {
		        StringWriter buffer = new StringWriter();
		        
		        try {
		            writeTo(buffer);
		        }
		        catch (IOException e) {
		            throw new RuntimeException(e); // TODO: change this exception type
		        }
		        
		        return buffer.toString();
			}
		};
    }
    
	private static final byte[] translateTable = (
			//
			"\u0042\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
			//                    \t    \n                \r
			+ "\u0042\u0042\u0041\u0041\u0042\u0042\u0041\u0042"
			//
			+ "\u0042\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
			//
			+ "\u0042\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
			//        sp    !     "     #     $     %     &     '
			+ "\u0041\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
			//         (    )     *     +     ,     -     .     /
			+ "\u0042\u0042\u0042\u003E\u0042\u0042\u0042\u003F"
			//         0    1     2     3     4     5     6     7
			+ "\u0034\u0035\u0036\u0037\u0038\u0039\u003A\u003B"
			//         8    9     :     ;     <     =     >     ?
			+ "\u003C\u003D\u0042\u0042\u0042\u0040\u0042\u0042"
			//         @    A     B     C     D     E     F     G
			+ "\u0042\u0000\u0001\u0002\u0003\u0004\u0005\u0006"
			//         H    I   J K   L     M   N   O
			+ "\u0007\u0008\t\n\u000B\u000C\r\u000E"
			//         P    Q     R     S     T     U     V    W
			+ "\u000F\u0010\u0011\u0012\u0013\u0014\u0015\u0016"
			//         X    Y     Z     [     \     ]     ^    _
			+ "\u0017\u0018\u0019\u0042\u0042\u0042\u0042\u0042"
			//         '    a     b     c     d     e     f     g
			+ "\u0042\u001A\u001B\u001C\u001D\u001E\u001F\u0020"
			//        h   i   j     k     l     m     n     o    p
			+ "\u0021\"\u0023\u0024\u0025\u0026\u0027\u0028"
			//        p     q     r     s     t     u     v     w
			+ "\u0029\u002A\u002B\u002C\u002D\u002E\u002F\u0030"
			//        x     y     z
			+ "\u0031\u0032\u0033").getBytes();
   
    /**
     * 
     * Decode the Sting from base64 into a byte array
     * 
     * @param value the string to be decoded
     * @return the decoded bytes as an array
     */
    public static byte[] decodeBase64(final String value) {
		int byteShift = 4;
		int tmp = 0;
		boolean done = false;
		final StringBuffer buffer = new StringBuffer();
		
		for (int i = 0; i != value.length(); i++) {
		final char c = value.charAt(i);
		final int sixBit = (c < 123) ? translateTable[c] : 66;

			if (sixBit < 64) {
				if (done) throw new RuntimeException("= character not at end of base64 value"); // TODO: change this exception type

				tmp = (tmp << 6) | sixBit;

				if (byteShift-- != 4) {
					buffer.append((char)((tmp >> (byteShift * 2)) & 0XFF));
				}

			} else if (sixBit == 64) {

				byteShift--;
				done = true;

			} else if (sixBit == 66) {
				// RFC 2045 says that I'm allowed to take the presence of 
				// these characters as evedence of data corruption
				// So I will
				throw new RuntimeException("bad character in base64 value"); // TODO: change this exception type
			}

			if (byteShift == 0) byteShift = 4;
		}

		try {
			return buffer.toString().getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Base 64 decode produced byte values > 255"); // TODO: change this exception type
		}
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

    /**
     * Converts the given string into a Character object
     * using the first character in the string
     *
     * @param self a String
     * @return the first Character
     */
    public static Character toCharacter(String self) {
        /** @todo use cache? */
        return new Character(self.charAt(0));
    }

    /**
     * Tokenize a String
     *
     * @param self a String
     * @param token the delimiter
     * @return a List of tokens
     */
    public static List tokenize(String self, String token) {
        return InvokerHelper.asList(new StringTokenizer(self, token));
    }

    /**
     * Tokenize a String (with a whitespace as delimiter)
     *
     * @param self a String
     * @return a List of tokens
     */
    public static List tokenize(String self) {
        return InvokerHelper.asList(new StringTokenizer(self));
    }

    /**
     * Appends a String
     *
     * @param left a String
     * @param value a String
     * @return a String
     */
    public static String plus(String left, Object value) {
        //return left + value;
        return left + toString(value);
    }

    /**
     * Appends a String
     *
     * @param value a Number
     * @param right a String
     * @return a String
     */
    public static String plus(Number value, String right) {
        return toString(value) + right;
    }

    /**
     * Remove a part of a String
     *
     * @param left a String
     * @param value a String part to remove
     * @return a String minus the part to be removed
     */
    public static String minus(String left, Object value) {
        String text = toString(value);
        return left.replaceFirst(text, "");
    }

    /**
     * Provide an implementation of contains() like Collection to make Strings more polymorphic
     * This method is not required on JDK 1.5 onwards
     *
     * @param self a String
     * @param text a String to look for
     * @return true if this string contains the given text
     */
    public static boolean contains(String self, String text) {
        int idx = self.indexOf(text);
        return idx >= 0;
    }

    /**
     * Count the number of occurencies of a substring
     *
     * @param self a String
     * @param text a substring
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
     * @param self a String
     * @return a String with an incremented digit at the end
     */
    public static String next(String self) {
        StringBuffer buffer = new StringBuffer(self);
        char firstCh = firstCharacter();
        for (int idx = buffer.length() - 1; idx >= 0; idx--) {
            char ch = next(buffer.charAt(idx));
            if (ch != ZERO_CHAR) {
                buffer.setCharAt(idx, ch);
                break;
            }
            else {
                // lets find the first char
                if (idx == 0) {
                    buffer.append("1");
                }
                else {
                    buffer.setCharAt(idx, firstCh);
                }
            }
        }
        return buffer.toString();
    }

    /**
     * Decrements the last digit in the given string, resetting
     * it and moving onto the next digit if increasing the digit
     * no longer becomes a letter or digit.
     *
     * @param self a String
     * @return a String with a decremented digit at the end
     */
    public static String previous(String self) {
        StringBuffer buffer = new StringBuffer(self);
        char lastCh = lastCharacter();
        for (int idx = buffer.length() - 1; idx >= 0; idx--) {
            char ch = previous(buffer.charAt(idx));
            if (ch != ZERO_CHAR) {
                buffer.setCharAt(idx, ch);
                break;
            }
            else {
                if (idx == 0) {
                    return null;
                }
                else {
                    // lets find the first char
                    buffer.setCharAt(idx, lastCh);
                }
            }
        }
        return buffer.toString();
    }

    /**
     * Executes the given string as a command line process. For more control
     * over the process mechanism in JDK 1.5 you can use java.lang.ProcessBuilder.
     *
     * @param self a command line String
     * @return the Process which has just started for this command line string
     */
    public static Process execute(String self) throws IOException {
        return Runtime.getRuntime().exec(self);
    }

    private static char next(char ch) {
        if (Character.isLetterOrDigit(++ch)) {
            return ch;
        }
        else {
            return ZERO_CHAR;
        }
    }

    private static char previous(char ch) {
        if (Character.isLetterOrDigit(--ch)) {
            return ch;
        }
        else {
            return ZERO_CHAR;
        }
    }

    /**
     * @return the first character used when a letter rolls over when incrementing
     */
    private static char firstCharacter() {
        char ch = ZERO_CHAR;
        while (!Character.isLetterOrDigit(ch)) {
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

    /**
     * Repeat a String a certain number of times
     *
     * @param self a String to be repeated
     * @param factor the number of times the String should be repeated
     * @return a String composed of a repeatition
     * @throws IllegalArgumentException if the number of repeatition is &lt; 0
     */
    public static String multiply(String self, Number factor) {
        int size = factor.intValue();
        if (size == 0)
            return "";
        else if (size < 0) {
            throw new IllegalArgumentException(
                "multiply() should be called with a number of 0 or greater not: " + size);
        }
        StringBuffer answer = new StringBuffer(self);
        for (int i = 1; i < size; i++) {
            answer.append(self);
        }
        return answer.toString();
    }

    protected static String toString(Object value) {
        return (value == null) ? "null" : value.toString();
    }

    // Number based methods
    //-------------------------------------------------------------------------

    /**
     * Increment a Character by one
     *
     * @param self a Character
     * @return an incremented Number
     */
    public static Number next(Character self) {
        return plus(self, ONE);
    }

    /**
     * Increment a Number by one
     *
     * @param self a Number
     * @return an incremented Number
     */
    public static Number next(Number self) {
        return plus(self, ONE);
    }

    /**
     * Decrement a Character by one
     *
     * @param self a Character
     * @return a decremented Number
     */
    public static Number previous(Character self) {
        return minus(self, ONE);
    }

    /**
     * Decrement a Number by one
     *
     * @param self a Number
     * @return a decremented Number
     */
    public static Number previous(Number self) {
        return minus(self, ONE);
    }

    /**
     * Add a Character and a Number
     * 
     * @param left a Character
     * @param right a Number
     * @return the addition of the Character and the Number
     */
    public static Number plus(Character left, Number right) {
        return plus(new Integer(left.charValue()), right);
    }

    /**
     * Add a Number and a Character
     * 
     * @param left a Number
     * @param right a Character
     * @return the addition of the Character and the Number
     */
    public static Number plus(Number left, Character right) {
        return plus(left, new Integer(right.charValue()));
    }

    /**
     * Add two Characters
     * 
     * @param left a Character
     * @param right a Character
     * @return the addition of both Characters
     */
    public static Number plus(Character left, Character right) {
        return plus(new Integer(left.charValue()), right);
    }

    /**
     * Add two Numbers
     *
     * @param left a Number
     * @param right another Number to add
     * @return the addition of both Numbers
     */
    public static Number plus(Number left, Number right) {
        return NumberMath.add(left, right);
    }

    /**
     * Compare a Character and a Number
     * 
     * @param left a Character
     * @param right a Number
     * @return the result of the comparison
     */
    public static int compareTo(Character left, Number right) {
        return compareTo(new Integer(left.charValue()), right);
    }

    /**
     * Compare a Number and a Character
     * 
     * @param left a Number
     * @param right a Character
     * @return the result of the comparison
     */
    public static int compareTo(Number left, Character right) {
        return compareTo(left, new Integer(right.charValue()));
    }

    /**
     * Compare two Characters
     * 
     * @param left a Character
     * @param right a Character
     * @return the result of the comparison
     */
    public static int compareTo(Character left, Character right) {
        return compareTo(new Integer(left.charValue()), right);
    }

    /**
     * Compare two Numbers
     *
     * @param left a Number
     * @param right another Number to compare to
     * @return the comparision of both numbers
     */
    public static int compareTo(Number left, Number right) {
        /** @todo maybe a double dispatch thing to handle new large numbers? */
        return NumberMath.compareTo(left, right);
    }

    /**
     * Subtract a Number from a Character
     * 
     * @param left a Character
     * @param right a Number
     * @return the addition of the Character and the Number
     */
    public static Number minus(Character left, Number right) {
        return minus(new Integer(left.charValue()), right);
    }

    /**
     * Subtract a Character from a Number
     * 
     * @param left a Number
     * @param right a Character
     * @return the addition of the Character and the Number
     */
    public static Number minus(Number left, Character right) {
        return minus(left, new Integer(right.charValue()));
    }

    /**
     * Subtraction two Characters
     * 
     * @param left a Character
     * @param right a Character
     * @return the addition of both Characters
     */
    public static Number minus(Character left, Character right) {
        return minus(new Integer(left.charValue()), right);
    }

    /**
     * Substraction of two Numbers
     *
     * @param left a Number
     * @param right another Number to substract to the first one
     * @return the substraction
     */
    public static Number minus(Number left, Number right) {
    	return NumberMath.subtract(left,right);
    }

    /**
     * Multiply a Character by a Number
     *
     * @param left a Character
     * @param right a Number
     * @return the multiplication of both
     */
    public static Number multiply(Character left, Number right) {
        return multiply(new Integer(left.charValue()), right);
    }

    /**
     * Multiply a Number by a Character
     *
     * @param left a Number
     * @param right a Character
     * @return the multiplication of both
     */
    public static Number multiply(Number left, Character right) {
        return multiply(left, new Integer(right.charValue()));
    }

    /**
     * Multiply two Characters
     *
     * @param left a Character
     * @param right another Character
     * @return the multiplication of both
     */
    public static Number multiply(Character left, Character right) {
        return multiply(new Integer(left.charValue()), right);
    }

    /**
     * Multiply two Numbers
     *
     * @param left a Number
     * @param right another Number
     * @return the multiplication of both
     */
	//Note:  This method is NOT called if left AND right are both BigIntegers or BigDecimals because
	//those classes implement a method with a better exact match.
    public static Number multiply(Number left, Number right) {
    	return NumberMath.multiply(left, right);
    }

    /**
     * Power of a Number to a certain exponent
     *
     * @param self a Number
     * @param exponent a Number exponent
     * @return a Number to the power of a certain exponent
     */
    public static Number power(Number self, Number exponent) {
		double answer = Math.pow(self.doubleValue(), exponent.doubleValue());
		if (NumberMath.isFloatingPoint(self) || NumberMath.isFloatingPoint(exponent) || answer < 1) {
			return new Double(answer);
		}
		else if (NumberMath.isLong(self) || NumberMath.isLong(exponent) || answer > Integer.MAX_VALUE) {
			return new Long((long) answer);
		}
		else {
			return new Integer((int) answer);
		}
    }

    /**
     * Divide a Character by a Number
     *
     * @param left a Character
     * @param right a Number
     * @return the multiplication of both
     */
    public static Number div(Character left, Number right) {
        return div(new Integer(left.charValue()), right);
    }

    /**
     * Divide a Number by a Character
     *
     * @param left a Number
     * @param right a Character
     * @return the multiplication of both
     */
    public static Number div(Number left, Character right) {
        return div(left, new Integer(right.charValue()));
    }

    /**
     * Divide two Characters
     *
     * @param left a Character
     * @param right another Character
     * @return the multiplication of both
     */
    public static Number div(Character left, Character right) {
        return div(new Integer(left.charValue()), right);
    }

    /**
     * Divide two Numbers
     * 
     * @param left a Number
     * @param right another Number
     * @return a Number resulting of the divide operation
     */
	//Method name changed from 'divide' to avoid collision with BigInteger method that has
	//different semantics.  We want a BigDecimal result rather than a BigInteger.
    public static Number div(Number left, Number right) {
    	return NumberMath.divide(left, right);
    }
    
    /**
     * Integer Divide a Character by a Number
     *
     * @param left a Character
     * @param right a Number
     * @return the integer division of both
     */
    public static Number intdiv(Character left, Number right) {
        return intdiv(new Integer(left.charValue()), right);
    }

    /**
     * Integer Divide a Number by a Character
     *
     * @param left a Number
     * @param right a Character
     * @return the integer division of both
     */
    public static Number intdiv(Number left, Character right) {
        return intdiv(left, new Integer(right.charValue()));
    }

    /**
     * Integer Divide two Characters
     *
     * @param left a Character
     * @param right another Character
     * @return the integer division of both
     */
    public static Number intdiv(Character left, Character right) {
        return intdiv(new Integer(left.charValue()), right);
    }

    /**
     * Integer Divide two Numbers
     * 
     * @param left a Number
     * @param right another Number
     * @return a Number (an Integer) resulting of the integer division operation
     */
    public static Number intdiv(Number left, Number right) {
    	return NumberMath.intdiv(left, right);
    }
    
    /**
     * Bitwise OR together two numbers
     *
     * @param left a Number
     * @param right another Number to bitwise OR
     * @return the bitwise OR of both Numbers
     */
    public static Number or(Number left, Number right) {
        return NumberMath.or(left, right);
    }
    
    /**
     * Bitwise AND together two Numbers
     *
     * @param left a Number
     * @param right another Number to bitwse AND
     * @return the bitwise AND of both Numbers
     */
    public static Number and(Number left, Number right) {
        return NumberMath.and(left, right);
    }
    
    /**
     * Performs a division modulus operation
     *
     * @param left a Number
     * @param right another Number to mod
     * @return the modulus result
     */
    public static Number mod(Number left, Number right) {
        return NumberMath.mod(left, right);
    }
    
    /**
     * Negates the number
     *
     * @param left a Number
     * @return the negation of the number
     */
    public static Number negate(Number left) {
        return NumberMath.negate(left);
    }


    /**
     * Iterates a number of times
     *
     * @param self a Number
     * @param closure the closure to call a number of times
     */
    public static void times(Number self, Closure closure) {
        for (int i = 0, size = self.intValue(); i < size; i++) {
            closure.call(new Integer(i));
            if (closure.getDirective() == Closure.DONE){
            	break;
            }
        }
    }

    /**
     * Iterates from this number up to the given number
     *
     * @param self a Number
     * @param to another Number to go up to
     * @param closure the closure to call
     */
    public static void upto(Number self, Number to, Closure closure) {
        for (int i = self.intValue(), size = to.intValue(); i <= size; i++) {
            closure.call(new Integer(i));
        }
    }

    /**
     * Iterates from this number up to the given number using a step increment
     *
     * @param self a Number to start with
     * @param to a Number to go up to
     * @param stepNumber a Number representing the step increment
     * @param closure the closure to call
     */
    public static void step(Number self, Number to, Number stepNumber, Closure closure) {
        for (int i = self.intValue(), size = to.intValue(), step = stepNumber.intValue(); i < size; i += step) {
            closure.call(new Integer(i));
        }
    }

    /**
     * Get the absolute value
     *
     * @param number a Number
     * @return the absolute value of that Number
     */
    //Note:  This method is NOT called if number is a BigInteger or BigDecimal because
    //those classes implement a method with a better exact match.
    public static int abs(Number number) {
        return Math.abs(number.intValue());
    }

    /**
     * Get the absolute value
     *
     * @param number a Long
     * @return the absolute value of that Long
     */
    public static long abs(Long number) {
        return Math.abs(number.longValue());
    }

    /**
     * Get the absolute value
     *
     * @param number a Float
     * @return the absolute value of that Float
     */
    public static float abs(Float number) {
        return Math.abs(number.floatValue());
    }

    /**
     * Get the absolute value
     *
     * @param number a Double
     * @return the absolute value of that Double
     */
    public static double abs(Double number) {
        return Math.abs(number.doubleValue());
    }

    /**
     * Get the absolute value
     *
     * @param number a Float
     * @return the absolute value of that Float
     */
    public static int round(Float number) {
        return Math.round(number.floatValue());
    }

    /**
     * Round the value
     *
     * @param number a Double
     * @return the absolute value of that Double
     */
    public static long round(Double number) {
        return Math.round(number.doubleValue());
    }

    /**
     * Parse a String into an Integer
     *
     * @param self a String
     * @return an Integer
     */
    public static Integer toInteger(String self) {
        return Integer.valueOf(self);
    }

    /**
     * Parse a String into a Long
     *
     * @param self a String
     * @return a Long
     */
    public static Long toLong(String self) {
        return Long.valueOf(self);
    }

    /**
     * Parse a String into a Float
     *
     * @param self a String
     * @return a Float
     */
    public static Float toFloat(String self) {
        return Float.valueOf(self);
    }

    /**
     * Parse a String into a Double
     *
     * @param self a String
     * @return a Double
     */
    public static Double toDouble(String self) {
        return Double.valueOf(self);
    }

    /**
     * Transform a Number into an Integer
     *
     * @param self a Number
     * @return an Integer
     */
    public static Integer toInteger(Number self) {
        return new Integer(self.intValue());
    }

    // Date methods
    //-------------------------------------------------------------------------

    /**
     * Increments a Date by a day
     *
     * @param self a Date
     * @return the next days date
     */
    public static Date next(Date self) {
        return plus(self, 1);
    }

    /**
     * Decrement a Date by a day
     *
     * @param self a Date
     * @return the previous days date
     */
    public static Date previous(Date self) {
        return minus(self, 1);
    }

    /**
     * Adds a number of days to this date and returns the new date
     *
     * @param self a Date
     * @param days the number of days to increase
     * @return the new date
     */
    public static Date plus(Date self, int days) {
        Calendar calendar = (Calendar) Calendar.getInstance().clone();
        calendar.setTime(self);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }

    /**
     * Subtracts a number of days from this date and returns the new date
     *
     * @param self a Date
     * @return the new date
     */
    public static Date minus(Date self, int days) {
        return plus(self, -days);
    }

    // File and stream based methods
    //-------------------------------------------------------------------------

    /**
     * Iterates through the given file line by line
     *
     * @param self a File
     * @param closure a closure
     * @throws IOException
     */
    public static void eachLine(File self, Closure closure) throws IOException {
        eachLine(newReader(self), closure);
    }

    /**
     * Iterates through the given reader line by line
     *
     * @param self a Reader
     * @param closure a closure
     * @throws IOException
     */
    public static void eachLine(Reader self, Closure closure) throws IOException {
        BufferedReader br = null;

        if (self instanceof BufferedReader)
            br = (BufferedReader) self;
        else
            br = new BufferedReader(self);

        try {
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                else {
                    closure.call(line);
                }
            }
            br.close();
        }
        catch (IOException e) {
            if (self != null) {
                try {
                    br.close();
                }
                catch (Exception e2) {
                    // ignore as we're already throwing
                }
                throw e;
            }
        }
    }
    
    /**
     * Iterates through the given file line by line, splitting on the seperator
     *
     * @param self a File
     * @param sep a String separator
     * @param closure a closure
     * @throws IOException
     */
    public static void splitEachLine(File self, String sep, Closure closure) throws IOException {
        splitEachLine(newReader(self), sep, closure);
    }

    /**
     * Iterates through the given reader line by line, splitting on the seperator
     *
     * @param self a Reader
     * @param sep a String separator
     * @param closure a closure
     * @throws IOException
     */
    public static void splitEachLine(Reader self, String sep, Closure closure) throws IOException {
        BufferedReader br = null;

        if (self instanceof BufferedReader)
            br = (BufferedReader) self;
        else
            br = new BufferedReader(self);

        List args = new ArrayList();

        try {
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                else {
                    List vals = Arrays.asList(line.split(sep));
                    args.clear();
                    args.add(vals);
                    closure.call(args);
                }
            }
            br.close();
        }
        catch (IOException e) {
            if (self != null) {
                try {
                    br.close();
                }
                catch (Exception e2) {
                    // ignore as we're already throwing
                }
                throw e;
            }
        }
    }
    
    /**
     * Read a single, whole line from the given Reader
     *
     * @param self a Reader
     * @return a line 
     * @throws IOException
     */
    public static String readLine(Reader self) throws IOException {
        BufferedReader br = null;

        if (self instanceof BufferedReader) {
            br = (BufferedReader) self;
        } else {
            br = new BufferedReader(self);
        }
        return br.readLine();
    }
    
    /**
     * Read a single, whole line from the given InputStream
     *
     * @param stream an InputStream
     * @return a line
     * @throws IOException
     */
    public static String readLine(InputStream stream) throws IOException {
        return readLine(new InputStreamReader(stream));
    }
    
    /**
     * Reads the file into a list of Strings for each line
     *
     * @param file a File
     * @return a List of lines
     * @throws IOException
     */
    public static List readLines(File file) throws IOException {
        IteratorClosureAdapter closure = new IteratorClosureAdapter(file);
        eachLine(file, closure);
        return closure.asList();
    }

    /**
     * Reads the content of the File opened with the specified encoding and returns it as a String
     *
     * @param file the file whose content we want to read
     * @param charset the charset used to read the content of the file
     * @return a String containing the content of the file
     * @throws IOException
     */
    public static String getText(File file, String charset) throws IOException {
        BufferedReader reader = newReader(file, charset);
        return getText(reader);
    }

    /**
     * Reads the content of the File and returns it as a String
     *
     * @param file the file whose content we want to read
     * @return a String containing the content of the file
     * @throws IOException
     */
    public static String getText(File file) throws IOException {
        BufferedReader reader = newReader(file);
        return getText(reader);
    }

    /**
     * Reads the content of this URL and returns it as a String
     *
     * @param url URL to read content from
     * @return the text from that URL
     * @throws IOException
     */
    public static String getText(URL url) throws IOException {
        return getText(url, CharsetToolkit.getDefaultSystemCharset().toString());
    }

    /**
     * Reads the content of this URL and returns it as a String
     *
     * @param url URL to read content from
     * @param charset opens the stream with a specified charset
     * @return the text from that URL
     * @throws IOException
     */
    public static String getText(URL url, String charset) throws IOException {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(url.openConnection().getInputStream(), charset));
        return getText(reader);
    }

    /**
     * Reads the content of this InputStream and returns it as a String
     *
     * @param is an input stream
     * @return the text from that URL
     * @throws IOException
     */
    public static String getText(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        return getText(reader);
    }

    /**
     * Reads the content of this InputStream with a specified charset and returns it as a String
     *
     * @param is an input stream
     * @param charset opens the stream with a specified charset
     * @return the text from that URL
     * @throws IOException
     */
    public static String getText(InputStream is, String charset) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
        return getText(reader);
    }

    /**
     * Reads the content of the BufferedReader and returns it as a String
     *
     * @param reader a BufferedReader whose content we want to read
     * @return a String containing the content of the buffered reader
     * @throws IOException
     */
    public static String getText(BufferedReader reader) throws IOException {
        StringBuffer answer = new StringBuffer();
        // reading the content of the file within a char buffer allow to keep the correct line endings
        char[] charBuffer = new char[4096];
        int nbCharRead = 0;
        while ((nbCharRead = reader.read(charBuffer)) != -1) {
            if (nbCharRead == charBuffer.length) {
                // appends a full buffer
                answer.append(charBuffer);
            }
            else {
                // appends the last incomplete buffer
                char[] endBuffer = new char[nbCharRead];
                System.arraycopy(charBuffer, 0, endBuffer, 0, nbCharRead);
                answer.append(endBuffer);
            }
        }
        reader.close();
        return answer.toString();
    }

    /**
     * Write the text and append a new line (depending on the platform line-ending)
     *
     * @param writer a BufferedWriter
     * @param line the line to write
     * @throws IOException
     */
    public static void writeLine(BufferedWriter writer, String line) throws IOException {
        writer.write(line);
        writer.newLine();
    }

    /**
     * Write the text to the File.
     *
     * @param file a File
     * @param text the text to write to the File
     * @throws IOException
     */
    public static void write(File file, String text) throws IOException {
        BufferedWriter writer = newWriter(file);
        writer.write(text);
        writer.close();
    }

    /**
     * Write the text to the File with a specified encoding.
     *
     * @param file a File
     * @param text the text to write to the File
     * @param charset the charset used
     * @throws IOException
     */
    public static void write(File file, String text, String charset) throws IOException {
        BufferedWriter writer = newWriter(file, charset);
        writer.write(text);
        writer.close();
    }

    /**
     * Append the text at the end of the File
     *
     * @param file a File
     * @param text the text to append at the end of the File
     * @throws IOException
     */
    public static void append(File file, String text) throws IOException {
        BufferedWriter writer = newWriter(file, true);
        writer.write(text);
        writer.close();
    }

    /**
     * Append the text at the end of the File with a specified encoding
     *
     * @param file a File
     * @param text the text to append at the end of the File
     * @param charset the charset used
     * @throws IOException
     */
    public static void append(File file, String text, String charset) throws IOException {
        BufferedWriter writer = newWriter(file, charset, true);
        writer.write(text);
        writer.close();
    }

    /**
     * Reads the reader into a list of Strings for each line
     *
     * @param reader a Reader
     * @return a List of lines
     * @throws IOException
     */
    public static List readLines(Reader reader) throws IOException {
        IteratorClosureAdapter closure = new IteratorClosureAdapter(reader);
        eachLine(reader, closure);
        return closure.asList();
    }

    /**
     * Invokes the closure for each file in the given directory
     *
     * @param self a File
     * @param closure a closure
     */
    public static void eachFile(File self, Closure closure) {
        File[] files = self.listFiles();
        for (int i = 0; i < files.length; i++) {
            closure.call(files[i]);
        }
    }

    /**
     * Invokes the closure for each file in the given directory and recursively.
     * It is a depth-first exploration, directories are included in the search.
     *
     * @param self a File
     * @param closure a closure
     */
    public static void eachFileRecurse(File self, Closure closure) {
        File[] files = self.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                closure.call(files[i]);
                eachFileRecurse(files[i], closure);
            } else {
                closure.call(files[i]);
            }
        }
    }

    /**
     * Helper method to create a buffered reader for a file
     *
     * @param file a File
     * @return a BufferedReader
     * @throws IOException
     */
    public static BufferedReader newReader(File file) throws IOException {
        CharsetToolkit toolkit = new CharsetToolkit(file);
        return toolkit.getReader();
    }

    /**
     * Helper method to create a buffered reader for a file, with a specified charset
     *
     * @param file a File
     * @param charset the charset with which we want to write in the File
     * @return a BufferedReader
     * @throws FileNotFoundException if the File was not found
     * @throws UnsupportedEncodingException if the encoding specified is not supported
     */
    public static BufferedReader newReader(File file, String charset)
        throws FileNotFoundException, UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
    }

    /**
     * Provides a reader for an arbitrary input stream
     * @param self an input stream
     * @return a reader
     */
    public static BufferedReader newReader(final InputStream self) {
        return new BufferedReader(new InputStreamReader(self));
    }

    /**
     * Helper method to create a new BufferedReader for a file and then
     * passes it into the closure and ensures its closed again afterwords
     *
     * @param file
     * @throws FileNotFoundException
     */
    public static void withReader(File file, Closure closure) throws IOException {
        withReader(newReader(file), closure);
    }

    /**
     * Helper method to create a buffered output stream for a file
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public static BufferedOutputStream newOutputStream(File file) throws IOException {
        return new BufferedOutputStream(new FileOutputStream(file));
    }

    /**
     * Helper method to create a new OutputStream for a file and then
     * passes it into the closure and ensures its closed again afterwords
     *
     * @param file a File
     * @throws FileNotFoundException
     */
    public static void withOutputStream(File file, Closure closure) throws IOException {
        withStream(newOutputStream(file), closure);
    }

    /**
     * Helper method to create a new InputStream for a file and then
     * passes it into the closure and ensures its closed again afterwords
     *
     * @param file a File
     * @throws FileNotFoundException
     */
    public static void withInputStream(File file, Closure closure) throws IOException {
        withStream(newInputStream(file), closure);
    }

    /**
     * Helper method to create a buffered writer for a file
     *
     * @param file a File
     * @return a BufferedWriter
     * @throws FileNotFoundException
     */
    public static BufferedWriter newWriter(File file) throws IOException {
        return new BufferedWriter(new FileWriter(file));
    }

    /**
     * Helper method to create a buffered writer for a file in append mode
     *
     * @param file a File
     * @param append true if in append mode
     * @return a BufferedWriter
     * @throws FileNotFoundException
     */
    public static BufferedWriter newWriter(File file, boolean append) throws IOException {
        return new BufferedWriter(new FileWriter(file, append));
    }

    /**
     * Helper method to create a buffered writer for a file
     *
     * @param file a File
     * @param charset the name of the encoding used to write in this file
     * @param append true if in append mode
     * @return a BufferedWriter
     * @throws FileNotFoundException
     */
    public static BufferedWriter newWriter(File file, String charset, boolean append) throws IOException {
        if (append) {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), charset));
        }
        else {
            // first write the Byte Order Mark for Unicode encodings
            FileOutputStream stream = new FileOutputStream(file);
            if ("UTF-16BE".equals(charset)) {
                writeUtf16Bom(stream, true);
            }
            else if ("UTF-16LE".equals(charset)) {
                writeUtf16Bom(stream, false);
            }
            return new BufferedWriter(new OutputStreamWriter(stream, charset));
        }
    }

    /**
     * Helper method to create a buffered writer for a file
     *
     * @param file    a File
     * @param charset the name of the encoding used to write in this file
     * @return a BufferedWriter
     * @throws FileNotFoundException
     */
    public static BufferedWriter newWriter(File file, String charset) throws IOException {
        return newWriter(file, charset, false);
    }

    /**
     * Write a Byte Order Mark at the begining of the file
     *
     * @param stream the FileOuputStream to write the BOM to
     * @param bigEndian true if UTF 16 Big Endian or false if Low Endian
     * @throws IOException
     */
    private static void writeUtf16Bom(FileOutputStream stream, boolean bigEndian) throws IOException {
        if (bigEndian) {
            stream.write(-2);
            stream.write(-1);
        }
        else {
            stream.write(-1);
            stream.write(-2);
        }
    }

    /**
     * Helper method to create a new BufferedWriter for a file and then
     * passes it into the closure and ensures it is closed again afterwords
     *
     * @param file a File
     * @param closure a closure
     * @throws FileNotFoundException
     */
    public static void withWriter(File file, Closure closure) throws IOException {
        withWriter(newWriter(file), closure);
    }

    /**
     * Helper method to create a new BufferedWriter for a file in a specified encoding
     * and then passes it into the closure and ensures it is closed again afterwords
     *
     * @param file a File
     * @param charset the charset used
     * @param closure a closure
     * @throws FileNotFoundException
     */
    public static void withWriter(File file, String charset, Closure closure) throws IOException {
        withWriter(newWriter(file, charset), closure);
    }

    /**
     * Helper method to create a new BufferedWriter for a file in a specified encoding
     * in append mode and then passes it into the closure and ensures it is closed again afterwords
     *
     * @param file a File
     * @param charset the charset used
     * @param closure a closure
     * @throws FileNotFoundException
     */
    public static void withWriterAppend(File file, String charset, Closure closure) throws IOException {
        withWriter(newWriter(file, charset, true), closure);
    }

    /**
     * Helper method to create a new PrintWriter for a file
     *
     * @param file a File
     * @throws FileNotFoundException
     */
    public static PrintWriter newPrintWriter(File file) throws IOException {
        return new PrintWriter(newWriter(file));
    }

    /**
     * Helper method to create a new PrintWriter for a file with a specified charset
     *
     * @param file a File
     * @param charset the charset
     * @return a PrintWriter
     * @throws FileNotFoundException
     */
    public static PrintWriter newPrintWriter(File file, String charset) throws IOException {
        return new PrintWriter(newWriter(file, charset));
    }

    /**
     * Helper method to create a new PrintWriter for a file and then
     * passes it into the closure and ensures its closed again afterwords
     *
     * @param file a File
     * @throws FileNotFoundException
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
     * Reads the stream into a list of Strings for each line
     *
     * @param stream a stream
     * @return a List of lines
     * @throws IOException
     */
    public static List readLines(InputStream stream) throws IOException {
        return readLines(new BufferedReader(new InputStreamReader(stream)));
    }

    /**
     * Iterates through the given stream line by line
     *
     * @param stream a stream
     * @param closure a closure
     * @throws IOException
     */
    public static void eachLine(InputStream stream, Closure closure) throws IOException {
        eachLine(new InputStreamReader(stream), closure);
    }

    /**
     * Iterates through the lines read from the URL's associated input stream
     *
     * @param url a URL to open and read
     * @param closure a closure to apply on each line
     * @throws IOException
     */
    public static void eachLine(URL url, Closure closure) throws IOException {
        eachLine(url.openConnection().getInputStream(), closure);
    }

    /**
     * Helper method to create a new BufferedReader for a URL and then
     * passes it into the closure and ensures its closed again afterwords
     *
     * @param url a URL
     * @throws FileNotFoundException
     */
    public static void withReader(URL url, Closure closure) throws IOException {
        withReader(url.openConnection().getInputStream(), closure);
    }

    /**
     * Helper method to create a new BufferedReader for a stream and then
     * passes it into the closure and ensures its closed again afterwords
     *
     * @param in a stream
     * @throws FileNotFoundException
     */
    public static void withReader(InputStream in, Closure closure) throws IOException {
        withReader(new InputStreamReader(in), closure);
    }

    /**
     * Allows an output stream to be used, calling the closure with the output stream
     * and then ensuring that the output stream is closed down again irrespective
     * of whether exceptions occur
     *
     * @param stream the stream which is used and then closed
     * @param closure the closure that the writer is passed into
     * @throws IOException
     */
    public static void withWriter(OutputStream stream, Closure closure) throws IOException {
        withWriter(new OutputStreamWriter(stream), closure);
    }

    /**
     * Allows an output stream to be used, calling the closure with the output stream
     * and then ensuring that the output stream is closed down again irrespective
     * of whether exceptions occur.
     *
     * @param stream the stream which is used and then closed
     * @param charset the charset used
     * @param closure the closure that the writer is passed into
     * @throws IOException
     */
    public static void withWriter(OutputStream stream, String charset, Closure closure) throws IOException {
        withWriter(new OutputStreamWriter(stream, charset), closure);
    }

    /**
     * Allows a OutputStream to be used, calling the closure with the stream
     * and then ensuring that the stream is closed down again irrespective
     * of whether exceptions occur.
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
     * @param file a File
     * @return a BufferedInputStream of the file
     * @throws FileNotFoundException
     */
    public static BufferedInputStream newInputStream(File file) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(file));
    }

    /**
     * Traverse through each byte of the specified File
     *
     * @param self a File
     * @param closure a closure
     */
    public static void eachByte(File self, Closure closure) throws IOException {
        BufferedInputStream is = newInputStream(self);
        eachByte(is, closure);
    }

    /**
     * Traverse through each byte of the specified stream
     *
     * @param is stream to iterate over
     * @param closure closure to apply to each byte
     * @throws IOException
     */
    public static void eachByte(InputStream is, Closure closure) throws IOException {
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
     * Traverse through each byte of the specified URL
     *
     * @param url url to iterate over
     * @param closure closure to apply to each byte
     * @throws IOException
     */
    public static void eachByte(URL url, Closure closure) throws IOException {
        InputStream is = url.openConnection().getInputStream();
        eachByte(is, closure);
    }

    /**
     * Transforms the characters from a reader with a Closure and write them to a writer
     *
     * @param reader
     * @param writer
     * @param closure
     */
    public static void transformChar(Reader reader, Writer writer, Closure closure) {
        int c;
        try {
            char[] chars = new char[1]; 
            while ((c = reader.read()) != -1) {
                chars[0] = (char)c;
                writer.write((String)closure.call(new String(chars)));
            }
        } catch (IOException e) {
        }
    }
    
    /**
     * Transforms the lines from a reader with a Closure and write them to a writer
     *
     * @param reader
     * @param writer
     * @param closure
     */
    public static void transformLine(Reader reader, Writer writer, Closure closure) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        BufferedWriter bw = new BufferedWriter(writer);
        String line;
        while ((line = br.readLine()) != null) {
            Object o = closure.call(line);
            if (o != null) {
                bw.write(o.toString());
                bw.newLine();
            }
        }
    }

    /**
     * Filter the lines from a reader and write them on the writer, according to a closure
     * which returns true or false.
     *
     * @param reader a reader
     * @param writer a writer
     * @param closure the closure which returns booleans
     * @throws IOException
     */
    public static void filterLine(Reader reader, Writer writer, Closure closure) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        BufferedWriter bw = new BufferedWriter(writer);
        String line;
        while ((line = br.readLine()) != null) {
            if (InvokerHelper.asBool(closure.call(line))) {
                bw.write(line);
                bw.newLine();
            }
        }
        bw.flush();
    }

    /**
     * Filters the lines of a File and creates a Writeable in return to stream the filtered lines
     *
     * @param self a File
     * @param closure a closure which returns a boolean indicating to filter the line or not
     * @return a Writable closure
     * @throws IOException if <code>self</code> is not readable
     */
    public static Writable filterLine(final File self, final Closure closure) throws IOException {
        return filterLine(newReader(self), closure);
    }

    /**
     * Filter the lines from a File and write them on a writer, according to a closure
     * which returns true or false
     *
     * @param self a File
     * @param writer a writer
     * @param closure a closure which returns a boolean value and takes a line as input
     * @throws IOException if <code>self</code> is not readable
     */
    public static void filterLine(final File self, final Writer writer, final Closure closure) throws IOException {
        filterLine(newReader(self), writer, closure);
    }

    /**
     * Filter the lines of a Reader and create a Writable in return to stream the filtered lines
     *
     * @param reader a reader
     * @param closure a closure returning a boolean indicating to filter or not a line
     * @return a Writable closure
     */
    public static Writable filterLine(Reader reader, final Closure closure) {
        final BufferedReader br = new BufferedReader(reader);
        return new Writable() {
            public Writer writeTo(Writer out) throws IOException {
                BufferedWriter bw = new BufferedWriter(out);
                String line;
                while ((line = br.readLine()) != null) {
                    if (InvokerHelper.asBool(closure.call(line))) {
                        bw.write(line);
                        bw.newLine();
                    }
                }
                bw.flush();
                return out;
            }

            public String toString() {
                StringWriter buffer = new StringWriter();
                try {
                    writeTo(buffer);
                }
                catch (IOException e) {
                    throw new RuntimeException(e); // TODO: change this exception type
                }
                return buffer.toString();
            }
        };
    }

    /**
     * Filter lines from an input stream using a closure predicate
     *
     * @param self an input stream
     * @param predicate a closure which returns boolean and takes a line
     * @return a filtered writer
     */
    public static Writable filterLine(final InputStream self, final Closure predicate) {
        return filterLine(newReader(self), predicate);
    }

    /**
     * Filters lines from an input stream, writing to a writer, using a closure which
     * returns boolean and takes a line.
     *
     * @param self an InputStream
     * @param writer a writer to write output to
     * @param predicate a closure which returns a boolean and takes a line as input
     */
    public static void filterLine(final InputStream self, final Writer writer, final Closure predicate)
            throws IOException {
        filterLine(newReader(self), writer, predicate);
    }

    /**
     * Reads the content of the file into an array of byte
     *
     * @param file a File
     * @return a List of Bytes
     */
    public static byte[] readBytes(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(bytes);
        fileInputStream.close();
        return bytes;
    }



    // ================================
    // Socket and ServerSocket methods

    /**
     * Allows an InputStream and an OutputStream from a Socket to be used,
     * calling the closure with the streams and then ensuring that the streams are closed down again
     * irrespective of whether exceptions occur.
     *
     * @param socket a Socket
     * @param closure a Closure
     * @throws IOException
     */
    public static void withStreams(Socket socket, Closure closure) throws IOException {
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        try {
            closure.call(new Object[]{input, output});
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                // noop
            }
            try {
                output.close();
            } catch (IOException e) {
                // noop
            }
        }
    }
    
    /**
     * Overloads the left shift operator to provide an append mechanism
     * to add things to the output stream of a socket
     *
     * @param self a Socket
     * @param value a value to append
     * @return a Writer
     */
    public static Writer leftShift(Socket self, Object value) throws IOException {
        return leftShift(self.getOutputStream(), value);
    }
    
    /**
     * Overloads the left shift operator to provide an append mechanism
     * to add bytes to the output stream of a socket
     *
     * @param self a Socket
     * @param value a value to append
     * @return an OutputStream
     */
    public static OutputStream leftShift(Socket self, byte[] value) throws IOException {
        return leftShift(self.getOutputStream(), value);
    }
    
    /**
     * Allow to pass a Closure to the accept methods of ServerSocket
     *
     * @param serverSocket a ServerSocket
     * @param closure a Closure
     * @return a Socket
     * @throws IOException
     */
    public static Socket accept(ServerSocket serverSocket, final Closure closure) throws IOException {
        final Socket socket = serverSocket.accept();
        new Thread(new Runnable()
        {
            public void run() {
                try {
                    closure.call(socket);
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // noop
                    }
                }
            }
        }).start();
        return socket;
    }


    /**
     * @param file a File
     * @return a File which wraps the input file and which implements Writable
     */
    public static File asWritable(File file) {
        return new WritableFile(file);
    }

    /**
     * @param file a File
     * @param encoding the encoding to be used when reading the file's contents
     * @return File which wraps the input file and which implements Writable
     */
    public static File asWritable(File file, String encoding) {
        return new WritableFile(file, encoding);
    }

    /**
     * Converts the given String into a List of strings of one character
     *
     * @param self a String
     * @return a List of characters (a 1-character String)
     */
    public static List toList(String self) {
        int size = self.length();
        List answer = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            answer.add(self.substring(i, i + 1));
        }
        return answer;
    }

    // Process methods
    //-------------------------------------------------------------------------

    /**
     * An alias method so that a process appears similar to System.out, System.in, System.err; 
     * you can use process.in, process.out, process.err in a similar way
     *
     * @return an InputStream
     */
    public static InputStream getIn(Process self) {
        return self.getInputStream();
    }

    /**
     * Read the text of the output stream of the Process.
     *
     * @param self a Process
     * @return the text of the output
     * @throws IOException
     */
    public static String getText(Process self) throws IOException {
        return getText(new BufferedReader(new InputStreamReader(self.getInputStream())));
    }

    /**
     * An alias method so that a process appears similar to System.out, System.in, System.err; 
     * you can use process.in, process.out, process.err in a similar way
     *
     * @return an InputStream
     */
    public static InputStream getErr(Process self) {
        return self.getErrorStream();
    }

    /**
     * An alias method so that a process appears similar to System.out, System.in, System.err; 
     * you can use process.in, process.out, process.err in a similar way
     *
     * @return an OutputStream
     */
    public static OutputStream getOut(Process self) {
        return self.getOutputStream();
    }
    
    /**
     * Overloads the left shift operator to provide an append mechanism
     * to pipe into a Process
     *
     * @param self a Process
     * @param value a value to append
     * @return a Writer
     */
    public static Writer leftShift(Process self, Object value) throws IOException {
        return leftShift(self.getOutputStream(), value);
    }
    
    /**
     * Overloads the left shift operator to provide an append mechanism
     * to pipe into a Process
     *
     * @param self a Process
     * @param value a value to append
     * @return an OutputStream
     */
    public static OutputStream leftShift(Process self, byte[] value) throws IOException {
        return leftShift(self.getOutputStream(), value);
    }

    /**
     * Wait for the process to finish during a certain amount of time, otherwise stops the process.
     *
     * @param self a Process
     * @param numberOfMillis the number of milliseconds to wait before stopping the process
     */
    public static void waitForOrKill(Process self, long numberOfMillis) {
        ProcessRunner runnable = new ProcessRunner(self);
        Thread thread = new Thread(runnable);
        thread.start();
        runnable.waitForOrKill(numberOfMillis);
    }
    /**
     * process each regex matched substring of a string object. The object
     * passed to the closure is a matcher with rich information of the last
     * successful match
     *
     * @author bing ran
     * @param str the target string
     * @param regex a Regex string
     * @param closure a closure
     */
    public static void eachMatch(String str, String regex, Closure closure) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        while (m.find()) {
            int count = m.groupCount();
            ArrayList groups = new ArrayList();
            for (int i = 0; i <= count; i++) {
                groups.add(m.group(i));
            }
    		closure.call(groups);
    	}
    }

    public static void each (Matcher matcher, Closure closure) {
        Matcher m = matcher;
        while (m.find()) {
            int count = m.groupCount();
            ArrayList groups = new ArrayList();
            for (int i = 0; i <= count; i++) {
                groups.add(m.group(i));
            }
            closure.call(groups);
    	}
    }

    /**
     * Iterates over every element of the collection and return the index of the first object 
     * that matches the condition specified in the closure
     *
     * @param self    the iteration object over which we iterate
     * @param closure  the filter to perform a match on the collection 
     * @return an integer that is the index of the first macthed object. 
     */
    public static int findIndexOf(Object self, Closure closure) {
        int i = 0;
    	for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext(); i++) {
            Object value = iter.next();
            if (InvokerHelper.asBool(closure.call(value))) {
                break;
            }
        }        
    	return i;
    }

    /**
     * A Runnable which waits for a process to complete together with a notification scheme
     * allowing another thread to wait a maximum number of seconds for the process to complete
     * before killing it.
     */
    protected static class ProcessRunner implements Runnable {
        Process process;
        private boolean finished;

        public ProcessRunner(Process process) {
            this.process = process;
        }

        public void run() {
            try {
                process.waitFor();
            }
            catch (InterruptedException e) {
            }
            synchronized (this) {
                notifyAll();
                finished = true;
            }
        }

        public synchronized void waitForOrKill(long millis) {
            if (!finished) {
                try {
                    wait(millis);
                }
                catch (InterruptedException e) {
                }
                if (!finished) {
                    process.destroy();
                }
            }
        }
    }
}
