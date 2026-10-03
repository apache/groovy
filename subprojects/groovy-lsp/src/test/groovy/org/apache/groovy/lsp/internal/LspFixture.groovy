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
package org.apache.groovy.lsp.internal

import org.apache.groovy.lsp.GroovyLanguageServer
import org.eclipse.lsp4j.ApplyWorkspaceEditParams
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse
import org.eclipse.lsp4j.ClientCapabilities
import org.eclipse.lsp4j.ConfigurationParams
import org.eclipse.lsp4j.DeclarationCapabilities
import org.eclipse.lsp4j.DefinitionCapabilities
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DocumentSymbolCapabilities
import org.eclipse.lsp4j.ImplementationCapabilities
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializedParams
import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.ProgressParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.TextDocumentClientCapabilities
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.TypeDefinitionCapabilities
import org.eclipse.lsp4j.WorkDoneProgressCreateParams
import org.eclipse.lsp4j.WorkspaceClientCapabilities
import org.eclipse.lsp4j.services.LanguageClient

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * In-process language server plus a collecting client, used by feature tests.
 */
class LspFixture implements LanguageClient {

    final GroovyLanguageServer server
    final Map<String, List<Diagnostic>> diagnostics = new ConcurrentHashMap<>()
    final List<MessageParams> messages = []
    final List<ApplyWorkspaceEditParams> appliedEdits = []
    final List<WorkDoneProgressCreateParams> progressCreates = []
    final List<ProgressParams> progressNotifications = []
    boolean failProgress
    boolean failConfiguration
    boolean configurationReturnsNullFuture
    boolean configurationCompletesNull
    boolean configurationHangs
    List<Object> configurationReply
    Map configurationSection

    LspFixture() {
        this(new LanguageServerContext())
    }

    LspFixture(LanguageServerContext context) {
        server = new GroovyLanguageServer(context)
        server.connect(this)
        def params = new InitializeParams()
        params.capabilities = clientCapabilities()
        server.initialize(params).get()
        server.initialized(new InitializedParams())
    }

    private static ClientCapabilities clientCapabilities() {
        def capabilities = new ClientCapabilities()
        def workspace = new WorkspaceClientCapabilities()
        workspace.applyEdit = true
        capabilities.workspace = workspace
        def text = new TextDocumentClientCapabilities()
        text.definition = new DefinitionCapabilities(false, true)
        text.typeDefinition = new TypeDefinitionCapabilities(false, true)
        text.implementation = new ImplementationCapabilities(false, true)
        text.declaration = new DeclarationCapabilities(false, true)
        def symbols = new DocumentSymbolCapabilities()
        symbols.hierarchicalDocumentSymbolSupport = true
        text.documentSymbol = symbols
        capabilities.textDocument = text
        capabilities
    }

    String open(String path, String text) {
        open(path, text, 'groovy')
    }

    String open(String path, String text, String languageId) {
        def uri = new File(path).absoluteFile.toURI().toString()
        def item = new TextDocumentItem(uri, languageId, 1, text)
        server.textDocumentService.didOpen(new DidOpenTextDocumentParams(item))
        server.context.recompile()
        uri
    }

    /**
     * A stable copy of every diagnostic published to this client. Do not
     * {@code flatten()} {@link #diagnostics}{@code .values()} — that is a
     * live {@link ConcurrentHashMap} view.
     */
    List<Diagnostic> publishedDiagnostics() {
        diagnostics.values().collectMany { it ?: [] }
    }

    @Override
    CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
        appliedEdits.add(params)
        CompletableFuture.completedFuture(new ApplyWorkspaceEditResponse(true))
    }

    @Override
    void telemetryEvent(Object object) {
    }

    @Override
    void publishDiagnostics(PublishDiagnosticsParams params) {
        diagnostics.put(params.uri, params.diagnostics ?: [])
    }

    @Override
    void showMessage(MessageParams messageParams) {
        messages.add(messageParams)
    }

    @Override
    CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
        CompletableFuture.completedFuture(null)
    }

    @Override
    void logMessage(MessageParams message) {
        messages.add(message)
    }

    @Override
    CompletableFuture<List<Object>> configuration(ConfigurationParams params) {
        if (failConfiguration) {
            throw new IllegalStateException('no config')
        }
        if (configurationHangs) {
            return new CompletableFuture<List<Object>>()
        }
        if (configurationReturnsNullFuture) {
            return null
        }
        if (configurationCompletesNull) {
            return CompletableFuture.completedFuture(null)
        }
        if (configurationReply != null) {
            return CompletableFuture.completedFuture(configurationReply)
        }
        CompletableFuture.completedFuture([configurationSection])
    }

    @Override
    CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params) {
        if (failProgress) {
            throw new IllegalStateException('progress disabled')
        }
        progressCreates.add(params)
        CompletableFuture.completedFuture(null)
    }

    @Override
    void notifyProgress(ProgressParams params) {
        progressNotifications.add(params)
    }

    void close() {
        server.shutdown().get()
        server.exit()
    }
}
