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

import org.apache.groovy.groovysh.jline.GroovyEngine
import org.apache.groovy.lsp.internal.engine.GroovyLanguageEngine
import org.jline.console.CmdDesc
import org.jline.console.CmdLine
import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine
import org.jline.utils.AttributedString

import java.lang.reflect.Modifier
import java.net.URLClassLoader
import java.util.regex.Pattern

/**
 * Optional compiler-backed session for groovysh. Loaded only when groovy-lsp
 * is on the classpath. Does not import LSP4J. The adapter composes the REPL
 * buffer, binding type declarations, and the current line; the engine compiles
 * that synthetic script through semantic analysis.
 */
final class GroovyshLanguageSession implements AutoCloseable, Completer {

    private static final Pattern SYSTEM_VAR = Pattern.compile('[A-Z]+[A-Z_]*')
    private static final Pattern IDENTIFIER = Pattern.compile('[\\p{L}_$][\\p{L}\\p{N}_$]*')
    private static final Set<String> SKIP = ['GROOVYSH_OPTIONS', 'GROOVY_OPTIONS', 'CONSOLE_OPTIONS',
            'ROOT', 'PWD', 'NANORC', '_args'] as Set

    private final GroovyEngine groovy
    private final GroovyLanguageEngine engine
    private int grapeGeneration
    private int loaderStamp

    GroovyshLanguageSession(GroovyEngine groovy) {
        this.groovy = groovy
        this.engine = new GroovyLanguageEngine()
        refreshLoader()
        engine.openBuffer(GroovyLanguageEngine.REPL_URI, 'groovy', '')
    }

    /**
     * Compiles the session buffer plus {@code line} and returns diagnostics.
     *
     * @param line current input
     * @return hits
     */
    List<GroovyLanguageEngine.Hit> diagnosticsFor(String line) {
        refreshLoader()
        engine.updateBuffer(GroovyLanguageEngine.REPL_URI, compose(line ?: ''))
        engine.compileNow()
        engine.diagnostics(GroovyLanguageEngine.REPL_URI)
    }

    /**
     * Completions for the current line at end-of-buffer.
     *
     * @param line current input
     * @return candidates
     */
    List<GroovyLanguageEngine.Candidate> completeLine(String line) {
        refreshLoader()
        String script = compose(line ?: '')
        engine.updateBuffer(GroovyLanguageEngine.REPL_URI, script)
        engine.compileNow()
        int[] caret = endOf(script)
        engine.complete(GroovyLanguageEngine.REPL_URI, caret[0], caret[1])
    }

    /**
     * Tail-tip description: compiler diagnostics for syntax, signatures for
     * an open call, otherwise the existing Inspector path.
     *
     * @param line JLine command line
     * @return description, or {@code null}
     */
    CmdDesc scriptDescription(CmdLine line) {
        try {
            if (line != null && line.descriptionType == CmdLine.DescriptionType.SYNTAX) {
                def hits = diagnosticsFor(line.head ?: '')
                if (hits) {
                    def desc = new CmdDesc()
                    desc.setMainDesc(hits.take(5).collect { new AttributedString(it.message() ?: '') })
                    return desc
                }
            }
            if (line != null && line.descriptionType == CmdLine.DescriptionType.METHOD) {
                def set = signaturesFor(line.head ?: '')
                if (set.signatures()) {
                    def desc = new CmdDesc()
                    desc.setMainDesc(set.signatures().collect { new AttributedString(it.label() ?: '') })
                    return desc
                }
            }
            return groovy.scriptDescription(line)
        } catch (Throwable ignored) {
            return null
        }
    }

    GroovyLanguageEngine.SignatureSet signaturesFor(String line) {
        refreshLoader()
        String script = compose(line ?: '')
        engine.updateBuffer(GroovyLanguageEngine.REPL_URI, script)
        engine.compileNow()
        int[] caret = endOf(script)
        int column = Math.max(caret[1] - 1, 0)
        engine.signatures(GroovyLanguageEngine.REPL_URI, caret[0], column)
    }

    /**
     * Call after {@code /grab} so the next compile does not reuse a stale loader.
     */
    void bumpGrapeGeneration() {
        grapeGeneration++
        engine.putExtra('grapeGeneration', String.valueOf(grapeGeneration))
        engine.setParentLoader(groovy.classLoader)
        loaderStamp = urlStamp(groovy.classLoader)
    }

