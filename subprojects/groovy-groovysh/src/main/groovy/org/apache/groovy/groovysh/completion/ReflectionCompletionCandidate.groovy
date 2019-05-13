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

/**
 * A candidate as String with additional jansi formatting codes
 */
@CompileStatic
class ReflectionCompletionCandidate implements Comparable<ReflectionCompletionCandidate> {

    private final String value
    private final List<String> jAnsiCodes

    ReflectionCompletionCandidate(final String value, final String... jAnsiCodes) {
        this.value = value
        this.jAnsiCodes = new ArrayList<>(Arrays.asList(jAnsiCodes))
    }

    String getValue() {
        return value
    }

    List<String> getjAnsiCodes() {
        return jAnsiCodes
    }

    @Override
    int compareTo(ReflectionCompletionCandidate o) {
        boolean hasBracket = this.value.contains('(')
        boolean otherBracket = o.value.contains('(')
        if (hasBracket == otherBracket) {
            this.value.compareTo(o.value)
        } else if (hasBracket && ! otherBracket) {
            return -1
        } else {
            return 1
        }

    }

    @Override
    String toString() {
        return value
    }

    @Override
    int hashCode() {
        return value.hashCode()
    }

    @Override
    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ReflectionCompletionCandidate that = (ReflectionCompletionCandidate) o

        return value == that.value
    }
}
