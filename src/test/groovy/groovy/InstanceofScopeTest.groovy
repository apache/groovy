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

import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.DynamicVariable
import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test

/**
 * AST-level scope tests for JEP 394 {@code instanceof} pattern variables
 * (GROOVY-12242), verifying that {@link org.codehaus.groovy.classgen.VariableScopeVisitor}
 * correctly scopes each pattern variable for every condition shape in the
 * visibility matrix.
 * <p>
 * Each test compiles to {@link Phases#SEMANTIC_ANALYSIS} (which runs
 * {@code VariableScopeVisitor}) and then inspects
 * {@link VariableExpression#getAccessedVariable()} to verify that:
 * <ul>
 *   <li>References <em>inside</em> the scope resolve to the pattern
 *       {@link DeclarationExpression}'s {@link VariableExpression} (i.e. the
 *       declared local, not a dynamic lookup).</li>
 *   <li>References <em>outside</em> the scope resolve to a
 *       {@link DynamicVariable} — which at runtime produces a
 *       {@link MissingPropertyException}, enforcing the JLS §6.3 rule in
 *       dynamic Groovy without any need for {@code @TypeChecked}.</li>
 * </ul>
 *
 * <h2>Visibility matrix (JLS §6.3.2.2 / JEP 394)</h2>
 * <pre>
 *  #  | Condition shape                       | if-block | else-block | after if-else
 *  ---|--------------------------------------|----------|------------|---------------
 *  1  | o instanceof String s                 | local    | dynamic    | dynamic (*)
 *  1b | o instanceof String s, else abrupt    | local    | (abrupt)   | local
 *  2  | !(o instanceof String s)              | dynamic  | local      | —
 *  3  | !(o instanceof String s), if abrupt   | (abrupt) | local      | local
 *  4  | o instanceof String s &amp;&amp; cond         | local    | dynamic    | dynamic
 *  5  | o instanceof String s || cond         | dynamic  | dynamic    | dynamic
 *  6  | !(o instanceof String s) &amp;&amp; cond      | dynamic  | —          | dynamic
 *  7a | !(o instanceof s) &amp;&amp; cond, else abrupt| dynamic  | (abrupt)   | dynamic
 *  7b | !(o instanceof s), abrupt else only   | dynamic  | (abrupt)   | dynamic (**)
 *  8  | !(o instanceof String s) || cond      | —        | local      | —
 * </pre>
 * (*) s NOT visible after when both branches fall through.
 * (**) missing case: abrupt else alone is not enough; if-block must
 * also be abrupt for §6.3.2.2-200-C-B to apply.
 */
final class InstanceofScopeTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Compiles {@code src} through {@link Phases#SEMANTIC_ANALYSIS} and
     * collects all {@code accessedVariable} values for {@link VariableExpression}s
     * that reference a variable named {@code varName} inside method {@code m} of
     * class {@code C}, excluding the declaration site itself.
     */
    private static List<Variable> collectAccesses(String src, String varName = 's') {
        def cu = new CompilationUnit()
        cu.addSource('C.groovy', src)
        cu.compile(Phases.SEMANTIC_ANALYSIS)
        List<Variable> accesses = []
        cu.ast.classes.find { it.name == 'C' }
                .getMethods('m')[0].code
                .visit(new CodeVisitorSupport() {
            @Override
            void visitVariableExpression(VariableExpression ve) {
                // Include only references, not the declaration site.
                // The declaration site has accessedVariable == ve (set to itself by declare()).
                if (ve.name == varName && ve.accessedVariable !== ve) {
                    accesses << ve.accessedVariable
                }
                super.visitVariableExpression(ve)
            }
        })
        accesses
    }

    /** True if the accessed variable is a local (not a DynamicVariable). */
    private static boolean isLocal(Variable v) {
        !(v instanceof DynamicVariable)
    }

    // -------------------------------------------------------------------------
    // Case 1: o instanceof String s
    // -------------------------------------------------------------------------

