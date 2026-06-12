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
import javax.swing.JPopupMenu.Separator as JPopupMenu_Separator
import javax.swing.JToolBar.Separator as JToolBar_Separator

// JetGroovy bug

/**
 * Factory for creating separator components appropriate for the current parent.
 */
public class SeparatorFactory extends AbstractFactory {

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
        Object parent = builder.getCurrent();
        if (parent instanceof JMenu) {
            return new JPopupMenu_Separator();
        } else if (parent instanceof JToolBar) {
            return new JToolBar_Separator();
        } else {
            return new JSeparator();
        }
    }
}
