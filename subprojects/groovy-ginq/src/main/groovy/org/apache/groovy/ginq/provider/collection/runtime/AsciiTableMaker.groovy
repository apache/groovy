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

import static java.util.stream.IntStream.range

/**
 * @since 4.0.0
 */
@PackageScope
@CompileStatic
class AsciiTableMaker {
    private static final int DEFAULT_MAX_WIDTH = Integer.MAX_VALUE
    private static final String[] EMPTY_STRING_ARRAY = new String[0]

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
            List<String[]> list = new ArrayList<>(tableData.size() + 1)
            def firstRecord = tableData.get(0)
            if (firstRecord instanceof NamedRecord) {
                list.add(((NamedRecord) firstRecord).nameList.toArray(EMPTY_STRING_ARRAY))
                for (e in tableData) {
                    if (e instanceof NamedRecord) {
                        String[] record = ((NamedRecord) e)*.toString()
                        list.add(record)
                    }
                }

                return '\n' + makeAsciiTable(list, DEFAULT_MAX_WIDTH, true)
            }
        }

        return tableData.toString()
    }

    /**
     * Create a ascii table
     *
     * @param table table data
     * @param maxWidth Maximum allowed width. Line will be wrapped beyond this width.
     * @param leftJustifiedRows If true, it will add "-" as a flag to format string to make it left justified. Otherwise right justified.
     * @return the string result representing the ascii table
     * @since 4.0.0
     */
    static String makeAsciiTable(List<String[]> table, int maxWidth, boolean leftJustifiedRows) {
        StringBuilder result = new StringBuilder()

        if (0 == maxWidth) maxWidth = DEFAULT_MAX_WIDTH

        // Create new table array with wrapped rows
        List<String[]> tableList = new ArrayList<>(table)
        List<String[]> finalTableList = new ArrayList<>(tableList.size() + 1)
        for (String[] row : tableList) {
            // If any cell data is more than max width, then it will need extra row.
            boolean needExtraRow = false
            // Count of extra split row.
            int splitRow = 0
            do {
                needExtraRow = false
                String[] newRow = new String[row.length]
                for (int i = 0; i < row.length; i++) {
                    // If data is less than max width, use that as it is.
                    def col = row[i] ?: ''
                    if (col.length() < maxWidth) {
                        newRow[i] = splitRow == 0 ? col : ''
                    } else if ((col.length() > (splitRow * maxWidth))) {
                        // If data is more than max width, then crop data at maxwidth.
                        // Remaining cropped data will be part of next row.
                        int end = Math.min(col.length(), ((splitRow * maxWidth) + maxWidth))
                        newRow[i] = col.substring((splitRow * maxWidth), end)
                        needExtraRow = true
                    } else {
                        newRow[i] = ''
                    }
                }
                finalTableList.add(newRow)
                if (needExtraRow) {
                    splitRow++
                }
            } while (needExtraRow)
        }
        String[] firstElem = finalTableList.get(0)
        String[][] finalTable = new String[finalTableList.size()][firstElem.length]
        for (int i = 0; i < finalTable.length; i++) {
            finalTable[i] = finalTableList.get(i)
        }

        // Calculate appropriate Length of each column by looking at width of data in each column.
        // Map columnLengths is <column_number, column_length>
        Map<Integer, Integer> columnLengths = new HashMap<>()
        Arrays.stream(finalTable).forEach(a -> range(0, a.length).forEach(i -> {
            columnLengths.putIfAbsent(i, 0)
            int len = a[i].length()
            if (columnLengths.get(i) < len) {
                columnLengths.put(i, len)
            }
        }))

        // Prepare format String
        def formatString = new StringBuilder(256)
        def flag = leftJustifiedRows ? '-' : ''
        for (e in columnLengths) {
            formatString.append('| %').append(flag).append(e.value).append('s ')
        }
        formatString.append('|\n')

        // Prepare line for top, bottom & below header row
        def line = new StringBuilder(256)
        for (e in columnLengths) {
            line.append('+').append('-' * (e.value + 2))
        }
        line.append('+\n')

        // Print table
        result.append(line)
        Arrays.stream(finalTable)
                .limit(1)
                .forEach(a -> result.append(String.format(formatString.toString(), (Object[]) a)))
        result.append(line)

        range(1, finalTable.length)
                .forEach(i -> result.append(String.format(formatString.toString(), (Object[]) finalTable[i])))
        result.append(line)

        return result.toString()
    }

    private AsciiTableMaker() {}
}
