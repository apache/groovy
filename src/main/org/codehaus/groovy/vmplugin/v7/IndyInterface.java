/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.vmplugin.v7;

import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassRegistryChangeEvent;
import groovy.lang.MetaClassRegistryChangeEventListener;
import groovy.lang.MetaMethod;
import groovy.lang.MetaObjectProtocol;
import groovy.lang.MetaProperty;
import groovy.lang.MissingMethodException;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.metaclass.MissingMethodExecutionFailed;
import org.codehaus.groovy.runtime.wrappers.Wrapper;

/**
 * Bytecode level interface for bootstrap methods used by invokedynamic.
 * 
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class IndyInterface {
        /**
         * flags for method and property calls
         */
        public static final int 
            SAFE_NAVIGATION = 1,  THIS_CALL     = 2, 
            GROOVY_OBJECT   = 4,  IMPLICIT_THIS = 8,
            SPREAD_CALL     = 16, UNCACHED_CALL = 32;

        public static enum CALL_TYPES {
            METHOD("invoke"), INIT("init"), GET("getProperty"), SET("setProperty");
            private final String name;
            private CALL_TYPES(String callSiteName) {
                this.name = callSiteName;
            }
            public String getCallSiteName(){ return name; }
        }
        
        protected final static Logger LOG;
        protected final static boolean LOG_ENABLED;
        static {
            LOG = Logger.getLogger(IndyInterface.class.getName());
            if (System.getProperty("groovy.indy.logging")!=null) {
                LOG.setLevel(Level.ALL);
                LOG_ENABLED = true;
            } else {
                LOG_ENABLED = false;
            }
        }
    /*
     * notes:
     *      MethodHandles#dropArguments: 
     *          invocation with (a,b,c), drop first 2 results in invocation
     *          with (a) only. 
     *      MethodHandles#insertArguments:
     *          invocation with (a,b,c), insert (x,y) results in error.
     *          first need to add with addParameters (X,Y), then bind them with 
     *          insert 
     */
    
        protected static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
        private static final MethodHandle SELECT_METHOD;
        static {
            MethodType mt = MethodType.methodType(Object.class, MutableCallSite.class, Class.class, String.class, int.class, Boolean.class, Boolean.class, Boolean.class, Object.class, Object[].class);
            try {
                SELECT_METHOD = LOOKUP.findStatic(IndyInterface.class, "selectMethod", mt);
            } catch (Exception e) {
                throw new GroovyBugError(e);
            }
        }
        private static final MethodType GENERAL_INVOKER_SIGNATURE = MethodType.methodType(Object.class, Object.class, Object[].class);
        protected static final MethodType INVOKE_METHOD_SIGNATURE = MethodType.methodType(Object.class, Class.class, Object.class, String.class, Object[].class, boolean.class, boolean.class);
        private static final MethodType O2O = MethodType.methodType(Object.class, Object.class);
        protected static final MethodHandle
            UNWRAP_METHOD,  SAME_MC,            IS_NULL,
            UNWRAP_EXCEPTION,   SAME_CLASS,
            META_METHOD_INVOKER,    GROOVY_OBJECT_INVOKER,
            HAS_CATEGORY_IN_CURRENT_THREAD_GUARD,
            BEAN_CONSTRUCTOR_PROPERTY_SETTER,
            META_PROPERTY_GETTER, META_CLASS_GET,
            SLOW_META_CLASS_FIND;
        static {
            try {
                UNWRAP_METHOD = LOOKUP.findStatic(IndyInterface.class, "unwrap", O2O);
                SAME_MC = LOOKUP.findStatic(IndyInterface.class, "isSameMetaClass", MethodType.methodType(boolean.class, MetaClass.class, Object.class));
                IS_NULL = LOOKUP.findStatic(IndyInterface.class, "isNull", MethodType.methodType(boolean.class, Object.class));
                UNWRAP_EXCEPTION = LOOKUP.findStatic(IndyInterface.class, "unwrap", MethodType.methodType(Object.class, GroovyRuntimeException.class));
                SAME_CLASS = LOOKUP.findStatic(IndyInterface.class, "sameClass", MethodType.methodType(boolean.class, Class.class, Object.class));
                META_METHOD_INVOKER = LOOKUP.findVirtual(MetaMethod.class, "doMethodInvoke", GENERAL_INVOKER_SIGNATURE);
                GROOVY_OBJECT_INVOKER = LOOKUP.findStatic(IndyInterface.class, "invokeGroovyObjectInvoker", MethodType.methodType(Object.class, MissingMethodException.class, Object.class, String.class, Object[].class));
                HAS_CATEGORY_IN_CURRENT_THREAD_GUARD = LOOKUP.findStatic(GroovyCategorySupport.class, "hasCategoryInCurrentThread", MethodType.methodType(boolean.class));
                BEAN_CONSTRUCTOR_PROPERTY_SETTER = LOOKUP.findStatic(IndyInterface.class, "setBeanProperties", MethodType.methodType(Object.class, MetaClass.class, Object.class, Map.class));
                META_PROPERTY_GETTER = LOOKUP.findVirtual(MetaProperty.class, "getProperty", O2O);
                META_CLASS_GET = LOOKUP.findVirtual(MetaObjectProtocol.class, "getProperty", MethodType.methodType(Object.class, Object.class, String.class));
                SLOW_META_CLASS_FIND = LOOKUP.findStatic(InvokerHelper.class, "getMetaClass", MethodType.methodType(MetaClass.class, Object.class));
            } catch (Exception e) {
                throw new GroovyBugError(e);
            }
        }
        protected static final MethodHandle NULL_REF = MethodHandles.constant(Object.class, null);
        
        protected static SwitchPoint switchPoint = new SwitchPoint();
        static {
            GroovySystem.getMetaClassRegistry().addMetaClassRegistryChangeEventListener(new MetaClassRegistryChangeEventListener() {
                public void updateConstantMetaClass(MetaClassRegistryChangeEvent cmcu) {
                	invalidateSwitchPoints();
                }
            });
        }

        protected static void invalidateSwitchPoints() {
            if (LOG_ENABLED) {
                 LOG.info("invalidating switch point");
            }
        	SwitchPoint old = switchPoint;
            switchPoint = new SwitchPoint();
            synchronized(IndyInterface.class) { SwitchPoint.invalidateAll(new SwitchPoint[]{old}); }
        }
        
        // the entry points from bytecode
        
        /**
         * bootstrap method for method calls from Groovy compiled code with indy 
         * enabled. This method gets a flags parameter which uses the following 
         * encoding:<ul>
         * <li>{@value #SAFE_NAVIGATION} is the flag value for safe navigation see {@link #SAFE_NAVIGATION}<li/>
         * <li>{@value #THIS_CALL} is the flag value for a call on this see {@link #THIS_CALL}</li>
         * </ul> 
         * @param caller - the caller
         * @param callType - the type of the call
         * @param type - the call site type
         * @param name - the real method name
         * @param flags - call flags
         * @return the produced CallSite
         * @since Groovy 2.1.0
         */
        public static CallSite bootstrap(Lookup caller, String callType, MethodType type, String name, int flags) {
            boolean safe = (flags&SAFE_NAVIGATION)!=0;
            boolean thisCall = (flags&THIS_CALL)!=0;
            boolean spreadCall = (flags&SPREAD_CALL)!=0;
            int callID;
            if (callType.equals(CALL_TYPES.METHOD.getCallSiteName())) {
                callID = CALL_TYPES.METHOD.ordinal();
            } else if (callType.equals(CALL_TYPES.INIT.getCallSiteName())) {
                callID = CALL_TYPES.INIT.ordinal();
            } else if (callType.equals(CALL_TYPES.GET.getCallSiteName())) {
                callID = CALL_TYPES.GET.ordinal();
            } else if (callType.equals(CALL_TYPES.SET.getCallSiteName())) {
                callID = CALL_TYPES.SET.ordinal();
            } else {
                throw new GroovyBugError("Unknown call type: "+callType);
            }
            return realBootstrap(caller, name, callID, type, safe, thisCall, spreadCall);
        }
        
        /**
         * bootstrap method for method calls with "this" as receiver
         * @deprecated since Groovy 2.1.0
         */
        public static CallSite bootstrapCurrent(Lookup caller, String name, MethodType type) {
            return realBootstrap(caller, name, CALL_TYPES.METHOD.ordinal(), type, false, true, false);
        }

        /**
         * bootstrap method for method calls with "this" as receiver safe
         * @deprecated since Groovy 2.1.0
         */
        public static CallSite bootstrapCurrentSafe(Lookup caller, String name, MethodType type) {
            return realBootstrap(caller, name, CALL_TYPES.METHOD.ordinal(), type, true, true, false);
        }
        
        /**
         * bootstrap method for standard method calls
         * @deprecated since Groovy 2.1.0
         */
        public static CallSite bootstrap(Lookup caller, String name, MethodType type) {
            return realBootstrap(caller, name, CALL_TYPES.METHOD.ordinal(), type, false, false, false);
        }
        
        /**
         * bootstrap method for null safe standard method calls
         * @deprecated since Groovy 2.1.0
         */
        public static CallSite bootstrapSafe(Lookup caller, String name, MethodType type) {
            return realBootstrap(caller, name, CALL_TYPES.METHOD.ordinal(), type, true, false, false);
        }
        
        /**
         * backing bootstrap method with all parameters
         */
        private static CallSite realBootstrap(Lookup caller, String name, int callID, MethodType type, boolean safe, boolean thisCall, boolean spreadCall) {
            // since indy does not give us the runtime types
            // we produce first a dummy call site, which then changes the target to one,
            // that does the method selection including the the direct call to the 
            // real method.
            MutableCallSite mc = new MutableCallSite(type);
            MethodHandle mh = makeFallBack(mc,caller.lookupClass(),name,callID,type,safe,thisCall,spreadCall);
            mc.setTarget(mh);
            return mc;
        }
        
        /**
         * Makes a fallback method for an invalidated method selection
         */
        protected static MethodHandle makeFallBack(MutableCallSite mc, Class<?> sender, String name, int callID, MethodType type, boolean safeNavigation, boolean thisCall, boolean spreadCall) {
            MethodHandle mh = MethodHandles.insertArguments(SELECT_METHOD, 0, mc, sender, name, callID, safeNavigation, thisCall, spreadCall, /*dummy receiver:*/ 1);
            mh =    mh.asCollector(Object[].class, type.parameterCount()).
                    asType(type);
            return mh;
        }

        /**
         * This method is called by he handle to realize the bean constructor
         * with property map.
         */
        public static Object setBeanProperties(MetaClass mc, Object bean, Map properties) {
            for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = entry.getKey().toString();

                Object value = entry.getValue();
                mc.setProperty(bean, key, value);
            }
            return bean;
        }

        /**
         * {@link GroovyObject#invokeMethod(String, Object)} path as fallback.
         * This method is called by the handle as exception handler in case the
         * selected method causes a MissingMethodExecutionFailed, where
         * we will just give through the exception, and a normal 
         * MissingMethodException where we call {@link GroovyObject#invokeMethod(String, Object)}
         * if receiver class, the type transported by the exception and the name
         * for the method stored in the exception and our current method name 
         * are equal.
         * Should those conditions not apply we just rethrow the exception.
         */
        public static Object invokeGroovyObjectInvoker(MissingMethodException e, Object receiver, String name, Object[] args) {
            if (e instanceof MissingMethodExecutionFailed) {
                throw (MissingMethodException)e.getCause();
            } else if (receiver.getClass() == e.getType() && e.getMethod().equals(name)) {
                //TODO: we should consider calling this one directly for MetaClassImpl,
                //      then we save the new method selection
                
                // in case there's nothing else, invoke the object's own invokeMethod()
                return ((GroovyObject)receiver).invokeMethod(name, args);
            } else {
                throw e;
            }
        }
        
        /**
         * Unwraps a {@link GroovyRuntimeException}.
         * This method is called by the handle to unwrap internal exceptions 
         * of the runtime.
         */
        public static Object unwrap(GroovyRuntimeException gre) throws Throwable {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
        
        /**
         * called by handle
         */
        public static boolean isSameMetaClass(MetaClass mc, Object receiver) {
            //TODO: remove this method if possible by switchpoint usage
            return receiver instanceof GroovyObject && mc==((GroovyObject)receiver).getMetaClass(); 
        }
        
        /**
         * Unwraps a {@link Wrapper}.
         * This method is called by the handle to unwrap a Wrapper, which
         * we use to force method selection.
         */
        public static Object unwrap(Object o) {
            Wrapper w = (Wrapper) o;
            return w.unwrap();
        }
        
        /**
         * Guard to check if the argument is null.
         * This method is called by the handle to check
         * if the provided argument is null.
         */
        public static boolean isNull(Object o) {
            return o == null;
        }
        
        /**
         * Guard to check if the argument is not null.
         * This method is called by the handle to check
         * if the provided argument is not null.
         */
        public static boolean isNotNull(Object o) {
            return o != null;
        }
        
        /**
         * Filter to negate a boolean
         */
        public static boolean negateBool(boolean b) {
        	return !b;
        }
        
        /**
         * Guard to check if the provided Object has the same
         * class as the provided Class. This method will
         * return false if the Object is null.
         */
        public static boolean sameClass(Class c, Object o) {
            if (o==null) return false;
            return o.getClass() == c;
        }

        /**
         * Core method for indy method selection using runtime types.
         */
        public static Object selectMethod(MutableCallSite callSite, Class sender, String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) throws Throwable {
            Selector selector = Selector.getSelector(callSite, sender, methodName, callID, safeNavigation, thisCall, spreadCall, arguments); 
            selector.setCallSiteTarget();

            MethodHandle call = selector.handle.asSpreader(Object[].class, arguments.length);
            call = call.asType(MethodType.methodType(Object.class,Object[].class));
            return call.invokeExact(arguments);
        }
}
