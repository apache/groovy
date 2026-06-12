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

import org.codehaus.groovy.runtime.InvokerHelper

/**
 * Factory for creating widgets that accept a text value argument.
 */
class TextArgWidgetFactory extends AbstractFactory {

    /**
     * Widget class instantiated by this factory.
     */
    final Class klass

    /**
     * Creates a new factory for creating widgets that accept a text value argument
     *
     * @param klass the widget class to instantiate
     */
    TextArgWidgetFactory(Class klass) {
        this.klass = klass
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
    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        if (value instanceof GString) value = value as String
        if (FactoryBuilderSupport.checkValueIsTypeNotString(value, name, klass)) {
            return value
        }

        Object widget = klass.getConstructor().newInstance()

        if (value instanceof String) {
            // this does not create property setting order issues, since the value arg precedes all attributes in the builder element
            InvokerHelper.setProperty(widget, "text", value)
        }

        return widget
    }

}
