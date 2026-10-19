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
 * The Groovy grammar is based on the optimized Java grammar:
 * https://github.com/antlr/grammars-v4/tree/master/java/java
 */
lexer grammar GroovyLexer;

options {
    superClass = AbstractLexer;
}

@header {
    import java.util.*;
    import java.util.function.Supplier;
    import org.apache.groovy.util.Maps;
    import static org.apache.groovy.parser.antlr4.SemanticPredicates.*;
}

@members {
    private boolean errorIgnored;
    private long tokenIndex;
    /** Previous default-channel token. {@link Token#INVALID_TYPE} at the start of an input. */
    private int  lastTokenType = Token.INVALID_TYPE;
    /**
     * Last default-channel token whose type is not {@code NL}.
     * Stays {@link Token#INVALID_TYPE} until one is seen. Not {@link Token#EOF}:
     * a negative index is rejected by {@link java.util.BitSet#get(int)}.
     */
    private int  lastRealTokenType = Token.INVALID_TYPE;
    /**
     * True after a member selector ({@code . ?. *. ??. .& :: @}) until the
     * name that follows it is emitted. Newlines and comments (both {@code NL})
     * do not clear it. Type arguments ({@code foo.<T>name}, including
     * qualified names inside them) stay inside this window.
     */
    private boolean pendingMemberName;
    /** True when {@link #lastRealTokenType} is that name. */
    private boolean lastRealIsMemberName;
    /**
     * True when a value was emitted immediately before {@link #lastRealTokenType}
     * (newlines ignored). Distinguishes infix {@code in} from the name {@code in}.
     */
    private boolean lastRealPrecededByValue;
    /** Nesting of {@code <...>} while {@link #pendingMemberName} is set. */
    private int memberTypeArgDepth;
    /**
     * Nesting of {@code ()}, {@code []}, and {@code {}} inside that
     * {@code <...>}. Comparisons there must not move {@link #memberTypeArgDepth}.
     */
    private int memberTypeArgGroupDepth;

    /**
     * When {@code false}, the {@code val} keyword is treated as a regular
     * identifier (lexed as IDENTIFIER, not VAL). This can be used as a porting
     * aid for migrating to Groovy 6 if affected by the known breaking edge cases.
     * Controlled by system property {@code groovy.val.enabled} (default: {@code true}).
     */
    private static final boolean VAL_ENABLED =
            Boolean.parseBoolean(System.getProperty("groovy.val.enabled", "true"));
    private boolean isValEnabled() { return VAL_ENABLED; }

    /**
     * Record the index and token type of the current token while emitting tokens.
     */
    @Override
    public void emit(Token token) {
        this.tokenIndex++;

        int tokenType = token.getType();
        // EOF is negative. Recording it makes the next slashy decision call
        // BitSet.get(-1). setInputStream rewinds only through reset().
        if (tokenType >= 0 && Token.DEFAULT_CHANNEL == token.getChannel()) {
            if (NL != tokenType) {
                this.noteRealToken(tokenType);
            }
            this.lastTokenType = tokenType;
        }

        if (RollBackOne == tokenType) {
            this.rollbackOneChar();
        }

        super.emit(token);
    }

    @Override
    public void reset() {
        super.reset();
        this.lastTokenType = Token.INVALID_TYPE;
        this.lastRealTokenType = Token.INVALID_TYPE;
        this.pendingMemberName = false;
        this.lastRealIsMemberName = false;
        this.lastRealPrecededByValue = false;
        this.memberTypeArgDepth = 0;
        this.memberTypeArgGroupDepth = 0;
    }

    /**
     * Token types after which {@code /.../} is division, not a slashy string
     * ({@code a++ / b}, {@code list[i] / n}, {@code obj.class / 2}).
     * Keywords that introduce an expression stay out of this set so
     * {@code return /a/} and {@code x in /a/} remain strings.
     * {@link java.util.BitSet} gives O(1) tests on this hot decision.
     */
    private static final BitSet REGEX_CHECK_SET = new BitSet();
    /**
     * Keywords followed by an expression or a statement. A slashy after them
     * is a string ({@code return /a/}, {@code else /a/}) unless the keyword
     * is a member name ({@code obj.return / 2}).
     */
    private static final BitSet STATEMENT_INTRODUCER_SET = new BitSet();
    /** {@code . ?. *. ??. .& :: @} — the name that follows is a member. */
    private static final BitSet MEMBER_SELECTOR_SET = new BitSet();
    static {
        int[] regexCheckTypes = {
            DEC,
            INC,
            THIS,
            RBRACE,
            RBRACK,
            RPAREN,
            GStringEnd,
            NullLiteral,
            StringLiteral,
            BooleanLiteral,
            IntegerLiteral,
            FloatingPointLiteral,
            Identifier, CapitalizedIdentifier,
            // Value-ending keywords. Not statement introducers, and not {@code in}
            // (infix {@code x in /a/} must stay a string). See isSlashyForbidden.
            AS, DEF, TRAIT, THREADSAFE, ASYNC,
            ABSTRACT, BREAK, CATCH, CLASS, CONST, CONTINUE, DEFAULT,
            BuiltInPrimitiveType,
            ENUM, EXTENDS, FINAL, FINALLY, FOR, GOTO, IF, IMPLEMENTS, IMPORT,
            INSTANCEOF, INTERFACE, MODULE, NATIVE, NEW, NON_SEALED, PACKAGE,
            PERMITS, PRIVATE, PROTECTED, PUBLIC, RECORD, SEALED, STATIC,
            STRICTFP, SUPER, SWITCH, SYNCHRONIZED, THROWS, TRANSIENT, TRY,
            VAL, VAR, VOID, VOLATILE, WHILE
        };
        int[] statementIntroducers = {
            ASSERT, AWAIT, CASE, DEFER, DO, ELSE, RETURN, THROW, YIELD
        };
        int[] memberSelectors = {
            DOT, SAFE_DOT, SPREAD_DOT, SAFE_CHAIN_DOT,
            METHOD_POINTER, METHOD_REFERENCE, AT
        };
        for (int t : regexCheckTypes) {
            REGEX_CHECK_SET.set(t);
        }
        for (int t : statementIntroducers) {
            STATEMENT_INTRODUCER_SET.set(t);
        }
        for (int t : memberSelectors) {
            MEMBER_SELECTOR_SET.set(t);
        }
    }

    /**
     * Remember whether {@code tokenType} is a member name, and whether a
     * value preceded it. {@code NL} is not passed here.
     */
    private void noteRealToken(final int tokenType) {
        // A member name is a value even when it is not an introducer (obj.in),
        // and so is a preceding in (in in /a/). yield, await, and defer are
        // not values: yield in / 2 stays division, so for (yield in /a/) is
        // not a slashy.
        this.lastRealPrecededByValue = isDivisionOperand(this.lastRealTokenType)
                || this.lastRealIsMemberName
                || IN == this.lastRealTokenType;
        if (this.pendingMemberName && this.memberTypeArgDepth > 0) {
            // foo.<a.b>name and foo.<Map<K, V>>name: dots and names inside
            // the arguments are not a new member selection.
            this.noteTypeArgumentToken(tokenType);
            this.lastRealIsMemberName = false;
        } else if (isMemberSelector(tokenType)) {
            this.lastRealIsMemberName = false;
            this.pendingMemberName = true;
            this.memberTypeArgDepth = 0;
            this.memberTypeArgGroupDepth = 0;
        } else if (this.pendingMemberName && LT == tokenType) {
            this.lastRealIsMemberName = false;
            this.memberTypeArgDepth = 1;
            this.memberTypeArgGroupDepth = 0;
        } else {
            this.lastRealIsMemberName = this.pendingMemberName;
            this.pendingMemberName = false;
            this.memberTypeArgDepth = 0;
            this.memberTypeArgGroupDepth = 0;
        }
        this.lastRealTokenType = tokenType;
    }

    /**
     * Track {@code <}/{@code >} that belong to the type argument. {@code >}
     * inside {@code ()}, {@code []}, or {@code {}} is a comparison or a shift
     * ({@code foo.<@Ann(x > 1) T>name}).
     */
    private void noteTypeArgumentToken(final int tokenType) {
        if (this.memberTypeArgGroupDepth > 0) {
            if (isTypeArgGroupOpen(tokenType)) {
                this.memberTypeArgGroupDepth++;
            } else if (isTypeArgGroupClose(tokenType)) {
                this.memberTypeArgGroupDepth--;
            }
            return;
        }
        if (isTypeArgGroupOpen(tokenType)) {
            this.memberTypeArgGroupDepth = 1;
            return;
        }
        if (LT == tokenType) {
            this.memberTypeArgDepth++;
        } else if (GT == tokenType) {
            this.memberTypeArgDepth--;
        }
    }

    private static boolean isTypeArgGroupOpen(final int tokenType) {
        return LPAREN == tokenType || LBRACK == tokenType || LBRACE == tokenType;
    }

    private static boolean isTypeArgGroupClose(final int tokenType) {
        return RPAREN == tokenType || RBRACK == tokenType || RBRACE == tokenType;
    }

    private boolean isRegexAllowed() {
        return !isSlashyForbidden(this.lastTokenType);
    }

    /**
     * A {@code /} that reaches EOF is an unclosed slashy string only when a
     * closed slashy would have been allowed there, looking through {@code NL}
     * to the last real token. {@code 9 \n / \n 3} and {@code obj.class \n / \n 2}
     * stay division; {@code s = /hello} and {@code return /hello} do not.
     * {@link Token#INVALID_TYPE} (no real token yet, including a leading newline)
     * is not an operand. {@code NL} itself still allows a closed slashy to
     * start the next line ({@code obj.return \n /a/}).
     */
    private boolean isUnclosedSlashy() {
        int decision = this.lastTokenType == NL ? this.lastRealTokenType : this.lastTokenType;
        return !isSlashyForbidden(decision);
    }

    /**
     * Whether {@code /} after {@code tokenType} is division.
     * {@code return /a/} is a string; {@code obj.return / 2} and {@code obj.class / 2}
     * are division; {@code x in /a/} is a string; {@code in / 2} is division.
     * Passing {@code NL} (not a real token) returns false so a closed slashy
     * may start a line.
     */
    private boolean isSlashyForbidden(final int tokenType) {
        if (isDivisionOperand(tokenType)) {
            return true;
        }
        // The decision token is not the one noteRealToken just described
        // (isRegexAllowed sees NL). Do not apply the member-name or {@code in} rule.
        if (tokenType != this.lastRealTokenType) {
            return false;
        }
        if (IN == tokenType) {
            return !this.lastRealPrecededByValue;
        }
        return this.lastRealIsMemberName && isStatementIntroducer(tokenType);
    }

    /** {@code REGEX_CHECK_SET} rejects a negative type, and {@link Token#EOF} is not an operand. */
    private static boolean isDivisionOperand(final int tokenType) {
        return tokenType >= 0 && REGEX_CHECK_SET.get(tokenType);
    }

    private static boolean isStatementIntroducer(final int tokenType) {
        return STATEMENT_INTRODUCER_SET.get(tokenType);
    }

    private static boolean isMemberSelector(final int tokenType) {
        return MEMBER_SELECTOR_SET.get(tokenType);
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

        @SuppressWarnings("unused")
        public int getLine() {
            return line;
        }

        @SuppressWarnings("unused")
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

        // We just care about "(", "[" and "?[", inside which the new lines will be ignored.
        // Notice: the new lines between "{" and "}" can not be ignored.
        if (null == paren) {
            return false;
        }

        String text = paren.getText();

        return ("(".equals(text) && TRY != paren.getLastTokenType()) // we don't treat try-paren(i.e. try (....)) as parenthesis
                    || "[".equals(text) || "?[".equals(text);
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
        } catch (EmptyStackException ignore) { // raised when parens are unmatched: too many ), ], or }
        }

        return Integer.MIN_VALUE;
    }

    private void addComment(int type) {
        Supplier<String> textSupplier = () -> _input.getText(Interval.of(_tokenStartCharIndex, getCharIndex() - 1));
        handleComment(type, textSupplier);
    }

    protected void handleComment(int type, Supplier<String> textSupplier) {
    }

    private static boolean isJavaIdentifierStartAndNotIdentifierIgnorable(int codePoint) {
        return Character.isJavaIdentifierStart(codePoint) && !Character.isIdentifierIgnorable(codePoint);
    }

    private static boolean isJavaIdentifierPartAndNotIdentifierIgnorable(int codePoint) {
        return Character.isJavaIdentifierPart(codePoint) && !Character.isIdentifierIgnorable(codePoint);
    }

    /**
     * Code point just consumed. After a UTF-16 surrogate pair, {@code LA(-1)}
     * is the low surrogate and is never uppercase — {@code Character.isUpperCase}
     * must see the pair (GROOVY-12398).
     */
    private int lastConsumedCodePoint() {
        int c1 = _input.LA(-1);
        int c2 = _input.LA(-2);
        if (c1 >= Character.MIN_LOW_SURROGATE && c1 <= Character.MAX_LOW_SURROGATE
                && c2 >= Character.MIN_HIGH_SURROGATE && c2 <= Character.MAX_HIGH_SURROGATE) {
            return Character.toCodePoint((char) c2, (char) c1);
        }
        return c1;
    }

    private boolean isSupplementaryCapitalizedIdentifierStart() {
        int cp = lastConsumedCodePoint();
        return Character.isJavaIdentifierStart(cp) && Character.isUpperCase(cp);
    }

    private boolean isSupplementaryUncapitalizedIdentifierStart() {
        int cp = lastConsumedCodePoint();
        return Character.isJavaIdentifierStart(cp) && !Character.isUpperCase(cp);
    }

    public boolean isErrorIgnored() {
        return errorIgnored;
    }

    public void setErrorIgnored(boolean errorIgnored) {
        this.errorIgnored = errorIgnored;
    }
}


