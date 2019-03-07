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

import org.codehaus.groovy.binding.BindingProxy

/**
 * @since Groovy 1.1
 */
class BindProxyFactory extends AbstractFactory {

    boolean isLeaf() {
        return true
    }

    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        if (value == null) {
            throw new RuntimeException("$name requires a value argument.");
        }
        BindingProxy mb = new BindingProxy(value);

        Object o = attributes.remove("bind");
        builder.context.bind = (o instanceof Boolean) && ((Boolean) o).booleanValue()
        return mb;
    }

    void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
        if (builder.context.bind) {
            node.bind()
        }
    }

}
