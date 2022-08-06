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
package groovy.console.ui

import groovy.beans.Bindable
import groovy.inspect.Inspector
import groovy.swing.SwingBuilder
import groovy.swing.table.TableSorter

import javax.swing.ToolTipManager
import javax.swing.WindowConstants
import java.awt.FlowLayout
import java.awt.Insets
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

import static groovy.console.ui.text.SmartDocumentFilter.HIGHLIGHTED_TOKEN_TYPE_LIST
import static groovy.inspect.Inspector.MEMBER_DECLARER_IDX
import static groovy.inspect.Inspector.MEMBER_EXCEPTIONS_IDX
import static groovy.inspect.Inspector.MEMBER_MODIFIER_IDX
import static groovy.inspect.Inspector.MEMBER_NAME_IDX
import static groovy.inspect.Inspector.MEMBER_ORIGIN_IDX
import static groovy.inspect.Inspector.MEMBER_PARAMS_IDX
import static groovy.inspect.Inspector.MEMBER_TYPE_IDX
import static groovy.inspect.Inspector.MEMBER_VALUE_IDX
import static javax.swing.ListSelectionModel.SINGLE_SELECTION
import static org.apache.groovy.parser.antlr4.GroovyLexer.VOCABULARY
/**
 * A little GUI to show some of the Inspector capabilities.
 * Starting this script opens the ObjectBrowser on "some String".
 * Use it in groovysh or groovyConsole to inspect your object of interest with:
 * <code>
 * ObjectBrowser.inspect(myObject)
 * </code>.
 */
class ObjectBrowser {

    def inspector
    String path
    int pathCount = 0
    def tracker
    def swing, frame, fieldTable, methodTable, arrayTable, collectionTable, mapTable, cards, pathMenu, mb
    private static final Comparator<Object> comparator = new Inspector.MemberComparatorWithValue()

    static void main(args) {
        inspect('some String')
    }

    static void inspect(objectUnderInspection, String path = '') {
        def browser = new ObjectBrowser(path: path ?: '')
        browser.inspector = new Inspector(objectUnderInspection)
        if (objectUnderInspection != null && browser.path == '') {
            browser.path = "${objectUnderInspection.getClass().name} instance"
        }
        browser.run()
    }

    void inspectAlso(objectUnderInspection, String path = '') {
        int idx = pathCount++
        def pathId = 'path' + idx
        tracker.current = idx
        cards.add(makeCard(swing, new Inspector(objectUnderInspection), path), pathId)
        cards.layout.show(cards, pathId)
        cards.revalidate()
        pathMenu.add(swing.menuItem { action(name: path, enabled: bind{ tracker.current != idx }, closure: this.&showAction.curry(idx)) })
        mb.revalidate()
    }

    void run() {
        swing = new SwingBuilder()
        tracker = new CardTracker()

        frame = swing.frame(title: 'Groovy Object Browser', location: [200, 200],
                size: [800, 600], pack: true, show: true,
                iconImage: swing.imageIcon(Console.ICON_PATH).image,
                defaultCloseOperation: WindowConstants.DISPOSE_ON_CLOSE) {

            mb = menuBar {
                pathMenu = menu(text: 'Path') {
                    int idx = pathCount++
                    menuItem { action(name: path.toString(), enabled: bind{ tracker.current != idx }, closure: this.&showAction.curry(idx)) }
                }
                menu(text: 'Help') {
                    menuItem { action(name: 'Usage', closure: this.&usageAction) }
                    menuItem { action(
                            name: 'About',
                            smallIcon: imageIcon(resource: 'icons/information.png', class: this),
                            closure: this.&aboutAction)
                    }
                }
            }
            cards = panel {
                cardLayout()
                makeCard(swing, inspector, path)
            }
        }
        // can't seem to get the 'path0' to stick so re-add and remove the original
        cards.add(makeCard(swing, inspector, path), 'path0')
        cards.remove(0)
        cards.revalidate()

        // Add a bit of formatting
        addSorter(arrayTable)
        addSorter(collectionTable)
        addSorter(mapTable)
        addSorter(fieldTable)
        addSorter(methodTable)

        frame.toFront()
    }

