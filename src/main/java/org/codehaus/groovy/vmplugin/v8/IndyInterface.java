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

import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovySystem;
import org.apache.groovy.runtime.indy.IndyInvalidation;
import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.runtime.GeneratedDispatcher;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Bytecode level interface for bootstrap methods used by invokedynamic.
 * This class provides a logging ability by using the boolean system property
 * groovy.indy.logging. Other than that this class contains the
 * interfacing methods with bytecode for invokedynamic as well as some helper
 * methods and classes.
 */
public class IndyInterface {
    private static final long INDY_OPTIMIZE_THRESHOLD = SystemUtil.getLongSafe("groovy.indy.optimize.threshold", 1_000L);
    private static final long INDY_FALLBACK_THRESHOLD = SystemUtil.getLongSafe("groovy.indy.fallback.threshold", 1_000L);
    private static final long INDY_FALLBACK_CUTOFF = SystemUtil.getLongSafe("groovy.indy.fallback.cutoff", 100L);
    /**
     * Dispatch plain method calls reflectively while a call site is cold,
     * deferring all MethodHandle chain construction (and its one-time
     * LambdaForm cost) to hit-count promotion. See
     * {@link ColdReflectiveMethodHandleWrapper}. On by default; set
     * {@code -Dgroovy.indy.cold.reflection=false} to disable (opt-out).
     */
    private static final boolean INDY_COLD_REFLECTION = SystemUtil.getBooleanSafe("groovy.indy.cold.reflection", true);

    /**
     * Flags for method and property calls.
     */
    public static final int SAFE_NAVIGATION=1, THIS_CALL=2, GROOVY_OBJECT=4, IMPLICIT_THIS=8, SPREAD_CALL=16, UNCACHED_CALL=32;

    private static final MethodHandleWrapper NULL_METHOD_HANDLE_WRAPPER = MethodHandleWrapper.getNullMethodHandleWrapper();
    private static final String NULL_OBJECT_CLASS_NAME = "org.codehaus.groovy.runtime.NullObject";

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
        INTERFACE("interface"),
        /**
         * Compound-assignment invocation type (GEP-15), e.g. {@code +=}. The
         * bootstrap {@code name} carries {@code assignName} and {@code baseName}
         * packed as {@code assignName + NAME_SEPARATOR + baseName} (NUL-separated); resolution is delegated
         * to {@link IndyCompoundAssign}.
         */
        COMPOUND_ASSIGN("compoundAssign");

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

    /**
     * Shared invoker for the reflective cold tier; bound per wrapper to the
     * uniform {@code (Object[])Object} shape (see {@link #invokeColdReflective}).
     */
    static final MethodHandle COLD_REFLECTIVE_INVOKER;

    static {
        try {
            MethodType mt = MethodType.methodType(MethodHandle.class, CacheableCallSite.class, Class.class, String.class, int.class, Boolean.class, Boolean.class, Boolean.class, Object.class, Object[].class);

            FROM_CACHE_HANDLE_METHOD = LOOKUP.findStatic(IndyInterface.class, "fromCacheHandle", mt);
            SELECT_METHOD_HANDLE_METHOD = LOOKUP.findStatic(IndyInterface.class, "selectMethodHandle", mt);
            COLD_REFLECTIVE_INVOKER = LOOKUP.findStatic(IndyInterface.class, "invokeColdReflective",
                    MethodType.methodType(Object.class, ColdReflectiveMethodHandleWrapper.class, Object[].class));
        } catch (Exception e) {
            throw new GroovyBugError(e);
        }
    }

