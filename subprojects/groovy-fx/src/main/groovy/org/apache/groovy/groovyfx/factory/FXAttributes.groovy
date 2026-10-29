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
import javafx.beans.property.Property
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableStringValue
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import org.codehaus.groovy.runtime.InvokerHelper

/**
 * Applies builder attributes to a JavaFX bean.
 * <p>
 * An {@code ObservableValue} given for a property binds the property to it
 * (converted to text with {@code Bindings.convert} when the property is a
 * {@code StringProperty} and the value is not already a string); a list or
 * array given for an {@code ObservableList} property replaces the
 * list's contents; a string given for a {@code Paint} property is parsed with
 * {@code Paint.valueOf}; a number or list given for an {@code Insets}
 * property, or a number or {@code [family, size]} list given for a
 * {@code Font} property, is converted. Everything else is set as a property,
 * which lets Groovy coerce closures to listener interfaces and strings to
 * enum constants.
 */
class FXAttributes {

    private FXAttributes() {
    }

    static void apply(Object node, Map attributes) {
        if (!attributes) return
        MetaClass metaClass = InvokerHelper.getMetaClass(node)
        attributes.each { Object key, Object value ->
            String name = key.toString()
            MetaProperty property = metaClass.hasProperty(node, name)
            Class type = property?.type
            if (value instanceof ObservableValue && !(type != null && ObservableValue.isAssignableFrom(type))) {
                bind(node, name, value)
            } else if (type != null && ObservableList.isAssignableFrom(type) && isSequence(value)) {
                ObservableList target = property.getProperty(node) as ObservableList
                target.setAll(value as List)
            } else {
                InvokerHelper.setProperty(node, name, coerce(type, value))
            }
        }
    }

    private static boolean isSequence(Object value) {
        value instanceof Iterable || (value != null && value.getClass().isArray())
    }

    private static void bind(Object node, String name, ObservableValue value) {
        String accessor = name + 'Property'
        MetaClass metaClass = InvokerHelper.getMetaClass(node)
        if (!metaClass.respondsTo(node, accessor, new Object[0])) {
            throw new IllegalArgumentException("Cannot bind attribute '${name}' of ${node.getClass().simpleName}: it has no ${accessor}() method")
        }
        Object property = InvokerHelper.invokeMethod(node, accessor, null)
        if (!(property instanceof Property)) {
            throw new IllegalArgumentException("Cannot bind attribute '${name}' of ${node.getClass().simpleName}: ${accessor}() is read-only")
        }
        if (property instanceof StringProperty && !(value instanceof ObservableStringValue)) {
            // a number, a GString-producing converter, ... shown as text
            value = Bindings.convert(value)
        }
        ((Property) property).bind(value)
    }

    static Object coerce(Class type, Object value) {
        if (type == null || value == null) return value
        if (Paint.isAssignableFrom(type) && value instanceof CharSequence) return Paint.valueOf(value.toString())
        if (type == Insets) return toInsets(value)
        if (type == Font) return toFont(value)
        value
    }

    /**
     * Converts a number ({@code 5}) or a CSS-style list ({@code [5]}, {@code [vertical, horizontal]},
     * {@code [top, right, bottom, left]}) to {@code Insets}.
     */
    static Insets toInsets(Object value) {
        if (value instanceof Insets) return value
        if (value instanceof Number) return new Insets(value.doubleValue())
        if (value instanceof List) {
            List<Number> sides = value as List<Number>
            switch (sides.size()) {
                case 1: return new Insets(sides[0].doubleValue())
                case 2: return new Insets(sides[0].doubleValue(), sides[1].doubleValue(), sides[0].doubleValue(), sides[1].doubleValue())
                case 4: return new Insets(sides[0].doubleValue(), sides[1].doubleValue(), sides[2].doubleValue(), sides[3].doubleValue())
            }
        }
        throw new IllegalArgumentException("Cannot convert ${value} to Insets: expected a number or a list of 1, 2 or 4 numbers")
    }

    /**
     * Converts a size ({@code 14}), a family name ({@code 'Serif'}) or a {@code [family, size]} list to a {@code Font}.
     */
    static Font toFont(Object value) {
        if (value instanceof Font) return value
        if (value instanceof Number) return Font.font(value.doubleValue())
        if (value instanceof CharSequence) return Font.font(value.toString())
        if (value instanceof List && value.size() == 2) return Font.font(value[0].toString(), (value[1] as Number).doubleValue())
        throw new IllegalArgumentException("Cannot convert ${value} to Font: expected a size, a family name or [family, size]")
    }
}
