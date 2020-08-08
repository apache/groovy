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
package org.apache.groovy.contracts.ast;

import groovy.contracts.Contracted;
import groovy.contracts.Ensures;
import groovy.contracts.Invariant;
import groovy.contracts.Requires;
import org.apache.groovy.contracts.ast.visitor.AnnotationProcessorVisitor;
import org.apache.groovy.contracts.ast.visitor.DomainModelInjectionVisitor;
import org.apache.groovy.contracts.ast.visitor.DynamicSetterInjectionVisitor;
import org.apache.groovy.contracts.ast.visitor.LifecycleAfterTransformationVisitor;
import org.apache.groovy.contracts.ast.visitor.LifecycleBeforeTransformationVisitor;
import org.apache.groovy.contracts.common.spi.ProcessingContextInformation;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.transform.GroovyASTTransformation;

/**
 * <p>
 * Custom AST transformation that removes closure annotations of {@link Invariant},
 * {@link Requires} and {@link Ensures} and adds Java
 * assertions executing the closure-code.
 * </p>
 * <p>
 * Whenever an assertion is broken an {@link org.apache.groovy.contracts.AssertionViolation} descendant class will be thrown.
 * </p>
 *
 * @see org.apache.groovy.contracts.PreconditionViolation
 * @see org.apache.groovy.contracts.PostconditionViolation
 * @see org.apache.groovy.contracts.ClassInvariantViolation
 */
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
public class GContractsASTTransformation extends BaseASTTransformation {

    /**
     * {@link org.codehaus.groovy.transform.ASTTransformation#visit(org.codehaus.groovy.ast.ASTNode[], org.codehaus.groovy.control.SourceUnit)}
     */
    public void visit(ASTNode[] nodes, SourceUnit unit) {
        final ModuleNode moduleNode = unit.getAST();

        ReaderSource source = getReaderSource(unit);
        final ClassNode contractedAnnotationClassNode = ClassHelper.makeWithoutCaching(Contracted.class);

        for (final ClassNode classNode : moduleNode.getClasses()) {
            if (classNode.getAnnotations(contractedAnnotationClassNode).isEmpty()) continue;

            final ProcessingContextInformation pci = new ProcessingContextInformation(classNode, unit, source);
            new LifecycleBeforeTransformationVisitor(unit, source, pci).visitClass(classNode);
            new AnnotationProcessorVisitor(unit, source, pci).visitClass(classNode);
            new DomainModelInjectionVisitor(unit, source, pci).visitClass(classNode);
            new LifecycleAfterTransformationVisitor(unit, source, pci).visitClass(classNode);
            new DynamicSetterInjectionVisitor(unit, source).visitClass(classNode);
        }
    }
}

