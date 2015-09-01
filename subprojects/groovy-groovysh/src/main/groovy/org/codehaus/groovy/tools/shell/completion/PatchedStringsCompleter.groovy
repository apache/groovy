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

import jline.console.completer.StringsCompleter

import static jline.internal.Preconditions.checkNotNull

/**
 * Changes JLine 2.12 StringsCompleter behavior to either always or never add blanks.
 */
public class PatchedStringsCompleter
        extends StringsCompleter
{

    private boolean withBlank = true

    public PatchedStringsCompleter() {
    }

    public PatchedStringsCompleter(final Collection<String> strings) {
        super(strings)
    }

    public PatchedStringsCompleter(final String... strings) {
        this(Arrays.asList(strings));
    }

    void setWithBlank(boolean withBlank) {
        this.withBlank = withBlank
    }

    @Override
    public int complete(final String buffer, final int cursor, final List<CharSequence> candidates) {
        // buffer could be null
        checkNotNull(candidates);

        if (buffer == null) {
            candidates.addAll(strings.collect({it -> withBlank ? it + ' ' : it}));
        }
        else {
            for (String match : strings.tailSet(buffer)) {
                if (!match.startsWith(buffer)) {
                    break;
                }

                candidates.add(withBlank ? match + ' ' : match);
            }
        }

        return candidates.isEmpty() ? -1 : 0;
    }
}
