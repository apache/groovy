package org.codehaus.groovy.syntax.lexer;

import org.codehaus.groovy.syntax.ReadException;
import org.codehaus.groovy.GroovyBugError;


/**
 *  Identifies and returns tokens from a source text.  <code>nextToken()</code>
 *  is the primary entry point.  This is the primary lexer for the Groovy language.
 *  It can delegate operations, but will not accept being delegated to.
 *
 *  @author Bob Mcwhirter
 *  @author James Strachan
 *  @author John Wilson
 *  @author Chris Poirier
 */

public class GroovyLexer extends GroovyLexerBase
{

    private CharStream charStream;           // the source of data for the lexer
    protected int line;                      // the current line in the source
    protected int column;                    // the current column in the source



   /**
    *  Initializes the <code>Lexer</code> from an opened <code>CharStream</code>.
    */

    public GroovyLexer(CharStream charStream)
    {
        this.charStream = charStream;
        this.line = 1;
        this.column = 1;
    }


   /**
    *  Returns the underlying <code>CharStream</code>.
    */

    public CharStream getCharStream()
    {
        return this.charStream;
    }



   /**
    *  Refuses to set a source.
    */

    public void setSource( Lexer source )
    {
        throw new GroovyBugError( "you can't set a source on the GroovyLexer" );
    }



   /**
    *  Similarly refuses to clear a source.
    */

    public void unsetSource()
    {
        throw new GroovyBugError( "you can't unset a source on the GroovyLexer" );
    }




  //---------------------------------------------------------------------------
  // STREAM PROCESSING

    private final char[] buf       = new char[5];          // ??
    private final int[]  charWidth = new int[buf.length];  // ??

    private int cur = 0;                                   // ??
    private int charsInBuffer = 0;                         // ??
    private boolean eosRead = false;                       // ??
    private boolean escapeLookahead = false;               // ??
    private char escapeLookaheadChar;                      // ??

    private boolean boundary = false;                      // set true when the lexer is on a line boundary



   /**
    *  Returns the current line number.
    */

    public int getLine()
    {
        return line;
    }



   /**
    *  Returns the current column within that line.
    */

    public int getColumn()
    {
        return column;
    }



   /**
    *  Returns the next <code>k</code>th character, without consuming any.
    */

    public char la(int k) throws LexerException, ReadException
    {
        if (k > this.charsInBuffer)
        {
            if( k > this.buf.length )
            {
                throw new GroovyBugError( "Could not look ahead for character: " + k + " due to buffer exhaustion" );
            }

            for (int i = 0; i != this.charsInBuffer; i++, this.cur++)
            {
               this.buf[i] = this.buf[this.cur];
               this.charWidth[i] = this.charWidth[this.cur];
            }

            fillBuffer();
        }

        return this.buf[this.cur + k - 1];
    }



   /**
    *  Eats a character from the input stream.  We don't
    *  support sources here, as we own the CharStream on which
    *  we are working.
    */

    public char consume() throws LexerException, ReadException
    {
        if (this.charsInBuffer == 0)
        {
            fillBuffer();
        }


        //
        // Consume the next character

        this.charsInBuffer--;

        int  width   = this.charWidth[this.cur];
        char c       = this.buf[this.cur++];
        this.column += width;


        //
        // Mark line boundaries as necessary.  Only relevant
        // non-manufactured tokens need apply.

        if( boundary || (c == '\n' && width == 1) )
        {
            boundary = false;
            line++;
            column = 1;
        }
        else if( c == '\r' && width == 1 )
        {
            if( la(1) != '\n' )
            {
                line++;
                column = 1;
            }
            else /* it is '\n' and */ if( this.charWidth[this.cur] == 1 )
            {
                boundary = true;
            }
        }


        return c;
    }



   /**
    *  Fills the lookahead buffer from the stream.
    */

    private void fillBuffer() throws ReadException, LexerException
    {
        this.cur = 0;

        do
        {
            if( this.eosRead )
            {
                this.buf[this.charsInBuffer] = CharStream.EOS;
            }
            else
            {
                char c = this.escapeLookahead ? this.escapeLookaheadChar : charStream.consume();

                this.escapeLookahead = false;
                this.charWidth[this.charsInBuffer] = 1;

                if(c == CharStream.EOS)
                {
                    this.eosRead = true;
                }

                if( c == '\\' )
                {
                    c = charStream.consume();

                    if( c == 'u' )
                    {
                        do
                        {
                            this.charWidth[this.charsInBuffer]++;
                            c = charStream.consume();
                        }
                        while (c == 'u'); // the spec allows any number of u characters after the \	

                        try
                        {
                            c =
                                (char) Integer.parseInt(
                                    new String(
                                        new char[] {
                                            c,
                                            charStream.consume(),
                                            charStream.consume(),
                                            charStream.consume()    }),
                                    16);
                            this.charWidth[this.charsInBuffer] += 4;
                        }
                        catch (NumberFormatException e)
                        {
                            throw new UnexpectedCharacterException(
                                getStartLine(),
                                getStartColumn() + 1,
                                c,
                                new char[] {
                            });
                        }
                    }
                    else
                    {
                        this.escapeLookahead = true;
                        this.escapeLookaheadChar = c;
                        c = '\\';
                    }
                }

                this.buf[this.charsInBuffer] = c;
            }
        }
        while (++this.charsInBuffer != this.buf.length);
    }

}
