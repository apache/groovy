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
package org.codehaus.groovy.runtime.wrappers;

import groovy.lang.MetaClass;

/**
 * Wraps a plain Java object and routes GroovyObject operations through a
 * delegated {@link MetaClass}.
 */
public class PojoWrapper extends Wrapper {
    /**
     * The meta class used to dispatch GroovyObject operations to the wrapped
     * object.
     */
    protected MetaClass delegate;
    /**
     * The wrapped plain Java object.
     */
    protected final Object wrapped;

    /**
     * Creates a wrapper for a plain Java object constrained to the supplied
     * type.
     *
     * @param wrapped the wrapped object
     * @param constrainedType the type the wrapped object should report
     */
    public PojoWrapper(final Object wrapped, final Class constrainedType) {
        super(constrainedType);
        this.wrapped = wrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object unwrap() {
        return this.wrapped;
    }

    /**
     * Returns a property value from the wrapped object using the delegated meta
     * class.
     *
     * @param property the property name
     * @return the resolved property value
     */
    @Override
    public Object getProperty(final String property) {
        return this.delegate.getProperty(this.wrapped, property);
    }

    /**
     * Invokes a method on the wrapped object using the delegated meta class.
     *
     * @param methodName the method name
     * @param arguments the invocation arguments
     * @return the invocation result
     */
    @Override
    public Object invokeMethod(final String methodName, final Object arguments) {
        return this.delegate.invokeMethod(this.wrapped, methodName, arguments);
    }

    /**
     * Sets the meta class used to dispatch GroovyObject operations for the
     * wrapped object.
     *
     * @param metaClass the meta class to delegate to
     */
    @Override
    public void setMetaClass(final MetaClass metaClass) {
        this.delegate = metaClass;
    }

    /**
     * Sets a property on the wrapped object using the delegated meta class.
     *
     * @param property the property name
     * @param newValue the new property value
     */
    @Override
    public void setProperty(final String property, final Object newValue) {
        this.delegate.setProperty(this.wrapped, property, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object getWrapped() {
        return this.wrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MetaClass getDelegatedMetaClass() {
        return this.delegate;
    }
}
