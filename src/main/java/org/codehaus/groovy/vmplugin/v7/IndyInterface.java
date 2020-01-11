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
package org.codehaus.groovy.vmplugin.v7;

import groovy.lang.GroovySystem;
import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.runtime.NullObject;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.invoke.SwitchPoint;
import java.util.Map;
import java.util.function.BiFunction;
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
    private static final long INDY_OPTIMIZE_THRESHOLD = SystemUtil.getLongSafe("groovy.indy.optimize.threshold", 100_000L);

    /**
     * flags for method and property calls
     */
    public static final int
            SAFE_NAVIGATION = 1, THIS_CALL = 2,
            GROOVY_OBJECT = 4, IMPLICIT_THIS = 8,
            SPREAD_CALL = 16, UNCACHED_CALL = 32;

    /**
     * Enum for easy differentiation between call types
     */
    public enum CallType {
        /**
         * Method invocation type
         */
        METHOD("invoke"),
        /**
         * Constructor invocation type
         */
        INIT("init"),
        /**
         * Get property invocation type
         */
        GET("getProperty"),
        /**
         * Set property invocation type
         */
        SET("setProperty"),
        /**
         * Cast invocation type
         */
        CAST("cast");

        private static final Map<String, CallType> NAME_CALLTYPE_MAP =
                Stream.of(CallType.values()).collect(Collectors.toMap(CallType::getCallSiteName, Function.identity()));

        /**
         * The name of the call site type
         */
        private final String name;

        CallType(String callSiteName) {
            this.name = callSiteName;
        }

        /**
         * Returns the name of the call site type
         */
        public String getCallSiteName() {
            return name;
        }

        public static CallType fromCallSiteName(String callSiteName) {
            return NAME_CALLTYPE_MAP.get(callSiteName);
        }
    }

    /**
     * Logger
     */
    protected static final Logger LOG;
    /**
     * boolean to indicate if logging for indy is enabled
     */
    protected static final boolean LOG_ENABLED;

    static {
        boolean enableLogger = false;

        LOG = Logger.getLogger(IndyInterface.class.getName());

        try {
            if (System.getProperty("groovy.indy.logging") != null) {
                LOG.setLevel(Level.ALL);
                enableLogger = true;
            }
        } catch (SecurityException e) {
            // Allow security managers to prevent system property access
        }

        LOG_ENABLED = enableLogger;
    }

    /**
     * LOOKUP constant used for for example unreflect calls
     */
    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * handle for the fromCache method
     */
    private static final MethodHandle FROM_CACHE_METHOD;

    /**
     * handle for the selectMethod method
     */
    private static final MethodHandle SELECT_METHOD;

    static {

        try {
            MethodType mt = MethodType.methodType(Object.class, MutableCallSite.class, Class.class, String.class, int.class, Boolean.class, Boolean.class, Boolean.class, Object.class, Object[].class);
            FROM_CACHE_METHOD = LOOKUP.findStatic(IndyInterface.class, "fromCache", mt);
        } catch (Exception e) {
            throw new GroovyBugError(e);
        }

        try {
            MethodType mt = MethodType.methodType(Object.class, MutableCallSite.class, Class.class, String.class, int.class, Boolean.class, Boolean.class, Boolean.class, Object.class, Object[].class);
            SELECT_METHOD = LOOKUP.findStatic(IndyInterface.class, "selectMethod", mt);
        } catch (Exception e) {
            throw new GroovyBugError(e);
        }
    }

    protected static SwitchPoint switchPoint = new SwitchPoint();

    static {
        GroovySystem.getMetaClassRegistry().addMetaClassRegistryChangeEventListener(cmcu -> invalidateSwitchPoints());
    }

    /**
     * Callback for constant meta class update change
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
     * bootstrap method for method calls from Groovy compiled code with indy
     * enabled. This method gets a flags parameter which uses the following
     * encoding:<ul>
     * <li>{@value #SAFE_NAVIGATION} is the flag value for safe navigation see {@link #SAFE_NAVIGATION}</li>
     * <li>{@value #THIS_CALL} is the flag value for a call on this see {@link #THIS_CALL}</li>
     * </ul>
     *
     * @param caller   - the caller
     * @param callType - the type of the call
     * @param type     - the call site type
     * @param name     - the real method name
     * @param flags    - call flags
     * @return the produced CallSite
     * @since Groovy 2.1.0
     */
    public static CallSite bootstrap(Lookup caller, String callType, MethodType type, String name, int flags) {
        CallType ct = CallType.fromCallSiteName(callType);
        if (null == ct) throw new GroovyBugError("Unknown call type: " + callType);

        int callID = ct.ordinal();
        boolean safe = (flags & SAFE_NAVIGATION) != 0;
        boolean thisCall = (flags & THIS_CALL) != 0;
        boolean spreadCall = (flags & SPREAD_CALL) != 0;

        return realBootstrap(caller, name, callID, type, safe, thisCall, spreadCall);
    }

    /**
     * backing bootstrap method with all parameters
     */
    private static CallSite realBootstrap(Lookup caller, String name, int callID, MethodType type, boolean safe, boolean thisCall, boolean spreadCall) {
        // since indy does not give us the runtime types
        // we produce first a dummy call site, which then changes the target to one when INDY_OPTIMIZE_THRESHOLD is reached,
        // that does the method selection including the direct call to the
        // real method.
        CacheableCallSite mc = new CacheableCallSite(type);
        final Class<?> sender = caller.lookupClass();
        MethodHandle mh = makeAdapter(mc, sender, name, callID, type, safe, thisCall, spreadCall);
        mc.setTarget(mh);
        mc.setDefaultTarget(mh);
        mc.setFallbackTarget(makeFallBack(mc, sender, name, callID, type, safe, thisCall, spreadCall));

        return mc;
    }

    /**
     * Makes a fallback method for an invalidated method selection
     */
    protected static MethodHandle makeFallBack(MutableCallSite mc, Class<?> sender, String name, int callID, MethodType type, boolean safeNavigation, boolean thisCall, boolean spreadCall) {
        return make(mc, sender, name, callID, type, safeNavigation, thisCall, spreadCall, SELECT_METHOD);
    }

    /**
     * Makes an adapter method for method selection, i.e. get the cached methodhandle(fast path) or fallback
     */
    private static MethodHandle makeAdapter(MutableCallSite mc, Class<?> sender, String name, int callID, MethodType type, boolean safeNavigation, boolean thisCall, boolean spreadCall) {
        return make(mc, sender, name, callID, type, safeNavigation, thisCall, spreadCall, FROM_CACHE_METHOD);
    }

    private static MethodHandle make(MutableCallSite mc, Class<?> sender, String name, int callID, MethodType type, boolean safeNavigation, boolean thisCall, boolean spreadCall, MethodHandle originalMH) {
        MethodHandle mh = MethodHandles.insertArguments(originalMH, 0, mc, sender, name, callID, safeNavigation, thisCall, spreadCall, /*dummy receiver:*/ 1);
        return mh.asCollector(Object[].class, type.parameterCount()).asType(type);
    }

    /**
     * Get the cached methodhandle. if the related methodhandle is not found in the inline cache, cache and return it.
     */
    public static Object fromCache(MutableCallSite callSite, Class<?> sender, String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) throws Throwable {
        MethodHandleWrapper mhw =
                doWithCallSite(
                        callSite, arguments,
                        (cs, receiver) ->
                                cs.getAndPut(
                                        receiver.getClass().getName(),
                                        c -> fallback(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, dummyReceiver, arguments)
                                )
                );

        if (mhw.isCanSetTarget() && (callSite.getTarget() != mhw.getTargetMethodHandle()) && (mhw.getLatestHitCount() > INDY_OPTIMIZE_THRESHOLD)) {
            callSite.setTarget(mhw.getTargetMethodHandle());
            if (LOG_ENABLED) LOG.info("call site target set, preparing outside invocation");

            mhw.resetLatestHitCount();
        }

        return mhw.getCachedMethodHandle().invokeExact(arguments);
    }

    /**
     * Core method for indy method selection using runtime types.
     */
    public static Object selectMethod(MutableCallSite callSite, Class<?> sender, String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) throws Throwable {
        final MethodHandleWrapper mhw = fallback(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, dummyReceiver, arguments);

        if (callSite instanceof CacheableCallSite) {
            CacheableCallSite cacheableCallSite = (CacheableCallSite) callSite;

            final MethodHandle defaultTarget = cacheableCallSite.getDefaultTarget();
            final long fallbackCount = cacheableCallSite.incrementFallbackCount();
            if ((fallbackCount > INDY_OPTIMIZE_THRESHOLD) && (cacheableCallSite.getTarget() != defaultTarget)) {
                cacheableCallSite.setTarget(defaultTarget);
                if (LOG_ENABLED) LOG.info("call site target reset to default, preparing outside invocation");

                cacheableCallSite.resetFallbackCount();
            }

            if (defaultTarget == cacheableCallSite.getTarget()) {
                // correct the stale methodhandle in the inline cache of callsite
                // it is important but impacts the performance somehow when cache misses frequently
                doWithCallSite(callSite, arguments, (cs, receiver) -> cs.put(receiver.getClass().getName(), mhw));
            }
        }

        return mhw.getCachedMethodHandle().invokeExact(arguments);
    }

    private static MethodHandleWrapper fallback(MutableCallSite callSite, Class<?> sender, String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) {
        Selector selector = Selector.getSelector(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, arguments);
        selector.setCallSiteTarget();

        return new MethodHandleWrapper(
                selector.handle.asSpreader(Object[].class, arguments.length).asType(MethodType.methodType(Object.class, Object[].class)),
                selector.handle,
                selector.cache
        );
    }

    private static <T> T doWithCallSite(MutableCallSite callSite, Object[] arguments, BiFunction<? super CacheableCallSite, ? super Object, ? extends T> f) {
        if (callSite instanceof CacheableCallSite) {
            CacheableCallSite cacheableCallSite = (CacheableCallSite) callSite;
            Object receiver = arguments[0];

            if (null == receiver) receiver = NullObject.getNullObject();

            return f.apply(cacheableCallSite, receiver);
        }

        throw new GroovyBugError("CacheableCallSite is expected, but the actual callsite is: " + callSite);
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
