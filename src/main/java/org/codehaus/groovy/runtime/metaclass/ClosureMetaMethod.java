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

    /**
     * Constructs a new ClosureMetaMethod with the specified name and closure.
     * The declaring class is obtained from the closure's owner.
     *
     * @param name the name of the method
     * @param c the closure to invoke when this method is called
     * @param doCall the cached method representing the closure's doCall method
     */
    public ClosureMetaMethod(String name, Closure c, CachedMethod doCall) {
        this(name, c.getOwner().getClass(), c, doCall);
    }

    /**
     * Constructs a new ClosureMetaMethod with the specified name, declaring class, and closure.
     *
     * @param name the name of the method
     * @param declaringClass the class that declares this meta method
     * @param c the closure to invoke when this method is called
     * @param doCall the cached method representing the closure's doCall method
     */
    public ClosureMetaMethod(String name, Class declaringClass, Closure c, CachedMethod doCall) {
        super(doCall.getNativeParameterTypes());
        this.name = name;
        this.callable = c;
        this.doCall = doCall;
        this.declaringClass = ReflectionCache.getCachedClass(declaringClass);
    }

    /**
     * Returns the modifiers for this method.
     *
     * @return Modifier.PUBLIC
     */
    @Override
    public int getModifiers() {
        return Modifier.PUBLIC;
    }

    /**
     * Returns the name of this closure meta method.
     *
     * @return the name of the method
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the return type of this method.
     *
     * @return Object.class, as closures return Object
     */
    @Override
    public Class getReturnType() {
        return Object.class;
    }

    /**
     * Returns the cached class that declares this meta method.
     *
     * @return the declaring class
     */
    @Override
    public CachedClass getDeclaringClass() {
        return declaringClass;
    }

    /**
     * Invokes the closure with the given arguments. The closure's delegate is set to the
     * object on which this method is being invoked.
     *
     * @param object the object on which the method is invoked (becomes the closure's delegate)
     * @param arguments the arguments to pass to the closure
     * @return the result of the closure invocation
     */
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

    /**
     * Creates a list of MetaMethod instances from the given closure.
     * Handles MethodClosure, GeneratedClosure, and anonymous closures appropriately.
     *
     * @param name the name of the method
     * @param declaringClass the class that declares this meta method
     * @param closure the closure to create meta methods from
     * @return a list of MetaMethod instances
     */
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

    /**
     * Returns the cached method that represents the closure's doCall method.
     *
     * @return the cached doCall method
     */
    public CachedMethod getDoCall() {
        return doCall;
    }

    /**
     * Creates a copy of the given ClosureMetaMethod. If the method is a MethodClosureMetaMethod,
     * returns a new MethodClosureMetaMethod; otherwise returns a new ClosureMetaMethod.
     *
     * @param closureMethod the closure meta method to copy
     * @return a copy of the closure meta method
     */
    public static ClosureMetaMethod copy(ClosureMetaMethod closureMethod) {
        if (closureMethod instanceof MethodClosureMetaMethod)
          return new MethodClosureMetaMethod(closureMethod.getName(), closureMethod.getDeclaringClass().getTheClass(), closureMethod.getClosure(), closureMethod.getDoCall());
        else
          return new ClosureMetaMethod(closureMethod.getName(), closureMethod.getDeclaringClass().getTheClass(), closureMethod.getClosure(), closureMethod.getDoCall());
    }

    private static class MethodClosureMetaMethod extends ClosureMetaMethod {
        /**
         * Constructs a new MethodClosureMetaMethod with the specified name, declaring class, closure, and method.
         *
         * @param name the name of the method
         * @param declaringClass the class that declares this meta method
         * @param closure the closure to invoke when this method is called
         * @param method the cached method representing the closure's method
         */
        public MethodClosureMetaMethod(String name, Class declaringClass, Closure closure, CachedMethod method) {
            super(name, declaringClass, closure, method);
        }

        /**
         * Invokes the closure's underlying method directly on the closure's owner.
         *
         * @param object the object on which the method is invoked (unused)
         * @param arguments the method arguments
         * @return the result of the method invocation
         */
        @Override
        public Object invoke(Object object, Object[] arguments) {
            return getDoCall().invoke(getClosure().getOwner(), arguments);
        }
    }

    static class AnonymousMetaMethod extends MetaMethod implements ClosureInvokingMethod {
        private final Closure closure;
        private final String name;
        private final Class declaringClass;

        /**
         * Constructs a new AnonymousMetaMethod with the specified closure, name, and declaring class.
         *
         * @param closure the closure to invoke when this method is called
         * @param name the name of the method
         * @param declaringClass the class that declares this meta method
         */
        public AnonymousMetaMethod(Closure closure, String name, Class declaringClass) {
            super(closure.getParameterTypes());
            this.closure = closure;
            this.name = name;
            this.declaringClass = declaringClass;
        }

        /**
         * Returns the closure associated with this method.
         *
         * @return the closure
         */
        @Override
        public Closure getClosure() {
            return closure;
        }

        /**
         * Returns the modifiers for this method.
         *
         * @return Modifier.PUBLIC
         */
        @Override
        public int getModifiers() {
            return Modifier.PUBLIC;
        }

        /**
         * Returns the name of this meta method.
         *
         * @return the name of the method
         */
        @Override
        public String getName() {
            return name;
        }

        /**
         * Returns the return type of this method.
         *
         * @return Object.class, as closures return Object
         */
        @Override
        public Class getReturnType() {
            return Object.class;
        }

        /**
         * Returns the cached class that declares this meta method.
         *
         * @return the cached declaring class
         */
        @Override
        public CachedClass getDeclaringClass() {
            return ReflectionCache.getCachedClass(declaringClass);
        }

        /**
         * Invokes the closure with the given arguments. The closure's delegate is set to the
         * object on which this method is being invoked.
         *
         * @param object the object on which the method is invoked (becomes the closure's delegate)
         * @param arguments the arguments to pass to the closure
         * @return the result of the closure invocation
         */
        @Override
        public Object invoke(Object object, Object[] arguments) {
            Closure cloned = (Closure) closure.clone();
            cloned.setDelegate(object);
            arguments = coerceArgumentsToClasses(arguments);
            return InvokerHelper.invokeMethod(cloned, "call", arguments);
        }
    }
}
