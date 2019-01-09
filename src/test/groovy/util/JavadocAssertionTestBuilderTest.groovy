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
package groovy.util

import junit.framework.TestCase
import org.codehaus.groovy.control.MultipleCompilationErrorsException

class JavadocAssertionTestBuilderTest extends GroovyTestCase {
    def builder
    
    void setUp() {
        builder = new JavadocAssertionTestBuilder()
    }
    
    void testBuildsTest() {
        Class test = builder.buildTest("SomeClass.java", '/** <pre class="groovyTestCase"> assert true </pre> */ public class SomeClass { }')
        assert test.newInstance() instanceof TestCase
        assert test.simpleName == "SomeClassJavadocAssertionTest"
        test.newInstance().testAssertionFromSomeClassLine1()
    }
    
    void testAssertionsAreCalled() {
        Class test = builder.buildTest("SomeClass.java", '/** <pre class="groovyTestCase"> assert false </pre> */ public class SomeClass { }')
        shouldFail(AssertionError) {
            test.newInstance().testAssertionFromSomeClassLine1()
        }
    }
    
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
    
    void testNoTestBuiltWhenThereAreNoAssertions() {
        Class test = builder.buildTest("SomeClass.java", "/** .. */ public class SomeClass { }")
        assert test == null
    }
    
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

    void testClassNeedNotBeAPreTag() {
        Class test = builder.buildTest("./test/com/someplace/package1/SomeClass.java", '''
            /** <code class="groovyTestCase"> assert true </code>
             */
            public class SomeClass { }
        ''')
        test.newInstance().testAssertionFromSomeClassLine2()
    }
    
    void testAssertionSyntaxErrorsReportedAtTestTime() {
        Class test = builder.buildTest("SomeClass.java", '''
            /** <pre class="groovyTestCase"> assert #@$@ </pre> */
            public class SomeClass { }
        ''')
        shouldFail(MultipleCompilationErrorsException) {
            test.newInstance().testAssertionFromSomeClassLine2()
        }
    }
    
    void testDecodesCommonHtml() {
        Class test = builder.buildTest("SomeClass.java", '''
            /** <pre class="groovyTestCase"> assert 3 &lt; 5
             * assert """&nbsp;&gt;&lt;&quot;&amp;&apos;&at;&ndash;""" == """ ><"&'@-"""</pre> */
            public class SomeClass { }
        ''')
        test.newInstance().testAssertionFromSomeClassLine2();
    }
}
