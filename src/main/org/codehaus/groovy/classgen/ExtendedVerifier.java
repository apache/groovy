/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.control.AnnotationConstantsVisitor;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.objectweb.asm.Opcodes;

/**
 * A specialized Groovy AST visitor meant to perform additional verifications upon the
 * current AST. Currently it does checks on annotated nodes and annotations itself.
 * <p>
 * Current limitations:
 * - annotations on local variables are not supported
 *
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
 */
public class ExtendedVerifier implements GroovyClassVisitor {
    public static final String JVM_ERROR_MESSAGE = "Please make sure you are running on a JVM >= 1.5";

    private SourceUnit source;
    private ClassNode currentClass;

    public ExtendedVerifier(SourceUnit sourceUnit) {
        this.source = sourceUnit;
    }

    public void visitClass(ClassNode node) {
        AnnotationConstantsVisitor acv = new AnnotationConstantsVisitor();
        acv.visitClass(node, this.source);
        this.currentClass = node;
        if (node.isAnnotationDefinition()) {
            visitAnnotations(node, AnnotationNode.ANNOTATION_TARGET);
        } else {
            visitAnnotations(node, AnnotationNode.TYPE_TARGET);
        }
        PackageNode packageNode = node.getPackage();
        if (packageNode != null) {
            visitAnnotations(packageNode, AnnotationNode.PACKAGE_TARGET);
        }
        node.visitContents(this);
    }

    public void visitField(FieldNode node) {
        visitAnnotations(node, AnnotationNode.FIELD_TARGET);
    }

    public void visitConstructor(ConstructorNode node) {
        visitConstructorOrMethod(node, AnnotationNode.CONSTRUCTOR_TARGET);
    }

    public void visitMethod(MethodNode node) {
        visitConstructorOrMethod(node, AnnotationNode.METHOD_TARGET);
    }

    private void visitConstructorOrMethod(MethodNode node, int methodTarget) {
        visitAnnotations(node, methodTarget);
        for (int i = 0; i < node.getParameters().length; i++) {
            Parameter parameter = node.getParameters()[i];
            visitAnnotations(parameter, AnnotationNode.PARAMETER_TARGET);
        }

        if (this.currentClass.isAnnotationDefinition() && !node.isStaticConstructor()) {
            ErrorCollector errorCollector = new ErrorCollector(this.source.getConfiguration());
            AnnotationVisitor visitor = new AnnotationVisitor(this.source, errorCollector);
            visitor.setReportClass(currentClass);
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
                visitor.checkCircularReference(currentClass, node.getReturnType(), code.getExpression());
            }
            this.source.getErrorCollector().addCollectorContents(errorCollector);
        }
    }

    public void visitProperty(PropertyNode node) {
    }

    protected void visitAnnotations(AnnotatedNode node, int target) {
        if (node.getAnnotations().isEmpty()) {
            return;
        }
        this.currentClass.setAnnotated(true);
        if (!isAnnotationCompatible()) {
            addError("Annotations are not supported in the current runtime. " + JVM_ERROR_MESSAGE, node);
            return;
        }
        for (AnnotationNode unvisited : node.getAnnotations()) {
            AnnotationNode visited = visitAnnotation(unvisited);
            boolean isTargetAnnotation = visited.getClassNode().isResolved() &&
                    visited.getClassNode().getName().equals("java.lang.annotation.Target");

            // Check if the annotation target is correct, unless it's the target annotating an annotation definition
            // defining on which target elements the annotation applies
            if (!isTargetAnnotation && !visited.isTargetAllowed(target)) {
                addError("Annotation @" + visited.getClassNode().getName()
                        + " is not allowed on element " + AnnotationNode.targetToName(target),
                        visited);
            }
            visitDeprecation(node, visited);
        }
    }

    private void visitDeprecation(AnnotatedNode node, AnnotationNode visited) {
        if (visited.getClassNode().isResolved() && visited.getClassNode().getName().equals("java.lang.Deprecated")) {
            if (node instanceof MethodNode) {
                MethodNode mn = (MethodNode) node;
                mn.setModifiers(mn.getModifiers() | Opcodes.ACC_DEPRECATED);
            } else if (node instanceof FieldNode) {
                FieldNode fn = (FieldNode) node;
                fn.setModifiers(fn.getModifiers() | Opcodes.ACC_DEPRECATED);
            } else if (node instanceof ClassNode) {
                ClassNode cn = (ClassNode) node;
                cn.setModifiers(cn.getModifiers() | Opcodes.ACC_DEPRECATED);
            }
        }
    }

    /**
     * Resolve metadata and details of the annotation.
     *
     * @param unvisited the node to visit
     * @return the visited node
     */
    private AnnotationNode visitAnnotation(AnnotationNode unvisited) {
        ErrorCollector errorCollector = new ErrorCollector(this.source.getConfiguration());
        AnnotationVisitor visitor = new AnnotationVisitor(this.source, errorCollector);
        AnnotationNode visited = visitor.visit(unvisited);
        this.source.getErrorCollector().addCollectorContents(errorCollector);
        return visited;
    }

    /**
     * Check if the current runtime allows Annotation usage.
     *
     * @return true if running on a 1.5+ runtime
     */
    protected boolean isAnnotationCompatible() {
        return CompilerConfiguration.isPostJDK5(this.source.getConfiguration().getTargetBytecode());
    }

    protected void addError(String msg, ASTNode expr) {
        this.source.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(
                        new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber(), expr.getLastLineNumber(), expr.getLastColumnNumber()), this.source)
        );
    }

    // TODO use it or lose it
    public void visitGenericType(GenericsType genericsType) {

    }
}
