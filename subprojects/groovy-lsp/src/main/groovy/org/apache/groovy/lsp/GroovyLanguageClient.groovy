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

import groovy.transform.CompileStatic
import groovy.transform.Internal
import groovy.transform.PackageScope
import org.apache.groovy.lsp.internal.engine.GroovyLanguageEngine
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
import org.eclipse.lsp4j.MarkedString
import org.eclipse.lsp4j.MarkupContent
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
@CompileStatic
final class GroovyLanguageClient implements LanguageClient, AutoCloseable {

    final GroovyLanguageServer server
    final String uri
    /** Serializes buffer version, sent text, and published diagnostics. */
    private final Object exchange = new Object()
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

    /**
     * Publishes the host {@link CompilerConfiguration}, including static
     * imports, aliases and AST customizers. {@code null} clears it.
     */
    void hostConfiguration(CompilerConfiguration configuration) {
        server.workspaceService.executeCommand(
                new ExecuteCommandParams('groovy.hostConfiguration', [configuration])).get()
    }

    void settings(Map groovy) {
        server.workspaceService.didChangeConfiguration(new DidChangeConfigurationParams([groovy: groovy]))
    }

    /**
     * Publishes {@code text} when it differs from the last buffer sent.
     * The lock covers only the version and the text. It is not held across
     * a compile: the compiler thread publishes diagnostics back onto this
     * client, and holding the lock there deadlocks.
     */
    void sync(String text) {
        synchronized (exchange) {
            String next = text ?: ''
            if (next == sent) {
                return
            }
            version++
            sent = next
            server.textDocumentService.didChange(new DidChangeTextDocumentParams(
                    new VersionedTextDocumentIdentifier(uri, version),
                    [new TextDocumentContentChangeEvent(next)]))
        }
    }

    List<Diagnostic> compile(String text) {
        sync(text)
        server.workspaceService.executeCommand(new ExecuteCommandParams('groovy.compile', [uri])).get()
        synchronized (exchange) {
            def found = diagnostics[uri]
            return found == null ? [] : found.toList()
        }
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
        if (!(hover instanceof Hover)) {
            return ''
        }
        Either contents = ((Hover) hover).contents
        if (contents == null) {
            return ''
        }
        if (contents.isRight()) {
            return textOf(contents.getRight())
        }
        Object left = contents.getLeft()
        if (!(left instanceof List)) {
            return ''
        }
        StringBuilder builder = new StringBuilder()
        boolean first = true
        for (Object row : (List) left) {
            if (!first) {
                builder.append('\n')
            }
            first = false
            builder.append(hoverPart(row))
        }
        return builder.toString()
    }

    private static String hoverPart(Object row) {
        if (!(row instanceof Either)) {
            return ''
        }
        Either either = (Either) row
        if (either.isRight()) {
            return textOf(either.getRight())
        }
        Object left = either.getLeft()
        return left == null ? '' : left.toString()
    }

    /** {@link MarkupContent} and {@link MarkedString} both expose the text as {@code value}. */
    private static String textOf(Object value) {
        String text = null
        if (value instanceof MarkupContent) {
            text = ((MarkupContent) value).value
        } else if (value instanceof MarkedString) {
            text = ((MarkedString) value).value
        }
        return text ? text : ''
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
        Object rows = server.workspaceService.executeCommand(
                new ExecuteCommandParams('groovy.tokens', [uri])).get()
        if (!(rows instanceof List)) {
            return []
        }
        return (List<Map<String, Object>>) rows
    }

    /**
     * Concrete syntax tree for {@code text}. The map has {@code name},
     * {@code text} and {@code children}.
     */
    Map<String, Object> cst(String text) {
        sync(text)
        Object tree = server.workspaceService.executeCommand(
                new ExecuteCommandParams('groovy.cst', [uri])).get()
        if (tree instanceof Map) {
            return (Map<String, Object>) tree
        }
        Map<String, Object> empty = new LinkedHashMap<>()
        empty.put('name', '')
        empty.put('text', '')
        empty.put('children', [])
        return empty
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
        if (diagnostic == null) {
            return ''
        }
        Either<String, MarkupContent> message = diagnostic.message
        if (message == null) {
            return ''
        }
        if (message.isRight()) {
            MarkupContent right = message.getRight()
            return right == null || right.value == null ? '' : right.value
        }
        String left = message.getLeft()
        return left == null ? '' : left
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
        collect(configuration) { ImportCustomizer customizer -> customizer.importClassNames }.join(',')
    }

