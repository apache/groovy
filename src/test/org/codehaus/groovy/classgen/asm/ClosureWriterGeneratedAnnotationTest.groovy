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
import junit.framework.TestCase
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.junit.Test

/**
 * Verifies if {@link Generated} annotations are added on {@code call} methods of generated closure classes.
 */
class ClosureWriterGeneratedAnnotationTest extends TestCase {
    private CompilationUnit compileScript(String scriptText) {
        CompilationUnit compilationUnit = new CompilationUnit()
        compilationUnit.addSource("script", scriptText)
        compilationUnit.compile(Phases.ALL)

        compilationUnit
    }

    private Collection<Class> findGeneratedClosureClasses(String outerClassName, CompilationUnit compilationUnit) {
        Collection<Class> generatedClosureClasses = []
        compilationUnit.classes.each {
            generatedClosureClasses.add(compilationUnit.classLoader.defineClass(it.name, it.bytes))
        }

        return generatedClosureClasses.findAll({ it.name.matches(/.*${ outerClassName }\$\_.*\_closure.*/) })
    }

    /**
     * For closure without params, no {@code call} methods should be generated.
     */
    @Test
    void testClosureWithNoParameters() {
        String scriptText = """    
        class MyClass {
            void myMethod() {
                [1..3].each {
                    println it
                }
            }
        }
        """

        CompilationUnit compilationUnit = compileScript(scriptText)
        Class myClosureClassCompiled = findGeneratedClosureClasses("MyClass", compilationUnit)[0]
        Collection callMethods = myClosureClassCompiled.declaredMethods.findAll { it.name == "call" }

        assert callMethods.size() == 0
    }

    /**
     * For closure with single param, a single annotated {@code call} method with corresponding parameter should be generated.
     */
    @Test
    void testClosureWithSingleParameter() {
        String scriptText = """    
        class MyClass {
            void myMethod() {
                [1..3].each { Integer myInt ->
                    println myInt
                }
            }
        }
        """

        CompilationUnit compilationUnit = compileScript(scriptText)
        Class myClosureClassCompiled = findGeneratedClosureClasses("MyClass", compilationUnit)[0]
        Collection callMethodCollection = myClosureClassCompiled.declaredMethods.findAll { it.name == "call" }

        assert callMethodCollection.size() == 1
        assert callMethodCollection[0].getAnnotation(Generated)
        assert callMethodCollection[0].getParameterTypes() == new Class[] {Integer}
    }

    /**
     * For closure with multiple params, a single annotated {@code call} method with corresponding parameters should be generated.
     */
    @Test
    void testClosureWithMultipleParameters() {
        String scriptText = """    
        class MyClass {
            void myMethod() {
                [1..3].eachWithIndex { IntRange entry, Integer i ->
                    println entry[i]
                }
            }
        }
        """

        CompilationUnit compilationUnit = compileScript(scriptText)
        Class myClosureClassCompiled = findGeneratedClosureClasses("MyClass", compilationUnit)[0]
        Collection callMethodCollection = myClosureClassCompiled.declaredMethods.findAll { it.name == "call" }

        assert callMethodCollection.size() == 1
        assert callMethodCollection[0].getAnnotation(Generated)
        assert callMethodCollection[0].getParameterTypes() == new Class[] {IntRange, Integer}
    }
}
