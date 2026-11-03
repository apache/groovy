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
package gls.invocation

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class ClosureDelegationTest {

    @Test
    void testMissingMethodMissingMethod() {
        assertScript '''
            class A {
                def methodMissing(String name, args) {
                    "A"
                }
            }

            def methodMissing(String name, args) {
                visited=true
                throw new MissingMethodException(name,this.class,args)
            }

            visited=false
            def closure = { foo() }
            closure.delegate = new A()
            assert closure() == "A"
            assert visited==true
        '''
    }

    @Test
    void testInvokeMethodMissingMethod() {
        assertScript '''
            class A {
                def invokeMethod(String name, args) {
                    "A"
                }
            }

            def methodMissing(String name, args) {
                visited=true
                throw new MissingMethodException(name,this.class,args)
            }

            visited=false
            def closure = { foo() }
            closure.delegate = new A()
            assert closure() == "A"
            assert visited==true
        '''
    }

    @Test
    void testMissingMethodInvokeMethod() {
        assertScript '''
            class A {
                def methodMissing(String name, args) {
                    "A"
                }
            }

            def invokeMethod(String name, args) {
                visited=true
                throw new MissingMethodException(name,this.class,args)
            }

            visited=false
            def closure = { foo() }
            closure.delegate = new A()
            assert closure() == "A"
            assert visited==true
        '''
    }

    @Test
    void testInvokeMethodInvokeMethod() {
        assertScript '''
            class A {
                def invokeMethod(String name, args) {
                    "A"
                }
            }

            def invokeMethod(String name, args) {
                visited=true
                throw new MissingMethodException(name,this.class,args)
            }

            visited=false
            def closure = { foo() }
            closure.delegate = new A()
            assert closure() == "A"
            assert visited==true
        '''
    }

    @Test
    void testStaticMethod() {
        assertScript '''
            class Foo {
                static visited
                static closureInStaticContext = {
                    foo()
                }
                static foo(){ visited = true }
            }

            Foo.closureInStaticContext()
            assert Foo.visited == true
        '''
    }

    @Test
    void testStaticContextClosureDelegation() {
        assertScript '''
            class Foo {
                static aClosure = { foo() }
                static { aClosure.delegate = new Bar() }
            }
            class Bar {
                static visited
                def foo() { visited = true }
            }

            Foo.aClosure()
            assert Bar.visited == true
        '''
    }

    // GROOVY-11858
    @Test
    void testDelegateMethodVsClosureExtension() {
        assertScript '''
            def array = 'a b c'.with { split() }

            assert array.length == 3
            assert array.contains('a')
            assert array.contains('b')
            assert array.contains('c')
        '''
    }

    // GROOVY-12664
    @Test
    void testClosureExtensionCalledOnceWhenDelegateAndOwnerUnavailable() {
        assertScript '''
            int count = 0
            def c = { -> with { ++count } }
            c.resolveStrategy = Closure.DELEGATE_ONLY
            c.delegate = null
            assert c() == 1
            assert count == 1

            count = 0
            c = { -> with { ++count } }
            c.resolveStrategy = Closure.DELEGATE_ONLY
            c.delegate = c
            assert c() == 1
            assert count == 1

            count = 0
            c = { -> with { ++count } }.dehydrate()
            c.resolveStrategy = Closure.DELEGATE_ONLY
            assert c() == 1
            assert count == 1

            count = 0
            c = { -> with { ++count } }.rehydrate(null, null, null)
            c.resolveStrategy = Closure.OWNER_ONLY
            assert c() == 1
            assert count == 1
        '''
    }

    // GROOVY-12664
    @Test
    void testClosureExtensionCalledOnceWhenDelegateAndOwnerUnavailableTypeChecked() {
        assertScript '''
            @groovy.transform.TypeChecked
            def test() {
                int count = 0
                Closure c = { -> with { ++count } }
                c.resolveStrategy = Closure.DELEGATE_ONLY
                c.delegate = null
                assert c() == 1
                assert count == 1
            }
            test()
        '''
    }

    // GROOVY-12664
    @Test
    void testClosureExtensionResultReturnedByMetaClass() {
        assertScript '''
            def c = { -> }
            c.resolveStrategy = Closure.DELEGATE_ONLY
            c.delegate = null
            def result = c.metaClass.invokeMethod(this.getClass(), c, 'sprintf', ['%d', 42] as Object[], false, true)
            assert result == '42'
        '''
    }
}
