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
package org.codehaus.groovy.runtime.powerassert;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides the source text for an assertion statement and translates
 * coordinates in the original source text to coordinates relative to the
 * assertion's (normalized) source text.
 */
public class SourceText {
    private final int firstLine;
    private String normalizedText;

    private final List<Integer> lineOffsets = new ArrayList<Integer>();
    private final List<Integer> textOffsets = new ArrayList<Integer>();

    /**
     * Constructs a <tt>SourceText</tt> by reading the given assertion's source
     * text from the given source unit.
     *
     * @param stat       an assertion statement
     * @param sourceUnit the source unit containing the assertion statement
     * @param janitor    a <tt>Janitor</tt> for cleaning up reader sources
     */
    public SourceText(AssertStatement stat, SourceUnit sourceUnit, Janitor janitor) {
        if (!hasPlausibleSourcePosition(stat))
            throw new SourceTextNotAvailableException(stat, sourceUnit, "Invalid source position");

        firstLine = stat.getLineNumber();
        textOffsets.add(0);
        StringBuilder normalizedTextBuffer = new StringBuilder();

        for (int line = stat.getLineNumber(); line <= stat.getLastLineNumber(); line++) {
            String lineText = sourceUnit.getSample(line, 0, janitor);
            if (lineText == null)
                throw new SourceTextNotAvailableException(stat, sourceUnit, "SourceUnit.getSample() returned null");

            // AST column numbers are code-point based, but lineText is a UTF-16 String;
            // convert before slicing so supplementary characters (e.g. emoji) don't shift
            // the cut by one UTF-16 unit each (GROOVY-12085). lineOffsets stays code-point
            // based to match the (code-point) columns passed to getNormalizedColumn().
            if (line == stat.getLastLineNumber())
                lineText = lineText.substring(0, codePointToIndex(lineText, stat.getLastColumnNumber() - 1));
            if (line == stat.getLineNumber()) {
                lineText = lineText.substring(codePointToIndex(lineText, stat.getColumnNumber() - 1));
                lineOffsets.add(stat.getColumnNumber() - 1);
            } else
                lineOffsets.add(countLeadingWhitespace(lineText));

            lineText = lineText.trim();
            if (line != stat.getLastLineNumber() && !lineText.isEmpty())
                lineText += ' ';
            normalizedTextBuffer.append(lineText);
            textOffsets.add(normalizedTextBuffer.length());
        }
        normalizedText = normalizedTextBuffer.toString();
    }

    /**
     * Returns the assertion's source text after removing line breaks.
     * <p>Limitation: Line comments within the assertion's source text are not
     * handled.
     *
     * @return the assertion's source text after removing line breaks.
     */
    public String getNormalizedText() {
        return normalizedText;
    }

    /**
     * Returns the column in <tt>getNormalizedText()</tt> corresponding
     * to the given line and column in the original source text. The
     * first character in the normalized text has column 1.
     *
     * @param line   a line number
     * @param column a column number
     * @return the column in getNormalizedText() corresponding to the given line
     *         and column in the original source text
     */
    public int getNormalizedColumn(int line, int column) {
        int deltaLine = line - firstLine;
        if (deltaLine < 0 || deltaLine >= lineOffsets.size()) // wrong line information
            return -1;
        int deltaColumn = column - lineOffsets.get(deltaLine);
        if (deltaColumn < 0) // wrong column information
            return -1;

        return textOffsets.get(deltaLine) + deltaColumn;
    }

    private static boolean hasPlausibleSourcePosition(ASTNode node) {
        return node.getLineNumber() > 0
                && node.getColumnNumber() > 0
                && node.getLastLineNumber() >= node.getLineNumber()
                && node.getLastColumnNumber() >
                (node.getLineNumber() == node.getLastLineNumber() ? node.getColumnNumber() : 0);
    }

    /**
     * Translates a 0-based code-point offset into a UTF-16 {@code String} index,
     * clamping to the bounds of {@code text}. AST column numbers are code-point
     * based (the lexer reads a code-point stream), whereas the sampled source line
     * is a UTF-16 {@code String}; the two differ once supplementary characters are
     * present.
     */
    private static int codePointToIndex(String text, int codePointOffset) {
        if (codePointOffset <= 0) return 0;
        int length = text.length();
        if (codePointOffset >= text.codePointCount(0, length)) return length;
        return text.offsetByCodePoints(0, codePointOffset);
    }

    private static int countLeadingWhitespace(String lineText) {
        int result = 0;
        while (result < lineText.length() && Character.isWhitespace(lineText.charAt(result)))
            result++;
        return result;
    }
}
