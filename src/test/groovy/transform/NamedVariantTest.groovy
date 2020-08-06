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
package groovy.transform

import java.lang.reflect.Modifier
import groovy.test.GroovyTestCase

/**
 * Unit tests for the NamedVariant annotation
 */
class NamedVariantTest extends GroovyTestCase {
    void testMethod() {
        def tester = new GroovyClassLoader().parseClass(
                '''class MyClass {
                  |    @groovy.transform.NamedVariant
                  |    void run(int number) {
                  |    }
                  |}'''.stripMargin())
        // Should have such method `void run(Map)`
        def method = tester.getDeclaredMethod("run", Map)
        assert method
        assert Modifier.isPublic(method.modifiers)
        assert method.returnType == void.class
    }

    void testMethodCall() {
        def tester = new GroovyClassLoader().parseClass(
                '''class MyClass {
                  |    @groovy.transform.NamedVariant
                  |    int run(int number) {
                  |        number
                  |    }
                  |}'''.stripMargin()).getConstructor().newInstance()

        assert tester.run(number: 123) == 123
        try {
            tester.run(number: "123")
        } catch (MissingMethodException ignored) {
            return
        }
        fail("Should have thrown MissingMethodException")
    }

    void testCoerceMethodCall() {
        def tester = new GroovyClassLoader().parseClass(
                '''class MyClass {
                  |    @groovy.transform.NamedVariant(coerce = true)
                  |    int run(int number) {
                  |        number
                  |    }
                  |}'''.stripMargin()).getConstructor().newInstance()

        assert tester.run(number: 123) == 123
        assert tester.run(number: "123") == 123
    }

    void testStaticCoerceMethodCall() {
        def tester = new GroovyClassLoader().parseClass(
                '''@groovy.transform.CompileStatic
                  |class MyClass {
                  |    @groovy.transform.NamedVariant(coerce = true)
                  |    int run(int number) {
                  |        number
                  |    }
                  |}'''.stripMargin()).getConstructor().newInstance()

        assert tester.run(number: 123) == 123
        assert tester.run(number: "123") == 123
    }
}
