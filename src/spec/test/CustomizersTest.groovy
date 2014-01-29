import groovy.transform.ConditionalInterrupt
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import groovy.util.logging.Log

/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class CustomizersTest extends GroovyTestCase {

    private CompilerConfiguration config;
    private GroovyShell shell;

    void setUp() {
        config = new CompilerConfiguration()
        shell = new GroovyShell(config)
    }

    @Override
    protected void assertScript(final String script) throws Exception {
        shell.evaluate(script, getTestClassName())
    }

    void testImportCustomizer() {

        // tag::import_cz[]
        def icz = new ImportCustomizer()
        // "normal" import
        icz.addImports('java.util.concurrent.atomic.AtomicInteger', 'java.util.concurrent.ConcurrentHashMap')
        // "aliases" import
        icz.addImport('CHM', 'java.util.concurrent.ConcurrentHashMap')
        // "static" import
        icz.addStaticImport('java.lang.Math', 'PI') // import static java.lang.Math.Pi
        // "aliased static" import
        icz.addStaticImport('pi', 'java.lang.Math', 'PI') // import static java.lang.Math.PI as pi
        // "star" import
        icz.addStarImports 'java.util.concurrent' // import java.util.concurrent.*
        // "static star" import
        icz.addStaticStars 'java.lang.Math' // import static java.lang.Math.*
        // end::import_cz[]

        config.addCompilationCustomizers(icz)

        assertScript '''
            def a = new AtomicInteger(123)
            def map = new CHM([:])
            assert PI == Math.PI
            assert pi == PI
            def c = {} as Callable
            assert cos(0) == 1
        '''
    }

    void testLogCustomizer() {
        // tag::ast_cz_simple[]
        def acz = new ASTTransformationCustomizer(Log)
        config.addCompilationCustomizers(acz)
        // end::ast_cz_simple[]

        assertScript '''
            log.info "It works!"
        '''
    }

    void testLogCustomizerWithCustomName() {
        // tag::ast_cz_customname[]
        def acz = new ASTTransformationCustomizer(Log, value: 'LOGGER') // use name 'LOGGER' instead of the default 'log'
        config.addCompilationCustomizers(acz)
        // end::ast_cz_customname[]

        assertScript '''
            LOGGER.info "It works!"
        '''
    }

    void testAstTransformationCustomizerWithClosureExpression() {
        // tag::ast_cz_closure[]
        def configuration = new CompilerConfiguration()
        def expression = new AstBuilder().buildFromCode(CompilePhase.CONVERSION) {-> true }.expression[0]
        def customizer = new ASTTransformationCustomizer(ConditionalInterrupt, value:expression, thrown:SecurityException)
        configuration.addCompilationCustomizers(customizer)
        def shell = new GroovyShell(configuration)
        shouldFail(SecurityException) {
            shell.evaluate("""
                // equivalent to adding @ConditionalInterrupt(value={true}, thrown: SecurityException)
                class MyClass {
                    void doIt() { }
                }
                new MyClass().doIt()
            """)
        }
        // end::ast_cz_closure[]
    }
}
