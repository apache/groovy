/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.swing.factory;

import groovy.swing.SwingBuilder;
import java.awt.Component;
import java.util.Map;

public class WidgetFactory implements Factory {
    
    public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
        if (SwingBuilder.checkValueIsType(value, name, Component.class)) {
            return value;
        } else if (properties.get(name) instanceof Component) {
            return properties.remove(name);
        } else {
            throw new RuntimeException(name + " must have a value argument of type java.awt.Component or a property named " + name + " of type java.awt.Component");
        }
    }    
}
