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
package org.codehaus.groovy.transform;

import groovy.transform.Memoized;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.List;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.cloneParams;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.newClass;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * Handles generation of code for the {@link Memoized} annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class MemoizedASTTransformation extends AbstractASTTransformation {

    private static final String CLOSURE_CALL_METHOD_NAME = "call";
    private static final Class<Memoized> MY_CLASS = Memoized.class;
    private static final ClassNode MY_TYPE = make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final String PROTECTED_CACHE_SIZE_NAME = "protectedCacheSize";
    private static final String MAX_CACHE_SIZE_NAME = "maxCacheSize";
    private static final String CLOSURE_LABEL = "Closure";
    private static final String METHOD_LABEL = "Priv";
    private static final ClassNode OVERRIDE_CLASSNODE = make(Override.class);

    @Override
    public void visit(ASTNode[] nodes, final SourceUnit source) {
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
            MethodNode delegatingMethod = buildDelegatingMethod(methodNode, ownerClassNode);
            addGeneratedMethod(ownerClassNode, delegatingMethod);

            int modifiers = ACC_PRIVATE | ACC_FINAL;
            if (methodNode.isStatic()) {
                modifiers = modifiers | ACC_STATIC;
            }

            int protectedCacheSize = getMemberIntValue(annotationNode, PROTECTED_CACHE_SIZE_NAME);
            int maxCacheSize = getMemberIntValue(annotationNode, MAX_CACHE_SIZE_NAME);
            MethodCallExpression memoizeClosureCallExpression =
                    buildMemoizeClosureCallExpression(delegatingMethod, protectedCacheSize, maxCacheSize);

            String memoizedClosureFieldName = buildUniqueName(ownerClassNode, CLOSURE_LABEL, methodNode);
            FieldNode memoizedClosureField = new FieldNode(memoizedClosureFieldName, modifiers,
                    newClass(ClassHelper.CLOSURE_TYPE), null, memoizeClosureCallExpression);
            ownerClassNode.addField(memoizedClosureField);

            BlockStatement newCode = new BlockStatement();
            MethodCallExpression closureCallExpression = callX(
                    fieldX(memoizedClosureField), CLOSURE_CALL_METHOD_NAME, args(methodNode.getParameters()));
            closureCallExpression.setImplicitThis(false);
            newCode.addStatement(returnS(closureCallExpression));
            methodNode.setCode(newCode);
            VariableScopeVisitor visitor = new VariableScopeVisitor(source, ownerClassNode instanceof InnerClassNode);
            if (ownerClassNode instanceof InnerClassNode) {
                visitor.visitClass(((InnerClassNode) ownerClassNode).getOuterMostClass());
            } else {
                visitor.visitClass(ownerClassNode);
            }
        }
    }

    private static MethodNode buildDelegatingMethod(final MethodNode annotatedMethod, final ClassNode ownerClassNode) {
        Statement code = annotatedMethod.getCode();
        int access = ACC_PROTECTED;
        if (annotatedMethod.isStatic()) {
            access = ACC_PRIVATE | ACC_STATIC;
        }
        MethodNode method = new MethodNode(
                buildUniqueName(ownerClassNode, METHOD_LABEL, annotatedMethod),
                access,
                annotatedMethod.getReturnType(),
                cloneParams(annotatedMethod.getParameters()),
                annotatedMethod.getExceptions(),
                code
        );
        method.addAnnotations(filterAnnotations(annotatedMethod.getAnnotations()));
        return method;
    }

    private static List<AnnotationNode> filterAnnotations(List<AnnotationNode> annotations) {
        List<AnnotationNode> result = new ArrayList<AnnotationNode>(annotations.size());
        for (AnnotationNode annotation : annotations) {
            if (!OVERRIDE_CLASSNODE.equals(annotation.getClassNode())) {
                result.add(annotation);
            }
        }
        return result;
    }

    private static final String MEMOIZE_METHOD_NAME = "memoize";
    private static final String MEMOIZE_AT_MOST_METHOD_NAME = "memoizeAtMost";
    private static final String MEMOIZE_AT_LEAST_METHOD_NAME = "memoizeAtLeast";
    private static final String MEMOIZE_BETWEEN_METHOD_NAME = "memoizeBetween";

    private static MethodCallExpression buildMemoizeClosureCallExpression(MethodNode privateMethod,
                                                                   int protectedCacheSize, int maxCacheSize) {
        Parameter[] srcParams = privateMethod.getParameters();
        Parameter[] newParams = cloneParams(srcParams);
        List<Expression> argList = new ArrayList<Expression>(newParams.length);
        for (int i = 0; i < srcParams.length; i++) {
            argList.add(varX(newParams[i]));
        }

        ClosureExpression expression = new ClosureExpression(
                newParams,
                stmt(callThisX(privateMethod.getName(), args(argList)))
        );
        MethodCallExpression mce;
        if (protectedCacheSize == 0 && maxCacheSize == 0) {
            mce = callX(expression, MEMOIZE_METHOD_NAME);
        } else if (protectedCacheSize == 0) {
            mce = callX(expression, MEMOIZE_AT_MOST_METHOD_NAME, args(constX(maxCacheSize)));
        } else if (maxCacheSize == 0) {
            mce = callX(expression, MEMOIZE_AT_LEAST_METHOD_NAME, args(constX(protectedCacheSize)));
        } else {
            mce = callX(expression, MEMOIZE_BETWEEN_METHOD_NAME, args(constX(protectedCacheSize), constX(maxCacheSize)));
        }
        mce.setImplicitThis(false);
        return mce;
    }

    private static String buildUniqueName(ClassNode owner, String ident, MethodNode methodNode) {
        StringBuilder nameBuilder = new StringBuilder("memoizedMethod" + ident + "$").append(methodNode.getName());
        if (methodNode.getParameters() != null) {
            for (Parameter parameter : methodNode.getParameters()) {
                nameBuilder.append(buildTypeName(parameter.getType()));
            }
        }
        while (owner.getField(nameBuilder.toString()) != null) {
            nameBuilder.insert(0, "_");
        }

        return nameBuilder.toString();
    }

    private static String buildTypeName(ClassNode type) {
        if (type.isArray()) {
            return String.format("%sArray", buildTypeName(type.getComponentType()));
        }
        return type.getNameWithoutPackage();
    }

}
