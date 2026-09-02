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
package org.apache.groovy.internal.runtime.invoke;

import groovy.transform.Internal;
import org.apache.groovy.util.HiddenClassDefiner;
import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.ClassLoaderForClassArtifacts;
import org.codehaus.groovy.reflection.android.AndroidSupport;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Factory for {@link DirectInvoker} instances bound to one {@link CachedMethod}.
 *
 * <p>Not user API. {@code CachedMethod.invoke} is the only production caller.
 * Package-visible helpers exist so tests can drive generation without going
 * through the hit-count hook.
 *
 * <p>Define path (one order):
 * <ol>
 *   <li>InvokerFactory nestmate + {@code INVOKE*} when the member is publicly
 *       invocable from this class and every type is resolvable here
 *       ({@code String.startsWith}).</li>
 *   <li>Declaring-class nestmate + {@code INVOKE*} when
 *       {@code privateLookupIn} is worth attempting and the host can resolve
 *       {@link DirectInvoker}.</li>
 *   <li>InvokerFactory nestmate + classData {@code MethodHandle} when this
 *       loader can resolve the erased {@code invokeExact} types.</li>
 *   <li>{@link ClassLoaderForClassArtifacts} visible class, only when that
 *       loader can resolve {@link DirectInvoker} — never for bootstrap hosts.</li>
 * </ol>
 *
 * Failures sticky-return {@code null}; they must not propagate to
 * {@code CachedMethod.invoke}.
 *
 * @since 6.0.0
 */
@Internal
public final class InvokerFactory {

    /**
     * Hits before generation. Default 100 — below
     * {@code groovy.indy.optimize.threshold} (1000) so cold indy still sits on
     * {@code doMethodInvoke} when the trampoline appears.
     */
    public static final String PROPERTY_THRESHOLD = "groovy.cachedmethod.invoker.threshold";

    /** Kill switch. Default {@code false}. */
    public static final String PROPERTY_DISABLE = "groovy.cachedmethod.invoker.disable";

    /**
     * Full-privilege lookup for <em>this</em> class — production nest host for
     * Steps 1 and 3. Caller-sensitive: must be captured here, not in
     * {@link HiddenClassDefiner}.
     */
    static final Lookup LOOKUP = MethodHandles.lookup();

    private InvokerFactory() {
    }

    /**
     * Attempts to bind a trampoline for {@code method}. Returns {@code null}
     * on any failure (sticky-fail). Public so {@code CachedMethod} in another
     * package can call it; not user API ({@link Internal}, {@code internal}
     * package). Tests in this package also drive generation through this
     * method without going through the hit-count hook.
     *
     * @param method the cached method to bind
     * @return a trampoline, or {@code null}
     */
    public static DirectInvoker tryCreate(final CachedMethod method) {
        if (method == null || !generationAllowed()) {
            return null;
        }
        if (method.isCallerSensitive() || Modifier.isAbstract(method.getModifiers())) {
            return null;
        }
        try {
            return defineSteps(method);
        } catch (Exception | LinkageError ignored) {
            // Sticky-fail expected define/link/access problems, including
            // VerifyError / NoClassDefFoundError from Step 4 defineClass.
            // Do not swallow VirtualMachineError (OOME / SOE).
            return null;
        }
    }

    /**
     * Package-visible predicate. Android cannot be flipped with a property
     * ({@code AndroidSupport.isRunningAndroid()} is {@code Class.forName(
     * "android.app.Activity")} captured at class-init). Native image is
     * {@link HiddenClassDefiner#isEnabled()} — do <em>not</em> also gate on
     * {@code AotDispatch.isAotLinkRequested()}, which is true on HotSpot when
     * {@code -Dgroovy.indy.aot.link=true} and would silently disable generation
     * in AOT-dispatch unit tests.
     *
     * @return {@code true} when generation may be attempted
     */
    public static boolean generationAllowed() {
        if (SystemUtil.getBooleanSafe(PROPERTY_DISABLE, false)) {
            return false;
        }
        if (!HiddenClassDefiner.isEnabled()) {
            return false;
        }
        return !AndroidSupport.isRunningAndroid();
    }

