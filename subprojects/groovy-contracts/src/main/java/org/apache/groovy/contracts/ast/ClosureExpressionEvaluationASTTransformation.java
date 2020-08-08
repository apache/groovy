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
import org.apache.groovy.contracts.ast.visitor.AnnotationClosureVisitor;
import org.apache.groovy.contracts.ast.visitor.ConfigurationSetup;
import org.apache.groovy.contracts.ast.visitor.ContractElementVisitor;
import org.apache.groovy.contracts.generation.CandidateChecks;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates {@link org.codehaus.groovy.ast.expr.ClosureExpression} instances in as actual annotation parameters and
 * generates special contract closure classes from them.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class ClosureExpressionEvaluationASTTransformation extends BaseASTTransformation {

    private void generateAnnotationClosureClasses(SourceUnit unit, ReaderSource source, List<ClassNode> classNodes) {
        final AnnotationClosureVisitor annotationClosureVisitor = new AnnotationClosureVisitor(unit, source);

        for (final ClassNode classNode : classNodes) {
            annotationClosureVisitor.visitClass(classNode);

            if (!CandidateChecks.isContractsCandidate(classNode)) continue;

            final ContractElementVisitor contractElementVisitor = new ContractElementVisitor(unit, source);
            contractElementVisitor.visitClass(classNode);

            if (!contractElementVisitor.isFoundContractElement()) continue;

            annotationClosureVisitor.visitClass(classNode);
            markClassNodeAsContracted(classNode);

            new ConfigurationSetup().init(classNode);
        }
    }

    /**
     * {@link org.codehaus.groovy.transform.ASTTransformation#visit(org.codehaus.groovy.ast.ASTNode[], org.codehaus.groovy.control.SourceUnit)}
     */
    public void visit(ASTNode[] nodes, SourceUnit unit) {
        final ModuleNode moduleNode = unit.getAST();

        ReaderSource source = getReaderSource(unit);
        final List<ClassNode> classNodes = new ArrayList<ClassNode>(moduleNode.getClasses());

        generateAnnotationClosureClasses(unit, source, classNodes);
    }

    private void markClassNodeAsContracted(final ClassNode classNode) {
        final ClassNode contractedAnnotationClassNode = ClassHelper.makeWithoutCaching(Contracted.class);

        if (classNode.getAnnotations(contractedAnnotationClassNode).isEmpty())
            classNode.addAnnotation(new AnnotationNode(contractedAnnotationClassNode));
    }
}
