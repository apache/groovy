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
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.InvokerHelper;

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
        
        public static Object selectMethod(MutableCallSite callSite, Class sender, String methodName, Object receiver, Object[] arguments) {
            MetaClassImpl mc = null;
            if (receiver instanceof GroovyObject) {
                mc = (MetaClassImpl) ((GroovyObject) receiver).getMetaClass(); 
            } else {
                mc = (MetaClassImpl) InvokerHelper.getMetaClass(receiver);
            }
            MetaMethod m = mc.getMethodWithCaching(receiver.getClass(), methodName, arguments, false);
            MethodType targetType = callSite.type();
            if (m instanceof CachedMethod) {
                CachedMethod cm = (CachedMethod) m;
                MethodHandle mh;
                try {
                    mh = LOOKUP.unreflect(cm.getCachedMethod()).asType(targetType);
                } catch (IllegalAccessException e) {
                    throw new GroovyBugError(e);
                }
                callSite.setTarget(mh);
            } else {
                // receiver, args
                MethodHandle mh;
                try {
                    mh = LOOKUP.findVirtual(m.getClass(), "invoke", GENERAL_INVOKER_SIGNATURE);
                } catch (Exception e) {
                    throw new GroovyBugError(e);
                }
                mh = mh.bindTo(m).
                        asCollector(Object[].class, targetType.parameterCount()-1).
                        asType(targetType);
                callSite.setTarget(mh);
            }
            return m.invoke(receiver, arguments);
        }
}
