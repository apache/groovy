package groovy.ui

import groovy.swing.SwingBuilder

import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.util.EventObject

import javax.swing.KeyStroke
import javax.swing.JSplitPane
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import javax.swing.text.StyleContext

import org.codehaus.groovy.runtime.InvokerHelper

/**
 * Groovy Swing console.
 *
 * @author Danno Ferrin
 */
class Console extends ConsoleSupport {

    frame
    swing
    textArea
    outputArea
    scriptList
    scriptFile
    private boolean dirty

    static void main(args) {
        console = new Console()
        console.run()
    }

    void run() {
        scriptList = []
        // if menu modifier is two keys we are out of luck as the javadocs
        // incicates it returns "Control+Shift" instead of "Control Shift"
        menuModifier = KeyEvent.getKeyModifiersText( 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
            .toLowerCase() + ' '

        swing = new SwingBuilder()
        frame = swing.frame(
		title:'GroovyConsole', 
		location:[100,100], 
		size:[800,400], 
		defaultCloseOperation:javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE) {
            newAction = action(
                name:'New',
                closure: fileNew,
                mnemonic: 'N',
                accelerator: menuModifier + 'N'
            )
            openAction = action(
                name:'Open',
                closure: fileOpen,
                mnemonic: 'O',
                accelerator: menuModifier + 'O'
            )
            saveAction = action(
                name:'Save',
                closure: fileSave,
                mnemonic: 'S',
                accelerator: menuModifier + 'S'
            )
            exitAction = action(name:'Exit', closure: exit, mnemonic: 'x')
            runAction = action(
                name:'Run',
                closure: runScript,
                mnemonic: 'R',
                keyStroke: 'ctrl ENTER',
                accelerator: 'ctrl R'
            )
            aboutAction = action(name:'About', closure: showAbout, mnemonic: 'A')
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
                    outputArea = textPane(editable:false)
                    addStylesToDocument(outputArea)
                }
                scrollPane {
                    textArea = textArea() { action(runAction) }
                    
                }
            }
        }
        
        frame.setSize(500,400)

        // add listeners
        frame.windowClosing = exit
        textArea.document.undoableEditHappened = { setDirty(true) }

        frame.show()
        SwingUtilities.invokeLater({textArea.requestFocus()});
    }

    void setDirty(boolean newDirty) {
        dirty = newDirty
        updateTitle()
    }

    updateTitle() {
        if (scriptFile != null) {
            frame.title = scriptFile.name + (dirty?" * ":"") + " - GroovyConsole"
        } else {
            frame.title = "GroovyConsole"
        }
    }

    selectFilename(name = "Open") {
        fc = new JFileChooser()
        fc.fileSelectionMode = JFileChooser.FILES_ONLY
        if (fc.showDialog(frame, name) == JFileChooser.APPROVE_OPTION) {
            return fc.selectedFile
        } else {
            return null
        }
    }

    fileNew(EventObject evt = null) {
      (new Console()).run()
    }

    fileOpen(EventObject evt = null) {
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
    
    runScript(EventObject evt = null) {
        text = textArea.getText()
        scriptList.add(text)

        doc = outputArea.getStyledDocument();

        promptStyle = getPromptStyle()
        commandStyle = getCommandStyle()
        outputStyle = getOutputStyle()

        for (line in text.tokenize("\n")) {
            doc.insertString(doc.getLength(), "\ngroovy> ", promptStyle)
            doc.insertString(doc.getLength(), line, commandStyle)
        }

        answer = evaluate(text)

        output = "\n" + InvokerHelper.inspect(answer)

        doc.insertString(doc.getLength(), output, outputStyle)

        println("Variables: " + shell.context.variables)

        if (scriptFile == null) {
            textArea.text = null
        }
    }

    protected void handleException(String text, Exception e) {
        pane = swing.optionPane(message:'Error: ' + e.getMessage() + '\nafter compiling: ' + text)
        dialog = pane.createDialog(frame, 'Compile error')
        dialog.show()
    }

    showAbout(EventObject evt = null) {
        version = InvokerHelper.getVersion()
        pane = swing.optionPane(message:'Welcome to the Groovy Console for evaluating Groovy scripts\nVersion ' + version)
        dialog = pane.createDialog(frame, 'About GroovyConsole')
        dialog.show()
    }

    exit(EventObject evt = null) {
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
