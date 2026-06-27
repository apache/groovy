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
package org.codehaus.groovy.transform.traitx

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * {@code T.this.*} qualifier syntax inside trait code is rejected at
 * compile time (GROOVY-12104). Without the reject, the call previously
 * compiled successfully and generated invalid or mis-typed bytecode that
 * failed at runtime: VerifyError on Groovy 4.x ("Class not assignable to
 * Closure"), ClassCastException at runtime on Groovy 5.x/6.x.
 *
 * <p>{@code T.this} has no coherent meaning for traits — per GEP-22
 * § this, super, and stackable traits item 1, {@code this} is the
 * implementing instance and the trait is not an enclosing scope of its
 * implementer. The supported alternatives are existing documented
 * features: {@code this.m(...)} for normal dispatch (override-visible
 * via row-1 dispatch), {@code T.super.m(...)} for explicit
 * trait-anchored dispatch.
 */
final class Groovy12104 {

    /** T.this.m() in a static trait method → compile error. */
    @Test
    void TthisMethodCall_inStaticTraitMethod_isCompileError() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            assertScript '''
                trait V {
                    static boolean defaultNullable() { false }
                    static seen() { V.this.defaultNullable() }   // <-- rejected
                }
                class Over implements V { static boolean defaultNullable() { true } }
                Over.seen()
            '''
        }
        assert err.message.contains("'V.this' is not allowed inside trait code")
        assert err.message.contains('this.defaultNullable(')
        assert err.message.contains('V.super.defaultNullable(')
    }

    /** T.this.m() in an instance trait method → compile error too. */
    @Test
    void TthisMethodCall_inInstanceTraitMethod_isCompileError() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            assertScript '''
                trait V {
                    boolean defaultNullable() { false }
                    def seen() { V.this.defaultNullable() }   // <-- rejected
                }
                class Over implements V { boolean defaultNullable() { true } }
                new Over().seen()
            '''
        }
        assert err.message.contains("'V.this' is not allowed inside trait code")
    }

    /** T.this.field in a trait static method → compile error. */
    @Test
    void TthisFieldAccess_inStaticTraitMethod_isCompileError() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            assertScript '''
                trait V {
                    static String origin = 'trait'
                    static seen() { V.this.origin }   // <-- rejected
                }
                class Over implements V { }
                Over.seen()
            '''
        }
        assert err.message.contains("'V.this' is not allowed inside trait code")
        assert err.message.contains('this.origin')
    }

    /** T.this.field in an instance trait method → compile error too. */
    @Test
    void TthisFieldAccess_inInstanceTraitMethod_isCompileError() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            assertScript '''
                trait V {
                    String origin = 'trait'
                    def seen() { V.this.origin }   // <-- rejected
                }
                class Over implements V { }
                new Over().seen()
            '''
        }
        assert err.message.contains("'V.this' is not allowed inside trait code")
    }

    /** Recommended alternative #1: this.m() — works for normal dispatch (declarer-bound by default; @Virtual opts into per-implementer override). */
    @Test
    void thisMethodCall_recommendedAlternative_works() {
        assertScript '''
            import groovy.transform.Virtual
            trait V {
                @Virtual static boolean defaultNullable() { false }
                static seen() { this.defaultNullable() }   // normal dispatch
            }
            class Over implements V { static boolean defaultNullable() { true } }
            assert Over.seen() == true   // @Virtual: override visible from trait body
        '''
    }

    /** Recommended alternative #2: T.super.m() — works for explicit trait-anchored dispatch. */
    @Test
    void TsuperMethodCall_recommendedAlternative_works() {
        assertScript '''
            trait V {
                static boolean defaultNullable() { false }
                static seen() { V.super.defaultNullable() }   // explicit trait-anchored
            }
            class Over implements V { static boolean defaultNullable() { true } }
            assert Over.seen() == false   // trait's own copy
        '''
    }

    /**
     * Plain (non-trait) class T.this in a static method → already a compile
     * error pre-12104 (Java-conventional behaviour). Regression guard that
     * the trait-only reject hasn't disturbed this.
     */
    @Test
    void Tthis_inPlainClassStatic_stillCompileError() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            assertScript '''
                class Outer {
                    static foo() { 'foo' }
                    static seen() { Outer.this.foo() }
                }
                Outer.seen()
            '''
        }
        // Note: the existing compile-error message for plain classes
        // differs from the trait-specific one — that's fine; both reject.
        assert err != null
    }

    /**
     * Trait static with explicit other-class qualifier (e.g. Locale.getDefault())
     * must NOT be rejected — the heuristic targets {@code T.this} specifically,
     * not any qualifier-style call.
     */
    @Test
    void explicitOtherClassQualifier_inTraitStatic_isAllowed() {
        assertScript '''
            trait V {
                static getDefaultLocale() { Locale.getDefault() }
            }
            class Over implements V { }
            assert Over.getDefaultLocale() != null
        '''
    }
}
