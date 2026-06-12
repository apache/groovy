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
package groovy.mock.interceptor;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.GroovySystem;
import groovy.lang.Interceptor;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassRegistry;
import groovy.lang.PropertyAccessInterceptor;
import groovy.lang.ProxyMetaClass;

/**
 * The ProxyMetaClass for the MockInterceptor.
 * Instance and class methods are intercepted, but constructors are not to allow mocking of aggregated objects.
 */

public class MockProxyMetaClass extends ProxyMetaClass {

    /**
     * Indicates whether constructor invocations should be intercepted.
     */
    public final boolean interceptConstruction;
    private boolean fallingThrough;

    /**
     * Marker closure used to signal that a call should be delegated to the adaptee meta class.
     */
    static class FallThroughMarker extends Closure {
        /**
         * Creates a marker closure with the supplied owner.
         *
         * @param owner the closure owner
         */
        FallThroughMarker(Object owner) {
            super(owner);
        }
    }
    /**
     * Shared marker instance signalling that interception should fall through to the adaptee.
     */
    static final FallThroughMarker FALL_THROUGH_MARKER = new FallThroughMarker(new Object());

    /**
     * @param adaptee the MetaClass to decorate with interceptability
     */
    public MockProxyMetaClass(MetaClassRegistry registry, Class theClass, MetaClass adaptee) {
        this(registry, theClass, adaptee, false);
    }

    /**
     * @param adaptee the MetaClass to decorate with interceptability
     */
    public MockProxyMetaClass(MetaClassRegistry registry, Class theClass, MetaClass adaptee, boolean interceptConstruction) {
        super(registry, theClass, adaptee);
        this.interceptConstruction = interceptConstruction;
    }

    /**
     * convenience factory method for the most usual case.
     */
    public static MockProxyMetaClass make(Class theClass) {
        return make(theClass, false);
    }

    /**
     * convenience factory method allowing interceptConstruction to be set.
     */
    public static MockProxyMetaClass make(Class theClass, boolean interceptConstruction) {
        MetaClassRegistry metaRegistry = GroovySystem.getMetaClassRegistry();
        MetaClass meta = metaRegistry.getMetaClass(theClass);
        return new MockProxyMetaClass(metaRegistry, theClass, meta, interceptConstruction);
    }

    /**
     * Intercepts an instance method invocation and delegates to the adaptee when requested.
     *
     * @param object the receiver of the method call
     * @param methodName the method name
     * @param arguments the invocation arguments
     * @return the interceptor result or the adaptee result when falling through
     */
    @Override
    public Object invokeMethod(final Object object, final String methodName, final Object[] arguments) {
        if (null == interceptor && !fallingThrough) {
            throw new RuntimeException("cannot invoke method '" + methodName + "' without interceptor");
        }
        Object result = FALL_THROUGH_MARKER;
        if (interceptor != null) {
            result = interceptor.beforeInvoke(object, methodName, arguments);
        }
        if (result == FALL_THROUGH_MARKER) {
            Interceptor saved = interceptor;
            interceptor = null;
            boolean savedFallingThrough = fallingThrough;
            fallingThrough = true;
            result = adaptee.invokeMethod(object, methodName, arguments);
            fallingThrough = savedFallingThrough;
            interceptor = saved;
        }
        return result;
    }

    /**
     * Intercepts an instance method invocation with sender metadata and delegates when requested.
     *
     * @param sender the sender class
     * @param object the receiver of the method call
     * @param methodName the method name
     * @param arguments the invocation arguments
     * @param isCallToSuper whether the call targets a superclass implementation
     * @param fromInsideClass whether the call originates from inside the declaring class
     * @return the interceptor result or the adaptee result when falling through
     */
    @Override
    public Object invokeMethod(final Class sender, final Object object, final String methodName, final Object[] arguments, final boolean isCallToSuper, final boolean fromInsideClass) {
        if (null == interceptor && !fallingThrough) {
            throw new RuntimeException("cannot invoke method '" + methodName + "' without interceptor");
        }
        Object result = FALL_THROUGH_MARKER;
        if (interceptor != null) {
            result = interceptor.beforeInvoke(object, methodName, arguments);
        }
        if (result == FALL_THROUGH_MARKER) {
            Interceptor saved = interceptor;
            interceptor = null;
            boolean savedFallingThrough = fallingThrough;
            fallingThrough = true;
            result = adaptee.invokeMethod(sender, object, methodName, arguments, isCallToSuper, fromInsideClass);
            fallingThrough = savedFallingThrough;
            interceptor = saved;
        }
        return result;
    }

