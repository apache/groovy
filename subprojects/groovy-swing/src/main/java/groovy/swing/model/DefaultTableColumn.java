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

import javax.swing.table.TableColumn;

/**
 * A {@link TableColumn} backed by a {@link ValueModel} for reading and writing cell values.
 */
public class DefaultTableColumn extends TableColumn {

    private ValueModel valueModel;

    /**
     * Creates a column backed by the supplied value model.
     *
     * @param valueModel the value model used to access cell values
     */
    public DefaultTableColumn(ValueModel valueModel) {
        this.valueModel = valueModel;
    }

    /**
     * Creates a column with a header value and backing value model.
     *
     * @param header the column header value
     * @param valueModel the value model used to access cell values
     */
    public DefaultTableColumn(Object header, ValueModel valueModel) {
        this(valueModel);
        setHeaderValue(header);
    }

    /**
     * Creates a column with header and identifier values.
     *
     * @param headerValue the column header value
     * @param identifier the logical identifier for the column
     * @param columnValueModel the value model used to access cell values
     */
    public DefaultTableColumn(Object headerValue, Object identifier, ValueModel columnValueModel) {
        this(headerValue, columnValueModel);
        setIdentifier(identifier);
    }

    /**
     * Returns a debug-friendly description of this column and its backing value model.
     *
     * @return the column description
     */
    @Override
    public String toString() {
        return super.toString() + "[header:" + getHeaderValue() + " valueModel:" + valueModel + "]";
    }

    /**
     * Evaluates the value of a cell
     *
     * @return the value
     * @param row the row of interest
     * @param rowIndex the index of the row of interest
     * @param columnIndex the column of interest
     */
    public Object getValue(Object row, int rowIndex, int columnIndex) {
        if (valueModel instanceof NestedValueModel nestedModel) {
            nestedModel.getSourceModel().setValue(row);
        }
        return valueModel.getValue();
    }

    /**
     * Writes a cell value through the backing value model.
     *
     * @param row the row object that owns the value
     * @param value the new cell value
     * @param rowIndex the source row index
     * @param columnIndex the source column index
     */
    public void setValue(Object row, Object value, int rowIndex, int columnIndex) {
        if (valueModel instanceof NestedValueModel nestedModel) {
            nestedModel.getSourceModel().setValue(row);
        }
        valueModel.setValue(value);
    }

    /**
     * @return the column type
     */
    public Class getType() {
        return valueModel.getType();
    }

    /**
     * Returns the value model used by this column.
     *
     * @return the backing value model
     */
    public ValueModel getValueModel() {
        return valueModel;
    }

}
