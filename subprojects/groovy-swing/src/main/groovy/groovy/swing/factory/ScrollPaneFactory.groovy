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
 * Factory for creating scroll panes.
 */
class ScrollPaneFactory extends groovy.swing.factory.BeanFactory {

    /**
     * Creates a new factory for creating scroll panes
     */
    public ScrollPaneFactory() {
        this(JScrollPane)
    }

    /**
     * Creates a new factory for creating scroll panes
     *
     * @param klass the widget class to instantiate
     */
    public ScrollPaneFactory(Class klass) {
        super(klass, false);
    }

    /**
     * Attaches a child node to its parent.
     *
     * @param factory the factory builder
     * @param parent the parent node
     * @param child the child node
     */
    public void setChild(FactoryBuilderSupport factory, Object parent, Object child) {
        if (!(child instanceof Component) || (child instanceof Window)) {
            return;
        }
        if (parent.getViewport()?.getView() != null) {
            throw new RuntimeException("ScrollPane can only have one child component");
        }
        if (child instanceof JViewport) {
            parent.setViewport(child);
        } else {
            parent.setViewportView(child);
        }

    }

}
