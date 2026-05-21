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
import groovy.lang.GroovySystem;
import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.runtime.GeneratedClosure;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.invoke.SwitchPoint;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 * </ol>
 * <p>
 * Logging can be enabled using the system property {@code groovy.indy.logging=true}.
 */
public class IndyInterface {
    private static final long INDY_OPTIMIZE_THRESHOLD = SystemUtil.getLongSafe("groovy.indy.optimize.threshold", 1_000L);
    private static final long INDY_FALLBACK_THRESHOLD = SystemUtil.getLongSafe("groovy.indy.fallback.threshold", 1_000L);
    private static final long INDY_FALLBACK_CUTOFF = SystemUtil.getLongSafe("groovy.indy.fallback.cutoff", 100L);
    static final int INDY_PIC_SIZE = SystemUtil.getIntegerSafe("groovy.indy.pic.size", 4);

    /**
     * Flags for method and property calls.
     */
    public static final int SAFE_NAVIGATION=1, THIS_CALL=2, GROOVY_OBJECT=4, IMPLICIT_THIS=8, SPREAD_CALL=16, UNCACHED_CALL=32;

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
            MethodType mt = MethodType.methodType(MethodHandle.class, CacheableCallSite.class, Class.class, String.class, int.class, Boolean.class, Boolean.class, Boolean.class, Object.class, Object[].class);

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

        int callID = ct.getOrderNumber();
        boolean safe       = (flags & SAFE_NAVIGATION) != 0;
        boolean thisCall   = (flags & THIS_CALL      ) != 0;
        boolean spreadCall = (flags & SPREAD_CALL    ) != 0;

        // first produce a dummy call site, since indy doesn't give the runtime types;
        // the site then changes to the target when INDY_OPTIMIZE_THRESHOLD is reached
        // that does the method selection including the direct call to the real method
        var mc = new CacheableCallSite(type, caller);
        Class<?> sender = caller.lookupClass();
        if (thisCall) {
            while (GeneratedClosure.class.isAssignableFrom(sender)) {
                sender = sender.getEnclosingClass(); // GROOVY-2433
            }
        }
        // make an adapter for method selection, i.e. get cached method handle (fast path) or fall back
        MethodHandle mh = makeBootHandle(mc, sender, name, callID, type, safe, thisCall, spreadCall, FROM_CACHE_HANDLE_METHOD);
        mc.setTarget(mh);
        mc.setDefaultTarget(mh);
        mc.setFallbackTarget(makeFallBack(mc, sender, name, callID, type, safe, thisCall, spreadCall));

