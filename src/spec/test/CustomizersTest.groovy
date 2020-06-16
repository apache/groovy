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
import groovy.transform.ConditionalInterrupt
import groovy.util.logging.Log
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.AttributeExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.codehaus.groovy.control.customizers.SourceAwareCustomizer
import org.codehaus.groovy.control.CompilerConfiguration
import static org.codehaus.groovy.control.customizers.builder.CompilerCustomizationBuilder.withConfig

import static org.codehaus.groovy.syntax.Types.*

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
        icz.addStaticImport('java.lang.Math', 'PI') // import static java.lang.Math.PI
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
        def acz = new ASTTransformationCustomizer(Log, value: 'LOGGER')
        // use name 'LOGGER' instead of the default 'log'
        config.addCompilationCustomizers(acz)
        // end::ast_cz_customname[]

        assertScript '''
            LOGGER.info "It works!"
        '''
    }

    void testAstTransformationCustomizerWithClosureExpression() {
        // tag::ast_cz_closure[]
        def configuration = new CompilerConfiguration()
        def expression = new AstBuilder().buildFromCode(CompilePhase.CONVERSION) { -> true }.expression[0]
        def customizer = new ASTTransformationCustomizer(ConditionalInterrupt, value: expression, thrown: SecurityException)
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

    void testSecureASTCustomizer() {
        // tag::secure_cz[]
        def scz = new SecureASTCustomizer()
        scz.with {
            closuresAllowed = false // user will not be able to write closures
            methodDefinitionAllowed = false // user will not be able to define methods
            allowedImports = [] // empty allowed list means imports are disallowed
            allowedStaticImports = [] // same for static imports
            allowedStaticStarImports = ['java.lang.Math'] // only java.lang.Math is allowed
            // the list of tokens the user can find
            // constants are defined in org.codehaus.groovy.syntax.Types
            allowedTokens = [ // <1>
                    PLUS,
                    MINUS,
                    MULTIPLY,
                    DIVIDE,
                    MOD,
                    POWER,
                    PLUS_PLUS,
                    MINUS_MINUS,
                    COMPARE_EQUAL,
                    COMPARE_NOT_EQUAL,
                    COMPARE_LESS_THAN,
                    COMPARE_LESS_THAN_EQUAL,
                    COMPARE_GREATER_THAN,
                    COMPARE_GREATER_THAN_EQUAL,
            ].asImmutable()
            // limit the types of constants that a user can define to number types only
            allowedConstantTypesClasses = [ // <2>
                    Integer,
                    Float,
                    Long,
                    Double,
                    BigDecimal,
                    Integer.TYPE,
                    Long.TYPE,
                    Float.TYPE,
                    Double.TYPE
            ].asImmutable()
            // method calls are only allowed if the receiver is of one of those types
            // be careful, it's not a runtime type!
            allowedReceiversClasses = [ // <2>
                    Math,
                    Integer,
                    Float,
                    Double,
                    Long,
                    BigDecimal
            ].asImmutable()
        }
        // end::secure_cz[]
        config.addCompilationCustomizers(scz)
        assertScript '''
            1+1
        '''
        shouldFail {
            assertScript '''
                println "not allowed"
            '''
        }
    }

    void testSecureASTCustomizerWithCustomChecker() {
        // tag::secure_cz_custom[]
        def scz = new SecureASTCustomizer()
        def checker = { expr ->
            !(expr instanceof AttributeExpression)
        } as SecureASTCustomizer.ExpressionChecker
        scz.addExpressionCheckers(checker)
        // end::secure_cz_custom[]
        config.addCompilationCustomizers(scz)
        shouldFail {
            assertScript '''// tag::secure_cz_custom_assert[]
class A {
    int val
}

def a = new A(val: 123)
a.@val // <1>
// end::secure_cz_custom_assert[]
'''
        }
    }

    void testSourceAwareCustomizer() {
        // tag::source_cz[]
        def delegate = new ImportCustomizer()
        def sac = new SourceAwareCustomizer(delegate)
        // end::source_cz[]

        // tag::source_cz_predicates[]
        // the customizer will only be applied to classes contained in a file name ending with 'Bean'
        sac.baseNameValidator = { baseName ->
            baseName.endsWith 'Bean'
        }

        // the customizer will only be applied to files which extension is '.spec'
        sac.extensionValidator = { ext -> ext == 'spec' }

        // source unit validation
        // allow compilation only if the file contains at most 1 class
        sac.sourceUnitValidator = { SourceUnit sourceUnit -> sourceUnit.AST.classes.size() == 1 }

        // class validation
        // the customizer will only be applied to classes ending with 'Bean'
        sac.classValidator = { ClassNode cn -> cn.endsWith('Bean') }

        // end::source_cz_predicates[]

        config.addCompilationCustomizers(sac)
    }

    void testCustomizerBuilder() {
        // tag::customizer_withconfig[]
        def conf = new CompilerConfiguration()
        withConfig(conf) {
            // ... <2>
        }
        // end::customizer_withconfig[]
    }
}
