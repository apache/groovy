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
package org.codehaus.groovy.vmplugin.v8;

import edu.umd.cs.findbugs.annotations.NonNull;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovySystem;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.runtime.GeneratedClosure;

import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.SAME_CLASS;

/**
 * Bytecode level interface for bootstrap methods used by invokedynamic.
 * <p>
 * This class provides the core logic for the {@code invokedynamic} (Indy) support in Groovy.
 * It handles the bootstrap process, method selection via {@link Selector}, and the
 * optimization lifecycle of {@link CacheableCallSite}.
 * <p>
 * <b>Optimization Lifecycle:</b>
 * <ol>
 *   <li><b>Bootstrap:</b> The JVM calls one of the bootstrap methods (e.g., {@code bootstrap}) when an {@code invokedynamic} instruction is first encountered.</li>
 *   <li><b>Initial Linkage:</b> The call site is initialized with a fallback target (adapter pointing to {@link #fromCacheHandle}).</li>
 *   <li><b>Execution & Selection:</b> On first execution, {@code fromCacheHandle} uses a {@link Selector} to find the target method and create a guarded {@link java.lang.invoke.MethodHandle}.</li>
 *   <li><b>Promotion & PIC:</b> After reaching {@link #INDY_OPTIMIZE_THRESHOLD} hits for a stable shape, {@link #optimizeCallSite} promotes the handle into a
 *       Polymorphic Inline Cache (PIC) chain directly in the call site target for maximum JIT optimization.</li>
 *   <li><b>Degradation:</b> After excessive SwitchPoint invalidations, the call site enters <b>degraded mode</b>
 *       where class-guarded MOP dispatch handles are used, avoiding global invalidation costs.</li>
 * </ol>
 * <p>
 * Logging can be enabled using the system property {@code groovy.indy.logging=true}.
 */
public class IndyInterface {
    private static final long INDY_OPTIMIZE_THRESHOLD = SystemUtil.getLongSafe("groovy.indy.optimize.threshold", 1_000L);
    private static final long INDY_FALLBACK_THRESHOLD = SystemUtil.getLongSafe("groovy.indy.fallback.threshold", 1_000L);
    private static final long INDY_FALLBACK_CUTOFF = SystemUtil.getLongSafe("groovy.indy.fallback.cutoff", 100L);
    private static final long INDY_DEGRADE_THRESHOLD = SystemUtil.getLongSafe("groovy.indy.degrade.threshold", 10L);
    static final int INDY_PIC_SIZE = SystemUtil.getIntegerSafe("groovy.indy.pic.size", 4);

    /**
     * Flags for method and property calls.
     */
    public static final int SAFE_NAVIGATION=1, THIS_CALL=2, GROOVY_OBJECT=4, IMPLICIT_THIS=8, SPREAD_CALL=16;

    private static final MethodHandleWrapper NULL_METHOD_HANDLE_WRAPPER = MethodHandleWrapper.getNullMethodHandleWrapper();

    /**
     * Enum for easy differentiation between call types.
     */
    public enum CallType {
        /**
         * Method invocation type.
         */
        METHOD("invoke"),
        /**
         * Constructor invocation type.
         */
        INIT("init"),
        /**
         * Get property invocation type.
         */
        GET("getProperty"),
        /**
         * Set property invocation type.
         */
        SET("setProperty"),
        /**
         * Cast invocation type.
         */
        CAST("cast"),
        /**
         * Interface method invocation type.
         */
        INTERFACE("interface");

        private static final Map<String, CallType> NAME_CALLTYPE_MAP = Stream.of(CallType.values())
            .collect(Collectors.toUnmodifiableMap(CallType::getCallSiteName, Function.identity()));

        /**
         * The call site type name.
         */
        private final String name;

        /**
         * Creates a call type for the given bootstrap name.
         *
         * @param callSiteName the bootstrap call-site name
         */
        CallType(String callSiteName) {
            this.name = callSiteName;
        }

        /**
         * Returns the name of the call site type
         */
        public String getCallSiteName() {
            return name;
        }

        /**
         * Resolves a call type by its bootstrap call-site name.
         *
         * @param callSiteName the bootstrap call-site name
         * @return the matching call type, or {@code null} if none matches
         */
        public static CallType fromCallSiteName(String callSiteName) {
            return NAME_CALLTYPE_MAP.get(callSiteName);
        }

