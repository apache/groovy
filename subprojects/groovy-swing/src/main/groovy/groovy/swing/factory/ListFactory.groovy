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

import groovy.swing.binding.JListMetaMethods
import groovy.swing.impl.ListWrapperListModel

import javax.swing.*

/**
 * Create a JList, and handle the optional items attribute.
 */
class ListFactory extends AbstractFactory {

    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        // FactoryBuilderSupport.checkValueIsType(value, name, JList)

        JList list
        Object items = attributes.remove("items")

        if (value instanceof JList) {
            list = value
        } else if (value instanceof Vector || value instanceof Object[]) {
            list = new JList(value)
        } else if (value instanceof List) {
            list = new JList(new ListWrapperListModel(items))
        } else {
            list = new JList()
        }

        if (items instanceof Vector) {
            list.setListData((Vector) items)
        } else if (items instanceof Object[]) {
            list.setListData((Object[]) items)
        } else if (items instanceof List) {
            list.model = new ListWrapperListModel(items)
        }

        JListMetaMethods.enhanceMetaClass(list)
        return list
    }

    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
        if (attributes.containsKey("listData")) {
            def listData = attributes.remove("listData")
            if (listData instanceof Vector || listData instanceof Object[]) {
                node.listData = listData
            } else if (listData instanceof List) {
                node.model = new ListWrapperListModel(listData)
            } else if (listData instanceof Collection) {
                node.listData = listData.toArray()
            } else {
                // allow any iterable ??
                node.listData = listData.collect([]) { it } as Object[]
            }
        }
        return true
    }

}

