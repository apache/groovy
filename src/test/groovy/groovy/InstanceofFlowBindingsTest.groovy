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
package groovy

import org.codehaus.groovy.ast.DynamicVariable
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.classgen.InstanceofFlowBindings
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.junit.jupiter.api.Test

final class InstanceofFlowBindingsTest {

    @Test
    void testBindingsOfPositiveInstanceof() {
        def b = InstanceofFlowBindings.of(parseCondition('o instanceof String s'))
        assert b.whenTrue()*.name == ['s']
        assert b.whenFalse().isEmpty()
        assert b.allNames() as List == ['s']
    }

    @Test
    void testBindingsOfNegatedInstanceof() {
        def b = InstanceofFlowBindings.of(parseCondition('!(o instanceof String s)'))
        assert b.whenTrue().isEmpty()
        assert b.whenFalse()*.name == ['s']
        assert b.allNames() as List == ['s']
    }

    @Test
    void testBindingsOfAnd() {
        def b = InstanceofFlowBindings.of(parseCondition('o instanceof String s && s.length() > 0'))
        assert b.whenTrue()*.name == ['s']
        assert b.whenFalse().isEmpty()
    }

    @Test
    void testBindingsOfOr() {
        def b = InstanceofFlowBindings.of(parseCondition('o instanceof String s || s.length() > 0'))
        assert b.whenTrue().isEmpty()
        assert b.whenFalse().isEmpty()
        assert b.isEmpty()
    }

    @Test
    void testBindingsOfNegatedOrFalsePath() {
        def b = InstanceofFlowBindings.of(parseCondition('!(o instanceof String s) || s.isEmpty()'))
        // left false-path binds s; OR false bindings = left.whenFalse ∪ right.whenFalse = {s}
        assert b.whenFalse()*.name == ['s']
        assert b.whenTrue().isEmpty()
    }

    @Test
    void testBindingsOfPlainInstanceofAreEmpty() {
        def b = InstanceofFlowBindings.of(parseCondition('o instanceof String'))
        assert b.isEmpty()
    }

    @Test
    void testOfNullIsEmpty() {
        assert InstanceofFlowBindings.of(null).isEmpty()
    }

    @Test
    void testContainsPattern() {
        assert InstanceofFlowBindings.containsPattern(parseCondition('o instanceof String s'))
        assert InstanceofFlowBindings.containsPattern(parseCondition('!(o instanceof String s)'))
        assert InstanceofFlowBindings.containsPattern(parseCondition('o instanceof String s && s'))
        assert !InstanceofFlowBindings.containsPattern(parseCondition('o instanceof String'))
        assert !InstanceofFlowBindings.containsPattern(null)
    }

    @Test
    void testContainsPatternNestedInCall() {
        def expr = parseMethodArg('m(o instanceof String s)')
        assert InstanceofFlowBindings.containsPattern(expr)
    }

    @Test
    void testRightOfOrIsDynamicVariable() {
        def src = '''
            class C {
                def m(Object o) {
                    if (o instanceof String s || s.length() > 0) {
                        return 1
                    }
                    return 0
                }
            }
        '''
        def cu = new CompilationUnit()
        cu.addSource('C.groovy', src)
        cu.compile(Phases.SEMANTIC_ANALYSIS)
        def accesses = []
        cu.ast.classes[0].getMethods('m')[0].code.visit(new CodeVisitorSupport() {
            @Override
            void visitVariableExpression(VariableExpression ve) {
                if (ve.name == 's') {
                    accesses << ve.accessedVariable
                }
                super.visitVariableExpression(ve)
            }
        })
        assert accesses.any { it instanceof DynamicVariable } :
                "expected DynamicVariable for RHS of ||, got: ${accesses*.class*.simpleName}"
    }

