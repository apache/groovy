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
 * A {@code @Virtual} trait {@code static} is overridable per implementing class and is
 * deliberately not promoted onto the trait interface, so a qualified {@code Trait.m(...)}
 * call has no implementing class to dispatch through. Such a call is rejected at compile
 * time with a clear, actionable message instead of the generic "cannot find matching
 * method" error (GROOVY-12112). The supported forms remain: an implementing class
 * ({@code Impl.m(...)}), and {@code Trait.super.m(...)} from within trait code for the
 * trait's own definition.
 */
final class Groovy12112 {

    private static String assertQualifiedVirtualRejected(String script) {
        def err = shouldFail(MultipleCompilationErrorsException) { assertScript(script) }
        assert err.message.contains('@Virtual trait static method')
        assert err.message.contains('.super.')           // points at the explicit-default form
        assert err.message.contains('implementing class') // points at the Impl.m(...) form
        err.message
    }

    @Test
    void testQualifiedVirtualFromExternalCode_isCompileError() {
        assertQualifiedVirtualRejected '''
            import groovy.transform.CompileStatic
            import groovy.transform.Virtual
            @CompileStatic
            trait P { @Virtual static String make(Object o) { 'P' } }
            class Impl implements P { }
            @CompileStatic
            def go() { P.make('x') }   // qualified call to a @Virtual trait static
            go()
        '''
    }

    @Test
    void testQualifiedVirtualFromSameTraitBody_isCompileError() {
        assertQualifiedVirtualRejected '''
            import groovy.transform.CompileStatic
            import groovy.transform.Virtual
            @CompileStatic
            trait P {
                @Virtual static String make(Object o) { 'P' }
                String run(String s) { P.make(s) }   // qualified, from the declaring trait's body
            }
            class Impl implements P { }
            new Impl().run('x')
        '''
    }

    @Test
    void testQualifiedVirtualFromChildTraitBody_isCompileError() {
        def msg = assertQualifiedVirtualRejected '''
            import groovy.transform.CompileStatic
            import groovy.transform.Virtual
            @CompileStatic
            trait P { @Virtual static String make(Object o) { 'P' } }
            @CompileStatic
            trait Q extends P {
                String run(String s) { P.make(s) }   // qualified inherited @Virtual, from sub-trait body
            }
            class Impl implements Q { }
            new Impl().run('x')
        '''
        // The T.super escape names the *enclosing* trait Q (Q.super walks Q's super chain and
        // reaches P's default); P.super.make() from Q's body would itself be illegal, so the
        // message must not suggest it.
        assert msg.contains('Q.super.make(')
        assert !msg.contains('P.super.make(')
    }

    @Test
    void testImplementingClassQualifiedStillWorks() {
        assertScript '''
            import groovy.transform.CompileStatic
            import groovy.transform.Virtual
            @CompileStatic
            trait P { @Virtual static String make(Object o) { 'P' } }
            class Over implements P { static String make(Object o) { 'Over' } }
            class Def  implements P { }
            @CompileStatic
            def go() { [Over.make('x'), Def.make('x')] }
            assert go() == ['Over', 'P']   // Impl.m(...): override wins; default otherwise
        '''
    }

    @Test
    void testTraitSuperStillWorks() {
        assertScript '''
            import groovy.transform.CompileStatic
            import groovy.transform.Virtual
            @CompileStatic
            trait P {
                @Virtual static String make(Object o) { 'P' }
                static String viaSuper(Object o) { P.super.make(o) }   // explicit trait-anchored default
            }
            class Over implements P { static String make(Object o) { 'Over' } }
            assert Over.viaSuper('x') == 'P'
        '''
    }

    @Test
    void testUnqualifiedVirtualStillWorks() {
        assertScript '''
            import groovy.transform.CompileStatic
            import groovy.transform.Virtual
            @CompileStatic
            trait P {
                @Virtual static String make(Object o) { 'P' }
                String run(Object o) { make(o) }   // unqualified: override-visible dispatch
            }
            class Over implements P { static String make(Object o) { 'Over' } }
            class Def  implements P { }
            assert new Over().run('x') == 'Over'
            assert new Def().run('x')  == 'P'
        '''
    }
}
