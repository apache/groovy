package org.codehaus.groovy.syntax.lexer;

import org.codehaus.groovy.syntax.ReadException;
import org.codehaus.groovy.syntax.Numbers;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.syntax.Token;

/**
 *  The core code used in lexing Groovy.
 *
 *  @author Bob Mcwhirter
 *  @author James Strachan
 *  @author John Wilson
 *  @author Chris Poirier
 */

public class GroovyLexerBase extends LexerBase
{

    protected StringLexer  stringLexer  = new StringLexer();   // support lexer for processing strings
    protected GStringLexer gstringLexer = new GStringLexer();  // support lexer for processing GStrings


   /**
    *  Finds and returns (and consumes) the next token from the underlying stream.
    *  Returns null when out of tokens.
    */

    public Token nextToken() throws ReadException, LexerException
    {
        // System.out.println( "entering GroovyLexerBase.nextToken() on " + this );

        Token token = null;
        OUTER_LOOP : while (token == null)
        {

            //
            // Get from the delegate, if available

            if( delegate != null )
            {
                token = delegate.nextToken();

                if( token == null )
                {
                    undelegate();
                }
                else
                {
                    break OUTER_LOOP;
                }
            }


            //
            // Otherwise, do it the hard way.

            char c = la();

            ROOT_SWITCH : switch (c)
            {
                case (CharStream.EOS) :
                {
                    break OUTER_LOOP;
                }
                case (' ') :
                case ('\t') :
                {
                    consume();
                    token = null;
                    break ROOT_SWITCH;
                }
                case ('\r') :
                case ('\n') :
                {
                    mark();
                    token = tokenizeEOL();
                    break ROOT_SWITCH;
                }
                case ('{') :
                {
                    mark();
                    consume();
                    token = symbol( Types.LEFT_CURLY_BRACE );
                    break ROOT_SWITCH;
                }
                case ('}') :
                {
                    mark();
                    consume();
                    token = symbol( Types.RIGHT_CURLY_BRACE );
                    break ROOT_SWITCH;
                }
                case ('[') :
                {
                    mark();
                    consume();
                    token = symbol( Types.LEFT_SQUARE_BRACKET );
                    break ROOT_SWITCH;
                }
                case (']') :
                {
                    mark();
                    consume();
                    token = symbol( Types.RIGHT_SQUARE_BRACKET );
                    break ROOT_SWITCH;
                }
                case ('(') :
                {
                    mark();
                    consume();
                    token = symbol( Types.LEFT_PARENTHESIS );
                    break ROOT_SWITCH;
                }
                case (')') :
                {
                    mark();
                    consume();
                    token = symbol( Types.RIGHT_PARENTHESIS );
                    break ROOT_SWITCH;
                }
                case ('#') :
                {
                    consume();

                    token = symbol( Types.NEWLINE, -1 );

                    CONSUME_LOOP : while( true )
                    {
                        switch (c = la())
                        {
                            case ('\r') :
                            case ('\n') :
                            {
                                readEOL();
                                break CONSUME_LOOP;
                            }
                            case CharStream.EOS :
                            {
                                break CONSUME_LOOP;
                            }
                            default :
                            {
                                consume();
                            }
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ('/') :
                {
                    mark();
                    consume();

                    c = la();

                    MULTICHAR_SWITCH : switch (c)
                    {
                        case ('=') :
                        {
                            consume();
                            token = symbol( Types.DIVIDE_EQUAL );
                            break MULTICHAR_SWITCH;
                        }
                        case ('/') :
                        {
                            consume();
                            token = symbol( Types.NEWLINE, -2 );

                            CONSUME_LOOP : while (true)
                            {
                                switch (c = la())
                                {
                                    case ('\r') :
                                    case ('\n') :
                                    {
                                        readEOL();
                                        break CONSUME_LOOP;
                                    }
                                    case CharStream.EOS :
                                    {
                                        break CONSUME_LOOP;
                                    }
                                    default :
                                    {
                                        consume();
                                    }
                                }
                            }
                            break MULTICHAR_SWITCH;
                        }
                        case ('*') :
                        {
                            CONSUME_LOOP : while (true)
                            {
                                CONSUME_SWITCH : switch (c = la())
                                {
                                    case ('*') :
                                    {
                                        consume();
                                        if (la() == '/')
                                        {
                                            consume();
                                            break CONSUME_LOOP;
                                        }
                                        break CONSUME_SWITCH;
                                    }
                                    case ('\r') :
                                    case ('\n') :
                                    {
                                        readEOL();
                                        break CONSUME_SWITCH;
                                    }
                                    case CharStream.EOS :
                                    {
                                        break CONSUME_LOOP;
                                    }
                                    default :
                                    {
                                        consume();
                                    }
                                }
                            }
                            token = null;
                            break MULTICHAR_SWITCH;
                        }
                        default :
                        {
                            token = symbol( Types.DIVIDE );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ('%') :
                {
                    mark();
                    consume();

                    c = la();

                    MULTICHAR_SWITCH : switch (c)
                    {
                        case ('=') :
                        {
                            consume();
                            token = symbol( Types.MOD_EQUAL );
                            break MULTICHAR_SWITCH;
                        }
                        default :
                        {
                            token = symbol( Types.MOD );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ('~') :
                {
                    mark();
                    consume();

                    token = symbol( Types.REGEX_PATTERN );
                    break ROOT_SWITCH;
                }
                case ('!') :
                {
                    mark();
                    consume();

                    c = la();

                    MULTICHAR_SWITCH : switch (c)
                    {
                        case ('=') :
                        {
                            consume();
                            if( la() == '=' )
                            {
                                consume();
                                token = symbol( Types.COMPARE_NOT_IDENTICAL );
                            }
                            else
                            {
                                token = symbol( Types.COMPARE_NOT_EQUAL );
                            }
                            break MULTICHAR_SWITCH;
                        }
                        default :
                        {
                            token = symbol( Types.NOT );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ('=') :
                {
                    mark();
                    consume();

                    c = la();

                    MULTICHAR_SWITCH : switch (c)
                    {
                        case ('=') :
                        {
                            consume();
                            c = la();

                            switch (c)
                            {
                                case '=' :
                                {
                                    consume();
                                    token = symbol( Types.COMPARE_IDENTICAL );
                                    break;
                                }
                                case '~' :
                                {
                                    consume();
                                    token = symbol( Types.MATCH_REGEX );
                                    break;
                                }
                                default :
                                {
                                    token = symbol( Types.COMPARE_EQUAL );
                                }
                            }
                            break MULTICHAR_SWITCH;
                        }
                        case '~' :
                        {
                            consume();
                            token = symbol( Types.FIND_REGEX );
                            break MULTICHAR_SWITCH;
                        }
                        default :
                        {
                            token = symbol( Types.EQUAL );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ('&') :
                {
                    mark();
                    consume();

                    c = la();

                    MULTICHAR_SWITCH : switch (c)
                    {
                        case ('&') :
                        {
                            consume();

                            if( la() == '=' )
                            {
                                consume();
                                token = symbol( Types.LOGICAL_AND_EQUAL );
                            }
                            else
                            {
                                token = symbol( Types.LOGICAL_AND );
                            }

                            break MULTICHAR_SWITCH;
                        }
                        default :
                        {
                            unexpected( c, new char[] { '&' }, 1 );
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ('|') :
                {
                    mark();
                    consume();
                    c = la();

                    MULTICHAR_SWITCH : switch (c)
                    {
                        case ('|') :
                        {
                            consume();

                            if( la() == '=' )
                            {
                                consume();
                                token = symbol( Types.LOGICAL_OR_EQUAL );
                            }
                            else
                            {
                                token = symbol( Types.LOGICAL_OR );
                            }

                            break MULTICHAR_SWITCH;
                        }
                        default :
                        {
                            token = symbol( Types.PIPE );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ('+') :
                {
                    mark();
                    consume();

                    c = la();

                    MULTICHAR_SWITCH : switch (c)
                    {
                        case ('+') :
                        {
                            consume();
                            token = symbol( Types.PLUS_PLUS );
                            break MULTICHAR_SWITCH;
                        }
                        case ('=') :
                        {
                            consume();
                            token = symbol( Types.PLUS_EQUAL );
                            break MULTICHAR_SWITCH;
                        }
                        default :
                        {
                            token = symbol( Types.PLUS );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ('-') :
                {
                    mark();
                    consume();

                    c = la();

                    MULTICHAR_SWITCH : switch (c)
                    {
                        case ('-') :
                        {
                            consume();
                            token = symbol( Types.MINUS_MINUS );
                            break MULTICHAR_SWITCH;
                        }
                        case ('=') :
                        {
                            consume();
                            token = symbol( Types.MINUS_EQUAL );
                            break MULTICHAR_SWITCH;
                        }
                        case ('>') :
                        {
                            consume();
                            token = symbol( Types.NAVIGATE );
                            break MULTICHAR_SWITCH;
                        }
                        default :
                        {
                            token = symbol( Types.MINUS );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ('*') :
                {
                    mark();
                    consume();

                    c = la();

                    MULTICHAR_SWITCH : switch (c)
                    {
                        case ('=') :
                        {
                            consume();
                            token = symbol( Types.MULTIPLY_EQUAL );
                            break MULTICHAR_SWITCH;
                        }
                        default :
                        {
                            token = symbol( Types.MULTIPLY );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case (':') :
                {
                    mark();
                    consume();

                    token = symbol( Types.COLON );
                    break ROOT_SWITCH;
                }
                case (',') :
                {
                    mark();
                    consume();
                    token = symbol( Types.COMMA );
                    break ROOT_SWITCH;
                }
                case (';') :
                {
                    mark();
                    consume();
                    token = symbol( Types.SEMICOLON );
                    break ROOT_SWITCH;
                }
                case ('?') :
                {
                    mark();
                    consume();
                    token = symbol( Types.QUESTION );
                    break ROOT_SWITCH;
                }
                case ('<') :
                {
                    mark();
                    consume();

                    c = la();

                    MULTICHAR_SWITCH : switch (c)
                    {
                        case ('=') :
                        {
                            consume();
                            c = la();
                            if (c == '>')
                            {
                                consume();
                                token = symbol( Types.COMPARE_TO );
                            }
                            else
                            {
                                token = symbol( Types.COMPARE_LESS_THAN_EQUAL );
                            }
                            break MULTICHAR_SWITCH;
                        }
                        case ('<') :
                        {
                            consume();
                            c = la();

                            //
                            // It's a "here-doc", created using <<<TOK ... \nTOK.   The terminator
                            // runs from the <<< to the end of the line.  The marker is then used
                            // to create a HereDocLexer which becomes our delegate until the heredoc
                            // is finished.

                            if (c == '<')
                            {
                                consume();

                                StringBuffer marker = new StringBuffer();
                                while( (c = la()) != '\n' && c != '\r' && c != CharStream.EOS )
                                {
                                    marker.append( consume() );
                                }

                                readEOL();

                                Lexer child = new HereDocLexer( marker.toString() );
                                delegate( child );

                                gstringLexer.reset();
                                child.delegate( gstringLexer );

                                break ROOT_SWITCH;
                            }
                            else
                            {
                                token = symbol( Types.LEFT_SHIFT );
                                break ROOT_SWITCH;
                            }
                        }
                        default :
                        {
                            token = symbol( Types.COMPARE_LESS_THAN );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ('>') :
                {
                    mark();
                    consume();

                    c = la();

                    MULTICHAR_SWITCH : switch (c)
                    {
                        case ('=') :
                        {
                            consume();
                            token = symbol( Types.COMPARE_GREATER_THAN_EQUAL );
                            break MULTICHAR_SWITCH;
                        }
                        case ('>') :
                        {
                            consume();
                            if( la() == '>' )
                            {
                                consume();
                                token = symbol( Types.RIGHT_SHIFT_UNSIGNED );
                            } 
                            else
                            {	
                            	token = symbol( Types.RIGHT_SHIFT );
                            }
                            break MULTICHAR_SWITCH;
                        }
                        default :
                        {
                            token = symbol( Types.COMPARE_GREATER_THAN );
                            break MULTICHAR_SWITCH;
                        }
                    }
                    break ROOT_SWITCH;
                }
                case ('\'') :
                {
                    mark();

                    stringLexer.reset();
                    stringLexer.allowGStrings(false);
                    delegate( stringLexer );

                    break ROOT_SWITCH;
                }
                case ('"') :
                {
                    mark();

                    stringLexer.reset();
                    stringLexer.allowGStrings(true);
                    delegate( stringLexer );

                    gstringLexer.reset();
                    stringLexer.delegate( gstringLexer );

                    break ROOT_SWITCH;
                }
                case ('0') :
                case ('1') :
                case ('2') :
                case ('3') :
                case ('4') :
                case ('5') :
                case ('6') :
                case ('7') :
                case ('8') :
                case ('9') :
                case ('.') :
                {
                    mark();

                    //
                    // If it is a '.' and not followed by a digit,
                    // it's an operator.

                    if( c == '.' && !Numbers.isDigit(la(2)) )
                    {
                        consume();
                        if( la() == '.' )
                        {
                            consume();
                            if( la() == '.' )
                            {
                                consume();
                                token = symbol( Types.DOT_DOT_DOT );
                            }
                            else
                            {
                                token = symbol( Types.DOT_DOT );
                            }
                        }
                        else
                        {
                            token = symbol( Types.DOT );
                        }
                        break ROOT_SWITCH;
                    }


                    //
                    // Otherwise, we are processing a number (integer or decimal).

                    StringBuffer numericLiteral = new StringBuffer();
                    boolean      isDecimal      = false;


                    //
                    // If it starts 0 and isn't a decimal number, we give
                    // special handling for hexadecimal or octal notation.

                    char c2 = la(2);
                    if( c == '0' && (c2 == 'X' || c2 == 'x' || Numbers.isDigit(c2)) )
                    {
                        numericLiteral.append( consume() );

                        if( (c = la()) == 'X' || c == 'x' )
                        {
                            numericLiteral.append( consume() );
                            if( Numbers.isHexDigit(la()) )
                            {
                                while( Numbers.isHexDigit(la()) )
                                {
                                    numericLiteral.append( consume() );
                                }
                            }
                            else
                            {
                                unexpected( la(), numericLiteral.length(), "expected hexadecimal digit" );
                            }
                        }
                        else
                        {
                            while( Numbers.isOctalDigit(la()) )
                            {
                                numericLiteral.append( consume() );
                            }

                            if( Numbers.isDigit(la()) )
                            {
                                unexpected( la(), numericLiteral.length(), "expected octal digit" );
                            }
                        }
                    }


                    //
                    // Otherwise, it's in base 10, integer or decimal.

                    else
                    {
                        while( Numbers.isDigit(la()) )
                        {
                            numericLiteral.append( consume() );
                        }


                        //
                        // Next, check for a decimal point

                        if( la() == '.' && Numbers.isDigit(la(2)) )
                        {
                            isDecimal = true;

                            numericLiteral.append( consume() );
                            while( Numbers.isDigit(la()) )
                            {
                                numericLiteral.append( consume() );
                            }

                            //
                            // Check for an exponent

                            if( (c = la()) == 'e' || c == 'E' )
                            {
                                numericLiteral.append( consume() );

                                if (la() == '+' || la() == '-')
                                {
                                    numericLiteral.append(consume());
                                }

                                if( Numbers.isDigit(la()) )
                                {
                                    while( Numbers.isDigit(la()) )
                                    {
                                        numericLiteral.append( consume() );
                                    }
                                }
                                else
                                {
                                    unexpected( la(), numericLiteral.length(), "expected exponent" );
                                }
                            }
                        }
                    }


                    //
                    // If there is a type suffix, include it.

                    if( Numbers.isNumericTypeSpecifier(la(), isDecimal) )
                    {
                        numericLiteral.append( consume() );
                    }


                    //
                    // For good error reporting, make sure there is nothing invalid next.

                    if( Character.isJavaIdentifierPart(c = la()) )
                    {
                        unexpected( c, numericLiteral.length(), "expected end of numeric literal" );
                    }


                    //
                    // Finally, create the token.

                    if( isDecimal )
                    {
                        token = Token.newDecimal( numericLiteral.toString(), getStartLine(), getStartColumn() );
                    }
                    else
                    {
                        token = Token.newInteger( numericLiteral.toString(), getStartLine(), getStartColumn() );
                    }

                    break ROOT_SWITCH;
                }
                default :
                {
                    mark();
                    if (Character.isJavaIdentifierStart(c))
                    {
                        StringBuffer identifier = new StringBuffer();

                        IDENTIFIER_LOOP : while (true)
                        {
                            c = la();

                            if (Character.isJavaIdentifierPart(c))
                            {
                                identifier.append(consume());
                            }
                            else
                            {
                                break IDENTIFIER_LOOP;
                            }
                        }

                        String text = identifier.toString();
                        token = Token.newKeyword( text, getStartLine(), getStartColumn() );

                        if (token == null)
                        {
                            token = Token.newIdentifier( text, getStartLine(), getStartColumn() );
                        }
                    }
                    else
                    {
                        unexpected( c, 1 );
                    }

                    break ROOT_SWITCH;
                }
            }
        }

        // System.out.println( "" + this + ".nextToken() returning [" + token + "]" );

        return token;
    }

}
