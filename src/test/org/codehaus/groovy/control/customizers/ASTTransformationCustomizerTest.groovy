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

import groovy.test.GroovyTestCase
import groovy.transform.TimedInterrupt
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.control.CompilerConfiguration
import groovy.util.logging.Log

import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.control.CompilePhase
import java.util.concurrent.atomic.AtomicBoolean
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.SourceUnit
import java.lang.annotation.Retention
import java.lang.annotation.Target
import org.codehaus.groovy.transform.GroovyASTTransformationClass
import java.lang.annotation.ElementType
import java.lang.annotation.RetentionPolicy
import org.codehaus.groovy.ast.ClassNode
import org.objectweb.asm.Opcodes
import org.codehaus.groovy.ast.builder.AstBuilder
import groovy.transform.ConditionalInterrupt

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

    void testLocalTransformationAndCustomClassLoader() {
        ClassLoader loader = new URLClassLoader([]as URL[]) {
            @Override
            Class<?> loadClass(String name) {
                null
            }
        }
        shouldFail(ClassNotFoundException) {
            customizer = new ASTTransformationCustomizer(Log, loader)
        }
    }

    void testLocalTransformationWithAnnotationParameter() {
        customizer = new ASTTransformationCustomizer(Log)
        customizer.annotationParameters = [value: 'logger']
        configuration.addCompilationCustomizers(customizer)
        def shell = new GroovyShell(configuration)
        def result = shell.evaluate("""
            class MyClass {}
            new MyClass()
        """)
        assert result.logger.class == Logger
    }

    void testLocalTransformationWithInvalidAnnotationParameter() {
        customizer = new ASTTransformationCustomizer(Log)
        shouldFail(IllegalArgumentException) {
            customizer.annotationParameters = [invalid: 'logger']
        }
    }

    void testLocalTransformationWithClosureAnnotationParameter() {
        // add @Contract({distance = 1 })
        customizer = new ASTTransformationCustomizer(Contract)
        final expression = new AstBuilder().buildFromCode(CompilePhase.CONVERSION) {->
            distance = 1
        }.expression[0]
        customizer.annotationParameters = [value: expression]
        configuration.addCompilationCustomizers(customizer)
        def shell = new GroovyShell(configuration)
        def result = shell.evaluate("""
            class MyClass {
                int distance
                MyClass() {}
            }
            new MyClass()
        """)
        assert result.distance == 1
    }

    void testLocalTransformationWithClosureAnnotationParameter_notAnnotatedAsASTInterface() {
        // add @Contract2({distance = 1 })
        customizer = new ASTTransformationCustomizer(Contract2, "org.codehaus.groovy.control.customizers.ContractAnnotation")
        final expression = new AstBuilder().buildFromCode(CompilePhase.CONVERSION) {->
            distance = 1
        }.expression[0]
        customizer.annotationParameters = [value: expression]
        configuration.addCompilationCustomizers(customizer)
        def shell = new GroovyShell(configuration)
        def result = shell.evaluate("""
            class MyClass {
                int distance
                MyClass() {}
            }
            new MyClass()
        """)
        assert result.distance == 1
    }

    void testLocalTransformationWithClassAnnotationParameter() {
        // add @ConditionalInterrupt(value={ true }, thrown=SecurityException)
        final expression = new AstBuilder().buildFromCode(CompilePhase.CONVERSION) {->
            true
        }.expression[0]
        customizer = new ASTTransformationCustomizer(ConditionalInterrupt, value:expression, thrown:SecurityException)
        configuration.addCompilationCustomizers(customizer)
        def shell = new GroovyShell(configuration)
        shouldFail(SecurityException) {
            shell.evaluate("""
                class MyClass {
                    void doIt() { }
                }
                new MyClass().doIt()
            """)
        }
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

    void testLocalTransformationWithListOfClassAnnotationParameter() {
        customizer = new ASTTransformationCustomizer(Newify, value: [Integer, Long])
        configuration.addCompilationCustomizers(customizer)
        def shell = new GroovyShell(configuration)
        def result = shell.evaluate '''
            Integer(11) + Long(31)
        '''
        assert result == 42
    }

    void testAnyExpressionAsParameterValue() {
        customizer = new ASTTransformationCustomizer(value:100, unit: new PropertyExpression(new ClassExpression(ClassHelper.make(TimeUnit)),'MILLISECONDS'), TimedInterrupt)
        configuration.addCompilationCustomizers(customizer)
        def shell = new GroovyShell(configuration)
        def result = shell.evaluate '''import java.util.concurrent.TimeoutException

boolean interrupted = false
try {
    100.times {
        Thread.sleep(100)
    }
} catch (TimeoutException e) {
    interrupted = true
}

interrupted'''
        assert result
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

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass("org.codehaus.groovy.control.customizers.ContractAnnotation")
protected @interface Contract {
    Class value();
}

@GroovyASTTransformation(phase=CompilePhase.CONVERSION)
protected class ContractAnnotation implements ASTTransformation, Opcodes {
    void visit(ASTNode[] nodes, SourceUnit source) {
        def node = nodes[0]
        def member = node.getMember("value")
        ((ClassNode)nodes[1]).getDeclaredConstructors()[0].code = member.code
    }
}

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
protected @interface Contract2 {
    Class value();
}
