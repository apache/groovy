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

class MethodClosureTest extends GroovyTestCase {

    def aa(x) {
        x
    }
    
    static bb(it) { it}

    void testMethodClosure() {
        Class[] c1 = [ Exception.class, Throwable.class ]
        Class[] c2 = [ IllegalStateException.class ]

        def cl = this.&aa

        assert cl instanceof Closure
        assert cl instanceof MethodClosure

        assert [c1, c2].collect(cl) == [c1,c2]
    }
    
    void testStaticMethodAccess() {
       def list = [1].collect (this.&bb)
       assert list == [1]
       list = [1].collect (MethodClosureTest.&bb)
       assert list == [1]
       def mct = new MethodClosureTest()
       list = [1].collect (mct.&bb)
       assert list == [1]
    }

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
            } catch(MissingMethodException ignore) {}
            try {
                foo.methodClosure()
                assert false
            } catch(MissingMethodException ignore) {}
            use(BarCategory) {
                assert bar.methodClosure() == 'result'
                assert foo.methodClosure() == 'result'
            }
        '''
    }
}