    @Override
    void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        if (candidates == null || line == null) {
            return
        }
        String raw = line.line() ?: ''
        if (raw.startsWith('/')) {
            return
        }
        try {
            def items = completeLine(raw)
            String word = line.word() ?: ''
            int cursor = Math.min(Math.max(line.wordCursor(), 0), word.length())
            String buffer = word.substring(0, cursor)
            int lastDelim = buffer.lastIndexOf('.')
            String curBuf = lastDelim >= 0 ? buffer.substring(0, lastDelim + 1) : ''
            items.each { item ->
                String insert = plainInsert(item.insertText() ?: item.label())
                if (insert.isEmpty()) {
                    insert = item.label() ?: ''
                }
                if (insert.isEmpty()) {
                    return
                }
                String value = insert.startsWith(curBuf) ? insert : curBuf + insert
                candidates.add(new Candidate(value, item.label() ?: insert, item.kind() ?: 'other',
                        item.detail() ?: null, null, null, false))
            }
        } catch (RuntimeException ignored) {
        }
    }

    @Override
    void close() {
        engine.close()
    }

    /**
     * Synthetic script: remembered snippets, then binding declarations for
     * names the buffer does not already introduce, then the current line.
     *
     * @param current current input
     * @return script text
     */
    String compose(String current) {
        String preamble = groovy.buffer ?: ''
        String decls = bindingDeclarations(preamble)
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

    private void refreshLoader() {
        def loader = groovy.classLoader
        engine.setParentLoader(loader)
        int stamp = urlStamp(loader)
        if (stamp != loaderStamp) {
            loaderStamp = stamp
            engine.putExtra('grapeGeneration', String.valueOf(stamp))
        }
    }

    static int urlStamp(ClassLoader loader) {
        if (loader instanceof URLClassLoader) {
            int hash = 1
            loader.URLs.each { url ->
                String form = url == null ? '' : url.toExternalForm()
                hash = 31 * hash + form.hashCode()
            }
            return hash
        }
        System.identityHashCode(loader)
    }

    private String bindingDeclarations(String preamble) {
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
        Set<String> already = declaredNames(preamble)
        def lines = []
        variables.each { key, value ->
            String name = String.valueOf(key)
            if (SKIP.contains(name) || SYSTEM_VAR.matcher(name).matches() || value == null) {
                return
            }
            if (!IDENTIFIER.matcher(name).matches() || already.contains(name)) {
                return
            }
            lines << "${typeLiteral(value)} ${name} = null"
        }
        lines.join('\n')
    }

    static Set<String> declaredNames(String buffer) {
        def names = [] as Set
        if (!buffer) {
            return names
        }
        buffer.eachLine { String line ->
            def assignment = line =~ /^\s*(?:@[\p{L}_][\p{L}\p{N}_.]*(?:\([^)]*\))?\s+)*(?:[\p{L}_][\p{L}\p{N}_.\$]*(?:\s+[\p{L}_][\p{L}\p{N}_.\$]*)*\s+)?(\p{L}[\p{L}\p{N}_\$]*)\s*=/
            if (assignment.find()) {
                names << assignment.group(1)
            }
            def method = line =~ /^\s*(?:def|void|int|long|boolean|[\p{L}_][\p{L}\p{N}_.]*)\s+(\p{L}[\p{L}\p{N}_\$]*)\s*\(/
            if (method.find()) {
                names << method.group(1)
            }
        }
        names
    }

    static String typeLiteral(Object value) {
        Class type = value.getClass()
        if (type.isArray()) {
            return type.canonicalName
        }
        if (type.isAnonymousClass() || type.isLocalClass() || type.isSynthetic()) {
            return 'java.lang.Object'
        }
        String name = type.name
        if (name.indexOf('$') >= 0 || !Modifier.isPublic(type.modifiers)) {
            return 'java.lang.Object'
        }
        name
    }

    static int[] endOf(String script) {
        if (script == null) {
            return [0, 0] as int[]
        }
        String[] lines = script.split('\n', -1)
        if (lines.length == 0) {
            return [0, 0] as int[]
        }
        int row = lines.length - 1
        [row, lines[row].length()] as int[]
    }

    static String plainInsert(String insert) {
        if (insert == null || insert.isEmpty()) {
            return ''
        }
        String flattened = insert.replaceAll(/\$\{\d+:([^}]*)\}/, '$1').replaceAll(/\$\d+/, '')
        int nl = flattened.indexOf('\n')
        nl < 0 ? flattened : flattened.substring(0, nl)
    }
}
