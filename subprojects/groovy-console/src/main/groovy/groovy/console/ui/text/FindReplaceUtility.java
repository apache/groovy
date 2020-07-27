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

import javax.swing.AbstractAction;
import javax.swing.Action;
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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.EventListener;

public final class FindReplaceUtility {

    public static final String FIND_ACTION_COMMAND = "Find";

    public static final String REPLACE_ACTION_COMMAND = "Replace";

    public static final String REPLACE_ALL_ACTION_COMMAND = "Replace All";

    public static final String CLOSE_ACTION_COMMAND = "Close";

    public static final Action FIND_ACTION = new FindAction();

    private static final JDialog FIND_REPLACE_DIALOG = new JDialog();

    private static final JPanel TEXT_FIELD_PANEL = new JPanel(new GridLayout(2, 1));

    private static final JPanel ENTRY_PANEL = new JPanel();

    private static final JPanel FIND_PANEL = new JPanel();
    private static final JLabel FIND_LABEL = new JLabel("Find What:    ");
    private static final JComboBox FIND_FIELD = new JComboBox();

    private static final JPanel REPLACE_PANEL = new JPanel();
    private static final JLabel REPLACE_LABEL = new JLabel("Replace With:");
    private static final JComboBox REPLACE_FIELD = new JComboBox();

    private static final JPanel BUTTON_PANEL = new JPanel();
    private static final JButton FIND_BUTTON = new JButton();
    private static final JButton REPLACE_BUTTON = new JButton();
    private static final JButton REPLACE_ALL_BUTTON = new JButton();
    private static final JButton CLOSE_BUTTON = new JButton();

    private static final Action CLOSE_ACTION = new CloseAction();
    private static final Action REPLACE_ACTION = new ReplaceAction();

    private static final JPanel CHECK_BOX_PANEL = new JPanel(new GridLayout(3, 1));
    private static final JCheckBox MATCH_CASE_CHECKBOX = new JCheckBox("Match Case      ");
    private static final JCheckBox IS_BACKWARDS_CHECKBOX = new JCheckBox("Search Backwards");
    private static final JCheckBox WRAP_SEARCH_CHECKBOX = new JCheckBox("Wrap Search     ");

    private static JTextComponent textComponent;
    private static AttributeSet attributeSet;

    private static int findReplaceCount;
    private static String lastAction;

    private static final EventListenerList EVENT_LISTENER_LIST = new EventListenerList();

    // the document segment
    private static final Segment SEGMENT = new Segment();

