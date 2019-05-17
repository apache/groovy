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
package groovy.inspect.swingui

import groovy.lang.GroovyClassLoader.ClassCollector
import groovy.swing.SwingBuilder
import groovy.transform.CompileStatic
import org.apache.groovy.io.StringBuilderWriter
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.ASMifier
import org.objectweb.asm.util.TraceClassVisitor

import javax.swing.*
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreeSelectionModel
import java.awt.*
import java.awt.event.KeyEvent
import java.util.List
import java.util.prefs.Preferences
import java.util.regex.Pattern

import static java.awt.GridBagConstraints.BOTH
import static java.awt.GridBagConstraints.HORIZONTAL
import static java.awt.GridBagConstraints.NONE
import static java.awt.GridBagConstraints.NORTHEAST
import static java.awt.GridBagConstraints.NORTHWEST
import static java.awt.GridBagConstraints.WEST

/**
 * This object is a GUI for looking at the AST that Groovy generates. 
 *
 * Usage: java groovy.inspect.swingui.AstBrowser [filename]
 *         where [filename] is an existing Groovy script. 
 */
@Deprecated
class AstBrowser {

    private static final String BYTECODE_MSG_SELECT_NODE = '// Please select a class node in the tree view.'
    private static final String NO_BYTECODE_AVAILABLE_AT_THIS_PHASE = '// No bytecode available at this phase'

    private inputArea, rootElement, decompiledSource, jTree, propertyTable, splitterPane, mainSplitter, bytecodeView, asmifierView
    boolean showScriptFreeForm, showScriptClass, showClosureClasses, showTreeView, showIndyBytecode
    GeneratedBytecodeAwareGroovyClassLoader classLoader
    def prefs = new AstBrowserUiPreferences()
    Action refreshAction
    private CompilerConfiguration config

    AstBrowser(inputArea, rootElement, classLoader, config = null) {
        this.inputArea = inputArea
        this.rootElement = rootElement
        this.classLoader = new GeneratedBytecodeAwareGroovyClassLoader(classLoader)
        this.config = config
    }

    SwingBuilder swing
    def frame

    static void main(args) {

        if (!args) {
            println 'Usage: java groovy.inspect.swingui.AstBrowser [filename]\nwhere [filename] is a Groovy script'
        } else {
            def file = new File((String) args[0])
            if (!file.exists()) {
                println "File $args[0] cannot be found."
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                new AstBrowser(null, null, new GroovyClassLoader()).run({file.text}, file.path)
            }
        }
    }

    void initAuxViews() {
        bytecodeView.textEditor.text = BYTECODE_MSG_SELECT_NODE
        asmifierView.textEditor.text = BYTECODE_MSG_SELECT_NODE
    }

    void run(Closure script) {
        run(script, null)
    }

