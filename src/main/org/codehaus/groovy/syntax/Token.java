package org.codehaus.groovy.syntax;

/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Lexical token.
 *
 *  @author <a href="mailto:bob@werken.com">bob mcwhirter</a>
 *
 *  @version $Id$
 */
public class Token {
    // ----------------------------------------------------------------------
    //     Constants
    // ----------------------------------------------------------------------

    /** Token type for "\n". */
    public static final int NEWLINE = 5;

    /** Token type for "{". */
    public static final int LEFT_CURLY_BRACE = 10;

    /** Token type for "}". */
    public static final int RIGHT_CURLY_BRACE = 20;

    /** Token type for "[". */
    public static final int LEFT_SQUARE_BRACKET = 30;

    /** Token type for "]". */
    public static final int RIGHT_SQUARE_BRACKET = 40;

    /** Token type for "(". */
    public static final int LEFT_PARENTHESIS = 50;

    /** Token type for ")". */
    public static final int RIGHT_PARENTHESIS = 60;

    /** Token type for ".". */
    public static final int DOT = 70;

    /** Token type for "..". */
    public static final int DOT_DOT = 75;

    /** Token type for "...". */
    public static final int DOT_DOT_DOT = 77;

    /** Token type for "!". */
    public static final int NOT = 80;

    /** Token type for "!=". */
    public static final int COMPARE_NOT_EQUAL = 90;

    /** Token type for "=". */
    public static final int EQUAL = 100;

    /**	Token type for "~=". */
    public static final int FIND_REGEX = 105;

    /**	Token type for "~==". */
    public static final int MATCH_REGEX = 106;

    /** Token type for "~" */
    public static final int PATTERN_REGEX = 107;

    /** Token type for "==". */
    public static final int COMPARE_IDENTICAL = 110;

    /** Token type for ":=". */
    public static final int COMPARE_EQUAL = 115;

    /** Token type for "<". */
    public static final int COMPARE_LESS_THAN = 120;

    /** Token type for "<=". */
    public static final int COMPARE_LESS_THAN_EQUAL = 130;

    /** Token type for ">". */
    public static final int COMPARE_GREATER_THAN = 140;

    /** Token type for ">=". */
    public static final int COMPARE_GREATER_THAN_EQUAL = 150;

    /** Token type for "<=>". */
    public static final int COMPARE_TO = 155;

    /** Token type for "->". */
    public static final int NAVIGATE = 158;

    /** Token type for "||". */
    public static final int LOGICAL_OR = 160;

    /** Token type for "&&". */
    public static final int LOGICAL_AND = 170;

    /** Token type for "+". */
    public static final int PLUS = 180;

    /** Token type for "++". */
    public static final int PLUS_PLUS = 190;

    /** Token type for "+=". */
    public static final int PLUS_EQUAL = 200;

    /** Token type for "-". */
    public static final int MINUS = 210;

    /** Token type for "--". */
    public static final int MINUS_MINUS = 220;

    /** Token type for "-=". */
    public static final int MINUS_EQUAL = 230;

    /** Token type for "/". */
    public static final int DIVIDE = 240;

    /** Token type for "/=". */
    public static final int DIVIDE_EQUAL = 250;

    /** Token type for "%". */
    public static final int MOD = 260;

    /** Token type for "%=". */
    public static final int MOD_EQUAL = 270;

    /** Token type for "*". */
    public static final int MULTIPLY = 280;

    /** Token type for "*=". */
    public static final int MULTIPLY_EQUAL = 290;

    public static final int COMMA = 295;

    /** Token type for ":". */
    public static final int COLON = 300;

    /** Token type for ";". */
    public static final int SEMICOLON = 301;

    /** Token type for "?". */
    public static final int QUESTION = 310;

    /** Token type for "|". */
    public static final int PIPE = 315;

    /** Token type for "<<". */
    public static final int LEFT_SHIFT = 317;

    /** Token type for ">>". */
    public static final int RIGHT_SHIFT = 318;

    /** Token type for double-quoted string literal. */
    public static final int DOUBLE_QUOTE_STRING = 320;

    /** Token type for single-quoted string literal. */
    public static final int SINGLE_QUOTE_STRING = 330;

