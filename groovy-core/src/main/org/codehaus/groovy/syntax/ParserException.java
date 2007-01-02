package org.codehaus.groovy.syntax;

import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.TokenException;

public class ParserException extends TokenException {
    public ParserException(String message, Token token) {
        super(message, token);
    }

    public ParserException(String message, Throwable cause, int lineNumber, int columnNumber) {
        super(message, cause, lineNumber, columnNumber);
    }

}
