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
package org.codehaus.groovy.control.customizers

import groovy.transform.TimedInterrupt
import groovy.util.logging.Log
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.junit.Test

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import static groovy.test.GroovyAssert.shouldFail
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX

/**
 * Tests the {@link ASTTransformationCustomizer}.
 */
final class ASTTransformationCustomizerTest {

    private final CompilerConfiguration config = new CompilerConfiguration()
    private final GroovyShell shell = new GroovyShell(config)

    @Test
    void testLocalTransformation() {
        def customizer = new ASTTransformationCustomizer(Log)
        config.addCompilationCustomizers(customizer)
        def result = shell.evaluate '''
            class MyClass {}
            new MyClass()
        '''
        assert result.log.class == java.util.logging.Logger
    }

    @Test
    void testLocalTransformationClassLoader() {
        def loader = new URLClassLoader() {
            @Override
            Class<?> loadClass(String name) {
            }
        }
        shouldFail(ClassNotFoundException) {
            new ASTTransformationCustomizer(Log, loader)
        }
    }

    @Test
    void testLocalTransformationStringParameter() {
        def customizer = new ASTTransformationCustomizer(Log)
        customizer.annotationParameters = [value: 'logger']
        config.addCompilationCustomizers(customizer)
        def result = shell.evaluate '''
            class MyClass {}
            new MyClass()
        '''
        assert result.logger.class == java.util.logging.Logger
    }

    @Test
    void testLocalTransformationUnknownParameter() {
        def customizer = new ASTTransformationCustomizer(Log)
        shouldFail(IllegalArgumentException) {
            customizer.annotationParameters = [invalid: 'logger']
        }
    }

    @Test
    void testLocalTransformationListOfClassParameter() {
        def customizer = new ASTTransformationCustomizer(Newify)
        customizer.annotationParameters = [value: [Integer, Long]]
        config.addCompilationCustomizers(customizer)
        def result = shell.evaluate '''
            Integer(11) + Long(31)
        '''
        assert result == 42
    }

    @Test
    void testLocalTransformationArrayOfClassParameter() {
        def customizer = new ASTTransformationCustomizer(Newify)
        customizer.annotationParameters = [value: new Class[]{Integer, Long}]
        config.addCompilationCustomizers(customizer)
        def result = shell.evaluate '''
            Integer(11) + Long(31)
        '''
        assert result == 42
    }

    @Test
    void testLocalTransformationPropertyExpressionParameter() {
        def customizer = new ASTTransformationCustomizer(TimedInterrupt)
        customizer.annotationParameters = [value: 500, unit: propX(classX(TimeUnit),'MILLISECONDS')]
        config.addCompilationCustomizers(customizer)
        assert shell.evaluate('''import java.util.concurrent.TimeoutException
            boolean interrupted = false
            try {
                10.times {
                    sleep 100
                }
            } catch (TimeoutException ignore) {
                interrupted = true
            }
            interrupted
        ''')
    }

    @Test // GROOVY-10654
    void testLocalTransformationEnumerationConstantParameter() {
        def customizer = new ASTTransformationCustomizer(TimedInterrupt)
        customizer.annotationParameters = [value: 500, unit: TimeUnit.MILLISECONDS]
        config.addCompilationCustomizers(customizer)
        assert shell.evaluate('''import java.util.concurrent.TimeoutException
            boolean interrupted = false
            try {
                10.times {
                    sleep 100
                }
            } catch (TimeoutException ignore) {
                interrupted = true
            }
            interrupted
        ''')
    }

    //--------------------------------------------------------------------------

    @Test
    void testGlobalTransformation() {
        TestTransformation transformation = new TestTransformation()
        config.addCompilationCustomizers(new ASTTransformationCustomizer(transformation))
        assert shell.evaluate('true')
        assert transformation.applied
    }

    @Test
    void testGlobalTransformation2() {
        TestTransformation transformation = new TestTransformation()
        config.addCompilationCustomizers(new ASTTransformationCustomizer(transformation))
        assert shell.evaluate('''
            class A {}
            class B {}
            true
        ''')
        assert transformation.applied
    }

    @GroovyASTTransformation(phase=CompilePhase.CONVERSION)
    private static class TestTransformation implements ASTTransformation {

        private final applied = new AtomicBoolean()

        boolean isApplied() { return applied.get() }

        void visit(ASTNode[] nodes, SourceUnit source) {
            if (applied.getAndSet(true)) {
                throw new Exception('Global AST transformation should only be applied once')
            }
        }
    }
}
