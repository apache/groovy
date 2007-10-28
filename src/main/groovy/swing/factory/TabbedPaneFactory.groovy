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

class TabbedPaneFactory extends BeanFactory {

    public TabbedPaneFactory(Class beanClass) {
        super(beanClass, false)
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        builder.context.tabbedPaneFactoryClosure = this.&inspectChild
        builder.addAttributeDelegate(builder.context.tabbedPaneFactoryClosure)
        return super.newInstance(builder, name, value, attributes)
    }

    public static void inspectChild(FactoryBuilderSupport builder, Object node, Map attributes) {
        Object name = attributes.remove('title')
        if (name) {
            builder.context.put(node, name)
        }
    }

    public void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        if (!(child instanceof Component) || (child instanceof Window)) {
            return;
        }
        try {
            def title = builder.context[child]
            if (title != null) {
                parent.add(title, child)
            } else {
                parent.add(child)
            }
        } catch (MissingPropertyException mpe) {
            parent.add(child)
        }
    }

    public void onNodeCompleted( FactoryBuilderSupport builder, Object parent, Object node ) {
        super.onNodeCompleted (builder, parent, node)
        builder.removeAttributeDelegate(builder.context.tabbedPaneFactoryClosure)
    }


}