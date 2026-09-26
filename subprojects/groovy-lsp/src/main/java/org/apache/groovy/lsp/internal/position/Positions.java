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
package org.apache.groovy.lsp.internal.position;

import org.apache.groovy.lsp.internal.compile.Identifiers;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Converts between Groovy AST coordinates (1-based line, 1-based exclusive
 * end column, Unicode code points) and LSP coordinates (0-based line,
 * 0-based character in the negotiated {@link PositionEncoding}).
 */
public final class Positions {

    private Positions() {
    }

    /**
     * Converts a Groovy caret (1-based line and column) to an LSP position.
     *
     * @param groovyLine 1-based line
     * @param groovyColumn 1-based code-point column
     * @param lineText the line text without terminator, or {@code ""} 
     * @param encoding negotiated encoding
     * @return the LSP position
     */
    public static Position toLsp(final int groovyLine, final int groovyColumn, final String lineText,
                                 final PositionEncoding encoding) {
        int line = Math.max(groovyLine, 1) - 1;
        int column = Math.max(groovyColumn, 1);
        return new Position(line, groovyColumnToCharacter(lineText, column, encoding));
    }

    /**
     * Converts an LSP position to a Groovy 1-based code-point column on that line.
     *
     * @param position LSP position
     * @param lineText the line text without terminator
     * @param encoding negotiated encoding
     * @return 1-based Groovy column
     */
    public static int toGroovyColumn(final Position position, final String lineText,
                                     final PositionEncoding encoding) {
        Objects.requireNonNull(position, "position");
        return characterToGroovyColumn(lineText, position.getCharacter(), encoding);
    }

    /**
     * Converts a Groovy AST node span to an LSP range.
     *
     * @param node positioned node
     * @param document document text
     * @param encoding negotiated encoding
     * @return the range, or {@code null} when the node has no source span
     */
    public static Range toRange(final ASTNode node, final String document, final PositionEncoding encoding) {
        if (node == null || node.getLineNumber() <= 0) {
            return null;
        }
        return toRange(node.getLineNumber(), node.getColumnNumber(),
                node.getLastLineNumber(), node.getLastColumnNumber(), document, encoding);
    }

    /**
     * Converts an identifier span to an LSP range by locating {@code node}'s
     * name in the source covering the node. Does not require extra fields
     * on {@code AnnotatedNode}. Returns {@code null} when the name cannot
     * be found so rename cannot rewrite a whole declaration.
     *
     * @param node annotated node
     * @param document document text
     * @param encoding negotiated encoding
     * @return the selection range, or {@code null}
     */
    public static Range toNameRange(final AnnotatedNode node, final String document,
                                    final PositionEncoding encoding) {
        if (node == null) {
            return null;
        }
        return findIdentifier(node, document, encoding);
    }

    /**
     * Selection range for an identifier: the method/property name of a call,
     * the identifier found in an annotated node's source span, otherwise
     * the full node.
     *
     * @param node positioned node
     * @param document document text
     * @param encoding negotiated encoding
     * @return the range, or {@code null}
     */
    public static Range toIdentifierRange(final ASTNode node, final String document,
                                          final PositionEncoding encoding) {
        if (node instanceof MethodCallExpression call && call.getMethod() != null) {
            Range range = toRange(call.getMethod(), document, encoding);
            if (range != null) {
                return range;
            }
        }
        if (node instanceof PropertyExpression property && property.getProperty() != null) {
            Range range = toRange(property.getProperty(), document, encoding);
            if (range != null) {
                return range;
            }
        }
        if (node instanceof AttributeExpression attribute && attribute.getProperty() != null) {
            Range range = toRange(attribute.getProperty(), document, encoding);
            if (range != null) {
                return range;
            }
        }
        if (node instanceof AnnotatedNode annotated) {
            return toNameRange(annotated, document, encoding);
        }
        return toRange(node, document, encoding);
    }

    /**
     * Converts a Groovy span to an LSP range.
     *
     * @param startLine 1-based start line
     * @param startColumn 1-based start column
     * @param endLine 1-based end line
     * @param endColumn exclusive 1-based end column
     * @param document full document text
     * @param encoding negotiated encoding
     * @return the range
     */
    public static Range toRange(final int startLine, final int startColumn, final int endLine, final int endColumn,
                                final String document, final PositionEncoding encoding) {
        int lastLine = endLine > 0 ? endLine : startLine;
        int lastColumn = endColumn > 0 ? endColumn : startColumn;
        String startText = lineText(document, startLine);
        String endText = startLine == lastLine ? startText : lineText(document, lastLine);
        return new Range(
                toLsp(startLine, startColumn, startText, encoding),
                toLsp(lastLine, lastColumn, endText, encoding));
    }

