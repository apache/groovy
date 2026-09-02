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
package org.codehaus.groovy.reflection;

import groovy.lang.MetaMethod;
import groovy.lang.MissingMethodException;
import org.apache.groovy.internal.runtime.invoke.DirectInvoker;
import org.apache.groovy.internal.runtime.invoke.InvokerFactory;
import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.MetaClassHelper;

import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Caches reflection information about a single method for fast lookup and invocation.
 * <p>
 * Extends {@link MetaMethod} and implements {@link Comparable} for method sorting.
 * Provides efficient access to method metadata for MOP and invokedynamic dispatch.
 */
@SuppressWarnings("rawtypes")
public class CachedMethod extends MetaMethod implements Comparable {

    /**
     * An empty array constant representing zero cached methods.
     */
    public static final CachedMethod[] EMPTY_ARRAY = new CachedMethod[0];

    /**
     * Finds a {@code CachedMethod} corresponding to the specified Java method
     * by searching the cached methods of its declaring class.
     *
     * @param method the method to find
     * @return the cached method, or {@code null} if not found
     */
    public static CachedMethod find(final Method method) {
        CachedMethod[] methods = ReflectionCache.getCachedClass(method.getDeclaringClass()).getMethods();
        int i = Arrays.binarySearch(methods, method, (o1, o2) -> {
            if (o1 instanceof CachedMethod) {
                return ((CachedMethod) o1).compareTo(o2);
            } else if (o2 instanceof CachedMethod) {
                return -((CachedMethod) o2).compareTo(o1);
            }
            // really, this should never happen, it's evidence of corruption if it does
            throw new ClassCastException("One of the two comparables must be a CachedMethod");
        });
        return (i < 0 ? null : methods[i]);
    }

    //--------------------------------------------------------------------------

    /**
     * The cached class that declares this method.
     */
    public  final CachedClass cachedClass;
    private final Method cachedMethod;

    private int hashCode;
    private Boolean callerSensitive;

    private boolean makeAccessibleDone;
    private CachedMethod transformedMethod;

    /**
     * Installed JIT-constant trampoline, or {@code null} if not yet generated
     * / sticky-failed. Volatile publishes an immutable referent (the generated
     * class has no mutable fields); the fast path reads it outside the
     * generation lock. Not an {@code AtomicReference}: every {@code CachedMethod}
     * would pay an extra object, including those that never inflate.
     */
    private transient volatile DirectInvoker invoker; // NOSONAR java:S3077 — immutable trampoline, volatile for publication only
    /**
     * Racy hit counter. {@code long} to match
     * {@code IndyInterface.INDY_OPTIMIZE_THRESHOLD}. Over-counting
     * generates slightly early; under-counting (lost increments)
     * delays generation. Both harmless.
     */
    private transient long invokeHits;
    /**
     * Cached {@link InvokerFactory#PROPERTY_THRESHOLD} so lukewarm invokes
     * do not call {@code Long.getLong} every time. {@link Long#MIN_VALUE}
     * means unread. Tests construct a fresh {@code CachedMethod} after
     * changing the property.
     */
    private transient long cachedThreshold = Long.MIN_VALUE;
    /**
     * Sticky failure: {@link InvokerFactory#tryCreate} returned {@code null}.
     * Volatile because the fast path reads it outside the generation lock.
     */
    private transient volatile boolean invokerAttempted;

    /**
     * The JDK's {@code CallerSensitive} marker lives in {@code jdk.internal}
     * and is not a compile-time dependency. Resolved once; {@code null} means
     * the probe cannot see the annotation type and callers must be treated as
     * caller-sensitive.
     */
    private static final Class<? extends Annotation> CALLER_SENSITIVE = findCallerSensitive();

    /**
     * Constructs a {@code CachedMethod} for the given method within a cached class.
     *
     * @param clazz the cached class that declares this method
     * @param method the Java method to cache
     */
    public CachedMethod(final CachedClass clazz, final Method method) {
        this.cachedMethod = method;
        this.cachedClass = clazz;
    }

    /**
     * Constructs a {@code CachedMethod} for the given Java method.
     * Automatically resolves the cached class from the method's declaring class.
     *
     * @param method the Java method to cache
     */
    public CachedMethod(final Method method) {
        this(ReflectionCache.getCachedClass(method.getDeclaringClass()), method);
    }

