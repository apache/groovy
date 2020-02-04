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

import groovy.lang.Tuple;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * A simple text pane that is printable and wrapping is optional.
 */
public class TextEditor extends JTextPane implements Pageable, Printable {

    public static final String FIND = "Find...";
    public static final String FIND_NEXT = "Find Next";
    public static final String FIND_PREVIOUS = "Find Previous";
    public static final String REPLACE = "Replace...";
    public static final String AUTO_INDENT = "AutoIndent";

    private static final String TABBED_SPACES = "    ";
    private static final Pattern TAB_BACK_PATTERN =
            Pattern.compile("^(([\t])|(    )|(   )|(  )|( ))", Pattern.MULTILINE);
    private static final Pattern LINE_START =
            Pattern.compile("^", Pattern.MULTILINE);

    private static final JTextPane PRINT_PANE = new JTextPane();
    private static final Dimension PRINT_SIZE = new Dimension();
    private static boolean isOvertypeMode;

    private Caret defaultCaret;
    private Caret overtypeCaret;

    private static final PageFormat PAGE_FORMAT;

    static {
        PrinterJob job = PrinterJob.getPrinterJob();
        PAGE_FORMAT = job.defaultPage();
    }

    private int numPages;

    private MouseAdapter mouseAdapter =
            new MouseAdapter() {
                Cursor cursor;

                public void mouseEntered(MouseEvent me) {
                    if (contains(me.getPoint())) {
                        cursor = getCursor();
                        Cursor curs = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
                        getRootPane().getLayeredPane().setCursor(curs);
                    } else {
                        getRootPane().getLayeredPane().setCursor(cursor);
                    }
                }

                public void mouseExited(MouseEvent me) {
                    getRootPane().getLayeredPane().setCursor(null);
                }
            };

    private boolean unwrapped;
    private boolean tabsAsSpaces;
    private boolean multiLineTab;

    /**
     * Creates a new instance of TextEditor
     */
    public TextEditor() {
        this(false);
    }

    /**
     * Creates a new instance of TextEditor
     */
    public TextEditor(boolean tabsAsSpaces) {
        this(tabsAsSpaces, false);
    }

    /**
     * Creates a new instance of TextEditor
     */
    public TextEditor(boolean tabsAsSpaces, boolean multiLineTab) {
        this(multiLineTab, tabsAsSpaces, false);
    }

