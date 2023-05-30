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

import org.codehaus.groovy.runtime.MethodClosure
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class MethodClosureTest {

    def aa(x) {
        x
    }

    static bb(x) {
        x
    }

    @Test
    void testMethodClosure() {
        def closure = this.&aa // instance.instanceMethod
        assert closure instanceof Closure
        assert closure instanceof MethodClosure

        Class[] c1 = [ Exception, Throwable ]
        Class[] c2 = [ IllegalStateException ]
        assert [c1, c2].collect(closure) == [c1,c2]
    }

    @Test
    void testMethodClosure2() {
        def closure = String.&toUpperCase // Class.instanceMethod
        assert closure instanceof Closure
        assert closure instanceof MethodClosure

        assert ["xx", "yy"].collect(closure) == ["XX","YY"]
    }

    @Test
    void testStaticMethodClosure() {
       def list = [1].collect(this.&bb)
       assert list == [1]
       list = [1].collect(MethodClosureTest.&bb) // Class.staticMethod
       assert list == [1]
       list = [1].collect(new MethodClosureTest().&bb) // instance.staticMethod
       assert list == [1]
    }

    @Test
    void testShellVariableAccess() {
        def shell = new GroovyShell()
        assert shell.evaluate("x = String.&toUpperCase; x('abc')") == "ABC"
        assert shell.evaluate("x = 'abc'.&toUpperCase; x()") == "ABC"
        assert shell.evaluate("x = Integer.&parseInt; x('123')") == 123
        assert shell.evaluate("x = 3.&parseInt; x('123')") == 123
    }

    @Test
    void testMethodClosureForPrintln() {
        assertScript '''
            def closure = System.out.&println
            closure('hello world')
        '''
    }

    // GROOVY-9140
    @Test
    void testMethodClosureWithoutThis() {
        String base = '''
            class C {
                def m() { 11 }
            }
            def closure = C.&m
        '''

        assertScript base + '''
            Object result = closure(new C())
            assert result == 11
        '''

        shouldFail MissingMethodException, base + '''
            closure()
        '''

        shouldFail MissingMethodException, base + '''
            closure("")
        '''
    }

    @Test
    void testMethodClosureWithCategory() {
        assertScript '''
            class Bar {
                protected methodClosure
                def storeMethodClosure() {
                    methodClosure = this.&method
                }
            }
            class Foo extends Bar {
                def storeMethodClosure() {
                    methodClosure = super.&method
                }
            }
            class BarCategory {
                static method(Bar self) { 'result' }
            }

            def bar = new Bar()
            def foo = new Foo()
            bar.storeMethodClosure()
            foo.storeMethodClosure()
            try {
                bar.methodClosure()
                assert false
            } catch(MissingMethodException ignore) {
            }
            try {
                foo.methodClosure()
                assert false
            } catch(MissingMethodException ignore) {
            }
            use(BarCategory) {
                assert bar.methodClosure() == 'result'
                assert foo.methodClosure() == 'result'
            }
        '''
    }

    // GROOVY-11075
    @Test
    void testMethodClosureCheckedException() {
        shouldFail IOException, '''
            class Foo {
                static void bar(String str) {
                    throw new IOException()
                }
            }
            def baz = Foo.&bar
            baz("")
        '''
    }

    // GROOVY-10929
    @Test
    void testMethodClosureIllegalArgumentException() {
        shouldFail IllegalArgumentException, '''
            static create(Class type) {
                throw new IllegalArgumentException("Class ${type.name} does not ...")
            }
            def closure = this.class.&create
            closure(Object)
        '''
    }
}
