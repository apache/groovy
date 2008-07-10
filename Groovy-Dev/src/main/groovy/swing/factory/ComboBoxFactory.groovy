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

package groovy.swing.factory

import javax.swing.JComboBox

public class ComboBoxFactory extends AbstractFactory {
    
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsType(value, name, JComboBox)
        //TODO expand to allow the value arg to be items
        Object items = attributes.get("items")
        if (items instanceof Vector) {
            return new JComboBox(attributes.remove("items"))
        } else if (items instanceof List) {
            List list = (List) attributes.remove("items")
            return new JComboBox(list.toArray())
        } else if (items instanceof Object[]) {
            return new JComboBox(attributes.remove("items"))
        } else if (value instanceof JComboBox) {
            return value
        } else {
            return new JComboBox()
        }
    }

}
