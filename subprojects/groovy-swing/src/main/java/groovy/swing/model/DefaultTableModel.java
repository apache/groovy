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
 * A table model built from row and column {@link ValueModel} instances.
 */
public class DefaultTableModel extends AbstractTableModel {

    private ValueModel rowModel;
    private ValueModel rowsModel;
    private MyTableColumnModel columnModel = new MyTableColumnModel();

    /**
     * Creates a table model with its own row holder.
     *
     * @param rowsModel the model that supplies the row collection
     */
    public DefaultTableModel(ValueModel rowsModel) {
        this(rowsModel, new ValueHolder());
    }

    /**
     * Creates a table model with explicit row and rows models.
     *
     * @param rowsModel the model that supplies the row collection
     * @param rowModel the model reused for exposing the current row
     */
    public DefaultTableModel(ValueModel rowsModel, ValueModel rowModel) {
        this.rowModel = rowModel;
        this.rowsModel = rowsModel;
    }

    /**
     * @return the column definitions.
     */
    @SuppressWarnings("rawtypes")
    public List getColumnList() {
        return columnModel.getColumnList();
    }

    /**
     * Returns the Swing column model maintained by this table model.
     *
     * @return the column model
     */
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

    /**
     * Adds a column whose header value is also used as its identifier.
     *
     * @param headerValue the header value and identifier
     * @param columnValueModel the value model used by the column
     * @return the created column
     */
    public DefaultTableColumn addColumn(Object headerValue, ValueModel columnValueModel) {
        return addColumn(headerValue, headerValue, columnValueModel);
    }

    /**
     * Adds a column definition with an explicit identifier.
     *
     * @param headerValue the header value
     * @param identifier the column identifier
     * @param columnValueModel the value model used by the column
     * @return the created column
     */
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

    /**
     * Returns the number of rows currently exposed by the rows model.
     *
     * @return the current row count
     */
    @Override
    public int getRowCount() {
        return getRows().size();
    }

    /**
     * Returns the number of configured columns.
     *
     * @return the current column count
     */
    @Override
    public int getColumnCount() {
        return columnModel.getColumnCount();
    }

    /**
     * Returns the header text for the supplied column.
     *
     * @param columnIndex the column index
     * @return the column header text, or {@code null} when unavailable
     */
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

    /**
     * Returns the declared value type for the supplied column.
     *
     * @param columnIndex the column index
     * @return the column value type
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Class getColumnClass(int columnIndex) {
        return getColumnModel(columnIndex).getType();
    }

    /**
     * Indicates whether the supplied cell can be edited through its column model.
     *
     * @param rowIndex the row index
     * @param columnIndex the column index
     * @return {@code true} when the column model is editable
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return getColumnModel(columnIndex).isEditable();
    }

    /**
     * Reads the value for the supplied row and column.
     *
     * @param rowIndex the row index
     * @param columnIndex the column index
     * @return the cell value, or {@code null} when the coordinates are invalid
     */
    @SuppressWarnings("rawtypes")
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

    /**
     * Writes a value to the supplied row and column when both coordinates are valid.
     *
     * @param value the new cell value
     * @param rowIndex the row index
     * @param columnIndex the column index
     */
    @SuppressWarnings("rawtypes")
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

    /**
     * Returns the value model used by the supplied column.
     *
     * @param columnIndex the column index
     * @return the column value model
     */
    protected ValueModel getColumnModel(int columnIndex) {
        DefaultTableColumn column = (DefaultTableColumn) columnModel.getColumn(columnIndex);
        return column.getValueModel();
    }

    @SuppressWarnings("rawtypes")
    /**
     * Returns the current rows as a list.
     *
     * @return the current row collection as a list, never {@code null}
     */
    protected List getRows() {
        Object value = rowsModel.getValue();
        if (value == null) {
            return Collections.EMPTY_LIST;
        }
        return InvokerHelper.asList(value);
    }

    /**
     * Column model implementation that keeps model indexes aligned with the current column order.
     */
    protected static class MyTableColumnModel extends DefaultTableColumnModel {
        /**
         * Returns the live list of table columns.
         *
         * @return the backing column list
         */
        @SuppressWarnings("rawtypes")
        public List getColumnList() {
            return tableColumns;
        }

        /**
         * Removes a column and then renumbers the remaining model indexes.
         *
         * @param column the column to remove
         */
        @Override
        public void removeColumn(TableColumn column) {
            super.removeColumn(column);
            renumberTableColumns();
        }

        /**
         * Moves a column and then renumbers the model indexes to match the new order.
         *
         * @param columnIndex the source column index
         * @param newIndex the destination column index
         */
        @Override
        public void moveColumn(int columnIndex, int newIndex) {
            super.moveColumn(columnIndex, newIndex);
            renumberTableColumns();
        }

        /**
         * Reassigns model indexes to match the current column order.
         */
        public void renumberTableColumns() {
            for (int i = tableColumns.size() - 1; i >= 0; i--) {
                tableColumns.get(i).setModelIndex(i);
            }
        }

    }

    /**
     * Returns the value model representing the current row object.
     *
     * @return the current row model
     */
    public ValueModel getRowModel() {
        return rowModel;
    }

    /**
     * Returns the model that supplies the backing row collection.
     *
     * @return the rows model
     */
    public ValueModel getRowsModel() {
        return rowsModel;
    }


}
