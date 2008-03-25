package groovy.model

import java.awt.BorderLayout
import javax.swing.BorderFactory
import groovy.swing.SwingBuilder
/**
 * 
 */
class MvcDemo {
    
    def frame
    def swing
    
    void run() {
        swing = new SwingBuilder()
        
        def frame = swing.frame(title:'MVC Demo', location:[200,200], size:[300,200]) {
            menuBar {
		        menu(text:'Help') {
		            menuItem() {
		                action(name:'About', closure:{ showAbout() })
		            }
		        }
		    }
		    panel {
                borderLayout()
                scrollPane(constraints:CENTER) {
    	            table() {
    	                tableModel(list:[ ['name':'James', 'location':'London'], ['name':'Bob', 'location':'Atlanta'] ]) {
                            propertyColumn(header:'Name', propertyName:'name')
                            propertyColumn(header:'Location', propertyName:'location')
    	                }
    	            }
    	        }
		    }
		}        
		frame.show()
    }
 
    void showAbout() {
 		def pane = swing.optionPane(message:'This demo shows how you can create UI models from simple MVC models')
 		def dialog = pane.createDialog(frame, 'About MVC Demo')
 		dialog.show()
    }
}
