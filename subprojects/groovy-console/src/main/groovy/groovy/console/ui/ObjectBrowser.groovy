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

import groovy.inspect.Inspector
import groovy.swing.table.TableSorter
import groovy.swing.SwingBuilder

import javax.swing.WindowConstants
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

import static javax.swing.ListSelectionModel.SINGLE_SELECTION

import static groovy.inspect.Inspector.MEMBER_DECLARER_IDX
import static groovy.inspect.Inspector.MEMBER_EXCEPTIONS_IDX
import static groovy.inspect.Inspector.MEMBER_MODIFIER_IDX
import static groovy.inspect.Inspector.MEMBER_NAME_IDX
import static groovy.inspect.Inspector.MEMBER_ORIGIN_IDX
import static groovy.inspect.Inspector.MEMBER_PARAMS_IDX
import static groovy.inspect.Inspector.MEMBER_TYPE_IDX
import static groovy.inspect.Inspector.MEMBER_VALUE_IDX
import static groovy.inspect.Inspector.MEMBER_RAW_VALUE_IDX

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
    def path
    def swing, frame, fieldTable, methodTable, arrayTable, collectionTable, mapTable

    static void main(args) {
        inspect('some String')
    }

    public ObjectBrowser(String path = '') {
        this.path = (path == null ? '' : path)
    }

    static void inspect(objectUnderInspection, String path = '') {
        def browser = new ObjectBrowser(path)
        browser.inspector = new Inspector(objectUnderInspection)
        if (objectUnderInspection != null && browser.path == '') {
            browser.path = "${objectUnderInspection.getClass().name} instance"
        }
        browser.run()
    }

    void run() {
        swing = new SwingBuilder()

        frame = swing.frame(title: 'Groovy Object Browser', location: [200, 200],
                size: [800, 600], pack: true, show: true,
                iconImage: swing.imageIcon(Console.ICON_PATH).image,
                defaultCloseOperation: WindowConstants.DISPOSE_ON_CLOSE) {

            menuBar {
                menu(text: 'Help') {
                    menuItem { action(name: 'About', closure: this.&showAbout) }
                }
            }
            panel {
                borderLayout()
                panel(name: 'Class Info',
                        border: emptyBorder([5, 10, 5, 10]),
                        constraints: NORTH) {
                    flowLayout(alignment: FlowLayout.LEFT)
                    def props = inspector.classProps
                    def classLabel = '<html>' + props.join('<br>')
                    label(classLabel)
                }
                tabbedPane(constraints: CENTER) {
                    if (inspector.object?.class?.array) {
                        scrollPane(name: ' Array data ') {
                            arrayTable = table(selectionMode: SINGLE_SELECTION) {
                                tableModel(list: inspector.object.toList().withIndex()) {
                                    closureColumn(header: 'Index', read: { it[1] })
                                    closureColumn(header: 'Value', read: { it[0] })
                                    closureColumn(header: 'Raw Value', read: { it[0] }) // to support sorting
                                }
                            }
                            arrayTable.columnModel.getColumn(2).with {
                                minWidth = 0
                                maxWidth = 0
                                width = 0
                            }
                            arrayTable.addMouseListener(new MouseAdapter() {
                                void mouseClicked(MouseEvent e) {
                                    if (e.clickCount == 2) {
                                        def selectedRow = arrayTable.selectedRow
                                        if (selectedRow != -1) {
                                            def value = arrayTable.model.getValueAt(selectedRow, 2)
                                            if (value != null) {
                                                closeFrameIfAltDoubleClick(e)
                                                ObjectBrowser.inspect(value, path + "[${arrayTable.model.getValueAt(selectedRow, 0)}]")
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    } else if (inspector.object instanceof Collection) {
                        scrollPane(name: ' Collection data ') {
                            collectionTable = table(selectionMode: SINGLE_SELECTION) {
                                tableModel(list: inspector.object.withIndex()) {
                                    closureColumn(header: 'Index', read: { it[1] })
                                    closureColumn(header: 'Value', read: { it[0] })
                                    closureColumn(header: 'Raw Value', read: { it[0] }) // to support sorting
                                }
                            }
                            collectionTable.columnModel.getColumn(2).with {
                                minWidth = 0
                                maxWidth = 0
                                width = 0
                            }
                            collectionTable.addMouseListener(new MouseAdapter() {
                                void mouseClicked(MouseEvent e) {
                                    if (e.clickCount == 2) {
                                        def selectedRow = collectionTable.selectedRow
                                        if (selectedRow != -1) {
                                            def value = collectionTable.model.getValueAt(selectedRow, 2)
                                            if (value != null) {
                                                closeFrameIfAltDoubleClick(e)
                                                ObjectBrowser.inspect(value, path + "[${collectionTable.model.getValueAt(selectedRow, 0)}]")
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    } else if (inspector.object instanceof Map) {
                        scrollPane(name: ' Map data ') {
                            mapTable = table(selectionMode: SINGLE_SELECTION) {
                                tableModel(list: inspector.object.entrySet().withIndex()) {
                                    closureColumn(header: 'Index', read: { it[1] })
                                    closureColumn(header: 'Key', read: { it[0].key })
                                    closureColumn(header: 'Value', read: { it[0].value })
                                    closureColumn(header: 'Raw Value', read: { it[0].value }) // to support sorting
                                }
                            }
                            mapTable.columnModel.getColumn(3).with {
                                minWidth = 0
                                maxWidth = 0
                                width = 0
                            }
                            mapTable.addMouseListener(new MouseAdapter() {
                                void mouseClicked(MouseEvent e) {
                                    if (e.clickCount == 2) {
                                        def selectedRow = mapTable.selectedRow
                                        if (selectedRow != -1) {
                                            def value = mapTable.model.getValueAt(selectedRow, 2)
                                            if (value != null) {
                                                closeFrameIfAltDoubleClick(e)
                                                ObjectBrowser.inspect(value, path + "[${mapTable.model.getValueAt(selectedRow, 1)}]")
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    }
                    scrollPane(name: ' Properties (includes public fields) ') {
                        def data = Inspector.sortWithRawValue(inspector.propertyInfoWithRawValue.toList())
                        fieldTable = table(selectionMode: SINGLE_SELECTION) {
                            tableModel(list: data) {
                                closureColumn(header: 'Name', read: { it[MEMBER_NAME_IDX] })
                                closureColumn(header: 'Value', read: { it[MEMBER_VALUE_IDX] })
                                closureColumn(header: 'Type', read: { it[MEMBER_TYPE_IDX] })
                                closureColumn(header: 'Origin', read: { it[MEMBER_ORIGIN_IDX] })
                                closureColumn(header: 'Modifier', read: { it[MEMBER_MODIFIER_IDX] })
                                closureColumn(header: 'Declarer', read: { it[MEMBER_DECLARER_IDX] })
                                closureColumn(header: 'Raw Value', read: { it[MEMBER_RAW_VALUE_IDX] }) // to support sorting
                            }
                        }
                        fieldTable.columnModel.getColumn(6).with {
                            minWidth = 0
                            maxWidth = 0
                            width = 0
                        }
                        fieldTable.addMouseListener(new MouseAdapter() {
                            void mouseClicked(MouseEvent e) {
                                if (e.clickCount == 2) {
                                    def selectedRow = fieldTable.selectedRow
                                    if (selectedRow != -1) {
                                        def value = fieldTable.model.getValueAt(selectedRow, MEMBER_RAW_VALUE_IDX)
                                        if (value != null) {
                                            closeFrameIfAltDoubleClick(e)
                                            ObjectBrowser.inspect(value, path + (path.length() === 0 ? '' : '.') + "${fieldTable.model.getValueAt(selectedRow, 0)}")
                                        }
                                    }
                                }
                            }
                        })
                    }
                    scrollPane(name: ' (Meta) Methods ') {
                        methodTable = table(selectionMode: SINGLE_SELECTION) {
                            def data = Inspector.sort(inspector.methods.toList())
                            data.addAll(Inspector.sort(inspector.metaMethods.toList()))
                            tableModel(list: data) {
                                closureColumn(header: 'Name', read: { it[MEMBER_NAME_IDX] })
                                closureColumn(header: 'Params', read: { it[MEMBER_PARAMS_IDX] })
                                closureColumn(header: 'Type', read: { it[MEMBER_TYPE_IDX] })
                                closureColumn(header: 'Origin', read: { it[MEMBER_ORIGIN_IDX] })
                                closureColumn(header: 'Modifier', read: { it[MEMBER_MODIFIER_IDX] })
                                closureColumn(header: 'Declarer', read: { it[MEMBER_DECLARER_IDX] })
                                closureColumn(header: 'Exceptions', read: { it[MEMBER_EXCEPTIONS_IDX] })
                            }
                        }
                    }
                }
                panel(name: 'Path',
                        border: emptyBorder([5, 10, 5, 10]),
                        constraints: SOUTH) {
                    boxLayout(axis: 2)
                    label('Path:  ')
                    textField(editable: false, text: path)
                }
            }
        }

        // Add a bit of formatting
        addSorter(arrayTable)
        addSorter(collectionTable)
        addSorter(mapTable)
        addSorter(fieldTable)
        addSorter(methodTable)

        frame.toFront()
    }

    void addSorter(table) {
        if (table != null) {
            def sorter = new TableSorter(table.model)
            table.model = sorter
            sorter.addMouseListenerToHeaderInTable(table)
        }
    }

    void closeFrameIfAltDoubleClick(MouseEvent e) {
        if (e.altDown) {
            frame.visible = false;
            frame.dispose();
        }
    }

    void showAbout(EventObject evt) {
        def pane = swing.optionPane()
        // work around GROOVY-1048
        def version = GroovySystem.version
        pane.setMessage('An interactive GUI to explore object capabilities.\nVersion ' + version)
        def dialog = pane.createDialog(frame, 'About Groovy Object Browser')
        dialog.show()
    }
}
