/*
 * Copyright 2003-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime;

import groovy.io.PlatformLineWriter;
import groovy.lang.*;
import groovy.util.*;
import groovy.io.EncodingAwareBufferedWriter;
import groovy.sql.GroovyRowResult;
import groovy.text.RegexUtils;
import org.codehaus.groovy.runtime.metaclass.MissingPropertyExceptionNoStack;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.typehandling.GroovyCastException;
import org.codehaus.groovy.runtime.typehandling.NumberMath;
import org.codehaus.groovy.tools.RootLoader;
import org.w3c.dom.NodeList;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
 * @author Joachim Baumann
 * @version $Revision$
 */
public class DefaultGroovyMethods extends DefaultGroovyMethodsSupport {

    private static final Logger LOG = Logger.getLogger(DefaultGroovyMethods.class.getName());
    private static final Integer ONE = new Integer(1);

    /**
     * Identity check. Since == is overridden in Groovy with the meaning of equality
     * we need some fallback to check for object identity.  Invoke using the
     * 'is' operator, like so: <code>def same = (this is that)</code>
     *
     * @param self  an object
     * @param other an object to compare identity with
     * @return true if self and other are both references to the same
     *         instance, false otherwise
     */
    public static boolean is(Object self, Object other) {
        return self == other;
    }

    /**
     * Allows the closure to be called for the object reference self
     * synonym for 'with()'.
     *
     * @param self    the object to have a closure act upon
     * @param closure the closure to call on the object
     * @return result of calling the closure
     */
    public static Object identity(Object self, Closure closure) {
        return DefaultGroovyMethods.with(self, closure);
    }

    /**
     * Allows the closure to be called for the object reference self
     *
     * @param self    the object to have a closure act upon
     * @param closure the closure to call on the object
     * @return result of calling the closure
     */
    public static Object with(Object self, Closure closure) {
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
     * @param self     the object to act upon
     * @param property the property name of interest
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
         * i.e. it does not return privates. I don't know what dump() really should be doing,
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
     * and provides the data in form of simple key/value pairs, i.e.&nsbp;without
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
     *
     * @param self          any Object
     * @param categoryClass a category class to use
     * @param closure       the closure to invoke with the category in place
     * @return the value returned from the closure
     */
    public static Object use(Object self, Class categoryClass, Closure closure) {
        return GroovyCategorySupport.use(categoryClass, closure);
    }

    /**
     * Scoped use method with list of categories.
     *
     * @param self              any Object
     * @param categoryClassList a list of category classes
     * @param closure           the closure to invoke with the categories in place
     * @return the value returned from the closure
     */
    public static Object use(Object self, List categoryClassList, Closure closure) {
        return GroovyCategorySupport.use(categoryClassList, closure);
    }

    /**
     * Allows the usage of addShutdownHook without getting the runtime first.
     *
     * @param self    the object the method is called on (ignored)
     * @param closure the shutdown hook action
     */
    public static void addShutdownHook(Object self, Closure closure) {
        Runtime.getRuntime().addShutdownHook(new Thread(closure));
    }

    /**
     * Allows you to use a list of categories, specifying the list as varargs.
     * <code>use(CategoryClass1, CategoryClass2) { ... }</code>
     * This method saves having to wrap the the category
     * classes in a list.
     *
     * @param self  any Object
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
     * @param self  any Object
     * @param value the value to print
     */
    public static void print(Object self, Object value) {
        // we won't get here if we are a PrintWriter
        if (self instanceof Writer) {
            final PrintWriter pw = new PrintWriter((Writer) self);
            pw.print(InvokerHelper.toString(value));
            pw.flush();
        } else {
            System.out.print(InvokerHelper.toString(value));
        }
    }

    /**
     * Print a value to the standard output stream.
     * This method delegates to the owner to execute the method.
     *
     * @param self  a generated closure
     * @param value the value to print
     */
    public static void print(Closure self, Object value) {
        Object owner = getClosureOwner(self);
        InvokerHelper.invokeMethod(owner, "print", new Object[]{value});
    }

    /**
     * Print a linebreak to the standard output stream.
     *
     * @param self any Object
     */
    public static void println(Object self) {
        // we won't get here if we are a PrintWriter
        if (self instanceof Writer) {
            PrintWriter pw = new PrintWriter((Writer) self, true);
            pw.println();
        } else {
            System.out.println();
        }
    }

    /**
     * Print a linebreak to the standard output stream.
     * This method delegates to the owner to execute the method.
     *
     * @param self  a closure
     */
    public static void println(Closure self) {
        Object owner = getClosureOwner(self);
        InvokerHelper.invokeMethod(owner, "println", new Object[0]);
    }

    private static Object getClosureOwner(Closure cls) {
        Object owner =  cls.getOwner();
        while (owner instanceof GeneratedClosure) {
            owner = ((Closure) owner).getOwner();
        }
        return owner;
    }

    /**
     * Print a value (followed by a newline) to the standard output stream.
     *
     * @param self  any Object
     * @param value the value to print
     */
    public static void println(Object self, Object value) {
        // we won't get here if we are a PrintWriter
        if (self instanceof Writer) {
            final PrintWriter pw = new PrintWriter((Writer) self, true);
            pw.println(InvokerHelper.toString(value));
        } else {
            System.out.println(InvokerHelper.toString(value));
        }
    }

    /**
     * Print a value (followed by a newline) to the standard output stream.
     * This method delegates to the owner to execute the method.
     *
     * @param self  a closure
     * @param value the value to print
     */
    public static void println(Closure self, Object value) {
        Object owner = getClosureOwner(self);
        InvokerHelper.invokeMethod(owner, "println", new Object[]{value});
    }

    /**
     * Printf to a console (Only works with JDK1.5 or later).
     *
     * @param self   any Object
     * @param format a format string
     * @param values values referenced by the format specifiers in the format string.
     */
    public static void printf(Object self, String format, Object[] values) {
        if (self instanceof PrintStream)
            printf((PrintStream) self, format, values);
        else
            printf(System.out, format, values);
    }

    /**
     * Sprintf to a string (Only works with JDK1.5 or later).
     *
     * @param self   any Object
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
     * Printf to a PrintStream (Only works with JDK1.5 or later).
     *
     * @param out    a PrintStream object
     * @param format a format string
     * @param values values referenced by the format specifiers in the format string.
     */
    private static void printf(PrintStream out, String format, Object[] values) {
        char version = System.getProperty("java.version").charAt(2);
        if (version >= '5') {
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
                throw new RuntimeException("invoke threw an IllegalAccessException.  This is impossible.");
            } catch (java.lang.reflect.InvocationTargetException ite) {
                throw new InvokerInvocationException(ite);
            }
        } else {
            throw new RuntimeException("printf requires JDK1.5 or later.");
        }
    }