    private makeCard(builder, inspector, String path) {
        builder.panel {
            borderLayout()
            panel(name: 'Class Info',
                    border: titledBorder('Class Info'),
                    constraints: NORTH) {
                flowLayout(alignment: FlowLayout.LEFT)
                def props = inspector.classProps.findAll { it != 'implements ' }
                try {
                    def module = inspector.object.class.module.name
                    if (module) props.add(0, "module ${module}")
                } catch(Exception ignore) {}

                final implementsPrefix = 'implements '
                def content = props.collect {
                    String p = it.trim()
                    if (p.startsWith(implementsPrefix)) {
                        return "${implementsPrefix}${p.substring(implementsPrefix.length()).replace(' ', ', ')}"
                    }
                    if (p.startsWith('is ')) {
                        return "(${p})"
                    }
                    return p
                }.join('\n').trim()

                for (def ln in (HIGHLIGHTED_TOKEN_TYPE_LIST.collect {
                                    VOCABULARY.getLiteralName(it)?.replace("'", '')
                                }.grep { it } + ['module', 'true', 'false'])) {
                    content = content.replaceAll(/\b(${ln})\b/, '<b>$1</b>')
                }
                content = content.replace('\n', '<br/>').replace(',', '<b>,</b>').replace(':', '<b>:</b>')
                def classLabel = "<html>${content}</html>"
                label(classLabel)
            }
            tabbedPane(constraints: CENTER) {
                if (inspector.object?.class?.array) {
                    scrollPane(name: ' Array data ') {
                        arrayTable = table(new CellValueToolTipJTable(), selectionMode: SINGLE_SELECTION) {
                            tableModel(list: inspector.object.toList().withIndex()) {
                                closureColumn(header: 'Index', read: { it[1] })
                                closureColumn(header: 'Value', read: { it[0] })
                                closureColumn(header: 'Raw Value', read: { it[0] })
                            }
                        }
                        arrayTable.columnModel.with {
                            getColumn(2).with {
                                minWidth = 0
                                maxWidth = 0
                                width = 0
                                preferredWidth = 0
                            }
                            getColumn(0).preferredWidth = 50
                            getColumn(1).preferredWidth = 400
                        }

                        arrayTable.addMouseListener(mouseListener(2) { row ->
                            path + "[${arrayTable.model.getValueAt(row, 0)}]"
                        })
                    }
                } else if (inspector.object instanceof Collection) {
                    scrollPane(name: ' Collection data ') {
                        collectionTable = table(new CellValueToolTipJTable(), selectionMode: SINGLE_SELECTION) {
                            tableModel(list: inspector.object.withIndex()) {
                                closureColumn(header: 'Index', read: { it[1] })
                                closureColumn(header: 'Value', read: { it[0] })
                                closureColumn(header: 'Raw Value', read: { it[0] })
                            }
                        }
                        collectionTable.columnModel.with {
                            getColumn(2).with {
                                minWidth = 0
                                maxWidth = 0
                                width = 0
                                preferredWidth = 0
                            }
                            getColumn(0).preferredWidth = 50
                            getColumn(1).preferredWidth = 400
                        }
                        collectionTable.addMouseListener(mouseListener(2) { row ->
                            path + "[${collectionTable.model.getValueAt(row, 0)}]"
                        })
                    }
                } else if (inspector.object instanceof Map) {
                    scrollPane(name: ' Map data ') {
                        mapTable = table(new CellValueToolTipJTable(), selectionMode: SINGLE_SELECTION) {
                            tableModel(list: inspector.object.entrySet().withIndex()) {
                                closureColumn(header: 'Index', read: { it[1] })
                                closureColumn(header: 'Key', read: { it[0].key })
                                closureColumn(header: 'Value', read: { it[0].value })
                                closureColumn(header: 'Raw Value', read: { it[0].value })
                            }
                        }
                        ToolTipManager.sharedInstance().registerComponent(mapTable)
                        mapTable.columnModel.with {
                            getColumn(3).with {
                                minWidth = 0
                                maxWidth = 0
                                width = 0
                                preferredWidth = 0
                            }
                            getColumn(0).preferredWidth = 50
                            getColumn(1).preferredWidth = 200
                            getColumn(2).preferredWidth = 400
                        }
                        mapTable.addMouseListener(mouseListener(2) { row ->
                            path + "[${mapTable.model.getValueAt(row, 1)}]"
                        })
                    }
                }
                scrollPane(name: ' Properties (includes public fields) ') {
                    def data = Inspector.sort(inspector.propertiesWithInfo.toList(), comparator)
                    fieldTable = table(new CellValueToolTipJTable(), selectionMode: SINGLE_SELECTION) {
                        tableModel(list: data) {
                            closureColumn(header: 'Name', read: { it.v2[MEMBER_NAME_IDX] })
                            closureColumn(header: 'Value', read: { it.v2[MEMBER_VALUE_IDX] })
                            closureColumn(header: 'Type', read: { it.v2[MEMBER_TYPE_IDX] })
                            closureColumn(header: 'Origin', read: { it.v2[MEMBER_ORIGIN_IDX] })
                            closureColumn(header: 'Modifier', read: { it.v2[MEMBER_MODIFIER_IDX] })
                            closureColumn(header: 'Declarer', read: { it.v2[MEMBER_DECLARER_IDX] })
                            closureColumn(header: 'Raw Value', read: { it.v1 })
                        }
                    }
                    fieldTable.columnModel.getColumn(6).with {
                        minWidth = 0
                        maxWidth = 0
                        width = 0
                    }
                    fieldTable.addMouseListener(mouseListener(6) { row ->
                        path + (path.size() == 0 ? '' : '.') + "${fieldTable.model.getValueAt(row, 0)}"
                    })
                }
                scrollPane(name: ' (Meta) Methods ') {
                    methodTable = table(new CellValueToolTipJTable(), selectionMode: SINGLE_SELECTION) {
                        def data = Inspector.sort(inspector.methodsWithInfo.toList(), comparator)
                        data.addAll(Inspector.sort(inspector.metaMethodsWithInfo.toList(), comparator))
                        tableModel(list: data) {
                            closureColumn(header: 'Name', read: { it.v2[MEMBER_NAME_IDX] })
                            closureColumn(header: 'Params', read: { it.v2[MEMBER_PARAMS_IDX] })
                            closureColumn(header: 'Type', read: { it.v2[MEMBER_TYPE_IDX] })
                            closureColumn(header: 'Origin', read: { it.v2[MEMBER_ORIGIN_IDX] })
                            closureColumn(header: 'Modifier', read: { it.v2[MEMBER_MODIFIER_IDX] })
                            closureColumn(header: 'Declarer', read: { it.v2[MEMBER_DECLARER_IDX] })
                            closureColumn(header: 'Exceptions', read: { it.v2[MEMBER_EXCEPTIONS_IDX] })
                            closureColumn(header: 'Raw Value', read: { it.v1 })
                        }
                    }
                    methodTable.columnModel.getColumn(7).with {
                        minWidth = 0
                        maxWidth = 0
                        width = 0
                    }
                    methodTable.addMouseListener(mouseListener(7) { row ->
                        path + (path.size() == 0 ? '' : ".method['") + "${methodTable.model.getValueAt(row, 0)}']"
                    })
                }
            }
            panel(name: 'Path',
                    border: emptyBorder([5, 10, 5, 10]),
                    constraints: SOUTH) {
                boxLayout(axis: 2)
                button(icon: imageIcon(resource: 'icons/resultset_previous.png', class: this),
                        margin: [5, 5, 5, 5] as Insets,
                        actionPerformed: { tracker.current--; show() },
                        enabled: bind { tracker.current > 0 })
                label('Path:  ')
                textField(editable: false, text: path)
                button(icon: imageIcon(resource: 'icons/resultset_next.png', class: this),
                        margin: [5, 5, 5, 5] as Insets,
                        actionPerformed: { tracker.current++; show() },
                        enabled: bind { tracker.current < pathCount - 1 }
                )
            }
        }
    }