    static {
        // MetaClass registry changes invalidate the affected class + subtypes (GROOVY-12191).
        // Hierarchy fan-out is owned here (not duplicated in setStrongMetaClass beyond a
        // local retire of the exact class for paths that never fire the registry).
        //
        // Pre-6.0 this listener rotated a process-wide SwitchPoint field on this class.
        // That field is removed (vmplugin is internal-by-intent): call sites now guard on
        // per-ClassInfo domains via applyMopSwitchPoints.
        GroovySystem.getMetaClassRegistry().addMetaClassRegistryChangeEventListener(cmcu -> {
            Class<?> type = cmcu.getClassToUpdate();
            if (type != null) {
                IndyInvalidation.invalidateClass(type);
                if (LOG_ENABLED) {
                    LOG.info("invalidating class SwitchPoint hierarchy for " + type.getName());
                }
            } else {
                IndyInvalidation.invalidateUnscoped();
                if (LOG_ENABLED) {
                    LOG.info("unscoped SwitchPoint invalidation (unattributed MetaClass change)");
                }
            }
        });
    }

    /**
     * Category enter/leave and {@code VMPlugin.invalidateCallSites()}.
     * Bulk-invalidates every loaded class SwitchPoint so sites re-link under the
     * new category state. Per-class MetaClass changes use
     * {@link IndyInvalidation#invalidateClass(Class)} (or
     * {@link org.codehaus.groovy.reflection.ClassInfo#incVersion()}) instead.
     * <p>
     * Pre-6.0 this method also rotated a process-wide {@code switchPoint} field.
     * That field is gone; bulk policy lives entirely in {@link IndyInvalidation}.
     */
    protected static void invalidateSwitchPoints() {
        if (LOG_ENABLED) {
            LOG.info("invalidating class SwitchPoints for category / invalidateCallSites");
        }
        IndyInvalidation.invalidateCategory();
    }

    /**
     * Link-time install of the per-class MOP SwitchPoint guard.
     * Production call-site wiring ({@link Selector}, {@link IndyCompoundAssign})
     * enters here so the bytecode bootstrap surface owns how handles are guarded.
     * <p>
     * Domain resolution (receiver → class → {@code ClassInfo} SwitchPoint) and
     * invalidation policy stay in {@link IndyInvalidation}; this method only
     * binds the resolved SwitchPoint with {@code guardWithTest}. External or
     * test code that needs the same guard without package access should use
     * {@link IndyInvalidation#guardWithMopSwitchPoints}.
     *
     * @param handle   fast-path handle
     * @param fallback re-link handle
     * @param receiver call receiver (may be {@code null})
     * @return guarded handle
     */
    static MethodHandle applyMopSwitchPoints(final MethodHandle handle, final MethodHandle fallback, final Object receiver) {
        return IndyInvalidation.classSwitchPointFor(receiver).guardWithTest(handle, fallback);
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

    /**
     * Get the cached methodHandle. if the related methodHandle is not found in the inline cache, cache and return it.
     */
    private static MethodHandle fromCacheHandle(CacheableCallSite callSite, Class<?> sender, String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) throws Throwable {
        FallbackSupplier fallbackSupplier = new FallbackSupplier(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, dummyReceiver, arguments);
        Object receiver = arguments[0];
        String receiverClassName = receiverCacheKey(receiver);
        MethodHandleWrapper mhw = callSite.getAndPut(receiverClassName, (theName) -> {
            MethodHandleWrapper fallback = fallbackSupplier.get();
            if (fallback.isCanSetTarget()) return fallback;
            return NULL_METHOD_HANDLE_WRAPPER;
        });

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

            if (mhw.getLatestHitCount() > INDY_OPTIMIZE_THRESHOLD) {
                if (callSite.getFallbackRound().get() > INDY_FALLBACK_CUTOFF) {
                    if (callSite.getTarget() != callSite.getDefaultTarget()) {
                        // reset the call site target to default forever to avoid JIT deoptimization storm further
                        callSite.setTarget(callSite.getDefaultTarget());
                    }
                } else if (mhw instanceof ColdReflectiveMethodHandleWrapper) {
                    // the reflective cold tier carries no relink target; build the
                    // full guarded chain now, promote to it, and replace the PIC entry
                    MethodHandleWrapper full = fallback(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, dummyReceiver, arguments, false);
                    if (full.isCanSetTarget()) {
                        callSite.put(receiverClassName, full);
                        callSite.setTarget(full.getTargetMethodHandle());
                        if (LOG_ENABLED) LOG.info("cold reflective wrapper promoted, call site target set");
                        mhw.resetLatestHitCount();
                        return full.getCachedMethodHandle();
                    }
                } else {
                    if (callSite.getTarget() != mhw.getTargetMethodHandle()) {
                        callSite.setTarget(mhw.getTargetMethodHandle());
                        if (LOG_ENABLED) LOG.info("call site target set, preparing outside invocation");
                    }
                }

                mhw.resetLatestHitCount();
            }
        }

        return mhw.getCachedMethodHandle();
    }