// §3.10.5 String Literals
// A quote that never closes matches nothing here, so it is UNEXPECTED_CHAR.
// requireUnexpectedCharacter maps that quote to "Unclosed string literal"
// unless the remainder is an illegal escape. `/` is also DIV, so an unclosed
// `/.../` cannot use that fall-through and is an EOF alternative of this rule.
// isUnclosedSlashy() is the first edge of that alternative, before any body
// character is read. `*` is safe because the follower is EOF, not `/`:
// `//` is not an empty slashy. EOF stays here so a second rule cannot
// swallow source after a real closer.
// An interpolated `/text $name` with no closing `/` is SlashyGStringBegin
// and a GString mode, not this alternative, so it does not say
// "Unclosed string literal". `$/` has no EOF alternative either:
// `($/2)` and `$/=` are division.
StringLiteral
    :   GStringQuotationMark  DqStringCharacter*  GStringQuotationMark
    |   SqStringQuotationMark  SqStringCharacter*  SqStringQuotationMark
    |   Slash { this.isRegexAllowed() && _input.LA(1) != '*' }?
            (   SlashyStringCharacter+ Slash
            |   { this.isUnclosedSlashy() }? SlashyStringCharacter* EOF { requireUnclosedString(errorIgnored); }
            )

    |   TdqStringQuotationMark  TdqStringCharacter*  TdqStringQuotationMark
    |   TsqStringQuotationMark  TsqStringCharacter*  TsqStringQuotationMark
    // `$/` is also identifier `$` plus division. An EOF alternative here would
    // reject `($/2)` and `$/=`. Unclosed `$/...` stays a later error.
    |   DollarSlashyGStringQuotationMarkBegin  DollarSlashyStringCharacter+  DollarSlashyGStringQuotationMarkEnd
    ;

