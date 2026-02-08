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

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import static groovy.test.GroovyAssert.assertScript

@RunWith(Parameterized)
class MethodOverridingAllowedTest {
    String baseVisibility, childVisibility

    MethodOverridingAllowedTest(String baseVisibility, String childVisibility) {
        this.baseVisibility = baseVisibility
        this.childVisibility = childVisibility
    }

    @Parameterized.Parameters(name = '{1} may override {0}')
    static data() {
        [
            ['private', 'private'],
            ['private', '@groovy.transform.PackageScope'],
            ['private', 'protected'],
            ['private', 'public'],
            ['@groovy.transform.PackageScope', '@groovy.transform.PackageScope'],
            ['@groovy.transform.PackageScope', 'protected'],
            ['@groovy.transform.PackageScope', 'public'],
            ['protected', 'protected'],
            ['protected', 'public'],
            ['public', 'public'],
        ]*.toArray()
    }

    @Test
    void 'stronger access may override weaker'() {
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
