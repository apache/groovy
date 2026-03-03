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
package groovy.transform.stc

import groovy.transform.AutoFinal
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo

/**
 * Support class for static type checking test cases.
 */
@AutoFinal @CompileStatic
abstract class StaticTypeCheckingTestCase {

    protected CompilerConfiguration config
    protected GroovyShell shell

    @BeforeEach
    void setUpTestCase() {
        config = new CompilerConfiguration()
        def imports = new ImportCustomizer()
        imports.addImports(
            'groovy.transform.ASTTest',
            'groovy.transform.stc.ClosureParams',
            'org.codehaus.groovy.ast.ClassHelper',
            'org.codehaus.groovy.transform.stc.StaticTypesMarker'
        )
        imports.addStaticStars(
            'org.codehaus.groovy.ast.ClassHelper',
            'org.codehaus.groovy.control.CompilePhase',
            'org.codehaus.groovy.transform.stc.StaticTypesMarker'
        )
        config.addCompilationCustomizers(new ASTTransformationCustomizer(TypeChecked), imports)
        configure()

        shell = new GroovyShell(config)
    }

    protected void configure() {}

    //--------------------------------------------------------------------------

    private String testMethodName
    private int testScriptCounter

    @BeforeEach
    void setUpTestName(TestInfo testInfo) {
        testMethodName = testInfo.getTestMethod().orElseThrow().getName()
    }

    protected final String getTestClassName() {
        "TestScript" + testMethodName + (++testScriptCounter) + ".groovy"
    }

    protected final Object assertScript(String script) {
        shell.evaluate(script, getTestClassName())
    }

    protected final void shouldFailWithMessages(String code, String... messages) {
        boolean success = false
        try {
            shell.evaluate(code, getTestClassName())
        } catch (MultipleCompilationErrorsException mce) {
            success = messages.every { message ->
                mce.errorCollector.errors.any {
                    it instanceof SyntaxErrorMessage && it.cause.message.contains(message)
                }
            }
            if (success && mce.errorCollector.errorCount > messages.length) {
                Assertions.fail("Expected error messages were found, but compiler threw additional errors : $mce")
            }
            if (!success) {
                Assertions.fail("Not all expected error messages were found, compiler threw these errors : $mce")
            }
        }
        if (!success) Assertions.fail("Test passed but should have failed with messages [$messages]")
    }
}
