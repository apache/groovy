package groovy.ui

import groovy.swing.SwingBuilder
import javax.swing.KeyStroke

class Console {

    property frame
	property swing
	property textArea
    property shell
    property counter

	static void main(args) {
        /** @todo bug in new 
        console = new Console()
         * 
         */
        console = Console.newInstance()
        
        console.run()
	}

	void run() {
        swing = new SwingBuilder()

        frame = swing.frame(title:'GroovyConsole', location:[100,100], size:[800,400]) {
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
/*            
            splitPane {
*/            
                scrollPane {
                    owner.textArea = textArea()
                }
/*
                scrollPane {
                    tree(model:new MyTableModel())
                }
            }
*/            
        }        
        frame.show()
    }
    
    showAbout() {
        pane = swing.optionPane(message:'Welcome to the Groovy Console for evaluating Groovy scripts')
        dialog = pane.createDialog(frame, 'About GroovyConsole')
        dialog.show()
    }
    
    runScript() {
        text = textArea.getText()
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
        
        println("Running script: " + text)
        
        shell.run(text, name, null)
        
        println("Variables: " + shell.context.variables)
    }
}