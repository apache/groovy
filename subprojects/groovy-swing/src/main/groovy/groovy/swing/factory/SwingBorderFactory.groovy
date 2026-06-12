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
 * Base factory for Swing border nodes.
 */
abstract class SwingBorderFactory extends AbstractFactory {

    /**
     * Indicates whether nodes created by this factory accept children.
     *
     * @return true if child nodes are not expected
     */
    public boolean isLeaf() {
        // no children
        return true;
    }

    /**
     * Handles custom node attributes before default bean processing.
     *
     * @param builder the factory builder
     * @param node the current node
     * @param attributes the node attributes
     * @return true if default attribute handling should continue
     */
    public boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
        // never do bean apply
        return false;
    }

    /**
     * Attaches the current node to its parent when parent-specific handling is required.
     *
     * @param builder the factory builder
     * @param parent the parent node
     * @param child the child node
     */
    public void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
        if (builder.context.applyBorderToParent) {
            if (parent instanceof JComponent) {
                parent.setBorder(child);
            } else if (parent instanceof RootPaneContainer) {
                setParent(builder, parent.contentPane, child)
            } else {
                throw new RuntimeException("Border cannot be applied to parent, it is neither a JComponent or a RootPaneContainer")
            }
        }
    }


}
