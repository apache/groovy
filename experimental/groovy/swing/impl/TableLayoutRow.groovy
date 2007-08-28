/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.swing.impl;

import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** 
 * Represents a row in a table layout
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision: 6778 $
 */
public class TableLayoutRow implements Startable {

    private TableLayout parent;
    private List cells = new ArrayList();
    private int rowIndex;
    
    public TableLayoutRow(TableLayout tableLayoutTag) {
        this.parent = tableLayoutTag;
    }

    /**
     * Adds a new cell to this row
     */
    public void addCell(TableLayoutCell tag) {
        int gridx = 0;
        for (TableLayoutCell cell in cells) {
            gridx += cell.getColspan();
        }
        tag.getConstraints().gridx = gridx;
        cells.add(tag);
    }
    
    public void start() {
        rowIndex = parent.nextRowIndex();

        // iterate through the rows and add each one to the layout...
        for (TableLayoutCell cell  in cells) {
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