    /**
     * Cold-tier dispatch for the {@code groovy.indy.cold.reflection} spike.
     * Re-validates the cached selection with plain-Java checks and invokes the
     * meta method reflectively (classic call-site semantics: argument coercion
     * via {@code doMethodInvoke}, {@code GroovyRuntimeException} unwrapping via
     * {@code ScriptBytecodeAdapter}). The handle bound to the wrapper has the
     * same {@code (Object[])Object} shape for every call site, arity, and
     * primitive pattern, so cold dispatch spins no per-shape LambdaForms.
     * <p>
     * On a failed validity check (or after the reflective-hit promotion
     * threshold), re-selection uses {@code fallback(..., allowColdReflection=false)}
     * and may write the full wrapper — or {@link #NULL_METHOD_HANDLE_WRAPPER} —
     * into the callsite PIC. That prevents an always-invalid class-domain
     * SwitchPoint from recursing through cold {@code tryBuild → invokeColdReflective}
     * (GROOVY-12191). This is a deliberate cold-tier promotion change beyond
     * pure SwitchPoint scoping: validity failure no longer re-enters the cold
     * reflective path on the same miss.
     */
    private static Object invokeColdReflective(ColdReflectiveMethodHandleWrapper cold, Object[] arguments) throws Throwable {
        if (cold.isValidFor(arguments)) {
            if (cold.incrementReflectiveHits() > INDY_OPTIMIZE_THRESHOLD) {
                // no longer cold: build the full guarded chain and replace the
                // PIC entry, so even sites the consecutive-hit promotion never
                // catches (e.g. polymorphic receivers) leave the reflective
                // tier after at most the threshold's worth of calls each
                MethodHandleWrapper full = fallback(cold.callSite, cold.sender, cold.methodName, cold.callID,
                        cold.safeNavigation, cold.thisCall, cold.spreadCall, 1, arguments, false);
                if (full.isCanSetTarget()) {
                    cold.callSite.put(receiverCacheKey(arguments[0]), full);
                    if (LOG_ENABLED) LOG.info("cold reflective wrapper replaced by full chain in PIC");
                    return full.getCachedMethodHandle().invokeExact(arguments);
                }
            }
            Object receiver = arguments[0];
            Object[] callArguments = new Object[arguments.length - 1];
            System.arraycopy(arguments, 1, callArguments, 0, callArguments.length);
            try {
                return cold.getMethod().doMethodInvoke(receiver, callArguments);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        }
        // Re-select without the cold tier so an always-invalid SwitchPoint (or any
        // permanent cold miss after class-domain failover) cannot recurse through
        // tryBuild → invokeColdReflective (GROOVY-12191).
        MethodHandleWrapper full = fallback(cold.callSite, cold.sender, cold.methodName, cold.callID,
                cold.safeNavigation, cold.thisCall, cold.spreadCall, 1, arguments, false);
        // PIC write policy — same as fromCacheHandle / selectMethodHandle:
        //
        // • canSetTarget == true  → store the full wrapper so the next hit for
        //   this receiver class reuses the linked chain (and can promote the
        //   call-site target).
        // • canSetTarget == false → store NULL_METHOD_HANDLE_WRAPPER, the PIC
        //   sentinel meaning "this receiver shape is not cacheable". Typical
        //   causes: per-instance MetaClass, spread-call (selector.cache=false).
        //   Storing the real uncacheable wrapper would pin a selection that is
        //   only valid for one instance / one spread shape and would skip
        //   re-selection on the next PIC hit. The sentinel forces
        //   fromCacheHandle to re-run fallback while this invocation still
        //   uses full.getCachedMethodHandle() for the current call.
        //
        // NULL_METHOD_HANDLE_WRAPPER is therefore the only safe PIC value when
        // the re-selected wrapper must not become a class-keyed cache entry.
        cold.callSite.put(receiverCacheKey(arguments[0]),
                full.isCanSetTarget() ? full : NULL_METHOD_HANDLE_WRAPPER);
        return full.getCachedMethodHandle().invokeExact(arguments);
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
        if ((fallbackCount > INDY_FALLBACK_THRESHOLD) && (callSite.getTarget() != defaultTarget)) {
            callSite.setTarget(defaultTarget);
            if (LOG_ENABLED) LOG.info("call site target reset to default, preparing outside invocation");
            callSite.resetFallbackCount();
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
     * Different {@code Class} objects (e.g. {@code A} vs {@code B}) share the same runtime class
     * ({@code java.lang.Class}) but dispatch to different methods. Including the represented class
     * name avoids PIC cache collisions for static-method call sites.
     */
    private static String receiverCacheKey(Object receiver) {
        if (receiver == null) return NULL_OBJECT_CLASS_NAME;
        if (receiver instanceof Class<?> c) return "java.lang.Class:" + c.getName();
        return receiver.getClass().getName();
    }

    private static MethodHandleWrapper fallback(CacheableCallSite callSite, Class<?> sender, String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) {
        return fallback(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, dummyReceiver, arguments, true);
    }

    private static MethodHandleWrapper fallback(CacheableCallSite callSite, Class<?> sender, String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments, boolean allowColdReflection) {
        // GEP-15: compound-assignment has its own resolver but rides the shared
        // call-site lifecycle (boot handle, PIC, promotion, deopt-storm guard).
        if (callID == CallType.COMPOUND_ASSIGN.ordinal()) {
            return IndyCompoundAssign.resolve(callSite, sender, methodName, arguments);
        }

        Selector selector = Selector.getSelector(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, arguments);

        if (INDY_COLD_REFLECTION && allowColdReflection && callID == CallType.METHOD.getOrderNumber()) {
            MethodHandleWrapper cold = ColdReflectiveMethodHandleWrapper.tryBuild(
                    selector, callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, arguments);
            if (cold != null) {
                if (LOG_ENABLED) LOG.info("using reflective cold tier for " + methodName);
                return cold;
            }
            // not a plain call: fall through; selection is deterministic, so the
            // partial selection above is simply redone by setCallSiteTarget()
        }

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

    /**
     * Invokedynamic bootstrap for a class's packed-closure dispatcher accessor (GROOVY-12151):
     * links the class's generated dispatch tables into one constant bundle, lazily on first
     * adapter creation. Delegates to {@link GeneratedDispatcher#bootstrap}; hosted here so
     * emitted bytecode references only this central bootstrap surface.
     *
     * @since 6.0.0
     */
    public static CallSite packedDispatchers(MethodHandles.Lookup caller, String name, MethodType type) throws Throwable {
        return GeneratedDispatcher.bootstrap(caller, name, type);
    }

    /**
     * Constant-dynamic bootstrap for a packed closure literal's declared parameter types
     * (GROOVY-12151): decodes a method descriptor into a {@code Class[]} resolved once per
     * literal site. Delegates to {@link GeneratedDispatcher#paramTypes}; hosted here so
     * emitted bytecode references only this central bootstrap surface.
     *
     * @since 6.0.0
     */
    public static Class<?>[] packedParamTypes(MethodHandles.Lookup caller, String name, Class<?> type, String descriptor) {
        return GeneratedDispatcher.paramTypes(caller, name, type, descriptor);
    }
}
