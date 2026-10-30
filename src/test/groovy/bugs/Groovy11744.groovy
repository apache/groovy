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
package bugs

import org.codehaus.groovy.control.CompilationFailedException
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * A variable named {@code _} may be declared again, the later declaration
 * replacing the earlier one, so a repeated {@code _} in a multi-assignment or
 * parameter list behaves like any other {@code _}: it holds the last value.
 */
final class Groovy11744 {

    @Test
    void testRepeatedUnderscoreHoldsTheLastValue() {
        assertScript '''
            def (_, y, _) = [3, 4, 5]
            assert _ == 5
            assert y == 4
        '''
        assertScript '''
            def m() {
                def (_, y, _) = [3, 4, 5]
                _
            }
            assert m() == 5
        '''
    }

    @Test
    void testRepeatedUnderscoreParameterHoldsTheLastArgument() {
        assertScript '''
            def c = { _, _ -> _ }
            def l = (_, _) -> _
            assert c(2, 3) == 3
            assert l(2, 3) == 3
        '''
    }

    @Test
    void testRedeclaringUnderscoreReplacesIt() {
        assertScript '''
            def _ = 1
            def (_, _) = [2, 3]
            assert _ == 3
        '''
        assertScript '''
            def _ = 1
            def x = (_, _) -> _
            assert x(2, 3) == 3
            assert _ == 1
        '''
        assertScript '''
            def _ = 1
            def _ = 2
            assert _ == 2
        '''
        assertScript '''
            def _ = 1
            def (_, y) = [2, 3]
            assert _ == 2
        '''
    }

    @Test
    void testConsecutiveMultiAssignments() {
        assertScript '''
            def (_, y, _) = [3, 4, 5]
            def (_, z, _) = [6, 7, 8]
            assert _ == 8
        '''
        assertScript '''
            def (_, y, _) = [3, 4, 5]
            def (_, z) = [6, 7]
            def (_, w, _) = [8, 9, 10]
            assert _ == 10
        '''
        assertScript '''
            def (_, y) = [1, 2]
            def (_, z) = [3, 4]
            assert _ == 3
        '''
    }

    @Test
    void testInnerDeclarationShadowsOuterUnderscore() {
        assertScript '''
            def _ = 1
            def c = { def _ = 5; _ }
            assert c() == 5
            assert _ == 1
            [10].each { _ -> assert _ == 10 }
            try { throw new Exception() } catch (Exception _) { }
            assert _ == 1
        '''
    }

    @Test
    void testClosureCapturesTheUnderscoreDeclaredBeforeIt() {
        assertScript '''
            def (_, y) = [1, 2]
            def c = { _ }
            def (_, z) = [3, 4]
            assert c() == 1
            assert _ == 3
        '''
    }

    @Test
    void testRedeclaredUnderscoreHidesTheBinding() {
        def binding = new Binding(_: 'b')
        assert new GroovyShell(binding).evaluate('''
            def (_, y, _) = [3, 4, 5]
            _
        ''') == 5
        assert binding.getVariable('_') == 'b'
    }

    @Test
    void testCompileStatic() {
        assertScript '''
            @groovy.transform.CompileStatic
            def m() {
                def (_, y, _) = [3, 4, 5]
                _
            }
            assert m() == 5
        '''
        assertScript '''
            @groovy.transform.CompileStatic
            def m() {
                def (int _, String s, String _) = [1, 'a', 'bc']
                _.length()
            }
            assert m() == 2
        '''
        assertScript '''
            @groovy.transform.CompileStatic
            def m() {
                def _ = 1
                def _ = 'two'
                _.length()
            }
            assert m() == 3
        '''
    }

    @Test
    void testOtherDuplicateDeclarationsAreStillErrors() {
        def err = shouldFail CompilationFailedException, '''
            def x = 1
            def x = 2
        '''
        assert err.message.contains('The current scope already contains a variable of the name x')

        err = shouldFail CompilationFailedException, '''
            def m(_, _) { }
        '''
        assert err.message.contains('The current scope already contains a variable of the name _')
    }
}
