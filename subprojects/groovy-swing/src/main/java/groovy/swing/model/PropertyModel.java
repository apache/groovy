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

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Represents a property of a value as a model.
 */
public class PropertyModel implements ValueModel, NestedValueModel {

    private ValueModel sourceModel;
    private String property;
    private Class type;
    boolean editable;

    public PropertyModel(ValueModel sourceModel, String property) {
        this(sourceModel, property, Object.class, true);
    }

    public PropertyModel(ValueModel sourceModel, String property, Class type) {
        this(sourceModel, property, type, true);
    }

    public PropertyModel(ValueModel sourceModel, String property, Class type, boolean editable) {
        this.sourceModel = sourceModel;
        this.property = property;
        this.type = type;
        //TODO After 1.1 we should introspect the meta property and set editable to false if the property is read only
        this.editable = editable;
    }

    public String getProperty() {
        return property;
    }

    public ValueModel getSourceModel() {
        return sourceModel;
    }

    public Object getValue() {
        Object source = sourceModel.getValue();
        if (source != null) {
            return InvokerHelper.getProperty(source, property);
        }
        return null;
    }

    public void setValue(Object value) {
        Object source = sourceModel.getValue();
        if (source != null) {
            InvokerHelper.setProperty(source, property, value);
        }
    }
    
    public Class getType() {
        return type;
    }

    public boolean isEditable() {
        return editable;
    }

}
