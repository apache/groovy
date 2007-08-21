/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.shell.completor

/**
 * Support for simple completors.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class SimpleCompletor
    extends jline.SimpleCompletor
{
    SimpleCompletor(String[] candidates) {
        super(candidates)
    }
    
    protected SimpleCompletor() {
        super(null)
    }

    def leftShift(final String candidate) {
        assert candidate
        
        addCandidateString(candidate)
    }

    //
    // NOTE: Duplicated (and augumented) from JLine sources to make it call getCandidates() to make the list more dynamic
    //

    int complete(final String buffer, final int cursor, final List clist) {
        def start = (buffer == null) ? '' : buffer
        def matches = getCandidates().tailSet(start)

        for (match in matches) {
            if (!(match.startsWith(start))) {
                break
            }

            if (delimiter != null) {
                int index = match.indexOf(delimiter, cursor)

                if (index != -1) {
                    match = match.substring(0, index + 1)
                }
            }

            clist.add(match)
        }

        if (clist.size() == 1) {
            clist.set(0, ((String) clist.get(0)) + " ")
        }

        return (clist.size() == 0) ? (-1) : 0
    }
}