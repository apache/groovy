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
package org.apache.groovy.lsp

import groovy.transform.Internal
import groovy.transform.PackageScope
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.eclipse.lsp4j.ClientCapabilities
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializedParams
import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.SemanticTokens
import org.eclipse.lsp4j.SemanticTokensParams
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.SignatureHelp
import org.eclipse.lsp4j.SignatureHelpParams
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient

import java.util.concurrent.CompletableFuture

/**
 * In-process LSP client shared by Groovy Console and groovysh.
 * It calls {@link GroovyLanguageServer} the way a remote editor does:
 * {@code initialize}, {@code textDocument/*} and
 * {@code workspace/executeCommand}. No {@code internal} types.
 * The calls are direct method calls, not JSON on each keystroke.
 */
@Internal
final class GroovyLanguageClient implements LanguageClient, AutoCloseable {

    final GroovyLanguageServer server
    final String uri
    private int version = 1
    private String sent = ''
    private final Map<String, List<Diagnostic>> diagnostics = [:]

    private GroovyLanguageClient(GroovyLanguageServer server, String uri) {
        this.server = server
        this.uri = uri
    }

    /**
     * Opens one untitled buffer. {@code compile} is false for a highlighter
     * that only asks for semantic tokens.
     */
    static GroovyLanguageClient open(String uri, String text, boolean compile) {
        def client = new GroovyLanguageClient(new GroovyLanguageServer(), uri)
        client.server.connect(client)
        def init = new InitializeParams()
        init.capabilities = new ClientCapabilities()
        client.server.initialize(init).get()
        client.settings(compile ? [compile: 'true'] : [compile: 'false'])
        client.server.initialized(new InitializedParams())
        def body = text ?: ''
        client.server.textDocumentService.didOpen(new DidOpenTextDocumentParams(
                new TextDocumentItem(uri, 'groovy', client.version, body)))
        client.sent = body
        client
    }

    void parentLoader(ClassLoader loader) {
        server.workspaceService.executeCommand(new ExecuteCommandParams('groovy.parentLoader', [loader])).get()
    }

    void settings(Map groovy) {
        server.workspaceService.didChangeConfiguration(new DidChangeConfigurationParams([groovy: groovy]))
    }

    void sync(String text) {
        String next = text ?: ''
        if (next == sent) {
            return
        }
        version++
        server.textDocumentService.didChange(new DidChangeTextDocumentParams(
                new VersionedTextDocumentIdentifier(uri, version),
                [new TextDocumentContentChangeEvent(next)]))
        sent = next
    }

    List<Diagnostic> compile(String text) {
        sync(text)
        server.workspaceService.executeCommand(new ExecuteCommandParams('groovy.compile', [uri])).get()
        diagnostics[uri] ?: []
    }

    List<CompletionItem> complete(String text, int line, int character) {
        sync(text)
        completionItems(server.textDocumentService.completion(
                new CompletionParams(new TextDocumentIdentifier(uri), new Position(line, character))).get())
    }

    /**
     * Completion items from a {@code textDocument/completion} result.
     * A missing list, or the {@code CompletionItem[]} form, is empty.
     */
    @PackageScope
    static List<CompletionItem> completionItems(Object result) {
        if (!(result instanceof Either)) {
            return []
        }
        Either either = (Either) result
        if (!either.isRight() || either.getRight() == null || !(either.getRight() instanceof CompletionList)) {
            return []
        }
        List<CompletionItem> items = ((CompletionList) either.getRight()).getItems()
        items == null ? [] : items
    }

    String hover(String text, int line, int character) {
        sync(text)
        hoverText(server.textDocumentService.hover(
                new HoverParams(new TextDocumentIdentifier(uri), new Position(line, character))).get())
    }

    /** Hover markdown from either {@link Hover} contents shape. */
    @PackageScope
    static String hoverText(Object hover) {
        if (!(hover instanceof Hover) || ((Hover) hover).contents == null) {
            return ''
        }
        def contents = ((Hover) hover).contents
        if (contents.isRight()) {
            return contents.getRight()?.value ?: ''
        }
        if (!contents.isLeft() || contents.getLeft() == null) {
            return ''
        }
        contents.getLeft().collect { either ->
            if (either != null && either.isRight()) {
                return either.getRight()?.value ?: ''
            }
            either == null ? '' : (either.getLeft() ?: '')
        }.join('\n')
    }

    SignatureHelp signatures(String text, int line, int character) {
        sync(text)
        server.textDocumentService.signatureHelp(
                new SignatureHelpParams(new TextDocumentIdentifier(uri), new Position(line, character))).get()
    }

    List<Integer> semanticTokens(String text) {
        sync(text)
        tokenData(server.textDocumentService.semanticTokensFull(new SemanticTokensParams(
                new TextDocumentIdentifier(uri))).get())
    }

    /** Token data, or an empty list when the response has none. */
    @PackageScope
    static List<Integer> tokenData(SemanticTokens tokens) {
        def data = tokens?.data
        data == null ? [] : data
    }

