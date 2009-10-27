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


import groovy.inspect.swingui.ScriptToTreeNodeAdapter
import groovy.swing.SwingBuilder
import java.awt.Cursor
import static java.awt.GridBagConstraints.*
import javax.swing.UIManager
import javax.swing.WindowConstants
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreeSelectionModel
import org.codehaus.groovy.control.Phases
import java.util.prefs.Preferences

/**
 * This object is a GUI for looking at the AST that Groovy generates. 
 * 
 * Usage: java groovy.inspect.swingui.AstBrowser [filename]
 *         where [filename] is an existing Groovy script. 
 * 
 * @author Hamlet D'Arcy (hamletdrc@gmail.com)
 * @author Guillaume Laforge, highlighting the code corresponding to a node selected in the tree view
 */

public class AstBrowser {

    private inputArea, rootElement
    boolean showScriptFreeForm, showScriptClass

    AstBrowser(inputArea, rootElement) {
        this.inputArea = inputArea
        this.rootElement = rootElement
    }

    def swing, frame

    public static void main(args) {
        
        if (!args) {
            println "Usage: java groovy.inspect.swingui.AstBrowser [filename]\nwhere [filename] is a Groovy script"
        } else {
            def file = new File((String)args[0])
            if (!file.exists()) {
                println "File $args[0] cannot be found."
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
                new AstBrowser(null, null).run({file.text}, file.path)
            }
        }
    }

    void run(Closure script) {
        run(script, null)
    }

    void run(Closure script, String name) {

        swing = new SwingBuilder()
        def prefs = new AstBrowserUiPreferences()
        def rootNode = new DefaultTreeModel(new DefaultMutableTreeNode("Loading...")) // populated later
        def phasePicker, jTree, propertyTable, splitterPane

        showScriptFreeForm = prefs.showScriptFreeForm
        showScriptClass = prefs.showScriptClass
        
        frame = swing.frame(title: 'Groovy AST Browser' + (name ? " - $name":''),
                location: prefs.frameLocation,
                size: prefs.frameSize,
                iconImage: swing.imageIcon(groovy.ui.Console.ICON_PATH).image,
                defaultCloseOperation: WindowConstants.DISPOSE_ON_CLOSE,
                windowClosing : { event -> prefs.save(frame, splitterPane, showScriptFreeForm, showScriptClass) } ) {

            menuBar {
                menu(text: 'Show Script', mnemonic: 'S') {
                    checkBoxMenuItem(selected: showScriptFreeForm) {action(name: 'Free Form', closure: this.&showScriptFreeForm, 
                            mnemonic: 'F',)}
                    checkBoxMenuItem(selected: showScriptClass) {action(name: 'Class Form', closure: this.&showScriptClass, 
                            mnemonic: 'C')}
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
                        selectedItem: CompilePhaseAdapter.SEMANTIC_ANALYSIS,
                        actionPerformed: {
                            compile(rootNode, swing, script(), phasePicker.selectedItem.phaseId)
                        }, 
                        constraints: gbc(gridx: 1, gridy: 0, gridwidth: 1, gridheight: 1, weightx: 1.0, weighty: 0, anchor: NORTHWEST, fill: NONE, insets: [2, 2, 2, 2]))
                button(text: 'Refresh',
                        actionPerformed: {
                            compile(rootNode, swing, script(), phasePicker.selectedItem.phaseId)
                        },
                        constraints: gbc(gridx: 2, gridy: 0, gridwidth: 1, gridheight: 1, weightx: 0, weighty: 0, anchor: NORTHEAST, fill: NONE, insets: [2, 2, 2, 3]))
                splitterPane = splitPane(
                        dividerLocation :  prefs.dividerLocation,
                        leftComponent: scrollPane() {
                            jTree = tree(
                                    name: "AstTreeView",
                                    model: rootNode) {}
                        },
                        rightComponent: scrollPane() {
                            propertyTable = table() {
                                tableModel(list:[[:]]) {
                                    propertyColumn(header:'Name', propertyName:'name')
                                    propertyColumn(header:'Value', propertyName:'value')
                                    propertyColumn(header:'Type', propertyName:'type')
                                }
                            }
                        },
                        constraints: gbc(gridx: 0, gridy: 1, gridwidth: 3, gridheight: 1, weightx: 1.0, weighty: 1.0, anchor: NORTHWEST, fill: BOTH, insets: [2, 2, 2, 2])) { }
            }
        }

        propertyTable.model.rows.clear() //for some reason this suppress an empty row
        
        jTree.cellRenderer.setLeafIcon(swing.imageIcon(groovy.ui.Console.NODE_ICON_PATH));

        jTree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION;
        jTree.addTreeSelectionListener({ TreeSelectionEvent e ->

            propertyTable.model.rows.clear()
            TreeNode node = jTree.lastSelectedPathComponent
            if (node != null && node instanceof TreeNodeWithProperties) {

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
            }
            propertyTable.model.fireTableDataChanged()
        } as TreeSelectionListener)

        frame.pack()
        frame.show()
        compile(rootNode, swing, script(), phasePicker.selectedItem.phaseId)
        jTree.rootVisible = false
        jTree.showsRootHandles = true   // some OS's require this as a step to show nodes
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

    void compile(node, swing, String script, int compilePhase) {
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR))
        def adapter = new ScriptToTreeNodeAdapter(showScriptFreeForm: showScriptFreeForm, showScriptClass: showScriptClass)
        node.setRoot(adapter.compile(script, compilePhase))
        frame.setCursor(Cursor.defaultCursor)
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
    final def dividerLocation
    final boolean showScriptFreeForm
    final boolean showScriptClass

    def AstBrowserUiPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(AstBrowserUiPreferences)
        frameLocation = [
                prefs.getInt("frameX", 200),
                prefs.getInt("frameY", 200)]
        frameSize = [
                prefs.getInt("frameWidth", 800),
                prefs.getInt("frameHeight", 600)]
        dividerLocation = prefs.getInt("splitterPaneLocation", 100)
        showScriptFreeForm = prefs.getBoolean("showScriptFreeForm", false)
        showScriptClass = prefs.getBoolean("showScriptClass", true)
    }

    def save(frame, splitter, scriptFreeFormPref, scriptClassPref) {
        Preferences prefs = Preferences.userNodeForPackage(AstBrowserUiPreferences)
        prefs.putInt("frameX", frame.location.x as int)
        prefs.putInt("frameY", frame.location.y as int)
        prefs.putInt("frameWidth", frame.size.width as int)
        prefs.putInt("frameHeight", frame.size.height as int)
        prefs.putInt("splitterPaneLocation", splitter.dividerLocation)
        prefs.putBoolean("showScriptFreeForm", scriptFreeFormPref)
        prefs.putBoolean("showScriptClass", scriptClassPref)
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
     * @param userObject    same as a DefaultMutableTreeNode requires
     * @param properties    a list of String lists
     */
    def TreeNodeWithProperties(userObject, List<List<String>> properties) {
        super(userObject)
        this.properties = properties
    }
}
