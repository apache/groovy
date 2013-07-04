/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform;

import groovy.transform.Memoized;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles generation of code for the {@link Memoized} annotation.
 *
 * @author Andrey Bloschetsov
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class MemoizedASTTransformation extends AbstractASTTransformation {

    private static final String CLOSURE_CALL_METHOD_NAME = "call";
    private static final Class<Memoized> MY_CLASS = Memoized.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final String PROTECTED_CACHE_SIZE_NAME = "protectedCacheSize";
    private static final String MAX_CACHE_SIZE_NAME = "maxCacheSize";
    private static final String CLOSURE_LABEL = "Closure";
    private static final String METHOD_LABEL = "Priv";

    public void visit(ASTNode[] nodes, final SourceUnit source) {
        if (nodes == null) {
            return;
        }
        init(nodes, source);

        AnnotationNode annotationNode = (AnnotationNode) nodes[0];
        AnnotatedNode annotatedNode = (AnnotatedNode) nodes[1];
        if (MY_TYPE.equals(annotationNode.getClassNode()) && annotatedNode instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) annotatedNode;
            if (methodNode.isAbstract()) {
                addError("Annotation " + MY_TYPE_NAME + " cannot be used for abstract methods.", methodNode);
                return;
            }
            if (methodNode.isVoidMethod()) {
                addError("Annotation " + MY_TYPE_NAME + " cannot be used for void methods.", methodNode);
                return;
            }

            ClassNode ownerClassNode = methodNode.getDeclaringClass();
            MethodNode privateMethod = buildPrivateDelegatingMethod(methodNode, ownerClassNode);
            ownerClassNode.addMethod(privateMethod);

            int modifiers = FieldNode.ACC_PRIVATE | FieldNode.ACC_FINAL;
            if (methodNode.isStatic()) {
                modifiers = modifiers | FieldNode.ACC_STATIC;
            }

            int protectedCacheSize = getIntMemberValue(annotationNode, PROTECTED_CACHE_SIZE_NAME);
            int maxCacheSize = getIntMemberValue(annotationNode, MAX_CACHE_SIZE_NAME);
            MethodCallExpression memoizeClosureCallExpression =
                    buildMemoizeClosureCallExpression(privateMethod, protectedCacheSize, maxCacheSize);

            String memoizedClosureFieldName = buildUniqueName(ownerClassNode, CLOSURE_LABEL, methodNode);
            FieldNode memoizedClosureField = new FieldNode(memoizedClosureFieldName, modifiers,
                    ClassHelper.CLOSURE_TYPE.getPlainNodeReference(), null, memoizeClosureCallExpression);
            ownerClassNode.addField(memoizedClosureField);

            BlockStatement newCode = new BlockStatement();
            ArgumentListExpression args = new ArgumentListExpression(methodNode.getParameters());
            MethodCallExpression closureCallExpression = new MethodCallExpression(new FieldExpression(
                    memoizedClosureField), CLOSURE_CALL_METHOD_NAME, args);
            closureCallExpression.setImplicitThis(false);
            newCode.addStatement(new ReturnStatement(closureCallExpression));
            methodNode.setCode(newCode);
            VariableScopeVisitor visitor = new VariableScopeVisitor(source);
            visitor.visitClass(ownerClassNode);
        }
    }

    private static Parameter[] cloneParams(Parameter[] source) {
        Parameter[] result = new Parameter[source.length];
        for (int i = 0; i < source.length; i++) {
            Parameter srcParam = source[i];
            Parameter dstParam = new Parameter(srcParam.getOriginType(), srcParam.getName());
            result[i] = dstParam;
        }
        return result;
    }

    private MethodNode buildPrivateDelegatingMethod(final MethodNode annotatedMethod, final ClassNode ownerClassNode) {
        Statement code = annotatedMethod.getCode();
        int access = ACC_PRIVATE;
        if (annotatedMethod.isStatic()) {
            access = ACC_PRIVATE | ACC_STATIC;
        }
        MethodNode privateMethod = new MethodNode(
                buildUniqueName(ownerClassNode, METHOD_LABEL, annotatedMethod),
                access,
                annotatedMethod.getReturnType(),
                cloneParams(annotatedMethod.getParameters()),
                annotatedMethod.getExceptions(),
                code
        );
        List<AnnotationNode> sourceAnnotations = annotatedMethod.getAnnotations();
        privateMethod.addAnnotations(new ArrayList<AnnotationNode>(sourceAnnotations));
        return privateMethod;
    }

    private int getIntMemberValue(AnnotationNode node, String name) {
        Object value = getMemberValue(node, name);
        if (value != null && value instanceof Integer) {
            return (Integer) value;
        }

        return 0;
    }

    private static final String MEMOIZE_METHOD_NAME = "memoize";
    private static final String MEMOIZE_AT_MOST_METHOD_NAME = "memoizeAtMost";
    private static final String MEMOIZE_AT_LEAST_METHOD_NAME = "memoizeAtLeast";
    private static final String MEMOIZE_BETWEEN_METHOD_NAME = "memoizeBetween";

    private MethodCallExpression buildMemoizeClosureCallExpression(MethodNode privateMethod,
                                                                   int protectedCacheSize, int maxCacheSize) {
        Parameter[] srcParams = privateMethod.getParameters();
        Parameter[] newParams = cloneParams(srcParams);
        List<Expression> argList = new ArrayList<Expression>(newParams.length);
        for (int i = 0; i < srcParams.length; i++) {
            argList.add(new VariableExpression(newParams[i]));
        }

        ClosureExpression expression = new ClosureExpression(
                newParams,
                new ExpressionStatement(
                        new MethodCallExpression(
                                new VariableExpression("this"),
                                privateMethod.getName(),
                                new ArgumentListExpression(argList))
                )
        );
        MethodCallExpression mce;
        if (protectedCacheSize == 0 && maxCacheSize == 0) {
            mce = new MethodCallExpression(expression, MEMOIZE_METHOD_NAME, MethodCallExpression.NO_ARGUMENTS);
        } else if (protectedCacheSize == 0) {
            mce = new MethodCallExpression(expression, MEMOIZE_AT_MOST_METHOD_NAME, new ArgumentListExpression(
                    new ConstantExpression(maxCacheSize)));
        } else if (maxCacheSize == 0) {
            mce = new MethodCallExpression(expression, MEMOIZE_AT_LEAST_METHOD_NAME, new ArgumentListExpression(
                    new ConstantExpression(protectedCacheSize)));
        } else {
            ArgumentListExpression args = new ArgumentListExpression(new Expression[]{
                    new ConstantExpression(protectedCacheSize), new ConstantExpression(maxCacheSize)});

            mce = new MethodCallExpression(expression, MEMOIZE_BETWEEN_METHOD_NAME, args);
        }
        mce.setImplicitThis(false);
        return mce;
    }

    /*
     * Build unique name.
     */
    private static String buildUniqueName(ClassNode owner, String ident, MethodNode methodNode) {
        StringBuilder nameBuilder = new StringBuilder("memoizedMethod" + ident + "$").append(methodNode.getName());
        if (methodNode.getParameters() != null) {
            for (Parameter parameter : methodNode.getParameters()) {
                nameBuilder.append(parameter.getType().getNameWithoutPackage());
            }
        }
        while (owner.getField(nameBuilder.toString()) != null) {
            nameBuilder.insert(0, "_");
        }

        return nameBuilder.toString();
    }

}
