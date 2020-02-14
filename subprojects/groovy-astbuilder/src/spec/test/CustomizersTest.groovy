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
import groovy.test.GroovyTestCase
import groovy.transform.ConditionalInterrupt
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

class CustomizersTest extends GroovyTestCase {
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
}
