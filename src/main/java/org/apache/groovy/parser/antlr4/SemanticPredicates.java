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
package org.apache.groovy.parser.antlr4;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ModifierNode;

import java.util.BitSet;

import static org.apache.groovy.parser.antlr4.GroovyParser.ASSIGN;
import static org.apache.groovy.parser.antlr4.GroovyParser.AT;
import static org.apache.groovy.parser.antlr4.GroovyParser.BuiltInPrimitiveType;
import static org.apache.groovy.parser.antlr4.GroovyParser.CapitalizedIdentifier;
import static org.apache.groovy.parser.antlr4.GroovyParser.DO;
import static org.apache.groovy.parser.antlr4.GroovyParser.DOT;
import static org.apache.groovy.parser.antlr4.GroovyParser.ExpressionContext;
import static org.apache.groovy.parser.antlr4.GroovyParser.FOR;
import static org.apache.groovy.parser.antlr4.GroovyParser.Identifier;
import static org.apache.groovy.parser.antlr4.GroovyParser.LBRACK;
import static org.apache.groovy.parser.antlr4.GroovyParser.LPAREN;
import static org.apache.groovy.parser.antlr4.GroovyParser.LT;
import static org.apache.groovy.parser.antlr4.GroovyParser.PostfixExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyParser.RPAREN;
import static org.apache.groovy.parser.antlr4.GroovyParser.StringLiteral;
import static org.apache.groovy.parser.antlr4.GroovyParser.WHILE;
import static org.apache.groovy.parser.antlr4.GroovyParser.YIELD;

/**
 * Semantic predicates for the lexer and parser.
 * <p>
 * Lexer helpers on this class run on the tokenisation hot path (GString
 * {@code $}, slashy strings, newline-as-separator). They use direct character
 * tests, not regular expressions. Parser helpers such as
 * {@link #isIdentifierAssign(TokenStream)} are O(1) bitset lookups so
 * {@code AdaptivePredict} can skip exploring assignment as an annotation
 * element value (GROOVY-12398).
 * </p>
 */
public class SemanticPredicates {
    private static final int PATH_EXPRESSION_ARGUMENTS = 2;
    private static final int PATH_EXPRESSION_CLOSURE_OR_LAMBDA = 3;

    /**
     * Token types accepted by {@code elementValuePairName} ({@code identifier | keywords}).
     * Used to distinguish {@code @Foo(a = 1)} (named pairs) from a single element value.
     * Must stay in sync with those two parser rules; {@code MODULE} is in
     * {@code identifier} but not in {@code keywords}.
     */
    private static final BitSet ELEMENT_VALUE_PAIR_NAME_TYPES = new BitSet();
    static {
        int[] types = {
                Identifier, CapitalizedIdentifier,
                GroovyParser.ABSTRACT, GroovyParser.AS, GroovyParser.ASSERT, GroovyParser.ASYNC, GroovyParser.AWAIT,
                GroovyParser.BREAK, GroovyParser.CASE, GroovyParser.CATCH, GroovyParser.CLASS, GroovyParser.CONST,
                GroovyParser.CONTINUE, GroovyParser.DEF, GroovyParser.DEFAULT, GroovyParser.DEFER, GroovyParser.DO,
                GroovyParser.ELSE, GroovyParser.ENUM, GroovyParser.EXTENDS, GroovyParser.FINAL, GroovyParser.FINALLY,
                GroovyParser.FOR, GroovyParser.GOTO, GroovyParser.IF, GroovyParser.IMPLEMENTS, GroovyParser.IMPORT,
                GroovyParser.IN, GroovyParser.INSTANCEOF, GroovyParser.INTERFACE, GroovyParser.NATIVE, GroovyParser.NEW,
                GroovyParser.NON_SEALED, GroovyParser.PACKAGE, GroovyParser.PERMITS, GroovyParser.RECORD,
                GroovyParser.RETURN, GroovyParser.SEALED, GroovyParser.STATIC, GroovyParser.STRICTFP, GroovyParser.SUPER,
                GroovyParser.SWITCH, GroovyParser.SYNCHRONIZED, GroovyParser.THIS, GroovyParser.THROW, GroovyParser.THROWS,
                GroovyParser.TRANSIENT, GroovyParser.TRAIT, GroovyParser.THREADSAFE, GroovyParser.TRY, GroovyParser.VAL,
                GroovyParser.VAR, GroovyParser.VOLATILE, GroovyParser.WHILE, GroovyParser.YIELD,
                GroovyParser.NullLiteral, GroovyParser.BooleanLiteral, BuiltInPrimitiveType, GroovyParser.VOID,
                GroovyParser.PUBLIC, GroovyParser.PROTECTED, GroovyParser.PRIVATE, GroovyParser.MODULE
        };
        for (int t : types) {
            ELEMENT_VALUE_PAIR_NAME_TYPES.set(t);
        }
    }

