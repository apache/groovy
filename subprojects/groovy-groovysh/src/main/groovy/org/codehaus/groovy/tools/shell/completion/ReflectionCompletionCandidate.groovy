package org.codehaus.groovy.tools.shell.completion

import groovy.transform.CompileStatic

/**
 * A candidate as String with additional jansi formatting codes
 */
@CompileStatic
class ReflectionCompletionCandidate implements Comparable<ReflectionCompletionCandidate> {

    private final String value;
    private final List<String> jAnsiCodes = [];

    ReflectionCompletionCandidate(String value, String... jAnsiCodes) {
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
            return -1;
        } else {
            return 1;
        }

    }

    @Override
    String toString() {
        return value;
    }

    @Override
    int hashCode() {
        return value.hashCode()
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ReflectionCompletionCandidate that = (ReflectionCompletionCandidate) o

        if (value != that.value) return false

        return true
    }
}
