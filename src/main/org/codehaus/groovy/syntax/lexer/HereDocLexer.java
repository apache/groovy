package org.codehaus.groovy.syntax.lexer;

import org.codehaus.groovy.syntax.ReadException;

/**
 *  A Lexer for processing here docs.  It reads a line at a time from
 *  the underlying stream (leaving the EOL for the next read), then
 *  offers that data for users.
 *
 *  @author Chris Poirier
 */

public class HereDocLexer extends TextLexerBase
{

    protected String  marker   = null;   // The marker to watch for
    protected boolean onmargin = true;   // If false, the marker can be indented
    protected String  data     = "";     // The current data
    protected int     consumed = -1;     // The last index consumed
    protected boolean last     = false;  // Set after the last line is read


   /**
    *  Initializes the lexer to read up to (and including) the marker
    *  on a line by itself.
    */

    public HereDocLexer( String marker )
    {
        if( marker.startsWith("-") )
        {
            this.marker = marker.substring( 1, marker.length() );
            this.onmargin = false;
        }
        else
        {
            this.marker = marker;
            this.onmargin = true;
        }
    }



   /**
    *  Sets the source lexer and sets the lexer running.
    */

    public void setSource( Lexer source )
    {
        super.setSource( source );

        data     = "";
        consumed = -1;
        last     = false;

        restart();
        delimit( true );
    }



   /**
    *  Unsets the source lexer.
    */

    public void unsetSource()
    {
        finish();
        super.unsetSource();
    }



   /**
    *  Sets delimiting on.  The first thing we to is check for and eat our
    *  delimiter.
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
                    finish();
                }
            }
            catch( Exception e )
            {
                finished = true;
            }
        }
    }




   /**
    *  Returns the next <code>k</code>th character, without consuming any.
    */

    public char la(int k) throws LexerException, ReadException
    {

        if( !finished && source != null )
        {
            if( consumed + k >= data.length() )
            {
                refill();
            }

            if( consumed + k < data.length() )
            {
                return data.charAt( consumed + k );
            }
        }

        return CharStream.EOS;
    }




   /**
    *  Eats a character from the input stream.  Searches for the delimiter if
    *  filtered.  Note that turning delimiting on also checks if we are at the
    *  delimiter, so if we aren't finished, there is something to consume.
    */

    public char consume() throws LexerException, ReadException
    {
        if( !finished && source != null )
        {
            char c = data.charAt( ++consumed );
            if( delimited && la(1) == CharStream.EOS )
            {
                finish();
            }

            return c;
        }

        return CharStream.EOS;
    }



   /**
    *  Reads the next line from the underlying stream.  If delimited, checks for
    *  the marker.  We don't update finished here, though, as that would prevent
    *  any buffered data from being read.
    */

    protected void refill() throws LexerException, ReadException
    {
        if( !finished && source != null && !last )
        {
            StringBuffer read = new StringBuffer();

            //
            // Read any residual data into the buffer.

            for( int i = consumed + 1; i < data.length(); i++ )
            {
                read.append( data.charAt(i) );
            }


            //
            // Read line ends until we have some non-blank lines to read.
            // Note that we have to be careful with the line ends, as the
            // end of one line belongs to the next, when it comes to discards
            // due to marker identification!

            char c;
            StringBuffer raw = new StringBuffer();
            while( (c = source.la()) == '\n' || c == '\r' )
            {
                if( raw.length() > 0 )
                {
                    read.append( raw );
                    raw.setLength( 0 );
                }

                if( !((LexerBase)source).readEOL(raw) ) // bad cast, but for now...
                {
                    throw new UnterminatedStringLiteralException(getStartLine(), getStartColumn());
                }
            }


            //
            // Read the next line, checking for the end marker, if delimited.
            // We leave the EOL for the next read...

            boolean use = true;

            if( !isDelimited() )
            {
                while( (c = source.la()) != '\n' && c != '\r' && c != CharStream.EOS )
                {
                    raw.append( source.consume() );
                }
            }

            else
            {
                //
                // If the marker started with the "-" modifier, whitespace is
                // allowed before the marker.  The marker can be followed on
                // the same line by code, so if it matches the beginning
                // pattern, we stop after reading the last character.

                if( !onmargin )
                {
                    while( (c = source.la()) == ' ' || c == '\t' )
                    {
                        raw.append( c );
                    }
                }

                int testing = 0, length = marker.length();
                boolean found = false, lost = false;
                while( (c = source.la()) != '\n' && c != '\r' && c != CharStream.EOS && !found )
                {
                    if( !lost && c == marker.charAt(testing) )
                    {
                        testing++;
                        if( testing == length )
                        {
                            found = true;
                        }
                    }
                    else
                    {
                        lost = true;
                    }

                    raw.append( source.consume() );
                }

                if( found )
                {
                    use  = false;
                }
            }


            //
            // It's either our delimiter or a line of data.

            if( use )
            {
                read.append( raw );
            }
            else
            {
                last = true;
            }


            data = read.toString();
            consumed = -1;
        }
    }

}
