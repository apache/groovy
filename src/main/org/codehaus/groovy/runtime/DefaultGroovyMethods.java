/*
 * Copyright 2003-2012 the original author or authors.
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
import groovy.util.*;

import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.reflection.MixinInMetaClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.callsite.BooleanClosureWrapper;
import org.codehaus.groovy.runtime.callsite.BooleanReturningMethodInvoker;
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
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class defines new groovy methods which appear on normal JDK
 * classes inside the Groovy environment. Static methods are used with the
 * first parameter being the destination class,
 * i.e. <code>public static String reverse(String self)</code>
 * provides a <code>reverse()</code> method for <code>String</code>.
 * <p>
 * NOTE: While this class contains many 'public' static methods, it is
 * primarily regarded as an internal class (its internal package name
 * suggests this also). We value backwards compatibility of these
 * methods when used within Groovy but value less backwards compatibility
 * at the Java method call level. I.e. future versions of Groovy may
 * remove or move a method call in this file but would normally
 * aim to keep the method available from within Groovy.
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
 * @author jeremi Joslin
 * @author Hamlet D'Arcy
 * @author Cedric Champeau
 * @author Tim Yates
 * @author Dinko Srkoc
 * @author Andre Steingress
 */
public class DefaultGroovyMethods extends DefaultGroovyMethodsSupport {