        /**
         * Returns the ordinal used as the call-site dispatch id.
         *
         * @return the call-type order number
         */
        public int getOrderNumber() {
            return ordinal();
        }
    }

    /**
     * Logger.
     */
    protected static final Logger LOG;
    /**
     * Indicates if indy logging is enabled.
     */
    protected static final boolean LOG_ENABLED;

    static {
        boolean enableLogger = false;

        LOG = Logger.getLogger(IndyInterface.class.getName());

        try {
            if (Boolean.getBoolean("groovy.indy.logging")) {
                LOG.setLevel(Level.ALL);
                enableLogger = true;
            }
        } catch (SecurityException e) {
            // Allow security managers to prevent system property access (legacy comment)
        }

        LOG_ENABLED = enableLogger;
    }

    /**
     * LOOKUP constant used for example in unreflect calls
     */
    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * shared invoker for cached method handles
     */
    private static final MethodHandle CACHED_INVOKER = MethodHandles.exactInvoker(MethodType.methodType(Object.class, Object[].class));

    /**
     * handle for the fromCacheHandle method
     */
    private static final MethodHandle FROM_CACHE_HANDLE_METHOD;

    /**
     * handle for the selectMethodHandle method
     */
    private static final MethodHandle SELECT_METHOD_HANDLE_METHOD;

    static {
        try {
            MethodType mt = MethodType.methodType(MethodHandle.class, CacheableCallSite.class, Class.class, String.class, Object[].class);

            FROM_CACHE_HANDLE_METHOD = LOOKUP.findStatic(IndyInterface.class, "fromCacheHandle", mt);
            SELECT_METHOD_HANDLE_METHOD = LOOKUP.findStatic(IndyInterface.class, "selectMethodHandle", mt);
        } catch (Exception e) {
            throw new GroovyBugError(e);
        }
    }

    /**
     * Shared switch point invalidated when metaclass state changes.
     * <p>
     * <b>Concurrency:</b> {@code volatile} ensures that global invalidations are immediately
     * visible to all threads across the JVM, causing them to fall back from JIT-optimized handles.
     */
    @SuppressWarnings("java:S3077")
    protected static volatile SwitchPoint switchPoint = new SwitchPoint();

    static {
        GroovySystem.getMetaClassRegistry().addMetaClassRegistryChangeEventListener(cmcu -> invalidateSwitchPoints());
    }

    /**
     * Callback for constant metaclass update change
     * <p>
     * <b>Concurrency:</b> Synchronizes on {@code IndyInterface.class} to atomically replace
     * the global switch point and invalidate the old one, preventing race conditions during
     * simultaneous MetaClass changes.
     */
    protected static void invalidateSwitchPoints() {
        if (LOG_ENABLED) {
            LOG.info("invalidating switch point");
        }

        synchronized (IndyInterface.class) {
            SwitchPoint old = switchPoint;
            switchPoint = new SwitchPoint();
            SwitchPoint.invalidateAll(new SwitchPoint[]{old});
        }
    }

    /**
     * Bootstrap method for method calls from Groovy-compiled code with indy.
     *
     * @param caller   - the caller
     * @param callType - the type of call
     * @param type     - the parameter(s) and return type specification
     * @param name     - the real method name
     * @param flags    - call flags <ul>
     *                   <li>{@value #SAFE_NAVIGATION} is the flag value for safe navigation; see {@link #SAFE_NAVIGATION}</li>
     *                   <li>{@value #THIS_CALL} is the flag value for a call on this; see {@link #THIS_CALL}</li>
     *                   <li>{@value #SPREAD_CALL} is the flag value for a spread call; see {@link #SPREAD_CALL}</li>
     *                   </ul>
     * @since 2.1.0
     */
    public static CallSite bootstrap(final MethodHandles.Lookup caller, final String callType, final MethodType type, final String name, final int flags) {
        CallType ct = CallType.fromCallSiteName(callType);
        if (null == ct) throw new GroovyBugError("Unknown call type: " + callType);

        boolean safe       = (flags & SAFE_NAVIGATION) != 0;
        boolean thisCall   = (flags & THIS_CALL      ) != 0;
        boolean spreadCall = (flags & SPREAD_CALL    ) != 0;

        // first produce a dummy call site, since indy doesn't give the runtime types;
        // the site then changes to the target when INDY_OPTIMIZE_THRESHOLD is reached
        // that does the method selection including the direct call to the real method
        var mc = new CacheableCallSite(type, caller, ct, safe, thisCall, spreadCall);
        Class<?> sender = caller.lookupClass();
        if (thisCall) {
            while (GeneratedClosure.class.isAssignableFrom(sender)) {
                sender = sender.getEnclosingClass(); // GROOVY-2433
            }
        }
        // make an adapter for method selection, i.e. get cached method handle (fast path) or fall back
        MethodHandle mh = makeBootHandle(mc, sender, name, FROM_CACHE_HANDLE_METHOD);
        mc.setTarget(mh);
        mc.setDefaultTarget(mh);
        mc.setFallbackTarget(makeFallBack(mc, sender, name));

        return mc;
    }

