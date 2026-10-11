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
package groovy.console.ui.language

import groovy.transform.CompileStatic
import groovy.transform.Internal
import groovy.transform.PackageScope
import org.apache.groovy.lsp.GroovyLanguageClient
import javax.swing.AbstractAction
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.KeyStroke
import javax.swing.ListCellRenderer
import javax.swing.event.DocumentEvent
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import groovy.console.ui.language.ConsoleLanguageHost.Candidate

/**
 * Completion list for the console editor: the rows, the keys, the
 * mouse selection, and the kind badge. {@link ConsoleLanguageHost}
 * keeps the buffer, the debounce, and the diagnostics.
 */
@CompileStatic
@Internal
final class CompletionPopup {

    /**
     * Safety bound for the popup. A resolved receiver can have several
     * hundred members once Groovy extension methods are included; the
     * scroll pane still shows {@link #VISIBLE_ROWS} at a time.
     */
    private static final int MAX_COMPLETIONS = 1024
    /** Rows kept on screen. Further entries use the vertical scrollbar. */
    private static final int VISIBLE_ROWS = 12

    /** IntelliJ-style kind colors. Each is distinct from the name text. */
    private static final Color KIND_METHOD = new Color(219, 92, 92)
    private static final Color KIND_FIELD = new Color(110, 127, 232)
    private static final Color KIND_PROPERTY = new Color(58, 168, 158)
    private static final Color KIND_VARIABLE = new Color(122, 138, 153)
    private static final Color KIND_CLASS = new Color(214, 158, 54)
    private static final Color KIND_INTERFACE = new Color(86, 166, 104)
    private static final Color KIND_ENUM = new Color(163, 113, 212)
    private static final Color KIND_KEYWORD = new Color(138, 122, 90)
    private static final Color KIND_SNIPPET = new Color(76, 141, 255)
    private static final Color KIND_OTHER = new Color(154, 160, 166)

    private final JTextPane input
    /** Candidates from the last Alt+/, before the typed prefix narrows them. */
    private final List<Candidate> completionAll = []
    private JPopupMenu completionPopup
    private JList<Candidate> completionList
    private DefaultListModel<Candidate> completionModel
    private boolean completionOpen
    /** Document offset where the name being completed starts. */
    private int completionAnchor = -1
    /** Document offset just after the name being completed. */
    private int completionEnd = -1

    @PackageScope
    CompletionPopup(JTextPane input) {
        this.input = input
    }

    @PackageScope
    boolean isOpen() {
        completionOpen
    }

    /**
     * Builds the completion popup without showing it, so tests can invoke the
     * insert action without a display. Methods show their parameter types and
     * names in gray, and the return type in its own column at the right.
     * The list keeps {@link #VISIBLE_ROWS} on screen and scrolls for the rest.
     * Focus stays in the editor so later keystrokes narrow this list.
     * Moving the pointer over a row selects it, with the same background the
     * up and down keys use.
     */
    @PackageScope
    JPopupMenu completionMenu(List<Candidate> items) {
        List<Candidate> source = items == null ? new ArrayList<Candidate>() : items
        List<Candidate> shown = new ArrayList<Candidate>(source.take(MAX_COMPLETIONS))
        DefaultListModel<Candidate> model = new DefaultListModel<Candidate>()
        for (Candidate item : shown) {
            model.addElement(item)
        }
        JList<Candidate> list = new JList<Candidate>(model)
        list.visibleRowCount = Math.min(VISIBLE_ROWS, Math.max(shown.size(), 1))
        list.cellRenderer = new CompletionRenderer()
        list.focusable = false
        // Hover selects a row. Autoscroll would move that row out from under
        // the pointer and deliver another move, which never returns.
        list.autoscrolls = false
        JScrollPane scroll = new JScrollPane(list)
        scroll.border = null
        scroll.focusable = false
        scroll.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        scroll.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        // Both fixed sizes are required. JList walks every row through the
        // renderer when either one is missing, and a receiver such as String
        // has several hundred members. That walk runs on the editor thread
        // while the popup opens.
        int[] cell = completionCellSize(list, shown)
        int bar = (int) scroll.verticalScrollBar.preferredSize.width
        int popupWidth = Math.min(cell[0] + bar, 560)
        list.fixedCellWidth = Math.max(popupWidth - bar, 1)
        list.fixedCellHeight = cell[1]
        scroll.preferredSize = new Dimension(popupWidth, cell[1] * list.visibleRowCount)
        JPopupMenu menu = new JPopupMenu()
        menu.focusable = false
        menu.add(scroll)
        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            void popupMenuWillBecomeVisible(PopupMenuEvent event) {
            }

            @Override
            void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
                completionOpen = false
            }

