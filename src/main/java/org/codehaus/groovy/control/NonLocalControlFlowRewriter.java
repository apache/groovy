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
package org.codehaus.groovy.control;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.runtime.LoopControl;
import org.codehaus.groovy.runtime.NonLocalReturn;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.catchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.tryCatchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Desugars non-local control flow from closures (GROOVY-12126).
 * <p>
 * A {@code return@name expr} inside a closure whose lexically enclosing method
 * is named {@code name} (or {@code return@script} inside a script body) becomes
 * a call to {@link NonLocalReturn#raise}, throwing a token-matched signal; the
 * target method's body is wrapped so the signal is caught and turned into an
 * ordinary return of the carried value. Methods without such returns are left
 * untouched. The reserved target {@code script} refers to the script body.
 * <p>
 * A {@code break} or {@code continue} inside a closure passed directly as a
 * method call argument — and not bound to a loop (or switch, for break) within
 * the closure — becomes {@code throw LoopControl.BREAK/CONTINUE}; cooperating
 * iterator methods (marked {@link groovy.transform.SupportsLoopControl}) catch
 * the signal per element. Such closures are tagged with node metadata under
 * the {@code NonLocalControlFlowRewriter.class} key so the static type checker
 * can verify the callee cooperates.
 * <p>
 * Runs at {@link Phases#CANONICALIZATION}, before static type checking and
 * class generation, so the desugared form compiles identically under dynamic
 * and static compilation.
 */
public class NonLocalControlFlowRewriter extends ClassCodeVisitorSupport {

    private static final ClassNode NLR_TYPE = ClassHelper.make(NonLocalReturn.class);
    private static final ClassNode LOOP_CONTROL_TYPE = ClassHelper.make(LoopControl.class);
    private static final String TOKEN_NAME = "$nlr$token";
    private static final String SCRIPT_TARGET = "script";

    private final SourceUnit sourceUnit;

    private ClassNode currentClass;
    private MethodNode currentMethod;
    private int closureDepth;
    private boolean methodNeedsWrapper;
    private boolean classNeedsScopeRepair;

    /** Closures passed directly as method call arguments, i.e. eligible for break/continue. */
    private final Set<ClosureExpression> eligibleClosures = Collections.newSetFromMap(new IdentityHashMap<>());
    private ClosureExpression currentClosure;
    private int loopDepth;
    private int switchDepth;

    public NonLocalControlFlowRewriter(final SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    public void rewrite(final ClassNode classNode) {
        currentClass = classNode;
        classNeedsScopeRepair = false;
        visitClass(classNode);
        if (classNeedsScopeRepair) {
            // the token variable is declared in the method body and referenced
            // inside closures, so captured-variable bookkeeping must be redone
            ClassNode outermost = classNode;
            while (outermost instanceof InnerClassNode && ((InnerClassNode) outermost).isAnonymous()) {
                outermost = outermost.getOuterClass();
            }
            new VariableScopeVisitor(sourceUnit).visitClass(outermost);
        }
    }

    @Override
    protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
        MethodNode previousMethod = currentMethod;
        int previousDepth = closureDepth;
        boolean previousNeedsWrapper = methodNeedsWrapper;
        currentMethod = isConstructor ? null : node;
        closureDepth = 0;
        methodNeedsWrapper = false;
        try {
            super.visitConstructorOrMethod(node, isConstructor);
            if (methodNeedsWrapper) {
                wrapMethod(node);
                classNeedsScopeRepair = true;
            }
        } finally {
            currentMethod = previousMethod;
            closureDepth = previousDepth;
            methodNeedsWrapper = previousNeedsWrapper;
        }
    }

    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        ClosureExpression previousClosure = currentClosure;
        int previousLoopDepth = loopDepth;
        int previousSwitchDepth = switchDepth;
        closureDepth += 1;
        currentClosure = expression;
        loopDepth = 0;
        switchDepth = 0;
        try {
            super.visitClosureExpression(expression);
        } finally {
            closureDepth -= 1;
            currentClosure = previousClosure;
            loopDepth = previousLoopDepth;
            switchDepth = previousSwitchDepth;
        }
    }

    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        markEligibleClosureArguments(call.getArguments());
        super.visitMethodCallExpression(call);
    }

    @Override
    public void visitStaticMethodCallExpression(final StaticMethodCallExpression call) {
        markEligibleClosureArguments(call.getArguments());
        super.visitStaticMethodCallExpression(call);
    }

    private void markEligibleClosureArguments(final Expression arguments) {
        if (arguments instanceof TupleExpression) {
            for (Expression argument : ((TupleExpression) arguments).getExpressions()) {
                if (argument instanceof ClosureExpression) {
                    eligibleClosures.add((ClosureExpression) argument);
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    // break/continue in closure arguments: statement-slot replacement

    /**
     * Returns the loop-control signal throw replacing the given statement, or
     * null when the statement is not an applicable break/continue (reporting
     * a compile error for break/continue misuses inside closures).
     */
    private Statement replacementFor(final Statement statement) {
        boolean isBreak = statement instanceof BreakStatement;
        if (!isBreak && !(statement instanceof ContinueStatement)) return null;
        if (closureDepth == 0) return null; // LabelVerifier's domain
        if (loopDepth > 0 || (isBreak && switchDepth > 0)) return null; // bound to a real loop/switch in the closure

        String kind = isBreak ? "break" : "continue";
        String label = isBreak ? ((BreakStatement) statement).getLabel() : ((ContinueStatement) statement).getLabel();
        if (label != null) {
            addError("labeled " + kind + " is not allowed inside a closure", statement);
            return null;
        }
        if (!eligibleClosures.contains(currentClosure)) {
            addError(kind + " inside a closure is only allowed when the closure is a direct argument of a method call", statement);
            return null;
        }
        currentClosure.putNodeMetaData(NonLocalControlFlowRewriter.class, Boolean.TRUE);
        Statement replacement = throwS(propX(classX(LOOP_CONTROL_TYPE), isBreak ? "BREAK" : "CONTINUE"));
        replacement.setSourcePosition(statement);
        return replacement;
    }

    @Override
    public void visitBlockStatement(final BlockStatement block) {
        List<Statement> statements = block.getStatements();
        for (int i = 0, n = statements.size(); i < n; i += 1) {
            Statement replacement = replacementFor(statements.get(i));
            if (replacement != null) statements.set(i, replacement);
        }
        super.visitBlockStatement(block);
    }

    @Override
    public void visitIfElse(final IfStatement ifElse) {
        Statement replacement = replacementFor(ifElse.getIfBlock());
        if (replacement != null) ifElse.setIfBlock(replacement);
        replacement = replacementFor(ifElse.getElseBlock());
        if (replacement != null) ifElse.setElseBlock(replacement);
        super.visitIfElse(ifElse);
    }

    @Override
    public void visitForLoop(final ForStatement forLoop) {
        loopDepth += 1;
        try {
            super.visitForLoop(forLoop);
        } finally {
            loopDepth -= 1;
        }
    }

    @Override
    public void visitWhileLoop(final WhileStatement loop) {
        loopDepth += 1;
        try {
            super.visitWhileLoop(loop);
        } finally {
            loopDepth -= 1;
        }
    }

    @Override
    public void visitDoWhileLoop(final DoWhileStatement loop) {
        loopDepth += 1;
        try {
            super.visitDoWhileLoop(loop);
        } finally {
            loopDepth -= 1;
        }
    }

    @Override
    public void visitSwitch(final SwitchStatement statement) {
        switchDepth += 1;
        try {
            Statement replacement = replacementFor(statement.getDefaultStatement());
            if (replacement != null) statement.setDefaultStatement(replacement);
            super.visitSwitch(statement);
        } finally {
            switchDepth -= 1;
        }
    }

    @Override
    public void visitCaseStatement(final CaseStatement statement) {
        Statement replacement = replacementFor(statement.getCode());
        if (replacement != null) statement.setCode(replacement);
        super.visitCaseStatement(statement);
    }

    @Override
    public void visitTryCatchFinally(final TryCatchStatement statement) {
        Statement replacement = replacementFor(statement.getTryStatement());
        if (replacement != null) statement.setTryStatement(replacement);
        for (CatchStatement catchStatement : statement.getCatchStatements()) {
            replacement = replacementFor(catchStatement.getCode());
            if (replacement != null) catchStatement.setCode(replacement);
        }
        replacement = replacementFor(statement.getFinallyStatement());
        if (replacement != null) statement.setFinallyStatement(replacement);
        super.visitTryCatchFinally(statement);
    }

    @Override
    public void visitSynchronizedStatement(final SynchronizedStatement statement) {
        Statement replacement = replacementFor(statement.getCode());
        if (replacement != null) statement.setCode(replacement);
        super.visitSynchronizedStatement(statement);
    }

    @Override
    public void visitReturnStatement(final ReturnStatement statement) {
        super.visitReturnStatement(statement);
        String target = statement.getTarget();
        if (target == null) return;

        if (currentMethod == null) {
            addError("return@" + target + " is only allowed inside a method body (or a closure within one)", statement);
            return;
        }
        if (!isTarget(currentMethod, target)) {
            if (isOuterTarget(target)) {
                addError("return@" + target + ": cannot return across a class boundary; '" + target
                        + "' lexically encloses class '" + currentClass.getNameWithoutPackage()
                        + "' but is not one of its methods", statement);
            } else {
                String hint = currentClass.isScript() && currentMethod.isScriptBody()
                        ? "; use return@script to return from a script body" : "";
                addError("return@" + target + ": no lexically enclosing method named '" + target + "'" + hint, statement);
            }
            return;
        }

        statement.setTarget(null);
        if (closureDepth == 0) return; // plain return suffices at method level

        Expression value = statement.getExpression() == ConstantExpression.EMPTY_EXPRESSION
                ? nullX() : statement.getExpression();
        Expression raise = callX(NLR_TYPE, "raise", args(varX(TOKEN_NAME, ClassHelper.OBJECT_TYPE), value));
        raise.setSourcePosition(statement);
        statement.setExpression(raise);
        methodNeedsWrapper = true;
    }

    private static boolean isTarget(final MethodNode method, final String target) {
        return target.equals(method.getName()) || (SCRIPT_TARGET.equals(target) && method.isScriptBody());
    }

    private boolean isOuterTarget(final String target) {
        ClassNode cn = currentClass;
        while (cn != null) {
            MethodNode enclosing = cn.getEnclosingMethod();
            if (enclosing != null && isTarget(enclosing, target)) return true;
            cn = cn.getOuterClass();
        }
        return false;
    }

    private static void wrapMethod(final MethodNode method) {
        Parameter signal = new Parameter(NLR_TYPE, "$nlr$e");
        ClassNode returnType = method.getReturnType();
        Statement returnCarriedValue = ClassHelper.isPrimitiveVoid(returnType)
                ? new ReturnStatement(ConstantExpression.EMPTY_EXPRESSION)
                : returnS(castX(returnType, callX(varX(signal), "getValue")));
        Statement catchBody = block(
                ifS(notX(callX(varX(signal), "matches", args(varX(TOKEN_NAME, ClassHelper.OBJECT_TYPE)))), throwS(varX(signal))),
                returnCarriedValue);
        TryCatchStatement handler = tryCatchS(method.getCode(), EmptyStatement.INSTANCE, catchS(signal, catchBody));
        BlockStatement newCode = block(
                declS(localVarX(TOKEN_NAME, ClassHelper.OBJECT_TYPE), ctorX(ClassHelper.OBJECT_TYPE)),
                handler);
        newCode.setSourcePosition(method.getCode());
        method.setCode(newCode);
    }
}
