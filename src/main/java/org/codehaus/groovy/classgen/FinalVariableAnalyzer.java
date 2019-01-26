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
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FinalVariableAnalyzer extends ClassCodeVisitorSupport {

    private final SourceUnit sourceUnit;
    private final VariableNotFinalCallback callback;

    private Set<Variable> declaredFinalVariables = null;
    private boolean inAssignmentRHS = false;
    private boolean inArgumentList = false;

    private enum VariableState {
        is_uninitialized(false),
        is_final(true),
        is_var(false),
        is_ambiguous(false); // any further use of that variable can trigger uninitialized ot not final errors

        private final boolean isFinal;

        VariableState(final boolean isFinal) {
            this.isFinal = isFinal;
        }

        public VariableState getNext() {
            switch (this) {
                case is_uninitialized:
                    return is_final;
                default:
                    return is_var;
            }
        }

        public boolean isFinal() {
            return isFinal;
        }
    }

    private final Deque<Map<Variable, VariableState>> assignmentTracker = new LinkedList<>();

    public FinalVariableAnalyzer(final SourceUnit sourceUnit) {
        this(sourceUnit, null);
    }

    public FinalVariableAnalyzer(final SourceUnit sourceUnit, final VariableNotFinalCallback callback) {
        this.callback = callback;
        this.sourceUnit = sourceUnit;
        assignmentTracker.add(new StateMap());
    }

    private Map<Variable, VariableState> pushState() {
        Map<Variable, VariableState> state = new StateMap();
        state.putAll(getState());
        assignmentTracker.add(state);
        return state;
    }

    private static Variable getTarget(Variable v) {
        if (v instanceof VariableExpression) {
            Variable t = ((VariableExpression) v).getAccessedVariable();
            if (t == v) return t;
            return getTarget(t);
        }
        return v;
    }

    private Map<Variable, VariableState> popState() {
        return assignmentTracker.removeLast();
    }

    private Map<Variable, VariableState> getState() {
        return assignmentTracker.getLast();
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    public boolean isEffectivelyFinal(Variable v) {
        VariableState state = getState().get(v);
        return (v instanceof Parameter && state == null)
                || (state != null && state.isFinal());
    }

    @Override
    public void visitBlockStatement(final BlockStatement block) {
        Set<Variable> old = declaredFinalVariables;
        declaredFinalVariables = new HashSet<>();
        super.visitBlockStatement(block);
        declaredFinalVariables = old;
    }

    @Override
    public void visitArgumentlistExpression(ArgumentListExpression ale) {
        boolean old = inArgumentList;
        inArgumentList = true;
        super.visitArgumentlistExpression(ale);
        inArgumentList = old;
    }

    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        boolean assignment = StaticTypeCheckingSupport.isAssignment(expression.getOperation().getType());
        boolean isDeclaration = expression instanceof DeclarationExpression;
        Expression leftExpression = expression.getLeftExpression();
        Expression rightExpression = expression.getRightExpression();
        if (isDeclaration) {
            recordFinalVars(leftExpression);
        }
        // visit RHS first for expressions like a = b = 0
        inAssignmentRHS = assignment;
        rightExpression.visit(this);
        inAssignmentRHS = false;
        leftExpression.visit(this);
        if (assignment) {
            recordAssignments(expression, isDeclaration, leftExpression, rightExpression);
        }
    }

    private void recordAssignments(BinaryExpression expression, boolean isDeclaration, Expression leftExpression, Expression rightExpression) {
        if (leftExpression instanceof Variable) {
            boolean uninitialized =
                    isDeclaration && rightExpression == EmptyExpression.INSTANCE;
            recordAssignment((Variable) leftExpression, isDeclaration, uninitialized, false, expression);
        } else if (leftExpression instanceof TupleExpression) {
            TupleExpression te = (TupleExpression) leftExpression;
            for (Expression next : te.getExpressions()) {
                if (next instanceof Variable) {
                    recordAssignment((Variable) next, isDeclaration, false, false, next);
                }
            }
        }
    }

    private void recordFinalVars(Expression leftExpression) {
        if (leftExpression instanceof VariableExpression) {
            VariableExpression var = (VariableExpression) leftExpression;
            if (Modifier.isFinal(var.getModifiers())) {
                declaredFinalVariables.add(var);
            }
        } else if (leftExpression instanceof TupleExpression) {
            TupleExpression te = (TupleExpression) leftExpression;
            for (Expression next : te.getExpressions()) {
                if (next instanceof Variable) {
                    declaredFinalVariables.add((Variable) next);
                }
            }
        }
    }

    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        boolean old = inAssignmentRHS;
        inAssignmentRHS = false;
        Map<Variable, VariableState> origState = new StateMap();
        origState.putAll(getState());
        super.visitClosureExpression(expression);
        cleanLocalVars(origState, getState());
        inAssignmentRHS = old;
    }

    private void cleanLocalVars(Map<Variable, VariableState> origState, Map<Variable, VariableState> state) {
        // clean local vars added during visit of closure
        for (Iterator<Map.Entry<Variable, VariableState>> iter = state.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<Variable, VariableState> next = iter.next();
            Variable key = next.getKey();
            if (key instanceof VariableExpression && ((VariableExpression)key).getAccessedVariable() == key && !origState.containsKey(key)) {
                // remove local variable
                iter.remove();
            }
        }
    }

    @Override
    public void visitPrefixExpression(final PrefixExpression expression) {
        inAssignmentRHS = expression.getExpression() instanceof VariableExpression;
        super.visitPrefixExpression(expression);
        inAssignmentRHS = false;
        checkPrePostfixOperation(expression.getExpression(), expression);
    }

    @Override
    public void visitPostfixExpression(final PostfixExpression expression) {
        inAssignmentRHS = expression.getExpression() instanceof VariableExpression;
        super.visitPostfixExpression(expression);
        inAssignmentRHS = false;
        checkPrePostfixOperation(expression.getExpression(), expression);
    }

    private void checkPrePostfixOperation(final Expression variable, final Expression originalExpression) {
        if (variable instanceof Variable) {
            recordAssignment((Variable) variable, false, false, true, originalExpression);
            if (variable instanceof VariableExpression) {
                Variable accessed = ((VariableExpression) variable).getAccessedVariable();
                if (accessed != variable) {
                    recordAssignment(accessed, false, false, true, originalExpression);
                }
            }
        }
    }

    @Override
    public void visitVariableExpression(final VariableExpression expression) {
        super.visitVariableExpression(expression);
        Map<Variable, VariableState> state = getState();
        Variable key = expression.getAccessedVariable();
        if (key == null) {
            fixVar(expression);
            key = expression.getAccessedVariable();
        }
        if (key != null && !key.isClosureSharedVariable() && callback != null) {
            VariableState variableState = state.get(key);
            if ((inAssignmentRHS || inArgumentList) && (variableState == VariableState.is_uninitialized || variableState == VariableState.is_ambiguous)) {
                callback.variableNotAlwaysInitialized(expression);
            }
        }
    }

    @Override
    public void visitIfElse(final IfStatement ifElse) {
        visitStatement(ifElse);
        ifElse.getBooleanExpression().visit(this);
        Map<Variable, VariableState> ifState = pushState();
        ifElse.getIfBlock().visit(this);
        popState();
        Statement elseBlock = ifElse.getElseBlock();
        Map<Variable, VariableState> elseState = pushState();
        visitPossiblyEmptyStatement(elseBlock);
        popState();

        // merge if/else branches
        Map<Variable, VariableState> curState = getState();
        Set<Variable> allVars = new HashSet<>();
        allVars.addAll(curState.keySet());
        allVars.addAll(ifState.keySet());
        allVars.addAll(elseState.keySet());
        for (Variable var : allVars) {
            VariableState beforeValue = curState.get(var);
            VariableState ifValue = ifState.get(var);
            VariableState elseValue = elseState.get(var);
            // merge if and else values
            VariableState mergedIfElse;
            mergedIfElse = isFinal(ifValue) && isFinal(elseValue) ? VariableState.is_final : VariableState.is_var;
            if (beforeValue != null) {
                curState.put(var, mergedIfElse);
            }
        }
    }

    private void visitPossiblyEmptyStatement(Statement block) {
        if (block instanceof EmptyStatement) {
            // dispatching to EmptyStatement will not call back visitor,
            // must call our visitEmptyStatement explicitly
            visitEmptyStatement((EmptyStatement) block);
        } else {
            block.visit(this);
        }
    }

    private boolean isFinal(VariableState value) {
        return value != null && value.isFinal;
    }

    @Override
    public void visitTryCatchFinally(final TryCatchStatement statement) {
        visitStatement(statement);
        Map<Variable, VariableState> beforeTryState = new HashMap<>(getState());
        pushState();
        Statement tryStatement = statement.getTryStatement();
        tryStatement.visit(this);
        Map<Variable, VariableState> afterTryState = new HashMap<>(getState());
        Statement finallyStatement = statement.getFinallyStatement();
        List<Map<Variable, VariableState>> afterStates = new ArrayList<>();
        // the try finally case
        visitPossiblyEmptyStatement(finallyStatement);
        if (!returningBlock(tryStatement)) {
            afterStates.add(new HashMap<>(getState()));
        }
        popState();
        // now the finally only case but only if no catches
        if (statement.getCatchStatements().isEmpty()) {
            visitPossiblyEmptyStatement(finallyStatement);
            if (!returningBlock(tryStatement)) {
                afterStates.add(new HashMap<>(getState()));
            }
        }
        for (CatchStatement catchStatement : statement.getCatchStatements()) {
            // We don't try to analyse which statement within the try block might have thrown an exception.
            // We make a crude assumption that anywhere from none to all of the statements might have been executed.
            // Run visitor for both scenarios so the eager checks will be performed for either of these cases.
            visitCatchFinally(beforeTryState, afterStates, catchStatement, finallyStatement);
            visitCatchFinally(afterTryState, afterStates, catchStatement, finallyStatement);
        }
        // after states can only be empty if try and catch statements all return in which case nothing to do
        if (afterStates.isEmpty()) return;
        // now adjust the state variables - any early returns won't have gotten here
        // but we need to check that the same status was observed by all paths
        // and mark as ambiguous if needed
        Map<Variable, VariableState> corrected = afterStates.remove(0);
        for (Map<Variable, VariableState> nextState : afterStates) {
            for (Map.Entry<Variable, VariableState> entry : corrected.entrySet()) {
                Variable var = entry.getKey();
                VariableState currentCorrectedState = entry.getValue();
                VariableState candidateCorrectedState = nextState.get(var);
                if (currentCorrectedState == VariableState.is_ambiguous) continue;
                if (currentCorrectedState != candidateCorrectedState) {
                    if (currentCorrectedState == VariableState.is_uninitialized || candidateCorrectedState == VariableState.is_uninitialized) {
                        corrected.put(var, VariableState.is_ambiguous);
                    } else {
                        corrected.put(var, VariableState.is_var);
                    }
                }
            }
        }
        getState().putAll(corrected);
    }

    private void visitCatchFinally(Map<Variable, VariableState> initialVarState, List<Map<Variable, VariableState>> afterTryCatchStates, CatchStatement catchStatement, Statement finallyStatement) {
        pushState();
//        getState().clear();
        getState().putAll(initialVarState);
        Statement code = catchStatement.getCode();
        catchStatement.visit(this);
        visitPossiblyEmptyStatement(finallyStatement);
        if (code == null || !returningBlock(code)) {
            afterTryCatchStates.add(new HashMap<>(getState()));
        }
        popState();
    }

    /**
     * @return true if the block's last statement is a return
     */
    private boolean returningBlock(Statement block) {
        if (block instanceof ReturnStatement) {
            return true;
        }
        if (!(block instanceof BlockStatement)) {
            return false;
        }
        BlockStatement bs = (BlockStatement) block;
        if (bs.getStatements().size() == 0) {
            return false;
        }
        Statement last = DefaultGroovyMethods.last(bs.getStatements());
        if (last instanceof ReturnStatement) {
            return true;
        }
        return false;
    }

    private void recordAssignment(
            Variable var,
            boolean isDeclaration,
            boolean uninitialized,
            boolean forceVariable,
            Expression expression) {
        if (var == null) {
            return;
        }

        // getTarget(var) can be null in buggy xform code, e.g. Spock
        if (getTarget(var) == null) {
            fixVar(var);
            // we maybe can't fix a synthetic field
            if (getTarget(var) == null) return;
        }

        if (!isDeclaration && var.isClosureSharedVariable()) {
            getState().put(var, VariableState.is_var);
        }
        VariableState variableState = getState().get(var);
        if (variableState == null) {
            variableState = uninitialized ? VariableState.is_uninitialized : VariableState.is_final;
            if (getTarget(var) instanceof Parameter) {
                variableState = VariableState.is_var;
            }
        } else {
            variableState = variableState.getNext();
        }
        if (forceVariable) {
            variableState = VariableState.is_var;
        }
        getState().put(var, variableState);
        if ((variableState == VariableState.is_var || variableState == VariableState.is_ambiguous) && callback != null) {
            callback.variableNotFinal(var, expression);
        }
    }

    // getTarget(var) can be null in buggy xform code, e.g. Spock <= 1.1
    // TODO consider removing fixVar once Spock 1.2 is released - replace with informational exception?
    // This fixes xform declaration expressions but not other synthetic fields which aren't set up correctly
    private void fixVar(Variable var) {
        if (getTarget(var) == null && var instanceof VariableExpression && getState() != null && var.getName() != null) {
            for (Variable v: getState().keySet()) {
                if (var.getName().equals(v.getName())) {
                    ((VariableExpression)var).setAccessedVariable(v);
                    break;
                }
            }
        }
    }

    public interface VariableNotFinalCallback {
        /**
         * Callback called whenever an assignment transforms an effectively final variable into a non final variable
         * (aka, breaks the "final" modifier contract)
         *
         * @param var  the variable detected as not final
         * @param bexp the expression responsible for the contract to be broken
         */
        void variableNotFinal(Variable var, Expression bexp);

        /**
         * Callback used whenever a variable is declared as final, but can remain in an uninitialized state
         *
         * @param var the variable detected as potentially uninitialized
         */
        void variableNotAlwaysInitialized(VariableExpression var);
    }

    private static class StateMap extends HashMap<Variable, VariableState> {
        private static final long serialVersionUID = -5881634573411342092L;

        @Override
        public VariableState get(final Object key) {
            return super.get(getTarget((Variable) key));
        }

        @Override
        public VariableState put(final Variable key, final VariableState value) {
            return super.put(getTarget(key), value);
        }
    }
}
