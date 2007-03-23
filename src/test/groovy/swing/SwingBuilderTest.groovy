package groovy.swing

import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.FlowLayout
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.event.KeyEvent
import java.awt.event.InputEvent

import java.text.SimpleDateFormat
import javax.swing.text.*
import javax.swing.*

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

    void testSetMnemonic() {
        def swing = new SwingBuilder()
        swing.panel(layout:new BorderLayout()) {
            label(id:'label0', text:'Name0', mnemonic:48)
            label(id:'label1', text:'Name1', mnemonic:'N')
        }
        int expected0 = '0'
        int expected1 = 'N'
        assert swing.label0.displayedMnemonic == expected0
        assert swing.label1.displayedMnemonic == expected1
        swing.menuItem() {
            action(id:'actionId', name:'About', mnemonic:'A')
        }
        int expected2 = 'A'
        assert swing.actionId.getValue(Action.MNEMONIC_KEY) == expected2
    }

    void testBuilderProperties() {
        def swing = new SwingBuilder()
        assert swing.class.name == 'groovy.swing.SwingBuilder'
    }

    void testFormattedTextField() {
        def swing = new SwingBuilder()
        def dummy = new Date()
        def field = swing.formattedTextField(value:dummy)
        assert field.value == dummy
        assert field.formatter.class == DateFormatter.class
        def dummyFormatter = new SimpleDateFormat()
        field = swing.formattedTextField(format:dummyFormatter)
        assert field.formatter.class == DateFormatter.class
        field = swing.formattedTextField()
        field.value = 3
        assert field.formatter.class == NumberFormatter.class
    }

    void testTabbedPane() {
        def swing = new SwingBuilder()
        swing.tabbedPane{
            button()
        }
    }

    void testComboBox() {
        def swing = new SwingBuilder()
        Object[] objects = ['a','b']
        def list = ['c', 'd', 'e']
        def vector = new Vector(['f', 'g', 'h', 'i'])
        assert swing.comboBox(items:objects).itemCount == 2
        assert swing.comboBox(items:list).itemCount == 3
        assert swing.comboBox(items:vector).itemCount == 4
    }

    void testMisplacedActionsAreIgnored() {
        def swing = new SwingBuilder()
        // labels don't support actions; should be ignored
        swing.label{
            action(id:'actionId', name:'About', mnemonic:'A')
        }
    }

    void testKeystrokesWithinActions() {
        def swing = new SwingBuilder()
        swing.panel{
            button(id:'buttonId'){
                action(id:'action1', keyStroke:'ctrl W')
                action(id:'action2',
                    keyStroke:KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.ALT_MASK))
            }
        }
        def component = swing.buttonId
        def expected1 = swing.action1.toString()
        def expected2 = swing.action2.toString()
        def keys = component.actionMap.allKeys().toList()
        assert keys.contains(expected1)
        assert keys.contains(expected2)
        def inputMap = component.inputMap
        def values = inputMap.allKeys().toList().collect{ inputMap.get(it) }
        assert values.contains(expected1)
        assert values.contains(expected2)
    }

    void testSetAccelerator() {
        def swing = new SwingBuilder()
        def help = swing.action(accelerator:'F1')
        def about = swing.action(accelerator:KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK))
        assert help.getValue(Action.ACCELERATOR_KEY).toString().contains('F1')
        def aboutStr = about.getValue(Action.ACCELERATOR_KEY).toString()
        println 'aboutStr=' + aboutStr
        //assert aboutStr.contains('ctrl')
        //assert aboutStr.contains('SPACE')
    }

    void testConstraints() {
        def swing = new SwingBuilder()
        swing.internalFrame(id:'frameId', layout:new BorderLayout(),
                border:BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder())) {
            vbox(id:'vboxId', constraints:BorderLayout.NORTH)
            hbox(id:'hboxId', constraints:BorderLayout.WEST)
            scrollPane(id:'scrollId', constraints:BorderLayout.CENTER,
                border:BorderFactory.createRaisedBevelBorder())
        }
        assert swing.vboxId.parent == swing.frameId.contentPane
        assert swing.hboxId.parent == swing.frameId.contentPane
        assert swing.scrollId.parent == swing.frameId.contentPane
    }

    void testClosureColumn() {
        def swing = new SwingBuilder()
        def msg = shouldFail{
            swing.closureColumn()
        }
        println  msg
        assert msg.contains('closureColumn must be a child of a tableModel')
        //swing.tableModel()
        msg = shouldFail{
            swing.table{
                tableModel(){
                    closureColumn()
                }
            }
        }
        assert msg.contains("Must specify 'read' Closure property for a closureColumn")
    }

    void testSetConstraints() {
        def swing = new SwingBuilder()
        swing.panel(layout:new BorderLayout()) {
            label(text:'Name', constraints:BorderLayout.CENTER)
        }
    }

    void testSetToolTipText() {
        def swing = new SwingBuilder()
        swing.panel(layout:new BorderLayout()) {
            label(id:'labelId', text:'Name', toolTipText:'This is the name field')
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