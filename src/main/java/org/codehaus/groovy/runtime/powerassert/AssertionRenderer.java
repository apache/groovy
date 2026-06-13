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

import org.codehaus.groovy.runtime.FormatHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a string representation of an assertion and its recorded values.
 */
public final class AssertionRenderer {
    private final String text;
    private final ValueRecorder recorder;
    private final List<StringBuilder> lines = new ArrayList<StringBuilder>();
    // startColumns.get(i) is the first non-empty column of lines.get(i)
    private final List<Integer> startColumns = new ArrayList<Integer>();

    private AssertionRenderer(String text, ValueRecorder recorder) {
        if (text.contains("\n"))
            throw new IllegalArgumentException("source text may not contain line breaks");

        this.text = text;
        this.recorder = recorder;
    }

    /**
     * Creates a string representation of an assertion and its recorded values.
     *
     * @param text     the assertion's source text
     * @param recorder a recorder holding the values recorded during evaluation
     *                 of the assertion
     * @return a string representation of the assertion and its recorded values
     */
    public static String render(String text, ValueRecorder recorder) {
        return new AssertionRenderer(text, recorder).render();
    }

    private String render() {
        renderText();
        sortValues();
        renderValues();
        return linesToString();
    }

    private void renderText() {
        lines.add(new StringBuilder(text));
        startColumns.add(0);
        lines.add(new StringBuilder()); // empty line
        startColumns.add(0);
    }

    private void sortValues() {
        // it's important to use a stable sort here, otherwise
        // renderValues() will skip the wrong values
        recorder.getValues().sort((v1, v2) -> v2.getColumn() - v1.getColumn());
    }

    private void renderValues() {
        List<Value> values = recorder.getValues();
        int valuesSize = values.size();

        nextValue:
        for (int i = 0; i < valuesSize; i++) {
            final Value value = values.get(i);
            final int startColumn = value.getColumn();
            if (startColumn < 1) continue; // skip values with unknown source position

            // if multiple values are associated with the same column, only
            // render the value which was recorded last (i.e. the value
            // corresponding to the outermost expression)
            // important for GROOVY-4344
            Value next = i + 1 < valuesSize ? values.get(i + 1) : null;
            if (next != null && next.getColumn() == startColumn) continue;

            String str = valueToString(value.getValue());
            if (str == null) continue; // null signals the value shouldn't be rendered

            // the recorded column is a code-point column in the (normalized) source text, but
            // markers are laid out one cell per column; translate to an estimated display column
            // so they line up with wide glyphs such as emoji in the common case (see displayColumn)
            final int placeColumn = displayColumn(text, startColumn);

            String[] strs = str.split("\r\n|\r|\n");
            int endColumn = strs.length == 1 ?
                    placeColumn + str.length() : // exclusive
                    Integer.MAX_VALUE; // multi-line strings are always placed on new lines

            for (int j = 1; j < lines.size(); j++)
                if (endColumn < startColumns.get(j)) {
                    placeString(lines.get(j), str, placeColumn);
                    startColumns.set(j, placeColumn);
                    continue nextValue;
                } else {
                    placeString(lines.get(j), "|", placeColumn);
                    if (j > 1) // make sure that no values are ever placed on empty line
                        startColumns.set(j, placeColumn + 1); // + 1: no whitespace required between end of value and "|"
                }

            // value could not be placed on existing lines, so place it on new line(s)
            for (String s : strs) {
                StringBuilder newLine = new StringBuilder();
                lines.add(newLine);
                placeString(newLine, s, placeColumn);
                startColumns.add(placeColumn);
            }
        }
    }

    private String linesToString() {
        StringBuilder firstLine = lines.get(0);
        for (int i = 1; i < lines.size(); i++)
            firstLine.append('\n').append(lines.get(i).toString());
        return firstLine.toString();
    }

    private static void placeString(StringBuilder line, String str, int column) {
        while (line.length() < column)
            line.append(' ');
        line.replace(column - 1, column - 1 + str.length(), str);
    }

