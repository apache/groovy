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
import groovy.contracts.Decreases;
import groovy.contracts.Invariant;
import groovy.contracts.ThrowsIf;
import org.apache.groovy.contracts.annotations.meta.ContractElement;
import org.apache.groovy.contracts.ast.visitor.AnnotationClosureVisitor;
import org.apache.groovy.contracts.ast.visitor.ConfigurationSetup;
import org.apache.groovy.contracts.ast.visitor.ContractElementVisitor;
import org.apache.groovy.contracts.generation.CandidateChecks;
import org.apache.groovy.contracts.util.AnnotationUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.codehaus.groovy.transform.trait.Traits;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates {@link ClosureExpression} instances in annotation parameters and
 * generates special contract closure classes from them.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class ClosureExpressionEvaluationASTTransformation extends BaseASTTransformation {

    private static final ClassNode THROWS_IF_TYPE = ClassHelper.make(ThrowsIf.class);
    private static final ClassNode DECREASES_TYPE = ClassHelper.make(Decreases.class);

    /**
     * {@link ASTTransformation#visit(ASTNode[], SourceUnit)}
     */
    @Override
    public void visit(ASTNode[] nodes, SourceUnit unit) {
        final ModuleNode moduleNode = unit.getAST();

        promoteImportInvariantsToScriptClass(moduleNode);

        ReaderSource source = getReaderSource(unit);
        final List<ClassNode> classNodes = new ArrayList<>(moduleNode.getClasses());

        rejectNonClosureConditions(unit, classNodes);
        rejectContractsOnTraits(unit, classNodes);
        generateAnnotationClosureClasses(unit, source, classNodes);
    }

    /**
     * Reports a contract annotation whose value is not a closure. The annotation type declares its value as a
     * {@code Class}, so a class literal passes the annotation checks, but later phases would take it for the
     * condition class generated from a closure: it was never checked, or failed compilation when the contract
     * was inherited. A condition already replaced by its generated class, when another source unit visited
     * this class as a supertype first, is recognised by its metadata.
     */
    private static void rejectNonClosureConditions(final SourceUnit unit, final List<ClassNode> classNodes) {
        for (ClassNode classNode : classNodes) {
            rejectNonClosureConditions(unit, classNode);
            for (MethodNode methodNode : classNode.getMethods()) {
                rejectNonClosureConditions(unit, methodNode);
            }
            for (MethodNode constructorNode : classNode.getDeclaredConstructors()) {
                rejectNonClosureConditions(unit, constructorNode);
            }
        }
    }

    private static void rejectNonClosureConditions(final SourceUnit unit, final AnnotatedNode node) {
        for (AnnotationNode annotation : AnnotationUtils.hasMetaAnnotations(node, ContractElement.class.getName())) {
            Expression value = annotation.getMember("value");
            if (value == null || value instanceof ClosureExpression
                    || value.getNodeMetaData(AnnotationClosureVisitor.META_DATA_ORIGINAL_TRY_CATCH_BLOCK) != null) continue;
            String name = annotation.getClassNode().getNameWithoutPackage();
            unit.addError(new SyntaxException(
                    "@" + name + " requires a closure containing the condition, e.g. @" + name + "({ ... }); found "
                            + (value instanceof ClassExpression ? "a class " : "") + value.getText(),
                    annotation.getLineNumber(), annotation.getColumnNumber()));
        }
    }

    /**
     * Reports a contract annotation on a trait, or on a trait method that has a body. The trait transform moves
     * such a method into the trait's helper class as a static method with a leading {@code $self} parameter,
     * so the condition class generated here no longer matched the method it was applied to: the compiler failed
     * with an index out of bounds error, or the generated check failed at run time. A contract on an abstract
     * trait method is kept; like an interface contract, it applies to the implementing class. {@link ThrowsIf} and
     * {@link Decreases} on a trait method with a body are reported too: their transforms run after the trait
     * transform has moved the method into the helper class and rewritten its body to reach trait state through
     * {@code $self}, and the guard or measure woven there is not rewritten, so a condition that refers to trait
     * state fails at run time with a missing property on the helper class. On an abstract trait method they stay
     * informational, as on any abstract method.
     */
    private static void rejectContractsOnTraits(final SourceUnit unit, final List<ClassNode> classNodes) {
        for (ClassNode classNode : classNodes) {
            if (!Traits.isTrait(classNode)) continue;
            for (AnnotationNode annotation : AnnotationUtils.hasMetaAnnotations(classNode, ContractElement.class.getName())) {
                unit.addError(new SyntaxException(
                        "[groovy-contracts] @" + annotation.getClassNode().getNameWithoutPackage()
                                + " is not supported on a trait (" + classNode.getName() + ").",
                        annotation.getLineNumber(), annotation.getColumnNumber()));
            }
            for (MethodNode methodNode : classNode.getMethods()) {
                if (methodNode.isAbstract()) continue;
                for (AnnotationNode annotation : AnnotationUtils.hasMetaAnnotations(methodNode, ContractElement.class.getName())) {
                    unit.addError(new SyntaxException(
                            "[groovy-contracts] @" + annotation.getClassNode().getNameWithoutPackage()
                                    + " is not supported on a trait method with a body (" + classNode.getName() + "." + methodNode.getName()
                                    + "); only an abstract trait method may carry a contract.",
                            annotation.getLineNumber(), annotation.getColumnNumber()));
                }
                List<AnnotationNode> guards = new ArrayList<>(methodNode.getAnnotations(THROWS_IF_TYPE));
                guards.addAll(methodNode.getAnnotations(DECREASES_TYPE));
                for (AnnotationNode annotation : guards) {
                    unit.addError(new SyntaxException(
                            "[groovy-contracts] @" + annotation.getClassNode().getNameWithoutPackage()
                                    + " is not supported on a trait method with a body (" + classNode.getName() + "." + methodNode.getName() + ").",
                            annotation.getLineNumber(), annotation.getColumnNumber()));
                }
            }
        }
    }

    /**
     * Promotes {@link Invariant} annotations found on import statements to the script body class,
     * allowing {@code @Invariant} on an import to act as a class invariant for the script.
     */
    private static void promoteImportInvariantsToScriptClass(final ModuleNode moduleNode) {
        final String invariantName = Invariant.class.getName();
        final List<ImportNode> allImports = new ArrayList<>();
        allImports.addAll(moduleNode.getImports());
        allImports.addAll(moduleNode.getStarImports());
        allImports.addAll(moduleNode.getStaticImports().values());
        allImports.addAll(moduleNode.getStaticStarImports().values());

        ClassNode scriptClass = null;
        for (ImportNode importNode : allImports) {
            for (AnnotationNode annotation : importNode.getAnnotations()) {
                if (invariantName.equals(annotation.getClassNode().getName())) {
                    if (scriptClass == null) {
                        scriptClass = moduleNode.getClasses().stream()
                                .filter(ClassNode::isScriptBody)
                                .findFirst().orElse(null);
                        if (scriptClass == null) return;
                    }
                    scriptClass.addAnnotation(annotation);
                }
            }
        }
    }

    private void generateAnnotationClosureClasses(SourceUnit unit, ReaderSource source, List<ClassNode> classNodes) {
        final AnnotationClosureVisitor annotationClosureVisitor = new AnnotationClosureVisitor(unit, source);

        for (final ClassNode classNode : classNodes) {
            annotationClosureVisitor.visitClass(classNode);

            if (!CandidateChecks.isContractsCandidate(classNode) && !CandidateChecks.isInterfaceContractsCandidate(classNode))
                continue;

            final ContractElementVisitor contractElementVisitor = new ContractElementVisitor(unit, source);
            contractElementVisitor.visitClass(classNode);
            if (!contractElementVisitor.isFoundContractElement()) continue;

            annotationClosureVisitor.visitClass(classNode);
            markClassNodeAsContracted(classNode);
            if (classNode.isInterface()) continue;

            new ConfigurationSetup().init(classNode);
        }
    }

    private void markClassNodeAsContracted(final ClassNode classNode) {
        final ClassNode contractedAnnotationClassNode = ClassHelper.makeWithoutCaching(Contracted.class);

        if (classNode.getAnnotations(contractedAnnotationClassNode).isEmpty())
            classNode.addAnnotation(new AnnotationNode(contractedAnnotationClassNode));
    }
}
