package org.codehaus.groovy.syntax.lexer;

import java.io.IOException;

import org.codehaus.groovy.syntax.Token;

public class Lexer
{
    private int line;
    private int column;

    private int startLine;
    private int startColumn;
    
    private CharStream charStream;

    public Lexer(CharStream charStream)
    {
        this.charStream = charStream;
        this.line   = 1;
        this.column = 1;
    }

    public CharStream getCharStream()
    {
        return this.charStream;
    }

    public Token nextToken()
        throws IOException, LexerException
    {
        Token token = null;

      OUTER_LOOP:
        while ( token == null )
        {
            char c = la();

          ROOT_SWITCH:
            switch ( c )
            {
                case ( CharStream.EOS ):
                {
                    break OUTER_LOOP;
                }
                case ( ' ' ):
                case ( '\t' ):
                {
                    consume();
                    token = null;
                    break ROOT_SWITCH;
                }
                case ( '\r' ):
                {
                    consume();
                    if ( la() == '\n' )
                    {
                        consume();
                    }
                    eol();
                    token = null;
                    break ROOT_SWITCH;
                }
                case ( '\n' ):
                {
                    consume();
                    if ( la() == '\r')
                    {
                        consume();
                    }
                    eol();
                    token = null;
                    break ROOT_SWITCH;
                }
                case ( '{' ):
                {
                    mark();
                    consume();
                    token = Token.leftCurlyBrace( getStartLine(),
                                                  getStartColumn() );
                    break ROOT_SWITCH;
                }
                case ( '}' ):
                {
                    mark();
                    consume();
                    token = Token.rightCurlyBrace( getStartLine(),
                                                   getStartColumn() );
                    break ROOT_SWITCH;
                }
                case ( '[' ):
                {
                    mark();
                    consume();
                    token = Token.leftSquareBracket( getStartLine(),
                                                     getStartColumn() );
                    break ROOT_SWITCH;
                }
                case ( ']' ):
                {
                    mark();
                    consume();
                    token = Token.rightSquareBracket( getStartLine(),
                                                      getStartColumn() );
                    break ROOT_SWITCH;
                }
                case ( '(' ):
                {
                    mark();
                    consume();
                    token = Token.leftParenthesis( getStartLine(),
                                                   getStartColumn() );
                    break ROOT_SWITCH;
                }
                case ( ')' ):
                {
                    mark();
                    consume();
                    token = Token.rightParenthesis( getStartLine(),
                                                    getStartColumn() );
                    break ROOT_SWITCH;
                }
                case ( '.' ):
                {
                    mark();
                    consume();
                    if ( la() == '.' )
                    {
                        consume();
                        token = Token.dotDot( getStartLine(),
                                              getStartColumn() );
                    }
                    else
                    {
                        token = Token.dot( getStartLine(),
                                           getStartColumn() );
                    }
                    break ROOT_SWITCH;
                }
                case ( '#' ):
                {
                    consume();

                  CONSUME_LOOP:
                    while ( true )
                    {
                        switch ( la() )
                        {
                            case ( '\r' ):
                            {
                                consume();
                                if ( la() == '\n' )
                                {
                                    consume();
                                }
                                eol();
                                break CONSUME_LOOP;
                            }
                            case ( '\n' ):
                            {
                                consume();
                                eol();
                                break CONSUME_LOOP;
                            }
                            default:
                            {
                                consume();
                            }
                        }
                    }
                    token = null;
                    break ROOT_SWITCH;
                }
                case ( '/' ):
                {
                    mark();
                    consume();

                    c = la();

                  MULTICHAR_SWITCH:
                    switch ( c )
                    {
                        case ( '=' ):
                        {
                            consume();
                            token = Token.divideEqual( getStartLine(),
                                                       getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                        case ( '/' ):
                        {
                            consume();

                          CONSUME_LOOP:
                            while ( true )
                            {
                                switch ( la() )
                                {
                                    case ( '\r' ):
                                    {
                                        consume();
                                        if ( la() == '\n' )
                                        {
                                            consume();
                                        }
                                        eol();
                                        break CONSUME_LOOP;
                                    }
                                    case ( '\n' ):
                                    {
                                        consume();
                                        eol();
                                        break CONSUME_LOOP;
                                    }
                                    default:
                                    {
                                        consume();
                                    }
                                }
                            }
                            token = null;
                            break MULTICHAR_SWITCH;
                        }
                        case ( '*' ):
                        {
                          CONSUME_LOOP:
                            while ( true )
                            {
                              CONSUME_SWITCH:
                                switch ( la() )
                                {
                                    case ( '*' ):
                                    {
                                        consume();
                                        if ( la() == '/' )
                                        {
                                            consume();
                                            break CONSUME_LOOP;
                                        }
                                        break CONSUME_SWITCH;
                                    }
                                    case ( '\r' ):
                                    {
                                        consume();
                                        if ( la() == '\n' )
                                        {
                                            consume();
                                        }
                                        eol();
                                        break CONSUME_SWITCH;
                                    }
                                    case ( '\n' ):
                                    {
                                        eol();
                                        consume();
                                        break CONSUME_SWITCH;
                                    }
                                    default:
                                    {
                                        consume();
                                    }
                                }
                            }
                            token = null;
                            break MULTICHAR_SWITCH;
                        }
                        default:
                        {
                            token = Token.divide( getStartLine(),
                                                  getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ( '%' ):
                {
                    mark();
                    consume();

                    c = la();

                  MULTICHAR_SWITCH:
                    switch ( c )
                    {
                        case ( '=' ):
                        {
                            consume();
                            token = Token.modEqual( getStartLine(),
                                                    getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                        default:
                        {
                            token = Token.mod( getStartLine(),
                                               getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ( '!' ):
                {
                    mark();
                    consume();

                    c = la();

                  MULTICHAR_SWITCH:
                    switch ( c )
                    {
                        case ( '=' ):
                        {
                            consume();
                            token = Token.compareNotEqual( getStartLine(),
                                                           getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                        default:
                        {
                            token = Token.not( getStartLine(),
                                               getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ( '=' ):
                {
                    mark();
                    consume();

                    c = la();

                  MULTICHAR_SWITCH:
                    switch ( c )
                    {
                        case ( '=' ):
                        {
                            consume();
                            c = la();

                            if ( c == '=' )
                            {
                                consume();
                                token = Token.compareIdentical( getStartLine(),
                                                                getStartColumn() );
                            }
                            else
                            {
                                token = Token.compareEqual( getStartLine(),
                                                            getStartColumn() );
                                                                         
                            }
                            break MULTICHAR_SWITCH;
                        }
                        default:
                        {
                            token = Token.equal( getStartLine(),
                                                 getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ( '&' ):
                {
                    mark();
                    consume();

                    c = la();

                  MULTICHAR_SWITCH:
                    switch ( c )
                    {
                        case ( '&' ):
                        {
                            consume();
                            token = Token.logicalAnd( getStartLine(),
                                                      getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                        default:
                        {
                            throw new UnexpectedCharacterException( getStartLine(),
                                                                    getStartColumn()+1,
                                                                    c,
                                                                    new char[] { '&' } );
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ( '|' ):
                {
                    mark();
                    consume();
                    c = la();
                    
                  MULTICHAR_SWITCH:
                    switch ( c )
                    {
                        case ( '|' ):
                        {
                            consume();
                            token = Token.logicalOr( getStartLine(),
                                                     getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                        default:
                        {
                            token = Token.pipe( getStartLine(),
                                                getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ( '+' ):
                {
                    mark();
                    consume();
                    
                    c = la();

                  MULTICHAR_SWITCH:
                    switch ( c )
                    {
                        case ( '+' ):
                        {
                            consume();
                            token = Token.plusPlus( getStartLine(),
                                                    getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                        case ( '=' ):
                        {
                            consume();
                            token = Token.plusEqual( getStartLine(),
                                                     getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                        default:
                        {
                            token = Token.plus( getStartLine(),
                                                getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ( '-' ):
                {
                    mark();
                    consume();

                    c = la();

                  MULTICHAR_SWITCH:
                    switch ( c )
                    {
                        case ( '-' ):
                        {
                            consume();
                            token = Token.minusMinus( getStartLine(),
                                                      getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                        case ( '=' ):
                        {
                            consume();
                            token = Token.minusEqual( getStartLine(),
                                                      getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                        default:
                        {
                            token = Token.minus( getStartLine(),
                                                 getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ( '*' ):
                {
                    mark();
                    consume();

                    c = la();

                  MULTICHAR_SWITCH:
                    switch( c )
                    {
                        case ( '=' ):
                        {
                            consume();
                            token = Token.multiplyEqual( getStartLine(),
                                                         getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                        default:
                        {
                            token = Token.multiply( getStartLine(),
                                                    getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ( ':' ):
                {
                    mark();
                    consume();

                    token = Token.colon( getStartLine(),
                                         getStartColumn() );
                    break ROOT_SWITCH;
                }
                case ( ',' ):
                {
                    mark();
                    consume();
                    token = Token.comma( getStartLine(),
                                         getStartColumn() );
                    break ROOT_SWITCH;
                }
                case ( ';' ):
                {
                    mark();
                    consume();
                    token = Token.semicolon( getStartLine(),
                                             getStartColumn() );
                    break ROOT_SWITCH;
                }
                case ( '?' ):
                {
                    mark();
                    consume();
                    token = Token.question( getStartLine(),
                                            getStartColumn() );
                    break ROOT_SWITCH;
                }
                case ( '<' ):
                {
                    mark();
                    consume();

                    c = la();

                  MULTICHAR_SWITCH:
                    switch ( c )
                    {
                        case ( '=' ):
                        {
                            consume();
                            token = Token.compareLessThanEqual( getStartLine(),
                                                                getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                        default:
                        {
                            token = Token.compareLessThan( getStartLine(),
                                                           getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ( '>' ):
                {
                    mark();
                    consume();

                    c = la();

                  MULTICHAR_SWITCH:
                    switch ( c )
                    {
                        case ( '=' ):
                        {
                            consume();
                            token = Token.compareGreaterThanEqual( getStartLine(),
                                                                   getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                        default:
                        {
                            token = Token.compareGreaterThan( getStartLine(),
                                                              getStartColumn() );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ( '\'') :
                {
                    mark();
                    consume();
                    
                    StringBuffer stringLiteral = new StringBuffer();
                    
                  LITERAL_LOOP:
                    while ( true )
                    {
                        c = la();
                        
                      LITERAL_SWITCH:
                        switch ( c )
                        {
                            case ( '\\' ):
                            {
                                consume();

                                c = la();

                              ESCAPE_SWITCH:
                                switch ( c )
                                {
                                    case ( 't' ):
                                    {
                                        consume();
                                        stringLiteral.append( '\t' );
                                        break ESCAPE_SWITCH;
                                    }
                                    case ( 'n' ):
                                    {
                                        consume();
                                        stringLiteral.append( '\n' );
                                        break ESCAPE_SWITCH;
                                    }
                                    case ( 'r' ):
                                    {
                                        consume();
                                        stringLiteral.append( '\r' );
                                        break ESCAPE_SWITCH;
                                    }
                                    default:
                                    {
                                        stringLiteral.append( consume() );
                                        break ESCAPE_SWITCH;
                                    }
                                }
                                break LITERAL_SWITCH;
                            }
                            case ( '\r' ):
                            case ( '\n' ):
                            case ( CharStream.EOS ):
                            {
                                throw new UnterminatedStringLiteralException( getStartLine(),
                                                                              getStartColumn() );
                            }
                            case ( '\'' ):
                            {
                                consume();
                                break LITERAL_LOOP;
                            }
                            default:
                            {
                                stringLiteral.append( consume() );
                                break LITERAL_SWITCH;
                            }
                        }
                    }

                    token = Token.singleQuoteString( getStartLine(),
                                                     getStartColumn(),
                                                     stringLiteral.toString() );

                    break ROOT_SWITCH;
                }
                case ( '"' ):
                {
                    mark();
                    consume();

                    StringBuffer stringLiteral = new StringBuffer();

                  LITERAL_LOOP:
                    while ( true )
                    {
                        c = la();

                      LITERAL_SWITCH:
                        switch ( c )
                        {
                            case ( '\\' ):
                            {
                                consume();

                                c = la();

                              ESCAPE_SWITCH:
                                switch ( c )
                                {
                                    case ( 't' ):
                                    {
                                        consume();
                                        stringLiteral.append( '\t' );
                                        break ESCAPE_SWITCH;
                                    }
                                    case ( 'n' ):
                                    {
                                        consume();
                                        stringLiteral.append( '\n' );
                                        break ESCAPE_SWITCH;
                                    }
                                    case ( 'r' ):
                                    {
                                        consume();
                                        stringLiteral.append( '\r' );
                                        break ESCAPE_SWITCH;
                                    }
                                    default:
                                    {
                                        stringLiteral.append( consume() );
                                        break ESCAPE_SWITCH;
                                    }
                                }
                                break LITERAL_SWITCH;
                            }
                            case ( '"' ):
                            {
                                consume();
                                break LITERAL_LOOP;
                            }
                            case ( '\r' ):
                            case ( '\n' ):
                            case ( CharStream.EOS ):
                            {
                                throw new UnterminatedStringLiteralException( getStartLine(),
                                                                              getStartColumn() );
                            }
                            default:
                            {
                                stringLiteral.append( consume() );
                                break LITERAL_SWITCH;
                            }
                        }
                    }

                    token = Token.doubleQuoteString( getStartLine(),
                                                     getStartColumn(),
                                                     stringLiteral.toString() );

                    break ROOT_SWITCH;
                }
                case ( '0' ):
                case ( '1' ):
                case ( '2' ):
                case ( '3' ):
                case ( '4' ):
                case ( '5' ):
                case ( '6' ):
                case ( '7' ):
                case ( '8' ):
                case ( '9' ):
                {
                    mark();
                    StringBuffer numericLiteral = new StringBuffer();

                    boolean isFloat = false;

                    while ( c == '0'
                            ||
                            c == '1'
                            ||
                            c == '2'
                            ||
                            c == '3'
                            ||
                            c == '4'
                            ||
                            c == '5'
                            ||
                            c == '6'
                            ||
                            c == '7'
                            ||
                            c == '8'
                            ||
                            c == '9' )
                    {
                        numericLiteral.append( consume() );
                        c = la();
                    }

                    if ( c == '.' )
                    {
                        if ( la( 2 ) == '.' )
                        {
                            // int followed by range op, break out.
                        }
                        else
                        {
                            isFloat = true;
                            numericLiteral.append( consume() );
                            c = la();
                            boolean moreDigits = false;
                            while ( c == '0' 
                                    ||
                                    c == '1'
                                    ||
                                    c == '2'
                                    ||
                                    c == '3'
                                    ||
                                    c == '4'
                                    ||
                                    c == '5'
                                    ||
                                    c == '6'
                                    ||
                                    c == '7'
                                    ||
                                    c == '8'
                                    ||
                                    c == '9' )
                            {
                                moreDigits = true;
                                numericLiteral.append( consume() );
                                c = la();
                            }
                            
                            if ( ! moreDigits )
                            {
                                throw new UnexpectedCharacterException( getStartLine(),
                                                                        getStartColumn() + numericLiteral.length(),
                                                                        c,
                                                                        new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' } );
                            }
                        }
                    }

                    if ( isFloat )
                    {
                        token = Token.floatNumber( getStartLine(),
                                                   getStartColumn(),
                                                   numericLiteral.toString() );
                    }
                    else
                    {
                        token = Token.integerNumber( getStartLine(),
                                                     getStartColumn(),
                                                     numericLiteral.toString() );
                    }
                    break ROOT_SWITCH;
                }
                default:
                {
                    mark();
                    if ( Character.isJavaIdentifierStart( c ) )
                    {
                        StringBuffer identifier = new StringBuffer();
                        
                        identifier.append( consume() );
                        
                      IDENTIFIER_LOOP:
                        while ( true )
                        {
                            c = la();
                            
                            if ( Character.isJavaIdentifierPart( c ) )
                            {
                                identifier.append( consume() );
                            }
                            else
                            {
                                break IDENTIFIER_LOOP;
                            }
                        }

                        token = Token.keyword( getStartLine(),
                                               getStartColumn(),
                                               identifier.toString() );

                        if ( token == null )
                        {
                            token = Token.identifier( getStartLine(),
                                                      getStartColumn(),
                                                      identifier.toString() );
                        }
                    }
                    else
                    {
                        throw new UnexpectedCharacterException( getStartLine(),
                                                                getStartColumn()+1,
                                                                c,
                                                                new char[] { } );
                    }
                    break ROOT_SWITCH;
                }
            }
        }

        return token;
    }

    protected void eol()
    {
        ++this.line;
        this.column = 1;
    }

    protected void mark()
    {
        this.startLine = this.line;
        this.startColumn = this.column;
    }

    protected int getStartLine()
    {
        return this.startLine;
    }

    protected int getStartColumn()
    {
        return this.startColumn;
    }

    protected char la()
        throws IOException
    {
        return la(1);
    }

    protected char la(int k)
        throws IOException
    {
        return getCharStream().la( k );
    }

    protected char consume()
        throws IOException
    {
        char c = getCharStream().consume();
        ++this.column;
        return c;
    }
}
