package org.codehaus.groovy.syntax.parser;

import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.TokenException;

public class ParserException extends TokenException {
    public ParserException(String message, Token token) {
        super(message, token);
    }

}
