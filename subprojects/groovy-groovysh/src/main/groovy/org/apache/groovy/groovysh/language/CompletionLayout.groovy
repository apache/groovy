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
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.jline.reader.Candidate
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine

/**
 * Turns language-server completions into one JLine row.
 * A method's parameter list and return type come from
 * {@link GroovyLanguageClient.MethodDetail}, not from parsing {@code detail}.
 * Rows share one cell width so several can sit on a line.
 */
@CompileStatic
@Internal
final class CompletionLayout {

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

    @PackageScope
    static void mapEngineCandidates(List<CompletionItem> items, ParsedLine line, List<Candidate> candidates) {
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
            GroovyLanguageClient.MethodDetail method = GroovyLanguageClient.methodDetail(item)
            rows.add(new Row(value, label, category(item.kind), method?.parameters, method?.returns))
        }
        int cell = cellWidth(width, columnCount(width))
        for (Row row : rows) {
            candidates.add(new Candidate(row.value, displayOf(row, cell), row.group, null, null, null, false))
        }
    }

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

    @PackageScope
    static int terminalWidth(LineReader reader) {
        if (reader == null || reader.terminal == null) {
            return DEFAULT_WIDTH
        }
        int width = reader.terminal.width
        width > 0 ? width : DEFAULT_WIDTH
    }

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

    @PackageScope
    static String fit(String text, int room) {
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

    @PackageScope
    static String visible(String display) {
        if (!display) {
            return ''
        }
        display.replaceAll(/\u001B\[[0-9;]*m/, '')
    }

    @PackageScope
    static String insertedText(CompletionItem item) {
        if (item == null) {
            return ''
        }
        if (item.kind == CompletionItemKind.Method || item.kind == CompletionItemKind.Function) {
            return item.label ?: ''
        }
        String insert = GroovyLanguageClient.flattenSnippetLine(item.insertText ?: item.label)
        if (insert.isEmpty()) {
            insert = item.label ?: ''
        }
        insert
    }

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
}
