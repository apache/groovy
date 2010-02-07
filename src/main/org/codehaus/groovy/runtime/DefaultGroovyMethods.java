/*
 * Copyright 2003-2010 the original author or authors.
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

import groovy.io.EncodingAwareBufferedWriter;
import groovy.io.FileType;
import groovy.io.FileVisitResult;
import groovy.io.GroovyPrintWriter;
import groovy.lang.*;
import groovy.sql.GroovyResultSet;
import groovy.util.*;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.reflection.MixinInMetaClass;
import org.codehaus.groovy.runtime.dgmimpl.NumberNumberDiv;
import org.codehaus.groovy.runtime.dgmimpl.NumberNumberMinus;
import org.codehaus.groovy.runtime.dgmimpl.NumberNumberMultiply;
import org.codehaus.groovy.runtime.dgmimpl.NumberNumberPlus;
import org.codehaus.groovy.runtime.dgmimpl.arrays.*;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.runtime.metaclass.MissingPropertyExceptionNoStack;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.typehandling.GroovyCastException;
import org.codehaus.groovy.runtime.typehandling.NumberMath;
import org.codehaus.groovy.tools.RootLoader;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
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
 * @author Marc Guillemot
 * @author Russel Winder
 * @author bing ran
 * @author Jochen Theodorou
 * @author Paul King
 * @author Michael Baehr
 * @author Joachim Baumann
 * @author Alex Tkachman
 * @author Ted Naleid
 * @author Brad Long
 * @author Jim Jagielski
 * @author Rodolfo Velasco
 */
public class DefaultGroovyMethods extends DefaultGroovyMethodsSupport {

    private static final Logger LOG = Logger.getLogger(DefaultGroovyMethods.class.getName());
    private static final Integer ONE = 1;

    public static final Class [] additionals = {
            NumberNumberPlus.class,
            NumberNumberMultiply.class,
            NumberNumberMinus.class,
            NumberNumberDiv.class,
            ObjectArrayGetAtMetaMethod.class,
            ObjectArrayPutAtMetaMethod.class,
            BooleanArrayGetAtMetaMethod.class,
            BooleanArrayPutAtMetaMethod.class,
            ByteArrayGetAtMetaMethod.class,
            ByteArrayPutAtMetaMethod.class,
            CharacterArrayGetAtMetaMethod.class,
            CharacterArrayPutAtMetaMethod.class,
            ShortArrayGetAtMetaMethod.class,
            ShortArrayPutAtMetaMethod.class,
            IntegerArrayGetAtMetaMethod.class,
            IntegerArrayPutAtMetaMethod.class,
            LongArrayGetAtMetaMethod.class,
            LongArrayPutAtMetaMethod.class,
            FloatArrayGetAtMetaMethod.class,
            FloatArrayPutAtMetaMethod.class,
            DoubleArrayGetAtMetaMethod.class,
            DoubleArrayPutAtMetaMethod.class,
    };

