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
package org.apache.groovy.groovyfx.factory

import javafx.scene.control.TableColumn
import javafx.scene.control.cell.PropertyValueFactory

/**
 * Creates table columns. The {@code property} attribute names the row
 * property the column displays, installing a {@code PropertyValueFactory}:
 * <pre>
 * tableView(items: people) {
 *     tableColumn('Name', property: 'name')
 *     tableColumn('Age', property: 'age')
 * }
 * </pre>
 */
class TableColumnFactory extends NodeFactory {

    TableColumnFactory() {
        super(TableColumn)
    }

    @Override
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
        Object property = attributes.remove('property')
        if (property != null) {
            ((TableColumn) node).cellValueFactory = new PropertyValueFactory(property.toString())
        }
        super.onHandleNodeAttributes(builder, node, attributes)
    }
}
