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

import groovy.swing.impl.TableLayout
import groovy.swing.impl.TableLayoutCell
import groovy.swing.impl.TableLayoutRow

import java.awt.*

/**
 * Factory for creating {@link TableLayout} nodes.
 */
public class TableLayoutFactory extends AbstractFactory {

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
        if (FactoryBuilderSupport.checkValueIsType(value, name, TableLayout)) {
            return value;
        }
        return new TableLayout();
    }

    /**
     * Attaches the current node to its parent when parent-specific handling is required.
     *
     * @param builder the factory builder
     * @param parent the parent node
     * @param child the child node
     */
    public void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
        if (builder.getParentFactory()) {
            builder.getParentFactory().setChild (builder, parent, child);
        }
    }
}

/**
 * Factory for creating {@link TableLayoutRow} nodes.
 */
public class TRFactory extends AbstractFactory {
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
        FactoryBuilderSupport.checkValueIsNull(value, name);
        //TODO we could make the value arg the parent
        Object parent = builder.getCurrent();
        if (parent instanceof TableLayout) {
            return new TableLayoutRow((TableLayout) parent);
        } else {
            throw new RuntimeException("'tr' must be within a 'tableLayout'");
        }
    }

    /**
     * Finalizes a node after its children have been processed.
     *
     * @param builder the factory builder
     * @param parent the parent node
     * @param node the current node
     */
    public void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
        node.addComponentsForRow()
    }
}

/**
 * Factory for creating {@link TableLayoutCell} nodes.
 */
public class TDFactory extends AbstractFactory {
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
        FactoryBuilderSupport.checkValueIsNull(value, name);
        //TODO we could make the value arg the TR
        Object parent = builder.getCurrent();
        if (parent instanceof TableLayoutRow) {
            return new TableLayoutCell((TableLayoutRow) parent);
        } else {
            throw new RuntimeException("'td' must be within a 'tr'");
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
        parent.addComponent(child)
    }
}
