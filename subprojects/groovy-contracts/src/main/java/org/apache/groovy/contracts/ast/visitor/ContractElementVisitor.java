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

import groovy.contracts.Contracted;
import org.apache.groovy.contracts.generation.CandidateChecks;
import org.apache.groovy.contracts.util.AnnotationUtils;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;

/**
 * Checks whether the given {@link org.codehaus.groovy.ast.ClassNode} is relevant for
 * further processing.
 */
public class ContractElementVisitor extends BaseVisitor implements ASTNodeMetaData {

    private ClassNode classNode;
    private boolean foundContractElement = false;

    /**
     * Creates a visitor that detects whether a class participates in contract processing.
     *
     * @param sourceUnit the source unit currently being transformed
     * @param source the reader source backing the source unit
     */
    public ContractElementVisitor(final SourceUnit sourceUnit, final ReaderSource source) {
        super(sourceUnit, source);
    }

    /**
     * Visits the supplied class and its hierarchy until any contract element is found.
     *
     * @param node the class to inspect
     */
    @Override
    public void visitClass(ClassNode node) {
        if (!CandidateChecks.isContractsCandidate(node) && !CandidateChecks.isInterfaceContractsCandidate(node)) return;

        classNode = node;

        // check for the @Contracted shortcut
        if (AnnotationUtils.hasAnnotationOfType(node, Contracted.class.getName())) {
            foundContractElement = true;
            return;
        }

        foundContractElement |= classNode.getNodeMetaData(CLOSURE_REPLACED) != null;

        if (!foundContractElement) {
            super.visitClass(node);
        }

        // check base classes
        if (!foundContractElement && node.getSuperClass() != null) {
            visitClass(node.getSuperClass());
        }

        // check interfaces
        if (!foundContractElement) {
            for (ClassNode interfaceNode : node.getInterfaces()) {
                visitClass(interfaceNode);
                if (foundContractElement) return;
            }
        }
    }

    /**
     * Checks whether the visited constructor or method already has rewritten contract metadata.
     *
     * @param methodNode the method or constructor being inspected
     * @param isConstructor whether the node represents a constructor
     */
    @Override
    protected void visitConstructorOrMethod(MethodNode methodNode, boolean isConstructor) {
        if (!CandidateChecks.couldBeContractElementMethodNode(classNode, methodNode) && !(CandidateChecks.isPreconditionCandidate(classNode, methodNode)))
            return;
        foundContractElement |= methodNode.getNodeMetaData(CLOSURE_REPLACED) != null;
    }

    /**
     * Indicates whether any contract element was detected while traversing the class hierarchy.
     *
     * @return {@code true} once a contract element has been found
     */
    public boolean isFoundContractElement() {
        return foundContractElement;
    }
}
