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
package groovy.test

import junit.framework.TestCase
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

class JavadocAssertionTestBuilderTest {
    def builder

    @BeforeEach
    void setUp() {
        builder = new JavadocAssertionTestBuilder()
    }

    @Test
    void testBuildsTest() {
        Class test = builder.buildTest("SomeClass.java", '/** <pre class="groovyTestCase"> assert true </pre> */ public class SomeClass { }')
        assert test.newInstance() instanceof TestCase
        assert test.simpleName == "SomeClassJavadocAssertionTest"
        test.newInstance().testAssertionFromSomeClassLine1()
    }

    @Test
    void testAssertionsAreCalled() {
        Class test = builder.buildTest("SomeClass.java", '/** <pre class="groovyTestCase"> assert false </pre> */ public class SomeClass { }')
        shouldFail(AssertionError) {
            test.newInstance().testAssertionFromSomeClassLine1()
        }
    }

    @Test
    void testCombinedClassesAreRecognised() {
        Class test = builder.buildTest("SomeClass.java",
                '/** <pre class="language-groovy groovyTestCase"> assert 1 + 1 == 2 </pre> */ public class SomeClass { }')
        assert test != null
        test.newInstance().testAssertionFromSomeClassLine1()
    }

    @Test
    void testSimilarClassNameIsNotMatched() {
        Class test = builder.buildTest("SomeClass.java",
                '/** <pre class="notAGroovyTestCaseAtAll"> assert false </pre> */ public class SomeClass { }')
        assert test == null
    }

    @Test
    void testSnippetTagWithGroovyTestCaseIdIsRecognised() {
        // {@snippet} body is verbatim per JEP 413, so no HTML-entity escaping.
        Class test = builder.buildTest("SomeClass.java",
                '''/**
                   | * {@snippet lang="groovy" id="groovyTestCase" :
                   | * assert "a<b>c".size() == 5
                   | * }
                   | */
                   |public class SomeClass { }'''.stripMargin())
        assert test != null
        test.newInstance().testAssertionFromSomeClassLine2()
    }

    @Test
    void testSnippetTagAssertionsAreExecuted() {
        Class test = builder.buildTest("SomeClass.java",
                '''/**
                   | * {@snippet lang="groovy" id="groovyTestCase" :
                   | * assert false
                   | * }
                   | */
                   |public class SomeClass { }'''.stripMargin())
        shouldFail(AssertionError) {
            test.newInstance().testAssertionFromSomeClassLine2()
        }
    }

    @Test
    void testSnippetTagWithBalancedBracesInBody() {
        // Brace-balanced body — closures and GStrings contribute {/} pairs that
        // must balance for the tag parser to find the correct closing brace.
        Class test = builder.buildTest("SomeClass.java",
                '''/**
                   | * {@snippet lang="groovy" id="groovyTestCase" :
                   | * def items = [1, 2, 3]
                   | * def sum = items.inject(0) { acc, x -> acc + x }
                   | * assert sum == 6
                   | * assert "${sum}" == "6"
                   | * }
                   | */
                   |public class SomeClass { }'''.stripMargin())
        assert test != null
        test.newInstance().testAssertionFromSomeClassLine2()
    }

    @Test
    void testSnippetTagWithoutGroovyTestCaseIdIsIgnored() {
        Class test = builder.buildTest("SomeClass.java",
                '''/**
                   | * {@snippet lang="groovy" :
                   | * assert false
                   | * }
                   | */
                   |public class SomeClass { }'''.stripMargin())
        // No groovyTestCase marker — snippet is a plain example, not a test.
        assert test == null
    }

    @Test
    void testLineNumbering() {
        Class test = builder.buildTest("SomeClass.java", '''
            /** <pre class="groovyTestCase"> assert true </pre>
             * Nothing on line 3
             * Two on this line <pre class="groovyTestCase"> assert true </pre> <pre class="groovyTestCase"> assert true </pre>
             */
            public class SomeClass { }
        ''')
        assert test.methods.findAll { it.name =~ /test.*/ }.size() == 3
        test.newInstance().testAssertionFromSomeClassLine2()
        test.newInstance().testAssertionFromSomeClassLine4a()
        test.newInstance().testAssertionFromSomeClassLine4b()
    }

    @Test
    void testNoTestBuiltWhenThereAreNoAssertions() {
        Class test = builder.buildTest("SomeClass.java", "/** .. */ public class SomeClass { }")
        assert test == null
    }

    @Test
    void testAssertionsMaySpanMultipleLines() {
        Class test = builder.buildTest("SomeClass.java", '''
            /** <pre class="groovyTestCase"> assert(
             * true)
             * assert true </pre>
             */
            public class SomeClass { }
        ''')
        test.newInstance().testAssertionFromSomeClassLine2()
    }

