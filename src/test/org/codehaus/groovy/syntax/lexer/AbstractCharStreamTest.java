package org.codehaus.groovy.syntax.lexer;

import org.codehaus.groovy.syntax.LookAheadExhaustionException;

import groovy.lang.GroovyTestCase;

import java.io.IOException;

public class AbstractCharStreamTest
    extends GroovyTestCase
{
    public static class MockCharStream
        extends AbstractCharStream
    {
        private int cur;
        private String text;

        public MockCharStream(String text)
        {
            this.text = text;
            this.cur  = 0;
        }

        public MockCharStream(String text,
                              String description)
        {
            super( description );
            this.text = text;
            this.cur  = 0;
        }

        public char nextChar()
            throws IOException
        {
            if ( this.cur >= this.text.length() )
            {
                return (char) -1;
            }

            char c = this.text.charAt( this.cur );

            ++this.cur;

            return c;
        }

        public void close()
            throws IOException
        {

        }
    }

    public void testConstruct_Default()
    {
        assertEquals( "<unknown>",
                      new MockCharStream( "cheddar" ).getDescription() );
    }

    public void testConstruct_WithDescription()
    {
        assertEquals( "/path/to/Cheddar.groovy",
                      new MockCharStream( "cheddar",
                                          "/path/to/Cheddar.groovy" ).getDescription() );
    }

    public void testNextChar()
        throws Exception
    {
        MockCharStream in = new MockCharStream( "cheddar" );

        assertEquals( 'c',
                      in.nextChar() );

        assertEquals( 'h',
                      in.nextChar() );

        assertEquals( 'e',
                      in.nextChar() );

        assertEquals( 'd',
                      in.nextChar() );

        assertEquals( 'd',
                      in.nextChar() );

        assertEquals( 'a',
                      in.nextChar() );

        assertEquals( 'r',
                      in.nextChar() );

        assertEquals( (char) -1,
                      in.nextChar() );
    }

    public void testLa()
        throws Exception
    {
        MockCharStream in = new MockCharStream( "cheddar" );

        assertEquals( 'c',
                      in.la() );

        assertEquals( 'c',
                      in.la() );

        assertEquals( 'c',
                      in.la( 1 ) );

        assertEquals( 'c',
                      in.la( 1 ) );

        assertEquals( 'h',
                      in.la( 2 ) );

        assertEquals( 'h',
                      in.la( 2 ) );

        assertEquals( 'e',
                      in.la( 3 ) );

        assertEquals( 'e',
                      in.la( 3 ) );
    }

    public void testLaAndConsume()
        throws Exception
    {
        MockCharStream in = new MockCharStream( "cheddar" );

        assertEquals( 'c',
                      in.la() );

        assertEquals( 'c',
                      in.consume() );

        assertEquals( 'h',
                      in.la() );

        assertEquals( 'h',
                      in.consume() );
        
        assertEquals( 'e',
                      in.la() );

        assertEquals( 'e',
                      in.consume() );
        
        assertEquals( 'd',
                      in.la() );

        assertEquals( 'd',
                      in.consume() );
        
        assertEquals( 'd',
                      in.la() );

        assertEquals( 'd',
                      in.consume() );
        
        assertEquals( 'a',
                      in.la() );

        assertEquals( 'a',
                      in.consume() );
        
        assertEquals( 'r',
                      in.la() );

        assertEquals( 'r',
                      in.consume() );

        assertEquals( (char) -1,
                      in.la() );

        assertEquals( (char) -1,
                      in.consume() );
        
    }

    public void testLaAndConsumeAtEnd()
        throws Exception
    {
        MockCharStream in = new MockCharStream( "" );

        assertEquals( (char) -1,
                      in.la() );

        assertEquals( (char) -1,
                      in.consume() );

        assertEquals( (char) -1,
                      in.la() );

        assertEquals( (char) -1,
                      in.consume() );
    }

    public void testExhaustLookAhead()
        throws Exception
    {
        MockCharStream in = new MockCharStream( "123456789" );

        assertEquals( '1',
                      in.la( 1 ) );

        assertEquals( '2',
                      in.la( 2 ) );

        assertEquals( '3',
                      in.la( 3 ) );

        assertEquals( '4',
                      in.la( 4 ) );

        assertEquals( '5',
                      in.la( 5 ) );

        in.consume();

        assertEquals( '6',
                      in.la( 5 ) );

        try
        {
            in.la( 6 );
            fail( "should have thrown LookAheadExhaustionException" );
        }
        catch (LookAheadExhaustionException e)
        {
            // expected and correct
            assertEquals( 6,
                          e.getLookAhead() );
        }
    }
}
