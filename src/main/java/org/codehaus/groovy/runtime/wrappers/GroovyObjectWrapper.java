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

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;

/**
 * Wraps an existing {@link GroovyObject} while constraining the type it should
 * present to the runtime.
 */
public class GroovyObjectWrapper extends Wrapper {
    /**
     * The wrapped Groovy object.
     */
    protected final GroovyObject wrapped;

    /**
     * Creates a wrapper for a Groovy object constrained to the supplied type.
     *
     * @param wrapped the wrapped Groovy object
     * @param constrainedType the type the wrapped object should report
     */
    public GroovyObjectWrapper(final GroovyObject wrapped, final Class constrainedType) {
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
     * Returns a property value from the wrapped Groovy object.
     *
     * @param property the property name
     * @return the resolved property value
     */
    @Override
    public Object getProperty(final String property) {
        return this.wrapped.getProperty(property);
    }

    /**
     * Invokes a method on the wrapped Groovy object.
     *
     * @param name the method name
     * @param args the invocation arguments
     * @return the invocation result
     */
    @Override
    public Object invokeMethod(final String name, final Object args) {
        return this.wrapped.invokeMethod(name, args);
    }

    /**
     * Updates the wrapped object's meta class.
     *
     * @param metaClass the new meta class
     */
    @Override
    public void setMetaClass(final MetaClass metaClass) {
        this.wrapped.setMetaClass(metaClass);
    }

    /**
     * Sets a property on the wrapped Groovy object.
     *
     * @param property the property name
     * @param newValue the new property value
     */
    @Override
    public void setProperty(final String property, final Object newValue) {
        this.wrapped.setProperty(property, newValue);
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
        return this.wrapped.getMetaClass();
    }
}