GStringBegin
    :   GStringQuotationMark DqStringCharacter* Dollar -> pushMode(DQ_GSTRING_MODE), pushMode(GSTRING_TYPE_SELECTOR_MODE)
    ;
TdqGStringBegin
    :   TdqStringQuotationMark   TdqStringCharacter* Dollar -> type(GStringBegin), pushMode(TDQ_GSTRING_MODE), pushMode(GSTRING_TYPE_SELECTOR_MODE)
    ;
// No EOF alternative. A missing closer (`/hello $name`) stays in the GString
// modes and keeps that diagnostic; it is not "Unclosed string literal".
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
    :   '.' IdentifierInGString
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

// character in the dollar slashy string. e.g. $/a/$
fragment DollarSlashyStringCharacter
    :   DollarDollarEscape
    |   DollarSlashDollarEscape { _input.LA(-4) != '$' }?
    |   DollarSlashEscape { _input.LA(1) != '$' }?
    |   Slash { _input.LA(1) != '$' }?
    |   Dollar { !isFollowedByJavaLetterInGString(_input) }?
    |   ~[/$\u0000]
    ;

// Groovy keywords
AS              : 'as';
DEF             : 'def';
IN              : 'in';
TRAIT           : 'trait';
THREADSAFE      : 'threadsafe'; // reserved keyword
ASYNC           : 'async';
AWAIT           : 'await';
DEFER           : 'defer';

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
INTERFACE     : 'interface';

