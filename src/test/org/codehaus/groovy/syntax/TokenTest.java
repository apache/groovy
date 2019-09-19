/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.syntax;


import groovy.test.GroovyTestCase;

public class TokenTest
        extends GroovyTestCase {

    public void testNothing() {
    }

/*
    private static final int LINE = 11;
    private static final int COLUMN = 33;

    public void testConstruct()
    {
        Token token = new Token( 42,
                                 "forty-two",
                                 11,
                                 22 );

        assertEquals( 42,
                      token.getType() );

        assertEquals( "forty-two",
                      token.getText() );

        assertEquals( 11,
                      token.getStartLine() );

        assertEquals( 22,
                      token.getStartColumn() );
    }

    public void testLeftCurlyBrace()
    {
        Token token = Token.leftCurlyBrace( LINE,
                                            COLUMN );

        assertToken( token,
                     Token.LEFT_CURLY_BRACE,
                     "{" );
    }

    public void testRightCurlyBrace()
    {
        Token token = Token.rightCurlyBrace( LINE,
                                             COLUMN );

        assertToken( token,
                     Token.RIGHT_CURLY_BRACE,
                     "}" );
    }

    public void testLeftSquareBracket()
    {
        Token token = Token.leftSquareBracket( LINE,
                                               COLUMN );

        assertToken( token,
                     Token.LEFT_SQUARE_BRACKET,
                     "[" );
    }

    public void testRightSquareBracket()
    {
        Token token = Token.rightSquareBracket( LINE,
                                                COLUMN );

        assertToken( token,
                     Token.RIGHT_SQUARE_BRACKET,
                     "]" );
    }

    public void testLeftParenthesis()
    {
        Token token = Token.leftParenthesis( LINE,
                                             COLUMN );

        assertToken( token,
                     Token.LEFT_PARENTHESIS,
                     "(" );
    }

    public void testRightParenthesis()
    {
        Token token = Token.rightParenthesis( LINE,
                                              COLUMN );

        assertToken( token,
                     Token.RIGHT_PARENTHESIS,
                     ")" );
    }

    public void testDot()
    {
        Token token = Token.dot( LINE,
                                 COLUMN );

        assertToken( token,
                     Token.DOT,
                     "." );
    }

    public void testDotDot()
    {
        Token token = Token.dotDot( LINE,
                                    COLUMN );

        assertToken( token,
                     Token.DOT_DOT,
                     ".." );
    }

    public void testNot()
    {
        Token token = Token.not( LINE,
                                 COLUMN );

        assertToken( token,
                     Token.NOT,
                     "!" );
    }

    public void testCompareNotEqual()
    {
        Token token = Token.compareNotEqual( LINE,
                                             COLUMN );

        assertToken( token,
                     Token.COMPARE_NOT_EQUAL,
                     "!=" );
    }

    public void testEqual()
    {
        Token token = Token.equal( LINE,
                                   COLUMN );

        assertToken( token,
                     Token.EQUAL,
                     "=" );
    }

    public void testCompareIdentical()
    {
        Token token = Token.compareIdentical( LINE,
                                              COLUMN );

        assertToken( token,
                     Token.COMPARE_IDENTICAL,
                     "===" );
    }

    public void testCompareEqual()
    {
        Token token = Token.compareEqual( LINE,
                                          COLUMN );

        assertToken( token,
                     Token.COMPARE_EQUAL,
                     "==" );
    }

    public void testCompareLessThan()
    {
        Token token = Token.compareLessThan( LINE,
                                             COLUMN );

        assertToken( token,
                     Token.COMPARE_LESS_THAN,
                     "<" );
    }

    public void testCompareLessThanEqual()
    {
        Token token = Token.compareLessThanEqual( LINE,
                                                  COLUMN );

        assertToken( token,
                     Token.COMPARE_LESS_THAN_EQUAL,
                     "<=" );
    }

    public void testCompareGreaterThan()
    {
        Token token = Token.compareGreaterThan( LINE,
                                                COLUMN );

        assertToken( token,
                     Token.COMPARE_GREATER_THAN,
                     ">" );
    }

    public void testCompareGreaterThanEqual()
    {
        Token token = Token.compareGreaterThanEqual( LINE,
                                                     COLUMN );

        assertToken( token,
                     Token.COMPARE_GREATER_THAN_EQUAL,
                     ">=" );
    }

    public void testLogicalOr()
    {
        Token token = Token.logicalOr( LINE,
                                       COLUMN );

        assertToken( token,
                     Token.LOGICAL_OR,
                     "||" );
    }

    public void testLogicalAnd()
    {
        Token token = Token.logicalAnd( LINE,
                                        COLUMN );

        assertToken( token,
                     Token.LOGICAL_AND,
                     "&&" );
    }

    public void testPlus()
    {
        Token token = Token.plus( LINE,
                                  COLUMN );

        assertToken( token,
                     Token.PLUS,
                     "+" );
    }

    public void testPlusPlus()
    {
        Token token = Token.plusPlus( LINE,
                                      COLUMN );

        assertToken( token,
                     Token.PLUS_PLUS,
                     "++" );
    }

    public void testPlusEqual()
    {
        Token token = Token.plusEqual( LINE,
                                       COLUMN );

        assertToken( token,
                     Token.PLUS_EQUAL,
                     "+=" );
    }

    public void testMinus()
    {
        Token token = Token.minus( LINE,
                                   COLUMN );

        assertToken( token,
                     Token.MINUS,
                     "-" );
    }

    public void testMinusMinus()
    {
        Token token = Token.minusMinus( LINE,
                                        COLUMN );

        assertToken( token,
                     Token.MINUS_MINUS,
                     "--" );
    }

    public void testMinusEqual()
    {
        Token token = Token.minusEqual( LINE,
                                        COLUMN );

        assertToken( token,
                     Token.MINUS_EQUAL,
                     "-=" );
    }

    public void testDivide()
    {
        Token token = Token.divide( LINE,
                                    COLUMN );

        assertToken( token,
                     Token.DIVIDE,
                     "/" );
    }

    public void testDivideEqual()
    {
        Token token = Token.divideEqual( LINE,
                                         COLUMN );

        assertToken( token,
                     Token.DIVIDE_EQUAL,
                     "/=" );
    }

    public void testMod()
    {
        Token token = Token.mod( LINE,
                                 COLUMN );

        assertToken( token,
                     Token.MOD,
                     "%" );
    }

    public void testModEqual()
    {
        Token token = Token.modEqual( LINE,
                                      COLUMN );

        assertToken( token,
                     Token.MOD_EQUAL,
                     "%=" );
    }

    public void testMultiply()
    {
        Token token = Token.multiply( LINE,
                                      COLUMN );

        assertToken( token,
                     Token.MULTIPLY,
                     "*" );
    }

    public void testMultiplyEqual()
    {
        Token token = Token.multiplyEqual( LINE,
                                           COLUMN );

        assertToken( token,
                     Token.MULTIPLY_EQUAL,
                     "*=" );
    }

    public void testComma()
    {
        Token token = Token.comma( LINE,
                                   COLUMN );

        assertToken( token,
                     Token.COMMA,
                     "," );
    }

    public void testColon()
    {
        Token token = Token.colon( LINE,
                                   COLUMN );

        assertToken( token,
                     Token.COLON,
                     ":" );
    }

    public void testSemicolon()
    {
        Token token = Token.semicolon( LINE,
                                       COLUMN );

        assertToken( token,
                     Token.SEMICOLON,
                     ";" );
    }

    public void testQuestion()
    {
        Token token = Token.question( LINE,
                                      COLUMN );

        assertToken( token,
                     Token.QUESTION,
                     "?" );
    }

    public void testPipe()
    {
        Token token = Token.pipe( LINE,
                                  COLUMN );

        assertToken( token,
                     Token.PIPE,
                     "|" );
    }

    public void testDoubleQuoteString()
    {
        Token token = Token.doubleQuoteString( LINE,
                                               COLUMN,
                                               "cheese" );

        assertToken( token,
                     Token.DOUBLE_QUOTE_STRING,
                     "cheese",
                     "<string literal>");
    }

    public void testSingleQuoteString()
    {
        Token token = Token.singleQuoteString( LINE,
                                               COLUMN,
                                               "cheese" );

        assertToken( token,
                     Token.SINGLE_QUOTE_STRING,
                     "cheese",
                     "<string literal>" );
    }

    public void testIdentifier()
    {
        Token token = Token.identifier( LINE,
                                        COLUMN,
                                        "cheese" );

        assertToken( token,
                     Token.IDENTIFIER,
                     "cheese",
                     "<identifier>" );
    }

    public void testIntegerNumber()
    {
        Token token = Token.integerNumber( LINE,
                                           COLUMN,
                                           "42" );

        assertToken( token,
                     Token.INTEGER_NUMBER,
                     "42",
                     "<number>" );
    }

    public void testFloatNumber()
    {
        Token token = Token.floatNumber( LINE,
                                         COLUMN,
                                         "42.84" );

        assertToken( token,
                     Token.FLOAT_NUMBER,
                     "42.84",
                     "<number>" );
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    public void testKeyword_As()
    {
        assertKeywordToken( "as",
                            Token.KEYWORD_AS );
    }

    public void testKeyword_Abstract()
    {
        assertKeywordToken( "abstract",
                            Token.KEYWORD_ABSTRACT );
    }

    public void testKeyword_Break()
    {
        assertKeywordToken( "break",
                            Token.KEYWORD_BREAK );
    }

    public void testKeyword_Case()
    {
        assertKeywordToken( "case",
                            Token.KEYWORD_CASE );
    }

    public void testKeyword_Catch()
    {
        assertKeywordToken( "catch",
                            Token.KEYWORD_CATCH );
    }

    public void testKeyword_Class()
    {
        assertKeywordToken( "class",
                            Token.KEYWORD_CLASS );
    }

    public void testKeyword_Const()
    {
        assertKeywordToken( "const",
                            Token.KEYWORD_CONST );
    }

    public void testKeyword_Continue()
    {
        assertKeywordToken( "continue",
                            Token.KEYWORD_CONTINUE );
    }

    public void testKeyword_Default()
    {
        assertKeywordToken( "default",
                            Token.KEYWORD_DEFAULT );
    }

    public void testKeyword_Do()
    {
        assertKeywordToken( "do",
                            Token.KEYWORD_DO );
    }

    public void testKeyword_Else()
    {
        assertKeywordToken( "else",
                            Token.KEYWORD_ELSE );
    }

    public void testKeyword_Extends()
    {
        assertKeywordToken( "extends",
                            Token.KEYWORD_EXTENDS );
    }

    public void testKeyword_Final()
    {
        assertKeywordToken( "final",
                            Token.KEYWORD_FINAL );
    }

    public void testKeyword_Finally()
    {
        assertKeywordToken( "finally",
                            Token.KEYWORD_FINALLY );
    }

    public void testKeyword_For()
    {
        assertKeywordToken( "for",
                            Token.KEYWORD_FOR );
    }

    public void testKeyword_Goto()
    {
        assertKeywordToken( "goto",
                            Token.KEYWORD_GOTO );
    }

    public void testKeyword_If()
    {
        assertKeywordToken( "if",
                            Token.KEYWORD_IF );
    }

    public void testKeyword_Implements()
    {
        assertKeywordToken( "implements",
                            Token.KEYWORD_IMPLEMENTS );
    }

    public void testKeyword_Import()
    {
        assertKeywordToken( "import",
                            Token.KEYWORD_IMPORT );
    }

    public void testKeyword_Instanceof()
    {
        assertKeywordToken( "instanceof",
                            Token.KEYWORD_INSTANCEOF );
    }

    public void testKeyword_Interface()
    {
        assertKeywordToken( "interface",
                            Token.KEYWORD_INTERFACE );
    }

    public void testKeyword_Native()
    {
        assertKeywordToken( "native",
                            Token.KEYWORD_NATIVE );
    }

    public void testKeyword_New()
    {
        assertKeywordToken( "new",
                            Token.KEYWORD_NEW );
    }

    public void testKeyword_Package()
    {
        assertKeywordToken( "package",
                            Token.KEYWORD_PACKAGE );
    }

    public void testKeyword_Private()
    {
        assertKeywordToken( "private",
                            Token.KEYWORD_PRIVATE );
    }

    public void testKeyword_Property()
    {
        assertKeywordToken( "property",
                            Token.KEYWORD_PROPERTY );
    }

    public void testKeyword_Protected()
    {
        assertKeywordToken( "protected",
                            Token.KEYWORD_PROTECTED );
    }

    public void testKeyword_Public()
    {
        assertKeywordToken( "public",
                            Token.KEYWORD_PUBLIC );
    }

    public void testKeyword_Return()
    {
        assertKeywordToken( "return",
                            Token.KEYWORD_RETURN );
    }

    public void testKeyword_Static()
    {
        assertKeywordToken( "static",
                            Token.KEYWORD_STATIC );
    }

    public void testKeyword_Super()
    {
        assertKeywordToken( "super",
                            Token.KEYWORD_SUPER );
    }

    public void testKeyword_Switch()
    {
        assertKeywordToken( "switch",
                            Token.KEYWORD_SWITCH );
    }

    public void testKeyword_Synchronized()
    {
        assertKeywordToken( "synchronized",
                            Token.KEYWORD_SYNCHRONIZED );
    }

    public void testKeyword_This()
    {
        assertKeywordToken( "this",
                            Token.KEYWORD_THIS );
    }

    public void testKeyword_Throw()
    {
        assertKeywordToken( "throw",
                            Token.KEYWORD_THROW );
    }

    public void testKeyword_Throws()
    {
        assertKeywordToken( "throws",
                            Token.KEYWORD_THROWS );
    }

    public void testKeyword_Try()
    {
        assertKeywordToken( "try",
                            Token.KEYWORD_TRY );
    }

    public void testKeyword_While()
    {
        assertKeywordToken( "while",
                            Token.KEYWORD_WHILE );
    }

    public void testUniqueKeywordTypes()
    {
        Map keywords = Token.getKeywordMap();

        Set types = new HashSet();

        types.addAll( keywords.values() );

        assertEquals( types.size(),
                      keywords.size() );
    }

    public void testUnknownTokenType()
    {
        assertEquals( "<unknown>",
                      Token.getTokenDescription( 6666 ) );
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    protected void assertKeywordToken(String text,
                                      int expectedType)
    {
        Token token = Token.keyword( LINE,
                                     COLUMN,
                                     text );

        assertToken( token,
                     expectedType,
                     text );
    }

    protected void assertToken(Token token,
                               int type,
                               String text)
    {
        assertToken( token,
                     type,
                     text,
                     '"' + text + '"' );
    }

    protected void assertToken(Token token,
                               int type,
                               String text,
                               String description)
    {
        assertEquals( type,
                      token.getType() );

        assertEquals( text,
                      token.getText() );

        assertEquals( description,
                      token.getDescription() );

        assertEquals( LINE,
                      token.getStartLine() );

        assertEquals( COLUMN,
                      token.getStartColumn() );
    }
    
*/
}
