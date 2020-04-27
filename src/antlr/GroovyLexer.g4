/*
 * This file is adapted from the Antlr4 Java grammar which has the following license
 *
 *  Copyright (c) 2013 Terence Parr, Sam Harwell
 *  All rights reserved.
 *  [The "BSD licence"]
 *
 *    http://www.opensource.org/licenses/bsd-license.php
 *
 * Subsequent modifications by the Groovy community have been done under the Apache License v2:
 *
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

/**
 * The Groovy grammar is based on the official grammar for Java:
 * https://github.com/antlr/grammars-v4/blob/master/java/Java.g4
 */
lexer grammar GroovyLexer;

options {
    superClass = AbstractLexer;
}

@header {
    import java.util.Deque;
    import java.util.ArrayDeque;
    import java.util.Map;
    import java.util.HashMap;
    import java.util.Set;
    import java.util.HashSet;
    import java.util.Collections;
    import java.util.Arrays;
    import java.util.stream.IntStream;
    import java.util.logging.Logger;
    import java.util.logging.Level;
    import java.util.EmptyStackException;
    import org.apache.groovy.util.Maps;
    import static org.apache.groovy.parser.antlr4.SemanticPredicates.*;
}

@members {
    private static final Logger LOGGER = Logger.getLogger(GroovyLexer.class.getName());
    private long tokenIndex     = 0;
    private int  lastTokenType  = 0;
    private int  invalidDigitCount = 0;

    /**
     * Record the index and token type of the current token while emitting tokens.
     */
    @Override
    public void emit(Token token) {
        this.tokenIndex++;

        int tokenType = token.getType();
        if (Token.DEFAULT_CHANNEL == token.getChannel()) {
            this.lastTokenType = tokenType;
        }

        if (RollBackOne == tokenType) {
            this.rollbackOneChar();
        }

        super.emit(token);
    }

    private static final int[] REGEX_CHECK_ARRAY =
                                    IntStream.of(
                                        Identifier, CapitalizedIdentifier, NullLiteral, BooleanLiteral, THIS, RPAREN, RBRACK, RBRACE,
                                        IntegerLiteral, FloatingPointLiteral, StringLiteral, GStringEnd, INC, DEC
                                    ).sorted().toArray();

    private boolean isRegexAllowed() {
        if (Arrays.binarySearch(REGEX_CHECK_ARRAY, this.lastTokenType) >= 0) {
            return false;
        }

        return true;
    }

    /**
     * just a hook, which will be overrided by GroovyLangLexer
     */
    protected void rollbackOneChar() {}

    private static class Paren {
        private String text;
        private int lastTokenType;
        private int line;
        private int column;

        public Paren(String text, int lastTokenType, int line, int column) {
            this.text = text;
            this.lastTokenType = lastTokenType;
            this.line = line;
            this.column = column;
        }

        public String getText() {
            return this.text;
        }

        public int getLastTokenType() {
            return this.lastTokenType;
        }

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }

        @Override
        public int hashCode() {
            return (int) (text.hashCode() * line + column);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Paren)) {
                return false;
            }

            Paren other = (Paren) obj;

            return this.text.equals(other.text) && (this.line == other.line && this.column == other.column);
        }
    }

    protected void enterParenCallback(String text) {}

    protected void exitParenCallback(String text) {}

    private final Deque<Paren> parenStack = new ArrayDeque<>(32);

    private void enterParen() {
        String text = getText();
        enterParenCallback(text);

        parenStack.push(new Paren(text, this.lastTokenType, getLine(), getCharPositionInLine()));
    }

    private void exitParen() {
        String text = getText();
        exitParenCallback(text);

        Paren paren = parenStack.peek();
        if (null == paren) return;
        parenStack.pop();
    }
    private boolean isInsideParens() {
        Paren paren = parenStack.peek();

        // We just care about "(" and "[", inside which the new lines will be ignored.
        // Notice: the new lines between "{" and "}" can not be ignored.
        if (null == paren) {
            return false;
        }

        return ("(".equals(paren.getText()) && TRY != paren.getLastTokenType()) // we don't treat try-paren(i.e. try (....)) as parenthesis
                    || "[".equals(paren.getText());
    }
    private void ignoreTokenInsideParens() {
        if (!this.isInsideParens()) {
            return;
        }

        this.setChannel(Token.HIDDEN_CHANNEL);
    }
    private void ignoreMultiLineCommentConditionally() {
        if (!this.isInsideParens() && isFollowedByWhiteSpaces(_input)) {
            return;
        }

        this.setChannel(Token.HIDDEN_CHANNEL);
    }

    @Override
    public int getSyntaxErrorSource() {
        return GroovySyntaxError.LEXER;
    }

    @Override
    public int getErrorLine() {
        return getLine();
    }

    @Override
    public int getErrorColumn() {
        return getCharPositionInLine() + 1;
    }

    @Override
    public int popMode() {
        try {
            return super.popMode();
        } catch (EmptyStackException ignored) { // raised when parens are unmatched: too many ), ], or }
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest(org.codehaus.groovy.runtime.DefaultGroovyMethods.asString(ignored));
            }
        }

        return Integer.MIN_VALUE;
    }
}