fragment
INT           : 'int';

fragment
LONG          : 'long';

MODULE        : 'module';
NATIVE        : 'native';
NEW           : 'new';
NON_SEALED    : 'non-sealed';
PACKAGE       : 'package';
PERMITS       : 'permits';
PRIVATE       : 'private';
PROTECTED     : 'protected';
PUBLIC        : 'public';
RECORD        : 'record';
RETURN        : 'return';
SEALED        : 'sealed';

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
VAL           : 'val' {isValEnabled()}?;
VAR           : 'var';
VOID          : 'void';
VOLATILE      : 'volatile';
WHILE         : 'while';
YIELD         : 'yield';

// §3.10.1 Integer Literals
// Digit sequences use character classes (optimized Java grammar style) so the
// lexer DFA does not walk a chain of one-character fragment rules. The invalid
// octal alt validates once at the end rather than counting digits in an action
// loop, which would prevent DFA compilation of this token.

IntegerLiteral
    :   (   DecimalIntegerLiteral
        |   HexIntegerLiteral
        |   OctalIntegerLiteral
        |   BinaryIntegerLiteral
        ) (Underscore { require(errorIgnored, "Number ending with underscores is invalid", -1, false); })?

    // !!! Error Alternative !!!
    |   '0' [0-9]+ { requireInvalidOctal(errorIgnored); } IntegerTypeSuffix?
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
    :   '0'
    |   [1-9] (Digits? | Underscores Digits)
    ;

