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
package groovy.console.ui;

import groovy.console.ui.text.GroovyFilter;
import groovy.console.ui.text.MatchingHighlighter;
import groovy.console.ui.text.SmartDocumentFilter;
import groovy.console.ui.text.StructuredSyntaxResources;
import groovy.console.ui.text.TextEditor;
import groovy.console.ui.text.TextUndoManager;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;

/**
 * Component which provides a styled editor for the console.
 */
public class ConsoleTextEditor extends JScrollPane {
    public String getDefaultFamily() {
        return defaultFamily;
    }

    public void setDefaultFamily(String defaultFamily) {
        this.defaultFamily = defaultFamily;
    }

    private class LineNumbersPanel extends JPanel {

        public LineNumbersPanel() {
            int initialSize = 3 * Preferences.userNodeForPackage(Console.class).getInt("fontSize", 12);
            setMinimumSize(new Dimension(initialSize, initialSize));
            setPreferredSize(new Dimension(initialSize, initialSize));
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // starting position in document
            int start = textEditor.viewToModel(getViewport().getViewPosition());
            // end position in document
            int end = textEditor.viewToModel(new Point(10,
                    getViewport().getViewPosition().y +
                            (int) textEditor.getVisibleRect().getHeight())
            );

            // translate offsets to lines
            Document doc = textEditor.getDocument();
            int startline = doc.getDefaultRootElement().getElementIndex(start) + 1;
            int endline = doc.getDefaultRootElement().getElementIndex(end) + 1;
            Font f = textEditor.getFont();
            int fontHeight = g.getFontMetrics(f).getHeight();
            int fontDesc = g.getFontMetrics(f).getDescent();
            int startingY = -1;

            try {
                startingY = textEditor.modelToView(start).y + fontHeight - fontDesc;
            } catch (BadLocationException e1) {
                System.err.println(e1.getMessage());
            }
            g.setFont(f);
            for (int line = startline, y = startingY; line <= endline; y += fontHeight, line++) {
                String lineNumber = StringGroovyMethods.padLeft((CharSequence)Integer.toString(line), 4, " ");
                g.drawString(lineNumber, 0, y);
            }
        }
    }

    private String defaultFamily = "Monospaced";

    private static final PrinterJob PRINTER_JOB = PrinterJob.getPrinterJob();

    private LineNumbersPanel numbersPanel = new LineNumbersPanel();

    private boolean documentChangedSinceLastRepaint = false;

