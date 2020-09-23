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
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import java.util.Map.Entry

/**
 * Tests for the {@code @Newify} AST transform.
 */
@RunWith(JUnit4)
class NewifyTransformBlackBoxTest extends CompilableTestSupport {

  @Test
  void testNewifyWithoutNamePattern() {
    final String classPart = """
            final a = A('XyZ')
            String foo(final x = null) { x?.toString() }
        """
    final script = newifyTestScript(true, [value: "[A]"], classPart, "final foo = new $newifyTestClassName(); foo.foo()")
    println script
    assert script.contains('@Newify')
    assertScript(script)
  }

  @Test
  void testNewifyWithoutNamePatternFails() {
    final String classPart = classCode([
        "final a = A('XyZ')",
        "final ab0 = new AB('XyZ')",
        "final ab1 = AB('XyZ')",
        "String foo(final x = null) { x?.toString() }"
    ])

    final script0 = newifyTestScript(true, [value: "[A,AB]"], classPart, "final foo = new $newifyTestClassName(); foo.foo()")
    final script1 = newifyTestScript(true, [value: "[A]"], classPart, "final foo = new $newifyTestClassName(); foo.foo()")

    assertScript(script0)

    final result = shouldNotCompile(script1)
    assert result.contains("Cannot find matching method NewifyFoo#AB(java.lang.String)")
  }


  @Test
  void testRegularClassNewifyWithNamePattern() {
    final String script = """
              import groovy.transform.Canonical
              import groovy.transform.CompileStatic
              import groovy.lang.Newify
              import java.lang.StringBuilder
              import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS

              @Canonical class TheClass { String classField }

              @Newify(pattern=/[A-Z][A-Za-z0-9_]+/)
              @CompileStatic
              def newTheClassField() {
                final sb = StringBuilder(13)
                sb.append("abc"); sb.append("_")
                sb.append("123"); sb.append("_")
                sb.append(sb.capacity())
                return sb
              }

               newTheClassField()
          """

    println "script=|$script|"
    final result = evalScript(script)
    println "result=$result"
    assert result instanceof java.lang.StringBuilder
    assert result.toString() == 'abc_123_13'
  }


  @Test
  void testInnerScriptClassNewifyWithNamePattern() {
    final String script = """
              import groovy.transform.Canonical
              import groovy.transform.CompileStatic
              import groovy.lang.Newify
              import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS

              @Canonical class A { String a }
              @Canonical class AB { String a; String b }
              @Canonical class ABC { String a; String b; String c }

              @Newify(pattern=/[A-Z].*/)
              @CompileStatic
              def createClassList() {
                final l = [ A('2018-04-08'), AB("I am", "class AB"), ABC("A","B","C") ]
                [ l.collect { it.getClass().getCanonicalName() }, l.collect { it.toString() } ]
              }

              createClassList()
          """

    println "script=|$script|"
    final List resultList = (List) evalScript(script)
    println "result=$resultList"

    assert resultList[0] == ['A', 'AB', 'ABC']
    assert resultList[1] == ['A(2018-04-08)', 'AB(I am, class AB)', 'ABC(A, B, C)']
  }


  @Test
  void testInnerClassesNewifyWithNamePattern() {
    final String script = """
        import groovy.transform.Canonical
        import groovy.transform.CompileStatic
        import groovy.lang.Newify
        import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS

        @Newify(pattern=/[A-Z].*/)
        class Foo {
          @Canonical class A { String a }
          @Canonical class AB { String a; String b }
          @Canonical class ABC { String a; String b; String c }

          List createClassList() {
            final l = [ A('2018-04-08'), AB("I am", "class AB"), ABC("A","B","C") ]
            //final l = [ A(this, '2018-04-08'), AB(this, "I am", "class AB"), ABC(this, "A","B","C") ]
            [ l.collect { it.getClass().getCanonicalName() }, l.collect { it.toString() } ]
          }
        }

        final Foo foo = new Foo()
        foo.createClassList()
      """

    println "script=|$script|"
    final List resultList = (List) evalScript(script)
    println "result=$resultList"

    assert resultList[0] == ['Foo.A', 'Foo.AB', 'Foo.ABC']
    assert resultList[1] == ['Foo$A(2018-04-08)', 'Foo$AB(I am, class AB)', 'Foo$ABC(A, B, C)']
  }


