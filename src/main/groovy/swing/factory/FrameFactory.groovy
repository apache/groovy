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
import javax.swing.JFrame

public class FrameFactory extends AbstractFactory {
    
    LinkedList/*<JFrame>*/ packers = new LinkedList([null])
    LinkedList/*<JFrame>*/ showers = new LinkedList([null])

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
        if (SwingBuilder.checkValueIsType(value, name, JFrame.class)) {
            return value;
        }
        JFrame frame = new JFrame();
        builder.getContainingWindows().add(frame);

        Object o = properties.remove("pack")
        if ((o instanceof Boolean) && ((Boolean) o).booleanValue()) {
            packers.add(frame)
        }
        o = properties.remove("show")
        if ((o instanceof Boolean) && ((Boolean) o).booleanValue()) {
            showers.add(frame)
        }

        builder.addDisposalClosure(frame.&dispose)

        return frame;
    }

    public void onNodeCompleted( FactoryBuilderSupport builder, Object parent, Object node ) {
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
