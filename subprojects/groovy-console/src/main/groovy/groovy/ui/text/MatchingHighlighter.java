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
package groovy.ui.text;

import groovy.lang.Tuple2;
import groovy.lang.Tuple3;
import org.antlr.v4.runtime.Token;
import org.apache.groovy.util.Maps;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static groovy.lang.Tuple.tuple;
import static org.apache.groovy.parser.antlr4.GroovyLexer.LBRACE;
import static org.apache.groovy.parser.antlr4.GroovyLexer.LBRACK;
import static org.apache.groovy.parser.antlr4.GroovyLexer.LPAREN;
import static org.apache.groovy.parser.antlr4.GroovyLexer.RBRACE;
import static org.apache.groovy.parser.antlr4.GroovyLexer.RBRACK;
import static org.apache.groovy.parser.antlr4.GroovyLexer.RPAREN;

/**
 * Represents highlighter to highlight matched parentheses, brackets and curly braces when caret touching them
 *
 * @since 3.0.0
 */
public class MatchingHighlighter implements CaretListener {
    private final SmartDocumentFilter smartDocumentFilter;
    private final JTextPane textEditor;
    private final DefaultStyledDocument doc;
    private static final Map<String, Tuple3<Integer, Integer, Boolean>> PAREN_MAP = Maps.of(
            "(", tuple(LPAREN, RPAREN, true),
            ")", tuple(RPAREN, LPAREN, false),
            "[", tuple(LBRACK, RBRACK, true),
            "]", tuple(RBRACK, LBRACK, false),
            "{", tuple(LBRACE, RBRACE, true),
            "}", tuple(RBRACE, LBRACE, false)
    );
    private volatile List<Tuple2<Integer, Position>> highlightedTokenInfoList = Collections.emptyList();

    public MatchingHighlighter(SmartDocumentFilter smartDocumentFilter, JTextPane textEditor) {
        this.smartDocumentFilter = smartDocumentFilter;
        this.textEditor = textEditor;
        this.doc = (DefaultStyledDocument) textEditor.getStyledDocument();

        initStyles();
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        highlight();
    }

    public void highlight() {
        // `SwingUtilities.invokeLater` is used to avoid "java.lang.IllegalStateException: Attempt to mutate in notification"
        SwingUtilities.invokeLater(this::doHighlight);
    }

    private void doHighlight() {
        clearHighlighted();

        if (!smartDocumentFilter.isLatest()) {
            return;
        }

        int caretPosition = textEditor.getCaretPosition();
        int f = -1;
        String c = null;
        try {
            f = caretPosition - 1;
            c = doc.getText(f, 1);
        } catch (BadLocationException e1) {
            // ignore
        }

        if (!PAREN_MAP.containsKey(c)) {
            try {
                f = caretPosition;
                c = doc.getText(f, 1);
            } catch (BadLocationException e1) {
                // ignore
            }
        }

        if (!PAREN_MAP.containsKey(c)) {
            return;
        }

        final int offset = f;
        final String p = c;

        highlightMatched(offset, p);
    }

    private void highlightMatched(int offset, String p) {
        List<Token> latestTokenList = smartDocumentFilter.getLatestTokenList();
        Tuple3<Integer, Integer, Boolean> tokenTypeTuple = PAREN_MAP.get(p);
        int triggerTokenType = tokenTypeTuple.getV1();
        int matchedTokenType = tokenTypeTuple.getV2();
        boolean normalOrder = tokenTypeTuple.getV3();
        Deque<Tuple2<Token, Boolean>> stack = new ArrayDeque<>();

        Token triggerToken = null;
        Token matchedToken = null;

        for (ListIterator<Token> iterator = latestTokenList.listIterator(normalOrder ? 0 : latestTokenList.size());
             normalOrder ? iterator.hasNext() : iterator.hasPrevious(); ) {
            Token token = normalOrder ? iterator.next() : iterator.previous();

            int tokenType = token.getType();
            if (tokenType == triggerTokenType) {
                Boolean triggerFlag = offset == token.getStartIndex();

                stack.push(tuple(token, triggerFlag));
            } else if (tokenType == matchedTokenType) {
                Tuple2<Token, Boolean> tokenAndTriggerFlagTuple = stack.pop();
                if (tokenAndTriggerFlagTuple.getV2()) {
                    triggerToken = tokenAndTriggerFlagTuple.getV1();
                    matchedToken = token;
                    break;
                }
            }
        }

        if (null != triggerToken && null != matchedToken) {
            highlightToken(p, triggerToken);
            highlightToken(p, matchedToken);
            try {
                highlightedTokenInfoList = Arrays.asList(
                        tuple(triggerToken.getType(), doc.createPosition(triggerToken.getStartIndex())),
                        tuple(matchedToken.getType(), doc.createPosition(matchedToken.getStartIndex()))
                );
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private void initStyles() {
        PAREN_MAP.keySet().forEach(e -> createHighlightedStyleByParen(e));
    }

    private final StyleContext styleContext = StyleContext.getDefaultStyleContext();
    private final Style defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE);

    private void createHighlightedStyleByParen(String p) {
        Style style = StyleContext.getDefaultStyleContext().addStyle(highlightedStyleName(p), findStyleByTokenType(PAREN_MAP.get(p).getV1()));
        StyleConstants.setForeground(style, Color.YELLOW.darker());
        StyleConstants.setBold(style, true);
    }

    private static String highlightedStyleName(String p) {
        return "highlighted" + p;
    }

    private Style findHighlightedStyleByParen(String p) {
        Style style = styleContext.getStyle(highlightedStyleName(p));

        return null == style ? defaultStyle : style;
    }

    private Style findStyleByTokenType(int tokenType) {
        Style style = styleContext.getStyle(String.valueOf(tokenType));

        return null == style ? defaultStyle : style;
    }

    private void highlightToken(String p, final Token tokenToHighlight) {
        Style style = findHighlightedStyleByParen(p);
        doc.setCharacterAttributes(tokenToHighlight.getStartIndex(),
                1,
                style,
                true);
    }

    private void clearHighlighted() {
        if (!highlightedTokenInfoList.isEmpty()) {
            for (Tuple2<Integer, Position> highlightedToken : highlightedTokenInfoList) {
                doc.setCharacterAttributes(
                        highlightedToken.getV2().getOffset(),
                        1,
                        findStyleByTokenType(highlightedToken.getV1()),
                        true
                );
            }

            highlightedTokenInfoList = Collections.emptyList();
        }
    }
}
