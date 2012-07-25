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
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Iterator;

/**
 * Handles generation of code for the @Singleton annotation
 *
 * @author Alex Tkachman
 */
@GroovyASTTransformation(phase= CompilePhase.CANONICALIZATION)
public class SingletonASTTransformation implements ASTTransformation, Opcodes {

    private String propertyName = "instance";

    /**
     *
     * @param nodes   the ast nodes
     * @param source  the source unit for the nodes
     */
    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (!(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException(
                String.format(
                    "Internal error: wrong types: %s / %s. Expected: AnnotationNode / AnnotatedNode", 
                    nodes[0].getClass(), 
                    nodes[1].getClass())
                );
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];

        if (parent instanceof ClassNode) {
            ClassNode classNode = (ClassNode) parent;
            final Expression fieldNameAttribute = node.getMember("property");
            if (fieldNameAttribute instanceof ConstantExpression)
                propertyName = (String) ((ConstantExpression) fieldNameAttribute).getValue();
            final Expression member = node.getMember("lazy");
            if(member instanceof ConstantExpression && ((ConstantExpression)member).getValue().equals(true))
               createLazy(classNode);
            else
               createNonLazy(classNode);
        }
    }

    private void createNonLazy(ClassNode classNode) {
        final FieldNode fieldNode = classNode.addField(propertyName, ACC_PUBLIC | ACC_FINAL | ACC_STATIC, classNode.getPlainNodeReference(), new ConstructorCallExpression(classNode, new ArgumentListExpression()));
        createConstructor(classNode, fieldNode);

        final BlockStatement body = new BlockStatement();
        body.addStatement(new ReturnStatement(new VariableExpression(fieldNode)));
        classNode.addMethod(getGetterName(), ACC_STATIC | ACC_PUBLIC, classNode.getPlainNodeReference(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private String getGetterName() {
        return "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    }

    private void createLazy(ClassNode classNode) {
        final FieldNode fieldNode = classNode.addField(propertyName, ACC_PRIVATE | ACC_STATIC | ACC_VOLATILE, classNode.getPlainNodeReference(), null);
        createConstructor(classNode, fieldNode);

        final BlockStatement body = new BlockStatement();
        final Expression instanceExpression = new VariableExpression(fieldNode);
        body.addStatement(new IfStatement(
            new BooleanExpression(new BinaryExpression(instanceExpression, Token.newSymbol("!=",-1,-1), ConstantExpression.NULL)),
            new ReturnStatement(instanceExpression),
            new SynchronizedStatement(
                    new ClassExpression(classNode),
                    new IfStatement(
                            new BooleanExpression(new BinaryExpression(instanceExpression, Token.newSymbol("!=",-1,-1), ConstantExpression.NULL)),
                                new ReturnStatement(instanceExpression),
                                new ReturnStatement(new BinaryExpression(instanceExpression,Token.newSymbol("=",-1,-1), new ConstructorCallExpression(classNode, new ArgumentListExpression())))
                    )
            )
        ));
        classNode.addMethod(getGetterName(), ACC_STATIC | ACC_PUBLIC, classNode.getPlainNodeReference(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private void createConstructor(ClassNode classNode, FieldNode field) {

        final List list = classNode.getDeclaredConstructors();
        MethodNode found = null;
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            MethodNode mn = (MethodNode) it.next();
            final Parameter[] parameters = mn.getParameters();
            if (parameters == null || parameters.length == 0) {
                found = mn;
                break;
            }
        }

        if (found == null) {
            final BlockStatement body = new BlockStatement();
            body.addStatement(new IfStatement(
                    new BooleanExpression(new BinaryExpression(new VariableExpression(field), Token.newSymbol("!=",-1,-1), ConstantExpression.NULL)),
                new ThrowStatement(
                        new ConstructorCallExpression(ClassHelper.make(RuntimeException.class),
                                new ArgumentListExpression(
                                            new ConstantExpression("Can't instantiate singleton " + classNode.getName() + ". Use " + classNode.getName() + "." + propertyName)))),
                new EmptyStatement()));
            classNode.addConstructor(new ConstructorNode(ACC_PRIVATE, body));
        }
    }
}