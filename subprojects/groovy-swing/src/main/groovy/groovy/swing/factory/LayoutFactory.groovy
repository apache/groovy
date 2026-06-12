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
import java.awt.*

/**
 * Base factory for creating layout managers and exposing layout constants.
 */
class LayoutFactory extends groovy.swing.factory.BeanFactory {

    /**
     * Cached layout constants copied into the builder context.
     */
    def contextProps
    /**
     * Builder context key for the child constraints attribute name.
     */
    public static final String DELEGATE_PROPERTY_CONSTRAINT = "_delegateProperty:Constrinat";
    /**
     * Default child attribute name for layout constraints.
     */
    public static final String DEFAULT_DELEGATE_PROPERTY_CONSTRAINT = "constraints";


    /**
     * Creates a new base factory for creating layout managers and exposing layout constants
     *
     * @param klass the widget class to instantiate
     */
    public LayoutFactory(Class klass) {
        super(klass)
    }

    /**
     * Creates a new base factory for creating layout managers and exposing layout constants
     *
     * @param klass the widget class to instantiate
     * @param leaf whether created nodes are leaf nodes
     */
    public LayoutFactory(Class klass, boolean leaf) {
        super(klass, leaf)
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
        builder.context[DELEGATE_PROPERTY_CONSTRAINT] = attributes.remove("constraintsProperty") ?: DEFAULT_DELEGATE_PROPERTY_CONSTRAINT
        Object o = super.newInstance(builder, name, value, attributes);
        addLayoutProperties(builder.getContext());
        return o;
    }

    /**
     * Copies layout constants into the builder context.
     *
     * @param context the builder context
     * @param layoutClass the layout class whose constants should be copied
     */
    public void addLayoutProperties(context, Class layoutClass) {
        if (contextProps == null) {
            contextProps = [:]
            layoutClass.fields.each {
                def name = it.name
                if (name.toUpperCase(Locale.ROOT) == name) {
                    contextProps.put(name, layoutClass."$name")
                }
            }
        }

        context.putAll(contextProps)
    }

    /**
     * Copies layout constants into the builder context.
     *
     * @param context the builder context
     */
    public void addLayoutProperties(context) {
        addLayoutProperties(context, beanClass)
    }

    /**
     * Attaches the current node to its parent when parent-specific handling is required.
     *
     * @param builder the factory builder
     * @param parent the parent node
     * @param child the child node
     */
    public void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
        if (parent instanceof Container) {
            getLayoutTarget(parent).layout = child
        }
    }

    /**
     * Returns the container whose layout should be updated.
     *
     * @param parent the parent node
     * @return the container whose layout should be updated
     */
    public static Container getLayoutTarget(Container parent) {
        if (parent instanceof RootPaneContainer) {
            RootPaneContainer rpc = (RootPaneContainer) parent;
            parent = rpc.getContentPane();
        }
        return parent;
    }

    /**
     * Captures a constraints attribute for later child insertion.
     *
     * @param builder the factory builder
     * @param node the current node
     * @param attributes the node attributes
     */
    public static constraintsAttributeDelegate(def builder, def node, def attributes) {
        def constraintsAttr = builder?.context?.getAt(DELEGATE_PROPERTY_CONSTRAINT) ?: DEFAULT_DELEGATE_PROPERTY_CONSTRAINT
        if (attributes.containsKey(constraintsAttr)) {
            builder.context.constraints = attributes.remove(constraintsAttr)
        }
    }

}
