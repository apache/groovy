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

import static groovy.swing.factory.LayoutFactory.DEFAULT_DELEGATE_PROPERTY_CONSTRAINT

/**
 * Factory that accepts pre-existing widget instances of a restricted type.
 */
public class WidgetFactory extends AbstractFactory {

    /**
     * Accepted runtime type for node values.
     */
    final Class restrictedType;
    /**
     * Whether created nodes are treated as leaves.
     */
    protected final boolean leaf

    /**
     * Creates a new factory that accepts pre-existing widget instances of a restricted type.
     *
     * @param restrictedType the accepted widget type
     * @param leaf whether created nodes are leaf nodes
     */
    public WidgetFactory(Class restrictedType, boolean leaf) {
        this.restrictedType = restrictedType
        this.leaf = leaf
    }

    /**
     * Indicates whether nodes created by this factory accept children.
     *
     * @return true if child nodes are not expected
     */
    boolean isLeaf() {
        return leaf
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
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        if (value == null) {
            value = attributes.remove(name);
        }
        if ((value != null) && FactoryBuilderSupport.checkValueIsType(value, name, restrictedType)) {
            return value;
        } else {
            throw new RuntimeException("$name must have either a value argument or an attribute named $name that must be of type $restrictedType.name");
        }
    }

    /**
     * Attaches a child node to its parent.
     *
     * @param builder the factory builder
     * @param parent the parent node
     * @param child the child node
     */
    public void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        if (!(child instanceof Component) || (child instanceof Window)) {
            return;
        }
        try {
            def constraints = builder.context.constraints
            if (constraints != null) {
                LayoutFactory.getLayoutTarget(parent).add(child, constraints)
                if (child instanceof JComponent) {
                    child.putClientProperty(DEFAULT_DELEGATE_PROPERTY_CONSTRAINT, constraints)
                }
                builder.context.remove('constraints')
            } else {
                LayoutFactory.getLayoutTarget(parent).add(child)
            }
        } catch (MissingPropertyException mpe) {
            LayoutFactory.getLayoutTarget(parent).add(child)
        }
    }

}
