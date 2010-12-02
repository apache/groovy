/*
 * Copyright 2008-2010 the original author or authors.
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

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.Token;
import org.objectweb.asm.Opcodes;

import java.lang.ref.SoftReference;
import java.util.Arrays;

/**
 * Handles generation of code for the @Lazy annotation
 *
 * @author Alex Tkachman
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class LazyASTTransformation implements ASTTransformation, Opcodes {

    private static final ClassNode SOFT_REF = ClassHelper.make(SoftReference.class);
    private static final Expression NULL_EXPR = ConstantExpression.NULL;
    private static final ClassNode OBJECT_TYPE = new ClassNode(Object.class);
    private static final Token ASSIGN = Token.newSymbol("=", -1, -1);
    private static final Token COMPARE_NOT_EQUAL = Token.newSymbol("!=", -1, -1);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];

        if (parent instanceof FieldNode) {
            final FieldNode fieldNode = (FieldNode) parent;
            final Expression member = node.getMember("soft");
            final Expression init = getInitExpr(fieldNode);

            fieldNode.rename("$" + fieldNode.getName());
            fieldNode.setModifiers(ACC_PRIVATE | (fieldNode.getModifiers() & (~(ACC_PUBLIC | ACC_PROTECTED))));
            
            if (member instanceof ConstantExpression && ((ConstantExpression) member).getValue().equals(true))
                createSoft(fieldNode, init);
            else {
                create(fieldNode, init);
                // @Lazy not meaningful with primitive so convert to wrapper if needed
                if (ClassHelper.isPrimitiveType(fieldNode.getType())) {
                    fieldNode.setType(ClassHelper.getWrapper(fieldNode.getType()));
                }
            }
        }
    }

    private void create(FieldNode fieldNode, final Expression initExpr) {
        final BlockStatement body = new BlockStatement();
        if (fieldNode.isStatic()) {
            addHolderClassIdiomBody(body, fieldNode, initExpr);
        } else if (isVolatile(fieldNode)) {
            addNonThreadSafeBody(body, fieldNode, initExpr);
        } else {
            addDoubleCheckedLockingBody(body, fieldNode, initExpr);
        }
        addMethod(fieldNode, body, fieldNode.getType());
    }

    private void addHolderClassIdiomBody(BlockStatement body, FieldNode fieldNode, Expression initExpr) {
        final ClassNode declaringClass = fieldNode.getDeclaringClass();
        final ClassNode fieldType = fieldNode.getType();
        final int visibility = ACC_PRIVATE | ACC_STATIC;
        final String fullName = declaringClass.getName() + "$" + fieldType.getNameWithoutPackage() + "Holder_" + fieldNode.getName().substring(1);
        final InnerClassNode holderClass = new InnerClassNode(declaringClass, fullName, visibility, OBJECT_TYPE);
        final String innerFieldName = "INSTANCE";
        holderClass.addField(innerFieldName, ACC_PRIVATE | ACC_STATIC | ACC_FINAL, fieldType, initExpr);
        final Expression innerField = new PropertyExpression(new ClassExpression(holderClass), innerFieldName);
        declaringClass.getModule().addClass(holderClass);
        body.addStatement(new ReturnStatement(innerField));
    }

    private void addDoubleCheckedLockingBody(BlockStatement body, FieldNode fieldNode, Expression initExpr) {
        final Expression fieldExpr = new VariableExpression(fieldNode);
        final VariableExpression localVar = new VariableExpression(fieldNode.getName() + "_local");
        body.addStatement(new ExpressionStatement(new DeclarationExpression(localVar, ASSIGN, fieldExpr)));
        body.addStatement(new IfStatement(
                new BooleanExpression(new BinaryExpression(localVar, COMPARE_NOT_EQUAL, NULL_EXPR)),
                new ReturnStatement(localVar),
                new SynchronizedStatement(
                        synchTarget(fieldNode),
                        new IfStatement(
                                new BooleanExpression(new BinaryExpression(fieldExpr, COMPARE_NOT_EQUAL, NULL_EXPR)),
                                new ReturnStatement(fieldExpr),
                                new ReturnStatement(new BinaryExpression(fieldExpr, ASSIGN, initExpr))
                        )
                )
        ));
    }

    private void addNonThreadSafeBody(BlockStatement body, FieldNode fieldNode, Expression initExpr) {
        final Expression fieldExpr = new VariableExpression(fieldNode);
        body.addStatement(new IfStatement(
                new BooleanExpression(new BinaryExpression(fieldExpr, COMPARE_NOT_EQUAL, NULL_EXPR)),
                new ExpressionStatement(fieldExpr),
                new ExpressionStatement(new BinaryExpression(fieldExpr, ASSIGN, initExpr))
        ));
    }

    private void addMethod(FieldNode fieldNode, BlockStatement body, ClassNode type) {
        int visibility = ACC_PUBLIC;
        if (fieldNode.isStatic()) visibility |= ACC_STATIC;
        final String name = "get" + MetaClassHelper.capitalize(fieldNode.getName().substring(1));
        fieldNode.getDeclaringClass().addMethod(name, visibility, type, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private void createSoft(FieldNode fieldNode, Expression initExpr) {
        final ClassNode type = fieldNode.getType();
        fieldNode.setType(SOFT_REF);
        createSoftGetter(fieldNode, initExpr, type);
        createSoftSetter(fieldNode, type);
    }

    private void createSoftGetter(FieldNode fieldNode, Expression initExpr, ClassNode type) {
        final BlockStatement body = new BlockStatement();
        final Expression fieldExpr = new VariableExpression(fieldNode);
        final Expression resExpr = new VariableExpression("res", type);
        final MethodCallExpression callExpression = new MethodCallExpression(fieldExpr, "get", new ArgumentListExpression());
        callExpression.setSafe(true);
        body.addStatement(new ExpressionStatement(new DeclarationExpression(resExpr, ASSIGN, callExpression)));

        final BlockStatement elseBlock = new BlockStatement();
        elseBlock.addStatement(new ExpressionStatement(new BinaryExpression(resExpr, ASSIGN, initExpr)));
        elseBlock.addStatement(new ExpressionStatement(new BinaryExpression(fieldExpr, ASSIGN, new ConstructorCallExpression(SOFT_REF, resExpr))));
        elseBlock.addStatement(new ExpressionStatement(resExpr));

        final Statement mainIf = new IfStatement(
                new BooleanExpression(new BinaryExpression(resExpr, COMPARE_NOT_EQUAL, NULL_EXPR)),
                new ExpressionStatement(resExpr),
                elseBlock
        );

        if (isVolatile(fieldNode)) {
            body.addStatement(mainIf);
        } else {
            body.addStatement(new IfStatement(
                    new BooleanExpression(new BinaryExpression(resExpr, COMPARE_NOT_EQUAL, NULL_EXPR)),
                    new ExpressionStatement(resExpr),
                    new SynchronizedStatement(synchTarget(fieldNode), mainIf)
            ));
        }
        addMethod(fieldNode, body, type);
    }

    private void createSoftSetter(FieldNode fieldNode, ClassNode type) {
        final BlockStatement body = new BlockStatement();
        final Expression fieldExpr = new VariableExpression(fieldNode);
        final String name = "set" + MetaClassHelper.capitalize(fieldNode.getName().substring(1));
        final Parameter parameter = new Parameter(type, "value");
        final Expression paramExpr = new VariableExpression(parameter);
        body.addStatement(new IfStatement(
                new BooleanExpression(new BinaryExpression(paramExpr, COMPARE_NOT_EQUAL, NULL_EXPR)),
                new ExpressionStatement(new BinaryExpression(fieldExpr, ASSIGN, new ConstructorCallExpression(SOFT_REF, paramExpr))),
                new ExpressionStatement(new BinaryExpression(fieldExpr, ASSIGN, NULL_EXPR))
        ));
        int visibility = ACC_PUBLIC;
        if (fieldNode.isStatic()) visibility |= ACC_STATIC;
        fieldNode.getDeclaringClass().addMethod(name, visibility, ClassHelper.VOID_TYPE, new Parameter[]{parameter}, ClassNode.EMPTY_ARRAY, body);
    }

    private Expression synchTarget(FieldNode fieldNode) {
        return fieldNode.isStatic() ? new ClassExpression(fieldNode.getDeclaringClass()) : VariableExpression.THIS_EXPRESSION;
    }

    private boolean isVolatile(FieldNode fieldNode) {
        return (fieldNode.getModifiers() & ACC_VOLATILE) == 0;
    }

    private Expression getInitExpr(FieldNode fieldNode) {
        Expression initExpr = fieldNode.getInitialValueExpression();
        fieldNode.setInitialValueExpression(null);

        if (initExpr == null)
            initExpr = new ConstructorCallExpression(fieldNode.getType(), new ArgumentListExpression());

        return initExpr;
    }
}