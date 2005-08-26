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

import groovy.lang.*;
import groovy.util.CharsetToolkit;
import groovy.util.ClosureComparator;
import groovy.util.OrderBy;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
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
 * @author Hein Meling
 * @author Dierk Koenig
 * @author Pilho Kim
 * @version $Revision$
 */
public class DefaultGroovyMethods {

    private static Logger log = Logger.getLogger(DefaultGroovyMethods.class.getName());

    private static final Integer ONE = new Integer(1);
    private static final char ZERO_CHAR = '\u0000';

    /**
     * Identity check. Since == is overridden in Groovy with the meaning of equality
     * we need some fallback to check for object identity.
     * @param self
     * @param other
     * @return true if self and other are identical, false otherwise
     */
    public static boolean is(Object self, Object other){
        return System.identityHashCode(self) == System.identityHashCode(other);
    }

    /**
     * Allows the closure to be called for the object reference self
     *
     * @param self     the object to have a closure act upon
     * @param closure  the closure to call on the object
     * @return         result of calling the closure
     */
    public static Object identity(Object self, Closure closure) {
        closure.setDelegate(self);
        return closure.call(self);
    }

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
                    } catch (Exception e) {
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
        for (Iterator itr = props.iterator(); itr.hasNext();) {
            PropertyValue pv = (PropertyValue) itr.next();
            closure.call(pv.getName());
        }
    }

    public static void eachProperty(Object self, Closure closure) {
        List props = allProperties(self);
        for (Iterator itr = props.iterator(); itr.hasNext();) {
            PropertyValue pv = (PropertyValue) itr.next();
            closure.call(pv);
        }
    }

    public static List allProperties(Object self) {
        List props = new ArrayList();
        MetaClass metaClass = InvokerHelper.getMetaClass(self);

        List mps;

        if (self instanceof groovy.util.Expando) {
            mps = ((groovy.util.Expando) self).getProperties();
        } else {
            // get the MetaProperty list from the MetaClass
            mps = metaClass.getProperties();
        }

        for (Iterator itr = mps.iterator(); itr.hasNext();) {
            MetaProperty mp = (MetaProperty) itr.next();
            PropertyValue pv = new PropertyValue(self, mp);
            props.add(pv);
        }

        return props;
    }

    /**
     * Scoped use method
     */
    public static void use(Object self, Class categoryClass, Closure closure) {
        GroovyCategorySupport.use(categoryClass, closure);
    }

    /**
     * Scoped use method with list of categories
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
     * Print a linebreak to the standard out.
     */
    public static void println(Object self) {
        System.out.println();
    }

    /**
     * Print to a console in interactive format along with a newline
     */
    public static void println(Object self, Object value) {
        System.out.println(InvokerHelper.toString(value));
    }

  /**
   *  Printf to a console.  Only works with JDK1.5 or later.
   *
   *  @author Russel Winder
   *  @version 2005.02.01.15.53
   */
  public static void printf(final Object self, final String format, final Object[] values) {
    if ( System.getProperty("java.version").charAt(2) == '5' ) {
      //
      //  Cannot just do:
      //
      //        System.out.printf(format, values) ;
      //
      //  because this fails to compile on JDK1.4.x and earlier.  So until the entire world is using
      //  JDK1.5 or later then we have to do things by reflection so as to hide the use of printf
      //  from the compiler.  In JDK1.5 you might try:
      //
      //        System.out.getClass().getMethod("printf", String.class, Object[].class).invoke(System.out, format, values) ;
      //
      //  but of course this doesn't work on JDK1.4 as it relies on varargs.  argh.  So we are
      //  forced into:
      //
      try {
        System.out.getClass().getMethod("printf", new Class[] {String.class, Object[].class}).invoke(System.out, new Object[] {format, values}) ;
      } catch ( NoSuchMethodException nsme ) {
        throw new RuntimeException ("getMethod threw a NoSuchMethodException.  This is impossible.") ;
      } catch ( IllegalAccessException iae ) {
        throw new RuntimeException ("invoke threw a IllegalAccessException.  This is impossible.") ;
      } catch ( java.lang.reflect.InvocationTargetException ite ) {
        throw new RuntimeException ("invoke threw a InvocationTargetException.  This is impossible.") ;
      }
    } else {
      throw new RuntimeException ("printf requires JDK1.5 or later.") ;
    }
  }
  
  /**
   * Returns a formatted string using the specified format string and
   * arguments.
   *
   * <p>
   * For examples, <pre>
   *     printf ( "Hello, %s!\n" , [ "world" ] as String[] )
   *     printf ( "Hello, %s!\n" , [ "Groovy" ])
   *     printf ( "%d + %d = %d\n" , [ 1 , 2 , 1+2 ] as Integer[] )
   *     printf ( "%d + %d = %d\n" , [ 3 , 3 , 3+3 ])
   * 
   *     ( 1..5 ).each { printf ( "-- %d\n" , [ it ] as Integer[] ) }
   *     ( 1..5 ).each { printf ( "-- %d\n" , [ it ] as int[] ) }
   *     ( 0x41..0x45 ).each { printf ( "-- %c\n" , [ it ] as char[] ) }
   *     ( 07..011 ).each { printf ( "-- %d\n" , [ it ] as byte[] ) }
   *     ( 7..11 ).each { printf ( "-- %d\n" , [ it ] as short[] ) }
   *     ( 7..11 ).each { printf ( "-- %d\n" , [ it ] as long[] ) }
   *     ( 7..11 ).each { printf ( "-- %5.2f\n" , [ it ] as float[] ) }
   *     ( 7..11 ).each { printf ( "-- %5.2g\n" , [ it ] as double[] ) }
   * </pre>
   * <p>
   * 
   * @param  format
   *         A format string
   *
   * @param  arg
   *         Argument which is referenced by the format specifiers in the format
   *         string.  The type of <code>arg</code> should be one of Object[], List,
   *         int[], short[], byte[], char[], boolean[], long[], float[], or double[].
   *
   * @return  A formatted string
   * @since  JDK 1.5
   *
   * @author Pilho Kim
   * @version 2005.07.25.02.31
   */
  public static void printf(final Object self, final String format, Object arg) {
      if (arg instanceof Object[]) {
          printf(self, format, (Object[]) arg);
          return;
      } else if (arg instanceof List) {
          printf(self, format, ((List) arg).toArray());
          return;
      } else if (!arg.getClass().isArray()) {
          Object[] o = (Object[]) java.lang.reflect.Array.newInstance(arg.getClass(), 1);
          o[0]=arg;
          printf(self, format, o);
          return;
      }

      Object[] ans = null;
      String elemType = arg.getClass().getName();
      if (elemType.equals("[I")) {
          int[] ia = (int[]) arg;
          ans = new Integer[ia.length];
          for (int i = 0; i < ia.length; i++) {
              ans[i] = new Integer(ia[i]);
          }
      }
      else if (elemType.equals("[C")) {
          char[] ia = (char[]) arg;
          ans = new Character[ia.length];
          for (int i = 0; i < ia.length; i++) {
              ans[i] = new Character(ia[i]);
          }
      }
      else if (elemType.equals("[Z")) {
          boolean[] ia = (boolean[]) arg;
          ans = new Boolean[ia.length];
          for (int i = 0; i < ia.length; i++) {
              ans[i] = new Boolean(ia[i]);
          }
      }
      else if (elemType.equals("[B")) {
          byte[] ia = (byte[]) arg;
          ans = new Byte[ia.length];
          for (int i = 0; i < ia.length; i++) {
              ans[i] = new Byte(ia[i]);
          }
      }
      else if (elemType.equals("[S")) {
          short[] ia = (short[]) arg;
          ans = new Short[ia.length];
          for (int i = 0; i < ia.length; i++) {
              ans[i] = new Short(ia[i]);
          }
      }
      else if (elemType.equals("[F")) {
          float[] ia = (float[]) arg;
          ans = new Float[ia.length];
          for (int i = 0; i < ia.length; i++) {
              ans[i] = new Float(ia[i]);
          }
      }
      else if (elemType.equals("[J")) {
          long[] ia = (long[]) arg;
          ans = new Long[ia.length];
          for (int i = 0; i < ia.length; i++) {
              ans[i] = new Long(ia[i]);
          }
      }
      else if (elemType.equals("[D")) {
          double[] ia = (double[]) arg;
          ans = new Double[ia.length];
          for (int i = 0; i < ia.length; i++) {
              ans[i] = new Double(ia[i]);
          }
      }
      else {
          throw new RuntimeException("printf(String," + arg + ")");
      }
      printf(self, format, (Object[]) ans);
  }


    /**
     * @return a String that matches what would be typed into a terminal to
     *         create this object. e.g. [1, 'hello'].inspect() -> [1, "hello"]
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

    private static Object packArray(Object object) {
        if (object instanceof Object[])
            return new Object[] {object};
        else
            return object;
    }

    // Collection based methods
    //-------------------------------------------------------------------------

    /**
     * Remove all duplicates from a given Collection.
     * Works on the receiver object and returns it.
     * For each duplicate, only the first member which is returned
     * by the given Collection's iterator is retained, but all other ones are removed.
     * The given Collection's original order is retained.
     * If there exists numbers in the Collection, then they are compared
     * as numbers, that is, 2, 2.0, 3L, (short)4 are comparable.
     *
     * <code><pre>
     *     def x = [2, 2.0, 3L, 1.0, (short)4, 1]
     *     def y = x.unique()
     *     assert( y == x && x == [2, 3L, 1.0, (short)4] )
     * </pre></code>
     *
     * @param self
     * @return self without duplicates
     */
   /*
    public static Collection unique(Collection self){
        if (self instanceof Set) return self;
        if (self.size() == new HashSet(self).size()) return self;
        Collection seen = new HashSet(self.size());
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            Object o =  iter.next();
            if (seen.contains(o)){
                iter.remove();
            } else {
                seen.add(o);
            }
        }
        return self;
    }
   */
    public static Collection unique(Collection self) {
        if (self instanceof Set)
            return self;
        List answer = new ArrayList();
        NumberComparator comparator = new NumberComparator();
        for (Iterator it = self.iterator(); it.hasNext();) {
            Object o =  it.next();
            boolean duplicated = false;
            for (Iterator it2 = answer.iterator(); it2.hasNext();) {
                Object o2 =  it2.next();
                if (comparator.compare(o, o2) == 0) {
                    duplicated = true;
                    break;
                }
            }
            if (!duplicated)
                answer.add(o);
        }
        self.clear();
        self.addAll(answer);
        return self;
    }

    /**
     * Remove all duplicates from a given Collection.
     * Works on the receiver object and returns it.
     * The order of members in the Collection are compared by the given Comparator.
     * For eachy duplicate, the first member which is returned
     * by the given Collection's iterator is retained, but all other ones are removed.
     * The given Collection's original order is retained.
     *
     * <code><pre>
     *     class Person {
     *         @Property fname, lname
     *         public String toString() {
     *             return fname + " " + lname
     *         }
     *     }
     *
     *     class PersonComparator implements Comparator {
     *         public int compare(Object o1, Object o2) {
     *             Person p1 = (Person) o1
     *             Person p2 = (Person) o2
     *             if (p1.lname != p2.lname)
     *                 return p1.lname.compareTo(p2.lname)
     *             else
     *                 return p1.fname.compareTo(p2.fname)
     *         }
     *
     *         public boolean equals(Object obj) {
     *             return this.equals(obj)
     *         }
     *     }
     *
     *     Person a = new Person(fname:"John", lname:"Taylor")
     *     Person b = new Person(fname:"Clark", lname:"Taylor")
     *     Person c = new Person(fname:"Tom", lname:"Cruz")
     *     Person d = new Person(fname:"Clark", lname:"Taylor")
     *
     *     def list = [a, b, c, d]
     *     List list2 = list.unique(new PersonComparator())
     *     assert( list2 == list && list == [a, b, c] )
     *     
     * </pre></code>
     *
     * @param self        a Collection
     * @param comparator  a Comparator.
     * @return self       without duplicates
     */
    public static Collection unique(Collection self, Comparator comparator) {
        if (self instanceof Set)
            return self;
        List answer = new ArrayList();
        for (Iterator it = self.iterator(); it.hasNext();) {
            Object o =  it.next();
            boolean duplicated = false;
            for (Iterator it2 = answer.iterator(); it2.hasNext();) {
                Object o2 =  it2.next();
                if (comparator.compare(o, o2) == 0) {
                    duplicated = true;
                    break;
                }
            }
            if (!duplicated)
                answer.add(o);
        }
        self.clear();
        self.addAll(answer);
        return self;
    }

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
     * @param self    an Object
     * @param closure a Closure
     */
    public static void eachWithIndex(Object self, Closure closure) {
        int counter = 0;
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            closure.call(new Object[]{iter.next(), new Integer(counter++)});
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
        for (Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            callClosureForMapEntry(closure, entry);
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
     * @param self   the object over which we iterate
     * @param filter the filter to perform on the collection (using the isCase(object) method)
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
     * @param self    the values of the object to map
     * @param closure the closure used to map each element of the collection
     * @return a List of the mapped values
     */
    public static List collect(Object self, Closure closure) {
        return (List) collect(self, new ArrayList(), closure);
    }

    /**
     * Iterates through this object transforming each object into a new value using the closure
     * as a transformer and adding it to the collection, returning the resulting collection.
     *
     * @param self       the values of the object to map
     * @param collection the Collection to which the mapped values are added
     * @param closure    the closure used to map each element of the collection
     * @return the resultant collection
     */
    public static Collection collect(Object self, Collection collection, Closure closure) {
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            collection.add(closure.call(iter.next()));
        }
        return collection;
    }

    /**
     * Iterates through this collection transforming each entry into a new value using the closure
     * as a transformer, returning a list of transformed values.
     *
     * @param self    a collection
     * @param closure the closure used for mapping
     * @return a List of the mapped values
     */
    public static List collect(Collection self, Closure closure) {
        return (List) collect(self, new ArrayList(self.size()), closure);
    }

    /**
     * Iterates through this collection transforming each entry into a new value using the closure
     * as a transformer, returning a list of transformed values.
     *
     * @param self       a collection
     * @param collection the Collection to which the mapped values are added
     * @param closure    the closure used to map each element of the collection
     * @return the resultant collection
     */
    public static Collection collect(Collection self, Collection collection, Closure closure) {
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            collection.add(closure.call(iter.next()));
            if (closure.getDirective() == Closure.DONE) {
                break;
            }
        }
        return collection;
    }

    /**
     * Iterates through this Map transforming each entry into a new value using the closure
     * as a transformer, returning a list of transformed values.
     *
     * @param self    a Map
     * @param closure the closure used for mapping
     * @return a List of the mapped values
     */
    public static Collection collect(Map self, Collection collection, Closure closure) {
        for (Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
            collection.add(closure.call(iter.next()));
        }
        return collection;
    }

    /**
     * Iterates through this Map transforming each entry into a new value using the closure
     * as a transformer, returning a list of transformed values.
     *
     * @param self       a Map
     * @param collection the Collection to which the mapped values are added
     * @param closure    the closure used to map each element of the collection
     * @return the resultant collection
     */
    public static List collect(Map self, Closure closure) {
        return (List) collect(self, new ArrayList(self.size()), closure);
    }

    /**
     * Finds the first value matching the closure condition
     *
     * @param self    an Object with an iterator returning its values
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
     * @param self    a Collection
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
     * @param self    a Map
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
     * @param self    an Object with an Iterator returning its values
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
     * @param self    a Collection
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
     * Finds all entries matching the closure condition. If the
     * closure takes one parameter then it will be passed the Map.Entry
     * otherwise if the closure takes two parameters then it will be
     * passed the key and the value.
     *
     * @param self    a Map
     * @param closure a closure condition applying on the entries
     * @return a new subMap
     */
    public static Map findAll(Map self, Closure closure) {
        Map answer = new HashMap(self.size());
        for (Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (InvokerHelper.asBool(callClosureForMapEntry(closure, entry))) {
                answer.put(entry.getKey(),entry.getValue());
            }
        }
        return answer;
    }

    // internal helper method
    protected static Object callClosureForMapEntry(Closure closure, Map.Entry entry) {
        if (closure.getMaximumNumberOfParameters() == 2) {
            return closure.call(new Object[]{entry.getKey(), entry.getValue()});
        }
        return closure.call(entry);
    }


    /**
     * Iterates through the given collection, passing in the initial value to
     * the closure along with the current iterated item then passing into the
     * next iteration the value of the previous closure.
     *
     * @param self    a Collection 
     * @param value   a value
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
     * Iterates through the given array of objects, passing in the initial value to
     * the closure along with the current iterated item then passing into the
     * next iteration the value of the previous closure.
     *
     * @param self    an Object[]
     * @param value   a value
     * @param closure a closure
     * @return the last value of the last iteration
     */
    public static Object inject(Object[] self, Object value, Closure closure) {
        Object[] params = new Object[2];
        for (int i = 0; i < self.length; i++) {
            params[0] = value;
            params[1] = self[i];
            value = closure.call(params);
        }
        return value;
    }

    /**
     * Concatenates all of the items of the collection together with the given String as a separator
     *
     * @param self      a Collection of objects
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
            } else {
                buffer.append(separator);
            }
            buffer.append(InvokerHelper.toString(value));
        }
        return buffer.toString();
    }

    /**
     * Concatenates all of the elements of the array together with the given String as a separator
     *
     * @param self      an array of Object
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
            } else {
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
     * @param self       a Collection
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
     * @param self       a Collection
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
     * @param self    a Collection
     * @param closure a closure used as a comparator
     * @return the minimum value
     */
    public static Object min(Collection self, Closure closure) {
        int params = closure.getMaximumNumberOfParameters();
        if (params == 1) {
            Object answer = null;
            Object answer_value = null;
            for (Iterator iter = self.iterator(); iter.hasNext();) {
                Object item = iter.next();
                Object value = closure.call(item);
                if (answer == null || InvokerHelper.compareLessThan(value, answer_value)) {
                    answer = item;
                    answer_value = value;
                }
            }
            return answer;
        } else {
            return min(self, new ClosureComparator(closure));
        }
    }

    /**
     * Selects the maximum value found in the collection using the given closure as a comparator
     *
     * @param self    a Collection
     * @param closure a closure used as a comparator
     * @return the maximum value
     */
    public static Object max(Collection self, Closure closure) {
        int params = closure.getMaximumNumberOfParameters();
        if (params == 1) {
            Object answer = null;
            Object answer_value = null;
            for (Iterator iter = self.iterator(); iter.hasNext();) {
                Object item = iter.next();
                Object value = closure.call(item);
                if (answer == null || InvokerHelper.compareLessThan(answer_value, value)) {
                    answer = item;
                    answer_value = value;
                }
            }
            return answer;
        } else {
            return max(self, new ClosureComparator(closure));
        }
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
     * Provide standard Groovy size() method for StringBuffers
     *
     * @param buffer a StringBuffer
     * @return the length of the StringBuffer
     */
    public static int size(StringBuffer buffer) {
        return buffer.length();
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
     * @param text  a String
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
     * @param text  a CharSequence
     * @param range a Range
     * @return the subsequence CharSequence
     */
    public static CharSequence getAt(CharSequence text, Range range) {
        int from = normaliseIndex(InvokerHelper.asInt(range.getFrom()), text.length());
        int to = normaliseIndex(InvokerHelper.asInt(range.getTo()), text.length());

        // If this is a backwards range, reverse the arguments to substring.
        if (from > to) {
            int tmp = from;
            from = to;
            to = tmp;
        }

        return text.subSequence(from, to + 1);
    }
    
    /**
     * Support the range subscript operator for CharSequence or StringBuffer with IntRange
     *
     * @param text  a CharSequence
     * @param range an IntRange
     * @return the subsequence CharSequence
     */    
    public static CharSequence getAt(CharSequence text, IntRange range) {
        return getAt(text, (Range) range);
    }
    
    /**
     * Support the range subscript operator for String with IntRange
     *
     * @param text  a String
     * @param range an IntRange
     * @return the resulting String
     */    
    public static String getAt(String text, IntRange range) {
        return getAt(text,(Range)range);
    }

    /**
     * Support the range subscript operator for String
     *
     * @param text  a String
     * @param range a Range
     * @return a substring corresponding to the Range
     */
    public static String getAt(String text, Range range) {
        int from = normaliseIndex(InvokerHelper.asInt(range.getFrom()), text.length());
        int to = normaliseIndex(InvokerHelper.asInt(range.getTo()), text.length());

        // If this is a backwards range, reverse the arguments to substring.
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

    /**
     * Turns a String into a regular expression pattern
     *
     * @param self a String to convert into a regular expression
     * @return the regular expression pattern
     */
    public static Pattern negate(String self) {
        return InvokerHelper.regexPattern(self);
    }

    /**
     * Replaces all occurrencies of a captured group by the result of a closure on that text.
     *
     * <p> For examples,
     * <pre>
     *     assert "FOOBAR-FOOBAR-" == "foobar-FooBar-".replaceAll("(([fF][oO]{2})[bB]ar)", { Object[] it -> it[0].toUpperCase() })
     *
     *     Here,
     *          it[0] is the global string of the matched group
     *          it[1] is the first string in the matched group
     *          it[2] is the second string in the matched group
     * 
     * 
     *     assert "FOO-FOO-" == "foobar-FooBar-".replaceAll("(([fF][oO]{2})[bB]ar)", { x, y, z -> z.toUpperCase() })
     *
     *     Here,
     *          x is the global string of the matched group
     *          y is the first string in the matched group
     *          z is the second string in the matched group
     * </pre>
     *
     * @param self a String
     * @param regex the capturing regex
     * @param closure the closure to apply on each captured group
     * @return a String with replaced content
     */
    public static String replaceAll(String self, String regex, Closure closure) {
        Matcher matcher = Pattern.compile(regex).matcher(self);
        if (matcher.find()) {
            matcher.reset();
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                int count = matcher.groupCount();
                ArrayList groups = new ArrayList();
                for (int i = 0; i <= count; i++) {
                    groups.add(matcher.group(i));
                }
                matcher.appendReplacement(sb, String.valueOf(closure.call((Object[]) groups.toArray() )));
            }
            matcher.appendTail(sb);
            return sb.toString();
        } else {
            return self;
        }
    }

    /**
     * Turns a String into a regular expression pattern
     *
     * @param self a GString to convert into a regular expression
     * @return the regular expression pattern
     */
    public static Pattern negate(GString self) {
        return InvokerHelper.regexPattern(self.toString());
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
     * @param padding       the charaters used for padding
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
     * @param padding       the charaters used for padding
     * @return the String padded to the right
     */

    public static String padRight(String self, Number numberOfChars, String padding) {
        int numChars = numberOfChars.intValue();
        if (numChars <= self.length()) {
            return self;
        } else {
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
     * @param padding       the charaters used for padding
     * @return the String centered with padded character around
     */
    public static String center(String self, Number numberOfChars, String padding) {
        int numChars = numberOfChars.intValue();
        if (numChars <= self.length()) {
            return self;
        } else {
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
     * Support the subscript operator, e.g. matcher[index], for a regex Matcher.
     *
     * For an example using no group match, <code><pre>
     *    def p = /ab[d|f]/ 
     *    def m = "abcabdabeabf" =~ p 
     *    for (i in 0..<m.count) { 
     *        println( "m.groupCount() = " + m.groupCount())
     *        println( "  " + i + ": " + m[i] )   // m[i] is a String
     *    }
     * </pre></code>
     *
     * For an example using group matches, <code><pre>
     *    def p = /(?:ab([c|d|e|f]))/ 
     *    def m = "abcabdabeabf" =~ p 
     *    for (i in 0..<m.count) { 
     *        println( "m.groupCount() = " + m.groupCount())
     *        println( "  " + i + ": " + m[i] )   // m[i] is a List
     *    }
     * </pre></code>
     *
     * For another example using group matches, <code><pre>
     *    def m = "abcabdabeabfabxyzabx" =~ /(?:ab([d|x-z]+))/
     *    m.count.times { 
     *        println( "m.groupCount() = " + m.groupCount())
     *        println( "  " + it + ": " + m[it] )   // m[it] is a List
     *    }
     * </pre></code>
     *
     * @param matcher a Matcher
     * @param idx     an index
     * @return object a matched String if no groups matched, list of matched groups otherwise.
     */
    public static Object getAt(Matcher matcher, int idx) {
        try {
            int count = getCount(matcher);
            if (idx < -count || idx >= count) {
                throw new IndexOutOfBoundsException("index is out of range " + (-count) + ".." + (count - 1) + " (index = " + idx + ")");
            }
            idx = normaliseIndex(idx, count);
            matcher.reset();
            for (int i = 0; i <= idx; i++) {
                matcher.find();
            }

            if (hasGroup(matcher)) {
                // are we using groups?
                // yes, so return the specified group as list
                ArrayList list = new ArrayList(matcher.groupCount());
                for (int i = 0; i <= matcher.groupCount(); i++) {
                    list.add(matcher.group(i));
                }
                return list;
            } else {
                // not using groups, so return the nth
                // occurrence of the pattern
                return matcher.group();
            }
        }
        catch (IllegalStateException ex) {
            return null;
        }
    }

    /**
     * Set the position of the given Matcher to the given index.
     *
     * @param matcher a Matcher
     * @param idx the index number
     */
    public static void setIndex(Matcher matcher, int idx) {
        int count = getCount(matcher);
        if (idx < -count || idx >= count) {
            throw new IndexOutOfBoundsException("index is out of range " + (-count) + ".." + (count - 1) + " (index = " + idx + ")");
        }
        if (idx == 0) {
            matcher.reset();
        }
        else if (idx > 0) {
            matcher.reset();
            for (int i = 0; i < idx; i++) {
                matcher.find();
            }
        }
        else if (idx < 0) {
            matcher.reset();
            idx += getCount(matcher);
            for (int i = 0; i < idx; i++) {
                matcher.find();
            }
        }
    }

    /**
     * Find the number of Strings matched to the given Matcher.
     *
     * @param matcher a Matcher
     * @return int  the number of Strings matched to the given matcher.
     */
    public static int getCount(Matcher matcher) {
        int counter = 0;
        matcher.reset();
        while (matcher.find()) {
            counter++;
        }
        matcher.reset();
        return counter;
    }

    /**
     * Check whether a Matcher contains a group or not.
     *
     * @param matcher a Matcher
     * @return boolean  <code>true</code> if matcher contains at least one group.
     */
    public static boolean hasGroup(Matcher matcher) {
        return matcher.groupCount() > 0;
    }

    /**
     * Support the range subscript operator for a List
     *
     * @param self  a List
     * @param range a Range
     * @return a sublist based on range borders or a new list if range is reversed
     * @see java.util.List#subList(int, int)
     */
    public static List getAt(List self, IntRange range) {
        RangeInfo info = subListBorders(self.size(), range);
        List answer = self.subList(info.from, info.to);  // sublist is always exclusive, but Ranges are not
        if (info.reverse) {
            answer = reverse(answer);
        }
        return answer;
    }

    // helper method for getAt and putAt
    protected static RangeInfo subListBorders(int size, IntRange range){
        int from = normaliseIndex(InvokerHelper.asInt(range.getFrom()), size);
        int to = normaliseIndex(InvokerHelper.asInt(range.getTo()), size);
        boolean reverse = range.isReverse();
        if (from > to) {                        // support list[1..-1]
            int tmp = to;
            to = from;
            from = tmp;
            reverse = !reverse;
        }
        return new RangeInfo(from, to+1, reverse);
    }

    // helper method for getAt and putAt
    protected static RangeInfo subListBorders(int size, EmptyRange range){
        int from = normaliseIndex(InvokerHelper.asInt(range.getFrom()), size);
        return new RangeInfo(from, from, false);
    }

    /**
     * Allows a List to be used as the indices to be used on a List
     *
     * @param self    a List
     * @param indices a Collection of indices
     * @return a new list of the values at the given indices
     */
    public static List getAt(List self, Collection indices) {
        List answer = new ArrayList(indices.size());
        for (Iterator iter = indices.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (value instanceof Range) {
                answer.addAll(getAt(self, (Range) value));
            } else if (value instanceof List) {
                answer.addAll(getAt(self, (List) value));
            } else {
                int idx = InvokerHelper.asInt(value);
                answer.add(getAt(self, idx));
            }
        }
        return answer;
    }

    /**
     * Allows a List to be used as the indices to be used on a List
     *
     * @param self    an Array of Objects
     * @param indices a Collection of indices
     * @return a new list of the values at the given indices
     */
    public static List getAt(Object[] self, Collection indices) {
        List answer = new ArrayList(indices.size());
        for (Iterator iter = indices.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (value instanceof Range) {
                answer.addAll(getAt(self, (Range) value));
            } else if (value instanceof Collection) {
                answer.addAll(getAt(self, (Collection) value));
            } else {
                int idx = InvokerHelper.asInt(value);
                answer.add(getAt(self, idx));
            }
        }
        return answer;
    }

    /**
     * Allows a List to be used as the indices to be used on a CharSequence
     *
     * @param self    a CharSequence
     * @param indices a Collection of indices
     * @return a String of the values at the given indices
     */
    public static CharSequence getAt(CharSequence self, Collection indices) {
        StringBuffer answer = new StringBuffer();
        for (Iterator iter = indices.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (value instanceof Range) {
                answer.append(getAt(self, (Range) value));
            } else if (value instanceof Collection) {
                answer.append(getAt(self, (Collection) value));
            } else {
                int idx = InvokerHelper.asInt(value);
                answer.append(getAt(self, idx));
            }
        }
        return answer.toString();
    }

    /**
     * Allows a List to be used as the indices to be used on a String
     *
     * @param self    a String
     * @param indices a Collection of indices
     * @return a String of the values at the given indices
     */
    public static String getAt(String self, Collection indices) {
        return (String) getAt((CharSequence) self, indices);
    }

    /**
     * Allows a List to be used as the indices to be used on a Matcher
     *
     * @param self    a Matcher
     * @param indices a Collection of indices
     * @return a String of the values at the given indices
     */
    public static String getAt(Matcher self, Collection indices) {
        StringBuffer answer = new StringBuffer();
        for (Iterator iter = indices.iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (value instanceof Range) {
                answer.append(getAt(self, (Range) value));
            } else if (value instanceof Collection) {
                answer.append(getAt(self, (Collection) value));
            } else {
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
     * @param map  a Map
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
     * @param map          a Map
     * @param key          the key to lookup the value of
     * @param defaultValue the value to return and add to the map for this key if
     *                     there is no entry for the given key
     * @return the value of the given key or the default value, added to the map if the
     *         key did not exist
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
     *         including the ranges's to value
     */
    public static List getAt(Object[] array, Range range) {
        List list = Arrays.asList(array);
        return getAt(list, range);
    }
    
    public static List getAt(Object[] array, IntRange range) {
        List list = Arrays.asList(array);
        return getAt(list, range);
    }    
    
    public static List getAt(Object[] array, ObjectRange range) {
        List list = Arrays.asList(array);
        return getAt(list, range);
    }

    /**
     * Support the subscript operator for an Array
     *
     * @param array an Array of Objects
     * @param idx   an index
     * @return the value at the given index
     */
    public static Object getAt(Object[] array, int idx) {
        return array[normaliseIndex(idx, array.length)];
    }

    /**
     * Support the subscript operator for an Array
     *
     * @param array an Array of Objects
     * @param idx   an index
     * @param value an Object to put at the given index
     */
    public static void putAt(Object[] array, int idx, Object value) {
        if (value instanceof Number) {
            Class arrayComponentClass = array.getClass().getComponentType();

            if (!arrayComponentClass.equals(value.getClass())) {
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
     * @param idx  an index
     * @return the value at the given index
     */
    public static Object getAt(List self, int idx) {
        int size = self.size();
        int i = normaliseIndex(idx, size);
        if (i < size) {
            return self.get(i);
        } else {
            return null;
        }
    }

    /**
     * A helper method to allow lists to work with subscript operators
     *
     * @param self  a List
     * @param idx   an index
     * @param value the value to put at the given index
     */
    public static void putAt(List self, int idx, Object value) {
        int size = self.size();
        idx = normaliseIndex(idx, size);
        if (idx < size) {
            self.set(idx, value);
        } else {
            while (size < idx) {
                self.add(size++, null);
            }
            self.add(idx, value);
        }
    }


     /**
     * Support the range subscript operator for StringBuffer
     *
     * @param self  a StringBuffer
     * @param range a Range
     * @param value the object that's toString() will be inserted
     */
    public static void putAt(StringBuffer self, IntRange range, Object value) {
        RangeInfo info = subListBorders(self.length(), range);
        self.replace(info.from, info.to,  value.toString());
    }
    /**
     * Support the range subscript operator for StringBuffer
     *
     * @param self  a StringBuffer
     * @param range a Range
     * @param value the object that's toString() will be inserted
     */
    public static void putAt(StringBuffer self, EmptyRange range, Object value) {
        RangeInfo info = subListBorders(self.length(), range);
        self.replace(info.from, info.to,  value.toString());
    }

    /**
     * A helper method to allow lists to work with subscript operators
     *
     * @param self  a List
     * @param range  the subset of the list to set
     * @param value the values to put at the given sublist or a Collection of values
     */
    public static void putAt(List self, EmptyRange range, Object value) {
        RangeInfo info = subListBorders(self.size(), range);
        List sublist = self.subList(info.from,  info.to);
        sublist.clear();
        if (value instanceof Collection){
            Collection col = (Collection) value;
            if (col.size() == 0) return;
            sublist.addAll(col);
        } else {
            sublist.add(value);
        }
    }

    /**
     * A helper method to allow lists to work with subscript operators
     *
     * @param self  a List
     * @param range  the subset of the list to set
     * @param value the value to put at the given sublist or a Collection of values
     */
    public static void putAt(List self, IntRange range, Object value) {
        RangeInfo info = subListBorders(self.size(), range);
        List sublist = self.subList(info.from,  info.to);
        sublist.clear();
        if (value instanceof Collection){
            Collection col = (Collection) value;
            if (col.size() == 0) return;
            sublist.addAll(col);
        } else {
            sublist.add(value);
        }
    }

    /**
     * A helper method to allow lists to work with subscript operators
     *
     * @param self  a List
     * @param splice  the subset of the list to set
     * @param values the value to put at the given sublist
     * @deprecated replace with putAt(List self, Range range, List value)
     */
     public static void putAt(List self, List splice, List values) {
         List sublist = getSubList(self, splice);
         sublist.clear();
         sublist.addAll(values);
     }

    /**
     * A helper method to allow lists to work with subscript operators
     *
     * @param self  a List
     * @param splice  the subset of the list to set
     * @param value the value to put at the given sublist
     * @deprecated replace with putAt(List self, Range range, Object value)
     */
    public static void putAt(List self, List splice, Object value) {
        List sublist = getSubList(self, splice);
        sublist.clear();
        sublist.add(value);
    }

    // helper method for putAt(Splice)
    // todo: remove after putAt(Splice) gets deleted
    protected static List getSubList(List self, List splice) {
        int left = 0;
        int right = 0;
        boolean emptyRange = false;
        if (splice.size() == 2) {
            left = InvokerHelper.asInt(splice.get(0));
            right = InvokerHelper.asInt(splice.get(1));
        } else if (splice instanceof IntRange) {
            IntRange range = (IntRange) splice;
            left = range.getFromInt();
            right = range.getToInt();
        } else if (splice instanceof EmptyRange) {
            RangeInfo info = subListBorders(self.size(), (EmptyRange) splice);
            left = info.from;
            emptyRange = true;
        } else {
            throw new IllegalArgumentException("You must specify a list of 2 indexes to create a sub-list");
        }
        int size = self.size();
        left = normaliseIndex(left, size);
        right = normaliseIndex(right, size);
        List sublist = null;
        if (!emptyRange) {
            sublist = self.subList(left, right + 1);
        } else {
            sublist = self.subList(left, left);
        }
        return sublist;
    }

    /**
     * Support the subscript operator for a List
     *
     * @param self a Map
     * @param key  an Object as a key for the map
     * @return the value corresponding to the given key
     */
    public static Object getAt(Map self, Object key) {
        return self.get(key);
    }

    /**
     * A helper method to allow lists to work with subscript operators
     *
     * @param self a Map
     * @param key  an Object as a key for the map
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
     * @param coll     a Collection
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
            } else {
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
     * Returns the converted <code>SpreadList</code> of the given <code>self</code>.
     * <p>
     * This is the same method to <code>toSpreadList(List self)</code>.
     * <p>
     * For examples, if there is defined a function like as
     * <blockquote><pre>
     *     def fn(a, b, c, d) { return a + b + c + d }
     * </pre></blockquote>, then all of the following three have the same meaning.
     * <blockquote><pre>
     *     println fn(1, [2, 3].spread(), 4)
     *     println fn(1, *[2, 3], 4)
     *     println fn(1, 2, 3, 4)
     * </pre></blockquote>
     * <p>
     * </pre><br>
     * 
     * @param self a list to be converted into a spreadlist
     * @return a newly created SpreadList if this list is not null and its size is positive.
     */
    public static SpreadList spread(List self) {
        return toSpreadList(self);
    }

    /**
     * Returns the converted <code>SpreadList</code> of the given <code>self</code>.
     * <p>
     * This is the same method to <code>toSpreadList(Object[] self)</code>.
     * <p>
     * For examples, if there is defined a function like as
     * <blockquote><pre>
     *     def fn(a, b, c, d) { return a + b + c + d }
     * </pre></blockquote>, then all of the following three have the same meaning.
     * <blockquote><pre>
     *     println fn(([1, 2, 3] as Object[]).spread(), 4)
     *     println fn(*[1, 2, 3], 4)
     *     println fn(1, 2, 3, 4)
     * </pre></blockquote>
     * <p>
     * @param self an array of objects to be converted into a spreadlist
     * @return a newly created SpreadList if this array is not null and its size is positive.
     */
    public static SpreadList spread(Object[] self) {
        return toSpreadList(self);
    }

    /**
     * Returns the converted <code>SpreadList</code> of the given <code>self</code>.
     * <p>
     * For examples, if there is defined a function like as
     * <blockquote><pre>
     *     def fn(a, b, c, d) { return a + b + c + d }
     * </pre></blockquote>, then all of the following three have the same meaning.
     * <blockquote><pre>
     *     println fn(1, [2, 3].toSpreadList(), 4)
     *     println fn(1, *[2, 3], 4)
     *     println fn(1, 2, 3, 4)
     * </pre></blockquote>
     * <p>
     * @param self a list to be converted into a spreadlist
     * @return a newly created SpreadList if this list is not null and its size is positive.
     */
    public static SpreadList toSpreadList(List self) {
        if (self == null)
            throw new GroovyRuntimeException("Fail to convert Object[] to SpreadList, because it is null.");
        else
            return toSpreadList(self.toArray());
    }

    /**
     * Returns the converted <code>SpreadList</code> of the given <code>self</code>.
     * <p>
     * For examples, if there is defined a function like as 
     * <blockquote><pre>
     *     def fn(a, b, c, d) { return a + b + c + d }
     * </pre></blockquote>, then all of the following three have the same meaning. 
     * <blockquote><pre>
     *     println fn(([1, 2, 3] as Object[]).toSpreadList(), 4)
     *     println fn(*[1, 2, 3], 4)
     *     println fn(1, 2, 3, 4)
     * </pre></blockquote>
     * <p>
     * @param self an array of objects to be converted into a spreadlist
     * @return a newly created SpreadList if this array is not null and its size is positive.
     */
    public static SpreadList toSpreadList(Object[] self) {
        if (self == null)
            throw new GroovyRuntimeException("Fail to convert Object[] to SpreadList, because it is null.");
        else if (self.length == 0)
            throw new GroovyRuntimeException("Fail to convert Object[] to SpreadList, because its length is 0.");
        else
           return new SpreadList(self);
    }
    
    public static SpreadMap spread(Map self) {
        return toSpreadMap(self);
    }

    /**
     * Returns the converted <code>SpreadList</code> of the given <code>self</code>.
     * <p>
     * For examples, if there is defined a function like as
     * <blockquote><pre>
     *     def fn(a, b, c, d) { return a + b + c + d }
     * </pre></blockquote>, then all of the following three have the same meaning.
     * <blockquote><pre>
     *     println fn(a:1, [b:2, c:3].toSpreadMap(), d:4)
     *     println fn(a:1, *:[b:2, c:3], d:4)
     *     println fn(a:1, b:2, c:3, d:4)
     * </pre></blockquote>
     * <p>
     * @param self a list to be converted into a spreadlist
     * @return a newly created SpreadList if this list is not null and its size is positive.
     */
    public static SpreadMap toSpreadMap(Map self) {
        if (self == null)
            throw new GroovyRuntimeException("Fail to convert Map to SpreadMap, because it is null.");
        else
            return new SpreadMap(self);
    }

    public static SpreadMap toSpreadMap(Object[] self) {
        if (self == null)
            throw new GroovyRuntimeException("Fail to convert Object[] to SpreadMap, because it is null.");
        else if (self.length % 2 != 0)
            throw new GroovyRuntimeException("Fail to convert Object[] to SpreadMap, because it's size is not even.");
        else
            return new SpreadMap(self);
    }   

    /**
     * Sorts the given collection into a sorted list.
     *
     * @param self the collection to be sorted
     * @return the sorted collection as a List
     */
    public static List sort(Collection self) {
        List answer = asList(self);
        Collections.sort(answer, new NumberComparator());
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
     * @param self       a List
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
     * @param self       a collection to be sorted
     * @param comparator a Comparator used for the comparison
     * @return a newly created sorted List
     */
    public static List sort(Collection self, Comparator comparator) {
        return sort(asList(self), comparator);
    }

    /**
     * A convenience method for sorting a List using a closure as a comparator
     *
     * @param self    a List
     * @param closure a Closure used as a comparator
     * @return a sorted List
     */
    public static List sort(List self, Closure closure) {
        // use a comparator of one item or two
        int params = closure.getMaximumNumberOfParameters();
        if (params == 1) {
            Collections.sort(self, new OrderBy(closure));
        } else {
            Collections.sort(self, new ClosureComparator(closure));
        }
        return self;
    }

    /**
     * A convenience method for sorting a Collection using a closure as a comparator
     *
     * @param self    a Collection to be sorted
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
        } else {
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
     * @param left  the left Collection
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
     * @param left  a Collection
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
     * @param self   a Collection
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
     * @param left  a List
     * @param right a Collection
     * @return a List as an intersection of both collections
     */
    public static List intersect(List left, Collection right) {

        if (left.size() == 0)
            return new ArrayList();

        boolean nlgnSort = sameType(new Collection[]{left, right});

        ArrayList result = new ArrayList();
        //creates the collection to look for values.
        Collection pickFrom = (Collection) new TreeSet(new NumberComparator());
        ((TreeSet) pickFrom).addAll(left);

        for (Iterator iter = right.iterator(); iter.hasNext();) {
            final Object o = iter.next();
            if (pickFrom.contains(o))
                result.add(o);
        }
        return result;
    }

    /**
     * Returns <code>true</code> if the intersection of two collenctions is empty.
     *
     * @param left       a Collection
     * @param right      a Collection
     * @return boolean   <code>true</code> if the intersection of two collenctions is empty, <code>false</code> otherwise.
     */
    public static boolean disjoint(Collection left, Collection right) {

        if (left.size() == 0 || right.size() == 0)
            return true;

        boolean nlgnSort = sameType(new Collection[]{left, right});

        Collection pickFrom = (Collection) new TreeSet(new NumberComparator());
        ((TreeSet) pickFrom).addAll(right);

        for (Iterator iter = left.iterator(); iter.hasNext();) {
            final Object o = iter.next();
            if (pickFrom.contains(o))
                return false;
        }
        return true;
    }

    // Default comparator for numbers of different types.
    private static class NumberComparator implements Comparator {
        public int compare(Object o1, Object o2) {
             if (o1 instanceof Number && o2 instanceof Number) {
                 BigDecimal x1 = new BigDecimal("" + o1);
                 BigDecimal x2 = new BigDecimal("" + o2);
                 return x1.compareTo(x2);
            }
            else if (o1.getClass() == o2.getClass() && o1 instanceof Comparable) {
                return ((Comparable) o1).compareTo((Comparable) o2);
            }
            else {
                 int x1 = o1.hashCode();
                 int x2 = o2.hashCode();
                 return (x1 - x2);
            }
        }

        public boolean equals(Object obj) {
             return this.equals(obj);
        }
    }

    /**
     * Compare two Lists.
     * If numbers exits in the Lists, then they are compared as numbers,
     * for example 2 == 2L.
     *
     * @param  left      a List
     * @param  right     a List
     * @return boolean   <code>true</code> if two Lists equals, <code>false</code> otherwise.
     */
    public static boolean equals(List left, List right) {
        if (left == null && right == null)
             return true;
        else if (left == null && right != null)
             return false;
        else if (left != null && right == null)
             return false;
        else if (left.size() != right.size())
             return false;
        NumberComparator numberComparator = new NumberComparator(); 
        Iterator it1 = left.iterator(), it2 = right.iterator();
        for (; it1.hasNext() && it2.hasNext(); ) {
            Object o1 = it1.next();
            Object o2 = it2.next();
            if (Number.class.isInstance(o1) && Number.class.isInstance(o2)) {
                if (numberComparator.compare(o1, o2) != 0)
                    return false;
            }
            else {
                if (o1 == null) {
                    if (o1 != null)
                        return false;
                }
                else if (!o1.equals(o2))
                    return false;
            }
        }

        if (it1.hasNext() || it2.hasNext())
            return false;
        return true;
    }

    /**
     * Create a List composed of the elements of the first list minus the elements of the collection
     *
     * @param self     a List
     * @param removeMe a Collection of elements to remove
     * @return a List with the common elements removed
     */
    public static List minus(List self, Collection removeMe) {

        if (self.size() == 0)
            return new ArrayList();

        boolean nlgnSort = sameType(new Collection[]{self, removeMe});

        //we can't use the same tactic as for intersection
        //since AbstractCollection only does a remove on the first
        //element it encounter.

        Comparator numberComparator = new NumberComparator();

        if (nlgnSort && (self.get(0) instanceof Comparable)) {
            //n*log(n) version
            Set answer = null;
            if (Number.class.isInstance(self.get(0))) {
                BigDecimal zero = new BigDecimal("0.0");
                answer = new TreeSet(numberComparator);
                answer.addAll(self);
                for (Iterator it = self.iterator(); it.hasNext(); ) {
                    Object o = it.next();
                    if (Number.class.isInstance(o)) {
                        for (Iterator it2 = removeMe.iterator(); it2.hasNext(); ) {
                            Object o2 = it2.next();
                            if (Number.class.isInstance(o2)) {
                                if (numberComparator.compare(o, o2) == 0)
                                    answer.remove(o);
                            }
                        }
                    }
                    else {
                        if (removeMe.contains(o))
                            answer.remove(o);
                    }
                }
            }
            else {
                answer = new TreeSet(numberComparator);
                answer.addAll(self);
                answer.removeAll(removeMe);
            }

            List ansList = new ArrayList();
            for (Iterator it = self.iterator(); it.hasNext(); ) {
                Object o = it.next();
                if (answer.contains(o))
                    ansList.add(o);
            }
            return ansList;
        } else {
            //n*n version
            List tmpAnswer = new LinkedList(self);
            for (Iterator iter = tmpAnswer.iterator(); iter.hasNext();) {
                Object element = iter.next();
                //boolean removeElement = false;
                for (Iterator iterator = removeMe.iterator(); iterator.hasNext();) {
                    Object elt = iterator.next();
                    if (elt != null && numberComparator.compare(element, elt) == 0) {
                        iter.remove();
                    }
                }
            }

            //remove duplicates
            //can't use treeset since the base classes are different
            return new ArrayList(tmpAnswer);
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
     * @param self    a List
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
            } else if (element instanceof Map) {
                flatten(((Map) element).values(), addTo);
            } else {
                addTo.add(element);
            }
        }
        return addTo;
    }

    /**
     * Overloads the left shift operator to provide an easy way to append objects to a list
     *
     * @param self  a Collection
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
     * @param self  a String
     * @param value an Obect
     * @return a StringBuffer
     */
    public static StringBuffer leftShift(String self, Object value) {
        return new StringBuffer(self).append(value);
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
     * @param self  a StringBuffer
     * @param value a value to append
     * @return a StringBuffer
     */
    public static StringBuffer leftShift(StringBuffer self, Object value) {
        self.append(value);
        return self;
    }

    /**
     * Overloads the left shift operator to provide an append mechanism to add things to a writer
     *
     * @param self  a Writer
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
     * @param self     a Writer
     * @param writable an object implementing the Writable interface
     */
    public static void write(Writer self, Writable writable) throws IOException {
        writable.writeTo(self);
    }

    /**
     * Overloads the left shift operator to provide an append mechanism to add things to a stream
     *
     * @param self  an OutputStream
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
     * @param self  an OutputStream
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
        } else {
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

    public static Object getAt(boolean[] array, int idx) {
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

    public static Object getAt(boolean[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    public static Object getAt(byte[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    public static Object getAt(char[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    public static Object getAt(short[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    public static Object getAt(int[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    public static Object getAt(long[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    public static Object getAt(float[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    public static Object getAt(double[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    public static Object getAt(boolean[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }
    
    public static Object getAt(byte[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    public static Object getAt(char[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    public static Object getAt(short[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    public static Object getAt(int[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    public static Object getAt(long[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    public static Object getAt(float[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    public static Object getAt(double[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    public static Object getAt(boolean[] array, ObjectRange range) {
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

    public static Object getAt(boolean[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    public static void putAt(boolean[] array, int idx, Boolean newValue) {
        primitiveArrayPut(array, idx, newValue);
    }

    public static void putAt(byte[] array, int idx, Object newValue) {
        if (!(newValue instanceof Byte)) {
            Number n = (Number) newValue;
            newValue = new Byte(n.byteValue());
        }
        primitiveArrayPut(array, idx, newValue);
    }

    public static void putAt(char[] array, int idx, Object newValue) {
        if (newValue instanceof String) {
            String s = (String) newValue;
            if (s.length()!=1) throw new IllegalArgumentException("String of length 1 expected but got a bigger one");
            char c = s.charAt(0);
            newValue = new Character(c);
        }
        primitiveArrayPut(array, idx, newValue);
    }

    public static void putAt(short[] array, int idx, Object newValue) {
        if (!(newValue instanceof Short)) {
            Number n = (Number) newValue;
            newValue = new Short(n.shortValue());
        }
        primitiveArrayPut(array, idx, newValue);
    }

    public static void putAt(int[] array, int idx, Object newValue) {
        if (!(newValue instanceof Integer)) {
            Number n = (Number) newValue;
            newValue = new Integer(n.intValue());
        }
        primitiveArrayPut(array, idx, newValue);
    }

    public static void putAt(long[] array, int idx, Object newValue) {
        if (!(newValue instanceof Long)) {
            Number n = (Number) newValue;
            newValue = new Long(n.longValue());
        }
        primitiveArrayPut(array, idx, newValue);
    }

    public static void putAt(float[] array, int idx, Object newValue) {
        if (!(newValue instanceof Float)) {
            Number n = (Number) newValue;
            newValue = new Float(n.floatValue());
        }
        primitiveArrayPut(array, idx, newValue);
    }

    public static void putAt(double[] array, int idx, Object newValue) {
        if (!(newValue instanceof Double)) {
            Number n = (Number) newValue;
            newValue = new Double(n.doubleValue());
        }
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
                    int d = ((data[dIndex] & 0XFF) << 16) | ((data[dIndex + 1] & 0XFF) << 8) | (data[dIndex + 2] & 0XFF);

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
                } catch (IOException e) {
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
                    buffer.append((char) ((tmp >> (byteShift * 2)) & 0XFF));
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
            } else if (value instanceof List) {
                answer.addAll(primitiveArrayGet(self, (List) value));
            } else {
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
     * @param self  a String
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
     * @param left  a String
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
     * Appends a String
     *
     * @param left  a StringBuffer
     * @param value a String
     * @return a String
     */
    public static String plus(StringBuffer left, String value) {
        return left + value;
    }


    /**
     * Remove a part of a String
     *
     * @param left  a String
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
            } else {
                break;
            }
        }
        return answer;
    }

    /**
     * This method is called by the ++ operator for the class String.
     * It increments the last character in the given string. If the
     * character in the string is Character.MAX_VALUE a Character.MIN_VALUE
     * will be appended. The empty string is incremented to a string
     * consisting of the character Character.MIN_VALUE.
     *
     * @param self a String
     * @return an incremented String
     */
    public static String next(String self) {
        StringBuffer buffer = new StringBuffer(self);
        if (buffer.length()==0) {
            buffer.append(Character.MIN_VALUE);
        } else {
            char last = buffer.charAt(buffer.length()-1);
            if (last==Character.MAX_VALUE) {
                buffer.append(Character.MIN_VALUE);
            } else {
                char next = last;
                next++;
                buffer.setCharAt(buffer.length()-1,next);
            }
        }
        return buffer.toString();
    }

    /**
     * This method is called by the -- operator for the class String.
     * It decrements the last character in the given string. If the
     * character in the string is Character.MIN_VALUE it will be deleted.
     * The empty string can't be decremented.
     *
     * @param self a String
     * @return a String with a decremented digit at the end
     */
    public static String previous(String self) {
       StringBuffer buffer = new StringBuffer(self);
       if (buffer.length()==0) throw new IllegalArgumentException("the string is empty");
       char last = buffer.charAt(buffer.length()-1);
       if (last==Character.MIN_VALUE) {
           buffer.deleteCharAt(buffer.length()-1);
       } else {
            char next = last;
            next--;
            buffer.setCharAt(buffer.length()-1,next);
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
    
    /**
     * Executes the command specified by the <code>String</code> array that is the parameter.
     * The first item in the array is the command the others are the parameters. For more
     * control over the process mechanism in JDK 1.5 you can use
     * <code>java.lang.ProcessBuilder</code>.
     *
     * @param commandArray an array of <code>String<code> containing the command name and
     * parameters as separate items in the array.
     * @return the Process which has just started for this command line string.
     */
    public static Process execute(final String[] commandArray) throws IOException {
        return Runtime.getRuntime().exec(commandArray) ;
    }

    /**
     * Executes the command specified by the <code>self</code> with environments <code>envp</code>
     * under the working directory <code>dir</code>.
     * For more control over the process mechanism in JDK 1.5 you can use <code>java.lang.ProcessBuilder</code>.
     *
     * @param   self      a command line String to be executed.
     * @param   envp      an array of Strings, each element of which
     *                    has environment variable settings in the format
     *                    <i>name</i>=<i>value</i>, or
     *                    <tt>null</tt> if the subprocess should inherit
     *                    the environment of the current process.
     * @param   dir       the working directory of the subprocess, or
     *                    <tt>null</tt> if the subprocess should inherit
     *                    the working directory of the current process.
     * @return   the Process which has just started for this command line string.
     *
     */
    public static Process execute(String self,  final String[] envp, File dir) throws IOException {
        return Runtime.getRuntime().exec(self, envp, dir) ;
    }

    /**
     * Executes the command specified by the <code>String</code> list that is the parameter.
     * The first item in the array is the command the others are the parameters. All entries
     * must be <code>String</code>s.  For more control over the process mechanism in JDK 1.5 you
     * can use <code>java.lang.ProcessBuilder</code>.
     *
     * @param commandList a list of <code>String<code> containing the command name and
     * parameters as separate items in the list.
     * @return the Process which has just started for this command line string.
     */
    public static Process execute(final List commandList) throws IOException {
      final String[] commandArray = new String[commandList.size()] ;
      Iterator it = commandList.iterator();
      for (int i = 0; it.hasNext(); ++i) {
          commandArray[i] = it.next().toString();
      }
      return execute(commandArray) ;
    }

    /**
     * Executes the command specified by the <code>self</code> with environments <code>envp</code>
     * under the working directory <code>dir</code>.
     * For more control over the process mechanism in JDK 1.5 you can use <code>java.lang.ProcessBuilder</code>.
     *
     * @param   self      a command line String to be executed.
     * @param   envp      a List of Strings, each member of which
     *                    has environment variable settings in the format
     *                    <i>name</i>=<i>value</i>, or
     *                    <tt>null</tt> if the subprocess should inherit
     *                    the environment of the current process.
     * @param   dir       the working directory of the subprocess, or
     *                    <tt>null</tt> if the subprocess should inherit
     *                    the working directory of the current process.
     * @return   the Process which has just started for this command line string.
     *
     */
    public static Process execute(String self, final List envp, File dir) throws IOException {
      final String[] commandArray = new String[envp.size()] ;
      Iterator it = envp.iterator();
      for (int i = 0; it.hasNext(); ++i) {
          commandArray[i] = it.next().toString();
      }
      return execute(self, commandArray, dir);
    }

    /**
     * Repeat a String a certain number of times
     *
     * @param self   a String to be repeated
     * @param factor the number of times the String should be repeated
     * @return a String composed of a repeatition
     * @throws IllegalArgumentException if the number of repeatition is &lt; 0
     */
    public static String multiply(String self, Number factor) {
        int size = factor.intValue();
        if (size == 0)
            return "";
        else if (size < 0) {
            throw new IllegalArgumentException("multiply() should be called with a number of 0 or greater not: " + size);
        }
        StringBuffer answer = new StringBuffer(self);
        for (int i = 1; i < size; i++) {
            answer.append(self);
        }
        return answer.toString();
    }
    
    /**
     * Returns the string representation of the given map with bracket boundaries.
     *
     * @param self a Map
     * @return the string representation
     */
    public static String toString(Map self) {
        return toMapString(self);
    }

    /**
     * Returns the string representation of the given map with bracket boundaries.
     *
     * @param self a Map
     * @return the string representation
     */
    public static String toMapString(Map self) {
        return (self == null) ? "null" : InvokerHelper.toMapString(self);
    }

    /**
     * Returns the string representation of the given collection with the bracket boundaries.
     *
     * @param self a Collection
     * @return the string representation
     */
    public static String toString(Collection self) {
        return toListString(self);
    }

    /**
     * Returns the string representation of the given collection with the bracket boundaries.
     *
     * @param self a Collection
     * @return the string representation
     */
    public static String toListString(Collection self) {
        return (self == null) ? "null" : InvokerHelper.toListString(self);
    }

    /**
     * Returns the string representation of the given array with the brace boundaries.
     *
     * @param self an Object[]
     * @return the string representation
     */
    public static String toString(Object[] self) {
        return toArrayString(self);
    }

    /**
     * Returns the string representation of the given array with the brace boundaries.
     *
     * @param self an Object[]
     * @return the string representation
     */
    public static String toArrayString(Object[] self) {
        return (self == null) ? "null" : InvokerHelper.toArrayString(self);
    }


    protected static String toString(Object value) {
        if (value instanceof Map)
            return toMapString((Map)value);
        else if (value instanceof Collection)
            return toListString((Collection)value);
        else if (value instanceof Object[])
            return toArrayString((Object[])value);
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
     * @param left  a Character
     * @param right a Number
     * @return the addition of the Character and the Number
     */
    public static Number plus(Character left, Number right) {
        return plus(new Integer(left.charValue()), right);
    }

    /**
     * Add a Number and a Character
     *
     * @param left  a Number
     * @param right a Character
     * @return the addition of the Character and the Number
     */
    public static Number plus(Number left, Character right) {
        return plus(left, new Integer(right.charValue()));
    }

    /**
     * Add two Characters
     *
     * @param left  a Character
     * @param right a Character
     * @return the addition of both Characters
     */
    public static Number plus(Character left, Character right) {
        return plus(new Integer(left.charValue()), right);
    }

    /**
     * Add two numbers and return the result.
     *
     * @param left  a Number
     * @param right another Number to add
     * @return the addition of both Numbers
     */
    public static Number plus(Number left, Number right) {
        return NumberMath.add(left, right);
    }

    /**
     * Compare a Character and a Number
     *
     * @param left  a Character
     * @param right a Number
     * @return the result of the comparison
     */
    public static int compareTo(Character left, Number right) {
        return compareTo(new Integer(left.charValue()), right);
    }

    /**
     * Compare a Number and a Character
     *
     * @param left  a Number
     * @param right a Character
     * @return the result of the comparison
     */
    public static int compareTo(Number left, Character right) {
        return compareTo(left, new Integer(right.charValue()));
    }

    /**
     * Compare two Characters
     *
     * @param left  a Character
     * @param right a Character
     * @return the result of the comparison
     */
    public static int compareTo(Character left, Character right) {
        return compareTo(new Integer(left.charValue()), right);
    }

    /**
     * Compare two Numbers
     *
     * @param left  a Number
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
     * @param left  a Character
     * @param right a Number
     * @return the addition of the Character and the Number
     */
    public static Number minus(Character left, Number right) {
        return minus(new Integer(left.charValue()), right);
    }

    /**
     * Subtract a Character from a Number
     *
     * @param left  a Number
     * @param right a Character
     * @return the addition of the Character and the Number
     */
    public static Number minus(Number left, Character right) {
        return minus(left, new Integer(right.charValue()));
    }

    /**
     * Subtraction two Characters
     *
     * @param left  a Character
     * @param right a Character
     * @return the addition of both Characters
     */
    public static Number minus(Character left, Character right) {
        return minus(new Integer(left.charValue()), right);
    }

    /**
     * Substraction of two Numbers
     *
     * @param left  a Number
     * @param right another Number to substract to the first one
     * @return the substraction
     */
    public static Number minus(Number left, Number right) {
        return NumberMath.subtract(left, right);
    }

    /**
     * Multiply a Character by a Number
     *
     * @param left  a Character
     * @param right a Number
     * @return the multiplication of both
     */
    public static Number multiply(Character left, Number right) {
        return multiply(new Integer(left.charValue()), right);
    }

    /**
     * Multiply a Number by a Character
     *
     * @param left  a Number
     * @param right a Character
     * @return the multiplication of both
     */
    public static Number multiply(Number left, Character right) {
        return multiply(left, new Integer(right.charValue()));
    }

    /**
     * Multiply two Characters
     *
     * @param left  a Character
     * @param right another Character
     * @return the multiplication of both
     */
    public static Number multiply(Character left, Character right) {
        return multiply(new Integer(left.charValue()), right);
    }

    /**
     * Multiply two Numbers
     *
     * @param left  a Number
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
     * @param self     a Number
     * @param exponent a Number exponent
     * @return a Number to the power of a certain exponent
     */
    public static Number power(Number self, Number exponent) {
	double base, exp, answer;
	base = self.doubleValue();
	exp = exponent.doubleValue();

        answer = Math.pow(base, exp);
	if ((double)((int)answer) == answer) {
            return new Integer((int)answer);
	}
        else if ((double)((long)answer) == answer) {
            return new Long((long)answer);
	}
	else {
            return new Double(answer);
	}
    }

    /**
     * Divide a Character by a Number
     *
     * @param left  a Character
     * @param right a Number
     * @return the multiplication of both
     */
    public static Number div(Character left, Number right) {
        return div(new Integer(left.charValue()), right);
    }

    /**
     * Divide a Number by a Character
     *
     * @param left  a Number
     * @param right a Character
     * @return the multiplication of both
     */
    public static Number div(Number left, Character right) {
        return div(left, new Integer(right.charValue()));
    }

    /**
     * Divide two Characters
     *
     * @param left  a Character
     * @param right another Character
     * @return the multiplication of both
     */
    public static Number div(Character left, Character right) {
        return div(new Integer(left.charValue()), right);
    }

    /**
     * Divide two Numbers
     *
     * @param left  a Number
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
     * @param left  a Character
     * @param right a Number
     * @return the integer division of both
     */
    public static Number intdiv(Character left, Number right) {
        return intdiv(new Integer(left.charValue()), right);
    }

    /**
     * Integer Divide a Number by a Character
     *
     * @param left  a Number
     * @param right a Character
     * @return the integer division of both
     */
    public static Number intdiv(Number left, Character right) {
        return intdiv(left, new Integer(right.charValue()));
    }

    /**
     * Integer Divide two Characters
     *
     * @param left  a Character
     * @param right another Character
     * @return the integer division of both
     */
    public static Number intdiv(Character left, Character right) {
        return intdiv(new Integer(left.charValue()), right);
    }

    /**
     * Integer Divide two Numbers
     *
     * @param left  a Number
     * @param right another Number
     * @return a Number (an Integer) resulting of the integer division operation
     */
    public static Number intdiv(Number left, Number right) {
        return NumberMath.intdiv(left, right);
    }

    /**
     * Bitwise OR together two numbers
     *
     * @param left  a Number
     * @param right another Number to bitwise OR
     * @return the bitwise OR of both Numbers
     */
    public static Number or(Number left, Number right) {
        return NumberMath.or(left, right);
    }

    /**
     * Bitwise AND together two Numbers
     *
     * @param left  a Number
     * @param right another Number to bitwse AND
     * @return the bitwise AND of both Numbers
     */
    public static Number and(Number left, Number right) {
        return NumberMath.and(left, right);
    }

     /**
     * Bitwise XOR together two Numbers
     *
     * @param left  a Number
     * @param right another Number to bitwse XOR
     * @return the bitwise XOR of both Numbers
     */
    public static Number xor(Number left, Number right) {
        return NumberMath.xor(left, right);
    }

    /**
     * Performs a division modulus operation
     *
     * @param left  a Number
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
     * @param self    a Number
     * @param closure the closure to call a number of times
     */
    public static void times(Number self, Closure closure) {
        for (int i = 0, size = self.intValue(); i < size; i++) {
            closure.call(new Integer(i));
            if (closure.getDirective() == Closure.DONE) {
                break;
            }
        }
    }

    /**
     * Iterates from this number up to the given number
     *
     * @param self    a Number
     * @param to      another Number to go up to
     * @param closure the closure to call
     */
    public static void upto(Number self, Number to, Closure closure) {
        int self1 = self.intValue();
        int to1 = to.intValue();
        if (self1 <= to1) {
            for (int i = self1; i <= to1; i++) {
                closure.call(new Integer(i));
            }
        }
        else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to +")");
    }

    public static void upto(long self, Number to, Closure closure) {
        long to1 = to.longValue();
        if (self <= to1) {
            for (long i = self; i <= to1; i++) {
                closure.call(new Long(i));
            }
        }
        else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to +")");
    }

    public static void upto(Long self, Number to, Closure closure) {
        long self1 = self.longValue();
        long to1 = to.longValue();
        if (self1 <= to1) {
            for (long i = self1; i <= to1; i++) {
                closure.call(new Long(i));
            }
        }
        else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to +")");
    }

    public static void upto(float self, Number to, Closure closure) {
        float to1 = to.floatValue();
        if (self <= to1) {
            for (float i = self; i <= to1; i++) {
                closure.call(new Float(i));
            }
        }
        else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to +")");
    }

    public static void upto(Float self, Number to, Closure closure) {
        float self1 = self.floatValue();
        float to1 = to.floatValue();
        if (self1 <= to1) {
            for (float i = self1; i <= to1; i++) {
                closure.call(new Float(i));
            }
        }
        else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to +")");
    }

    public static void upto(Double self, Number to, Closure closure) {
        double self1 = self.doubleValue();
        double to1 = to.doubleValue();
        if (self1 <= to1) {
            for (double i = self1; i <= to1; i++) {
                closure.call(new Double(i));
            }
        }
        else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to +")");
    }

    public static void upto(BigInteger self, Number to, Closure closure) {
        if (to instanceof BigDecimal) {
            final BigDecimal one = new BigDecimal("1.0");
            BigDecimal self1 = new BigDecimal(self);
            BigDecimal to1 = (BigDecimal) to;
            if (self1.compareTo(to1) <= 0) {
                for (BigDecimal i = self1; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            }
            else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to +")");
        }
        else if (to instanceof BigInteger) {
            final BigInteger one = new BigInteger("1");
            BigInteger to1 = (BigInteger) to;
            if (self.compareTo(to1) <= 0) {
                for (BigInteger i = self; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            }
            else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to +")");
        }
        else {
            final BigInteger one = new BigInteger("1");
            BigInteger to1 = new BigInteger("" + to);
            if (self.compareTo(to1) <= 0) {
                for (BigInteger i = self; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            }
            else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to +")");
        }
    }

    public static void upto(BigDecimal self, Number to, Closure closure) {
        final BigDecimal one = new BigDecimal("1.0");
        if (to instanceof BigDecimal) {
            BigDecimal to1 = (BigDecimal) to;
            if (self.compareTo(to1) <= 0) {
                for (BigDecimal i = self; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            }
            else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to +")");
        }
        else if (to instanceof BigInteger) {
            BigDecimal to1 = new BigDecimal((BigInteger) to);
            if (self.compareTo(to1) <= 0) {
                for (BigDecimal i = self; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            }
            else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to +")");
        }
        else {
            BigDecimal to1 = new BigDecimal(""+to);
            if (self.compareTo(to1) <= 0) {
                for (BigDecimal i = self; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            }
            else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to +")");
        }
    }

    /**
     * Iterates from this number down to the given number
     *
     * @param self    a Number
     * @param to      another Number to go down to
     * @param closure the closure to call
     */
    public static void downto(Number self, Number to, Closure closure) {
        int self1 = self.intValue();
        int to1 = to.intValue();
        if (self1 >= to1) {
            for (int i = self1; i >= to1; i--) {
                closure.call(new Integer(i));
            }
        }
        else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to +")");
    }

    public static void downto(long self, Number to, Closure closure) {
        long to1 = to.longValue();
        if (self >= to1) {
            for (long i = self; i >= to1; i--) {
                closure.call(new Long(i));
            }
        }
        else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to +")");
    }

    public static void downto(Long self, Number to, Closure closure) {
        long self1 = self.longValue();
        long to1 = to.longValue();
        if (self1 >= to1) {
            for (long i = self1; i >= to1; i--) {
                closure.call(new Long(i));
            }
        }
        else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to +")");
    }

    public static void downto(float self, Number to, Closure closure) {
        float to1 = to.floatValue();
        if (self >= to1) {
            for (float i = self; i >= to1; i--) {
               closure.call(new Float(i));
            }
        }
        else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to +")");
    }

    public static void downto(Float self, Number to, Closure closure) {
        float self1 = self.floatValue();
        float to1 = to.floatValue();
        if (self1 >= to1) {
            for (float i = self1; i >= to1; i--) {
               closure.call(new Float(i));
            }
        }
        else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to +")");
    }

    public static void downto(double self, Number to, Closure closure) {
        double to1 = to.doubleValue();
        if (self >= to1) {
            for (double i = self; i >= to1; i--) {
                closure.call(new Double(i));
            }
        }
        else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to +")");
    }

    public static void downto(Double self, Number to, Closure closure) {
        double self1 = self.doubleValue();
        double to1 = to.doubleValue();
        if (self1 >= to1) {
            for (double i = self1; i >= to1; i--) {
                closure.call(new Double(i));
            }
        }
        else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to +")");
    }

    public static void downto(BigInteger self, Number to, Closure closure) {
        if (to instanceof BigDecimal) {
            final BigDecimal one = new BigDecimal("1.0");
            BigDecimal to1 = (BigDecimal) to;
            if (self.compareTo(to1) >= 0) {
                for (BigDecimal i = new BigDecimal(self); i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i);
                }
            }
            else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to +")");
        }
        else if (to instanceof BigInteger) {
            final BigInteger one = new BigInteger("1");
            BigInteger to1 = (BigInteger) to;
            if (self.compareTo(to1) >= 0) {
                for (BigInteger i = self; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i);
                }
            }
            else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to +")");
        }
        else {
            final BigInteger one = new BigInteger("1");
            BigInteger to1 = new BigInteger("" + to);
            if (self.compareTo(to1) >= 0) {
                for (BigInteger i = self; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i);
                }
            }
            else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to +")");
        }
    }

    public static void downto(BigDecimal self, Number to, Closure closure) {
        final BigDecimal one = new BigDecimal("1.0");
        if (to instanceof BigDecimal) {
            BigDecimal to1 = (BigDecimal) to;
            if (self.compareTo(to1) >= 0) {
                for (BigDecimal i = self; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i);
                }
            }
            else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to +")");
        }
        else if (to instanceof BigInteger) {
            BigDecimal to1 = new BigDecimal((BigInteger) to);
            if (self.compareTo(to1) >= 0) {
                for (BigDecimal i = self; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i);
                }
            }
            else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to +")");
        }
        else {
            BigDecimal to1 = new BigDecimal(""+to);
            if (self.compareTo(to1) >= 0) {
                for (BigDecimal i = self; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i);
                }
            }
            else
                throw new GroovyRuntimeException("Infinite loop in " + self +".downto(" + to +")");
        }
    }

    /**
     * Iterates from this number up to the given number using a step increment
     *
     * @param self       a Number to start with
     * @param to         a Number to go up to
     * @param stepNumber a Number representing the step increment
     * @param closure    the closure to call
     */
    public static void step(Number self, Number to, Number stepNumber, Closure closure) {
        if (self instanceof BigDecimal || to instanceof BigDecimal || stepNumber instanceof BigDecimal) {
            final BigDecimal zero = new BigDecimal("0.0");
            BigDecimal self1 = (self instanceof BigDecimal) ? (BigDecimal) self : new BigDecimal("" + self);
            BigDecimal to1 = (to instanceof BigDecimal) ? (BigDecimal) to : new BigDecimal("" + to);
            BigDecimal stepNumber1 = (stepNumber instanceof BigDecimal) ? (BigDecimal) stepNumber : new BigDecimal("" + stepNumber);
            if (stepNumber1.compareTo(zero) > 0 && to1.compareTo(self1) > 0) {
                for (BigDecimal i = self1; i.compareTo(to1) < 0; i = i.add(stepNumber1)) {
                    closure.call(i);
                }
            }
            else if (stepNumber1.compareTo(zero) < 0 && to1.compareTo(self1) < 0) {
                for (BigDecimal i = self1; i.compareTo(to1) > 0; i = i.add(stepNumber1)) {
                    closure.call(i);
                }
            }
            else
                throw new GroovyRuntimeException("Infinite loop in " + self1 + ".step(" + to1 + ", " + stepNumber1 + ")");
        }
        else if (self instanceof BigInteger || to instanceof BigInteger || stepNumber instanceof BigInteger) {
            final BigInteger zero = new BigInteger("0");
            BigInteger self1 = (self instanceof BigInteger) ? (BigInteger) self : new BigInteger("" + self);
            BigInteger to1 = (to instanceof BigInteger) ? (BigInteger) to : new BigInteger("" + to);
            BigInteger stepNumber1 = (stepNumber instanceof BigInteger) ? (BigInteger) stepNumber : new BigInteger("" + stepNumber);
            if (stepNumber1.compareTo(zero) > 0 && to1.compareTo(self1) > 0) {
                for (BigInteger i = self1; i.compareTo(to1) < 0; i = i.add(stepNumber1)) {
                    closure.call(i);
                }
            }
            else if (stepNumber1.compareTo(zero) < 0 && to1.compareTo(self1) < 0) {
                for (BigInteger i = self1; i.compareTo(to1) > 0; i = i.add(stepNumber1)) {
                    closure.call(i);
                }
            }
            else
                throw new GroovyRuntimeException("Infinite loop in " + self1 + ".step(" + to1 + ", " + stepNumber1 + ")");
        }
        else {
            int self1 = self.intValue();
            int to1 = to.intValue();
            int stepNumber1 = stepNumber.intValue();
            if (stepNumber1 > 0 && to1 > self1) {
                for (int i = self1; i < to1; i += stepNumber1) {
                    closure.call(new Integer(i));
                }
            }
            else if (stepNumber1 < 0 && to1 < self1) {
                for (int i = self1; i > to1; i += stepNumber1) {
                    closure.call(new Integer(i));
                }
            }
            else
                throw new GroovyRuntimeException("Infinite loop in " + self1 + ".step(" + to1 + ", " + stepNumber1 + ")");
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
     * Helper method to create an object input stream from the given file.
     *
     * @param file a file
     * @return an object input stream
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static ObjectInputStream newObjectInputStream(File file) throws FileNotFoundException, IOException {
        return new ObjectInputStream(new FileInputStream(file));
    }

    /**
     * Iterates through the given file object by object
     *
     * @param self    a File
     * @param closure a closure
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void eachObject(File self, Closure closure) throws IOException, ClassNotFoundException {
        eachObject(newObjectInputStream(self), closure);
    }

    /**
     * Iterates through the given object stream object by object
     *
     * @param self    an ObjectInputStream
     * @param closure a closure
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void eachObject(ObjectInputStream ois, Closure closure) throws IOException, ClassNotFoundException {
        try {
            while (true) {
                try {
                    Object obj = ois.readObject();
                    // we allow null objects in the object stream
                    closure.call(obj);
                } catch (EOFException e) {
                    break;
                }
            }
            ois.close();
        } catch (ClassNotFoundException e) {
            try {
                ois.close();
            } catch (Exception e2) {
                // ignore as we're already throwing
            }
            throw e;
        } catch (IOException e) {
            try {
               ois.close();
            } catch (Exception e2) {
               // ignore as we're already throwing
            }
            throw e;
        }
    }

    /**
     * Iterates through the given file line by line
     *
     * @param self    a File
     * @param closure a closure
     * @throws IOException
     */
    public static void eachLine(File self, Closure closure) throws IOException {
        eachLine(newReader(self), closure);
    }

    /**
     * Iterates through the given reader line by line
     *
     * @param self    a Reader
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
                } else {
                    closure.call(line);
                }
            }
            br.close();
        } catch (IOException e) {
            if (self != null) {
                try {
                    br.close();
                } catch (Exception e2) {
                    // ignore as we're already throwing
                }
                throw e;
            }
        }
    }

    /**
     * Iterates through the given file line by line, splitting on the seperator
     *
     * @param self    a File
     * @param sep     a String separator
     * @param closure a closure
     * @throws IOException
     */
    public static void splitEachLine(File self, String sep, Closure closure) throws IOException {
        splitEachLine(newReader(self), sep, closure);
    }

    /**
     * Iterates through the given reader line by line, splitting on the seperator
     *
     * @param self    a Reader
     * @param sep     a String separator
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
                } else {
                    List vals = Arrays.asList(line.split(sep));
                    args.clear();
                    args.add(vals);
                    closure.call(args);
                }
            }
            br.close();
        } catch (IOException e) {
            if (self != null) {
                try {
                    br.close();
                } catch (Exception e2) {
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
     * @param file    the file whose content we want to read
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
     * @param url     URL to read content from
     * @param charset opens the stream with a specified charset
     * @return the text from that URL
     * @throws IOException
     */
    public static String getText(URL url, String charset) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), charset));
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
     * @param is      an input stream
     * @param charset opens the stream with a specified charset
     * @return the text from that URL
     * @throws IOException
     */
    public static String getText(InputStream is, String charset) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
        return getText(reader);
    }

    /**
     * Reads the content of the Reader and returns it as a String
     *
     * @param reader a Reader whose content we want to read
     * @return a String containing the content of the buffered reader
     * @throws IOException
     */
    public static String getText(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        return getText(bufferedReader);
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
            // appends buffer
            answer.append(charBuffer, 0, nbCharRead);
        }
        reader.close();
        return answer.toString();
    }

    /**
     * Write the text and append a new line (depending on the platform line-ending)
     *
     * @param writer a BufferedWriter
     * @param line   the line to write
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
     * @param file    a File
     * @param text    the text to write to the File
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
     * @param file    a File
     * @param text    the text to append at the end of the File
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
     * This method is used to throw useful exceptions when the eachFile* and eachDir closure methods
     * are used incorrectly.
     *
     * @param dir The directory to check
     * @throws FileNotFoundException Thrown if the given directory does not exist
     * @throws IllegalArgumentException Thrown if the provided File object does not represent a directory
     */
    private static void checkDir(File dir) throws FileNotFoundException, IllegalArgumentException {
        if (!dir.exists())
          throw new FileNotFoundException(dir.getAbsolutePath());
        if (!dir.isDirectory())
          throw new IllegalArgumentException("The provided File object is not a directory: " + dir.getAbsolutePath());
    }

    /**
     * Invokes the closure for each file in the given directory
     *
     * @param self    a File
     * @param closure a closure
     * @throws FileNotFoundException Thrown if the given directory does not exist
     * @throws IllegalArgumentException Thrown if the provided File object does not represent a directory
     */
    public static void eachFile(File self, Closure closure) throws FileNotFoundException, IllegalArgumentException {
        checkDir(self);
        File[] files = self.listFiles();
        for (int i = 0; i < files.length; i++) {
            closure.call(files[i]);
        }
    }

    /**
     * Invokes the closure for each file in the given directory and recursively.
     * It is a depth-first exploration, directories are included in the search.
     *
     * @param self    a File
     * @param closure a closure
     * @throws FileNotFoundException Thrown if the given directory does not exist
     * @throws IllegalArgumentException Thrown if the provided File object does not represent a directory
     */
    public static void eachFileRecurse(File self, Closure closure) throws FileNotFoundException, IllegalArgumentException {
        checkDir(self);
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
     * Invokes the closure for each directory in the given directory,
     * ignoring regular files.
     *
     * @param self    a directory
     * @param closure a closure
     * @throws FileNotFoundException Thrown if the given directory does not exist
     * @throws IllegalArgumentException Thrown if the provided File object does not represent a directory
     */
    public static void eachDir(File self, Closure closure) throws FileNotFoundException, IllegalArgumentException {
        checkDir(self);
        File[] files = self.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                closure.call(files[i]);
            }
        }
    }

    /**
     * Invokes the closure for each file matching the given filter in the given directory
     * - calling the isCase() method used by switch statements.  This method can be used
     * with different kinds of filters like regular expresions, classes, ranges etc.
     *
     * @param self   a file
     * @param filter the filter to perform on the directory (using the isCase(object) method)
     * @param closure
     * @throws FileNotFoundException Thrown if the given directory does not exist
     * @throws IllegalArgumentException Thrown if the provided File object does not represent a directory
     */
    public static void eachFileMatch(File self, Object filter, Closure closure) throws FileNotFoundException, IllegalArgumentException {
        checkDir(self);
        File[] files = self.listFiles();
        MetaClass metaClass = InvokerHelper.getMetaClass(filter);
        for (int i = 0; i < files.length; i++) {
            if (InvokerHelper.asBool(metaClass.invokeMethod(filter, "isCase", files[i].getName()))) {
                closure.call(files[i]);
            }
        }
    }

    /**
     * Allow simple syntax for using timers.
     * 
     * @param timer a timer object
     * @param delay the delay in milliseconds before running the closure code
     * @param closure
     */
    public static void runAfter(Timer timer, int delay, final Closure closure) {
        TimerTask timerTask = new TimerTask() {
            public void run() {
                closure.call();
            }
        };
        timer.schedule(timerTask, delay);
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
     * @param file    a File
     * @param charset the charset with which we want to write in the File
     * @return a BufferedReader
     * @throws FileNotFoundException        if the File was not found
     * @throws UnsupportedEncodingException if the encoding specified is not supported
     */
    public static BufferedReader newReader(File file, String charset)
            throws FileNotFoundException, UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
    }

    /**
     * Provides a reader for an arbitrary input stream
     *
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
     * @param file   a File
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
     * @param file    a File
     * @param charset the name of the encoding used to write in this file
     * @param append  true if in append mode
     * @return a BufferedWriter
     * @throws FileNotFoundException
     */
    public static BufferedWriter newWriter(File file, String charset, boolean append) throws IOException {
        if (append) {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), charset));
        } else {
            // first write the Byte Order Mark for Unicode encodings
            FileOutputStream stream = new FileOutputStream(file);
            if ("UTF-16BE".equals(charset)) {
                writeUtf16Bom(stream, true);
            } else if ("UTF-16LE".equals(charset)) {
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
     * @param stream    the FileOuputStream to write the BOM to
     * @param bigEndian true if UTF 16 Big Endian or false if Low Endian
     * @throws IOException
     */
    private static void writeUtf16Bom(FileOutputStream stream, boolean bigEndian) throws IOException {
        if (bigEndian) {
            stream.write(-2);
            stream.write(-1);
        } else {
            stream.write(-1);
            stream.write(-2);
        }
    }

    /**
     * Helper method to create a new BufferedWriter for a file and then
     * passes it into the closure and ensures it is closed again afterwords
     *
     * @param file    a File
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
     * @param file    a File
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
     * @param file    a File
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
     * @param file    a File
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
     * @param writer  the writer which is used and then closed
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
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
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
     * @param writer  the writer which is used and then closed
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
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
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
     * @param stream  the stream which is used and then closed
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
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
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
     * @param stream  a stream
     * @param closure a closure
     * @throws IOException
     */
    public static void eachLine(InputStream stream, Closure closure) throws IOException {
        eachLine(new InputStreamReader(stream), closure);
    }

    /**
     * Iterates through the lines read from the URL's associated input stream
     *
     * @param url     a URL to open and read
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
     * @param stream  the stream which is used and then closed
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
     * @param stream  the stream which is used and then closed
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
     * @param stream  the stream which is used and then closed
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
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
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
     * @param self    a File
     * @param closure a closure
     */
    public static void eachByte(File self, Closure closure) throws IOException {
        BufferedInputStream is = newInputStream(self);
        eachByte(is, closure);
    }

    /**
     * Traverse through each byte of the specified stream
     *
     * @param is      stream to iterate over
     * @param closure closure to apply to each byte
     * @throws IOException
     */
    public static void eachByte(InputStream is, Closure closure) throws IOException {
        try {
            while (true) {
                int b = is.read();
                if (b == -1) {
                    break;
                } else {
                    closure.call(new Byte((byte) b));
                }
            }
            is.close();
        } catch (IOException e) {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e2) {
                    // ignore as we're already throwing
                }
                throw e;
            }
        }
    }

    /**
     * Traverse through each byte of the specified URL
     *
     * @param url     url to iterate over
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
                chars[0] = (char) c;
                writer.write((String) closure.call(new String(chars)));
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
     * @param reader  a reader
     * @param writer  a writer
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
     * @param self    a File
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
     * @param self    a File
     * @param writer  a writer
     * @param closure a closure which returns a boolean value and takes a line as input
     * @throws IOException if <code>self</code> is not readable
     */
    public static void filterLine(final File self, final Writer writer, final Closure closure) throws IOException {
        filterLine(newReader(self), writer, closure);
    }

    /**
     * Filter the lines of a Reader and create a Writable in return to stream the filtered lines
     *
     * @param reader  a reader
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
                } catch (IOException e) {
                    throw new RuntimeException(e); // TODO: change this exception type
                }
                return buffer.toString();
            }
        };
    }

    /**
     * Filter lines from an input stream using a closure predicate
     *
     * @param self      an input stream
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
     * @param self      an InputStream
     * @param writer    a writer to write output to
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
        DataInputStream dis = new DataInputStream(fileInputStream);
        dis.readFully(bytes);
        dis.close();
        return bytes;
    }



    // ================================
    // Socket and ServerSocket methods

    /**
     * Allows an InputStream and an OutputStream from a Socket to be used,
     * calling the closure with the streams and then ensuring that the streams are closed down again
     * irrespective of whether exceptions occur.
     *
     * @param socket  a Socket
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
     * @param self  a Socket
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
     * @param self  a Socket
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
     * @param closure      a Closure
     * @return a Socket
     * @throws IOException
     */
    public static Socket accept(ServerSocket serverSocket, final Closure closure) throws IOException {
        final Socket socket = serverSocket.accept();
        new Thread(new Runnable() {
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
     * @param file     a File
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
     * @param self  a Process
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
     * @param self  a Process
     * @param value a value to append
     * @return an OutputStream
     */
    public static OutputStream leftShift(Process self, byte[] value) throws IOException {
        return leftShift(self.getOutputStream(), value);
    }

    /**
     * Wait for the process to finish during a certain amount of time, otherwise stops the process.
     *
     * @param self           a Process
     * @param numberOfMillis the number of milliseconds to wait before stopping the process
     */
    public static void waitForOrKill(Process self, long numberOfMillis) {
        ProcessRunner runnable = new ProcessRunner(self);
        Thread thread = new Thread(runnable);
        thread.start();
        runnable.waitForOrKill(numberOfMillis);
    }

    /**
     * Process each regex group matched substring of the given string. If the closure
     * parameter takes one argument an array with all match groups is passed to it.
     * If the closure takes as many arguments as there are match groups, then each
     * parameter will be one match group.
     *
     * @param self    the source string
     * @param regex   a Regex string
     * @param closure a closure with one parameter or as much parameters as groups
     * @author bing ran
     * @author Pilho Kim
     * @author Jochen Theodorou
     */
    public static void eachMatch(String self, String regex, Closure closure) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(self);
        while (m.find()) {
            int count = m.groupCount();
            ArrayList groups = new ArrayList();
            for (int i = 0; i <= count; i++) {
                groups.add(m.group(i));
            }
            if (groups.size()==1 || closure.getMaximumNumberOfParameters()<groups.size()) {
                // not enough parameters there to give each group part
                // it's own parameter, so try a closure with one parameter
                // and give it all groups as a array
                closure.call((Object)groups.toArray());
            } else { 
                closure.call((Object[])groups.toArray());
            }
        }
    }

    /**
     * Process each matched substring of the given group matcher. The object
     * passed to the closure is an array of strings, matched per a successful match.
     *
     * @param self    the source matcher
     * @param closure a closure
     * @author bing ran
     * @author Pilho Kim
     */
    public static void each(Matcher self, Closure closure) {
        Matcher m = self;
        while (m.find()) {
            int count = m.groupCount();
            ArrayList groups = new ArrayList();
            for (int i = 0; i <= count; i++) {
                groups.add(m.group(i));
            }
            closure.call((Object[])groups.toArray());
        }
    }

    /**
     * Iterates over every element of the collection and return the index of the first object
     * that matches the condition specified in the closure
     *
     * @param self    the iteration object over which we iterate
     * @param closure the filter to perform a match on the collection
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
            } catch (InterruptedException e) {
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
                } catch (InterruptedException e) {
                }
                if (!finished) {
                    process.destroy();
                }
            }
        }
    }
    protected static class RangeInfo {
        protected int from, to;
        protected boolean reverse;

        public RangeInfo(int from, int to, boolean reverse) {
            this.from = from;
            this.to = to;
            this.reverse = reverse;
        }
    }
}