    /** Token type for identifier. */
    public static final int IDENTIFIER = 340;

    /** Token type for integral number. */
    public static final int INTEGER_NUMBER = 350;

    /** Token type for floating-point number. */
    public static final int FLOAT_NUMBER = 351;

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public static final int KEYWORD_ABSTRACT     = 501;
    public static final int KEYWORD_AS           = 502;
    public static final int KEYWORD_BREAK        = 503;
    public static final int KEYWORD_CASE         = 504;
    public static final int KEYWORD_CATCH        = 505;
    public static final int KEYWORD_CLASS        = 506;
    public static final int KEYWORD_CONST        = 507;
    public static final int KEYWORD_CONTINUE     = 508;
    public static final int KEYWORD_DEFAULT      = 509;
    public static final int KEYWORD_DO           = 510;
    public static final int KEYWORD_ELSE         = 511;
    public static final int KEYWORD_EXTENDS      = 512;
    public static final int KEYWORD_FINAL        = 513;
    public static final int KEYWORD_FINALLY      = 514;
    public static final int KEYWORD_FOR          = 515;
    public static final int KEYWORD_GOTO         = 516;
    public static final int KEYWORD_IF           = 517;
    public static final int KEYWORD_IMPLEMENTS   = 518;
    public static final int KEYWORD_IMPORT       = 519;
    public static final int KEYWORD_INSTANCEOF   = 520;
    public static final int KEYWORD_INTERFACE    = 521;
    public static final int KEYWORD_NATIVE       = 522;
    public static final int KEYWORD_NEW          = 523;
    public static final int KEYWORD_PACKAGE      = 524;
    public static final int KEYWORD_PRIVATE      = 525;
    public static final int KEYWORD_PROTECTED    = 526;
    public static final int KEYWORD_PUBLIC       = 527;
    public static final int KEYWORD_RETURN       = 528;
    public static final int KEYWORD_STATIC       = 529;
    public static final int KEYWORD_SUPER        = 530;
    public static final int KEYWORD_SWITCH       = 531;
    public static final int KEYWORD_SYNCHRONIZED = 532;
    public static final int KEYWORD_THIS         = 533;
    public static final int KEYWORD_THROW        = 534;
    public static final int KEYWORD_THROWS       = 535;
    public static final int KEYWORD_TRY          = 536;
    public static final int KEYWORD_WHILE        = 537;
    public static final int KEYWORD_PROPERTY     = 538;
    public static final int KEYWORD_TRUE         = 539;
    public static final int KEYWORD_FALSE        = 540;
    public static final int KEYWORD_ASSERT       = 541;
    public static final int KEYWORD_NULL         = 542;
    public static final int KEYWORD_VOID         = 543;
    public static final int KEYWORD_INT          = 544;
    public static final int KEYWORD_FLOAT        = 545;
    public static final int KEYWORD_DOUBLE       = 546;
    public static final int KEYWORD_CHAR         = 547;
    public static final int KEYWORD_BYTE         = 548;
    public static final int KEYWORD_LONG         = 549;
    public static final int KEYWORD_SHORT        = 550;
    public static final int KEYWORD_BOOLEAN      = 551;
    public static final int KEYWORD_DEF          = 560;
    public static final int KEYWORD_TRANSIENT    = 570;
    public static final int KEYWORD_VOLATILE     = 571;
    public static final int KEYWORD_IN           = 572;

    public static final int SYNTH_METHOD                = 800;
    public static final int SYNTH_PARAMETER_DECLARATION = 801;
    public static final int SYNTH_LIST                  = 802;
    public static final int SYNTH_MAP                   = 803;

    public static final int SYNTH_POSTFIX = 810;
    public static final int SYNTH_PREFIX  = 811;
    public static final int SYNTH_CAST    = 815;

    public static final int SYNTH_BLOCK   = 816;
    public static final int SYNTH_CLOSURE = 817;
    public static final int SYNTH_LABEL   = 818;


    private static final Map KEYWORDS = new HashMap();

    private static void addKeyword(String text, int type) {
        KEYWORDS.put(text, new Integer(type));
    }

    public static Map getKeywordMap() {
        return KEYWORDS;
    }

