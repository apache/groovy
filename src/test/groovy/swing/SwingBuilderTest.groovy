/*
 * $Id:  $
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
 
package groovy.swing

import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.text.SimpleDateFormat
import javax.swing.Action
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JCheckBoxMenuItem
import javax.swing.JColorChooser
import javax.swing.JComboBox
import javax.swing.JDesktopPane
import javax.swing.JDialog
import javax.swing.JEditorPane
import javax.swing.JFileChooser
import javax.swing.JFormattedTextField
import javax.swing.JFrame
import javax.swing.JInternalFrame
import javax.swing.JLabel
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
import javax.swing.JTabbedPane
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.JTextPane
import javax.swing.JToggleButton
import javax.swing.JToolBar
import javax.swing.JTree
import javax.swing.JViewport
import javax.swing.JWindow
import javax.swing.KeyStroke
import javax.swing.SpringLayout
import javax.swing.SwingUtilities
import javax.swing.text.DateFormatter
import javax.swing.text.NumberFormatter

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
        if (isHeadless()) return

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
        if (isHeadless()) return

        def swing = new SwingBuilder()
        def buttonGroup = swing.buttonGroup()
        shouldFail(MissingPropertyException) {
            swing.label(buttonGroup:buttonGroup)
        }
    }

    void testWidget() {
        if (isHeadless()) return

        def swing = new SwingBuilder()
        def label = swing.label("By Value:")
        def widgetByValue = swing.widget(label) 
        assert widgetByValue != null
        def widgetByLabel = swing.widget(widget: label) 
        assert widgetByLabel != null
    }

    void testTableColumn() {
        if (isHeadless()) return

        // TODO is this required?
        def swing = new SwingBuilder()
        swing.table{
            tableColumn()
        }
    }

    void testSplitPane() {
        if (isHeadless()) return

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
        if (isHeadless()) return

        def swing = new SwingBuilder()
        swing.window{
            window()
            frame{ window() }
        }
    }

    void testDialogs() {
        if (isHeadless()) return

        def swing = new SwingBuilder()
        swing.dialog()
        swing.frame{ dialog() }
        swing.dialog{ dialog() }
    }

    void testWindows() {
        if (isHeadless()) return

        def swing = new SwingBuilder()
        swing.window()
        swing.frame{ window() }
        swing.dialog{ window() }
    }

    void testNodeCreation() {
        if (isHeadless()) return

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
        if (isHeadless()) return

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
        int actual = swing.actionId.getValue(Action.MNEMONIC_KEY)
        assert  actual == expected2
    }

    void testBuilderProperties() {
        if (isHeadless()) return

        def swing = new SwingBuilder()
        assert swing.class.name == 'groovy.swing.SwingBuilder'
    }

    void testFormattedTextField() {
        if (isHeadless()) return

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
        if (isHeadless()) return

        def swing = new SwingBuilder()
        swing.tabbedPane{
            button()
        }
    }

    void testComboBox() {
        if (isHeadless()) return

        def swing = new SwingBuilder()
        Object[] objects = ['a','b']
        def list = ['c', 'd', 'e']
        def vector = new Vector(['f', 'g', 'h', 'i'])
        assert swing.comboBox(items:objects).itemCount == 2
        assert swing.comboBox(items:list).itemCount == 3
        assert swing.comboBox(items:vector).itemCount == 4
    }

    void testMisplacedActionsAreIgnored() {
        if (isHeadless()) return

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
        if (isHeadless()) return

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
        if (isHeadless()) return

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
        if (isHeadless()) return

        def swing = new SwingBuilder()
        def help = swing.action(accelerator:'F1')
        def about = swing.action(accelerator:KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK))
        assert help.getValue(Action.ACCELERATOR_KEY).toString()
                             .indexOf(KeyEvent.getKeyText(KeyEvent.VK_F1)) > -1
        def aboutKeyStroke = about.getValue(Action.ACCELERATOR_KEY)
        assert aboutKeyStroke.keyCode == KeyEvent.VK_SPACE
        assert(aboutKeyStroke.modifiers & InputEvent.CTRL_MASK) != 0
    }

    Action verifyAccel(Action action, int mustHave = 0) {
        int mods = action.getValue(Action.ACCELERATOR_KEY).modifiers
        assert mods != 0
        assert (mods & mustHave) == mustHave
        // don't assert (modd % musthave) != 0 because mustHave may be the platform shortcut modifer
        return action
    }

    void testSetAcceleratorShortcuts() {
        if (isHeadless()) return

        def swing = new SwingBuilder()
        char q = 'Q'
        swing.actions() {
            verifyAccel(action(accelerator: shortcut(q)))
            verifyAccel(action(accelerator: shortcut(q, InputEvent.SHIFT_DOWN_MASK)), InputEvent.SHIFT_DOWN_MASK)
            verifyAccel(action(accelerator: shortcut(KeyEvent.VK_NUMPAD5)))
            verifyAccel(action(accelerator: shortcut(KeyEvent.VK_NUMPAD5, InputEvent.SHIFT_DOWN_MASK)), InputEvent.SHIFT_DOWN_MASK)
            verifyAccel(action(accelerator: shortcut('DELETE')))
            verifyAccel(action(accelerator: shortcut('DELETE', InputEvent.SHIFT_DOWN_MASK)), InputEvent.SHIFT_DOWN_MASK)
        }
    }

    void testBorderLayoutConstraints() {
        if (isHeadless()) return

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
        if (isHeadless()) return

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
            tableModel(id: 'model'){
                propertyColumn(propertyName:'p')
                propertyColumn(propertyName:'ph', header: 'header')
                propertyColumn(propertyName:'pt', type: String)
                propertyColumn(propertyName:'pth', type: String, header: 'header')
                propertyColumn(propertyName:'pe', editable:false)
                propertyColumn(propertyName:'peh', editable:false, header: 'header')
                propertyColumn(propertyName:'pet', editable:false, type: String, )
                propertyColumn(propertyName:'peth', editable:false, type: String, header: 'header')
            }
        }
        swing.model.columnList.each() { col ->
            def propName = col.valueModel.property
            assert (col.headerValue == 'header') ^ !propName.contains('h')
            assert (col.type == String) ^ !propName.contains('t')
            assert col.valueModel.editable ^ propName.contains('e')
        }
    }

    void testClosureColumn() {
        if (isHeadless()) return

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
        def table = swing.table{
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

        assert table.columnModel.class.name == 'groovy.model.DefaultTableModel$MyTableColumnModel'
    }

    void testTableModelChange() {
        if (isHeadless()) return

        def swing = new SwingBuilder()
        def table = swing.table {
            tableModel {
                propertyColumn(propertyName:'p')
                propertyColumn(propertyName:'ph', header: 'header')
                propertyColumn(propertyName:'pt', type: String)
            }
        }

        def sorter = new groovy.inspect.swingui.TableSorter(table.model)
        table.model = sorter

        //GROOVY-2111 - resetting the model w/ a pass-through cleared the columns
        assert table.columnModel.columnCount == 3
    }

    void testSetConstraints() {
        if (isHeadless()) return

        def swing = new SwingBuilder()
        swing.panel(layout:new BorderLayout()) {
            label(text:'Name', constraints:BorderLayout.CENTER)
        }
    }

    void testSetToolTipText() {
        if (isHeadless()) return

        def swing = new SwingBuilder()
        swing.panel(layout:new BorderLayout()) {
            label(id:'labelId', text:'Name', toolTipText:'This is the name field')
        }

    }

    void testTableLayout() {
        if (isHeadless()) return

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

    void testAttributeOrdering() {
        if (isHeadless()) return

        def swing = new SwingBuilder()

        def frame = swing.frame(
            size:[500,500],
            locationRelativeTo:null
        )
        def locationFirst = frame.location

        frame = swing.frame(
            locationRelativeTo:null,
            size:[500,500]
        )
        def locationLast = frame.location

        // setLocationReativeTo(null) places the component in the center of
        // the screen, relative to it's size, so centering it after sizing it
        // should result in a 250,250 offset from centering it before sizing it
        assert locationFirst != locationLast
    }
    
    void testWidgetPassthroughConstraints() {
        if (isHeadless()) return

        def swing = new SwingBuilder()
        def foo = swing.button('North')
        def frame = swing.frame() {
            borderLayout()
            widget(foo, constraints: BorderLayout.NORTH)
            // a failed test throws MissingPropertyException by now
        }
    }    
    
    void testGROOVY1837ReuseAction() {
        if (isHeadless()) return
        
        def swing = new SwingBuilder()

        def testAction = swing.action(name:'test', mnemonic:'A', accelerator:'ctrl R')
        assert testAction.getValue(Action.MNEMONIC_KEY) != null
        assert testAction.getValue(Action.ACCELERATOR_KEY) != null
        
        swing.action(testAction)
        assert testAction.getValue(Action.MNEMONIC_KEY) != null
        assert testAction.getValue(Action.ACCELERATOR_KEY) != null
    }

    void testSeparators() {
        if (isHeadless()) return
        
        def swing = new SwingBuilder()
        swing.frame() {
            menu("test") {
                separator(id:"menuSep")
            }
            toolBar() {
                separator(id:"tbSep")
            }
            separator(id:"sep")
        }
        assert swing.menuSep instanceof JPopupMenu.Separator
        assert swing.tbSep instanceof JToolBar.Separator
        assert swing.sep instanceof JSeparator
    }

    void testCollectionNodes() {
        if (isHeadless()) return
        
        def swing = new SwingBuilder()
        def collection = swing.actions() {
            action(id:'test')
        }
        assert collection.contains(swing.test)
    }

    void testFactoryCornerCases() {
        if (isHeadless()) return
        
        def swing = new SwingBuilder()
        assert swing.bogusWidget() == null

        swing.registerFactory("nullWidget", 
            [newInstance:{builder, name, value, props -> null}] as groovy.swing.factory.Factory)
        assert swing.nullWidget() == null
    } 

    void testFactoryLogging() {
        def logger = java.util.logging.Logger.getLogger(SwingBuilder.class.name)
        def oldLevel = logger.getLevel()
        logger.setLevel(java.util.logging.Level.FINE)
        def swing = new SwingBuilder()
        swing.label()
        logger.setLevel(oldLevel)
    }

    void testEnhancedValueArguments() {
        if (isHeadless()) return
        
        def swing = new SwingBuilder()

        def anAction = swing.action(name:"test action")
        def icon = new javax.swing.plaf.metal.MetalComboBoxIcon()
        def richActionItems = [
            'button', 
            'checkBox', 
            'radioButton', 
            'toggleButton',
            'menuItem', 
            'checkBoxMenuItem', 
            'radioButtonMenuItem'
        ]

        richActionItems.each {name ->
            swing."$name"(anAction, id:"${name}Action".toString())
            swing."$name"(icon, id:"${name}Icon".toString())
            swing."$name"("string", id:"${name}String".toString())
            swing."$name"(swing."${name}Action", id:"${name}Self".toString())

            assert swing."${name}Action"
            assert swing."${name}Icon"
            assert swing."${name}String".text == 'string'
            assert swing."${name}Self" == swing."${name}Action"
            shouldFail {
                swing."$name"(['bad'])
            }
        }

        // elements that take no value argument
        def noValueItems = [
            "actions",
            "boxLayout",
            "comboBox",
            "formattedTextField",
            "glue",
            "hbox",
            "hglue",
            "hstrut",
            "map",
            "rigidArea",
            "separator",
            "vbox",
            "vglue",
            "vstrut",
            "window",
        ]

        noValueItems.each {name ->
            println name
            shouldFail {
                swing.frame() {
                    "$name"(swing."$name"(), id:"${name}Self".toString())
                }
	    }
        }

         // elements that only take their own type as a value argument
        def selfItems = [
            "action",
            "borderLayout",
            "boundedRangeModel",
            "box",
            "buttonGroup",
            "cardLayout",
            //"closureColumn",
            "colorChooser",
            //"container",
            "desktopPane",
            "dialog",
            "fileChooser",
            "flowLayout",
            "frame",
            "gbc",
            "gridBagConstraints",
            "gridBagLayout",
            "gridLayout",
            "internalFrame",
            "layeredPane",
            "list",
            "menu",
            "menuBar",
            "optionPane",
            //"overlayLayout",
            "panel",
            "popupMenu",
            "progressBar",
            //"propertyColumn",
            "scrollBar",
            "scrollPane",
            "slider",
            "spinner",
            "spinnerDateModel",
            "spinnerListModel",
            "spinnerNumberModel",
            "splitPane",
            "springLayout",
            "tabbedPane",
            "table",
            "tableColumn",
            "tableLayout",
            "tableModel",
            //"td",
            "toolBar",
            //"tr",
            "tree",
            "viewport",
            //"widget",
        ]
        selfItems.each {name ->
            println name
            swing.frame() {
                "$name"(swing."$name"(), id:"${name}Self".toString())
            }

            shouldFail {
                swing.frame() {
                    swing."$name"(icon)
                }
	    }
        }

         // elements take their own type as a value argument or a stringa s a text property
        def textItems = [
            "editorPane",
            "label",
            "passwordField",
            "textArea",
            "textField",
            "textPane",
        ]
        textItems.each {name ->
            println name
            swing.frame() {
                "$name"(swing."$name"(), id:"${name}Self".toString())
                "$name"('text', id:"${name}Text".toString())
            }
            assert swing."${name}Text".text == 'text'

            shouldFail {
                swing.frame() {
                    swing."$name"(icon)
                }
	    }
        }
        
        // leftovers...
        swing.frame() {
            action(action:anAction)
            box(axis:BoxLayout.Y_AXIS)
            hstrut(5)
            vstrut(5)
            tableModel(tableModel:tableModel())
            container(panel()) {
                widget(label("label"))
                bean("anything")
            }
            container(container:panel()) {
                widget(widget:label("label"))
                bean(bean:"anything")
            }
        }
        shouldFail() {
            swing.actions(property:'fails')
        }
        shouldFail() {
            swing.widget()
        }
        shouldFail() {
            swing.container()
        }
        shouldFail() {
            swing.bean()
        }
    }

    public void testEDT() {
        if (isHeadless()) return
        
        def swing = new SwingBuilder()
        
        boolean pass = false
        swing.edt() { pass = SwingUtilities.isEventDispatchThread() }
        assert pass
        
        pass = false
        swing.edt() { swing.edt() { pass = SwingUtilities.isEventDispatchThread() } }
        assert pass
    }

}