    void run(Closure script, String name) {

        swing = new SwingBuilder()
        def phasePicker

        showScriptFreeForm = prefs.showScriptFreeForm
        showScriptClass = prefs.showScriptClass
        showClosureClasses = prefs.showClosureClasses
        showTreeView = prefs.showTreeView
        showIndyBytecode = prefs.showIndyBytecode

        frame = swing.frame(title: 'Groovy AST Browser' + (name ? " - $name" : ''),
                location: prefs.frameLocation,
                size: prefs.frameSize,
                iconImage: swing.imageIcon(groovy.ui.Console.ICON_PATH).image,
                defaultCloseOperation: WindowConstants.DISPOSE_ON_CLOSE,
                windowClosing: { event -> prefs.save(frame, splitterPane, mainSplitter, showScriptFreeForm, showScriptClass, showClosureClasses, phasePicker.selectedItem, showTreeView, showIndyBytecode) }) {

            menuBar {
                menu(text: 'Show Script', mnemonic: 'S') {
                    checkBoxMenuItem(selected: showScriptFreeForm) {
                        action(name: 'Free Form', closure: this.&showScriptFreeForm,
                                mnemonic: 'F',)
                    }
                    checkBoxMenuItem(selected: showScriptClass) {
                        action(name: 'Class Form', closure: this.&showScriptClass,
                                mnemonic: 'C')
                    }
                    checkBoxMenuItem(selected: showClosureClasses) {
                        action(name: 'Generated Closure/Lambda Classes', closure: this.&showClosureClasses,
                                mnemonic: 'G')
                    }
                    checkBoxMenuItem(selected: showTreeView) {
                        action(name: 'Tree View', closure: this.&showTreeView,
                                mnemonic: 'T')
                    }
                    checkBoxMenuItem(selected: showIndyBytecode) {
                        action(name: 'Generate Indy Bytecode', closure: this.&showIndyBytecode,
                                mnemonic: 'I')
                    }
                }
                menu(text: 'View', mnemonic: 'V') {
                    menuItem {action(name: 'Larger Font', closure: this.&largerFont, mnemonic: 'L', accelerator: shortcut('shift L'))}
                    menuItem {action(name: 'Smaller Font', closure: this.&smallerFont, mnemonic: 'S', accelerator: shortcut('shift S'))}
                    menuItem {
                        refreshAction = action(name: 'Refresh', closure: {
                            decompile(phasePicker.selectedItem.phaseId, script())
                            compile(jTree, script(), phasePicker.selectedItem.phaseId)
                            initAuxViews()
                        }, mnemonic: 'R', accelerator: KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0))
                    }
                }
                menu(text: 'Help', mnemonic: 'H') {
                    menuItem {action(name: 'About', closure: this.&showAbout, mnemonic: 'A')}
                }
            }
            panel {
                gridBagLayout()
                label(text: 'At end of Phase: ',
                        constraints: gbc(gridx: 0, gridy: 0, gridwidth: 1, gridheight: 1, weightx: 0, weighty: 0, anchor: WEST, fill: HORIZONTAL, insets: [2, 2, 2, 2]))
                phasePicker = comboBox(items: CompilePhaseAdapter.values(),
                        selectedItem: prefs.selectedPhase,
                        actionPerformed: {
                            // reset text to the default as the phase change removes the focus from the class node
                            initAuxViews()

                            decompile(phasePicker.selectedItem.phaseId, script())
                            compile(jTree, script(), phasePicker.selectedItem.phaseId)
                        },
                        constraints: gbc(gridx: 1, gridy: 0, gridwidth: 1, gridheight: 1, weightx: 1.0, weighty: 0, anchor: NORTHWEST, fill: NONE, insets: [2, 2, 2, 2]))
                button(text: 'Refresh',
                        actionPerformed: {
                            decompile(phasePicker.selectedItem.phaseId, script())
                            compile(jTree, script(), phasePicker.selectedItem.phaseId)
                            initAuxViews()
                        },
                        constraints: gbc(gridx: 2, gridy: 0, gridwidth: 1, gridheight: 1, weightx: 0, weighty: 0, anchor: NORTHEAST, fill: NONE, insets: [2, 2, 2, 3]))
                splitterPane = splitPane(
                        visible: showTreeView, 
                        leftComponent: scrollPane {
                            jTree = tree(
                                    name: 'AstTreeView', rowHeight: 0, /* force recalc */
                                    model: new DefaultTreeModel(new DefaultMutableTreeNode('Loading...'))) {}
                        },
                        rightComponent: scrollPane {
                            propertyTable = table {
                                tableModel(list: [[:]]) {
                                    propertyColumn(header: 'Name', propertyName: 'name')
                                    propertyColumn(header: 'Value', propertyName: 'value')
                                    propertyColumn(header: 'Type', propertyName: 'type')
                                }
                            }
                        }
                ) { }
                mainSplitter = splitPane(
                        orientation: JSplitPane.VERTICAL_SPLIT,
                        topComponent: splitterPane,
                        bottomComponent: tabbedPane {
                            widget(decompiledSource = new groovy.ui.ConsoleTextEditor(editable: false, showLineNumbers: false), title:'Source')
                            widget(bytecodeView = new groovy.ui.ConsoleTextEditor(editable: false, showLineNumbers: false), title:getByteCodeTitle())
                            widget(asmifierView = new groovy.ui.ConsoleTextEditor(editable: false, showLineNumbers: false), title:getASMifierTitle())
                        },
                        constraints: gbc(gridx: 0, gridy: 2, gridwidth: 3, gridheight: 1, weightx: 1.0, weighty: 1.0, anchor: NORTHWEST, fill: BOTH, insets: [2, 2, 2, 2])) { }

            }
        }

