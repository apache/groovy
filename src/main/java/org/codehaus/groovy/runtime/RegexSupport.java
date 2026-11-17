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
package org.codehaus.groovy.runtime;

import java.util.regex.Matcher;

/**
 * Used to store the last regex match.
 */
public class RegexSupport {

    private static final ThreadLocal CURRENT_MATCHER = new ThreadLocal();

    /**
     * Returns the matcher saved for the current thread.
     *
     * @return that matcher, or null when this thread has not saved one
     */
    public static Matcher getLastMatcher() {
        return (Matcher) CURRENT_MATCHER.get();
    }

    /**
     * Saves {@code matcher} as the last match on the current thread.
     * Another thread keeps its own matcher. Passing null makes
     * {@link #getLastMatcher} return null.
     *
     * @param matcher matcher to store, or null to clear this thread's match
     */
    public static void setLastMatcher(Matcher matcher) {
        CURRENT_MATCHER.set(matcher);
    }
}
