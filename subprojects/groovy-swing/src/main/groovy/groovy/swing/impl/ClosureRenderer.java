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

public class ClosureRenderer implements ListCellRenderer, TableCellRenderer, TreeCellRenderer {

    Closure update;
    List children = new ArrayList();

    JList list;
    JTable table;
    JTree tree;
    Object value;
    boolean selected;
    boolean focused;
    boolean leaf;
    boolean expanded;
    int row;
    int column;
    boolean tableHeader;
    private boolean defaultRenderer;

    public ClosureRenderer() {
        this(null);
    }

    public ClosureRenderer(Closure c) {
        setUpdate(c);
    }


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

    public Closure getUpdate() {
        return update;
    }

    public void setUpdate(Closure update) {
        if (update != null) {
            update.setDelegate(this);
            update.setResolveStrategy(Closure.DELEGATE_FIRST);
        }
        this.update = update;
    }

    public void setTableHeader(boolean tableHeader) {
        this.tableHeader = tableHeader;
    }

    public boolean isTableHeader() {
        return tableHeader;
    }

    public List getChildren() {
        return children;
    }

    public JList getList() {
        return list;
    }

    public JTable getTable() {
        return table;
    }

    public Object getValue() {
        return value;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isFocused() {
        return focused;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public JTree getTree() {
        return tree;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean isDefaultRenderer() {
        return defaultRenderer;
    }
}
