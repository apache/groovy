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

import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyFieldDoc;
import org.codehaus.groovy.groovydoc.GroovyType;

/**
 * Default {@link GroovyFieldDoc} implementation.
 */
public class SimpleGroovyFieldDoc extends SimpleGroovyMemberDoc implements GroovyFieldDoc {
    private GroovyType type;
    private String constantValueExpression;

    /**
     * Creates a documented field owned by the supplied class.
     *
     * @param name the field name
     * @param belongsToClass the declaring class
     */
    public SimpleGroovyFieldDoc(String name, GroovyClassDoc belongsToClass) {
        super(name, belongsToClass);
    }

    /** {@inheritDoc} */
    @Override
    public Object constantValue() {/*todo*/return null;}

    /**
     * Stores the source expression used to initialize this field's constant value.
     *
     * @param constantValueExpression the constant value expression
     */
    public void setConstantValueExpression(String constantValueExpression) {
        this.constantValueExpression = constantValueExpression;
    }

    /** {@inheritDoc} */
    @Override
    public String constantValueExpression() {
        return constantValueExpression;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isTransient() {/*todo*/return false;}

    /** {@inheritDoc} */
    @Override
    public boolean isVolatile() {/*todo*/return false;}

    //    public GroovySerialFieldTag[] serialFieldTags() {/*todo*/return null;}

    /** {@inheritDoc} */
    @Override
    public GroovyType type() {
        return type;
    }

    /**
     * Sets the resolved type for this field.
     *
     * @param type the field type
     */
    public void setType(GroovyType type) {
        this.type = type;
    }
}