    /**
     * Check whether the next characters are only white spaces until the end of line or end of file.
     * Matches Java regex {@code \s} minus the newlines the loop already stops at: space, tab, VT, FF.
     */
    public static boolean isFollowedByWhiteSpaces(CharStream cs) {
        for (int index = 1, c = cs.LA(index); c != '\r' && c != '\n' && c != CharStream.EOF; index++, c = cs.LA(index)) {
            if (c != ' ' && c != '\t' && c != '\f' && c != '\u000B') {
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether the next character is one of the specified characters.
     */
    public static boolean isFollowedBy(CharStream cs, char... chars) {
        int c1 = cs.LA(1);

        for (char c : chars) {
            if (c1 == c) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check whether the next character is a valid Java identifier part or a left curly brace (for GString expressions).
     */
    public static boolean isFollowedByJavaLetterInGString(CharStream cs) {
        int c1 = cs.LA(1);

        if (c1 == '$' || c1 < 0) { // single $ is not a valid identifier; EOF is not either
            return false;
        }

        if (c1 == '{' || c1 == '_'
                || (c1 >= 'A' && c1 <= 'Z')
                || (c1 >= 'a' && c1 <= 'z')) {
            return true;
        }

        if (c1 <= 0x7F) {
            return false;
        }

        if (c1 >= 0xD800 && c1 <= 0xDBFF) {
            int c2 = cs.LA(2);
            return c2 >= 0xDC00 && c2 <= 0xDFFF
                    && Character.isJavaIdentifierPart(Character.toCodePoint((char) c1, (char) c2));
        }

        return Character.isJavaIdentifierPart(c1);
    }

    /**
     * {@code true} when the upcoming tokens are {@code name '=' ...}, i.e. a named
     * annotation element-value pair rather than a single element value. Inside
     * annotation parentheses newlines are already hidden, so {@code LT(2)} is the
     * token after the name.
     */
    public static boolean isIdentifierAssign(TokenStream ts) {
        int t1 = ts.LT(1).getType();
        return t1 >= 0 && ELEMENT_VALUE_PAIR_NAME_TYPES.get(t1) && ASSIGN == ts.LT(2).getType();
    }

    /**
     * Check whether following a method name of command expression.
     * Method name should not end with "2: arguments" and "3: closure"
     *
     * @param context the preceding expression
     */
    public static boolean isFollowingArgumentsOrClosure(ExpressionContext context) {
        if (!(context instanceof PostfixExprAltContext ctx)) return false;

        try {
            int pathExpressionType = ctx.postfixExpression().pathExpression().t;
            return PATH_EXPRESSION_ARGUMENTS == pathExpressionType || PATH_EXPRESSION_CLOSURE_OR_LAMBDA == pathExpressionType;
        } catch (RuntimeException e) {
            throw new GroovyBugError("Unexpected structure of expression context: " + context, e);
        }
    }

    /**
     * Distinguish between method declaration and method call/constructor declaration
     */
    public static boolean isInvalidMethodDeclaration(TokenStream ts) {
        int tokenType = ts.LT(1).getType();

        return (Identifier == tokenType || CapitalizedIdentifier == tokenType || StringLiteral == tokenType || YIELD == tokenType)
                && LPAREN == (ts.LT(2).getType());
    }

    private static final BitSet MODIFIER_TYPES = new BitSet();
    static {
        for (Integer tokenType : ModifierNode.MODIFIER_OPCODE_MAP.keySet()) {
            if (tokenType != null && tokenType >= 0) {
                MODIFIER_TYPES.set(tokenType);
            }
        }
    }

    /**
     * O(1) modifier-token membership. Negative types (e.g. {@link Token#EOF}) are never
     * modifiers
     */
    private static boolean isModifierType(final int tokenType) {
        return tokenType >= 0 && MODIFIER_TYPES.get(tokenType);
    }

    /**
     * Distinguish between local variable declaration and method call, e.g. `a b`
     */
    public static boolean isInvalidLocalVariableDeclaration(TokenStream ts) {
        int index = 2;
        Token token;
        int tokenType;
        int tokenType2 = ts.LT(index).getType();
        int tokenType3;

        if (DOT == tokenType2) {
            int tokeTypeN;

            do {
                index = index + 2;
                tokeTypeN = ts.LT(index).getType();
            } while (DOT == tokeTypeN);

            if (LT == tokeTypeN || LBRACK == tokeTypeN) {
                return false;
            }

            index = index - 1;
            tokenType2 = ts.LT(index + 1).getType();
        } else {
            index = 1;
        }

        token = ts.LT(index);
        tokenType = token.getType();
        tokenType3 = ts.LT(index + 2).getType();
        int nextCodePoint = token.getText().codePointAt(0);

        return // VOID == tokenType ||
                !(BuiltInPrimitiveType == tokenType || isModifierType(tokenType))
                        && !Character.isUpperCase(nextCodePoint)
                        && nextCodePoint != '@'
                        && !(ASSIGN == tokenType3 || (LT == tokenType2 || LBRACK == tokenType2))
                || (nextCodePoint == '@' && isAnnotatedLoopStatement(ts));

    }

    /**
     * When the input starts with one or more annotations, scan past them and check whether
     * the first non-annotation token is a loop keyword ({@code for}, {@code while}, {@code do}).
     * If so, the construct is an annotated loop statement, NOT a local variable declaration.
     *
     * @param ts the token stream positioned at the first annotation {@code @} token
     * @return {@code true} if annotations are followed by a loop keyword
     */
    static boolean isAnnotatedLoopStatement(TokenStream ts) {
        int idx = 1; // ts.LT(1) is '@'
        while (ts.LT(idx).getType() == AT) {
            idx += 2; // skip AT and annotation name
            // skip qualifier parts of a fully-qualified annotation name, e.g. @java.lang.Deprecated
            while (ts.LT(idx).getType() == DOT) {
                idx += 2; // skip DOT and next name element
            }
            // skip annotation arguments (parenthesised), handling nesting
            if (ts.LT(idx).getType() == LPAREN) {
                idx++;
                int depth = 1;
                while (depth > 0 && ts.LT(idx).getType() != Token.EOF) {
                    int t = ts.LT(idx++).getType();
                    if (t == LPAREN) depth++;
                    else if (t == RPAREN) depth--;
                }
            }
        }
        int afterAnnotations = ts.LT(idx).getType();
        return afterAnnotations == FOR || afterAnnotations == WHILE || afterAnnotations == DO;
    }
}