fragment
Digits
    :   [0-9] ([0-9_]* [0-9])?
    ;

fragment
Underscores
    :   '_'+
    ;

fragment
Underscore
    :   '_'
    ;

fragment
HexNumeral
    :   '0' [xX] HexDigits
    ;

fragment
HexDigits
    :   HexDigit ([0-9a-fA-F_]* HexDigit)?
    ;

fragment
HexDigit
    :   [0-9a-fA-F]
    ;

fragment
OctalNumeral
    :   '0' '_'* OctalDigits
    ;

fragment
OctalDigits
    :   [0-7] ([0-7_]* [0-7])?
    ;

fragment
BinaryNumeral
    :   '0' [bB] BinaryDigits
    ;

fragment
BinaryDigits
    :   [01] ([01_]* [01])?
    ;

// §3.10.2 Floating-Point Literals

FloatingPointLiteral
    :   (   DecimalFloatingPointLiteral
        |   HexadecimalFloatingPointLiteral
        ) (Underscore { require(errorIgnored, "Number ending with underscores is invalid", -1, false); })?
    ;

fragment
DecimalFloatingPointLiteral
    :   Digits? '.' Digits ExponentPart? FloatTypeSuffix?
    |   Digits ExponentPart FloatTypeSuffix?
    |   Digits FloatTypeSuffix
    ;

fragment
ExponentPart
    :   [eE] [+\-]? Digits
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
    :   HexNumeral '.'?
    |   '0' [xX] HexDigits? '.' HexDigits
    ;

fragment
BinaryExponent
    :   [pP] [+\-]? Digits
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
    :   Backslash [0-7]
    |   Backslash [0-7] [0-7]
    |   Backslash [0-3] [0-7] [0-7]
    ;

// Groovy allows 1 or more u's after the backslash
fragment
UnicodeEscape
    :   Backslash 'u' HexDigit HexDigit HexDigit HexDigit
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

// escaped forward slash
fragment
DollarSlashEscape
    :   '$/'
    ;

// escaped dollar sign
fragment
DollarDollarEscape
    :   '$$'
    ;

// escaped dollar slashy string delimiter
fragment
DollarSlashDollarEscape
    :   '$/$'
    ;

// §3.10.7 The Null Literal
NullLiteral
    :   'null'
    ;

