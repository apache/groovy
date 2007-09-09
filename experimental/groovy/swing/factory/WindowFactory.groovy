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

import groovy.swing.SwingBuilder
import java.awt.Dialog
import java.awt.Frame
import javax.swing.JWindow

public class WindowFactory implements Factory {
    
    public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
        SwingBuilder.checkValueIsNull(value, name);
        //TODO we could make the value arg the owner, or a window to allow adding more compnents
        JWindow window;
        Object owner = properties.remove("owner");
        LinkedList containingWindows = builder.getContainingWindows();
        // if owner not explicit, use the last window type in the list
        if ((owner == null) && !containingWindows.isEmpty()) {
            owner = containingWindows.getLast();
        }
        if (owner instanceof Frame) {
            window = new JWindow((Frame) owner);
        } else if (owner instanceof Dialog) {
            window = new JWindow((Dialog) owner);
        } else {
            window = new JWindow();
        }
        containingWindows.add(window);
        return window;
    }

}
