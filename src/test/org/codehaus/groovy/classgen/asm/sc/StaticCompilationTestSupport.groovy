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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.AutoFinal
import groovy.transform.SelfType
import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.CheckClassAdapter

import java.security.CodeSource

/**
 * A mixin class which can be used to transform a static type checking test case
 * into a static compilation test case.
 * <p>
 * For each test method, it initializes the property {@code astTrees}, which is
 * available to the developer for additional checks. This property provides the
 * AST and the result of the ASM check class adapter (bytecode) for each class.
 */
@AutoFinal @SelfType(StaticTypeCheckingTestCase)
trait StaticCompilationTestSupport {

    Map<String, Tuple2<ClassNode, String>> astTrees
    CompilationUnit compilationUnit

    void extraSetup() {
        astTrees = new HashMap<>()
        config = new CompilerConfiguration()
        config.addCompilationCustomizers(
                new ImportCustomizer().tap {
                    addImports(
                            'groovy.transform.ASTTest',
                            'groovy.transform.CompileStatic',
                            'groovy.transform.stc.ClosureParams',
                            'org.codehaus.groovy.ast.ClassHelper',
                            'org.codehaus.groovy.transform.stc.StaticTypesMarker')
                    addStaticStars(
                            'groovy.transform.TypeCheckingMode',
                            'org.codehaus.groovy.ast.ClassHelper',
                            'org.codehaus.groovy.control.CompilePhase',
                            'org.codehaus.groovy.transform.stc.StaticTypesMarker')
                },
                new ASTTransformationCustomizer(groovy.transform.CompileStatic),
                new ASTTreeCollector(this)
        )
        configure()

        GroovyClassLoader loader = new CompilationUnitAwareGroovyClassLoader(this.class.classLoader, config, this)
        shell = new GroovyShell(loader, config)
    }

    void tearDown() {
        astTrees = null
        compilationUnit = null
        super.tearDown()
    }

    void assertAndDump(String script) {
        try {
            assertScript(script)
        } finally {
            println astTrees
        }
    }

    static class CompilationUnitAwareGroovyClassLoader extends GroovyClassLoader {
        StaticCompilationTestSupport testCase

        CompilationUnitAwareGroovyClassLoader(
                ClassLoader loader,
                CompilerConfiguration config,
                StaticCompilationTestSupport testCase) {
            super(loader, config)
            this.testCase = testCase
        }

        @Override
        protected CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource source) {
            testCase.compilationUnit = new CompilationUnit(config, source, this)
        }
    }

    static class ASTTreeCollector extends CompilationCustomizer {
        StaticCompilationTestSupport testCase

        ASTTreeCollector(StaticCompilationTestSupport testCase) {
            super(CompilePhase.CLASS_GENERATION)
            this.testCase = testCase
        }

        @Override
        void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
            def unit = testCase.compilationUnit
            if (unit) {
                def groovyClass = unit.classes.find { it.name == classNode.name }
                def stringWriter = new StringWriter()
                try {
                    CheckClassAdapter.verify(new ClassReader(groovyClass.bytes),
                        source.classLoader, true, new PrintWriter(stringWriter))
                } catch (Throwable t)  {
                    t.printStackTrace(new PrintWriter(stringWriter))
                }
                testCase.astTrees[groovyClass.name] = new Tuple2<>(classNode, stringWriter.toString())

                for (innerClass in classNode.innerClasses) { // collect closures, etc.
                    if (context.compileUnit.getGeneratedInnerClass(innerClass.name)) {
                        this.call(source, context, innerClass)
                    }
                }
            }
        }
    }
}
