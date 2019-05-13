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
package org.codehaus.groovy.tools.shell.util

import jline.console.completer.Completer
import org.codehaus.groovy.runtime.InvokerHelper

/**
 * Support for simple completors.
 */
@Deprecated
class SimpleCompletor implements Completer {

    SortedSet<String> candidates

    /**
    * A delimiter to use to qualify completions.
    */
    protected String delimiter

    boolean withBlank = true


    SimpleCompletor(final String[] candidates) {
        setCandidateStrings(candidates)
    }

    SimpleCompletor() {
        this(new String[0])
    }

    SimpleCompletor(final Closure loader) {
        this()

        assert loader != null

        Object obj = loader.call()

        List list = null

        if (obj instanceof List) {
            list = (List) obj
        }

        //
        // TODO: Maybe handle arrays too?
        //

        if (list == null) {
            throw new IllegalStateException('The loader closure did not return a list of candidates; found: ' + obj)
        }

        Iterator iter = list.iterator()

        while (iter.hasNext()) {
            add(InvokerHelper.toString(iter.next()))
        }
    }

    void setWithBlank(boolean withBlank) {
        this.withBlank = withBlank
    }

    void add(final String candidate) {
        addCandidateString(candidate)
    }

    Object leftShift(final String s) {
        add(s)

        return null
    }

    @Override
    int complete(final String buffer, final int cursor, final List<CharSequence> clist) {
        String start = (buffer == null) ? '' : buffer

        SortedSet<String> matches = getCandidates().tailSet(start)

        for (Iterator i = matches.iterator(); i.hasNext();) {
            String can = (String) i.next()

            if (!(can.startsWith(start))) {
                break
            }

            String delim = delimiter

            if (delim != null) {
                int index = can.indexOf(delim, cursor)

                if (index != -1) {
                    can = can.substring(0, index + 1)
                }
            }

            if (withBlank) {
                can += ' '
            }

            clist.add(can)
        }

        // the index of the completion is always from the beginning of the buffer.
        return (clist.size() == 0) ? (-1) : 0
    }

    void setCandidates(final SortedSet<String> candidates) {
        this.candidates = candidates
    }

    SortedSet<String> getCandidates() {
        return Collections.unmodifiableSortedSet(this.candidates)
    }

    void setCandidateStrings(final String[] strings) {
        setCandidates(new TreeSet(Arrays.asList(strings)))
    }

    void addCandidateString(final String string) {
        if (string != null) {
            candidates.add(string)
        }
    }
}
