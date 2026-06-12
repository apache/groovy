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

import groovy.lang.Closure;

/**
 * Represents a value model using a closure to extract
 * the value from some source model and an optional write closure
 * for updating the value.
 */
public class ClosureModel implements ValueModel, NestedValueModel {

    private final ValueModel sourceModel;
    private final Closure readClosure;
    private final Closure writeClosure;
    private final Class type;

    /**
     * Creates a read-only closure-backed model.
     *
     * @param sourceModel the model that supplies the source object
     * @param readClosure the closure used to read the derived value
     */
    public ClosureModel(ValueModel sourceModel, Closure readClosure) {
        this(sourceModel, readClosure, null);
    }

    /**
     * Creates a closure-backed model with explicit read and write closures.
     *
     * @param sourceModel the model that supplies the source object
     * @param readClosure the closure used to read the derived value
     * @param writeClosure the closure used to write back a new value, or {@code null} for read-only access
     */
    public ClosureModel(ValueModel sourceModel, Closure readClosure, Closure writeClosure) {
        this(sourceModel, readClosure, writeClosure, Object.class);
    }

    /**
     * Creates a closure-backed model with an explicit declared type.
     *
     * @param sourceModel the model that supplies the source object
     * @param readClosure the closure used to read the derived value
     * @param writeClosure the closure used to write back a new value, or {@code null} for read-only access
     * @param type the declared type of the derived value
     */
    public ClosureModel(ValueModel sourceModel, Closure readClosure, Closure writeClosure, Class type) {
        this.sourceModel = sourceModel;
        this.readClosure = readClosure;
        this.writeClosure = writeClosure;
        this.type = type;
    }

    /**
     * Returns the model that supplies the source object consumed by the closures.
     *
     * @return the nested source model
     */
    @Override
    public ValueModel getSourceModel() {
        return sourceModel;
    }

    /**
     * Evaluates the current source object through the read closure.
     *
     * @return the derived value, or {@code null} if the source model currently holds {@code null}
     */
    @Override
    public Object getValue() {
        Object source = sourceModel.getValue();
        if (source != null) {
            return readClosure.call(source);
        }
        return null;
    }

    /**
     * Applies the write closure to the current source object when the model is editable.
     *
     * @param value the value to write
     */
    @Override
    public void setValue(Object value) {
        if (writeClosure != null) {
            Object source = sourceModel.getValue();
            if (source != null) {
                writeClosure.call(source, value);
            }
        }
    }

    /**
     * Returns the declared value type for this derived model.
     *
     * @return the model type
     */
    @Override
    public Class getType() {
        return type;
    }

    /**
     * Indicates whether this model has a write closure.
     *
     * @return {@code true} when a write closure is available
     */
    @Override
    public boolean isEditable() {
        return writeClosure != null;
    }
}
