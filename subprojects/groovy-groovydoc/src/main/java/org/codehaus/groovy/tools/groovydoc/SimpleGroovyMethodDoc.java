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
import org.codehaus.groovy.groovydoc.GroovyMethodDoc;
import org.codehaus.groovy.groovydoc.GroovyType;

/**
 * Default {@link GroovyMethodDoc} implementation.
 */
public class SimpleGroovyMethodDoc extends SimpleGroovyExecutableMemberDoc implements GroovyMethodDoc {
    private GroovyType returnType;
    private String typeParameters;

    /**
     * Creates a documented method owned by the supplied class.
     *
     * @param name the method name
     * @param belongsToClass the declaring class
     */
    public SimpleGroovyMethodDoc(String name, GroovyClassDoc belongsToClass) {
        super(name, belongsToClass);
    }

    /** {@inheritDoc} */
    @Override
    public GroovyType returnType() {
        return returnType;
    }

    // TODO need returnType.qualifiedTypeName() here

    /** {@inheritDoc} */
    @Override
    public void setReturnType(GroovyType returnType) {
        this.returnType = returnType;
    }

    /** {@inheritDoc} */
    @Override
    public GroovyClassDoc overriddenClass() {/*todo*/
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public GroovyMethodDoc overriddenMethod() {/*todo*/
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public GroovyType overriddenType() {/*todo*/
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean overrides(GroovyMethodDoc arg0) {/*todo*/
        return false;
    }

    /**
     * Returns the rendered type parameters for this method.
     *
     * @return the type parameter declaration text
     */
    public String typeParameters() {
        return typeParameters;
    }

    /**
     * Stores the rendered type parameters for this method.
     *
     * @param typeParameters the type parameter declaration text
     */
    public void setTypeParameters(String typeParameters) {
        this.typeParameters = typeParameters;
    }
}
