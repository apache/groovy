package groovy.ui

import groovy.swing.SwingBuilder

import java.awt.Toolkit
import java.awt.event.KeyEvent

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

    void setDirty(boolean newDirty) {
        dirty = newDirty
        updateTitle()
    }

    static void main(args) {
        console = new Console()
        console.run()
    }

    void run() {
        scriptList = []
        swing = new SwingBuilder()

        menuModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
        frame = swing.frame(title:'GroovyConsole', location:[100,100], size:[800,400], defaultCloseOperation:javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE) {
            menuBar {
                menu(text:'File', mnemonic:0x46) {
                    menuItem() {
                        action(
                            name:'New',
                            closure:{ fileNew() },
                            mnemonicKey:0x4e,
                            acceleratorKey:KeyStroke.getKeyStroke(KeyEvent.VK_N, menuModifier)
                        )
                    }
                    menuItem() {
                        action(
                            name:'Open',
                            closure:{ fileOpen() },
                            mnemonicKey:0x4f,
                            acceleratorKey:KeyStroke.getKeyStroke(KeyEvent.VK_O, menuModifier)
                        )
                    }
                    separator()
                    menuItem() {
                        action(
                            name:'Save',
                            closure:{ fileSave() },
                            mnemonicKey:0x53,
                            acceleratorKey:KeyStroke.getKeyStroke(KeyEvent.VK_S, menuModifier)
                        )
                    }
                    separator()
                    menuItem() {
                        action(name:'Exit', closure:{ exit() }, mnemonicKey:0x58)
                    }
                }
                menu(text:'Actions', mnemonic:0x41) {
                    menuItem() {
                        action(
                            name:'Run',
                            closure:{ runScript() },
                            mnemonicKey:0x52,
                            keyStroke:'ctrl enter',
                            acceleratorKey:KeyStroke.getKeyStroke(KeyEvent.VK_R, menuModifier)
                        )
                    }
                }
                menu(text:'Help', mnemonic:0x48) {
                    menuItem() {
                        action(name:'About', closure:{ showAbout() }, mnemonic:0x41)
                    }
                }
            }
            splitPane(orientation:JSplitPane.VERTICAL_SPLIT, resizeWeight:0.50F) {
                scrollPane {
                    outputArea = textPane(editable:false)
                    addStylesToDocument(outputArea)
                }
                scrollPane {
                    textArea = textArea()
                }
            }
        }
        frame.setSize(500,400)

        // add listeners
        frame.windowClosing = { exit() }
        textArea.document.undoableEditHappened = { setDirty(true) }

        frame.show()
        SwingUtilities.invokeLater({textArea.requestFocus()});
    }

    updateTitle() {
        if (scriptFile != null) {
            frame.title = scriptFile.name + (dirty?" * ":"") + " - GroovyConsole"
        } else {
            frame.title = "GroovyConsole"
        }
    }

    runScript() {
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

    showAbout() {
        version = InvokerHelper.getVersion()
        pane = swing.optionPane(message:'Welcome to the Groovy Console for evaluating Groovy scripts\nVersion ' + version)
        dialog = pane.createDialog(frame, 'About GroovyConsole')
        dialog.show()
    }

    protected void handleException(String text, Exception e) {
        pane = swing.optionPane(message:'Error: ' + e.getMessage() + '\nafter compiling: ' + text)
        dialog = pane.createDialog(frame, 'Compile error')
        dialog.show()
    }

    exit() {
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

    fileNew() {
      (new Console()).run()
    }

    fileOpen() {
        scriptFile = selectFilename();
        if (scriptFile != null) {
            textArea.text = scriptFile.readLines().join('\n');
            setDirty(false)
            textArea.caretPosition = 0
        }
    }

    boolean fileSave() {
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


    selectFilename(name = "Open") {
        fc = new JFileChooser()
        fc.fileSelectionMode = JFileChooser.FILES_ONLY
        if (fc.showDialog(frame, name) == JFileChooser.APPROVE_OPTION) {
            return fc.selectedFile
        } else {
            return null
        }
    }
}
