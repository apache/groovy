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

package org.apache.groovy.lsp.internal.feature;

import org.antlr.v4.runtime.Token;
import org.apache.groovy.lsp.internal.compile.AstQuery;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.compile.ImportSupport;
import org.apache.groovy.lsp.internal.util.GroovySourceTokens;
import org.apache.groovy.parser.antlr4.GroovyLexer;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeKind;

import java.util.ArrayList;
import java.util.List;

/**
 * Folding ranges for types, members, imports, comments and regions.
 */
public final class FoldingService {

    private static final String REGION = "region";
    private static final String ENDREGION = "endregion";

    public List<FoldingRange> folding(final CompiledDocument document) {
        List<FoldingRange> ranges = new ArrayList<>();
        if (document == null || document.getModule() == null) {
            return ranges;
        }
        for (ASTNode node : AstQuery.declarations(document.getModule())) {
            if (node.getLineNumber() > 0 && node.getLastLineNumber() > node.getLineNumber()) {
                FoldingRange range = new FoldingRange(node.getLineNumber() - 1, node.getLastLineNumber() - 1);
                ranges.add(range);
            }
        }
        addImportFolds(document.getModule(), ranges);
        List<Token> tokens = GroovySourceTokens.tokenize(document.getText());
        addCommentFolds(tokens, ranges);
        addRegionFolds(tokens, ranges);
        return ranges;
    }

    /**
     * Block comments are {@link GroovyLexer#NL} tokens whose text starts
     * with {@code /*}. The lexer rewrites {@code ML_COMMENT} to {@code NL},
     * so the type alone does not distinguish a comment from a newline.
     * String, slashy, and dollar-slashy text never produce those tokens.
     * An unclosed comment runs to EOF and is not folded.
     */
    private static void addCommentFolds(final List<Token> tokens, final List<FoldingRange> ranges) {
        for (Token token : tokens) {
            String body = token.getText();
            if (token.getType() != GroovyLexer.NL || body == null || !body.startsWith("/*") || !body.endsWith("*/")) {
                continue;
            }
            int start = token.getLine() - 1;
            int end = start + newlineCount(body);
            if (end > start) {
                FoldingRange range = new FoldingRange(start, end);
                range.setKind(FoldingRangeKind.Comment);
                ranges.add(range);
            }
        }
    }

    private static int newlineCount(final String body) {
        int count = 0;
        for (int i = 0; i < body.length(); i++) {
            if (body.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }

    private static void addRegionFolds(final List<Token> tokens, final List<FoldingRange> ranges) {
        List<Integer> open = new ArrayList<>();
        for (Token token : tokens) {
            String directive = regionDirective(token);
            if (directive != null) {
                int line = token.getLine() - 1;
                if (REGION.equals(directive)) {
                    open.add(line);
                } else if (ENDREGION.equals(directive) && !open.isEmpty()) {
                    int start = open.remove(open.size() - 1);
                    if (line > start) {
                        FoldingRange range = new FoldingRange(start, line);
                        range.setKind(FoldingRangeKind.Region);
                        ranges.add(range);
                    }
                }
            }
        }
    }

    private static String regionDirective(final Token token) {
        if (token == null || token.getType() != GroovyLexer.NL || token.getText() == null
                || !token.getText().startsWith("//")) {
            return null;
        }
        String body = token.getText().substring(2).strip();
        if (body.startsWith("#")) {
            body = body.substring(1).strip();
        }
        if (body.startsWith(REGION)) {
            return REGION;
        }
        if (body.startsWith(ENDREGION)) {
            return ENDREGION;
        }
        return null;
    }

    private static void addImportFolds(final ModuleNode module, final List<FoldingRange> ranges) {
        List<ImportNode> imports = ImportSupport.allImports(module);
        int start = Integer.MAX_VALUE;
        int end = -1;
        for (ImportNode imp : imports) {
            if (imp == null || imp.getLineNumber() <= 0) {
                continue;
            }
            start = Math.min(start, imp.getLineNumber() - 1);
            end = Math.max(end, Math.max(imp.getLastLineNumber(), imp.getLineNumber()) - 1);
        }
        if (end > start) {
            FoldingRange range = new FoldingRange(start, end);
            range.setKind(FoldingRangeKind.Imports);
            ranges.add(range);
        }
    }

}
