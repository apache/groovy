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
package org.codehaus.groovy.antlr;

import antlr.CharScanner;
import antlr.Token;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Hashtable;

/**
 * Swing application to graphically display the tokens produced by the lexer.
 */
public class LexerFrame extends JFrame implements ActionListener {
    private final JSplitPane jSplitPane1 = new JSplitPane();
    private final JScrollPane jScrollPane1 = new JScrollPane();
    private final JScrollPane jScrollPane2 = new JScrollPane();
    private final JTextPane tokenPane = new HScrollableTextPane();
    private final JButton jbutton = new JButton("open");
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JTextArea scriptPane = new JTextArea();
    private final Class lexerClass;
    private final Hashtable tokens = new Hashtable();

    public LexerFrame(Class lexerClass, Class tokenTypesClass) {
        super("Token Steam Viewer");
        this.lexerClass = lexerClass;
        try {
            jbInit();
            setSize(500, 500);
            listTokens(tokenTypesClass);

            final JPopupMenu popup = new JPopupMenu();
            popup.add(loadFileAction);

            jbutton.setSize(30, 30);
            jbutton.addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e) {
                    //if(e.isPopupTrigger())
                    popup.show(scriptPane, e.getX(), e.getY());
                }
            });
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listTokens(Class tokenTypes) throws Exception {
        for (Field field : tokenTypes.getDeclaredFields()) {
            tokens.put(field.get(null), field.getName());
        }
    }

    public void actionPerformed(ActionEvent ae) {
        Token token = (Token) ((JComponent) ae.getSource()).getClientProperty("token");
        if (token.getType() == Token.EOF_TYPE) {
            scriptPane.select(0, 0);
            return;
        }
        try {
            int start = scriptPane.getLineStartOffset(token.getLine() - 1) + token.getColumn() - 1;
            scriptPane.select(start, start + token.getText().length());
            scriptPane.requestFocus();
        } catch (BadLocationException ex) {
            // IGNORE
        }
    }

    private Action loadFileAction = new AbstractAction("Open File...") {
        public void actionPerformed(ActionEvent ae) {
            final JFileChooser jfc = new JFileChooser();
            final int response = jfc.showOpenDialog(LexerFrame.this);
            if (response != JFileChooser.APPROVE_OPTION) {
                return;
            }
            try {
                scanScript(jfc.getSelectedFile());
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    private void scanScript(final File file) throws Exception {
        scriptPane.read(new FileReader(file), null);

        // create lexer
        final Constructor constructor = lexerClass.getConstructor(InputStream.class);
        final FileInputStream fileInputStream = new FileInputStream(file);
        final CharScanner lexer = (CharScanner) constructor.newInstance(fileInputStream);

        tokenPane.setEditable(true);
        tokenPane.setText("");

        int line = 1;
        final ButtonGroup bg = new ButtonGroup();
        Token token;

        while (true) {
            token = lexer.nextToken();
            JToggleButton tokenButton = new JToggleButton((String) tokens.get(Integer.valueOf(token.getType())));
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
            if (token.getType() == Token.EOF_TYPE){
                break;
            }
        }

        tokenPane.setEditable(false);
        tokenPane.setCaretPosition(0);
        fileInputStream.close();
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

    private void jbInit() throws Exception {
        final Border border1 = BorderFactory.createEmptyBorder();
        jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
        tokenPane.setEditable(false);
        tokenPane.setText("");
        scriptPane.setFont(new java.awt.Font("DialogInput", 0, 12));
        scriptPane.setEditable(false);
        scriptPane.setMargin(new Insets(5, 5, 5, 5));
        scriptPane.setText("");
        jScrollPane1.setBorder(border1);
        jScrollPane2.setBorder(border1);
        jSplitPane1.setMinimumSize(new Dimension(800, 600));
        mainPanel.add(jSplitPane1, BorderLayout.CENTER);
        mainPanel.add(jbutton, BorderLayout.NORTH);
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
        new LexerFrame(GroovyLexer.class, GroovyTokenTypes.class).setVisible(true);
    }

    private static class HScrollableTextPane extends JTextPane {
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