  @Test
  void testInnerStaticClassesNewifyWithNamePattern() {
    final String script = """
          import groovy.transform.Canonical
          import groovy.transform.CompileStatic
          import groovy.lang.Newify
          import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS

          @Newify(pattern=/[A-Z].*/)
          class Foo {
            @Canonical static class A { String a }
            @Canonical static class AB { String a; String b }
            @Canonical static class ABC { String a; String b; String c }

            List createClassList() {
              final l = [ A('2018-04-08'), AB("I am", "class AB"), ABC("A","B","C") ]
              [ l.collect { it.getClass().getCanonicalName() }, l.collect { it.toString() } ]
            }
          }

          final Foo foo = new Foo()
          foo.createClassList()
      """

    println "script=|$script|"
    final List resultList = (List) evalScript(script)
    println "result=$resultList"

    assert resultList[0] == ['Foo.A', 'Foo.AB', 'Foo.ABC']
    assert resultList[1] == ['Foo$A(2018-04-08)', 'Foo$AB(I am, class AB)', 'Foo$ABC(A, B, C)']
  }


  @Test
  void testAmbiguousInnerStaticClassesNewifyWithNamePatternFails() {
    final String script = """
          import groovy.transform.CompileStatic
          import groovy.lang.Newify
          import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS

          @Newify(pattern=/[A-Z].*/)
          class Foo {
            static class Foo {
              static class Foo { }
            }
            List createClassList() {
              final l = [ new Foo(), new Foo.Foo.Foo(), Foo() ]
              [ l.collect { it.getClass().getCanonicalName() }, l.collect { it.toString() } ]
            }
          }

          final Foo foo = new Foo()
          foo.createClassList()
      """

    println "script=|$script|"

    final String result = shouldNotCompile(script)
    assert result ==~ '(?s).*Inner class name lookup is ambiguous between the following classes: Foo, Foo\\$Foo, Foo\\$Foo\\$Foo\\..*'
  }


  @Test
  void testImportedClassesNewifyWithNamePattern() {
    final String script = """
        import groovy.transform.Canonical
        import groovy.transform.CompileStatic
        import groovy.lang.Newify
        import java.lang.StringBuilder
        import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS

        @Canonical class A { String a }
        @Canonical class AB { String a; String b }
        @Canonical class ABC { String a; String b; String c }

        @Newify(pattern=/[A-Z][A-Za-z0-9_]*/)
        @CompileStatic
        def createClassList() {
          final l = [ A('2018-04-08'), StringBuilder('*lol*'), AB("I am", "class AB"), ABC("A","B","C") ]
          [ l.collect { it.getClass().getCanonicalName() }, l.collect { it.toString() } ]
        }

        createClassList()
      """

    println "script=|$script|"
    final List resultList = (List) evalScript(script)
    println "result=$resultList"

    assert resultList[0] == ['A', 'java.lang.StringBuilder', 'AB', 'ABC']
    assert resultList[1] == ['A(2018-04-08)', '*lol*', 'AB(I am, class AB)', 'ABC(A, B, C)']
  }


  @Test
  void testAlwaysExistingClassesNewifyWithNamePattern() {
    final String script = """
              import groovy.transform.Canonical
              import groovy.transform.CompileStatic
              import groovy.lang.Newify
              import java.lang.StringBuilder
              import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS

              @Canonical class A { String a }
              @Canonical class AB { String a; String b }
              @Canonical class ABC { String a; String b; String c }

              @Newify(pattern=/[A-Z][A-Za-z0-9_]*/)
              @CompileStatic
              def createClassList() {
                final l = [ A('2018-04-08'), StringBuilder('*lol*'), AB("I am", "class AB"), ABC("A","B","C"), Object() ]
                [ l.collect { it.getClass().getName() }, l.collect { it.toString().replaceAll(/@[a-f0-9]+\\b/,'') } ]
              }

              createClassList()
          """

    println "script=|$script|"
    final List resultList = (List) evalScript(script)
    println "result=$resultList"

    assert resultList[0] == ['A', 'java.lang.StringBuilder', 'AB', 'ABC', 'java.lang.Object']
    assert resultList[1] == ['A(2018-04-08)', '*lol*', 'AB(I am, class AB)', 'ABC(A, B, C)', 'java.lang.Object']
  }


