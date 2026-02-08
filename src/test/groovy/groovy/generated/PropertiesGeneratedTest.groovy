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
class PropertiesGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> withProps = parseClass('''class WithProps {
           String name
       }''')

    final Class<?> withExplicitProps = parseClass('''class WithExplicitProps {
           private String name
           String getName() { name }
           void setName(String n) { name = n }
       }''')

    @Test
    void test_implicit_getName_is_annotated() {
        assertMethodIsAnnotated(withProps, 'getName')
    }

    @Test
    void test_implicit_setName_is_annotated() {
        assertMethodIsAnnotated(withProps, 'setName', String)
    }

    @Test
    void test_explicit_getName_is_not_annotated() {
        assertMethodIsNotAnnotated(withExplicitProps, 'getName')
    }

    @Test
    void test_explicit_setName_is_not_annotated() {
        assertMethodIsNotAnnotated(withExplicitProps, 'setName', String)
    }
}