// §3.10.5 String Literals
StringLiteral
    :   GStringQuotationMark    DqStringCharacter* GStringQuotationMark
    |   SqStringQuotationMark   SqStringCharacter* SqStringQuotationMark

    |   Slash      { this.isRegexAllowed() && _input.LA(1) != '*' }?
                 SlashyStringCharacter+       Slash

    |   TdqStringQuotationMark  TdqStringCharacter*    TdqStringQuotationMark
    |   TsqStringQuotationMark  TsqStringCharacter*    TsqStringQuotationMark
    |   DollarSlashyGStringQuotationMarkBegin   DollarSlashyStringCharacter+   DollarSlashyGStringQuotationMarkEnd
    ;

// Groovy gstring
GStringBegin
    :   GStringQuotationMark DqStringCharacter* Dollar -> pushMode(DQ_GSTRING_MODE), pushMode(GSTRING_TYPE_SELECTOR_MODE)
    ;
TdqGStringBegin
    :   TdqStringQuotationMark   TdqStringCharacter* Dollar -> type(GStringBegin), pushMode(TDQ_GSTRING_MODE), pushMode(GSTRING_TYPE_SELECTOR_MODE)
    ;
SlashyGStringBegin
    :   Slash { this.isRegexAllowed() && _input.LA(1) != '*' }? SlashyStringCharacter* Dollar { isFollowedByJavaLetterInGString(_input) }? -> type(GStringBegin), pushMode(SLASHY_GSTRING_MODE), pushMode(GSTRING_TYPE_SELECTOR_MODE)
    ;
DollarSlashyGStringBegin
    :   DollarSlashyGStringQuotationMarkBegin DollarSlashyStringCharacter* Dollar { isFollowedByJavaLetterInGString(_input) }? -> type(GStringBegin), pushMode(DOLLAR_SLASHY_GSTRING_MODE), pushMode(GSTRING_TYPE_SELECTOR_MODE)
    ;

mode DQ_GSTRING_MODE;
GStringEnd
    :   GStringQuotationMark     -> popMode
    ;
GStringPart
    :   Dollar  -> pushMode(GSTRING_TYPE_SELECTOR_MODE)
    ;
GStringCharacter
    :   DqStringCharacter -> more
    ;

mode TDQ_GSTRING_MODE;
TdqGStringEnd
    :   TdqStringQuotationMark    -> type(GStringEnd), popMode
    ;
TdqGStringPart
    :   Dollar   -> type(GStringPart), pushMode(GSTRING_TYPE_SELECTOR_MODE)
    ;
TdqGStringCharacter
    :   TdqStringCharacter -> more
    ;

mode SLASHY_GSTRING_MODE;
SlashyGStringEnd
    :   Dollar? Slash  -> type(GStringEnd), popMode
    ;
SlashyGStringPart
    :   Dollar { isFollowedByJavaLetterInGString(_input) }?   -> type(GStringPart), pushMode(GSTRING_TYPE_SELECTOR_MODE)
    ;
SlashyGStringCharacter
    :   SlashyStringCharacter -> more
    ;

