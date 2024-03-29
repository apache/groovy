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

import javax.swing.JTable
import java.awt.event.MouseEvent

class CellValueToolTipJTable extends JTable {
    private static final long serialVersionUID = 7112943034646629299L

    String getToolTipText(MouseEvent me) {
        int viewRowIndex = rowAtPoint(me.point)
        int viewColumnIndex = columnAtPoint(me.point)

        def value = getValueAt(viewRowIndex, viewColumnIndex)

        return (value != null ? String.valueOf(value) : null)
    }
}
