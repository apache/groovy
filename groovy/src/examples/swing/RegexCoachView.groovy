import static java.awt.BorderLayout.*
import static javax.swing.JSplitPane.VERTICAL_SPLIT
import static javax.swing.WindowConstants.EXIT_ON_CLOSE

frame(title: 'The Groovy Regex Coach', location: [20, 40], size: [600, 500], defaultCloseOperation: EXIT_ON_CLOSE) {
    panel {
        borderLayout()
        splitPane(orientation: VERTICAL_SPLIT, dividerLocation: 150) {
            panel {
                borderLayout()
                label(constraints: NORTH, text: 'Regular expression:')
                scrollPane(constraints: CENTER) {
                    textPane(id: 'regexPane')
                }
                label(constraints: SOUTH, id: 'regexStatus', text: ' ')
            }
            panel {
                borderLayout()
                label(constraints: NORTH, text: 'Target string:')
                scrollPane(constraints: CENTER) {
                    textPane(id: 'targetPane')
                }
                panel(constraints: SOUTH) {
                    borderLayout()
                    label(constraints: NORTH, id: 'targetStatus', text: ' ')
                    panel(constraints: SOUTH) {
                        flowLayout()
                        button('<<-', id: 'scanLeft')
                        button('->>', id: 'scanRight')
                    }
                }
            }
        }
    }
}