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

import java.awt.Container
import javax.swing.BoxLayout
import org.codehaus.groovy.runtime.InvokerHelper

public class BoxLayoutFactory extends AbstractFactory {

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsNull(value, name);
        Object parent = builder.getCurrent();
        if (parent instanceof Container) {
            Object axisObject = attributes.remove("axis");
            int axis = BoxLayout.X_AXIS;
            if (axisObject != null) {
                Integer i = (Integer) axisObject;
                axis = i.intValue();
            }

            Container target = LayoutFactory.getLayoutTarget(parent);
            BoxLayout answer = new BoxLayout(target, axis);

            // now let's try to set the layout property
            InvokerHelper.setProperty(target, "layout", answer);
            return answer;
        } else {
            throw new RuntimeException("Must be nested inside a Container");
        }
    }

    public void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
        if (parent instanceof Container) {
            Container target = LayoutFactory.getLayoutTarget(parent);
            InvokerHelper.setProperty(target, "layout", child);
        }
    }

}
