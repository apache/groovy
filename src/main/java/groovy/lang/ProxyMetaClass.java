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
package groovy.lang;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * As subclass of MetaClass, ProxyMetaClass manages calls from Groovy Objects to POJOs.
 * It enriches MetaClass with the feature of making method invocations interceptable by
 * an Interceptor. To this end, it acts as a decorator (decorator pattern) allowing
 * to add or withdraw this feature at runtime.
 * See groovy/lang/InterceptorTest.groovy for details.
 * <p>
 * WARNING: This implementation of ProxyMetaClass is NOT thread-safe and hence should only be used for
 * as a per-instance MetaClass running in a single thread. Do not place this MetaClass in the MetaClassRegistry
 * as it will result in unpredictable behaviour
 *
 * @see groovy.lang.MetaClassRegistry
 */
public class ProxyMetaClass extends MetaClassImpl implements AdaptingMetaClass {

    protected MetaClass adaptee;
    protected Interceptor interceptor;

    /**
     * convenience factory method for the most usual case.
     */
    public static ProxyMetaClass getInstance(final Class theClass) {
        MetaClassRegistry metaRegistry = GroovySystem.getMetaClassRegistry();
        MetaClass meta = metaRegistry.getMetaClass(theClass);
        return new ProxyMetaClass(metaRegistry, theClass, meta);
    }

    /**
     * @param adaptee the MetaClass to decorate with interceptability
     */
    public ProxyMetaClass(final MetaClassRegistry registry, final Class theClass, final MetaClass adaptee) {
        super(registry, theClass);
        this.adaptee = Objects.requireNonNull(adaptee, "adaptee must not be null");
        super.initialize();
    }

    @Override
    public synchronized void initialize() {
        this.adaptee.initialize();
    }

    @Override
    public MetaClass getAdaptee() {
        return this.adaptee;
    }

    @Override
    public void setAdaptee(final MetaClass metaClass) {
        this.adaptee = metaClass;
    }

    /**
     * @return the interceptor in use or null if no interceptor is used
     */
    public Interceptor getInterceptor() {
        return interceptor;
    }

    /**
     * @param interceptor may be null to reset any interception
     */
    public void setInterceptor(final Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    /**
     * Use the ProxyMetaClass for the given Closure.
     * Cares for balanced register/unregister.
     *
     * @param closure piece of code to be executed with registered ProxyMetaClass
     */
    public Object use(final Closure closure) {
        // grab existing meta (usually adaptee but we may have nested use calls)
        MetaClass origMetaClass = registry.getMetaClass(theClass);
        registry.setMetaClass(theClass, this);
        try {
            return closure.call();
        } finally {
            registry.setMetaClass(theClass, origMetaClass);
        }
    }

    /**
     * Use the ProxyMetaClass for the given Closure.
     * Cares for balanced setting/unsetting ProxyMetaClass.
     *
     * @param closure piece of code to be executed with ProxyMetaClass
     */
    public Object use(final GroovyObject object, final Closure closure) {
        // grab existing meta (usually adaptee but we may have nested use calls)
        MetaClass origMetaClass = object.getMetaClass();
        object.setMetaClass(this);
        try {
            return closure.call();
        } finally {
            object.setMetaClass(origMetaClass);
        }
    }

    /**
     * Call invokeMethod on adaptee with logic like in MetaClass unless we have an Interceptor.
     * With Interceptor the call is nested in its beforeInvoke and afterInvoke methods.
     * The method call is suppressed if Interceptor.doInvoke() returns false.
     * See Interceptor for details.
     */
    @Override
    public Object invokeMethod(final Object object, final String methodName, final Object[] arguments) {
        return doCall(object, methodName, arguments, interceptor, () -> adaptee.invokeMethod(object, methodName, arguments));
    }

    /**
     * Call invokeMethod on adaptee with logic like in MetaClass unless we have an Interceptor.
     * With Interceptor the call is nested in its beforeInvoke and afterInvoke methods.
     * The method call is suppressed if Interceptor.doInvoke() returns false.
     * See Interceptor for details.
     */
    @Override
    public Object invokeMethod(final Class sender, final Object object, final String methodName, final Object[] arguments, final boolean isCallToSuper, final boolean fromInsideClass) {
        return doCall(object, methodName, arguments, interceptor, () -> adaptee.invokeMethod(sender, object, methodName, arguments, isCallToSuper, fromInsideClass));
    }

    /**
     * Call invokeStaticMethod on adaptee with logic like in MetaClass unless we have an Interceptor.
     * With Interceptor the call is nested in its beforeInvoke and afterInvoke methods.
     * The method call is suppressed if Interceptor.doInvoke() returns false.
     * See Interceptor for details.
     */
    @Override
    public Object invokeStaticMethod(final Object object, final String methodName, final Object[] arguments) {
        return doCall(object, methodName, arguments, interceptor, () -> adaptee.invokeStaticMethod(object, methodName, arguments));
    }

    /**
     * Call invokeConstructor on adaptee with logic like in MetaClass unless we have an Interceptor.
     * With Interceptor the call is nested in its beforeInvoke and afterInvoke methods.
     * The method call is suppressed if Interceptor.doInvoke() returns false.
     * See Interceptor for details.
     */
    @Override
    public Object invokeConstructor(final Object[] arguments) {
        return doCall(theClass, "ctor", arguments, interceptor, () -> adaptee.invokeConstructor(arguments));
    }

    /**
     * Interceptors the call to getProperty if a PropertyAccessInterceptor is
     * available
     *
     * @param object   the object to invoke the getter on
     * @param property the property name
     * @return the value of the property
     */
    public Object getProperty(final Class aClass, final Object object, final String property, final boolean useSuper, final boolean fromInsideClass) {
        if (null == interceptor) {
            return super.getProperty(aClass, object, property, useSuper, fromInsideClass);
        }
        if (interceptor instanceof PropertyAccessInterceptor) {
            PropertyAccessInterceptor pae = (PropertyAccessInterceptor) interceptor;

            Object result = pae.beforeGet(object, property);
            if (interceptor.doInvoke()) {
                result = super.getProperty(aClass, object, property, useSuper, fromInsideClass);
            }
            return result;
        }
        return super.getProperty(aClass, object, property, useSuper, fromInsideClass);
    }

    /**
     * Interceptors the call to a property setter if a PropertyAccessInterceptor
     * is available
     *
     * @param object   The object to invoke the setter on
     * @param property The property name to set
     * @param newValue The new value of the property
     */
    public void setProperty(final Class aClass, final Object object, final String property, final Object newValue, final boolean useSuper, final boolean fromInsideClass) {
        if (null == interceptor) {
            super.setProperty(aClass, object, property, newValue, useSuper, fromInsideClass);
        }
        if (interceptor instanceof PropertyAccessInterceptor) {
            PropertyAccessInterceptor pae = (PropertyAccessInterceptor) interceptor;

            pae.beforeSet(object, property, newValue);
            if (interceptor.doInvoke()) {
                super.setProperty(aClass, object, property, newValue, useSuper, fromInsideClass);
            }
        } else {
            super.setProperty(aClass, object, property, newValue, useSuper, fromInsideClass);
        }
    }

    private Object doCall(final Object object, final String methodName, final Object[] arguments, final Interceptor interceptor, final Supplier<Object> howToInvoke) {
        if (interceptor == null) {
            return howToInvoke.get();
        }
        Object result = interceptor.beforeInvoke(object, methodName, arguments);
        if (interceptor.doInvoke()) {
            result = howToInvoke.get();
        }
        result = interceptor.afterInvoke(object, methodName, arguments, result);
        return result;
    }
}
