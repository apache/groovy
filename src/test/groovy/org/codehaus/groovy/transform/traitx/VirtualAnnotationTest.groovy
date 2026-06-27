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

import groovy.test.GroovyAssert
import org.junit.Test

/**
 * Coverage for the {@code @Virtual} marker. Plain public trait statics
 * are declarer-bound (trait-body calls always invoke the trait's own
 * copy regardless of any same-named static on the implementer);
 * annotating the trait static with {@code @Virtual} opts into the
 * per-implementer override path used by Grails-style framework hooks.
 *
 * <p>Two dimensions are covered: validation (misapplied markers are
 * rejected at compile time) and dispatch (with vs without the marker,
 * across dynamic and {@code @CompileStatic} call sites).
 */
final class VirtualAnnotationTest {

    // ============ Default (no annotation) — declarer-bound ============

    @Test
    void plainStatic_isDeclarerBound_dispatch() {
        GroovyAssert.assertScript '''
            trait V {
                static boolean defaultNullable() { false }
                static boolean seen() { this.defaultNullable() }
            }
            class Over implements V { static boolean defaultNullable() { true } }
            assert Over.seen() == false              // trait's own copy
            assert Over.defaultNullable() == true    // direct call: override wins
        '''
    }

    @Test
    void plainStatic_isDeclarerBound_underCompileStatic() {
        GroovyAssert.assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait V {
                static boolean defaultNullable() { false }
                static boolean seen() { this.defaultNullable() }
            }
            @CompileStatic class Over implements V { static boolean defaultNullable() { true } }
            assert Over.seen() == false
        '''
    }

    // ============ @Virtual — virtual dispatch via implementer ============

    @Test
    void virtual_dispatchesThroughImplementer() {
        GroovyAssert.assertScript '''
            import groovy.transform.Virtual
            trait V {
                @Virtual static boolean defaultNullable() { false }
                static boolean seen() { this.defaultNullable() }
            }
            class Over implements V { static boolean defaultNullable() { true } }
            class Def  implements V { }
            assert Over.seen() == true               // override visible to trait body
            assert Def.seen()  == false              // no override -> trait default
        '''
    }

    @Test
    void virtual_unqualifiedCall_dispatchesThroughImplementer() {
        GroovyAssert.assertScript '''
            import groovy.transform.Virtual
            trait V {
                @Virtual static boolean defaultNullable() { false }
                static boolean seenUnqualified() { defaultNullable() }
            }
            class Over implements V { static boolean defaultNullable() { true } }
            assert Over.seenUnqualified() == true
        '''
    }

    @Test
    void virtual_underCompileStatic() {
        GroovyAssert.assertScript '''
            import groovy.transform.CompileStatic
            import groovy.transform.Virtual
            @CompileStatic
            trait V {
                @Virtual static boolean defaultNullable() { false }
                static boolean seen() { this.defaultNullable() }
            }
            @CompileStatic class Over implements V { static boolean defaultNullable() { true } }
            @CompileStatic class Def  implements V { }
            assert Over.seen() == true
            assert Def.seen()  == false
        '''
    }

    @Test
    void virtual_fromInstanceMethod() {
        GroovyAssert.assertScript '''
            import groovy.transform.Virtual
            trait T {
                @Virtual static String which() { 'trait' }
                String greet() { which() }
            }
            class C implements T { static String which() { 'class' } }
            class D implements T {}
            assert new C().greet() == 'class'
            assert new D().greet() == 'trait'
        '''
    }

    // ============ Validation ============

    @Test
    void virtual_onInstanceMethod_isCompileError() {
        def err = GroovyAssert.shouldFail '''
            trait T {
                @groovy.transform.Virtual
                String hello() { 'x' }
            }
        '''
        assert err.message.contains('@Virtual can only be applied to public static trait methods')
        assert err.message.contains('is not static')
    }

    @Test
    void virtual_onPrivateStatic_isCompileError() {
        def err = GroovyAssert.shouldFail '''
            trait T {
                @groovy.transform.Virtual
                private static String hidden() { 'x' }
            }
        '''
        assert err.message.contains('@Virtual can only be applied to public static trait methods')
        assert err.message.contains('is private')
    }

    @Test
    void virtual_onAbstractMethod_isCompileError() {
        def err = GroovyAssert.shouldFail '''
            abstract trait T {
                @groovy.transform.Virtual
                abstract String hello()
            }
        '''
        assert err.message.contains('@Virtual can only be applied to public static trait methods')
    }
}
