package groovy.swing

import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.FlowLayout
import java.awt.GridBagLayout
import java.awt.GridLayout
//import javax.swing.BoxLayout
//import javax.swing.OverlayLayout
import javax.swing.SpringLayout

import javax.swing.JFrame
import javax.swing.JDialog
import javax.swing.JWindow

import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JCheckBoxMenuItem
import javax.swing.JColorChooser
import javax.swing.JComboBox
import javax.swing.JDesktopPane
import javax.swing.JEditorPane;
import javax.swing.JFileChooser
import javax.swing.JFormattedTextField
import javax.swing.JLayeredPane
import javax.swing.JList
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JPopupMenu
import javax.swing.JProgressBar
import javax.swing.JRadioButton
import javax.swing.JRadioButtonMenuItem
import javax.swing.JScrollBar
import javax.swing.JScrollPane
import javax.swing.JSeparator
import javax.swing.JSlider
import javax.swing.JSpinner
import javax.swing.JSplitPane
import javax.swing.JFileChooser
import javax.swing.JInternalFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JTabbedPane
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.JTextPane
import javax.swing.JToggleButton
import javax.swing.JToolBar
import javax.swing.JTree
import javax.swing.JViewport

class SwingBuilderTest extends GroovyTestCase {

    private boolean isHeadless() {
        try {
            new JFrame("testing")
            return false
        } catch (java.awt.HeadlessException he) {
            return true
        }
    }

    void testNamedWidgetCreation() {
        if (isHeadless()) return

        def topLevelWidgets = [
            frame: [JFrame.class, true],
            dialog: [JDialog.class, true],
            window: [JWindow.class, false],
            fileChooser: [JFileChooser.class, false],
            optionPane: [JOptionPane.class, false]
        ]
        def swing = new SwingBuilder()
        topLevelWidgets.each{ name, widgetInfo ->
            if (widgetInfo[1])
                swing."$name"(id:"${name}Id".toString(), title:"This is my $name")
            else
                swing."$name"(id:"${name}Id".toString())
            def widget = swing."${name}Id"
            assert widget.class == widgetInfo[0]
            if (widgetInfo[1]) assert widget.title == "This is my $name"
        }
    }

    void testLayoutCreation() {
        if (isHeadless()) return

        def layouts = [
            borderLayout: BorderLayout.class,
            cardLayout: CardLayout.class,
            flowLayout: FlowLayout.class,
            gridBagLayout: GridBagLayout.class,
            gridLayout: GridLayout.class,
//            overlayLayout: OverlayLayout.class,
            springLayout: SpringLayout.class,
//            boxLayout: BoxLayout.class
        ]
        def swing = new SwingBuilder()
        layouts.each{ name, expectedLayoutClass ->
            def frame = swing.frame(){
               "$name"()
            }
            assert frame.contentPane.layout.class == expectedLayoutClass
        }
    }

    void testWidgetCreation() {
        def widgets = [
            button: JButton.class,
            checkBox: JCheckBox.class,
            checkBoxMenuItem: JCheckBoxMenuItem.class,
            colorChooser: JColorChooser.class,
            comboBox: JComboBox.class,
            desktopPane: JDesktopPane.class,
            editorPane: JEditorPane.class,
            formattedTextField: JFormattedTextField.class,
            internalFrame: JInternalFrame.class,
            label: JLabel.class,
            layeredPane: JLayeredPane.class,
            list: JList.class,
            menu: JMenu.class,
            menuBar: JMenuBar.class,
            menuItem: JMenuItem.class,
            panel: JPanel.class,
            passwordField: JPasswordField.class,
            popupMenu: JPopupMenu.class,
            progressBar: JProgressBar.class,
            radioButton: JRadioButton.class,
            radioButtonMenuItem: JRadioButtonMenuItem.class,
            scrollBar: JScrollBar.class,
            scrollPane: JScrollPane.class,
            separator: JSeparator.class,
            slider: JSlider.class,
            spinner: JSpinner.class,
            splitPane: JSplitPane.class,
            tabbedPane: JTabbedPane.class,
            table: JTable.class,
            textArea: JTextArea.class,
            textPane: JTextPane.class,
            textField: JTextField.class,
            toggleButton: JToggleButton.class,
            toolBar: JToolBar.class,
            tree: JTree.class,
            viewport: JViewport.class,
        ]
        def swing = new SwingBuilder()
        widgets.each{ name, expectedLayoutClass ->
            def frame = swing.frame(){
               "$name"(id:"${name}Id".toString())
            }
            assert swing."${name}Id".class == expectedLayoutClass
        }
    }

    void testSplitPane() {
        def swing = new SwingBuilder()
        def frame = swing.frame(){
            splitPane(id:'hsplit', orientation: JSplitPane.HORIZONTAL_SPLIT) {
                button(id:'left')
                button(id:'right')
            }
            splitPane(id:'vsplit', orientation: JSplitPane.VERTICAL_SPLIT) {
                button(id:'top')
                button(id:'bottom')
            }
        }
        assert swing.hsplit.leftComponent == swing.left
        assert swing.hsplit.rightComponent == swing.right
        assert swing.vsplit.topComponent == swing.top
        assert swing.vsplit.bottomComponent == swing.bottom
    }

    void testNodeCreation() {
        def swing = new SwingBuilder()
        def frame = swing.frame(){
            // 4 valid parameter combinations
            button()
            button('Text')
            button(label:'Label')
            button(label:'Label', 'Text')
        }
        shouldFail(){
            frame = swing.frame(){
                // invalid parameter
                button(new Date())
            }
        }
    }

    void testTableLayout() {
        def swing = new SwingBuilder()
        def msg = shouldFail(RuntimeException){
            def frame = swing.frame(){
                tr()
            }
        }
        assert msg == "'tr' must be within a 'tableLayout'"
        msg = shouldFail(RuntimeException){
            def frame = swing.frame(){
                tableLayout(){
                    td()
                }
            }
        }
        assert msg == "'td' must be within a 'tr'"
    }
}