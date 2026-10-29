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
package org.apache.groovy.groovysh.language

import groovy.transform.CompileStatic
import groovy.transform.Internal
import groovy.transform.PackageScope
import org.apache.groovy.lsp.GroovyLanguageClient
import org.apache.groovy.groovysh.jline.GroovyEngine
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.syntax.Types
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.SignatureInformation
import org.jline.console.CmdDesc
import org.jline.console.CmdLine
import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine
import org.jline.utils.AttributedString

import java.lang.reflect.Modifier
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicReference
import java.util.regex.Pattern

/**
 * Compiler-backed session for groovysh. Speaks to {@code GroovyLanguageServer}
 * with the same LSP messages a remote editor sends. The adapter composes
 * the REPL buffer, binding type declarations, and the current line.
 * Classpath and imports are published when the session is constructed,
 * and again only when that {@code CompilerConfiguration} instance changes.
 * Interactive imports are buffer snippets, not customizer edits.
 * Implementation detail of groovysh, not an embedding API.
 */
@CompileStatic
@Internal
final class GroovyshLanguageSession implements AutoCloseable, Completer {

    /** This session's unsaved buffer. The server does not assign the name. */
    @PackageScope
    static final String BUFFER_URI = 'groovy-buffer:repl'

    private static final Pattern IDENTIFIER = Pattern.compile('[\\p{L}_$][\\p{L}\\p{N}_$]*')
    private static final Set<String> SKIP = ['GROOVYSH_OPTIONS', 'GROOVY_OPTIONS', 'CONSOLE_OPTIONS',
            'ROOT', 'PWD', 'NANORC', '_args'] as Set

    /** Bound for one completion or tail-tip compile. A busy compile is skipped. */
    private static final long QUERY_BUDGET_MS = 2_000L

    private final GroovyEngine groovy
    private final GroovyLanguageClient language
    private final ExecutorService queries = Executors.newSingleThreadExecutor({ Runnable runnable ->
        Thread thread = new Thread(runnable, 'groovysh-language')
        thread.daemon = true
        thread
    })
    private final AtomicReference<Future<?>> inflight = new AtomicReference<>()
    private String namesPreamble
    private Set<String> namesCache
    private CompilerConfiguration boundConfiguration
    private boolean closed

    GroovyshLanguageSession(GroovyEngine groovy) {
        this.groovy = groovy
        this.language = GroovyLanguageClient.open(BUFFER_URI, '', true)
        bindEngine()
    }

    /**
     * Compiles the session buffer plus {@code line} and returns diagnostics.
     *
     * @param line current input
     * @return hits
     */
    @PackageScope
    List<Diagnostic> diagnosticsFor(String line) {
        bindEngine()
        language.compile(compose(line ?: ''))
    }

    /**
     * Completions for the current line at end-of-buffer.
     *
     * @param line current input
     * @return candidates
     */
    @PackageScope
    List<CompletionItem> completeLine(String line) {
        completeLine(line, -1)
    }

    /**
     * Completions for {@code line} at {@code cursor} (0-based index into
     * {@code line}). A negative cursor means end of the composed script.
     *
     * @param line current input
     * @param cursor offset in {@code line}
     * @return candidates
     */
    @PackageScope
    List<CompletionItem> completeLine(String line, int cursor) {
        bindEngine()
        String current = line ?: ''
        String script = compose(current)
        int[] caret = caretInComposed(script, current, cursor)
        language.complete(script, caret[0], caret[1])
    }

    /**
     * Tail-tip description. {@code SYNTAX} reports compiler diagnostics and
     * does not evaluate. {@code METHOD} asks for signatures on the full line
     * (so an unclosed {@code (} is visible) and falls back to Inspector only
     * when the compiler has none.
     *
     * @param line JLine command line
     * @return description, or {@code null}
     */
    CmdDesc scriptDescription(CmdLine line) {
        if (line != null && line.descriptionType == CmdLine.DescriptionType.SYNTAX) {
            def hits = query({ diagnosticsFor(line.line ?: '') } as Callable<List<Diagnostic>>, Collections.<Diagnostic>emptyList())
            if (hits) {
                def desc = new CmdDesc()
                desc.setMainDesc(hits.take(5).collect { new AttributedString(GroovyLanguageClient.diagnosticText(it)) })
                return desc
            }
            return null
        }
        try {
            if (line != null && line.descriptionType == CmdLine.DescriptionType.METHOD) {
                String raw = line.line ?: ''
                String head = line.head ?: ''
                int cursor = Math.min(head.length() + 1, raw.length())
                def desc = describeSignatures(query(
                        { signaturesFor(raw, cursor) } as Callable<List<SignatureInformation>>,
                        Collections.<SignatureInformation>emptyList()))
                if (desc != null) {
                    return desc
                }
            }
        } catch (Exception ignored) {
        }
        try {
            return groovy.scriptDescription(line)
        } catch (Exception ignored) {
            return null
        }
    }

    /**
     * Signatures at the end of {@code line}.
     *
     * @param line current input
     * @return signatures, never {@code null}
     */
    @PackageScope
    List<SignatureInformation> signaturesFor(String line) {
        signaturesFor(line, -1)
    }

    /**
     * Signatures for {@code line} at {@code cursor} (0-based index into
     * {@code line}). A negative cursor means end of the composed script.
     *
     * @param line current input
     * @param cursor offset in {@code line}
     * @return signatures, never {@code null}
     */
    @PackageScope
    List<SignatureInformation> signaturesFor(String line, int cursor) {
        bindEngine()
        String current = line ?: ''
        String script = compose(current)
        int[] caret = caretInComposed(script, current, cursor)
        def help = language.signatures(script, caret[0], caret[1])
        help?.signatures ?: []
    }

