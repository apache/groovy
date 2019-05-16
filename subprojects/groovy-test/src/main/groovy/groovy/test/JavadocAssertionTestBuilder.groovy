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

import java.util.regex.Pattern

/**
 * <code>JavadocAssertionTestBuilder</code> will dynamically create a test cases from Groovy assertions placed within
 * the Javadoc comments of a source file. Assertions should be placed within an html tag with a <code>class="groovyTestCase"</code>
 * attribute assignment. Example:
 * <pre>&lt;pre class="groovyTestCase"&gt; assert "example".size() == 7 &lt;/pre&gt;</pre>
 * When extracting the code for the test, single-line snippets of code without braces within a {{@code @code} ...}
 * tag will have the javadoc {@code code} tag stripped. Similarly, html entities are converted back when extracting
 * code, so {@code &lt;} and {@code &gt;} will be converted to {@code <} and {@code >}.
 */
class JavadocAssertionTestBuilder {
    // TODO write tests for this classes functionality
    private static final Pattern javadocPattern =
        Pattern.compile( /(?ims)\/\*\*.*?\*\// )
    private static final Pattern assertionPattern =
        Pattern.compile( /(?ims)<([a-z]+)\s+class\s*=\s*['"]groovyTestCase['"]\s*>.*?<\s*\/\s*\1>/ )

    Class buildTest(String filename, String code) {
        Class test = null
        
        List assertionTags = getAssertionTags(code)
        if (assertionTags) {
            String testName = getTestName(filename)

            Map lineNumberToAssertions = getLineNumberToAssertionsMap(code, assertionTags)                            
            List testMethods = getTestMethods(lineNumberToAssertions, filename)
            String testCode = getTestCode(testName, testMethods)

            test = createClass(testCode)
        }

        return test
    }
    
    private List getAssertionTags(String code) {
        List assertions = new ArrayList()

        code.eachMatch(javadocPattern) { javadoc ->
            assertions.addAll(javadoc.findAll(assertionPattern))
        }
        
        return assertions
    }
    
    private String getTestName(String filename) {
        String filenameWithoutPath = new File(filename).name
        String testName = filenameWithoutPath.substring(0, filenameWithoutPath.lastIndexOf(".")) +
            "JavadocAssertionTest"
        
        return testName
    }
    
    private Map getLineNumberToAssertionsMap(String code, List assertionTags) {
        Map lineNumberToAssertions = [:] as LinkedHashMap

        int codeIndex = 0
        assertionTags.each { tag ->
            codeIndex = code.indexOf(tag, codeIndex)
            int lineNumber = code.substring(0, codeIndex).findAll("(?m)^").size()
            codeIndex += tag.size()
            
            String assertion = getAssertion(tag)
            
            lineNumberToAssertions.get(lineNumber, []) << assertion
        }

        return lineNumberToAssertions
    }
    
    private String getAssertion(String tag) {
        String tagInner = tag.substring(tag.indexOf(">")+1, tag.lastIndexOf("<"))
        String htmlAssertion = tagInner.replaceAll("(?m)^\\s*\\*", "")
        String assertion = htmlAssertion
        // TODO improve on this
        [nbsp:' ', gt:'>', lt:'<', quot:'"', apos:"'", at:'@', '#64':'@', ndash:'-', amp:'&'].each { key, value ->
            assertion = assertion.replaceAll("(?i)&$key;", value)
        }
        assertion = assertion.replaceAll(/(?i)\{@code ([^}]*)\}/, '$1')

        return assertion
    }
    
    private List getTestMethods(Map lineNumberToAssertions, String filename) {
        List testMethods = lineNumberToAssertions.collect { lineNumber, assertions ->
            Character differentiator = 'a'
            assertions.collect { assertion ->
                String suffix = (assertions.size() > 1 ? "$lineNumber$differentiator" : lineNumber)
                differentiator++
                getTestMethodCodeForAssertion(suffix, assertion, basename(filename))
            }
        }.flatten()

        return testMethods
    }

    private String basename(String fullPath) {
        def path = new File(fullPath)
        def fullName = path.name
        fullName.substring(0, fullName.lastIndexOf("."))
    }

    private String getTestMethodCodeForAssertion(String suffix, String assertion, String basename) {
        return """
            public void testAssertionFrom${basename}Line$suffix() {
                byte[] bytes = [ ${(assertion.getBytes("UTF-8") as List).join(", ")} ] as byte[]
                Eval.me(new String(bytes, "UTF-8"))
            }
        """
    }
    
    private String getTestCode(String testName, List testMethods) {
        return """
            class $testName extends junit.framework.TestCase {
                """+testMethods.join("\r\n")+"""
            }
        """
    }

    private Class createClass(String testCode) {
        return new GroovyClassLoader().parseClass(testCode)
    }    
}
