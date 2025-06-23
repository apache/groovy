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
package org.apache.groovy.groovysh.completion

import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine

/**
 * A completer within compatible strings (single/double quotes, single/double triple quotes)
 * showing informational alternatives that can occur after the backslash escape character.
 * No completion occurs and the cursor remains where it is.
 *
 * @since 2.4.13
 */
class BackslashEscapeCompleter implements Completer {
    private static final Map<String, String> VALID_ESCAPEES = [
        r: 'return', n: 'newline', t: 'tab',
        '\\': 'backslash', "'": 'single quote', '"': 'double quote',
        b: 'backspace', f: 'formfeed']
    private static final GROUP = 'Escape Sequences'

    @Override
    void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String buffer = line.line().substring(0, line.cursor())
        if (!buffer.endsWith('\\')) {
            return
        }

        VALID_ESCAPEES.each { k, v ->
            candidates.add(new Candidate(k, "$k ($v)", GROUP, null, null, null, true))
        }
        candidates.add(new Candidate('u', "$uXXXX (unicode)", GROUP, 'Complete with a 4-digit hex code', null, null, false))
    }
}
