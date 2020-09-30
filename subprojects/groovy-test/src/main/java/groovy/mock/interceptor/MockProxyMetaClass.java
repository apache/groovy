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

    public final boolean interceptConstruction;
    private boolean fallingThrough;

    static class FallThroughMarker extends Closure {
        public FallThroughMarker(Object owner) {
            super(owner);
        }
    }
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
