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
package org.codehaus.groovy.control.customizers.builder

import groovy.mock.interceptor.StubFor
import groovy.transform.ToString
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.transform.ToStringASTTransformation
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import org.codehaus.groovy.control.customizers.SourceAwareCustomizer
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Test cases for {@link CompilerCustomizationBuilder}
 */
class CompilerCustomizationBuilderTest extends GroovyTestCase {
    void testAstCustomizerBuilder() {
        def builder = new CompilerCustomizationBuilder()
        def cz = builder.ast(ToString)
        assert cz instanceof ASTTransformationCustomizer
        assert cz.transformation.class == ToStringASTTransformation

        cz = builder.ast(includeNames:true, ToString)
        assert cz instanceof ASTTransformationCustomizer
        assert cz.transformation.class == ToStringASTTransformation
        assert cz.annotationNode.getMember('includeNames').text == 'true'
    }

    void testSecureASTCustomizerBuilder() {
        def builder = new CompilerCustomizationBuilder()
        def cz = builder.secureAst {
            isPackageAllowed = false
        }
        assert cz instanceof SecureASTCustomizer
        assert cz.isPackageAllowed() == false
        cz = builder.secureAst {
            isPackageAllowed = true
        }
        assert cz.isPackageAllowed() == true

        cz = builder.secureAst(isPackageAllowed: false)
        assert cz.isPackageAllowed() == false
        cz = builder.secureAst(isPackageAllowed: true)
        assert cz.isPackageAllowed() == true

    }

    void testCustomizerArray() {
        def builder = new CompilerCustomizationBuilder()
        def cz = builder.customizers {
            ast(ToString)
            secureAst()
        }
        assert cz instanceof CompilationCustomizer[]
        assert cz.length == 2
        assert cz[0] instanceof ASTTransformationCustomizer
        assert cz[1] instanceof SecureASTCustomizer
    }

    void testImportBuilder() {
        def builder = new CompilerCustomizationBuilder()
        // regular imports using strings
        def cz = builder.imports('java.util.concurrent.atomic.AtomicInteger')
        assert cz instanceof ImportCustomizer
        assert cz.imports.size() == 1
        assert cz.imports[0].classNode.name == 'java.util.concurrent.atomic.AtomicInteger'

        cz = builder.imports('java.util.concurrent.atomic.AtomicInteger', 'java.util.concurrent.atomic.AtomicLong')
        assert cz.imports.size() == 2
        assert cz.imports[0].classNode.name == 'java.util.concurrent.atomic.AtomicInteger'
        assert cz.imports[1].classNode.name == 'java.util.concurrent.atomic.AtomicLong'

        // regular imports using classes (not recommended for classloading, but people like it)
        cz = builder.imports(AtomicInteger)
        assert cz instanceof ImportCustomizer
        assert cz.imports.size() == 1
        assert cz.imports[0].classNode.name == 'java.util.concurrent.atomic.AtomicInteger'

        cz = builder.imports(AtomicInteger, AtomicLong)
        assert cz.imports.size() == 2
        assert cz.imports[0].classNode.name == 'java.util.concurrent.atomic.AtomicInteger'
        assert cz.imports[1].classNode.name == 'java.util.concurrent.atomic.AtomicLong'

        // use alias
        cz = builder.imports {
            alias 'AI', 'java.util.concurrent.atomic.AtomicInteger'
        }
        assert cz.imports.size() == 1
        assert cz.imports[0].classNode.name == 'java.util.concurrent.atomic.AtomicInteger'
        assert cz.imports[0].alias == 'AI'
        cz = builder.imports {
            alias 'AI', 'java.util.concurrent.atomic.AtomicInteger'
            alias 'AL', 'java.util.concurrent.atomic.AtomicLong'
        }
        assert cz.imports.size() == 2
        assert cz.imports[0].classNode.name == 'java.util.concurrent.atomic.AtomicInteger'
        assert cz.imports[0].alias == 'AI'
        assert cz.imports[1].classNode.name == 'java.util.concurrent.atomic.AtomicLong'
        assert cz.imports[1].alias == 'AL'
        cz = builder.imports {
            alias 'AI', 'java.util.concurrent.atomic.AtomicInteger'
            alias 'AL', 'java.util.concurrent.atomic.AtomicLong'
            normal 'java.util.concurrent.atomic.AtomicBoolean'
        }
        assert cz.imports.size() == 3
        assert cz.imports[0].classNode.name == 'java.util.concurrent.atomic.AtomicInteger'
        assert cz.imports[0].alias == 'AI'
        assert cz.imports[1].classNode.name == 'java.util.concurrent.atomic.AtomicLong'
        assert cz.imports[1].alias == 'AL'
        assert cz.imports[2].classNode.name == 'java.util.concurrent.atomic.AtomicBoolean'

        cz = builder.imports {
            alias 'AI', AtomicInteger
            alias 'AL', AtomicLong
        }
        assert cz.imports.size() == 2
        assert cz.imports[0].classNode.name == 'java.util.concurrent.atomic.AtomicInteger'
        assert cz.imports[0].alias == 'AI'
        assert cz.imports[1].classNode.name == 'java.util.concurrent.atomic.AtomicLong'
        assert cz.imports[1].alias == 'AL'

        // star imports
        cz = builder.imports {
            star 'java.util.concurrent.atomic'
        }
        assert cz.imports.size() == 1
        assert cz.imports[0].type.toString() == 'star'
        assert cz.imports[0].star == 'java.util.concurrent.atomic.'

        // static star imports
        cz = builder.imports {
            staticStar 'java.lang.Math'
        }
        assert cz.imports.size() == 1
        assert cz.imports[0].type.toString() == 'staticStar'
        assert cz.imports[0].classNode.name == 'java.lang.Math'

        // static import
        cz = builder.imports {
            staticMember 'java.lang.Math', 'PI'
        }
        assert cz.imports.size() == 1
        assert cz.imports[0].type.toString() == 'staticImport'
        assert cz.imports[0].classNode.name == 'java.lang.Math'
        assert cz.imports[0].field == 'PI'
        cz = builder.imports {
            staticMember 'pi', 'java.lang.Math', 'PI'
        }
        assert cz.imports.size() == 1
        assert cz.imports[0].type.toString() == 'staticImport'
        assert cz.imports[0].classNode.name == 'java.lang.Math'
        assert cz.imports[0].field == 'PI'
        assert cz.imports[0].alias == 'pi'
    }

