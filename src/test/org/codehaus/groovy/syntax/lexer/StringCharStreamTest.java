package org.codehaus.groovy.syntax.lexer;

import groovy.util.GroovyTestCase;

public class StringCharStreamTest
    extends GroovyTestCase
{
    public void testNextChar_EmptyString()
        throws Exception
    {
        StringCharStream charStream = new StringCharStream( "" );

        assertEquals( CharStream.EOS,
                      charStream.consume() );
        assertEquals( CharStream.EOS,
                      charStream.consume() );
    }

    public void testconsume_NonEmptyString()
        throws Exception
    {
        StringCharStream charStream = new StringCharStream( "cheese" );

        assertEquals( 'c',
                      charStream.consume() );
        assertEquals( 'h',
                      charStream.consume() );
        assertEquals( 'e',
                      charStream.consume() );
        assertEquals( 'e',
                      charStream.consume() );
        assertEquals( 's',
                      charStream.consume() );
        assertEquals( 'e',
                      charStream.consume() );
        assertEquals( CharStream.EOS,
                      charStream.consume() );
        assertEquals( CharStream.EOS,
                      charStream.consume() );
    }
}
