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
package org.codehaus.groovy.ast;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents record component
 *
 * @since 4.0.0
 */
public class RecordComponentNode extends AnnotatedNode {
    private final String name;
    private final ClassNode type;

    public RecordComponentNode(ClassNode declaringClass, String name, ClassNode type) {
        this(declaringClass, name, type, Collections.emptyList());
    }

    public RecordComponentNode(ClassNode declaringClass, String name, ClassNode type, List<AnnotationNode> annotations) {
        this.name = name;
        this.type = type;
        setDeclaringClass(declaringClass);
        for (AnnotationNode annotationNode : annotations) {
            addAnnotation(annotationNode);
        }
    }

    public String getName() {
        return name;
    }

    public ClassNode getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecordComponentNode)) return false;
        RecordComponentNode that = (RecordComponentNode) o;
        return name.equals(that.name) && getDeclaringClass().equals(that.getDeclaringClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeclaringClass(), name);
    }
}
