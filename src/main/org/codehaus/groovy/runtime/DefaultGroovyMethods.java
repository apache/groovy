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
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.typehandling.NumberMath;
import org.codehaus.groovy.tools.RootLoader;
import org.w3c.dom.NodeList;

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
 * @author Marc Guillemot
 * @author Russel Winder
 * @author bing ran
 * @author Jochen Theodorou
 * @author Paul King
 * @author Michael Baehr
 * @version $Revision$
 */
public class DefaultGroovyMethods {

    private static final Logger LOG = Logger.getLogger(DefaultGroovyMethods.class.getName());
    private static final Integer ONE = new Integer(1);

    /**
     * Identity check. Since == is overridden in Groovy with the meaning of equality
     * we need some fallback to check for object identity.
     *
     * @param self an object
     * @param other an object to compare identity with
     * @return true if self and other are identical, false otherwise
     */
    public static boolean is(Object self, Object other) {
        return self == other;
    }

    /**
     * Allows the closure to be called for the object reference self
     *
     * @param self    the object to have a closure act upon
     * @param closure the closure to call on the object
     * @return result of calling the closure
     */
    public static Object identity(Object self, Closure closure) {
        final Closure clonedClosure = (Closure) closure.clone();
        clonedClosure.setDelegate(self);
        return clonedClosure.call(self);
    }

    /**
     * Allows the subscript operator to be used to lookup dynamic property values.
     * <code>bean[somePropertyNameExpression]</code>. The normal property notation
     * of groovy is neater and more concise but only works with compile-time known
     * property names.
     *
     * @param self the object to act upon
     * @param property the property of interest
     * @return the property value
     */
    public static Object getAt(Object self, String property) {
        return InvokerHelper.getProperty(self, property);
    }

    /**
     * Allows the subscript operator to be used to set dynamically named property values.
     * <code>bean[somePropertyNameExpression] = foo</code>. The normal property notation
     * of groovy is neater and more concise but only works with property names which
     * are known at compile time.
     *
     * @param self     the object to act upon
     * @param property the name of the property to set
     * @param newValue the value to set
     */
    public static void putAt(Object self, String property, Object newValue) {
        InvokerHelper.setProperty(self, property, newValue);
    }

    /**
     * Generates a detailed dump string of an object showing its class,
     * hashCode and fields.
     *
     * @param self an object
     * @return the dump representation
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

        /*jes this may be rewritten to use the new getProperties() stuff
         * but the original pulls out private variables, whereas getProperties()
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

        /* here is a different implementation that uses getProperties(). I have left
         * it commented out because it returns a slightly different list of properties;
         * ie it does not return privates. I don't know what dump() really should be doing,
         * although IMO showing private fields is a no-no
         */
        /*
        List props = getProperties(self);
            for(Iterator itr = props.keySet().iterator(); itr.hasNext(); ) {
            String propName = itr.next().toString();

            // the original skipped this, so I will too
            if(pv.getName().equals("metaClass")) continue;
            if(pv.getName().equals("class")) continue;

            buffer.append(" ");
            buffer.append(propName);
            buffer.append("=");
            try {
                buffer.append(InvokerHelper.toString(props.get(propName)));
            }
            catch (Exception e) {
                buffer.append(e);
            }
        }
        */

