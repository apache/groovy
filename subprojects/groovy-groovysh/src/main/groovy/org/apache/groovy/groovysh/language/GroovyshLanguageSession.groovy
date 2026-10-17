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
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
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
import java.util.regex.Matcher
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

    private static final Pattern SYSTEM_VAR = Pattern.compile('[A-Z]+[A-Z_]*')
    private static final Pattern IDENTIFIER = Pattern.compile('[\\p{L}_$][\\p{L}\\p{N}_$]*')
    private static final Set<String> SKIP = ['GROOVYSH_OPTIONS', 'GROOVY_OPTIONS', 'CONSOLE_OPTIONS',
            'ROOT', 'PWD', 'NANORC', '_args'] as Set

    private final GroovyEngine groovy
    private final GroovyLanguageClient language
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
            try {
                def hits = diagnosticsFor(line.line ?: '')
                if (hits) {
                    def desc = new CmdDesc()
                    desc.setMainDesc(hits.take(5).collect { new AttributedString(GroovyLanguageClient.diagnosticText(it)) })
                    return desc
                }
            } catch (Exception ignored) {
            }
            return null
        }
        try {
            if (line != null && line.descriptionType == CmdLine.DescriptionType.METHOD) {
                String raw = line.line ?: ''
                String head = line.head ?: ''
                int cursor = Math.min(head.length() + 1, raw.length())
                def desc = describeSignatures(signaturesFor(raw, cursor))
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
            return positionOf(script, script.length())
        }
        int currentStart = script.length() - current.length()
        return positionOf(script, currentStart + cursor)
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
            def items = completeLine(raw, line.cursor())
            mapEngineCandidates(items, line, candidates, terminalWidth(reader))
        } catch (RuntimeException ignored) {
        }
    }

    @Override
    void close() {
        if (closed) {
            return
        }
        closed = true
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
            language.settings([
                    classpath: GroovyLanguageClient.classpathOf(groovy.classLoader),
                    imports: GroovyLanguageClient.importsOf(configuration),
                    starImports: GroovyLanguageClient.starImportsOf(configuration),
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
            if (SKIP.contains(name) || SYSTEM_VAR.matcher(name).matches() || value == null) {
                return
            }
            if (!IDENTIFIER.matcher(name).matches() || already?.contains(name)) {
                return
            }
            lines << "${typeLiteral(value)} ${name} = null"
        }
        lines.join('\n')
    }

    @PackageScope
    static String typeLiteral(Object value) {
        Class type = value.getClass()
        if (type.isArray()) {
            return type.canonicalName
        }
        if (type.isAnonymousClass() || type.isLocalClass() || type.isSynthetic()) {
            return 'java.lang.Object'
        }
        if (!Modifier.isPublic(type.modifiers)) {
            return 'java.lang.Object'
        }
        String canonical = type.canonicalName
        canonical != null ? canonical : 'java.lang.Object'
    }

    /** {@code returnType name(Type param, Type name)}. */
    private static final Pattern SIGNATURE = Pattern.compile(/^(.*)\s+(\S+)\((.*)\)\s*$/)
    /** Same gray JLine uses for completion descriptions ({@code fg:bright-black}). */
    private static final String GRAY = '\u001B[90m'
    private static final String RESET = '\u001B[0m'
    /** Minimum columns between the parameter list and the return type. */
    private static final int RETURN_GAP = 2
    /** JLine inserts this many columns between completion columns. */
    private static final int COLUMN_GAP = 3
    /** Preferred column width. Extra columns are used only while each stays at least this wide. */
    private static final int PREFERRED_CELL = 36
    /** Upper bound so a very wide terminal does not become a dense wall of columns. */
    private static final int MAX_COLUMNS = 4
    private static final int DEFAULT_WIDTH = 80

    /**
     * Maps engine candidates onto JLine {@link Candidate} values using the
     * current word as the replacement prefix. The group is the full kind
     * name. A method's name is immediately followed by its parameter list.
     * The return type is right-aligned after that list, in the same gray.
     * Candidates share one cell width so JLine can place several side by side.
     * Selecting a method inserts only the name.
     */
    @PackageScope
    static void mapEngineCandidates(List<CompletionItem> items, ParsedLine line,
                                    List<Candidate> candidates) {
        mapEngineCandidates(items, line, candidates, DEFAULT_WIDTH)
    }

    /**
     * @param width terminal columns available for the list
     */
    @PackageScope
    static void mapEngineCandidates(List<CompletionItem> items, ParsedLine line,
                                    List<Candidate> candidates, int width) {
        if (items == null || line == null || candidates == null) {
            return
        }
        String word = line.word() ?: ''
        int cursor = Math.min(Math.max(line.wordCursor(), 0), word.length())
        String buffer = word.substring(0, cursor)
        int lastDelim = buffer.lastIndexOf('.')
        String curBuf = lastDelim >= 0 ? buffer.substring(0, lastDelim + 1) : ''
        List<Row> rows = []
        for (CompletionItem item : items) {
            if (item == null) {
                continue
            }
            String insert = insertedText(item)
            if (insert.isEmpty()) {
                continue
            }
            String value = insert.startsWith(curBuf) ? insert : curBuf + insert
            String label = item.label ?: insert
            rows.add(new Row(value, label, category(item.kind), parametersOf(item.detail), returnTypeOf(item.detail)))
        }
        int cell = cellWidth(width, columnCount(width))
        for (Row row : rows) {
            candidates.add(new Candidate(row.value, displayOf(row, cell), row.group, null, null, null, false))
        }
    }

    /**
     * How many candidates fit on one line. A column is added only while it
     * stays at least {@link #PREFERRED_CELL} wide, and never more than
     * {@link #MAX_COLUMNS}.
     */
    @PackageScope
    static int columnCount(int width) {
        int columns = 1
        int limit = Math.min(MAX_COLUMNS, Math.max(width, 1))
        for (int n = 1; n <= limit; n++) {
            if (cellWidth(width, n) >= PREFERRED_CELL) {
                columns = n
            }
        }
        columns
    }

    /**
     * Visible width of one candidate so that {@code columns} of them, plus
     * JLine's gap between columns, fit on {@code width} without wrapping.
     */
    @PackageScope
    static int cellWidth(int width, int columns) {
        int safe = width > 0 ? width : DEFAULT_WIDTH
        int n = columns < 1 ? 1 : columns
        int available = safe - (n - 1) * COLUMN_GAP - 1
        if (available < n) {
            return 1
        }
        available.intdiv(n)
    }

    private static int terminalWidth(LineReader reader) {
        if (reader == null || reader.terminal == null) {
            return DEFAULT_WIDTH
        }
        int width = reader.terminal.width
        width > 0 ? width : DEFAULT_WIDTH
    }

    /**
     * {@code name(Type param)} in gray immediately after the name, then a gap,
     * then the return type in the same gray. Every signature row is exactly
     * {@code cell} columns wide, so the return types share a right edge and
     * several candidates can sit on one line. A signature that does not fit
     * is cut with {@code ...} before the return type.
     */
    private static String displayOf(Row row, int cell) {
        if (row.parameters == null) {
            return row.label
        }
        String params = '(' + row.parameters + ')'
        String ret = row.returns ?: ''
        int room = ret ? cell - RETURN_GAP - ret.length() : cell
        if (room < 1) {
            room = 1
        }
        String left = fit(row.label + params, room)
        int paren = left.indexOf('(')
        int gap = cell - left.length() - ret.length()
        if (gap < 0) {
            gap = 0
        }
        StringBuilder shown = new StringBuilder()
        if (paren >= 0) {
            shown.append(left, 0, paren)
            shown.append(GRAY).append(left.substring(paren)).append(RESET)
        } else {
            shown.append(left)
        }
        shown.append(spaces(gap))
        if (ret) {
            shown.append(GRAY).append(ret).append(RESET)
        }
        shown.toString()
    }

    /** Cuts {@code text} to {@code room} columns, keeping a {@code ...} tail when it cuts. */
    private static String fit(String text, int room) {
        if (text == null || room <= 0) {
            return ''
        }
        if (text.length() <= room) {
            return text
        }
        if (room <= 3) {
            return text.substring(0, room)
        }
        text.substring(0, room - 3) + '...'
    }

    private static String spaces(int count) {
        if (count <= 0) {
            return ''
        }
        char[] pad = new char[count]
        Arrays.fill(pad, (char) ' ')
        new String(pad)
    }

    /** Display text with color sequences removed, for tests. */
    @PackageScope
    static String visible(String display) {
        if (!display) {
            return ''
        }
        display.replaceAll(/\u001B\[[0-9;]*m/, '')
    }

    private static final class Row {
        final String value
        final String label
        final String group
        final String parameters
        final String returns

        private Row(String value, String label, String group, String parameters, String returns) {
            this.value = value
            this.label = label
            this.group = group
            this.parameters = parameters
            this.returns = returns
        }
    }

    /**
     * Text written into the buffer. A method contributes its name.
     * Parameter placeholders stay in the list and are not inserted.
     */
    @PackageScope
    static String insertedText(CompletionItem item) {
        if (item == null) {
            return ''
        }
        if (item.kind == CompletionItemKind.Method || item.kind == CompletionItemKind.Function) {
            return item.label ?: ''
        }
        String insert = flattenSnippetLine(item.insertText ?: item.label)
        if (insert.isEmpty()) {
            insert = item.label ?: ''
        }
        insert
    }

    /**
     * Full kind name used as the completion group. Static methods are
     * {@code Method}. An unknown kind is {@code Other}.
     */
    @PackageScope
    static String category(CompletionItemKind kind) {
        if (kind == null) {
            return 'Other'
        }
        if (kind == CompletionItemKind.Function || kind == CompletionItemKind.Method) {
            return 'Method'
        }
        kind.name()
    }

    /**
     * Parameter types and names from a method signature, or {@code null}
     * when {@code detail} is not a signature. An empty string is a method
     * with no parameters.
     */
    @PackageScope
    static String parametersOf(String detail) {
        Matcher matcher = signatureOf(detail)
        matcher == null ? null : matcher.group(3)
    }

    /**
     * Return type from a method signature, or {@code null} when
     * {@code detail} is not a signature.
     */
    @PackageScope
    static String returnTypeOf(String detail) {
        Matcher matcher = signatureOf(detail)
        if (matcher == null) {
            return null
        }
        String type = matcher.group(1).trim()
        type.isEmpty() ? null : type
    }

    private static Matcher signatureOf(String detail) {
        if (!detail) {
            return null
        }
        Matcher matcher = SIGNATURE.matcher(detail)
        matcher.matches() ? matcher : null
    }

    @PackageScope
    static String flattenSnippetLine(String insert) {
        String flattened = flattenSnippet(insert)
        int nl = flattened.indexOf('\n')
        nl < 0 ? flattened : flattened.substring(0, nl)
    }

    @PackageScope
    static String flattenSnippet(String insert) {
        if (!insert) {
            return ''
        }
        insert.replaceAll(/\$\{\d+:([^}]*)}/, '$1').replaceAll(/\$\d+/, '')
    }

    private static int[] positionOf(String text, int offset) {
        if (!text || offset <= 0) {
            return [0, 0] as int[]
        }
        int capped = Math.min(offset, text.length())
        int line = 0
        int character = 0
        for (int i = 0; i < capped; i++) {
            char c = text.charAt(i)
            if (c == '\n' as char) {
                line++
                character = 0
            } else if (c == '\r' as char) {
                if (i + 1 < text.length() && text.charAt(i + 1) == '\n' as char) {
                    i++
                }
                line++
                character = 0
            } else {
                character++
            }
        }
        [line, character] as int[]
    }
}
