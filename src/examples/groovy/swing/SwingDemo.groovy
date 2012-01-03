package groovy.swing

import java.awt.BorderLayout
import javax.swing.BorderFactory
import groovy.model.MvcDemo
import groovy.swing.SwingBuilder

class SwingDemo {

    def swing = new SwingBuilder()
    def frame
    
    static void main(args) {
        def demo = new SwingDemo()
        demo.run()
    }

    void run() {
        def frame = swing.frame(
            title:'This is a Frame',
            location:[100,100],
            size:[800,400],
            defaultCloseOperation:javax.swing.WindowConstants.EXIT_ON_CLOSE) {

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
                panel(border:BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), 'titled border')) {
                    borderLayout()
                    vbox(constraints:NORTH) {
                        panel {
                            borderLayout()
                            label(text:'Name', constraints:WEST, toolTipText:'This is the name field')
                            textField(text:'James', constraints:CENTER, toolTipText:'Enter the name into this field')
                        }
                        panel {
                            borderLayout()
                            label(text:'Location', constraints:WEST, toolTipText:'This is the location field')
                            comboBox(items:['Atlanta', 'London', 'New York'], constraints:CENTER, toolTipText:'Choose the location into this field')
                        }
                        button(text:'Click Me', actionPerformed:{event -> println("closure fired with event: " + event) })
                    }
                    scrollPane(constraints:CENTER, border:BorderFactory.createRaisedBevelBorder()) {
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
    
    void showAbout() {
        // this version doesn't auto-size & position the dialog
        /*
        def dialog = swing.dialog(owner:frame, title:'About GroovySwing') {
            optionPane(message:'Welcome to the wonderful world of GroovySwing')
        }
        */
         def pane = swing.optionPane(message:'Welcome to the wonderful world of GroovySwing')
         def dialog = pane.createDialog(frame, 'About GroovySwing')
         dialog.show()
    }
    
    void showGroovyTableDemo() {
        def demo = new TableDemo()
        demo.run()
    }

    void showMVCDemo() {
        def demo = new MvcDemo()
        demo.run()
    }

    void showTableLayoutDemo() {
        def demo = new TableLayoutDemo()
        demo.run()
    }
}
