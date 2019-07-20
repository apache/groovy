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

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.transform.sc.ListOfExpressionsExpression;
import org.codehaus.groovy.transform.sc.TemporaryVariableExpression;

import java.util.Arrays;

/**
 * Contains helper methods aimed at facilitating the generation of statically compiled bytecode for property access.
 *
 * @since 2.4.0
 */
public abstract class StaticPropertyAccessHelper {

    public static Expression transformToSetterCall(
            Expression receiver,
            MethodNode setterMethod,
            final Expression arguments,
            boolean implicitThis,
            boolean safe,
            boolean spreadSafe,
            boolean requiresReturnValue,
            Expression location) {
        if (requiresReturnValue) {
            TemporaryVariableExpression tmp = new TemporaryVariableExpression(arguments);
            PoppingMethodCallExpression call = new PoppingMethodCallExpression(receiver, setterMethod, tmp);
            call.setImplicitThis(implicitThis);
            call.setSafe(safe);
            call.setSpreadSafe(spreadSafe);
            call.setSourcePosition(location);
            PoppingListOfExpressionsExpression result = new PoppingListOfExpressionsExpression(tmp, call);
            result.setSourcePosition(location);
            return result;
        } else {
            MethodCallExpression call = new MethodCallExpression(
                    receiver,
                    setterMethod.getName(),
                    arguments
            );
            call.setImplicitThis(implicitThis);
            call.setSafe(safe);
            call.setSpreadSafe(spreadSafe);
            call.setMethodTarget(setterMethod);
            call.setSourcePosition(location);
            return call;
        }
    }

    private static class PoppingListOfExpressionsExpression extends ListOfExpressionsExpression {
        private final TemporaryVariableExpression tmp;
        private final PoppingMethodCallExpression call;

        public PoppingListOfExpressionsExpression(final TemporaryVariableExpression tmp, final PoppingMethodCallExpression call) {
            super(Arrays.asList(
                    tmp,
                    call
            ));
            this.tmp = tmp;
            this.call = call;
        }

        @Override
        public Expression transformExpression(final ExpressionTransformer transformer) {
            PoppingMethodCallExpression tcall = (PoppingMethodCallExpression) call.transformExpression(transformer);
            return new PoppingListOfExpressionsExpression(tcall.tmp, tcall);
        }

        @Override
        public void visit(final GroovyCodeVisitor visitor) {
            super.visit(visitor);
            if (visitor instanceof AsmClassGenerator) {
                tmp.remove(((AsmClassGenerator) visitor).getController());
            }
        }
    }

    private static class PoppingMethodCallExpression extends MethodCallExpression {
        private final Expression receiver;
        private final MethodNode setter;
        private final TemporaryVariableExpression tmp;

        public PoppingMethodCallExpression(final Expression receiver, final MethodNode setterMethod, final TemporaryVariableExpression tmp) {
            super(receiver, setterMethod.getName(), tmp);
            this.receiver = receiver;
            this.setter = setterMethod;
            this.tmp = tmp;
            setMethodTarget(setterMethod);
        }

        @Override
        public Expression transformExpression(final ExpressionTransformer transformer) {
            PoppingMethodCallExpression trn = new PoppingMethodCallExpression(receiver.transformExpression(transformer), setter, (TemporaryVariableExpression) tmp.transformExpression(transformer));
            trn.copyNodeMetaData(this);
            trn.setSourcePosition(this);
            trn.setImplicitThis(isImplicitThis());
            trn.setSafe(isSafe());
            trn.setSpreadSafe(isSpreadSafe());
            return trn;
        }

        @Override
        public void visit(final GroovyCodeVisitor visitor) {
            super.visit(visitor);
            if (visitor instanceof AsmClassGenerator) {
                // ignore the return of the call
                ((AsmClassGenerator) visitor).getController().getOperandStack().pop();
            }
        }
    }
}
