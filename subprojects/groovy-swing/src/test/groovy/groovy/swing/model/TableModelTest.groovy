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
package groovy.swing.model

import groovy.test.GroovyTestCase

class TableModelTest extends GroovyTestCase {
    
    void testTableModel() {
        def list = [ ['name':'James', 'location':'London'], ['name':'Bob', 'location':'Atlanta']]
        
        def listModel = new ValueHolder(list)
        
        def model = new DefaultTableModel(listModel)
        model.addColumn(new DefaultTableColumn("Name", new PropertyModel(model.rowModel, "name")))
        model.addColumn(new DefaultTableColumn("Location", new PropertyModel(model.rowModel, "location")))
        
        assert model.rowCount == 2
        assert model.columnCount == 2
        assertValueAt(model, 0, 0, 'James')
        assertValueAt(model, 0, 1, 'London')
        assertValueAt(model, 1, 0, 'Bob')
        assertValueAt(model, 1, 1, 'Atlanta')
        
        assert model.getColumnName(0) == 'Name'
        assert model.getColumnName(1) == 'Location'
        
        // let's set some values
        model.setValueAt('Antigua', 0, 1)
        assertValueAt(model, 0, 1, 'Antigua')
        
        // let's check the real model changed too
        def james = list.get(0)
        assert james.location == 'Antigua'
    }
    
    protected void assertValueAt(model, row, col, expected) {
        def value = model.getValueAt(row, col)
        assert value == expected , "for row " + row + " col " + col
    }
}
