package org.codehaus.groovy.syntax.lexer;

import groovy.lang.GroovyTestCase;

import org.codehaus.groovy.syntax.Token;

public class LexerTest
    extends GroovyTestCase
{
    private Lexer lexer;

    public void testEndOfStream()
        throws Exception
    {
        newLexer( "" );

        assertEnd();

        assertEnd();

        assertEnd();

        assertEnd();
    }

    public void testSingleLineComment_Newline()
        throws Exception
    {
        newLexer( "// I like cheese\ncheese" );

        assertNextToken( Token.IDENTIFIER,
                         "cheese" );

        assertEnd();
    }

    public void testSingleLineComment_CarriageReturn()
        throws Exception
    {
        newLexer( "// I like cheese\rcheese" );

        assertNextToken( Token.IDENTIFIER,
                         "cheese" );

        assertEnd();
    }

    public void testSingleLineComment_CarriageReturn_Newline()
        throws Exception
    {
        newLexer( "// I like cheese\r\ncheese" );

        assertNextToken( Token.IDENTIFIER,
                         "cheese" );

        assertEnd();
    }

    public void testMultilineComment_MiddleOfLine()
        throws Exception
    {
        newLexer( "cheese /* is */ toasty" );

        assertNextToken( Token.IDENTIFIER,
                         "cheese" );

        assertNextToken( Token.IDENTIFIER,
                         "toasty" );

        assertEnd();
    }

    public void testMultilineComment_SpanningLines()
        throws Exception
    {
        newLexer( "cheese /* is \n really */ toasty" );

        assertNextToken( Token.IDENTIFIER,
                         "cheese" );

        assertNextToken( Token.IDENTIFIER,
                         "toasty" );

        assertEnd();
    }

    public void testMultilineComment_EmbeddedStarts()
        throws Exception
    {
        newLexer( "cheese /* * * * / * / */ toasty" );

        assertNextToken( Token.IDENTIFIER,
                         "cheese" );

        assertNextToken( Token.IDENTIFIER,
                         "toasty" );

        assertEnd();
    }

    public void testIgnoredWhitespace()
        throws Exception
    {
        newLexer( " \r \n \r\n \n\r    \t   \t");

        assertEnd();
    }

    public void testLeftCurlyBrace()
        throws Exception
    {
        assertSimple( "{",
                      Token.LEFT_CURLY_BRACE );
    }

    public void testRightCurlyBrace()
        throws Exception
    {
        assertSimple( "}",
                      Token.RIGHT_CURLY_BRACE );
    }

    public void testLeftSquareBracket()
        throws Exception
    {
        assertSimple( "[",
                      Token.LEFT_SQUARE_BRACKET );
    }

    public void testRightSquareBracket()
        throws Exception
    {
        assertSimple( "]",
                      Token.RIGHT_SQUARE_BRACKET );
    }

    public void testLeftParenthesis()
        throws Exception
    {
        assertSimple( "(",
                      Token.LEFT_PARENTHESIS );
    }

    public void testRightParenthesis()
        throws Exception
    {
        assertSimple( ")",
                      Token.RIGHT_PARENTHESIS );
    }

    public void testDot()
        throws Exception
    {
        assertSimple( ".",
                      Token.DOT );
    }

    public void testDotDot()
        throws Exception
    {
        assertSimple( "..",
                      Token.DOT_DOT );
    }

    public void testNot()
        throws Exception
    {
        assertSimple( "!",
                      Token.NOT );
    }

    public void testCompareNotEqual()
        throws Exception
    {
        assertSimple( "!=",
                      Token.COMPARE_NOT_EQUAL );
    }

    public void testEqual()
        throws Exception
    {
        assertSimple( "=",
                      Token.EQUAL );
    }

    public void testCompareEqual()
        throws Exception
    {
        assertSimple( "==",
                      Token.COMPARE_EQUAL );
    }

    public void testCompareIdentical()
        throws Exception
    {
        assertSimple( "===",
                      Token.COMPARE_IDENTICAL );
    }

    public void testCompareLessThan()
        throws Exception
    {
        assertSimple( "<",
                      Token.COMPARE_LESS_THAN );
    }

    public void testCompareLessThanEqual()
        throws Exception
    {
        assertSimple( "<=",
                      Token.COMPARE_LESS_THAN_EQUAL );
    }

    public void testCompareGreaterThan()
        throws Exception
    {
        assertSimple( ">",
                      Token.COMPARE_GREATER_THAN );
    }

    public void testCompareGreaterThanEqual()
        throws Exception
    {
        assertSimple( ">=",
                      Token.COMPARE_GREATER_THAN_EQUAL );
    }

    public void testLogicalOr()
        throws Exception
    {
        assertSimple( "||",
                      Token.LOGICAL_OR );
    }

    public void testPipe()
        throws Exception
    {
        assertSimple( "|",
                      Token.PIPE );
    }

    public void testLogicalAnd()
        throws Exception
    {
        assertSimple( "&&",
                      Token.LOGICAL_AND );
    }

    public void testAmpersand_UnexpectedCharacter()
        throws Exception
    {
        newLexer( "&a" );

        char[] expected = assertUnexpectedCharacter( 'a',
                                                     1,
                                                     2 );

        assertLength( 1,
                      expected );

        assertContains( '&',
                        expected );
    }

    public void testPlus()
        throws Exception
    {
        assertSimple( "+",
                      Token.PLUS );
    }

    public void testPlusPlus()
        throws Exception
    {
        assertSimple( "++",
                      Token.PLUS_PLUS );
    }

    public void testPlusEqual()
        throws Exception
    {
        assertSimple( "+=",
                      Token.PLUS_EQUAL );
    }

    public void testMinus()
        throws Exception
    {
        assertSimple( "-",
                      Token.MINUS );
    }

    public void testMinusMinus()
        throws Exception
    {
        assertSimple( "--",
                      Token.MINUS_MINUS );
    }

    public void testMinusEqual()
        throws Exception
    {
        assertSimple( "-=",
                      Token.MINUS_EQUAL );
    }

    public void testDivide()
        throws Exception
    {
        assertSimple( "/",
                      Token.DIVIDE );
    }

    public void testDivideEqual()
        throws Exception
    {
        assertSimple( "/=",
                      Token.DIVIDE_EQUAL );
    }

    public void testMod()
        throws Exception
    {
        assertSimple( "%",
                      Token.MOD );
    }

    public void testModEqual()
        throws Exception
    {
        assertSimple( "%=",
                      Token.MOD_EQUAL );
    }

    public void testMultiply()
        throws Exception
    {
        assertSimple( "*",
                      Token.MULTIPLY );
    }

    public void testMultiplyEqual()
        throws Exception
    {
        assertSimple( "*=",
                      Token.MULTIPLY_EQUAL );
    }

    public void testColon()
        throws Exception
    {
        assertSimple( ":",
                      Token.COLON );
    }

    public void testSemicolon()
        throws Exception
    {
        assertSimple( ";",
                      Token.SEMICOLON );
    }

    public void testQuestion()
        throws Exception
    {
        assertSimple( "?",
                      Token.QUESTION );
    }

    public void testDoubleQuoteString_Simple()
        throws Exception
    {
        newLexer( "\"cheese\"" );

        assertNextToken( Token.DOUBLE_QUOTE_STRING,
                         "cheese" );

        assertEnd();
    }

    public void testDoubleQuoteString_EscapedTab()
        throws Exception
    {
        newLexer( "\"che\\tese\"" );

        assertNextToken( Token.DOUBLE_QUOTE_STRING,
                         "che\tese" );

        assertEnd();
    }

    public void testDoubleQuoteString_EscapedNewline()
        throws Exception
    {
        newLexer( "\"che\\nese\"" );

        assertNextToken( Token.DOUBLE_QUOTE_STRING,
                         "che\nese" );

        assertEnd();
    }
    
    public void testDoubleQuoteString_EscapedCarriageReturn()
        throws Exception
    {
        newLexer( "\"che\\rese\"" );

        assertNextToken( Token.DOUBLE_QUOTE_STRING,
                         "che\rese" );

        assertEnd();
    }

    public void testDoubleQuoteString_EscapedOther()
        throws Exception
    {
        newLexer( "\"che\\bese\"" );

        assertNextToken( Token.DOUBLE_QUOTE_STRING,
                         "chebese" );

        assertEnd();
    }

    public void testSingleQuoteString_Simple()
        throws Exception
    {
        newLexer( "'cheese'" );

        assertNextToken( Token.SINGLE_QUOTE_STRING,
                         "cheese" );

        assertEnd();
    }

    public void testSingleQuoteString_EscapedTab()
        throws Exception
    {
        newLexer( "'che\\tese'" );

        assertNextToken( Token.SINGLE_QUOTE_STRING,
                         "che\tese" );

        assertEnd();
    }

    public void testSingleQuoteString_EscapedNewline()
        throws Exception
    {
        newLexer( "'che\\nese'" );

        assertNextToken( Token.SINGLE_QUOTE_STRING,
                         "che\nese" );

        assertEnd();
    }
    
    public void testSingleQuoteString_EscapedCarriageReturn()
        throws Exception
    {
        newLexer( "'che\\rese'" );

        assertNextToken( Token.SINGLE_QUOTE_STRING,
                         "che\rese" );

        assertEnd();
    }

    public void testSingleQuoteString_EscapedOther()
        throws Exception
    {
        newLexer( "'che\\bese'" );

        assertNextToken( Token.SINGLE_QUOTE_STRING,
                         "chebese" );

        assertEnd();
    }

    public void testUnterminatedStringLiteral_DoubleQuote_Newline()
        throws Exception
    {
        newLexer( "\"cheese\n\"" );

        try
        {
            nextToken();
            fail( "should have thrown UnterminatedStringLiteralException" );
        }
        catch (UnterminatedStringLiteralException e)
        {
            // expected and correct
        }
    }

    public void testUnterminatedStringLiteral_DoubleQuote_CarriageReturn()
        throws Exception
    {
        newLexer( "\"cheese\r\"" );

        try
        {
            nextToken();
            fail( "should have thrown UnterminatedStringLiteralException" );
        }
        catch (UnterminatedStringLiteralException e)
        {
            // expected and correct
        }
    }

    public void testUnterminatedStringLiteral_DoubleQuote_EndOfStream()
        throws Exception
    {
        newLexer( "\"cheese" );

        try
        {
            nextToken();
            fail( "should have thrown UnterminatedStringLiteralException" );
        }
        catch (UnterminatedStringLiteralException e)
        {
            // expected and correct
        }
    }

    public void testUnterminatedStringLiteral_SingleQuote_Newline()
        throws Exception
    {
        newLexer( "'cheese\n'" );

        try
        {
            nextToken();
            fail( "should have thrown UnterminatedStringLiteralException" );
        }
        catch (UnterminatedStringLiteralException e)
        {
            // expected and correct
        }
    }

    public void testUnterminatedStringLiteral_SingleQuote_CarriageReturn()
        throws Exception
    {
        newLexer( "'cheese\r'" );

        try
        {
            nextToken();
            fail( "should have thrown UnterminatedStringLiteralException" );
        }
        catch (UnterminatedStringLiteralException e)
        {
            // expected and correct
        }
    }
    
    public void testUnterminatedStringLiteral_SingleQuote_EndOfStream()
        throws Exception
    {
        newLexer( "'cheese" );

        try
        {
            nextToken();
            fail( "should have thrown UnterminatedStringLiteralException" );
        }
        catch (UnterminatedStringLiteralException e)
        {
            // expected and correct
        }
    }

    public void testIdentifier()
        throws Exception
    {
        assertSimple( "cheese",
                      Token.IDENTIFIER );
    }

    public void testNumber_Integer()
        throws Exception
    {
        assertSimple( "42",
                      Token.INTEGER_NUMBER );
    }

    public void testNumber_FloatingPoint()
        throws Exception
    {
        assertSimple( "42.84",
                      Token.FLOAT_NUMBER );
    }

    public void testNumber_UnexpectedCharacter()
        throws Exception
    {
        newLexer( "42.cheese" );

        char[] expected = assertUnexpectedCharacter( 'c',
                                                     1,
                                                     4 );

        assertLength( 10,
                      expected );

        assertContains( '0',
                        expected );
        assertContains( '1',
                        expected );
        assertContains( '2',
                        expected );
        assertContains( '3',
                        expected );
        assertContains( '4',
                        expected );
        assertContains( '5',
                        expected );
        assertContains( '6',
                        expected );
        assertContains( '7',
                        expected );
        assertContains( '8',
                        expected );
        assertContains( '9',
                        expected );
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    public void testKeyword_Abstract()
        throws Exception
    {
        assertSimple( "abstract",
                       Token.KEYWORD_ABSTRACT );
    }

    public void testKeyword_As()
        throws Exception
    {
        assertSimple( "as",
                      Token.KEYWORD_AS );
    }

    public void testKeyword_Break()
        throws Exception
    {
        assertSimple( "break",
                      Token.KEYWORD_BREAK );
    }

    public void testKeyword_Case()
        throws Exception
    {
        assertSimple( "case",
                      Token.KEYWORD_CASE );
    }

    public void testKeyword_Catch()
        throws Exception
    {
        assertSimple( "catch",
                      Token.KEYWORD_CATCH );
    }

    public void testKeyword_Class()
        throws Exception
    {
        assertSimple( "class",
                      Token.KEYWORD_CLASS );
    }

    public void testKeyword_Const()
        throws Exception
    {
        assertSimple( "const",
                      Token.KEYWORD_CONST );
    }

    public void testKeyword_Continue()
        throws Exception
    {
        assertSimple( "continue",
                      Token.KEYWORD_CONTINUE );
    }

    public void testKeyword_Default()
        throws Exception
    {
        assertSimple( "default",
                      Token.KEYWORD_DEFAULT );
    }

    public void testKeyword_Do()
        throws Exception
    {
        assertSimple( "do",
                      Token.KEYWORD_DO );
    }

    public void testKeyword_Else()
        throws Exception
    {
        assertSimple( "else",
                      Token.KEYWORD_ELSE );
    }

    public void testKeyword_Extends()
        throws Exception
    {
        assertSimple( "extends",
                      Token.KEYWORD_EXTENDS );
    }

    public void testKeyword_Final()
        throws Exception
    {
        assertSimple( "final",
                      Token.KEYWORD_FINAL );
    }

    public void testKeyword_Finally()
        throws Exception
    {
        assertSimple( "finally",
                      Token.KEYWORD_FINALLY );
    }

    public void testKeyword_For()
        throws Exception
    {
        assertSimple( "for",
                      Token.KEYWORD_FOR );
    }

    public void testKeyword_Goto()
        throws Exception
    {
        assertSimple( "goto",
                      Token.KEYWORD_GOTO );
    }

    public void testKeyword_If()
        throws Exception
    {
        assertSimple( "if",
                      Token.KEYWORD_IF );
    }

    public void testKeyword_Implements()
        throws Exception
    {
        assertSimple( "implements",
                      Token.KEYWORD_IMPLEMENTS );
    }

    public void testKeyword_Import()
        throws Exception
    {
        assertSimple( "import",
                      Token.KEYWORD_IMPORT );
    }

    public void testKeyword_Instanceof()
        throws Exception
    {
        assertSimple( "instanceof",
                      Token.KEYWORD_INSTANCEOF );
    }

    public void testKeyword_Interface()
        throws Exception
    {
        assertSimple( "interface",
                      Token.KEYWORD_INTERFACE );
    }

    public void testKeyword_Native()
        throws Exception
    {
        assertSimple( "native",
                      Token.KEYWORD_NATIVE );
    }

    public void testKeyword_New()
        throws Exception
    {
        assertSimple( "new",
                      Token.KEYWORD_NEW );
    }

    public void testKeyword_Package()
        throws Exception
    {
        assertSimple( "package",
                      Token.KEYWORD_PACKAGE );
    }

    public void testKeyword_Private()
        throws Exception
    {
        assertSimple( "private",
                     Token.KEYWORD_PRIVATE );
    }

    public void testKeyword_Property()
        throws Exception
    {
        assertSimple( "property",
                     Token.KEYWORD_PROPERTY );
    }

    public void testKeyword_Protected()
        throws Exception
    {
        assertSimple( "protected",
                      Token.KEYWORD_PROTECTED );
    }

    public void testKeyword_Public()
        throws Exception
    {
        assertSimple( "public",
                      Token.KEYWORD_PUBLIC );
    }

    public void testKeyword_Return()
        throws Exception
    {
        assertSimple( "return",
                      Token.KEYWORD_RETURN );
    }

    public void testKeyword_Static()
        throws Exception
    {
        assertSimple( "static",
                      Token.KEYWORD_STATIC );
    }

    public void testKeyword_Super()
        throws Exception
    {
        assertSimple( "super",
                      Token.KEYWORD_SUPER );
    }

    public void testKeyword_Switch()
        throws Exception
    {
        assertSimple( "switch",
                      Token.KEYWORD_SWITCH );
    }

    public void testKeyword_Synchronized()
        throws Exception
    {
        assertSimple( "synchronized",
                      Token.KEYWORD_SYNCHRONIZED );
    }

    public void testKeyword_This()
        throws Exception
    {
        assertSimple( "this",
                      Token.KEYWORD_THIS );
    }

    public void testKeyword_Throw()
        throws Exception
    {
        assertSimple( "throw",
                      Token.KEYWORD_THROW );
    }

    public void testKeyword_Throws()
        throws Exception
    {
        assertSimple( "throws",
                      Token.KEYWORD_THROWS );
    }

    public void testKeyword_Try()
        throws Exception
    {
        assertSimple( "try",
                      Token.KEYWORD_TRY );
    }

    public void testKeyword_While()
        throws Exception
    {
        assertSimple( "while",
                      Token.KEYWORD_WHILE );
    }

    public void testUnexpecteCharacterException()
        throws Exception
    {
        newLexer( "#" );

        try
        {
            nextToken();
            fail( "should have thrown UnexpectedCharacterException" );
        }
        catch (UnexpectedCharacterException e)
        {
            // expected and correct
            assertEquals( '#',
                          e.getCharacter() );
        }
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    protected void assertSimple(String text,
                                int type)
        throws Exception
    {
        newLexer( text );

        assertNextToken( type,
                         text );
           
        assertEnd();
    }
                                 
    protected void assertNextToken(int type,
                                   String text)
        throws Exception
    {
        Token token = this.lexer.nextToken();

        assertNotNull( token );

        assertEquals( type,
                      token.getType() );

        assertEquals( text,
                      token.getText() );
    }

    protected void nextToken()
        throws Exception
    {
        this.lexer.nextToken();
    }

    protected char[] assertUnexpectedCharacter(char c,
                                               int line,
                                               int column)
        throws Exception
    {
        try
        {
            this.lexer.nextToken();
            fail( "should have thrown UnexpectedCharacterException" );
        }
        catch (UnexpectedCharacterException e)
        {
            // expected and correct
            assertEquals( c,
                          e.getCharacter() );

            assertEquals( line,
                          e.getLine() );

            assertEquals( column,
                          e.getColumn() );

            return e.getExpected();
        }

        return new char[]{};
    }

    protected void assertEnd()
        throws Exception
    {
        assertNull( this.lexer.nextToken() );
    }

    protected void newLexer(String text)
    {
        StringCharStream in = new StringCharStream( text );

        this.lexer = new Lexer( in );
    }
}