    static {
        addKeyword( "abstract"    , KEYWORD_ABSTRACT     );
        addKeyword( "as"          , KEYWORD_AS           );
        addKeyword( "assert"      , KEYWORD_ASSERT       );
        addKeyword( "break"       , KEYWORD_BREAK        );
        addKeyword( "case"        , KEYWORD_CASE         );
        addKeyword( "catch"       , KEYWORD_CATCH        );
        addKeyword( "class"       , KEYWORD_CLASS        );
        addKeyword( "const"       , KEYWORD_CONST        );
        addKeyword( "continue"    , KEYWORD_CONTINUE     );
        addKeyword( "def"         , KEYWORD_DEF          );
        addKeyword( "default"     , KEYWORD_DEFAULT      );
        addKeyword( "do"          , KEYWORD_DO           );
        addKeyword( "else"        , KEYWORD_ELSE         );
        addKeyword( "extends"     , KEYWORD_EXTENDS      );
        addKeyword( "final"       , KEYWORD_FINAL        );
        addKeyword( "finally"     , KEYWORD_FINALLY      );
        addKeyword( "for"         , KEYWORD_FOR          );
        addKeyword( "goto"        , KEYWORD_GOTO         );
        addKeyword( "if"          , KEYWORD_IF           );
//        addKeyword( "in"          , KEYWORD_IN           );
        addKeyword( "implements"  , KEYWORD_IMPLEMENTS   );
        addKeyword( "import"      , KEYWORD_IMPORT       );
        addKeyword( "instanceof"  , KEYWORD_INSTANCEOF   );
        addKeyword( "interface"   , KEYWORD_INTERFACE    );
        addKeyword( "native"      , KEYWORD_NATIVE       );
        addKeyword( "new"         , KEYWORD_NEW          );
        addKeyword( "package"     , KEYWORD_PACKAGE      );
        addKeyword( "private"     , KEYWORD_PRIVATE      );
        addKeyword( "property"    , KEYWORD_PROPERTY     );
        addKeyword( "protected"   , KEYWORD_PROTECTED    );
        addKeyword( "public"      , KEYWORD_PUBLIC       );
        addKeyword( "return"      , KEYWORD_RETURN       );
        addKeyword( "static"      , KEYWORD_STATIC       );
        addKeyword( "super"       , KEYWORD_SUPER        );
        addKeyword( "switch"      , KEYWORD_SWITCH       );
        addKeyword( "synchronized", KEYWORD_SYNCHRONIZED );
        addKeyword( "this"        , KEYWORD_THIS         );
        addKeyword( "throw"       , KEYWORD_THROW        );
        addKeyword( "throws"      , KEYWORD_THROWS       );
        addKeyword( "transient"   , KEYWORD_TRANSIENT    );
        addKeyword( "try"         , KEYWORD_TRY          );
        addKeyword( "volatile"    , KEYWORD_VOLATILE     );
        addKeyword( "while"       , KEYWORD_WHILE        );

        addKeyword( "true"        , KEYWORD_TRUE         );
        addKeyword( "false"       , KEYWORD_FALSE        );
        addKeyword( "null"        , KEYWORD_NULL         );

        addKeyword( "void"        , KEYWORD_VOID         );
        addKeyword( "int"         , KEYWORD_INT          );
        addKeyword( "float"       , KEYWORD_FLOAT        );
        addKeyword( "double"      , KEYWORD_DOUBLE       );
        addKeyword( "short"       , KEYWORD_SHORT        );
        addKeyword( "boolean"     , KEYWORD_BOOLEAN      );
        addKeyword( "char"        , KEYWORD_CHAR         );
        addKeyword( "byte"        , KEYWORD_BYTE         );
        addKeyword( "long"        , KEYWORD_LONG         );
    }

    private static final Map TOKEN_DESCRIPTIONS = new HashMap();

    private static void addTokenDescription(int type, String description) {
        addTokenDescription(new Integer(type), description);
    }

    private static void addTokenDescription(Integer type, String description) {
        if (description.startsWith("<") && description.endsWith(">")) {
            TOKEN_DESCRIPTIONS.put(type, description);
        }
        else {
            TOKEN_DESCRIPTIONS.put(type, '"' + description + '"');
        }
    }

