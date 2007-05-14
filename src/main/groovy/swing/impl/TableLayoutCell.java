/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package groovy.swing.impl;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 * Represents a cell in a table layout.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class TableLayoutCell implements ContainerFacade {

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
     */
    public void setAlign(String align) {
        this.align = align;
    }

    /**
     * Sets the vertical alignment to a case insensitive value of {TOP, MIDDLE, BOTTOM}
     */
    public void setValign(String valign) {
        this.valign = valign;
    }

    
    /**
     * Sets the number of columns that this cell should span. The default value is 1
     */
    public void setColspan(int colspan) {
        this.colspan = colspan;
    }

    /**
     * Sets the number of rows that this cell should span. The default value is 1
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
     */
    public void setColfill(boolean colfill) {
        this.colfill = colfill;
    }

    /**
     * Sets whether or not this row should allow its component to stretch to fill the space available
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
