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

class Groovy7973Bug extends GroovyTestCase {

    private static final String SCRIPT1 = '''
        class Test {
            def op1() { 'A'.with{ this.class.name } }
            def op2a() { new Object() { def inner() {
                this.class.name + Test.this.class.name
            } } }
            def op2b() { new Object() { def inner() {
                'B'.with{ this.class.name + Test.this.class.name }
            } } }
            def op3a() { new Object() { def inner() { new Object() { def innerinner() {
                this.class.name + Test$3.this.class.name + Test.this.class.name
            } } } } }
            def op3b() { new Object() { def inner() { new Object() { def innerinner() {
                'C'.with{ this.class.name + Test$5.this.class.name + Test.this.class.name }
            } } } } }
        }

        def t = new Test()
        assert t.op1() == 'Test'
        assert t.op2a().inner() == 'Test$1Test'
        assert t.op2b().inner() == 'Test$2Test'
        assert t.op3a().inner().innerinner() == 'Test$3$4Test$3Test'
        assert t.op3b().inner().innerinner() == 'Test$5$6Test$5Test'
    '''

    private static final String SCRIPT2 = '''
        class Test {
            def op1() { this }
            def op2() { ''.with{ this } }
            def op3() { new Object() { def inner() { this } } }
            def op4() { new Object() { def inner() { ''.with{ this } } } }
            def op5() { new Object() { def inner() { Test.this } } }
            def op6() { new Object() { def inner() { ''.with{ Test.this } } } }
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

    void testClassDotThis() {
        assertScript SCRIPT1
    }

    void testClassDotThis_CS() {
        assertScript '@groovy.transform.CompileStatic\n' + SCRIPT1
    }

    void testClassDotThisAIC() {
        assertScript SCRIPT2
    }

    void testClassDotThisAIC_CS() {
        assertScript '@groovy.transform.CompileStatic\n' + SCRIPT2
    }
}