    void testSourceAwareCustomizerBuilder() {
        def builder = new CompilerCustomizationBuilder()
        def cz = builder.source {
            ast(ToString)
        }
        assert cz instanceof SourceAwareCustomizer
        assert cz.delegate instanceof ASTTransformationCustomizer
        assert cz.phase == cz.delegate.phase
        assert cz.baseNameValidator == null
        assert cz.extensionValidator == null
        assert cz.sourceUnitValidator == null
        assert cz.classValidator == null

        cz = builder.source(extension: 'gx') {
            ast(ToString)
        }
        assert cz instanceof SourceAwareCustomizer
        assert cz.delegate instanceof ASTTransformationCustomizer
        assert cz.phase == cz.delegate.phase
        assert cz.baseNameValidator == null
        assert cz.sourceUnitValidator == null
        assert cz.extensionValidator != null
        assert cz.classValidator == null
        assert cz.extensionValidator.call('gx') == true
        assert cz.extensionValidator.call('foo') == false

        cz = builder.source(extensions: ['gx', 'foo']) {
            ast(ToString)
        }
        assert cz instanceof SourceAwareCustomizer
        assert cz.delegate instanceof ASTTransformationCustomizer
        assert cz.phase == cz.delegate.phase
        assert cz.baseNameValidator == null
        assert cz.sourceUnitValidator == null
        assert cz.extensionValidator != null
        assert cz.classValidator == null
        assert cz.extensionValidator.call('gx') == true
        assert cz.extensionValidator.call('foo') == true

        cz = builder.source(extensionValidator: { it in ['gx', 'foo']}) {
            ast(ToString)
        }
        assert cz instanceof SourceAwareCustomizer
        assert cz.delegate instanceof ASTTransformationCustomizer
        assert cz.phase == cz.delegate.phase
        assert cz.baseNameValidator == null
        assert cz.sourceUnitValidator == null
        assert cz.extensionValidator != null
        assert cz.classValidator == null
        assert cz.extensionValidator.call('gx') == true
        assert cz.extensionValidator.call('foo') == true

        cz = builder.source(basename: 'gx') {
            ast(ToString)
        }
        assert cz instanceof SourceAwareCustomizer
        assert cz.delegate instanceof ASTTransformationCustomizer
        assert cz.phase == cz.delegate.phase
        assert cz.extensionValidator == null
        assert cz.sourceUnitValidator == null
        assert cz.baseNameValidator != null
        assert cz.classValidator == null
        assert cz.baseNameValidator.call('gx') == true
        assert cz.baseNameValidator.call('foo') == false

        cz = builder.source(basenames: ['gx', 'foo']) {
            ast(ToString)
        }
        assert cz instanceof SourceAwareCustomizer
        assert cz.delegate instanceof ASTTransformationCustomizer
        assert cz.phase == cz.delegate.phase
        assert cz.extensionValidator == null
        assert cz.sourceUnitValidator == null
        assert cz.classValidator == null
        assert cz.baseNameValidator != null
        assert cz.baseNameValidator.call('gx') == true
        assert cz.baseNameValidator.call('foo') == true

        cz = builder.source(basenameValidator: { it in ['gx', 'foo'] }) {
            ast(ToString)
        }
        assert cz instanceof SourceAwareCustomizer
        assert cz.delegate instanceof ASTTransformationCustomizer
        assert cz.phase == cz.delegate.phase
        assert cz.extensionValidator == null
        assert cz.sourceUnitValidator == null
        assert cz.baseNameValidator != null
        assert cz.baseNameValidator.call('gx') == true
        assert cz.baseNameValidator.call('foo') == true

        cz = builder.source(unitValidator: { SourceUnit unit -> unit.getName().contains 'foo' }) {
            ast(ToString)
        }
        assert cz instanceof SourceAwareCustomizer
        assert cz.delegate instanceof ASTTransformationCustomizer
        assert cz.phase == cz.delegate.phase
        assert cz.extensionValidator == null
        assert cz.baseNameValidator == null
        assert cz.classValidator == null
        assert cz.sourceUnitValidator != null
        assert cz.sourceUnitValidator.call(new SourceUnit(name:'gx')) == false
        assert cz.sourceUnitValidator.call(new SourceUnit(name:'barfoo')) == true

        cz = builder.source(classValidator: { ClassNode cn -> cn.getName().contains 'Foo' }) {
            ast(ToString)
        }
        def valid = new StubFor(ClassNode)
        def invalid = new StubFor(ClassNode)
        valid.demand.getName { 'ClassWithFooInName' }
        invalid.demand.getName { 'ClassWithBarInName' }

        assert cz instanceof SourceAwareCustomizer
        assert cz.delegate instanceof ASTTransformationCustomizer
        assert cz.phase == cz.delegate.phase
        assert cz.extensionValidator == null
        assert cz.baseNameValidator == null
        assert cz.sourceUnitValidator == null
        assert cz.classValidator != null
        valid.use {
            assert cz.classValidator.call(new ClassNode(String)) == true
        }
        invalid.use {
            assert cz.classValidator.call(new ClassNode(String)) == false
        }

    }

