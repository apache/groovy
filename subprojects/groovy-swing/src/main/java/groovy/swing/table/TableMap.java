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
package groovy.swing.table;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * In a chain of data manipulators some behaviour is common. TableMap
 * provides most of this behaviour and can be subclassed by filters
 * that only need to override a handful of specific methods. TableMap
 * implements TableModel by routing all requests to its model, and
 * TableModelListener by routing all events to its listeners. Inserting
 * a TableMap which has not been subclassed into a chain of table filters
 * should have no effect.
 */
public class TableMap extends AbstractTableModel implements TableModelListener {
    /**
     * The wrapped table model.
     */
    protected TableModel model;

    /**
     * Returns the wrapped table model.
     *
     * @return the delegate model
     */
    public TableModel getModel() {
        return model;
    }

    /**
     * Replaces the wrapped table model and starts listening for its change events.
     *
     * @param model the delegate model
     */
    public void setModel(TableModel model) {
        this.model = model;
        model.addTableModelListener(this);
    }

    // By default, Implement TableModel by forwarding all messages
    // to the model.

    /**
     * Returns the value from the wrapped model at the supplied coordinates.
     *
     * @param aRow the source row index
     * @param aColumn the source column index
     * @return the delegate value
     */
    @Override
    public Object getValueAt(int aRow, int aColumn) {
        return model.getValueAt(aRow, aColumn);
    }

    /**
     * Writes a value through to the wrapped model.
     *
     * @param aValue the new cell value
     * @param aRow the source row index
     * @param aColumn the source column index
     */
    @Override
    public void setValueAt(Object aValue, int aRow, int aColumn) {
        model.setValueAt(aValue, aRow, aColumn);
    }

    /**
     * Returns the current row count from the wrapped model.
     *
     * @return the delegate row count, or {@code 0} when no model is set
     */
    @Override
    public int getRowCount() {
        return (model == null) ? 0 : model.getRowCount();
    }

    /**
     * Returns the current column count from the wrapped model.
     *
     * @return the delegate column count, or {@code 0} when no model is set
     */
    @Override
    public int getColumnCount() {
        return (model == null) ? 0 : model.getColumnCount();
    }

    /**
     * Returns the delegate column name for the supplied index.
     *
     * @param aColumn the source column index
     * @return the delegate column name
     */
    @Override
    public String getColumnName(int aColumn) {
        return model.getColumnName(aColumn);
    }

    /**
     * Returns the delegate column class for the supplied index.
     *
     * @param aColumn the source column index
     * @return the delegate column type
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Class getColumnClass(int aColumn) {
        return model.getColumnClass(aColumn);
    }

    /**
     * Indicates whether the delegate model allows editing for the supplied cell.
     *
     * @param row the source row index
     * @param column the source column index
     * @return {@code true} when the delegate cell is editable
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        return model.isCellEditable(row, column);
    }
//
// Implementation of the TableModelListener interface,
//

    // By default forward all events to all the listeners.
    /**
     * Forwards model change notifications to this table model's listeners.
     *
     * @param e the model change event
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        fireTableChanged(e);
    }
}
