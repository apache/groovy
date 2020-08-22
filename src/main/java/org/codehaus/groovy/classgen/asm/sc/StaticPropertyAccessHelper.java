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
 * Facilitates the generation of statically-compiled bytecode for property access.
 *
 * @since 2.4.0
 */
public abstract class StaticPropertyAccessHelper {

    public static Expression transformToSetterCall(
            final Expression receiver,
            final MethodNode setterMethod,
            final Expression valueExpression,
            final boolean implicitThis,
            final boolean safe,
            final boolean spreadSafe,
            final boolean returnValue,
            final Expression sourceExpression) {
        if (returnValue) {
            TemporaryVariableExpression tmp = new TemporaryVariableExpression(valueExpression);
            PoppingMethodCallExpression call = new PoppingMethodCallExpression(receiver, setterMethod, tmp);
            call.setSafe(safe);
            call.setSpreadSafe(spreadSafe);
            call.setImplicitThis(implicitThis);
            call.setSourcePosition(sourceExpression);
            PoppingListOfExpressionsExpression list = new PoppingListOfExpressionsExpression(tmp, call);
            list.setSourcePosition(sourceExpression);
            return list;
        } else {
            MethodCallExpression call = new MethodCallExpression(receiver, setterMethod.getName(), valueExpression);
            call.setSafe(safe);
            call.setSpreadSafe(spreadSafe);
            call.setImplicitThis(implicitThis);
            call.setMethodTarget(setterMethod);
            call.setSourcePosition(sourceExpression);
            return call;
        }
    }

    private static class PoppingListOfExpressionsExpression extends ListOfExpressionsExpression {

        private final TemporaryVariableExpression tmp;
        private final PoppingMethodCallExpression call;

        public PoppingListOfExpressionsExpression(final TemporaryVariableExpression tmp, final PoppingMethodCallExpression call) {
            super(Arrays.asList(tmp, call));
            this.tmp = tmp;
            this.call = call;
        }

        @Override
        public Expression transformExpression(final ExpressionTransformer transformer) {
            PoppingMethodCallExpression call = (PoppingMethodCallExpression) transformer.transform(this.call);
            return new PoppingListOfExpressionsExpression(call.tmp, call);
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

        private final TemporaryVariableExpression tmp;

        public PoppingMethodCallExpression(final Expression receiver, final MethodNode setterMethod, final TemporaryVariableExpression tmp) {
            super(receiver, setterMethod.getName(), tmp);
            setMethodTarget(setterMethod);
            this.tmp = tmp;
        }

        @Override
        public Expression transformExpression(final ExpressionTransformer transformer) {
            PoppingMethodCallExpression call = new PoppingMethodCallExpression(transformer.transform(getObjectExpression()), getMethodTarget(), (TemporaryVariableExpression) transformer.transform(tmp));
            call.copyNodeMetaData(this);
            call.setSourcePosition(this);
            call.setSafe(isSafe());
            call.setSpreadSafe(isSpreadSafe());
            call.setImplicitThis(isImplicitThis());
            return call;
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
