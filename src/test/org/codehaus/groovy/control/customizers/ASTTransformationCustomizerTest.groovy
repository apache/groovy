/*
 * Copyright 2003-2011 the original author or authors.
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

package org.codehaus.groovy.control.customizers

import org.codehaus.groovy.control.CompilerConfiguration
import groovy.util.logging.Log
import java.util.logging.Logger
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.control.CompilePhase
import java.util.concurrent.atomic.AtomicBoolean
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.SourceUnit

/**
 * Tests the {@link ASTTransformationCustomizer}.
 */
class ASTTransformationCustomizerTest extends GroovyTestCase {
    CompilerConfiguration configuration
    ASTTransformationCustomizer customizer

    void setUp() {
        configuration = new CompilerConfiguration()
    }

    void testLocalTransformation() {
        customizer = new ASTTransformationCustomizer(Log)
        configuration.addCompilationCustomizers(customizer)
        def shell = new GroovyShell(configuration)
        def result = shell.evaluate("""
            class MyClass {}
            new MyClass()
        """)
        assert result.log.class == Logger
    }

    void testGlobalTransformation() {
        final TestTransformation transformation = new TestTransformation()
        customizer = new ASTTransformationCustomizer(transformation)
        configuration.addCompilationCustomizers(customizer)
        def shell = new GroovyShell(configuration)
        assert shell.evaluate('true')
        assert transformation.applied.get()
    }

    void testGlobalTransformation2() {
        final TestTransformation transformation = new TestTransformation()
        customizer = new ASTTransformationCustomizer(transformation)
        configuration.addCompilationCustomizers(customizer)
        def shell = new GroovyShell(configuration)
        assert shell.evaluate("""
            class A {}
            class B {}
            true
        """)
        assert transformation.applied.get()
    }

    @GroovyASTTransformation(phase=CompilePhase.CONVERSION)
    private static class TestTransformation implements ASTTransformation {

        private AtomicBoolean applied = new AtomicBoolean(false)

        void visit(ASTNode[] nodes, SourceUnit source) {
            if (applied.getAndSet(true)) {
                throw new Exception("Global AST transformation should only be applied once")
            }
        }
        
    }
}
