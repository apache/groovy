package org.codehaus.groovy.syntax.lexer;

//{{{ imports
import org.codehaus.groovy.syntax.ReadException;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.syntax.Token;
//}}}

/**
 *  A lexer for GStrings, usually run on a LexerFilter base.
 *
 *  @author Chris Poirier
 */

public class GStringLexer extends LexerBase
{

    protected boolean sentStartToken      = false;
    protected boolean sentEndToken        = false;

    protected StringBuffer       fullText = new StringBuffer();
    protected int       fullTextStartLine = 0;
    protected int     fullTextStartColumn = 0;

    protected GroovyExpressionLexer child = null;
    protected boolean        inExpression = false;


   /**
    *  Finds and returns (consuming) the next token from the underlying stream.
    *  Returns null when out of tokens.
    */

    protected Token undelegatedNextToken() throws ReadException, LexerException
    {
        // System.out.println( "" + this + "undelegatedNextToken()" );
        Token token = null;


        //
        // Handle bracketing tokens and EOS

        if( !sentStartToken )
        {
            mark();
            fullTextStartLine   = getStartLine();
            fullTextStartColumn = getStartColumn();
            sentStartToken      = true;

            // System.out.println( "" + this + "returning GSTRING_START" );
            return symbol( Types.GSTRING_START );
        }
        else if( la(1) == CharStream.EOS )
        {
            if( !sentEndToken )
            {
                sentEndToken = true;
                token = Token.newSymbol( Types.GSTRING_END, fullTextStartLine, fullTextStartColumn );
                token.setText( fullText.toString() );
            }

            // System.out.println( "" + this + "returning " + token );
            return token;
        }


        //
        // If we get this far, we are no longer delegated.  If
        // we just processed an expression, the next character
        // had better be a '}'...

        if( inExpression && la(1) != '}' )
        {
            mark();
            unexpected( la(1), 0 );
        }


        //
        // Otherwise, it's a lex...

        mark();
        StringBuffer segment = new StringBuffer();

        char c;
        MAIN_LOOP: while( true )
        {
            c = la(1);

            ROOT_SWITCH: switch( c )
            {
                case CharStream.EOS:
                {
                    break MAIN_LOOP;
                }

                case '\r':
                case '\n':
                {
                    readEOL( segment );
                    break ROOT_SWITCH;
                }

                case '\\':
                {
                    ESCAPE_SWITCH: switch( la(2) )
                    {
                        case '\\':
                        case '$':
                        {
                            consume();
                            segment.append( consume() );
                            break ESCAPE_SWITCH;
                        }

                        default:
                        {
                            segment.append( consume() );
                            break ESCAPE_SWITCH;
                        }
                    }

                    break ROOT_SWITCH;
                }

                case '$':
                {
                    if( la(2) == '{' )
                    {
                        if( segment.length() == 0 )
                        {
                            sourceDelimiting( false );  // ensures ${"..."} works

                            mark();
                            consume();
                            consume();

                            token = symbol( Types.GSTRING_EXPRESSION_START );
                            inExpression = true;

                            if( child == null )
                            {
                                child = new GroovyExpressionLexer();
                            }
                            else
                            {
                                child.reset();
                            }

                            delegate( child );

                            break MAIN_LOOP;
                        }
                        else
                        {
                            break MAIN_LOOP;
                        }
                    }
                    else
                    {
                        segment.append( consume() );
                    }

                    break ROOT_SWITCH;
                }

                case '}':
                {
                    if( inExpression )
                    {
                        mark();
                        consume();
                        token = symbol( Types.GSTRING_EXPRESSION_END );

                        inExpression = false;

                        break MAIN_LOOP;
                    }
                    else
                    {
                        segment.append( consume() );
                        break ROOT_SWITCH;
                    }
                }

                default:
                {
                    segment.append( consume() );
                    break ROOT_SWITCH;
                }
            }
        }


        if( token != null )
        {
            // System.out.println( "" + this + "returning " + token );
            return token;
        }
        else
        {
            // System.out.println( "" + this + "returning string of " + segment );
            return Token.newString( segment.toString(), getStartLine(), getStartColumn() );
        }

    }




  //---------------------------------------------------------------------------
  // DELEGATION


   /**
    *  Coordinates with our source about delimiting.  When
    *  entering or processing sub-expressions, source delimiting
    *  should be off.
    */

    protected void sourceDelimiting( boolean delimit )
    {
        if( source instanceof Delimiter )
        {
            ((Delimiter)source).delimit( delimit );
        }
    }



   /**
    *  Delegates our duties to another Lexer.
    */

    public void delegate( Lexer to )
    {
        this.delegate = to;
        sourceDelimiting( false );
        to.setSource( this );
    }



   /**
    *  Retakes responsibility for our duties.
    */

    public void undelegate()
    {
        if( delegate != null )
        {
            delegate.unsetSource( );
            delegate = null;
            sourceDelimiting( true );
        }
    }



   /**
    *  Sets the source lexer.
    */

    public void setSource( Lexer source )
    {
        super.setSource( source );

        sentStartToken = false;
        sentEndToken   = false;

        fullTextStartLine   = getStartLine();
        fullTextStartColumn = getStartColumn();
        fullText            = new StringBuffer();

        inExpression = false;
    }



   /**
    *  Unsets the source lexer.
    */

    public void unsetSource()
    {
        super.unsetSource();

        sentStartToken = false;
        sentEndToken   = false;
        fullText       = null;
        inExpression   = false;
    }




  //---------------------------------------------------------------------------
  // STREAM ROUTINES


   /**
    *  Eats a character from the input stream.
    */

    public char consume() throws LexerException, ReadException
    {
        char c = super.consume();

        if( c != CharStream.EOS )
        {
            fullText.append(c);
        }

        return c;
    }


}