    private static final Logger LOG = Logger.getLogger(DefaultGroovyMethods.class.getName());
    private static final Integer ONE = 1;
    private static final BigInteger BI_INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final BigInteger BI_INT_MIN = BigInteger.valueOf(Integer.MIN_VALUE);
    private static final BigInteger BI_LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger BI_LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);

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
     * Allows the closure to be called for the object reference self.
     * Synonym for 'with()'.
     *
     * @param self    the object to have a closure act upon
     * @param closure the closure to call on the object
     * @return result of calling the closure
     * @since 1.0
     */
    public static <T> T identity(Object self, Closure<T> closure) {
        return DefaultGroovyMethods.with(self, closure);
    }

    /**
     * Allows the closure to be called for the object reference self. <br/><br/>
     * Any method invoked inside the closure will first be invoked on the
     * self reference. For instance, the following method calls to the append()
     * method are invoked on the StringBuilder instance:
     * <pre>
     * def b = new StringBuilder().with {
     *   append('foo')
     *   append('bar')
     *   return it
     * }
     * assert b.toString() == 'foobar'
     * </pre>
     * This is commonly used to simplify object creation, such as this example:
     * <pre>
     * def p = new Person().with {
     *   firstName = 'John'
     *   lastName = 'Doe'
     *   return it
     * }
     * </pre>
     *
     * @param self    the object to have a closure act upon
     * @param closure the closure to call on the object
     * @return result of calling the closure
     * @since 1.5.0
     */
    public static <T> T with(Object self, Closure<T> closure) {
        @SuppressWarnings("unchecked")
        final Closure<T> clonedClosure = (Closure<T>) closure.clone();
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
     * Retrieves the list of {@link groovy.lang.MetaProperty} objects for 'self' and wraps it
     * in a list of {@link groovy.lang.PropertyValue} objects that additionally provide
     * the value for each property of 'self'.
     *
     * @param self the receiver object
     * @return list of {@link groovy.lang.PropertyValue} objects
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
     * Convenience method that calls {@link #getMetaPropertyValues(java.lang.Object)}(self)
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
    public static <T> T use(Object self, Class categoryClass, Closure<T> closure) {
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
    public static <T> T use(Object self, List<Class> categoryClassList, Closure<T> closure) {
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
        else if (self instanceof Writer)
            printf((Writer) self, format, arg);
        else
            printf(System.out, format, arg);
    }

    private static void printf(PrintStream self, String format, Object arg) {
        self.print(sprintf(self, format, arg));
    }

    private static void printf(Writer self, String format, Object arg) {
        try {
            self.write(sprintf(self, format, arg));
        } catch (IOException e) {
            printf(System.out, format, arg);
        }
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
     * 'Case' implementation for a CharSequence, which simply calls the equivalent method for String.
     *
     * @param caseValue   the case value
     * @param switchValue the switch value
     * @return true if the switchValue's toString() equals the caseValue
     * @since 1.8.2
     */
    public static boolean isCase(CharSequence caseValue, Object switchValue) {
        return isCase(caseValue.toString(), switchValue);
    }

    /**
     * 'Case' implementation for a GString, which simply calls the equivalent method for String.
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
     * }</pre>
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
     * @see java.util.Collection#contains(java.lang.Object)
     * @since 1.0
     */
    public static boolean isCase(Collection caseValue, Object switchValue) {
        return caseValue.contains(switchValue);
    }

    /**
     * 'Case' implementation for maps which tests the groovy truth
     * value obtained using the 'switch' operand as key.
     * For example:
     * <pre class="groovyTestCase">switch( 'foo' ) {
     *   case [foo:true, bar:false]:
     *     assert true
     *     break
     *   default:
     *     assert false
     * }</pre>
     *
     * @param caseValue   the case value
     * @param switchValue the switch value
     * @return the groovy truth value from caseValue corresponding to the switchValue key
     * @since 1.7.6
     */
    public static boolean isCase(Map caseValue, Object switchValue) {
        return DefaultTypeTransformation.castToBoolean(caseValue.get(switchValue));
    }

    /**
     * 'Case' implementation for the {@link java.util.regex.Pattern} class, which allows
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
     * @see #unique(Collection, boolean)
     * @since 1.0
     */
    public static <T> Collection<T> unique(Collection<T> self) {
        return unique(self, true);
    }

    /**
     * Remove all duplicates from a given Collection using the default comparator.
     * If mutate is true, it works by modifying the original object (and also returning it).
     * If mutate is false, a new collection is returned leaving the original unchanged.
     * <pre class="groovyTestCase">
     * assert [1,3] == [1,3,3].unique()
     * </pre>
     * <pre class="groovyTestCase">
     * def orig = [1, 3, 2, 3]
     * def uniq = orig.unique(false)
     * assert orig == [1, 3, 2, 3]
     * assert uniq == [1, 3, 2]
     * </pre>
     *
     * @param self a collection
     * @param mutate false will cause a new list containing unique items from the collection to be created, true will mutate collections in place
     * @return the now modified collection
     * @since 1.8.1
     */
    public static <T> Collection<T> unique(Collection<T> self, boolean mutate) {
        List<T> answer = new ArrayList<T>();
        for (T t : self) {
            boolean duplicated = false;
            for (T t2 : answer) {
                if (coercedEquals(t, t2)) {
                    duplicated = true;
                    break;
                }
            }
            if (!duplicated)
                answer.add(t);
        }
        if (mutate) {
            self.clear();
            self.addAll(answer);
        }
        return mutate ? self : answer ;
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
     * {@link java.lang.Comparable#compareTo(java.lang.Object)} or {@link java.lang.Object#equals(java.lang.Object)}).
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
     * {@link java.lang.Comparable#compareTo(java.lang.Object)} or {@link java.lang.Object#equals(java.lang.Object)}).
     * If the closure takes two parameters, two items from the collection
     * will be passed as arguments, and the closure should return an
     * int value (with 0 indicating the items are not unique).
     * <pre class="groovyTestCase">assert [1,4] == [1,3,4,5].unique { it % 2 }</pre>
     * <pre class="groovyTestCase">assert [2,3,4] == [2,3,3,4].unique { a, b -> a <=> b }</pre>
     *
     * @param self    a Collection
     * @param closure a 1 or 2 arg Closure used to determine unique items
     * @return self   without any duplicates
     * @see #unique(Collection, boolean, Closure)
     * @since 1.0
     */
    public static <T> Collection<T> unique(Collection<T> self, Closure closure) {
        return unique(self, true, closure);
    }

    /**
     * A convenience method for making a collection unique using a Closure to determine duplicate (equal) items.
     * If mutate is true, it works on the receiver object and returns it. If mutate is false, a new collection is returned.
     * </p>
     * If the closure takes a single parameter, the
     * argument passed will be each element, and the closure
     * should return a value used for comparison (either using
     * {@link java.lang.Comparable#compareTo(java.lang.Object)} or {@link java.lang.Object#equals(java.lang.Object)}).
     * If the closure takes two parameters, two items from the collection
     * will be passed as arguments, and the closure should return an
     * int value (with 0 indicating the items are not unique).
     * <pre class="groovyTestCase">
     * def orig = [1, 3, 4, 5]
     * def uniq = orig.unique(false) { it % 2 }
     * assert orig == [1, 3, 4, 5]
     * assert uniq == [1, 4]
     * </pre>
     * <pre class="groovyTestCase">
     * def orig = [2, 3, 3, 4]
     * def uniq = orig.unique(false) { a, b -> a <=> b }
     * assert orig == [2, 3, 3, 4]
     * assert uniq == [2, 3, 4]
     * </pre>
     *
     * @param self    a Collection
     * @param mutate  false will always cause a new list to be created, true will mutate lists in place
     * @param closure a 1 or 2 arg Closure used to determine unique items
     * @return self   without any duplicates
     * @since 1.8.1
     */
    public static <T> Collection<T> unique(Collection<T> self, boolean mutate, Closure closure) {
        // use a comparator of one item or two
        int params = closure.getMaximumNumberOfParameters();
        if (params == 1) {
            OrderBy<T> by = new OrderBy<T>(closure);
            by.setEqualityCheck(true);
            self = unique(self, mutate, by);
        } else {
            self = unique(self, mutate, new ClosureComparator<T>(closure));
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
     * Works on the original object (and also returns it).
     * The order of members in the Collection are compared by the given Comparator.
     * For each duplicate, the first member which is returned
     * by the given Collection's iterator is retained, but all other ones are removed.
     * The given Collection's original order is preserved.
     * <p/>
     * <code><pre class="groovyTestCase">
     *     class Person {
     *         def fname, lname
     *         String toString() {
     *             return fname + " " + lname
     *         }
     *     }
     *
     *     class PersonComparator implements Comparator {
     *         int compare(Object o1, Object o2) {
     *             Person p1 = (Person) o1
     *             Person p2 = (Person) o2
     *             if (p1.lname != p2.lname)
     *                 return p1.lname.compareTo(p2.lname)
     *             else
     *                 return p1.fname.compareTo(p2.fname)
     *         }
     *
     *         boolean equals(Object obj) {
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
     * @return self      the now modified collection without duplicates
     * @see #unique(java.util.Collection, boolean, java.util.Comparator)
     * @since 1.0
     */
    public static <T> Collection<T> unique(Collection<T> self, Comparator<T> comparator) {
        return unique(self, true, comparator) ;
    }

    /**
     * Remove all duplicates from a given Collection.
     * If mutate is true, it works on the original object (and also returns it). If mutate is false, a new collection is returned.
     * The order of members in the Collection are compared by the given Comparator.
     * For each duplicate, the first member which is returned
     * by the given Collection's iterator is retained, but all other ones are removed.
     * The given Collection's original order is preserved.
     * <p/>
     * <code><pre class="groovyTestCase">
     *     class Person {
     *         def fname, lname
     *         String toString() {
     *             return fname + " " + lname
     *         }
     *     }
     *
     *     class PersonComparator implements Comparator {
     *         int compare(Object o1, Object o2) {
     *             Person p1 = (Person) o1
     *             Person p2 = (Person) o2
     *             if (p1.lname != p2.lname)
     *                 return p1.lname.compareTo(p2.lname)
     *             else
     *                 return p1.fname.compareTo(p2.fname)
     *         }
     *
     *         boolean equals(Object obj) {
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
     *     List list2 = list.unique(false, new PersonComparator())
     *     assert( list2 != list && list2 == [a, b, c] )
     * </pre></code>
     *
     *
     * @param self       a Collection
     * @param mutate     false will always cause a new collection to be created, true will mutate collections in place
     * @param comparator a Comparator
     * @return self      the collection without duplicates
     * @since 1.8.1
     */
    public static <T> Collection<T> unique(Collection<T> self, boolean mutate, Comparator<T> comparator) {
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
        if (mutate) {
            self.clear();
            self.addAll(answer);
        }
        return mutate ? self : answer;
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
    public static <T> T eachWithIndex(T self, Closure closure) {
        final Object[] args = new Object[2];
        int counter = 0;
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            args[0] = iter.next();
            args[1] = counter++;
            closure.call(args);
        }
        return self;
    }

    private static <T> Iterator<T> each(Iterator<T> iter, Closure closure) {
        while (iter.hasNext()) {
            Object arg = iter.next();
            closure.call(arg);
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
     * In general, the order in which the map contents are processed
     * cannot be guaranteed. In practise, specialized forms of Map,
     * e.g. a TreeMap will have its contents processed according to
     * the natural ordering of the map.
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
     * Allows a Map to be iterated through in reverse order using a closure.
     *
     * In general, the order in which the map contents are processed
     * cannot be guaranteed. In practise, specialized forms of Map,
     * e.g. a TreeMap will have its contents processed according to the
     * reverse of the natural ordering of the map.
     *
     * @param self    the map over which we iterate
     * @param closure the 1 or 2 arg closure applied on each entry of the map
     * @return returns the self parameter
     * @see #each(Map, Closure)
     * @since 1.7.2
     */
    public static <K, V> Map<K, V> reverseEach(Map<K, V> self, Closure closure) {
        final Iterator<Map.Entry<K, V>> entries = reverse(self.entrySet().iterator());
        while (entries.hasNext()) {
            callClosureForMapEntry(closure, entries.next());
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
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            if (!bcw.call(iter.next())) {
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
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        for (Map.Entry<K, V> entry : self.entrySet()) {
            if (!bcw.callForMap(entry)) {
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
        BooleanReturningMethodInvoker bmi = new BooleanReturningMethodInvoker();
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            if (!bmi.convertToBoolean(iter.next())) {
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
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            if (bcw.call(iter.next())) return true;
        }
        return false;
    }

    /**
     * Iterates over the entries of a map, and checks whether a predicate is
     * valid for at least one entry. If the
     * closure takes one parameter then it will be passed the Map.Entry
     * otherwise if the closure takes two parameters then it will be
     * passed the key and the value.
     * <pre class="groovyTestCase">
     * assert [2:3, 4:5, 5:10].any { key, value -> key * 2 == value }
     * assert ![2:3, 4:5, 5:10].any { entry -> entry.key == entry.value * 2 }
     * </pre>
     *
     * @param self    the map over which we iterate
     * @param closure the 1 or 2 arg closure predicate used for matching
     * @return true if any entry in the map matches the closure predicate
     * @since 1.5.0
     */
    public static <K, V> boolean any(Map<K, V> self, Closure<?> closure) {
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        for (Map.Entry<K, V> entry : self.entrySet()) {
            if (bcw.callForMap(entry)) {
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
        BooleanReturningMethodInvoker bmi = new BooleanReturningMethodInvoker();
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            if (bmi.convertToBoolean(iter.next())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Iterates over the collection of items which this Object represents and returns each item that matches
     * the given filter - calling the <code>{@link #isCase(java.lang.Object, java.lang.Object)}</code>
     * method used by switch statements.  This method can be used with different
     * kinds of filters like regular expressions, classes, ranges etc.
     * Example:
     * <pre class="groovyTestCase">
     * def list = ['a', 'b', 'aa', 'bc', 3, 4.5]
     * assert list.grep( ~/a+/ )  == ['a', 'aa']
     * assert list.grep( ~/../ )  == ['aa', 'bc']
     * assert list.grep( Number ) == [ 3, 4.5 ]
     * assert list.grep{ it.toString().size() == 1 } == [ 'a', 'b', 3 ]
     * </pre>
     *
     * @param self   the object over which we iterate
     * @param filter the filter to perform on the object (using the {@link #isCase(java.lang.Object, java.lang.Object)} method)
     * @return a collection of objects which match the filter
     * @since 1.5.6
     */
    public static Collection grep(Object self, Object filter) {
        Collection answer = createSimilarOrDefaultCollection(self);
        BooleanReturningMethodInvoker bmi = new BooleanReturningMethodInvoker("isCase");
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            Object object = iter.next();
            if (bmi.invoke(filter, object)) {
                answer.add(object);
            }
        }
        return answer;
    }

    /**
     * Iterates over the collection of items which this Object represents and returns each item that matches
     * using the IDENTITY Closure as a filter - effectively returning all elements which satisfy Groovy truth.
     * <p/>
     * Example:
     * <pre class="groovyTestCase">
     * def items = [1, 2, 0, false, true, '', 'foo', [], [4, 5], null]
     * assert items.grep() == [1, 2, true, 'foo', [4, 5]]
     * </pre>
     *
     * @param self   the object over which we iterate
     * @return a collection of objects which match the filter
     * @since 1.8.1
     * @see Closure#IDENTITY
     */
    public static Collection grep(Object self) {
        return grep(self, Closure.IDENTITY);
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
        if (answer <= Integer.MAX_VALUE) return (int) answer;
        return answer;
    }

    /**
     * Counts the number of occurrences which satisfy the given closure from the
     * items within this Iterator.
     * The iterator will become exhausted of elements after determining the count value.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">assert [2,4,2,1,3,5,2,4,3].toSet().iterator().count{ it % 2 == 0 } == 2</pre>
     *
     * @param self  the Iterator from which we count the number of matching occurrences
     * @param closure a closure condition
     * @return the number of occurrences
     * @since 1.8.0
     */
    public static Number count(Iterator self, Closure closure) {
        long answer = 0;
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        while (self.hasNext()) {
            if (bcw.call(self.next())) {
                ++answer;
            }
        }
        // for b/c with Java return an int if we can
        if (answer <= Integer.MAX_VALUE) return (int) answer;
        return answer;
    }

    /**
     * Counts the number of occurrences of the given value inside this collection.
     * Comparison is done using Groovy's == operator (using
     * <code>compareTo(value) == 0</code> or <code>equals(value)</code> ).
     * <p>
     * Example usage:
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
     * Counts the number of occurrences which satisfy the given closure from inside this collection.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">assert [2,4,2,1,3,5,2,4,3].count{ it % 2 == 0 } == 5</pre>
     *
     * @param self  the collection within which we count the number of occurrences
     * @param closure a closure condition
     * @return the number of occurrences
     * @since 1.8.0
     */
    public static Number count(Collection self, Closure closure) {
        return count(self.iterator(), closure);
    }

    /**
     * Counts the number of occurrences which satisfy the given closure from inside this map.
     * If the closure takes one parameter then it will be passed the Map.Entry.
     * Otherwise, the closure should take two parameters and will be passed the key and value.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">assert [a:1, b:1, c:2, d:2].count{ k,v -> k == 'a' || v == 2 } == 3</pre>
     *
     * @param self  the map within which we count the number of occurrences
     * @param closure a 1 or 2 arg Closure condition applying on the entries
     * @return the number of occurrences
     * @since 1.8.0
     */
    public static Number count(Map self, Closure<?> closure) {
        long answer = 0;
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        for (Object entry : self.entrySet()) {
            if (bcw.callForMap((Map.Entry)entry)) {
                ++answer;
            }
        }
        // for b/c with Java return an int if we can
        if (answer <= Integer.MAX_VALUE) return (int) answer;
        return answer;
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
     * Counts the number of occurrences which satisfy the given closure from inside this array.
     *
     * @param self  the array within which we count the number of occurrences
     * @param closure a closure condition
     * @return the number of occurrences
     * @since 1.8.0
     */
    public static Number count(Object[] self, Closure closure) {
        return count(Arrays.asList(self), closure);
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
     * Convert a Collection to a List. Always returns a new List
     * even if the Collection is already a List.
     * <p>
     * Example usage:
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
     * Convert an Iterable to a List. The Iterable's iterator will
     * become exhausted of elements after making this conversion.
     *
     * @param self an Iterable
     * @return a List
     * @since 1.8.7
     */
    public static <T> List<T> toList(Iterable<T> self) {
        return toList(self.iterator());
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
     * Collates this list into sub-lists of length <code>size</code>.
     * Example:
     * <pre class="groovyTestCase">def list = [ 1, 2, 3, 4, 5, 6, 7 ]
     * def coll = list.collate( 3 )
     * assert coll == [ [ 1, 2, 3 ], [ 4, 5, 6 ], [ 7 ] ]</pre>
     *
     * @param self          a List
     * @param size          the length of each sub-list in the returned list
     * @return a List containing the data collated into sub-lists
     * @since 1.8.6
     */
    public static <T> List<List<T>> collate( List<T> self, int size ) {
        return collate( self, size, size, true ) ;
    }

    /**
     * Collates this list into sub-lists of length <code>size</code> stepping through the code <code>step</code>
     * elements for each subList.
     * Example:
     * <pre class="groovyTestCase">def list = [ 1, 2, 3, 4 ]
     * def coll = list.collate( 3, 1 )
     * assert coll == [ [ 1, 2, 3 ], [ 2, 3, 4 ], [ 3, 4 ], [ 4 ] ]</pre>
     *
     * @param self          a List
     * @param size          the length of each sub-list in the returned list
     * @param step          the number of elements to step through for each sub-list
     * @return a List containing the data collated into sub-lists
     * @since 1.8.6
     */
    public static <T> List<List<T>> collate( List<T> self, int size, int step ) {
        return collate( self, size, step, true ) ;
    }

    /**
     * Collates this list into sub-lists of length <code>size</code>. Any remaining elements in
     * the list after the subdivision will be dropped if <code>keepRemainder</code> is false.
     * Example:
     * <pre class="groovyTestCase">def list = [ 1, 2, 3, 4, 5, 6, 7 ]
     * def coll = list.collate( 3, false )
     * assert coll == [ [ 1, 2, 3 ], [ 4, 5, 6 ] ]</pre>
     *
     * @param self          a List
     * @param size          the length of each sub-list in the returned list
     * @param keepRemainder if true, any rmeaining elements are returned as sub-lists.  Otherwise they are discarded
     * @return a List containing the data collated into sub-lists
     * @since 1.8.6
     */
    public static <T> List<List<T>> collate( List<T> self, int size, boolean keepRemainder ) {
        return collate( self, size, size, keepRemainder ) ;
    }

    /**
     * Collates this list into sub-lists of length <code>size</code> stepping through the code <code>step</code>
     * elements for each sub-list.  Any remaining elements in the list after the subdivision will be dropped if
     * <code>keepRemainder</code> is false.
     * Example:
     * <pre class="groovyTestCase">def list = [ 1, 2, 3, 4 ]
     * assert list.collate( 3, 1, true  ) == [ [ 1, 2, 3 ], [ 2, 3, 4 ], [ 3, 4 ], [ 4 ] ]
     * assert list.collate( 3, 1, false ) == [ [ 1, 2, 3 ], [ 2, 3, 4 ] ]</pre>
     *
     * @param self          a List
     * @param size          the length of each sub-list in the returned list
     * @param step          the number of elements to step through for each sub-list
     * @param keepRemainder if true, any rmeaining elements are returned as sub-lists.  Otherwise they are discarded
     * @return a List containing the data collated into sub-lists
     * @since 1.8.6
     */
    public static <T> List<List<T>> collate( List<T> self, int size, int step, boolean keepRemainder ) {
        List<List<T>> answer = new ArrayList<List<T>>();
        if( size <= 0 || self.size() == 0 ) {
            answer.add( self ) ;
        }
        else {
            for( int pos = 0 ; pos < self.size() && pos > -1 ; pos += step ) {
                if( !keepRemainder && pos > self.size() - size ) {
                    break ;
                }
                List<T> element = new ArrayList<T>() ;
                for( int offs = pos ; offs < pos + size && offs < self.size() ; offs++ ) {
                    element.add( self.get( offs ) ) ;
                }
                answer.add( element ) ;
            }
        }
        return answer ;
    }

    /**
     * Iterates through this aggregate Object transforming each item into a new value using the
     * <code>transform</code> closure, returning a list of transformed values.
     * Example:
     * <pre class="groovyTestCase">def list = [1, 'a', 1.23, true ]
     * def types = list.collect { it.class }
     * assert types == [Integer, String, BigDecimal, Boolean]</pre>
     *
     * @param self      an aggregate Object with an Iterator returning its items
     * @param transform the closure used to transform each item of the aggregate object
     * @return a List of the transformed values
     * @since 1.0
     */
    public static <T> List<T> collect(Object self, Closure<T> transform) {
        return (List<T>) collect(self, new ArrayList<T>(), transform);
    }

    /**
     * Iterates through this aggregate Object transforming each item into a new value using Closure.IDENTITY
     * as a transformer, basically returning a list of items copied from the original object.
     * <pre class="groovyTestCase">assert [1,2,3] == [1,2,3].iterator().collect()</pre>
     *
     * @param self an aggregate Object with an Iterator returning its items
     * @return a List of the transformed values
     * @see Closure#IDENTITY
     * @since 1.8.5
     */
    public static Collection collect(Object self) {
        return collect(self, Closure.IDENTITY);
    }

    /**
     * Iterates through this aggregate Object transforming each item into a new value using the <code>transform</code> closure
     * and adding it to the supplied <code>collector</code>.
     *
     * @param self      an aggregate Object with an Iterator returning its items
     * @param collector the Collection to which the transformed values are added
     * @param transform the closure used to transform each item of the aggregate object
     * @return the collector with all transformed values added to it
     * @since 1.0
     */
    public static <T> Collection<T> collect(Object self, Collection<T> collector, Closure<? extends T> transform) {
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext(); ) {
            collector.add(transform.call(iter.next()));
        }
        return collector;
    }

    /**
     * Iterates through this collection transforming each entry into a new value using the <code>transform</code> closure
     * returning a list of transformed values.
     * <pre class="groovyTestCase">assert [2,4,6] == [1,2,3].collect { it * 2 }</pre>
     *
     * @param self      a collection
     * @param transform the closure used to transform each item of the collection
     * @return a List of the transformed values
     * @since 1.0
     */
    public static <T> List<T> collect(Collection<?> self, Closure<T> transform) {
        return (List<T>) collect(self, new ArrayList<T>(self.size()), transform);
    }

    /**
     * Iterates through this collection transforming each entry into a new value using Closure.IDENTITY
     * as a transformer, basically returning a list of items copied from the original collection.
     * <pre class="groovyTestCase">assert [1,2,3] == [1,2,3].collect()</pre>
     *
     * @param self    a collection
     * @return a List of the transformed values
     * @since 1.8.5
     * @see Closure#IDENTITY
     */
    public static <T> List<T> collect(Collection<T> self) {
        return (List<T>) collect(self, Closure.IDENTITY);
    }

    /**
     * Iterates through this collection transforming each value into a new value using the <code>transform</code> closure
     * and adding it to the supplied <code>collector</code>.
     * <pre class="groovyTestCase">assert [1,2,3] as HashSet == [2,4,5,6].collect(new HashSet()) { (int)(it / 2) }</pre>
     *
     * @param self      a collection
     * @param collector the Collection to which the transformed values are added
     * @param transform the closure used to transform each item of the collection
     * @return the collector with all transformed values added to it
     * @since 1.0
     */
    public static <T> Collection<T> collect(Collection<?> self, Collection<T> collector, Closure<? extends T> transform) {
        for (Object item : self) {
            collector.add(transform.call(item));
            if (transform.getDirective() == Closure.DONE) {
                break;
            }
        }
        return collector;
    }

    /**
     * Deprecated alias for collectNested
     *
     * @deprecated Use collectNested instead
     * @see #collectNested(Collection, Closure)
     */
    public static List collectAll(Collection self, Closure transform) {
        return collectNested(self, transform);
    }

    /**
     * Recursively iterates through this collection transforming each non-Collection value
     * into a new value using the closure as a transformer. Returns a potentially nested
     * list of transformed values.
     * <pre class="groovyTestCase">
     * assert [2,[4,6],[8],[]] == [1,[2,3],[4],[]].collectNested { it * 2 }
     * </pre>
     *
     * @param self      a collection
     * @param transform the closure used to transform each item of the collection
     * @return the resultant collection
     * @since 1.8.1
     */
    public static List collectNested(Collection self, Closure transform) {
        return (List) collectNested(self, new ArrayList(self.size()), transform);
    }

    /**
     * Deprecated alias for collectNested
     *
     * @deprecated Use collectNested instead
     * @see #collectNested(Collection, Collection, Closure)
     */
    public static Collection collectAll(Collection self, Collection collector, Closure transform) {
        return collectNested(self, collector, transform);
    }

    /**
     * Recursively iterates through this collection transforming each non-Collection value
     * into a new value using the <code>transform</code> closure. Returns a potentially nested
     * collection of transformed values.
     * <pre class="groovyTestCase">def x = [1,[2,3],[4],[]].collectNested(new Vector()) { it * 2 }
     * assert x == [2,[4,6],[8],[]]
     * assert x instanceof Vector</pre>
     *
     * @param self      a collection
     * @param collector an initial Collection to which the transformed values are added
     * @param transform the closure used to transform each element of the collection
     * @return the collector with all transformed values added to it
     * @since 1.8.1
     */
    public static Collection collectNested(Collection self, Collection collector, Closure transform) {
        for (Object item : self) {
            if (item instanceof Collection) {
                Collection c = (Collection) item;
                collector.add(collectNested(c, createSimilarCollection(collector, c.size()), transform));
            } else {
                collector.add(transform.call(item));
            }
            if (transform.getDirective() == Closure.DONE) {
                break;
            }
        }
        return collector;
    }

    /**
     * Projects each item from a source collection to a collection and concatenates (flattens) the resulting collections into a single list.
     * <p/>
     * <pre class="groovyTestCase">
     * def nums = 1..10
     * def squaresAndCubesOfEvens = nums.collectMany{ it % 2 ? [] : [it**2, it**3] }
     * assert squaresAndCubesOfEvens == [4, 8, 16, 64, 36, 216, 64, 512, 100, 1000]
     *
     * def animals = ['CAT', 'DOG', 'ELEPHANT'] as Set
     * def smallAnimals = animals.collectMany{ it.size() > 3 ? [] : [it.toLowerCase()] }
     * assert smallAnimals == ['cat', 'dog']
     *
     * def orig = nums as Set
     * def origPlusIncrements = orig.collectMany{ [it, it+1] }
     * assert origPlusIncrements.size() == orig.size() * 2
     * assert origPlusIncrements.unique().size() == orig.size() + 1
     * </pre>
     *
     * @param self       a collection
     * @param projection a projecting Closure returning a collection of items
     * @return a list created from the projected collections concatenated (flattened) together
     * @see #sum(java.util.Collection, groovy.lang.Closure)
     * @since 1.8.1
     */
    public static <T> List<T> collectMany(Collection self, Closure<Collection<? extends T>> projection) {
        return (List<T>) collectMany(self, new ArrayList<T>(), projection);
    }

    /**
     * Projects each item from a source collection to a result collection and concatenates (flattens) the resulting
     * collections adding them into the <code>collector</code>.
     * <p/>
     * <pre class="groovyTestCase">
     * def animals = ['CAT', 'DOG', 'ELEPHANT'] as Set
     * def smallAnimals = animals.collectMany(['ant', 'bee']){ it.size() > 3 ? [] : [it.toLowerCase()] }
     * assert smallAnimals == ['ant', 'bee', 'cat', 'dog']
     *
     * def nums = 1..5
     * def origPlusIncrements = nums.collectMany([] as Set){ [it, it+1] }
     * assert origPlusIncrements.size() == nums.size() + 1
     * </pre>
     *
     * @param self       a collection
     * @param collector  an initial collection to add the projected items to
     * @param projection a projecting Closure returning a collection of items
     * @return the collector with the projected collections concatenated (flattened) to it
     * @since 1.8.5
     */
    public static <T> Collection<T> collectMany(Collection self, Collection<T> collector, Closure<Collection<? extends T>> projection) {
        for (Object next : self) {
            collector.addAll(projection.call(next));
        }
        return collector;
    }

    /**
     * Projects each item from a source map to a result collection and concatenates (flattens) the resulting
     * collections adding them into the <code>collector</code>.
     * <p/>
     * <pre class="groovyTestCase">
     * def map = [bread:3, milk:5, butter:2]
     * def result = map.collectMany(['x']){ k, v -> k.startsWith('b') ? k.toList() : [] }
     * assert result == ['x', 'b', 'r', 'e', 'a', 'd', 'b', 'u', 't', 't', 'e', 'r']
     * </pre>
     *
     * @param self       a map
     * @param collector  an initial collection to add the projected items to
     * @param projection a projecting Closure returning a collection of items
     * @return the collector with the projected collections concatenated (flattened) to it
     * @since 1.8.8
     */
    public static <T> Collection<T> collectMany(Map<?, ?> self, Collection<T> collector, Closure<Collection<? extends T>> projection) {
        for (Map.Entry<?, ?> entry : self.entrySet()) {
            collector.addAll(callClosureForMapEntry(projection, entry));
        }
        return collector;
    }

    /**
     * Projects each item from a source map to a result collection and concatenates (flattens) the resulting
     * collections adding them into a collection.
     * <p/>
     * <pre class="groovyTestCase">
     * def map = [bread:3, milk:5, butter:2]
     * def result = map.collectMany{ k, v -> k.startsWith('b') ? k.toList() : [] }
     * assert result == ['b', 'r', 'e', 'a', 'd', 'b', 'u', 't', 't', 'e', 'r']
     * </pre>
     *
     * @param self       a map
     * @param projection a projecting Closure returning a collection of items
     * @return the collector with the projected collections concatenated (flattened) to it
     * @since 1.8.8
     */
    public static <T> Collection<T> collectMany(Map<?, ?> self, Closure<Collection<? extends T>> projection) {
        return collectMany(self, new ArrayList<T>(), projection);
    }

    /**
     * Projects each item from a source array to a collection and concatenates (flattens) the resulting collections into a single list.
     * <p/>
     * <pre class="groovyTestCase">
     * def nums = [1, 2, 3, 4, 5, 6] as Object[]
     * def squaresAndCubesOfEvens = nums.collectMany{ it % 2 ? [] : [it**2, it**3] }
     * assert squaresAndCubesOfEvens == [4, 8, 16, 64, 36, 216]
     * </pre>
     *
     * @param self       an object array
     * @param projection a projecting Closure returning a collection of items
     * @return a list created from the projected collections concatenated (flattened) together
     * @see #sum(Object[], groovy.lang.Closure)
     * @since 1.8.1
     */
    public static <T> List<T> collectMany(Object[] self, Closure<Collection<? extends T>> projection) {
        return collectMany(toList(self), projection);
    }

    /**
     * Projects each item from a source iterator to a collection and concatenates (flattens) the resulting collections into a single list.
     * <p/>
     * <pre class="groovyTestCase">
     * def numsIter = [1, 2, 3, 4, 5, 6].iterator()
     * def squaresAndCubesOfEvens = numsIter.collectMany{ it % 2 ? [] : [it**2, it**3] }
     * assert squaresAndCubesOfEvens == [4, 8, 16, 64, 36, 216]
     * </pre>
     *
     * @param self       an iterator
     * @param projection a projecting Closure returning a collection of items
     * @return a list created from the projected collections concatenated (flattened) together
     * @see #sum(Iterator, groovy.lang.Closure)
     * @since 1.8.1
     */
    public static <T> List<T> collectMany(Iterator<Object> self, Closure<Collection<? extends T>> projection) {
        return collectMany(toList(self), projection);
    }

    /**
     * Iterates through this Map transforming each map entry into a new value using the <code>transform</code> closure
     * returning the <code>collector</code> with all transformed vakues added to it.
     * <pre class="groovyTestCase">assert [a:1, b:2].collect( [] as HashSet ) { key, value -> key*value } == ["a", "bb"] as Set
     * assert [3:20, 2:30].collect( [] as HashSet ) { entry -> entry.key * entry.value } == [60] as Set</pre>
     *
     * @param self      a Map
     * @param collector the Collection to which transformed values are added
     * @param transform the transformation closure which can take one (Map.Entry) or two (key, value) parameters
     * @return the collector with all transformed values added to it
     * @since 1.0
     */
    public static <T> Collection<T> collect(Map<?, ?> self, Collection<T> collector, Closure<? extends T> transform) {
        for (Map.Entry<?, ?> entry : self.entrySet()) {
            collector.add(callClosureForMapEntry(transform, entry));
        }
        return collector;
    }

    /**
     * Iterates through this Map transforming each map entry into a new value using the <code>transform</code> closure
     * returning a list of transformed values.
     * <pre class="groovyTestCase">assert [a:1, b:2].collect { key, value -> key*value } == ["a", "bb"]
     * assert [3:20, 2:30].collect { entry -> entry.key * entry.value } == [60, 60]</pre>
     *
     * @param self    a Map
     * @param transform the transformation closure which can take one (Map.Entry) or two (key, value) parameters
     * @return the resultant list of transformed values
     * @since 1.0
     */
    public static <T> List<T> collect(Map self, Closure<T> transform) {
        return (List<T>) collect(self, new ArrayList<T>(self.size()), transform);
    }

    /**
     * Iterates through this Map transforming each map entry using the <code>transform</code> closure
     * returning a map of the transformed entries.
     * <pre class="groovyTestCase">
     * assert [a:1, b:2].collectEntries( [:] ) { k, v -> [v, k] } == [1:'a', 2:'b']
     * assert [a:1, b:2].collectEntries( [30:'C'] ) { key, value ->
     *     [(value*10): key.toUpperCase()] } == [10:'A', 20:'B', 30:'C']
     * </pre>
     *
     * @param self      a Map
     * @param collector the Map into which the transformed entries are put
     * @param transform the closure used for transforming, which can take one (Map.Entry) or two (key, value) parameters and
     *                  should return a Map.Entry, a Map or a two-element list containing the resulting key and value
     * @return the collector with all transformed values added to it
     * @see #collect(Map, Collection, Closure)
     * @since 1.7.9
     */
    public static <K, V> Map<K, V> collectEntries(Map<?, ?> self, Map<K, V> collector, Closure<?> transform) {
        for (Map.Entry<?, ?> entry : self.entrySet()) {
            addEntry(collector, callClosureForMapEntry(transform, entry));
        }
        return collector;
    }

    /**
     * Iterates through this Map transforming each entry using the <code>transform</code> closure
     * and returning a map of the transformed entries.
     * <pre class="groovyTestCase">
     * assert [a:1, b:2].collectEntries { key, value -> [value, key] } == [1:'a', 2:'b']
     * assert [a:1, b:2].collectEntries { key, value ->
     *     [(value*10): key.toUpperCase()] } == [10:'A', 20:'B']
     * </pre>
     *
     * @param self      a Map
     * @param transform the closure used for transforming, which can take one (Map.Entry) or two (key, value) parameters and
     *                  should return a Map.Entry, a Map or a two-element list containing the resulting key and value
     * @return a Map of the transformed entries
     * @see #collect(Map, Collection, Closure)
     * @since 1.7.9
     */
    public static Map<?, ?> collectEntries(Map<?, ?> self, Closure<?> transform) {
        return collectEntries(self, createSimilarMap(self), transform);
    }

    /**
     * Iterates through this Collection transforming each item using the <code>transform</code> closure
     * and returning a map of the resulting transformed entries.
     * <pre class="groovyTestCase">
     * def letters = "abc"
     * // collect letters with index using list style
     * assert (0..2).collectEntries { index -> [index, letters[index]] } == [0:'a', 1:'b', 2:'c']
     * // collect letters with index using map style
     * assert (0..2).collectEntries { index -> [(index): letters[index]] } == [0:'a', 1:'b', 2:'c']
     * </pre>
     *
     * @param self      a Collection
     * @param transform the closure used for transforming, which has an item from self as the parameter and
     *                  should return a Map.Entry, a Map or a two-element list containing the resulting key and value
     * @return a Map of the transformed entries
     * @see #collectEntries(Collection, Map, Closure)
     * @since 1.7.9
     */
    public static <K, V> Map<K, V> collectEntries(Collection<?> self, Closure<?> transform) {
        return collectEntries(self, new LinkedHashMap<K, V>(), transform);
    }

    /**
     * A variant of collectEntries using the identity closure as the transform.
     * The source collection should be a list of <code>[key, value]</code> tuples or a <code>Map.Entry</code>.
     * <pre class="groovyTestCase">
     * def nums = [1, 10, 100, 1000]
     * def tuples = nums.collect{ [it, it.toString().size()] }
     * assert tuples == [[1, 1], [10, 2], [100, 3], [1000, 4]]
     * def map = tuples.collectEntries()
     * assert map == [1:1, 10:2, 100:3, 1000:4]
     * </pre>
     *
     * @param self      a Collection
     * @return a Map of the transformed entries
     * @see #collectEntries(Collection, Closure)
     * @since 1.8.5
     */
    public static <K, V> Map<K, V> collectEntries(Collection<?> self) {
        return collectEntries(self, new LinkedHashMap<K, V>(), Closure.IDENTITY);
    }

    /**
     * Iterates through this Collection transforming each item using the closure
     * as a transformer into a map entry, returning a map of the transformed entries.
     * <pre class="groovyTestCase">
     * def letters = "abc"
     * // collect letters with index
     * assert (0..2).collectEntries( [:] ) { index -> [index, letters[index]] } == [0:'a', 1:'b', 2:'c']
     * assert (0..2).collectEntries( [4:'d'] ) { index ->
     *     [(index+1): letters[index]] } == [1:'a', 2:'b', 3:'c', 4:'d']
     * </pre>
     *
     * @param self      a Collection
     * @param collector the Map into which the transformed entries are put
     * @param transform the closure used for transforming, which has an item from self as the parameter and
     *                  should return a Map.Entry, a Map or a two-element list containing the resulting key and value
     * @return the collector with all transformed values added to it
     * @see #collect(Map, Collection, Closure)
     * @since 1.7.9
     */
    public static <K, V> Map<K, V> collectEntries(Collection<?> self, Map<K, V> collector, Closure<?> transform) {
        for (Object next : self) {
            addEntry(collector, transform.call(next));
        }
        return collector;
    }

    /**
     * A variant of collectEntries using the identity closure as the transform.
     *
     * @param self      a Collection
     * @param collector the Map into which the transformed entries are put
     * @return the collector with all transformed values added to it
     * @see #collectEntries(Collection, Map, Closure)
     * @since 1.8.5
     */
    public static <K, V> Map<K, V> collectEntries(Collection<?> self, Map<K, V> collector) {
        return collectEntries(self, collector, Closure.IDENTITY);
    }

    /**
     * Iterates through this array transforming each item using the <code>transform</code> closure
     * and returning a map of the resulting transformed entries.
     * <pre class="groovyTestCase">
     * def letters = "abc"
     * def nums = [0, 1, 2] as Integer[]
     * // collect letters with index
     * assert nums.collectEntries( [:] ) { index -> [index, letters[index]] } == [0:'a', 1:'b', 2:'c']
     * assert nums.collectEntries( [4:'d'] ) { index ->
     *     [(index+1): letters[index]] } == [1:'a', 2:'b', 3:'c', 4:'d']
     * </pre>
     *
     * @param self      an Object array
     * @param collector the Map into which the transformed entries are put
     * @param transform the closure used for transforming, which has an item from self as the parameter and
     *                  should return a Map.Entry, a Map or a two-element list containing the resulting key and value
     * @return the collector with all transformed values added to it
     * @see #collect(Map, Collection, Closure)
     * @since 1.7.9
     */
    public static <K, V> Map<K, V> collectEntries(Object[] self, Map<K, V> collector, Closure<?> transform) {
        return collectEntries(toList(self), collector, transform);
    }

    /**
     * A variant of collectEntries using the identity closure as the transform.
     *
     * @param self      an Object array
     * @param collector the Map into which the transformed entries are put
     * @return the collector with all transformed values added to it
     * @see #collectEntries(Object[], Map, Closure)
     * @since 1.8.5
     */
    public static <K, V> Map<K, V> collectEntries(Object[] self, Map<K, V> collector) {
        return collectEntries(self, collector, Closure.IDENTITY);
    }

    /**
     * Iterates through this array transforming each item using the <code>transform</code> closure
     * and returning a map of the resulting transformed entries.
     * <pre class="groovyTestCase">
     * def letters = "abc"
     * def nums = [0, 1, 2] as Integer[]
     * // collect letters with index using list style
     * assert nums.collectEntries { index -> [index, letters[index]] } == [0:'a', 1:'b', 2:'c']
     * // collect letters with index using map style
     * assert nums.collectEntries { index -> [(index): letters[index]] } == [0:'a', 1:'b', 2:'c']
     * </pre>
     *
     * @param self      a Collection
     * @param transform the closure used for transforming, which has an item from self as the parameter and
     *                  should return a Map.Entry, a Map or a two-element list containing the resulting key and value
     * @return a Map of the transformed entries
     * @see #collectEntries(Collection, Map, Closure)
     * @since 1.7.9
     */
    public static <K, V> Map<K, V> collectEntries(Object[] self, Closure<?> transform) {
        return collectEntries(toList(self), new LinkedHashMap<K, V>(), transform);
    }

    /**
     * A variant of collectEntries using the identity closure as the transform.
     *
     * @param self      an Object array
     * @return the collector with all transformed values added to it
     * @see #collectEntries(Object[], Closure)
     * @since 1.8.5
     */
    public static <K, V> Map<K, V> collectEntries(Object[] self) {
        return collectEntries(self, Closure.IDENTITY);
    }

    private static <K, V> void addEntry(Map<K, V> result, Object newEntry) {
        if (newEntry instanceof Map) {
            leftShift(result, (Map)newEntry);
        } else if (newEntry instanceof List && ((List)newEntry).size() == 2) {
            List list = (List) newEntry;
            leftShift(result, new MapEntry(list.get(0), list.get(1)));
        } else {
            // TODO: enforce stricter behavior?
            // given Map.Entry is an interface, we get a proxy which gives us lots
            // of flexibility but sometimes the error messages might be unexpected
            leftShift(result, asType(newEntry, Map.Entry.class));
        }
    }

    /**
     * Finds the first value matching the closure condition
     *
     * @param self    an Object with an iterator returning its values
     * @param closure a closure condition
     * @return the first Object found or null if none was found
     * @since 1.0
     */
    public static Object find(Object self, Closure closure) {
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            Object value = iter.next();
            if (bcw.call(value)) {
                return value;
            }
        }
        return null;
    }

    /**
     * Finds the first item matching the IDENTITY Closure (i.e.&nbsp;matching Groovy truth).
     * <p/>
     * Example:
     * <pre class="groovyTestCase">
     * def items = [null, 0, 0.0, false, '', [], 42, 43]
     * assert items.find() == 42
     * </pre>
     *
     * @param self    an Object with an Iterator returning its values
     * @return the first Object found or null if none was found
     * @since 1.8.1
     * @see Closure#IDENTITY
     */
    public static Object find(Object self) {
        return find(self, Closure.IDENTITY);
    }

    /**
     * Treats the object as iterable, iterating through the values it represents and returns the first non-null result obtained from calling the closure, otherwise returns the defaultResult.
     *
     * @param self    an Object with an iterator returning its values
     * @param defaultResult an Object that should be returned if all closure results are null
     * @param closure a closure that returns a non-null value when processing should stop
     * @return the first non-null result of the closure, otherwise the default value
     * @since 1.7.5
     */
    public static Object findResult(Object self, Object defaultResult, Closure closure) {
        Object result = findResult(self, closure);
        if (result == null) return defaultResult;
        return result;
    }

    /**
     * Treats the object as iterable, iterating through the values it represents and returns the first non-null result obtained from calling the closure, otherwise returns null.
     *
     * @param self    an Object with an iterator returning its values
     * @param closure a closure that returns a non-null value when processing should stop
     * @return the first non-null result of the closure
     * @since 1.7.5
     */
    public static Object findResult(Object self, Closure closure) {
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            Object value = iter.next();
            Object result = closure.call(value);
            if (result != null) {
                return result;
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
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        for (T value : self) {
            if (bcw.call(value)) {
                return value;
            }
        }
        return null;
    }

    /**
     * Finds the first item matching the IDENTITY Closure (i.e.&nbsp;matching Groovy truth).
     * <p/>
     * Example:
     * <pre class="groovyTestCase">
     * def items = [null, 0, 0.0, false, '', [], 42, 43]
     * assert items.find() == 42
     * </pre>
     *
     * @param self    a Collection
     * @return the first Object found or null if none was found
     * @since 1.8.1
     * @see Closure#IDENTITY
     */
    public static <T> T find(Collection<T> self) {
        return find(self, Closure.IDENTITY);
    }

    /**
     * Iterates through the collection calling the given closure for each item but stopping once the first non-null
     * result is found and returning that result.&nbsp;If all are null, the defaultResult is returned.
     * <p/>
     * Examples:
     * <pre class="groovyTestCase">
     * def list = [1,2,3]
     * assert "Found 2" == list.findResult("default") { it > 1 ? "Found $it" : null }
     * assert "default" == list.findResult("default") { it > 3 ? "Found $it" : null }
     * </pre>
     *
     * @param self          a Collection
     * @param defaultResult an Object that should be returned if all closure results are null
     * @param closure a closure that returns a non-null value when processing should stop and a value should be returned
     * @return the first non-null result from calling the closure, or the defaultValue
     * @since 1.7.5
     */
    public static <T, U extends T, V extends T> T findResult(Collection<?> self, U defaultResult, Closure<V> closure) {
        T result = findResult(self, closure);
        if (result == null) return defaultResult;
        return result;
    }

    /**
     * Iterates through the collection calling the given closure for each item but stopping once the first non-null
     * result is found and returning that result.&nbsp;If all results are null, null is returned.
     * <p/>
     * Example:
     * <pre class="groovyTestCase">
     * def list = [1,2,3]
     * assert "Found 2" == list.findResult { it > 1 ? "Found $it" : null }
     * </pre>
     *
     * @param self    a Collection
     * @param closure a closure that returns a non-null value when processing should stop and a value should be returned
     * @return the first non-null result from calling the closure, or null
     * @since 1.7.5
     */
    public static <T> T findResult(Collection<?> self, Closure<T> closure) {
        for (Object value : self) {
            T result = closure.call(value);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Iterates through the collection transforming items using the supplied closure
     * and collecting any non-null results.
     * <p/>
     * Example:
     * <pre class="groovyTestCase">
     * def list = [1,2,3]
     * def result = list.findResults { it > 1 ? "Found $it" : null }
     * assert result == ["Found 2", "Found 3"]
     * </pre>
     *
     * @param self               a Collection
     * @param filteringTransform a Closure that should return either a non-null transformed value or null for items which should be discarded
     * @return the list of non-null transformed values
     * @since 1.8.1
     */
    public static <T> Collection<T> findResults(Collection<?> self, Closure<T> filteringTransform) {
        List<T> result = new ArrayList<T>();
        for (Object value : self) {
            T transformed = filteringTransform.call(value);
            if (transformed != null) {
                result.add(transformed);
            }
        }
        return result;
    }

    /**
     * Iterates through the map transforming items using the supplied closure
     * and collecting any non-null results.
     * If the closure takes two parameters, the entry key and value are passed.
     * If the closure takes one parameter, the Map.Entry object is passed.
     * <p/>
     * Example:
     * <pre class="groovyTestCase">
     * def map = [a:1, b:2, hi:2, cat:3, dog:2]
     * def result = map.findResults { k, v -> k.size() == v ? "Found $k:$v" : null }
     * assert result == ["Found a:1", "Found hi:2", "Found cat:3"]
     * </pre>
     *
     * @param self               a Map
     * @param filteringTransform a 1 or 2 arg Closure that should return either a non-null transformed value or null for items which should be discarded
     * @return the list of non-null transformed values
     * @since 1.8.1
     */
    public static <T> Collection<T> findResults(Map<?, ?> self, Closure<T> filteringTransform) {
        List<T> result = new ArrayList<T>();
        for (Map.Entry<?, ?> entry : self.entrySet()) {
            T transformed = callClosureForMapEntry(filteringTransform, entry);
            if (transformed != null) {
                result.add(transformed);
            }
        }
        return result;
    }

    /**
     * Finds the first entry matching the closure condition.
     * If the closure takes two parameters, the entry key and value are passed.
     * If the closure takes one parameter, the Map.Entry object is passed.
     * <pre class="groovyTestCase">assert [a:1, b:3].find { it.value == 3 }.key == "b"</pre>
     *
     * @param self    a Map
     * @param closure a 1 or 2 arg Closure condition
     * @return the first Object found
     * @since 1.0
     */
    public static <K, V> Map.Entry<K, V> find(Map<K, V> self, Closure<?> closure) {
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        for (Map.Entry<K, V> entry : self.entrySet()) {
            if (bcw.callForMap(entry)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Returns the first non-null closure result found by passing each map entry to the closure, otherwise the defaultResult is returned.
     * If the closure takes two parameters, the entry key and value are passed.
     * If the closure takes one parameter, the Map.Entry object is passed.
     * <pre class="groovyTestCase">
     * assert "Found b:3" == [a:1, b:3].findResult("default") { if (it.value == 3) return "Found ${it.key}:${it.value}" }
     * assert "default" == [a:1, b:3].findResult("default") { if (it.value == 9) return "Found ${it.key}:${it.value}" }
     * assert "Found a:1" == [a:1, b:3].findResult("default") { k, v -> if (k.size() + v == 2) return "Found $k:$v" }
     * </pre>
     *
     * @param self    a Map
     * @param defaultResult an Object that should be returned if all closure results are null
     * @param closure a 1 or 2 arg Closure that returns a non-null value when processing should stop and a value should be returned
     * @return the first non-null result collected by calling the closure, or the defaultResult if no such result was found
     * @since 1.7.5
     */
    public static <T, U extends T, V extends T> T findResult(Map<?, ?> self, U defaultResult, Closure<V> closure) {
        T result = findResult(self, closure);
        if (result == null) return defaultResult;
        return result;
    }

    /**
     * Returns the first non-null closure result found by passing each map entry to the closure, otherwise null is returned.
     * If the closure takes two parameters, the entry key and value are passed.
     * If the closure takes one parameter, the Map.Entry object is passed.
     * <pre class="groovyTestCase">
     * assert "Found b:3" == [a:1, b:3].findResult { if (it.value == 3) return "Found ${it.key}:${it.value}" }
     * assert null == [a:1, b:3].findResult { if (it.value == 9) return "Found ${it.key}:${it.value}" }
     * assert "Found a:1" == [a:1, b:3].findResult { k, v -> if (k.size() + v == 2) return "Found $k:$v" }
     * </pre>
     *
     * @param self    a Map
     * @param closure a 1 or 2 arg Closure that returns a non-null value when processing should stop and a value should be returned
     * @return the first non-null result collected by calling the closure, or null if no such result was found
     * @since 1.7.5
     */
    public static <T> T findResult(Map<?, ?> self, Closure<T> closure) {
        for (Map.Entry<?, ?> entry : self.entrySet()) {
            T result = callClosureForMapEntry(closure, entry);
            if (result != null) {
                return result;
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
     * Finds the items matching the IDENTITY Closure (i.e.&nbsp;matching Groovy truth).
     * <p/>
     * Example:
     * <pre class="groovyTestCase">
     * def items = [1, 2, 0, false, true, '', 'foo', [], [4, 5], null]
     * assert items.findAll() == [1, 2, true, 'foo', [4, 5]]
     * </pre>
     *
     * @param self    a Collection
     * @return a List of the values found
     * @since 1.8.1
     * @see Closure#IDENTITY
     */
    public static <T> Collection<T>  findAll(Collection<T> self) {
        return findAll(self, Closure.IDENTITY);
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

    /**
     * Finds all items matching the IDENTITY Closure (i.e.&nbsp;matching Groovy truth).
     * <p/>
     * Example:
     * <pre class="groovyTestCase">
     * def items = [1, 2, 0, false, true, '', 'foo', [], [4, 5], null]
     * assert items.findAll() == [1, 2, true, 'foo', [4, 5]]
     * </pre>
     *
     * @param self    an Object with an Iterator returning its values
     * @return a List of the values found
     * @since 1.8.1
     * @see Closure#IDENTITY
     */
    public static Collection findAll(Object self) {
        return findAll(self, Closure.IDENTITY);
    }

    private static <T> Collection<T> findAll(Closure closure, Collection<T> answer, Iterator<? extends T> iter) {
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        while (iter.hasNext()) {
            T value = iter.next();
            if (bcw.call(value)) {
                answer.add(value);
            }
        }
        return answer;
    }

    /**
     * Returns <tt>true</tt> if this collection contains all of the elements
     * in the specified array.
     *
     * @param  self  a Collection to be checked for containment
     * @param  items array to be checked for containment in this collection
     * @return <tt>true</tt> if this collection contains all of the elements
     *           in the specified array
     * @see    Collection#containsAll(Collection)
     * @since 1.7.2
     */
    public static boolean containsAll(Collection self, Object[] items) {
        return self.containsAll(Arrays.asList(items));
    }

    /**
     * Modifies this collection by removing its elements that are contained
     * within the specified object array.
     *
     * See also <code>findAll</code> and <code>grep</code> when wanting to produce a new list
     * containing items which don't match some criteria while leaving the original collection unchanged.
     *
     * @param  self  a Collection to be modified
     * @param  items array containing elements to be removed from this collection
     * @return <tt>true</tt> if this collection changed as a result of the call
     * @see    Collection#removeAll(Collection)
     * @since 1.7.2
     */
    public static boolean removeAll(Collection self, Object[] items) {
        Collection pickFrom = new TreeSet(new NumberAwareComparator());
        pickFrom.addAll(Arrays.asList(items));
        return self.removeAll(pickFrom);
    }

    /**
     * Modifies this collection so that it retains only its elements that are contained
     * in the specified array.  In other words, removes from this collection all of
     * its elements that are not contained in the specified array.
     *
     * See also <code>grep</code> and <code>findAll</code> when wanting to produce a new list
     * containing items which match some specified items but leaving the original collection unchanged.
     *
     * @param  self  a Collection to be modified
     * @param  items array containing elements to be retained from this collection
     * @return <tt>true</tt> if this collection changed as a result of the call
     * @see    Collection#retainAll(Collection)
     * @since 1.7.2
     */
    public static boolean retainAll(Collection self, Object[] items) {
        Collection pickFrom = new TreeSet(new NumberAwareComparator());
        pickFrom.addAll(Arrays.asList(items));
        return self.retainAll(pickFrom);
    }

    /**
     * Modifies this collection so that it retains only its elements
     * that are matched according to the specified closure condition.  In other words,
     * removes from this collection all of its elements that don't match.
     *
     * See also <code>findAll</code> and <code>grep</code> when wanting to produce a new list
     * containing items which match some criteria but leaving the original collection unchanged.
     *
     * @param  self      a Collection to be modified
     * @param  condition a closure condition
     * @return <tt>true</tt> if this collection changed as a result of the call
     * @see    Iterator#remove()
     * @since 1.7.2
     */
    public static boolean retainAll(Collection self, Closure condition) {
        Iterator iter = InvokerHelper.asIterator(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(condition);
        boolean result = false;
        while (iter.hasNext()) {
            Object value = iter.next();
            if (!bcw.call(value)) {
                iter.remove();
                result = true;
            }
        }
        return result;
    }

    /**
     * Modifies this collection by removing the elements that are matched according
     * to the specified closure condition.
     *
     * See also <code>findAll</code> and <code>grep</code> when wanting to produce a new list
     * containing items which don't match some criteria while leaving the original collection unchanged.
     *
     * @param  self a Collection to be modified
     * @param  condition a closure condition
     * @return <tt>true</tt> if this collection changed as a result of the call
     * @see    Iterator#remove()
     * @since 1.7.2
     */
    public static boolean removeAll(Collection self, Closure condition) {
        Iterator iter = InvokerHelper.asIterator(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(condition);
        boolean result = false;
        while (iter.hasNext()) {
            Object value = iter.next();
            if (bcw.call(value)) {
                iter.remove();
                result = true;
            }
        }
        return result;
    }

    /**
     * Modifies the collection by adding all of the elements in the specified array to the collection.
     * The behavior of this operation is undefined if
     * the specified array is modified while the operation is in progress.
     *
     * See also <code>plus</code> or the '+' operator if wanting to produce a new collection
     * containing additional items but while leaving the original collection unchanged.
     *
     * @param  self  a Collection to be modified
     * @param  items array containing elements to be added to this collection
     * @return <tt>true</tt> if this collection changed as a result of the call
     * @see    Collection#addAll(Collection)
     * @since 1.7.2
     */
    public static <T> boolean addAll(Collection<T> self, T[] items) {
        return self.addAll(Arrays.asList(items));
    }

    /**
     * Modifies this list by inserting all of the elements in the specified array into the
     * list at the specified position.  Shifts the
     * element currently at that position (if any) and any subsequent
     * elements to the right (increases their indices).  The new elements
     * will appear in this list in the order that they occur in the array.
     * The behavior of this operation is undefined if the specified array
     * is modified while the operation is in progress.
     *
     * See also <code>plus</code> for similar functionality with copy semantics, i.e. which produces a new
     * list after adding the additional items at the specified position but leaves the original list unchanged.
     *
     * @param self  a list to be modified
     * @param items array containing elements to be added to this collection
     * @param index index at which to insert the first element from the
     *              specified array
     * @return <tt>true</tt> if this collection changed as a result of the call
     * @see    List#addAll(int, Collection)
     * @since 1.7.2
     */
    public static <T> boolean addAll(List<T> self, int index, T[] items) {
        return self.addAll(index, Arrays.asList(items));
    }

    /**
     * Splits all items into two lists based on the closure condition.
     * The first list contains all items matching the closure expression.
     * The second list all those that don't.
     *
     * @param self    an Object with an Iterator returning its values
     * @param closure a closure condition
     * @return a List whose first item is the accepted values and whose second item is the rejected values
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
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">assert [[2,4],[1,3]] == [1,2,3,4].split { it % 2 == 0 }</pre>
     *
     * @param self    a Collection of values
     * @param closure a closure condition
     * @return a List whose first item is the accepted values and whose second item is the rejected values
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
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        while (iter.hasNext()) {
            T value = iter.next();
            if (bcw.call(value)) {
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
     * <p>
     * Example usage:
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
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">def result = [1, 2, 3].subsequences()
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
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">def result = [1, 2, 3].permutations()
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
     * <p>
     * Example usage:
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
     * Adds GroovyCollections#transpose(List) as a method on lists. <br/>
     * A TransposeFunction takes a collection of columns and returns a collection of
     * rows. The first row consists of the first element from each column. Successive
     * rows are constructed similarly.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">def result = [['a', 'b'], [1, 2]].transpose()
     * assert result == [['a', 1], ['b', 2]]</pre>
     * <pre class="groovyTestCase">def result = [['a', 'b'], [1, 2], [3, 4]].transpose()
     * assert result == [['a', 1, 3], ['b', 2, 4]]</pre>
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
     * <p>
     * Example usage:
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
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        for (Map.Entry<K, V> entry : self.entrySet()) {
            if (bcw.callForMap(entry)) {
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
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">assert [0:[2,4,6], 1:[1,3,5]] == [1,2,3,4,5,6].groupBy { it % 2 }</pre>
     *
     * @param self    a collection to group
     * @param closure a closure mapping entries on keys
     * @return a new Map grouped by keys
     * @since 1.0
     */
    public static <K, T> Map<K, List<T>> groupBy(Collection<T> self, Closure<K> closure) {
        Map<K, List<T>> answer = new LinkedHashMap<K, List<T>>();
        for (T element : self) {
            K value = closure.call(element);
            groupAnswer(answer, element, value);
        }
        return answer;
    }

    /**
     * Sorts all collection members into (sub)groups determined by the supplied
     * mapping closures. Each closure should return the key that this item
     * should be grouped by. The returned LinkedHashMap will have an entry for each
     * distinct 'key path' returned from the closures, with each value being a list
     * of items for that 'group path'. <p>
     *
     * Example usage:
     * <pre class="groovyTestCase">def result = [1,2,3,4,5,6].groupBy({ it % 2 }, { it < 4 })
     * assert result == [1:[(true):[1, 3], (false):[5]], 0:[(true):[2], (false):[4, 6]]]</pre>
     *
     * Another example:
     * <pre>def sql = groovy.sql.Sql.newInstance(/&ast; ... &ast;/)
     * def data = sql.rows("SELECT * FROM a_table").groupBy({ it.column1 }, { it.column2 }, { it.column3 })
     * if (data.val1.val2.val3) {
     *     // there exists a record where:
     *     //   a_table.column1 == val1
     *     //   a_table.column2 == val2, and
     *     //   a_table.column3 == val3
     * } else {
     *     // there is no such record
     * }</pre>
     * If an empty array of closures is supplied the IDENTITY Closure will be used.
     *
     * @param self     a collection to group
     * @param closures an array of closures, each mapping entries on keys
     * @return a new Map grouped by keys on each criterion
     * @since 1.8.1
     * @see Closure#IDENTITY
     */
    public static Map groupBy(Collection self, Object... closures) {
        final Closure head = closures.length == 0 ? Closure.IDENTITY : (Closure) closures[0];

        @SuppressWarnings("unchecked")
        Map<Object, List> first = groupBy(self, head);
        if (closures.length < 2)
            return first;

        final Object[] tail = new Object[closures.length - 1];
        System.arraycopy(closures, 1, tail, 0, closures.length - 1); // Arrays.copyOfRange only since JDK 1.6

        // inject([:]) { a,e -> a << [(e.key): e.value.groupBy(tail)] }
        Map<Object, Map> acc = new LinkedHashMap<Object, Map>();
        for (Map.Entry<Object, List> item : first.entrySet()) {
            acc.put(item.getKey(), groupBy(item.getValue(), tail));
        }

        return acc;
    }

    /**
     * Sorts all collection members into (sub)groups determined by the supplied
     * mapping closures. Each closure should return the key that this item
     * should be grouped by. The returned LinkedHashMap will have an entry for each
     * distinct 'key path' returned from the closures, with each value being a list
     * of items for that 'group path'. <p>
     *
     * Example usage:
     * <pre class="groovyTestCase">def result = [1,2,3,4,5,6].groupBy([{ it % 2 }, { it < 4 }])
     * assert result == [1:[(true):[1, 3], (false):[5]], 0:[(true):[2], (false):[4, 6]]]</pre>
     *
     * Another example:
     * <pre>def sql = groovy.sql.Sql.newInstance(/&ast; ... &ast;/)
     * def data = sql.rows("SELECT * FROM a_table").groupBy([{ it.column1 }, { it.column2 }, { it.column3 }])
     * if (data.val1.val2.val3) {
     *     // there exists a record where:
     *     //   a_table.column1 == val1
     *     //   a_table.column2 == val2, and
     *     //   a_table.column3 == val3
     * } else {
     *     // there is no such record
     * }</pre>
     * If an empty list of closures is supplied the IDENTITY Closure will be used.
     *
     * @param self     a collection to group
     * @param closures a list of closures, each mapping entries on keys
     * @return a new Map grouped by keys on each criterion
     * @since 1.8.1
     * @see Closure#IDENTITY
     */
    public static Map groupBy(Collection self, List<Closure> closures) {
        return groupBy(self, closures.toArray());
    }


    /**
     * Sorts all collection members into groups determined by the supplied mapping
     * closure and counts the group size.  The closure should return the key that each
     * item should be grouped by.  The returned Map will have an entry for each
     * distinct key returned from the closure, with each value being the frequency of
     * items occurring for that group.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">assert [0:2, 1:3] == [1,2,3,4,5].countBy { it % 2 }</pre>
     *
     * @param self    a collection to group and count
     * @param closure a closure mapping items to the frequency keys
     * @return a new Map grouped by keys with frequency counts
     * @since 1.8.0
     */
    public static <K> Map<K, Integer> countBy(Collection self, Closure<K> closure) {
        return countBy(self.iterator(), closure);
    }

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
     * @param self    an object array to group and count
     * @param closure a closure mapping items to the frequency keys
     * @return a new Map grouped by keys with frequency counts
     * @see #countBy(Collection, Closure)
     * @since 1.8.0
     */
    public static <K> Map<K, Integer> countBy(Object[] self, Closure<K> closure) {
        return countBy(Arrays.asList(self), closure);
    }

    /**
     * Sorts all iterator items into groups determined by the supplied mapping
     * closure and counts the group size.  The closure should return the key that each
     * item should be grouped by.  The returned Map will have an entry for each
     * distinct key returned from the closure, with each value being the frequency of
     * items occurring for that group.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">assert [1,2,2,2,3].toSet().iterator().countBy{ it % 2 } == [1:2, 0:1]</pre>
     *
     * @param self    an iterator to group and count
     * @param closure a closure mapping items to the frequency keys
     * @return a new Map grouped by keys with frequency counts
     * @see #countBy(Collection, Closure)
     * @since 1.8.0
     */
    public static <K> Map<K, Integer> countBy(Iterator self, Closure<K> closure) {
        Map<K, Integer> answer = new LinkedHashMap<K, Integer>();
        while (self.hasNext()) {
            K value = closure.call(self.next());
            countAnswer(answer, value);
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
     * group. (If instead of a list of map entries, you want an actual map
     * use {code}groupBy{code}.)
     * <pre class="groovyTestCase">def result = [a:1,b:2,c:3,d:4,e:5,f:6].groupEntriesBy { it.value % 2 }
     * assert result[0]*.key == ["b", "d", "f"]
     * assert result[1]*.value == [1, 3, 5]</pre>
     *
     * @param self    a map to group
     * @param closure a 1 or 2 arg Closure mapping entries on keys
     * @return a new Map grouped by keys
     * @since 1.5.2
     */
    public static <G, K, V> Map<G, List<Map.Entry<K, V>>> groupEntriesBy(Map<K, V> self, Closure<G> closure) {
        final Map<G, List<Map.Entry<K, V>>> answer = new LinkedHashMap<G, List<Map.Entry<K, V>>>();
        for (Map.Entry<K, V> entry : self.entrySet()) {
            G value = callClosureForMapEntry(closure, entry);
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
     * belong to each group. (If instead of a map, you want a list of map entries
     * use {code}groupEntriesBy{code}.)
     * <p>
     * If the <code>self</code> map is one of TreeMap, Hashtable or Properties,
     * the returned Map will preserve that type, otherwise a LinkedHashMap will
     * be returned.
     * <pre class="groovyTestCase">def result = [a:1,b:2,c:3,d:4,e:5,f:6].groupBy { it.value % 2 }
     * assert result == [0:[b:2, d:4, f:6], 1:[a:1, c:3, e:5]]</pre>
     *
     * @param self    a map to group
     * @param closure a closure mapping entries on keys
     * @return a new Map grouped by keys
     * @since 1.0
     */
    public static <G, K, V> Map<G, Map<K, V>> groupBy(Map<K, V> self, Closure<G> closure) {
        final Map<G, List<Map.Entry<K, V>>> initial = groupEntriesBy(self, closure);
        final Map<G, Map<K, V>> answer = new LinkedHashMap<G, Map<K, V>>();
        for (Map.Entry<G, List<Map.Entry<K, V>>> outer : initial.entrySet()) {
            G key = outer.getKey();
            List<Map.Entry<K, V>> entries = outer.getValue();
            Map<K, V> target = createSimilarMap(self);
            putAll(target, entries);
            answer.put(key, target);
        }
        return answer;
    }

    /**
     * Groups the members of a map into sub maps determined by the supplied
     * mapping closures. Each closure will be passed a Map.Entry or key and
     * value (depending on the number of parameters the closure accepts) and
     * should return the key that each item should be grouped under. The
     * resulting map will have an entry for each 'group path' returned by all
     * closures, with values being the map members from the original map that
     * belong to each such 'group path'. <p>
     *
     * If the <code>self</code> map is one of TreeMap, Hashtable, or Properties,
     * the returned Map will preserve that type, otherwise a LinkedHashMap will
     * be returned.
     *
     * <pre class="groovyTestCase">def result = [a:1,b:2,c:3,d:4,e:5,f:6].groupBy({ it.value % 2 }, { it.key.next() })
     * assert result == [1:[b:[a:1], d:[c:3], f:[e:5]], 0:[c:[b:2], e:[d:4], g:[f:6]]]</pre>
     * If an empty array of closures is supplied the IDENTITY Closure will be used.
     *
     * @param self     a map to group
     * @param closures an array of closures that map entries on keys
     * @return a new map grouped by keys on each criterion
     * @since 1.8.1
     * @see Closure#IDENTITY
     */
    public static Map<Object, Map> groupBy(Map self, Object... closures) {
        @SuppressWarnings("unchecked")
        final Closure<Object> head = closures.length == 0 ? Closure.IDENTITY : (Closure) closures[0];

        @SuppressWarnings("unchecked")
        Map<Object, Map> first = groupBy(self, head);
        if (closures.length < 2)
            return first;

        final Object[] tail = new Object[closures.length - 1];
        System.arraycopy(closures, 1, tail, 0, closures.length - 1); // Arrays.copyOfRange only since JDK 1.6

        Map<Object, Map> acc = new LinkedHashMap<Object, Map>();
        for (Map.Entry<Object, Map> item: first.entrySet()) {
            acc.put(item.getKey(), groupBy(item.getValue(), tail));
        }

        return acc;
    }

    /**
     * Groups the members of a map into sub maps determined by the supplied
     * mapping closures. Each closure will be passed a Map.Entry or key and
     * value (depending on the number of parameters the closure accepts) and
     * should return the key that each item should be grouped under. The
     * resulting map will have an entry for each 'group path' returned by all
     * closures, with values being the map members from the original map that
     * belong to each such 'group path'. <p>
     *
     * If the <code>self</code> map is one of TreeMap, Hashtable, or Properties,
     * the returned Map will preserve that type, otherwise a LinkedHashMap will
     * be returned.
     *
     * <pre class="groovyTestCase">def result = [a:1,b:2,c:3,d:4,e:5,f:6].groupBy([{ it.value % 2 }, { it.key.next() }])
     * assert result == [1:[b:[a:1], d:[c:3], f:[e:5]], 0:[c:[b:2], e:[d:4], g:[f:6]]]</pre>
     * If an empty list of closures is supplied the IDENTITY Closure will be used.
     *
     * @param self     a map to group
     * @param closures a list of closures that map entries on keys
     * @return a new map grouped by keys on each criterion
     * @since 1.8.1
     * @see Closure#IDENTITY
     */
    public static Map<Object, Map> groupBy(Map self, List<Closure> closures) {
        return groupBy(self, closures.toArray());
    }

    /**
     * Groups the members of a map into groups determined by the
     * supplied mapping closure and counts the frequency of the created groups.
     * The closure will be passed a Map.Entry or
     * key and value (depending on the number of parameters the closure accepts)
     * and should return the key that each item should be grouped under.  The
     * resulting map will have an entry for each 'group' key returned by the
     * closure, with values being the frequency counts for that 'group'.
     * <p>
     * <pre class="groovyTestCase">def result = [a:1,b:2,c:3,d:4,e:5].countBy { it.value % 2 }
     * assert result == [0:2, 1:3]</pre>
     *
     * @param self    a map to group and count
     * @param closure a closure mapping entries to frequency count keys
     * @return a new Map grouped by keys with frequency counts
     * @since 1.8.0
     */
    public static <K> Map<K, Integer> countBy(Map self, Closure<K> closure) {
        Map<K, Integer> answer = new LinkedHashMap<K, Integer>();
        for (Object entry : self.entrySet()) {
            countAnswer(answer, callClosureForMapEntry(closure, (Map.Entry) entry));
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
    protected static <K, T> void groupAnswer(final Map<K, List<T>> answer, T element, K value) {
        if (answer.containsKey(value)) {
            answer.get(value).add(element);
        } else {
            List<T> groupedElements = new ArrayList<T>();
            groupedElements.add(element);
            answer.put(value, groupedElements);
        }
    }

    private static <T> void countAnswer(final Map<T, Integer> answer, T mappedKey) {
        if (!answer.containsKey(mappedKey)) {
            answer.put(mappedKey, 0);
        }
        int current = answer.get(mappedKey);
        answer.put(mappedKey, current + 1);
    }

    // internal helper method
    protected static <T> T callClosureForMapEntry(Closure<T> closure, Map.Entry entry) {
        if (closure.getMaximumNumberOfParameters() == 2) {
            return closure.call(new Object[]{entry.getKey(), entry.getValue()});
        }
        return closure.call(entry);
    }

    // internal helper method
    protected static <T> T callClosureForLine(Closure<T> closure, String line, int counter) {
        if (closure.getMaximumNumberOfParameters() == 2) {
            return closure.call(new Object[]{line, counter});
        }
        return closure.call(line);
    }

    // internal helper method
    protected static <T> T callClosureForMapEntryAndCounter(Closure<T> closure, Map.Entry entry, int counter) {
        if (closure.getMaximumNumberOfParameters() == 3) {
            return closure.call(new Object[]{entry.getKey(), entry.getValue(), counter});
        }
        if (closure.getMaximumNumberOfParameters() == 2) {
            return closure.call(new Object[]{entry, counter});
        }
        return closure.call(entry);
    }


    /**
     * Performs the same function as the version of inject that takes an initial value, but
     * uses the head of the Collection as the initial value, and iterates over the tail.
     * <pre class="groovyTestCase">
     * assert 1 * 2 * 3 * 4 == [ 1, 2, 3, 4 ].inject { acc, val -> acc * val }
     * assert ['b'] == [['a','b'], ['b','c'], ['d','b']].inject { acc, val -> acc.intersect( val ) }
     * LinkedHashSet set = [ 't', 'i', 'm' ]
     * assert 'tim' == set.inject { a, b -> a + b }
     * </pre>
     *
     * @param self         a Collection
     * @param closure      a closure
     * @return the result of the last closure call
     * @throws NoSuchElementException if the collection is empty.
     * @see #inject(Collection, Object, Closure)
     * @since 1.8.7
     */
    public static <T, V extends T> T inject(Collection<T> self, Closure<V> closure ) {
        if( self.isEmpty() ) {
            throw new NoSuchElementException( "Cannot call inject() on an empty collection without passing an initial value." ) ;
        }
        List<T> list = asList( self ) ;
        if( list.size() == 1 ) {
            return list.get( 0 ) ;
        }
        return (T) inject( drop( list, 1 ), head( list ), closure );
    }

    /**
     * Iterates through the given Collection, passing in the initial value to
     * the 2-arg closure along with the first item. The result is passed back (injected) into
     * the closure along with the second item. The new result is injected back into
     * the closure along with the third item and so on until the entire collection
     * has been used. Also known as <tt>foldLeft</tt> or <tt>reduce</tt> in functional parlance.
     *
     * Examples:
     * <pre class="groovyTestCase">
     * assert 1*1*2*3*4 == [1,2,3,4].inject(1) { acc, val -> acc * val }
     *
     * assert 0+1+2+3+4 == [1,2,3,4].inject(0) { acc, val -> acc + val }
     *
     * assert 'The quick brown fox' ==
     *     ['quick', 'brown', 'fox'].inject('The') { acc, val -> acc + ' ' + val }
     *
     * assert 'bat' ==
     *     ['rat', 'bat', 'cat'].inject('zzz') { min, next -> next < min ? next : min }
     *
     * def max = { a, b -> [a, b].max() }
     * def animals = ['bat', 'rat', 'cat']
     * assert 'rat' == animals.inject('aaa', max)
     * </pre>
     * Visual representation of the last example above:
     * <pre>
     *    initVal  animals[0]
     *       v        v
     * max('aaa',   'bat')  =>  'bat'  animals[1]
     *                            v       v
     *                      max('bat',  'rat')  =>  'rat'  animals[2]
     *                                                v       v
     *                                          max('rat',  'cat')  =>  'rat'
     * </pre>
     *
     * @param self         a Collection
     * @param initialValue some initial value
     * @param closure      a closure
     * @return the result of the last closure call
     * @since 1.0
     */
    public static <T, U extends T, V extends T> T inject(Collection self, U initialValue, Closure<V> closure) {
        return (T) inject(self.iterator(), initialValue, closure);
    }

    /**
     * Iterates through the given Map, passing in the initial value to
     * the 2-arg Closure along with the first item (or 3-arg Closure along with the first key and value).
     * The result is passed back (injected) into
     * the closure along with the second item. The new result is injected back into
     * the closure along with the third item and so on until the entire collection
     * has been used. Also known as <tt>foldLeft</tt> or <tt>reduce</tt> in functional parlance.
     *
     * Examples:
     * <pre class="groovyTestCase">
     * def map = [a:1, b:2, c:3]
     * assert map.inject([]) { list, k, v ->
     *   list + [k] * v
     * } == ['a', 'b', 'b', 'c', 'c', 'c']
     * </pre>
     *
     * @param self         a Map
     * @param initialValue some initial value
     * @param closure      a 2 or 3 arg Closure
     * @return the result of the last closure call
     * @since 1.8.1
     */
    public static <T, U extends T, V extends T> T inject(Map<?, ?> self, U initialValue, Closure<V> closure) {
        T value = initialValue;
        for (Map.Entry<?, ?> entry : self.entrySet()) {
            if (closure.getMaximumNumberOfParameters() == 3) {
                value = closure.call(value, entry.getKey(), entry.getValue());
            } else {
                value = closure.call(value, entry);
            }
        }
        return value;
    }


    /**
     * Iterates through the given Iterator, passing in the initial value to
     * the closure along with the first item. The result is passed back (injected) into
     * the closure along with the second item. The new result is injected back into
     * the closure along with the third item and so on until the Iterator has been
     * expired of values. Also known as foldLeft in functional parlance.
     *
     * @param self         an Iterator
     * @param initialValue some initial value
     * @param closure      a closure
     * @return the result of the last closure call
     * @see #inject(Collection, Object, Closure)
     * @since 1.5.0
     */
    public static <T, U extends T, V extends T> T inject(Iterator self, U initialValue, Closure<V> closure) {
        T value = initialValue;
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
     * Iterates through the given Object, passing in the first value to
     * the closure along with the first item. The result is passed back (injected) into
     * the closure along with the second item. The new result is injected back into
     * the closure along with the third item and so on until further iteration of
     * the object is not possible. Also known as foldLeft in functional parlance.
     *
     * @param self         an Object
     * @param closure      a closure
     * @return the result of the last closure call
     * @throws NoSuchElementException if the collection is empty.
     * @see #inject(Collection, Object, Closure)
     * @since 1.8.7
     */
    public static <T, V extends T> T inject(Object self, Closure<V> closure) {
        Iterator iter = InvokerHelper.asIterator(self);
        if( !iter.hasNext() ) {
            throw new NoSuchElementException( "Cannot call inject() over an empty iterable without passing an initial value." ) ;
        }
        Object initialValue = iter.next() ;
        return (T) inject(iter, initialValue, closure);
    }

    /**
     * Iterates through the given Object, passing in the initial value to
     * the closure along with the first item. The result is passed back (injected) into
     * the closure along with the second item. The new result is injected back into
     * the closure along with the third item and so on until further iteration of
     * the object is not possible. Also known as foldLeft in functional parlance.
     *
     * @param self         an Object
     * @param initialValue some initial value
     * @param closure      a closure
     * @return the result of the last closure call
     * @see #inject(Collection, Object, Closure)
     * @since 1.5.0
     */
    public static <T, U extends T, V extends T> T inject(Object self, U initialValue, Closure<V> closure) {
        Iterator iter = InvokerHelper.asIterator(self);
        return (T) inject(iter, initialValue, closure);
    }

    /**
     * Iterates through the given array as with inject(Object[],initialValue,closure), but
     * using the first element of the array as the initialValue, and then iterating
     * the remaining elements of the array.
     *
     * @param self         an Object[]
     * @param closure      a closure
     * @return the result of the last closure call
     * @throws NoSuchElementException if the array is empty.
     * @see #inject(Object[], Object, Closure)
     * @since 1.8.7
     */
    public static <T, V extends T> T inject(Object[] self, Closure<V> closure) {
        return inject( (Object)self, closure ) ;
    }

    /**
     * Iterates through the given array, passing in the initial value to
     * the closure along with the first item. The result is passed back (injected) into
     * the closure along with the second item. The new result is injected back into
     * the closure along with the third item and so on until all elements of the array
     * have been used. Also known as foldLeft in functional parlance.
     *
     * @param self         an Object[]
     * @param initialValue some initial value
     * @param closure      a closure
     * @return the result of the last closure call
     * @see #inject(Collection, Object, Closure)
     * @since 1.5.0
     */
    public static <T, U extends T, V extends T> T inject(Object[] self, U initialValue, Closure<V> closure) {
        Object[] params = new Object[2];
        T value = initialValue;
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
     * @see #sum(java.util.Collection)
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
     * Selects an item in the collection having the minimum
     * value as determined by the supplied closure.
     * If more than one item has the minimum value,
     * an arbitrary choice is made between the items having the minimum value.
     * </p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     * <pre class="groovyTestCase">
     * assert "hi" == ["hello","hi","hey"].min { it.length() }
     * </pre>
     * <pre class="groovyTestCase">
     * def lastDigit = { a, b -> a % 10 <=> b % 10 }
     * assert [19, 55, 91].min(lastDigit) == 91
     * </pre>
     * <pre class="groovyTestCase">
     * def pets = ['dog', 'cat', 'anaconda']
     * def shortestName = pets.min{ it.size() } // one of 'dog' or 'cat'
     * assert shortestName.size() == 3
     * </pre>
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
     * Selects an entry in the map having the minimum
     * calculated value as determined by the supplied closure.
     * If more than one entry has the minimum value,
     * an arbitrary choice is made between the entries having the minimum value.
     * </p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     * <pre class="groovyTestCase">
     * def zoo = [monkeys:6, lions:5, tigers:7]
     * def leastCommonEntry = zoo.min{ it.value }
     * assert leastCommonEntry.value == 5
     * def mostCommonEntry = zoo.min{ a, b -> b.value <=> a.value } // double negative!
     * assert mostCommonEntry.value == 7
     * </pre>
     * Edge case for multiple min values:
     * <pre class="groovyTestCase">
     * def zoo = [monkeys:6, lions:5, tigers:7]
     * def lastCharOfName = { e -> e.key[-1] }
     * def ans = zoo.min(lastCharOfName) // some random entry
     * assert lastCharOfName(ans) == 's'
     * </pre>
     *
     * @param self    a Map
     * @param closure a 1 or 2 arg Closure used to determine the correct ordering
     * @return the Map.Entry having the minimum value as determined by the closure
     * @since 1.7.6
     */
    public static <K, V> Map.Entry<K, V> min(Map<K, V> self, Closure closure) {
        return min(self.entrySet(), closure);
    }

    /**
     * Selects an entry in the map having the maximum
     * calculated value as determined by the supplied closure.
     * If more than one entry has the maximum value,
     * an arbitrary choice is made between the entries having the maximum value.
     * </p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison. An example:
     * <pre class="groovyTestCase">
     * def zoo = [monkeys:6, lions:5, tigers:7]
     * def mostCommonEntry = zoo.max{ it.value }
     * assert mostCommonEntry.value == 7
     * def leastCommonEntry = zoo.max{ a, b -> b.value <=> a.value } // double negative!
     * assert leastCommonEntry.value == 5
     * </pre>
     * Edge case for multiple max values:
     * <pre class="groovyTestCase">
     * def zoo = [monkeys:6, lions:5, tigers:7]
     * def lengthOfNamePlusNumber = { e -> e.key.size() + e.value }
     * def ans = zoo.max(lengthOfNamePlusNumber) // one of [monkeys:6, tigers:7]
     * assert lengthOfNamePlusNumber(ans) == 13
     * </pre>
     *
     * @param self    a Map
     * @param closure a 1 or 2 arg Closure used to determine the correct ordering
     * @return the Map.Entry having the maximum value as determined by the closure
     * @since 1.7.6
     */
    public static <K, V> Map.Entry<K, V> max(Map<K, V> self, Closure closure) {
        return max(self.entrySet(), closure);
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
     * Selects an item in the collection having the maximum
     * value as determined by the supplied closure.
     * If more than one item has the maximum value,
     * an arbitrary choice is made between the items having the maximum value.
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
     * <pre class="groovyTestCase">
     * def pets = ['dog', 'elephant', 'anaconda']
     * def longestName = pets.max{ it.size() } // one of 'elephant' or 'anaconda'
     * assert longestName.size() == 8
     * </pre>
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
     * Provide the standard Groovy <code>size()</code> method for <code>CharSequence</code>.
     *
     * @param text a CharSequence
     * @return the length of the CharSequence
     * @since 1.8.2
     */
    public static int size(CharSequence text) {
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
     * @see java.lang.StringBuilder#reverse()
     */
    public static String reverse(String self) {
        return new StringBuilder(self).reverse().toString();
    }

    /**
     * Creates a new CharSequence which is the reverse (backwards) of this string
     *
     * @param self a CharSequence
     * @return a new CharSequence with all the characters reversed.
     * @see #reverse(String)
     * @since 1.8.2
     */
    public static CharSequence reverse(CharSequence self) {
        return new StringBuilder(self).reverse().toString();
    }

    /**
     * <p>Strip leading whitespace/control characters followed by '|' from
     * every line in a String.</p>
     * <pre class="groovyTestCase">
     * assert 'ABC\n123\n456' == '''ABC
     *                             |123
     *                             |456'''.stripMargin()
     * </pre>
     *
     * @param self The String to strip the margin from
     * @return the stripped String
     * @see #stripMargin(String, char)
     * @since 1.7.3
     */
    public static String stripMargin(String self) {
        return stripMargin(self, '|');
    }

    /**
     * <p>Strip leading whitespace/control characters followed by '|' from
     * every line in a CharSequence.</p>
     *
     * @param self The CharSequence to strip the margin from
     * @return the stripped CharSequence
     * @see #stripMargin(CharSequence, char)
     * @since 1.8.2
     */
    public static CharSequence stripMargin(CharSequence self) {
        return stripMargin(self, '|');
    }

    /**
     * <p>Strip leading whitespace/control characters followed by <tt>marginChar</tt> from
     * every line in a String.</p>
     *
     * @param self       The String to strip the margin from
     * @param marginChar Any character that serves as margin delimiter
     * @return the stripped String
     * @see #stripMargin(String, char)
     * @since 1.7.3
     */
    public static String stripMargin(String self, String marginChar) {
        if (marginChar == null || marginChar.length() == 0) return stripMargin(self, '|');
        // TODO IllegalArgumentException for marginChar.length() > 1 ? Or support String as marker?
        return stripMargin(self, marginChar.charAt(0));
    }

    /**
     * <p>Strip leading whitespace/control characters followed by <tt>marginChar</tt> from
     * every line in a CharSequence.</p>
     *
     * @param self       The CharSequence to strip the margin from
     * @param marginChar Any character that serves as margin delimiter
     * @return the stripped CharSequence
     * @see #stripMargin(String, String)
     * @since 1.8.2
     */
    public static String stripMargin(CharSequence self, CharSequence marginChar) {
        return stripMargin(self.toString(), marginChar.toString());
    }

    /**
     * <p>Strip leading whitespace/control characters followed by <tt>marginChar</tt> from
     * every line in a String.</p>
     * <pre class="groovyTestCase">
     * assert 'ABC\n123\n456' == '''ABC
     *                             *123
     *                             *456'''.stripMargin('*')
     * </pre>
     *
     * @param self       The String to strip the margin from
     * @param marginChar Any character that serves as margin delimiter
     * @return the stripped String
     * @since 1.7.3
     */
    public static String stripMargin(String self, char marginChar) {
        if (self.length() == 0) return self;
        try {
            StringBuilder builder = new StringBuilder();
            for (String line : readLines(self)) {
                builder.append(stripMarginFromLine(line, marginChar));
                builder.append("\n");
            }
            // remove the normalized ending line ending if it was not present
            if (!self.endsWith("\n")) {
                builder.deleteCharAt(builder.length() - 1);
            }
            return builder.toString();
        } catch (IOException e) {
            /* ignore */
        }
        return self;
    }

    /**
     * <p>Strip leading whitespace/control characters followed by <tt>marginChar</tt> from
     * every line in a String.</p>
     *
     * @param self       The CharSequence to strip the margin from
     * @param marginChar Any character that serves as margin delimiter
     * @return the stripped CharSequence
     * @see #stripMargin(String, char)
     * @since 1.8.2
     */
    public static CharSequence stripMargin(CharSequence self, char marginChar) {
        return stripMargin(self.toString(), marginChar);
    }

    // TODO expose this for stream based stripping?
    private static String stripMarginFromLine(String line, char marginChar) {
        int length = line.length();
        int index = 0;
        while (index < length && line.charAt(index) <= ' ') index++;
        return (index < length && line.charAt(index) == marginChar) ? line.substring(index + 1) : line;
    }

    /**
     * <p>Strip leading spaces from every line in a String. The
     * line with the least number of leading spaces determines
     * the number to remove. Lines only containing whitespace are
     * ignored when calculating the number of leading spaces to strip.</p>
     * <pre class="groovyTestCase">
     * assert '  A\n B\nC' == '   A\n  B\n C'.stripIndent()
     * </pre>
     *
     * @param self     The String to strip the leading spaces from
     * @return the stripped String
     * @see #stripIndent(String, int)
     * @since 1.7.3
     */
    public static String stripIndent(String self) {
        if (self.length() == 0) return self;
        int runningCount = -1;
        try {
            for (String line : readLines(self)) {
                // don't take blank lines into account for calculating the indent
                if (isAllWhitespace(line)) continue;
                if (runningCount == -1) runningCount = line.length();
                runningCount = findMinimumLeadingSpaces(line, runningCount);
                if (runningCount == 0) break;
            }
        } catch (IOException e) {
            /* ignore */
        }
        return stripIndent(self, runningCount == -1 ? 0 : runningCount);
    }

    /**
     * <p>Strip leading spaces from every line in a CharSequence. The
     * line with the least number of leading spaces determines
     * the number to remove. Lines only containing whitespace are
     * ignored when calculating the number of leading spaces to strip.</p>
     *
     * @param self     The CharSequence to strip the leading spaces from
     * @return the stripped CharSequence
     * @see #stripIndent(String)
     * @since 1.8.2
     */
    public static CharSequence stripIndent(CharSequence self) {
        return stripIndent(self.toString());
    }

    /**
     * True if a String only contains whitespace characters.
     *
     * @param self The String to check the characters in
     * @return true If all characters are whitespace characters
     * @see Character#isWhitespace(char)
     * @since 1.6
     */
    public static boolean isAllWhitespace(String self) {
        for (int i = 0; i < self.length(); i++) {
            if (!Character.isWhitespace(self.charAt(i)))
                return false;
        }
        return true;
    }

    /**
     * True if a CharSequence only contains whitespace characters.
     *
     * @param self The CharSequence to check the characters in
     * @return true If all characters are whitespace characters
     * @see #isAllWhitespace(String)
     * @since 1.8.2
     */
    public static boolean isAllWhitespace(CharSequence self) {
        return isAllWhitespace(self.toString());
    }

    // TODO expose this for stream based scenarios?
    private static int findMinimumLeadingSpaces(String line, int count) {
        int length = line.length();
        int index = 0;
        while (index < length && index < count && Character.isWhitespace(line.charAt(index))) index++;
        return index;
    }

    /**
     * <p>Strip <tt>numChar</tt> leading characters from
     * every line in a String.</p>
     * <pre class="groovyTestCase">
     * assert 'DEF\n456' == '''ABCDEF\n123456'''.stripIndent(3)
     * </pre>
     *
     * @param self     The String to strip the characters from
     * @param numChars The number of characters to strip
     * @return the stripped String
     * @since 1.7.3
     */
    public static String stripIndent(String self, int numChars) {
        if (self.length() == 0 || numChars <= 0) return self;
        try {
            StringBuilder builder = new StringBuilder();
            for (String line : readLines(self)) {
                // normalize an empty or whitespace line to \n
                // or strip the indent for lines containing non-space characters
                if (!isAllWhitespace(line)) {
                    builder.append(stripIndentFromLine(line, numChars));
                }
                builder.append("\n");
            }
            // remove the normalized ending line ending if it was not present
            if (!self.endsWith("\n")) {
                builder.deleteCharAt(builder.length() - 1);
            }
            return builder.toString();
        } catch (IOException e) {
            /* ignore */
        }
        return self;
    }

    /**
     * <p>Strip <tt>numChar</tt> leading characters from
     * every line in a CharSequence.</p>
     *
     * @param self     The CharSequence to strip the characters from
     * @param numChars The number of characters to strip
     * @return the stripped CharSequence
     * @since 1.8.2
     */
    public static CharSequence stripIndent(CharSequence self, int numChars) {
        return stripIndent(self);
    }

    // TODO expose this for stream based stripping?
    private static String stripIndentFromLine(String line, int numChars) {
        int length = line.length();
        return numChars <= length ? line.substring(numChars) : "";
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
     * Transforms a CharSequence representing a URL into a URL object.
     *
     * @param self the CharSequence representing a URL
     * @return a URL
     * @throws MalformedURLException is thrown if the URL is not well formed.
     * @since 1.8.2
     */
    public static URL toURL(CharSequence self) throws MalformedURLException {
        return new URL(self.toString());
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
     * Transforms a CharSequence representing a URI into a URI object.
     *
     * @param self the CharSequence representing a URI
     * @return a URI
     * @throws URISyntaxException is thrown if the URI is not well formed.
     * @since 1.8.2
     */
    public static URI toURI(CharSequence self) throws URISyntaxException {
        return new URI(self.toString());
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
     * Turns a CharSequence into a regular expression Pattern
     *
     * @param self a String to convert into a regular expression
     * @return the regular expression pattern
     * @since 1.8.2
     */
    public static Pattern bitwiseNegate(CharSequence self) {
        return Pattern.compile(self.toString());
    }

    /**
     * Replaces the first substring of a String that matches the given
     * compiled regular expression with the given replacement.
     * <p/>
     * Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in the
     * replacement string may cause the results to be different than if it were
     * being treated as a literal replacement string; see
     * {@link java.util.regex.Matcher#replaceFirst}.
     * Use {@link java.util.regex.Matcher#quoteReplacement} to suppress the special
     * meaning of these characters, if desired.
     * <p/>
     * <pre class="groovyTestCase">
     * assert "foo".replaceFirst('o', 'X') == 'fXo'
     * </pre>
     *
     * @param   self the string that is to be matched
     * @param   pattern the regex Pattern to which the string of interest is to be matched
     * @param   replacement the string to be substituted for the first match
     * @return  The resulting <tt>String</tt>
     * @see java.lang.String#replaceFirst(java.lang.String, java.lang.String)
     * @since 1.6.1
     */
    public static String replaceFirst(String self, Pattern pattern, String replacement) {
        return pattern.matcher(self).replaceFirst(replacement);
    }

    /**
     * Replaces the first substring of a CharSequence that matches the given
     * compiled regular expression with the given replacement.
     *
     * @param   self the CharSequence that is to be matched
     * @param   pattern the regex Pattern to which the CharSequence of interest is to be matched
     * @param   replacement the CharSequence to be substituted for the first match
     * @return  The resulting <tt>CharSequence</tt>
     * @see #replaceFirst(String, Pattern, String)
     * @since 1.8.2
     */
    public static CharSequence replaceFirst(CharSequence self, Pattern pattern, CharSequence replacement) {
        return pattern.matcher(self).replaceFirst(replacement.toString());
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
     * <p/>
     * <pre class="groovyTestCase">
     * assert "foo".replaceAll('o', 'X') == 'fXX'
     * </pre>
     *
     * @param   self the string that is to be matched
     * @param   pattern the regex Pattern to which the string of interest is to be matched
     * @param   replacement the string to be substituted for the first match
     * @return  The resulting <tt>String</tt>
     * @see java.lang.String#replaceAll(java.lang.String, java.lang.String)
     * @since 1.6.1
     */
    public static String replaceAll(String self, Pattern pattern, String replacement) {
        return pattern.matcher(self).replaceAll(replacement);
    }

    /**
     * Replaces all substrings of a CharSequence that match the given
     * compiled regular expression with the given replacement.
     *
     * @param   self the CharSequence that is to be matched
     * @param   pattern the regex Pattern to which the CharSequence of interest is to be matched
     * @param   replacement the CharSequence to be substituted for the first match
     * @return  The resulting <tt>CharSequence</tt>
     * @see #replaceAll(String, Pattern, String)
     * @since 1.8.2
     */
    public static CharSequence replaceAll(CharSequence self, Pattern pattern, CharSequence replacement) {
        return pattern.matcher(self).replaceAll(replacement.toString());
    }

    /**
     * Translates a string by replacing characters from the sourceSet with characters from replacementSet.
     * If the first character from sourceSet appears in the string, it will be replaced with the first character from replacementSet.
     * If the second character from sourceSet appears in the string, it will be replaced with the second character from replacementSet.
     * and so on for all provided replacement characters.
     * <p/>
     * Here is an example which converts the vowels in a word from lower to uppercase:
     * <pre>
     * assert 'hello'.tr('aeiou', 'AEIOU') == 'hEllO'
     * </pre>
     * A character range using regex-style syntax can also be used, e.g. here is an example which converts a word from lower to uppercase:
     * <pre>
     * assert 'hello'.tr('a-z', 'A-Z') == 'HELLO'
     * </pre>
     * Hyphens at the start or end of sourceSet or replacementSet are treated as normal hyphens and are not
     * considered to be part of a range specification. Similarly, a hyphen immediately after an earlier range
     * is treated as a normal hyphen. So, '-x', 'x-' have no ranges while 'a-c-e' has the range 'a-c' plus
     * the '-' character plus the 'e' character.
     * <p/>
     * Unlike the unix tr command, Groovy's tr command supports reverse ranges, e.g.:
     * <pre>
     * assert 'hello'.tr('z-a', 'Z-A') == 'HELLO'
     * </pre>
     * If replacementSet is smaller than sourceSet, then the last character from replacementSet is used as the replacement for all remaining source characters as shown here:
     * <pre>
     * assert 'Hello World!'.tr('a-z', 'A') == 'HAAAA WAAAA!'
     * </pre>
     * If sourceSet contains repeated characters, the last specified replacement is used as shown here:
     * <pre>
     * assert 'Hello World!'.tr('lloo', '1234') == 'He224 W4r2d!'
     * </pre>
     * The functionality provided by tr can be achieved using regular expressions but tr provides a much more compact
     * notation and efficient implementation for certain scenarios.
     *
     * @param   self the string that is to be translated
     * @param   sourceSet the set of characters to translate from
     * @param   replacementSet the set of replacement characters
     * @return  The resulting translated <tt>String</tt>
     * @see org.codehaus.groovy.util.StringUtil#tr(String, String, String)
     * @since 1.7.3
     */
    public static String tr(final String self, String sourceSet, String replacementSet) throws ClassNotFoundException {
        return (String) InvokerHelper.invokeStaticMethod("org.codehaus.groovy.util.StringUtil", "tr", new Object[]{self, sourceSet, replacementSet});
    }

    /**
     * Translates a string by replacing characters from the sourceSet with characters from replacementSet.
     *
     * @param   self the CharSequence that is to be translated
     * @param   sourceSet the set of characters to translate from
     * @param   replacementSet the set of replacement characters
     * @return  The resulting translated <tt>CharSequence</tt>
     * @see #tr(String, String, String)
     * @since 1.8.2
     */
    public static CharSequence tr(final CharSequence self, CharSequence sourceSet, CharSequence replacementSet) throws ClassNotFoundException {
        return tr(self.toString(), sourceSet.toString(), replacementSet.toString());
    }

    /**
     * Tells whether or not self matches the given
     * compiled regular expression Pattern.
     *
     * @param   self the string that is to be matched
     * @param   pattern the regex Pattern to which the string of interest is to be matched
     * @return  true if the string matches
     * @see java.lang.String#matches(java.lang.String)
     * @since 1.6.1
     */
    public static boolean matches(String self, Pattern pattern) {
        return pattern.matcher(self).matches();
    }

    /**
     * Tells whether or not a CharSequence matches the given
     * compiled regular expression Pattern.
     *
     * @param   self the CharSequence that is to be matched
     * @param   pattern the regex Pattern to which the string of interest is to be matched
     * @return  true if the CharSequence matches
     * @see java.lang.String#matches(java.lang.String)
     * @since 1.8.2
     */
    public static boolean matches(CharSequence self, Pattern pattern) {
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
     * Finds the first occurrence of a regular expression CharSequence within a CharSequence.
     *
     * @param self  a CharSequence
     * @param regex the capturing regex
     * @return a CharSequence containing the matched portion, or null if the regex doesn't match
     * @see #find(String, Pattern)
     * @since 1.8.2
     */
    public static CharSequence find(CharSequence self, CharSequence regex) {
        return find(self.toString(), Pattern.compile(regex.toString()));
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
     * Finds the first occurrence of a compiled regular expression Pattern within a CharSequence.
     *
     * @param self    a CharSequence
     * @param pattern the compiled regex Pattern
     * @return a CharSequence containing the matched portion, or null if the regex pattern doesn't match
     * @see #find(String, Pattern)
     * @since 1.8.2
     */
    public static CharSequence find(CharSequence self, Pattern pattern) {
        return find(self.toString(), pattern);
    }

    /**
     * Returns the result of calling a closure with the first occurrence of a regular expression found within a String.
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
     * Returns the result of calling a closure with the first occurrence of a regular expression found within a CharSequence.
     * If the regex doesn't match, the closure will not be called and find will return null.
     *
     * @param self    a CharSequence
     * @param regex   the capturing regex CharSequence
     * @param closure the closure that will be passed the full match, plus each of the capturing groups
     * @return a CharSequence containing the result of the closure, or null if the regex pattern doesn't match
     * @see #find(String, Pattern, Closure)
     * @since 1.8.2
     */
    public static CharSequence find(CharSequence self, CharSequence regex, Closure closure) {
        return find(self.toString(), Pattern.compile(regex.toString()), closure);
    }

    /**
     * Returns the result of calling a closure with the first occurrence of a compiled regular expression found within a String.
     * If the regex doesn't match, the closure will not be called and find will return null.
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

    /*
     * Returns the result of calling a closure with the first occurrence of a regular expression found within a
     * CharSequence.&nbsp;If the regex doesn't match, the closure will not be called and find will return null.
     *
     * @param self    a CharSequence
     * @param pattern the compiled regex Pattern
     * @param closure the closure that will be passed the full match, plus each of the capturing groups
     * @return a CharSequence containing the result of the closure, or null if the regex pattern doesn't match
     * @see #find(String, java.util.regex.Pattern, groovy.lang.Closure)
     * @since 1.8.2
     */
    public static CharSequence find(CharSequence self, Pattern pattern, Closure closure) {
        return find(self.toString(), pattern, closure);
    }

    /**
     * Returns a (possibly empty) list of all occurrences of a regular expression (in String format) found within a String.
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
    public static List<String> findAll(String self, String regex) {
        return findAll(self, Pattern.compile(regex));
    }

    /**
     * Returns a (possibly empty) list of all occurrences of a regular expression (in CharSequence format) found within a CharSequence.
     *
     * @param self  a CharSequence
     * @param regex the capturing regex CharSequence
     * @return a List containing all full matches of the regex within the CharSequence, an empty list will be returned if there are no matches
     * @see #findAll(String, String)
     * @since 1.8.2
     */
    public static List<CharSequence> findAll(CharSequence self, CharSequence regex) {
        return new ArrayList<CharSequence>(findAll(self.toString(), regex.toString()));
    }

    /**
     * Returns a (possibly empty) list of all occurrences of a regular expression (in Pattern format) found within a String.
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
    public static List<String> findAll(String self, Pattern pattern) {
        Matcher matcher = pattern.matcher(self);
        List<String> list = new ArrayList<String>();
        for (Iterator iter = iterator(matcher); iter.hasNext();) {
            if (hasGroup(matcher)) {
                list.add((String) ((List) iter.next()).get(0));
            } else {
                list.add((String) iter.next());
            }
        }
        return list;
    }

    /**
     * Returns a (possibly empty) list of all occurrences of a regular expression (in Pattern format) found within a CharSequence.
     *
     * @param self    a CharSequence
     * @param pattern the compiled regex Pattern
     * @return a List containing all full matches of the Pattern within the CharSequence, an empty list will be returned if there are no matches
     * @see #findAll(String, Pattern)
     * @since 1.8.2
     */
    public static List<CharSequence> findAll(CharSequence self, Pattern pattern) {
        return new ArrayList<CharSequence>(findAll(self.toString(), pattern));
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
    public static <T> List<T> findAll(String self, String regex, Closure<T> closure) {
        return findAll(self, Pattern.compile(regex), closure);
    }

    /**
     * Finds all occurrences of a capturing regular expression CharSequence within a CharSequence.
     *
     * @param self    a CharSequence
     * @param regex   the capturing regex CharSequence
     * @param closure will be passed the full match plus each of the capturing groups
     * @return a List containing all full matches of the regex within the CharSequence, an empty list will be returned if there are no matches
     * @see #findAll(String, String, Closure)
     * @since 1.8.2
     */
    public static <T> List<T> findAll(CharSequence self, CharSequence regex, Closure<T> closure) {
        return findAll(self.toString(), regex.toString(), closure);
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
    public static <T> List<T> findAll(String self, Pattern pattern, Closure<T> closure) {
        Matcher matcher = pattern.matcher(self);
        return collect(matcher, closure);
    }

    /**
     * Finds all occurrences of a compiled regular expression Pattern within a CharSequence.
     *
     * @param self    a CharSequence
     * @param pattern the compiled regex Pattern
     * @param closure will be passed the full match plus each of the capturing groups
     * @return a List containing all full matches of the regex Pattern within the CharSequence, an empty list will be returned if there are no matches
     * @see #findAll(String, Pattern, Closure)
     * @since 1.8.2
     */
    public static <T> List<T> findAll(CharSequence self, Pattern pattern, Closure<T> closure) {
        return findAll(self.toString(), pattern, closure);
    }

    /**
     * Replaces all occurrences of a captured group by the result of a closure on that text.
     * <p/>
     * <p> For examples,
     * <pre>
     *     assert "hellO wOrld" == "hello world".replaceAll("(o)") { it[0].toUpperCase() }
     * <p/>
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
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @since 1.0
     * @see java.util.regex.Matcher#quoteReplacement(java.lang.String)
     * @see #replaceAll(String, Pattern, Closure)
     */
    public static String replaceAll(final String self, final String regex, final Closure closure) {
        return replaceAll(self, Pattern.compile(regex), closure);
    }

    /**
     * Replaces all occurrences of a captured group by the result of a closure on that text.
     *
     * @param self    a CharSequence
     * @param regex   the capturing regex
     * @param closure the closure to apply on each captured group
     * @return a CharSequence with replaced content
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @since 1.8.2
     * @see #replaceAll(String, Pattern, Closure)
     */
    public static CharSequence replaceAll(final CharSequence self, final CharSequence regex, final Closure closure) {
        return replaceAll(self.toString(), Pattern.compile(regex.toString()), closure);
    }

    /**
     * Replaces each substring of this CharSequence that matches the given
     * regular expression with the given replacement.
     *
     * @param self        a CharSequence
     * @param regex       the capturing regex
     * @param replacement the capturing regex
     * @return a CharSequence with replaced content
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @see String#replaceAll(String, String)
     * @since 1.8.2
     */
    public static CharSequence replaceAll(final CharSequence self, final CharSequence regex, final CharSequence replacement) {
        return self.toString().replaceAll(regex.toString(), replacement.toString());
    }

    /**
     * Replaces the first occurrence of a captured group by the result of a closure call on that text.
     * <p/>
     * <p> For example (with some replaceAll variants thrown in for comparison purposes),
     * <pre>
     * assert "hellO world" == "hello world".replaceFirst("(o)") { it[0].toUpperCase() } // first match
     * assert "hellO wOrld" == "hello world".replaceAll("(o)") { it[0].toUpperCase() }   // all matches
     * <p/>
     * assert '1-FISH, two fish' == "one fish, two fish".replaceFirst(/([a-z]{3})\s([a-z]{4})/) { [one:1, two:2][it[1]] + '-' + it[2].toUpperCase() }
     * assert '1-FISH, 2-FISH' == "one fish, two fish".replaceAll(/([a-z]{3})\s([a-z]{4})/) { [one:1, two:2][it[1]] + '-' + it[2].toUpperCase() }
     * </pre>
     *
     * @param self    a String
     * @param regex   the capturing regex
     * @param closure the closure to apply on the first captured group
     * @return a String with replaced content
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @since 1.7.7
     * @see java.util.regex.Matcher#quoteReplacement(java.lang.String)
     * @see #replaceFirst(String, Pattern, Closure)
     */
    public static String replaceFirst(final String self, final String regex, final Closure closure) {
        return replaceFirst(self, Pattern.compile(regex), closure);
    }

    /**
     * Replaces the first substring of this CharSequence that matches the given
     * regular expression with the given replacement.
     *
     * @param self        a CharSequence
     * @param regex       the capturing regex
     * @param replacement the capturing regex
     * @return a CharSequence with replaced content
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @see String#replaceAll(String, String)
     * @since 1.8.2
     */
    public static String replaceFirst(final CharSequence self, final CharSequence regex, final CharSequence replacement) {
        return self.toString().replaceFirst(regex.toString(), replacement.toString());
    }

    /**
     * Replaces the first occurrence of a captured group by the result of a closure call on that text.
     *
     * @param self    a CharSequence
     * @param regex   the capturing regex
     * @param closure the closure to apply on the first captured group
     * @return a CharSequence with replaced content
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @see #replaceFirst(String, String, Closure)
     * @since 1.8.2
     */
    public static String replaceFirst(final CharSequence self, final CharSequence regex, final Closure closure) {
        return replaceFirst(self.toString(), regex.toString(), closure);
    }

    /**
     * Get a replacement corresponding to the matched pattern for {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#replaceAll(String, Pattern, Closure)}.
     * The closure take parameter:
     * <ul>
     * <li>Whole of match if the pattern include no capturing group</li>
     * <li>Object[] of capturing groups if the closure takes Object[] as parameter</li>
     * <li>List of capturing groups</li>
     * </ul>
     *
     * @param    matcher the matcher object used for matching
     * @param    closure specified with replaceAll() to get replacement
     * @return   replacement correspond replacement for a match
     */
    private static String getReplacement(Matcher matcher, Closure closure) {
        if (!hasGroup(matcher)) {
            return InvokerHelper.toString(closure.call(matcher.group()));
        }

        int count = matcher.groupCount();
        List<String> groups = new ArrayList<String>();
        for (int i = 0; i <= count; i++) {
            groups.add(matcher.group(i));
        }

        if (closure.getParameterTypes().length == 1
            && closure.getParameterTypes()[0] == Object[].class) {
            return InvokerHelper.toString(closure.call(groups.toArray()));
        }
        return InvokerHelper.toString(closure.call(groups));
    }

    /**
     * Replaces all occurrences of a captured group by the result of a closure call on that text.
     * <p/>
     * <p> For examples,
     * <pre>
     *     assert "hellO wOrld" == "hello world".replaceAll(~"(o)") { it[0].toUpperCase() }
     * <p/>
     *     assert "FOOBAR-FOOBAR-" == "foobar-FooBar-".replaceAll(~"(([fF][oO]{2})[bB]ar)", { it[0].toUpperCase() })
     * <p/>
     *     Here,
     *          it[0] is the global string of the matched group
     *          it[1] is the first string in the matched group
     *          it[2] is the second string in the matched group
     * <p/>
     * <p/>
     *     assert "FOOBAR-FOOBAR-" == "foobar-FooBar-".replaceAll(~"(([fF][oO]{2})[bB]ar)", { Object[] it -> it[0].toUpperCase() })
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
     * @param pattern the capturing regex Pattern
     * @param closure the closure to apply on each captured group
     * @return a String with replaced content
     * @since 1.6.8
     * @see java.util.regex.Matcher#quoteReplacement(java.lang.String)
     */
    public static String replaceAll(final String self, final Pattern pattern, final Closure closure) {
        final Matcher matcher = pattern.matcher(self);
        if (matcher.find()) {
            final StringBuffer sb = new StringBuffer(self.length() + 16);
            do {
                String replacement = getReplacement(matcher, closure);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            } while (matcher.find());
            matcher.appendTail(sb);
            return sb.toString();
        } else {
            return self;
        }
    }

    /**
     * Replaces all occurrences of a captured group by the result of a closure call on that text.
     *
     * @param self    a CharSequence
     * @param pattern the capturing regex Pattern
     * @param closure the closure to apply on each captured group
     * @return a CharSequence with replaced content
     * @since 1.8.2
     * @see #replaceAll(String, Pattern, Closure)
     */
    public static String replaceAll(final CharSequence self, final Pattern pattern, final Closure closure) {
        return replaceAll(self.toString(), pattern, closure);
    }

    /**
     * Replaces the first occurrence of a captured group by the result of a closure call on that text.
     * <p/>
     * <p> For example (with some replaceAll variants thrown in for comparison purposes),
     * <pre>
     * assert "hellO world" == "hello world".replaceFirst(~"(o)") { it[0].toUpperCase() } // first match
     * assert "hellO wOrld" == "hello world".replaceAll(~"(o)") { it[0].toUpperCase() }   // all matches
     * <p/>
     * assert '1-FISH, two fish' == "one fish, two fish".replaceFirst(~/([a-z]{3})\s([a-z]{4})/) { [one:1, two:2][it[1]] + '-' + it[2].toUpperCase() }
     * assert '1-FISH, 2-FISH' == "one fish, two fish".replaceAll(~/([a-z]{3})\s([a-z]{4})/) { [one:1, two:2][it[1]] + '-' + it[2].toUpperCase() }
     * </pre>
     *
     * @param self    a String
     * @param pattern the capturing regex Pattern
     * @param closure the closure to apply on the first captured group
     * @return a String with replaced content
     * @since 1.7.7
     * @see #replaceAll(String, Pattern, Closure)
     */
    public static String replaceFirst(final String self, final Pattern pattern, final Closure closure) {
        final Matcher matcher = pattern.matcher(self);
        if (matcher.find()) {
            final StringBuffer sb = new StringBuffer(self.length() + 16);
            String replacement = getReplacement(matcher, closure);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            matcher.appendTail(sb);
            return sb.toString();
        } else {
            return self;
        }
    }

    /**
     * Replaces the first occurrence of a captured group by the result of a closure call on that text.
     *
     * @param self    a CharSequence
     * @param pattern the capturing regex Pattern
     * @param closure the closure to apply on the first captured group
     * @return a CharSequence with replaced content
     * @see #replaceFirst(String, Pattern, Closure)
     * @since 1.8.2
     */
    public static String replaceFirst(final CharSequence self, final Pattern pattern, final Closure closure) {
        return replaceFirst(self.toString(), pattern, closure);
    }

    private static String getPadding(String padding, int length) {
        if (padding.length() < length) {
            return multiply(padding, length / padding.length() + 1).substring(0, length);
        } else {
            return padding.substring(0, length);
        }
    }

    /**
     * Pad a String to a minimum length specified by <tt>numberOfChars</tt>, adding the supplied padding String as many times as needed to the left.
     *
     * If the String is already the same size or bigger than the target <tt>numberOfChars</tt>, then the original String is returned. An example:
     * <pre>
     * println 'Numbers:'
     * [1, 10, 100, 1000].each{ println it.toString().padLeft(5, '*') }
     * [2, 20, 200, 2000].each{ println it.toString().padLeft(5, '*_') }
     * </pre>
     * will produce output like:
     * <pre>
     * Numbers:
     * ****1
     * ***10
     * **100
     * *1000
     * *_*_2
     * *_*20
     * *_200
     * *2000
     * </pre>
     *
     * @param self          a String object
     * @param numberOfChars the total minimum number of characters of the resulting string
     * @param padding       the characters used for padding
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
     * Pad a CharSequence to a minimum length specified by <tt>numberOfChars</tt>, adding the supplied padding CharSequence as many times as needed to the left.
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the resulting CharSequence
     * @param padding       the characters used for padding
     * @return the CharSequence padded to the left
     * @see #padLeft(String, Number, String)
     * @since 1.8.2
     */
    public static CharSequence padLeft(CharSequence self, Number numberOfChars, CharSequence padding) {
        return padLeft(self.toString(), numberOfChars, padding.toString());
    }

    /**
     * Pad a String to a minimum length specified by <tt>numberOfChars</tt> by adding the space character to the left as many times as needed.
     *
     * If the String is already the same size or bigger than the target <tt>numberOfChars</tt>, then the original String is returned. An example:
     * <pre>
     * println 'Numbers:'
     * [1, 10, 100, 1000].each{ println it.toString().padLeft(5) }
     * </pre>
     * will produce output like:
     * <pre>
     * Numbers:
     *     1
     *    10
     *   100
     *  1000
     * </pre>
     *
     * @param self          a String object
     * @param numberOfChars the total minimum number of characters of the resulting string
     * @return the String padded to the left
     * @see #padLeft(String, Number, String)
     * @since 1.0
     */
    public static String padLeft(String self, Number numberOfChars) {
        return padLeft(self, numberOfChars, " ");
    }

    /**
     * Pad a CharSequence to a minimum length specified by <tt>numberOfChars</tt> by adding the space character to the left as many times as needed.
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the resulting CharSequence
     * @return the CharSequence padded to the left
     * @see #padLeft(CharSequence, Number, CharSequence)
     * @since 1.8.2
     */
    public static CharSequence padLeft(CharSequence self, Number numberOfChars) {
        return padLeft(self, numberOfChars, " ");
    }

    /**
     * Pad a String to a minimum length specified by <tt>numberOfChars</tt>, adding the supplied padding String as many times as needed to the right.
     *
     * If the String is already the same size or bigger than the target <tt>numberOfChars</tt>, then the original String is returned. An example:
     * <pre>
     * ['A', 'BB', 'CCC', 'DDDD'].each{ println it.padRight(5, '#') + it.size() }
     * </pre>
     * will produce output like:
     * <pre>
     * A####1
     * BB###2
     * CCC##3
     * DDDD#4
     * </pre>
     *
     * @param self          a String object
     * @param numberOfChars the total minimum number of characters of the resulting string
     * @param padding       the characters used for padding
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
     * Pad a CharSequence to a minimum length specified by <tt>numberOfChars</tt>, adding the supplied padding CharSequence as many times as needed to the right.
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the resulting CharSequence
     * @param padding       the characters used for padding
     * @return the CharSequence padded to the right
     * @see #padRight(String, Number, String)
     * @since 1.8.2
     */
    public static CharSequence padRight(CharSequence self, Number numberOfChars, CharSequence padding) {
        return padRight(self.toString(), numberOfChars, padding.toString());
    }

    /**
     * Pad a String to a minimum length specified by <tt>numberOfChars</tt> by adding the space character to the right as many times as needed.
     *
     * If the String is already the same size or bigger than the target <tt>numberOfChars</tt>, then the original String is returned. An example:
     * <pre>
     * ['A', 'BB', 'CCC', 'DDDD'].each{ println it.padRight(5) + it.size() }
     * </pre>
     * will produce output like:
     * <pre>
     * A    1
     * BB   2
     * CCC  3
     * DDDD 4
     * </pre>
     *
     * @param self          a String object
     * @param numberOfChars the total minimum number of characters of the resulting string
     * @return the String padded to the right
     * @since 1.0
     */
    public static String padRight(String self, Number numberOfChars) {
        return padRight(self, numberOfChars, " ");
    }

    /**
     * Pad a CharSequence to a minimum length specified by <tt>numberOfChars</tt> by adding the space character to the right as many times as needed.
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the resulting string
     * @return the CharSequence padded to the right
     * @see #padRight(String, Number)
     * @since 1.8.2
     */
    public static CharSequence padRight(CharSequence self, Number numberOfChars) {
        return padRight(self.toString(), numberOfChars);
    }

    /**
     * Pad a String to a minimum length specified by <tt>numberOfChars</tt>, appending the supplied padding String around the original as many times as needed keeping it centered.
     *
     * If the String is already the same size or bigger than the target <tt>numberOfChars</tt>, then the original String is returned. An example:
     * <pre>
     * ['A', 'BB', 'CCC', 'DDDD'].each{ println '|' + it.center(6, '+') + '|' }
     * </pre>
     * will produce output like:
     * <pre>
     * |++A+++|
     * |++BB++|
     * |+CCC++|
     * |+DDDD+|
     * </pre>
     *
     * @param self          a String object
     * @param numberOfChars the total minimum number of characters of the resulting string
     * @param padding       the characters used for padding
     * @return the String centered with padded characters around it
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
     * Pad a CharSequence to a minimum length specified by <tt>numberOfChars</tt>, appending the supplied padding CharSequence around the original as many times as needed keeping it centered.
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the resulting CharSequence
     * @param padding       the characters used for padding
     * @return the CharSequence centered with padded characters around it
     * @see #center(String, Number, String)
     * @since 1.8.2
     */
    public static CharSequence center(CharSequence self, Number numberOfChars, CharSequence padding) {
        return center(self.toString(), numberOfChars, padding.toString());
    }

    /**
     * Pad a String to a minimum length specified by <tt>numberOfChars</tt> by adding the space character around it as many times as needed so that it remains centered.
     *
     * If the String is already the same size or bigger than the target <tt>numberOfChars</tt>, then the original String is returned. An example:
     * <pre>
     * ['A', 'BB', 'CCC', 'DDDD'].each{ println '|' + it.center(6) + '|' }
     * </pre>
     * will produce output like:
     * <pre>
     * |  A   |
     * |  BB  |
     * | CCC  |
     * | DDDD |
     * </pre>
     *
     * @param self          a String object
     * @param numberOfChars the total minimum number of characters of the resulting string
     * @return the String centered with padded characters around it
     * @see #center(String, Number, String)
     * @since 1.0
     */
    public static String center(String self, Number numberOfChars) {
        return center(self, numberOfChars, " ");
    }

    /**
     * Pad a CharSequence to a minimum length specified by <tt>numberOfChars</tt> by adding the space character around it as many times as needed so that it remains centered.
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the resulting CharSequence
     * @return the CharSequence centered with padded characters around it
     * @see #center(String, Number)
     * @since 1.8.2
     */
    public static CharSequence center(CharSequence self, Number numberOfChars) {
        return center(self.toString(), numberOfChars);
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
     * Support the range subscript operator for a List.
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
     * Support the range subscript operator for a List.
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
     * Returns the longest prefix of this list where each element
     * passed to the given closure condition evaluates to true.
     * Similar to {@link #takeWhile(Iterable, groovy.lang.Closure)}
     * except that it attempts to preserve the type of the original list.
     * <pre class="groovyTestCase">
     * def nums = [ 1, 3, 2 ]
     * assert nums.takeWhile{ it < 1 } == []
     * assert nums.takeWhile{ it < 3 } == [ 1 ]
     * assert nums.takeWhile{ it < 4 } == [ 1, 3, 2 ]
     * </pre>
     *
     * @param self      the original list
     * @param condition the closure that must evaluate to true to
     *                  continue taking elements
     * @return a prefix of the given list where each element passed to
     *         the given closure evaluates to true
     * @since 1.8.7
     */
    public static <T> List<T> takeWhile(List<T> self, Closure condition) {
        int num = 0;
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(condition);
        for (T value : self) {
            if (bcw.call(value)) {
                num += 1;
            } else {
                break;
            }
        }
        return take(self, num);
    }

    /**
     * Returns a List containing the longest prefix of the elements from this Iterable
     * where each element passed to the given closure evaluates to true.
     * <pre class="groovyTestCase">
     * class AbcIterable implements Iterable<String> {
     *     Iterator<String> iterator() { "abc".iterator() }
     * }
     * def abc = new AbcIterable()
     * assert abc.takeWhile{ it < 'b' } == ['a']
     * assert abc.takeWhile{ it <= 'b' } == ['a', 'b']
     * </pre>
     *
     * @param self      an Iterable
     * @param condition the closure that must evaluate to true to
     *                  continue taking elements
     * @return a List containing a prefix of the elements from the given Iterable where
     *         each element passed to the given closure evaluates to true
     * @since 1.8.7
     */
    public static <T> List<T> takeWhile(Iterable<T> self, Closure condition) {
        return toList(takeWhile(self.iterator(), condition));
    }

    /**
     * Returns the longest prefix of this Map where each entry (or key/value pair) when
     * passed to the given closure evaluates to true.
     * <pre class="groovyTestCase">
     * def shopping = [milk:1, bread:2, chocolate:3]
     * assert shopping.takeWhile{ it.key.size() < 6 } == [milk:1, bread:2]
     * assert shopping.takeWhile{ it.value % 2 } == [milk:1]
     * assert shopping.takeWhile{ k, v -> k.size() + v <= 7 } == [milk:1, bread:2]
     * </pre>
     * If the map instance does not have ordered keys, then this function could appear to take random
     * entries. Groovy by default uses LinkedHashMap, so this shouldn't be an issue in the main.
     *
     * @param self      a Map
     * @param condition a 1 (or 2) arg Closure that must evaluate to true for the
     *                  entry (or key and value) to continue taking elements
     * @return a prefix of the given Map where each entry (or key/value pair) passed to
     *         the given closure evaluates to true
     * @since 1.8.7
     */
    public static <K, V> Map<K, V> takeWhile(Map<K, V> self, Closure<?> condition) {
        if (self.isEmpty()) {
            return createSimilarMap(self);
        }
        Map<K, V> ret = createSimilarMap(self);
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(condition);
        for (Map.Entry<K, V> entry : self.entrySet()) {
            if (!bcw.callForMap(entry)) break;
            ret.put(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    /**
     * Returns the longest prefix of this array where each element
     * passed to the given closure evaluates to true.
     * <pre class="groovyTestCase">
     * def nums = [ 1, 3, 2 ] as Integer[]
     * assert nums.takeWhile{ it < 1 } == [] as Integer[]
     * assert nums.takeWhile{ it < 3 } == [ 1 ] as Integer[]
     * assert nums.takeWhile{ it < 4 } == [ 1, 3, 2 ] as Integer[]
     * </pre>
     *
     * @param self      the original array
     * @param condition the closure that must evaluate to true to
     *                  continue taking elements
     * @return a prefix of the given array where each element passed to
     *         the given closure evaluates to true
     * @since 1.8.7
     */
    public static <T> T[] takeWhile(T[] self, Closure condition) {
        int num = 0;
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(condition);
        while (num < self.length) {
            T value = self[num];
            if (bcw.call(value)) {
                num += 1;
            } else {
                break;
            }
        }
        return take(self, num);
    }

    /**
     * Returns the longest prefix of elements in this iterator where
     * each element passed to the given condition closure evaluates to true.
     * <p/>
     * <pre class="groovyTestCase">
     * def a = 0
     * def iter = [ hasNext:{ true }, next:{ a++ } ] as Iterator
     *
     * assert [].iterator().takeWhile{ it < 3 }.toList() == []
     * assert [1, 2, 3, 4, 5].iterator().takeWhile{ it < 3 }.toList() == [ 1, 2 ]
     * assert iter.takeWhile{ it < 5 }.toList() == [ 0, 1, 2, 3, 4 ]
     * </pre>
     *
     * @param self      the Iterator
     * @param condition the closure that must evaluate to true to
     *                  continue taking elements
     * @return a prefix of elements in the given iterator where each
     *         element passed to the given closure evaluates to true
     * @since 1.8.7
     */
    public static <T> Iterator<T> takeWhile(Iterator<T> self, Closure condition) {
        return new TakeWhileIterator<T>(self, condition);
    }

    private static class TakeWhileIterator<E> implements Iterator<E> {
        private final Iterator<E> delegate;
        private final BooleanClosureWrapper condition;
        private boolean exhausted;
        private E next;

        private TakeWhileIterator(Iterator<E> delegate, Closure condition) {
            this.delegate = delegate;
            this.condition = new BooleanClosureWrapper(condition);
            advance();
        }

        public boolean hasNext() {
            return !exhausted;
        }

        public E next() {
            if (exhausted) throw new NoSuchElementException();
            E result = next;
            advance();
            return result;
        }

        public void remove() {
            if (exhausted) throw new NoSuchElementException();
            delegate.remove();
        }

        private void advance() {
            exhausted = !delegate.hasNext();
            if (!exhausted) {
                next = delegate.next();
                if (!condition.call(next)) {
                    exhausted = true;
                    next = null;
                }
            }
        }
    }

    /**
     * Returns the longest prefix of this CharSequence where each
     * element passed to the given closure evalutes to true.
     *
     * <pre class="groovyTestCase">
     *     def text = "Groovy"
     *     assert text.takeWhile{ it &lt; 'A' } == ''
     *     assert text.takeWhile{ it &lt; 'Z' } == 'G'
     *     assert text.takeWhile{ it &lt; 'z'  } == 'Groovy'
     * </pre>
     *
     * @param self the original CharSequence
     * @param num the number of chars to take from this CharSequence
     * @return a CharSequence consisting of the first <code>num</code> chars,
     *         or else the whole CharSequence if it has less then <code>num</code> elements.
     * @since 1.8.1
     */
    public static CharSequence takeWhile(CharSequence self, Closure closure) {
        int num = 0;
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        while (num < self.length()) {
            char value = self.charAt(num);
            if (bcw.call(value)) {
                num += 1;
            } else {
                break;
            }
        }
        return take(self, num);
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
     * Support the subscript operator for an Iterator. The iterator
     * will be partially exhausted up until the idx entry after returning
     * if a +ve or 0 idx is used, or fully exhausted if a -ve idx is used
     * or no corresponding entry was found. Typical usage:
     * <pre class="groovyTestCase">
     * def iter = [2, "a", 5.3].iterator()
     * assert iter[1] == "a"
     * </pre>
     * A more elaborate example:
     * <pre class="groovyTestCase">
     * def items = [2, "a", 5.3]
     * def iter = items.iterator()
     * assert iter[-1] == 5.3
     * // iter exhausted, so reset
     * iter = items.iterator()
     * assert iter[1] == "a"
     * // iter partially exhausted so now idx starts after "a"
     * assert iter[0] == 5.3
     * </pre>
     *
     * @param self an Iterator
     * @param idx  an index value (-self.size() <= idx < self.size())
     * @return the value at the given index (after normalisation) or null if no corresponding value was found
     * @since 1.7.2
     */
    public static <T> T getAt(Iterator<T> self, int idx) {
        if (idx < 0) {
            // calculate whole list in this case
            // recommend avoiding -ve's as this is not as efficient
            List<T> list = toList(self);
            int adjustedIndex = idx + list.size();
            if (adjustedIndex < 0 || adjustedIndex >= list.size()) return null;
            return list.get(adjustedIndex);
        }

        int count = 0;
        while (self.hasNext()) {
            if (count == idx) {
                return self.next();
            } else {
                count++;
                self.next();
            }
        }

        return null;
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
     * @see #putAt(java.util.List, groovy.lang.EmptyRange, java.lang.Object)
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
     * range are replaced with items from the collection.
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
     * range are replaced with the operand.  The <code>value</code> operand is
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
        Object first = splice.get(0);
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
     * A helper method to allow maps to work with subscript operators
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
     * Support the subscript operator for Collection.
     * <pre class="groovyTestCase">assert [String, Long, Integer] == ["a",5L,2]["class"]</pre>
     *
     * @param coll     a Collection
     * @param property a String
     * @return a List
     * @since 1.0
     */
    public static List getAt(Collection coll, String property) {
        List<Object> answer = new ArrayList<Object>(coll.size());
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
     * Create an array as a union of two arrays.
     * <pre class="groovyTestCase">
     * Integer[] a = [1, 2, 3]
     * Integer[] b = [4, 5, 6]
     * assert a + b == [1, 2, 3, 4, 5, 6] as Integer[]
     * </pre>
     *
     * @param left  the left Array
     * @param right the right Array
     * @return A new array containing right appended to left.
     * @since 1.8.7
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] plus(T[] left, T[] right) {
        return (T[]) plus(toList(left), toList(right)).toArray();
    }

    /**
     * Create an array containing elements from an original array plus an additional appended element.
     * <pre class="groovyTestCase">
     * Integer[] a = [1, 2, 3]
     * Integer[] result = a + 4
     * assert result == [1, 2, 3, 4] as Integer[]
     * </pre>
     *
     * @param left  the array
     * @param right the value to append
     * @return A new array containing left with right appended to it.
     * @since 1.8.7
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] plus(T[] left, T right) {
        return (T[]) plus(toList(left), right).toArray();
    }

    /**
     * Create an array containing elements from an original array plus those from a Collection.
     * <pre class="groovyTestCase">
     * Integer[] a = [1, 2, 3]
     * def additions = [7, 8]
     * assert a + additions == [1, 2, 3, 7, 8] as Integer[]
     * </pre>
     *
     * @param left  the array
     * @param right a Collection to be appended
     * @return A new array containing left with right appended to it.
     * @since 1.8.7
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] plus(T[] left, Collection<T> right) {
        return (T[]) plus(toList(left), right).toArray();
    }

    /**
     * Create an array containing elements from an original array plus those from an Iterable.
     * <pre class="groovyTestCase">
     * class AbcIterable implements Iterable<String> {
     *     Iterator<String> iterator() { "abc".iterator() }
     * }
     * String[] letters = ['x', 'y', 'z']
     * def result = letters + new AbcIterable()
     * assert result == ['x', 'y', 'z', 'a', 'b', 'c'] as String[]
     * assert result.class.array
     * </pre>
     *
     * @param left  the array
     * @param right an Iterable to be appended
     * @return A new array containing elements from left with those from right appended.
     * @since 1.8.7
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] plus(T[] left, Iterable<T> right) {
        return (T[]) plus(toList(left), toList(right)).toArray();
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
     * Create a Collection as a union of a Collection and an Iterable. If the left collection
     * is a Set, then the returned collection will be a Set otherwise a List.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     *
     * @param left  the left Collection
     * @param right the right Iterable
     * @return the merged Collection
     * @since 1.8.7
     * @see #plus(Collection, Collection)
     */
    public static <T> Collection<T> plus(Collection<T> left, Iterable<T> right) {
        return plus(left, toList(right));
    }

    /**
     * Creates a new List by inserting all of the elements in the specified array
     * to the elements from the original List at the specified index.
     * Shifts the element currently at that index (if any) and any subsequent
     * elements to the right (increasing their indices).
     * The new elements will appear in the resulting List in the order that
     * they occur in the original array.
     * The behavior of this operation is undefined if the list or
     * array operands are modified while the operation is in progress.
     * The original list and array operands remain unchanged.
     *
     * <pre class="groovyTestCase">
     * def items = [1, 2, 3]
     * def newItems = items.plus(2, 'a'..'c' as String[])
     * assert newItems == [1, 2, 'a', 'b', 'c', 3]
     * assert items == [1, 2, 3]
     * </pre>
     *
     * See also <code>addAll</code> for similar functionality with modify semantics, i.e. which performs
     * the changes on the original list itself.
     *
     * @param self  an original list
     * @param items array containing elements to be merged with elements from the original list
     * @param index index at which to insert the first element from the specified array
     * @return the new list
     * @see #plus(List, int, List)
     * @since 1.8.1
     */
    public static <T> List<T> plus(List<T> self, int index, T[] items) {
        return plus(self, index, Arrays.asList(items));
    }

    /**
     * Creates a new List by inserting all of the elements in the given additions List
     * to the elements from the original List at the specified index.
     * Shifts the element currently at that index (if any) and any subsequent
     * elements to the right (increasing their indices).  The new elements
     * will appear in the resulting List in the order that they occur in the original lists.
     * The behavior of this operation is undefined if the original lists
     * are modified while the operation is in progress. The original lists remain unchanged.
     *
     * <pre class="groovyTestCase">
     * def items = [1, 2, 3]
     * def newItems = items.plus(2, 'a'..'c')
     * assert newItems == [1, 2, 'a', 'b', 'c', 3]
     * assert items == [1, 2, 3]
     * </pre>
     *
     * See also <code>addAll</code> for similar functionality with modify semantics, i.e. which performs
     * the changes on the original list itself.
     *
     * @param self      an original List
     * @param additions a List containing elements to be merged with elements from the original List
     * @param index     index at which to insert the first element from the given additions List
     * @return the new list
     * @since 1.8.1
     */
    public static <T> List<T> plus(List<T> self, int index, List<T> additions) {
        final List<T> answer = new ArrayList<T>(self);
        answer.addAll(index, additions);
        return answer;
    }

    /**
     * Creates a new List by inserting all of the elements in the given Iterable
     * to the elements from this List at the specified index.
     *
     * @param self      an original list
     * @param additions an Iterable containing elements to be merged with the elements from the original List
     * @param index     index at which to insert the first element from the given additions Iterable
     * @return the new list
     * @since 1.8.7
     * @see #plus(List, int, List)
     */
    public static <T> List<T> plus(List<T> self, int index, Iterable<T> additions) {
        return plus(self, index, toList(additions));
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
     * Synonym for {@link #toSpreadMap(java.util.Map)}.
     * @param self a map
     * @return a newly created SpreadMap
     * @since 1.0
     */
    public static SpreadMap spread(Map self) {
        return toSpreadMap(self);
    }

    /**
     * Returns a new <code>SpreadMap</code> from this map.
     * <p/>
     * The example below shows the various possible use cases:
     * <pre class="groovyTestCase">
     * def fn(Map m) { return m.a + m.b + m.c + m.d }
     *
     * assert fn(a:1, b:2, c:3, d:4) == 10
     * assert fn(a:1, *:[b:2, c:3], d:4) == 10
     * assert fn([a:1, b:2, c:3, d:4].toSpreadMap()) == 10
     * assert fn((['a', 1, 'b', 2, 'c', 3, 'd', 4] as Object[]).toSpreadMap()) == 10
     * assert fn(['a', 1, 'b', 2, 'c', 3, 'd', 4].toSpreadMap()) == 10
     * assert fn(['abcd'.toList(), 1..4].transpose().flatten().toSpreadMap()) == 10
     * </pre>
     * Note that toSpreadMap() is not normally used explicitly but under the covers by Groovy.
     *
     * @param self a map to be converted into a SpreadMap
     * @return a newly created SpreadMap if this map is not null and its size is positive.
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
     * <p/>
     * @param self an object array
     * @return a newly created SpreadMap
     * @see groovy.lang.SpreadMap#SpreadMap(java.lang.Object[])
     * @see #toSpreadMap(java.util.Map)
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
     * Creates a spreadable map from this list.
     * <p/>
     * @param self a list
     * @return a newly created SpreadMap
     * @see groovy.lang.SpreadMap#SpreadMap(java.util.List)
     * @see #toSpreadMap(java.util.Map)
     * @since 1.8.0
     */
    public static SpreadMap toSpreadMap(List self) {
        if (self == null)
            throw new GroovyRuntimeException("Fail to convert List to SpreadMap, because it is null.");
        else if (self.size() % 2 != 0)
            throw new GroovyRuntimeException("Fail to convert List to SpreadMap, because it's size is not even.");
        else
            return new SpreadMap(self);
    }

    /**
     * Wraps a map using the decorator pattern with a wrapper that intercepts all calls
     * to <code>get(key)</code>. If an unknown key is found, a default value will be
     * stored into the Map before being returned. The default value stored will be the
     * result of calling the supplied Closure with the key as the parameter to the Closure.
     * Example usage:
     * <pre class="groovyTestCase">
     * def map = [a:1, b:2].withDefault{ k -> k.toCharacter().isLowerCase() ? 10 : -10 }
     * def expected = [a:1, b:2, c:10, D:-10]
     * assert expected.every{ e -> e.value == map[e.key] }
     *
     * def constMap = [:].withDefault{ 42 }
     * assert constMap.foo == 42
     * assert constMap.size() == 1
     * </pre>
     *
     * @param self a Map
     * @param init a Closure which is passed the unknown key
     * @return the wrapped Map
     * @since 1.7.1
     */
    public static <K, V> Map<K, V> withDefault(Map<K, V> self, Closure init) {
        return MapWithDefault.newInstance(self, init);
    }

    /**
     * An alias for <code>withLazyDefault</code> which decorates a list allowing
     * it to grow when called with index values outside the normal list bounds.
     *
     * @param self a List
     * @param init a Closure with the target index as parameter which generates the default value
     * @return the decorated List
     * @see #withLazyDefault(java.util.List, groovy.lang.Closure)
     * @see #withEagerDefault(java.util.List, groovy.lang.Closure)
     * @since 1.8.7
     */
    public static <T> List<T> withDefault(List<T> self, Closure init) {
        return withLazyDefault(self, init);
    }

    /**
     * Decorates a list allowing it to grow when called with a non-existent index value.
     * When called with such values, the list is grown in size and a default value
     * is placed in the list by calling a supplied <code>init</code> Closure. Subsequent
     * retrieval operations if finding a null value in the list assume it was set
     * as null from an earlier growing operation and again call the <code>init</code> Closure
     * to populate the retrieved value; consequently the list can't be used to store null values.
     * <p/>
     * How it works: The decorated list intercepts all calls
     * to <code>getAt(index)</code> and <code>get(index)</code>. If an index greater than
     * or equal to the current <code>size()</code> is used, the list will grow automatically
     * up to the specified index. Gaps will be filled by {@code null}. If a default value
     * should also be used to fill gaps instead of {@code null}, use <code>withEagerDefault</code>.
     * If <code>getAt(index)</code> or <code>get(index)</code> are called and a null value
     * is found, it is assumed that the null value was a consequence of an earlier grow list
     * operation and the <code>init</code> Closure is called to populate the value.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * def list = [0, 1].withLazyDefault{ 42 }
     * assert list[0] == 0
     * assert list[1] == 1
     * assert list[3] == 42   // default value
     * assert list == [0, 1, null, 42] // gap filled with null
     *
     * // illustrate using the index when generating default values
     * def list2 = [5].withLazyDefault{ index -> index * index }
     * assert list2[3] == 9
     * assert list2 == [5, null, null, 9]
     * assert list2[2] == 4
     * assert list2 == [5, null, 4, 9]
     *
     * // illustrate what happens with null values
     * list2[2] = null
     * assert list2[2] == 4
     * </pre>
     *
     * @param self a List
     * @param init a Closure with the target index as parameter which generates the default value
     * @return the decorated List
     * @since 1.8.7
     */
    public static <T> List<T> withLazyDefault(List<T> self, Closure init) {
        return ListWithDefault.newInstance(self, true, init);
    }

    /**
     * Decorates a list allowing it to grow when called with a non-existent index value.
     * When called with such values, the list is grown in size and a default value
     * is placed in the list by calling a supplied <code>init</code> Closure. Null values
     * can be stored in the list.
     * <p/>
     * How it works: The decorated list intercepts all calls
     * to <code>getAt(index)</code> and <code>get(index)</code>. If an index greater than
     * or equal to the current <code>size()</code> is used, the list will grow automatically
     * up to the specified index. Gaps will be filled by calling the <code>init</code> Closure.
     * If generating a default value is a costly operation consider using <code>withLazyDefault</code>.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * def list = [0, 1].withEagerDefault{ 42 }
     * assert list[0] == 0
     * assert list[1] == 1
     * assert list[3] == 42   // default value
     * assert list == [0, 1, 42, 42]   // gap filled with default value
     *
     * // illustrate using the index when generating default values
     * def list2 = [5].withEagerDefault{ index -> index * index }
     * assert list2[3] == 9
     * assert list2 == [5, 1, 4, 9]
     *
     * // illustrate what happens with null values
     * list2[2] = null
     * assert list2[2] == null
     * assert list2 == [5, 1, null, 9]
     * </pre>
     *
     * @param self a List
     * @param init a Closure with the target index as parameter which generates the default value
     * @return the wrapped List
     * @since 1.8.7
     */
    public static <T> List<T> withEagerDefault(List<T> self, Closure init) {
        return ListWithDefault.newInstance(self, false, init);
    }

    /**
     * Sorts the Collection. Assumes that the collection items are comparable
     * and uses their natural ordering to determine the resulting order.
     * If the Collection is a List, it is sorted in place and returned.
     * Otherwise, the elements are first placed into a new list which is then
     * sorted and returned - leaving the original Collection unchanged.
     * <pre class="groovyTestCase">assert [1,2,3] == [3,1,2].sort()</pre>
     *
     * @param self the collection to be sorted
     * @return the sorted collection as a List
     * @see #sort(Collection, boolean)
     * @since 1.0
     */
    public static <T> List<T> sort(Collection<T> self) {
        return sort(self, true);
    }

    /**
     * Sorts the Collection. Assumes that the collection items are
     * comparable and uses their natural ordering to determine the resulting order.
     * If the Collection is a List and mutate is true,
     * it is sorted in place and returned. Otherwise, the elements are first placed
     * into a new list which is then sorted and returned - leaving the original Collection unchanged.
     * <pre class="groovyTestCase">assert [1,2,3] == [3,1,2].sort()</pre>
     * <pre class="groovyTestCase">
     * def orig = [1, 3, 2]
     * def sorted = orig.sort(false)
     * assert orig == [1, 3, 2]
     * assert sorted == [1, 2, 3]
     * </pre>
     *
     * @param self   the collection to be sorted
     * @param mutate false will always cause a new list to be created, true will mutate lists in place
     * @return the sorted collection as a List
     * @since 1.8.1
     */
    public static <T> List<T> sort(Collection<T> self, boolean mutate) {
        List<T> answer = mutate ? asList(self) : toList(self);
        Collections.sort(answer, new NumberAwareComparator<T>());
        return answer;
    }

    /**
     * Sorts the elements from the given map into a new ordered map using
     * the closure as a comparator to determine the ordering.
     * The original map is unchanged.
     * <pre class="groovyTestCase">def map = [a:5, b:3, c:6, d:4].sort { a, b -> a.value <=> b.value }
     * assert map == [b:3, d:4, a:5, c:6]</pre>
     *
     * @param self the original unsorted map
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
     * Sorts the elements from the given map into a new ordered Map using
     * the specified key comparator to determine the ordering.
     * The original map is unchanged.
     * <pre class="groovyTestCase">def map = [ba:3, cz:6, ab:5].sort({ a, b -> a[-1] <=> b[-1] } as Comparator)
     * assert map*.value == [3, 5, 6]</pre>
     *
     * @param self the original unsorted map
     * @param comparator a Comparator
     * @return the sorted map
     * @since 1.7.2
     */
    public static <K, V> Map<K, V> sort(Map<K, V> self, Comparator<K> comparator) {
        Map<K, V> result = new TreeMap<K, V>(comparator);
        result.putAll(self);
        return result;
    }

    /**
     * Sorts the elements from the given map into a new ordered Map using
     * the natural ordering of the keys to determine the ordering.
     * The original map is unchanged.
     * <pre class="groovyTestCase">map = [ba:3, cz:6, ab:5].sort()
     * assert map*.value == [5, 3, 6]
     * </pre>
     *
     * @param self the original unsorted map
     * @return the sorted map
     * @since 1.7.2
     */
    public static <K, V> Map<K, V> sort(Map<K, V> self) {
        return new TreeMap<K, V>(self);
    }

    /**
     * Modifies this array so that its elements are in sorted order.
     * The array items are assumed to be comparable.
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
     * Sorts the given array into sorted order.
     * The array items are assumed to be comparable.
     * If mutate is true, the array is sorted in place and returned. Otherwise, a new sorted
     * array is returned and the original array remains unchanged.
     * <pre class="groovyTestCase">
     * def orig = ["hello","hi","Hey"] as String[]
     * def sorted = orig.sort(false)
     * assert orig == ["hello","hi","Hey"] as String[]
     * assert sorted == ["Hey","hello","hi"] as String[]
     * orig.sort(true)
     * assert orig == ["Hey","hello","hi"] as String[]
     * </pre>
     *
     * @param self   the array to be sorted
     * @param mutate false will always cause a new array to be created, true will mutate the array in place
     * @return the sorted array
     * @since 1.8.1
     */
    public static <T> T[] sort(T[] self, boolean mutate) {
        T[] answer = mutate ? self : self.clone();
        Arrays.sort(answer, new NumberAwareComparator<T>());
        return answer;
    }

    /**
     * Sorts the given iterator items into a sorted iterator. The items are
     * assumed to be comparable. The original iterator will become
     * exhausted of elements after completing this method call.
     * A new iterator is produced that traverses the items in sorted order.
     *
     * @param self the Iterator to be sorted
     * @return the sorted items as an Iterator
     * @since 1.5.5
     */
    public static <T> Iterator<T> sort(Iterator<T> self) {
        return sort(toList(self)).listIterator();
    }

    /**
     * Sorts the given iterator items into a sorted iterator using the comparator. The
     * original iterator will become exhausted of elements after completing this method call.
     * A new iterator is produced that traverses the items in sorted order.
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
     * Sorts the Collection using the given Comparator. If the Collection is a List,
     * it is sorted in place and returned. Otherwise, the elements are first placed
     * into a new list which is then sorted and returned - leaving the original Collection unchanged.
     * <pre class="groovyTestCase">
     * assert ["hi","hey","hello"] == ["hello","hi","hey"].sort( { a, b -> a.length() <=> b.length() } as Comparator )
     * </pre>
     * <pre class="groovyTestCase">
     * assert ["hello","Hey","hi"] == ["hello","hi","Hey"].sort(String.CASE_INSENSITIVE_ORDER)
     * </pre>
     *
     * @param self       a collection to be sorted
     * @param comparator a Comparator used for the comparison
     * @return a sorted List
     * @see #sort(Collection, boolean, Comparator)
     * @since 1.0
     */
    public static <T> List<T> sort(Collection<T> self, Comparator<T> comparator) {
        return sort(self, true, comparator);
    }

    /**
     * Sorts the Collection using the given Comparator. If the Collection is a List and mutate
     * is true, it is sorted in place and returned. Otherwise, the elements are first placed
     * into a new list which is then sorted and returned - leaving the original Collection unchanged.
     * <pre class="groovyTestCase">
     * assert ["hi","hey","hello"] == ["hello","hi","hey"].sort( { a, b -> a.length() <=> b.length() } as Comparator )
     * </pre>
     * <pre class="groovyTestCase">
     * def orig = ["hello","hi","Hey"]
     * def sorted = orig.sort(false, String.CASE_INSENSITIVE_ORDER)
     * assert orig == ["hello","hi","Hey"]
     * assert sorted == ["hello","Hey","hi"]
     * </pre>
     *
     * @param self       a collection to be sorted
     * @param mutate     false will always cause a new list to be created, true will mutate lists in place
     * @param comparator a Comparator used for the comparison
     * @return a sorted List
     * @since 1.8.1
     */
    public static <T> List<T> sort(Collection<T> self, boolean mutate, Comparator<T> comparator) {
        List<T> list = mutate ? asList(self) : toList(self);
        Collections.sort(list, comparator);
        return list;
    }

    /**
     * Sorts the given array into sorted order using the given comparator.
     *
     * @param self the array to be sorted
     * @param comparator a Comparator used for the comparison
     * @return the sorted array
     * @since 1.5.5
     */
    public static <T> T[] sort(T[] self, Comparator<T> comparator) {
        return sort(self, true, comparator);
    }

    /**
     * Modifies this array so that its elements are in sorted order as determined by the given comparator.
     * If mutate is true, the array is sorted in place and returned. Otherwise, a new sorted
     * array is returned and the original array remains unchanged.
     * <pre class="groovyTestCase">
     * def orig = ["hello","hi","Hey"] as String[]
     * def sorted = orig.sort(false, String.CASE_INSENSITIVE_ORDER)
     * assert orig == ["hello","hi","Hey"] as String[]
     * assert sorted == ["hello","Hey","hi"] as String[]
     * orig.sort(true, String.CASE_INSENSITIVE_ORDER)
     * assert orig == ["hello","Hey","hi"] as String[]
     * </pre>
     *
     * @param self       the array containing elements to be sorted
     * @param mutate     false will always cause a new array to be created, true will mutate arrays in place
     * @param comparator a Comparator used for the comparison
     * @return a sorted array
     * @since 1.8.1
     */
    public static <T> T[] sort(T[] self, boolean mutate, Comparator<T> comparator) {
        T[] answer = mutate ? self : self.clone();
        Arrays.sort(answer, comparator);
        return answer;
    }

    /**
     * Sorts the given iterator items into a sorted iterator using the Closure to determine the correct ordering.
     * The original iterator will be fully processed after the method call.
     * </p>
     * If the closure has two parameters it is used like a traditional Comparator.
     * I.e. it should compare its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than, equal to,
     * or greater than the second respectively. Otherwise, the Closure is assumed
     * to take a single parameter and return a Comparable (typically an Integer)
     * which is then used for further comparison.
     *
     * @param self    the Iterator to be sorted
     * @param closure a Closure used to determine the correct ordering
     * @return the sorted items as an Iterator
     * @since 1.5.5
     */
    public static <T> Iterator<T> sort(Iterator<T> self, Closure closure) {
        return sort(toList(self), closure).listIterator();
    }

    /**
     * Sorts the elements from this array into a newly created array using
     * the Closure to determine the correct ordering.
     * </p>
     * If the closure has two parameters it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer, zero, or a positive integer when the
     * first parameter is less than, equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a Comparable (typically an Integer)
     * which is then used for further comparison.
     *
     * @param self the array containing the elements to be sorted
     * @param closure a Closure used to determine the correct ordering
     * @return the sorted array
     * @since 1.5.5
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] sort(T[] self, Closure closure) {
        return sort(self, false, closure);
    }

    /**
     * Modifies this array so that its elements are in sorted order using the Closure to determine the correct ordering.
     * If mutate is false, a new array is returned and the original array remains unchanged.
     * Otherwise, the original array is sorted in place and returned.
     * </p>
     * If the closure has two parameters it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer, zero, or a positive integer when the
     * first parameter is less than, equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a Comparable (typically an Integer)
     * which is then used for further comparison.
     * <pre class="groovyTestCase">
     * def orig = ["hello","hi","Hey"] as String[]
     * def sorted = orig.sort(false) { it.size() }
     * assert orig == ["hello","hi","Hey"] as String[]
     * assert sorted == ["hi","Hey","hello"] as String[]
     * orig.sort(true) { it.size() }
     * assert orig == ["hi","Hey","hello"] as String[]
     * </pre>
     *
     * @param self    the array to be sorted
     * @param mutate  false will always cause a new array to be created, true will mutate arrays in place
     * @param closure a Closure used to determine the correct ordering
     * @return the sorted array
     * @since 1.8.1
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] sort(T[] self, boolean mutate, Closure closure) {
        T[] answer = (T[]) sort(toList(self), closure).toArray();
        if (mutate) {
            System.arraycopy(answer, 0, self, 0, answer.length);
        }
        return mutate ? self : answer;
    }

    /**
     * Sorts this Collection using the given Closure to determine the correct ordering. If the Collection is a List,
     * it is sorted in place and returned. Otherwise, the elements are first placed
     * into a new list which is then sorted and returned - leaving the original Collection unchanged.
     * </p>
     * If the Closure has two parameters
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
     * @see #sort(Collection, boolean, Closure)
     * @since 1.0
     */
    public static <T> List<T> sort(Collection<T> self, Closure closure) {
        return sort(self, true, closure);
    }

    /**
     * Sorts this Collection using the given Closure to determine the correct ordering. If the Collection is a List
     * and mutate is true, it is sorted in place and returned. Otherwise, the elements are first placed
     * into a new list which is then sorted and returned - leaving the original Collection unchanged.
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
     * <pre class="groovyTestCase">
     * def orig = ["hello","hi","Hey"]
     * def sorted = orig.sort(false) { it.toUpperCase() }
     * assert orig == ["hello","hi","Hey"]
     * assert sorted == ["hello","Hey","hi"]
     * </pre>
     *
     * @param self    a Collection to be sorted
     * @param mutate  false will always cause a new list to be created, true will mutate lists in place
     * @param closure a 1 or 2 arg Closure used to determine the correct ordering
     * @return a newly created sorted List
     * @since 1.8.1
     */
    public static <T> List<T> sort(Collection<T> self, boolean mutate, Closure closure) {
        List<T> list = mutate ? asList(self) : toList(self);
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
     * Avoids doing unnecessary work when sorting an already sorted set (i.e. an identity function for an already sorted set).
     *
     * @param self an already sorted set
     * @return the set
     * @since 1.0
     */
    public static <T> SortedSet<T> sort(SortedSet<T> self) {
        return self;
    }

    /**
     * Avoids doing unnecessary work when sorting an already sorted map (i.e. an identity function for an already sorted map).
     *
     * @param self an already sorted map
     * @return the map
     * @since 1.8.1
     */
    public static <K, V> SortedMap<K, V> sort(SortedMap<K, V> self) {
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
     * <pre class="groovyTestCase">
     * def list = [3, 4, 2]
     * assert list.last() == 2
     * // check original is unaltered
     * assert list == [3, 4, 2]
     * </pre>
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
     * Returns the last item from the Iterable.
     * <pre class="groovyTestCase">
     * def set = [3, 4, 2] as LinkedHashSet
     * assert set.last() == 2
     * // check original unaltered
     * assert set == [3, 4, 2] as Set
     * </pre>
     * The first element returned by the Iterable's iterator is returned.
     * If the Iterable doesn't guarantee a defined order it may appear like
     * a random element is returned.
     *
     * @param self an Iterable
     * @return the first item from the Iterable
     * @throws NoSuchElementException if the Iterable is empty and you try to access the last() item.
     * @since 1.8.7
     */
    public static <T> T last(Iterable<T> self) {
        Iterator<T> iterator = self.iterator();
        if (!iterator.hasNext()) {
            throw new NoSuchElementException("Cannot access last() element from an empty Iterable");
        }
        T result = null;
        while (iterator.hasNext()) {
            result = iterator.next();
        }
        return result;
    }

    /**
     * Returns the last item from the array.
     * <pre class="groovyTestCase">
     * def array = [3, 4, 2].toArray()
     * assert array.last() == 2
     * </pre>
     *
     * @param self an array
     * @return the last item from the array
     * @throws NoSuchElementException if the array is empty and you try to access the last() item.
     * @since 1.7.3
     */
    public static <T> T last(T[] self) {
        if (self.length == 0) {
            throw new NoSuchElementException("Cannot access last() element from an empty Array");
        }
        return self[self.length - 1];
    }

    /**
     * Returns the first item from the List.
     * <pre class="groovyTestCase">
     * def list = [3, 4, 2]
     * assert list.first() == 3
     * // check original is unaltered
     * assert list == [3, 4, 2]
     * </pre>
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
     * Returns the first item from the Iterable.
     * <pre class="groovyTestCase">
     * def set = [3, 4, 2] as LinkedHashSet
     * assert set.first() == 3
     * // check original is unaltered
     * assert set == [3, 4, 2] as Set
     * </pre>
     * The first element returned by the Iterable's iterator is returned.
     * If the Iterable doesn't guarantee a defined order it may appear like
     * a random element is returned.
     *
     * @param self an Iterable
     * @return the first item from the Iterable
     * @throws NoSuchElementException if the Iterable is empty and you try to access the first() item.
     * @since 1.8.7
     */
    public static <T> T first(Iterable<T> self) {
        Iterator<T> iterator = self.iterator();
        if (!iterator.hasNext()) {
            throw new NoSuchElementException("Cannot access first() element from an empty Iterable");
        }
        return iterator.next();
    }

    /**
     * Returns the first item from the array.
     * <pre class="groovyTestCase">
     * def array = [3, 4, 2].toArray()
     * assert array.first() == 3
     * </pre>
     *
     * @param self an array
     * @return the first item from the array
     * @throws NoSuchElementException if the array is empty and you try to access the first() item.
     * @since 1.7.3
     */
    public static <T> T first(T[] self) {
        if (self.length == 0) {
            throw new NoSuchElementException("Cannot access first() element from an empty array");
        }
        return self[0];
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
     * Returns the first item from the Object array.
     * <pre class="groovyTestCase">def array = [3, 4, 2].toArray()
     * assert array.head() == 3</pre>
     *
     * @param self an Object array
     * @return the first item from the Object array
     * @throws NoSuchElementException if the array is empty and you try to access the head() item.
     * @since 1.7.3
     */
    public static <T> T head(T[] self) {
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
     * Returns the items from the Object array excluding the first item.
     * <pre class="groovyTestCase">
     *     String[] strings = ["a", "b", "c"]
     *     def result = strings.tail()
     *     assert strings.class.componentType == String
     * </pre>
     *
     * @param self an Object array
     * @return an Object array without its first element
     * @throws NoSuchElementException if the list is empty and you try to access the tail() item.
     * @since 1.7.3
     */
    public static <T> T[] tail(T[] self) {
        if (self.length == 0) {
            throw new NoSuchElementException("Cannot access tail() for an empty Object array");
        }
        Class<T> componentType = (Class<T>) self.getClass().getComponentType();
        T[] result = (T[]) Array.newInstance(componentType, self.length - 1);
        System.arraycopy(self, 1, result, 0, self.length - 1);
        return result;
    }

    /**
     * Returns the first <code>num</code> elements from the head of this list.
     * <pre class="groovyTestCase">
     * def strings = [ 'a', 'b', 'c' ]
     * assert strings.take( 0 ) == []
     * assert strings.take( 2 ) == [ 'a', 'b' ]
     * assert strings.take( 5 ) == [ 'a', 'b', 'c' ]
     * </pre>
     * Similar to {@link #take(Iterable, int)}
     * except that it attempts to preserve the type of the original list.
     *
     * @param self the original list
     * @param num  the number of elements to take from this list
     * @return a list consisting of the first <code>num</code> elements of this list,
     *         or else the whole list if it has less then <code>num</code> elements.
     * @since 1.8.1
     */
    public static <T> List<T> take(List<T> self, int num) {
        if (self.isEmpty() || num <= 0) {
            return createSimilarList(self, 0);
        }
        if (self.size() <= num) {
            List<T> ret = createSimilarList(self, self.size());
            ret.addAll(self);
            return ret;
        }
        List<T> ret = createSimilarList(self, num);
        ret.addAll(self.subList(0, num));
        return ret;
    }

    /**
     * Returns the first <code>num</code> elements from the head of this array.
     * <pre class="groovyTestCase">
     * String[] strings = [ 'a', 'b', 'c' ]
     * assert strings.take( 0 ) == [] as String[]
     * assert strings.take( 2 ) == [ 'a', 'b' ] as String[]
     * assert strings.take( 5 ) == [ 'a', 'b', 'c' ] as String[]
     * </pre>
     *
     * @param self the original array
     * @param num  the number of elements to take from this array
     * @return an array consisting of the first <code>num</code> elements of this array,
     *         or else the whole array if it has less then <code>num</code> elements.
     * @since 1.8.1
     */
    public static <T> T[] take(T[] self, int num) {
        if (self.length == 0 || num <= 0) {
            return createSimilarArray(self, 0);
        }

        if (self.length <= num) {
            T[] ret = createSimilarArray(self, self.length);
            System.arraycopy(self, 0, ret, 0, self.length);
            return ret;
        }

        T[] ret = createSimilarArray(self, num);
        System.arraycopy(self, 0, ret, 0, num);
        return ret;
    }

    /**
     * Returns the first <code>num</code> elements from the head of this Iterable.
     * <pre class="groovyTestCase">
     * class AbcIterable implements Iterable<String> {
     *     Iterator<String> iterator() { "abc".iterator() }
     * }
     * def abc = new AbcIterable()
     * assert abc.take(0) == []
     * assert abc.take(1) == ['a']
     * assert abc.take(3) == ['a', 'b', 'c']
     * assert abc.take(5) == ['a', 'b', 'c']
     * </pre>
     *
     * @param self the original Iterable
     * @param num  the number of elements to take from this Iterable
     * @return a List consisting of the first <code>num</code> elements from this Iterable,
     *         or else all the elements from the Iterable if it has less then <code>num</code> elements.
     * @since 1.8.7
     */
    public static <T> List<T> take(Iterable<T> self, int num) {
        return toList(take(self.iterator(), num));
    }

    /**
     * Returns a new map containing the first <code>num</code> elements from the head of this map.
     * If the map instance does not have ordered keys, then this function could return a random <code>num</code>
     * entries. Groovy by default uses LinkedHashMap, so this shouldn't be an issue in the main.
     * <pre class="groovyTestCase">
     * def strings = [ 'a':10, 'b':20, 'c':30 ]
     * assert strings.take( 0 ) == [:]
     * assert strings.take( 2 ) == [ 'a':10, 'b':20 ]
     * assert strings.take( 5 ) == [ 'a':10, 'b':20, 'c':30 ]
     * </pre>
     *
     * @param self the original map
     * @param num  the number of elements to take from this map
     * @return a new map consisting of the first <code>num</code> elements of this map,
     *         or else the whole map if it has less then <code>num</code> elements.
     * @since 1.8.1
     */
    public static <K, V> Map<K, V> take(Map<K, V> self, int num) {
        if (self.isEmpty() || num <= 0) {
            return createSimilarMap(self);
        }
        Map<K, V> ret = createSimilarMap(self);
        for (K key : self.keySet()) {
            ret.put(key, self.get(key));
            if (--num <= 0) {
                break;
            }
        }
        return ret;
    }

    /**
     * Returns an iterator of up to the first <code>num</code> elements from this iterator.
     * The original iterator is stepped along by <code>num</code> elements.
     * <pre class="groovyTestCase">
     * def a = 0
     * def iter = [ hasNext:{ true }, next:{ a++ } ] as Iterator
     * def iteratorCompare( Iterator a, List b ) {
     *     a.collect { it } == b
     * }
     * assert iteratorCompare( iter.take( 0 ), [] )
     * assert iteratorCompare( iter.take( 2 ), [ 0, 1 ] )
     * assert iteratorCompare( iter.take( 5 ), [ 2, 3, 4, 5, 6 ] )
     * </pre>
     *
     * @param self the Iterator
     * @param num  the number of elements to take from this iterator
     * @return an iterator consisting of up to the first <code>num</code> elements of this iterator.
     * @since 1.8.1
     */
    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> take(Iterator<T> self, int num) {
        return new TakeIterator(self, num);
    }

    private static class TakeIterator<E> implements Iterator<E> {
        private final Iterator<E> delegate;
        private Integer num;

        private TakeIterator(Iterator<E> delegate, Integer num) {
            this.delegate = delegate;
            this.num = num;
        }

        public boolean hasNext() {
            return delegate.hasNext() && num > 0;
        }

        public E next() {
            if (num == 0) throw new NoSuchElementException();
            num--;
            return delegate.next();
        }

        public void remove() {
            delegate.remove();
        }
    }

    /**
     * Returns the first <code>num</code> elements from this CharSequence.
     * <pre class="groovyTestCase">
     *     def text = "Groovy"
     *     assert text.take( 0 ) == ''
     *     assert text.take( 2 ) == 'Gr'
     *     assert text.take( 7 ) == 'Groovy'
     * </pre>
     *
     * @param self the original CharSequence
     * @param num the number of chars to take from this CharSequence
     * @return a CharSequence consisting of the first <code>num</code> chars,
     *         or else the whole CharSequence if it has less then <code>num</code> elements.
     * @since 1.8.1
     */
    public static CharSequence take( CharSequence self, int num ) {
        if( num < 0 ) {
            return self.subSequence( 0, 0 ) ;
        }
        if( self.length() <= num ) {
            return self ;
        }
        return self.subSequence( 0, num ) ;
    }

    /**
     * Drops the given number of elements from the head of this list
     * if they are available.
     * <pre class="groovyTestCase">
     * def strings = [ 'a', 'b', 'c' ]
     * assert strings.drop( 0 ) == [ 'a', 'b', 'c' ]
     * assert strings.drop( 2 ) == [ 'c' ]
     * assert strings.drop( 5 ) == []
     * </pre>
     * Similar to {@link #drop(Iterable, int)}
     * except that it attempts to preserve the type of the original list.
     *
     * @param self the original list
     * @param num  the number of elements to drop from this list
     * @return a list consisting of all elements of this list except the first
     *         <code>num</code> ones, or else the empty list, if this list has
     *         less than <code>num</code> elements.
     * @since 1.8.1
     */
    public static <T> List<T> drop(List<T> self, int num) {
        if (self.size() <= num) {
            return createSimilarList(self, 0);
        }
        if (num <= 0) {
            List<T> ret = createSimilarList(self, self.size());
            ret.addAll(self);
            return ret;
        }
        List<T> ret = createSimilarList(self, self.size() - num);
        ret.addAll(self.subList(num, self.size()));
        return ret;
    }

    /**
     * Drops the given number of elements from the head of this Iterable.
     * <pre class="groovyTestCase">
     * class AbcIterable implements Iterable<String> {
     *     Iterator<String> iterator() { "abc".iterator() }
     * }
     * def abc = new AbcIterable()
     * assert abc.drop(0) == ['a', 'b', 'c']
     * assert abc.drop(1) == ['b', 'c']
     * assert abc.drop(3) == []
     * assert abc.drop(5) == []
     * </pre>
     *
     * @param self the original Iterable
     * @param num  the number of elements to drop from this Iterable
     * @return a List consisting of all the elements of this Iterable minus the first <code>num</code> elements,
     *         or an empty list if it has less then <code>num</code> elements.
     * @since 1.8.7
     */
    public static <T> List<T> drop(Iterable<T> self, int num) {
        return toList(drop(self.iterator(), num));
    }

    /**
     * Drops the given number of elements from the head of this array
     * if they are available.
     * <pre class="groovyTestCase">
     * String[] strings = [ 'a', 'b', 'c' ]
     * assert strings.drop( 0 ) == [ 'a', 'b', 'c' ] as String[]
     * assert strings.drop( 2 ) == [ 'c' ] as String[]
     * assert strings.drop( 5 ) == [] as String[]
     * </pre>
     *
     * @param self the original array
     * @param num  the number of elements to drop from this array
     * @return an array consisting of all elements of this array except the
     *         first <code>num</code> ones, or else the empty array, if this
     *         array has less than <code>num</code> elements.
     * @since 1.8.1
     */
    public static <T> T[] drop(T[] self, int num) {
        if (self.length <= num) {
            return createSimilarArray(self, 0);
        }
        if (num <= 0) {
            T[] ret = createSimilarArray(self, self.length);
            System.arraycopy(self, 0, ret, 0, self.length);
            return ret;
        }

        T[] ret = createSimilarArray(self, self.length - num);
        System.arraycopy(self, num, ret, 0, self.length - num);
        return ret;
    }

    /**
     * Drops the given number of key/value pairs from the head of this map if they are available.
     * <pre class="groovyTestCase">
     * def strings = [ 'a':10, 'b':20, 'c':30 ]
     * assert strings.drop( 0 ) == [ 'a':10, 'b':20, 'c':30 ]
     * assert strings.drop( 2 ) == [ 'c':30 ]
     * assert strings.drop( 5 ) == [:]
     * </pre>
     * If the map instance does not have ordered keys, then this function could drop a random <code>num</code>
     * entries. Groovy by default uses LinkedHashMap, so this shouldn't be an issue in the main.
     *
     * @param self the original map
     * @param num  the number of elements to drop from this map
     * @return a map consisting of all key/value pairs of this map except the first
     *         <code>num</code> ones, or else the empty map, if this map has
     *         less than <code>num</code> elements.
     * @since 1.8.1
     */
    public static <K, V> Map<K, V> drop(Map<K, V> self, int num) {
        if (self.size() <= num) {
            return createSimilarMap(self);
        }
        if (num == 0) {
            return cloneSimilarMap(self);
        }
        Map<K, V> ret = createSimilarMap(self);
        for (K key : self.keySet()) {
            if (num-- <= 0) {
                ret.put(key, self.get(key));
            }
        }
        return ret;
    }

    /**
     * Drops the given number of elements from the head of this iterator if they are available.
     * The original iterator is stepped along by <code>num</code> elements.
     * <pre class="groovyTestCase">
     * def iteratorCompare( Iterator a, List b ) {
     *     a.collect { it } == b
     * }
     * def iter = [ 1, 2, 3, 4, 5 ].listIterator()
     * assert iteratorCompare( iter.drop( 0 ), [ 1, 2, 3, 4, 5 ] )
     * iter = [ 1, 2, 3, 4, 5 ].listIterator()
     * assert iteratorCompare( iter.drop( 2 ), [ 3, 4, 5 ] )
     * iter = [ 1, 2, 3, 4, 5 ].listIterator()
     * assert iteratorCompare( iter.drop( 5 ), [] )
     * </pre>
     *
     * @param self the original iterator
     * @param num  the number of elements to drop from this iterator
     * @return The iterator stepped along by <code>num</code> elements if they exist.
     * @since 1.8.1
     */
    public static <T> Iterator<T> drop(Iterator<T> self, int num) {
        while (num-- > 0 && self.hasNext()) {
            self.next();
        }
        return self;
    }

    /**
     * Drops the given number of chars from the head of this CharSequence
     * if they are available.
     * <pre class="groovyTestCase">
     *     def text = "Groovy"
     *     assert text.drop( 0 ) == 'Groovy'
     *     assert text.drop( 2 ) == 'oovy'
     *     assert text.drop( 7 ) == ''
     * </pre>
     *
     * @param self the original CharSequence
     * @param num the number of characters to drop from this iterator
     * @return a CharSequence consisting of all characters except the first <code>num</code> ones,
     *         or else an empty String, if this CharSequence has less than <code>num</code> characters.
     * @since 1.8.1
     */
    public static CharSequence drop(CharSequence self, int num) {
        if( num <= 0 ) {
            return self ;
        }
        if( self.length() <= num ) {
            return self.subSequence( 0, 0 ) ;
        }
        return self.subSequence( num, self.length() ) ;
    }

    /**
     * Returns a suffix of this List where elements are dropped from the front
     * while the given Closure evaluates to true.
     * Similar to {@link #dropWhile(Iterable, groovy.lang.Closure)}
     * except that it attempts to preserve the type of the original list.
     * <pre class="groovyTestCase">
     * def nums = [ 1, 3, 2 ]
     * assert nums.dropWhile{ it < 4 } == []
     * assert nums.dropWhile{ it < 3 } == [ 3, 2 ]
     * assert nums.dropWhile{ it != 2 } == [ 2 ]
     * assert nums.dropWhile{ it == 0 } == [ 1, 3, 2 ]
     * </pre>
     *
     * @param self      the original list
     * @param condition the closure that must evaluate to true to continue dropping elements
     * @return the shortest suffix of the given List such that the given closure condition
     *         evaluates to true for each element dropped from the front of the List
     * @since 1.8.7
     */
    public static <T> List<T> dropWhile(List<T> self, Closure<?> condition) {
        int num = 0;
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(condition);
        for (T value : self) {
            if (bcw.call(value)) {
                num += 1;
            } else {
                break;
            }
        }
        return drop(self, num);
    }

    /**
     * Returns a suffix of this Iterable where elements are dropped from the front
     * while the given closure evaluates to true.
     * <pre class="groovyTestCase">
     * class AbcIterable implements Iterable<String> {
     *     Iterator<String> iterator() { "abc".iterator() }
     * }
     * def abc = new AbcIterable()
     * assert abc.dropWhile{ it < 'b' } == ['b', 'c']
     * assert abc.dropWhile{ it <= 'b' } == ['c']
     * </pre>
     *
     * @param self      an Iterable
     * @param condition the closure that must evaluate to true to continue dropping elements
     * @return the shortest suffix of the given Iterable such that the given closure condition
     *         evaluates to true for each element dropped from the front of the Iterable
     * @since 1.8.7
     */
    public static <T> List<T> dropWhile(Iterable<T> self, Closure<?> condition) {
        return toList(dropWhile(self.iterator(), condition));
    }

    /**
     * Create a suffix of the given Map by dropping as many entries as possible from the
     * front of the original Map such that calling the given closure condition evaluates to
     * true when passed each of the dropped entries (or key/value pairs).
     * <pre class="groovyTestCase">
     * def shopping = [milk:1, bread:2, chocolate:3]
     * assert shopping.takeWhile{ it.key.size() < 6 } == [milk:1, bread:2]
     * assert shopping.takeWhile{ it.value % 2 } == [milk:1]
     * assert shopping.takeWhile{ k, v -> k.size() + v <= 7 } == [milk:1, bread:2]
     * </pre>
     * If the map instance does not have ordered keys, then this function could appear to drop random
     * entries. Groovy by default uses LinkedHashMap, so this shouldn't be an issue in the main.
     *
     * @param self      a Map
     * @param condition a 1 (or 2) arg Closure that must evaluate to true for the
     *                  entry (or key and value) to continue dropping elements
     * @return the shortest suffix of the given Map such that the given closure condition
     *         evaluates to true for each element dropped from the front of the Map
     * @since 1.8.7
     */
    public static <K, V> Map<K, V> dropWhile(Map<K, V> self, Closure<?> condition) {
        if (self.isEmpty()) {
            return createSimilarMap(self);
        }
        Map<K, V> ret = createSimilarMap(self);
        boolean dropping = true;
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(condition);
        for (Map.Entry<K, V> entry : self.entrySet()) {
            if (dropping && !bcw.callForMap(entry)) dropping = false;
            if (!dropping) ret.put(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    /**
     * Create a suffix of the given array by dropping as many elements as possible from the
     * front of the original array such that calling the given closure condition evaluates to
     * true when passed each of the dropped elements.
     * <pre class="groovyTestCase">
     * def nums = [ 1, 3, 2 ] as Integer[]
     * assert nums.dropWhile{ it <= 3 } == [ ] as Integer[]
     * assert nums.dropWhile{ it < 3 } == [ 3, 2 ] as Integer[]
     * assert nums.dropWhile{ it != 2 } == [ 2 ] as Integer[]
     * assert nums.dropWhile{ it == 0 } == [ 1, 3, 2 ] as Integer[]
     * </pre>
     *
     * @param self      the original array
     * @param condition the closure that must evaluate to true to
     *                  continue dropping elements
     * @return the shortest suffix of the given array such that the given closure condition
     *         evaluates to true for each element dropped from the front of the array
     * @since 1.8.7
     */
    public static <T> T[] dropWhile(T[] self, Closure<?> condition) {
        int num = 0;
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(condition);
        while (num < self.length) {
            if (bcw.call(self[num])) {
                num += 1;
            } else {
                break;
            }
        }
        return drop(self, num);
    }

    /**
     * Creates an Iterator that returns a suffix of the elements from an original Iterator. As many elements
     * as possible are dropped from the front of the original Iterator such that calling the given closure
     * condition evaluates to true when passed each of the dropped elements.
     * <p/>
     * <pre class="groovyTestCase">
     * def a = 0
     * def iter = [ hasNext:{ a < 10 }, next:{ a++ } ] as Iterator
     * assert [].iterator().dropWhile{ it < 3 }.toList() == []
     * assert [1, 2, 3, 4, 5].iterator().dropWhile{ it < 3 }.toList() == [ 3, 4, 5 ]
     * assert iter.dropWhile{ it < 5 }.toList() == [ 5, 6, 7, 8, 9 ]
     * </pre>
     *
     * @param self      the Iterator
     * @param condition the closure that must evaluate to true to continue dropping elements
     * @return the shortest suffix of elements from the given Iterator such that the given closure condition
     *         evaluates to true for each element dropped from the front of the Iterator
     * @since 1.8.7
     */
    public static <T> Iterator<T> dropWhile(Iterator<T> self, Closure<?> condition) {
        return new DropWhileIterator<T>(self, condition);
    }

    private static class DropWhileIterator<E> implements Iterator<E> {
        private final Iterator<E> delegate;
        private final Closure condition;
        private boolean buffering = false;
        private E buffer = null;

        private DropWhileIterator(Iterator<E> delegate, Closure condition) {
            this.delegate = delegate;
            this.condition = condition;
            prepare();
        }

        public boolean hasNext() {
            return buffering || delegate.hasNext();
        }

        public E next() {
            if (buffering) {
                E result = buffer;
                buffering = false;
                buffer = null;
                return result;
            } else {
                return delegate.next();
            }
        }

        public void remove() {
            if (buffering) {
                buffering = false;
                buffer = null;
            } else {
                delegate.remove();
            }
        }

        private void prepare() {
            BooleanClosureWrapper bcw = new BooleanClosureWrapper(condition);
            while (delegate.hasNext()) {
                E next = delegate.next();
                if (!bcw.call(next)) {
                    buffer = next;
                    buffering = true;
                    break;
                }
            }
        }
    }

    /**
     * Converts this Collection to a List. Returns the original Collection
     * if it is already a List.
     * <p>
     * Example usage:
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
            return toList(self);
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
        return bool;
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
     * Coerces a byte array to a boolean value.
     * A byte array is false if the array is of length 0,
     * and true otherwise.
     *
     * @param array an array
     * @return the array's boolean value
     * @since 1.7.4
     */
    public static boolean asBoolean(byte[] array) {
        return array.length > 0;
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
        return array.length > 0;
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
        return array.length > 0;
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
        return array.length > 0;
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
        return array.length > 0;
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
        return array.length > 0;
    }

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
        return array.length > 0;
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
        return character != 0;
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
     * @see #asType(java.lang.Object, java.lang.Class)
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static <T> T asType(Collection col, Class<T> clazz) {
        if (col.getClass() == clazz) {
            return (T) col;
        }
        if (clazz == List.class) {
            return (T) asList(col);
        }
        if (clazz == Set.class) {
            if (col instanceof Set) return (T) col;
            return (T) new LinkedHashSet(col);
        }
        if (clazz == SortedSet.class) {
            if (col instanceof SortedSet) return (T) col;
            return (T) new TreeSet(col);
        }
        if (clazz == Queue.class) {
            if (col instanceof Queue) return (T) col;
            return (T) new LinkedList(col);
        }
        if (clazz == Stack.class) {
            if (col instanceof Stack) return (T) col;
            final Stack stack = new Stack();
            stack.addAll(col);
            return (T) stack;
        }

        if (clazz!=String[].class && ReflectionCache.isArray(clazz)) {
            try {
                return (T) asArrayType(col, clazz);
            } catch (GroovyCastException e) {
                /* ignore */
            }
        }

        Object[] args = {col};
        try {
            return (T) InvokerHelper.invokeConstructorOf(clazz, args);
        } catch (Exception e) {
            // ignore, the constructor that takes a Collection as an argument may not exist
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            try {
                Collection result = (Collection) InvokerHelper.invokeConstructorOf(clazz, null);
                result.addAll(col);
                return (T)result;
            } catch (Exception e) {
                // ignore, the no arg constructor might not exist.
            }
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
     * @see #asType(java.lang.Object, java.lang.Class)
     * @since 1.5.1
     */
    @SuppressWarnings("unchecked")
    public static <T> T asType(Object[] ary, Class<T> clazz) {
        if (clazz == List.class) {
            return (T) new ArrayList(Arrays.asList(ary));
        }
        if (clazz == Set.class) {
            return (T) new HashSet(Arrays.asList(ary));
        }
        if (clazz == SortedSet.class) {
            return (T) new TreeSet(Arrays.asList(ary));
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
    @SuppressWarnings("unchecked")
    public static <T> T asType(Closure cl, Class<T> clazz) {
        if (clazz.isInterface() && !(clazz.isInstance(cl))) {
            return (T) Proxy.newProxyInstance(
                    clazz.getClassLoader(),
                    new Class[]{clazz},
                    new ConvertedClosure(cl));
        }
        try {
            return asType((Object) cl, clazz);
        } catch (GroovyCastException ce) {
            try {
                return (T) ProxyGenerator.INSTANCE.instantiateAggregateFromBaseClass(cl, clazz);
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
    @SuppressWarnings("unchecked")
    public static <T> T asType(Map map, Class<T> clazz) {
        if (!(clazz.isInstance(map)) && clazz.isInterface()) {
            return (T) Proxy.newProxyInstance(
                    clazz.getClassLoader(),
                    new Class[]{clazz},
                    new ConvertedMap(map));
        }
        try {
            return asType((Object) map, clazz);
        } catch (GroovyCastException ce) {
            try {
                return (T) ProxyGenerator.INSTANCE.instantiateAggregateFromBaseClass(map, clazz);
            } catch (GroovyRuntimeException cause) {
                throw new GroovyCastException("Error casting map to " + clazz.getName() +
                        ", Reason: " + cause.getMessage());
            }
        }
    }

    /**
     * Creates a new List with the identical contents to this list
     * but in reverse order.
     * <pre class="groovyTestCase">
     * def list = ["a", 4, false]
     * assert list.reverse() == [false, 4, "a"]
     * assert list == ["a", 4, false]
     * </pre>
     *
     * @param self a List
     * @return a reversed List
     * @see #reverse(List, boolean)
     * @since 1.0
     */
    public static <T> List<T> reverse(List<T> self) {
        return reverse(self, false);
    }

    /**
     * Reverses the elements in a list. If mutate is true, the original list is modified in place and returned.
     * Otherwise, a new list containing the reversed items is produced.
     * <pre class="groovyTestCase">
     * def list = ["a", 4, false]
     * assert list.reverse(false) == [false, 4, "a"]
     * assert list == ["a", 4, false]
     * assert list.reverse(true) == [false, 4, "a"]
     * assert list == [false, 4, "a"]
     * </pre>
     *
     * @param self a List
     * @param mutate true if the list itself should be reversed in place and returned, false if a new list should be created
     * @return a reversed List
     * @since 1.8.1
     */
    public static <T> List<T> reverse(List<T> self, boolean mutate) {
        if (mutate) {
            Collections.reverse(self);
            return self;
        }
        int size = self.size();
        List<T> answer = new ArrayList<T>(size);
        ListIterator<T> iter = self.listIterator(size);
        while (iter.hasPrevious()) {
            answer.add(iter.previous());
        }
        return answer;
    }

    /**
     * Creates a new array containing items which are the same as this array but in reverse order.
     *
     * @param self    an array
     * @return an array containing the reversed items
     * @see #reverse(Object[], boolean)
     * @since 1.5.5
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] reverse(T[] self) {
        return reverse(self, false);
    }

    /**
     * Reverse the items in an array. If mutate is true, the original array is modified in place and returned.
     * Otherwise, a new array containing the reversed items is produced.
     *
     * @param self   an array
     * @param mutate true if the array itself should be reversed in place and returned, false if a new array should be created
     * @return an array containing the reversed items
     * @since 1.8.1
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] reverse(T[] self, boolean mutate) {
        if (!mutate) {
            return (T[]) toList(new ReverseListIterator<T>(Arrays.asList(self))).toArray();
        }
        List<T> items = Arrays.asList(self);
        Collections.reverse(items);
        System.arraycopy((T[])items.toArray(), 0, self, 0, items.size());
        return self;
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
     * Create a Map composed of the intersection of both maps.
     * Any entries that exist in both maps are added to the resultant map.
     * <pre class="groovyTestCase">assert [4:4,5:5] == [1:1,2:2,3:3,4:4,5:5].intersect([4:4,5:5,6:6,7:7,8:8])</pre>
     * <pre class="groovyTestCase">assert [1: 1, 2: 2, 3: 3, 4: 4].intersect( [1: 1.0, 2: 2, 5: 5] ) == [1:1, 2:2]</pre>
     *
     * @param left     a map
     * @param right    a map
     * @return a Map as an intersection of both maps
     * @since 1.7.4
     */
    public static <K,V> Map<K,V> intersect(Map<K,V> left, Map<K,V> right) {
        final Map<K,V> ansMap = createSimilarMap(left);
        if (right != null && right.size() > 0) {
            final Iterator<Map.Entry<K,V>> it1 = left.entrySet().iterator();
            while (it1.hasNext()) {
                final Map.Entry<K,V> e1 = it1.next();
                final Iterator<Map.Entry<K,V>> it2 = right.entrySet().iterator();
                while (it2.hasNext()) {
                    final Map.Entry<K,V> e2 = it2.next();
                    if (DefaultTypeTransformation.compareEqual(e1, e2)) {
                        ansMap.put(e1.getKey(), e1.getValue());
                    }
                }
            }
        }
        return ansMap;
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

    /**
     * Compare the contents of this array to the contents of the given array.
     *
     * @param left  an int array
     * @param right the array being compared
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
     * Determines if the contents of this array are equal to the
     * contents of the given list, in the same order.  This returns
     * <code>false</code> if either collection is <code>null</code>.
     *
     * @param left  an array
     * @param right the List being compared
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
     * @param left  a List
     * @param right the Object[] being compared to
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
        for (int i = left.length - 1; i >= 0; i--) {
            final Object o1 = left[i];
            final Object o2 = right.get(i);
            if (o1 == null) {
                if (o2 != null) return false;
            } else if (!coercedEquals(o1, o2)) {
                return false;
            }
        }
        return true;
    }

    private static boolean coercedEquals(Object o1, Object o2) {
        if (o1 instanceof Comparable) {
            if (!(o2 instanceof Comparable && numberAwareCompareTo((Comparable) o1, (Comparable) o2) == 0)) {
                return false;
            }
        }
        return DefaultTypeTransformation.compareEqual(o1, o2);
    }

    /**
     * Compare the contents of two Lists.  Order matters.
     * If numbers exist in the Lists, then they are compared as numbers,
     * for example 2 == 2L. If both lists are <code>null</code>, the result
     * is true; otherwise if either list is <code>null</code>, the result
     * is <code>false</code>.
     * <pre class="groovyTestCase">assert ["a", 2].equals(["a", 2])
     * assert ![2, "a"].equals("a", 2)
     * assert [2.0, "a"].equals(2L, "a") // number comparison at work</pre>
     *
     * @param left  a List
     * @param right the List being compared to
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
        if (left == right) {
            return true;
        }
        if (left.size() != right.size()) {
            return false;
        }
        final Iterator it1 = left.iterator(), it2 = right.iterator();
        while (it1.hasNext()) {
            final Object o1 = it1.next();
            final Object o2 = it2.next();
            if (o1 == null) {
                if (o2 != null) return false;
            } else if (!coercedEquals(o1, o2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compare the contents of two Sets for equality using Groovy's coercion rules.
     * <p/>
     * Returns <tt>true</tt> if the two sets have the same size, and every member
     * of the specified set is contained in this set (or equivalently, every member
     * of this set is contained in the specified set).
     * If numbers exist in the sets, then they are compared as numbers,
     * for example 2 == 2L.  If both sets are <code>null</code>, the result
     * is true; otherwise if either set is <code>null</code>, the result
     * is <code>false</code>. Example usage:
     * <pre class="groovyTestCase">
     * Set s1 = ["a", 2]
     * def s2 = [2, 'a'] as Set
     * Set s3 = [3, 'a']
     * def s4 = [2.0, 'a'] as Set
     * def s5 = [2L, 'a'] as Set
     * assert s1.equals(s2)
     * assert !s1.equals(s3)
     * assert s1.equals(s4)
     * assert s1.equals(s5)</pre>
     *
     * @param self  a Set
     * @param other the Set being compared to
     * @return <tt>true</tt> if the contents of both sets are identical
     * @since 1.8.0
     */
    public static <T> boolean equals(Set<T> self, Set<T> other) {
        if (self == null) {
            return other == null;
        }
        if (other == null) {
            return false;
        }
        if (self == other) {
            return true;
        }
        if (self.size() != other.size()) {
            return false;
        }
        final Iterator<T> it1 = self.iterator();
        Collection<T> otherItems = new HashSet<T>(other);
        while (it1.hasNext()) {
            final Object o1 = it1.next();
            final Iterator<T> it2 = otherItems.iterator();
            T foundItem = null;
            boolean found = false;
            while (it2.hasNext() && foundItem == null) {
                final T o2 = it2.next();
                if (coercedEquals(o1, o2)) {
                    foundItem = o2;
                    found = true;
                }
            }
            if (!found) return false;
            otherItems.remove(foundItem);
        }
        return otherItems.size() == 0;
    }

    /**
     * Compares two Maps treating coerced numerical values as identical.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">assert [a:2, b:3] == [a:2L, b:3.0]</pre>
     *
     * @param self  this Map
     * @param other the Map being compared to
     * @return <tt>true</tt> if the contents of both maps are identical
     * @since 1.8.0
     */
    public static boolean equals(Map self, Map other) {
        if (self == null) {
            return other == null;
        }
        if (other == null) {
            return false;
        }
        if (self == other) {
            return true;
        }
        if (self.size() != other.size()) {
            return false;
        }
        if (!self.keySet().equals(other.keySet())) {
            return false;
        }
        for (Object key : self.keySet()) {
            if (!coercedEquals(self.get(key), other.get(key))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a Set composed of the elements of the first Set minus the
     * elements of the given Collection.
     *
     * @param self     a Set object
     * @param removeMe the items to remove from the Set
     * @return the resulting Set
     * @since 1.5.0
     */
    public static <T> Set<T> minus(Set<T> self, Collection<?> removeMe) {
        Comparator comparator = (self instanceof SortedSet) ? ((SortedSet) self).comparator() : null;
        final Set<T> ansSet = createSimilarSet(self);
        ansSet.addAll(self);
        if (removeMe != null) {
            for (T o1 : self) {
                for (Object o2 : removeMe) {
                    boolean areEqual = (comparator != null) ? (comparator.compare(o1, o2) == 0) : coercedEquals(o1, o2);
                    if (areEqual) {
                        ansSet.remove(o1);
                    }
                }
            }
        }
        return ansSet;
    }

    /**
     * Create a Set composed of the elements of the first Set minus the
     * elements from the given Iterable.
     *
     * @param self     a Set object
     * @param removeMe the items to remove from the Set
     * @return the resulting Set
     * @since 1.8.7
     */
    public static <T> Set<T> minus(Set<T> self, Iterable<?> removeMe) {
        return minus(self, toList(removeMe));
    }

    /**
     * Create a Set composed of the elements of the first Set minus the given element.
     *
     * @param self     a Set object
     * @param removeMe the element to remove from the Set
     * @return the resulting Set
     * @since 1.5.0
     */
    public static <T> Set<T> minus(Set<T> self, Object removeMe) {
        Comparator comparator = (self instanceof SortedSet) ? ((SortedSet) self).comparator() : null;
        final Set<T> ansSet = createSimilarSet(self);
        for (T t : self) {
            boolean areEqual = (comparator != null)? (comparator.compare(t, removeMe) == 0) : coercedEquals(t, removeMe);
            if (!areEqual) ansSet.add(t);
        }
        return ansSet;
    }

    /**
     * Create an array composed of the elements of the first array minus the
     * elements of the given Iterable.
     *
     * @param self     an object array
     * @param removeMe a Collection of elements to remove
     * @return an array with the supplied elements removed
     * @since 1.5.5
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] minus(T[] self, Iterable removeMe) {
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
    public static <T> T[] minus(T[] self, Object[] removeMe) {
        return (T[]) minus(toList(self), toList(removeMe)).toArray();
    }

    /**
     * Create a List composed of the elements of the first list minus
     * every occurrence of elements of the given Collection.
     * <pre class="groovyTestCase">assert [1, "a", true, true, false, 5.3] - [true, 5.3] == [1, "a", false]</pre>
     *
     * @param self     a List
     * @param removeMe a Collection of elements to remove
     * @return a List with the given elements removed
     * @since 1.0
     */
    public static <T> List<T> minus(List<T> self, Collection<?> removeMe) {

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
                        for (Object t2 : removeMe) {
                            if (Number.class.isInstance(t2)) {
                                if (numberComparator.compare(t, (T)t2) == 0)
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
                for (Iterator<?> iterator = removeMe.iterator(); iterator.hasNext() && !elementRemoved;) {
                    Object elt = iterator.next();
                    if (numberComparator.compare(element, (T)elt) == 0) {
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
     * Create a List composed of the elements of the first list minus
     * every occurrence of elements of the given Iterable.
     * <pre class="groovyTestCase">
     * class AbcIterable implements Iterable<String> {
     *     Iterator<String> iterator() { "abc".iterator() }
     * }
     * assert "backtrack".toList() - new AbcIterable() == ["k", "t", "r", "k"]
     * </pre>
     *
     * @param self     a List
     * @param removeMe an Iterable of elements to remove
     * @return a List with the supplied elements removed
     * @since 1.8.7
     */
    public static <T> List<T> minus(List<T> self, Iterable<?> removeMe) {
        return minus(self, toList(removeMe));
    }

    /**
     * Create a new List composed of the elements of the first list minus every occurrence of the
     * given element to remove.
     * <pre class="groovyTestCase">assert ["a", 5, 5, true] - 5 == ["a", true]</pre>
     *
     * @param self     a List object
     * @param removeMe an element to remove from the list
     * @return the resulting List with the given element removed
     * @since 1.0
     */
    public static <T> List<T> minus(List<T> self, Object removeMe) {
        List<T> ansList = new ArrayList<T>();
        for (T t : self) {
            if (!coercedEquals(t, removeMe)) ansList.add(t);
        }
        return ansList;
    }

    /**
     * Create a new object array composed of the elements of the first array
     * minus the element to remove.
     *
     * @param self    an object array
     * @param removeMe an element to remove from the array
     * @return a new array with the operand removed
     * @since 1.5.5
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] minus(T[] self, Object removeMe) {
        return (T[]) minus(toList(self), removeMe).toArray();
    }

    /**
     * Create a Map composed of the entries of the first map minus the
     * entries of the given map.
     *
     * @param self     a map object
     * @param removeMe the entries to remove from the map
     * @return the resulting map
     * @since 1.7.4
     */
    public static <K,V> Map<K,V> minus(Map<K,V> self, Map removeMe) {
        final Map<K,V> ansMap = createSimilarMap(self);
        ansMap.putAll(self);
        if (removeMe != null && removeMe.size() > 0) {
            for (Map.Entry<K, V> e1 : self.entrySet()) {
                for (Object e2 : removeMe.entrySet()) {
                    if (DefaultTypeTransformation.compareEqual(e1, e2)) {
                        ansMap.remove(e1.getKey());
                    }
                }
            }
        }
        return ansMap;
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
    public static Collection<?> flatten(Collection<?> self) {
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
    public static <T> Collection<T> flatten(Collection<T> self, Closure<? extends T> flattenUsing) {
        return flatten(self, createSimilarCollection(self), flattenUsing);
    }

    private static <T> Collection<T> flatten(Collection elements, Collection<T> addTo, Closure<? extends T> flattenUsing) {
        for (Object element : elements) {
            if (element instanceof Collection) {
                flatten((Collection) element, addTo, flattenUsing);
            } else if (element != null && element.getClass().isArray()) {
                flatten(DefaultTypeTransformation.arrayAsCollection(element), addTo, flattenUsing);
            } else {
                T flattened = flattenUsing.call(new Object[]{element});
                boolean returnedSelf = flattened == element;
                if (!returnedSelf && flattened instanceof Collection) {
                    List<?> list = toList((Collection<?>) flattened);
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
     * Overloads the left shift operator to provide an easy way to put
     * one maps entries into another map. This allows the compact syntax
     * <code>map1 << map2</code>; otherwise it's just a synonym for
     * <code>putAll</code> though it returns the original map rather than
     * being a <code>void</code> method. Example usage:
     * <pre class="groovyTestCase">def map = [a:1, b:2]
     * map << [c:3, d:4]
     * assert map == [a:1, b:2, c:3, d:4]</pre>
     *
     * @param self  a Map
     * @param other another Map whose entries should be added to the original Map.
     * @return same map, after the values have been added to it.
     * @since 1.7.2
     */
    public static <K, V> Map<K, V> leftShift(Map<K, V> self, Map<K, V> other) {
        self.putAll(other);
        return self;
    }

    /**
     * Overloads the left shift operator to provide an easy way to append multiple
     * objects as string representations to a String.
     *
     * @param self  a String
     * @param value an Object
     * @return a StringBuffer built from this string
     * @since 1.0
     */
    public static StringBuffer leftShift(String self, Object value) {
        return new StringBuffer(self).append(value);
    }

    /**
     * Overloads the left shift operator to provide an easy way to append multiple
     * objects as string representations to a CharSequence.
     *
     * @param self  a CharSequence
     * @param value an Object
     * @return a StringBuilder built from this CharSequence
     * @since 1.8.2
     */
    public static StringBuilder leftShift(CharSequence self, Object value) {
        return new StringBuilder(self).append(value);
    }

    /**
     * Overloads the left shift operator to provide syntactic sugar for appending to a StringBuilder.
     *
     * @param self  a StringBuilder
     * @param value an Object
     * @return the original StringBuilder
     * @since 1.8.2
     */
    public static StringBuilder leftShift(StringBuilder self, Object value) {
        self.append(value);
        return self;
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
     * @see java.lang.reflect.Array#getLength(java.lang.Object)
     * @since 1.5.0
     */
    public static int size(boolean[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a byte array
     * @return the length of the array
     * @see java.lang.reflect.Array#getLength(java.lang.Object)
     * @since 1.0
     */
    public static int size(byte[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a char array
     * @return the length of the array
     * @see java.lang.reflect.Array#getLength(java.lang.Object)
     * @since 1.0
     */
    public static int size(char[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a short array
     * @return the length of the array
     * @see java.lang.reflect.Array#getLength(java.lang.Object)
     * @since 1.0
     */
    public static int size(short[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array an int array
     * @return the length of the array
     * @see java.lang.reflect.Array#getLength(java.lang.Object)
     * @since 1.0
     */
    public static int size(int[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a long array
     * @return the length of the array
     * @see java.lang.reflect.Array#getLength(java.lang.Object)
     * @since 1.0
     */
    public static int size(long[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a float array
     * @return the length of the array
     * @see java.lang.reflect.Array#getLength(java.lang.Object)
     * @since 1.0
     */
    public static int size(float[] array) {
        return Array.getLength(array);
    }

    /**
     * Allows arrays to behave similar to collections.
     * @param array a double array
     * @return the length of the array
     * @see java.lang.reflect.Array#getLength(java.lang.Object)
     * @since 1.0
     */
    public static int size(double[] array) {
        return Array.getLength(array);
    }

    /**
     * Converts this array to a List of the same size, with each element
     * added to the list.
     *
     * @param array a byte array
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

    /**
     * Converts this array to a Set, with each unique element
     * added to the set.
     *
     * @param array a byte array
     * @return a set containing the unique contents of this array.
     * @since 1.8.0
     */
    @SuppressWarnings("unchecked")
    public static Set<Byte> toSet(byte[] array) {
        return toSet(DefaultTypeTransformation.primitiveArrayToList(array));
    }

    /**
     * Converts this array to a Set, with each unique element
     * added to the set.
     *
     * @param array a boolean array
     * @return a set containing the unique contents of this array.
     * @since 1.8.0
     */
    @SuppressWarnings("unchecked")
    public static Set<Boolean> toSet(boolean[] array) {
        return toSet(DefaultTypeTransformation.primitiveArrayToList(array));
    }

    /**
     * Converts this array to a Set, with each unique element
     * added to the set.
     *
     * @param array a char array
     * @return a set containing the unique contents of this array.
     * @since 1.8.0
     */
    @SuppressWarnings("unchecked")
    public static Set<Character> toSet(char[] array) {
        return toSet(DefaultTypeTransformation.primitiveArrayToList(array));
    }

    /**
     * Converts this array to a Set, with each unique element
     * added to the set.
     *
     * @param array a short array
     * @return a set containing the unique contents of this array.
     * @since 1.8.0
     */
    @SuppressWarnings("unchecked")
    public static Set<Short> toSet(short[] array) {
        return toSet(DefaultTypeTransformation.primitiveArrayToList(array));
    }

    /**
     * Converts this array to a Set, with each unique element
     * added to the set.
     *
     * @param array an int array
     * @return a set containing the unique contents of this array.
     * @since 1.8.0
     */
    @SuppressWarnings("unchecked")
    public static Set<Integer> toSet(int[] array) {
        return toSet(DefaultTypeTransformation.primitiveArrayToList(array));
    }

    /**
     * Converts this array to a Set, with each unique element
     * added to the set.
     *
     * @param array a long array
     * @return a set containing the unique contents of this array.
     * @since 1.8.0
     */
    @SuppressWarnings("unchecked")
    public static Set<Long> toSet(long[] array) {
        return toSet(DefaultTypeTransformation.primitiveArrayToList(array));
    }

    /**
     * Converts this array to a Set, with each unique element
     * added to the set.
     *
     * @param array a float array
     * @return a set containing the unique contents of this array.
     * @since 1.8.0
     */
    @SuppressWarnings("unchecked")
    public static Set<Float> toSet(float[] array) {
        return toSet(DefaultTypeTransformation.primitiveArrayToList(array));
    }

    /**
     * Converts this array to a Set, with each unique element
     * added to the set.
     *
     * @param array a double array
     * @return a set containing the unique contents of this array.
     * @since 1.8.0
     */
    @SuppressWarnings("unchecked")
    public static Set<Double> toSet(double[] array) {
        return toSet(DefaultTypeTransformation.primitiveArrayToList(array));
    }

    /**
     * Convert a Collection to a Set. Always returns a new Set
     * even if the Collection is already a Set.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * def result = [1, 2, 2, 2, 3].toSet()
     * assert result instanceof Set
     * assert result == [1, 2, 3] as Set
     * </pre>
     *
     * @param self a collection
     * @return a Set
     * @since 1.8.0
     */
    public static <T> Set<T> toSet(Collection<T> self) {
        Set<T> answer = new HashSet<T>(self.size());
        answer.addAll(self);
        return answer;
    }

    /**
     * Convert an iterator to a Set. The iterator will become
     * exhausted of elements after making this conversion.
     *
     * @param self an iterator
     * @return a Set
     * @since 1.8.0
     */
    public static <T> Set<T> toSet(Iterator<T> self) {
        Set<T> answer = new HashSet<T>();
        while (self.hasNext()) {
            answer.add(self.next());
        }
        return answer;
    }

    /**
     * Convert an enumeration to a Set.
     *
     * @param self an enumeration
     * @return a Set
     * @since 1.8.0
     */
    public static <T> Set<T> toSet(Enumeration<T> self) {
        Set<T> answer = new HashSet<T>();
        while (self.hasMoreElements()) {
            answer.add(self.nextElement());
        }
        return answer;
    }

    /**
     * Implements the getAt(int) method for primitive type arrays.
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
     * Implements the getAt(Range) method for primitive type arrays.
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
     * Implements the getAt(Collection) method for primitive type arrays.  Each
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
     * Implements the setAt(int idx) method for primitive type arrays.
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
     * then the result is true otherwise it is false.
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
     * Identity conversion which returns Boolean.TRUE for a true Boolean and Boolean.FALSE for a false Boolean.
     *
     * @param self a Boolean
     * @return the original Boolean
     * @since 1.7.6
     */
    public static Boolean toBoolean(Boolean self) {
        return self;
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
     * Convenience method to split a CharSequence (with whitespace as delimiter).
     * Similar to tokenize, but returns an Array of CharSequence instead of a List.
     *
     * @param self the CharSequence to split
     * @return CharSequence[] result of split
     * @see #split(String)
     * @since 1.8.2
     */
    public static CharSequence[] split(CharSequence self) {
        return split(self.toString());
    }

    /**
     * Convenience method to capitalize the first letter of a string
     * (typically the first letter of a word). Example usage:
     * <pre class="groovyTestCase">
     * assert 'h'.capitalize() == 'H'
     * assert 'hello'.capitalize() == 'Hello'
     * assert 'hello world'.capitalize() == 'Hello world'
     * assert 'Hello World' ==
     *     'hello world'.split(' ').collect{ it.capitalize() }.join(' ')
     * </pre>
     *
     * @param self The string to capitalize
     * @return The capitalized String
     * @since 1.7.3
     */
    public static String capitalize(String self) {
        if (self == null || self.length() == 0) return self;
        return Character.toUpperCase(self.charAt(0)) + self.substring(1);
    }

    /**
     * Convenience method to capitalize the first letter of a CharSequence.
     *
     * @param self The CharSequence to capitalize
     * @return The capitalized CharSequence
     * @see #capitalize(String)
     * @since 1.8.2
     */
    public static CharSequence capitalize(CharSequence self) {
        return capitalize(self.toString());
    }

    /**
     * Expands all tabs into spaces with tabStops of size 8.
     *
     * @param self A String to expand
     * @return The expanded String
     * @since 1.7.3
     * @see #expand(java.lang.String, int)
     */
    public static String expand(String self) {
        return expand(self, 8);
    }

    /**
     * Expands all tabs into spaces with tabStops of size 8.
     *
     * @param self A CharSequence to expand
     * @return The expanded CharSequence
     * @see #expand(java.lang.String)
     * @since 1.8.2
     */
    public static CharSequence expand(CharSequence self) {
        return expand(self.toString(), 8);
    }

    /**
     * Expands all tabs into spaces. If the String has multiple
     * lines, expand each line - restarting tab stops at the start
     * of each line.
     *
     * @param self A String to expand
     * @param tabStop The number of spaces a tab represents
     * @return The expanded String
     * @since 1.7.3
     */
    public static String expand(String self, int tabStop) {
        if (self.length() == 0) return self;
        try {
            StringBuilder builder = new StringBuilder();
            for (String line : readLines(self)) {
                builder.append(expandLine(line, tabStop));
                builder.append("\n");
            }
            // remove the normalized ending line ending if it was not present
            if (!self.endsWith("\n")) {
                builder.deleteCharAt(builder.length() - 1);
            }
            return builder.toString();
        } catch (IOException e) {
            /* ignore */
        }
        return self;
    }

    /**
     * Expands all tabs into spaces. If the CharSequence has multiple
     * lines, expand each line - restarting tab stops at the start
     * of each line.
     *
     * @param self A CharSequence to expand
     * @param tabStop The number of spaces a tab represents
     * @return The expanded CharSequence
     * @see #expand(String, int)
     * @since 1.8.2
     */
    public static CharSequence expand(CharSequence self, int tabStop) {
        return expand(self.toString(), tabStop);
    }

    /**
     * Expands all tabs into spaces. Assumes the String represents a single line of text.
     *
     * @param self A line to expand
     * @param tabStop The number of spaces a tab represents
     * @return The expanded String
     * @since 1.7.3
     */
    public static String expandLine(String self, int tabStop) {
        int index;
        while ((index = self.indexOf('\t')) != -1) {
            StringBuilder builder = new StringBuilder(self);
            int count = tabStop - index % tabStop;
            builder.deleteCharAt(index);
            for (int i = 0; i < count; i++) builder.insert(index, " ");
            self = builder.toString();
        }
        return self;
    }

    /**
     * Expands all tabs into spaces. Assumes the CharSequence represents a single line of text.
     *
     * @param self A line to expand
     * @param tabStop The number of spaces a tab represents
     * @return The expanded CharSequence
     * @see #expandLine(String, int)
     * @since 1.8.2
     */
    public static CharSequence expandLine(CharSequence self, int tabStop) {
        return expandLine(self.toString(), tabStop);
    }

    /**
     * Replaces sequences of whitespaces with tabs using tabStops of size 8.
     *
     * @param self A String to unexpand
     * @return The unexpanded String
     * @since 1.7.3
     * @see #unexpand(java.lang.String, int)
     */
    public static String unexpand(String self) {
        return unexpand(self, 8);
    }

    /**
     * Replaces sequences of whitespaces with tabs using tabStops of size 8.
     *
     * @param self A CharSequence to unexpand
     * @return The unexpanded CharSequence
     * @see #unexpand(java.lang.String)
     * @since 1.8.2
     */
    public static CharSequence unexpand(CharSequence self) {
        return unexpand(self.toString());
    }

    /**
     * Replaces sequences of whitespaces with tabs.
     *
     * @param self A String to unexpand
     * @param tabStop The number of spaces a tab represents
     * @return The unexpanded String
     * @since 1.7.3
     */
    public static String unexpand(String self, int tabStop) {
        if (self.length() == 0) return self;
        try {
            StringBuilder builder = new StringBuilder();
            for (String line : readLines(self)) {
                builder.append(unexpandLine(line, tabStop));
                builder.append("\n");
            }
            // remove the normalized ending line ending if it was not present
            if (!self.endsWith("\n")) {
                builder.deleteCharAt(builder.length() - 1);
            }
            return builder.toString();
        } catch (IOException e) {
            /* ignore */
        }
        return self;
    }

    /**
     * Replaces sequences of whitespaces with tabs.
     *
     * @param self A CharSequence to unexpand
     * @param tabStop The number of spaces a tab represents
     * @return The unexpanded CharSequence
     * @see #unexpand(String, int)
     * @since 1.8.2
     */
    public static CharSequence unexpand(CharSequence self, int tabStop) {
        return unexpand(self.toString(), tabStop);
    }

    /**
     * Replaces sequences of whitespaces with tabs within a line.
     *
     * @param self A line to unexpand
     * @param tabStop The number of spaces a tab represents
     * @return The unexpanded String
     * @since 1.7.3
     */
    public static String unexpandLine(String self, int tabStop) {
        StringBuilder builder = new StringBuilder(self);
        int index = 0;
        while (index + tabStop < builder.length()) {
            // cut original string in tabstop-length pieces
            String piece = builder.substring(index, index + tabStop);
            // count trailing whitespace characters
            int count = 0;
            while ((count < tabStop) && (Character.isWhitespace(piece.charAt(tabStop - (count + 1)))))
                count++;
            // replace if whitespace was found
            if (count > 0) {
                piece = piece.substring(0, tabStop - count) + '\t';
                builder.replace(index, index + tabStop, piece);
                index = index + tabStop - (count - 1);
            } else
                index = index + tabStop;
        }
        return builder.toString();
    }

    /**
     * Replaces sequences of whitespaces with tabs within a line.
     *
     * @param self A line to unexpand
     * @param tabStop The number of spaces a tab represents
     * @return The unexpanded CharSequence
     * @see #unexpandLine(String, int)
     * @since 1.8.2
     */
    public static CharSequence unexpandLine(CharSequence self, int tabStop) {
        return unexpandLine(self.toString(), tabStop);
    }

    /**
     * Convenience method to split a GString (with whitespace as delimiter).
     *
     * @param self the GString to split
     * @return String[] result of split
     * @see #split(java.lang.String)
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
    @SuppressWarnings("unchecked")
    public static List<String> tokenize(String self, String token) {
        return InvokerHelper.asList(new StringTokenizer(self, token));
    }

    /**
     * Tokenize a CharSequence based on the given CharSequence delimiter.
     *
     * @param self  a CharSequence
     * @param token the delimiter
     * @return a List of tokens
     * @see #tokenize(String, String)
     * @since 1.8.2
     */
    public static List<CharSequence> tokenize(CharSequence self, CharSequence token) {
        return new ArrayList<CharSequence>(tokenize(self.toString(), token.toString()));
    }

    /**
     * Tokenize a String based on the given character delimiter.
     * For example:
     * <pre class="groovyTestCase">
     * char pathSep = ':'
     * assert "/tmp:/usr".tokenize(pathSep) == ["/tmp", "/usr"]
     * </pre>
     *
     * @param self  a String
     * @param token the delimiter
     * @return a List of tokens
     * @see java.util.StringTokenizer#StringTokenizer(java.lang.String, java.lang.String)
     * @since 1.7.2
     */
    public static List<String> tokenize(String self, Character token) {
        return tokenize(self, token.toString());
    }

    /**
     * Tokenize a CharSequence based on the given character delimiter.
     *
     * @param self  a CharSequence
     * @param token the delimiter
     * @return a List of tokens
     * @see #tokenize(String, Character)
     * @since 1.8.2
     */
    public static List<CharSequence> tokenize(CharSequence self, Character token) {
        return tokenize(self, token.toString());
    }

    /**
     * Tokenize a String (with a whitespace as the delimiter).
     *
     * @param self a String
     * @return a List of tokens
     * @see java.util.StringTokenizer#StringTokenizer(java.lang.String)
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<String> tokenize(String self) {
        return InvokerHelper.asList(new StringTokenizer(self));
    }

    /**
     * Tokenize a CharSequence (with a whitespace as the delimiter).
     *
     * @param self a CharSequence
     * @return a List of tokens
     * @see #tokenize(String)
     * @since 1.8.2
     */
    public static List<CharSequence> tokenize(CharSequence self) {
        return new ArrayList<CharSequence>(tokenize(self.toString()));
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
     * Appends the String representation of the given operand to this string.
     *
     * @param left  a CharSequence
     * @param value any Object
     * @return the new CharSequence with the object appended
     * @since 1.8.2
     */
    public static CharSequence plus(CharSequence left, Object value) {
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
     * Remove a part of a CharSequence by replacing the first occurrence
     * of target within self with '' and returns the result.
     *
     * @param self   a CharSequence
     * @param target an object representing the part to remove
     * @return a CharSequence minus the part to be removed
     * @see #minus(String, Object)
     * @since 1.8.2
     */
    public static CharSequence minus(CharSequence self, Object target) {
        return minus(self.toString(), target);
    }

    /**
     * Provide an implementation of contains() like
     * {@link java.util.Collection#contains(java.lang.Object)} to make Strings more polymorphic.
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
     * Provide an implementation of contains() like
     * {@link java.util.Collection#contains(java.lang.Object)} to make CharSequences more polymorphic.
     *
     * @param self a CharSequence
     * @param text the CharSequence to look for
     * @return true if this CharSequence contains the given text
     * @see #contains(String, String)
     * @since 1.8.2
     */
    public static boolean contains(CharSequence self, CharSequence text) {
        return contains(self.toString(), text.toString());
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
    public static boolean contains(short[] self, Object value) {
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
    public static boolean contains(char[] self, Object value) {
        for (char next : self) {
            if (DefaultTypeTransformation.compareEqual(value, next)) return true;
        }
        return false;
    }

    /**
     * Checks whether the array contains the given value.
     *
     * @param self  the array within which we count the number of occurrences
     * @param value the value being searched for
     * @return the number of occurrences
     * @since 1.8.6
     */
    public static boolean contains(boolean[] self, Object value) {
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
    public static boolean contains(double[] self, Object value) {
        for (double next : self) {
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
    public static boolean contains(byte[] self, Object value) {
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
    public static boolean contains(Object[] self, Object value) {
        for (Object next : self) {
            if (DefaultTypeTransformation.compareEqual(value, next)) return true;
        }
        return false;
    }

    /**
     * Count the number of occurrences of a substring.
     *
     * @param self a String
     * @param text a substring
     * @return the number of occurrences of the given string inside this String
     * @since 1.0
     */
    public static int count(String self, String text) {
        int answer = 0;
        for (int idx = 0; true; idx++) {
            idx = self.indexOf(text, idx);
            if (idx >= 0) {
                ++answer;
		if (idx == self.length()) break;
            } else {
                break;
            }
        }
        return answer;
    }

    /**
     * Count the number of occurrences of a sub CharSequence.
     *
     * @param self a CharSequence
     * @param text a sub CharSequence
     * @return the number of occurrences of the given CharSequence inside this CharSequence
     * @see #count(String, String)
     * @since 1.8.2
     */
    public static int count(CharSequence self, CharSequence text) {
        return count(self.toString(), text.toString());
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
     * This method is called by the ++ operator for the class CharSequence.
     *
     * @param self a CharSequence
     * @return an incremented CharSequence
     * @see #next(String)
     * @since 1.8.2
     */
    public static CharSequence next(CharSequence self) {
        return next(self.toString());
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
     * This method is called by the -- operator for the class CharSequence.
     *
     * @param self a CharSequence
     * @return a CharSequence with a decremented digit at the end
     * @see #previous(String)
     * @since 1.8.2
     */
    public static CharSequence previous(CharSequence self) {
        return previous(self.toString());
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
     * @param commandArray an array of <code>String</code> containing the command name and
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
     * @param commandArray an array of <code>String</code> containing the command name and
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
     * @param commandArray an array of <code>String</code> containing the command name and
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
     * Repeat a CharSequence a certain number of times.
     *
     * @param self   a CharSequence to be repeated
     * @param factor the number of times the CharSequence should be repeated
     * @return a CharSequence composed of a repetition
     * @throws IllegalArgumentException if the number of repetitions is &lt; 0
     * @since 1.8.2
     */
    public static CharSequence multiply(CharSequence self, Number factor) {
        return multiply(self.toString(), factor);
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
     * @see #toMapString(java.util.Map)
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
        return toMapString(self, -1);
    }

    /**
     * Returns the string representation of this map.  The string displays the
     * contents of the map, i.e. <code>[one:1, two:2, three:3]</code>.
     *
     * @param self a Map
     * @param maxSize stop after approximately this many characters and append '...'
     * @return the string representation
     * @since 1.0
     */
    public static String toMapString(Map self, int maxSize) {
        return (self == null) ? "null" : InvokerHelper.toMapString(self, maxSize);
    }

    /**
     * Returns the string representation of the given collection.  The string
     * displays the contents of the collection, i.e.
     * <code>[1, 2, a]</code>.
     *
     * @param self a Collection
     * @return the string representation
     * @see #toListString(java.util.Collection)
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
        return toListString(self, -1);
    }

    /**
     * Returns the string representation of the given list.  The string
     * displays the contents of the list, similar to a list literal, i.e.
     * <code>[1, 2, a]</code>.
     *
     * @param self a Collection
     * @param maxSize stop after approximately this many characters and append '...'
     * @return the string representation
     * @since 1.7.3
     */
    public static String toListString(Collection self, int maxSize) {
        return (self == null) ? "null" : InvokerHelper.toListString(self, maxSize);
    }

    /**
     * Returns the string representation of this array's contents.
     *
     * @param self an Object[]
     * @return the string representation
     * @see #toArrayString(java.lang.Object[])
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
     * @see java.lang.Integer#valueOf(int)
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
     * @see java.lang.Integer#valueOf(int)
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
     * @see #plus(java.lang.Number, java.lang.Character)
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
     * @return the comparison of both numbers
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
     * is used in the multiplication (the ordinal value is the unicode
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
     * Power of a BigDecimal to an integer certain exponent.  If the
     * exponent is positive, call the BigDecimal.pow(int) method to
     * maintain precision. Called by the '**' operator.
     *
     * @param self     a BigDecimal
     * @param exponent an Integer exponent
     * @return a Number to the power of a the exponent
     */
    public static Number power(BigDecimal self, Integer exponent) {
        if (exponent >= 0) {
            return self.pow(exponent);
        } else {
            return power(self, (double) exponent);
        }
    }

    /**
     * Power of a BigInteger to an integer certain exponent.  If the
     * exponent is positive, call the BigInteger.pow(int) method to
     * maintain precision. Called by the '**' operator.
     *
     *  @param self     a BigInteger
     *  @param exponent an Integer exponent
     *  @return a Number to the power of a the exponent
     */
    public static Number power(BigInteger self, Integer exponent) {
        if (exponent >= 0) {
            return self.pow(exponent);
        } else {
            return power(self, (double) exponent);
        }
    }

    /**
     * Power of an integer to an integer certain exponent.  If the
     * exponent is positive, convert to a BigInteger and call
     * BigInteger.pow(int) method to maintain precision. Called by the
     * '**' operator.
     *
     *  @param self     an Integer
     *  @param exponent an Integer exponent
     *  @return a Number to the power of a the exponent
     */
    public static Number power(Integer self, Integer exponent) {
        if (exponent >= 0) {
            BigInteger answer = BigInteger.valueOf(self).pow(exponent);
            if (answer.compareTo(BI_INT_MIN) >= 0 && answer.compareTo(BI_INT_MAX) <= 0) {
                return answer.intValue();
            } else {
                return answer;
            }
        } else {
            return power(self, (double) exponent);
        }
    }

    /**
     * Power of a long to an integer certain exponent.  If the
     * exponent is positive, convert to a BigInteger and call
     * BigInteger.pow(int) method to maintain precision. Called by the
     * '**' operator.
     *
     * @param self     a Long
     * @param exponent an Integer exponent
     * @return a Number to the power of a the exponent
     */
    public static Number power(Long self, Integer exponent) {
        if (exponent >= 0) {
            BigInteger answer = BigInteger.valueOf(self).pow(exponent);
            if (answer.compareTo(BI_LONG_MIN) >= 0 && answer.compareTo(BI_LONG_MAX) <= 0) {
                return answer.longValue();
            } else {
                return answer;
            }
        } else {
            return power(self, (double) exponent);
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
            } else if(self1.compareTo(to1) != 0)
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
            } else if(self1.compareTo(to1) != 0)
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
            } else if(self1 != to1)
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
     * @see #isBigDecimal(java.lang.String)
     * @since 1.5.0
     */
    public static boolean isNumber(String self) {
        return isBigDecimal(self);
    }

    /**
     * Parse a CharSequence into an Integer
     *
     * @param self a CharSequence
     * @return an Integer
     * @see #toInteger(java.lang.String)
     * @since 1.8.2
     */
    public static Integer toInteger(CharSequence self) {
        return toInteger(self.toString());
    }

    /**
     * Parse a CharSequence into a Long
     *
     * @param self a CharSequence
     * @return a Long
     * @see #toLong(java.lang.String)
     * @since 1.8.2
     */
    public static Long toLong(CharSequence self) {
        return toLong(self.toString());
    }

    /**
     * Parse a CharSequence into a Short
     *
     * @param self a CharSequence
     * @return a Short
     * @see #toShort(java.lang.String)
     * @since 1.8.2
     */
    public static Short toShort(CharSequence self) {
        return toShort(self.toString());
    }

    /**
     * Parse a CharSequence into a Float
     *
     * @param self a CharSequence
     * @return a Float
     * @see #toFloat(java.lang.String)
     * @since 1.8.2
     */
    public static Float toFloat(CharSequence self) {
        return toFloat(self.toString());
    }

    /**
     * Parse a CharSequence into a Double
     *
     * @param self a CharSequence
     * @return a Double
     * @see #toDouble(java.lang.String)
     * @since 1.8.2
     */
    public static Double toDouble(CharSequence self) {
        return toDouble(self.toString());
    }

    /**
     * Parse a CharSequence into a BigInteger
     *
     * @param self a CharSequence
     * @return a BigInteger
     * @see #toBigInteger(java.lang.String)
     * @since 1.8.2
     */
    public static BigInteger toBigInteger(CharSequence self) {
        return toBigInteger(self.toString());
    }

    /**
     * Parse a CharSequence into a BigDecimal
     *
     * @param self a CharSequence
     * @return a BigDecimal
     * @see #toBigDecimal(java.lang.String)
     * @since 1.8.2
     */
    public static BigDecimal toBigDecimal(CharSequence self) {
        return toBigDecimal(self.toString());
    }

    /**
     * Determine if a CharSequence can be parsed as an Integer.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     * @see #isInteger(java.lang.String)
     * @since 1.8.2
     */
    public static boolean isInteger(CharSequence self) {
        return isInteger(self.toString());
    }

    /**
     * Determine if a CharSequence can be parsed as a Long.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     * @see #isLong(java.lang.String)
     * @since 1.8.2
     */
    public static boolean isLong(CharSequence self) {
        return isLong(self.toString());
    }

    /**
     * Determine if a CharSequence can be parsed as a Float.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     * @see #isFloat(java.lang.String)
     * @since 1.8.2
     */
    public static boolean isFloat(CharSequence self) {
        return isFloat(self.toString());
    }

    /**
     * Determine if a CharSequence can be parsed as a Double.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     * @see #isDouble(java.lang.String)
     * @since 1.8.2
     */
    public static boolean isDouble(CharSequence self) {
        return isDouble(self.toString());
    }

    /**
     * Determine if a CharSequence can be parsed as a BigInteger.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     * @see #isBigInteger(java.lang.String)
     * @since 1.8.2
     */
    public static boolean isBigInteger(CharSequence self) {
        return isBigInteger(self.toString());
    }

    /**
     * Determine if a CharSequence can be parsed as a BigDecimal.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     * @see #isBigDecimal(java.lang.String)
     * @since 1.8.2
     */
    public static boolean isBigDecimal(CharSequence self) {
        return isBigDecimal(self.toString());
    }

    /**
     * Determine if a CharSequence can be parsed as a Number.
     * Synonym for 'isBigDecimal()'.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     * @see #isNumber(java.lang.String)
     * @since 1.8.2
     */
    public static boolean isNumber(CharSequence self) {
        return isNumber(self.toString());
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
        // With Bigs and other unknowns, this is likely to be the same.

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
     * {@link #asType(java.lang.Object, java.lang.Class)}:
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
    @SuppressWarnings("unchecked")
    public static <T> T asType(Number self, Class<T> c) {
        if (c == BigDecimal.class) {
            return (T) toBigDecimal(self);
        } else if (c == BigInteger.class) {
            return (T) toBigInteger(self);
        } else if (c == Double.class) {
            return (T) toDouble(self);
        } else if (c == Float.class) {
            return (T) toFloat(self);
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
     * Logical implication of two boolean operators
     *
     * @param left left operator
     * @param right right operator
     * @return result of logical implication
     * @since 1.8.3
     */
    public static Boolean implies(Boolean left, Boolean right) {
        return !left || right;
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
     * @see #withStream(java.io.OutputStream, groovy.lang.Closure)
     * @since 1.5.0
     */
    public static <T> T withObjectOutputStream(File file, Closure<T> closure) throws IOException {
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
     * @see #withStream(java.io.OutputStream, groovy.lang.Closure)
     * @since 1.5.0
     */
    public static <T> T withObjectOutputStream(OutputStream outputStream, Closure<T> closure) throws IOException {
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
     * @see #eachObject(java.io.ObjectInputStream, groovy.lang.Closure)
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
     * @see #withStream(java.io.InputStream, groovy.lang.Closure)
     * @since 1.5.2
     */
    public static <T> T withObjectInputStream(File file, Closure<T> closure) throws IOException {
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
     * @see #withStream(java.io.InputStream, groovy.lang.Closure)
     * @since 1.5.2
     */
    public static <T> T withObjectInputStream(File file, ClassLoader classLoader, Closure<T> closure) throws IOException {
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
     * @see #withStream(java.io.InputStream, groovy.lang.Closure)
     * @since 1.5.0
     */
    public static <T> T withObjectInputStream(InputStream inputStream, Closure<T> closure) throws IOException {
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
     * @see #withStream(java.io.InputStream, groovy.lang.Closure)
     * @since 1.5.0
     */
    public static <T> T withObjectInputStream(InputStream inputStream, ClassLoader classLoader, Closure<T> closure) throws IOException {
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
     * @see #eachLine(java.lang.String, int, groovy.lang.Closure)
     * @since 1.5.5
     */
    public static <T> T eachLine(String self, Closure<T> closure) throws IOException {
        return eachLine(self, 0, closure);
    }

    /**
     * Iterates through this CharSequence line by line.  Each line is passed
     * to the given 1 or 2 arg closure. If a 2 arg closure is found
     * the line count is passed as the second argument.
     *
     * @param self    a CharSequence
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     * @see #eachLine(java.lang.String, groovy.lang.Closure)
     * @since 1.8.2
     */
    public static <T> T eachLine(CharSequence self, Closure<T> closure) throws IOException {
        return eachLine(self.toString(), closure);
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
    public static <T> T eachLine(String self, int firstLine, Closure<T> closure) throws IOException {
        int count = firstLine;
        T result = null;
        for (String line : readLines(self)) {
            result = callClosureForLine(closure, line, count);
            count++;
        }
        return result;
    }

    /**
     * Iterates through this CharSequence line by line.  Each line is passed
     * to the given 1 or 2 arg closure. If a 2 arg closure is found
     * the line count is passed as the second argument.
     *
     * @param self    a CharSequence
     * @param firstLine the line number value used for the first line (default is 1, set to 0 to start counting from 0)
     * @param closure a closure (arg 1 is line, optional arg 2 is line number)
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     * @see #eachLine(java.lang.String, int, groovy.lang.Closure)
     * @since 1.8.2
     */
    public static <T> T eachLine(CharSequence self, int firstLine, Closure<T> closure) throws IOException {
        return eachLine(self.toString(), firstLine, closure);
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
    public static <T> T eachLine(File self, Closure<T> closure) throws IOException {
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
     * @see #eachLine(java.io.File, java.lang.String, int, groovy.lang.Closure)
     * @since 1.6.8
     */
    public static <T> T eachLine(File self, String charset, Closure<T> closure) throws IOException {
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
    public static <T> T eachLine(File self, int firstLine, Closure<T> closure) throws IOException {
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
    public static <T> T eachLine(File self, String charset, int firstLine, Closure<T> closure) throws IOException {
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
     * @see #eachLine(java.io.InputStream, java.lang.String, int, groovy.lang.Closure)
     * @since 1.5.5
     */
    public static <T> T eachLine(InputStream stream, String charset, Closure<T> closure) throws IOException {
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
    public static <T> T eachLine(InputStream stream, String charset, int firstLine, Closure<T> closure) throws IOException {
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
    public static <T> T eachLine(InputStream stream, Closure<T> closure) throws IOException {
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
    public static <T> T eachLine(InputStream stream, int firstLine, Closure<T> closure) throws IOException {
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
    public static <T> T eachLine(URL url, Closure<T> closure) throws IOException {
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
    public static <T> T eachLine(URL url, int firstLine, Closure<T> closure) throws IOException {
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
     * @see #eachLine(java.net.URL, java.lang.String, int, groovy.lang.Closure)
     * @since 1.5.6
     */
    public static <T> T eachLine(URL url, String charset, Closure<T> closure) throws IOException {
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
    public static <T> T eachLine(URL url, String charset, int firstLine, Closure<T> closure) throws IOException {
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
    public static <T> T eachLine(Reader self, Closure<T> closure) throws IOException {
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
    public static <T> T eachLine(Reader self, int firstLine, Closure<T> closure) throws IOException {
        BufferedReader br;
        int count = firstLine;
        T result = null;

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
     * @see #splitEachLine(java.io.Reader, java.lang.String, groovy.lang.Closure)
     * @since 1.5.5
     */
    public static <T> T splitEachLine(File self, String regex, Closure<T> closure) throws IOException {
        return splitEachLine(newReader(self), regex, closure);
    }

    /**
     * Iterates through this file line by line, splitting each line using
     * the given separator Pattern. For each line, the given closure is called with
     * a single parameter being the list of strings computed by splitting the line
     * around matches of the given regular expression Pattern.
     * Finally the resources used for processing the file are closed.
     *
     * @param self    a File
     * @param pattern the regular expression Pattern for the delimiter
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #splitEachLine(java.io.Reader, java.util.regex.Pattern, groovy.lang.Closure)
     * @since 1.6.8
     */
    public static <T> T splitEachLine(File self, Pattern pattern, Closure<T> closure) throws IOException {
        return splitEachLine(newReader(self), pattern, closure);
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
     * @see #splitEachLine(java.io.Reader, java.lang.String, groovy.lang.Closure)
     * @since 1.6.8
     */
    public static <T> T splitEachLine(File self, String regex, String charset, Closure<T> closure) throws IOException {
        return splitEachLine(newReader(self, charset), regex, closure);
    }

    /**
     * Iterates through this file line by line, splitting each line using
     * the given regex separator Pattern. For each line, the given closure is called with
     * a single parameter being the list of strings computed by splitting the line
     * around matches of the given regular expression.
     * Finally the resources used for processing the file are closed.
     *
     * @param self    a File
     * @param pattern the regular expression Pattern for the delimiter
     * @param charset opens the file with a specified charset
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #splitEachLine(java.io.Reader, java.util.regex.Pattern, groovy.lang.Closure)
     * @since 1.6.8
     */
    public static <T> T splitEachLine(File self, Pattern pattern, String charset, Closure<T> closure) throws IOException {
        return splitEachLine(newReader(self, charset), pattern, closure);
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
     * @see #splitEachLine(java.io.Reader, java.lang.String, groovy.lang.Closure)
     * @since 1.6.8
     */
    public static <T> T splitEachLine(URL self, String regex, Closure<T> closure) throws IOException {
        return splitEachLine(newReader(self), regex, closure);
    }

    /**
     * Iterates through the input stream associated with this URL line by line, splitting each line using
     * the given regex separator Pattern. For each line, the given closure is called with
     * a single parameter being the list of strings computed by splitting the line
     * around matches of the given regular expression.
     * Finally the resources used for processing the URL are closed.
     *
     * @param self    a URL to open and read
     * @param pattern the regular expression Pattern for the delimiter
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #splitEachLine(java.io.Reader, java.util.regex.Pattern, groovy.lang.Closure)
     * @since 1.6.8
     */
    public static <T> T splitEachLine(URL self, Pattern pattern, Closure<T> closure) throws IOException {
        return splitEachLine(newReader(self), pattern, closure);
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
     * @see #splitEachLine(java.io.Reader, java.lang.String, groovy.lang.Closure)
     * @since 1.6.8
     */
    public static <T> T splitEachLine(URL self, String regex, String charset, Closure<T> closure) throws IOException {
        return splitEachLine(newReader(self, charset), regex, closure);
    }

    /**
     * Iterates through the input stream associated with this URL line by line, splitting each line using
     * the given regex separator Pattern. For each line, the given closure is called with
     * a single parameter being the list of strings computed by splitting the line
     * around matches of the given regular expression.
     * Finally the resources used for processing the URL are closed.
     *
     * @param self    a URL to open and read
     * @param pattern the regular expression Pattern for the delimiter
     * @param charset opens the file with a specified charset
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #splitEachLine(java.io.Reader, java.util.regex.Pattern, groovy.lang.Closure)
     * @since 1.6.8
     */
    public static <T> T splitEachLine(URL self, Pattern pattern, String charset, Closure<T> closure) throws IOException {
        return splitEachLine(newReader(self, charset), pattern, closure);
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
     * @see java.lang.String#split(java.lang.String)
     * @since 1.5.5
     */
    public static <T> T splitEachLine(Reader self, String regex, Closure<T> closure) throws IOException {
        return splitEachLine(self, Pattern.compile(regex), closure);
    }

    /**
     * Iterates through the given reader line by line, splitting each line using
     * the given regex separator Pattern. For each line, the given closure is called with
     * a single parameter being the list of strings computed by splitting the line
     * around matches of the given regular expression.  The Reader is closed afterwards.
     * <p/>
     * Here is an example:
     * <pre>
     * def s = 'The 3 quick\nbrown 4 fox'
     * def result = ''
     * new StringReader(s).splitEachLine(~/\d/){ parts ->
     *     result += "${parts[0]}_${parts[1]}|"
     * }
     * assert result == 'The _ quick|brown _ fox|'
     * </pre>
     *
     * @param self    a Reader, closed after the method returns
     * @param pattern the regular expression Pattern for the delimiter
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @return the last value returned by the closure
     * @see java.lang.String#split(java.lang.String)
     * @since 1.6.8
     */
    public static <T> T splitEachLine(Reader self, Pattern pattern, Closure<T> closure) throws IOException {
        BufferedReader br;
        T result = null;

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
                    List vals = Arrays.asList(pattern.split(line));
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
     * @param regex   the delimiting regular expression
     * @param charset opens the stream with a specified charset
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @return the last value returned by the closure
     * @see #splitEachLine(java.io.Reader, java.lang.String, groovy.lang.Closure)
     * @since 1.5.5
     */
    public static <T> T splitEachLine(InputStream stream, String regex, String charset, Closure<T> closure) throws IOException {
        return splitEachLine(new BufferedReader(new InputStreamReader(stream, charset)), regex, closure);
    }

    /**
     * Iterates through the given InputStream line by line using the specified
     * encoding, splitting each line using the given separator Pattern.  The list of tokens
     * for each line is then passed to the given closure. Finally, the stream
     * is closed.
     *
     * @param stream  an InputStream
     * @param pattern the regular expression Pattern for the delimiter
     * @param charset opens the stream with a specified charset
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #splitEachLine(java.io.Reader, java.util.regex.Pattern, groovy.lang.Closure)
     * @since 1.6.8
     */
    public static <T> T splitEachLine(InputStream stream, Pattern pattern, String charset, Closure<T> closure) throws IOException {
        return splitEachLine(new BufferedReader(new InputStreamReader(stream, charset)), pattern, closure);
    }

    /**
     * Iterates through the given InputStream line by line, splitting each line using
     * the given separator.  The list of tokens for each line is then passed to
     * the given closure. The stream is closed before the method returns.
     *
     * @param stream  an InputStream
     * @param regex   the delimiting regular expression
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @return the last value returned by the closure
     * @see #splitEachLine(java.io.Reader, java.lang.String, groovy.lang.Closure)
     * @since 1.5.6
     */
    public static <T> T splitEachLine(InputStream stream, String regex, Closure<T> closure) throws IOException {
        return splitEachLine(new BufferedReader(new InputStreamReader(stream)), regex, closure);
    }

    /**
     * Iterates through the given InputStream line by line, splitting each line using
     * the given separator Pattern.  The list of tokens for each line is then passed to
     * the given closure. The stream is closed before the method returns.
     *
     * @param stream  an InputStream
     * @param pattern the regular expression Pattern for the delimiter
     * @param closure a closure
     * @throws IOException if an IOException occurs.
     * @return the last value returned by the closure
     * @see #splitEachLine(java.io.Reader, java.util.regex.Pattern, groovy.lang.Closure)
     * @since 1.6.8
     */
    public static <T> T splitEachLine(InputStream stream, Pattern pattern, Closure<T> closure) throws IOException {
        return splitEachLine(new BufferedReader(new InputStreamReader(stream)), pattern, closure);
    }

    /**
     * Iterates through the given String line by line, splitting each line using
     * the given separator.  The list of tokens for each line is then passed to
     * the given closure.
     *
     * @param self    a String
     * @param regex   the delimiting regular expression
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @see java.lang.String#split(java.lang.String)
     * @since 1.5.5
     */
    public static <T> T splitEachLine(String self, String regex, Closure<T> closure) throws IOException {
        return splitEachLine(self, Pattern.compile(regex), closure);
    }

    /**
     * Iterates through the given CharSequence line by line, splitting each line using
     * the given separator.  The list of tokens for each line is then passed to
     * the given closure.
     *
     * @param self    a CharSequence
     * @param regex   the delimiting regular expression
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @see #splitEachLine(String, String, Closure)
     * @since 1.8.2
     */
    public static <T> T splitEachLine(CharSequence self, CharSequence regex, Closure<T> closure) throws IOException {
        return splitEachLine(self.toString(), regex.toString(), closure);
    }

    /**
     * Iterates through the given String line by line, splitting each line using
     * the given separator Pattern.  The list of tokens for each line is then passed to
     * the given closure.
     *
     * @param self    a String
     * @param pattern the regular expression Pattern for the delimiter
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     * @see java.util.regex.Pattern#split(java.lang.CharSequence)
     * @since 1.6.8
     */
    public static <T> T splitEachLine(String self, Pattern pattern, Closure<T> closure) throws IOException {
        final List<String> list = readLines(self);
        T result = null;
        for (String line : list) {
            List vals = Arrays.asList(pattern.split(line));
            result = closure.call(vals);
        }
        return result;
    }

    /**
     * Iterates through the given CharSequence line by line, splitting each line using
     * the given separator Pattern.  The list of tokens for each line is then passed to
     * the given closure.
     *
     * @param self    a CharSequence
     * @param pattern the regular expression Pattern for the delimiter
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     * @see #splitEachLine(String, Pattern, Closure)
     * @since 1.8.2
     */
    public static <T> T splitEachLine(CharSequence self, Pattern pattern, Closure<T> closure) throws IOException {
        return splitEachLine(self.toString(), pattern, closure);
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
     * Return a CharSequence with lines (separated by LF, CR/LF, or CR)
     * terminated by the platform specific line separator.
     *
     * @param self a CharSequence object
     * @return the denormalized CharSequence
     * @see #denormalize(String)
     * @since 1.8.2
     */
    public static CharSequence denormalize(final CharSequence self) {
        return denormalize(self.toString());
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
     * Return a CharSequence with linefeeds and carriage returns normalized to linefeeds.
     *
     * @param self a CharSequence object
     * @return the normalized CharSequence
     * @see #normalize(String)
     * @since 1.8.2
     */
    public static CharSequence normalize(final CharSequence self) {
        return normalize(self.toString());
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
     * Return the lines of a CharSequence as a List of CharSequence.
     *
     * @param self a CharSequence object
     * @return a list of lines
     * @throws java.io.IOException if an error occurs
     * @since 1.8.2
     */
    public static List<CharSequence> readLines(CharSequence self) throws IOException {
        return new ArrayList<CharSequence>(readLines(self.toString()));
    }

    /**
     * Reads the file into a list of Strings, with one item for each line.
     *
     * @param file a File
     * @return a List of lines
     * @throws IOException if an IOException occurs.
     * @see #readLines(java.io.Reader)
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
     * @see #readLines(java.io.Reader)
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
     * @see #readLines(java.io.Reader)
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
     * @see #readLines(java.io.Reader)
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
     * @see #readLines(java.io.Reader)
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
     * @see #readLines(java.io.Reader)
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
     * Read the content of this URL and returns it as a String.
     *
     * @param url URL to read content from
     * @param parameters connection parameters
     * @return the text from that URL
     * @throws IOException if an IOException occurs.
     * @since 1.8.1
     */
    public static String getText(URL url, Map parameters) throws IOException {
        return getText(url, parameters, CharsetToolkit.getDefaultSystemCharset().toString());
    }

    /**
     * Read the data from this URL and return it as a String.  The connection
     * stream is closed before this method returns.
     *
     * @param url     URL to read content from
     * @param charset opens the stream with a specified charset
     * @return the text from that URL
     * @throws IOException if an IOException occurs.
     * @see java.net.URLConnection#getInputStream()
     * @since 1.0
     */
    public static String getText(URL url, String charset) throws IOException {
        BufferedReader reader = newReader(url, charset);
        return getText(reader);
    }

    /**
     * Read the data from this URL and return it as a String.  The connection
     * stream is closed before this method returns.
     *
     * @param url     URL to read content from
     * @param parameters connection parameters
     * @param charset opens the stream with a specified charset
     * @return the text from that URL
     * @throws IOException if an IOException occurs.
     * @see java.net.URLConnection#getInputStream()
     * @since 1.8.1
     */
    public static String getText(URL url, Map parameters, String charset) throws IOException {
        BufferedReader reader = newReader(url, parameters, charset);
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
     * @see #getText(java.io.BufferedReader)
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
     * Read the content of the File and returns it as a byte[].
     *
     * @param file the file whose content we want to read
     * @return a String containing the content of the file
     * @throws IOException if an IOException occurs.
     * @since 1.7.1
     */
    public static byte[] getBytes(File file) throws IOException {
        return getBytes(new FileInputStream(file));
    }

    /**
     * Read the content of this URL and returns it as a byte[].
     *
     * @param url URL to read content from
     * @return the byte[] from that URL
     * @throws IOException if an IOException occurs.
     * @since 1.7.1
     */
    public static byte[] getBytes(URL url) throws IOException {
        return getBytes(url.openConnection().getInputStream());
    }

    /**
     * Read the content of this InputStream and return it as a byte[].
     * The stream is closed before this method returns.
     *
     * @param is an input stream
     * @return the byte[] from that InputStream
     * @throws IOException if an IOException occurs.
     * @since 1.7.1
     */
    public static byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream answer = new ByteArrayOutputStream();
        // reading the content of the file within a byte buffer
        byte[] byteBuffer = new byte[8192];
        int nbByteRead /* = 0*/;
        try {
            while ((nbByteRead = is.read(byteBuffer)) != -1) {
                // appends buffer
                answer.write(byteBuffer, 0, nbByteRead);
            }
        } finally {
            closeWithWarning(is);
        }
        return answer.toByteArray();
    }

    /**
     * Write the bytes from the byte array to the File.
     *
     * @param file  the file to write to
     * @param bytes the byte[] to write to the file
     * @throws IOException if an IOException occurs.
     * @since 1.7.1
     */
    public static void setBytes(File file, byte[] bytes) throws IOException {
        setBytes(new FileOutputStream(file), bytes);
    }

    /**
     * Write the byte[] to the output stream.
     * The stream is closed before this method returns.
     *
     * @param os    an output stream
     * @param bytes the byte[] to write to the output stream
     * @throws IOException if an IOException occurs.
     * @since 1.7.1
     */
    public static void setBytes(OutputStream os, byte[] bytes) throws IOException {
        try {
            os.write(bytes);
        } finally {
            closeWithWarning(os);
        }
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
     * @see #write(java.io.File, java.lang.String)
     * @since 1.5.1
     */
    public static void setText(File file, String text) throws IOException {
        write(file, text);
    }

    /**
     * Synonym for write(text, charset) allowing:
     * <pre>
     * myFile.setText('some text', charset)
     * </pre>
     * or with some help from <code>ExpandoMetaClass</code>, you could do something like:
     * <pre>
     * myFile.metaClass.setText = { String s -> delegate.setText(s, 'UTF-8') }
     * myfile.text = 'some text'
     * </pre>
     *
     * @param file A File
     * @param charset The charset used when writing to the file
     * @param text The text to write to the File
     * @throws IOException if an IOException occurs.
     * @see #write(java.io.File, java.lang.String, java.lang.String)
     * @since 1.7.3
     */
    public static void setText(File file, String text, String charset) throws IOException {
        write(file, text, charset);
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
     * Append binary data to the file.  See {@link #append(java.io.File, java.io.InputStream)}
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
            if (fileType == FileType.ANY ||
                    (fileType != FileType.FILES && file.isDirectory()) ||
                    (fileType != FileType.DIRECTORIES && file.isFile())) {
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
     * @see java.io.File#listFiles()
     * @see #eachFile(java.io.File, groovy.io.FileType, groovy.lang.Closure)
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
     * @see java.io.File#listFiles()
     * @see #eachFile(java.io.File, groovy.io.FileType, groovy.lang.Closure)
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
     * <dt>type</dt><dd>A {@link groovy.io.FileType} enum to determine if normal files or directories or both are processed</dd>
     * <dt>preDir</dt><dd>A {@link groovy.lang.Closure} run before each directory is processed and optionally returning a {@link groovy.io.FileVisitResult} value
     *     which can be used to control subsequent processing.</dd>
     * <dt>preRoot</dt><dd>A boolean indicating that the 'preDir' closure should be applied at the root level</dd>
     * <dt>postDir</dt><dd>A {@link groovy.lang.Closure} run after each directory is processed and optionally returning a {@link groovy.io.FileVisitResult} value
     *     which can be used to control subsequent processing.</dd>
     * <dt>postRoot</dt><dd>A boolean indicating that the 'postDir' closure should be applied at the root level</dd>
     * <dt>visitRoot</dt><dd>A boolean indicating that the given closure should be applied for the root dir
     *     (not applicable if the 'type' is set to {@link groovy.io.FileType#FILES})</dd>
     * <dt>maxDepth</dt><dd>The maximum number of directory levels when recursing
     *     (default is -1 which means infinite, set to 0 for no recursion)</dd>
     * <dt>filter</dt><dd>A filter to perform on traversed files/directories (using the {@link #isCase(java.lang.Object, java.lang.Object)} method). If set,
     *     only files/dirs which match are candidates for visiting.</dd>
     * <dt>nameFilter</dt><dd>A filter to perform on the name of traversed files/directories (using the {@link #isCase(java.lang.Object, java.lang.Object)} method). If set,
     *     only files/dirs which match are candidates for visiting. (Must not be set if 'filter' is set)</dd>
     * <dt>excludeFilter</dt><dd>A filter to perform on traversed files/directories (using the {@link #isCase(java.lang.Object, java.lang.Object)} method).
     *     If set, any candidates which match won't be visited.</dd>
     * <dt>excludeNameFilter</dt><dd>A filter to perform on the names of traversed files/directories (using the {@link #isCase(java.lang.Object, java.lang.Object)} method).
     *     If set, any candidates which match won't be visited. (Must not be set if 'excludeFilter' is set)</dd>
     * <dt>sort</dt><dd>A {@link groovy.lang.Closure} which if set causes the files and subdirectories for each directory to be processed in sorted order.
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
     * @param closure the Closure to invoke on each file/directory and optionally returning a {@link groovy.io.FileVisitResult} value
     *     which can be used to control subsequent processing
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory or illegal filter combinations are supplied
     * @see #sort(java.util.Collection, groovy.lang.Closure)
     * @see groovy.io.FileVisitResult
     * @see groovy.io.FileType
     * @since 1.7.1
     */
    public static void traverse(final File self, final Map<String, Object> options, final Closure closure)
            throws FileNotFoundException, IllegalArgumentException {
        Number maxDepthNumber = asType(options.remove("maxDepth"), Number.class);
        int maxDepth = maxDepthNumber == null ? -1 : maxDepthNumber.intValue();
        Boolean visitRoot = asType(get(options, "visitRoot", false), Boolean.class);
        Boolean preRoot = asType(get(options, "preRoot", false), Boolean.class);
        Boolean postRoot = asType(get(options, "postRoot", false), Boolean.class);
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
        boolean included = filterToUse == null || DefaultTypeTransformation.castToBoolean(filterMC.invokeMethod(filterToUse, "isCase", filterParam));
        boolean excluded = excludeFilterToUse != null && DefaultTypeTransformation.castToBoolean(excludeMC.invokeMethod(excludeFilterToUse, "isCase", excludeParam));
        return included && !excluded;
    }

    /**
     * Invokes the closure for each descendant file in this directory tree.
     * Sub-directories are recursively traversed in a depth-first fashion.
     * Convenience method for {@link #traverse(java.io.File, java.util.Map, groovy.lang.Closure)} when
     * no options to alter the traversal behavior are required.
     *
     * @param self    a File
     * @param closure the Closure to invoke on each file/directory and optionally returning a {@link groovy.io.FileVisitResult} value
     *     which can be used to control subsequent processing
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     * @see #traverse(java.io.File, java.util.Map, groovy.lang.Closure)
     * @since 1.7.1
     */
    public static void traverse(final File self, final Closure closure)
            throws FileNotFoundException, IllegalArgumentException {
        traverse(self, new HashMap<String, Object>(), closure);
    }

    /**
     * Invokes the closure specified with key 'visit' in the options Map
     * for each descendant file in this directory tree. Convenience method
     * for {@link #traverse(java.io.File, java.util.Map, groovy.lang.Closure)} allowing the 'visit' closure
     * to be included in the options Map rather than as a parameter.
     *
     * @param self    a File
     * @param options a Map of options to alter the traversal behavior
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory or illegal filter combinations are supplied
     * @see #traverse(java.io.File, java.util.Map, groovy.lang.Closure)
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
     * @see #eachFileRecurse(java.io.File, groovy.io.FileType, groovy.lang.Closure)
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
     * @see #eachFileRecurse(java.io.File, groovy.io.FileType, groovy.lang.Closure)
     * @since 1.5.0
     */
    public static void eachDirRecurse(final File self, final Closure closure) throws FileNotFoundException, IllegalArgumentException {
        eachFileRecurse(self, FileType.DIRECTORIES, closure);
    }

    /**
     * Invokes the closure for each file whose name (file.name) matches the given nameFilter in the given directory
     * - calling the {@link #isCase(java.lang.Object, java.lang.Object)} method to determine if a match occurs.  This method can be used
     * with different kinds of filters like regular expressions, classes, ranges etc.
     * Both regular files and subdirectories may be candidates for matching depending
     * on the value of fileType.
     * <pre>
     * // collect names of files in baseDir matching supplied regex pattern
     * import static groovy.io.FileType.*
     * def names = []
     * baseDir.eachFileMatch FILES, ~/foo\d\.txt/, { names << it.name }
     * assert names == ['foo1.txt', 'foo2.txt']
     *
     * // remove all *.bak files in baseDir
     * baseDir.eachFileMatch FILES, ~/.*\.bak/, { File bak -> bak.delete() }
     *
     * // print out files > 4K in size from baseDir
     * baseDir.eachFileMatch FILES, { new File(baseDir, it).size() > 4096 }, { println "$it.name ${it.size()}" }
     * </pre>
     *
     * @param self       a file
     * @param fileType   whether normal files or directories or both should be processed
     * @param nameFilter the filter to perform on the name of the file/directory (using the {@link #isCase(java.lang.Object, java.lang.Object)} method)
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
     * - calling the {@link #isCase(java.lang.Object, java.lang.Object)} method to determine if a match occurs.  This method can be used
     * with different kinds of filters like regular expressions, classes, ranges etc.
     * Both regular files and subdirectories are matched.
     *
     * @param self       a file
     * @param nameFilter the nameFilter to perform on the name of the file (using the {@link #isCase(java.lang.Object, java.lang.Object)} method)
     * @param closure    the closure to invoke
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     * @see #eachFileMatch(java.io.File, groovy.io.FileType, java.lang.Object, groovy.lang.Closure)
     * @since 1.5.0
     */
    public static void eachFileMatch(final File self, final Object nameFilter, final Closure closure)
            throws FileNotFoundException, IllegalArgumentException {
        eachFileMatch(self, FileType.ANY, nameFilter, closure);
    }

    /**
     * Invokes the closure for each subdirectory whose name (dir.name) matches the given nameFilter in the given directory
     * - calling the {@link #isCase(java.lang.Object, java.lang.Object)} method to determine if a match occurs.  This method can be used
     * with different kinds of filters like regular expressions, classes, ranges etc.
     * Only subdirectories are matched; regular files are ignored.
     *
     * @param self       a file
     * @param nameFilter the nameFilter to perform on the name of the directory (using the {@link #isCase(java.lang.Object, java.lang.Object)} method)
     * @param closure    the closure to invoke
     * @throws FileNotFoundException    if the given directory does not exist
     * @throws IllegalArgumentException if the provided File object does not represent a directory
     * @see #eachFileMatch(java.io.File, groovy.io.FileType, java.lang.Object, groovy.lang.Closure)
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
     * Renames the file. It's a shortcut for {@link java.io.File#renameTo(File)}
     *
     * @param self a File
     * @param newPathName The new pathname for the named file
     * @return  <code>true</code> if and only if the renaming succeeded;
     *             <code>false</code> otherwise
     * @since 1.7.4
     */
    public static boolean renameTo(final File self, String newPathName) {
        return self.renameTo(new File(newPathName));
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
    public static <T> T withReader(File file, Closure<T> closure) throws IOException {
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
    public static <T> T withReader(File file, String charset, Closure<T> closure) throws IOException {
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
     * @see #withStream(java.io.OutputStream, groovy.lang.Closure)
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
     * @see #withStream(java.io.InputStream, groovy.lang.Closure)
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
     * @see #withStream(java.io.InputStream, groovy.lang.Closure)
     * @since 1.5.2
     */
    public static <T> T withInputStream(URL url, Closure<T> closure) throws IOException {
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
     * @see #withStream(java.io.OutputStream, groovy.lang.Closure)
     * @since 1.5.2
     */
    public static <T> T withDataOutputStream(File file, Closure<T> closure) throws IOException {
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
     * @see #withStream(java.io.InputStream, groovy.lang.Closure)
     * @since 1.5.2
     */
    public static <T> T withDataInputStream(File file, Closure<T> closure) throws IOException {
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
     * Write a Byte Order Mark at the beginning of the file
     *
     * @param stream    the FileOutputStream to write the BOM to
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
    public static <T> T withWriter(File file, Closure<T> closure) throws IOException {
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
    public static <T> T withWriter(File file, String charset, Closure<T> closure) throws IOException {
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
    public static <T> T withWriterAppend(File file, String charset, Closure<T> closure) throws IOException {
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
    public static <T> T withWriterAppend(File file, Closure<T> closure) throws IOException {
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
    public static <T> T withPrintWriter(File file, Closure<T> closure) throws IOException {
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
    public static <T> T withPrintWriter(File file, String charset, Closure<T> closure) throws IOException {
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
    public static <T> T withPrintWriter(Writer writer, Closure<T> closure) throws IOException {
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
    public static <T> T withWriter(Writer writer, Closure<T> closure) throws IOException {
        try {
            T result = closure.call(writer);

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
    public static <T> T withReader(Reader reader, Closure<T> closure) throws IOException {
        try {
            T result = closure.call(reader);

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
    public static <T> T withStream(InputStream stream, Closure<T> closure) throws IOException {
        try {
            T result = closure.call(stream);

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
    public static <T> T withReader(URL url, Closure<T> closure) throws IOException {
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
    public static <T> T withReader(URL url, String charset, Closure<T> closure) throws IOException {
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
    public static <T> T withReader(InputStream in, Closure<T> closure) throws IOException {
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
    public static <T> T withReader(InputStream in, String charset, Closure<T> closure) throws IOException {
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
     * @see #withWriter(java.io.Writer, groovy.lang.Closure)
     * @since 1.5.2
     */
    public static <T> T withWriter(OutputStream stream, Closure<T> closure) throws IOException {
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
     * @see #withWriter(java.io.Writer, groovy.lang.Closure)
     * @since 1.5.2
     */
    public static <T> T withWriter(OutputStream stream, String charset, Closure<T> closure) throws IOException {
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
    public static <T> T withStream(OutputStream os, Closure<T> closure) throws IOException {
        try {
            T result = closure.call(os);
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
     * Creates an inputstream for this URL, with the possibility to set different connection parameters using the
     * <i>parameters map</i>:
     * <ul>
     *     <li>connectTimeout : the connection timeout</li>
     *     <li>readTimeout : the read timeout</li>
     *     <li>useCaches : set the use cache property for the URL connection</li>
     *     <li>allowUserInteraction : set the user interaction flag for the URL connection</li>
     *     <li>requestProperties : a map of properties to be passed to the URL connection</li>
     * </ul>
     * @param parameters an optional map specifying part or all of supported connection parameters
     * @param url the url for which to create the inputstream
     * @return an InputStream from the underlying URLConnection
     * @throws IOException if an I/O error occurs while creating the input stream
     * @since 1.8.1
     */
    private static InputStream configuredInputStream(Map parameters, URL url) throws IOException {
        final URLConnection connection = url.openConnection();
        if (parameters!=null) {
            if (parameters.containsKey("connectTimeout")) {
                connection.setConnectTimeout(asType(parameters.get("connectTimeout"), Integer.class));
            }
            if (parameters.containsKey("readTimeout")) {
                connection.setReadTimeout(asType(parameters.get("readTimeout"), Integer.class));
            }
            if (parameters.containsKey("useCaches")) {
                connection.setUseCaches(asType(parameters.get("useCaches"), Boolean.class));
            }
            if (parameters.containsKey("allowUserInteraction")) {
                connection.setAllowUserInteraction(asType(parameters.get("allowUserInteraction"), Boolean.class));
            }
            if (parameters.containsKey("requestProperties")) {
                @SuppressWarnings("unchecked")
                Map<String,String> properties = (Map<String, String>) parameters.get("requestProperties");
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

        }
        return connection.getInputStream();
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
        return new BufferedInputStream(configuredInputStream(null, url));
    }

    /**
     * Creates a buffered input stream for this URL.
     *
     * @param url a URL
     * @param parameters connection parameters
     * @return a BufferedInputStream for the URL
     * @throws MalformedURLException is thrown if the URL is not well formed
     * @throws IOException if an I/O error occurs while creating the input stream
     * @since 1.8.1
     */
    public static BufferedInputStream newInputStream(URL url, Map parameters) throws MalformedURLException, IOException {
        return new BufferedInputStream(configuredInputStream(parameters, url));
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
        return newReader(configuredInputStream(null, url));
    }

    /**
     * Creates a buffered reader for this URL.
     *
     * @param url a URL
     * @param parameters connection parameters
     * @return a BufferedReader for the URL
     * @throws MalformedURLException is thrown if the URL is not well formed
     * @throws IOException if an I/O error occurs while creating the input stream
     * @since 1.8.1
     */
    public static BufferedReader newReader(URL url, Map parameters) throws MalformedURLException, IOException {
        return newReader(configuredInputStream(parameters, url));
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
        return new BufferedReader(new InputStreamReader(configuredInputStream(null, url), charset));
    }

    /**
     * Creates a buffered reader for this URL using the given encoding.
     *
     * @param url a URL
     * @param parameters connection parameters
     * @param charset opens the stream with a specified charset
     * @return a BufferedReader for the URL
     * @throws MalformedURLException is thrown if the URL is not well formed
     * @throws IOException if an I/O error occurs while creating the input stream
     * @since 1.8.1
     */
    public static BufferedReader newReader(URL url, Map parameters, String charset) throws MalformedURLException, IOException {
        return new BufferedReader(new InputStreamReader(configuredInputStream(parameters, url), charset));
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
     * @see #eachByte(java.io.InputStream, groovy.lang.Closure)
     * @since 1.0
     */
    public static void eachByte(File self, Closure closure) throws IOException {
        BufferedInputStream is = newInputStream(self);
        eachByte(is, closure);
    }

    /**
     * Traverse through the bytes of this File, bufferLen bytes at a time.
     *
     * @param self      a File
     * @param bufferLen the length of the buffer to use.
     * @param closure   a 2 parameter closure which is passed the byte[] and a number of bytes successfully read.
     * @throws IOException if an IOException occurs.
     * @see #eachByte(java.io.InputStream, int, groovy.lang.Closure)
     * @since 1.7.4
     */
    public static void eachByte(File self, int bufferLen, Closure closure) throws IOException {
        BufferedInputStream is = newInputStream(self);
        eachByte(is, bufferLen, closure);
    }

    /**
     * Traverse through each byte of this Byte array. Alias for each.
     *
     * @param self    a Byte array
     * @param closure a closure
     * @see #each(java.lang.Object, groovy.lang.Closure)
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
     * @see #each(java.lang.Object, groovy.lang.Closure)
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
     * Traverse through each the specified stream reading bytes into a buffer
     * and calling the 2 parameter closure with this buffer and the number of bytes.
     *
     * @param is        stream to iterate over, closed after the method call.
     * @param bufferLen the length of the buffer to use.
     * @param closure   a 2 parameter closure which is passed the byte[] and a number of bytes successfully read.
     * @throws IOException if an IOException occurs.
     * @since 1.8
     */
    public static void eachByte(InputStream is, int bufferLen, Closure closure) throws IOException {
        byte[] buffer = new byte[ bufferLen ] ;
        int bytesRead = 0 ;
        try {
            while ( ( bytesRead = is.read( buffer, 0, bufferLen ) ) > 0 ) {
                closure.call( new Object[]{ buffer, bytesRead } ) ;
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
     * @see #eachByte(java.io.InputStream, groovy.lang.Closure)
     * @since 1.0
     */
    public static void eachByte(URL url, Closure closure) throws IOException {
        InputStream is = url.openConnection().getInputStream();
        eachByte(is, closure);
    }

    /**
     * Reads the InputStream from this URL, passing a byte[] and a number of bytes
     * to the given closure.  The URL stream will be closed before this method returns.
     *
     * @param url       url to iterate over
     * @param bufferLen the length of the buffer to use.
     * @param closure   a 2 parameter closure which is passed the byte[] and a number of bytes successfully read.
     * @throws IOException if an IOException occurs.
     * @see #eachByte(java.io.InputStream, int, groovy.lang.Closure)
     * @since 1.8
     */
    public static void eachByte(URL url, int bufferLen, Closure closure) throws IOException {
        InputStream is = url.openConnection().getInputStream();
        eachByte(is, bufferLen, closure);
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
            BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
            while ((line = br.readLine()) != null) {
                if (bcw.call(line)) {
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
     * Filters the lines of a File and creates a Writable in return to
     * stream the filtered lines.
     *
     * @param self    a File
     * @param closure a closure which returns a boolean indicating to filter
     *                the line or not
     * @return a Writable closure
     * @throws IOException if <code>self</code> is not readable
     * @see #filterLine(java.io.Reader, groovy.lang.Closure)
     * @since 1.0
     */
    public static Writable filterLine(File self, Closure closure) throws IOException {
        return filterLine(newReader(self), closure);
    }

    /**
     * Filters the lines of a File and creates a Writable in return to
     * stream the filtered lines.
     *
     * @param self    a File
     * @param charset opens the file with a specified charset
     * @param closure a closure which returns a boolean indicating to filter
     *                the line or not
     * @return a Writable closure
     * @throws IOException if an IOException occurs
     * @see #filterLine(java.io.Reader, groovy.lang.Closure)
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
     * @see #filterLine(java.io.Reader, java.io.Writer, groovy.lang.Closure)
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
     * @see #filterLine(java.io.Reader, java.io.Writer, groovy.lang.Closure)
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
                BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
                while ((line = br.readLine()) != null) {
                    if (bcw.call(line)) {
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
     * @see #filterLine(java.io.Reader, groovy.lang.Closure)
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
     * @see #filterLine(java.io.Reader, groovy.lang.Closure)
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
     * @see #filterLine(java.io.Reader, java.io.Writer, groovy.lang.Closure)
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
     * @see #filterLine(java.io.Reader, java.io.Writer, groovy.lang.Closure)
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
     * @see #filterLine(java.io.Reader, groovy.lang.Closure)
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
     * @see #filterLine(java.io.Reader, groovy.lang.Closure)
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
     * @see #filterLine(java.io.Reader, java.io.Writer, groovy.lang.Closure)
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
     * @see #filterLine(java.io.Reader, java.io.Writer, groovy.lang.Closure)
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
    public static <T> T withStreams(Socket socket, Closure<T> closure) throws IOException {
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        try {
            T result = closure.call(new Object[]{input, output});

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
    public static <T> T withObjectStreams(Socket socket, Closure<T> closure) throws IOException {
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(output);
        ObjectInputStream ois = new ObjectInputStream(input);
        try {
            T result = closure.call(new Object[]{ois, oos});

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
        return accept(serverSocket, true, closure);
    }

    /**
     * Accepts a connection and passes the resulting Socket to the closure
     * which runs in a new Thread or the calling thread, as needed.
     *
     * @param serverSocket a ServerSocket
     * @param runInANewThread This flag should be true, if the closure should be invoked in a new thread, else false.
     * @param closure      a Closure
     * @return a Socket
     * @throws IOException if an IOException occurs.
     * @see java.net.ServerSocket#accept()
     * @since 1.7.6
     */
    public static Socket accept(ServerSocket serverSocket, final boolean runInANewThread,
            final Closure closure) throws IOException {
        final Socket socket = serverSocket.accept();
        if(runInANewThread) {
            new Thread(new Runnable() {
                public void run() {
                    invokeClosureWithSocket(socket, closure);
                }
            }).start();
        } else {
            invokeClosureWithSocket(socket, closure);
        }
        return socket;
    }

    private static void invokeClosureWithSocket(Socket socket, Closure closure) {
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

    /**
     * Converts this File to a {@link groovy.lang.Writable}.
     *
     * @param file a File
     * @return a File which wraps the input file and which implements Writable
     * @since 1.0
     */
    public static File asWritable(File file) {
        return new WritableFile(file);
    }

    /**
     * Converts this File to a {@link groovy.lang.Writable} or delegates to default
     * {@link #asType(java.lang.Object, java.lang.Class)}.
     *
     * @param f a File
     * @param c the desired class
     * @return the converted object
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static <T> T asType(File f, Class<T> c) {
        if (c == Writable.class) {
            return (T) asWritable(f);
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
     * Converts the given CharSequence into a List of CharSequence of one character.
     *
     * @param self a CharSequence
     * @return a List of characters (a 1-character CharSequence)
     * @see #toSet(String)
     * @since 1.8.2
     */
    public static List<CharSequence> toList(CharSequence self) {
        return new ArrayList<CharSequence>(toList(self.toString()));
    }

    /**
     * Converts the given String into a Set of unique strings of one character.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * assert 'groovy'.toSet() == ['v', 'g', 'r', 'o', 'y'] as Set
     * assert "abc".toSet().iterator()[0] instanceof String
     * </pre>
     *
     * @param self a String
     * @return a Set of unique character Strings (each a 1-character String)
     * @since 1.8.0
     */
    public static Set<String> toSet(String self) {
        return new HashSet<String>(toList(self));
    }

    /**
     * Converts the given CharSequence into a Set of unique CharSequence of one character.
     *
     * @param self a CharSequence
     * @return a Set of unique character CharSequence (each a 1-character CharSequence)
     * @see #toSet(String)
     * @since 1.8.2
     */
    public static Set<CharSequence> toSet(CharSequence self) {
        return new HashSet<CharSequence>(toList(self));
    }

    /**
     * Converts the given String into an array of characters.
     * Alias for toCharArray.
     *
     * @param self a String
     * @return an array of characters
     * @see java.lang.String#toCharArray()
     * @since 1.6.0
     */
    public static char[] getChars(String self) {
        return self.toCharArray();
    }

    /**
     * Converts the given CharSequence into an array of characters.
     *
     * @param self a CharSequence
     * @return an array of characters
     * @see #getChars(String)
     * @since 1.8.2
     */
    public static char[] getChars(CharSequence self) {
        return getChars(self.toString());
    }

    /**
     * Converts the GString to a File, or delegates to the default
     * {@link #asType(java.lang.Object, java.lang.Class)}
     *
     * @param self a GString
     * @param c    the desired class
     * @return the converted object
     * @since 1.5.0
     */
    @SuppressWarnings("unchecked")
    public static <T> T asType(GString self, Class<T> c) {
        if (c == File.class) {
            return (T) new File(self.toString());
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
     * {@link #asType(java.lang.Object, java.lang.Class)}.
     *
     * @param self a String
     * @param c    the desired class
     * @return the converted object
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static <T> T asType(String self, Class<T> c) {
        if (c == List.class) {
            return (T) toList(self);
        } else if (c == BigDecimal.class) {
            return (T) toBigDecimal(self);
        } else if (c == BigInteger.class) {
            return (T) toBigInteger(self);
        } else if (c == Long.class || c == Long.TYPE) {
            return (T) toLong(self);
        } else if (c == Integer.class || c == Integer.TYPE) {
            return (T) toInteger(self);
        } else if (c == Short.class || c == Short.TYPE) {
            return (T) toShort(self);
        } else if (c == Byte.class || c == Byte.TYPE) {
            return (T) Byte.valueOf(self.trim());
        } else if (c == Character.class || c == Character.TYPE) {
            return (T) toCharacter(self);
        } else if (c == Double.class || c == Double.TYPE) {
            return (T) toDouble(self);
        } else if (c == Float.class || c == Float.TYPE) {
            return (T) toFloat(self);
        } else if (c == File.class) {
            return (T) new File(self);
        } else if (DefaultTypeTransformation.isEnumSubclass(c)) {
            return (T) InvokerHelper.invokeMethod(c, "valueOf", new Object[]{ self });
        }
        return asType((Object) self, c);
    }

    /**
     * <p>Provides a method to perform custom 'dynamic' type conversion
     * to the given class using the <code>as</code> operator.
     *
     * @param self a CharSequence
     * @param c    the desired class
     * @return the converted object
     * @see #asType(String, Class)
     * @since 1.8.2
     */
    public static <T> T asType(CharSequence self, Class<T> c) {
        return asType(self.toString(), c);
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
     * Process each regex group matched substring of the given CharSequence. If the closure
     * parameter takes one argument, an array with all match groups is passed to it.
     * If the closure takes as many arguments as there are match groups, then each
     * parameter will be one match group.
     *
     * @param self    the source CharSequence
     * @param regex   a Regex CharSequence
     * @param closure a closure with one parameter or as much parameters as groups
     * @return the source CharSequence
     * @see #eachMatch(String, String, groovy.lang.Closure)
     * @since 1.8.2
     */
    public static String eachMatch(CharSequence self, CharSequence regex, Closure closure) {
        return eachMatch(self.toString(), regex.toString(), closure);
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
     * Process each regex group matched substring of the given pattern. If the closure
     * parameter takes one argument, an array with all match groups is passed to it.
     * If the closure takes as many arguments as there are match groups, then each
     * parameter will be one match group.
     *
     * @param self    the source CharSequence
     * @param pattern a regex Pattern
     * @param closure a closure with one parameter or as much parameters as groups
     * @return the source CharSequence
     * @see #eachMatch(String, Pattern, groovy.lang.Closure)
     * @since 1.8.2
     */
    public static String eachMatch(CharSequence self, Pattern pattern, Closure closure) {
        return eachMatch(self.toString(), pattern, closure);
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
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext(); i++) {
            Object value = iter.next();
            if (i < startIndex) {
                continue;
            }
            if (bcw.call(value)) {
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
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext(); i++) {
            Object value = iter.next();
            if (i < startIndex) {
                continue;
            }
            if (bcw.call(value)) {
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
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext(); count++) {
            Object value = iter.next();
            if (count < startCount) {
                continue;
            }
            if (bcw.call(value)) {
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
    @SuppressWarnings("unchecked")
    public static <T> T asType(Object obj, Class<T> type) {
        if (String.class == type) {
            return (T) InvokerHelper.toString(obj);
        }

        // fall back to cast
        try {
          return (T) DefaultTypeTransformation.castToType(obj, type);
        }
        catch (GroovyCastException e) {
            MetaClass mc = InvokerHelper.getMetaClass(obj);
            if (mc instanceof ExpandoMetaClass) {
                ExpandoMetaClass emc = (ExpandoMetaClass) mc;
                Object mixedIn = emc.castToMixedType(obj, type);
                if (mixedIn != null)
                  return (T) mixedIn;
            }
            if (type.isInterface()) {
                try {
                    List<Class> interfaces = new ArrayList<Class>();
                    interfaces.add(type);
                    return (T) ProxyGenerator.INSTANCE.instantiateDelegate(interfaces, obj);
                } catch (GroovyRuntimeException cause) {
                    // ignore
                }
            }
            throw e;
        }
    }

    private static Object asArrayType(Object object, Class type) {
        if (type.isAssignableFrom(object.getClass())) {
            return object;
        }
        Collection list = DefaultTypeTransformation.asCollection(object);
        int size = list.size();
        Class elementType = type.getComponentType();
        Object array = Array.newInstance(elementType, size);
        int idx = 0;

        if (boolean.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setBoolean(array, idx, (Boolean) InvokerHelper.invokeStaticMethod(DefaultGroovyMethods.class, "asType", new Object[]{element, boolean.class}));
            }
        } else if (byte.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setByte(array, idx, (Byte) InvokerHelper.invokeStaticMethod(DefaultGroovyMethods.class, "asType", new Object[]{element, byte.class}));
            }
        } else if (char.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setChar(array, idx, (Character) InvokerHelper.invokeStaticMethod(DefaultGroovyMethods.class, "asType", new Object[]{element, char.class}));
            }
        } else if (double.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setDouble(array, idx, (Double) InvokerHelper.invokeStaticMethod(DefaultGroovyMethods.class, "asType", new Object[]{element, double.class}));
            }
        } else if (float.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setFloat(array, idx, (Float) InvokerHelper.invokeStaticMethod(DefaultGroovyMethods.class, "asType", new Object[]{element, float.class}));
            }
        } else if (int.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setInt(array, idx, (Integer) InvokerHelper.invokeStaticMethod(DefaultGroovyMethods.class, "asType", new Object[]{element, int.class}));
            }
        } else if (long.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setLong(array, idx, (Long) InvokerHelper.invokeStaticMethod(DefaultGroovyMethods.class, "asType", new Object[]{element, long.class}));
            }
        } else if (short.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setShort(array, idx, (Short) InvokerHelper.invokeStaticMethod(DefaultGroovyMethods.class, "asType", new Object[]{element, short.class}));
            }
        } else for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
            Object element = iter.next();
            Array.set(array, idx, InvokerHelper.invokeStaticMethod(DefaultGroovyMethods.class, "asType", new Object[]{element, elementType}));
        }
        return array;
    }

    /**
     * Convenience method to dynamically create a new instance of this
     * class.  Calls the default constructor.
     *
     * @param c a class
     * @return a new instance of this class
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> c) {
        return (T) InvokerHelper.invokeConstructorOf(c, null);
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
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> c, Object[] args) {
        if (args == null) args = new Object[]{null};
        return (T) InvokerHelper.invokeConstructorOf(c, args);
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
            disablePrimitiveOptimization(self);
        } else if (self instanceof Class) {
            ((MetaClassRegistryImpl)GroovySystem.getMetaClassRegistry()).setMetaClass((Class)self, metaClass);
        } else {
            ((MetaClassRegistryImpl)GroovySystem.getMetaClassRegistry()).setMetaClass(self, metaClass);
        }
    }

    private static void disablePrimitiveOptimization(Object self) {
        Field sdyn;
        Class c = self.getClass();
        try {
            sdyn = c.getDeclaredField(Verifier.STATIC_METACLASS_BOOL);
            sdyn.setBoolean(null, true);
        } catch (Throwable e) {
            //DO NOTHING
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
     * Attempts to create an Iterator for the given object by first
     * converting it to a Collection.
     *
     * @param a an array
     * @return an Iterator for the given Array.
     * @see org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation#asCollection(java.lang.Object[])
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
     * @see org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation#asCollection(java.lang.Object)
     * @since 1.0
     */
    public static Iterator iterator(Object o) {
        return DefaultTypeTransformation.asCollection(o).iterator();
    }

    /**
     * Allows an Enumeration to behave like an Iterator.  Note that the
     * {@link java.util.Iterator#remove() remove()} method is unsupported since the
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
     * Returns an {@link java.util.Iterator} which traverses each match.
     *
     * @param matcher a Matcher object
     * @return an Iterator for a Matcher
     * @see java.util.regex.Matcher#group()
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
                    List<String> list = new ArrayList<String>(matcher.groupCount());
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
     * @see groovy.lang.MetaObjectProtocol#respondsTo(java.lang.Object, java.lang.String, java.lang.Object[])
     * @since 1.6.0
     */
    public static List<MetaMethod> respondsTo(Object self, String name, Object[] argTypes) {
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
     * @see groovy.lang.MetaObjectProtocol#respondsTo(java.lang.Object, java.lang.String)
     * @since 1.6.1
     */
    public static List<MetaMethod> respondsTo(Object self, String name) {
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
     * @see groovy.lang.MetaObjectProtocol#hasProperty(java.lang.Object, java.lang.String)
     * @since 1.6.1
     */
    public static MetaProperty hasProperty(Object self, String name) {
        return InvokerHelper.getMetaClass(self).hasProperty(self, name);
    }

}