    /**
     * Compares this cached method with another method-like object for ordering.
     * Comparison is based on method name, return type, parameters, and declaring class.
     *
     * @param other the object to compare with (typically another {@code CachedMethod} or {@link Method})
     * @return a negative, zero, or positive value if this method is less than, equal to, or greater than the other
     */
    @Override
    public int compareTo(final Object other) {
        if (other == this) return 0;
        if (other == null) return -1;
        return (other instanceof CachedMethod ? compareToCachedMethod((CachedMethod) other) : compareToMethod((Method) other));
    }

    private int compareToCachedMethod(final CachedMethod other) {
        int strComp = getName().compareTo(other.getName());
        if (strComp != 0)
            return strComp;

        int retComp = getReturnType().getName().compareTo(other.getReturnType().getName());
        if (retComp != 0)
            return retComp;

        CachedClass[] params = getParameterTypes();
        CachedClass[] otherParams = other.getParameterTypes();

        int pd = params.length - otherParams.length;
        if (pd != 0)
            return pd;

        for (int i = 0, n = params.length; i < n; i += 1) {
            final int nameComp = params[i].getName().compareTo(otherParams[i].getName());
            if (nameComp != 0)
                return nameComp;
        }

        final int classComp = cachedClass.toString().compareTo(other.getDeclaringClass().toString());
        if (classComp != 0)
            return classComp;

        throw new RuntimeException("Should never happen");
    }

    private int compareToMethod(final Method other) {
        int strComp = getName().compareTo(other.getName());
        if (strComp != 0)
            return strComp;

        int retComp = getReturnType().getName().compareTo(other.getReturnType().getName());
        if (retComp != 0)
            return retComp;

        CachedClass[] params = getParameterTypes();
        Class<?>[] mparams = other.getParameterTypes();

        int pd = params.length - mparams.length;
        if (pd != 0)
            return pd;

        for (int i = 0, n = params.length; i < n; i += 1) {
            final int nameComp = params[i].getName().compareTo(mparams[i].getName());
            if (nameComp != 0)
                return nameComp;
        }

        return 0;
    }

    /**
     * Checks equality with another method.
     * Two methods are equal if they represent the same underlying Java method.
     *
     * @param other the object to compare with
     * @return {@code true} if this method equals the other object; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof CachedMethod && cachedMethod.equals(((CachedMethod) other).cachedMethod))
            || (other instanceof Method && cachedMethod.equals(other));
    }

    /**
     * Returns the hash code for this cached method based on the underlying Java method.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        if (hashCode == 0) {
           hashCode = cachedMethod.hashCode();
           if (hashCode == 0) hashCode = 0xcafebebe;
        }
        return hashCode;
    }

    /**
     * Returns the string representation of the underlying Java method.
     *
     * @return a string representation of this cached method
     */
    @Override
    public String toString() {
        return cachedMethod.toString();
    }

    //--------------------------------------------------------------------------

    /**
     * Checks whether the given caller class can legally access this method.
     *
     * @param callerClass the class attempting to access this method
     * @return {@code true} if access is allowed; {@code false} otherwise
     */
    public boolean canAccessLegally(final Class<?> callerClass) {
        return ReflectionUtils.checkAccessible(callerClass, cachedMethod.getDeclaringClass(), cachedMethod.getModifiers(), false);
    }