    /**
     * Creates a new instance of TextEditor
     */
    public TextEditor(boolean tabsAsSpaces, boolean multiLineTab, boolean unwrapped) {
        this.tabsAsSpaces = tabsAsSpaces;
        this.multiLineTab = multiLineTab;
        this.unwrapped = unwrapped;

        // remove and replace the delete action to another spot so ctrl H later
        // on is strictly for showing the find & replace dialog
        ActionMap aMap = getActionMap();
        Action action = null;
        do {
            action = action == null ? aMap.get(DefaultEditorKit.deletePrevCharAction) : null;
            aMap.remove(DefaultEditorKit.deletePrevCharAction);
            aMap = aMap.getParent();
        } while (aMap != null);
        aMap = getActionMap();
        InputMap iMap = getInputMap();
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0, false);
        iMap.put(keyStroke, "delete");
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, KeyEvent.SHIFT_MASK, false);
        iMap.put(keyStroke, "delete");
        aMap.put("delete", action);

        // set all the actions
        action = new FindAction();
        aMap.put(FIND, action);
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK, false);
        iMap.put(keyStroke, FIND);

        aMap.put(FIND_NEXT, FindReplaceUtility.FIND_ACTION);
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0, false);
        iMap.put(keyStroke, FIND_NEXT);

        aMap.put(FIND_PREVIOUS, FindReplaceUtility.FIND_ACTION);
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK, false);
        iMap.put(keyStroke, FIND_PREVIOUS);

        action = new TabAction();
        aMap.put("TextEditor-tabAction", action);
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0, false);
        iMap.put(keyStroke, "TextEditor-tabAction");

        action = new ShiftTabAction();
        aMap.put("TextEditor-shiftTabAction", action);
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK, false);
        iMap.put(keyStroke, "TextEditor-shiftTabAction");

        action = new ReplaceAction();
        getActionMap().put(REPLACE, action);
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK, false);
        do {
            iMap.remove(keyStroke);
            iMap = iMap.getParent();
        } while (iMap != null);
        getInputMap().put(keyStroke, REPLACE);

        action = new AutoIndentAction();
        getActionMap().put(AUTO_INDENT, action);
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        getInputMap().put(keyStroke, AUTO_INDENT);

        setAutoscrolls(true);

        defaultCaret = getCaret();
        overtypeCaret = new OvertypeCaret();
        overtypeCaret.setBlinkRate(defaultCaret.getBlinkRate());
    }

    public void addNotify() {
        super.addNotify();
        addMouseListener(mouseAdapter);
        FindReplaceUtility.registerTextComponent(this);
    }

    public int getNumberOfPages() {
        Paper paper = PAGE_FORMAT.getPaper();
        numPages = (int) Math.ceil(getSize().getHeight() / paper.getImageableHeight());
        return numPages;
    }

    public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
        return PAGE_FORMAT;
    }

    public Printable getPrintable(int param) throws IndexOutOfBoundsException {
        return this;
    }

    public int print(Graphics graphics, PageFormat pageFormat, int page)
            throws PrinterException {
        if (page < numPages) {
            Paper paper = pageFormat.getPaper();

            // initialize the PRINT_PANE (need this so that wrapping
            // can take place)
            PRINT_PANE.setDocument(getDocument());

            PRINT_PANE.setFont(getFont());
            PRINT_SIZE.setSize(paper.getImageableWidth(),
                    getSize().getHeight());
            PRINT_PANE.setSize(PRINT_SIZE);

            // translate the graphics origin upwards so the area of the page we
            // want to print is in the origin; the clipping region auto set
            // will take care of the rest
            double y = -(page * paper.getImageableHeight()) + paper.getImageableY();

            ((Graphics2D) graphics).translate(paper.getImageableX(), y);

            // print the text with its own routines
            PRINT_PANE.print(graphics);

            // translate the graphics object back to reality in the y dimension
            // so we can print a page number
            ((Graphics2D) graphics).translate(0, -y);
            Rectangle rect = graphics.getClipBounds();
            graphics.setClip(rect.x, 0, rect.width, (int) paper.getHeight() + 100);

            // get the name of the pane (or user name) and the time for the header
            Calendar cal = Calendar.getInstance();
            String header = cal.getTime().toString().trim();
            String name = getName() == null ?
                    System.getProperty("user.name").trim() : getName().trim();
            String pageStr = String.valueOf(page + 1);

            Font font = Font.decode("Monospaced 8");
            graphics.setFont(font);
            FontMetrics fm = graphics.getFontMetrics(font);
            int width = SwingUtilities.computeStringWidth(fm, header);
            ((Graphics2D) graphics).drawString(header,
                    (float) (paper.getImageableWidth() / 2 - width / 2),
                    (float) paper.getImageableY() / 2 + fm.getHeight());

            ((Graphics2D) graphics).translate(0, paper.getImageableY() - fm.getHeight());
            double height = paper.getImageableHeight() + paper.getImageableY() / 2;
            width = SwingUtilities.computeStringWidth(fm, name);
            ((Graphics2D) graphics).drawString(name,
                    (float) (paper.getImageableWidth() / 2 - width / 2),
                    (float) height - fm.getHeight() / 2);

            ((Graphics2D) graphics).translate(0, fm.getHeight());
            width = SwingUtilities.computeStringWidth(fm, pageStr);
            ((Graphics2D) graphics).drawString(pageStr,
                    (float) (paper.getImageableWidth() / 2 - width / 2),
                    (float) height - fm.getHeight() / 2);

            return Printable.PAGE_EXISTS;
        }
        return Printable.NO_SUCH_PAGE;
    }

    public boolean getScrollableTracksViewportWidth() {
        boolean bool = super.getScrollableTracksViewportWidth();
        if (unwrapped) {
            Component parent = this.getParent();
            ComponentUI ui = this.getUI();
            int uiWidth = ui.getPreferredSize(this).width;
            bool = (parent == null) || (uiWidth < parent.getSize().width);
        }
        return bool;
    }

    /**
     * Whether using the tab key indents the selected lines of code
     *
     * @return true if multiline tabbing is active
     */
    public boolean isMultiLineTabbed() {
        return multiLineTab;
    }

    /**
     * @return true if overtype mode is active, false for insert mode
     */
    public static boolean isOvertypeMode() {
        return isOvertypeMode;
    }

    /**
     * @return true if tabs are converted to spaces upon typing
     */
    public boolean isTabsAsSpaces() {
        return tabsAsSpaces;
    }

    /**
     * @return true if text wrapping is disabled
     */
    public boolean isUnwrapped() {
        return unwrapped;
    }

    protected void processKeyEvent(KeyEvent e) {
        super.processKeyEvent(e);

        //  Handle release of Insert key to toggle overtype/insert mode
        //  unless a modifier is active (eg Shift+Insert for paste or
        //  Ctrl+Insert for Copy)
        if (e.getID() == KeyEvent.KEY_RELEASED &&
                e.getKeyCode() == KeyEvent.VK_INSERT &&
                e.getModifiersEx() == 0) {
            setOvertypeMode(!isOvertypeMode());
        }
    }

    public void removeNotify() {
        super.removeNotify();
        removeMouseListener(mouseAdapter);
        FindReplaceUtility.unregisterTextComponent(this);
    }

    public void replaceSelection(String text) {
        //  Implement overtype mode by selecting the character at the current
        //  caret position
        if (isOvertypeMode()) {
            int pos = getCaretPosition();
            if (getSelectedText() == null && pos < getDocument().getLength()) {
                moveCaretPosition(pos + 1);
            }
        }
        super.replaceSelection(text);
    }

    public void setBounds(int x, int y, int width, int height) {
        if (unwrapped) {
            Dimension size = this.getPreferredSize();
            super.setBounds(x, y,
                    Math.max(size.width, width),
                    Math.max(size.height, height));
        } else {
            super.setBounds(x, y, width, height);
        }
    }

    /**
     * @param multiLineTab the new multiLine tab value
     */
    public void isMultiLineTabbed(boolean multiLineTab) {
        this.multiLineTab = multiLineTab;
    }

    /**
     * @param tabsAsSpaces whether tabs are converted to spaces
     */
    public void isTabsAsSpaces(boolean tabsAsSpaces) {
        this.tabsAsSpaces = tabsAsSpaces;
    }

    /**
     * Set the caret to use depending on overtype/insert mode
     *
     * @param isOvertypeMode the new mode; true = overtype
     */
    public void setOvertypeMode(boolean isOvertypeMode) {
        TextEditor.isOvertypeMode = isOvertypeMode;
        int pos = getCaretPosition();
        setCaret(isOvertypeMode() ? overtypeCaret : defaultCaret);
        setCaretPosition(pos);
    }

    /**
     * @param unwrapped the new unwrapped value
     */
    public void setUnwrapped(boolean unwrapped) {
        this.unwrapped = unwrapped;
    }

    private static class FindAction extends AbstractAction {
        public void actionPerformed(ActionEvent ae) {
            FindReplaceUtility.showDialog();
        }
    }

    private static class ReplaceAction extends AbstractAction {
        public void actionPerformed(ActionEvent ae) {
            FindReplaceUtility.showDialog(true);
        }
    }

    private class ShiftTabAction extends AbstractAction {
        public void actionPerformed(ActionEvent ae) {
            try {
                if (multiLineTab && TextEditor.this.getSelectedText() != null) {
                    int end = Utilities.getRowEnd(TextEditor.this, getSelectionEnd());
                    TextEditor.this.setSelectionEnd(end);

                    Element el = Utilities.getParagraphElement(TextEditor.this, getSelectionStart());
                    int start = el.getStartOffset();
                    TextEditor.this.setSelectionStart(start);

                    // remove text and reselect the text
                    String text = tabsAsSpaces ?
                            TAB_BACK_PATTERN.matcher(getSelectedText()).replaceAll("") :
                            getSelectedText().replaceAll("^\t", "");

                    int stop = start + text.length();
                    setRenderRange(start, stop);
                    TextEditor.this.replaceSelection(text);
                    TextEditor.this.select(start, stop);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class TabAction extends AbstractAction {
        public void actionPerformed(ActionEvent ae) {
            try {
                Document doc = TextEditor.this.getDocument();
                String text = tabsAsSpaces ? TABBED_SPACES : "\t";
                if (multiLineTab && getSelectedText() != null) {
                    int end = Utilities.getRowEnd(TextEditor.this, getSelectionEnd());
                    TextEditor.this.setSelectionEnd(end);

                    Element el = Utilities.getParagraphElement(TextEditor.this, getSelectionStart());
                    int start = el.getStartOffset();
                    TextEditor.this.setSelectionStart(start);

                    String toReplace = TextEditor.this.getSelectedText();
                    toReplace = LINE_START.matcher(toReplace).replaceAll(text);

                    int stop = start + toReplace.length();
                    setRenderRange(start, stop);
                    TextEditor.this.replaceSelection(toReplace);
                    TextEditor.this.select(start, stop);
                } else {
                    int pos = TextEditor.this.getCaretPosition();
                    doc.insertString(pos, text, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setRenderRange(int start, int stop) {
        DocumentFilter documentFilter = ((DefaultStyledDocument) TextEditor.this.getDocument()).getDocumentFilter();
        if (documentFilter instanceof SmartDocumentFilter) {
            SmartDocumentFilter smartDocumentFilter = (SmartDocumentFilter) documentFilter;
            smartDocumentFilter.setRenderRange(Tuple.tuple(start, stop));
        }
    }

    /**
     * Paint a horizontal line the width of a column and 1 pixel high
     */
    private static class OvertypeCaret extends DefaultCaret {
        //The overtype caret will simply be a horizontal line one pixel high
        // (once we determine where to paint it)
        public void paint(Graphics g) {
            if (isVisible()) {
                try {
                    JTextComponent component = getComponent();
                    Rectangle r = component.getUI().modelToView(component, getDot());
                    Color c = g.getColor();
                    g.setColor(component.getBackground());
                    g.setXORMode(component.getCaretColor());
                    r.setBounds(r.x, r.y,
                            g.getFontMetrics().charWidth('w'),
                            g.getFontMetrics().getHeight());
                    g.fillRect(r.x, r.y, r.width, r.height);
                    g.setPaintMode();
                    g.setColor(c);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }

        /*
         *  Damage must be overridden whenever the paint method is overridden
         *  (The damaged area is the area the caret is painted in. We must
         *  consider the area for the default caret and this caret)
         */
        protected synchronized void damage(Rectangle r) {
            if (r != null) {
                JTextComponent component = getComponent();
                x = r.x;
                y = r.y;
                Font font = component.getFont();
                width = component.getFontMetrics(font).charWidth('w');
                height = r.height;
                repaint();
            }
        }
    }
}
