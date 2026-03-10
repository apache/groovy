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

import groovy.transform.ConditionalInterrupt
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformationClass
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests the {@link ASTTransformationCustomizer} for cases which rely on AST Builder.
 */
class ASTTransformationCustomizerTest {
    CompilerConfiguration configuration
    ASTTransformationCustomizer customizer

    @BeforeEach
    void setUp() {
        configuration = new CompilerConfiguration()
    }

    @Test
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

    @Test
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

    @Test
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

}

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass("org.codehaus.groovy.control.customizers.ContractAnnotation")
@interface Contract {
    Class value();
}

@GroovyASTTransformation(phase=CompilePhase.CONVERSION)
class ContractAnnotation implements ASTTransformation, Opcodes {
    void visit(ASTNode[] nodes, SourceUnit source) {
        def node = nodes[0]
        def member = node.getMember("value")
        ((ClassNode)nodes[1]).getDeclaredConstructors()[0].code = member.code
    }
}

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@interface Contract2 {
    Class value();
}
