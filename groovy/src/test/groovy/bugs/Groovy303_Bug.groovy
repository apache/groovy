package groovy.bugs

import java.awt.*
import java.awt.event.*
import javax.swing.*

/**
 * @author Bing Ran
 * @author Andy Dwelly
 * @version $Revision$
 */
class Groovy303_Bug extends GroovySwingTestCase {

    void testBug() {
        if (headless) return
        def scholastic = new Scholastic()
        scholastic.createUI()
    }
}

class Scholastic implements ActionListener {

    void createUI() {
        println('createUI called')
        def frame = new JFrame("Hello World")
        def contents = frame.getContentPane()
        def pane = new JPanel()
        pane.setLayout(new BorderLayout())
        def button = new JButton("A button")
        button.addActionListener(this)
        pane.add(button, BorderLayout.CENTER)
        contents.add(pane)
        frame.setSize(100, 100)
        //frame.setVisible(true)
        button.doClick()
    }

    public void actionPerformed(ActionEvent event) {
        println "hello"
    }
}
