/*
 * FindReplaceUtility.java
 *
 * Copyright (c) 2004, 2007 Evan A Slatis
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import java.util.EventListener;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import javax.swing.event.EventListenerList;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;

/**
 *
 * @author  hippy
 */
public class FindReplaceUtility {
    
    public static final String FIND_ACTION_COMMAND = "Find";
    
    public static final String REPLACE_ACTION_COMMAND = "Replace";
    
    public static final String REPLACE_ALL_ACTION_COMMAND = "Replace All";
    
    public static final String CLOSE_ACTION_COMMAND = "Close";
    
    public static final Action FIND_ACTION = new FindAction();
    
    private static JDialog findReplaceDialog = new JDialog();
        
    private static JPanel textFieldPanel = new JPanel(new GridLayout(2, 1));
        
    private static JPanel entryPanel = new JPanel();
    
    private static JPanel findPanel = new JPanel();
    private static JLabel findLabel = new JLabel("Find What:    ");
    private static JComboBox findField = new JComboBox();
    
    private static JPanel replacePanel = new JPanel();
    private static JLabel replaceLabel = new JLabel("Replace With:");
    private static JComboBox replaceField = new JComboBox();

    private static JPanel buttonPanel = new JPanel();
    private static JButton findButton = new JButton();
    private static JButton replaceButton = new JButton();
    private static JButton replaceAllButton = new JButton();
    private static JButton closeButton = new JButton();
    
    private static Action closeAction = new CloseAction();
    private static Action replaceAction = new ReplaceAction();

    private static JPanel checkBoxPanel = new JPanel(new GridLayout(3, 1));
    private static JCheckBox matchCaseCBox = new JCheckBox("Match Case      ");
    private static JCheckBox isBackwardsCBox = new JCheckBox("Search Backwards");
    private static JCheckBox wrapSearchCBox = new JCheckBox("Wrap Search     ");
    
    private static JTextComponent textComponent;
    private static AttributeSet attributeSet;
    
    private static int findReplaceCount;
    private static String lastAction;
    
    private static EventListenerList eventListenerList = new EventListenerList();
    
    // the document segment
    private static Segment segment = new Segment();
 
    private static FocusAdapter textFocusListener = new FocusAdapter() {
        public void focusGained(FocusEvent fe) {
            textComponent = (JTextComponent)fe.getSource();
            attributeSet =
                textComponent.getDocument().getDefaultRootElement().getAttributes();
        }
    };

