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

    Class restrictedType;

    public WidgetFactory(Class restrictedType) {
        this.restrictedType = restrictedType;
    }
    
    public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
        if (value == null) {
            value = properties.remove(name);
        }
        if ((value != null) && SwingBuilder.checkValueIsType(value, name, restrictedType)) {
            return value;
        } else {
            throw new RuntimeException(name + " must have either a value argument or an attribute named " + name + " that must be of type " + restrictedType.getName());
        }
    }    
}
