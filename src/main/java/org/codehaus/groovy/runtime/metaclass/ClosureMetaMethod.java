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
package org.codehaus.groovy.runtime.metaclass;

import groovy.lang.Closure;
import groovy.lang.ClosureInvokingMethod;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.MethodClosure;
import org.codehaus.groovy.runtime.NullObject;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * A MetaMethod that accepts a closure in the constructor which is invoked when the MetaMethod is called.
 * The delegate of the closure is set to the instance that the MetaMethod is invoked on when called.
 *
 * @since 1.5
 */
public class ClosureMetaMethod extends MetaMethod implements ClosureInvokingMethod {

    private final String name;
    private final Closure callable;
    private final CachedMethod doCall;
    private final CachedClass  declaringClass;

    public ClosureMetaMethod(String name, Closure c, CachedMethod doCall) {
        this(name, c.getOwner().getClass(), c, doCall);
    }

    public ClosureMetaMethod(String name, Class declaringClass, Closure c, CachedMethod doCall) {
        super(doCall.getNativeParameterTypes());
        this.name = name;
        this.callable = c;
        this.doCall = doCall;
        this.declaringClass = ReflectionCache.getCachedClass(declaringClass);
    }

    @Override
    public int getModifiers() {
        return Modifier.PUBLIC;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class getReturnType() {
        return Object.class;
    }

    @Override
    public CachedClass getDeclaringClass() {
        return declaringClass;
    }

    @Override
    public Object invoke(final Object object, final Object[] arguments) {
        Closure clone = (Closure) callable.clone();
        if (object == NullObject.getNullObject()) {
            clone.setDelegate(null); // GROOVY-6567
        } else {
            clone.setDelegate(object);
        }
        return doCall.invoke(clone, coerceArgumentsToClasses(arguments));
    }

    /**
     * Retrieves the closure that is invoked by this MetaMethod
     *
     * @return The closure
     */
    @Override
    public Closure getClosure() {
        return callable;
    }

    public static List<MetaMethod> createMethodList(final String name, final Class declaringClass, final Closure closure) {
        List<MetaMethod> mms = new ArrayList<>();
        if (closure instanceof MethodClosure) {
            for (CachedMethod method : ReflectionCache.getCachedClass(((MethodClosure) closure).getOwnerClass()).getMethods()) {
                if (method.getName().equals(((MethodClosure) closure).getMethod())) {
                    MetaMethod metaMethod = new MethodClosureMetaMethod(name, declaringClass, closure, method);
                    mms.add(adjustParamTypesForStdMethods(metaMethod, name));
                }
            }
        } else if (closure instanceof GeneratedClosure) {
            for (CachedMethod method : ReflectionCache.getCachedClass(closure.getClass()).getMethods()) {
                if ("doCall".equals(method.getName())) {
                    MetaMethod metaMethod = new ClosureMetaMethod(name, declaringClass, closure, method);
                    mms.add(adjustParamTypesForStdMethods(metaMethod, name));
                }
            }
        } else {
            MetaMethod metaMethod = new AnonymousMetaMethod(closure, name, declaringClass);
            mms.add(adjustParamTypesForStdMethods(metaMethod, name));
        }
        return mms;
    }

    private static MetaMethod adjustParamTypesForStdMethods(MetaMethod metaMethod, String methodName) {
        Class[] nativeParamTypes = metaMethod.getNativeParameterTypes();
        nativeParamTypes = (nativeParamTypes != null) ? nativeParamTypes : MetaClassHelper.EMPTY_TYPE_ARRAY;
        // for methodMissing, first parameter should be String type - to allow overriding of this method without
        // type String explicitly specified for first parameter (missing method name) - GROOVY-2951
        if("methodMissing".equals(methodName) && nativeParamTypes.length == 2 && nativeParamTypes[0] != String.class) {
            nativeParamTypes[0] = String.class;
        }
        return metaMethod;
    }
    public CachedMethod getDoCall() {
        return doCall;
    }

    public static ClosureMetaMethod copy(ClosureMetaMethod closureMethod) {
        if (closureMethod instanceof MethodClosureMetaMethod)
          return new MethodClosureMetaMethod(closureMethod.getName(), closureMethod.getDeclaringClass().getTheClass(), closureMethod.getClosure(), closureMethod.getDoCall());
        else
          return new ClosureMetaMethod(closureMethod.getName(), closureMethod.getDeclaringClass().getTheClass(), closureMethod.getClosure(), closureMethod.getDoCall());
    }

    private static class MethodClosureMetaMethod extends ClosureMetaMethod {
        public MethodClosureMetaMethod(String name, Class declaringClass, Closure closure, CachedMethod method) {
            super(name, declaringClass, closure, method);
        }

        @Override
        public Object invoke(Object object, Object[] arguments) {
            return getDoCall().invoke(getClosure().getOwner(), arguments);
        }
    }

    static class AnonymousMetaMethod extends MetaMethod {
        private final Closure closure;
        private final String name;
        private final Class declaringClass;

        public AnonymousMetaMethod(Closure closure, String name, Class declaringClass) {
            super(closure.getParameterTypes());
            this.closure = closure;
            this.name = name;
            this.declaringClass = declaringClass;
        }

        @Override
        public int getModifiers() {
            return Modifier.PUBLIC;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Class getReturnType() {
            return Object.class;
        }

        @Override
        public CachedClass getDeclaringClass() {
            return ReflectionCache.getCachedClass(declaringClass);
        }

        @Override
        public Object invoke(Object object, Object[] arguments) {
            Closure cloned = (Closure) closure.clone();
            cloned.setDelegate(object);
            arguments = coerceArgumentsToClasses(arguments);
            return InvokerHelper.invokeMethod(cloned, "call", arguments);
        }
    }
}
