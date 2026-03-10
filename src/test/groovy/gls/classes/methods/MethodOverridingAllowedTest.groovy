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
package gls.classes.methods

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

import static groovy.test.GroovyAssert.assertScript

class MethodOverridingAllowedTest {

    static List<Arguments> data() {
        [
            Arguments.of('private', 'private'),
            Arguments.of('private', '@groovy.transform.PackageScope'),
            Arguments.of('private', 'protected'),
            Arguments.of('private', 'public'),
            Arguments.of('@groovy.transform.PackageScope', '@groovy.transform.PackageScope'),
            Arguments.of('@groovy.transform.PackageScope', 'protected'),
            Arguments.of('@groovy.transform.PackageScope', 'public'),
            Arguments.of('protected', 'protected'),
            Arguments.of('protected', 'public'),
            Arguments.of('public', 'public'),
        ]
    }

    @ParameterizedTest(name = '{1} may override {0}')
    @MethodSource('data')
    void 'stronger access may override weaker'(String baseVisibility, String childVisibility) {
        assertScript("""
            class Base {
                $baseVisibility myMethod() { true }
            }
            class Child extends Base {
                $childVisibility myMethod() { true }
            }
            assert new Child() != null
        """)
    }
}
