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
class GroovyObjectGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> classUnderTest = parseClass('class MyClass { }')

    @Test
    void test_invokeMethod_is_annotated() {
        assertMethodIsAnnotated(classUnderTest, 'invokeMethod', String, Object)
    }

    @Test
    void test_getProperty_is_annotated() {
        assertMethodIsAnnotated(classUnderTest, 'getProperty', String)
    }

    @Test
    void test_setProperty_is_annotated() {
        assertMethodIsAnnotated(classUnderTest, 'setProperty', String, Object)
    }

    @Test
    void test_getMetaClass_is_annotated() {
        assertMethodIsAnnotated(classUnderTest, 'getMetaClass')
    }

    @Test
    void test_setMetaClass_is_annotated() {
        assertMethodIsAnnotated(classUnderTest, 'setMetaClass', MetaClass)
    }
}