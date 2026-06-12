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
package groovy.swing.impl;

import groovy.lang.Closure;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared Swing renderer that delegates rendering customization to a Groovy closure.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ClosureRenderer implements ListCellRenderer, TableCellRenderer, TreeCellRenderer {

    /**
     * The closure invoked during rendering.
     */
    Closure update;
    /**
     * Renderer children made available to the update closure.
     */
    List children = new ArrayList();

    /**
     * The current list being rendered, if any.
     */
    JList list;
    /**
     * The current table being rendered, if any.
     */
    JTable table;
    /**
     * The current tree being rendered, if any.
     */
    JTree tree;
    /**
     * The current cell value.
     */
    Object value;
    /**
     * Whether the current cell is selected.
     */
    boolean selected;
    /**
     * Whether the current cell has focus.
     */
    boolean focused;
    /**
     * Whether the current tree node is a leaf.
     */
    boolean leaf;
    /**
     * Whether the current tree node is expanded.
     */
    boolean expanded;
    /**
     * The current row index.
     */
    int row;
    /**
     * The current column index, or {@code -1} when not applicable.
     */
    int column;
    /**
     * Whether table rendering should use the header renderer.
     */
    boolean tableHeader;
    private boolean defaultRenderer;

    /**
     * Creates a renderer with no update closure.
     */
    public ClosureRenderer() {
        this(null);
    }

    /**
     * Creates a renderer that delegates to the supplied closure.
     *
     * @param c the update closure
     */
    public ClosureRenderer(Closure c) {
        setUpdate(c);
    }


    /**
     * Prepares list-rendering state and delegates component creation to {@link #render()}.
     *
     * @param list the list being rendered
     * @param value the current cell value
     * @param index the list index being rendered
     * @param isSelected whether the cell is selected
     * @param cellHasFocus whether the cell has focus
     * @return the renderer component
     */
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        this.list = list;
        this.table = null;
        this.tree = null;
        this.value = value;
        this.row = index;
        this.column = -1;
        this.selected = isSelected;
        this.focused = cellHasFocus;
        this.leaf = false;
        this.expanded = false;

        return render();
    }

    /**
     * Prepares table-rendering state and delegates component creation to {@link #render()}.
     *
     * @param table the table being rendered
     * @param value the current cell value
     * @param isSelected whether the cell is selected
     * @param hasFocus whether the cell has focus
     * @param row the model row being rendered
     * @param column the model column being rendered
     * @return the renderer component
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.list = null;
        this.table = table;
        this.tree = null;
        this.value = value;
        this.row = row;
        this.column = column;
        this.selected = isSelected;
        this.focused = hasFocus;
        this.leaf = false;
        this.expanded = false;

        return render();
    }

    /**
     * Prepares tree-rendering state and delegates component creation to {@link #render()}.
     *
     * @param tree the tree being rendered
     * @param value the current node value
     * @param selected whether the row is selected
     * @param expanded whether the node is expanded
     * @param leaf whether the node is a leaf
     * @param row the tree row being rendered
     * @param hasFocus whether the row has focus
     * @return the renderer component
     */
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        this.list = null;
        this.table = null;
        this.tree = tree;
        this.value = value;
        this.row = row;
        this.column = -1;
        this.selected = selected;
        this.focused = hasFocus;
        this.leaf = leaf;
        this.expanded = expanded;

        return render();
    }

    private Component render() {
        if (children.isEmpty() || defaultRenderer) {
            defaultRenderer = true;
            children.clear();
            if (table != null) {
                TableCellRenderer tcr;
                if (tableHeader) {
                    tcr = table.getTableHeader().getDefaultRenderer();
                } else {
                    tcr = table.getDefaultRenderer(table.getColumnClass(column));
                }
                children.add(tcr.getTableCellRendererComponent(table, value, selected, focused, row, column));
            } else if (tree != null) {
                TreeCellRenderer tcr;
                tcr = new DefaultTreeCellRenderer();
                children.add(tcr.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, focused));
            } else if (list != null) {
                ListCellRenderer lcr = (ListCellRenderer) UIManager.get("List.cellRenderer");
                if (lcr == null) {
                    lcr = new DefaultListCellRenderer();
                }
                children.add(lcr.getListCellRendererComponent(list, value, row, selected, focused));
            }
        }
        Object o = update.call();
        if (o instanceof Component) {
            return (Component) o;
        } else {
            return (Component) children.get(0);
        }
    }

    /**
     * Returns the update closure invoked during rendering.
     *
     * @return the update closure
     */
    public Closure getUpdate() {
        return update;
    }

    /**
     * Sets the update closure invoked during rendering.
     *
     * @param update the update closure
     */
    public void setUpdate(Closure update) {
        if (update != null) {
            update.setDelegate(this);
            update.setResolveStrategy(Closure.DELEGATE_FIRST);
        }
        this.update = update;
    }

    /**
     * Configures whether table rendering should use the table-header renderer.
     *
     * @param tableHeader {@code true} to render using the table-header renderer
     */
    public void setTableHeader(boolean tableHeader) {
        this.tableHeader = tableHeader;
    }

    /**
     * Returns whether table rendering uses the table-header renderer.
     *
     * @return {@code true} when header rendering is enabled
     */
    public boolean isTableHeader() {
        return tableHeader;
    }

    /**
     * Returns the renderer children exposed to the update closure.
     *
     * @return the renderer children
     */
    public List getChildren() {
        return children;
    }

    /**
     * Returns the current list being rendered.
     *
     * @return the current list, or {@code null}
     */
    public JList getList() {
        return list;
    }

    /**
     * Returns the current table being rendered.
     *
     * @return the current table, or {@code null}
     */
    public JTable getTable() {
        return table;
    }

    /**
     * Returns the current cell value.
     *
     * @return the current value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns whether the current cell is selected.
     *
     * @return {@code true} when the current cell is selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Returns whether the current cell has focus.
     *
     * @return {@code true} when the current cell has focus
     */
    public boolean isFocused() {
        return focused;
    }

    /**
     * Returns the current row index.
     *
     * @return the current row index
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the current column index.
     *
     * @return the current column index, or {@code -1}
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the current tree being rendered.
     *
     * @return the current tree, or {@code null}
     */
    public JTree getTree() {
        return tree;
    }

    /**
     * Returns whether the current tree node is a leaf.
     *
     * @return {@code true} when the current node is a leaf
     */
    public boolean isLeaf() {
        return leaf;
    }

    /**
     * Returns whether the current tree node is expanded.
     *
     * @return {@code true} when the current node is expanded
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * Returns whether the default renderer was used as the base renderer.
     *
     * @return {@code true} when the default renderer provided the base component
     */
    public boolean isDefaultRenderer() {
        return defaultRenderer;
    }
}
