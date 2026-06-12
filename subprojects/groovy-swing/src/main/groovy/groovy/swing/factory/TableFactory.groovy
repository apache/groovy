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
package groovy.swing.factory

import groovy.swing.binding.JTableMetaMethods

import javax.swing.*
import javax.swing.table.TableColumn
import javax.swing.table.TableModel

/**
 * Factory for creating tables and attaching child models or columns.
 */
class TableFactory extends BeanFactory {

    /**
     * Creates a new factory for creating tables and attaching child models or columns
     */
    public TableFactory() {
        this(JTable)
    }

    /**
     * Creates a new factory for creating tables and attaching child models or columns
     *
     * @param klass the widget class to instantiate
     */
    public TableFactory(Class klass) {
        super(klass, false)
    }

    /**
     * Creates the node handled by this factory.
     *
     * @param builder the factory builder
     * @param name the node name
     * @param value the node value
     * @param attributes the node attributes
     * @return the created or reused node
     */
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        Object table = super.newInstance(builder, name, value, attributes);
        // insure metaproperties are registered
        JTableMetaMethods.enhanceMetaClass(table)
        return table
    }

    /**
     * Attaches a child node to its parent.
     *
     * @param builder the factory builder
     * @param parent the parent node
     * @param child the child node
     */
    public void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        if (child instanceof TableColumn) {
            parent.addColumn(child);
        } else if (child instanceof TableModel) {
            parent.model = child;
        }
    }

}