    private TextEditor textEditor = new TextEditor(true, true, true) {

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            // only repaint the line numbers in the gutter when the document has changed
            // in case lines (hence line numbers) have been added or removed from the document
            if (documentChangedSinceLastRepaint) {
                numbersPanel.repaint();
                documentChangedSinceLastRepaint = false;
            }
        }
    };

    private UndoAction undoAction = new UndoAction();
    private RedoAction redoAction = new RedoAction();
    private PrintAction printAction = new PrintAction();

    private boolean editable = true;

    private TextUndoManager undoManager;

    /**
     * Creates a new instance of ConsoleTextEditor
     */
    public ConsoleTextEditor() {
        textEditor.setFont(new Font(defaultFamily, Font.PLAIN,
		        Preferences.userNodeForPackage(Console.class).getInt("fontSize", 12)));

        setViewportView(new JPanel(new BorderLayout()) {{
            add(numbersPanel, BorderLayout.WEST);
            add(textEditor, BorderLayout.CENTER);
        }});

        textEditor.setDragEnabled(editable);

        getVerticalScrollBar().setUnitIncrement(10);

        initActions();

        DefaultStyledDocument doc = new DefaultStyledDocument();
        doc.setDocumentFilter(new GroovyFilter(doc));
        textEditor.setDocument(doc);

        // add a document listener, to hint whether the line number gutter has to be repainted
        // when the number of lines changes
        doc.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent documentEvent) {
                documentChangedSinceLastRepaint = true;
            }

            public void removeUpdate(DocumentEvent documentEvent) {
                documentChangedSinceLastRepaint = true;
            }

            public void changedUpdate(DocumentEvent documentEvent) {
                documentChangedSinceLastRepaint = true;
                int width = 3 * Preferences.userNodeForPackage(Console.class).getInt("fontSize", 12);
                numbersPanel.setPreferredSize(new Dimension(width, width));
            }
        });

        // create and add the undo/redo manager
        this.undoManager = new TextUndoManager();
        doc.addUndoableEditListener(undoManager);

        // add the undo actions
        undoManager.addPropertyChangeListener(undoAction);
        undoManager.addPropertyChangeListener(redoAction);

        doc.addDocumentListener(undoAction);
        doc.addDocumentListener(redoAction);

        InputMap im = textEditor.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK, false);
        im.put(ks, StructuredSyntaxResources.UNDO);
        ActionMap am = textEditor.getActionMap();
        am.put(StructuredSyntaxResources.UNDO, undoAction);

        ks = KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK, false);
        im.put(ks, StructuredSyntaxResources.REDO);
        am.put(StructuredSyntaxResources.REDO, redoAction);

        ks = KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK, false);
        im.put(ks, StructuredSyntaxResources.PRINT);
        am.put(StructuredSyntaxResources.PRINT, printAction);
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
        if (showLineNumbers) {
            setViewportView(new JPanel(new BorderLayout()) {{
                add(numbersPanel, BorderLayout.WEST);
                add(textEditor, BorderLayout.CENTER);
            }});
        } else {
            setViewportView(textEditor);
        }
    }

    public void setEditable(boolean editable) {
        textEditor.setEditable(editable); 
    }

    public boolean clipBoardAvailable() {
        Transferable t = StructuredSyntaxResources.SYSTEM_CLIPBOARD.getContents(this);
        return t.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    public TextEditor getTextEditor() {
        return textEditor;
    }

    protected void initActions() {
        ActionMap map = getActionMap();

        PrintAction printAction = new PrintAction();
        map.put(StructuredSyntaxResources.PRINT, printAction);
    }

    private class PrintAction extends AbstractAction {

        public PrintAction() {
            setEnabled(true);
        }

        public void actionPerformed(ActionEvent ae) {
            PRINTER_JOB.setPageable(textEditor);

            try {
                if (PRINTER_JOB.printDialog()) {
                    PRINTER_JOB.print();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    } // end ConsoleTextEditor.PrintAction

    private class RedoAction extends UpdateCaretListener implements PropertyChangeListener {

        public RedoAction() {
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent ae) {
            undoManager.redo();
            setEnabled(undoManager.canRedo());
            undoAction.setEnabled(undoManager.canUndo());
            super.actionPerformed(ae);
        }

        public void propertyChange(PropertyChangeEvent pce) {
            setEnabled(undoManager.canRedo());
        }
    } // end ConsoleTextEditor.RedoAction

    private abstract class UpdateCaretListener extends AbstractAction implements DocumentListener {

        protected int lastUpdate;

        public void changedUpdate(DocumentEvent de) {
        }

        public void insertUpdate(DocumentEvent de) {
            lastUpdate = de.getOffset() + de.getLength();
        }

        public void removeUpdate(DocumentEvent de) {
            lastUpdate = de.getOffset();
        }

        public void actionPerformed(ActionEvent ae) {
            textEditor.setCaretPosition(lastUpdate);
        }
    }

    private class UndoAction extends UpdateCaretListener implements PropertyChangeListener {

        public UndoAction() {
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent ae) {
            undoManager.undo();
            setEnabled(undoManager.canUndo());
            redoAction.setEnabled(undoManager.canRedo());
            super.actionPerformed(ae);
        }

        public void propertyChange(PropertyChangeEvent pce) {
            setEnabled(undoManager.canUndo());
        }
    }

    public Action getUndoAction() {
        return undoAction;
    }

    public Action getRedoAction() {
        return redoAction;
    }

    public Action getPrintAction() {
        return printAction;
    }

    public void enableHighLighter(Class clazz) {
        DefaultStyledDocument doc = (DefaultStyledDocument) textEditor.getDocument();

        try {
            DocumentFilter documentFilter = (DocumentFilter) clazz.getConstructor(doc.getClass()).newInstance(doc);
            doc.setDocumentFilter(documentFilter);

            disableMatchingHighlighter();
            if (documentFilter instanceof SmartDocumentFilter) {
                final SmartDocumentFilter smartDocumentFilter = (SmartDocumentFilter) documentFilter;
                enableMatchingHighlighter(smartDocumentFilter);
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private void enableMatchingHighlighter(SmartDocumentFilter smartDocumentFilter) {
        textEditor.addCaretListener(new MatchingHighlighter(smartDocumentFilter, textEditor));
    }

    private void disableMatchingHighlighter() {
        for (CaretListener cl : textEditor.getCaretListeners()) {
            if (cl instanceof MatchingHighlighter) {
                textEditor.removeCaretListener(cl);
            }
        }
    }
}
