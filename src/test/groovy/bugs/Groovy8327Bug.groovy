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

import groovy.test.GroovyTestCase

class Groovy8327Bug extends GroovyTestCase {
    void testCallStaticMethodInClosureParamOfThisConstructor() {
        assertScript '''
            class A {
                static String x = '123'
                static String g() { 'abc' }
                A() {
                    this({g() + getX()})
                }
                A(a) { assert 'abc123' == a() }
            }
            
            assert new A()
        '''
    }

    void testCallStaticMethodInThisConstructor() {
        assertScript '''
            class A {
                static String x = '123'
                static String g() { 'abc' }
                A() {
                    this(g() + getX())
                }
                A(a) { assert 'abc123' == a }
            }
            
            assert new A()
        '''
    }

    void testCallStaticMethodInClosureParamOfSuperConstructor() {
        assertScript '''
            class B {
                B(b) { assert 'abc123' == b() }
            }
            class A extends B {
                static String x = '123'
                static String g() { 'abc' }
                A() {
                    super({g() + getX()})
                }
            }
            
            assert new A()
        '''
    }

    void testCallStaticMethodInSuperConstructor() {
        assertScript '''
            class B {
                B(b) { assert 'abc123' == b }
            }
            class A extends B {
                static String x = '123'
                static String g() { 'abc' }
                A() {
                    super(g() + getX())
                }
            }
            
            assert new A()
        '''
    }

    void testCallSuperStaticMethodInClosureParamOfSuperConstructor() {
        assertScript '''
            class B {
                B(b) { assert 'abc123' == b() }
                static String x = '123'
                static String g() { 'abc' }
            }
            class A extends B {
                A() {
                    super({g() + getX()})
                }
            }
            
            assert new A()
        '''
    }

    void testCallSuperStaticMethodInSuperConstructor() {
        assertScript '''
            class B {
                B(b) { assert 'abc123' == b }
                static String x = '123'
                static String g() { 'abc' }
            }
            class A extends B {
                A() {
                    super(g() + getX())
                }
            }
            
            assert new A()
        '''
    }
}
