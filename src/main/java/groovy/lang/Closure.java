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
package groovy.lang;

import org.apache.groovy.internal.util.UncheckedThrow;
import org.apache.groovy.io.StringBuilderWriter;
import org.apache.groovy.util.Maps;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.reflection.stdclasses.CachedClosureClass;
import org.codehaus.groovy.runtime.ComposedClosure;
import org.codehaus.groovy.runtime.CurriedClosure;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.BooleanClosureWrapper;
import org.codehaus.groovy.runtime.memoize.ConcurrentCommonCache;
import org.codehaus.groovy.runtime.memoize.ConcurrentSoftCache;
import org.codehaus.groovy.runtime.memoize.LRUCache;
import org.codehaus.groovy.runtime.memoize.Memoize;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serial;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents any closure object in Groovy.
 * <p>
 * Groovy allows instances of Closures to be called in a
 * short form. For example:
 * <pre class="language-groovy groovyTestCase">
 * def a = 1
 * def c = { a }
 * assert c() == 1
 * </pre>
 * To be able to use a Closure in this way with your own
 * subclass, you need to provide a doCall method with any
 * signature you want to. This ensures that
 * {@link #getMaximumNumberOfParameters()} and
 * {@link #getParameterTypes()} will work too without any
 * additional code. If no doCall method is provided a
 * closure must be used in its long form like
 * <pre class="language-groovy groovyTestCase">
 * def a = 1
 * def c = {a}
 * assert c.call() == 1
 * </pre>
 */
public abstract class Closure<V> extends GroovyObjectSupport implements Cloneable, Runnable, GroovyCallable<V>, Serializable {

    /**
     * With this resolveStrategy set the closure will attempt to resolve property references and methods to the
     * owner first, then the delegate (<b>this is the default strategy</b>).
     *
     * For example the following code:
     * <pre class="language-groovy groovyTestCase">
     * class Test {
     *     def x = 30
     *     def y = 40
     *
     *     def run() {
     *         def data = [ x: 10, y: 20 ]
     *         def cl = { y = x + y }
     *         cl.delegate = data
     *         cl()
     *         assert x == 30
     *         assert y == 70
     *         assert data == [x:10, y:20]
     *     }
     * }
     *
     * new Test().run()
     * </pre>
     * Will succeed, because the x and y fields declared in the Test class shadow the variables in the delegate.<p>
     * <i>Note that local variables are always looked up first, independently of the resolution strategy.</i>
     */
    public static final int OWNER_FIRST = 0;

    /**
     * With this resolveStrategy set the closure will attempt to resolve property references and methods to the
     * delegate first then the owner.
     *
     * For example the following code:
     * <pre class="language-groovy groovyTestCase">
     * class Test {
     *     def x = 30
     *     def y = 40
     *
     *     def run() {
     *         def data = [ x: 10, y: 20 ]
     *         def cl = { y = x + y }
     *         cl.delegate = data
     *         cl.resolveStrategy = Closure.DELEGATE_FIRST
     *         cl()
     *         assert x == 30
     *         assert y == 40
     *         assert data == [x:10, y:30]
     *     }
     * }
     *
     * new Test().run()
     * </pre>
     * This will succeed, because the x and y variables declared in the delegate shadow the fields in the owner class.<p>
     * <i>Note that local variables are always looked up first, independently of the resolution strategy.</i>
     */
    public static final int DELEGATE_FIRST = 1;

    /**
     * With this resolveStrategy set the closure will resolve property references and methods to the owner only
     * and not call the delegate at all. For example the following code :
     *
     * <pre class="language-groovy groovyTestCase">
     * class Test {
     *     def x = 30
     *     def y = 40
     *
     *     def run() {
     *         def data = [ x: 10, y: 20, z: 30 ]
     *         def cl = { y = x + y + z }
     *         cl.delegate = data
     *         cl.resolveStrategy = Closure.OWNER_ONLY
     *         cl()
     *         println x
     *         println y
     *         println data
     *     }
     * }
     *
     * groovy.test.GroovyAssert.shouldFail(MissingPropertyException) {
     *     new Test().run()
     * }
     * </pre>
     *
     * will throw "No such property: z" error because even if the z variable is declared in the delegate, no
     * lookup is made.<p>
     * <i>Note that local variables are always looked up first, independently of the resolution strategy.</i>
     */
    public static final int OWNER_ONLY = 2;

    /**
     * With this resolveStrategy set the closure will resolve property references and methods to the delegate
     * only and entirely bypass the owner. For example the following code :
     *
     * <pre class="language-groovy groovyTestCase">
     * class Test {
     *     def x = 30
     *     def y = 40
     *     def z = 50
     *
     *     def run() {
     *         def data = [ x: 10, y: 20 ]
     *         def cl = { y = x + y + z }
     *         cl.delegate = data
     *         cl.resolveStrategy = Closure.DELEGATE_ONLY
     *         cl()
     *         println x
     *         println y
     *         println data
     *     }
     * }
     *
     * groovy.test.GroovyAssert.shouldFail {
     *     new Test().run()
     * }
     * </pre>
     *
     * will throw an error because even if the owner declares a "z" field, the resolution strategy will bypass
     * lookup in the owner.<p>
     * <i>Note that local variables are always looked up first, independently of the resolution strategy.</i>
     */
    public static final int DELEGATE_ONLY = 3;

    /**
     * With this resolveStrategy set the closure will resolve property references to itself and go
     * through the usual MetaClass look-up process. This means that properties and methods are neither resolved
     * from the owner nor the delegate, but only on the closure object itself. This allows the developer to
     * override getProperty using ExpandoMetaClass of the closure itself.<p>
     * <i>Note that local variables are always looked up first, independently of the resolution strategy.</i>
     */
    public static final int TO_SELF = 4;

    /**
     * Directive constants indicating that processing should stop or skip the current item.
     */
    public static final int DONE = 1, SKIP = 2;
    private static final Object[] EMPTY_OBJECT_ARRAY = {};
    /**
     * Identity closure returning its single argument.
     */
    public static final Closure IDENTITY = new Closure<Object>(null) {
        @Serial private static final long serialVersionUID = 730973623329943963L;

        /**
         * Returns the supplied argument unchanged.
         *
         * @param args the argument to return
         * @return the supplied argument
         */
        public Object doCall(Object args) {
            return args;
        }
    };

    private static final Map<String, Function<Closure, Object>> PROPERTY_GETTERS = Maps.of(
            "owner", Closure::getOwner,
            "delegate", Closure::getDelegate,
            "thisObject", Closure::getThisObject,
            "class", Closure::getClass,
            "metaClass", Closure::getMetaClass,
            "directive", Closure::getDirective,
            "parameterTypes", Closure::getParameterTypes,
            "resolveStrategy", Closure::getResolveStrategy,
            "maximumNumberOfParameters", Closure::getMaximumNumberOfParameters
    );
    private static final Map<String, BiConsumer<Closure, Object>> PROPERTY_SETTERS = Maps.of(
            "delegate", Closure::setDelegate,
            "metaClass", (closure, value) -> closure.setMetaClass((MetaClass) value),
            "directive", (closure, value) -> closure.setDirective((Integer) value),
            "resolveStrategy", (closure, value) -> closure.setResolveStrategy((Integer) value)
    );

    /**
     * Lazily cached call overrides for closure subclasses.
     */
    private static final ClassValue<CallOverride> CALL_OVERRIDES = new ClassValue<CallOverride>() {
        /**
         * Resolves cached call overrides for the supplied closure type.
         *
         * @param type the closure type
         * @return the resolved call override metadata
         */
        @Override
        protected CallOverride computeValue(Class<?> type) {
            return CallOverride.lookup(type);
        }
    };
    private static final ThreadLocal<Boolean> IN_CALL_FALLBACK = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private Object delegate;
    private Object owner;
    private Object thisObject;
    private int resolveStrategy = OWNER_FIRST;
    private int directive;
    /**
     * Parameter types for the longest {@code doCall} method.
     */
    protected Class<?>[] parameterTypes;
    /**
     * Maximum number of parameters accepted by this closure.
     */
    protected int maximumNumberOfParameters;
    @Serial private static final long serialVersionUID = 4368710879820278874L;
    private BooleanClosureWrapper bcw;

    /**
     * Creates a closure with the supplied owner and lexical {@code this} object.
     *
     * @param owner the closure owner
     * @param thisObject the lexical {@code this} object
     */
    public Closure(Object owner, Object thisObject) {
        this.owner = owner;
        this.delegate = owner;
        this.thisObject = thisObject;

        final CachedClosureClass cachedClass = (CachedClosureClass) ReflectionCache.getCachedClass(getClass());
        parameterTypes = cachedClass.getParameterTypes();
        maximumNumberOfParameters = cachedClass.getMaximumNumberOfParameters();
    }

    /**
     * Constructor used when the "this" object for the Closure is null.
     * This is rarely the case in normal Groovy usage.
     *
     * @param owner the Closure owner
     */
    public Closure(Object owner) {
        this(owner, null);
    }

    /**
     * Sets the strategy which the closure uses to resolve property references and methods.
     * The default is Closure.OWNER_FIRST
     *
     * @param resolveStrategy The resolve strategy to set
     *
     * @see groovy.lang.Closure#DELEGATE_FIRST
     * @see groovy.lang.Closure#DELEGATE_ONLY
     * @see groovy.lang.Closure#OWNER_FIRST
     * @see groovy.lang.Closure#OWNER_ONLY
     * @see groovy.lang.Closure#TO_SELF
     */
    public void setResolveStrategy(int resolveStrategy) {
        this.resolveStrategy = resolveStrategy;
    }

    /**
     * Gets the strategy which the closure uses to resolve methods and properties
     *
     * @return The resolve strategy
     *
     * @see groovy.lang.Closure#DELEGATE_FIRST
     * @see groovy.lang.Closure#DELEGATE_ONLY
     * @see groovy.lang.Closure#OWNER_FIRST
     * @see groovy.lang.Closure#OWNER_ONLY
     * @see groovy.lang.Closure#TO_SELF
     */
    public int getResolveStrategy() {
        return resolveStrategy;
    }

    /**
     * Returns the lexical {@code this} object for this closure.
     *
     * @return the lexical {@code this} object
     */
    public Object getThisObject() {
        return thisObject;
    }

    private transient Class<?> thisType;
    private Class<?> getThisType() {
        Class<?> thisType = this.thisType;
        if (thisType == null) {
            thisType = getClass();
            while (GeneratedClosure.class.isAssignableFrom(thisType)) {
                thisType = thisType.getEnclosingClass();
            }
            this.thisType = thisType;
        }
        return thisType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(final String property) {
        Function<Closure, Object> getter = PROPERTY_GETTERS.get(property);
        if (getter != null) {
            return getter.apply(this);
        }

        return resolveGetProperty(property);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(final String property, final Object newValue) {
        BiConsumer<Closure, Object> setter = PROPERTY_SETTERS.get(property);
        if (setter != null) {
            setter.accept(this, newValue);
            return;
        }

        resolveSetProperty(property, newValue);
    }

    private Object resolveGetProperty(final String property) {
        switch (resolveStrategy) {
            case DELEGATE_FIRST:
                final Object delegate = getDelegate();
                final Object owner = getOwner();
                if (delegate == null) return getProperty(owner, property);
                return getPropertyTryThese(delegate, owner, property);
            case DELEGATE_ONLY:
                return getProperty(getDelegate(), property);
            case OWNER_ONLY:
                return getProperty(getOwner(), property);
            case TO_SELF:
                return super.getProperty(property);
            default:
                return getPropertyTryThese(getOwner(), getDelegate(), property);
        }
    }

    private void resolveSetProperty(final String property, final Object newValue) {
        switch (resolveStrategy) {
            case DELEGATE_FIRST:
                final Object delegate = getDelegate();
                final Object owner = getOwner();
                if (delegate == null) setProperty(owner, property, newValue);
                else setPropertyTryThese(delegate, owner, property, newValue);
                break;
            case DELEGATE_ONLY:
                setProperty(getDelegate(), property, newValue);
                break;
            case OWNER_ONLY:
                setProperty(getOwner(), property, newValue);
                break;
            case TO_SELF:
                super.setProperty(property, newValue);
                break;
            default:
                setPropertyTryThese(getOwner(), getDelegate(), property, newValue);
        }
    }


    private Object getProperty(final Object receiver, final String property) {
        try {
            return InvokerHelper.getProperty(receiver, property);
        } catch (GroovyRuntimeException e1) {
            if (null != receiver && this != receiver && this instanceof GeneratedClosure) { // GROOVY-11128
                final Class<?> thisType = getThisType();
                if (thisType != receiver.getClass() && thisType.isInstance(receiver)) {
                    try {
                        return ((GroovyObject) receiver).getMetaClass().getProperty(thisType, receiver, property, false, true);
                    } catch (GroovyRuntimeException e2) {
                        e1.addSuppressed(e2);
                    }
                }
            }
            throw e1;
        }
    }

    private Object getPropertyTryThese(final Object o1, final Object o2, final String property) {
        try {
            return getProperty(o1, property);
        } catch (MissingPropertyException | MissingFieldException e1) {
            if (o2 != null && o1 != this && o1 != o2) {
                try {
                    return getProperty(o2, property);
                } catch (GroovyRuntimeException e2) {
                    e1.addSuppressed(e2);
                }
            }
            throw e1;
        }
    }

    private void setProperty(final Object receiver, final String property, final Object newValue) {
        try {
            InvokerHelper.setProperty(receiver, property, newValue);
        } catch (GroovyRuntimeException e1) {
            if (null != receiver && this != receiver && this instanceof GeneratedClosure) { // GROOVY-11128
                final Class<?> thisType = getThisType();
                if (thisType != receiver.getClass() && thisType.isInstance(receiver)) {
                    try {
                        ((GroovyObject) receiver).getMetaClass().setProperty(thisType, receiver, property, newValue, false, true);
                        return;
                    } catch (GroovyRuntimeException e2) {
                        e1.addSuppressed(e2);
                    }
                }
            }
            throw e1;
        }
    }

    private void setPropertyTryThese(final Object o1, final Object o2, final String property, final Object newValue) {
        try {
            setProperty(o1, property, newValue);
        } catch (GroovyRuntimeException e1) {
            if (o1 != null && o1 != this && o1 != o2) {
                try {
                    setProperty(o2, property, newValue);
                    return;
                } catch (GroovyRuntimeException e2) {
                    e1.addSuppressed(e2);
                }
            }
            throw e1;
        }
    }

    /**
     * Evaluates this closure for Groovy switch/case matching.
     *
     * @param switchValue the switch value to test
     * @return {@code true} if the case matches
     */
    public boolean isCase(final Object switchValue) {
        if (bcw == null) {
            bcw = new BooleanClosureWrapper(this);
        }
        return bcw.call(switchValue);
    }

    /**
     * Invokes the closure with no arguments, returning any value if applicable.
     *
     * @return The value if applicable or null if there is no return statement in the closure.
     */
    @Override
    public V call() {
        return call(EMPTY_OBJECT_ARRAY);
    }

    /**
     * Invokes the closure with given argument(s), returning any value if applicable.
     *
     * @param arguments could be a single value or a List of values
     * @return The value if applicable or null if there is no return statement in the closure.
     */
    public V call(final Object arguments) {
        return call(new Object[]{arguments});
    }

    /**
     * Invokes the closure with given argument(s), returning any value if applicable.
     *
     * @return The value if applicable or null if there is no return statement in the closure.
     */
    @SuppressWarnings("unchecked")
    public V call(final Object... arguments) {
        Class<?> myClass = getClass();
        if (myClass != Closure.class && arguments != null && mopUnperturbed()) {
            CallOverride override = CALL_OVERRIDES.get(myClass);
            // call selects a doCall: the cache is keyed on the subclass's doCall declarations,
            // arity-indexed (GROOVY-12165) so the shapes the GDK drives hard (Map#each,
            // eachWithIndex, inject) dispatch directly. An argument that is already an instance
            // of each declared parameter type dispatches directly; anything that needs Groovy
            // coercion (GString -> String, number conversions, null) falls through to the
            // metaclass below, which coerces exactly as before (GROOVY-12164). A subclass with
            // no doCall keeps the GROOVY-11911 call()/call(Object) compatibility carve-out,
            // whose targets alone need the re-entry latch (a custom call override may delegate
            // back to call(...), which must not re-dispatch to the same target).
            Method target = null;
            int arity = arguments.length;
            if (arity < CallOverride.ARITY_LIMIT) {
                Method m = override.byArity[arity];
                if (m != null && CallOverride.guardsPass(override.guards[arity], arguments)) {
                    target = m;
                }
            }
            if (target != null) {
                if (!override.callForm[arity]) {
                    // a doCall body: re-entry is ordinary recursion, no latch required
                    try {
                        return (V) target.invoke(this, arguments);
                    } catch (InvocationTargetException ite) {
                        UncheckedThrow.rethrow(ite.getCause());
                        return null; // unreachable statement
                    } catch (IllegalAccessException iae) {
                        throw new GroovyRuntimeException(iae);
                    }
                } else if (!IN_CALL_FALLBACK.get()) {
                    IN_CALL_FALLBACK.set(Boolean.TRUE);
                    try {
                        return (V) target.invoke(this, arguments);
                    } catch (InvocationTargetException ite) {
                        UncheckedThrow.rethrow(ite.getCause());
                        return null; // unreachable statement
                    } catch (IllegalAccessException iae) {
                        throw new GroovyRuntimeException(iae);
                    } finally {
                        IN_CALL_FALLBACK.set(Boolean.FALSE);
                    }
                }
            }
        }
        try {
            return (V) getMetaClass().invokeMethod(this, "doCall", arguments);
        } catch (InvokerInvocationException e) {
            UncheckedThrow.rethrow(e.getCause());
            return null; // unreachable statement
        } catch (Exception e) {
            return (V) throwRuntimeException(e);
        }
    }

    /**
     * Whether something MOP-relevant is in play for this instance: a non-stock metaclass
     * (replaced, wrapped or Expando — it must see {@code invokeMethod}). Checked once at
     * construction and latched by {@link #setMetaClass}, which is the only way the instance's
     * metaclass field can change — so reading one boolean here is equivalent to re-checking
     * the metaclass class on every call, at a fraction of the cost on the hot dispatch lanes.
     */
    private transient boolean mopPerturbed = nonStockMetaClass();

    private boolean nonStockMetaClass() {
        MetaClass mc = super.getMetaClass();
        Class<?> mcClass = (mc == null) ? null : mc.getClass();
        return mcClass != org.codehaus.groovy.runtime.metaclass.ClosureMetaClass.class && mcClass != MetaClassImpl.class
                && mcClass != org.codehaus.groovy.runtime.metaclass.PackedClosureMetaClass.class;
    }

    @Override
    public void setMetaClass(final MetaClass metaClass) {
        super.setMetaClass(metaClass);
        // conservative: any explicitly assigned metaclass (even a stock one) routes via the MOP
        mopPerturbed = true;
    }

    /**
     * Whether nothing MOP-relevant is in play for this instance, so a cached direct
     * dispatch path is semantically invisible: the metaclass is a stock one and no
     * category is active on the current thread — the same conditions the classic
     * call-site caches check (see {@code AbstractCallSite}). One boolean load and one
     * quick-exit counter read, cheap enough for per-element dispatch lanes.
     */
    protected final boolean mopUnperturbed() {
        return !mopPerturbed && !org.codehaus.groovy.runtime.GroovyCategorySupport.hasCategoryInCurrentThread();
    }

    /**
     * Throws the supplied throwable as a runtime exception, wrapping checked throwables.
     *
     * @param throwable the throwable to rethrow
     * @return never returns normally
     */
    protected static Object throwRuntimeException(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        } else {
            throw new GroovyRuntimeException(throwable.getMessage(), throwable);
        }
    }

    /**
     * @return the owner Object to which method calls will go which is
     *         typically the outer class when the closure is constructed
     */
    public Object getOwner() {
        return this.owner;
    }

    /**
     * @return the delegate Object to which method calls will go which is
     *         typically the outer class when the closure is constructed
     */
    public Object getDelegate() {
        return this.delegate;
    }

    /**
     * Allows the delegate to be changed such as when performing markup building
     *
     * @param delegate the new delegate
     */
    public void setDelegate(Object delegate) {
        this.delegate = delegate;
    }

    /**
     * @return the parameter types of the longest doCall method
     * of this closure
     */
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * @return the maximum number of parameters a doCall method
     * of this closure can take
     */
    public int getMaximumNumberOfParameters() {
        return maximumNumberOfParameters;
    }

    /**
     * @return a version of this closure which implements Writable.  Note that
     * the returned Writable also overrides {@link #toString()} in order
     * to allow rendering the result directly to a String.
     */
    public Closure asWritable() {
        return new WritableClosure();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        call();
    }

    /**
     * Support for Closure currying.
     * <p>
     * Typical usage:
     * <pre class="language-groovy groovyTestCase">
     * def multiply = { a, b {@code ->} a * b }
     * def doubler = multiply.curry(2)
     * assert doubler(4) == 8
     * </pre>
     * Note: special treatment is given to Closure vararg-style capability.
     * If you curry a vararg parameter, you don't consume the entire vararg array
     * but instead the first parameter of the vararg array as the following example shows:
     * <pre class="language-groovy groovyTestCase">
     * def a = { one, two, Object[] others {@code ->} one + two + others.sum() }
     * assert a.parameterTypes.name == ['java.lang.Object', 'java.lang.Object', '[Ljava.lang.Object;']
     * assert a(1,2,3,4) == 10
     * def b = a.curry(1)
     * assert b.parameterTypes.name == ['java.lang.Object', '[Ljava.lang.Object;']
     * assert b(2,3,4) == 10
     * def c = b.curry(2)
     * assert c.parameterTypes.name == ['[Ljava.lang.Object;']
     * assert c(3,4) == 10
     * def d = c.curry(3)
     * assert d.parameterTypes.name == ['[Ljava.lang.Object;']
     * assert d(4) == 10
     * def e = d.curry(4)
     * assert e.parameterTypes.name == ['[Ljava.lang.Object;']
     * assert e() == 10
     * assert e(5) == 15
     * </pre>
     *
     *
     * @param arguments the arguments to bind
     * @return the new closure with its arguments bound
     */
    public Closure<V> curry(final Object... arguments) {
        return new CurriedClosure<V>(this, arguments);
    }

    /**
     * Support for Closure currying.
     *
     * @param argument the argument to bind
     * @return the new closure with the argument bound
     * @see #curry(Object...)
     */
    public Closure<V> curry(final Object argument) {
        return curry(new Object[]{argument});
    }

    /**
     * Support for Closure "right" currying.
     * Parameters are supplied on the right rather than left as per the normal curry() method.
     * Typical usage:
     * <pre class="language-groovy groovyTestCase">
     * def divide = { a, b {@code ->} a / b }
     * def halver = divide.rcurry(2)
     * assert halver(8) == 4
     * </pre>
     *
     * The position of the curried parameters will be calculated lazily, for example,
     * if two overloaded doCall methods are available, the supplied arguments plus the
     * curried arguments will be concatenated and the result used for method selection.
     *
     * @param arguments the arguments to bind
     * @return the new closure with its arguments bound
     * @see #curry(Object...)
     */
    public Closure<V> rcurry(final Object... arguments) {
        return new CurriedClosure<V>(-arguments.length, this, arguments);
    }

    /**
     * Support for Closure "right" currying.
     *
     * @param argument the argument to bind
     * @return the new closure with the argument bound
     * @see #rcurry(Object...)
     */
    public Closure<V> rcurry(final Object argument) {
        return rcurry(new Object[]{argument});
    }

    /**
     * Support for Closure currying at a given index.
     * Parameters are supplied from index position "n".
     * Typical usage:
     * <pre>
     * def caseInsensitive = { a, b {@code ->} a.toLowerCase() {@code <=>} b.toLowerCase() } as Comparator
     * def caseSensitive = { a, b {@code ->} a {@code <=>} b } as Comparator
     * def animals1 = ['ant', 'dog', 'BEE']
     * def animals2 = animals1 + ['Cat']
     * // curry middle param of this utility method:
     * // Collections#binarySearch(List list, Object key, Comparator c)
     * {@code def catSearcher = Collections.&binarySearch.ncurry(1, "cat")}
     * [[animals1, animals2], [caseInsensitive, caseSensitive]].combinations().each{ a, c {@code ->}
     *   def idx = catSearcher(a.sort(c), c)
     *   print a.sort(c).toString().padRight(22)
     *   {@code if (idx < 0) println "Not found but would belong in position ${-idx - 1}"}
     *   else println "Found at index $idx"
     * }
     * // {@code =>}
     * // [ant, BEE, dog]       Not found but would belong in position 2
     * // [ant, BEE, Cat, dog]  Found at index 2
     * // [BEE, ant, dog]       Not found but would belong in position 2
     * // [BEE, Cat, ant, dog]  Not found but would belong in position 3
     * </pre>
     *
     * The position of the curried parameters will be calculated eagerly
     * and implies all arguments prior to the specified n index are supplied.
     * Default parameter values prior to the n index will not be available.
     *
     * @param n the index from which to bind parameters (may be -ve in which case it will be normalized)
     * @param arguments the arguments to bind
     * @return the new closure with its arguments bound
     * @see #curry(Object...)
     */
    public Closure<V> ncurry(int n, final Object... arguments) {
        return new CurriedClosure<V>(n, this, arguments);
    }

    /**
     * Support for Closure currying at a given index.
     *
     * @param argument the argument to bind
     * @return the new closure with the argument bound
     * @see #ncurry(int, Object...)
     */
    public Closure<V> ncurry(int n, final Object argument) {
        return ncurry(n, new Object[]{argument});
    }

    /**
     * Support for Closure forward composition.
     * <p>
     * Typical usage:
     * <pre class="language-groovy groovyTestCase">
     * def times2 = { a {@code ->} a * 2 }
     * def add3 = { a {@code ->} a + 3 }
     * def timesThenAdd = times2 {@code >>} add3
     * // equivalent: timesThenAdd = { a {@code ->} add3(times2(a)) }
     * assert timesThenAdd(3) == 9
     * </pre>
     *
     * @param other the Closure to compose with the current Closure
     * @return the new composed Closure
     */
    public <W> Closure<W> rightShift(final Closure<W> other) {
        return new ComposedClosure<W>(this, other);
    }

    /**
     * Support for Closure reverse composition.
     * <p>
     * Typical usage:
     * <pre class="language-groovy groovyTestCase">
     * def times2 = { a {@code ->} a * 2 }
     * def add3 = { a {@code ->} a + 3 }
     * def addThenTimes = times2 {@code <<} add3
     * // equivalent: addThenTimes = { a {@code ->} times2(add3(a)) }
     * assert addThenTimes(3) == 12
     * </pre>
     *
     * @param other the Closure to compose with the current Closure
     * @return the new composed Closure
     */
    public Closure<V> leftShift(final Closure other) {
        return new ComposedClosure<V>(other, this);
    }

    /**
     * Alias for {@link #rightShift(Closure)}
     *
     * @return the newly composed closure
     */
    public <W> Closure<W> andThen(final Closure<W> other) {
        return rightShift(other);
    }

    /**
     * Call {@link #andThen(Closure)} on {@code this}.
     *
     * @return the newly composed closure
     */
    public Closure<V> andThenSelf() {
        return andThen(this);
    }

    /**
     * Call {@link #andThen(Closure)} on {@code this} exactly {@code times} times.
     *
     * @param times the number of times to reverse compose the closure with itself
     * @return the newly composed closure
     */
    public Closure<V> andThenSelf(int times) {
        if (times == 0) return this;
        if (times == 1) return andThen(this);
        return andThen(andThenSelf(times - 1));
    }

    /**
     * Alias for {@link #leftShift(Closure)}
     *
     * @return the newly composed closure
     */
    public Closure<V> compose(final Closure other) {
        return leftShift(other);
    }

    /**
     * Call {@link #compose(Closure)} on {@code this}.
     *
     * @return the newly composed closure
     */
    public Closure<V> composeSelf() {
        return compose(this);
    }

    /**
     * Call {@link #compose(Closure)} on {@code this} exactly {@code times} times.
     *
     * @param times the number of times to compose the closure with itself
     * @return the newly composed closure
     */
    public Closure<V> composeSelf(int times) {
        if (times == 0) return this;
        if (times == 1) return compose(this);
        return compose(composeSelf(times - 1));
    }

    /**
     * Alias for calling a Closure for non-closure arguments.
     * <p>
     * Typical usage:
     * <pre class="language-groovy groovyTestCase">
     * def times2 = { a {@code ->} a * 2 }
     * def add3 = { a {@code ->} a + 3 }
     * assert add3 {@code <<} times2 {@code <<} 3 == 9
     * </pre>
     *
     * @param arg the argument to call the closure with
     * @return the result of calling the Closure
     */
    public V leftShift(final Object arg) {
        return call(arg);
    }

    /**
     * Creates a caching variant of the closure.
     * Whenever the closure is called, the mapping between the parameters and the return value is preserved in cache
     * making subsequent calls with the same arguments fast.
     * This variant will keep all cached values forever, i.e. till the closure gets garbage-collected.
     * The returned function can be safely used concurrently from multiple threads, however, the implementation
     * values high average-scenario performance and so concurrent calls on the memoized function with identical argument values
     * may not necessarily be able to benefit from each other's cached return value. With this having been mentioned,
     * the performance trade-off still makes concurrent use of memoized functions safe and highly recommended.
     *
     * The cache gets garbage-collected together with the memoized closure.
     *
     * @return A new closure forwarding to the original one while caching the results
     */
    public Closure<V> memoize() {
        return Memoize.buildMemoizeFunction(new ConcurrentCommonCache(), this);
    }

    /**
     * Creates a caching variant of the closure with upper limit on the cache size.
     * Whenever the closure is called, the mapping between the parameters and the return value is preserved in cache
     * making subsequent calls with the same arguments fast.
     * This variant will keep all values until the upper size limit is reached. Then the values in the cache start rotating
     * using the LRU (Last Recently Used) strategy.
     * The returned function can be safely used concurrently from multiple threads, however, the implementation
     * values high average-scenario performance and so concurrent calls on the memoized function with identical argument values
     * may not necessarily be able to benefit from each other's cached return value. With this having been mentioned,
     * the performance trade-off still makes concurrent use of memoized functions safe and highly recommended.
     *
     * The cache gets garbage-collected together with the memoized closure.
     *
     * @param maxCacheSize The maximum size the cache can grow to
     * @return A new function forwarding to the original one while caching the results
     */
    public Closure<V> memoizeAtMost(final int maxCacheSize) {
        if (maxCacheSize < 0) throw new IllegalArgumentException("A non-negative number is required as the maxCacheSize parameter for memoizeAtMost.");

        return Memoize.buildMemoizeFunction(new LRUCache(maxCacheSize), this);
    }

    /**
     * Creates a caching variant of the closure with automatic cache size adjustment and lower limit
     * on the cache size.
     * Whenever the closure is called, the mapping between the parameters and the return value is preserved in cache
     * making subsequent calls with the same arguments fast.
     * This variant allows the garbage collector to release entries from the cache and at the same time allows
     * the user to specify how many entries should be protected from the eventual gc-initiated eviction.
     * Cached entries exceeding the specified preservation threshold are made available for eviction based on
     * the LRU (Last Recently Used) strategy.
     * Given the non-deterministic nature of garbage collector, the actual cache size may grow well beyond the limits
     * set by the user if memory is plentiful.
     * The returned function can be safely used concurrently from multiple threads, however, the implementation
     * values high average-scenario performance and so concurrent calls on the memoized function with identical argument values
     * may not necessarily be able to benefit from each other's cached return value. Also the protectedCacheSize parameter
     * might not be respected accurately in such scenarios for some periods of time. With this having been mentioned,
     * the performance trade-off still makes concurrent use of memoized functions safe and highly recommended.
     *
     * The cache gets garbage-collected together with the memoized closure.
     * @param protectedCacheSize Number of cached return values to protect from garbage collection
     * @return A new function forwarding to the original one while caching the results
     */
    public Closure<V> memoizeAtLeast(final int protectedCacheSize) {
        if (protectedCacheSize < 0) throw new IllegalArgumentException("A non-negative number is required as the protectedCacheSize parameter for memoizeAtLeast.");

        return Memoize.buildSoftReferenceMemoizeFunction(protectedCacheSize, new ConcurrentSoftCache<Object, Object>(), this);
    }

    /**
     * Creates a caching variant of the closure with automatic cache size adjustment and lower and upper limits
     * on the cache size.
     * Whenever the closure is called, the mapping between the parameters and the return value is preserved in cache
     * making subsequent calls with the same arguments fast.
     * This variant allows the garbage collector to release entries from the cache and at the same time allows
     * the user to specify how many entries should be protected from the eventual gc-initiated eviction.
     * Cached entries exceeding the specified preservation threshold are made available for eviction based on
     * the LRU (Last Recently Used) strategy.
     * Given the non-deterministic nature of garbage collector, the actual cache size may grow well beyond the protected
     * size limits set by the user, if memory is plentiful.
     * Also, this variant will never exceed in size the upper size limit. Once the upper size limit has been reached,
     * the values in the cache start rotating using the LRU (Last Recently Used) strategy.
     * The returned function can be safely used concurrently from multiple threads, however, the implementation
     * values high average-scenario performance and so concurrent calls on the memoized function with identical argument values
     * may not necessarily be able to benefit from each other's cached return value. Also the protectedCacheSize parameter
     * might not be respected accurately in such scenarios for some periods of time. With this having been mentioned,
     * the performance trade-off still makes concurrent use of memoized functions safe and highly recommended.
     *
     * The cache gets garbage-collected together with the memoized closure.
     * @param protectedCacheSize Number of cached return values to protect from garbage collection
     * @param maxCacheSize The maximum size the cache can grow to
     * @return A new function forwarding to the original one while caching the results
     */
    public Closure<V> memoizeBetween(final int protectedCacheSize, final int maxCacheSize) {
        if (protectedCacheSize < 0) throw new IllegalArgumentException("A non-negative number is required as the protectedCacheSize parameter for memoizeBetween.");
        if (maxCacheSize < 0) throw new IllegalArgumentException("A non-negative number is required as the maxCacheSize parameter for memoizeBetween.");
        if (protectedCacheSize > maxCacheSize) throw new IllegalArgumentException("The maxCacheSize parameter to memoizeBetween is required to be greater or equal to the protectedCacheSize parameter.");

        return Memoize.buildSoftReferenceMemoizeFunction(protectedCacheSize, new ConcurrentSoftCache<Object, Object>(maxCacheSize), this);
    }

    /**
     * Builds a trampolined variant of the current closure.
     * To prevent stack overflow due to deep recursion, functions can instead leverage the trampoline mechanism
     * and avoid recursive calls altogether. Under trampoline, the function is supposed to perform one step of
     * the calculation and, instead of a recursive call to itself or another function, it returns a new closure,
     * which will be executed by the trampoline as the next step.
     * Once a non-closure value is returned, the trampoline stops and returns the value as the final result.
     * Here is an example:
     * <pre>
     * def fact
     * fact = { n, total {@code ->}
     *     n == 0 ? total : fact.trampoline(n - 1, n * total)
     * }.trampoline()
     * def factorial = { n {@code ->} fact(n, 1G)}
     * println factorial(20) // {@code =>} 2432902008176640000
     * </pre>
     *
     * @param args Parameters to the closure, so as the trampoline mechanism can call it
     * @return A closure, which will execute the original closure on a trampoline.
     */
    public Closure<V> trampoline(final Object... args) {
        return new TrampolineClosure<V>(this.curry(args));
    }

    /**
     * Builds a trampolined variant of the current closure.
     * To prevent stack overflow due to deep recursion, functions can instead leverage the trampoline mechanism
     * and avoid recursive calls altogether. Under trampoline, the function is supposed to perform one step of
     * the calculation and, instead of a recursive call to itself or another function, it returns a new closure,
     * which will be executed by the trampoline as the next step.
     * Once a non-closure value is returned, the trampoline stops and returns the value as the final result.
     * @return A closure, which will execute the original closure on a trampoline.
     * @see #trampoline(Object...)
     */
    public Closure<V> trampoline() {
        return new TrampolineClosure<V>(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            return null;
        }
    }

    /*
     * Implementation note:
     *   This has to be an inner class!
     *
     * Reason:
     *   Closure.this.call will call the outer call method, but
     * with the inner class as executing object. This means any
     * invokeMethod or getProperty call will be called on this
     * inner class instead of the outer!
     */
    private class WritableClosure extends Closure implements Writable {
        @Serial private static final long serialVersionUID = -5749205698681690370L;

        private WritableClosure() {
            super(Closure.this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Writer writeTo(Writer out) throws IOException {
            Closure.this.call(new Object[]{out});

            return out;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object invokeMethod(String method, Object arguments) {
            if ("clone".equals(method)) {
                return clone();
            }
            if ("curry".equals(method)) {
                return curry((Object[]) arguments);
            }
            if ("asWritable".equals(method)) {
                return asWritable();
            }
            return Closure.this.invokeMethod(method, arguments);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getProperty(String property) {
            return Closure.this.getProperty(property);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setProperty(String property, Object newValue) {
            Closure.this.setProperty(property, newValue);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object call() {
            return ((Closure) getOwner()).call();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object call(Object arguments) {
            return ((Closure) getOwner()).call(arguments);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object call(Object... args) {
            return ((Closure) getOwner()).call(args);
        }

        /**
         * Delegates to {@link #call(Object...)}.
         *
         * @param args the call arguments
         * @return the call result
         */
        public Object doCall(Object... args) {
            return call(args);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getDelegate() {
            return Closure.this.getDelegate();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setDelegate(Object delegate) {
            Closure.this.setDelegate(delegate);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class[] getParameterTypes() {
            return Closure.this.getParameterTypes();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getMaximumNumberOfParameters() {
            return Closure.this.getMaximumNumberOfParameters();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Closure asWritable() {
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            Closure.this.run();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object clone() {
            return ((Closure) Closure.this.clone()).asWritable();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Closure.this.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object arg0) {
            return Closure.this.equals(arg0);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            final Writer writer = new StringBuilderWriter();

            try {
                writeTo(writer);
            } catch (IOException e) {
                return "";
            }

            return writer.toString();
        }

        /**
         * Returns a writable curried variant of this closure.
         *
         * @param arguments the arguments to bind
         * @return the curried writable closure
         */
        @Override
        public Closure curry(final Object... arguments) {
            return (new CurriedClosure(this, arguments)).asWritable();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setResolveStrategy(int resolveStrategy) {
            Closure.this.setResolveStrategy(resolveStrategy);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getResolveStrategy() {
            return Closure.this.getResolveStrategy();
        }
    }

    /**
     * @return Returns the directive.
     */
    public int getDirective() {
        return directive;
    }

    /**
     * @param directive The directive to set.
     */
    public void setDirective(int directive) {
        this.directive = directive;
    }

    /**
     * Returns a copy of this closure where the "owner", "delegate" and "thisObject"
     * fields are null, allowing proper serialization when one of them is not serializable.
     *
     * @return a serializable closure.
     *
     * @since 1.8.5
     */
    public Closure<V> dehydrate() {
        @SuppressWarnings("unchecked")
        Closure<V> result = (Closure<V>) this.clone();
        result.delegate = null;
        result.owner = null;
        result.thisObject = null;
        return result;
    }

    /**
     * Returns a copy of this closure for which the delegate, owner and thisObject are
     * replaced with the supplied parameters. Use this when you want to rehydrate a
     * closure which has been made serializable thanks to the {@link #dehydrate()}
     * method.
     * @param delegate the closure delegate
     * @param owner the closure owner
     * @param thisObject the closure "this" object
     * @return a copy of this closure where owner, delegate and thisObject are replaced
     *
     * @since 1.8.5
     */
    public Closure<V> rehydrate(Object delegate, Object owner, Object thisObject) {
        @SuppressWarnings("unchecked")
        Closure<V> result = (Closure<V>) this.clone();
        result.delegate = delegate;
        result.owner = owner;
        result.thisObject = thisObject;
        return result;
    }

    /**
     * Verifies, during deserialization, that the {@code owner}/{@code delegate}/{@code thisObject}
     * references reachable from {@code root} do not form a cycle through other closures. Such a cycle
     * cannot arise from normal Groovy code or from the {@link #dehydrate()}/{@link #rehydrate} round-trip,
     * but it can be forged in a hand-crafted serialized stream; invoking the resulting closure would then
     * recurse indefinitely and exhaust the stack (a denial-of-service "gadget").
     * <p>
     * This is exposed as a {@code static} helper, rather than a {@code readResolve}/{@code readObject} hook
     * on {@code Closure} itself, on purpose:
     * <ul>
     *   <li>a hook on {@code Closure} would have to be inheritable (i.e. {@code protected}) to reach
     *       generated subclasses, which would force every existing subclass that declares the idiomatic
     *       {@code private readResolve()} to widen its visibility and fail to compile;</li>
     *   <li>a {@code readObject} hook would additionally interpose this (core-loaded) class on the stack
     *       while a closure's fields are read, shifting {@link java.io.ObjectInputStream}'s
     *       latest-user-defined loader away from the loader that defined the closure's captured types.</li>
     * </ul>
     * Groovy's own serializable gadget closures ({@link org.codehaus.groovy.runtime.CurriedClosure},
     * {@link org.codehaus.groovy.runtime.ComposedClosure}) call this from their {@code private readResolve()}.
     * Any other {@code Closure} subclass that participates in serialization can opt in the same way (the
     * method is {@code protected static}, so it is reachable from subclasses but not from arbitrary code).
     *
     * @param root the freshly-deserialized closure to validate
     * @throws InvalidObjectException if a closure reference cycle is detected
     * @since 6.0.0
     */
    protected static void checkForReferenceCycle(final Closure<?> root) throws InvalidObjectException {
        // Iterative depth-first search over the raw owner/delegate/thisObject fields, following only
        // Closure-valued links. "grey" = on the current DFS path, "black" = fully explored. An edge back
        // to a grey node is a genuine cycle; an edge to a black node is a harmless shared reference (e.g.
        // a curried closure whose owner and delegate point at the same wrapped closure). The raw fields
        // are read directly (not via the overridable getters, which would themselves recurse on a cyclic
        // graph); access is permitted as this is the declaring class.
        final Set<Closure<?>> grey = Collections.newSetFromMap(new IdentityHashMap<>());
        final Set<Closure<?>> black = Collections.newSetFromMap(new IdentityHashMap<>());
        final Deque<Object> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            final Object top = stack.peek();
            if (top instanceof Marker) {
                stack.pop();
                final Closure<?> done = ((Marker) top).closure;
                grey.remove(done);
                black.add(done);
                continue;
            }
            final Closure<?> node = (Closure<?>) stack.pop();
            if (black.contains(node) || grey.contains(node)) {
                continue; // already handled via a shared reference
            }
            grey.add(node);
            stack.push(new Marker(node));
            for (final Object link : new Object[]{node.owner, node.delegate, node.thisObject}) {
                if (link instanceof Closure<?> child) {
                    if (grey.contains(child)) {
                        throw new InvalidObjectException(
                                "Closure owner/delegate/thisObject references form a cycle; refusing to deserialize");
                    }
                    if (!black.contains(child)) {
                        stack.push(child);
                    }
                }
            }
        }
    }

    /** Sentinel pushed below a node's children during the {@link #checkForReferenceCycle} cycle check. */
    private static final class Marker {
        final Closure<?> closure;

        Marker(final Closure<?> closure) {
            this.closure = closure;
        }
    }

    /**
     * NOTE: this reflective cache is a bridge, not a destination. For compiler-generated closure
     * classes the long-term plan (GEP-27) is for the compiler to emit a {@code call(Object...)}
     * override that arity-dispatches directly to the right {@code doCall} — plain virtual
     * dispatch, no reflection, module-system-clean — at which point this cache serves only
     * legacy jars and hand-written subclasses. Extend it with that destination in mind.
     */
    private static final class CallOverride {
        /**
         * Cached arities: {@code 0..ARITY_LIMIT-1}. Sized to cover every callback shape the GDK
         * drives (iteration and inject/withIndex variants peak at three parameters, plus slack).
         */
        static final int ARITY_LIMIT = 5;
        /**
         * Marker instance indicating that no dispatch targets exist.
         */
        static final CallOverride NONE = new CallOverride(new Method[ARITY_LIMIT], new Class[ARITY_LIMIT][], new boolean[ARITY_LIMIT]);
        /**
         * Cached {@code doCall} targets indexed by parameter count, or null where absent,
         * ambiguous, or inaccessible — {@code call} selects a {@code doCall}, so the cache is
         * keyed on the {@code doCall} declarations themselves. At each arity an all-{@code Object}
         * doCall wins outright; otherwise the single unambiguous doCall with declared parameter
         * types is cached with guards (GROOVY-12164/12165). For a subclass declaring NO doCall at
         * all, the GROOVY-11911 compatibility carve-out caches its {@code call()}/{@code call(Object)}
         * overrides instead (the historical Java-written adapter shape).
         */
        final Method[] byArity;
        /**
         * Per-arity instance-of guards for {@link #byArity}: the declared parameter types
         * (boxed for primitives), with null entries for {@code Object} parameters; a wholly
         * null guard array means no checks at all. An argument that is not already an instance
         * of its declared type needs Groovy coercion, which only the metaclass fallback provides.
         */
        final Class<?>[][] guards;
        /**
         * Which cached targets are {@code call} forms (the 11911 shapes) rather than doCall
         * bodies. Only those need the re-entry latch: a custom {@code call} override may
         * delegate back to {@code call(...)}, which must not re-dispatch to the same target.
         * doCall targets are user bodies — re-entry through them is ordinary recursion — so
         * their dispatch skips the ThreadLocal entirely (and nested closure calls inside a
         * dispatched body keep their own fast path).
         */
        final boolean[] callForm;

        private CallOverride(Method[] byArity, Class<?>[][] guards, boolean[] callForm) {
            this.byArity = byArity;
            this.guards = guards;
            this.callForm = callForm;
        }

        static boolean guardsPass(Class<?>[] guards, Object[] args) {
            if (guards == null) return true; // all parameters are Object
            for (int i = 0, n = args.length; i < n; i += 1) {
                Class<?> g = guards[i];
                if (g != null && !g.isInstance(args[i])) return false;
            }
            return true;
        }

        /**
         * Finds the subclass's {@code doCall} declarations, one target per arity — {@code call}
         * selects a {@code doCall}, so the cache is keyed on the doCall declarations themselves
         * and any visibility the MOP would dispatch is accepted (the hierarchy is walked with
         * {@code getDeclaredMethods}; an overridden signature keeps its most-derived form).
         * Bridge methods are skipped (their erased twin is the real declaration), as are static
         * declarations and methods with array-typed parameters (that shape belongs to vararg
         * collection, which is metaclass work). Same-arity overloads beyond an all-Object one
         * are ambiguous: the metaclass performs the selection.
         * <p>
         * Declared {@code call()}/{@code call(Object)} overrides (the GROOVY-11911 shapes) take
         * precedence at their arities: DGM's {@code closure.call(item)} reaches such an override
         * through plain virtual dispatch anyway, so the varargs entry must agree, and a
         * decorating override must not be bypassed. Typed or multi-argument {@code call}
         * overloads are NOT honoured — per the {@code Closure} contract, only doCall carries
         * the body.
         *
         * @param type the closure subclass
         * @return the resolved dispatch metadata
         */
        static CallOverride lookup(Class<?> type) {
            if (type == Closure.class) return NONE;
            // The cache is only valid where it mirrors the MOP's selection. MetaClassImpl's
            // invokeMethod has dedicated dispatch for these adapter types (a method pointer
            // invokes its target method; a curried closure uncurries and re-enters), so their
            // doCall declarations, where present, are NOT what the MOP would select — e.g.
            // MethodClosure's vestigial doCall would mis-dispatch a class-owner method pointer.
            if (org.codehaus.groovy.runtime.MethodClosure.class.isAssignableFrom(type)
                    || org.codehaus.groovy.runtime.CurriedClosure.class.isAssignableFrom(type)) {
                return NONE;
            }
            Method[] exact = new Method[ARITY_LIMIT];
            Method[] typed = new Method[ARITY_LIMIT];
            boolean[] typedAmbiguous = new boolean[ARITY_LIMIT];
            Set<String> seen = new HashSet<>();
            for (Class<?> c = type; c != null && c != Closure.class; c = c.getSuperclass()) {
                for (Method m : c.getDeclaredMethods()) {
                    if (!"doCall".equals(m.getName()) || m.isBridge() || Modifier.isStatic(m.getModifiers())) {
                        continue;
                    }
                    Class<?>[] params = m.getParameterTypes();
                    if (!seen.add(java.util.Arrays.toString(params))) continue; // overridden below: most-derived wins
                    int arity = m.getParameterCount();
                    if (arity >= ARITY_LIMIT) continue;
                    boolean allObject = true, hasArray = false;
                    for (Class<?> p : params) {
                        if (p != Object.class) allObject = false;
                        if (p.isArray()) hasArray = true;
                    }
                    if (hasArray) continue;
                    if (allObject) {
                        if (exact[arity] == null) exact[arity] = m;
                    } else if (typed[arity] != null) {
                        typedAmbiguous[arity] = true;
                    } else {
                        typed[arity] = m;
                    }
                }
            }
            Method[] byArity = new Method[ARITY_LIMIT];
            Class<?>[][] guards = new Class[ARITY_LIMIT][];
            boolean[] callForm = new boolean[ARITY_LIMIT];
            boolean any = false;
            // GROOVY-11911: a declared call()/call(Object) override takes precedence at its
            // arity even when doCall methods exist — DGM's closure.call(item) reaches such an
            // override through plain virtual dispatch anyway, so the varargs entry must agree
            // (a decorating override would otherwise be silently bypassed on one form only).
            Method zero = findOverride(type);
            if (zero != null) {
                byArity[0] = zero;
                callForm[0] = true;
                any = true;
            }
            Method one = findOverride(type, Object.class);
            if (one != null) {
                byArity[1] = one;
                callForm[1] = true;
                any = true;
            }
            for (int arity = 0; arity < ARITY_LIMIT; arity += 1) {
                if (byArity[arity] != null) continue; // a call-form override claimed this arity
                Method m = (exact[arity] != null) ? exact[arity]
                        : (typedAmbiguous[arity] ? null : typed[arity]);
                if (m == null) continue;
                try {
                    m.setAccessible(true);
                } catch (RuntimeException ignored) {
                    // module/package access denied; fall through to MOP doCall
                    continue;
                }
                byArity[arity] = m;
                if (m != exact[arity]) {
                    Class<?>[] params = m.getParameterTypes();
                    Class<?>[] g = new Class[arity];
                    for (int i = 0; i < arity; i += 1) {
                        g[i] = (params[i] == Object.class) ? null : wrapperOf(params[i]);
                    }
                    guards[arity] = g;
                }
                any = true;
            }
            return any ? new CallOverride(byArity, guards, callForm) : NONE;
        }

        private static Method findOverride(Class<?> type, Class<?>... params) {
            try {
                Method m = type.getMethod("call", params);
                // a static call(...) is not an instance override: reflective invocation would
                // silently ignore the receiver, where the MOP fallback dispatches instance doCall
                if (m.getDeclaringClass() == Closure.class || Modifier.isStatic(m.getModifiers())) return null;
                try {
                    m.setAccessible(true);
                } catch (RuntimeException ignored) {
                    // module/package access denied; fall through to MOP doCall
                    return null;
                }
                return m;
            } catch (NoSuchMethodException ignored) {
                return null;
            }
        }

        private static Class<?> wrapperOf(Class<?> type) {
            if (!type.isPrimitive()) return type;
            if (type == int.class) return Integer.class;
            if (type == long.class) return Long.class;
            if (type == boolean.class) return Boolean.class;
            if (type == double.class) return Double.class;
            if (type == char.class) return Character.class;
            if (type == byte.class) return Byte.class;
            if (type == short.class) return Short.class;
            if (type == float.class) return Float.class;
            return type; // void: unreachable for a parameter type
        }
    }
}