    /**
     * Creates the token of the given id
     */
    public static Token newToken(int type, int startLine, int startColumn) {
        return new Token(type, getTokenDescription(type), startLine, startColumn);
    }

    public static String getTokenDescription(int type) {
        Integer typeKey = new Integer(type);

        if (TOKEN_DESCRIPTIONS.containsKey(typeKey)) {
            return (String) TOKEN_DESCRIPTIONS.get(typeKey);
        }

        return "<unknown>";
    }

    static {
        for (Iterator keywordIter = KEYWORDS.keySet().iterator(); keywordIter.hasNext();) {
            String keyword = (String) keywordIter.next();
            Integer typeKey = (Integer) KEYWORDS.get(keyword);

            addTokenDescription(typeKey, keyword);
        }

        addTokenDescription( NEWLINE             , "<newline>"        );
        addTokenDescription( LEFT_CURLY_BRACE    , "{"                );
        addTokenDescription( RIGHT_CURLY_BRACE   , "}"                );
        addTokenDescription( LEFT_SQUARE_BRACKET , "["                );
        addTokenDescription( RIGHT_SQUARE_BRACKET, "]"                );
        addTokenDescription( LEFT_PARENTHESIS    , "("                );
        addTokenDescription( RIGHT_PARENTHESIS   , ")"                );
        addTokenDescription( DOT                 , "."                );
        addTokenDescription( DOT_DOT             , ".."               );
        addTokenDescription( NOT                 , "!"                );
        addTokenDescription( COMPARE_NOT_EQUAL   , "!="               );
        addTokenDescription( EQUAL               , "="                );
        addTokenDescription( FIND_REGEX          , "~="               );
        addTokenDescription( MATCH_REGEX         , "~=="              );
        addTokenDescription( PATTERN_REGEX       , "~"                );
        addTokenDescription( COMPARE_EQUAL       , "=="               );
        addTokenDescription( COMPARE_IDENTICAL   , "==="              );
        addTokenDescription( COMPARE_LESS_THAN   , "<"                );
        addTokenDescription( COMPARE_LESS_THAN_EQUAL   , "<="         );
        addTokenDescription( COMPARE_GREATER_THAN      , ">"          );
        addTokenDescription( COMPARE_GREATER_THAN_EQUAL, ">="         );
        addTokenDescription( LOGICAL_OR          , "||"               );
        addTokenDescription( LOGICAL_AND         , "&&"               );
        addTokenDescription( PLUS                , "+"                );
        addTokenDescription( PLUS_PLUS           , "++"               );
        addTokenDescription( PLUS_EQUAL          , "+="               );
        addTokenDescription( MINUS               , "-"                );
        addTokenDescription( MINUS_MINUS         , "--"               );
        addTokenDescription( MINUS_EQUAL         , "-="               );
        addTokenDescription( DIVIDE              , "/"                );
        addTokenDescription( DIVIDE_EQUAL        , "/="               );
        addTokenDescription( MOD                 , "%"                );
        addTokenDescription( MOD_EQUAL           , "%="               );
        addTokenDescription( MULTIPLY            , "*"                );
        addTokenDescription( MULTIPLY_EQUAL      , "*="               );
        addTokenDescription( COMMA               , ","                );
        addTokenDescription( COLON               , ":"                );
        addTokenDescription( SEMICOLON           , ";"                );
        addTokenDescription( QUESTION            , "?"                );
        addTokenDescription( PIPE                , "|"                );
        addTokenDescription( LEFT_SHIFT          , "<<"               );
        addTokenDescription( RIGHT_SHIFT         , ">>"               );
        addTokenDescription( DOUBLE_QUOTE_STRING , "<string literal>" );
        addTokenDescription( SINGLE_QUOTE_STRING , "<string literal>" );
        addTokenDescription( IDENTIFIER          , "<identifier>"     );
        addTokenDescription( INTEGER_NUMBER      , "<number>"         );
        addTokenDescription( FLOAT_NUMBER        , "<number>"         );
        addTokenDescription( INTEGER_NUMBER      , "<number>"         );
     }

    // ----------------------------------------------------------------------
    //     Instance members
    // ----------------------------------------------------------------------

