/*
 * Copyright 2003-2007 the original author or authors.
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
 */

package groovy.inspect.swingui

import groovy.swing.SwingBuilder

import java.awt.Cursor
import java.awt.Font
import java.awt.event.KeyEvent
import java.util.prefs.Preferences
import javax.swing.JSplitPane
import javax.swing.KeyStroke
import javax.swing.UIManager
import javax.swing.WindowConstants
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreeSelectionModel
import org.codehaus.groovy.control.Phases

import java.util.regex.Pattern

import static java.awt.GridBagConstraints.*
import org.codehaus.groovy.ast.ClassNode
import groovy.lang.GroovyClassLoader.ClassCollector
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.SourceUnit
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.TraceClassVisitor

/**
 * This object is a GUI for looking at the AST that Groovy generates. 
 *
 * Usage: java groovy.inspect.swingui.AstBrowser [filename]
 *         where [filename] is an existing Groovy script. 
 *
 * @author Hamlet D'Arcy (hamletdrc@gmail.com)
 * @author Guillaume Laforge, highlighting the code corresponding to a node selected in the tree view
 * @author Roshan Dawrani - separated out the swing UI related code from the model part so model could be used for various UIs
 */

public class AstBrowser {

    private inputArea, rootElement, decompiledSource, jTree, propertyTable, splitterPane, mainSplitter, bytecodeView
    boolean showScriptFreeForm, showScriptClass, showTreeView
    GeneratedBytecodeAwareGroovyClassLoader classLoader
    def prefs = new AstBrowserUiPreferences()

    AstBrowser(inputArea, rootElement, classLoader) {
        this.inputArea = inputArea
        this.rootElement = rootElement
        this.classLoader = new GeneratedBytecodeAwareGroovyClassLoader(classLoader)
    }

    SwingBuilder swing
    def frame

    public static void main(args) {

        if (!args) {
            println "Usage: java groovy.inspect.swingui.AstBrowser [filename]\nwhere [filename] is a Groovy script"
        } else {
            def file = new File((String) args[0])
            if (!file.exists()) {
                println "File $args[0] cannot be found."
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new AstBrowser(null, null, new GroovyClassLoader()).run({file.text}, file.path)
            }
        }
    }

    void run(Closure script) {
        run(script, null)
    }

