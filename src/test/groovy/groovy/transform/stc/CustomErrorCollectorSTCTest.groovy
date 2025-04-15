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
package groovy.transform.stc

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor

/**
 * Unit tests for static type checking : custom error collector.
 */
class CustomErrorCollectorSTCTest extends StaticTypeCheckingTestCase {

    void testShouldNotFail() {
        CompilerConfiguration c = new CompilerConfiguration()
        GroovyShell shell = new GroovyShell(c)
        c.addCompilationCustomizers(new CompilationCustomizer(CompilePhase.INSTRUCTION_SELECTION) {
            @Override
            void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) {
                def visitor = new StaticTypeCheckingVisitor(source, classNode)
                visitor.visitClass(classNode)
            }
        })
        shouldFail(MultipleCompilationErrorsException) {
            shell.evaluate('int x = new Object()')
        }

        c = new CompilerConfiguration()
        shell = new GroovyShell(c)
        c.addCompilationCustomizers(new CompilationCustomizer(CompilePhase.INSTRUCTION_SELECTION) {
            @Override
            void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) {
                def visitor = new StaticTypeCheckingVisitor(source, classNode)
                visitor.typeCheckingContext.pushErrorCollector(new ErrorCollector(c))
                visitor.visitClass(classNode)
            }
        })
        shouldFail(GroovyCastException) {
            shell.evaluate('int x = new Object()')
        }
    }
}