    /** Type. */
    private int type;

    /** Parser identified meaning for polymorphic token. */
    private int interpretation;

    /** Actual text. */
    private String text;

    /** Source starting line. */
    private int startLine;

    /** Source starting column. */
    private int startColumn;

    // ----------------------------------------------------------------------
    //     Constructors
    // ----------------------------------------------------------------------

    /** Construct.
     *
     *  @param type Type of the token.
     *  @param text Actual text.
     *  @param startLine Starting line within source.
     *  @param startColumn Starting column within source.
     */
    protected Token(int type, String text, int startLine, int startColumn) {
        this.type = type;
        this.interpretation = type;
        this.text = text;
        this.startLine = startLine;
        this.startColumn = startColumn;
    }

    // ----------------------------------------------------------------------
    //     Instance methods
    // ----------------------------------------------------------------------

    /** Retrieve the type.
     *
     *  @return The type.
     */
    public int getType() {
        return this.type;
    }

    /** Retrieve the interpretation (usually, but not always, the same
     *  as type.
     */
    public int getInterpretation() {
        return this.interpretation;
    }

    /** Sets an interpretation for the token.
     */

    public void setInterpretation( int type ) {
        this.interpretation = type;
    }

    

    /** Retrieve the actual token text.
     *
     *  @return The text.
     */
    public String getText() {
        return this.text;
    }

    /** Retrieve the starting line within the source.
     *
     *  @return The starting line.
     */
    public int getStartLine() {
        return this.startLine;
    }

    /** Retrieve the starting column within the source.
     *
     *  @return The starting column.
     */
    public int getStartColumn() {
        return this.startColumn;
    }

    public String getDescription() {
        return getTokenDescription(getType());
    }

    public String toString() {
        return "[Token (" + getDescription() + "): text=" + this.text + ",type=" + type + ",interpretation=" + interpretation + "]";
    }

    // ----------------------------------------------------------------------
    //     Factory methods
    // ----------------------------------------------------------------------

