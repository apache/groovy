package org.codehaus.groovy.syntax;


import groovy.util.GroovyTestCase;

import java.io.IOException;

public class AbstractTokenStreamTest
    extends GroovyTestCase
{
    public static class MockTokenStream
        extends AbstractTokenStream
    {
        private int cur;
        private Token[] tokens;

        public MockTokenStream(Token[] tokens)
        {
            this.tokens = tokens;
            this.cur  = 0;
        }

        public Token nextToken()
            throws IOException, SyntaxException
        {
            if ( this.cur >= this.tokens.length )
            {
                return null;
            }

            Token token = this.tokens[ this.cur ];

            ++this.cur;

            return token;
        }
    }

    public void testNextToken()
        throws Exception
    {
        Token[] tokens = new Token[]
            {
                Token.leftParenthesis( 1, 1 ),
                Token.rightParenthesis( 1, 2 )
            };

        MockTokenStream in = new MockTokenStream( tokens );


        assertSame( tokens[0],
                    in.nextToken() );
        
        assertSame( tokens[1],
                    in.nextToken() );

        assertNull( in.nextToken() );

    }

    public void testLa()
        throws Exception
    {
        Token[] tokens = new Token[]
            {
                Token.leftParenthesis( 1, 1 ),
                Token.rightParenthesis( 1, 2 )
            };

        MockTokenStream in = new MockTokenStream( tokens );


        assertSame( tokens[0],
                    in.la() );
        
        assertSame( tokens[0],
                    in.la() );

        assertSame( tokens[0],
                    in.la( 1 ) );
        
        assertSame( tokens[0],
                    in.la( 1 ) );

        assertSame( tokens[1],
                    in.la( 2 ) );
        
        assertSame( tokens[1],
                    in.la( 2 ) );
    }

    public void testLaAndConsume()
        throws Exception
    {
        Token[] tokens = new Token[]
            {
                Token.leftParenthesis( 1, 1 ),
                Token.rightParenthesis( 1, 2 )
            };
        
        MockTokenStream in = new MockTokenStream( tokens );
        
        assertSame( tokens[0],
                    in.la() );
        
        assertSame( tokens[0],
                    in.la() );
        
        assertSame( tokens[0],
                    in.la( 1 ) );
        
        assertSame( tokens[0],
                    in.la( 1 ) );
        
        assertSame( tokens[1],
                    in.la( 2 ) );

        in.consume( Token.LEFT_PARENTHESIS );

        assertSame( tokens[1],
                    in.la() );
        
        assertSame( tokens[1],
                    in.la() );
        
        assertSame( tokens[1],
                    in.la( 1 ) );
        
        assertSame( tokens[1],
                    in.la( 1 ) );
    }

    public void testLaOutOfOrder()
        throws Exception
    {
        Token[] tokens = new Token[]
            {
                Token.identifier( 1,
                                  1,
                                  "cheeseIt" ),
                Token.leftParenthesis( 1,
                                       10 ),
                Token.rightParenthesis( 1,
                                        11 )
            };

        MockTokenStream in = new MockTokenStream( tokens );

        assertSame( tokens[2],
                    in.la( 3 ) );

        assertSame( tokens[1],
                    in.la( 2 ) );

        assertSame( tokens[0],
                    in.la( 1 ) );
    }

    public void testLaAtEnd()
        throws Exception
    {
        Token[] tokens = new Token[]
            {
            };
        
        MockTokenStream in = new MockTokenStream( tokens );
        
        assertNull( in.la() );

        assertNull( in.la() );

        assertNull( in.la() );
    }

    /** 
     * this test is broken as we have a large look-ahead token buffer now 
     * to handle newlines. if we supported mid-stream consumption 
     * (e.g. consumeAtIndex(3) then we could avoid such a large buffer
     */
    public void DISABLED_testExhaustLookAhead()
        throws Exception
    {
        Token[] tokens = new Token[]
            {
                Token.leftParenthesis( 1, 1 ),
                Token.rightParenthesis( 1, 2 ),
                Token.leftSquareBracket( 1, 3 ),
                Token.rightSquareBracket( 1, 4 ),
                Token.leftCurlyBrace( 1, 5 ),
                Token.rightCurlyBrace( 1, 6 )
            };
        
        MockTokenStream in = new MockTokenStream( tokens );
        
        assertSame( tokens[0],
                    in.la() );
        
        assertSame( tokens[1],
                    in.la( 2 ) );

        assertSame( tokens[2],
                    in.la( 3 ) );

        assertSame( tokens[3],
                    in.la( 4 ) );

        assertSame( tokens[4],
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

    public void testTokenMismatch()
        throws Exception
    {
        Token[] tokens = new Token[]
            {
                Token.leftParenthesis( 1, 1 ),
                Token.rightParenthesis( 1, 2 )
            };
        
        MockTokenStream in = new MockTokenStream( tokens );
        
        try
        {
            in.consume( Token.RIGHT_PARENTHESIS );
            fail( "should have thrown TokenMismatchException" );
        }
        catch (TokenMismatchException e)
        {
            // expected and correct

            assertSame( tokens[0],
                        e.getToken() );

            assertEquals( Token.RIGHT_PARENTHESIS,
                          e.getExpectedType() );
        }
    }
}
