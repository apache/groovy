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

import javax.swing.*
import javax.swing.table.*
import javax.swing.tree.*

class SwingMetaMethodsTest extends GroovySwingTestCase {
    void testContainer() {
        testInEDT {
            def container = new JPanel()
            def c0 = new JLabel()
            def c1 = new JLabel()

            assert container.size() == 0
            container << c0
            assert container.size() == 1
            container << c1
            assert container.size() == 2
            assert container[0] == c0
            assert container[1] == c1
            assert container.collect([]) {it} == [c0, c1]
            container.clear()
            assert container.size() == 0
        }
    }

    void testButtonGroup() {
        testInEDT {
            def buttonGroup = new ButtonGroup()
            def c0 = new JButton()
            def c1 = new JButton()

            assert buttonGroup.size() == 0
            buttonGroup << c0
            assert buttonGroup.size() == 1
            buttonGroup << c1
            assert buttonGroup.size() == 2
            assert buttonGroup[0] == c0
            assert buttonGroup[1] == c1
            assert buttonGroup.collect([]) {it} == [c0, c1]
        }
    }

    void testListModel() {
        testInEDT {
            def list = [1, 2, 3, 4, 5]
            def model = [
                    getSize: {-> list.size() },
                    getElementAt: {int i -> list[i] }
            ] as AbstractListModel

            assert model.size() == list.size()
            assert model[2] == list[2]
            assert [2, 4, 6, 8, 10] == model.collect([]) { it * 2 }
        }
    }

    void testDefaultListModel() {
        testInEDT {
            def list = [1, 2, 3, 4, 5]
            def model = new DefaultListModel()
            list.each { model << it }

            assert model.size() == list.size()
            assert model[2] == list[2]
            assert [2, 4, 6, 8, 10] == model.collect([]) { it * 2 }
            model[2] = 42
            assert 42 == model[2]
        }
    }

    void testJComboBox() {
        testInEDT {
            def model = new DefaultComboBoxModel()
            def combo = new JComboBox(model)
            def item0 = "Item 0"
            def item1 = "Item 1"

            assert combo.size() == 0
            combo << item0
            assert combo.size() == 1
            combo << item1
            assert combo.size() == 2
            assert combo[0] == item0
            assert combo[1] == item1
            assert combo.collect([]) {it} == [item0, item1]
            combo.clear()
            assert combo.size() == 0
        }
    }

    void testMutableComboModel() {
        testInEDT {
            def list = [1, 2, 3, 4, 5]
            def model = new DefaultComboBoxModel()
            list.each { model << it }

            assert model.size() == list.size()
            assert model[2] == list[2]
            assert [2, 4, 6, 8, 10] == model.collect([]) { it * 2 }
            model[2] = 42
            assert model.size() == 6
            assert 42 == model[2]
            model.clear()
            assert !model.size()
        }
    }

    void testTableModel() {
        testInEDT {
            def data = [
                    [1, 11, 111, 1111],
                    [2, 22, 222, 2222],
                    [3, 33, 333, 3333]
            ]
            def model = [
                    getColumnCount: {-> 4 },
                    getRowCount: {-> 3 },
                    getValueAt: {int r, int c -> data[r][c] }
            ] as AbstractTableModel

            assert model.size() == 3
            assert model[1] == [2, 22, 222, 2222]
            assert [1, 2, 3] == model.collect([]) {row -> row[0] }
        }
    }

    void testDefaultTableModel() {
        testInEDT {
            def model = new DefaultTableModel(0i, 4i)
            assert model.size() == 0

            model << null
            model << [1]
            model << [2, 22]
            model << [3, 33, 333]
            model << [4, 44, 444, 4444]
            model << [5, 55, 555, 5555, 5555]

            assert model.size() == 6
            assert model[0] == [null, null, null, null]
            assert model[1] == [1, null, null, null]
            assert model[2] == [2, 22, null, null]
            assert model[3] == [3, 33, 333, null]
            assert model[4] == [4, 44, 444, 4444]
            assert model[5] == [5, 55, 555, 5555]

            model[2] = [9, 9, 9, 9]
            assert model.size() == 7
            assert model[2] == [9, 9, 9, 9]
            assert model[3] == [2, 22, null, null]
        }
    }