    /** Factory method for token for "\n".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token newline(int startLine, int startColumn) {
        return newToken(NEWLINE, "<newline>", startLine, startColumn);
    }

    /** Factory method for token for "{".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token leftCurlyBrace(int startLine, int startColumn) {
        return newToken(LEFT_CURLY_BRACE, "{", startLine, startColumn);
    }

    /** Factory method for token for "}".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token rightCurlyBrace(int startLine, int startColumn) {
        return newToken(RIGHT_CURLY_BRACE, "}", startLine, startColumn);
    }

    /** Factory method for token for "[".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token leftSquareBracket(int startLine, int startColumn) {
        return newToken(LEFT_SQUARE_BRACKET, "[", startLine, startColumn);
    }

    /** Factory method for token for "]".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token rightSquareBracket(int startLine, int startColumn) {
        return newToken(RIGHT_SQUARE_BRACKET, "]", startLine, startColumn);
    }

    /** Factory method for token for "(".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token leftParenthesis(int startLine, int startColumn) {
        return newToken(LEFT_PARENTHESIS, "(", startLine, startColumn);
    }

    /** Factory method for token for ")".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token rightParenthesis(int startLine, int startColumn) {
        return newToken(RIGHT_PARENTHESIS, ")", startLine, startColumn);
    }

    /** Factory method for token for ".".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token dot(int startLine, int startColumn) {
        return newToken(DOT, ".", startLine, startColumn);
    }

    public static Token dotDot(int startLine, int startColumn) {
        return newToken(DOT_DOT, "..", startLine, startColumn);
    }

    public static Token dotDotDot(int startLine, int startColumn) {
        return newToken(DOT_DOT_DOT, "...", startLine, startColumn);
    }

    /** Factory method for token for "!".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token not(int startLine, int startColumn) {
        return newToken(NOT, "!", startLine, startColumn);
    }

    /** Factory method for token for "!=".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token compareNotEqual(int startLine, int startColumn) {
        return newToken(COMPARE_NOT_EQUAL, "!=", startLine, startColumn);
    }

    /** Factory method for token for "~=".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token findRegex(int startLine, int startColumn) {
        return newToken(FIND_REGEX, "~=", startLine, startColumn);
    }

    /** Factory method for token for "~==".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token matchRegex(int startLine, int startColumn) {
        return newToken(MATCH_REGEX, "~==", startLine, startColumn);
    }

    /** Factory method for token for "=".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token equal(int startLine, int startColumn) {
        return newToken(EQUAL, "=", startLine, startColumn);
    }

    /** Factory method for token for "==".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token compareIdentical(int startLine, int startColumn) {
        return newToken(COMPARE_IDENTICAL, "===", startLine, startColumn);
    }

    /** Factory method for token for ":=".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token compareEqual(int startLine, int startColumn) {
        return newToken(COMPARE_EQUAL, "==", startLine, startColumn);
    }

    /** Factory method for token for "<".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token compareLessThan(int startLine, int startColumn) {
        return newToken(COMPARE_LESS_THAN, "<", startLine, startColumn);
    }

    /** Factory method for token for "<=".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token compareLessThanEqual(int startLine, int startColumn) {
        return newToken(COMPARE_LESS_THAN_EQUAL, "<=", startLine, startColumn);
    }

    /** Factory method for token for ">".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token compareGreaterThan(int startLine, int startColumn) {
        return newToken(COMPARE_GREATER_THAN, ">", startLine, startColumn);
    }

    /** Factory method for token for ">=".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token compareGreaterThanEqual(int startLine, int startColumn) {
        return newToken(COMPARE_GREATER_THAN_EQUAL, ">=", startLine, startColumn);
    }

    /** Factory method for token for "<=>".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token compareTo(int startLine, int startColumn) {
        return newToken(COMPARE_TO, "<=>", startLine, startColumn);
    }

    /** Factory method for token for "->".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token navigate(int startLine, int startColumn) {
        return newToken(NAVIGATE, "->", startLine, startColumn);
    }

    /** Factory method for token for "||".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token logicalOr(int startLine, int startColumn) {
        return newToken(LOGICAL_OR, "||", startLine, startColumn);
    }

    /** Factory method for token for "&&".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token logicalAnd(int startLine, int startColumn) {
        return newToken(LOGICAL_AND, "&&", startLine, startColumn);
    }

    /** Factory method for token for "+".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token plus(int startLine, int startColumn) {
        return newToken(PLUS, "+", startLine, startColumn);
    }

    /** Factory method for token for "++".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token plusPlus(int startLine, int startColumn) {
        return newToken(PLUS_PLUS, "++", startLine, startColumn);
    }

    /** Factory method for token for "+=".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token plusEqual(int startLine, int startColumn) {
        return newToken(PLUS_EQUAL, "+=", startLine, startColumn);
    }

    /** Factory method for token for "-".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token minus(int startLine, int startColumn) {
        return newToken(MINUS, "-", startLine, startColumn);
    }

    /** Factory method for token for "--".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token minusMinus(int startLine, int startColumn) {
        return newToken(MINUS_MINUS, "--", startLine, startColumn);
    }

    /** Factory method for token for "-=".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token minusEqual(int startLine, int startColumn) {
        return newToken(MINUS_EQUAL, "-=", startLine, startColumn);
    }

    /** Factory method for token for "/".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token divide(int startLine, int startColumn) {
        return newToken(DIVIDE, "/", startLine, startColumn);
    }

    /** Factory method for token for "/=".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token divideEqual(int startLine, int startColumn) {
        return newToken(DIVIDE_EQUAL, "/=", startLine, startColumn);
    }

    /** Factory method for token for "%".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token mod(int startLine, int startColumn) {
        return newToken(MOD, "%", startLine, startColumn);
    }

    /** Factory method for token for "%=".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token modEqual(int startLine, int startColumn) {
        return newToken(MOD_EQUAL, "%=", startLine, startColumn);
    }

    /** Factory method for token for "*".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token multiply(int startLine, int startColumn) {
        return newToken(MULTIPLY, "*", startLine, startColumn);
    }

    /** Factory method for token for "*=".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token multiplyEqual(int startLine, int startColumn) {
        return newToken(MULTIPLY_EQUAL, "*=", startLine, startColumn);
    }

    public static Token comma(int startLine, int startColumn) {
        return newToken(COMMA, ",", startLine, startColumn);
    }

    /** Factory method for token for ":".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token colon(int startLine, int startColumn) {
        return newToken(COLON, ":", startLine, startColumn);
    }

    /** Factory method for token for ";".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token semicolon(int startLine, int startColumn) {
        return newToken(SEMICOLON, ";", startLine, startColumn);
    }

    /** Factory method for token for "?".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token question(int startLine, int startColumn) {
        return newToken(QUESTION, "?", startLine, startColumn);
    }

    /** Factory method for token for "|".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token pipe(int startLine, int startColumn) {
        return newToken(PIPE, "|", startLine, startColumn);
    }

    /** Factory method for token for "<<".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token leftShift(int startLine, int startColumn) {
        return newToken(LEFT_SHIFT, "<<", startLine, startColumn);
    }

    /** Factory method for token for ">>".
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token rightShift(int startLine, int startColumn) {
        return newToken(RIGHT_SHIFT, ">>", startLine, startColumn);
    }

    /** Factory method for token for double-quoted string.
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token doubleQuoteString(int startLine, int startColumn, String text) {
        return newToken(DOUBLE_QUOTE_STRING, text, startLine, startColumn);
    }

    /** Factory method for token for double-quoted string.
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token patternRegex(int startLine, int startColumn) {
        return newToken(PATTERN_REGEX, startLine, startColumn);
    }

    /** Factory method for token for single-quoted string.
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token singleQuoteString(int startLine, int startColumn, String text) {
        return newToken(SINGLE_QUOTE_STRING, text, startLine, startColumn);
    }

    /** Factory method for token for identifier.
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    public static Token identifier(int startLine, int startColumn, String text) {
        return newToken(IDENTIFIER, text, startLine, startColumn);
    }

    public static Token keyword(int startLine, int startColumn, String text) {
        if (KEYWORDS.containsKey(text)) {
            return newToken(((Integer) KEYWORDS.get(text)).intValue(), text, startLine, startColumn);
        }

        return null;
    }

    public static Token integerNumber(int startLine, int startColumn, String text) {
        return newToken(INTEGER_NUMBER, text, startLine, startColumn);
    }

    public static Token floatNumber(int startLine, int startColumn, String text) {
        return newToken(FLOAT_NUMBER, text, startLine, startColumn);
    }

    public static Token syntheticMethod() {
        return newToken(SYNTH_METHOD, "<synthetic>", -1, -1);
    }

    public static Token syntheticParameterDeclaration() {
        return newToken(SYNTH_PARAMETER_DECLARATION, "<synthetic>", -1, -1);
    }

    public static Token syntheticMap() {
        return newToken(SYNTH_MAP, "<synthetic>", -1, -1);
    }

    public static Token syntheticList() {
        return newToken(SYNTH_LIST, "<synthetic>", -1, -1);
    }

    public static Token syntheticPostfix() {
        return newToken(SYNTH_POSTFIX, "<synthetic>", -1, -1);
    }

    public static Token syntheticPrefix() {
        return newToken(SYNTH_PREFIX, "<synthetic>", -1, -1);
    }

    public static Token syntheticCast() {
        return newToken(SYNTH_CAST, "<synthetic>", -1, -1);
    }

    public static Token syntheticBlock() {
        return newToken(SYNTH_CAST, "<synthetic>", -1, -1);
    }

    public static Token syntheticClosure() {
        return newToken(SYNTH_CAST, "<synthetic>", -1, -1);
    }


    public Token toIdentifier() {
        if( type == IDENTIFIER ) {
            return this;
        } 
        else {
            return Token.identifier( startLine, startColumn, text );
        }
    }


    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    /** Generic factory method for a token.
     *
     *  @param type The token type.
     *  @param text The token text.
     *
     *  @param startLine Line upon which the token starts.
     *  @param startColumn Column upon which the token starts.
     *
     *  @return The token.
     */
    protected static Token newToken(int type, String text, int startLine, int startColumn) {
        return new Token(type, text, startLine, startColumn);
    }