    /**
     * Identity check. Since == is overridden in Groovy with the meaning of equality
     * we need some fallback to check for object identity.  Invoke using the
     * 'is' method, like so: <code>def same = this.is(that)</code>
     *
     * @param self  an object
     * @param other an object to compare identity with
     * @return true if self and other are both references to the same
     *         instance, false otherwise
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.5.0
     */
    public static Object with(Object self, Closure closure) {
        final Closure clonedClosure = (Closure) closure.clone();
        clonedClosure.setResolveStrategy(Closure.DELEGATE_FIRST);
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
     */
    public static String dump(Object self) {
        if (self == null) {
            return "null";
        }
        StringBuilder buffer = new StringBuilder("<");
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
            for (final Field field : klass.getDeclaredFields()) {
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
            if(pv.getName().equals("class")) continue;
            if(pv.getName().equals("metaClass")) continue;

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
     * @since 1.0
     */
    public static List<PropertyValue> getMetaPropertyValues(Object self) {
        MetaClass metaClass = InvokerHelper.getMetaClass(self);
        List<MetaProperty> mps = metaClass.getProperties();
        List<PropertyValue> props = new ArrayList<PropertyValue>(mps.size());
        for (MetaProperty mp : mps) {
            props.add(new PropertyValue(self, mp));
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
     * @since 1.0
     */
    public static Map getProperties(Object self) {
        List<PropertyValue> metaProps = getMetaPropertyValues(self);
        Map<String, Object> props = new LinkedHashMap<String, Object>(metaProps.size());

        for (PropertyValue mp : metaProps) {
            try {
                props.put(mp.getName(), mp.getValue());
            } catch (Exception e) {
                LOG.throwing(self.getClass().getName(), "getProperty(" + mp.getName() + ")", e);
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
     * @since 1.0
     */
    public static Object use(Object self, Class categoryClass, Closure closure) {
        return GroovyCategorySupport.use(categoryClass, closure);
    }

    /**
     * Extend object with category methods.
     * All methods for given class and all super classes will be added to the object.
     *
     * @param self          any Class
     * @param categoryClasses a category classes to use
     * @since 1.6.0
     */
    public static void mixin(MetaClass self, List<Class> categoryClasses) {
        MixinInMetaClass.mixinClassesToMetaClass(self, categoryClasses);
    }

    /**
     * Extend class globally with category methods.
     * All methods for given class and all super classes will be added to the class.
     *
     * @param self          any Class
     * @param categoryClasses a category classes to use
     * @since 1.6.0
     */
    public static void mixin(Class self, List<Class> categoryClasses) {
        mixin(getMetaClass(self), categoryClasses);
    }

    /**
     * Extend class globally with category methods.
     *
     * @param self          any Class
     * @param categoryClass a category class to use
     * @since 1.6.0
     */
    public static void mixin(Class self, Class categoryClass) {
        mixin(getMetaClass(self), Collections.singletonList(categoryClass));
    }

    /**
     * Extend class globally with category methods.
     *
     * @param self          any Class
     * @param categoryClass a category class to use
     * @since 1.6.0
     */
    public static void mixin(Class self, Class[] categoryClass) {
        mixin(getMetaClass(self), Arrays.asList(categoryClass));
    }

    /**
     * Extend class globally with category methods.
     *
     * @param self          any Class
     * @param categoryClass a category class to use
     * @since 1.6.0
     */
    public static void mixin(MetaClass self, Class categoryClass) {
        mixin(self, Collections.singletonList(categoryClass));
    }

    /**
     * Extend class globally with category methods.
     *
     * @param self          any Class
     * @param categoryClass a category class to use
     * @since 1.6.0
     */
    public static void mixin(MetaClass self, Class[] categoryClass) {
        mixin(self, Arrays.asList(categoryClass));
    }

    /**
     * Scoped use method with list of categories.
     *
     * @param self              any Object
     * @param categoryClassList a list of category classes
     * @param closure           the closure to invoke with the categories in place
     * @return the value returned from the closure
     * @since 1.0
     */
    public static Object use(Object self, List<Class> categoryClassList, Closure closure) {
        return GroovyCategorySupport.use(categoryClassList, closure);
    }

    /**
     * Allows the usage of addShutdownHook without getting the runtime first.
     *
     * @param self    the object the method is called on (ignored)
     * @param closure the shutdown hook action
     * @since 1.5.0
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
     * @since 1.0
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
        List<Class> list = new ArrayList<Class>(array.length - 1);
        for (int i = 0; i < array.length - 1; ++i) {
            Class categoryClass;
            try {
                categoryClass = (Class) array[i];
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Expecting a Category Class for argument " + i);
            }
            list.add(categoryClass);
        }
        return GroovyCategorySupport.use(list, closure);
    }

    /**
     * Print a value formatted Groovy style to self if it
     * is a Writer, otherwise to the standard output stream.
     *
     * @param self  any Object
     * @param value the value to print
     * @since 1.0
     */
    public static void print(Object self, Object value) {
        // we won't get here if we are a PrintWriter
        if (self instanceof Writer) {
            try {
                ((Writer) self).write(InvokerHelper.toString(value));
            } catch (IOException e) {
                // TODO: Should we have some unified function like PrintWriter.checkError()?
            }
        } else {
            System.out.print(InvokerHelper.toString(value));
        }
    }
    
    /**
     * Print a value formatted Groovy style to the print writer.
     *
     * @param self  a PrintWriter
     * @param value the value to print
     * @since 1.0
     */
    public static void print(PrintWriter self, Object value) {
        self.print(InvokerHelper.toString(value));
    }
    
    /**
     * Print a value formatted Groovy style to the print stream.
     *
     * @param self  a PrintStream
     * @param value the value to print
     * @since 1.6.0
     */
    public static void print(PrintStream self, Object value) {
        self.print(InvokerHelper.toString(value));
    }
    
    /**
     * Print a value to the standard output stream.
     * This method delegates to the owner to execute the method.
     *
     * @param self  a generated closure
     * @param value the value to print
     * @since 1.0
     */
    public static void print(Closure self, Object value) {
        Object owner = getClosureOwner(self);
        InvokerHelper.invokeMethod(owner, "print", new Object[]{value});
    }

    /**
     * Print a linebreak to the standard output stream.
     *
     * @param self any Object
     * @since 1.0
     */
    public static void println(Object self) {
        // we won't get here if we are a PrintWriter
        if (self instanceof Writer) {
            PrintWriter pw = new GroovyPrintWriter((Writer) self);
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
     * @since 1.0
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
     * Print a value formatted Groovy style (followed by a newline) to self
     * if it is a Writer, otherwise to the standard output stream.
     *
     * @param self  any Object
     * @param value the value to print
     * @since 1.0
     */
    public static void println(Object self, Object value) {
        // we won't get here if we are a PrintWriter
        if (self instanceof Writer) {
            final PrintWriter pw = new GroovyPrintWriter((Writer) self);
            pw.println(value);
        } else {
            System.out.println(InvokerHelper.toString(value));
        }
    }
    
    /**
     * Print a value formatted Groovy style (followed by a newline) to the print writer.
     *
     * @param self  a PrintWriter
     * @param value the value to print
     * @since 1.0
     */
    public static void println(PrintWriter self, Object value) {
        self.println(InvokerHelper.toString(value));
    }
    
    /**
     * Print a value formatted Groovy style (followed by a newline) to the print stream.
     *
     * @param self  any Object
     * @param value the value to print
     * @since 1.6.0
     */
    public static void println(PrintStream self, Object value) {
        self.println(InvokerHelper.toString(value));
    }
    
    /**
     * Print a value (followed by a newline) to the standard output stream.
     * This method delegates to the owner to execute the method.
     *
     * @param self  a closure
     * @param value the value to print
     * @since 1.0
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
     * @since 1.0
     */
    public static void printf(Object self, String format, Object[] values) {
        if (self instanceof PrintStream)
            ((PrintStream)self).printf(format, values);
        else
            System.out.printf(format, values);
    }

    /**
     * Sprintf to a string (Only works with JDK1.5 or later).
     *
     * @param self   any Object
     * @param format a format string
     * @param values values referenced by the format specifiers in the format string.
     * @return the resulting formatted string
     * @since 1.5.0
     */
    public static String sprintf(Object self, String format, Object[] values) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputStream);
        out.printf(format, values);
        return outputStream.toString();
    }

    /**
     * Prints a formatted string using the specified format string and
     * arguments.
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
     * @since 1.0
     */
    public static void printf(Object self, String format, Object arg) {
        if (self instanceof PrintStream)
            printf((PrintStream) self, format, arg);
        else
            printf(System.out, format, arg);
    }

    private static void printf(PrintStream self, String format, Object arg) {
        self.print(sprintf(self, format, arg));
    }

    /**
     * Returns a formatted string using the specified format string and
     * arguments.
     * <p/>
     *
     * @param self   any Object
     * @param format A format string
     * @param arg    Argument which is referenced by the format specifiers in the format
     *               string.  The type of <code>arg</code> should be one of Object[], List,
     *               int[], short[], byte[], char[], boolean[], long[], float[], or double[].
     * @return the resulting printf'd string
     * @since 1.5.0
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
                ans[i] = ia[i];
            }
        } else if (elemType.equals("[C")) {
            char[] ca = (char[]) arg;
            ans = new Character[ca.length];
            for (int i = 0; i < ca.length; i++) {
                ans[i] = ca[i];
            }
        } else if (elemType.equals("[Z")) {
            boolean[] ba = (boolean[]) arg;
            ans = new Boolean[ba.length];
            for (int i = 0; i < ba.length; i++) {
                ans[i] = ba[i];
            }
        } else if (elemType.equals("[B")) {
            byte[] ba = (byte[]) arg;
            ans = new Byte[ba.length];
            for (int i = 0; i < ba.length; i++) {
                ans[i] = ba[i];
            }
        } else if (elemType.equals("[S")) {
            short[] sa = (short[]) arg;
            ans = new Short[sa.length];
            for (int i = 0; i < sa.length; i++) {
                ans[i] = sa[i];
            }
        } else if (elemType.equals("[F")) {
            float[] fa = (float[]) arg;
            ans = new Float[fa.length];
            for (int i = 0; i < fa.length; i++) {
                ans[i] = fa[i];
            }
        } else if (elemType.equals("[J")) {
            long[] la = (long[]) arg;
            ans = new Long[la.length];
            for (int i = 0; i < la.length; i++) {
                ans[i] = la[i];
            }
        } else if (elemType.equals("[D")) {
            double[] da = (double[]) arg;
            ans = new Double[da.length];
            for (int i = 0; i < da.length; i++) {
                ans[i] = da[i];
            }
        } else {
            throw new RuntimeException("sprintf(String," + arg + ")");
        }
        return sprintf(self, format, ans);
    }


    /**
     * Inspects returns the String that matches what would be typed into a
     * terminal to create this object.
     *
     * @param self any Object
     * @return a String that matches what would be typed into a terminal to
     *         create this object. e.g. [1, 'hello'].inspect() -> [1, "hello"]
     * @since 1.0
     */
    public static String inspect(Object self) {
        return InvokerHelper.inspect(self);
    }

    /**
     * Print to a console in interactive format.
     *
     * @param self any Object
     * @param out  the PrintWriter used for printing
     * @since 1.0
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
     * @since 1.0
     */
    public static void println(Object self, PrintWriter out) {
        if (out == null) {
            out = new PrintWriter(System.out);
        }
        out.println(InvokerHelper.toString(self));
    }

    /**
     * Provide a dynamic method invocation method which can be overloaded in
     * classes to implement dynamic proxies easily.
     *
     * @param object    any Object
     * @param method    the name of the method to call
     * @param arguments the arguments to use
     * @return the result of the method call
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
     */
    public static boolean isCase(String caseValue, Object switchValue) {
        if (switchValue == null) {
            return caseValue == null;
        }
        return caseValue.equals(switchValue.toString());
    }

    /**
     * 'Case' implementation for a GString, which simply calls the equivalet method for String.
     *
     * @param caseValue   the case value
     * @param switchValue the switch value
     * @return true if the switchValue's toString() equals the caseValue
     * @since 1.6.0
     */
    public static boolean isCase(GString caseValue, Object switchValue) {
        return isCase(caseValue.toString(), switchValue);
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
     * @since 1.0
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
     * <pre class="groovyTestCase">switch( 3 ) {
     *   case [1,3,5]:
     *     assert true
     *     break
     *   default:
     *     assert false
     * }</pre>
     *
     * @param caseValue   the case value
     * @param switchValue the switch value
     * @return true if the caseValue is deemed to contain the switchValue
     * @see java.util.Collection#contains(Object)
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.5.0
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
     * @since 1.5.5
     */
    public static <T> Iterator<T> unique(Iterator<T> self) {
        return toList(unique(toList(self))).listIterator();
    }

    /**
     * Modifies this collection to remove all duplicated items, using the
     * default comparator.
     * <pre class="groovyTestCase">assert [1,3] == [1,3,3].unique()</pre>
     *
     * @param self a collection
     * @return the now modified collection
     * @since 1.0
     */
    public static <T> Collection<T> unique(Collection<T> self) {
        if (self instanceof Set)
            return self;
        List<T> answer = new ArrayList<T>();
        NumberAwareComparator<T> numberAwareComparator = new NumberAwareComparator<T>();
        for (T t : self) {
            boolean duplicated = false;
            for (T t2 : answer) {
                if (numberAwareComparator.compare(t, t2) == 0) {
                    duplicated = true;
                    break;
                }
            }
            if (!duplicated)
                answer.add(t);
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
     * @since 1.6.0
     */
    public static int numberAwareCompareTo(Comparable self, Comparable other) {
        NumberAwareComparator<Comparable> numberAwareComparator = new NumberAwareComparator<Comparable>();
        return numberAwareComparator.compare(self, other);
    }

    /**
     * Returns an iterator equivalent to this iterator but with all duplicated items
     * removed by using a Closure to determine duplicate (equal) items.
     * The original iterator will be fully processed after the call.
     * </p>
     * If the closure takes a
     * single parameter, the argument passed will be each element, and the
     * closure should return a value used for comparison (either using
     * {@link Comparable#compareTo(Object)} or {@link Object#equals(Object)}).
     * If the closure takes two parameters, two items from the Iterator
     * will be passed as arguments, and the closure should return an
     * int value (with 0 indicating the items are not unique).
     *
     * @param self an Iterator
     * @param closure a Closure used to determine unique items
     * @return the modified Iterator
     * @since 1.5.5
     */
    public static <T> Iterator<T> unique(Iterator<T> self, Closure closure) {
        return toList(unique(toList(self), closure)).listIterator();
    }

    /**
     * A convenience method for making a collection unique using a Closure
     * to determine duplicate (equal) items.
     * </p>
     * If the closure takes a single parameter, the
     * argument passed will be each element, and the closure
     * should return a value used for comparison (either using
     * {@link Comparable#compareTo(Object)} or {@link Object#equals(Object)}).
     * If the closure takes two parameters, two items from the collection
     * will be passed as arguments, and the closure should return an
     * int value (with 0 indicating the items are not unique).
     * <pre class="groovyTestCase">assert [1,4] == [1,3,4,5].unique { it % 2 }</pre>
     * <pre class="groovyTestCase">assert [2,3,4] == [2,3,3,4].unique { a, b -> a <=> b }</pre>
     *
     * @param self    a Collection
     * @param closure a 1 or 2 arg Closure used to determine unique items
     * @return self   without any duplicates
     * @since 1.0 
     */
    public static <T> Collection<T> unique(Collection<T> self, Closure closure) {
        // use a comparator of one item or two
        int params = closure.getMaximumNumberOfParameters();
        if (params == 1) {
            unique(self, new OrderBy<T>(closure));
        } else {
            unique(self, new ClosureComparator<T>(closure));
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
     * @since 1.5.5
     */
    public static <T> Iterator<T> unique(Iterator<T> self, Comparator<T> comparator) {
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
     * <code><pre class="groovyTestCase">
     *     class Person {
     *         def fname, lname
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
     * </pre></code>
     *
     * @param self       a Collection
     * @param comparator a Comparator
     * @return self       the now modified collection without duplicates
     * @since 1.0
     */
    public static <T> Collection<T> unique(Collection<T> self, Comparator<T> comparator) {
        List<T> answer = new ArrayList<T>();
        for (T t : self) {
            boolean duplicated = false;
            for (T t2 : answer) {
                if (comparator.compare(t, t2) == 0) {
                    duplicated = true;
                    break;
                }
            }
            if (!duplicated)
                answer.add(t);
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
     * @since 1.0
     */
    public static <T> T each(T self, Closure closure) {
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
     * @since 1.0
     */
    public static Object eachWithIndex(Object self, Closure closure) {
        int counter = 0;
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            closure.call(new Object[]{iter.next(), counter++});
        }
        return self;
    }

    private static <T> Iterator<T> each(Iterator<T> iter, Closure closure) {
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
     * <pre class="groovyTestCase">def result = ""
     * [a:1, b:3].each { key, value -> result += "$key$value" }
     * assert result == "a1b3"</pre>
     * <pre class="groovyTestCase">def result = ""
     * [a:1, b:3].each { entry -> result += entry }
     * assert result == "a=1b=3"</pre>
     *
     * @param self    the map over which we iterate
     * @param closure the 1 or 2 arg closure applied on each entry of the map
     * @return returns the self parameter
     * @since 1.5.0
     */
    public static <K, V> Map<K, V> each(Map<K, V> self, Closure closure) {
        for (Map.Entry entry : self.entrySet()) {
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
     * <pre class="groovyTestCase">def result = ""
     * [a:1, b:3].eachWithIndex { key, value, index -> result += "$index($key$value)" }
     * assert result == "0(a1)1(b3)"</pre>
     * <pre class="groovyTestCase">def result = ""
     * [a:1, b:3].eachWithIndex { entry, index -> result += "$index($entry)" }
     * assert result == "0(a=1)1(b=3)"</pre>
     *
     * @param self    the map over which we iterate
     * @param closure a 2 or 3 arg Closure to operate on each item
     * @return the self Object
     * @since 1.5.0
     */
    public static <K, V> Map<K, V> eachWithIndex(Map<K, V> self, Closure closure) {
        int counter = 0;
        for (Map.Entry entry : self.entrySet()) {
            callClosureForMapEntryAndCounter(closure, entry, counter++);
        }
        return self;
    }

    /**
     * Iterate over each element of the list in the reverse order.
     * <pre class="groovyTestCase">def result = []
     * [1,2,3].reverseEach { result << it }
     * assert result == [3,2,1]</pre>
     *
     * @param self    a List
     * @param closure a closure to which each item is passed.
     * @return the original list
     * @since 1.5.0
     */
    public static <T> List<T> reverseEach(List<T> self, Closure closure) {
        each(new ReverseListIterator<T>(self), closure);
        return self;
    }

    /**
     * Iterate over each element of the array in the reverse order.
     *
     * @param self    an Object array
     * @param closure a closure to which each item is passed
     * @return the original array
     * @since 1.5.2
     */
    public static <T> T[] reverseEach(T[] self, Closure closure) {
        each(new ReverseListIterator<T>(Arrays.asList(self)), closure);
        return self;
    }

    /**
     * Reverse the items in an Object array.
     *
     * @param self    an Object array
     * @return an array containing the reversed items
     * @since 1.5.5
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] reverse(T[] self) {
        return (T[]) toList(new ReverseListIterator<T>(Arrays.asList(self))).toArray();
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
     * @since 1.0
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
     * valid for all entries. If the
     * closure takes one parameter then it will be passed the Map.Entry
     * otherwise if the closure takes two parameters then it will be
     * passed the key and the value.
     * <pre class="groovyTestCase">def map = [a:1, b:2.0, c:2L]
     * assert !map.every { key, value -> value instanceof Integer }
     * assert map.every { entry -> entry.value instanceof Number }</pre>
     *
     * @param self    the map over which we iterate
     * @param closure the 1 or 2 arg Closure predicate used for matching
     * @return true if every entry of the map matches the closure predicate
     * @since 1.5.0
     */
    public static <K, V> boolean every(Map<K, V> self, Closure closure) {
        for (Map.Entry entry : self.entrySet()) {
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
     * @since 1.5.0
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
     * @since 1.0
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
     * valid for at least one entry. If the
     * closure takes one parameter then it will be passed the Map.Entry
     * otherwise if the closure takes two parameters then it will be
     * passed the key and the value.
     * <pre class="groovyTestCase">assert [2:3, 4:5, 5:10].any { key, value -> key * 2 == value }
     * assert ![2:3, 4:5, 5:10].any { entry -> entry.key == entry.value * 2 }</pre>
     *
     * @param self    the map over which we iterate
     * @param closure the 1 or 2 arg closure predicate used for matching
     * @return true if any entry in the map matches the closure predicate
     * @since 1.5.0
     */
    public static <K, V> boolean any(Map<K, V> self, Closure closure) {
        for (Map.Entry entry : self.entrySet()) {
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
     * @since 1.5.0
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
     * @param filter the filter to perform on the object (using the {@link #isCase(Object,Object)} method)
     * @return a collection of objects which match the filter
     * @since 1.5.6
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
     * @since 1.5.0
     */
    public static Number count(Iterator self, Object value) {
        long answer = 0;
        while (self.hasNext()) {
            if (DefaultTypeTransformation.compareEqual(self.next(), value)) {
                ++answer;
            }
        }
        // for b/c with Java return an int if we can
        if (answer <= Integer.MAX_VALUE) return new Long(answer).intValue();
        return answer;
    }

    /**
     * Counts the number of occurrences of the given value inside this collection.
     * Comparison is done using Groovy's == operator (using
     * <code>compareTo(value) == 0</code> or <code>equals(value)</code> ).
     * <pre class="groovyTestCase">assert [2,4,2,1,3,5,2,4,3].count(4) == 2</pre>
     *
     * @param self  the collection within which we count the number of occurrences
     * @param value the value being searched for
     * @return the number of occurrences
     * @since 1.0
     */
    public static Number count(Collection self, Object value) {
        return count(self.iterator(), value);
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
        return count(Arrays.asList(self), value);
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
        return count(InvokerHelper.asIterator(self), value);
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
        return count(InvokerHelper.asIterator(self), value);
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
        return count(InvokerHelper.asIterator(self), value);
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
        return count(InvokerHelper.asIterator(self), value);
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
    public static Number count(boolean[] self, Object value) {
        return count(InvokerHelper.asIterator(self), value);
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
        return count(InvokerHelper.asIterator(self), value);
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
        return count(InvokerHelper.asIterator(self), value);
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
        return count(InvokerHelper.asIterator(self), value);
    }

    /**
     * Convert a collection to a List.
     * <pre class="groovyTestCase">def x = [1,2,3] as HashSet
     * assert x.class == HashSet
     * assert x.toList() instanceof List</pre>
     *
     * @param self a collection
     * @return a List
     * @since 1.0
     */
    public static <T> List<T> toList(Collection<T> self) {
        List<T> answer = new ArrayList<T>(self.size());
        answer.addAll(self);
        return answer;
    }

    /**
     * Convert an iterator to a List. The iterator will become
     * exhausted of elements after making this conversion.
     *
     * @param self an iterator
     * @return a List
     * @since 1.5.0
     */
    public static <T> List<T> toList(Iterator<T> self) {
        List<T> answer = new ArrayList<T>();
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
     * @since 1.5.0
     */
    public static <T> List<T> toList(Enumeration<T> self) {
        List<T> answer = new ArrayList<T>();
        while (self.hasMoreElements()) {
            answer.add(self.nextElement());
        }
        return answer;
    }

    /**
     * Iterates through this object transforming each value into a new value using the
     * closure as a transformer, returning a list of transformed values.
     * Example:
     * <pre class="groovyTestCase">def list = [1, 'a', 1.23, true ]
     * def types = list.collect { it.class }
     * assert types == [Integer, String, BigDecimal, Boolean]</pre>
     *
     * @param self    the values of the object to transform
     * @param closure the closure used to transform each element of the collection
     * @return a List of the transformed values
     * @since 1.0
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
     * @since 1.0
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
     * <pre class="groovyTestCase">assert [2,4,6] == [1,2,3].collect { it * 2 }</pre>
     *
     * @param self    a collection
     * @param closure the closure used for mapping
     * @return a List of the transformed values
     * @since 1.0
     */
    public static List collect(Collection self, Closure closure) {
        return (List) collect(self, new ArrayList(self.size()), closure);
    }

    /**
     * Iterates through this collection transforming each value into a new value using the closure
     * as a transformer, returning an initial collection plus the transformed values.
     * <pre class="groovyTestCase">assert [1,2,3] as HashSet == [2,4,5,6].collect(new HashSet()) { (int)(it / 2) }</pre>
     *
     * @param self       a collection
     * @param collection an initial Collection to which the transformed values are added
     * @param closure    the closure used to transform each element of the collection
     * @return the resulting collection of transformed values
     * @since 1.0
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
     * <pre class="groovyTestCase">assert [2,[4,6],[8],[]] == [1,[2,3],[4],[]].collectAll { it * 2 }</pre>
     *
     * @param self       a collection
     * @param closure    the closure used to transform each element of the collection
     * @return the resultant collection
     * @since 1.5.2
     */
    public static List collectAll(Collection self, Closure closure) {
        return (List) collectAll(self, new ArrayList(self.size()), closure);
    }

    /**
     * Recursively iterates through this collection transforming each non-Collection value
     * into a new value using the closure as a transformer. Returns a potentially nested
     * collection of transformed values.
     * <pre class="groovyTestCase">def x = [1,[2,3],[4],[]].collectAll(new Vector()) { it * 2 }
     * assert x == [2,[4,6],[8],[]]
     * assert x instanceof Vector</pre>
     *
     * @param self       a collection
     * @param collection an initial Collection to which the transformed values are added
     * @param closure    the closure used to transform each element of the collection
     * @return the resultant collection
     * @since 1.5.2
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
     * <pre class="groovyTestCase">assert [a:1, b:2].collect( [] as HashSet ) { key, value -> key*value } == ["a", "bb"] as Set
     * assert [3:20, 2:30].collect( [] as HashSet ) { entry -> entry.key * entry.value } == [60] as Set</pre>
     *
     * @param self       a Map
     * @param collection the Collection to which the mapped values are added
     * @param closure    the closure used for mapping, which can take one (Map.Entry) or two (key, value) parameters
     * @return a List of the mapped values
     * @since 1.0
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
     * <pre class="groovyTestCase">assert [a:1, b:2].collect { key, value -> key*value } == ["a", "bb"]
     * assert [3:20, 2:30].collect { entry -> entry.key * entry.value } == [60, 60]</pre>
     *
     * @param self    a Map
     * @param closure the closure used to map each element of the collection
     * @return the resultant collection
     * @since 1.0
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
     * @since 1.0
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
     * <pre class="groovyTestCase">def list = [1,2,3]
     * assert 2 == list.find { it > 1 }
     * </pre>
     *
     * @param self    a Collection
     * @param closure a closure condition
     * @return the first Object found
     * @since 1.0
     */
    public static <T> T find(Collection<T> self, Closure closure) {
        for (T value : self) {
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
     * <pre class="groovyTestCase">assert [a:1, b:3].find { it.value == 3 }.key == "b"</pre>
     *
     * @param self    a Map
     * @param closure a 1 or 2 arg Closure condition
     * @return the first Object found
     * @since 1.0
     */
    public static <K, V> Map.Entry<K, V> find(Map<K, V> self, Closure closure) {
        for (Map.Entry<K, V> entry : self.entrySet()) {
            if (DefaultTypeTransformation.castToBoolean(callClosureForMapEntry(closure, entry))) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Finds all values matching the closure condition.
     * <pre class="groovyTestCase">assert [2,4] == [1,2,3,4].findAll { it % 2 == 0 }</pre>
     *
     * @param self    a Collection
     * @param closure a closure condition
     * @return a Collection of matching values
     * @since 1.5.6
     */
    public static <T> Collection<T> findAll(Collection<T> self, Closure closure) {
        Collection<T> answer = createSimilarCollection(self);
        Iterator<T> iter = self.iterator();
        return findAll(closure, answer, iter);
    }

    /**
     * Finds all items matching the closure condition.
     *
     * @param self    an Object with an Iterator returning its values
     * @param closure a closure condition
     * @return a List of the values found
     * @since 1.6.0
     */
    public static Collection findAll(Object self, Closure closure) {
        List answer = new ArrayList();
        Iterator iter = InvokerHelper.asIterator(self);
        return findAll(closure, answer, iter);
    }

    private static <T> Collection<T> findAll(Closure closure, Collection<T> answer, Iterator<T> iter) {
        while (iter.hasNext()) {
            T value = iter.next();
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
     * @since 1.6.0
     */
    public static Collection split(Object self, Closure closure) {
        List accept = new ArrayList();
        List reject = new ArrayList();
        return split(closure, accept, reject, InvokerHelper.asIterator(self));
    }

    /**
     * Splits all items into two collections based on the closure condition.
     * The first list contains all items which match the closure expression.
     * The second list all those that don't.
     * <pre class="groovyTestCase">assert [[2,4],[1,3]] == [1,2,3,4].split { it % 2 == 0 }</pre>
     *
     * @param self    a Collection of values
     * @param closure a closure condition
     * @return a List containing whose first item is the accepted values and whose second item is the rejected values
     * @since 1.6.0
     */
    public static <T> Collection<Collection<T>> split(Collection<T> self, Closure closure) {
        Collection<T> accept = createSimilarCollection(self);
        Collection<T> reject = createSimilarCollection(self);
        Iterator<T> iter = self.iterator();
        return split(closure, accept, reject, iter);
    }

    private static <T> Collection<Collection<T>> split(Closure closure, Collection<T> accept, Collection<T> reject, Iterator<T> iter) {
        List<Collection<T>> answer = new ArrayList<Collection<T>>();
        while (iter.hasNext()) {
            T value = iter.next();
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
     * <pre class="groovyTestCase">assert [['a', 'b'],[1, 2, 3]].combinations() == [['a', 1], ['b', 1], ['a', 2], ['b', 2], ['a', 3], ['b', 3]]</pre>
     *
     * @param self a Collection of lists
     * @return a List of the combinations found
     * @see groovy.util.GroovyCollections#combinations(java.util.Collection)
     * @since 1.5.0
     */
    public static List combinations(Collection self) {
        return GroovyCollections.combinations(self);
    }

    /**
     * Finds all non-null subsequences of a list.
     * E.g. <pre class="groovyTestCase">def result = [1, 2, 3].subsequences()
     * assert result == [[1, 2, 3], [1, 3], [2, 3], [1, 2], [1], [2], [3]] as Set</pre>
     *
     * @param self the List of items
     * @return the subsequences from the list
     * @since 1.7.0
     */
    public static <T> Set<List<T>> subsequences(List<T> self) {
        return GroovyCollections.subsequences(self);
    }

    /**
     * Finds all permutations of a collection.
     * E.g. <pre class="groovyTestCase">def result = [1, 2, 3].permutations()
     * assert result == [[3, 2, 1], [3, 1, 2], [1, 3, 2], [2, 3, 1], [2, 1, 3], [1, 2, 3]] as Set</pre>
     *
     * @param self the Collection of items
     * @return the permutations from the list
     * @since 1.7.0
     */
    public static <T> Set<List<T>> permutations(List<T> self) {
        Set<List<T>> ans = new HashSet<List<T>>();
        PermutationGenerator<T> generator = new PermutationGenerator<T>(self);
        while (generator.hasNext()) {
            ans.add(generator.next());
        }
        return ans;
    }

    /**
     * Iterates over all permutations of a collection, running a closure for each iteration.
     * <pre class="groovyTestCase">def permutations = []
     * [1, 2, 3].eachPermutation{ permutations << it }
     * assert permutations == [[1, 2, 3], [1, 3, 2], [2, 1, 3], [2, 3, 1], [3, 1, 2], [3, 2, 1]]</pre>
     *
     * @param self the Collection of items
     * @param closure the closure to call for each permutation
     * @return the permutations from the list
     * @since 1.7.0
     */
    public static <T> Iterator<List<T>> eachPermutation(Collection<T> self, Closure closure) {
        Iterator<List<T>> generator = new PermutationGenerator<T>(self);
        while (generator.hasNext()) {
            closure.call(generator.next());
        }
        return generator;
    }

    /**
     * Adds GroovyCollections#transpose(List) as a method on lists.
     * <pre class="groovyTestCase">def result = [['a', 'b'], [1, 2]].transpose()
     * assert result == [['a', 1], ['b', 2]]</pre>
     *
     * @param self a List of lists
     * @return a List of the transposed lists
     * @see groovy.util.GroovyCollections#transpose(java.util.List)
     * @since 1.5.0
     */
    public static List transpose(List self) {
        return GroovyCollections.transpose(self);
    }

    /**
     * Finds all entries matching the closure condition. If the
     * closure takes one parameter then it will be passed the Map.Entry.
     * Otherwise if the closure should take two parameters, which will be
     * the key and the value.
     * <p>
     * If the <code>self</code> map is one of TreeMap, LinkedHashMap, Hashtable
     * or Properties, the returned Map will preserve that type, otherwise a HashMap will
     * be returned.
     * <pre class="groovyTestCase">def result = [a:1, b:2, c:4, d:5].findAll { it.value % 2 == 0 }
     * assert result.every { it instanceof Map.Entry }
     * assert result*.key == ["b", "c"]
     * assert result*.value == [2, 4]</pre>
     *
     * @param self    a Map
     * @param closure a 1 or 2 arg Closure condition applying on the entries
     * @return a new subMap
     * @since 1.0
     */
    public static <K, V> Map<K, V> findAll(Map<K, V> self, Closure closure) {
        Map<K, V> answer = createSimilarMap(self);
        for (Map.Entry<K, V> entry : self.entrySet()) {
            if (DefaultTypeTransformation.castToBoolean(callClosureForMapEntry(closure, entry))) {
                answer.put(entry.getKey(), entry.getValue());
            }
        }
        return answer;
    }

    /**
     * Sorts all collection members into groups determined by the
     * supplied mapping closure.  The closure should return the key that this
     * item should be grouped by.  The returned LinkedHashMap will have an entry for each
     * distinct key returned from the closure, with each value being a list of
     * items for that group.
     * <pre class="groovyTestCase">assert [0:[2,4,6], 1:[1,3,5]] == [1,2,3,4,5,6].groupBy { it % 2 }</pre>
     *
     * @param self    a collection to group (no map)
     * @param closure a closure mapping entries on keys
     * @return a new Map grouped by keys
     * @since 1.0
     */
    public static <T> Map<Object, List<T>> groupBy(Collection<T> self, Closure closure) {
        Map<Object, List<T>> answer = new LinkedHashMap<Object, List<T>>();
        for (T element : self) {
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
     * <pre class="groovyTestCase">def result = [a:1,b:2,c:3,d:4,e:5,f:6].groupBy { it.value % 2 }
     * assert result[0]*.key == ["b", "d", "f"]
     * assert result[1]*.value == [1, 3, 5]</pre>
     *
     * @param self    a map to group
     * @param closure a 1 or 2 arg Closure mapping entries on keys
     * @return a new Map grouped by keys
     * @since 1.5.2
     */
    public static <K, V> Map<Object, List<Map.Entry<K, V>>> groupEntriesBy(Map<K, V> self, Closure closure) {
        final Map<Object, List<Map.Entry<K, V>>> answer = new LinkedHashMap<Object, List<Map.Entry<K, V>>>();
        for (Map.Entry<K, V> entry : self.entrySet()) {
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
     * <p>
     * If the <code>self</code> map is one of TreeMap, LinkedHashMap, Hashtable
     * or Properties, the returned Map will preserve that type, otherwise a HashMap will
     * be returned.
     * <pre class="groovyTestCase">def result = [a:1,b:2,c:3,d:4,e:5,f:6].groupBy { it.value % 2 }
     * assert result == [0:[b:2, d:4, f:6], 1:[a:1, c:3, e:5]]</pre>
     *
     * @param self    a map to group
     * @param closure a closure mapping entries on keys
     * @return a new Map grouped by keys
     * @since 1.0
     */
    public static <K, V> Map<Object, Map<K, V>> groupBy(Map<K, V> self, Closure closure) {
        final Map<Object, List<Map.Entry<K, V>>> initial = groupEntriesBy(self, closure);
        final Map<Object, Map<K, V>> answer = new LinkedHashMap<Object, Map<K, V>>();
        for (Map.Entry<Object, List<Map.Entry<K, V>>> outer : initial.entrySet()) {
            Object key = outer.getKey();
            List<Map.Entry<K, V>> entries = outer.getValue();
            Map<K, V> target = createSimilarMap(self);
            putAll(target, entries);
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
     * @since 1.5.0
     */
    protected static <T> void groupAnswer(final Map<Object, List<T>> answer, T element, Object value) {
        if (answer.containsKey(value)) {
            answer.get(value).add(element);
        } else {
            List<T> groupedElements = new ArrayList<T>();
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
            return closure.call(new Object[]{line, counter});
        }
        return closure.call(line);
    }

    // internal helper method
    protected static Object callClosureForMapEntryAndCounter(Closure closure, Map.Entry entry, int counter) {
        if (closure.getMaximumNumberOfParameters() == 3) {
            return closure.call(new Object[]{entry.getKey(), entry.getValue(), counter});
        }
        if (closure.getMaximumNumberOfParameters() == 2) {
            return closure.call(new Object[]{entry, counter});
        }
        return closure.call(entry);
    }


    /**
     * Iterates through the given collection, passing in the initial value to
     * the closure along with the current iterated item then passing into the
     * next iteration the value of the previous closure.
     * <pre class="groovyTestCase">assert 1*1*2*3*4 == [1,2,3,4].inject(1) { acc, val -> acc * val }</pre>
     *
     * @param self    a Collection
     * @param value   a value
     * @param closure a closure
     * @return the last value of the last iteration
     * @since 1.0
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
     * @since 1.5.0
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
     * @since 1.5.0
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
     * @since 1.5.0
     */
    public static Object inject(Object[] self, Object initialValue, Closure closure) {
        Object[] params = new Object[2];
        Object value = initialValue;
        for (Object next : self) {
            params[0] = value;
            params[1] = next;
            value = closure.call(params);
        }
        return value;
    }

    /**
     * Sums the items in a collection.  This is equivalent to invoking the
     * "plus" method on all items in the collection.
     * <pre class="groovyTestCase">assert 1+2+3+4 == [1,2,3,4].sum()</pre>
     *
     * @param self Collection of values to add together
     * @return The sum of all of the items
     * @since 1.0
     */
    public static Object sum(Collection self) {
        return sum(self, null, true);
    }

    /**
     * Sums the items in an array.  This is equivalent to invoking the
     * "plus" method on all items in the array.
     *
     * @param self The array of values to add together
     * @return The sum of all of the items
     * @see #sum(Collection)
     * @since 1.7.1
     */
    public static Object sum(Object[] self) {
        return sum(toList(self), null, true);
    }

    /**
     * Sums the items from an Iterator.  This is equivalent to invoking the
     * "plus" method on all items from the Iterator. The iterator will become
     * exhausted of elements after determining the sum value.
     *
     * @param self an Iterator for the values to add together
     * @return The sum of all of the items
     * @since 1.5.5
     */
    public static Object sum(Iterator<Object> self) {
        return sum(toList(self), null, true);
    }

    /**
     * Sums the items in a collection, adding the result to some initial value.
     * <pre class="groovyTestCase">assert 5+1+2+3+4 == [1,2,3,4].sum(5)</pre>
     *
     * @param self         a collection of values to sum
     * @param initialValue the items in the collection will be summed to this initial value
     * @return The sum of all of the items.
     * @since 1.5.0
     */
    public static Object sum(Collection self, Object initialValue) {
        return sum(self, initialValue, false);
    }

    /**
     * Sums the items in an array, adding the result to some initial value.
     *
     * @param self         an array of values to sum
     * @param initialValue the items in the array will be summed to this initial value
     * @return The sum of all of the items.
     * @since 1.7.1
     */
    public static Object sum(Object[] self, Object initialValue) {
        return sum(toList(self), initialValue, false);
    }

    /**
     * Sums the items from an Iterator, adding the result to some initial value.  This is
     * equivalent to invoking the "plus" method on all items from the Iterator. The iterator
     * will become exhausted of elements after determining the sum value.
     *
     * @param self         an Iterator for the values to add together
     * @param initialValue the items in the collection will be summed to this initial value
     * @return The sum of all of the items
     * @since 1.5.5
     */
    public static Object sum(Iterator<Object> self, Object initialValue) {
        return sum(toList(self), initialValue, false);
    }

    private static Object sum(Collection self, Object initialValue, boolean first) {
        Object result = initialValue;
        Object[] param = new Object[1];
        for (Object next : self) {
            param[0] = next;
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
     * <pre class="groovyTestCase">assert 4+6+10+12 == [2,3,5,6].sum() { it * 2 }</pre>
     *
     * @param self    a Collection
     * @param closure a single parameter closure that returns a numeric value.
     * @return The sum of the values returned by applying the closure to each
     *         item of the collection.
     * @since 1.0
     */
    public static Object sum(Collection self, Closure closure) {
        return sum(self, null, closure, true);
    }

    /**
     * Sums the result of apply a closure to each item of an array.
     * <code>array.sum(closure)</code> is equivalent to:
     * <code>array.collect(closure).sum()</code>.
     *
     * @param self    An array
     * @param closure a single parameter closure that returns a numeric value.
     * @return The sum of the values returned by applying the closure to each
     *         item of the array.
     * @since 1.7.1
     */
    public static Object sum(Object[] self, Closure closure) {
        return sum(toList(self), null, closure, true);
    }

    /**
     * Sums the result of apply a closure to each item returned from an iterator.
     * <code>iter.sum(closure)</code> is equivalent to:
     * <code>iter.collect(closure).sum()</code>. The iterator will become
     * exhausted of elements after determining the sum value.
     *
     * @param self    An Iterator
     * @param closure a single parameter closure that returns a numeric value.
     * @return The sum of the values returned by applying the closure to each
     *         item from the Iterator.
     * @since 1.7.1
     */
    public static Object sum(Iterator<Object> self, Closure closure) {
        return sum(toList(self), null, closure, true);
    }

    /**
     * Sums the result of applying a closure to each item of a collection to some initial value.
     * <code>coll.sum(initVal, closure)</code> is equivalent to:
     * <code>coll.collect(closure).sum(initVal)</code>.
     * <pre class="groovyTestCase">assert 50+4+6+10+12 == [2,3,5,6].sum(50) { it * 2 }</pre>
     *
     * @param self         a Collection
     * @param closure      a single parameter closure that returns a numeric value.
     * @param initialValue the closure results will be summed to this initial value
     * @return The sum of the values returned by applying the closure to each
     *         item of the collection.
     * @since 1.5.0
     */
    public static Object sum(Collection self, Object initialValue, Closure closure) {
        return sum(self, initialValue, closure, false);
    }

    /**
     * Sums the result of applying a closure to each item of an array to some initial value.
     * <code>array.sum(initVal, closure)</code> is equivalent to:
     * <code>array.collect(closure).sum(initVal)</code>.
     *
     * @param self         an array
     * @param closure      a single parameter closure that returns a numeric value.
     * @param initialValue the closure results will be summed to this initial value
     * @return The sum of the values returned by applying the closure to each
     *         item of the array.
     * @since 1.7.1
     */
    public static Object sum(Object[] self, Object initialValue, Closure closure) {
        return sum(toList(self), initialValue, closure, false);
    }

    /**
     * Sums the result of applying a closure to each item of an Iterator to some initial value.
     * <code>iter.sum(initVal, closure)</code> is equivalent to:
     * <code>iter.collect(closure).sum(initVal)</code>. The iterator will become
     * exhausted of elements after determining the sum value.
     *
     * @param self         an Iterator
     * @param closure      a single parameter closure that returns a numeric value.
     * @param initialValue the closure results will be summed to this initial value
     * @return The sum of the values returned by applying the closure to each
     *         item from the Iterator.
     * @since 1.7.1
     */
    public static Object sum(Iterator<Object> self, Object initialValue, Closure closure) {
        return sum(toList(self), initialValue, closure, false);
    }

    private static Object sum(Collection self, Object initialValue, Closure closure, boolean first) {
        Object result = initialValue;
        Object[] closureParam = new Object[1];
        Object[] plusParam = new Object[1];
        for (Object next : self) {
            closureParam[0] = next;
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
     * @since 1.5.5
     */
    public static String join(Iterator<Object> self, String separator) {
        return join(toList(self), separator);
    }

    /**
     * Concatenates the <code>toString()</code> representation of each
     * item in this collection, with the given String as a separator between
     * each item.
     * <pre class="groovyTestCase">assert "1, 2, 3" == [1,2,3].join(", ")</pre>
     *
     * @param self      a Collection of objects
     * @param separator a String separator
     * @return the joined String
     * @since 1.0
     */
    public static String join(Collection self, String separator) {
        StringBuilder buffer = new StringBuilder();
        boolean first = true;

        if (separator == null) separator = "";

        for (Object value : self) {
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
     * @since 1.0
     */
    public static String join(Object[] self, String separator) {
        StringBuilder buffer = new StringBuilder();
        boolean first = true;

        if (separator == null) separator = "";

        for (Object next : self) {
            String value = InvokerHelper.toString(next);
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
     * <pre class="groovyTestCase">assert 2 == [4,2,5].min()</pre>
     *
     * @param self a Collection
     * @return the minimum value
     * @see groovy.util.GroovyCollections#min(java.util.Collection)
     * @since 1.0
     */
    public static <T> T min(Collection<T> self) {
        return GroovyCollections.min(self);
    }

    /**
     * Adds min() method to Iterator objects. The iterator will become
     * exhausted of elements after determining the minimum value.
     *
     * @param self an Iterator
     * @return the minimum value
     * @see #min(java.util.Collection)
     * @since 1.5.5
     */
    public static <T> T min(Iterator<T> self) {
        return min(toList(self));
    }

    /**
     * Adds min() method to Object arrays.
     *
     * @param self an Object array
     * @return the minimum value
     * @see #min(java.util.Collection)
     * @since 1.5.5
     */
    public static <T> T min(T[] self) {
        return min(toList(self));
    }

    /**
     * Selects the minimum value found in the collection using the given comparator.
     * <pre class="groovyTestCase">assert "hi" == ["hello","hi","hey"].min( { a, b -> a.length() <=> b.length() } as Comparator )</pre>
     *
     * @param self       a Collection
     * @param comparator a Comparator
     * @return the minimum value
     * @since 1.0
     */
    public static <T> T min(Collection<T> self, Comparator<T> comparator) {
        T answer = null;
        for (T value : self) {
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
     * @since 1.5.5
     */
    public static <T> T min(Iterator<T> self, Comparator<T> comparator) {
        return min(toList(self), comparator);
    }

    /**
     * Selects the minimum value found from the Object array using the given comparator.
     *
     * @param self       an Object array
     * @param comparator a Comparator
     * @return the minimum value
     * @see #min(java.util.Collection, java.util.Comparator)
     * @since 1.5.5
     */
    public static <T> T min(T[] self, Comparator<T> comparator) {
        return min(toList(self), comparator);
    }

    /**
     * Selects the minimum value found in the collection
     * using the closure to determine the correct ordering.
     * </p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     * <pre class="groovyTestCase">assert "hi" == ["hello","hi","hey"].min { it.length() } </pre>
     *
     * @param self    a Collection
     * @param closure a 1 or 2 arg Closure used to determine the correct ordering
     * @return the minimum value
     * @since 1.0
     */
    public static <T> T min(Collection<T> self, Closure closure) {
        int params = closure.getMaximumNumberOfParameters();
        if (params != 1) {
            return min(self, new ClosureComparator<T>(closure));
        }
        T answer = null;
        Object answer_value = null;
        for (T item : self) {
            Object value = closure.call(item);
            if (answer == null || ScriptBytecodeAdapter.compareLessThan(value, answer_value)) {
                answer = item;
                answer_value = value;
            }
        }
        return answer;
    }

    /**
     * Selects the minimum value found from the Iterator
     * using the closure to determine the correct ordering.
     * The iterator will become
     * exhausted of elements after this operation.
     * </p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     *
     * @param self    an Iterator
     * @param closure a Closure used to determine the correct ordering
     * @return the minimum value
     * @see #min(java.util.Collection, groovy.lang.Closure)
     * @since 1.5.5
     */
    public static <T> T min(Iterator<T> self, Closure closure) {
        return min(toList(self), closure);
    }

    /**
     * Selects the minimum value found from the Object array
     * using the closure to determine the correct ordering.
     * </p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     *
     * @param self    an Object array
     * @param closure a Closure used to determine the correct ordering
     * @return the minimum value
     * @see #min(java.util.Collection, groovy.lang.Closure)
     * @since 1.5.5
     */
    public static <T> T min(T[] self, Closure closure) {
        return min(toList(self), closure);
    }

    /**
     * Adds max() method to Collection objects.
     * <pre class="groovyTestCase">assert 5 == [2,3,1,5,4].max()</pre>
     *
     * @param self a Collection
     * @return the maximum value
     * @see groovy.util.GroovyCollections#max(java.util.Collection)
     * @since 1.0
     */
    public static <T> T max(Collection<T> self) {
        return GroovyCollections.max(self);
    }

    /**
     * Adds max() method to Iterator objects. The iterator will become
     * exhausted of elements after determining the maximum value.
     *
     * @param self an Iterator
     * @return the maximum value
     * @see groovy.util.GroovyCollections#max(java.util.Collection)
     * @since 1.5.5
     */
    public static <T> T max(Iterator<T> self) {
        return max(toList(self));
    }

    /**
     * Adds max() method to Object arrays.
     *
     * @param self an Object array
     * @return the maximum value
     * @see #max(java.util.Collection)
     * @since 1.5.5
     */
    public static <T> T max(T[] self) {
        return max(toList(self));
    }

    /**
     * Selects the maximum value found in the collection
     * using the closure to determine the correct ordering.
     * </p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     * <pre class="groovyTestCase">assert "hello" == ["hello","hi","hey"].max { it.length() }</pre>
     * <pre class="groovyTestCase">assert "hello" == ["hello","hi","hey"].max { a, b -> a.length() <=> b.length() }</pre>
     *
     * @param self    a Collection
     * @param closure a 1 or 2 arg Closure used to determine the correct ordering
     * @return the maximum value
     * @since 1.0
     */
    public static <T> T max(Collection<T> self, Closure closure) {
        int params = closure.getMaximumNumberOfParameters();
        if (params != 1) {
            return max(self, new ClosureComparator<T>(closure));
        }
        T answer = null;
        Object answerValue = null;
        for (T item : self) {
            Object value = closure.call(item);
            if (answer == null || ScriptBytecodeAdapter.compareLessThan(answerValue, value)) {
                answer = item;
                answerValue = value;
            }
        }
        return answer;
    }

    /**
     * Selects the maximum value found from the Iterator
     * using the closure to determine the correct ordering.
     * The iterator will become exhausted of elements after this operation.
     * </p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     *
     * @param self    an Iterator
     * @param closure a Closure used to determine the correct ordering
     * @return the maximum value
     * @see #max(java.util.Collection, groovy.lang.Closure)
     * @since 1.5.5
     */
    public static <T> T max(Iterator<T> self, Closure closure) {
        return max(toList(self), closure);
    }

    /**
     * Selects the maximum value found from the Object array
     * using the closure to determine the correct ordering.
     * </p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     *
     * @param self    an Object array
     * @param closure a Closure used to determine the correct ordering
     * @return the maximum value
     * @see #max(java.util.Collection, groovy.lang.Closure)
     * @since 1.5.5
     */
    public static <T> T max(T[] self, Closure closure) {
        return max(toList(self), closure);
    }

    /**
     * Selects the maximum value found in the collection using the given comparator.
     * <pre class="groovyTestCase">assert "hello" == ["hello","hi","hey"].max( { a, b -> a.length() <=> b.length() } as Comparator )</pre>
     *
     * @param self       a Collection
     * @param comparator a Comparator
     * @return the maximum value
     * @since 1.0
     */
    public static <T> T max(Collection<T> self, Comparator<T> comparator) {
        T answer = null;
        for (T value : self) {
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
     * @since 1.5.5
     */
    public static <T> T max(Iterator<T> self, Comparator<T> comparator) {
        return max(toList(self), comparator);
    }

    /**
     * Selects the maximum value found from the Object array using the given comparator.
     *
     * @param self       an Object array
     * @param comparator a Comparator
     * @return the maximum value
     * @since 1.5.5
     */
    public static <T> T max(T[] self, Comparator<T> comparator) {
        return max(toList(self), comparator);
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>Iterator</code>.
     * The iterator will become exhausted of elements after determining the size value.
     *
     * @param self an Iterator
     * @return the length of the Iterator
     * @since 1.5.5
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
     * @since 1.0
     */
    public static int size(String text) {
        return text.length();
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>StringBuffer</code>.
     *
     * @param buffer a StringBuffer
     * @return the length of the StringBuffer
     * @since 1.0
     */
    public static int size(StringBuffer buffer) {
        return buffer.length();
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>File</code>.
     *
     * @param self a file object
     * @return the file's size (length)
     * @since 1.5.0
     */
    public static long size(File self) {
        return self.length();
    }


    /**
     * Provide the standard Groovy <code>size()</code> method for <code>Matcher</code>.
     *
     * @param self a matcher object
     * @return the matcher's size (count)
     * @since 1.5.0
     */
    public static long size(Matcher self) {
        return getCount(self);
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for an array.
     *
     * @param self an Array of objects
     * @return the size (length) of the Array
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.5.0
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
     * @since 1.0
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
     * @since 1.5.0
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
     * @since 1.0
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
     * @since 1.0
     * @see StringBuilder#reverse()
     */
    public static String reverse(String self) {
        return new StringBuilder(self).reverse().toString();
    }

    /**
     * Transforms a String representing a URL into a URL object.
     *
     * @param self the String representing a URL
     * @return a URL
     * @throws MalformedURLException is thrown if the URL is not well formed.
     * @since 1.0
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
     * @since 1.0
     */
    public static URI toURI(String self) throws URISyntaxException {
        return new URI(self);
    }

    /**
     * Turns a String into a regular expression Pattern
     *
     * @param self a String to convert into a regular expression
     * @return the regular expression pattern
     * @since 1.5.0
     */
    public static Pattern bitwiseNegate(String self) {
        return Pattern.compile(self);
    }

    /**
     * Replaces the first substring of a String that matches the given
     * compiled regular expression with the given replacement.
     * <p>
     * Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in the
     * replacement string may cause the results to be different than if it were
     * being treated as a literal replacement string; see
     * {@link java.util.regex.Matcher#replaceFirst}.
     * Use {@link java.util.regex.Matcher#quoteReplacement} to suppress the special
     * meaning of these characters, if desired.
     *
     * @param   self the string that is to be matched
     * @param   pattern the regex Pattern to which the string of interest is to be matched
     * @param   replacement the string to be substituted for the first match
     * @return  The resulting <tt>String</tt>
     * @see java.lang.String#replaceFirst(String, String)
     *
     * @since 1.6.1
     */
    public static String replaceFirst(String self, Pattern pattern, String replacement) {
        return pattern.matcher(self).replaceFirst(replacement);
    }

    /**
     * Replaces all substrings of a String that match the given
     * compiled regular expression with the given replacement.
     * <p>
     * Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in the
     * replacement string may cause the results to be different than if it were
     * being treated as a literal replacement string; see
     * {@link java.util.regex.Matcher#replaceAll}.
     * Use {@link java.util.regex.Matcher#quoteReplacement} to suppress the special
     * meaning of these characters, if desired.
     *
     * @param   self the string that is to be matched
     * @param   pattern the regex Pattern to which the string of interest is to be matched
     * @param   replacement the string to be substituted for the first match
     * @return  The resulting <tt>String</tt>
     * @see java.lang.String#replaceAll(String, String)
     * @since 1.6.1
     */
    public static String replaceAll(String self, Pattern pattern, String replacement) {
        return pattern.matcher(self).replaceAll(replacement);
    }

    /**
     * Tells whether or not self matches the given
     * compiled regular expression Pattern.
     *
     * @param   self the string that is to be matched
     * @param   pattern the regex Pattern to which the string of interest is to be matched
     * @return  The resulting <tt>String</tt>
     * @see java.lang.String#matches(String)
     * @since 1.6.1
     */
    public static boolean matches(String self, Pattern pattern) {
        return pattern.matcher(self).matches();
    }

    /**
     * Finds the first occurrence of a regular expression String within a String.
     * If the regex doesn't match, null will be returned.
     * <p/>
     * <p> For example, if the regex doesn't match the result is null:
     * <pre>
     *     assert null == "New York, NY".find(/\d{5}/)
     * </pre>
     * </p>
     * <p> If it does match, we get the matching string back:
     * <pre>
     *      assert "10292" == "New York, NY 10292-0098".find(/\d{5}/)
     * </pre>
     * </p>
     * <p> If we have capture groups in our expression, we still get back the full match
     * <pre>
     *      assert "10292-0098" == "New York, NY 10292-0098".find(/(\d{5})-?(\d{4})/)
     * </pre>
     * </p>
     *
     * @param self  a String
     * @param regex the capturing regex
     * @return a String containing the matched portion, or null if the regex doesn't match
     * @since 1.6.1
     */
    public static String find(String self, String regex) {
        return find(self, Pattern.compile(regex));
    }

    /**
     * Finds the first occurrence of a compiled regular expression Pattern within a String.
     * If the pattern doesn't match, null will be returned.
     * <p/>
     * <p> For example, if the pattern doesn't match the result is null:
     * <pre>
     *     assert null == "New York, NY".find(~/\d{5}/)
     * </pre>
     * </p>
     * <p> If it does match, we get the matching string back:
     * <pre>
     *      assert "10292" == "New York, NY 10292-0098".find(~/\d{5}/)
     * </pre>
     * </p>
     * <p> If we have capture groups in our expression, the groups are ignored and
     * we get back the full match:
     * <pre>
     *      assert "10292-0098" == "New York, NY 10292-0098".find(~/(\d{5})-?(\d{4})/)
     * </pre>
     * If you need to work with capture groups, then use the closure version
     * of this method or use Groovy's matcher operators or use <tt>eachMatch</tt>.
     * </p>
     *
     * @param self    a String
     * @param pattern the compiled regex Pattern
     * @return a String containing the matched portion, or null if the regex pattern doesn't match
     * @since 1.6.1
     */
    public static String find(String self, Pattern pattern) {
        Matcher matcher = pattern.matcher(self);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    /**
     * Finds the first occurrence of a regular expression String within a String.
     * If the regex doesn't match, the closure will not be called and find will return null.
     * <p/>
     * <p> For example, if the regex doesn't match, the result is null:
     * <pre>
     *     assert null == "New York, NY".find(~/\d{5}/) { match -> return "-$match-"}
     * </pre>
     * </p>
     * <p> If it does match and we don't have any capture groups in our regex, there is a single parameter
     * on the closure that the match gets passed to:
     * <pre>
     *      assert "-10292-" == "New York, NY 10292-0098".find(~/\d{5}/) { match -> return "-$match-"}
     * </pre>
     * </p>
     * <p> If we have capture groups in our expression, our closure has one parameter for the match, followed by
     * one for each of the capture groups:
     * <pre>
     *      assert "10292" == "New York, NY 10292-0098".find(~/(\d{5})-?(\d{4})/) { match, zip, plusFour ->
     *          assert match == "10292-0098"
     *          assert zip == "10292"
     *          assert plusFour == "0098"
     *          return zip
     *      }
     * </pre>
     * <p> If we have capture groups in our expression, and our closure has one parameter,
     * the closure will be passed an array with the first element corresponding to the whole match,
     * followed by an element for each of the capture groups:
     * <pre>
     *      assert "10292" == "New York, NY 10292-0098".find(~/(\d{5})-?(\d{4})/) { match, zip, plusFour ->
     *          assert array[0] == "10292-0098"
     *          assert array[1] == "10292"
     *          assert array[2] == "0098"
     *          return array[1]
     *      }
     * </pre>
     * <p> If a capture group is optional, and doesn't match, then the corresponding value
     * for that capture group passed to the closure will be null as illustrated here:
     * <pre>
     *      assert "2339999" == "adsf 233-9999 adsf".find(~/(\d{3})?-?(\d{3})-(\d{4})/) { match, areaCode, exchange, stationNumber ->
     *          assert "233-9999" == match
     *          assert null == areaCode
     *          assert "233" == exchange
     *          assert "9999" == stationNumber
     *          return "$exchange$stationNumber"
     *      }
     * </pre>
     * </p>
     *
     * @param self    a String
     * @param regex   the capturing regex string
     * @param closure the closure that will be passed the full match, plus each of the capturing groups
     * @return a String containing the result of the closure, or null if the regex pattern doesn't match
     * @since 1.6.1
     */
    public static String find(String self, String regex, Closure closure) {
        return find(self, Pattern.compile(regex), closure);
    }

    /**
     * Finds the first occurrence of a compiled regular expression Pattern within a String.
     * If the pattern doesn't match, the closure will not be called and find will return null.
     * <p/>
     * <p> For example, if the pattern doesn't match, the result is null:
     * <pre>
     *     assert null == "New York, NY".find(~/\d{5}/) { match -> return "-$match-"}
     * </pre>
     * </p>
     * <p> If it does match and we don't have any capture groups in our regex, there is a single parameter
     * on the closure that the match gets passed to:
     * <pre>
     *      assert "-10292-" == "New York, NY 10292-0098".find(~/\d{5}/) { match -> return "-$match-"}
     * </pre>
     * </p>
     * <p> If we have capture groups in our expression, our closure has one parameter for the match, followed by
     * one for each of the capture groups:
     * <pre>
     *      assert "10292" == "New York, NY 10292-0098".find(~/(\d{5})-?(\d{4})/) { match, zip, plusFour ->
     *          assert match == "10292-0098"
     *          assert zip == "10292"
     *          assert plusFour == "0098"
     *          return zip
     *      }
     * </pre>
     * <p> If we have capture groups in our expression, and our closure has one parameter,
     * the closure will be passed an array with the first element corresponding to the whole match,
     * followed by an element for each of the capture groups:
     * <pre>
     *      assert "10292" == "New York, NY 10292-0098".find(~/(\d{5})-?(\d{4})/) { match, zip, plusFour ->
     *          assert array[0] == "10292-0098"
     *          assert array[1] == "10292"
     *          assert array[2] == "0098"
     *          return array[1]
     *      }
     * </pre>
     * <p> If a capture group is optional, and doesn't match, then the corresponding value
     * for that capture group passed to the closure will be null as illustrated here:
     * <pre>
     *      assert "2339999" == "adsf 233-9999 adsf".find(~/(\d{3})?-?(\d{3})-(\d{4})/) { match, areaCode, exchange, stationNumber ->
     *          assert "233-9999" == match
     *          assert null == areaCode
     *          assert "233" == exchange
     *          assert "9999" == stationNumber
     *          return "$exchange$stationNumber"
     *      }
     * </pre>
     * </p>
     *
     * @param self    a String
     * @param pattern the compiled regex Pattern
     * @param closure the closure that will be passed the full match, plus each of the capturing groups
     * @return a String containing the result of the closure, or null if the regex pattern doesn't match
     * @since 1.6.1
     */
    public static String find(String self, Pattern pattern, Closure closure) {
        Matcher matcher = pattern.matcher(self);
        if (matcher.find()) {
            if (hasGroup(matcher)) {
                int count = matcher.groupCount();
                List groups = new ArrayList(count);
                for (int i = 0; i <= count; i++) {
                    groups.add(matcher.group(i));
                }
                return InvokerHelper.toString(closure.call(groups));
            } else {
                return InvokerHelper.toString(closure.call(matcher.group(0)));
            }
        }
        return null;
    }

    /**
     * Finds all occurrences of a regular expression string within a String.  A List is returned containing all full matches or
     * an empty list if there are no matches within the string.
     * <p/>
     * <p>For example, if the regex doesn't match, it returns an empty list:
     * <pre>
     * assert [] == "foo".findAll(/(\w*) Fish/)
     * </pre>
     * <p>Any regular expression matches are returned in a list, and all regex capture groupings are ignored, only the full match is returned:
     * <pre>
     * def expected = ["One Fish", "Two Fish", "Red Fish", "Blue Fish"]
     * assert expected == "One Fish, Two Fish, Red Fish, Blue Fish".findAll(/(\w*) Fish/)
     * </pre>
     * If you need to work with capture groups, then use the closure version
     * of this method or use Groovy's matcher operators or use <tt>eachMatch</tt>.
     * </p>
     *
     * @param self  a String
     * @param regex the capturing regex String
     * @return a List containing all full matches of the regex within the string, an empty list will be returned if there are no matches
     * @since 1.6.1
     */
    public static List findAll(String self, String regex) {
        return findAll(self, Pattern.compile(regex));
    }

    /**
     * Finds all occurrences of a regular expression Pattern within a String.  A List is returned containing all full matches or
     * an empty list if there are no matches within the string.
     * <p/>
     * <p>For example, if the pattern doesn't match, it returns an empty list:
     * <pre>
     * assert [] == "foo".findAll(~/(\w*) Fish/)
     * </pre>
     * <p>Any regular expression matches are returned in a list, and all regex capture groupings are ignored, only the full match is returned:
     * <pre>
     * def expected = ["One Fish", "Two Fish", "Red Fish", "Blue Fish"]
     * assert expected == "One Fish, Two Fish, Red Fish, Blue Fish".findAll(~/(\w*) Fish/)
     * </pre>
     *
     * @param self    a String
     * @param pattern the compiled regex Pattern
     * @return a List containing all full matches of the Pattern within the string, an empty list will be returned if there are no matches
     * @since 1.6.1
     */
    public static List findAll(String self, Pattern pattern) {
        Matcher matcher = pattern.matcher(self);
        List list = new ArrayList();
        for (Iterator iter = iterator(matcher); iter.hasNext();) {
            if (hasGroup(matcher)) {
                list.add(((List) iter.next()).get(0));
            } else {
                list.add(iter.next());
            }
        }
        return list;
    }

    /**
     * Finds all occurrences of a regular expression string within a String.   Any matches are passed to the specified closure.  The closure
     * is expected to have the full match in the first parameter.  If there are any capture groups, they will be placed in subsequent parameters.
     * <p/>
     * If there are no matches, the closure will not be called, and an empty List will be returned.
     * <p/>
     * <p>For example, if the regex doesn't match, it returns an empty list:
     * <pre>
     * assert [] == "foo".findAll(/(\w*) Fish/) { match, firstWord -> return firstWord }
     * </pre>
     * <p>Any regular expression matches are passed to the closure, if there are no capture groups, there will be one parameter for the match:
     * <pre>
     * assert ["couldn't", "wouldn't"] == "I could not, would not, with a fox.".findAll(/.ould/) { match -> "${match}n't"}
     * </pre>
     * <p>If there are capture groups, the first parameter will be the match followed by one parameter for each capture group:
     * <pre>
     * def orig = "There's a Wocket in my Pocket"
     * assert ["W > Wocket", "P > Pocket"] == orig.findAll(/(.)ocket/) { match, firstLetter -> "$firstLetter > $match" }
     * </pre>
     *
     * @param self    a String
     * @param regex   the capturing regex String
     * @param closure will be passed the full match plus each of the capturing groups
     * @return a List containing all full matches of the regex within the string, an empty list will be returned if there are no matches
     * @since 1.6.1
     */
    public static List findAll(String self, String regex, Closure closure) {
        return findAll(self, Pattern.compile(regex), closure);
    }

    /**
     * Finds all occurrences of a compiled regular expression Pattern within a String.   Any matches are passed to the specified closure.  The closure
     * is expected to have the full match in the first parameter.  If there are any capture groups, they will be placed in subsequent parameters.
     * <p/>
     * If there are no matches, the closure will not be called, and an empty List will be returned.
     * <p/>
     * <p>For example, if the pattern doesn't match, it returns an empty list:
     * <pre>
     * assert [] == "foo".findAll(~/(\w*) Fish/) { match, firstWord -> return firstWord }
     * </pre>
     * <p>Any regular expression matches are passed to the closure, if there are no capture groups, there will be one parameter for the match:
     * <pre>
     * assert ["couldn't", "wouldn't"] == "I could not, would not, with a fox.".findAll(~/.ould/) { match -> "${match}n't"}
     * </pre>
     * <p>If there are capture groups, the first parameter will be the match followed by one parameter for each capture group:
     * <pre>
     * def orig = "There's a Wocket in my Pocket"
     * assert ["W > Wocket", "P > Pocket"] == orig.findAll(~/(.)ocket/) { match, firstLetter -> "$firstLetter > $match" }
     * </pre>
     *
     * @param self    a String
     * @param pattern the compiled regex Pattern
     * @param closure will be passed the full match plus each of the capturing groups
     * @return a List containing all full matches of the regex Pattern within the string, an empty list will be returned if there are no matches
     * @since 1.6.1
     */
    public static List findAll(String self, Pattern pattern, Closure closure) {
        Matcher matcher = pattern.matcher(self);
        return collect(matcher, closure);
    }

    /**
     * Replaces all occurrences of a captured group by the result of a closure on that text.
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
     * <p>Note that unlike String.replaceAll(String regex, String replacement), where the replacement string
     * treats '$' and '\' specially (for group substitution), the result of the closure is converted to a string
     * and that value is used literally for the replacement.</p>
     *
     * @param self    a String
     * @param regex   the capturing regex
     * @param closure the closure to apply on each captured group
     * @return a String with replaced content
     * @since 1.0
     * @see java.util.regex.Matcher#quoteReplacement(String)
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
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            } while (matcher.find());
            matcher.appendTail(sb);
            return sb.toString();
        } else {
            return self;
        }
    }

    private static String getPadding(String padding, int length) {
        if (padding.length() < length) {
            return multiply(padding, length / padding.length() + 1).substring(0, length);
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
     */
    public static String center(String self, Number numberOfChars) {
        return center(self, numberOfChars, " ");
    }

    /**
     * Support the subscript operator, e.g.&nbsp;matcher[index], for a regex Matcher.
     * <p/>
     * For an example using no group match,
     * <pre>
     *    def p = /ab[d|f]/
     *    def m = "abcabdabeabf" =~ p
     *    assert 2 == m.count
     *    assert 2 == m.size() // synonym for m.getCount()
     *    assert ! m.hasGroup()
     *    assert 0 == m.groupCount()
     *    def matches = ["abd", "abf"]
     *    for (i in 0..&lt;m.count) {
     *    &nbsp;&nbsp;assert m[i] == matches[i]
     *    }
     * </pre>
     * <p/>
     * For an example using group matches,
     * <pre>
     *    def p = /(?:ab([c|d|e|f]))/
     *    def m = "abcabdabeabf" =~ p
     *    assert 4 == m.count
     *    assert m.hasGroup()
     *    assert 1 == m.groupCount()
     *    def matches = [["abc", "c"], ["abd", "d"], ["abe", "e"], ["abf", "f"]]
     *    for (i in 0..&lt;m.count) {
     *    &nbsp;&nbsp;assert m[i] == matches[i]
     *    }
     * </pre>
     * <p/>
     * For another example using group matches,
     * <pre>
     *    def m = "abcabdabeabfabxyzabx" =~ /(?:ab([d|x-z]+))/
     *    assert 3 == m.count
     *    assert m.hasGroup()
     *    assert 1 == m.groupCount()
     *    def matches = [["abd", "d"], ["abxyz", "xyz"], ["abx", "x"]]
     *    for (i in 0..&lt;m.count) {
     *    &nbsp;&nbsp;assert m[i] == matches[i]
     *    }
     * </pre>
     *
     * @param matcher a Matcher
     * @param idx     an index
     * @return object a matched String if no groups matched, list of matched groups otherwise.
     * @since 1.0
     */
    public static Object getAt(Matcher matcher, int idx) {
        try {
            int count = getCount(matcher);
            if (idx < -count || idx >= count) {
                throw new IndexOutOfBoundsException("index is out of range " + (-count) + ".." + (count - 1) + " (index = " + idx + ")");
            }
            idx = normaliseIndex(idx, count);

            Iterator iter = iterator(matcher);
            Object result = null;
            for (int i = 0; i <= idx; i++) {
                result = iter.next();
            }
            return result;
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
     * @since 1.0
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
     * @since 1.0
     */
    public static int getCount(Matcher matcher) {
        int counter = 0;
        matcher.reset();
        while (matcher.find()) {
            counter++;
        }
        return counter;
    }

    /**
     * Check whether a Matcher contains a group or not.
     *
     * @param matcher a Matcher
     * @return boolean  <code>true</code> if matcher contains at least one group.
     * @since 1.0
     */
    public static boolean hasGroup(Matcher matcher) {
        return matcher.groupCount() > 0;
    }

    /**
     * Support the range subscript operator for a List
     * <pre class="groovyTestCase">def list = [1, "a", 4.5, true]
     * assert list[1..2] == ["a", 4.5]</pre>
     *
     * @param self  a List
     * @param range a Range indicating the items to get
     * @return a sublist based on range borders or a new list if range is reversed
     * @see java.util.List#subList(int,int)
     * @since 1.0
     */
    public static <T> List<T> getAt(List<T> self, Range range) {
        RangeInfo info = subListBorders(self.size(), range);
        List<T> answer = self.subList(info.from, info.to);  // sublist is always exclusive, but Ranges are not
        if (info.reverse) {
            answer = reverse(answer);
        }
        return answer;
    }

    /**
     * Support the range subscript operator for a List
     * <pre class="groovyTestCase">def list = [true, 1, 3.4]
     * assert list[0..<0] == []</pre>
     *
     * @param self  a List
     * @param range a Range indicating the items to get
     * @return a sublist based on range borders or a new list if range is reversed
     * @see java.util.List#subList(int,int)
     * @since 1.0
     */
    public static <T> List<T> getAt(List<T> self, EmptyRange range) {
        return new ArrayList<T> ();
    }

    /**
     * Select a List of items from a List using a Collection to
     * identify the indices to be selected.
     * <pre class="groovyTestCase">def list = [true, 1, 3.4, false]
     * assert list[1,0,2] == [1, true, 3.4]</pre>
     *
     * @param self    a List
     * @param indices a Collection of indices
     * @return a new list of the values at the given indices
     * @since 1.0
     */
    public static <T> List<T> getAt(List<T> self, Collection indices) {
        List<T> answer = new ArrayList<T>(indices.size());
        for (Object value : indices) {
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
     * @since 1.0
     */
    public static <T> List<T> getAt(T[] self, Collection indices) {
        List<T> answer = new ArrayList<T>(indices.size());
        for (Object value : indices) {
            if (value instanceof Range) {
                answer.addAll(getAt(self, (Range) value));
            } else if (value instanceof Collection) {
                answer.addAll(getAt(self, (Collection) value));
            } else {
                int idx = DefaultTypeTransformation.intUnbox(value);
                answer.add(getAtImpl(self, idx));
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
     * @since 1.0
     */
    public static CharSequence getAt(CharSequence self, Collection indices) {
        StringBuilder answer = new StringBuilder();
        for (Object value : indices) {
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
     * @since 1.0
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
     * @since 1.6.0
     */
    public static List getAt(Matcher self, Collection indices) {
        List result = new ArrayList();
        for (Object value : indices) {
            if (value instanceof Range) {
                result.addAll(getAt(self, (Range) value));
            } else {
                int idx = DefaultTypeTransformation.intUnbox(value);
                result.add(getAt(self, idx));
            }
        }
        return result;
    }

    /**
     * Creates a sub-Map containing the given keys. This method is similar to
     * List.subList() but uses keys rather than index ranges.
     * <pre class="groovyTestCase">assert [1:10, 2:20, 4:40].subMap( [2, 4] ) == [2:20, 4:40]</pre>
     *
     * @param map  a Map
     * @param keys a Collection of keys
     * @return a new Map containing the given keys
     * @since 1.0
     */
    public static <K, V> Map<K, V> subMap(Map<K, V> map, Collection<K> keys) {
        Map<K, V> answer = new LinkedHashMap<K, V>(keys.size());
        for (K key : keys) {
            answer.put(key, map.get(key));
        }
        return answer;
    }

    /**
     * Looks up an item in a Map for the given key and returns the value - unless
     * there is no entry for the given key in which case add the default value
     * to the map and return that.
     * <pre class="groovyTestCase">def map=[:]
     * map.get("a", []) << 5
     * assert map == [a:[5]]</pre>
     *
     * @param map          a Map
     * @param key          the key to lookup the value of
     * @param defaultValue the value to return and add to the map for this key if
     *                     there is no entry for the given key
     * @return the value of the given key or the default value, added to the map if the
     *         key did not exist
     * @since 1.0
     */
    public static <K, V> V get(Map<K, V> map, K key, V defaultValue) {
        if (!map.containsKey(key)) {
            map.put(key, defaultValue);
        }
        return map.get(key);
    }

    /**
     * Support the range subscript operator for an Array
     *
     * @param array an Array of Objects
     * @param range a Range
     * @return a range of a list from the range's from index up to but not
     *         including the range's to value
     * @since 1.0
     */
    public static <T> List<T> getAt(T[] array, Range range) {
        List<T> list = Arrays.asList(array);
        return getAt(list, range);
    }

    /**
     *
     * @param array an Array of Objects
     * @param range an IntRange
     * @return a range of a list from the range's from index up to but not
     *         including the range's to value
     * @since 1.0
     */
    public static <T> List<T> getAt(T[] array, IntRange range) {
        List<T> list = Arrays.asList(array);
        return getAt(list, range);
    }

    /**
     *
     * @param array an Array of Objects
     * @param range an EmptyRange
     * @return an empty Range
     * @since 1.5.0
     */
    public static <T> List<T> getAt(T[] array, EmptyRange range) {
        return new ArrayList<T>();
    }

    /**
     *
     * @param array an Array of Objects
     * @param range an ObjectRange
     * @return a range of a list from the range's from index up to but not
     *         including the range's to value
     * @since 1.0
     */
    public static <T> List<T> getAt(T[] array, ObjectRange range) {
        List<T> list = Arrays.asList(array);
        return getAt(list, range);
    }

    private static <T> T getAtImpl(T[] array, int idx) {
        return array[normaliseIndex(idx, array.length)];
    }

    /**
     * Allows conversion of arrays into a mutable List.
     *
     * @param array an Array of Objects
     * @return the array as a List
     * @since 1.0
     */
    public static <T> List<T> toList(T[] array) {
        return new ArrayList<T>(Arrays.asList(array));
    }

    /**
     * Support the subscript operator for a List.
     * <pre class="groovyTestCase">def list = [2, "a", 5.3]
     * assert list[1] == "a"</pre>
     *
     * @param self a List
     * @param idx  an index
     * @return the value at the given index
     * @since 1.0
     */
    public static <T> T getAt(List<T> self, int idx) {
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
     * @since 1.5.5
     */
    public static int getAt(Date self, int field) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(self);
        return cal.get(field);
    }

    /**
     * A helper method to allow lists to work with subscript operators.
     * <pre class="groovyTestCase">def list = [2, 3]
     * list[0] = 1
     * assert list == [1, 3]</pre>
     *
     * @param self  a List
     * @param idx   an index
     * @param value the value to put at the given index
     * @since 1.0
     */
    public static <T> void putAt(List<T> self, int idx, T value) {
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
     * @since 1.0
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
     * @since 1.0
     */
    public static void putAt(StringBuffer self, EmptyRange range, Object value) {
        RangeInfo info = subListBorders(self.length(), range);
        self.replace(info.from, info.to, value.toString());
    }

    /**
     * A helper method to allow lists to work with subscript operators.
     * <pre class="groovyTestCase">def list = ["a", true]
     * list[1..<1] = 5
     * assert list == ["a", 5, true]</pre>
     *
     * @param self  a List
     * @param range the (in this case empty) subset of the list to set
     * @param value the values to put at the given sublist or a Collection of values
     * @since 1.0
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

    /**
     * A helper method to allow lists to work with subscript operators.
     * <pre class="groovyTestCase">def list = ["a", true]
     * list[1..<1] = [4, 3, 2]
     * assert list == ["a", 4, 3, 2, true]</pre>
     *
     * @param self  a List
     * @param range the (in this case empty) subset of the list to set
     * @param value the Collection of values
     * @since 1.0
     * @see #putAt(List, EmptyRange, Object)
     */
    public static void putAt(List self, EmptyRange range, Collection value) {
    	putAt(self, range, (Object)value);
    }

    private static <T> List<T> resizeListWithRangeAndGetSublist(List<T> self, IntRange range) {
        RangeInfo info = subListBorders(self.size(), range);
        int size = self.size();
        if (info.to >= size) {
            while (size < info.to) {
                self.add(size++, null);
            }
        }
        List<T> sublist = self.subList(info.from, info.to);
        sublist.clear();
        return sublist;
    }

    /**
     * List subscript assignment operator when given a range as the index and
     * the assignment operand is a collection.
     * Example: <pre class="groovyTestCase">def myList = [4, 3, 5, 1, 2, 8, 10]
     * myList[3..5] = ["a", true]
     * assert myList == [4, 3, 5, "a", true, 10]</pre>
     *
     * Items in the given
     * range are relaced with items from the collection.
     *
     * @param self  a List
     * @param range the subset of the list to set
     * @param col   the collection of values to put at the given sublist
     * @since 1.5.0
     */
    public static void putAt(List self, IntRange range, Collection col) {
        List sublist = resizeListWithRangeAndGetSublist(self, range);
        if (col.isEmpty()) return;
        sublist.addAll(col);
    }

    /**
     * List subscript assignment operator when given a range as the index.
     * Example: <pre class="groovyTestCase">def myList = [4, 3, 5, 1, 2, 8, 10]
     * myList[3..5] = "b"
     * assert myList == [4, 3, 5, "b", 10]</pre>
     *
     * Items in the given
     * range are relaced with the operand.  The <code>value</code> operand is
     * always treated as a single value.
     *
     * @param self  a List
     * @param range the subset of the list to set
     * @param value the value to put at the given sublist
     * @since 1.0
     */
    public static void putAt(List self, IntRange range, Object value) {
        List sublist = resizeListWithRangeAndGetSublist(self, range);
        sublist.add(value);
    }

    /**
     * A helper method to allow lists to work with subscript operators.
     * <pre class="groovyTestCase">def list = ["a", true, 42, 9.4]
     * list[1, 4] = ["x", false]
     * assert list == ["a", "x", 42, 9.4, false]</pre>
     *
     * @param self   a List
     * @param splice the subset of the list to set
     * @param values the value to put at the given sublist
     * @since 1.0
     */
    public static void putAt(List self, List splice, List values) {
        if (splice.isEmpty()) {
            if ( ! values.isEmpty() )
                throw new IllegalArgumentException("Trying to replace 0 elements with "+values.size()+" elements");
            return;
        }
        Object first = splice.iterator().next();
        if (first instanceof Integer) {
            if (values.size() != splice.size())
                throw new IllegalArgumentException("Trying to replace "+splice.size()+" elements with "+values.size()+" elements");
            Iterator<?> valuesIter = values.iterator();
            for (Object index : splice) {
                putAt(self, (Integer) index, valuesIter.next());
            }
        } else {
            throw new IllegalArgumentException("Can only index a List with another List of Integers, not a List of "+first.getClass().getName());
        }
    }

    /**
     * A helper method to allow lists to work with subscript operators.
     * <pre class="groovyTestCase">def list = ["a", true, 42, 9.4]
     * list[1, 3] = 5
     * assert list == ["a", 5, 42, 5]</pre>
     *
     * @param self   a List
     * @param splice the subset of the list to set
     * @param value  the value to put at the given sublist
     * @since 1.0
     */
    public static void putAt(List self, List splice, Object value) {
        if (splice.isEmpty()) {
            return;
        }
        Object first = splice.iterator().next();
        if (first instanceof Integer) {
            for (Object index : splice) {
                self.set((Integer) index, value);
            }
        } else {
            throw new IllegalArgumentException("Can only index a List with another List of Integers, not a List of "+first.getClass().getName());
        }
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
     * <pre class="groovyTestCase">def map = [a:10]
     * assert map["a"] == 10</pre>
     *
     * @param self a Map
     * @param key  an Object as a key for the map
     * @return the value corresponding to the given key
     * @since 1.0
     */
    public static <K,V> V getAt(Map<K,V> self, K key) {
        return self.get(key);
    }

    /**
     * Returns a new <code>Map</code> containing all entries from <code>left</code> and <code>right</code>,
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
     * <pre class="groovyTestCase">assert [a:10, b:20] + [a:5, c:7] == [a:5, b:20, c:7]</pre>
     *
     * @param left  a Map
     * @param right a Map
     * @return a new Map containing all entries from left and right
     * @since 1.5.0
     */
    public static <K, V> Map<K, V> plus(Map<K, V> left, Map<K, V> right) {
        Map<K, V> map = cloneSimilarMap(left);
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
     * @since 1.0
     */
    public static <K,V> V putAt(Map<K,V> self, K key, V value) {
        self.put(key, value);
        return value;
    }

    /**
     * Support the subscript operator for List
     * <pre class="groovyTestCase">assert [String, Long, Integer] == ["a",5L,2]["class"]</pre>
     *
     * @param coll     a Collection
     * @param property a String
     * @return a List
     * @since 1.0
     */
    public static List getAt(Collection coll, String property) {
        List answer = new ArrayList(coll.size());
        for (Object item : coll) {
            if (item == null) continue;
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
     * @since 1.0
     */
    public static <K,V> Map<K,V> asImmutable(Map<? extends K, ? extends V> self) {
        return Collections.unmodifiableMap(self);
    }

    /**
     * A convenience method for creating an immutable sorted map.
     *
     * @param self a SortedMap
     * @return an immutable SortedMap
     * @see java.util.Collections#unmodifiableSortedMap(java.util.SortedMap)
     * @since 1.0
     */
    public static <K,V> SortedMap<K,V> asImmutable(SortedMap<K, ? extends V> self) {
        return Collections.unmodifiableSortedMap(self);
    }

    /**
     * A convenience method for creating an immutable list
     *
     * @param self a List
     * @return an immutable List
     * @see java.util.Collections#unmodifiableList(java.util.List)
     * @since 1.0
     */
    public static <T> List<T> asImmutable(List<? extends T> self) {
        return Collections.unmodifiableList(self);
    }

    /**
     * A convenience method for creating an immutable list.
     *
     * @param self a Set
     * @return an immutable Set
     * @see java.util.Collections#unmodifiableSet(java.util.Set)
     * @since 1.0
     */
    public static <T> Set<T> asImmutable(Set<? extends T> self) {
        return Collections.unmodifiableSet(self);
    }

    /**
     * A convenience method for creating an immutable sorted set.
     *
     * @param self a SortedSet
     * @return an immutable SortedSet
     * @see java.util.Collections#unmodifiableSortedSet(java.util.SortedSet)
     * @since 1.0
     */
    public static <T> SortedSet<T> asImmutable(SortedSet<T> self) {
        return Collections.unmodifiableSortedSet(self);
    }

    /**
     * A convenience method for creating an immutable Collection.
     * <pre class="groovyTestCase">def mutable = [1,2,3]
     * def immutable = mutable.asImmutable()
     * mutable << 4
     * try {
     *   immutable << 4
     *   assert false
     * } catch (UnsupportedOperationException) {
     *   assert true
     * }</pre>
     *
     * @param self a Collection
     * @return an immutable Collection
     * @see java.util.Collections#unmodifiableCollection(java.util.Collection)
     * @since 1.5.0
     */
    public static <T> Collection<T> asImmutable(Collection<? extends T> self) {
        return Collections.unmodifiableCollection(self);
    }

    /**
     * A convenience method for creating a synchronized Map.
     *
     * @param self a Map
     * @return a synchronized Map
     * @see java.util.Collections#synchronizedMap(java.util.Map)
     * @since 1.0
     */
    public static <K,V> Map<K,V> asSynchronized(Map<K,V> self) {
        return Collections.synchronizedMap(self);
    }

    /**
     * A convenience method for creating a synchronized SortedMap.
     *
     * @param self a SortedMap
     * @return a synchronized SortedMap
     * @see java.util.Collections#synchronizedSortedMap(java.util.SortedMap)
     * @since 1.0
     */
    public static <K,V> SortedMap<K,V> asSynchronized(SortedMap<K,V> self) {
        return Collections.synchronizedSortedMap(self);
    }

    /**
     * A convenience method for creating a synchronized Collection.
     *
     * @param self a Collection
     * @return a synchronized Collection
     * @see java.util.Collections#synchronizedCollection(java.util.Collection)
     * @since 1.0
     */
    public static <T> Collection<T> asSynchronized(Collection<T> self) {
        return Collections.synchronizedCollection(self);
    }

    /**
     * A convenience method for creating a synchronized List.
     *
     * @param self a List
     * @return a synchronized List
     * @see java.util.Collections#synchronizedList(java.util.List)
     * @since 1.0
     */
    public static <T> List<T> asSynchronized(List<T> self) {
        return Collections.synchronizedList(self);
    }

    /**
     * A convenience method for creating a synchronized Set.
     *
     * @param self a Set
     * @return a synchronized Set
     * @see java.util.Collections#synchronizedSet(java.util.Set)
     * @since 1.0
     */
    public static <T> Set<T> asSynchronized(Set<T> self) {
        return Collections.synchronizedSet(self);
    }

    /**
     * A convenience method for creating a synchronized SortedSet.
     *
     * @param self a SortedSet
     * @return a synchronized SortedSet
     * @see java.util.Collections#synchronizedSortedSet(java.util.SortedSet)
     * @since 1.0
     */
    public static <T> SortedSet<T> asSynchronized(SortedSet<T> self) {
        return Collections.synchronizedSortedSet(self);
    }

    /**
     * Synonym for {@link #toSpreadMap(Map)}.
     * @param self a map
     * @return a newly created Spreadmap
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * <pre class="groovyTestCase">assert [1,2,3] == [3,1,2].sort()</pre>
     *
     * @param self the collection to be sorted
     * @return the sorted collection as a List
     * @since 1.0
     */
    public static <T> List<T> sort(Collection<T> self) {
        List<T> answer = asList(self);
        Collections.sort(answer, new NumberAwareComparator<T>());
        return answer;
    }

    /**
     * Sorts the given map into a sorted map using
     * the closure as a comparator.
     * <pre class="groovyTestCase">def map = [a:5, b:3, c:6, d:4].sort { a, b -> a.value <=> b.value }
     * assert map == [b:3, d:4, a:5, c:6]</pre>
     *
     * @param self the map to be sorted
     * @param closure a Closure used as a comparator
     * @return the sorted map
     * @since 1.6.0
     */
    public static <K, V> Map<K, V> sort(Map<K, V> self, Closure closure) {
        Map<K, V> result = new LinkedHashMap<K, V>();
        List<Map.Entry<K, V>> entries = asList(self.entrySet());
        sort(entries, closure);
        for (Map.Entry<K, V> entry : entries) {
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
     * @since 1.5.5
     */
    public static <T> T[] sort(T[] self) {
        Arrays.sort(self, new NumberAwareComparator<T>());
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
     * @since 1.5.5
     */
    public static <T> Iterator<T> sort(Iterator<T> self) {
        return sort(toList(self)).listIterator();
    }

    /**
     * Sorts the given iterator items into a sorted iterator using
     * the comparator.
     *
     * @param self       the Iterator to be sorted
     * @param comparator a Comparator used for comparing items
     * @return the sorted items as an Iterator
     * @since 1.5.5
     */
    public static <T> Iterator<T> sort(Iterator<T> self, Comparator<T> comparator) {
        return sort(toList(self), comparator).listIterator();
    }

    /**
     * Sorts the Collection using the given comparator.  The elements are
     * sorted into a new list, and the existing collection is unchanged.
     * <pre class="groovyTestCase">assert ["hi","hey","hello"] == ["hello","hi","hey"].sort( { a, b -> a.length() <=> b.length() } as Comparator )</pre>
     *
     * @param self       a collection to be sorted
     * @param comparator a Comparator used for the comparison
     * @return a newly created sorted List
     * @since 1.0
     */
    public static <T> List<T> sort(Collection<T> self, Comparator<T> comparator) {
        List<T> list = asList(self);
        Collections.sort(list, comparator);
        return list;
    }

    /**
     * Sorts the given Object array into sorted order using the given comparator.
     *
     * @param self the array to be sorted
     * @param comparator a Comparator used for the comparison
     * @return the sorted array
     * @since 1.5.5
     */
    public static <T> T[] sort(T[] self, Comparator<T> comparator) {
        Arrays.sort(self, comparator);
        return self;
    }

    /**
     * Sorts the given iterator items into a sorted iterator using
     * the Closure to determine the correct ordering. The original
     * iterator will be fully processed after the method call.
     * </p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     *
     * @param self       the Iterator to be sorted
     * @param closure a Closure used to determine the correct ordering
     * @return the sorted items as an Iterator
     * @since 1.5.5
     */
    public static <T> Iterator<T> sort(Iterator<T> self, Closure closure) {
        return sort(toList(self), closure).listIterator();
    }

    /**
     * Sorts the given Object array into a newly created array using
     * the Closure to determine the correct ordering.
     * </p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     *
     * @param self the array to be sorted
     * @param closure a Closure used to determine the correct ordering
     * @return the sorted array
     * @since 1.5.5
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] sort(T[] self, Closure closure) {
        return (T[]) sort(toList(self), closure).toArray();
    }

    /**
     * Sorts this Collection using
     * the closure to determine the correct ordering.
     * </p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     * <pre class="groovyTestCase">assert ["hi","hey","hello"] == ["hello","hi","hey"].sort { it.length() }</pre>
     * <pre class="groovyTestCase">assert ["hi","hey","hello"] == ["hello","hi","hey"].sort { a, b -> a.length() <=> b.length() }</pre>
     *
     * @param self    a Collection to be sorted
     * @param closure a 1 or 2 arg Closure used to determine the correct ordering
     * @return a newly created sorted List
     * @since 1.0
     */
    public static <T> List<T> sort(Collection<T> self, Closure closure) {
        List<T> list = asList(self);
        // use a comparator of one item or two
        int params = closure.getMaximumNumberOfParameters();
        if (params == 1) {
            Collections.sort(list, new OrderBy<T>(closure));
        } else {
            Collections.sort(list, new ClosureComparator<T>(closure));
        }
        return list;
    }

    /**
     * Avoids doing unnecessary work when sorting an already sorted set.
     *
     * @param self an identity function for an already sorted set
     * @return the sorted set
     * @since 1.0
     */
    public static <T> SortedSet<T> sort(SortedSet<T> self) {
        return self;
    }

    /**
     * Removes the last item from the List. Using add() and pop()
     * is similar to push and pop on a Stack.
     * <pre class="groovyTestCase">def list = ["a", false, 2]
     * assert list.pop() == 2
     * assert list == ["a", false]</pre>
     *
     * @param self a List
     * @return the item removed from the List
     * @throws NoSuchElementException if the list is empty and you try to pop() it.
     * @since 1.0
     */
    public static <T> T pop(List<T> self) {
        if (self.isEmpty()) {
            throw new NoSuchElementException("Cannot pop() an empty List");
        }
        return self.remove(self.size() - 1);
    }

    /**
     * Provides an easy way to append multiple Map.Entry values to a Map.
     *
     * @param self    a Map
     * @param entries a Collection of Map.Entry items to be added to the Map.
     * @return the same map, after the items have been added to it.
     * @since 1.6.1
     */
    public static <K, V> Map<K, V> putAll(Map<K, V> self, Collection<Map.Entry<K, V>> entries) {
        for (Map.Entry<K, V> entry : entries) {
            self.put(entry.getKey(), entry.getValue());
        }
        return self;
    }

    /**
     * Returns a new <code>Map</code> containing all entries from <code>self</code> and <code>entries</code>,
     * giving precedence to <code>entries</code>.  Any keys appearing in both Maps
     * will appear in the resultant map with values from the <code>entries</code>
     * operand. If <code>self</code> map is one of TreeMap, LinkedHashMap, Hashtable
     * or Properties, the returned Map will preserve that type, otherwise a HashMap will
     * be returned.
     * </p>
     *
     * @param self    a Map
     * @param entries a Collection of Map.Entry items to be added to the Map.
     * @return a new Map containing all key, value pairs from self and entries
     * @since 1.6.1
     */
    public static <K, V> Map<K, V> plus(Map<K, V> self, Collection<Map.Entry<K, V>> entries) {
        Map<K, V> map = cloneSimilarMap(self);
        putAll(map, entries);
        return map;
    }

    /**
     * Appends an item to the List. Synonym for add().
     * <pre class="groovyTestCase">def list = [3, 4, 2]
     * list.push("x")
     * assert list == [3, 4, 2, "x"]</pre>
     *
     * @param self a List
     * @param value element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of the
     *            <tt>Collection.add</tt> method).
     * @throws NoSuchElementException if the list is empty and you try to pop() it.
     * @since 1.5.5
     */
    public static <T> boolean push(List<T> self, T value) {
        return self.add(value);
    }

    /**
     * Returns the last item from the List.
     * <pre class="groovyTestCase">def list = [3, 4, 2]
     * assert list.last() == 2
     * assert list == [3, 4, 2]</pre>
     *
     * @param self a List
     * @return the last item from the List
     * @throws NoSuchElementException if the list is empty and you try to access the last() item.
     * @since 1.5.5
     */
    public static <T> T last(List<T> self) {
        if (self.isEmpty()) {
            throw new NoSuchElementException("Cannot access last() element from an empty List");
        }
        return self.get(self.size() - 1);
    }

    /**
     * Returns the first item from the List.
     * <pre class="groovyTestCase">def list = [3, 4, 2]
     * assert list.first() == 3
     * assert list == [3, 4, 2]</pre>
     *
     * @param self a List
     * @return the first item from the List
     * @throws NoSuchElementException if the list is empty and you try to access the first() item.
     * @since 1.5.5
     */
    public static <T> T first(List<T> self) {
        if (self.isEmpty()) {
            throw new NoSuchElementException("Cannot access first() element from an empty List");
        }
        return self.get(0);
    }

    /**
     * Returns the first item from the List.
     * <pre class="groovyTestCase">def list = [3, 4, 2]
     * assert list.head() == 3
     * assert list == [3, 4, 2]</pre>
     *
     * @param self a List
     * @return the first item from the List
     * @throws NoSuchElementException if the list is empty and you try to access the head() item.
     * @since 1.5.5
     */
    public static <T> T head(List<T> self) {
        return first(self);
    }

    /**
     * Returns the items from the List excluding the first item.
     * <pre class="groovyTestCase">def list = [3, 4, 2]
     * assert list.tail() == [4, 2]
     * assert list == [3, 4, 2]</pre>
     *
     * @param self a List
     * @return a list without its first element
     * @throws NoSuchElementException if the list is empty and you try to access the tail() item.
     * @since 1.5.6
     */
    public static <T> List<T> tail(List<T> self) {
        if (self.isEmpty()) {
            throw new NoSuchElementException("Cannot access tail() for an empty List");
        }
        List<T> result = new ArrayList<T>(self);
        result.remove(0);
        return result;
    }

    /**
     * Converts this collection to a List.
     * <pre class="groovyTestCase">assert new HashSet().asList() instanceof List</pre>
     *
     * @param self a collection to be converted into a List
     * @return a newly created List if this collection is not already a List
     * @since 1.0
     */
    public static <T> List<T> asList(Collection<T> self) {
        if (self instanceof List) {
            return (List<T>) self;
        } else {
            return new ArrayList<T>(self);
        }
    }

    /**
     * Coerce an object instance to a boolean value.
     * An object is coerced to true if it's not null, to false if it is null.
     *
     * @param object the object to coerce
     * @return the boolean value
     * @since 1.7.0
     */
    public static boolean asBoolean(Object object) {
        return object != null;
    }

    /**
     * Coerce an Boolean instance to a boolean value.
     *
     * @param bool the Boolean
     * @return the boolean value
     * @since 1.7.0
     */
    public static boolean asBoolean(Boolean bool) {
        return bool.booleanValue();
    }

    /**
     * Coerce a Matcher instance to a boolean value.
     *
     * @param matcher the matcher
     * @return the boolean value
     * @since 1.7.0
     */
    public static boolean asBoolean(Matcher matcher) {
        RegexSupport.setLastMatcher(matcher);
        return matcher.find();
    }

    /**
     * Coerce a collection instance to a boolean value.
     * A collection is coerced to false if it's empty, and to true otherwise.
     * <pre class="groovyTestCase">assert [1,2].asBoolean() == true</pre>
     * <pre class="groovyTestCase">assert [].asBoolean() == false</pre>
     *
     * @param collection the collection
     * @return the boolean value
     * @since 1.7.0
     */
    public static boolean asBoolean(Collection collection) {
        return !collection.isEmpty();
    }

    /**
     * Coerce a map instance to a boolean value.
     * A map is coerced to false if it's empty, and to true otherwise.
     * <pre class="groovyTestCase">assert [:] as Boolean == false
     * assert [a:2] as Boolean == true</pre>
     *
     * @param map the map
     * @return the boolean value
     * @since 1.7.0
     */
    public static boolean asBoolean(Map map) {
        return !map.isEmpty();
    }

    /**
     * Coerce an iterator instance to a boolean value.
     * An iterator is coerced to false if there are no more elements to iterate over,
     * and to true otherwise.
     *
     * @param iterator the iterator
     * @return the boolean value
     * @since 1.7.0
     */
    public static boolean asBoolean(Iterator iterator) {
        return iterator.hasNext();
    }

    /**
     * Coerce an enumeration instance to a boolean value.
     * An enumeration is coerced to false if there are no more elements to enumerate,
     * and to true otherwise.
     *
     * @param enumeration the enumeration
     * @return the boolean value
     * @since 1.7.0
     */
    public static boolean asBoolean(Enumeration enumeration) {
        return enumeration.hasMoreElements();
    }

    /**
     * Coerce a string (an instance of CharSequence) to a boolean value.
     * A string is coerced to false if it is of length 0,
     * and to true otherwise.
     *
     * @param string the character sequence
     * @return the boolean value
     * @since 1.7.0
     */
    public static boolean asBoolean(CharSequence string) {
        return string.length() > 0;
    }

    /**
     * Coerce an Object array to a boolean value.
     * An Object array is false if the array is of length 0.
     * and to true otherwise
     *
     * @param array the array
     * @return the boolean value
     * @since 1.7.0
     */
    public static boolean asBoolean(Object[] array) {
        return array.length > 0;
    }

    /**
     * Coerce a character to a boolean value.
     * A character is coerced to false if it's character value is equal to 0,
     * and to true otherwise.
     *
     * @param character the character
     * @return the boolean value
     * @since 1.7.0
     */

    public static boolean asBoolean(Character character) {
        return character.charValue() != 0;
    }

    /**
     * Coerce a number to a boolean value.
     * A number is coerced to false if its double value is equal to 0, and to true otherwise,
     * and to true otherwise.
     *
     * @param number the number
     * @return the boolean value
     * @since 1.7.0
     */
    public static boolean asBoolean(Number number) {
        return number.doubleValue() != 0;
    }

    /**
     * Coerce a GroovyResultSet to a boolean value.
     * A GroovyResultSet is coerced to false if there are no more rows to iterate over,
     * and to true otherwise.
     *
     * @param grs the GroovyResultSet
     * @return the boolean value
     * @since 1.7.0
     */
    public static boolean asBoolean(GroovyResultSet grs) {
        //TODO: check why this asBoolean() method is needed for SqlTest to pass with custom boolean coercion in place
        return true;
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
     * @since 1.0
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
        if (clazz == Queue.class) {
            if (col instanceof Queue) return col;
            return new LinkedList(col);
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
     * @since 1.5.1
     */
    public static Object asType(Object[] ary, Class clazz) {
        if (clazz == List.class) {
            return new ArrayList(Arrays.asList(ary));
        }
        if (clazz == Set.class) {
            return new HashSet(Arrays.asList(ary));
        }
        if (clazz == SortedSet.class) {
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
     * @since 1.0
     */
    public static Object asType(Closure cl, Class clazz) {
        if (clazz.isInterface() && !(clazz.isInstance(cl))) {
            return Proxy.newProxyInstance(
                    clazz.getClassLoader(),
                    new Class[]{clazz},
                    new ConvertedClosure(cl));
        }
        try {
            return asType((Object) cl, clazz);
        } catch (GroovyCastException ce) {
            try {
                return ProxyGenerator.INSTANCE.instantiateAggregateFromBaseClass(cl, clazz);
            } catch (GroovyRuntimeException cause) {
                throw new GroovyCastException("Error casting closure to " + clazz.getName() +
                        ", Reason: " + cause.getMessage());
            }
        }
    }

    /**
     * Coerces this map to the given type, using the map's keys as the public
     * method names, and values as the implementation.  Typically the value
     * would be a closure which behaves like the method implementation.
     *
     * @param map   this map
     * @param clazz the target type
     * @return a Proxy of the given type, which defers calls to this map's elements.
     * @since 1.0
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
     * <pre class="groovyTestCase">def list = ["a", 4, false]
     * assert list.reverse() == [false, 4, "a"]
     * assert list == ["a", 4, false]</pre>
     *
     * @param self a List
     * @return a reversed List
     * @since 1.0
     */
    public static <T> List<T> reverse(List<T> self) {
        int size = self.size();
        List<T> answer = new ArrayList<T>(size);
        ListIterator<T> iter = self.listIterator(size);
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
     * @since 1.5.5
     */
    public static <T> Iterator<T> reverse(Iterator<T> self) {
        return new ReverseListIterator<T>(toList(self));
    }

    /**
     * Create a Collection as a union of two collections. If the left collection
     * is a Set, then the returned collection will be a Set otherwise a List.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     * <pre class="groovyTestCase">assert [1,2,3,4] == [1,2] + [3,4]</pre>
     *
     * @param left  the left Collection
     * @param right the right Collection
     * @return the merged Collection
     * @since 1.5.0
     */
    public static <T> Collection<T> plus(Collection<T> left, Collection<T> right) {
        final Collection<T> answer = cloneSimilarCollection(left, left.size() + right.size());
        answer.addAll(right);
        return answer;
    }

    /**
     * Create a collection as a union of a Collection and an Object. If the collection
     * is a Set, then the returned collection will be a Set otherwise a List.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     * <pre class="groovyTestCase">assert [1,2,3] == [1,2] + 3</pre>
     *
     * @param left  a Collection
     * @param right an object to add/append
     * @return the resulting Collection
     * @since 1.5.0
     */
    public static <T> Collection<T> plus(Collection<T> left, T right) {
        final Collection<T> answer = cloneSimilarCollection(left, left.size() + 1);
        answer.add(right);
        return answer;
    }

    /**
     * Create a List composed of the elements of this list, repeated
     * a certain number of times.  Note that for non-primitive
     * elements, multiple references to the same instance will be added.
     * <pre class="groovyTestCase">assert [1,2,3,1,2,3] == [1,2,3] * 2</pre>
     *
     * @param self   a Collection
     * @param factor the number of times to append
     * @return the multiplied list
     * @since 1.0
     */
    public static <T> List<T> multiply(Collection<T> self, Number factor) {
        int size = factor.intValue();
        List<T> answer = new ArrayList<T>(self.size() * size);
        for (int i = 0; i < size; i++) {
            answer.addAll(self);
        }
        return answer;
    }

    /**
     * Create a Collection composed of the intersection of both collections.  Any
     * elements that exist in both collections are added to the resultant collection.
     * <pre class="groovyTestCase">assert [4,5] == [1,2,3,4,5].intersect([4,5,6,7,8])</pre>
     *
     * @param left  a Collection
     * @param right a Collection
     * @return a Collection as an intersection of both collections
     * @since 1.5.6
     */
    public static <T> Collection<T> intersect(Collection<T> left, Collection<T> right) {
        if (left.isEmpty())
            return createSimilarCollection(left, 0);

        if (left.size() < right.size()) {
            Collection<T> swaptemp = left;
            left = right;
            right = swaptemp;
        }

        // TODO optimise if same type?
        // boolean nlgnSort = sameType(new Collection[]{left, right});

        Collection<T> result = createSimilarCollection(left, left.size());
        //creates the collection to look for values.
        Collection<T> pickFrom = new TreeSet<T>(new NumberAwareComparator<T>());
        pickFrom.addAll(left);

        for (final T t : right) {
            if (pickFrom.contains(t))
                result.add(t);
        }
        return result;
    }

    /**
     * Returns <code>true</code> if the intersection of two collections is empty.
     * <pre class="groovyTestCase">assert [1,2,3].disjoint([3,4,5]) == false</pre>
     * <pre class="groovyTestCase">assert [1,2].disjoint([3,4]) == true</pre>
     *
     * @param left  a Collection
     * @param right a Collection
     * @return boolean   <code>true</code> if the intersection of two collections
     *         is empty, <code>false</code> otherwise.
     * @since 1.0
     */
    public static boolean disjoint(Collection left, Collection right) {

        if (left.isEmpty() || right.isEmpty())
            return true;

        Collection pickFrom = new TreeSet(new NumberAwareComparator());
        pickFrom.addAll(right);

        for (final Object o : left) {
            if (pickFrom.contains(o))
                return false;
        }
        return true;
    }

    // Default comparator for objects accounting for numbers of different types.
    // Also handles nulls. Null is less than everything else.
    private static class NumberAwareComparator<T> implements Comparator<T> {
        public int compare(T o1, T o2) {
            try {
                return DefaultTypeTransformation.compareTo(o1, o2);
            } catch (ClassCastException cce) {
            } catch (GroovyRuntimeException gre) {}
            // since the object does not have a valid compareTo method
            // we compare using the hashcodes. null cases are handled by
            // DefaultTypeTransformation.compareTo
            int x1 = o1.hashCode();
            int x2 = o2.hashCode();
            if (x1==x2) return 0;
            if (x1<x2)  return -1;
            return 1;
        }
    }

    /**
     * Compare the contents of this array to the contents of the given array.
     *
     * @param left an int array
     * @param right the operand array.
     * @return true if the contents of both arrays are equal.
     * @since 1.5.0
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
     * @since 1.5.0
     */
    public static boolean equals(Object[] left, List right) {
        return coercedEquals(left, right);
    }

    /**
     * Determines if the contents of this list are equal to the
     * contents of the given array in the same order.  This returns
     * <code>false</code> if either collection is <code>null</code>.
     * <pre class="groovyTestCase">assert [1, "a"].equals( [ 1, "a" ] as Object[] )</pre>
     *
     * @param left  this List
     * @param right this Object[] being compared to
     * @return true if the contents of both collections are equal
     * @since 1.5.0
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
     * <pre class="groovyTestCase">assert ["a", 2].equals(["a", 2])
     * assert ![2, "a"].equals("a", 2)
     * assert [2.0, "a"].equals(2L, "a") // number comparison at work</pre>
     *
     * @param left  this List
     * @param right the List being compared to.
     * @return boolean   <code>true</code> if the contents of both lists are identical,
     *         <code>false</code> otherwise.
     * @since 1.0
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
     * @since 1.5.0
     */
    public static <T> Set<T> minus(Set<T> self, Collection operands) {
        final Set<T> ansSet = createSimilarSet(self);
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
     * @since 1.5.0
     */
    public static <T> Set<T> minus(Set<T> self, Object operand) {
        final Set<T> ansSet = createSimilarSet(self);
        Comparator numberComparator = new NumberAwareComparator();
        for (T t : self) {
            if (numberComparator.compare(t, operand) != 0) ansSet.add(t);
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
     * @since 1.5.5
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] minus(T[] self, Collection<T> removeMe) {
        return (T[]) minus(toList(self), removeMe).toArray();
    }

    /**
     * Create an array composed of the elements of the first array minus the
     * elements of the given array.
     *
     * @param self     an object array
     * @param removeMe an array of elements to remove
     * @return an array with the supplied elements removed
     * @since 1.5.5
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] minus(T[] self, T[] removeMe) {
        return (T[]) minus(toList(self), toList(removeMe)).toArray();
    }

    /**
     * Create a List composed of the elements of the first list minus
     * every occurrence of elements of the given collection.
     * <pre class="groovyTestCase">assert [1, "a", true, true, false, 5.3] - [true, 5.3] == [1, "a", false]</pre>
     *
     * @param self     a List
     * @param removeMe a Collection of elements to remove
     * @return a List with the supplied elements removed
     * @since 1.0
     */
    public static <T> List<T> minus(List<T> self, Collection<T> removeMe) {

        if (self.size() == 0)
            return new ArrayList<T>();

        boolean nlgnSort = sameType(new Collection[]{self, removeMe});

        // We can't use the same tactic as for intersection
        // since AbstractCollection only does a remove on the first
        // element it encounters.

        Comparator<T> numberComparator = new NumberAwareComparator<T>();

        if (nlgnSort && (self.get(0) instanceof Comparable)) {
            //n*LOG(n) version
            Set<T> answer;
            if (Number.class.isInstance(self.get(0))) {
                answer = new TreeSet<T>(numberComparator);
                answer.addAll(self);
                for (T t : self) {
                    if (Number.class.isInstance(t)) {
                        for (T t2 : removeMe) {
                            if (Number.class.isInstance(t2)) {
                                if (numberComparator.compare(t, t2) == 0)
                                    answer.remove(t);
                            }
                        }
                    } else {
                        if (removeMe.contains(t))
                            answer.remove(t);
                    }
                }
            } else {
                answer = new TreeSet<T>(numberComparator);
                answer.addAll(self);
                answer.removeAll(removeMe);
            }

            List<T> ansList = new ArrayList<T>();
            for (T o : self) {
                if (answer.contains(o))
                    ansList.add(o);
            }
            return ansList;
        } else {
            //n*n version
            List<T> tmpAnswer = new LinkedList<T>(self);
            for (Iterator<T> iter = tmpAnswer.iterator(); iter.hasNext();) {
                T element = iter.next();
                boolean elementRemoved = false;
                for (Iterator<T> iterator = removeMe.iterator(); iterator.hasNext() && !elementRemoved;) {
                    T elt = iterator.next();
                    if (numberComparator.compare(element, elt) == 0) {
                        iter.remove();
                        elementRemoved = true;
                    }
                }
            }

            //remove duplicates
            //can't use treeset since the base classes are different
            return new ArrayList<T>(tmpAnswer);
        }
    }

    /**
     * Create a new List composed of the elements of the first list minus every occurrence of the
     * operand.
     * <pre class="groovyTestCase">assert ["a", 5, 5, true] - 5 == ["a", true]</pre>
     *
     * @param self    a List object
     * @param operand an element to remove from the list
     * @return the resulting List with the operand removed
     * @since 1.0
     */
    public static <T> List<T> minus(List<T> self, Object operand) {
        Comparator numberComparator = new NumberAwareComparator();
        List<T> ansList = new ArrayList<T>();
        for (T t : self) {
            if (numberComparator.compare(t, operand) != 0) ansList.add(t);
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
     * @since 1.5.5
     */
    public static <T> T[] minus(T[] self, Object operand) {
        return (T[]) minus(toList(self), operand).toArray();
    }

    /**
     * Flatten a collection.  This collection and any nested arrays or
     * collections have their contents (recursively) added to the new collection.
     * <pre class="groovyTestCase">assert [1,2,3,4,5] == [1,[2,3],[[4]],[],5].flatten()</pre>
     *
     * @param self a Collection to flatten
     * @return a flattened Collection
     * @since 1.6.0
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
        for (Object element : elements) {
            if (element instanceof Collection) {
                flatten((Collection) element, addTo);
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
     * @since 1.6.0
     */
    public static Collection flatten(Collection self, Closure flattenUsing) {
        return flatten(self, createSimilarCollection(self), flattenUsing);
    }

    private static Collection flatten(Collection elements, Collection addTo, Closure flattenUsing) {
        for (Object element : elements) {
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
                    addTo.add(flattened);
                }
            }
        }
        return addTo;
    }

    /**
     * Overloads the left shift operator to provide an easy way to append
     * objects to a Collection.
     * <pre class="groovyTestCase">def list = [1,2]
     * list << 3
     * assert list == [1,2,3]</pre>
     *
     * @param self  a Collection
     * @param value an Object to be added to the collection.
     * @return same collection, after the value was added to it.
     * @since 1.0
     */
    public static <T> Collection<T> leftShift(Collection<T> self, T value) {
        self.add(value);
        return self;
    }

    /**
     * Overloads the left shift operator to provide an easy way to append
     * objects to a BlockingQueue.
     * In case of bounded queue the method will block till space in the queue become available
     * <pre class="groovyTestCase">def list = new java.util.concurrent.LinkedBlockingQueue ()
     * list << 3 << 2 << 1
     * assert list.iterator().collect{it} == [3,2,1]</pre>
     *
     * @param self  a Collection
     * @param value an Object to be added to the collection.
     * @return same collection, after the value was added to it.
     * @since 1.7.1
     */
    public static <T> BlockingQueue<T> leftShift(BlockingQueue<T> self, T value) throws InterruptedException {
        self.put(value);
        return self;
    }

    /**
     * Overloads the left shift operator to provide an easy way to append
     * Map.Entry values to a Map.
     *
     * @param self  a Map
     * @param entry a Map.Entry to be added to the Map.
     * @return same map, after the value has been added to it.
     * @since 1.6.0
     */
    public static <K, V> Map<K, V> leftShift(Map<K, V> self, Map.Entry<K, V> entry) {
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.5.0
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
     * @since 1.5.0
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
     * @since 1.5.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.5.0
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
     * @since 1.0
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
     * @since 1.0
     */
    public static OutputStream leftShift(OutputStream self, byte[] value) throws IOException {
        self.write(value);
        self.flush();
        return self;
    }

    // Primitive type array methods
    //-------------------------------------------------------------------------

    /**
     * Support the subscript operator with a range for a byte array
     *
     * @param array a byte array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved bytes
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Byte> getAt(byte[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with a range for a char array
     *
     * @param array a char array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved chars
     * @since 1.5.0
     */
    @SuppressWarnings("unchecked")
    public static List<Character> getAt(char[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with a range for a short array
     *
     * @param array a short array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved shorts
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Short> getAt(short[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with a range for an int array
     *
     * @param array an int array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the ints at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Integer> getAt(int[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with a range for a long array
     *
     * @param array a long array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved longs
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Long> getAt(long[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with a range for a float array
     *
     * @param array a float array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved floats
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Float> getAt(float[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with a range for a double array
     *
     * @param array a double array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved doubles
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Double> getAt(double[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with a range for a boolean array
     *
     * @param array a boolean array
     * @param range a range indicating the indices for the items to retrieve
     * @return list of the retrieved booleans
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Boolean> getAt(boolean[] array, Range range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an IntRange for a byte array
     *
     * @param array a byte array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved bytes
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Byte> getAt(byte[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an IntRange for a char array
     *
     * @param array a char array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved chars
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Character> getAt(char[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an IntRange for a short array
     *
     * @param array a short array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved shorts
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Short> getAt(short[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an IntRange for an int array
     *
     * @param array an int array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved ints
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Integer> getAt(int[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an IntRange for a long array
     *
     * @param array a long array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved longs
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Long> getAt(long[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an IntRange for a float array
     *
     * @param array a float array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved floats
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Float> getAt(float[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an IntRange for a double array
     *
     * @param array a double array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved doubles
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Double> getAt(double[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an IntRange for a boolean array
     *
     * @param array a boolean array
     * @param range an IntRange indicating the indices for the items to retrieve
     * @return list of the retrieved booleans
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Boolean> getAt(boolean[] array, IntRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with an ObjectRange for a byte array
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
     * Support the subscript operator with an ObjectRange for a char array
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
     * Support the subscript operator with an ObjectRange for a short array
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
     * Support the subscript operator with an ObjectRange for an int array
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
     * Support the subscript operator with an ObjectRange for a long array
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
     * Support the subscript operator with an ObjectRange for a float array
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
     * Support the subscript operator with an ObjectRange for a double array
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
     * Support the subscript operator with an ObjectRange for a byte array
     *
     * @param array a byte array
     * @param range an ObjectRange indicating the indices for the items to retrieve
     * @return list of the retrieved bytes
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Boolean> getAt(boolean[] array, ObjectRange range) {
        return primitiveArrayGet(array, range);
    }

    /**
     * Support the subscript operator with a collection for a byte array
     *
     * @param array a byte array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the bytes at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Byte> getAt(byte[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator with a collection for a char array
     *
     * @param array a char array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the chars at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Character> getAt(char[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator with a collection for a short array
     *
     * @param array a short array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the shorts at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Short> getAt(short[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator with a collection for an int array
     *
     * @param array an int array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the ints at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Integer> getAt(int[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator with a collection for a long array
     *
     * @param array a long array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the longs at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Long> getAt(long[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator with a collection for a float array
     *
     * @param array a float array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the floats at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Float> getAt(float[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator with a collection for a double array
     *
     * @param array a double array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the doubles at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Double> getAt(double[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator with a collection for a boolean array
     *
     * @param array a boolean array
     * @param indices a collection of indices for the items to retrieve
     * @return list of the booleans at the given indices
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Boolean> getAt(boolean[] array, Collection indices) {
        return primitiveArrayGet(array, indices);
    }

    /**
     * Support the subscript operator for a Bitset
     *
     * @param self  a BitSet
     * @param index index to retrieve
     * @return value of the bit at the given index
     * @see java.util.BitSet
     * @since 1.5.0
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
     * @since 1.5.0
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

//    public static Boolean putAt(boolean[] array, int idx, Boolean newValue) {
//        return (Boolean) primitiveArrayPut(array, idx, newValue);
//    }
//
//    public static Byte putAt(byte[] array, int idx, Object newValue) {
//        if (!(newValue instanceof Byte)) {
//            Number n = (Number) newValue;
//            newValue = new Byte(n.byteValue());
//        }
//        return (Byte) primitiveArrayPut(array, idx, newValue);
//    }
//
//    public static Character putAt(char[] array, int idx, Object newValue) {
//        if (newValue instanceof String) {
//            String s = (String) newValue;
//            if (s.length() != 1) throw new IllegalArgumentException("String of length 1 expected but got a bigger one");
//            char c = s.charAt(0);
//            newValue = new Character(c);
//        }
//        return (Character) primitiveArrayPut(array, idx, newValue);
//    }
//
//    public static Short putAt(short[] array, int idx, Object newValue) {
//        if (!(newValue instanceof Short)) {
//            Number n = (Number) newValue;
//            newValue = new Short(n.shortValue());
//        }
//        return (Short) primitiveArrayPut(array, idx, newValue);
//    }
//
//    public static Integer putAt(int[] array, int idx, Object newValue) {
//        if (!(newValue instanceof Integer)) {
//            Number n = (Number) newValue;
//            newValue = Integer.valueOf(n.intValue());
//        }
//        array [normaliseIndex(idx,array.length)] = ((Integer)newValue).intValue();
//        return (Integer) newValue;
//    }
//
//    public static Long putAt(long[] array, int idx, Object newValue) {
//        if (!(newValue instanceof Long)) {
//            Number n = (Number) newValue;
//            newValue = new Long(n.longValue());
//        }
//        return (Long) primitiveArrayPut(array, idx, newValue);
//    }
//
//    public static Float putAt(float[] array, int idx, Object newValue) {
//        if (!(newValue instanceof Float)) {
//            Number n = (Number) newValue;
//            newValue = new Float(n.floatValue());
//        }
//        return (Float) primitiveArrayPut(array, idx, newValue);
//    }
//
//    public static Double putAt(double[] array, int idx, Object newValue) {
//        if (!(newValue instanceof Double)) {
//            Number n = (Number) newValue;
//            newValue = new Double(n.doubleValue());
//        }
//        return (Double) primitiveArrayPut(array, idx, newValue);
//    }

    /**
     * Support assigning a range of values with a single assignment statement.
     *
     * @param self  a BitSet
     * @param range the range of values to set
     * @param value value
     * @see java.util.BitSet
     * @see groovy.lang.Range
     * @since 1.5.0
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
     * @since 1.5.0
     */
    public static void putAt(BitSet self, int index, boolean value) {
        self.set(index, value);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a boolean array
     * @return the length of the array
     * @see Array#getLength(Object)
     * @since 1.5.0
     */
    public static int size(boolean[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a byte array
     * @return the length of the array
     * @see Array#getLength(Object)
     * @since 1.0
     */
    public static int size(byte[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a char array
     * @return the length of the array
     * @see Array#getLength(Object)
     * @since 1.0
     */
    public static int size(char[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a short array
     * @return the length of the array
     * @see Array#getLength(Object)
     * @since 1.0
     */
    public static int size(short[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array an int array
     * @return the length of the array
     * @see Array#getLength(Object)
     * @since 1.0
     */
    public static int size(int[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a long array
     * @return the length of the array
     * @see Array#getLength(Object)
     * @since 1.0
     */
    public static int size(long[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a float array
     * @return the length of the array
     * @see Array#getLength(Object)
     * @since 1.0
     */
    public static int size(float[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a double array
     * @return the length of the array
     * @see Array#getLength(Object)
     * @since 1.0
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
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Byte> toList(byte[] array) {
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
    @SuppressWarnings("unchecked")
    public static List<Boolean> toList(boolean[] array) {
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
    @SuppressWarnings("unchecked")
    public static List<Character> toList(char[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param array a short array
     * @return a list containing the contents of this array.
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Short> toList(short[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param array an int array
     * @return a list containing the contents of this array.
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Integer> toList(int[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param array a long array
     * @return a list containing the contents of this array.
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Long> toList(long[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param array a float array
     * @return a list containing the contents of this array.
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Float> toList(float[] array) {
        return DefaultTypeTransformation.primitiveArrayToList(array);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param array a double array
     * @return a list containing the contents of this array.
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<Double> toList(double[] array) {
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
     * @since 1.5.1
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
     * @since 1.0
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
     * @since 1.5.7
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
     * @since 1.0
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
     * @since 1.0
     */
    public static byte[] decodeBase64(String value) {
        int byteShift = 4;
        int tmp = 0;
        boolean done = false;
        final StringBuilder buffer = new StringBuilder();

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
     * @since 1.5.0
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
     * @since 1.5.0
     */
    protected static List primitiveArrayGet(Object self, Range range) {
        List answer = new ArrayList();
        for (Object next : range) {
            int idx = DefaultTypeTransformation.intUnbox(next);
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
     * @since 1.0
     */
    protected static List primitiveArrayGet(Object self, Collection indices) {
        List answer = new ArrayList();
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

    /**
     * Implements the setAt(int idx) method for primitve type arrays.
     *
     * @param self     an object
     * @param idx      the index of interest
     * @param newValue the new value to be put into the index of interest
     * @return the added value
     * @since 1.5.0
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
     * @since 1.0
     */
    public static Character toCharacter(String self) {
        return self.charAt(0);
    }

    /**
     * Converts the given string into a Boolean object.
     * If the trimmed string is "true", "y" or "1" (ignoring case)
     * then the result is true othewrwise it is false.
     *
     * @param self a String
     * @return The Boolean value
     * @since 1.0
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
     * @since 1.5.0
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
     * Convenience method to split a GString (with whitespace as delimiter).
     *
     * @param self the GString to split
     * @return String[] result of split
     * @see #split(String)
     * @since 1.6.1
     */
    public static String[] split(GString self) {
        return split(self.toString());
    }

    /**
     * Tokenize a String based on the given string delimiter.
     *
     * @param self  a String
     * @param token the delimiter
     * @return a List of tokens
     * @see java.util.StringTokenizer#StringTokenizer(java.lang.String, java.lang.String)
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
     */
    public static String next(String self) {
        StringBuilder buffer = new StringBuilder(self);
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
     * @since 1.0
     */
    public static String previous(String self) {
        StringBuilder buffer = new StringBuilder(self);
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
     * Executes the command specified by <code>self</code> as a command-line process.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code> (JDK 1.5+).</p>
     *
     * @param self a command line String
     * @return the Process which has just started for this command line representation
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static Process execute(final String self) throws IOException {
        return Runtime.getRuntime().exec(self);
    }

    /**
     * Executes the command specified by <code>self</code> with environment defined by <code>envp</code>
     * and under the working directory <code>dir</code>.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code> (JDK 1.5+).</p>
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
     * @return the Process which has just started for this command line representation.
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static Process execute(final String self, final String[] envp, final File dir) throws IOException {
        return Runtime.getRuntime().exec(self, envp, dir);
    }

    /**
     * Executes the command specified by <code>self</code> with environment defined
     * by <code>envp</code> and under the working directory <code>dir</code>.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code> (JDK 1.5+).</p>
     *
     * @param self a command line String to be executed.
     * @param envp a List of Objects (converted to Strings using toString), each member of which
     *             has environment variable settings in the format
     *             <i>name</i>=<i>value</i>, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the environment of the current process.
     * @param dir  the working directory of the subprocess, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the working directory of the current process.
     * @return the Process which has just started for this command line representation.
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static Process execute(final String self, final List envp, final File dir) throws IOException {
        return execute(self, stringify(envp), dir);
    }

    /**
     * Executes the command specified by the given <code>String</code> array.
     * The first item in the array is the command; the others are the parameters.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code> (JDK 1.5+).</p>
     *
     * @param commandArray an array of <code>String<code> containing the command name and
     *                     parameters as separate items in the array.
     * @return the Process which has just started for this command line representation.
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static Process execute(final String[] commandArray) throws IOException {
        return Runtime.getRuntime().exec(commandArray);
    }

    /**
     * Executes the command specified by the <code>String</code> array given in the first parameter,
     * with the environment defined by <code>envp</code> and under the working directory <code>dir</code>.
     * The first item in the array is the command; the others are the parameters.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code> (JDK 1.5+).</p>
     *
     * @param commandArray an array of <code>String<code> containing the command name and
     *                     parameters as separate items in the array.
     * @param envp an array of Strings, each member of which
     *             has environment variable settings in the format
     *             <i>name</i>=<i>value</i>, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the environment of the current process.
     * @param dir  the working directory of the subprocess, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the working directory of the current process.
     * @return the Process which has just started for this command line representation.
     * @throws IOException if an IOException occurs.
     * @since 1.7.1
     */
    public static Process execute(final String[] commandArray, final String[] envp, final File dir) throws IOException {
        return Runtime.getRuntime().exec(commandArray, envp, dir);
    }

    /**
     * Executes the command specified by the <code>String</code> array given in the first parameter,
     * with the environment defined by <code>envp</code> and under the working directory <code>dir</code>.
     * The first item in the array is the command; the others are the parameters.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code> (JDK 1.5+).</p>
     *
     * @param commandArray an array of <code>String<code> containing the command name and
     *                     parameters as separate items in the array.
     * @param envp a List of Objects (converted to Strings using toString), each member of which
     *             has environment variable settings in the format
     *             <i>name</i>=<i>value</i>, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the environment of the current process.
     * @param dir  the working directory of the subprocess, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the working directory of the current process.
     * @return the Process which has just started for this command line representation.
     * @throws IOException if an IOException occurs.
     * @since 1.7.1
     */
    public static Process execute(final String[] commandArray, final List envp, final File dir) throws IOException {
        return Runtime.getRuntime().exec(commandArray, stringify(envp), dir);
    }

    /**
     * Executes the command specified by the given list. The toString() method is called
     * for each item in the list to convert into a resulting String.
     * The first item in the list is the command the others are the parameters.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code> (JDK 1.5+).</p>
     *
     * @param commands a list containing the command name and
     *                    parameters as separate items in the list.
     * @return the Process which has just started for this command line representation.
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static Process execute(final List commands) throws IOException {
        return execute(stringify(commands));
    }

    /**
     * Executes the command specified by the given list,
     * with the environment defined by <code>envp</code> and under the working directory <code>dir</code>.
     * The first item in the list is the command; the others are the parameters. The toString()
     * method is called on items in the list to convert them to Strings.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code> (JDK 1.5+).</p>
     *
     * @param commands a List containing the command name and
     *                     parameters as separate items in the list.
     * @param envp an array of Strings, each member of which
     *             has environment variable settings in the format
     *             <i>name</i>=<i>value</i>, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the environment of the current process.
     * @param dir  the working directory of the subprocess, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the working directory of the current process.
     * @return the Process which has just started for this command line representation.
     * @throws IOException if an IOException occurs.
     * @since 1.7.1
     */
    public static Process execute(final List commands, final String[] envp, final File dir) throws IOException {
        return Runtime.getRuntime().exec(stringify(commands), envp, dir);
    }

    /**
     * Executes the command specified by the given list,
     * with the environment defined by <code>envp</code> and under the working directory <code>dir</code>.
     * The first item in the list is the command; the others are the parameters. The toString()
     * method is called on items in the list to convert them to Strings.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code> (JDK 1.5+).</p>
     *
     * @param commands a List containing the command name and
     *                     parameters as separate items in the list.
     * @param envp a List of Objects (converted to Strings using toString), each member of which
     *             has environment variable settings in the format
     *             <i>name</i>=<i>value</i>, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the environment of the current process.
     * @param dir  the working directory of the subprocess, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the working directory of the current process.
     * @return the Process which has just started for this command line representation.
     * @throws IOException if an IOException occurs.
     * @since 1.7.1
     */
    public static Process execute(final List commands, final List envp, final File dir) throws IOException {
        return Runtime.getRuntime().exec(stringify(commands), stringify(envp), dir);
    }

    private static String[] stringify(final List orig) {
        if (orig == null) return null;
        String[] result = new String[orig.size()];
        for (int i = 0; i < orig.size(); i++) {
            result[i] = orig.get(i).toString();
        }
        return result;
    }

    /**
     * Repeat a String a certain number of times.
     *
     * @param self   a String to be repeated
     * @param factor the number of times the String should be repeated
     * @return a String composed of a repetition
     * @throws IllegalArgumentException if the number of repetitions is &lt; 0
     * @since 1.0
     */
    public static String multiply(String self, Number factor) {
        int size = factor.intValue();
        if (size == 0)
            return "";
        else if (size < 0) {
            throw new IllegalArgumentException("multiply() should be called with a number of 0 or greater not: " + size);
        }
        StringBuilder answer = new StringBuilder(self);
        for (int i = 1; i < size; i++) {
            answer.append(self);
        }
        return answer.toString();
    }
    
    /**
     * Returns the string representation of the given array.
     * 
     * @param self an array
     * @return the string representation
     * @since 1.6.0
     */
    public static String toString(boolean[] self) {
        return InvokerHelper.toString(self);
    }

    /**
     * Returns the string representation of the given array.
     * 
     * @param self an array
     * @return the string representation
     * @since 1.6.0
     */
    public static String toString(byte[] self) {
        return InvokerHelper.toString(self);
    }

    /**
     * Returns the string representation of the given array.
     * 
     * @param self an array
     * @return the string representation
     * @since 1.6.0
     */
    public static String toString(char[] self) {
        return InvokerHelper.toString(self);
    }

    /**
     * Returns the string representation of the given array.
     * 
     * @param self an array
     * @return the string representation
     * @since 1.6.0
     */
    public static String toString(short[] self) {
        return InvokerHelper.toString(self);
    }

    /**
     * Returns the string representation of the given array.
     * 
     * @param self an array
     * @return the string representation
     * @since 1.6.0
     */
    public static String toString(int[] self) {
        return InvokerHelper.toString(self);
    }

    /**
     * Returns the string representation of the given array.
     * 
     * @param self an array
     * @return the string representation
     * @since 1.6.0
     */
    public static String toString(long[] self) {
        return InvokerHelper.toString(self);
    }

    /**
     * Returns the string representation of the given array.
     * 
     * @param self an array
     * @return the string representation
     * @since 1.6.0
     */
    public static String toString(float[] self) {
        return InvokerHelper.toString(self);
    }

    /**
     * Returns the string representation of the given array.
     * 
     * @param self an array
     * @return the string representation
     * @since 1.6.0
     */
    public static String toString(double[] self) {
        return InvokerHelper.toString(self);
    }

    /**
     * Returns the string representation of the given map.
     *
     * @param self a Map
     * @return the string representation
     * @see #toMapString(Map)
     * @since 1.0
     */
    public static String toString(AbstractMap self) {
        return toMapString(self);
    }

    /**
     * Returns the string representation of this map.  The string displays the
     * contents of the map, i.e. <code>[one:1, two:2, three:3]</code>.
     *
     * @param self a Map
     * @return the string representation
     * @since 1.0
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
     * @since 1.0
     */
    public static String toString(AbstractCollection self) {
        return toListString(self);
    }

    /**
     * Returns the string representation of the given list.  The string
     * displays the contents of the list, similar to a list literal, i.e.
     * <code>[1, 2, a]</code>.
     *
     * @param self a Collection
     * @return the string representation
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
     */
    public static String toArrayString(Object[] self) {
        return (self == null) ? "null" : InvokerHelper.toArrayString(self);
    }

    /**
     * Create a String representation of this object.
     * @param value an object
     * @return a string.
     * @since 1.0
     */
    public static String toString(Object value) {
        return InvokerHelper.toString(value);
    }

    // Number based methods
    //-------------------------------------------------------------------------

    /**
     * Increment a Character by one.
     *
     * @param self a Character
     * @return an incremented Character
     * @since 1.5.7
     */
    public static Character next(Character self) {
        return (char) (self + 1);
    }

    /**
     * Increment a Number by one.
     *
     * @param self a Number
     * @return an incremented Number
     * @since 1.0
     */
    public static Number next(Number self) {
        return NumberNumberPlus.plus(self, ONE);
    }

    /**
     * Decrement a Character by one.
     *
     * @param self a Character
     * @return a decremented Character
     * @since 1.5.7
     */
    public static Character previous(Character self) {
        return (char) (self - 1);
    }

    /**
     * Decrement a Number by one.
     *
     * @param self a Number
     * @return a decremented Number
     * @since 1.0
     */
    public static Number previous(Number self) {
        return NumberNumberMinus.minus(self, ONE);
    }

    /**
     * Add a Character and a Number. The ordinal value of the Character
     * is used in the addition (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     *
     * @see Integer#valueOf(int)
     * @param left  a Character
     * @param right a Number
     * @return the Number corresponding to the addition of left and right
     * @since 1.0
     */
    public static Number plus(Character left, Number right) {
        return NumberNumberPlus.plus(Integer.valueOf(left), right);
    }

    /**
     * Add a Number and a Character.  The ordinal value of the Character
     * is used in the addition (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     *
     * @see Integer#valueOf(int)
     * @param left  a Number
     * @param right a Character
     * @return The Number corresponding to the addition of left and right
     * @since 1.0
     */
    public static Number plus(Number left, Character right) {
        return NumberNumberPlus.plus(left, Integer.valueOf(right));
    }

    /**
     * Add one Character to another. The ordinal values of the Characters
     * are used in the addition (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     *
     * @see #plus(Number, Character)
     * @param left  a Character
     * @param right a Character
     * @return the Number corresponding to the addition of left and right
     * @since 1.0
     */
    public static Number plus(Character left, Character right) {
        return plus(Integer.valueOf(left), right);
    }

    /**
     * Compare a Character and a Number. The ordinal value of the Character
     * is used in the comparison (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     *
     * @param left  a Character
     * @param right a Number
     * @return the result of the comparison
     * @since 1.0
     */
    public static int compareTo(Character left, Number right) {
        return compareTo(Integer.valueOf(left), right);
    }

    /**
     * Compare a Number and a Character. The ordinal value of the Character
     * is used in the comparison (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     *
     * @param left  a Number
     * @param right a Character
     * @return the result of the comparison
     * @since 1.0
     */
    public static int compareTo(Number left, Character right) {
        return compareTo(left, Integer.valueOf(right));
    }

    /**
     * Compare two Characters. The ordinal values of the Characters
     * are compared (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     *
     * @param left  a Character
     * @param right a Character
     * @return the result of the comparison
     * @since 1.0
     */
    public static int compareTo(Character left, Character right) {
        return compareTo(Integer.valueOf(left), right);
    }

    /**
     * Compare two Numbers.  Equality (==) for numbers dispatches to this.
     *
     * @param left  a Number
     * @param right another Number to compare to
     * @return the comparision of both numbers
     * @since 1.0
     */
    public static int compareTo(Number left, Number right) {
        /** @todo maybe a double dispatch thing to handle new large numbers? */
        return NumberMath.compareTo(left, right);
    }

    /**
     * Subtract a Number from a Character. The ordinal value of the Character
     * is used in the subtraction (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     *
     * @param left  a Character
     * @param right a Number
     * @return the Number corresponding to the subtraction of right from left
     * @since 1.0
     */
    public static Number minus(Character left, Number right) {
        return NumberNumberMinus.minus(Integer.valueOf(left), right);
    }

    /**
     * Subtract a Character from a Number. The ordinal value of the Character
     * is used in the subtraction (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     *
     * @param left  a Number
     * @param right a Character
     * @return the Number corresponding to the subtraction of right from left
     * @since 1.0
     */
    public static Number minus(Number left, Character right) {
        return NumberNumberMinus.minus(left, Integer.valueOf(right));
    }

    /**
     * Subtract one Character from another. The ordinal values of the Characters
     * is used in the comparison (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     *
     * @param left  a Character
     * @param right a Character
     * @return the Number corresponding to the subtraction of right from left
     * @since 1.0
     */
    public static Number minus(Character left, Character right) {
        return minus(Integer.valueOf(left), right);
    }

    /**
     * Multiply a Character by a Number. The ordinal value of the Character
     * is used in the multiplcation (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     *
     * @param left  a Character
     * @param right a Number
     * @return the Number corresponding to the multiplication of left by right
     * @since 1.0
     */
    public static Number multiply(Character left, Number right) {
        return NumberNumberMultiply.multiply(Integer.valueOf(left), right);
    }

    /**
     * Multiply a Number by a Character. The ordinal value of the Character
     * is used in the multiplication (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     *
     * @param left  a Number
     * @param right a Character
     * @return the multiplication of left by right
     * @since 1.0
     */
    public static Number multiply(Number left, Character right) {
        return NumberNumberMultiply.multiply(Integer.valueOf(right), left);
    }

    /**
     * Multiply two Characters. The ordinal values of the Characters
     * are used in the multiplication (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     *
     * @param left  a Character
     * @param right another Character
     * @return the Number corresponding to the multiplication of left by right
     * @since 1.0
     */
    public static Number multiply(Character left, Character right) {
        return multiply(Integer.valueOf(left), right);
    }

    /**
     * Multiply a BigDecimal and a Double.
     * Note: This method was added to enforce the Groovy rule of
     * BigDecimal*Double == Double. Without this method, the
     * multiply(BigDecimal) method in BigDecimal would respond
     * and return a BigDecimal instead. Since BigDecimal is preferred
     * over Number, the Number*Number method is not chosen as in older
     * versions of Groovy.
     *
     * @param left  a BigDecimal
     * @param right a Double
     * @return the multiplication of left by right
     * @since 1.0
     */
    public static Number multiply(BigDecimal left, Double right) {
        return NumberMath.multiply(left, right);
    }

    /**
     * Multiply a BigDecimal and a BigInteger.
     * Note: This method was added to enforce the Groovy rule of
     * BigDecimal*long == long. Without this method, the
     * multiply(BigDecimal) method in BigDecimal would respond
     * and return a BigDecimal instead. Since BigDecimal is preferred
     * over Number, the Number*Number method is not chosen as in older
     * versions of Groovy. BigInteger is the fallback for all integer
     * types in Groovy
     *
     * @param left  a BigDecimal
     * @param right a BigInteger
     * @return the multiplication of left by right
     * @since 1.0
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
     * @since 1.0
     */
    public static Number power(Number self, Number exponent) {
        double base, exp, answer;
        base = self.doubleValue();
        exp = exponent.doubleValue();

        answer = Math.pow(base, exp);
        if ((double) ((int) answer) == answer) {
            return (int) answer;
        } else if ((double) ((long) answer) == answer) {
            return (long) answer;
        } else {
            return answer;
        }
    }

    /**
     * Divide a Character by a Number. The ordinal value of the Character
     * is used in the division (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     *
     * @param left  a Character
     * @param right a Number
     * @return the Number corresponding to the division of left by right
     * @since 1.0
     */
    public static Number div(Character left, Number right) {
        return NumberNumberDiv.div(Integer.valueOf(left), right);
    }

    /**
     * Divide a Number by a Character. The ordinal value of the Character
     * is used in the division (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     *
     * @param left  a Number
     * @param right a Character
     * @return the Number corresponding to the division of left by right
     * @since 1.0
     */
    public static Number div(Number left, Character right) {
        return NumberNumberDiv.div(left, Integer.valueOf(right));
    }

    /**
     * Divide one Character by another. The ordinal values of the Characters
     * are used in the division (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     *
     * @param left  a Character
     * @param right another Character
     * @return the Number corresponding to the division of left by right
     * @since 1.0
     */
    public static Number div(Character left, Character right) {
        return div(Integer.valueOf(left), right);
    }

    /**
     * Integer Divide a Character by a Number. The ordinal value of the Character
     * is used in the division (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     *
     * @param left  a Character
     * @param right a Number
     * @return a Number (an Integer) resulting from the integer division operation
     * @since 1.0
     */
    public static Number intdiv(Character left, Number right) {
        return intdiv(Integer.valueOf(left), right);
    }

    /**
     * Integer Divide a Number by a Character. The ordinal value of the Character
     * is used in the division (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     *
     * @param left  a Number
     * @param right a Character
     * @return a Number (an Integer) resulting from the integer division operation
     * @since 1.0
     */
    public static Number intdiv(Number left, Character right) {
        return intdiv(left, Integer.valueOf(right));
    }

    /**
     * Integer Divide two Characters. The ordinal values of the Characters
     * are used in the division (the ordinal value is the unicode
     * value which for simple character sets is the ASCII value).
     *
     * @param left  a Character
     * @param right another Character
     * @return a Number (an Integer) resulting from the integer division operation
     * @since 1.0
     */
    public static Number intdiv(Character left, Character right) {
        return intdiv(Integer.valueOf(left), right);
    }

    /**
     * Integer Divide two Numbers.
     *
     * @param left  a Number
     * @param right another Number
     * @return a Number (an Integer) resulting from the integer division operation
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.5.0
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
     * @since 1.5.0
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
     * @since 1.5.0
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
     * @since 1.5.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.5.0
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
     * @since 1.0
     */
    public static void times(Number self, Closure closure) {
        for (int i = 0, size = self.intValue(); i < size; i++) {
            closure.call(i);
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
     * @since 1.0
     */
    public static void upto(Number self, Number to, Closure closure) {
        int self1 = self.intValue();
        int to1 = to.intValue();
        if (self1 <= to1) {
            for (int i = self1; i <= to1; i++) {
                closure.call(i);
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
     * @since 1.0
     */
    public static void upto(long self, Number to, Closure closure) {
        long to1 = to.longValue();
        if (self <= to1) {
            for (long i = self; i <= to1; i++) {
                closure.call(i);
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
     * @since 1.0
     */
    public static void upto(Long self, Number to, Closure closure) {
        long to1 = to.longValue();
        if (self <= to1) {
            for (long i = self; i <= to1; i++) {
                closure.call(i);
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
     * @since 1.0
     */
    public static void upto(float self, Number to, Closure closure) {
        float to1 = to.floatValue();
        if (self <= to1) {
            for (float i = self; i <= to1; i++) {
                closure.call(i);
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
     * @since 1.0
     */
    public static void upto(Float self, Number to, Closure closure) {
        float to1 = to.floatValue();
        if (self <= to1) {
            for (float i = self; i <= to1; i++) {
                closure.call(i);
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
     * @since 1.0
     */
    public static void upto(double self, Number to, Closure closure) {
        double to1 = to.doubleValue();
        if (self <= to1) {
            for (double i = self; i <= to1; i++) {
                closure.call(i);
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
     * @since 1.0
     */
    public static void upto(Double self, Number to, Closure closure) {
        double to1 = to.doubleValue();
        if (self <= to1) {
            for (double i = self; i <= to1; i++) {
                closure.call(i);
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
     * @since 1.0
     */
    public static void upto(BigInteger self, Number to, Closure closure) {
        if (to instanceof BigDecimal) {
            final BigDecimal one = BigDecimal.valueOf(10, 1);
            BigDecimal self1 = new BigDecimal(self);
            BigDecimal to1 = (BigDecimal) to;
            if (self1.compareTo(to1) <= 0) {
                for (BigDecimal i = self1; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to + ")");
        } else if (to instanceof BigInteger) {
            final BigInteger one = BigInteger.valueOf(1);
            BigInteger to1 = (BigInteger) to;
            if (self.compareTo(to1) <= 0) {
                for (BigInteger i = self; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".upto(" + to + ")");
        } else {
            final BigInteger one = BigInteger.valueOf(1);
            BigInteger to1 = new BigInteger(to.toString());
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
     * <pre>0.1.upto( 10 ) {
     *   println it
     * }</pre>
     * Prints numbers 0.1, 1.1, 2.1... to 9.1
     *
     * @param self    a BigDecimal
     * @param to the end number
     * @param closure the code to execute for each number
     * @since 1.0
     */
    public static void upto(BigDecimal self, Number to, Closure closure) {
        final BigDecimal one = BigDecimal.valueOf(10, 1);  // That's what you get for "1.0".
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
            BigDecimal to1 = new BigDecimal(to.toString());
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
     * @since 1.0
     */
    public static void downto(Number self, Number to, Closure closure) {
        int self1 = self.intValue();
        int to1 = to.intValue();
        if (self1 >= to1) {
            for (int i = self1; i >= to1; i--) {
                closure.call(i);
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
     * @since 1.0
     */
    public static void downto(long self, Number to, Closure closure) {
        long to1 = to.longValue();
        if (self >= to1) {
            for (long i = self; i >= to1; i--) {
                closure.call(i);
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
     * @since 1.0
     */
    public static void downto(Long self, Number to, Closure closure) {
        long to1 = to.longValue();
        if (self >= to1) {
            for (long i = self; i >= to1; i--) {
                closure.call(i);
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
     * @since 1.0
     */
    public static void downto(float self, Number to, Closure closure) {
        float to1 = to.floatValue();
        if (self >= to1) {
            for (float i = self; i >= to1; i--) {
                closure.call(i);
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
     * @since 1.0
     */
    public static void downto(Float self, Number to, Closure closure) {
        float to1 = to.floatValue();
        if (self >= to1) {
            for (float i = self; i >= to1; i--) {
                closure.call(i);
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
     * @since 1.0
     */
    public static void downto(double self, Number to, Closure closure) {
        double to1 = to.doubleValue();
        if (self >= to1) {
            for (double i = self; i >= to1; i--) {
                closure.call(i);
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
     * @since 1.0
     */
    public static void downto(Double self, Number to, Closure closure) {
        double to1 = to.doubleValue();
        if (self >= to1) {
            for (double i = self; i >= to1; i--) {
                closure.call(i);
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
     * @since 1.0
     */
    public static void downto(BigInteger self, Number to, Closure closure) {
        if (to instanceof BigDecimal) {
            final BigDecimal one = BigDecimal.valueOf(10, 1);  // That's what you get for "1.0".
            final BigDecimal to1 = (BigDecimal) to;
            final BigDecimal selfD = new BigDecimal(self);
            if (selfD.compareTo(to1) >= 0) {
                for (BigDecimal i = selfD; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i.toBigInteger());
                }
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
        } else if (to instanceof BigInteger) {
            final BigInteger one = BigInteger.valueOf(1);
            final BigInteger to1 = (BigInteger) to;
            if (self.compareTo(to1) >= 0) {
                for (BigInteger i = self; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("Infinite loop in " + self + ".downto(" + to + ")");
        } else {
            final BigInteger one = BigInteger.valueOf(1);
            final BigInteger to1 = new BigInteger(to.toString());
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
     * @since 1.0
     */
    public static void downto(BigDecimal self, Number to, Closure closure) {
        final BigDecimal one = BigDecimal.valueOf(10, 1);  // Quick way to get "1.0".
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
            BigDecimal to1 = new BigDecimal(to.toString());
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
     * @since 1.0
     */
    public static void step(Number self, Number to, Number stepNumber, Closure closure) {
        if (self instanceof BigDecimal || to instanceof BigDecimal || stepNumber instanceof BigDecimal) {
            final BigDecimal zero = BigDecimal.valueOf(0, 1);  // Same as "0.0".
            BigDecimal self1 = (self instanceof BigDecimal) ? (BigDecimal) self : new BigDecimal(self.toString());
            BigDecimal to1 = (to instanceof BigDecimal) ? (BigDecimal) to : new BigDecimal(to.toString());
            BigDecimal stepNumber1 = (stepNumber instanceof BigDecimal) ? (BigDecimal) stepNumber : new BigDecimal(stepNumber.toString());
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
            final BigInteger zero = BigInteger.valueOf(0);
            BigInteger self1 = (self instanceof BigInteger) ? (BigInteger) self : new BigInteger(self.toString());
            BigInteger to1 = (to instanceof BigInteger) ? (BigInteger) to : new BigInteger(to.toString());
            BigInteger stepNumber1 = (stepNumber instanceof BigInteger) ? (BigInteger) stepNumber : new BigInteger(stepNumber.toString());
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
                    closure.call(i);
                }
            } else if (stepNumber1 < 0 && to1 < self1) {
                for (int i = self1; i > to1; i += stepNumber1) {
                    closure.call(i);
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
     * @since 1.0
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
     * @since 1.0
     */
    public static long abs(Long number) {
        return Math.abs(number.longValue());
    }

    /**
     * Get the absolute value
     *
     * @param number a Float
     * @return the absolute value of that Float
     * @since 1.0
     */
    public static float abs(Float number) {
        return Math.abs(number.floatValue());
    }

    /**
     * Get the absolute value
     *
     * @param number a Double
     * @return the absolute value of that Double
     * @since 1.0
     */
    public static double abs(Double number) {
        return Math.abs(number);
    }

    /**
     * Round the value
     *
     * @param number a Float
     * @return the rounded value of that Float
     * @since 1.0
     */
    public static int round(Float number) {
        return Math.round(number.floatValue());
    }
    
    /**
     * Round the value
     *
     * @param number a Float
     * @param precision the number of decimal places to keep
     * @return the Float rounded to the number of decimal places specified by precision
     * @since 1.6.0
     */
    public static float round(Float number, int precision) {
        return (float)(Math.floor(number.doubleValue()*Math.pow(10,precision)+0.5)/Math.pow(10,precision));
    }
    
    /**
     * Truncate the value
     *
     * @param number a Float
     * @param precision the number of decimal places to keep
     * @return the Float truncated to the number of decimal places specified by precision
     * @since 1.6.0
     */
    public static float trunc(Float number, int precision) {
        return (float)(Math.floor(number.doubleValue()*Math.pow(10,precision))/Math.pow(10,precision));
    }
    
    /**
     * Truncate the value
     *
     * @param number a Double
     * @return the Double truncated to 0 decimal places (i.e. a synonym for floor)
     * @since 1.6.0
     */
    public static float trunc(Float number) {
        return (float)Math.floor(number.doubleValue());
    }

    /**
     * Round the value
     *
     * @param number a Double
     * @return the rounded value of that Double
     * @since 1.0
     */
    public static long round(Double number) {
        return Math.round(number);
    }
    
    /**
     * Round the value
     *
     * @param number a Double
     * @param precision the number of decimal places to keep
     * @return the Double rounded to the number of decimal places specified by precision
     * @since 1.6.4
     */
    public static double round(Double number, int precision) {
        return Math.floor(number *Math.pow(10,precision)+0.5)/Math.pow(10,precision);
    }
    
    /**
     * Truncate the value
     *
     * @param number a Double
     * @return the Double truncated to 0 decimal places (i.e. a synonym for floor)
     * @since 1.6.4
     */
    public static double trunc(Double number) {
        return Math.floor(number);
    }
    
    /**
     * Truncate the value
     *
     * @param number a Double
     * @param precision the number of decimal places to keep
     * @return the Double truncated to the number of decimal places specified by precision
     * @since 1.6.4
     */
    public static double trunc(Double number, int precision) {
        return Math.floor(number *Math.pow(10,precision))/Math.pow(10,precision);
    }

    /**
     * Parse a String into an Integer
     *
     * @param self a String
     * @return an Integer
     * @since 1.0
     */
    public static Integer toInteger(String self) {
        return Integer.valueOf(self.trim());
    }

    /**
     * Parse a String into a Long
     *
     * @param self a String
     * @return a Long
     * @since 1.0
     */
    public static Long toLong(String self) {
        return Long.valueOf(self.trim());
    }

    /**
     * Parse a String into a Short
     *
     * @param self a String
     * @return a Short
     * @since 1.5.7
     */
    public static Short toShort(String self) {
        return Short.valueOf(self.trim());
    }

    /**
     * Parse a String into a Float
     *
     * @param self a String
     * @return a Float
     * @since 1.0
     */
    public static Float toFloat(String self) {
        return Float.valueOf(self.trim());
    }

    /**
     * Parse a String into a Double
     *
     * @param self a String
     * @return a Double
     * @since 1.0
     */
    public static Double toDouble(String self) {
        return Double.valueOf(self.trim());
    }

    /**
     * Parse a String into a BigInteger
     *
     * @param self a String
     * @return a BigInteger
     * @since 1.0
     */
    public static BigInteger toBigInteger(String self) {
        return new BigInteger(self.trim());
    }

    /**
     * Parse a String into a BigDecimal
     *
     * @param self a String
     * @return a BigDecimal
     * @since 1.0
     */
    public static BigDecimal toBigDecimal(String self) {
        return new BigDecimal(self.trim());
    }

    /**
     * Determine if a String can be parsed into an Integer.
     *
     * @param self a String
     * @return true if the string can be parsed
     * @since 1.5.0
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
     * @since 1.5.0
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
     * @since 1.5.0
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
     * @since 1.5.0
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
     * @since 1.5.0
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
     * @since 1.5.0
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
     * @since 1.5.0
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
     * @since 1.5.7
     */
    public static boolean isUpperCase(Character self) {
        return Character.isUpperCase(self);
    }

    /**
     * Determine if a Character is lowercase.
     * Synonym for 'Character.isLowerCase(this)'.
     *
     * @param self a Character
     * @return true if the character is lowercase
     * @see java.lang.Character#isLowerCase(char)
     * @since 1.5.7
     */
    public static boolean isLowerCase(Character self) {
        return Character.isLowerCase(self);
    }

    /**
     * Determines if a character is a letter.
     * Synonym for 'Character.isLetter(this)'.
     *
     * @param self a Character
     * @return true if the character is a letter
     * @see java.lang.Character#isLetter(char)
     * @since 1.5.7
     */
    public static boolean isLetter(Character self) {
        return Character.isLetter(self);
    }

    /**
     * Determines if a character is a digit.
     * Synonym for 'Character.isDigit(this)'.
     *
     * @param self a Character
     * @return true if the character is a digit
     * @see java.lang.Character#isDigit(char)
     * @since 1.5.7
     */
    public static boolean isDigit(Character self) {
        return Character.isDigit(self);
    }

    /**
     * Determines if a character is a letter or digit.
     * Synonym for 'Character.isLetterOrDigit(this)'.
     *
     * @param self a Character
     * @return true if the character is a letter or digit
     * @see java.lang.Character#isLetterOrDigit(char)
     * @since 1.5.7
     */
    public static boolean isLetterOrDigit(Character self) {
        return Character.isLetterOrDigit(self);
    }

    /**
     * Determines if a character is a whitespace character.
     * Synonym for 'Character.isWhitespace(this)'.
     *
     * @param self a Character
     * @return true if the character is a whitespace character
     * @see java.lang.Character#isWhitespace(char)
     * @since 1.5.7
     */
    public static boolean isWhitespace(Character self) {
        return Character.isWhitespace(self);
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
     * @since 1.5.7
     */
    public static char toUpperCase(Character self) {
        return Character.toUpperCase(self);
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
     * @since 1.5.7
     */
    public static char toLowerCase(Character self) {
        return Character.toLowerCase(self);
    }

    /**
     * Transform a Number into an Integer
     *
     * @param self a Number
     * @return an Integer
     * @since 1.0
     */
    public static Integer toInteger(Number self) {
        return self.intValue();
    }

    /**
     * Transform a Number into a Long
     *
     * @param self a Number
     * @return an Long
     * @since 1.0
     */
    public static Long toLong(Number self) {
        return self.longValue();
    }

    /**
     * Transform a Number into a Float
     *
     * @param self a Number
     * @return an Float
     * @since 1.0
     */
    public static Float toFloat(Number self) {
        return self.floatValue();
    }

    /**
     * Transform a Number into a Double
     *
     * @param self a Number
     * @return an Double
     * @since 1.0
     */
    public static Double toDouble(Number self) {
        // Conversions in which all decimal digits are known to be good.
        if ((self instanceof Double)
            || (self instanceof Long) 
            || (self instanceof Integer)
            || (self instanceof Short)
            || (self instanceof Byte)) 
        {
            return self.doubleValue();
        }
        
        // Chances are this is a Float or a Big.
        // With Float we're extending binary precision and that gets ugly in decimal.
        // If we used Float.doubleValue() on 0.1f we get 0.10000000149011612.
        // Note that this is different than casting '(double) 0.1f' which will do the
        // binary extension just like in Java.
        // With Bigs and other unkowns, this is likely to be the same.
        
        return Double.valueOf(self.toString());
    }

    /**
     * Transform a Number into a BigDecimal
     *
     * @param self a Number
     * @return an BigDecimal
     * @since 1.0
     */
    public static BigDecimal toBigDecimal(Number self) {
        // Quick method for scalars.
        if ((self instanceof Long) 
            || (self instanceof Integer)
            || (self instanceof Short)
            || (self instanceof Byte)) 
        {
            return BigDecimal.valueOf(self.longValue());
        }
        
        return new BigDecimal(self.toString());
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
     * @since 1.0
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
     * @since 1.0
     */
    public static BigInteger toBigInteger(Number self) {
        if (self instanceof BigInteger) {
            return (BigInteger) self;
        } else if (self instanceof BigDecimal) {
            return ((BigDecimal) self).toBigInteger();
        } else if (self instanceof Double) {
            return new BigDecimal((Double)self).toBigInteger();
        } else if (self instanceof Float) {
            return new BigDecimal((Float)self).toBigInteger();
        } else {
            return new BigInteger(Long.toString(self.longValue()));
        }
    }

    // Date methods
    //-------------------------------------------------------------------------

    /**
     * Increment a Date by one day.
     *
     * @param self a Date
     * @return the next days date
     * @since 1.0
     */
    public static Date next(Date self) {
        return plus(self, 1);
    }

    /**
     * Increment a java.sql.Date by one day.
     *
     * @param self a java.sql.Date
     * @return the next days date
     * @since 1.0
     */
    public static java.sql.Date next(java.sql.Date self) {
        return new java.sql.Date(next((Date) self).getTime());
    }

    /**
     * Decrement a Date by one day.
     *
     * @param self a Date
     * @return the previous days date
     * @since 1.0
     */
    public static Date previous(Date self) {
        return minus(self, 1);
    }

    /**
     * Decrement a java.sql.Date by one day.
     *
     * @param self a java.sql.Date
     * @return the previous days date
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.6.0
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
     * @since 1.6.0
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
     * format pattern.</p>
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
     * @param self a Date
     * @param format the format pattern to use according to {@link SimpleDateFormat}
     * @return a string representation of this date.
     * @since 1.5.7
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
     * @param self a Date
     * @return a string representation of this date
     * @since 1.5.7
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
     * @param self a Date
     * @return a string representing the time portion of this date
     * @since 1.5.7
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
     * @param self a Date
     * @return a string representation of this date and time
     * @since 1.5.7
     */
    public static String getDateTimeString( Date self ) {
    	return DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.MEDIUM).format( self );
    }

    /**
     * Common code for {@link #clearTime(Calendar)} and {@link #clearTime(Date)}
     * and {@link #clearTime(java.sql.Date)}
     */
    private static void clearTimeCommon(final Calendar self) {
		self.clear(Calendar.HOUR_OF_DAY);
        self.clear(Calendar.HOUR);
        self.clear(Calendar.MINUTE);
        self.clear(Calendar.SECOND);
        self.clear(Calendar.MILLISECOND);
	}

    /**
     * Clears the time portion of this Date instance; Util where it makes sense to
     * compare month/day/year only portions of a Date
     *
     * @param self a Date
     *
     * @since 1.6.7
     */
	public static void clearTime(final Date self){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(self);
		clearTimeCommon(calendar);
		self.setTime(calendar.getTime().getTime());
	}

    /**
     * Clears the time portion of this java.sql.Date instance; Util where it makes sense to
     * compare month/day/year only portions of a Date
     *
     * @param self a java.sql.Date
     *
     * @since 1.6.7
     */
	public static void clearTime(final java.sql.Date self){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(self);
		clearTimeCommon(calendar);
		self.setTime(calendar.getTime().getTime());
	}

    /**
     * Clears the time portion of this Calendar instance; Util where it makes sense to
     *
     * compare month/day/year only portions of a Calendar
     * @param self a Calendar
     *
     * @since 1.6.7
     */
	public static void clearTime(final Calendar self){
		clearTimeCommon(self);
	}
    
    /**
     * <p>Shortcut for {@link SimpleDateFormat} to output a String representation
     * of this calendar instance.  This method respects the Calendar's assigned 
     * {@link TimeZone}, whereas calling <code>cal.time.format('HH:mm:ss')</code> 
     * would use the system timezone.</p>
     * <p>Note that Calendar equivalents of <code>date.getDateString()</code> 
     * and variants do not exist because those methods are Locale-dependent.  
     * Although a Calendar may be assigned a {@link Locale}, that information is 
     * lost and therefore cannot be used to control the default date/time formats 
     * provided by these methods.  Instead, the system Locale would always be 
     * used.  The alternative is to simply call 
     * {@link DateFormat#getDateInstance(int, Locale)} and pass the same Locale
     * that was used for the Calendar.</p>
     * 
     * @see DateFormat#setTimeZone(TimeZone)
     * @see SimpleDateFormat#format(Date)
     * @see #format(Date, String)
     * @param self this calendar
     * @param pattern format pattern
     * @return String representation of this calendar with the given format.
     * @since 1.6.0
     */
    public static String format( Calendar self, String pattern ) {
    	SimpleDateFormat sdf = new SimpleDateFormat( pattern );
    	sdf.setTimeZone( self.getTimeZone() );
    	return sdf.format( self.getTime() );
    }
    
    // Boolean based methods
    //-------------------------------------------------------------------------


    /**
     * Logical conjunction of two boolean operators.
     *
     * @param left left operator
     * @param right right operator
     * @return result of logical conjunction
     * @since 1.0
     */
    public static Boolean and(Boolean left, Boolean right) {
        return left && right;
    }

    /**
     * Logical disjunction of two boolean operators
     *
     * @param left left operator
     * @param right right operator
     * @return result of logical disjunction
     * @since 1.0
     */
    public static Boolean or(Boolean left, Boolean right) {
        return left || right;
    }

    /**
     * Exclusive disjunction of two boolean operators
     *
     * @param left left operator
     * @param right right operator
     * @return result of exclusive disjunction
     * @since 1.0
     */
    public static Boolean xor(Boolean left, Boolean right) {
        return left ^ right;
    }

//    public static Boolean negate(Boolean left) {
//        return Boolean.valueOf(!left.booleanValue());
//    }

    // File and stream based methods
    //-------------------------------------------------------------------------

    /**
     * Create an object output stream for this file.
     *
     * @param file a file
     * @return an object output stream
     * @throws IOException if an IOException occurs.
     * @since 1.5.0
     */
    public static ObjectOutputStream newObjectOutputStream(File file) throws IOException {
        return new ObjectOutputStream(new FileOutputStream(file));
    }

    /**
     * Create an object output stream for this output stream.
     *
     * @param outputStream an output stream
     * @return an object output stream
     * @throws IOException if an IOException occurs.
     * @since 1.5.0
     */
    public static ObjectOutputStream newObjectOutputStream(OutputStream outputStream) throws IOException {
        return new ObjectOutputStream(outputStream);
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
     * @since 1.5.0
     */
    public static Object withObjectOutputStream(File file, Closure closure) throws IOException {
        return withStream(newObjectOutputStream(file), closure);
    }

    /**
     * Create a new ObjectOutputStream for this output stream and then pass it to the
     * closure.  This method ensures the stream is closed after the closure
     * returns.
     *
     * @param outputStream am output stream
     * @param closure      a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withStream(OutputStream,Closure)
     * @since 1.5.0
     */
    public static Object withObjectOutputStream(OutputStream outputStream, Closure closure) throws IOException {
        return withStream(newObjectOutputStream(outputStream), closure);
    }

    /**
     * Create an object input stream for this file.
     *
     * @param file a file
     * @return an object input stream
     * @throws IOException if an IOException occurs.
     * @since 1.5.0
     */
    public static ObjectInputStream newObjectInputStream(File file) throws IOException {
        return new ObjectInputStream(new FileInputStream(file));
    }

    /**
     * Create an object input stream for this input stream.
     *
     * @param inputStream an input stream
     * @return an object input stream
     * @throws IOException if an IOException occurs.
     * @since 1.5.0
     */
    public static ObjectInputStream newObjectInputStream(InputStream inputStream) throws IOException {
        return new ObjectInputStream(inputStream);
    }

    /**
     * Create an object input stream for this input stream using the given class loader.
     *
     * @param inputStream an input stream
     * @param classLoader the class loader to use when loading the class
     * @return an object input stream
     * @throws IOException if an IOException occurs.
     * @since 1.5.0
     */
    public static ObjectInputStream newObjectInputStream(InputStream inputStream, final ClassLoader classLoader) throws IOException {
        return new ObjectInputStream(inputStream) {
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                return Class.forName(desc.getName(), true, classLoader);

            }
        };
    }

    /**
     * Create an object input stream for this file using the given class loader.
     *
     * @param file        a file
     * @param classLoader the class loader to use when loading the class
     * @return an object input stream
     * @throws IOException if an IOException occurs.
     * @since 1.5.0
     */
    public static ObjectInputStream newObjectInputStream(File file, final ClassLoader classLoader) throws IOException {
        return newObjectInputStream(new FileInputStream(file), classLoader);
    }

    /**
     * Iterates through the given file object by object.
     *
     * @param self    a File
     * @param closure a closure
     * @throws IOException            if an IOException occurs.
     * @throws ClassNotFoundException if the class  is not found.
     * @see #eachObject(ObjectInputStream,Closure)
     * @since 1.0
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
     * @since 1.0
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
            closeWithWarning(ois);
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
     * @since 1.5.2
     */
    public static Object withObjectInputStream(File file, Closure closure) throws IOException {
        return withStream(newObjectInputStream(file), closure);
    }

    /**
     * Create a new ObjectInputStream for this file associated with the given class loader and pass it to the closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param file        a File
     * @param classLoader the class loader to use when loading the class
     * @param closure     a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withStream(InputStream,Closure)
     * @since 1.5.2
     */
    public static Object withObjectInputStream(File file, ClassLoader classLoader, Closure closure) throws IOException {
        return withStream(newObjectInputStream(file, classLoader), closure);
    }

    /**
     * Create a new ObjectInputStream for this file and pass it to the closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param inputStream an input stream
     * @param closure     a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withStream(InputStream,Closure)
     * @since 1.5.0
     */
    public static Object withObjectInputStream(InputStream inputStream, Closure closure) throws IOException {
        return withStream(newObjectInputStream(inputStream), closure);
    }

    /**
     * Create a new ObjectInputStream for this file and pass it to the closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param inputStream an input stream
     * @param classLoader the class loader to use when loading the class
     * @param closure     a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withStream(InputStream,Closure)
     * @since 1.5.0
     */
    public static Object withObjectInputStream(InputStream inputStream, ClassLoader classLoader, Closure closure) throws IOException {
        return withStream(newObjectInputStream(inputStream, classLoader), closure);
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
     * @since 1.5.5
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
     * @param firstLine the line number value used for the first line (default is 1, set to 0 to start counting from 0)
     * @param closure a closure (arg 1 is line, optional arg 2 is line number)
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     * @since 1.5.7
     */
    public static Object eachLine(String self, int firstLine, Closure closure) throws IOException {
        int count = firstLine;
        String line = null;
        for (Object o : readLines(self)) {
            line = (String) o;
            callClosureForLine(closure, line, count);
            count++;
        }
        return line;
    }

    /**
     * Iterates through this file line by line.  Each line is passed to the
     * given 1 or 2 arg closure.  The file is read using a reader which
     * is closed before this method returns.
     *
     * @param self    a File
     * @param closure a closure (arg 1 is line, optional arg 2 is line number starting at line 1)
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #eachLine(java.io.File, int, groovy.lang.Closure)
     * @since 1.5.5
     */
    public static Object eachLine(File self, Closure closure) throws IOException {
        return eachLine(self, 1, closure);
    }

    /**
     * Iterates through this file line by line.  Each line is passed to the
     * given 1 or 2 arg closure.  The file is read using a reader which
     * is closed before this method returns.
     *
     * @param self    a File
     * @param charset opens the file with a specified charset
     * @param closure a closure (arg 1 is line, optional arg 2 is line number starting at line 1)
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #eachLine(java.io.File, String, int, groovy.lang.Closure)
     * @since 1.6.8
     */
    public static Object eachLine(File self, String charset, Closure closure) throws IOException {
        return eachLine(self, charset, 1, closure);
    }

    /**
     * Iterates through this file line by line.  Each line is passed
     * to the given 1 or 2 arg closure.  The file is read using a reader
     * which is closed before this method returns.
     *
     * @param self    a File
     * @param firstLine the line number value used for the first line (default is 1, set to 0 to start counting from 0)
     * @param closure a closure (arg 1 is line, optional arg 2 is line number)
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #eachLine(java.io.Reader, int, groovy.lang.Closure)
     * @since 1.5.7
     */
    public static Object eachLine(File self, int firstLine, Closure closure) throws IOException {
        return eachLine(newReader(self), firstLine, closure);
    }

    /**
     * Iterates through this file line by line.  Each line is passed
     * to the given 1 or 2 arg closure.  The file is read using a reader
     * which is closed before this method returns.
     *
     * @param self    a File
     * @param charset opens the file with a specified charset
     * @param firstLine the line number value used for the first line (default is 1, set to 0 to start counting from 0)
     * @param closure a closure (arg 1 is line, optional arg 2 is line number)
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #eachLine(java.io.Reader, int, groovy.lang.Closure)
     * @since 1.6.8
     */
    public static Object eachLine(File self, String charset, int firstLine, Closure closure) throws IOException {
        return eachLine(newReader(self, charset), firstLine, closure);
    }

    /**
     * Iterates through this stream reading with the provided charset, passing each line to the
     * given 1 or 2 arg closure.  The stream is closed before this method returns.
     *
     * @param stream  a stream
     * @param charset opens the stream with a specified charset
     * @param closure a closure (arg 1 is line, optional arg 2 is line number starting at line 1)
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #eachLine(java.io.InputStream, String, int, groovy.lang.Closure)
     * @since 1.5.5
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
     * @param firstLine the line number value used for the first line (default is 1, set to 0 to start counting from 0)
     * @param closure a closure (arg 1 is line, optional arg 2 is line number)
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #eachLine(java.io.Reader, int, groovy.lang.Closure)
     * @since 1.5.7
     */
    public static Object eachLine(InputStream stream, String charset, int firstLine, Closure closure) throws IOException {
        return eachLine(new InputStreamReader(stream, charset), firstLine, closure);
    }

    /**
     * Iterates through this stream, passing each line to the given 1 or 2 arg closure.
     * The stream is closed before this method returns.
     *
     * @param stream  a stream
     * @param closure a closure (arg 1 is line, optional arg 2 is line number starting at line 1)
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #eachLine(java.io.InputStream, int, groovy.lang.Closure)
     * @since 1.5.6
     */
    public static Object eachLine(InputStream stream, Closure closure) throws IOException {
        return eachLine(stream, 1, closure);
    }

    /**
     * Iterates through this stream, passing each line to the given 1 or 2 arg closure.
     * The stream is closed before this method returns.
     *
     * @param stream  a stream
     * @param firstLine the line number value used for the first line (default is 1, set to 0 to start counting from 0)
     * @param closure a closure (arg 1 is line, optional arg 2 is line number)
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #eachLine(java.io.Reader, int, groovy.lang.Closure)
     * @since 1.5.7
     */
    public static Object eachLine(InputStream stream, int firstLine, Closure closure) throws IOException {
        return eachLine(new InputStreamReader(stream), firstLine, closure);
    }

    /**
     * Iterates through the lines read from the URL's associated input stream passing each
     * line to the given 1 or 2 arg closure. The stream is closed before this method returns.
     *
     * @param url     a URL to open and read
     * @param closure a closure to apply on each line (arg 1 is line, optional arg 2 is line number starting at line 1)
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #eachLine(java.net.URL, int, groovy.lang.Closure)
     * @since 1.5.6
     */
    public static Object eachLine(URL url, Closure closure) throws IOException {
        return eachLine(url, 1, closure);
    }

    /**
     * Iterates through the lines read from the URL's associated input stream passing each
     * line to the given 1 or 2 arg closure. The stream is closed before this method returns.
     *
     * @param url       a URL to open and read
     * @param firstLine the line number value used for the first line (default is 1, set to 0 to start counting from 0)
     * @param closure   a closure to apply on each line (arg 1 is line, optional arg 2 is line number)
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #eachLine(java.io.InputStream, int, groovy.lang.Closure)
     * @since 1.5.7
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
     * @param closure a closure to apply on each line (arg 1 is line, optional arg 2 is line number starting at line 1)
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #eachLine(java.net.URL, String, int, groovy.lang.Closure)
     * @since 1.5.6
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
     * @param firstLine the line number value used for the first line (default is 1, set to 0 to start counting from 0)
     * @param closure   a closure to apply on each line (arg 1 is line, optional arg 2 is line number)
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #eachLine(java.io.Reader, int, groovy.lang.Closure)
     * @since 1.5.7
     */
    public static Object eachLine(URL url, String charset, int firstLine, Closure closure) throws IOException {
        return eachLine(newReader(url, charset), firstLine, closure);
    }

    /**
     * Iterates through the given reader line by line.  Each line is passed to the
     * given 1 or 2 arg closure. If the closure has two arguments, the line count is passed
     * as the second argument. The Reader is closed before this method returns.
     *
     * @param self    a Reader, closed after the method returns
     * @param closure a closure (arg 1 is line, optional arg 2 is line number starting at line 1)
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #eachLine(java.io.Reader, int, groovy.lang.Closure)
     * @since 1.5.6
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
     * @param firstLine the line number value used for the first line (default is 1, set to 0 to start counting from 0)
     * @param closure   a closure which will be passed each line (or for 2 arg closures the line and line count)
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @since 1.5.7
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
            closeWithWarning(self);
            closeWithWarning(br);
        }
    }

    /**
     * Iterates through this file line by line, splitting each line using
     * the given regex separator. For each line, the given closure is called with
     * a single parameter being the list of strings computed by splitting the line
     * around matches of the given regular expression.
     * Finally the resources used for processing the file are closed.
     *
     * @param self    a File
     * @param regex   the delimiting regular expression
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @return the last value returned by the closure
     * @see #splitEachLine(Reader,String,Closure)
     * @since 1.5.5
     */
    public static Object splitEachLine(File self, String regex, Closure closure) throws IOException {
        return splitEachLine(newReader(self), regex, closure);
    }

    /**
     * Iterates through this file line by line, splitting each line using
     * the given regex separator. For each line, the given closure is called with
     * a single parameter being the list of strings computed by splitting the line
     * around matches of the given regular expression.
     * Finally the resources used for processing the file are closed.
     *
     * @param self    a File
     * @param regex   the delimiting regular expression
     * @param charset opens the file with a specified charset
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @return the last value returned by the closure
     * @see #splitEachLine(Reader,String,Closure)
     * @since 1.6.8
     */
    public static Object splitEachLine(File self, String regex, String charset, Closure closure) throws IOException {
        return splitEachLine(newReader(self, charset), regex, closure);
    }

    /**
     * Iterates through the input stream associated with this URL line by line, splitting each line using
     * the given regex separator. For each line, the given closure is called with
     * a single parameter being the list of strings computed by splitting the line
     * around matches of the given regular expression.
     * Finally the resources used for processing the URL are closed.
     *
     * @param self    a URL to open and read
     * @param regex   the delimiting regular expression
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @return the last value returned by the closure
     * @see #splitEachLine(Reader,String,Closure)
     * @since 1.6.8
     */
    public static Object splitEachLine(URL self, String regex, Closure closure) throws IOException {
        return splitEachLine(newReader(self), regex, closure);
    }

    /**
     * Iterates through the input stream associated with this URL line by line, splitting each line using
     * the given regex separator. For each line, the given closure is called with
     * a single parameter being the list of strings computed by splitting the line
     * around matches of the given regular expression.
     * Finally the resources used for processing the URL are closed.
     *
     * @param self    a URL to open and read
     * @param regex   the delimiting regular expression
     * @param charset opens the file with a specified charset
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @return the last value returned by the closure
     * @see #splitEachLine(Reader,String,Closure)
     * @since 1.6.8
     */
    public static Object splitEachLine(URL self, String regex, String charset, Closure closure) throws IOException {
        return splitEachLine(newReader(self, charset), regex, closure);
    }

    /**
     * Iterates through the given reader line by line, splitting each line using
     * the given regex separator. For each line, the given closure is called with
     * a single parameter being the list of strings computed by splitting the line
     * around matches of the given regular expression.  The Reader is closed afterwards.
     * <p/>
     * Here is an example:
     * <pre>
     * def s = 'The 3 quick\nbrown 4 fox'
     * def result = ''
     * new StringReader(s).splitEachLine(/\d/){ parts ->
     *     result += "${parts[0]}_${parts[1]}|"
     * }
     * assert result == 'The _ quick|brown _ fox|'
     * </pre>
     *
     * @param self    a Reader, closed after the method returns
     * @param regex   the delimiting regular expression
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @return the last value returned by the closure
     * @see java.lang.String#split(String)
     * @since 1.5.5
     */
    public static Object splitEachLine(Reader self, String regex, Closure closure) throws IOException {
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
                    List vals = Arrays.asList(line.split(regex));
                    result = closure.call(vals);
                }
            }
            Reader temp = self;
            self = null;
            temp.close();
            return result;
        } finally {
            closeWithWarning(self);
            closeWithWarning(br);
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
     * @since 1.5.5
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
     * @since 1.5.6
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
     * @since 1.5.5
     */
    public static Object splitEachLine(String self, String sep, Closure closure) throws IOException {
        final List<String> list = readLines(self);
        Object result = null;
        for (String line : list) {
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
     * @since 1.0
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
     * @since 1.0
     */
    public static String readLine(InputStream stream) throws IOException {
        throw new DeprecationException(
                "readLine() on InputStream is no longer supported. " +
                        "Either use a Reader or encapsulate the InputStream" +
                        " with a BufferedReader and an InputStreamReader."
        );
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
    public static String denormalize(final String self) {
        // Don't do this in static initializer because we may never be needed.
        // TODO: Put this lineSeparator property somewhere everyone can use it.
        if (lineSeparator == null) {
            final StringWriter sw = new StringWriter(2);
            try {
                // We use BufferedWriter rather than System.getProperty because
                // it has the security manager rigamarole to deal with the possible exception.
                final BufferedWriter bw = new BufferedWriter(sw);
                bw.newLine();
                bw.flush();
                lineSeparator = sw.toString();
            } catch (IOException ioe) {
                // This shouldn't happen, but this is the same default used by
                // BufferedWriter on a security exception.
                lineSeparator = "\n";
            }
        }
        
        final int len = self.length();
        
        if (len < 1) {
            return self;
        }
        
        final StringBuilder sb = new StringBuilder((110 * len) / 100);

        int i = 0;
        
        while (i < len) {
            final char ch = self.charAt(i++);

            switch (ch) {
                case '\r':
                    sb.append(lineSeparator);
                    
                    // Eat the following LF if any.
                    if ((i < len) && (self.charAt(i) == '\n')) {
                        ++i;
                    }
                    
                    break;

                case '\n':
                    sb.append(lineSeparator);
                    break;

                default:
                    sb.append(ch);
                    break;
            }
         }

        return sb.toString();
    }

    /**
     * Return a String with linefeeds and carriage returns normalized to linefeeds.
     *
     * @param self a String object
     * @return the normalized string
     * @since 1.6.0
     */
    public static String normalize(final String self) {
        int nx = self.indexOf('\r');
        
        if (nx < 0) {
            return self;
        }
      
        final int len = self.length();
        final StringBuilder sb = new StringBuilder(len);

        int i = 0;
        
        do {
            sb.append(self, i, nx);
            sb.append('\n');
            
            if ((i = nx + 1) >= len) break;
            
            if (self.charAt(i) == '\n') {
                // skip the LF in CR LF
                if (++i >= len) break;
            }
            
            nx = self.indexOf('\r', i);
        } while (nx > 0);
        
        sb.append(self, i, len);

        return sb.toString();
    }

    /**
     * Return the lines of a String as a List of Strings.
     *
     * @param self a String object
     * @return a list of lines
     * @throws java.io.IOException if an error occurs
     * @since 1.5.5
     */
    public static List<String> readLines(String self) throws IOException {
        return readLines(new StringReader(self));
    }

    /**
     * Reads the file into a list of Strings, with one item for each line.
     *
     * @param file a File
     * @return a List of lines
     * @throws IOException if an IOException occurs.
     * @see #readLines(Reader)
     * @since 1.0
     */
    public static List<String> readLines(File file) throws IOException {
        return readLines(newReader(file));
    }

    /**
     * Reads the file into a list of Strings, with one item for each line.
     *
     * @param file a File
     * @param charset opens the file with a specified charset
     * @return a List of lines
     * @throws IOException if an IOException occurs.
     * @see #readLines(Reader)
     * @since 1.6.8
     */
    public static List<String> readLines(File file, String charset) throws IOException {
        return readLines(newReader(file, charset));
    }

    /**
     * Reads the stream into a list, with one element for each line.
     *
     * @param stream a stream
     * @return a List of lines
     * @throws IOException if an IOException occurs.
     * @see #readLines(Reader)
     * @since 1.0
     */
    public static List<String> readLines(InputStream stream) throws IOException {
        return readLines(newReader(stream));
    }

    /**
     * Reads the stream into a list, with one element for each line.
     *
     * @param stream a stream
     * @param charset opens the stream with a specified charset
     * @return a List of lines
     * @throws IOException if an IOException occurs.
     * @see #readLines(Reader)
     * @since 1.6.8
     */
    public static List<String> readLines(InputStream stream, String charset) throws IOException {
        return readLines(newReader(stream, charset));
    }

    /**
     * Reads the URL contents into a list, with one element for each line.
     *
     * @param self a URL
     * @return a List of lines
     * @throws IOException if an IOException occurs.
     * @see #readLines(Reader)
     * @since 1.6.8
     */
    public static List<String> readLines(URL self) throws IOException {
        return readLines(newReader(self));
    }

    /**
     * Reads the URL contents into a list, with one element for each line.
     *
     * @param self a URL
     * @param charset opens the URL with a specified charset
     * @return a List of lines
     * @throws IOException if an IOException occurs.
     * @see #readLines(Reader)
     * @since 1.6.8
     */
    public static List<String> readLines(URL self, String charset) throws IOException {
        return readLines(newReader(self, charset));
    }

    /**
     * Reads the reader into a list of Strings, with one entry for each line.
     * The reader is closed before this method returns.
     *
     * @param reader a Reader
     * @return a List of lines
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static List<String> readLines(Reader reader) throws IOException {
        IteratorClosureAdapter closure = new IteratorClosureAdapter(reader);
        eachLine(reader, closure);
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
     */
    public static String getText(URL url, String charset) throws IOException {
        BufferedReader reader = newReader(url, charset);
        return getText(reader);
    }

    /**
     * Read the content of this InputStream and return it as a String.
     * The stream is closed before this method returns.
     *
     * @param is an input stream
     * @return the text from that URL
     * @throws IOException if an IOException occurs.
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
     */
    public static String getText(BufferedReader reader) throws IOException {
        StringBuilder answer = new StringBuilder();
        // reading the content of the file within a char buffer
        // allow to keep the correct line endings
        char[] charBuffer = new char[8192];
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
            closeWithWarning(reader);
        }
        return answer.toString();
    }

    /**
     * Write the text and append a newline (using the platform's line-ending).
     *
     * @param writer a BufferedWriter
     * @param line   the line to write
     * @throws IOException if an IOException occurs.
     * @since 1.0
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
     * @since 1.0
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
            closeWithWarning(writer);
        }
    }

    /**
     * Synonym for write(text) allowing file.text = 'foo'.
     *
     * @param file a File
     * @param text the text to write to the File
     * @throws IOException if an IOException occurs.
     * @see #write(File, String)
     * @since 1.5.1
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
     * @since 1.0
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
     * @since 1.5.0
     */
    public static File leftShift(File file, byte[] bytes) throws IOException {
        append(file, bytes);
        return file;
    }

    /**
     * Append binary data to the file.  See {@link #append(File, InputStream)} 
     * @param file a File
     * @param data an InputStream of data to write to the file
     * @return the file
     * @throws IOException if an IOException occurs.
     * @since 1.5.0
     */
    public static File leftShift(File file, InputStream data) throws IOException {
    	append(file, data);
    	return file;
    }

    /**
     * Write the text to the File, using the specified encoding.
     *
     * @param file    a File
     * @param text    the text to write to the File
     * @param charset the charset used
     * @throws IOException if an IOException occurs.
     * @since 1.0
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
            closeWithWarning(writer);
        }
    }

    /**
     * Append the text at the end of the File.
     *
     * @param file a File
     * @param text the text to append at the end of the File
     * @throws IOException if an IOException occurs.
     * @since 1.0
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
            closeWithWarning(writer);
        }
    }

    /**
     * Append bytes to the end of a File.
     *
     * @param file a File
     * @param bytes the byte array to append to the end of the File
     * @throws IOException if an IOException occurs.
     * @since 1.5.1
     */
    public static void append(File file, byte[] bytes) throws IOException {
        BufferedOutputStream stream = null;
        try {
            stream = new BufferedOutputStream( new FileOutputStream(file,true) );
            stream.write(bytes, 0, bytes.length);
            stream.flush();

            OutputStream temp = stream;
            stream = null;
            temp.close();
        } finally {
            closeWithWarning(stream);
        }
    }

    /**
     * Append binary data to the file.  It <strong>will not</strong> be 
     * interpreted as text.
     * @param self a File
     * @param stream stream to read data from.  
     * @throws IOException if an IOException occurs.
     * @since 1.5.0
     */
    public static void append(File self, InputStream stream ) throws IOException {
    	OutputStream out = new FileOutputStream( self, true );
    	try {
    		leftShift( out, stream );
    	}
    	finally {
    		closeWithWarning( out ); 
    	}
    }

    /**
     * Append the text at the end of the File, using a specified encoding.
     *
     * @param file    a File
     * @param text    the text to append at the end of the File
     * @param charset the charset used
     * @throws IOException if an IOException occurs.
     * @since 1.0
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
            closeWithWarning(writer);
        }
    }

    /**
     * This method is used to throw useful exceptions when the eachFile* and eachDir closure methods
     * are used incorrectly.
     *
     * @param dir The directory to check
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     * @since 1.0
     */
    private static void checkDir(File dir) throws FileNotFoundException, IllegalArgumentException {
        if (!dir.exists())
            throw new FileNotFoundException(dir.getAbsolutePath());
        if (!dir.isDirectory())
            throw new IllegalArgumentException("The provided File object is not a directory: " + dir.getAbsolutePath());
    }

    /**
     * Invokes the closure for each 'child' file in this 'parent' folder/directory.
     * Both regular files and subfolders/subdirectories can be processed depending
     * on the fileType enum value.
     *
     * @param self    a file object
     * @param fileType if normal files or directories or both should be processed
     * @param closure the closure to invoke
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     * @since 1.7.1
     */
    public static void eachFile(final File self, final FileType fileType, final Closure closure)
            throws FileNotFoundException, IllegalArgumentException {
        checkDir(self);
        final File[] files = self.listFiles();
        // null check because of http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4803836
        if (files == null) return;
        for (File file : files) {
            if ((fileType != FileType.FILES && file.isDirectory()) ||
                    (fileType != FileType.DIRECTORIES && file.isFile())){
                closure.call(file);
            }
        }
    }

    /**
     * Invokes the closure for each 'child' file in this 'parent' folder/directory.
     * Both regular files and subfolders/subdirectories are processed.
     *
     * @param self    a File (that happens to be a folder/directory)
     * @param closure a closure (first parameter is the 'child' file)
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     * @see File#listFiles()
     * @see #eachFile(File, FileType, Closure)
     * @since 1.5.0
     */
    public static void eachFile(final File self, final Closure closure) throws FileNotFoundException, IllegalArgumentException {
        eachFile(self, FileType.ANY, closure);
    }

    /**
     * Invokes the closure for each subdirectory in this directory,
     * ignoring regular files.
     *
     * @param self    a File (that happens to be a folder/directory)
     * @param closure a closure (first parameter is the subdirectory file)
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     * @see File#listFiles()
     * @see #eachFile(File, FileType, Closure)
     * @since 1.0
     */
    public static void eachDir(File self, Closure closure) throws FileNotFoundException, IllegalArgumentException {
        eachFile(self, FileType.DIRECTORIES, closure);
    }

    /**
     * Invokes the closure for each descendant file in this directory.
     * Sub-directories are recursively searched in a depth-first fashion.
     * Both regular files and subdirectories may be passed to the closure
     * depending on the value of fileType.
     *
     * @param self     a file object
     * @param fileType if normal files or directories or both should be processed
     * @param closure  the closure to invoke on each file
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     * @since 1.7.1
     */
    public static void eachFileRecurse(final File self, final FileType fileType, final Closure closure)
            throws FileNotFoundException, IllegalArgumentException {
        checkDir(self);
        final File[] files = self.listFiles();
        // null check because of http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4803836
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                if (fileType != FileType.FILES) closure.call(file);
                eachFileRecurse(file, fileType, closure);
            } else if (fileType != FileType.DIRECTORIES) {
                closure.call(file);
            }
        }
    }

    /**
     * Invokes <code>closure</code> for each descendant file in this directory tree.
     * Sub-directories are recursively traversed as found.
     * The traversal can be adapted by providing various options in the <code>options</code> Map according
     * to the following keys:<dl>
     * <dt>type</dt><dd>A {@link FileType} enum to determine if normal files or directories or both are processed</dd>
     * <dt>preDir</dt><dd>A {@link Closure} run before each directory is processed and optionally returning a {@link FileVisitResult} value
     *     which can be used to control subsequent processing.</dd>
     * <dt>preRoot</dt><dd>A boolean indicating that the 'preDir' closure should be applied at the root level</dd>
     * <dt>postDir</dt><dd>A {@link Closure} run after each directory is processed and optionally returning a {@link FileVisitResult} value
     *     which can be used to control subsequent processing.</dd>
     * <dt>postRoot</dt><dd>A boolean indicating that the 'postDir' closure should be applied at the root level</dd>
     * <dt>visitRoot</dt><dd>A boolean indicating that the given closure should be applied for the root dir
     *     (not applicable if the 'type' is set to {@link FileType#FILES})</dd>
     * <dt>maxDepth</dt><dd>The maximum number of directory levels when recursing
     *     (default is -1 which means infinite, set to 0 for no recursion)</dd>
     * <dt>filter</dt><dd>A filter to perform on traversed files/directories (using the {@link #isCase(Object,Object)} method). If set,
     *     only files/dirs which match are candidates for visiting.</dd>
     * <dt>nameFilter</dt><dd>A filter to perform on the name of traversed files/directories (using the {@link #isCase(Object,Object)} method). If set,
     *     only files/dirs which match are candidates for visiting. (Must not be set if 'filter' is set)</dd>
     * <dt>excludeFilter</dt><dd>A filter to perform on traversed files/directories (using the {@link #isCase(Object,Object)} method).
     *     If set, any candidates which match won't be visited.</dd>
     * <dt>excludeNameFilter</dt><dd>A filter to perform on the names of traversed files/directories (using the {@link #isCase(Object,Object)} method).
     *     If set, any candidates which match won't be visited. (Must not be set if 'excludeFilter' is set)</dd>
     * <dt>sort</dt><dd>A {@link Closure} which if set causes the files and subdirectories for each directory to be processed in sorted order.
     *     Note that even when processing only files, the order of visited subdirectories will be affected by this parameter.</dd>
     * </dl>
     * This example prints out file counts and size aggregates for groovy source files within a directory tree:
     * <pre>
     * def totalSize = 0
     * def count = 0
     * def sortByTypeThenName = { a, b ->
     *     a.isFile() != b.isFile() ? a.isFile() <=> b.isFile() : a.name <=> b.name
     * }
     * rootDir.traverse(
     *         type         : FILES,
     *         nameFilter   : ~/.*\.groovy/,
     *         preDir       : { if (it.name == '.svn') return SKIP_SUBTREE },
     *         postDir      : { println "Found $count files in $it.name totalling $totalSize bytes"
     *                         totalSize = 0; count = 0 },
     *         postRoot     : true
     *         sort         : sortByTypeThenName
     * ) {it -> totalSize += it.size(); count++ }
     * </pre>
     *
     * @param self    a File
     * @param options a Map of options to alter the traversal behavior
     * @param closure the Closure to invoke on each file/directory and optionally returning a {@link FileVisitResult} value
     *     which can be used to control subsequent processing
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory or illegal filter combinations are supplied
     * @see #sort(Collection, Closure)
     * @see groovy.io.FileVisitResult
     * @see groovy.io.FileType
     * @since 1.7.1
     */
    public static void traverse(final File self, final Map<String, Object> options, final Closure closure)
            throws FileNotFoundException, IllegalArgumentException {
        Number maxDepthNumber = (Number) asType(options.remove("maxDepth"), Number.class);
        int maxDepth = maxDepthNumber == null ? -1 : maxDepthNumber.intValue();
        Boolean visitRoot = (Boolean) asType(get(options, "visitRoot", false), Boolean.class);
        Boolean preRoot = (Boolean) asType(get(options, "preRoot", false), Boolean.class);
        Boolean postRoot = (Boolean) asType(get(options, "postRoot", false), Boolean.class);
        final Closure pre = (Closure) options.get("preDir");
        final Closure post = (Closure) options.get("postDir");
        final FileType type = (FileType) options.get("type");
        final Object filter = options.get("filter");
        final Object nameFilter = options.get("nameFilter");
        final Object excludeFilter = options.get("excludeFilter");
        final Object excludeNameFilter = options.get("excludeNameFilter");
        Object preResult = null;
        if (preRoot && pre != null) {
            preResult = pre.call(self);
        }
        if (preResult == FileVisitResult.TERMINATE ||
                preResult == FileVisitResult.SKIP_SUBTREE) return;

        FileVisitResult terminated = traverse(self, options, closure, maxDepth);

        if (type != FileType.FILES && visitRoot) {
            if (closure != null && notFiltered(self, filter, nameFilter, excludeFilter, excludeNameFilter)) {
                Object closureResult = closure.call(self);
                if (closureResult == FileVisitResult.TERMINATE) return;
            }
        }

        if (postRoot && post != null && terminated != FileVisitResult.TERMINATE) post.call(self);
    }

    private static boolean notFiltered(File file, Object filter, Object nameFilter, Object excludeFilter, Object excludeNameFilter) {
        if (filter == null && nameFilter == null && excludeFilter == null && excludeNameFilter == null) return true;
        if (filter != null && nameFilter != null) throw new IllegalArgumentException("Can't set both 'filter' and 'nameFilter'");
        if (excludeFilter != null && excludeNameFilter != null) throw new IllegalArgumentException("Can't set both 'excludeFilter' and 'excludeNameFilter'");
        Object filterToUse = null;
        Object filterParam = null;
        if (filter != null) {
            filterToUse = filter;
            filterParam = file;
        } else if (nameFilter != null) {
            filterToUse = nameFilter;
            filterParam = file.getName();
        }
        Object excludeFilterToUse = null;
        Object excludeParam = null;
        if (excludeFilter != null) {
            excludeFilterToUse = excludeFilter;
            excludeParam = file;
        } else if (excludeNameFilter != null) {
            excludeFilterToUse = excludeNameFilter;
            excludeParam = file.getName();
        }
        final MetaClass filterMC = filterToUse == null ? null : InvokerHelper.getMetaClass(filterToUse);
        final MetaClass excludeMC = excludeFilterToUse == null ? null : InvokerHelper.getMetaClass(excludeFilterToUse);
        boolean included = filterToUse == null || (filterToUse != null && DefaultTypeTransformation.castToBoolean(filterMC.invokeMethod(filterToUse, "isCase", filterParam)));
        boolean excluded = excludeFilterToUse != null && DefaultTypeTransformation.castToBoolean(excludeMC.invokeMethod(excludeFilterToUse, "isCase", excludeParam));
        return included && !excluded;
    }

    /**
     * Invokes the closure for each descendant file in this directory tree.
     * Sub-directories are recursively traversed in a depth-first fashion.
     * Convenience method for {@link #traverse(File, Map, Closure)} when
     * no options to alter the traversal behavior are required.
     *
     * @param self    a File
     * @param closure the Closure to invoke on each file/directory and optionally returning a {@link FileVisitResult} value
     *     which can be used to control subsequent processing
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     * @see #traverse(File, Map, Closure)
     * @since 1.7.1
     */
    public static void traverse(final File self, final Closure closure)
            throws FileNotFoundException, IllegalArgumentException {
        traverse(self, new HashMap<String, Object>(), closure);
    }

    /**
     * Invokes the closure specified with key 'visit' in the options Map
     * for each descendant file in this directory tree. Convenience method
     * for {@link #traverse(File, Map, Closure)} allowing the 'visit' closure
     * to be included in the options Map rather than as a parameter.
     *
     * @param self    a File
     * @param options a Map of options to alter the traversal behavior
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory or illegal filter combinations are supplied
     * @see #traverse(File, Map, Closure)
     * @since 1.7.1
     */
    public static void traverse(final File self, final Map<String, Object> options)
            throws FileNotFoundException, IllegalArgumentException {
        final Closure visit = (Closure) options.remove("visit");
        traverse(self, options, visit);
    }

    private static FileVisitResult traverse(final File self, final Map<String, Object> options, final Closure closure, final int maxDepth)
            throws FileNotFoundException, IllegalArgumentException {
        checkDir(self);
        final Closure pre = (Closure) options.get("preDir");
        final Closure post = (Closure) options.get("postDir");
        final FileType type = (FileType) options.get("type");
        final Object filter = options.get("filter");
        final Object nameFilter = options.get("nameFilter");
        final Object excludeFilter = options.get("excludeFilter");
        final Object excludeNameFilter = options.get("excludeNameFilter");
        final Closure sort = (Closure) options.get("sort");

        final File[] origFiles = self.listFiles();
        // null check because of http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4803836
        if (origFiles != null) {
            List<File> files = Arrays.asList(origFiles);
            if (sort != null) files = sort(files, sort);
            for (File file : files) {
                if (file.isDirectory()) {
                    if (type != FileType.FILES) {
                        if (closure != null && notFiltered(file, filter, nameFilter, excludeFilter, excludeNameFilter)) {
                            Object closureResult = closure.call(file);
                            if (closureResult == FileVisitResult.SKIP_SIBLINGS) break;
                            if (closureResult == FileVisitResult.TERMINATE) return FileVisitResult.TERMINATE;
                        }
                    }
                    if (maxDepth != 0) {
                        Object preResult = null;
                        if (pre != null) {
                            preResult = pre.call(file);
                        }
                        if (preResult == FileVisitResult.SKIP_SIBLINGS) break;
                        if (preResult == FileVisitResult.TERMINATE) return FileVisitResult.TERMINATE;
                        if (preResult != FileVisitResult.SKIP_SUBTREE) {
                            FileVisitResult terminated = traverse(file, options, closure, maxDepth - 1);
                            if (terminated == FileVisitResult.TERMINATE) return terminated;
                        }
                        Object postResult = null;
                        if (post != null) {
                            postResult = post.call(file);
                        }
                        if (postResult == FileVisitResult.SKIP_SIBLINGS) break;
                        if (postResult == FileVisitResult.TERMINATE) return FileVisitResult.TERMINATE;
                    }
                } else if (type != FileType.DIRECTORIES) {
                    if (closure != null && notFiltered(file, filter, nameFilter, excludeFilter, excludeNameFilter)) {
                        Object closureResult = closure.call(file);
                        if (closureResult == FileVisitResult.SKIP_SIBLINGS) break;
                        if (closureResult == FileVisitResult.TERMINATE) return FileVisitResult.TERMINATE;
                    }
                }
            }
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Invokes the closure for each descendant file in this directory.
     * Sub-directories are recursively searched in a depth-first fashion.
     * Both regular files and subdirectories are passed to the closure.
     *
     * @param self    a File
     * @param closure a closure
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     * @see #eachFileRecurse(File, FileType, Closure)
     * @since 1.0
     */
    public static void eachFileRecurse(File self, Closure closure) throws FileNotFoundException, IllegalArgumentException {
        eachFileRecurse(self, FileType.ANY, closure);
    }

    /**
     * Invokes the closure for each descendant directory of this directory.
     * Sub-directories are recursively searched in a depth-first fashion.
     * Only subdirectories are passed to the closure; regular files are ignored.
     *
     * @param self    a directory
     * @param closure a closure
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     * @see #eachFileRecurse(File, FileType, Closure)
     * @since 1.5.0
     */
    public static void eachDirRecurse(final File self, final Closure closure) throws FileNotFoundException, IllegalArgumentException {
        eachFileRecurse(self, FileType.DIRECTORIES, closure);
    }

    /**
     * Invokes the closure for each file whose name (file.name) matches the given nameFilter in the given directory
     * - calling the {@link #isCase(Object,Object)} method to determine if a match occurs.  This method can be used
     * with different kinds of filters like regular expressions, classes, ranges etc.
     * Both regular files and subdirectories may be candidates for matching depending
     * on the value of fileType.
     *
     * @param self       a file
     * @param fileType   whether normal files or directories or both should be processed
     * @param nameFilter the filter to perform on the name of the file/directory (using the {@link #isCase(Object,Object)} method)
     * @param closure    the closure to invoke
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     * @since 1.7.1
     */
    public static void eachFileMatch(final File self, final FileType fileType, final Object nameFilter, final Closure closure)
            throws FileNotFoundException, IllegalArgumentException {
        checkDir(self);
        final File[] files = self.listFiles();
        // null check because of http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4803836
        if (files == null) return;
        final MetaClass metaClass = InvokerHelper.getMetaClass(nameFilter);
        for (final File currentFile : files) {
            if ((fileType != FileType.FILES && currentFile.isDirectory()) ||
                    (fileType != FileType.DIRECTORIES && currentFile.isFile())) {
                if (DefaultTypeTransformation.castToBoolean(metaClass.invokeMethod(nameFilter, "isCase", currentFile.getName())))
                    closure.call(currentFile);
            }
        }
    }

    /**
     * Invokes the closure for each file whose name (file.name) matches the given nameFilter in the given directory
     * - calling the {@link #isCase(Object,Object)} method to determine if a match occurs.  This method can be used
     * with different kinds of filters like regular expressions, classes, ranges etc.
     * Both regular files and subdirectories are matched.
     *
     * @param self       a file
     * @param nameFilter the nameFilter to perform on the name of the file (using the {@link #isCase(Object,Object)} method)
     * @param closure    the closure to invoke
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     * @see #eachFileMatch(File, FileType, Object, Closure)
     * @since 1.5.0
     */
    public static void eachFileMatch(final File self, final Object nameFilter, final Closure closure)
            throws FileNotFoundException, IllegalArgumentException {
        eachFileMatch(self, FileType.ANY, nameFilter, closure);
    }

    /**
     * Invokes the closure for each subdirectory whose name (dir.name) matches the given nameFilter in the given directory
     * - calling the {@link #isCase(Object,Object)} method to determine if a match occurs.  This method can be used
     * with different kinds of filters like regular expressions, classes, ranges etc.
     * Only subdirectories are matched; regular files are ignored.
     *
     * @param self       a file
     * @param nameFilter the nameFilter to perform on the name of the directory (using the {@link #isCase(Object,Object)} method)
     * @param closure    the closure to invoke
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     * @see #eachFileMatch(File, FileType, Object, Closure)
     * @since 1.5.0
     */
    public static void eachDirMatch(final File self, final Object nameFilter, final Closure closure) throws FileNotFoundException, IllegalArgumentException {
        eachFileMatch(self, FileType.DIRECTORIES, nameFilter, closure);
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
     * @param self a File
     * @return true if the file doesn't exist or deletion was successful
     * @since 1.6.0
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
        for (File file : files) {
            if (file.isDirectory()) {
                if (!deleteDir(file))
                    result = false;
            } else {
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
     * @since 1.5.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.6.0
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
     * @since 1.5.2
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
     * @since 1.6.0
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
     * @since 1.0
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
     * @since 1.5.0
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
     * @since 1.5.2
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
     * @since 1.5.2
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
     * @since 1.5.2
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
     * @since 1.5.2
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
     * @since 1.5.2
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.5.2
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
     * @since 1.5.2
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
     * @since 1.5.2
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
     * @since 1.5.2
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
     * @since 1.0
     */
    public static PrintWriter newPrintWriter(File file) throws IOException {
        return new GroovyPrintWriter(newWriter(file));
    }

    /**
     * Create a new PrintWriter for this file, using specified
     * charset.
     *
     * @param file    a File
     * @param charset the charset
     * @return a PrintWriter
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static PrintWriter newPrintWriter(File file, String charset) throws IOException {
        return new GroovyPrintWriter(newWriter(file, charset));
    }

    /**
     * Create a new PrintWriter for this file, using specified
     * charset.
     *
     * @param writer   a writer
     * @return a PrintWriter
     * @since 1.6.0
     */
    public static PrintWriter newPrintWriter(Writer writer) {
        return new GroovyPrintWriter(writer);
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
     * @since 1.5.2
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
     * @since 1.5.2
     */
    public static Object withPrintWriter(File file, String charset, Closure closure) throws IOException {
        return withWriter(newPrintWriter(file, charset), closure);
    }

    /**
     * Create a new PrintWriter with a specified charset for
     * this file.  The writer is passed to the closure, and will be closed
     * before this method returns.
     *
     * @param writer   a writer
     * @param closure the closure to invoke with the PrintWriter
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @since 1.6.0
     */
    public static Object withPrintWriter(Writer writer, Closure closure) throws IOException {
        return withWriter(newPrintWriter(writer), closure);
    }

    /**
     * Allows this writer to be used within the closure, ensuring that it
     * is flushed and closed before this method returns.
     *
     * @param writer  the writer which is used and then closed
     * @param closure the closure that the writer is passed into
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @since 1.5.2
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
            closeWithWarning(writer);
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
     * @since 1.5.2
     */
    public static Object withReader(Reader reader, Closure closure) throws IOException {
        try {
            Object result = closure.call(reader);

            Reader temp = reader;
            reader = null;
            temp.close();

            return result;
        } finally {
            closeWithWarning(reader);
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
     * @since 1.5.2
     */
    public static Object withStream(InputStream stream, Closure closure) throws IOException {
        try {
            Object result = closure.call(stream);

            InputStream temp = stream;
            stream = null;
            temp.close();

            return result;
        } finally {
            closeWithWarning(stream);
        }
    }

    /**
     * Helper method to create a new BufferedReader for a URL and then
     * passes it to the closure.  The reader is closed after the closure returns.
     *
     * @param url     a URL
     * @param closure the closure to invoke with the reader
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @since 1.5.2
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
     * @since 1.5.6
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
     * @since 1.5.2
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
     * @since 1.5.6
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
     * @since 1.5.2
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
     * @since 1.5.2
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
     * @since 1.5.2
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
            closeWithWarning(os);
        }
    }

    /**
     * Creates a buffered input stream for this file.
     *
     * @param file a File
     * @return a BufferedInputStream of the file
     * @throws FileNotFoundException if the file is not found.
     * @since 1.0
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
     * @since 1.5.2
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
     * @since 1.5.5
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
     * @since 1.5.5
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
     * @since 1.5.0
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
     * @since 1.0
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
     * @since 1.5.5
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
     * @since 1.5.5
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
     * @since 1.0
     */
    public static void eachByte(InputStream is, Closure closure) throws IOException {
        try {
            while (true) {
                int b = is.read();
                if (b == -1) {
                    break;
                } else {
                    closure.call((byte) b);
                }
            }

            InputStream temp = is;
            is = null;
            temp.close();
        } finally {
            closeWithWarning(is);
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
     * @since 1.0
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
     * @since 1.5.0
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
            closeWithWarning(self);
            closeWithWarning(writer);
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
     * @since 1.0
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
            closeWithWarning(br);
            closeWithWarning(reader);
            closeWithWarning(bw);
            closeWithWarning(writer);
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
     * @since 1.0
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
            closeWithWarning(br);
            closeWithWarning(reader);
            closeWithWarning(bw);
            closeWithWarning(writer);
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
     * @since 1.0
     */
    public static Writable filterLine(File self, Closure closure) throws IOException {
        return filterLine(newReader(self), closure);
    }

    /**
     * Filters the lines of a File and creates a Writeable in return to
     * stream the filtered lines.
     *
     * @param self    a File
     * @param charset opens the file with a specified charset
     * @param closure a closure which returns a boolean indicating to filter
     *                the line or not
     * @return a Writable closure
     * @throws IOException if an IOException occurs
     * @see #filterLine(Reader,Closure)
     * @since 1.6.8
     */
    public static Writable filterLine(File self, String charset, Closure closure) throws IOException {
        return filterLine(newReader(self, charset), closure);
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
     * @since 1.0
     */
    public static void filterLine(File self, Writer writer, Closure closure) throws IOException {
        filterLine(newReader(self), writer, closure);
    }

    /**
     * Filter the lines from this File, and write them to the given writer based
     * on the given closure predicate.
     *
     * @param self    a File
     * @param writer  a writer destination to write filtered lines to
     * @param charset opens the file with a specified charset
     * @param closure a closure which takes each line as a parameter and returns
     *                <code>true</code> if the line should be written to this writer.
     * @throws IOException if an IO error occurs
     * @see #filterLine(Reader,Writer,Closure)
     * @since 1.6.8
     */
    public static void filterLine(File self, Writer writer, String charset, Closure closure) throws IOException {
        filterLine(newReader(self, charset), writer, closure);
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
     * @since 1.0
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
     * Filter lines from an input stream using a closure predicate.  The closure
     * will be passed each line as a String, and it should return
     * <code>true</code> if the line should be passed to the writer.
     *
     * @param self      an input stream
     * @param predicate a closure which returns boolean and takes a line
     * @return a writable which writes out the filtered lines
     * @see #filterLine(Reader, Closure)
     * @since 1.0
     */
    public static Writable filterLine(InputStream self, Closure predicate) {
        return filterLine(newReader(self), predicate);
    }

    /**
     * Filter lines from an input stream using a closure predicate.  The closure
     * will be passed each line as a String, and it should return
     * <code>true</code> if the line should be passed to the writer.
     *
     * @param self      an input stream
     * @param charset   opens the stream with a specified charset
     * @param predicate a closure which returns boolean and takes a line
     * @return a writable which writes out the filtered lines
     * @throws UnsupportedEncodingException if the encoding specified is not supported
     * @see #filterLine(Reader, Closure)
     * @since 1.6.8
     */
    public static Writable filterLine(InputStream self, String charset, Closure predicate)
            throws UnsupportedEncodingException {
        return filterLine(newReader(self, charset), predicate);
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
     * @since 1.0
     */
    public static void filterLine(InputStream self, Writer writer, Closure predicate)
            throws IOException {
        filterLine(newReader(self), writer, predicate);
    }

    /**
     * Uses a closure to filter lines from this InputStream and pass them to
     * the given writer. The closure will be passed each line as a String, and
     * it should return <code>true</code> if the line should be passed to the
     * writer.
     *
     * @param self      the InputStream
     * @param writer    a writer to write output to
     * @param charset   opens the stream with a specified charset
     * @param predicate a closure which returns true if a line should be accepted
     * @throws IOException if an IOException occurs.
     * @see #filterLine(Reader,Writer,Closure)
     * @since 1.6.8
     */
    public static void filterLine(InputStream self, Writer writer, String charset, Closure predicate)
            throws IOException {
        filterLine(newReader(self, charset), writer, predicate);
    }

    /**
     * Filter lines from a URL using a closure predicate.  The closure
     * will be passed each line as a String, and it should return
     * <code>true</code> if the line should be passed to the writer.
     *
     * @param self      a URL
     * @param predicate a closure which returns boolean and takes a line
     * @return a writable which writes out the filtered lines
     * @throws IOException if an IO exception occurs
     * @see #filterLine(Reader, Closure)
     * @since 1.6.8
     */
    public static Writable filterLine(URL self, Closure predicate)
            throws IOException {
        return filterLine(newReader(self), predicate);
    }

    /**
     * Filter lines from a URL using a closure predicate.  The closure
     * will be passed each line as a String, and it should return
     * <code>true</code> if the line should be passed to the writer.
     *
     * @param self      the URL
     * @param charset   opens the URL with a specified charset
     * @param predicate a closure which returns boolean and takes a line
     * @return a writable which writes out the filtered lines
     * @throws IOException if an IO exception occurs
     * @see #filterLine(Reader, Closure)
     * @since 1.6.8
     */
    public static Writable filterLine(URL self, String charset, Closure predicate)
            throws IOException {
        return filterLine(newReader(self, charset), predicate);
    }

    /**
     * Uses a closure to filter lines from this URL and pass them to
     * the given writer. The closure will be passed each line as a String, and
     * it should return <code>true</code> if the line should be passed to the
     * writer.
     *
     * @param self      the URL
     * @param writer    a writer to write output to
     * @param predicate a closure which returns true if a line should be accepted
     * @throws IOException if an IOException occurs.
     * @see #filterLine(Reader,Writer,Closure)
     * @since 1.6.8
     */
    public static void filterLine(URL self, Writer writer, Closure predicate)
            throws IOException {
        filterLine(newReader(self), writer, predicate);
    }

    /**
     * Uses a closure to filter lines from this URL and pass them to
     * the given writer. The closure will be passed each line as a String, and
     * it should return <code>true</code> if the line should be passed to the
     * writer.
     *
     * @param self      the URL
     * @param writer    a writer to write output to
     * @param charset   opens the URL with a specified charset
     * @param predicate a closure which returns true if a line should be accepted
     * @throws IOException if an IOException occurs.
     * @see #filterLine(Reader,Writer,Closure)
     * @since 1.6.8
     */
    public static void filterLine(URL self, Writer writer, String charset, Closure predicate)
            throws IOException {
        filterLine(newReader(self, charset), writer, predicate);
    }

    /**
     * Reads the content of the file into a byte array.
     *
     * @param file a File
     * @return a byte array with the contents of the file.
     * @throws IOException if an IOException occurs.
     * @since 1.0
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
            closeWithWarning(dis);
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
     * @since 1.5.2
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
            closeWithWarning(input);
            closeWithWarning(output);
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
     * @since 1.5.0
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
            closeWithWarning(ois);
            closeWithWarning(input);
            closeWithWarning(oos);
            closeWithWarning(output);
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
     */
    public static Socket accept(ServerSocket serverSocket, final Closure closure) throws IOException {
        final Socket socket = serverSocket.accept();
        new Thread(new Runnable() {
            public void run() {
                try {
                    closure.call(socket);
                } finally {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            LOG.warning("Caught exception closing socket: " + e);
                        }
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
     */
    public static File asWritable(File file, String encoding) {
        return new WritableFile(file, encoding);
    }

    /**
     * Converts the given String into a List of strings of one character.
     *
     * @param self a String
     * @return a List of characters (a 1-character String)
     * @since 1.0
     */
    public static List<String> toList(String self) {
        int size = self.length();
        List<String> answer = new ArrayList<String>(size);
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
     * @since 1.6.0
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
     * @since 1.5.0
     */
    public static Object asType(GString self, Class c) {
        if (c == File.class) {
            return new File(self.toString());
        } else if (Number.class.isAssignableFrom(c)) {
            return asType(self.toString(), c);
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
     * @since 1.0
     */
    public static Object asType(String self, Class c) {
        if (c == List.class) {
            return toList(self);
        } else if (c == BigDecimal.class) {
            return toBigDecimal(self);
        } else if (c == BigInteger.class) {
            return toBigInteger(self);
        } else if (c == Long.class || c == Long.TYPE) {
            return toLong(self);
        } else if (c == Integer.class || c == Integer.TYPE) {
            return toInteger(self);
        } else if (c == Short.class || c == Short.TYPE) {
            return toShort(self);
        } else if (c == Byte.class || c == Byte.TYPE) {
            return Byte.valueOf(self.trim());
        } else if (c == Character.class || c == Character.TYPE) {
            return toCharacter(self);
        } else if (c == Double.class || c == Double.TYPE) {
            return toDouble(self);
        } else if (c == Float.class || c == Float.TYPE) {
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
     */
    public static OutputStream leftShift(Process self, byte[] value) throws IOException {
        return leftShift(self.getOutputStream(), value);
    }

    /**
     * Wait for the process to finish during a certain amount of time, otherwise stops the process.
     *
     * @param self           a Process
     * @param numberOfMillis the number of milliseconds to wait before stopping the process
     * @since 1.0
     */
    public static void waitForOrKill(Process self, long numberOfMillis) {
        ProcessRunner runnable = new ProcessRunner(self);
        Thread thread = new Thread(runnable);
        thread.start();
        runnable.waitForOrKill(numberOfMillis);
    }

    /**
     * Gets the output and error streams from a process and reads them
     * to keep the process from blocking due to a full output buffer.
     * The stream data is thrown away but blocking due to a full output buffer is avoided.
     * Use this method if you don't care about the standard or error output and just
     * want the process to run silently - use carefully however, because since the stream
     * data is thrown away, it might be difficult to track down when something goes wrong.
     * For this, two Threads are started, so this method will return immediately.
     *
     * @param self a Process
     * @since 1.0
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
     * @since 1.5.2
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
     * @since 1.5.2
     */
    public static void consumeProcessOutput(Process self, OutputStream output, OutputStream error) {
        consumeProcessOutputStream(self, output);
        consumeProcessErrorStream(self, error);
    }

    /**
     * Gets the output and error streams from a process and reads them
     * to keep the process from blocking due to a full output buffer.
     * The stream data is thrown away but blocking due to a full output buffer is avoided.
     * Use this method if you don't care about the standard or error output and just
     * want the process to run silently - use carefully however, because since the stream
     * data is thrown away, it might be difficult to track down when something goes wrong.
     * For this, two Threads are started, but join()ed, so we wait.
     *
     * @param self a Process
     * @since 1.6.5
     */
    public static void waitForProcessOutput(Process self) {
        waitForProcessOutput(self, (OutputStream)null, (OutputStream)null);
    }

    /**
     * Gets the output and error streams from a process and reads them
     * to keep the process from blocking due to a full output buffer.
     * The processed stream data is appended to the supplied StringBuffer.
     * For this, two Threads are started, but join()ed, so we wait.
     *
     * @param self a Process
     * @param output a StringBuffer to capture the process stdout
     * @param error a StringBuffer to capture the process stderr
     * @since 1.6.5
     */
    public static void waitForProcessOutput(Process self, StringBuffer output, StringBuffer error) {
        Thread tout = consumeProcessOutputStream(self, output);
        Thread terr = consumeProcessErrorStream(self, error);
        try { tout.join(); } catch (InterruptedException ignore) {}
        try { terr.join(); } catch (InterruptedException ignore) {}
    }

    /**
     * Gets the output and error streams from a process and reads them
     * to keep the process from blocking due to a full output buffer.
     * The processed stream data is appended to the supplied OutputStream.
     * For this, two Threads are started, but join()ed, so we wait.
     *
     * @param self a Process
     * @param output an OutputStream to capture the process stdout
     * @param error an OutputStream to capture the process stderr
     * @since 1.6.5
     */
    public static void waitForProcessOutput(Process self, OutputStream output, OutputStream error) {
        Thread tout = consumeProcessOutputStream(self, output);
        Thread terr = consumeProcessErrorStream(self, error);
        try { tout.join(); } catch (InterruptedException ignore) {}
        try { terr.join(); } catch (InterruptedException ignore) {}
    }

    /**
     * Gets the error stream from a process and reads it
     * to keep the process from blocking due to a full buffer.
     * The processed stream data is appended to the supplied StringBuffer.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param error a StringBuffer to capture the process stderr
     * @return the Thread
     * @since 1.5.2
     */
    public static Thread consumeProcessErrorStream(Process self, StringBuffer error) {
        Thread thread = new Thread(new TextDumper(self.getErrorStream(), error));
        thread.start();
        return thread;
    }

    /**
     * Gets the error stream from a process and reads it
     * to keep the process from blocking due to a full buffer.
     * The processed stream data is appended to the supplied OutputStream.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param err an OutputStream to capture the process stderr
     * @return the Thread
     * @since 1.5.2
     */
    public static Thread consumeProcessErrorStream(Process self, OutputStream err) {
        Thread thread = new Thread(new ByteDumper(self.getErrorStream(), err));
        thread.start();
        return thread;
    }

    /**
     * Gets the error stream from a process and reads it
     * to keep the process from blocking due to a full buffer.
     * The processed stream data is appended to the supplied Writer.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param err a Writer to capture the process stderr
     * @return the Thread
     * @since 1.5.2
     */
    public static Thread consumeProcessErrorStream(Process self, Writer err) {
        Thread thread = new Thread(new TextDumper(self.getErrorStream(), err));
        thread.start();
        return thread;
    }

    /**
     * Gets the output stream from a process and reads it
     * to keep the process from blocking due to a full output buffer.
     * The processed stream data is appended to the supplied StringBuffer.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param output a StringBuffer to capture the process stdout
     * @return the Thread
     * @since 1.5.2
     */
    public static Thread consumeProcessOutputStream(Process self, StringBuffer output) {
        Thread thread = new Thread(new TextDumper(self.getInputStream(), output));
        thread.start();
        return thread;
    }

    /**
     * Gets the output stream from a process and reads it
     * to keep the process from blocking due to a full output buffer.
     * The processed stream data is appended to the supplied OutputStream.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param output an OutputStream to capture the process stdout
     * @return the Thread
     * @since 1.5.2
     */
    public static Thread consumeProcessOutputStream(Process self, OutputStream output) {
        Thread thread = new Thread(new ByteDumper(self.getInputStream(), output));
        thread.start();
        return thread;
    }

    /**
     * Gets the output stream from a process and reads it
     * to keep the process from blocking due to a full output buffer.
     * The processed stream data is appended to the supplied Writer.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param output a Writer to capture the process stdout
     * @return the Thread
     * @since 1.5.2
     */
    public static Thread consumeProcessOutputStream(Process self, Writer output) {
        Thread thread = new Thread(new TextDumper(self.getInputStream(), output));
        thread.start();
        return thread;
    }

    /**
     * Creates a new BufferedWriter as stdin for this process,
     * passes it to the closure, and ensures the stream is flushed
     * and closed after the closure returns.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param closure a closure
     * @since 1.5.2
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
     * @since 1.5.2
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
     * @since 1.5.2
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
                    closeWithWarning(out);
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
     * @since 1.5.1
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
     * @since 1.6.0
     */
    public static String eachMatch(String self, String regex, Closure closure) {
        return eachMatch(self, Pattern.compile(regex), closure);
    }

    /**
     * Process each regex group matched substring of the given pattern. If the closure
     * parameter takes one argument, an array with all match groups is passed to it.
     * If the closure takes as many arguments as there are match groups, then each
     * parameter will be one match group.
     *
     * @param self    the source string
     * @param pattern a regex Pattern
     * @param closure a closure with one parameter or as much parameters as groups
     * @return the source string
     * @since 1.6.1
     */
    public static String eachMatch(String self, Pattern pattern, Closure closure) {
        Matcher m = pattern.matcher(self);
        each(m, closure);
        return self;
    }

    /**
     * Iterates over the elements of an iterable collection of items and returns
     * the index of the first item that matches the condition specified in the closure.
     *
     * @param self    the iteration object over which to iterate
     * @param closure the filter to perform a match on the collection
     * @return an integer that is the index of the first matched object or -1 if no match was found
     * @since 1.0
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
     * @since 1.5.0
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
     * @since 1.5.2
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
     * @since 1.5.2
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
     * @return a list of numbers corresponding to the index values of all matched objects
     * @since 1.5.2
     */
    public static List<Number> findIndexValues(Object self, Closure closure) {
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
     * @return a list of numbers corresponding to the index values of all matched objects
     * @since 1.5.2
     */
    public static List<Number> findIndexValues(Object self, Number startIndex, Closure closure) {
        List<Number> result = new ArrayList<Number>();
        long count = 0;
        long startCount = startIndex.longValue();
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext(); count++) {
            Object value = iter.next();
            if (count < startCount) {
                continue;
            }
            if (DefaultTypeTransformation.castToBoolean(closure.call(value))) {
                result.add(count);
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
     * @since 1.5.0
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
     * @since 1.0
     */
    public static Object asType(Object obj, Class type) {
        if (String.class == type) {
            return InvokerHelper.toString(obj);
        }
        
        try {
          return DefaultTypeTransformation.castToType(obj, type);
        }
        catch (GroovyCastException e) {
            MetaClass mc = InvokerHelper.getMetaClass(obj);
            if (mc instanceof ExpandoMetaClass) {
                ExpandoMetaClass emc = (ExpandoMetaClass) mc;
                Object mixedIn = emc.castToMixedType(obj, type);
                if (mixedIn != null)
                  return mixedIn;
            }
            throw e;
        }
    }

    /**
     * Convenience method to dynamically create a new instance of this
     * class.  Calls the default constructor.
     *
     * @param c a class
     * @return a new instance of this class
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.5.0
     */
    public static MetaClass getMetaClass(Class c) {
        MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        MetaClass mc = metaClassRegistry.getMetaClass(c);
        if (mc instanceof ExpandoMetaClass
                || mc instanceof DelegatingMetaClass && ((DelegatingMetaClass) mc).getAdaptee() instanceof ExpandoMetaClass)
            return mc;
        else {
            return new HandleMetaClass(mc);
        }
    }

    /**
     * Obtains a MetaClass for an object either from the registry or in the case of
     * a GroovyObject from the object itself.
     *
     * @param obj The object in question
     * @return The MetaClass
     * @since 1.5.0
     */
    public static MetaClass getMetaClass(Object obj) {
        MetaClass mc = InvokerHelper.getMetaClass(obj);
        return new HandleMetaClass(mc, obj);
    }

    /**
     * Obtains a MetaClass for an object either from the registry or in the case of
     * a GroovyObject from the object itself.
     *
     * @param obj The object in question
     * @return The MetaClass
     * @since 1.6.0
     */
    public static MetaClass getMetaClass(GroovyObject obj) {
        // we need this method as trick to guarantee correct method selection
        return getMetaClass((Object)obj);
    }

    /**
     * Sets the metaclass for a given class.
     *
     * @param self the class whose metaclass we wish to set
     * @param metaClass the new MetaClass
     * @since 1.6.0
     */
    public static void setMetaClass(Class self, MetaClass metaClass) {
        final MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        if (metaClass == null)
          metaClassRegistry.removeMetaClass(self);
        else {
          if (metaClass instanceof HandleMetaClass) {
            metaClassRegistry.setMetaClass(self, ((HandleMetaClass)metaClass).getAdaptee());
          } else {
            metaClassRegistry.setMetaClass(self, metaClass);
          }
          if (self==NullObject.class) {
              NullObject.getNullObject().setMetaClass(metaClass);
          }
        }
    }

    /**
     * Set the metaclass for an object
     * @param self the object whose metaclass we want to set
     * @param metaClass the new metaclass value
     * @since 1.6.0
     */
    public static void setMetaClass(Object self, MetaClass metaClass) {
        if (metaClass instanceof HandleMetaClass)
            metaClass = ((HandleMetaClass)metaClass).getAdaptee();

        if (self instanceof GroovyObject) {
            ((GroovyObject)self).setMetaClass(metaClass);
        } else if (self instanceof Class) {
            ((MetaClassRegistryImpl)GroovySystem.getMetaClassRegistry()).setMetaClass((Class)self, metaClass);
        } else {
            ((MetaClassRegistryImpl)GroovySystem.getMetaClassRegistry()).setMetaClass(self, metaClass);
        }
    }

    /**
     * Sets/updates the metaclass for a given class to a closure.
     *
     * @param self the class whose metaclass we wish to update
     * @param closure the closure representing the new metaclass
     * @return the new metaclass value
     * @throws GroovyRuntimeException if the metaclass can't be set for this class
     * @since 1.6.0
     */
    public static MetaClass metaClass (Class self, Closure closure){
        MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        MetaClass mc = metaClassRegistry.getMetaClass(self);

        if (mc instanceof ExpandoMetaClass) {
            ((ExpandoMetaClass) mc).define(closure);
            return mc;
        }
        else {
            if (mc instanceof DelegatingMetaClass && ((DelegatingMetaClass) mc).getAdaptee() instanceof ExpandoMetaClass) {
                ((ExpandoMetaClass)((DelegatingMetaClass) mc).getAdaptee()).define(closure);
                return mc;
            }
            else {
                if (mc instanceof DelegatingMetaClass && ((DelegatingMetaClass) mc).getAdaptee().getClass() == MetaClassImpl.class) {
                    ExpandoMetaClass emc =  new ExpandoMetaClass(self, false, true);
                    emc.initialize();
                    emc.define(closure);
                    ((DelegatingMetaClass) mc).setAdaptee(emc);
                    return mc;
                }
                else {
                    if (mc.getClass() == MetaClassImpl.class) {
                        // default case
                        mc = new ExpandoMetaClass(self, false, true);
                        mc.initialize();
                        ((ExpandoMetaClass)mc).define(closure);
                        metaClassRegistry.setMetaClass(self, mc);
                        return mc;
                    }
                    else {
                        throw new GroovyRuntimeException("Can't add methods to custom meta class " + mc);
                    }
                }
            }
        }
    }

    /**
     * Sets/updates the metaclass for a given object to a closure.
     *
     * @param self the object whose metaclass we wish to update
     * @param closure the closure representing the new metaclass
     * @return the new metaclass value
     * @throws GroovyRuntimeException if the metaclass can't be set for this object
     * @since 1.6.0
     */
    public static MetaClass metaClass (Object self, Closure closure){
        MetaClass emc = hasPerInstanceMetaClass(self);
        if (emc == null) {
            final ExpandoMetaClass metaClass = new ExpandoMetaClass(self.getClass(), false, true);
            metaClass.initialize();
            metaClass.define(closure);
            setMetaClass(self, metaClass);
            return metaClass;
        }
        else {
            if (emc instanceof ExpandoMetaClass) {
                ((ExpandoMetaClass)emc).define(closure);
                return emc;
            }
            else {
                if (emc instanceof DelegatingMetaClass && ((DelegatingMetaClass)emc).getAdaptee() instanceof ExpandoMetaClass) {
                    ((ExpandoMetaClass)((DelegatingMetaClass)emc).getAdaptee()).define(closure);
                    return emc;
                }
                else {
                    throw new RuntimeException("Can't add methods to non-ExpandoMetaClass " + emc);
                }
            }
        }
    }

    private static MetaClass hasPerInstanceMetaClass(Object object) {
        if (object instanceof GroovyObject) {
            MetaClass mc = ((GroovyObject)object).getMetaClass();
            if (mc == GroovySystem.getMetaClassRegistry().getMetaClass(object.getClass()) || mc.getClass() == MetaClassImpl.class)
              return null;
            else
              return mc;
        }
        else {
            ClassInfo info = ClassInfo.getClassInfo(object.getClass());
            info.lock();
            try {
                return info.getPerInstanceMetaClass(object);
            }
            finally {
                info.unlock();
            }
        }
    }

    /**
     * A Runnable which waits for a process to complete together with a notification scheme
     * allowing another thread to wait a maximum number of seconds for the process to complete
     * before killing it.
     *
     * @since 1.0
     */
    protected static class ProcessRunner implements Runnable {
        Process process;
        private boolean finished;

        public ProcessRunner(Process process) {
            this.process = process;
        }
        
        private void doProcessWait() {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                // Ignore
            }
        }

        public void run() {
            doProcessWait();
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
                    doProcessWait();
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
     * @param a an array
     * @return an Iterator for the given Array.
     * @see DefaultTypeTransformation#asCollection(Object[])
     * @since 1.6.4
     */
    public static <T> Iterator<T> iterator(T[] a) {
        return DefaultTypeTransformation.asCollection(a).iterator();
    }

    /**
     * Attempts to create an Iterator for the given object by first
     * converting it to a Collection.
     *
     * @param o an object
     * @return an Iterator for the given Object.
     * @see DefaultTypeTransformation#asCollection(Object)
     * @since 1.0
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
     * @since 1.0
     */
    public static <T> Iterator<T> iterator(final Enumeration<T> enumeration) {
        return new Iterator<T>() {
            private T last;

            public boolean hasNext() {
                return enumeration.hasMoreElements();
            }

            public T next() {
                last = enumeration.nextElement();
                return last;
            }

            public void remove() {
                throw new UnsupportedOperationException("Cannot remove() from an Enumeration");
            }
        };
    }

    /**
     * Makes org.w3c.dom.NodeList iterable by returning a read-only Iterator which traverses
     * over each Node.
     *
     * @param nodeList a NodeList
     * @return an Iterator for a NodeList
     * @since 1.0
     * @deprecated moved to {@link org.codehaus.groovy.runtime.XmlGroovyMethods}
     */
    @Deprecated
    public static Iterator<org.w3c.dom.Node> iterator(final org.w3c.dom.NodeList nodeList) {
        return XmlGroovyMethods.iterator(nodeList);
    }

    /**
     * Returns an {@link Iterator} which traverses each match.
     *
     * @param matcher a Matcher object
     * @return an Iterator for a Matcher
     * @see Matcher#group()
     * @since 1.0
     */
    public static Iterator iterator(final Matcher matcher) {
        matcher.reset();
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
     * @since 1.5.0
     */
    public static Iterator<String> iterator(Reader self) {
        final BufferedReader bufferedReader;
        if (self instanceof BufferedReader)
            bufferedReader = (BufferedReader) self;
        else
            bufferedReader = new BufferedReader(self);
        return new Iterator<String>() {
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

            public String next() {
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
     * @since 1.5.0
     */
    public static Iterator<Byte> iterator(InputStream self) {
        return iterator(new DataInputStream(self));
    }

    /**
     * Standard iterator for a data input stream which iterates through the
     * stream content a Byte at a time.
     *
     * @param self a DataInputStream object
     * @return an Iterator for the DataInputStream
     * @since 1.5.0
     */
    public static Iterator<Byte> iterator(final DataInputStream self) {
        return new Iterator<Byte>() {
            Byte nextVal;
            boolean nextMustRead = true;
            boolean hasNext = true;

            public boolean hasNext() {
                if (nextMustRead && hasNext) {
                    try {
                        nextVal = self.readByte();
                        nextMustRead = false;
                    } catch (IOException e) {
                        hasNext = false;
                    }
                }
                return hasNext;
            }

            public Byte next() {
                Byte retval = null;
                if (nextMustRead) {
                    try {
                        retval = self.readByte();
                    } catch (IOException e) {
                        hasNext = false;
                    }
                } else
                    retval = nextVal;
                nextMustRead = true;
                return retval;
            }

            public void remove() {
                throw new UnsupportedOperationException("Cannot remove() from a DataInputStream Iterator");
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
     * @since 1.5.0
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
     * @since 1.5.0
     */
    public static <T> Iterator<T> iterator(Iterator<T> self) {
        return self;
    }

    /**
     * <p>Returns an object satisfying Groovy truth if the implementing MetaClass responds to
     * a method with the given name and arguments types.
     *
     * <p>Note that this method's return value is based on realised methods and does not take into account
     * objects or classes that implement invokeMethod or methodMissing
     *
     * <p>This method is "safe" in that it will always return a value and never throw an exception
     *
     * @param self The object to inspect
     * @param name The name of the method of interest
     * @param argTypes The argument types to match against
     * @return A List of MetaMethods matching the argument types which will be empty if no matching methods exist
     * @see MetaObjectProtocol#respondsTo(Object, String, Object[])
     * @since 1.6.0
     */
    public static List respondsTo(Object self, String name, Object[] argTypes) {
        return InvokerHelper.getMetaClass(self).respondsTo(self, name, argTypes);
    }

    /**
     * <p>Returns an object satisfying Groovy truth if the implementing MetaClass responds to
     * a method with the given name regardless of the arguments.
     *
     * <p>Note that this method's return value is based on realised methods and does not take into account
     * objects or classes that implement invokeMethod or methodMissing
     *
     * <p>This method is "safe" in that it will always return a value and never throw an exception
     *
     * @param self The object to inspect
     * @param name The name of the method of interest
     * @return A List of MetaMethods matching the given name or an empty list if no matching methods exist
     * @see MetaObjectProtocol#respondsTo(Object, String)
     * @since 1.6.1
     */
    public static List respondsTo(Object self, String name) {
        return InvokerHelper.getMetaClass(self).respondsTo(self, name);
    }

    /**
     * <p>Returns true of the implementing MetaClass has a property of the given name
     *
     * <p>Note that this method will only return true for realised properties and does not take into
     * account implementation of getProperty or propertyMissing
     *
     * @param self The object to inspect
     * @param name The name of the property of interest
     * @return The found MetaProperty or null if it doesn't exist
     * @see MetaObjectProtocol#hasProperty(Object, String)
     * @since 1.6.1
     */
    public static MetaProperty hasProperty(Object self, String name) {
        return InvokerHelper.getMetaClass(self).hasProperty(self, name);
    }

    /**
     * Returns a GroovyRowResult given a ResultSet.
     *
     * @param rs a java.sql.ResultSet
     * @return the resulting groovy.sql.GroovyRowResult
     * @throws java.sql.SQLException if a database error occurs
     * @deprecated moved to {@link org.codehaus.groovy.runtime.SqlGroovyMethods#toRowResult(java.sql.ResultSet)}
     * @since 1.6.0
     */
    @Deprecated
    public static groovy.sql.GroovyRowResult toRowResult(java.sql.ResultSet rs) throws java.sql.SQLException {
        return SqlGroovyMethods.toRowResult(rs);
    }

}