    /**
     * Non-nestmate {@code INVOKE*} gate (Step 1 / Step 4). Same public-type
     * test as {@code CallSiteGenerator.isCompilable} minus Android / name-char
     * bits (those live on {@link #generationAllowed()} / hidden names).
     *
     * @param method the candidate
     * @return {@code true} when an InvokerFactory nestmate can legally
     *         {@code INVOKE*} the member
     */
    static boolean isPubliclyInvocableFromInvokerFactory(final CachedMethod method) {
        final Class<?> declaring = method.getDeclaringClass().getTheClass();
        if (!Modifier.isPublic(declaring.getModifiers())) {
            return false;
        }
        if (!method.isPublic()) {
            return false;
        }
        for (Class<?> p : method.getNativeParameterTypes()) {
            if (!p.isPrimitive() && !Modifier.isPublic(p.getModifiers())) {
                return false;
            }
        }
        final Class<?> ret = method.getReturnType();
        return ret.isPrimitive() || Modifier.isPublic(ret.getModifiers());
    }

    /**
     * Loader visibility copied from {@code ProxyClassDefiner.loaderCanResolve}:
     * child loaders see parents; parents do not see children. Bootstrap types
     * ({@code getClassLoader() == null}) are visible to everyone.
     *
     * @param host the class whose loader is asked to resolve {@code type}
     * @param type the type named by the bytecode
     * @return {@code true} when {@code host}'s loader can resolve {@code type}
     */
    static boolean loaderCanResolve(final Class<?> host, final Class<?> type) {
        if (type == null || type.isPrimitive()) {
            return true;
        }
        final ClassLoader defining = type.getClassLoader();
        if (defining == null) {
            return true;
        }
        if (host == null) {
            return false;
        }
        for (ClassLoader cl = host.getClassLoader(); cl != null; cl = cl.getParent()) {
            if (cl == defining) {
                return true;
            }
        }
        return false;
    }

    /**
     * Step 3 encoding, package-visible so tests can exercise classData without
     * waiting for Steps 1–2 to fail.
     *
     * @param method the cached method
     * @return a classData trampoline, or {@code null}
     */
    static DirectInvoker tryCreateClassData(final CachedMethod method) {
        if (method == null || !generationAllowed()) {
            return null;
        }
        try {
            final Method m = method.getCachedMethod();
            final MethodHandle mh = LOOKUP.unreflect(m);
            final byte[] bytes = InvokerBytecode.emitClassData(m);
            final Lookup hidden = HiddenClassDefiner.tryDefineNestmateWithClassData(
                    LOOKUP, bytes, mh, true);
            return instantiate(hidden);
        } catch (Exception | LinkageError ignored) {
            return null;
        }
    }

    /**
     * Step 4 encoding, package-visible so tests can exercise a visible
     * {@link ClassLoaderForClassArtifacts} artifact without waiting for
     * Steps 1–3 to fail (unnamed modules usually succeed at Step 2).
     *
     * @param method the cached method
     * @return a visible-class trampoline, or {@code null}
     */
    static DirectInvoker tryCreateVisibleArtifact(final CachedMethod method) {
        if (method == null || !generationAllowed()) {
            return null;
        }
        try {
            return defineVisibleArtifact(method, method.getCachedMethod());
        } catch (Exception | LinkageError ignored) {
            return null;
        }
    }

    // -------------------------------------------------------------------------

    private static DirectInvoker defineSteps(final CachedMethod method) {
        final Method m = method.getCachedMethod();
        final Class<?> declaring = m.getDeclaringClass();
        final Class<?>[] params = m.getParameterTypes();
        final Class<?> returnType = m.getReturnType();

        DirectInvoker di = tryStep1(method, m, declaring, params, returnType);
        if (di != null) {
            return di;
        }
        di = tryStep2(m, declaring);
        if (di != null) {
            return di;
        }
        di = tryStep3(method, declaring, params, returnType);
        if (di != null) {
            return di;
        }
        return tryStep4(method, m, declaring);
    }

    private static DirectInvoker tryStep1(
            final CachedMethod method,
            final Method m,
            final Class<?> declaring,
            final Class<?>[] params,
            final Class<?> returnType) {
        if (isPubliclyInvocableFromInvokerFactory(method)
                && canResolveInvokeTypes(LOOKUP.lookupClass(), declaring, params, returnType)) {
            return defineInvokeStarOnLookup(m, LOOKUP);
        }
        return null;
    }

