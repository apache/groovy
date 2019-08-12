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
package groovy.inspect.swingui

import groovy.transform.CompileStatic

import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import java.awt.*

/**
 * A table cell renderer that will return a component instead of drawing it,
 * or call the default in the case of a non component object.
 * This hack allows to render a button shape in a table cell.
 */
@CompileStatic
@Deprecated
class ButtonOrDefaultRenderer extends DefaultTableCellRenderer {
    Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof JComponent) {
            value.setSize(Math.round(value.getSize().getWidth())?.toInteger(), table.getRowHeight(row))
            return value
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
    }
}
