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

import groovy.lang.AdaptingMetaClass;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaClassImpl.MetaConstructor;
import groovy.lang.MetaClassRegistryChangeEvent;
import groovy.lang.MetaClassRegistryChangeEventListener;
import groovy.lang.MetaMethod;
import groovy.lang.MetaObjectProtocol;
import groovy.lang.MetaProperty;
import groovy.lang.MissingMethodException;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.NullObject;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.dgmimpl.NumberNumberMetaMethod;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.runtime.metaclass.MethodMetaProperty;
import org.codehaus.groovy.runtime.metaclass.MissingMethodExecutionFailed;
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod;
import org.codehaus.groovy.runtime.metaclass.ReflectionMetaMethod;
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
            SAFE_NAVIGATION = 0x1,  THIS_CALL     = 0x2, 
            GROOVY_OBJECT   = 0x4,  IMPLICIT_THIS = 0x8,
            SPREAD_CALL     = 0x16;

        public static enum CALL_TYPES {
            METHOD("invoke"), INIT("init"), GET("getProperty"), SET("setProperty");
            private final String name;
            private CALL_TYPES(String callSiteName) {
                this.name = callSiteName;
            }
            public String getCallSiteName(){ return name; }
        }
        
        private final static Logger LOG;
        private final static boolean LOG_ENABLED;
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
    
        private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
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
        private static final MethodType INVOKE_METHOD_SIGNATURE = MethodType.methodType(Object.class, Class.class, Object.class, String.class, Object[].class, boolean.class, boolean.class);
        private static final MethodType O2O = MethodType.methodType(Object.class, Object.class);
        private static final MethodHandle   
            UNWRAP_METHOD,  SAME_MC,            IS_NULL,
            UNWRAP_EXCEPTION,   SAME_CLASS,
            META_METHOD_INVOKER,    GROOVY_OBJECT_INVOKER,
            HAS_CATEGORY_IN_CURRENT_THREAD_GUARD,
            BEAN_CONSTRUCOTOR_PROPERTY_SETTER;
        static {
            try {
                UNWRAP_METHOD = LOOKUP.findStatic(IndyInterface.class, "unwrap", O2O);
                SAME_MC = LOOKUP.findStatic(IndyInterface.class, "isSameMetaClass", MethodType.methodType(boolean.class, MetaClass.class, Object.class));
                IS_NULL = LOOKUP.findStatic(IndyInterface.class, "isNull", MethodType.methodType(boolean.class, Object.class));
                UNWRAP_EXCEPTION = LOOKUP.findStatic(IndyInterface.class, "unwrap", MethodType.methodType(Object.class, GroovyRuntimeException.class));
                SAME_CLASS = LOOKUP.findStatic(IndyInterface.class, "sameClass", MethodType.methodType(boolean.class, Class.class, Object.class));
                META_METHOD_INVOKER = LOOKUP.findVirtual(MetaMethod.class, "invoke", GENERAL_INVOKER_SIGNATURE);
                GROOVY_OBJECT_INVOKER = LOOKUP.findStatic(IndyInterface.class, "invokeGroovyObjectInvoker", MethodType.methodType(Object.class, MissingMethodException.class, Object.class, String.class, Object[].class));
                HAS_CATEGORY_IN_CURRENT_THREAD_GUARD = LOOKUP.findStatic(GroovyCategorySupport.class, "hasCategoryInCurrentThread", MethodType.methodType(boolean.class));
                BEAN_CONSTRUCOTOR_PROPERTY_SETTER = LOOKUP.findStatic(IndyInterface.class, "setBeanProperties", MethodType.methodType(Object.class, MetaClass.class, Object.class, Map.class));
            } catch (Exception e) {
                throw new GroovyBugError(e);
            }
        }
        private static final MethodHandle NULL_REF = MethodHandles.constant(Object.class, null);
        
        private static SwitchPoint switchPoint = new SwitchPoint();
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
        private static MethodHandle makeFallBack(MutableCallSite mc, Class<?> sender, String name, int callID, MethodType type, boolean safeNavigation, boolean thisCall, boolean spreadCall) {
            MethodHandle mh = MethodHandles.insertArguments(SELECT_METHOD, 0, mc, sender, name, callID, safeNavigation, thisCall, spreadCall, /*dummy receiver:*/ 1);
            mh =    mh.asCollector(Object[].class, type.parameterCount()).
                    asType(type);
            return mh;
        }
        
        /**
         * Gives the meta class to an Object.
         */
        private static MetaClass getMetaClass(CallInfo ci) {
            Object receiver = ci.args[0];
            if (receiver == null) {
                return NullObject.getNullObject().getMetaClass();
            } else if (receiver instanceof GroovyObject) {
                return ((GroovyObject) receiver).getMetaClass();
            } else if (ci.callID == CALL_TYPES.INIT.ordinal()) {
                return GroovySystem.getMetaClassRegistry().getMetaClass((Class) receiver);
            } else {
                return ((MetaClassRegistryImpl) GroovySystem.getMetaClassRegistry()).getMetaClass(receiver);
            }
        }
        
        /**
         * Returns if a method is static
         */
        private static boolean isStatic(Method m) {
            int mods = m.getModifiers();
            return (mods & Modifier.STATIC) != 0;
        }
        
        /**
         * Creates a MethodHandle using a before selected MetaMethod.
         * If the MetaMethod has reflective information available, then
         * we will use that information to create the target MethodHandle. 
         * If that is not the case we will produce a handle, which will use the
         * MetaMethod itself for invocation.
         */
        private static void setHandleForMetaMethod(CallInfo info) {
            MetaMethod metaMethod = info.method;

            if (metaMethod instanceof NumberNumberMetaMethod) {
            	if (LOG_ENABLED) LOG.info("meta method is number method");
                info.catchException = false;
                if (IndyMath.chooseMathMethod(info, metaMethod)) {
                    if (LOG_ENABLED) LOG.info("indy math successfull");
                    return;
                }
            }
            
            boolean isCategoryTypeMethod = metaMethod instanceof NewInstanceMetaMethod;
            if (LOG_ENABLED) LOG.info("meta method is category type method: "+isCategoryTypeMethod);
            
            if (metaMethod instanceof ReflectionMetaMethod) {
                if (LOG_ENABLED) LOG.info("meta method is reflective method");
                ReflectionMetaMethod rmm = (ReflectionMetaMethod) metaMethod;
                metaMethod = rmm.getCachedMethod();
            }
            
            if (metaMethod instanceof CachedMethod) {
                if (LOG_ENABLED) LOG.info("meta method is CachedMethod instance");
                CachedMethod cm = (CachedMethod) metaMethod;
                info.isVargs = cm.isVargsMethod();
                try {
                    Method m = cm.getCachedMethod();
                    info.handle = LOOKUP.unreflect(m);
                    if (LOG_ENABLED) LOG.info("successfully unreflected method");
                    if (!isCategoryTypeMethod && isStatic(m)) {
                        info.handle = MethodHandles.dropArguments(info.handle, 0, Class.class);
                    }
                } catch (IllegalAccessException e) {
                    throw new GroovyBugError(e);
                }
            } else if (metaMethod instanceof MetaConstructor) {
                if (LOG_ENABLED) LOG.info("meta method is MetaConstructor instance");
                MetaConstructor mc = (MetaConstructor) metaMethod;
                info.isVargs = mc.isVargsMethod();
                Constructor con = mc.getCachedConstrcutor().cachedConstructor;
                try {
                    info.handle = LOOKUP.unreflectConstructor(con);
                    if (LOG_ENABLED) LOG.info("successfully unreflected constructor");
                } catch (IllegalAccessException e) {
                    throw new GroovyBugError(e);
                }
            } else if (info.method != null) {
                if (LOG_ENABLED) LOG.info("meta method is dgm helper");
                // dgm method helper path
                info.handle = META_METHOD_INVOKER;
                info.handle = info.handle.bindTo(info.method);
                if (LOG_ENABLED) LOG.info("bound method name to META_METHOD_INVOKER");
                if (info.method.getNativeParameterTypes().length==1 && 
                    info.args.length==1) 
                {
                    // the method expects a parameter but we don't provide an 
                    // argument for that. So we give in a Object[], containing 
                    // a null value
                    // since MethodHandles.insertArguments is a vargs method giving
                    // only the array would be like just giving a null value, so
                    // we need to wrap the array that represents our argument in
                    // another one for the vargs call
                    info.handle = MethodHandles.insertArguments(info.handle, 1, new Object[]{new Object[]{null}});
                    if (LOG_ENABLED) LOG.info("null argument expansion");
                } else if (info.method.isVargsMethod()) {
                    // the method expects the arguments as Object[] in a Object[]
                    info.handle = info.handle.asCollector(Object[].class, 1);
                    info.handle = info.handle.asCollector(Object[].class, info.targetType.parameterCount()-1);
                    info.currentType = MethodType.methodType(info.method.getReturnType(), info.method.getNativeParameterTypes());
                    info.currentType = info.currentType.insertParameterTypes(0, info.method.getDeclaringClass().getTheClass());
                    if (LOG_ENABLED) LOG.info("wrapping vargs for dgm helper");
                } else {
                    info.handle = info.handle.asCollector(Object[].class, info.targetType.parameterCount()-1);
                    info.currentType = MethodType.methodType(info.method.getReturnType(), info.method.getNativeParameterTypes());
                    info.currentType = info.currentType.insertParameterTypes(0, info.method.getDeclaringClass().getTheClass());
                    if (LOG_ENABLED) LOG.info("normal dgm helper wrapping");
                }
            }
        }
        
        /**
         * Uses the meta class to get a meta method.
         */
        private static void chooseMethod(Object receiver, MetaClassImpl mci, CallInfo ci) {
            if (receiver instanceof Class) {
                if (LOG_ENABLED) LOG.info("receiver is a class");
                ci.method = mci.retrieveStaticMethod(ci.name, removeRealReceiver(ci.args));
            } else {
                ci.method = mci.getMethodWithCaching(ci.selector, ci.name, removeRealReceiver(ci.args), false);
            }
        }
        
        /**
         * Creates a MethodHandle, which will use the meta class path.
         * This method is called only if no handle has been created before. This
         * is usually the case if the method selection failed.
         */
        private static void setMetaClassCallHandleIfNedded(MetaClass mc, CallInfo ci) {
            if (ci.handle!=null) return;
            try {
                ci.useMetaClass = true;
                if (LOG_ENABLED) LOG.info("set meta class invocation path");
                Object receiver = ci.args[0];
                if (receiver instanceof Class) {
                    ci.handle = LOOKUP.findVirtual(mc.getClass(), "invokeStaticMethod", MethodType.methodType(Object.class, Object.class, String.class, Object[].class));
                    ci.handle = ci.handle.bindTo(mc);
                    if (LOG_ENABLED) LOG.info("use invokeStaticMethod with bound meta class");
                } else {
                    boolean useShortForm = mc instanceof AdaptingMetaClass;
                    if (useShortForm) {
                        ci.handle = LOOKUP.findVirtual(MetaObjectProtocol.class, "invokeMethod", MethodType.methodType(Object.class, Object.class, String.class, Object[].class));
                    } else {
                        ci.handle = LOOKUP.findVirtual(MetaClass.class, "invokeMethod", INVOKE_METHOD_SIGNATURE);
                        ci.handle = MethodHandles.insertArguments(ci.handle, ci.handle.type().parameterCount()-2, false, true);
                    }
                    
                    ci.handle = ci.handle.bindTo(mc);
                    if (!useShortForm) {
                        ci.handle = ci.handle.bindTo(ci.selector);
                    }
                    if (LOG_ENABLED) LOG.info("use invokeMethod with bound meta class");
                    
                    if (receiver instanceof GroovyObject) {
                        // if the meta class call fails we may still want to fall back to call
                        // GroovyObject#invokeMethod if the receiver is a GroovyObject
                        if (LOG_ENABLED) LOG.info("add MissingMethod handler for GrooObject#invokeMethod fallback path");
                        ci.handle = MethodHandles.catchException(ci.handle, MissingMethodException.class, GROOVY_OBJECT_INVOKER);
                    }
                }
                ci.handle = MethodHandles.insertArguments(ci.handle, 1, ci.name);
                ci.handle = ci.handle.asCollector(Object[].class, ci.targetType.parameterCount()-1);
                if (LOG_ENABLED) LOG.info("bind method name and create collector for arguments");
            } catch (Exception e) {
                throw new GroovyBugError(e);
            }
            
           
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
         * Corrects method argument wrapping.
         * In cases in which we want to force a certain method selection
         * we use Wrapper classes to transport the static type information.
         * This method will be used to undo thee wrapping.
         */
        private static void correctWrapping(CallInfo ci) {
            if (ci.useMetaClass) return;
            Class[] pt = ci.handle.type().parameterArray();
            if (ci.currentType!=null) pt = ci.currentType.parameterArray();
            for (int i=1; i<ci.args.length; i++) {
                if (ci.args[i] instanceof Wrapper) {
                    Class type = pt[i];
                    MethodType mt = MethodType.methodType(type, Object.class);
                    ci.handle = MethodHandles.filterArguments(ci.handle, i, UNWRAP_METHOD.asType(mt));
                    if (LOG_ENABLED) LOG.info("added filter for Wrapper for argument at pos "+i);
                }
            }
        }
        
        /**
         * There are some conversions we have to do explicitly.
         * These are GString to String, Number to Byte and Number to BigInteger
         * conversions.
         */
        private static void correctCoerce(CallInfo ci) {
            if (ci.useMetaClass) return;
            if (ci.beanConstructor) return;
            
            Class[] parameters = ci.handle.type().parameterArray();
            if (ci.currentType!=null) parameters = ci.currentType.parameterArray();
            if (ci.args.length != parameters.length) {
                throw new GroovyBugError("At this point argument array length and parameter array length should be the same");
            }
            for (int i=0; i<ci.args.length; i++) {
            	if (parameters[i]==Object.class) continue; 
                Object arg = ci.args[i];
                // we have to handle here different cases in which we do no
                // transformations. We depend on our method selection to have
                // selected only a compatible method, that means for a null
                // argument we don't have to do anything. Same of course is if
                // the argument is an instance of the parameter type. We also
                // exclude boxing, since the MethodHandles will do that part
                // already for us. Another case is the conversion of a primitive
                // to another primitive or of the wrappers, or a combination of 
                // these. This is also handled already. What is left is the 
                // GString conversion and the number conversions.
                if (arg==null) continue;
                Class got = arg.getClass();
                if (got==parameters[i]) continue;
                Class wrappedPara = TypeHelper.getWrapperClass(parameters[i]);
                if (wrappedPara==got) continue;
                if (parameters[i].isAssignableFrom(got)) continue;
                if (isPrimitiveOrWrapper(parameters[i]) && isPrimitiveOrWrapper(got)) continue;
                ci.handle = TypeTransformers.addTransformer(ci.handle, i, arg, wrappedPara);
                if (LOG_ENABLED) LOG.info("added transformer at pos "+i+" for type "+got+" to type "+wrappedPara);
            }
        }

        /**
         * Return true if the given argument is either a primitive or
         * one of its wrapper. 
         */
        private static boolean isPrimitiveOrWrapper(Class c) {
            return  c == byte.class    || c == Byte.class      ||
                    c == int.class     || c == Integer.class   ||
                    c == long.class    || c == Long.class      ||
                    c == float.class   || c == Float.class     ||
                    c == double.class  || c == Double.class    ||
                    c == short.class   || c == Short.class     ||
                    c == boolean.class || c == Boolean.class   ||
                    c == char.class    || c == Character.class;
        }



        /**
         * Gives a replacement receiver for null.
         * In case of the receiver being null we want to do the method
         * invocation on NullObject instead.
         */
        private static void correctNullReceiver(CallInfo ci){
            if (ci.args[0]!=null) return;
            ci.handle = ci.handle.bindTo(NullObject.getNullObject());
            ci.handle = MethodHandles.dropArguments(ci.handle, 0, ci.targetType.parameterType(0));
            if (LOG_ENABLED) LOG.info("binding null object receiver and dropping old receiver");
        }
        
        /**
         * Sets all argument and receiver guards.
         */
        private static void setGuards(CallInfo ci, Object receiver) {
            if (ci.handle==null) return;
            if (ci.spread) return;
            
            MethodHandle fallback = makeFallBack(ci.callSite, ci.sender, ci.name, ci.callID, ci.targetType, ci.safeNavigationOrig, ci.thisCall, ci.spread);
            
            // special guards for receiver
            if (receiver instanceof GroovyObject) {
                GroovyObject go = (GroovyObject) receiver;
                MetaClass mc = (MetaClass) go.getMetaClass();
                MethodHandle test = SAME_MC.bindTo(mc); 
                // drop dummy receiver
                test = test.asType(MethodType.methodType(boolean.class,ci.targetType.parameterType(0)));
                ci.handle = MethodHandles.guardWithTest(test, ci.handle, fallback);
                if (LOG_ENABLED) LOG.info("added meta class equality check");
            }
            
            if (!ci.useMetaClass) {
                // category method needs Thread check
                // cases:
                // (1) method is a category method
                //     We need to check if the category in the current thread is still active.
                //     Since we invalidate on leaving the category checking for it being
                //     active directly is good enough.
                // (2) method is in use scope, but not from category
                //     Since entering/leaving a category will invalidate, there is no need for any special check
                // (3) method is not in use scope /and not from category
                //     Since entering/leaving a category will invalidate, there is no need for any special check
                if (ci.method instanceof NewInstanceMetaMethod) {
                	ci.handle = MethodHandles.guardWithTest(HAS_CATEGORY_IN_CURRENT_THREAD_GUARD, ci.handle, fallback);
                    if (LOG_ENABLED) LOG.info("added category-in-current-thread-guard for category method");
                }
            }
            
            // handle constant meta class and category changes
            ci.handle = switchPoint.guardWithTest(ci.handle, fallback);
            if (LOG_ENABLED) LOG.info("added switch point guard");
            
            // guards for receiver and parameter
            Class[] pt = ci.handle.type().parameterArray();
            for (int i=0; i<ci.args.length; i++) {
                Object arg = ci.args[i];
                MethodHandle test = null;
                if (arg==null) {
                    test = IS_NULL.asType(MethodType.methodType(boolean.class, pt[i]));
                    if (LOG_ENABLED) LOG.info("added null argument check at pos "+i);
                } else { 
                    Class argClass = arg.getClass();
                    if (Modifier.isFinal(argClass.getModifiers()) && TypeHelper.argumentClassIsParameterClass(argClass,pt[i])) continue;
                    test = SAME_CLASS.
                                bindTo(argClass).
                                asType(MethodType.methodType(boolean.class, pt[i]));
                    if (LOG_ENABLED) LOG.info("added same class check at pos "+i);
                }
                Class[] drops = new Class[i];
                for (int j=0; j<drops.length; j++) drops[j] = pt[j];
                test = MethodHandles.dropArguments(test, 0, drops);
                ci.handle = MethodHandles.guardWithTest(test, ci.handle, fallback);
            }
        }

        private static void correctConstructor(MetaClass mc, CallInfo info) {
            if (info.handle==null) return;
            if (info.callID == CALL_TYPES.INIT.ordinal()) {
                if (info.beanConstructor) {
                    // we have handle that takes no arguments to create the bean, 
                    // we have to use its return value to call #setBeanProperties with it
                    // and the meta class.

                    // to do this we first bind the values to #setBeanProperties
                    MethodHandle handle = BEAN_CONSTRUCOTOR_PROPERTY_SETTER.bindTo(mc);
                    info.handle = MethodHandles.foldArguments(handle, info.handle.asType(MethodType.methodType(Object.class)));
                }
                info.handle = MethodHandles.dropArguments(info.handle, 0, Class.class);
            }
        }

        /**
         * Handles cases in which we have to correct the length of arguments
         * using the parameters. This might be needed for vargs and for one 
         * parameter calls without arguments (null is used then).  
         */
        private static void correctParameterLength(CallInfo info) {
            if (info.handle==null) return;
            if (info.beanConstructor) return;
            
            Class[] params = info.handle.type().parameterArray();
            if (!info.isVargs) {
                if (params.length != info.args.length) {
                  //TODO: add null argument
                }
                return;
            }

            Class lastParam = params[params.length-1];
            Object lastArg = info.args[info.args.length-1];
            if (params.length == info.args.length) {
                // may need rewrap
                if (lastParam == lastArg || lastArg == null) return;
                if (lastParam.isInstance(lastArg)) return;
                // arg is not null and not assignment compatible
                // so we really need to rewrap
                info.handle = info.handle.asCollector(lastParam, 1);
            } else if (params.length > info.args.length) {
                // we depend on the method selection having done a good 
                // job before already, so the only case for this here is, that
                // we have no argument for the array, meaning params.length is
                // args.length+1. In that case we have to fill in an empty array
                info.handle = MethodHandles.insertArguments(info.handle, params.length-1, Array.newInstance(lastParam.getComponentType(), 0));
                if (LOG_ENABLED) LOG.info("added empty array for missing vargs part");
            } else { //params.length < args.length
                // we depend on the method selection having done a good 
                // job before already, so the only case for this here is, that
                // all trailing arguments belong into the vargs array
                info.handle = info.handle.asCollector(
                        lastParam,
                        info.args.length - params.length + 1);
                if (LOG_ENABLED) LOG.info("changed surplus arguments to be collected for vargs call");
            }
            
        }
        
        /**
         * Adds the standard exception handler.  
         */
        private static void addExceptionHandler(CallInfo info) {
            //TODO: if we would know exactly which paths require the exceptions
            //      and which paths not, we can sometimes save this guard 
            if (info.handle==null || info.catchException==false) return;
            Class returnType = info.handle.type().returnType();
            if (returnType!=Object.class) {
                MethodType mtype = MethodType.methodType(returnType, GroovyRuntimeException.class); 
                info.handle = MethodHandles.catchException(info.handle, GroovyRuntimeException.class, UNWRAP_EXCEPTION.asType(mtype));
            } else {
                info.handle = MethodHandles.catchException(info.handle, GroovyRuntimeException.class, UNWRAP_EXCEPTION);
            }
            if (LOG_ENABLED) LOG.info("added GroovyRuntimeException unwrapper");
        }
        
        /**
         * Sets the null constant for safe navigation.
         * In case of foo?.bar() and foo being null, we don't call the method,
         * instead we simply return null. This produces a handle, which will 
         * return the constant.
         */
        private static boolean setNullForSafeNavigation(CallInfo info) {
            if (!info.safeNavigation) return false;
            info.handle = MethodHandles.dropArguments(NULL_REF,0,info.targetType.parameterArray());
            if (LOG_ENABLED) LOG.info("set null returning handle for safe navigation");
            return true;
        }
        
        /**
         * Sets the method selection base.
         */
        private static void setMethodSelectionBase(CallInfo ci, MetaClass mc) {
            if (ci.thisCall) {
                ci.selector = ci.sender;
            } else if (ci.args[0]==null) {
                ci.selector = NullObject.class;
            } else {
                ci.selector = mc.getTheClass();
            }
            if (LOG_ENABLED) LOG.info("method selection base set to "+ci.selector);
        }
        
        /**
         * Core method for indy method selection using runtime types.
         */
        public static Object selectMethod(MutableCallSite callSite, Class sender, String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) throws Throwable {
            //TODO: handle GroovyInterceptable 
            CallInfo callInfo = new CallInfo();
            callInfo.callID = callID;
            callInfo.targetType = callSite.type();
            callInfo.name = methodName;
            callInfo.args = spread(arguments, spreadCall);
            callInfo.callSite = callSite;
            callInfo.sender = sender;
            callInfo.safeNavigationOrig = safeNavigation;
            callInfo.safeNavigation = safeNavigation && arguments[0]==null;
            callInfo.thisCall = thisCall;
            callInfo.spread = spreadCall;
            if (LOG_ENABLED) {
                String msg =
                    "----------------------------------------------------"+
                    "\n\t\tinvocation of method '"+methodName+"'"+
                    "\n\t\tinvocation type: "+CALL_TYPES.values()[callID]+
                    "\n\t\tsender: "+sender+
                    "\n\t\ttargetType: "+callInfo.targetType+
                    "\n\t\tsafe navigation: "+safeNavigation+
                    "\n\t\tthisCall: "+thisCall+
                    "\n\t\tspreadCall: "+spreadCall+
                    "\n\t\twith "+arguments.length+" arguments";
                for (int i=0; i<arguments.length; i++) {
                    msg += "\n\t\t\targument["+i+"] = "+arguments[i];
                }
                LOG.info(msg);
            }

            if (!setNullForSafeNavigation(callInfo)) {
                //            setInterceptableHandle(callInfo);
                MetaClass mc = getMetaClass(callInfo);
                if (LOG_ENABLED) LOG.info("meta class is "+mc);
                setMethodSelectionBase(callInfo, mc);
                chooseMethodOrProperty(mc, callInfo);
                setHandleForMetaMethod(callInfo);
                correctConstructor(mc, callInfo);
                setMetaClassCallHandleIfNedded(mc, callInfo);
                correctWrapping(callInfo);
                correctParameterLength(callInfo);
                correctCoerce(callInfo);
                correctNullReceiver(callInfo);
                correctSpreading(callInfo);

                if (LOG_ENABLED) LOG.info("casting explicit from "+callInfo.handle.type()+" to "+callInfo.targetType);
                callInfo.handle =  MethodHandles.explicitCastArguments(callInfo.handle,callInfo.targetType);

                addExceptionHandler(callInfo);
            } 
            setGuards(callInfo, callInfo.args[0]);

            if (callInfo.spread) {
                if (LOG_ENABLED) LOG.info("call site target kept for spread call");
            } else {
                callSite.setTarget(callInfo.handle);
                if (LOG_ENABLED) LOG.info("call site target set, preparing outside invocation");
            }

            MethodHandle call = callInfo.handle.asSpreader(Object[].class, arguments.length);
            call = call.asType(MethodType.methodType(Object.class,Object[].class));
            return call.invokeExact(arguments);
        }
        
        private static void correctSpreading(CallInfo ci) {
            if (!ci.spread) return;
            ci.handle = ci.handle.asSpreader(Object[].class, ci.args.length-1);
        }

        /**
         * Uses the meta class to get a meta method for a method call, 
         * constructor invocation, property set or property get.
         * There will be no meta method selected, if the meta class is no MetaClassImpl
         * or the meta class is an AdaptingMetaClass.
         */
        private static void chooseMethodOrProperty(MetaClass mc, CallInfo callInfo) {
            int callID = callInfo.callID;
            if (!(mc instanceof MetaClassImpl) || mc instanceof AdaptingMetaClass) {
                if (LOG_ENABLED) LOG.info("meta class is neither MetaClassImpl nor AdoptingMetaClass, normal method selection path disabled.");
                return;
            }
            if (LOG_ENABLED) LOG.info("meta class is a MetaClassImpl");
            
            MetaClassImpl mci = (MetaClassImpl) mc;
            Object receiver = callInfo.args[0];
            if (receiver==null) {
                if (LOG_ENABLED) LOG.info("receiver is null");
                receiver = NullObject.getNullObject();
            }
            
            if (callID == CALL_TYPES.INIT.ordinal()) {
                chooseConstructor(mci, callInfo);
            } else if (callID == CALL_TYPES.METHOD.ordinal()) {
                chooseMethod(receiver, mci, callInfo);
            } else {
                chooseProperty(receiver, mci, callInfo);
            }
            if (LOG_ENABLED) LOG.info("retrieved method from meta class: "+callInfo.method);
        }

        /**
         * this method chooses a property from the meta class.
         */
        private static void chooseProperty(Object receiver, MetaClassImpl mci, CallInfo callInfo) {
            if (receiver instanceof GroovyObject) {
                Class aClass = receiver.getClass();
                Method method = null;
                try {
                    method = aClass.getMethod("getProperty", String.class);
                    if (!method.isSynthetic()) {
                        callInfo.method = new ReflectionMetaMethod(new CachedMethod(method));
                    }
                } catch (NoSuchMethodException e) {}

            } 
            
            if (callInfo.method==null) {
                MetaProperty res = mci.getEffectiveGetMetaProperty(mci.getTheClass(), receiver, callInfo.name, false);
                if (res instanceof MethodMetaProperty) {
                    MethodMetaProperty mmp = (MethodMetaProperty) res;
                    callInfo.method = mmp.getMetaMethod();
                } else {
                    callInfo.property = res;
                } 
            }
        }

        /**
         * this method chooses a constructor from the meta class
         */
        private static void chooseConstructor(MetaClassImpl mci, CallInfo ci) {
            if (LOG_ENABLED) LOG.info("getting constructor");
            Object[] args = removeRealReceiver(ci.args);
            ci.method = mci.retrieveConstructor(args);
            if (ci.method instanceof MetaConstructor) {
                MetaConstructor mcon = (MetaConstructor) ci.method;
                if (mcon.isBeanConstructor()) {
                    if (LOG_ENABLED) LOG.info("do beans constructor");
                    ci.beanConstructor = true;
                }
            }
        }

        /**
         * Helper method to remove the receiver from the argument array
         * by producing a new array.
         */
        private static Object[] removeRealReceiver(Object[] args) {
            Object[] ar = new Object[args.length-1];
            for (int i=1; i<args.length; i++) {
                ar[i-1] = args[i];
            }
            return ar;
        }

        private static Object[] spread(Object[] args, boolean spreadCall) {
            if (!spreadCall) return args;
            Object[] normalArguments = (Object[]) args[1];
            Object[] ret = new Object[normalArguments.length+1];
            ret[0] = args[0];
            System.arraycopy(normalArguments, 0, ret, 1, ret.length-1);
            return ret;
        }
}
