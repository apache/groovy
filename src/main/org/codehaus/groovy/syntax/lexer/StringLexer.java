package org.codehaus.groovy.syntax.lexer;

//{{{ imports
import org.codehaus.groovy.syntax.ReadException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.GroovyBugError;
//}}}

/**
 *  A Lexer for processing standard strings.
 *
 *  @author Chris Poirier
 */

public class StringLexer extends TextLexerBase
{

    protected String  delimiter = null;
    protected char    watchFor;
    protected boolean allowGStrings = false;
    protected boolean emptyString   = true;   // If set, we need to send an empty string


   /**
    *  If set true, the filter will allow \\ and \$ to pass through unchanged.
    *  You should set this appropriately BEFORE setting source!
    */

    public void allowGStrings( boolean allow )
    {
        allowGStrings = allow;
    }



   /**
    *  Returns a single STRING, then null.   The STRING is all of the processed
    *  input.  Backslashes are stripped, with the \r, \n, and \t converted
    *  appropriately.
    */

    public Token undelegatedNextToken( ) throws ReadException, LexerException
    {
        if( emptyString )
        {
            emptyString = false;
            return Token.newString( "", getStartLine(), getStartColumn() );
        }
        else if( finished )
        {
            return null;
        }
        else
        {
            StringBuffer string = new StringBuffer();

            while( la(1) != CharStream.EOS )
            {
                string.append( consume() );
            }

            return Token.newString( string.toString(), getStartLine(), getStartColumn() );
        }
    }



   /**
    *  Controls delimiter search.  When turned on, the first thing we do
    *  is check for and eat our delimiter.
    */

    public void delimit( boolean delimit )
    {
        super.delimit( delimit );

        if( delimit )
        {
            try
            {
                if( !finished && la(1) == CharStream.EOS )
                {
                    finishUp();

                    //
                    // The GStringLexer will correctly handle the empty string.
                    // We don't.  In order to ensure that an empty string is
                    // supplied, we set a flag that is checked during
                    // undelegatedNextToken().

                    if( !allowGStrings )
                    {
                        emptyString = true;
                    }
                }
            }
            catch( Exception e )
            {
                finished = true;
            }
        }
    }




   /**
    *  Sets the source lexer and identifies and consumes the opening delimiter.
    */

    public void setSource( Lexer source )
    {
        super.setSource( source );

        emptyString = false;

        try
        {
            char c = source.la();
            switch( c )
            {
                case '\'':
                case '"':
                    mark();
                    source.consume();

                    if( source.la() == c && source.la(2) == c )
                    {
                        source.consume(); source.consume();
                        delimiter = new StringBuffer().append(c).append(c).append(c).toString();
                    }
                    else
                    {
                        delimiter = new StringBuffer().append(c).toString();
                    }

                    watchFor = delimiter.charAt(0);
                    break;


                default:
                {
                    throw new GroovyBugError( "at the time of StringLexer.setSource(), the source must be on a single or double quote" );
                }
            }

            restart();
            delimit( true );
        }
        catch( Exception e )
        {
            //
            // If we couldn't read our delimiter, we'll just
            // cancel our source.  nextToken() will return null.

            e.printStackTrace();
            unsetSource( );
        }
    }



   /**
    *  Unsets our source.
    */

    public void unsetSource()
    {
        super.unsetSource();
        delimiter   = null;
        finished    = true;
        emptyString = false;
    }




  //---------------------------------------------------------------------------
  // STREAM ROUTINES

    private int    lookahead  = 0;             // the number of characters identified
    private char[] characters = new char[3];   // the next characters identified by la()
    private int[]  widths     = new int[3];    // the source widths of the next characters



   /**
    *  Returns the next <code>k</code>th character, without consuming any.
    */

    public char la(int k) throws LexerException, ReadException
    {

        if( !finished && source != null )
        {

            if( delimited )
            {

                if( k > characters.length )
                {
                    throw new GroovyBugError( "StringLexer lookahead tolerance exceeded" );
                }

                if( lookahead >= k )
                {
                    return characters[k-1];
                }

                lookahead = 0;

                char c = ' ', c1 = ' ', c2 = ' ';
                int offset = 1, width = 0;
                for( int i = 1; i <= k; i++ )
                {
                    c1 = source.la(offset);
                    C1_SWITCH: switch( c1 )
                    {
                        case CharStream.EOS:
                        {
                            return c1;
                        }

                        case '\\':
                        {
                            c2 = source.la( offset + 1 );

                            ESCAPE_SWITCH: switch( c2 )
                            {

                                case CharStream.EOS:
                                    return c2;

                                case '\\':
                                case '$':
                                {
                                    if( allowGStrings )
                                    {
                                        c = c1;
                                        width = 1;
                                    }
                                    else
                                    {
                                        c = c2;
                                        width = 2;
                                    }
                                    break ESCAPE_SWITCH;
                                }

                                case 'r':
                                    c = '\r';
                                    width = 2;
                                    break ESCAPE_SWITCH;

                                case 't':
                                    c = '\t';
                                    width = 2;
                                    break ESCAPE_SWITCH;

                                case 'n':
                                    c = '\n';
                                    width = 2;
                                    break ESCAPE_SWITCH;


                                default:
                                    c = c2;
                                    width = 2;
                                    break ESCAPE_SWITCH;
                            }
                            break C1_SWITCH;
                        }

                        default:
                        {
                            if( c1 == watchFor )
                            {
                                boolean atEnd = true;
                                for( int j = 1; j < delimiter.length(); j++ )
                                {
                                    if( source.la(offset+j) != delimiter.charAt(j) )
                                    {
                                        atEnd = false;
                                        break;
                                    }
                                }

                                if( atEnd )
                                {
                                    return CharStream.EOS;
                                }
                            }

                            c = c1;
                            width = 1;
                            break C1_SWITCH;
                        }
                    }


                    characters[lookahead] = c;
                    widths[lookahead]     = width;

                    offset += width;
                    lookahead += 1;
                }

                return c;                                         // <<< FLOW CONTROL <<<<<<<<<
            }

            lookahead = 0;
            return source.la(k);
        }

        return CharStream.EOS;

    }



   /**
    *  Eats a character from the input stream.  Searches for the delimiter if
    *  delimited.  Note that turning delimiting on also checks if we are at the
    *  delimiter, so if we aren't finished, there is something to consume.
    */

    public char consume() throws LexerException, ReadException
    {
        if( !finished && source != null )
        {
            char c = CharStream.EOS;

            if( delimited )
            {
                if( lookahead < 1 )
                {
                    la( 1 );
                }

                if( lookahead >= 1 )
                {
                    c = characters[0];
                    for( int i = 0; i < widths[0]; i++ )
                    {
                        source.consume();
                    }

                    lookahead = 0;
                }

                if( la(1) == CharStream.EOS )
                {
                    finishUp();
                }
            }
            else
            {
                c = source.consume();
            }

            lookahead = 0;
            return c;
        }

        return CharStream.EOS;
    }



   /**
    *  Eats our delimiter from the stream and marks us finished.
    */

    protected void finishUp() throws LexerException, ReadException
    {
        for( int i = 0; i < delimiter.length(); i++ )
        {
            char c = source.la(1);
            if( c == CharStream.EOS )
            {
                throw new UnterminatedStringLiteralException(getStartLine(), getStartColumn());
            }
            else if( c == delimiter.charAt(i) )
            {
                source.consume();
            }
            else
            {
                throw new GroovyBugError( "la() said delimiter [" + delimiter + "], finishUp() found [" + c + "]" );
            }
        }

        finish();
    }

}
