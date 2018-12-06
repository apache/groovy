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
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.apache.groovy.parser.antlr4.GroovyParser.ASSIGN;
import static org.apache.groovy.parser.antlr4.GroovyParser.BuiltInPrimitiveType;
import static org.apache.groovy.parser.antlr4.GroovyParser.CapitalizedIdentifier;
import static org.apache.groovy.parser.antlr4.GroovyParser.DOT;
import static org.apache.groovy.parser.antlr4.GroovyParser.ExpressionContext;
import static org.apache.groovy.parser.antlr4.GroovyParser.Identifier;
import static org.apache.groovy.parser.antlr4.GroovyParser.LBRACK;
import static org.apache.groovy.parser.antlr4.GroovyParser.LPAREN;
import static org.apache.groovy.parser.antlr4.GroovyParser.LT;
import static org.apache.groovy.parser.antlr4.GroovyParser.PathExpressionContext;
import static org.apache.groovy.parser.antlr4.GroovyParser.PostfixExprAltContext;
import static org.apache.groovy.parser.antlr4.GroovyParser.PostfixExpressionContext;
import static org.apache.groovy.parser.antlr4.GroovyParser.StringLiteral;
import static org.apache.groovy.parser.antlr4.util.StringUtils.matches;

/**
 * Some semantic predicates for altering the behaviour of the lexer and parser
 */
public class SemanticPredicates {
    private static final Pattern NONSPACES_PATTERN = Pattern.compile("\\S+?");
    private static final Pattern LETTER_AND_LEFTCURLY_PATTERN = Pattern.compile("[a-zA-Z_{]");
    private static final Pattern NONSURROGATE_PATTERN = Pattern.compile("[^\u0000-\u007F\uD800-\uDBFF]");
    private static final Pattern SURROGATE_PAIR1_PATTERN = Pattern.compile("[\uD800-\uDBFF]");
    private static final Pattern SURROGATE_PAIR2_PATTERN = Pattern.compile("[\uDC00-\uDFFF]");

    public static boolean isFollowedByWhiteSpaces(CharStream cs) {
        for (int index = 1, c = cs.LA(index); !('\r' == c || '\n' == c || CharStream.EOF == c); index++, c = cs.LA(index)) {
            if (matches(String.valueOf((char) c), NONSPACES_PATTERN)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isFollowedBy(CharStream cs, char... chars) {
        int c1 = cs.LA(1);

        for (char c : chars) {
            if (c1 == c) {
                return true;
            }
        }

        return false;
    }

    public static boolean isFollowedByJavaLetterInGString(CharStream cs) {
        int c1 = cs.LA(1);

        if ('$' == c1) { // single $ is not a valid identifier
            return false;
        }

        String str1 = String.valueOf((char) c1);

        if (matches(str1, LETTER_AND_LEFTCURLY_PATTERN)) {
            return true;
        }

        if (matches(str1, NONSURROGATE_PATTERN)
                && Character.isJavaIdentifierPart(c1)) {
            return true;
        }

        int c2 = cs.LA(2);
        String str2 = String.valueOf((char) c2);

        if (matches(str1, SURROGATE_PAIR1_PATTERN)
                && matches(str2, SURROGATE_PAIR2_PATTERN)
                && Character.isJavaIdentifierPart(Character.toCodePoint((char) c1, (char) c2))) {

            return true;
        }

        return false;
    }

    /**
     * Check whether following a method name of command expression.
     * Method name should not end with "2: arguments" and "3: closure"
     *
     * @param context the preceding expression
     */
    public static boolean isFollowingArgumentsOrClosure(ExpressionContext context) {
        if (context instanceof PostfixExprAltContext) {
            List<ParseTree> peacChildren = ((PostfixExprAltContext) context).children;

            if (1 == peacChildren.size()) {
                ParseTree peacChild = peacChildren.get(0);

                if (peacChild instanceof PostfixExpressionContext) {
                    List<ParseTree>  pecChildren = ((PostfixExpressionContext) peacChild).children;

                    if (1 == pecChildren.size()) {
                        ParseTree pecChild = pecChildren.get(0);

                        if (pecChild instanceof PathExpressionContext) {
                            PathExpressionContext pec = (PathExpressionContext) pecChild;
                            int t = pec.t;

                            return (2 == t || 3 == t);
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Distinguish between method declaration and method call/constructor declaration
     */
    public static boolean isInvalidMethodDeclaration(TokenStream ts) {
        int tokenType = ts.LT(1).getType();

        return (Identifier == tokenType || CapitalizedIdentifier == tokenType || StringLiteral == tokenType)
                && LPAREN == (ts.LT(2).getType());
    }

    private static final Set<Integer> MODIFIER_SET =
            Collections.unmodifiableSet(ModifierNode.MODIFIER_OPCODE_MAP.keySet());
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
            int tokeTypeN = tokenType2;

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

        return // VOID == tokenType ||
                !(BuiltInPrimitiveType == tokenType || MODIFIER_SET.contains(tokenType))
                        && Character.isLowerCase(token.getText().codePointAt(0))
                        && !(ASSIGN == tokenType3 || (LT == tokenType2 || LBRACK == tokenType2));

    }

}
