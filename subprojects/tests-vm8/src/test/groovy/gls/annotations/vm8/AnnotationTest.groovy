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
package gls.annotations.vm8

/**
 * Tests various properties of annotation definitions.
 */
class AnnotationTest extends GroovyTestCase {

    void testAnnotationWithRepeatableSupportedPrecompiledJava() {
        assertScript '''
            import java.lang.annotation.*
            import vm8.*

            class MyClass {
                // TODO confirm the JDK9 behavior is what we expect
                private static final List<String> expected = [
                    '@vm8.Requires(value=[@vm8.Require(value=val1), @vm8.Require(value=val2)])',     // JDK5-8
                    '@vm8.Requires(value={@vm8.Require(value="val1"), @vm8.Require(value="val2")})', // JDK9
                    '@vm8.Requires({@vm8.Require("val1"), @vm8.Require("val2")})'                    // JDK14
                ]

                // control
                @Requires([@Require("val1"), @Require("val2")])
                String method1() { 'method1' }

                // duplicate candidate for auto collection
                @Require(value = "val1")
                @Require(value = "val2")
                String method2() { 'method2' }

                static void main(String... args) {
                    MyClass myc = new MyClass()
                    assert 'method1' == myc.method1()
                    assert 'method2' == myc.method2()
                    assert expected.contains(checkAnnos(myc, "method1"))
                    assert expected.contains(checkAnnos(myc, "method2"))
                }

                private static String checkAnnos(MyClass myc, String name) {
                    def m = myc.getClass().getMethod(name)
                    List annos = m.getAnnotations()
                    assert annos.size() == 1
                    annos[0].toString()
                }
            }
        '''
    }
}
