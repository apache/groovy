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
package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.VariableSlotLoader;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;

/**
 * Emits the {@code isCase} call the type checker selected for a {@code case}
 * label, shared by the statically compiled switch expression and switch
 * statement writers so that a label resolves the same way in either position
 * (GROOVY-12407).
 *
 * @since 7.0.0
 */
public class StaticTypesIsCaseWriter {

    private StaticTypesIsCaseWriter() {
    }

    /**
     * Emits the selected {@code isCase} as a direct method call via
     * {@code StaticInvocationWriter.writeDirectMethodCall}, leaving a boolean
     * on the operand stack for the following {@code IFEQ}.
     *
     * @param controller    the writer controller
     * @param caseStatement the arm whose label is being tested
     * @param selectorIndex local variable slot holding the switch selector
     * @param selectorType  type of that slot
     * @return {@code false} when the type checker selected no target, in which
     *         case the caller emits its own comparison; {@code true} when the
     *         call was written
     */
    public static boolean writeDirectIsCase(final WriterController controller, final CaseStatement caseStatement,
            final int selectorIndex, final ClassNode selectorType) {
        MethodNode target = caseStatement.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        if (target == null) {
            return false;
        }
        Expression caseValue = caseStatement.getExpression();
        OperandStack operandStack = controller.getOperandStack();
        VariableSlotLoader selector = new VariableSlotLoader(selectorType, selectorIndex, operandStack);
        MethodCallExpression call = callX(caseValue, "isCase", args(selector));
        call.setImplicitThis(false);
        call.setMethodTarget(target);
        call.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, target);
        call.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.boolean_TYPE);
        Object privateAccess = caseStatement.getNodeMetaData(StaticTypesMarker.PV_METHODS_ACCESS);
        if (privateAccess != null) {
            call.putNodeMetaData(StaticTypesMarker.PV_METHODS_ACCESS, privateAccess);
        }
        call.setSourcePosition(caseValue);
        call.visit(controller.getAcg());
        operandStack.doGroovyCast(ClassHelper.boolean_TYPE);
        return true;
    }
}
