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
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
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

import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.eqX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.indexX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isInstanceOfX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

public abstract class InnerClassVisitorHelper extends ClassCodeVisitorSupport {

    private static final ClassNode OBJECT_ARRAY = ClassHelper.OBJECT_TYPE.makeArray();

    protected static void setPropertyGetterDispatcher(BlockStatement block, Expression thiz, Parameter[] parameters) {
        List<ConstantExpression> gStringStrings = new ArrayList<ConstantExpression>();
        gStringStrings.add(new ConstantExpression(""));
        gStringStrings.add(new ConstantExpression(""));
        List<Expression> gStringValues = new ArrayList<Expression>();
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
        List<ConstantExpression> gStringStrings = new ArrayList<ConstantExpression>();
        gStringStrings.add(new ConstantExpression(""));
        gStringStrings.add(new ConstantExpression(""));
        List<Expression> gStringValues = new ArrayList<Expression>();
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
        List<ConstantExpression> gStringStrings = new ArrayList<ConstantExpression>();
        gStringStrings.add(new ConstantExpression(""));
        gStringStrings.add(new ConstantExpression(""));
        List<Expression> gStringValues = new ArrayList<Expression>();
        gStringValues.add(varX(parameters[0]));
        Expression name = new GStringExpression("$name", gStringStrings, gStringValues);

        // if (!(args instanceof Object[])) return thiz."$name"(args)
        block.addStatement(ifS(
            notX(isInstanceOfX(varX(parameters[1]), OBJECT_ARRAY)),
            returnS(callX(thiz, name, varX(parameters[1])))));

        // if (((Object[])args).length == 1) return thiz."$name"(args[0])
        block.addStatement(ifS(
            eqX(propX(castX(OBJECT_ARRAY, varX(parameters[1])), "length"), constX(1, true)),
            returnS(callX(thiz, name, indexX(castX(OBJECT_ARRAY, varX(parameters[1])), constX(0, true))))));

        // return thiz."$name"(*args)
        block.addStatement(returnS(callX(thiz, name, new SpreadExpression(varX(parameters[1])))));
    }

    protected static boolean isStatic(InnerClassNode node) {
        return node.getDeclaredField("this$0") == null;
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
        block.addStatement(assignS(fieldX(fn), varX(p)));
    }

    protected static boolean shouldHandleImplicitThisForInnerClass(ClassNode cn) {
        final int explicitOrImplicitStatic = Opcodes.ACC_STATIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ENUM;
        return (cn.getModifiers() & explicitOrImplicitStatic) == 0 && (cn instanceof InnerClassNode && !((InnerClassNode) cn).isAnonymous());
    }
}