    private static final FocusAdapter TEXT_FOCUS_LISTENER = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent fe) {
            textComponent = (JTextComponent) fe.getSource();
            attributeSet =
                    textComponent.getDocument().getDefaultRootElement().getAttributes();
        }
    };

    static {
        FIND_REPLACE_DIALOG.setResizable(false);
        FIND_REPLACE_DIALOG.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        // is next line needed at all?
        /* KeyStroke keyStroke = */
        KeyStroke.getKeyStroke("enter");
        KeyAdapter keyAdapter = new KeyAdapter() {
            public void keyTyped(KeyEvent ke) {
                if (ke.getKeyChar() == KeyEvent.VK_ENTER) {
                    FIND_BUTTON.doClick();
                }
            }
        };
        FIND_PANEL.setLayout(new FlowLayout(FlowLayout.RIGHT));
        FIND_PANEL.add(FIND_LABEL);
        FIND_PANEL.add(FIND_FIELD);
        FIND_FIELD.addItem("");
        FIND_FIELD.setEditable(true);
        FIND_FIELD.getEditor().getEditorComponent().addKeyListener(keyAdapter);
        Dimension d = FIND_FIELD.getPreferredSize();
        d.width = 225;
        FIND_FIELD.setPreferredSize(d);

        REPLACE_PANEL.add(REPLACE_LABEL);
        REPLACE_PANEL.add(REPLACE_FIELD);
        REPLACE_FIELD.setEditable(true);
        REPLACE_FIELD.getEditor().getEditorComponent().addKeyListener(keyAdapter);
        REPLACE_FIELD.setPreferredSize(d);

        TEXT_FIELD_PANEL.setLayout(new BoxLayout(TEXT_FIELD_PANEL, BoxLayout.Y_AXIS));
        TEXT_FIELD_PANEL.add(FIND_PANEL);
        TEXT_FIELD_PANEL.add(REPLACE_PANEL);

        ENTRY_PANEL.add(TEXT_FIELD_PANEL);
        FIND_REPLACE_DIALOG.getContentPane().add(ENTRY_PANEL, BorderLayout.WEST);

        CHECK_BOX_PANEL.add(MATCH_CASE_CHECKBOX);

        CHECK_BOX_PANEL.add(IS_BACKWARDS_CHECKBOX);

        CHECK_BOX_PANEL.add(WRAP_SEARCH_CHECKBOX);

        ENTRY_PANEL.add(CHECK_BOX_PANEL);
        ENTRY_PANEL.setLayout(new BoxLayout(ENTRY_PANEL, BoxLayout.Y_AXIS));

        REPLACE_ALL_BUTTON.setAction(new ReplaceAllAction());
        REPLACE_ALL_BUTTON.setHorizontalAlignment(JButton.CENTER);
        d = REPLACE_ALL_BUTTON.getPreferredSize();

        BUTTON_PANEL.setLayout(new BoxLayout(BUTTON_PANEL, BoxLayout.Y_AXIS));
        FIND_BUTTON.setAction(FIND_ACTION);
        FIND_BUTTON.setPreferredSize(d);
        FIND_BUTTON.setHorizontalAlignment(JButton.CENTER);
        JPanel panel = new JPanel();
        panel.add(FIND_BUTTON);
        BUTTON_PANEL.add(panel);
        FIND_REPLACE_DIALOG.getRootPane().setDefaultButton(FIND_BUTTON);

        REPLACE_BUTTON.setAction(REPLACE_ACTION);
        REPLACE_BUTTON.setPreferredSize(d);
        REPLACE_BUTTON.setHorizontalAlignment(JButton.CENTER);
        panel = new JPanel();
        panel.add(REPLACE_BUTTON);
        BUTTON_PANEL.add(panel);

        panel = new JPanel();
        panel.add(REPLACE_ALL_BUTTON);
        BUTTON_PANEL.add(panel);

        CLOSE_BUTTON.setAction(CLOSE_ACTION);
        CLOSE_BUTTON.setPreferredSize(d);
        CLOSE_BUTTON.setHorizontalAlignment(JButton.CENTER);
        panel = new JPanel();
        panel.add(CLOSE_BUTTON);
        BUTTON_PANEL.add(panel);
        FIND_REPLACE_DIALOG.getContentPane().add(BUTTON_PANEL);

        KeyStroke stroke = (KeyStroke) CLOSE_ACTION.getValue(Action.ACCELERATOR_KEY);
        JRootPane rPane = FIND_REPLACE_DIALOG.getRootPane();
        rPane.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(stroke, "exit");
        rPane.getActionMap().put("exit", CLOSE_ACTION);
    }

    // Singleton

    private FindReplaceUtility() {
    }

    public static void addTextListener(TextListener tl) {
        EVENT_LISTENER_LIST.add(TextListener.class, tl);
    }

    private static void fireTextEvent() {
        EventListener[] lstrs =
                EVENT_LISTENER_LIST.getListeners(TextListener.class);
        if (lstrs != null && lstrs.length > 0) {
            TextEvent te =
                    new TextEvent(FIND_REPLACE_DIALOG, TextEvent.TEXT_VALUE_CHANGED);
            for (EventListener lstr : lstrs) {
                ((TextListener) lstr).textValueChanged(te);
            }
        }
    }

    /**
     * @return the last action
     */
    public static String getLastAction() {
        return lastAction;
    }

    /**
     * @return the replacement count
     */
    public static int getReplacementCount() {
        return findReplaceCount;
    }

    /**
     * @return the search text
     */
    public static String getSearchText() {
        return (String) FIND_FIELD.getSelectedItem();
    }

    /**
     * @param textComponent the text component to listen to
     */
    public static void registerTextComponent(JTextComponent textComponent) {
        textComponent.addFocusListener(TEXT_FOCUS_LISTENER);
    }

    public static void removeTextListener(TextListener tl) {
        EVENT_LISTENER_LIST.remove(TextListener.class, tl);
    }

    /**
     * Find and select the next searchable matching text.
     *
     * @param reverse look forwards or backwards
     * @param pos     the starting index to start finding from
     * @return the location of the next selected, or -1 if not found
     */
    private static int findNext(boolean reverse, int pos) {
        boolean backwards = IS_BACKWARDS_CHECKBOX.isSelected();
        backwards = backwards ? !reverse : reverse;

        String pattern = (String) FIND_FIELD.getSelectedItem();
        if (pattern != null && pattern.length() > 0) {
            try {
                Document doc = textComponent.getDocument();
                doc.getText(0, doc.getLength(), SEGMENT);
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
            boolean wrapped = WRAP_SEARCH_CHECKBOX.isSelected();
            int end = backwards ? 0 : SEGMENT.getEndIndex();
            pos += backwards ? -1 : 1;

            int length = textComponent.getDocument().getLength();
            if (pos > length) {
                pos = wrapped ? 0 : length;
            }

            boolean found = false;
            while (!found && (backwards ? pos > end : pos < end)) {
                found = !MATCH_CASE_CHECKBOX.isSelected() && SEGMENT.array[pos] == oppFirst;
                found = found ? found : SEGMENT.array[pos] == first;

                if (found) {
                    pos += backwards ? -(pattern.length() - 1) : 0;
                    for (int i = 0; found && i < pattern.length(); i++) {
                        char c = pattern.charAt(i);
                        found = SEGMENT.array[pos + i] == c;
                        if (!MATCH_CASE_CHECKBOX.isSelected() && !found) {
                            c = Character.isUpperCase(c) ?
                                    Character.toLowerCase(c) :
                                    Character.toUpperCase(c);
                            found = SEGMENT.array[pos + i] == c;
                        }
                    }
                }

                if (!found) {
                    pos += backwards ? -1 : 1;

                    if (pos == end && wrapped) {
                        pos = backwards ? SEGMENT.getEndIndex() : 0;
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
        Object findObject = FIND_FIELD.getSelectedItem();
        Object replaceObject = REPLACE_FIELD.isShowing() ?
                (String) REPLACE_FIELD.getSelectedItem() : "";

        if (findObject != null && replaceObject != null) {
            boolean found = false;
            for (int i = 0; !found && i < FIND_FIELD.getItemCount(); i++) {
                found = FIND_FIELD.getItemAt(i).equals(findObject);
            }
            if (!found) {
                FIND_FIELD.insertItemAt(findObject, 0);
                if (FIND_FIELD.getItemCount() > 7) {
                    FIND_FIELD.removeItemAt(7);
                }
            }

            if (REPLACE_FIELD.isShowing()) {
                found = false;
                for (int i = 0; !found && i < REPLACE_FIELD.getItemCount(); i++) {
                    found = REPLACE_FIELD.getItemAt(i).equals(findObject);
                }
                if (!found) {
                    REPLACE_FIELD.insertItemAt(replaceObject, 0);
                    if (REPLACE_FIELD.getItemCount() > 7) {
                        REPLACE_FIELD.removeItemAt(7);
                    }
                }
            }
        }

    }

    public static void showDialog() {
        showDialog(false);
    }

    /**
     * @param isReplace show a replace dialog rather than a find dialog if true
     */
    public static void showDialog(boolean isReplace) {
        String title = isReplace ? REPLACE_ACTION_COMMAND : FIND_ACTION_COMMAND;
        FIND_REPLACE_DIALOG.setTitle(title);

        String text = textComponent.getSelectedText();
        if (text == null) {
            text = "";
        }
        FIND_FIELD.getEditor().setItem(text);
        FIND_FIELD.getEditor().selectAll();

        REPLACE_PANEL.setVisible(isReplace);
        REPLACE_ALL_BUTTON.setVisible(isReplace);
        CLOSE_BUTTON.setVisible(isReplace);

        Action action = isReplace ?
                REPLACE_ACTION : CLOSE_ACTION;
        REPLACE_BUTTON.setAction(action);

        REPLACE_BUTTON.setPreferredSize(null);
        Dimension d = isReplace ?
                REPLACE_ALL_BUTTON.getPreferredSize() :
                REPLACE_BUTTON.getPreferredSize();
        FIND_BUTTON.setPreferredSize(d);
        REPLACE_BUTTON.setPreferredSize(d);
        CLOSE_BUTTON.setPreferredSize(d);

        FIND_REPLACE_DIALOG.invalidate();
        FIND_REPLACE_DIALOG.repaint();
        FIND_REPLACE_DIALOG.pack();

        java.awt.Frame[] frames = java.awt.Frame.getFrames();
        for (Frame frame : frames) {
            if (frame.isFocused()) {
                FIND_REPLACE_DIALOG.setLocationRelativeTo(frame);
            }
        }

        FIND_REPLACE_DIALOG.setVisible(true);
        FIND_FIELD.requestFocusInWindow();
    }

    /**
     * @param textComponent the text component to stop listening to
     */
    public static void unregisterTextComponent(JTextComponent textComponent) {
        textComponent.removeFocusListener(TEXT_FOCUS_LISTENER);
    }

    private static class FindAction extends AbstractAction {

        public FindAction() {
            putValue(Action.NAME, FIND_ACTION_COMMAND);
            putValue(Action.ACTION_COMMAND_KEY, FIND_ACTION_COMMAND);
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
        }

        public void actionPerformed(ActionEvent ae) {
            lastAction = FIND_ACTION_COMMAND;
            findReplaceCount = 0;

            if (FIND_REPLACE_DIALOG.isVisible() &&
                    FIND_REPLACE_DIALOG.getTitle().equals(FIND_ACTION_COMMAND)) {
            }

            int pos = textComponent.getSelectedText() == null ?
                    textComponent.getCaretPosition() :
                    textComponent.getSelectionStart();

            boolean reverse = (ae.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
            pos = findNext(reverse, pos);

            if (pos > -1) {
                String pattern = (String) FIND_FIELD.getSelectedItem();
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
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
        }

        public void actionPerformed(ActionEvent ae) {
            lastAction = ae.getActionCommand();
            findReplaceCount = 0;

            int pos = textComponent.getSelectedText() == null ?
                    textComponent.getCaretPosition() :
                    textComponent.getSelectionStart();

            pos = findNext(false, pos - 1);

            if (pos > -1) {
                String find = (String) FIND_FIELD.getSelectedItem();
                String replace = (String) REPLACE_FIELD.getSelectedItem();
                replace = replace == null ? "" : replace;
                Document doc = textComponent.getDocument();
                try {
                    doc.remove(pos, find.length());
                    doc.insertString(pos, replace, attributeSet);

                    int last = pos;
                    pos = findNext(false, pos);
                    if (pos > -1) {
                        textComponent.select(pos, pos + find.length());
                    } else {
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
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
        }

        public void actionPerformed(ActionEvent ae) {
            lastAction = ae.getActionCommand();
            findReplaceCount = 0;

            int last = textComponent.getSelectedText() == null ?
                    textComponent.getCaretPosition() :
                    textComponent.getSelectionStart();

            int pos = findNext(false, last - 1);

            String find = (String) FIND_FIELD.getSelectedItem();
            String replace = (String) REPLACE_FIELD.getSelectedItem();
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
            } else {
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
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ESCAPE"));
        }

        public void actionPerformed(ActionEvent ae) {
            FIND_REPLACE_DIALOG.dispose();
        }
    }

    public static void dispose() {
        FIND_REPLACE_DIALOG.dispose();
    }
}
