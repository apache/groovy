package org.codehaus.groovy.tools.shell.completion

import org.codehaus.groovy.antlr.GroovySourceToken

/**
 * Interface for classes that complete identifier tokens within a groovy Statement
 * (Class, variable, keyword, method, ...)
 * Similar to JLine Completor, but adapted for usage in GroovySyntaxCompletor
 */
public interface IdentifierCompletor {

    /**
     *
     * @param tokens List of tokens, non empty, last token is an identifier token, previous token is not a dot
     * @param candidates
     * @return
     */
    boolean complete(List<GroovySourceToken> tokens, List candidates);

}