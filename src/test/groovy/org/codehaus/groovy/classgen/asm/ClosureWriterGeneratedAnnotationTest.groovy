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
package org.codehaus.groovy.classgen.asm

import groovy.transform.Generated
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.junit.Test

import java.lang.reflect.Method

/**
 * Verifies if {@link Generated} annotations are added on {@code call} methods of generated closure classes.
 */
final class ClosureWriterGeneratedAnnotationTest {

    private CompilationUnit compileScript(String script) {
        new CompilationUnit().tap {
            addSource('script', script)
            compile(Phases.CLASS_GENERATION)
        }
    }

    private Collection<Class> findGeneratedClosureClasses(String outerClassName, CompilationUnit compilationUnit) {
        List<Class> generatedClosureClasses = []
        compilationUnit.classes.each {
            generatedClosureClasses.add(compilationUnit.classLoader.defineClass(it.name, it.bytes))
        }
        generatedClosureClasses.findAll { it.name =~ /${outerClassName}\$\_.*\_closure/ }
    }

    /**
     * For closure with implicit param, no {@code call} methods should be generated.
     */
    @Test
    void testClosureWithSingleParameter1() {
        String scriptText = '''
            class MyClass {
                void myMethod() {
                    [1..3].each {
                        println it
                    }
                }
            }
        '''
        Class<?> generatedClosureClass = findGeneratedClosureClasses('MyClass',compileScript(scriptText))[0]
        Collection<Method> callMethods = generatedClosureClass.declaredMethods.findAll { it.name == 'call' }

        assert callMethods.size() == 0
    }

    /**
     * For closure with annotated param, a single annotated {@code call} method with corresponding parameter should be generated.
     */
    @Test
    void testClosureWithSingleParameter2() {
        String scriptText = '''
            class MyClass {
                void myMethod() {
                    [1..3].each { @Deprecated i ->
                        println i
                    }
                }
            }
        '''
        Class<?> generatedClosureClass = findGeneratedClosureClasses('MyClass',compileScript(scriptText))[0]
        Collection<Method> callMethods = generatedClosureClass.declaredMethods.findAll { it.name == 'call' }

        assert callMethods.size() == 1
        assert callMethods[0].getAnnotation(Generated)
        assert callMethods[0].getParameterTypes() == new Class[] {Object}
    }

    /**
     * For closure with non-object param, a single annotated {@code call} method with corresponding parameter should be generated.
     */
    @Test
    void testClosureWithSingleParameter3() {
        String scriptText = '''
            class MyClass {
                void myMethod() {
                    [1..3].each { Integer myInt ->
                        println myInt
                    }
                }
            }
        '''
        Class<?> generatedClosureClass = findGeneratedClosureClasses('MyClass',compileScript(scriptText))[0]
        Collection<Method> callMethods = generatedClosureClass.declaredMethods.findAll { it.name == 'call' }

        assert callMethods.size() == 1
        assert callMethods[0].getAnnotation(Generated)
        assert callMethods[0].getParameterTypes() == new Class[] {Integer}
    }

    /**
     * For closure with multiple params, a single annotated {@code call} method with corresponding parameters should be generated.
     */
    @Test
    void testClosureWithMultipleParameters() {
        String scriptText = '''
            class MyClass {
                void myMethod() {
                    [1..3].eachWithIndex { IntRange entry, Integer i ->
                        println entry[i]
                    }
                }
            }
        '''
        Class<?> generatedClosureClass = findGeneratedClosureClasses('MyClass',compileScript(scriptText))[0]
        Collection<Method> callMethods = generatedClosureClass.declaredMethods.findAll { it.name == 'call' }

        assert callMethods.size() == 1
        assert callMethods[0].getAnnotation(Generated)
        assert callMethods[0].getParameterTypes() == new Class[] {IntRange, Integer}
    }
}
