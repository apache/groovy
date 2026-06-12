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
package org.codehaus.groovy.tools.groovydoc;

import org.codehaus.groovy.groovydoc.GroovyAnnotationRef;
import org.codehaus.groovy.groovydoc.GroovyParameter;
import org.codehaus.groovy.groovydoc.GroovyType;

import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link GroovyParameter} implementation.
 */
public class SimpleGroovyParameter implements GroovyParameter {
    private static final GroovyAnnotationRef[] EMPTY_GROOVYANNOTATIONREF_ARRAY = new GroovyAnnotationRef[0];
    private final String name;
    private String typeName;
    private String defaultValue;
    private GroovyType type;
    private boolean vararg;
    private final List<GroovyAnnotationRef> annotationRefs;

    /**
     * Creates a documented parameter with the supplied name.
     *
     * @param name the parameter name
     */
    public SimpleGroovyParameter(String name) {
        this.name = name;
        annotationRefs = new ArrayList<GroovyAnnotationRef>();
    }

    /** {@inheritDoc} */
    @Override
    public String defaultValue() {
        return defaultValue;
    }

    /**
     * Stores the default value expression declared for this parameter.
     *
     * @param defaultValue the default value expression
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public String typeName() {
        if (type == null) {
            return typeName;
        }
        return type.simpleTypeName();
    }

    /**
     * Stores the unresolved type name declared for this parameter.
     *
     * @param typeName the declared type name
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /** {@inheritDoc} */
    @Override
    public GroovyAnnotationRef[] annotations() {
        return annotationRefs.toArray(EMPTY_GROOVYANNOTATIONREF_ARRAY);
    }

    /**
     * Adds an annotation reference declared on this parameter.
     *
     * @param ref the annotation reference to add
     */
    public void addAnnotationRef(GroovyAnnotationRef ref) {
        annotationRefs.add(ref);
    }

    /** {@inheritDoc} */
    @Override
    public GroovyType type() {
        return type;
    }

    /**
     * Sets the resolved parameter type for later class resolution.
     *
     * @param type the resolved parameter type
     */
    public void setType(GroovyType type) {
        this.type = type;
    }

    /**
     * Indicates whether the parameter type has been resolved.
     *
     * @return {@code true} if the parameter type is available
     */
    public boolean isTypeAvailable() {
        return !(type == null);
    }

    /**
     * Indicates whether this parameter is variadic.
     *
     * @return {@code true} if this parameter is variadic
     */
    public boolean vararg() {
        return vararg;
    }

    /**
     * Sets whether this parameter is variadic.
     *
     * @param vararg {@code true} if this parameter is variadic
     */
    public void setVararg(boolean vararg) {
        this.vararg = vararg;
    }
}
