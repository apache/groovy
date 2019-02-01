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
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.control.SourceUnit;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A visitor used as a callback to {@link org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor#existsProperty(org.codehaus.groovy.ast.expr.PropertyExpression, boolean, org.codehaus.groovy.ast.ClassCodeVisitorSupport)}
 * which will return set the type of the found property in the provided reference.
 */
class PropertyLookupVisitor extends ClassCodeVisitorSupport {
    private final AtomicReference<ClassNode> result;

    public PropertyLookupVisitor(final AtomicReference<ClassNode> result) {
        this.result = result;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }

    @Override
    public void visitMethod(final MethodNode node) {
        result.set(node.getReturnType());
    }

    @Override
    public void visitProperty(final PropertyNode node) {
        result.set(node.getType());
    }

    @Override
    public void visitField(final FieldNode field) {
        result.set(field.getType());
    }
}
