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
import org.apache.groovy.contracts.common.spi.AnnotationProcessor;
import org.apache.groovy.contracts.common.spi.ProcessingContextInformation;
import org.apache.groovy.contracts.generation.CandidateChecks;
import org.apache.groovy.contracts.util.AnnotationUtils;
import org.apache.groovy.contracts.util.Validate;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
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

        List<MethodNode> methodNodes = new ArrayList<MethodNode>();
        methodNodes.addAll(type.getMethods());
        methodNodes.addAll(type.getDeclaredConstructors());

        for (MethodNode methodNode : methodNodes) {
            if (!CandidateChecks.isClassInvariantCandidate(type, methodNode) && !CandidateChecks.isPreOrPostconditionCandidate(type, methodNode))
                continue;

            handleMethodNode(methodNode, AnnotationUtils.hasMetaAnnotations(methodNode, ContractElement.class.getName()));
        }

        // visit all interfaces of this class
        visitInterfaces(type, type.getInterfaces());
        visitAbstractBaseClassesForInterfaceMethodNodes(type, type.getSuperClass());
    }

    private void visitAbstractBaseClassesForInterfaceMethodNodes(ClassNode origin, ClassNode superClass) {
        if (superClass == null) return;
        if (!Modifier.isAbstract(superClass.getModifiers())) return;

        for (ClassNode interfaceClassNode : superClass.getInterfaces()) {
            List<MethodNode> methodNodes = new ArrayList<MethodNode>();
            methodNodes.addAll(interfaceClassNode.getMethods());

            for (MethodNode interfaceMethodNode : methodNodes) {
                final List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(interfaceMethodNode, ContractElement.class.getName());
                if (annotationNodes == null || annotationNodes.isEmpty()) continue;

                MethodNode implementingMethodNode = superClass.getMethod(interfaceMethodNode.getName(), interfaceMethodNode.getParameters());

                // if implementingMethodNode == null, then superClass is abstract and does not implement
                // the current interface methodNode
                if (implementingMethodNode != null) continue;

                MethodNode implementationInOriginClassNode = origin.getMethod(interfaceMethodNode.getName(), interfaceMethodNode.getParameters());
                if (implementationInOriginClassNode == null) continue;

                handleMethodNode(implementationInOriginClassNode, annotationNodes);
            }
        }
    }

    private void visitInterfaces(final ClassNode classNode, final ClassNode[] interfaces) {
        for (ClassNode interfaceClassNode : interfaces) {
            List<MethodNode> methodNodes = new ArrayList<MethodNode>();
            methodNodes.addAll(interfaceClassNode.getMethods());

            // @ContractElement annotations are by now only supported on method interfaces
            for (MethodNode interfaceMethodNode : methodNodes) {
                MethodNode implementingMethodNode = classNode.getMethod(interfaceMethodNode.getName(), interfaceMethodNode.getParameters());
                if (implementingMethodNode == null) continue;

                final List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(interfaceMethodNode, ContractElement.class.getName());
                handleInterfaceMethodNode(classNode, implementingMethodNode, annotationNodes);
            }

            visitInterfaces(classNode, interfaceClassNode.getInterfaces());
        }
    }

    private void handleClassNode(final ClassNode classNode) {
        final List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(classNode, ContractElement.class.getName());

        for (AnnotationNode annotationNode : annotationNodes) {
            final AnnotationProcessor annotationProcessor = createAnnotationProcessor(annotationNode);

            if (annotationProcessor != null && annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME) instanceof ClassExpression) {
                final ClassExpression closureClassExpression = (ClassExpression) annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME);

                MethodCallExpression doCall = callX(
                        ctorX(closureClassExpression.getType(), args(VariableExpression.THIS_EXPRESSION, VariableExpression.THIS_EXPRESSION)),
                        "doCall"
                );
                doCall.setMethodTarget(closureClassExpression.getType().getMethods("doCall").get(0));

                final BooleanExpression booleanExpression = boolX(doCall);
                booleanExpression.setSourcePosition(annotationNode);

                annotationProcessor.process(pci, pci.contract(), classNode, closureClassExpression.getNodeMetaData(AnnotationClosureVisitor.META_DATA_ORIGINAL_TRY_CATCH_BLOCK), booleanExpression);
            }
        }
    }

    private void handleInterfaceMethodNode(ClassNode type, MethodNode methodNode, List<AnnotationNode> annotationNodes) {
        handleMethodNode(type.getMethod(methodNode.getName(), methodNode.getParameters()), annotationNodes);
    }

    private void handleMethodNode(MethodNode methodNode, List<AnnotationNode> annotationNodes) {
        if (methodNode == null) return;

        for (AnnotationNode annotationNode : annotationNodes) {
            final AnnotationProcessor annotationProcessor = createAnnotationProcessor(annotationNode);

            if (annotationProcessor != null && annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME) instanceof ClassExpression) {
                boolean isPostcondition = AnnotationUtils.hasAnnotationOfType(annotationNode.getClassNode(), org.apache.groovy.contracts.annotations.meta.Postcondition.class.getName());

                ClassExpression closureClassExpression = (ClassExpression) annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME);

                ArgumentListExpression closureArgumentList = new ArgumentListExpression();

                for (Parameter parameter : methodNode.getParameters()) {
                    closureArgumentList.addExpression(varX(parameter));
                }

                if (methodNode.getReturnType() != ClassHelper.VOID_TYPE && isPostcondition && !(methodNode instanceof ConstructorNode)) {
                    closureArgumentList.addExpression(localVarX("result", methodNode.getReturnType()));
                }

                if (isPostcondition && !(methodNode instanceof ConstructorNode)) {
                    closureArgumentList.addExpression(localVarX("old", new ClassNode(Map.class)));
                }

                MethodCallExpression doCall = callX(
                        ctorX(annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME).getType(), args(VariableExpression.THIS_EXPRESSION, VariableExpression.THIS_EXPRESSION)),
                        "doCall",
                        closureArgumentList
                );
                ClassNode type = annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME).getType();
                doCall.setMethodTarget(type.getMethods("doCall").get(0));

                final BooleanExpression booleanExpression = boolX(doCall);
                booleanExpression.setSourcePosition(annotationNode);

                annotationProcessor.process(pci, pci.contract(), methodNode.getDeclaringClass(), methodNode, closureClassExpression.getNodeMetaData(AnnotationClosureVisitor.META_DATA_ORIGINAL_TRY_CATCH_BLOCK), booleanExpression);

                // if the implementation method has no annotation, we need to set a dummy marker in order to find parent pre/postconditions
                if (!AnnotationUtils.hasAnnotationOfType(methodNode, annotationNode.getClassNode().getName())) {
                    AnnotationNode annotationMarker = new AnnotationNode(annotationNode.getClassNode());
                    annotationMarker.setMember(CLOSURE_ATTRIBUTE_NAME, annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME));
                    annotationMarker.setRuntimeRetention(true);
                    annotationMarker.setSourceRetention(false);

                    methodNode.addAnnotation(annotationMarker);
                }
            }
        }
    }

    private AnnotationProcessor createAnnotationProcessor(AnnotationNode annotationNode) {
        ClassExpression annotationProcessingAnno = null;

        List<AnnotationNode> annotations = annotationNode.getClassNode().redirect().getAnnotations();
        for (AnnotationNode anno : annotations) {
            Class typeClass = anno.getClassNode().getTypeClass();

            if (typeClass.getName().equals("org.apache.groovy.contracts.annotations.meta.AnnotationProcessorImplementation")) {
                annotationProcessingAnno = (ClassExpression) anno.getMember("value");
                break;
            }
        }

        if (annotationProcessingAnno == null)
            throw new GroovyBugError("Annotation processing class could not be found! This indicates a bug in groovy-contracts, please file an issue!");

        try {
            final Class clz = Class.forName(annotationProcessingAnno.getType().getTypeClass().getName());
            return (AnnotationProcessor) clz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        }

        throw new GroovyBugError("Annotation processing class could not be instantiated! This indicates a bug in groovy-contracts, please file an issue!");
    }
}
