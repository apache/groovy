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
package org.apache.groovy.lsp.internal.compile

import org.apache.groovy.lsp.internal.diagnostic.DiagnosticConverter
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.syntax.SyntaxException
import org.junit.jupiter.api.Test

final class CompilerSafetyTest {

    @Test
    void astTestClosureDoesNotRunWhenDisabled() {
        System.clearProperty('groovy.asttest.enable')
        def compiler = new GroovyCompiler()
        def settings = CompilerSettings.defaults()
        def uri = URI.create('file:///tmp/AstTestSafety.groovy')
        def src = '''
            import groovy.transform.ASTTest
            import org.codehaus.groovy.control.CompilePhase
            @ASTTest(phase = CompilePhase.SEMANTIC_ANALYSIS, value = { throw new RuntimeException('asttest-executed') })
            class Victim {}
            '''
        def snapshot = compiler.compile(
                [new TextDocument(uri, 'groovy', 1, src)],
                [],
                settings,
                CompilerSafetyTest.classLoader)
        def compiled = snapshot.get(uri)
        assert compiled != null
        assert System.getProperty('groovy.asttest.enable') == null
        assert compiled.errors.every { !it.toString().contains('asttest-executed') }
    }

    @Test
    void diagnosticsAreScopedToOwningFile() {
        def config = new CompilerConfiguration()
        def a = new SourceUnit('file:///tmp/A.groovy', 'class A {', config, null, null)
        def b = new SourceUnit('file:///tmp/B.groovy', 'class B {}', config, null, null)
        def collector = new ErrorCollector(config)
        collector.addErrorAndContinue(new SyntaxErrorMessage(new SyntaxException('bad A', 1, 1), a))
        def docA = new CompiledDocument(URI.create('file:///tmp/A.groovy'), 1, 'class A {', null, a, collector)
        def docB = new CompiledDocument(URI.create('file:///tmp/B.groovy'), 1, 'class B {}', null, b, collector)
        def converter = new DiagnosticConverter()
        assert converter.convert(docA, PositionEncoding.UTF16).size() == 1
        assert converter.convert(docB, PositionEncoding.UTF16).isEmpty()
    }

    @Test
    void compilerReusesThenClosesClassLoader() {
        def compiler = new GroovyCompiler()
        def settings = CompilerSettings.defaults()
        def uri = URI.create('file:///tmp/Loader.groovy')
        def src = 'class Loader {}\n'
        compiler.compile([new TextDocument(uri, 'groovy', 1, src)], [], settings, CompilerSafetyTest.classLoader)
        compiler.compile([new TextDocument(uri, 'groovy', 2, src)], [], settings, CompilerSafetyTest.classLoader)
        compiler.close()
        def again = compiler.compile([new TextDocument(uri, 'groovy', 3, src)], [], settings, CompilerSafetyTest.classLoader)
        assert again.get(uri).module != null
        compiler.close()
    }
}