        initAuxViews()

        propertyTable.model.rows.clear() //for some reason this suppress an empty row

        jTree.cellRenderer.setLeafIcon(swing.imageIcon(groovy.ui.Console.NODE_ICON_PATH))
        jTree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        jTree.addTreeSelectionListener({ TreeSelectionEvent e ->

            propertyTable.model.rows.clear()
            propertyTable.columnModel.getColumn(1).cellRenderer = new ButtonOrDefaultRenderer()
            propertyTable.columnModel.getColumn(1).cellEditor = new ButtonOrTextEditor()
            TreeNode node = jTree.lastSelectedPathComponent
            if (node instanceof TreeNodeWithProperties) {
                def titleSuffix = node.properties.find{ it[0] == 'text' }?.get(1)
                for (it in node.properties) {
                    def propList = it
                    if (propList[2] == "ListHashMap" && propList[1] != 'null' && propList[1] != '[:]') {
                        //If the class is a ListHashMap, make it accessible in a new frame through a button
                        def btnPanel = swing.button(
                            text: "See key/value pairs",
                            actionPerformed: {
                                def mapTable
                                String title = titleSuffix ? propList[0] + " (" + titleSuffix + ")" : propList[0]
                                def props = swing.frame(title: title, defaultCloseOperation: JFrame.DISPOSE_ON_CLOSE,
                                        show: true, locationRelativeTo: null) {
                                    lookAndFeel("system")
                                    panel {
                                        scrollPane() {
                                            mapTable = swing.table() {
                                                tableModel(list: [[:]]) {
                                                    propertyColumn(header: 'Name', propertyName: 'name')
                                                    propertyColumn(header: 'Value', propertyName: 'value')
                                                }
                                            }
                                        }
                                    }
                                }
                                mapTable.model.rows.clear()
                                propList[1].substring(1, propList[1].length() - 1).tokenize(',').each {
                                    def kv = it.tokenize(':')
                                    if (kv)
                                        mapTable.model.rows << ["name": kv[0], "value": kv[1]]
                                }
                                props.pack()
                            })
                        propertyTable.model.rows << ["name": propList[0], "value": btnPanel, "type": propList[2]]
                        btnPanel.updateUI()
                    } else {
                        propertyTable.model.rows << ["name": it[0], "value": it[1], "type": it[2]]
                    }
                }

                if (inputArea && rootElement) {
                    // get the line / column information to select the text represented by the current selected node
                    def lineInfo = node.properties.findAll { it[0] in ['lineNumber', 'columnNumber', 'lastLineNumber', 'lastColumnNumber'] }
                    def lineInfoMap = lineInfo.inject([:]) { map, info -> map[(info[0])] = Integer.valueOf(info[1]); return map }

                    // when there are valid line / column information (ie. != -1), create a selection in the input area
                    if (!lineInfoMap.every { k, v -> v == -1 }) {
                        def startOffset = rootElement.getElement(lineInfoMap.lineNumber - 1).startOffset
                        inputArea.setCaretPosition(startOffset + lineInfoMap.columnNumber - 1)

                        def endOffset = rootElement.getElement(lineInfoMap.lastLineNumber - 1).startOffset
                        inputArea.moveCaretPosition(endOffset + lineInfoMap.lastColumnNumber - 1)
                    } else {
                        // if no line number is provided, unselect the current selection
                        // but keep the caret at the same position
                        inputArea.moveCaretPosition(inputArea.getCaretPosition())
                    }
                }

                if (node.classNode || node.methodNode) {
                    bytecodeView.textEditor.text = '// Loading bytecode ...'
                    asmifierView.textEditor.text = '// Loading ASMifier\'s output ...'
                    boolean showOnlyMethodCode = node.methodNode

                    swing.doOutside {
                        def className = showOnlyMethodCode ? node.getPropertyValue('declaringClass') : node.getPropertyValue('name')
                        def bytecode = classLoader.getBytecode(className)
                        if (bytecode) {
                            def methodName = node.getPropertyValue('name')
                            def methodDescriptor = node.getPropertyValue('descriptor')
                            boolean isMethodNameAndMethodDescriptorAvailable = methodName && methodDescriptor

                            String bytecodeSource = generateSource(bytecode, {writer -> new TraceClassVisitor(new PrintWriter(writer))})
                            showSource(bytecodeView, bytecodeSource, showOnlyMethodCode, isMethodNameAndMethodDescriptorAvailable, {"^.*\\n.*${Pattern.quote(methodName + methodDescriptor)}[\\s\\S]*?\\n[}|\\n]"})

                            String asmifierSource = generateSource(bytecode, {writer -> new TraceClassVisitor(null, new ASMifier(), new PrintWriter(writer))})
                            showSource(asmifierView, asmifierSource, showOnlyMethodCode, isMethodNameAndMethodDescriptorAvailable, {"^.*\\n.*${Pattern.quote(methodName)}.*?${Pattern.quote(methodDescriptor)}[\\s\\S]*?\\n[}|\\n]"})
                        } else {
                            swing.doLater {
                                bytecodeView.textEditor.text = NO_BYTECODE_AVAILABLE_AT_THIS_PHASE
                                asmifierView.textEditor.text = NO_BYTECODE_AVAILABLE_AT_THIS_PHASE
                            }
                        }
                    }

                } else {
                    bytecodeView.textEditor.text = ''
                    asmifierView.textEditor.text = ''
                }
            }
            propertyTable.model.fireTableDataChanged()
        } as TreeSelectionListener)

