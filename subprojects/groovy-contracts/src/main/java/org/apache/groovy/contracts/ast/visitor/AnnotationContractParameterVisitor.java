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
package org.apache.groovy.contracts.ast.visitor;

import org.apache.groovy.contracts.util.AnnotationUtils;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;

/**
 * This {@link BaseVisitor} walks up the class hierarchy for the given {@link org.codehaus.groovy.ast.ClassNode}
 * and adds {@link org.apache.groovy.contracts.annotations.meta.ContractElement} annotations to method parameters.
 */
public class AnnotationContractParameterVisitor extends BaseVisitor {

    private MethodNode currentMethodNode;

    /**
     * Creates a visitor that scans inherited method parameters for contract annotations.
     *
     * @param sourceUnit the source unit currently being transformed
     * @param source the reader source backing the source unit
     */
    public AnnotationContractParameterVisitor(final SourceUnit sourceUnit, final ReaderSource source) {
        super(sourceUnit, source);
    }

    /**
     * Visits the class hierarchy and implemented interfaces of the supplied node.
     *
     * @param node the class to inspect
     */
    @Override
    public void visitClass(ClassNode node) {
        if (node == null) return;

        // walk up the class hierarchy
        super.visitClass(node.getSuperClass());

        // walk through all interfaces
        for (ClassNode i : node.getAllInterfaces()) {
            super.visitClass(i);
        }
    }

    /**
     * Tracks the currently visited method so parameter annotations can be resolved in context.
     *
     * @param node the method being visited
     */
    @Override
    public void visitMethod(MethodNode node) {
        currentMethodNode = node;
        super.visitMethod(node);
        currentMethodNode = null;
    }

    /**
     * Triggers contract-meta-annotation lookup for parameter annotations on the current method.
     *
     * @param node the annotated node being visited
     */
    @Override
    public void visitAnnotations(AnnotatedNode node) {
        if (!(node instanceof Parameter) || currentMethodNode == null) return;
        AnnotationUtils.hasMetaAnnotations(node, "org.apache.groovy.contracts.annotations.meta.ContractElement");
    }
}
