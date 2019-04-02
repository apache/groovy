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
        if (!text || !source) { return text }
        source = expandHyphen(source)
        replacement = expandHyphen(replacement)

        // padding replacement with a last character, if necessary
        replacement = replacement.padRight(source.size(), replacement[-1])

        text.collect { String original ->
            if (source.contains(original)) {
                replacement[source.lastIndexOf(original)]
            } else {
                original
            }
        }.join('')
    }

    // no expansion for hyphen at start or end of Strings
    private static String expandHyphen(String text) {
        if (!text.contains('-')) { return text }
        text.replaceAll(/(.)-(.)/, { all, begin, end -> (begin..end).join('') })
    }
}
