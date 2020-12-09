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
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

public abstract class InnerClassVisitorHelper extends ClassCodeVisitorSupport {

    protected static void addFieldInit(final Parameter p, final FieldNode fn, final BlockStatement block) {
        block.addStatement(assignS(fieldX(fn), varX(p)));
    }

    protected static void setPropertyGetterDispatcher(final BlockStatement block, final Expression target, final Parameter[] parameters) {
        block.addStatement(
                returnS(
                        propX(
                                target,
                                dynName(parameters[0])
                        )
                )
        );
    }

    protected static void setPropertySetterDispatcher(final BlockStatement block, final Expression target, final Parameter[] parameters) {
        block.addStatement(
                stmt(
                        assignX(
                                propX(
                                        target,
                                        dynName(parameters[0])
                                ),
                                varX(parameters[1])
                        )
                )
        );
    }

    protected static void setMethodDispatcherCode(final BlockStatement block, final Expression target, final Parameter[] parameters) {
        block.addStatement(
                returnS(
                        callX(
                                target,
                                dynName(parameters[0]),
                                args(new SpreadExpression(varX(parameters[1])))
                        )
                )
        );
    }

    private static Expression dynName(final Parameter p) {
        List<ConstantExpression> gStringStrings = new ArrayList<>();
        gStringStrings.add(new ConstantExpression(""));
        gStringStrings.add(new ConstantExpression(""));

        List<Expression> gStringValues = new ArrayList<>();
        gStringValues.add(varX(p));

        return new GStringExpression("$name", gStringStrings, gStringValues);
    }

    protected static boolean isStatic(final InnerClassNode cn) {
        return cn.getDeclaredField("this$0") == null;
    }

    protected static ClassNode getClassNode(final ClassNode cn, final boolean isStatic) {
        return isStatic ? ClassHelper.CLASS_Type : cn; // TODO: Set class type parameter?
    }

    protected static int getObjectDistance(ClassNode cn) {
        int count = 0;
        while (cn != null && cn != ClassHelper.OBJECT_TYPE) {
            cn = cn.getSuperClass();
            count += 1;
        }
        return count;
    }

    protected static boolean shouldHandleImplicitThisForInnerClass(final ClassNode cn) {
        final int explicitOrImplicitStatic = Opcodes.ACC_STATIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ENUM;
        return (cn.getModifiers() & explicitOrImplicitStatic) == 0 && (cn instanceof InnerClassNode && !((InnerClassNode) cn).isAnonymous());
    }
}
