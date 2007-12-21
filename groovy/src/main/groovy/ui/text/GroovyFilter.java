/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.ui.text;

import java.awt.Color;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleContext;


/**
 *
 * @author Evan "Hippy" Slatis
 */
public class GroovyFilter extends StructuredSyntaxDocumentFilter {
    
    // java tab policy action
    private static final Action AUTO_TAB_ACTION = new AutoTabAction();
    
    // Style names
    public static final String COMMENT = "comment";
    public static final String SLASH_STAR_COMMENT = "/\\*(?s:.)*?(?:\\*/|\\z)";
    public static final String SLASH_SLASH_COMMENT =  "//.*";
    public static final String QUOTES =
    	"(?ms:\"{3}(?!\\\"{1,3}).*?(?:\"{3}|\\z))|(?:\"{1}(?!\\\").*?(?:\"|\\Z))";
    public static final String SINGLE_QUOTES =
    	"(?ms:'{3}(?!'{1,3}).*?(?:'{3}|\\z))|(?:'[^'].*?(?:'|\\z))";
    public static final String SLASHY_QUOTES = "/[^/*].*?/";
    public static final String DIGIT = "\\d+?[efld]?";
    
    public static final String IDENT = "[\\w\\$&&[\\D]][\\w\\$]*";
    public static final String OPERATION = "[\\w\\$&&[\\D]][\\w\\$]* *\\(";
    public static final String LEFT_PARENS = "\\(";
    
    private static final Color COMMENT_COLOR = 
        Color.LIGHT_GRAY.darker().darker();
    
    
    public static final String RESERVED_WORD = "reserved";
    public static final String[] RESERVED_WORDS =   {"\\babstract\\b",
                                                    "\\bassert\\b",
                                                    "\\bdefault\\b",
                                                    "\\bif\\b",
                                                    "\\bprivate\\b",
                                                    "\\bthis\\b",
                                                    "\\bboolean\\b",
                                                    "\\bdo\\b",
                                                    "\\bimplements\\b",
                                                    "\\bprotected\\b",
                                                    "\\bthrow\\b",
                                                    "\\bbreak\\b",
                                                    "\\bdouble\\b",
                                                    "\\bimport\\b",
                                                    "\\bpublic\\b",
                                                    "\\bthrows\\b",
                                                    "\\bbyte\\b",
                                                    "\\belse\\b",
                                                    "\\binstanceof\\b",
                                                    "\\breturn\\b",
                                                    "\\btransient\\b",
                                                    "\\bcase\\b",
                                                    "\\bextends\\b",
                                                    "\\bint\\b",
                                                    "\\bshort\\b",
                                                    "\\btry\\b",
                                                    "\\bcatch\\b",
                                                    "\\bfinal\\b",
                                                    "\\binterface\\b",
                                                    "\\benum\\b",
                                                    "\\bstatic\\b",
                                                    "\\bvoid\\b",
                                                    "\\bchar\\b",
                                                    "\\bfinally\\b",
                                                    "\\blong\\b",
                                                    "\\bstrictfp\\b",
                                                    "\\bvolatile\\b",
                                                    "\\bclass\\b",
                                                    "\\bfloat\\b",
                                                    "\\bnative\\b",
                                                    "\\bsuper\\b",
                                                    "\\bwhile\\b",
                                                    "\\bconst\\b",
                                                    "\\bfor\\b",
                                                    "\\bnew\\b",
                                                    "\\bswitch\\b",
                                                    "\\bcontinue\\b",
                                                    "\\bgoto\\b",
                                                    "\\bpackage\\b",
                                                    "\\bdef\\b",
                                                    "\\bas\\b",
                                                    "\\bin\\b",
                                                    "\\bsynchronized\\b",
                                                    "\\bnull\\b"};
    
    /**
     * Creates a new instance of GroovyFilter
     */
    public GroovyFilter(DefaultStyledDocument doc) {
        super(doc);
        init();
    }
    
    private void init() {
        StyleContext styleContext = StyleContext.getDefaultStyleContext();
        Style defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE);
        
        Style comment = styleContext.addStyle(COMMENT, defaultStyle);
        StyleConstants.setForeground(comment, COMMENT_COLOR);
        StyleConstants.setItalic(comment, true);
        
        Style quotes = styleContext.addStyle(QUOTES, defaultStyle);
        StyleConstants.setForeground(quotes, Color.MAGENTA.darker().darker());
        