    void testInlinedCustomizerFactory() {
        def builder = new CompilerCustomizationBuilder()
        def foo = 0
        def astNode = null
        def cz = builder.inline(phase: 'CONVERSION') { source, context, classNode ->
            foo = 1
            astNode = classNode
        }
        assert foo == 0
        assert astNode == null
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(cz)
        def shell = new GroovyShell(config)
        shell.evaluate'''void m() { assert true }
            m()'''
        assert foo == 1
        assert astNode.methods.find { it.name == 'm' }
    }

    void testCompilerConfigurationBuilderStyle() {
        def config = new CompilerConfiguration()
        // the "customizers" method is added through a custom metaclass
        CompilerCustomizationBuilder.withConfig(config) {
            ast(ToString)
        }
        def shell = new GroovyShell(config)
        def result = shell.evaluate '''class A { int x }
        new A(x:1).toString()'''
        assert result == 'A(1)'
    }

    void testCompilerConfigurationBuilderWithSecureAstCustomizer() {
        def config = new CompilerConfiguration()
        CompilerCustomizationBuilder.withConfig(config) {
            secureAst {
                allowedImports = []
            }
        }
        assert config.compilationCustomizers.first().allowedImports == []
    }

    // GROOVY-9035
    void testEmptySourceAwareCustomizerBuilder() {
        def builder = new CompilerCustomizationBuilder()
        def cz = builder.source {
            // intentionally empty
        }
        assert cz instanceof SourceAwareCustomizer
    }

    private static class SourceUnit {
        String name
    }
}