        updateFontSize(prefs.decompiledSourceFontSize)

        frame.pack()
        frame.location = prefs.frameLocation
        frame.size = prefs.frameSize
        splitterPane.dividerLocation = prefs.verticalDividerLocation
        mainSplitter.dividerLocation = prefs.horizontalDividerLocation
        frame.visible = true

        String source = script()
        decompile(phasePicker.selectedItem.phaseId, source)
        compile(jTree, source, phasePicker.selectedItem.phaseId)
        jTree.rootVisible = false
        jTree.showsRootHandles = true   // some OS's require this as a step to show nodes

    }

    private static final int INITIAL_CAPACITY = 64 * 1024 // 64K
    private String generateSource(byte[] bytecode, getVisitor) {
        def sw = new StringBuilderWriter(INITIAL_CAPACITY) // the generated code of `println 123` occupies about 618 bytes, so we should increase the initial capacity to 64K
        new ClassReader(bytecode).accept(getVisitor(sw), 0)
        return sw.toString()
    }

    private void showSource(view, String source, boolean showOnlyMethodCode, boolean isMethodNameAndMethodDescriptorAvailable, getPatternStr) {
        swing.doLater {
            view.textEditor.text = source
            if (showOnlyMethodCode && isMethodNameAndMethodDescriptorAvailable) {
                def pattern = Pattern.compile(getPatternStr(), Pattern.MULTILINE)
                def matcher = pattern.matcher(source)
                if (matcher.find()) {
                    view.textEditor.text = source.substring(matcher.start(0), matcher.end(0))
                }
            }

            view.textEditor.caretPosition = 0
        }
    }

    void largerFont(EventObject evt = null) {
        updateFontSize(decompiledSource.textEditor.font.size + 2)
    }

    void smallerFont(EventObject evt = null) {
        updateFontSize(decompiledSource.textEditor.font.size - 2)
    }

    private final updateFontSize = { newFontSize ->
        if (newFontSize > 40) {
            newFontSize = 40
        } else if (newFontSize < 4) {
            newFontSize = 4
        }

        prefs.decompiledSourceFontSize = newFontSize

        def newDecompilerFont = new Font(decompiledSource.textEditor.font.name, decompiledSource.textEditor.font.style, newFontSize)
        decompiledSource.textEditor.font = newDecompilerFont

        def newFont = new Font(jTree.cellRenderer.font.name, jTree.cellRenderer.font.style, newFontSize)
        jTree.cellRenderer.font = newFont
        jTree.model.reload(jTree.model.root)
        propertyTable.tableHeader.font = newFont
        propertyTable.font = newFont
        propertyTable.rowHeight = newFontSize + 2
    }

    void showAbout(EventObject evt) {
        def pane = swing.optionPane()
        def version = GroovySystem.getVersion()
        pane.setMessage('An interactive GUI to explore AST capabilities\nVersion ' + version)
        def dialog = pane.createDialog(frame, 'About Groovy AST Browser')
        dialog.pack()
        dialog.visible = true
    }

    void showScriptFreeForm(EventObject evt) {
        showScriptFreeForm = evt.source.selected
    }

    void showScriptClass(EventObject evt) {
        showScriptClass = evt.source.selected
    }

    void showClosureClasses(EventObject evt)  {
        showClosureClasses = evt.source.selected
    }

    void showTreeView(EventObject evt = null) {
        showTreeView = !showTreeView
        splitterPane.visible = showTreeView
        if (showTreeView) {
            mainSplitter.dividerLocation = 100
        } else {
            mainSplitter.dividerLocation = 0
        }
    }

    void showIndyBytecode(EventObject evt = null) {
        showIndyBytecode = evt.source.selected
        initAuxViews()
        refreshAction.actionPerformed(null)
        updateTabTitles()
    }

    private void updateTabTitles() {
        def tabPane = mainSplitter.bottomComponent
        int tabCount = tabPane.getTabCount()
        for (int i = 0; i < tabCount; i++) {
            def component = tabPane.getComponentAt(i);
            if (bytecodeView.is(component)) {
                tabPane.setTitleAt(i, getByteCodeTitle())
            } else if (asmifierView.is(component)) {
                tabPane.setTitleAt(i, getASMifierTitle())
            }
        }
    }

    private String getByteCodeTitle() {
        'Bytecode' + (showIndyBytecode ? ' (Indy)' : '')
    }

    private String getASMifierTitle() {
        'ASMifier' + (showIndyBytecode ? ' (Indy)' : '')
    }

    void decompile(phaseId, source) {

        decompiledSource.textEditor.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR))
        decompiledSource.textEditor.text = 'Loading...'

        swing.doOutside {
            try {

                String result = new AstNodeToScriptAdapter().compileToScript(source, phaseId, classLoader, showScriptFreeForm, showScriptClass, config)
                swing.doLater {
                    decompiledSource.textEditor.text = result 
                    decompiledSource.textEditor.setCaretPosition(0)
                    decompiledSource.textEditor.setCursor(Cursor.defaultCursor)
                }
            } catch (Throwable t) {
                swing.doLater {
                    decompiledSource.textEditor.text = t.getMessage()
                    decompiledSource.textEditor.setCaretPosition(0)
                    decompiledSource.textEditor.setCursor(Cursor.defaultCursor)
                }
                throw t
            }
        }
    }

    void compile(jTree, String script, int compilePhase) {
        jTree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR))
        def model = jTree.model
        swing.edt {
            def root = model.getRoot()
            root.removeAllChildren()
            root.add(new DefaultMutableTreeNode(new DefaultMutableTreeNode('Loading...')))
            model.reload(root)
        }
        swing.doOutside {
            try {
                def nodeMaker = new SwingTreeNodeMaker()
                def adapter = new ScriptToTreeNodeAdapter(classLoader, showScriptFreeForm, showScriptClass, showClosureClasses, nodeMaker, config)
                classLoader.clearBytecodeTable()
                def result = adapter.compile(script, compilePhase, showIndyBytecode)
                swing.doLater {
                    model.setRoot(result)
                    model.reload()
                    jTree.setCursor(Cursor.defaultCursor)
                }
            } catch (Throwable t) {
                swing.doLater {
                    jTree.setCursor(Cursor.defaultCursor)
                }
                throw t
            }
        }
    }
}