    private static DirectInvoker tryStep2(final Method m, final Class<?> declaring) {
        if (HiddenClassDefiner.canAttemptPrivateLookup(declaring)
                && loaderCanResolve(declaring, DirectInvoker.class)) {
            return defineInvokeStarOnDeclaringClass(m, declaring);
        }
        return null;
    }

    private static DirectInvoker tryStep3(
            final CachedMethod method,
            final Class<?> declaring,
            final Class<?>[] params,
            final Class<?> returnType) {
        if (canResolveInvokeTypes(LOOKUP.lookupClass(), declaring, params, returnType)) {
            return tryCreateClassData(method);
        }
        return null;
    }

    private static DirectInvoker tryStep4(
            final CachedMethod method, final Method m, final Class<?> declaring) {
        if (declaring.getClassLoader() != null
                && loaderCanResolve(declaring, DirectInvoker.class)
                && isPubliclyInvocableFromInvokerFactory(method)) {
            return defineVisibleArtifact(method, m);
        }
        return null;
    }

    private static boolean canResolveInvokeTypes(
            final Class<?> host,
            final Class<?> declaring,
            final Class<?>[] params,
            final Class<?> returnType) {
        if (!loaderCanResolve(host, declaring) || !loaderCanResolve(host, returnType)) {
            return false;
        }
        for (Class<?> p : params) {
            if (!loaderCanResolve(host, p)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Step 1: today's {@code tryDefineNestmate} returning {@code Class}, then
     * public no-arg {@code getConstructor().newInstance()}.
     */
    private static DirectInvoker defineInvokeStarOnLookup(final Method method, final Lookup hostLookup) {
        final byte[] bytes = InvokerBytecode.emitInvokeStar(method);
        final Class<?> cls = HiddenClassDefiner.tryDefineNestmate(hostLookup, bytes, true);
        return instantiate(cls);
    }

    /**
     * Step 2: nestmate of the declaring class, not of InvokerFactory.
     * {@code hostLookup = privateLookupIn(declaringClass, InvokerFactory.LOOKUP)}
     * — passing {@code InvokerFactory.LOOKUP} as the <em>define</em> lookup
     * would make the hidden class a nestmate of this factory and private
     * {@code INVOKE*} would {@code IllegalAccessError}.
     */
    private static DirectInvoker defineInvokeStarOnDeclaringClass(
            final Method method, final Class<?> declaring) {
        try {
            final Lookup hostLookup = MethodHandles.privateLookupIn(declaring, LOOKUP);
            final byte[] bytes = InvokerBytecode.emitInvokeStar(method);
            final Lookup hidden = HiddenClassDefiner.tryDefineNestmateLookup(hostLookup, bytes, true);
            return instantiate(hidden);
        } catch (IllegalAccessException | SecurityException e) {
            return null;
        }
    }

    private static DirectInvoker defineVisibleArtifact(final CachedMethod cached, final Method method) {
        try {
            final ClassLoaderForClassArtifacts loader =
                    cached.cachedClass.classInfo.getArtifactClassLoader();
            if (loader == null) {
                return null;
            }
            final String binaryName = loader.createClassName(method.getName());
            final byte[] bytes = InvokerBytecode.emitInvokeStar(method, binaryName.replace('.', '/'));
            final Constructor<?> ctor = loader.defineClassAndGetConstructor(binaryName, bytes);
            if (ctor == null) {
                return null;
            }
            return (DirectInvoker) ctor.newInstance();
        } catch (Exception | LinkageError ignored) {
            return null;
        }
    }

    private static DirectInvoker instantiate(final Class<?> cls) {
        if (cls == null) {
            return null;
        }
        try {
            return (DirectInvoker) cls.getConstructor().newInstance();
        } catch (Exception | LinkageError ignored) {
            return null;
        }
    }

    private static DirectInvoker instantiate(final Lookup hidden) {
        if (hidden == null) {
            return null;
        }
        try {
            final MethodHandle ctor = hidden.findConstructor(
                    hidden.lookupClass(), MethodType.methodType(void.class));
            return (DirectInvoker) ctor.invoke();
        } catch (Error e) {
            throw e;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
