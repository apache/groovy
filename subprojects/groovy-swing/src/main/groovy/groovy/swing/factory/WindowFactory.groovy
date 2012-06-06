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

import javax.swing.JWindow

public class WindowFactory extends RootPaneContainerFactory {
    
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        JWindow window;
        if (FactoryBuilderSupport.checkValueIsType(value, name, JWindow.class)) {
            window = value
        } else {
            LinkedList containingWindows = builder.containingWindows;
            Object owner = attributes.remove("owner");
            // if owner not explicit, use the last window type in the list
            if ((owner == null) && !containingWindows.empty) {
                owner = containingWindows.last;
            }
            if (owner) {
                // the joys of the MOP!
                window = new JWindow(owner);
            } else {
                window = new JWindow();
            }
        }

        handleRootPaneTasks(builder, window, attributes)

        return window;
    }

}
