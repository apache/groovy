package org.codehaus.groovy.syntax.lexer;

import groovy.util.GroovyTestCase;

import org.codehaus.groovy.syntax.ReadException;

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

        public char consume()
            throws ReadException
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
            throws ReadException
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

    public void testconsume()
        throws Exception
    {
        MockCharStream in = new MockCharStream( "cheddar" );

        assertEquals( 'c',
                      in.consume() );

        assertEquals( 'h',
                      in.consume() );

        assertEquals( 'e',
                      in.consume() );

        assertEquals( 'd',
                      in.consume() );

        assertEquals( 'd',
                      in.consume() );

        assertEquals( 'a',
                      in.consume() );

        assertEquals( 'r',
                      in.consume() );

        assertEquals( (char) -1,
                      in.consume() );
    }

    public void testConsume()
        throws Exception
    {
        MockCharStream in = new MockCharStream( "cheddar" );

        assertEquals( 'c',
                      in.consume() );

        assertEquals( 'h',
                      in.consume() );

        assertEquals( 'e',
                      in.consume() );
        
        assertEquals( 'd',
                      in.consume() );

        assertEquals( 'd',
                      in.consume() );
        
        assertEquals( 'a',
                      in.consume() );

        assertEquals( 'r',
                      in.consume() );

        assertEquals( (char) -1,
                      in.consume() );
        
    }

    public void testConsumeAtEnd()
        throws Exception
    {
        MockCharStream in = new MockCharStream( "" );

        assertEquals( (char) -1,
                      in.consume() );

        assertEquals( (char) -1,
                      in.consume() );
    }
}
