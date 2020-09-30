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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.lang.ref.SoftReference;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.apache.groovy.util.BeanUtils.capitalize;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifElseS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

/**
 * Handles generation of code for the @Lazy annotation
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class LazyASTTransformation extends AbstractASTTransformation {

    private static final ClassNode SOFT_REF = makeWithoutCaching(SoftReference.class, false);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];

        if (parent instanceof FieldNode) {
            final FieldNode fieldNode = (FieldNode) parent;
            visitField(this, node, fieldNode);
        }
    }

    static void visitField(ErrorCollecting xform, AnnotationNode node, FieldNode fieldNode) {
        final Expression soft = node.getMember("soft");
        final Expression init = getInitExpr(xform, fieldNode);

        String backingFieldName = "$" + fieldNode.getName();
        fieldNode.rename(backingFieldName);
        fieldNode.setModifiers(ACC_PRIVATE | ACC_SYNTHETIC | (fieldNode.getModifiers() & (~(ACC_PUBLIC | ACC_PROTECTED))));
        PropertyNode pNode = fieldNode.getDeclaringClass().getProperty(backingFieldName);
        if (pNode != null) {
            fieldNode.getDeclaringClass().getProperties().remove(pNode);
        }

        if (soft instanceof ConstantExpression && ((ConstantExpression) soft).getValue().equals(true)) {
            createSoft(fieldNode, init);
        } else {
            create(fieldNode, init);
            // @Lazy not meaningful with primitive so convert to wrapper if needed
            if (ClassHelper.isPrimitiveType(fieldNode.getType())) {
                fieldNode.setType(ClassHelper.getWrapper(fieldNode.getType()));
            }
        }
    }

    private static void create(FieldNode fieldNode, final Expression initExpr) {
        final BlockStatement body = new BlockStatement();
        if (fieldNode.isStatic()) {
            addHolderClassIdiomBody(body, fieldNode, initExpr);
        } else if (fieldNode.isVolatile()) {
            addDoubleCheckedLockingBody(body, fieldNode, initExpr);
        } else {
            addNonThreadSafeBody(body, fieldNode, initExpr);
        }
        addMethod(fieldNode, body, fieldNode.getType());
    }

    private static void addHolderClassIdiomBody(BlockStatement body, FieldNode fieldNode, Expression initExpr) {
        final ClassNode declaringClass = fieldNode.getDeclaringClass();
        final ClassNode fieldType = fieldNode.getType();
        final int visibility = ACC_PRIVATE | ACC_STATIC;
        final String fullName = declaringClass.getName() + "$" + fieldType.getNameWithoutPackage() + "Holder_" + fieldNode.getName().substring(1);
        final InnerClassNode holderClass = new InnerClassNode(declaringClass, fullName, visibility, ClassHelper.OBJECT_TYPE);
        final String innerFieldName = "INSTANCE";

        // we have two options:
        // (1) embed initExpr within holder class but redirect field access/method calls to declaring class members
        // (2) keep initExpr within a declaring class method that is only called by the holder class
        // currently we have gone with (2) for simplicity with only a slight memory footprint increase in the declaring class
        final String initializeMethodName = (fullName + "_initExpr").replace('.', '_');
        addGeneratedMethod(declaringClass, initializeMethodName, ACC_PRIVATE | ACC_STATIC | ACC_FINAL, fieldType,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, returnS(initExpr));
        holderClass.addField(innerFieldName, ACC_PRIVATE | ACC_STATIC | ACC_FINAL, fieldType,
                callX(declaringClass, initializeMethodName));

        final Expression innerField = propX(classX(holderClass), innerFieldName);
        declaringClass.getModule().addClass(holderClass);
        body.addStatement(returnS(innerField));
    }

    private static void addDoubleCheckedLockingBody(BlockStatement body, FieldNode fieldNode, Expression initExpr) {
        final Expression fieldExpr = varX(fieldNode);
        final VariableExpression localVar = localVarX(fieldNode.getName() + "_local");
        body.addStatement(declS(localVar, fieldExpr));
        body.addStatement(ifElseS(
                notNullX(localVar),
                returnS(localVar),
                new SynchronizedStatement(
                        syncTarget(fieldNode),
                        ifElseS(
                                notNullX(fieldExpr),
                                returnS(fieldExpr),
                                returnS(assignX(fieldExpr, initExpr))
                        )
                )
        ));
    }

    private static void addNonThreadSafeBody(BlockStatement body, FieldNode fieldNode, Expression initExpr) {
        final Expression fieldExpr = varX(fieldNode);
        body.addStatement(ifElseS(notNullX(fieldExpr), stmt(fieldExpr), assignS(fieldExpr, initExpr)));
    }

    private static void addMethod(FieldNode fieldNode, BlockStatement body, ClassNode type) {
        int visibility = ACC_PUBLIC;
        if (fieldNode.isStatic()) visibility |= ACC_STATIC;
        String propName = capitalize(fieldNode.getName().substring(1));
        ClassNode declaringClass = fieldNode.getDeclaringClass();
        addGeneratedMethod(declaringClass, "get" + propName, visibility, type, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
        if (ClassHelper.boolean_TYPE.equals(type)) {
            addGeneratedMethod(declaringClass, "is" + propName, visibility, type,
                    Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, stmt(callThisX("get" + propName)));
        }
    }

    private static void createSoft(FieldNode fieldNode, Expression initExpr) {
        final ClassNode type = fieldNode.getType();
        fieldNode.setType(SOFT_REF);
        createSoftGetter(fieldNode, initExpr, type);
        createSoftSetter(fieldNode, type);
    }

    private static void createSoftGetter(FieldNode fieldNode, Expression initExpr, ClassNode type) {
        final BlockStatement body = new BlockStatement();
        final Expression fieldExpr = varX(fieldNode);
        final Expression resExpr = localVarX("_result", type);
        final MethodCallExpression callExpression = callX(fieldExpr, "get");
        callExpression.setSafe(true);
        body.addStatement(declS(resExpr, callExpression));

        final Statement mainIf = ifElseS(notNullX(resExpr), stmt(resExpr), block(
                assignS(resExpr, initExpr),
                assignS(fieldExpr, ctorX(SOFT_REF, resExpr)),
                stmt(resExpr)));

        if (fieldNode.isVolatile()) {
            body.addStatement(ifElseS(
                    notNullX(resExpr),
                    stmt(resExpr),
                    new SynchronizedStatement(syncTarget(fieldNode), block(
                            assignS(resExpr, callExpression),
                            mainIf)
                    )
            ));
        } else {
            body.addStatement(mainIf);
        }
        addMethod(fieldNode, body, type);
    }

    private static void createSoftSetter(FieldNode fieldNode, ClassNode type) {
        final BlockStatement body = new BlockStatement();
        final Expression fieldExpr = varX(fieldNode);
        final String name = getSetterName(fieldNode.getName().substring(1));
        final Parameter parameter = param(type, "value");
        final Expression paramExpr = varX(parameter);
        body.addStatement(ifElseS(
                notNullX(paramExpr),
                assignS(fieldExpr, ctorX(SOFT_REF, paramExpr)),
                assignS(fieldExpr, nullX())
        ));
        int visibility = ACC_PUBLIC;
        if (fieldNode.isStatic()) visibility |= ACC_STATIC;
        ClassNode declaringClass = fieldNode.getDeclaringClass();
        addGeneratedMethod(declaringClass, name, visibility, ClassHelper.VOID_TYPE, params(parameter), ClassNode.EMPTY_ARRAY, body);
    }

    private static Expression syncTarget(FieldNode fieldNode) {
        return fieldNode.isStatic() ? classX(fieldNode.getDeclaringClass()) : varX("this");
    }

    private static Expression getInitExpr(ErrorCollecting xform, FieldNode fieldNode) {
        Expression initExpr = fieldNode.getInitialValueExpression();
        fieldNode.setInitialValueExpression(null);

        if (initExpr == null || initExpr instanceof EmptyExpression) {
            if (fieldNode.getType().isAbstract()) {
                xform.addError("You cannot lazily initialize '" + fieldNode.getName() + "' from the abstract class '" +
                        fieldNode.getType().getName() + "'", fieldNode);
            }
            initExpr = ctorX(fieldNode.getType());
        }

        return initExpr;
    }
}
