package org.codehaus.groovy.syntax.parser;

import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;


public class ParserException
    extends SyntaxException
{
    public ParserException(String message) {
        super(message);
    }
    
    public ParserException(String message, Token token) {
        this(message + " at line: " + token.getStartLine() + " column: " + token.getStartColumn());
    }
}
