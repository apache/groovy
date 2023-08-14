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

import org.apache.groovy.contracts.annotations.meta.ContractElement;
import org.apache.groovy.contracts.annotations.meta.Postcondition;
import org.apache.groovy.contracts.common.spi.AnnotationProcessor;
import org.apache.groovy.contracts.common.spi.ProcessingContextInformation;
import org.apache.groovy.contracts.generation.CandidateChecks;
import org.apache.groovy.contracts.util.AnnotationUtils;
import org.apache.groovy.contracts.util.Validate;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.groovy.contracts.ast.visitor.AnnotationClosureVisitor.META_DATA_ORIGINAL_TRY_CATCH_BLOCK;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Visits annotations of meta-type {@link ContractElement} and applies the AST transformations of the underlying
 * {@link org.apache.groovy.contracts.common.spi.AnnotationProcessor} implementation.
 *
 * @see org.apache.groovy.contracts.common.spi.AnnotationProcessor
 */
public class AnnotationProcessorVisitor extends BaseVisitor {

    private ProcessingContextInformation pci;

    public AnnotationProcessorVisitor(final SourceUnit sourceUnit, final ReaderSource source, final ProcessingContextInformation pci) {
        super(sourceUnit, source);
        Validate.notNull(pci);
        this.pci = pci;
    }

    @Override
    public void visitClass(ClassNode type) {
        handleClassNode(type);

        List<MethodNode> methodNodes = new ArrayList<>();
        methodNodes.addAll(type.getMethods());
        methodNodes.addAll(type.getDeclaredConstructors());

        for (MethodNode methodNode : methodNodes) {
            if (CandidateChecks.isClassInvariantCandidate(type, methodNode) || CandidateChecks.isPreOrPostconditionCandidate(type, methodNode)) {
                handleMethodAnnotations(methodNode, AnnotationUtils.hasMetaAnnotations(methodNode, ContractElement.class.getName()));
            }
        }

        // visit all interfaces of this class
        visitInterfaces(type, type.getInterfaces());
        visitAbstractBaseClassesForInterfaceMethodNodes(type, type.getSuperClass());
    }

    private void visitAbstractBaseClassesForInterfaceMethodNodes(ClassNode origin, ClassNode superClass) {
        if (superClass == null || !superClass.isAbstract()) return;
        for (ClassNode interfaceNode : superClass.getInterfaces()) {
            List<MethodNode> interfaceMethods = new ArrayList<>(interfaceNode.getMethods());
            for (MethodNode interfaceMethod : interfaceMethods) {
                List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(interfaceMethod, ContractElement.class.getName());
                if (annotationNodes == null || annotationNodes.isEmpty()) continue;

                MethodNode implementingMethod = superClass.getMethod(interfaceMethod.getName(), interfaceMethod.getParameters());
                // if implementingMethodNode == null, then superClass is abstract and does not implement the interface methodNode
                if (implementingMethod != null) continue;

                MethodNode implementationInOriginClassNode = origin.getMethod(interfaceMethod.getName(), interfaceMethod.getParameters());
                if (implementationInOriginClassNode == null) continue;

                handleMethodAnnotations(implementationInOriginClassNode, annotationNodes);
            }
        }
    }

    private void visitInterfaces(final ClassNode classNode, final ClassNode[] interfaces) {
        for (ClassNode interfaceNode : interfaces) {
            List<MethodNode> interfaceMethods = new ArrayList<>(interfaceNode.getMethods());
            // @ContractElement annotations are by now only supported on method interfaces
            for (MethodNode interfaceMethod : interfaceMethods) {
                MethodNode implementingMethod = classNode.getMethod(interfaceMethod.getName(), interfaceMethod.getParameters());
                if (implementingMethod == null) continue;

                List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(interfaceMethod, ContractElement.class.getName());
                handleInterfaceMethodNode(classNode, implementingMethod, annotationNodes);
            }

            visitInterfaces(classNode, interfaceNode.getInterfaces());
        }
    }

