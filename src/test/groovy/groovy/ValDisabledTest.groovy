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

import groovy.junit6.plugin.ForkedJvm
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Tests that when {@code groovy.val.enabled=false}, GEP-16 breaking
 * changes are resolved and {@code val} behaves as a regular identifier.
 * <p>
 * Each test runs in a freshly forked JVM with the property set, so the
 * lexer's {@code static final VAL_ENABLED} is initialised to {@code false}.
 */
@ForkedJvm(systemProperties = ['groovy.val.enabled=false'])
final class ValDisabledTest {

    @Test
    void testFieldNamedValBeforeMethod() {
        assertScript '''
            class Foo {
                def val
                void doSomething() {}
            }
            def f = new Foo()
            f.val = 42
            assert f.val == 42
        '''
    }

    @Test
    void testValAsCastExpression() {
        assertScript '''
            def val = 42
            def result = val as String
            assert result == '42'
        '''
    }

    @Test
    void testClassNamedVal() {
        assertScript '''
            class val {
                int x
            }
            def v = new val(x: 5)
            assert v.x == 5
        '''
    }

    @Test
    void testValAsMethodReturnType() {
        assertScript '''
            class val {
                int x
            }
            import val as Val
            class Foo {
                Val bar() { new val(x: 99) }
            }
            assert new Foo().bar().x == 99
        '''
    }

    @Test
    void testValAsExplicitType() {
        assertScript '''
            class val {
                int x
            }
            val v = new val(x: 7)
            assert v.x == 7
        '''
    }

    @Test
    void testDefValAssignment() {
        assertScript '''
            def val = 1
            assert val == 1
        '''
    }

    @Test
    void testValReassignment() {
        assertScript '''
            def val = 1
            val = 2
            assert val == 2
        '''
    }

    @Test
    void testValAsMapKey() {
        assertScript '''
            def m = [val: 42]
            assert m.val == 42
        '''
    }

    @Test
    void testValPropertyAccess() {
        assertScript '''
            class Foo { def val = 'hello' }
            def f = new Foo()
            assert f.val == 'hello'
        '''
    }
}