    static {
        findReplaceDialog.setResizable(false);
        findReplaceDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        KeyStroke keyStroke = KeyStroke.getKeyStroke("enter");
        KeyAdapter keyAdapter = new KeyAdapter() {            
            public void keyTyped(KeyEvent ke) {
                if (ke.getKeyChar() == KeyEvent.VK_ENTER) {
                    findButton.doClick();
                }
            }
        };
        findPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        findPanel.add(findLabel);
        findPanel.add(findField);
        findField.addItem("");
        findField.setEditable(true);
        findField.getEditor().getEditorComponent().addKeyListener(keyAdapter);
        Dimension d = findField.getPreferredSize();
        d.width = 225;
        findField.setPreferredSize(d);
        
        replacePanel.add(replaceLabel);
        replacePanel.add(replaceField);
        replaceField.setEditable(true);
        replaceField.getEditor().getEditorComponent().addKeyListener(keyAdapter);
        replaceField.setPreferredSize(d);
        
        textFieldPanel.setLayout(new BoxLayout(textFieldPanel, BoxLayout.Y_AXIS));
        textFieldPanel.add(findPanel);
        textFieldPanel.add(replacePanel);
        
        entryPanel.add(textFieldPanel);
        findReplaceDialog.getContentPane().add(entryPanel, BorderLayout.WEST);

        checkBoxPanel.add(matchCaseCBox);
        
        checkBoxPanel.add(isBackwardsCBox);
        
        checkBoxPanel.add(wrapSearchCBox);
                
        entryPanel.add(checkBoxPanel);
        entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.Y_AXIS));

        replaceAllButton.setAction(new ReplaceAllAction());
        replaceAllButton.setHorizontalAlignment(JButton.CENTER);
        d = replaceAllButton.getPreferredSize();
        
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        findButton.setAction(FIND_ACTION);
        findButton.setPreferredSize(d);
        findButton.setHorizontalAlignment(JButton.CENTER);
        JPanel panel = new JPanel();
        panel.add(findButton);
        buttonPanel.add(panel);
        findReplaceDialog.getRootPane().setDefaultButton(findButton);
        
        replaceButton.setAction(replaceAction);
        replaceButton.setPreferredSize(d);
        replaceButton.setHorizontalAlignment(JButton.CENTER);
        panel = new JPanel();
        panel.add(replaceButton);
        buttonPanel.add(panel);
        
        panel = new JPanel();
        panel.add(replaceAllButton);
        buttonPanel.add(panel);
        
        closeButton.setAction(closeAction);
        closeButton.setPreferredSize(d);
        closeButton.setHorizontalAlignment(JButton.CENTER);
        panel = new JPanel();
        panel.add(closeButton);
        buttonPanel.add(panel);
        findReplaceDialog.getContentPane().add(buttonPanel);
        
        KeyStroke stroke = (KeyStroke)closeAction.getValue(Action.ACCELERATOR_KEY);
        JRootPane rPane = findReplaceDialog.getRootPane();
        rPane.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(stroke, "exit");
        rPane.getActionMap().put("exit", closeAction);
    }

    // Singleton
    private FindReplaceUtility() {
    }
    
    public static void addTextListener(TextListener tl) {
        eventListenerList.add(TextListener.class, tl);
    }
    
    private static void fireTextEvent() {
        EventListener[] lstrs =
            eventListenerList.getListeners(TextListener.class);
        if (lstrs != null && lstrs.length > 0) {
            TextEvent te =
                new TextEvent(findReplaceDialog, TextEvent.TEXT_VALUE_CHANGED);
            for (int i = 0; i < lstrs.length; i++) {
                ((TextListener)lstrs[i]).textValueChanged(te);
            }
        }
    }
    
    /**
     * @return
     */    
    public static String getLastAction() {
        return lastAction;
    }
    
    /**
     * @return
     */    
    public static int getReplacementCount() {
        return findReplaceCount;
    }
    
    /**
     * @return
     */    
    public static String getSearchText() {
        return (String)findField.getSelectedItem();
    }
    
    /**
     * @param te
     */    
    public static void registerTextComponent(JTextComponent te) {
        te.addFocusListener(textFocusListener);
    }
    
    public static void removeTextListener(TextListener tl) {
        eventListenerList.remove(TextListener.class, tl);
    }
    
    /**
     * Find and select the next searchable matching text.
     *
     * @param reverse look forwards or backwards
     * @return the location of the next selected, or -1 if not found
     */        
    private static int findNext(boolean reverse, int pos) {
        boolean backwards = isBackwardsCBox.isSelected();
        backwards = backwards ? !reverse : reverse;
        
        String pattern = (String)findField.getSelectedItem();
        if (pattern != null && pattern.length() > 0) {
            try {
                Document doc = textComponent.getDocument();
                doc.getText(0, doc.getLength(), segment);
            }
            catch (Exception e) {
                // should NEVER reach here
                e.printStackTrace();
            }

            pos += textComponent.getSelectedText() == null ? 
                (backwards ? -1 : 1) : 0;

            char first = backwards ?
                pattern.charAt(pattern.length() - 1) : pattern.charAt(0);
            char oppFirst = Character.isUpperCase(first) ? 
                Character.toLowerCase(first) : Character.toUpperCase(first);
            int start = pos;
            boolean wrapped = wrapSearchCBox.isSelected();
            int end = backwards ? 0 : segment.getEndIndex();
            pos += backwards ? -1 : 1;
            
        	int length = textComponent.getDocument().getLength();
        	if (pos > length) {
        		pos = wrapped ? 0 : length;
        	}
            
            boolean found = false;
            while (!found && (backwards ? pos > end : pos < end)) {
                found = !matchCaseCBox.isSelected() ?
                    segment.array[pos] == oppFirst : false;
                found = found ? found : segment.array[pos] == first;
                
                if (found) {
                    pos += backwards ? -(pattern.length() - 1) : 0;
                    for (int i = 0; found && i < pattern.length(); i++) {
                        char c = pattern.charAt(i);
                        found =  segment.array[pos + i] == c;
                        if (!matchCaseCBox.isSelected() && !found) {
                            c = Character.isUpperCase(c) ? 
                                Character.toLowerCase(c) :
                                Character.toUpperCase(c);
                            found =  segment.array[pos + i] == c;
                        }
                    }
                }
                
                if (!found) {
                    pos += backwards ? -1 : 1;

                    if (pos == end && wrapped) {
                        pos = backwards ? segment.getEndIndex() : 0;
                        end = start;
                        wrapped = false;
                    }
                }
            }
            pos = found ? pos : -1;
        }
        
        return pos;
    }
    
    private static void setListStrings() {
        Object findObject = (String)findField.getSelectedItem();
        Object replaceObject = replaceField.isShowing() ? 
            (String)replaceField.getSelectedItem() : "";
            
        if (findObject != null && replaceObject != null) {
            boolean found = false;
            for (int i = 0; !found && i < findField.getItemCount(); i++) {
                found = findField.getItemAt(i).equals(findObject);
            }
            if (!found) {
                findField.insertItemAt(findObject, 0);
                if (findField.getItemCount() > 7) {
                    findField.removeItemAt(7);
                }
            }
            
            if (replaceField.isShowing()) {
                found = false;
                for (int i = 0; !found && i < replaceField.getItemCount(); i++) {
                    found = replaceField.getItemAt(i).equals(findObject);
                }
                if (!found) {
                    replaceField.insertItemAt(replaceObject, 0);
                    if (replaceField.getItemCount() > 7) {
                        replaceField.removeItemAt(7);
                    }
                }
            }
        }

    }
    
    public static void showDialog() {
        showDialog(false);
    }
    
    /**
     * @param isReplace
     */    
    public static void showDialog(boolean isReplace) {
        String title = isReplace ? REPLACE_ACTION_COMMAND : FIND_ACTION_COMMAND;
        findReplaceDialog.setTitle(title);
        
        String text = textComponent.getSelectedText();
        if (text == null) {
        	text = "";
        }
        findField.getEditor().setItem(text);
        findField.getEditor().selectAll();
                
        replacePanel.setVisible(isReplace);
        replaceAllButton.setVisible(isReplace);
        closeButton.setVisible(isReplace);

        Action action = isReplace ? 
            replaceAction : closeAction;
        replaceButton.setAction(action);

        replaceButton.setPreferredSize(null);
        Dimension d = isReplace ? 
            replaceAllButton.getPreferredSize() :
            replaceButton.getPreferredSize();
        findButton.setPreferredSize(d);
        replaceButton.setPreferredSize(d);
        closeButton.setPreferredSize(d);

        findReplaceDialog.invalidate();
        findReplaceDialog.repaint();
        findReplaceDialog.pack();
        
        java.awt.Frame[] frames = java.awt.Frame.getFrames();
        for (int i = 0; i < frames.length; i++) {
            if (frames[i].isFocused()) {
                findReplaceDialog.setLocationRelativeTo(frames[i]);
            }
        }
        
        findReplaceDialog.setVisible(true);
        findField.requestFocusInWindow();
    }
    
    /**
     * @param te
     */    
    public static void unregisterTextComponent(JTextComponent te) {
        te.removeFocusListener(textFocusListener);
    }
    
    private static class FindAction extends AbstractAction {
        
        public FindAction() {
            putValue(Action.NAME, FIND_ACTION_COMMAND);
            putValue(Action.ACTION_COMMAND_KEY, FIND_ACTION_COMMAND);
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_F));
        }
        
        public void actionPerformed(ActionEvent ae) {
            lastAction = FIND_ACTION_COMMAND;
            findReplaceCount = 0;
            
            if (findReplaceDialog.isVisible() &&
                findReplaceDialog.getTitle().equals(FIND_ACTION_COMMAND)) {
            }

            int pos = textComponent.getSelectedText() == null ? 
                textComponent.getCaretPosition() : 
                textComponent.getSelectionStart();
            
            boolean reverse = (ae.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
            pos = findNext(reverse, pos);
            
            if (pos > -1) {
                String pattern = (String)findField.getSelectedItem();
                textComponent.select(pos, pos + pattern.length());
                findReplaceCount = 1;
            }
            
            setListStrings();
            
            fireTextEvent();
        }
    }
    
    private static class ReplaceAction extends AbstractAction {
        
        public ReplaceAction() {
            putValue(Action.NAME, REPLACE_ACTION_COMMAND);
            putValue(Action.ACTION_COMMAND_KEY, REPLACE_ACTION_COMMAND);
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
        }

        public void actionPerformed(ActionEvent ae) {
            lastAction = ae.getActionCommand();
            findReplaceCount = 0;
            
            int pos = textComponent.getSelectedText() == null ? 
                textComponent.getCaretPosition() : 
                textComponent.getSelectionStart();

            pos = findNext(false, pos - 1);

            if (pos > -1) {
                String find = (String)findField.getSelectedItem();
                String replace = (String)replaceField.getSelectedItem();
                replace = replace == null ? "" : replace;
                Document doc = textComponent.getDocument();
                try {
                    doc.remove(pos, find.length());
                    doc.insertString(pos, replace, attributeSet);

                    int last = pos;
                    pos = findNext(false, pos);
                    if (pos > -1) {
                        textComponent.select(pos, pos + find.length());
                    }
                    else {
                        textComponent.setCaretPosition(last + replace.length());
                    }
                }
                catch (BadLocationException ble) {
                    ble.printStackTrace();
                }

                findReplaceCount = 1;
            }
            setListStrings();
            
            fireTextEvent();
        }
    }
    
    private static class ReplaceAllAction extends AbstractAction {
        
        public ReplaceAllAction() {
            putValue(Action.NAME, REPLACE_ALL_ACTION_COMMAND);
            putValue(Action.ACTION_COMMAND_KEY, REPLACE_ALL_ACTION_COMMAND);
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        }

        public void actionPerformed(ActionEvent ae) {
            lastAction = ae.getActionCommand();
            findReplaceCount = 0;
            
            int last = textComponent.getSelectedText() == null ? 
                textComponent.getCaretPosition() : 
                textComponent.getSelectionStart();

            int pos = findNext(false, last - 1);

            String find = (String)findField.getSelectedItem();
            String replace = (String)replaceField.getSelectedItem();
            replace = replace == null ? "" : replace;
            while (pos > -1) {
                Document doc = textComponent.getDocument();
                try {
                    doc.remove(pos, find.length());
                    doc.insertString(pos, replace, attributeSet);

                    last = pos;
                    pos = findNext(false, pos);
                }
                catch (BadLocationException ble) {
                    ble.printStackTrace();
                }

                findReplaceCount++;
            }
            
            if (pos > -1) {
                textComponent.select(pos, pos + find.length());
            }
            else {
                textComponent.setCaretPosition(last + replace.length());
            }
            setListStrings();
            
            fireTextEvent();
        }
    }
    
    private static class CloseAction extends AbstractAction {
        
        public CloseAction() {
            putValue(Action.NAME, CLOSE_ACTION_COMMAND);
            putValue(Action.ACTION_COMMAND_KEY, CLOSE_ACTION_COMMAND);
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ESCAPE"));
        }

        public void actionPerformed(ActionEvent ae) {
            findReplaceDialog.dispose();
        }
    }
}
