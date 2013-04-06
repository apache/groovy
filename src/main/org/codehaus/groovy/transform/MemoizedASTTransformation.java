/*
 * Copyright 2008-2013 the original author or authors.
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
package org.codehaus.groovy.transform;

import groovy.lang.Memoized;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Handles generation of code for the {@link Memoized} annotation.
 * 
 * @author Andrey Bloschetsov
 */
@GroovyASTTransformation
public class MemoizedASTTransformation extends AbstractASTTransformation {

    private static final String MEMOIZE_METHOD_NAME = "memoize";
    private static final String CLOSURE_CALL_METHOD_NAME = "call";
    private static final Class<Memoized> MY_CLASS = Memoized.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes == null) {
            return;
        }
        init(nodes, source);

        AnnotationNode annotationNode = (AnnotationNode) nodes[0];
        AnnotatedNode annotatedNode = (AnnotatedNode) nodes[1];
        if (MY_TYPE.equals(annotationNode.getClassNode()) && annotatedNode instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) annotatedNode;

            ClosureExpression closureExpression = new ClosureExpression(methodNode.getParameters(),
                    methodNode.getCode());
            closureExpression.setVariableScope(methodNode.getVariableScope());

            ClassNode ownerClassNode = methodNode.getDeclaringClass();
            String uniqueName = buildUniqueNameByMethod(methodNode);
            String ownerClassName = ownerClassNode.getNameWithoutPackage();
            int modifiers = FieldNode.ACC_PRIVATE | FieldNode.ACC_FINAL;
            if (methodNode.isStatic()) {
                modifiers = modifiers | FieldNode.ACC_STATIC;
            }

            String memoizedClosureFieldName = ownerClassName + "_memoizeMethodClosure$" + uniqueName;
            MethodCallExpression memoizeClosureCallExpression = new MethodCallExpression(closureExpression,
                    MEMOIZE_METHOD_NAME, MethodCallExpression.NO_ARGUMENTS);
            FieldNode memoizedClosureField = new FieldNode(memoizedClosureFieldName, modifiers,
                    ClassHelper.DYNAMIC_TYPE, null, memoizeClosureCallExpression);
            ownerClassNode.addField(memoizedClosureField);

            BlockStatement newCode = new BlockStatement();
            ArgumentListExpression args = new ArgumentListExpression(methodNode.getParameters());
            MethodCallExpression closureCallExpression = new MethodCallExpression(new FieldExpression(
                    memoizedClosureField), CLOSURE_CALL_METHOD_NAME, args);
            newCode.addStatement(new ReturnStatement(closureCallExpression));
            newCode.setVariableScope(methodNode.getVariableScope());
            methodNode.setCode(newCode);
        }
    }

    /*
     * Build unique name.
     */
    private String buildUniqueNameByMethod(MethodNode methodNode) {
        StringBuilder nameBuilder = new StringBuilder(methodNode.getName());
        if (methodNode.getParameters() != null) {
            for (Parameter parameter : methodNode.getParameters()) {
                nameBuilder.append(parameter.getType().getNameWithoutPackage());
            }
        }

        return nameBuilder.toString();
    }
}
