package groovy.ui

import groovy.swing.SwingBuilder
import groovy.inspect.swingui.ObjectBrowser

import java.awt.BorderLayout
import java.awt.EventQueue
import java.awt.Color
import java.awt.Font
import java.awt.Insets
import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.io.PrintWriter
import java.io.StringWriter
import java.util.EventObject

import javax.swing.*
import javax.swing.text.*
import javax.swing.event.*

import org.codehaus.groovy.runtime.InvokerHelper

/**
 * Groovy Swing console.
 *
 * Allows user to interactively enter and execute Groovy. 
 *
 * @author Danno Ferrin
 * @author Dierk Koenig, changed Layout, included Selection sensitivity, included ObjectBrowser
 * @author Alan Green included history, System.out capture and some smaller features.
 */
class Console extends ConsoleSupport implements CaretListener {

	// Whether or not std output should be captured to the console
	@Property captureStdOut = true

	// Maximum size of history
	@Property int maxHistory = 10
	
	// Maximum number of characters to show on console from std out
	@Property int maxOutputChars = 10000

	// UI
    def frame
    def swing
    def inputArea
    def outputArea
    def JLabel statusLabel

	// Internal history
    def List history = []
    def int historyIndex = 1 // valid values are 0..history.length()

	// Current editor state
    def boolean dirty
    def int textSelectionStart  // keep track of selections in inputArea
    def int textSelectionEnd
    def scriptFile

	// Running scripts
    def int scriptNameCounter = 0
    def systemOutInterceptor
    
    // Modal dialog thrown up when script is running
    def runWaitDialog
    def runThread = null

    static void main(args) {
        def console = new Console()
        console.run()
    }

    void run() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
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
            def clearOutputAction = action(
                name:'Clear Output', closure: this.&clearOutput, mnemonic: 'C', keyStroke: 'ctrl W',
                accelerator: 'ctrl W'
            )
            def runAction = action(
                name:'Run', closure: this.&runScript, mnemonic: 'R', keyStroke: 'ctrl ENTER',
                accelerator: 'ctrl R'
            )
            def inspectLastAction = action(
                name:'Inspect Last', closure: this.&inspectLast, mnemonic: 'I', keyStroke: 'ctrl I',
                accelerator: 'ctrl I'
            )
            def inspectVariablesAction = action(
            	name:'Inspect Variables', closure: this.&inspectVariables, mnemonic: 'V', keyStroke: 'ctrl J',
                accelerator: 'ctrl J'
            )
            def captureStdOutAction = action(
            	name:'Capture Standard Output', closure: this.&captureStdOut, mnemonic: 'C'
            )
            def largerFontAction = action(
                name:'Larger Font', closure: this.&largerFont, mnemonic: 'L', keyStroke: 'alt shift L',
                accelerator: 'alt shift L'
            )
            def smallerFontAction = action(
                name:'Smaller Font', closure: this.&smallerFont, mnemonic: 'S', keyStroke: 'alt shift S',
                accelerator: 'alt shift S'
            )
            def aboutAction = action(name:'About', closure: this.&showAbout, mnemonic: 'A')
            menuBar {
                menu(text:'File', mnemonic: 'F') {
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
                	separator()
                	menuItem() { action(clearOutputAction) }
                }
                menu(text:'Actions', mnemonic: 'A') {
                    menuItem() { action(runAction) }
                    menuItem() { action(inspectLastAction) }
                    menuItem() { action(inspectVariablesAction) }
                    separator()
                    checkBoxMenuItem(selected: captureStdOut) { action(captureStdOutAction) }
                    separator()
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
        }   // end of frame
        
        runWaitDialog = swing.dialog(title: 'Groovy executing', owner: frame, modal: true) {
        	//boxLayout(axis: BoxLayout.Y_AXIS)  // todo mittie: dialog.setLayout -> dialog.contentPane.setLayout()
        	label(text: "Groovy is now executing. Please wait.", 
        		border: BorderFactory.createEmptyBorder(10, 10, 10, 10), alignmentX: 0.5f)
        	button(action: action(name: 'Interrupt', closure: this.&confirmRunInterrupt),
	        	border: BorderFactory.createEmptyBorder(10, 10, 10, 10), alignmentX: 0.5f)
        } // end of runWaitDialog

        // add listeners
        frame.windowClosing = this.&exit
        inputArea.addCaretListener(this)
        inputArea.document.undoableEditHappened = { setDirty(true) }
        
        systemOutInterceptor = new SystemOutputInterceptor(this.&notifySystemOut)
        systemOutInterceptor.start();
        
        bindResults()
        
        frame.show()
        SwingUtilities.invokeLater({inputArea.requestFocus()});
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
        
        // Ensure we don't have too much in console (takes to much memory)
        if (doc.length > maxOutputChars) {
        	doc.remove(0, doc.length - maxOutputChars)
        }
    }

