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
package org.codehaus.groovy.tools.shell.completion

import groovy.transform.CompileStatic
import jline.console.completer.ArgumentCompleter
import jline.console.completer.ArgumentCompleter.ArgumentDelimiter
import jline.console.completer.ArgumentCompleter.ArgumentList
import jline.console.completer.Completer
import jline.internal.Log

import static jline.internal.Preconditions.checkNotNull

/**
 * This fixes strict jline 2.12 ArgumentCompleter
 * See https://github.com/jline/jline2/pull/202
 */
@CompileStatic
@Deprecated
class StricterArgumentCompleter extends ArgumentCompleter {

    /**
     *  Create a new completer with the default
     * {@link jline.console.completer.ArgumentCompleter.WhitespaceArgumentDelimiter}.
     *
     * @param completers The embedded completers
     */
    StricterArgumentCompleter(List<Completer> completers) {
        super(completers)
    }

    int complete(final String buffer, final int cursor, final List<CharSequence> candidates) {
        // buffer can be null
        checkNotNull(candidates)

        ArgumentDelimiter delim = delimiter
        ArgumentList list = delim.delimit(buffer, cursor)
        int argpos = list.argumentPosition
        int argIndex = list.cursorArgumentIndex

        if (argIndex < 0) {
            return -1
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
            String[] args = list.getArguments()
            String arg = (args == null || i >= args.length) ? "" : args[i]

            List<CharSequence> subCandidates = new LinkedList<CharSequence>()
            int offset = sub.complete(arg, arg.length(), subCandidates)
            if (offset == -1) {
                return -1
            }

            // for strict matching, one of the candidates must equal the current argument "arg",
            // starting from offset within arg, but the suitable candidate may actually also have a
            // delimiter at the end.
            boolean candidateMatches = false
            for (CharSequence subCandidate : subCandidates) {
                // each Subcandidate may end with the delimiter.
                // That it contains the delimiter is possible, but not likely.
                String[] candidateDelimList = delim.delimit(subCandidate, 0).arguments
                if (candidateDelimList.length == 0) {
                    continue
                }
                String trimmedCand = candidateDelimList[0]
                if (trimmedCand.equals(arg.substring(offset))) {
                    candidateMatches = true
                    break
                }
            }
            if (!candidateMatches) {
                return -1
            }
        }

        int ret = completer.complete(list.getCursorArgument(), argpos, candidates)

        if (ret == -1) {
            return -1
        }

        int pos = ret + list.bufferPosition - argpos

        // Special case: when completing in the middle of a line, and the area under the cursor is a delimiter,
        // then trim any delimiters from the candidates, since we do not need to have an extra delimiter.
        //
        // E.g., if we have a completion for "foo", and we enter "f bar" into the buffer, and move to after the "f"
        // and hit TAB, we want "foo bar" instead of "foo  bar".

        if ((cursor != buffer.length()) && delim.isDelimiter(buffer, cursor)) {
            for (int i = 0; i < candidates.size(); i++) {
                CharSequence val = candidates.get(i)
                while (val.length() > 0 && delim.isDelimiter(val, val.length() - 1)) {
                    val = val.subSequence(0, val.length() - 1)
                }
                candidates.set(i, val)
            }
        }

        Log.trace("Completing ", buffer, " (pos=", cursor, ") with: ", candidates, ": offset=", pos)

        return pos
    }
}
