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

import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.ArrayList;
import java.util.Collections;
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
        Collections.sort(recorder.getValues(),
                (v1, v2) -> v2.getColumn() - v1.getColumn()
        );
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

            String[] strs = str.split("\r\n|\r|\n");
            int endColumn = strs.length == 1 ?
                    startColumn + str.length() : // exclusive
                    Integer.MAX_VALUE; // multi-line strings are always placed on new lines

            for (int j = 1; j < lines.size(); j++)
                if (endColumn < startColumns.get(j)) {
                    placeString(lines.get(j), str, startColumn);
                    startColumns.set(j, startColumn);
                    continue nextValue;
                } else {
                    placeString(lines.get(j), "|", startColumn);
                    if (j > 1) // make sure that no values are ever placed on empty line
                        startColumns.set(j, startColumn + 1); // + 1: no whitespace required between end of value and "|"
                }

            // value could not be placed on existing lines, so place it on new line(s)
            for (String s : strs) {
                StringBuilder newLine = new StringBuilder();
                lines.add(newLine);
                placeString(newLine, s, startColumn);
                startColumns.add(startColumn);
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
            toString = InvokerHelper.format(value, true, -1, false);
        } catch (Exception e) {
            return String.format("%s (toString() threw %s)",
                    javaLangObjectToString(value), e.getClass().getName());
        }

        if (toString == null) {
            return String.format("%s (toString() == null)", javaLangObjectToString(value));
        }

        if (toString.equals("")) {
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
