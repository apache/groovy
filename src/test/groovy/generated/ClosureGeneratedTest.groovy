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
package groovy.generated

import org.junit.Test

final class ClosureGeneratedTest extends AbstractGeneratedAstTestCase {

    @Test
    void 'captured argument accessor is annotated'() {
        def classUnderTest = parseClass '''
            class C {
                Closure<String> m(String string) {
                    return { -> string }
                }
            }
        '''
        def objectUnderTest = classUnderTest.newInstance()
        Closure<String> cls = classUnderTest.getMethod('m', String).invoke(objectUnderTest, 'value')

        assertMethodIsAnnotated/*AsGenerated*/(cls.class, 'getString')
    }

    // GROOVY-11313
    @Test
    void 'no accessor for captured argument with reserved name'() {
        def classUnderTest = parseClass '''
            class C {
                Closure<String> m(String owner) {
                    return { -> owner }
                }
            }
        '''
        def objectUnderTest = classUnderTest.newInstance()
        Closure<String> cls = classUnderTest.getMethod('m', String).invoke(objectUnderTest, 'value')

        def getter = cls.getClass().getMethod('getOwner')
        assert getter.declaringClass == Closure.class
    }
}
