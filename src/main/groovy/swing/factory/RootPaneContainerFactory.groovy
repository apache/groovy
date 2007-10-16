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

abstract class RootPaneContainerFactory extends AbstractFactory {

    LinkedList packers = new LinkedList([null])
    LinkedList showers = new LinkedList([null])

    public void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        if (!(child instanceof Component) || (child instanceof Window)) {
            return;
        }
        try {
            def constraints = builder.constraints
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

        builder.containingWindows.add(container)

        Object o = attributes.remove("pack")
        if ((o instanceof Boolean) && ((Boolean) o).booleanValue()) {
            packers.add(container)
        }
        o = attributes.remove("show")
        if ((o instanceof Boolean) && ((Boolean) o).booleanValue()) {
            showers.add(container)
        }

        builder.addDisposalClosure(container.&dispose)
    }

    public void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
        if (node instanceof Window) {
            def containingWindows = builder.containingWindows
            if (!containingWindows.empty && containingWindows.last == node) {
                containingWindows.removeLast();
            }
        }

        if (packers.last.is(node)) {
            node.pack()
            packers.removeLast()
        }
        if (showers.last.is(node)) {
            node.visible = true
            showers.removeLast()
        }
    }


}