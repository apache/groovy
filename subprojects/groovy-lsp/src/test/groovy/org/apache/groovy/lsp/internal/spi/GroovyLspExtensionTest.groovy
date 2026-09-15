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
package org.apache.groovy.lsp.internal.spi

import org.apache.groovy.lsp.internal.ExtensionHost
import org.apache.groovy.lsp.internal.LanguageServerContext
import org.apache.groovy.lsp.internal.LspFixture
import org.apache.groovy.lsp.internal.feature.SupportServices
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.protocol.ServerCapabilityFactory
import org.apache.groovy.lsp.spi.GroovyLspExtension
import org.apache.groovy.lsp.spi.GroovyLspSession
import org.codehaus.groovy.control.CompilationUnit
import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.CodeActionContext
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DocumentDiagnosticParams
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

final class GroovyLspExtensionTest {

    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
    }

    @Test
    void afterCompileCommandDiagnosticsAndCodeAction() {
        def plugin = new RecordingExtension()
        fixture = new LspFixture(new LanguageServerContext(ExtensionHost.of(plugin)))
        def uri = fixture.open('Hello.groovy', 'class Hello {}\n')
        assert plugin.compiles >= 1
        assert fixture.publishedDiagnostics().any {
            SupportServices.diagnosticMessage(it) == 'from-plugin'
        }
        def result = fixture.server.workspaceService.executeCommand(
                new ExecuteCommandParams('groovy.lsp.extension.ping', [])).get()
        assert result == 'pong'
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        new Range(new Position(0, 0), new Position(0, 1)),
                        new CodeActionContext([]))).get()
        assert actions.any { it.isRight() && it.getRight().title == 'Plugin action' }
        def caps = new ServerCapabilityFactory().create(PositionEncoding.UTF16, plugin.commands())
        assert 'groovy.lsp.extension.ping' in caps.executeCommandProvider.commands
        assert 'groovy.lsp.organizeImports' in caps.executeCommandProvider.commands
        def pull = fixture.server.textDocumentService.diagnostic(
                new DocumentDiagnosticParams(new TextDocumentIdentifier(uri))).get()
        def items = pull.getLeft().items
        assert items.any { SupportServices.diagnosticMessage(it) == 'from-plugin' }
    }

    @Test
    void throwingPluginDoesNotAbortCompile() {
        def boom = new GroovyLspExtension() {
            void afterCompile(CompilationUnit unit) {
                throw new IllegalStateException('boom')
            }
        }
        fixture = new LspFixture(new LanguageServerContext(ExtensionHost.of(boom)))
        def uri = fixture.open('Hello.groovy', 'class Hello {}\n')
        assert fixture.server.context.snapshot.get(uri).module != null
    }

    @Test
    void unknownCommandStillReturnsNullWithoutPlugins() {
        fixture = new LspFixture()
        assert fixture.server.workspaceService.executeCommand(
                new ExecuteCommandParams('unknown', [])).get() == null
        assert ExtensionHost.none().ids().isEmpty()
        assert ExtensionHost.of().ids().isEmpty()
        assert ExtensionHost.discover(GroovyLspExtensionTest.classLoader).ids().isEmpty()
    }

    private static final class RecordingExtension implements GroovyLspExtension {
        int compiles

        @Override
        String id() {
            'test.ping'
        }

        @Override
        void afterCompile(CompilationUnit unit) {
            compiles++
        }

        @Override
        List<Diagnostic> extraDiagnostics(URI uri, String sourceText) {
            [new Diagnostic(new Range(new Position(0, 0), new Position(0, 1)), 'from-plugin')]
        }

        @Override
        List<Either<Command, CodeAction>> extraCodeActions(URI uri, List<Diagnostic> diagnostics, Range range) {
            [Either.forRight(new CodeAction('Plugin action'))]
        }

        @Override
        List<String> commands() {
            ['groovy.lsp.extension.ping']
        }

        @Override
        Object executeCommand(String command, List<Object> arguments, GroovyLspSession session) {
            'pong'
        }
    }
}
