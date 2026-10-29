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

import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableValue
import org.codehaus.groovy.runtime.InvokerHelper

import java.util.concurrent.Callable

/**
 * Produces an observable value for use as an attribute, which the receiving
 * node then binds to:
 * <pre>
 * label(text: bind(model.nameProperty()))
 * label(text: bind(model.ageProperty(), converter: { "Age: $it" }))
 * label(text: bind(source: model, sourceProperty: 'name'))
 * </pre>
 * Passing an observable value directly as the attribute has the same effect;
 * {@code bind} exists for readability and for the {@code converter} and
 * {@code source} forms.
 */
class BindFactory extends AbstractFactory {

    @Override
    boolean isLeaf() {
        true
    }

    @Override
    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        ObservableValue source
        if (value instanceof ObservableValue) {
            source = value
        } else if (attributes.containsKey('source')) {
            Object bean = attributes.remove('source')
            Object property = attributes.remove('sourceProperty')
            if (property == null) {
                throw new IllegalArgumentException("bind(source: ...) also needs sourceProperty: 'name'")
            }
            Object observable = InvokerHelper.invokeMethod(bean, property.toString() + 'Property', null)
            if (!(observable instanceof ObservableValue)) {
                throw new IllegalArgumentException("${property}Property() of ${bean.getClass().simpleName} is not an ObservableValue")
            }
            source = observable
        } else {
            throw new IllegalArgumentException("bind() needs an observable value, for example bind(model.nameProperty())")
        }
        Object converter = attributes.remove('converter')
        if (converter instanceof Closure) {
            Closure convert = converter
            return Bindings.createObjectBinding({
                Object converted = convert.call(source.value)
                converted instanceof GString ? converted.toString() : converted
            } as Callable, source)
        }
        source
    }

    @Override
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
        if (attributes) {
            throw new IllegalArgumentException("bind() accepts only source, sourceProperty and converter attributes. Found: ${attributes.keySet()}")
        }
        false
    }
}
