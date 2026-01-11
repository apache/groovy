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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.RecordComponentNode;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.AnnotationConstantsVisitor;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.codehaus.groovy.ast.AnnotationNode.ANNOTATION_TARGET;
import static org.codehaus.groovy.ast.AnnotationNode.CONSTRUCTOR_TARGET;
import static org.codehaus.groovy.ast.AnnotationNode.FIELD_TARGET;
import static org.codehaus.groovy.ast.AnnotationNode.LOCAL_VARIABLE_TARGET;
import static org.codehaus.groovy.ast.AnnotationNode.METHOD_TARGET;
import static org.codehaus.groovy.ast.AnnotationNode.PACKAGE_TARGET;
import static org.codehaus.groovy.ast.AnnotationNode.PARAMETER_TARGET;
import static org.codehaus.groovy.ast.AnnotationNode.RECORD_COMPONENT_TARGET;
import static org.codehaus.groovy.ast.AnnotationNode.TYPE_PARAMETER_TARGET;
import static org.codehaus.groovy.ast.AnnotationNode.TYPE_TARGET;
import static org.codehaus.groovy.ast.AnnotationNode.TYPE_USE_TARGET;
import static org.codehaus.groovy.ast.ClassHelper.DEPRECATED_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.makeCached;
import static org.codehaus.groovy.ast.tools.GeneralUtils.listX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpec;
import static org.codehaus.groovy.ast.tools.GenericsUtils.createGenericsSpec;
import static org.codehaus.groovy.ast.tools.GenericsUtils.parameterizeType;
import static org.codehaus.groovy.ast.tools.ParameterUtils.parametersEqual;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.evaluateExpression;

/**
 * A specialized Groovy AST visitor meant to perform additional verifications upon the
 * current AST. Currently it does checks on annotated nodes and annotations itself.
 * <p>
 * Current limitations:
 * - annotations on local variables are not supported
 */
public class ExtendedVerifier extends ClassCodeVisitorSupport {

    @Deprecated(forRemoval = true, since = "5.0.0")
    public static final String JVM_ERROR_MESSAGE = "Please make sure you are running on a JVM >= 1.5";

    private ClassNode currentClass;
    private final SourceUnit source;
    private final Map<String, Boolean> repeatableCache = new HashMap<>();

    public ExtendedVerifier(final SourceUnit sourceUnit) {
        this.source = sourceUnit;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return this.source;
    }

    @Override
    public void visitClass(final ClassNode node) {
        AnnotationConstantsVisitor acv = new AnnotationConstantsVisitor();
        acv.visitClass(node, this.source);
        this.currentClass = node;
        PackageNode packageNode = node.getPackage();
        if (packageNode != null) {
            visitAnnotations(packageNode, PACKAGE_TARGET);
        }
        if (node.isAnnotationDefinition()) {
            visitAnnotations(node, ANNOTATION_TARGET);
        } else {
            visitAnnotations(node, TYPE_TARGET);
            visitGenericsTypeAnnotations(node);
        }
        visitTypeAnnotations(node.getUnresolvedSuperClass());
        for (ClassNode anInterface : node.getInterfaces()) {
            visitTypeAnnotations(anInterface);
        }
        if (node.isRecord()) {
            for (var recordComponent : node.getRecordComponents()) {
                visitAnnotations(recordComponent, RECORD_COMPONENT_TARGET);
                visitTypeAnnotations(recordComponent.getType());
                extractTypeUseAnnotations(recordComponent.getAnnotations(), recordComponent.getType(), RECORD_COMPONENT_TARGET);
            }
        }
        node.visitContents(this);
    }

    @Override
    public void visitField(final FieldNode node) {
        visitAnnotations(node, FIELD_TARGET);

        if (!node.isStatic() && this.currentClass.isRecord()) {
            // record's instance fields are created by compiler and reuse type instance of record components.
            // return here to avoid processing type instance repeatedly.
            return;
        }
        visitTypeAnnotations(node.getType());
        extractTypeUseAnnotations(node.getAnnotations(), node.getType(), FIELD_TARGET);
    }

