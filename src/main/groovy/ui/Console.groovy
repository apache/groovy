package groovy.ui

import groovy.swing.SwingBuilder
import groovy.inspect.swingui.ObjectBrowser

import java.awt.BorderLayout
import java.awt.Toolkit
import java.awt.Insets
import java.awt.Color
import java.awt.Font
import java.awt.event.KeyEvent
import java.util.EventObject

import javax.swing.*
import javax.swing.text.*
import javax.swing.event.*

import org.codehaus.groovy.runtime.InvokerHelper

/**
 * Groovy Swing console.
 *
 * @author Danno Ferrin
 * @author Dierk Koenig, changed Layout, included Selection sensitivity, included ObjectBrowser
 */
class Console extends ConsoleSupport implements CaretListener {

	// TODO: make this configurable
	private static final int MAX_HISTORY = 10;

    def frame
    def swing
    def inputArea
    def outputArea
    def scriptList
    def scriptFile
    def lastResult    
    private JLabel statusLabel
    private List history = []
    private int historyIndex = 1 // valid values are 0..history.length()
    private boolean dirty
    private int textSelectionStart  // keep track of selections in inputArea
    private int textSelectionEnd

    static void main(args) {
        def console = new Console()
        console.run()
    }

    void run() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
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
                name:'New', closure: this.&fileNew, mnemonic: 'N'
            )
            def openAction = action(
                name:'Open', closure: this.&fileOpen, mnemonic: 'O', accelerator: menuModifier + 'O'
            )
            def saveAction = action(
                name:'Save', closure: this.&fileSave, mnemonic: 'S', accelerator: menuModifier + 'S'
            )
            def exitAction = action(
                name:'Exit', closure: this.&exit, mnemonic: 'x', accelerator: 'alt F4'
            )	            
            def historyPrevAction = action(
                name:'Previous', closure: this.&historyPrev, mnemonic: 'P', accelerator: 'ctrl P'
            )            
            def historyNextAction = action(
            	name: 'Next', closure: this.&historyNext, mnemonic: 'N', accelerator: 'ctrl N'
            )            
            def runAction = action(
                name:'Run', closure: this.&runScript, mnemonic: 'R', keyStroke: 'ctrl ENTER',
                accelerator: 'ctrl R'
            )
            def inspectAction = action(
                name:'Inspect', closure: this.&inspect, mnemonic: 'I', keyStroke: 'ctrl I',
                accelerator: 'ctrl I'
            )
            def largerFontAction = action(
                name:'Larger Font', closure: this.&largerFont, mnemonic: 'L', keyStroke: 'ctrl L',
                accelerator: 'ctrl L'
            )
            def smallerFontAction = action(
                name:'Smaller Font', closure: this.&smallerFont, mnemonic: 'S', keyStroke: 'ctrl S',
                accelerator: 'ctrl S'
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
                menu(text:'Edit', mnemonic: 'E') {
                	menuItem() { action(historyNextAction) }
                	menuItem() { action(historyPrevAction) }
                	// Add copy/cut/paste here
                }
                menu(text:'Actions', mnemonic: 'A') {
                    menuItem() { action(runAction) }
                    menuItem() { action(inspectAction) }
                    menuItem() { action(largerFontAction) }
                    menuItem() { action(smallerFontAction) }
                }
                menu(text:'Help', mnemonic: 'H') {
                    menuItem() { action(aboutAction) }
                }
            }
            
            borderLayout()
            
            splitPane(id:'splitPane', resizeWeight:0.50F, 
            	orientation:JSplitPane.VERTICAL_SPLIT, constraints: BorderLayout.CENTER) 
            {
                scrollPane {
                    inputArea = textArea(
                        margin: new Insets(3,3,3,3), font: new Font('Monospaced',Font.PLAIN,12)
                    ) { action(runAction) }
                }
                scrollPane {
                    outputArea = textPane(editable:false, background: new Color(255,255,218))
                    addStylesToDocument(outputArea)
                }
            }
            
            statusLabel = label(id:'status', text: 'Welcome to the Groovy.', constraints: BorderLayout.SOUTH,
            	border: BorderFactory.createLoweredBevelBorder())
        }   // end of SwingBuilder use

        // add listeners
        frame.windowClosing = this.&exit
        inputArea.addCaretListener(this)
        inputArea.document.undoableEditHappened = { setDirty(true) }

        frame.show()
        SwingUtilities.invokeLater({inputArea.requestFocus()});
    }
    
    void addToHistory(record) {
    	history.add(record)
    	// history.size here just retrieves method closure
    	if (history.size() > MAX_HISTORY) {
    		history.remove(0)
    	}
    	// history.size doesn't work here either
    	historyIndex = history.size()
    }

	// Append a string to the output area
    void appendOutput(text, style){
    	def doc = outputArea.getStyledDocument();
        doc.insertString(doc.getLength(), text, style)
    }

	// Append a string to the output area on a new line
    void appendOutputNl(text, style){
    	def doc = outputArea.getStyledDocument();
    	if (doc.getLength() != 0) {
    		doc.insertString(doc.getLength(), "\n", style);
    	}
        doc.insertString(doc.getLength(), text, style)
    }
    
    private static void beep() {
    	Toolkit.defaultToolkit.beep()
    }

    void caretUpdate(CaretEvent e){
        textSelectionStart = Math.min(e.dot,e.mark)
        textSelectionEnd = Math.max(e.dot,e.mark)
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

    protected void handleException(String text, Exception e) {
        def pane = swing.optionPane()
         // work around GROOVY-1048
        pane.setMessage('Error: ' + e + '\n' + e.getMessage() + '\nafter compiling: ' + text)
        def dialog = pane.createDialog(frame, 'Compile error')
        dialog.show()
    }

    void fileNew(EventObject evt = null) {
      (new Console()).run()
    }

    void fileOpen(EventObject evt = null) {
        scriptFile = selectFilename();
        if (scriptFile != null) {
            inputArea.text = scriptFile.readLines().join('\n');
            setDirty(false)
            inputArea.caretPosition = 0
        }
    }

    boolean fileSave(EventObject evt = null) {
        if (scriptFile == null) {
            scriptFile = selectFilename("Save");
        }
        if (scriptFile != null) {
            scriptFile.write(inputArea.text)
            setDirty(false);
            return true
        } else {
            return false
        }
    }

    void inspect(EventObject evt = null){
        if (null == lastResult) return
        ObjectBrowser.inspect(lastResult)
    }
    
    void historyNext(EventObject evt = null) {
    	if (historyIndex < history.size()) {
    		historyIndex++;
    		setInputTextFromHistory()
    	} else {
    		setStatusText("Can't go past end of history (time travel not allowed)")
    		beep()
    	}
    }

    void historyPrev(EventObject evt = null) {
    	if (historyIndex > 0) {
    		historyIndex--;
    		setInputTextFromHistory()
    	} else {
    		setStatusText("Can't go past start of history")
    		beep()
    	}
    }

    void largerFont(EventObject evt = null) {
        if (inputArea.font.size > 40) return
        def newFont = new Font('Monospaced', Font.PLAIN, inputArea.font.size + 2)
        inputArea.font = newFont
        outputArea.font = newFont
    }
    
    void runScript(EventObject evt = null) {
    	def record = new RunText( allText: inputArea.getText(),
    		selectionStart: textSelectionStart, selectionEnd: textSelectionEnd,
    		scriptFile: scriptFile)
    		
    	addToHistory(record)
    	
    	// Always separate from previous output with a line break
        for (line in record.textToRun.tokenize("\n")) {
            appendOutputNl('groovy> ', promptStyle)
            appendOutput(line, commandStyle)
        }

        lastResult = evaluate(record.textToRun)
        appendOutputNl("${InvokerHelper.inspect(lastResult)}\n", outputStyle);
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

    void setDirty(boolean newDirty) {
        dirty = newDirty
        updateTitle()
    }
    
    private void setInputTextFromHistory() {
		if (historyIndex < history.size()) {
			def runText = history[historyIndex]
			inputArea.text = runText.allText
			inputArea.selectionStart = runText.selectionStart
			inputArea.selectionEnd = runText.selectionEnd
			setDirty(true) // Should calculate dirty flag properly (hash last saved/read text in each file)
			setStatusText("command history ${history.size() - historyIndex}")
		} else {
			inputArea.text = ""
			setStatusText('at end of history')
		}    	
    }
    
    void setStatusText(String text) {
    	statusLabel.text = text
    }

    void showAbout(EventObject evt = null) {
        def version = InvokerHelper.getVersion()
        def pane = swing.optionPane()
         // work around GROOVY-1048
        pane.setMessage('Welcome to the Groovy Console for evaluating Groovy scripts\nVersion ' + version)
        def dialog = pane.createDialog(frame, 'About GroovyConsole')
        dialog.show()
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
}

/** A snapshot of the code area, when the user selected "run" */
class RunText {
	@Property allText
	@Property selectionStart
	@Property selectionEnd
	@Property scriptName
	
	def getTextToRun() {
        if (selectionStart != selectionEnd) {   
            return allText[selectionStart ..< selectionEnd]
        }
        return allText
	}
}