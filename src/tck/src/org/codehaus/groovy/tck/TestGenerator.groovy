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
package org.codehaus.groovy.tck

/**
 * This will take a groovy test file and turn it into a Java TestCase
 */
class TestGenerator{
    String generate(realOutputPath, targetDir, srcName,srcText) {
//        System.out.println('single \\\\')
//        System.out.println("double \\\\")
        srcText = srcText.replaceAll('\\\\','\\\\\\\\') // need to escape a slash with slash slash

        def resultWriter = new StringWriter()
        def result = new PrintWriter(resultWriter)

        def fileName = srcName
        def fileStem = fileName.tokenize(".")[0]

        def comments = scrape(srcText," * ",".") // Take the first javadoc sentence, if it exists, for use as method name
        if (comments == null || comments[0] == null) {comments = [""]}
        def behaviourDescription = comments[0].trim()

        if ("" != realOutputPath) {
            def realOutputPackage = ''
            if (File.separator != '\\')
                realOutputPackage = realOutputPath.replaceAll(File.separator,'.')
            else
                realOutputPackage = realOutputPath.replaceAll('\\\\','.')
            result.println("package ${realOutputPackage};")
        }
        result.println("import junit.framework.*;")
        result.println("import org.codehaus.groovy.tck.*;")

        result.println("public class ${fileStem}Test extends TestCase {")

        //methodName = turnSentenceIntoJavaName(behaviourDescription)
        def methodName = ""
        methodName = "test${methodName}"

        // test for the source 'as is'
        printCommonTestMethodStart(result, "${methodName}Pass",srcText)
        result.println('        Object result = helper.evaluate(srcBuffer.toString(),"' + "${methodName}Pass" + '");')
        result.println('        if (result instanceof TestResult) {')
        result.println('            TestResult testResult = (TestResult)result;')
        result.println('            if (testResult.errorCount() > 0) {')
        result.println('                TestFailure firstTestFailure = (TestFailure)testResult.errors().nextElement();')
        result.println('                throw firstTestFailure.thrownException();')
        result.println('            }')
        result.println('            if (testResult.failureCount() > 0) {')
        result.println('                AssertionFailedError firstFailure = (AssertionFailedError)(testResult.failures().nextElement());')
        result.println('                throw firstFailure;')
        result.println('            }')
        result.println('        }')
        result.println("    }")

        // test for each of the '@pass' alternatives
        def passAlternatives = generateAlternatives(srcText,"@pass")

        passAlternatives.eachWithIndex{anAlternative,i ->
            printCommonTestMethodStart(result, "${methodName}Pass${i+1}",anAlternative[0]);
            result.println('        Object result = helper.evaluate(srcBuffer.toString(),"' + "${methodName}Pass${i+1}" + '");')
            result.println('        if (result instanceof TestResult) {')
            result.println('            TestResult testResult = (TestResult)result;')
            result.println('            if (testResult.errorCount() > 0) {')
            result.println('                TestFailure firstTestFailure = (TestFailure)testResult.errors().nextElement();')
            result.println('                throw firstTestFailure.thrownException();')
            result.println('            }')
            result.println('            if (testResult.failureCount() > 0) {')
            result.println('                AssertionFailedError firstFailure = (AssertionFailedError)(testResult.failures().nextElement());')
            result.println('                throw firstFailure;')
            result.println('            }')
            result.println('        }')

            result.println("    }")
        }

        // test for each of the '@fail:parse' alternatives
        def failureToParseAlternatives = generateAlternatives(srcText,"@fail:parse")
        failureToParseAlternatives.eachWithIndex{anAlternative,i ->
            printCommonTestMethodStart(result, "${methodName}FailParse${i+1}",anAlternative[0]);
            result.println("        try {")
            result.println('            helper.parse(srcBuffer.toString(),"' + "${methodName}FailParse${i+1}" + '");')


            result.println('            fail("This line did not fail to parse: ' + anAlternative[1] + '");')
            result.println("        } catch (Exception e) {")
            result.println("            // ignore an exception as that is what we're hoping for in this case.")
            result.println("        }")
            result.println("    }")
        }

        // test for each of the '@fail' alternatives, i.e. without being followed by a colon
        def failureAlternatives = generateAlternatives(srcText,"@fail(?!:)")
        failureAlternatives.eachWithIndex{anAlternative,i ->
            printCommonTestMethodStart(result, "${methodName}Fail${i+1}",anAlternative[0]);
            result.println("        try {")
            result.println('            helper.evaluate(srcBuffer.toString(),"' + "${methodName}Fail${i+1}" + '");')
            result.println('            fail("This line did not fail to evaluate: ' + anAlternative[1] + '");')
            result.println("        } catch (Exception e) {")
            result.println("            // ignore an exception as that is what we're hoping for in this case.")
            result.println("        }")
            result.println("    }")
        }
        result.println('    protected String lineSep = System.lineSeparator();')
        result.println('    protected TestGeneratorHelper helper = new ClassicGroovyTestGeneratorHelper();')
        result.println("}")

        return resultWriter.toString()
    }


