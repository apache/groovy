package groovy.swing

import java.awt.BorderLayout
import javax.swing.BorderFactory
import groovy.model.MvcDemo

class SwingDemo {
    
    property frame
    property swing
    
    void run() {
        swing = new SwingBuilder()

        frame = swing.frame(title:'This is a Frame', location:[100,100], size:[800,400]) {
            menuBar {
		        menu(text:'File') {
                    menuItem() {
                        action(name:'New', closure:{ "clicked on the new menu item!".println() })
                    }
                    menuItem() {
                        action(name:'Open', closure:{ "clicked on the open menu item!".println() })
                    }
                    separator()
                    menuItem() {
                        action(name:'Save', enabled:false, closure:{ "clicked on the Save menu item!".println() })
                    }
		        }
                menu(text:'Demos') {
                    menuItem() {
                        action(name:'Simple TableModel Demo', closure:{ showGroovyTableDemo() })
                    }
                    menuItem() {
                        action(name:'MVC Demo', closure:{ showMVCDemo() })
                    }
                    menuItem() {
                        action(name:'TableLayout Demo', closure:{ showTableLayoutDemo() })
                    }
                }
                menu(text:'Help') {
                    menuItem() {
                        action(name:'About', closure:{ showAbout() })
                    }
                }
		    }
		    splitPane {
                panel(layout:new BorderLayout(), border:BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), 'titled border')) {
    				vbox(constraints:BorderLayout.NORTH) {
                        panel(layout:new BorderLayout()) {
                            label(text:'Name', constraints:BorderLayout.WEST, toolTipText:'This is the name field')
                            textField(text:'James', constraints:BorderLayout.CENTER, toolTipText:'Enter the name into this field')
                        }
                        panel(layout:new BorderLayout()) {
                            label(text:'Location', constraints:BorderLayout.WEST, toolTipText:'This is the location field')
                            comboBox(items:['Atlanta', 'London', 'New York'], constraints:BorderLayout.CENTER, toolTipText:'Choose the location into this field')
                        }
                        button(text:'Click Me', actionPerformed:{event | "closure fired!".println(); event.println() })
                    }
                    scrollPane(constraints:BorderLayout.CENTER, border:BorderFactory.createRaisedBevelBorder()) {
                    	textArea(text:'Some text goes here', toolTipText:'This is a large text area to type in text')
                    }
                }
		        scrollPane {
		            table(model:new MyTableModel())
		        }
		    }
		}        
		frame.show()
    }
    
    showAbout() {
        // this version doesn't auto-size & position the dialog
        /*
        dialog = swing.dialog(owner:frame, title:'About GroovySwing') {
            optionPane(message:'Welcome to the wonderful world of GroovySwing')
        }
		*/ 		
 		pane = swing.optionPane(message:'Welcome to the wonderful world of GroovySwing')
 		dialog = pane.createDialog(frame, 'About GroovySwing')
 		dialog.show()
    }
    
    showGroovyTableDemo() {
        demo = new TableDemo()
        demo.run()
    }

    showMVCDemo() {
        demo = new MvcDemo()
        demo.run()
    }

    showTableLayoutDemo() {
        demo = new TableLayoutDemo()
        demo.run()
    }
}
