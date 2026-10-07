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
package org.apache.groovy.lsp.internal.diagnostic

import org.codehaus.groovy.control.messages.Diagnostic as GroovyDiagnostic
import org.codehaus.groovy.control.messages.Message
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.jsonrpc.messages.Either

import org.apache.groovy.lsp.internal.compile.CompiledDocument
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.Janitor
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.SimpleMessage
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.control.messages.WarningMessage
import org.codehaus.groovy.syntax.SyntaxException
import org.eclipse.lsp4j.DiagnosticSeverity
import org.junit.jupiter.api.Test

import java.io.PrintWriter
import java.io.Writer

final class DiagnosticConverterTest {

    @Test
    void convertsSyntaxErrorAndWarning() {
        def config = new CompilerConfiguration()
        def source = new SourceUnit('file:///tmp/A.groovy', 'class A {', config, null, null)
        def error = new SyntaxErrorMessage(new SyntaxException('unexpected input', 1, 9), source)
        def warning = new WarningMessage(WarningMessage.LIKELY_ERRORS, 'unused import', null, source)
        def collector = new ErrorCollector(config)
        collector.addErrorAndContinue(error)
        collector.addWarning(warning)
        def compiled = new CompiledDocument(URI.create('file:///tmp/A.groovy'), 1, 'class A {', null, source, collector)
        def diagnostics = new DiagnosticConverter().convert(compiled, PositionEncoding.UTF16)
        assert diagnostics.size() == 2
        assert diagnostics[0].severity == DiagnosticSeverity.Error
        assert diagnostics[0].source == 'groovy'
        assert diagnostics[1].tags
    }

    @Test
    void convertNullDocument() {
        assert new DiagnosticConverter().convert(null, PositionEncoding.UTF16).isEmpty()
        def config = new CompilerConfiguration()
        def source = new SourceUnit('file:///tmp/A.groovy', 'x', config, null, null)
        def message = new SimpleMessage('ok', source)
        assert !DiagnosticConverter.belongsTo(message, null)
        assert !DiagnosticConverter.belongsTo(null, new CompiledDocument(
                URI.create('file:///tmp/A.groovy'), 1, 'x', null, source, null))
    }

    @Test
    void simpleMessageFallsBack() {
        def config = new CompilerConfiguration()
        def source = new SourceUnit('file:///tmp/A.groovy', 'x', config, null, null)
        def message = new SimpleMessage('deprecated API', source)
        def diagnostic = new DiagnosticConverter().toDiagnostic(message, 'x', PositionEncoding.UTF16, DiagnosticSeverity.Warning)
        assert DiagnosticConverter.diagnosticMessage(diagnostic).contains('deprecated')
        assert diagnostic.tags
    }

    @Test
    void janitorNotRequiredForSimple() {
        def config = new CompilerConfiguration()
        def source = new SourceUnit('file:///tmp/A.groovy', 'x', config, null, null)
        def message = new SimpleMessage('ok', source)
        message.write(new PrintWriter(Writer.nullWriter()), new Janitor())
        def diagnostic = new DiagnosticConverter().toDiagnostic(message, '', PositionEncoding.UTF16, DiagnosticSeverity.Error)
        assert diagnostic.range != null
    }
    @Test
    void diagnosticMessagesAndSmallUtilities() {
        def uri = URI.create('file:///tmp/coverage-diag.groovy')
        def config = new CompilerConfiguration()
        def source = new SourceUnit(uri.toString(), 'class A {}', config, null, new ErrorCollector(config))
        def collector = new ErrorCollector(config)
        collector.addErrorAndContinue(new Message() {
            @Override
            void write(PrintWriter writer, Janitor janitor) {
            }

            @Override
            GroovyDiagnostic toDiagnostic() {
                throw new IllegalStateException('no diagnostic')
            }
        })
        int[] calls = [0]
        collector.addErrorAndContinue(new Message() {
            @Override
            void write(PrintWriter writer, Janitor janitor) {
            }

            @Override
            GroovyDiagnostic toDiagnostic() {
                if (calls[0]++ == 0) {
                    return new GroovyDiagnostic(uri.toString(), 1, 1, 'first')
                }
                throw new IllegalStateException('second')
            }
        })
        collector.addErrorAndContinue(new SimpleMessage('orphan', null))
        def document = new CompiledDocument(uri, 4, 'class A {}', null, source, collector)
        assert document.version == 4
        def diagnostics = new DiagnosticConverter().convert(document, PositionEncoding.UTF16)
        assert diagnostics.size() == 1
        assert !DiagnosticConverter.diagnosticMessage(diagnostics[0]).contains('first')
        assert DiagnosticConverter.diagnosticMessage(null) == null
        assert DiagnosticConverter.diagnosticMessage(new Diagnostic()) == null

        def markup = new Diagnostic(new Range(new Position(0, 0), new Position(0, 1)), 'temp')
        markup.setMessage(new MarkupContent('markdown', 'rendered'))
        assert DiagnosticConverter.diagnosticMessage(markup) == 'rendered'
        def ctor = Either.getDeclaredConstructor(Object, Object)
        ctor.accessible = true
        def empty = ctor.newInstance(null, null)
        def neither = new Diagnostic()
        neither.setMessage(empty)
        assert DiagnosticConverter.diagnosticMessage(neither).contains('Either')
    }

}
