package groovy.swing

import java.awt.BorderLayout
import javax.swing.BorderFactory
import groovy.swing.SwingBuilder

/**
 * Demonstrates the use of the table layout
 */
class TableLayoutDemo {
    
    def frame
    def swing
    
    void run() {
        swing = new SwingBuilder()
        
        frame = swing.frame(title:'TableLayout Demo', location:[200,200], size:[300,200]) {
            menuBar {
                menu(text:'Help') {
                    menuItem() {
                        action(name:'About', closure:{ showAbout() })
                    }
                }
            }
            tableLayout {
                tr {
                    td {
                        label(text:'name')
                    }
                    td(colfill:true) {
                        textField(text:'James')
                    }
                }
                tr {
                    td {
                        label(text:'location')
                    }
                    td(colfill:true) {
                        textField(text:'London')
                    }
                }
                tr {
                    td(colspan:2, align:'center') {
                        button(text:'OK')
                    }
                }
            }
        }
        frame.show()
    }
    
    void showAbout() {
         def pane = swing.optionPane(message:'This demo shows how you can use HTML style table layouts with Swing components')
         def dialog = pane.createDialog(frame, 'About TableLayout Demo')
         dialog.show()
    }
}
