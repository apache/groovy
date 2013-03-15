/*
 * Copyright 2008-2012 the original author or authors.
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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.Token;
import org.objectweb.asm.Opcodes;

import java.lang.ref.SoftReference;
import java.util.Arrays;

import static org.codehaus.groovy.transform.AbstractASTTransformUtil.assignStatement;
import static org.codehaus.groovy.transform.AbstractASTTransformUtil.declStatement;
import static org.codehaus.groovy.transform.AbstractASTTransformUtil.notNullExpr;

/**
 * Handles generation of code for the @Lazy annotation
 *
 * @author Alex Tkachman
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class LazyASTTransformation implements ASTTransformation, Opcodes {

    private static final ClassNode SOFT_REF = ClassHelper.makeWithoutCaching(SoftReference.class, false);
    private static final Expression NULL_EXPR = ConstantExpression.NULL;
    private static final Token ASSIGN = Token.newSymbol("=", -1, -1);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new GroovyBugError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];

        if (parent instanceof FieldNode) {
            final FieldNode fieldNode = (FieldNode) parent;
            final Expression soft = node.getMember("soft");
            final Expression init = getInitExpr(fieldNode);

            fieldNode.rename("$" + fieldNode.getName());
            fieldNode.setModifiers(ACC_PRIVATE | (fieldNode.getModifiers() & (~(ACC_PUBLIC | ACC_PROTECTED))));

            if (soft instanceof ConstantExpression && ((ConstantExpression) soft).getValue().equals(true)) {
                createSoft(fieldNode, init);
            }
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
        } else if (fieldNode.isVolatile()) {
            addDoubleCheckedLockingBody(body, fieldNode, initExpr);
        } else {
            addNonThreadSafeBody(body, fieldNode, initExpr);
        }
        addMethod(fieldNode, body, fieldNode.getType());
    }

    private void addHolderClassIdiomBody(BlockStatement body, FieldNode fieldNode, Expression initExpr) {
        final ClassNode declaringClass = fieldNode.getDeclaringClass();
        final ClassNode fieldType = fieldNode.getType();
        final int visibility = ACC_PRIVATE | ACC_STATIC;
        final String fullName = declaringClass.getName() + "$" + fieldType.getNameWithoutPackage() + "Holder_" + fieldNode.getName().substring(1);
        final InnerClassNode holderClass = new InnerClassNode(declaringClass, fullName, visibility, ClassHelper.OBJECT_TYPE);
        final String innerFieldName = "INSTANCE";
        holderClass.addField(innerFieldName, ACC_PRIVATE | ACC_STATIC | ACC_FINAL, fieldType, initExpr);
        final Expression innerField = new PropertyExpression(new ClassExpression(holderClass), innerFieldName);
        declaringClass.getModule().addClass(holderClass);
        body.addStatement(new ReturnStatement(innerField));
    }

    private void addDoubleCheckedLockingBody(BlockStatement body, FieldNode fieldNode, Expression initExpr) {
        final Expression fieldExpr = new VariableExpression(fieldNode);
        final VariableExpression localVar = new VariableExpression(fieldNode.getName() + "_local");
        body.addStatement(declStatement(localVar, fieldExpr));
        body.addStatement(new IfStatement(
                notNullExpr(localVar),
                new ReturnStatement(localVar),
                new SynchronizedStatement(
                        syncTarget(fieldNode),
                        new IfStatement(
                                notNullExpr(fieldExpr),
                                new ReturnStatement(fieldExpr),
                                new ReturnStatement(new BinaryExpression(fieldExpr, ASSIGN, initExpr))
                        )
                )
        ));
    }

    private void addNonThreadSafeBody(BlockStatement body, FieldNode fieldNode, Expression initExpr) {
        final Expression fieldExpr = new VariableExpression(fieldNode);
        body.addStatement(new IfStatement(
                notNullExpr(fieldExpr),
                new ExpressionStatement(fieldExpr),
                assignStatement(fieldExpr, initExpr)
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
        body.addStatement(declStatement(resExpr, callExpression));

        final BlockStatement elseBlock = new BlockStatement();
        elseBlock.addStatement(assignStatement(resExpr, initExpr));
        elseBlock.addStatement(assignStatement(fieldExpr, new ConstructorCallExpression(SOFT_REF, resExpr)));
        elseBlock.addStatement(new ExpressionStatement(resExpr));

        final Statement mainIf = new IfStatement(notNullExpr(resExpr), new ExpressionStatement(resExpr), elseBlock);

        if (fieldNode.isVolatile()) {
            final BlockStatement mainIfBlock = new BlockStatement();
            mainIfBlock.addStatement( assignStatement(resExpr, callExpression) );
            mainIfBlock.addStatement( mainIf );
            body.addStatement(new IfStatement(
                    notNullExpr(resExpr),
                    new ExpressionStatement(resExpr),
                    new SynchronizedStatement(syncTarget(fieldNode), mainIfBlock)
            ));
        } else {
            body.addStatement(mainIf);
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
                notNullExpr(paramExpr),
                assignStatement(fieldExpr, new ConstructorCallExpression(SOFT_REF, paramExpr)),
                assignStatement(fieldExpr, NULL_EXPR)
        ));
        int visibility = ACC_PUBLIC;
        if (fieldNode.isStatic()) visibility |= ACC_STATIC;
        fieldNode.getDeclaringClass().addMethod(name, visibility, ClassHelper.VOID_TYPE, new Parameter[]{parameter}, ClassNode.EMPTY_ARRAY, body);
    }

    private Expression syncTarget(FieldNode fieldNode) {
        return fieldNode.isStatic() ? new ClassExpression(fieldNode.getDeclaringClass()) : VariableExpression.THIS_EXPRESSION;
    }

    private Expression getInitExpr(FieldNode fieldNode) {
        Expression initExpr = fieldNode.getInitialValueExpression();
        fieldNode.setInitialValueExpression(null);

        if (initExpr == null)
            initExpr = new ConstructorCallExpression(fieldNode.getType(), new ArgumentListExpression());

        return initExpr;
    }
}