    void run(Closure script, String name) {

        swing = new SwingBuilder()
        def phasePicker

        showScriptFreeForm = prefs.showScriptFreeForm
        showScriptClass = prefs.showScriptClass
        showTreeView = prefs.showTreeView

        frame = swing.frame(title: 'Groovy AST Browser' + (name ? " - $name" : ''),
                location: prefs.frameLocation,
                size: prefs.frameSize,
                iconImage: swing.imageIcon(groovy.ui.Console.ICON_PATH).image,
                defaultCloseOperation: WindowConstants.DISPOSE_ON_CLOSE,
                windowClosing: { event -> prefs.save(frame, splitterPane, mainSplitter, showScriptFreeForm, showScriptClass, phasePicker.selectedItem, showTreeView) }) {

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
                    checkBoxMenuItem(selected: showTreeView) {
                        action(name: 'Tree View', closure: this.&showTreeView,
                                mnemonic: 'T')
                    }
                }
                menu(text: 'View', mnemonic: 'V') {
                    menuItem() {action(name: 'Larger Font', closure: this.&largerFont, mnemonic: 'L', accelerator: shortcut('shift L'))}
                    menuItem() {action(name: 'Smaller Font', closure: this.&smallerFont, mnemonic: 'S', accelerator: shortcut('shift S'))}
                    menuItem() {
                        action(name: 'Refresh', closure: {
                            decompile(phasePicker.selectedItem.phaseId, script())
                            compile(jTree, script(), phasePicker.selectedItem.phaseId)
                        }, mnemonic: 'R', accelerator: KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0))
                    }
                }
                menu(text: 'Help', mnemonic: 'H') {
                    menuItem() {action(name: 'About', closure: this.&showAbout, mnemonic: 'A')}
                }
            }
            panel {
                gridBagLayout()
                label(text: "At end of Phase: ",
                        constraints: gbc(gridx: 0, gridy: 0, gridwidth: 1, gridheight: 1, weightx: 0, weighty: 0, anchor: WEST, fill: HORIZONTAL, insets: [2, 2, 2, 2]))
                phasePicker = comboBox(items: CompilePhaseAdapter.values(),
                        selectedItem: prefs.selectedPhase,
                        actionPerformed: {
                            // reset text to the default as the phase change removes the focus from the class node
                            bytecodeView.textEditor.text = '// Please select a class node in the tree view.'

                            decompile(phasePicker.selectedItem.phaseId, script())
                            compile(jTree, script(), phasePicker.selectedItem.phaseId)
                        },
                        constraints: gbc(gridx: 1, gridy: 0, gridwidth: 1, gridheight: 1, weightx: 1.0, weighty: 0, anchor: NORTHWEST, fill: NONE, insets: [2, 2, 2, 2]))
                button(text: 'Refresh',
                        actionPerformed: {
                            decompile(phasePicker.selectedItem.phaseId, script())
                            compile(jTree, script(), phasePicker.selectedItem.phaseId)
                        },
                        constraints: gbc(gridx: 2, gridy: 0, gridwidth: 1, gridheight: 1, weightx: 0, weighty: 0, anchor: NORTHEAST, fill: NONE, insets: [2, 2, 2, 3]))
                splitterPane = splitPane(
                        visible: showTreeView, 
                        leftComponent: scrollPane() {
                            jTree = tree(
                                    name: "AstTreeView",
                                    model: new DefaultTreeModel(new DefaultMutableTreeNode("Loading..."))) {}
                        },
                        rightComponent: scrollPane() {
                            propertyTable = table() {
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
                            widget(bytecodeView = new groovy.ui.ConsoleTextEditor(editable: false, showLineNumbers: false), title:'Bytecode')
                        },
                        constraints: gbc(gridx: 0, gridy: 2, gridwidth: 3, gridheight: 1, weightx: 1.0, weighty: 1.0, anchor: NORTHWEST, fill: BOTH, insets: [2, 2, 2, 2])) { }

            }
        }

        bytecodeView.textEditor.text = '// Please select a class node in the tree view.'

        propertyTable.model.rows.clear() //for some reason this suppress an empty row

        jTree.cellRenderer.setLeafIcon(swing.imageIcon(groovy.ui.Console.NODE_ICON_PATH));
        jTree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION;
        jTree.addTreeSelectionListener({ TreeSelectionEvent e ->

            propertyTable.model.rows.clear()
            TreeNode node = jTree.lastSelectedPathComponent
            if (node instanceof TreeNodeWithProperties) {

                node.properties.each {
                    propertyTable.model.rows << ["name": it[0], "value": it[1], "type": it[2]]
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
                    boolean showOnlyMethodCode = node.methodNode

                    swing.doOutside {
                        def className = showOnlyMethodCode ? node.getPropertyValue('declaringClass') : node.getPropertyValue('name')
                        def bytecode = classLoader.getBytecode(className)
                        if (bytecode) {
                            def writer = new StringWriter()
                            def visitor = new TraceClassVisitor(new PrintWriter(writer));
                            def reader = new ClassReader(bytecode)
                            reader.accept(visitor, 0)

                            def source = writer.toString()
                            swing.doLater {
                                bytecodeView.textEditor.text = source

                                if (showOnlyMethodCode)  {
                                    def methodName = node.getPropertyValue('name')
                                    def methodDescriptor = node.getPropertyValue('descriptor')

                                    if (methodName && methodDescriptor)  {
                                        def pattern = Pattern.compile("^.*\\n.*${Pattern.quote(methodName + methodDescriptor)}[\\s\\S]*?\\n[}|\\n]", Pattern.MULTILINE)
                                        def matcher = pattern.matcher(source)
                                        if (matcher.find())  {
                                            bytecodeView.textEditor.text = source.substring(matcher.start(0), matcher.end(0))
                                        }
                                    }
                                }

                                bytecodeView.textEditor.caretPosition = 0
                            }
                        } else {
                            swing.doLater { bytecodeView.textEditor.text = '// No bytecode available at this phase' }
                        }
                    }

                } else {
                    bytecodeView.textEditor.text = ''
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
        frame.show()

        String source = script()
        decompile(phasePicker.selectedItem.phaseId, source)
        compile(jTree, source, phasePicker.selectedItem.phaseId)
        jTree.rootVisible = false
        jTree.showsRootHandles = true   // some OS's require this as a step to show nodes

    }

    void largerFont(EventObject evt = null) {
        updateFontSize(decompiledSource.textEditor.font.size + 2)
    }

    void smallerFont(EventObject evt = null) {
        updateFontSize(decompiledSource.textEditor.font.size - 2)
    }

    private updateFontSize = {newFontSize ->
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
        pane.setMessage('An interactive GUI to explore AST capabilities.')
        def dialog = pane.createDialog(frame, 'About Groovy AST Browser')
        dialog.show()
    }

    void showScriptFreeForm(EventObject evt) {
        showScriptFreeForm = evt.source.selected
    }

    void showScriptClass(EventObject evt) {
        showScriptClass = evt.source.selected
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

    void decompile(phaseId, source) {

        decompiledSource.textEditor.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        decompiledSource.textEditor.text = 'Loading...';

        swing.doOutside {
            try {

                String result = new AstNodeToScriptAdapter().compileToScript(source, phaseId, classLoader, showScriptFreeForm, showScriptClass)
                swing.doLater {
                    decompiledSource.textEditor.text = result 
                    decompiledSource.textEditor.setCaretPosition(0)
                    decompiledSource.textEditor.setCursor(Cursor.defaultCursor);
                }
            } catch (Throwable t) {
                swing.doLater {
                    decompiledSource.textEditor.text = t.getMessage();
                    decompiledSource.textEditor.setCaretPosition(0)
                    decompiledSource.textEditor.setCursor(Cursor.defaultCursor);
                }
                throw t
            }
        }
    }

    void compile(jTree, String script, int compilePhase) {
        jTree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR))
        def model = jTree.model
        swing.edt {
            def root = model.getRoot();
            root.removeAllChildren()
            root.add(new DefaultMutableTreeNode(new DefaultMutableTreeNode('Loading...')));
            model.reload(root);
        }
        swing.doOutside {
            try {
                def nodeMaker = new SwingTreeNodeMaker()
                def adapter = new ScriptToTreeNodeAdapter(classLoader, showScriptFreeForm, showScriptClass, nodeMaker)
                classLoader.clearBytecodeTable()
                def result = adapter.compile(script, compilePhase)
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
 *
 * @author Hamlet D'Arcy
 */
class AstBrowserUiPreferences {

    final def frameLocation
    final def frameSize
    final def verticalDividerLocation
    final def horizontalDividerLocation
    final boolean showScriptFreeForm
    final boolean showTreeView
    final boolean showScriptClass
    int decompiledSourceFontSize
    final CompilePhaseAdapter selectedPhase

    def AstBrowserUiPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(AstBrowserUiPreferences)
        frameLocation = [
                prefs.getInt("frameX", 200),
                prefs.getInt("frameY", 200)]
        frameSize = [
                prefs.getInt("frameWidth", 800),
                prefs.getInt("frameHeight", 600)]

        decompiledSourceFontSize = prefs.getInt("decompiledFontSize", 12)
        verticalDividerLocation = Math.max(prefs.getInt("verticalSplitterLocation", 100), 100)
        horizontalDividerLocation = Math.max(prefs.getInt("horizontalSplitterLocation", 100), 100)
        showScriptFreeForm = prefs.getBoolean("showScriptFreeForm", false)
        showScriptClass = prefs.getBoolean("showScriptClass", true)
        showTreeView = prefs.getBoolean("showTreeView", true)
        int phase = prefs.getInt('compilerPhase', Phases.SEMANTIC_ANALYSIS)
        selectedPhase = CompilePhaseAdapter.values().find {
            it.phaseId == phase
        }
    }

    def save(frame, vSplitter, hSplitter, scriptFreeFormPref, scriptClassPref, CompilePhaseAdapter phase, showTreeView) {
        Preferences prefs = Preferences.userNodeForPackage(AstBrowserUiPreferences)
        prefs.putInt("decompiledFontSize", decompiledSourceFontSize as int)
        prefs.putInt("frameX", frame.location.x as int)
        prefs.putInt("frameY", frame.location.y as int)
        prefs.putInt("frameWidth", frame.size.width as int)
        prefs.putInt("frameHeight", frame.size.height as int)
        prefs.putInt("verticalSplitterLocation", vSplitter.dividerLocation)
        prefs.putInt("horizontalSplitterLocation", hSplitter.dividerLocation)
        prefs.putBoolean("showScriptFreeForm", scriptFreeFormPref)
        prefs.putBoolean("showScriptClass", scriptClassPref)
        prefs.putBoolean("showTreeView", showTreeView)
        prefs.putInt('compilerPhase', phase.phaseId)
    }
}

/**
 * An adapter for the CompilePhase enum that can be entered into a Swing combobox.
 *
 * @author Hamlet D'Arcy
 */
enum CompilePhaseAdapter {
    INITIALIZATION(Phases.INITIALIZATION, "Initialization"),
    PARSING(Phases.PARSING, "Parsing"),
    CONVERSION(Phases.CONVERSION, "Conversion"),
    SEMANTIC_ANALYSIS(Phases.SEMANTIC_ANALYSIS, "Semantic Analysis"),
    CANONICALIZATION(Phases.CANONICALIZATION, "Canonicalization"),
    INSTRUCTION_SELECTION(Phases.INSTRUCTION_SELECTION, "Instruction Selection"),
    CLASS_GENERATION(Phases.CLASS_GENERATION, "Class Generation"),
    OUTPUT(Phases.OUTPUT, "Output"),
    FINALIZATION(Phases.FINALIZATION, "Finalization")

    final int phaseId
    final String string

    def CompilePhaseAdapter(phaseId, string) {
        this.phaseId = phaseId
        this.string = string
    }

    public String toString() {
        return string
    }
}

/**
 * This class is a TreeNode and you can store additional properties on it.
 *
 * @author Hamlet D'Arcy
 */
class TreeNodeWithProperties extends DefaultMutableTreeNode {

    List<List<String>> properties

    /**
     * Creates a tree node and attaches properties to it.
     * @param userObject same as a DefaultMutableTreeNode requires
     * @param properties a list of String lists
     */
    def TreeNodeWithProperties(userObject, List<List<String>> properties) {
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
 *
 * @author Roshan Dawrani
 */
interface AstBrowserNodeMaker<T> {
    T makeNode(Object userObject)

    T makeNodeWithProperties(Object userObject, List<List<String>> properties)
}

/**
 * Creates tree nodes for swing UI  
 *
 * @author Roshan Dawrani
 */
class SwingTreeNodeMaker implements AstBrowserNodeMaker<DefaultMutableTreeNode> {
    DefaultMutableTreeNode makeNode(Object userObject) {
        new DefaultMutableTreeNode(userObject)
    }

    DefaultMutableTreeNode makeNodeWithProperties(Object userObject, List<List<String>> properties) {
        new TreeNodeWithProperties(userObject, properties)
    }
}

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

class GeneratedBytecodeAwareGroovyClassLoader extends GroovyClassLoader {

    private final Map<String, byte[]> bytecode = new HashMap<String, byte[]>();

    GeneratedBytecodeAwareGroovyClassLoader(final GroovyClassLoader parent) {
        super(parent)
    }

    @Override
    protected ClassCollector createCollector(CompilationUnit unit, SourceUnit su) {
        def collector = super.createCollector(unit, su)
        new BytecodeCollector(collector, bytecode)
    }

    public void clearBytecodeTable() {
        bytecode.clear()
    }

    public byte[] getBytecode(final String className) {
        bytecode[className]
    }
}