    /**
     * JLS §6.3.2.2-200-A: e.whenTrue is in scope in the then-block (S).
     * e.whenFalse ({}) is in scope in the else-block (T) — so s is dynamic there.
     * No abrupt completion → s is NOT introduced after the if-else.
     */
    @Test
    void testCase1_simpleInstanceof_ifBlockLocal_elseBlockDynamic_afterDynamic() {
        def src = '''
            class C {
                def m(Object o) {
                    if (o instanceof String s) {
                        s.length()        // use in if-block  → must be local
                    } else {
                        s                 // use in else-block → must be dynamic
                    }
                    s                     // use after if-else → must be dynamic
                }
            }
        '''
        def accesses = collectAccesses(src)
        assert accesses.size() == 3
        assert isLocal(accesses[0])   : "if-block: s must be local (JLS §6.3.2.2-200-A)"
        assert !isLocal(accesses[1])  : "else-block: s must be dynamic (e.whenFalse={})"
        assert !isLocal(accesses[2])  : "after if: s must be dynamic (both branches fall through)"
    }

    // -------------------------------------------------------------------------
    // Case 1b: o instanceof String s, else cannot complete normally
    // -------------------------------------------------------------------------

    /**
     * JLS §6.3.2.2-200-C-A: introduced by e.whenTrue={s}, S can complete normally,
     * T cannot complete normally → s IS introduced after the if-else.
     */
    @Test
    void testCase1b_simpleInstanceof_abruptElse_afterLocal() {
        def src = '''
            class C {
                def m(Object o) {
                    if (o instanceof String s) {
                        // S falls through
                    } else {
                        throw new IllegalArgumentException()
                    }
                    s                     // after: T is abrupt, S falls through → s visible (C-A)
                }
            }
        '''
        def accesses = collectAccesses(src)
        assert accesses.size() == 1
        assert isLocal(accesses[0])   : "after if (abrupt else): s must be local (JLS §6.3.2.2-200-C-A)"
    }

    // -------------------------------------------------------------------------
    // Case 2: !(o instanceof String s)
    // -------------------------------------------------------------------------

