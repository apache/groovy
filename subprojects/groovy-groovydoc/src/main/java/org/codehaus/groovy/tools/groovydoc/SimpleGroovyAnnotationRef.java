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
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.runtime.StringGroovyMethods;

/**
 * Default {@link GroovyAnnotationRef} implementation backed by parsed source text.
 */
public class SimpleGroovyAnnotationRef implements GroovyAnnotationRef {
    private GroovyClassDoc type;
    private final String desc;
    private String name;
    /**
     * Creates an annotation reference from its simple name and raw source description.
     *
     * @param name the annotation name
     * @param desc the raw source description of the annotation
     */
    public SimpleGroovyAnnotationRef(String name, String desc) {
        this.name = name;
        final String params = StringGroovyMethods.minus(desc, "@" + name);
        this.desc = "()".equals(params) ? "" : params;
    }
    /**
     * Associates the resolved annotation type with this reference.
     *
     * @param type the resolved annotation type
     */
    public void setType(GroovyClassDoc type) {
        this.type = type;
    }
    /** {@inheritDoc} */
    @Override
    public GroovyClassDoc type() {
        return type;
    }
    /**
     * Indicates whether the annotation type has been resolved.
     *
     * @return {@code true} if the annotation type is available
     */
    public boolean isTypeAvailable() {
        return type != null;
    }
    /** {@inheritDoc} */
    @Override
    public String name() {
        return name;
    }
    /**
     * Replaces the annotation name used by this reference.
     *
     * @param name the annotation name to store
     */
    public void setName(String name) {
        this.name = name;
    }
    /** {@inheritDoc} */
    @Override
    public String description() {
        return desc;
    }
}
