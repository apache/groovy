/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.transform.stc

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import groovy.transform.TypeChecked
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 * Support class for static type checking test cases.
 *
 * @author Cedric Champeau
 */
abstract class StaticTypeCheckingTestCase extends GroovyTestCase {
    protected CompilerConfiguration config
    protected GroovyShell shell

    @Override
    protected void setUp() {
        super.setUp()
        config = new CompilerConfiguration()
        def imports = new ImportCustomizer()
        imports.addImports(
                'groovy.transform.ASTTest', 'org.codehaus.groovy.transform.stc.StaticTypesMarker',
                'org.codehaus.groovy.ast.ClassHelper'
            )
        imports.addStaticStars('org.codehaus.groovy.control.CompilePhase')
        imports.addStaticStars('org.codehaus.groovy.transform.stc.StaticTypesMarker')
        imports.addStaticStars('org.codehaus.groovy.ast.ClassHelper')
        config.addCompilationCustomizers(new ASTTransformationCustomizer(TypeChecked), imports)
        configure()
        shell = new GroovyShell(config)
    }

    protected void configure() {}

    @Override
    protected void tearDown() {
        super.tearDown()
        shell = null
        config = null
    }

    @Override
    protected void assertScript(String script) {
        shell.evaluate(script, getTestClassName())
    }

    protected Class assertClass(String classCode) {
        GroovyClassLoader loader = new GroovyClassLoader(this.class.classLoader, config)
        loader.parseClass(classCode)
    }

    protected void shouldFailWithMessages(final String code, final String... messages) {
        boolean success = false
        try {
            shell.evaluate(code, getTestClassName())
        } catch (MultipleCompilationErrorsException mce) {
            mce.errorCollector.errors.each {
                messages.each { message ->
                    success = success || (it instanceof SyntaxErrorMessage && it.cause.message.contains(message))
                }
            }
            if (!success) throw mce;
            if (success && mce.errorCollector.errorCount!=messages.length) {
                throw new AssertionError("Expected error message was found, but compiler thrown more than one error : "+mce.toString())
            }
        }
        if (!success) throw new AssertionError("Test should have failed with messages [$messages]")
    }

}
