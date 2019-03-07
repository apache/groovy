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

import groovy.util.logging.Log

import javax.swing.*
import javax.swing.table.TableColumnModel

@Log
class ColumnModelFactory extends AbstractFactory {

    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        if (value instanceof TableColumnModel) {
            return value
        }

        Class jxTableClass = null
        try {
            jxTableClass = Class.forName("org.jdesktop.swingx.JXTable")
        } catch (ClassNotFoundException ex) {
        }

        if (jxTableClass != null && jxTableClass.isAssignableFrom(builder.current.getClass())) {
            return Class.forName("org.jdesktop.swingx.table.DefaultTableColumnModelExt").newInstance()
        } else {
            return new javax.swing.table.DefaultTableColumnModel()
        }
    }

    void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
        if (parent instanceof JTable) {
            parent.columnModel = node
        } else {
            log.warning("ColumnModel must be a child of a table. Found: " + parent.getClass());
        }
    }
}