    /**
     * Prints a formatted string using the specified format string and
     * arguments (Only works with JDK1.5 or later).
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
     * @param self   any Object
     * @param format A format string
     * @param arg    Argument which is referenced by the format specifiers in the format
     *               string.  The type of <code>arg</code> should be one of Object[], List,
     *               int[], short[], byte[], char[], boolean[], long[], float[], or double[].
     */
    public static void printf(Object self, String format, Object arg) {
        if (self instanceof PrintStream)
            printf((PrintStream) self, format, arg);
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
     * <p/>
     * TODO: remove duplication with printf
     *
     * @param self   any Object
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
     * Inspects returns the String that matches what would be typed into a
     * terminal to create this object.
     *
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
     * @param out  the PrintWriter used for printing
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
     * @param out  the PrintWriter used for printing
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
     * @param object    any Object
     * @param method    the name of the method to call
     * @param arguments the arguments to use
     * @return the result of the method call
     */
    public static Object invokeMethod(Object object, String method, Object arguments) {
        return InvokerHelper.invokeMethod(object, method, arguments);
    }

    // isCase methods
    //-------------------------------------------------------------------------

    /**
     * Method for overloading the behavior of the 'case' method in switch statements.
     * The default implementation handles arrays types but otherwise simply delegates
     * to Object#equals, but this may be overridden for other types. In this example:
     * <pre> switch( a ) {
     *   case b: //some code
     * }</pre>
     * "some code" is called when <code>b.isCase( a )</code> returns
     * <code>true</code>.
     *
     * @param caseValue   the case value
     * @param switchValue the switch value
     * @return true if the switchValue is deemed to be equal to the caseValue
     */
    public static boolean isCase(Object caseValue, Object switchValue) {
        if (caseValue.getClass().isArray()) {
            return isCase(DefaultTypeTransformation.asCollection(caseValue), switchValue);
        }
        return caseValue.equals(switchValue);
    }

    /**
     * 'Case' implementation for a String, which uses String#equals(Object)
     * in order to allow Strings to be used in switch statements.
     * For example:
     * <pre>switch( str ) {
     *   case 'one' :
     *   // etc...
     * }</pre>
     * Note that this returns <code>true</code> for the case where both the
     * 'switch' and 'case' operand is <code>null</code>.
     *
     * @param caseValue   the case value
     * @param switchValue the switch value
     * @return true if the switchValue's toString() equals the caseValue
     */
    public static boolean isCase(String caseValue, Object switchValue) {
        if (switchValue == null) {
            return caseValue == null;
        }
        return caseValue.equals(switchValue.toString());
    }

    /**
     * Special 'Case' implementation for Class, which allows testing
     * for a certain class in a switch statement.
     * For example:
     * <pre>switch( obj ) {
     *   case List :
     *     // obj is a list
     *     break;
     *   case Set :
     *     // etc
     * }<pre>
     *
     * @param caseValue   the case value
     * @param switchValue the switch value
     * @return true if the switchValue is deemed to be assignable from the given class
     */
    public static boolean isCase(Class caseValue, Object switchValue) {
        if (switchValue instanceof Class) {
            Class val = (Class) switchValue;
            return caseValue.isAssignableFrom(val);
        }
        return caseValue.isInstance(switchValue);
    }

    /**
     * 'Case' implementation for collections which tests if the 'switch'
     * operand is contained in any of the 'case' values.
     * For example:
     * <pre>switch( item ) {
     *   case firstList :
     *     // item is contained in this list
     *     // etc
     * }</pre>
     *
     * @param caseValue   the case value
     * @param switchValue the switch value
     * @return true if the caseValue is deemed to contain the switchValue
     * @see java.util.Collection#contains(Object)
     */
    public static boolean isCase(Collection caseValue, Object switchValue) {
        return caseValue.contains(switchValue);
    }

    /**
     * 'Case' implementation for the {@link Pattern} class, which allows
     * testing a String against a number of regular expressions.
     * For example:
     * <pre>switch( str ) {
     *   case ~/one/ :
     *     // the regex 'one' matches the value of str
     * }
     * </pre>
     * Note that this returns true for the case where both the pattern and
     * the 'switch' values are <code>null</code>.
     *
     * @param caseValue   the case value
     * @param switchValue the switch value
     * @return true if the switchValue is deemed to match the caseValue
     */
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

    /**
     * Special 'case' implementation for all numbers, which delegates to the
     * <code>compareTo()</code> method for comparing numbers of different
     * types.
     *
     * @param caseValue   the case value
     * @param switchValue the switch value
     * @return true if the numbers are deemed equal
     */
    public static boolean isCase(Number caseValue, Number switchValue) {
        return NumberMath.compareTo(caseValue, switchValue) == 0;
    }

    /**
     * Returns an iterator equivalent to this iterator all duplicated items removed
     * by using the default comparator. The original iterator will become
     * exhausted of elements after determining the unique values. A new iterator
     * for the unique values will be returned.
     *
     * @param self an Iterator
     * @return the modified Iterator
     */
    public static Iterator unique(Iterator self) {
        return toList(unique(toList(self))).listIterator();
    }

    /**
     * Modifies this collection to remove all duplicated items, using the
     * default comparator.
     *
     * @param self a collection
     * @return the now modified collection
     */
    public static Collection unique(Collection self) {
        if (self instanceof Set)
            return self;
        List answer = new ArrayList();
        NumberAwareComparator numberAwareComparator = new NumberAwareComparator();
        for (Iterator it = self.iterator(); it.hasNext();) {
            Object o = it.next();
            boolean duplicated = false;
            for (Iterator it2 = answer.iterator(); it2.hasNext();) {
                Object o2 = it2.next();
                if (numberAwareComparator.compare(o, o2) == 0) {
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
     * Provides a method that compares two comparables using Groovy's
     * default number aware comparator.
     *
     * @param self a Comparable
     * @param other another Comparable
     * @return a -ve number, 0 or a +ve number according to Groovy's compareTo contract
     */
    public static int numberAwareCompareTo(Comparable self, Comparable other) {
        NumberAwareComparator numberAwareComparator = new NumberAwareComparator();
        return numberAwareComparator.compare(self, other);
    }

    /**
     * Returns an iterator equivalent to this iterator all duplicated items
     * removed by using a closure as a comparator.  If the closure takes a
     * single parameter, the argument passed will be each element, and the
     * closure should return a value used for comparison (either using
     * {@link Comparable#compareTo(Object)} or Object#equals() ).
     *
     * @param self an Iterator
     * @param closure a Closure used as a comparator
     * @return the modified Iterator
     */
    public static Iterator unique(Iterator self, Closure closure) {
        return toList(unique(toList(self), closure)).listIterator();
    }

    /**
     * A convenience method for making a collection unique using a closure
     * as a comparator.  If the closure takes a single parameter, the
     * argument passed will be each element, and the closure
     * should return a value used for comparison (either using
     * {@link Comparable#compareTo(Object)} or Object#equals() ).  If the
     * closure takes two parameters, two items from the collection
     * will be passed as arguments, and the closure should return an
     * int value (with 0 indicating the items are not unique).
     *
     * @param self    a Collection
     * @param closure a Closure used as a comparator
     * @return self   without any duplicates
     */
    public static Collection unique(Collection self, Closure closure) {
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
     * Returns an iterator equivalent to this iterator with all duplicated
     * items removed by using the supplied comparator.
     *
     * @param self an Iterator
     * @param comparator a Comparator
     * @return the modified Iterator
     */
    public static Iterator unique(Iterator self, Comparator comparator) {
        return toList(unique(toList(self), comparator)).listIterator();
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
     * @param comparator a Comparator
     * @return self       the now modified collection without duplicates
     */
    public static Collection unique(Collection self, Comparator comparator) {
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
     * Iterates through an aggregate type or data structure,
     * passing each item to the given closure.  Custom types may utilize this
     * method by simply providing an "iterator()" method.  The items returned
     * from the resulting iterator will be passed to the closure.
     *
     * @param self    the object over which we iterate
     * @param closure the closure applied on each element found
     * @return the self Object
     */
    public static Object each(Object self, Closure closure) {
        each(InvokerHelper.asIterator(self), closure);
        return self;
    }

    /**
     * Iterates through an aggregate type or data structure,
     * passing each item and the item's index (a counter starting at
     * zero) to the given closure.
     *
     * @param self    an Object
     * @param closure a Closure to operate on each item
     * @return the self Object
     */
    public static Object eachWithIndex(Object self, Closure closure) {
        int counter = 0;
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            closure.call(new Object[]{iter.next(), new Integer(counter++)});
        }
        return self;
    }

    private static Iterator each(Iterator iter, Closure closure) {
        while (iter.hasNext()) {
            closure.call(iter.next());
        }
        return iter;
    }

    /**
     * Allows a Map to be iterated through using a closure. If the
     * closure takes one parameter then it will be passed the Map.Entry
     * otherwise if the closure takes two parameters then it will be
     * passed the key and the value.
     *
     * @param self    the map over which we iterate
     * @param closure the closure applied on each entry of the map
     * @return returns the self parameter
     */
    public static Map each(Map self, Closure closure) {
        for (Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            callClosureForMapEntry(closure, entry);
        }
        return self;
    }

    /**
     * Allows a Map to be iterated through using a closure. If the
     * closure takes two parameters then it will be passed the Map.Entry and
     * the item's index (a counter starting at zero) otherwise if the closure
     * takes three parameters then it will be passed the key, the value, and
     * the index.
     *
     * @param self    the map over which we iterate
     * @param closure a Closure to operate on each item
     * @return the self Object
     */
    public static Object eachWithIndex(Map self, Closure closure) {
        int counter = 0;
        for (Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            callClosureForMapEntryAndCounter(closure, entry, counter++);
        }
        return self;
    }

    /**
     * Iterate over each element of the list in the reverse order.
     *
     * @param self    a List
     * @param closure a closure to which each item is passed.
     * @return the original list
     */
    public static List reverseEach(List self, Closure closure) {
        each(new ReverseListIterator(self), closure);
        return self;
    }

    /**
     * Iterate over each element of the array in the reverse order.
     *
     * @param self    an Object array
     * @param closure a closure to which each item is passed
     * @return the original array
     */
    public static Object[] reverseEach(Object[] self, Closure closure) {
        each(new ReverseListIterator(Arrays.asList(self)), closure);
        return self;
    }

    /**
     * Reverse the items in an Object array.
     *
     * @param self    an Object array
     * @return an array containing the reversed items
     */
    public static Object[] reverse(Object[] self) {
        return toList(new ReverseListIterator(Arrays.asList(self))).toArray();
    }

    /**
     * Used to determine if the given predicate closure is valid (i.e.&nsbp;returns
     * <code>true</code> for all items in this data structure).
     * A simple example for a list:
     * <pre>def list = [3,4,5]
     * def greaterThanTwo = list.every { it > 2 }
     * </pre>
     *
     * @param self    the object over which we iterate
     * @param closure the closure predicate used for matching
     * @return true if every iteration of the object matches the closure predicate
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
     * Iterates over the entries of a map, and checks whether a predicate is
     * valid for all entries.
     *
     * @param self    the map over which we iterate
     * @param closure the closure predicate used for matching
     * @return true if every entry of the map matches the closure predicate
     */
    public static boolean every(Map self, Closure closure) {
        for (Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (!DefaultTypeTransformation.castToBoolean(callClosureForMapEntry(closure, entry))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Iterates over every element of a collection, and checks whether all
     * elements are <code>true</code> according to the Groovy Truth.
     * Equivalent to <code>self.every({element -> element})</code>
     *
     * @param self the object over which we iterate
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
     * Iterates over the contents of an object or collection, and checks whether a
     * predicate is valid for at least one element.
     *
     * @param self    the object over which we iterate
     * @param closure the closure predicate used for matching
     * @return true   if any iteration for the object matches the closure predicate
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
     * Iterates over the entries of a map, and checks whether a predicate is
     * valid for at least one entry
     *
     * @param self    the map over which we iterate
     * @param closure the closure predicate used for matching
     * @return true if any entry in the map matches the closure predicate
     */
    public static boolean any(Map self, Closure closure) {
        for (Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (DefaultTypeTransformation.castToBoolean(callClosureForMapEntry(closure, entry))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Iterates over the elements of a collection, and checks whether at least
     * one element is true according to the Groovy Truth.
     * Equivalent to self.any({element -> element})
     *
     * @param self the object over which we iterate
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
     * Iterates over every element of the collection and returns each item that matches
     * the given filter - calling the <code>{@link #isCase(Object,Object)}</code>
     * method used by switch statements.  This method can be used with different
     * kinds of filters like regular expressions, classes, ranges etc.
     * Example:
     * <pre>def list = ['a', 'b', 'aa', 'bc' ]
     * def filtered = list.grep( ~/a+/ ) //contains 'a' and 'aa'
     * </pre>
     *
     * @param self   the object over which we iterate
     * @param filter the filter to perform on the collection (using the isCase(object) method)
     * @return a collection of objects which match the filter
     */
    public static Collection grep(Object self, Object filter) {
        Collection answer = createSimilarOrDefaultCollection(self);
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
     * Counts the number of occurrences of the given value from the
     * items within this Iterator.
     * Comparison is done using Groovy's == operator (using
     * <code>compareTo(value) == 0</code> or <code>equals(value)</code> ).
     * The iterator will become exhausted of elements after determining the count value.
     *
     * @param self  the Iterator from which we count the number of matching occurrences
     * @param value the value being searched for
     * @return the number of occurrences
     */
    public static int count(Iterator self, Object value) {
        return count(toList(self), value);
    }

    /**
     * Counts the number of occurrences of the given value inside this collection.
     * Comparison is done using Groovy's == operator (using
     * <code>compareTo(value) == 0</code> or <code>equals(value)</code> ).
     *
     * @param self  the collection within which we count the number of occurrences
     * @param value the value being searched for
     * @return the number of occurrences
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
     * Convert an iterator to a List. The iterator will become
     * exhausted of elements after making this conversion.
     *
     * @param self an iterator
     * @return a List
     */
    public static List toList(Iterator self) {
        List answer = new ArrayList();
        while (self.hasNext()) {
            answer.add(self.next());
        }
        return answer;
    }

    /**
     * Convert an enumeration to a List.
     *
     * @param self an enumeration
     * @return a List
     */
    public static List toList(Enumeration self) {
        List answer = new ArrayList();
        while (self.hasMoreElements()) {
            answer.add(self.nextElement());
        }
        return answer;
    }

    /**
     * Iterates through this object transforming each value into a new value using the
     * closure as a transformer, returning a list of transformed values.
     * Example:
     * <pre>def list = [1, 'a', 1.23, true ]
     * def types = list.collect { it.class }
     * </pre>
     *
     * @param self    the values of the object to transform
     * @param closure the closure used to transform each element of the collection
     * @return a List of the transformed values
     */
    public static List collect(Object self, Closure closure) {
        return (List) collect(self, new ArrayList(), closure);
    }

    /**
     * Iterates through this object transforming each object into a new value using the closure
     * as a transformer and adding it to the collection, returning the resulting collection.
     *
     * @param self       the values of the object to transform
     * @param collection the Collection to which the transformed values are added
     * @param closure    the closure used to map each element of the collection
     * @return the given collection after the transformed values are added
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
     * @return a List of the transformed values
     */
    public static List collect(Collection self, Closure closure) {
        return (List) collect(self, new ArrayList(self.size()), closure);
    }

    /**
     * Iterates through this collection transforming each value into a new value using the closure
     * as a transformer, returning an initial collection plus the transformed values.
     *
     * @param self       a collection
     * @param collection an initial Collection to which the transformed values are added
     * @param closure    the closure used to transform each element of the collection
     * @return the resulting collection of transformed values
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
     * Recursively iterates through this collection transforming each non-Collection value
     * into a new value using the closure as a transformer. Returns a potentially nested
     * list of transformed values.
     *
     * @param self       a collection
     * @param closure    the closure used to transform each element of the collection
     * @return the resultant collection
     */
    public static List collectAll(Collection self, Closure closure) {
        return (List) collectAll(self, new ArrayList(self.size()), closure);
    }

    /**
     * Recursively iterates through this collection transforming each non-Collection value
     * into a new value using the closure as a transformer. Returns a potentially nested
     * collection of transformed values.
     *
     * @param self       a collection
     * @param collection an initial Collection to which the transformed values are added
     * @param closure    the closure used to transform each element of the collection
     * @return the resultant collection
     */
    public static Collection collectAll(Collection self, Collection collection, Closure closure) {
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            final Object o = iter.next();
            if (o instanceof Collection) {
                Collection c = (Collection) o;
                collection.add(collectAll(c, createSimilarCollection(collection, c.size()), closure));
            } else {
                collection.add(closure.call(o));
            }
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
     * @param closure    the closure used for mapping, which can take one (Map.Entry) or two (key, value) parameters
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
     * Finds the first value matching the closure condition.  Example:
     * <pre>def list = [1,2,3]
     * list.find { it > 1 } // returns 2
     * </pre>
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
     * Finds the first entry matching the closure condition.  If the closure takes
     * two parameters, the entry key and value are passed.  If the closure takes
     * one parameter, the Map.Entry object is passed.
     *
     * @param self    a Map
     * @param closure a closure condition
     * @return the first Object found
     */
    public static Object find(Map self, Closure closure) {
        for (Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (DefaultTypeTransformation.castToBoolean(callClosureForMapEntry(closure, entry))) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Finds all items matching the closure condition.
     *
     * @param self    an Object with an Iterator returning its values
     * @param closure a closure condition
     * @return a List of the values found
     */
    public static Collection findAll(Object self, Closure closure) {
        List answer = new ArrayList();
        Iterator iter = InvokerHelper.asIterator(self);
        return findAll(closure, answer, iter);
    }

    /**
     * Finds all values matching the closure condition.
     *
     * @param self    a Collection
     * @param closure a closure condition
     * @return a Collection of matching values
     */
    public static Collection findAll(Collection self, Closure closure) {
        Collection answer = createSimilarCollection(self);
        Iterator iter = self.iterator();
        return findAll(closure, answer, iter);
    }

    private static Collection findAll(Closure closure, Collection answer, Iterator iter) {
        while (iter.hasNext()) {
            Object value = iter.next();
            if (DefaultTypeTransformation.castToBoolean(closure.call(value))) {
                answer.add(value);
            }
        }
        return answer;
    }

    /**
     * Splits all items into two lists based on the closure condition.
     * The first list contains all items matching the closure expression.
     * The second list all those that don't.
     *
     * @param self    an Object with an Iterator returning its values
     * @param closure a closure condition
     * @return a List containing whose first item is the accepted values and whose second item is the rejected values
     */
    public static Collection split(Object self, Closure closure) {
        List accept = new ArrayList();
        List reject = new ArrayList();
        Iterator iter = InvokerHelper.asIterator(self);
        return split(closure, accept, reject, iter);
    }

    /**
     * Splits all items into two collections based on the closure condition.
     * The first list contains all items which match the closure expression.
     * The second list all those that don't.
     *
     * @param self    a Collection of values
     * @param closure a closure condition
     * @return a List containing whose first item is the accepted values and whose second item is the rejected values
     */
    public static Collection split(Collection self, Closure closure) {
        Collection accept = createSimilarCollection(self);
        Collection reject = createSimilarCollection(self);
        Iterator iter = self.iterator();
        return split(closure, accept, reject, iter);
    }

    private static Collection split(Closure closure, Collection accept, Collection reject, Iterator iter) {
        List answer = new ArrayList();
        while (iter.hasNext()) {
            Object value = iter.next();
            if (DefaultTypeTransformation.castToBoolean(closure.call(value))) {
                accept.add(value);
            } else {
                reject.add(value);
            }
        }
        answer.add(accept);
        answer.add(reject);
        return answer;
    }

    /**
     * Adds GroovyCollections#combinations(Collection) as a method on collections.
     *
     * @param self a Collection of lists
     * @return a List of the combinations found
     * @see groovy.util.GroovyCollections#combinations(java.util.Collection)
     */
    public static List combinations(Collection self) {
        return GroovyCollections.combinations(self);
    }

    /**
     * Adds GroovyCollections#transpose(List) as a method on lists.
     *
     * @param self a List of lists
     * @return a List of the transposed lists
     * @see groovy.util.GroovyCollections#transpose(java.util.List)
     */
    public static List transpose(List self) {
        return GroovyCollections.transpose(self);
    }

    /**
     * Finds all entries matching the closure condition. If the
     * closure takes one parameter then it will be passed the Map.Entry.
     * Otherwise if the closure should take two parameters, which will be
     * the key and the value.
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
     * Sorts all collection members into groups determined by the
     * supplied mapping closure.  The closure should return the key that this
     * item should be grouped by.  The returned Map will have an entry for each
     * distinct key returned from the closure, with each value being a list of
     * items for that group.
     *
     * @param self    a collection to group (no map)
     * @param closure a closure mapping entries on keys
     * @return a new Map grouped by keys
     */
    public static Map groupBy(Collection self, Closure closure) {
        Map answer = new HashMap();
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            Object element = iter.next();
            Object value = closure.call(element);
            groupAnswer(answer, element, value);
        }
        return answer;
    }

    /**
     * Groups all map entries into groups determined by the
     * supplied mapping closure. The closure will be passed a Map.Entry or
     * key and value (depending on the number of parameters the closure accepts)
     * and should return the key that each item should be grouped under.  The
     * resulting map will have an entry for each 'group' key returned by the
     * closure, with values being the list of map entries that belong to each
     * group.
     *
     * @param self    a map to group
     * @param closure a closure mapping entries on keys
     * @return a new Map grouped by keys
     */
    public static Map groupEntriesBy(Map self, Closure closure) {
        final Map answer = new HashMap();
        for (final Iterator iter = self.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object value = callClosureForMapEntry(closure, entry);
            groupAnswer(answer, entry, value);
        }
        return answer;
    }

    /**
     * Groups the members of a map into sub maps determined by the
     * supplied mapping closure. The closure will be passed a Map.Entry or
     * key and value (depending on the number of parameters the closure accepts)
     * and should return the key that each item should be grouped under.  The
     * resulting map will have an entry for each 'group' key returned by the
     * closure, with values being the map members from the original map that
     * belong to each group.
     *
     * @param self    a map to group
     * @param closure a closure mapping entries on keys
     * @return a new Map grouped by keys
     */
    public static Map groupBy(Map self, Closure closure) {
        final Map initial = groupEntriesBy(self, closure);
        final Map answer = new HashMap();
        Iterator iterator = initial.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry outer = (Map.Entry) iterator.next();
            Object key = outer.getKey();
            List entries = (List) outer.getValue();
            Map target = new HashMap();
            for (int i = 0; i < entries.size(); i++) {
                Map.Entry inner = (Map.Entry) entries.get(i);
                target.put(inner.getKey(), inner.getValue());
            }
            answer.put(key, target);
        }
        return answer;
    }

    /**
     * Groups the current element according to the value
     *
     * @param answer  the map containing the results
     * @param element the element to be placed
     * @param value   the value according to which the element will be placed
     */
    protected static void groupAnswer(final Map answer, Object element, Object value) {
        if (answer.containsKey(value)) {
            ((List) answer.get(value)).add(element);
        } else {
            List groupedElements = new ArrayList();
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

    // internal helper method
    protected static Object callClosureForLine(Closure closure, String line, int counter) {
        if (closure.getMaximumNumberOfParameters() == 2) {
            return closure.call(new Object[]{line, new Integer(counter)});
        }
        return closure.call(line);
    }

    protected static Object callClosureForMapEntryAndCounter(Closure closure, Map.Entry entry, int counter) {
        if (closure.getMaximumNumberOfParameters() == 3) {
            return closure.call(new Object[]{entry.getKey(), entry.getValue(), new Integer(counter)});
        }
        if (closure.getMaximumNumberOfParameters() == 2) {
            return closure.call(new Object[]{entry, new Integer(counter)});
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
        return inject(self.iterator(), value, closure);
    }

    /**
     * Iterates through the given iterator, passing in the initial value to
     * the closure along with the current iterated item then passing into the
     * next iteration the value of the previous closure.
     *
     * @param self    a Collection
     * @param value   a value
     * @param closure a closure
     * @return the last value of the last iteration
     */
    public static Object inject(Iterator self, Object value, Closure closure) {
        Object[] params = new Object[2];
        while (self.hasNext()) {
            Object item = self.next();
            params[0] = value;
            params[1] = item;
            value = closure.call(params);
        }
        return value;
    }

    /**
     * Iterates through the given object, passing in the initial value to
     * the closure along with the current iterated item then passing into the
     * next iteration the value of the previous closure.
     *
     * @param self    a Collection
     * @param value   a value
     * @param closure a closure
     * @return the last value of the last iteration
     */
    public static Object inject(Object self, Object value, Closure closure) {
        Iterator iter = InvokerHelper.asIterator(self);
        return inject(iter, value, closure);
    }

    /**
     * Iterates through the given array of objects, passing in the initial value to
     * the closure along with the current iterated item then passing into the
     * next iteration the value of the previous closure.
     *
     * @param self         an Object[]
     * @param initialValue an initialValue
     * @param closure      a closure
     * @return the last value of the last iteration
     */
    public static Object inject(Object[] self, Object initialValue, Closure closure) {
        Object[] params = new Object[2];
        Object value = initialValue;
        for (int i = 0; i < self.length; i++) {
            params[0] = value;
            params[1] = self[i];
            value = closure.call(params);
        }
        return value;
    }

    /**
     * Sums the items in a collection.  This is equivalent to invoking the
     * "plus" method on all items in the collection.
     *
     * @param self Collection of values to add together
     * @return The sum of all of the items
     */
    public static Object sum(Collection self) {
        return sum(self, null, true);
    }

    /**
     * Sums the items from an Iterator.  This is equivalent to invoking the
     * "plus" method on all items from the Iterator. The iterator will become
     * exhausted of elements after determining the sum value.
     *
     * @param self an Iterator for the values to add together
     * @return The sum of all of the items
     */
    public static Object sum(Iterator self) {
        return sum(toList(self), null, true);
    }

    /**
     * Sums the items in a collection, adding the result to some initial value.
     *
     * @param self         a collection of values to sum
     * @param initialValue the items in the collection will be summed to this initial value
     * @return The sum of all of the collection items.
     */
    public static Object sum(Collection self, Object initialValue) {
        return sum(self, initialValue, false);
    }

    /**
     * Sums the items from an Iterator.  This is equivalent to invoking the
     * "plus" method on all items from the Iterator.
     *
     * @param self         an Iterator for the values to add together
     * @param initialValue the items in the collection will be summed to this initial value
     * @return The sum of all of the items
     */
    public static Object sum(Iterator self, Object initialValue) {
        return sum(toList(self), initialValue, false);
    }

    private static Object sum(Collection self, Object initialValue, boolean first) {
        Object result = initialValue;
        Object[] param = new Object[1];
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            param[0] = iter.next();
            if (first) {
                result = param[0];
                first = false;
                continue;
            }
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
        return sum(self, null, closure, true);
    }

    /**
     * Sums the result of apply a closure to each item of a collection to sum intial value.
     * <code>coll.sum(closure)</code> is equivalent to:
     * <code>coll.collect(closure).sum()</code>.
     *
     * @param self         a Collection
     * @param closure      a single parameter closure that returns a numeric value.
     * @param initialValue the closure results will be summed to this initial value
     * @return The sum of the values returned by applying the closure to each
     *         item of the list.
     */
    public static Object sum(Collection self, Object initialValue, Closure closure) {
        return sum(self, initialValue, closure, false);
    }

    private static Object sum(Collection self, Object initialValue, Closure closure, boolean first) {
        Object result = initialValue;
        Object[] closureParam = new Object[1];
        Object[] plusParam = new Object[1];
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            closureParam[0] = iter.next();
            plusParam[0] = closure.call(closureParam);
            if (first) {
                result = plusParam[0];
                first = false;
                continue;
            }
            MetaClass metaClass = InvokerHelper.getMetaClass(result);
            result = metaClass.invokeMethod(result, "plus", plusParam);
        }
        return result;
    }

    /**
     * Concatenates the <code>toString()</code> representation of each
     * item from the iterator, with the given String as a separator between
     * each item. The iterator will become exhausted of elements after
     * determining the resulting conjoined value.
     *
     * @param self      an Iterator of items
     * @param separator a String separator
     * @return the joined String
     */
    public static String join(Iterator self, String separator) {
        return join(toList(self), separator);
    }

    /**
     * Concatenates the <code>toString()</code> representation of each
     * item in this collection, with the given String as a separator between
     * each item.
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
     * Concatenates the <code>toString()</code> representation of each
     * items in this array, with the given String as a separator between each
     * item.
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
     * Adds min() method to Collection objects.
     *
     * @param self a Collection
     * @return the minimum value
     * @see groovy.util.GroovyCollections#min(java.util.Collection)
     */
    public static Object min(Collection self) {
        return GroovyCollections.min(self);
    }

    /**
     * Adds min() method to Iterator objects. The iterator will become
     * exhausted of elements after determining the minimum value.
     *
     * @param self an Iterator
     * @return the minimum value
     * @see #min(java.util.Collection)
     */
    public static Object min(Iterator self) {
        return min(toList(self));
    }

    /**
     * Adds min() method to Object arrays.
     *
     * @param self an Object array
     * @return the minimum value
     * @see #min(java.util.Collection)
     */
    public static Object min(Object[] self) {
        return min(toList(self));
    }

    /**
     * Selects the minimum value found in the collection using the given comparator.
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
     * Selects the minimum value found from the Iterator using the given comparator.
     *
     * @param self       an Iterator
     * @param comparator a Comparator
     * @return the minimum value
     * @see #min(java.util.Collection, java.util.Comparator)
     */
    public static Object min(Iterator self, Comparator comparator) {
        return min(toList(self), comparator);
    }

    /**
     * Selects the minimum value found from the Object array using the given comparator.
     *
     * @param self       an Object array
     * @param comparator a Comparator
     * @return the minimum value
     * @see #min(java.util.Collection, java.util.Comparator)
     */
    public static Object min(Object[] self, Comparator comparator) {
        return min(toList(self), comparator);
    }

    /**
     * Selects the minimum value found in the collection using the given closure
     * as a comparator.  The closure should return a comparable value (i.e. a
     * number) for each item passed.  The collection item for which the closure
     * returns the smallest comparable value will be returned from this method
     * as the minimum.
     *
     * @param self    a Collection
     * @param closure a closure used as a comparator
     * @return the minimum value
     */
    public static Object min(Collection self, Closure closure) {
        int params = closure.getMaximumNumberOfParameters();
        if (params != 1) {
            return min(self, new ClosureComparator(closure));
        }
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
    }

    /**
     * Selects the minimum value found from the Iterator using the given closure
     * as a comparator.  The closure should return a comparable value (i.e. a
     * number) for each item passed. The iterator will become
     * exhausted of elements after this operation.
     *
     * @param self    an Iterator
     * @param closure a closure used as a comparator
     * @return the minimum value
     * @see #min(java.util.Collection, groovy.lang.Closure)
     */
    public static Object min(Iterator self, Closure closure) {
        return min(toList(self), closure);
    }

    /**
     * Selects the minimum value found from the Object array using the given closure
     * as a comparator.  The closure should return a comparable value (i.e. a
     * number) for each item passed.
     *
     * @param self    an Object array
     * @param closure a closure used as a comparator
     * @return the minimum value
     * @see #min(java.util.Collection, groovy.lang.Closure)
     */
    public static Object min(Object[] self, Closure closure) {
        return min(toList(self), closure);
    }

    /**
     * Adds max() method to Collection objects.
     *
     * @param self a Collection
     * @return the maximum value
     * @see groovy.util.GroovyCollections#max(java.util.Collection)
     */
    public static Object max(Collection self) {
        return GroovyCollections.max(self);
    }

    /**
     * Adds max() method to Iterator objects. The iterator will become
     * exhausted of elements after determining the maximum value.
     *
     * @param self an Iterator
     * @return the maximum value
     * @see groovy.util.GroovyCollections#max(java.util.Collection)
     */
    public static Object max(Iterator self) {
        return max(toList(self));
    }

    /**
     * Adds max() method to Object arrays.
     *
     * @param self an Object array
     * @return the maximum value
     * @see #max(java.util.Collection)
     */
    public static Object max(Object[] self) {
        return max(toList(self));
    }

    /**
     * Selects the maximum value found in the collection using the given closure
     * as a comparator.  The closure should return a comparable value (i.e. a
     * number) for each item passed.  The collection item for which the closure
     * returns the largest comparable value will be returned from this method
     * as the maximum.
     *
     * @param self    a Collection
     * @param closure a closure used as a comparator
     * @return the maximum value
     */
    public static Object max(Collection self, Closure closure) {
        int params = closure.getMaximumNumberOfParameters();
        if (params != 1) {
            return max(self, new ClosureComparator(closure));
        }
        Object answer = null;
        Object AnswerValue = null;
        for (Iterator iter = self.iterator(); iter.hasNext();) {
            Object item = iter.next();
            Object value = closure.call(item);
            if (answer == null || ScriptBytecodeAdapter.compareLessThan(AnswerValue, value)) {
                answer = item;
                AnswerValue = value;
            }
        }
        return answer;
    }

    /**
     * Selects the maximum value found from the Iterator using the given closure
     * as a comparator.  The closure should return a comparable value (i.e. a
     * number) for each item passed. The iterator will become
     * exhausted of elements after this operation.
     *
     * @param self    an Iterator
     * @param closure a closure used as a comparator
     * @return the maximum value
     * @see #max(java.util.Collection, groovy.lang.Closure)
     */
    public static Object max(Iterator self, Closure closure) {
        return max(toList(self), closure);
    }

    /**
     * Selects the maximum value found from the Object array using the given closure
     * as a comparator.  The closure should return a comparable value (i.e. a
     * number) for each item passed.
     *
     * @param self    an Object array
     * @param closure a closure used as a comparator
     * @return the maximum value
     * @see #max(java.util.Collection, groovy.lang.Closure)
     */
    public static Object max(Object[] self, Closure closure) {
        return max(toList(self), closure);
    }

    /**
     * Selects the maximum value found in the collection using the given comparator.
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
     * Selects the maximum value found from the Iterator using the given comparator.
     *
     * @param self       an Iterator
     * @param comparator a Comparator
     * @return the maximum value
     */
    public static Object max(Iterator self, Comparator comparator) {
        return max(toList(self), comparator);
    }

    /**
     * Selects the maximum value found from the Object array using the given comparator.
     *
     * @param self       an Object array
     * @param comparator a Comparator
     * @return the maximum value
     */
    public static Object max(Object[] self, Comparator comparator) {
        return max(toList(self), comparator);
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>Iterator</code>.
     * The iterator will become exhausted of elements after determining the size value.
     *
     * @param self an Iterator
     * @return the length of the Iterator
     */
    public static int size(Iterator self) {
        int count = 0;
        while (self.hasNext()) {
            self.next();
            count++;
        }
        return count;
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>String</code>.
     *
     * @param text a String
     * @return the length of the String
     */
    public static int size(String text) {
        return text.length();
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>StringBuffer</code>.
     *
     * @param buffer a StringBuffer
     * @return the length of the StringBuffer
     */
    public static int size(StringBuffer buffer) {
        return buffer.length();
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>File</code>.
     *
     * @param self a file object
     * @return the file's size (length)
     */
    public static long size(File self) {
        return self.length();
    }


    /**
     * Provide the standard Groovy <code>size()</code> method for <code>Matcher</code>.
     *
     * @param self a matcher object
     * @return the matcher's size (count)
     */
    public static long size(Matcher self) {
        return getCount(self);
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for an array.
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
     * @param text  a String
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

        boolean reverse = range.isReverse();
        // If this is a backwards range, reverse the arguments to substring.
        if (from > to) {
            int tmp = from;
            from = to;
            to = tmp;
            reverse = !reverse;
        }

        CharSequence sequence = text.subSequence(from, to + 1);
        return reverse ? reverse((String) sequence) : sequence;
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
     * Support the range subscript operator for CharSequence or StringBuffer with EmptyRange
     *
     * @param text  a CharSequence
     * @param range an EmptyRange
     * @return the subsequence CharSequence
     */
    public static CharSequence getAt(CharSequence text, EmptyRange range) {
        return "";
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
     * Support the range subscript operator for String with EmptyRange
     *
     * @param text  a String
     * @param range an EmptyRange
     * @return the resulting String
     */
    public static String getAt(String text, EmptyRange range) {
        return "";
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
    public static Pattern bitwiseNegate(String self) {
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
     * <p>Note that unlike String.replaceAll(String pattern, String replacement), where the replacement string
     * treats '$' and '\' specially (for group substitution), the result of the closure is converted to a string
     * and that value is used literally for the replacement.</p>
     *
     * @param self    a String
     * @param regex   the capturing regex
     * @param closure the closure to apply on each captured group
     * @return a String with replaced content
     * @since 1.0
     * @see java.util.regex.Matcher.quoteReplacement(String)
     */
    public static String replaceAll(final String self, final String regex, final Closure closure) {
        final Matcher matcher = Pattern.compile(regex).matcher(self);
        if (matcher.find()) {
            final StringBuffer sb = new StringBuffer(self.length() + 16);
            do {
                int count = matcher.groupCount();
                List groups = new ArrayList();
                for (int i = 0; i <= count; i++) {
                    groups.add(matcher.group(i));
                }
                final String replacement = InvokerHelper.toString(closure.call(groups.toArray()));
                matcher.appendReplacement(sb, RegexUtils.quoteReplacement(replacement));
            } while (matcher.find());
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
     * @param self          a String object
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
     * @param self          a String object
     * @param numberOfChars the total number of characters
     * @return the String padded to the left
     */

    public static String padLeft(String self, Number numberOfChars) {
        return padLeft(self, numberOfChars, " ");
    }

    /**
     * Pad a String with the characters appended to the right
     *
     * @param self          a String object
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
     * @param self          a String object
     * @param numberOfChars the total number of characters
     * @return the String padded to the right
     */

    public static String padRight(String self, Number numberOfChars) {
        return padRight(self, numberOfChars, " ");
    }

    /**
     * Center a String and pad it with the characters appended around it
     *
     * @param self          a String object
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
     * Center a String and pad it with spaces appended around it
     *
     * @param self          a String object
     * @param numberOfChars the total number of characters
     * @return the String centered with padded character around
     */
    public static String center(String self, Number numberOfChars) {
        return center(self, numberOfChars, " ");
    }

/**
     * Support the subscript operator, e.g.&nbsp;matcher[index], for a regex Matcher.
     * <p/>
     * For an example using no group match, <code><pre>
     *    def p = /ab[d|f]/
     *    def m = "abcabdabeabf" =~ p
     *    for (i in 0..&lt;m.count) {
     *        println( "m.groupCount() = " + m.groupCount())
     *        println( "  " + i + ": " + m[i] )   // m[i] is a String
     *    }
     * </pre></code>
     * <p/>
     * For an example using group matches, <code><pre>
     *    def p = /(?:ab([c|d|e|f]))/
     *    def m = "abcabdabeabf" =~ p
     *    for (i in 0..&lt;m.count) {
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
                List list = new ArrayList(matcher.groupCount());
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
     * @param range a Range indicating the items to get
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

    /**
     * Select a List of items from a List using a Collection to
     * identify the indices to be selected.
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
     * Select a List of items from an Object array using a Collection to
     * identify the indices to be selected.
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
     * Select a List of characters from a CharSequence using a Collection
     * to identify the indices to be selected.
     *
     * @param self    a CharSequence
     * @param indices a Collection of indices
     * @return a CharSequence consisting of the characters at the given indices
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
     * Select a List of characters from a String using a Collection
     * to identify the indices to be selected.
     *
     * @param self    a String
     * @param indices a Collection of indices
     * @return a String consisting of the characters at the given indices
     */
    public static String getAt(String self, Collection indices) {
        return (String) getAt((CharSequence) self, indices);
    }

    /**
     * Select a List of values from a Matcher using a Collection
     * to identify the indices to be selected.
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
        Map answer = new LinkedHashMap(keys.size());
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

    public static List getAt(Object[] array, EmptyRange range) {
        return new ArrayList();
    }

    public static List getAt(Object[] array, ObjectRange range) {
        List list = Arrays.asList(array);
        return getAt(list, range);
    }

    /**
     * Support the subscript operator for an Array.
     *
     * @param array an Array of Objects
     * @param idx   an index
     * @return the value at the given index
     */
    public static Object getAt(Object[] array, int idx) {
        return array[normaliseIndex(idx, array.length)];
    }

    /**
     * Support the subscript operator for an Array.
     *
     * @param array an Array of Objects
     * @param idx   an index
     * @param value an Object to put at the given index
     */
    public static void putAt(Object[] array, int idx, Object value) {
    	Class arrayComponentClass = array.getClass().getComponentType();
    	final int index = normaliseIndex(idx, array.length);
        if (value instanceof Number) {
            if (!arrayComponentClass.equals(value.getClass())) {
                Object newVal = DefaultTypeTransformation.castToType(value, arrayComponentClass);
                array[index] = newVal;
                return;
            }
        } else if (Character.class.isAssignableFrom(arrayComponentClass)) {
        	array[index] = DefaultTypeTransformation.getCharFromSizeOneString(value);
            return;
        } else if (Number.class.isAssignableFrom(arrayComponentClass)) {
        	if(value instanceof Character || value instanceof String || value instanceof GString) {
        		Character ch = DefaultTypeTransformation.getCharFromSizeOneString(value);
        		array[index] = DefaultTypeTransformation.castToType(ch, arrayComponentClass);
        		return;
        	}
        }
        array[index] = value;
    }

    /**
     * Allows conversion of arrays into a mutable List.
     *
     * @param array an Array of Objects
     * @return the array as a List
     */
    public static List toList(Object[] array) {
        return new ArrayList(Arrays.asList(array));
    }

    /**
     * Support the subscript operator for a List.
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
     * Support the subscript operator for a Date.
     *
     * @param self  a Date
     * @param field a Calendar field, e.g. MONTH
     * @return the value for the given field, e.g. FEBRUARY
     * @see java.util.Calendar
     */
    public static int getAt(Date self, int field) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(self);
        return cal.get(field);
    }

    /**
     * A helper method to allow lists to work with subscript operators.
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
     * Support the range subscript operator for StringBuffer.  Index values are
     * treated as characters within the buffer.
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
     * Support the range subscript operator for StringBuffer.
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
     * A helper method to allow lists to work with subscript operators.
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
            if (col.isEmpty()) return;
            sublist.addAll(col);
        } else {
            sublist.add(value);
        }
    }

    private static List resizeListWithRangeAndGetSublist(List self, IntRange range) {
        RangeInfo info = subListBorders(self.size(), range);
        int size = self.size();
        if (info.to >= size) {
            while (size < info.to) {
                self.add(size++, null);
            }
        }
        List sublist = self.subList(info.from, info.to);
        sublist.clear();
        return sublist;
    }

    /**
     * List subscript assignment operator when given a range as the index and
     * the assignment operand is a collection.
     * Example: <code>myList[3..5] = anotherList</code>.  Items in the given
     * range are relaced with items from the collection.
     *
     * @param self  a List
     * @param range the subset of the list to set
     * @param col   the collection of values to put at the given sublist
     */
    public static void putAt(List self, IntRange range, Collection col) {
        List sublist = resizeListWithRangeAndGetSublist(self, range);
        if (col.isEmpty()) return;
        sublist.addAll(col);
    }

    /**
     * List subscript assignment operator when given a range as the index.
     * Example: <code>myList[3..5] = newItem</code>.  Items in the given
     * range are relaced with the operand.  The <code>value</code> operand is
     * always treated as a single value.
     *
     * @param self  a List
     * @param range the subset of the list to set
     * @param value the value to put at the given sublist
     */
    public static void putAt(List self, IntRange range, Object value) {
        List sublist = resizeListWithRangeAndGetSublist(self, range);
        sublist.add(value);
    }

    /**
     * A helper method to allow lists to work with subscript operators.
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
     * A helper method to allow lists to work with subscript operators.
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
     * Support the subscript operator for a Map.
     *
     * @param self a Map
     * @param key  an Object as a key for the map
     * @return the value corresponding to the given key
     */
    public static Object getAt(Map self, Object key) {
        return self.get(key);
    }

    /**
     * <p/>
     * Returns a new Map containing all entries from <code>left</code> and <code>right</code>,
     * giving precedence to <code>right</code>.  Any keys appearing in both Maps
     * will appear in the resultant map with values from the <code>right</code>
     * operand. If the <code>left</code> map is one of TreeMap, LinkedHashMap, Hashtable
     * or Properties, the returned Map will preserve that type, otherwise a HashMap will
     * be returned.
     * </p>
     * <p/>
     * Roughly equivalent to <code>Map m = new HashMap(); m.putAll(left); m.putAll(right); return m;</code>
     * but with some additional logic to preserve the <code>left</code> Map type for common cases as
     * described above.
     * </p>
     *
     * @param left  a Map
     * @param right a Map
     * @return a new Map containing all entries from left and right
     */
    public static Map plus(Map left, Map right) {
        Map map = cloneSimilarMap(left);
        map.putAll(right);
        return map;
    }

    /**
     * A helper method to allow lists to work with subscript operators
     *
     * @param self  a Map
     * @param key   an Object as a key for the map
     * @param value the value to put into the map
     * @return the value corresponding to the given key
     */
    public static Object putAt(Map self, Object key, Object value) {
        self.put(key, value);
        return value;
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
            Object value;
            try {
                value = InvokerHelper.getProperty(item, property);
            } catch (MissingPropertyExceptionNoStack mpe) {
                String causeString = new MissingPropertyException(mpe.getProperty(), mpe.getType()).toString();
                throw new MissingPropertyException("Exception evaluating property '" + property +
                        "' for " + coll.getClass().getName() + ", Reason: " + causeString);
            }
            answer.add(value);
        }
        return answer;
    }

    /**
     * A convenience method for creating an immutable map.
     *
     * @param self a Map
     * @return an immutable Map
     * @see java.util.Collections#unmodifiableMap(java.util.Map)
     */
    public static Map asImmutable(Map self) {
        return Collections.unmodifiableMap(self);
    }

    /**
     * A convenience method for creating an immutable sorted map.
     *
     * @param self a SortedMap
     * @return an immutable SortedMap
     * @see java.util.Collections#unmodifiableSortedMap(java.util.SortedMap)
     */
    public static SortedMap asImmutable(SortedMap self) {
        return Collections.unmodifiableSortedMap(self);
    }

    /**
     * A convenience method for creating an immutable list
     *
     * @param self a List
     * @return an immutable List
     * @see java.util.Collections#unmodifiableList(java.util.List)
     */
    public static List asImmutable(List self) {
        return Collections.unmodifiableList(self);
    }

    /**
     * A convenience method for creating an immutable list.
     *
     * @param self a Set
     * @return an immutable Set
     * @see java.util.Collections#unmodifiableSet(java.util.Set)
     */
    public static Set asImmutable(Set self) {
        return Collections.unmodifiableSet(self);
    }

    /**
     * A convenience method for creating an immutable sorted set.
     *
     * @param self a SortedSet
     * @return an immutable SortedSet
     * @see java.util.Collections#unmodifiableSortedSet(java.util.SortedSet)
     */
    public static SortedSet asImmutable(SortedSet self) {
        return Collections.unmodifiableSortedSet(self);
    }

    /**
     * A convenience method for creating an immutable Collection.
     *
     * @param self a Collection
     * @return an immutable Collection
     * @see java.util.Collections#unmodifiableCollection(java.util.Collection)
     */
    public static Collection asImmutable(Collection self) {
        return Collections.unmodifiableCollection(self);
    }

    /**
     * A convenience method for creating a synchronized Map.
     *
     * @param self a Map
     * @return a synchronized Map
     * @see java.util.Collections#synchronizedMap(java.util.Map)
     */
    public static Map asSynchronized(Map self) {
        return Collections.synchronizedMap(self);
    }

    /**
     * A convenience method for creating a synchronized SortedMap.
     *
     * @param self a SortedMap
     * @return a synchronized SortedMap
     * @see java.util.Collections#synchronizedSortedMap(java.util.SortedMap)
     */
    public static SortedMap asSynchronized(SortedMap self) {
        return Collections.synchronizedSortedMap(self);
    }

    /**
     * A convenience method for creating a synchronized Collection.
     *
     * @param self a Collection
     * @return a synchronized Collection
     * @see java.util.Collections#synchronizedCollection(java.util.Collection)
     */
    public static Collection asSynchronized(Collection self) {
        return Collections.synchronizedCollection(self);
    }

    /**
     * A convenience method for creating a synchronized List.
     *
     * @param self a List
     * @return a synchronized List
     * @see java.util.Collections#synchronizedList(java.util.List)
     */
    public static List asSynchronized(List self) {
        return Collections.synchronizedList(self);
    }

    /**
     * A convenience method for creating a synchronized Set.
     *
     * @param self a Set
     * @return a synchronized Set
     * @see java.util.Collections#synchronizedSet(java.util.Set)
     */
    public static Set asSynchronized(Set self) {
        return Collections.synchronizedSet(self);
    }

    /**
     * A convenience method for creating a synchronized SortedSet.
     *
     * @param self a SortedSet
     * @return a synchronized SortedSet
     * @see java.util.Collections#synchronizedSortedSet(java.util.SortedSet)
     */
    public static SortedSet asSynchronized(SortedSet self) {
        return Collections.synchronizedSortedSet(self);
    }

    /**
     * Synonym for {@link #toSpreadMap(Map)}.
     * @param self a map
     * @return a newly created Spreadmap
     */
    public static SpreadMap spread(Map self) {
        return toSpreadMap(self);
    }

    /**
     * Returns a new <code>SpreadMap</code> from this map.
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
     * @see groovy.lang.SpreadMap#SpreadMap(java.util.Map)
     */
    public static SpreadMap toSpreadMap(Map self) {
        if (self == null)
            throw new GroovyRuntimeException("Fail to convert Map to SpreadMap, because it is null.");
        else
            return new SpreadMap(self);
    }

    /**
     * Creates a spreadable map from this array.
     * @param self an object array
     * @return a newly created Spreadmap
     * @see groovy.lang.SpreadMap#SpreadMap(java.lang.Object[])
     */
    public static SpreadMap toSpreadMap(Object[] self) {
        if (self == null)
            throw new GroovyRuntimeException("Fail to convert Object[] to SpreadMap, because it is null.");
        else if (self.length % 2 != 0)
            throw new GroovyRuntimeException("Fail to convert Object[] to SpreadMap, because it's size is not even.");
        else
            return new SpreadMap(self);
    }

    /**
     * Sorts the given collection into a sorted list.  The collection items are
     * assumed to be comparable.
     *
     * @param self the collection to be sorted
     * @return the sorted collection as a List
     */
    public static List sort(Collection self) {
        List answer = asList(self);
        Collections.sort(answer, new NumberAwareComparator());
        return answer;
    }

    /**
     * Sorts the given map into a sorted map using
     * the closure as a comparator.
     *
     * @param self the map to be sorted
     * @param closure a Closure used as a comparator
     * @return the sorted map
     */
    public static Map sort(Map self, Closure closure) {
        Map result = new LinkedHashMap();
        List entries = asList(self.entrySet());
        sort(entries, closure);
        Iterator iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Sorts the given Object array into sorted order.  The array items are
     * assumed to be comparable.
     *
     * @param self the array to be sorted
     * @return the sorted array
     */
    public static Object[] sort(Object[] self) {
        Arrays.sort(self, new NumberAwareComparator());
        return self;
    }

    /**
     * Sorts the given iterator items into a sorted iterator.  The items are
     * assumed to be comparable.  The original iterator will become
     * exhausted of elements after completing this method call. A new iterator
     * is produced that traverses the items in sorted order.
     *
     * @param self the Iterator to be sorted
     * @return the sorted items as an Iterator
     */
    public static Iterator sort(Iterator self) {
        return sort(toList(self)).listIterator();
    }

    /**
     * Sorts the given iterator items into a sorted iterator using
     * the comparator.
     *
     * @param self       the Iterator to be sorted
     * @param comparator a Comparator used for comparing items
     * @return the sorted items as an Iterator
     */
    public static Iterator sort(Iterator self, Comparator comparator) {
        return sort(toList(self), comparator).listIterator();
    }

    /**
     * Sorts the Collection using the given comparator.  The elements are
     * sorted into a new list, and the existing collection is unchanged.
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
     * Sorts the given Object array into sorted order using the given comparator.
     *
     * @param self the array to be sorted
     * @param comparator a Comparator used for the comparison
     * @return the sorted array
     */
    public static Object[] sort(Object[] self, Comparator comparator) {
        Arrays.sort(self, comparator);
        return self;
    }

    /**
     * Sorts the given iterator items into a sorted iterator using
     * the closure as a comparator.
     *
     * @param self       the Iterator to be sorted
     * @param closure a Closure used as a comparator
     * @return the sorted items as an Iterator
     */
    public static Iterator sort(Iterator self, Closure closure) {
        return sort(toList(self), closure).listIterator();
    }

    /**
     * Sorts the given Object array into a newly created array using the given comparator.
     *
     * @param self the array to be sorted
     * @param closure a Closure used as a comparator
     * @return the sorted array
     */
    public static Object[] sort(Object[] self, Closure closure) {
        return sort(toList(self), closure).toArray();
    }

    /**
     * Sorts this Collection using the given closure as a comparator.  The
     * closure is passed each item from the collection, and is assumed to
     * return a comparable value (i.e. an int).
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
     * Appends an item to the List. Synonym for add().
     *
     * @param self a List
     * @param value element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of the
     *            <tt>Collection.add</tt> method).
     * @throws NoSuchElementException if the list is empty and you try to pop() it.
     */
    public static boolean push(List self, Object value) {
        return self.add(value);
    }

    /**
     * Returns the last item from the List.
     *
     * @param self a List
     * @return the last item from the List
     * @throws NoSuchElementException if the list is empty and you try to access the last() item.
     */
    public static Object last(List self) {
        if (self.isEmpty()) {
            throw new NoSuchElementException("Cannot access last() element from an empty List");
        }
        return self.get(self.size() - 1);
    }

    /**
     * Returns the first item from the List.
     *
     * @param self a List
     * @return the first item from the List
     * @throws NoSuchElementException if the list is empty and you try to access the first() item.
     */
    public static Object first(List self) {
        if (self.isEmpty()) {
            throw new NoSuchElementException("Cannot access first() element from an empty List");
        }
        return self.get(0);
    }

    /**
     * Returns the first item from the List.
     *
     * @param self a List
     * @return the first item from the List
     * @throws NoSuchElementException if the list is empty and you try to access the head() item.
     */
    public static Object head(List self) {
        return first(self);
    }

    /**
     * Returns the items from the List excluding the first item.
     *
     * @param self a List
     * @return a list without its first element
     * @throws NoSuchElementException if the list is empty and you try to access the tail() item.
     */
    public static List tail(List self) {
        if (self.isEmpty()) {
            throw new NoSuchElementException("Cannot access tail() for an empty List");
        }
        List result = new ArrayList(self);
        result.remove(0);
        return result;
    }

    /**
     * Converts this collection to a List.
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
     * Converts the given collection to another type. A default concrete
     * type is used for List, Set, or SortedSet. If the given type has
     * a constructor taking a collection, that is used. Otherwise, the
     * call is deferred to {link #asType(Object,Class)}.  If this
     * collection is already of the given type, the same instance is
     * returned.
     *
     * @param col   a collection
     * @param clazz the desired class
     * @return the object resulting from this type conversion
     * @see #asType(Object,Class)
     */
    public static Object asType(Collection col, Class clazz) {
        if (col.getClass() == clazz) {
            return col;
        }
        if (clazz == List.class) {
            return asList(col);
        }
        if (clazz == Set.class) {
            if (col instanceof Set) return col;
            return new HashSet(col);
        }
        if (clazz == SortedSet.class) {
            if (col instanceof SortedSet) return col;
            return new TreeSet(col);
        }
        if (clazz == Stack.class) {
            if (col instanceof Stack) return col;
            final Stack stack = new Stack();
            stack.addAll(col);
            return stack;
        }
        Object[] args = {col};
        try {
            return InvokerHelper.invokeConstructorOf(clazz, args);
        } catch (Exception e) {
            // ignore
        }
        return asType((Object) col, clazz);
    }

    /**
     * Converts the given array to either a List, Set, or
     * SortedSet.  If the given class is something else, the
     * call is deferred to {link #asType(Object,Class)}.
     *
     * @param ary   an array
     * @param clazz the desired class
     * @return the object resulting from this type conversion
     * @see #asType(Object,Class)
     */
    public static Object asType(Object[] ary, Class clazz) {
        if (clazz == List.class) {
            return new ArrayList(Arrays.asList(ary));
        } else if (clazz == Set.class) {
            return new HashSet(Arrays.asList(ary));
        } else if (clazz == SortedSet.class) {
            return new TreeSet(Arrays.asList(ary));
        }
        return asType((Object) ary, clazz);
    }

    /**
     * Coerces the closure to an implementation of the given class.  The class
     * is assumed to be an interface or class with a single method definition.
     * The closure is used as the implementation of that single method.
     *
     * @param cl    the implementation of the single method
     * @param clazz the target type
     * @return a Proxy of the given type which wraps this closure.
     */
    public static Object asType(Closure cl, Class clazz) {
        if (clazz.isInterface() && !(clazz.isInstance(cl))) {
            return Proxy.newProxyInstance(
                    clazz.getClassLoader(),
                    new Class[]{clazz},
                    new ConvertedClosure(cl));
        }
        return asType((Object) cl, clazz);
    }

    /**
     * Coerces this map to the given type, using the map's keys as the public
     * method names, and values as the implementation.  Typically the value
     * would be a closure which behaves like the method implementation.
     *
     * @param map   this map
     * @param clazz the target type
     * @return a Proxy of the given type, which defers calls to this map's elements.
     */
    public static Object asType(Map map, Class clazz) {
        if (!(clazz.isInstance(map)) && clazz.isInterface()) {
            return Proxy.newProxyInstance(
                    clazz.getClassLoader(),
                    new Class[]{clazz},
                    new ConvertedMap(map));
        }
        try {
            return asType((Object) map, clazz);
        } catch (GroovyCastException ce) {
            try {
                return ProxyGenerator.INSTANCE.instantiateAggregateFromBaseClass(map, clazz);
            } catch (GroovyRuntimeException cause) {
                throw new GroovyCastException("Error casting map to " + clazz.getName() +
                        ", Reason: " + cause.getMessage());
            }
        }
    }

    /**
     * Reverses the list.  The result is a new List with the identical contents
     * in reverse order.
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
     * Reverses the iterator. The original iterator will become
     * exhausted of elements after determining the reversed values.
     * A new iterator for iterating through the reversed values is returned.
     *
     * @param self an Iterator
     * @return a reversed Iterator
     */
    public static Iterator reverse(Iterator self) {
        return new ReverseListIterator(toList(self));
    }

    /**
     * Create a Collection as a union of two collections. If the left collection
     * is a Set, then the returned collection will be a Set otherwise a List.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     *
     * @param left  the left Collection
     * @param right the right Collection
     * @return the merged Collection
     */
    public static Collection plus(Collection left, Collection right) {
        final Collection answer = cloneSimilarCollection(left, left.size() + right.size());
        answer.addAll(right);
        return answer;
    }

    /**
     * Create a collection as a union of a Collection and an Object. If the collection
     * is a Set, then the returned collection will be a Set otherwise a List.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     *
     * @param left  a Collection
     * @param right an object to add/append
     * @return the resulting Collection
     */
    public static Collection plus(Collection left, Object right) {
        final Collection answer = cloneSimilarCollection(left, left.size() + 1);
        answer.add(right);
        return answer;
    }

    /**
     * Create a List composed of the elements of this list, repeated
     * a certain number of times.  Note that for non-primitive
     * elements, multiple references to the same instance will be added.
     *
     * @param self   a Collection
     * @param factor the number of times to append
     * @return the multiplied list
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
     * Create a Collection composed of the intersection of both collections.  Any
     * elements that exist in both collections are added to the resultant collection.
     *
     * @param left  a Collection
     * @param right a Collection
     * @return a Collection as an intersection of both collections
     */
    public static Collection intersect(Collection left, Collection right) {
        if (left.isEmpty())
            return createSimilarCollection(left, 0);

        if (left.size() < right.size()) {
            Collection swaptemp = left;
            left = right;
            right = swaptemp;
        }

        // TODO optimise if same type?
        // boolean nlgnSort = sameType(new Collection[]{left, right});

        Collection result = createSimilarCollection(left, left.size());
        //creates the collection to look for values.
        Collection pickFrom = new TreeSet(new NumberAwareComparator());
        pickFrom.addAll(left);

        for (Iterator iter = right.iterator(); iter.hasNext();) {
            final Object o = iter.next();
            if (pickFrom.contains(o))
                result.add(o);
        }
        return result;
    }

    /**
     * Returns <code>true</code> if the intersection of two collections is empty.
     *
     * @param left  a Collection
     * @param right a Collection
     * @return boolean   <code>true</code> if the intersection of two collections
     *         is empty, <code>false</code> otherwise.
     */
    public static boolean disjoint(Collection left, Collection right) {

        if (left.isEmpty() || right.isEmpty())
            return true;

        Collection pickFrom = new TreeSet(new NumberAwareComparator());
        pickFrom.addAll(right);

        for (Iterator iter = left.iterator(); iter.hasNext();) {
            final Object o = iter.next();
            if (pickFrom.contains(o))
                return false;
        }
        return true;
    }

    // Default comparator for objects accounting for numbers of different types.
    // Also handles nulls. Null is less than everything else.
    private static class NumberAwareComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            try {
                return DefaultTypeTransformation.compareTo(o1, o2);
            } catch (ClassCastException cce) {
            } catch (GroovyRuntimeException gre) {}
            int x1 = o1.hashCode();
            int x2 = o2.hashCode();
            return (x1 - x2);
        }

        public boolean equals(Object obj) {
            return this.equals(obj);
        }
    }

    /**
     * Compare the contents of this array to the contents of the given array.
     *
     * @param left an int array
     * @param right the operand array.
     * @return true if the contents of both arrays are equal.
     */
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

    /**
     * Determines if the contents of this array are equal to the
     * contents of the given list, in the same order.  This returns
     * <code>false</code> if either collection is <code>null</code>.
     *
     * @param left  this array
     * @param right the list being compared
     * @return true if the contents of both collections are equal
     */
    public static boolean equals(Object[] left, List right) {
        return coercedEquals(left, right);
    }

    /**
     * Determines if the contents of this list are equal to the
     * contents of the given array in the same order.  This returns
     * <code>false</code> if either collection is <code>null</code>.
     *
     * @param left  this List
     * @param right this Object[] being compared to
     * @return true if the contents of both collections are equal
     */
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
        final NumberAwareComparator numberAwareComparator = new NumberAwareComparator();
        for (int i = left.length - 1; i >= 0; i--) {
            final Object o1 = left[i];
            final Object o2 = right.get(i);
            if (o1 == null) {
                if (o2 != null) return false;
            } else {
                if (o1 instanceof Number) {
                    if (!(o2 instanceof Number && numberAwareComparator.compare(o1, o2) == 0)) {
                        return false;
                    }
                } else {
                    if (!DefaultTypeTransformation.compareEqual(o1, o2)) return false;
                }
            }
        }
        return true;
    }

    /**
     * Compare the contents of two Lists.  Order matters.
     * If numbers exist in the Lists, then they are compared as numbers,
     * for example 2 == 2L.  If either list is <code>null</code>, the result
     * is <code>false</code>.
     *
     * @param left  this List
     * @param right the List being compared to.
     * @return boolean   <code>true</code> if the contents of both lists are identical,
     *         <code>false</code> otherwise.
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
        final NumberAwareComparator numberAwareComparator = new NumberAwareComparator();
        final Iterator it1 = left.iterator(), it2 = right.iterator();
        while (it1.hasNext()) {
            final Object o1 = it1.next();
            final Object o2 = it2.next();
            if (o1 == null) {
                if (o2 != null) return false;
            } else {
                if (o1 instanceof Number) {
                    if (!(o2 instanceof Number && numberAwareComparator.compare(o1, o2) == 0)) {
                        return false;
                    }
                } else {
                    if (!DefaultTypeTransformation.compareEqual(o1, o2)) return false;
                }
            }
        }
        return true;
    }

    /**
     * Compare the contents of two Sets for equality using Groovy's coercion rules.
     * WARNING: may not be included in 1.1
     * <p/>
     * Returns <tt>true</tt> if the two sets have the same size, and every member
     * of the specified set is contained in this set (or equivalently, every member
     * of this set is contained in the specified set).
     * If numbers exist in the Lists, then they are compared as numbers,
     * for example 2 == 2L.  If either list is <code>null</code>, the result
     * is <code>false</code>.
     *
     * @param self  this List
     * @param other the List being compared to
     * @return <tt>true</tt> if the contents of both lists are identical
     */
    /*
    public static boolean coercedEquals(Set self, Set other) {
        if (self == null) {
            return other == null;
        }
        if (other == null) {
            return false;
        }
        if (self.size() != other.size()) {
            return false;
        }
        final NumberAwareComparator numberAwareComparator = new NumberAwareComparator();
        final Iterator it1 = self.iterator();
        Collection otherItems = new HashSet(other);
        while (it1.hasNext()) {
            final Object o1 = it1.next();
            if (o1 == null && !other.contains(null)) return false;
            final Iterator it2 = otherItems.iterator();
            Object foundItem = null;
            while (it2.hasNext() && foundItem == null) {
                final Object o2 = it2.next();
                if (o1 instanceof Number) {
                    if (o2 instanceof Number && numberAwareComparator.compare(o1, o2) == 0) {
                        foundItem = o2;
                    }
                } else {
                    try {
                        if (DefaultTypeTransformation.compareEqual(o1, o2)) {
                            foundItem = o2;
                        }
                    } catch (ClassCastException e) {
                        // ignore
                    }
                }
            }
            if (foundItem == null) return false;
            otherItems.remove(foundItem);
        }
        return otherItems.size() == 0;
    }
*/

    /**
     * Create a Set composed of the elements of the first set minus the
     * elements of the given collection.
     * <p/>
     * TODO: remove using number comparator?
     *
     * @param self     a set object
     * @param operands the items to remove from the set
     * @return the resulting set
     */
    public static Set minus(Set self, Collection operands) {
        final Set ansSet = createSimilarSet(self);
        ansSet.addAll(self);
        if (self.size() > 0) {
            ansSet.removeAll(operands);
        }
        return ansSet;
    }

    /**
     * Create a Set composed of the elements of the first set minus the operand.
     *
     * @param self    a set object
     * @param operand the operand to remove from the set
     * @return the resulting set
     */
    public static Set minus(Set self, Object operand) {
        final Set ansSet = createSimilarSet(self);
        Comparator numberComparator = new NumberAwareComparator();
        for (Iterator it = self.iterator(); it.hasNext();) {
            Object o = it.next();
            if (numberComparator.compare(o, operand) != 0) ansSet.add(o);
        }
        return ansSet;
    }

    /**
     * Create an array composed of the elements of the first array minus the
     * elements of the given collection.
     *
     * @param self     an object array
     * @param removeMe a Collection of elements to remove
     * @return an array with the supplied elements removed
     */
    public static Object[] minus(Object[] self, Collection removeMe) {
        return minus(toList(self), removeMe).toArray();
    }

    /**
     * Create an array composed of the elements of the first array minus the
     * elements of the given array.
     *
     * @param self     an object array
     * @param removeMe an array of elements to remove
     * @return an array with the supplied elements removed
     */
    public static Object[] minus(Object[] self, Object[] removeMe) {
        return minus(toList(self), toList(removeMe)).toArray();
    }

    /**
     * Create a List composed of the elements of the first list minus the
     * elements of the given collection.
     *
     * @param self     a List
     * @param removeMe a Collection of elements to remove
     * @return a List with the supplied elements removed
     */
    public static List minus(List self, Collection removeMe) {

        if (self.size() == 0)
            return new ArrayList();

        boolean nlgnSort = sameType(new Collection[]{self, removeMe});

        // We can't use the same tactic as for intersection
        // since AbstractCollection only does a remove on the first
        // element it encounters.

        Comparator numberComparator = new NumberAwareComparator();

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
                boolean elementRemoved = false;
                for (Iterator iterator = removeMe.iterator(); iterator.hasNext() && !elementRemoved;) {
                    Object elt = iterator.next();
                    if (numberComparator.compare(element, elt) == 0) {
                        iter.remove();
                        elementRemoved = true;
                    }
                }
            }

            //remove duplicates
            //can't use treeset since the base classes are different
            return new ArrayList(tmpAnswer);
        }
    }

    /**
     * Create a new List composed of the elements of the first list minus the
     * operand.
     *
     * @param self    a List object
     * @param operand an element to remove from the list
     * @return the resulting List with the operand removed
     */
    public static List minus(List self, Object operand) {
        Comparator numberComparator = new NumberAwareComparator();
        List ansList = new ArrayList();
        for (Iterator it = self.iterator(); it.hasNext();) {
            Object o = it.next();
            if (numberComparator.compare(o, operand) != 0) ansList.add(o);
        }
        return ansList;
    }

    /**
     * Create a new object array composed of the elements of the first array
     * minus the operand.
     *
     * @param self    an object array
     * @param operand an element to remove from the array
     * @return a new array with the operand removed
     */
    public static Object[] minus(Object[] self, Object operand) {
        return minus(toList(self), operand).toArray();
    }

    /**
     * Flatten a collection.  This collection and any nested arrays or
     * collections have their contents (recursively) added to the new collection.
     * <B>WARNING:</b>  Any Maps found in the collection are flattened to the
     * Map's values and merged into the flattened collection.  This behavior
     * may change in a future release. If you don't want Maps to be flattened
     * use flatten(Collection, Closure) with an identity closure.
     *
     * @param self a Collection to flatten
     * @return a flattened Collection
     */
    public static Collection flatten(Collection self) {
        return flatten(self, createSimilarCollection(self));
    }

    /**
     * Flatten an array.  This array and any nested arrays or
     * collections have their contents (recursively) added to the new collection.
     *
     * @param self an Array to flatten
     * @return a flattened Collection
     * @since 1.6.0
     */
    public static Collection flatten(Object[] self) {
        return flatten(toList(self), new ArrayList());
    }

    /**
     * Flatten an array.  This array and any nested arrays or
     * collections have their contents (recursively) added to the new collection.
     *
     * @param self a boolean Array to flatten
     * @return a flattened Collection
     * @since 1.6.0
     */
    public static Collection flatten(boolean[] self) {
        return flatten(toList(self), new ArrayList());
    }

    /**
     * Flatten an array.  This array and any nested arrays or
     * collections have their contents (recursively) added to the new collection.
     *
     * @param self a byte Array to flatten
     * @return a flattened Collection
     * @since 1.6.0
     */
    public static Collection flatten(byte[] self) {
        return flatten(toList(self), new ArrayList());
    }

    /**
     * Flatten an array.  This array and any nested arrays or
     * collections have their contents (recursively) added to the new collection.
     *
     * @param self a char Array to flatten
     * @return a flattened Collection
     * @since 1.6.0
     */
    public static Collection flatten(char[] self) {
        return flatten(toList(self), new ArrayList());
    }

    /**
     * Flatten an array.  This array and any nested arrays or
     * collections have their contents (recursively) added to the new collection.
     *
     * @param self a short Array to flatten
     * @return a flattened Collection
     * @since 1.6.0
     */
    public static Collection flatten(short[] self) {
        return flatten(toList(self), new ArrayList());
    }

    /**
     * Flatten an array.  This array and any nested arrays or
     * collections have their contents (recursively) added to the new collection.
     *
     * @param self an int Array to flatten
     * @return a flattened Collection
     * @since 1.6.0
     */
    public static Collection flatten(int[] self) {
        return flatten(toList(self), new ArrayList());
    }

    /**
     * Flatten an array.  This array and any nested arrays or
     * collections have their contents (recursively) added to the new collection.
     *
     * @param self a long Array to flatten
     * @return a flattened Collection
     * @since 1.6.0
     */
    public static Collection flatten(long[] self) {
        return flatten(toList(self), new ArrayList());
    }

    /**
     * Flatten an array.  This array and any nested arrays or
     * collections have their contents (recursively) added to the new collection.
     *
     * @param self a float Array to flatten
     * @return a flattened Collection
     * @since 1.6.0
     */
    public static Collection flatten(float[] self) {
        return flatten(toList(self), new ArrayList());
    }

    /**
     * Flatten an array.  This array and any nested arrays or
     * collections have their contents (recursively) added to the new collection.
     *
     * @param self a double Array to flatten
     * @return a flattened Collection
     * @since 1.6.0
     */
    public static Collection flatten(double[] self) {
        return flatten(toList(self), new ArrayList());
    }

    private static Collection flatten(Collection elements, Collection addTo) {
        Iterator iter = elements.iterator();
        while (iter.hasNext()) {
            Object element = iter.next();
            if (element instanceof Collection) {
                flatten((Collection) element, addTo);
            } else if (element instanceof Map) {
                flatten(((Map)element).values(), addTo);
            } else if (element != null && element.getClass().isArray()) {
                flatten(DefaultTypeTransformation.arrayAsCollection(element), addTo);
            } else {
                // found a leaf
                addTo.add(element);
            }
        }
        return addTo;
    }

    /**
     * Flatten a collection.  This collection and any nested arrays or
     * collections have their contents (recursively) added to the new collection.
     * For any non-Array, non-Collection object which represents some sort
     * of collective type, the supplied closure should yield the contained items;
     * otherwise, the closure should just return any element which corresponds to a leaf.
     *
     * @param self a Collection
     * @param flattenUsing a closure to determine how to flatten non-Array, non-Collection elements
     * @return a flattened Collection
     */
    public static Collection flatten(Collection self, Closure flattenUsing) {
        return flatten(self, createSimilarCollection(self), flattenUsing);
    }

    private static Collection flatten(Collection elements, Collection addTo, Closure flattenUsing) {
        Iterator iter = elements.iterator();
        while (iter.hasNext()) {
            Object element = iter.next();
            if (element instanceof Collection) {
                flatten((Collection) element, addTo, flattenUsing);
            } else if (element != null && element.getClass().isArray()) {
                flatten(DefaultTypeTransformation.arrayAsCollection(element), addTo, flattenUsing);
            } else {
                Object flattened = flattenUsing.call(new Object[]{element});
                boolean returnedSelf = flattened == element;
                if (!returnedSelf && flattened instanceof Collection) {
                    List list = toList((Collection)flattened);
                    if (list.size() == 1 && list.get(0) == element) {
                        returnedSelf = true;
                    }
                }
                if (flattened instanceof Collection && !returnedSelf) {
                    flatten((Collection) flattened, addTo, flattenUsing);
                } else {
                    addTo.add(element);
                }
            }
        }
        return addTo;
    }

    /**
     * Overloads the left shift operator to provide an easy way to append
     * objects to a Collection.
     *
     * @param self  a Collection
     * @param value an Object to be added to the collection.
     * @return same collection, after the value was added to it.
     */
    public static Collection leftShift(Collection self, Object value) {
        self.add(value);
        return self;
    }

    /**
     * Overloads the left shift operator to provide an easy way to append
     * Map.Entry values to a Map.
     *
     * @param self  a Map
     * @param entry a Map.Entry to be added to the Map.
     * @return same map, after the value has been added to it.
     */
    public static Map leftShift(Map self, Map.Entry entry) {
        self.put(entry.getKey(), entry.getValue());
        return self;
    }

    /**
     * Overloads the left shift operator to provide an easy way to append multiple
     * objects as string representations to a String.
     *
     * @param self  a String
     * @param value an Obect
     * @return a StringBuffer built from this string
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
     * objects as string representations to a StringBuffer.
     *
     * @param self  a StringBuffer
     * @param value a value to append
     * @return the StringBuffer on which this operation was invoked
     */
    public static StringBuffer leftShift(StringBuffer self, Object value) {
        self.append(value);
        return self;
    }

    /**
     * Overloads the left shift operator to provide a mechanism to append
     * values to a writer.
     *
     * @param self  a Writer
     * @param value a value to append
     * @return the writer on which this operation was invoked
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
     * @param self    a Number object
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
     * @param self    a Number object
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
     * @param self    a Number object
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

    // Primitive type array methods
    //-------------------------------------------------------------------------

    /**
     * Support the subscript operator for a byte array
     *
     * @param array a byte array
     * @param index the index of the item to retrieve
     * @return the byte at the given index
     */
    public static Object getAt(byte[] array, int index) {
        return primitiveArrayGet(array, index);
    }

    /**
     * Support the subscript operator for a char array
     *
     * @param array a char array
     * @param index the index of the item to retrieve
     * @return the char at the given index
     */
    public static Object getAt(char[] array, int index) {
        return primitiveArrayGet(array, index);
    }

    /**
     * Support the subscript operator for a short array
     *
     * @param array a short array
     * @param index the index of the item to retrieve
     * @return the short at the given index
     */
    public static Object getAt(short[] array, int index) {
        return primitiveArrayGet(array, index);
    }

    /**
     * Support the subscript operator for an int array
     *
     * @param array an int array
     * @param index the index of the item to retrieve
     * @return the int at the given index
     */
    public static Object getAt(int[] array, int index) {
        return primitiveArrayGet(array, index);
    }

    /**
     * Support the subscript operator for a long array
     *
     * @param array a long array
     * @param index the index of the item to retrieve
     * @return the long at the given index
     */
    public static Object getAt(long[] array, int index) {
        return primitiveArrayGet(array, index);
    }

    /**
     * Support the subscript operator for a float array
     *
     * @param array a float array
     * @param index the index of the item to retrieve
     * @return the float at the given index
     */
    public static Object getAt(float[] array, int index) {
        return primitiveArrayGet(array, index);
    }

    /**
     * Support the subscript operator for a double array
     *
     * @param array a double array
     * @param index the index of the item to retrieve
     * @return the double at the given index
     */
    public static Object getAt(double[] array, int index) {
        return primitiveArrayGet(array, index);
    }

    /**
     * Support the subscript operator for a boolean array
     *
     * @param array a boolean array
     * @param index the index of the item to retrieve
     * @return the boolean at the given index
     */
    public static Object getAt(boolean[] array, int index) {
        return primitiveArrayGet(array, index);
    }

    /**
     * Support the subscript operator with a range for a byte array
     *
     * @param array a byte array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved bytes
     */
    public static Object getAt(byte[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with a range for a char array
     *
     * @param array a char array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved chars
     */
    public static Object getAt(char[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with a range for a short array
     *
     * @param array a short array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved shorts
     */
    public static Object getAt(short[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with a range for an int array
     *
     * @param array an int array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the ints at the given indices
     */
    public static Object getAt(int[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with a range for a long array
     *
     * @param array a long array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved longs
     */
    public static Object getAt(long[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with a range for a float array
     *
     * @param array a float array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved floats
     */
    public static Object getAt(float[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with a range for a double array
     *
     * @param array a double array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved doubles
     */
    public static Object getAt(double[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with a range for a boolean array
     *
     * @param array a boolean array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved booleans
     */
    public static Object getAt(boolean[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an IntRange for a byte array
     *
     * @param array a byte array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved bytes
     */
    public static Object getAt(byte[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an IntRange for a char array
     *
     * @param array a char array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved chars
     */
    public static Object getAt(char[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an IntRange for a short array
     *
     * @param array a short array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved shorts
     */
    public static Object getAt(short[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an IntRange for an int array
     *
     * @param array an int array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved ints
     */
    public static Object getAt(int[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an IntRange for a long array
     *
     * @param array a long array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved longs
     */
    public static Object getAt(long[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an IntRange for a float array
     *
     * @param array a float array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved floats
     */
    public static Object getAt(float[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an IntRange for a double array
     *
     * @param array a double array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved doubles
     */
    public static Object getAt(double[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an IntRange for a boolean array
     *
     * @param array a boolean array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved booleans
     */
    public static Object getAt(boolean[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an ObjectRange for a byte array
     *
     * @param array a byte array
     * @param range an ObjectRange indicating the indices for the items to retrieve
     * @return list of the retrieved bytes
     */
    public static Object getAt(byte[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an ObjectRange for a char array
     *
     * @param array a char array
     * @param range an ObjectRange indicating the indices for the items to retrieve
     * @return list of the retrieved chars
     */
    public static Object getAt(char[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an ObjectRange for a short array
     *
     * @param array a short array
     * @param range an ObjectRange indicating the indices for the items to retrieve
     * @return list of the retrieved shorts
     */
    public static Object getAt(short[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an ObjectRange for an int array
     *
     * @param array an int array
     * @param range an ObjectRange indicating the indices for the items to retrieve
     * @return list of the retrieved ints
     */
    public static Object getAt(int[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an ObjectRange for a long array
     *
     * @param array a long array
     * @param range an ObjectRange indicating the indices for the items to retrieve
     * @return list of the retrieved longs
     */
    public static Object getAt(long[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an ObjectRange for a float array
     *
     * @param array a float array
     * @param range an ObjectRange indicating the indices for the items to retrieve
     * @return list of the retrieved floats
     */
    public static Object getAt(float[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an ObjectRange for a double array
     *
     * @param array a double array
     * @param range an ObjectRange indicating the indices for the items to retrieve
     * @return list of the retrieved doubles
     */
    public static Object getAt(double[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an ObjectRange for a byte array
     *
     * @param array a byte array
     * @param range an ObjectRange indicating the indices for the items to retrieve
     * @return list of the retrieved bytes
     */
    public static Object getAt(boolean[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with a collection for a byte array
     *
     * @param array a byte array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the bytes at the given indices
     */
    public static Object getAt(byte[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator with a collection for a char array
     *
     * @param array a char array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the chars at the given indices
     */
    public static Object getAt(char[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator with a collection for a short array
     *
     * @param array a short array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the shorts at the given indices
     */
    public static Object getAt(short[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator with a collection for an int array
     *
     * @param array an int array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the ints at the given indices
     */
    public static Object getAt(int[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator with a collection for a long array
     *
     * @param array a long array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the longs at the given indices
     */
    public static Object getAt(long[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator with a collection for a float array
     *
     * @param array a float array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the floats at the given indices
     */
    public static Object getAt(float[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator with a collection for a double array
     *
     * @param array a double array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the doubles at the given indices
     */
    public static Object getAt(double[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator with a collection for a boolean array
     *
     * @param array a boolean array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the booleans at the given indices
     */
    public static Object getAt(boolean[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator for a Bitset
     *
     * @param self  a BitSet
     * @param index index to retrieve
     * @return value of the bit at the given index
     * @see java.util.BitSet
     */
    public static boolean getAt(BitSet self, int index) {
        return self.get(index);
    }

    /**
     * Support retrieving a subset of a BitSet using a Range
     *
     * @param self  a BitSet
     * @param range a Range defining the desired subset
     * @return a new BitSet that represents the requested subset
     * @see java.util.BitSet
     * @see groovy.lang.IntRange
     */
    public static BitSet getAt(BitSet self, IntRange range) {
        int from = DefaultTypeTransformation.intUnbox(range.getFrom());
        int to = DefaultTypeTransformation.intUnbox(range.getTo());

        BitSet result = new BitSet();

        int numberOfBits = to - from + 1;
        int adjuster = 1;
        int offset = from;

        if (range.isReverse()) {
            adjuster = -1;
            offset = to;
        }

        for (int i = 0; i < numberOfBits; i++) {
            result.set(i, self.get(offset + (adjuster * i)));
        }

        return result;
    }

    public static Boolean putAt(boolean[] array, int idx, Boolean newValue) {
        return (Boolean) primitiveArrayPut(array, idx, newValue);
    }

    public static Byte putAt(byte[] array, int idx, Object newValue) {
        if (!(newValue instanceof Byte)) {
            Number n = (Number) newValue;
            newValue = new Byte(n.byteValue());
        }
        return (Byte) primitiveArrayPut(array, idx, newValue);
    }

    public static Character putAt(char[] array, int idx, Object newValue) {
    	newValue = DefaultTypeTransformation.getCharFromSizeOneString(newValue);
        return (Character) primitiveArrayPut(array, idx, newValue);
    }

    public static Short putAt(short[] array, int idx, Object newValue) {
        if (!(newValue instanceof Short)) {
        	if(newValue instanceof Character || newValue instanceof String || newValue instanceof GString) {
        		Character ch = DefaultTypeTransformation.getCharFromSizeOneString(newValue);
        		newValue = DefaultTypeTransformation.castToType(ch, Short.class);
        	} else {
                Number n = (Number) newValue;
                newValue = new Short(n.shortValue());
        	}
        }
        return (Short) primitiveArrayPut(array, idx, newValue);
    }

    public static Integer putAt(int[] array, int idx, Object newValue) {
        if (!(newValue instanceof Integer)) {
        	if(newValue instanceof Character || newValue instanceof String || newValue instanceof GString) {
        		Character ch = DefaultTypeTransformation.getCharFromSizeOneString(newValue);
        		newValue = DefaultTypeTransformation.castToType(ch, Integer.class);
        	} else {
                Number n = (Number) newValue;
                newValue = new Integer(n.intValue());
        	}
        }
        return (Integer) primitiveArrayPut(array, idx, newValue);
    }

    public static Long putAt(long[] array, int idx, Object newValue) {
        if (!(newValue instanceof Long)) {
        	if(newValue instanceof Character || newValue instanceof String || newValue instanceof GString) {
        		Character ch = DefaultTypeTransformation.getCharFromSizeOneString(newValue);
        		newValue = DefaultTypeTransformation.castToType(ch, Long.class);
        	} else {
                Number n = (Number) newValue;
                newValue = new Long(n.longValue());
        	}
        }
        return (Long) primitiveArrayPut(array, idx, newValue);
    }

    public static Float putAt(float[] array, int idx, Object newValue) {
        if (!(newValue instanceof Float)) {
        	if(newValue instanceof Character || newValue instanceof String || newValue instanceof GString) {
        		Character ch = DefaultTypeTransformation.getCharFromSizeOneString(newValue);
        		newValue = DefaultTypeTransformation.castToType(ch, Float.class);
        	} else {
                Number n = (Number) newValue;
                newValue = new Float(n.floatValue());
        	}
        }
        return (Float) primitiveArrayPut(array, idx, newValue);
    }

    public static Double putAt(double[] array, int idx, Object newValue) {
        if (!(newValue instanceof Double)) {
        	if(newValue instanceof Character || newValue instanceof String || newValue instanceof GString) {
        		Character ch = DefaultTypeTransformation.getCharFromSizeOneString(newValue);
        		newValue = DefaultTypeTransformation.castToType(ch, Double.class);
        	} else {
                Number n = (Number) newValue;
                newValue = new Double(n.doubleValue());
        	}
        }
        return (Double) primitiveArrayPut(array, idx, newValue);
    }

    /**
     * Support assigning a range of values with a single assignment statement.
     *
     * @param self  a BitSet
     * @param range the range of values to set
     * @param value value
     * @see java.util.BitSet
     * @see groovy.lang.Range
     */
    public static void putAt(BitSet self, IntRange range, boolean value) {
        int from = DefaultTypeTransformation.intUnbox(range.getFrom());
        int to = DefaultTypeTransformation.intUnbox(range.getTo());

        // If this is a backwards range, reverse the arguments to set.
        if (from > to) {
            int tmp = to;
            to = from;
            from = tmp;
        }
        self.set(from, to + 1, value);
    }

    /**
     * Support subscript-style assignment for a BitSet.
     *
     * @param self  a BitSet
     * @param index index of the entry to set
     * @param value value
     * @see java.util.BitSet
     */
    public static void putAt(BitSet self, int index, boolean value) {
        self.set(index, value);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a boolean array
     * @return the length of the array
     * @see Array#getLength(Object)
     */
    public static int size(boolean[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a byte array
     * @return the length of the array
     * @see Array#getLength(Object)
     */
    public static int size(byte[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a char array
     * @return the length of the array
     * @see Array#getLength(Object)
     */
    public static int size(char[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a short array
     * @return the length of the array
     * @see Array#getLength(Object)
     */
    public static int size(short[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array an int array
     * @return the length of the array
     * @see Array#getLength(Object)
     */
    public static int size(int[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a long array
     * @return the length of the array
     * @see Array#getLength(Object)
     */
    public static int size(long[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a float array
     * @return the length of the array
     * @see Array#getLength(Object)
     */
    public static int size(float[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a double array
     * @return the length of the array
     * @see Array#getLength(Object)
     */
    public static int size(double[] array) {
        return Array.getLength(array);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param array an array
     * @return a list containing the contents of this array.
     */
    public static List toList(byte[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param array a boolean array
     * @return a list containing the contents of this array.
     * @since 1.6.0
     */
    public static List toList(boolean[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param array a char array
     * @return a list containing the contents of this array.
     * @since 1.0
     */
    public static List toList(char[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param array a short array
     * @return a list containing the contents of this array.
     */
    public static List toList(short[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param array an int array
     * @return a list containing the contents of this array.
     */
    public static List toList(int[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param array a long array
     * @return a list containing the contents of this array.
     */
    public static List toList(long[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param array a float array
     * @return a list containing the contents of this array.
     */
    public static List toList(float[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param array a double array
     * @return a list containing the contents of this array.
     */
    public static List toList(double[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    private static final char[] T_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();

    private static final String CHUNK_SEPARATOR = "\r\n";

    /**
     * Produce a Writable object which writes the Base64 encoding of the byte array.
     * Calling toString() on the result returns the encoding as a String. For more
     * information on Base64 encoding and chunking see <code>RFC 4648</code>.
     *
     * @param data Byte array to be encoded
     * @param chunked whether or not the Base64 encoded data should be MIME chunked
     * @return object which will write the Base64 encoding of the byte array
     */
    public static Writable encodeBase64(Byte[] data, final boolean chunked) {
        return encodeBase64(DefaultTypeTransformation.convertToByteArray(data), chunked);
    }

    /**
     * Produce a Writable object which writes the Base64 encoding of the byte array.
     * Calling toString() on the result returns the encoding as a String. For more
     * information on Base64 encoding and chunking see <code>RFC 4648</code>.
     *
     * @param data Byte array to be encoded
     * @return object which will write the Base64 encoding of the byte array
     */
    public static Writable encodeBase64(Byte[] data) {
        return encodeBase64(DefaultTypeTransformation.convertToByteArray(data), false);
    }

    /**
     * Produce a Writable object which writes the Base64 encoding of the byte array.
     * Calling toString() on the result returns the encoding as a String. For more
     * information on Base64 encoding and chunking see <code>RFC 4648</code>.
     *
     * @param data byte array to be encoded
     * @param chunked whether or not the Base64 encoded data should be MIME chunked
     * @return object which will write the Base64 encoding of the byte array
     */
    public static Writable encodeBase64(final byte[] data, final boolean chunked) {
        return new Writable() {
            public Writer writeTo(final Writer writer) throws IOException {
                int charCount = 0;
                final int dLimit = (data.length / 3) * 3;

                for (int dIndex = 0; dIndex != dLimit; dIndex += 3) {
                    int d = ((data[dIndex] & 0XFF) << 16) | ((data[dIndex + 1] & 0XFF) << 8) | (data[dIndex + 2] & 0XFF);

                    writer.write(T_TABLE[d >> 18]);
                    writer.write(T_TABLE[(d >> 12) & 0X3F]);
                    writer.write(T_TABLE[(d >> 6) & 0X3F]);
                    writer.write(T_TABLE[d & 0X3F]);

                    if (chunked && ++charCount == 19) {
                        writer.write(CHUNK_SEPARATOR);
                        charCount = 0;
                    }
                }

                if (dLimit != data.length) {
                    int d = (data[dLimit] & 0XFF) << 16;

                    if (dLimit + 1 != data.length) {
                        d |= (data[dLimit + 1] & 0XFF) << 8;
                    }

                    writer.write(T_TABLE[d >> 18]);
                    writer.write(T_TABLE[(d >> 12) & 0X3F]);
                    writer.write((dLimit + 1 < data.length) ? T_TABLE[(d >> 6) & 0X3F] : '=');
                    writer.write('=');
                    if (chunked && charCount != 0) {
                        writer.write(CHUNK_SEPARATOR);
                    }
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

    /**
     * Produce a Writable object which writes the Base64 encoding of the byte array.
     * Calling toString() on the result returns the encoding as a String. For more
     * information on Base64 encoding and chunking see <code>RFC 4648</code>.
     *
     * @param data byte array to be encoded
     * @return object which will write the Base64 encoding of the byte array
     */
    public static Writable encodeBase64(final byte[] data) {
        return encodeBase64(data, false);
    }

    private static final byte[] TRANSLATE_TABLE = (
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
     * Decode the String from Base64 into a byte array.
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
            final int sixBit = (c < 123) ? TRANSLATE_TABLE[c] : 66;

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
     * @param idx  the index of interest
     * @return the returned value from the array
     */
    protected static Object primitiveArrayGet(Object self, int idx) {
        return Array.get(self, normaliseIndex(idx, Array.getLength(self)));
    }

    /**
     * Implements the getAt(Range) method for primitve type arrays.
     *
     * @param self  an array object
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
     * Implements the getAt(Collection) method for primitve type arrays.  Each
     * value in the collection argument is assumed to be a valid array index.
     * The value at each index is then added to a list which is returned.
     *
     * @param self    an array object
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
     * Implements the setAt(int idx) method for primitve type arrays.
     *
     * @param self     an object
     * @param idx      the index of interest
     * @param newValue the new value to be put into the index of interest
     * @return the added value
     */
    protected static Object primitiveArrayPut(Object self, int idx, Object newValue) {
        Array.set(self, normaliseIndex(idx, Array.getLength(self)), newValue);
        return newValue;
    }

    // String methods
    //-------------------------------------------------------------------------

    /**
     * Converts the given string into a Character object
     * using the first character in the string.
     *
     * @param self a String
     * @return the first Character
     */
    public static Character toCharacter(String self) {
        /** @todo use cache? */
        return new Character(self.charAt(0));
    }

    /**
     * Converts the given string into a Boolean object.
     * If the trimmed string is "true", "y" or "1" (ignoring case)
     * then the result is true othewrwise it is false.
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
     * Convenience method to split a string (with whitespace as delimiter)
     * Like tokenize, but returns an Array of Strings instead of a List
     *
     * @param self the string to split
     * @return String[] result of split
     */
    public static String[] split(String self) {
        StringTokenizer st = new StringTokenizer(self);
        String[] strings = new String[st.countTokens()];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = st.nextToken();
        }
        return strings;
    }

    /**
     * Tokenize a String based on the given string delimiter.
     *
     * @param self  a String
     * @param token the delimiter
     * @return a List of tokens
     * @see java.util.StringTokenizer#StringTokenizer(java.lang.String, java.lang.String)
     */
    public static List tokenize(String self, String token) {
        return InvokerHelper.asList(new StringTokenizer(self, token));
    }

    /**
     * Tokenize a String (with a whitespace as the delimiter).
     *
     * @param self a String
     * @return a List of tokens
     * @see StringTokenizer#StringTokenizer(java.lang.String)
     */
    public static List tokenize(String self) {
        return InvokerHelper.asList(new StringTokenizer(self));
    }

    /**
     * Appends the String representation of the given operand to this string.
     *
     * @param left  a String
     * @param value any Object
     * @return the new string with the object appended
     */
    public static String plus(String left, Object value) {
        return left + toString(value);
    }

    /**
     * Appends a String to the string representation of this number.
     *
     * @param value a Number
     * @param right a String
     * @return a String
     */
    public static String plus(Number value, String right) {
        return toString(value) + right;
    }

    /**
     * Appends a String to this StringBuffer.
     *
     * @param left  a StringBuffer
     * @param value a String
     * @return a String
     */
    public static String plus(StringBuffer left, String value) {
        return left + value;
    }

    /**
     * Remove a part of a String. This replaces the first occurrence
     * of target within self with '' and returns the result. If
     * target is a regex Pattern, the first occurrence of that
     * pattern will be removed (using regex matching), otherwise
     * the first occurrence of target.toString() will be removed.
     *
     * @param self   a String
     * @param target an object representing the part to remove
     * @return a String minus the part to be removed
     */
    public static String minus(String self, Object target) {
        if (target instanceof Pattern) {
            return ((Pattern)target).matcher(self).replaceFirst("");
        }
        String text = toString(target);
        int index = self.indexOf(text);
        if (index == -1) return self;
        int end = index + text.length();
        if (self.length() > end) {
            return self.substring(0, index) + self.substring(end);
        }
        return self.substring(0, index);
    }

    /**
     * Provide an implementation of contains() like
     * {@link Collection#contains(Object)} to make Strings more polymorphic.
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
     * Count the number of occurencies of a substring.
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
        if (envp == null) {
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
     * Repeat a String a certain number of times.
     *
     * @param self   a String to be repeated
     * @param factor the number of times the String should be repeated
     * @return a String composed of a repetition
     * @throws IllegalArgumentException if the number of repetitions is &lt; 0
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
     * Returns the string representation of the given map.
     *
     * @param self a Map
     * @return the string representation
     * @see #toMapString(Map)
     */
    public static String toString(Map self) {
        return toMapString(self);
    }

    /**
     * Returns the string representation of this map.  The string displays the
     * contents of the map, i.e. <code>{one=1, two=2, three=3}</code>.
     *
     * @param self a Map
     * @return the string representation
     */
    public static String toMapString(Map self) {
        return (self == null) ? "null" : InvokerHelper.toMapString(self);
    }

    /**
     * Returns the string representation of the given collection.  The string
     * displays the contents of the collection, i.e.
     * <code>[1, 2, a]</code>.
     *
     * @param self a Collection
     * @return the string representation
     * @see #toListString(Collection)
     */
    public static String toString(Collection self) {
        return toListString(self);
    }

    /**
     * Returns the string representation of the given list.  The string
     * displays the contents of the list, similar to a list literal, i.e.
     * <code>[1, 2, a]</code>.
     *
     * @param self a Collection
     * @return the string representation
     */
    public static String toListString(Collection self) {
        return (self == null) ? "null" : InvokerHelper.toListString(self);
    }

    /**
     * Returns the string representation of this array's contents.
     *
     * @param self an Object[]
     * @return the string representation
     * @see #toArrayString(Object[])
     */
    public static String toString(Object[] self) {
        return toArrayString(self);
    }

    /**
     * Returns the string representation of the given array.  The string
     * displays the contents of the array, similar to an array literal, i.e.
     * <code>{1, 2, "a"}</code>.
     *
     * @param self an Object[]
     * @return the string representation
     */
    public static String toArrayString(Object[] self) {
        return (self == null) ? "null" : InvokerHelper.toArrayString(self);
    }

    /**
     * Create a String representation of this object.
     * @param value an object
     * @return a string.
     */
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
     * Increment a Character by one.
     *
     * @param self a Character
     * @return an incremented Number
     */
    public static Character next(Character self) {
        char leftChar = self.charValue();
        char result = (char) (leftChar + 1);
        return new Character(result);
    }

    /**
     * Increment a Number by one.
     *
     * @param self a Number
     * @return an incremented Number
     */
    public static Number next(Number self) {
        return plus(self, ONE);
    }

    /**
     * Decrement a Character by one.
     *
     * @param self a Character
     * @return a decremented Number
     */
    public static Character previous(Character self) {
        char leftChar = self.charValue();
        char result = (char) (leftChar - 1);
        return new Character(result);
    }

    /**
     * Decrement a Number by one.
     *
     * @param self a Number
     * @return a decremented Number
     */
    public static Number previous(Number self) {
        return minus(self, ONE);
    }

    /**
     * Add a Character and a Number.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.  This character should be one
     * of the digits '0' through '9', and the result is addition of the integer
     * conversion of this character plus the operand.
     *
     * @see Integer#valueOf(String)
     * @param left  a Character
     * @param right a Number
     * @return the Number corresponding to the addition of left and right
     */
    public static Number plus(Character left, Number right) {
        return plus(new Integer(left.charValue()), right);
    }

    /**
     * Add a Number and a Character.  This assumes the character is one of the
     * digits '0' through '9'.
     *
     * @see Integer#valueOf(String)
     * @param left  a Number
     * @param right a Character
     * @return The Number corresponding to the addition of left and right
     */
    public static Number plus(Number left, Character right) {
        return plus(left, new Integer(right.charValue()));
    }

    /**
     * Add two Characters.  Both characters are assumed to represent digits ('0'
     * through '9') and add the results.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     *
     * @see #plus(Number, Character)
     * @param left  a Character
     * @param right a Character
     * @return the Number corresponding to the addition of left and right
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
     * Compare a Character and a Number.  The character is assumed to be a
     * digit (i.e. '0' through '9') which is converted to its Integer
     * representation.
     *
     * @param left  a Character
     * @param right a Number
     * @return the result of the comparison
     */
    public static int compareTo(Character left, Number right) {
        return compareTo(new Integer(left.charValue()), right);
    }

    /**
     * Compare a Number and a Character.  The character is assumed to be a
     * digit (i.e. '0' through '9') which is converted to its Integer
     * representation.
     *
     * @param left  a Number
     * @param right a Character
     * @return the result of the comparison
     */
    public static int compareTo(Number left, Character right) {
        return compareTo(left, new Integer(right.charValue()));
    }

    /**
     * Compare two Characters.  Each character is assumed to be a
     * digit (i.e. '0' through '9') which is converted to its Integer
     * representation.
     *
     * @param left  a Character
     * @param right a Character
     * @return the result of the comparison
     */
    public static int compareTo(Character left, Character right) {
        return compareTo(new Integer(left.charValue()), right);
    }

    /**
     * Compare two Numbers.  Equality (==) for numbers dispatches to this.
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
     * Subtract a Number from a Character.  The character is assumed to be a
     * digit (i.e. '0' through '9') which is converted to its Integer
     * representation.
     *
     * @param left  a Character
     * @param right a Number
     * @return the Number corresponding to the subtraction of right from left
     */
    public static Number minus(Character left, Number right) {
        return minus(new Integer(left.charValue()), right);
    }

    /**
     * Subtract a Character from a Number.  The character is assumed to be a
     * digit (i.e. '0' through '9') which is converted to its Integer
     * representation.
     *
     * @param left  a Number
     * @param right a Character
     * @return the Number corresponding to the subtraction of right from left
     */
    public static Number minus(Number left, Character right) {
        return minus(left, new Integer(right.charValue()));
    }

    /**
     * Subtract one Characters from another by converting them both to their
     * Integer representations.  Each character is assumed to be a
     * digit (i.e. '0' through '9') which is converted to its Integer
     * representation.
     *
     * @param left  a Character
     * @param right a Character
     * @return the Number corresponding to the subtraction of right from left
     */
    public static Number minus(Character left, Character right) {
        return minus(new Integer(left.charValue()), right);
    }

    /**
     * Substraction of two Numbers.
     *
     * @param left  a Number
     * @param right another Number to substract to the first one
     * @return the substraction
     */
    public static Number minus(Number left, Number right) {
        return NumberMath.subtract(left, right);
    }

    /**
     * Multiply a Character by a Number.  The character is assumed to be a
     * digit (i.e. '0' through '9') which is converted to its Integer
     * representation.
     *
     * @param left  a Character
     * @param right a Number
     * @return the Number corresponding to the multiplication of left by right
     */
    public static Number multiply(Character left, Number right) {
        return multiply(new Integer(left.charValue()), right);
    }

    /**
     * Multiply a Number by a Character.  The character is assumed to be a
     * digit (i.e. '0' through '9') which is converted to its Integer
     * representation.
     *
     * @param left  a Number
     * @param right a Character
     * @return the multiplication of left by right
     */
    public static Number multiply(Number left, Character right) {
        return multiply(left, new Integer(right.charValue()));
    }

    /**
     * Multiply two Characters.  Each character is assumed to be a
     * digit (i.e. '0' through '9') which is converted to its Integer
     * representation.
     *
     * @param left  a Character
     * @param right another Character
     * @return the Number corresponding to the multiplication of left by right
     */
    public static Number multiply(Character left, Character right) {
        return multiply(new Integer(left.charValue()), right);
    }

    /**
     * Multiply two Numbers.
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
     * @return the multiplication of left by right
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
     * @return the multiplication of left by right
     */
    public static Number multiply(BigDecimal left, BigInteger right) {
        return NumberMath.multiply(left, right);
    }

    /**
     * Power of a Number to a certain exponent.  Called by the '**' operator.
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
     * Divide a Character by a Number.  The character is assumed to be a
     * digit (i.e. '0' through '9') which is converted to its Integer
     * representation.
     *
     * @param left  a Character
     * @param right a Number
     * @return the Number corresponding to the division of left by right
     */
    public static Number div(Character left, Number right) {
        return div(new Integer(left.charValue()), right);
    }

    /**
     * Divide a Number by a Character.  The character is assumed to be a
     * digit (i.e. '0' through '9') which is converted to its Integer
     * representation.
     *
     * @param left  a Number
     * @param right a Character
     * @return the Number corresponding to the division of left by right
     */
    public static Number div(Number left, Character right) {
        return div(left, new Integer(right.charValue()));
    }

    /**
     * Divide one Character by another.  Each character is assumed to be a
     * digit (i.e. '0' through '9') which is converted to its Integer
     * representation.
     *
     * @param left  a Character
     * @param right another Character
     * @return the Number corresponding to the division of left by right
     */
    public static Number div(Character left, Character right) {
        return div(new Integer(left.charValue()), right);
    }

    /**
     * Divide two Numbers.
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
     * Integer Divide a Character by a Number.
     *
     * @param left  a Character
     * @param right a Number
     * @return a Number (an Integer) resulting from the integer division operation
     */
    public static Number intdiv(Character left, Number right) {
        return intdiv(new Integer(left.charValue()), right);
    }

    /**
     * Integer Divide a Number by a Character.
     *
     * @param left  a Number
     * @param right a Character
     * @return a Number (an Integer) resulting from the integer division operation
     */
    public static Number intdiv(Number left, Character right) {
        return intdiv(left, new Integer(right.charValue()));
    }

    /**
     * Integer Divide two Characters.
     *
     * @param left  a Character
     * @param right another Character
     * @return a Number (an Integer) resulting from the integer division operation
     */
    public static Number intdiv(Character left, Character right) {
        return intdiv(new Integer(left.charValue()), right);
    }

    /**
     * Integer Divide two Numbers.
     *
     * @param left  a Number
     * @param right another Number
     * @return a Number (an Integer) resulting from the integer division operation
     */
    public static Number intdiv(Number left, Number right) {
        return NumberMath.intdiv(left, right);
    }

    /**
     * Bitwise OR together two numbers.
     *
     * @param left  a Number
     * @param right another Number to bitwise OR
     * @return the bitwise OR of both Numbers
     */
    public static Number or(Number left, Number right) {
        return NumberMath.or(left, right);
    }

    /**
     * Bitwise AND together two Numbers.
     *
     * @param left  a Number
     * @param right another Number to bitwise AND
     * @return the bitwise AND of both Numbers
     */
    public static Number and(Number left, Number right) {
        return NumberMath.and(left, right);
    }

    /**
     * Bitwise AND together two BitSets.
     *
     * @param left  a BitSet
     * @param right another BitSet to bitwise AND
     * @return the bitwise AND of both BitSets
     */
    public static BitSet and(BitSet left, BitSet right) {
        BitSet result = (BitSet) left.clone();
        result.and(right);
        return result;
    }

    /**
     * Bitwise XOR together two BitSets.  Called when the '^' operator is used
     * between two bit sets.
     *
     * @param left  a BitSet
     * @param right another BitSet to bitwise AND
     * @return the bitwise XOR of both BitSets
     */
    public static BitSet xor(BitSet left, BitSet right) {
        BitSet result = (BitSet) left.clone();
        result.xor(right);
        return result;
    }

    /**
     * Bitwise NEGATE a BitSet.
     *
     * @param self a BitSet
     * @return the bitwise NEGATE of the BitSet
     */
    public static BitSet bitwiseNegate(BitSet self) {
        BitSet result = (BitSet) self.clone();
        result.flip(0, result.size() - 1);
        return result;
    }

    /**
     * Bitwise OR together two BitSets.  Called when the '|' operator is used
     * between two bit sets.
     *
     * @param left  a BitSet
     * @param right another BitSet to bitwise AND
     * @return the bitwise OR of both BitSets
     */
    public static BitSet or(BitSet left, BitSet right) {
        BitSet result = (BitSet) left.clone();
        result.or(right);
        return result;
    }

    /**
     * Bitwise XOR together two Numbers.  Called when the '|' operator is used.
     *
     * @param left  a Number
     * @param right another Number to bitwse XOR
     * @return the bitwise XOR of both Numbers
     */
    public static Number xor(Number left, Number right) {
        return NumberMath.xor(left, right);
    }

    /**
     * Performs a division modulus operation.  Called by the '%' operator.
     *
     * @param left  a Number
     * @param right another Number to mod
     * @return the modulus result
     */
    public static Number mod(Number left, Number right) {
        return NumberMath.mod(left, right);
    }

    /**
     * Negates the number.  Equivalent to the '-' operator when it preceeds
     * a single operand, i.e. <code>-10</code>
     *
     * @param left a Number
     * @return the negation of the number
     */
    public static Number unaryMinus(Number left) {
        return NumberMath.unaryMinus(left);
    }


    /**
     * Executes the closure this many times, starting from zero.  The current
     * index is passed to the closure each time.
     * Example:
     * <pre>10.times {
     *   println it
     * }</pre>
     * Prints the numbers 0 through 9.
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
     * Iterates from this number up to the given number, inclusive,
     * incrementing by one each time.
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

    /**
     * Iterates from this number up to the given number, inclusive,
     * incrementing by one each time.
     *
     * @param self    a long
     * @param to the end number
     * @param closure the code to execute for each number
     */
    public static void upto(long self, Number to, Closure closure) {
        long to1 = to.longValue();
        if (self <= to1) {
            for (long i = self; i <= to1; i++) {
                closure.call(new Long(i));
            }
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to + ")");
    }

    /**
     * Iterates from this number up to the given number, inclusive,
     * incrementing by one each time.
     *
     * @param self    a Long
     * @param to the end number
     * @param closure the code to execute for each number
     */
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

    /**
     * Iterates from this number up to the given number, inclusive,
     * incrementing by one each time.
     *
     * @param self    a float
     * @param to the end number
     * @param closure the code to execute for each number
     */
    public static void upto(float self, Number to, Closure closure) {
        float to1 = to.floatValue();
        if (self <= to1) {
            for (float i = self; i <= to1; i++) {
                closure.call(new Float(i));
            }
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to + ")");
    }

    /**
     * Iterates from this number up to the given number, inclusive,
     * incrementing by one each time.
     *
     * @param self    a Float
     * @param to the end number
     * @param closure the code to execute for each number
     */
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

    /**
     * Iterates from this number up to the given number, inclusive,
     * incrementing by one each time.
     *
     * @param self    a double
     * @param to the end number
     * @param closure the code to execute for each number
     */
    public static void upto(double self, Number to, Closure closure) {
        double to1 = to.doubleValue();
        if (self <= to1) {
            for (double i = self; i <= to1; i++) {
                closure.call(new Double(i));
            }
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to + ")");
    }

    /**
     * Iterates from this number up to the given number, inclusive,
     * incrementing by one each time.
     *
     * @param self    a Double
     * @param to the end number
     * @param closure the code to execute for each number
     */
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

    /**
     * Iterates from this number up to the given number, inclusive,
     * incrementing by one each time.  Example:
     * <pre>0.upto( 10 ) {
     *   println it
     * }</pre>
     * Prints numbers 0 to 10
     *
     * @param self    a BigInteger
     * @param to the end number
     * @param closure the code to execute for each number
     */
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

    /**
     * Iterates from this number up to the given number, inclusive,
     * incrementing by one each time.
     * <pre>0.upto( 10 ) {
     *   println it
     * }</pre>
     * Prints numbers 0.1, 1.1, 2.1... to 9.1
     *
     * @param self    a BigDecimal
     * @param to the end number
     * @param closure the code to execute for each number
     */
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
     * Iterates from this number down to the given number, inclusive,
     * decrementing by one each time.
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

    /**
     * Iterates from this number down to the given number, inclusive,
     * decrementing by one each time.
     *
     * @param self    a long
     * @param to the end number
     * @param closure the code to execute for each number
     */
    public static void downto(long self, Number to, Closure closure) {
        long to1 = to.longValue();
        if (self >= to1) {
            for (long i = self; i >= to1; i--) {
                closure.call(new Long(i));
            }
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
    }

    /**
     * Iterates from this number down to the given number, inclusive,
     * decrementing by one each time.
     *
     * @param self    a Long
     * @param to the end number
     * @param closure the code to execute for each number
     */
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

    /**
     * Iterates from this number down to the given number, inclusive,
     * decrementing by one each time.
     *
     * @param self    a float
     * @param to the end number
     * @param closure the code to execute for each number
     */
    public static void downto(float self, Number to, Closure closure) {
        float to1 = to.floatValue();
        if (self >= to1) {
            for (float i = self; i >= to1; i--) {
                closure.call(new Float(i));
            }
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
    }

    /**
     * Iterates from this number down to the given number, inclusive,
     * decrementing by one each time.
     *
     * @param self    a Float
     * @param to the end number
     * @param closure the code to execute for each number
     */
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

    /**
     * Iterates from this number down to the given number, inclusive,
     * decrementing by one each time.
     *
     * @param self    a double
     * @param to the end number
     * @param closure the code to execute for each number
     */
    public static void downto(double self, Number to, Closure closure) {
        double to1 = to.doubleValue();
        if (self >= to1) {
            for (double i = self; i >= to1; i--) {
                closure.call(new Double(i));
            }
        } else
            throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
    }

    /**
     * Iterates from this number down to the given number, inclusive,
     * decrementing by one each time.
     *
     * @param self    a Double
     * @param to the end number
     * @param closure the code to execute for each number
     */
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

    /**
     * Iterates from this number down to the given number, inclusive,
     * decrementing by one each time.
     *
     * @param self    a BigInteger
     * @param to the end number
     * @param closure the code to execute for each number
     */
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

    /**
     * Iterates from this number down to the given number, inclusive,
     * decrementing by one each time.  Each number is passed to the closure.
     * Example:
     * <pre>10.5.downto(0) {
     *   println it
     * }</pre>
     * Prints numbers 10.5, 9.5 ... to 0.5.
     *
     * @param self    a BigDecimal
     * @param to the end number
     * @param closure the code to execute for each number
     */
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
     * Iterates from this number up to the given number using a step increment.
     * Each intermediate number is passed to the given closure.  Example:
     * <pre>0.step( 10, 2 ) {
     *   println it
     * }</pre>
     * Prints even numbers 0 through 8.
     *
     * @param self       a Number to start with
     * @param to         a Number to go up to, exclusive
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
     * Parse a String into a Short
     *
     * @param self a String
     * @return a Short
     */
    public static Short toShort(String self) {
        return Short.valueOf(self.trim());
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
     * Determine if a String can be parsed into an Integer.
     *
     * @param self a String
     * @return true if the string can be parsed
     */
    public static boolean isInteger(String self) {
        try {
            Integer.valueOf(self.trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Determine if a String can be parsed into a Long.
     *
     * @param self a String
     * @return true if the string can be parsed
     */
    public static boolean isLong(String self) {
        try {
            Long.valueOf(self.trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Determine if a String can be parsed into a Float.
     *
     * @param self a String
     * @return true if the string can be parsed
     */
    public static boolean isFloat(String self) {
        try {
            Float.valueOf(self.trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Determine if a String can be parsed into a Double.
     *
     * @param self a String
     * @return true if the string can be parsed
     */
    public static boolean isDouble(String self) {
        try {
            Double.valueOf(self.trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Determine if a String can be parsed into a BigInteger.
     *
     * @param self a String
     * @return true if the string can be parsed
     */
    public static boolean isBigInteger(String self) {
        try {
            new BigInteger(self.trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Determine if a String can be parsed into a BigDecimal.
     *
     * @param self a String
     * @return true if the string can be parsed
     */
    public static boolean isBigDecimal(String self) {
        try {
            new BigDecimal(self.trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Determine if a String can be parsed into a Number.
     * Synonym for 'isBigDecimal()'.
     *
     * @param self a String
     * @return true if the string can be parsed
     * @see #isBigDecimal(String)
     */
    public static boolean isNumber(String self) {
        return isBigDecimal(self);
    }

    /**
     * Determine if a Character is uppercase.
     * Synonym for 'Character.isUpperCase(this)'.
     *
     * @param self a Character
     * @return true if the character is uppercase
     * @see java.lang.Character#isUpperCase(char)
     */
    public static boolean isUpperCase(Character self) {
        return Character.isUpperCase(self.charValue());
    }

    /**
     * Determine if a Character is lowercase.
     * Synonym for 'Character.isLowerCase(this)'.
     *
     * @param self a Character
     * @return true if the character is lowercase
     * @see java.lang.Character#isLowerCase(char)
     */
    public static boolean isLowerCase(Character self) {
        return Character.isLowerCase(self.charValue());
    }

    /**
     * Determines if a character is a letter.
     * Synonym for 'Character.isLetter(this)'.
     *
     * @param self a Character
     * @return true if the character is a letter
     * @see java.lang.Character#isLetter(char)
     */
    public static boolean isLetter(Character self) {
        return Character.isLetter(self.charValue());
    }

    /**
     * Determines if a character is a digit.
     * Synonym for 'Character.isDigit(this)'.
     *
     * @param self a Character
     * @return true if the character is a digit
     * @see java.lang.Character#isDigit(char)
     */
    public static boolean isDigit(Character self) {
        return Character.isDigit(self.charValue());
    }

    /**
     * Determines if a character is a letter or digit.
     * Synonym for 'Character.isLetterOrDigit(this)'.
     *
     * @param self a Character
     * @return true if the character is a letter or digit
     * @see java.lang.Character#isLetterOrDigit(char)
     */
    public static boolean isLetterOrDigit(Character self) {
        return Character.isLetterOrDigit(self.charValue());
    }

    /**
     * Determines if a character is a whitespace character.
     * Synonym for 'Character.isWhitespace(this)'.
     *
     * @param self a Character
     * @return true if the character is a whitespace character
     * @see java.lang.Character#isWhitespace(char)
     */
    public static boolean isWhitespace(Character self) {
        return Character.isWhitespace(self.charValue());
    }

    /**
     * Converts the character to uppercase.
     * Synonym for 'Character.toUpperCase(this)'.
     *
     * @param self a Character to convert
     * @return  the uppercase equivalent of the character, if any;
     *          otherwise, the character itself.
     * @see     java.lang.Character#isUpperCase(char)
     * @see     java.lang.String#toUpperCase()
     */
    public static char toUpperCase(Character self) {
        return Character.toUpperCase(self.charValue());
    }

    /**
     * Converts the character to lowercase.
     * Synonym for 'Character.toLowerCase(this)'.
     *
     * @param self a Character to convert
     * @return  the lowercase equivalent of the character, if any;
     *          otherwise, the character itself.
     * @see     java.lang.Character#isLowerCase(char)
     * @see     java.lang.String#toLowerCase()
     */
    public static char toLowerCase(Character self) {
        return Character.toLowerCase(self.charValue());
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

    /**
     * Transform this number to a the given type, using the 'as' operator.  The
     * following types are supported in addition to the default
     * {@link #asType(Object,Class)}:
     * <ul>
     *  <li>BigDecimal</li>
     *  <li>BigInteger</li>
     *  <li>Double</li>
     *  <li>Float</li>
     * </ul>
     * @param self this number
     * @param c the desired type of the transformed result
     * @return an instance of the given type
     */
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
     * Transform this Number into a BigInteger.
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
     * Increment a Date by one day.
     *
     * @param self a Date
     * @return the next days date
     */
    public static Date next(Date self) {
        return plus(self, 1);
    }

    /**
     * Increment a java.sql.Date by one day.
     *
     * @param self a java.sql.Date
     * @return the next days date
     */
    public static java.sql.Date next(java.sql.Date self) {
        return new java.sql.Date(next((Date) self).getTime());
    }

    /**
     * Decrement a Date by one day.
     *
     * @param self a Date
     * @return the previous days date
     */
    public static Date previous(Date self) {
        return minus(self, 1);
    }

    /**
     * Decrement a java.sql.Date by one day.
     *
     * @param self a java.sql.Date
     * @return the previous days date
     */
    public static java.sql.Date previous(java.sql.Date self) {
        return new java.sql.Date(previous((Date) self).getTime());
    }

    /**
     * Add a number of days to this date and returns the new date.
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
     * Add a number of days to this date and returns the new date.
     *
     * @param self a java.sql.Date
     * @param days the number of days to increase
     * @return the new date
     */
    public static java.sql.Date plus(java.sql.Date self, int days) {
        return new java.sql.Date(plus((Date) self, days).getTime());
    }

    /**
     * Subtract a number of days from this date and returns the new date.
     *
     * @param self a Date
     * @param days the number of days to subtract
     * @return the new date
     */
    public static Date minus(Date self, int days) {
        return plus(self, -days);
    }

    /**
     * Subtract a number of days from this date and returns the new date.
     *
     * @param self a java.sql.Date
     * @param days the number of days to subtract
     * @return the new date
     */
    public static java.sql.Date minus(java.sql.Date self, int days) {
        return new java.sql.Date(minus((Date) self, days).getTime());
    }

    /**
     * Subtract another date from this one and return the number of days of the difference.
     *
     * Date self = Date then + (Date self - Date then)
     *
     * IOW, if self is before then the result is a negative value.
     *
     * @param self a Calendar
     * @param then another Calendar
     * @return number of days
     */

    public static int minus(Calendar self, Calendar then) {
        Calendar a = self;
        Calendar b = then;

        boolean swap = a.before(b);

        if (swap) {
           Calendar t = a;
           a = b;
           b = t;
        }

        int days = 0;

        b = (Calendar) b.clone();

        while (a.get(Calendar.YEAR) > b.get(Calendar.YEAR)) {
           days += 1 + (b.getActualMaximum(Calendar.DAY_OF_YEAR) - b.get(Calendar.DAY_OF_YEAR));
           b.set(Calendar.DAY_OF_YEAR, 1);
           b.add(Calendar.YEAR, 1);
        }

        days += a.get(Calendar.DAY_OF_YEAR) - b.get(Calendar.DAY_OF_YEAR);

        if (swap) days = -days;

        return days;
    }

    /**
     * Subtract another Date from this one and return the number of days of the difference.
     *
     * Date self = Date then + (Date self - Date then)
     *
     * IOW, if self is before then the result is a negative value.
     *
     * @param self a Date
     * @param then another Date
     * @return number of days
     */
    public static int minus(Date self, Date then) {
        Calendar a = (Calendar) Calendar.getInstance().clone();
        a.setTime(self);
        Calendar b = (Calendar) Calendar.getInstance().clone();
        b.setTime(then);
        return minus(a, b);
    }

    /**
     * <p>Create a String representation of this date according to the given
     * pattern.</p>
     *
     * <p>For example, if the system timezone is GMT,
     * <code>new Date(0).format('MM/dd/yy')</code> would return the string
     * <code>"01/01/70"</code>. See documentation for {@link SimpleDateFormat}
     * for format pattern use.</p>
     *
     * <p>Note that a new DateFormat instance is created for every
     * invocation of this method (for thread safety).</p>
     *
     * @see SimpleDateFormat
     * @param self
     * @param format the format pattern to use according to {@link SimpleDateFormat}
     * @return a string representation of this date.
     */
    public static String format( Date self, String format ) {
    	return new SimpleDateFormat( format ).format( self );
    }

    /**
     * <p>Return a string representation of the 'day' portion of this date
     * according to the locale-specific {@link DateFormat#SHORT} default format.
     * For an "en_UK" system locale, this would be <code>dd/MM/yy</code>.</p>
     *
     * <p>Note that a new DateFormat instance is created for every
     * invocation of this method (for thread safety).</p>
     *
     * @see DateFormat#getDateInstance(int)
     * @see DateFormat#SHORT
     * @param self
     * @return a string representation of this date
     */
    public static String getDateString( Date self ) {
    	return DateFormat.getDateInstance(DateFormat.SHORT).format( self );
    }

    /**
     * <p>Return a string representation of the time portion of this date
     * according to the locale-specific {@link DateFormat#MEDIUM} default format.
     * For an "en_UK" system locale, this would be <code>HH:MM:ss</code>.</p>
     *
     * <p>Note that a new DateFormat instance is created for every
     * invocation of this method (for thread safety).</p>
     *
     * @see DateFormat#getTimeInstance(int)
     * @see DateFormat#MEDIUM
     * @param self
     * @return a string representing the time portion of this date
     */
    public static String getTimeString( Date self ) {
    	return DateFormat.getTimeInstance(DateFormat.MEDIUM).format( self );
    }

    /**
     * <p>Return a string representation of the date and time time portion of
     * this Date instance, according to the locale-specific format used by
     * {@link DateFormat}.  This method uses the {@link DateFormat#SHORT}
     * preset for the day portion and {@link DateFormat#MEDIUM} for the time
     * portion of the output string.</p>
     *
     * <p>Note that a new DateFormat instance is created for every
     * invocation of this method (for thread safety).</p>
     *
     * @see DateFormat#getDateTimeInstance(int, int)
     * @param self
     * @return a string representation of this date and time
     */
    public static String getDateTimeString( Date self ) {
    	return DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.MEDIUM).format( self );
    }

    // Boolean based methods
    //-------------------------------------------------------------------------

    public static Boolean and(Boolean left, Boolean right) {
        return Boolean.valueOf(left.booleanValue() && right.booleanValue());
    }

    public static Boolean or(Boolean left, Boolean right) {
        return Boolean.valueOf(left.booleanValue() || right.booleanValue());
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
     * Create an object input stream for this file.
     *
     * @param file a file
     * @return an object input stream
     * @throws IOException if an IOException occurs.
     */
    public static ObjectInputStream newObjectInputStream(File file) throws IOException {
        return new ObjectInputStream(new FileInputStream(file));
    }

    /**
     * Create an object output stream for this file.
     *
     * @param file a file
     * @return an object output stream
     * @throws IOException if an IOException occurs.
     */
    public static ObjectOutputStream newObjectOutputStream(File file) throws IOException {
        return new ObjectOutputStream(new FileOutputStream(file));
    }

    /**
     * Iterates through the given file object by object.
     *
     * @param self    a File
     * @param closure a closure
     * @throws IOException            if an IOException occurs.
     * @throws ClassNotFoundException if the class  is not found.
     * @see #eachObject(ObjectInputStream,Closure)
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
     * @throws IOException            if an IOException occurs.
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
     * Create a new ObjectInputStream for this file and pass it to the closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param file    a File
     * @param closure a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withStream(InputStream,Closure)
     */
    public static Object withObjectInputStream(File file, Closure closure) throws IOException {
        return withStream(newObjectInputStream(file), closure);
    }

    /**
     * Create a new ObjectOutputStream for this file and then pass it to the
     * closure.  This method ensures the stream is closed after the closure
     * returns.
     *
     * @param file    a File
     * @param closure a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withStream(OutputStream,Closure)
     */
    public static Object withObjectOutputStream(File file, Closure closure) throws IOException {
        return withStream(newObjectOutputStream(file), closure);
    }

    /**
     * Iterates through this String line by line.  Each line is passed
     * to the given 1 or 2 arg closure. If a 2 arg closure is found
     * the line count is passed as the second argument.
     *
     * @param self    a String
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     * @see #eachLine(String, int, groovy.lang.Closure)
     */
    public static Object eachLine(String self, Closure closure) throws IOException {
        return eachLine(self, 0, closure);
    }

    /**
     * Iterates through this String line by line.  Each line is passed
     * to the given 1 or 2 arg closure. If a 2 arg closure is found
     * the line count is passed as the second argument.
     *
     * @param self    a String
     * @param firstLine the count of the first line
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     * @since 1.5.7
     */
    public static Object eachLine(String self, int firstLine, Closure closure) throws IOException {
        int count = firstLine;
        String line = null;
        List list = readLines(self);
        for (int i = 0; i < list.size(); i++) {
            line = (String) list.get(i);
            callClosureForLine(closure, line, count);
            count++;
        }
        return line;
    }

    /**
     * Iterates through this file line by line.  Each line is passed to the
     * given 1 or 2 arg closure.  The file reader is closed before this method
     * returns.
     *
     * @param self    a File
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #eachLine(java.io.File, int, groovy.lang.Closure)
     */
    public static Object eachLine(File self, Closure closure) throws IOException {
        return eachLine(self, 1, closure);
    }

    /**
     * Iterates through this file line by line.  Each line is passed
     * to the given 1 or 2 arg closure.  The file reader is closed
     * before this method returns.
     *
     * @param self    a File
     * @param firstLine the count of the first line
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #eachLine(java.io.Reader, int, groovy.lang.Closure)
     */
    public static Object eachLine(File self, int firstLine, Closure closure) throws IOException {
        return eachLine(newReader(self), firstLine, closure);
    }

    /**
     * Iterates through this stream reading with the provided charset, passing each line to the
     * given 1 or 2 arg closure.  The stream is closed before this method returns.
     *
     * @param stream  a stream
     * @param charset opens the stream with a specified charset
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #eachLine(java.io.InputStream, String, int, groovy.lang.Closure)
     */
    public static Object eachLine(InputStream stream, String charset, Closure closure) throws IOException {
        return eachLine(stream, charset, 1, closure);
    }

    /**
     * Iterates through this stream reading with the provided charset, passing each line to
     * the given 1 or 2 arg closure.  The stream is closed after this method returns.
     *
     * @param stream    a stream
     * @param charset   opens the stream with a specified charset
     * @param firstLine the count of the first line
     * @param closure   a closure
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #eachLine(Reader,Closure)
     */
    public static Object eachLine(InputStream stream, String charset, int firstLine, Closure closure) throws IOException {
        return eachLine(new InputStreamReader(stream, charset), firstLine, closure);
    }

    /**
     * Iterates through this stream, passing each line to the given 1 or 2 arg closure.
     * The stream is closed before this method returns.
     *
     * @param stream  a stream
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #eachLine(java.io.InputStream, int, groovy.lang.Closure)
     */
    public static Object eachLine(InputStream stream, Closure closure) throws IOException {
        return eachLine(stream, 1, closure);
    }

    /**
     * Iterates through this stream, passing each line to the given 1 or 2 arg closure.
     * The stream is closed before this method returns.
     *
     * @param stream  a stream
     * @param firstLine the count of the first line
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #eachLine(java.io.Reader, int, groovy.lang.Closure)
     */
    public static Object eachLine(InputStream stream, int firstLine, Closure closure) throws IOException {
        return eachLine(new InputStreamReader(stream), firstLine, closure);
    }

    /**
     * Iterates through the lines read from the URL's associated input stream passing each
     * line to the given 1 or 2 arg closure. The stream is closed before this method returns.
     *
     * @param url     a URL to open and read
     * @param closure a closure to apply on each line
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #eachLine(java.net.URL, int, groovy.lang.Closure)
     */
    public static Object eachLine(URL url, Closure closure) throws IOException {
        return eachLine(url, 1, closure);
    }

    /**
     * Iterates through the lines read from the URL's associated input stream passing each
     * line to the given 1 or 2 arg closure. The stream is closed before this method returns.
     *
     * @param url       a URL to open and read
     * @param firstLine the count of the first line
     * @param closure   a closure to apply on each line
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #eachLine(java.io.InputStream, int, groovy.lang.Closure)
     */
    public static Object eachLine(URL url, int firstLine, Closure closure) throws IOException {
        return eachLine(url.openConnection().getInputStream(), firstLine, closure);
    }

    /**
     * Iterates through the lines read from the URL's associated input stream passing each
     * line to the given 1 or 2 arg closure. The stream is closed before this method returns.
     *
     * @param url     a URL to open and read
     * @param charset opens the stream with a specified charset
     * @param closure a closure to apply on each line
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #eachLine(java.net.URL, String, int, groovy.lang.Closure)
     */
    public static Object eachLine(URL url, String charset, Closure closure) throws IOException {
        return eachLine(url, charset, 1, closure);
    }

    /**
     * Iterates through the lines read from the URL's associated input stream passing each
     * line to the given 1 or 2 arg closure. The stream is closed before this method returns.
     *
     * @param url       a URL to open and read
     * @param charset   opens the stream with a specified charset
     * @param firstLine the count of the first line
     * @param closure   a closure to apply on each line
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #eachLine(java.io.Reader, int, groovy.lang.Closure)
     */
    public static Object eachLine(URL url, String charset, int firstLine, Closure closure) throws IOException {
        return eachLine(new InputStreamReader(url.openConnection().getInputStream(), charset), firstLine, closure);
    }

    /**
     * Iterates through the given reader line by line.  Each line is passed to the
     * given 1 or 2 arg closure. If the closure has two arguments, the line count is passed
     * as the second argument. The Reader is closed before this method returns.
     *
     * @param self    a Reader, closed after the method returns
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     */
    public static Object eachLine(Reader self, Closure closure) throws IOException {
        return eachLine(self, 1, closure);
    }

    /**
     * Iterates through the given reader line by line.  Each line is passed to the
     * given 1 or 2 arg closure. If the closure has two arguments, the line count is passed
     * as the second argument. The Reader is closed before this method returns.
     *
     * @param self      a Reader, closed after the method returns
     * @param firstLine the count of the first line
     * @param closure   a closure which will be passed each line (or for 2 argument closures the line and count)
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object eachLine(Reader self, int firstLine, Closure closure) throws IOException {
        BufferedReader br;
        int count = firstLine;
        Object result = null;

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
                    result = callClosureForLine(closure, line, count);
                    count++;
                }
            }
            Reader temp = self;
            self = null;
            temp.close();
            return result;
        } finally {
            closeReaderWithWarning(self);
            closeReaderWithWarning(br);
        }
    }

    /**
     * Iterates through this file line by line, splitting on the seperator.
     * The list of tokens for each line is then passed to the given closure.
     * Finally the resources used for processing the file are closed.
     *
     * @param self    a File
     * @param sep     a String separator
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #splitEachLine(Reader,String,Closure)
     */
    public static Object splitEachLine(File self, String sep, Closure closure) throws IOException {
        return splitEachLine(newReader(self), sep, closure);
    }

    /**
     * Iterates through the given reader line by line, splitting each line using
     * the given separator.  The list of tokens for each line is then passed to
     * the given closure.  The Reader is closed afterwards.
     *
     * @param self    a Reader, closed after the method returns
     * @param sep     a String separator
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see String#split(String)
     */
    public static Object splitEachLine(Reader self, String sep, Closure closure) throws IOException {
        BufferedReader br;
        Object result = null;

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
                    result = closure.call(vals);
                }
            }
            Reader temp = self;
            self = null;
            temp.close();
            return result;
        } finally {
            closeReaderWithWarning(self);
            closeReaderWithWarning(br);
        }
    }

    /**
     * Iterates through the given InputStream line by line using the specified
     * encoding, splitting each line using the given separator.  The list of tokens
     * for each line is then passed to the given closure. Finally, the stream
     * is closed.
     *
     * @param stream  an InputStream
     * @param sep     a String separator
     * @param charset opens the stream with a specified charset
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #splitEachLine(Reader,String,Closure)
     */
    public static Object splitEachLine(InputStream stream, String sep, String charset, Closure closure) throws IOException {
        return splitEachLine(new BufferedReader(new InputStreamReader(stream, charset)), sep, closure);
    }

    /**
     * Iterates through the given InputStream line by line, splitting each line using
     * the given separator.  The list of tokens for each line is then passed to
     * the given closure. The stream is closed before the method returns.
     *
     * @param stream  an InputStream
     * @param sep     a String separator
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #splitEachLine(Reader,String,Closure)
     */
    public static Object splitEachLine(InputStream stream, String sep, Closure closure) throws IOException {
        return splitEachLine(new BufferedReader(new InputStreamReader(stream)), sep, closure);
    }

    /**
     * Iterates through the given String line by line, splitting each line using
     * the given separator.  The list of tokens for each line is then passed to
     * the given closure.
     *
     * @param self    a String
     * @param sep     a String separator
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     * @see String#split(String)
     */
    public static Object splitEachLine(String self, String sep, Closure closure) throws IOException {
        final List list = readLines(self);
        Object result = null;
        for (int i = 0; i < list.size(); i++) {
            String line = (String) list.get(i);
            List vals = Arrays.asList(line.split(sep));
            result = closure.call(vals);
        }
        return result;
    }

    /**
     * Read a single, whole line from the given Reader.
     *
     * @param self a Reader
     * @return a line
     * @throws IOException if an IOException occurs.
     */
    public static String readLine(Reader self) throws IOException {
        if (self instanceof BufferedReader) {
            BufferedReader br = (BufferedReader) self;
            return br.readLine();
        }
        if (self.markSupported()) {
            return readLineFromReaderWithMark(self);
        }
        return readLineFromReaderWithoutMark(self);
    }

    private static int charBufferSize = 4096;     // half the default stream buffer size
    private static int expectedLineLength = 160;  // double the default line length
    private static int EOF = -1;                  // End Of File

    /*
    * This method tries to read subsequent buffers from the reader using a mark
    */
    private static String readLineFromReaderWithMark(final Reader input)
            throws IOException {
        char[] cbuf = new char[charBufferSize];
        try {
            input.mark(charBufferSize);
        } catch (IOException e) {
            // this should never happen
            LOG.warning("Caught exception setting mark on supporting reader: " + e);
            // fallback
            return readLineFromReaderWithoutMark(input);
        }

        // could be changed into do..while, but then
        // we might create an additional StringBuffer
        // instance at the end of the stream
        int count = input.read(cbuf);
        if (count == EOF) // we are at the end of the input data
            return null;

        StringBuffer line = new StringBuffer(expectedLineLength);
        // now work on the buffer(s)
        int ls = lineSeparatorIndex(cbuf, count);
        while (ls == -1) {
            line.append(cbuf, 0, count);
            count = input.read(cbuf);
            if (count == EOF) {
                // we are at the end of the input data
                return line.toString();
            }
            ls = lineSeparatorIndex(cbuf, count);
        }
        line.append(cbuf, 0, ls);

        // correct ls if we have \r\n
        int skipLS = 1;
        if (ls + 1 < count) {
            // we are not at the end of the buffer
            if (cbuf[ls] == '\r' && cbuf[ls + 1] == '\n') {
                skipLS++;
            }
        } else {
            if (cbuf[ls] == '\r' && input.read() == '\n') {
                skipLS++;
            }
        }

        //reset() and skip over last linesep
        input.reset();
        input.skip(line.length() + skipLS);
        return line.toString();
    }

    /*
    * This method reads without a buffer.
    * It returns too many empty lines if \r\n combinations
    * are used. Nothing can be done because we can't push
    * back the character we have just read.
    */
    private static String readLineFromReaderWithoutMark(Reader input)
            throws IOException {

        int c = input.read();
        if (c == -1)
            return null;
        StringBuffer line = new StringBuffer(expectedLineLength);

        while (c != EOF && c != '\n' && c != '\r') {
            char ch = (char) c;
            line.append(ch);
            c = input.read();
        }
        return line.toString();
    }

    /*
     * searches for \n or \r
     * Returns -1 if not found.
     */
    private static int lineSeparatorIndex(char[] array, int length) {
        for (int k = 0; k < length; k++) {
            if (isLineSeparator(array[k])) {
                return k;
            }
        }
        return -1;
    }

    /*
    * true if either \n or \r
    */
    private static boolean isLineSeparator(char c) {
        return c == '\n' || c == '\r';
    }

    /**
     * Just throws a DeprecationException.  DO NOT USE.  It used to read a single, whole line from the given InputStream.
     *
     * @param stream an InputStream
     * @return a line
     * @throws IOException if an IOException occurs.
     * @deprecated use Reader#readLine instead please
     */
    public static String readLine(InputStream stream) throws IOException {
        throw new DeprecationException(
                "readLine() on InputStream is no longer supported. " +
                        "Either use a Reader or encapsulate the InputStream" +
                        " with a BufferedReader and an InputStreamReader."
        );
    }

    /**
     * Return the lines of a String as a List of Strings.
     *
     * @param self a String object
     * @return a list of lines
     * @throws java.io.IOException if an error occurs
     * @since 1.5.5
     */
    public static List readLines(String self) throws IOException {
        return readLines(new StringReader(self));
    }

    static String lineSeparator = null;
    
    /**
     * Return a String with lines (separated by LF, CR/LF, or CR)
     * terminated by the platform specific line separator. 
     *
     * @param self a String object
     * @return the denormalized string
     * @since 1.6.0
     */
    public static String denormalize(String self) {
        if (lineSeparator == null) {
            final StringWriter sw = new StringWriter(2);
            try {
                final BufferedWriter bw = new BufferedWriter(sw);
                bw.newLine();
                bw.flush();
                lineSeparator = sw.toString();
            } catch (IOException ioe) {
                lineSeparator = "\n";
            }
        }
        
        self = normalize(self);
        
        if (!lineSeparator.equals("\n")) {
            self = self.replace("\n", lineSeparator);
        }
        
        return self;
    }

    /**
     * Return a String with linefeeds and carriage returns normalized to linefeeds.
     *
     * @param self a String object
     * @return the normalized string
     * @since 1.5.8
     */
    public static String normalize(String self) {
        if (self.contains("\r")) {
            self = self.replace("\r\n", "\n");
            self = self.replace('\r', '\n');
        }
        
        return self;
    }

    /**
     * Reads the file into a list of Strings, with one item for each line.
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
     * Read the content of the File using the specified encoding and return it
     * as a String.
     *
     * @param file    the file whose content we want to read
     * @param charset the charset used to read the content of the file
     * @return a String containing the content of the file
     * @throws IOException if an IOException occurs.
     */
    public static String getText(File file, String charset) throws IOException {
        return getText(newReader(file, charset));
    }

    /**
     * Read the content of the File and returns it as a String.
     *
     * @param file the file whose content we want to read
     * @return a String containing the content of the file
     * @throws IOException if an IOException occurs.
     */
    public static String getText(File file) throws IOException {
        return getText(newReader(file));
    }

    /**
     * Read the content of this URL and returns it as a String.
     *
     * @param url URL to read content from
     * @return the text from that URL
     * @throws IOException if an IOException occurs.
     */
    public static String getText(URL url) throws IOException {
        return getText(url, CharsetToolkit.getDefaultSystemCharset().toString());
    }

    /**
     * Read the data from this URL and return it as a String.  The connection
     * stream is closed before this method returns.
     *
     * @param url     URL to read content from
     * @param charset opens the stream with a specified charset
     * @return the text from that URL
     * @throws IOException if an IOException occurs.
     * @see URLConnection#getInputStream()
     */
    public static String getText(URL url, String charset) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), charset));
        return getText(reader);
    }

    /**
     * Read the content of this InputStream and return it as a String.
     * The stream is closed before this method returns.
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
     * Read the content of this InputStream using specified charset and return
     * it as a String.  The stream is closed before this method returns.
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
     * Read the content of the Reader and return it as a String.  The reader
     * is closed before this method returns.
     *
     * @param reader a Reader whose content we want to read
     * @return a String containing the content of the buffered reader
     * @throws IOException if an IOException occurs.
     * @see #getText(BufferedReader)
     */
    public static String getText(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        return getText(bufferedReader);
    }

    /**
     * Read the content of the BufferedReader and return it as a String.
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
     * Write the text and append a newline (using the platform's line-ending).
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
     * Synonym for write(text) allowing file.text = 'foo'.
     *
     * @param file a File
     * @param text the text to write to the File
     * @throws IOException if an IOException occurs.
     * @see #write(File, String)
     */
    public static void setText(File file, String text) throws IOException {
        write(file, text);
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
     * Write bytes to a File.
     *
     * @param file a File
     * @param bytes the byte array to append to the end of the File
     * @return the original file
     * @throws IOException if an IOException occurs.
     */
    public static File leftShift(File file, byte[] bytes) throws IOException {
        append(file, bytes);
        return file;
    }

    /**
     * Write the text to the File, using the specified encoding.
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
     * Append the text at the end of the File.
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
     * Append bytes to the end of a File.
     *
     * @param file a File
     * @param bytes the byte array to append to the end of the File
     * @throws IOException if an IOException occurs.
     */
    public static void append(File file, byte[] bytes) throws IOException {
        BufferedOutputStream stream = null;
        try {
            stream = newOutputStream(file);
            stream.write(bytes, 0, bytes.length);
            stream.flush();

            OutputStream temp = stream;
            stream = null;
            temp.close();
        } finally {
            closeOutputStreamWithWarning(stream);
        }
    }

    /**
     * Append the text at the end of the File, using a specified encoding.
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
     * Reads the reader into a list of Strings, with one entry for each line.
     * The reader is closed before this method returns.
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
     * Common code for {@link #eachFile(File,Closure)} and {@link #eachDir(File,Closure)}
     *
     * @param self    a file object
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
        if (files == null) return;
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
     * @see File#listFiles()
     */
    public static void eachFile(final File self, final Closure closure) throws FileNotFoundException, IllegalArgumentException {
        eachFile(self, closure, false);
    }

    /**
     * Invokes the closure for each directory in this directory,
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
     * Common code for {@link #eachFileRecurse(File,Closure)} and {@link #eachDirRecurse(File,Closure)}
     *
     * @param self    a file object
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
        if (files == null) return;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                closure.call(files[i]);
                eachFileRecurse(files[i], closure, onlyDir);
            } else if (!onlyDir) {
                closure.call(files[i]);
            }
        }
    }

    /**
     * Invokes the closure for each descendant file in this directory.
     * Sub-directories are recursively searched in a depth-first fashion.
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
     * Invokes the closure for each descendant directory of this directory.
     * Sub-directories are recursively searched in a depth-first fashion.
     * Only directories are passed to the closure; regular files are ignored.
     *
     * @param self    a directory
     * @param closure a closure
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     * @since 1.1 beta 1
     * @see #eachFileRecurse(File,Closure,boolean)
     */
    public static void eachDirRecurse(final File self, final Closure closure) throws FileNotFoundException, IllegalArgumentException {
        eachFileRecurse(self, closure, true);
    }

    /**
     * Common code for {@link #eachFileMatch(File,Object,Closure)} and {@link #eachDirMatch(File,Object,Closure)}
     *
     * @param self    a file
     * @param filter  the filter to perform on the file/directory (using the isCase(object) method)
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
        if (files == null) return;
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
     * Invokes the closure for each file whose name (file.name) matches the given filter in the given directory
     * - calling the isCase() method to determine if a match occurs.  This method can be used
     * with different kinds of filters like regular expressions, classes, ranges etc.
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
     * Invokes the closure for each directory whose name (dir.name) matches the given filter in the given directory
     * - calling the isCase() method to determine if a match occurs.  This method can be used
     * with different kinds of filters like regular expressions, classes, ranges etc.
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
     * Deletes a directory with all contained files and subdirectories.
     * <p>The method returns
     * <ul>
     * <li>true, when deletion was successful</li>
     * <li>true, when it is called for a non existing directory</li>
     * <li>false, when it is called for a file which isn't a directory</li>
     * <li>false, when directory couldn't be deleted</li>
     * </ul>
     * </p>
     *
     * @return true if deletion was successful
     */
    public static boolean deleteDir(final File self) {
        if (!self.exists())
            return true;
        if (!self.isDirectory())
            return false;

        File[] files = self.listFiles();
        if (files == null)
            // couldn't access files
            return false;

        // delete contained files
        boolean result = true;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                if(!deleteDir(file))
                    result = false;
            }
            else {
                if (!file.delete())
                    result = false;
            }
        }

        // now delete directory itself
        if(!self.delete())
            result = false;

        return result;
    }

    /**
     * Allows a simple syntax for using timers.  This timer will execute the
     * given closure after the given delay.
     *
     * @param timer   a timer object
     * @param delay   the delay in milliseconds before running the closure code
     * @param closure the closure to invoke
     * @return The timer task which has been scheduled.
     */
    public static TimerTask runAfter(Timer timer, int delay, final Closure closure) {
        TimerTask timerTask = new TimerTask() {
            public void run() {
                closure.call();
            }
        };
        timer.schedule(timerTask, delay);
        return timerTask;
    }

    /**
     * Create a buffered reader for this file.
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
     * Create a buffered reader for this file, using the specified
     * charset as the encoding.
     *
     * @param file    a File
     * @param charset the charset for this File
     * @return a BufferedReader
     * @throws FileNotFoundException        if the File was not found
     * @throws UnsupportedEncodingException if the encoding specified is not supported
     */
    public static BufferedReader newReader(File file, String charset)
            throws FileNotFoundException, UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
    }

    /**
     * Creates a reader for this input stream.
     *
     * @param self an input stream
     * @return a reader
     */
    public static BufferedReader newReader(InputStream self) {
        return new BufferedReader(new InputStreamReader(self));
    }

    /**
     * Creates a reader for this input stream, using the specified
     * charset as the encoding.
     *
     * @param self an input stream
     * @param charset the charset for this input stream
     * @return a reader
     * @throws UnsupportedEncodingException if the encoding specified is not supported
     */
    public static BufferedReader newReader(InputStream self, String charset) throws UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(self, charset));
    }

    /**
     * Create a new BufferedReader for this file and then
     * passes it into the closure, ensuring the reader is closed after the
     * closure returns.
     *
     * @param file    a file object
     * @param closure a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object withReader(File file, Closure closure) throws IOException {
        return withReader(newReader(file), closure);
    }

    /**
     * Create a new BufferedReader for this file using the specified charset and then
     * passes it into the closure, ensuring the reader is closed after the
     * closure returns.
     *
     * @param file    a file object
     * @param charset the charset for this input stream
     * @param closure a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object withReader(File file, String charset, Closure closure) throws IOException {
        return withReader(newReader(file, charset), closure);
    }

    /**
     * Create a buffered output stream for this file.
     *
     * @param file a file object
     * @return the created OutputStream
     * @throws IOException if an IOException occurs.
     */
    public static BufferedOutputStream newOutputStream(File file) throws IOException {
        return new BufferedOutputStream(new FileOutputStream(file));
    }

    /**
     * Creates a new data output stream for this file.
     *
     * @param file a file object
     * @return the created DataOutputStream
     * @throws IOException if an IOException occurs.
     */
    public static DataOutputStream newDataOutputStream(File file) throws IOException {
        return new DataOutputStream(new FileOutputStream(file));
    }

    /**
     * Creates a new OutputStream for this file and passes it into the closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param file    a File
     * @param closure a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withStream(OutputStream,Closure)
     */
    public static Object withOutputStream(File file, Closure closure) throws IOException {
        return withStream(newOutputStream(file), closure);
    }

    /**
     * Create a new InputStream for this file and passes it into the closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param file    a File
     * @param closure a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withStream(InputStream,Closure)
     */
    public static Object withInputStream(File file, Closure closure) throws IOException {
        return withStream(newInputStream(file), closure);
    }

    /**
     * Creates a new InputStream for this URL and passes it into the closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param url     a URL
     * @param closure a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withStream(InputStream,Closure)
     */
    public static Object withInputStream(URL url, Closure closure) throws IOException {
        return withStream(newInputStream(url), closure);
    }

    /**
     * Create a new DataOutputStream for this file and passes it into the closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param file    a File
     * @param closure a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withStream(OutputStream,Closure)
     */
    public static Object withDataOutputStream(File file, Closure closure) throws IOException {
        return withStream(newDataOutputStream(file), closure);
    }

    /**
     * Create a new DataInputStream for this file and passes it into the closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param file    a File
     * @param closure a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withStream(InputStream,Closure)
     */
    public static Object withDataInputStream(File file, Closure closure) throws IOException {
        return withStream(newDataInputStream(file), closure);
    }

    /**
     * Create a buffered writer for this file.
     *
     * @param file a File
     * @return a BufferedWriter
     * @throws IOException if an IOException occurs.
     */
    public static BufferedWriter newWriter(File file) throws IOException {
        return new BufferedWriter(new FileWriter(file));
    }

    /**
     * Creates a buffered writer for this file, optionally appending to the
     * existing file content.
     *
     * @param file   a File
     * @param append true if data should be appended to the file
     * @return a BufferedWriter
     * @throws IOException if an IOException occurs.
     */
    public static BufferedWriter newWriter(File file, boolean append) throws IOException {
        return new BufferedWriter(new FileWriter(file, append));
    }

    /**
     * Helper method to create a buffered writer for a file.  If the given
     * charset is "UTF-16BE" or "UTF-16LE", the requisite byte order mark is
     * written to the stream before the writer is returned.
     *
     * @param file    a File
     * @param charset the name of the encoding used to write in this file
     * @param append  true if in append mode
     * @return a BufferedWriter
     * @throws IOException if an IOException occurs.
     */
    public static BufferedWriter newWriter(File file, String charset, boolean append) throws IOException {
        if (append) {
            return new EncodingAwareBufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), charset));
        } else {
            // first write the Byte Order Mark for Unicode encodings
            FileOutputStream stream = new FileOutputStream(file);
            if ("UTF-16BE".equals(charset)) {
                writeUtf16Bom(stream, true);
            } else if ("UTF-16LE".equals(charset)) {
                writeUtf16Bom(stream, false);
            }
            return new EncodingAwareBufferedWriter(new OutputStreamWriter(stream, charset));
        }
    }

    /**
     * Creates a buffered writer for this file, writing data using the given
     * encoding.
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
     * Creates a new BufferedWriter for this file, passes it to the closure, and
     * ensures the stream is flushed and closed after the closure returns.
     *
     * @param file    a File
     * @param closure a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object withWriter(File file, Closure closure) throws IOException {
        return withWriter(newWriter(file), closure);
    }

    /**
     * Creates a new BufferedWriter for this file, passes it to the closure, and
     * ensures the stream is flushed and closed after the closure returns.
     * The writer will use the given charset encoding.
     *
     * @param file    a File
     * @param charset the charset used
     * @param closure a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object withWriter(File file, String charset, Closure closure) throws IOException {
        return withWriter(newWriter(file, charset), closure);
    }

    /**
     * Create a new BufferedWriter which will append to this
     * file.  The writer is passed to the closure and will be closed before
     * this method returns.
     *
     * @param file    a File
     * @param charset the charset used
     * @param closure a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object withWriterAppend(File file, String charset, Closure closure) throws IOException {
        return withWriter(newWriter(file, charset, true), closure);
    }

    /**
     * Create a new BufferedWriter for this file in append mode.  The writer
     * is passed to the closure and is closed after the closure returns.
     *
     * @param file    a File
     * @param closure a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object withWriterAppend(File file, Closure closure) throws IOException {
        return withWriter(newWriter(file, true), closure);
    }

    /**
     * Create a new PrintWriter for this file.
     *
     * @param file a File
     * @return the created PrintWriter
     * @throws IOException if an IOException occurs.
     */
    public static PrintWriter newPrintWriter(File file) throws IOException {
        return new PrintWriter(newWriter(file));
    }

    /**
     * Create a new PrintWriter for this file, using specified
     * charset.
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
     * Create a new PrintWriter for this file which is then
     * passed it into the given closure.  This method ensures its the writer
     * is closed after the closure returns.
     *
     * @param file    a File
     * @param closure the closure to invoke with the PrintWriter
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object withPrintWriter(File file, Closure closure) throws IOException {
        return withWriter(newPrintWriter(file), closure);
    }

    /**
     * Create a new PrintWriter with a specified charset for
     * this file.  The writer is passed to the closure, and will be closed
     * before this method returns.
     *
     * @param file    a File
     * @param charset the charset
     * @param closure the closure to invoke with the PrintWriter
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object withPrintWriter(File file, String charset, Closure closure) throws IOException {
        return withWriter(newPrintWriter(file, charset), closure);
    }

    /**
     * Allows this writer to be used within the closure, ensuring that it
     * is flushed and closed before this method returns.
     *
     * @param writer  the writer which is used and then closed
     * @param closure the closure that the writer is passed into
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object withWriter(Writer writer, Closure closure) throws IOException {
        try {
            Object result = closure.call(writer);

            try {
                writer.flush();
            } catch (IOException e) {
                // try to continue even in case of error
            }
            Writer temp = writer;
            writer = null;
            temp.close();
            return result;
        } finally {
            closeWriterWithWarning(writer);
        }
    }

    /**
     * Allows this reader to be used within the closure, ensuring that it
     * is closed before this method returns.
     *
     * @param reader  the reader which is used and then closed
     * @param closure the closure that the writer is passed into
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object withReader(Reader reader, Closure closure) throws IOException {
        try {
            Object result = closure.call(reader);

            Reader temp = reader;
            reader = null;
            temp.close();

            return result;
        } finally {
            closeReaderWithWarning(reader);
        }
    }

    /**
     * Allows this input stream to be used within the closure, ensuring that it
     * is flushed and closed before this method returns.
     *
     * @param stream  the stream which is used and then closed
     * @param closure the closure that the stream is passed into
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object withStream(InputStream stream, Closure closure) throws IOException {
        try {
            Object result = closure.call(stream);

            InputStream temp = stream;
            stream = null;
            temp.close();

            return result;
        } finally {
            closeInputStreamWithWarning(stream);
        }
    }

    /**
     * Reads the stream into a list, with one element for each line.
     *
     * @param stream a stream
     * @return a List of lines
     * @throws IOException if an IOException occurs.
     * @see #readLines(Reader)
     */
    public static List readLines(InputStream stream) throws IOException {
        return readLines(new BufferedReader(new InputStreamReader(stream)));
    }

    /**
     * Helper method to create a new BufferedReader for a URL and then
     * passes it to the closure.  The reader is closed after the closure returns.
     *
     * @param url     a URL
     * @param closure the closure to invoke with the reader
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object withReader(URL url, Closure closure) throws IOException {
        return withReader(url.openConnection().getInputStream(), closure);
    }

    /**
     * Helper method to create a new Reader for a URL and then
     * passes it to the closure.  The reader is closed after the closure returns.
     *
     * @param url     a URL
     * @param charset the charset used
     * @param closure the closure to invoke with the reader
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object withReader(URL url, String charset, Closure closure) throws IOException {
        return withReader(url.openConnection().getInputStream(), charset, closure);
    }

    /**
     * Helper method to create a new Reader for a stream and then
     * passes it into the closure.  The reader (and this stream) is closed after
     * the closure returns.
     *
     * @see java.io.InputStreamReader
     * @param in      a stream
     * @param closure the closure to invoke with the InputStream
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object withReader(InputStream in, Closure closure) throws IOException {
        return withReader(new InputStreamReader(in), closure);
    }

    /**
     * Helper method to create a new Reader for a stream and then
     * passes it into the closure.  The reader (and this stream) is closed after
     * the closure returns.
     *
     * @see java.io.InputStreamReader
     * @param in      a stream
     * @param charset the charset used to decode the stream
     * @param closure the closure to invoke with the reader
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object withReader(InputStream in, String charset, Closure closure) throws IOException {
        return withReader(new InputStreamReader(in, charset), closure);
    }

    /**
     * Creates a writer from this stream, passing it to the given closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param stream  the stream which is used and then closed
     * @param closure the closure that the writer is passed into
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withWriter(Writer,Closure)
     */
    public static Object withWriter(OutputStream stream, Closure closure) throws IOException {
        return withWriter(new OutputStreamWriter(stream), closure);
    }

    /**
     * Creates a writer from this stream, passing it to the given closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param stream  the stream which is used and then closed
     * @param charset the charset used
     * @param closure the closure that the writer is passed into
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withWriter(Writer,Closure)
     */
    public static Object withWriter(OutputStream stream, String charset, Closure closure) throws IOException {
        return withWriter(new OutputStreamWriter(stream, charset), closure);
    }

    /**
     * Passes this OutputStream to the closure, ensuring that the stream
     * is closed after the closure returns, regardless of errors.
     *
     * @param os      the stream which is used and then closed
     * @param closure the closure that the stream is passed into
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object withStream(OutputStream os, Closure closure) throws IOException {
        try {
            Object result = closure.call(os);
            os.flush();

            OutputStream temp = os;
            os = null;
            temp.close();

            return result;
        } finally {
            closeOutputStreamWithWarning(os);
        }
    }

    /**
     * Creates a buffered input stream for this file.
     *
     * @param file a File
     * @return a BufferedInputStream of the file
     * @throws FileNotFoundException if the file is not found.
     */
    public static BufferedInputStream newInputStream(File file) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(file));
    }

    /**
     * Creates a buffered input stream for this URL.
     *
     * @param url a URL
     * @return a BufferedInputStream for the URL
     * @throws MalformedURLException is thrown if the URL is not well formed
     * @throws IOException if an I/O error occurs while creating the input stream
     */
    public static BufferedInputStream newInputStream(URL url) throws MalformedURLException, IOException {
        return new BufferedInputStream(url.openConnection().getInputStream());
    }

    /**
     * Creates a buffered reader for this URL.
     *
     * @param url a URL
     * @return a BufferedReader for the URL
     * @throws MalformedURLException is thrown if the URL is not well formed
     * @throws IOException if an I/O error occurs while creating the input stream
     */
    public static BufferedReader newReader(URL url) throws MalformedURLException, IOException {
        return newReader(url.openConnection().getInputStream());
    }

    /**
     * Creates a buffered reader for this URL using the given encoding.
     *
     * @param url a URL
     * @param charset opens the stream with a specified charset
     * @return a BufferedReader for the URL
     * @throws MalformedURLException is thrown if the URL is not well formed
     * @throws IOException if an I/O error occurs while creating the input stream
     */
    public static BufferedReader newReader(URL url, String charset) throws MalformedURLException, IOException {
        return new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), charset));
    }

    /**
     * Create a data input stream for this file
     *
     * @param file a File
     * @return a DataInputStream of the file
     * @throws FileNotFoundException if the file is not found.
     */
    public static DataInputStream newDataInputStream(File file) throws FileNotFoundException {
        return new DataInputStream(new FileInputStream(file));
    }

    /**
     * Traverse through each byte of this File
     *
     * @param self    a File
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @see #eachByte(InputStream,Closure)
     */
    public static void eachByte(File self, Closure closure) throws IOException {
        BufferedInputStream is = newInputStream(self);
        eachByte(is, closure);
    }

    /**
     * Traverse through each byte of this Byte array. Alias for each.
     *
     * @param self    a Byte array
     * @param closure a closure
     * @see #each(Object,Closure)
     */
    public static void eachByte(Byte[] self, Closure closure) {
        each(self, closure);
    }

    /**
     * Traverse through each byte of this byte array. Alias for each.
     *
     * @param self    a byte array
     * @param closure a closure
     * @see #each(Object,Closure)
     */
    public static void eachByte(byte[] self, Closure closure) {
        each(self, closure);
    }

    /**
     * Traverse through each byte of the specified stream. The
     * stream is closed after the closure returns.
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
     * Reads the InputStream from this URL, passing each byte to the given
     * closure.  The URL stream will be closed before this method returns.
     *
     * @param url     url to iterate over
     * @param closure closure to apply to each byte
     * @throws IOException if an IOException occurs.
     * @see #eachByte(InputStream,Closure)
     */
    public static void eachByte(URL url, Closure closure) throws IOException {
        InputStream is = url.openConnection().getInputStream();
        eachByte(is, closure);
    }

    /**
     * Transforms each character from this reader by passing it to the given
     * closure.  The Closure should return each transformed character, which
     * will be passed to the Writer.  The reader and writer will be both be
     * closed before this method returns.
     *
     * @param self    a Reader object
     * @param writer  a Writer to receive the transformed characters
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
     * closed after the operation.
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
     * according to a closure which returns true if the line should be included.
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
     * stream the filtered lines.
     *
     * @param self    a File
     * @param closure a closure which returns a boolean indicating to filter
     *                the line or not
     * @return a Writable closure
     * @throws IOException if <code>self</code> is not readable
     * @see #filterLine(Reader,Closure)
     */
    public static Writable filterLine(File self, Closure closure) throws IOException {
        return filterLine(newReader(self), closure);
    }

    /**
     * Filter the lines from this File, and write them to the given writer based
     * on the given closure predicate.
     *
     * @param self    a File
     * @param writer  a writer destination to write filtered lines to
     * @param closure a closure which takes each line as a parameter and returns
     *                <code>true</code> if the line should be written to this writer.
     * @throws IOException if <code>self</code> is not readable
     * @see #filterLine(Reader,Writer,Closure)
     */
    public static void filterLine(File self, Writer writer, Closure closure) throws IOException {
        filterLine(newReader(self), writer, closure);
    }

    /**
     * Filter the lines from this Reader, and return a Writable which can be
     * used to stream the filtered lines to a destination.  The closure should
     * return <code>true</code> if the line should be passed to the writer.
     *
     * @param reader  this reader
     * @param closure a closure used for filtering
     * @return a Writable which will use the closure to filter each line
     *         from the reader when the Writable#writeTo(Writer) is called.
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
     * Returns a GroovyRowResult given a ResultSet.
     *
     * @param rs a ResultSet
     * @return the resulting GroovyRowResult
     * @throws SQLException if a database error occurs
     */
    public static GroovyRowResult toRowResult(ResultSet rs) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        LinkedHashMap lhm = new LinkedHashMap(metadata.getColumnCount(), 1);
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            lhm.put(metadata.getColumnName(i), rs.getObject(i));
        }
        return new GroovyRowResult(lhm);
    }

    /**
     * Filter lines from an input stream using a closure predicate.  The closure
     * will be passed each line as a String, and it should return
     * <code>true</code> if the line should be passed to the writer.
     *
     * @param self      an input stream
     * @param predicate a closure which returns boolean and takes a line
     * @return a writable which writes out the filtered lines
     * @see #filterLine(Reader, Closure)
     */
    public static Writable filterLine(InputStream self, Closure predicate) {
        return filterLine(newReader(self), predicate);
    }

    /**
     * Uses a closure to filter lines from this InputStream and pass them to
     * the given writer. The closure will be passed each line as a String, and
     * it should return <code>true</code> if the line should be passed to the
     * writer.
     *
     * @param self      the InputStream
     * @param writer    a writer to write output to
     * @param predicate a closure which returns true if a line should be accepted
     * @throws IOException if an IOException occurs.
     * @see #filterLine(Reader,Writer,Closure)
     */
    public static void filterLine(InputStream self, Writer writer, Closure predicate)
            throws IOException {
        filterLine(newReader(self), writer, predicate);
    }

    /**
     * Reads the content of the file into a byte array.
     *
     * @param file a File
     * @return a byte array with the contents of the file.
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
     * Passes the Socket's InputStream and OutputStream to the closure.  The
     * streams will be closed after the closure returns, even if an exception
     * is thrown.
     *
     * @param socket  a Socket
     * @param closure a Closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     */
    public static Object withStreams(Socket socket, Closure closure) throws IOException {
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        try {
            Object result = closure.call(new Object[]{input, output});

            InputStream temp1 = input;
            input = null;
            temp1.close();
            OutputStream temp2 = output;
            output = null;
            temp2.close();

            return result;
        } finally {
            closeInputStreamWithWarning(input);
            closeOutputStreamWithWarning(output);
        }
    }

    /**
     * Creates an InputObjectStream and an OutputObjectStream from a Socket, and
     * passes them to the closure.  The streams will be closed after the closure
     * returns, even if an exception is thrown.
     *
     * @param socket  this Socket
     * @param closure a Closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @since 1.1 beta 2
     */
    public static Object withObjectStreams(Socket socket, Closure closure) throws IOException {
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(output);
        ObjectInputStream ois = new ObjectInputStream(input);
        try {
            Object result = closure.call(new Object[]{ois, oos});

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

            return result;
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
     * Accepts a connection and passes the resulting Socket to the closure
     * which runs in a new Thread.
     *
     * @param serverSocket a ServerSocket
     * @param closure      a Closure
     * @return a Socket
     * @throws IOException if an IOException occurs.
     * @see java.net.ServerSocket#accept()
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
     * Converts this File to a {@link Writable}.
     *
     * @param file a File
     * @return a File which wraps the input file and which implements Writable
     */
    public static File asWritable(File file) {
        return new WritableFile(file);
    }

    /**
     * Converts this File to a {@link Writable} or delegates to default
     * {@link #asType(Object,Class)}.
     *
     * @param f a File
     * @param c the desired class
     * @return the converted object
     */
    public static Object asType(File f, Class c) {
        if (c == Writable.class) {
            return asWritable(f);
        }
        return asType((Object) f, c);
    }

    /**
     * Allows a file to return a Writable implementation that can output itself
     * to a Writer stream.
     *
     * @param file     a File
     * @param encoding the encoding to be used when reading the file's contents
     * @return File which wraps the input file and which implements Writable
     */
    public static File asWritable(File file, String encoding) {
        return new WritableFile(file, encoding);
    }

    /**
     * Converts the given String into a List of strings of one character.
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

    /**
     * Converts the given String into an array of characters.
     * Alias for toCharArray.
     *
     * @param self a String
     * @return an array of characters
     * @see String#toCharArray()
     */
    public static char[] getChars(String self) {
        return self.toCharArray();
    }

    /**
     * Converts the GString to a File, or delegates to the default
     * {@link #asType(Object,Class)}
     *
     * @param self a GString
     * @param c    the desired class
     * @return the converted object
     */
    public static Object asType(GString self, Class c) {
        if (c == File.class) {
            return new File(self.toString());
        }
        return asType((Object) self, c);
    }

    /**
     * <p>Provides a method to perform custom 'dynamic' type conversion
     * to the given class using the <code>as</code> operator.</p>
     * <strong>Example:</strong> <code>'123' as Double</code>
     * <p>By default, the following types are supported:
     * <ul>
     * <li>List</li>
     * <li>BigDecimal</li>
     * <li>BigInteger</li>
     * <li>Long</li>
     * <li>Integer</li>
     * <li>Short</li>
     * <li>Byte</li>
     * <li>Character</li>
     * <li>Double</li>
     * <li>Float</li>
     * <li>File</li>
     * <li>Subclasses of Enum (Java 5 and above)</li>
     * </ul>
     * If any other type is given, the call is delegated to
     * {@link #asType(Object,Class)}.
     *
     * @param self a String
     * @param c    the desired class
     * @return the converted object
     */
    public static Object asType(String self, Class c) {
        if (c == List.class) {
            return toList(self);
        } else if (c == BigDecimal.class) {
            return toBigDecimal(self);
        } else if (c == BigInteger.class) {
            return toBigInteger(self);
        } else if (c == Long.class) {
            return toLong(self);
        } else if (c == Integer.class) {
            return toInteger(self);
        } else if (c == Short.class) {
            return toShort(self);
        } else if (c == Byte.class) {
            return Byte.valueOf(self.trim());
        } else if (c == Character.class) {
            return toCharacter(self);
        } else if (c == Double.class) {
            return toDouble(self);
        } else if (c == Float.class) {
            return toFloat(self);
        } else if (c == File.class) {
            return new File(self);
        } else if (DefaultTypeTransformation.isEnumSubclass(c)) {
            return InvokerHelper.invokeMethod(c, "valueOf", new Object[]{ self });
        }
        return asType((Object) self, c);
    }

    // Process methods
    //-------------------------------------------------------------------------

    /**
     * An alias method so that a process appears similar to System.out, System.in, System.err;
     * you can use process.in, process.out, process.err in a similar fashion.
     *
     * @param self a Process instance
     * @return the InputStream for the process
     */
    public static InputStream getIn(Process self) {
        return self.getInputStream();
    }

    /**
     * Read the text of the output stream of the Process.
     *
     * @param self a Process instance
     * @return the text of the output
     * @throws IOException if an IOException occurs.
     */
    public static String getText(Process self) throws IOException {
        return getText(new BufferedReader(new InputStreamReader(self.getInputStream())));
    }

    /**
     * An alias method so that a process appears similar to System.out, System.in, System.err;
     * you can use process.in, process.out, process.err in a similar fashion.
     *
     * @param self a Process instance
     * @return the error InputStream for the process
     */
    public static InputStream getErr(Process self) {
        return self.getErrorStream();
    }

    /**
     * An alias method so that a process appears similar to System.out, System.in, System.err;
     * you can use process.in, process.out, process.err in a similar fashion.
     *
     * @param self a Process instance
     * @return the OutputStream for the process
     */
    public static OutputStream getOut(Process self) {
        return self.getOutputStream();
    }

    /**
     * Overloads the left shift operator (&lt;&lt;) to provide an append mechanism
     * to pipe data to a Process.
     *
     * @param self  a Process instance
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
     * @param self  a Process instance
     * @param value data to append
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
     * Gets the output and error streams from a process and reads them
     * to keep the process from blocking due to a full output buffer. For this,
     * two Threads are started, so this method will return immediately.
     *
     * @param self a Process
     */
    public static void consumeProcessOutput(Process self) {
        consumeProcessOutput(self, (OutputStream)null, (OutputStream)null);
    }

    /**
     * Gets the output and error streams from a process and reads them
     * to keep the process from blocking due to a full output buffer.
     * The processed stream data is appended to the supplied StringBuffer.
     * For this, two Threads are started, so this method will return immediately.
     *
     * @param self a Process
     * @param output a StringBuffer to capture the process stdout
     * @param error a StringBuffer to capture the process stderr
     */
    public static void consumeProcessOutput(Process self, StringBuffer output, StringBuffer error) {
        consumeProcessOutputStream(self, output);
        consumeProcessErrorStream(self, error);
    }

    /**
     * Gets the output and error streams from a process and reads them
     * to keep the process from blocking due to a full output buffer.
     * The processed stream data is appended to the supplied OutputStream.
     * For this, two Threads are started, so this method will return immediately.
     *
     * @param self a Process
     * @param output an OutputStream to capture the process stdout
     * @param error an OutputStream to capture the process stderr
     */
    public static void consumeProcessOutput(Process self, OutputStream output, OutputStream error) {
        consumeProcessOutputStream(self, output);
        consumeProcessErrorStream(self, error);
    }

    /**
     * Gets the error stream from a process and reads it
     * to keep the process from blocking due to a full buffer.
     * The processed stream data is appended to the supplied StringBuffer.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param error a StringBuffer to capture the process stderr
     */
    public static void consumeProcessErrorStream(Process self, StringBuffer error) {
        new Thread(new TextDumper(self.getErrorStream(), error)).start();
    }

    /**
     * Gets the error stream from a process and reads it
     * to keep the process from blocking due to a full buffer.
     * The processed stream data is appended to the supplied OutputStream.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param err an OutputStream to capture the process stderr
     */
    public static void consumeProcessErrorStream(Process self, OutputStream err) {
        new Thread(new ByteDumper(self.getErrorStream(), err)).start();
    }

    /**
     * Gets the error stream from a process and reads it
     * to keep the process from blocking due to a full buffer.
     * The processed stream data is appended to the supplied Writer.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param err a Writer to capture the process stderr
     */
    public static void consumeProcessErrorStream(Process self, Writer err) {
        new Thread(new TextDumper(self.getErrorStream(), err)).start();
    }

    /**
     * Gets the output stream from a process and reads it
     * to keep the process from blocking due to a full output buffer.
     * The processed stream data is appended to the supplied StringBuffer.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param output a StringBuffer to capture the process stdout
     */
    public static void consumeProcessOutputStream(Process self, StringBuffer output) {
        new Thread(new TextDumper(self.getInputStream(), output)).start();
    }

    /**
     * Gets the output stream from a process and reads it
     * to keep the process from blocking due to a full output buffer.
     * The processed stream data is appended to the supplied OutputStream.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param output an OutputStream to capture the process stdout
     */
    public static void consumeProcessOutputStream(Process self, OutputStream output) {
        new Thread(new ByteDumper(self.getInputStream(), output)).start();
    }

    /**
     * Gets the output stream from a process and reads it
     * to keep the process from blocking due to a full output buffer.
     * The processed stream data is appended to the supplied Writer.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param output a Writer to capture the process stdout
     */
    public static void consumeProcessOutputStream(Process self, Writer output) {
        new Thread(new TextDumper(self.getInputStream(), output)).start();
    }

    /**
     * Creates a new BufferedWriter as stdin for this process,
     * passes it to the closure, and ensures the stream is flushed
     * and closed after the closure returns.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param closure a closure
     */
    public static void withWriter(final Process self, final Closure closure) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    withWriter(new BufferedOutputStream(getOut(self)), closure);
                } catch (IOException e) {
                    throw new GroovyRuntimeException("exception while reading process stream", e);
                }
            }
        }).start();
    }

    /**
     * Creates a new buffered OutputStream as stdin for this process,
     * passes it to the closure, and ensures the stream is flushed
     * and closed after the closure returns.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param closure a closure
     */
    public static void withOutputStream(final Process self, final Closure closure) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    withStream(new BufferedOutputStream(getOut(self)), closure);
                } catch (IOException e) {
                    throw new GroovyRuntimeException("exception while reading process stream", e);
                }
            }
        }).start();
    }

    /**
     * Allows one Process to asynchronously pipe data to another Process.
     *
     * @param left  a Process instance
     * @param right a Process to pipe output to
     * @return the second Process to allow chaining
     * @throws IOException if an IOException occurs.
     */
    public static Process pipeTo(final Process left, final Process right) throws IOException {
        new Thread(new Runnable() {
            public void run() {
                InputStream in = new BufferedInputStream(getIn(left));
                OutputStream out = new BufferedOutputStream(getOut(right));
                byte[] buf = new byte[8192];
                int next;
                try {
                    while ((next = in.read(buf)) != -1) {
                        out.write(buf, 0, next);
                    }
                } catch (IOException e) {
                    throw new GroovyRuntimeException("exception while reading process stream", e);
                } finally {
                    closeOutputStreamWithWarning(out);
                }
            }
        }).start();
        return right;
    }

    /**
     * Overrides the or operator to allow one Process to asynchronously
     * pipe data to another Process.
     *
     * @param left  a Process instance
     * @param right a Process to pipe output to
     * @return the second Process to allow chaining
     * @throws IOException if an IOException occurs.
     */
    public static Process or(final Process left, final Process right) throws IOException {
        return pipeTo(left, right);
    }

    /**
     * Process each regex group matched substring of the given string. If the closure
     * parameter takes one argument, an array with all match groups is passed to it.
     * If the closure takes as many arguments as there are match groups, then each
     * parameter will be one match group.
     *
     * @param self    the source string
     * @param regex   a Regex string
     * @param closure a closure with one parameter or as much parameters as groups
     * @return the source string
     */
    public static void eachMatch(String self, String regex, Closure closure) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(self);
        while (m.find()) {
            int count = m.groupCount();
            List groups = new ArrayList();
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
     * @return the matcher
     */
    public static Matcher each(Matcher self, Closure closure) {
        while (self.find()) {
            int count = self.groupCount();
            List groups = new ArrayList();
            for (int i = 0; i <= count; i++) {
                groups.add(self.group(i));
            }
            closure.call(groups.toArray());
        }
        return self;
    }

    /**
     * Iterates over the elements of an iterable collection of items and returns
     * the index of the first item that matches the condition specified in the closure.
     *
     * @param self    the iteration object over which to iterate
     * @param closure the filter to perform a match on the collection
     * @return an integer that is the index of the first matched object or -1 if no match was found
     */
    public static int findIndexOf(Object self, Closure closure) {
        return findIndexOf(self, 0, closure);
    }

    /**
     * Iterates over the elements of an iterable collection of items, starting from a
     * specified startIndex, and returns the index of the first item that matches the
     * condition specified in the closure.
     *
     * @param self       the iteration object over which to iterate
     * @param startIndex start matching from this index
     * @param closure    the filter to perform a match on the collection
     * @return an integer that is the index of the first matched object or -1 if no match was found
     */
    public static int findIndexOf(Object self, int startIndex, Closure closure) {
        int result = -1;
        int i = 0;
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext(); i++) {
            Object value = iter.next();
            if (i < startIndex) {
                continue;
            }
            if (DefaultTypeTransformation.castToBoolean(closure.call(value))) {
                result = i;
                break;
            }
        }
        return result;
    }

    /**
     * Iterates over the elements of an iterable collection of items and returns
     * the index of the last item that matches the condition specified in the closure.
     *
     * @param self    the iteration object over which to iterate
     * @param closure the filter to perform a match on the collection
     * @return an integer that is the index of the last matched object or -1 if no match was found
     */
    public static int findLastIndexOf(Object self, Closure closure) {
        return findLastIndexOf(self, 0, closure);
    }

    /**
     * Iterates over the elements of an iterable collection of items, starting
     * from a specified startIndex, and returns the index of the last item that
     * matches the condition specified in the closure.
     *
     * @param self       the iteration object over which to iterate
     * @param startIndex start matching from this index
     * @param closure    the filter to perform a match on the collection
     * @return an integer that is the index of the last matched object or -1 if no match was found
     */
    public static int findLastIndexOf(Object self, int startIndex, Closure closure) {
        int result = -1;
        int i = 0;
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext(); i++) {
            Object value = iter.next();
            if (i < startIndex) {
                continue;
            }
            if (DefaultTypeTransformation.castToBoolean(closure.call(value))) {
                result = i;
            }
        }
        return result;
    }

    /**
     * Iterates over the elements of an iterable collection of items and returns
     * the index values of the items that match the condition specified in the closure.
     *
     * @param self    the iteration object over which to iterate
     * @param closure the filter to perform a match on the collection
     * @return a list of integers corresponding to the index values of all matched objects
     */
    public static List findIndexValues(Object self, Closure closure) {
        return findIndexValues(self, 0, closure);
    }

    /**
     * Iterates over the elements of an iterable collection of items, starting from
     * a specified startIndex, and returns the index values of the items that match
     * the condition specified in the closure.
     *
     * @param self       the iteration object over which to iterate
     * @param startIndex start matching from this index
     * @param closure    the filter to perform a match on the collection
     * @return a list of integers corresponding to the index values of all matched objects
     */
    public static List findIndexValues(Object self, int startIndex, Closure closure) {
        List result = new ArrayList();
        int i = 0;
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext(); i++) {
            Object value = iter.next();
            if (i < startIndex) {
                continue;
            }
            if (DefaultTypeTransformation.castToBoolean(closure.call(value))) {
                result.add(new Integer(i));
            }
        }
        return result;
    }

    /**
     * Iterates through the classloader parents until it finds a loader with a class
     * named "org.codehaus.groovy.tools.RootLoader". If there is no such class
     * <code>null</code> will be returned. The name is used for comparison because
     * a direct comparison using == may fail as the class may be loaded through
     * different classloaders.
     *
     * @param self a ClassLoader
     * @return the rootLoader for the ClassLoader
     * @see org.codehaus.groovy.tools.RootLoader
     */
    public static ClassLoader getRootLoader(ClassLoader self) {
        while (true) {
            if (self == null) return null;
            if (isRootLoaderClassOrSubClass(self)) return self;
            self = self.getParent();
        }
    }

    private static boolean isRootLoaderClassOrSubClass(ClassLoader self) {
        Class current = self.getClass();
        while(!current.getName().equals(Object.class.getName())) {
            if(current.getName().equals(RootLoader.class.getName())) return true;
            current = current.getSuperclass();
        }

        return false;
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

    /**
     * Convenience method to dynamically create a new instance of this
     * class.  Calls the default constructor.
     *
     * @param c a class
     * @return a new instance of this class
     */
    public static Object newInstance(Class c) {
        return InvokerHelper.invokeConstructorOf(c, null);
    }

    /**
     * Helper to construct a new instance from the given arguments.
     * The constructor is called based on the number and types in the
     * args array.  Use <code>newInstance(null)</code> or simply
     * <code>newInstance()</code> for the default (no-arg) constructor.
     *
     * @param c    a class
     * @param args the constructor arguments
     * @return a new instance of this class.
     */
    public static Object newInstance(Class c, Object[] args) {
        if (args == null) args = new Object[]{null};
        return InvokerHelper.invokeConstructorOf(c, args);
    }


    /**
     * Adds a "metaClass" property to all class objects so you can use the syntax
     * <code>String.metaClass.myMethod = { println "foo" }</code>
     *
     * @param c The java.lang.Class instance
     * @return An MetaClass instance
     */
    public static MetaClass getMetaClass(Class c) {
        MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        MetaClass mc = metaClassRegistry.getMetaClass(c);
        if (mc instanceof ExpandoMetaClass
                || mc instanceof DelegatingMetaClass && ((DelegatingMetaClass) mc).getAdaptee() instanceof ExpandoMetaClass)
            return mc;
        else {
            MetaClass emc = ExpandoMetaClassCreationHandle.instance.create(c, metaClassRegistry);
            emc.initialize();
            metaClassRegistry.setMetaClass(c, emc);
            return emc;
        }
    }

    /**
     * Obtains a MetaClass for an object either from the registry or in the case of
     * a GroovyObject from the object itself.
     *
     * @param obj The object in question
     * @return The MetaClass
     */
    public static MetaClass getMetaClass(Object obj) {
        if (obj instanceof GroovyObject) {
            return ((GroovyObject) obj).getMetaClass();
        }
        return GroovySystem.getMetaClassRegistry().getMetaClass(obj.getClass());
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

    private static class TextDumper implements Runnable {
        InputStream in;
        StringBuffer sb;
        Writer w;

        public TextDumper(InputStream in, StringBuffer sb) {
            this.in = in;
            this.sb = sb;
        }

        public TextDumper(InputStream in, Writer w) {
            this.in = in;
            this.w = w;
        }

        public void run() {
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            String next;
            try {
                while ((next = br.readLine()) != null) {
                    if (sb != null) {
                        sb.append(next);
                        sb.append("\n");
                    } else {
                        w.write(next);
                        w.write("\n");
                    }
                }
            } catch (IOException e) {
                throw new GroovyRuntimeException("exception while reading process stream", e);
            }
        }
    }

    private static class ByteDumper implements Runnable {
        InputStream in;
        OutputStream out;

        public ByteDumper(InputStream in, OutputStream out) {
            this.in = new BufferedInputStream(in);
            this.out = out;
        }

        public ByteDumper(InputStream in) {
            this.in = new BufferedInputStream(in);
        }

        public void run() {
            byte[] buf = new byte[8192];
            int next;
            try {
                while ((next = in.read(buf)) != -1) {
                    if (out != null) out.write(buf, 0, next);
                }
            } catch (IOException e) {
                throw new GroovyRuntimeException("exception while dumping process stream", e);
            }
        }
    }

    /**
     * Attempts to create an Iterator for the given object by first
     * converting it to a Collection.
     *
     * @param o an object
     * @return an Iterator for the given Object.
     * @see DefaultTypeTransformation#asCollection(Object)
     */
    public static Iterator iterator(Object o) {
        return DefaultTypeTransformation.asCollection(o).iterator();
    }

    /**
     * Allows an Enumeration to behave like an Iterator.  Note that the
     * {@link Iterator#remove() remove()} method is unsupported since the
     * underlying Enumeration does not provide a mechanism for removing items.
     *
     * @param enumeration an Enumeration object
     * @return an Iterator for the given Enumeration
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
     * Makes NodeList iterable by returning a read-only Iterator which traverses
     * over each Node.
     *
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
     * Retuns an {@link Iterator} which traverses each match.
     *
     * @param matcher a Matcher object
     * @return an Iterator for a Matcher
     * @see Matcher#group()
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
     * Creates an iterator which will traverse through the reader a line at a time.
     *
     * @param self a Reader object
     * @return an Iterator for the Reader
     * @see java.io.BufferedReader#readLine()
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
     * Standard iterator for a input stream which iterates through the stream
     * content in a byte-based fashion.
     *
     * @param self an InputStream object
     * @return an Iterator for the InputStream
     */
    public static Iterator iterator(InputStream self) {
        return iterator(new DataInputStream(self));
    }

    /**
     * Standard iterator for a data input stream which iterates through the
     * stream content a byte at a time.
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
     * Just throws a DeprecationException.  DO NOT USE.  It used to provide an iterator for text file content
     * one line at a time.
     *
     * @param self a file object
     * @return a line-based iterator
     * @throws IOException if there is a problem processing the file (e.g. file is not found)
     * @deprecated use File#eachLine instead please
     */
    public static Iterator iterator(File self) throws IOException {
        throw new DeprecationException(
                "Iterators on files are not supported any more. " +
                        "Use File.eachLine() instead. Alternatively you can use FileReader.iterator() " +
                        "and provide your own exception handling."
        );
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
