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

public class SimpleGroovyParameter implements GroovyParameter {
    private static final GroovyAnnotationRef[] EMPTY_GROOVYANNOTATIONREF_ARRAY = new GroovyAnnotationRef[0];
    private final String name;
    private String typeName;
    private String defaultValue;
    private GroovyType type;
    private boolean vararg;
    private final List<GroovyAnnotationRef> annotationRefs;

    public SimpleGroovyParameter(String name) {
        this.name = name;
        annotationRefs = new ArrayList<>();
    }

    public String defaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String name() {
        return name;
    }

    public String typeName() {
        if (type == null) {
            return typeName;
        }
        return type.simpleTypeName();
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public GroovyAnnotationRef[] annotations() {
        return annotationRefs.toArray(EMPTY_GROOVYANNOTATIONREF_ARRAY);
    }

    public void addAnnotationRef(GroovyAnnotationRef ref) {
        annotationRefs.add(ref);
    }

    public GroovyType type() {
        return type;
    }

    /* for later class resolution */
    public void setType(GroovyType type) {
        this.type = type;
    }

    public boolean isTypeAvailable() {
        return !(type == null);
    }

    public boolean vararg() {
        return vararg;
    }

    public void setVararg(boolean vararg) {
        this.vararg = vararg;
    }
}
