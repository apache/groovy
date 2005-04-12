package org.codehaus.groovy.syntax;


import groovy.util.GroovyTestCase;
import org.codehaus.groovy.GroovyBugError;


public class AbstractTokenStreamTest
    extends GroovyTestCase
{
    public static class MockTokenStream
        extends AbstractTokenStream
    {
        private int cur;
        private int checkpoint;
        private Token[] tokens;

        public MockTokenStream(Token[] tokens)
        {
            this.tokens = tokens;
            this.cur  = 0;
        }

        public Token nextToken()
            throws ReadException, SyntaxException
        {
            if ( this.cur >= this.tokens.length )
            {
                return null;
            }

            Token token = this.tokens[ this.cur ];

            ++this.cur;

            return token;
        }

        public void checkpoint() {
            checkpoint = cur;
        }

        public void restore() {
            cur = checkpoint;
        }
    }

    public void testNextToken()
        throws Exception
    {
        Token[] tokens = new Token[]
            {
                Token.newSymbol( "(", 1, 1 ),
                Token.newSymbol( ")", 1, 2 )
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
                Token.newSymbol( "(", 1, 1 ),
                Token.newSymbol( ")", 1, 2 )
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
                Token.newSymbol( "(", 1, 1 ),
                Token.newSymbol( ")", 1, 2 )
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

        in.consume( Types.LEFT_PARENTHESIS );

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
                Token.newIdentifier( "cheeseIt", 1, 1 ),
                Token.newSymbol( "(", 1, 10 ),
                Token.newSymbol( ")", 1, 11 )
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
                Token.newSymbol( "(", 1, 1 ),
                Token.newSymbol( ")", 1, 2 ),
                Token.newSymbol( "[", 1, 3 ),
                Token.newSymbol( "]", 1, 4 ),
                Token.newSymbol( "{", 1, 5 ),
                Token.newSymbol( "}", 1, 6 )
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
            fail( "should have thrown GroovyBugError" );
        }
        catch (GroovyBugError e)
        {
            // expected and correct
//            assertEquals( 6,
//                          e.getLookAhead() );
        }
    }

    public void testTokenMismatch()
        throws Exception
    {
        Token[] tokens = new Token[]
            {
                Token.newSymbol( "(", 1, 1 ),
                Token.newSymbol( ")", 1, 2 )
            };
        
        MockTokenStream in = new MockTokenStream( tokens );
        
        try
        {
            in.consume( Types.RIGHT_PARENTHESIS );
            fail( "should have thrown TokenMismatchException" );
        }
        catch (TokenMismatchException e)
        {
            // expected and correct

            assertSame( tokens[0],
                        e.getUnexpectedToken() );

            assertEquals( Types.RIGHT_PARENTHESIS,
                          e.getExpectedType() );
        }
    }
}
