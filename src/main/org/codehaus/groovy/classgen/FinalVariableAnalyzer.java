/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;

import java.util.HashMap;
import java.util.Map;

public class FinalVariableAnalyzer extends ClassCodeVisitorSupport {

    private final SourceUnit sourceUnit;
    private final VariableNotFinalCallback callback;

    private static enum VariableState {
        is_uninitialized,
        is_final,
        is_var;

        public VariableState getNext() {
            switch (this) {
                case is_uninitialized: return is_final;
                default:
                    return is_var;
            }
        }

        public boolean isFinal() {
            return this == is_final || this == is_uninitialized;
        }
    }

    private final Map<Variable, VariableState> assignmentCount = new HashMap<Variable, VariableState>();

    public FinalVariableAnalyzer(final SourceUnit sourceUnit) {
        this(sourceUnit, null);
    }

    public FinalVariableAnalyzer(final SourceUnit sourceUnit, final VariableNotFinalCallback callback) {
        this.callback = callback;
        this.sourceUnit = sourceUnit;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    public boolean isEffectivelyFinal(Variable v) {
        if (v instanceof VariableExpression) {
            v = ((VariableExpression) v).getAccessedVariable();
        }
        VariableState state = assignmentCount.get(v);
        return state == null || state.isFinal();
    }

    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        super.visitBinaryExpression(expression);
        if (StaticTypeCheckingSupport.isAssignment(expression.getOperation().getType())) {
            Expression leftExpression = expression.getLeftExpression();
            if (leftExpression instanceof Variable) {
                boolean isDeclaration = expression instanceof DeclarationExpression;
                boolean uninitialized =
                        isDeclaration &&
                        expression.getRightExpression() == EmptyExpression.INSTANCE;
                recordAssignment((Variable) leftExpression, isDeclaration, uninitialized, expression);
                if (leftExpression instanceof VariableExpression) {
                    Variable accessed = ((VariableExpression) leftExpression).getAccessedVariable();
                    if (accessed!=leftExpression) {
                        recordAssignment(accessed, isDeclaration, uninitialized, expression);
                    }
                }
            }
        }
    }

    private void recordAssignment(
            Variable var,
            boolean isDeclaration,
            boolean uninitialized,
            BinaryExpression expression) {
        if (var==null) {
            return;
        }
        if (!isDeclaration && var.isClosureSharedVariable()) {
            assignmentCount.put(var, VariableState.is_var);
        }
        VariableState count = assignmentCount.get(var);
        if (count==null) {
            count = uninitialized?VariableState.is_uninitialized:VariableState.is_final;
            if (var instanceof Parameter) {
                count = VariableState.is_var;
            }
        } else {
            count = count.getNext();
        }
        assignmentCount.put(var, count);
        if (count==VariableState.is_var && callback!=null) {
            callback.variableNotFinal(var, expression);
        }
    }

    public static interface VariableNotFinalCallback {
        /**
         * Callback called whenever an assignment transforms an effectively final
         * variable into a non final variable (aka, breaks the "final" modifier contract)
         * @param var the variable detected as not final
         * @param bexp the expression responsible for the contract to be broken
         */
        void variableNotFinal(Variable var, BinaryExpression bexp);
    }
}