/**
 * This class sets and restores control positions in the browser.
 */
@Deprecated
class AstBrowserUiPreferences {

    final frameLocation
    final frameSize
    final verticalDividerLocation
    final horizontalDividerLocation
    final boolean showScriptFreeForm
    final boolean showTreeView
    final boolean showScriptClass
    final boolean showClosureClasses
    final boolean showIndyBytecode
    int decompiledSourceFontSize
    final CompilePhaseAdapter selectedPhase

    AstBrowserUiPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(AstBrowserUiPreferences)
        frameLocation = [
                prefs.getInt('frameX', 200),
                prefs.getInt('frameY', 200)]
        frameSize = [
                prefs.getInt('frameWidth', 800),
                prefs.getInt('frameHeight', 600)]

        decompiledSourceFontSize = prefs.getInt('decompiledFontSize', 12)
        verticalDividerLocation = Math.max(prefs.getInt('verticalSplitterLocation', 100), 100)
        horizontalDividerLocation = Math.max(prefs.getInt('horizontalSplitterLocation', 100), 100)
        showScriptFreeForm = prefs.getBoolean('showScriptFreeForm', false)
        showScriptClass = prefs.getBoolean('showScriptClass', true)
        showClosureClasses = prefs.getBoolean('showClosureClasses', false)
        showTreeView = prefs.getBoolean('showTreeView', true)
        showIndyBytecode = prefs.getBoolean('showIndyBytecode', false)
        int phase = prefs.getInt('compilerPhase', Phases.SEMANTIC_ANALYSIS)
        selectedPhase = CompilePhaseAdapter.values().find {
            it.phaseId == phase
        }
    }

    def save(frame, vSplitter, hSplitter, scriptFreeFormPref, scriptClassPref, closureClassesPref, CompilePhaseAdapter phase, showTreeView, showIndyBytecode=false) {
        Preferences prefs = Preferences.userNodeForPackage(AstBrowserUiPreferences)
        prefs.putInt('decompiledFontSize', decompiledSourceFontSize as int)
        prefs.putInt('frameX', frame.location.x as int)
        prefs.putInt('frameY', frame.location.y as int)
        prefs.putInt('frameWidth', frame.size.width as int)
        prefs.putInt('frameHeight', frame.size.height as int)
        prefs.putInt('verticalSplitterLocation', vSplitter.dividerLocation)
        prefs.putInt('horizontalSplitterLocation', hSplitter.dividerLocation)
        prefs.putBoolean('showScriptFreeForm', scriptFreeFormPref)
        prefs.putBoolean('showScriptClass', scriptClassPref)
        prefs.putBoolean('showClosureClasses', closureClassesPref)
        prefs.putBoolean('showTreeView', showTreeView)
        prefs.putBoolean('showIndyBytecode', showIndyBytecode)
        prefs.putInt('compilerPhase', phase.phaseId)
    }
}