    /** Comma-separated star imports from {@code configuration}, or empty. */
    static String starImportsOf(CompilerConfiguration configuration) {
        collect(configuration) { ImportCustomizer customizer -> customizer.starImports }.join(',')
    }

    /** Comma-separated static imports from {@code configuration}, or empty. */
    static String staticImportsOf(CompilerConfiguration configuration) {
        collect(configuration) { ImportCustomizer customizer -> customizer.staticImports }.join(',')
    }

    /** Comma-separated static-star class names from {@code configuration}, or empty. */
    static String staticStarsOf(CompilerConfiguration configuration) {
        collect(configuration) { ImportCustomizer customizer -> customizer.staticStarImports }.join(',')
    }

    /** Comma-separated {@code alias=class} entries, or empty. */
    static String aliasesOf(CompilerConfiguration configuration) {
        if (configuration == null) {
            return ''
        }
        def pairs = []
        configuration.compilationCustomizers?.each { customizer ->
            if (customizer instanceof ImportCustomizer) {
                customizer.aliases.each { alias, type -> pairs << "${alias}=${type}" }
            }
        }
        pairs.join(',')
    }

    private static List<String> collect(CompilerConfiguration configuration, Closure<List<String>> read) {
        if (configuration == null) {
            return []
        }
        List<String> names = []
        configuration.compilationCustomizers?.each { customizer ->
            if (customizer instanceof ImportCustomizer) {
                def found = read.call((ImportCustomizer) customizer)
                if (found != null) {
                    names.addAll(found)
                }
            }
        }
        names
    }

    /**
     * Parameter list and return type carried on a method completion.
     * {@code parameters} is the text inside the parentheses. An empty
     * string is a method with no parameters. Absent when the item is
     * not a method signature.
     */
    static MethodDetail methodDetail(CompletionItem item) {
        Object data = item == null ? null : item.data
        if (!(data instanceof Map)) {
            return null
        }
        Map<?, ?> raw = (Map<?, ?>) data
        if (!raw.containsKey('p') && !raw.containsKey('r')) {
            return null
        }
        Object parameters = raw.get('p')
        Object returns = raw.get('r')
        String parameterText = parameters == null ? '' : parameters.toString()
        String returnText = returns == null ? '' : returns.toString()
        return new MethodDetail(parameterText, returnText)
    }

    /**
     * UTF-16 line and character for {@code offset}. CR, LF, and CRLF break lines.
     *
     * @return {@code [line, character]}
     */
    static int[] positionOf(String text, int offset) {
        GroovyLanguageEngine.positionOf(text, offset)
    }

    /**
     * UTF-16 offset of a 0-based line and character, clamped to that line.
     */
    static int offsetOf(String text, int line, int character) {
        GroovyLanguageEngine.offsetOf(text, line, character)
    }

    /** Snippet placeholders removed. Never {@code null}. */
    static String flattenSnippet(String insert) {
        GroovyLanguageEngine.flattenSnippet(insert)
    }

    /** {@link #flattenSnippet(String)} cut at the first newline. */
    static String flattenSnippetLine(String insert) {
        GroovyLanguageEngine.flattenSnippetLine(insert)
    }

    /**
     * Diagnostic range as UTF-16 offsets. {@code paintLastAtEof} paints the
     * last character of a non-empty document when the range sits at the end.
     * The console passes {@code false} so that character is not marked.
     *
     * @return {@code [start, end]}
     */
    static int[] spanOf(String text, int startLine, int startCharacter, int endLine, int endCharacter,
                        boolean paintLastAtEof) {
        GroovyLanguageEngine.spanOf(text, startLine, startCharacter, endLine, endCharacter, paintLastAtEof)
    }

    /** Parameter text and return type of one method completion. */
    static final class MethodDetail {
        final String parameters
        final String returns

        private MethodDetail(String parameters, String returns) {
            this.parameters = parameters
            this.returns = returns
        }
    }

    /** Diagnostics last published for this buffer, or {@code null} when none were. */
    @PackageScope
    List<Diagnostic> publishedOrNull() {
        synchronized (exchange) {
            if (!diagnostics.containsKey(uri)) {
                return null
            }
            def found = diagnostics[uri]
            return found == null ? null : found.toList()
        }
    }

    @Override
    void publishDiagnostics(PublishDiagnosticsParams params) {
        if (params == null) {
            return
        }
        synchronized (exchange) {
            diagnostics[params.uri] = params.diagnostics ?: []
        }
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
