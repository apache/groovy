package org.codehaus.groovy.syntax.lexer;

import org.codehaus.groovy.GroovyTestCase;

public class StringCharStreamTest
    extends GroovyTestCase
{
    public void testNextChar_EmptyString()
        throws Exception
    {
        StringCharStream charStream = new StringCharStream( "" );

        assertEquals( CharStream.EOS,
                      charStream.nextChar() );
        assertEquals( CharStream.EOS,
                      charStream.nextChar() );
    }

    public void testNextChar_NonEmptyString()
        throws Exception
    {
        StringCharStream charStream = new StringCharStream( "cheese" );

        assertEquals( 'c',
                      charStream.nextChar() );
        assertEquals( 'h',
                      charStream.nextChar() );
        assertEquals( 'e',
                      charStream.nextChar() );
        assertEquals( 'e',
                      charStream.nextChar() );
        assertEquals( 's',
                      charStream.nextChar() );
        assertEquals( 'e',
                      charStream.nextChar() );
        assertEquals( CharStream.EOS,
                      charStream.nextChar() );
        assertEquals( CharStream.EOS,
                      charStream.nextChar() );
    }
}