    private void handleClassNode(final ClassNode classNode) {
        List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(classNode, ContractElement.class.getName());
        for (AnnotationNode annotationNode : annotationNodes) {
            AnnotationProcessor annotationProcessor = createAnnotationProcessor(annotationNode);
            if (annotationProcessor != null) {
                Expression valueExpression = getReplacedCondition(annotationNode);
                BlockStatement blockStatement = valueExpression.getNodeMetaData(META_DATA_ORIGINAL_TRY_CATCH_BLOCK);
                annotationProcessor.process(pci, pci.contract(), classNode, blockStatement, asConditionExecution(annotationNode));
            }
        }
    }

    private void handleInterfaceMethodNode(ClassNode type, MethodNode methodNode, List<AnnotationNode> annotationNodes) {
        handleMethodAnnotations(type.getMethod(methodNode.getName(), methodNode.getParameters()), annotationNodes);
    }

    private void handleMethodAnnotations(MethodNode methodNode, List<AnnotationNode> annotationNodes) {
        if (methodNode == null) return;
        for (AnnotationNode annotationNode : annotationNodes) {
            AnnotationProcessor annotationProcessor = createAnnotationProcessor(annotationNode);
            if (annotationProcessor != null && getReplacedCondition(annotationNode) != null) {
                handleMethodAnnotation(methodNode, annotationNode, annotationProcessor);
            }
        }
    }

    private void handleMethodAnnotation(MethodNode methodNode, AnnotationNode annotationNode, AnnotationProcessor annotationProcessor) {
        boolean isPostcondition = AnnotationUtils.hasAnnotationOfType(annotationNode.getClassNode(), Postcondition.class.getName());

        ArgumentListExpression argumentList = new ArgumentListExpression();
        for (Parameter parameter : methodNode.getParameters()) {
            argumentList.addExpression(varX(parameter));
        }
        if (isPostcondition && !methodNode.isVoidMethod()) {
            argumentList.addExpression(localVarX("result", methodNode.getReturnType()));
        }
        if (isPostcondition && !methodNode.isConstructor()) {
            argumentList.addExpression(localVarX("old", ClassHelper.MAP_TYPE.getPlainNodeReference()));
        }

        Expression valueExpression = getReplacedCondition(annotationNode);
        BooleanExpression booleanExpression = asConditionExecution(annotationNode);
        ((MethodCallExpression) booleanExpression.getExpression()).setArguments(argumentList);
        BlockStatement blockStatement = valueExpression.getNodeMetaData(META_DATA_ORIGINAL_TRY_CATCH_BLOCK);
        annotationProcessor.process(pci, pci.contract(), methodNode.getDeclaringClass(), methodNode, blockStatement, booleanExpression);

        // if the implementation method has no annotation, we need to set a dummy marker in order to find parent pre/postconditions
        if (!AnnotationUtils.hasAnnotationOfType(methodNode, annotationNode.getClassNode().getName())) {
            AnnotationNode markerAnnotation = new AnnotationNode(annotationNode.getClassNode());
            replaceCondition(markerAnnotation, valueExpression);
            markerAnnotation.setRuntimeRetention(true);
            markerAnnotation.setSourceRetention(false);
            methodNode.addAnnotation(markerAnnotation);
        }
    }

    private AnnotationProcessor createAnnotationProcessor(AnnotationNode annotationNode) {
        Expression annotationProcessor = null;

        List<AnnotationNode> annotations = annotationNode.getClassNode().getAnnotations();
        for (AnnotationNode anno : annotations) {
            if (anno.getClassNode().getName().equals("org.apache.groovy.contracts.annotations.meta.AnnotationProcessorImplementation")) {
                annotationProcessor = anno.getMember("value");
                break;
            }
        }

        if (annotationProcessor != null) {
            try {
                var apt = Class.forName(annotationProcessor.getType().getName());
                return (AnnotationProcessor) apt.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException ignore) {
            }
        }

        throw new GroovyBugError("Annotation processing class could not be instantiated! This indicates a bug in groovy-contracts, please file an issue!");
    }
}
