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

import java.lang.reflect.Modifier


/**
 * Tests for the {@code @AutoFinal} AST transform.
 */

// Execute single test:
// gradlew :test --build-cache --tests org.codehaus.groovy.transform.AutoFinalTransformTest
@RunWith(JUnit4)
class AutoFinalTransformBlackBoxTest extends CompilableTestSupport {

  @Test
  void testAutoFinal() {
    //assertAutoFinalClassTestScript("param0", ["String foo() { final cls = { String param0 -> param0 = 'abc'; finalClsParam1 }; cls() }"])
    assertAutoFinalClassTestScript("param0", ["String foo() { final cls = { String param0 -> param0 = 'abc'; param0 }; cls() }"])
  }

  @Test
  void testAutoFinalClassMethod1() {
    assertAutoFinalClassTestScript("param1", ["String foo(String param1, param2) {  param1 = 'abc'; param1 }"])
  }

  @Test
  void testAutoFinalClassMethod2() {
    assertAutoFinalClassTestScript("param2", ["String foo(String param1, param2) {  param2 = new Object(); param2 }"])
  }

  // Check default parameters are not negatively impacted by @AutoFinal
  @Test
  void testAutoFinalClassMethodDefaultParameters() {
    final String classPart = """
      String foo(String param1 = 'XyZ', param2 = Closure.IDENTITY ) { 
        assert param1.equals('XyZ')
        assert param2.is(Closure.IDENTITY)
        return param1 
      }
    """
    final script = autoFinalTestScript(true, [ classPart ], "final foo = new $autoFinalTestClassName(); foo.foo()")
    assert script.contains('@AutoFinal')
    assertScript(script)
  }




  void assertAutoFinalClassTestScript(final String paramName, final List<String> classBodyTerms) {
    assertAutoFinalTestScriptWithAnnotation(paramName, classBodyTerms)
    assertAutoFinalTestScriptWithoutAnnotation(paramName, classBodyTerms)
  }

  // Checks Groovy compiler behavior when putting the passed classBodyTerms into an @AutoFinal annotated class
  void assertAutoFinalTestScriptWithAnnotation(final String paramName, final List<String> classBodyTerms) {
    final script = autoFinalTestScript(true, classBodyTerms)
    assert script.contains('@AutoFinal')
    final result = shouldNotCompile(script)
    println "\nassertAutoFinalTestScript result: |$result|\n\n"
    assert result.contains("The parameter [$paramName] is declared final but is reassigned")
  }

  void assertAutoFinalTestScriptWithoutAnnotation(final String paramName, final List<String> classBodyTerms) {
    final script = autoFinalTestScript(false, classBodyTerms)
    assert !script.contains('@AutoFinal')
    shouldCompile(script)
  }

  String autoFinalTestScript(final boolean autoFinalAnnotationQ, final List<String> classBodyTerms, final String scriptTerm = '') {
    final String script = """
            import groovy.transform.AutoFinal
            import groovy.transform.ASTTest
            import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS
            import static java.lang.reflect.Modifier.isFinal

            ${autoFinalAnnotationQ ? '@AutoFinal' : ''}
            class $autoFinalTestClassName {
                ${classBodyTerms.collect { "\t\t\t\t$it" }.join('\n')}
            } 

            $scriptTerm
        """
    println "script: |$script|"
    return script
  }

  String getAutoFinalTestClassName() {
    'AutoFinalFoo'
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
