/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.transform.powerassert;

import java.util.*;

/**
 * Creates a string representation of an assertion and its recorded values.
 *
 * @author Peter Niederwieser
 */
public class AssertionRenderer {
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
        Collections.sort(recorder.getValues(),
                new Comparator<Value>() {
                    public int compare(Value v1, Value v2) {
                        return v2.getColumn() - v1.getColumn();
                    }
                }
        );
    }

    private void renderValues() {
        nextValue:
        for (Value value : recorder.getValues()) {
            String str = valueToString(value.getValue());
            if (str == null) continue;

            int startColumn = value.getColumn();
            if (startColumn < 1) continue; // node with invalid source position

            String[] strs = str.split("\r\n|\r|\n");
            int endColumn = strs.length == 1 ?
                    value.getColumn() + str.length() : // exclusive
                    Integer.MAX_VALUE; // multi-line strings are always placed on new lines

            for (int i = 1; i < lines.size(); i++)
                if (endColumn < startColumns.get(i)) {
                    placeString(lines.get(i), str, startColumn);
                    startColumns.set(i, startColumn);
                    continue nextValue;
                } else {
                    placeString(lines.get(i), "|", startColumn);
                    if (i > 1) // make sure that no values are ever placed on empty line
                        startColumns.set(i, startColumn + 1); // + 1: no whitespace required between end of value and "|"
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
        if (value == null) return "null";
        if (isEmptyStr(value)) return "\"\"";

        String str;
        String errorMsg = "";
        try {
            str = value.getClass().isArray() ? arrayToString(value) : value.toString();
        } catch (Exception e) {
            str = null;
            errorMsg = " (toString() threw " + e.getClass().getName() + ")";
        }

        // if the value has no string representation, produce something like Object.toString()
        if (str == null || str.equals("")) {
            String hash = Integer.toHexString(System.identityHashCode(value));
            if(errorMsg.isEmpty()) {
            	errorMsg = (str == null) ? " (toString() == null)" : " (toString() == \"\")";
            }
            return value.getClass().getName() + "@" + hash + errorMsg;
        }

        return str;
    }

    private static boolean isEmptyStr(Object value) {
    	Class<?> clazz = value.getClass();
    	if((clazz == String.class || clazz == StringBuffer.class || 
    			clazz == StringBuilder.class) && value.toString().equals("")) {
    		return true;
    	}
    	return false;
    }
    
    private static String arrayToString(Object array) {
        Class<?> clazz = array.getClass();

        if (clazz == byte[].class) return Arrays.toString((byte[]) array);
        if (clazz == short[].class) return Arrays.toString((short[]) array);
        if (clazz == int[].class) return Arrays.toString((int[]) array);
        if (clazz == long[].class) return Arrays.toString((long[]) array);
        if (clazz == char[].class) return Arrays.toString((char[]) array);
        if (clazz == float[].class) return Arrays.toString((float[]) array);
        if (clazz == double[].class) return Arrays.toString((double[]) array);
        if (clazz == boolean[].class) return Arrays.toString((boolean[]) array);
        return Arrays.deepToString((Object[]) array);
    }
}
