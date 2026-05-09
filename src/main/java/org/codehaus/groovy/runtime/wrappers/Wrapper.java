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
 * Base class for runtime wrappers that expose a value through
 * {@link GroovyObject} while reporting a constrained type.
 */
public abstract class Wrapper implements GroovyObject {
    /**
     * The constrained type the wrapped value should present to the runtime.
     */
    protected final Class constrainedType;

    /**
     * Creates a wrapper for values that should appear as the supplied type.
     *
     * @param constrainedType the type the wrapped value should report
     */
    public Wrapper(final Class constrainedType) {
        this.constrainedType = constrainedType;
    }

    /**
     * Returns the {@link MetaClass} used to service GroovyObject operations for
     * the wrapped value.
     *
     * @return the delegated meta class
     */
    @Override
    public MetaClass getMetaClass() {
        return getDelegatedMetaClass();
    }

    /**
     * Returns the constrained type associated with this wrapper.
     *
     * @return the type the wrapped value should be treated as
     */
    public Class getType() {
        return this.constrainedType;
    }

    /**
     * Returns the wrapped value.
     *
     * @return the underlying value represented by this wrapper
     */
    public abstract Object unwrap();

    /**
     * Returns the object used as the delegation target for meta-class-based
     * operations.
     *
     * @return the underlying wrapped object
     */
    protected abstract Object getWrapped();

    /**
     * Returns the meta class that should handle GroovyObject operations for the
     * wrapped value.
     *
     * @return the delegated meta class
     */
    protected abstract MetaClass getDelegatedMetaClass();
}
