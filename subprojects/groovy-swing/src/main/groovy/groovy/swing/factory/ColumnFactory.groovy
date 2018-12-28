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

import javax.swing.table.TableCellRenderer
import javax.swing.table.TableColumn
import javax.swing.table.TableColumnModel

@Log
class ColumnFactory extends AbstractFactory {

    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        if (value instanceof TableColumn) {
            return value
        }

        TableColumn node
        Class jxTableClass = null
        try {
            jxTableClass = Class.forName("org.jdesktop.swingx.JXTable")
        } catch (ClassNotFoundException ex) {
        }

        if (jxTableClass != null && builder.current instanceof TableColumnModel) {
            node = Class.forName("org.jdesktop.swingx.table.TableColumnExt").newInstance()
        } else {
            node = new javax.swing.table.TableColumn()
        }

        if (value != null) {
            node.identifier = value.toString()
            attributes.remove('identifier')
        }

        if (attributes.width) {
            if (attributes.width instanceof Collection) {
                // 3 values: min, pref, max
                // 2 values: min, pref
                // 1 value:  pref
                def (min, pref, max) = attributes.width
                if (!pref && !max) {
                    node.minWidth = 0
                    node.preferredWidth = min as Integer
                    node.maxWidth = Integer.MAX_VALUE
                } else {
                    if (min) {
                        node.minWidth = min as Integer
                    }
                    if (pref) {
                        node.preferredWidth = pref as Integer
                    }
                    if (max) {
                        node.maxWidth = max as Integer
                    }
                }
            } else if (attributes.width instanceof Number) {
                node.minWidth = attributes.width.intValue()
                node.preferredWidth = attributes.width.intValue()
                node.maxWidth = attributes.width.intValue()
            }
            attributes.remove('width')
        }
        return node
    }

    void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
        if (!(parent instanceof TableColumnModel)) {
            log.warning("Column must be a child of a columnModel. Found " + parent.getClass())
        }
        parent.addColumn(node)
    }

    void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        if (!(parent instanceof TableColumn)) {
            log.warning("Renderer must be a child of a tableColumn. Found " + parent.getClass())
        }
        if (child instanceof TableCellRenderer) {
            switch (builder.getCurrentName()) {
                case "headerRenderer":
                    child.tableHeader = true
                    parent.headerRenderer = child
                    break;
                case "cellRenderer":
                    child.tableHeader = false
                    parent.cellRenderer = child
                    break;
            }
        }
    }
}