/**
 * An adapter for the CompilePhase enum that can be entered into a Swing combobox.
 */
@CompileStatic
@Deprecated
enum CompilePhaseAdapter {
    INITIALIZATION(Phases.INITIALIZATION, 'Initialization'),
    PARSING(Phases.PARSING, 'Parsing'),
    CONVERSION(Phases.CONVERSION, 'Conversion'),
    SEMANTIC_ANALYSIS(Phases.SEMANTIC_ANALYSIS, 'Semantic Analysis'),
    CANONICALIZATION(Phases.CANONICALIZATION, 'Canonicalization'),
    INSTRUCTION_SELECTION(Phases.INSTRUCTION_SELECTION, 'Instruction Selection'),
    CLASS_GENERATION(Phases.CLASS_GENERATION, 'Class Generation'),
    OUTPUT(Phases.OUTPUT, 'Output'),
    FINALIZATION(Phases.FINALIZATION, 'Finalization')

    final int phaseId
    final String string

    CompilePhaseAdapter(int phaseId, String string) {
        this.phaseId = phaseId
        this.string = string
    }

    String toString() {
        return string
    }
}

/**
 * This class is a TreeNode and you can store additional properties on it.
 */
@CompileStatic
@Deprecated
class TreeNodeWithProperties extends DefaultMutableTreeNode {

