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
package groovy.model;

import groovy.lang.Closure;

import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * A default table model made up of PropertyModels on a Value model.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class DefaultTableModel extends AbstractTableModel {

    private ValueModel rowModel;
    private ValueModel rowsModel;
    private MyTableColumnModel columnModel = new MyTableColumnModel();

    public DefaultTableModel(ValueModel rowsModel) {
        this(rowsModel, new ValueHolder());
    }
    
    public DefaultTableModel(ValueModel rowsModel, ValueModel rowModel) {
        this.rowModel = rowModel;
        this.rowsModel = rowsModel;
    }
    
    /**
     * @return the column definitions.
     */
    public List getColumnList() {
        return columnModel.getColumnList();
    }

    public TableColumnModel getColumnModel() {
        return columnModel;
    }
    
    /**
     * Adds a property model column to the table
     */
    public DefaultTableColumn addPropertyColumn(Object headerValue, String property, Class type) {
        return addColumn(headerValue, new PropertyModel(rowModel, property, type));
    }
    
    /**
     * Adds a closure based column to the table
     */
    public DefaultTableColumn addClosureColumn(Object headerValue, Closure readClosure, Closure writeClosure, Class type) {
        return addColumn(headerValue, new ClosureModel(rowModel, readClosure, writeClosure, type));
    }
    
    public DefaultTableColumn addColumn(Object headerValue, ValueModel columnValueModel) {
        DefaultTableColumn answer = new DefaultTableColumn(headerValue, columnValueModel);
        addColumn(answer);
        return answer;
    }
    
    /**
     * Adds a new column definition to the table
     */
    public void addColumn(DefaultTableColumn column) {
        columnModel.addColumn(column);
    }
    
    /**
     * Removes a column definition from the table
     */
    public void removeColumn(DefaultTableColumn column) {
        columnModel.removeColumn(column);
    }
    
    public int getRowCount() {
        return getRows().size();
    }

    public int getColumnCount() {
        return columnModel.getColumnCount();
    }
    
    public String getColumnName(int columnIndex) {
        String answer = null;
        if (columnIndex < 0 || columnIndex >= columnModel.getColumnCount()) {
            return answer;
        }
        Object value = columnModel.getColumn(columnIndex).getHeaderValue();
        if (value != null) {
            return value.toString();
        }
        return answer;
    }

    public Class getColumnClass(int columnIndex) {
        return getColumnModel(columnIndex).getType();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return getColumnModel(columnIndex).isEditable();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        List rows = getRows();
        Object answer = null;
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            return answer;
        }
        if (columnIndex < 0 || columnIndex >= columnModel.getColumnCount()) {
            return answer;
        }
        Object row = getRows().get(rowIndex);
        rowModel.setValue(row);
        DefaultTableColumn column = (DefaultTableColumn) columnModel.getColumn(columnIndex);
        if (row == null || column == null) {
            return answer;
        }
        return column.getValue(row, rowIndex, columnIndex);
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        List rows = getRows();
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            return;
        }
        if (columnIndex < 0 || columnIndex >= columnModel.getColumnCount()) {
            return;
        }
        Object row = getRows().get(rowIndex);
        rowModel.setValue(row);
        DefaultTableColumn column = (DefaultTableColumn) columnModel.getColumn(columnIndex);
        if (row == null || column == null) {
            return;
        }
        column.setValue(row, value, rowIndex, columnIndex);
    }

    protected ValueModel getColumnModel(int columnIndex) {
        DefaultTableColumn column = (DefaultTableColumn) columnModel.getColumn(columnIndex);
        return column.getValueModel();
    }

    protected List getRows() {
        Object value = rowsModel.getValue();
        if (value == null) {
            return Collections.EMPTY_LIST;
        }
        return InvokerHelper.asList(value);
    }

    protected static class MyTableColumnModel extends DefaultTableColumnModel {
        public List getColumnList() {
            return tableColumns;
        }
    }
    
    public ValueModel getRowModel() {
        return rowModel;
    }

    public ValueModel getRowsModel() {
        return rowsModel;
    }

}