    /**
     * Returns the annotation of the specified type on this method, if present.
     *
     * @param <T> the annotation type
     * @param annotationClass the class of the annotation to retrieve
     * @return the annotation instance, or {@code null} if not present
     */
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        return cachedMethod.getAnnotation(annotationClass);
    }

    /**
     * Returns the underlying Java method, making it accessible if necessary.
     *
     * @return the cached method with accessibility ensured
     */
    public Method getCachedMethod() {
        makeAccessibleIfNecessary();
        return cachedMethod;
    }

    /**
     * Returns the cached class that declares this method.
     *
     * @return the declaring cached class
     */
    @Override
    public CachedClass getDeclaringClass() {
        return cachedClass;
    }

    /**
     * Returns the bytecode method descriptor for this method (e.g., "(II)Z").
     * The descriptor encodes parameter and return types in JVM format.
     *
     * @return the method descriptor string
     */
    @Override
    public String getDescriptor() {
        return BytecodeHelper.getMethodDescriptor(getReturnType(), getNativeParameterTypes());
    }

    /**
     * Returns the modifiers of this method (e.g., public, static, synchronized).
     * See {@link java.lang.reflect.Modifier} for modifier constants.
     *
     * @return the method modifiers
     */
    @Override
    public int getModifiers() {
        return cachedMethod.getModifiers();
    }

    /**
     * Returns the name of this method.
     *
     * @return the method name
     */
    @Override
    public String getName() {
        return cachedMethod.getName();
    }

    /**
     * Returns the number of parameters this method accepts.
     *
     * @return the parameter count
     */
    public int getParamsCount() {
        return getParameterTypes().length;
    }

    /**
     * Returns the parameter type information for this method.
     * <p>
     * Note: This method always returns {@code null} and is retained for compatibility.
     *
     * @return {@code null}
     */
    public ParameterTypes getParamTypes() {
        return null;
    }

    /**
     * Returns the native Java parameter types of this method.
     *
     * @return an array of parameter classes
     */
    @Override
    public Class[] getPT() {
        return cachedMethod.getParameterTypes();
    }

    /**
     * Returns the return type of this method.
     *
     * @return the method's return class
     */
    @Override
    public Class getReturnType() {
        return cachedMethod.getReturnType();
    }

    /**
     * Returns the complete method signature (name + bytecode descriptor).
     * For example: "toString()Ljava/lang/String;"
     *
     * @return the method signature
     */
    @Override
    public String getSignature() {
        return getName() + getDescriptor();
    }

    /**
     * Returns whether this method is synthetic (generated by the compiler).
     *
     * @return {@code true} if this method is synthetic; {@code false} otherwise
     */
    @Override
    public boolean isSynthetic() {
        return cachedMethod.isSynthetic();
    }

    /**
     * Returns the transformed version of this method, if one exists.
     * Transformed methods are compiler-optimized variants.
     *
     * @return the transformed method, or {@code null} if no transformation exists
     */
    public CachedMethod getTransformedMethod() {
        return transformedMethod;
    }

    /**
     * Sets the transformed version of this method.
     *
     * @param transformedMethod the transformed method, or {@code null} to clear
     */
    public void setTransformedMethod(final CachedMethod transformedMethod) {
        this.transformedMethod = transformedMethod;
    }

    //--------------------------------------------------------------------------

    /**
     * Invokes this method on the given object with the specified arguments.
     * Handles accessibility, exception wrapping, and method invocation.
     *
     * <p>After {@code groovy.cachedmethod.invoker.threshold} hits (default 100)
     * a {@link DirectInvoker} may be installed so the call is a JIT-constant
     * {@code INVOKE*} / classData {@code invokeExact} rather than
     * {@link Method#invoke}. Caller-sensitive and abstract methods stay
     * reflective. This is the MOP "Groovy as caller" path — the trampoline
     * must not be used as an invokedynamic target.
     *
     * @param object the target object (may be {@code null} for static methods)
     * @param arguments the arguments to pass to the method
     * @return the result of the method invocation
     * @throws InvokerInvocationException if the method raises a checked exception or illegal access occurs
     * @throws RuntimeException if the method raises an uncaught exception (except MissingMethodException)
     * @throws ClassCastException on the generated path when an argument cannot
     *         be {@code CHECKCAST} to the parameter type (the reflective path
     *         wraps {@code IllegalArgumentException} in {@code InvokerInvocationException}
     *         instead; same delta as DGM / {@code CallSiteGenerator}). Wrong
     *         arity does not take that shortcut: too few or extra arguments
     *         fall back to {@link Method#invoke} so the wrapped
     *         {@code IllegalArgumentException} is preserved.
     */
    @Override
    public final Object invoke(final Object object, final Object[] arguments) {
        makeAccessibleIfNecessary();
        DirectInvoker di = invoker;
        if (di != null) {
            return invokeGenerated(di, object, arguments);
        }
        if (shouldAttemptGeneration()) {
            synchronized (this) {
                if (invoker == null && !invokerAttempted) {
                    DirectInvoker created = InvokerFactory.tryCreate(this);
                    if (created != null) {
                        invoker = created;
                    } else {
                        invokerAttempted = true;
                    }
                }
                di = invoker;
            }
            if (di != null) {
                return invokeGenerated(di, object, arguments);
            }
        }
        return invokeReflective(object, arguments);
    }

    private boolean shouldAttemptGeneration() {
        if (invokerAttempted) {
            return false;
        }
        if (!InvokerFactory.generationAllowed()) {
            // Sticky: do not re-read disable / hidden-class / Android flags
            // on every subsequent invoke. Kill switches are process-level.
            invokerAttempted = true;
            return false;
        }
        if (isCallerSensitive() || Modifier.isAbstract(getModifiers())) {
            invokerAttempted = true;
            return false;
        }
        long hits = ++invokeHits;
        long threshold = cachedThreshold;
        if (threshold == Long.MIN_VALUE) {
            threshold = SystemUtil.getLongSafe(InvokerFactory.PROPERTY_THRESHOLD, 100L);
            cachedThreshold = threshold;
        }
        return hits > threshold;
    }

    private Object invokeGenerated(final DirectInvoker di, final Object object, final Object[] arguments) {
        Object[] args = (arguments != null) ? arguments : MetaClassHelper.EMPTY_ARRAY;
        // Generated trampolines index declared slots only: too few would
        // AALOAD-AIOOBE, extra would be ignored. Method.invoke rejects both
        // (and packs varargs). Mismatch is the error / spread-varargs path.
        if (args.length != cachedMethod.getParameterCount()) {
            return invokeReflective(object, arguments);
        }
        try {
            return normalizeBoxedReturn(di.invoke(object, args));
        } catch (Throwable t) {
            // Match Method.invoke / ITE policy: RuntimeException other than
            // MissingMethodException is rethrown; Error, checked, and MME become
            // InvokerInvocationException (ITE used to wrap those as its cause).
            if (t instanceof RuntimeException && !(t instanceof MissingMethodException)) {
                throw (RuntimeException) t;
            }
            throw new InvokerInvocationException(t);
        }
    }

    private Object invokeReflective(final Object object, final Object[] arguments) {
        try {
            return normalizeBoxedReturn(cachedMethod.invoke(object, arguments));
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InvokerInvocationException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            throw (cause instanceof RuntimeException && !(cause instanceof MissingMethodException))
                    ? (RuntimeException) cause
                    : new InvokerInvocationException(e);
        }
    }

    /**
     * Keeps reference identity ({@code ===}) of primitive returns consistent
     * across dispatch paths (GROOVY-12140).
     *
     * @see MetaClassHelper#normalizeBoxedReturn(Object, Class)
     */
    private Object normalizeBoxedReturn(final Object value) {
        return MetaClassHelper.normalizeBoxedReturn(value, cachedMethod.getReturnType());
    }

    private void makeAccessibleIfNecessary() {
        if (!makeAccessibleDone) {
            ReflectionUtils.makeAccessibleInPrivilegedAction(cachedMethod);
            makeAccessibleDone = true;
        }
    }

    /**
     * Indicates whether the underlying method is caller-sensitive, meaning its
     * behaviour depends on the immediate caller — such methods must not be
     * invoked through intermediaries that change the observed caller. This
     * covers the {@code jdk.internal.reflect.CallerSensitive} annotation and
     * declaring-class-based cases that are caller-context sensitive without
     * the annotation (object serialization's stack-walking loader lookup).
     * Computed once and cached; when the probe cannot decide, the method is
     * conservatively treated as caller-sensitive. Note that an abstract
     * method never carries the annotation itself: callers dispatching
     * virtually should also probe the runtime receiver's implementation.
     *
     * @return {@code true} if the method is (or must be assumed) caller-sensitive
     * @since 6.0.0
     */
    public boolean isCallerSensitive() {
        Boolean sensitive = callerSensitive;
        if (sensitive == null) {
            sensitive = computeCallerSensitive(cachedMethod);
            callerSensitive = sensitive;
        }
        return sensitive;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Annotation> findCallerSensitive() {
        try {
            return (Class<? extends Annotation>) Class.forName("jdk.internal.reflect.CallerSensitive");
        } catch (ClassNotFoundException | LinkageError ignore) {
            return null;
        }
    }

    private static boolean computeCallerSensitive(final Method method) {
        // object serialization is caller-context sensitive without carrying
        // the annotation: its latestUserDefinedLoader walks the stack for the
        // class loader, so intermediary frames change its behavior
        Class<?> declaringClass = method.getDeclaringClass();
        if (ObjectInput.class.isAssignableFrom(declaringClass)
                || ObjectOutput.class.isAssignableFrom(declaringClass)
                || ObjectInputStream.class.isAssignableFrom(declaringClass)
                || ObjectOutputStream.class.isAssignableFrom(declaringClass)) {
            return true;
        }
        if (CALLER_SENSITIVE == null) {
            return true;
        }
        try {
            return method.isAnnotationPresent(CALLER_SENSITIVE);
        } catch (Throwable ignore) {
            return true;
        }
    }

    /**
     * Makes this method accessible and returns the underlying Java method.
     * Synonym for {@link #getCachedMethod()}.
     *
     * @return the cached method with accessibility ensured
     */
    public final Method setAccessible() {
        return getCachedMethod();
    }
}
