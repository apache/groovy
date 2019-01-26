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
package groovy.sql;

import groovy.lang.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts and indexes named parameters from a sql string.
 *
 * This class is package-private scoped and is only intended for internal use.
 *
 * @see groovy.sql.Sql
 */
class ExtractIndexAndSql {

    private static final Pattern NAMED_QUERY_PATTERN = Pattern.compile("(?<!:)(:)(\\w+)|\\?(\\d*)(?:\\.(\\w+))?");
    private static final char QUOTE = '\'';

    private final String sql;
    private List<Tuple> indexPropList;
    private String newSql;

    /**
     * Used to track the current position within the sql while parsing
     */
    private int index = 0;

    /**
     * Static factory method used to create a new instance.  Since parsing of the input
     * is required, this ensures the object is fully initialized.
     *
     * @param sql statement to be parsed
     * @return an instance of {@link ExtractIndexAndSql}
     */
    static ExtractIndexAndSql from(String sql) {
        return new ExtractIndexAndSql(sql).invoke();
    }

    /**
     * Checks a sql statement to determine whether it contains parameters.
     *
     * @param sql statement
     * @return {@code true} if the statement contains named parameters, otherwise {@code false}
     */
    static boolean hasNamedParameters(String sql) {
        return NAMED_QUERY_PATTERN.matcher(sql).find();
    }

    private ExtractIndexAndSql(String sql) {
        this.sql = sql;
    }

    List<Tuple> getIndexPropList() {
        return indexPropList;
    }

    String getNewSql() {
        return newSql;
    }

    private ExtractIndexAndSql invoke() {
        indexPropList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        StringBuilder currentChunk = new StringBuilder();
        while (index < sql.length()) {
            switch (sql.charAt(index)) {
                case QUOTE:
                    sb.append(adaptForNamedParams(currentChunk.toString(), indexPropList));
                    currentChunk = new StringBuilder();
                    appendToEndOfString(sb);
                    break;
                case '-':
                    if (next() == '-') {
                        sb.append(adaptForNamedParams(currentChunk.toString(), indexPropList));
                        currentChunk = new StringBuilder();
                        appendToEndOfLine(sb);
                    } else {
                        currentChunk.append(sql.charAt(index));
                    }
                    break;
                case '/':
                    if (next() == '*') {
                        sb.append(adaptForNamedParams(currentChunk.toString(), indexPropList));
                        currentChunk = new StringBuilder();
                        appendToEndOfComment(sb);
                    } else {
                        currentChunk.append(sql.charAt(index));
                    }
                    break;
                default:
                    currentChunk.append(sql.charAt(index));
            }
            index++;
        }
        sb.append(adaptForNamedParams(currentChunk.toString(), indexPropList));
        newSql = sb.toString();
        return this;
    }

    private void appendToEndOfString(StringBuilder buffer) {
        buffer.append(QUOTE);
        int startQuoteIndex = index;
        ++index;
        boolean foundClosingQuote = false;
        while (index < sql.length()) {
            char c = sql.charAt(index);
            buffer.append(c);
            if (c == QUOTE && next() != QUOTE) {
                if (startQuoteIndex == (index - 1)) {   // empty quote ''
                    foundClosingQuote = true;
                    break;
                }
                int previousQuotes = countPreviousRepeatingChars(QUOTE);
                if (previousQuotes == 0 ||
                        (previousQuotes % 2 == 0 && (index - previousQuotes) != startQuoteIndex) ||
                        (previousQuotes % 2 != 0 && (index - previousQuotes) == startQuoteIndex)) {
                    foundClosingQuote = true;
                    break;
                }
            }
            ++index;
        }
        if (!foundClosingQuote) {
            throw new IllegalStateException("Failed to process query. Unterminated ' character?");
        }
    }

    private int countPreviousRepeatingChars(char c) {
        int pos = index - 1;
        while (pos >= 0) {
            if (sql.charAt(pos) != c) {
                break;
            }
            --pos;
        }
        return (index - 1) - pos;
    }

    private void appendToEndOfComment(StringBuilder buffer) {
        while (index < sql.length()) {
            char c = sql.charAt(index);
            buffer.append(c);
            if (c == '*' && next() == '/') {
                buffer.append('/');
                ++index;
                break;
            }
            ++index;
        }
    }

    private void appendToEndOfLine(StringBuilder buffer) {
        while (index < sql.length()) {
            char c = sql.charAt(index);
            buffer.append(c);
            if (c == '\n' || c == '\r') {
                break;
            }
            ++index;
        }
    }

    private char next() {
        return ((index + 1) < sql.length()) ? sql.charAt(index + 1) : '\0';
    }

    private static String adaptForNamedParams(String sql, List<Tuple> indexPropList) {
        StringBuilder newSql = new StringBuilder();
        int txtIndex = 0;

        Matcher matcher = NAMED_QUERY_PATTERN.matcher(sql);
        while (matcher.find()) {
            newSql.append(sql, txtIndex, matcher.start()).append('?');
            String indexStr = matcher.group(1);
            if (indexStr == null) indexStr = matcher.group(3);
            int index = (indexStr == null || indexStr.length() == 0 || ":".equals(indexStr)) ? 0 : Integer.parseInt(indexStr) - 1;
            String prop = matcher.group(2);
            if (prop == null) prop = matcher.group(4);
            indexPropList.add(new Tuple(index, prop == null || prop.length() == 0 ? "<this>" : prop));
            txtIndex = matcher.end();
        }
        newSql.append(sql.substring(txtIndex)); // append ending SQL after last param.
        return newSql.toString();
    }

}
