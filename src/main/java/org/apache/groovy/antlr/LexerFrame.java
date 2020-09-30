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
package org.apache.groovy.antlr;

import org.antlr.v4.runtime.Token;
import org.apache.groovy.parser.antlr4.GroovyLangLexer;
import org.apache.groovy.parser.antlr4.GroovyLexer;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Swing application to graphically display the tokens produced by the lexer.
 */
public class LexerFrame extends JFrame implements ActionListener {
    private static final long serialVersionUID = 2715693043143492893L;
    private final JSplitPane jSplitPane1 = new JSplitPane();
    private final JScrollPane jScrollPane1 = new JScrollPane();
    private final JScrollPane jScrollPane2 = new JScrollPane();
    private final JTextPane tokenPane = new HScrollableTextPane();
    private final JButton jbutton = new JButton("open");
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JTextArea scriptPane = new JTextArea();
    private final Map<Integer, String> tokens = new HashMap<>();
    private static final Class<GroovyLexer> TOKEN_TYPES_CLASS = GroovyLexer.class;

    /**
     * Constructor used when invoking as a standalone application
     *
     */
    public LexerFrame() {
        this(null);
    }

    /**
     * Constructor used when invoking for a specific file
     *
     */
    public LexerFrame(Reader reader) {
        super("Token Stream Viewer");
        try {
            jbInit(reader);
            setSize(500, 500);
            listTokens(TOKEN_TYPES_CLASS);

            if (reader == null) {
                final JPopupMenu popup = new JPopupMenu();
                popup.add(loadFileAction);
                jbutton.setSize(30, 30);
                jbutton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        //if(e.isPopupTrigger())
                        popup.show(scriptPane, e.getX(), e.getY());
                    }
                });
            } else {
                safeScanScript(reader);
            }

            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listTokens(Class tokenTypes) throws Exception {
        for (Field field : tokenTypes.getDeclaredFields()) {
            int modifiers = field.getModifiers();

            if (!(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers))) {
                continue;
            }

            Object fieldValue = field.get(null);
            if (!(fieldValue instanceof Integer)) {
                continue;
            }

            String fieldName = field.getName();
            if (fieldName.endsWith("_MODE")) {
                continue;
            }

            tokens.put((Integer) fieldValue, fieldName);
        }

        tokens.put(-1, "EOF");
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Token token = (Token) ((JComponent) ae.getSource()).getClientProperty("token");
        if (eof().equals(token.getType())) {
            scriptPane.select(0, 0);
            return;
        }
        try {
            int start = scriptPane.getLineStartOffset(token.getLine() - 1) + getColumn(token);
            String text = token.getText();
            scriptPane.select(start, "".equals(text.trim()) ? start : start + text.length());
            scriptPane.requestFocus();
        } catch (BadLocationException ex) {
            // IGNORE
        }
    }

    private final Action loadFileAction = new AbstractAction("Open File...") {
        private static final long serialVersionUID = 4541927184172762704L;

        @Override
        public void actionPerformed(ActionEvent ae) {
            final JFileChooser jfc = new JFileChooser();
            final int response = jfc.showOpenDialog(LexerFrame.this);
            if (response != JFileChooser.APPROVE_OPTION) {
                return;
            }
            safeScanScript(jfc.getSelectedFile());
        }
    };

    private void safeScanScript(File file) {
        try {
            scanScript(new StringReader(ResourceGroovyMethods.getText(file)));
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    private void safeScanScript(Reader reader) {
        try {
            scanScript(reader instanceof StringReader ? (StringReader) reader : new StringReader(IOGroovyMethods.getText(reader)));
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    private void scanScript(final StringReader reader) throws Exception {
        scriptPane.read(reader, null);
        reader.reset();

        // create lexer
        final GroovyLangLexer lexer = new GroovyLangLexer(reader);

        tokenPane.setEditable(true);
        tokenPane.setText("");

        int line = 1;
        final ButtonGroup bg = new ButtonGroup();
        Token token;

        while (true) {
            token = lexer.nextToken();
            JToggleButton tokenButton = new JToggleButton(tokens.get(token.getType()));
            bg.add(tokenButton);
            tokenButton.addActionListener(this);
            tokenButton.setToolTipText(token.getText());
            tokenButton.putClientProperty("token", token);
            tokenButton.setMargin(new Insets(0, 1, 0, 1));
            tokenButton.setFocusPainted(false);
            if (token.getLine() > line) {
                tokenPane.getDocument().insertString(tokenPane.getDocument().getLength(), "\n", null);
                line = token.getLine();
            }
            insertComponent(tokenButton);
            if (eof().equals(token.getType())) {
                break;
            }
        }

        tokenPane.setEditable(false);
        tokenPane.setCaretPosition(0);
        reader.close();
    }

    private Integer eof() {
        return GroovyLangLexer.EOF;
    }

    private Integer getColumn(Token token) {
        return token.getCharPositionInLine();
    }

    private void insertComponent(JComponent comp) {
        try {
            tokenPane.getDocument().insertString(tokenPane.getDocument().getLength(), " ", null);
        } catch (BadLocationException ex1) {
            // Ignore
        }
        try {
            tokenPane.setCaretPosition(tokenPane.getDocument().getLength() - 1);
        } catch (Exception ex) {
            tokenPane.setCaretPosition(0);
        }
        tokenPane.insertComponent(comp);
    }

    private void jbInit(Reader reader) throws Exception {
        final Border border = BorderFactory.createEmptyBorder();
        jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
        tokenPane.setEditable(false);
        tokenPane.setText("");
        scriptPane.setFont(new java.awt.Font("DialogInput", 0, 12));
        scriptPane.setEditable(false);
        scriptPane.setMargin(new Insets(5, 5, 5, 5));
        scriptPane.setText("");
        jScrollPane1.setBorder(border);
        jScrollPane2.setBorder(border);
        jSplitPane1.setMinimumSize(new Dimension(800, 600));
        mainPanel.add(jSplitPane1, BorderLayout.CENTER);
        if (reader == null) {
            mainPanel.add(jbutton, BorderLayout.NORTH);
        }
        this.getContentPane().add(mainPanel);
        jSplitPane1.add(jScrollPane1, JSplitPane.LEFT);
        jScrollPane1.getViewport().add(tokenPane, null);
        jSplitPane1.add(jScrollPane2, JSplitPane.RIGHT);
        jScrollPane2.getViewport().add(scriptPane, null);

        jScrollPane1.setColumnHeaderView(new JLabel(" Token Stream:"));
        jScrollPane2.setColumnHeaderView(new JLabel(" Input Script:"));
        jSplitPane1.setResizeWeight(0.5);
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {
            // Ignore
        }
        LexerFrame lexerFrame = null;

        if (args.length == 0) {
            lexerFrame = new LexerFrame();
        } else if (args.length > 1) {
            System.err.println("usage: java LexerFrame [filename.ext]");
            System.exit(1);
        } else {
            String filename = args[0];
            FileReader fileReader = new FileReader(filename);
            lexerFrame = new LexerFrame(fileReader);
        }
        lexerFrame.setVisible(true);
    }

    private static class HScrollableTextPane extends JTextPane {
        private static final long serialVersionUID = -8582328309470654441L;

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return (getSize().width < getParent().getSize().width);
        }

        @Override
        public void setSize(final Dimension d) {
            if (d.width < getParent().getSize().width) {
                d.width = getParent().getSize().width;
            }
            super.setSize(d);
        }
    }
}