    /**
     * Returns whether {@code (line, column)} (Groovy caret) lies in {@code node}.
     * The end column is exclusive.
     *
     * @param node positioned node
     * @param line 1-based line
     * @param column 1-based column
     * @return {@code true} when the caret is inside the node
     */
    public static boolean contains(final ASTNode node, final int line, final int column) {
        if (node == null || node.getLineNumber() <= 0) {
            return false;
        }
        int lastLine = node.getLastLineNumber() > 0 ? node.getLastLineNumber() : node.getLineNumber();
        int lastColumn = node.getLastColumnNumber() > 0 ? node.getLastColumnNumber() : node.getColumnNumber();
        if (line < node.getLineNumber() || line > lastLine) {
            return false;
        }
        if (line == node.getLineNumber() && column < node.getColumnNumber()) {
            return false;
        }
        return line != lastLine || column < lastColumn;
    }

    /**
     * Returns a ranking for "innermost node": later start, then earlier end.
     * Each coordinate occupies 16 bits so the fields do not overlap.
     *
     * @param node positioned node
     * @return a comparable key; {@code null} nodes rank last
     */
    public static long innerScore(final ASTNode node) {
        if (node == null || node.getLineNumber() <= 0) {
            return Long.MIN_VALUE;
        }
        int lastLine = node.getLastLineNumber() > 0 ? node.getLastLineNumber() : node.getLineNumber();
        int lastColumn = node.getLastColumnNumber() > 0 ? node.getLastColumnNumber() : node.getColumnNumber();
        return (pack16(node.getLineNumber()) << 48)
                | (pack16(node.getColumnNumber()) << 32)
                | ((0xFFFFL - pack16(lastLine)) << 16)
                | (0xFFFFL - pack16(lastColumn));
    }

    private static long pack16(final int value) {
        if (value < 0) {
            return 0L;
        }
        return Math.min(value, 0xFFFF);
    }

    /**
     * Converts a 1-based Groovy column to an LSP character offset on {@code lineText}.
     *
     * @param lineText line without terminator
     * @param groovyColumn 1-based code-point column
     * @param encoding negotiated encoding
     * @return LSP character offset
     */
    public static int groovyColumnToCharacter(final String lineText, final int groovyColumn,
                                       final PositionEncoding encoding) {
        String line = lineText == null ? "" : lineText;
        int codePoints = Math.max(groovyColumn, 1) - 1;
        int total = line.codePointCount(0, line.length());
        if (codePoints > total) {
            codePoints = total;
        }
        return codePointsToCharacter(line, codePoints, encoding);
    }

    /**
     * Converts an LSP character offset on {@code lineText} to a 1-based Groovy column.
     *
     * @param lineText line without terminator
     * @param character LSP character offset
     * @param encoding negotiated encoding
     * @return 1-based Groovy column
     */
    public static int characterToGroovyColumn(final String lineText, final int character,
                                       final PositionEncoding encoding) {
        String line = lineText == null ? "" : lineText;
        int max = characterCount(line, encoding);
        int clamped = character < 0 ? 0 : Math.min(character, max);
        return characterToCodePoints(line, clamped, encoding) + 1;
    }

    static int codePointsToCharacter(final String line, final int codePointOffset,
                                     final PositionEncoding encoding) {
        if (codePointOffset <= 0) {
            return 0;
        }
        int utf16 = line.offsetByCodePoints(0, codePointOffset);
        return switch (encoding == null ? PositionEncoding.UTF16 : encoding) {
            case UTF16 -> utf16;
            case UTF32 -> codePointOffset;
            case UTF8 -> line.substring(0, utf16).getBytes(StandardCharsets.UTF_8).length;
        };
    }

    static int characterToCodePoints(final String line, final int character,
                                     final PositionEncoding encoding) {
        PositionEncoding enc = encoding == null ? PositionEncoding.UTF16 : encoding;
        if (character <= 0) {
            return 0;
        }
        return switch (enc) {
            case UTF32 -> Math.min(character, line.codePointCount(0, line.length()));
            case UTF16 -> {
                int idx = Math.min(character, line.length());
                if (idx > 0 && idx < line.length() && Character.isLowSurrogate(line.charAt(idx))
                        && Character.isHighSurrogate(line.charAt(idx - 1))) {
                    idx -= 1;
                }
                yield line.codePointCount(0, idx);
            }
            case UTF8 -> codePointsForUtf8Prefix(line, character);
        };
    }

