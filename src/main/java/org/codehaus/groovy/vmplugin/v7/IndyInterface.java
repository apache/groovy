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
import java.lang.invoke.*;
import java.util.Arrays;

/**
 * Legacy class containing methods called by Groovy 2.5 Indy compiled bytecode.
 * Includes the interfacing methods with bytecode for invokedynamic and some helper methods and classes.
 */
@Deprecated
public class IndyInterface {

    /**
     * Flags for method and property calls.
     */
    public static final int
            SAFE_NAVIGATION = 1, THIS_CALL = 2,
            GROOVY_OBJECT = 4, IMPLICIT_THIS = 8,
            SPREAD_CALL = 16, UNCACHED_CALL = 32;

    /**
     * Enum for easy differentiation between call types.
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

        /**
         * The name of the call site type.
         */
        private final String name;

        CallType(String name) {
            this.name = name;
        }

        /**
         * Returns the name of the call site type.
         */
        public String getCallSiteName() {
            return name;
        }

        public static CallType fromCallSiteName(String callSiteName) {
            return Arrays.stream(CallType.values()).filter(type -> type.getCallSiteName().equals(callSiteName)).findFirst().orElse(null);
        }
    }

    protected static java.util.logging.Logger LOG;

    protected static final boolean LOG_ENABLED;

    static {
        try {
            if (Boolean.getBoolean("groovy.indy.logging")) {
                LOG = java.util.logging.Logger.getLogger(IndyInterface.class.getName());
                LOG.setLevel(java.util.logging.Level.ALL);
            }
        } catch (SecurityException ignore) {
        }
        LOG_ENABLED = (LOG != null);
    }

    /**
     * LOOKUP constant used for example in unreflect calls.
     */
    public static final MethodHandles.Lookup LOOKUP = org.codehaus.groovy.vmplugin.v8.IndyInterface.LOOKUP;

    protected static SwitchPoint switchPoint = new SwitchPoint();

    static {
        GroovySystem.getMetaClassRegistry().addMetaClassRegistryChangeEventListener(event -> invalidateSwitchPoints());
    }

    /**
     * Callback for constant meta class update change.
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
     * Bootstrap method for method calls from Groovy compiled code with indy
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
     * @since 2.1.0
     */
    public static CallSite bootstrap(MethodHandles.Lookup caller, String callType, MethodType type, String name, int flags) {
        return org.codehaus.groovy.vmplugin.v8.IndyInterface.bootstrap(caller, callType, type, name, flags);
    }

    /**
     * Get the cached methodhandle. if the related methodhandle is not found in the inline cache, cache and return it.
     */
    public static Object fromCache(MutableCallSite callSite, Class<?> sender, String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) throws Throwable {
        return org.codehaus.groovy.vmplugin.v8.IndyInterface.fromCache(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, dummyReceiver, arguments);
    }

    /**
     * Core method for indy method selection using runtime types.
     */
    public static Object selectMethod(MutableCallSite callSite, Class<?> sender, String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) throws Throwable {
        return org.codehaus.groovy.vmplugin.v8.IndyInterface.selectMethod(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, dummyReceiver, arguments);
    }

    /**
     * @since 2.5.0
     */
    public static CallSite staticArrayAccess(MethodHandles.Lookup lookup, String name, MethodType type) {
        return org.codehaus.groovy.vmplugin.v8.IndyInterface.staticArrayAccess(lookup, name, type);
    }
}
