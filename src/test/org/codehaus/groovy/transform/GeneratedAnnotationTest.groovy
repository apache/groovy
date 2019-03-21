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
package org.codehaus.groovy.transform

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Tests for the @Generated annotation.
 */
@RunWith(JUnit4)
class GeneratedAnnotationTest extends GroovyShellTestCase {
    @Rule public TestName nameRule = new TestName()

    @Before
    void setUp() {
        super.setUp()
    }

    @After
    void tearDown() {
        super.tearDown()
    }

    @Test
    void testDefaultGroovyMethodsAreAnnotatedWithGenerated() {
        def person = evaluate('''
            class Person {
                String name
            }
            new Person()
        ''')

        GroovyObject.declaredMethods.each { m ->
            def method = person.class.declaredMethods.find { it.name == m.name }
            assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')
        }
    }

    @Test
    void testDefaultConstructorIsAnnotatedWithGenerated_GROOVY9051() {
        def person = evaluate('''
            class Person {
            }
            new Person()
        ''')

        def cons = person.class.declaredConstructors
        assert cons.size() == 1
        assert cons[0].annotations*.annotationType().name.contains('groovy.transform.Generated')
    }

    @Test
    void testOverriddenDefaultGroovyMethodsAreNotAnnotatedWithGenerated() {
        def person = evaluate('''
            class Person {
                String name
                
                def invokeMethod(String name, args) { }
            }
            new Person()
        ''')

        def method = person.class.declaredMethods.find { it.name == 'invokeMethod' }
        assert !('groovy.transform.Generated' in method.annotations*.annotationType().name)
    }
}