    static int characterCount(final String line, final PositionEncoding encoding) {
        PositionEncoding enc = encoding == null ? PositionEncoding.UTF16 : encoding;
        return switch (enc) {
            case UTF16 -> line.length();
            case UTF32 -> line.codePointCount(0, line.length());
            case UTF8 -> line.getBytes(StandardCharsets.UTF_8).length;
        };
    }

    /**
     * First identifier occurrence of {@code node}'s name inside its source
     * span, as a Java identifier (not a substring of a longer name).
     *
     * @param node positioned node
     * @param document document text
     * @param encoding negotiated encoding
     * @return the name range, or {@code null}
     */
    public static Range findIdentifier(final ASTNode node, final String document,
                                       final PositionEncoding encoding) {
        if (node == null || document == null || node.getLineNumber() <= 0) {
            return null;
        }
        String name = Identifiers.nameOf(node);
        if (name == null || name.isEmpty()) {
            return null;
        }
        int startLine = node.getLineNumber();
        int lastLine = node.getLastLineNumber() > 0 ? node.getLastLineNumber() : startLine;
        int startColumn = Math.max(node.getColumnNumber(), 1);
        int lastColumn = node.getLastColumnNumber();
        int nameCps = name.codePointCount(0, name.length());
        for (int line = startLine; line <= lastLine; line++) {
            String text = lineText(document, line);
            int from = line == startLine ? Math.max(startColumn - 1, 0) : 0;
            int[] cps = text.codePoints().toArray();
            int to = cps.length;
            if (line == lastLine && lastColumn > 0) {
                to = Math.min(to, Math.max(lastColumn - 1, 0));
            }
            int hit = indexOfIdentifier(cps, name, from, to);
            if (hit >= 0) {
                return toRange(line, hit + 1, line, hit + nameCps + 1, document, encoding);
            }
        }
        return null;
    }

    private static int indexOfIdentifier(final int[] cps, final String name, final int from, final int to) {
        int[] nameCps = name.codePoints().toArray();
        if (nameCps.length == 0) {
            return -1;
        }
        int start = Math.max(from, 0);
        int end = Math.min(to, cps.length);
        for (int i = start; i + nameCps.length <= end; i++) {
            if (isIdentifierAt(cps, nameCps, i)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isIdentifierAt(final int[] cps, final int[] nameCps, final int i) {
        if (!regionEquals(cps, i, nameCps)) {
            return false;
        }
        if (i > 0 && Character.isJavaIdentifierPart(cps[i - 1])) {
            return false;
        }
        int after = i + nameCps.length;
        return after >= cps.length || !Character.isJavaIdentifierPart(cps[after]);
    }

    private static boolean regionEquals(final int[] cps, final int offset, final int[] nameCps) {
        for (int i = 0; i < nameCps.length; i++) {
            if (cps[offset + i] != nameCps[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the text of the 1-based line, without its terminator.
     *
     * @param document full document
     * @param groovyLine 1-based line number
     * @return the line, or {@code ""} when out of range
     */
    public static String lineText(final String document, final int groovyLine) {
        if (document == null || groovyLine < 1) {
            return "";
        }
        int current = 1;
        int start = 0;
        int i = 0;
        while (i < document.length()) {
            char c = document.charAt(i);
            if (c == '\n' || c == '\r') {
                if (current == groovyLine) {
                    return document.substring(start, i);
                }
                if (c == '\r' && i + 1 < document.length() && document.charAt(i + 1) == '\n') {
                    i += 1;
                }
                current += 1;
                start = i + 1;
            }
            i += 1;
        }
        if (current == groovyLine) {
            return document.substring(start);
        }
        return "";
    }

    private static int codePointsForUtf8Prefix(final String line, final int byteCount) {
        byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
        int limit = Math.min(Math.max(byteCount, 0), bytes.length);
        int i = 0;
        int codePoints = 0;
        while (i < limit) {
            int b = bytes[i] & 0xFF;
            int len;
            if (b < 0x80) {
                len = 1;
            } else if (b < 0xE0) {
                len = 2;
            } else if (b < 0xF0) {
                len = 3;
            } else {
                len = 4;
            }
            if (i + len > limit) {
                break;
            }
            i += len;
            codePoints += 1;
        }
        return codePoints;
    }
}
