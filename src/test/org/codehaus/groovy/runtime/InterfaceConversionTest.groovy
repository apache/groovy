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

class InterfaceConversionTest extends GroovyTestCase {

    void testClosureConversion() {
        def c1 = { Object[] args -> args?.length }
        def c2 = c1 as InterfaceConversionTestFoo
        assert !(c1 instanceof InterfaceConversionTestFoo)
        assert c2 instanceof InterfaceConversionTestFoo
        assert c2.a() == 0
        assert c2.b(null) == null
    }

    void testMapConversion() {
        def m1 = [a: { 1 }, b: { 2 }]
        def m2 = m1 as InterfaceConversionTestFoo

        assert !(m1 instanceof InterfaceConversionTestFoo)
        assert m2 instanceof InterfaceConversionTestFoo
        assert m2.a() == 1
        assert m2.b(null) == 2
    }

    //GROOVY-7104
    void testDefaultInterfaceMethodCallOnProxy() {
        try {
            // checks for Java 8
            Class.forName("java.util.function.Consumer", false, this.class.classLoader)
        } catch (ignore) {
            return
        }
        Comparator c1 = { a, b -> a <=> b }
        assert c1.compare("a", "b") == -1
        def c2 = c1.reversed()
        assert c2.compare("a", "b") == 1
    }
}

interface InterfaceConversionTestFoo {
    def a()

    def b(Integer i)
}