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

package groovy.ui

import groovy.inspect.swingui.ObjectBrowser
import groovy.swing.SwingBuilder
import groovy.ui.text.FindReplaceUtility
import java.awt.*
import java.awt.image.BufferedImage
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
// to disambiguate from java.awt.List
import java.util.List
import javax.swing.*
import javax.swing.event.CaretEvent
import javax.swing.event.CaretListener
import javax.swing.text.Style
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext
import javax.swing.text.StyledDocument
import javax.swing.text.Element
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.StackTraceUtils

/**
 * Groovy Swing console.
 *
 * Allows user to interactively enter and execute Groovy.
 *
 * @version $Id$
 * @author Danno Ferrin
 * @author Dierk Koenig, changed Layout, included Selection sensitivity, included ObjectBrowser
 * @author Alan Green more features: history, System.out capture, bind result to _
 */
class Console implements CaretListener {

    // Whether or not std output should be captured to the console
    def captureStdOut = true

    // Maximum size of history
    int maxHistory = 10

    // Maximum number of characters to show on console at any time
    int maxOutputChars = 20000

    // UI
    SwingBuilder swing
    JFrame frame
    JTextPane inputArea
    JTextPane outputArea
    JLabel statusLabel
    JDialog runWaitDialog

    // row info
    Element rootElement
    int cursorPos
    int rowNum
    int colNum

    // Styles for output area
    Style promptStyle;
    Style commandStyle;
    Style outputStyle;
    Style resultStyle;

    // Internal history
    List history = []
    int historyIndex = 1 // valid values are 0..history.length()

    // Current editor state
    boolean dirty
    int textSelectionStart  // keep track of selections in inputArea
    int textSelectionEnd
    def scriptFile

    // Running scripts
    GroovyShell shell
    int scriptNameCounter = 0
    def systemOutInterceptor
    def runThread = null
    Closure beforeExecution
    Closure afterExecution

    static String ICON_PATH = 'groovy/ui/ConsoleIcon.png' // used by ObjectBrowser too

    static void main(args) {
        java.util.logging.Logger.getLogger(StackTraceUtils.STACK_LOG_NAME).useParentHandlers = true
        def console = new Console()
        console.run()
    }

    Console() {
        shell = new GroovyShell()
    }

    Console(Binding binding) {
        shell = new GroovyShell(binding)
    }

    Console(ClassLoader parent, Binding binding) {
        shell = new GroovyShell(parent,binding)
    }

