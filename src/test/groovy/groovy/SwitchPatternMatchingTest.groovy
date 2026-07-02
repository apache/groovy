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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for GEP-19 structural pattern matching in switch: type patterns
 * with binding and {@code when} guards (arrow-form labels).
 */
final class SwitchPatternMatchingTest {

    @Test
    void testTypePatternDispatch() {
        def describe = { obj ->
            switch (obj) {
                case String s  -> s.toUpperCase()
                case Integer i -> i * 2
                case Number n  -> n.doubleValue()
                default        -> 'other'
            }
        }
        assert describe('abc') == 'ABC'
        assert describe(21) == 42
        assert describe(1.5G) == 1.5d
        assert describe(null) == 'other'
    }

    @Test
    void testWhenGuardFallsThroughToNextCase() {
        def sign = { obj ->
            switch (obj) {
                case Integer i when i > 0 -> 'positive'
                case Integer i when i < 0 -> 'negative'
                case Integer i            -> 'zero'
                default                   -> 'not an int'
            }
        }
        assert sign(7) == 'positive'
        assert sign(-7) == 'negative'
        assert sign(0) == 'zero'
        assert sign('x') == 'not an int'
    }

    @Test
    void testGuardSeesPatternVariableAndOuterLocals() {
        int min = 3
        def result = switch ('abcd') {
            case String s when s.length() > min -> "long ${s.length()}"
            case String s                       -> 'short'
            default                             -> 'other'
        }
        assert result == 'long 4'
    }

    @Test
    void testPatternsCoexistWithLegacyLabels() {
        def classify = { obj ->
            switch (obj) {
                case null                 -> 'nil'
                case 'a'..'c'             -> 'a to c'
                case ~/[a-z]+/            -> 'letters'
                case Integer i when i > 9 -> 'big int'
                case Integer i            -> "int $i"
                case { it == [] }         -> 'empty list closure'
                default                   -> 'other'
            }
        }
        assert classify('abc') == 'letters'
        assert classify('b') == 'a to c'
        assert classify(10) == 'big int'
        assert classify(5) == 'int 5'
        assert classify([]) == 'empty list closure'
        assert classify(null) == 'nil'
        assert classify(3.5) == 'other'
    }

    @Test
    void testArrowSwitchStatementWithPatterns() {
        def out = []
        switch (42 as Object) {
            case String s  -> out << "string $s"
            case Integer i -> out << "int ${i + 1}"
            default        -> out << 'other'
        }
        assert out == ['int 43']
    }

    @Test
    void testPatternVariableNotVisibleAfterSwitch() {
        shouldFail MissingPropertyException, '''
            switch (42 as Object) {
                case Integer i -> i
                default        -> 0
            }
            i
        '''
    }

    @Test
    void testSubjectEvaluatedOnce() {
        assertScript '''
            int count = 0
            def next = { count++; 42 }
            def result = switch (next()) {
                case Integer i when i == 42 -> 'match'
                default                     -> 'no'
            }
            assert result == 'match'
            assert count == 1
        '''
    }

    @Test
    void testTypePatternCompileStatic() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            def describe(Object obj) {
                switch (obj) {
                    case Integer i when i > 0 -> 'positive ' + (i * 2)
                    case String s             -> s.toUpperCase() // proves narrowing: only String has toUpperCase()
                    case Number n             -> 'number ' + n.doubleValue()
                    default                   -> 'other'
                }
            }
            assert describe(21) == 'positive 42'
            assert describe('abc') == 'ABC'
            assert describe(-1.5G) == 'number -1.5'
            assert describe(new Object()) == 'other'
            assert describe(null) == 'other'
        '''
    }

    @Test
    void testGenericTypePattern() {
        def result = switch (['a', 'bb'] as Object) {
            case List<String> strings -> strings*.size().sum()
            default                   -> -1
        }
        assert result == 3
    }

    @Test
    void testPatternRequiresArrowForm() {
        def err = shouldFail '''
            def result = switch (42) {
                case Integer i: yield i
                default: yield 0
            }
        '''
        assert err.message.contains('arrow form')
    }

    @Test
    void testStatementFormSwitchDoesNotSupportPatterns() {
        shouldFail '''
            switch (42) {
                case Integer i:
                    println i
                    break
            }
        '''
    }
}
