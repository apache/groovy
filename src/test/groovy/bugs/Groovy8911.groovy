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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * GROOVY-8911: {@code @CompileStatic} must accept a class literal
 * such as {@code Long} where the parameter type is {@code Class}.
 */
final class Groovy8911 {

    @Test
    void testClassLiteralArgumentForClassParameter() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            class Holder {
                boolean hasProperty(String name, Class type) { type == Long }
                boolean isVersioned() { true }
                def test() {
                    if (isVersioned() && hasProperty('version', Long)) {
                        return 'ok'
                    }
                    return 'no'
                }
            }
            assert new Holder().test() == 'ok'
        '''
    }

    @Test
    void testClassLiteralWithJavaInterfaceStringConstant() {
        assertScript '''
            import groovy.transform.CompileStatic

            interface Props {
                String VERSION = 'version'
            }

            @CompileStatic
            class Holder {
                boolean hasProperty(String name, Class type) {
                    name == Props.VERSION && type == Long
                }
                boolean isVersioned() { true }
                def testLiteral() {
                    if (isVersioned() && hasProperty(Props.VERSION, Long)) {
                        return 'literal-ok'
                    }
                    return 'literal-no'
                }
            }
            assert new Holder().testLiteral() == 'literal-ok'
        '''
    }
}