  //---------------------------------------------------------------------------
  // INSTANCE TYPE COMPARISONS


   /**
    *  Returns true if this token is of the specified type.
    */

    public boolean isA( int type ) {
        return this.type == type;
    }



   /**
    *  Returns true if this token is of any of the specified types.
    */

    public boolean isA( int[] types ) {
        return Token.ofType( type, types );
    }



   /**
    * Returns true if this token performs an assignment to the LHS such as
    * = or += or *= etc.
    */

    public boolean isAssignmentToken() {
        return Token.isAssignmentToken( type );
    }



   /**
    *  Returns true if the token is a primitive type.
    */

    public boolean isPrimitiveTypeKeyword( boolean evenVoid ) {
        return Token.isPrimitiveTypeKeyword( type, evenVoid );
    }



   /**
    *  Returns true if the token is an identifier or a primitive type.
    */

    public boolean isIdentifierOrPrimitiveTypeKeyword() {
        return Token.isIdentifierOrPrimitiveTypeKeyword( type );
    }



   /**
    *  Returns true if the token is a valid as a name that has
    *  already been declared.  It is more general than a name that
    *  can be declared, as some names are reserved.
    */

    public boolean isValidNameReference() {
        return Token.isValidNameReference( type );
    }



   /**
    *  Returns true if the token is a class modifier.
    */

