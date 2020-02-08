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
package org.apache.groovy.swing.extensions;

import groovy.lang.GString;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.typehandling.ShortTypeHandling;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * This class defines all the new Swing-related groovy methods which enhance
 * the normal JDK Swing classes when inside the Groovy environment.
 * Static methods are used with the first parameter the destination class.
 */
public class SwingExtensions {

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>Container</code>.
     *
     * @param self a Container
     * @return the component count of the container
     * @since 1.6.4
     */
    public static int size(Container self) {
        return self.getComponentCount();
    }

    /**
     * Support the subscript operator for Container.
     *
     * @param self  a Container
     * @param index the index of the Component to get
     * @return the component at the given index
     * @since 1.6.4
     */
    public static Component getAt(Container self, int index) {
        return self.getComponent(index);
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * components to a Container.
     *
     * @param self a Container
     * @param c    a Component to be added to the container.
     * @return same container, after the value was added to it.
     * @since 1.6.4
     */
    public static Container leftShift(Container self, Component c) {
        self.add(c);
        return self;
    }

    /**
     * Returns an {@link java.util.Iterator} which traverses the Container one Component at a time.
     *
     * @param self a Container
     * @return an Iterator for a Container
     * @since 1.6.4
     */
    public static Iterator<Component> iterator(Container self) {
        return DefaultGroovyMethods.iterator(self.getComponents());
    }

    /**
     * Removes all components from the Container.
     *
     * @param self a Container
     * @since 1.6.4
     */
    public static void clear(Container self) {
        self.removeAll();
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>ButtonGroup</code>.
     *
     * @param self a ButtonGroup
     * @return the button count of the buttonGroup
     * @since 1.6.4
     */
    public static int size(ButtonGroup self) {
        return self.getButtonCount();
    }

    /**
     * Support the subscript operator for ButtonGroup.
     *
     * @param self  a ButtonGroup
     * @param index the index of the AbstractButton to get
     * @return the button at the given index
     * @since 1.6.4
     */
    public static AbstractButton getAt(ButtonGroup self, int index) {
        int size = self.getButtonCount();
        if (index < 0 || index >= size) return null;
        Enumeration<AbstractButton> buttons = self.getElements();
        for (int i = 0; i <= index; i++) {
            AbstractButton b = buttons.nextElement();
            if (i == index) return b;
        }
        return null;
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * buttons to a ButtonGroup.
     *
     * @param self a ButtonGroup
     * @param b    an AbstractButton to be added to the buttonGroup.
     * @return same buttonGroup, after the value was added to it.
     * @since 1.6.4
     */
    public static ButtonGroup leftShift(ButtonGroup self, AbstractButton b) {
        self.add(b);
        return self;
    }

    /**
     * Returns an {@link java.util.Iterator} which traverses the ButtonGroup one AbstractButton at a time.
     *
     * @param self a ButtonGroup
     * @return an Iterator for a ButtonGroup
     * @since 1.6.4
     */
    public static Iterator<AbstractButton> iterator(ButtonGroup self) {
        return DefaultGroovyMethods.iterator(self.getElements());
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>ListModel</code>.
     *
     * @param self a ListModel
     * @return the size of the ListModel
     * @since 1.6.4
     */
    public static int size(ListModel<?> self) {
        return self.getSize();
    }

    /**
     * Support the subscript operator for ListModel.
     *
     * @param self  a ListModel
     * @param index the index of the element to get
     * @return the element at the given index
     * @since 1.6.4
     */
    public static Object getAt(ListModel<?> self, int index) {
        return self.getElementAt(index);
    }

    /**
     * Returns an {@link java.util.Iterator} which traverses the ListModel one element at a time.
     *
     * @param self a ListModel
     * @return an Iterator for a ListModel
     * @since 1.6.4
     */
    public static Iterator<?> iterator(final ListModel<?> self) {
        return new Iterator<Object>() {
            private int index = 0;

            public boolean hasNext() {
                return index < self.getSize();
            }

            public Object next() {
                return self.getElementAt(index++);
            }

            public void remove() {
                throw new UnsupportedOperationException("ListModel is immutable.");
            }
        };
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * elements to a DefaultListModel.
     *
     * @param self a DefaultListModel
     * @param e    an element to be added to the listModel.
     * @return same listModel, after the value was added to it.
     * @since 1.6.4
     */
    public static DefaultListModel<?> leftShift(DefaultListModel<Object> self, Object e) {
        self.addElement(e);
        return self;
    }

    /**
     * Allow DefaultListModel to work with subscript operators.<p>
     * <b>WARNING:</b> this operation does not replace the element at the
     * specified index, rather it inserts the element at that index, thus
     * increasing the size of of the model by 1.
     *
     * @param self  a DefaultListModel
     * @param index an index
     * @param e     the element to insert at the given index
     * @since 1.6.4
     */
    public static void putAt(DefaultListModel<Object> self, int index, Object e) {
        self.set(index, e);
    }

    /**
     * Removes all elements from the DefaultListModel.
     *
     * @param self a DefaultListModel
     * @since 1.6.4
     */
    public static void clear(DefaultListModel<?> self) {
        self.removeAllElements();
    }

    /**
     * Returns an {@link java.util.Iterator} which traverses the DefaultListModel one element at a time.
     *
     * @param self a DefaultListModel
     * @return an Iterator for a DefaultListModel
     * @since 1.6.4
     */
    public static Iterator<?> iterator(final DefaultListModel<Object> self) {
        return new Iterator<Object>() {
            private int index = 0;

            public boolean hasNext() {
                return index > -1 && index < self.getSize();
            }

            public Object next() {
                return self.getElementAt(index++);
            }

            public void remove() {
                if (hasNext()) self.removeElementAt(index--);
            }
        };
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>JComboBox</code>.
     *
     * @param self a JComboBox
     * @return the item count of the comboBox
     * @since 1.6.4
     */
    public static int size(JComboBox self) {
        return self.getItemCount();
    }

    /**
     * Support the subscript operator for JComboBox.
     *
     * @param self  a JComboBox
     * @param index the index of the item to get
     * @return the tem at the given index
     * @since 1.6.4
     */
    public static Object getAt(JComboBox self, int index) {
        return self.getItemAt(index);
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * items to a JComboBox.
     *
     * @param self a JComboBox
     * @param i    an item to be added to the comboBox.
     * @return same comboBox, after the value was added to it.
     * @since 1.6.4
     */
    public static JComboBox<?> leftShift(JComboBox<Object> self, Object i) {
        self.addItem(i);
        return self;
    }

    /**
     * Removes all items from the JComboBox.
     *
     * @param self a JComboBox
     * @since 1.6.4
     */
    public static void clear(JComboBox<?> self) {
        self.removeAllItems();
    }

    /**
     * Returns an {@link java.util.Iterator} which traverses the ComboBox one item at a time.
     *
     * @param self a ComboBox
     * @return an Iterator for a ComboBox
     * @since 1.6.4
     */
    public static Iterator<?> iterator(JComboBox<Object> self) {
        return iterator(self.getModel());
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * items to a MutableComboBoxModel.
     *
     * @param self a MutableComboBoxModel
     * @param i    an item to be added to the model.
     * @return same model, after the value was added to it.
     * @since 1.6.4
     */
    public static MutableComboBoxModel<?> leftShift(MutableComboBoxModel<Object> self, Object i) {
        self.addElement(i);
        return self;
    }

    /**
     * Allow MutableComboBoxModel to work with subscript operators.<p>
     * <b>WARNING:</b> this operation does not replace the item at the
     * specified index, rather it inserts the item at that index, thus
     * increasing the size of the model by 1.
     *
     * @param self  a MutableComboBoxModel
     * @param index an index
     * @param i     the item to insert at the given index
     * @since 1.6.4
     */
    public static void putAt(MutableComboBoxModel<Object> self, int index, Object i) {
        self.insertElementAt(i, index);
    }

    /**
     * Returns an {@link java.util.Iterator} which traverses the MutableComboBoxModel one item at a time.
     *
     * @param self a MutableComboBoxModel
     * @return an Iterator for a MutableComboBoxModel
     * @since 1.6.4
     */
    public static Iterator<?> iterator(final MutableComboBoxModel<Object> self) {
        return new Iterator<Object>() {
            private int index = 0;

            public boolean hasNext() {
                return index > -1 && index < self.getSize();
            }

            public Object next() {
                return self.getElementAt(index++);
            }

            public void remove() {
                if (hasNext()) self.removeElementAt(index--);
            }
        };
    }

    /**
     * Removes all items from the model.
     *
     * @param self a DefaultComboBoxModel
     * @since 1.7.3
     */
    public static void clear(DefaultComboBoxModel<?> self) {
        self.removeAllElements();
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>TableModel</code>.
     *
     * @param self a TableModel
     * @return the row count of the TableModel
     * @since 1.6.4
     */
    public static int size(TableModel self) {
        return self.getRowCount();
    }

    /**
     * Support the subscript operator for TableModel.
     *
     * @param self  a TableModel
     * @param index the index of the row to get
     * @return the row at the given index
     * @since 1.6.4
     */
    public static Object[] getAt(TableModel self, int index) {
        int cols = self.getColumnCount();
        Object[] rowData = new Object[cols];
        for (int col = 0; col < cols; col++) {
            rowData[col] = self.getValueAt(index, col);
        }
        return rowData;
    }

    /**
     * Returns an {@link java.util.Iterator} which traverses the TableModel one row at a time.
     *
     * @param self a TableModel
     * @return an Iterator for a TableModel
     * @since 1.6.4
     */
    public static Iterator<?> iterator(final TableModel self) {
        return new Iterator<Object>() {
            private int row = 0;

            public boolean hasNext() {
                return row < self.getRowCount();
            }

            public Object next() {
                int cols = self.getColumnCount();
                Object[] rowData = new Object[cols];
                for (int col = 0; col < cols; col++) {
                    rowData[col] = self.getValueAt(row, col);
                }
                row++;
                return rowData;
            }

            public void remove() {
                throw new UnsupportedOperationException("TableModel is immutable.");
            }
        };
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * rows to a DefaultTableModel.
     * <p>
     * if row.size &lt; model.size -&gt; row will be padded with nulls<br>
     * if row.size &gt; model.size -&gt; additional columns will be discarded<br>
     *
     * @param self a DefaultTableModel
     * @param row  a row to be added to the model.
     * @return same model, after the value was added to it.
     * @since 1.6.4
     */
    public static DefaultTableModel leftShift(DefaultTableModel self, Object row) {
        if (row == null) {
            // adds an empty row
            self.addRow((Object[]) null);
            return self;
        }
        self.addRow(buildRowData(self, row));
        return self;
    }

    /**
     * Allow DefaultTableModel to work with subscript operators.<p>
     * <b>WARNING:</b> this operation does not replace the item at the
     * specified index, rather it inserts the item at that index, thus
     * increasing the size of the model by 1.<p>
     * <p>
     * if row.size &lt; model.size -&gt; row will be padded with nulls<br>
     * if row.size &gt; model.size -&gt; additional columns will be discarded
     *
     * @param self  a DefaultTableModel
     * @param index an index
     * @param row   the row to insert at the given index
     * @since 1.6.4
     */
    public static void putAt(DefaultTableModel self, int index, Object row) {
        if (row == null) {
            // adds an empty row
            self.insertRow(index, (Object[]) null);
            return;
        }
        self.insertRow(index, buildRowData(self, row));
    }

    private static Object[] buildRowData(DefaultTableModel delegate, Object row) {
        int cols = delegate.getColumnCount();
        Object[] rowData = new Object[cols];
        int i = 0;
        for (Iterator<?> it = DefaultGroovyMethods.iterator(row); it.hasNext() && i < cols;) {
            rowData[i++] = it.next();
        }
        return rowData;
    }

    /**
     * Returns an {@link java.util.Iterator} which traverses the DefaultTableModel one item at a time.
     *
     * @param self a DefaultTableModel
     * @return an Iterator for a DefaultTableModel
     * @since 1.6.4
     */
    public static Iterator<?> iterator(final DefaultTableModel self) {
        return new Iterator<Object>() {
            private int row = 0;

            public boolean hasNext() {
                return row > -1 && row < self.getRowCount();
            }

            public Object next() {
                int cols = self.getColumnCount();
                Object[] rowData = new Object[cols];
                for (int col = 0; col < cols; col++) {
                    rowData[col] = self.getValueAt(row, col);
                }
                row++;
                return rowData;
            }

            public void remove() {
                if (hasNext()) self.removeRow(row--);
            }
        };
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>TableColumnModel</code>.
     *
     * @param self a TableColumnModel
     * @return the column count of the TableColumnModel
     * @since 1.6.4
     */
    public static int size(TableColumnModel self) {
        return self.getColumnCount();
    }

    /**
     * Support the subscript operator for TableColumnModel.
     *
     * @param self  a TableColumnModel
     * @param index the index of the column to get
     * @return the column at the given index
     * @since 1.6.4
     */
    public static TableColumn getAt(TableColumnModel self, int index) {
        return self.getColumn(index);
    }

    /**
     * Returns an {@link java.util.Iterator} which traverses the TableColumnModel one TableColumn at a time.
     *
     * @param self a TableColumnModel
     * @return an Iterator for a TableColumnModel
     * @since 1.6.4
     */
    public static Iterator<TableColumn> iterator(final TableColumnModel self) {
        return new Iterator<TableColumn>() {
            private int index = 0;

            public boolean hasNext() {
                return index > -1 && index < self.getColumnCount();
            }

            public TableColumn next() {
                return self.getColumn(index++);
            }

            public void remove() {
                if (hasNext()) self.removeColumn(self.getColumn(index--));
            }
        };
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * columns to a TableColumnModel.
     *
     * @param self   a TableColumnModel
     * @param column a TableColumn to be added to the model.
     * @return same model, after the value was added to it.
     * @since 1.6.4
     */
    public static TableColumnModel leftShift(TableColumnModel self, TableColumn column) {
        self.addColumn(column);
        return self;
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>TreePath</code>.
     *
     * @param self a TreePath
     * @return the path count of the treePath
     * @since 1.6.4
     */
    public static int size(TreePath self) {
        return self.getPathCount();
    }

    /**
     * Support the subscript operator for TreePath.
     *
     * @param self  a TreePath
     * @param index the index of the path to get
     * @return the path at the given index
     * @since 1.6.4
     */
    public static Object getAt(TreePath self, int index) {
        return self.getPath()[index];
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * paths to a treePath.<p>
     * <b>WARNING:</b> this operation returns a new TreePath, not the original one.<p>
     *
     * @param self a TreePath
     * @param p    an object to be added to the treePath.
     * @return same treePath, after the value was added to it.
     * @since 1.6.4
     */
    public static TreePath leftShift(TreePath self, Object p) {
        return self.pathByAddingChild(p);
    }

    /**
     * Returns an {@link java.util.Iterator} which traverses the TreePath one path at a time.
     *
     * @param self a TreePath
     * @return an Iterator for a TreePath
     * @since 1.6.4
     */
    public static Iterator<?> iterator(TreePath self) {
        return DefaultGroovyMethods.iterator(self.getPath());
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>TreeNode</code>.
     *
     * @param self a TreeNode
     * @return the child count of the treeNode
     * @since 1.6.4
     */
    public static int size(TreeNode self) {
        return self.getChildCount();
    }

    /**
     * Support the subscript operator for TreeNode.
     *
     * @param self  a TreeNode
     * @param index the index of the child node to get
     * @return the child node at the given index
     * @since 1.6.4
     */
    public static TreeNode getAt(TreeNode self, int index) {
        return self.getChildAt(index);
    }

    /**
     * Returns an {@link java.util.Iterator} which traverses the TreeNode one node at a time.
     *
     * @param self a TreeNode
     * @return an Iterator for a TreeNode
     * @since 1.6.4
     */
    @SuppressWarnings("unchecked")
    public static Iterator<TreeNode> iterator(TreeNode self) {
        return (Iterator<TreeNode>) DefaultGroovyMethods.iterator(self.children());
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * nodes to a MutableTreeNode.<p>
     *
     * @param self a MutableTreeNode
     * @param node a node to be added to the treeNode.
     * @return same treeNode, after the value was added to it.
     * @since 1.6.4
     */
    public static MutableTreeNode leftShift(MutableTreeNode self, MutableTreeNode node) {
        self.insert(node, self.getChildCount());
        return self;
    }

    /**
     * Allow MutableTreeNode to work with subscript operators.<p>
     * <b>WARNING:</b> this operation does not replace the node at the
     * specified index, rather it inserts the node at that index, thus
     * increasing the size of the treeNode by 1.<p>
     *
     * @param self  a MutableTreeNode
     * @param index an index
     * @param node  the node to insert at the given index
     * @since 1.6.4
     */
    public static void putAt(MutableTreeNode self, int index, MutableTreeNode node) {
        self.insert(node, index);
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * nodes to a DefaultMutableTreeNode.<p>
     *
     * @param self a DefaultMutableTreeNode
     * @param node a node to be added to the treeNode.
     * @return same treeNode, after the value was added to it.
     * @since 1.6.4
     */
    public static DefaultMutableTreeNode leftShift(DefaultMutableTreeNode self, DefaultMutableTreeNode node) {
        self.add(node);
        return self;
    }


    /**
     * Removes all children nodes from the DefaultMutableTreeNode.
     *
     * @param self a DefaultMutableTreeNode
     * @since 1.6.4
     */
    public static void clear(DefaultMutableTreeNode self) {
        self.removeAllChildren();
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>JMenu</code>.
     *
     * @param self a JMenu
     * @return the menu component count of the menu
     * @since 1.6.4
     */
    public static int size(JMenu self) {
        return self.getMenuComponentCount();
    }

    /**
     * Support the subscript operator for JMenu.
     *
     * @param self  a JMenu
     * @param index the index of the menu component to get
     * @return the menu component at the given index
     * @since 1.6.4
     */
    public static Component getAt(JMenu self, int index) {
        return self.getMenuComponent(index);
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * components to a menu.<p>
     *
     * @param self   a JMenu
     * @param action an action to be added to the menu.
     * @return same menu, after the value was added to it.
     * @since 1.6.4
     */
    public static JMenu leftShift(JMenu self, Action action) {
        self.add(action);
        return self;
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * components to a menu.<p>
     *
     * @param self      a JMenu
     * @param component a component to be added to the menu.
     * @return same menu, after the value was added to it.
     * @since 1.6.4
     */
    public static JMenu leftShift(JMenu self, Component component) {
        self.add(component);
        return self;
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * components to a menu.<p>
     *
     * @param self a JMenu
     * @param item an item to be added to the menu.
     * @return same menu, after the value was added to it.
     * @since 1.6.4
     */
    public static JMenu leftShift(JMenu self, JMenuItem item) {
        self.add(item);
        return self;
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * components to a menu.<p>
     *
     * @param self a JMenu
     * @param str  a String to be added to the menu.
     * @return same menu, after the value was added to it.
     * @since 1.6.4
     */
    public static JMenu leftShift(JMenu self, String str) {
        self.add(str);
        return self;
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * components to a menu.<p>
     *
     * @param self a JMenu
     * @param gstr a GString to be added to the menu.
     * @return same menu, after the value was added to it.
     * @since 1.6.4
     */
    public static JMenu leftShift(JMenu self, GString gstr) {
        self.add(gstr.toString());
        return self;
    }

    /**
     * Returns an {@link java.util.Iterator} which traverses the JMenu one component at a time.
     *
     * @param self a JMenu
     * @return an Iterator for a JMenu
     * @since 1.6.4
     */
    public static Iterator<Component> iterator(JMenu self) {
        return DefaultGroovyMethods.iterator(self.getMenuComponents());
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>JMenuBar</code>.
     *
     * @param self a JMenuBar
     * @return the menu count of the menuBar
     * @since 1.6.4
     */
    public static int size(JMenuBar self) {
        return self.getMenuCount();
    }

    /**
     * Support the subscript operator for JMenuBar.
     *
     * @param self  a JMenuBar
     * @param index the index of the menu to get
     * @return the menu at the given index
     * @since 1.6.4
     */
    public static JMenu getAt(JMenuBar self, int index) {
        return self.getMenu(index);
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * menus to a menuBar.<p>
     *
     * @param self a JMenuBar
     * @param menu a menu to be added to the menuBar.
     * @return same menuBar, after the value was added to it.
     * @since 1.6.4
     */
    public static JMenuBar leftShift(JMenuBar self, JMenu menu) {
        self.add(menu);
        return self;
    }

    /**
     * Returns an {@link java.util.Iterator} which traverses the JMenuBar one menu at a time.
     *
     * @param self a JMenuBar
     * @return an Iterator for a JMenuBar
     * @since 1.6.4
     */
    public static Iterator<MenuElement> iterator(JMenuBar self) {
        return DefaultGroovyMethods.iterator(self.getSubElements());
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * components to a popupMenu.<p>
     *
     * @param self   a JPopupMenu
     * @param action an action to be added to the popupMenu.
     * @return same popupMenu, after the value was added to it.
     * @since 1.6.4
     */
    public static JPopupMenu leftShift(JPopupMenu self, Action action) {
        self.add(action);
        return self;
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * components to a popupMenu.<p>
     *
     * @param self      a JPopupMenu
     * @param component a component to be added to the popupMenu.
     * @return same popupMenu, after the value was added to it.
     * @since 1.6.4
     */
    public static JPopupMenu leftShift(JPopupMenu self, Component component) {
        self.add(component);
        return self;
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * components to a popupMenu.<p>
     *
     * @param self a JPopupMenu
     * @param item an item to be added to the popupMenu.
     * @return same popupMenu, after the value was added to it.
     * @since 1.6.4
     */
    public static JPopupMenu leftShift(JPopupMenu self, JMenuItem item) {
        self.add(item);
        return self;
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * components to a popupMenu.<p>
     *
     * @param self a JPopupMenu
     * @param str  a String to be added to the popupMenu.
     * @return same popupMenu, after the value was added to it.
     * @since 1.6.4
     */
    public static JPopupMenu leftShift(JPopupMenu self, String str) {
        self.add(str);
        return self;
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * components to a popupMenu.<p>
     *
     * @param self a JPopupMenu
     * @param gstr a GString to be added to the popupMenu.
     * @return same popupMenu, after the value was added to it.
     * @since 1.6.4
     */
    public static JPopupMenu leftShift(JPopupMenu self, GString gstr) {
        self.add(gstr.toString());
        return self;
    }

    /**
     * Returns an {@link java.util.Iterator} which traverses the JPopupMenu one MenuElement at a time.
     *
     * @param self a JPopupMenu
     * @return an Iterator for a JPopupMenu
     * @since 1.6.4
     */
    public static Iterator<MenuElement> iterator(JPopupMenu self) {
        return DefaultGroovyMethods.iterator(self.getSubElements());
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>JTabbedPane</code>.
     *
     * @param self a JTabbedPane
     * @return the tab count of the tabbedPane
     * @since 1.6.4
     */
    public static int size(JTabbedPane self) {
        return self.getTabCount();
    }

    /**
     * Removes all elements from the JTabbedPane.
     *
     * @param self a JTabbedPane
     * @since 1.6.4
     */
    public static void clear(JTabbedPane self) {
        self.removeAll();
    }

    /**
     * Support the subscript operator for JTabbedPane.
     *
     * @param self  a JTabbedPane
     * @param index the index of the tab component to get
     * @return the component at the given index
     * @since 1.6.4
     */
    public static Component getAt(JTabbedPane self, int index) {
        return self.getComponentAt(index);
    }

    /**
     * Returns an {@link java.util.Iterator} which traverses the JTabbedPane one Component tab at a time.
     *
     * @param self a JTabbedPane
     * @return an Iterator for a JTabbedPane
     * @since 1.6.4
     */
    public static Iterator<Component> iterator(final JTabbedPane self) {
        return new Iterator<Component>() {
            private int index = 0;

            public boolean hasNext() {
                return index > -1 && index < self.getTabCount();
            }

            public Component next() {
                return self.getComponentAt(index++);
            }

            public void remove() {
                if (hasNext()) self.removeTabAt(index--);
            }
        };
    }

    /**
     * Overloads the left shift operator to provide an easy way to add
     * components to a toolBar.<p>
     *
     * @param self   a JToolBar
     * @param action an Action to be added to the toolBar.
     * @return same toolBar, after the value was added to it.
     * @since 1.6.4
     */
    public static JToolBar leftShift(JToolBar self, Action action) {
        self.add(action);
        return self;
    }

    /**
     * Support the subscript operator for JToolBar.
     *
     * @param self  a JToolBar
     * @param index the index of the tab component to get
     * @return the tab component at the given index
     * @since 1.6.4
     */
    public static Component getAt(JToolBar self, int index) {
        return self.getComponentAtIndex(index);
    }

    /**
     * Allows the usage of a one-element string for a mnemonic
     * @param button a AbstractButton
     * @param mnemonic the String
     * @since 2.3.7
     */
    public static void setMnemonic(AbstractButton button, String mnemonic) {
        char c = ShortTypeHandling.castToChar(mnemonic);
        button.setMnemonic(c);
    }
}
