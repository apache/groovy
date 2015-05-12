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

import static jline.internal.Preconditions.checkNotNull;

/**
 * Similar to a strict jline ArgumentCompleter, this completer
 * completes the n+1th argument only if the 1st to nth argument have matches.
 * However, it only does so if the 1st to nth argument match *exactly*, not just partially.
 * This prevents interaction of completers between e.g. ":s", ":set" ":show" on same ":s foo"
 * See https://github.com/jline/jline2/pull/123
 *
 */
@CompileStatic
class StricterArgumentCompleter extends ArgumentCompleter {

    /**
     *  Create a new completer with the default
     *  { @link jline.console.completer.ArgumentCompleter.WhitespaceArgumentDelimiter } .
     *
     * @param completers The embedded completers
     */
    StricterArgumentCompleter(List<Completer> completers) {
        super(completers)
    }

    public int complete(final String buffer, final int cursor, final List<CharSequence> candidates) {
        if (isStrict()) {
            checkNotNull(candidates);
            ArgumentDelimiter delim = getDelimiter();
            ArgumentList list = delim.delimit(buffer, cursor);
            int argIndex = list.getCursorArgumentIndex();
            // stricter check that all previous arguments have been matched *exactly*
            // ensure that all the previous completers are successful before allowing this completer to pass (only if strict).
            for (int i = 0; i < argIndex; i++) {
                Completer sub = completers.get(i >= completers.size() ? (completers.size() - 1) : i);
                String[] args = list.getArguments();
                String arg = (args == null || i >= args.length) ? "" : args[i];

                List<CharSequence> subCandidates = new LinkedList<CharSequence>();

                if (sub.complete(arg, arg.length(), subCandidates) == -1) {
                    return -1;
                }

                boolean candidateMatches = false;
                for (CharSequence subCandidate: subCandidates) {
                    // Since we know delimiter is whitespace, we can use String.trim(), in contrast to super class
                    String trimmedCand = subCandidate.toString().trim();
                    if (trimmedCand.equals(arg)) {
                        candidateMatches = true;
                        break;
                    }
                }
                if (!candidateMatches) {
                    return -1;
                }

            }

        }
        return super.complete(buffer, cursor, candidates);
    }
}
