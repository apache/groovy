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

import groovy.swing.model.DefaultTableModel
import groovy.swing.model.ValueHolder
import groovy.swing.model.ValueModel

import javax.swing.JTable
import javax.swing.table.TableModel
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class TableModelFactory extends AbstractFactory {
    
    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        if (FactoryBuilderSupport.checkValueIsType(value, name, TableModel)) {
            return value
        } else if (attributes.get(name) instanceof TableModel) {
            return attributes.remove(name)
        } else {
            ValueModel model = (ValueModel) attributes.remove("model")
            if (model == null) {
                Object list = attributes.remove("list")
                if (list == null) {
                    list = new ArrayList()
                }
                model = new ValueHolder(list)
            }
            return new DefaultTableModel(model)
        }
    }

    void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
        if ((node.columnCount > 0) && (parent instanceof JTable)) {
            parent.autoCreateColumnsFromModel = false
            PropertyChangeListener listener = {e ->
                    if ((e.propertyName == 'model') && e.newValue instanceof DefaultTableModel) {
                        e.source.columnModel = e.newValue.columnModel
                        e.source.revalidate()
                        e.source.repaint()
                    }
                } as PropertyChangeListener

            parent.addPropertyChangeListener('model', listener)
            builder.addDisposalClosure( {parent.removePropertyChangeListener('model', listener)})

            // the table has already set the model, so fire the listener manually
            listener.propertyChange(new PropertyChangeEvent(parent, 'model', null, node)) 
        }
    }
}

class PropertyColumnFactory extends AbstractFactory {

    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsNull(value, name)
        Object current = builder.getCurrent()
        if (current instanceof DefaultTableModel) {
            DefaultTableModel model = (DefaultTableModel) current
            String property = (String) attributes.remove("propertyName")
            if (property == null) {
                throw new IllegalArgumentException("Must specify a property for a propertyColumn")
            }
            Object header = attributes.remove("header")
            if (header == null) {
                header = ""
            }
            Class type = (Class) attributes.remove("type")
            if (type == null) {
                type = Object
            }
            Boolean editable = (Boolean) attributes.remove("editable")
            if (editable == null) {
                editable = Boolean.TRUE
            }
            return model.addPropertyColumn(header, property, type, editable.booleanValue())
        } else {
            throw new RuntimeException("propertyColumn must be a child of a tableModel")
        }
    }
}

class ClosureColumnFactory extends AbstractFactory {

    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsNull(value, name)
        Object current = builder.getCurrent()
        if (current instanceof DefaultTableModel) {
            DefaultTableModel model = (DefaultTableModel) current
            Object header = attributes.remove("header")
            if (header == null) {
                header = ""
            }
            Closure readClosure = (Closure) attributes.remove("read")
            if (readClosure == null) {
                throw new IllegalArgumentException("Must specify 'read' Closure property for a closureColumn")
            }
            Closure writeClosure = (Closure) attributes.remove("write")
            Class type = (Class) attributes.remove("type")
            if (type == null) {
                type = Object
            }
            return model.addClosureColumn(header, readClosure, writeClosure, type)
        } else {
            throw new RuntimeException("closureColumn must be a child of a tableModel")
        }
    }
}
