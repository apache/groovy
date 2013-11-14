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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;

import java.util.List;

/**
 * Handles generation of code for the @Singleton annotation
 *
 * @author Alex Tkachman
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class SingletonASTTransformation extends AbstractASTTransformation {

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];

        if (parent instanceof ClassNode) {
            ClassNode classNode = (ClassNode) parent;
            String propertyName = getMemberStringValue(node, "property", "instance");
            boolean isLazy = memberHasValue(node, "lazy", true);
            boolean isStrict = !memberHasValue(node, "strict", false);
            createField(classNode, propertyName, isLazy, isStrict);
        }
    }

    private void createField(ClassNode classNode, String propertyName, boolean isLazy, boolean isStrict) {
        int modifiers = isLazy ? ACC_PRIVATE | ACC_STATIC | ACC_VOLATILE : ACC_PUBLIC | ACC_FINAL | ACC_STATIC;
        Expression initialValue = isLazy ? null : new ConstructorCallExpression(classNode, new ArgumentListExpression());
        final FieldNode fieldNode = classNode.addField(propertyName, modifiers, classNode.getPlainNodeReference(), initialValue);
        createConstructor(classNode, fieldNode, propertyName, isStrict);
        final BlockStatement body = new BlockStatement();
        body.addStatement(isLazy ? lazyBody(classNode, fieldNode) : nonLazyBody(fieldNode));
        classNode.addMethod(getGetterName(propertyName), ACC_STATIC | ACC_PUBLIC, classNode.getPlainNodeReference(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private Statement nonLazyBody(FieldNode fieldNode) {
        return new ReturnStatement(new VariableExpression(fieldNode));
    }

    private Statement lazyBody(ClassNode classNode, FieldNode fieldNode) {
        final Expression instanceExpression = new VariableExpression(fieldNode);
        return new IfStatement(
                new BooleanExpression(new BinaryExpression(instanceExpression, Token.newSymbol("!=", -1, -1), ConstantExpression.NULL)),
                new ReturnStatement(instanceExpression),
                new SynchronizedStatement(
                        new ClassExpression(classNode),
                        new IfStatement(
                                new BooleanExpression(new BinaryExpression(instanceExpression, Token.newSymbol("!=", -1, -1), ConstantExpression.NULL)),
                                new ReturnStatement(instanceExpression),
                                new ReturnStatement(new BinaryExpression(instanceExpression, Token.newSymbol("=", -1, -1), new ConstructorCallExpression(classNode, new ArgumentListExpression())))
                        )
                )
        );
    }

    private String getGetterName(String propertyName) {
        return "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    }

    private void createConstructor(ClassNode classNode, FieldNode field, String propertyName, boolean isStrict) {
        final List<ConstructorNode> cNodes = classNode.getDeclaredConstructors();
        ConstructorNode foundNoArg = null;
        for (ConstructorNode cNode : cNodes) {
            final Parameter[] parameters = cNode.getParameters();
            if (parameters == null || parameters.length == 0) {
                foundNoArg = cNode;
                break;
            }
        }

        if (isStrict && cNodes.size() != 0) {
            for (ConstructorNode cNode : cNodes) {
                addError("@Singleton didn't expect to find one or more additional constructors: remove constructor(s) or set strict=false", cNode);
            }
        }

        if (foundNoArg == null) {
            final BlockStatement body = new BlockStatement();
            body.addStatement(new IfStatement(
                    new BooleanExpression(new BinaryExpression(new VariableExpression(field), Token.newSymbol("!=", -1, -1), ConstantExpression.NULL)),
                    new ThrowStatement(
                            new ConstructorCallExpression(ClassHelper.make(RuntimeException.class),
                                    new ArgumentListExpression(
                                            new ConstantExpression("Can't instantiate singleton " + classNode.getName() + ". Use " + classNode.getName() + "." + propertyName)))),
                    EmptyStatement.INSTANCE));
            classNode.addConstructor(new ConstructorNode(ACC_PRIVATE, body));
        }
    }
}