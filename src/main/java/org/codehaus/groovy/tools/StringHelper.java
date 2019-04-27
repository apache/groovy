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
package org.codehaus.groovy.tools;

import java.util.LinkedList;
import java.util.List;

public class StringHelper {
    private static final char
        SPACE = ' ', SINGLE_QUOTE = '\'', DOUBLE_QUOTE = '"';
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * This method tokenizes a string by space characters, 
     * but ignores spaces in quoted parts,that are parts in 
     * '' or "". The method does allows the usage of "" in '' 
     * and '' in "". The space character between tokens is not 
     * returned. 
     * 
     * @param s the string to tokenize
     * @return the tokens
     */
    public static String[] tokenizeUnquoted(String s) {
        List tokens = new LinkedList();
        int first = 0;
        while (first < s.length()) {
            first = skipWhitespace(s, first);
            int last = scanToken(s, first);
            if (first < last) {
                tokens.add(s.substring(first, last));
            }
            first = last;
        }
        return (String[])tokens.toArray(EMPTY_STRING_ARRAY);
    }

    private static int scanToken(String s, int pos0) {
        int pos = pos0;
        while (pos < s.length()) {
            char c = s.charAt(pos);
            if (SPACE==c) break;
            pos++;
            if (SINGLE_QUOTE == c) {
                pos = scanQuoted(s, pos, SINGLE_QUOTE);
            } else if (DOUBLE_QUOTE == c) {
                pos = scanQuoted(s, pos, DOUBLE_QUOTE);
            }
        }
        return pos;
    }

    private static int scanQuoted(String s, int pos0, char quote) {
        int pos = pos0;
        while (pos < s.length()) {
            char c = s.charAt(pos++);
            if (quote == c) break;
        }
        return pos;
    }

    private static int skipWhitespace(String s, int pos0) {
        int pos = pos0;
        while (pos < s.length()) {
            char c = s.charAt(pos);
            if (SPACE!=c) break;
            pos++;
        }
        return pos;
    } 
}
