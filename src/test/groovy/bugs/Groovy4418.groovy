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
import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
final class Groovy4418 {

    @Test
    void testStaticFieldAccess() {
        assertScript '''
            class Base {
                static String field = 'foo'
            }
            class Subclass extends Base {
                static method() {
                    field
                }
            }
            assert new Subclass().method() == 'foo'
        '''
    }

    @Test // GROOVY-6183
    void testStaticAttributeAccess() {
        assertScript '''
            class A {
                static protected int x
                static void reset() { this.@x = 2 }
            }
            assert A.x == 0
            assert A.@x == 0
            A.reset()
            assert A.x == 2
            assert A.@x == 2
        '''
    }

    @Test // GROOVY-8385
    void testParentClassStaticAttributeSetAccess() {
        assertScript '''
            class A {
                static protected p
                static getP(){ -1 }
                static setP(value){ p = 2 }
            }
            class B extends A {
                def m(){ this.@p = 1 }
            }
            def x = new B()
            assert A.@p == null
            x.m()
            assert A.@p == 1
        '''

        assertScript '''
            class A {
                static protected p
                static getP(){ -1 }
                static setP(value){ p = 2 }
            }
            class B extends A {
                def m(){ super.@p = 1 }
            }
            def x = new B()
            assert A.@p == null
            x.m()
            assert A.@p == 1
        '''

        assertScript '''
            class A {
                static protected p
                static getP(){ -1 }
                static setP(value){ p = 2 }
            }
            class AA extends A {}
            class B extends AA {
                def m(){ super.@p = 1 }
            }
            def x = new B()
            assert A.@p == null
            x.m()
            assert A.@p == 1
        '''
    }

    @Test // GROOVY-8385
    void testParentClassNonStaticAttributeSetAccess() {
        assertScript '''
            class A {
                protected p
                def getP() { -1 }
                void setP(value) { p = 2 }
            }
            class B extends A {
                def m() { this.@p = 1 }
            }
            def x = new B()
            assert x.@p == null
            x.m()
            assert x.@p == 1
        '''

        assertScript '''
            class A {
                protected p
                def getP() { -1 }
                void setP(value) { p = 2 }
            }
            class B extends A {
                def m() { super.@p = 1 }
            }
            def x = new B()
            assert x.@p == null
            x.m()
            assert x.@p == 1
        '''

        assertScript '''
            class A {
                protected p
                def getP() { -1 }
                void setP(value) { p = 2 }
            }
            class AA extends A {}
            class B extends AA {
                def m() { super.@p = 1 }
            }
            def x = new B()
            assert x.@p == null
            x.m()
            assert x.@p == 1
        '''
    }

    @Test // GROOVY-8385
    void testParentClassPrivateStaticAttributeSetAccess() {
        shouldFail MissingFieldException, '''
            class A {
                static private p
                static def getP() { -1 }
                static void setP(value) { p = 2 }
            }
            class B extends A {
              def m() { this.@p = 1 }
            }
            assert A.@p == null
            new B().m()
            assert A.@p == 1
            assert B.@p == 1
        '''
    }

    @Test // GROOVY-8385
    void testParentClassPrivateNonStaticAttributeSetAccess() {
        shouldFail MissingFieldException, '''
            class A {
                private p
                def getP() { -1 }
                void setP(value) { p = 2 }
            }
            class B extends A {
              def m() { this.@p = 1 }
            }
            def b = new B()
            assert b.@p == null
            b.m()
            assert b.@p == 1
        '''
    }
}
