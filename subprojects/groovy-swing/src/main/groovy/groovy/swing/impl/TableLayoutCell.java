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

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 * Represents a cell in a table layout.
 */
public class TableLayoutCell {

    protected static final Logger LOG = Logger.getLogger(TableLayoutCell.class.getName());
    
    private TableLayoutRow parent;
    private Component component;
    private GridBagConstraints constraints;
    private String align;
    private String valign;

    public int getColspan() {
        return colspan;
    }

    public int getRowspan() {
        return rowspan;
    }

    private int colspan = 1;
    private int rowspan = 1;
    private boolean colfill;
    private boolean rowfill;

        
    public TableLayoutCell(TableLayoutRow parent) {
        this.parent = parent;
    }

    public void addComponent(Component component)  {
        if (this.component != null) {
            LOG.log(Level.WARNING, "This td cell already has a component: " + component);
        }
        this.component = component;
        parent.addCell(this);
    }
    
    public Component getComponent() {
        return component;
    }

    /**
     * Sets the horizontal alignment to a case insensitive value of {LEFT, CENTER, RIGHT}
     * @param align one of  'left', 'center', or 'right'
     */
    public void setAlign(String align) {
        this.align = align;
    }

    /**
     * Sets the vertical alignment to a case insensitive value of {TOP, MIDDLE, BOTTOM}
     * @param valign one of 'top', 'middle', 'bottom'
     */
    public void setValign(String valign) {
        this.valign = valign;
    }

    
    /**
     * Sets the number of columns that this cell should span. The default value is 1
     * @param colspan The default is 1
     */
    public void setColspan(int colspan) {
        this.colspan = colspan;
    }

    /**
     * Sets the number of rows that this cell should span. The default value is 1
     * @param rowspan The default is 1
     */
    public void setRowspan(int rowspan) {
        this.rowspan = rowspan;
    }

    /**
     * Returns the colfill.
     * @return boolean
     */
    public boolean isColfill() {
        return colfill;
    }

    /**
     * Returns the rowfill.
     * @return boolean
     */
    public boolean isRowfill() {
        return rowfill;
    }

    /**
     * Sets whether or not this column should allow its component to stretch to fill the space available
     * @param colfill The default is false
     */
    public void setColfill(boolean colfill) {
        this.colfill = colfill;
    }

    /**
     * Sets whether or not this row should allow its component to stretch to fill the space available
     * @param rowfill The default is false
     */
    public void setRowfill(boolean rowfill) {
        this.rowfill = rowfill;
    }


    /**
     * @return the constraints of this cell
     */
    public GridBagConstraints getConstraints() {
        if (constraints == null) {
            constraints = createConstraints();
        }
        return constraints;
    }
    
    // Implementation methods
    //-------------------------------------------------------------------------                    
    
    protected GridBagConstraints createConstraints() {
        GridBagConstraints answer = new GridBagConstraints();
        answer.anchor = getAnchor();
        if (colspan < 1) {
            colspan = 1;
        }
        if (rowspan < 1) {
            rowspan = 1;
        }
        if (isColfill())  {
            answer.fill = isRowfill()
                ? GridBagConstraints.BOTH 
                : GridBagConstraints.HORIZONTAL;
        }
        else {
            answer.fill = isRowfill()
                ? GridBagConstraints.VERTICAL 
                : GridBagConstraints.NONE;
        }
        answer.weightx = 0.2;
        answer.weighty = 0;
        answer.gridwidth = colspan;
        answer.gridheight = rowspan;
        return answer;
    }
    
    /**
     * @return the GridBagConstraints enumeration for anchor
     */
    protected int getAnchor() {
        boolean isTop = "top".equalsIgnoreCase(valign);
        boolean isBottom = "bottom".equalsIgnoreCase(valign);
        
        if ("center".equalsIgnoreCase(align)) {
            if (isTop) {
                return GridBagConstraints.NORTH;
            }
            else if (isBottom) {
                return GridBagConstraints.SOUTH;
            }
            else {
                return GridBagConstraints.CENTER;
            }
        }
        else if ("right".equalsIgnoreCase(align)) {
            if (isTop) {
                return GridBagConstraints.NORTHEAST;
            }
            else if (isBottom) {
                return GridBagConstraints.SOUTHEAST;
            }
            else {
                return GridBagConstraints.EAST;
            }
        }
        else {
            // defaults to left
            if (isTop) {
                return GridBagConstraints.NORTHWEST;
            }
            else if (isBottom) {
                return GridBagConstraints.SOUTHWEST;
            }
            else {
                return GridBagConstraints.WEST;
            }
        }
    }
}