    void run() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        System.setProperty("apple.laf.useScreenMenuBar", "true")
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "GroovyConsole")
        System.setProperty("groovy.sanitized.stacktraces", """org.codehaus.groovy.runtime.
                org.codehaus.groovy.
                groovy.lang.
                gjdk.groovy.lang.
                sun.
                java.lang.reflect.
                java.lang.Thread
                groovy.ui.Console""")

        swing = new SwingBuilder()

        def inputEditor = new ConsoleTextEditor()

        swing.actions {
            action(id: 'newFileAction',
                name: 'New File',
                closure: this.&fileNewFile,
                mnemonic: 'N',
                accelerator: shortcut('N')
            )
            action(id: 'newWindowAction',
                name: 'New Window',
                closure: this.&fileNewWindow,
                mnemonic: 'W',
                accelerator: shortcut('shift N')
            )
            action(id: 'openAction',
                name: 'Open',
                closure: this.&fileOpen,
                mnemonic: 'O',
                accelerator: shortcut('O')
            )
            action(id: 'saveAction',
                name: 'Save',
                closure: this.&fileSave,
                mnemonic: 'S',
                accelerator: shortcut('S')
            )
            action(id: 'saveAsAction',
                name: 'Save As...',
                closure: this.&fileSaveAs,
                mnemonic: 'A',
            )
            action(inputEditor.printAction,
                id: 'printAction',
                name: 'Print...',
                mnemonic: 'P',
                accelerator: shortcut('P'))
            action(id: 'exitAction',
                name: 'Exit',
                closure: this.&exit,
                mnemonic: 'X'
            )
            // whether or not application exit should have an
            // accellerator is debatable in usability circles
            // at the very least a confirm dialog should dhow up
            //accelerator: shortcut('Q')
            action(inputEditor.undoAction,
                id: 'undoAction',
                name: 'Undo',
                mnemonic: 'U',
                accelerator: shortcut('Z')
            )
            action(inputEditor.redoAction,
                id: 'redoAction',
                name: 'Redo',
                closure: this.&redo,
                mnemonic: 'R',
                accelerator: shortcut('shift Z') // is control-shift-Z or control-Y more common?
            )
            action(id: 'findAction',
                name: 'Find...',
                closure: this.&find,
                mnemonic: 'F',
                accelerator: shortcut('F')
            )
            action(id: 'findNextAction',
                name: 'Find Next',
                closure: this.&findNext,
                mnemonic: 'N',
                accelerator: KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0)
            )
            action(id: 'findPreviousAction',
                name: 'Find Previous',
                closure: this.&findPrevious,
                mnemonic: 'V',
                accelerator: KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_DOWN_MASK)
            )
            action(id: 'replaceAction',
                name: 'Replace...',
                closure: this.&replace,
                mnemonic: 'E',
                accelerator: shortcut('H')
            )
            action(id: 'cutAction',
                name: 'Cut',
                closure: this.&cut,
                mnemonic: 'T',
                accelerator: shortcut('X')
            )
            action(id: 'copyAction',
                name: 'Copy',
                closure: this.&copy,
                mnemonic: 'C',
                accelerator: shortcut('C')
            )
            action(id: 'pasteAction',
                name: 'Paste',
                closure: this.&paste,
                mnemonic: 'P',
                accelerator: shortcut('V')
            )
            action(id: 'selectAllAction',
                name: 'Select All',
                closure: this.&selectAll,
                mnemonic: 'A',
                accelerator: shortcut('A')
            )
            action(id: 'historyPrevAction',
                name: 'Previous',
                closure: this.&historyPrev,
                mnemonic: 'P',
                accelerator: shortcut(KeyEvent.VK_COMMA)
            )
            action(id: 'historyNextAction',
                name: 'Next',
                closure: this.&historyNext,
                mnemonic: 'N',
                accelerator: shortcut(KeyEvent.VK_PERIOD)
            )
            action(id: 'clearOutputAction',
                name: 'Clear Output',
                closure: this.&clearOutput,
                mnemonic: 'O',
                accelerator: shortcut('W')
            )
            action(id: 'runAction',
                name: 'Run',
                closure: this.&runScript,
                mnemonic: 'R',
                keyStroke: 'ctrl ENTER', // does this need to be shortcutted or explicitly ctrl?
                accelerator: shortcut('R')
            )
            action(id: 'inspectLastAction',
                name: 'Inspect Last',
                closure: this.&inspectLast,
                mnemonic: 'I',
                accelerator: shortcut('I')
            )
            action(id: 'inspectVariablesAction',
                name: 'Inspect Variables',
                closure: this.&inspectVariables,
                mnemonic: 'V',
                accelerator: shortcut('J')
            )
            action(id: 'captureStdOutAction',
                name: 'Capture Standard Output',
                closure: this.&captureStdOut,
                mnemonic: 'C'
            )
            action(id: 'fullStackTracesAction',
                name: 'Show Full Stack Traces',
                closure: this.&fullStackTraces,
                mnemonic: 'F'
            )
            try {
                System.setProperty("groovy.full.stacktrace",
                    Boolean.toString(Boolean.parseBoolean(System.getProperty("groovy.full.stacktrace", "false"))))
            } catch (SecurityException se) {
                fullStackTracesAction.enabled = false;
            }
            action(id: 'largerFontAction',
                name: 'Larger Font',
                closure: this.&largerFont,
                mnemonic: 'L',
                accelerator: shortcut('shift L')
            )
            action(id: 'smallerFontAction',
                name: 'Smaller Font',
                closure: this.&smallerFont,
                mnemonic: 'S',
                accelerator: shortcut('shift S')
            )
            action(id: 'aboutAction',
                name: 'About',
                closure: this.&showAbout,
                mnemonic: 'A'
            )
            action(id: 'interruptAction',
                name: 'Interrupt',
                closure: this.&confirmRunInterrupt
            )
        }


        frame = swing.frame(
            title: 'GroovyConsole',
            location: [100,100], // in groovy 2.0 use platform default location
            defaultCloseOperation: WindowConstants.DO_NOTHING_ON_CLOSE
        ) {
            menuBar {
                menu(text: 'File', mnemonic: 'F') {
                    menuItem(newFileAction)
                    menuItem(newWindowAction)
                    menuItem(openAction)
                    separator()
                    menuItem(saveAction)
                    menuItem(saveAsAction)
                    separator()
                    menuItem(printAction)
                    separator()
                    menuItem(exitAction)
                }

                menu(text: 'Edit', mnemonic: 'E') {
                    menuItem(undoAction)
                    menuItem(redoAction)
                    separator()
                    menuItem(cutAction)
                    menuItem(copyAction)
                    menuItem(pasteAction)
                    separator()
                    menuItem(findAction)
                    menuItem(findNextAction)
                    menuItem(findPreviousAction)
                    menuItem(replaceAction)
                    separator()
                    menuItem(selectAllAction)
                }

                menu(text: 'View', mnemonic: 'V') {
                    menuItem(clearOutputAction)
                    separator()
                    menuItem(largerFontAction)
                    menuItem(smallerFontAction)
                    separator()
                    checkBoxMenuItem(captureStdOutAction, selected: captureStdOut)
                    checkBoxMenuItem(fullStackTracesAction, id:'fullStackTracesMenuItem')
                    try {
                        fullStackTracesMenuItem.selected =
                            Boolean.parseBoolean(System.getProperty("groovy.full.stacktrace", "false"))
                    } catch (SecurityException se) { }
                }

                menu(text: 'History', mnemonic: 'I') {
                    menuItem(historyPrevAction)
                    menuItem(historyNextAction)
                }

                menu(text: 'Script', mnemonic: 'S') {
                    menuItem(runAction)
                    separator()
                    menuItem(inspectLastAction)
                    menuItem(inspectVariablesAction)
                }

                menu(text: 'Help', mnemonic: 'H') {
                    menuItem(aboutAction)
                }
            }

            borderLayout()

            splitPane(id: 'splitPane', resizeWeight: 0.50F,
                orientation: JSplitPane.VERTICAL_SPLIT, constraints: BorderLayout.CENTER)
            {
                widget(inputEditor)
                scrollPane {
                    textPane(id: 'outputArea',
                        editable: false,
                        background: new Color(255,255,218)
                    )
                }
            }

            panel(id: 'statusPanel', constraints: BorderLayout.SOUTH, layout : new BorderLayout()) {
                label(id: 'status',
                     text: 'Welcome to Groovy.',
                     constraints: BorderLayout.CENTER,
                     border: BorderFactory.createCompoundBorder(
                               BorderFactory.createLoweredBevelBorder(),
                               BorderFactory.createEmptyBorder(1,3,1,3))
                )

                label(id: 'rowNumAndColNum',
                       text: '1:1',
                       border: BorderFactory.createEmptyBorder(1,3,1,3),
                       constraints: BorderLayout.EAST,
                       border: BorderFactory.createCompoundBorder(
                               BorderFactory.createLoweredBevelBorder(),
                               BorderFactory.createEmptyBorder(1,3,1,3))
                )
            }
        }   // end of frame

        inputArea = inputEditor.textEditor

        // attach ctrl-enter to input area
        swing.container(inputArea) {
            action(runAction)
        }

        outputArea = swing.outputArea
        addStylesToDocument(outputArea)

        Graphics g = GraphicsEnvironment.localGraphicsEnvironment.createGraphics (new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB))
        FontMetrics fm = g.getFontMetrics(outputArea.font)
        outputArea.preferredSize = [
            fm.charWidth(0x77) * 81,
            (fm.getHeight() + fm.leading) * 12] as Dimension
        //inputArea.setFont(outputArea.font)
        inputEditor.preferredSize = outputArea.preferredSize
        // good enough, ther are margins and scrollbars and such to worry about for 80x12x2


        statusLabel = swing.status

        runWaitDialog = swing.dialog(title: 'Groovy executing',
                owner: frame,
                modal: true
        ) {
            vbox(border: BorderFactory.createEmptyBorder(6, 6, 6, 6)) {
                label(text: "Groovy is now executing. Please wait.", alignmentX: 0.5f)
                vstrut()
                button(interruptAction,
                    margin: new Insets(10, 20, 10, 20),
                    alignmentX: 0.5f
                )
            }
        } // end of runWaitDialog

        // add listeners
        frame.windowClosing = this.&exit
        inputArea.addCaretListener(this)
        inputArea.document.undoableEditHappened = { setDirty(true) }

        rootElement = inputArea.document.defaultRootElement

        systemOutInterceptor = new SystemOutputInterceptor(this.&notifySystemOut)
        systemOutInterceptor.start();

        bindResults()

        // add icon
        def icon = new ImageIcon(getClass().classLoader.getResource(ICON_PATH))
        frame.iconImage = icon.image

        frame.pack()
        frame.show()
        SwingUtilities.invokeLater({inputArea.requestFocus()});
    }

    void addStylesToDocument(JTextPane outputArea) {
        outputArea.setFont(new Font("Monospaced", outputArea.font.style, outputArea.font.size))
        StyledDocument doc = outputArea.getStyledDocument();

        Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", defStyle);
        StyleConstants.setFontFamily(regular, "Monospaced")

        promptStyle = doc.addStyle("prompt", regular)
        StyleConstants.setForeground(promptStyle, Color.BLUE)

        commandStyle = doc.addStyle("command", regular);
        StyleConstants.setForeground(commandStyle, Color.MAGENTA)

        outputStyle = regular

        resultStyle = doc.addStyle("result", regular)
        StyleConstants.setBackground(resultStyle, Color.BLUE)
        StyleConstants.setBackground(resultStyle, Color.YELLOW)
    }

    void addToHistory(record) {
        history.add(record)
        // history.size here just retrieves method closure
        if (history.size() > maxHistory) {
            history.remove(0)
        }
        // history.size doesn't work here either
        historyIndex = history.size()
    }

    // Append a string to the output area
    void appendOutput(text, style){
        def doc = outputArea.styledDocument
        doc.insertString(doc.length, text, style)

        // Ensure we don't have too much in console (takes too much memory)
        if (doc.length > maxOutputChars) {
            doc.remove(0, doc.length - maxOutputChars)
        }
    }

    // Append a string to the output area on a new line
    void appendOutputNl(text, style){
        def doc = outputArea.styledDocument
        def len = doc.length
        if (len > 0 && doc.getText(len - 1, 1) != "\n") {
            appendOutput("\n", style);
        }
        appendOutput(text, style)
    }

    // Return false if use elected to cancel
    boolean askToSaveFile() {
        if (scriptFile == null || !dirty) {
            return true
        }
        switch (JOptionPane.showConfirmDialog(frame,
            "Save changes to " + scriptFile.name + "?",
            "GroovyConsole", JOptionPane.YES_NO_CANCEL_OPTION))
        {
            case JOptionPane.YES_OPTION:
                return fileSave()
            case JOptionPane.NO_OPTION:
                return true
            default:
                return false
        }
    }

    void beep() {
        Toolkit.defaultToolkit.beep()
    }

    // Binds the "_" and "__" variables in the shell
    void bindResults() {
        shell.setVariable("_", getLastResult()) // lastResult doesn't seem to work
        shell.setVariable("__", history.collect {it.result})
    }

    // Handles menu event
    void captureStdOut(EventObject evt) {
        captureStdOut = evt.source.selected
    }

    void fullStackTraces(EventObject evt) {
        System.setProperty("groovy.full.stacktrace",
            Boolean.toString(evt.source.selected))
    }

    void caretUpdate(CaretEvent e){
        textSelectionStart = Math.min(e.dot,e.mark)
        textSelectionEnd = Math.max(e.dot,e.mark)

        setRowNumAndColNum()
    }

    void clearOutput(EventObject evt = null) {
        outputArea.setText('')
    }

    // Confirm whether to interrupt the running thread
    void confirmRunInterrupt(EventObject evt) {
        def rc = JOptionPane.showConfirmDialog(frame, "Attempt to interrupt script?",
            "GroovyConsole", JOptionPane.YES_NO_OPTION)
        if (rc == JOptionPane.YES_OPTION && runThread != null) {
            runThread.interrupt()
        }
    }

    void exit(EventObject evt = null) {
        if (askToSaveFile()) {
            frame.hide()
            frame.dispose()
        }

        systemOutInterceptor.stop();
    }

    void fileNewFile(EventObject evt = null) {
        if (askToSaveFile()) {
            scriptFile = null
            setDirty(false)
            inputArea.text = ''
        }
    }

    // Start a new window with a copy of current variables
    void fileNewWindow(EventObject evt = null) {
      (new Console(new Binding(new HashMap(shell.context.variables)))).run()
    }

    void fileOpen(EventObject evt = null) {
        scriptFile = selectFilename();
        if (scriptFile != null) {
            inputArea.text = scriptFile.readLines().join('\n');
            setDirty(false)
            inputArea.caretPosition = 0
        }
    }

    // Save file - return false if user cancelled save
    boolean fileSave(EventObject evt = null) {
        if (scriptFile == null) {
            return fileSaveAs(evt)
        } else {
            scriptFile.write(inputArea.text)
            setDirty(false);
            return true
        }
    }

    // Save file - return false if user cancelled save
    boolean fileSaveAs(EventObject evt = null) {
        scriptFile = selectFilename("Save");
        if (scriptFile != null) {
            scriptFile.write(inputArea.text)
            setDirty(false);
            return true
        } else {
            return false
        }
    }

    def finishException(Throwable t) {
        statusLabel.text = 'Execution terminated with exception.'
        history[-1].exception = t

        appendOutputNl("Exception thrown: ", promptStyle)
        appendOutput(t.toString(), resultStyle)

        StringWriter sw = new StringWriter()
        new PrintWriter(sw).withWriter { pw -> StackTraceUtils.deepSanitize(t).printStackTrace(pw) }

        appendOutputNl("\n${sw.buffer}\n", outputStyle)
        bindResults()
    }

    def finishNormal(Object result) {
        // Take down the wait/cancel dialog
        history[-1].result = result
        if (result != null) {
            statusLabel.text = 'Execution complete.'
            appendOutputNl("Result: ", promptStyle)
            appendOutput("${InvokerHelper.inspect(result)}", resultStyle)
        } else {
            statusLabel.text = 'Execution complete. Result was null.'
        }
        bindResults()
    }

    // Gets the last, non-null result
    def getLastResult() {
        // runtime bugs in here history.reverse produces odd lookup
        // return history.reverse.find {it != null}
        if (!history) {
            return
        }
        for (i in (history.size() - 1)..0) {
            if (history[i].result != null) {
                return history[i].result
            }
        }
        return null
    }

    // Allow access to shell from outside console
    // (useful for configuring shell before startup)
    GroovyShell getShell() {
        return shell
    }

    void historyNext(EventObject evt = null) {
        if (historyIndex < history.size()) {
            historyIndex++;
            setInputTextFromHistory()
        } else {
            statusLabel.text = "Can't go past end of history (time travel not allowed)"
            beep()
        }
    }

    void historyPrev(EventObject evt = null) {
        if (historyIndex > 0) {
            historyIndex--;
            setInputTextFromHistory()
        } else {
            statusLabel.text = "Can't go past start of history"
            beep()
        }
    }

    void inspectLast(EventObject evt = null){
        if (null == lastResult) {
            JOptionPane.showMessageDialog(frame, "The last result is null.",
                "Cannot Inspect", JOptionPane.INFORMATION_MESSAGE)
            return
        }
        ObjectBrowser.inspect(lastResult)
    }

    void inspectVariables(EventObject evt = null) {
        ObjectBrowser.inspect(shell.context.variables)
    }

    void largerFont(EventObject evt = null) {
        if (inputArea.font.size > 40) return
        def newFont = new Font('Monospaced', Font.PLAIN, inputArea.font.size + 2)
        inputArea.font = newFont
        outputArea.font = newFont
    }

    Boolean notifySystemOut(String str) {
        if (!captureStdOut) {
            // Output as normal
            return true
        }

        // Put onto GUI
        if (EventQueue.isDispatchThread()) {
            appendOutput(str, outputStyle)
        }
        else {
            SwingUtilities.invokeLater {
                appendOutput(str, outputStyle)
            }
        }
        return false
    }

    // actually run the
    void runScript(EventObject evt = null) {
        def endLine = System.getProperty('line.separator')
        def record = new HistoryRecord( allText: inputArea.getText().replaceAll(endLine, '\n'),
            selectionStart: textSelectionStart, selectionEnd: textSelectionEnd)
        addToHistory(record)

        // Print the input text
        for (line in record.textToRun.tokenize("\n")) {
            appendOutputNl('groovy> ', promptStyle)
            appendOutput(line, commandStyle)
        }

        //appendOutputNl("") - with wrong number of args, causes StackOverFlowError;
        appendOutputNl("\n", promptStyle)

        // Kick off a new thread to do the evaluation
        statusLabel.text = 'Running Script...'

        // Run in separate thread, so that System.out can be captured
        runThread = Thread.start {
            try {
                SwingUtilities.invokeLater { showRunWaitDialog() }
                String name = "Script${scriptNameCounter++}"
                if(beforeExecution) {
                    beforeExecution()
                }
                def result = shell.evaluate(record.textToRun, name);
                if(afterExecution) {
                    afterExecution()
                }
                SwingUtilities.invokeLater { finishNormal(result) }
            } catch (Throwable t) {
                SwingUtilities.invokeLater { finishException(t) }
            } finally {
                SwingUtilities.invokeLater {
                    runWaitDialog.hide();
                    runThread = null
                }
            }
        }
    }

    def selectFilename(name = "Open") {
        def fc = new JFileChooser()
        fc.fileSelectionMode = JFileChooser.FILES_ONLY
        fc.acceptAllFileFilterUsed = true
        if (fc.showDialog(frame, name) == JFileChooser.APPROVE_OPTION) {
            return fc.selectedFile
        } else {
            return null
        }
    }

    void setDirty(boolean newDirty) {
        dirty = newDirty
        updateTitle()
    }

    private void setInputTextFromHistory() {
        if (historyIndex < history.size()) {
            def record = history[historyIndex]
            inputArea.text = record.allText
            inputArea.selectionStart = record.selectionStart
            inputArea.selectionEnd = record.selectionEnd
            setDirty(true) // Should calculate dirty flag properly (hash last saved/read text in each file)
            statusLabel.text = "command history ${history.size() - historyIndex}"
        } else {
            inputArea.text = ""
            statusLabel.text = 'at end of history'
        }
    }

    // Adds a variable to the binding
    // Useful for adding variables before openning the console
    void setVariable(String name, Object value) {
        shell.context.setVariable(name, value)
    }

    void showAbout(EventObject evt = null) {
        def version = InvokerHelper.getVersion()
        def pane = swing.optionPane()
         // work around GROOVY-1048
        pane.setMessage('Welcome to the Groovy Console for evaluating Groovy scripts\nVersion ' + version)
        def dialog = pane.createDialog(frame, 'About GroovyConsole')
        dialog.show()
    }

    void find(EventObject evt = null) {
        FindReplaceUtility.showDialog()
    }

    void findNext(EventObject evt = null) {
        FindReplaceUtility.FIND_ACTION.actionPerformed(evt)
    }

    void findPrevious(EventObject evt = null) {
        def reverseEvt = new ActionEvent(
            evt.getSource(), evt.getID(),
            evt.getActionCommand(), evt.getWhen(),
            ActionEvent.SHIFT_MASK) //reverse
        FindReplaceUtility.FIND_ACTION.actionPerformed(reverseEvt)
    }

    void replace(EventObject evt = null) {
        FindReplaceUtility.showDialog(true)
    }


    // Shows the 'wait' dialog
    void showRunWaitDialog() {
        runWaitDialog.pack()
        int x = frame.x + (frame.width - runWaitDialog.width) / 2
        int y = frame.y + (frame.height - runWaitDialog.height) / 2
        runWaitDialog.setLocation(x, y)
        runWaitDialog.show()
    }

    void smallerFont(EventObject evt = null){
        if (inputArea.font.size < 5) return
        def newFont = new Font('Monospaced', Font.PLAIN, inputArea.font.size - 2)
        inputArea.font = newFont
        outputArea.font = newFont
    }

    void updateTitle() {
        if (scriptFile != null) {
            frame.title = scriptFile.name + (dirty?" * ":"") + " - GroovyConsole"
        } else {
            frame.title = "GroovyConsole"
        }
    }

    void invokeTextAction(evt, closure) {
        def source = evt.getSource()
        if (source != null) {
            closure(inputArea)
        }
    }

    void cut(EventObject evt = null) {
        invokeTextAction(evt, { source -> source.cut() })
    }

    void copy(EventObject evt = null) {
        invokeTextAction(evt, { source -> source.copy() })
    }

    void paste(EventObject evt = null) {
        invokeTextAction(evt, { source -> source.paste() })
    }

    void selectAll(EventObject evt = null) {
        invokeTextAction(evt, { source -> source.selectAll() })
    }

    void setRowNumAndColNum() {
        cursorPos = inputArea.getCaretPosition()
        rowNum = rootElement.getElementIndex(cursorPos) + 1

        def rowElement = rootElement.getElement(rowNum - 1)
        colNum = cursorPos - rowElement.getStartOffset() + 1

        swing.rowNumAndColNum.setText("$rowNum:$colNum")
    }
}

/** A single time when the user selected "run" */
class HistoryRecord {
    def allText
    def selectionStart
    def selectionEnd
    def scriptName
    def result
    def exception

    def getTextToRun() {
        if (selectionStart != selectionEnd) {
            return allText[selectionStart ..< selectionEnd]
        }
        return allText
    }

    def getValue() {
        return exception ? exception : result
    }
}
