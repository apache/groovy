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
package org.apache.groovy.ginq.provider.collection.runtime

import groovy.transform.CompileStatic
import groovy.transform.PackageScope

/**
 * @since 4.0.0
 */
@PackageScope
@CompileStatic
class AsciiTableMaker {
    private static final String[] EMPTY_STRING_ARRAY = new String[0]
    private static final int PADDING = 1
    private static final String SPACE = " "
    // Padding space for each cell

    /**
     * Makes ASCII table for list whose elements are of type {@link NamedRecord}.
     *
     * @param queryable the {@link Queryable} instance
     * @return the string result representing the ascii table
     * @since 4.0.0
     */
    static <T> String makeAsciiTable(Queryable<T> queryable) {
        def tableData = queryable.toList()
        if (tableData) {
            def firstRecord = tableData.get(0)
            if (firstRecord instanceof NamedRecord) {

                List<String[]> data = new ArrayList<>(tableData.size())
                for (e in tableData) {
                    if (e instanceof NamedRecord) {
                        String[] record = ((NamedRecord) e)*.toString()
                        data.add(record)
                    }
                }

                String[] headers = ((NamedRecord) firstRecord).nameList.toArray(EMPTY_STRING_ARRAY)
                boolean[] alignLeft = new boolean[headers.length]
                Arrays.fill(alignLeft, true)

                return '\n' + buildTable(headers, data, alignLeft)
            }
        }

        return tableData.toString()
    }

    private static String buildTable(String[] headers, List<String[]> data, boolean[] alignLeft) {
        int[] columnWidths = calculateColumnWidths(headers, data)

        def headerCnt = headers ? headers.length : 0
        def dataElementCnt = countElements(data)
        def allElementCnt = headerCnt + dataElementCnt
                                            + headerCnt * 3 // 3 lines of separator

        StringBuilder tableBuilder = new StringBuilder(allElementCnt * 10)
        String separator = buildSeparator(columnWidths)
        if (headers) {
            tableBuilder.append(separator)
            tableBuilder.append(buildRow(headers, columnWidths, alignLeft))
        }
        tableBuilder.append(separator)

        for (String[] row : data) {
            tableBuilder.append(buildRow(row, columnWidths, alignLeft))
        }
        tableBuilder.append(separator)

        return tableBuilder.toString()
    }

    private static int countElements(List<String[]> data) {
        if (!data) return 0

        return data.size() * data[0].length
    }

    private static int[] calculateColumnWidths(String[] headers, List<String[]> data) {
        int[] columnWidths = new int[headers.length]

        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = getDisplayWidth(headers[i])
        }

        for (String[] row : data) {
            for (int i = 0, n = row.length; i < n; i++) {
                int displayWidth = getDisplayWidth(row[i])
                if (displayWidth > columnWidths[i]) {
                    columnWidths[i] = displayWidth
                }
            }
        }

        for (int i = 0, n = columnWidths.length; i < n; i++) {
            columnWidths[i] += PADDING * 2 // Add padding
        }

        return columnWidths
    }

    private static String buildRow(String[] row, int[] columnWidths, boolean[] alignLeft) {
        StringBuilder rowBuilder = new StringBuilder(Arrays.stream(columnWidths).sum())
        rowBuilder.append("|")
        for (int i = 0; i < row.length; i++) {
            rowBuilder.append(padString(row[i], columnWidths[i], alignLeft[i]))
            rowBuilder.append("|")
        }
        rowBuilder.append("\n")
        return rowBuilder.toString()
    }

    private static String buildSeparator(int[] columnWidths) {
        StringBuilder separatorBuilder = new StringBuilder(Arrays.stream(columnWidths).sum())
        separatorBuilder.append("+")
        for (int width : columnWidths) {
            for (int i = 0; i < width; i++) {
                separatorBuilder.append("-")
            }
            separatorBuilder.append("+")
        }
        separatorBuilder.append("\n")
        return separatorBuilder.toString()
    }

    private static String padString(String str, int width, boolean alignLeft) {
        int displayWidth = getDisplayWidth(str)
        StringBuilder padded = new StringBuilder(displayWidth)
        int paddingRight = width - displayWidth - PADDING

        if (alignLeft) {
            padded.append(SPACE * PADDING) // Left padding
            padded.append(str ?: '')
            padded.append(SPACE * paddingRight) // Right padding
        } else {
            padded.append(SPACE * paddingRight) // Left padding
            padded.append(str ?: '')
            padded.append(SPACE * PADDING) // Right padding
        }

        return padded.toString()
    }

    private static int getDisplayWidth(String str) {
        if (!str) return 0
        int width = 0
        for (char c : str.toCharArray()) {
            width += isFullWidth(c) ? 2 : 1
        }
        return width
    }

    private static boolean isFullWidth(char c) {
        // space and visible ASCII characters
        if (32 <= c && c <= 126) return false

        Character.UnicodeBlock block

        try {
            block = Character.UnicodeBlock.of(c)
        } catch (IllegalArgumentException ignored) {
            return false
        }

        // Unicode block check for full-width characters
        return block === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                block === Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
                block === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                block === Character.UnicodeBlock.GENERAL_PUNCTUATION ||
                block === Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION ||
                block === Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS ||
                block === Character.UnicodeBlock.HIRAGANA ||
                block === Character.UnicodeBlock.KATAKANA ||
                block === Character.UnicodeBlock.HANGUL_SYLLABLES
    }

    private AsciiTableMaker() {}
}
