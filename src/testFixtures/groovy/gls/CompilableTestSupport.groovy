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
package gls

import groovy.test.GroovyAssert
import groovy.transform.AutoFinal
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException

import static org.junit.jupiter.api.Assertions.fail

/**
 * Abstract base class for tests verifying Groovy script compilation and execution behavior.
 * 
 * Provides utility methods for testing whether Groovy scripts compile successfully,
 * execute successfully, or fail as expected. This is particularly useful for language
 * specification tests (gls - Groovy Language Specification) that need to verify compiler
 * behavior and error handling across different scenarios.
 * 
 * Each script is evaluated with a unique name to avoid potential name collisions in
 * test suites with many concurrent tests.
 */
@AutoFinal @CompileStatic
abstract class CompilableTestSupport {

    private static String getTestScriptName() {
        String uuid = UUID.randomUUID()
        new StringBuilder('TestScript')
            .append(uuid,  0,  8)
            .append(uuid,  9, 13)
            .append(uuid, 14, 18)
            .append(uuid, 19, 23)
            .append(uuid, 24, 36)
            .append('.groovy')
    }

    //--------------------------------------------------------------------------

    /**
     * Evaluates a Groovy script and returns the result of its execution.
     * 
     * The script is evaluated in a new GroovyShell context with a dynamically generated
     * unique script name based on a UUID to avoid conflicts in test suites.
     * 
     * @param script the Groovy script code to evaluate
     * @return the result object from evaluating the script
     * @throws Exception if the script contains syntax errors or raises an exception during execution
     */
    protected static Object assertScript(String script) {
        new GroovyShell().evaluate(script,testScriptName)
    }

    /**
     * Executes a Groovy script expecting it to fail at runtime and returns the failure message.
     * 
     * Uses {@link GroovyAssert#shouldFail(String)} to assert that the script execution
     * raises an exception. The test fails if the script executes successfully without error.
     * 
     * @param script the Groovy script code that is expected to fail during execution
     * @return the error message from the caught exception
     * @throws AssertionError if the script executes successfully without throwing an exception
     */
    protected static String shouldFail(String script) {
        GroovyAssert.shouldFail(script).getMessage()
    }

    /**
     * Verifies that a Groovy script compiles successfully.
     * 
     * Attempts to parse and compile the provided script using a GroovyClassLoader.
     * The test passes if compilation succeeds. A CompilationFailedException will be
     * thrown if the script contains syntax errors or other compilation issues.
     * 
     * @param script the Groovy script code to compile
     * @throws CompilationFailedException if the script fails to compile
     */
    protected static void shouldCompile(String script) {
        try (def gcl = new GroovyClassLoader()) {
            gcl.parseClass(script,testScriptName)
        }
    }

    /**
     * Verifies that a Groovy script fails to compile and returns the compilation error message.
     * 
     * Attempts to parse and compile the provided script using a GroovyClassLoader.
     * The test expects the compilation to fail; if it succeeds, an AssertionError is thrown.
     * On compilation failure, the error message from the CompilationFailedException is returned
     * for assertion verification.
     * 
     * @param script the Groovy script code that is expected to fail compilation
     * @return the compilation error message describing why the script failed to compile
     * @throws AssertionError if the script compiles successfully when it should have failed
     */
    protected static String shouldNotCompile(String script) {
        try (def gcl = new GroovyClassLoader()) {
            gcl.parseClass(script,testScriptName)
        } catch (CompilationFailedException ex) {
            return ex.message
        }
        fail('the compilation succeeded but should have failed')
    }
}
