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

import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single row in a {@link TableLayout}.
 */
public class TableLayoutRow {

    private final TableLayout parent;
    private final List<groovy.swing.impl.TableLayoutCell> cells = new ArrayList<groovy.swing.impl.TableLayoutCell>();
    private int rowIndex;

    /**
     * Creates a row for the supplied table layout.
     *
     * @param tableLayoutTag the owning table layout
     */
    public TableLayoutRow(TableLayout tableLayoutTag) {
        this.parent = tableLayoutTag;
    }

    /**
     * Adds a new cell to this row
     * @param tag the td element
     */
    public void addCell(groovy.swing.impl.TableLayoutCell tag) {
        int gridx = 0;
        for (TableLayoutCell cell : cells) {
            gridx += cell.getColspan();
        }
        tag.getConstraints().gridx = gridx;
        cells.add(tag);
    }

    /**
     * Materializes the row into the parent layout by assigning row coordinates to each cell.
     */
    public void addComponentsForRow() {
        rowIndex = parent.nextRowIndex();

        // iterate through the rows and add each one to the layout...
        for (TableLayoutCell cell : cells) {
            GridBagConstraints c = cell.getConstraints();
            c.gridy = rowIndex;
            // add the cell to the table
            parent.addCell(cell);
        }
    }

    /**
     * @return the row index of this row
     */
    public int getRowIndex() {
        return rowIndex;
    }

}
