package groovy.ui

import groovy.swing.SwingBuilder

import javax.swing.KeyStroke
import javax.swing.JSplitPane
import javax.swing.text.StyleContext

import org.codehaus.groovy.runtime.InvokerHelper

class Console extends ConsoleSupport {

    property frame
	property swing
    property textArea
    property outputArea
    property scriptList
    
	static void main(args) {
        console = new Console()
        console.run()
	}

	void run() {
	    scriptList = []
        swing = new SwingBuilder()

        frame = swing.frame(title:'GroovyConsole') { // location:[100,100],  size:[800,400]) {
            menuBar {
                menu(text:'File') {
                    menuItem() {
                        action(name:'New', closure:{ println("clicked on the new menu item!") })
                    }
                    menuItem() {
                        action(name:'Open', closure:{ println("clicked on the open menu item!") })
                    }
                    separator()
                    menuItem() {
                        action(name:'Save', enabled:false, closure:{ println("clicked on the Save menu item!") })
                    }
                }
                menu(text:'Actions') {
                    menuItem() {
                        action(name:'Run', closure:{ owner.runScript() }, 
                        keyStroke:'ctrl enter',
                        acceleratorKey:KeyStroke.getKeyStroke("meta R")
                        /*, 
,
                        mnemonicKey:"alt R"
                        actionCommandKey:'F2'                        actionCommandKey:KeyStroke.getKeyStroke("F3")
                        */
                        )
                    }
                }
                menu(text:'Help') {
                    menuItem() {
                        action(name:'About', closure:{ showAbout() })
                    }
                }
            }
            splitPane(orientation:JSplitPane.VERTICAL_SPLIT) {
                scrollPane {
                    owner.outputArea = textPane(editable:false)
                    owner.addStylesToDocument(owner.outputArea)
                }
                scrollPane {
                    owner.textArea = textArea()
                }
            }
        }        
        frame.setSize(500,400)
        frame.show()
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

        output = "\n" + InvokerHelper.toString(answer)
        
        doc.insertString(doc.getLength(), output, outputStyle)
        
        println("Variables: " + shell.context.variables)
        
        textArea.setText("")
    }
    
    showAbout() {
        pane = swing.optionPane(message:'Welcome to the Groovy Console for evaluating Groovy scripts')
        dialog = pane.createDialog(frame, 'About GroovyConsole')
        dialog.show()
    }
        
    protected void handleException(String text, Exception e) {
        pane = swing.optionPane(message:'Error: ' + e.getMessage() + '\nafter compiling: ' + text)
        dialog = pane.createDialog(frame, 'Compile error')
        dialog.show()
    }
}