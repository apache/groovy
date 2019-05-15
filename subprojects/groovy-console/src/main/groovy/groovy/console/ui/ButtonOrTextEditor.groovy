/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package groovy.inspect.swingui

import groovy.transform.CompileStatic

import javax.swing.*
import javax.swing.table.TableCellEditor
import java.awt.*
import java.awt.event.ActionListener
import java.awt.event.FocusListener

/**
 * A table cell editor that will return a button automatically if it is the cell value,
 * a text field if the value exists, or null otherwise (non editable cell).
 * This hack allows to interact with buttons in a cell.
 */
@CompileStatic
class ButtonOrTextEditor extends AbstractCellEditor implements TableCellEditor {
    /** The Swing component being edited. */
    protected JComponent editorComponent

    @Override
    Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof JButton) {
            editorComponent = value
            ((JButton) editorComponent).addActionListener({ fireEditingStopped() } as ActionListener)
        } else if (value instanceof JTextArea) {
            editorComponent = value
        } else if (value) {
            editorComponent = new JTextArea(value.toString())
            editorComponent.addFocusListener({ fireEditingCanceled() } as FocusListener)
        } else {
            editorComponent = null
        }
        editorComponent
    }

    @Override
    Object getCellEditorValue() {
        editorComponent
    }
}