    @Test
    void testNegatedIfBranchIsDynamicVariable() {
        def src = '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s)) {
                        return s
                    }
                    return null
                }
            }
        '''
        def cu = new CompilationUnit()
        cu.addSource('C.groovy', src)
        cu.compile(Phases.SEMANTIC_ANALYSIS)
        def accesses = []
        cu.ast.classes[0].getMethods('m')[0].code.visit(new CodeVisitorSupport() {
            @Override
            void visitVariableExpression(VariableExpression ve) {
                if (ve.name == 's') {
                    accesses << ve.accessedVariable
                }
                super.visitVariableExpression(ve)
            }
        })
        // declaration + use in if-branch (must be DynamicVariable)
        assert accesses.count { it instanceof DynamicVariable } >= 1
    }

    // -------------------------------------------------------------------------
    // GROOVY-12242: systematic binding-analysis unit tests
    //
    // Covers every condition shape in the visibility matrix:
    //   (1) instanceof s
    //   (2) !instanceof s
    //   (3) !instanceof s (negated via BooleanExpression)
    //   (4) instanceof s && cond
    //   (5) instanceof s || cond
    //   (6) !instanceof s && cond
    //   (7) !instanceof s || cond
    //   (8) !(instanceof s && cond)
    //   (9) double negation !!(instanceof s)
    // -------------------------------------------------------------------------

    // (4) o instanceof String s && cond — true: {s}, false: {}
    @Test
    void testAndWithPattern_trueBinds_falseEmpty() {
        def b = InstanceofFlowBindings.of(parseCondition('o instanceof String s && true'))
        assert b.whenTrue()*.name == ['s']
        assert b.whenFalse().isEmpty()
    }

    // (5) o instanceof String s || cond — both paths empty (can't guarantee s on true path,
    //     and cond alone doesn't bind s on the false path)
    @Test
    void testOrWithPattern_bothEmpty() {
        def b = InstanceofFlowBindings.of(parseCondition('o instanceof String s || true'))
        assert b.isEmpty()
    }

    // (6) !(o instanceof String s) && cond
    //     Left's false path binds s, but && propagates no false-bindings.
    //     Left's true path is empty. Right contributes nothing.
    //     → true: {}, false: {}
    @Test
    void testNegatedAndCond_bothEmpty() {
        def b = InstanceofFlowBindings.of(parseCondition('!(o instanceof String s) && true'))
        assert b.isEmpty()
    }

    // (7) !(o instanceof String s) || cond
    //     Left false-path binds s (since !(s) is false → s matched).
    //     Right's false path is empty. || false-path = union of false-paths = {s}.
    //     → true: {}, false: {s}
    @Test
    void testNegatedOrCond_falseBinds() {
        def b = InstanceofFlowBindings.of(parseCondition('!(o instanceof String s) || true'))
        assert b.whenFalse()*.name == ['s']
        assert b.whenTrue().isEmpty()
    }

    // (8) !(o instanceof String s && s.length() > 0)
    //     Inner: true:{s}, false:{}. Negated: true:{}, false:{s}.
    //     → true: {}, false: {s}
    @Test
    void testNegatedAndPattern_falseBindsAfterNegation() {
        def b = InstanceofFlowBindings.of(parseCondition('!(o instanceof String s && s.length() > 0)'))
        assert b.whenFalse()*.name == ['s']
        assert b.whenTrue().isEmpty()
    }

    // (9) double negation !!(o instanceof String s) ≡ o instanceof String s
    //     Inner: true:{s}, false:{}. Negated once: true:{}, false:{s}.
    //     Negated twice: true:{s}, false:{}.
    @Test
    void testDoubleNegation_sameAsPositive() {
        def b = InstanceofFlowBindings.of(parseCondition('!!(o instanceof String s)'))
        assert b.whenTrue()*.name == ['s']
        assert b.whenFalse().isEmpty()
    }

    // (4) o instanceof String s && p instanceof Integer i  (JLS §6.3.1.1)
    // Rule B: a&&b when-true = a.whenTrue ∪ b.whenTrue = {s} ∪ {i} = {s, i}
    // Rule A: s (introduced by a when true) is definitely matched at b — legal,
    // different names, no §6.3.1.1-200-A error.
    // No rule for when-false (§6.3.1.1 note: cannot determine which side failed).
    @Test
    void testAndWithTwoPatterns_bothInTrue() {
        def b = InstanceofFlowBindings.of(
                parseCondition2('o instanceof String s && p instanceof Integer i'))
        assert b.whenTrue()*.name as Set == ['s', 'i'] as Set
        assert b.whenFalse().isEmpty()
    }

    // (5) o instanceof String s || p instanceof Integer i  (JLS §6.3.1.2)
    // Rule B: a||b when-false = a.whenFalse ∪ b.whenFalse
    //   a.whenFalse for (o instanceof String s) = {} (§6.3.1.5: no when-false rule for instanceof)
    //   b.whenFalse for (p instanceof Integer i) = {}
    //   a||b when-false = {} ∪ {} = {}
    // No rule for when-true (§6.3.1.2 note: cannot determine which side was true).
    // Different names s/i → no §6.3.1.2-200-A or -200-B error.
    @Test
    void testOrWithTwoPatterns_bothEmpty() {
        def b = InstanceofFlowBindings.of(
                parseCondition2('o instanceof String s || p instanceof Integer i'))
        assert b.isEmpty()
    }

    // !(a instanceof String s) || !(b instanceof Integer i)  (JLS §6.3.1.2 + §6.3.1.3)
    // !(o instanceof String s): when-false = {s}  (§6.3.1.3: !a when-false = a.whenTrue)
    // !(p instanceof Integer i): when-false = {i}
    // Rule A: s (introduced by left when false) is in scope at right (§6.3.1.2-100-A).
    // Rule B: a||b when-false = {s} ∪ {i} = {s, i}
    // Different names s/i → no §6.3.1.2-200-B error.
    @Test
    void testOrWithTwoNegatedPatterns_falseHasBoth() {
        def b = InstanceofFlowBindings.of(
                parseCondition2('!(o instanceof String s) || !(p instanceof Integer i)'))
        assert b.whenFalse()*.name as Set == ['s', 'i'] as Set
        assert b.whenTrue().isEmpty()
    }

    // isEmpty() is false when only whenFalse is non-empty
    @Test
    void testIsEmpty_falseWhenOnlyFalseSide() {
        def b = InstanceofFlowBindings.of(parseCondition('!(o instanceof String s)'))
        assert !b.isEmpty()
        assert b.whenTrue().isEmpty()
        assert !b.whenFalse().isEmpty()
    }

    // allNames() returns names from both sides (stable order)
    @Test
    void testAllNames_mergesBothSides() {
        // construct a condition that has s on true and t on false:
        // (o instanceof String s && true) || !(o instanceof Integer t)
        // → true: {} (|| doesn't guarantee), false: {t} (from right)
        // Use allNames on something that has both sides:
        // !(o instanceof String s) alone: whenFalse={s}, whenTrue={}; allNames={s}
        def b1 = InstanceofFlowBindings.of(parseCondition('!(o instanceof String s)'))
        assert b1.allNames() == ['s'] as Set

        // positive: whenTrue={s}, whenFalse={}
        def b2 = InstanceofFlowBindings.of(parseCondition('o instanceof String s'))
        assert b2.allNames() == ['s'] as Set
    }

    // whenTrueNames() / whenFalseNames() return Set (not List)
    // Uses a legal condition; tests that the return type is a Set.
    @Test
    void testNameSets_returnSets() {
        // o instanceof String s && true  →  whenTrue introduces {s}, whenFalse is {}
        def b = InstanceofFlowBindings.of(parseCondition('o instanceof String s && true'))
        assert b.whenTrueNames() instanceof Set
        assert b.whenFalseNames() instanceof Set
        assert b.whenTrueNames() == ['s'] as Set
        assert b.whenFalseNames().isEmpty()
    }

    // JLS §6.3.1.1-200-A: it is a compile-time error when the same pattern variable name
    // is introduced by BOTH operands of &&.  InstanceofFlowBindings is a pre-error analysis
    // (it runs before the error is reported) and will conservatively union the sets; the
    // resulting program is still rejected at compile time by VariableScopeVisitor.
    // This test documents that behavior without asserting on specific names (the analysis
    // result for rejected input is unspecified / implementation-defined).
    @Test
    void testJLS_6_3_1_1_DuplicateNameIsCompileError_analysisStillRuns() {
        // 'o instanceof String s && o instanceof String s' — same name s on both sides,
        // §6.3.1.1-200-A error.  The analysis sees both, union deduplicates to size 1.
        // We only check it doesn't throw; the compiler will still reject the source.
        def b = InstanceofFlowBindings.of(parseCondition('o instanceof String s && o instanceof String s'))
        // whenTrue may contain s (deduped) — at minimum the result is not null / EMPTY
        assert b != null
        // The union of {s} and {s} should deduplicate to one entry
        assert b.whenTrueNames().size() == 1 : 'union should deduplicate same-named vars'
        assert b.whenFalseNames().isEmpty()  : 'no false-path bindings for &&'
    }

    private static Expression parseCondition(String condition) {
        def src = """
            class C {
                def m(Object o) {
                    if ($condition) return 1
                    return 0
                }
            }
        """
        def cu = new CompilationUnit()
        cu.addSource('C.groovy', src)
        cu.compile(Phases.CONVERSION)
        IfStatement ifStmt = null
        cu.ast.classes[0].getMethods('m')[0].code.visit(new CodeVisitorSupport() {
            @Override
            void visitIfElse(IfStatement statement) {
                ifStmt = statement
            }
        })
        ifStmt.booleanExpression.expression
    }

    private static Expression parseMethodArg(String statement) {
        def src = """
            class C {
                def m(Object o) {
                    $statement
                }
            }
        """
        def cu = new CompilationUnit()
        cu.addSource('C.groovy', src)
        cu.compile(Phases.CONVERSION)
        Expression found = null
        cu.ast.classes[0].getMethods('m')[0].code.visit(new CodeVisitorSupport() {
            @Override
            void visitMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression call) {
                if (call.methodAsString == 'm' || call.objectExpression.text == 'this') {
                    found = call
                }
                super.visitMethodCallExpression(call)
            }
            @Override
            void visitExpressionStatement(org.codehaus.groovy.ast.stmt.ExpressionStatement stmt) {
                found = stmt.expression
                super.visitExpressionStatement(stmt)
            }
        })
        found
    }

    /**
     * Variant of {@link #parseCondition} for conditions that reference two variables
     * ({@code o} and {@code p}), e.g. multi-pattern {@code &&} / {@code ||} combinations.
     */
    private static Expression parseCondition2(String condition) {
        def src = """
            class C {
                def m(Object o, Object p) {
                    if ($condition) return 1
                    return 0
                }
            }
        """
        def cu = new CompilationUnit()
        cu.addSource('C.groovy', src)
        cu.compile(Phases.CONVERSION)
        IfStatement ifStmt = null
        cu.ast.classes[0].getMethods('m')[0].code.visit(new CodeVisitorSupport() {
            @Override
            void visitIfElse(IfStatement statement) {
                ifStmt = statement
            }
        })
        ifStmt.booleanExpression.expression
    }
}
