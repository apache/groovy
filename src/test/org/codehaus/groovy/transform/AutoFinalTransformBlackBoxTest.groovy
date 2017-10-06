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

import gls.CompilableTestSupport
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


/**
 * Tests for the {@code @AutoFinal} AST transform.
 */

// Execute single test:
// gradlew :test --build-cache --tests org.codehaus.groovy.transform.AutoFinalTransformTest
@RunWith(JUnit4)
class AutoFinalTransformBlackBoxTest extends CompilableTestSupport {

  @Test
  @Ignore
  void testAutoFinalOnClass_v2() {
    // 1) ASTTest explicitely checks for final modifier (which isn't put into bytecode)
    // 2) shouldNotCompile checks that the Groovy compiler responds in the expected way to an attempt at assigning a value to a method parameter
    final result = shouldNotCompile('''
            import groovy.transform.AutoFinal
            import groovy.transform.ASTTest
            import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS
            import static java.lang.reflect.Modifier.isFinal

            @ASTTest(phase=SEMANTIC_ANALYSIS, value = {
                assert node.methods.size() == 1
                node.methods[0].with {
                    assert it.name == 'fullName'
                    assert it.parameters.every{ p -> isFinal(p.modifiers) }
                }
                assert node.constructors.size() == 1
                node.constructors[0].with {
                    assert it.parameters.every{ p -> isFinal(p.modifiers) }
                }
            })
            @AutoFinal
            class Person {
                final String first, last
                Person(String first, String last) {
                    this.first = first
                    this.last = last
                }
                String fullName(boolean reversed = false, String separator = ' ') {
                    reversed = true
                    seperator = '<!#!>'
                    "${reversed ? last : first}$separator${reversed ? first : last}"
                }
            }

            final js = new Person('John', 'Smith')
            assert js.fullName() == 'John Smith'
            assert js.fullName(true, ', ') == 'Smith, John'
        ''')
    //println "\n\nAutoFinalTransformTest#testAutoFinalOnClass2 result: |$result|\n\n"
    assert result.contains('The parameter [reversed] is declared final but is reassigned')
  }

  @Test
  @Ignore
  void testAutoFinal_v1() {
    final result = shouldNotCompile('''
        //final throwable = shouldThrow(' ''
            import groovy.transform.AutoFinal
            import groovy.transform.ASTTest
            import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS
            import static java.lang.reflect.Modifier.isFinal

            @AutoFinal
            class Person {
                final String first, last
                Person(String first, String last) {
                    this.first = first
                    this.last = last
                }
                String fullName(boolean reversed = false, String separator = ' ') {
                    final cls = { String finalClsParam0 -> finalClsParam0 = "abc"; finalClsParam0 }
                    final clsResult = cls()
                    return clsResult
                }
            }

            final js = new Person('John', 'Smith')
            assert js.fullName() == 'John Smith'
            assert js.fullName(true, ', ') == 'Smith, John'
        ''')

    //printStackTrace(throwable)

    //println "\n\n${throwable.printStackTrace()}"

    println "\n\nAutoFinalTransformTest#testAutoFinalOnClosure_v1 result: |$result|\n\n"
    assert result.contains('The parameter [finalClsParam0] is declared final but is reassigned')
  }



  @Test
  void testAutoFinal() {
    assertAutoFinalClassTestScript("param0", ["String foo() { final cls = { String param0 -> param0 = 'abc'; finalClsParam1 }; cls() }"])
  }

  @Test
  void testAutoFinalClassMethod1() {
    assertAutoFinalClassTestScript("param1", ["String foo(String param1, param2) {  param1 = 'abc'; param1 }"])
  }

  @Test
  void testAutoFinalClassMethod2() {
    assertAutoFinalClassTestScript("param2", ["String foo(String param1, param2) {  param2 = new Object(); param2 }"])
  }

  @Test
  void testAutoFinalClassMethodDefaultParameters() {
    assertAutoFinalClassTestScript("param2", ["String foo(String param1, param2) {  param2 = new Object(); param2 }"])
  }



  void assertAutoFinalClassTestScript(final String paramName, final List<String> classBodyTerms) {
    assertAutoFinalTestScriptWithAnnotation(paramName, classBodyTerms)
    assertAutoFinalTestScriptWithoutAnnotation(paramName, classBodyTerms)
  }

  // Checks that the Groovy compiler rejects an attempt to assign a value to a method parameter
  void assertAutoFinalTestScriptWithAnnotation(final String paramName, final List<String> classBodyTerms) {
    final script = autoFinalTestScript(true, classBodyTerms)
    final result = shouldNotCompile(script)
    println "\nassertAutoFinalTestScript result: |$result|\n\n"
    assert result.contains("The parameter [$paramName] is declared final but is reassigned")
  }

  void assertAutoFinalTestScriptWithoutAnnotation(final String paramName, final List<String> classBodyTerms) {
    final script = autoFinalTestScript(false, classBodyTerms)
    shouldCompile(script)
  }

  String autoFinalTestScript(final boolean autoFinalAnnotationQ, final List<String> classBodyTerms) {
    final String script = """
            import groovy.transform.AutoFinal
            import groovy.transform.ASTTest
            import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS
            import static java.lang.reflect.Modifier.isFinal

            ${autoFinalAnnotationQ ? '@AutoFinal' : ''}
            class AutoFinalFoo {
                ${classBodyTerms.collect { "\t\t\t\t$it" }.join('\n')}
            }
        """
    println "script: |$script|"
    return script
  }


  void printStackTrace(final Throwable throwable) {
    println "${throwable.getClass().name}${throwable.message ? ": $throwable.message" : ""}"
    throwable.stackTrace.each { println it }
    final inner = throwable.cause
    if(inner != null) {
      println "Caused by........................................................................................."
      printStackTrace(inner)
    }
  }


  Throwable shouldThrow(final String script) {
    try {
      final GroovyClassLoader gcl = new GroovyClassLoader()
      gcl.parseClass(script, getTestClassName())
    }
    catch(Throwable throwable) {
      return throwable
    }
    throw new Exception("Script was expected to throw here!")
  }
}