    /**
     * {@code compose} always appends {@code current} last, so a cursor into
     * {@code current} maps by prefix length of the composed script.
     */
    private static int[] caretInComposed(String script, String current, int cursor) {
        if (cursor < 0 || cursor > current.length()) {
            return GroovyLanguageClient.positionOf(script, script.length())
        }
        int currentStart = script.length() - current.length()
        return GroovyLanguageClient.positionOf(script, currentStart + cursor)
    }

    /**
     * Builds a METHOD tail-tip from compiler signatures.
     *
     * @param set engine signatures
     * @return description, or {@code null} when there are none
     */
    @PackageScope
    static CmdDesc describeSignatures(List<SignatureInformation> signatures) {
        if (!signatures) {
            return null
        }
        def desc = new CmdDesc()
        desc.setMainDesc(signatures.collect { new AttributedString(it?.label ?: '') })
        desc
    }

    @Override
    void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        if (closed || candidates == null || line == null) {
            return
        }
        String raw = line.line() ?: ''
        if (raw.startsWith('/')) {
            return
        }
        try {
            def items = query({ completeLine(raw, line.cursor()) } as Callable<List<CompletionItem>>,
                    Collections.<CompletionItem>emptyList())
            CompletionLayout.mapEngineCandidates(items, line, candidates, CompletionLayout.terminalWidth(reader))
        } catch (RuntimeException ignored) {
        }
    }

    /**
     * Runs one compile unless another is still in flight or it exceeds
     * {@link #QUERY_BUDGET_MS}. Callers that need the result directly,
     * including tests, call {@link #completeLine(String, int)} instead.
     */
    private <T> T query(Callable<T> action, T fallback) {
        Future<?> running = inflight.get()
        if (running != null && !running.isDone()) {
            return fallback
        }
        Future<T> future = queries.submit(action)
        inflight.set(future)
        try {
            return future.get(QUERY_BUDGET_MS, TimeUnit.MILLISECONDS)
        } catch (TimeoutException ignored) {
            // Leave the task running. cancel(true) marks the future done while
            // the worker is still inside the compile, and the interrupt stays
            // set on that thread for the next task.
            return fallback
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt()
            return fallback
        } catch (Exception ignored) {
            return fallback
        }
    }

    @Override
    void close() {
        if (closed) {
            return
        }
        closed = true
        queries.shutdownNow()
        language.close()
    }

    /**
     * Synthetic script: remembered snippets, then binding declarations for
     * names the buffer does not already introduce, then the current line.
     *
     * @param current current input
     * @return script text
     */
    @PackageScope
    String compose(String current) {
        String preamble = groovy.buffer ?: ''
        String decls = bindingDeclarations(namesIn(preamble))
        def parts = []
        if (preamble) {
            parts << preamble
        }
        if (decls) {
            parts << decls
        }
        parts << (current ?: '')
        parts.join('\n')
    }

    private Set<String> namesIn(String preamble) {
        if (!preamble) {
            return [] as Set
        }
        if (preamble == namesPreamble) {
            return namesCache
        }
        bindEngine()
        namesCache = language.scriptNames(preamble) as Set
        namesPreamble = preamble
        namesCache
    }

    private void bindEngine() {
        // Publish only when the instance changes so a repeated completion
        // can reuse the compile. In-place customizer edits are not a
        // groovysh path: the engine configuration is final and set only
        // at construction, and interactive imports are buffer snippets.
        CompilerConfiguration configuration = groovy.compilerConfiguration
        if (configuration != boundConfiguration) {
            boundConfiguration = configuration
            language.parentLoader(groovy.classLoader)
            language.hostConfiguration(configuration)
            // Imports and AST customizers travel on the configuration object.
            // A second string-built ImportCustomizer would also bind an alias
            // under the simple class name.
            language.settings([
                    classpath: GroovyLanguageClient.classpathOf(groovy.classLoader),
                    scriptBaseClass: configuration?.scriptBaseClass ?: ''
            ])
        }
    }

    private String bindingDeclarations(Set<String> already) {
        Map variables
        try {
            def found = groovy.find(null)
            if (!(found instanceof Map)) {
                return ''
            }
            variables = new LinkedHashMap(found)
        } catch (Exception ignored) {
            return ''
        }
        def lines = []
        variables.each { key, value ->
            String name = String.valueOf(key)
            if (SKIP.contains(name) || value == null) {
                return
            }
            if (!IDENTIFIER.matcher(name).matches() || Types.isKeyword(name) || already?.contains(name)) {
                return
            }
            lines << "${typeLiteral(value)} ${name} = null"
        }
        lines.join('\n')
    }

    @PackageScope
    static String typeLiteral(Object value) {
        Class type = value.getClass()
        int rank = 0
        while (type.isArray()) {
            rank++
            type = type.componentType
        }
        if (type.isPrimitive()) {
            return value.getClass().canonicalName
        }
        String base = unresolvable(type) || type.canonicalName == null
                ? 'java.lang.Object'
                : type.canonicalName
        base + ('[]' * rank)
    }

    /**
     * A declaration the language compile cannot resolve. Keywords are
     * rejected by the caller. Anonymous, local, non-public, and script
     * classes (including one the shell has purged) become
     * {@code java.lang.Object}. Array ranks are applied by the caller.
     */
    private static boolean unresolvable(Class type) {
        if (type.isAnonymousClass() || type.isLocalClass() || type.isSynthetic()) {
            return true
        }
        if (!Modifier.isPublic(type.modifiers)) {
            return true
        }
        if (Script.isAssignableFrom(type)) {
            return true
        }
        Class outer = type
        while (outer.enclosingClass != null) {
            outer = outer.enclosingClass
        }
        outer !== type && Script.isAssignableFrom(outer)
    }

}
