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
package org.apache.groovy.parser.antlr4.util;

import groovy.lang.Tuple2;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.groovy.parser.antlr4.GroovyParser;
import org.codehaus.groovy.ast.ASTNode;

import static groovy.lang.Tuple.tuple;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asBoolean;

/**
 * Utilities for configuring node positions
 */
public class PositionConfigureUtils {
    /**
     * Sets location(lineNumber, colNumber, lastLineNumber, lastColumnNumber) for node using standard context information.
     * Note: this method is implemented to be closed over ASTNode. It returns same node as it received in arguments.
     *
     * @param astNode Node to be modified.
     * @param ctx     Context from which information is obtained.
     * @return Modified astNode.
     */
    public static <T extends ASTNode> T configureAST(T astNode, GroovyParser.GroovyParserRuleContext ctx) {
        Token start = ctx.getStart();
        Token stop = ctx.getStop();

        astNode.setLineNumber(start.getLine());
        astNode.setColumnNumber(start.getCharPositionInLine() + 1);

        configureEndPosition(astNode, stop);

        return astNode;
    }

    public static Tuple2<Integer, Integer> endPosition(Token token) {
        String stopText = token.getText();
        int stopTextLength = 0;
        int newLineCnt = 0;
        if (null != stopText) {
            stopTextLength = stopText.length();
            newLineCnt = (int) StringUtils.countChar(stopText, '\n');
        }

        if (0 == newLineCnt) {
            return tuple(token.getLine(), token.getCharPositionInLine() + 1 + token.getText().length());
        } else { // e.g. GStringEnd contains newlines, we should fix the location info
            return tuple(token.getLine() + newLineCnt, stopTextLength - stopText.lastIndexOf('\n'));
        }
    }

    public static <T extends ASTNode> T configureAST(T astNode, TerminalNode terminalNode) {
        return configureAST(astNode, terminalNode.getSymbol());
    }

    public static <T extends ASTNode> T configureAST(T astNode, Token token) {
        astNode.setLineNumber(token.getLine());
        astNode.setColumnNumber(token.getCharPositionInLine() + 1);
        astNode.setLastLineNumber(token.getLine());
        astNode.setLastColumnNumber(token.getCharPositionInLine() + 1 + token.getText().length());

        return astNode;
    }

    public static <T extends ASTNode> T configureAST(T astNode, ASTNode source) {
        astNode.setLineNumber(source.getLineNumber());
        astNode.setColumnNumber(source.getColumnNumber());
        astNode.setLastLineNumber(source.getLastLineNumber());
        astNode.setLastColumnNumber(source.getLastColumnNumber());

        return astNode;
    }

    public static <T extends ASTNode> T configureAST(T astNode, GroovyParser.GroovyParserRuleContext ctx, ASTNode stop) {
        Token start = ctx.getStart();

        astNode.setLineNumber(start.getLine());
        astNode.setColumnNumber(start.getCharPositionInLine() + 1);

        if (asBoolean(stop)) {
            astNode.setLastLineNumber(stop.getLastLineNumber());
            astNode.setLastColumnNumber(stop.getLastColumnNumber());
        } else {
            configureEndPosition(astNode, start);
        }

        return astNode;
    }

    public static <T extends ASTNode> void configureEndPosition(T astNode, Token token) {
        Tuple2<Integer, Integer> endPosition = endPosition(token);
        astNode.setLastLineNumber(endPosition.getV1());
        astNode.setLastColumnNumber(endPosition.getV2());
    }

    public static <T extends ASTNode> T configureAST(T astNode, ASTNode start, ASTNode stop) {
        astNode.setLineNumber(start.getLineNumber());
        astNode.setColumnNumber(start.getColumnNumber());

        if (asBoolean(stop)) {
            astNode.setLastLineNumber(stop.getLastLineNumber());
            astNode.setLastColumnNumber(stop.getLastColumnNumber());
        } else {
            astNode.setLastLineNumber(start.getLastLineNumber());
            astNode.setLastColumnNumber(start.getLastColumnNumber());
        }

        return astNode;
    }
}
