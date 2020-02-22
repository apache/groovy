/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.swing

import javax.swing.JPopupMenu.Separator as JPopupMenu_Separator
import javax.swing.JToolBar.Separator as JToolBar_Separator

import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.text.SimpleDateFormat
import javax.swing.border.TitledBorder
import javax.swing.plaf.metal.MetalLookAndFeel
import javax.swing.text.DateFormatter
import javax.swing.text.NumberFormatter
import java.awt.*
import javax.swing.*
import groovy.util.HeadlessTestSupport

class SwingBuilderTest extends GroovySwingTestCase {

    void testWidgetId() {
        testInEDT {

            def swing = new SwingBuilder()
            def localVar = null

            swing.panel {
                label('byAttr', id: 'byAttr')
                byExpr = label('byExpr')
                localVar = label('localVar')
            }
            swing[SwingBuilder.DELEGATE_PROPERTY_OBJECT_ID] = 'key'
            swing.panel {
                label('byKey', key: 'byKey')
            }

            assert localVar != null
            assert swing.getVariables().containsKey('byAttr')
            assert swing.getVariables().containsKey('byExpr')
            assert !swing.getVariables().containsKey('localVar')
            assert swing.getVariables().containsKey('byKey')
        }
    }

    void testNamedWidgetCreation() {
        testInEDT {
            def topLevelWidgets = [
                    frame: [JFrame, true],
                    dialog: [JDialog, true],
                    window: [JWindow, false],
                    fileChooser: [JFileChooser, false],
                    optionPane: [JOptionPane, false]
            ]
            def swing = new SwingBuilder()
            topLevelWidgets.each { name, widgetInfo ->
                if (widgetInfo[1])
                    swing."$name"(id: "${name}Id".toString(), title: "This is my $name")
                else
                    swing."$name"(id: "${name}Id".toString())
                def widget = swing."${name}Id"
                assert widget.class == widgetInfo[0]
                if (widgetInfo[1]) assert widget.title == "This is my $name"
            }
        }
    }

    void testLayoutCreation() {
        testInEDT {

            def layouts = [
                    borderLayout: BorderLayout,
                    cardLayout: CardLayout,
                    flowLayout: FlowLayout,
                    gridBagLayout: GridBagLayout,
                    gridLayout: GridLayout,
                    springLayout: SpringLayout
            ]
            def swing = new SwingBuilder()
            layouts.each { name, expectedLayoutClass ->
                def frame = swing.frame {
                    "$name"()
                }
                assert frame.contentPane.layout.class == expectedLayoutClass
            }
        }
    }

    void testGridBagFactory() {
        testInEDT {
            def swing = new SwingBuilder()

            swing.frame {
                gridBagLayout()
                label(fill: BOTH)
            }
            shouldFail {
                swing.frame {
                    flowLayout()
                    label(fill: BOTH)
                }
            }
            shouldFail {
                swing.frame {
                    label(fill: GridBagConstraints.BOTH)
                }
            }
        }
    }

    void testBorderLayout() {
        testInEDT {
            def swing = new SwingBuilder()

            swing.frame {
                borderLayout()
                label("x", constraints: NORTH)
            }

            // test that BorderLayout.NORTH is not implied
            shouldFail(MissingPropertyException) {
                swing.frame {
                    label("x", constraints: NORTH)
                }
            }
        }
    }

    void testLayoutConstraintsProperty() {
        testInEDT {
            def swing = new SwingBuilder()

            swing.frame {
                borderLayout(constraintsProperty: 'direction')
                label("x", direction: NORTH)
            }
        }
    }

    void testWidgetCreation() {
        testInEDT {

            def widgets = [
                    button: JButton,
                    checkBox: JCheckBox,
                    checkBoxMenuItem: JCheckBoxMenuItem,
                    colorChooser: JColorChooser,
                    comboBox: JComboBox,
                    desktopPane: JDesktopPane,
                    editorPane: JEditorPane,
                    formattedTextField: JFormattedTextField,
                    internalFrame: JInternalFrame,
                    label: JLabel,
                    layeredPane: JLayeredPane,
                    list: JList,
                    menu: JMenu,
                    menuBar: JMenuBar,
                    menuItem: JMenuItem,
                    panel: JPanel,
                    passwordField: JPasswordField,
                    popupMenu: JPopupMenu,
                    progressBar: JProgressBar,
                    radioButton: JRadioButton,
                    radioButtonMenuItem: JRadioButtonMenuItem,
                    scrollBar: JScrollBar,
                    scrollPane: JScrollPane,
                    separator: JSeparator,
                    slider: JSlider,
                    spinner: JSpinner,
                    splitPane: JSplitPane,
                    tabbedPane: JTabbedPane,
                    table: JTable,
                    textArea: JTextArea,
                    textPane: JTextPane,
                    textField: JTextField,
                    toggleButton: JToggleButton,
                    toolBar: JToolBar,
                    tree: JTree,
                    viewport: JViewport,
            ]
            def swing = new SwingBuilder()
            widgets.each { name, expectedLayoutClass ->
                def frame = swing.frame {
                    "$name"(id: "${name}Id".toString())
                }
                assert swing."${name}Id".class == expectedLayoutClass
            }
        }
    }

    void testButtonGroup() {
        testInEDT {
            def swing = new SwingBuilder()

            swing.panel {
                buttonGroup(id: 'group1')
                buttonGroup(id: 'group2')
                checkBox(id: 'cb1a', buttonGroup: group1)
                checkBox(id: 'cb1b', buttonGroup: group1)
                checkBox(id: 'cb2a', buttonGroup: group2)
                checkBox(id: 'cb2b', buttonGroup: group2)

            }

            def statusCBs = {[swing.cb1a.selected, swing.cb1b.selected, swing.cb2a.selected, swing.cb2b.selected]}

            assert statusCBs() == [false, false, false, false]

            swing.cb1a.selected = true
            assert statusCBs() == [true, false, false, false]

            swing.cb1b.selected = true
            assert statusCBs() == [false, true, false, false]

            swing.cb2a.selected = true
            assert statusCBs() == [false, true, true, false]

            swing.cb2b.selected = true
            assert statusCBs() == [false, true, false, true]

        }
    }

