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
package groovy.swing.model;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.InvokerHelper;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.util.Collections;
import java.util.List;

/**
 * A default table model made up of PropertyModels on a Value model.
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
        return addColumn(headerValue, property, new PropertyModel(rowModel, property, type));
    }
    
    /**
     * Adds a property model column to the table
     */
    public DefaultTableColumn addPropertyColumn(Object headerValue, String property, Class type, boolean editable) {
        return addColumn(headerValue, property, new PropertyModel(rowModel, property, type, editable));
    }
    
    /**
     * Adds a closure based column to the table
     */
    public DefaultTableColumn addClosureColumn(Object headerValue, Closure readClosure, Closure writeClosure, Class type) {
        return addColumn(headerValue, new ClosureModel(rowModel, readClosure, writeClosure, type));
    }
    
    public DefaultTableColumn addColumn(Object headerValue, ValueModel columnValueModel) {
        return addColumn(headerValue, headerValue, columnValueModel);
    }

    public DefaultTableColumn addColumn(Object headerValue, Object identifier, ValueModel columnValueModel) {
        DefaultTableColumn answer = new DefaultTableColumn(headerValue, identifier, columnValueModel);
        addColumn(answer);
        return answer;
    }
    
    /**
     * Adds a new column definition to the table
     */
    public void addColumn(DefaultTableColumn column) {
        column.setModelIndex(columnModel.getColumnCount());
        columnModel.addColumn(column);
    }
    
    /**
     * Removes a column definition from the table
     */
    public void removeColumn(DefaultTableColumn column) {
        columnModel.removeColumn(column);
    }
    
    @Override
    public int getRowCount() {
        return getRows().size();
    }

    @Override
    public int getColumnCount() {
        return columnModel.getColumnCount();
    }
    
    @Override
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

    @Override
    public Class getColumnClass(int columnIndex) {
        return getColumnModel(columnIndex).getType();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return getColumnModel(columnIndex).isEditable();
    }

    @Override
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

    @Override
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

        @Override
        public void removeColumn(TableColumn column) {
            super.removeColumn(column);
            renumberTableColumns();
        }

        @Override
        public void moveColumn(int columnIndex, int newIndex) {
            super.moveColumn(columnIndex, newIndex);
            renumberTableColumns();
        }

        public void renumberTableColumns() {
            for (int i = tableColumns.size() - 1; i >= 0; i--) {
                ((DefaultTableColumn)tableColumns.get(i)).setModelIndex(i);
            }
        }

    }
    
    public ValueModel getRowModel() {
        return rowModel;
    }

    public ValueModel getRowsModel() {
        return rowsModel;
    }


}