    /**
     * Makes a fallback method for an invalidated method selection.
     */
    protected static MethodHandle makeFallBack(CacheableCallSite mc, Class<?> sender, String name) {
        return makeBootHandle(mc, sender, name, SELECT_METHOD_HANDLE_METHOD);
    }

    private static MethodHandle makeBootHandle(CacheableCallSite mc, Class<?> sender, String name, MethodHandle fromCacheOrSelectMethod) {
        // Step 1: bind site-constant arguments
        MethodHandle boundHandle = MethodHandles.insertArguments(
            fromCacheOrSelectMethod,
            0, // insert start index
            mc,
            sender,
            name
        );
        // boundHandle: (Object receiver, Object[] arguments) → MethodHandle

        // Step 2: fold into the shared invoker (MethodHandle, Object[]) → Object
        MethodHandle bootHandle = MethodHandles.foldArguments(
            CACHED_INVOKER, // (MethodHandle, Object[]) → Object
            boundHandle  // (Object, Object[]) → MethodHandle
        );
        // bootHandle: (Object receiver, Object[] arguments) → Object

        // Step 3: adapt to call site type: collect all arguments into Object[] and then asType
        bootHandle = bootHandle.asCollector(Object[].class, mc.type().parameterCount()).asType(mc.type());

        return bootHandle;
    }

    //--------------------------------------------------------------------------
    // Degraded mode — class-guarded handle via direct helper method
    //--------------------------------------------------------------------------

    /**
     * Degraded-mode dispatch entry point.
     * Called by the degraded method handle at invocation time. Dynamically looks up
     * the metaclass for the receiver and invokes the method through it.
     * This is always correct under metaclass churn because it never caches a stale
     * method handle.
     *
     * @param receiver the receiver object
     * @param name     the method name
     * @param args     the invocation arguments
     * @return the method invocation result
     */
    public static Object invokeDegraded(Object receiver, String name, Object[] args) {
        try {
            return org.codehaus.groovy.runtime.InvokerHelper.getMetaClass(receiver)
                    .invokeMethod(receiver, name, args);
        } catch (GroovyRuntimeException e) {
            return e; // same as MethodHandles.catchException + UNWRAP_EXCEPTION in normal path
        }
    }

    /**
     * Degraded-mode property-get dispatch entry point.
     * Called by the degraded handle for property get operations.
     *
     * @param receiver the receiver object
     * @param name     the property name
     * @return the property value
     */
    public static Object getPropertyDegraded(Object receiver, String name) {
        try {
            return org.codehaus.groovy.runtime.InvokerHelper.getMetaClass(receiver)
                    .getProperty(receiver, name);
        } catch (GroovyRuntimeException e) {
            return e; // same as MethodHandles.catchException + UNWRAP_EXCEPTION in normal path
        }
    }

    /**
     * The method handle for {@link #invokeDegraded}.
     */
    private static final MethodHandle DEGRADED_INVOKE_HANDLE;