    /**
     * Lexer keywords, cached after the first request. Includes primitive
     * names and {@code true}, {@code false}, {@code null}.
     */
    static List<String> keywordList() {
        if (cachedKeywords != null) {
            return cachedKeywords
        }
        def client = open('groovy-buffer:keywords', '', false)
        try {
            cachedKeywords = scriptNameList(client.server.workspaceService.executeCommand(
                    new ExecuteCommandParams('groovy.keywords', [])).get())
        } finally {
            client.close()
        }
        cachedKeywords
    }

    private static volatile List<String> cachedKeywords

    /**
     * Lenient lexer tokens for {@code text}. Each map has {@code name},
     * {@code text}, {@code line}, {@code character} and {@code length}.
     */
    List<Map<String, Object>> tokens(String text) {
        sync(text)
        def rows = server.workspaceService.executeCommand(
                new ExecuteCommandParams('groovy.tokens', [uri])).get()
        rows instanceof List ? rows : []
    }

    /**
     * Concrete syntax tree for {@code text}. The map has {@code name},
     * {@code text} and {@code children}.
     */
    Map<String, Object> cst(String text) {
        sync(text)
        def tree = server.workspaceService.executeCommand(
                new ExecuteCommandParams('groovy.cst', [uri])).get()
        tree instanceof Map ? tree : [name: '', text: '', children: []]
    }

    List<String> scriptNames(String text) {
        sync(text)
        scriptNameList(server.workspaceService.executeCommand(
                new ExecuteCommandParams('groovy.scriptNames', [uri])).get())
    }

    /** Script names from {@code groovy.scriptNames}, or empty when the result is not a list. */
    @PackageScope
    static List<String> scriptNameList(Object names) {
        if (!(names instanceof List)) {
            return []
        }
        names.collect { it == null ? '' : it.toString() }
    }

    /**
     * Diagnostic text. LSP 3.18 sends either a string or markup.
     * Hosts in other packages use this to render tail-tips.
     */
    static String diagnosticText(Diagnostic diagnostic) {
        def message = diagnostic?.message
        if (!(message instanceof Either)) {
            return message == null ? '' : message.toString()
        }
        Either either = (Either) message
        if (either.isRight()) {
            def right = either.getRight()
            return right == null ? '' : (right.value ?: '')
        }
        def left = either.getLeft()
        left == null ? '' : left.toString()
    }

    /**
     * File URLs of a URLClassLoader, in listed order.
     * Other loaders, and non-file URLs, contribute nothing.
     */
    static List<String> classpathOf(ClassLoader loader) {
        if (!(loader instanceof URLClassLoader)) {
            return []
        }
        ((URLClassLoader) loader).URLs.findAll { it != null && it.protocol == 'file' }
                .collect { it.toURI().path }
    }

    /** Comma-separated regular imports from {@code configuration}, or empty. */
    static String importsOf(CompilerConfiguration configuration) {
        namesOf(configuration, 'regular').join(',')
    }

    /** Comma-separated star imports from {@code configuration}, or empty. */
    static String starImportsOf(CompilerConfiguration configuration) {
        namesOf(configuration, 'star').join(',')
    }

    private static List<String> namesOf(CompilerConfiguration configuration, String kind) {
        if (configuration == null) {
            return []
        }
        List<String> names = []
        configuration.compilationCustomizers?.each { customizer ->
            if (customizer instanceof ImportCustomizer) {
                def field = ImportCustomizer.getDeclaredField('imports')
                field.accessible = true
                field.get(customizer).each { imp ->
                    def typeField = imp.getClass().getDeclaredField('type')
                    typeField.accessible = true
                    if (String.valueOf(typeField.get(imp)) != kind) {
                        return
                    }
                    if (kind == 'star') {
                        def starField = imp.getClass().getDeclaredField('star')
                        starField.accessible = true
                        def star = starField.get(imp)
                        if (star != null) {
                            names << star.toString()
                        }
                    } else {
                        def nodeField = imp.getClass().getDeclaredField('classNode')
                        nodeField.accessible = true
                        def node = nodeField.get(imp)
                        if (node instanceof ClassNode) {
                            names << ((ClassNode) node).getName()
                        }
                    }
                }
            }
        }
        names
    }

    /** Diagnostics last published for this buffer, or {@code null} when none were. */
    @PackageScope
    List<Diagnostic> publishedOrNull() {
        diagnostics.containsKey(uri) ? diagnostics[uri] : null
    }

    @Override
    void publishDiagnostics(PublishDiagnosticsParams params) {
        if (params == null) {
            return
        }
        diagnostics[params.uri] = params.diagnostics ?: []
    }

    @Override
    void telemetryEvent(Object object) {
    }

    @Override
    void showMessage(MessageParams messageParams) {
    }

    @Override
    CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
        CompletableFuture.completedFuture(null)
    }

    @Override
    void logMessage(MessageParams message) {
    }

    @Override
    void close() {
        server.textDocumentService.didClose(new DidCloseTextDocumentParams(new TextDocumentIdentifier(uri)))
        server.shutdown().get()
        server.exit()
    }
}
