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
package groovy.bugs

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

@CompileStatic
final class Groovy7973 {

    private static final String SCRIPT1 = '''
        class Test {
            def op1() { 'A'.with{ this.class.name } }
            def op2a() { new Object() { def inner() {
                Test.this.class.name + '::' + this.class.name
            } } }
            def op2b() { new Object() { def inner() {
                'B'.with { Test.this.class.name + '::' + this.class.name }
            } } }
            def op3a() { new Object() { def inner() { new Object() { def innerinner() {
                Test.this.class.name + '::' + this.class.name
            } } } } }
            def op3b() { new Object() { def inner() { new Object() { def innerinner() {
                'C'.with { Test.this.class.name + '::' + this.class.name }
            } } } } }
        }

        def t = new Test()
        assert t.op1() == 'Test'
        assert t.op2a().inner() == 'Test::Test$1'
        assert t.op2b().inner() == 'Test::Test$2'
        assert t.op3a().inner().innerinner() == 'Test::Test$3$1'
        assert t.op3b().inner().innerinner() == 'Test::Test$4$1'
    '''

    private static final String SCRIPT2 = '''
        class Test {
            def op1() { this }
            def op2() { ''.with { this } }
            def op3() { new Object() { def inner() { this } } }
            def op4() { new Object() { def inner() { ''.with { this } } } }
            def op5() { new Object() { def inner() { Test.this } } }
            def op6() { new Object() { def inner() { ''.with { Test.this } } } }
            class Inner {
                def inner1() { this }
                def inner2() { ''.with { this } }
                def inner3() { Test.this }
                def inner4() { ''.with { Test.this } }
            }
        }

        def t = new Test()
        assert t.op1().class.name == 'Test'
        assert t.op2().class.name == 'Test'
        assert t.op3().inner().class.name == 'Test$1'
        assert t.op4().inner().class.name == 'Test$2'
        assert t.op5().inner().class.name == 'Test'
        assert t.op6().inner().class.name == 'Test'

        def inner = new Test.Inner(t)
        assert inner.inner1().class.name == 'Test$Inner'
        assert inner.inner2().class.name == 'Test$Inner'
        assert inner.inner3().class.name == 'Test'
        assert inner.inner4().class.name == 'Test'
    '''

    @Test
    void testClassDotThis() {
        assertScript SCRIPT1
    }

    @Test
    void testClassDotThis_CS() {
        assertScript '@groovy.transform.CompileStatic\n' + SCRIPT1
    }

    @Test
    void testClassDotThisAIC() {
        assertScript SCRIPT2
    }

    @Test
    void testClassDotThisAIC_CS() {
        assertScript '@groovy.transform.CompileStatic\n' + SCRIPT2
    }
}
