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
package org.apache.groovy.contracts.ast;

import groovy.contracts.ThrowsIf;
import org.apache.groovy.contracts.ThrowsIfSupport;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.equalsNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Handles a method's {@link ThrowsIf} arm-set. Processed once per method (the
 * transformation fires per annotation, so repeats are deduplicated via node
 * metadata) because the semantics are per-<em>set</em>: guards are generated in
 * declaration order, and the {@code checked} wrapper judges an escaping throw
 * against <em>every</em> arm.
 * <p>
 * For each {@code WOVEN}-origin arm the guard-throw is generated at method
 * entry, so that conceptually:
 * <pre>
 * &#64;ThrowsIf(value = { b == 0 }, exception = ArithmeticException)
 * int divide(int a, int b) { a.intdiv(b) }
 * </pre>
 * compiles as:
 * <pre>
 * int divide(int a, int b) {
 *     if (b == 0) throw new ArithmeticException('@ThrowsIf: b == 0')
 *     a.intdiv(b)
 * }
 * </pre>
 * — the general form of the pattern {@code groovy.transform.NullCheck} provides
 * for the null-check special case. Arms with {@code woven = false} generate no
 * guard — the throw already exists, in the body ({@code direct = true}, the
 * default) or in invoked code ({@code direct = false}); identical bytecode, the
 * {@code direct} distinction is information for tools and is not read here.
 * <p>
 * When any arm is {@code checked}, the conditions are additionally snapshot on
 * entry and the body is wrapped so that, conceptually:
 * <pre>
 * boolean $c0 = &lt;cond0&gt;; ...                                 // entry snapshot, every arm
 * if ($c0) throw new E0(...)                                  // woven arms, in declaration order
 * Throwable $t = null
 * try { &lt;original body&gt; }
 * catch (Throwable caught) {
 *     $t = caught
 *     throw ThrowsIfSupport.onlyWhen(caught, [$c0, ...], [E0, ...], [texts], allExhaustive)
 * } finally {
 *     if ($t == null) ThrowsIfSupport.mustThrow([unwoven $ci ...], [texts])   // normal return only
 * }
 * </pre>
 * A justified throw propagates untouched (defined behaviour); a broken
 * implementation raises {@link org.apache.groovy.contracts.ThrowsIfViolation},
 * never the declared exception. Non-woven arms with no {@code checked} flag
 * anywhere generate nothing — runtime-retained metadata for tools.
 *
 * @since 6.0.0
 * @see ThrowsIf
 * @see ThrowsIfSupport
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class ThrowsIfASTTransformation implements ASTTransformation {

    private static final String PROCESSED_KEY = "org.apache.groovy.contracts.THROWS_IF_PROCESSED";
    private static final ClassNode THROWS_IF_TYPE = ClassHelper.make(ThrowsIf.class);
    private static final ClassNode SUPPORT_TYPE = ClassHelper.make(ThrowsIfSupport.class);
    private static final AtomicLong COUNTER = new AtomicLong();

    /** One parsed arm of the method's {@code @ThrowsIf} set. */
    private static final class Arm {
        Expression condition;
        String conditionText;
        ClassNode exceptionType;
        boolean woven;      // non-woven arms generate no guard (`direct` is tool metadata, unread here)
        boolean checked;
        boolean exhaustive;
    }

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        if (nodes.length != 2) return;
        if (!(nodes[0] instanceof AnnotationNode)) return;
        if (!(nodes[1] instanceof MethodNode method)) return; // covers constructors (ConstructorNode)
        if (method.isAbstract() || method.getCode() == null) return;
        if (method.getNodeMetaData(PROCESSED_KEY) != null) return; // per-set semantics: fire once
        method.putNodeMetaData(PROCESSED_KEY, Boolean.TRUE);

        List<Arm> arms = collectArms(method);
        if (arms.isEmpty()) return;

        boolean anyChecked = arms.stream().anyMatch(a -> a.checked);
        // A constructor's explicit super(...)/this(...) must stay the FIRST statement (else Groovy
        // stops recognising it as the special call and inserts an implicit super()): split it off,
        // weave into the remainder, and re-attach it up front. The guard consequently runs just
        // after the explicit constructor call — a language constraint, documented on the annotation.
        Statement specialCall = null;
        Statement originalBody = method.getCode();
        if (originalBody instanceof BlockStatement blockStatement && !blockStatement.getStatements().isEmpty()
                && isSpecialConstructorCall(blockStatement.getStatements().get(0))) {
            specialCall = blockStatement.getStatements().get(0);
            BlockStatement remainder = new BlockStatement(
                    new ArrayList<>(blockStatement.getStatements().subList(1, blockStatement.getStatements().size())),
                    blockStatement.getVariableScope());
            remainder.setSourcePosition(blockStatement);
            originalBody = remainder;
        }
        List<Statement> prologue = new ArrayList<>();
        if (specialCall != null) prologue.add(specialCall);
        String suffix = Long.toString(COUNTER.getAndIncrement());

        if (!anyChecked) {
            // Guards only, in declaration order.
            int before = prologue.size();
            for (Arm arm : arms) {
                if (arm.woven) prologue.add(guardFor(arm, arm.condition.transformExpression(e -> e)));
            }
            if (prologue.size() == before) return;   // nothing woven, nothing checked — metadata only
            prologue.add(originalBody);
        } else {
            // Snapshot every arm's condition on entry (the only-when judgement needs all of them),
            // reuse the snapshots for the woven guards, then wrap the body with the checks.
            List<Expression> heldVars = new ArrayList<>();
            for (int i = 0; i < arms.size(); i++) {
                VariableExpression held = localVarX("$_gc_ti_c" + i + "_" + suffix, ClassHelper.boolean_TYPE);
                prologue.add(declS(held, boolX(arms.get(i).condition.transformExpression(e -> e))));
                heldVars.add(varX(held));
            }
            for (int i = 0; i < arms.size(); i++) {
                Arm arm = arms.get(i);
                if (arm.woven) prologue.add(guardFor(arm, heldVars.get(i)));
            }
            boolean allExhaustive = arms.stream().allMatch(a -> a.exhaustive);
            List<Expression> types = new ArrayList<>();
            List<Expression> texts = new ArrayList<>();
            List<Expression> unwovenHeld = new ArrayList<>();
            List<Expression> unwovenTexts = new ArrayList<>();
            for (int i = 0; i < arms.size(); i++) {
                Arm arm = arms.get(i);
                types.add(classX(arm.exceptionType));
                texts.add(constX(arm.conditionText));
                if (!arm.woven) {   // checked is SET-level: every non-woven arm is must-throw
                    // checked (woven arms hold must-throw by construction) — a sibling must not
                    // silently opt out while still serving as an only-when justifier
                    unwovenHeld.add(heldVars.get(i));
                    unwovenTexts.add(constX(arm.conditionText));
                }
            }
            VariableExpression thrown = localVarX("$_gc_ti_t_" + suffix, ClassHelper.make(Throwable.class));
            prologue.add(declS(thrown, nullX()));
            Parameter caught = new Parameter(ClassHelper.make(Throwable.class), "$_gc_ti_caught_" + suffix);
            Statement catchBody = block(
                    assignS(varX(thrown), varX(caught)),
                    throwS(callX(SUPPORT_TYPE, "onlyWhen", args(
                            varX(caught),
                            arrayOf(ClassHelper.boolean_TYPE, heldVars),
                            arrayOf(ClassHelper.CLASS_Type.getPlainNodeReference(), types),
                            arrayOf(ClassHelper.STRING_TYPE, texts),
                            constX(allExhaustive, true)))));
            Statement finallyBody = ifS(equalsNullX(varX(thrown)),
                    stmt(callX(SUPPORT_TYPE, "mustThrow", args(
                            arrayOf(ClassHelper.boolean_TYPE, unwovenHeld),
                            arrayOf(ClassHelper.STRING_TYPE, unwovenTexts)))));
            TryCatchStatement wrapper = new TryCatchStatement(originalBody, block(finallyBody));
            wrapper.addCatch(new CatchStatement(caught, catchBody));
            prologue.add(wrapper);
        }

        BlockStatement newBody = block(prologue.toArray(Statement[]::new));
        newBody.setSourcePosition(originalBody);
        method.setCode(newBody);

        // The conditions' variable references came from annotation closures and may not have been
        // resolved; re-run scope analysis now that they are real method-body expressions so
        // @TypeChecked/@CompileStatic can see declared types.
        LoopContractSupport.resolveVariableScopes(source);
    }

    /** All well-formed {@code @ThrowsIf} arms on the method, in declaration order. */
    private static List<Arm> collectArms(final MethodNode method) {
        List<Arm> arms = new ArrayList<>();
        for (AnnotationNode annotation : method.getAnnotations(THROWS_IF_TYPE)) {
            Expression value = annotation.getMember("value");
            if (!(value instanceof ClosureExpression closureExpression)) continue;
            Expression condition = MethodVariantASTTransformation.extractExpression(closureExpression);
            if (condition == null) continue;
            Arm arm = new Arm();
            arm.condition = condition;
            arm.conditionText = condition.getText();
            arm.exceptionType = exceptionType(annotation);
            arm.woven = !isFalse(annotation.getMember("woven"));
            arm.checked = isTrue(annotation.getMember("checked"));
            arm.exhaustive = !isFalse(annotation.getMember("exhaustive"));
            arms.add(arm);
        }
        return arms;
    }

    /** {@code if (<held>) throw new <exception>('@ThrowsIf: <condition>')} */
    private static Statement guardFor(final Arm arm, final Expression held) {
        Statement guard = ifS(boolX(held),
                throwS(ctorX(arm.exceptionType, args(constX("@ThrowsIf: " + arm.conditionText)))));
        guard.setSourcePosition(arm.condition);
        return guard;
    }

    /** True when the statement is a constructor's explicit {@code super(...)}/{@code this(...)} call. */
    private static boolean isSpecialConstructorCall(final Statement statement) {
        return statement instanceof ExpressionStatement expressionStatement
                && expressionStatement.getExpression() instanceof ConstructorCallExpression call
                && call.isSpecialCall();
    }

    private static Expression arrayOf(final ClassNode elementType, final List<Expression> elements) {
        return new ArrayExpression(elementType, elements);
    }

    private static ClassNode exceptionType(final AnnotationNode annotation) {
        Expression member = annotation.getMember("exception");
        if (member instanceof ClassExpression classExpression) {
            return classExpression.getType();
        }
        return ClassHelper.make(Throwable.class);
    }

    private static boolean isTrue(final Expression e) {
        return e instanceof ConstantExpression constant && Boolean.TRUE.equals(constant.getValue());
    }

    private static boolean isFalse(final Expression e) {
        return e instanceof ConstantExpression constant && Boolean.FALSE.equals(constant.getValue());
    }
}
