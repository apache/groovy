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

import groovy.lang.GString;
import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.math.BigInteger;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.wrappers.Wrapper;

/**
 * Bytecode level interface for bootstrap methods used by invokedynamic.
 * 
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class IndyInterface {
    
        private static MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
        private static MethodHandle SELECT_METHOD;
        static {
            MethodType mt = MethodType.methodType(Object.class, MutableCallSite.class, Class.class, String.class, Object.class, Object[].class);
            try {
                SELECT_METHOD = LOOKUP.findStatic(IndyInterface.class, "selectMethod", mt);
            } catch (Exception e) {
                throw new GroovyBugError(e);
            }
        }
        private static MethodType GENERAL_INVOKER_SIGNATURE = MethodType.methodType(Object.class, Object.class, Object[].class);
        private static MethodType INVOKE_METHOD_SIGNATURE = MethodType.methodType(Object.class, String.class, Object[].class);
        private static MethodType O2O = MethodType.methodType(Object.class, Object.class);
        private static MethodHandle UNWRAP_METHOD, TO_STRING, TO_BYTE, TO_BIGINT;
        static {
            try {
                UNWRAP_METHOD = LOOKUP.findStatic(IndyInterface.class, "unwrap", O2O);
                TO_STRING = LOOKUP.findStatic(IndyInterface.class, "coerceToString", O2O);
                TO_BYTE = LOOKUP.findStatic(IndyInterface.class, "coerceToByte", O2O);
                TO_BIGINT = LOOKUP.findStatic(IndyInterface.class, "coerceToBigInt", O2O);
            } catch (Exception e) {
                throw new GroovyBugError(e);
            }
        }
        
        public static CallSite bootstrap(Lookup caller, String name, MethodType type) {
            // since indy does not give us the runtime types
            // we produce first a dummy call site, which then changes the target to one,
            // that does the method selection including the the direct call to the 
            // real method.
            MutableCallSite mc = new MutableCallSite(type);
            MethodHandle mh = SELECT_METHOD.
                                bindTo(mc).
                                bindTo(caller.lookupClass()).
                                bindTo(name).
                                asCollector(Object[].class, type.parameterCount()-1).
                                asType(type);
            mc.setTarget(mh);
            return mc;
        }
        
        private static MetaClass getMetaClass(Object receiver) {
            if (receiver instanceof GroovyObject) {
                return ((GroovyObject) receiver).getMetaClass(); 
            } else {
                return InvokerHelper.getMetaClass(receiver);
            }
        }
        
        private static class CallInfo {
            public Object[] args;
            public MetaMethod method;
            public MethodType targetType;
            public String methodName;
            public Object receiver;
            public MethodHandle handle;
            public boolean useMetaClass = false;
        }
        
        private static void setHandleForMetaMethod(CallInfo info) {
            if (info.method instanceof CachedMethod) {
                CachedMethod cm = (CachedMethod) info.method;
                try {
                    info.handle = LOOKUP.unreflect(cm.getCachedMethod());
                } catch (IllegalAccessException e) {
                    throw new GroovyBugError(e);
                }
            } else {
                // receiver, args
                try {
                    info.handle = LOOKUP.findVirtual(info.method.getClass(), "invoke", GENERAL_INVOKER_SIGNATURE);
                } catch (Exception e) {
                    throw new GroovyBugError(e);
                }
                info.handle = info.handle.bindTo(info.method).
                                asCollector(Object[].class, info.targetType.parameterCount()-1);
            }
        }
        
        private static void chooseMethod(MetaClass mc, CallInfo ci) {
            if (mc instanceof MetaClassImpl) {
                MetaClassImpl mci = (MetaClassImpl) mc;
                ci.method = mci.getMethodWithCaching(ci.receiver.getClass(), ci.methodName, ci.args, false);
            }
        }
        
        private static void setMetaClassCallHandleIfNedded(MetaClass mc, CallInfo ci) {
            if (ci.handle!=null) return;
            try {
                ci.useMetaClass = true;
                ci.handle = LOOKUP.findVirtual(mc.getClass(), "invokeMethod", INVOKE_METHOD_SIGNATURE);
            } catch (Exception e) {
                throw new GroovyBugError(e);
            }
            ci.handle = ci.handle.bindTo(mc).
                        asCollector(Object[].class, ci.targetType.parameterCount()-1);
        }
        
        /**
         * called by handle
         */
        public static Object unwrap(Object o) {
            Wrapper w = (Wrapper) o;
            return w.unwrap();
        }
        
        /**
         * called by handle
         */
        public static Object coerceToString(Object o) {
            return o.toString();
        }
        
        /**
         * called by handle
         */
        public static Object coerceToByte(Object o) {
            return new Byte(((Number) o).byteValue());
        }
        
        /**
         * called by handle
         */
        public static Object coerceToBigInt(Object o) {
            return new BigInteger(String.valueOf((Number) o));
        }
        
        private static void correctWrapping(CallInfo ci) {
            if (ci.useMetaClass) return;
            for (int i=0; i<ci.args.length; i++) {
                if (ci.args[i] instanceof Wrapper) { 
                    ci.handle = MethodHandles.filterArguments(ci.handle, i+1, UNWRAP_METHOD);
                }
            }
        }
        
        private static void correctCoerce(CallInfo ci) {
            if (ci.useMetaClass) return;
            Class[] parameters = ci.targetType.parameterArray();
            for (int i=0; i<ci.args.length; i++) {
                Class got = ci.args[i].getClass(); 
                if (got == GString.class && parameters[i+1] == String.class) {
                    ci.handle = MethodHandles.filterArguments(ci.handle, i+1, TO_STRING);                    
                } else if (parameters[i+1] == Byte.class && got != Byte.class) {
                    ci.handle = MethodHandles.filterArguments(ci.handle, i+1, TO_BYTE);
                } else if (parameters[i+1] == BigInteger.class && got != BigInteger.class) {
                    ci.handle = MethodHandles.filterArguments(ci.handle, i+1, TO_BIGINT);
                }
            }
        }
        
        public static Object selectMethod(MutableCallSite callSite, Class sender, String methodName, Object receiver, Object[] arguments) throws Throwable {
            CallInfo callInfo = new CallInfo();
            callInfo.targetType = callSite.type();
            callInfo.methodName = methodName;
            callInfo.receiver = receiver;
            callInfo.args = arguments;
            MetaClass mc = getMetaClass(receiver);
            chooseMethod(mc, callInfo);
            setHandleForMetaMethod(callInfo);
            setMetaClassCallHandleIfNedded(mc, callInfo);
            correctWrapping(callInfo);
            correctCoerce(callInfo);
            
            callInfo.handle = callInfo.handle.asType(callInfo.targetType);
            callSite.setTarget(callInfo.handle);
            
            MetaClassHelper.unwrap(arguments);
            return callInfo.handle.invokeWithArguments(repack(receiver, arguments));
        }
        
        private static Object[] repack(Object o, Object[] args) {
            Object[] ar = new Object[args.length+1];
            ar[0] = o;
            for (int i=0; i<args.length; i++) {
                ar[i+1] = args[i];
            }
            return ar;
        }
}
