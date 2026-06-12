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

/**
 * Cell editor implementation backed by builder closures for tables and trees.
 */
class ClosureCellEditor extends AbstractCellEditor implements TableCellEditor, TreeCellEditor {
    private static final long serialVersionUID = -5924126854559540546L

    /**
     * Optional callback closures invoked by {@link #invokeMethod(String, Object)}.
     */
    Map<String, Closure> callbacks = [:]
    /**
     * Closure used to create or customize the editor component.
     */
    Closure prepareEditor
    /**
     * Closure used to extract the current editor value.
     */
    Closure editorValue
    /**
     * Child components made available to editor closures.
     */
    List children = []
    /**
     * Indicates whether the default Swing editor should be used as the backing child component.
     */
    boolean defaultEditor

    /**
     * Table currently being edited, or {@code null} for tree editing.
     */
    JTable table
    /**
     * Tree currently being edited, or {@code null} for table editing.
     */
    JTree tree
    /**
     * Value currently being edited.
     */
    Object value
    /**
     * Whether the edited row is selected.
     */
    boolean selected
    /**
     * Whether the edited tree node is expanded.
     */
    boolean expanded
    /**
     * Whether the edited tree node is a leaf.
     */
    boolean leaf
    /**
     * Row currently being edited.
     */
    int row
    /**
     * Table column currently being edited, or {@code -1} for tree editing.
     */
    int column

    /**
     * Creates a closure-backed cell editor.
     *
     * @param c closure used to produce the editor value
     * @param callbacks optional method callback overrides
     */
    ClosureCellEditor(Closure c = null, Map<String, Closure> callbacks = [:]) {
        this.editorValue = c
        this.callbacks.putAll(callbacks)
    }

    /**
     * Prepares the editor context for table editing and returns the editor component.
     *
     * @param table the table requesting the editor
     * @param value the cell value being edited
     * @param isSelected whether the row is selected
     * @param row the view row index
     * @param column the view column index
     * @return the component used to edit the table cell
     */
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

    /**
     * Prepares the editor context for tree editing and returns the editor component.
     *
     * @param tree the tree requesting the editor
     * @param value the node value being edited
     * @param isSelected whether the node is selected
     * @param expanded whether the node is expanded
     * @param leaf whether the node is a leaf
     * @param row the visible row index
     * @return the component used to edit the tree node
     */
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

    /**
     * Returns the value supplied by the configured editor value closure.
     *
     * @return the edited value
     */
    @Override
    Object getCellEditorValue() {
        editorValue.call()
    }

    /**
     * Sets the closure used to extract the editor value.
     *
     * <p>The closure delegates to this editor with {@link Closure#DELEGATE_FIRST} resolution.</p>
     *
     * @param editorValue the value-producing closure
     */
    void setEditorValue(Closure editorValue) {
        if (editorValue != null) {
            editorValue.delegate = this
            editorValue.resolveStrategy = Closure.DELEGATE_FIRST
        }
        this.editorValue = editorValue
    }

    /**
     * Sets the closure used to prepare the editor component.
     *
     * <p>The closure delegates to this editor with {@link Closure#DELEGATE_FIRST} resolution.</p>
     *
     * @param prepareEditor the preparation closure
     */
    void setPrepareEditor(Closure prepareEditor) {
        if (prepareEditor != null) {
            prepareEditor.delegate = this
            prepareEditor.resolveStrategy = Closure.DELEGATE_FIRST
        }
        this.prepareEditor = prepareEditor
    }

    /**
     * Dispatches method invocations to registered callbacks before falling back to the normal meta-class lookup.
     *
     * @param name the invoked method name
     * @param args the invocation arguments
     * @return the callback result or the invoked method result
     */
    @Override
    Object invokeMethod(String name, Object args) {
        def calledMethod = ClosureCellEditor.metaClass.getMetaMethod(name, args)
        if (callbacks."$name" && callbacks."$name" instanceof Closure)
            return callbacks."$name".call(calledMethod, this, args)
        else
            return calledMethod?.invoke(this, args)
    }
}
