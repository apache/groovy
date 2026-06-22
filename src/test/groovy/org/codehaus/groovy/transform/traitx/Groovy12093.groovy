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
 * Exercises the {@code @Anchored} marker on trait static methods (GROOVY-12093).
 *
 * <p>The marker selects declarer-bound dispatch (Java/interface-static
 * flavour) in place of the default override-flavour dispatch, and (by
 * default) also promotes the annotated method onto the generated trait
 * interface.
 *
 * <pre>
 *   plain  static m()              -&gt; override flavour (dispatch via
 *                                      implementer; Grails Validateable
 *                                      canary; Groovy 4.0.32 + post-#2529)
 *   {@literal @}Anchored static m()           -&gt; declarer-bound dispatch + on the
 *                                      generated trait interface
 *   {@literal @}Anchored(inInterface=false)
 *     static m()                   -&gt; declarer-bound dispatch only, not
 *                                      published on the interface
 * </pre>
 *
 * Dispatch is implemented in {@code TraitReceiverTransformer}; interface
 * promotion is implemented in {@code TraitASTTransformation}.
 */
final class Groovy12093 {

    // Basic-case coverage lives in TraitStaticDispatchMatrix:
    //   * plain-static override visible to trait body  → matrix row 1
    //   * @Anchored makes dispatch trait-anchored      → matrix row 1a
    //   * @Anchored from-trait T.m()                   → matrix row 7a
    //   * @Anchored external Trait.m()                 → matrix row 8a
    //   * @Anchored(inInterface=false) opt-out         → matrix row 13
    // This class covers the @Anchored-specific tests that the matrix
    // doesn't carry: per-callee keying, @CompileStatic variants, the
    // inInterface=false STC compile-error case, and validation that
    // @Anchored is rejected on non-public-static targets.

    /**
     * The marker is keyed on the <em>callee</em>, not the caller — same
     * discipline as the existing private-static escape.
     */
    @Test
    void anchored_isKeyedOnCallee() {
        assertScript '''
            import groovy.transform.Anchored

            trait V {
                static       boolean overridable() { false }
                @Anchored
                static       boolean anchored()    { false }
                // Same caller, different callees: result depends on each
                // callee's own marker, not the caller's.
                static List<Boolean> bothSeen() { [this.overridable(), this.anchored()] }
            }
            class Over implements V {
                static boolean overridable() { true }
                static boolean anchored()    { true }
            }
            assert Over.bothSeen() == [true, false]
        '''
    }

    /** Plain {@code static} inside {@code @CompileStatic} still routes via the implementer. */
    @Test
    void plainStatic_compileStatic_overrideVisible() {
        assertScript '''
            @groovy.transform.CompileStatic
            trait V {
                static String name() { 'trait' }
                static String seen() { this.name() }
            }
            @groovy.transform.CompileStatic
            class Impl implements V { static String name() { 'impl' } }

            assert Impl.seen() == 'impl'
        '''
    }

    /** {@code @Anchored} inside {@code @CompileStatic} stays trait-anchored. */
    @Test
    void anchored_compileStatic_traitAnchored() {
        assertScript '''
            import groovy.transform.Anchored

            @groovy.transform.CompileStatic
            trait V {
                @Anchored
                static String name() { 'trait' }
                static String seen() { this.name() }
            }
            @groovy.transform.CompileStatic
            class Impl implements V { static String name() { 'impl' } }

            assert Impl.seen() == 'trait'
        '''
    }

    /**
     * STC sanity check: external {@code V.name()} from a {@code @CompileStatic}
     * caller type-checks and runs — the interface static is visible to the
     * static type checker, not just to dynamic dispatch.
     */
    @Test
    void anchored_compileStatic_externalTraitDotM_typeChecks() {
        assertScript '''
            import groovy.transform.Anchored
            import groovy.transform.CompileStatic

            trait V {
                @Anchored
                static String name() { 'trait' }
            }
            class Impl implements V { }

            @CompileStatic
            static String callExternally() {
                V.name()        // must type-check against the interface static
            }

            assert callExternally() == 'trait'
        '''
    }

    /**
     * STC sanity check: from-trait {@code V.m()} from inside a
     * {@code @CompileStatic} trait body also type-checks against the
     * interface static.
     */
    @Test
    void anchored_compileStatic_fromTraitT_DotM_typeChecks() {
        assertScript '''
            import groovy.transform.Anchored

            @groovy.transform.CompileStatic
            trait V {
                @Anchored
                static String name() { 'trait' }
                static String forced() { V.name() }   // trait-qualified, under @CS
            }
            @groovy.transform.CompileStatic
            class Over implements V { static String name() { 'over' } }

            assert Over.forced() == 'trait'
        '''
    }

    /**
     * STC sanity check: with the {@code inInterface=false} opt-out, external
     * {@code V.name()} from a {@code @CompileStatic} caller should fail to
     * compile — no interface static to bind against.
     */
    @Test
    void anchored_inInterfaceFalse_compileStatic_externalCall_isCompileError() {
        shouldFail(MultipleCompilationErrorsException) {
            new GroovyShell().evaluate '''
                import groovy.transform.Anchored
                import groovy.transform.CompileStatic

                trait V {
                    @Anchored(inInterface=false)
                    static String name() { 'trait' }
                }
                class Impl implements V { }

                @CompileStatic
                static String callExternally() {
                    V.name()       // not on the interface — should be a compile error under @CS
                }
                callExternally()
            '''
        }
    }

    /**
     * The generated interface forwarder for an {@code @Anchored} static should
     * carry the original method's runtime-retention annotations (e.g.
     * {@code @Deprecated}), matching the forwarders {@code TraitComposer} creates
     * for ordinary trait methods.
     */
    @Test
    void anchored_propagatesRuntimeAnnotationsToInterfaceForwarder() {
        assertScript '''
            import groovy.transform.Anchored
            import java.lang.reflect.Modifier

            trait T {
                @Deprecated
                @Anchored
                static String m() { 'T' }
            }

            def m = T.getDeclaredMethod('m')
            assert Modifier.isStatic(m.modifiers)
            assert m.isAnnotationPresent(Deprecated)
        '''
    }

    // ---- Validation: @Anchored must target a public static (non-abstract) trait method ----

    /** {@code @Anchored} on an instance method must be a compile error. */
    @Test
    void anchored_onInstanceMethod_isCompileError() {
        def err = shouldFail '''
            import groovy.transform.Anchored
            trait V {
                @Anchored
                def m() { 'V' }            // instance, not static
            }
            new Object()
        '''
        assert err.message.contains('@Anchored can only be applied to public static trait methods')
        assert err.message.contains('is not static')
    }

    /** {@code @Anchored} on a private static method must be a compile error. */
    @Test
    void anchored_onPrivateStatic_isCompileError() {
        def err = shouldFail '''
            import groovy.transform.Anchored
            trait V {
                @Anchored
                private static m() { 'V' }
            }
            new Object()
        '''
        assert err.message.contains('@Anchored can only be applied to public static trait methods')
        assert err.message.contains('is private')
    }

    /** {@code @Anchored} on an abstract trait method (which is implicitly
     * non-static — Groovy rejects {@code abstract static}) surfaces the same
     * "not static" diagnostic. */
    @Test
    void anchored_onAbstractInstanceMethod_isCompileError() {
        def err = shouldFail '''
            import groovy.transform.Anchored
            trait V {
                @Anchored
                abstract m()
            }
            new Object()
        '''
        assert err.message.contains('@Anchored can only be applied to public static trait methods')
        assert err.message.contains('is not static')
    }
}