  @Test
  void testNewifyWithNamePatternMixed() {
    final String script = """
              import groovy.transform.Canonical
              import groovy.transform.CompileStatic
              import groovy.lang.Newify
              import java.lang.StringBuilder
              import groovy.lang.Binding
              import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS

              @Canonical class A { String a }
              @Canonical class AB { String a; String b }
              @Canonical class ABC { String a; String b; String c }

              @Newify(pattern=/[A-Z][A-Za-z0-9_]*/)
              @CompileStatic
              def createClassList() {
                final l = [
                  A('2018-04-08'), StringBuilder('*lol*'), AB("I am", "class AB"), ABC("A","B","C"), Object(),
                  Reference(), Binding(), Double(123.456d), Integer(987), BigInteger('987654321',10),
                  BigDecimal('1234.5678')
                ]
                [ l.collect { it.getClass().getName() }, l.collect { it.toString().replaceAll(/@[a-f0-9]+\\b/,'') } ]
              }

              createClassList()
          """

    println "script=|$script|"
    final List resultList = (List) evalScript(script)
    println "result=$resultList"

    assert resultList[0] == [
        'A', 'java.lang.StringBuilder', 'AB', 'ABC', 'java.lang.Object',
        'groovy.lang.Reference', 'groovy.lang.Binding', 'java.lang.Double', 'java.lang.Integer', 'java.math.BigInteger',
        'java.math.BigDecimal'
    ]
    assert resultList[1] == [
        'A(2018-04-08)', '*lol*', 'AB(I am, class AB)', 'ABC(A, B, C)', 'java.lang.Object',
        'groovy.lang.Reference', 'groovy.lang.Binding', '123.456', '987', '987654321',
        '1234.5678'
    ]
  }


  @Test
  void testAliasImportedClassesNewifyWithNamePattern() {
    final String script = """
        import groovy.lang.Newify
        import java.lang.StringBuilder as WobblyOneDimensionalObjectBuilda
        import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS

        @Newify(pattern=/[A-Z][A-Za-z0-9_]*/)
        def createClassList() {
          final l = [ WobblyOneDimensionalObjectBuilda('Discrete Reality') ]
          [ l.collect { it.getClass().getCanonicalName() }, l.collect { it.toString() } ]
        }

        createClassList()
      """

    println "script=|$script|"
    final List resultList = (List) evalScript(script)
    println "result=$resultList"

    assert resultList[0] == ['java.lang.StringBuilder']
    assert resultList[1] == ['Discrete Reality']
  }


  @Test
  void testAliasShadowededImportedClassesNewifyWithNamePatternFails() {
    final String script = """
        import groovy.transform.CompileStatic
        import groovy.lang.Newify
        import java.lang.StringBuilder as WobblyOneDimensionalObjectBuilda
        import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS

        @CompileStatic
        @Newify(pattern=/[A-Z][A-Za-z0-9_]*/)
        def createClassList() {
          final l = [ WobblyOneDimensionalObjectBuilda('Discrete Reality'), StringBuilder('Quantum Loops') ]
          [ l.collect { it.getClass().getCanonicalName() }, l.collect { it.toString() } ]
        }

        createClassList()
      """

    println "script=|$script|"

    final String result = shouldNotCompile(script)
    assert result ==~ /(?s).*\[Static type checking] - Cannot find matching method TestScript[A-Za-z0-9]*#StringBuilder\(java\.lang\.String\).*/
  }


  @Test
  void testInvalidNamePatternNewifyWithNamePatternFails() {
    final String script = """
        import groovy.transform.CompileStatic
        import groovy.lang.Newify
        import java.lang.StringBuilder as WobblyOneDimensionalObjectBuilda
        import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS

        @CompileStatic
        @Newify(pattern=/[A-/)
        def createClassList() {
          final l = [ WobblyOneDimensionalObjectBuilda('Discrete Reality'), StringBuilder('Quantum Loops') ]
          [ l.collect { it.getClass().getCanonicalName() }, l.collect { it.toString() } ]
        }

        createClassList()
      """

    println "script=|$script|"

    final String result = shouldNotCompile(script)
    assert result ==~ /(?s).*Invalid class name pattern: Illegal character range near index 3.*/
  }


  @Test
  void testStaticallyAndDynamicallyCompiledMixedClassesNewifyWithNamePattern() {
    final List<Boolean> compileStaticFlags = [true]
    assertMixedClassesNewifyWithNamePatternResult("@Newify(pattern=/[A-Z].*/)", compileStaticFlags,
        ['Foo.A', 'Foo.AB', 'Foo.ABC'], ['Foo$A(2018-04-08)', 'Foo$AB(I am, class AB)', 'Foo$ABC(A, B, C)']
    )
  }

  @Test
  void testStaticallyCompiledMixedClassesNoNewify() {
    assertMixedClassesNewifyWithNamePatternFails("", [true], standardCompileStaticErrorMsg)
  }

  @Test
  void testStaticallyCompiledMixedClassesNewifyWithNamePattern() {
    assertMixedClassesNewifyWithNamePatternFails("@Newify(pattern=/XXX/)", [true], standardCompileStaticErrorMsg)
  }

  @Test
  void testDynmaicallyCompiledMixedClassesNoNewify() {
    assertMixedClassesNewifyWithNamePatternFails("", [false], standardCompileDynamiccErrorMsg)
  }

  @Test
  void testDynmaicallyCompiledMixedClassesNewifyWithNamePattern() {
    assertMixedClassesNewifyWithNamePatternFails("@Newify(pattern=/XXX/)", [false], standardCompileDynamiccErrorMsg)
  }