    // -- useful stuff

    /**
     * Creates alternative versions of the given source, one for each end of line comment tag e.g.&#160;//@fail
     * will remove the double slash from the start of each of the matching line.
     * e.g. src text of...
     * <pre>
     *     // a = 1 // @fail
     *     // b = 2 // @fail
     * </pre>
     * will return
     * <pre>
     * [ "a = 1 // @fail NLS // b = 2 // @fail",
     *   "// a = 1 // @fail NLS b = 2 // @fail" ]
     * </pre>
     *
     */
    List generateAlternatives(String srcText, String tag) {
        def alternatives = []
        def m = java.util.regex.Pattern.compile("//(.*?//\\s*" + tag + "\\S*)\\s").matcher(srcText)
        while (m.find()) {
            def foundText = m.group(1)
            def uncommentedSrcText = (srcText.substring(0,m.start()) + "  " + srcText.substring(m.start() + 2))
            alternatives << [uncommentedSrcText, foundText.replaceAll('"', '\\\\"')]
        }
        return alternatives
    }


    /**
     * Common setup code for each test method
     */
    void printCommonTestMethodStart(result, fullMethodName,someSrcText) {
        def buffer = new StringReader(someSrcText)

        result.println("    public void ${fullMethodName}() throws Throwable {")
        result.println("        StringBuffer srcBuffer = new StringBuffer();")

        // append each line to the buffer
        buffer.eachLine {line ->
            // escape double quotes
            line = line.replaceAll('"','\\\\"')
            result.println ('        srcBuffer.append("' + line + '").append(lineSep);')
        }
    }

    /**
     * Converts the given sentence into a Java style name like TheQuickBrownFox
     */
    String turnSentenceIntoJavaName(String sentence) {
        //uppercase each word and remove spaces to give camel case
        def tokens = sentence.tokenize(" ,;");
        def methodName = ""
        for (t in tokens) {
            if (t.size() > 1) {
                methodName += ( t[0].toUpperCase() + t[1..<t.size()].toLowerCase() )
            } else if (t.size() == 1) {
                methodName += t[0].toUpperCase()
            }
        }

        //remove non-alphanumeric characters
        methodName = methodName.replaceAll("[^A-Za-z0-9]","")

        return methodName
    }

    /**
     * Fetches a list of all the occurrences of text between a string delimiter.
     */
    List scrape(String txt, String tag) {
        return scrape(txt,tag,tag)
    }

    /**
     * Fetches a list of all the occurrences of text between two string delimiters (tags).
     */
    List scrape(String txt, String openTag, String closeTag) {
        def i = 0; def j = 0; def k = 0;
        def contents = []
        if (txt != null) {
            while (i> -1 && k > -1) {
              i = txt.indexOf(openTag,k)
                if (i > -1) {
                  j = i + openTag.length()
                    if (j > -1) {
                      k = txt.indexOf(closeTag,j)
                        if (k > -1) {
                          contents << txt.substring(j,k)
                        }
                    }
                }
            }
        }
        return contents
    }

}