        Style charQuotes = styleContext.addStyle(SINGLE_QUOTES, defaultStyle);
        StyleConstants.setForeground(charQuotes, Color.GREEN.darker().darker());
        
        Style slashyQuotes = styleContext.addStyle(SLASHY_QUOTES, defaultStyle);
        StyleConstants.setForeground(slashyQuotes, Color.ORANGE.darker());
        
        Style digit = styleContext.addStyle(DIGIT, defaultStyle);
        StyleConstants.setForeground(digit, Color.RED.darker());
        
        Style operation = styleContext.addStyle(OPERATION, defaultStyle);
        StyleConstants.setBold(operation, true);
        
        Style ident = styleContext.addStyle(IDENT, defaultStyle);
        
        Style reservedWords = styleContext.addStyle(RESERVED_WORD, defaultStyle);
        StyleConstants.setBold(reservedWords, true);
        StyleConstants.setForeground(reservedWords, Color.BLUE.darker().darker());
        
        Style leftParens = styleContext.addStyle(IDENT, defaultStyle);
        
        getRootNode().putStyle(SLASH_STAR_COMMENT, comment);
        getRootNode().putStyle(SLASH_SLASH_COMMENT, comment);
        getRootNode().putStyle(QUOTES, quotes);
        getRootNode().putStyle(SINGLE_QUOTES, charQuotes);
        getRootNode().putStyle(SLASHY_QUOTES, slashyQuotes);
        getRootNode().putStyle(DIGIT, digit);
        
        getRootNode().putStyle(OPERATION, operation);
        StructuredSyntaxDocumentFilter.LexerNode node = createLexerNode();
        node.putStyle(RESERVED_WORDS, reservedWords);
        node.putStyle(LEFT_PARENS, leftParens);
        getRootNode().putChild(OPERATION, node);
        
        getRootNode().putStyle(IDENT, ident);
        node = createLexerNode();
        node.putStyle(RESERVED_WORDS, reservedWords);
        getRootNode().putChild(IDENT, node);
    }
    
    public static void installAutoTabAction(JTextComponent tComp) {
        tComp.getActionMap().put("GroovyFilter-autoTab", AUTO_TAB_ACTION);
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        tComp.getInputMap().put(keyStroke, "GroovyFilter-autoTab");
    }
    
    private static class AutoTabAction extends AbstractAction {
        
        private StyledDocument doc;
        private final Segment segment = new Segment();
        private final StringBuffer buffer = new StringBuffer();
        
        public void actionPerformed(ActionEvent ae) {
            JTextComponent tComp = (JTextComponent)ae.getSource();
            if (tComp.getDocument() instanceof StyledDocument) {
                doc = (StyledDocument)tComp.getDocument();
                try {
                    doc.getText(0, doc.getLength(), segment);
                }
                catch (Exception e) {
                    // should NEVER reach here
                    e.printStackTrace();
                }
                int offset = tComp.getCaretPosition();
                int index = findTabLocation(offset);
                buffer.delete(0, buffer.length());
                buffer.append('\n');
                if (index > -1) {
                    for (int i = 0; i < index + 4; i++) {
                        buffer.append(' ');
                    }
                }
                try {
                    doc.insertString(offset, buffer.toString(),
                                 doc.getDefaultRootElement().getAttributes());
                }
                catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
            }
        }
    
        public int findTabLocation(int offset) {  

            // find first {
            boolean cont = true;
            while (offset > -1 && cont) {
                Element el = doc.getCharacterElement(offset);
                Object color =
                    el.getAttributes().getAttribute(StyleConstants.Foreground);
                if (!COMMENT_COLOR.equals(color)) {
                    cont = segment.array[offset] != '{' &&
                           segment.array[offset] != '}';
                }
                offset -= cont ? 1 : 0;
            }

            if (offset > -1 && segment.array[offset] == '{') {
                while (offset > -1 &&
                       !Character.isWhitespace(segment.array[offset--])){
                }
            }

            int index = offset < 0 || segment.array[offset] == '}' ? -4 : 0;
            if (offset > -1) {
                Element top = doc.getDefaultRootElement();
                offset = top.getElement(top.getElementIndex(offset)).getStartOffset();

                while (Character.isWhitespace(segment.array[offset++])) {
                    index++;
                }
            }

            return index;
        }
    }
}
