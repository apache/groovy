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

import groovy.transform.Internal;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.syntax.Types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Flow-sensitive analysis of JEP&nbsp;394 {@code instanceof} pattern bindings
 * (GROOVY-12242).
 * <p>
 * This is pure <em>semantic</em> analysis: given a boolean expression, which
 * pattern variables are <em>definitely bound</em> when the expression is
 * {@code true} versus {@code false}? (Same idea as compiler “flow info” /
 * JEP 394 flow scoping — not a bytecode construct.)
 * <ul>
 *   <li>{@link #of(Expression)} — true/false binding sets for a condition</li>
 *   <li>{@link #containsPattern(Expression)} — nested type-pattern presence
 *       (e.g. whether an expression statement needs CompileStack isolation)</li>
 * </ul>
 * Covered shapes: {@code e instanceof T t}, negation / {@code !instanceof},
 * {@code &&} (union of true bindings), {@code ||} (union of false bindings).
 * Other shapes contribute nothing (conservative).
 * <p>
 * <b>Design note — dual use across compiler phases.</b><br>
 * This class is consumed by two separate compiler phases:
 * <ol>
 *   <li>{@link VariableScopeVisitor} (semantic analysis) — uses the binding
 *       sets to declare pattern-variable names only on the live path, so that
 *       subsequent name resolution sees the correct scope.</li>
 *   <li>{@link org.codehaus.groovy.classgen.asm.InstanceofFlowSlotPublisher}
 *       (code generation) — uses the same binding sets to publish/hide bytecode
 *       locals on the matching control-flow arm, keeping CompileStack slot
 *       visibility consistent with the resolved scopes.</li>
 * </ol>
 * Both phases need to ask the same question ("which names are live on which
 * path?"), so sharing this analysis type avoids two independent implementations
 * that could diverge.
 *
 * @see org.codehaus.groovy.classgen.asm.InstanceofFlowSlotPublisher
 * @since 6.0.0
 */
@Internal
public final class InstanceofFlowBindings {

    private static final InstanceofFlowBindings EMPTY =
            new InstanceofFlowBindings(List.of(), List.of());

    private final List<VariableExpression> whenTrue;
    private final List<VariableExpression> whenFalse;

    private InstanceofFlowBindings(final List<VariableExpression> whenTrue,
                                      final List<VariableExpression> whenFalse) {
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
    }

    /**
     * Pattern variables that are definitely assigned when the analysed expression
     * evaluates to {@code true}.
     */
    public List<VariableExpression> whenTrue() {
        return whenTrue;
    }

    /**
     * Pattern variables that are definitely assigned when the analysed expression
     * evaluates to {@code false}.
     */
    public List<VariableExpression> whenFalse() {
        return whenFalse;
    }

    /** Whether any pattern variable is bound on either path. */
    public boolean isEmpty() {
        return whenTrue.isEmpty() && whenFalse.isEmpty();
    }

    /**
     * Names of pattern variables bound when the expression is {@code true}.
     */
    public Set<String> whenTrueNames() {
        return names(whenTrue);
    }

    /**
     * Names of pattern variables bound when the expression is {@code false}.
     */
    public Set<String> whenFalseNames() {
        return names(whenFalse);
    }

    /**
     * All pattern-variable names appearing in either path (stable encounter order).
     * <p>
     * Implemented by iterating both lists directly rather than composing
     * {@link #whenTrueNames()} and {@link #whenFalseNames()}: that would
     * allocate two intermediate {@link Set} objects only to merge them into a
     * third, whereas the direct loop allocates only the result set.
     */
    public Set<String> allNames() {
        if (isEmpty()) return Collections.emptySet();
        Set<String> names = new LinkedHashSet<>(whenTrue.size() + whenFalse.size());
        for (VariableExpression ve : whenTrue) names.add(ve.getName());
        for (VariableExpression ve : whenFalse) names.add(ve.getName());
        return names;
    }

    private static Set<String> names(final List<VariableExpression> vars) {
        if (vars.isEmpty()) return Collections.emptySet();
        Set<String> result = new LinkedHashSet<>(vars.size());
        for (VariableExpression ve : vars) {
            result.add(ve.getName());
        }
        return result;
    }

    /**
     * Analyses {@code expression} for definite {@code instanceof} pattern bindings.
     *
     * @param expression a boolean condition (may be a {@link BooleanExpression} wrapper)
     * @return the true/false binding sets; never {@code null}
     */
    public static InstanceofFlowBindings of(final Expression expression) {
        if (expression == null) {
            return EMPTY;
        }
        return analyse(expression);
    }

    /**
     * Returns {@code true} if {@code expression} contains any JEP&nbsp;394 type
     * pattern ({@code e instanceof T t} or {@code e !instanceof T t}), including
     * <em>arbitrarily nested</em> subexpressions (e.g. a pattern buried inside a
     * method-call argument or a ternary).
     * <p>
     * <strong>Why a full subtree walk?</strong> This method is used for
     * expression-statement isolation: before discarding an expression-statement's
     * value, the code generator needs to know whether <em>any</em> descendant
     * node may have allocated a CompileStack slot for a pattern variable, even
     * if {@link #of} does not model that node (e.g. a pattern inside a method
     * argument). The conservative "does any descendant match?" question requires
     * visiting the whole tree. Short-circuiting ({@code found[0]} check) stops
     * the walk as soon as the first match is detected.
     *
     * @param expression any expression; {@code null} yields {@code false}
     */
    public static boolean containsPattern(final Expression expression) {
        if (expression == null) return false;
        boolean[] found = {false};
        expression.visit(new CodeVisitorSupport() {
            @Override
            public void visitBinaryExpression(final BinaryExpression be) {
                if (found[0]) return;
                int op = be.getOperation().getType();
                if ((op == Types.KEYWORD_INSTANCEOF || op == Types.COMPARE_NOT_INSTANCEOF)
                        && isTypePattern(be.getRightExpression())) {
                    found[0] = true;
                    return;
                }
                super.visitBinaryExpression(be);
            }
        });
        return found[0];
    }

    private static boolean isTypePattern(final Expression right) {
        return right instanceof DeclarationExpression decl
                && !decl.isMultipleAssignmentDeclaration()
                && decl.getVariableExpression() != null;
    }

    /**
     * Core recursive descent that computes binding sets.
     * <p>
     * <strong>Why not a full subtree walk?</strong> Unlike {@link #containsPattern},
     * this method only needs to understand the <em>boolean algebra</em> of the
     * condition (which variables are <em>definitely</em> bound on each path),
     * not to locate patterns anywhere in an arbitrary expression tree. The
     * recursive descent follows exactly the operators that can propagate
     * definite-assignment ({@code instanceof}, {@code !instanceof}, {@code &&},
     * {@code ||}, {@code !}) and returns {@link #EMPTY} conservatively for any
     * other expression shape. This keeps the traversal shallow and
     * proportional to the boolean structure of the condition, not to the total
     * AST size.
     */
    private static InstanceofFlowBindings analyse(final Expression expression) {
        Expression expr = expression;

        // Unwrap BooleanExpression wrappers; NotExpression is handled below so that
        // nested negations compose correctly.
        while (expr instanceof BooleanExpression && !(expr instanceof NotExpression)) {
            expr = ((BooleanExpression) expr).getExpression();
        }

        if (expr instanceof NotExpression not) {
            return analyse(not.getExpression()).negated();
        }

        if (expr instanceof BinaryExpression binary) {
            int op = binary.getOperation().getType();
            if (op == Types.KEYWORD_INSTANCEOF) {
                return ofInstanceof(binary);
            }
            if (op == Types.COMPARE_NOT_INSTANCEOF) {
                // AST may still carry !instanceof before codegen rewrites it to !(… instanceof …).
                return ofInstanceof(binary).negated();
            }
            if (op == Types.LOGICAL_AND) {
                InstanceofFlowBindings left = analyse(binary.getLeftExpression());
                InstanceofFlowBindings right = analyse(binary.getRightExpression());
                // True path: the condition succeeds only if both sides are true, so
                // both sides' true-bindings are definitely assigned. The false path is
                // not definite: either side alone may have caused failure.
                return new InstanceofFlowBindings(
                        union(left.whenTrue, right.whenTrue),
                        List.of());
            }
            if (op == Types.LOGICAL_OR) {
                InstanceofFlowBindings left = analyse(binary.getLeftExpression());
                InstanceofFlowBindings right = analyse(binary.getRightExpression());
                // False path: the condition fails only if both sides are false, so
                // both sides' false-bindings are definitely assigned. The true path
                // is not definite: only the left side may have been evaluated.
                return new InstanceofFlowBindings(
                        List.of(),
                        union(left.whenFalse, right.whenFalse));
            }
        }

        return EMPTY;
    }

    private static InstanceofFlowBindings ofInstanceof(final BinaryExpression binary) {
        Expression right = binary.getRightExpression();
        if (isTypePattern(right)) {
            VariableExpression patternVar = ((DeclarationExpression) right).getVariableExpression();
            return new InstanceofFlowBindings(List.of(patternVar), List.of());
        }
        return EMPTY;
    }

    private InstanceofFlowBindings negated() {
        if (isEmpty()) return this;
        return new InstanceofFlowBindings(whenFalse, whenTrue);
    }

    private static List<VariableExpression> union(final List<VariableExpression> a,
                                                  final List<VariableExpression> b) {
        if (a.isEmpty()) return b;
        if (b.isEmpty()) return a;
        List<VariableExpression> result = new ArrayList<>(a.size() + b.size());
        Set<String> seen = new LinkedHashSet<>();
        for (VariableExpression ve : a) {
            if (seen.add(ve.getName())) result.add(ve);
        }
        for (VariableExpression ve : b) {
            if (seen.add(ve.getName())) result.add(ve);
        }
        return List.copyOf(result);
    }
}
