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

/**
 * Factory for creating button groups and wiring buttons into them.
 */
class ButtonGroupFactory extends BeanFactory {

    /**
     * Builder context key for the child button group attribute name.
     */
    public static final String DELEGATE_PROPERTY_BUTTON_GROUP = "_delegateProperty:buttonGroup";
    /**
     * Default child attribute name for the button group.
     */
    public static final String DEFAULT_DELEGATE_PROPERTY_BUTTON_GROUP = "buttonGroup";

    /**
     * Creates a new factory for creating button groups and wiring buttons into them
     */
    public ButtonGroupFactory() {
        super(ButtonGroup, true)
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
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        builder.context[DELEGATE_PROPERTY_BUTTON_GROUP] = attributes.remove("buttonGroupProperty") ?: DEFAULT_DELEGATE_PROPERTY_BUTTON_GROUP
        return super.newInstance(builder, name, value, attributes);
    }

    /**
     * Applies a button group supplied through child attributes.
     *
     * @param builder the factory builder
     * @param node the current node
     * @param attributes the node attributes
     */
    public static buttonGroupAttributeDelegate(def builder, def node, def attributes) {
        def buttonGroupAttr = builder?.context?.getAt(DELEGATE_PROPERTY_BUTTON_GROUP) ?: DEFAULT_DELEGATE_PROPERTY_BUTTON_GROUP
        if (attributes.containsKey(buttonGroupAttr)) {
            def o = attributes.get(buttonGroupAttr)
            if ((o instanceof ButtonGroup) && (node instanceof AbstractButton)) {
                node.model.group = o
                attributes.remove(buttonGroupAttr)
            }
         }
    }

}