    @Override
    public void visitProperty(final PropertyNode node) {
    }

    @Override
    public void visitDeclarationExpression(final DeclarationExpression expression) {
        visitAnnotations(expression, LOCAL_VARIABLE_TARGET);
        if (expression.isMultipleAssignmentDeclaration()) {
            expression.getTupleExpression().forEach(e -> visitTypeAnnotations(e.getType()));
        } else {
            ClassNode type = expression.getLeftExpression().getType();
            visitTypeAnnotations(type);
            extractTypeUseAnnotations(expression.getAnnotations(), type, LOCAL_VARIABLE_TARGET);
        }
        expression.getRightExpression().visit(this);
    }

    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression expression) {
        if (!expression.isSpecialCall()) {
            visitTypeAnnotations(expression.getType());
        }
        super.visitConstructorCallExpression(expression);
    }

    private void visitConstructorOrMethod(final MethodNode node) {
        visitGenericsTypeAnnotations(node);
        for (Parameter parameter : node.getParameters()) {
            visitAnnotations(parameter, PARAMETER_TARGET);
            visitTypeAnnotations(parameter.getType());
            extractTypeUseAnnotations(parameter.getAnnotations(), parameter.getType(), PARAMETER_TARGET);
        }
        if (node.getExceptions() != null) {
            for (ClassNode t : node.getExceptions()) {
                visitTypeAnnotations(t);
            }
        }

        if (this.currentClass.isAnnotationDefinition() && !node.isStaticConstructor()) {
            ErrorCollector errorCollector = new ErrorCollector(this.source.getConfiguration());
            AnnotationVisitor visitor = new AnnotationVisitor(this.source, errorCollector);
            visitor.setReportClass(this.currentClass);
            visitor.checkReturnType(node.getReturnType(), node);
            if (node.getParameters().length > 0) {
                addError("Annotation members may not have parameters.", node.getParameters()[0]);
            }
            if (node.getExceptions().length > 0) {
                addError("Annotation members may not have a throws clause.", node.getExceptions()[0]);
            }
            ReturnStatement code = (ReturnStatement) node.getCode();
            if (code != null) {
                visitor.visitExpression(node.getName(), code.getExpression(), node.getReturnType());
                visitor.checkCircularReference(this.currentClass, node.getReturnType(), code.getExpression());
            }
            this.source.getErrorCollector().addCollectorContents(errorCollector);
        }

        Statement code = node.getCode();
        if (code != null) {
            code.visit(this);
        }
    }

    @Override
    public void visitConstructor(final ConstructorNode node) {
        visitAnnotations(node, CONSTRUCTOR_TARGET);
        visitConstructorOrMethod(node);
        if (!node.getReturnType().isRedirectNode() && node.getAnnotations().stream().anyMatch(anno -> anno.isTargetAllowed(TYPE_USE_TARGET))) {
            node.setReturnType(node.getReturnType().getPlainNodeReference(false)); // GROOVY-10937
        }
        extractTypeUseAnnotations(node.getAnnotations(), node.getReturnType(), CONSTRUCTOR_TARGET);
    }

    @Override
    public void visitMethod(final MethodNode node) {
        visitAnnotations(node, METHOD_TARGET);
        visitTypeAnnotations(node.getReturnType());
        visitConstructorOrMethod(node);
        extractTypeUseAnnotations(node.getAnnotations(), node.getReturnType(), METHOD_TARGET);
    }

    //--------------------------------------------------------------------------

    private void visitTypeAnnotations(final ClassNode node) {
        if ((node.isRedirectNode() || node.isPrimaryClassNode()) && !Boolean.TRUE.equals(node.putNodeMetaData("EXTENDED_VERIFIER_SEEN", Boolean.TRUE))) {
            visitAnnotations(node, node.getTypeAnnotations(), node.isGenericsPlaceHolder() ? TYPE_PARAMETER_TARGET : TYPE_USE_TARGET);
            if (node.isArray()) visitTypeAnnotations(node.getComponentType());
            else visitGenericsTypeAnnotations(node);
        }
    }

    private void visitGenericsTypeAnnotations(final ClassNode node) {
        GenericsType[] genericsTypes = node.getGenericsTypes();
        if (node.isUsingGenerics() && genericsTypes != null) {
            visitGenericsTypeAnnotations(genericsTypes);
        }
    }

    private void visitGenericsTypeAnnotations(final MethodNode node) {
        GenericsType[] genericsTypes = node.getGenericsTypes();
        if (genericsTypes != null) {
            visitGenericsTypeAnnotations(genericsTypes);
        }
    }

    private void visitGenericsTypeAnnotations(final GenericsType[] genericsTypes) {
        for (GenericsType gt : genericsTypes) {
            visitTypeAnnotations(gt.getType());
            if (gt.getLowerBound() != null) {
                visitTypeAnnotations(gt.getLowerBound());
            }
            if (gt.getUpperBounds() != null) {
                for (ClassNode ub : gt.getUpperBounds()) {
                    visitTypeAnnotations(ub);
                }
            }
        }
    }

    private void extractTypeUseAnnotations(final List<AnnotationNode> mixed, ClassNode targetType, final int keepTarget) {
        List<AnnotationNode> typeUseAnnos = new ArrayList<>();
        for (AnnotationNode anno : mixed) {
            if (anno.isTargetAllowed(TYPE_USE_TARGET)) {
                typeUseAnnos.add(anno);
            }
        }
        if (!typeUseAnnos.isEmpty()) {
            while (targetType.isArray()) targetType = targetType.getComponentType();
            targetType.addTypeAnnotations(typeUseAnnos);
            for (AnnotationNode anno : typeUseAnnos) {
                if (!anno.isTargetAllowed(keepTarget)) {
                    mixed.remove(anno);
                }
            }
        }
    }

    protected void visitAnnotations(final AnnotatedNode node, final int target) {
        visitAnnotations(node, node.getAnnotations(), target);
    }

    private void visitAnnotations(final AnnotatedNode node, final List<AnnotationNode> annotations, final int target) {
        if (annotations.isEmpty()) return;
        this.currentClass.setAnnotated(true);
        Map<String, List<AnnotationNode>> nonSourceAnnotations = new LinkedHashMap<>();
        boolean skippable = Boolean.TRUE.equals(node.getNodeMetaData("_SKIPPABLE_ANNOTATIONS"));
        for (Iterator<AnnotationNode> iterator = annotations.iterator(); iterator.hasNext(); ) {
            AnnotationNode unvisited = iterator.next();
            AnnotationNode visited;
            {
                ErrorCollector errorCollector = new ErrorCollector(source.getConfiguration());
                AnnotationVisitor visitor = new AnnotationVisitor(source, errorCollector);
                visited = visitor.visit(unvisited);
                source.getErrorCollector().addCollectorContents(errorCollector);
            }

            String name = visited.getClassNode().getName();
            if (skippable && shouldSkip(node, visited)) {
                iterator.remove();
                continue;
            }
            if (!visited.hasSourceRetention()) {
                List<AnnotationNode> seen = nonSourceAnnotations.get(name);
                if (seen == null) {
                    seen = new ArrayList<>();
                } else if (!isRepeatable(visited)) {
                    addError("Cannot specify duplicate annotation on the same member : " + name, visited);
                }
                seen.add(visited);
                nonSourceAnnotations.put(name, seen);
            }

            // Check if the annotation target is correct, unless it's the target annotating an annotation definition
            // defining on which target elements the annotation applies
            boolean isTargetAnnotation = "java.lang.annotation.Target".equals(name);
            if (!isTargetAnnotation && !visited.isTargetAllowed(target) && !isTypeUseScenario(visited, target)) {
                addError("Annotation @" + name + " is not allowed on element " + AnnotationNode.targetToName(target), visited);
            }
            visitDeprecation(node, visited);
            visitOverride(node, visited);
        }
        processDuplicateAnnotationContainers(node, nonSourceAnnotations);
    }

    private boolean shouldSkip(final AnnotatedNode node, final AnnotationNode visited) {
        return (node instanceof ClassNode && !visited.isTargetAllowed(TYPE_TARGET) && !visited.isTargetAllowed(TYPE_USE_TARGET) && visited.isTargetAllowed(CONSTRUCTOR_TARGET))
                || (node instanceof ConstructorNode && !visited.isTargetAllowed(CONSTRUCTOR_TARGET) && visited.isTargetAllowed(TYPE_TARGET))
                || (node instanceof FieldNode && !visited.isTargetAllowed(FIELD_TARGET) && !visited.isTargetAllowed(TYPE_USE_TARGET))
                || (node instanceof Parameter && !visited.isTargetAllowed(PARAMETER_TARGET) && !visited.isTargetAllowed(TYPE_USE_TARGET))
                || (node instanceof MethodNode && !(node instanceof ConstructorNode) && !visited.isTargetAllowed(METHOD_TARGET) && !visited.isTargetAllowed(TYPE_USE_TARGET))
                || (node instanceof RecordComponentNode && !visited.isTargetAllowed(RECORD_COMPONENT_TARGET) && !visited.isTargetAllowed(TYPE_USE_TARGET));
    }

    private boolean isRepeatable(final AnnotationNode annoNode) {
        ClassNode annoClassNode = annoNode.getClassNode();
        String name = annoClassNode.getName();
        if (!repeatableCache.containsKey(name)) {
            boolean result = false;
            for (AnnotationNode anno : annoClassNode.getAnnotations()) {
                if ("java.lang.annotation.Repeatable".equals(anno.getClassNode().getName())) {
                    result = true;
                    break;
                }
            }
            repeatableCache.put(name, result);
        }
        return repeatableCache.get(name);
    }

    private boolean isTypeUseScenario(final AnnotationNode visited, final int target) {
        // allow type use everywhere except package
        return (visited.isTargetAllowed(TYPE_USE_TARGET) && ((target & PACKAGE_TARGET) == 0));
    }

    private void processDuplicateAnnotationContainers(final AnnotatedNode node, final Map<String, List<AnnotationNode>> nonSourceAnnotations) {
        for (Map.Entry<String, List<AnnotationNode>> entry : nonSourceAnnotations.entrySet()) {
            if (entry.getValue().size() > 1) {
                ClassNode repeatable = null;
                AnnotationNode repeatee = entry.getValue().get(0);
                for (AnnotationNode anno : repeatee.getClassNode().getAnnotations()) {
                    if ("java.lang.annotation.Repeatable".equals(anno.getClassNode().getName())) {
                        Expression value = anno.getMember("value");
                        if (value instanceof ClassExpression && value.getType().isAnnotationDefinition()) {
                            repeatable = value.getType();
                            break;
                        }
                    }
                }
                if (repeatable != null) {
                    if (nonSourceAnnotations.containsKey(repeatable.getName())) {
                        addError("Cannot specify duplicate annotation on the same member. Explicit " + repeatable.getName() + " found when creating implicit container for " + entry.getKey(), node);
                    }
                    AnnotationNode collector = new AnnotationNode(repeatable);
                    if (repeatee.hasClassRetention()) {
                        collector.setClassRetention(true);
                    } else if (repeatee.hasRuntimeRetention()) {
                        collector.setRuntimeRetention(true);
                    } else { // load retention policy from annotation definition
                        List<AnnotationNode> retention = repeatable.getAnnotations(makeCached(Retention.class));
                        if (!retention.isEmpty()) {
                            Object policy;
                            Expression value = retention.get(0).getMember("value");
                            if (value instanceof PropertyExpression) {
                                policy = ((PropertyExpression) value).getPropertyAsString();
                            } else { // NOTE: it is risky to evaluate the expression from repeatable's source this way:
                                policy = evaluateExpression(value, source.getConfiguration(), source.getClassLoader());
                            }
                            if ("CLASS".equals(policy)) {
                                collector.setClassRetention(true);
                            } else if ("RUNTIME".equals(policy)) {
                                collector.setRuntimeRetention(true);
                            }
                        }
                    }
                    collector.addMember("value", listX(entry.getValue().stream().map(AnnotationConstantExpression::new).collect(toList())));
                    node.getAnnotations().removeAll(entry.getValue());
                    node.addAnnotation(collector);
                }
            }
        }
    }

    private static void visitDeprecation(final AnnotatedNode node, final AnnotationNode visited) {
        if (visited.getClassNode().isResolved() && visited.getClassNode().equals(DEPRECATED_TYPE)) {
            if (node instanceof MethodNode mn) {
                mn.setModifiers(mn.getModifiers() | Opcodes.ACC_DEPRECATED);
            } else if (node instanceof FieldNode fn) {
                fn.setModifiers(fn.getModifiers() | Opcodes.ACC_DEPRECATED);
            } else if (node instanceof ClassNode cn) {
                cn.setModifiers(cn.getModifiers() | Opcodes.ACC_DEPRECATED);
            }
        }
    }

    // TODO GROOVY-5011 handle case of @Override on a property
    private void visitOverride(final AnnotatedNode node, final AnnotationNode visited) {
        ClassNode annotationType = visited.getClassNode();
        if (annotationType.isResolved() && "java.lang.Override".equals(annotationType.getName())) {
            if (node instanceof MethodNode mNode && !Boolean.TRUE.equals(node.getNodeMetaData(Verifier.DEFAULT_PARAMETER_GENERATED))) {
                boolean override = false;
                ClassNode  cNode = mNode.getDeclaringClass();
                if (mNode.hasDefaultValue()) {
                    List<MethodNode> variants = cNode.getDeclaredMethods(mNode.getName());
                    for (MethodNode m : variants) {
                        if (m.getAnnotations().contains(visited) && isOverrideMethod(m)) {
                            override = true;
                            break;
                        }
                    }
                } else {
                    override = isOverrideMethod(mNode);
                }

                if (!override) {
                    addError("Method '" + mNode.getName() + "' from class '" + cNode.getName() + "' does not override " +
                            "method from its superclass or interfaces but is annotated with @Override.", visited);
                }
            }
        }
    }

    private static boolean isOverrideMethod(final MethodNode method) {
        final ClassNode declaringClass = method.getDeclaringClass();
        var todo = new LinkedList<ClassNode>();
        var done = new HashSet<ClassNode>();
        var next = declaringClass;
        do {
            if (done.add(next)) {
                if (next != declaringClass
                        && getDeclaredMethodCorrected(method, next) != null) {
                    return true;
                }
                ClassNode sc = next.redirect().getUnresolvedSuperClass(false);
                if (sc != null) todo.addFirst(parameterizeType(next, sc));
                for (ClassNode in : next.getInterfaces()) {
                    todo.add(parameterizeType(next, in));
                }
            }
        } while ((next = todo.poll()) != null);

        return false;
    }

    private static MethodNode getDeclaredMethodCorrected(final MethodNode mn, final ClassNode cn) {
        Map<String, ClassNode> genericsSpec = createGenericsSpec(cn);
        for (MethodNode declared : cn.getDeclaredMethods(mn.getName())) {
            MethodNode corrected = correctToGenericsSpec(genericsSpec, declared);
            if (parametersEqual(corrected.getParameters(), mn.getParameters())) {
                return corrected;
            }
        }
        return null;
    }
}
