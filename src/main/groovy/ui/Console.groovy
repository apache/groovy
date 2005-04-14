package groovy.ui

import groovy.swing.SwingBuilder

import java.awt.Toolkit
import java.awt.Insets
import java.awt.Color
import java.awt.Font
import java.awt.event.KeyEvent
import java.util.EventObject

import javax.swing.event.CaretListener
import javax.swing.event.CaretEvent
import javax.swing.KeyStroke
import javax.swing.JSplitPane
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import javax.swing.text.StyleContext
import javax.swing.text.BadLocationException;

import org.codehaus.groovy.runtime.InvokerHelper

/**
 * Groovy Swing console.
 *
 * @author Danno Ferrin
 */
class Console extends ConsoleSupport implements CaretListener {

    def frame
    def swing
    def textArea
    def outputArea
    def scriptList
    def scriptFile
    private boolean dirty
    private int textSelectionStart  // keep track of selections in textArea
    private int textSelectionEnd

    static void main(args) {
        def console = new Console()
        console.run()
    }

    void run() {
        scriptList = []
        // if menu modifier is two keys we are out of luck as the javadocs
        // indicates it returns "Control+Shift" instead of "Control Shift"
        def menuModifier = KeyEvent.getKeyModifiersText(
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()).toLowerCase() + ' '

        def swing = new SwingBuilder()
        def frame = swing.frame(
            title:'GroovyConsole',
            location:[100,100],
            size:[500,400],
            defaultCloseOperation:javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE) {
            def newAction = action(
                name:'New', closure: this.&fileNew, mnemonic: 'N', accelerator: menuModifier + 'N'
            )
            def openAction = action(
                name:'Open', closure: this.&fileOpen, mnemonic: 'O', accelerator: menuModifier + 'O'
            )
            def saveAction = action(
                name:'Save', closure: this.&fileSave, mnemonic: 'S', accelerator: menuModifier + 'S'
            )
            def exitAction = action(
                name:'Exit', closure: this.&exit, mnemonic: 'x'
            )
            def runAction = action(
                name:'Run', closure: this.&runScript, mnemonic: 'R', keyStroke: 'ctrl ENTER',
                accelerator: 'ctrl R'
            )
            def aboutAction = action(name:'About', closure: this.&showAbout, mnemonic: 'A')
            menuBar {
                menu(text:'File', mnemonic:0x46) {
                    menuItem() { action(newAction) }
                    menuItem() { action(openAction) }
                    separator()
                    menuItem() { action(saveAction) }
                    separator()
                    menuItem() { action(exitAction) }
                }
                menu(text:'Actions', mnemonic: 'A') {
                    menuItem() { action(runAction) }
                }
                menu(text:'Help', mnemonic: 'H') {
                    menuItem() { action(aboutAction) }
                }
            }
            splitPane(orientation:JSplitPane.VERTICAL_SPLIT, resizeWeight:0.50F) {
                scrollPane {
                    textArea = textArea(
                        margin: new Insets(3,3,3,3), font: new Font('Monospaced',Font.PLAIN,12)
                    ) { action(runAction) }
                }
                scrollPane {
                    outputArea = textPane(editable:false, background: new Color(255,255,218))
                    addStylesToDocument(outputArea)
                }
            }
        }   // end of SwingBuilder use

        // add listeners
        frame.windowClosing = this.&exit
        textArea.addCaretListener(this)
        textArea.document.undoableEditHappened = { setDirty(true) }

        frame.show()
        SwingUtilities.invokeLater({textArea.requestFocus()});
    }

    void caretUpdate(CaretEvent e){
        textSelectionStart = Math.min(e.dot,e.mark)
        textSelectionEnd = Math.max(e.dot,e.mark)
    }

    void setDirty(boolean newDirty) {
        dirty = newDirty
        updateTitle()
    }

    void updateTitle() {
        if (scriptFile != null) {
            frame.title = scriptFile.name + (dirty?" * ":"") + " - GroovyConsole"
        } else {
            frame.title = "GroovyConsole"
        }
    }

    def selectFilename(name = "Open") {
        def fc = new JFileChooser()
        fc.fileSelectionMode = JFileChooser.FILES_ONLY
        if (fc.showDialog(frame, name) == JFileChooser.APPROVE_OPTION) {
            return fc.selectedFile
        } else {
            return null
        }
    }

    void fileNew(EventObject evt = null) {
      (new Console()).run()
    }

    void fileOpen(EventObject evt = null) {
        scriptFile = selectFilename();
        if (scriptFile != null) {
            textArea.text = scriptFile.readLines().join('\n');
            setDirty(false)
            textArea.caretPosition = 0
        }
    }

    boolean fileSave(EventObject evt = null) {
        if (scriptFile == null) {
            scriptFile = selectFilename("Save");
        }
        if (scriptFile != null) {
            scriptFile.write(textArea.text)
            setDirty(false);
            return true
        } else {
            return false
        }
    }

    void append(doc, text, style){
        doc.insertString(doc.getLength(), text, style)
    }
    
    void runScript(EventObject evt = null) {
        def text = textArea.getText()
        if (textSelectionStart != textSelectionEnd) {   // we have a real selection
            text = textArea.getText()[textSelectionStart...textSelectionEnd]
        }
        scriptList.add(text)

        def doc = outputArea.getStyledDocument();

        for (line in text.tokenize("\n")) {
            if (doc.length > 0) { append(doc,  "\n", promptStyle)}
            append(doc, 'groovy> ', promptStyle)
            append(doc, line, commandStyle)
        }

        def answer = evaluate(text)
        def output = "\n" + InvokerHelper.inspect(answer)

        append(doc, output, outputStyle)

        println("Variables: " + shell.context.variables)

        if (scriptFile == null) {
            textArea.text = null
        }
    }

    protected void handleException(String text, Exception e) {
        def pane = swing.optionPane(message:'Error: ' + e + '\n' + e.getMessage() + '\nafter compiling: ' + text)
        def dialog = pane.createDialog(frame, 'Compile error')
        dialog.show()
    }

    void showAbout(EventObject evt = null) {
        def version = InvokerHelper.getVersion()
        def pane = swing.optionPane(message:'Welcome to the Groovy Console for evaluating Groovy scripts\nVersion ' + version)
        def dialog = pane.createDialog(frame, 'About GroovyConsole')
        dialog.show()
    }

    void exit(EventObject evt = null) {
        if (scriptFile != null && dirty) {
            switch (JOptionPane.showConfirmDialog(frame,
                "Save changes to " + scriptFile.name + "?",
                "GroovyConsole", JOptionPane.YES_NO_CANCEL_OPTION))
            {
                case JOptionPane.YES_OPTION:
                    if (!fileSave())
                        break
                case JOptionPane.NO_OPTION:
                    frame.hide()
                    frame.dispose()
            }
        } else {
            frame.hide()
            frame.dispose()
        }
    }

}
