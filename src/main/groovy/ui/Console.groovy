package groovy.ui

import groovy.swing.SwingBuilder
import javax.swing.KeyStroke
import javax.swing.JSplitPane
import org.codehaus.groovy.runtime.InvokerHelper

class Console {

    property frame
	property swing
    property textArea
    property outputArea
    property historyArea
    property shell
    property counter
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
                        action(name:'Run', closure:{ runScript() }, accelerator_key:KeyStroke.getKeyStroke('meta r'))
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
	                owner.historyArea = textArea(editable:false)
                }
                splitPane(orientation:JSplitPane.VERTICAL_SPLIT) {
                    scrollPane {
                        owner.textArea = textArea()
                    }
                    scrollPane {
                        owner.outputArea = textArea(editable:false)
                    }
                }
            }
        }        
        frame.setSize(500,400)
        frame.show()
    }
    
    showAbout() {
        pane = swing.optionPane(message:'Welcome to the Groovy Console for evaluating Groovy scripts')
        dialog = pane.createDialog(frame, 'About GroovyConsole')
        dialog.show()
    }
    
    runScript() {
        text = textArea.getText()
        scriptList.add(text)
        historyArea.setText( historyArea.getText() + "\n" + text )
        
        if (shell == null) {
        	shell = new GroovyShell()
        }
        if (counter == null) {
            counter = 1
        }
        else {
        	counter = counter + 1
        }
        name = "Script" + counter
        
        Object answer = shell.evaluate(text, name)

        outputArea.setText(InvokerHelper.toString(answer))
        
        println("Variables: " + shell.context.variables)
        
        textArea.setText("")
    }
}