    def mouseListener(int valueCol, Closure pathClosure) {
        def outer = this
        new MouseAdapter() {
            void mouseClicked(MouseEvent e) {
                if (e.clickCount == 2) {
                    launch(e.source, valueCol, e.shiftDown, pathClosure)
                }
            }

            void mouseReleased(MouseEvent e) {
                def table = e.source
                int r = table.rowAtPoint(e.point)
                if (r >= 0 && r < table.rowCount) {
                    table.setRowSelectionInterval(r, r)
                } else {
                    table.clearSelection()
                }

                if (table.selectedRow < 0) return
                if (e.isPopupTrigger()) {
                    def popup = swing.popupMenu {
                        menuItem(action(
                                name: 'Copy',
                                closure: outer.&copyAction.curry(table, e),
                                mnemonic: 'C',
                                accelerator: shortcut('C'),
                                smallIcon: imageIcon(resource: 'icons/page_copy.png', class: this),
                                shortDescription: 'Copy'
                        ))
                        menuItem(action(
                                name: 'Browse',
                                enabled: table.model.getValueAt(table.selectedRow, valueCol) != null,
                                closure: outer.&launchAction.curry(table, valueCol, false, pathClosure),
                                smallIcon: imageIcon(resource: 'icons/page_white_stack.png', class: this),
                                shortDescription: 'Browse'
                        ))
                        menuItem(action(
                                name: 'Browse in new window',
                                enabled: table.model.getValueAt(table.selectedRow, valueCol) != null,
                                closure: outer.&launchAction.curry(table, valueCol, true, pathClosure),
                                smallIcon: imageIcon(resource: 'icons/page_white_go.png', class: this),
                                shortDescription: 'Browse window'
                        ))
                    }
                    popup.show(e.component, e.x, e.y)
                }
            }
        }
    }

