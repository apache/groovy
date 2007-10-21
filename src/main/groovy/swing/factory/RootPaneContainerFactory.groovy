/*
 * Copyright 2003-2007 the original author or authors.
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

import java.awt.Component
import java.awt.Window
import javax.swing.JButton

abstract class RootPaneContainerFactory extends AbstractFactory {

    public void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        if (!(child instanceof Component) || (child instanceof Window)) {
            return;
        }
        try {
            def constraints = builder.context.constraints
            if (constraints != null) {
                parent.contentPane.add(child, constraints)
            } else {
                parent.contentPane.add(child)
            }
        } catch (MissingPropertyException mpe) {
            parent.contentPane.add(child)
        }
    }

    public void handleRootPaneTasks(FactoryBuilderSupport builder, Window container, Map attributes) {
        builder.context.defaultButtonDelegate =
            builder.addAttributeDelegate {myBuilder, node, myAttributes ->
                if (myAttributes.defaultButton && (node instanceof JButton)) {
                    container.rootPane.defaultButton = node
                    myAttributes.remove('defaultButton')
                }
            }

        builder.containingWindows.add(container)

        builder.context.pack = attributes.remove('pack')
        builder.context.show = attributes.remove('show')

        builder.addDisposalClosure(container.&dispose)
    }

    public void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
        if (node instanceof Window) {
            def containingWindows = builder.containingWindows
            if (!containingWindows.empty && containingWindows.last == node) {
                containingWindows.removeLast();
            }
        }

        if (builder.context.pack) {
            node.pack()
        }
        if (builder.context.show) {
            node.visible = true
        }

        builder.removeAttributeDelegate(builder.context.defaultButtonDelegate)
    }

}