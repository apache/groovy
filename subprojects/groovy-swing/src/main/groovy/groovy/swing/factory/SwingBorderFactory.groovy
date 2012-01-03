/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.swing.factory

import javax.swing.JComponent
import javax.swing.RootPaneContainer

abstract class SwingBorderFactory extends AbstractFactory {

    public boolean isLeaf() {
        // no children
        return true;
    }

    public boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
        // never do bean apply
        return false;
    }

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