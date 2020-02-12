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

class ClosureGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> classUnderTest = parseClass('''class ClassUnderTest {
           Closure<String> c(String arg) {
               def closureVar = {
                   arg
               }
               closureVar
           }
       }''')

    @Test
    void test_captured_argument_is_annotated() {
        def objectUnderTest = classUnderTest.newInstance()
        Closure<String> cls = classUnderTest.getMethod('c', String)
            .invoke(objectUnderTest, 'var')
        assertMethodIsAnnotated(cls.class, 'getArg')
    }
}