mode DOLLAR_SLASHY_GSTRING_MODE;
DollarSlashyGStringEnd
    :   DollarSlashyGStringQuotationMarkEnd      -> type(GStringEnd), popMode
    ;
DollarSlashyGStringPart
    :   Dollar { isFollowedByJavaLetterInGString(_input) }?   -> type(GStringPart), pushMode(GSTRING_TYPE_SELECTOR_MODE)
    ;
DollarSlashyGStringCharacter
    :   DollarSlashyStringCharacter -> more
    ;

mode GSTRING_TYPE_SELECTOR_MODE;
GStringLBrace
    :   '{' { this.enterParen();  } -> type(LBRACE), popMode, pushMode(DEFAULT_MODE)
    ;
GStringIdentifier
    :   IdentifierInGString -> type(Identifier), popMode, pushMode(GSTRING_PATH_MODE)
    ;


mode GSTRING_PATH_MODE;
GStringPathPart
    :   Dot IdentifierInGString
    ;
RollBackOne
    :   . {
            // a trick to handle GStrings followed by EOF properly
            int readChar = _input.LA(-1);
            if (EOF == _input.LA(1) && ('"' == readChar || '/' == readChar)) {
                setType(GStringEnd);
            } else {
                setChannel(HIDDEN);
            }
          } -> popMode
    ;


