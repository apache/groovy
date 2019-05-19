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

    protected MetaClass adaptee = null;
    protected Interceptor interceptor = null;


    /**
     * convenience factory method for the most usual case.
     */
    public static ProxyMetaClass getInstance(Class theClass) {
        MetaClassRegistry metaRegistry = GroovySystem.getMetaClassRegistry();
        MetaClass meta = metaRegistry.getMetaClass(theClass);
        return new ProxyMetaClass(metaRegistry, theClass, meta);
    }

    /**
     * @param adaptee the MetaClass to decorate with interceptability
     */
    public ProxyMetaClass(MetaClassRegistry registry, Class theClass, MetaClass adaptee) {
        super(registry, theClass);
        this.adaptee = adaptee;
        if (null == adaptee) throw new IllegalArgumentException("adaptee must not be null");
        super.initialize();
    }

    public synchronized void initialize() {
        this.adaptee.initialize();
    }

    /**
     * Use the ProxyMetaClass for the given Closure.
     * Cares for balanced register/unregister.
     *
     * @param closure piece of code to be executed with registered ProxyMetaClass
     */
    public Object use(Closure closure) {
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
    public Object use(GroovyObject object, Closure closure) {
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
     * @return the interceptor in use or null if no interceptor is used
     */
    public Interceptor getInterceptor() {
        return interceptor;
    }

    /**
     * @param interceptor may be null to reset any interception
     */
    public void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    /**
     * Call invokeMethod on adaptee with logic like in MetaClass unless we have an Interceptor.
     * With Interceptor the call is nested in its beforeInvoke and afterInvoke methods.
     * The method call is suppressed if Interceptor.doInvoke() returns false.
     * See Interceptor for details.
     */
    public Object invokeMethod(final Object object, final String methodName, final Object[] arguments) {
        return doCall(object, methodName, arguments, interceptor, new Callable() {
            public Object call() {
                return adaptee.invokeMethod(object, methodName, arguments);
            }
        });
    }

    /**
     * Call invokeMethod on adaptee with logic like in MetaClass unless we have an Interceptor.
     * With Interceptor the call is nested in its beforeInvoke and afterInvoke methods.
     * The method call is suppressed if Interceptor.doInvoke() returns false.
     * See Interceptor for details.
     */
    @Override
    public Object invokeMethod(final Class sender, final Object object, final String methodName, final Object[] arguments, final boolean isCallToSuper, final boolean fromInsideClass) {
        return doCall(object, methodName, arguments, interceptor, new Callable() {
            public Object call() {
                return adaptee.invokeMethod(sender, object, methodName, arguments, isCallToSuper, fromInsideClass);
            }
        });
    }

    /**
     * Call invokeStaticMethod on adaptee with logic like in MetaClass unless we have an Interceptor.
     * With Interceptor the call is nested in its beforeInvoke and afterInvoke methods.
     * The method call is suppressed if Interceptor.doInvoke() returns false.
     * See Interceptor for details.
     */
    public Object invokeStaticMethod(final Object object, final String methodName, final Object[] arguments) {
        return doCall(object, methodName, arguments, interceptor, new Callable() {
            public Object call() {
                return adaptee.invokeStaticMethod(object, methodName, arguments);
            }
        });
    }

    /**
     * Call invokeConstructor on adaptee with logic like in MetaClass unless we have an Interceptor.
     * With Interceptor the call is nested in its beforeInvoke and afterInvoke methods.
     * The method call is suppressed if Interceptor.doInvoke() returns false.
     * See Interceptor for details.
     */
    public Object invokeConstructor(final Object[] arguments) {
        return doCall(theClass, "ctor", arguments, interceptor, new Callable() {
            public Object call() {
                return adaptee.invokeConstructor(arguments);
            }
        });
    }

    /**
     * Interceptors the call to getProperty if a PropertyAccessInterceptor is
     * available
     *
     * @param object   the object to invoke the getter on
     * @param property the property name
     * @return the value of the property
     */
    public Object getProperty(Class aClass, Object object, String property, boolean b, boolean b1) {
        if (null == interceptor) {
            return super.getProperty(aClass, object, property, b, b1);
        }
        if (interceptor instanceof PropertyAccessInterceptor) {
            PropertyAccessInterceptor pae = (PropertyAccessInterceptor) interceptor;

            Object result = pae.beforeGet(object, property);
            if (interceptor.doInvoke()) {
                result = super.getProperty(aClass, object, property, b, b1);
            }
            return result;
        }
        return super.getProperty(aClass, object, property, b, b1);
    }

    /**
     * Interceptors the call to a property setter if a PropertyAccessInterceptor
     * is available
     *
     * @param object   The object to invoke the setter on
     * @param property The property name to set
     * @param newValue The new value of the property
     */
    public void setProperty(Class aClass, Object object, String property, Object newValue, boolean b, boolean b1) {
        if (null == interceptor) {
            super.setProperty(aClass, object, property, newValue, b, b1);
        }
        if (interceptor instanceof PropertyAccessInterceptor) {
            PropertyAccessInterceptor pae = (PropertyAccessInterceptor) interceptor;

            pae.beforeSet(object, property, newValue);
            if (interceptor.doInvoke()) {
                super.setProperty(aClass, object, property, newValue, b, b1);
            }
        } else {
            super.setProperty(aClass, object, property, newValue, b, b1);
        }
    }

    public MetaClass getAdaptee() {
        return this.adaptee;
    }

    public void setAdaptee(MetaClass metaClass) {
        this.adaptee = metaClass;
    }

    // since Java has no Closures...
    private interface Callable {
        Object call();
    }

    private Object doCall(Object object, String methodName, Object[] arguments, Interceptor interceptor, Callable howToInvoke) {
        if (null == interceptor) {
            return howToInvoke.call();
        }
        Object result = interceptor.beforeInvoke(object, methodName, arguments);
        if (interceptor.doInvoke()) {
            result = howToInvoke.call();
        }
        result = interceptor.afterInvoke(object, methodName, arguments, result);
        return result;
    }
}
