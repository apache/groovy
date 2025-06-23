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

import groovy.transform.CompileStatic
import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine

import org.jline.reader.impl.completer.ArgumentCompleter
//import org.jline.reader.impl.completer.ArgumentCompleter.WhitespaceArgumentDelimiter

/**
 * This fixes strict jline 2.12 ArgumentCompleter
 * See https://github.com/jline/jline2/pull/202
 */
@CompileStatic
class StricterArgumentCompleter extends ArgumentCompleter {
/**
     *  Create a new completer with the default
     * {@link org.jline.reader.impl.completer.ArgumentCompleter.WhitespaceArgumentDelimiter}.
     *
     * @param completers The embedded completers
     */
    StricterArgumentCompleter(List<Completer> completers) {
        super(completers)
    }

    @Override
    void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        // buffer can be null
//        checkNotNull(candidates)

        def list = line.words()
        int argpos = line.wordCursor()
        int argIndex = line.cursor()

        if (argIndex < 0) {
            return
        }

        List<Completer> completers = getCompleters()
        Completer completer

        // if we are beyond the end of the completers, just use the last one
        if (argIndex >= completers.size()) {
            completer = completers.get(completers.size() - 1)
        } else {
            completer = completers.get(argIndex)
        }

        // ensure that all the previous completers are successful before allowing this completer to pass (only if strict).
        for (int i = 0; isStrict() && (i < argIndex); i++) {
            Completer sub = completers.get(i >= completers.size() ? (completers.size() - 1) : i)
            String[] args = list.toArray(new String[0])
            String arg = (args == null || i >= args.length) ? "" : args[i]

            List<Candidate> subCandidates = new LinkedList<Candidate>()
            sub.complete(reader, line, subCandidates as List<Candidate>)

            // for strict matching, one of the candidates must equal the current argument "arg",
            // starting from offset within arg, but the suitable candidate may actually also have a
            // delimiter at the end.
            boolean candidateMatches = false
            for (Candidate subCandidate : subCandidates) {
/*                // each Subcandidate may end with the delimiter.
                // That it contains the delimiter is possible, but not likely.
                String[] candidateDelimList = delim.delimit(subCandidate, 0).arguments
                if (candidateDelimList.length == 0) {
                    continue
                }
                String trimmedCand = candidateDelimList[0]
                if (trimmedCand.equals(arg.substring(offset))) {
                    candidateMatches = true
                    break
                }*/
            }
            if (!candidateMatches) {
                return
            }
        }

/*        int ret = completer.complete(list.getCursorArgument(), argpos, candidates)

        if (ret == -1) {
            return
        }

        int pos = ret + list.bufferPosition - argpos*/

        // Special case: when completing in the middle of a line, and the area under the cursor is a delimiter,
        // then trim any delimiters from the candidates, since we do not need to have an extra delimiter.
        //
        // E.g., if we have a completion for "foo", and we enter "f bar" into the buffer, and move to after the "f"
        // and hit TAB, we want "foo bar" instead of "foo  bar".

/*        if ((cursor != buffer.length()) && delim.isDelimiter(buffer, cursor)) {
            for (int i = 0; i < candidates.size(); i++) {
                CharSequence val = candidates.get(i)
                while (val.length() > 0 && delim.isDelimiter(val, val.length() - 1)) {
                    val = val.subSequence(0, val.length() - 1)
                }
                candidates.set(i, val)
            }
        }*/

//        Log.trace("Completing ", buffer, " (pos=", cursor, ") with: ", candidates, ": offset=", pos)

//        return pos
    }
}
