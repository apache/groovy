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

import javax.swing.*
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.util.logging.Level
import java.util.logging.Logger

class RichActionWidgetFactory extends AbstractFactory {
    static final Class[] ACTION_ARGS = [Action]
    static final Class[] ICON_ARGS = [Icon]
    static final Class[] STRING_ARGS = [String]

    final Constructor actionCtor
    final Constructor iconCtor
    final Constructor stringCtor
    final Class klass

    RichActionWidgetFactory(Class klass) {
        try {
            actionCtor = klass.getConstructor(ACTION_ARGS)
            iconCtor = klass.getConstructor(ICON_ARGS)
            stringCtor = klass.getConstructor(STRING_ARGS)
            this.klass = klass
        } catch (NoSuchMethodException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex)
        } catch (SecurityException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex)
        }
    }

    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        try {
            if (value instanceof GString) value = value as String
            if (value == null) {
                return klass.newInstance()
            } else if (value instanceof Action) {
                return actionCtor.newInstance(value)
            } else if (value instanceof Icon) {
                return iconCtor.newInstance(value)
            } else if (value instanceof String) {
                return stringCtor.newInstance(value)
            } else if (klass.isAssignableFrom(value.getClass())) {
                return value
            } else {
                throw new RuntimeException("$name can only have a value argument of type javax.swing.Action, javax.swing.Icon, java.lang.String, or $klass.name")
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to create component for '$name' reason: $e", e)
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Failed to create component for '$name' reason: $e", e)
        }
    }

}