// Groovy Operators

RANGE_INCLUSIVE         : '..';
RANGE_EXCLUSIVE_LEFT    : '<..';
RANGE_EXCLUSIVE_RIGHT   : '..<';
RANGE_EXCLUSIVE_FULL    : '<..<';
SPREAD_DOT              : '*.';
SAFE_DOT                : '?.';
SAFE_INDEX              : '?[' { this.enterParen();     } -> pushMode(DEFAULT_MODE);
SAFE_CHAIN_DOT          : '??.';
ELVIS                   : '?:';
METHOD_POINTER          : '.&';
METHOD_REFERENCE        : '::';
REGEX_FIND              : '=~';
REGEX_MATCH             : '==~';
POWER                   : '**';
POWER_ASSIGN            : '**=';
SPACESHIP               : '<=>';
IDENTICAL               : '===';
IMPLIES                 : '==>';
NOT_IDENTICAL           : '!==';
ARROW                   : '->';

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
DOT             : '.';

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
// ASCII is a pure-DFA split ([A-Z] vs [a-z$_]) — no predicate on that hot path.
// BMP non-ASCII still uses LA(-1). Supplementary-plane letters must use the
// decoded code point: LA(-1) after a surrogate pair is the low surrogate,
// which is never uppercase, so the old isUpperCase(LA(-1)) test silently
// classified every supplementary identifier as Identifier (GROOVY-12398).
CapitalizedIdentifier
    :   [A-Z] JavaLetterOrDigit*
    |   ~[\u0000-\u007F\uD800-\uDBFF]
        { isJavaIdentifierStartAndNotIdentifierIgnorable(_input.LA(-1)) && Character.isUpperCase(_input.LA(-1)) }?
        JavaLetterOrDigit*
    |   [\uD800-\uDBFF] [\uDC00-\uDFFF]
        { isSupplementaryCapitalizedIdentifierStart() }?
        JavaLetterOrDigit*
    ;

Identifier
    :   [a-z$_] JavaLetterOrDigit*
    |   ~[\u0000-\u007F\uD800-\uDBFF]
        { isJavaIdentifierStartAndNotIdentifierIgnorable(_input.LA(-1)) && !Character.isUpperCase(_input.LA(-1)) }?
        JavaLetterOrDigit*
    |   [\uD800-\uDBFF] [\uDC00-\uDFFF]
        { isSupplementaryUncapitalizedIdentifierStart() }?
        JavaLetterOrDigit*
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
        { isJavaIdentifierStartAndNotIdentifierIgnorable(_input.LA(-1)) }?
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
        { isJavaIdentifierPartAndNotIdentifierIgnorable(_input.LA(-1)) }?
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
NL  : LineTerminator   { ignoreTokenInsideParens(); }
    ;

// Multiple-line comments (including groovydoc comments).
// One outermost alt so `-> type(NL)` is legal. `.*?` then (`*/` | EOF) stops
// at the first closer; a *separate* lexer rule for unclosed comments would
// win by longest-match and swallow trailing source. javac reports
// "unclosed comment" at the opener; requireUnclosedComment keeps the caret there.
ML_COMMENT
    :   '/*' .*? ('*/' | EOF { requireUnclosedComment(errorIgnored); })
        { addComment(0); ignoreMultiLineCommentConditionally(); } -> type(NL)
    ;

// Single-line comments
SL_COMMENT
    :   '//' ~[\r\n\uFFFF]* { addComment(1); ignoreTokenInsideParens(); }             -> type(NL)
    ;

// Script-header comments.
// The very first characters of the file may be "#!".  If so, ignore the first line.
SH_COMMENT
    :   '#!' { require(errorIgnored || 0 == this.tokenIndex, "Shebang comment should appear at the first line", -2, false); } ShCommand (LineTerminator '#!' ShCommand)* -> skip
    ;

// Unexpected characters (and unclosed quotes / illegal string escapes).
// Display goes through AbstractLexer#getCharErrorDisplay. An unexpected
// quote is an unclosed string unless a scan-ahead finds an illegal escape
// in an otherwise closed literal (e.g. "C:\Users\me").
UNEXPECTED_CHAR
    :   . { requireUnexpectedCharacter(errorIgnored); }
    ;
