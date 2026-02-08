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

import groovy.transform.CompileStatic
import org.junit.Test

@CompileStatic
class AutoCloneGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> implicitAutoClone = parseClass('''@groovy.transform.AutoClone
       class ClassUnderTest {
       }''')

    final Class<?> explicitAutoClone = parseClass('''@groovy.transform.AutoClone
       class ClassUnderTest {
           Object clone() throws java.lang.CloneNotSupportedException { null }
       }''')

    @Test
    void test_clone_is_annotated() {
        assertExactMethodIsAnnotated(implicitAutoClone, 'clone', Object)
    }

    @Test
    void test_clone_with_exact_type_is_annotated() {
        assertExactMethodIsAnnotated(implicitAutoClone, 'clone', implicitAutoClone)
    }

    @Test
    void test_clone_is_not_annotated() {
        assertExactMethodIsNotAnnotated(explicitAutoClone, 'clone', Object)
    }
}