    void testButtonGroupOnlyForButtons() {
        testInEDT {
            def swing = new SwingBuilder()

            def buttonGroup = swing.buttonGroup()
            shouldFail(MissingPropertyException) {
                swing.label(buttonGroup: buttonGroup)
            }
        }
    }

    void testWidget() {
        testInEDT {

            def swing = new SwingBuilder()
            def label = swing.label("By Value:")
            def widgetByValue = swing.widget(label)
            assert widgetByValue != null
            def widgetByLabel = swing.widget(widget: label)
            assert widgetByLabel != null
        }
    }

    void testSplitPane() {
        testInEDT {

            def swing = new SwingBuilder()
            def buttonGroup = swing.buttonGroup()
            def frame = swing.frame {
                splitPane(id: 'hsplit', orientation: JSplitPane.HORIZONTAL_SPLIT) {
                    button(id: 'left', buttonGroup: buttonGroup)
                    button(id: 'right', buttonGroup: buttonGroup)
                }
                splitPane(id: 'vsplit', orientation: JSplitPane.VERTICAL_SPLIT) {
                    button(id: 'top')
                    button(id: 'bottom')
                }
            }
            assert swing.hsplit.leftComponent == swing.left
            assert swing.hsplit.rightComponent == swing.right
            assert swing.vsplit.topComponent == swing.top
            assert swing.vsplit.bottomComponent == swing.bottom
        }
    }

    void testNestedWindows() {
        testInEDT {

            def swing = new SwingBuilder()
            swing.window(id: 'root') {
                window(id: 'child1')
                frame(id: 'child2') {
                    window(id: 'child2_1')
                }
            }
            swing.window(id: 'root2')

            //assert swing.root.owner == null
            assert swing.child1.owner == swing.root
            assert swing.child2.owner == null // it's a frame, frames have no owners
            assert swing.child2_1.owner == swing.child2

            assert swing.root2.owner != swing.root
            assert swing.root2.owner != swing.child1
            assert swing.root2.owner != swing.child2
            assert swing.root2.owner != swing.child2_1

            swing.panel {
                swing.frame()
                swing.window()
                swing.dialog()
            }
        }
    }

    void testFrames() {
        testInEDT {
            def swing = new SwingBuilder()

            swing.frame(id: 'frame') {
                button('test', id: 'button', defaultButton: true)
            }
            assert swing.frame.rootPane.defaultButton == swing.button
            assert swing.button.defaultButton
        }
    }

    void testDialogs() {
        testInEDT {

            def swing = new SwingBuilder()
            swing.dialog(id: 'd1')
            swing.frame(id: 'f') { dialog(id: 'fd') }
            swing.dialog(id: 'd') { dialog(id: 'dd') }
            swing.dialog(id: 'd2')

            //assert swing.d1.owner == null
            assert swing.fd.owner == swing.f
            assert swing.dd.owner == swing.d

            assert swing.d2.owner != swing.dd
            assert swing.d2.owner != swing.fd
            assert swing.d2.owner != swing.d
            assert swing.d2.owner != swing.f
            assert swing.d2.owner != swing.d1
        }
    }

    void testWindows() {
        testInEDT {

            def swing = new SwingBuilder()
            swing.window()
            swing.frame { window() }
            swing.dialog { window() }
        }
    }

    void testNodeCreation() {
        testInEDT {

            def swing = new SwingBuilder()
            def frame = swing.frame {
                // 4 valid parameter combinations
                button()
                button('Text')
                button(label: 'Label')
                button(label: 'Label', 'Text')
            }
            shouldFail {
                frame = swing.frame {
                    // invalid parameter
                    button(new Date())
                }
            }
        }
    }

    void testSetMnemonic() {
        testInEDT {

            def swing = new SwingBuilder()
            swing.panel(layout: new BorderLayout()) {
                label(id: 'label0', text: 'Name0', displayedMnemonic: 48)
                label(id: 'label1', text: 'Name1', displayedMnemonic: 'N' as char)
            }
            int expected0 = '0'
            int expected1 = 'N'
            assert swing.label0.displayedMnemonic == expected0
            assert swing.label1.displayedMnemonic == expected1
            swing.menuBar {
                menu {
                    menuItem {
                        action(id: 'actionId', name: 'About', mnemonic: 'A')
                    }
                }
            }
            int expected2 = 'A'
            int actual = swing.actionId.getValue(Action.MNEMONIC_KEY)
            assert actual == expected2
        }
    }

    void testBuilderProperties() {
        testInEDT {

            def swing = new SwingBuilder()
            assert swing.class.name == SwingBuilder.class.name
        }
    }

    void testFormattedTextField() {
        testInEDT {

            def swing = new SwingBuilder()
            def dummy = new Date()
            def field = swing.formattedTextField(value: dummy)
            assert field.value == dummy
            assert field.formatter.class == DateFormatter
            def dummyFormatter = new SimpleDateFormat()
            field = swing.formattedTextField(format: dummyFormatter)
            assert field.formatter.class == DateFormatter
            field = swing.formattedTextField()
            field.value = 3
            assert field.formatter.class == NumberFormatter
        }
    }

    void testScrollPane() {
        testInEDT {

            def swing = new SwingBuilder()
            shouldFail {
                swing.scrollPane {
                    button("OK")
                    button("Cancel")
                }
            }
        }
    }

    void testComboBox() {
        testInEDT {

            def swing = new SwingBuilder()
            Object[] objects = ['a', 'b']
            def list = ['c', 'd', 'e']
            def vector = new Vector(['f', 'g', 'h', 'i'])
            assert swing.comboBox(items: objects).itemCount == 2
            assert swing.comboBox(items: list).itemCount == 3
            assert swing.comboBox(items: vector).itemCount == 4
            assert swing.comboBox().itemCount == 0
        }
    }