            @Override
            void popupMenuCanceled(PopupMenuEvent event) {
                completionOpen = false
            }
        })
        boolean[] selecting = new boolean[1]
        MouseAdapter pointer = new MouseAdapter() {
            @Override
            void mousePressed(MouseEvent event) {
                int index = completionRowAt(list, event.point)
                if (index < 0) {
                    return
                }
                list.selectedIndex = index
                Candidate item = list.model.getElementAt(index)
                hideCompletion()
                acceptCompletion(item)
            }

            @Override
            void mouseMoved(MouseEvent event) {
                if (selecting[0]) {
                    return
                }
                int index = completionRowAt(list, event.point)
                if (index < 0 || index == list.selectedIndex) {
                    return
                }
                selecting[0] = true
                try {
                    list.selectedIndex = index
                } finally {
                    selecting[0] = false
                }
            }
        }
        list.addMouseListener(pointer)
        list.addMouseMotionListener(pointer)
        list.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), 'choose')
        list.actionMap.put('choose', new AbstractAction() {
            @Override
            void actionPerformed(ActionEvent event) {
                int index = list.selectedIndex
                if (index < 0 || index >= list.model.size) {
                    return
                }
                Candidate item = list.model.getElementAt(index)
                hideCompletion()
                acceptCompletion(item)
            }
        })
        if (model.size() > 0) {
            list.selectedIndex = 0
        }
        menu
    }

    /**
     * Remembers {@code items} and narrows them as the editor text changes.
     * {@code anchor} is the document offset of the name being typed.
     *
     * @return the list, so tests can read the filtered model
     */
    @PackageScope
    JList<Candidate> armCompletion(List<Candidate> items, int anchor) {
        hideCompletion()
        JPopupMenu menu = completionMenu(items)
        completionPopup = menu
        JScrollPane scroll = (JScrollPane) menu.getComponent(0)
        completionList = (JList<Candidate>) scroll.viewport.view
        completionModel = (DefaultListModel<Candidate>) completionList.model
        completionAll.clear()
        for (int i = 0; i < completionModel.size(); i++) {
            completionAll.add(completionModel.getElementAt(i))
        }
        completionAnchor = Math.max(anchor, 0)
        String text = ConsoleLanguageHost.textOf(input)
        int caret = input == null ? completionAnchor : Math.min(Math.max(input.caretPosition, 0), text.length())
        completionEnd = Math.max(caret, completionAnchor)
        completionOpen = true
        refillCompletion()
        completionList
    }

    @PackageScope
    boolean completionShowing() {
        completionOpen
    }

    /**
     * Width and height of one completion row, measured from the text.
     * Building a component for every candidate makes the editor thread walk
     * the whole list again when the popup opens.
     *
     * @return {@code [width, height]}
     */
    @PackageScope
    static int[] completionCellSize(JList<Candidate> list, List<Candidate> items) {
        Font font = list?.font
        if (font == null) {
            font = new Font(Font.SANS_SERIF, Font.PLAIN, 12)
        }
        FontMetrics metrics = list == null ? null : list.getFontMetrics(font)
        int text = metrics == null ? 16 : metrics.height
        int height = Math.max(text + 4, 20)
        int width = 280
        if (items != null && metrics != null) {
            for (Candidate item : items) {
                String name = item?.label() ?: ''
                SignatureParts parts = SignatureParts.of(item)
                String params = parts == null ? '' : '(' + parts.parameters + ')'
                String returns = parts?.returns ?: ''
                // Borders, kind disc, gap, and the return column's left padding.
                int row = 16 + 16 + 8 + 32
                row += metrics.stringWidth(name)
                row += metrics.stringWidth(params)
                row += metrics.stringWidth(returns)
                if (row > width) {
                    width = row
                }
            }
        }
        new int[] { width, height }
    }

    /**
     * Row under {@code point}, or {@code -1} when the point is outside every
     * cell. {@link JList#locationToIndex} reports the nearest row even in the
     * empty area below the last one, so the cell bounds are checked as well.
     */
    @PackageScope
    static int completionRowAt(JList<?> list, Point point) {
        if (list == null || point == null || list.model == null) {
            return -1
        }
        int index = list.locationToIndex(point)
        if (index < 0 || index >= list.model.size) {
            return -1
        }
        Rectangle bounds = list.getCellBounds(index, index)
        if (bounds == null || !bounds.contains(point)) {
            return -1
        }
        index
    }

    /**
     * Up and down move the selection. Enter inserts it. Escape closes the
     * list. Other keys are left for the editor.
     *
     * @return {@code true} when the event was consumed
     */
    @PackageScope
    boolean handleCompletionKey(KeyEvent event) {
        int code = event.keyCode
        if (code == KeyEvent.VK_ESCAPE) {
            hideCompletion()
            event.consume()
            return true
        }
        if (code == KeyEvent.VK_ENTER) {
            chooseArmed()
            event.consume()
            return true
        }
        if (code == KeyEvent.VK_DOWN) {
            moveSelection(1)
            event.consume()
            return true
        }
        if (code == KeyEvent.VK_UP) {
            moveSelection(-1)
            event.consume()
            return true
        }
        false
    }

    private void moveSelection(int delta) {
        if (completionList == null || completionModel == null || completionModel.size() == 0) {
            return
        }
        int next = completionList.selectedIndex + delta
        if (next < 0) {
            next = 0
        }
        if (next >= completionModel.size()) {
            next = completionModel.size() - 1
        }
        completionList.selectedIndex = next
        completionList.ensureIndexIsVisible(next)
    }

    private void chooseArmed() {
        if (completionList == null || completionModel == null) {
            return
        }
        int index = completionList.selectedIndex
        if (index < 0 || index >= completionModel.size()) {
            return
        }
        Candidate item = completionModel.getElementAt(index)
        int end = completionEnd
        hideCompletion()
        if (input != null && end >= 0) {
            int length = ConsoleLanguageHost.textOf(input).length()
            input.caretPosition = Math.min(Math.max(end, 0), length)
        }
        acceptCompletion(item)
    }

    @PackageScope
    void hideCompletion() {
        completionOpen = false
        completionAnchor = -1
        completionEnd = -1
        completionAll.clear()
        JPopupMenu menu = completionPopup
        completionPopup = null
        completionList = null
        completionModel = null
        if (menu != null && menu.visible) {
            menu.visible = false
        }
    }

    @PackageScope
    void onDocumentEdit(DocumentEvent event) {
        if (!completionOpen || event == null) {
            return
        }
        int length = event.length
        int offset = event.offset
        if (event.type == DocumentEvent.EventType.INSERT) {
            if (offset < completionAnchor) {
                completionAnchor += length
                completionEnd += length
            } else if (offset <= completionEnd) {
                completionEnd += length
            } else {
                hideCompletion()
                return
            }
        } else if (event.type == DocumentEvent.EventType.REMOVE) {
            int end = offset + length
            if (offset < completionAnchor && end > completionAnchor) {
                hideCompletion()
                return
            }
            if (end <= completionAnchor) {
                completionAnchor -= length
                completionEnd -= length
            } else if (offset < completionEnd) {
                completionEnd -= Math.min(end, completionEnd) - offset
                if (completionEnd < completionAnchor) {
                    completionEnd = completionAnchor
                }
            } else {
                hideCompletion()
                return
            }
        }
        refillCompletion()
    }

    private void refillCompletion() {
        if (!completionOpen || completionList == null || completionModel == null || input == null) {
            return
        }
        String text = ConsoleLanguageHost.textOf(input)
        if (completionAnchor < 0 || completionEnd < completionAnchor || completionEnd > text.length()) {
            hideCompletion()
            return
        }
        String prefix = text.substring(completionAnchor, completionEnd)
        for (int i = 0; i < prefix.length(); i++) {
            if (!Character.isJavaIdentifierPart(prefix.charAt(i))) {
                hideCompletion()
                return
            }
        }
        completionModel.clear()
        for (Candidate item : completionAll) {
            if (matchesPrefix(item?.label(), prefix)) {
                completionModel.addElement(item)
            }
        }
        int rows = Math.min(VISIBLE_ROWS, Math.max(completionModel.size(), 1))
        completionList.visibleRowCount = rows
        if (completionModel.size() > 0) {
            completionList.selectedIndex = 0
            completionList.ensureIndexIsVisible(0)
        }
        Container view = completionList.parent
        if (view?.parent instanceof JScrollPane) {
            JScrollPane scroll = (JScrollPane) view.parent
            int rowHeight = completionList.fixedCellHeight > 0 ? completionList.fixedCellHeight : 18
            Dimension current = scroll.preferredSize
            scroll.preferredSize = new Dimension((int) current.width, rowHeight * rows)
        }
    }

    /**
     * Start of the identifier that touches {@code caret}, or {@code caret}
     * when the caret is not inside a name.
     */
    @PackageScope
    static int prefixStart(String text, int caret) {
        if (!text || caret <= 0) {
            return 0
        }
        int start = Math.min(caret, text.length())
        while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) {
            start--
        }
        start
    }

    /**
     * Case-insensitive prefix match. An empty prefix matches every label.
     */
    @PackageScope
    static boolean matchesPrefix(String label, String prefix) {
        if (!prefix) {
            return true
        }
        if (!label || label.length() < prefix.length()) {
            return false
        }
        label.regionMatches(true, 0, prefix, 0, prefix.length())
    }

    /**
     * Text written into the editor. A method contributes its name.
     * Parameter placeholders stay in the list and are not inserted.
     * Snippets still expand.
     */
    @PackageScope
    static String completionInsert(Candidate item) {
        if (item == null) {
            return ''
        }
        String kind = item.kind()?.toLowerCase(Locale.ROOT)
        if (kind == 'method' || kind == 'function') {
            return item.label() ?: ''
        }
        GroovyLanguageClient.flattenSnippet(item.insertText ?: item.label)
    }

    @PackageScope
    void acceptCompletion(Candidate item) {
        ConsoleLanguageHost.insertAtCaret(input, completionInsert(item))
    }

    @PackageScope
    void showMenu(List<Candidate> items) {
        if (input == null || !items) {
            hideCompletion()
            return
        }
        String text = ConsoleLanguageHost.textOf(input)
        int caret = Math.min(Math.max(input.caretPosition, 0), text.length())
        armCompletion(items, prefixStart(text, caret))
        JPopupMenu menu = completionPopup
        try {
            Point point = input.caret.magicCaretPosition ?: new Point(0, 0)
            menu.show(input, (int) point.x, (int) point.y + 16)
        } catch (RuntimeException ignored) {
        }
    }

    /**
     * IntelliJ's one-letter kind mark. Methods and static methods are {@code m}.
     * An unknown kind is {@code ?}.
     */
    @PackageScope
    static String kindMark(String kind) {
        switch (kind == null ? '' : kind.toLowerCase(Locale.ROOT)) {
            case 'method':
            case 'function':
                return 'm'
            case 'field':
                return 'f'
            case 'property':
                return 'p'
            case 'variable':
                return 'v'
            case 'class':
                return 'c'
            case 'interface':
                return 'i'
            case 'enum':
                return 'e'
            case 'keyword':
                return 'k'
            case 'snippet':
                return 's'
            default:
                return '?'
        }
    }

    /**
     * Fill color for {@link #kindMark}. Kinds that share a letter still share
     * a color; every kind color differs from ordinary name text.
     */
    @PackageScope
    static Color kindColor(String kind) {
        switch (kind == null ? '' : kind.toLowerCase(Locale.ROOT)) {
            case 'method':
            case 'function':
                return KIND_METHOD
            case 'field':
                return KIND_FIELD
            case 'property':
                return KIND_PROPERTY
            case 'variable':
                return KIND_VARIABLE
            case 'class':
                return KIND_CLASS
            case 'interface':
                return KIND_INTERFACE
            case 'enum':
                return KIND_ENUM
            case 'keyword':
                return KIND_KEYWORD
            case 'snippet':
                return KIND_SNIPPET
            default:
                return KIND_OTHER
        }
    }

    /**
     * Parameter list and return type of a method completion.
     */
    @PackageScope
    static final class SignatureParts {
        final String parameters
        final String returns

        private SignatureParts(String parameters, String returns) {
            this.parameters = parameters
            this.returns = returns
        }

        static SignatureParts of(Candidate item) {
            if (item?.parameters() == null && item?.returns() == null) {
                return null
            }
            new SignatureParts(item.parameters() ?: '', item.returns() ?: '')
        }
    }

    /**
     * One completion row: the name in the list color, the parameter types and
     * names in gray, and the return type in gray at the right.
     */
    @CompileStatic
    private static final class CompletionRenderer implements ListCellRenderer<Candidate> {
        @Override
        Component getListCellRendererComponent(JList<? extends Candidate> list, Candidate value, int index,
                                               boolean selected, boolean focus) {
            SignatureParts parts = SignatureParts.of(value)
            JPanel row = new JPanel(new BorderLayout(0, 0))
            row.border = BorderFactory.createEmptyBorder(2, 8, 2, 8)
            row.opaque = true
            Color rowBackground = selected ? list.selectionBackground : list.background
            Color rowForeground = selected ? list.selectionForeground : list.foreground
            row.background = rowBackground
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0))
            left.opaque = false
            String kind = value == null ? null : value.kind()
            KindBadge badge = new KindBadge(kindMark(kind), kindColor(kind))
            left.add(badge)
            left.add(Box.createHorizontalStrut(8))
            JLabel name = new JLabel(value?.label() ?: '')
            name.name = 'completion-name'
            name.foreground = rowForeground
            left.add(name)
            // A fixed light gray disappears on a light selection. Mix the row's
            // own foreground into its background so parameters and the return
            // type stay visible, and still quieter than the name.
            Color muted = secondary(rowForeground, rowBackground)
            if (parts != null) {
                JLabel parameters = new JLabel('(' + parts.parameters + ')')
                parameters.name = 'completion-parameters'
                parameters.foreground = muted
                left.add(parameters)
            }
            row.add(left, BorderLayout.CENTER)
            JLabel returns = new JLabel(parts?.returns ?: '')
            returns.name = 'completion-return'
            returns.horizontalAlignment = JLabel.RIGHT
            returns.border = BorderFactory.createEmptyBorder(0, 32, 0, 0)
            returns.foreground = muted
            row.add(returns, BorderLayout.EAST)
            row
        }

        /** Foreground pulled part way toward the row background. */
        private static Color secondary(Color foreground, Color background) {
            Color ink = foreground == null ? Color.BLACK : foreground
            Color paper = background == null ? Color.WHITE : background
            int towardPaper = 45
            int keep = 100 - towardPaper
            int r = (ink.red * keep + paper.red * towardPaper).intdiv(100)
            int g = (ink.green * keep + paper.green * towardPaper).intdiv(100)
            int b = (ink.blue * keep + paper.blue * towardPaper).intdiv(100)
            new Color(r, g, b)
        }
    }

    /**
     * Colored disc with one letter. The disc, not the glyph color, carries
     * the kind, so the mark stays separate from the name beside it.
     */
    @CompileStatic
    private static final class KindBadge extends JComponent {
        private final String letter
        private final Color fill

        KindBadge(String letter, Color fill) {
            this.letter = letter
            this.fill = fill
            name = 'completion-kind'
            putClientProperty('mark', letter)
            putClientProperty('fill', fill)
            toolTipText = letter
            font = new Font(Font.SANS_SERIF, Font.BOLD, 10)
            opaque = false
        }

        @Override
        Dimension getPreferredSize() {
            new Dimension(16, 16)
        }

        @Override
        Dimension getMinimumSize() {
            getPreferredSize()
        }

        @Override
        void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create()
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g.color = fill
            g.fillOval(0, 0, 15, 15)
            g.color = Color.WHITE
            g.font = font
            int textWidth = g.fontMetrics.stringWidth(letter)
            int x = ((15 - textWidth) / 2) as int
            int y = (((15 - g.fontMetrics.height) / 2) as int) + g.fontMetrics.ascent
            g.drawString(letter, x, y)
            g.dispose()
        }
    }
}
