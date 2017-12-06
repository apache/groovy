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

import groovy.swing.binding.JComboBoxMetaMethods

import javax.swing.*

public class ComboBoxFactory extends AbstractFactory {
    
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsType(value, name, JComboBox)
        //TODO expand to allow the value arg to be items
        Object items = attributes.get("items")
        JComboBox comboBox
        if (items instanceof Vector) {
            comboBox = new JComboBox(attributes.remove("items"))
        } else if (items instanceof List) {
            List list = (List) attributes.remove("items")
            comboBox = new JComboBox(list.toArray())
        } else if (items instanceof Object[]) {
            comboBox = new JComboBox(attributes.remove("items"))
        } else if (value instanceof JComboBox) {
            comboBox = value
        } else {
            comboBox = new JComboBox()
        }
        JComboBoxMetaMethods.enhanceMetaClass(comboBox)
        return comboBox
    }

}
