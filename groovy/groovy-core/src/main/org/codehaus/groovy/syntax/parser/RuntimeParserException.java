package org.codehaus.groovy.syntax.parser;

import groovy.lang.GroovyRuntimeException;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.syntax.SyntaxException;

/** 
 * A helper class to allow parser exceptions to be thrown anywhere in the code. 
 * Should be replaced when no longer required.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */ 
public class RuntimeParserException extends GroovyRuntimeException {
    
    public RuntimeParserException(String message, ASTNode node) {
        super(message + ". Node: " + node, node);
    }

    public void throwParserException() throws SyntaxException {
        throw new SyntaxException(getMessage(), getNode().getLineNumber(), getNode().getColumnNumber());
    }
    
    /*
    private Token token;

    public RuntimeParserException(String message, Token token) {
        super(message);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    public void throwParserException() throws SyntaxException {
        throw new TokenException(getMessage(), token);
    }
    */
}
