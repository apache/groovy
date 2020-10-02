package org.apache.groovy.gradle

import groovy.transform.CompileStatic

@CompileStatic
class GroovyAllExtension {
    final List<String> excludedFromJavadocs = []

    void excludeFromJavadoc(String... items) {
        Collections.addAll(excludedFromJavadocs, items)
    }
}
