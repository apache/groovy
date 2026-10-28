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

import groovy.lang.MissingPropertyException
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-11744: a repeated {@code _} is an unnamed placeholder. It must not
 * replace a real {@code _} that is already in scope, including through a
 * multi-assignment or a closure/lambda parameter. A lone {@code _} stays a
 * normal variable. With no real {@code _} in scope, the placeholders do not
 * introduce one.
 */
final class Groovy11744 {

    @Test
    void testMultiAssignDoesNotOverwritePriorUnderscore() {
        assertScript '''
            def _ = 1
            def (_, _) = [2, 3]
            assert _ == 1
        '''
    }

    @Test
    void testMultiAssignKeepsNamedSlotsAndPriorUnderscore() {
        assertScript '''
            def _ = 1
            def (_, y, _) = [3, 4, 5]
            assert y == 4
            assert _ == 1
        '''
    }

    @Test
    void testMultiAssignInsideClosureDoesNotOverwritePriorUnderscore() {
        assertScript '''
            def _ = 1
            def c = { ->
                def (_, _) = [2, 3]
                _
            }
            assert c() == 1
        '''
    }

    @Test
    void testMultiAssignDoesNotShadowField() {
        assertScript '''
            class C {
                def _ = 1
                def m() {
                    def (_, y, _) = [3, 4, 5]
                    assert y == 4
                    assert _ == 1
                }
            }
            new C().m()
        '''
    }

    @Test
    void testCompileStaticMultiAssignDoesNotOverwritePriorUnderscore() {
        assertScript '''
            @groovy.transform.CompileStatic
            def m() {
                def _ = 1
                def (_, y, _) = [3, 4, 5]
                assert y == 4
                assert _ == 1
            }
            m()
        '''
    }

    @Test
    void testLambdaParameterDoesNotOverwritePriorUnderscore() {
        assertScript '''
            def _ = 1
            def x = (_, _) -> _
            assert x(2, 3) == 1
        '''
    }

    @Test
    void testClosureParameterDoesNotOverwritePriorUnderscore() {
        assertScript '''
            def _ = 1
            def x = { _, _ -> _ }
            assert x(2, 3) == 1
        '''
    }

    @Test
    void testLambdaCanReadPriorUnderscoreAlongsideNamedParams() {
        assertScript '''
            def _ = 9
            def g = (_, a, _, b) -> a + b + _
            assert g(1000, 100, 10, 1) == 110
        '''
    }

    @Test
    void testClosureCanReadPriorUnderscoreAlongsideNamedParams() {
        assertScript '''
            def _ = 9
            def g = { _, a, _, b -> a + b + _ }
            assert g(1000, 100, 10, 1) == 110
        '''
    }

    @Test
    void testCompileStaticLambdaDoesNotOverwritePriorUnderscore() {
        assertScript '''
            import java.util.function.BiFunction

            @groovy.transform.CompileStatic
            def m() {
                int _ = 1
                BiFunction<Integer, Integer, Integer> f = (_, _) -> _
                assert f.apply(2, 3) == 1
            }
            m()
        '''
    }

    @Test
    void testUnnamedMultiAssignDoesNotIntroduceUnderscore() {
        assertScript '''
            def (_, y, _) = [3, 4, 5]
            assert y == 4
        '''
        shouldFail(MissingPropertyException, '''
            def (_, y, _) = [3, 4, 5]
            _
        ''')
    }

    @Test
    void testClosureAssignsPriorUnderscoreAfterUnnamedDeclaration() {
        assertScript '''
            def _ = 1
            def c = { ->
                def (_, _) = [2, 3]
                _ = 8
            }
            c()
            assert _ == 8
        '''
    }

    @Test
    void testSingleUnderscoreStillBinds() {
        assertScript '''
            def (_, y) = [2, 3]
            assert _ == 2
            assert y == 3
        '''
    }

    @Test
    void testAssignmentToRealUnderscoreStillWrites() {
        assertScript '''
            def _ = 1
            def y
            (_, y, _) = [3, 4, 5]
            assert y == 4
            assert _ == 5
        '''
    }

    @Test
    void testPlaceholderSlotsDoNotDisturbNamedVariables() {
        assertScript '''
            def (x, _, y, _, z) = [1, 2, 3, 4, 5]
            assert "$x $y $z" == '1 3 5'
        '''
    }

    @Test
    void testCompileStaticUnderscoreStaysUndeclaredWithoutPriorBinding() {
        shouldFail '''
            @groovy.transform.CompileStatic
            def m() {
                def (x, _, y, _, z) = [1, 2, 3, 4, 5]
                _
            }
            m()
        '''
    }
}
