/*
 * Copyright 2008 the original author or authors.
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
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.objectweb.asm.Opcodes;

import java.lang.ref.SoftReference;

/**
 * Handles generation of code for the @Singleton annotation
 *
 * @author Alex Tkachman
 */
@GroovyASTTransformation(phase= CompilePhase.CANONICALIZATION)
public class LazyASTTransformation implements ASTTransformation, Opcodes {

    final static ClassNode SOFT_REF = ClassHelper.make(SoftReference.class);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (!(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException("Internal error: wrong types: $node.class / $parent.class");
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];

        if (parent instanceof FieldNode) {
            FieldNode fieldNode = (FieldNode) parent;
            final Expression member = node.getMember("soft");
            final Expression init = getInitExpr(fieldNode);

            fieldNode.rename("$" + fieldNode.getName());
            fieldNode.setModifiers(ACC_PRIVATE | (fieldNode.getModifiers() & (~(ACC_PUBLIC|ACC_PROTECTED))));

            if(member instanceof ConstantExpression && ((ConstantExpression)member).getValue().equals(true))
               createSoft(fieldNode, init);
            else {
               create(fieldNode, init);
            }
        }
    }

    private void create(FieldNode fieldNode, final Expression initExpr) {

        BlockStatement body = new BlockStatement();
        final FieldExpression fieldExpr = new FieldExpression(fieldNode);
        if ((fieldNode.getModifiers() & ACC_VOLATILE) == 0) {
            body.addStatement(new IfStatement(
                    new BooleanExpression(new BinaryExpression(fieldExpr, Token.newSymbol("!=",-1,-1), ConstantExpression.NULL)),
                    new ExpressionStatement(fieldExpr),
                    new ExpressionStatement(new BinaryExpression(fieldExpr, Token.newSymbol("=",-1,-1), initExpr))
            ));
        }
        else {
            body.addStatement(new IfStatement(
                new BooleanExpression(new BinaryExpression(fieldExpr, Token.newSymbol("!=",-1,-1), ConstantExpression.NULL)),
                new ReturnStatement(fieldExpr),
                new SynchronizedStatement(
                        VariableExpression.THIS_EXPRESSION,
                        new IfStatement(
                                new BooleanExpression(new BinaryExpression(fieldExpr, Token.newSymbol("!=",-1,-1), ConstantExpression.NULL)),
                                    new ReturnStatement(fieldExpr),
                                    new ReturnStatement(new BinaryExpression(fieldExpr,Token.newSymbol("=",-1,-1), initExpr))
                        )
                )
            ));
        }
        final String name = "get" + fieldNode.getName().substring(1, 2).toUpperCase() + fieldNode.getName().substring(2);
        fieldNode.getDeclaringClass().addMethod(name, ACC_PUBLIC, fieldNode.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private void createSoft(FieldNode fieldNode, Expression initExpr) {
        ClassNode type = fieldNode.getType();

        fieldNode.setType(SOFT_REF);

        createSoftGetter(fieldNode, initExpr, type);
        createSoftSetter(fieldNode, type);
    }

    private void createSoftGetter(FieldNode fieldNode, Expression initExpr, ClassNode type) {
        BlockStatement body = new BlockStatement();
        final FieldExpression fieldExpr = new FieldExpression(fieldNode);

        final VariableExpression resExpr = new VariableExpression("res", type);
        final MethodCallExpression callExpression = new MethodCallExpression(new FieldExpression(fieldNode), "get", new ArgumentListExpression());
        callExpression.setSafe(true);
        body.addStatement(new ExpressionStatement(new DeclarationExpression(resExpr, Token.newSymbol("=",-1,-1), callExpression)));

        BlockStatement elseBlock = new BlockStatement();
        elseBlock.addStatement(new ExpressionStatement(new BinaryExpression(resExpr, Token.newSymbol("=",-1,-1), initExpr)));
        elseBlock.addStatement(new ExpressionStatement(new BinaryExpression(fieldExpr, Token.newSymbol("=",-1,-1), new ConstructorCallExpression(SOFT_REF, resExpr))));
        elseBlock.addStatement(new ExpressionStatement(resExpr));

        final IfStatement mainIf = new IfStatement(
                new BooleanExpression(new BinaryExpression(resExpr, Token.newSymbol("!=", -1, -1), ConstantExpression.NULL)),
                new ExpressionStatement(resExpr),
                elseBlock
        );

        if ((fieldNode.getModifiers() & ACC_VOLATILE) == 0) {
            body.addStatement(mainIf);
        }
        else {
            body.addStatement(new IfStatement(
                    new BooleanExpression(new BinaryExpression(resExpr, Token.newSymbol("!=",-1,-1), ConstantExpression.NULL)),
                    new ExpressionStatement(resExpr),
                    new SynchronizedStatement(
                            VariableExpression.THIS_EXPRESSION,
                            mainIf
                    )
            ));
        }
        final String name = "get" + fieldNode.getName().substring(1, 2).toUpperCase() + fieldNode.getName().substring(2);
        fieldNode.getDeclaringClass().addMethod(name, ACC_PUBLIC, type, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private void createSoftSetter(FieldNode fieldNode, ClassNode type) {
        BlockStatement body = new BlockStatement();
        final FieldExpression fieldExpr = new FieldExpression(fieldNode);
        final String name = "set" + fieldNode.getName().substring(1, 2).toUpperCase() + fieldNode.getName().substring(2);
        final Parameter parameter = new Parameter(type, "value");
        final VariableExpression paramExpr = new VariableExpression(parameter);
        body.addStatement(new IfStatement(
                new BooleanExpression(new BinaryExpression(paramExpr, Token.newSymbol("!=",-1,-1), ConstantExpression.NULL)),
                new ExpressionStatement(new BinaryExpression(fieldExpr, Token.newSymbol("=",-1,-1), new ConstructorCallExpression(SOFT_REF, paramExpr))),
                new ExpressionStatement(new BinaryExpression(fieldExpr, Token.newSymbol("=",-1,-1), ConstantExpression.NULL))
        ));
        fieldNode.getDeclaringClass().addMethod(name, ACC_PUBLIC, ClassHelper.VOID_TYPE, new Parameter[] {parameter}, ClassNode.EMPTY_ARRAY, body);
    }

    private Expression getInitExpr(FieldNode fieldNode) {
        Expression initExpr = fieldNode.getInitialValueExpression();
        fieldNode.setInitialValueExpression(null);

        if (initExpr == null)
          initExpr = new ConstructorCallExpression(fieldNode.getType(), new ArgumentListExpression());

        return initExpr;
    }
}