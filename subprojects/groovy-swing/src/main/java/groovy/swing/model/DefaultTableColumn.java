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
package groovy.model;

import javax.swing.table.TableColumn;

/** 
 * Represents a column using a ValueModel to extract the value.
 */
public class DefaultTableColumn extends TableColumn {

    private ValueModel valueModel;    
    
    public DefaultTableColumn(ValueModel valueModel) {
        this.valueModel = valueModel;
    }

    public DefaultTableColumn(Object header, ValueModel valueModel) {
        this(valueModel);
        setHeaderValue(header);
    }

    public DefaultTableColumn(Object headerValue, Object identifier, ValueModel columnValueModel) {
        this(headerValue, columnValueModel);
        setIdentifier(identifier);
    }

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
        if (valueModel instanceof NestedValueModel) {
            NestedValueModel nestedModel = (NestedValueModel) valueModel;
            nestedModel.getSourceModel().setValue(row);
        }
        return valueModel.getValue();
    }

    public void setValue(Object row, Object value, int rowIndex, int columnIndex) {
        if (valueModel instanceof NestedValueModel) {
            NestedValueModel nestedModel = (NestedValueModel) valueModel;
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

    public ValueModel getValueModel() {
        return valueModel;
    }

}