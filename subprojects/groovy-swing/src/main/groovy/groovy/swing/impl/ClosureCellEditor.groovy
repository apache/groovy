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
package groovy.swing.impl

import javax.swing.*
import javax.swing.table.TableCellEditor
import javax.swing.tree.TreeCellEditor
import java.awt.*
import java.util.List

class ClosureCellEditor extends AbstractCellEditor implements TableCellEditor, TreeCellEditor {
    Map<String, Closure> callbacks = [:]
    Closure prepareEditor
    Closure editorValue
    List children = []
    boolean defaultEditor

    JTable table
    JTree tree
    Object value
    boolean selected
    boolean expanded
    boolean leaf
    int row
    int column

    ClosureCellEditor(Closure c = null, Map<String, Closure> callbacks = [:]) {
        this.editorValue = c
        this.callbacks.putAll(callbacks)
    }

    @Override
    Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.table = table
        this.tree = null
        this.value = value
        this.selected = isSelected
        this.expanded = false
        this.leaf = false
        this.row = row
        this.column = column

        return prepare();
    }

    Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        this.table = null
        this.tree = tree
        this.value = value
        this.selected = isSelected
        this.expanded = expanded
        this.leaf = leaf
        this.row = row
        this.column = -1

        return prepare();
    }

    private Component prepare() {
        if (children.isEmpty() || defaultEditor) {
            defaultEditor = true
            children.clear()
            if (table) {
                TableCellEditor tce = table.getDefaultEditor(table.getColumnClass(column))
                children.add(tce.getTableCellEditorComponent(table, value, selected, row, column))
            } else if (tree) {
                TreeCellEditor tce = new DefaultCellEditor(new JTextField())
                children.add(tce.getTreeCellEditorComponent(tree, value, selected, expanded, leaf, row))
            }
        }
        Object o = prepareEditor.call()
        if (o instanceof Component) {
            return (Component) o
        } else {
            return (Component) children[0]
        }
    }

    @Override
    Object getCellEditorValue() {
        editorValue.call()
    }

    void setEditorValue(Closure editorValue) {
        if (editorValue != null) {
            editorValue.delegate = this
            editorValue.resolveStrategy = Closure.DELEGATE_FIRST
        }
        this.editorValue = editorValue
    }

    void setPrepareEditor(Closure prepareEditor) {
        if (prepareEditor != null) {
            prepareEditor.delegate = this
            prepareEditor.resolveStrategy = Closure.DELEGATE_FIRST
        }
        this.prepareEditor = prepareEditor
    }

    @Override
    Object invokeMethod(String name, Object args) {
        def calledMethod = ClosureCellEditor.metaClass.getMetaMethod(name, args)
        if (callbacks."$name" && callbacks."$name" instanceof Closure)
            return callbacks."$name".call(calledMethod, this, args)
        else
            return calledMethod?.invoke(this, args)
    }
}
