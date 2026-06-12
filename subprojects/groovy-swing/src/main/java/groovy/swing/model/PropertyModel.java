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
 * Exposes a named property of another {@link ValueModel} as its own value model.
 */
public class PropertyModel implements ValueModel, NestedValueModel {

    private ValueModel sourceModel;
    private String property;
    private Class type;
    /**
     * Indicates whether writes should be forwarded to the source property.
     */
    boolean editable;

    /**
     * Creates a writable property model with {@link Object} as its declared type.
     *
     * @param sourceModel the model that supplies the owning object
     * @param property the property name to access
     */
    public PropertyModel(ValueModel sourceModel, String property) {
        this(sourceModel, property, Object.class, true);
    }

    /**
     * Creates a writable property model with an explicit declared type.
     *
     * @param sourceModel the model that supplies the owning object
     * @param property the property name to access
     * @param type the declared property type
     */
    public PropertyModel(ValueModel sourceModel, String property, Class type) {
        this(sourceModel, property, type, true);
    }

    /**
     * Creates a property model with explicit type and editability.
     *
     * @param sourceModel the model that supplies the owning object
     * @param property the property name to access
     * @param type the declared property type
     * @param editable whether writes should be forwarded to the source property
     */
    public PropertyModel(ValueModel sourceModel, String property, Class type, boolean editable) {
        this.sourceModel = sourceModel;
        this.property = property;
        this.type = type;
        //TODO After 1.1 we should introspect the meta property and set editable to false if the property is read only
        this.editable = editable;
    }

    /**
     * Returns the property name represented by this model.
     *
     * @return the property name
     */
    public String getProperty() {
        return property;
    }

    /**
     * Returns the model that supplies the owning object for the bound property.
     *
     * @return the nested source model
     */
    @Override
    public ValueModel getSourceModel() {
        return sourceModel;
    }

    /**
     * Reads the current property value from the source object.
     *
     * @return the current property value, or {@code null} when the source object is {@code null}
     */
    @Override
    public Object getValue() {
        Object source = sourceModel.getValue();
        if (source != null) {
            return InvokerHelper.getProperty(source, property);
        }
        return null;
    }

    /**
     * Writes a new value to the property on the current source object.
     *
     * @param value the new property value
     */
    @Override
    public void setValue(Object value) {
        Object source = sourceModel.getValue();
        if (source != null) {
            InvokerHelper.setProperty(source, property, value);
        }
    }

    /**
     * Returns the declared property type.
     *
     * @return the property type
     */
    @Override
    public Class getType() {
        return type;
    }

    /**
     * Indicates whether writes should be forwarded to the source property.
     *
     * @return {@code true} when the property model is writable
     */
    @Override
    public boolean isEditable() {
        return editable;
    }

}