  @Test
  void testExtractName() {
    ['', 'A', 'Bc', 'DEF'].each { String s ->
      assertExtractName(s, s)
      assertExtractName("\$$s", s)
      assertExtractName("A\$$s", s)
      assertExtractName("Foo\$$s", s)
      assertExtractName("Foo\$Foo\$$s", s)
      assertExtractName("A\$AB\$ABC\$$s", s)
    }
  }


  String getStandardCompileDynamiccErrorMsg() {
    "No signature of method: Foo.A() is applicable for argument types: (String) values: [2018-04-08]"
  }

  String getStandardCompileStaticErrorMsg() {
    "[Static type checking] - Cannot find matching method Foo#A(java.lang.String)."
  }

  void assertMixedClassesNewifyWithNamePatternFails(
      final String newifyAnnotation, final List<Boolean> compileStaticFlags, final String errorMsgStartsWith) {
    try {
      mixedClassesNewifyWithNamePattern(newifyAnnotation, compileStaticFlags)
    }
    catch(Exception e) {
      assert e.message.contains(errorMsgStartsWith)
    }
  }

  void assertMixedClassesNewifyWithNamePatternResult(
      final String newifyAnnotation,
      final List<Boolean> compileStaticFlags, final List<String> classNameList, final List<String> resultList) {
    final List list = mixedClassesNewifyWithNamePattern(newifyAnnotation, compileStaticFlags)
    assert list[0] == classNameList
    assert list[1] == resultList
  }

  List mixedClassesNewifyWithNamePattern(final String newifyAnnotation, final List<Boolean> compileStaticFlags) {

    int iCompileStaticOrDynamic = 0
    final Closure<String> compileStaticOrDynamicCls = {
      compileStaticFlags[iCompileStaticOrDynamic++] ? "@CompileStatic" : "@CompileDynamic"
    }

    final String script = """
            import groovy.transform.Canonical
            import groovy.transform.CompileStatic
            import groovy.transform.CompileDynamic
            import groovy.lang.Newify
            import java.lang.StringBuilder
            import groovy.lang.Binding
            import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS

            $newifyAnnotation
            ${compileStaticOrDynamicCls()}
            class Foo {
              @Canonical static class A { String a }
              @Canonical static class AB { String a; String b }
              @Canonical static class ABC { String a; String b; String c }

              List createClassList() {
                final l = [ A('2018-04-08'), AB("I am", "class AB"), ABC("A","B","C") ]
                [ l.collect { it.getClass().getCanonicalName() }, l.collect { it.toString() } ]
              }
            }

            final Foo foo = new Foo()
            foo.createClassList()
        """

    println "script=|$script|"
    final List resultList = (List) evalScript(script)
    println "result=$resultList"

    return resultList
  }


  void assertExtractName(final String s, final String expected) {
    final String result = NewifyASTTransformation.extractName(s)
    println "|$s| -> |$result|"
    assert result == expected
  }


  String classCode(final List<String> lines) { code(lines, 1) }

  String scriptCode(final List<String> lines) { code(lines, 0) }

  String code(final List<String> lines, final int indent = 0) {
    lines.collect { "${'\t' * indent}${it};" }.join('\n')
  }

  String newifyTestScript(
      final boolean hasAnnotation,
      final Map<String, Object> annotationParameters,
      final String classPart, final String scriptPart = '') {
    assert !hasAnnotation || (annotationParameters != null); assert classPart
    final String annotationParametersTerm = annotationParameters ? "(${annotationParameters.collect { final Entry<String, Object> e -> "$e.key=$e.value" }.join(', ')})" : ''
    final String script = """
            import groovy.transform.Canonical
            import groovy.transform.CompileStatic
            import groovy.lang.Newify
            import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS

            @Canonical class A { String a }
            @Canonical class AB { String a; String b }
            @Canonical class ABC { String a; String b; String c }

            @CompileStatic
            ${hasAnnotation ? "@Newify${annotationParametersTerm}" : ''}
            class $newifyTestClassName {
                $classPart
            }

            $scriptPart
        """
    return script
  }

  String getNewifyTestClassName() {
    'NewifyFoo'
  }


  static def evalScript(final String script) throws Exception {
    GroovyShell shell = new GroovyShell();
    shell.evaluate(script);
  }


  static Throwable compileShouldThrow(final String script, final String testClassName) {
    try {
      final GroovyClassLoader gcl = new GroovyClassLoader()
      gcl.parseClass(script, testClassName)
    }
    catch(Throwable throwable) {
      return throwable
    }
    throw new Exception("Script was expected to throw here!")
  }

}
