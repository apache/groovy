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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.List;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedConstructor;
import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifElseS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.newClass;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_VOLATILE;

/**
 * Handles generation of code for the @Singleton annotation
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
        Expression initialValue = isLazy ? null : ctorX(classNode);
        final FieldNode fieldNode = classNode.addField(propertyName, modifiers, newClass(classNode), initialValue);
        createConstructor(classNode, fieldNode, propertyName, isStrict);
        final BlockStatement body = new BlockStatement();
        body.addStatement(isLazy ? lazyBody(classNode, fieldNode) : nonLazyBody(fieldNode));
        addGeneratedMethod(classNode, getGetterName(propertyName), ACC_STATIC | ACC_PUBLIC, newClass(classNode), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private static Statement nonLazyBody(FieldNode fieldNode) {
        return returnS(varX(fieldNode));
    }

    private static Statement lazyBody(ClassNode classNode, FieldNode fieldNode) {
        final Expression instanceExpression = varX(fieldNode);
        return ifElseS(
                notNullX(instanceExpression),
                returnS(instanceExpression),
                new SynchronizedStatement(
                        classX(classNode),
                        ifElseS(
                                notNullX(instanceExpression),
                                returnS(instanceExpression),
                                returnS(assignX(instanceExpression, ctorX(classNode)))
                        )
                )
        );
    }

    private static String getGetterName(String propertyName) {
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

        if (isStrict && !cNodes.isEmpty()) {
            for (ConstructorNode cNode : cNodes) {
                addError("@Singleton didn't expect to find one or more additional constructors: remove constructor(s) or set strict=false", cNode);
            }
        }

        if (foundNoArg == null) {
            final BlockStatement body = new BlockStatement();
            body.addStatement(ifS(
                    notNullX(varX(field)),
                    throwS(
                            ctorX(make(RuntimeException.class),
                                    args(constX("Can't instantiate singleton " + classNode.getName() + ". Use " + classNode.getName() + "." + propertyName))))
            ));
            addGeneratedConstructor(classNode, new ConstructorNode(ACC_PRIVATE, body));
        }
    }
}