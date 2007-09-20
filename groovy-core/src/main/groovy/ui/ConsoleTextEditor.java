/*
 * UIResourceMgr.java
 *
 * Copyright 2004, 2007 the original author or authors.
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

package groovy.ui;

import groovy.ui.text.GroovyFilter;
import groovy.ui.text.StructuredSyntaxResources;
import groovy.ui.text.TextEditor;
import groovy.ui.text.TextUndoManager;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Component which provides a styled editor for the console.
 *
 * @version $Id$
 * @author hippy
 */
public class ConsoleTextEditor extends JScrollPane {

    private static final PrinterJob PRINTER_JOB = PrinterJob.getPrinterJob();
        
    private TextEditor textEditor = new TextEditor(true, true, true);
    
    private UndoAction undoAction = new UndoAction();
    private RedoAction redoAction = new RedoAction();
    private PrintAction printAction = new PrintAction();
    
    private boolean editable = true;
    
    private File textFile;
    
    private TextUndoManager undoManager;

    /**
     * Creates a new instance of ConsoleTextEditor
     */
    public ConsoleTextEditor() {        
        textEditor.setFont(StructuredSyntaxResources.EDITOR_FONT);
        
        setWheelScrollingEnabled(true);

        setViewportView(textEditor);
        
        textEditor.setDragEnabled(editable);
        
        initActions();
        
        DefaultStyledDocument doc = new DefaultStyledDocument();
        doc.setDocumentFilter(new GroovyFilter(doc));
        textEditor.setDocument(doc);

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
    
    private class UndoAction extends UpdateCaretListener  implements PropertyChangeListener {

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

}