	// Append a string to the output area on a new line
    void appendOutputNl(text, style){
    	if (outputArea.styledDocument.length) {
    		appendOutput("\n", style);
    	}
    	appendOutput(text, style)
    }
    
    private static void beep() {
    	Toolkit.defaultToolkit.beep()
    }
    
    // Binds the "_" and "__" variables in the shell
    void bindResults() {
		shell.setVariable("_", history.empty ? null : lastResult)
		shell.setVariable("__", history.collect {it.result})
    }
    
    // Handles menu event
    void captureStdOut(EventObject evt) {
    	captureStdOut = evt.source.selected
    }

    void caretUpdate(CaretEvent e){
        textSelectionStart = Math.min(e.dot,e.mark)
        textSelectionEnd = Math.max(e.dot,e.mark)
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
    
	static String extractTraceback(Exception e) {
		StringWriter sw = new StringWriter()
		new PrintWriter(sw).withWriter { pw -> e.printStackTrace(pw) }
		return sw.buffer
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
    
    def finishException(Throwable t) {
    	statusLabel.text = 'Execution terminated with exception.'
    	history[-1].exception = t
		appendOutputNl("Exception thrown: $t", promptStyle);
		appendOutputNl(extractTraceback(t), outputStyle);
		bindResults()	
    }
    
    def finishNormal(Object result) {
    	// Take down the wait/cancel dialog
    	statusLabel.text = 'Execution complete.'
    	history[-1].result = result
		appendOutputNl("${InvokerHelper.inspect(lastResult)}", outputStyle);
		bindResults()	
    }
    
    def getLastResult() {
    	if (!history) {
    		return null
    	}
    	return history[-1].value
    }
    
    void handleException(String text, Exception e) {
    	throw new RuntimeException("?");
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
    	def record = new RunText( allText: inputArea.getText(),
    		selectionStart: textSelectionStart, selectionEnd: textSelectionEnd,
    		scriptFile: scriptFile)
    	addToHistory(record)

		// Print the input text    	
        for (line in record.textToRun.tokenize("\n")) {
            appendOutputNl('groovy> ', promptStyle)
            appendOutput(line, commandStyle)
        }
        
        //appendOutputNl("") - with wrong number of args, causes StackOverFlowError;
        appendOutputNl("", outputStyle)
        
        // Kick off a new thread to do the evaluation
        statusLabel.text = 'Running Script...'
        
        // Run in separate thread, so that System.out can be captured
    	runThread = Thread.start {
    		try {
    			SwingUtilities.invokeLater { showRunWaitDialog() }
		        String name = "Script${scriptNameCounter++}"
				def result = shell.evaluate(record.textToRun, name); 
				SwingUtilities.invokeLater { finishNormal(result) }
	    	} catch (Exception e) {
	    		// This assignment required because closure can't see 'e'
	    		def t = e
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
			def runText = history[historyIndex]
			inputArea.text = runText.allText
			inputArea.selectionStart = runText.selectionStart
			inputArea.selectionEnd = runText.selectionEnd
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
}

/** A snapshot of the code area, when the user selected "run" */
class RunText {
	@Property allText
	@Property selectionStart
	@Property selectionEnd
	@Property scriptName
	@Property result
	@Property exception
	
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