mode DEFAULT_MODE;
// character in the double quotation string. e.g. "a"
fragment
DqStringCharacter
    :   ~["\r\n\\$]
    |   EscapeSequence
    ;

// character in the single quotation string. e.g. 'a'
fragment
SqStringCharacter
    :   ~['\r\n\\]
    |   EscapeSequence
    ;

// character in the triple double quotation string. e.g. """a"""
fragment TdqStringCharacter
    :   ~["\\$]
    |   GStringQuotationMark { _input.LA(1) != '"' || _input.LA(2) != '"' || _input.LA(3) == '"' && (_input.LA(4) != '"' || _input.LA(5) != '"') }?
    |   EscapeSequence
    ;

// character in the triple single quotation string. e.g. '''a'''
fragment TsqStringCharacter
    :   ~['\\]
    |   SqStringQuotationMark { _input.LA(1) != '\'' || _input.LA(2) != '\'' || _input.LA(3) == '\'' && (_input.LA(4) != '\'' || _input.LA(5) != '\'') }?
    |   EscapeSequence
    ;

// character in the slashy string. e.g. /a/
fragment SlashyStringCharacter
    :   SlashEscape
    |   Dollar { !isFollowedByJavaLetterInGString(_input) }?
    |   ~[/$\u0000]
    ;

// character in the collar slashy string. e.g. $/a/$
fragment DollarSlashyStringCharacter
    :   SlashEscape | DollarSlashEscape | DollarDollarEscape
    |   Slash { _input.LA(1) != '$' }?
    |   Dollar { !isFollowedByJavaLetterInGString(_input) }?
    |   ~[/$\u0000]
    ;

// Groovy keywords
AS              : 'as';
DEF             : 'def';
VAL             : 'val';
LET             : 'let';
IN              : 'in';
TRAIT           : 'trait';
THREADSAFE      : 'threadsafe'; // reserved keyword

// the reserved type name of Java10
VAR             : 'var';

// §3.9 Keywords
BuiltInPrimitiveType
    :   BOOLEAN
    |   CHAR
    |   BYTE
    |   SHORT
    |   INT
    |   LONG
    |   FLOAT
    |   DOUBLE
    ;

ABSTRACT      : 'abstract';
ASSERT        : 'assert';

fragment
BOOLEAN       : 'boolean';

BREAK         : 'break';

fragment
BYTE          : 'byte';

CASE          : 'case';
CATCH         : 'catch';

fragment
CHAR          : 'char';

CLASS         : 'class';
CONST         : 'const';
CONTINUE      : 'continue';
DEFAULT       : 'default';
DO            : 'do';

fragment
DOUBLE        : 'double';

ELSE          : 'else';
ENUM          : 'enum';
EXTENDS       : 'extends';
FINAL         : 'final';
FINALLY       : 'finally';

fragment
FLOAT         : 'float';


FOR           : 'for';
IF            : 'if';
GOTO          : 'goto';
IMPLEMENTS    : 'implements';
IMPORT        : 'import';
INSTANCEOF    : 'instanceof';

fragment
INT           : 'int';

INTERFACE     : 'interface';

fragment
LONG          : 'long';

NATIVE        : 'native';
NEW           : 'new';
PACKAGE       : 'package';
PRIVATE       : 'private';
PROTECTED     : 'protected';
PUBLIC        : 'public';
RETURN        : 'return';

fragment
SHORT         : 'short';


STATIC        : 'static';
STRICTFP      : 'strictfp';
SUPER         : 'super';
SWITCH        : 'switch';
SYNCHRONIZED  : 'synchronized';
THIS          : 'this';
THROW         : 'throw';
THROWS        : 'throws';
TRANSIENT     : 'transient';
TRY           : 'try';
VOID          : 'void';
VOLATILE      : 'volatile';
WHILE         : 'while';


// §3.10.1 Integer Literals

IntegerLiteral
    :   (   DecimalIntegerLiteral
        |   HexIntegerLiteral
        |   OctalIntegerLiteral
        |   BinaryIntegerLiteral
        ) (Underscore { require(false, "Number ending with underscores is invalid", -1, true); })?

    // !!! Error Alternative !!!
    |   Zero ([0-9] { invalidDigitCount++; })+ { require(false, "Invalid octal number", -(invalidDigitCount + 1), true); } IntegerTypeSuffix?
    ;

fragment
Zero
    :   '0'
    ;

fragment
DecimalIntegerLiteral
    :   DecimalNumeral IntegerTypeSuffix?
    ;

fragment
HexIntegerLiteral
    :   HexNumeral IntegerTypeSuffix?
    ;

fragment
OctalIntegerLiteral
    :   OctalNumeral IntegerTypeSuffix?
    ;

fragment
BinaryIntegerLiteral
    :   BinaryNumeral IntegerTypeSuffix?
    ;

fragment
IntegerTypeSuffix
    :   [lLiIgG]
    ;

fragment
DecimalNumeral
    :   Zero
    |   NonZeroDigit (Digits? | Underscores Digits)
    ;

fragment
Digits
    :   Digit (DigitOrUnderscore* Digit)?
    ;

fragment
Digit
    :   Zero
    |   NonZeroDigit
    ;

fragment
NonZeroDigit
    :   [1-9]
    ;

fragment
DigitOrUnderscore
    :   Digit
    |   Underscore
    ;

fragment
Underscores
    :   Underscore+
    ;

fragment
Underscore
    :   '_'
    ;

fragment
HexNumeral
    :   Zero [xX] HexDigits
    ;

fragment
HexDigits
    :   HexDigit (HexDigitOrUnderscore* HexDigit)?
    ;

fragment
HexDigit
    :   [0-9a-fA-F]
    ;

fragment
HexDigitOrUnderscore
    :   HexDigit
    |   Underscore
    ;

fragment
OctalNumeral
    :   Zero Underscores? OctalDigits
    ;

fragment
OctalDigits
    :   OctalDigit (OctalDigitOrUnderscore* OctalDigit)?
    ;

fragment
OctalDigit
    :   [0-7]
    ;

fragment
OctalDigitOrUnderscore
    :   OctalDigit
    |   Underscore
    ;

fragment
BinaryNumeral
    :   Zero [bB] BinaryDigits
    ;

fragment
BinaryDigits
    :   BinaryDigit (BinaryDigitOrUnderscore* BinaryDigit)?
    ;

fragment
BinaryDigit
    :   [01]
    ;

fragment
BinaryDigitOrUnderscore
    :   BinaryDigit
    |   Underscore
    ;

// §3.10.2 Floating-Point Literals

FloatingPointLiteral
    :   (   DecimalFloatingPointLiteral
        |   HexadecimalFloatingPointLiteral
        ) (Underscore { require(false, "Number ending with underscores is invalid", -1, true); })?
    ;

fragment
DecimalFloatingPointLiteral
    :   Digits Dot Digits ExponentPart? FloatTypeSuffix?
    |   Digits ExponentPart FloatTypeSuffix?
    |   Digits FloatTypeSuffix
    ;

fragment
ExponentPart
    :   ExponentIndicator SignedInteger
    ;

fragment
ExponentIndicator
    :   [eE]
    ;

fragment
SignedInteger
    :   Sign? Digits
    ;

fragment
Sign
    :   [+\-]
    ;

fragment
FloatTypeSuffix
    :   [fFdDgG]
    ;

fragment
HexadecimalFloatingPointLiteral
    :   HexSignificand BinaryExponent FloatTypeSuffix?
    ;

fragment
HexSignificand
    :   HexNumeral Dot?
    |   Zero [xX] HexDigits? Dot HexDigits
    ;

fragment
BinaryExponent
    :   BinaryExponentIndicator SignedInteger
    ;

fragment
BinaryExponentIndicator
    :   [pP]
    ;

fragment
Dot :   '.'
    ;

// §3.10.3 Boolean Literals

BooleanLiteral
    :   'true'
    |   'false'
    ;


// §3.10.6 Escape Sequences for Character and String Literals

fragment
EscapeSequence
    :   Backslash [btnfrs"'\\]
    |   OctalEscape
    |   UnicodeEscape
    |   DollarEscape
    |   LineEscape
    ;


fragment
OctalEscape
    :   Backslash OctalDigit
    |   Backslash OctalDigit OctalDigit
    |   Backslash ZeroToThree OctalDigit OctalDigit
    ;

// Groovy allows 1 or more u's after the backslash
fragment
UnicodeEscape
    :   Backslash 'u' HexDigit HexDigit HexDigit HexDigit
    ;

fragment
ZeroToThree
    :   [0-3]
    ;

// Groovy Escape Sequences

fragment
DollarEscape
    :   Backslash Dollar
    ;

fragment
LineEscape
    :   Backslash LineTerminator
    ;

fragment
LineTerminator
    :   '\r'? '\n' | '\r'
    ;

fragment
SlashEscape
    :   Backslash Slash
    ;

fragment
Backslash
    :   '\\'
    ;

fragment
Slash
    :   '/'
    ;

fragment
Dollar
    :   '$'
    ;

fragment
GStringQuotationMark
    :   '"'
    ;

fragment
SqStringQuotationMark
    :   '\''
    ;

fragment
TdqStringQuotationMark
    :   '"""'
    ;

fragment
TsqStringQuotationMark
    :   '\'\'\''
    ;

fragment
DollarSlashyGStringQuotationMarkBegin
    :   '$/'
    ;

fragment
DollarSlashyGStringQuotationMarkEnd
    :   '/$'
    ;

fragment
DollarSlashEscape
    :   '$/$'
    ;

fragment
DollarDollarEscape
    :   '$$'
    ;

// §3.10.7 The Null Literal
NullLiteral
    :   'null'
    ;

// Groovy Operators

RANGE_INCLUSIVE     : '..';
RANGE_EXCLUSIVE     : '..<';
SPREAD_DOT          : '*.';
SAFE_DOT            : '?.';
SAFE_CHAIN_DOT      : '??.';
ELVIS               : '?:';
METHOD_POINTER      : '.&';
METHOD_REFERENCE    : '::';
REGEX_FIND          : '=~';
REGEX_MATCH         : '==~';
POWER               : '**';
POWER_ASSIGN        : '**=';
SPACESHIP           : '<=>';
IDENTICAL           : '===';
NOT_IDENTICAL       : '!==';
ARROW               : '->';

// !internalPromise will be parsed as !in ternalPromise, so semantic predicates are necessary
NOT_INSTANCEOF      : '!instanceof' { isFollowedBy(_input, ' ', '\t', '\r', '\n') }?;
NOT_IN              : '!in'         { isFollowedBy(_input, ' ', '\t', '\r', '\n', '[', '(', '{') }?;


// §3.11 Separators

LPAREN          : '('  { this.enterParen();     } -> pushMode(DEFAULT_MODE);
RPAREN          : ')'  { this.exitParen();      } -> popMode;

LBRACE          : '{'  { this.enterParen();     } -> pushMode(DEFAULT_MODE);
RBRACE          : '}'  { this.exitParen();      } -> popMode;

LBRACK          : '['  { this.enterParen();     } -> pushMode(DEFAULT_MODE);
RBRACK          : ']'  { this.exitParen();      } -> popMode;

SEMI            : ';';
COMMA           : ',';
DOT             : Dot;

// §3.12 Operators

ASSIGN          : '=';
GT              : '>';
LT              : '<';
NOT             : '!';
BITNOT          : '~';
QUESTION        : '?';
COLON           : ':';
EQUAL           : '==';
LE              : '<=';
GE              : '>=';
NOTEQUAL        : '!=';
AND             : '&&';
OR              : '||';
INC             : '++';
DEC             : '--';
ADD             : '+';
SUB             : '-';
MUL             : '*';
DIV             : Slash;
BITAND          : '&';
BITOR           : '|';
XOR             : '^';
MOD             : '%';


ADD_ASSIGN      : '+=';
SUB_ASSIGN      : '-=';
MUL_ASSIGN      : '*=';
DIV_ASSIGN      : '/=';
AND_ASSIGN      : '&=';
OR_ASSIGN       : '|=';
XOR_ASSIGN      : '^=';
MOD_ASSIGN      : '%=';
LSHIFT_ASSIGN   : '<<=';
RSHIFT_ASSIGN   : '>>=';
URSHIFT_ASSIGN  : '>>>=';
ELVIS_ASSIGN    : '?=';


// §3.8 Identifiers (must appear after all keywords in the grammar)
CapitalizedIdentifier
    :   [A-Z] JavaLetterOrDigit*
    ;

Identifier
    :   JavaLetter JavaLetterOrDigit*
    ;

fragment
IdentifierInGString
    :   JavaLetterInGString JavaLetterOrDigitInGString*
    ;

fragment
JavaLetter
    :   [a-zA-Z$_] // these are the "java letters" below 0x7F
    |   // covers all characters above 0x7F which are not a surrogate
        ~[\u0000-\u007F\uD800-\uDBFF]
        { Character.isJavaIdentifierStart(_input.LA(-1)) && !Character.isIdentifierIgnorable(_input.LA(-1)) }?
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
        { Character.isJavaIdentifierStart(Character.toCodePoint((char) _input.LA(-2), (char) _input.LA(-1))) }?
    ;

fragment
JavaLetterInGString
    :   JavaLetter { _input.LA(-1) != '$' }?
    ;

fragment
JavaLetterOrDigit
    :   [a-zA-Z0-9$_] // these are the "java letters or digits" below 0x7F
    |   // covers all characters above 0x7F which are not a surrogate
        ~[\u0000-\u007F\uD800-\uDBFF]
        { Character.isJavaIdentifierPart(_input.LA(-1)) && !Character.isIdentifierIgnorable(_input.LA(-1)) }?
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
        { Character.isJavaIdentifierPart(Character.toCodePoint((char) _input.LA(-2), (char) _input.LA(-1))) }?
    ;

fragment
JavaLetterOrDigitInGString
    :   JavaLetterOrDigit  { _input.LA(-1) != '$' }?
    ;

fragment
ShCommand
    :   ~[\r\n\uFFFF]*
    ;

//
// Additional symbols not defined in the lexical specification
//

AT : '@';
ELLIPSIS : '...';

//
// Whitespace, line escape and comments
//
WS  : ([ \t]+ | LineEscape+) -> skip
    ;

// Inside (...) and [...] but not {...}, ignore newlines.
NL  : LineTerminator   { this.ignoreTokenInsideParens(); }
    ;

// Multiple-line comments (including groovydoc comments)
ML_COMMENT
    :   '/*' .*? '*/'       { this.ignoreMultiLineCommentConditionally(); } -> type(NL)
    ;

// Single-line comments
SL_COMMENT
    :   '//' ~[\r\n\uFFFF]* { this.ignoreTokenInsideParens(); }             -> type(NL)
    ;

// Script-header comments.
// The very first characters of the file may be "#!".  If so, ignore the first line.
SH_COMMENT
    :   '#!' { require(0 == this.tokenIndex, "Shebang comment should appear at the first line", -2, true); } ShCommand (LineTerminator '#!' ShCommand)* -> skip
    ;

// Unexpected characters will be handled by groovy parser later.
UNEXPECTED_CHAR
    :   .
    ;
