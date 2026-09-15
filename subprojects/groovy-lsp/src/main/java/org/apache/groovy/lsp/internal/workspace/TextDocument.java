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
package org.apache.groovy.lsp.internal.workspace;

import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;

import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * An in-memory text document with a version. Incremental LSP changes are
 * applied against the negotiated position encoding.
 */
public record TextDocument(URI uri, String languageId, int version, String text) {

    /**
     * Creates a document snapshot.
     *
     * @param uri document URI
     * @param languageId language id, possibly {@code null}
     * @param version LSP version
     * @param text full text
     */
    public TextDocument {
        Objects.requireNonNull(uri, "uri");
        text = text == null ? "" : text;
    }

    /**
     * Count of leading spaces or tabs on {@code line}.
     *
     * @param line a source line, possibly {@code null}
     * @return the indent width in characters
     */
    public static int leadingWhitespace(final String line) {
        if (line == null || line.isEmpty()) {
            return 0;
        }
        int indent = 0;
        while (indent < line.length() && (line.charAt(indent) == ' ' || line.charAt(indent) == '\t')) {
            indent += 1;
        }
        return indent;
    }

    public URI getUri() {
        return uri;
    }

    public String getLanguageId() {
        return languageId;
    }

    public int getVersion() {
        return version;
    }

    public String getText() {
        return text;
    }

    /**
     * Applies a full or incremental change list, returning a new snapshot.
     *
     * @param newVersion the version after the change
     * @param changes LSP change events
     * @param encoding negotiated encoding
     * @return the updated document
     */
    public TextDocument apply(final int newVersion, final List<TextDocumentContentChangeEvent> changes,
                              final PositionEncoding encoding) {
        String next = text;
        if (changes != null) {
            for (TextDocumentContentChangeEvent change : changes) {
                next = applyOne(next, change, encoding);
            }
        }
        return new TextDocument(uri, languageId, newVersion, next);
    }

    static String applyOne(final String current, final TextDocumentContentChangeEvent change,
                           final PositionEncoding encoding) {
        if (change.getRange() == null) {
            return change.getText() == null ? "" : change.getText();
        }
        int start = offsetOf(current, change.getRange().getStart(), encoding);
        int end = offsetOf(current, change.getRange().getEnd(), encoding);
        if (start < 0) {
            start = 0;
        }
        if (end < start) {
            end = start;
        }
        if (end > current.length()) {
            end = current.length();
        }
        String insert = change.getText() == null ? "" : change.getText();
        return current.substring(0, start) + insert + current.substring(end);
    }

    /**
     * Converts an LSP position to a UTF-16 offset in {@code document}.
     *
     * @param document full text
     * @param position LSP position
     * @param encoding negotiated encoding
     * @return the offset, clamped to {@code [0, document.length()]}
     */
    public static int offsetOf(final String document, final Position position, final PositionEncoding encoding) {
        if (document == null || document.isEmpty() || position == null) {
            return 0;
        }
        int line = Math.max(position.getLine(), 0);
        int currentLine = 0;
        int i = 0;
        while (i < document.length() && currentLine < line) {
            char c = document.charAt(i);
            if (c == '\n') {
                currentLine += 1;
            } else if (c == '\r') {
                if (i + 1 < document.length() && document.charAt(i + 1) == '\n') {
                    i += 1;
                }
                currentLine += 1;
            }
            i += 1;
        }
        if (currentLine < line) {
            return document.length();
        }
        String lineText = Positions.lineText(document, line + 1);
        int column = Positions.characterToGroovyColumn(lineText, position.getCharacter(), encoding);
        int codePoints = Math.max(column - 1, 0);
        int available = lineText.codePointCount(0, lineText.length());
        if (codePoints > available) {
            codePoints = available;
        }
        return i + lineText.offsetByCodePoints(0, codePoints);
    }

    /**
     * Returns the 1-based Groovy line/column of an LSP position.
     *
     * @param position LSP position
     * @param encoding negotiated encoding
     * @return {@code int[]{line, column}}
     */
    public int[] toGroovy(final Position position, final PositionEncoding encoding) {
        int line = position.getLine() + 1;
        int column = Positions.toGroovyColumn(position, Positions.lineText(text, line), encoding);
        return new int[]{line, column};
    }

    /**
     * Returns an LSP range covering the whole document.
     *
     * @param encoding negotiated encoding
     * @return the full range
     */
    public Range fullRange(final PositionEncoding encoding) {
        if (text.isEmpty()) {
            return new Range(new Position(0, 0), new Position(0, 0));
        }
        int lastNl = 1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n' || (c == '\r' && (i + 1 >= text.length() || text.charAt(i + 1) != '\n'))) {
                lastNl += 1;
            }
        }
        if (!text.isEmpty() && (text.charAt(text.length() - 1) == '\n' || text.charAt(text.length() - 1) == '\r')) {
            return new Range(new Position(0, 0), new Position(lastNl - 1, 0));
        }
        String lastLine = Positions.lineText(text, lastNl);
        return new Range(new Position(0, 0),
                Positions.toLsp(lastNl, lastLine.codePointCount(0, lastLine.length()) + 1, lastLine, encoding));
    }
}