        return mc;
    }

    /**
     * Makes a fallback method for an invalidated method selection.
     */
    protected static MethodHandle makeFallBack(MutableCallSite mc, Class<?> sender, String name, int callID, MethodType type, boolean safeNavigation, boolean thisCall, boolean spreadCall) {
        return makeBootHandle(mc, sender, name, callID, type, safeNavigation, thisCall, spreadCall, SELECT_METHOD_HANDLE_METHOD);
    }

    private static MethodHandle makeBootHandle(MutableCallSite mc, Class<?> sender, String name, int callID, MethodType type, boolean safeNavigation, boolean thisCall, boolean spreadCall, MethodHandle fromCacheOrSelectMethod) {
        final Object dummyReceiver = 1;
        // Step 1: bind site-constant arguments
        MethodHandle boundHandle = MethodHandles.insertArguments(
            fromCacheOrSelectMethod,
            0, // insert start index
            mc,
            sender,
            name,
            callID,
            safeNavigation,
            thisCall,
            spreadCall,
            dummyReceiver
        );
        // boundHandle: (Object receiver, Object[] arguments) → MethodHandle

        // Step 2: fold into the shared invoker (MethodHandle, Object[]) → Object
        MethodHandle bootHandle = MethodHandles.foldArguments(
            CACHED_INVOKER, // (MethodHandle, Object[]) → Object
            boundHandle  // (Object, Object[]) → MethodHandle
        );
        // bootHandle: (Object receiver, Object[] arguments) → Object

        // Step 3: adapt to call site type: collect all arguments into Object[] and then asType
        bootHandle = bootHandle.asCollector(Object[].class, type.parameterCount()).asType(type);

        return bootHandle;
    }

    private static class FallbackSupplier {
        private final CacheableCallSite callSite;
        private final Class<?> sender;
        private final String methodName;
        private final int callID;
        private final Boolean safeNavigation;
        private final Boolean thisCall;
        private final Boolean spreadCall;
        private final Object dummyReceiver;
        private final Object[] arguments;
        private MethodHandleWrapper result;

        /**
         * Creates a supplier that computes fallback handles lazily.
         *
         * @param callSite the current call site
         * @param sender the sending class
         * @param methodName the method name
         * @param callID the call-type id
         * @param safeNavigation whether safe navigation is enabled
         * @param thisCall whether the invocation is a {@code this} call
         * @param spreadCall whether spread-call semantics are active
         * @param dummyReceiver the synthetic receiver placeholder
         * @param arguments the invocation arguments
         */
        FallbackSupplier(CacheableCallSite callSite, Class<?> sender, String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) {
            this.callSite = callSite;
            this.sender = sender;
            this.methodName = methodName;
            this.callID = callID;
            this.safeNavigation = safeNavigation;
            this.thisCall = thisCall;
            this.spreadCall = spreadCall;
            this.dummyReceiver = dummyReceiver;
            this.arguments = arguments;
        }

        /**
         * Returns the cached fallback result, computing it on first use.
         *
         * @return the fallback method-handle wrapper
         */
        MethodHandleWrapper get() {
            if (null == result) {
                result = fallback(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, dummyReceiver, arguments);
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
        MethodHandle mh = fromCacheHandle(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, dummyReceiver, arguments);
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
    private static MethodHandle fromCacheHandle(CacheableCallSite callSite, Class<?> sender, String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) throws Throwable {
        Object receiver = arguments[0];
        Object receiverKey = receiverCacheKey(receiver);

        MethodHandleWrapper mhw = callSite.get(receiverKey);
        if (mhw != null) {
            mhw.incrementLatestHitCount();
            if (mhw.isCanSetTarget() && (callSite.getTarget() != mhw.getTargetMethodHandle()) && mhw.getLatestHitCount() > INDY_OPTIMIZE_THRESHOLD && callSite.picInsertIfMissing(receiverKey)) {
                optimizeCallSite(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, arguments, receiverKey, mhw);
            }
            return mhw.getCachedMethodHandle();
        }

        FallbackSupplier fallbackSupplier = new FallbackSupplier(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, dummyReceiver, arguments);
        mhw = callSite.getAndPut(receiverKey, theKey -> {
            MethodHandleWrapper fallback = fallbackSupplier.get();
            if (fallback.isCanSetTarget()) return fallback;
            return NULL_METHOD_HANDLE_WRAPPER;
        }, sender);

        if (mhw == NULL_METHOD_HANDLE_WRAPPER) {
            // The PIC stores a sentinel to remember "do not relink this receiver shape";
            // execution still needs a real handle for the current invocation.
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
                optimizeCallSite(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, arguments, receiverKey, mhw);
            }
        }

        return mhw.getCachedMethodHandle();
    }

    private static void optimizeCallSite(CacheableCallSite callSite, Class<?> sender, String methodName, int callID, boolean safeNavigation, boolean thisCall, boolean spreadCall, Object[] arguments, Object receiverKey, MethodHandleWrapper mhw) {
        if (callSite.getFallbackRound().get() > INDY_FALLBACK_CUTOFF) {
            if (callSite.getTarget() != callSite.getDefaultTarget()) {
                callSite.setTarget(callSite.getDefaultTarget());
            }
        } else {
            callSite.maybeUpdatePic(receiverKey, picChain -> {
                Selector selector = Selector.getSelector(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, arguments);
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
        MethodHandle mh = selectMethodHandle(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, dummyReceiver, arguments);
        return mh.invokeExact(arguments);
    }

    /**
     * Core method for indy method selection using runtime types.
     */
    private static MethodHandle selectMethodHandle(CacheableCallSite callSite, Class<?> sender, String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) throws Throwable {
        MethodHandleWrapper mhw = fallback(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, dummyReceiver, arguments);

        MethodHandle defaultTarget = callSite.getDefaultTarget();
        long fallbackCount = callSite.incrementFallbackCount();
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

    private static MethodHandleWrapper fallback(CacheableCallSite callSite, Class<?> sender, String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) {
        Selector selector = Selector.getSelector(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, arguments);
        selector.setCallSiteTarget();

        return new MethodHandleWrapper(
                selector.handle.asSpreader(Object[].class, arguments.length).asType(MethodType.methodType(Object.class, Object[].class)),
                selector.handle,
                selector.method,
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
