package org.codehaus.groovy.syntax;


/**
 *  Provides the common code for <code>{@link TokenStream}</code> implementations.
 */

public abstract class AbstractTokenStream
    implements TokenStream
{
    private Token[] buf;             // A circular buffer of tokens
    private int first;               // la(1) points to this offset into buf
    private int avail;               // the number of ahead tokens in buf

    private int checkpoint_first;    // last checkpoint() copy of first
    private int checkpoint_avail;    // last checkpoint() copy of avail
 
    private String sourceLocator;    // A descriptor of the source of the stream

   
   /**
    *  Default constructor.
    */

    public AbstractTokenStream()
    {
        this( "<unknown>" );
    }


   /**
    *  Initializes the stream with information about the source.
    */

    public AbstractTokenStream(String sourceLocator)
    {
        this.buf           = new Token[8 * 1024];
        this.first         = -1;
        this.avail         = 0;
        this.sourceLocator = sourceLocator;
    }


   /**
    *  Returns a description of the source (typically a file name).
    */

    public String getSourceLocator()
    {
        return this.sourceLocator;
    }


   /**
    *  Implemented by concrete subtypes, provides access to the next
    *  token in the underlying stream.
    */

    protected abstract Token nextToken()
        throws ReadException, SyntaxException;


   /**
    *  Returns the next token in the stream without consuming it.
    */

    public Token la()
        throws ReadException, SyntaxException
    {
        return la( 1 );
    }


   /**
    *  Returns the <code>k</code>th token in the stream without consuming
    *  it (or any other unconsumed tokens).
    */

    public Token la(int k)
        throws ReadException, SyntaxException
    {
        if ( k > buf.length )
        {
            throw new LookAheadExhaustionError( k );   // Parser tried to look too far ahead for our buffer size
        }


        //
        // If necessary, read more tokens from the underlying stream.

        if ( k > this.avail )
        {
            int numToPopulate = k - this.avail;

            for ( int i = 0 ; i < numToPopulate ; ++i )
            {
                if ( this.first < 0 )
                {
                    this.first = 0;
                }
                int pop = ( ( this.first + this.avail ) % this.buf.length );
                this.buf[ pop ] = nextToken();
                ++this.avail;
            }
        }


        //
        // Return the requested token.

        int pos = ( ( k + this.first - 1 ) % this.buf.length );

        return this.buf[ pos ];
    }


   /**
    *  Removes and returns the first token in the stream, provided it
    *  matches the specified type.
    */

    public Token consume(int type)
        throws ReadException, SyntaxException
    {
        Token token = la();

        if ( token.getType() != type )
        {
            throw new TokenMismatchException( token, type );
        }

        ++this.first;
        --this.avail;

        this.first %= this.buf.length;

        return token;
    }


   /**
    *  Saves the look-ahead state for <code>restore()</code>ing later.
    */

    public void checkpoint() {
        checkpoint_first = first;
        checkpoint_avail = avail;
    }


   /**
    *  Restores the look-ahead state saved by <code>checkpoint()</code>.
    */

    public void restore() {
        first = checkpoint_first;
        avail = checkpoint_avail;
    }
}
