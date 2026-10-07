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

package org.apache.groovy.lsp.internal.feature;

import org.antlr.v4.runtime.Token;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.util.GroovySourceTokens;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.apache.groovy.parser.antlr4.GroovyLexer;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Indentation-only formatting and on-type outdent.
 */
public final class FormattingService {

    public List<TextEdit> format(final TextDocument document, final Range range, final int tabSize,
                                 final boolean insertSpaces, final PositionEncoding encoding) {
        String text = document.getText();
        PositionEncoding enc = encoding == null ? PositionEncoding.UTF16 : encoding;
        boolean full = range == null;
        Range target = full ? document.fullRange(enc) : range;
        String[] lines = text.split("\n", -1);
        int startLine = Math.max(target.getStart().getLine(), 0);
        int endLine = Math.min(target.getEnd().getLine(), lines.length - 1);
        if (endLine < startLine) {
            return List.of();
        }
        String indentUnit = indentUnit(tabSize, insertSpaces);
        List<String> formatted = formatAllLines(lines, insertSpaces, indentUnit, frozenIndentLines(text));
        if (full) {
            String result = String.join("\n", formatted);
            if (!result.endsWith("\n") && !result.isEmpty()) {
                result += "\n";
            }
            if (result.equals(text)) {
                return List.of();
            }
            return List.of(new TextEdit(document.fullRange(enc), result));
        }
        StringBuilder original = new StringBuilder();
        StringBuilder rebuilt = new StringBuilder();
        for (int i = startLine; i <= endLine; i++) {
            if (i > startLine) {
                original.append('\n');
                rebuilt.append('\n');
            }
            original.append(lines[i]);
            rebuilt.append(i < formatted.size() ? formatted.get(i) : lines[i]);
        }
        if (original.toString().equals(rebuilt.toString())) {
            return List.of();
        }
        String last = lines[endLine];
        Position end = Positions.toLsp(endLine + 1, last.codePointCount(0, last.length()) + 1, last, enc);
        return List.of(new TextEdit(new Range(new Position(startLine, 0), end), rebuilt.toString()));
    }

    private static List<String> formatAllLines(final String[] lines, final boolean insertSpaces,
                                               final String indentUnit, final Set<Integer> frozen) {
        int[] indent = {0};
        List<String> formatted = new ArrayList<>(lines.length);
        for (int i = 0; i < lines.length; i++) {
            formatted.add(frozen.contains(i) ? lines[i] : formatLine(lines[i], insertSpaces, indentUnit, indent));
        }
        return formatted;
    }

    private static String formatLine(final String raw, final boolean insertSpaces, final String indentUnit,
                                     final int[] indent) {
        String line = rtrim(raw).replace("\t", insertSpaces ? indentUnit : "\t");
        String stripped = line.stripLeading();
        applyCloser(stripped, indent);
        String rebuilt = stripped.isEmpty() ? "" : indentUnit.repeat(indent[0]) + stripped;
        applyOpener(stripped, indent);
        return rebuilt;
    }

    private static void updateIndent(final String raw, final int[] indent) {
        String stripped = rtrim(raw).stripLeading();
        applyCloser(stripped, indent);
        applyOpener(stripped, indent);
    }

    private static void applyCloser(final String stripped, final int[] indent) {
        if (indentCloser(stripped)) {
            indent[0] = Math.max(indent[0] - 1, 0);
        }
    }

    private static void applyOpener(final String stripped, final int[] indent) {
        if (indentOpener(stripped)) {
            indent[0] += 1;
        }
    }

    private static boolean indentCloser(final String stripped) {
        return stripped.startsWith("}") || stripped.startsWith(")") || stripped.startsWith("]");
    }

    private static boolean indentOpener(final String stripped) {
        return stripped.endsWith("{") || stripped.endsWith("(") && !stripped.contains(")");
    }

    private static String indentUnit(final int tabSize, final boolean insertSpaces) {
        return insertSpaces ? " ".repeat(Math.max(tabSize, 1)) : "\t";
    }

    private static List<TextEdit> replaceLeadingWhitespace(final String line, final int lineNumber, final String pad) {
        int existing = TextDocument.leadingWhitespace(line);
        if (pad.equals(line.substring(0, existing))) {
            return List.of();
        }
        return List.of(new TextEdit(new Range(new Position(lineNumber, 0), new Position(lineNumber, existing)), pad));
    }

