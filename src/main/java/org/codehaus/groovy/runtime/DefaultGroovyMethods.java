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

import groovy.io.GroovyPrintWriter;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.EmptyRange;
import groovy.lang.ExpandoMetaClass;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovySystem;
import groovy.lang.Groovydoc;
import groovy.lang.IntRange;
import groovy.lang.ListWithDefault;
import groovy.lang.MapWithDefault;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaClassRegistry;
import groovy.lang.MetaMethod;
import groovy.lang.MetaProperty;
import groovy.lang.MissingPropertyException;
import groovy.lang.ObjectRange;
import groovy.lang.PropertyValue;
import groovy.lang.Range;
import groovy.lang.SpreadMap;
import groovy.lang.Tuple2;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FirstParam;
import groovy.transform.stc.FromString;
import groovy.transform.stc.MapEntryOrKeyValue;
import groovy.transform.stc.SimpleType;
import groovy.util.BufferedIterator;
import groovy.util.ClosureComparator;
import groovy.util.GroovyCollections;
import groovy.util.MapEntry;
import groovy.util.OrderBy;
import groovy.util.PermutationGenerator;
import groovy.util.ProxyGenerator;
import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.reflection.MixinInMetaClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.reflection.ReflectionUtils;
import org.codehaus.groovy.reflection.stdclasses.CachedSAMClass;
import org.codehaus.groovy.runtime.callsite.BooleanClosureWrapper;
import org.codehaus.groovy.runtime.callsite.BooleanReturningMethodInvoker;
import org.codehaus.groovy.runtime.dgmimpl.NumberNumberDiv;
import org.codehaus.groovy.runtime.dgmimpl.NumberNumberMinus;
import org.codehaus.groovy.runtime.dgmimpl.NumberNumberMultiply;
import org.codehaus.groovy.runtime.dgmimpl.NumberNumberPlus;
import org.codehaus.groovy.runtime.dgmimpl.arrays.BooleanArrayGetAtMetaMethod;
import org.codehaus.groovy.runtime.dgmimpl.arrays.BooleanArrayPutAtMetaMethod;
import org.codehaus.groovy.runtime.dgmimpl.arrays.ByteArrayGetAtMetaMethod;
import org.codehaus.groovy.runtime.dgmimpl.arrays.ByteArrayPutAtMetaMethod;
import org.codehaus.groovy.runtime.dgmimpl.arrays.CharacterArrayGetAtMetaMethod;
import org.codehaus.groovy.runtime.dgmimpl.arrays.CharacterArrayPutAtMetaMethod;
import org.codehaus.groovy.runtime.dgmimpl.arrays.DoubleArrayGetAtMetaMethod;
import org.codehaus.groovy.runtime.dgmimpl.arrays.DoubleArrayPutAtMetaMethod;
import org.codehaus.groovy.runtime.dgmimpl.arrays.FloatArrayGetAtMetaMethod;
import org.codehaus.groovy.runtime.dgmimpl.arrays.FloatArrayPutAtMetaMethod;
import org.codehaus.groovy.runtime.dgmimpl.arrays.IntegerArrayGetAtMetaMethod;
import org.codehaus.groovy.runtime.dgmimpl.arrays.IntegerArrayPutAtMetaMethod;
import org.codehaus.groovy.runtime.dgmimpl.arrays.LongArrayGetAtMetaMethod;
import org.codehaus.groovy.runtime.dgmimpl.arrays.LongArrayPutAtMetaMethod;
import org.codehaus.groovy.runtime.dgmimpl.arrays.ObjectArrayGetAtMetaMethod;
import org.codehaus.groovy.runtime.dgmimpl.arrays.ObjectArrayPutAtMetaMethod;
import org.codehaus.groovy.runtime.dgmimpl.arrays.ShortArrayGetAtMetaMethod;
import org.codehaus.groovy.runtime.dgmimpl.arrays.ShortArrayPutAtMetaMethod;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.runtime.metaclass.MissingPropertyExceptionNoStack;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.typehandling.GroovyCastException;
import org.codehaus.groovy.runtime.typehandling.NumberMath;
import org.codehaus.groovy.tools.RootLoader;
import org.codehaus.groovy.transform.trait.Traits;
import org.codehaus.groovy.util.ArrayIterator;
import org.codehaus.groovy.util.IteratorBufferedIterator;
import org.codehaus.groovy.util.ListBufferedIterator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import static groovy.lang.groovydoc.Groovydoc.EMPTY_GROOVYDOC;

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
 */
public class DefaultGroovyMethods extends DefaultGroovyMethodsSupport {

    private static final Logger LOG = Logger.getLogger(DefaultGroovyMethods.class.getName());
    private static final Integer ONE = 1;
    private static final BigInteger BI_INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final BigInteger BI_INT_MIN = BigInteger.valueOf(Integer.MIN_VALUE);
    private static final BigInteger BI_LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger BI_LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);

    public static final Class[] ADDITIONAL_CLASSES = {
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

    public static final Class[] DGM_LIKE_CLASSES = new Class[]{
            DefaultGroovyMethods.class,
            EncodingGroovyMethods.class,
            IOGroovyMethods.class,
            ProcessGroovyMethods.class,
            ResourceGroovyMethods.class,
            SocketGroovyMethods.class,
            StringGroovyMethods.class//,
            // Below are registered as module extension classes
//            DateUtilExtensions.class,
//            DateTimeStaticExtensions.class,
//            DateTimeExtensions.class,
//            SqlExtensions.class,
//            SwingGroovyMethods.class,
//            XmlExtensions.class,
//            NioExtensions.class
    };
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final NumberAwareComparator<Comparable> COMPARABLE_NUMBER_AWARE_COMPARATOR = new NumberAwareComparator<Comparable>();

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
     * @see #with(Object, Closure)
     * @since 1.0
     */
    public static <T,U> T identity(
            @DelegatesTo.Target("self") U self,
            @DelegatesTo(value=DelegatesTo.Target.class,
                    target="self",
                    strategy=Closure.DELEGATE_FIRST)
            @ClosureParams(FirstParam.class)
                    Closure<T> closure) {
        return DefaultGroovyMethods.with(self, closure);
    }

    /**
     * Allows the closure to be called for the object reference self.
     * <p>
     * Any method invoked inside the closure will first be invoked on the
     * self reference. For instance, the following method calls to the append()
     * method are invoked on the StringBuilder instance:
     * <pre class="groovyTestCase">
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
     * The other typical usage, uses the self object while creating some value:
     * <pre>
     * def fullName = person.with{ "$firstName $lastName" }
     * </pre>
     *
     * @param self    the object to have a closure act upon
     * @param closure the closure to call on the object
     * @return result of calling the closure
     * @see #with(Object, boolean, Closure)
     * @see #tap(Object, Closure)
     * @since 1.5.0
     */
    @SuppressWarnings("unchecked")
    public static <T,U> T with(
            @DelegatesTo.Target("self") U self,
            @DelegatesTo(value=DelegatesTo.Target.class,
                    target="self",
                    strategy=Closure.DELEGATE_FIRST)
            @ClosureParams(FirstParam.class)
            Closure<T> closure) {
        return (T) with(self, false, (Closure<Object>)closure);
    }

    /**
     * Allows the closure to be called for the object reference self.
     * <p/>
     * Any method invoked inside the closure will first be invoked on the
     * self reference. For example, the following method calls to the append()
     * method are invoked on the StringBuilder instance and then, because
     * 'returning' is true, the self instance is returned:
     * <pre class="groovyTestCase">
     * def b = new StringBuilder().with(true) {
     *   append('foo')
     *   append('bar')
     * }
     * assert b.toString() == 'foobar'
     * </pre>
     * The returning parameter is commonly set to true when using with to simplify object
     * creation, such as this example:
     * <pre>
     * def p = new Person().with(true) {
     *   firstName = 'John'
     *   lastName = 'Doe'
     * }
     * </pre>
     * Alternatively, 'tap' is an alias for 'with(true)', so that method can be used instead.
     *
     * The other main use case for with is when returning a value calculated using self as shown here:
     * <pre>
     * def fullName = person.with(false){ "$firstName $lastName" }
     * </pre>
     * Alternatively, 'with' is an alias for 'with(false)', so the boolean parameter can be ommitted instead.
     *
     * @param self      the object to have a closure act upon
     * @param returning if true, return the self object; otherwise, the result of calling the closure
     * @param closure   the closure to call on the object
     * @return the self object or the result of calling the closure depending on 'returning'
     * @see #with(Object, Closure)
     * @see #tap(Object, Closure)
     * @since 2.5.0
     */
    public static <T,U extends T, V extends T> T with(
            @DelegatesTo.Target("self") U self,
            boolean returning,
            @DelegatesTo(value=DelegatesTo.Target.class,
                    target="self",
                    strategy=Closure.DELEGATE_FIRST)
            @ClosureParams(FirstParam.class)
            Closure<T> closure) {
        @SuppressWarnings("unchecked")
        final Closure<V> clonedClosure = (Closure<V>) closure.clone();
        clonedClosure.setResolveStrategy(Closure.DELEGATE_FIRST);
        clonedClosure.setDelegate(self);
        V result = clonedClosure.call(self);
        return returning ? self : result;
    }

    /**
     * Allows the closure to be called for the object reference self (similar
     * to <code>with</code> and always returns self.
     * <p>
     * Any method invoked inside the closure will first be invoked on the
     * self reference. For instance, the following method calls to the append()
     * method are invoked on the StringBuilder instance:
     * <pre>
     * def b = new StringBuilder().tap {
     *   append('foo')
     *   append('bar')
     * }
     * assert b.toString() == 'foobar'
     * </pre>
     * This is commonly used to simplify object creation, such as this example:
     * <pre>
     * def p = new Person().tap {
     *   firstName = 'John'
     *   lastName = 'Doe'
     * }
     * </pre>
     *
     * @param self    the object to have a closure act upon
     * @param closure the closure to call on the object
     * @return self
     * @see #with(Object, boolean, Closure)
     * @see #with(Object, Closure)
     * @since 2.5.0
     */
    @SuppressWarnings("unchecked")
    public static <T,U> U tap(
            @DelegatesTo.Target("self") U self,
            @DelegatesTo(value=DelegatesTo.Target.class,
                    target="self",
                    strategy=Closure.DELEGATE_FIRST)
            @ClosureParams(FirstParam.class)
            Closure<T> closure) {
        return (U) with(self, true, (Closure<Object>)closure);
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
                    AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                        ReflectionUtils.trySetAccessible(field);
                        return null;
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
     * and provides the data in form of simple key/value pairs, i.e. without
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
     * Gets the url of the jar file/source file containing the specified class
     *
     * @param self the class
     * @return the url of the jar, {@code null} if the specified class is from JDK
     * @since 2.5.0
     */
    public static URL getLocation(Class self) {
        CodeSource codeSource = self.getProtectionDomain().getCodeSource();

        return null == codeSource ? null : codeSource.getLocation();
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
     * This method saves having to wrap the category
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
        InvokerHelper.invokeMethod(owner, "println", EMPTY_OBJECT_ARRAY);
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
     * Printf to the standard output stream.
     *
     * @param self   any Object
     * @param format a format string
     * @param values values referenced by the format specifiers in the format string
     * @since 1.0
     */
    public static void printf(Object self, String format, Object[] values) {
        if (self instanceof PrintStream)
            ((PrintStream)self).printf(format, values);
        else
            System.out.printf(format, values);
    }

    /**
     * Printf 0 or more values to the standard output stream using a format string.
     * This method delegates to the owner to execute the method.
     *
     * @param self   a generated closure
     * @param format a format string
     * @param values values referenced by the format specifiers in the format string
     * @since 3.0.0
     */
    public static void printf(Closure self, String format, Object[] values) {
        Object owner = getClosureOwner(self);
        Object[] newValues = new Object[values.length + 1];
        newValues[0] = format;
        System.arraycopy(values, 0, newValues, 1, values.length);
        InvokerHelper.invokeMethod(owner, "printf", newValues);
    }

    /**
     * Printf a value to the standard output stream using a format string.
     * This method delegates to the owner to execute the method.
     *
     * @param self   a generated closure
     * @param format a format string
     * @param value  value referenced by the format specifier in the format string
     * @since 3.0.0
     */
    public static void printf(Closure self, String format, Object value) {
        Object owner = getClosureOwner(self);
        Object[] newValues = new Object[2];
        newValues[0] = format;
        newValues[1] = value;
        InvokerHelper.invokeMethod(owner, "printf", newValues);
    }

    /**
     * Sprintf to a string.
     *
     * @param self   any Object
     * @param format a format string
     * @param values values referenced by the format specifiers in the format string
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
     * Prints a formatted string using the specified format string and arguments.
     * <p>
     * Examples:
     * <pre>
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
        switch (elemType) {
            case "[I":
                int[] ia = (int[]) arg;
                ans = new Integer[ia.length];
                for (int i = 0; i < ia.length; i++) {
                    ans[i] = ia[i];
                }
                break;
            case "[C":
                char[] ca = (char[]) arg;
                ans = new Character[ca.length];
                for (int i = 0; i < ca.length; i++) {
                    ans[i] = ca[i];
                }
                break;
            case "[Z": {
                boolean[] ba = (boolean[]) arg;
                ans = new Boolean[ba.length];
                for (int i = 0; i < ba.length; i++) {
                    ans[i] = ba[i];
                }
                break;
            }
            case "[B": {
                byte[] ba = (byte[]) arg;
                ans = new Byte[ba.length];
                for (int i = 0; i < ba.length; i++) {
                    ans[i] = ba[i];
                }
                break;
            }
            case "[S":
                short[] sa = (short[]) arg;
                ans = new Short[sa.length];
                for (int i = 0; i < sa.length; i++) {
                    ans[i] = sa[i];
                }
                break;
            case "[F":
                float[] fa = (float[]) arg;
                ans = new Float[fa.length];
                for (int i = 0; i < fa.length; i++) {
                    ans[i] = fa[i];
                }
                break;
            case "[J":
                long[] la = (long[]) arg;
                ans = new Long[la.length];
                for (int i = 0; i < la.length; i++) {
                    ans[i] = la[i];
                }
                break;
            case "[D":
                double[] da = (double[]) arg;
                ans = new Double[da.length];
                for (int i = 0; i < da.length; i++) {
                    ans[i] = da[i];
                }
                break;
            default:
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
     *         create this object. e.g. [1, 'hello'].inspect() {@code ->} [1, "hello"]
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
     * Returns an iterator equivalent to this iterator with all duplicated items removed
     * by using Groovy's default number-aware comparator. The original iterator will become
     * exhausted of elements after determining the unique values. A new iterator
     * for the unique values will be returned.
     *
     * @param self an Iterator
     * @return a new Iterator of the unique items from the original iterator
     * @since 1.5.5
     */
    public static <T> Iterator<T> unique(Iterator<T> self) {
        return uniqueItems(new IteratorIterableAdapter<T>(self)).listIterator();
    }

    /**
     * Modifies this collection to remove all duplicated items, using Groovy's
     * default number-aware comparator.
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
     * Modifies this List to remove all duplicated items, using Groovy's
     * default number-aware comparator.
     * <pre class="groovyTestCase">assert [1,3] == [1,3,3].unique()</pre>
     *
     * @param self a List
     * @return the now modified List
     * @see #unique(Collection, boolean)
     * @since 2.4.0
     */
    public static <T> List<T> unique(List<T> self) {
        return (List<T>) unique((Collection<T>) self, true);
    }

    /**
     * Remove all duplicates from a given Collection using Groovy's default number-aware comparator.
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
        List<T> answer = uniqueItems(self);
        if (mutate) {
            self.clear();
            self.addAll(answer);
        }
        return mutate ? self : answer ;
    }

    private static <T> List<T> uniqueItems(Iterable<T> self) {
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
        return answer;
    }

    /**
     * Remove all duplicates from a given List using Groovy's default number-aware comparator.
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
     * @param self a List
     * @param mutate false will cause a new List containing unique items from the List to be created, true will mutate List in place
     * @return the now modified List
     * @since 2.4.0
     */
    public static <T> List<T> unique(List<T> self, boolean mutate) {
        return (List<T>) unique((Collection<T>) self, mutate);
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
        return COMPARABLE_NUMBER_AWARE_COMPARATOR.compare(self, other);
    }

    /**
     * Returns an iterator equivalent to this iterator but with all duplicated items
     * removed by using a Closure to determine duplicate (equal) items.
     * The original iterator will be fully processed after the call.
     * <p>
     * If the closure takes a single parameter, the argument passed will be each element,
     * and the closure should return a value used for comparison (either using
     * {@link java.lang.Comparable#compareTo(java.lang.Object)} or {@link java.lang.Object#equals(java.lang.Object)}).
     * If the closure takes two parameters, two items from the Iterator
     * will be passed as arguments, and the closure should return an
     * int value (with 0 indicating the items are not unique).
     *
     * @param self      an Iterator
     * @param condition a Closure used to determine unique items
     * @return the modified Iterator
     * @since 1.5.5
     */
    public static <T> Iterator<T> unique(Iterator<T> self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure condition) {
        Comparator<T> comparator = condition.getMaximumNumberOfParameters() == 1
                ? new OrderBy<T>(condition, true)
                : new ClosureComparator<T>(condition);
        return unique(self, comparator);
    }

    /**
     * A convenience method for making a collection unique using a Closure
     * to determine duplicate (equal) items.
     * <p>
     * If the closure takes a single parameter, the
     * argument passed will be each element, and the closure
     * should return a value used for comparison (either using
     * {@link java.lang.Comparable#compareTo(java.lang.Object)} or {@link java.lang.Object#equals(java.lang.Object)}).
     * If the closure takes two parameters, two items from the collection
     * will be passed as arguments, and the closure should return an
     * int value (with 0 indicating the items are not unique).
     * <pre class="groovyTestCase">assert [1,4] == [1,3,4,5].unique { it % 2 }</pre>
     * <pre class="groovyTestCase">assert [2,3,4] == [2,3,3,4].unique { a, b {@code ->} a {@code <=>} b }</pre>
     *
     * @param self    a Collection
     * @param closure a 1 or 2 arg Closure used to determine unique items
     * @return self   without any duplicates
     * @see #unique(Collection, boolean, Closure)
     * @since 1.0
     */
    public static <T> Collection<T> unique(Collection<T> self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure closure) {
        return unique(self, true, closure);
    }

    /**
     * A convenience method for making a List unique using a Closure
     * to determine duplicate (equal) items.
     * <p>
     * If the closure takes a single parameter, the
     * argument passed will be each element, and the closure
     * should return a value used for comparison (either using
     * {@link java.lang.Comparable#compareTo(java.lang.Object)} or {@link java.lang.Object#equals(java.lang.Object)}).
     * If the closure takes two parameters, two items from the List
     * will be passed as arguments, and the closure should return an
     * int value (with 0 indicating the items are not unique).
     * <pre class="groovyTestCase">assert [1,4] == [1,3,4,5].unique { it % 2 }</pre>
     * <pre class="groovyTestCase">assert [2,3,4] == [2,3,3,4].unique { a, b {@code ->} a {@code <=>} b }</pre>
     *
     * @param self    a List
     * @param closure a 1 or 2 arg Closure used to determine unique items
     * @return self   without any duplicates
     * @see #unique(Collection, boolean, Closure)
     * @since 2.4.0
     */
    public static <T> List<T> unique(List<T> self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure closure) {
        return (List<T>) unique((Collection<T>) self, true, closure);
    }

    /**
     * A convenience method for making a collection unique using a Closure to determine duplicate (equal) items.
     * If mutate is true, it works on the receiver object and returns it. If mutate is false, a new collection is returned.
     * <p>
     * If the closure takes a single parameter, each element from the Collection will be passed to the closure. The closure
     * should return a value used for comparison (either using {@link java.lang.Comparable#compareTo(java.lang.Object)} or
     * {@link java.lang.Object#equals(java.lang.Object)}). If the closure takes two parameters, two items from the collection
     * will be passed as arguments, and the closure should return an int value (with 0 indicating the items are not unique).
     * <pre class="groovyTestCase">
     * def orig = [1, 3, 4, 5]
     * def uniq = orig.unique(false) { it % 2 }
     * assert orig == [1, 3, 4, 5]
     * assert uniq == [1, 4]
     * </pre>
     * <pre class="groovyTestCase">
     * def orig = [2, 3, 3, 4]
     * def uniq = orig.unique(false) { a, b {@code ->} a {@code <=>} b }
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
    public static <T> Collection<T> unique(Collection<T> self, boolean mutate, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure closure) {
        // use a comparator of one item or two
        int params = closure.getMaximumNumberOfParameters();
        if (params == 1) {
            self = unique(self, mutate, new OrderBy<T>(closure, true));
        } else {
            self = unique(self, mutate, new ClosureComparator<T>(closure));
        }
        return self;
    }

    /**
     * A convenience method for making a List unique using a Closure to determine duplicate (equal) items.
     * If mutate is true, it works on the receiver object and returns it. If mutate is false, a new collection is returned.
     * <p>
     * If the closure takes a single parameter, each element from the List will be passed to the closure. The closure
     * should return a value used for comparison (either using {@link java.lang.Comparable#compareTo(java.lang.Object)} or
     * {@link java.lang.Object#equals(java.lang.Object)}). If the closure takes two parameters, two items from the collection
     * will be passed as arguments, and the closure should return an int value (with 0 indicating the items are not unique).
     * <pre class="groovyTestCase">
     * def orig = [1, 3, 4, 5]
     * def uniq = orig.unique(false) { it % 2 }
     * assert orig == [1, 3, 4, 5]
     * assert uniq == [1, 4]
     * </pre>
     * <pre class="groovyTestCase">
     * def orig = [2, 3, 3, 4]
     * def uniq = orig.unique(false) { a, b {@code ->} a {@code <=>} b }
     * assert orig == [2, 3, 3, 4]
     * assert uniq == [2, 3, 4]
     * </pre>
     *
     * @param self    a List
     * @param mutate  false will always cause a new list to be created, true will mutate lists in place
     * @param closure a 1 or 2 arg Closure used to determine unique items
     * @return self   without any duplicates
     * @since 2.4.0
     */
    public static <T> List<T> unique(List<T> self, boolean mutate, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure closure) {
        return (List<T>) unique((Collection<T>) self, mutate, closure);
    }

    /**
     * Returns an iterator equivalent to this iterator with all duplicated
     * items removed by using the supplied comparator. The original iterator
     * will be exhausted upon returning.
     *
     * @param self an Iterator
     * @param comparator a Comparator
     * @return the modified Iterator
     * @since 1.5.5
     */
    public static <T> Iterator<T> unique(Iterator<T> self, Comparator<T> comparator) {
        return uniqueItems(new IteratorIterableAdapter<T>(self), comparator).listIterator();
    }

    private static final class IteratorIterableAdapter<T> implements Iterable<T> {
        private final Iterator<T> self;

        private IteratorIterableAdapter(Iterator<T> self) {
            this.self = self;
        }

        @Override
        public Iterator<T> iterator() {
            return self;
        }
    }

    /**
     * Remove all duplicates from a given Collection.
     * Works on the original object (and also returns it).
     * The order of members in the Collection are compared by the given Comparator.
     * For each duplicate, the first member which is returned
     * by the given Collection's iterator is retained, but all other ones are removed.
     * The given Collection's original order is preserved.
     * <p>
     * <pre class="groovyTestCase">
     * class Person {
     *     def fname, lname
     *     String toString() {
     *         return fname + " " + lname
     *     }
     * }
     *
     * class PersonComparator implements Comparator {
     *     int compare(Object o1, Object o2) {
     *         Person p1 = (Person) o1
     *         Person p2 = (Person) o2
     *         if (p1.lname != p2.lname)
     *             return p1.lname.compareTo(p2.lname)
     *         else
     *             return p1.fname.compareTo(p2.fname)
     *     }
     *
     *     boolean equals(Object obj) {
     *         return this.equals(obj)
     *     }
     * }
     *
     * Person a = new Person(fname:"John", lname:"Taylor")
     * Person b = new Person(fname:"Clark", lname:"Taylor")
     * Person c = new Person(fname:"Tom", lname:"Cruz")
     * Person d = new Person(fname:"Clark", lname:"Taylor")
     *
     * def list = [a, b, c, d]
     * List list2 = list.unique(new PersonComparator())
     * assert( list2 == list {@code &&} list == [a, b, c] )
     * </pre>
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
     * Remove all duplicates from a given List.
     * Works on the original object (and also returns it).
     * The order of members in the List are compared by the given Comparator.
     * For each duplicate, the first member which is returned
     * by the given List's iterator is retained, but all other ones are removed.
     * The given List's original order is preserved.
     * <p>
     * <pre class="groovyTestCase">
     * class Person {
     *     def fname, lname
     *     String toString() {
     *         return fname + " " + lname
     *     }
     * }
     *
     * class PersonComparator implements Comparator {
     *     int compare(Object o1, Object o2) {
     *         Person p1 = (Person) o1
     *         Person p2 = (Person) o2
     *         if (p1.lname != p2.lname)
     *             return p1.lname.compareTo(p2.lname)
     *         else
     *             return p1.fname.compareTo(p2.fname)
     *     }
     *
     *     boolean equals(Object obj) {
     *         return this.equals(obj)
     *     }
     * }
     *
     * Person a = new Person(fname:"John", lname:"Taylor")
     * Person b = new Person(fname:"Clark", lname:"Taylor")
     * Person c = new Person(fname:"Tom", lname:"Cruz")
     * Person d = new Person(fname:"Clark", lname:"Taylor")
     *
     * def list = [a, b, c, d]
     * List list2 = list.unique(new PersonComparator())
     * assert( list2 == list {@code &&} list == [a, b, c] )
     * </pre>
     *
     * @param self       a List
     * @param comparator a Comparator
     * @return self      the now modified List without duplicates
     * @see #unique(java.util.Collection, boolean, java.util.Comparator)
     * @since 2.4.0
     */
    public static <T> List<T> unique(List<T> self, Comparator<T> comparator) {
        return (List<T>) unique((Collection<T>) self, true, comparator);
    }

    /**
     * Remove all duplicates from a given Collection.
     * If mutate is true, it works on the original object (and also returns it). If mutate is false, a new collection is returned.
     * The order of members in the Collection are compared by the given Comparator.
     * For each duplicate, the first member which is returned
     * by the given Collection's iterator is retained, but all other ones are removed.
     * The given Collection's original order is preserved.
     * <p>
     * <pre class="groovyTestCase">
     * class Person {
     *     def fname, lname
     *     String toString() {
     *         return fname + " " + lname
     *     }
     * }
     *
     * class PersonComparator implements Comparator {
     *     int compare(Object o1, Object o2) {
     *         Person p1 = (Person) o1
     *         Person p2 = (Person) o2
     *         if (p1.lname != p2.lname)
     *             return p1.lname.compareTo(p2.lname)
     *         else
     *             return p1.fname.compareTo(p2.fname)
     *     }
     *
     *     boolean equals(Object obj) {
     *         return this.equals(obj)
     *     }
     * }
     *
     * Person a = new Person(fname:"John", lname:"Taylor")
     * Person b = new Person(fname:"Clark", lname:"Taylor")
     * Person c = new Person(fname:"Tom", lname:"Cruz")
     * Person d = new Person(fname:"Clark", lname:"Taylor")
     *
     * def list = [a, b, c, d]
     * List list2 = list.unique(false, new PersonComparator())
     * assert( list2 != list {@code &&} list2 == [a, b, c] )
     * </pre>
     *
     * @param self       a Collection
     * @param mutate     false will always cause a new collection to be created, true will mutate collections in place
     * @param comparator a Comparator
     * @return self      the collection without duplicates
     * @since 1.8.1
     */
    public static <T> Collection<T> unique(Collection<T> self, boolean mutate, Comparator<T> comparator) {
        List<T> answer = uniqueItems(self, comparator);
        if (mutate) {
            self.clear();
            self.addAll(answer);
        }
        return mutate ? self : answer;
    }

    private static <T> List<T> uniqueItems(Iterable<T> self, Comparator<T> comparator) {
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
        return answer;
    }

    /**
     * Remove all duplicates from a given List.
     * If mutate is true, it works on the original object (and also returns it). If mutate is false, a new List is returned.
     * The order of members in the List are compared by the given Comparator.
     * For each duplicate, the first member which is returned
     * by the given List's iterator is retained, but all other ones are removed.
     * The given List's original order is preserved.
     * <p>
     * <pre class="groovyTestCase">
     * class Person {
     *     def fname, lname
     *     String toString() {
     *         return fname + " " + lname
     *     }
     * }
     *
     * class PersonComparator implements Comparator {
     *     int compare(Object o1, Object o2) {
     *         Person p1 = (Person) o1
     *         Person p2 = (Person) o2
     *         if (p1.lname != p2.lname)
     *             return p1.lname.compareTo(p2.lname)
     *         else
     *             return p1.fname.compareTo(p2.fname)
     *     }
     *
     *     boolean equals(Object obj) {
     *         return this.equals(obj)
     *     }
     * }
     *
     * Person a = new Person(fname:"John", lname:"Taylor")
     * Person b = new Person(fname:"Clark", lname:"Taylor")
     * Person c = new Person(fname:"Tom", lname:"Cruz")
     * Person d = new Person(fname:"Clark", lname:"Taylor")
     *
     * def list = [a, b, c, d]
     * List list2 = list.unique(false, new PersonComparator())
     * assert( list2 != list {@code &&} list2 == [a, b, c] )
     * </pre>
     *
     * @param self       a List
     * @param mutate     false will always cause a new List to be created, true will mutate List in place
     * @param comparator a Comparator
     * @return self      the List without duplicates
     * @since 2.4.0
     */
    public static <T> List<T> unique(List<T> self, boolean mutate, Comparator<T> comparator) {
        return (List<T>) unique((Collection<T>) self, mutate, comparator);
    }

    /**
     * Returns an iterator equivalent to this iterator but with all duplicated items
     * removed where duplicate (equal) items are deduced by calling the supplied Closure condition.
     * <p>
     * If the supplied Closure takes a single parameter, the argument passed will be each element,
     * and the closure should return a value used for comparison (either using
     * {@link java.lang.Comparable#compareTo(java.lang.Object)} or {@link java.lang.Object#equals(java.lang.Object)}).
     * If the closure takes two parameters, two items from the Iterator
     * will be passed as arguments, and the closure should return an
     * int value (with 0 indicating the items are not unique).
     * <pre class="groovyTestCase">
     * def items = "Hello".toList() + [null, null] + "there".toList()
     * def toLower = { it == null ? null : it.toLowerCase() }
     * def noDups = items.iterator().toUnique(toLower).toList()
     * assert noDups == ['H', 'e', 'l', 'o', null, 't', 'r']
     * </pre>
     * <pre class="groovyTestCase">assert [1,4] == [1,3,4,5].toUnique { it % 2 }</pre>
     * <pre class="groovyTestCase">assert [2,3,4] == [2,3,3,4].toUnique { a, b {@code ->} a {@code <=>} b }</pre>
     *
     * @param self an Iterator
     * @param condition a Closure used to determine unique items
     * @return an Iterator with no duplicate items
     * @since 2.4.0
     */
    public static <T> Iterator<T> toUnique(Iterator<T> self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure condition) {
        return toUnique(self, condition.getMaximumNumberOfParameters() == 1
                ? new OrderBy<T>(condition, true)
                : new ClosureComparator<T>(condition));
    }

    private static final class ToUniqueIterator<E> implements Iterator<E> {
        private final Iterator<E> delegate;
        private final Set<E> seen;
        private boolean exhausted;
        private E next;

        private ToUniqueIterator(Iterator<E> delegate, Comparator<E> comparator) {
            this.delegate = delegate;
            seen = new TreeSet<E>(comparator);
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
            boolean foundNext = false;
            while (!foundNext && !exhausted) {
                exhausted = !delegate.hasNext();
                if (!exhausted) {
                    next = delegate.next();
                    foundNext = seen.add(next);
                }
            }
        }
    }

    /**
     * Returns an iterator equivalent to this iterator with all duplicated
     * items removed by using the supplied comparator.
     *
     * @param self an Iterator
     * @param comparator a Comparator used to determine unique (equal) items
     *        If {@code null}, the Comparable natural ordering of the elements will be used.
     * @return an Iterator with no duplicate items
     * @since 2.4.0
     */
    public static <T> Iterator<T> toUnique(Iterator<T> self, Comparator<T> comparator) {
        return new ToUniqueIterator<T>(self, comparator);
    }

    /**
     * Returns an iterator equivalent to this iterator with all duplicated
     * items removed by using the natural ordering of the items.
     *
     * @param self an Iterator
     * @return an Iterator with no duplicate items
     * @since 2.4.0
     */
    public static <T> Iterator<T> toUnique(Iterator<T> self) {
        return toUnique(self, (Comparator<T>) null);
    }

    /**
     * Returns a Collection containing the items from the Iterable but with duplicates removed.
     * The items in the Iterable are compared by the given Comparator.
     * For each duplicate, the first member which is returned from the
     * Iterable is retained, but all other ones are removed.
     * <p>
     * <pre class="groovyTestCase">
     * class Person {
     *     def fname, lname
     *     String toString() {
     *         return fname + " " + lname
     *     }
     * }
     *
     * class PersonComparator implements Comparator {
     *     int compare(Object o1, Object o2) {
     *         Person p1 = (Person) o1
     *         Person p2 = (Person) o2
     *         if (p1.lname != p2.lname)
     *             return p1.lname.compareTo(p2.lname)
     *         else
     *             return p1.fname.compareTo(p2.fname)
     *     }
     *
     *     boolean equals(Object obj) {
     *         return this.equals(obj)
     *     }
     * }
     *
     * Person a = new Person(fname:"John", lname:"Taylor")
     * Person b = new Person(fname:"Clark", lname:"Taylor")
     * Person c = new Person(fname:"Tom", lname:"Cruz")
     * Person d = new Person(fname:"Clark", lname:"Taylor")
     *
     * def list = [a, b, c, d]
     * List list2 = list.toUnique(new PersonComparator())
     * assert list2 == [a, b, c] {@code &&} list == [a, b, c, d]
     * </pre>
     *
     * @param self       an Iterable
     * @param comparator a Comparator used to determine unique (equal) items
     *        If {@code null}, the Comparable natural ordering of the elements will be used.
     * @return the Collection of non-duplicate items
     * @since 2.4.0
     */
    public static <T> Collection<T> toUnique(Iterable<T> self, Comparator<T> comparator) {
        Collection<T> result = createSimilarCollection((Collection<T>) self);
        addAll(result, toUnique(self.iterator(), comparator));
        return result;
    }

    /**
     * Returns a List containing the items from the List but with duplicates removed.
     * The items in the List are compared by the given Comparator.
     * For each duplicate, the first member which is returned from the
     * List is retained, but all other ones are removed.
     * <p>
     * <pre class="groovyTestCase">
     * class Person {
     *     def fname, lname
     *     String toString() {
     *         return fname + " " + lname
     *     }
     * }
     *
     * class PersonComparator implements Comparator {
     *     int compare(Object o1, Object o2) {
     *         Person p1 = (Person) o1
     *         Person p2 = (Person) o2
     *         if (p1.lname != p2.lname)
     *             return p1.lname.compareTo(p2.lname)
     *         else
     *             return p1.fname.compareTo(p2.fname)
     *     }
     *
     *     boolean equals(Object obj) {
     *         return this.equals(obj)
     *     }
     * }
     *
     * Person a = new Person(fname:"John", lname:"Taylor")
     * Person b = new Person(fname:"Clark", lname:"Taylor")
     * Person c = new Person(fname:"Tom", lname:"Cruz")
     * Person d = new Person(fname:"Clark", lname:"Taylor")
     *
     * def list = [a, b, c, d]
     * List list2 = list.toUnique(new PersonComparator())
     * assert list2 == [a, b, c] {@code &&} list == [a, b, c, d]
     * </pre>
     *
     * @param self       a List
     * @param comparator a Comparator used to determine unique (equal) items
     *        If {@code null}, the Comparable natural ordering of the elements will be used.
     * @return the List of non-duplicate items
     * @since 2.4.0
     */
    public static <T> List<T> toUnique(List<T> self, Comparator<T> comparator) {
        return (List<T>) toUnique((Iterable<T>) self, comparator);
    }

    /**
     * Returns a Collection containing the items from the Iterable but with duplicates removed
     * using the natural ordering of the items to determine uniqueness.
     * <p>
     * <pre class="groovyTestCase">
     * String[] letters = ['c', 'a', 't', 's', 'a', 't', 'h', 'a', 't']
     * String[] expected = ['c', 'a', 't', 's', 'h']
     * assert letters.toUnique() == expected
     * </pre>
     *
     * @param self       an Iterable
     * @return the Collection of non-duplicate items
     * @since 2.4.0
     */
    public static <T> Collection<T> toUnique(Iterable<T> self) {
        return toUnique(self, (Comparator<T>) null);
    }

    /**
     * Returns a List containing the items from the List but with duplicates removed
     * using the natural ordering of the items to determine uniqueness.
     * <p>
     * <pre class="groovyTestCase">
     * def letters = ['c', 'a', 't', 's', 'a', 't', 'h', 'a', 't']
     * def expected = ['c', 'a', 't', 's', 'h']
     * assert letters.toUnique() == expected
     * </pre>
     *
     * @param self       a List
     * @return the List of non-duplicate items
     * @since 2.4.0
     */
    public static <T> List<T> toUnique(List<T> self) {
        return toUnique(self, (Comparator<T>) null);
    }

    /**
     * Returns a Collection containing the items from the Iterable but with duplicates removed.
     * The items in the Iterable are compared by the given Closure condition.
     * For each duplicate, the first member which is returned from the
     * Iterable is retained, but all other ones are removed.
     * <p>
     * If the closure takes a single parameter, each element from the Iterable will be passed to the closure. The closure
     * should return a value used for comparison (either using {@link java.lang.Comparable#compareTo(java.lang.Object)} or
     * {@link java.lang.Object#equals(java.lang.Object)}). If the closure takes two parameters, two items from the Iterable
     * will be passed as arguments, and the closure should return an int value (with 0 indicating the items are not unique).
     * <p>
     * <pre class="groovyTestCase">
     * class Person {
     *     def fname, lname
     *     String toString() {
     *         return fname + " " + lname
     *     }
     * }
     *
     * Person a = new Person(fname:"John", lname:"Taylor")
     * Person b = new Person(fname:"Clark", lname:"Taylor")
     * Person c = new Person(fname:"Tom", lname:"Cruz")
     * Person d = new Person(fname:"Clark", lname:"Taylor")
     *
     * def list = [a, b, c, d]
     * def list2 = list.toUnique{ p1, p2 {@code ->} p1.lname != p2.lname ? p1.lname &lt;=&gt; p2.lname : p1.fname &lt;=&gt; p2.fname }
     * assert( list2 == [a, b, c] {@code &&} list == [a, b, c, d] )
     * def list3 = list.toUnique{ it.toString() }
     * assert( list3 == [a, b, c] {@code &&} list == [a, b, c, d] )
     * </pre>
     *
     * @param self      an Iterable
     * @param condition a Closure used to determine unique items
     * @return a new Collection
     * @see #toUnique(Iterable, Comparator)
     * @since 2.4.0
     */
    public static <T> Collection<T> toUnique(Iterable<T> self, @ClosureParams(value = FromString.class, options = {"T", "T,T"}) Closure condition) {
        Comparator<T> comparator = condition.getMaximumNumberOfParameters() == 1
                ? new OrderBy<T>(condition, true)
                : new ClosureComparator<T>(condition);
        return toUnique(self, comparator);
    }

    /**
     * Returns a List containing the items from the List but with duplicates removed.
     * The items in the List are compared by the given Closure condition.
     * For each duplicate, the first member which is returned from the
     * Iterable is retained, but all other ones are removed.
     * <p>
     * If the closure takes a single parameter, each element from the Iterable will be passed to the closure. The closure
     * should return a value used for comparison (either using {@link java.lang.Comparable#compareTo(java.lang.Object)} or
     * {@link java.lang.Object#equals(java.lang.Object)}). If the closure takes two parameters, two items from the Iterable
     * will be passed as arguments, and the closure should return an int value (with 0 indicating the items are not unique).
     * <p>
     * <pre class="groovyTestCase">
     * class Person {
     *     def fname, lname
     *     String toString() {
     *         return fname + " " + lname
     *     }
     * }
     *
     * Person a = new Person(fname:"John", lname:"Taylor")
     * Person b = new Person(fname:"Clark", lname:"Taylor")
     * Person c = new Person(fname:"Tom", lname:"Cruz")
     * Person d = new Person(fname:"Clark", lname:"Taylor")
     *
     * def list = [a, b, c, d]
     * def list2 = list.toUnique{ p1, p2 {@code ->} p1.lname != p2.lname ? p1.lname &lt;=&gt; p2.lname : p1.fname &lt;=&gt; p2.fname }
     * assert( list2 == [a, b, c] {@code &&} list == [a, b, c, d] )
     * def list3 = list.toUnique{ it.toString() }
     * assert( list3 == [a, b, c] {@code &&} list == [a, b, c, d] )
     * </pre>
     *
     * @param self      a List
     * @param condition a Closure used to determine unique items
     * @return a new List
     * @see #toUnique(Iterable, Comparator)
     * @since 2.4.0
     */
    public static <T> List<T> toUnique(List<T> self, @ClosureParams(value = FromString.class, options = {"T", "T,T"}) Closure condition) {
        return (List<T>) toUnique((Iterable<T>) self, condition);
    }

    /**
     * Returns a new Array containing the items from the original Array but with duplicates removed with the supplied
     * comparator determining which items are unique.
     * <p>
     * <pre class="groovyTestCase">
     * String[] letters = ['c', 'a', 't', 's', 'A', 't', 'h', 'a', 'T']
     * String[] lower = ['c', 'a', 't', 's', 'h']
     * class LowerComparator implements Comparator {
     *     int compare(let1, let2) { let1.toLowerCase() {@code <=>} let2.toLowerCase() }
     * }
     * assert letters.toUnique(new LowerComparator()) == lower
     * </pre>
     *
     * @param self an array
     * @param comparator a Comparator used to determine unique (equal) items
     *        If {@code null}, the Comparable natural ordering of the elements will be used.
     * @return the unique items from the array
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toUnique(T[] self, Comparator<T> comparator) {
        Collection<T> items = toUnique(toList(self), comparator);
        T[] result = createSimilarArray(self, items.size());
        return items.toArray(result);
    }

    /**
     * Returns a new Array containing the items from the original Array but with duplicates removed using the
     * natural ordering of the items in the array.
     * <p>
     * <pre class="groovyTestCase">
     * String[] letters = ['c', 'a', 't', 's', 'a', 't', 'h', 'a', 't']
     * String[] expected = ['c', 'a', 't', 's', 'h']
     * def result = letters.toUnique()
     * assert result == expected
     * assert result.class.componentType == String
     * </pre>
     *
     * @param self an array
     * @return the unique items from the array
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toUnique(T[] self) {
        return (T[]) toUnique(self, (Comparator) null);
    }

    /**
     * Returns a new Array containing the items from the original Array but with duplicates removed with the supplied
     * comparator determining which items are unique.
     * <p>
     * <pre class="groovyTestCase">
     * String[] letters = ['c', 'a', 't', 's', 'A', 't', 'h', 'a', 'T']
     * String[] expected = ['c', 'a', 't', 's', 'h']
     * assert letters.toUnique{ p1, p2 {@code ->} p1.toLowerCase() {@code <=>} p2.toLowerCase() } == expected
     * assert letters.toUnique{ it.toLowerCase() } == expected
     * </pre>
     *
     * @param self an array
     * @param condition a Closure used to determine unique items
     * @return the unique items from the array
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toUnique(T[] self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure condition) {
        Comparator<T> comparator = condition.getMaximumNumberOfParameters() == 1
                ? new OrderBy<T>(condition, true)
                : new ClosureComparator<T>(condition);
        return toUnique(self, comparator);
    }

    /**
     * Iterates through an array passing each array entry to the given closure.
     * <pre class="groovyTestCase">
     * String[] letters = ['a', 'b', 'c']
     * String result = ''
     * letters.each{ result += it }
     * assert result == 'abc'
     * </pre>
     *
     * @param self    the array over which we iterate
     * @param closure the closure applied on each array entry
     * @return the self array
     * @since 2.5.0
     */
    public static <T> T[] each(T[] self, @ClosureParams(FirstParam.Component.class) Closure closure) {
        for(T item : self){
            closure.call(item);
        }
        return self;
    }

    /**
     * Iterates through an aggregate type or data structure,
     * passing each item to the given closure.  Custom types may utilize this
     * method by simply providing an "iterator()" method.  The items returned
     * from the resulting iterator will be passed to the closure.
     * <pre class="groovyTestCase">
     * String result = ''
     * ['a', 'b', 'c'].each{ result += it }
     * assert result == 'abc'
     * </pre>
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
     * Iterates through an array,
     * passing each array element and the element's index (a counter starting at
     * zero) to the given closure.
     * <pre class="groovyTestCase">
     * String[] letters = ['a', 'b', 'c']
     * String result = ''
     * letters.eachWithIndex{ letter, index {@code ->} result += "$index:$letter" }
     * assert result == '0:a1:b2:c'
     * </pre>
     *
     * @param self    an array
     * @param closure a Closure to operate on each array entry
     * @return the self array
     * @since 2.5.0
     */
    public static <T> T[] eachWithIndex(T[] self, @ClosureParams(value=FromString.class, options="T,Integer") Closure closure) {
        final Object[] args = new Object[2];
        int counter = 0;
        for(T item : self) {
            args[0] = item;
            args[1] = counter++;
            closure.call(args);
        }
        return self;
    }

    /**
     * Iterates through an aggregate type or data structure,
     * passing each item and the item's index (a counter starting at
     * zero) to the given closure.
     * <pre class="groovyTestCase">
     * String result = ''
     * ['a', 'b', 'c'].eachWithIndex{ letter, index {@code ->} result += "$index:$letter" }
     * assert result == '0:a1:b2:c'
     * </pre>
     *
     * @param self    an Object
     * @param closure a Closure to operate on each item
     * @return the self Object
     * @since 1.0
     */
    public static <T> T eachWithIndex(T self, /*@ClosureParams(value=FromString.class, options="?,Integer")*/ Closure closure) {
        final Object[] args = new Object[2];
        int counter = 0;
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext();) {
            args[0] = iter.next();
            args[1] = counter++;
            closure.call(args);
        }
        return self;
    }

    /**
     * Iterates through an iterable type,
     * passing each item and the item's index (a counter starting at
     * zero) to the given closure.
     *
     * @param self    an Iterable
     * @param closure a Closure to operate on each item
     * @return the self Iterable
     * @since 2.3.0
     */
    public static <T> Iterable<T> eachWithIndex(Iterable<T> self, @ClosureParams(value=FromString.class, options="T,java.lang.Integer") Closure closure) {
        eachWithIndex(self.iterator(), closure);
        return self;
    }

    /**
     * Iterates through an iterator type,
     * passing each item and the item's index (a counter starting at
     * zero) to the given closure.
     *
     * @param self    an Iterator
     * @param closure a Closure to operate on each item
     * @return the self Iterator (now exhausted)
     * @since 2.3.0
     */
    public static <T> Iterator<T> eachWithIndex(Iterator<T> self, @ClosureParams(value=FromString.class, options="T,java.lang.Integer") Closure closure) {
        final Object[] args = new Object[2];
        int counter = 0;
        while (self.hasNext()) {
            args[0] = self.next();
            args[1] = counter++;
            closure.call(args);
        }
        return self;
    }

    /**
     * Iterates through a Collection,
     * passing each item and the item's index (a counter starting at
     * zero) to the given closure.
     *
     * @param self    a Collection
     * @param closure a Closure to operate on each item
     * @return the self Collection
     * @since 2.4.0
     */
    public static <T> Collection<T> eachWithIndex(Collection<T> self, @ClosureParams(value=FromString.class, options="T,java.lang.Integer") Closure closure) {
        return (Collection<T>) eachWithIndex((Iterable<T>) self, closure);
    }

    /**
     * Iterates through a List,
     * passing each item and the item's index (a counter starting at
     * zero) to the given closure.
     *
     * @param self    a List
     * @param closure a Closure to operate on each item
     * @return the self List
     * @since 2.4.0
     */
    public static <T> List<T> eachWithIndex(List<T> self, @ClosureParams(value=FromString.class, options="T,java.lang.Integer") Closure closure) {
        return (List<T>) eachWithIndex((Iterable<T>) self, closure);
    }

    /**
     * Iterates through a Set,
     * passing each item and the item's index (a counter starting at
     * zero) to the given closure.
     *
     * @param self    a Set
     * @param closure a Closure to operate on each item
     * @return the self Set
     * @since 2.4.0
     */
    public static <T> Set<T> eachWithIndex(Set<T> self, @ClosureParams(value=FromString.class, options="T,java.lang.Integer") Closure closure) {
        return (Set<T>) eachWithIndex((Iterable<T>) self, closure);
    }

    /**
     * Iterates through a SortedSet,
     * passing each item and the item's index (a counter starting at
     * zero) to the given closure.
     *
     * @param self    a SortedSet
     * @param closure a Closure to operate on each item
     * @return the self SortedSet
     * @since 2.4.0
     */
    public static <T> SortedSet<T> eachWithIndex(SortedSet<T> self, @ClosureParams(value=FromString.class, options="T,java.lang.Integer") Closure closure) {
        return (SortedSet<T>) eachWithIndex((Iterable<T>) self, closure);
    }

    /**
     * Iterates through an Iterable, passing each item to the given closure.
     *
     * @param self    the Iterable over which we iterate
     * @param closure the closure applied on each element found
     * @return the self Iterable
     */
    public static <T> Iterable<T> each(Iterable<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        each(self.iterator(), closure);
        return self;
    }

    /**
     * Iterates through an Iterator, passing each item to the given closure.
     *
     * @param self    the Iterator over which we iterate
     * @param closure the closure applied on each element found
     * @return the self Iterator
     * @since 2.4.0
     */
    public static <T> Iterator<T> each(Iterator<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        while (self.hasNext()) {
            Object arg = self.next();
            closure.call(arg);
        }
        return self;
    }

    /**
     * Iterates through a Collection, passing each item to the given closure.
     *
     * @param self    the Collection over which we iterate
     * @param closure the closure applied on each element found
     * @return the self Collection
     * @since 2.4.0
     */
    public static <T> Collection<T> each(Collection<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        return (Collection<T>) each((Iterable<T>) self, closure);
    }

    /**
     * Iterates through a List, passing each item to the given closure.
     *
     * @param self    the List over which we iterate
     * @param closure the closure applied on each element found
     * @return the self List
     * @since 2.4.0
     */
    public static <T> List<T> each(List<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        return (List<T>) each((Iterable<T>) self, closure);
    }

    /**
     * Iterates through a Set, passing each item to the given closure.
     *
     * @param self    the Set over which we iterate
     * @param closure the closure applied on each element found
     * @return the self Set
     * @since 2.4.0
     */
    public static <T> Set<T> each(Set<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        return (Set<T>) each((Iterable<T>) self, closure);
    }

    /**
     * Iterates through a SortedSet, passing each item to the given closure.
     *
     * @param self    the SortedSet over which we iterate
     * @param closure the closure applied on each element found
     * @return the self SortedSet
     * @since 2.4.0
     */
    public static <T> SortedSet<T> each(SortedSet<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        return (SortedSet<T>) each((Iterable<T>) self, closure);
    }

    /**
     * Allows a Map to be iterated through using a closure. If the
     * closure takes one parameter then it will be passed the Map.Entry
     * otherwise if the closure takes two parameters then it will be
     * passed the key and the value.
     * <pre class="groovyTestCase">def result = ""
     * [a:1, b:3].each { key, value {@code ->} result += "$key$value" }
     * assert result == "a1b3"</pre>
     * <pre class="groovyTestCase">def result = ""
     * [a:1, b:3].each { entry {@code ->} result += entry }
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
    public static <K, V> Map<K, V> each(Map<K, V> self, @ClosureParams(MapEntryOrKeyValue.class) Closure closure) {
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
    public static <K, V> Map<K, V> reverseEach(Map<K, V> self, @ClosureParams(MapEntryOrKeyValue.class) Closure closure) {
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
     * [a:1, b:3].eachWithIndex { key, value, index {@code ->} result += "$index($key$value)" }
     * assert result == "0(a1)1(b3)"</pre>
     * <pre class="groovyTestCase">def result = ""
     * [a:1, b:3].eachWithIndex { entry, index {@code ->} result += "$index($entry)" }
     * assert result == "0(a=1)1(b=3)"</pre>
     *
     * @param self    the map over which we iterate
     * @param closure a 2 or 3 arg Closure to operate on each item
     * @return the self Object
     * @since 1.5.0
     */
    public static <K, V> Map<K, V> eachWithIndex(Map<K, V> self, @ClosureParams(value=MapEntryOrKeyValue.class, options="index=true") Closure closure) {
        int counter = 0;
        for (Map.Entry entry : self.entrySet()) {
            callClosureForMapEntryAndCounter(closure, entry, counter++);
        }
        return self;
    }

    /**
     * Iterate over each element of the list in the reverse order.
     * <pre class="groovyTestCase">def result = []
     * [1,2,3].reverseEach { result &lt;&lt; it }
     * assert result == [3,2,1]</pre>
     *
     * @param self    a List
     * @param closure a closure to which each item is passed.
     * @return the original list
     * @since 1.5.0
     */
    public static <T> List<T> reverseEach(List<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        each(new ReverseListIterator<T>(self), closure);
        return self;
    }

    /**
     * Iterate over each element of the array in the reverse order.
     *
     * @param self    an array
     * @param closure a closure to which each item is passed
     * @return the original array
     * @since 1.5.2
     */
    public static <T> T[] reverseEach(T[] self, @ClosureParams(FirstParam.Component.class) Closure closure) {
        each(new ReverseListIterator<T>(Arrays.asList(self)), closure);
        return self;
    }

    /**
     * Used to determine if the given predicate closure is valid (i.e. returns
     * <code>true</code> for all items in this data structure).
     * A simple example for a list:
     * <pre>def list = [3,4,5]
     * def greaterThanTwo = list.every { it {@code >} 2 }
     * </pre>
     *
     * @param self      the object over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if every iteration of the object matches the closure predicate
     * @since 1.0
     */
    public static boolean every(Object self, Closure predicate) {
        return every(InvokerHelper.asIterator(self), predicate);
    }

    /**
     * Used to determine if the given predicate closure is valid (i.e. returns
     * <code>true</code> for all items in this iterator).
     * A simple example for a list:
     * <pre>def list = [3,4,5]
     * def greaterThanTwo = list.iterator().every { it {@code >} 2 }
     * </pre>
     *
     * @param self      the iterator over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if every iteration of the object matches the closure predicate
     * @since 2.3.0
     */
    public static <T> boolean every(Iterator<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure predicate) {
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        while (self.hasNext()) {
            if (!bcw.call(self.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Used to determine if the given predicate closure is valid (i.e. returns
     * <code>true</code> for all items in this Array).
     *
     * @param self      an Array
     * @param predicate the closure predicate used for matching
     * @return true if every element of the Array matches the closure predicate
     * @since 2.5.0
     */
    public static <T> boolean every(T[] self, @ClosureParams(FirstParam.Component.class) Closure predicate) {
        return every(new ArrayIterator<T>(self), predicate);
    }

    /**
     * Used to determine if the given predicate closure is valid (i.e. returns
     * <code>true</code> for all items in this iterable).
     * A simple example for a list:
     * <pre>def list = [3,4,5]
     * def greaterThanTwo = list.every { it {@code >} 2 }
     * </pre>
     *
     * @param self      the iterable over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if every iteration of the object matches the closure predicate
     * @since 2.3.0
     */
    public static <T> boolean every(Iterable<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure predicate) {
        return every(self.iterator(), predicate);
    }

    /**
     * Iterates over the entries of a map, and checks whether a predicate is
     * valid for all entries. If the
     * closure takes one parameter then it will be passed the Map.Entry
     * otherwise if the closure takes two parameters then it will be
     * passed the key and the value.
     * <pre class="groovyTestCase">def map = [a:1, b:2.0, c:2L]
     * assert !map.every { key, value {@code ->} value instanceof Integer }
     * assert map.every { entry {@code ->} entry.value instanceof Number }</pre>
     *
     * @param self      the map over which we iterate
     * @param predicate the 1 or 2 arg Closure predicate used for matching
     * @return true if every entry of the map matches the closure predicate
     * @since 1.5.0
     */
    public static <K, V> boolean every(Map<K, V> self, @ClosureParams(value = MapEntryOrKeyValue.class) Closure predicate) {
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
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
     * Equivalent to <code>self.every({element {@code ->} element})</code>
     * <pre class="groovyTestCase">
     * assert [true, true].every()
     * assert [1, 1].every()
     * assert ![1, 0].every()
     * </pre>
     *
     * @param self the object over which we iterate
     * @return true if every item in the collection matches satisfies Groovy truth
     * @since 1.5.0
     */
    public static boolean every(Object self) {
        BooleanReturningMethodInvoker bmi = new BooleanReturningMethodInvoker();
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext(); ) {
            if (!bmi.convertToBoolean(iter.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Iterates over the contents of an object or collection, and checks whether a
     * predicate is valid for at least one element.
     * <pre class="groovyTestCase">
     * assert [1, 2, 3].any { it == 2 }
     * assert ![1, 2, 3].any { it {@code >} 3 }
     * </pre>
     *
     * @param self      the object over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the object matches the closure predicate
     * @since 1.0
     */
    public static boolean any(Object self, Closure predicate) {
        return any(InvokerHelper.asIterator(self), predicate);
    }

    /**
     * Iterates over the contents of an iterator, and checks whether a
     * predicate is valid for at least one element.
     * <pre class="groovyTestCase">
     * assert [1, 2, 3].iterator().any { it == 2 }
     * assert ![1, 2, 3].iterator().any { it {@code >} 3 }
     * </pre>
     *
     * @param self      the iterator over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the object matches the closure predicate
     * @since 1.0
     */
    public static <T> boolean any(Iterator<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure predicate) {
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
        while (self.hasNext()) {
            if (bcw.call(self.next())) return true;
        }
        return false;
    }

    /**
     * Iterates over the contents of an iterable, and checks whether a
     * predicate is valid for at least one element.
     * <pre class="groovyTestCase">
     * assert [1, 2, 3].any { it == 2 }
     * assert ![1, 2, 3].any { it {@code >} 3 }
     * </pre>
     *
     * @param self      the iterable over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the object matches the closure predicate
     * @since 1.0
     */
    public static <T> boolean any(Iterable<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure predicate) {
        return any(self.iterator(), predicate);
    }

    /**
     * Iterates over the contents of an Array, and checks whether a
     * predicate is valid for at least one element.
     *
     * @param self      the array over which we iterate
     * @param predicate the closure predicate used for matching
     * @return true if any iteration for the object matches the closure predicate
     * @since 2.5.0
     */
    public static <T> boolean any(T[] self, @ClosureParams(FirstParam.Component.class) Closure predicate) {
        return any(new ArrayIterator<T>(self), predicate);
    }

    /**
     * Iterates over the entries of a map, and checks whether a predicate is
     * valid for at least one entry. If the
     * closure takes one parameter then it will be passed the Map.Entry
     * otherwise if the closure takes two parameters then it will be
     * passed the key and the value.
     * <pre class="groovyTestCase">
     * assert [2:3, 4:5, 5:10].any { key, value {@code ->} key * 2 == value }
     * assert ![2:3, 4:5, 5:10].any { entry {@code ->} entry.key == entry.value * 2 }
     * </pre>
     *
     * @param self      the map over which we iterate
     * @param predicate the 1 or 2 arg closure predicate used for matching
     * @return true if any entry in the map matches the closure predicate
     * @since 1.5.0
     */
    public static <K, V> boolean any(Map<K, V> self, @ClosureParams(MapEntryOrKeyValue.class) Closure<?> predicate) {
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(predicate);
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
     * Equivalent to self.any({element {@code ->} element})
     * <pre class="groovyTestCase">
     * assert [false, true].any()
     * assert [0, 1].any()
     * assert ![0, 0].any()
     * </pre>
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
     * Iterates over the collection of items and returns each item that matches
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
     * @param self   a collection
     * @param filter the filter to perform on each element of the collection (using the {@link #isCase(java.lang.Object, java.lang.Object)} method)
     * @return a collection of objects which match the filter
     * @since 2.0
     */
    public static <T> Collection<T> grep(Collection<T> self, Object filter) {
        Collection<T> answer = createSimilarCollection(self);
        BooleanReturningMethodInvoker bmi = new BooleanReturningMethodInvoker("isCase");
        for (T element : self) {
            if (bmi.invoke(filter, element)) {
                answer.add(element);
            }
        }
        return answer;
    }

    /**
     * Iterates over the collection of items and returns each item that matches
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
     * @param self   a List
     * @param filter the filter to perform on each element of the collection (using the {@link #isCase(java.lang.Object, java.lang.Object)} method)
     * @return a List of objects which match the filter
     * @since 2.4.0
     */
    public static <T> List<T> grep(List<T> self, Object filter) {
        return (List<T>) grep((Collection<T>) self, filter);
    }

    /**
     * Iterates over the collection of items and returns each item that matches
     * the given filter - calling the <code>{@link #isCase(java.lang.Object, java.lang.Object)}</code>
     * method used by switch statements.  This method can be used with different
     * kinds of filters like regular expressions, classes, ranges etc.
     * Example:
     * <pre class="groovyTestCase">
     * def set = ['a', 'b', 'aa', 'bc', 3, 4.5] as Set
     * assert set.grep( ~/a+/ )  == ['a', 'aa'] as Set
     * assert set.grep( ~/../ )  == ['aa', 'bc'] as Set
     * assert set.grep( Number ) == [ 3, 4.5 ] as Set
     * assert set.grep{ it.toString().size() == 1 } == [ 'a', 'b', 3 ] as Set
     * </pre>
     *
     * @param self   a Set
     * @param filter the filter to perform on each element of the collection (using the {@link #isCase(java.lang.Object, java.lang.Object)} method)
     * @return a Set of objects which match the filter
     * @since 2.4.0
     */
    public static <T> Set<T> grep(Set<T> self, Object filter) {
        return (Set<T>) grep((Collection<T>) self, filter);
    }

    /**
     * Iterates over the array of items and returns a collection of items that match
     * the given filter - calling the <code>{@link #isCase(java.lang.Object, java.lang.Object)}</code>
     * method used by switch statements. This method can be used with different
     * kinds of filters like regular expressions, classes, ranges etc.
     * Example:
     * <pre class="groovyTestCase">
     * def items = ['a', 'b', 'aa', 'bc', 3, 4.5] as Object[]
     * assert items.grep( ~/a+/ )  == ['a', 'aa']
     * assert items.grep( ~/../ )  == ['aa', 'bc']
     * assert items.grep( Number ) == [ 3, 4.5 ]
     * assert items.grep{ it.toString().size() == 1 } == [ 'a', 'b', 3 ]
     * </pre>
     *
     * @param self   an array
     * @param filter the filter to perform on each element of the array (using the {@link #isCase(java.lang.Object, java.lang.Object)} method)
     * @return a collection of objects which match the filter
     * @since 2.0
     */
    public static <T> Collection<T> grep(T[] self, Object filter) {
        Collection<T> answer = new ArrayList<T>();
        BooleanReturningMethodInvoker bmi = new BooleanReturningMethodInvoker("isCase");
        for (T element : self) {
            if (bmi.invoke(filter, element)) {
                answer.add(element);
            }
        }
        return answer;
    }

    /**
     * Iterates over the collection of items which this Object represents and returns each item that matches
     * using the IDENTITY Closure as a filter - effectively returning all elements which satisfy Groovy truth.
     * <p>
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
     * Iterates over the collection returning each element that matches
     * using the IDENTITY Closure as a filter - effectively returning all elements which satisfy Groovy truth.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * def items = [1, 2, 0, false, true, '', 'foo', [], [4, 5], null]
     * assert items.grep() == [1, 2, true, 'foo', [4, 5]]
     * </pre>
     *
     * @param self a Collection
     * @return a collection of elements satisfy Groovy truth
     * @see Closure#IDENTITY
     * @since 2.0
     */
    public static <T> Collection<T> grep(Collection<T> self) {
        return grep(self, Closure.IDENTITY);
    }

    /**
     * Iterates over the collection returning each element that matches
     * using the IDENTITY Closure as a filter - effectively returning all elements which satisfy Groovy truth.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * def items = [1, 2, 0, false, true, '', 'foo', [], [4, 5], null]
     * assert items.grep() == [1, 2, true, 'foo', [4, 5]]
     * </pre>
     *
     * @param self a List
     * @return a List of elements satisfy Groovy truth
     * @see Closure#IDENTITY
     * @since 2.4.0
     */
    public static <T> List<T> grep(List<T> self) {
        return grep(self, Closure.IDENTITY);
    }

    /**
     * Iterates over the collection returning each element that matches
     * using the IDENTITY Closure as a filter - effectively returning all elements which satisfy Groovy truth.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * def items = [1, 2, 0, false, true, '', 'foo', [], [4, 5], null] as Set
     * assert items.grep() == [1, 2, true, 'foo', [4, 5]] as Set
     * </pre>
     *
     * @param self a Set
     * @return a Set of elements satisfy Groovy truth
     * @see Closure#IDENTITY
     * @since 2.4.0
     */
    public static <T> Set<T> grep(Set<T> self) {
        return grep(self, Closure.IDENTITY);
    }

    /**
     * Iterates over the array returning each element that matches
     * using the IDENTITY Closure as a filter - effectively returning all elements which satisfy Groovy truth.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * def items = [1, 2, 0, false, true, '', 'foo', [], [4, 5], null] as Object[]
     * assert items.grep() == [1, 2, true, 'foo', [4, 5]]
     * </pre>
     *
     * @param self an array
     * @return a collection of elements which satisfy Groovy truth
     * @see Closure#IDENTITY
     * @since 2.0
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> grep(T[] self) {
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
    public static <T> Number count(Iterator<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
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
     * @deprecated use count(Iterable, Closure)
     * @since 1.0
     */
    @Deprecated
    public static Number count(Collection self, Object value) {
        return count(self.iterator(), value);
    }

    /**
     * Counts the number of occurrences of the given value inside this Iterable.
     * Comparison is done using Groovy's == operator (using
     * <code>compareTo(value) == 0</code> or <code>equals(value)</code> ).
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">assert [2,4,2,1,3,5,2,4,3].count(4) == 2</pre>
     *
     * @param self  the Iterable within which we count the number of occurrences
     * @param value the value being searched for
     * @return the number of occurrences
     * @since 2.2.0
     */
    public static Number count(Iterable self, Object value) {
        return count(self.iterator(), value);
    }

    /**
     * @deprecated use count(Iterable, Closure)
     * @since 1.8.0
     */
    @Deprecated
    public static Number count(Collection self, Closure closure) {
        return count(self.iterator(), closure);
    }

    /**
     * Counts the number of occurrences which satisfy the given closure from inside this Iterable.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">assert [2,4,2,1,3,5,2,4,3].count{ it % 2 == 0 } == 5</pre>
     *
     * @param self  the Iterable within which we count the number of occurrences
     * @param closure a closure condition
     * @return the number of occurrences
     * @since 2.2.0
     */
    public static <T> Number count(Iterable<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        return count(self.iterator(), closure);
    }

    /**
     * Counts the number of occurrences which satisfy the given closure from inside this map.
     * If the closure takes one parameter then it will be passed the Map.Entry.
     * Otherwise, the closure should take two parameters and will be passed the key and value.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">assert [a:1, b:1, c:2, d:2].count{ k,v {@code ->} k == 'a' {@code ||} v == 2 } == 3</pre>
     *
     * @param self  the map within which we count the number of occurrences
     * @param closure a 1 or 2 arg Closure condition applying on the entries
     * @return the number of occurrences
     * @since 1.8.0
     */
    public static <K,V> Number count(Map<K,V> self, @ClosureParams(MapEntryOrKeyValue.class) Closure<?> closure) {
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
        return count((Iterable)Arrays.asList(self), value);
    }

    /**
     * Counts the number of occurrences which satisfy the given closure from inside this array.
     *
     * @param self  the array within which we count the number of occurrences
     * @param closure a closure condition
     * @return the number of occurrences
     * @since 1.8.0
     */
    public static <T> Number count(T[] self, @ClosureParams(FirstParam.Component.class) Closure closure) {
        return count((Iterable)Arrays.asList(self), closure);
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
     * @deprecated Use the Iterable version of toList instead
     * @see #toList(Iterable)
     * @since 1.0
     */
    @Deprecated
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
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">def x = [1,2,3] as HashSet
     * assert x.class == HashSet
     * assert x.toList() instanceof List</pre>
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
     * Collates this iterable into sub-lists of length <code>size</code>.
     * Example:
     * <pre class="groovyTestCase">def list = [ 1, 2, 3, 4, 5, 6, 7 ]
     * def coll = list.collate( 3 )
     * assert coll == [ [ 1, 2, 3 ], [ 4, 5, 6 ], [ 7 ] ]</pre>
     *
     * @param self          an Iterable
     * @param size          the length of each sub-list in the returned list
     * @return a List containing the data collated into sub-lists
     * @since 2.4.0
     */
    public static <T> List<List<T>> collate(Iterable<T> self, int size) {
        return collate(self, size, true);
    }

    /**
     * Collates an array.
     *
     * @param self          an array
     * @param size          the length of each sub-list in the returned list
     * @return a List containing the array values collated into sub-lists
     * @see #collate(Iterable, int)
     * @since 2.5.0
     */
    public static <T> List<List<T>> collate(T[] self, int size) {
        return collate((Iterable)Arrays.asList(self), size, true);
    }

    /**
     * @deprecated use the Iterable variant instead
     * @see #collate(Iterable, int)
     * @since 1.8.6
     */
    @Deprecated
    public static <T> List<List<T>> collate( List<T> self, int size ) {
        return collate((Iterable<T>) self, size) ;
    }

    /**
     * Collates this iterable into sub-lists of length <code>size</code> stepping through the code <code>step</code>
     * elements for each subList.
     * Example:
     * <pre class="groovyTestCase">def list = [ 1, 2, 3, 4 ]
     * def coll = list.collate( 3, 1 )
     * assert coll == [ [ 1, 2, 3 ], [ 2, 3, 4 ], [ 3, 4 ], [ 4 ] ]</pre>
     *
     * @param self          an Iterable
     * @param size          the length of each sub-list in the returned list
     * @param step          the number of elements to step through for each sub-list
     * @return a List containing the data collated into sub-lists
     * @since 2.4.0
     */
    public static <T> List<List<T>> collate(Iterable<T> self, int size, int step) {
        return collate(self, size, step, true);
    }

    /**
     * Collates an array into sub-lists.
     *
     * @param self          an array
     * @param size          the length of each sub-list in the returned list
     * @param step          the number of elements to step through for each sub-list
     * @return a List containing the array elements collated into sub-lists
     * @see #collate(Iterable, int, int)
     * @since 2.5.0
     */
    public static <T> List<List<T>> collate(T[] self, int size, int step) {
        return collate((Iterable)Arrays.asList(self), size, step, true);
    }

    /**
     * @deprecated use the Iterable variant instead
     * @see #collate(Iterable, int, int)
     * @since 1.8.6
     */
    @Deprecated
    public static <T> List<List<T>> collate( List<T> self, int size, int step ) {
        return collate((Iterable<T>) self, size, step) ;
    }

    /**
     * Collates this iterable into sub-lists of length <code>size</code>. Any remaining elements in
     * the iterable after the subdivision will be dropped if <code>keepRemainder</code> is false.
     * Example:
     * <pre class="groovyTestCase">def list = [ 1, 2, 3, 4, 5, 6, 7 ]
     * def coll = list.collate( 3, false )
     * assert coll == [ [ 1, 2, 3 ], [ 4, 5, 6 ] ]</pre>
     *
     * @param self          an Iterable
     * @param size          the length of each sub-list in the returned list
     * @param keepRemainder if true, any remaining elements are returned as sub-lists.  Otherwise they are discarded
     * @return a List containing the data collated into sub-lists
     * @since 2.4.0
     */
    public static <T> List<List<T>> collate(Iterable<T> self, int size, boolean keepRemainder) {
        return collate(self, size, size, keepRemainder);
    }

    /**
     * Collates this array into sub-lists.
     *
     * @param self          an array
     * @param size          the length of each sub-list in the returned list
     * @param keepRemainder if true, any remaining elements are returned as sub-lists.  Otherwise they are discarded
     * @return a List containing the array elements collated into sub-lists
     * @see #collate(Iterable, int, boolean)
     * @since 2.5.0
     */
    public static <T> List<List<T>> collate(T[] self, int size, boolean keepRemainder) {
        return collate((Iterable)Arrays.asList(self), size, size, keepRemainder);
    }

    /**
     * @deprecated use the Iterable variant instead
     * @see #collate(Iterable, int, boolean)
     * @since 1.8.6
     */
    @Deprecated
    public static <T> List<List<T>> collate( List<T> self, int size, boolean keepRemainder ) {
        return collate((Iterable<T>) self, size, keepRemainder) ;
    }

    /**
     * Collates this iterable into sub-lists of length <code>size</code> stepping through the code <code>step</code>
     * elements for each sub-list.  Any remaining elements in the iterable after the subdivision will be dropped if
     * <code>keepRemainder</code> is false.
     * Example:
     * <pre class="groovyTestCase">
     * def list = [ 1, 2, 3, 4 ]
     * assert list.collate( 2, 2, true  ) == [ [ 1, 2 ], [ 3, 4 ] ]
     * assert list.collate( 3, 1, true  ) == [ [ 1, 2, 3 ], [ 2, 3, 4 ], [ 3, 4 ], [ 4 ] ]
     * assert list.collate( 3, 1, false ) == [ [ 1, 2, 3 ], [ 2, 3, 4 ] ]
     * </pre>
     *
     * @param self          an Iterable
     * @param size          the length of each sub-list in the returned list
     * @param step          the number of elements to step through for each sub-list
     * @param keepRemainder if true, any remaining elements are returned as sub-lists.  Otherwise they are discarded
     * @return a List containing the data collated into sub-lists
     * @throws IllegalArgumentException if the step is zero.
     * @since 2.4.0
     */
    public static <T> List<List<T>> collate(Iterable<T> self, int size, int step, boolean keepRemainder) {
        List<T> selfList = asList(self);
        List<List<T>> answer = new ArrayList<List<T>>();
        if (size <= 0) {
            answer.add(selfList);
        } else {
            if (step == 0) throw new IllegalArgumentException("step cannot be zero");
            for (int pos = 0; pos < selfList.size() && pos > -1; pos += step) {
                if (!keepRemainder && pos > selfList.size() - size) {
                    break ;
                }
                List<T> element = new ArrayList<T>() ;
                for (int offs = pos; offs < pos + size && offs < selfList.size(); offs++) {
                    element.add(selfList.get(offs));
                }
                answer.add( element ) ;
            }
        }
        return answer ;
    }

    /**
     * Collates this array into into sub-lists.
     *
     * @param self          an array
     * @param size          the length of each sub-list in the returned list
     * @param step          the number of elements to step through for each sub-list
     * @param keepRemainder if true, any remaining elements are returned as sub-lists.  Otherwise they are discarded
     * @return a List containing the array elements collated into sub-lists
     * @since 2.5.0
     */
    public static <T> List<List<T>> collate(T[] self, int size, int step, boolean keepRemainder) {
        return collate((Iterable)Arrays.asList(self), size, step, keepRemainder);
    }

    /**
     * @deprecated use the Iterable variant instead
     * @see #collate(Iterable, int, int, boolean)
     * @since 1.8.6
     */
    @Deprecated
    public static <T> List<List<T>> collate( List<T> self, int size, int step, boolean keepRemainder ) {
        return collate((Iterable<T>) self, size, step, keepRemainder);
    }

    /**
     * Iterates through this aggregate Object transforming each item into a new value using Closure.IDENTITY
     * as a transformer, basically returning a list of items copied from the original object.
     * <pre class="groovyTestCase">assert [1,2,3] == [1,2,3].iterator().collect()</pre>
     *
     * @param self an aggregate Object with an Iterator returning its items
     * @return a Collection of the transformed values
     * @see Closure#IDENTITY
     * @since 1.8.5
     */
    public static Collection collect(Object self) {
        return collect(self, Closure.IDENTITY);
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
        return collect(InvokerHelper.asIterator(self), collector, transform);
    }

    /**
     * Iterates through this Array transforming each item into a new value using the
     * <code>transform</code> closure, returning a list of transformed values.
     *
     * @param self      an Array
     * @param transform the closure used to transform each item of the Array
     * @return a List of the transformed values
     * @since 2.5.0
     */
    public static <S,T> List<T> collect(S[] self, @ClosureParams(FirstParam.Component.class) Closure<T> transform) {
        return collect(new ArrayIterator<S>(self), transform);
    }

    /**
     * Iterates through this Array transforming each item into a new value using the <code>transform</code> closure
     * and adding it to the supplied <code>collector</code>.
     * <pre class="groovyTestCase">
     * Integer[] nums = [1,2,3]
     * List<Integer> answer = []
     * nums.collect(answer) { it * 2 }
     * assert [2,4,6] == answer
     * </pre>
     *
     * @param self      an Array
     * @param collector the Collection to which the transformed values are added
     * @param transform the closure used to transform each item
     * @return the collector with all transformed values added to it
     * @since 2.5.0
     */
    public static <S,T> Collection<T> collect(S[] self, Collection<T> collector, @ClosureParams(FirstParam.Component.class) Closure<? extends T> transform) {
        return collect(new ArrayIterator<S>(self), collector, transform);
    }

    /**
     * Iterates through this Iterator transforming each item into a new value using the
     * <code>transform</code> closure, returning a list of transformed values.
     *
     * @param self      an Iterator
     * @param transform the closure used to transform each item
     * @return a List of the transformed values
     * @since 2.5.0
     */
    public static <S,T> List<T> collect(Iterator<S> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<T> transform) {
        return (List<T>) collect(self, new ArrayList<T>(), transform);
    }

    /**
     * Iterates through this Iterator transforming each item into a new value using the <code>transform</code> closure
     * and adding it to the supplied <code>collector</code>.
     *
     * @param self      an Iterator
     * @param collector the Collection to which the transformed values are added
     * @param transform the closure used to transform each item
     * @return the collector with all transformed values added to it
     * @since 2.5.0
     */
    public static <S,T> Collection<T> collect(Iterator<S> self, Collection<T> collector, @ClosureParams(FirstParam.FirstGenericType.class) Closure<? extends T> transform) {
        while (self.hasNext()) {
            collector.add(transform.call(self.next()));
        }
        return collector;
    }

    /**
     * Iterates through this collection transforming each entry into a new value using Closure.IDENTITY
     * as a transformer, basically returning a list of items copied from the original collection.
     * <pre class="groovyTestCase">assert [1,2,3] == [1,2,3].collect()</pre>
     *
     * @param self a collection
     * @return a List of the transformed values
     * @see Closure#IDENTITY
     * @since 1.8.5
     * @deprecated use the Iterable version instead
     */
    @Deprecated
    public static <T> List<T> collect(Collection<T> self) {
        return collect((Iterable<T>) self);
    }

    /**
     * Iterates through this collection transforming each entry into a new value using the <code>transform</code> closure
     * returning a list of transformed values.
     *
     * @param self      a collection
     * @param transform the closure used to transform each item of the collection
     * @return a List of the transformed values
     * @deprecated use the Iterable version instead
     * @since 1.0
     */
    @Deprecated
    public static <S,T> List<T> collect(Collection<S> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<T> transform) {
        return (List<T>) collect(self, new ArrayList<T>(self.size()), transform);
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
     * @deprecated use the Iterable version instead
     * @since 1.0
     */
    @Deprecated
    public static <S,T> Collection<T> collect(Collection<S> self, Collection<T> collector, @ClosureParams(FirstParam.FirstGenericType.class) Closure<? extends T> transform) {
        for (S item : self) {
            collector.add(transform.call(item));
            if (transform.getDirective() == Closure.DONE) {
                break;
            }
        }
        return collector;
    }

    /**
     * Iterates through this collection transforming each entry into a new value using Closure.IDENTITY
     * as a transformer, basically returning a list of items copied from the original collection.
     * <pre class="groovyTestCase">assert [1,2,3] == [1,2,3].collect()</pre>
     *
     * @param self an Iterable
     * @return a List of the transformed values
     * @see Closure#IDENTITY
     * @since 2.5.0
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> collect(Iterable<T> self) {
        return collect(self, (Closure<T>) Closure.IDENTITY);
    }

    /**
     * Iterates through this Iterable transforming each entry into a new value using the <code>transform</code> closure
     * returning a list of transformed values.
     * <pre class="groovyTestCase">assert [2,4,6] == [1,2,3].collect { it * 2 }</pre>
     *
     * @param self      an Iterable
     * @param transform the closure used to transform each item of the collection
     * @return a List of the transformed values
     * @since 2.5.0
     */
    public static <S,T> List<T> collect(Iterable<S> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<T> transform) {
        return collect(self.iterator(), transform);
    }

    /**
     * Iterates through this collection transforming each value into a new value using the <code>transform</code> closure
     * and adding it to the supplied <code>collector</code>.
     * <pre class="groovyTestCase">assert [1,2,3] as HashSet == [2,4,5,6].collect(new HashSet()) { (int)(it / 2) }</pre>
     *
     * @param self      an Iterable
     * @param collector the Collection to which the transformed values are added
     * @param transform the closure used to transform each item
     * @return the collector with all transformed values added to it
     * @since 2.5.0
     */
    public static <S,T> Collection<T> collect(Iterable<S> self, Collection<T> collector, @ClosureParams(FirstParam.FirstGenericType.class) Closure<? extends T> transform) {
        for (S item : self) {
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
    @Deprecated
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
        return (List) collectNested((Iterable) self, new ArrayList(self.size()), transform);
    }

    /**
     * Recursively iterates through this Iterable transforming each non-Collection value
     * into a new value using the closure as a transformer. Returns a potentially nested
     * list of transformed values.
     * <pre class="groovyTestCase">
     * assert [2,[4,6],[8],[]] == [1,[2,3],[4],[]].collectNested { it * 2 }
     * </pre>
     *
     * @param self      an Iterable
     * @param transform the closure used to transform each item of the Iterable
     * @return the resultant list
     * @since 2.2.0
     */
    public static List collectNested(Iterable self, Closure transform) {
        return (List) collectNested(self, new ArrayList(), transform);
    }

    /**
     * Deprecated alias for collectNested
     *
     * @deprecated Use collectNested instead
     * @see #collectNested(Iterable, Collection, Closure)
     */
    @Deprecated
    public static Collection collectAll(Collection self, Collection collector, Closure transform) {
        return collectNested((Iterable)self, collector, transform);
    }

    /**
     * @deprecated Use the Iterable version of collectNested instead
     * @see #collectNested(Iterable, Collection, Closure)
     * @since 1.8.1
     */
    @Deprecated
    public static Collection collectNested(Collection self, Collection collector, Closure transform) {
        return collectNested((Iterable)self, collector, transform);
    }

    /**
     * Recursively iterates through this Iterable transforming each non-Collection value
     * into a new value using the <code>transform</code> closure. Returns a potentially nested
     * collection of transformed values.
     * <pre class="groovyTestCase">
     * def x = [1,[2,3],[4],[]].collectNested(new Vector()) { it * 2 }
     * assert x == [2,[4,6],[8],[]]
     * assert x instanceof Vector
     * </pre>
     *
     * @param self      an Iterable
     * @param collector an initial Collection to which the transformed values are added
     * @param transform the closure used to transform each element of the Iterable
     * @return the collector with all transformed values added to it
     * @since 2.2.0
     */
    public static Collection collectNested(Iterable self, Collection collector, Closure transform) {
        for (Object item : self) {
            if (item instanceof Collection) {
                Collection c = (Collection) item;
                collector.add(collectNested((Iterable)c, createSimilarCollection(collector, c.size()), transform));
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
     * @deprecated Use the Iterable version of collectMany instead
     * @see #collectMany(Iterable, Closure)
     * @since 1.8.1
     */
    @Deprecated
    public static <T,E> List<T> collectMany(Collection<E> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<? extends Collection<? extends T>> projection) {
        return collectMany((Iterable)self, projection);
    }

    /**
     * @deprecated Use the Iterable version of collectMany instead
     * @see #collectMany(Iterable, Collection, Closure)
     * @since 1.8.5
     */
    @Deprecated
    public static <T,E> Collection<T> collectMany(Collection<E> self, Collection<T> collector, @ClosureParams(FirstParam.FirstGenericType.class) Closure<? extends Collection<? extends T>> projection) {
        return collectMany((Iterable)self, collector, projection);
    }

    /**
     * Projects each item from a source Iterable to a collection and concatenates (flattens) the resulting collections into a single list.
     * <p>
     * <pre class="groovyTestCase">
     * def nums = 1..10
     * def squaresAndCubesOfEvens = nums.collectMany{ it % 2 ? [] : [it**2, it**3] }
     * assert squaresAndCubesOfEvens == [4, 8, 16, 64, 36, 216, 64, 512, 100, 1000]
     *
     * def animals = ['CAT', 'DOG', 'ELEPHANT'] as Set
     * def smallAnimals = animals.collectMany{ it.size() {@code >} 3 ? [] : [it.toLowerCase()] }
     * assert smallAnimals == ['cat', 'dog']
     *
     * def orig = nums as Set
     * def origPlusIncrements = orig.collectMany{ [it, it+1] }
     * assert origPlusIncrements.size() == orig.size() * 2
     * assert origPlusIncrements.unique().size() == orig.size() + 1
     * </pre>
     *
     * @param self       an Iterable
     * @param projection a projecting Closure returning a collection of items
     * @return a list created from the projected collections concatenated (flattened) together
     * @see #sum(java.util.Collection, groovy.lang.Closure)
     * @since 2.2.0
     */
    public static <T,E> List<T> collectMany(Iterable<E> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<? extends Collection<? extends T>> projection) {
        return (List<T>) collectMany(self, new ArrayList<T>(), projection);
    }

    /**
     * Projects each item from a source collection to a result collection and concatenates (flattens) the resulting
     * collections adding them into the <code>collector</code>.
     * <p>
     * <pre class="groovyTestCase">
     * def animals = ['CAT', 'DOG', 'ELEPHANT'] as Set
     * def smallAnimals = animals.collectMany(['ant', 'bee']){ it.size() {@code >} 3 ? [] : [it.toLowerCase()] }
     * assert smallAnimals == ['ant', 'bee', 'cat', 'dog']
     *
     * def nums = 1..5
     * def origPlusIncrements = nums.collectMany([] as Set){ [it, it+1] }
     * assert origPlusIncrements.size() == nums.size() + 1
     * </pre>
     *
     * @param self       an Iterable
     * @param collector  an initial collection to add the projected items to
     * @param projection a projecting Closure returning a collection of items
     * @return the collector with the projected collections concatenated (flattened) into it
     * @since 2.2.0
     */
    public static <T,E> Collection<T> collectMany(Iterable<E> self, Collection<T> collector, @ClosureParams(FirstParam.FirstGenericType.class) Closure<? extends Collection<? extends T>> projection) {
        for (E next : self) {
            collector.addAll(projection.call(next));
        }
        return collector;
    }

    /**
     * Projects each item from a source map to a result collection and concatenates (flattens) the resulting
     * collections adding them into the <code>collector</code>.
     * <p>
     * <pre class="groovyTestCase">
     * def map = [bread:3, milk:5, butter:2]
     * def result = map.collectMany(['x']){ k, v {@code ->} k.startsWith('b') ? k.toList() : [] }
     * assert result == ['x', 'b', 'r', 'e', 'a', 'd', 'b', 'u', 't', 't', 'e', 'r']
     * </pre>
     *
     * @param self       a map
     * @param collector  an initial collection to add the projected items to
     * @param projection a projecting Closure returning a collection of items
     * @return the collector with the projected collections concatenated (flattened) to it
     * @since 1.8.8
     */
    public static <T,K,V> Collection<T> collectMany(Map<K, V> self, Collection<T> collector, @ClosureParams(MapEntryOrKeyValue.class) Closure<? extends Collection<? extends T>> projection) {
        for (Map.Entry<K, V> entry : self.entrySet()) {
            collector.addAll(callClosureForMapEntry(projection, entry));
        }
        return collector;
    }

    /**
     * Projects each item from a source map to a result collection and concatenates (flattens) the resulting
     * collections adding them into a collection.
     * <p>
     * <pre class="groovyTestCase">
     * def map = [bread:3, milk:5, butter:2]
     * def result = map.collectMany{ k, v {@code ->} k.startsWith('b') ? k.toList() : [] }
     * assert result == ['b', 'r', 'e', 'a', 'd', 'b', 'u', 't', 't', 'e', 'r']
     * </pre>
     *
     * @param self       a map
     * @param projection a projecting Closure returning a collection of items
     * @return the collector with the projected collections concatenated (flattened) to it
     * @since 1.8.8
     */
    public static <T,K,V> Collection<T> collectMany(Map<K, V> self, @ClosureParams(MapEntryOrKeyValue.class) Closure<? extends Collection<? extends T>> projection) {
        return collectMany(self, new ArrayList<T>(), projection);
    }

    /**
     * Projects each item from a source array to a collection and concatenates (flattens) the resulting collections into a single list.
     * <p>
     * <pre class="groovyTestCase">
     * def nums = [1, 2, 3, 4, 5, 6] as Object[]
     * def squaresAndCubesOfEvens = nums.collectMany{ it % 2 ? [] : [it**2, it**3] }
     * assert squaresAndCubesOfEvens == [4, 8, 16, 64, 36, 216]
     * </pre>
     *
     * @param self       an array
     * @param projection a projecting Closure returning a collection of items
     * @return a list created from the projected collections concatenated (flattened) together
     * @see #sum(Object[], groovy.lang.Closure)
     * @since 1.8.1
     */
    @SuppressWarnings("unchecked")
    public static <T,E> List<T> collectMany(E[] self, @ClosureParams(FirstParam.Component.class) Closure<? extends Collection<? extends T>> projection) {
        return collectMany((Iterable<E>)toList(self), projection);
    }

    /**
     * Projects each item from a source iterator to a collection and concatenates (flattens) the resulting collections into a single list.
     * <p>
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
    @SuppressWarnings("unchecked")
    public static <T,E> List<T> collectMany(Iterator<E> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<? extends Collection<? extends T>> projection) {
        return collectMany((Iterable)toList(self), projection);
    }

    /**
     * Iterates through this Map transforming each map entry into a new value using the <code>transform</code> closure
     * returning the <code>collector</code> with all transformed values added to it.
     * <pre class="groovyTestCase">assert [a:1, b:2].collect( [] as HashSet ) { key, value {@code ->} key*value } == ["a", "bb"] as Set
     * assert [3:20, 2:30].collect( [] as HashSet ) { entry {@code ->} entry.key * entry.value } == [60] as Set</pre>
     *
     * @param self      a Map
     * @param collector the Collection to which transformed values are added
     * @param transform the transformation closure which can take one (Map.Entry) or two (key, value) parameters
     * @return the collector with all transformed values added to it
     * @since 1.0
     */
    public static <T,K,V> Collection<T> collect(Map<K, V> self, Collection<T> collector, @ClosureParams(MapEntryOrKeyValue.class) Closure<? extends T> transform) {
        for (Map.Entry<K, V> entry : self.entrySet()) {
            collector.add(callClosureForMapEntry(transform, entry));
        }
        return collector;
    }

    /**
     * Iterates through this Map transforming each map entry into a new value using the <code>transform</code> closure
     * returning a list of transformed values.
     * <pre class="groovyTestCase">assert [a:1, b:2].collect { key, value {@code ->} key*value } == ["a", "bb"]
     * assert [3:20, 2:30].collect { entry {@code ->} entry.key * entry.value } == [60, 60]</pre>
     *
     * @param self    a Map
     * @param transform the transformation closure which can take one (Map.Entry) or two (key, value) parameters
     * @return the resultant list of transformed values
     * @since 1.0
     */
    public static <T,K,V> List<T> collect(Map<K,V> self, @ClosureParams(MapEntryOrKeyValue.class) Closure<T> transform) {
        return (List<T>) collect(self, new ArrayList<T>(self.size()), transform);
    }

    /**
     * Iterates through this Map transforming each map entry using the <code>transform</code> closure
     * returning a map of the transformed entries.
     * <pre class="groovyTestCase">
     * assert [a:1, b:2].collectEntries( [:] ) { k, v {@code ->} [v, k] } == [1:'a', 2:'b']
     * assert [a:1, b:2].collectEntries( [30:'C'] ) { key, value {@code ->}
     *     [(value*10): key.toUpperCase()] } == [10:'A', 20:'B', 30:'C']
     * </pre>
     * Note: When using the list-style of result, the behavior is '<code>def (key, value) = listResultFromClosure</code>'.
     * While we strongly discourage using a list of size other than 2, Groovy's normal semantics apply in this case;
     * throwing away elements after the second one and using null for the key or value for the case of a shortened list.
     * If your collector Map doesn't support null keys or values, you might get a runtime error, e.g. NullPointerException or IllegalArgumentException.
     *
     * @param self      a Map
     * @param collector the Map into which the transformed entries are put
     * @param transform the closure used for transforming, which can take one (Map.Entry) or two (key, value) parameters and
     *                  should return a Map.Entry, a Map or a two-element list containing the resulting key and value
     * @return the collector with all transformed values added to it
     * @see #collect(Map, Collection, Closure)
     * @since 1.7.9
     */
    public static <K, V, S, T> Map<K, V> collectEntries(Map<S, T> self, Map<K, V> collector, @ClosureParams(MapEntryOrKeyValue.class) Closure<?> transform) {
        for (Map.Entry<S, T> entry : self.entrySet()) {
            addEntry(collector, callClosureForMapEntry(transform, entry));
        }
        return collector;
    }

    /**
     * Iterates through this Map transforming each entry using the <code>transform</code> closure
     * and returning a map of the transformed entries.
     * <pre class="groovyTestCase">
     * assert [a:1, b:2].collectEntries { key, value {@code ->} [value, key] } == [1:'a', 2:'b']
     * assert [a:1, b:2].collectEntries { key, value {@code ->}
     *     [(value*10): key.toUpperCase()] } == [10:'A', 20:'B']
     * </pre>
     * Note: When using the list-style of result, the behavior is '<code>def (key, value) = listResultFromClosure</code>'.
     * While we strongly discourage using a list of size other than 2, Groovy's normal semantics apply in this case;
     * throwing away elements after the second one and using null for the key or value for the case of a shortened list.
     * If your Map doesn't support null keys or values, you might get a runtime error, e.g. NullPointerException or IllegalArgumentException.
     *
     * @param self      a Map
     * @param transform the closure used for transforming, which can take one (Map.Entry) or two (key, value) parameters and
     *                  should return a Map.Entry, a Map or a two-element list containing the resulting key and value
     * @return a Map of the transformed entries
     * @see #collect(Map, Collection, Closure)
     * @since 1.7.9
     */
    public static <K,V> Map<?, ?> collectEntries(Map<K, V> self, @ClosureParams(MapEntryOrKeyValue.class) Closure<?> transform) {
        return collectEntries(self, createSimilarMap(self), transform);
    }

    /**
     * @deprecated Use the Iterable version of collectEntries instead
     * @see #collectEntries(Iterable, Closure)
     * @since 1.7.9
     */
    @Deprecated
    public static <K, V> Map<K, V> collectEntries(Collection<?> self, Closure<?> transform) {
        return collectEntries((Iterable)self, new LinkedHashMap<K, V>(), transform);
    }

    /**
     * A variant of collectEntries for Iterators.
     *
     * @param self      an Iterator
     * @param transform the closure used for transforming, which has an item from self as the parameter and
     *                  should return a Map.Entry, a Map or a two-element list containing the resulting key and value
     * @return a Map of the transformed entries
     * @see #collectEntries(Iterable, Closure)
     * @since 1.8.7
     */
    public static <K, V, E> Map<K, V> collectEntries(Iterator<E> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<?> transform) {
        return collectEntries(self, new LinkedHashMap<K, V>(), transform);
    }

    /**
     * Iterates through this Iterable transforming each item using the <code>transform</code> closure
     * and returning a map of the resulting transformed entries.
     * <pre class="groovyTestCase">
     * def letters = "abc"
     * // collect letters with index using list style
     * assert (0..2).collectEntries { index {@code ->} [index, letters[index]] } == [0:'a', 1:'b', 2:'c']
     * // collect letters with index using map style
     * assert (0..2).collectEntries { index {@code ->} [(index): letters[index]] } == [0:'a', 1:'b', 2:'c']
     * </pre>
     * Note: When using the list-style of result, the behavior is '<code>def (key, value) = listResultFromClosure</code>'.
     * While we strongly discourage using a list of size other than 2, Groovy's normal semantics apply in this case;
     * throwing away elements after the second one and using null for the key or value for the case of a shortened list.
     *
     * @param self      an Iterable
     * @param transform the closure used for transforming, which has an item from self as the parameter and
     *                  should return a Map.Entry, a Map or a two-element list containing the resulting key and value
     * @return a Map of the transformed entries
     * @see #collectEntries(Iterator, Closure)
     * @since 1.8.7
     */
    public static <K,V,E> Map<K, V> collectEntries(Iterable<E> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<?> transform) {
        return collectEntries(self.iterator(), transform);
    }

    /**
     * @deprecated Use the Iterable version of collectEntries instead
     * @see #collectEntries(Iterable)
     * @since 1.8.5
     */
    @Deprecated
    public static <K, V> Map<K, V> collectEntries(Collection<?> self) {
        return collectEntries((Iterable)self, new LinkedHashMap<K, V>(), Closure.IDENTITY);
    }

    /**
     * A variant of collectEntries for Iterators using the identity closure as the transform.
     *
     * @param self an Iterator
     * @return a Map of the transformed entries
     * @see #collectEntries(Iterable)
     * @since 1.8.7
     */
    public static <K, V> Map<K, V> collectEntries(Iterator<?> self) {
        return collectEntries(self, Closure.IDENTITY);
    }

    /**
     * A variant of collectEntries for Iterable objects using the identity closure as the transform.
     * The source Iterable should contain a list of <code>[key, value]</code> tuples or <code>Map.Entry</code> objects.
     * <pre class="groovyTestCase">
     * def nums = [1, 10, 100, 1000]
     * def tuples = nums.collect{ [it, it.toString().size()] }
     * assert tuples == [[1, 1], [10, 2], [100, 3], [1000, 4]]
     * def map = tuples.collectEntries()
     * assert map == [1:1, 10:2, 100:3, 1000:4]
     * </pre>
     *
     * @param self an Iterable
     * @return a Map of the transformed entries
     * @see #collectEntries(Iterator)
     * @since 1.8.7
     */
    public static <K, V> Map<K, V> collectEntries(Iterable<?> self) {
        return collectEntries(self.iterator());
    }

    /**
     * @deprecated Use the Iterable version of collectEntries instead
     * @see #collectEntries(Iterable, Map, Closure)
     * @since 1.7.9
     */
    @Deprecated
    public static <K, V> Map<K, V> collectEntries(Collection<?> self, Map<K, V> collector, Closure<?> transform) {
        return collectEntries((Iterable<?>)self, collector, transform);
    }

    /**
     * A variant of collectEntries for Iterators using a supplied map as the destination of transformed entries.
     *
     * @param self      an Iterator
     * @param collector the Map into which the transformed entries are put
     * @param transform the closure used for transforming, which has an item from self as the parameter and
     *                  should return a Map.Entry, a Map or a two-element list containing the resulting key and value
     * @return the collector with all transformed values added to it
     * @since 1.8.7
     */
    public static <K, V, E> Map<K, V> collectEntries(Iterator<E> self, Map<K, V> collector, @ClosureParams(FirstParam.FirstGenericType.class) Closure<?> transform) {
        while (self.hasNext()) {
            E next = self.next();
            addEntry(collector, transform.call(next));
        }
        return collector;
    }

    /**
     * Iterates through this Iterable transforming each item using the closure
     * as a transformer into a map entry, returning the supplied map with all of the transformed entries added to it.
     * <pre class="groovyTestCase">
     * def letters = "abc"
     * // collect letters with index
     * assert (0..2).collectEntries( [:] ) { index {@code ->} [index, letters[index]] } == [0:'a', 1:'b', 2:'c']
     * assert (0..2).collectEntries( [4:'d'] ) { index {@code ->}
     *     [(index+1): letters[index]] } == [1:'a', 2:'b', 3:'c', 4:'d']
     * </pre>
     * Note: When using the list-style of result, the behavior is '<code>def (key, value) = listResultFromClosure</code>'.
     * While we strongly discourage using a list of size other than 2, Groovy's normal semantics apply in this case;
     * throwing away elements after the second one and using null for the key or value for the case of a shortened list.
     * If your collector Map doesn't support null keys or values, you might get a runtime error, e.g. NullPointerException or IllegalArgumentException.
     *
     * @param self      an Iterable
     * @param collector the Map into which the transformed entries are put
     * @param transform the closure used for transforming, which has an item from self as the parameter and
     *                  should return a Map.Entry, a Map or a two-element list containing the resulting key and value
     * @return the collector with all transformed values added to it
     * @see #collectEntries(Iterator, Map, Closure)
     * @since 1.8.7
     */
    public static <K, V, E> Map<K, V> collectEntries(Iterable<E> self, Map<K, V> collector, @ClosureParams(FirstParam.FirstGenericType.class) Closure<?> transform) {
        return collectEntries(self.iterator(), collector, transform);
    }

    /**
     * @deprecated Use the Iterable version of collectEntries instead
     * @see #collectEntries(Iterable, Map)
     * @since 1.8.5
     */
    @Deprecated
    public static <K, V> Map<K, V> collectEntries(Collection<?> self, Map<K, V> collector) {
        return collectEntries((Iterable<?>)self, collector, Closure.IDENTITY);
    }

    /**
     * A variant of collectEntries for Iterators using the identity closure as the
     * transform and a supplied map as the destination of transformed entries.
     *
     * @param self      an Iterator
     * @param collector the Map into which the transformed entries are put
     * @return the collector with all transformed values added to it
     * @see #collectEntries(Iterable, Map)
     * @since 1.8.7
     */
    public static <K, V> Map<K, V> collectEntries(Iterator<?> self, Map<K, V> collector) {
        return collectEntries(self, collector, Closure.IDENTITY);
    }

    /**
     * A variant of collectEntries for Iterables using the identity closure as the
     * transform and a supplied map as the destination of transformed entries.
     *
     * @param self      an Iterable
     * @param collector the Map into which the transformed entries are put
     * @return the collector with all transformed values added to it
     * @see #collectEntries(Iterator, Map)
     * @since 1.8.7
     */
    public static <K, V> Map<K, V> collectEntries(Iterable<?> self, Map<K, V> collector) {
        return collectEntries(self.iterator(), collector);
    }

    /**
     * Iterates through this array transforming each item using the <code>transform</code> closure
     * and returning a map of the resulting transformed entries.
     * <pre class="groovyTestCase">
     * def letters = "abc"
     * def nums = [0, 1, 2] as Integer[]
     * // collect letters with index
     * assert nums.collectEntries( [:] ) { index {@code ->} [index, letters[index]] } == [0:'a', 1:'b', 2:'c']
     * assert nums.collectEntries( [4:'d'] ) { index {@code ->}
     *     [(index+1): letters[index]] } == [1:'a', 2:'b', 3:'c', 4:'d']
     * </pre>
     * Note: When using the list-style of result, the behavior is '<code>def (key, value) = listResultFromClosure</code>'.
     * While we strongly discourage using a list of size other than 2, Groovy's normal semantics apply in this case;
     * throwing away elements after the second one and using null for the key or value for the case of a shortened list.
     * If your collector Map doesn't support null keys or values, you might get a runtime error, e.g. NullPointerException or IllegalArgumentException.
     *
     * @param self      an array
     * @param collector the Map into which the transformed entries are put
     * @param transform the closure used for transforming, which has an item from self as the parameter and
     *                  should return a Map.Entry, a Map or a two-element list containing the resulting key and value
     * @return the collector with all transformed values added to it
     * @see #collect(Map, Collection, Closure)
     * @since 1.7.9
     */
    @SuppressWarnings("unchecked")
    public static <K, V, E> Map<K, V> collectEntries(E[] self, Map<K, V> collector, @ClosureParams(FirstParam.Component.class) Closure<?> transform) {
        return collectEntries((Iterable)toList(self), collector, transform);
    }

    /**
     * A variant of collectEntries using the identity closure as the transform.
     *
     * @param self      an array
     * @param collector the Map into which the transformed entries are put
     * @return the collector with all transformed values added to it
     * @see #collectEntries(Object[], Map, Closure)
     * @since 1.8.5
     */
    public static <K, V, E> Map<K, V> collectEntries(E[] self, Map<K, V> collector) {
        return collectEntries(self, collector, Closure.IDENTITY);
    }

    /**
     * Iterates through this array transforming each item using the <code>transform</code> closure
     * and returning a map of the resulting transformed entries.
     * <pre class="groovyTestCase">
     * def letters = "abc"
     * def nums = [0, 1, 2] as Integer[]
     * // collect letters with index using list style
     * assert nums.collectEntries { index {@code ->} [index, letters[index]] } == [0:'a', 1:'b', 2:'c']
     * // collect letters with index using map style
     * assert nums.collectEntries { index {@code ->} [(index): letters[index]] } == [0:'a', 1:'b', 2:'c']
     * </pre>
     * Note: When using the list-style of result, the behavior is '<code>def (key, value) = listResultFromClosure</code>'.
     * While we strongly discourage using a list of size other than 2, Groovy's normal semantics apply in this case;
     * throwing away elements after the second one and using null for the key or value for the case of a shortened list.
     *
     * @param self      a Collection
     * @param transform the closure used for transforming, which has an item from self as the parameter and
     *                  should return a Map.Entry, a Map or a two-element list containing the resulting key and value
     * @return a Map of the transformed entries
     * @see #collectEntries(Iterable, Map, Closure)
     * @since 1.7.9
     */
    public static <K, V, E> Map<K, V> collectEntries(E[] self, @ClosureParams(FirstParam.Component.class) Closure<?> transform) {
        return collectEntries((Iterable)toList(self), new LinkedHashMap<K, V>(), transform);
    }

    /**
     * A variant of collectEntries using the identity closure as the transform.
     *
     * @param self      an array
     * @return the collector with all transformed values added to it
     * @see #collectEntries(Object[], Closure)
     * @since 1.8.5
     */
    public static <K, V, E> Map<K, V> collectEntries(E[] self) {
        return collectEntries(self, Closure.IDENTITY);
    }

    private static <K, V> void addEntry(Map<K, V> result, Object newEntry) {
        if (newEntry instanceof Map) {
            leftShift(result, (Map)newEntry);
        } else if (newEntry instanceof List) {
            List list = (List) newEntry;
            // def (key, value) == list
            Object key = list.isEmpty() ? null : list.get(0);
            Object value = list.size() <= 1 ? null : list.get(1);
            leftShift(result, new MapEntry(key, value));
        } else if (newEntry.getClass().isArray()) {
            Object[] array = (Object[]) newEntry;
            // def (key, value) == array.toList()
            Object key = array.length == 0 ? null : array[0];
            Object value = array.length <= 1 ? null : array[1];
            leftShift(result, new MapEntry(key, value));
        } else {
            // TODO: enforce stricter behavior?
            // given Map.Entry is an interface, we get a proxy which gives us lots
            // of flexibility but sometimes the error messages might be unexpected
            leftShift(result, asType(newEntry, Map.Entry.class));
        }
    }

    /**
     * Finds the first value matching the closure condition.
     *
     * <pre class="groovyTestCase">
     * def numbers = [1, 2, 3]
     * def result = numbers.find { it {@code >} 1}
     * assert result == 2
     * </pre>
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
     * Finds the first item matching the IDENTITY Closure (i.e.&#160;matching Groovy truth).
     * <p>
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
     * Finds the first value matching the closure condition.  Example:
     * <pre class="groovyTestCase">def list = [1,2,3]
     * assert 2 == list.find { it {@code >} 1 }
     * </pre>
     *
     * @param self    a Collection
     * @param closure a closure condition
     * @return the first Object found, in the order of the collections iterator, or null if no element matches
     * @since 1.0
     */
    public static <T> T find(Collection<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        for (T value : self) {
            if (bcw.call(value)) {
                return value;
            }
        }
        return null;
    }

    /**
     * Finds the first element in the array that matches the given closure condition.
     * Example:
     * <pre class="groovyTestCase">
     * def list = [1,2,3] as Integer[]
     * assert 2 == list.find { it {@code >} 1 }
     * assert null == list.find { it {@code >} 5 }
     * </pre>
     *
     * @param self      an Array
     * @param condition a closure condition
     * @return the first element from the array that matches the condition or null if no element matches
     * @since 2.0
     */
    public static <T> T find(T[] self, @ClosureParams(FirstParam.Component.class) Closure condition) {
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(condition);
        for (T element : self) {
            if (bcw.call(element)) {
                return element;
            }
        }
        return null;
    }

    /**
     * Finds the first item matching the IDENTITY Closure (i.e.&#160;matching Groovy truth).
     * <p>
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
     * Treats the object as iterable, iterating through the values it represents and returns the first non-null result obtained from calling the closure, otherwise returns null.
     * <p>
     * <pre class="groovyTestCase">
     * int[] numbers = [1, 2, 3]
     * assert numbers.findResult { if(it {@code >} 1) return it } == 2
     * assert numbers.findResult { if(it {@code >} 4) return it } == null
     * </pre>
     *
     * @param self      an Object with an iterator returning its values
     * @param condition a closure that returns a non-null value to indicate that processing should stop and the value should be returned
     * @return the first non-null result of the closure
     * @since 1.7.5
     */
    public static Object findResult(Object self, Closure condition) {
        for (Iterator iter = InvokerHelper.asIterator(self); iter.hasNext(); ) {
            Object value = iter.next();
            Object result = condition.call(value);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Treats the object as iterable, iterating through the values it represents and returns the first non-null result obtained from calling the closure, otherwise returns the defaultResult.
     * <p>
     * <pre class="groovyTestCase">
     * int[] numbers = [1, 2, 3]
     * assert numbers.findResult(5) { if(it {@code >} 1) return it } == 2
     * assert numbers.findResult(5) { if(it {@code >} 4) return it } == 5
     * </pre>
     *
     * @param self          an Object with an iterator returning its values
     * @param defaultResult an Object that should be returned if all closure results are null
     * @param condition     a closure that returns a non-null value to indicate that processing should stop and the value should be returned
     * @return the first non-null result of the closure, otherwise the default value
     * @since 1.7.5
     */
    public static Object findResult(Object self, Object defaultResult, Closure condition) {
        Object result = findResult(self, condition);
        if (result == null) return defaultResult;
        return result;
    }

    /**
     * Iterates through the collection calling the given closure for each item but stopping once the first non-null
     * result is found and returning that result. If all are null, the defaultResult is returned.
     *
     * @param self          a Collection
     * @param defaultResult an Object that should be returned if all closure results are null
     * @param condition     a closure that returns a non-null value to indicate that processing should stop and the value should be returned
     * @return the first non-null result from calling the closure, or the defaultValue
     * @since 1.7.5
     * @deprecated use the Iterable version instead
     */
    @Deprecated
    public static <S, T, U extends T, V extends T> T findResult(Collection<S> self, U defaultResult, @ClosureParams(FirstParam.FirstGenericType.class) Closure<V> condition) {
        return findResult((Iterable<S>) self, defaultResult, condition);
    }

    /**
     * Iterates through the collection calling the given closure for each item but stopping once the first non-null
     * result is found and returning that result. If all results are null, null is returned.
     *
     * @param self      a Collection
     * @param condition a closure that returns a non-null value to indicate that processing should stop and the value should be returned
     * @return the first non-null result from calling the closure, or null
     * @since 1.7.5
     * @deprecated use the Iterable version instead
     */
    @Deprecated
    public static <S,T> T findResult(Collection<S> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<T> condition) {
        return findResult((Iterable<S>) self, condition);
    }

    /**
     * Iterates through the Iterator calling the given closure condition for each item but stopping once the first non-null
     * result is found and returning that result. If all are null, the defaultResult is returned.
     * <p>
     * Examples:
     * <pre class="groovyTestCase">
     * def iter = [1,2,3].iterator()
     * assert "Found 2" == iter.findResult("default") { it {@code >} 1 ? "Found $it" : null }
     * assert "default" == iter.findResult("default") { it {@code >} 3 ? "Found $it" : null }
     * </pre>
     *
     * @param self          an Iterator
     * @param defaultResult an Object that should be returned if all closure results are null
     * @param condition     a closure that returns a non-null value to indicate that processing should stop and the value should be returned
     * @return the first non-null result from calling the closure, or the defaultValue
     * @since 2.5.0
     */
    public static <S, T, U extends T, V extends T> T findResult(Iterator<S> self, U defaultResult, @ClosureParams(FirstParam.FirstGenericType.class) Closure<V> condition) {
        T result = findResult(self, condition);
        if (result == null) return defaultResult;
        return result;
    }

    /**
     * Iterates through the Iterator calling the given closure condition for each item but stopping once the first non-null
     * result is found and returning that result. If all results are null, null is returned.
     *
     * @param self      an Iterator
     * @param condition a closure that returns a non-null value to indicate that processing should stop and the value should be returned
     * @return the first non-null result from calling the closure, or null
     * @since 2.5.0
     */
    public static <T, U> T findResult(Iterator<U> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<T> condition) {
        while (self.hasNext()) {
            U next = self.next();
            T result = condition.call(next);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Iterates through the Iterable calling the given closure condition for each item but stopping once the first non-null
     * result is found and returning that result. If all are null, the defaultResult is returned.
     * <p>
     * Examples:
     * <pre class="groovyTestCase">
     * def list = [1,2,3]
     * assert "Found 2" == list.findResult("default") { it {@code >} 1 ? "Found $it" : null }
     * assert "default" == list.findResult("default") { it {@code >} 3 ? "Found $it" : null }
     * </pre>
     *
     * @param self          an Iterable
     * @param defaultResult an Object that should be returned if all closure results are null
     * @param condition     a closure that returns a non-null value to indicate that processing should stop and the value should be returned
     * @return the first non-null result from calling the closure, or the defaultValue
     * @since 2.5.0
     */
    public static <S, T, U extends T, V extends T> T findResult(Iterable<S> self, U defaultResult, @ClosureParams(FirstParam.FirstGenericType.class) Closure<V> condition) {
        T result = findResult(self, condition);
        if (result == null) return defaultResult;
        return result;
    }

    /**
     * Iterates through the Iterable calling the given closure condition for each item but stopping once the first non-null
     * result is found and returning that result. If all results are null, null is returned.
     *
     * @param self      an Iterable
     * @param condition a closure that returns a non-null value to indicate that processing should stop and the value should be returned
     * @return the first non-null result from calling the closure, or null
     * @since 2.5.0
     */
    public static <T, U> T findResult(Iterable<U> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<T> condition) {
        return findResult(self.iterator(), condition);
    }

    /**
     * Iterates through the Array calling the given closure condition for each item but stopping once the first non-null
     * result is found and returning that result. If all are null, the defaultResult is returned.
     *
     * @param self          an Array
     * @param defaultResult an Object that should be returned if all closure results are null
     * @param condition     a closure that returns a non-null value to indicate that processing should stop and the value should be returned
     * @return the first non-null result from calling the closure, or the defaultValue
     * @since 2.5.0
     */
    public static <S, T, U extends T, V extends T> T findResult(S[] self, U defaultResult, @ClosureParams(FirstParam.Component.class) Closure<V> condition) {
        return findResult(new ArrayIterator<S>(self), defaultResult, condition);
    }

    /**
     * Iterates through the Array calling the given closure condition for each item but stopping once the first non-null
     * result is found and returning that result. If all results are null, null is returned.
     *
     * @param self      an Array
     * @param condition a closure that returns a non-null value to indicate that processing should stop and the value should be returned
     * @return the first non-null result from calling the closure, or null
     * @since 2.5.0
     */
    public static <S, T> T findResult(S[] self, @ClosureParams(FirstParam.Component.class) Closure<T> condition) {
        return findResult(new ArrayIterator<S>(self), condition);
    }

    /**
     * Returns the first non-null closure result found by passing each map entry to the closure, otherwise null is returned.
     * If the closure takes two parameters, the entry key and value are passed.
     * If the closure takes one parameter, the Map.Entry object is passed.
     * <pre class="groovyTestCase">
     * assert "Found b:3" == [a:1, b:3].findResult { if (it.value == 3) return "Found ${it.key}:${it.value}" }
     * assert null == [a:1, b:3].findResult { if (it.value == 9) return "Found ${it.key}:${it.value}" }
     * assert "Found a:1" == [a:1, b:3].findResult { k, v {@code ->} if (k.size() + v == 2) return "Found $k:$v" }
     * </pre>
     *
     * @param self      a Map
     * @param condition a 1 or 2 arg Closure that returns a non-null value when processing should stop and a value should be returned
     * @return the first non-null result collected by calling the closure, or null if no such result was found
     * @since 1.7.5
     */
    public static <T, K, V> T findResult(Map<K, V> self, @ClosureParams(MapEntryOrKeyValue.class) Closure<T> condition) {
        for (Map.Entry<K, V> entry : self.entrySet()) {
            T result = callClosureForMapEntry(condition, entry);
            if (result != null) {
                return result;
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
     * assert "Found a:1" == [a:1, b:3].findResult("default") { k, v {@code ->} if (k.size() + v == 2) return "Found $k:$v" }
     * </pre>
     *
     * @param self          a Map
     * @param defaultResult an Object that should be returned if all closure results are null
     * @param condition     a 1 or 2 arg Closure that returns a non-null value when processing should stop and a value should be returned
     * @return the first non-null result collected by calling the closure, or the defaultResult if no such result was found
     * @since 1.7.5
     */
    public static <T, U extends T, V extends T, A, B> T findResult(Map<A, B> self, U defaultResult, @ClosureParams(MapEntryOrKeyValue.class) Closure<V> condition) {
        T result = findResult(self, condition);
        if (result == null) return defaultResult;
        return result;
    }

    /**
     * @see #findResults(Iterable, Closure)
     * @since 1.8.1
     * @deprecated Use the Iterable version of findResults instead
     */
    @Deprecated
    public static <T, U> Collection<T> findResults(Collection<U> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<T> filteringTransform) {
        return findResults((Iterable<?>) self, filteringTransform);
    }

    /**
     * Iterates through the Iterable transforming items using the supplied closure
     * and collecting any non-null results.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * def list = [1,2,3]
     * def result = list.findResults { it {@code >} 1 ? "Found $it" : null }
     * assert result == ["Found 2", "Found 3"]
     * </pre>
     *
     * @param self               an Iterable
     * @param filteringTransform a Closure that should return either a non-null transformed value or null for items which should be discarded
     * @return the list of non-null transformed values
     * @since 2.2.0
     */
    public static <T, U> Collection<T> findResults(Iterable<U> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<T> filteringTransform) {
        return findResults(self.iterator(), filteringTransform);
    }

    /**
     * Iterates through the Iterator transforming items using the supplied closure
     * and collecting any non-null results.
     *
     * @param self               an Iterator
     * @param filteringTransform a Closure that should return either a non-null transformed value or null for items which should be discarded
     * @return the list of non-null transformed values
     * @since 2.5.0
     */
    public static <T, U> Collection<T> findResults(Iterator<U> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<T> filteringTransform) {
        List<T> result = new ArrayList<T>();
        while (self.hasNext()) {
            U value = self.next();
            T transformed = filteringTransform.call(value);
            if (transformed != null) {
                result.add(transformed);
            }
        }
        return result;
    }

    /**
     * Iterates through the Array transforming items using the supplied closure
     * and collecting any non-null results.
     *
     * @param self               an Array
     * @param filteringTransform a Closure that should return either a non-null transformed value or null for items which should be discarded
     * @return the list of non-null transformed values
     * @since 2.5.0
     */
    public static <T, U> Collection<T> findResults(U[] self, @ClosureParams(FirstParam.Component.class) Closure<T> filteringTransform) {
        return findResults(new ArrayIterator<U>(self), filteringTransform);
    }

    /**
     * Iterates through the map transforming items using the supplied closure
     * and collecting any non-null results.
     * If the closure takes two parameters, the entry key and value are passed.
     * If the closure takes one parameter, the Map.Entry object is passed.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * def map = [a:1, b:2, hi:2, cat:3, dog:2]
     * def result = map.findResults { k, v {@code ->} k.size() == v ? "Found $k:$v" : null }
     * assert result == ["Found a:1", "Found hi:2", "Found cat:3"]
     * </pre>
     *
     * @param self               a Map
     * @param filteringTransform a 1 or 2 arg Closure that should return either a non-null transformed value or null for items which should be discarded
     * @return the list of non-null transformed values
     * @since 1.8.1
     */
    public static <T, K, V> Collection<T> findResults(Map<K, V> self, @ClosureParams(MapEntryOrKeyValue.class) Closure<T> filteringTransform) {
        List<T> result = new ArrayList<T>();
        for (Map.Entry<K, V> entry : self.entrySet()) {
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
    public static <K, V> Map.Entry<K, V> find(Map<K, V> self, @ClosureParams(MapEntryOrKeyValue.class) Closure<?> closure) {
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
        for (Map.Entry<K, V> entry : self.entrySet()) {
            if (bcw.callForMap(entry)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Finds all values matching the closure condition.
     * <pre class="groovyTestCase">assert ([2,4] as Set) == ([1,2,3,4] as Set).findAll { it % 2 == 0 }</pre>
     *
     * @param self    a Set
     * @param closure a closure condition
     * @return a Set of matching values
     * @since 2.4.0
     */
    public static <T> Set<T> findAll(Set<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        return (Set<T>) findAll((Collection<T>) self, closure);
    }

    /**
     * Finds all values matching the closure condition.
     * <pre class="groovyTestCase">assert [2,4] == [1,2,3,4].findAll { it % 2 == 0 }</pre>
     *
     * @param self    a List
     * @param closure a closure condition
     * @return a List of matching values
     * @since 2.4.0
     */
    public static <T> List<T> findAll(List<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        return (List<T>) findAll((Collection<T>) self, closure);
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
    public static <T> Collection<T> findAll(Collection<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        Collection<T> answer = createSimilarCollection(self);
        Iterator<T> iter = self.iterator();
        return findAll(closure, answer, iter);
    }

    /**
     * Finds all elements of the array matching the given Closure condition.
     * <pre class="groovyTestCase">
     * def items = [1,2,3,4] as Integer[]
     * assert [2,4] == items.findAll { it % 2 == 0 }
     * </pre>
     *
     * @param self      an array
     * @param condition a closure condition
     * @return a list of matching values
     * @since 2.0
     */
    public static <T> Collection<T> findAll(T[] self, @ClosureParams(FirstParam.Component.class) Closure condition) {
        Collection<T> answer = new ArrayList<T>();
        return findAll(condition, answer, new ArrayIterator<T>(self));
    }

    /**
     * Finds the items matching the IDENTITY Closure (i.e.&#160;matching Groovy truth).
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * def items = [1, 2, 0, false, true, '', 'foo', [], [4, 5], null] as Set
     * assert items.findAll() == [1, 2, true, 'foo', [4, 5]] as Set
     * </pre>
     *
     * @param self    a Set
     * @return a Set of the values found
     * @since 2.4.0
     * @see Closure#IDENTITY
     */
    public static <T> Set<T> findAll(Set<T> self) {
        return findAll(self, Closure.IDENTITY);
    }

    /**
     * Finds the items matching the IDENTITY Closure (i.e.&#160;matching Groovy truth).
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * def items = [1, 2, 0, false, true, '', 'foo', [], [4, 5], null]
     * assert items.findAll() == [1, 2, true, 'foo', [4, 5]]
     * </pre>
     *
     * @param self    a List
     * @return a List of the values found
     * @since 2.4.0
     * @see Closure#IDENTITY
     */
    public static <T> List<T> findAll(List<T> self) {
        return findAll(self, Closure.IDENTITY);
    }

    /**
     * Finds the items matching the IDENTITY Closure (i.e.&#160;matching Groovy truth).
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * def items = [1, 2, 0, false, true, '', 'foo', [], [4, 5], null]
     * assert items.findAll() == [1, 2, true, 'foo', [4, 5]]
     * </pre>
     *
     * @param self    a Collection
     * @return a Collection of the values found
     * @since 1.8.1
     * @see Closure#IDENTITY
     */
    public static <T> Collection<T> findAll(Collection<T> self) {
        return findAll(self, Closure.IDENTITY);
    }

    /**
     * Finds the elements of the array matching the IDENTITY Closure (i.e.&#160;matching Groovy truth).
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * def items = [1, 2, 0, false, true, '', 'foo', [], [4, 5], null] as Object[]
     * assert items.findAll() == [1, 2, true, 'foo', [4, 5]]
     * </pre>
     *
     * @param self an array
     * @return a collection of the elements found
     * @see Closure#IDENTITY
     * @since 2.0
     */
    public static <T> Collection<T> findAll(T[] self) {
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
     * Finds all items matching the IDENTITY Closure (i.e.&#160;matching Groovy truth).
     * <p>
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
     * Returns <tt>true</tt> if this iterable contains the item.
     *
     * @param  self an Iterable to be checked for containment
     * @param  item an Object to be checked for containment in this iterable
     * @return <tt>true</tt> if this iterable contains the item
     * @see    Collection#contains(Object)
     * @since 2.4.0
     */
    public static boolean contains(Iterable self, Object item) {
        for (Object e : self) {
            if (Objects.equals(item, e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <tt>true</tt> if this iterable contains all of the elements
     * in the specified array.
     *
     * @param  self  an Iterable to be checked for containment
     * @param  items array to be checked for containment in this iterable
     * @return <tt>true</tt> if this collection contains all of the elements
     *           in the specified array
     * @see    Collection#containsAll(Collection)
     * @since 2.4.0
     */
    public static boolean containsAll(Iterable self, Object[] items) {
        return asCollection(self).containsAll(Arrays.asList(items));
    }

    /**
     * @deprecated use the Iterable variant instead
     * @see #containsAll(Iterable, Object[])
     * @since 1.7.2
     */
    @Deprecated
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
     * <pre class="groovyTestCase">def list = ['a', 'b']
     * list.retainAll { it == 'b' }
     * assert list == ['b']</pre>
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
    public static <T> boolean retainAll(Collection<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
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
     * Modifies this map so that it retains only its elements that are matched
     * according to the specified closure condition.  In other words, removes from
     * this map all of its elements that don't match. If the closure takes one
     * parameter then it will be passed the <code>Map.Entry</code>. Otherwise the closure should
     * take two parameters, which will be the key and the value.
     *
     * <pre class="groovyTestCase">def map = [a:1, b:2]
     * map.retainAll { k,v {@code ->} k == 'b' }
     * assert map == [b:2]</pre>
     *
     * See also <code>findAll</code> when wanting to produce a new map containing items
     * which match some criteria but leaving the original map unchanged.
     *
     * @param self a Map to be modified
     * @param condition a 1 or 2 arg Closure condition applying on the entries
     * @return <tt>true</tt> if this map changed as a result of the call
     * @since 2.5.0
     */
    public static <K, V> boolean retainAll(Map<K, V> self, @ClosureParams(MapEntryOrKeyValue.class) Closure condition) {
        Iterator<Map.Entry<K, V>> iter = self.entrySet().iterator();
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(condition);
        boolean result = false;
        while (iter.hasNext()) {
            Map.Entry<K, V> entry = iter.next();
            if (!bcw.callForMap(entry)) {
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
     * <pre class="groovyTestCase">def list = ['a', 'b']
     * list.removeAll { it == 'b' }
     * assert list == ['a']</pre>
     *
     * See also <code>findAll</code> and <code>grep</code> when wanting to produce a new list
     * containing items which match some criteria but leaving the original collection unchanged.
     *
     * @param  self a Collection to be modified
     * @param  condition a closure condition
     * @return <tt>true</tt> if this collection changed as a result of the call
     * @see    Iterator#remove()
     * @since 1.7.2
     */
    public static <T> boolean removeAll(Collection<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
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
     * Modifies this map by removing the elements that are matched according to the
     * specified closure condition. If the closure takes one parameter then it will be
     * passed the <code>Map.Entry</code>. Otherwise the closure should take two parameters, which
     * will be the key and the value.
     *
     * <pre class="groovyTestCase">def map = [a:1, b:2]
     * map.removeAll { k,v {@code ->} k == 'b' }
     * assert map == [a:1]</pre>
     *
     * See also <code>findAll</code> when wanting to produce a new map containing items
     * which match some criteria but leaving the original map unchanged.
     *
     * @param self a Map to be modified
     * @param condition a 1 or 2 arg Closure condition applying on the entries
     * @return <tt>true</tt> if this map changed as a result of the call
     * @since 2.5.0
     */
    public static <K, V> boolean removeAll(Map<K, V> self, @ClosureParams(MapEntryOrKeyValue.class) Closure condition) {
        Iterator<Map.Entry<K, V>> iter = self.entrySet().iterator();
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(condition);
        boolean result = false;
        while (iter.hasNext()) {
            Map.Entry<K, V> entry = iter.next();
            if (bcw.callForMap(entry)) {
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
    public static <T> Collection<Collection<T>> split(Collection<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        Collection<T> accept = createSimilarCollection(self);
        Collection<T> reject = createSimilarCollection(self);
        Iterator<T> iter = self.iterator();
        return split(closure, accept, reject, iter);
    }

    /**
     * Splits all items into two collections based on the closure condition.
     * The first list contains all items which match the closure expression.
     * The second list all those that don't.
     *
     * @param self    an Array
     * @param closure a closure condition
     * @return a List whose first item is the accepted values and whose second item is the rejected values
     * @since 2.5.0
     */
    public static <T> Collection<Collection<T>> split(T[] self, @ClosureParams(FirstParam.Component.class) Closure closure) {
        List<T> accept = new ArrayList<T>();
        List<T> reject = new ArrayList<T>();
        Iterator<T> iter = new ArrayIterator<T>(self);
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
     * Splits all items into two collections based on the closure condition.
     * The first list contains all items which match the closure expression.
     * The second list all those that don't.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">assert [[2,4],[1,3]] == [1,2,3,4].split { it % 2 == 0 }</pre>
     *
     * @param self    a List of values
     * @param closure a closure condition
     * @return a List whose first item is the accepted values and whose second item is the rejected values
     * @since 2.4.0
     */
    @SuppressWarnings("unchecked")
    public static <T> List<List<T>> split(List<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        return (List<List<T>>) (List<?>) split((Collection<T>) self, closure);
    }

    /**
     * Splits all items into two collections based on the closure condition.
     * The first list contains all items which match the closure expression.
     * The second list all those that don't.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">assert [[2,4] as Set, [1,3] as Set] == ([1,2,3,4] as Set).split { it % 2 == 0 }</pre>
     *
     * @param self    a Set of values
     * @param closure a closure condition
     * @return a List whose first item is the accepted values and whose second item is the rejected values
     * @since 2.4.0
     */
    @SuppressWarnings("unchecked")
    public static <T> List<Set<T>> split(Set<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        return (List<Set<T>>) (List<?>) split((Collection<T>) self, closure);
    }

    /**
     * @deprecated Use the Iterable version of combinations instead
     * @see #combinations(Iterable)
     * @since 1.5.0
     */
    @Deprecated
    public static List combinations(Collection self) {
        return combinations((Iterable)self);
    }

    /**
     * Adds GroovyCollections#combinations(Iterable) as a method on Iterables.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * assert [['a', 'b'],[1, 2, 3]].combinations() == [['a', 1], ['b', 1], ['a', 2], ['b', 2], ['a', 3], ['b', 3]]
     * </pre>
     *
     * @param self an Iterable of collections
     * @return a List of the combinations found
     * @see groovy.util.GroovyCollections#combinations(java.lang.Iterable)
     * @since 2.2.0
     */
    public static List combinations(Iterable self) {
        return GroovyCollections.combinations(self);
    }


    /**
     * Adds GroovyCollections#combinations(Iterable, Closure) as a method on collections.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">assert [[2, 3],[4, 5, 6]].combinations {x,y {@code ->} x*y } == [8, 12, 10, 15, 12, 18]</pre>
     *
     * @param self a Collection of lists
     * @param function a closure to be called on each combination
     * @return a List of the results of applying the closure to each combinations found
     * @see groovy.util.GroovyCollections#combinations(Iterable)
     * @since 2.2.0
     */
    public static List combinations(Iterable self, Closure<?> function) {
        return collect((Iterable)GroovyCollections.combinations(self), function);
    }

    /**
     * Applies a function on each combination of the input lists.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">[[2, 3],[4, 5, 6]].eachCombination { println "Found $it" }</pre>
     *
     * @param self a Collection of lists
     * @param function a closure to be called on each combination
     * @see groovy.util.GroovyCollections#combinations(Iterable)
     * @since 2.2.0
     */
    public static void eachCombination(Iterable self, Closure<?> function) {
        each(GroovyCollections.combinations(self), function);
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
     * Finds all permutations of an iterable.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">def result = [1, 2, 3].permutations()
     * assert result == [[3, 2, 1], [3, 1, 2], [1, 3, 2], [2, 3, 1], [2, 1, 3], [1, 2, 3]] as Set</pre>
     *
     * @param self the Iterable of items
     * @return the permutations from the list
     * @since 1.7.0
     */
    public static <T> Set<List<T>> permutations(Iterable<T> self) {
        Set<List<T>> ans = new HashSet<List<T>>();
        PermutationGenerator<T> generator = new PermutationGenerator<T>(self);
        while (generator.hasNext()) {
            ans.add(generator.next());
        }
        return ans;
    }

    /**
     * @deprecated Use the Iterable version of permutations instead
     * @see #permutations(Iterable)
     * @since 1.7.0
     */
    @Deprecated
    public static <T> Set<List<T>> permutations(List<T> self) {
        return permutations((Iterable<T>) self);
    }

    /**
     * Finds all permutations of an iterable, applies a function to each permutation and collects the result
     * into a list.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">Set result = [1, 2, 3].permutations { it.collect { v {@code ->} 2*v }}
     * assert result == [[6, 4, 2], [6, 2, 4], [2, 6, 4], [4, 6, 2], [4, 2, 6], [2, 4, 6]] as Set</pre>
     *
     * @param self the Iterable of items
     * @param function the function to apply on each permutation
     * @return the list of results of the application of the function on each permutation
     * @since 2.2.0
     */
    public static <T,V> List<V> permutations(Iterable<T> self, Closure<V> function) {
        return collect((Iterable<List<T>>) permutations(self),function);
    }

    /**
     * @deprecated Use the Iterable version of permutations instead
     * @see #permutations(Iterable, Closure)
     * @since 2.2.0
     */
    @Deprecated
    public static <T, V> List<V> permutations(List<T> self, Closure<V> function) {
        return permutations((Iterable<T>) self, function);
    }

    /**
     * @deprecated Use the Iterable version of eachPermutation instead
     * @see #eachPermutation(Iterable, Closure)
     * @since 1.7.0
     */
    @Deprecated
    public static <T> Iterator<List<T>> eachPermutation(Collection<T> self, Closure closure) {
        return eachPermutation((Iterable<T>) self, closure);
    }

    /**
     * Iterates over all permutations of a collection, running a closure for each iteration.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">def permutations = []
     * [1, 2, 3].eachPermutation{ permutations &lt;&lt; it }
     * assert permutations == [[1, 2, 3], [1, 3, 2], [2, 1, 3], [2, 3, 1], [3, 1, 2], [3, 2, 1]]</pre>
     *
     * @param self the Collection of items
     * @param closure the closure to call for each permutation
     * @return the permutations from the list
     * @since 1.7.0
     */
    public static <T> Iterator<List<T>> eachPermutation(Iterable<T> self, Closure closure) {
        Iterator<List<T>> generator = new PermutationGenerator<T>(self);
        while (generator.hasNext()) {
            closure.call(generator.next());
        }
        return generator;
    }

    /**
     * Adds GroovyCollections#transpose(List) as a method on lists.
     * A Transpose Function takes a collection of columns and returns a collection of
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
     * <pre class="groovyTestCase">
     * def result = [a:1, b:2, c:4, d:5].findAll { it.value % 2 == 0 }
     * assert result.every { it instanceof Map.Entry }
     * assert result*.key == ["b", "c"]
     * assert result*.value == [2, 4]
     * </pre>
     *
     * @param self    a Map
     * @param closure a 1 or 2 arg Closure condition applying on the entries
     * @return a new subMap
     * @since 1.0
     */
    public static <K, V> Map<K, V> findAll(Map<K, V> self, @ClosureParams(MapEntryOrKeyValue.class)  Closure closure) {
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
     * @deprecated Use the Iterable version of groupBy instead
     * @see #groupBy(Iterable, Closure)
     * @since 1.0
     */
    @Deprecated
    public static <K, T> Map<K, List<T>> groupBy(Collection<T> self, Closure<K> closure) {
        return groupBy((Iterable<T>)self, closure);
    }

    /**
     * Sorts all Iterable members into groups determined by the supplied mapping closure.
     * The closure should return the key that this item should be grouped by. The returned
     * LinkedHashMap will have an entry for each distinct key returned from the closure,
     * with each value being a list of items for that group.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * assert [0:[2,4,6], 1:[1,3,5]] == [1,2,3,4,5,6].groupBy { it % 2 }
     * </pre>
     *
     * @param self    a collection to group
     * @param closure a closure mapping entries on keys
     * @return a new Map grouped by keys
     * @since 2.2.0
     */
    public static <K, T> Map<K, List<T>> groupBy(Iterable<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<K> closure) {
        Map<K, List<T>> answer = new LinkedHashMap<K, List<T>>();
        for (T element : self) {
            K value = closure.call(element);
            groupAnswer(answer, element, value);
        }
        return answer;
    }

    /**
     * Sorts all array members into groups determined by the supplied mapping closure.
     * The closure should return the key that this item should be grouped by. The returned
     * LinkedHashMap will have an entry for each distinct key returned from the closure,
     * with each value being a list of items for that group.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * Integer[] items = [1,2,3,4,5,6]
     * assert [0:[2,4,6], 1:[1,3,5]] == items.groupBy { it % 2 }
     * </pre>
     *
     * @param self    an array to group
     * @param closure a closure mapping entries on keys
     * @return a new Map grouped by keys
     * @see #groupBy(Iterable, Closure)
     * @since 2.2.0
     */
    public static <K, T> Map<K, List<T>> groupBy(T[] self, @ClosureParams(FirstParam.Component.class) Closure<K> closure) {
        return groupBy((Iterable<T>)Arrays.asList(self), closure);
    }

    /**
     * @deprecated Use the Iterable version of groupBy instead
     * @see #groupBy(Iterable, Object...)
     * @since 1.8.1
     */
    @Deprecated
    public static Map groupBy(Collection self, Object... closures) {
        return groupBy((Iterable)self, closures);
    }

    /**
     * Sorts all Iterable members into (sub)groups determined by the supplied
     * mapping closures. Each closure should return the key that this item
     * should be grouped by. The returned LinkedHashMap will have an entry for each
     * distinct 'key path' returned from the closures, with each value being a list
     * of items for that 'group path'.
     *
     * Example usage:
     * <pre class="groovyTestCase">def result = [1,2,3,4,5,6].groupBy({ it % 2 }, { it {@code <} 4 })
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
     * @since 2.2.0
     * @see Closure#IDENTITY
     */
    public static Map groupBy(Iterable self, Object... closures) {
        final Closure head = closures.length == 0 ? Closure.IDENTITY : (Closure) closures[0];

        @SuppressWarnings("unchecked")
        Map<Object, List> first = groupBy(self, head);
        if (closures.length < 2)
            return first;

        final Object[] tail = new Object[closures.length - 1];
        System.arraycopy(closures, 1, tail, 0, closures.length - 1); // Arrays.copyOfRange only since JDK 1.6

        // inject([:]) { a,e {@code ->} a {@code <<} [(e.key): e.value.groupBy(tail)] }
        Map<Object, Map> acc = new LinkedHashMap<Object, Map>();
        for (Map.Entry<Object, List> item : first.entrySet()) {
            acc.put(item.getKey(), groupBy((Iterable)item.getValue(), tail));
        }

        return acc;
    }

    /**
     * Sorts all array members into (sub)groups determined by the supplied
     * mapping closures as per the Iterable variant of this method.
     *
     * @param self     an array to group
     * @param closures an array of closures, each mapping entries on keys
     * @return a new Map grouped by keys on each criterion
     * @see #groupBy(Iterable, Object...)
     * @see Closure#IDENTITY
     * @since 2.2.0
     */
    public static Map groupBy(Object[] self, Object... closures) {
        return groupBy((Iterable)Arrays.asList(self), closures);
    }

    /**
     * @deprecated Use the Iterable version of groupBy instead
     * @see #groupBy(Iterable, List)
     * @since 1.8.1
     */
    @Deprecated
    public static Map groupBy(Collection self, List<Closure> closures) {
        return groupBy((Iterable)self, closures);
    }

    /**
     * Sorts all Iterable members into (sub)groups determined by the supplied
     * mapping closures. Each closure should return the key that this item
     * should be grouped by. The returned LinkedHashMap will have an entry for each
     * distinct 'key path' returned from the closures, with each value being a list
     * of items for that 'group path'.
     *
     * Example usage:
     * <pre class="groovyTestCase">
     * def result = [1,2,3,4,5,6].groupBy([{ it % 2 }, { it {@code <} 4 }])
     * assert result == [1:[(true):[1, 3], (false):[5]], 0:[(true):[2], (false):[4, 6]]]
     * </pre>
     *
     * Another example:
     * <pre>
     * def sql = groovy.sql.Sql.newInstance(/&ast; ... &ast;/)
     * def data = sql.rows("SELECT * FROM a_table").groupBy([{ it.column1 }, { it.column2 }, { it.column3 }])
     * if (data.val1.val2.val3) {
     *     // there exists a record where:
     *     //   a_table.column1 == val1
     *     //   a_table.column2 == val2, and
     *     //   a_table.column3 == val3
     * } else {
     *     // there is no such record
     * }
     * </pre>
     * If an empty list of closures is supplied the IDENTITY Closure will be used.
     *
     * @param self     a collection to group
     * @param closures a list of closures, each mapping entries on keys
     * @return a new Map grouped by keys on each criterion
     * @since 2.2.0
     * @see Closure#IDENTITY
     */
    public static Map groupBy(Iterable self, List<Closure> closures) {
        return groupBy(self, closures.toArray());
    }

    /**
     * Sorts all array members into (sub)groups determined by the supplied
     * mapping closures as per the list variant of this method.
     *
     * @param self     an array to group
     * @param closures a list of closures, each mapping entries on keys
     * @return a new Map grouped by keys on each criterion
     * @see Closure#IDENTITY
     * @see #groupBy(Iterable, List)
     * @since 2.2.0
     */
    public static Map groupBy(Object[] self, List<Closure> closures) {
        return groupBy((Iterable)Arrays.asList(self), closures);
    }

    /**
     * @deprecated Use the Iterable version of countBy instead
     * @see #countBy(Iterable, Closure)
     * @since 1.8.0
     */
    @Deprecated
    public static <K> Map<K, Integer> countBy(Collection self, Closure<K> closure) {
        return countBy((Iterable) self, closure);
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
     * @since 2.2.0
     */
    public static <K,E> Map<K, Integer> countBy(Iterable<E> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<K> closure) {
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
     * @param self    an array to group and count
     * @param closure a closure mapping items to the frequency keys
     * @return a new Map grouped by keys with frequency counts
     * @see #countBy(Collection, Closure)
     * @since 1.8.0
     */
    public static <K,E> Map<K, Integer> countBy(E[] self, @ClosureParams(FirstParam.Component.class) Closure<K> closure) {
        return countBy((Iterable)Arrays.asList(self), closure);
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
    public static <K,E> Map<K, Integer> countBy(Iterator<E> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<K> closure) {
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
    public static <G, K, V> Map<G, List<Map.Entry<K, V>>> groupEntriesBy(Map<K, V> self, @ClosureParams(MapEntryOrKeyValue.class) Closure<G> closure) {
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
    public static <G, K, V> Map<G, Map<K, V>> groupBy(Map<K, V> self, @ClosureParams(MapEntryOrKeyValue.class) Closure<G> closure) {
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
     * belong to each such 'group path'.
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
     * belong to each such 'group path'.
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
    public static <K,U,V> Map<K, Integer> countBy(Map<U,V> self, @ClosureParams(MapEntryOrKeyValue.class) Closure<K> closure) {
        Map<K, Integer> answer = new LinkedHashMap<K, Integer>();
        for (Map.Entry<U,V> entry : self.entrySet()) {
            countAnswer(answer, callClosureForMapEntry(closure, entry));
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
        List<T> groupedElements = answer.computeIfAbsent(value, k -> new ArrayList<T>());

        groupedElements.add(element);
    }

    private static <T> void countAnswer(final Map<T, Integer> answer, T mappedKey) {
        Integer current = answer.get(mappedKey);

        if (null == current) {
            current = 0;
        }

        answer.put(mappedKey, current + 1);
    }

    // internal helper method
    protected static <T, K, V> T callClosureForMapEntry(@ClosureParams(value=FromString.class, options={"K,V","Map.Entry<K,V>"}) Closure<T> closure, Map.Entry<K,V> entry) {
        if (closure.getMaximumNumberOfParameters() == 2) {
            return closure.call(entry.getKey(), entry.getValue());
        }
        return closure.call(entry);
    }

    // internal helper method
    protected static <T> T callClosureForLine(@ClosureParams(value=FromString.class, options={"String","String,Integer"}) Closure<T> closure, String line, int counter) {
        if (closure.getMaximumNumberOfParameters() == 2) {
            return closure.call(line, counter);
        }
        return closure.call(line);
    }

    // internal helper method
    protected static <T, K, V> T callClosureForMapEntryAndCounter(@ClosureParams(value=FromString.class, options={"K,V,Integer", "K,V","Map.Entry<K,V>"}) Closure<T> closure, Map.Entry<K,V> entry, int counter) {
        if (closure.getMaximumNumberOfParameters() == 3) {
            return closure.call(entry.getKey(), entry.getValue(), counter);
        }
        if (closure.getMaximumNumberOfParameters() == 2) {
            return closure.call(entry, counter);
        }
        return closure.call(entry);
    }


    /**
     * Performs the same function as the version of inject that takes an initial value, but
     * uses the head of the Collection as the initial value, and iterates over the tail.
     * <pre class="groovyTestCase">
     * assert 1 * 2 * 3 * 4 == [ 1, 2, 3, 4 ].inject { acc, val {@code ->} acc * val }
     * assert ['b'] == [['a','b'], ['b','c'], ['d','b']].inject { acc, val {@code ->} acc.intersect( val ) }
     * LinkedHashSet set = [ 't', 'i', 'm' ]
     * assert 'tim' == set.inject { a, b {@code ->} a + b }
     * </pre>
     *
     * @param self         a Collection
     * @param closure      a closure
     * @return the result of the last closure call
     * @throws NoSuchElementException if the collection is empty.
     * @see #inject(Collection, Object, Closure)
     * @since 1.8.7
     */
    public static <T, V extends T> T inject(Collection<T> self, @ClosureParams(value=FromString.class,options="V,T") Closure<V> closure ) {
        if( self.isEmpty() ) {
            throw new NoSuchElementException( "Cannot call inject() on an empty collection without passing an initial value." ) ;
        }
        Iterator<T> iter = self.iterator();
        T head = iter.next();
        Collection<T> tail = tail(self);
        if (!tail.iterator().hasNext()) {
            return head;
        }
        // cast with explicit weaker generics for now to keep jdk6 happy, TODO: find better fix
        return (T) inject((Collection) tail, head, closure);
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
     * assert 1*1*2*3*4 == [1,2,3,4].inject(1) { acc, val {@code ->} acc * val }
     *
     * assert 0+1+2+3+4 == [1,2,3,4].inject(0) { acc, val {@code ->} acc + val }
     *
     * assert 'The quick brown fox' ==
     *     ['quick', 'brown', 'fox'].inject('The') { acc, val {@code ->} acc + ' ' + val }
     *
     * assert 'bat' ==
     *     ['rat', 'bat', 'cat'].inject('zzz') { min, next {@code ->} next {@code <} min ? next : min }
     *
     * def max = { a, b {@code ->} [a, b].max() }
     * def animals = ['bat', 'rat', 'cat']
     * assert 'rat' == animals.inject('aaa', max)
     * </pre>
     * Visual representation of the last example above:
     * <pre>
     *    initVal  animals[0]
     *       v        v
     * max('aaa',   'bat')  {@code =>}  'bat'  animals[1]
     *                            v       v
     *                      max('bat',  'rat')  {@code =>}  'rat'  animals[2]
     *                                                v       v
     *                                          max('rat',  'cat')  {@code =>}  'rat'
     * </pre>
     *
     * @param self         a Collection
     * @param initialValue some initial value
     * @param closure      a closure
     * @return the result of the last closure call
     * @since 1.0
     */
    public static <E, T, U extends T, V extends T> T inject(Collection<E> self, U initialValue, @ClosureParams(value=FromString.class,options="U,E") Closure<V> closure) {
        // cast with explicit weaker generics for now to keep jdk6 happy, TODO: find better fix
        return (T) inject((Iterator) self.iterator(), initialValue, closure);
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
     * assert map.inject([]) { list, k, v {@code ->}
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
    public static <K, V, T, U extends T, W extends T> T inject(Map<K, V> self, U initialValue, @ClosureParams(value=FromString.class,options={"U,Map.Entry<K,V>","U,K,V"})  Closure<W> closure) {
        T value = initialValue;
        for (Map.Entry<K, V> entry : self.entrySet()) {
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
    public static <E,T, U extends T, V extends T> T inject(Iterator<E> self, U initialValue, @ClosureParams(value=FromString.class,options="U,E") Closure<V> closure) {
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
    public static <E,T, V extends T> T inject(E[] self, @ClosureParams(value=FromString.class,options="E,E") Closure<V> closure) {
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
    public static <E, T, U extends T, V extends T> T inject(E[] self, U initialValue, @ClosureParams(value=FromString.class,options="U,E") Closure<V> closure) {
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
     * @deprecated Use the Iterable version of sum instead
     * @see #sum(Iterable)
     * @since 1.0
     */
    @Deprecated
    public static Object sum(Collection self) {
        return sum((Iterable)self);
    }

    /**
     * Sums the items in an Iterable.  This is equivalent to invoking the
     * "plus" method on all items in the Iterable.
     * <pre class="groovyTestCase">assert 1+2+3+4 == [1,2,3,4].sum()</pre>
     *
     * @param self Iterable of values to add together
     * @return The sum of all of the items
     * @since 2.2.0
     */
    public static Object sum(Iterable self) {
        return sum(self, null, true);
    }

    /**
     * Sums the items in an array.  This is equivalent to invoking the
     * "plus" method on all items in the array.
     *
     * @param self The array of values to add together
     * @return The sum of all of the items
     * @see #sum(java.util.Iterator)
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
     * Sums the items in an array.
     * <pre class="groovyTestCase">assert (1+2+3+4 as byte) == ([1,2,3,4] as byte[]).sum()</pre>
     *
     * @param self The array of values to add together
     * @return The sum of all of the items
     * @since 2.4.2
     */
    public static byte sum(byte[] self) {
        return sum(self, (byte) 0);
    }

    /**
     * Sums the items in an array.
     * <pre class="groovyTestCase">assert (1+2+3+4 as short) == ([1,2,3,4] as short[]).sum()</pre>
     *
     * @param self The array of values to add together
     * @return The sum of all of the items
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
     * @return The sum of all of the items
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
     * @return The sum of all of the items
     * @since 2.4.2
     */
    public static long sum(long[] self) {
        return sum(self, 0);
    }

    /**
     * Sums the items in an array.
     * <pre class="groovyTestCase">assert (1+2+3+4 as char) == ([1,2,3,4] as char[]).sum()</pre>
     *
     * @param self The array of values to add together
     * @return The sum of all of the items
     * @since 2.4.2
     */
    public static char sum(char[] self) {
        return sum(self, (char) 0);
    }

    /**
     * Sums the items in an array.
     * <pre class="groovyTestCase">assert (1+2+3+4 as float) == ([1,2,3,4] as float[]).sum()</pre>
     *
     * @param self The array of values to add together
     * @return The sum of all of the items
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
     * @return The sum of all of the items
     * @since 2.4.2
     */
    public static double sum(double[] self) {
        return sum(self, 0);
    }

    /**
     * @deprecated Use the Iterable version of sum instead
     * @see #sum(Iterable, Object)
     * @since 1.5.0
     */
    @Deprecated
    public static Object sum(Collection self, Object initialValue) {
        return sum(self, initialValue, false);
    }

    /**
     * Sums the items in an Iterable, adding the result to some initial value.
     * <pre class="groovyTestCase">
     * assert 5+1+2+3+4 == [1,2,3,4].sum(5)
     * </pre>
     *
     * @param self         an Iterable of values to sum
     * @param initialValue the items in the collection will be summed to this initial value
     * @return The sum of all of the items.
     * @since 2.2.0
     */
    public static Object sum(Iterable self, Object initialValue) {
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

    private static Object sum(Iterable self, Object initialValue, boolean first) {
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
     * Sums the items in an array, adding the result to some initial value.
     * <pre class="groovyTestCase">assert (5+1+2+3+4 as byte) == ([1,2,3,4] as byte[]).sum(5 as byte)</pre>
     *
     * @param self         an array of values to sum
     * @param initialValue the items in the array will be summed to this initial value
     * @return The sum of all of the items.
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
     * <pre class="groovyTestCase">assert (5+1+2+3+4 as short) == ([1,2,3,4] as short[]).sum(5 as short)</pre>
     *
     * @param self         an array of values to sum
     * @param initialValue the items in the array will be summed to this initial value
     * @return The sum of all of the items.
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
     * @return The sum of all of the items.
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
     * @return The sum of all of the items.
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
     * <pre class="groovyTestCase">assert (5+1+2+3+4 as char) == ([1,2,3,4] as char[]).sum(5 as char)</pre>
     *
     * @param self         an array of values to sum
     * @param initialValue the items in the array will be summed to this initial value
     * @return The sum of all of the items.
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
     * <pre class="groovyTestCase">assert (5+1+2+3+4 as float) == ([1,2,3,4] as float[]).sum(5)</pre>
     *
     * @param self         an array of values to sum
     * @param initialValue the items in the array will be summed to this initial value
     * @return The sum of all of the items.
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
     * @return The sum of all of the items.
     * @since 2.4.2
     */
    public static double sum(double[] self, double initialValue) {
        double s = initialValue;
        for (double v : self) {
            s += v;
        }
        return s;
    }

    /**
     * @deprecated Use the Iterable version of sum instead
     * @see #sum(Iterable, Closure)
     * @since 1.0
     */
    @Deprecated
    public static Object sum(Collection self, Closure closure) {
        return sum((Iterable)self, closure);
    }

    /**
     * Sums the result of applying a closure to each item of an Iterable.
     * <code>coll.sum(closure)</code> is equivalent to:
     * <code>coll.collect(closure).sum()</code>.
     * <pre class="groovyTestCase">assert 4+6+10+12 == [2,3,5,6].sum { it * 2 }</pre>
     *
     * @param self    an Iterable
     * @param closure a single parameter closure that returns a (typically) numeric value.
     * @return The sum of the values returned by applying the closure to each
     *         item of the Iterable.
     * @since 2.2.0
     */
    public static <T> Object sum(Iterable<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        return sum(self.iterator(), null, closure, true);
    }

    /**
     * Sums the result of applying a closure to each item of an array.
     * <code>array.sum(closure)</code> is equivalent to:
     * <code>array.collect(closure).sum()</code>.
     *
     * @param self    An array
     * @param closure a single parameter closure that returns a (typically) numeric value.
     * @return The sum of the values returned by applying the closure to each
     *         item of the array.
     * @since 1.7.1
     */
    public static <T> Object sum(T[] self, @ClosureParams(FirstParam.Component.class) Closure closure) {
        return sum(new ArrayIterator<T>(self), null, closure, true);
    }

    /**
     * Sums the result of applying a closure to each item returned from an iterator.
     * <code>iter.sum(closure)</code> is equivalent to:
     * <code>iter.collect(closure).sum()</code>. The iterator will become
     * exhausted of elements after determining the sum value.
     *
     * @param self    An Iterator
     * @param closure a single parameter closure that returns a (typically) numeric value.
     * @return The sum of the values returned by applying the closure to each
     *         item from the Iterator.
     * @since 1.7.1
     */
    public static <T> Object sum(Iterator<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        return sum(self, null, closure, true);
    }

    /**
     * @deprecated Use the Iterable version of sum instead
     * @see #sum(Iterable, Object, Closure)
     * @since 1.5.0
     */
    @Deprecated
    public static Object sum(Collection self, Object initialValue, Closure closure) {
        return sum((Iterable)self, initialValue, closure);
    }

    /**
     * Sums the result of applying a closure to each item of an Iterable to some initial value.
     * <code>iter.sum(initVal, closure)</code> is equivalent to:
     * <code>iter.collect(closure).sum(initVal)</code>.
     * <pre class="groovyTestCase">assert 50+4+6+10+12 == [2,3,5,6].sum(50) { it * 2 }</pre>
     *
     * @param self         an Iterable
     * @param closure      a single parameter closure that returns a (typically) numeric value.
     * @param initialValue the closure results will be summed to this initial value
     * @return The sum of the values returned by applying the closure to each
     *         item of the collection.
     * @since 1.5.0
     */
    public static <T> Object sum(Iterable<T> self, Object initialValue, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        return sum(self.iterator(), initialValue, closure, false);
    }

    /**
     * Sums the result of applying a closure to each item of an array to some initial value.
     * <code>array.sum(initVal, closure)</code> is equivalent to:
     * <code>array.collect(closure).sum(initVal)</code>.
     *
     * @param self         an array
     * @param closure      a single parameter closure that returns a (typically) numeric value.
     * @param initialValue the closure results will be summed to this initial value
     * @return The sum of the values returned by applying the closure to each
     *         item of the array.
     * @since 1.7.1
     */
    public static <T> Object sum(T[] self, Object initialValue, @ClosureParams(FirstParam.Component.class) Closure closure) {
        return sum(new ArrayIterator<T>(self), initialValue, closure, false);
    }

    /**
     * Sums the result of applying a closure to each item of an Iterator to some initial value.
     * <code>iter.sum(initVal, closure)</code> is equivalent to:
     * <code>iter.collect(closure).sum(initVal)</code>. The iterator will become
     * exhausted of elements after determining the sum value.
     *
     * @param self         an Iterator
     * @param closure      a single parameter closure that returns a (typically) numeric value.
     * @param initialValue the closure results will be summed to this initial value
     * @return The sum of the values returned by applying the closure to each
     *         item from the Iterator.
     * @since 1.7.1
     */
    public static <T> Object sum(Iterator<T> self, Object initialValue, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        return sum(self, initialValue, closure, false);
    }

    private static <T> Object sum(Iterator<T> self, Object initialValue, Closure closure, boolean first) {
        Object result = initialValue;
        Object[] closureParam = new Object[1];
        Object[] plusParam = new Object[1];
        while (self.hasNext()) {
            closureParam[0] = self.next();
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
     * Averages the items in an Iterable.  This is equivalent to invoking the
     * "plus" method on all items in the Iterable and then dividing by the
     * total count using the "div" method for the resulting sum.
     * <pre class="groovyTestCase">assert 3 == [1, 2, 6].average()</pre>
     *
     * @param self Iterable of values to average
     * @return The average of all of the items
     * @since 3.0.0
     */
    public static Object average(Iterable self) {
        Object result = null;
        long count = 0;
        Object[] param = new Object[1];
        for (Object next : self) {
            param[0] = next;
            if (count == 0) {
                result = param[0];
            } else {
                MetaClass metaClass = InvokerHelper.getMetaClass(result);
                result = metaClass.invokeMethod(result, "plus", param);
            }
            count++;
        }
        MetaClass metaClass = InvokerHelper.getMetaClass(result);
        result = metaClass.invokeMethod(result, "div", count);
        return result;
    }

    /**
     * Averages the items in an array.  This is equivalent to invoking the
     * "plus" method on all items in the array and then dividing by the
     * total count using the "div" method for the resulting sum.
     * <pre class="groovyTestCase">assert 3 == ([1, 2, 6] as Integer[]).average()</pre>
     *
     * @param self The array of values to average
     * @return The average of all of the items
     * @see #sum(java.lang.Object[])
     * @since 3.0.0
     */
    public static Object average(Object[] self) {
        Object result = sum(self);
        MetaClass metaClass = InvokerHelper.getMetaClass(result);
        result = metaClass.invokeMethod(result, "div", self.length);
        return result;
    }

    /**
     * Averages the items from an Iterator.  This is equivalent to invoking the
     * "plus" method on all items in the array and then dividing by the
     * total count using the "div" method for the resulting sum.
     * The iterator will become exhausted of elements after determining the average value.
     *
     * @param self an Iterator for the values to average
     * @return The average of all of the items
     * @since 3.0.0
     */
    public static Object average(Iterator<Object> self) {
        return average(toList(self));
    }

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
        return s/count;
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
        return s/count;
    }

    /**
     * Averages the result of applying a closure to each item of an Iterable.
     * <code>iter.average(closure)</code> is equivalent to:
     * <code>iter.collect(closure).average()</code>.
     * <pre class="groovyTestCase">
     * assert 20 == [1, 3].average { it * 10 }
     * assert 3 == ['to', 'from'].average { it.size() }
     * </pre>
     *
     * @param self    an Iterable
     * @param closure a single parameter closure that returns a (typically) numeric value.
     * @return The average of the values returned by applying the closure to each
     *         item of the Iterable.
     * @since 3.0.0
     */
    public static <T> Object average(Iterable<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        return average(self.iterator(), closure);
    }

    /**
     * Averages the result of applying a closure to each item of an array.
     * <code>array.average(closure)</code> is equivalent to:
     * <code>array.collect(closure).average()</code>.
     * <pre class="groovyTestCase">
     * def (nums, strings) = [[1, 3] as Integer[], ['to', 'from'] as String[]]
     * assert 20 == nums.average { it * 10 }
     * assert 3 == strings.average { it.size() }
     * assert 3 == strings.average (String::size)
     * </pre>
     *
     * @param self    An array
     * @param closure a single parameter closure that returns a (typically) numeric value.
     * @return The average of the values returned by applying the closure to each
     *         item of the array.
     * @since 3.0.0
     */
    public static <T> Object average(T[] self, @ClosureParams(FirstParam.Component.class) Closure closure) {
        return average(new ArrayIterator<T>(self), closure);
    }

    /**
     * Averages the result of applying a closure to each item returned from an iterator.
     * <code>iter.average(closure)</code> is equivalent to:
     * <code>iter.collect(closure).average()</code>.
     * The iterator will become exhausted of elements after determining the average value.
     *
     * @param self    An Iterator
     * @param closure a single parameter closure that returns a (typically) numeric value.
     * @return The average of the values returned by applying the closure to each
     *         item from the Iterator.
     * @since 3.0.0
     */
    public static <T> Object average(Iterator<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        Object result = null;
        long count = 0;
        Object[] closureParam = new Object[1];
        Object[] plusParam = new Object[1];
        while (self.hasNext()) {
            closureParam[0] = self.next();
            plusParam[0] = closure.call(closureParam);
            if (count == 0) {
                result = plusParam[0];
            } else {
                MetaClass metaClass = InvokerHelper.getMetaClass(result);
                result = metaClass.invokeMethod(result, "plus", plusParam);
            }
            count++;
        }
        MetaClass metaClass = InvokerHelper.getMetaClass(result);
        result = metaClass.invokeMethod(result, "div", count);
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
        return join((Iterable)toList(self), separator);
    }

    /**
     * @deprecated Use the Iterable version of join instead
     * @see #join(Iterable, String)
     * @since 1.0
     */
    @Deprecated
    public static String join(Collection self, String separator) {
        return join((Iterable)self, separator);
    }

    /**
     * Concatenates the <code>toString()</code> representation of each
     * item in this Iterable, with the given String as a separator between each item.
     * <pre class="groovyTestCase">assert "1, 2, 3" == [1,2,3].join(", ")</pre>
     *
     * @param self      an Iterable of objects
     * @param separator a String separator
     * @return the joined String
     * @since 1.0
     */
    public static String join(Iterable self, String separator) {
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
        StringBuilder buffer = new StringBuilder();
        boolean first = true;

        if (separator == null) separator = "";

        for (boolean next : self) {
            if (first) {
                first = false;
            } else {
                buffer.append(separator);
            }
            buffer.append(next);
        }
        return buffer.toString();
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
        StringBuilder buffer = new StringBuilder();
        boolean first = true;

        if (separator == null) separator = "";

        for (byte next : self) {
            if (first) {
                first = false;
            } else {
                buffer.append(separator);
            }
            buffer.append(next);
        }
        return buffer.toString();
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
        StringBuilder buffer = new StringBuilder();
        boolean first = true;

        if (separator == null) separator = "";

        for (char next : self) {
            if (first) {
                first = false;
            } else {
                buffer.append(separator);
            }
            buffer.append(next);
        }
        return buffer.toString();
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
        StringBuilder buffer = new StringBuilder();
        boolean first = true;

        if (separator == null) separator = "";

        for (double next : self) {
            if (first) {
                first = false;
            } else {
                buffer.append(separator);
            }
            buffer.append(next);
        }
        return buffer.toString();
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
        StringBuilder buffer = new StringBuilder();
        boolean first = true;

        if (separator == null) separator = "";

        for (float next : self) {
            if (first) {
                first = false;
            } else {
                buffer.append(separator);
            }
            buffer.append(next);
        }
        return buffer.toString();
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
        StringBuilder buffer = new StringBuilder();
        boolean first = true;

        if (separator == null) separator = "";

        for (int next : self) {
            if (first) {
                first = false;
            } else {
                buffer.append(separator);
            }
            buffer.append(next);
        }
        return buffer.toString();
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
        StringBuilder buffer = new StringBuilder();
        boolean first = true;

        if (separator == null) separator = "";

        for (long next : self) {
            if (first) {
                first = false;
            } else {
                buffer.append(separator);
            }
            buffer.append(next);
        }
        return buffer.toString();
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
        StringBuilder buffer = new StringBuilder();
        boolean first = true;

        if (separator == null) separator = "";

        for (short next : self) {
            if (first) {
                first = false;
            } else {
                buffer.append(separator);
            }
            buffer.append(next);
        }
        return buffer.toString();
    }

    /**
     * @deprecated Use the Iterable version of min instead
     * @see #min(Iterable)
     * @since 1.0
     */
    @Deprecated
    public static <T> T min(Collection<T> self) {
        return GroovyCollections.min(self);
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
    public static <T> T min(Iterable<T> self) {
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
        return min((Iterable<T>)toList(self));
    }

    /**
     * Adds min() method to Object arrays.
     *
     * @param self an array
     * @return the minimum value
     * @see #min(java.util.Collection)
     * @since 1.5.5
     */
    public static <T> T min(T[] self) {
        return min((Iterable<T>)toList(self));
    }

    /**
     * @deprecated Use the Iterable version of min instead
     * @see #min(Iterable, Comparator)
     * @since 1.0
     */
    @Deprecated
    public static <T> T min(Collection<T> self, Comparator<T> comparator) {
        return min((Iterable<T>) self, comparator);
    }

    /**
     * Selects the minimum value found in the Iterable using the given comparator.
     * <pre class="groovyTestCase">assert "hi" == ["hello","hi","hey"].min( { a, b {@code ->} a.length() {@code <=>} b.length() } as Comparator )</pre>
     *
     * @param self       an Iterable
     * @param comparator a Comparator
     * @return the minimum value or null for an empty Iterable
     * @since 2.2.0
     */
    public static <T> T min(Iterable<T> self, Comparator<T> comparator) {
        T answer = null;
        boolean first = true;
        for (T value : self) {
            if (first) {
                first = false;
                answer = value;
            } else if (comparator.compare(value, answer) < 0) {
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
        return min((Iterable<T>)toList(self), comparator);
    }

    /**
     * Selects the minimum value found from the Object array using the given comparator.
     *
     * @param self       an array
     * @param comparator a Comparator
     * @return the minimum value
     * @see #min(java.util.Collection, java.util.Comparator)
     * @since 1.5.5
     */
    public static <T> T min(T[] self, Comparator<T> comparator) {
        return min((Iterable<T>)toList(self), comparator);
    }

    /**
     * @deprecated Use the Iterable version of min instead
     * @see #min(Iterable, Closure)
     * @since 1.0
     */
    @Deprecated
    public static <T> T min(Collection<T> self, Closure closure) {
        return min((Iterable<T>)self, closure);
    }

    /**
     * Selects the item in the iterable which when passed as a parameter to the supplied closure returns the
     * minimum value. A null return value represents the least possible return value. If more than one item
     * has the minimum value, an arbitrary choice is made between the items having the minimum value.
     * <p>
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
     * def lastDigit = { a, b {@code ->} a % 10 {@code <=>} b % 10 }
     * assert [19, 55, 91].min(lastDigit) == 91
     * </pre>
     * <pre class="groovyTestCase">
     * def pets = ['dog', 'cat', 'anaconda']
     * def shortestName = pets.min{ it.size() } // one of 'dog' or 'cat'
     * assert shortestName.size() == 3
     * </pre>
     *
     * @param self    an Iterable
     * @param closure a 1 or 2 arg Closure used to determine the correct ordering
     * @return an item from the Iterable having the minimum value returned by calling the supplied closure with that item as parameter or null for an empty Iterable
     * @since 1.0
     */
    public static <T> T min(Iterable<T> self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure closure) {
        int params = closure.getMaximumNumberOfParameters();
        if (params != 1) {
            return min(self, new ClosureComparator<T>(closure));
        }
        boolean first = true;
        T answer = null;
        Object answerValue = null;
        for (T item : self) {
            Object value = closure.call(item);
            if (first) {
                first = false;
                answer = item;
                answerValue = value;
            } else if (ScriptBytecodeAdapter.compareLessThan(value, answerValue)) {
                answer = item;
                answerValue = value;
            }
        }
        return answer;
    }

    /**
     * Selects an entry in the map having the minimum
     * calculated value as determined by the supplied closure.
     * If more than one entry has the minimum value,
     * an arbitrary choice is made between the entries having the minimum value.
     * <p>
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
     * def mostCommonEntry = zoo.min{ a, b {@code ->} b.value {@code <=>} a.value } // double negative!
     * assert mostCommonEntry.value == 7
     * </pre>
     * Edge case for multiple min values:
     * <pre class="groovyTestCase">
     * def zoo = [monkeys:6, lions:5, tigers:7]
     * def lastCharOfName = { e {@code ->} e.key[-1] }
     * def ans = zoo.min(lastCharOfName) // some random entry
     * assert lastCharOfName(ans) == 's'
     * </pre>
     *
     * @param self    a Map
     * @param closure a 1 or 2 arg Closure used to determine the correct ordering
     * @return the Map.Entry having the minimum value as determined by the closure
     * @since 1.7.6
     */
    public static <K, V> Map.Entry<K, V> min(Map<K, V> self, @ClosureParams(value=FromString.class, options={"Map.Entry<K,V>", "Map.Entry<K,V>,Map.Entry<K,V>"}) Closure closure) {
        return min((Iterable<Map.Entry<K, V>>)self.entrySet(), closure);
    }

    /**
     * Selects an entry in the map having the maximum
     * calculated value as determined by the supplied closure.
     * If more than one entry has the maximum value,
     * an arbitrary choice is made between the entries having the maximum value.
     * <p>
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
     * def leastCommonEntry = zoo.max{ a, b {@code ->} b.value {@code <=>} a.value } // double negative!
     * assert leastCommonEntry.value == 5
     * </pre>
     * Edge case for multiple max values:
     * <pre class="groovyTestCase">
     * def zoo = [monkeys:6, lions:5, tigers:7]
     * def lengthOfNamePlusNumber = { e {@code ->} e.key.size() + e.value }
     * def ans = zoo.max(lengthOfNamePlusNumber) // one of [monkeys:6, tigers:7]
     * assert lengthOfNamePlusNumber(ans) == 13
     * </pre>
     *
     * @param self    a Map
     * @param closure a 1 or 2 arg Closure used to determine the correct ordering
     * @return the Map.Entry having the maximum value as determined by the closure
     * @since 1.7.6
     */
    public static <K, V> Map.Entry<K, V> max(Map<K, V> self, @ClosureParams(value=FromString.class, options={"Map.Entry<K,V>", "Map.Entry<K,V>,Map.Entry<K,V>"}) Closure closure) {
        return max((Iterable<Map.Entry<K, V>>)self.entrySet(), closure);
    }

    /**
     * Selects the minimum value found from the Iterator
     * using the closure to determine the correct ordering.
     * The iterator will become
     * exhausted of elements after this operation.
     * <p>
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
    public static <T> T min(Iterator<T> self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure closure) {
        return min((Iterable<T>)toList(self), closure);
    }

    /**
     * Selects the minimum value found from the Object array
     * using the closure to determine the correct ordering.
     * <p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     *
     * @param self    an array
     * @param closure a Closure used to determine the correct ordering
     * @return the minimum value
     * @see #min(java.util.Collection, groovy.lang.Closure)
     * @since 1.5.5
     */
    public static <T> T min(T[] self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure closure) {
        return min((Iterable<T>)toList(self), closure);
    }

    /**
     * @deprecated Use the Iterable version of max instead
     * @see #max(Iterable)
     * @since 1.0
     */
    @Deprecated
    public static <T> T max(Collection<T> self) {
        return GroovyCollections.max((Iterable<T>)self);
    }

    /**
     * Adds max() method to Iterable objects.
     * <pre class="groovyTestCase">
     * assert 5 == [2,3,1,5,4].max()
     * </pre>
     *
     * @param self an Iterable
     * @return the maximum value
     * @see groovy.util.GroovyCollections#max(java.lang.Iterable)
     * @since 2.2.0
     */
    public static <T> T max(Iterable<T> self) {
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
        return max((Iterable<T>)toList(self));
    }

    /**
     * Adds max() method to Object arrays.
     *
     * @param self an array
     * @return the maximum value
     * @see #max(java.util.Collection)
     * @since 1.5.5
     */
    public static <T> T max(T[] self) {
        return max((Iterable<T>)toList(self));
    }

    /**
     * @deprecated Use the Iterable version of max instead
     * @see #max(Iterable, Closure)
     * @since 1.0
     */
    @Deprecated
    public static <T> T max(Collection<T> self, Closure closure) {
        return max((Iterable<T>) self, closure);
    }

    /**
     * Selects the item in the iterable which when passed as a parameter to the supplied closure returns the
     * maximum value. A null return value represents the least possible return value, so any item for which
     * the supplied closure returns null, won't be selected (unless all items return null). If more than one item
     * has the maximum value, an arbitrary choice is made between the items having the maximum value.
     * <p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     * <pre class="groovyTestCase">assert "hello" == ["hello","hi","hey"].max { it.length() }</pre>
     * <pre class="groovyTestCase">assert "hello" == ["hello","hi","hey"].max { a, b {@code ->} a.length() {@code <=>} b.length() }</pre>
     * <pre class="groovyTestCase">
     * def pets = ['dog', 'elephant', 'anaconda']
     * def longestName = pets.max{ it.size() } // one of 'elephant' or 'anaconda'
     * assert longestName.size() == 8
     * </pre>
     *
     * @param self    an Iterable
     * @param closure a 1 or 2 arg Closure used to determine the correct ordering
     * @return an item from the Iterable having the maximum value returned by calling the supplied closure with that item as parameter or null for an empty Iterable
     * @since 2.2.0
     */
    public static <T> T max(Iterable<T> self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure closure) {
        int params = closure.getMaximumNumberOfParameters();
        if (params != 1) {
            return max(self, new ClosureComparator<T>(closure));
        }
        boolean first = true;
        T answer = null;
        Object answerValue = null;
        for (T item : self) {
            Object value = closure.call(item);
            if (first) {
                first = false;
                answer = item;
                answerValue = value;
            } else if (ScriptBytecodeAdapter.compareLessThan(answerValue, value)) {
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
     * <p>
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
    public static <T> T max(Iterator<T> self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure closure) {
        return max((Iterable<T>)toList(self), closure);
    }

    /**
     * Selects the maximum value found from the Object array
     * using the closure to determine the correct ordering.
     * <p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     *
     * @param self    an array
     * @param closure a Closure used to determine the correct ordering
     * @return the maximum value
     * @see #max(java.util.Collection, groovy.lang.Closure)
     * @since 1.5.5
     */
    public static <T> T max(T[] self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure closure) {
        return max((Iterable<T>)toList(self), closure);
    }

    /**
     * @deprecated Use the Iterable version of max instead
     * @see #max(Iterable, Comparator)
     * @since 1.0
     */
    @Deprecated
    public static <T> T max(Collection<T> self, Comparator<T> comparator) {
        return max((Iterable<T>)self, comparator);
    }

    /**
     * Selects the maximum value found in the Iterable using the given comparator.
     * <pre class="groovyTestCase">
     * assert "hello" == ["hello","hi","hey"].max( { a, b {@code ->} a.length() {@code <=>} b.length() } as Comparator )
     * </pre>
     *
     * @param self       an Iterable
     * @param comparator a Comparator
     * @return the maximum value or null for an empty Iterable
     * @since 2.2.0
     */
    public static <T> T max(Iterable<T> self, Comparator<T> comparator) {
        T answer = null;
        boolean first = true;
        for (T value : self) {
            if (first) {
                first = false;
                answer = value;
            } else if (comparator.compare(value, answer) > 0) {
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
        return max((Iterable<T>)toList(self), comparator);
    }

    /**
     * Selects the maximum value found from the Object array using the given comparator.
     *
     * @param self       an array
     * @param comparator a Comparator
     * @return the maximum value
     * @since 1.5.5
     */
    public static <T> T max(T[] self, Comparator<T> comparator) {
        return max((Iterable<T>)toList(self), comparator);
    }

    /**
     * Returns indices of the collection.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert 0..2 == [5, 6, 7].indices
     * </pre>
     *
     * @param self a collection
     * @return an index range
     * @since 2.4.0
     */
    public static IntRange getIndices(Collection self) {
        return new IntRange(false, 0, self.size());
    }

    /**
     * Returns indices of the array.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * String[] letters = ['a', 'b', 'c', 'd']
     * {@code assert 0..<4 == letters.indices}
     * </pre>
     *
     * @param self an array
     * @return an index range
     * @since 2.4.0
     */
    public static <T> IntRange getIndices(T[] self) {
        return new IntRange(false, 0, self.length);
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
     * Provide the standard Groovy <code>size()</code> method for <code>Iterable</code>.
     * <pre class="groovyTestCase">
     * def items = [1, 2, 3]
     * def iterable = { [ hasNext:{ !items.isEmpty() }, next:{ items.pop() } ] as Iterator } as Iterable
     * assert iterable.size() == 3
     * </pre>
     *
     * @param self an Iterable
     * @return the length of the Iterable
     * @since 2.3.8
     */
    public static int size(Iterable self) {
        return size(self.iterator());
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
     * Check whether an <code>Iterable</code> has elements
     * <pre class="groovyTestCase">
     * def items = [1]
     * def iterable = { [ hasNext:{ !items.isEmpty() }, next:{ items.pop() } ] as Iterator } as Iterable
     * assert !iterable.isEmpty()
     * iterable.iterator().next()
     * assert iterable.isEmpty()
     * </pre>
     *
     * @param self an Iterable
     * @return true if the iterable has no elements, false otherwise
     * @since 2.5.0
     */
    public static boolean isEmpty(Iterable self) {
        return !self.iterator().hasNext();
    }


    /**
     * Support the range subscript operator for a List.
     * <pre class="groovyTestCase">def list = [1, "a", 4.5, true]
     * assert list[1..2] == ["a", 4.5]</pre>
     *
     * @param self  a List
     * @param range a Range indicating the items to get
     * @return a new list instance based on range borders
     *
     * @since 1.0
     */
    public static <T> List<T> getAt(List<T> self, Range range) {
        RangeInfo info = subListBorders(self.size(), range);

        List<T> subList = self.subList(info.from, info.to);
        if (info.reverse) {
            subList = reverse(subList);
        }

        // trying to guess the concrete list type and create a new instance from it
        List<T> answer = createSimilarList(self, subList.size());
        answer.addAll(subList);

        return answer;
    }


    /**
     * Select a List of items from an eager or lazy List using a Collection to
     * identify the indices to be selected.
     * <pre class="groovyTestCase">def list = [].withDefault { 42 }
     * assert list[1,0,2] == [42, 42, 42]</pre>
     *
     * @param self    a ListWithDefault
     * @param indices a Collection of indices
     *
     * @return a new eager or lazy list of the values at the given indices
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getAt(ListWithDefault<T> self, Collection indices) {
        List<T> answer = ListWithDefault.newInstance(new ArrayList<T>(indices.size()), self.isLazyDefaultValues(), self.getInitClosure());
        for (Object value : indices) {
            if (value instanceof Collection) {
                answer.addAll((List<T>) InvokerHelper.invokeMethod(self, "getAt", value));
            } else {
                int idx = normaliseIndex(DefaultTypeTransformation.intUnbox(value), self.size());
                answer.add(self.getAt(idx));
            }
        }
        return answer;
    }

    /**
     * Support the range subscript operator for an eager or lazy List.
     * <pre class="groovyTestCase">def list = [].withDefault { 42 }
     * assert list[1..2] == [null, 42]</pre>
     *
     * @param self  a ListWithDefault
     * @param range a Range indicating the items to get
     *
     * @return a new eager or lazy list instance based on range borders
     */
    public static <T> List<T> getAt(ListWithDefault<T> self, Range range) {
        RangeInfo info = subListBorders(self.size(), range);

        // if a positive index is accessed not initialized so far
        // initialization up to that index takes place
        if (self.size() < info.to) {
            self.get(info.to - 1);
        }

        List<T> answer = self.subList(info.from, info.to);
        if (info.reverse) {
            answer =  ListWithDefault.newInstance(reverse(answer), self.isLazyDefaultValues(), self.getInitClosure());
        } else {
            // instead of using the SubList backed by the parent list, a new ArrayList instance is used
            answer =  ListWithDefault.newInstance(new ArrayList<T>(answer), self.isLazyDefaultValues(), self.getInitClosure());
        }

        return answer;
    }

    /**
     * Support the range subscript operator for an eager or lazy List.
     * <pre class="groovyTestCase">
     * def list = [true, 1, 3.4].withDefault{ 42 }
     * {@code assert list[0..<0] == []}
     * </pre>
     *
     * @param self  a ListWithDefault
     * @param range a Range indicating the items to get
     *
     * @return a new list instance based on range borders
     *
     */
    public static <T> List<T> getAt(ListWithDefault<T> self, EmptyRange range) {
        return ListWithDefault.newInstance(new ArrayList<T>(), self.isLazyDefaultValues(), self.getInitClosure());
    }

    /**
     * Support the range subscript operator for a List.
     * <pre class="groovyTestCase">
     * def list = [true, 1, 3.4]
     * {@code assert list[0..<0] == []}
     * </pre>
     *
     * @param self  a List
     * @param range a Range indicating the items to get
     * @return a new list instance based on range borders
     *
     * @since 1.0
     */
    public static <T> List<T> getAt(List<T> self, EmptyRange range) {
        return createSimilarList(self, 0);
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
    @SuppressWarnings("unchecked")
    public static <T> List<T> getAt(List<T> self, Collection indices) {
        List<T> answer = new ArrayList<T>(indices.size());
        for (Object value : indices) {
            if (value instanceof Collection) {
                answer.addAll((List<T>)InvokerHelper.invokeMethod(self, "getAt", value));
            } else {
                int idx = DefaultTypeTransformation.intUnbox(value);
                answer.add(getAt(self, idx));
            }
        }
        return answer;
    }

    /**
     * Select a List of items from an array using a Collection to
     * identify the indices to be selected.
     *
     * @param self    an array
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
            if (map.containsKey(key)) {
                answer.put(key, map.get(key));
            }
        }
        return answer;
    }

    /**
     * Creates a sub-Map containing the given keys. This method is similar to
     * List.subList() but uses keys rather than index ranges. The original
     * map is unaltered.
     * <pre class="groovyTestCase">
     * def orig = [1:10, 2:20, 3:30, 4:40]
     * assert orig.subMap([1, 3] as int[]) == [1:10, 3:30]
     * assert orig.subMap([2, 4] as Integer[]) == [2:20, 4:40]
     * assert orig.size() == 4
     * </pre>
     *
     * @param map  a Map
     * @param keys an array of keys
     * @return a new Map containing the given keys
     * @since 2.1.0
     */
    public static <K, V> Map<K, V> subMap(Map<K, V> map, K[] keys) {
        Map<K, V> answer = new LinkedHashMap<K, V>(keys.length);
        for (K key : keys) {
            if (map.containsKey(key)) {
                answer.put(key, map.get(key));
            }
        }
        return answer;
    }

    /**
     * Looks up an item in a Map for the given key and returns the value - unless
     * there is no entry for the given key in which case add the default value
     * to the map and return that.
     * <pre class="groovyTestCase">def map=[:]
     * map.get("a", []) &lt;&lt; 5
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
     * Support subscript operator for list access.
     */
    public static <T> T getAt(List<T> self, Number idx) {
        return getAt(self, idx.intValue());
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
     * @param idx  an index value (-self.size() &lt;= idx &lt; self.size())
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
     * Support the subscript operator for an Iterable. Typical usage:
     * <pre class="groovyTestCase">
     * // custom Iterable example:
     * class MyIterable implements Iterable {
     *   Iterator iterator() { [1, 2, 3].iterator() }
     * }
     * def myIterable = new MyIterable()
     * assert myIterable[1] == 2
     *
     * // Set example:
     * def set = [1,2,3] as LinkedHashSet
     * assert set[1] == 2
     * </pre>
     *
     * @param self an Iterable
     * @param idx  an index value (-self.size() &lt;= idx &lt; self.size()) but using -ve index values will be inefficient
     * @return the value at the given index (after normalisation) or null if no corresponding value was found
     * @since 2.1.0
     */
    public static <T> T getAt(Iterable<T> self, int idx) {
        return getAt(self.iterator(), idx);
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
     * Support subscript operator for list modification.
     */
    public static <T> void putAt(List<T> self, Number idx, T value) {
        putAt(self, idx.intValue(), value);
    }

    /**
     * A helper method to allow lists to work with subscript operators.
     * <pre class="groovyTestCase">
     * def list = ["a", true]
     * {@code list[1..<1] = 5}
     * assert list == ["a", 5, true]
     * </pre>
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
     * <pre class="groovyTestCase">
     * def list = ["a", true]
     * {@code list[1..<1] = [4, 3, 2]}
     * assert list == ["a", 4, 3, 2, true]
     * </pre>
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

    // todo: remove after putAt(Splice) gets deleted
    @Deprecated
    protected static List getSubList(List self, List splice) {
        int left /* = 0 */;
        int right = 0;
        boolean emptyRange = false;
        if (splice.size() == 2) {
            left = DefaultTypeTransformation.intUnbox(splice.get(0));
            right = DefaultTypeTransformation.intUnbox(splice.get(1));
        } else if (splice instanceof IntRange) {
            IntRange range = (IntRange) splice;
            left = range.getFrom();
            right = range.getTo();
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
     * <p>
     * Roughly equivalent to <code>Map m = new HashMap(); m.putAll(left); m.putAll(right); return m;</code>
     * but with some additional logic to preserve the <code>left</code> Map type for common cases as
     * described above.
     * <pre class="groovyTestCase">
     * assert [a:10, b:20] + [a:5, c:7] == [a:5, b:20, c:7]
     * </pre>
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
     * <pre class="groovyTestCase">
     * assert [String, Long, Integer] == ["a",5L,2]["class"]
     * </pre>
     *
     * @param coll     a Collection
     * @param property a String
     * @return a List
     * @since 1.0
     */
    public static List getAt(Collection coll, String property) {
        List<Object> answer = new ArrayList<Object>(coll.size());
        return getAtIterable(coll, property, answer);
    }

    private static List getAtIterable(Iterable coll, String property, List<Object> answer) {
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
     * A convenience method for creating an immutable Map.
     *
     * @param self a Map
     * @return an unmodifiable view of a copy of the original, i.e. an effectively immutable copy
     * @see #asImmutable(java.util.List)
     * @see #asUnmodifiable(java.util.Map)
     * @since 1.0
     */
    public static <K, V> Map<K, V> asImmutable(Map<K, V> self) {
        return asUnmodifiable(new LinkedHashMap<K, V>(self));
    }

    /**
     * A convenience method for creating an immutable SortedMap.
     *
     * @param self a SortedMap
     * @return an unmodifiable view of a copy of the original, i.e. an effectively immutable copy
     * @see #asImmutable(java.util.List)
     * @see #asUnmodifiable(java.util.SortedMap)
     * @since 1.0
     */
    public static <K, V> SortedMap<K, V> asImmutable(SortedMap<K, V> self) {
        return asUnmodifiable(new TreeMap<K, V>(self));
    }

    /**
     * A convenience method for creating an immutable List.
     * <pre class="groovyTestCase">
     * def mutable = [1,2,3]
     * def immutable = mutable.asImmutable()
     * try {
     *     immutable &lt;&lt; 4
     *     assert false
     * } catch (UnsupportedOperationException) {
     *     assert true
     * }
     * mutable &lt;&lt; 4
     * assert mutable.size() == 4
     * assert immutable.size() == 3
     * </pre>
     *
     * @param self a List
     * @return an unmodifiable view of a copy of the original, i.e. an effectively immutable copy
     * @see #asUnmodifiable(java.util.List)
     * @since 1.0
     */
    public static <T> List<T> asImmutable(List<T> self) {
        return asUnmodifiable(new ArrayList<T>(self));
    }

    /**
     * A convenience method for creating an immutable Set.
     *
     * @param self a Set
     * @return an unmodifiable view of a copy of the original, i.e. an effectively immutable copy
     * @see #asImmutable(java.util.List)
     * @see #asUnmodifiable(java.util.Set)
     * @since 1.0
     */
    public static <T> Set<T> asImmutable(Set<T> self) {
        return asUnmodifiable(new LinkedHashSet<T>(self));
    }

    /**
     * A convenience method for creating an immutable SortedSet.
     *
     * @param self a SortedSet
     * @return an unmodifiable view of a copy of the original, i.e. an effectively immutable copy
     * @see #asImmutable(java.util.List)
     * @see #asUnmodifiable(java.util.SortedSet)
     * @since 1.0
     */
    public static <T> SortedSet<T> asImmutable(SortedSet<T> self) {
        return asUnmodifiable(new TreeSet<T>(self));
    }

    /**
     * A convenience method for creating an immutable Collection.
     *
     * @param self a Collection
     * @return an unmodifiable view of a copy of the original, i.e. an effectively immutable copy
     * @see #asImmutable(java.util.List)
     * @see #asUnmodifiable(java.util.Collection)
     * @since 1.5.0
     */
    public static <T> Collection<T> asImmutable(Collection<T> self) {
        return asUnmodifiable((Collection<T>) new ArrayList<T>(self));
    }

    /**
     * Creates an unmodifiable view of a Map.
     *
     * @param self a Map
     * @return an unmodifiable view of the Map
     * @see java.util.Collections#unmodifiableMap(java.util.Map)
     * @see #asUnmodifiable(java.util.List)
     * @since 2.5.0
     */
    public static <K, V> Map<K, V> asUnmodifiable(Map<K, V> self) {
        return Collections.unmodifiableMap(self);
    }

    /**
     * Creates an unmodifiable view of a SortedMap.
     *
     * @param self a SortedMap
     * @return an unmodifiable view of the SortedMap
     * @see java.util.Collections#unmodifiableSortedMap(java.util.SortedMap)
     * @see #asUnmodifiable(java.util.List)
     * @since 2.5.0
     */
    public static <K, V> SortedMap<K, V> asUnmodifiable(SortedMap<K, V> self) {
        return Collections.unmodifiableSortedMap(self);
    }

    /**
     * Creates an unmodifiable view of a List.
     * <pre class="groovyTestCase">
     * def mutable = [1,2,3]
     * def unmodifiable = mutable.asUnmodifiable()
     * try {
     *     unmodifiable &lt;&lt; 4
     *     assert false
     * } catch (UnsupportedOperationException) {
     *     assert true
     * }
     * mutable &lt;&lt; 4
     * assert unmodifiable.size() == 4
     * </pre>
     *
     * @param self a List
     * @return an unmodifiable view of the List
     * @see java.util.Collections#unmodifiableList(java.util.List)
     * @since 2.5.0
     */
    public static <T> List<T> asUnmodifiable(List<T> self) {
        return Collections.unmodifiableList(self);
    }

    /**
     * Creates an unmodifiable view of a Set.
     *
     * @param self a Set
     * @return an unmodifiable view of the Set
     * @see java.util.Collections#unmodifiableSet(java.util.Set)
     * @see #asUnmodifiable(java.util.List)
     * @since 2.5.0
     */
    public static <T> Set<T> asUnmodifiable(Set<T> self) {
        return Collections.unmodifiableSet(self);
    }

    /**
     * Creates an unmodifiable view of a SortedSet.
     *
     * @param self a SortedSet
     * @return an unmodifiable view of the SortedSet
     * @see java.util.Collections#unmodifiableSortedSet(java.util.SortedSet)
     * @see #asUnmodifiable(java.util.List)
     * @since 2.5.0
     */
    public static <T> SortedSet<T> asUnmodifiable(SortedSet<T> self) {
        return Collections.unmodifiableSortedSet(self);
    }

    /**
     * Creates an unmodifiable view of a Collection.
     *
     * @param self a Collection
     * @return an unmodifiable view of the Collection
     * @see java.util.Collections#unmodifiableCollection(java.util.Collection)
     * @see #asUnmodifiable(java.util.List)
     * @since 2.5.0
     */
    public static <T> Collection<T> asUnmodifiable(Collection<T> self) {
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
     * <p>
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
     * <p>
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
     * <p>
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
     * Creates a spreadable map from this iterable.
     * <p>
     * @param self an iterable
     * @return a newly created SpreadMap
     * @see groovy.lang.SpreadMap#SpreadMap(java.util.List)
     * @see #toSpreadMap(java.util.Map)
     * @since 2.4.0
     */
    public static SpreadMap toSpreadMap(Iterable self) {
        if (self == null)
            throw new GroovyRuntimeException("Fail to convert Iterable to SpreadMap, because it is null.");
        else
            return toSpreadMap(asList(self));
    }

    /**
     * Wraps a map using the decorator pattern with a wrapper that intercepts all calls
     * to <code>get(key)</code>. If an unknown key is found, a default value will be
     * stored into the Map before being returned. The default value stored will be the
     * result of calling the supplied Closure with the key as the parameter to the Closure.
     * Example usage:
     * <pre class="groovyTestCase">
     * def map = [a:1, b:2].withDefault{ k {@code ->} k.toCharacter().isLowerCase() ? 10 : -10 }
     * def expected = [a:1, b:2, c:10, D:-10]
     * assert expected.every{ e {@code ->} e.value == map[e.key] }
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
    public static <K, V> Map<K, V> withDefault(Map<K, V> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<V> init) {
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
    public static <T> ListWithDefault<T> withDefault(List<T> self, @ClosureParams(value=SimpleType.class, options = "int") Closure<T> init) {
        return withLazyDefault(self, init);
    }

    @Deprecated
    public static <T> List<T> withDefault$$bridge(List<T> self, @ClosureParams(value=SimpleType.class, options = "int") Closure<T> init) {
        return withDefault(self, init);
    }

    /**
     * Decorates a list allowing it to grow when called with a non-existent index value.
     * When called with such values, the list is grown in size and a default value
     * is placed in the list by calling a supplied <code>init</code> Closure. Subsequent
     * retrieval operations if finding a null value in the list assume it was set
     * as null from an earlier growing operation and again call the <code>init</code> Closure
     * to populate the retrieved value; consequently the list can't be used to store null values.
     * <p>
     * How it works: The decorated list intercepts all calls
     * to <code>getAt(index)</code> and <code>get(index)</code>. If an index greater than
     * or equal to the current <code>size()</code> is used, the list will grow automatically
     * up to the specified index. Gaps will be filled by {@code null}. If a default value
     * should also be used to fill gaps instead of {@code null}, use <code>withEagerDefault</code>.
     * If <code>getAt(index)</code> or <code>get(index)</code> are called and a null value
     * is found, it is assumed that the null value was a consequence of an earlier grow list
     * operation and the <code>init</code> Closure is called to populate the value.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * def list = [0, 1].withLazyDefault{ 42 }
     * assert list[0] == 0
     * assert list[1] == 1
     * assert list[3] == 42   // default value
     * assert list == [0, 1, null, 42] // gap filled with null
     *
     * // illustrate using the index when generating default values
     * def list2 = [5].withLazyDefault{ index {@code ->} index * index }
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
    public static <T> ListWithDefault<T> withLazyDefault(List<T> self, @ClosureParams(value=SimpleType.class, options="int") Closure<T> init) {
        return ListWithDefault.newInstance(self, true, init);
    }

    @Deprecated
    public static <T> List<T> withLazyDefault$$bridge(List<T> self, @ClosureParams(value=SimpleType.class, options = "int") Closure<T> init) {
        return ListWithDefault.newInstance(self, true, init);
    }

    /**
     * Decorates a list allowing it to grow when called with a non-existent index value.
     * When called with such values, the list is grown in size and a default value
     * is placed in the list by calling a supplied <code>init</code> Closure. Null values
     * can be stored in the list.
     * <p>
     * How it works: The decorated list intercepts all calls
     * to <code>getAt(index)</code> and <code>get(index)</code>. If an index greater than
     * or equal to the current <code>size()</code> is used, the list will grow automatically
     * up to the specified index. Gaps will be filled by calling the <code>init</code> Closure.
     * If generating a default value is a costly operation consider using <code>withLazyDefault</code>.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * def list = [0, 1].withEagerDefault{ 42 }
     * assert list[0] == 0
     * assert list[1] == 1
     * assert list[3] == 42   // default value
     * assert list == [0, 1, 42, 42]   // gap filled with default value
     *
     * // illustrate using the index when generating default values
     * def list2 = [5].withEagerDefault{ index {@code ->} index * index }
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
    public static <T> ListWithDefault<T> withEagerDefault(List<T> self, @ClosureParams(value=SimpleType.class, options="int") Closure<T> init) {
        return ListWithDefault.newInstance(self, false, init);
    }

    @Deprecated
    public static <T> List<T> withEagerDefault$$bridge(List<T> self, @ClosureParams(value=SimpleType.class, options = "int") Closure<T> init) {
        return ListWithDefault.newInstance(self, false, init);
    }

    /**
     * Zips an Iterable with indices in (value, index) order.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * assert [["a", 0], ["b", 1]] == ["a", "b"].withIndex()
     * assert ["0: a", "1: b"] == ["a", "b"].withIndex().collect { str, idx {@code ->} "$idx: $str" }
     * </pre>
     *
     * @param self an Iterable
     * @return a zipped list with indices
     * @see #indexed(Iterable)
     * @since 2.4.0
     */
    public static <E> List<Tuple2<E, Integer>> withIndex(Iterable<E> self) {
        return withIndex(self, 0);
    }

    /**
     * Zips an Iterable with indices in (index, value) order.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * assert [0: "a", 1: "b"] == ["a", "b"].indexed()
     * assert ["0: a", "1: b"] == ["a", "b"].indexed().collect { idx, str {@code ->} "$idx: $str" }
     * </pre>
     *
     * @param self an Iterable
     * @return a zipped map with indices
     * @see #withIndex(Iterable)
     * @since 2.4.0
     */
    public static <E> Map<Integer, E> indexed(Iterable<E> self) {
        return indexed(self, 0);
    }

    /**
     * Zips an Iterable with indices in (value, index) order.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * assert [["a", 5], ["b", 6]] == ["a", "b"].withIndex(5)
     * assert ["1: a", "2: b"] == ["a", "b"].withIndex(1).collect { str, idx {@code ->} "$idx: $str" }
     * </pre>
     *
     * @param self   an Iterable
     * @param offset an index to start from
     * @return a zipped list with indices
     * @see #indexed(Iterable, int)
     * @since 2.4.0
     */
    public static <E> List<Tuple2<E, Integer>> withIndex(Iterable<E> self, int offset) {
        return toList(withIndex(self.iterator(), offset));
    }

    /**
     * Zips an Iterable with indices in (index, value) order.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * assert [5: "a", 6: "b"] == ["a", "b"].indexed(5)
     * assert ["1: a", "2: b"] == ["a", "b"].indexed(1).collect { idx, str {@code ->} "$idx: $str" }
     * </pre>
     *
     * @param self   an Iterable
     * @param offset an index to start from
     * @return a Map (since the keys/indices are unique) containing the elements from the iterable zipped with indices
     * @see #withIndex(Iterable, int)
     * @since 2.4.0
     */
    public static <E> Map<Integer, E> indexed(Iterable<E> self, int offset) {
        Map<Integer, E> result = new LinkedHashMap<Integer, E>();
        Iterator<Tuple2<Integer, E>> indexed = indexed(self.iterator(), offset);
        while (indexed.hasNext()) {
            Tuple2<Integer, E> next = indexed.next();
            result.put(next.getV1(), next.getV2());
        }
        return result;
    }

    /**
     * Zips an iterator with indices in (value, index) order.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * assert [["a", 0], ["b", 1]] == ["a", "b"].iterator().withIndex().toList()
     * assert ["0: a", "1: b"] == ["a", "b"].iterator().withIndex().collect { str, idx {@code ->} "$idx: $str" }.toList()
     * </pre>
     *
     * @param self an iterator
     * @return a zipped iterator with indices
     * @see #indexed(Iterator)
     * @since 2.4.0
     */
    public static <E> Iterator<Tuple2<E, Integer>> withIndex(Iterator<E> self) {
        return withIndex(self, 0);
    }

    /**
     * Zips an iterator with indices in (index, value) order.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * assert [[0, "a"], [1, "b"]] == ["a", "b"].iterator().indexed().collect{ tuple {@code ->} [tuple.first, tuple.second] }
     * assert ["0: a", "1: b"] == ["a", "b"].iterator().indexed().collect { idx, str {@code ->} "$idx: $str" }.toList()
     * </pre>
     *
     * @param self an iterator
     * @return a zipped iterator with indices
     * @see #withIndex(Iterator)
     * @since 2.4.0
     */
    public static <E> Iterator<Tuple2<Integer, E>> indexed(Iterator<E> self) {
        return indexed(self, 0);
    }

    /**
     * Zips an iterator with indices in (value, index) order.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * assert [["a", 5], ["b", 6]] == ["a", "b"].iterator().withIndex(5).toList()
     * assert ["1: a", "2: b"] == ["a", "b"].iterator().withIndex(1).collect { str, idx {@code ->} "$idx: $str" }.toList()
     * </pre>
     *
     * @param self   an iterator
     * @param offset an index to start from
     * @return a zipped iterator with indices
     * @see #indexed(Iterator, int)
     * @since 2.4.0
     */
    public static <E> Iterator<Tuple2<E, Integer>> withIndex(Iterator<E> self, int offset) {
        return new ZipPostIterator<E>(self, offset);
    }

    /**
     * Zips an iterator with indices in (index, value) order.
     * <p/>
     * Example usage:
     * <pre class="groovyTestCase">
     * assert [[5, "a"], [6, "b"]] == ["a", "b"].iterator().indexed(5).toList()
     * assert ["a: 1", "b: 2"] == ["a", "b"].iterator().indexed(1).collect { idx, str {@code ->} "$str: $idx" }.toList()
     * </pre>
     *
     * @param self   an iterator
     * @param offset an index to start from
     * @return a zipped iterator with indices
     * @see #withIndex(Iterator, int)
     * @since 2.4.0
     */
    public static <E> Iterator<Tuple2<Integer, E>> indexed(Iterator<E> self, int offset) {
        return new ZipPreIterator<E>(self, offset);
    }

    private static final class ZipPostIterator<E> implements Iterator<Tuple2<E, Integer>> {
        private final Iterator<E> delegate;
        private int index;

        private ZipPostIterator(Iterator<E> delegate, int offset) {
            this.delegate = delegate;
            this.index = offset;
        }

        public boolean hasNext() {
            return delegate.hasNext();
        }

        public Tuple2<E, Integer> next() {
            if (!hasNext()) throw new NoSuchElementException();
            return new Tuple2<E, Integer>(delegate.next(), index++);
        }

        public void remove() {
            delegate.remove();
        }
    }

    private static final class ZipPreIterator<E> implements Iterator<Tuple2<Integer, E>> {
        private final Iterator<E> delegate;
        private int index;

        private ZipPreIterator(Iterator<E> delegate, int offset) {
            this.delegate = delegate;
            this.index = offset;
        }

        public boolean hasNext() {
            return delegate.hasNext();
        }

        public Tuple2<Integer, E> next() {
            if (!hasNext()) throw new NoSuchElementException();
            return new Tuple2<Integer, E>(index++, delegate.next());
        }

        public void remove() {
            delegate.remove();
        }
    }

    /**
     * @deprecated Use the Iterable version of sort instead
     * @see #sort(Iterable,boolean)
     * @since 1.0
     */
    @Deprecated
    public static <T> List<T> sort(Collection<T> self) {
        return sort((Iterable<T>) self, true);
    }

    /**
     * Sorts the Collection. Assumes that the collection items are comparable
     * and uses their natural ordering to determine the resulting order.
     * If the Collection is a List, it is sorted in place and returned.
     * Otherwise, the elements are first placed into a new list which is then
     * sorted and returned - leaving the original Collection unchanged.
     * <pre class="groovyTestCase">assert [1,2,3] == [3,1,2].sort()</pre>
     *
     * @param self the Iterable to be sorted
     * @return the sorted Iterable as a List
     * @see #sort(Collection, boolean)
     * @since 2.2.0
     */
    public static <T> List<T> sort(Iterable<T> self) {
        return sort(self, true);
    }

    /**
     * @deprecated Use the Iterable version of sort instead
     * @see #sort(Iterable, boolean)
     * @since 1.8.1
     */
    @Deprecated
    public static <T> List<T> sort(Collection<T> self, boolean mutate) {
        return sort((Iterable<T>) self, mutate);
    }

    /**
     * Sorts the Iterable. Assumes that the Iterable items are
     * comparable and uses their natural ordering to determine the resulting order.
     * If the Iterable is a List and mutate is true,
     * it is sorted in place and returned. Otherwise, the elements are first placed
     * into a new list which is then sorted and returned - leaving the original Iterable unchanged.
     * <pre class="groovyTestCase">assert [1,2,3] == [3,1,2].sort()</pre>
     * <pre class="groovyTestCase">
     * def orig = [1, 3, 2]
     * def sorted = orig.sort(false)
     * assert orig == [1, 3, 2]
     * assert sorted == [1, 2, 3]
     * </pre>
     *
     * @param self   the iterable to be sorted
     * @param mutate false will always cause a new list to be created, true will mutate lists in place
     * @return the sorted iterable as a List
     * @since 2.2.0
     */
    public static <T> List<T> sort(Iterable<T> self, boolean mutate) {
        List<T> answer = mutate ? asList(self) : toList(self);
        answer.sort(new NumberAwareComparator<T>());
        return answer;
    }

    /**
     * Sorts the elements from the given map into a new ordered map using
     * the closure as a comparator to determine the ordering.
     * The original map is unchanged.
     * <pre class="groovyTestCase">def map = [a:5, b:3, c:6, d:4].sort { a, b {@code ->} a.value {@code <=>} b.value }
     * assert map == [b:3, d:4, a:5, c:6]</pre>
     *
     * @param self the original unsorted map
     * @param closure a Closure used as a comparator
     * @return the sorted map
     * @since 1.6.0
     */
    public static <K, V> Map<K, V> sort(Map<K, V> self, @ClosureParams(value=FromString.class, options={"Map.Entry<K,V>","Map.Entry<K,V>,Map.Entry<K,V>"}) Closure closure) {
        Map<K, V> result = new LinkedHashMap<K, V>();
        List<Map.Entry<K, V>> entries = asList((Iterable<Map.Entry<K, V>>) self.entrySet());
        sort((Iterable<Map.Entry<K, V>>) entries, closure);
        for (Map.Entry<K, V> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Sorts the elements from the given map into a new ordered Map using
     * the specified key comparator to determine the ordering.
     * The original map is unchanged.
     * <pre class="groovyTestCase">def map = [ba:3, cz:6, ab:5].sort({ a, b {@code ->} a[-1] {@code <=>} b[-1] } as Comparator)
     * assert map*.value == [3, 5, 6]</pre>
     *
     * @param self the original unsorted map
     * @param comparator a Comparator
     * @return the sorted map
     * @since 1.7.2
     */
    public static <K, V> Map<K, V> sort(Map<K, V> self, Comparator<? super K> comparator) {
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
        return sort((Iterable<T>) toList(self)).listIterator();
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
    public static <T> Iterator<T> sort(Iterator<T> self, Comparator<? super T> comparator) {
        return sort(toList(self), true, comparator).listIterator();
    }

    /**
     * @deprecated Use the Iterable version of sort instead
     * @see #sort(Iterable, boolean, Comparator)
     * @since 1.0
     */
    @Deprecated
    public static <T> List<T> sort(Collection<T> self, Comparator<T> comparator) {
        return sort((Iterable<T>) self, true, comparator);
    }

    /**
     * @deprecated Use the Iterable version of sort instead
     * @see #sort(Iterable, boolean, Comparator)
     * @since 1.8.1
     */
    @Deprecated
    public static <T> List<T> sort(Collection<T> self, boolean mutate, Comparator<T> comparator) {
        return sort((Iterable<T>) self, mutate, comparator);
    }

    /**
     * Sorts the Iterable using the given Comparator. If the Iterable is a List and mutate
     * is true, it is sorted in place and returned. Otherwise, the elements are first placed
     * into a new list which is then sorted and returned - leaving the original Iterable unchanged.
     * <pre class="groovyTestCase">
     * assert ["hi","hey","hello"] == ["hello","hi","hey"].sort(false, { a, b {@code ->} a.length() {@code <=>} b.length() } as Comparator )
     * </pre>
     * <pre class="groovyTestCase">
     * def orig = ["hello","hi","Hey"]
     * def sorted = orig.sort(false, String.CASE_INSENSITIVE_ORDER)
     * assert orig == ["hello","hi","Hey"]
     * assert sorted == ["hello","Hey","hi"]
     * </pre>
     *
     * @param self       the Iterable to be sorted
     * @param mutate     false will always cause a new list to be created, true will mutate lists in place
     * @param comparator a Comparator used for the comparison
     * @return a sorted List
     * @since 2.2.0
     */
    public static <T> List<T> sort(Iterable<T> self, boolean mutate, Comparator<? super T> comparator) {
        List<T> list = mutate ? asList(self) : toList(self);
        list.sort(comparator);
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
    public static <T> T[] sort(T[] self, Comparator<? super T> comparator) {
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
    public static <T> T[] sort(T[] self, boolean mutate, Comparator<? super T> comparator) {
        T[] answer = mutate ? self : self.clone();
        Arrays.sort(answer, comparator);
        return answer;
    }

    /**
     * Sorts the given iterator items into a sorted iterator using the Closure to determine the correct ordering.
     * The original iterator will be fully processed after the method call.
     * <p>
     * If the closure has two parameters it is used like a traditional Comparator.
     * I.e.&#160;it should compare its two parameters for order, returning a negative integer,
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
    public static <T> Iterator<T> sort(Iterator<T> self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure closure) {
        return sort((Iterable<T>) toList(self), closure).listIterator();
    }

    /**
     * Sorts the elements from this array into a newly created array using
     * the Closure to determine the correct ordering.
     * <p>
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
    public static <T> T[] sort(T[] self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure closure) {
        return sort(self, false, closure);
    }

    /**
     * Modifies this array so that its elements are in sorted order using the Closure to determine the correct ordering.
     * If mutate is false, a new array is returned and the original array remains unchanged.
     * Otherwise, the original array is sorted in place and returned.
     * <p>
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
    public static <T> T[] sort(T[] self, boolean mutate, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure closure) {
        T[] answer = (T[]) sort((Iterable<T>) toList(self), closure).toArray();
        if (mutate) {
            System.arraycopy(answer, 0, self, 0, answer.length);
        }
        return mutate ? self : answer;
    }

    /**
     * @deprecated Use the Iterable version of sort instead
     * @see #sort(Iterable, boolean, Closure)
     * @since 1.8.1
     */
    @Deprecated
    public static <T> List<T> sort(Collection<T> self, boolean mutate, Closure closure) {
        return sort((Iterable<T>)self, mutate, closure);
    }

    /**
     * @deprecated Use the Iterable version of sort instead
     * @see #sort(Iterable, Closure)
     * @since 1.0
     */
    @Deprecated
    public static <T> List<T> sort(Collection<T> self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure closure) {
        return sort((Iterable<T>)self, closure);
    }

    /**
     * Sorts this Iterable using the given Closure to determine the correct ordering. If the Iterable is a List,
     * it is sorted in place and returned. Otherwise, the elements are first placed
     * into a new list which is then sorted and returned - leaving the original Iterable unchanged.
     * <p>
     * If the Closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     * <pre class="groovyTestCase">assert ["hi","hey","hello"] == ["hello","hi","hey"].sort { it.length() }</pre>
     * <pre class="groovyTestCase">assert ["hi","hey","hello"] == ["hello","hi","hey"].sort { a, b {@code ->} a.length() {@code <=>} b.length() }</pre>
     *
     * @param self    the Iterable to be sorted
     * @param closure a 1 or 2 arg Closure used to determine the correct ordering
     * @return a newly created sorted List
     * @see #sort(Collection, boolean, Closure)
     * @since 2.2.0
     */
    public static <T> List<T> sort(Iterable<T> self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure closure) {
        return sort(self, true, closure);
    }

    /**
     * Sorts this Iterable using the given Closure to determine the correct ordering. If the Iterable is a List
     * and mutate is true, it is sorted in place and returned. Otherwise, the elements are first placed
     * into a new list which is then sorted and returned - leaving the original Iterable unchanged.
     * <p>
     * If the closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     * <pre class="groovyTestCase">assert ["hi","hey","hello"] == ["hello","hi","hey"].sort { it.length() }</pre>
     * <pre class="groovyTestCase">assert ["hi","hey","hello"] == ["hello","hi","hey"].sort { a, b {@code ->} a.length() {@code <=>} b.length() }</pre>
     * <pre class="groovyTestCase">
     * def orig = ["hello","hi","Hey"]
     * def sorted = orig.sort(false) { it.toUpperCase() }
     * assert orig == ["hello","hi","Hey"]
     * assert sorted == ["hello","Hey","hi"]
     * </pre>
     *
     * @param self    the Iterable to be sorted
     * @param mutate  false will always cause a new list to be created, true will mutate lists in place
     * @param closure a 1 or 2 arg Closure used to determine the correct ordering
     * @return a newly created sorted List
     * @since 2.2.0
     */
    public static <T> List<T> sort(Iterable<T> self, boolean mutate, @ClosureParams(value=FromString.class, options={"T","T,T"})  Closure closure) {
        List<T> list = mutate ? asList(self) : toList(self);
        // use a comparator of one item or two
        int params = closure.getMaximumNumberOfParameters();
        if (params == 1) {
            list.sort(new OrderBy<T>(closure));
        } else {
            list.sort(new ClosureComparator<T>(closure));
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
     * Sorts the Iterable. Assumes that the Iterable elements are
     * comparable and uses a {@link NumberAwareComparator} to determine the resulting order.
     * {@code NumberAwareComparator} has special treatment for numbers but otherwise uses the
     * natural ordering of the Iterable elements. The elements are first placed into a new list which
     * is then sorted and returned - leaving the original Iterable unchanged.
     * <pre class="groovyTestCase">
     * def orig = [1, 3, 2]
     * def sorted = orig.toSorted()
     * assert orig == [1, 3, 2]
     * assert sorted == [1, 2, 3]
     * </pre>
     *
     * @param self   the Iterable to be sorted
     * @return the sorted iterable as a List
     * @see #toSorted(Iterable, Comparator)
     * @since 2.4.0
     */
    public static <T> List<T> toSorted(Iterable<T> self) {
        return toSorted(self, new NumberAwareComparator<T>());
    }

    /**
     * Sorts the Iterable using the given Comparator. The elements are first placed
     * into a new list which is then sorted and returned - leaving the original Iterable unchanged.
     * <pre class="groovyTestCase">
     * def orig = ["hello","hi","Hey"]
     * def sorted = orig.toSorted(String.CASE_INSENSITIVE_ORDER)
     * assert orig == ["hello","hi","Hey"]
     * assert sorted == ["hello","Hey","hi"]
     * </pre>
     *
     * @param self       the Iterable to be sorted
     * @param comparator a Comparator used for the comparison
     * @return a sorted List
     * @since 2.4.0
     */
    public static <T> List<T> toSorted(Iterable<T> self, Comparator<T> comparator) {
        List<T> list = toList(self);
        list.sort(comparator);
        return list;
    }

    /**
     * Sorts this Iterable using the given Closure to determine the correct ordering. The elements are first placed
     * into a new list which is then sorted and returned - leaving the original Iterable unchanged.
     * <p>
     * If the Closure has two parameters
     * it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than,
     * equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a
     * Comparable (typically an Integer) which is then used for
     * further comparison.
     * <pre class="groovyTestCase">assert ["hi","hey","hello"] == ["hello","hi","hey"].sort { it.length() }</pre>
     * <pre class="groovyTestCase">assert ["hi","hey","hello"] == ["hello","hi","hey"].sort { a, b {@code ->} a.length() {@code <=>} b.length() }</pre>
     *
     * @param self    the Iterable to be sorted
     * @param closure a 1 or 2 arg Closure used to determine the correct ordering
     * @return a newly created sorted List
     * @see #toSorted(Iterable, Comparator)
     * @since 2.4.0
     */
    public static <T> List<T> toSorted(Iterable<T> self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure closure) {
        Comparator<T> comparator = (closure.getMaximumNumberOfParameters() == 1) ? new OrderBy<T>(closure) : new ClosureComparator<T>(closure);
        return toSorted(self, comparator);
    }

    /**
     * Sorts the Iterator. Assumes that the Iterator elements are
     * comparable and uses a {@link NumberAwareComparator} to determine the resulting order.
     * {@code NumberAwareComparator} has special treatment for numbers but otherwise uses the
     * natural ordering of the Iterator elements.
     * A new iterator is produced that traverses the items in sorted order.
     *
     * @param self       the Iterator to be sorted
     * @return the sorted items as an Iterator
     * @see #toSorted(Iterator, Comparator)
     * @since 2.4.0
     */
    public static <T> Iterator<T> toSorted(Iterator<T> self) {
        return toSorted(self, new NumberAwareComparator<T>());
    }

    /**
     * Sorts the given iterator items using the comparator. The
     * original iterator will become exhausted of elements after completing this method call.
     * A new iterator is produced that traverses the items in sorted order.
     *
     * @param self       the Iterator to be sorted
     * @param comparator a Comparator used for comparing items
     * @return the sorted items as an Iterator
     * @since 2.4.0
     */
    public static <T> Iterator<T> toSorted(Iterator<T> self, Comparator<T> comparator) {
        return toSorted(toList(self), comparator).listIterator();
    }

    /**
     * Sorts the given iterator items into a sorted iterator using the Closure to determine the correct ordering.
     * The original iterator will be fully processed after the method call.
     * <p>
     * If the closure has two parameters it is used like a traditional Comparator.
     * I.e.&#160;it should compare its two parameters for order, returning a negative integer,
     * zero, or a positive integer when the first parameter is less than, equal to,
     * or greater than the second respectively. Otherwise, the Closure is assumed
     * to take a single parameter and return a Comparable (typically an Integer)
     * which is then used for further comparison.
     *
     * @param self    the Iterator to be sorted
     * @param closure a Closure used to determine the correct ordering
     * @return the sorted items as an Iterator
     * @see #toSorted(Iterator, Comparator)
     * @since 2.4.0
     */
    public static <T> Iterator<T> toSorted(Iterator<T> self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure closure) {
        Comparator<T> comparator = (closure.getMaximumNumberOfParameters() == 1) ? new OrderBy<T>(closure) : new ClosureComparator<T>(closure);
        return toSorted(self, comparator);
    }

    /**
     * Returns a sorted version of the given array using the supplied comparator.
     *
     * @param self the array to be sorted
     * @return the sorted array
     * @see #toSorted(Object[], Comparator)
     * @since 2.4.0
     */
    public static <T> T[] toSorted(T[] self) {
        return toSorted(self, new NumberAwareComparator<T>());
    }

    /**
     * Returns a sorted version of the given array using the supplied comparator to determine the resulting order.
     * <pre class="groovyTestCase">
     * def sumDigitsComparator = [compare: { num1, num2 {@code ->} num1.toString().toList()*.toInteger().sum() {@code <=>} num2.toString().toList()*.toInteger().sum() }] as Comparator
     * Integer[] nums = [9, 44, 222, 7000]
     * def result = nums.toSorted(sumDigitsComparator)
     * assert result instanceof Integer[]
     * assert result == [222, 7000, 44, 9]
     * </pre>
     *
     * @param self the array to be sorted
     * @param comparator a Comparator used for the comparison
     * @return the sorted array
     * @since 2.4.0
     */
    public static <T> T[] toSorted(T[] self, Comparator<T> comparator) {
        T[] answer = self.clone();
        Arrays.sort(answer, comparator);
        return answer;
    }

    /**
     * Sorts the elements from this array into a newly created array using
     * the Closure to determine the correct ordering.
     * <p>
     * If the closure has two parameters it is used like a traditional Comparator. I.e. it should compare
     * its two parameters for order, returning a negative integer, zero, or a positive integer when the
     * first parameter is less than, equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single parameter and return a Comparable (typically an Integer)
     * which is then used for further comparison.
     *
     * @param self the array containing the elements to be sorted
     * @param condition a Closure used to determine the correct ordering
     * @return a sorted array
     * @see #toSorted(Object[], Comparator)
     * @since 2.4.0
     */
    public static <T> T[] toSorted(T[] self, @ClosureParams(value=FromString.class, options={"T","T,T"}) Closure condition) {
        Comparator<T> comparator = (condition.getMaximumNumberOfParameters() == 1) ? new OrderBy<T>(condition) : new ClosureComparator<T>(condition);
        return toSorted(self, comparator);
    }

    /**
     * Sorts the elements from the given map into a new ordered map using
     * a {@link NumberAwareComparator} on map entry values to determine the resulting order.
     * {@code NumberAwareComparator} has special treatment for numbers but otherwise uses the
     * natural ordering of the Iterator elements. The original map is unchanged.
     * <pre class="groovyTestCase">
     * def map = [a:5L, b:3, c:6, d:4.0].toSorted()
     * assert map.toString() == '[b:3, d:4.0, a:5, c:6]'
     * </pre>
     *
     * @param self the original unsorted map
     * @return the sorted map
     * @since 2.4.0
     */
    public static <K, V> Map<K, V> toSorted(Map<K, V> self) {
        return toSorted(self, new NumberAwareValueComparator<K, V>());
    }

    private static class NumberAwareValueComparator<K, V> implements Comparator<Map.Entry<K, V>> {
        private final Comparator<V> delegate = new NumberAwareComparator<V>();

        @Override
        public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
            return delegate.compare(e1.getValue(), e2.getValue());
        }
    }

    /**
     * Sorts the elements from the given map into a new ordered map using
     * the supplied comparator to determine the ordering. The original map is unchanged.
     * <pre class="groovyTestCase">
     * def keyComparator = [compare: { e1, e2 {@code ->} e1.key {@code <=>} e2.key }] as Comparator
     * def valueComparator = [compare: { e1, e2 {@code ->} e1.value {@code <=>} e2.value }] as Comparator
     * def map1 = [a:5, b:3, d:4, c:6].toSorted(keyComparator)
     * assert map1.toString() == '[a:5, b:3, c:6, d:4]'
     * def map2 = [a:5, b:3, d:4, c:6].toSorted(valueComparator)
     * assert map2.toString() == '[b:3, d:4, a:5, c:6]'
     * </pre>
     *
     * @param self the original unsorted map
     * @param comparator a Comparator used for the comparison
     * @return the sorted map
     * @since 2.4.0
     */
    public static <K, V> Map<K, V> toSorted(Map<K, V> self, Comparator<Map.Entry<K, V>> comparator) {
        List<Map.Entry<K, V>> sortedEntries = toSorted(self.entrySet(), comparator);
        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : sortedEntries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Sorts the elements from the given map into a new ordered map using
     * the supplied Closure condition as a comparator to determine the ordering. The original map is unchanged.
     * <p>
     * If the closure has two parameters it is used like a traditional Comparator. I.e. it should compare
     * its two entry parameters for order, returning a negative integer, zero, or a positive integer when the
     * first parameter is less than, equal to, or greater than the second respectively. Otherwise,
     * the Closure is assumed to take a single entry parameter and return a Comparable (typically an Integer)
     * which is then used for further comparison.
     * <pre class="groovyTestCase">
     * def map = [a:5, b:3, c:6, d:4].toSorted { a, b {@code ->} a.value {@code <=>} b.value }
     * assert map.toString() == '[b:3, d:4, a:5, c:6]'
     * </pre>
     *
     * @param self the original unsorted map
     * @param condition a Closure used as a comparator
     * @return the sorted map
     * @since 2.4.0
     */
    public static <K, V> Map<K, V> toSorted(Map<K, V> self, @ClosureParams(value=FromString.class, options={"Map.Entry<K,V>","Map.Entry<K,V>,Map.Entry<K,V>"}) Closure condition) {
        Comparator<Map.Entry<K,V>> comparator = (condition.getMaximumNumberOfParameters() == 1) ? new OrderBy<Map.Entry<K,V>>(condition) : new ClosureComparator<Map.Entry<K,V>>(condition);
        return toSorted(self, comparator);
    }

    /**
     * Avoids doing unnecessary work when sorting an already sorted set
     *
     * @param self an already sorted set
     * @return an ordered copy of the sorted set
     * @since 2.4.0
     */
    public static <T> Set<T> toSorted(SortedSet<T> self) {
        return new LinkedHashSet<T>(self);
    }

    /**
     * Avoids doing unnecessary work when sorting an already sorted map
     *
     * @param self an already sorted map
     * @return an ordered copy of the map
     * @since 2.4.0
     */
    public static <K, V> Map<K, V> toSorted(SortedMap<K, V> self) {
        return new LinkedHashMap<K, V>(self);
    }

    /**
     * Removes the initial item from the List.
     *
     * <pre class="groovyTestCase">
     * def list = ["a", false, 2]
     * assert list.pop() == 'a'
     * assert list == [false, 2]
     * </pre>
     *
     * This is similar to pop on a Stack where the first item in the list
     * represents the top of the stack.
     *
     * Note: The behavior of this method changed in Groovy 2.5 to align with Java.
     * If you need the old behavior use 'removeLast'.
     *
     * @param self a List
     * @return the item removed from the List
     * @throws NoSuchElementException if the list is empty
     * @since 1.0
     */
    public static <T> T pop(List<T> self) {
        if (self.isEmpty()) {
            throw new NoSuchElementException("Cannot pop() an empty List");
        }
        return self.remove(0);
    }

    /**
     * Removes the last item from the List.
     *
     * <pre class="groovyTestCase">
     * def list = ["a", false, 2]
     * assert list.removeLast() == 2
     * assert list == ["a", false]
     * </pre>
     *
     * Using add() and removeLast() is similar to push and pop on a Stack
     * where the last item in the list represents the top of the stack.
     *
     * @param self a List
     * @return the item removed from the List
     * @throws NoSuchElementException if the list is empty
     * @since 2.5.0
     */
    public static <T> T removeLast(List<T> self) {
        if (self.isEmpty()) {
            throw new NoSuchElementException("Cannot removeLast() an empty List");
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
    public static <K, V> Map<K, V> putAll(Map<K, V> self, Collection<? extends Map.Entry<? extends K, ? extends V>> entries) {
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
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
     *
     * @param self    a Map
     * @param entries a Collection of Map.Entry items to be added to the Map.
     * @return a new Map containing all key, value pairs from self and entries
     * @since 1.6.1
     */
    public static <K, V> Map<K, V> plus(Map<K, V> self, Collection<? extends Map.Entry<? extends K, ? extends V>> entries) {
        Map<K, V> map = cloneSimilarMap(self);
        putAll(map, entries);
        return map;
    }

    /**
     * Prepends an item to the start of the List.
     *
     * <pre class="groovyTestCase">
     * def list = [3, 4, 2]
     * list.push("x")
     * assert list == ['x', 3, 4, 2]
     * </pre>
     *
     * This is similar to push on a Stack where the first item in the list
     * represents the top of the stack.
     *
     * Note: The behavior of this method changed in Groovy 2.5 to align with Java.
     * If you need the old behavior use 'add'.
     *
     * @param self a List
     * @param value element to be prepended to this list.
     * @return <tt>true</tt> (for legacy compatibility reasons).
     * @since 1.5.5
     */
    public static <T> boolean push(List<T> self, T value) {
        self.add(0, value);
        return true;
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
     * The last element returned by the Iterable's iterator is returned.
     * If the Iterable doesn't guarantee a defined order it may appear like
     * a random element is returned.
     *
     * @param self an Iterable
     * @return the last item from the Iterable
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
     * Returns the first item from the Iterable.
     * <pre class="groovyTestCase">
     * def set = [3, 4, 2] as LinkedHashSet
     * assert set.head() == 3
     * // check original is unaltered
     * assert set == [3, 4, 2] as Set
     * </pre>
     * The first element returned by the Iterable's iterator is returned.
     * If the Iterable doesn't guarantee a defined order it may appear like
     * a random element is returned.
     *
     * @param self an Iterable
     * @return the first item from the Iterable
     * @throws NoSuchElementException if the Iterable is empty and you try to access the head() item.
     * @since 2.4.0
     */
    public static <T> T head(Iterable<T> self) {
        return first(self);
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
     * @param self an array
     * @return the first item from the Object array
     * @throws NoSuchElementException if the array is empty and you try to access the head() item.
     * @since 1.7.3
     */
    public static <T> T head(T[] self) {
        return first(self);
    }

    /**
     * Returns the items from the List excluding the first item.
     * <pre class="groovyTestCase">
     * def list = [3, 4, 2]
     * assert list.tail() == [4, 2]
     * assert list == [3, 4, 2]
     * </pre>
     *
     * @param self a List
     * @return a List without its first element
     * @throws NoSuchElementException if the List is empty and you try to access the tail()
     * @since 1.5.6
     */
    public static <T> List<T> tail(List<T> self) {
        return (List<T>) tail((Iterable<T>)self);
    }

    /**
     * Returns the items from the SortedSet excluding the first item.
     * <pre class="groovyTestCase">
     * def sortedSet = [3, 4, 2] as SortedSet
     * assert sortedSet.tail() == [3, 4] as SortedSet
     * assert sortedSet == [3, 4, 2] as SortedSet
     * </pre>
     *
     * @param self a SortedSet
     * @return a SortedSet without its first element
     * @throws NoSuchElementException if the SortedSet is empty and you try to access the tail()
     * @since 2.4.0
     */
    public static <T> SortedSet<T> tail(SortedSet<T> self) {
        return (SortedSet<T>) tail((Iterable<T>) self);
    }

    /**
     * Calculates the tail values of this Iterable: the first value will be this list of all items from the iterable and the final one will be an empty list, with the intervening values the results of successive applications of tail on the items.
     * <pre class="groovyTestCase">
     * assert [1, 2, 3, 4].tails() == [[1, 2, 3, 4], [2, 3, 4], [3, 4], [4], []]
     * </pre>
     *
     * @param self an Iterable
     * @return a List of the tail values from the given Iterable
     * @since 2.5.0
     */
    public static <T> List<List<T>> tails(Iterable<T> self) {
        return GroovyCollections.tails(self);
    }

    /**
     * Returns the items from the Iterable excluding the first item.
     * <pre class="groovyTestCase">
     * def list = [3, 4, 2]
     * assert list.tail() == [4, 2]
     * assert list == [3, 4, 2]
     * </pre>
     *
     * @param self an Iterable
     * @return a collection without its first element
     * @throws NoSuchElementException if the iterable is empty and you try to access the tail()
     * @since 2.4.0
     */
    public static <T> Collection<T> tail(Iterable<T> self) {
        if (!self.iterator().hasNext()) {
            throw new NoSuchElementException("Cannot access tail() for an empty iterable");
        }
        Collection<T> result = createSimilarCollection(self);
        addAll(result, tail(self.iterator()));
        return result;
    }

    /**
     * Returns the items from the array excluding the first item.
     * <pre class="groovyTestCase">
     * String[] strings = ["a", "b", "c"]
     * def result = strings.tail()
     * assert result.class.componentType == String
     * String[] expected = ["b", "c"]
     * assert result == expected
     * </pre>
     *
     * @param self an array
     * @return an array without its first element
     * @throws NoSuchElementException if the array is empty and you try to access the tail()
     * @since 1.7.3
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] tail(T[] self) {
        if (self.length == 0) {
            throw new NoSuchElementException("Cannot access tail() for an empty array");
        }
        T[] result = createSimilarArray(self, self.length - 1);
        System.arraycopy(self, 1, result, 0, self.length - 1);
        return result;
    }

    /**
     * Returns the original iterator after throwing away the first element.
     *
     * @param self the original iterator
     * @return the iterator without its first element
     * @throws NoSuchElementException if the array is empty and you try to access the tail()
     * @since 1.8.1
     */
    public static <T> Iterator<T> tail(Iterator<T> self) {
        if (!self.hasNext()) {
            throw new NoSuchElementException("Cannot access tail() for an empty Iterator");
        }
        self.next();
        return self;
    }

    /**
     * Calculates the init values of this Iterable: the first value will be this list of all items from the iterable and the final one will be an empty list, with the intervening values the results of successive applications of init on the items.
     * <pre class="groovyTestCase">
     * assert [1, 2, 3, 4].inits() == [[1, 2, 3, 4], [1, 2, 3], [1, 2], [1], []]
     * </pre>
     *
     * @param self an Iterable
     * @return a List of the init values from the given Iterable
     * @since 2.5.0
     */
    public static <T> List<List<T>> inits(Iterable<T> self) {
        return GroovyCollections.inits(self);
    }

    /**
     * Returns the items from the Iterable excluding the last item. Leaves the original Iterable unchanged.
     * <pre class="groovyTestCase">
     * def list = [3, 4, 2]
     * assert list.init() == [3, 4]
     * assert list == [3, 4, 2]
     * </pre>
     *
     * @param self an Iterable
     * @return a Collection without its last element
     * @throws NoSuchElementException if the iterable is empty and you try to access init()
     * @since 2.4.0
     */
    public static <T> Collection<T> init(Iterable<T> self) {
        if (!self.iterator().hasNext()) {
            throw new NoSuchElementException("Cannot access init() for an empty Iterable");
        }
        Collection<T> result;
        if (self instanceof Collection) {
            Collection<T> selfCol = (Collection<T>) self;
            result = createSimilarCollection(selfCol, selfCol.size() - 1);
        } else {
            result = new ArrayList<T>();
        }
        addAll(result, init(self.iterator()));
        return result;
    }

    /**
     * Returns the items from the List excluding the last item. Leaves the original List unchanged.
     * <pre class="groovyTestCase">
     * def list = [3, 4, 2]
     * assert list.init() == [3, 4]
     * assert list == [3, 4, 2]
     * </pre>
     *
     * @param self a List
     * @return a List without its last element
     * @throws NoSuchElementException if the List is empty and you try to access init()
     * @since 2.4.0
     */
    public static <T> List<T> init(List<T> self) {
        return (List<T>) init((Iterable<T>) self);
    }

    /**
     * Returns the items from the SortedSet excluding the last item. Leaves the original SortedSet unchanged.
     * <pre class="groovyTestCase">
     * def sortedSet = [3, 4, 2] as SortedSet
     * assert sortedSet.init() == [2, 3] as SortedSet
     * assert sortedSet == [3, 4, 2] as SortedSet
     * </pre>
     *
     * @param self a SortedSet
     * @return a SortedSet without its last element
     * @throws NoSuchElementException if the SortedSet is empty and you try to access init()
     * @since 2.4.0
     */
    public static <T> SortedSet<T> init(SortedSet<T> self) {
        return (SortedSet<T>) init((Iterable<T>) self);
    }

    /**
     * Returns an Iterator containing all of the items from this iterator except the last one.
     * <pre class="groovyTestCase">
     * def iter = [3, 4, 2].listIterator()
     * def result = iter.init()
     * assert result.toList() == [3, 4]
     * </pre>
     *
     * @param self an Iterator
     * @return an Iterator without the last element from the original Iterator
     * @throws NoSuchElementException if the iterator is empty and you try to access init()
     * @since 2.4.0
     */
    public static <T> Iterator<T> init(Iterator<T> self) {
        if (!self.hasNext()) {
            throw new NoSuchElementException("Cannot access init() for an empty Iterator");
        }
        return new InitIterator<T>(self);
    }

    private static final class InitIterator<E> implements Iterator<E> {
        private final Iterator<E> delegate;
        private boolean exhausted;
        private E next;

        private InitIterator(Iterator<E> delegate) {
            this.delegate = delegate;
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
            advance();
        }

        private void advance() {
            next = delegate.next();
            exhausted = !delegate.hasNext();
        }
    }

    /**
     * Returns the items from the Object array excluding the last item.
     * <pre class="groovyTestCase">
     *     String[] strings = ["a", "b", "c"]
     *     def result = strings.init()
     *     assert result.length == 2
     *     assert strings.class.componentType == String
     * </pre>
     *
     * @param self an array
     * @return an array without its last element
     * @throws NoSuchElementException if the array is empty and you try to access the init() item.
     * @since 2.4.0
     */
    public static <T> T[] init(T[] self) {
        if (self.length == 0) {
            throw new NoSuchElementException("Cannot access init() for an empty Object array");
        }
        T[] result = createSimilarArray(self, self.length - 1);
        System.arraycopy(self, 0, result, 0, self.length - 1);
        return result;
    }

    /**
     * Returns the first <code>num</code> elements from the head of this List.
     * <pre class="groovyTestCase">
     * def strings = [ 'a', 'b', 'c' ]
     * assert strings.take( 0 ) == []
     * assert strings.take( 2 ) == [ 'a', 'b' ]
     * assert strings.take( 5 ) == [ 'a', 'b', 'c' ]
     * </pre>
     *
     * @param self the original List
     * @param num  the number of elements to take from this List
     * @return a List consisting of the first <code>num</code> elements from this List,
     *         or else all the elements from the List if it has less then <code>num</code> elements.
     * @since 1.8.1
     */
    public static <T> List<T> take(List<T> self, int num) {
        return (List<T>) take((Iterable<T>)self, num);
    }

    /**
     * Returns the first <code>num</code> elements from the head of this SortedSet.
     * <pre class="groovyTestCase">
     * def strings = [ 'a', 'b', 'c' ] as SortedSet
     * assert strings.take( 0 ) == [] as SortedSet
     * assert strings.take( 2 ) == [ 'a', 'b' ] as SortedSet
     * assert strings.take( 5 ) == [ 'a', 'b', 'c' ] as SortedSet
     * </pre>
     *
     * @param self the original SortedSet
     * @param num  the number of elements to take from this SortedSet
     * @return a SortedSet consisting of the first <code>num</code> elements from this List,
     *         or else all the elements from the SortedSet if it has less then <code>num</code> elements.
     * @since 2.4.0
     */
    public static <T> SortedSet<T> take(SortedSet<T> self, int num) {
        return (SortedSet<T>) take((Iterable<T>) self, num);
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
     * def strings = [ 'a', 'b', 'c' ]
     * assert strings.take( 0 ) == []
     * assert strings.take( 2 ) == [ 'a', 'b' ]
     * assert strings.take( 5 ) == [ 'a', 'b', 'c' ]
     *
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
     * @return a Collection consisting of the first <code>num</code> elements from this Iterable,
     *         or else all the elements from the Iterable if it has less then <code>num</code> elements.
     * @since 1.8.7
     */
    public static <T> Collection<T> take(Iterable<T> self, int num) {
        Collection<T> result = self instanceof Collection ? createSimilarCollection((Collection<T>) self, Math.max(num, 0)) : new ArrayList<T>();
        addAll(result, take(self.iterator(), num));
        return result;
    }

    /**
     * Adds all items from the iterator to the Collection.
     *
     * @param self the collection
     * @param items the items to add
     * @return true if the collection changed
     */
    public static <T> boolean addAll(Collection<T> self, Iterator<? extends T> items) {
        boolean changed = false;
        while (items.hasNext()) {
            T next =  items.next();
            if (self.add(next)) changed = true;
        }
        return changed;
    }

    /**
     * Adds all items from the iterable to the Collection.
     *
     * @param self the collection
     * @param items the items to add
     * @return true if the collection changed
     */
    public static <T> boolean addAll(Collection<T> self, Iterable<? extends T> items) {
        boolean changed = false;
        for (T next : items) {
            if (self.add(next)) changed = true;
        }
        return changed;
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
        for (Map.Entry<K, V> entry : self.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();

            ret.put(key, value);
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
    public static <T> Iterator<T> take(Iterator<T> self, int num) {
        return new TakeIterator<T>(self, num);
    }

    private static final class TakeIterator<E> implements Iterator<E> {
        private final Iterator<E> delegate;
        private Integer num;

        private TakeIterator(Iterator<E> delegate, Integer num) {
            this.delegate = delegate;
            this.num = num;
        }

        public boolean hasNext() {
            return num > 0 && delegate.hasNext();
        }

        public E next() {
            if (num <= 0) throw new NoSuchElementException();
            num--;
            return delegate.next();
        }

        public void remove() {
            delegate.remove();
        }
    }

    @Deprecated
    public static CharSequence take(CharSequence self, int num) {
        return StringGroovyMethods.take(self, num);
    }

    /**
     * Returns the last <code>num</code> elements from the tail of this array.
     * <pre class="groovyTestCase">
     * String[] strings = [ 'a', 'b', 'c' ]
     * assert strings.takeRight( 0 ) == [] as String[]
     * assert strings.takeRight( 2 ) == [ 'b', 'c' ] as String[]
     * assert strings.takeRight( 5 ) == [ 'a', 'b', 'c' ] as String[]
     * </pre>
     *
     * @param self the original array
     * @param num  the number of elements to take from this array
     * @return an array consisting of the last <code>num</code> elements of this array,
     *         or else the whole array if it has less then <code>num</code> elements.
     * @since 2.4.0
     */
    public static <T> T[] takeRight(T[] self, int num) {
        if (self.length == 0 || num <= 0) {
            return createSimilarArray(self, 0);
        }

        if (self.length <= num) {
            T[] ret = createSimilarArray(self, self.length);
            System.arraycopy(self, 0, ret, 0, self.length);
            return ret;
        }

        T[] ret = createSimilarArray(self, num);
        System.arraycopy(self, self.length - num, ret, 0, num);
        return ret;
    }

    /**
     * Returns the last <code>num</code> elements from the tail of this Iterable.
     * <pre class="groovyTestCase">
     * def strings = [ 'a', 'b', 'c' ]
     * assert strings.takeRight( 0 ) == []
     * assert strings.takeRight( 2 ) == [ 'b', 'c' ]
     * assert strings.takeRight( 5 ) == [ 'a', 'b', 'c' ]
     *
     * class AbcIterable implements Iterable<String> {
     *     Iterator<String> iterator() { "abc".iterator() }
     * }
     * def abc = new AbcIterable()
     * assert abc.takeRight(0) == []
     * assert abc.takeRight(1) == ['c']
     * assert abc.takeRight(3) == ['a', 'b', 'c']
     * assert abc.takeRight(5) == ['a', 'b', 'c']
     * </pre>
     *
     * @param self the original Iterable
     * @param num  the number of elements to take from this Iterable
     * @return a Collection consisting of the last <code>num</code> elements from this Iterable,
     *         or else all the elements from the Iterable if it has less then <code>num</code> elements.
     * @since 2.4.0
     */
    public static <T> Collection<T> takeRight(Iterable<T> self, int num) {
        if (num <= 0 || !self.iterator().hasNext()) {
            return self instanceof Collection ? createSimilarCollection((Collection<T>) self, 0) : new ArrayList<T>();
        }
        Collection<T> selfCol = self instanceof Collection ? (Collection<T>) self : toList(self);
        if (selfCol.size() <= num) {
            Collection<T> ret = createSimilarCollection(selfCol, selfCol.size());
            ret.addAll(selfCol);
            return ret;
        }
        Collection<T> ret = createSimilarCollection(selfCol, num);
        ret.addAll(asList((Iterable<T>) selfCol).subList(selfCol.size() - num, selfCol.size()));
        return ret;
    }

    /**
     * Returns the last <code>num</code> elements from the tail of this List.
     * <pre class="groovyTestCase">
     * def strings = [ 'a', 'b', 'c' ]
     * assert strings.takeRight( 0 ) == []
     * assert strings.takeRight( 2 ) == [ 'b', 'c' ]
     * assert strings.takeRight( 5 ) == [ 'a', 'b', 'c' ]
     * </pre>
     *
     * @param self the original List
     * @param num  the number of elements to take from this List
     * @return a List consisting of the last <code>num</code> elements from this List,
     *         or else all the elements from the List if it has less then <code>num</code> elements.
     * @since 2.4.0
     */
    public static <T> List<T> takeRight(List<T> self, int num) {
        return (List<T>) takeRight((Iterable<T>) self, num);
    }

    /**
     * Returns the last <code>num</code> elements from the tail of this SortedSet.
     * <pre class="groovyTestCase">
     * def strings = [ 'a', 'b', 'c' ] as SortedSet
     * assert strings.takeRight( 0 ) == [] as SortedSet
     * assert strings.takeRight( 2 ) == [ 'b', 'c' ] as SortedSet
     * assert strings.takeRight( 5 ) == [ 'a', 'b', 'c' ] as SortedSet
     * </pre>
     *
     * @param self the original SortedSet
     * @param num  the number of elements to take from this SortedSet
     * @return a SortedSet consisting of the last <code>num</code> elements from this SortedSet,
     *         or else all the elements from the SortedSet if it has less then <code>num</code> elements.
     * @since 2.4.0
     */
    public static <T> SortedSet<T> takeRight(SortedSet<T> self, int num) {
        return (SortedSet<T>) takeRight((Iterable<T>) self, num);
    }

    /**
     * Drops the given number of elements from the head of this List.
     * <pre class="groovyTestCase">
     * def strings = [ 'a', 'b', 'c' ] as SortedSet
     * assert strings.drop( 0 ) == [ 'a', 'b', 'c' ] as SortedSet
     * assert strings.drop( 2 ) == [ 'c' ] as SortedSet
     * assert strings.drop( 5 ) == [] as SortedSet
     * </pre>
     *
     * @param self the original SortedSet
     * @param num  the number of elements to drop from this Iterable
     * @return a SortedSet consisting of all the elements of this Iterable minus the first <code>num</code> elements,
     *         or an empty list if it has less then <code>num</code> elements.
     * @since 2.4.0
     */
    public static <T> SortedSet<T> drop(SortedSet<T> self, int num) {
        return (SortedSet<T>) drop((Iterable<T>) self, num);
    }

    /**
     * Drops the given number of elements from the head of this List.
     * <pre class="groovyTestCase">
     * def strings = [ 'a', 'b', 'c' ]
     * assert strings.drop( 0 ) == [ 'a', 'b', 'c' ]
     * assert strings.drop( 2 ) == [ 'c' ]
     * assert strings.drop( 5 ) == []
     * </pre>
     *
     * @param self the original List
     * @param num  the number of elements to drop from this Iterable
     * @return a List consisting of all the elements of this Iterable minus the first <code>num</code> elements,
     *         or an empty list if it has less then <code>num</code> elements.
     * @since 1.8.1
     */
    public static <T> List<T> drop(List<T> self, int num) {
        return (List<T>) drop((Iterable<T>) self, num);
    }

    /**
     * Drops the given number of elements from the head of this Iterable.
     * <pre class="groovyTestCase">
     * def strings = [ 'a', 'b', 'c' ]
     * assert strings.drop( 0 ) == [ 'a', 'b', 'c' ]
     * assert strings.drop( 2 ) == [ 'c' ]
     * assert strings.drop( 5 ) == []
     *
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
     * @return a Collection consisting of all the elements of this Iterable minus the first <code>num</code> elements,
     *         or an empty list if it has less then <code>num</code> elements.
     * @since 1.8.7
     */
    public static <T> Collection<T> drop(Iterable<T> self, int num) {
        Collection<T> result = createSimilarCollection(self);
        addAll(result, drop(self.iterator(), num));
        return result;
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
        for (Map.Entry<K, V> entry : self.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();

            if (num-- <= 0) {
                ret.put(key, value);
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
     * Drops the given number of elements from the tail of this SortedSet.
     * <pre class="groovyTestCase">
     * def strings = [ 'a', 'b', 'c' ] as SortedSet
     * assert strings.dropRight( 0 ) == [ 'a', 'b', 'c' ] as SortedSet
     * assert strings.dropRight( 2 ) == [ 'a' ] as SortedSet
     * assert strings.dropRight( 5 ) == [] as SortedSet
     * </pre>
     *
     * @param self the original SortedSet
     * @param num  the number of elements to drop from this SortedSet
     * @return a List consisting of all the elements of this SortedSet minus the last <code>num</code> elements,
     *         or an empty SortedSet if it has less then <code>num</code> elements.
     * @since 2.4.0
     */
    public static <T> SortedSet<T> dropRight(SortedSet<T> self, int num) {
        return (SortedSet<T>) dropRight((Iterable<T>) self, num);
    }

    /**
     * Drops the given number of elements from the tail of this List.
     * <pre class="groovyTestCase">
     * def strings = [ 'a', 'b', 'c' ]
     * assert strings.dropRight( 0 ) == [ 'a', 'b', 'c' ]
     * assert strings.dropRight( 2 ) == [ 'a' ]
     * assert strings.dropRight( 5 ) == []
     * </pre>
     *
     * @param self the original List
     * @param num  the number of elements to drop from this List
     * @return a List consisting of all the elements of this List minus the last <code>num</code> elements,
     *         or an empty List if it has less then <code>num</code> elements.
     * @since 2.4.0
     */
    public static <T> List<T> dropRight(List<T> self, int num) {
        return (List<T>) dropRight((Iterable<T>) self, num);
    }

    /**
     * Drops the given number of elements from the tail of this Iterable.
     * <pre class="groovyTestCase">
     * def strings = [ 'a', 'b', 'c' ]
     * assert strings.dropRight( 0 ) == [ 'a', 'b', 'c' ]
     * assert strings.dropRight( 2 ) == [ 'a' ]
     * assert strings.dropRight( 5 ) == []
     *
     * class AbcIterable implements Iterable<String> {
     *     Iterator<String> iterator() { "abc".iterator() }
     * }
     * def abc = new AbcIterable()
     * assert abc.dropRight(0) == ['a', 'b', 'c']
     * assert abc.dropRight(1) == ['a', 'b']
     * assert abc.dropRight(3) == []
     * assert abc.dropRight(5) == []
     * </pre>
     *
     * @param self the original Iterable
     * @param num  the number of elements to drop from this Iterable
     * @return a Collection consisting of all the elements of this Iterable minus the last <code>num</code> elements,
     *         or an empty list if it has less then <code>num</code> elements.
     * @since 2.4.0
     */
    public static <T> Collection<T> dropRight(Iterable<T> self, int num) {
        Collection<T> selfCol = self instanceof Collection ? (Collection<T>) self : toList(self);
        if (selfCol.size() <= num) {
            return createSimilarCollection(selfCol, 0);
        }
        if (num <= 0) {
            Collection<T> ret = createSimilarCollection(selfCol, selfCol.size());
            ret.addAll(selfCol);
            return ret;
        }
        Collection<T> ret = createSimilarCollection(selfCol, selfCol.size() - num);
        ret.addAll(asList((Iterable<T>)selfCol).subList(0, selfCol.size() - num));
        return ret;
    }

    /**
     * Drops the given number of elements from the tail of this Iterator.
     * <pre class="groovyTestCase">
     * def getObliterator() { "obliter8".iterator() }
     * assert obliterator.dropRight(-1).toList() == ['o', 'b', 'l', 'i', 't', 'e', 'r', '8']
     * assert obliterator.dropRight(0).toList() == ['o', 'b', 'l', 'i', 't', 'e', 'r', '8']
     * assert obliterator.dropRight(1).toList() == ['o', 'b', 'l', 'i', 't', 'e', 'r']
     * assert obliterator.dropRight(4).toList() == ['o', 'b', 'l', 'i']
     * assert obliterator.dropRight(7).toList() == ['o']
     * assert obliterator.dropRight(8).toList() == []
     * assert obliterator.dropRight(9).toList() == []
     * </pre>
     *
     * @param self the original Iterator
     * @param num  the number of elements to drop
     * @return an Iterator consisting of all the elements of this Iterator minus the last <code>num</code> elements,
     *         or an empty Iterator if it has less then <code>num</code> elements.
     * @since 2.4.0
     */
    public static <T> Iterator<T> dropRight(Iterator<T> self, int num) {
        if (num <= 0) {
            return self;
        }
        return new DropRightIterator<T>(self, num);
    }

    private static final class DropRightIterator<E> implements Iterator<E> {
        private final Iterator<E> delegate;
        private final LinkedList<E> discards;
        private boolean exhausted;
        private int num;

        private DropRightIterator(Iterator<E> delegate, int num) {
            this.delegate = delegate;
            this.num = num;
            discards = new LinkedList<E>();
            advance();
        }

        public boolean hasNext() {
            return !exhausted;
        }

        public E next() {
            if (exhausted) throw new NoSuchElementException();
            E result = discards.removeFirst();
            advance();
            return result;
        }

        public void remove() {
            if (exhausted) throw new NoSuchElementException();
            delegate.remove();
        }

        private void advance() {
            while (discards.size() <= num && !exhausted) {
                exhausted = !delegate.hasNext();
                if (!exhausted) {
                    discards.add(delegate.next());
                }
            }
        }
    }

    /**
     * Drops the given number of elements from the tail of this array
     * if they are available.
     * <pre class="groovyTestCase">
     * String[] strings = [ 'a', 'b', 'c' ]
     * assert strings.dropRight( 0 ) == [ 'a', 'b', 'c' ] as String[]
     * assert strings.dropRight( 2 ) == [ 'a' ] as String[]
     * assert strings.dropRight( 5 ) == [] as String[]
     * </pre>
     *
     * @param self the original array
     * @param num  the number of elements to drop from this array
     * @return an array consisting of all elements of this array except the
     *         last <code>num</code> ones, or else the empty array, if this
     *         array has less than <code>num</code> elements.
     * @since 2.4.0
     */
    public static <T> T[] dropRight(T[] self, int num) {
        if (self.length <= num) {
            return createSimilarArray(self, 0);
        }
        if (num <= 0) {
            T[] ret = createSimilarArray(self, self.length);
            System.arraycopy(self, 0, ret, 0, self.length);
            return ret;
        }

        T[] ret = createSimilarArray(self, self.length - num);
        System.arraycopy(self, 0, ret, 0, self.length - num);
        return ret;
    }

    /**
     * Returns the longest prefix of this list where each element
     * passed to the given closure condition evaluates to true.
     * Similar to {@link #takeWhile(Iterable, groovy.lang.Closure)}
     * except that it attempts to preserve the type of the original list.
     * <pre class="groovyTestCase">
     * def nums = [ 1, 3, 2 ]
     * assert nums.takeWhile{ it {@code <} 1 } == []
     * assert nums.takeWhile{ it {@code <} 3 } == [ 1 ]
     * assert nums.takeWhile{ it {@code <} 4 } == [ 1, 3, 2 ]
     * </pre>
     *
     * @param self      the original list
     * @param condition the closure that must evaluate to true to
     *                  continue taking elements
     * @return a prefix of the given list where each element passed to
     *         the given closure evaluates to true
     * @since 1.8.7
     */
    public static <T> List<T> takeWhile(List<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
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
     * Returns a Collection containing the longest prefix of the elements from this Iterable
     * where each element passed to the given closure evaluates to true.
     * <pre class="groovyTestCase">
     * class AbcIterable implements Iterable<String> {
     *     Iterator<String> iterator() { "abc".iterator() }
     * }
     * def abc = new AbcIterable()
     * assert abc.takeWhile{ it {@code <} 'b' } == ['a']
     * assert abc.takeWhile{ it {@code <=} 'b' } == ['a', 'b']
     * </pre>
     *
     * @param self      an Iterable
     * @param condition the closure that must evaluate to true to
     *                  continue taking elements
     * @return a Collection containing a prefix of the elements from the given Iterable where
     *         each element passed to the given closure evaluates to true
     * @since 1.8.7
     */
    public static <T> Collection<T> takeWhile(Iterable<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
        Collection<T> result = createSimilarCollection(self);
        addAll(result, takeWhile(self.iterator(), condition));
        return result;
    }

    /**
     * Returns the longest prefix of this SortedSet where each element
     * passed to the given closure condition evaluates to true.
     * Similar to {@link #takeWhile(Iterable, groovy.lang.Closure)}
     * except that it attempts to preserve the type of the original SortedSet.
     * <pre class="groovyTestCase">
     * def nums = [ 1, 2, 3 ] as SortedSet
     * assert nums.takeWhile{ it {@code <} 1 } == [] as SortedSet
     * assert nums.takeWhile{ it {@code <} 2 } == [ 1 ] as SortedSet
     * assert nums.takeWhile{ it {@code <} 4 } == [ 1, 2, 3 ] as SortedSet
     * </pre>
     *
     * @param self      the original SortedSet
     * @param condition the closure that must evaluate to true to
     *                  continue taking elements
     * @return a prefix of the given SortedSet where each element passed to
     *         the given closure evaluates to true
     * @since 2.4.0
     */
    public static <T> SortedSet<T> takeWhile(SortedSet<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
        return (SortedSet<T>) takeWhile((Iterable<T>) self, condition);
    }

    /**
     * Returns the longest prefix of this Map where each entry (or key/value pair) when
     * passed to the given closure evaluates to true.
     * <pre class="groovyTestCase">
     * def shopping = [milk:1, bread:2, chocolate:3]
     * assert shopping.takeWhile{ it.key.size() {@code <} 6 } == [milk:1, bread:2]
     * assert shopping.takeWhile{ it.value % 2 } == [milk:1]
     * assert shopping.takeWhile{ k, v {@code ->} k.size() + v {@code <=} 7 } == [milk:1, bread:2]
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
    public static <K, V> Map<K, V> takeWhile(Map<K, V> self, @ClosureParams(MapEntryOrKeyValue.class) Closure condition) {
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
     * assert nums.takeWhile{ it {@code <} 1 } == [] as Integer[]
     * assert nums.takeWhile{ it {@code <} 3 } == [ 1 ] as Integer[]
     * assert nums.takeWhile{ it {@code <} 4 } == [ 1, 3, 2 ] as Integer[]
     * </pre>
     *
     * @param self      the original array
     * @param condition the closure that must evaluate to true to
     *                  continue taking elements
     * @return a prefix of the given array where each element passed to
     *         the given closure evaluates to true
     * @since 1.8.7
     */
    public static <T> T[] takeWhile(T[] self, @ClosureParams(FirstParam.Component.class) Closure condition) {
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
     * <p>
     * <pre class="groovyTestCase">
     * def a = 0
     * def iter = [ hasNext:{ true }, next:{ a++ } ] as Iterator
     *
     * assert [].iterator().takeWhile{ it {@code <} 3 }.toList() == []
     * assert [1, 2, 3, 4, 5].iterator().takeWhile{ it {@code <} 3 }.toList() == [ 1, 2 ]
     * assert iter.takeWhile{ it {@code <} 5 }.toList() == [ 0, 1, 2, 3, 4 ]
     * </pre>
     *
     * @param self      the Iterator
     * @param condition the closure that must evaluate to true to
     *                  continue taking elements
     * @return a prefix of elements in the given iterator where each
     *         element passed to the given closure evaluates to true
     * @since 1.8.7
     */
    public static <T> Iterator<T> takeWhile(Iterator<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
        return new TakeWhileIterator<T>(self, condition);
    }

    private static final class TakeWhileIterator<E> implements Iterator<E> {
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
     * Returns a suffix of this SortedSet where elements are dropped from the front
     * while the given Closure evaluates to true.
     * Similar to {@link #dropWhile(Iterable, groovy.lang.Closure)}
     * except that it attempts to preserve the type of the original SortedSet.
     * <pre class="groovyTestCase">
     * def nums = [ 1, 2, 3 ] as SortedSet
     * assert nums.dropWhile{ it {@code <} 4 } == [] as SortedSet
     * assert nums.dropWhile{ it {@code <} 2 } == [ 2, 3 ] as SortedSet
     * assert nums.dropWhile{ it != 3 } == [ 3 ] as SortedSet
     * assert nums.dropWhile{ it == 0 } == [ 1, 2, 3 ] as SortedSet
     * </pre>
     *
     * @param self      the original SortedSet
     * @param condition the closure that must evaluate to true to continue dropping elements
     * @return the shortest suffix of the given SortedSet such that the given closure condition
     *         evaluates to true for each element dropped from the front of the SortedSet
     * @since 2.4.0
     */
    public static <T> SortedSet<T> dropWhile(SortedSet<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
        return (SortedSet<T>) dropWhile((Iterable<T>) self, condition);
    }

    /**
     * Returns a suffix of this List where elements are dropped from the front
     * while the given Closure evaluates to true.
     * Similar to {@link #dropWhile(Iterable, groovy.lang.Closure)}
     * except that it attempts to preserve the type of the original list.
     * <pre class="groovyTestCase">
     * def nums = [ 1, 3, 2 ]
     * assert nums.dropWhile{ it {@code <} 4 } == []
     * assert nums.dropWhile{ it {@code <} 3 } == [ 3, 2 ]
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
    public static <T> List<T> dropWhile(List<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
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
     * class HorseIterable implements Iterable<String> {
     *     Iterator<String> iterator() { "horse".iterator() }
     * }
     * def horse = new HorseIterable()
     * assert horse.dropWhile{ it {@code <} 'r' } == ['r', 's', 'e']
     * assert horse.dropWhile{ it {@code <=} 'r' } == ['s', 'e']
     * </pre>
     *
     * @param self      an Iterable
     * @param condition the closure that must evaluate to true to continue dropping elements
     * @return a Collection containing the shortest suffix of the given Iterable such that the given closure condition
     *         evaluates to true for each element dropped from the front of the Iterable
     * @since 1.8.7
     */
    public static <T> Collection<T> dropWhile(Iterable<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
        Collection<T> selfCol = self instanceof Collection ? (Collection<T>) self : toList(self);
        Collection<T> result = createSimilarCollection(selfCol);
        addAll(result, dropWhile(self.iterator(), condition));
        return result;
    }

    /**
     * Create a suffix of the given Map by dropping as many entries as possible from the
     * front of the original Map such that calling the given closure condition evaluates to
     * true when passed each of the dropped entries (or key/value pairs).
     * <pre class="groovyTestCase">
     * def shopping = [milk:1, bread:2, chocolate:3]
     * assert shopping.dropWhile{ it.key.size() {@code <} 6 } == [chocolate:3]
     * assert shopping.dropWhile{ it.value % 2 } == [bread:2, chocolate:3]
     * assert shopping.dropWhile{ k, v {@code ->} k.size() + v {@code <=} 7 } == [chocolate:3]
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
    public static <K, V> Map<K, V> dropWhile(Map<K, V> self, @ClosureParams(MapEntryOrKeyValue.class) Closure condition) {
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
     * assert nums.dropWhile{ it {@code <=} 3 } == [ ] as Integer[]
     * assert nums.dropWhile{ it {@code <} 3 } == [ 3, 2 ] as Integer[]
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
    public static <T> T[] dropWhile(T[] self, @ClosureParams(FirstParam.Component.class) Closure<?> condition) {
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
     * <pre class="groovyTestCase">
     * def a = 0
     * def iter = [ hasNext:{ a {@code <} 10 }, next:{ a++ } ] as Iterator
     * assert [].iterator().dropWhile{ it {@code <} 3 }.toList() == []
     * assert [1, 2, 3, 4, 5].iterator().dropWhile{ it {@code <} 3 }.toList() == [ 3, 4, 5 ]
     * assert iter.dropWhile{ it {@code <} 5 }.toList() == [ 5, 6, 7, 8, 9 ]
     * </pre>
     *
     * @param self      the Iterator
     * @param condition the closure that must evaluate to true to continue dropping elements
     * @return the shortest suffix of elements from the given Iterator such that the given closure condition
     *         evaluates to true for each element dropped from the front of the Iterator
     * @since 1.8.7
     */
    public static <T> Iterator<T> dropWhile(Iterator<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure<?> condition) {
        return new DropWhileIterator<T>(self, condition);
    }

    private static final class DropWhileIterator<E> implements Iterator<E> {
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
     * Converts this Iterable to a Collection. Returns the original Iterable
     * if it is already a Collection.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * assert new HashSet().asCollection() instanceof Collection
     * </pre>
     *
     * @param self an Iterable to be converted into a Collection
     * @return a newly created List if this Iterable is not already a Collection
     * @since 2.4.0
     */
    public static <T> Collection<T> asCollection(Iterable<T> self) {
        if (self instanceof Collection) {
            return (Collection<T>) self;
        } else {
            return toList(self);
        }
    }

    /**
     * @deprecated Use the Iterable version of asList instead
     * @see #asList(Iterable)
     * @since 1.0
     */
    @Deprecated
    public static <T> List<T> asList(Collection<T> self) {
        return asList((Iterable<T>)self);
    }

    /**
     * Converts this Iterable to a List. Returns the original Iterable
     * if it is already a List.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * assert new HashSet().asList() instanceof List
     * </pre>
     *
     * @param self an Iterable to be converted into a List
     * @return a newly created List if this Iterable is not already a List
     * @since 2.2.0
     */
    public static <T> List<T> asList(Iterable<T> self) {
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
     * Coerce a Boolean instance to a boolean value.
     *
     * @param bool the Boolean
     * @return the boolean value
     * @since 1.7.0
     */
    public static boolean asBoolean(Boolean bool) {
        if (null == bool) {
            return false;
        }

        return bool;
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
        if (null == collection) {
            return false;
        }

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
        if (null == map) {
            return false;
        }

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
        if (null == iterator) {
            return false;
        }

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
        if (null == enumeration) {
            return false;
        }

        return enumeration.hasMoreElements();
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
        if (null == array) {
            return false;
        }

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
        if (null == array) {
            return false;
        }

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
        if (null == array) {
            return false;
        }

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
        if (null == array) {
            return false;
        }

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
        if (null == array) {
            return false;
        }

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
        if (null == array) {
            return false;
        }

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
        if (null == array) {
            return false;
        }

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
        if (null == array) {
            return false;
        }

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
        if (null == array) {
            return false;
        }

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
        if (null == character) {
            return false;
        }

        return character != 0;
    }

    /**
     * Coerce a Float instance to a boolean value.
     *
     * @param object the Float
     * @return {@code true} for non-zero and non-NaN values, else {@code false}
     * @since 2.6.0
     */
    public static boolean asBoolean(Float object) {
        float f = object;
        return f != 0.0f && !Float.isNaN(f);
    }

    /**
     * Coerce a Double instance to a boolean value.
     *
     * @param object the Double
     * @return {@code true} for non-zero and non-NaN values, else {@code false}
     * @since 2.6.0
     */
    public static boolean asBoolean(Double object) {
        double d = object;
        return d != 0.0d && !Double.isNaN(d);
    }

    /**
     * Coerce a number to a boolean value.
     * A number is coerced to false if its double value is equal to 0, and to true otherwise.
     *
     * @param number the number
     * @return the boolean value
     * @since 1.7.0
     */
    public static boolean asBoolean(Number number) {
        if (null == number) {
            return false;
        }

        return number.doubleValue() != 0;
    }

    /**
     * Converts the given iterable to another type.
     *
     * @param iterable a Iterable
     * @param clazz    the desired class
     * @return the object resulting from this type conversion
     * @see #asType(Collection, Class)
     * @since 2.4.12
     */
    @SuppressWarnings("unchecked")
    public static <T> T asType(Iterable iterable, Class<T> clazz) {
        if (Collection.class.isAssignableFrom(clazz)) {
            return asType((Collection) toList(iterable), clazz);
        }

        return asType((Object) iterable, clazz);
    }

    /**
     * Converts the given collection to another type. A default concrete
     * type is used for List, Set, or SortedSet. If the given type has
     * a constructor taking a collection, that is used. Otherwise, the
     * call is deferred to {@link #asType(Object,Class)}.  If this
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
            return (T) asList((Iterable) col);
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
     * call is deferred to {@link #asType(Object,Class)}.
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
            if (Traits.isTrait(clazz)) {
                Method samMethod = CachedSAMClass.getSAMMethod(clazz);
                if (samMethod!=null) {
                    Map impl = Collections.singletonMap(samMethod.getName(),cl);
                    return (T) ProxyGenerator.INSTANCE.instantiateAggregate(impl, Collections.singletonList(clazz));
                }
            }
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
        if (!(clazz.isInstance(map)) && clazz.isInterface() && !Traits.isTrait(clazz)) {
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
     * Randomly reorders the elements of the specified list.
     * <pre class="groovyTestCase">
     * def list = ["a", 4, false]
     * def origSize = list.size()
     * def origCopy = new ArrayList(list)
     * list.shuffle()
     * assert list.size() == origSize
     * assert origCopy.every{ list.contains(it) }
     * </pre>
     *
     * @param self a List
     * @see Collections#shuffle(List)
     * @since 3.0.0
     */
    public static void shuffle(List<?> self) {
        Collections.shuffle(self);
    }

    /**
     * Randomly reorders the elements of the specified list using the
     * specified random instance as the source of randomness.
     * <pre class="groovyTestCase">
     * def r = new Random()
     * def list = ["a", 4, false]
     * def origSize = list.size()
     * def origCopy = new ArrayList(list)
     * list.shuffle(r)
     * assert list.size() == origSize
     * assert origCopy.every{ list.contains(it) }
     * </pre>
     *
     * @param self a List
     * @see Collections#shuffle(List)
     * @since 3.0.0
     */
    public static void shuffle(List<?> self, Random rnd) {
        Collections.shuffle(self, rnd);
    }

    /**
     * Creates a new list containing the elements of the specified list
     * but in a random order.
     * <pre class="groovyTestCase">
     * def orig = ["a", 4, false]
     * def shuffled = orig.shuffled()
     * assert orig.size() == shuffled.size()
     * assert orig.every{ shuffled.contains(it) }
     * </pre>
     *
     * @param self a List
     * @see Collections#shuffle(List)
     * @since 3.0.0
     */
    public static <T> List<T> shuffled(List<T> self) {
        List<T> copy = new ArrayList(self);
        Collections.shuffle(copy);
        return copy;
    }

    /**
     * Creates a new list containing the elements of the specified list but in a random
     * order using the specified random instance as the source of randomness.
     * <pre class="groovyTestCase">
     * def r = new Random()
     * def orig = ["a", 4, false]
     * def shuffled = orig.shuffled(r)
     * assert orig.size() == shuffled.size()
     * assert orig.every{ shuffled.contains(it) }
     * </pre>
     *
     * @param self a List
     * @see Collections#shuffle(List)
     * @since 3.0.0
     */
    public static <T> List<T> shuffled(List<T> self, Random rnd) {
        List<T> copy = new ArrayList(self);
        Collections.shuffle(copy, rnd);
        return copy;
    }

    /**
     * Randomly reorders the elements of the specified array.
     * <pre class="groovyTestCase">
     * Integer[] array = [10, 5, 20]
     * def origSize = array.size()
     * def items = array.toList()
     * array.shuffle()
     * assert array.size() == origSize
     * assert items.every{ array.contains(it) }
     * </pre>
     *
     * @param self an array
     * @since 3.0.0
     */
    public static <T> void shuffle(T[] self) {
        Random rnd = r;
        if (rnd == null)
            r = rnd = new Random(); // harmless race.
        shuffle(self, rnd);
    }

    private static Random r;

    /**
     * Randomly reorders the elements of the specified array using the
     * specified random instance as the source of randomness.
     * <pre class="groovyTestCase">
     * def r = new Random()
     * Integer[] array = [10, 5, 20]
     * def origSize = array.size()
     * def items = array.toList()
     * array.shuffle(r)
     * assert array.size() == origSize
     * assert items.every{ array.contains(it) }
     * </pre>
     *
     * @param self an array
     * @since 3.0.0
     */
    public static <T> void shuffle(T[] self, Random rnd) {
        for (int i = 0; i < self.length-1; i++) {
            int nextIndex = rnd.nextInt(self.length);
            T tmp = self[i];
            self[i] = self[nextIndex];
            self[nextIndex] = tmp;
        }
    }

    /**
     * Creates a new array containing the elements of the specified array but in a random order.
     * <pre class="groovyTestCase">
     * Integer[] orig = [10, 5, 20]
     * def array = orig.shuffled()
     * assert orig.size() == array.size()
     * assert orig.every{ array.contains(it) }
     * </pre>
     *
     * @param self an array
     * @return the shuffled array
     * @since 3.0.0
     */
    public static <T> T[] shuffled(T[] self) {
        Random rnd = r;
        if (rnd == null)
            r = rnd = new Random(); // harmless race.
        return shuffled(self, rnd);
    }

    /**
     * Creates a new array containing the elements of the specified array but in a random
     * order using the specified random instance as the source of randomness.
     * <pre class="groovyTestCase">
     * def r = new Random()
     * Integer[] orig = [10, 5, 20]
     * def array = orig.shuffled(r)
     * assert orig.size() == array.size()
     * assert orig.every{ array.contains(it) }
     * </pre>
     *
     * @param self an array
     * @return the shuffled array
     * @since 3.0.0
     */
    public static <T> T[] shuffled(T[] self, Random rnd) {
        T[] result = self.clone();
        List<T> items = Arrays.asList(self);
        Collections.shuffle(items, rnd);
        System.arraycopy(items.toArray(), 0, result, 0, items.size());
        return result;
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
        System.arraycopy(items.toArray(), 0, self, 0, items.size());
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
        return (T[]) plus((List<T>) toList(left), toList(right)).toArray();
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
        return (T[]) plus((List<T>) toList(left), right).toArray();
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
        return (T[]) plus((List<T>) toList(left), toList(right)).toArray();
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
     * Create a Collection as a union of two iterables. If the left iterable
     * is a Set, then the returned collection will be a Set otherwise a List.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     * <pre class="groovyTestCase">assert [1,2,3,4] == [1,2] + [3,4]</pre>
     *
     * @param left  the left Iterable
     * @param right the right Iterable
     * @return the merged Collection
     * @since 2.4.0
     */
    public static <T> Collection<T> plus(Iterable<T> left, Iterable<T> right) {
        return plus(asCollection(left), asCollection(right));
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
        return plus(left, asCollection(right));
    }

    /**
     * Create a List as a union of a List and an Iterable.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     *
     * @param left  the left List
     * @param right the right Iterable
     * @return the merged List
     * @since 2.4.0
     * @see #plus(Collection, Collection)
     */
    public static <T> List<T> plus(List<T> left, Iterable<T> right) {
        return (List<T>) plus((Collection<T>) left, asCollection(right));
    }

    /**
     * Create a List as a union of a List and a Collection.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     *
     * @param left  the left List
     * @param right the right Collection
     * @return the merged List
     * @since 2.4.0
     * @see #plus(Collection, Collection)
     */
    public static <T> List<T> plus(List<T> left, Collection<T> right) {
        return (List<T>) plus((Collection<T>) left, right);
    }

    /**
     * Create a Set as a union of a Set and an Iterable.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     *
     * @param left  the left Set
     * @param right the right Iterable
     * @return the merged Set
     * @since 2.4.0
     * @see #plus(Collection, Collection)
     */
    public static <T> Set<T> plus(Set<T> left, Iterable<T> right) {
        return (Set<T>) plus((Collection<T>) left, asCollection(right));
    }

    /**
     * Create a Set as a union of a Set and a Collection.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     *
     * @param left  the left Set
     * @param right the right Collection
     * @return the merged Set
     * @since 2.4.0
     * @see #plus(Collection, Collection)
     */
    public static <T> Set<T> plus(Set<T> left, Collection<T> right) {
        return (Set<T>) plus((Collection<T>) left, right);
    }

    /**
     * Create a SortedSet as a union of a SortedSet and an Iterable.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     *
     * @param left  the left SortedSet
     * @param right the right Iterable
     * @return the merged SortedSet
     * @since 2.4.0
     * @see #plus(Collection, Collection)
     */
    public static <T> SortedSet<T> plus(SortedSet<T> left, Iterable<T> right) {
        return (SortedSet<T>) plus((Collection<T>) left, asCollection(right));
    }

    /**
     * Create a SortedSet as a union of a SortedSet and a Collection.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     *
     * @param left  the left SortedSet
     * @param right the right Collection
     * @return the merged SortedSet
     * @since 2.4.0
     * @see #plus(Collection, Collection)
     */
    public static <T> SortedSet<T> plus(SortedSet<T> left, Collection<T> right) {
        return (SortedSet<T>) plus((Collection<T>) left, right);
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
     * Create a collection as a union of an Iterable and an Object. If the iterable
     * is a Set, then the returned collection will be a Set otherwise a List.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     * <pre class="groovyTestCase">assert [1,2,3] == [1,2] + 3</pre>
     *
     * @param left  an Iterable
     * @param right an object to add/append
     * @return the resulting Collection
     * @since 2.4.0
     */
    public static <T> Collection<T> plus(Iterable<T> left, T right) {
        return plus(asCollection(left), right);
    }

    /**
     * Create a List as a union of a List and an Object.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     * <pre class="groovyTestCase">assert [1,2,3] == [1,2] + 3</pre>
     *
     * @param left  a List
     * @param right an object to add/append
     * @return the resulting List
     * @since 2.4.0
     */
    public static <T> List<T> plus(List<T> left, T right) {
        return (List<T>) plus((Collection<T>) left, right);
    }

    /**
     * Create a Set as a union of a Set and an Object.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     * <pre class="groovyTestCase">assert [1,2,3] == [1,2] + 3</pre>
     *
     * @param left  a Set
     * @param right an object to add/append
     * @return the resulting Set
     * @since 2.4.0
     */
    public static <T> Set<T> plus(Set<T> left, T right) {
        return (Set<T>) plus((Collection<T>) left, right);
    }

    /**
     * Create a SortedSet as a union of a SortedSet and an Object.
     * This operation will always create a new object for the result,
     * while the operands remain unchanged.
     * <pre class="groovyTestCase">assert [1,2,3] == [1,2] + 3</pre>
     *
     * @param left  a SortedSet
     * @param right an object to add/append
     * @return the resulting SortedSet
     * @since 2.4.0
     */
    public static <T> SortedSet<T> plus(SortedSet<T> left, T right) {
        return (SortedSet<T>) plus((Collection<T>) left, right);
    }

    /**
     * @deprecated use the Iterable variant instead
     * @see #multiply(Iterable, Number)
     * @since 1.0
     */
    @Deprecated
    public static <T> Collection<T> multiply(Collection<T> self, Number factor) {
        return multiply((Iterable<T>) self, factor);
    }

    /**
     * Create a Collection composed of the elements of this Iterable, repeated
     * a certain number of times.  Note that for non-primitive
     * elements, multiple references to the same instance will be added.
     * <pre class="groovyTestCase">assert [1,2,3,1,2,3] == [1,2,3] * 2</pre>
     *
     * Note: if the Iterable happens to not support duplicates, e.g. a Set, then the
     * method will effectively return a Collection with a single copy of the Iterable's items.
     *
     * @param self   an Iterable
     * @param factor the number of times to append
     * @return the multiplied Collection
     * @since 2.4.0
     */
    public static <T> Collection<T> multiply(Iterable<T> self, Number factor) {
        Collection<T> selfCol = asCollection(self);
        int size = factor.intValue();
        Collection<T> answer = createSimilarCollection(selfCol, selfCol.size() * size);
        for (int i = 0; i < size; i++) {
            answer.addAll(selfCol);
        }
        return answer;
    }

    /**
     * Create a List composed of the elements of this Iterable, repeated
     * a certain number of times.  Note that for non-primitive
     * elements, multiple references to the same instance will be added.
     * <pre class="groovyTestCase">assert [1,2,3,1,2,3] == [1,2,3] * 2</pre>
     *
     * Note: if the Iterable happens to not support duplicates, e.g. a Set, then the
     * method will effectively return a Collection with a single copy of the Iterable's items.
     *
     * @param self   a List
     * @param factor the number of times to append
     * @return the multiplied List
     * @since 2.4.0
     */
    public static <T> List<T> multiply(List<T> self, Number factor) {
        return (List<T>) multiply((Iterable<T>) self, factor);
    }

    /**
     * Create a Collection composed of the intersection of both collections.  Any
     * elements that exist in both collections are added to the resultant collection.
     * For collections of custom objects; the objects should implement java.lang.Comparable
     * <pre class="groovyTestCase">assert [4,5] == [1,2,3,4,5].intersect([4,5,6,7,8])</pre>
     * By default, Groovy uses a {@link NumberAwareComparator} when determining if an
     * element exists in both collections.
     *
     * @param left  a Collection
     * @param right a Collection
     * @return a Collection as an intersection of both collections
     * @see #intersect(Collection, Collection, Comparator)
     * @since 1.5.6
     */
    public static <T> Collection<T> intersect(Collection<T> left, Collection<T> right) {
        return intersect(left, right, new NumberAwareComparator<>());
    }

    /**
     * Create a Collection composed of the intersection of both collections.  Any
     * elements that exist in both collections are added to the resultant collection.
     * For collections of custom objects; the objects should implement java.lang.Comparable
     * <pre class="groovyTestCase">
     * assert [3,4] == [1,2,3,4].intersect([3,4,5,6], Comparator.naturalOrder())
     * </pre>
     * <pre class="groovyTestCase">
     * def one = ['a', 'B', 'c', 'd']
     * def two = ['b', 'C', 'd', 'e']
     * def compareIgnoreCase = { a, b {@code ->} a.toLowerCase() {@code <=>} b.toLowerCase() }
     * assert one.intersect(two) == ['d']
     * assert two.intersect(one) == ['d']
     * assert one.intersect(two, compareIgnoreCase) == ['b', 'C', 'd']
     * assert two.intersect(one, compareIgnoreCase) == ['B', 'c', 'd']
     * </pre>
     *
     * @param left  a Collection
     * @param right a Collection
     * @param comparator a Comparator
     * @return a Collection as an intersection of both collections
     * @since 2.5.0
     */
    public static <T> Collection<T> intersect(Collection<T> left, Collection<T> right, Comparator<T> comparator) {
        if (left.isEmpty() || right.isEmpty())
            return createSimilarCollection(left, 0);

        Collection<T> result = createSimilarCollection(left, Math.min(left.size(), right.size()));
        //creates the collection to look for values.
        Collection<T> pickFrom = new TreeSet<T>(comparator);
        pickFrom.addAll(left);

        for (final T t : right) {
            if (pickFrom.contains(t))
                result.add(t);
        }
        return result;
    }

    /**
     * Create a Collection composed of the intersection of both iterables.  Any
     * elements that exist in both iterables are added to the resultant collection.
     * For iterables of custom objects; the objects should implement java.lang.Comparable
     * <pre class="groovyTestCase">assert [4,5] == [1,2,3,4,5].intersect([4,5,6,7,8])</pre>
     * By default, Groovy uses a {@link NumberAwareComparator} when determining if an
     * element exists in both collections.
     *
     * @param left  an Iterable
     * @param right an Iterable
     * @return a Collection as an intersection of both iterables
     * @see #intersect(Iterable, Iterable, Comparator)
     * @since 2.4.0
     */
    public static <T> Collection<T> intersect(Iterable<T> left, Iterable<T> right) {
        return intersect(asCollection(left), asCollection(right));
    }

    /**
     * Create a Collection composed of the intersection of both iterables.  Any
     * elements that exist in both iterables are added to the resultant collection.
     * For iterables of custom objects; the objects should implement java.lang.Comparable
     * <pre class="groovyTestCase">assert [3,4] == [1,2,3,4].intersect([3,4,5,6], Comparator.naturalOrder())</pre>
     *
     * @param left  an Iterable
     * @param right an Iterable
     * @param comparator a Comparator
     * @return a Collection as an intersection of both iterables
     * @since 2.5.0
     */
    public static <T> Collection<T> intersect(Iterable<T> left, Iterable<T> right, Comparator<T> comparator) {
        return intersect(asCollection(left), asCollection(right), comparator);
    }

    /**
     * Create a List composed of the intersection of a List and an Iterable.  Any
     * elements that exist in both iterables are added to the resultant collection.
     * <pre class="groovyTestCase">assert [4,5] == [1,2,3,4,5].intersect([4,5,6,7,8])</pre>
     * By default, Groovy uses a {@link NumberAwareComparator} when determining if an
     * element exists in both collections.
     *
     * @param left  a List
     * @param right an Iterable
     * @return a List as an intersection of a List and an Iterable
     * @see #intersect(List, Iterable, Comparator)
     * @since 2.4.0
     */
    public static <T> List<T> intersect(List<T> left, Iterable<T> right) {
        return (List<T>) intersect((Collection<T>) left, asCollection(right));
    }

    /**
     * Create a List composed of the intersection of a List and an Iterable.  Any
     * elements that exist in both iterables are added to the resultant collection.
     * <pre class="groovyTestCase">assert [3,4] == [1,2,3,4].intersect([3,4,5,6])</pre>
     *
     * @param left  a List
     * @param right an Iterable
     * @param comparator a Comparator
     * @return a List as an intersection of a List and an Iterable
     * @since 2.5.0
     */
    public static <T> List<T> intersect(List<T> left, Iterable<T> right, Comparator<T> comparator) {
        return (List<T>) intersect((Collection<T>) left, asCollection(right), comparator);
    }

    /**
     * Create a Set composed of the intersection of a Set and an Iterable.  Any
     * elements that exist in both iterables are added to the resultant collection.
     * <pre class="groovyTestCase">assert [4,5] as Set == ([1,2,3,4,5] as Set).intersect([4,5,6,7,8])</pre>
     * By default, Groovy uses a {@link NumberAwareComparator} when determining if an
     * element exists in both collections.
     *
     * @param left  a Set
     * @param right an Iterable
     * @return a Set as an intersection of a Set and an Iterable
     * @see #intersect(Set, Iterable, Comparator)
     * @since 2.4.0
     */
    public static <T> Set<T> intersect(Set<T> left, Iterable<T> right) {
        return (Set<T>) intersect((Collection<T>) left, asCollection(right));
    }

    /**
     * Create a Set composed of the intersection of a Set and an Iterable.  Any
     * elements that exist in both iterables are added to the resultant collection.
     * <pre class="groovyTestCase">assert [3,4] as Set == ([1,2,3,4] as Set).intersect([3,4,5,6], Comparator.naturalOrder())</pre>
     *
     * @param left  a Set
     * @param right an Iterable
     * @param comparator a Comparator
     * @return a Set as an intersection of a Set and an Iterable
     * @since 2.5.0
     */
    public static <T> Set<T> intersect(Set<T> left, Iterable<T> right, Comparator<T> comparator) {
        return (Set<T>) intersect((Collection<T>) left, asCollection(right), comparator);
    }

    /**
     * Create a SortedSet composed of the intersection of a SortedSet and an Iterable.  Any
     * elements that exist in both iterables are added to the resultant collection.
     * <pre class="groovyTestCase">assert [4,5] as SortedSet == ([1,2,3,4,5] as SortedSet).intersect([4,5,6,7,8])</pre>
     * By default, Groovy uses a {@link NumberAwareComparator} when determining if an
     * element exists in both collections.
     *
     * @param left  a SortedSet
     * @param right an Iterable
     * @return a Set as an intersection of a SortedSet and an Iterable
     * @see #intersect(SortedSet, Iterable, Comparator)
     * @since 2.4.0
     */
    public static <T> SortedSet<T> intersect(SortedSet<T> left, Iterable<T> right) {
        return (SortedSet<T>) intersect((Collection<T>) left, asCollection(right));
    }

    /**
     * Create a SortedSet composed of the intersection of a SortedSet and an Iterable.  Any
     * elements that exist in both iterables are added to the resultant collection.
     * <pre class="groovyTestCase">assert [4,5] as SortedSet == ([1,2,3,4,5] as SortedSet).intersect([4,5,6,7,8])</pre>
     *
     * @param left  a SortedSet
     * @param right an Iterable
     * @param comparator a Comparator
     * @return a Set as an intersection of a SortedSet and an Iterable
     * @since 2.5.0
     */
    public static <T> SortedSet<T> intersect(SortedSet<T> left, Iterable<T> right, Comparator<T> comparator) {
        return (SortedSet<T>) intersect((Collection<T>) left, asCollection(right), comparator);
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
        if (right != null && !right.isEmpty()) {
            for (Map.Entry<K, V> e1 : left.entrySet()) {
                for (Map.Entry<K, V> e2 : right.entrySet()) {
                    if (DefaultTypeTransformation.compareEqual(e1, e2)) {
                        ansMap.put(e1.getKey(), e1.getValue());
                    }
                }
            }
        }
        return ansMap;
    }

    /**
     * Returns <code>true</code> if the intersection of two iterables is empty.
     * <pre class="groovyTestCase">assert [1,2,3].disjoint([3,4,5]) == false</pre>
     * <pre class="groovyTestCase">assert [1,2].disjoint([3,4]) == true</pre>
     *
     * @param left  an Iterable
     * @param right an Iterable
     * @return boolean   <code>true</code> if the intersection of two iterables
     *         is empty, <code>false</code> otherwise.
     * @since 2.4.0
     */
    public static boolean disjoint(Iterable left, Iterable right) {
        Collection leftCol = asCollection(left);
        Collection rightCol = asCollection(right);

        if (leftCol.isEmpty() || rightCol.isEmpty())
            return true;

        Collection pickFrom = new TreeSet(new NumberAwareComparator());
        pickFrom.addAll(rightCol);

        for (final Object o : leftCol) {
            if (pickFrom.contains(o))
                return false;
        }
        return true;
    }

    /**
     * @deprecated use the Iterable variant instead
     * @see #disjoint(Iterable, Iterable)
     * @since 1.0
     */
    @Deprecated
    public static boolean disjoint(Collection left, Collection right) {
        return disjoint(left, right);
    }

    /**
     * Chops the array into pieces, returning lists with sizes corresponding to the supplied chop sizes.
     * If the array isn't large enough, truncated (possibly empty) pieces are returned.
     * Using a chop size of -1 will cause that piece to contain all remaining items from the array.
     *
     * @param self      an Array to be chopped
     * @param chopSizes the sizes for the returned pieces
     * @return a list of lists chopping the original array elements into pieces determined by chopSizes
     * @see #collate(Object[], int) to chop a list into pieces of a fixed size
     * @since 2.5.2
     */
    public static <T> List<List<T>> chop(T[] self, int... chopSizes) {
        return chop(Arrays.asList(self), chopSizes);
    }

    /**
     * Chops the Iterable into pieces, returning lists with sizes corresponding to the supplied chop sizes.
     * If the Iterable isn't large enough, truncated (possibly empty) pieces are returned.
     * Using a chop size of -1 will cause that piece to contain all remaining items from the Iterable.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * assert [1, 2, 3, 4].chop(1) == [[1]]
     * assert [1, 2, 3, 4].chop(1,-1) == [[1], [2, 3, 4]]
     * assert ('a'..'h').chop(2, 4) == [['a', 'b'], ['c', 'd', 'e', 'f']]
     * assert ['a', 'b', 'c', 'd', 'e'].chop(3) == [['a', 'b', 'c']]
     * assert ['a', 'b', 'c', 'd', 'e'].chop(1, 2, 3) == [['a'], ['b', 'c'], ['d', 'e']]
     * assert ['a', 'b', 'c', 'd', 'e'].chop(1, 2, 3, 3, 3) == [['a'], ['b', 'c'], ['d', 'e'], [], []]
     * </pre>
     *
     * @param self      an Iterable to be chopped
     * @param chopSizes the sizes for the returned pieces
     * @return a list of lists chopping the original iterable into pieces determined by chopSizes
     * @see #collate(Iterable, int) to chop an Iterable into pieces of a fixed size
     * @since 2.5.2
     */
    public static <T> List<List<T>> chop(Iterable<T> self, int... chopSizes) {
        return chop(self.iterator(), chopSizes);
    }

    /**
     * Chops the iterator items into pieces, returning lists with sizes corresponding to the supplied chop sizes.
     * If the iterator is exhausted early, truncated (possibly empty) pieces are returned.
     * Using a chop size of -1 will cause that piece to contain all remaining items from the iterator.
     *
     * @param self      an Iterator to be chopped
     * @param chopSizes the sizes for the returned pieces
     * @return a list of lists chopping the original iterator elements into pieces determined by chopSizes
     * @since 2.5.2
     */
    public static <T> List<List<T>> chop(Iterator<T> self, int... chopSizes) {
        List<List<T>> result = new ArrayList<List<T>>();
        for (Integer nextSize : chopSizes) {
            int size = nextSize;
            List<T> next = new ArrayList<T>();
            while (size-- != 0 && self.hasNext()) {
                next.add(self.next());
            }
            result.add(next);
        }
        return result;
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
     * <p>
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
        return otherItems.isEmpty();
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
        for (Object o : self.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Object key = entry.getKey();
            Object value = entry.getValue();

            if (!coercedEquals(value, other.get(key))) {
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
        return minus(self, asCollection(removeMe));
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
     * Create a SortedSet composed of the elements of the first SortedSet minus the
     * elements of the given Collection.
     *
     * @param self     a SortedSet object
     * @param removeMe the items to remove from the SortedSet
     * @return the resulting SortedSet
     * @since 2.4.0
     */
    public static <T> SortedSet<T> minus(SortedSet<T> self, Collection<?> removeMe) {
        return (SortedSet<T>) minus((Set<T>) self, removeMe);
    }

    /**
     * Create a SortedSet composed of the elements of the first SortedSet minus the
     * elements of the given Iterable.
     *
     * @param self     a SortedSet object
     * @param removeMe the items to remove from the SortedSet
     * @return the resulting SortedSet
     * @since 2.4.0
     */
    public static <T> SortedSet<T> minus(SortedSet<T> self, Iterable<?> removeMe) {
        return (SortedSet<T>) minus((Set<T>) self, removeMe);
    }

    /**
     * Create a SortedSet composed of the elements of the first SortedSet minus the given element.
     *
     * @param self     a SortedSet object
     * @param removeMe the element to remove from the SortedSet
     * @return the resulting SortedSet
     * @since 2.4.0
     */
    public static <T> SortedSet<T> minus(SortedSet<T> self, Object removeMe) {
        return (SortedSet<T>) minus((Set<T>) self, removeMe);
    }

    /**
     * Create an array composed of the elements of the first array minus the
     * elements of the given Iterable.
     *
     * @param self     an array
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
     * @param self     an array
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
        return (List<T>) minus((Collection<T>) self, removeMe);
    }

    /**
     * Create a new Collection composed of the elements of the first Collection minus
     * every occurrence of elements of the given Collection.
     * <pre class="groovyTestCase">assert [1, "a", true, true, false, 5.3] - [true, 5.3] == [1, "a", false]</pre>
     *
     * @param self     a Collection
     * @param removeMe a Collection of elements to remove
     * @return a Collection with the given elements removed
     * @since 2.4.0
     */
    public static <T> Collection<T> minus(Collection<T> self, Collection<?> removeMe) {
        Collection<T> ansCollection = createSimilarCollection(self);
        if (self.isEmpty())
            return ansCollection;
        T head = self.iterator().next();

        boolean nlgnSort = sameType(new Collection[]{self, removeMe});

        // We can't use the same tactic as for intersection
        // since AbstractCollection only does a remove on the first
        // element it encounters.

        Comparator<T> numberComparator = new NumberAwareComparator<T>();

        if (nlgnSort && (head instanceof Comparable)) {
            //n*LOG(n) version
            Set<T> answer;
            if (head instanceof Number) {
                answer = new TreeSet<T>(numberComparator);
                answer.addAll(self);
                for (T t : self) {
                    if (t instanceof Number) {
                        for (Object t2 : removeMe) {
                            if (t2 instanceof Number) {
                                if (numberComparator.compare(t, (T) t2) == 0)
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

            for (T o : self) {
                if (answer.contains(o))
                    ansCollection.add(o);
            }
        } else {
            //n*n version
            List<T> tmpAnswer = new LinkedList<T>(self);
            for (Iterator<T> iter = tmpAnswer.iterator(); iter.hasNext();) {
                T element = iter.next();
                boolean elementRemoved = false;
                for (Iterator<?> iterator = removeMe.iterator(); iterator.hasNext() && !elementRemoved;) {
                    Object elt = iterator.next();
                    if (DefaultTypeTransformation.compareEqual(element, elt)) {
                        iter.remove();
                        elementRemoved = true;
                    }
                }
            }

            //remove duplicates
            //can't use treeset since the base classes are different
            ansCollection.addAll(tmpAnswer);
        }
        return ansCollection;
    }

    /**
     * Create a new List composed of the elements of the first List minus
     * every occurrence of elements of the given Iterable.
     * <pre class="groovyTestCase">assert [1, "a", true, true, false, 5.3] - [true, 5.3] == [1, "a", false]</pre>
     *
     * @param self     a List
     * @param removeMe an Iterable of elements to remove
     * @return a new List with the given elements removed
     * @since 1.8.7
     */
    public static <T> List<T> minus(List<T> self, Iterable<?> removeMe) {
        return (List<T>) minus((Iterable<T>) self, removeMe);
    }

    /**
     * Create a new Collection composed of the elements of the first Iterable minus
     * every occurrence of elements of the given Iterable.
     * <pre class="groovyTestCase">
     * assert [1, "a", true, true, false, 5.3] - [true, 5.3] == [1, "a", false]
     * </pre>
     *
     * @param self     an Iterable
     * @param removeMe an Iterable of elements to remove
     * @return a new Collection with the given elements removed
     * @since 2.4.0
     */
    public static <T> Collection<T> minus(Iterable<T> self, Iterable<?> removeMe) {
        return minus(asCollection(self), asCollection(removeMe));
    }

    /**
     * Create a new List composed of the elements of the first List minus every occurrence of the
     * given element to remove.
     * <pre class="groovyTestCase">assert ["a", 5, 5, true] - 5 == ["a", true]</pre>
     *
     * @param self     a List object
     * @param removeMe an element to remove from the List
     * @return the resulting List with the given element removed
     * @since 1.0
     */
    public static <T> List<T> minus(List<T> self, Object removeMe) {
        return (List<T>) minus((Iterable<T>) self, removeMe);
    }

    /**
     * Create a new Collection composed of the elements of the first Iterable minus every occurrence of the
     * given element to remove.
     * <pre class="groovyTestCase">assert ["a", 5, 5, true] - 5 == ["a", true]</pre>
     *
     * @param self     an Iterable object
     * @param removeMe an element to remove from the Iterable
     * @return the resulting Collection with the given element removed
     * @since 2.4.0
     */
    public static <T> Collection<T> minus(Iterable<T> self, Object removeMe) {
        Collection<T> ansList = createSimilarCollection(self);
        for (T t : self) {
            if (!coercedEquals(t, removeMe)) ansList.add(t);
        }
        return ansList;
    }

    /**
     * Create a new object array composed of the elements of the first array
     * minus the element to remove.
     *
     * @param self    an array
     * @param removeMe an element to remove from the array
     * @return a new array with the operand removed
     * @since 1.5.5
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] minus(T[] self, Object removeMe) {
        return (T[]) minus((Iterable<T>) toList(self), removeMe).toArray();
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
        if (removeMe != null && !removeMe.isEmpty()) {
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
     * Flatten a Collection.  This Collection and any nested arrays or
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
     * Flatten an Iterable.  This Iterable and any nested arrays or
     * collections have their contents (recursively) added to the new collection.
     * <pre class="groovyTestCase">assert [1,2,3,4,5] == [1,[2,3],[[4]],[],5].flatten()</pre>
     *
     * @param self an Iterable to flatten
     * @return a flattened Collection
     * @since 1.6.0
     */
    public static Collection<?> flatten(Iterable<?> self) {
        return flatten(self, createSimilarCollection(self));
    }

    /**
     * Flatten a List.  This List and any nested arrays or
     * collections have their contents (recursively) added to the new List.
     * <pre class="groovyTestCase">assert [1,2,3,4,5] == [1,[2,3],[[4]],[],5].flatten()</pre>
     *
     * @param self a List to flatten
     * @return a flattened List
     * @since 2.4.0
     */
    public static List<?> flatten(List<?> self) {
        return (List<?>) flatten((Collection<?>) self);
    }

    /**
     * Flatten a Set.  This Set and any nested arrays or
     * collections have their contents (recursively) added to the new Set.
     * <pre class="groovyTestCase">assert [1,2,3,4,5] as Set == ([1,[2,3],[[4]],[],5] as Set).flatten()</pre>
     *
     * @param self a Set to flatten
     * @return a flattened Set
     * @since 2.4.0
     */
    public static Set<?> flatten(Set<?> self) {
        return (Set<?>) flatten((Collection<?>) self);
    }

    /**
     * Flatten a SortedSet.  This SortedSet and any nested arrays or
     * collections have their contents (recursively) added to the new SortedSet.
     * <pre class="groovyTestCase">
     * Set nested = [[0,1],[2],3,[4],5]
     * SortedSet sorted = new TreeSet({ a, b {@code ->} (a instanceof List ? a[0] : a) {@code <=>} (b instanceof List ? b[0] : b) } as Comparator)
     * sorted.addAll(nested)
     * assert [0,1,2,3,4,5] as SortedSet == sorted.flatten()
     * </pre>
     *
     * @param self a SortedSet to flatten
     * @return a flattened SortedSet
     * @since 2.4.0
     */
    public static SortedSet<?> flatten(SortedSet<?> self) {
        return (SortedSet<?>) flatten((Collection<?>) self);
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

    private static Collection flatten(Iterable elements, Collection addTo) {
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
     * @deprecated Use the Iterable version of flatten instead
     * @see #flatten(Iterable, Closure)
     * @since 1.6.0
     */
    @Deprecated
    public static <T> Collection<T> flatten(Collection<T> self, Closure<? extends T> flattenUsing) {
        return flatten(self, createSimilarCollection(self), flattenUsing);
    }

    /**
     * Flatten an Iterable.  This Iterable and any nested arrays or
     * collections have their contents (recursively) added to the new collection.
     * For any non-Array, non-Collection object which represents some sort
     * of collective type, the supplied closure should yield the contained items;
     * otherwise, the closure should just return any element which corresponds to a leaf.
     *
     * @param self an Iterable
     * @param flattenUsing a closure to determine how to flatten non-Array, non-Collection elements
     * @return a flattened Collection
     * @since 1.6.0
     */
    public static <T> Collection<T> flatten(Iterable<T> self, Closure<? extends T> flattenUsing) {
        return flatten(self, createSimilarCollection(self), flattenUsing);
    }

    private static <T> Collection<T> flatten(Iterable elements, Collection<T> addTo, Closure<? extends T> flattenUsing) {
        for (Object element : elements) {
            if (element instanceof Collection) {
                flatten((Collection) element, addTo, flattenUsing);
            } else if (element != null && element.getClass().isArray()) {
                flatten(DefaultTypeTransformation.arrayAsCollection(element), addTo, flattenUsing);
            } else {
                T flattened = flattenUsing.call(new Object[]{element});
                boolean returnedSelf = flattened == element;
                if (!returnedSelf && flattened instanceof Collection) {
                    List<?> list = toList((Iterable<?>) flattened);
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
     * list &lt;&lt; 3
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
     * objects to a List.
     * <pre class="groovyTestCase">def list = [1,2]
     * list &lt;&lt; 3
     * assert list == [1,2,3]</pre>
     *
     * @param self  a List
     * @param value an Object to be added to the List.
     * @return same List, after the value was added to it.
     * @since 2.4.0
     */
    public static <T> List<T> leftShift(List<T> self, T value) {
        return (List<T>) leftShift((Collection<T>) self, value);
    }

    /**
     * Overloads the left shift operator to provide an easy way to append
     * objects to a Set.
     * <pre class="groovyTestCase">def set = [1,2] as Set
     * set &lt;&lt; 3
     * assert set == [1,2,3] as Set</pre>
     *
     * @param self  a Set
     * @param value an Object to be added to the Set.
     * @return same Set, after the value was added to it.
     * @since 2.4.0
     */
    public static <T> Set<T> leftShift(Set<T> self, T value) {
        return (Set<T>) leftShift((Collection<T>) self, value);
    }

    /**
     * Overloads the left shift operator to provide an easy way to append
     * objects to a SortedSet.
     * <pre class="groovyTestCase">def set = [1,2] as SortedSet
     * set &lt;&lt; 3
     * assert set == [1,2,3] as SortedSet</pre>
     *
     * @param self  a SortedSet
     * @param value an Object to be added to the SortedSet.
     * @return same SortedSet, after the value was added to it.
     * @since 2.4.0
     */
    public static <T> SortedSet<T> leftShift(SortedSet<T> self, T value) {
        return (SortedSet<T>) leftShift((Collection<T>) self, value);
    }

    /**
     * Overloads the left shift operator to provide an easy way to append
     * objects to a BlockingQueue.
     * In case of bounded queue the method will block till space in the queue become available
     * <pre class="groovyTestCase">def list = new java.util.concurrent.LinkedBlockingQueue ()
     * list &lt;&lt; 3 &lt;&lt; 2 &lt;&lt; 1
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
     * <code>map1 &lt;&lt; map2</code>; otherwise it's just a synonym for
     * <code>putAll</code> though it returns the original map rather than
     * being a <code>void</code> method. Example usage:
     * <pre class="groovyTestCase">def map = [a:1, b:2]
     * map &lt;&lt; [c:3, d:4]
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
        RangeInfo info = subListBorders(array.length, range);
        List<Byte> answer = primitiveArrayGet(array, new IntRange(true, info.from, info.to - 1));
        return info.reverse ? reverse(answer) : answer;
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
        RangeInfo info = subListBorders(array.length, range);
        List<Character> answer = primitiveArrayGet(array, new IntRange(true, info.from, info.to - 1));
        return info.reverse ? reverse(answer) : answer;
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
        RangeInfo info = subListBorders(array.length, range);
        List<Short> answer = primitiveArrayGet(array, new IntRange(true, info.from, info.to - 1));
        return info.reverse ? reverse(answer) : answer;
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
        RangeInfo info = subListBorders(array.length, range);
        List<Integer> answer = primitiveArrayGet(array, new IntRange(true, info.from, info.to - 1));
        return info.reverse ? reverse(answer) : answer;
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
        RangeInfo info = subListBorders(array.length, range);
        List<Long> answer = primitiveArrayGet(array, new IntRange(true, info.from, info.to - 1));
        return info.reverse ? reverse(answer) : answer;
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
        RangeInfo info = subListBorders(array.length, range);
        List<Float> answer = primitiveArrayGet(array, new IntRange(true, info.from, info.to - 1));
        return info.reverse ? reverse(answer) : answer;
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
        RangeInfo info = subListBorders(array.length, range);
        List<Double> answer = primitiveArrayGet(array, new IntRange(true, info.from, info.to - 1));
        return info.reverse ? reverse(answer) : answer;
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
        RangeInfo info = subListBorders(array.length, range);
        List<Boolean> answer = primitiveArrayGet(array, new IntRange(true, info.from, info.to - 1));
        return info.reverse ? reverse(answer) : answer;
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
        int i = normaliseIndex(index, self.length());
        return self.get(i);
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
        RangeInfo info = subListBorders(self.length(), range);
        BitSet result = new BitSet();

        int numberOfBits = info.to - info.from;
        int adjuster = 1;
        int offset = info.from;

        if (info.reverse) {
            adjuster = -1;
            offset = info.to - 1;
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
        RangeInfo info = subListBorders(self.length(), range);
        self.set(info.from, info.to, value);
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
     * Convert an Iterable to a Set. Always returns a new Set
     * even if the Iterable is already a Set.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * def result = [1, 2, 2, 2, 3].toSet()
     * assert result instanceof Set
     * assert result == [1, 2, 3] as Set
     * </pre>
     *
     * @param self an Iterable
     * @return a Set
     * @since 2.4.0
     */
    public static <T> Set<T> toSet(Iterable<T> self) {
        return toSet(self.iterator());
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
     * Compare a BigDecimal to another.
     * A fluent api style alias for {@code compareTo}.
     *
     * @param left  a BigDecimal
     * @param right a BigDecimal
     * @return true if left is equal to or bigger than right
     * @since 3.0.1
     */
    public static Boolean isAtLeast(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) >= 0;
    }

    /**
     * Compare a BigDecimal to a String representing a number.
     * A fluent api style alias for {@code compareTo}.
     *
     * @param left  a BigDecimal
     * @param right a String representing a number
     * @return true if left is equal to or bigger than the value represented by right
     * @since 3.0.1
     */
    public static Boolean isAtLeast(BigDecimal left, String right) {
        return isAtLeast(left, new BigDecimal(right));
    }

    /**
     * Power of a Number to a certain exponent. Called by the '**' operator.
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
     * Power of a BigDecimal to an integer certain exponent. If the
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
     * Power of a BigInteger to an integer certain exponent. If the
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
     * Power of an integer to an integer certain exponent. If the
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
     * Power of a long to an integer certain exponent. If the
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
     * Power of a BigInteger to a BigInteger certain exponent. Called by the '**' operator.
     *
     * @param self     a BigInteger
     * @param exponent a BigInteger exponent
     * @return a BigInteger to the power of a the exponent
     * @since 2.3.8
     */
    public static BigInteger power(BigInteger self, BigInteger exponent) {
        if ((exponent.signum() >= 0) && (exponent.compareTo(BI_INT_MAX) <= 0)) {
            return self.pow(exponent.intValue());
        } else {
            return BigDecimal.valueOf(Math.pow(self.doubleValue(), exponent.doubleValue())).toBigInteger();
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
     * Bitwise NEGATE a Number.
     *
     * @param left a Number
     * @return the bitwise NEGATE of the Number
     * @since 2.2.0
     */
    public static Number bitwiseNegate(Number left) {
        return NumberMath.bitwiseNegate(left);
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
     * Bitwise XOR together two Numbers.  Called when the '^' operator is used.
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
     * Returns the number, effectively being a noop for numbers.
     * Operator overloaded form of the '+' operator when it preceeds
     * a single operand, i.e. <code>+10</code>
     *
     * @param left a Number
     * @return the number
     * @since 2.2.0
     */
    public static Number unaryPlus(Number left) {
        return NumberMath.unaryPlus(left);
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
    public static void times(Number self, @ClosureParams(value=SimpleType.class,options="int")  Closure closure) {
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
    public static void upto(Number self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        int self1 = self.intValue();
        int to1 = to.intValue();
        if (self1 <= to1) {
            for (int i = self1; i <= to1; i++) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to upto() cannot be less than the value (" + self + ") it's called on.");
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
    public static void upto(long self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        long to1 = to.longValue();
        if (self <= to1) {
            for (long i = self; i <= to1; i++) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to upto() cannot be less than the value (" + self + ") it's called on.");
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
    public static void upto(Long self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        long to1 = to.longValue();
        if (self <= to1) {
            for (long i = self; i <= to1; i++) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to upto() cannot be less than the value (" + self + ") it's called on.");
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
    public static void upto(float self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        float to1 = to.floatValue();
        if (self <= to1) {
            for (float i = self; i <= to1; i++) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to upto() cannot be less than the value (" + self + ") it's called on.");
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
    public static void upto(Float self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        float to1 = to.floatValue();
        if (self <= to1) {
            for (float i = self; i <= to1; i++) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to upto() cannot be less than the value (" + self + ") it's called on.");
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
    public static void upto(double self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        double to1 = to.doubleValue();
        if (self <= to1) {
            for (double i = self; i <= to1; i++) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to upto() cannot be less than the value (" + self + ") it's called on.");
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
    public static void upto(Double self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        double to1 = to.doubleValue();
        if (self <= to1) {
            for (double i = self; i <= to1; i++) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to upto() cannot be less than the value (" + self + ") it's called on.");
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
    public static void upto(BigInteger self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        if (to instanceof BigDecimal) {
            final BigDecimal one = BigDecimal.valueOf(10, 1);
            BigDecimal self1 = new BigDecimal(self);
            BigDecimal to1 = (BigDecimal) to;
            if (self1.compareTo(to1) <= 0) {
                for (BigDecimal i = self1; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException(
                        MessageFormat.format(
                                "The argument ({0}) to upto() cannot be less than the value ({1}) it''s called on.",
                                to, self));
        } else if (to instanceof BigInteger) {
            final BigInteger one = BigInteger.valueOf(1);
            BigInteger to1 = (BigInteger) to;
            if (self.compareTo(to1) <= 0) {
                for (BigInteger i = self; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException(
                        MessageFormat.format("The argument ({0}) to upto() cannot be less than the value ({1}) it''s called on.",
                                to, self));
        } else {
            final BigInteger one = BigInteger.valueOf(1);
            BigInteger to1 = new BigInteger(to.toString());
            if (self.compareTo(to1) <= 0) {
                for (BigInteger i = self; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException(MessageFormat.format(
                        "The argument ({0}) to upto() cannot be less than the value ({1}) it''s called on.",
                        to, self));
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
    public static void upto(BigDecimal self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        final BigDecimal one = BigDecimal.valueOf(10, 1);  // That's what you get for "1.0".
        if (to instanceof BigDecimal) {
            BigDecimal to1 = (BigDecimal) to;
            if (self.compareTo(to1) <= 0) {
                for (BigDecimal i = self; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("The argument (" + to +
                        ") to upto() cannot be less than the value (" + self + ") it's called on.");
        } else if (to instanceof BigInteger) {
            BigDecimal to1 = new BigDecimal((BigInteger) to);
            if (self.compareTo(to1) <= 0) {
                for (BigDecimal i = self; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("The argument (" + to +
                        ") to upto() cannot be less than the value (" + self + ") it's called on.");
        } else {
            BigDecimal to1 = new BigDecimal(to.toString());
            if (self.compareTo(to1) <= 0) {
                for (BigDecimal i = self; i.compareTo(to1) <= 0; i = i.add(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("The argument (" + to +
                        ") to upto() cannot be less than the value (" + self + ") it's called on.");
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
    public static void downto(Number self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        int self1 = self.intValue();
        int to1 = to.intValue();
        if (self1 >= to1) {
            for (int i = self1; i >= to1; i--) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to downto() cannot be greater than the value (" + self + ") it's called on.");
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
    public static void downto(long self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        long to1 = to.longValue();
        if (self >= to1) {
            for (long i = self; i >= to1; i--) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to downto() cannot be greater than the value (" + self + ") it's called on.");
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
    public static void downto(Long self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        long to1 = to.longValue();
        if (self >= to1) {
            for (long i = self; i >= to1; i--) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to downto() cannot be greater than the value (" + self + ") it's called on.");
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
    public static void downto(float self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        float to1 = to.floatValue();
        if (self >= to1) {
            for (float i = self; i >= to1; i--) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to downto() cannot be greater than the value (" + self + ") it's called on.");    }

    /**
     * Iterates from this number down to the given number, inclusive,
     * decrementing by one each time.
     *
     * @param self    a Float
     * @param to the end number
     * @param closure the code to execute for each number
     * @since 1.0
     */
    public static void downto(Float self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        float to1 = to.floatValue();
        if (self >= to1) {
            for (float i = self; i >= to1; i--) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to downto() cannot be greater than the value (" + self + ") it's called on.");    }

    /**
     * Iterates from this number down to the given number, inclusive,
     * decrementing by one each time.
     *
     * @param self    a double
     * @param to the end number
     * @param closure the code to execute for each number
     * @since 1.0
     */
    public static void downto(double self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        double to1 = to.doubleValue();
        if (self >= to1) {
            for (double i = self; i >= to1; i--) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to downto() cannot be greater than the value (" + self + ") it's called on.");    }

    /**
     * Iterates from this number down to the given number, inclusive,
     * decrementing by one each time.
     *
     * @param self    a Double
     * @param to the end number
     * @param closure the code to execute for each number
     * @since 1.0
     */
    public static void downto(Double self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        double to1 = to.doubleValue();
        if (self >= to1) {
            for (double i = self; i >= to1; i--) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to downto() cannot be greater than the value (" + self + ") it's called on.");    }

    /**
     * Iterates from this number down to the given number, inclusive,
     * decrementing by one each time.
     *
     * @param self    a BigInteger
     * @param to the end number
     * @param closure the code to execute for each number
     * @since 1.0
     */
    public static void downto(BigInteger self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        if (to instanceof BigDecimal) {
            final BigDecimal one = BigDecimal.valueOf(10, 1);  // That's what you get for "1.0".
            final BigDecimal to1 = (BigDecimal) to;
            final BigDecimal selfD = new BigDecimal(self);
            if (selfD.compareTo(to1) >= 0) {
                for (BigDecimal i = selfD; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i.toBigInteger());
                }
            } else
                throw new GroovyRuntimeException(
                        MessageFormat.format(
                                "The argument ({0}) to downto() cannot be greater than the value ({1}) it''s called on.",
                                to, self));
        } else if (to instanceof BigInteger) {
            final BigInteger one = BigInteger.valueOf(1);
            final BigInteger to1 = (BigInteger) to;
            if (self.compareTo(to1) >= 0) {
                for (BigInteger i = self; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException(
                        MessageFormat.format(
                                "The argument ({0}) to downto() cannot be greater than the value ({1}) it''s called on.",
                                to, self));
        } else {
            final BigInteger one = BigInteger.valueOf(1);
            final BigInteger to1 = new BigInteger(to.toString());
            if (self.compareTo(to1) >= 0) {
                for (BigInteger i = self; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException(
                        MessageFormat.format("The argument ({0}) to downto() cannot be greater than the value ({1}) it''s called on.",
                                to, self));
        }
    }

    /**
     * Iterates from this number down to the given number, inclusive,
     * decrementing by one each time.  Each number is passed to the closure.
     * Example:
     * <pre>
     * 10.5.downto(0) {
     *   println it
     * }
     * </pre>
     * Prints numbers 10.5, 9.5 ... to 0.5.
     *
     * @param self    a BigDecimal
     * @param to the end number
     * @param closure the code to execute for each number
     * @since 1.0
     */
    public static void downto(BigDecimal self, Number to, @ClosureParams(FirstParam.class) Closure closure) {
        final BigDecimal one = BigDecimal.valueOf(10, 1);  // Quick way to get "1.0".
        if (to instanceof BigDecimal) {
            BigDecimal to1 = (BigDecimal) to;
            if (self.compareTo(to1) >= 0) {
                for (BigDecimal i = self; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("The argument (" + to +
                        ") to downto() cannot be greater than the value (" + self + ") it's called on.");        } else if (to instanceof BigInteger) {
            BigDecimal to1 = new BigDecimal((BigInteger) to);
            if (self.compareTo(to1) >= 0) {
                for (BigDecimal i = self; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("The argument (" + to +
                        ") to downto() cannot be greater than the value (" + self + ") it's called on.");        } else {
            BigDecimal to1 = new BigDecimal(to.toString());
            if (self.compareTo(to1) >= 0) {
                for (BigDecimal i = self; i.compareTo(to1) >= 0; i = i.subtract(one)) {
                    closure.call(i);
                }
            } else
                throw new GroovyRuntimeException("The argument (" + to +
                        ") to downto() cannot be greater than the value (" + self + ") it's called on.");        }
    }

    /**
     * Iterates from this number up to the given number using a step increment.
     * Each intermediate number is passed to the given closure.  Example:
     * <pre>
     * 0.step( 10, 2 ) {
     *   println it
     * }
     * </pre>
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
        return Math.abs(number);
    }

    /**
     * Get the absolute value
     *
     * @param number a Float
     * @return the absolute value of that Float
     * @since 1.0
     */
    public static float abs(Float number) {
        return Math.abs(number);
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
        return Math.round(number);
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
        final double p = Math.pow(10, precision);
        final double n = number.doubleValue() * p;
        if (number < 0f) {
            return (float) (Math.ceil(n) / p);
        }
        return (float) (Math.floor(n) / p);
    }

    /**
     * Truncate the value
     *
     * @param number a Float
     * @return the Float truncated to 0 decimal places
     * @since 1.6.0
     */
    public static float trunc(Float number) {
        if (number < 0f) {
            return (float)Math.ceil(number.doubleValue());
        }
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
     * @return the Double truncated to 0 decimal places
     * @since 1.6.4
     */
    public static double trunc(Double number) {
        if (number < 0d) {
            return Math.ceil(number);
        }
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
        if (number < 0d) {
            return Math.ceil(number *Math.pow(10,precision))/Math.pow(10,precision);
        }
        return Math.floor(number *Math.pow(10,precision))/Math.pow(10,precision);
    }

    /**
     * Round the value
     * <p>
     * Note that this method differs from {@link java.math.BigDecimal#round(java.math.MathContext)}
     * which specifies the digits to retain starting from the leftmost nonzero
     * digit. This methods rounds the integral part to the nearest whole number.
     *
     * @param number a BigDecimal
     * @return the rounded value of that BigDecimal
     * @see #round(java.math.BigDecimal, int)
     * @see java.math.BigDecimal#round(java.math.MathContext)
     * @since 2.5.0
     */
    public static BigDecimal round(BigDecimal number) {
        return round(number, 0);
    }

    /**
     * Round the value
     * <p>
     * Note that this method differs from {@link java.math.BigDecimal#round(java.math.MathContext)}
     * which specifies the digits to retain starting from the leftmost nonzero
     * digit. This method operates on the fractional part of the number and
     * the precision argument specifies the number of digits to the right of
     * the decimal point to retain.
     *
     * @param number a BigDecimal
     * @param precision the number of decimal places to keep
     * @return a BigDecimal rounded to the number of decimal places specified by precision
     * @see #round(java.math.BigDecimal)
     * @see java.math.BigDecimal#round(java.math.MathContext)
     * @since 2.5.0
     */
    public static BigDecimal round(BigDecimal number, int precision) {
        return number.setScale(precision, RoundingMode.HALF_UP);
    }

    /**
     * Truncate the value
     *
     * @param number a BigDecimal
     * @return a BigDecimal truncated to 0 decimal places
     * @see #trunc(java.math.BigDecimal, int)
     * @since 2.5.0
     */
    public static BigDecimal trunc(BigDecimal number) {
        return trunc(number, 0);
    }

    /**
     * Truncate the value
     *
     * @param number a BigDecimal
     * @param precision the number of decimal places to keep
     * @return a BigDecimal truncated to the number of decimal places specified by precision
     * @see #trunc(java.math.BigDecimal)
     * @since 2.5.0
     */
    public static BigDecimal trunc(BigDecimal number, int precision) {
        return number.setScale(precision, RoundingMode.DOWN);
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
     * @return a Long
     * @since 1.0
     */
    public static Long toLong(Number self) {
        return self.longValue();
    }

    /**
     * Transform a Number into a Float
     *
     * @param self a Number
     * @return a Float
     * @since 1.0
     */
    public static Float toFloat(Number self) {
        return self.floatValue();
    }

    /**
     * Transform a Number into a Double
     *
     * @param self a Number
     * @return a Double
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
     * @return a BigDecimal
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
     * @return a BigInteger
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
        return left && Boolean.TRUE.equals(right);
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
        return left || Boolean.TRUE.equals(right);
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
        return !left || Boolean.TRUE.equals(right);
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
        return left ^ Boolean.TRUE.equals(right);
    }

//    public static Boolean negate(Boolean left) {
//        return Boolean.valueOf(!left.booleanValue());
//    }

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
     * Traverse through each byte of this Byte array. Alias for each.
     *
     * @param self    a Byte array
     * @param closure a closure
     * @see #each(java.lang.Object, groovy.lang.Closure)
     * @since 1.5.5
     */
    public static void eachByte(Byte[] self, @ClosureParams(FirstParam.Component.class) Closure closure) {
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
    public static void eachByte(byte[] self, @ClosureParams(FirstParam.Component.class) Closure closure) {
        each(self, closure);
    }

    /**
     * Iterates over the elements of an aggregate of items and returns
     * the index of the first item that matches the condition specified in the closure.
     *
     * @param self      the iteration object over which to iterate
     * @param condition the matching condition
     * @return an integer that is the index of the first matched object or -1 if no match was found
     * @since 1.0
     */
    public static int findIndexOf(Object self, Closure condition) {
        return findIndexOf(self, 0, condition);
    }

    /**
     * Iterates over the elements of an aggregate of items, starting from a
     * specified startIndex, and returns the index of the first item that matches the
     * condition specified in the closure.
     *
     * @param self       the iteration object over which to iterate
     * @param startIndex start matching from this index
     * @param condition  the matching condition
     * @return an integer that is the index of the first matched object or -1 if no match was found
     * @since 1.5.0
     */
    public static int findIndexOf(Object self, int startIndex, Closure condition) {
        return findIndexOf(InvokerHelper.asIterator(self), condition);
    }

    /**
     * Iterates over the elements of an Iterator and returns the index of the first item that satisfies the
     * condition specified by the closure.
     *
     * @param self      an Iterator
     * @param condition the matching condition
     * @return an integer that is the index of the first matched object or -1 if no match was found
     * @since 2.5.0
     */
    public static <T> int findIndexOf(Iterator<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
        return findIndexOf(self, 0, condition);
    }

    /**
     * Iterates over the elements of an Iterator, starting from a
     * specified startIndex, and returns the index of the first item that satisfies the
     * condition specified by the closure.
     *
     * @param self       an Iterator
     * @param startIndex start matching from this index
     * @param condition  the matching condition
     * @return an integer that is the index of the first matched object or -1 if no match was found
     * @since 2.5.0
     */
    public static <T> int findIndexOf(Iterator<T> self, int startIndex, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
        int result = -1;
        int i = 0;
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(condition);
        while (self.hasNext()) {
            Object value = self.next();
            if (i++ < startIndex) {
                continue;
            }
            if (bcw.call(value)) {
                result = i - 1;
                break;
            }
        }
        return result;
    }

    /**
     * Iterates over the elements of an Iterable and returns the index of the first item that satisfies the
     * condition specified by the closure.
     *
     * @param self      an Iterable
     * @param condition the matching condition
     * @return an integer that is the index of the first matched object or -1 if no match was found
     * @since 2.5.0
     */
    public static <T> int findIndexOf(Iterable<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
        return findIndexOf(self, 0, condition);
    }

    /**
     * Iterates over the elements of an Iterable, starting from a
     * specified startIndex, and returns the index of the first item that satisfies the
     * condition specified by the closure.
     *
     * @param self       an Iterable
     * @param startIndex start matching from this index
     * @param condition  the matching condition
     * @return an integer that is the index of the first matched object or -1 if no match was found
     * @since 2.5.0
     */
    public static <T> int findIndexOf(Iterable<T> self, int startIndex, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
        return findIndexOf(self.iterator(), startIndex, condition);
    }

    /**
     * Iterates over the elements of an Array and returns the index of the first item that satisfies the
     * condition specified by the closure.
     *
     * @param self      an Array
     * @param condition the matching condition
     * @return an integer that is the index of the first matched object or -1 if no match was found
     * @since 2.5.0
     */
    public static <T> int findIndexOf(T[] self, @ClosureParams(FirstParam.Component.class) Closure condition) {
        return findIndexOf(self, 0, condition);
    }

    /**
     * Iterates over the elements of an Array, starting from a
     * specified startIndex, and returns the index of the first item that satisfies the
     * condition specified by the closure.
     *
     * @param self       an Array
     * @param startIndex start matching from this index
     * @param condition  the matching condition
     * @return an integer that is the index of the first matched object or -1 if no match was found
     * @since 2.5.0
     */
    public static <T> int findIndexOf(T[] self, int startIndex, @ClosureParams(FirstParam.Component.class) Closure condition) {
        return findIndexOf(new ArrayIterator<T>(self), startIndex, condition);
    }

    /**
     * Iterates over the elements of an aggregate of items and returns
     * the index of the last item that matches the condition specified in the closure.
     *
     * @param self      the iteration object over which to iterate
     * @param condition the matching condition
     * @return an integer that is the index of the last matched object or -1 if no match was found
     * @since 1.5.2
     */
    public static int findLastIndexOf(Object self, Closure condition) {
        return findLastIndexOf(self, 0, condition);
    }

    /**
     * Iterates over the elements of an aggregate of items, starting
     * from a specified startIndex, and returns the index of the last item that
     * matches the condition specified in the closure.
     *
     * @param self       the iteration object over which to iterate
     * @param startIndex start matching from this index
     * @param condition  the matching condition
     * @return an integer that is the index of the last matched object or -1 if no match was found
     * @since 1.5.2
     */
    public static int findLastIndexOf(Object self, int startIndex, Closure condition) {
        return findLastIndexOf(InvokerHelper.asIterator(self), startIndex, condition);
    }

    /**
     * Iterates over the elements of an Iterator and returns
     * the index of the last item that matches the condition specified in the closure.
     *
     * @param self      an Iterator
     * @param condition the matching condition
     * @return an integer that is the index of the last matched object or -1 if no match was found
     * @since 2.5.0
     */
    public static <T> int findLastIndexOf(Iterator<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
        return findLastIndexOf(self, 0, condition);
    }

    /**
     * Iterates over the elements of an Iterator, starting
     * from a specified startIndex, and returns the index of the last item that
     * matches the condition specified in the closure.
     *
     * @param self       an Iterator
     * @param startIndex start matching from this index
     * @param condition  the matching condition
     * @return an integer that is the index of the last matched object or -1 if no match was found
     * @since 2.5.0
     */
    public static <T> int findLastIndexOf(Iterator<T> self, int startIndex, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
        int result = -1;
        int i = 0;
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(condition);
        while (self.hasNext()) {
            Object value = self.next();
            if (i++ < startIndex) {
                continue;
            }
            if (bcw.call(value)) {
                result = i - 1;
            }
        }
        return result;
    }

    /**
     * Iterates over the elements of an Iterable and returns
     * the index of the last item that matches the condition specified in the closure.
     *
     * @param self      an Iterable
     * @param condition the matching condition
     * @return an integer that is the index of the last matched object or -1 if no match was found
     * @since 2.5.0
     */
    public static <T> int findLastIndexOf(Iterable<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
        return findLastIndexOf(self.iterator(), 0, condition);
    }

    /**
     * Iterates over the elements of an Iterable, starting
     * from a specified startIndex, and returns the index of the last item that
     * matches the condition specified in the closure.
     *
     * @param self       an Iterable
     * @param startIndex start matching from this index
     * @param condition  the matching condition
     * @return an integer that is the index of the last matched object or -1 if no match was found
     * @since 2.5.0
     */
    public static <T> int findLastIndexOf(Iterable<T> self, int startIndex, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
        return findLastIndexOf(self.iterator(), startIndex, condition);
    }

    /**
     * Iterates over the elements of an Array and returns
     * the index of the last item that matches the condition specified in the closure.
     *
     * @param self      an Array
     * @param condition the matching condition
     * @return an integer that is the index of the last matched object or -1 if no match was found
     * @since 2.5.0
     */
    public static <T> int findLastIndexOf(T[] self, @ClosureParams(FirstParam.Component.class) Closure condition) {
        return findLastIndexOf(new ArrayIterator<T>(self), 0, condition);
    }

    /**
     * Iterates over the elements of an Array, starting
     * from a specified startIndex, and returns the index of the last item that
     * matches the condition specified in the closure.
     *
     * @param self       an Array
     * @param startIndex start matching from this index
     * @param condition  the matching condition
     * @return an integer that is the index of the last matched object or -1 if no match was found
     * @since 2.5.0
     */
    public static <T> int findLastIndexOf(T[] self, int startIndex, @ClosureParams(FirstParam.Component.class) Closure condition) {
        // TODO could be made more efficient by using a reverse index
        return findLastIndexOf(new ArrayIterator<T>(self), startIndex, condition);
    }

    /**
     * Iterates over the elements of an aggregate of items and returns
     * the index values of the items that match the condition specified in the closure.
     *
     * @param self      the iteration object over which to iterate
     * @param condition the matching condition
     * @return a list of numbers corresponding to the index values of all matched objects
     * @since 1.5.2
     */
    public static List<Number> findIndexValues(Object self, Closure condition) {
        return findIndexValues(self, 0, condition);
    }

    /**
     * Iterates over the elements of an aggregate of items, starting from
     * a specified startIndex, and returns the index values of the items that match
     * the condition specified in the closure.
     *
     * @param self       the iteration object over which to iterate
     * @param startIndex start matching from this index
     * @param condition  the matching condition
     * @return a list of numbers corresponding to the index values of all matched objects
     * @since 1.5.2
     */
    public static List<Number> findIndexValues(Object self, Number startIndex, Closure condition) {
        return findIndexValues(InvokerHelper.asIterator(self), startIndex, condition);
    }

    /**
     * Iterates over the elements of an Iterator and returns
     * the index values of the items that match the condition specified in the closure.
     *
     * @param self      an Iterator
     * @param condition the matching condition
     * @return a list of numbers corresponding to the index values of all matched objects
     * @since 2.5.0
     */
    public static <T> List<Number> findIndexValues(Iterator<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
        return findIndexValues(self, 0, condition);
    }

    /**
     * Iterates over the elements of an Iterator, starting from
     * a specified startIndex, and returns the index values of the items that match
     * the condition specified in the closure.
     *
     * @param self       an Iterator
     * @param startIndex start matching from this index
     * @param condition  the matching condition
     * @return a list of numbers corresponding to the index values of all matched objects
     * @since 2.5.0
     */
    public static <T> List<Number> findIndexValues(Iterator<T> self, Number startIndex, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
        List<Number> result = new ArrayList<Number>();
        long count = 0;
        long startCount = startIndex.longValue();
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(condition);
        while (self.hasNext()) {
            Object value = self.next();
            if (count++ < startCount) {
                continue;
            }
            if (bcw.call(value)) {
                result.add(count - 1);
            }
        }
        return result;
    }

    /**
     * Iterates over the elements of an Iterable and returns
     * the index values of the items that match the condition specified in the closure.
     *
     * @param self      an Iterable
     * @param condition the matching condition
     * @return a list of numbers corresponding to the index values of all matched objects
     * @since 2.5.0
     */
    public static <T> List<Number> findIndexValues(Iterable<T> self, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
        return findIndexValues(self, 0, condition);
    }

    /**
     * Iterates over the elements of an Iterable, starting from
     * a specified startIndex, and returns the index values of the items that match
     * the condition specified in the closure.
     *
     * @param self       an Iterable
     * @param startIndex start matching from this index
     * @param condition  the matching condition
     * @return a list of numbers corresponding to the index values of all matched objects
     * @since 2.5.0
     */
    public static <T> List<Number> findIndexValues(Iterable<T> self, Number startIndex, @ClosureParams(FirstParam.FirstGenericType.class) Closure condition) {
        return findIndexValues(self.iterator(), startIndex, condition);
    }

    /**
     * Iterates over the elements of an Array and returns
     * the index values of the items that match the condition specified in the closure.
     *
     * @param self      an Array
     * @param condition the matching condition
     * @return a list of numbers corresponding to the index values of all matched objects
     * @since 2.5.0
     */
    public static <T> List<Number> findIndexValues(T[] self, @ClosureParams(FirstParam.Component.class) Closure condition) {
        return findIndexValues(self, 0, condition);
    }

    /**
     * Iterates over the elements of an Array, starting from
     * a specified startIndex, and returns the index values of the items that match
     * the condition specified in the closure.
     *
     * @param self       an Array
     * @param startIndex start matching from this index
     * @param condition  the matching condition
     * @return a list of numbers corresponding to the index values of all matched objects
     * @since 2.5.0
     */
    public static <T> List<Number> findIndexValues(T[] self, Number startIndex, @ClosureParams(FirstParam.Component.class) Closure condition) {
        return findIndexValues(new ArrayIterator<T>(self), startIndex, condition);
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
     * Set the metaclass for an object.
     * @param self the object whose metaclass we want to set
     * @param metaClass the new metaclass value
     * @since 1.6.0
     */
    public static void setMetaClass(Object self, MetaClass metaClass) {
        if (metaClass instanceof HandleMetaClass)
            metaClass = ((HandleMetaClass)metaClass).getAdaptee();

        if (self instanceof Class) {
            GroovySystem.getMetaClassRegistry().setMetaClass((Class) self, metaClass);
        } else {
            ((MetaClassRegistryImpl)GroovySystem.getMetaClassRegistry()).setMetaClass(self, metaClass);
        }
    }

    /**
     * Set the metaclass for a GroovyObject.
     * @param self the object whose metaclass we want to set
     * @param metaClass the new metaclass value
     * @since 2.0.0
     */
    public static void setMetaClass(GroovyObject self, MetaClass metaClass) {
        // this method was introduced as to prevent from a stack overflow, described in GROOVY-5285
        if (metaClass instanceof HandleMetaClass)
            metaClass = ((HandleMetaClass)metaClass).getAdaptee();

        self.setMetaClass(metaClass);
        disablePrimitiveOptimization(self);
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
    public static MetaClass metaClass(Class self, @ClosureParams(value=SimpleType.class, options="java.lang.Object")
            @DelegatesTo(type="groovy.lang.ExpandoMetaClass.DefiningClosure", strategy=Closure.DELEGATE_ONLY) Closure closure) {
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
    public static MetaClass metaClass(Object self, @ClosureParams(value=SimpleType.class, options="java.lang.Object")
            @DelegatesTo(type="groovy.lang.ExpandoMetaClass.DefiningClosure", strategy=Closure.DELEGATE_ONLY) Closure closure) {
        MetaClass emc = hasPerInstanceMetaClass(self);
        if (emc == null) {
            final ExpandoMetaClass metaClass = new ExpandoMetaClass(self.getClass(), false, true);
            metaClass.initialize();
            metaClass.define(closure);
            if (self instanceof GroovyObject) {
                setMetaClass((GroovyObject)self, metaClass);
            } else {
                setMetaClass(self, metaClass);
            }
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
            public boolean hasNext() {
                return enumeration.hasMoreElements();
            }

            public T next() {
                return enumeration.nextElement();
            }

            public void remove() {
                throw new UnsupportedOperationException("Cannot remove() from an Enumeration");
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
     * Returns a <code>BufferedIterator</code> that allows examining the next element without
     * consuming it.
     * <pre class="groovyTestCase">
     * assert [1, 2, 3, 4].iterator().buffered().with { [head(), toList()] } == [1, [1, 2, 3, 4]]
     * </pre>
     *
     * @param self an iterator object
     * @return a BufferedIterator wrapping self
     * @since 2.5.0
     */
    public static <T> BufferedIterator<T> buffered(Iterator<T> self) {
        if (self instanceof BufferedIterator) {
            return (BufferedIterator<T>) self;
        } else {
            return new IteratorBufferedIterator<T>(self);
        }
    }

    /**
     * Returns a <code>BufferedIterator</code> that allows examining the next element without
     * consuming it.
     * <pre class="groovyTestCase">
     * assert new LinkedHashSet([1,2,3,4]).bufferedIterator().with { [head(), toList()] } == [1, [1,2,3,4]]
     * </pre>
     *
     * @param self an iterable object
     * @return a BufferedIterator for traversing self
     * @since 2.5.0
     */
    public static <T> BufferedIterator<T> bufferedIterator(Iterable<T> self) {
        return new IteratorBufferedIterator<T>(self.iterator());
    }

    /**
     * Returns a <code>BufferedIterator</code> that allows examining the next element without
     * consuming it.
     * <pre class="groovyTestCase">
     * assert [1, 2, 3, 4].bufferedIterator().with { [head(), toList()] } == [1, [1, 2, 3, 4]]
     * </pre>
     *
     * @param self a list
     * @return a BufferedIterator for traversing self
     * @since 2.5.0
     */
    public static <T> BufferedIterator<T> bufferedIterator(List<T> self) {
        return new ListBufferedIterator<T>(self);
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

    /**
     * Dynamically wraps an instance into something which implements the
     * supplied trait classes. It is guaranteed that the returned object
     * will implement the trait interfaces, but the original type of the
     * object is lost (replaced with a proxy).
     * @param self object to be wrapped
     * @param traits a list of trait classes
     * @return a proxy implementing the trait interfaces
     */
    public static Object withTraits(Object self, Class<?>... traits) {
        List<Class> interfaces = new ArrayList<Class>();
        Collections.addAll(interfaces, traits);
        return ProxyGenerator.INSTANCE.instantiateDelegate(interfaces, self);
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert [1, 3, 2, 4] == [1, 2, 3, 4].swap(1, 2)
     * </pre>
     *
     * @param self a List
     * @param i a position
     * @param j a position
     * @return self
     * @see Collections#swap(List, int, int)
     * @since 2.4.0
     */
    public static <T> List<T> swap(List<T> self, int i, int j) {
        Collections.swap(self, i, j);
        return self;
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert (["a", "c", "b", "d"] as String[]) == (["a", "b", "c", "d"] as String[]).swap(1, 2)
     * </pre>
     *
     * @param self an array
     * @param i a position
     * @param j a position
     * @return self
     * @since 2.4.0
     */
    public static <T> T[] swap(T[] self, int i, int j) {
        T tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert ([false, true, false, true] as boolean[]) == ([false, false, true, true] as boolean[]).swap(1, 2)
     * </pre>
     *
     * @param self a boolean array
     * @param i a position
     * @param j a position
     * @return self
     * @since 2.4.0
     */
    public static boolean[] swap(boolean[] self, int i, int j) {
        boolean tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert ([1, 3, 2, 4] as byte[]) == ([1, 2, 3, 4] as byte[]).swap(1, 2)
     * </pre>
     *
     * @param self a boolean array
     * @param i a position
     * @param j a position
     * @return self
     * @since 2.4.0
     */
    public static byte[] swap(byte[] self, int i, int j) {
        byte tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert ([1, 3, 2, 4] as char[]) == ([1, 2, 3, 4] as char[]).swap(1, 2)
     * </pre>
     *
     * @param self a boolean array
     * @param i a position
     * @param j a position
     * @return self
     * @since 2.4.0
     */
    public static char[] swap(char[] self, int i, int j) {
        char tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert ([1, 3, 2, 4] as double[]) == ([1, 2, 3, 4] as double[]).swap(1, 2)
     * </pre>
     *
     * @param self a boolean array
     * @param i a position
     * @param j a position
     * @return self
     * @since 2.4.0
     */
    public static double[] swap(double[] self, int i, int j) {
        double tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert ([1, 3, 2, 4] as float[]) == ([1, 2, 3, 4] as float[]).swap(1, 2)
     * </pre>
     *
     * @param self a boolean array
     * @param i a position
     * @param j a position
     * @return self
     * @since 2.4.0
     */
    public static float[] swap(float[] self, int i, int j) {
        float tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert ([1, 3, 2, 4] as int[]) == ([1, 2, 3, 4] as int[]).swap(1, 2)
     * </pre>
     *
     * @param self a boolean array
     * @param i a position
     * @param j a position
     * @return self
     * @since 2.4.0
     */
    public static int[] swap(int[] self, int i, int j) {
        int tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert ([1, 3, 2, 4] as long[]) == ([1, 2, 3, 4] as long[]).swap(1, 2)
     * </pre>
     *
     * @param self a boolean array
     * @param i a position
     * @param j a position
     * @return self
     * @since 2.4.0
     */
    public static long[] swap(long[] self, int i, int j) {
        long tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    /**
     * Swaps two elements at the specified positions.
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * assert ([1, 3, 2, 4] as short[]) == ([1, 2, 3, 4] as short[]).swap(1, 2)
     * </pre>
     *
     * @param self a boolean array
     * @param i a position
     * @param j a position
     * @return self
     * @since 2.4.0
     */
    public static short[] swap(short[] self, int i, int j) {
        short tmp = self[i];
        self[i] = self[j];
        self[j] = tmp;
        return self;
    }

    /**
     * Modifies this list by removing the element at the specified position
     * in this list. Returns the removed element. Essentially an alias for
     * {@link List#remove(int)} but with no ambiguity for List&lt;Integer&gt;.
     * <p/>
     * Example:
     * <pre class="groovyTestCase">
     * def list = [1, 2, 3]
     * list.removeAt(1)
     * assert [1, 3] == list
     * </pre>
     *
     * @param self a List
     * @param index the index of the element to be removed
     * @return the element previously at the specified position
     * @since 2.4.0
     */
    public static <E> E removeAt(List<E> self, int index) {
        return self.remove(index);
    }

    /**
     * Modifies this collection by removing a single instance of the specified
     * element from this collection, if it is present. Essentially an alias for
     * {@link Collection#remove(Object)} but with no ambiguity for Collection&lt;Integer&gt;.
     * <p/>
     * Example:
     * <pre class="groovyTestCase">
     * def list = [1, 2, 3, 2]
     * list.removeElement(2)
     * assert [1, 3, 2] == list
     * </pre>
     *
     * @param self a Collection
     * @param o element to be removed from this collection, if present
     * @return true if an element was removed as a result of this call
     * @since 2.4.0
     */
    public static <E> boolean removeElement(Collection<E> self, Object o) {
        return self.remove(o);
    }

    /**
     * Get runtime groovydoc
     * @param holder the groovydoc hold
     * @return runtime groovydoc
     * @since 2.6.0
     */
    public static groovy.lang.groovydoc.Groovydoc getGroovydoc(AnnotatedElement holder) {
        Groovydoc groovydocAnnotation = holder.getAnnotation(Groovydoc.class);

        return null == groovydocAnnotation
                    ? EMPTY_GROOVYDOC
                    : new groovy.lang.groovydoc.Groovydoc(groovydocAnnotation.value(), holder);
    }

    @Deprecated
    public static <T> T asType(CharSequence self, Class<T> c) {
        return StringGroovyMethods.asType(self, c);
    }

    /**
     * Get the detail information of {@link Throwable} instance's stack trace
     *
     * @param self a Throwable instance
     * @return the detail information of stack trace
     * @since 2.5.3
     */
    public static String asString(Throwable self) {
        StringBuilderWriter sw = new StringBuilderWriter();

        try (PrintWriter pw = new PrintWriter(sw)) {
            self.printStackTrace(pw);
        }

        return sw.toString();
    }
}