        buffer.append(">");
        return buffer.toString();
    }

    /**
     * Retrieves the list of {@link MetaProperty} objects for 'self' and wraps it
     * in a list of {@link PropertyValue} objects that additionally provide
     * the value for each property of 'self'.
     *
     * @param self the receiver object
     * @return list of {@link PropertyValue} objects
     * @see groovy.util.Expando#getMetaPropertyValues()
     */
    public static List getMetaPropertyValues(Object self) {
        MetaClass metaClass = InvokerHelper.getMetaClass(self);
        List mps = metaClass.getProperties();
        List props = new ArrayList(mps.size());
        for (Iterator itr = mps.iterator(); itr.hasNext();) {
            MetaProperty mp = (MetaProperty) itr.next();
            PropertyValue pv = new PropertyValue(self, mp);
            props.add(pv);
        }
        return props;
    }

    /**
     * Convenience method that calls {@link #getMetaPropertyValues(Object)}(self)
     * and provides the data in form of simple key/value pairs, i.e. without
     * type() information.
     *
     * @param self the receiver object
     * @return meta properties as Map of key/value pairs
     */
    public static Map getProperties(Object self) {
        List metaProps = getMetaPropertyValues(self);
        Map props = new HashMap(metaProps.size());

        for (Iterator itr = metaProps.iterator(); itr.hasNext();) {
            PropertyValue pv = (PropertyValue) itr.next();
            try {
                props.put(pv.getName(), pv.getValue());
            } catch (Exception e) {
                LOG.throwing(self.getClass().getName(), "getProperty(" + pv.getName() + ")", e);
            }
        }
        return props;
    }

    /**
     * Scoped use method
     * @param self any Object
     * @param categoryClass a category class to use
     * @param closure the closure to invoke with the category in place
     * @return the value returned from the closure
     */
    public static Object use(Object self, Class categoryClass, Closure closure) {
        return GroovyCategorySupport.use(categoryClass, closure);
    }

    /**
     * Scoped use method with list of categories.
     *
     * @param self any Object
     * @param categoryClassList a list of category classes
     * @param closure the closure to invoke with the categories in place
     * @return the value returned from the closure
     */
    public static Object use(Object self, List categoryClassList, Closure closure) {
        return GroovyCategorySupport.use(categoryClassList, closure);
    }

    /**
     * Allows the usage of addShutdownHook without getting the runtime first. 
     * 
     * @param self the object the method is called on (ignored)
     * @param closure the shutdown hook action
     */
    public static void addShutdownHook (Object self, Closure closure) {
        Runtime.getRuntime().addShutdownHook(new Thread( closure));
    }

    /**
     * use() a list of categories, specifying the list as varargs:<br>
     * use(CategoryClass1, CategoryClass2) { ... }<br>
     * This method prevents the error of forgetting to wrap the the category
     * classes in a list.
     *
     * @param self any Object
     * @param array a list of category classes and a Closure
     * @return the value returned from the closure
     */
    public static Object use(Object self, Object[] array) {
        if (array.length < 2)
            throw new IllegalArgumentException(
                    "Expecting at least 2 arguments, a category class and a Closure");
        Closure closure;
        try {
            closure = (Closure) array[array.length - 1];
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Expecting a Closure to be the last argument");
        }
        List list = new ArrayList(array.length - 1);
        for (int i = 0; i < array.length - 1; ++i)
            list.add(array[i]);
        return GroovyCategorySupport.use(list, closure);
    }

    /**
     * Print a value to the standard output stream.
     *
     * @param self any Object
     * @param value the value to print
     */
    public static void print(Object self, Object value) {
        System.out.print(InvokerHelper.toString(value));
    }

    /**
     * Print a linebreak to the standard output stream.
     *
     * @param self any Object
     */
    public static void println(Object self) {
        System.out.println();
    }

    /**
     * Print a value (followed by a newline) to the standard output stream.
     *
     * @param self any Object
     * @param value the value to print
     */
    public static void println(Object self, Object value) {
        System.out.println(InvokerHelper.toString(value));
    }

    /**
     * Printf to a console.  Only works with JDK1.5 or later.
     *
     * @param self any Object
     * @param format a format string
     * @param values values referenced by the format specifiers in the format string.
     */
    public static void printf(Object self, String format, Object[] values) {
        if (self instanceof PrintStream)
            printf((PrintStream)self, format, values);
        else
            printf(System.out, format, values);
    }

    /**
     * Sprintf to a string.  Only works with JDK1.5 or later.
     *
     * @param self any Object
     * @param format a format string
     * @param values values referenced by the format specifiers in the format string.
     * @return the resulting formatted string
     */
    public static String sprintf(Object self, String format, Object[] values) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputStream);
        printf(out, format, values);
        return outputStream.toString();
    }

    /**
     * Printf to a PrintStream.  Only works with JDK1.5 or later.
     *
     * @param out a PrintStream object
     * @param format a format string
     * @param values values referenced by the format specifiers in the format string.
     */
    private static void printf(PrintStream out, String format, Object[] values) {
        char version = System.getProperty("java.version").charAt(2);
        if ( version >= '5') {
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
                out.getClass().getMethod("printf", new Class[]{String.class, Object[].class}).invoke(out, new Object[]{format, values});
            } catch (NoSuchMethodException nsme) {
                throw new RuntimeException("getMethod threw a NoSuchMethodException.  This is impossible.");
            } catch (IllegalAccessException iae) {
                throw new RuntimeException("invoke threw a IllegalAccessException.  This is impossible.");
            } catch (java.lang.reflect.InvocationTargetException ite) {
                throw new RuntimeException("invoke threw a InvocationTargetException.  This is impossible.");
            }
        } else {
            throw new RuntimeException("printf requires JDK1.5 or later.");
        }
    }

    /**
     * Prints a formatted string using the specified format string and
     * arguments.
     * <p/>
     * <p/>
     * For examples, <pre>
     *     printf ( "Hello, %s!\n" , [ "world" ] as String[] )
     *     printf ( "Hello, %s!\n" , [ "Groovy" ])
     *     printf ( "%d + %d = %d\n" , [ 1 , 2 , 1+2 ] as Integer[] )
     *     printf ( "%d + %d = %d\n" , [ 3 , 3 , 3+3 ])
     * <p/>
     *     ( 1..5 ).each { printf ( "-- %d\n" , [ it ] as Integer[] ) }
     *     ( 1..5 ).each { printf ( "-- %d\n" , [ it ] as int[] ) }
     *     ( 0x41..0x45 ).each { printf ( "-- %c\n" , [ it ] as char[] ) }
     *     ( 07..011 ).each { printf ( "-- %d\n" , [ it ] as byte[] ) }
     *     ( 7..11 ).each { printf ( "-- %d\n" , [ it ] as short[] ) }
     *     ( 7..11 ).each { printf ( "-- %d\n" , [ it ] as long[] ) }
     *     ( 7..11 ).each { printf ( "-- %5.2f\n" , [ it ] as float[] ) }
     *     ( 7..11 ).each { printf ( "-- %5.2g\n" , [ it ] as double[] ) }
     * </pre>
     * <p/>
     *
     * @param self any Object
     * @param format A format string
     * @param arg    Argument which is referenced by the format specifiers in the format
     *               string.  The type of <code>arg</code> should be one of Object[], List,
     *               int[], short[], byte[], char[], boolean[], long[], float[], or double[].
     */
    public static void printf(Object self, String format, Object arg) {
        if (self instanceof PrintStream)
            printf((PrintStream)self, format, arg);
        else
            printf(System.out, format, arg);
    }

    private static void printf(PrintStream self, String format, Object arg) {
        if (arg instanceof Object[]) {
            printf(self, format, (Object[]) arg);
            return;
        }
        if (arg instanceof List) {
            printf(self, format, ((List) arg).toArray());
            return;
        }
        if (!arg.getClass().isArray()) {
            Object[] o = (Object[]) java.lang.reflect.Array.newInstance(arg.getClass(), 1);
            o[0] = arg;
            printf(self, format, o);
            return;
        }

        Object[] ans;
        String elemType = arg.getClass().getName();
        if (elemType.equals("[I")) {
            int[] ia = (int[]) arg;
            ans = new Integer[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = new Integer(ia[i]);
            }
        } else if (elemType.equals("[C")) {
            char[] ia = (char[]) arg;
            ans = new Character[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = new Character(ia[i]);
            }
        } else if (elemType.equals("[Z")) {
            boolean[] ia = (boolean[]) arg;
            ans = new Boolean[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = new Boolean(ia[i]);
            }
        } else if (elemType.equals("[B")) {
            byte[] ia = (byte[]) arg;
            ans = new Byte[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = new Byte(ia[i]);
            }
        } else if (elemType.equals("[S")) {
            short[] ia = (short[]) arg;
            ans = new Short[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = new Short(ia[i]);
            }
        } else if (elemType.equals("[F")) {
            float[] ia = (float[]) arg;
            ans = new Float[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = new Float(ia[i]);
            }
        } else if (elemType.equals("[J")) {
            long[] ia = (long[]) arg;
            ans = new Long[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = new Long(ia[i]);
            }
        } else if (elemType.equals("[D")) {
            double[] ia = (double[]) arg;
            ans = new Double[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = new Double(ia[i]);
            }
        } else {
            throw new RuntimeException("printf(String," + arg + ")");
        }
        printf(self, format, ans);
    }

    /**
     * Returns a formatted string using the specified format string and
     * arguments.
     * 
     * TODO: remove duplication with printf
     *
     * @param self any Object
     * @param format A format string
     * @param arg    Argument which is referenced by the format specifiers in the format
     *               string.  The type of <code>arg</code> should be one of Object[], List,
     *               int[], short[], byte[], char[], boolean[], long[], float[], or double[].
     * @return the resulting printf'd string
     */
    public static String sprintf(Object self, String format, Object arg) {
        if (arg instanceof Object[]) {
            return sprintf(self, format, (Object[]) arg);
        }
        if (arg instanceof List) {
            return sprintf(self, format, ((List) arg).toArray());
        }
        if (!arg.getClass().isArray()) {
            Object[] o = (Object[]) java.lang.reflect.Array.newInstance(arg.getClass(), 1);
            o[0] = arg;
            return sprintf(self, format, o);
        }

        Object[] ans;
        String elemType = arg.getClass().getName();
        if (elemType.equals("[I")) {
            int[] ia = (int[]) arg;
            ans = new Integer[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = new Integer(ia[i]);
            }
        } else if (elemType.equals("[C")) {
            char[] ia = (char[]) arg;
            ans = new Character[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = new Character(ia[i]);
            }
        } else if (elemType.equals("[Z")) {
            boolean[] ia = (boolean[]) arg;
            ans = new Boolean[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = new Boolean(ia[i]);
            }
        } else if (elemType.equals("[B")) {
            byte[] ia = (byte[]) arg;
            ans = new Byte[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = new Byte(ia[i]);
            }
        } else if (elemType.equals("[S")) {
            short[] ia = (short[]) arg;
            ans = new Short[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = new Short(ia[i]);
            }
        } else if (elemType.equals("[F")) {
            float[] ia = (float[]) arg;
            ans = new Float[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = new Float(ia[i]);
            }
        } else if (elemType.equals("[J")) {
            long[] ia = (long[]) arg;
            ans = new Long[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = new Long(ia[i]);
            }
        } else if (elemType.equals("[D")) {
            double[] ia = (double[]) arg;
            ans = new Double[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = new Double(ia[i]);
            }
        } else {
            throw new RuntimeException("sprintf(String," + arg + ")");
        }
        return sprintf(self, format, (Object[]) ans);
    }


    /**
     * @param self any Object
     * @return a String that matches what would be typed into a terminal to
     *         create this object. e.g. [1, 'hello'].inspect() -> [1, "hello"]
     */
    public static String inspect(Object self) {
        return InvokerHelper.inspect(self);
    }

    /**
     * Print to a console in interactive format.
     *
     * @param self any Object
     * @param out the PrintWriter used for printing
     */
    public static void print(Object self, PrintWriter out) {
        if (out == null) {
            out = new PrintWriter(System.out);
        }
        out.print(InvokerHelper.toString(self));
    }

    /**
     * Print to a console in interactive format.
     *
     * @param self any Object
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
     *
     * @param object any Object
     * @param method the name of the method to call
     * @param arguments the arguments to use
     * @return the result of the method call
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
        if (switchValue instanceof Class) {
            Class val = (Class) switchValue;
            return caseValue.isAssignableFrom(val);
        }
        return caseValue.isInstance(switchValue);
    }

    public static boolean isCase(Collection caseValue, Object switchValue) {
        return caseValue.contains(switchValue);
    }

    public static boolean isCase(Pattern caseValue, Object switchValue) {
        if (switchValue == null) {
            return caseValue == null;
        }
        final Matcher matcher = caseValue.matcher(switchValue.toString());
        if (matcher.matches()) {
            RegexSupport.setLastMatcher(matcher);
            return true;
        } else {
            return false;
        }
    }

    // Collection based methods
    //-------------------------------------------------------------------------

    public static Collection unique(Collection self) {
        if (self instanceof Set)
            return self;
        List answer = new ArrayList();
        NumberComparator comparator = new NumberComparator();
        for (Iterator it = self.iterator(); it.hasNext();) {
            Object o = it.next();
            boolean duplicated = false;
            for (Iterator it2 = answer.iterator(); it2.hasNext();) {
                Object o2 = it2.next();
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
     * A convenience method for making a collection unique using a closure as a comparator.
     *
     * @param self    a Collection
     * @param closure a Closure used as a comparator
     * @return self   without any duplicates
     */
    public static Collection unique(Collection self, Closure closure) {
        if (self instanceof Set)
            return self;
        // use a comparator of one item or two
        int params = closure.getMaximumNumberOfParameters();
        if (params == 1) {
            unique(self, new OrderBy(closure));
        } else {
            unique(self, new ClosureComparator(closure));
        }
        return self;
    }

    /**
     * Remove all duplicates from a given Collection.
     * Works on the receiver object and returns it.
     * The order of members in the Collection are compared by the given Comparator.
     * For each duplicate, the first member which is returned
     * by the given Collection's iterator is retained, but all other ones are removed.
     * The given Collection's original order is preserved.
     * <p/>
     * <code><pre>
     *     class Person {
     *         def fname, lname
     *         public String toString() {
     *             return fname + " " + lname
     *         }
     *     }
     * <p/>
     *     class PersonComparator implements Comparator {
     *         public int compare(Object o1, Object o2) {
     *             Person p1 = (Person) o1
     *             Person p2 = (Person) o2
     *             if (p1.lname != p2.lname)
     *                 return p1.lname.compareTo(p2.lname)
     *             else
     *                 return p1.fname.compareTo(p2.fname)
     *         }
     * <p/>
     *         public boolean equals(Object obj) {
     *             return this.equals(obj)
     *         }
     *     }
     * <p/>
     *     Person a = new Person(fname:"John", lname:"Taylor")
     *     Person b = new Person(fname:"Clark", lname:"Taylor")
     *     Person c = new Person(fname:"Tom", lname:"Cruz")
     *     Person d = new Person(fname:"Clark", lname:"Taylor")
     * <p/>
     *     def list = [a, b, c, d]
     *     List list2 = list.unique(new PersonComparator())
     *     assert( list2 == list && list == [a, b, c] )
     * <p/>
     * </pre></code>
     *
     * @param self       a Collection
     * @param comparator a Comparator.
     * @return self       without duplicates
     */
    public static Collection unique(Collection self, Comparator comparator) {
        if (self instanceof Set)
            return self;
        List answer = new ArrayList();
        for (Iterator it = self.iterator(); it.hasNext();) {
            Object o = it.next();
            boolean duplicated = false;
            for (Iterator it2 = answer.iterator(); it2.hasNext();) {
                Object o2 = it2.next();
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
            if (!DefaultTypeTransformation.castToBoolean(closure.call(iter.next()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Iterates over every element of a collection, and check whether all elements are true according to the Groovy Truth.
     * Equivalent to self.every({element -> element})
     *
     * @param self    the object over which we iterate
     * @return true if every item in the collection matches the closure
     *         predicate
     */
    public static boolean every(Object self) {
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            if (!DefaultTypeTransformation.castToBoolean(iter.next())) {
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
            if (DefaultTypeTransformation.castToBoolean(closure.call(iter.next()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Iterates over every element of a collection, and check whether at least one element is true according to the Groovy Truth.
     * Equivalent to self.any({element -> element})
     *
     * @param self    the object over which we iterate
     * @return true if any item in the collection matches the closure predicate
     */
    public static boolean any(Object self) {
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            if (DefaultTypeTransformation.castToBoolean(iter.next())) {
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
            if (DefaultTypeTransformation.castToBoolean(metaClass.invokeMethod(filter, "isCase", object))) {
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
            if (DefaultTypeTransformation.compareEqual(iter.next(), value)) {
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
     * @param self       a Map
     * @param collection the Collection to which the mapped values are added
     * @param closure    the closure used for mapping, which can be with one(Map.Entry) or two(key, value) parameters
     * @return a List of the mapped values
     */
    public static Collection collect(Map self, Collection collection, Closure closure) {
        boolean isTwoParams = (closure.getParameterTypes().length == 2);
        for (Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
            if (isTwoParams) {
                Map.Entry entry = (Map.Entry) iter.next();
                collection.add(closure.call(new Object[]{entry.getKey(), entry.getValue()}));
            } else {
                collection.add(closure.call(iter.next()));
            }
        }
        return collection;
    }

    /**
     * Iterates through this Map transforming each entry into a new value using the closure
     * as a transformer, returning a list of transformed values.
     *
     * @param self    a Map
     * @param closure the closure used to map each element of the collection
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
            if (DefaultTypeTransformation.castToBoolean(closure.call(value))) {
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
            if (DefaultTypeTransformation.castToBoolean(closure.call(value))) {
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
            if (DefaultTypeTransformation.castToBoolean(closure.call(value))) {
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
            if (DefaultTypeTransformation.castToBoolean(closure.call(value))) {
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
            if (DefaultTypeTransformation.castToBoolean(closure.call(value))) {
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
            if (DefaultTypeTransformation.castToBoolean(callClosureForMapEntry(closure, entry))) {
                answer.put(entry.getKey(), entry.getValue());
            }
        }
        return answer;
    }

    /**
     * Groups all collection members into groups determined by the
     * supplied mapping closure.
     *
     * @param self    a collection to group (no map)
     * @param closure a closure mapping entries on keys
     * @return a new Map grouped by keys
     */
    public static Map groupBy(Collection self, Closure closure) {
        Map answer = new HashMap();
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            groupCurrentElement(closure, answer, iter);
        }
        return answer;
    }

    /**
     * Groups all map members into groups determined by the
     * supplied mapping closure.
     *
     * @param self     a map to group
     * @param closure  a closure mapping entries on keys
     * @return         a new Map grouped by keys
     */
    /* Removed for 1.0, to be discussed for 1.1
    public static Map groupBy(Map self, Closure closure) {
        final Map answer = new HashMap();
        for (final Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
            groupCurrentElement(closure, answer, iter);
        }
        return answer;
    }
    */

    /**
     * Groups the current element of the iterator as determined
     * by the mapping closure.
     *
     * @param closure  a closure mapping the current entry on a key
     * @param answer   the map containing the results
     * @param iter     the iterator from which the current element stems
     */
    private static void groupCurrentElement(Closure closure, Map answer, Iterator iter) {
	Object element = iter.next();
	Object value = closure.call(element);
	if (answer.containsKey(value)) {
	    ((List) answer.get(value)).add(element);
	} else {
	    ArrayList groupedElements = new ArrayList();
	    groupedElements.add(element);
	    answer.put(value, groupedElements);
	}
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
     * Sums a collection of numeric values. <code>coll.sum()</code> is equivalent to:
     * <code>coll.inject(0) {value, item -> value + item}</code>.
     *
     * @param self Collection of values to add together.
     * @return The sum of all of the list itmems.
     */
    public static Object sum(Collection self) {
        Object result = null;

        if (self.size() == 0) return result;

        boolean isNumber = true;

        Class classref = null;
        try {
            classref = Class.forName("java.lang.Number");
        } catch (Exception ex) {
            // Ignore
        }

        for (Iterator iter = self.iterator(); iter.hasNext();) {
            if (!classref.isInstance(iter.next())) {
                isNumber = false;
                break;
            }
        }

        if (isNumber) {
            result = new Integer(0);
        } else {
            result = new String();
        }

        Object[] param = new Object[1];
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            param[0] = iter.next();
            MetaClass metaClass = InvokerHelper.getMetaClass(result);
            result = metaClass.invokeMethod(result, "plus", param);
        }
        return result;
    }

    /**
     * Sums the result of apply a closure to each item of a collection.
     * <code>coll.sum(closure)</code> is equivalent to:
     * <code>coll.collect(closure).sum()</code>.
     *
     * @param self    a Collection
     * @param closure a single parameter closure that returns a numeric value.
     * @return The sum of the values returned by applying the closure to each
     *         item of the list.
     */
    public static Object sum(Collection self, Closure closure) {
        Object result = new Integer(0);
        Object[] closureParam = new Object[1];
        Object[] plusParam = new Object[1];
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            closureParam[0] = iter.next();
            plusParam[0] = closure.call(closureParam);
            MetaClass metaClass = InvokerHelper.getMetaClass(result);
            result = metaClass.invokeMethod(result, "plus", plusParam);
        }
        return result;
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

        if (separator == null) separator = "";

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

        if (separator == null) separator = "";

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
                if (answer == null || ScriptBytecodeAdapter.compareGreaterThan(value, answer)) {
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
                if (answer == null || ScriptBytecodeAdapter.compareLessThan(value, answer)) {
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
                if (answer == null || ScriptBytecodeAdapter.compareLessThan(value, answer_value)) {
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
                if (answer == null || ScriptBytecodeAdapter.compareLessThan(answer_value, value)) {
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
     * Provide the standard Groovy size method for a file.
     *
     * @param self a file object
     * @return the file's size (length)
     */
    public static long size(File self) {
        return self.length();
    }


    /**
     * Provide the standard Groovy size method for a matcher.
     *
     * @param self a matcher object
     * @return the matcher's size (count)
     */
    public static long size(Matcher self) {
        return getCount(self);
    }

    /**
     * Provide the standard Groovy size method for an array.
     *
     * @param self an Array of objects
     * @return the size (length) of the Array
     */
    public static int size(Object[] self) {
        return self.length;
    }

    /**
     * Support the subscript operator for CharSequence.
     *
     * @param text  a CharSequence
     * @param index the index of the Character to get
     * @return the Character at the given index
     */
    public static CharSequence getAt(CharSequence text, int index) {
        index = normaliseIndex(index, text.length());
        return text.subSequence(index, index + 1);
    }

    /**
     * Support the subscript operator for String.
     *
     * @param text a String
     * @param index the index of the Character to get
     * @return the Character at the given index
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
        int from = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getFrom()), text.length());
        int to = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getTo()), text.length());

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
        return getAt(text, (Range) range);
    }

    /**
     * Support the range subscript operator for String
     *
     * @param text  a String
     * @param range a Range
     * @return a substring corresponding to the Range
     */
    public static String getAt(String text, Range range) {
        int from = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getFrom()), text.length());
        int to = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getTo()), text.length());

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
     * Transforms a String representing a URI into a URI object.
     *
     * @param self the String representing a URI
     * @return a URI
     * @throws URISyntaxException is thrown if the URI is not well formed.
     */
    public static URI toURI(String self) throws URISyntaxException {
        return new URI(self);
    }

    /**
     * Turns a String into a regular expression pattern
     *
     * @param self a String to convert into a regular expression
     * @return the regular expression pattern
     */
    public static Pattern negate(String self) {
        return Pattern.compile(self);
    }

    /**
     * Replaces all occurrencies of a captured group by the result of a closure on that text.
     * <p/>
     * <p> For examples,
     * <pre>
     *     assert "FOOBAR-FOOBAR-" == "foobar-FooBar-".replaceAll("(([fF][oO]{2})[bB]ar)", { Object[] it -> it[0].toUpperCase() })
     * <p/>
     *     Here,
     *          it[0] is the global string of the matched group
     *          it[1] is the first string in the matched group
     *          it[2] is the second string in the matched group
     * <p/>
     * <p/>
     *     assert "FOO-FOO-" == "foobar-FooBar-".replaceAll("(([fF][oO]{2})[bB]ar)", { x, y, z -> z.toUpperCase() })
     * <p/>
     *     Here,
     *          x is the global string of the matched group
     *          y is the first string in the matched group
     *          z is the second string in the matched group
     * </pre>
     *
     * @param self    a String
     * @param regex   the capturing regex
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
                matcher.appendReplacement(sb, String.valueOf(closure.call(groups.toArray())));
            }
            matcher.appendTail(sb);
            return sb.toString();
        } else {
            return self;
        }
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
     * @param self a String object
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
     * @param self a String object
     * @param numberOfChars the total number of characters
     * @return the String padded to the left
     */

    public static String padLeft(String self, Number numberOfChars) {
        return padLeft(self, numberOfChars, " ");
    }

    /**
     * Pad a String with the characters appended to the right
     *
     * @param self a String object
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
     * @param self a String object
     * @param numberOfChars the total number of characters
     * @return the String padded to the right
     */

    public static String padRight(String self, Number numberOfChars) {
        return padRight(self, numberOfChars, " ");
    }

    /**
     * Center a String and padd it with the characters appended around it
     *
     * @param self a String object
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
     * @param self a String object
     * @param numberOfChars the total number of characters
     * @return the String centered with padded character around
     */
    public static String center(String self, Number numberOfChars) {
        return center(self, numberOfChars, " ");
    }

    /**
     * Support the subscript operator, e.g. matcher[index], for a regex Matcher.
     * <p/>
     * For an example using no group match, <code><pre>
     *    def p = /ab[d|f]/
     *    def m = "abcabdabeabf" =~ p
     *    for (i in 0..<m.count) {
     *        println( "m.groupCount() = " + m.groupCount())
     *        println( "  " + i + ": " + m[i] )   // m[i] is a String
     *    }
     * </pre></code>
     * <p/>
     * For an example using group matches, <code><pre>
     *    def p = /(?:ab([c|d|e|f]))/
     *    def m = "abcabdabeabf" =~ p
     *    for (i in 0..<m.count) {
     *        println( "m.groupCount() = " + m.groupCount())
     *        println( "  " + i + ": " + m[i] )   // m[i] is a List
     *    }
     * </pre></code>
     * <p/>
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
     * @param idx     the index number
     */
    public static void setIndex(Matcher matcher, int idx) {
        int count = getCount(matcher);
        if (idx < -count || idx >= count) {
            throw new IndexOutOfBoundsException("index is out of range " + (-count) + ".." + (count - 1) + " (index = " + idx + ")");
        }
        if (idx == 0) {
            matcher.reset();
        } else if (idx > 0) {
            matcher.reset();
            for (int i = 0; i < idx; i++) {
                matcher.find();
            }
        } else if (idx < 0) {
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
     * @see java.util.List#subList(int,int)
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
    protected static RangeInfo subListBorders(int size, IntRange range) {
        int from = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getFrom()), size);
        int to = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getTo()), size);
        boolean reverse = range.isReverse();
        if (from > to) {                        // support list[1..-1]
            int tmp = to;
            to = from;
            from = tmp;
            reverse = !reverse;
        }
        return new RangeInfo(from, to + 1, reverse);
    }

    // helper method for getAt and putAt
    protected static RangeInfo subListBorders(int size, EmptyRange range) {
        int from = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getFrom()), size);
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
                int idx = DefaultTypeTransformation.intUnbox(value);
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
                int idx = DefaultTypeTransformation.intUnbox(value);
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
                int idx = DefaultTypeTransformation.intUnbox(value);
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
                int idx = DefaultTypeTransformation.intUnbox(value);
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
                Object newVal = DefaultTypeTransformation.castToType(value, arrayComponentClass);
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
        self.replace(info.from, info.to, value.toString());
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
        self.replace(info.from, info.to, value.toString());
    }

    /**
     * A helper method to allow lists to work with subscript operators
     *
     * @param self  a List
     * @param range the subset of the list to set
     * @param value the values to put at the given sublist or a Collection of values
     */
    public static void putAt(List self, EmptyRange range, Object value) {
        RangeInfo info = subListBorders(self.size(), range);
        List sublist = self.subList(info.from, info.to);
        sublist.clear();
        if (value instanceof Collection) {
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
     * @param range the subset of the list to set
     * @param value the value to put at the given sublist or a Collection of values
     */
    public static void putAt(List self, IntRange range, Object value) {
        RangeInfo info = subListBorders(self.size(), range);
        List sublist = self.subList(info.from, info.to);
        sublist.clear();
        if (value instanceof Collection) {
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
     * @param self   a List
     * @param splice the subset of the list to set
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
     * @param self   a List
     * @param splice the subset of the list to set
     * @param value  the value to put at the given sublist
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
        int left /* = 0 */;
        int right = 0;
        boolean emptyRange = false;
        if (splice.size() == 2) {
            left = DefaultTypeTransformation.intUnbox(splice.get(0));
            right = DefaultTypeTransformation.intUnbox(splice.get(1));
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
        List sublist /* = null */;
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
     * <p>
     * Returns a new Map containg all entries from <code>left</code> and <code>right</code>,
     * giving precedence to <code>right</code>.
     * </p>
     *
     * <p>
     * Equivalent to <code>Map m = new HashMap(); m.putAll(left); m.putAll(right); return m;</code>
     * </p>
     *
     * @param left  a Map
     * @param right a Map
     * @return a new Map containing all entries from left and right
     */
    public static Map plus(Map left, Map right) {
        Map map = new TreeMap(left);
        map.putAll(right);
        return map;
    }

    /**
     * A helper method to allow lists to work with subscript operators
     *
     * @param self a Map
     * @param key  an Object as a key for the map
     * @param value the value to put into the map
     * @return the value corresponding to the given key
     */
    public static Object putAt(Map self, Object key, Object value) {
        self.put(key, value);
        return value;
    }

    /**
     * This converts a possibly negative index to a real index into the array.
     *
     * @param i the unnormalised index
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

     public static SpreadMap spread(Map self) {
        return toSpreadMap(self);
    }

    /**
     * Returns the converted <code>SpreadLMap</code> of the given <code>self</code>.
     * <p/>
     * For examples, if there is defined a function like as
     * <blockquote><pre>
     *     def fn(a, b, c, d) { return a + b + c + d }
     * </pre></blockquote>, then all of the following three have the same meaning.
     * <blockquote><pre>
     *     println fn(a:1, [b:2, c:3].toSpreadMap(), d:4)
     *     println fn(a:1, *:[b:2, c:3], d:4)
     *     println fn(a:1, b:2, c:3, d:4)
     * </pre></blockquote>
     * <p/>
     *
     * @param self a list to be converted into a spreadmap
     * @return a newly created Spreadmap if this list is not null and its size is positive.
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
     * Avoids doing unnecessary work when sorting an already sorted set.
     *
     * @param self an identity function for an already sorted set
     * @return the sorted set
     */
    public static SortedSet sort(SortedSet self) {
        return self;
    }

    /**
     * Removes the last item from the List. Using add() and pop()
     * is similar to push and pop on a Stack.
     *
     * @param self a List
     * @return the item removed from the List
     * @throws NoSuchElementException if the list is empty and you try to pop() it.
     */
    public static Object pop(List self) {
        if (self.isEmpty()) {
            throw new NoSuchElementException("Cannot pop() an empty List");
        }
        return self.remove(self.size() - 1);
    }

    /**
     * A convenience method for sorting a Collection with a specific comparator
     *
     * @param self       a collection to be sorted
     * @param comparator a Comparator used for the comparison
     * @return a newly created sorted List
     */
    public static List sort(Collection self, Comparator comparator) {
        List list = asList(self);
        Collections.sort(list, comparator);
        return list;
    }

    /**
     * A convenience method for sorting a Collection using a closure as a comparator
     *
     * @param self    a Collection to be sorted
     * @param closure a Closure used as a comparator
     * @return a newly created sorted List
     */
    public static List sort(Collection self, Closure closure) {
        List list = asList(self);
        // use a comparator of one item or two
        int params = closure.getMaximumNumberOfParameters();
        if (params == 1) {
            Collections.sort(list, new OrderBy(closure));
        } else {
            Collections.sort(list, new ClosureComparator(closure));
        }
        return list;
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

    public static Object asType(Collection col, Class clazz) {
        if (clazz == List.class) {
            return asList(col);
        } else if (clazz == Set.class) {
            if (col instanceof Set) return col;
            return new HashSet(col);
        } else if (clazz == SortedSet.class) {
            if (col instanceof TreeSet) return col;
            return new TreeSet(col);
        }
        return asType((Object) col, clazz);
    }

    public static Object asType(Closure cl, Class clazz) {
        if (clazz.isInterface() && !(clazz.isInstance(cl))) {
            return Proxy.newProxyInstance(
                    clazz.getClassLoader(),
                    new Class[]{clazz},
                    new ConvertedClosure(cl));
        }
        return asType((Object) cl, clazz);
    }

    public static Object asType(Map map, Class clazz) {
        if (clazz.isInterface() && !(clazz.isInstance(map))) {
            return Proxy.newProxyInstance(
                    clazz.getClassLoader(),
                    new Class[]{clazz},
                    new ConvertedMap(map));
        }
        return asType((Object) map, clazz);
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
     * Create a Collection as a union of two collections. If the left collection
     * is a Set, then the returned collection will be a Set otherwise a List.
     * TODO: remove equivalent numbers after merge, e.g. 1L and 1G?
     *
     * @param left  the left Collection
     * @param right the right Collection
     * @return the merged Collection
     */
    public static Collection plus(Collection left, Collection right) {
        Collection answer;
        if (left instanceof Set)
            answer = new HashSet();
        else
            answer = new ArrayList(left.size() + right.size());
        answer.addAll(left);
        answer.addAll(right);
        return answer;
    }

    /**
     * Create a collection as a union of a Collection and an Object. If the collection
     * is a Set, then the returned collection will be a Set otherwise a List.
     * TODO: remove equivalent numbers after merge, e.g. 1L and 1G?
     *
     * @param left  a Collection
     * @param right an object to add/append
     * @return the resulting Collection
     */
    public static Collection plus(Collection left, Object right) {
        Collection answer;
        if (left instanceof Set)
            answer = new HashSet();
        else
            answer = new ArrayList(left.size() + 1);
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
     * @param left  a Collection
     * @param right a Collection
     * @return a List as an intersection of both collections
     */
    public static List intersect(Collection left, Collection right) {
        if (left.size() == 0)
            return new ArrayList();

        // TODO optomise if same type?
        // boolean nlgnSort = sameType(new Collection[]{left, right});

        ArrayList result = new ArrayList();
        //creates the collection to look for values.
        Collection pickFrom = new TreeSet(new NumberComparator());
        pickFrom.addAll(left);

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
     * @param left  a Collection
     * @param right a Collection
     * @return boolean   <code>true</code> if the intersection of two collenctions is empty, <code>false</code> otherwise.
     */
    public static boolean disjoint(Collection left, Collection right) {

        if (left.size() == 0 || right.size() == 0)
            return true;

        Collection pickFrom = new TreeSet(new NumberComparator());
        pickFrom.addAll(right);

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
            } else if (o1.getClass() == o2.getClass() && o1 instanceof Comparable) {
                return ((Comparable) o1).compareTo((Comparable) o2);
            } else {
                int x1 = o1.hashCode();
                int x2 = o2.hashCode();
                return (x1 - x2);
            }
        }

        public boolean equals(Object obj) {
            return this.equals(obj);
        }
    }

    public static boolean equals(int[] left, int[] right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }
        if (left.length != right.length) {
            return false;
        }
        for (int i = 0; i < left.length; i++) {
            if (left[i] != right[i]) return false;
        }
        return true;
    }

    public static boolean equals(Object[] left, Object[] right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }
        if (left.length != right.length) {
            return false;
        }
        final NumberComparator numberComparator = new NumberComparator();
        for (int i = left.length - 1; i >= 0; i--) {
            final Object o1 = left[i];
            final Object o2 = right[i];
            if (o1 == null) {
                if (o2 != null) return false;
            } else {
                if (o1 instanceof Number) {
                    if (!(o2 instanceof Number && numberComparator.compare(o1, o2) == 0)) {
                        return false;
                    }
                } else {
                    // Use this way of calling equals in case the element is a List
                    // or any other type which has an equals in DGM
                    if (!invokeEquals(o1, o2)) return false;
                }
            }
        }
        return true;
    }

    private static boolean invokeEquals(Object o1, Object o2) {
        return ((Boolean) InvokerHelper.invokeMethod(o1, "equals", new Object[]{o2})).booleanValue();
    }

    public static boolean equals(Object[] left, List right) {
        return coercedEquals(left, right);
    }

    public static boolean equals(List left, Object[] right) {
        return coercedEquals(right, left);
    }

    private static boolean coercedEquals(Object[] left, List right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }
        if (left.length != right.size()) {
            return false;
        }
        final NumberComparator numberComparator = new NumberComparator();
        for (int i = left.length - 1; i >= 0; i--) {
            final Object o1 = left[i];
            final Object o2 = right.get(i);
            if (o1 == null) {
                if (o2 != null) return false;
            } else {
                if (o1 instanceof Number) {
                    if (!(o2 instanceof Number && numberComparator.compare(o1, o2) == 0)) {
                        return false;
                    }
                } else {
                    // Use this way of calling equals in case the element is a List
                    // or any other type which has an equals in DGM
                    if (!invokeEquals(o1, o2)) return false;
                }
            }
        }
        return true;
    }

    /**
     * Compare two Lists.
     * If numbers exits in the Lists, then they are compared as numbers,
     * for example 2 == 2L.
     *
     * @param left  a List
     * @param right a List
     * @return boolean   <code>true</code> if two Lists equals, <code>false</code> otherwise.
     */
    public static boolean equals(List left, List right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }
        if (left.size() != right.size()) {
            return false;
        }
        final NumberComparator numberComparator = new NumberComparator();
        final Iterator it1 = left.iterator(), it2 = right.iterator();
        while (it1.hasNext()) {
            final Object o1 = it1.next();
            final Object o2 = it2.next();
            if (o1 == null) {
                if (o2 != null) return false;
            } else {
                if (o1 instanceof Number) {
                    if (!(o2 instanceof Number && numberComparator.compare(o1, o2) == 0)) {
                        return false;
                    }
                } else {
                    // Use this way of calling equals in case the element is a List
                    // or any other type which has an equals in DGM
                    if (!invokeEquals(o1, o2)) return false;
                }
            }
        }
        return true;
    }

    /**
     * Create a Set composed of the elements of the first set minus the elements of the collection.
     *
     * TODO: remove using number comparator
     *
     * @param self a set object
     * @param operands the items to remove from the set
     * @return the resulting set
     */
    public static Set minus(Set self, Collection operands) {
        if (self.size() == 0)
            return new HashSet();
        Set ansSet = new HashSet(self);
        ansSet.removeAll(operands);
        return ansSet;
    }

    /**
     * Create a Set composed of the elements of the first set minus the operand.
     *
     * @param self a set object
     * @param operand the operand to remove from the set
     * @return the resulting set
     */
    public static Set minus(Set self, Object operand) {
        Set ansSet = new HashSet();
        Comparator numberComparator = new NumberComparator();
        for (Iterator it = self.iterator(); it.hasNext();) {
            Object o = it.next();
            if (numberComparator.compare(o, operand) != 0) ansSet.add(o);
        }
        return ansSet;
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

        // We can't use the same tactic as for intersection
        // since AbstractCollection only does a remove on the first
        // element it encounters.

        Comparator numberComparator = new NumberComparator();

        if (nlgnSort && (self.get(0) instanceof Comparable)) {
            //n*LOG(n) version
            Set answer;
            if (Number.class.isInstance(self.get(0))) {
                answer = new TreeSet(numberComparator);
                answer.addAll(self);
                for (Iterator it = self.iterator(); it.hasNext();) {
                    Object o = it.next();
                    if (Number.class.isInstance(o)) {
                        for (Iterator it2 = removeMe.iterator(); it2.hasNext();) {
                            Object o2 = it2.next();
                            if (Number.class.isInstance(o2)) {
                                if (numberComparator.compare(o, o2) == 0)
                                    answer.remove(o);
                            }
                        }
                    } else {
                        if (removeMe.contains(o))
                            answer.remove(o);
                    }
                }
            } else {
                answer = new TreeSet(numberComparator);
                answer.addAll(self);
                answer.removeAll(removeMe);
            }

            List ansList = new ArrayList();
            for (Iterator it = self.iterator(); it.hasNext();) {
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
     * Create a Set composed of the elements of the first set minus the operand
     * @param self a List object
     * @param operand an element to remove from the list
     * @return the resulting list with the operand removed
     */
    public static List minus(List self, Object operand) {
        Comparator numberComparator = new NumberComparator();
        List ansList = new ArrayList();
        for (Iterator it = self.iterator(); it.hasNext();) {
            Object o = it.next();
            if (numberComparator.compare(o, operand) != 0) ansList.add(o);
        }
        return ansList;
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
     * Flatten a set
     *
     * @param self a Set
     * @return a flattened Set
     */
    public static Set flatten(Set self) {
        return new HashSet(flatten(self, new LinkedList()));
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
     * @throws IOException if an I/O error occurs.
     */
    public static Writer leftShift(Writer self, Object value) throws IOException {
        InvokerHelper.write(self, value);
        return self;
    }

    /**
     * Implementation of the left shift operator for integral types.  Non integral
     * Number types throw UnsupportedOperationException.
     *
     * @param self a Number object
     * @param operand the shift distance by which to left shift the number
     * @return the resulting number
     */
    public static Number leftShift(Number self, Number operand) {
        return NumberMath.leftShift(self, operand);
    }

    /**
     * Implementation of the right shift operator for integral types.  Non integral
     * Number types throw UnsupportedOperationException.
     *
     * @param self a Number object
     * @param operand the shift distance by which to right shift the number
     * @return the resulting number
     */
    public static Number rightShift(Number self, Number operand) {
        return NumberMath.rightShift(self, operand);
    }

    /**
     * Implementation of the right shift (unsigned) operator for integral types.  Non integral
     * Number types throw UnsupportedOperationException.
     *
     * @param self a Number object
     * @param operand the shift distance by which to right shift (unsigned) the number
     * @return the resulting number
     */
    public static Number rightShiftUnsigned(Number self, Number operand) {
        return NumberMath.rightShiftUnsigned(self, operand);
    }

    /**
     * A helper method so that dynamic dispatch of the writer.write(object) method
     * will always use the more efficient Writable.writeTo(writer) mechanism if the
     * object implements the Writable interface.
     *
     * @param self     a Writer
     * @param writable an object implementing the Writable interface
     * @throws IOException if an I/O error occurs.
     */
    public static void write(Writer self, Writable writable) throws IOException {
        writable.writeTo(self);
    }

    /**
     * Overloads the leftShift operator to provide an append mechanism to add values to a stream.
     *
     * @param self  an OutputStream
     * @param value a value to append
     * @return a Writer
     * @throws IOException if an I/O error occurs.
     */
    public static Writer leftShift(OutputStream self, Object value) throws IOException {
        OutputStreamWriter writer = new FlushingStreamWriter(self);
        leftShift(writer, value);
        return writer;
    }

    /**
     * Overloads the leftShift operator to add objects to an ObjectOutputStream.
     *
     * @param self  an ObjectOutputStream
     * @param value an object to write to the stream
     * @throws IOException if an I/O error occurs.
     * @since 1.1 beta 2
     */
    public static void leftShift(ObjectOutputStream self, Object value) throws IOException {
        self.writeObject(value);
    }

    /**
     * Pipe an InputStream into an OutputStream for efficient stream copying.
     *
     * @param self stream on which to write
     * @param in   stream to read from
     * @return the outputstream itself
     * @throws IOException if an I/O error occurs.
     */
    public static OutputStream leftShift(OutputStream self, InputStream in) throws IOException {
        byte[] buf = new byte[1024];
        while (true) {
            int count = in.read(buf, 0, buf.length);
            if (count == -1) break;
            if (count == 0) {
                Thread.yield();
                continue;
            }
            self.write(buf, 0, count);
        }
        self.flush();
        return self;
    }

    /**
     * Overloads the leftShift operator to provide an append mechanism to add bytes to a stream.
     *
     * @param self  an OutputStream
     * @param value a value to append
     * @return an OutputStream
     * @throws IOException if an I/O error occurs.
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
            if (s.length() != 1) throw new IllegalArgumentException("String of length 1 expected but got a bigger one");
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
    
    public static int size(boolean[] array) {
        return Array.getLength(array);
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
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    public static List toList(char[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    public static List toList(short[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    public static List toList(int[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    public static List toList(long[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    public static List toList(float[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    public static List toList(double[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    private static final char[] tTable = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();

    public static Writable encodeBase64(Byte[] data) {
        return encodeBase64(DefaultTypeTransformation.convertToByteArray(data));
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
                    throw new StringWriterIOException(e);
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
    public static byte[] decodeBase64(String value) {
        int byteShift = 4;
        int tmp = 0;
        boolean done = false;
        final StringBuffer buffer = new StringBuffer();

        for (int i = 0; i != value.length(); i++) {
            final char c = value.charAt(i);
            final int sixBit = (c < 123) ? translateTable[c] : 66;

            if (sixBit < 64) {
                if (done)
                    throw new RuntimeException("= character not at end of base64 value"); // TODO: change this exception type

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
     * Implements the getAt(int) method for primitve type arrays.
     *
     * @param self an array object
     * @param idx the index of interest
     * @return the returned value from the array
     */
    protected static Object primitiveArrayGet(Object self, int idx) {
        return Array.get(self, normaliseIndex(idx, Array.getLength(self)));
    }

    /**
     * Implements the getAt(Range) method for primitve type arrays.
     *
     * @param self an array object
     * @param range the range of indices of interest
     * @return the returned values from the array corresponding to the range

     */
    protected static List primitiveArrayGet(Object self, Range range) {
        List answer = new ArrayList();
        for (Iterator iter = range.iterator(); iter.hasNext();) {
            int idx = DefaultTypeTransformation.intUnbox(iter.next());
            answer.add(primitiveArrayGet(self, idx));
        }
        return answer;
    }

    /**
     * Implements the getAt(Collection) method for primitve type arrays.
     *
     * @param self an array object
     * @param indices the indices of interest
     * @return the returned values from the array
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
                int idx = DefaultTypeTransformation.intUnbox(value);
                answer.add(primitiveArrayGet(self, idx));
            }
        }
        return answer;
    }

    /**
     * Implements the set(int idx) method for primitve type arrays.
     *
     * @param self an object
     * @param idx the index of interest
     * @param newValue the new value to be put into the index of interest
     */
    protected static void primitiveArrayPut(Object self, int idx, Object newValue) {
        Array.set(self, normaliseIndex(idx, Array.getLength(self)), newValue);
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
     * Converts the given string into a Boolean object
     * If the trimmed string is "true", "y" or "1" (ignoring case)
     * then the result is true othewrwise it is false
     *
     * @param self a String
     * @return The Boolean value
     */
    public static Boolean toBoolean(String self) {
        final String trimmed = self.trim();

        if ("true".equalsIgnoreCase(trimmed) || "y".equalsIgnoreCase(trimmed) || "1".equals(trimmed)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
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
     * @param value any Object
     * @return a String
     */
    public static String plus(String left, Object value) {
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
        if (buffer.length() == 0) {
            buffer.append(Character.MIN_VALUE);
        } else {
            char last = buffer.charAt(buffer.length() - 1);
            if (last == Character.MAX_VALUE) {
                buffer.append(Character.MIN_VALUE);
            } else {
                char next = last;
                next++;
                buffer.setCharAt(buffer.length() - 1, next);
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
        if (buffer.length() == 0) throw new IllegalArgumentException("the string is empty");
        char last = buffer.charAt(buffer.length() - 1);
        if (last == Character.MIN_VALUE) {
            buffer.deleteCharAt(buffer.length() - 1);
        } else {
            char next = last;
            next--;
            buffer.setCharAt(buffer.length() - 1, next);
        }
        return buffer.toString();
    }

    /**
     * Executes the given string as a command line process. For more control
     * over the process mechanism in JDK 1.5 you can use java.lang.ProcessBuilder.
     *
     * @param self a command line String
     * @return the Process which has just started for this command line string
     * @throws IOException if an IOException occurs.
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
     *                     parameters as separate items in the array.
     * @return the Process which has just started for this command line string.
     * @throws IOException if an IOException occurs.
     */
    public static Process execute(String[] commandArray) throws IOException {
        return Runtime.getRuntime().exec(commandArray);
    }

    /**
     * Executes the command specified by the <code>self</code> with environments <code>envp</code>
     * under the working directory <code>dir</code>.
     * For more control over the process mechanism in JDK 1.5 you can use <code>java.lang.ProcessBuilder</code>.
     *
     * @param self a command line String to be executed.
     * @param envp an array of Strings, each element of which
     *             has environment variable settings in the format
     *             <i>name</i>=<i>value</i>, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the environment of the current process.
     * @param dir  the working directory of the subprocess, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the working directory of the current process.
     * @return the Process which has just started for this command line string.
     * @throws IOException if an IOException occurs.
     */
    public static Process execute(String self, final String[] envp, File dir) throws IOException {
        return Runtime.getRuntime().exec(self, envp, dir);
    }

    /**
     * Executes the command specified by the <code>String</code> list that is the parameter.
     * The first item in the array is the command the others are the parameters. All entries
     * must be <code>String</code>s.  For more control over the process mechanism in JDK 1.5 you
     * can use <code>java.lang.ProcessBuilder</code>.
     *
     * @param commandList a list of <code>String<code> containing the command name and
     *                    parameters as separate items in the list.
     * @return the Process which has just started for this command line string.
     * @throws IOException if an IOException occurs.
     */
    public static Process execute(List commandList) throws IOException {
        final String[] commandArray = new String[commandList.size()];
        Iterator it = commandList.iterator();
        for (int i = 0; it.hasNext(); ++i) {
            commandArray[i] = it.next().toString();
        }
        return execute(commandArray);
    }

    /**
     * Executes the command specified by the <code>self</code> with environments <code>envp</code>
     * under the working directory <code>dir</code>.
     * For more control over the process mechanism in JDK 1.5 you can use <code>java.lang.ProcessBuilder</code>.
     *
     * @param self a command line String to be executed.
     * @param envp a List of Strings, each member of which
     *             has environment variable settings in the format
     *             <i>name</i>=<i>value</i>, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the environment of the current process.
     * @param dir  the working directory of the subprocess, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the working directory of the current process.
     * @return the Process which has just started for this command line string.
     * @throws IOException if an IOException occurs.
     */
    public static Process execute(String self, List envp, File dir) throws IOException {
        if (envp==null) {
            return execute(self, (String[]) null, dir);
        }
        String[] commandArray = new String[envp.size()];
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
            return toMapString((Map) value);
        else if (value instanceof Collection)
            return toListString((Collection) value);
        else if (value instanceof Object[])
            return toArrayString((Object[]) value);
        else
            return InvokerHelper.toString(value);
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
     * Multiply a BigDecimal and a Double.
     * Note: This method was added to enforce the Groovy rule of
     * BigDecimal*Double == Double. Without this method, the
     * multiply(BigDecimal) method in BigDecimal would respond
     * and return a BigDecimal instead. Since BigDecimal is prefered
     * over Number, the Number*Number method is not choosen as in older
     * versions of Groovy.
     *
     * @param left  a BigDecimal
     * @param right a Double
     * @return the multiplication of both
     */
    public static Number multiply(BigDecimal left, Double right) {
        return NumberMath.multiply(left, right);
    }

    /**
     * Multiply a BigDecimal and a BigInteger.
     * Note: This method was added to enforce the Groovy rule of
     * BigDecimal*long == long. Without this method, the
     * multiply(BigDecimal) method in BigDecimal would respond
     * and return a BigDecimal instead. Since BigDecimal is prefered
     * over Number, the Number*Number method is not choosen as in older
     * versions of Groovy. Biginteger is the fallback for all integer
     * types in Groovy
     *
     * @param left  a BigDecimal
     * @param right a BigInteger
     * @return the multiplication of both
     */
    public static Number multiply(BigDecimal left, BigInteger right) {
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
        if ((double) ((int) answer) == answer) {
            return new Integer((int) answer);
        } else if ((double) ((long) answer) == answer) {
            return new Long((long) answer);
        } else {
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
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to + ")");
    }

    public static void upto(long self, Number to, Closure closure) {
        long to1 = to.longValue();
        if (self <= to1) {
            for (long i = self; i <= to1; i++) {
                closure.call(new Long(i));
            }
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to + ")");
    }

    public static void upto(Long self, Number to, Closure closure) {
        long self1 = self.longValue();
        long to1 = to.longValue();
        if (self1 <= to1) {
            for (long i = self1; i <= to1; i++) {
                closure.call(new Long(i));
            }
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to + ")");
    }

    public static void upto(float self, Number to, Closure closure) {
        float to1 = to.floatValue();
        if (self <= to1) {
            for (float i = self; i <= to1; i++) {
                closure.call(new Float(i));
            }
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to + ")");
    }

    public static void upto(Float self, Number to, Closure closure) {
        float self1 = self.floatValue();
        float to1 = to.floatValue();
        if (self1 <= to1) {
            for (float i = self1; i <= to1; i++) {
                closure.call(new Float(i));
            }
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to + ")");
    }

    public static void upto(Double self, Number to, Closure closure) {
        double self1 = self.doubleValue();
        double to1 = to.doubleValue();
        if (self1 <= to1) {
            for (double i = self1; i <= to1; i++) {
                closure.call(new Double(i));
            }
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to + ")");
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
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to + ")");
        } else if (to instanceof BigInteger) {
            final BigInteger one = new BigInteger("1");
            BigInteger to1 = (BigInteger) to;
            if (self.compareTo(to1) <= 0) {
                for (BigInteger i = self; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to + ")");
        } else {
            final BigInteger one = new BigInteger("1");
            BigInteger to1 = new BigInteger("" + to);
            if (self.compareTo(to1) <= 0) {
                for (BigInteger i = self; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to + ")");
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
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to + ")");
        } else if (to instanceof BigInteger) {
            BigDecimal to1 = new BigDecimal((BigInteger) to);
            if (self.compareTo(to1) <= 0) {
                for (BigDecimal i = self; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to + ")");
        } else {
            BigDecimal to1 = new BigDecimal("" + to);
            if (self.compareTo(to1) <= 0) {
                for (BigDecimal i = self; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to + ")");
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
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
    }

    public static void downto(long self, Number to, Closure closure) {
        long to1 = to.longValue();
        if (self >= to1) {
            for (long i = self; i >= to1; i--) {
                closure.call(new Long(i));
            }
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
    }

    public static void downto(Long self, Number to, Closure closure) {
        long self1 = self.longValue();
        long to1 = to.longValue();
        if (self1 >= to1) {
            for (long i = self1; i >= to1; i--) {
                closure.call(new Long(i));
            }
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
    }

    public static void downto(float self, Number to, Closure closure) {
        float to1 = to.floatValue();
        if (self >= to1) {
            for (float i = self; i >= to1; i--) {
                closure.call(new Float(i));
            }
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
    }

    public static void downto(Float self, Number to, Closure closure) {
        float self1 = self.floatValue();
        float to1 = to.floatValue();
        if (self1 >= to1) {
            for (float i = self1; i >= to1; i--) {
                closure.call(new Float(i));
            }
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
    }

    public static void downto(double self, Number to, Closure closure) {
        double to1 = to.doubleValue();
        if (self >= to1) {
            for (double i = self; i >= to1; i--) {
                closure.call(new Double(i));
            }
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
    }

    public static void downto(Double self, Number to, Closure closure) {
        double self1 = self.doubleValue();
        double to1 = to.doubleValue();
        if (self1 >= to1) {
            for (double i = self1; i >= to1; i--) {
                closure.call(new Double(i));
            }
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
    }

    public static void downto(BigInteger self, Number to, Closure closure) {
        if (to instanceof BigDecimal) {
            final BigDecimal one = new BigDecimal("1.0");
            final BigDecimal to1 = (BigDecimal) to;
            final BigDecimal selfD = new BigDecimal(self);
            if (selfD.compareTo(to1) >= 0) {
                for (BigDecimal i = selfD; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i.toBigInteger());
                }
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
        } else if (to instanceof BigInteger) {
            final BigInteger one = new BigInteger("1");
            final BigInteger to1 = (BigInteger) to;
            if (self.compareTo(to1) >= 0) {
                for (BigInteger i = self; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
        } else {
            final BigInteger one = new BigInteger("1");
            final BigInteger to1 = new BigInteger("" + to);
            if (self.compareTo(to1) >= 0) {
                for (BigInteger i = self; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
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
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
        } else if (to instanceof BigInteger) {
            BigDecimal to1 = new BigDecimal((BigInteger) to);
            if (self.compareTo(to1) >= 0) {
                for (BigDecimal i = self; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
        } else {
            BigDecimal to1 = new BigDecimal("" + to);
            if (self.compareTo(to1) >= 0) {
                for (BigDecimal i = self; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
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
            } else if (stepNumber1.compareTo(zero) < 0 && to1.compareTo(self1) < 0) {
                for (BigDecimal i = self1; i.compareTo(to1) > 0; i = i.add(stepNumber1)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self1 + ".step(" + to1 + ", " + stepNumber1 + ")");
        } else if (self instanceof BigInteger || to instanceof BigInteger || stepNumber instanceof BigInteger) {
            final BigInteger zero = new BigInteger("0");
            BigInteger self1 = (self instanceof BigInteger) ? (BigInteger) self : new BigInteger("" + self);
            BigInteger to1 = (to instanceof BigInteger) ? (BigInteger) to : new BigInteger("" + to);
            BigInteger stepNumber1 = (stepNumber instanceof BigInteger) ? (BigInteger) stepNumber : new BigInteger("" + stepNumber);
            if (stepNumber1.compareTo(zero) > 0 && to1.compareTo(self1) > 0) {
                for (BigInteger i = self1; i.compareTo(to1) < 0; i = i.add(stepNumber1)) {
                    closure.call(i);
                }
            } else if (stepNumber1.compareTo(zero) < 0 && to1.compareTo(self1) < 0) {
                for (BigInteger i = self1; i.compareTo(to1) > 0; i = i.add(stepNumber1)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self1 + ".step(" + to1 + ", " + stepNumber1 + ")");
        } else {
            int self1 = self.intValue();
            int to1 = to.intValue();
            int stepNumber1 = stepNumber.intValue();
            if (stepNumber1 > 0 && to1 > self1) {
                for (int i = self1; i < to1; i += stepNumber1) {
                    closure.call(new Integer(i));
                }
            } else if (stepNumber1 < 0 && to1 < self1) {
                for (int i = self1; i > to1; i += stepNumber1) {
                    closure.call(new Integer(i));
                }
            } else
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
        return Integer.valueOf(self.trim());
    }

    /**
     * Parse a String into a Long
     *
     * @param self a String
     * @return a Long
     */
    public static Long toLong(String self) {
        return Long.valueOf(self.trim());
    }

    /**
     * Parse a String into a Float
     *
     * @param self a String
     * @return a Float
     */
    public static Float toFloat(String self) {
        return Float.valueOf(self.trim());
    }

    /**
     * Parse a String into a Double
     *
     * @param self a String
     * @return a Double
     */
    public static Double toDouble(String self) {
        return Double.valueOf(self.trim());
    }

    /**
     * Parse a String into a BigInteger
     *
     * @param self a String
     * @return a BigInteger
     */
    public static BigInteger toBigInteger(String self) {
        return new BigInteger(self.trim());
    }

    /**
     * Parse a String into a BigDecimal
     *
     * @param self a String
     * @return a BigDecimal
     */
    public static BigDecimal toBigDecimal(String self) {
        return new BigDecimal(self.trim());
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

    /**
     * Transform a Number into a Long
     *
     * @param self a Number
     * @return an Long
     */
    public static Long toLong(Number self) {
        return new Long(self.longValue());
    }

    /**
     * Transform a Number into a Float
     *
     * @param self a Number
     * @return an Float
     */
    public static Float toFloat(Number self) {
        return new Float(self.floatValue());
    }

    /**
     * Transform a Number into a Double
     *
     * @param self a Number
     * @return an Double
     */
    public static Double toDouble(Number self) {
        return new Double(self.doubleValue());
    }

    /**
     * Transform a Number into a BigDecimal
     *
     * @param self a Number
     * @return an BigDecimal
     */
    public static BigDecimal toBigDecimal(Number self) {
        return new BigDecimal(self.doubleValue());
    }

    public static Object asType(Number self, Class c) {
        if (c == BigDecimal.class) {
            return toBigDecimal(self);
        } else if (c == BigInteger.class) {
            return toBigInteger(self);
        } else if (c == Double.class) {
            return toDouble(self);
        } else if (c == Float.class) {
            return toFloat(self);
        }
        return asType((Object) self, c);
    }

    /**
     * Transform a Number into a BigInteger
     *
     * @param self a Number
     * @return an BigInteger
     */
    public static BigInteger toBigInteger(Number self) {
        return new BigInteger(Long.toString(self.longValue()));
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
     * Increments a java.sql.Date by a day
     *
     * @param self a java.sql.Date
     * @return the next days date
     */
    public static java.sql.Date next(java.sql.Date self) {
        return new java.sql.Date(next((Date) self).getTime());
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
     * Decrement a java.sql.Date by a day
     *
     * @param self a java.sql.Date
     * @return the previous days date
     */
    public static java.sql.Date previous(java.sql.Date self) {
        return new java.sql.Date(previous((Date) self).getTime());
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
     * Adds a number of days to this date and returns the new date
     *
     * @param self a java.sql.Date
     * @param days the number of days to increase
     * @return the new date
     */
    public static java.sql.Date plus(java.sql.Date self, int days) {
        return new java.sql.Date(plus((Date) self, days).getTime());
    }

    /**
     * Subtracts a number of days from this date and returns the new date
     *
     * @param self a Date
     * @param days the number of days to subtract
     * @return the new date
     */
    public static Date minus(Date self, int days) {
        return plus(self, -days);
    }

    /**
     * Subtracts a number of days from this date and returns the new date
     *
     * @param self a java.sql.Date
     * @param days the number of days to subtract
     * @return the new date
     */
    public static java.sql.Date minus(java.sql.Date self, int days) {
        return new java.sql.Date(minus((Date) self, days).getTime());
    }

    // Boolean based methods
    //-------------------------------------------------------------------------

    public static Boolean and(Boolean left, Boolean right) {
        return Boolean.valueOf(left.booleanValue() & right.booleanValue());
    }

    public static Boolean or(Boolean left, Boolean right) {
        return Boolean.valueOf(left.booleanValue() | right.booleanValue());
    }

    public static Boolean xor(Boolean left, Boolean right) {
        return Boolean.valueOf(left.booleanValue() ^ right.booleanValue());
    }

//    public static Boolean negate(Boolean left) {
//        return Boolean.valueOf(!left.booleanValue());
//    }

    // File and stream based methods
    //-------------------------------------------------------------------------

    /**
     * Helper method to create an object input stream from the given file.
     *
     * @param file a file
     * @return an object input stream
     * @throws IOException if an IOException occurs.
     */
    public static ObjectInputStream newObjectInputStream(File file) throws IOException {
        return new ObjectInputStream(new FileInputStream(file));
    }

    /**
     * Helper method to create an object output stream from the given file.
     *
     * @param file a file
     * @return an object output stream
     * @throws IOException if an IOException occurs.
     */
    public static ObjectOutputStream newObjectOutputStream(File file) throws IOException {
        return new ObjectOutputStream(new FileOutputStream(file));
    }

    /**
     * Iterates through the given file object by object
     *
     * @param self    a File
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @throws ClassNotFoundException if the class  is not found.
     */
    public static void eachObject(File self, Closure closure) throws IOException, ClassNotFoundException {
        eachObject(newObjectInputStream(self), closure);
    }

    /**
     * Iterates through the given object stream object by object. The
     * ObjectInputStream is closed afterwards.
     *
     * @param ois     an ObjectInputStream, closed after the operation
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @throws ClassNotFoundException if the class  is not found.
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
            InputStream temp = ois;
            ois = null;
            temp.close();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                }
                catch (Exception e) {
                    // ignore this exception since there
                    // has to be another already
                    LOG.warning("Caught exception closing ObjectInputStream: " + e);
                }
            }
        }
    }

    /**
     * Helper method to create a new ObjectInputStream for a file and then
     * passes it into the closure and ensures its closed again afterwords
     *
     * @param file a File
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     */
    public static void withObjectInputStream(File file, Closure closure) throws IOException {
        withStream(newObjectInputStream(file), closure);
    }

    /**
     * Helper method to create a new ObjectOutputStream for a file and then
     * passes it into the closure and ensures its closed again afterwords
     *
     * @param file a File
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     */
    public static void withObjectOutputStream(File file, Closure closure) throws IOException {
        withStream(newObjectOutputStream(file), closure);
    }

    /**
     * Iterates through the given file line by line
     *
     * @param self    a File
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     */
    public static void eachLine(File self, Closure closure) throws IOException {
        eachLine(newReader(self), closure);
    }

    /**
     * Iterates through the given reader line by line. The
     * Reader is closed afterwards
     *
     * @param self    a Reader, closed after the method returns
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     */
    public static void eachLine(Reader self, Closure closure) throws IOException {
        BufferedReader br /* = null */;

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
            Reader temp = self;
            self = null;
            temp.close();
        } finally {
            closeReaderWithWarning(self);
            closeReaderWithWarning(br);
        }
    }

    /**
     * Iterates through the given file line by line, splitting on the seperator
     *
     * @param self    a File
     * @param sep     a String separator
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     */
    public static void splitEachLine(File self, String sep, Closure closure) throws IOException {
        splitEachLine(newReader(self), sep, closure);
    }

    /**
     * Iterates through the given reader line by line, splitting on the separator.
     * The Reader is closed afterwards.
     *
     * @param self    a Reader, closed after the method returns
     * @param sep     a String separator
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     */
    public static void splitEachLine(Reader self, String sep, Closure closure) throws IOException {
        BufferedReader br /* = null */;

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
                    List vals = Arrays.asList(line.split(sep));
                    closure.call(vals);
                }
            }
            Reader temp = self;
            self = null;
            temp.close();
        } finally {
            closeReaderWithWarning(self);
            closeReaderWithWarning(br);
        }
    }

    /**
     * Read a single, whole line from the given Reader
     *
     * @param self a Reader
     * @return a line
     * @throws IOException if an IOException occurs.
     */
    public static String readLine(Reader self) throws IOException {
        BufferedReader br /* = null */;

        if (self instanceof BufferedReader) {
            br = (BufferedReader) self;
        } else {
            br = new BufferedReader(self); // todo dk: bug! will return null on second call
        }
        return br.readLine();
    }

    /**
     * Read a single, whole line from the given InputStream
     *
     * @param stream an InputStream
     * @return a line
     * @throws IOException if an IOException occurs.
     */
    public static String readLine(InputStream stream) throws IOException {
        return readLine(new InputStreamReader(stream));
    }

    /**
     * Reads the file into a list of Strings for each line
     *
     * @param file a File
     * @return a List of lines
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
     */
    public static String getText(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        return getText(bufferedReader);
    }

    /**
     * Reads the content of the BufferedReader and returns it as a String.
     * The BufferedReader is closed afterwards.
     *
     * @param reader a BufferedReader whose content we want to read
     * @return a String containing the content of the buffered reader
     * @throws IOException if an IOException occurs.
     */
    public static String getText(BufferedReader reader) throws IOException {
        StringBuffer answer = new StringBuffer();
        // reading the content of the file within a char buffer
        // allow to keep the correct line endings
        char[] charBuffer = new char[4096];
        int nbCharRead /* = 0*/;
        try {
            while ((nbCharRead = reader.read(charBuffer)) != -1) {
                // appends buffer
                answer.append(charBuffer, 0, nbCharRead);
            }
            Reader temp = reader;
            reader = null;
            temp.close();
        } finally {
            closeReaderWithWarning(reader);
        }
        return answer.toString();
    }

    /**
     * Write the text and append a new line (depending on the platform
     * line-ending)
     *
     * @param writer a BufferedWriter
     * @param line   the line to write
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
     */
    public static void write(File file, String text) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = newWriter(file);
            writer.write(text);
            writer.flush();

            Writer temp = writer;
            writer = null;
            temp.close();
        } finally {
            closeWriterWithWarning(writer);
        }
    }

    /**
     * Write the text to the File.
     *
     * @param file a File
     * @param text the text to write to the File
     * @return the original file
     * @throws IOException if an IOException occurs.
     */
    public static File leftShift(File file, Object text) throws IOException {
        append(file, text);
        return file;
    }

    /**
     * Write the text to the File with a specified encoding.
     *
     * @param file    a File
     * @param text    the text to write to the File
     * @param charset the charset used
     * @throws IOException if an IOException occurs.
     */
    public static void write(File file, String text, String charset) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = newWriter(file, charset);
            writer.write(text);
            writer.flush();

            Writer temp = writer;
            writer = null;
            temp.close();
        } finally {
            closeWriterWithWarning(writer);
        }
    }

    /**
     * Append the text at the end of the File
     *
     * @param file a File
     * @param text the text to append at the end of the File
     * @throws IOException if an IOException occurs.
     */
    public static void append(File file, Object text) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = newWriter(file, true);
            InvokerHelper.write(writer, text);
            writer.flush();

            Writer temp = writer;
            writer = null;
            temp.close();
        } finally {
            closeWriterWithWarning(writer);
        }
    }

    /**
     * Append the text at the end of the File with a specified encoding
     *
     * @param file    a File
     * @param text    the text to append at the end of the File
     * @param charset the charset used
     * @throws IOException if an IOException occurs.
     */
    public static void append(File file, Object text, String charset) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = newWriter(file, charset, true);
            InvokerHelper.write(writer, text);
            writer.flush();

            Writer temp = writer;
            writer = null;
            temp.close();
        } finally {
            closeWriterWithWarning(writer);
        }
    }

    /**
     * Reads the reader into a list of Strings for each line
     *
     * @param reader a Reader
     * @return a List of lines
     * @throws IOException if an IOException occurs.
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
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     */
    private static void checkDir(File dir) throws FileNotFoundException, IllegalArgumentException {
        if (!dir.exists())
            throw new FileNotFoundException(dir.getAbsolutePath());
        if (!dir.isDirectory())
            throw new IllegalArgumentException("The provided File object is not a directory: " + dir.getAbsolutePath());
    }

    /**
     * Common code for {@link #eachFile(File, Closure)} and {@link #eachDir(File, Closure)}
     * @param self a file object
     * @param closure the closure to invoke
     * @param onlyDir if normal file should be skipped
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     */
    private static void eachFile(final File self, final Closure closure, final boolean onlyDir) 
    		throws FileNotFoundException, IllegalArgumentException {
        checkDir(self);
        final File[] files = self.listFiles();
        // null check because of http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4803836
        if (files==null) return;
        for (int i = 0; i < files.length; i++) {
        	if (!onlyDir || files[i].isDirectory()) {
        		closure.call(files[i]);
        	}
        }
    }

    /**
     * Invokes the closure for each file in the given directory
     *
     * @param self    a File
     * @param closure a closure
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     */
    public static void eachFile(final File self, final Closure closure) throws FileNotFoundException, IllegalArgumentException {
    	eachFile(self, closure, false);
    }

    /**
     * Invokes the closure for each directory in the given directory,
     * ignoring regular files.
     *
     * @param self    a directory
     * @param closure a closure
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     */
    public static void eachDir(File self, Closure closure) throws FileNotFoundException, IllegalArgumentException {
    	eachFile(self, closure, true);
    }

    /**
     * Common code for {@link #eachFileRecurse(File, Closure)} and {@link #eachDirRecurse(File, Closure)}
     * @param self a file object
     * @param closure the closure to invoke on each file
     * @param onlyDir if normal file should be skipped
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     */
    private static void eachFileRecurse(final File self, final Closure closure, final boolean onlyDir) 
    		throws FileNotFoundException, IllegalArgumentException {
        checkDir(self);
        final File[] files = self.listFiles();
        // null check because of http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4803836
        if (files==null) return;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                closure.call(files[i]);
                eachFileRecurse(files[i], closure, onlyDir);
            }
            else if (!onlyDir) {
                closure.call(files[i]);
            }
        }
    }

    /**
     * Invokes the closure for each file in the given directory and recursively.
     * It is a depth-first exploration, directories are included in the search.
     *
     * @param self    a File
     * @param closure a closure
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     */
    public static void eachFileRecurse(File self, Closure closure) throws FileNotFoundException, IllegalArgumentException {
    	eachFileRecurse(self, closure, false);
    }

    /**
     * Invokes the closure for each directory in the given directory and recursively ignoring regular files.
     * It is a depth-first exploration, directories are included in the search.
     *
     * @param self    a directory
     * @param closure a closure
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     * @since 1.1 beta 1
     */
    public static void eachDirRecurse(final File self, final Closure closure) throws FileNotFoundException, IllegalArgumentException {
    	eachFileRecurse(self, closure, true);
    }

    /**
     * Common code for {@link #eachFileMatch(File, Object, Closure)} and {@link #eachDirMatch(File, Object, Closure)}
     * @param self    a file
     * @param filter  the filter to perform on the directory (using the isCase(object) method)
     * @param closure the closure to invoke
     * @param onlyDir if normal file should be skipped
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     */
    private static void eachFileMatch(final File self, final Object filter, final Closure closure, final boolean onlyDir)
    		throws FileNotFoundException, IllegalArgumentException {
        checkDir(self);
        final File[] files = self.listFiles();
        // null check because of http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4803836
        if (files==null) return;
        final MetaClass metaClass = InvokerHelper.getMetaClass(filter);
        for (int i = 0; i < files.length; i++) {
        	final File currentFile = files[i];
            if ((!onlyDir || currentFile.isDirectory())
            		&& DefaultTypeTransformation.castToBoolean(metaClass.invokeMethod(filter, "isCase", currentFile.getName()))) {
                closure.call(currentFile);
            }
        }
    }

    /**
     * Invokes the closure for each file matching the given filter in the given directory
     * - calling the isCase() method used by switch statements.  This method can be used
     * with different kinds of filters like regular expresions, classes, ranges etc.
     *
     * @param self    a file
     * @param filter  the filter to perform on the directory (using the isCase(object) method)
     * @param closure the closure to invoke
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     */
    public static void eachFileMatch(final File self, final Object filter, final Closure closure) 
    		throws FileNotFoundException, IllegalArgumentException {
    	eachFileMatch(self, filter, closure, false);
    }

    /**
     * Invokes the closure for each directory matching the given filter in the given directory
     * - calling the isCase() method used by switch statements.  This method can be used
     * with different kinds of filters like regular expresions, classes, ranges etc.
     *
     * @param self    a file
     * @param filter  the filter to perform on the directory (using the isCase(object) method)
     * @param closure the closure to invoke
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     * @since 1.1 beta 1
     */
    public static void eachDirMatch(final File self, final Object filter, final Closure closure) throws FileNotFoundException, IllegalArgumentException {
    	eachFileMatch(self, filter, closure, true);
    }

    /**
     * Allow simple syntax for using timers.
     *
     * @param timer   a timer object
     * @param delay   the delay in milliseconds before running the closure code
     * @param closure the closure to invoke
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
     * @throws IOException if an IOException occurs.
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
    public static BufferedReader newReader(InputStream self) {
        return new BufferedReader(new InputStreamReader(self));
    }

    /**
     * Helper method to create a new BufferedReader for a file and then
     * passes it into the closure and ensures its closed again afterwords
     *
     * @param file a file object
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     */
    public static void withReader(File file, Closure closure) throws IOException {
        withReader(newReader(file), closure);
    }

    /**
     * Helper method to create a buffered output stream for a file
     *
     * @param file a file object
     * @return the created OutputStream
     * @throws IOException if an IOException occurs.
     */
    public static BufferedOutputStream newOutputStream(File file) throws IOException {
        return new BufferedOutputStream(new FileOutputStream(file));
    }

    /**
     * Helper method to create a data output stream for a file
     *
     * @param file a file object
     * @return the created DataOutputStream
     * @throws IOException if an IOException occurs.
     */
    public static DataOutputStream newDataOutputStream(File file) throws IOException {
        return new DataOutputStream(new FileOutputStream(file));
    }

    /**
     * Helper method to create a new OutputStream for a file and then
     * passes it into the closure and ensures its closed again afterwords
     *
     * @param file a File
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     */
    public static void withOutputStream(File file, Closure closure) throws IOException {
        withStream(newOutputStream(file), closure);
    }

    /**
     * Helper method to create a new InputStream for a file and then
     * passes it into the closure and ensures its closed again afterwords
     *
     * @param file a File
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     */
    public static void withInputStream(File file, Closure closure) throws IOException {
        withStream(newInputStream(file), closure);
    }

    /**
     * Helper method to create a new DataOutputStream for a file and then
     * passes it into the closure and ensures its closed again afterwords
     *
     * @param file a File
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     */
    public static void withDataOutputStream(File file, Closure closure) throws IOException {
        withStream(newDataOutputStream(file), closure);
    }

    /**
     * Helper method to create a new DataInputStream for a file and then
     * passes it into the closure and ensures its closed again afterwords
     *
     * @param file a File
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     */
    public static void withDataInputStream(File file, Closure closure) throws IOException {
        withStream(newDataInputStream(file), closure);
    }

    /**
     * Helper method to create a buffered writer for a file
     *
     * @param file a File
     * @return a BufferedWriter
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
     */
    public static BufferedWriter newWriter(File file, String charset) throws IOException {
        return newWriter(file, charset, false);
    }

    /**
     * Write a Byte Order Mark at the begining of the file
     *
     * @param stream    the FileOuputStream to write the BOM to
     * @param bigEndian true if UTF 16 Big Endian or false if Low Endian
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
     */
    public static void withWriterAppend(File file, String charset, Closure closure) throws IOException {
        withWriter(newWriter(file, charset, true), closure);
    }

    /**
     * Helper method to create a new PrintWriter for a file
     *
     * @param file a File
     * @throws IOException if an IOException occurs.
     * @return the created PrintWriter
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
     * @throws IOException if an IOException occurs.
     */
    public static PrintWriter newPrintWriter(File file, String charset) throws IOException {
        return new PrintWriter(newWriter(file, charset));
    }

    /**
     * Helper method to create a new PrintWriter for a file and then
     * passes it into the closure and ensures its closed again afterwords
     *
     * @param file a File
     * @param closure the closure to invoke with the PrintWriter
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
     */
    public static void withWriter(Writer writer, Closure closure) throws IOException {
        try {
            closure.call(writer);
            writer.flush();

            Writer temp = writer;
            writer = null;
            temp.close();
        } finally {
            closeWriterWithWarning(writer);
        }
    }

    /**
     * Allows a Reader to be used, calling the closure with the reader
     * and then ensuring that the reader is closed down again irrespective
     * of whether exceptions occur or the
     *
     * @param reader  the reader which is used and then closed
     * @param closure the closure that the writer is passed into
     * @throws IOException if an IOException occurs.
     */
    public static void withReader(Reader reader, Closure closure) throws IOException {
        try {
            closure.call(reader);

            Reader temp = reader;
            reader = null;
            temp.close();
        } finally {
            closeReaderWithWarning(reader);
        }
    }

    /**
     * Allows a InputStream to be used, calling the closure with the stream
     * and then ensuring that the stream is closed down again irrespective
     * of whether exceptions occur or the
     *
     * @param stream  the stream which is used and then closed
     * @param closure the closure that the stream is passed into
     * @throws IOException if an IOException occurs.
     */
    public static void withStream(InputStream stream, Closure closure) throws IOException {
        try {
            closure.call(stream);

            InputStream temp = stream;
            stream = null;
            temp.close();
        } finally {
            closeInputStreamWithWarning(stream);
        }
    }

    /**
     * Reads the stream into a list of Strings for each line
     *
     * @param stream a stream
     * @return a List of lines
     * @throws IOException if an IOException occurs.
     */
    public static List readLines(InputStream stream) throws IOException {
        return readLines(new BufferedReader(new InputStreamReader(stream)));
    }

    /**
     * Iterates through the given stream line by line
     *
     * @param stream  a stream
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     */
    public static void eachLine(InputStream stream, Closure closure) throws IOException {
        eachLine(new InputStreamReader(stream), closure);
    }

    /**
     * Iterates through the lines read from the URL's associated input stream
     *
     * @param url     a URL to open and read
     * @param closure a closure to apply on each line
     * @throws IOException if an IOException occurs.
     */
    public static void eachLine(URL url, Closure closure) throws IOException {
        eachLine(url.openConnection().getInputStream(), closure);
    }

    /**
     * Helper method to create a new BufferedReader for a URL and then
     * passes it into the closure and ensures its closed again afterwords.
     *
     * @param url a URL
     * @param closure the closure to invoke with the reader
     * @throws IOException if an IOException occurs.
     */
    public static void withReader(URL url, Closure closure) throws IOException {
        withReader(url.openConnection().getInputStream(), closure);
    }

    /**
     * Helper method to create a new BufferedReader for a stream and then
     * passes it into the closure and ensures its closed again afterwords.
     *
     * @param in a stream
     * @param closure the closure to invoke with the InputStream
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
     */
    public static void withWriter(OutputStream stream, String charset, Closure closure) throws IOException {
        withWriter(new OutputStreamWriter(stream, charset), closure);
    }

    /**
     * Allows a OutputStream to be used, calling the closure with the stream
     * and then ensuring that the stream is closed down again irrespective
     * of whether exceptions occur.
     *
     * @param os      the stream which is used and then closed
     * @param closure the closure that the stream is passed into
     * @throws IOException if an IOException occurs.
     */
    public static void withStream(OutputStream os, Closure closure) throws IOException {
        try {
            closure.call(os);
            os.flush();

            OutputStream temp = os;
            os = null;
            temp.close();
        } finally {
            closeOutputStreamWithWarning(os);
        }
    }

    /**
     * Helper method to create a buffered input stream for a file
     *
     * @param file a File
     * @return a BufferedInputStream of the file
     * @throws FileNotFoundException if the file is not found.
     */
    public static BufferedInputStream newInputStream(File file) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(file));
    }

    /**
     * Helper method to create a data input stream for a file
     *
     * @param file a File
     * @return a DataInputStream of the file
     * @throws FileNotFoundException if the file is not found.
     */
    public static DataInputStream newDataInputStream(File file) throws FileNotFoundException {
        return new DataInputStream(new FileInputStream(file));
    }

    /**
     * Traverse through each byte of the specified File
     *
     * @param self    a File
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     */
    public static void eachByte(File self, Closure closure) throws IOException {
        BufferedInputStream is = newInputStream(self);
        eachByte(is, closure);
    }

    /**
     * Traverse through each byte of the specified stream. The
     * stream is closed afterwards.
     *
     * @param is      stream to iterate over, closed after the method call
     * @param closure closure to apply to each byte
     * @throws IOException if an IOException occurs.
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

            InputStream temp = is;
            is = null;
            temp.close();
        } finally {
            closeInputStreamWithWarning(is);
        }
    }

    /**
     * Traverse through each byte of the specified URL
     *
     * @param url     url to iterate over
     * @param closure closure to apply to each byte
     * @throws IOException if an IOException occurs.
     */
    public static void eachByte(URL url, Closure closure) throws IOException {
        InputStream is = url.openConnection().getInputStream();
        eachByte(is, closure);
    }

    /**
     * Transforms the characters from a self with a Closure and
     * writes them to a writer.
     *
     * @param self a Reader object
     * @param writer a Writer to receive the transformed characters
     * @param closure a closure that performs the required transformation
     * @throws IOException if an IOException occurs.
     */
    public static void transformChar(Reader self, Writer writer, Closure closure) throws IOException {
        int c;
        try {
            char[] chars = new char[1];
            while ((c = self.read()) != -1) {
                chars[0] = (char) c;
                writer.write((String) closure.call(new String(chars)));
            }
            writer.flush();

            Writer temp2 = writer;
            writer = null;
            temp2.close();
            Reader temp1 = self;
            self = null;
            temp1.close();
        } finally {
            closeReaderWithWarning(self);
            closeWriterWithWarning(writer);
        }
    }

    /**
     * Transforms the lines from a reader with a Closure and
     * write them to a writer. Both Reader and Writer are
     * closed after the operation
     *
     * @param reader  Lines of text to be transformed. Reader is closed afterwards.
     * @param writer  Where transformed lines are written. Writer is closed afterwards.
     * @param closure Single parameter closure that is called to transform each line of
     *                text from the reader, before writing it to the writer.
     * @throws IOException if an IOException occurs.
     */
    public static void transformLine(Reader reader, Writer writer, Closure closure) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        BufferedWriter bw = new BufferedWriter(writer);
        String line;
        try {
            while ((line = br.readLine()) != null) {
                Object o = closure.call(line);
                if (o != null) {
                    bw.write(o.toString());
                    bw.newLine();
                }
            }
            bw.flush();

            Writer temp2 = writer;
            writer = null;
            temp2.close();
            Reader temp1 = reader;
            reader = null;
            temp1.close();
        } finally {
            closeReaderWithWarning(br);
            closeReaderWithWarning(reader);
            closeWriterWithWarning(bw);
            closeWriterWithWarning(writer);
        }
    }

    /**
     * Filter the lines from a reader and write them on the writer,
     * according to a closure which returns true or false.
     * Both Reader and Writer are closed after the operation.
     *
     * @param reader  a reader, closed after the call
     * @param writer  a writer, closed after the call
     * @param closure the closure which returns booleans
     * @throws IOException if an IOException occurs.
     */
    public static void filterLine(Reader reader, Writer writer, Closure closure) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        BufferedWriter bw = new BufferedWriter(writer);
        String line;
        try {
            while ((line = br.readLine()) != null) {
                if (DefaultTypeTransformation.castToBoolean(closure.call(line))) {
                    bw.write(line);
                    bw.newLine();
                }
            }
            bw.flush();

            Writer temp2 = writer;
            writer = null;
            temp2.close();
            Reader temp1 = reader;
            reader = null;
            temp1.close();
        } finally {
            closeReaderWithWarning(br);
            closeReaderWithWarning(reader);
            closeWriterWithWarning(bw);
            closeWriterWithWarning(writer);
        }

    }

    /**
     * Filters the lines of a File and creates a Writeable in return to
     * stream the filtered lines
     *
     * @param self    a File
     * @param closure a closure which returns a boolean indicating to filter
     *                the line or not
     * @return a Writable closure
     * @throws IOException if <code>self</code> is not readable
     */
    public static Writable filterLine(File self, Closure closure) throws IOException {
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
    public static void filterLine(File self, Writer writer, Closure closure) throws IOException {
        filterLine(newReader(self), writer, closure);
    }

    /**
     * Filter the lines of a Reader and create a Writable in return to stream
     * the filtered lines.
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
                    if (DefaultTypeTransformation.castToBoolean(closure.call(line))) {
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
                    throw new StringWriterIOException(e);
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
    public static Writable filterLine(InputStream self, Closure predicate) {
        return filterLine(newReader(self), predicate);
    }

    /**
     * Filters lines from an input stream, writing to a writer, using a closure which
     * returns boolean and takes a line.
     *
     * @param self      an InputStream
     * @param writer    a writer to write output to
     * @param predicate a closure which returns a boolean and takes a line as input
     * @throws IOException if an IOException occurs.
     */
    public static void filterLine(InputStream self, Writer writer, Closure predicate)
            throws IOException {
        filterLine(newReader(self), writer, predicate);
    }

    /**
     * Reads the content of the file into an array of byte
     *
     * @param file a File
     * @return a List of Bytes
     * @throws IOException if an IOException occurs.
     */
    public static byte[] readBytes(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fileInputStream);
        try {
            dis.readFully(bytes);

            InputStream temp = dis;
            dis = null;
            temp.close();
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    LOG.warning("Caught exception closing DataInputStream: " + e);
                }
            }
        }
        return bytes;
    }

    // ================================
    // Socket and ServerSocket methods

    /**
     * Allows an InputStream and an OutputStream from a Socket to be used,
     * calling the closure with the streams and then ensuring that the streams
     * are closed down again irrespective of whether exceptions occur.
     *
     * @param socket  a Socket
     * @param closure a Closure
     * @throws IOException if an IOException occurs.
     */
    public static void withStreams(Socket socket, Closure closure) throws IOException {
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        try {
            closure.call(new Object[]{input, output});

            InputStream temp1 = input;
            input = null;
            temp1.close();
            OutputStream temp2 = output;
            output = null;
            temp2.close();
        } finally {
            closeInputStreamWithWarning(input);
            closeOutputStreamWithWarning(output);
        }
    }

    /**
     * Allows an InputObjectStream and an OutputObjectStream from a Socket to be used,
     * calling the closure with the object streams and then ensuring that the streams
     * are closed down again irrespective of whether exceptions occur.
     *
     * @param socket  a Socket
     * @param closure a Closure
     * @throws IOException if an IOException occurs.
     * @since 1.1 beta 2
     */
    public static void withObjectStreams(Socket socket, Closure closure) throws IOException {
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(output);
        ObjectInputStream ois = new ObjectInputStream(input);
        try {
            closure.call(new Object[]{ois, oos});

            InputStream temp1 = ois;
            ois = null;
            temp1.close();
            temp1 = input;
            input = null;
            temp1.close();
            OutputStream temp2 = oos;
            oos = null;
            temp2.close();
            temp2 = output;
            output = null;
            temp2.close();
        } finally {
            closeInputStreamWithWarning(ois);
            closeInputStreamWithWarning(input);
            closeOutputStreamWithWarning(oos);
            closeOutputStreamWithWarning(output);
        }
    }

    // TODO reduce duplication by using Closable if we raise minimum requirement to Java 1.5
    private static void closeInputStreamWithWarning(InputStream input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                LOG.warning("Caught exception closing InputStream: " + e);
            }
        }
    }

    private static void closeOutputStreamWithWarning(OutputStream output) {
        if (output != null) {
            try {
                output.close();
            } catch (IOException e) {
                LOG.warning("Caught exception closing OutputStream: " + e);
            }
        }
    }

    private static void closeReaderWithWarning(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
                // ignore this exception since this
                // is only our internal problem
                LOG.warning("Caught exception closing Reader: " + e);
            }
        }
    }

    private static void closeWriterWithWarning(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                LOG.warning("Caught exception closing Writer: " + e);
            }
        }
    }

    /**
     * Overloads the left shift operator to provide an append mechanism to
     * add things to the output stream of a socket
     *
     * @param self  a Socket
     * @param value a value to append
     * @return a Writer
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
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
                        LOG.warning("Caught exception closing socket: " + e);
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

    public static Object asType(File f, Class c) {
        if (c == Writable.class) {
            return asWritable(f);
        }
        return asType((Object) f, c);
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

    public static Object asType(String self, Class c) {
        if (c == List.class) {
            return toList(self);
        } else if (c == BigDecimal.class) {
            return toBigDecimal(self);
        } else if (c == BigInteger.class) {
            return toBigInteger(self);
        } else if (c == Character.class) {
            return toCharacter(self);
        } else if (c == Double.class) {
            return toDouble(self);
        } else if (c == Float.class) {
            return toFloat(self);
        }
        return asType((Object) self, c);
    }

    // Process methods
    //-------------------------------------------------------------------------

    /**
     * An alias method so that a process appears similar to System.out, System.in, System.err;
     * you can use process.in, process.out, process.err in a similar way
     *
     * @param self a Process object
     * @return the InputStream for the process
     */
    public static InputStream getIn(Process self) {
        return self.getInputStream();
    }

    /**
     * Read the text of the output stream of the Process.
     *
     * @param self a Process
     * @return the text of the output
     * @throws IOException if an IOException occurs.
     */
    public static String getText(Process self) throws IOException {
        return getText(new BufferedReader(new InputStreamReader(self.getInputStream())));
    }

    /**
     * An alias method so that a process appears similar to System.out, System.in, System.err;
     * you can use process.in, process.out, process.err in a similar way
     *
     * @param self a Process object
     * @return the error InputStream for the process
     */
    public static InputStream getErr(Process self) {
        return self.getErrorStream();
    }

    /**
     * An alias method so that a process appears similar to System.out, System.in, System.err;
     * you can use process.in, process.out, process.err in a similar way
     *
     * @param self a Process object
     * @return the OutputStream for the process
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
     * @throws IOException if an IOException occurs.
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
     * @throws IOException if an IOException occurs.
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
     * gets the input and error streams from a process and reads them
     * to avoid the process to block due to a full ouput buffer. For this
     * two Threads are started, so this method will return immediately
     *
     * @param self a Process
     */
    public static void consumeProcessOutput(Process self) {
        Dumper d = new Dumper(self.getErrorStream());
        Thread t = new Thread(d);
        t.start();
        d = new Dumper(self.getInputStream());
        t = new Thread(d);
        t.start();
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
            if (groups.size() == 1 || closure.getMaximumNumberOfParameters() < groups.size()) {
                // not enough parameters there to give each group part
                // it's own parameter, so try a closure with one parameter
                // and give it all groups as a array
                closure.call((Object) groups.toArray());
            } else {
                closure.call(groups.toArray());
            }
        }
    }

    /**
     * Process each matched substring of the given group matcher. The object
     * passed to the closure is an array of strings, matched per a successful match.
     *
     * @param self    the source matcher
     * @param closure a closure
     */
    public static void each(Matcher self, Closure closure) {
        while (self.find()) {
            int count = self.groupCount();
            ArrayList groups = new ArrayList();
            for (int i = 0; i <= count; i++) {
                groups.add(self.group(i));
            }
            closure.call(groups.toArray());
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
            if (DefaultTypeTransformation.castToBoolean(closure.call(value))) {
                break;
            }
        }
        return i;
    }

    /**
     * Iterates through the class loader parents until it finds a loader with a class
     * named equal to org.codehaus.groovy.tools.RootLoader. If there is no such class
     * null will be returned. The name has to be used because a direct compare with
     * == may fail as the class may be loaded through different classloaders.
     *
     * @see org.codehaus.groovy.tools.RootLoader
     * @param self a ClassLoader
     * @return the rootLoader for the ClassLoader
     */
    public static ClassLoader getRootLoader(ClassLoader self) {
        while (true) {
            if (self == null) return null;
            if (self.getClass().getName().equals(RootLoader.class.getName())) return self;
            self = self.getParent();
        }
    }

    /**
     * Converts a given object to a type. This method is used through
     * the "as" operator and is overloadable as any other operator.
     *
     * @param obj  the object to convert
     * @param type the goal type
     * @return the resulting object
     */
    public static Object asType(Object obj, Class type) {
        return DefaultTypeTransformation.castToType(obj, type);
    }

    public static Object newInstance(Class c) {
        return InvokerHelper.getInstance().invokeConstructorOf(c,null);
    }

    public static Object newInstance(Class c, Object[] args) {
        if (args==null) args=new Object[]{null};
        return InvokerHelper.getInstance().invokeConstructorOf(c,args);
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
                // Ignore
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
                    // Ignore
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

    private static class Dumper implements Runnable {
        InputStream in;

        public Dumper(InputStream in) {
            this.in = in;
        }

        public void run() {
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            try {
                while (br.readLine() != null) {
                }
            } catch (IOException e) {
                throw new GroovyRuntimeException("exception while reading process stream", e);
            }
        }
    }

    public static Iterator iterator(Object o) {
        return DefaultTypeTransformation.asCollection(o).iterator();
    }

    /**
     * @param enumeration an Enumeration object
     * @return an Iterator for the Enumeration
     */
    public static Iterator iterator(final Enumeration enumeration) {
        return new Iterator() {
            private Object last;

            public boolean hasNext() {
                return enumeration.hasMoreElements();
            }

            public Object next() {
                last = enumeration.nextElement();
                return last;
            }

            public void remove() {
                throw new UnsupportedOperationException("Cannot remove() from an Enumeration");
            }
        };
    }

    // TODO move into DOMCategory once we can make use of optional categories transparent

    /**
     * @param nodeList a NodeList
     * @return an Iterator for a NodeList
     */
    public static Iterator iterator(final NodeList nodeList) {
        return new Iterator() {
            private int current /* = 0 */;

            public boolean hasNext() {
                return current < nodeList.getLength();
            }

            public Object next() {
                return nodeList.item(current++);
            }

            public void remove() {
                throw new UnsupportedOperationException("Cannot remove() from a NodeList iterator");
            }
        };
    }

    /**
     * @param matcher a Matcher object
     * @return an Iterator for a Matcher
     */
    public static Iterator iterator(final Matcher matcher) {
        return new Iterator() {
            private boolean found /* = false */;
            private boolean done /* = false */;

            public boolean hasNext() {
                if (done) {
                    return false;
                }
                if (!found) {
                    found = matcher.find();
                    if (!found) {
                        done = true;
                    }
                }
                return found;
            }

            public Object next() {
                if (!found) {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                }
                found = false;
                return matcher.group();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * @param self a Reader object
     * @return an Iterator for the Reader
     */
    public static Iterator iterator(Reader self) {
        final BufferedReader bufferedReader;
        if (self instanceof BufferedReader)
            bufferedReader = (BufferedReader) self;
        else
            bufferedReader = new BufferedReader(self);
        return new Iterator() {
            String nextVal /* = null */;
            boolean nextMustRead = true;
            boolean hasNext = true;

            public boolean hasNext() {
                if (nextMustRead && hasNext) {
                    try {
                        nextVal = readNext();
                        nextMustRead = false;
                    } catch (IOException e) {
                        hasNext = false;
                    }
                }
                return hasNext;
            }

            public Object next() {
                String retval = null;
                if (nextMustRead) {
                    try {
                        retval = readNext();
                    } catch (IOException e) {
                        hasNext = false;
                    }
                } else
                    retval = nextVal;
                nextMustRead = true;
                return retval;
            }

            private String readNext() throws IOException {
                String nv = bufferedReader.readLine();
                if (nv == null)
                    hasNext = false;
                return nv;
            }

            public void remove() {
                throw new UnsupportedOperationException("Cannot remove() from a Reader Iterator");
            }
        };
    }

    /**
     * Standard iterator for a input stream which iterates through the stream content in a byte-based fashion.
     *
     * @param self an InputStream object
     * @return an Iterator for the InputStream
     */
    public static Iterator iterator(InputStream self) {
        return iterator(new DataInputStream(self));
    }

    /**
     * Standard iterator for a data input stream which iterates through the stream content in a byte-based fashion.
     *
     * @param self a DataInputStream object
     * @return an Iterator for the DataInputStream
     */
    public static Iterator iterator(final DataInputStream self) {
        return new Iterator() {
            Byte nextVal;
            boolean nextMustRead = true;
            boolean hasNext = true;

            public boolean hasNext() {
                if (nextMustRead && hasNext) {
                    try {
                        byte bPrimitive = self.readByte();
                        nextVal = new Byte(bPrimitive);
                        nextMustRead = false;
                    } catch (IOException e) {
                        hasNext = false;
                    }
                }
                return hasNext;
            }

            public Object next() {
                Byte retval = null;
                if (nextMustRead) {
                    try {
                        byte b = self.readByte();
                        retval = new Byte(b);
                    } catch (IOException e) {
                        hasNext = false;
                    }
                } else
                    retval = nextVal;
                nextMustRead = true;
                return retval;
            }

            public void remove() {
                throw new UnsupportedOperationException("Cannot remove() from an InputStream Iterator");
            }
        };
    }

    /**
     * Standard iterator for a file which iterates through the file content in a line-based fashion.
     *
     * @param self a file object
     * @return a line-based iterator
     * @throws IOException if there is a problem processing the file (e.g. file is not found)
     */
    public static Iterator iterator(File self) throws IOException {
        return iterator(newReader(self));
    }

    /**
     * An identity function for iterators, supporting 'duck-typing' when trying to get an
     * iterator for each object within a collection, some of which may already be iterators.
     *
     * @param self an iterator object
     * @return itself
     */
    public static Iterator iterator(Iterator self) {
        return self;
    }
}