    /**
     * Intercepts a static method invocation and delegates to the adaptee when requested.
     *
     * @param object the static receiver
     * @param methodName the method name
     * @param arguments the invocation arguments
     * @return the interceptor result or the adaptee result when falling through
     */
    @Override
    public Object invokeStaticMethod(final Object object, final String methodName, final Object[] arguments) {
        if (null == interceptor && !fallingThrough) {
            throw new RuntimeException("cannot invoke static method '" + methodName + "' without interceptor");
        }
        Object result = FALL_THROUGH_MARKER;
        if (interceptor != null) {
            result = interceptor.beforeInvoke(object, methodName, arguments);
        }
        if (result == FALL_THROUGH_MARKER) {
            Interceptor saved = interceptor;
            interceptor = null;
            boolean savedFallingThrough = fallingThrough;
            fallingThrough = true;
            result = adaptee.invokeStaticMethod(object, methodName, arguments);
            fallingThrough = savedFallingThrough;
            interceptor = saved;
        }
        return result;
    }

    /**
     * Intercepts a property read and delegates to the adaptee when no custom value is supplied.
     *
     * @param aClass the dispatch class
     * @param object the receiver of the property access
     * @param property the property name
     * @param b implementation-specific flag forwarded to the adaptee
     * @param b1 implementation-specific flag forwarded to the adaptee
     * @return the intercepted or delegated property value
     */
    @Override
    public Object getProperty(Class aClass, Object object, String property, boolean b, boolean b1) {
        if (null == interceptor && !fallingThrough) {
            throw new RuntimeException("cannot get property '" + property + "' without interceptor");
        }
        Object result = FALL_THROUGH_MARKER;
        if (interceptor != null && interceptor instanceof PropertyAccessInterceptor) {
            result = ((PropertyAccessInterceptor) interceptor).beforeGet(object, property);
        }
        if (result == FALL_THROUGH_MARKER) {
            Interceptor saved = interceptor;
            interceptor = null;
            boolean savedFallingThrough = fallingThrough;
            fallingThrough = true;
            result = adaptee.getProperty(aClass, object, property, b, b1);
            fallingThrough = savedFallingThrough;
            interceptor = saved;
        }
        return result;
    }

    /**
     * Intercepts a property write and delegates to the adaptee when no custom handling is supplied.
     *
     * @param aClass the dispatch class
     * @param object the receiver of the property access
     * @param property the property name
     * @param newValue the value to assign
     * @param b implementation-specific flag forwarded to the adaptee
     * @param b1 implementation-specific flag forwarded to the adaptee
     */
    @Override
    public void setProperty(Class aClass, Object object, String property, Object newValue, boolean b, boolean b1) {
        if (null == interceptor && !fallingThrough) {
            throw new RuntimeException("cannot set property '" + property + "' without interceptor");
        }

        Object result = FALL_THROUGH_MARKER;
        if (interceptor != null && interceptor instanceof PropertyAccessInterceptor) {
            // cheat and borrow first param for result as we don't use it anyway
            Object[] resultHolder = new Object[1];
            ((PropertyAccessInterceptor) interceptor).beforeSet(resultHolder, property, newValue);
            result = resultHolder[0];
        }
        if (result == FALL_THROUGH_MARKER) {
            Interceptor saved = interceptor;
            interceptor = null;
            boolean savedFallingThrough = fallingThrough;
            fallingThrough = true;
            adaptee.setProperty(aClass, object, property, newValue, b, b1);
            fallingThrough = savedFallingThrough;
            interceptor = saved;
        }
    }

    /**
     * Unlike general impl in superclass, ctors are not intercepted but relayed
     * unless interceptConstruction is set.
     */
    /**
     * Intercepts or delegates constructor invocation depending on {@link #interceptConstruction}.
     *
     * @param arguments the constructor arguments
     * @return the constructed instance or interceptor-supplied replacement
     */
    @Override
    public Object invokeConstructor(final Object[] arguments) {
        if (interceptConstruction && null == interceptor)
            throw new RuntimeException("cannot invoke constructor without interceptor");

        if (interceptConstruction) {
            GroovyObject newInstance = (GroovyObject) interceptor.beforeInvoke(null, getTheClass().getSimpleName(), arguments);
            newInstance.setMetaClass(this);
            return newInstance;
        }

        return adaptee.invokeConstructor(arguments);
    }

}