    void testList() {
        testInEDT {

            def swing = new SwingBuilder()
            Object[] objects = ['a', 'b']
            def list = ['c', 'd', 'e']
            def vector = new Vector(['f', 'g', 'h', 'i'])
            assert swing.list(items: objects).model.size == 2
            assert swing.list(items: list).model.size == 3
            assert swing.list(items: vector).model.size == 4
            assert swing.list().model.size == 0
            assert swing.list(listData: objects).model.size == 2
            assert swing.list(listData: list).model.size == 3
            assert swing.list(listData: vector).model.size == 4
            assert swing.list(listData: "list").model.size == 4
            assert swing.list(listData: [a: 1, b: 2].collect { k, v -> v}).model.size == 2
            def theList = swing.list(items: list)
            list[1] = 'a'
            assert theList.model.getElementAt(1) == 'a'
            theList.model.add('z')
            assert list.size() == 4
            assert list[3] == 'z'
        }
    }

    void testMisplacedActionsAreIgnored() {
        testInEDT {

            def swing = new SwingBuilder()
            // labels don't support actions; should be ignored
            swing.label {
                action(id: 'actionId', Name: 'about', mnemonic: 'A', closure: {x -> x})
                map()
            }
            swing.panel {
                borderLayout {
                    // layouts don't support actions, will be ignored
                    action(id: 'actionId')
                }
            }
        }
    }

    void testBoxLayout() {
        testInEDT {

            def swing = new SwingBuilder()
            def message = shouldFail {
                swing.boxLayout()
            }
            assert message.contains('Must be nested inside a Container')
            // default is X_AXIS
            swing.panel(id: 'panel') {
                boxLayout(id: 'layout1')
            }
            // can also set explicit axis
            swing.frame(id: 'frame') {
                boxLayout(id: 'layout2', axis: BoxLayout.Y_AXIS)
            }
        }
    }