    /**
     * The method handle for {@link #getPropertyDegraded}.
     */
    private static final MethodHandle DEGRADED_GET_PROPERTY_HANDLE;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            DEGRADED_INVOKE_HANDLE = lookup.findStatic(IndyInterface.class, "invokeDegraded",
                    MethodType.methodType(Object.class, Object.class, String.class, Object[].class));
            DEGRADED_GET_PROPERTY_HANDLE = lookup.findStatic(IndyInterface.class, "getPropertyDegraded",
                    MethodType.methodType(Object.class, Object.class, String.class));
        } catch (Exception e) {
            throw new GroovyBugError(e);
        }
    }

    /**
     * Builds a degraded-mode method handle for the given call site and receiver class.
     * <p>
     * The handle guards on receiver class only (no SwitchPoint), and dispatches
     * through a static helper method that dynamically looks up the metaclass at
     * invocation time. This avoids the global invalidation cost at the expense of
     * going through the metaclass's invokeMethod on each invocation.
     *
     * @param callSite      the call site
     * @param name          the method name
     * @param receiverClass the expected receiver class
     * @return a class-guarded, SwitchPoint-free method handle, or {@code null} if
     *         degraded mode is not applicable for this call type
     */
    private static MethodHandle buildDegradedHandle(CacheableCallSite callSite, String name, Class<?> receiverClass) {
        MethodType callSiteType = callSite.type();
        int paramCount = callSiteType.parameterCount();

        // Build the base dispatch handle through the static helper method.
        // DEGRADED_INVOKE_HANDLE: (Object receiver, String name, Object[] args) -> Object
        // DEGRADED_GET_PROPERTY_HANDLE: (Object receiver, String name) -> Object

        MethodHandle invokeHandle;
        if (callSite.callType == CallType.GET) {
            // Property get: getPropertyDegraded(receiver, name)
            invokeHandle = MethodHandles.insertArguments(DEGRADED_GET_PROPERTY_HANDLE, 1, name);
            // invokeHandle: (Object receiver) -> Object
        } else if (callSite.callType == CallType.METHOD) {
            // Method invoke: invokeDegraded(receiver, name, args)
            invokeHandle = MethodHandles.insertArguments(DEGRADED_INVOKE_HANDLE, 1, name);
            // invokeHandle: (Object receiver, Object[] args) -> Object
        } else {
            return null;
        }

        // Adapt from (Object receiver, Object[] args) -> Object to (Object receiver, arg1, arg2, ...) -> Object
        // using asCollector for METHOD, no adaptation needed for GET
        if (callSite.callType == CallType.METHOD) {
            int argCount = paramCount - 1;
            invokeHandle = invokeHandle.asCollector(Object[].class, argCount);
        }

        // Now invokeHandle has signature (Object receiver, arg1, ...) -> Object
        // Adapt to the call site type (e.g., (DomainObject, String) -> Object) using explicitCastArguments
        invokeHandle = MethodHandles.explicitCastArguments(invokeHandle, callSiteType);

        // Add class guard: if receiver.getClass() != receiverClass, fall through to re-selection
        MethodHandle classGuard = SAME_CLASS.bindTo(receiverClass);
        classGuard = classGuard.asType(MethodType.methodType(boolean.class, Object.class));

        // Drop the remaining arguments for the guard to match call site arity
        if (paramCount > 1) {
            Class<?>[] dropTypes = callSiteType.dropParameterTypes(1, paramCount).parameterArray();
            classGuard = MethodHandles.dropArguments(classGuard, 1, dropTypes);
        }

        // Adapt the guard to match the call site type (e.g. (Class)boolean for static calls).
        // asType handles widening from receiverType → Object.
        classGuard = classGuard.asType(callSiteType.changeReturnType(boolean.class));

        // Build the guarded handle: if class matches -> invoke through helper; else -> re-select
        MethodHandle fallback = callSite.getFallbackTarget();
        MethodHandle target = MethodHandles.guardWithTest(classGuard, invokeHandle, fallback);

        if (LOG_ENABLED) LOG.info("built degraded handle for " + receiverClass.getName() + "." + name);

        return target;
    }

    //--------------------------------------------------------------------------
    // Normal fast path and fallback
    //--------------------------------------------------------------------------

    private static class FallbackSupplier {
        private final CacheableCallSite callSite;
        private final Class<?> sender;
        private final String methodName;
        private final Object[] arguments;
        private MethodHandleWrapper result;

        /**
         * Creates a supplier that computes fallback handles lazily.
         *
         * @param callSite the current call site
         * @param sender the sending class
         * @param methodName the method name
         * @param arguments the invocation arguments
         */
        FallbackSupplier(CacheableCallSite callSite, Class<?> sender, String methodName, Object[] arguments) {
            this.callSite = callSite;
            this.sender = sender;
            this.methodName = methodName;
            this.arguments = arguments;
        }

        /**
         * Returns the cached fallback result, computing it on first use.
         *
         * @return the fallback method-handle wrapper
         */
        MethodHandleWrapper get() {
            if (null == result) {
                result = fallback(callSite, sender, methodName, arguments);
            }

            return result;
        }
    }

    /**
     * Get the cached methodHandle. if the related methodHandle is not found in the inline cache, cache and return it.
     * @deprecated Use the new bootHandle-based approach instead.
     */
    @Deprecated
    public static Object fromCache(CacheableCallSite callSite, Class<?> sender, String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) throws Throwable {
        MethodHandle mh = fromCacheHandle(callSite, sender, methodName, arguments);
        return mh.invokeExact(arguments);
    }

    private static final Object NULL_KEY = new Object();
    private static final ClassValue<Object> STATIC_KEYS = new ClassValue<>() {
        @Override
        protected Object computeValue(@NonNull Class<?> type) {
            return new Object();
        }
    };

    /**
     * Get the cached methodHandle. if the related methodHandle is not found in the inline cache, cache and return it.
     */
    private static MethodHandle fromCacheHandle(CacheableCallSite callSite, Class<?> sender, String methodName, Object[] arguments) throws Throwable {
        if (callSite.degraded) {
            // In degraded mode, build the class-guarded MOP handle directly
            // without running the full selector. The handle is stateless and
            // always reflects the current metaclass at invocation time.
            MethodHandle degradedHandle = buildDegradedHandle(callSite, methodName, arguments[0].getClass());
            if (degradedHandle != null) {
                // Wrap for the invoker: (Object, Object[]) -> Object
                return degradedHandle.asSpreader(Object[].class, arguments.length)
                        .asType(MethodType.methodType(Object.class, Object[].class));
            }
            // Fall through to full selection for unsupported call types (INIT, CAST, etc.)
        }

        Object receiver = arguments[0];
        Object receiverKey = receiverCacheKey(receiver);

        MethodHandleWrapper mhw = callSite.get(receiverKey);
        if (mhw != null && (mhw == NULL_METHOD_HANDLE_WRAPPER || mhw.getSwitchPoint() == switchPoint)) {
            mhw.incrementLatestHitCount();
            if (mhw.isCanSetTarget() && (callSite.getTarget() != mhw.getTargetMethodHandle()) && mhw.getLatestHitCount() > INDY_OPTIMIZE_THRESHOLD && callSite.picInsertIfMissing(receiverKey)) {
                optimizeCallSite(callSite, sender, methodName, arguments, receiverKey, mhw);
            }
            return mhw.getCachedMethodHandle();
        }

        FallbackSupplier fallbackSupplier = new FallbackSupplier(callSite, sender, methodName, arguments);
        mhw = callSite.getAndPut(receiverKey, theKey -> {
            MethodHandleWrapper fallback = fallbackSupplier.get();
            if (fallback.isCanSetTarget()) return fallback;
            return NULL_METHOD_HANDLE_WRAPPER;
        }, sender);

        if (mhw == NULL_METHOD_HANDLE_WRAPPER || mhw.getSwitchPoint() != switchPoint) {
            // The PIC stores a sentinel to remember "do not relink this receiver shape";
            // execution still needs a real handle for the current invocation.
            // OR the cached handle is stale.
            mhw = fallbackSupplier.get();
        }

        if (mhw.isCanSetTarget() && (callSite.getTarget() != mhw.getTargetMethodHandle())) {
            // GROOVY-11935: Set invokedynamic call site target immediately to enable earlier JIT inlining.
            if (callSite.type().parameterType(0) == Class.class) {
                var method = mhw.getMethod();
                if (method != null && Modifier.isStatic(method.getModifiers())) {
                    callSite.setTarget(mhw.getTargetMethodHandle());
                }
            }

            if (mhw.getLatestHitCount() > INDY_OPTIMIZE_THRESHOLD && callSite.picInsertIfMissing(receiverKey)) {
                optimizeCallSite(callSite, sender, methodName, arguments, receiverKey, mhw);
            }
        }

        return mhw.getCachedMethodHandle();
    }

    private static void optimizeCallSite(CacheableCallSite callSite, Class<?> sender, String methodName, Object[] arguments, Object receiverKey, MethodHandleWrapper mhw) {
        // NOTE: degrade call sites never reach this method because fromCacheHandle
        // returns early at line ~466. The handles for degraded sites also have
        // canSetTarget=false, so the callers check for isCanSetTarget() and skip us.

        if (callSite.getFallbackRound().get() > INDY_FALLBACK_CUTOFF) {
            if (callSite.getTarget() != callSite.getDefaultTarget()) {
                callSite.setTarget(callSite.getDefaultTarget());
            }
        } else {
            callSite.maybeUpdatePic(receiverKey, picChain -> {
                Selector selector = Selector.getSelector(callSite, sender, methodName, arguments);
                selector.skipSwitchPoint = true;
                selector.fallback = picChain;
                selector.setCallSiteTarget();
                // wrap with top-level SwitchPoint guard
                MethodHandle target = switchPoint.guardWithTest(selector.handle, callSite.getDefaultTarget());
                callSite.setTarget(target);
                if (LOG_ENABLED) LOG.info("call site target updated with PIC link, pic size: " + callSite.getPicCount());
                return selector.handle;
            });
        }
        mhw.resetLatestHitCount();
    }

    /**
     * Core method for indy method selection using runtime types.
     * @deprecated Use the new bootHandle-based approach instead.
     */
    @Deprecated
    public static Object selectMethod(CacheableCallSite callSite, Class<?> sender, String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) throws Throwable {
        MethodHandle mh = selectMethodHandle(callSite, sender, methodName, arguments);
        return mh.invokeExact(arguments);
    }

    /**
     * Core method for indy method selection using runtime types.
     */
    private static MethodHandle selectMethodHandle(CacheableCallSite callSite, Class<?> sender, String methodName, Object[] arguments) throws Throwable {
        // Check for degradation: if fallback rounds exceed the threshold, enter degraded mode
        if (!callSite.degraded && callSite.getFallbackRound().get() >= INDY_DEGRADE_THRESHOLD) {
            callSite.degraded = true;
            if (LOG_ENABLED) LOG.info("call site entered degraded mode due to excessive metaclass churn");
        }

        if (callSite.degraded) {
            // In degraded mode, produce a SwitchPoint-free, class-guarded MOP handle.
            // This handle dynamically looks up the metaclass at runtime via
            // InvokerHelper.getMetaClass(), then dispatches through mc.invokeMethod.
            // No selector cost on subsequent calls (they go through fromCacheHandle).
            MethodHandle degradedHandle = buildDegradedHandle(callSite, methodName, arguments[0].getClass());
            if (degradedHandle != null) {
                callSite.setTarget(degradedHandle);
                MethodHandle cachedHandle = degradedHandle.asSpreader(Object[].class, arguments.length)
                    .asType(MethodType.methodType(Object.class, Object[].class));
                return cachedHandle;
            }
            // Fall through to normal path if degraded handle is not applicable
        }

        long fallbackCount = callSite.incrementFallbackCount();
        MethodHandleWrapper mhw = fallback(callSite, sender, methodName, arguments);
        MethodHandle defaultTarget = callSite.getDefaultTarget();

        if (callSite.tryResetToDefaultTarget(defaultTarget, INDY_FALLBACK_THRESHOLD, fallbackCount)) {
            if (LOG_ENABLED) LOG.info("call site target reset to default, preparing outside invocation");
        }

        if (callSite.getTarget() == defaultTarget) {
            // correct the stale methodHandle in the inline cache of callsite
            // it is important but impacts the performance somehow when cache misses frequently
            Object receiver = arguments[0];

            // Avoid PIC pollution: don't write back uncached wrappers, e.g. for instance-level metaClass dispatches.
            callSite.put(receiverCacheKey(receiver), mhw.isCanSetTarget() ? mhw : NULL_METHOD_HANDLE_WRAPPER);
        }

        return mhw.getCachedMethodHandle();
    }

    /**
     * Computes the PIC cache key for the given receiver.
     */
    static Object receiverCacheKey(Object receiver) {
        if (receiver == null) return NULL_KEY;
        if (receiver instanceof Class<?> c) return STATIC_KEYS.get(c);
        return receiver.getClass();
    }

    private static MethodHandleWrapper fallback(CacheableCallSite callSite, Class<?> sender, String methodName, Object[] arguments) {
        Selector selector = Selector.getSelector(callSite, sender, methodName, arguments);
        selector.setCallSiteTarget();

        return new MethodHandleWrapper(
                selector.handle.asSpreader(Object[].class, arguments.length).asType(MethodType.methodType(Object.class, Object[].class)),
                selector.handle,
                selector.method,
                switchPoint,
                selector.cache
        );
    }

    /**
     * @since 2.5.0
     */
    public static CallSite staticArrayAccess(MethodHandles.Lookup lookup, String name, MethodType type) {
        if (type.parameterCount() == 2) {
            return new ConstantCallSite(IndyArrayAccess.arrayGet(type));
        } else {
            return new ConstantCallSite(IndyArrayAccess.arraySet(type));
        }
    }
}
