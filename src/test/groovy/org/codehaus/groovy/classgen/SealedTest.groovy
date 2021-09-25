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

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.isAtLeastJdk
import static org.junit.Assume.assumeTrue

@CompileStatic
class SealedTest {
    @Test
    void testInferredPermittedNestedClasses() {
        assumeTrue(isAtLeastJdk('17.0'))
        def config = new CompilerConfiguration(targetBytecode: CompilerConfiguration.JDK17)
        List<Class> classes = (List<Class>) new GroovyShell(config).evaluate('''
            import groovy.transform.Sealed

            @Sealed class Shape {
                final class Triangle extends Shape { }
                final class Polygon extends Shape { }
            }
            Shape.getPermittedSubclasses()
        ''')
        assert classes*.name == ['Shape$Triangle', 'Shape$Polygon']
    }

    @Test
    void testInferredPermittedNestedClassesWithNativeDisabled() {
        assumeTrue(isAtLeastJdk('17.0'))
        def config = new CompilerConfiguration(targetBytecode: CompilerConfiguration.JDK17, sealedNative: false)
        def classes = new GroovyShell(config).evaluate('''
            import groovy.transform.Sealed

            @Sealed class Shape {
                final class Triangle extends Shape { }
                final class Polygon extends Shape { }
            }
            Shape.getPermittedSubclasses()
        ''')
        assert !classes
    }
}