    @Test
    void testTagMustBeInsideJavadoc() {
        Class test = builder.buildTest("SomeClass.java", '''
            /** <pre class="groovyTestCase"> assert true </pre>
             */
            public class SomeClass {
                 public static void main(String[] args) {
                    // <pre class="groovyTestCase"> assert false </pre>
                 }
            }
        ''')
        assert test.methods.findAll { it.name =~ /test.*/ }.size() == 1
        test.newInstance().testAssertionFromSomeClassLine2()
    }

    @Test
    void testClassNamesMayBeReusedAcrossPackages() {
        Class package1Test = builder.buildTest("./test/com/someplace/package1/SomeClass.java", '''
            /** <pre class="groovyTestCase"> assert true </pre>
             */
            public class SomeClass { }
        ''')
        Class package2Test = builder.buildTest("./test/com/someplace/package2/SomeClass.java", '''
            /** <pre class="groovyTestCase"> assert true </pre>
             */
            public class SomeClass { }
        ''')
        assert package1Test.simpleName == "SomeClassJavadocAssertionTest"
        assert package2Test.simpleName == "SomeClassJavadocAssertionTest"
        assert package1Test != package2Test
    }

    @Test
    void testClassNeedNotBeAPreTag() {
        Class test = builder.buildTest("./test/com/someplace/package1/SomeClass.java", '''
            /** <code class="groovyTestCase"> assert true </code>
             */
            public class SomeClass { }
        ''')
        test.newInstance().testAssertionFromSomeClassLine2()
    }

    @Test
    void testAssertionSyntaxErrorsReportedAtTestTime() {
        Class test = builder.buildTest("SomeClass.java", '''
            /** <pre class="groovyTestCase"> assert #@$@ </pre> */
            public class SomeClass { }
        ''')
        shouldFail(MultipleCompilationErrorsException) {
            test.newInstance().testAssertionFromSomeClassLine2()
        }
    }

    // Stage A — /// Markdown doc comment with the existing HTML-wrapper convention.
    @Test
    void testTripleSlashCommentWithHtmlWrapper() {
        Class test = builder.buildTest("SomeClass.groovy", '''
            /// <pre class="groovyTestCase"> assert 1 + 1 == 2 </pre>
            class SomeClass { }
        ''')
        assert test != null
        assert test.methods.findAll { it.name =~ /test.*/ }.size() == 1
        test.newInstance().testAssertionFromSomeClassLine2()
    }

    // Stage A — /// Markdown doc comment spanning multiple lines with a HTML wrapper.
    @Test
    void testTripleSlashCommentMultilineHtmlWrapper() {
        Class test = builder.buildTest("SomeClass.groovy", '''
            /// <pre class="groovyTestCase">
            /// assert 2 + 2 == 4
            /// assert "x".size() == 1
            /// </pre>
            class SomeClass { }
        ''')
        assert test != null
        test.newInstance().testAssertionFromSomeClassLine2()
    }

    // Stage B — fenced Markdown code block inside a /// run. The infostring
    // `groovy groovyTestCase` uses the first word as the language (keeps syntax
    // highlighting working) and the second as the test marker.
    @Test
    void testFencedCodeBlockAssertion() {
        Class test = builder.buildTest("SomeClass.groovy", '''
            /// Some intro prose.
            ///
            /// ```groovy groovyTestCase
            /// assert 2 + 2 == 4
            /// ```
            ///
            /// More prose.
            class SomeClass { }
        ''')
        assert test != null
        assert test.methods.findAll { it.name =~ /test.*/ }.size() == 1
        test.newInstance().testAssertionFromSomeClassLine4()
    }

    // Stage B — fenced block assertion that fails still fails.
    @Test
    void testFencedCodeBlockFailingAssertion() {
        Class test = builder.buildTest("SomeClass.groovy", '''
            /// ```groovy groovyTestCase
            /// assert false
            /// ```
            class SomeClass { }
        ''')
        shouldFail(AssertionError) {
            test.newInstance().testAssertionFromSomeClassLine2()
        }
    }

    // Stage B — a fenced block whose infostring is just `groovy` (no
    // groovyTestCase marker) must NOT be turned into a test.
    @Test
    void testFencedCodeBlockWithoutMarkerIgnored() {
        Class test = builder.buildTest("SomeClass.groovy", '''
            /// ```groovy
            /// assert false
            /// ```
            class SomeClass { }
        ''')
        assert test == null
    }

    @Test
    void testDecodesCommonHtml() {
        Class test = builder.buildTest("SomeClass.java", '''
            /** <pre class="groovyTestCase"> assert 3 &lt; 5
             * assert """&nbsp;&gt;&lt;&quot;&amp;&apos;&at;&ndash;""" == """ ><"&'@-"""</pre> */
            public class SomeClass { }
        ''')
        test.newInstance().testAssertionFromSomeClassLine2();
    }
}
