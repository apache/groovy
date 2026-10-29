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
package groovy.console.ui.text;

import groovy.console.ui.text.SmartDocumentFilter.Delimiter;
import groovy.lang.Tuple2;
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
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static groovy.lang.Tuple.tuple;

/**
 * Represents highlighter to highlight matched parentheses, brackets and curly braces when caret touching them
 *
 * @since 3.0.0
 */
public class MatchingHighlighter implements CaretListener {
    private final SmartDocumentFilter smartDocumentFilter;
    private final JTextPane textEditor;
    private final DefaultStyledDocument doc;
    /** Matched bracket texts, and whether the scan runs forward from the opener. */
    private static final Map<String, Tuple2<List<String>, Boolean>> PAREN_MAP = Maps.of(
            "(", tuple(List.of(")"), true),
            ")", tuple(List.of("("), false),
            "?[", tuple(List.of("]"), true),
            "[", tuple(List.of("]"), true),
            "]", tuple(List.of("[", "?["), false),
            "{", tuple(List.of("}"), true),
            "}", tuple(List.of("{"), false)
    );
    /** Replaced as a whole. The list itself is never mutated after publication. */
    private final AtomicReference<List<Tuple2<Position, Integer>>> highlightedTokenInfoList =
            new AtomicReference<>(List.of());

    /**
     * Creates a highlighter bound to the supplied document filter and editor.
     *
     * @param smartDocumentFilter the token source used to find matching delimiters
     * @param textEditor the text pane whose delimiters are highlighted
     */
    public MatchingHighlighter(SmartDocumentFilter smartDocumentFilter, JTextPane textEditor) {
        this.smartDocumentFilter = smartDocumentFilter;
        this.textEditor = textEditor;
        this.doc = (DefaultStyledDocument) textEditor.getStyledDocument();

        initStyles();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void caretUpdate(CaretEvent e) {
        highlight();
    }

    /**
     * Schedules highlighting for the delimiter nearest the caret.
     */
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
        for (int len = 2; len >= 1; len--) {
            try {
                f = caretPosition - len;
                c = doc.getText(f, len);

                if (PAREN_MAP.containsKey(c)) break;
            } catch (BadLocationException e1) {
                // ignore
            }

            try {
                f = caretPosition;
                c = doc.getText(f, len);

                if (PAREN_MAP.containsKey(c)) break;
            } catch (BadLocationException e1) {
                // ignore
            }

            if (2 == len) {
                try {
                    f = caretPosition - 1;
                    c = doc.getText(f, len);

                    if (PAREN_MAP.containsKey(c)) break;
                } catch (BadLocationException e1) {
                    // ignore
                }
            }
        }

        if (!PAREN_MAP.containsKey(c)) return;

        final int offset = f;
        final String p = c;

        highlightMatched(offset, p);
    }

    private void highlightMatched(int offset, String p) {
        List<Delimiter> latestTokenList = smartDocumentFilter.getLatestTokenList();
        Tuple2<List<String>, Boolean> pair = PAREN_MAP.get(p);
        List<String> matchedText = pair.getV1();
        boolean normalOrder = pair.getV2();
        Deque<Tuple2<Delimiter, Boolean>> stack = new ArrayDeque<>();

        Delimiter triggerToken = null;
        Delimiter matchedToken = null;

        for (ListIterator<Delimiter> iterator = latestTokenList.listIterator(normalOrder ? 0 : latestTokenList.size());
             normalOrder ? iterator.hasNext() : iterator.hasPrevious(); ) {
            Delimiter token = normalOrder ? iterator.next() : iterator.previous();

            if (p.equals(token.getText())) {
                stack.push(tuple(token, offset == token.getOffset()));
            } else if (matchedText.contains(token.getText())) {
                if (!stack.isEmpty()) {
                    Tuple2<Delimiter, Boolean> tokenAndTriggerFlagTuple = stack.pop();
                    if (tokenAndTriggerFlagTuple.getV2()) {
                        triggerToken = tokenAndTriggerFlagTuple.getV1();
                        matchedToken = token;
                        break;
                    }
                }
            }
        }

        if (null != triggerToken && null != matchedToken) {
            highlightToken(p, triggerToken);
            highlightToken(p, matchedToken);
            try {
                highlightedTokenInfoList.set(List.of(
                        tuple(doc.createPosition(triggerToken.getOffset()), triggerToken.getLength()),
                        tuple(doc.createPosition(matchedToken.getOffset()), matchedToken.getLength())
                ));
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
        Style style = StyleContext.getDefaultStyleContext().addStyle(highlightedStyleName(p), defaultStyle);
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

    private void highlightToken(String p, final Delimiter tokenToHighlight) {
        Style style = findHighlightedStyleByParen(p);
        doc.setCharacterAttributes(tokenToHighlight.getOffset(),
                tokenToHighlight.getLength(),
                style,
                true);
    }

    private void clearHighlighted() {
        List<Tuple2<Position, Integer>> highlighted = highlightedTokenInfoList.get();
        if (highlighted.isEmpty()) {
            return;
        }
        for (Tuple2<Position, Integer> highlightedTokenInfo : highlighted) {
            // Delimiters keep the default style. A saved attribute set can
            // land on the next token after the matched brace is deleted.
            doc.setCharacterAttributes(
                    highlightedTokenInfo.getV1().getOffset(),
                    highlightedTokenInfo.getV2(),
                    defaultStyle,
                    true
            );
        }
        highlightedTokenInfoList.set(List.of());
    }
}
