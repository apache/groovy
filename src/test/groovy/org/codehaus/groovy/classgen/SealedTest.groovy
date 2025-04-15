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
package org.codehaus.groovy.classgen

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.isAtLeastJdk
import static org.junit.Assume.assumeTrue

final class SealedTest {

    @Test
    void testInferredPermittedNestedClasses() {
        assumeTrue(isAtLeastJdk('17.0'))

        assertScript '''
            sealed class Shape {
                final class Triangle extends Shape { }
                final class Polygon extends Shape { }
            }
            List<Class> classes = Shape.getPermittedSubclasses()
            assert classes*.name == ['Shape$Triangle', 'Shape$Polygon']
        '''
    }

    @Test
    void testInferredPermittedNestedClassesWithNativeDisabled() {
        assumeTrue(isAtLeastJdk('17.0'))

        assertScript '''
            import groovy.transform.*
            @SealedOptions(mode=SealedMode.EMULATE)
            sealed class Shape {
                final class Triangle extends Shape { }
                final class Polygon extends Shape { }
            }
            List<Class> classes = Shape.getPermittedSubclasses()
            assert !classes
        '''
    }
}