    void testDefaultTableColumnModel() {
        testInEDT {
            def model = new DefaultTableColumnModel()
            assert model.size() == 0

            model << new TableColumn(2)
            model << new TableColumn(1)
            model << new TableColumn(0)
            assert model.size() == 3

            assert 2 == model[0].modelIndex
            assert [2, 1, 0] == model*.getModelIndex()
        }
    }

    void testTreePath() {
        testInEDT {
            def path = new TreePath(["A", "B"] as Object[])
            assert path.size() == 2
            assert "A" == path[0]
            assert ["A", "B"] == path.collect([]) {it}

            path = path << "C"
            assert path.size() == 3
            assert "C" == path[2]
            assert ["A", "B", "C"] == path.collect([]) {it}
        }
    }

    void testDefaultMutableTreeNode() {
        testInEDT {
            def root = new DefaultMutableTreeNode("root")
            assert 0 == root.size()
            root << new DefaultMutableTreeNode("one")
            root << new DefaultMutableTreeNode("two")
            root << new DefaultMutableTreeNode("three")

            assert 3 == root.size()
            assert ["one", "two", "three"] == root*.getUserObject()

            root[2] << new DefaultMutableTreeNode("A")
            root[2] << new DefaultMutableTreeNode("B")
            assert root[2].size() == 2
            assert ["A", "B"] == root[2]*.getUserObject()
            root[2][1] = new DefaultMutableTreeNode("C")
            assert root[2].size() == 3
            assert ["A", "C", "B"] == root[2]*.getUserObject()
            root[2].clear()
            assert 0 == root[2].size()
        }
    }

    void testMenu() {
        testInEDT {
            def menu = new JMenu()
            assert menu.size() == 0
            def label = new JLabel()
            menu << label
            assert menu.size() == 1
            assert menu[0] == label
            menu << new JMenuItem("item")
            assert menu.size() == 2
            menu << ([actionPerformed: {}] as AbstractAction)
            assert menu.size() == 3
            menu << "item3"
            assert menu.size() == 4
            def i = 4
            menu << "item$i"
            assert menu.size() == 5
            assert label == menu.find {it instanceof JLabel}
        }
    }

    void testMenuBar() {
        testInEDT {
            def menuBar = new JMenuBar()
            assert menuBar.size() == 0
            def menu = new JMenu("Menu0")
            menuBar << menu
            assert menuBar.size() == 1
            assert menuBar[0] == menu
            menuBar << new JMenu("Menu1")
            assert menuBar.size() == 2
            assert ["Menu0", "Menu1"] == menuBar*.text
        }
    }

    void testPopupMenu() {
        testInEDT {
            def popupMenu = new JPopupMenu()
            assert popupMenu.size() == 0
            def label = new JLabel()
            popupMenu << label
            assert popupMenu.size() == 1
            assert popupMenu[0] == label
            popupMenu << new JMenuItem("item")
            assert popupMenu.size() == 2
            popupMenu << ([actionPerformed: {}] as AbstractAction)
            assert popupMenu.size() == 3
            popupMenu << "item3"
            assert popupMenu.size() == 4
            def i = 4
            popupMenu << "item$i"
            assert popupMenu.size() == 5
            assert popupMenu.find {it.text == "item3"}
        }
    }

    void testTabbedPane() {
        testInEDT {
            def tabbedPane = new JTabbedPane()
            def c0 = new JLabel(name: "label0", text: "Label0")
            def c1 = new JLabel(name: "label1", text: "Label1")
            assert tabbedPane.size() == 0
            tabbedPane << c0
            assert tabbedPane.size() == 1
            tabbedPane << c1
            assert tabbedPane.size() == 2
            assert tabbedPane[0] == c0
            assert [c0, c1] == tabbedPane.collect([]) {it}
            tabbedPane.clear()
            assert tabbedPane.size() == 0
        }
    }

    void testToolBar() {
        testInEDT {
            def toolBar = new JToolBar()
            def c0 = new JButton(name: "label0", text: "Label0")
            def c1 = new JButton(name: "label1", text: "Label1")
            assert toolBar.size() == 0
            toolBar << c0
            assert toolBar.size() == 1
            toolBar << c1
            assert toolBar.size() == 2
            assert toolBar[0] == c0
            assert [c0, c1] == toolBar.collect([]) {it}
        }
    }
}
