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
package org.codehaus.groovy.util

import groovy.transform.CompileStatic

/**
 * String utility functions.
 */
@CompileStatic
class StringUtil {

    /**
     * Provides Groovy with functionality similar to the unix tr command
     * which translates a string replacing characters from a source set
     * with characters from a replacement set.
     *
     * @since 1.7.3
     */
    static String tr(String text, String source, String replacement) {
        if (text == null   || source == null   || replacement == null  ) return text
        if (text.isEmpty() || source.isEmpty() || replacement.isEmpty()) return text

        source = expandHyphen(source)
        replacement = expandHyphen(replacement)

        // padding replacement with a last character, if necessary
        replacement = replacement.padRight(source.size(), replacement[-1])

        text.collect { String character ->
            if (source.contains(character)) {
                replacement[source.lastIndexOf(character)]
            } else {
                character
            }
        }.join('')
    }

    // no expansion for hyphen at start or end of Strings
    private static String expandHyphen(String text) {
        if (text.contains('-')) {
            text.replaceAll(/(.)-(.)/) { _, start, until -> (start..until).join('') }
        } else {
            text
        }
    }

    private static String BLOCK = '\u2588'
    private static char FRACTIONAL_OFFSET = BLOCK.codePointAt(0) + 7

    /**
     * Draw a band whose width is proportional to (x - min) and equal to width
     * when x equals max. Unicode block element characters are used to draw
     * fractional blocks. A 1/8 block is always added at the start of the bar.
     *
     * If using Windows command-line, make sure you are using the Unicode
     * codepage (65001) and a font that supports block elements (like Cascadia).
     *
     * @param x the value for the bar to be drawn
     * @param min the minimum value
     * @param max the maximum value
     * @param width how many characters should the maximum value be rendered as (default 40)
     * @return the rendered ascii barchart string
     * @since 5.0.0
     */
    static String bar(Number x, Number min, Number max, int width = 40) {
        int fracWidth = width * 8
        assert min < max && x >= min && x <= max
        Number interval = max - min
        int barWidth = ((x - min) / interval * fracWidth).intValue()
        int fullChunks = barWidth.intdiv(8)
        int remainder = barWidth % 8
        // unicode for fractional blocks (7/8, 6/8, etc.) are adjacent
        BLOCK * fullChunks + Character.valueOf(FRACTIONAL_OFFSET - remainder as char)
    }
}