    /**
     * JLS §6.3.2.2-200-B: e.whenFalse={s} is in scope in the else-block (T).
     * JLS §6.3.2.2-200-A: e.whenTrue={} → if-block does NOT see s.
     */
    @Test
    void testCase2_negatedInstanceof_ifBlockDynamic_elseBlockLocal() {
        def src = '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s)) {
                        s                 // use in if-block  → must be dynamic (e.whenTrue={})
                    } else {
                        s.length()        // use in else-block → must be local (e.whenFalse={s})
                    }
                }
            }
        '''
        def accesses = collectAccesses(src)
        assert accesses.size() == 2
        assert !isLocal(accesses[0])  : "if-block: s must be dynamic (JLS §6.3.2.2-200-A, whenTrue={})"
        assert isLocal(accesses[1])   : "else-block: s must be local (JLS §6.3.2.2-200-B, whenFalse={s})"
    }

    // -------------------------------------------------------------------------
    // Case 3: !(o instanceof String s), abrupt if-block (early return/throw)
    // -------------------------------------------------------------------------

    /**
     * JLS §6.3.2.2-200-C-B: introduced by e.whenFalse={s}, S cannot complete
     * normally (early return), T can → s IS introduced after the if statement.
     */
    @Test
    void testCase3_negatedInstanceof_abruptIf_afterLocal() {
        def src = '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s)) {
                        return null       // S cannot complete normally
                    }
                    s.length()           // after: S abrupt, no else → s visible (C-B)
                }
            }
        '''
        def accesses = collectAccesses(src)
        assert accesses.size() == 1
        assert isLocal(accesses[0])   : "after if (abrupt if-block): s must be local (JLS §6.3.2.2-200-C-B)"
    }

    // -------------------------------------------------------------------------
    // Case 4: o instanceof String s && cond
    // -------------------------------------------------------------------------

    /**
     * JLS §6.3.1.1: a&&b when-true = {s} → if-block sees s.
     * JLS §6.3.1.1 (note): no when-false rule → else-block does NOT see s.
     * No abrupt completion → after if-else does NOT see s.
     */
    @Test
    void testCase4_andChain_ifBlockLocal_elseBlockDynamic_afterDynamic() {
        def src = '''
            class C {
                def m(Object o) {
                    if (o instanceof String s && s.length() > 0) {
                        s.toUpperCase()   // if-block → local
                    } else {
                        s                 // else-block → dynamic
                    }
                    s                     // after → dynamic
                }
            }
        '''
        def accesses = collectAccesses(src)
        // s appears in: condition (RHS of &&, already in scope), if-block, else-block, after
        // The && RHS visit also resolves s; let's filter just the block uses:
        // accesses are in AST visit order: condition-RHS s, then if-block s, else-block s, after s
        assert accesses.size() >= 3
        // The if-block use and after-if uses are tracked; find the else and after ones
        // All local ones should be the if-block one, all dynamic ones the else and after
        def locals  = accesses.findAll { isLocal(it) }
        def dynamics = accesses.findAll { !isLocal(it) }
        assert !locals.isEmpty()   : "if-block (and && RHS) references to s must be local"
        assert !dynamics.isEmpty() : "else-block and after-if references to s must be dynamic"
    }

    /**
     * §6.3.1.1 Rule A: s (introduced by left when true) is definitely matched
     * at right — so the RHS of && also sees s as a local.
     */
    @Test
    void testCase4_andChain_rhs_seesPatternVarAsLocal() {
        def src = '''
            class C {
                def m(Object o) {
                    if (o instanceof String s && s.length() > 0) {
                        return 1
                    }
                    return 0
                }
            }
        '''
        def accesses = collectAccesses(src)
        // The only s reference is in the && RHS: must be local
        assert accesses.size() == 1
        assert isLocal(accesses[0])   : "RHS of && must see s as local (JLS §6.3.1.1 Rule A)"
    }

    // -------------------------------------------------------------------------
    // Case 5: o instanceof String s || cond
    // -------------------------------------------------------------------------

    /**
     * JLS §6.3.1.2 (note): no when-true rule for || → if-block does NOT see s.
     * VariableScopeVisitor declares nothing for the if-block → DynamicVariable.
     */
    @Test
    void testCase5_orChain_ifBlockDynamic() {
        def src = '''
            class C {
                def m(Object o) {
                    if (o instanceof String s || true) {
                        s                 // if-block → must be dynamic
                    }
                }
            }
        '''
        def accesses = collectAccesses(src)
        assert accesses.size() == 1
        assert !isLocal(accesses[0])  : "if-block with || condition: s must be dynamic (JLS §6.3.1.2 note)"
    }

    /**
     * JLS §6.3.1.2 Rule A: s introduced by left.whenFalse is in scope in the
     * right of ||. But the right of || is NOT where s is declared on a false path —
     * that is about the || sub-expression, not the if-block.
     * Specifically: `o instanceof String s || s.isEmpty()` — the s in `s.isEmpty()`
     * is the RIGHT of ||, which is inside the if's condition, NOT the if-block.
     * After || evaluates to true, VariableScopeVisitor gives the if-block e.whenTrue={}
     * → still dynamic in the if-block body.
     */
    @Test
    void testCase5_orChain_rhs_seesPatternFalseVar() {
        // Right side of ||: s.isEmpty() — s was introduced by left.whenFalse={s}
        // So this s reference should be local (JLS §6.3.1.2-100-A)
        def src = '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s) || s.isEmpty()) {
                        return 1
                    }
                    return 0
                }
            }
        '''
        def accesses = collectAccesses(src)
        // s in s.isEmpty() is the RHS of ||; it's introduced by left.whenFalse={s}
        assert accesses.size() == 1
        assert isLocal(accesses[0])   : "RHS of || (after !(instanceof s)): s must be local (JLS §6.3.1.2-100-A)"
    }

    // -------------------------------------------------------------------------
    // Case 6: !(o instanceof String s) && cond
    // -------------------------------------------------------------------------

    /**
     * JLS §6.3.1.3 + §6.3.1.1: !(instanceof s).whenTrue = {} →
     * (!(instanceof s) && cond).whenTrue = {} → if-block does NOT see s.
     */
    @Test
    void testCase6_negatedAndCond_ifBlockDynamic() {
        def src = '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s) && true) {
                        s                 // if-block → must be dynamic
                    }
                }
            }
        '''
        def accesses = collectAccesses(src)
        assert accesses.size() == 1
        assert !isLocal(accesses[0])  : "if-block: !(instanceof s) && cond → s must be dynamic"
    }

    /**
     * After if, with abrupt else: JLS §6.3.2.2-200-C-A:
     * e.whenTrue={}, T abrupt → C-A doesn't apply (e.whenTrue={}).
     * C-B: e.whenFalse={s}, S cannot complete normally? S = !(s) && cond if-block
     * can complete normally → C-B doesn't apply either. So s NOT visible after.
     */
    @Test
    void testCase7a_negatedAndCond_abruptElse_afterDynamic() {
        def src = '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s) && true) {
                        // S falls through
                    } else {
                        throw new RuntimeException()
                    }
                    s                     // after → must be dynamic (C-B doesn't apply)
                }
            }
        '''
        def accesses = collectAccesses(src)
        assert accesses.size() == 1
        assert !isLocal(accesses[0])  : "after if (!(instanceof s) && cond, abrupt else): s must be dynamic"
    }

    // -------------------------------------------------------------------------
    // Case 7b: missing case — !(o instanceof String s), abrupt else only
    // -------------------------------------------------------------------------

    /**
     * explicit missing case:
     * <pre>
     *   if (!(o instanceof String s)) {
     *     println "not String"   // S can complete normally
     *   } else {
     *     println "String"
     *     return                 // T cannot complete normally
     *   }
     *   println s  // s NOT visible: C-B requires S to be abrupt, but S falls through
     * </pre>
     *
     * JLS §6.3.2.2-200-C analysis:
     * e = !(o instanceof String s): whenFalse={s}
     * C-B: e.whenFalse={s} AND S cannot complete normally AND T can → required but S CAN complete → C-B does NOT apply
     * C-A: e.whenTrue={} → nothing
     * → s NOT introduced after the if-else statement.
     */
    @Test
    void testCase7b_negatedInstanceof_abruptElseOnly_afterDynamic() {
        def src = '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s)) {
                        println "not String"     // S can complete normally
                    } else {
                        println "String"
                        return s                 // T cannot complete normally
                    }
                    s                            // after → must be dynamic (C-B does NOT apply)
                }
            }
        '''
        def accesses = collectAccesses(src)
        // s appears in: else-block (return s) and after the if-else
        // else-block: e.whenFalse={s} → local
        // after: C-B does not apply → dynamic
        assert accesses.size() == 2
        assert isLocal(accesses[0])   : "else-block: s must be local (JLS §6.3.2.2-200-B, e.whenFalse={s})"
        assert !isLocal(accesses[1])  : "after if (abrupt-else-only): s must be dynamic (C-B does NOT apply)"
    }

    // -------------------------------------------------------------------------
    // Case 8: !(o instanceof String s) || cond
    // -------------------------------------------------------------------------

    /**
     * JLS §6.3.1.2 + §6.3.1.3:
     * !(instanceof s) → whenFalse={s}.
     * a || b: b is in scope for left.whenFalse={s} (Rule A).
     * a || b: whenFalse = left.whenFalse ∪ right.whenFalse = {s} ∪ {} = {s}.
     * → else-block sees s (e.whenFalse={s}, JLS §6.3.2.2-200-B).
     * → if-block does NOT see s (JLS §6.3.1.2 note: no whenTrue rule for ||).
     */
    @Test
    void testCase8_negatedOrCond_elseBlockLocal_ifBlockDynamic() {
        def src = '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s) || s.isEmpty()) {
                        s                 // if-block → dynamic (|| has no whenTrue rule)
                    } else {
                        s.length()        // else-block → local (e.whenFalse={s})
                    }
                }
            }
        '''
        def accesses = collectAccesses(src)
        // s appears in: condition RHS (s.isEmpty()), if-block, else-block
        // condition RHS: s introduced by left.whenFalse={s} via || Rule A → local
        // if-block: e.whenTrue={} → dynamic
        // else-block: e.whenFalse={s} → local
        assert accesses.size() == 3
        assert isLocal(accesses[0])   : "|| RHS (s.isEmpty): s must be local (JLS §6.3.1.2-100-A)"
        assert !isLocal(accesses[1])  : "if-block: s must be dynamic (JLS §6.3.1.2 note, no whenTrue rule)"
        assert isLocal(accesses[2])   : "else-block: s must be local (JLS §6.3.2.2-200-B, whenFalse={s})"
    }

    // -------------------------------------------------------------------------
    // Double negation / De Morgan identities
    // -------------------------------------------------------------------------

    /**
     * JLS §6.3.1.3 applied twice: !!(instanceof s) when-true = {s} — same as plain instanceof.
     */
    @Test
    void testDoubleNegation_ifBlockLocal() {
        def src = '''
            class C {
                def m(Object o) {
                    if (!!(o instanceof String s)) {
                        s.length()        // if-block → local
                    }
                }
            }
        '''
        def accesses = collectAccesses(src)
        assert accesses.size() == 1
        assert isLocal(accesses[0])   : "if-block with !! condition: s must be local (§6.3.1.3 applied twice)"
    }

    /**
     * De Morgan: !(a && b) ≡ !a || !b. No definite binding on either path.
     * But the && RHS (b = s.length() > 0) is in scope for the pattern-true
     * arm (JLS §6.3.1.1 Rule A), so the condition itself contains a local ref.
     */
    @Test
    void testDeMorgan_notAndNegation_bothDynamic() {
        def src = '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s && s.length() > 0)) {
                        s                 // if-block → dynamic (whenTrue of !(&&) = {})
                    } else {
                        s                 // else-block → local (whenFalse of !(&&) = {s})
                    }
                }
            }
        '''
        def accesses = collectAccesses(src)
        // Visit order: condition (s.length() > 0 inside &&), if-block s, else-block s.
        // !(a && b): whenTrue = (a&&b).whenFalse = {}; whenFalse = (a&&b).whenTrue = {s}
        // condition (&&) RHS: s.length() > 0 — left's whenTrue={s} is in scope (§6.3.1.1 Rule A) → local
        // if-block: e.whenTrue={} → dynamic
        // else-block: e.whenFalse={s} → local
        assert accesses.size() == 3
        assert isLocal(accesses[0])   : "condition (&&) RHS: s must be local (JLS §6.3.1.1 Rule A)"
        assert !isLocal(accesses[1])  : "if-block: !(&&) whenTrue={} → s must be dynamic"
        assert isLocal(accesses[2])   : "else-block: !(&&) whenFalse={s} → s must be local"
    }

    // -------------------------------------------------------------------------
    // Ternary expression (JLS §6.3.1.4)
    // -------------------------------------------------------------------------

    /**
     * JLS §6.3.1.4: a ? b : c introduces no bindings for when-true or when-false.
     * The condition's whenTrue={s} is visible in b, and whenFalse={} in c.
     */
    @Test
    void testTernaryExpression_trueExprLocal_falseExprDynamic() {
        def src = '''
            class C {
                def m(Object o) {
                    def r = (o instanceof String s) ? s.length() : s  // s in false-expr → dynamic
                    r
                }
            }
        '''
        def accesses = collectAccesses(src)
        // s in true-expr (s.length()) → local (condition.whenTrue={s})
        // s in false-expr (s) → dynamic (condition.whenFalse={})
        assert accesses.size() == 2
        assert isLocal(accesses[0])   : "ternary true-expr: s must be local (condition.whenTrue={s})"
        assert !isLocal(accesses[1])  : "ternary false-expr: s must be dynamic (condition.whenFalse={})"
    }

    // -------------------------------------------------------------------------
    // Redeclaration where pattern variable is NOT visible → new local is OK
    // -------------------------------------------------------------------------

    /**
     * Else-block of {@code o instanceof String s}: s is not in scope, so
     * {@code def s = ...} declares a fresh local. Uses of s in the else-block
     * must resolve to that local (not the pattern binding, not DynamicVariable).
     */
    @Test
    void testRedeclareInElseBlock_wherePatternNotVisible_isLocal() {
        def src = '''
            class C {
                def m(Object o) {
                    if (o instanceof String s) {
                        s.length()
                    } else {
                        def s = 'local'
                        s
                    }
                }
            }
        '''
        def accesses = collectAccesses(src)
        // if-block s (pattern local), else-block s (new local declaration's use)
        assert accesses.size() == 2
        assert isLocal(accesses[0])  : "if-block: pattern s must be local"
        assert isLocal(accesses[1])  : "else-block: redeclared s must be a local"
        // The two locals must be distinct Variable objects.
        assert accesses[0].is(accesses[0]) // sanity
        assert !accesses[0].is(accesses[1]) : "else-block s must not be the pattern variable"
    }

    /**
     * After if-else where both branches fall through, pattern s is not in scope;
     * {@code def s = ...} is a fresh local.
     */
    @Test
    void testRedeclareAfterIf_wherePatternNotVisible_isLocal() {
        def src = '''
            class C {
                def m(Object o) {
                    if (o instanceof String s) {
                        s.length()
                    }
                    def s = 42
                    s
                }
            }
        '''
        def accesses = collectAccesses(src)
        // if-block s (pattern), after-if s (new local)
        assert accesses.size() == 2
        assert isLocal(accesses[0])  : "if-block: pattern s must be local"
        assert isLocal(accesses[1])  : "after if: redeclared s must be a local"
        assert !accesses[0].is(accesses[1]) : "after-if s must not be the pattern variable"
    }

    /**
     * True-branch of {@code !(o instanceof String s)}: whenTrue is empty, so
     * redeclaring s is allowed and yields a fresh local.
     */
    @Test
    void testRedeclareInNegatedIfBlock_wherePatternNotVisible_isLocal() {
        def src = '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s)) {
                        def s = 'x'
                        s
                    } else {
                        s.length()
                    }
                }
            }
        '''
        def accesses = collectAccesses(src)
        // if-block s (new local), else-block s (pattern local)
        assert accesses.size() == 2
        assert isLocal(accesses[0])  : "if-block: redeclared s must be a local"
        assert isLocal(accesses[1])  : "else-block: pattern s must be local"
        assert !accesses[0].is(accesses[1]) : "if-block s must not be the pattern variable"
    }

    // -------------------------------------------------------------------------
    // shouldNotCompile: cannot redeclare where pattern variable IS visible
    // -------------------------------------------------------------------------

    /**
     * Then-block of {@code o instanceof String s}: s is in scope → redeclare fails.
     */
    @Test
    void testShouldNotCompile_redeclareInIfBlock_wherePatternVisible() {
        def err = shouldNotCompile '''
            class C {
                def m(Object o) {
                    if (o instanceof String s) {
                        def s = 'nope'
                    }
                }
            }
        '''
        assert err =~ /already contains a variable of the name s/
    }

    /**
     * After abrupt else of {@code o instanceof String s}: s survives → redeclare fails.
     */
    @Test
    void testShouldNotCompile_redeclareAfterAbruptElse_wherePatternVisible() {
        def err = shouldNotCompile '''
            class C {
                def m(Object o) {
                    if (o instanceof String s) {
                        // fall through
                    } else {
                        return
                    }
                    def s = 'nope'
                }
            }
        '''
        assert err =~ /already contains a variable of the name s/
    }

    /**
     * After {@code if (!(o instanceof String s)) return}: s is introduced → redeclare fails.
     */
    @Test
    void testShouldNotCompile_redeclareAfterEarlyReturn_wherePatternVisible() {
        def err = shouldNotCompile '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s)) return
                    def s = 'nope'
                }
            }
        '''
        assert err =~ /already contains a variable of the name s/
    }

    /**
     * Else-block of {@code !(o instanceof String s)}: whenFalse={s} → redeclare fails.
     */
    @Test
    void testShouldNotCompile_redeclareInElseOfNegated_wherePatternVisible() {
        def err = shouldNotCompile '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s)) {
                        // not string
                    } else {
                        def s = 'nope'
                    }
                }
            }
        '''
        assert err =~ /already contains a variable of the name s/
    }

    private static String shouldNotCompile(String src) {
        try {
            def cu = new CompilationUnit()
            cu.addSource('C.groovy', src)
            cu.compile(Phases.SEMANTIC_ANALYSIS)
            throw new AssertionError("Expected compilation to fail:\n$src")
        } catch (Exception e) {
            return e.message ?: e.toString()
        }
    }
}
