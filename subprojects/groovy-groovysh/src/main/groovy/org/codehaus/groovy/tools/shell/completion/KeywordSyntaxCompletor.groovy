package org.codehaus.groovy.tools.shell.completion

import org.codehaus.groovy.antlr.GroovySourceToken

/**
 * Completor completing variable and method names from known variables in the shell
 */
public class KeywordSyntaxCompletor implements IdentifierCompletor {

    final String[] KEYWORDS = [
            "abstract",
            "assert", "boolean", "break", "byte",
            "case",
            // "catch (", // special
            "char", "class", "continue",
            "def", // short, but keep, else "default" completes, annoyingly
            "default",
            "do",
            "double",
            "else", "enum", "extends",
            //"false",// value
            "final",
            //"finally {", // special
            "float",
            //"for (", // special
            //"if (", // special
            //"import",
            "in",
            "instanceof",
            "int", // short, but keeping for consistency, all primitives
            "interface",
            "long",
            //"native",
            "new",
            //"null", // value
            "private", "protected", "public",
            "return", "short",
            "static",
            //"super",// value
            //"switch (", // special
            "synchronized",
            //"this", // value
            //threadsafe,
            "throw", "throws",
            "transient",
            //"true", // value
            //"try {", //special
            "void", "volatile"
            //"while (" // special
    ]

    // VALUE_KEYWORDS and SPECIAL_FUNCTIONS completed without added blank
    final String[] VALUE_KEYWORDS = [
            "true",
            "false",
            "this",
            "super",
            "null"]

    final String[] SPECIAL_FUNCTIONS = [
            "catch (",
            "finally {",
            "for (",
            "if (",
            "switch (",
            "try {",
            "while ("]

    @Override
    public boolean complete(final List<GroovySourceToken> tokens, List candidates) {
        String prefix = tokens.last().getText()
        boolean foundMatch = false
        for (String varName in KEYWORDS) {
            if (varName.startsWith(prefix)) {
                candidates << varName + " "
                foundMatch = true
            }
        }
        for (String varName in VALUE_KEYWORDS) {
            if (varName.startsWith(prefix)) {
                candidates << varName
                foundMatch = true
            }
        }
        for (String varName in SPECIAL_FUNCTIONS) {
            if (varName.startsWith(prefix)) {
                candidates << varName
                foundMatch = true
            }
        }
        return foundMatch
    }
}
