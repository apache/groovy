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

import org.codehaus.groovy.control.CompilationFailedException
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

import static groovy.test.GroovyAssert.shouldFail

class MethodOverridingDeniedTest {

    static List<Arguments> data() {
        [
                Arguments.of('public', 'private'),
                Arguments.of('public', '@groovy.transform.PackageScope'),
                Arguments.of('public', 'protected'),
                Arguments.of('protected', 'private'),
                Arguments.of('protected', '@groovy.transform.PackageScope'),
                Arguments.of('@groovy.transform.PackageScope', 'private'),
        ]
    }

    @ParameterizedTest(name = '{1} should not override {0}')
    @MethodSource('data')
    void 'weaker access must not override stronger'(String baseVisibility, String childVisibility) {
        def ex = shouldFail(CompilationFailedException,"""
            abstract class Base {
                $baseVisibility abstract myMethod()
            }
            class Child extends Base {
                $childVisibility myMethod() { true }
            }
            assert new Child() != null
        """)
        assert ex.message.contains('cannot override myMethod in Base')
        assert ex.message.contains('attempting to assign weaker access privileges')
        assert ex.message.contains("was ${baseVisibility.contains('PackageScope') ? 'package-private' : baseVisibility}")
    }
}
