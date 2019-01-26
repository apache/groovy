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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

public abstract class InnerClassVisitorHelper extends ClassCodeVisitorSupport {
    protected static void setPropertyGetterDispatcher(BlockStatement block, Expression thiz, Parameter[] parameters) {
        List<ConstantExpression> gStringStrings = new ArrayList<>();
        gStringStrings.add(new ConstantExpression(""));
        gStringStrings.add(new ConstantExpression(""));
        List<Expression> gStringValues = new ArrayList<>();
        gStringValues.add(new VariableExpression(parameters[0]));
        block.addStatement(
                new ReturnStatement(
                        new PropertyExpression(
                                thiz,
                                new GStringExpression("$name", gStringStrings, gStringValues)
                        )
                )
        );
    }

    protected static void setPropertySetterDispatcher(BlockStatement block, Expression thiz, Parameter[] parameters) {
        List<ConstantExpression> gStringStrings = new ArrayList<>();
        gStringStrings.add(new ConstantExpression(""));
        gStringStrings.add(new ConstantExpression(""));
        List<Expression> gStringValues = new ArrayList<>();
        gStringValues.add(new VariableExpression(parameters[0]));
        block.addStatement(
                new ExpressionStatement(
                        new BinaryExpression(
                                new PropertyExpression(
                                        thiz,
                                        new GStringExpression("$name", gStringStrings, gStringValues)
                                ),
                                Token.newSymbol(Types.ASSIGN, -1, -1),
                                new VariableExpression(parameters[1])
                        )
                )
        );
    }

    protected static void setMethodDispatcherCode(BlockStatement block, Expression thiz, Parameter[] parameters) {
        List<ConstantExpression> gStringStrings = new ArrayList<>();
        gStringStrings.add(new ConstantExpression(""));
        gStringStrings.add(new ConstantExpression(""));
        List<Expression> gStringValues = new ArrayList<>();
        gStringValues.add(new VariableExpression(parameters[0]));
        block.addStatement(
                new ReturnStatement(
                        new MethodCallExpression(
                                thiz,
                                new GStringExpression("$name", gStringStrings, gStringValues),
                                new ArgumentListExpression(
                                        new SpreadExpression(new VariableExpression(parameters[1]))
                                )
                        )
                )
        );
    }

    protected static boolean isStatic(InnerClassNode node) {
        VariableScope scope = node.getVariableScope();
        if (scope != null) return scope.isInStaticContext();
        return (node.getModifiers() & Opcodes.ACC_STATIC) != 0;
    }

    protected static ClassNode getClassNode(ClassNode node, boolean isStatic) {
        if (isStatic) node = ClassHelper.CLASS_Type;
        return node;
    }

    protected static int getObjectDistance(ClassNode node) {
        int count = 0;
        while (node != null && node != ClassHelper.OBJECT_TYPE) {
            count++;
            node = node.getSuperClass();
        }
        return count;
    }

    protected static void addFieldInit(Parameter p, FieldNode fn, BlockStatement block) {
        VariableExpression ve = new VariableExpression(p);
        FieldExpression fe = new FieldExpression(fn);
        block.addStatement(new ExpressionStatement(
                new BinaryExpression(fe, Token.newSymbol(Types.ASSIGN, -1, -1), ve)
        ));
    }

    protected static boolean shouldHandleImplicitThisForInnerClass(ClassNode cn) {
        if (cn.isEnum() || cn.isInterface()) return false;
        if ((cn.getModifiers() & Opcodes.ACC_STATIC) != 0) return false;

        if (!(cn instanceof InnerClassNode)) return false;
        InnerClassNode innerClass = (InnerClassNode) cn;
        // scope != null means aic, we don't handle that here
        if (innerClass.getVariableScope() != null) return false;
        // static inner classes don't need this$0
        return (innerClass.getModifiers() & Opcodes.ACC_STATIC) == 0;
    }
}