    public boolean isModifier() {
        return Token.isModifier( type );
    }




  //---------------------------------------------------------------------------
  // STATIC TYPE COMPARISONS


   /**
    *  Returns true if specified token type is in the supplied list.
    */

    public static boolean ofType(int type, int[] types) {
        boolean ofType = false;

        for (int i = 0; i < types.length; i++) {
            if (type == types[i]) {
                ofType = true;
                break;
            }
        }

        return ofType;
    }



   /**
    *  Returns true if the token type is a primitive type.
    */

    public static boolean isPrimitiveTypeKeyword( int type, boolean evenVoid ) {

        boolean is = false;

        switch (type) {
            case Token.KEYWORD_INT :
            case Token.KEYWORD_FLOAT :
            case Token.KEYWORD_DOUBLE :
            case Token.KEYWORD_CHAR :
            case Token.KEYWORD_BYTE :
            case Token.KEYWORD_SHORT :
            case Token.KEYWORD_LONG :
            case Token.KEYWORD_BOOLEAN :
                is = true;
                break;

            case Token.KEYWORD_VOID :
                if (evenVoid) {
                    is = true;
                }
        }

        return is;
    }



   /**
    *  Returns true if the specified type is an identifier or a
    *  primitive type.
    */

    public static boolean isIdentifierOrPrimitiveTypeKeyword(int type) {
        return (type == Token.IDENTIFIER || isPrimitiveTypeKeyword(type, false));
    }



   /**
    *  Returns true if the specified type is valid as a reference to
    *  an existing name.
    */

    public static boolean isValidNameReference( int type ) {
        switch( type ) {
            case Token.IDENTIFIER:
            case Token.KEYWORD_CLASS:
            case Token.KEYWORD_INTERFACE:
            case Token.KEYWORD_DEF:
               return true;
        }
        return false;
    }



   /**
    * Returns true if this token performs an assignment to the LHS such as
    * = or += or *= etc.
    */

    public static boolean isAssignmentToken( int type ) {
        switch (type) {
            case Token.EQUAL :
            case Token.PLUS_EQUAL :
            case Token.MINUS_EQUAL :
            case Token.MULTIPLY_EQUAL :
            case Token.DIVIDE_EQUAL :
            case Token.MOD_EQUAL :
                return true;
        }
        return false;
    }



   /**
    *  Returns true if the specified token type is a class modifier.
    */

    public static boolean isModifier(int type) {
        boolean modifier = false;

        switch (type) {
            case (Token.KEYWORD_PUBLIC) :
            case (Token.KEYWORD_PROTECTED) :
            case (Token.KEYWORD_PRIVATE) :
            case (Token.KEYWORD_STATIC) :
            case (Token.KEYWORD_FINAL) :
            case (Token.KEYWORD_SYNCHRONIZED) :
            case (Token.KEYWORD_ABSTRACT) :
            case (Token.KEYWORD_VOLATILE) :
            case (Token.KEYWORD_TRANSIENT) :
                modifier = true;
        }

        return modifier;
    }


}