    void launch(table, valueCol, boolean newWindow, pathClosure) {
        def selectedRow = table.selectedRow
        if (selectedRow != -1) {
            def value = table.model.getValueAt(selectedRow, valueCol)
            if (value != null) {
                if (newWindow)
                    ObjectBrowser.inspect(value, pathClosure(selectedRow))
                else
                    inspectAlso(value, pathClosure(selectedRow))
            }
        }
    }

    void addSorter(table) {
        if (table != null) {
            def sorter = new TableSorter(table.model)
            table.model = sorter
            sorter.addMouseListenerToHeaderInTable(table)
        }
    }

    void showAction(int idx, EventObject evt) {
        tracker.current = idx
        show()
    }

    void show() {
        cards.layout.show(cards, 'path' + tracker.current)
        cards.revalidate()
    }

    void usageAction(EventObject evt) {
        def pane = swing.optionPane()
        // work around GROOVY-1048
        pane.setMessage(
            'Double-click on a row to drill-down into the child level.\n' +
            'A new card is created for the child level. The cards can\n' +
            'be selected using menu items in the Path menu.\n' +
            'Shift-double-click on a row to launch new Object Browser\n' +
            'window.')
        def dialog = pane.createDialog(frame, 'Object Browser Usage')
        dialog.show()
    }

    void launchAction(table, valueCol, boolean newWindow, pathClosure, EventObject evt) {
        launch(table, valueCol, newWindow, pathClosure)
    }

    void copyAction(table, MouseEvent me, EventObject evt) {
        def toolTipText = table.getToolTipText(me)
        if (toolTipText) {
            Toolkit.defaultToolkit.systemClipboard.setContents(new StringSelection(toolTipText), null)
        }
    }

    void aboutAction(EventObject evt) {
        def pane = swing.optionPane()
        // work around GROOVY-1048
        def version = GroovySystem.version
        pane.setMessage('An interactive GUI to explore object capabilities.\nVersion ' + version)
        def dialog = pane.createDialog(frame, 'About Groovy Object Browser')
        dialog.show()
    }
}

@Bindable class CardTracker {
    int current = 0
}
