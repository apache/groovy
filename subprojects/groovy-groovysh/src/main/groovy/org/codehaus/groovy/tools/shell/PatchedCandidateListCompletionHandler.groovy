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
package org.codehaus.groovy.tools.shell

import groovy.transform.CompileStatic
import jline.console.ConsoleReader
import jline.console.CursorBuffer
import jline.console.completer.CandidateListCompletionHandler
import org.codehaus.groovy.tools.shell.util.JAnsiHelper

/**
 * jline completion handler displays ANSIfied candidates nicely,
 * but does not de-ANSIfy when adding to the prompt :-(
 *
 * So this class just adds this functionality.
 *
 * See https://github.com/jline/jline2/issues/132
 */
@CompileStatic
class PatchedCandidateListCompletionHandler extends CandidateListCompletionHandler {

    public boolean complete(final ConsoleReader reader, final List<CharSequence> candidates, final int pos) throws
            IOException
    {
        CursorBuffer buf = reader.getCursorBuffer();
        final List<CharSequence> deAnsifiedcandidates = candidates.collect({CharSequence candidate -> JAnsiHelper.stripAnsi(candidate) })

        // if there is only one completion, then fill in the buffer
        if (candidates.size() == 1) {
            CharSequence value = deAnsifiedcandidates.get(0);

            // fail if the only candidate is the same as the current buffer
            if (value.equals(buf.toString())) {
                return false;
            }

            setBuffer(reader, value, pos);

            return true;
        }
        else if (candidates.size() > 1) {
            String value = this.getUnambiguousCompletions(deAnsifiedcandidates);
            setBuffer(reader, value, pos);
        }

        printCandidates(reader, candidates);

        // redraw the current console buffer
        reader.drawLine();

        return true;
    }

    /**
     * copied from CandidateListCompletionHandler because it was private :-(
     * Returns a root that matches all the {@link String} elements of the specified {@link List},
     * or null if there are no commonalities. For example, if the list contains
     * <i>foobar</i>, <i>foobaz</i>, <i>foobuz</i>, the method will return <i>foob</i>.
     */
    private String getUnambiguousCompletions(final List<CharSequence> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        // convert to an array for speed
        String[] strings = candidates.toArray(new String[candidates.size()]);

        String first = strings[0];
        StringBuilder candidate = new StringBuilder();

        for (int i = 0; i < first.length(); i++) {
            if (startsWith(first.substring(0, i + 1), strings)) {
                candidate.append(first.charAt(i));
            }
            else {
                break;
            }
        }

        return candidate.toString();
    }

    /**
     * copied from CandidateListCompletionHandler because it was private :-(
     * @return true is all the elements of <i>candidates</i> start with <i>starts</i>
     */
    private boolean startsWith(final String starts, final String[] candidates) {
        for (String candidate : candidates) {
            if (!candidate.startsWith(starts)) {
                return false;
            }
        }

        return true;
    }
}