    List<List<String>> properties

    /**
     * Creates a tree node and attaches properties to it.
     * @param userObject same as a DefaultMutableTreeNode requires
     * @param properties a list of String lists
     */
    TreeNodeWithProperties(userObject, List<List<String>> properties) {
        super(userObject)
        this.properties = properties
    }

    String getPropertyValue(String name)  {
        def match = properties.find { n, v, t -> name == n }
        return match != null ? match[1] : null
    }

    boolean isClassNode() {
        getPropertyValue('class') in ['class org.codehaus.groovy.ast.ClassNode', 'class org.codehaus.groovy.ast.InnerClassNode']
    }

    boolean isMethodNode() {
        getPropertyValue('class') in ['class org.codehaus.groovy.ast.MethodNode', 'class org.codehaus.groovy.ast.ConstructorNode']
    }
}

/**
 * This interface is used to create tree nodes of various types 
 */
@CompileStatic
@Deprecated
interface AstBrowserNodeMaker<T> {
    T makeNode(Object userObject)

    T makeNodeWithProperties(Object userObject, List<List<String>> properties)
}

/**
 * Creates tree nodes for swing UI  
 */
@CompileStatic
@Deprecated
class SwingTreeNodeMaker implements AstBrowserNodeMaker<DefaultMutableTreeNode> {
    DefaultMutableTreeNode makeNode(Object userObject) {
        new DefaultMutableTreeNode(userObject)
    }

    DefaultMutableTreeNode makeNodeWithProperties(Object userObject, List<List<String>> properties) {
        new TreeNodeWithProperties(userObject, properties)
    }
}

@Deprecated
class BytecodeCollector extends ClassCollector {

    Map<String, byte[]> bytecode

    BytecodeCollector(ClassCollector delegate, Map<String,byte[]> bytecode) {
        super(delegate.cl, delegate.unit, delegate.su)
        this.bytecode = bytecode
    }

    @Override
    protected Class createClass(byte[] code, ClassNode classNode) {
        bytecode[classNode.name] = code
        return super.createClass(code, classNode)
    }

}

@CompileStatic
@Deprecated
class GeneratedBytecodeAwareGroovyClassLoader extends GroovyClassLoader {

    private final Map<String, byte[]> bytecode = new HashMap<String, byte[]>()

    GeneratedBytecodeAwareGroovyClassLoader(final GroovyClassLoader parent) {
        super(parent)
    }

    @Override
    protected ClassCollector createCollector(CompilationUnit unit, SourceUnit su) {
        def collector = super.createCollector(unit, su)
        new BytecodeCollector(collector, bytecode)
    }

    void clearBytecodeTable() {
        bytecode.clear()
    }

    byte[] getBytecode(final String className) {
        bytecode[className]
    }
}
