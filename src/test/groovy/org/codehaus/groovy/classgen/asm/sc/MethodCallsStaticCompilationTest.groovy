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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.MethodCallsSTCTest
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

final class MethodCallsStaticCompilationTest extends MethodCallsSTCTest implements StaticCompilationTestSupport {

    @Test
    void testReferenceToInaccessiblePrivateProperty() {
        shouldFail shell, MultipleCompilationErrorsException, '''
            class Main {
               static main(args) { Peer.xxx }
            }
            class Peer {
                private static int xxx = 666
            }
        '''
    }

    // GROOVY-7863
    @Test
    void testDoublyNestedPrivateMethodAccess() {
        assertScript '''
            class Foo {
                class Bar {
                    class Baz {
                        int c() { d() }
                    }
                    int b() { new Baz().c() }
                }
                int a() {
                    new Bar().b()
                }
                private int d() { 123 }
            }
            assert new Foo().a() == 123
        '''
        assert astTrees['Foo$Bar$Baz'][1].contains('INVOKEVIRTUAL Foo.d ()I')
    }

    // GROOVY-12509
    @Override @Test
    void testCallToPrivateMethodOfSiblingClass() {
        super.testCallToPrivateMethodOfSiblingClass()
        String other = astTrees['Outer$Other'][1]
        assert other.contains('INVOKESTATIC Outer$Box.sm ()Ljava/lang/String;')
        assert other.contains('INVOKEVIRTUAL Outer$Box.im ()Ljava/lang/String;')
        assert !other.contains('ScriptBytecodeAdapter.invoke')
    }

    // GROOVY-12509: dynamic Groovy and @TypeChecked reject a class that mixes
    // private and public methods of one name, so these overloads exist only here
    @Test
    void testPrivateOverloadOfSiblingClassIsPreferred() {
        assertScript '''
            class Outer {
                static class Box {
                    private String m(int i) { 'm(int)' }
                    String m(Object o) { 'm(Object)' }
                    private static String sm(String s) { 'sm(String)' }
                    static String sm(Object o) { 'sm(Object)' }
                }
                static class Other {
                    static String test() { new Box().m(1) + Box.sm('x') }
                    static String viaDelegate() { new Box().with { m(1) } }
                }
            }
            assert Outer.Other.test() == 'm(int)sm(String)'
            assert Outer.Other.viaDelegate() == 'm(int)'
        '''
    }
    // GROOVY-12510
    @Override @Test
    void testCallToPrivateMethodOfSuperclassThroughReceiverOfItsType() {
        assertScript PRIVATE_METHOD_THROUGH_DECLARING_TYPE + '''
            def nested = new Outer.Box.Nested()
            assert nested.viaSuper() == 'im:nested'
            assert nested.viaCast() == 'im:nested'
            assert Outer.param(sub) == 'im:sub'
            assert Outer.local() == 'im:sub'
            assert Outer.result() == 'im:sub'
            assert Outer.coerced() == 'im:sub'
            assert Outer.narrowed(sub) == 'im:sub'
            assert Outer.closure() == 'im:sub'
            assert sub.viaSuper() == 'im:sub'
            assert sub.viaCast() == 'im:sub'
            assert Outer.Other.param(sub) == 'im:sub'
            assert Outer.Other.local() == 'im:sub'
        '''
    }

    // GROOVY-12510: dynamic Groovy and @TypeChecked reject a class that mixes private
    // and public methods of one name. Through the subclass only the public method is
    // a member; through a variable declared with the superclass type both are
    @Test
    void testPrivateOverloadOfSuperclassThroughSubclassOrDeclaredType() {
        assertScript '''
            class Outer {
                static class Box {
                    private String m(String s) { 'm(String)' }
                    String m(Object o) { 'm(Object)' }
                }
                static class Sub extends Box {
                }
                static String viaSubclass() { new Sub().m('x') }
                static String viaDeclaredType() { Box b = new Sub(); b.m('x') }
            }
            assert Outer.viaSubclass() == 'm(Object)'
            assert Outer.viaDeclaredType() == 'm(String)'
        '''
    }
}
