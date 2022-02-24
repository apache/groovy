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

import groovy.test.GroovyShellTestCase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import java.lang.reflect.Method

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

        GroovyObject.declaredMethods.findAll{ !isCoverageInstrumentationMethod(it) }.each { m ->
            def method = person.class.declaredMethods.find { it.name == m.name }
            if (method && !method.name.contains('jacoco')) {
                assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')
            }
        }
    }

    private static boolean isCoverageInstrumentationMethod(Method it) {
        it.name.contains('jacoco')
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

    @Test
    void testCapturedArgForGeneratedClosureIsAnnotatedWithGenerated_GROOVY9396() {
        def objectUnderTest = evaluate'''
        class ClassUnderTest {
            Closure<String> c(String arg) {
                def closureVar = {
                    arg
                }
                closureVar
            }
        }
        new ClassUnderTest()
        '''

        Closure<String> closure = objectUnderTest.class.getMethod('c', String).invoke(objectUnderTest, 'var')
        def getArg = closure.class.getMethod('getArg')
        assert getArg.annotations*.annotationType().name.contains('groovy.transform.Generated')
    }

    @Test
    void testTraitComposerMarksGeneratedMethodsForVariablesAsGenerated_GROOVY10505() {
        def objectUnderTest = evaluate'''
        trait TraitWithVariable {
            private String variableA
        }
        trait TraitWithFinalVariable {
            private final String variableB
        }
        trait TraitWithStaticVariable {
            private static String variableC
        }
        trait TraitWithVariableInitialized {
            private String variableA = "simple variable initialized"
        }
        trait TraitWithFinalVariableInitialized {
            private final String variableB = "final variable initialized"
        }
        trait TraitWithStaticVariableInitialized {
            private static String variableC = "static variable initialized"
        }
        trait TraitCompose implements TraitWithVariable, TraitWithFinalVariable, TraitWithStaticVariable,
                                        TraitWithVariableInitialized, TraitWithFinalVariableInitialized,
                                        TraitWithStaticVariableInitialized {

        }
        class ClassUnderTest implements TraitCompose {

        }
        new ClassUnderTest()
        '''

        def method = objectUnderTest.class.declaredMethods.find { it.name == 'TraitWithVariable__variableA$get'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')
        method = objectUnderTest.class.declaredMethods.find { it.name == 'TraitWithVariable__variableA$set'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')

        method = objectUnderTest.class.declaredMethods.find { it.name == 'TraitWithFinalVariable__variableB$get'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')
        method = objectUnderTest.class.declaredMethods.find { it.name == 'TraitWithFinalVariable__variableB$set'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')

        method = objectUnderTest.class.declaredMethods.find { it.name == 'TraitWithStaticVariable__variableC$get'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')
        method = objectUnderTest.class.declaredMethods.find { it.name == 'TraitWithStaticVariable__variableC$set'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')

        method = objectUnderTest.class.declaredMethods.find { it.name == 'TraitWithVariableInitialized__variableA$get'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')
        method = objectUnderTest.class.declaredMethods.find { it.name == 'TraitWithVariableInitialized__variableA$set'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')

        method = objectUnderTest.class.declaredMethods.find { it.name == 'TraitWithFinalVariableInitialized__variableB$get'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')
        method = objectUnderTest.class.declaredMethods.find { it.name == 'TraitWithFinalVariableInitialized__variableB$set'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')

        method = objectUnderTest.class.declaredMethods.find { it.name == 'TraitWithStaticVariableInitialized__variableC$get'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')
        method = objectUnderTest.class.declaredMethods.find { it.name == 'TraitWithStaticVariableInitialized__variableC$set'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')
    }

    @Test
    void testTraitComposerMarksGeneratedMethodsForMethodsAsGenerated_GROOVY10505() {
        def objectUnderTest = evaluate'''
        trait TraitWithMethod {
            String methodA() { "method without generated annotation" }
        }
        trait TraitWithMethodAsGenerated {
            @groovy.transform.Generated
            String methodB() { "method with generated annotation" }
        }
        trait TraitWithStaticMethod {
            static String methodC() { "static method" }
        }
        trait TraitWithFinalMethod {
            final String methodD() { "final method" }
        }
        trait TraitCompose implements TraitWithMethod, TraitWithMethodAsGenerated, TraitWithStaticMethod, TraitWithFinalMethod {

        }
        class ClassUnderTest implements TraitCompose {

        }
        new ClassUnderTest()
        '''

        def method = objectUnderTest.class.declaredMethods.find { it.name == 'TraitWithMethodAsGeneratedtrait$super$methodB'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')

        method = objectUnderTest.class.declaredMethods.find { it.name == 'TraitWithMethodtrait$super$methodA'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')

        method = objectUnderTest.class.declaredMethods.find { it.name == 'TraitWithFinalMethodtrait$super$methodD'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')


        // and original methods are marked as they were

        method = objectUnderTest.class.declaredMethods.find { it.name == 'methodA'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated') == false

        method = objectUnderTest.class.declaredMethods.find { it.name == 'methodB'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated')

        method = objectUnderTest.class.declaredMethods.find { it.name == 'methodC'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated') == false

        method = objectUnderTest.class.declaredMethods.find { it.name == 'methodD'}
        assert method.annotations*.annotationType().name.contains('groovy.transform.Generated') == false
    }
}