    void testKeystrokesWithinActions() {
        testInEDT {

            def swing = new SwingBuilder()
            swing.panel {
                button(id: 'buttonId') {
                    action(id: 'action1', keyStroke: 'ctrl W')
                    action(id: 'action2',
                            keyStroke: KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.ALT_MASK))
                    action(id: 'action3', keyStroke: "${'ctrl Z'}")
                }
            }
            def component = swing.buttonId
            def expected1 = swing.action1.toString()
            def expected2 = swing.action2.toString()
            def expected3 = swing.action3.toString()
            def keys = component.actionMap.allKeys().toList()
            assert keys.contains(expected1)
            assert keys.contains(expected2)
            assert keys.contains(expected3)
            def inputMap = component.inputMap
            def values = inputMap.allKeys().toList().collect { inputMap.get(it) }
            assert values.contains(expected1)
            assert values.contains(expected2)
            assert values.contains(expected3)
        }
    }

    void testActionClosures() {
        testInEDT {

            def swing = new SwingBuilder()
            def testTarget = 'blank'
            swing.actions {
                action(id: 'a', closure: {testTarget = 'A'})
                action(id: 'b') {testTarget = 'B' }
                action(id: 'c', closure: {evt -> testTarget = 'C'})
                action(id: 'd') {evt -> testTarget = 'D' }
            }

            ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "")
            assert testTarget == 'blank'
            swing.a.actionPerformed(evt)
            assert testTarget == 'A'
            swing.b.actionPerformed(evt)
            assert testTarget == 'B'
            swing.c.actionPerformed(evt)
            assert testTarget == 'C'
            swing.d.actionPerformed(evt)
            assert testTarget == 'D'

            // negative tests
            swing.actions {
                action(id: 'z')
                shouldFail(RuntimeException) {
                    action(id: 'y', closure: {testTarget = 'Y'}) {testTarget = 'YY'}
                }
                shouldFail(RuntimeException) {
                    action([actionPerformed: {testTarget = 'X'}] as AbstractAction, id: 'x') { testTarget = 'XX'}
                }
            }
            shouldFail(NullPointerException) {
                swing.z.actionPerformed(evt)
            }

        }
    }

    void testSetAccelerator() {
        testInEDT {

            def swing = new SwingBuilder()
            def help = swing.action(accelerator: 'F1')
            def about = swing.action(accelerator: KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK))
            assert help.getValue(Action.ACCELERATOR_KEY).toString()
                    .indexOf(KeyEvent.getKeyText(KeyEvent.VK_F1)) > -1
            def aboutKeyStroke = about.getValue(Action.ACCELERATOR_KEY)
            assert aboutKeyStroke.keyCode == KeyEvent.VK_SPACE
            assert (aboutKeyStroke.modifiers & InputEvent.CTRL_MASK) != 0
        }
    }

    private verifyAccel(action, int mustHave = 0) {
        int mods = action.getValue(Action.ACCELERATOR_KEY).modifiers
        assert mods != 0
        assert (mods & mustHave) == mustHave
        // don't assert (modd % musthave) != 0 because mustHave may be the platform shortcut modifer
        return action
    }

    void testSetAcceleratorShortcuts() {
        testInEDT {

            def swing = new SwingBuilder()
            char q = 'Q'
            swing.actions {
                verifyAccel(action(accelerator: shortcut(q)))
                verifyAccel(action(accelerator: shortcut(q, InputEvent.SHIFT_DOWN_MASK)), InputEvent.SHIFT_DOWN_MASK)
                verifyAccel(action(accelerator: shortcut(KeyEvent.VK_NUMPAD5)))
                verifyAccel(action(accelerator: shortcut(KeyEvent.VK_NUMPAD5, InputEvent.SHIFT_DOWN_MASK)), InputEvent.SHIFT_DOWN_MASK)
                verifyAccel(action(accelerator: shortcut('DELETE')))
                verifyAccel(action(accelerator: shortcut('DELETE', InputEvent.SHIFT_DOWN_MASK)), InputEvent.SHIFT_DOWN_MASK)
            }
        }
    }

    void testBorderLayoutConstraints() {
        testInEDT {

            def swing = new SwingBuilder()
            swing.internalFrame(id: 'frameId',
                    border: BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder())) {
                swing.frameId.contentPane.layout = new BorderLayout()
                vbox(id: 'vboxId', constraints: BorderLayout.NORTH)
                hbox(id: 'hboxId', constraints: BorderLayout.WEST)
                rigidArea(id: 'area1', constraints: BorderLayout.EAST, size: [3, 4] as Dimension)
                rigidArea(id: 'area2', constraints: BorderLayout.SOUTH, width: 30, height: 40)
                scrollPane(id: 'scrollId', constraints: BorderLayout.CENTER,
                        border: BorderFactory.createRaisedBevelBorder()) {
                    panel() {
                        glue()
                        vglue()
                        hglue()
                        vstrut()
                        vstrut(height: 8)
                        hstrut()
                        hstrut(width: 8)
                        rigidArea(id: 'area3')
                    }
                }
            }
            assert swing.vboxId.parent == swing.frameId.contentPane
            assert swing.hboxId.parent == swing.frameId.contentPane
            assert swing.scrollId.parent == swing.frameId.contentPane
        }
    }

    void testSetConstraints() {
        testInEDT {

            def swing = new SwingBuilder()
            swing.panel(layout: new BorderLayout()) {
                label(text: 'Name', constraints: BorderLayout.CENTER)
            }
        }
    }

    void testSetToolTipText() {
        testInEDT {

            def swing = new SwingBuilder()
            swing.panel(layout: new BorderLayout()) {
                label(id: 'labelId', text: 'Name', toolTipText: 'This is the name field')
            }

        }
    }

    void testAttributeOrdering() {
        testInEDT {

            def swing = new SwingBuilder()

            def frame = swing.frame(
                    size: [500, 500],
                    locationRelativeTo: null
            )
            def locationFirst = frame.location

            frame = swing.frame(
                    locationRelativeTo: null,
                    size: [500, 500]
            )
            def locationLast = frame.location

            // setLocationReativeTo(null) places the component in the center of
            // the screen, relative to it's size, so centering it after sizing it
            // should result in a 250,250 offset from centering it before sizing it
            assert locationFirst != locationLast
        }
    }

    void testWidgetPassthroughConstraints() {
        testInEDT {

            def swing = new SwingBuilder()
            def foo = swing.button('North')
            def frame = swing.frame {
                borderLayout()
                widget(foo, constraints: BorderLayout.NORTH)
                // a failed test throws MissingPropertyException by now
            }
        }
    }

    void testGROOVY1837ReuseAction() {
        testInEDT {

            def swing = new SwingBuilder()

            def testAction = swing.action(name: 'test', mnemonic: 'A', accelerator: 'ctrl R')
            assert testAction.getValue(Action.MNEMONIC_KEY) != null
            assert testAction.getValue(Action.ACCELERATOR_KEY) != null

            swing.action(testAction)
            assert testAction.getValue(Action.MNEMONIC_KEY) != null
            assert testAction.getValue(Action.ACCELERATOR_KEY) != null
        }
    }

    void testSeparators() {
        testInEDT {

            def swing = new SwingBuilder()
            swing.frame {
                menu("test") {
                    separator(id: "menuSep")
                }
                toolBar {
                    separator(id: "tbSep")
                }
                separator(id: "sep")
            }
            assert swing.menuSep instanceof JPopupMenu_Separator
            assert swing.tbSep instanceof JToolBar_Separator
            assert swing.sep instanceof JSeparator
        }
    }

    void testCollectionNodes() {
        testInEDT {

            def swing = new SwingBuilder()
            def collection = swing.actions {
                action(id: 'test')
            }
            assert collection.contains(swing.test)
        }
    }

    void testFactoryCornerCases() {
        testInEDT {

            def swing = new SwingBuilder()

            // change in 1.6, bad node names throw exceptions instead of being ignored
            shouldFail(MissingMethodException) {
                swing.bogusWidget() == null
            }

            swing.registerFactory("nullWidget",
                    [newInstance: {builder, name, value, props -> null}] as AbstractFactory)
            assert swing.nullWidget() == null
        }
    }

    void testFactoryLogging() {
        testInEDT {

            def logger = java.util.logging.Logger.getLogger(SwingBuilder.class.name)
            def oldLevel = logger.getLevel()
            logger.setLevel(java.util.logging.Level.FINE)
            def swing = new SwingBuilder()
            swing.label()
            logger.setLevel(oldLevel)
        }
    }

    void testEnhancedValueArguments() {
        testInEDT {

            def swing = new SwingBuilder()

            // elements that take an action, icon, string, GString,
            // or their own type as a value arg
            def anAction = swing.action(name: "test action")
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
                swing."$name"(anAction, id: "${name}Action".toString())
                swing."$name"(icon, id: "${name}Icon".toString())
                swing."$name"("string", id: "${name}String".toString())
                swing."$name"("${'g'}string", id: "${name}GString".toString())
                swing."$name"(swing."${name}Action", id: "${name}Self".toString())

                assert swing."${name}Action"
                assert swing."${name}Icon"
                assert swing."${name}String".text == 'string'
                assert swing."${name}GString".text == 'gstring'
                assert swing."${name}Self" == swing."${name}Action"
                shouldFail {
                    swing."$name"(['bad'])
                }
            }

            // elements that take no value argument
            def noValueItems = [
                    "actions",
                    "boxLayout",
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
            ]

            noValueItems.each {name ->
                //println name
                shouldFail {
                    swing.frame {
                        "$name"(swing."$name"(), id: "${name}Self".toString())
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
                    "comboBox",
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
                    // "list", // list acceps JList, Vector, Object[], List
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
                    "window",
            ]
            selfItems.each {name ->
                //println name
                swing.frame {
                    "$name"(swing."$name"(), id: "${name}Self".toString())
                }

                shouldFail {
                    swing.frame {
                        swing."$name"(icon)
                    }
                }
            }

            // elements take their own type as a value argument or a [g]string as a text property
            def textItems = [
                    "editorPane",
                    "label",
                    "menu",
                    "passwordField",
                    "textArea",
                    "textField",
                    "textPane",
            ]
            textItems.each {name ->
                swing.frame {
                    "$name"(swing."$name"(), id: "${name}Self".toString())
                    "$name"('text', id: "${name}Text".toString())
                    "$name"("${'g'}string", id: "${name}GString".toString())
                }
                assert swing."${name}Text".text == 'text'
                assert swing."${name}GString".text == 'gstring'

                shouldFail {
                    swing.frame {
                        swing."$name"(icon)
                    }
                }
            }

            // leftovers...
            swing.frame {
                action(action: anAction)
                box(axis: BoxLayout.Y_AXIS)
                hstrut(5)
                vstrut(5)
                tableModel(tableModel: tableModel())
                container(id: 'c', panel()) {
                    widget(id: 'w', label("label"))
                    bean("anything")
                }
                container(container: panel()) {
                    widget(widget: label("label"))
                    bean(bean: "anything")
                }
            }
            assert swing.w.parent == swing.c
            shouldFail {
                swing.actions(property: 'fails')
            }
            shouldFail {
                swing.widget()
            }
            shouldFail {
                swing.widget(label('label')) {
                    label('No Content For You!')
                }
            }
            shouldFail {
                swing.container()
            }
            shouldFail {
                swing.bean()
            }
            shouldFail {
                swing.bean("anything") {
                    label('Nothing')
                }
            }
        }
    }

    boolean instancePass

    void markPassed() {
        instancePass = true
    }

    void testEDT() {
        if (HeadlessTestSupport.headless) return
        def swing = new SwingBuilder()

        boolean pass = false
        swing.edt { pass = SwingUtilities.isEventDispatchThread() }
        assert pass

        pass = false
        swing.edt { swing.edt { pass = SwingUtilities.isEventDispatchThread() } }
        assert pass

        instancePass = false
        swing.edt this.&markPassed
        assert instancePass
    }

    void testDoLater() {
        if (HeadlessTestSupport.headless) return
        def swing = new SwingBuilder()

        boolean pass = false
        swing.doLater {sleep 100; pass = true }
        assert !pass
        // check for pass changing up to 3 times, then call it a failed test
        int maxFailures = 3
        while (maxFailures > 0) {
            sleep 200
            if (pass) break
            maxFailures--
        }
        assert pass

        // doLater in the EDT is still a do later
        pass = false
        swing.edt { swing.doLater {sleep 100; pass = true } }
        assert !pass
        // check for pass changing up to 3 times, then call it a failed test
        maxFailures = 3
        while (maxFailures > 0) {
            sleep 200
            if (pass) break
            maxFailures--
        }
        assert pass

        instancePass = false
        swing.doLater this.&markPassed
        // check for pass changing up to 3 times, then call it a failed test
        maxFailures = 3
        while (maxFailures > 0) {
            sleep 50
            if (instancePass) break
            maxFailures--
        }
        assert instancePass
    }

    void testDoOutside() {
        testInEDT {
            def swing = new SwingBuilder()

            boolean pass = false
            swing.doOutside {sleep 100; pass = true }
            assert !pass
            // check for pass changing up to 3 times, then call it a failed test
            int maxFailures = 3
            while (maxFailures > 0) {
                sleep 200
                if (pass) break
                maxFailures--
            }
            assert pass

            pass = false
            swing.edt {
                swing.doOutside {sleep 100; pass = true }
                assert !pass
                // check for pass changing up to 3 times, then call it a failed test
                int myMaxFailures = 3
                while (myMaxFailures > 0) {
                    sleep 200
                    if (pass) break
                    myMaxFailures--
                }
                assert pass
            }

            instancePass = false
            swing.doOutside this.&markPassed
            // check for pass changing up to 3 times, then call it a failed test
            maxFailures = 3
            while (maxFailures > 0) {
                sleep 50
                if (instancePass) break
                maxFailures--
            }
            assert instancePass
        }
    }

    void testJumbledThreading() {
        if (HeadlessTestSupport.headless) return;

        def swing = new SwingBuilder()
        Closure threadTest = {c ->
            boolean notifyReached = false;
            Throwable caughtThrowable = null
            Thread t = Thread.start {
                try {
                    c()
                } catch (Throwable throwable) {
                    caughtThrowable = throwable
                }
                notifyReached = true
                synchronized (swing) { swing.notifyAll() }
            }

            synchronized (swing) { swing.wait(2000); }
            if (!notifyReached && t.isAlive()) {
                Thread.start {
                    sleep(1000)
                    exit(0)
                }
                fail("EDT Deadlock")
            }
            if (caughtThrowable) {
                throw caughtThrowable
            }
            assert swing.l.parent != null
            notifyReached = false
        }

        threadTest {
            swing.frame {
                edt {
                    label('label', id: 'l')
                }
            }
        }

        threadTest {
            swing.edt {
                swing.frame {
                    edt {
                        label('label', id: 'l')
                    }
                }
            }
        }

        // full build in EDT shold be fine
        threadTest {
            swing.edt {
                swing.frame {
                    label('label', id: 'l')
                }
            }
        }

        // nested build(Closure) call.
        // Bad form, but it shouldn't break stuff
        threadTest {
            swing.frame {
                build {
                    label('label', id: 'l')
                }
            }
        }

        // insure the legacy static build(Closure) call still works.
        def oldSwing = swing
        threadTest {
            swing = SwingBuilder.build {
                frame {
                    label('label', id: 'l')
                }
            }
        }
        assert swing != oldSwing
    }

    void testParallelBuild() {
        if (HeadlessTestSupport.headless) return;

        def swing = new SwingBuilder()
        def p
        def l

        Thread t1 = Thread.start {
            p = swing.panel() {
                sleep(100)
                label('child')
            }
        }
        Thread t2 = Thread.start {
            sleep(50)
            l = swing.label('loner')
        }

        t1.join()
        t2.join()

        assert l.parent == null
    }

    void testDispose() {
        testInEDT {
            def swing = new SwingBuilder()

            swing.frame(id: 'frame').pack()
            swing.dialog(id: 'dialog').pack()
            swing.window(id: 'window').pack()

            //TODO check bind and model
            assert swing.frame.isDisplayable()
            assert swing.dialog.isDisplayable()
            assert swing.window.isDisplayable()
            swing.dispose()

            //TODO check bind and model
            assert !swing.frame.isDisplayable()
            assert !swing.dialog.isDisplayable()
            assert !swing.window.isDisplayable()

        }
    }

    void testPackAndShow() {
        testInEDT {
            def swing = new SwingBuilder()

            swing.frame(id: 'frame', pack: true)
            swing.dialog(id: 'dialog', pack: true)
            swing.window(id: 'window', pack: true)

            assert swing.frame.isDisplayable()
            assert swing.dialog.isDisplayable()
            assert swing.window.isDisplayable()
            swing.dispose()

            swing.frame(id: 'frame', show: true)
            swing.dialog(id: 'dialog', show: true)
            swing.window(id: 'window', show: true)

            assert swing.frame.visible
            assert swing.dialog.visible
            assert swing.window.visible
            swing.dispose()

            swing.frame(id: 'frame', pack: true, show: true)
            swing.dialog(id: 'dialog', pack: true, show: true)
            swing.window(id: 'window', pack: true, show: true)

            assert swing.frame.visible
            assert swing.dialog.visible
            assert swing.window.visible
            swing.dispose()
        }
    }

    void testContainment() {
        testInEDT {
            def swing = new SwingBuilder()

            def topLevel = [
                    "window",
                    "frame",
                    "dialog",
                    "internalFrame",
            ]

            def containers = [
                    "hbox",
                    "box",
                    "desktopPane",
                    "layeredPane",
                    "panel",
                    "popupMenu",
                    //"scrollPane",
                    "splitPane",
                    "tabbedPane",
                    "toolBar",
                    "viewport",
            ]

            def components = [
                    "comboBox",
                    "formattedTextField",
                    "glue",
                    "hbox",
                    "hglue",
                    "hstrut",
                    "rigidArea",
                    "separator",
                    "vbox",
                    "vglue",
                    "vstrut",
                    "box",
                    "colorChooser",
                    "desktopPane",
                    "fileChooser",
                    "internalFrame",
                    "layeredPane",
                    "list",
                    "menu",
                    //"menuBar",
                    "optionPane",
                    "panel",
                    //"popupMenu",
                    "progressBar",
                    "scrollBar",
                    "scrollPane",
                    "slider",
                    "spinner",
                    "splitPane",
                    "tabbedPane",
                    "table",
                    "toolBar",
                    "tree",
                    "viewport",
                    "editorPane",
                    "label",
                    "passwordField",
                    "textArea",
                    "textField",
                    "textPane",
            ]


            topLevel.each {parentWidget ->
                components.each { childWidget ->
                    //println "$parentWidget / $childWidget"
                    def child
                    def parent = swing."$parentWidget" { child = "$childWidget"() }
                    assert parent.contentPane == child.parent
                }
            }

            containers.each {parentWidget ->
                components.each { childWidget ->
                    //println "$parentWidget / $childWidget"
                    def child
                    def parent = swing."$parentWidget" { child = "$childWidget"() }
                    assert parent == child.parent
                }
            }

            components.each { childWidget ->
                //println "scrollPane / $childWidget"

                def child
                def parent = swing.scrollPane { child = "$childWidget"() }
                if (childWidget == 'viewport') {
                    assert parent.viewport == child
                } else {
                    assert parent.viewport == child.parent
                }
            }
        }
    }

    void testMenus() {
        testInEDT {
            def swing = new SwingBuilder()

            def frame = swing.frame {
                menuBar(id: 'bar') {
                    menu('menu', id: 'menu') {
                        menuItem('item', id: 'item')
                        checkBoxMenuItem('check', id: 'check')
                        radioButtonMenuItem('radio', id: 'radio')
                        separator(id: 'sep')
                        menu('subMenu', id: 'subMenu') {
                            menuItem('item', id: 'subitem')
                            checkBoxMenuItem('check', id: 'subcheck')
                            radioButtonMenuItem('radio', id: 'subradio')
                            separator(id: 'subsep')
                            menu('subSubMenu', id: 'subSubMenu')
                        }
                    }
                }
            }

            assert frame.JMenuBar == swing.bar
            assert swing.bar.menuCount == 1
            assert swing.bar.getMenu(0) == swing.menu

            assert swing.menu.itemCount == 5
            assert swing.menu.getItem(0) == swing.item
            assert swing.menu.getItem(1) == swing.check
            assert swing.menu.getItem(2) == swing.radio
            assert swing.menu.getItem(3) == null // not a menu item
            assert swing.menu.getMenuComponent(3) == swing.sep
            assert swing.menu.getItem(4) == swing.subMenu
            shouldFail { swing.menu.getItem(5) }

            assert swing.subMenu.itemCount == 5
            assert swing.subMenu.getItem(0) == swing.subitem
            assert swing.subMenu.getItem(1) == swing.subcheck
            assert swing.subMenu.getItem(2) == swing.subradio
            assert swing.subMenu.getItem(3) == null // not a menu item
            assert swing.subMenu.getMenuComponent(3) == swing.subsep
            assert swing.subMenu.getItem(4) == swing.subSubMenu
        }
    }

    void testLookAndFeel() {
        testInEDT {
            def swing = new SwingBuilder()

            def oldLAF = UIManager.getLookAndFeel()
            try {
                // test LAFs guaranteed to be everywhere
                swing.lookAndFeel('metal')
                swing.lookAndFeel('system')
                swing.lookAndFeel('crossPlatform')

                // test alternate invocations...
                swing.lookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel())
                shouldFail {
                    swing.lookAndFeel(this)
                }
                swing.lookAndFeel('javax.swing.plaf.metal.MetalLookAndFeel')
                swing.lookAndFeel("${'j'}avax.swing.plaf.metal.MetalLookAndFeel")
                shouldFail {
                    swing.lookAndFeel('BogusLookAndFeel')
                }

                // test Metal Themeing and aux attributes
                swing.lookAndFeel('metal', theme: 'steel', boldFonts: false)
                shouldFail {
                    swing.lookAndFeel('metal', theme: 'steel', boldFonts: false, bogusAttribute: 'bad bad bad')
                }

                // test setup via attribute alone
                swing.lookAndFeel(lookAndFeel: 'metal', theme: 'steel', boldFonts: false)

                // test Look and Feel Closure
                swing.lookAndFeel('metal') { laf ->
                    assert laf instanceof MetalLookAndFeel
                }
                swing.lookAndFeel('metal', boldFonts: true) { laf ->
                    assert laf instanceof MetalLookAndFeel
                }
                swing.lookAndFeel(lookAndFeel: 'metal', boldFonts: true) { laf ->
                    assert laf instanceof MetalLookAndFeel
                }
                shouldFail {
                    swing.lookAndFeel() {laf ->
                        "do" + "Nothing"
                    }
                }
            } finally {
                UIManager.setLookAndFeel(oldLAF)
            }
        }
    }

    void testMultiLookAndFeel() {
        testInEDT {
            def swing = new SwingBuilder()

            def oldLAF = UIManager.getLookAndFeel()
            try {
                def mlaf = new MetalLookAndFeel()
                assert swing.lookAndFeel('metal', 'bogus') instanceof MetalLookAndFeel
                assert swing.lookAndFeel('bogus', 'metal') instanceof MetalLookAndFeel
                assert swing.lookAndFeel(['metal'], 'bogus') instanceof MetalLookAndFeel
                assert swing.lookAndFeel('bogus', ['metal']) instanceof MetalLookAndFeel
                assert swing.lookAndFeel(['metal', [boldFonts: false]], 'bogus') instanceof MetalLookAndFeel
                assert swing.lookAndFeel('bogus', ['metal', [boldFonts: false]]) instanceof MetalLookAndFeel
                assert swing.lookAndFeel(mlaf, 'bogus') instanceof MetalLookAndFeel
                assert swing.lookAndFeel('bogus', mlaf) instanceof MetalLookAndFeel
                assert swing.lookAndFeel([mlaf], 'bogus') instanceof MetalLookAndFeel
                assert swing.lookAndFeel('bogus', [mlaf]) instanceof MetalLookAndFeel
                assert swing.lookAndFeel([mlaf, [boldFonts: false]], 'bogus') instanceof MetalLookAndFeel
                assert swing.lookAndFeel('bogus', [mlaf, [boldFonts: false]]) instanceof MetalLookAndFeel
                assert swing.lookAndFeel('bogus', 'fake', 'impossible') == null
            } finally {
                UIManager.setLookAndFeel(oldLAF)
            }
        }
    }

    void testBorders() {
        testInEDT {
            def swing = new SwingBuilder()

            // classic smoke test, try every valid combination and look for smoke...
            swing.frame {
                lineBorder(color: Color.BLACK, parent: true)
                lineBorder(color: Color.BLACK, thickness: 4, parent: true)
                lineBorder(color: Color.BLACK, roundedCorners: true, parent: true)
                lineBorder(color: Color.BLACK, thickness: 4, roundedCorners: true, parent: true)
                raisedBevelBorder(parent: true)
                raisedBevelBorder(highlight: Color.GREEN, shadow: Color.PINK, parent: true)
                raisedBevelBorder(highlightOuter: Color.GREEN, highlightInner: Color.RED, shadowOuter: Color.PINK, shadowInner: Color.BLUE, parent: true)
                loweredBevelBorder(parent: true)
                loweredBevelBorder(highlight: Color.GREEN, shadow: Color.PINK, parent: true)
                loweredBevelBorder(highlightOuter: Color.GREEN, highlightInner: Color.RED, shadowOuter: Color.PINK, shadowInner: Color.BLUE, parent: true)
                etchedBorder(parent: true)
                etchedBorder(highlight: Color.GREEN, shadow: Color.PINK, parent: true)
                loweredEtchedBorder(parent: true)
                loweredEtchedBorder(highlight: Color.GREEN, shadow: Color.PINK, parent: true)
                raisedEtchedBorder(parent: true)
                raisedEtchedBorder(highlight: Color.GREEN, shadow: Color.PINK, parent: true)
                titledBorder("Title 1", parent: true)
                titledBorder(title: "Title 2", parent: true)
                titledBorder("Title 3", position: 'bottom', parent: true)
                titledBorder(title: "Title 4", position: 'aboveBottom', parent: true)
                titledBorder("Title 5", position: TitledBorder.ABOVE_TOP, parent: true)
                titledBorder(title: "Title 6", position: TitledBorder.BOTTOM, parent: true)
                titledBorder("Title 7", justification: 'right', parent: true)
                titledBorder(title: "Title 8", justification: 'acenter', parent: true)
                titledBorder("Title 9", justification: TitledBorder.TRAILING, parent: true)
                titledBorder(title: "Title A", justification: TitledBorder.LEADING, parent: true)
                titledBorder("Title B", border: lineBorder(color: Color.RED, thickness: 6), parent: true)
                titledBorder(title: "Title C", border: lineBorder(color: Color.BLUE, thickness: 6), parent: true)
                titledBorder("Title D", color: Color.CYAN, parent: true)
                titledBorder(title: "Title E", border: lineBorder(color: Color.BLUE, thickness: 6), parent: true)
                emptyBorder(6, parent: true)
                emptyBorder([3, 5, 6, 9], parent: true)
                emptyBorder(top: 6, left: 5, bottom: 6, right: 9, parent: true)
                compoundBorder([titledBorder("single")], parent: true)
                compoundBorder([titledBorder("outer"), titledBorder("inner")], parent: true)
                compoundBorder(outer: titledBorder("outer"), inner: titledBorder("inner"), parent: true)
                compoundBorder([titledBorder("outer"), titledBorder("middle"), titledBorder("inner")], parent: true)
                matteBorder(Color.MAGENTA, size: 7, parent: true)
                matteBorder(7, color: Color.MAGENTA, parent: true)
                matteBorder(javax.swing.plaf.metal.MetalIconFactory.getCheckBoxIcon(), size: 9, parent: true)
                matteBorder(9, icon: javax.swing.plaf.metal.MetalIconFactory.getCheckBoxIcon(), parent: true)

                lineBorder(color: Color.BLACK)
                lineBorder(color: Color.BLACK, thickness: 4)
                lineBorder(color: Color.BLACK, roundedCorners: true)
                lineBorder(color: Color.BLACK, thickness: 4, roundedCorners: true)
                raisedBevelBorder()
                raisedBevelBorder(highlight: Color.GREEN, shadow: Color.PINK)
                raisedBevelBorder(highlightOuter: Color.GREEN, highlightInner: Color.RED, shadowOuter: Color.PINK, shadowInner: Color.BLUE)
                loweredBevelBorder()
                loweredBevelBorder(highlight: Color.GREEN, shadow: Color.PINK)
                loweredBevelBorder(highlightOuter: Color.GREEN, highlightInner: Color.RED, shadowOuter: Color.PINK, shadowInner: Color.BLUE)
                etchedBorder()
                etchedBorder(highlight: Color.GREEN, shadow: Color.PINK)
                loweredEtchedBorder()
                loweredEtchedBorder(highlight: Color.GREEN, shadow: Color.PINK)
                raisedEtchedBorder()
                raisedEtchedBorder(highlight: Color.GREEN, shadow: Color.PINK)
                titledBorder("Title 1")
                titledBorder(title: "Title 2")
                titledBorder("Title 3", position: 'bottom')
                titledBorder(title: "Title 4", position: 'aboveBottom')
                titledBorder("Title 5", position: TitledBorder.ABOVE_TOP)
                titledBorder(title: "Title 6", position: TitledBorder.BOTTOM)
                titledBorder("Title 7", justification: 'right')
                titledBorder(title: "Title 8", justification: 'acenter')
                titledBorder("Title 9", justification: TitledBorder.TRAILING)
                titledBorder(title: "Title A", justification: TitledBorder.LEADING)
                titledBorder("Title B", border: lineBorder(color: Color.RED, thickness: 6))
                titledBorder(title: "Title C", border: lineBorder(color: Color.BLUE, thickness: 6))
                titledBorder("Title D", color: Color.CYAN)
                titledBorder(title: "Title E", border: lineBorder(color: Color.BLUE, thickness: 6))
                emptyBorder(6)
                emptyBorder([3, 5, 6, 9])
                emptyBorder(top: 6, left: 5, bottom: 6, right: 9)
                compoundBorder([titledBorder("single")])
                compoundBorder([titledBorder("outer"), titledBorder("inner")])
                compoundBorder(outer: titledBorder("outer"), inner: titledBorder("inner"))
                compoundBorder([titledBorder("outer"), titledBorder("middle"), titledBorder("inner")])
                matteBorder(Color.MAGENTA, size: 7)
                matteBorder(7, color: Color.MAGENTA)
                matteBorder(javax.swing.plaf.metal.MetalIconFactory.getCheckBoxIcon(), size: 9)
                matteBorder(9, icon: javax.swing.plaf.metal.MetalIconFactory.getCheckBoxIcon())
            }
        }
    }

    void testBorderAttachment() {
        testInEDT {
            def swing = new SwingBuilder()

            swing.frame(id: 'frame') {
                raisedBevelBorder()
            }
            assert swing.frame.contentPane.border == null

            swing.frame(id: 'frame') {
                raisedBevelBorder(parent: true)
            }
            assert swing.frame.contentPane.border != null

            swing.panel(id: 'panel') {
                raisedBevelBorder()
            }
            assert swing.panel.border == null

            swing.panel(id: 'panel') {
                raisedBevelBorder(parent: true)
            }
            assert swing.panel.border != null
        }
    }

    void testRenderer() {
        testInEDT {
            def swing = new SwingBuilder()

            int count = 0
            def lcr = swing.listCellRenderer {
                label()
                onRender {
                    count++
                }
            }

            def f = swing.frame(pack: true, show: true) {
                ls = list(items: ["one", "two"], cellRenderer: lcr)
            }
            assert count == 2
            f.dispose()

            count = 0
            def lcr2 = swing.listCellRenderer {
                label()
                button()
                onRender {
                    count++
                    return (children[row % 2])
                }
            }

            assert lcr2.getListCellRendererComponent(swing.ls, "x", 0, false, false) instanceof javax.swing.JLabel
            assert lcr2.getListCellRendererComponent(swing.ls, "x", 1, false, false) instanceof javax.swing.JButton

        }
    }

    void testNoParent() {
        testInEDT {
            def swing = new SwingBuilder()
            def panel = swing.panel {
                button(id: "b1")
                noparent {
                    button(id: "b2")
                }
            }
            assert panel.componentCount == 1
            assert swing.b1.parent == panel
            assert !swing.b2.parent
        }
    }

    void testClientProperties() {
        testInEDT {
            def swing = new SwingBuilder()
            def button = swing.button(clientPropertyPropA: "A")
            assert button.getClientProperty("PropA") == "A"

            button = swing.button("clientPropertyPropWith.dotAnd:colon": ".&:")
            assert button.getClientProperty("PropWith.dotAnd:colon") == ".&:"

            button = swing.button(clientProperties: [prop1: "1", prop2: "2"])
            assert button.getClientProperty("prop1") == "1"
            assert button.getClientProperty("prop2") == "2"
        }
    }

    void testKeyStrokeAction() {
        testInEDT {
            def swing = new SwingBuilder()

            def noop = swing.action(name: "KeyAction", closure: {})

            // component as value
            def button = swing.button()
            swing.keyStrokeAction(button,
                    actionKey: "asValue",
                    keyStroke: "V",
                    action: noop)
            assert button.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke("V")) == "asValue"
            assert button.actionMap.get("asValue") == noop

            // component as property
            swing.keyStrokeAction(component: button,
                    actionKey: "asProperty",
                    keyStroke: "P",
                    action: noop)
            assert button.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke("P")) == "asProperty"
            assert button.actionMap.get("asProperty") == noop

            // nested in component
            button = swing.button {
                keyStrokeAction(actionKey: "nested",
                        keyStroke: "N",
                        action: noop)
            }
            assert button.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke("N")) == "nested"
            assert button.actionMap.get("nested") == noop

            // nested in component + shortcut
            button = swing.button {
                keyStrokeAction(actionKey: "nested_shortcut",
                        keyStroke: shortcut("N"),
                        action: noop)
            }
            assert button.getInputMap(JComponent.WHEN_FOCUSED).get(swing.shortcut("N")) == "nested_shortcut"
            assert button.actionMap.get("nested_shortcut") == noop

            // kstroke as GString
            swing.keyStrokeAction(component: button,
                    actionKey: "GringKeyStroke",
                    keyStroke: "G",
                    action: noop)
            assert button.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke("G")) == "GringKeyStroke"
            assert button.actionMap.get("GringKeyStroke") == noop

        }
    }

    void testAutomaticNameBasedOnIdAttribute() {
        testInEDT {
            def swing = new SwingBuilder()

            def node = swing.button(id: 'groovy')
            assert node == swing.groovy
            assert node.name == 'groovy'

            node = swing.button(id: 'groovy', name: 'java')
            assert node == swing.groovy
            assert node.name == 'java'

            node = swing.map(id: 'aMap')
            assert node == swing.aMap
            assert !node.name
        }
    }
}
