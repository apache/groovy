package groovy.ui

import groovy.swing.SwingBuilder

import javax.swing.KeyStroke
import javax.swing.JSplitPane
import javax.swing.text.StyleContext

import org.codehaus.groovy.runtime.InvokerHelper

class Console {

    property frame
	property swing
    property textArea
    property outputArea
    property shell
    property counter
    property scriptList
    property promptStyle
    property commandStyle
    property outputStyle
    
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
                        action(name:'Run', closure:{ runScript() }, accelerator_key:KeyStroke.getKeyStroke('ctrl enter'))
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
                    owner.addStylesToDocument()
                }
                scrollPane {
                    owner.textArea = textArea()
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
    
    addStylesToDocument() {
        doc = outputArea.getStyledDocument();
        
        def = StyleContext.getDefaultStyleContext().
                          getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");

        promptStyle = doc.addStyle("prompt", regular);
        StyleConstants.setForeground(promptStyle, Color.BLUE);
        
        commandStyle = doc.addStyle("command", regular);
        StyleConstants.setForeground(commandStyle, Color.MAGENTA);

        outputStyle = doc.addStyle("output", regular);
        StyleConstants.setBold(outputStyle, true);
    }
    
    runScript() {
        text = textArea.getText()
        scriptList.add(text)
        
        //try {
            doc = outputArea.getStyledDocument();
            
            for (line in text.tokenize("\n")) {
                doc.insertString(doc.getLength(), "\ngroovy> ", promptStyle)
                doc.insertString(doc.getLength(), line, commandStyle)
            }

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
            answer = shell.evaluate(text, name)
        /*    
        } 
        catch (Exception e) {
            answer = e
        }
        */
        output = "\n" + InvokerHelper.toString(answer)
        
	    println("adding output")

        doc.insertString(doc.getLength(), output, outputStyle)
        
        println("Variables: " + shell.context.variables)
        
        textArea.setText("")
    }
}