    /**
     * Best-effort translation of a 1-based code-point column in {@code text} into a
     * 1-based terminal display column, estimating the cell width of each preceding
     * character. This lets value markers line up with assertion source containing
     * wide glyphs (e.g. emoji and CJK) in the common case where such glyphs occupy
     * two cells. It is necessarily approximate: actual width depends on the font and
     * terminal, and grapheme clusters (combining marks, ZWJ sequences, regional-
     * indicator flags, variation selectors) are not resolved. For text consisting of
     * ordinary narrow characters this is the identity, so rendering is unchanged.
     */
    private static int displayColumn(String text, int column) {
        int display = 1, index = 0, length = text.length();
        for (int cp = 1; cp < column; cp++) {
            if (index < length) {
                int codePoint = text.codePointAt(index);
                display += displayWidth(codePoint);
                index += Character.charCount(codePoint);
            } else {
                display += 1; // beyond the text: treat as a single-cell position
            }
        }
        return display;
    }

    /**
     * Estimates the number of terminal cells a code point occupies: 0 for combining
     * or formatting marks, 2 for wide (East Asian Wide/Fullwidth) characters and most
     * emoji, and 1 otherwise. See {@link #displayColumn} for the caveats.
     */
    private static int displayWidth(int codePoint) {
        switch (Character.getType(codePoint)) {
            case Character.NON_SPACING_MARK:
            case Character.ENCLOSING_MARK:
            case Character.FORMAT:
                return 0;
            default:
                return isWide(codePoint) ? 2 : 1;
        }
    }

    private static boolean isWide(int cp) {
        return (cp >= 0x1100 && cp <= 0x115F)    // Hangul Jamo
            || cp == 0x2329 || cp == 0x232A      // angle brackets
            || (cp >= 0x2E80 && cp <= 0x303E)    // CJK radicals .. Kangxi
            || (cp >= 0x3041 && cp <= 0x33FF)    // Hiragana .. CJK symbols and punctuation
            || (cp >= 0x3400 && cp <= 0x4DBF)    // CJK Unified Ideographs Extension A
            || (cp >= 0x4E00 && cp <= 0x9FFF)    // CJK Unified Ideographs
            || (cp >= 0xA000 && cp <= 0xA4CF)    // Yi
            || (cp >= 0xAC00 && cp <= 0xD7A3)    // Hangul Syllables
            || (cp >= 0xF900 && cp <= 0xFAFF)    // CJK Compatibility Ideographs
            || (cp >= 0xFE10 && cp <= 0xFE19)    // Vertical Forms
            || (cp >= 0xFE30 && cp <= 0xFE6F)    // CJK Compatibility Forms
            || (cp >= 0xFF00 && cp <= 0xFF60)    // Fullwidth Forms
            || (cp >= 0xFFE0 && cp <= 0xFFE6)    // Fullwidth signs
            || (cp >= 0x1F000 && cp <= 0x1F1FF)  // mahjong/domino/cards/regional indicators
            || (cp >= 0x1F300 && cp <= 0x1FAFF)  // emoji & pictographs
            || (cp >= 0x20000 && cp <= 0x3FFFD); // CJK Unified Ideographs Extension B+
    }

    /**
     * Returns a string representation of the given value, or <tt>null</tt> if
     * the value should not be included (because it does not add any valuable
     * information).
     *
     * @param value a value
     * @return a string representation of the given value
     */
    private static String valueToString(Object value) {
        String toString;

        try {
            toString = FormatHelper.format(value, true, -1, false);
        } catch (Exception e) {
            return String.format("%s (toString() threw %s)",
                    javaLangObjectToString(value), e.getClass().getName());
        }

        if (toString == null) {
            return String.format("%s (toString() == null)", javaLangObjectToString(value));
        }

        if (toString.isEmpty()) {
            if (hasStringLikeType(value)) return "\"\"";
            return String.format("%s (toString() == \"\")", javaLangObjectToString(value));
        }

        return toString;
    }

    private static boolean hasStringLikeType(Object value) {
        Class<?> clazz = value.getClass();
        return clazz == String.class || clazz == StringBuffer.class || clazz == StringBuilder.class;
    }

    private static String javaLangObjectToString(Object value) {
        String hash = Integer.toHexString(System.identityHashCode(value));
        return value.getClass().getName() + "@" + hash;
    }
}
