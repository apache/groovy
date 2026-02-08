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
package org.codehaus.groovy.runtime

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class InterfaceConversionTest {

    @Test
    void testClosureConversion() {
        assertScript '''
            interface I {
                def a()
                def b(Integer i)
            }
            def c = { Object[] args -> args?.length }
            def i = c as I
            assert c !instanceof I
            assert i  instanceof I
            assert i.a() == 0
            assert i.b(null) == null
        '''
    }

    @Test
    void testMapConversion() {
        assertScript '''
            interface I {
                def a()
                def b(Integer i)
            }
            def m = [a: { 1 }, b: { 2 }]
            def i = m as I
            assert m !instanceof I
            assert i  instanceof I
            assert i.a() == 1
            assert i.b(null) == 2
        '''
    }

    @Test // GROOVY-7104
    void testDefaultInterfaceMethodCallOnProxy() {
        assertScript '''
            Comparator<?> c = { a,b -> a <=> b }
            assert c.compare("x","y") < 0
            c = c.reversed() // default method
            assert c.compare("x","y") > 0
        '''
    }

    @Test // GROOVY-10391
    void testDefaultInterfaceMethodCallOnProxy2() {
        assertScript '''
            java.util.function.Predicate<?> p = { q -> false }
            def not = p.negate() // default method
            assert not.test("x")
        '''
    }
}