    private static List<TextEdit> outdentCloser(final TextDocument document, final Position position,
                                                final int tabSize, final boolean insertSpaces,
                                                final PositionEncoding encoding) {
        String text = document.getText();
        int after = TextDocument.offsetOf(text, position, encoding == null ? PositionEncoding.UTF16 : encoding);
        if (after <= 0 || !GroovySourceTokens.isStructuralRbrace(text, after - 1)) {
            return List.of();
        }
        String[] lines = text.split("\n", -1);
        int line = position.getLine();
        if (line < 0 || line >= lines.length) {
            return List.of();
        }
        String current = lines[line];
        String stripped = current.stripLeading();
        if (!stripped.startsWith("}")) {
            return List.of();
        }
        int level = Math.max(indentLevelBefore(lines, line, frozenIndentLines(text)) - 1, 0);
        return replaceLeadingWhitespace(current, line, indentUnit(tabSize, insertSpaces).repeat(level));
    }

    /**
     * Indents the new line after a newline, matching Metals' on-type formatter.
     *
     * @param document open document
     * @param position caret after the typed character
     * @param typed typed character
     * @param tabSize tab size
     * @param insertSpaces whether to insert spaces
     * @return an indent edit, or empty
     */
    public List<TextEdit> onTypeFormat(final TextDocument document, final Position position, final String typed,
                                       final int tabSize, final boolean insertSpaces) {
        return onTypeFormat(document, position, typed, tabSize, insertSpaces, PositionEncoding.UTF16);
    }

    /**
     * Indents after newline or outdents a structural {@code '}'}'.
     *
     * @param encoding negotiated encoding
     * @return a tiny indent edit, or empty
     */
    public List<TextEdit> onTypeFormat(final TextDocument document, final Position position, final String typed,
                                       final int tabSize, final boolean insertSpaces,
                                       final PositionEncoding encoding) {
        if (document == null || typed == null || position.getLine() < 0) {
            return List.of();
        }
        if ("}".equals(typed)) {
            return outdentCloser(document, position, tabSize, insertSpaces, encoding);
        }
        if (!"\n".equals(typed) || position.getLine() <= 0) {
            return List.of();
        }
        String text = document.getText();
        String[] lines = text.split("\n", -1);
        int line = position.getLine();
        if (line >= lines.length) {
            return List.of();
        }
        Set<Integer> frozen = frozenIndentLines(text);
        if (frozen.contains(line)) {
            return List.of();
        }
        int level = indentLevelBefore(lines, line, frozen);
        return replaceLeadingWhitespace(lines[line], line, indentUnit(tabSize, insertSpaces).repeat(level));
    }

    /**
     * Indent level in effect at {@code line}, after every earlier line.
     * Frozen continuation lines do not open or close a block.
     */
    private static int indentLevelBefore(final String[] lines, final int line, final Set<Integer> frozen) {
        int[] indent = {0};
        int limit = Math.min(Math.max(line, 0), lines.length);
        for (int i = 0; i < limit; i++) {
            if (!frozen.contains(i)) {
                updateIndent(lines[i], indent);
            }
        }
        return indent[0];
    }

    private static Set<Integer> frozenIndentLines(final String text) {
        Set<Integer> frozen = new HashSet<>();
        for (Token token : GroovySourceTokens.tokenize(text)) {
            String body = token.getText();
            if (!multilineStringOrComment(token, body)) {
                continue;
            }
            int startLine = token.getLine() - 1;
            int extra = body.split("\n", -1).length - 1;
            for (int i = 1; i <= extra; i++) {
                frozen.add(startLine + i);
            }
        }
        return frozen;
    }

    private static boolean multilineStringOrComment(final Token token, final String body) {
        if (body == null || !body.contains("\n")) {
            return false;
        }
        int type = token.getType();
        if (type == GroovyLexer.StringLiteral || type == GroovyLexer.GStringBegin
                || type == GroovyLexer.GStringPart || type == GroovyLexer.GStringEnd) {
            return true;
        }
        return type == GroovyLexer.NL && (body.startsWith("//") || body.startsWith("/*"));
    }

    private static String rtrim(final String line) {
        int end = line.length();
        while (end > 0 && Character.isWhitespace(line.charAt(end - 1))) {
            end -= 1;
        }
        return line.substring(0, end);
    }
}
