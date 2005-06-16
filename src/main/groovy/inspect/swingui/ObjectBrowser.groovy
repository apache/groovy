package groovy.inspect.swingui

import java.awt.*
import javax.swing.*
import groovy.swing.SwingBuilder
import groovy.inspect.Inspector

/**
A little GUI to show some of the Inspector capabilities.
Starting this script opens the ObjectBrowser on "some String".
Use it in groovysh or groovyConsole to inspect your object of
interest with
<code>
ObjectBrowser.inspect(myObject)
</code>.

@author Dierk Koenig
**/
class ObjectBrowser {
    
    @Property inspector
    def swing, frame, fieldTable, methodTable

    static void main(args) {
        inspect("some String")
    }
    static void inspect(objectUnderInspection){
        def browser = new ObjectBrowser()
        browser.inspector = new Inspector(objectUnderInspection)
        browser.run()
    }
    
    void run() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        swing = new SwingBuilder()
        
        frame = swing.frame(title:'Groovy Object Browser', location:[200,200], size:[800,600],
                defaultCloseOperation:WindowConstants.DISPOSE_ON_CLOSE) {
            menuBar {
                menu(text:'Help') {
                    menuItem() {
                        action(name:'About', closure:{ showAbout() })
                    }
                }
            }
            panel() {
                borderLayout()
                panel(  name:"Class Info",
                        border: BorderFactory.createEmptyBorder(5,10,5,10),
                        constraints:BorderLayout.NORTH) {
                    flowLayout(alignment:FlowLayout.LEFT)
                    def props = inspector.classProps
                    def classLabel = '<html>' + props.join('<br>')
                    label(classLabel)
                }
                tabbedPane(constraints:BorderLayout.CENTER){
                    scrollPane(name:' (Meta) Methods ' ) {
                        methodTable = table() {
                            def data = safeCopy(Inspector.sort(inspector.methods))
                            data.addAll(safeCopy(Inspector.sort(inspector.metaMethods)))

                            tableModel(list:data) {
                                closureColumn(header:'Name',        read:{it[Inspector.MEMBER_NAME_IDX]})
                                closureColumn(header:'Params',      read:{it[Inspector.MEMBER_PARAMS_IDX]})
                                closureColumn(header:'Type',        read:{it[Inspector.MEMBER_TYPE_IDX]})
                                closureColumn(header:'Origin',      read:{it[Inspector.MEMBER_ORIGIN_IDX]})
                                closureColumn(header:'Modifier',    read:{it[Inspector.MEMBER_MODIFIER_IDX]})
                                closureColumn(header:'Declarer',    read:{it[Inspector.MEMBER_DECLARER_IDX]})
                                closureColumn(header:'Exceptions',  read:{it[Inspector.MEMBER_EXCEPTIONS_IDX]})
                            }
                        }
                    }
                    scrollPane(name: ' Public Fields and Properties ') {
                        fieldTable = table() {
                            def data = safeCopy(Inspector.sort(inspector.publicFields))
                            data.addAll(safeCopy(Inspector.sort(inspector.properties)))
                            tableModel(list:data) {
                                closureColumn(header:'Name',        read:{it[Inspector.MEMBER_NAME_IDX]})
                                closureColumn(header:'Value',       read:{it[Inspector.MEMBER_VALUE_IDX]})
                                closureColumn(header:'Type',        read:{it[Inspector.MEMBER_TYPE_IDX]})
                                closureColumn(header:'Origin',      read:{it[Inspector.MEMBER_ORIGIN_IDX]})
                                closureColumn(header:'Modifier',    read:{it[Inspector.MEMBER_MODIFIER_IDX]})
                                closureColumn(header:'Declarer',    read:{it[Inspector.MEMBER_DECLARER_IDX]})
                            }
                        }
                    }
                }
            }
        }
        def fieldSorter = new TableSorter(fieldTable.model)
        fieldTable.model = fieldSorter
        fieldSorter.addMouseListenerToHeaderInTable(fieldTable)

        def methodSorter = new TableSorter(methodTable.model)
        methodTable.model = methodSorter
        methodSorter.addMouseListenerToHeaderInTable(methodTable)
        frame.show()
        frame.toFront()
    }
    
    void showAbout() {
         def pane = swing.optionPane(message:'An interactive GUI to explore object capabilities.')
         def dialog = pane.createDialog(frame, 'About Groovy Object Browser')
         dialog.show()
    }

    // work around bug GROOVY-886-764-888-584
    def safeCopy(objectArrayOfStringArrays){
        def copy = []
        for (i in 0..<objectArrayOfStringArrays.size()){
            def row = []
            for (j in 0..<objectArrayOfStringArrays[i].size()){
                row << objectArrayOfStringArrays[i][j]
            }
            copy << row
        }
        // copy.each(){println it}  // only for logging
        return copy
    }
}
