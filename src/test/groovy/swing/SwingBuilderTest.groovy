package groovy.swing

import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.ComponentOrientation
import java.awt.Dimension
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

    void testButtonGroupOnlyForButtons() {
        def swing = new SwingBuilder()
        def buttonGroup = swing.buttonGroup()
        shouldFail(MissingPropertyException) {
            swing.label(buttonGroup:buttonGroup)
        }
    }

    void testWidget() {
        def swing = new SwingBuilder()
        def label = swing.label()
        swing.widget(label)
    }

    void testTableColumn() {
        // TODO is this required?
        def swing = new SwingBuilder()
        swing.table{
            tableColumn()
        }
    }

    void testSplitPane() {
        def swing = new SwingBuilder()
        def buttonGroup = swing.buttonGroup()
        def frame = swing.frame(){
            splitPane(id:'hsplit', orientation: JSplitPane.HORIZONTAL_SPLIT) {
                button(id:'left', buttonGroup:buttonGroup)
                button(id:'right', buttonGroup:buttonGroup)
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

    void testNestedWindows() {
        def swing = new SwingBuilder()
        swing.window{
            window()
            frame{ window() }
        }
    }

    void testDialogs() {
        def swing = new SwingBuilder()
        swing.dialog()
        swing.frame{ dialog() }
        swing.dialog{ dialog() }
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
        swing.menuBar{
            menu{
                menuItem {
                    action(id:'actionId', name:'About', mnemonic:'A')
                }
            }
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
            action(id:'actionId', Name:'about', mnemonic:'A', closure:{x->x})
            map()
        }
        swing.panel{
            borderLayout{
                // layouts don't support actions, will be ignored
                action(id:'actionId')
            }
        }
    }

    void testBoxLayout() {
        def swing = new SwingBuilder()
        def message = shouldFail{
            swing.boxLayout()
        }
        assert message.contains('Must be nested inside a Container')
        // default is X_AXIS
        swing.panel(id:'panel'){
            boxLayout(id:'layout1')
        }
        // can also set explicit axis
        swing.frame(id:'frame'){
            boxLayout(id:'layout2', axis:BoxLayout.Y_AXIS)
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
        def aboutStr = about.getValue(Action.ACCELERATOR_KEY).toString().toLowerCase()
        assert aboutStr.contains('ctrl')
        assert aboutStr.contains('space')
    }

    void testConstraints() {
        def swing = new SwingBuilder()
        swing.internalFrame(id:'frameId',
                border:BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder())) {
            swing.frameId.contentPane.layout = new BorderLayout()
            vbox(id:'vboxId', constraints:BorderLayout.NORTH)
            hbox(id:'hboxId', constraints:BorderLayout.WEST)
            rigidArea(id:'area1', constraints:BorderLayout.EAST, size:[3,4] as Dimension)
            rigidArea(id:'area2', constraints:BorderLayout.SOUTH, width:30, height:40)
            scrollPane(id:'scrollId', constraints:BorderLayout.CENTER,
                border:BorderFactory.createRaisedBevelBorder()) {
                glue()
                vglue()
                hglue()
                vstrut()
                vstrut(height:8)
                hstrut()
                hstrut(width:8)
                rigidArea(id:'area3')
                viewport()
            }
        }
        assert swing.vboxId.parent == swing.frameId.contentPane
        assert swing.hboxId.parent == swing.frameId.contentPane
        assert swing.scrollId.parent == swing.frameId.contentPane
    }

    void testPropertyColumn() {
        def swing = new SwingBuilder()
        def msg = shouldFail{
            swing.propertyColumn()
        }
        assert msg.contains('propertyColumn must be a child of a tableModel')
        msg = shouldFail{
            swing.table{
                tableModel(){
                    propertyColumn()
                }
            }
        }
        assert msg.contains("Must specify a property for a propertyColumn"): \
            "Instead found message: " + msg
        swing.table{
            tableModel(){
                propertyColumn(header:'header', propertyName:'foo')
                propertyColumn(propertyName:'bar', type:String.class)
            }
        }
    }

    void testClosureColumn() {
        def swing = new SwingBuilder()
        def msg = shouldFail{
            swing.closureColumn()
        }
        assert msg.contains('closureColumn must be a child of a tableModel')
        msg = shouldFail{
            swing.table{
                tableModel(){
                    closureColumn()
                }
            }
        }
        assert msg.contains("Must specify 'read' Closure property for a closureColumn"): \
            "Instead found message: " + msg
        def closure = { x -> x }
        swing.table{
            tableModel(){
                closureColumn(read:closure, write:closure, header:'header')
            }
            tableModel(model:new groovy.model.ValueHolder('foo')){
                closureColumn(read:closure, type:String.class)
            }
            tableModel(list:['a','b']){
                closureColumn(read:closure, type:String.class)
            }
        }
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
        swing.frame(){
            tableLayout(){
                tr() {
                    td {
                        label()
                    }
                }
            }
        }
    }
}